/*
 * This file is part of MyPet-NPC
 *
 * Copyright (C) 2011-2013 Keyle
 * MyPet-NPC is licensed under the GNU Lesser General Public License.
 *
 * MyPet-NPC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyPet-NPC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package de.keyle.mypet.npc.traits;

import de.Keyle.MyPet.MyPetApi;
import de.Keyle.MyPet.api.Util;
import de.Keyle.MyPet.api.WorldGroup;
import de.Keyle.MyPet.api.entity.MyPet;
import de.Keyle.MyPet.api.entity.StoredMyPet;
import de.Keyle.MyPet.api.player.MyPetPlayer;
import de.Keyle.MyPet.api.player.Permissions;
import de.Keyle.MyPet.api.repository.RepositoryCallback;
import de.Keyle.MyPet.api.util.hooks.EconomyHook;
import de.Keyle.MyPet.api.util.inventory.IconMenu;
import de.Keyle.MyPet.api.util.inventory.IconMenuItem;
import de.Keyle.MyPet.api.util.locale.Translation;
import de.Keyle.MyPet.util.selectionmenu.MyPetSelectionGui;
import de.keyle.mypet.npc.MyPetNpcPlugin;
import de.keyle.mypet.npc.util.Configuration;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.List;
import java.util.UUID;

import static org.bukkit.ChatColor.*;

public class StorageTrait extends Trait {
    public StorageTrait() {
        super("mypet-storage");
    }

    @EventHandler
    public void onRightClick(final NPCRightClickEvent npcEvent) {
        if (this.npc != npcEvent.getNPC()) {
            return;
        }

        final Player player = npcEvent.getClicker();

        if (!Permissions.has(player, "MyPet.npc.storage")) {
            player.sendMessage(Translation.getString("Message.No.Allowed", player));
            return;
        }


        if (MyPetApi.getPlayerManager().isMyPetPlayer(player)) {
            final MyPetPlayer myPetPlayer = MyPetApi.getPlayerManager().getMyPetPlayer(player);
            assert myPetPlayer != null;
            if (myPetPlayer.hasMyPet()) {

                final NPC npc = this.npc;

                MyPetApi.getRepository().getMyPets(myPetPlayer, new RepositoryCallback<List<StoredMyPet>>() {
                    @Override
                    public void callback(List<StoredMyPet> pets) {
                        WorldGroup wg = WorldGroup.getGroupByWorld(myPetPlayer.getPlayer().getWorld().getName());
                        int inactivePetCount = 0;
                        UUID activePetUUID = myPetPlayer.getMyPet().getUUID();

                        for (StoredMyPet mypet : pets) {
                            if (activePetUUID.equals(mypet.getUUID()) || (!mypet.getWorldGroup().equals("") && !mypet.getWorldGroup().equals(wg.getName()))) {
                                continue;
                            }
                            inactivePetCount++;
                        }

                        int maxPetCount = 0;
                        if (!player.isOp()) {
                            for (int i = 54; i > 0; i--) {
                                if (Permissions.has(player, "MyPet.npc.storage.max." + i)) {
                                    maxPetCount = i;
                                    break;
                                }
                            }
                        } else {
                            maxPetCount = 54;
                        }

                        if (inactivePetCount >= maxPetCount) {
                            if (Permissions.has(player, "MyPet.npc.storage.bypass")) {

                                String stats = "(" + inactivePetCount + "/" + maxPetCount + ")";

                                final MyPetSelectionGui gui = new MyPetSelectionGui(myPetPlayer, stats + " " + Translation.getString("Message.Npc.SwitchTitle", player));
                                gui.open(pets, new RepositoryCallback<StoredMyPet>() {
                                    @Override
                                    public void callback(StoredMyPet storedMyPet) {
                                        MyPetApi.getMyPetManager().deactivateMyPet(myPetPlayer, true);
                                        MyPet activePet = MyPetApi.getMyPetManager().activateMyPet(storedMyPet);
                                        if (activePet != null && myPetPlayer.isOnline()) {
                                            Player p = myPetPlayer.getPlayer();
                                            activePet.getOwner().sendMessage(Util.formatText(Translation.getString("Message.Npc.ChosenPet", player), activePet.getPetName()));
                                            WorldGroup wg = WorldGroup.getGroupByWorld(p.getWorld().getName());
                                            myPetPlayer.setMyPetForWorldGroup(wg.getName(), activePet.getUUID());

                                            switch (activePet.createEntity()) {
                                                case Canceled:
                                                    activePet.getOwner().sendMessage(Util.formatText(Translation.getString("Message.Spawn.Prevent", player), activePet.getPetName()));
                                                    break;
                                                case NoSpace:
                                                    activePet.getOwner().sendMessage(Util.formatText(Translation.getString("Message.Spawn.NoSpace", player), activePet.getPetName()));
                                                    break;
                                                case NotAllowed:
                                                    activePet.getOwner().sendMessage(Translation.getString("Message.No.AllowedHere", player).replace("%petname%", activePet.getPetName()));
                                                    break;
                                                case Dead:
                                                    activePet.getOwner().sendMessage(Translation.getString("Message.Spawn.Respawn.In", player).replace("%petname%", activePet.getPetName()).replace("%time%", "" + activePet.getRespawnTime()));
                                                    break;
                                            }
                                        }
                                    }
                                });
                            } else {
                                player.sendMessage(Util.formatText(Translation.getString("Message.Npc.StorageFull", myPetPlayer), npc.getFullName(), maxPetCount));
                            }
                        } else {
                            IconMenu menu = new IconMenu(Translation.getString("Message.Npc.HandOverTitle", myPetPlayer), new IconMenu.OptionClickEventHandler() {
                                @Override
                                public void onOptionClick(IconMenu.OptionClickEvent event) {
                                    if (event.getPosition() == 3) {
                                        boolean store = true;
                                        double costs = calculateStorageCosts(myPetPlayer.getMyPet());
                                        if (EconomyHook.canUseEconomy() && costs > 0 && npc.hasTrait(WalletTrait.class)) {
                                            WalletTrait walletTrait = npc.getTrait(WalletTrait.class);
                                            if (!EconomyHook.canPay(myPetPlayer, costs)) {
                                                player.sendMessage(Util.formatText(Translation.getString("Message.No.Money", myPetPlayer), myPetPlayer.getMyPet().getPetName(), npcEvent.getNPC().getName()));
                                                store = false;
                                            }
                                            if (EconomyHook.pay(myPetPlayer, costs)) {
                                                walletTrait.deposit(costs);
                                            } else {
                                                store = false;
                                            }
                                        }

                                        if (store) {
                                            StoredMyPet storedMyPet = myPetPlayer.getMyPet();
                                            if (MyPetApi.getMyPetManager().deactivateMyPet(myPetPlayer, true)) {
                                                // remove pet from world groups
                                                String wg = myPetPlayer.getWorldGroupForMyPet(storedMyPet.getUUID());
                                                myPetPlayer.setMyPetForWorldGroup(wg, null);
                                                MyPetApi.getRepository().updateMyPetPlayer(myPetPlayer, null);

                                                player.sendMessage(Util.formatText(Translation.getString("Message.Npc.HandOver", myPetPlayer), storedMyPet.getPetName(), npcEvent.getNPC().getName()));
                                            }
                                        }
                                    }
                                    event.setWillClose(true);
                                    event.setWillDestroy(true);
                                }
                            }, MyPetNpcPlugin.getPlugin());
                            String[] lore;
                            double storageCosts = calculateStorageCosts(myPetPlayer.getMyPet());
                            if (EconomyHook.canUseEconomy() && npc.hasTrait(WalletTrait.class) && storageCosts > 0) {
                                lore = new String[3];
                                lore[1] = "";
                                lore[2] = RESET + Translation.getString("Name.Costs", myPetPlayer) + ": " + (EconomyHook.canPay(myPetPlayer, storageCosts) ? GREEN : RED) + storageCosts + DARK_GREEN + " " + EconomyHook.getEconomy().currencyNameSingular();
                            } else {
                                lore = new String[1];
                            }
                            lore[0] = RESET + Util.formatText(Translation.getString("Message.Npc.YesHandOver", myPetPlayer), myPetPlayer.getMyPet().getPetName());
                            menu.setOption(3, new IconMenuItem().setMaterial(Material.WOOL).setData(5).setTitle(GREEN + Translation.getString("Name.Yes", myPetPlayer)).setLore(lore));
                            menu.setOption(5, new IconMenuItem().setMaterial(Material.WOOL).setData(14).setTitle(RED + Translation.getString("Name.No", myPetPlayer)).setLore(RESET + Util.formatText(Translation.getString("Message.Npc.NoHandOver", myPetPlayer), myPetPlayer.getMyPet().getPetName())));
                            menu.open(player);
                        }
                    }
                });
            } else {
                final MyPetSelectionGui gui = new MyPetSelectionGui(myPetPlayer, Translation.getString("Message.Npc.TakeTitle", myPetPlayer));
                MyPetApi.getRepository().getMyPets(myPetPlayer, new RepositoryCallback<List<StoredMyPet>>() {
                    @Override
                    public void callback(List<StoredMyPet> pets) {
                        if (pets.size() > 0) {
                            gui.open(pets, new RepositoryCallback<StoredMyPet>() {
                                @Override
                                public void callback(StoredMyPet storedMyPet) {
                                    MyPet myPet = MyPetApi.getMyPetManager().activateMyPet(storedMyPet);
                                    if (myPet != null) {
                                        Player player = myPetPlayer.getPlayer();
                                        myPet.getOwner().sendMessage(Util.formatText(Translation.getString("Message.Npc.ChosenPet", myPetPlayer), myPet.getPetName()));
                                        WorldGroup wg = WorldGroup.getGroupByWorld(player.getWorld().getName());
                                        myPetPlayer.setMyPetForWorldGroup(wg.getName(), myPet.getUUID());
                                        MyPetApi.getRepository().updateMyPetPlayer(myPetPlayer, null);

                                        switch (myPet.createEntity()) {
                                            case Canceled:
                                                myPet.getOwner().sendMessage(Util.formatText(Translation.getString("Message.Spawn.Prevent", myPetPlayer), myPet.getPetName()));
                                                break;
                                            case NoSpace:
                                                myPet.getOwner().sendMessage(Util.formatText(Translation.getString("Message.Spawn.NoSpace", myPetPlayer), myPet.getPetName()));
                                                break;
                                            case NotAllowed:
                                                myPet.getOwner().sendMessage(Translation.getString("Message.No.AllowedHere", myPetPlayer).replace("%petname%", myPet.getPetName()));
                                                break;
                                            case Dead:
                                                myPet.getOwner().sendMessage(Translation.getString("Message.Spawn.Respawn.In", myPetPlayer).replace("%petname%", myPet.getPetName()).replace("%time%", "" + myPet.getRespawnTime()));
                                                break;
                                        }
                                    }
                                }
                            });
                        } else {
                            myPetPlayer.sendMessage(Translation.getString("Message.No.HasPet", myPetPlayer));
                        }
                    }
                });
            }
            return;
        }
        player.sendMessage(Translation.getString("Message.No.HasPet", player));
    }

    public double calculateStorageCosts(MyPet myPet) {
        return Configuration.NPC_STORAGE_COSTS_FIXED + (myPet.getExperience().getLevel() * Configuration.NPC_STORAGE_COSTS_FACTOR);
    }
}