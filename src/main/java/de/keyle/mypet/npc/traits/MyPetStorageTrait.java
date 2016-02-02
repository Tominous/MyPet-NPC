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

import de.Keyle.MyPet.api.player.MyPetPlayer;
import de.Keyle.MyPet.api.repository.RepositoryCallback;
import de.Keyle.MyPet.entity.types.InactiveMyPet;
import de.Keyle.MyPet.entity.types.MyPet;
import de.Keyle.MyPet.repository.MyPetList;
import de.Keyle.MyPet.repository.PlayerList;
import de.Keyle.MyPet.util.Util;
import de.Keyle.MyPet.util.WorldGroup;
import de.Keyle.MyPet.util.hooks.Economy;
import de.Keyle.MyPet.util.hooks.Permissions;
import de.Keyle.MyPet.util.iconmenu.IconMenu;
import de.Keyle.MyPet.util.iconmenu.IconMenuItem;
import de.Keyle.MyPet.util.locale.Translation;
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

public class MyPetStorageTrait extends Trait {
    public MyPetStorageTrait() {
        super("mypet-storage");
    }

    @EventHandler
    public void onRightClick(final NPCRightClickEvent npcEvent) {
        if (this.npc != npcEvent.getNPC()) {
            return;
        }

        final Player player = npcEvent.getClicker();

        if (!Permissions.has(player, "MyPet.npc.storage.interact")) {
            player.sendMessage(Translation.getString("Message.No.Allowed", player));
            return;
        }


        if (PlayerList.isMyPetPlayer(player)) {
            final MyPetPlayer myPetPlayer = PlayerList.getMyPetPlayer(player);
            assert myPetPlayer != null;
            if (myPetPlayer.hasMyPet()) {

                final NPC npc = this.npc;

                MyPetList.getInactiveMyPets(myPetPlayer, new RepositoryCallback<List<InactiveMyPet>>() {
                    @Override
                    public void callback(List<InactiveMyPet> pets) {
                        WorldGroup wg = WorldGroup.getGroupByWorld(myPetPlayer.getPlayer().getWorld().getName());
                        int inactivePetCount = 0;
                        UUID activePetUUID = myPetPlayer.getMyPet().getUUID();

                        for (InactiveMyPet mypet : pets) {
                            if (activePetUUID.equals(mypet.getUUID()) || !mypet.getWorldGroup().equals("") || !mypet.getWorldGroup().equals(wg.getName())) {
                                continue;
                            }
                            inactivePetCount++;
                        }

                        int maxPetCount = 0;
                        if (!player.isOp()) {
                            for (int i = 1; i <= 27; i++) {
                                if (Permissions.has(player, "MyPet.npc.storage.max." + (55 - i))) {
                                    maxPetCount = 55 - i;
                                    break;
                                }
                                if (Permissions.has(player, "MyPet.npc.storage.max." + i)) {
                                    maxPetCount = i;
                                    break;
                                }
                            }
                        } else {
                            maxPetCount = 54;
                        }
                        if (inactivePetCount >= maxPetCount) {
                            player.sendMessage(Util.formatText(Translation.getString("Message.Npc.StorageFull", myPetPlayer), npc.getFullName(), maxPetCount));
                            return;
                        }
                        IconMenu menu = new IconMenu(Translation.getString("Message.Npc.HandOverTitle", myPetPlayer), 9, new IconMenu.OptionClickEventHandler() {
                            @Override
                            public void onOptionClick(IconMenu.OptionClickEvent event) {
                                if (event.getPosition() == 3) {
                                    boolean store = true;
                                    double costs = calculateStorageCosts(myPetPlayer.getMyPet());
                                    if (Economy.canUseEconomy() && costs > 0 && npc.hasTrait(MyPetWalletTrait.class)) {
                                        MyPetWalletTrait walletTrait = npc.getTrait(MyPetWalletTrait.class);
                                        if (!Economy.canPay(myPetPlayer, costs)) {
                                            player.sendMessage(Util.formatText(Translation.getString("Message.No.Money", myPetPlayer), myPetPlayer.getMyPet().getPetName(), npcEvent.getNPC().getName()));
                                            store = false;
                                        }
                                        if (Economy.pay(myPetPlayer, costs)) {
                                            walletTrait.deposit(costs);
                                        } else {
                                            store = false;
                                        }
                                    }

                                    if (store) {
                                        MyPet myPet = myPetPlayer.getMyPet();
                                        if (MyPetList.deactivateMyPet(myPetPlayer)) {
                                            // remove pet from world groups
                                            String wg = myPetPlayer.getWorldGroupForMyPet(myPet.getUUID());
                                            myPetPlayer.setMyPetForWorldGroup(wg, null);

                                            player.sendMessage(Util.formatText(Translation.getString("Message.Npc.HandOver", myPetPlayer), myPet.getPetName(), npcEvent.getNPC().getName()));
                                        }
                                    }
                                }
                                event.setWillClose(true);
                                event.setWillDestroy(true);
                            }
                        }, MyPetNpcPlugin.getPlugin());
                        String[] lore;
                        double storageCosts = calculateStorageCosts(myPetPlayer.getMyPet());
                        if (Economy.canUseEconomy() && npc.hasTrait(MyPetWalletTrait.class) && storageCosts > 0) {
                            lore = new String[3];
                            lore[1] = "";
                            lore[2] = RESET + Translation.getString("Name.Costs", myPetPlayer) + ": " + (Economy.canPay(myPetPlayer, storageCosts) ? GREEN : RED) + storageCosts + DARK_GREEN + " " + Economy.getEconomy().currencyNameSingular();
                        } else {
                            lore = new String[1];
                        }
                        lore[0] = RESET + Util.formatText(Translation.getString("Message.Npc.YesHandOver", myPetPlayer), myPetPlayer.getMyPet().getPetName());
                        menu.setOption(3, new IconMenuItem().setMaterial(Material.WOOL).setData(5).setTitle(GREEN + Translation.getString("Name.Yes", myPetPlayer)).setLore(lore));
                        menu.setOption(5, new IconMenuItem().setMaterial(Material.WOOL).setData(14).setTitle(RED + Translation.getString("Name.No", myPetPlayer)).setLore(RESET + Util.formatText(Translation.getString("Message.Npc.NoHandOver", myPetPlayer), myPetPlayer.getMyPet().getPetName())));
                        menu.open(player);
                    }
                });
            } else {
                MyPetSelectionGui gui = new MyPetSelectionGui(myPetPlayer, Translation.getString("Message.Npc.TakeTitle", myPetPlayer));
                gui.open(new RepositoryCallback<InactiveMyPet>() {
                    @Override
                    public void callback(InactiveMyPet myPet) {
                        MyPet activePet = MyPetList.activateMyPet(myPet);
                        if (activePet != null) {
                            Player player = myPetPlayer.getPlayer();
                            activePet.sendMessageToOwner(Util.formatText(Translation.getString("Message.Npc.ChosenPet", myPetPlayer), activePet.getPetName()));
                            WorldGroup wg = WorldGroup.getGroupByWorld(player.getWorld().getName());
                            myPetPlayer.setMyPetForWorldGroup(wg.getName(), activePet.getUUID());

                            switch (activePet.createPet()) {
                                case Canceled:
                                    activePet.sendMessageToOwner(Util.formatText(Translation.getString("Message.Spawn.Prevent", myPetPlayer), activePet.getPetName()));
                                    break;
                                case NoSpace:
                                    activePet.sendMessageToOwner(Util.formatText(Translation.getString("Message.Spawn.NoSpace", myPetPlayer), activePet.getPetName()));
                                    break;
                                case NotAllowed:
                                    activePet.sendMessageToOwner(Translation.getString("Message.No.AllowedHere", myPetPlayer).replace("%petname%", activePet.getPetName()));
                                    break;
                                case Dead:
                                    activePet.sendMessageToOwner(Translation.getString("Message.Spawn.Respawn.In", myPetPlayer).replace("%petname%", activePet.getPetName()).replace("%time%", "" + activePet.getRespawnTime()));
                                    break;
                            }
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