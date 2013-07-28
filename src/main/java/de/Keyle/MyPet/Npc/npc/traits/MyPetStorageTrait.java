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

package de.Keyle.MyPet.Npc.npc.traits;

import de.Keyle.MyPet.Npc.MyPetNpcPlugin;
import de.Keyle.MyPet.Npc.util.SpawnerEggTypes;
import de.Keyle.MyPet.api.event.MyPetSpoutEvent;
import de.Keyle.MyPet.api.event.MyPetSpoutEvent.MyPetSpoutEventReason;
import de.Keyle.MyPet.entity.types.InactiveMyPet;
import de.Keyle.MyPet.entity.types.MyPet;
import de.Keyle.MyPet.entity.types.MyPetList;
import de.Keyle.MyPet.util.*;
import de.Keyle.MyPet.util.locale.Locales;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import static org.bukkit.Bukkit.getPluginManager;

public class MyPetStorageTrait extends Trait
{
    public MyPetStorageTrait()
    {
        super("mypet-storage");
    }

    @EventHandler
    public void onRightClick(final NPCRightClickEvent npcEvent)
    {
        if (this.npc != npcEvent.getNPC())
        {
            return;
        }

        Player player = npcEvent.getClicker();

        if (!Permissions.has(player, "MyPet.Npc.interact"))
        {
            player.sendMessage(Locales.getString("Message.NotAllowed", player));
            return;
        }

        if (MyPetPlayer.isMyPetPlayer(player))
        {
            final MyPetPlayer myPetPlayer = MyPetPlayer.getMyPetPlayer(player);
            if (myPetPlayer.hasMyPet())
            {
                WorldGroup wg = WorldGroup.getGroup(myPetPlayer.getPlayer().getWorld().getName());
                int inactivePetCount = 0;
                for (InactiveMyPet mypet : myPetPlayer.getInactiveMyPets())
                {
                    if (!mypet.getWorldGroup().equals("") && !mypet.getWorldGroup().equals(wg.getName()))
                    {
                        continue;
                    }
                    inactivePetCount++;
                }
                int maxPetCount = 0;
                for (int i = 54 ; i >= 9 ; i -= 9)
                {
                    if (Permissions.has(player, "MyPet.Npc.max." + i))
                    {
                        maxPetCount = i;
                        break;
                    }
                }
                if (inactivePetCount >= maxPetCount)
                {
                    player.sendMessage(Util.formatText(Locales.getString("Message.Npc.StorageFull", myPetPlayer), this.npc.getFullName(), maxPetCount));
                    return;
                }
                IconMenu menu = new IconMenu(Locales.getString("Message.Npc.HandOverTitle", myPetPlayer), 9, new IconMenu.OptionClickEventHandler()
                {
                    @Override
                    public void onOptionClick(IconMenu.OptionClickEvent event)
                    {
                        if (event.getPosition() == 3)
                        {
                            // remove pet from world groups
                            String wg = myPetPlayer.getWorldGroupForMyPet(myPetPlayer.getMyPet().getUUID());
                            myPetPlayer.setMyPetForWorldGroup(wg, null);

                            event.getPlayer().sendMessage(Util.formatText(Locales.getString("Message.Npc.HandOver", myPetPlayer), myPetPlayer.getMyPet().getPetName(), npcEvent.getNPC().getName()));
                            MyPetList.setMyPetInactive(myPetPlayer);
                        }
                        event.setWillClose(true);
                        event.setWillDestroy(true);
                    }
                }, MyPetNpcPlugin.getPlugin());

                menu.setOption(3, new ItemStack(Material.WOOL, 0, (short) 5), ChatColor.GREEN + Locales.getString("Name.Yes", myPetPlayer), new String[]{ChatColor.RESET + Util.formatText(Locales.getString("Message.Npc.YesHandOver", myPetPlayer), myPetPlayer.getMyPet().getPetName())});
                menu.setOption(5, new ItemStack(Material.WOOL, 0, (short) 14), ChatColor.RED + Locales.getString("Name.No", myPetPlayer), new String[]{ChatColor.RESET + Util.formatText(Locales.getString("Message.Npc.NoHandOver", myPetPlayer), myPetPlayer.getMyPet().getPetName())});
                menu.open(player);
            }
            else if (myPetPlayer.hasInactiveMyPets())
            {
                final Map<Integer, UUID> petSlotList = new HashMap<Integer, UUID>();
                IconMenu menu = new IconMenu(Locales.getString("Message.Npc.TakeTitle", myPetPlayer), 54, new IconMenu.OptionClickEventHandler()
                {
                    @Override
                    public void onOptionClick(IconMenu.OptionClickEvent event)
                    {
                        if (petSlotList.containsKey(event.getPosition()))
                        {
                            InactiveMyPet myPet = myPetPlayer.getInactiveMyPet(petSlotList.get(event.getPosition()));
                            if (myPet != null)
                            {
                                MyPet activePet = MyPetList.setMyPetActive(myPet);
                                event.getPlayer().sendMessage(Util.formatText(Locales.getString("Message.Npc.ChosenPet", myPetPlayer), activePet.getPetName()));
                                WorldGroup wg = WorldGroup.getGroup(event.getPlayer().getWorld().getName());
                                myPetPlayer.setMyPetForWorldGroup(wg.getName(), activePet.getUUID());

                                switch (activePet.createPet())
                                {
                                    case Success:
                                        if (Configuration.ENABLE_EVENTS)
                                        {
                                            getPluginManager().callEvent(new MyPetSpoutEvent(activePet, MyPetSpoutEventReason.Call));
                                        }
                                        break;
                                    case Canceled:
                                        event.getPlayer().sendMessage(Util.formatText(Locales.getString("Message.SpawnPrevent", myPetPlayer), activePet.getPetName()));
                                        break;
                                    case NoSpace:
                                        event.getPlayer().sendMessage(Util.formatText(Locales.getString("Message.SpawnNoSpace", myPetPlayer), activePet.getPetName()));
                                        break;
                                    case NotAllowed:
                                        event.getPlayer().sendMessage(Colorizer.setColors(Locales.getString("Message.NotAllowedHere", myPetPlayer)).replace("%petname%", activePet.getPetName()));
                                        break;
                                    case Dead:
                                        event.getPlayer().sendMessage(Colorizer.setColors(Locales.getString("Message.RespawnIn", myPetPlayer)).replace("%petname%", activePet.getPetName()).replace("%time%", "" + activePet.getRespawnTime()));
                                        break;
                                }
                            }
                        }
                        event.setWillClose(true);
                        event.setWillDestroy(true);
                    }
                }, MyPetNpcPlugin.getPlugin());

                WorldGroup wg = WorldGroup.getGroup(myPetPlayer.getPlayer().getWorld().getName());
                for (int i = 0 ; i < myPetPlayer.getInactiveMyPets().size() && i < 54 ; i++)
                {
                    InactiveMyPet mypet = myPetPlayer.getInactiveMyPets().get(i);
                    if (!mypet.getWorldGroup().equals("") && !mypet.getWorldGroup().equals(wg.getName()))
                    {
                        continue;
                    }

                    List<String> lore = new ArrayList<String>();
                    lore.add(ChatColor.RESET + Locales.getString("Name.Hunger", myPetPlayer) + ": " + ChatColor.GOLD + mypet.getHungerValue());
                    if (mypet.getRespawnTime() > 0)
                    {
                        lore.add(ChatColor.RESET + Locales.getString("Name.Respawntime", myPetPlayer) + ": " + ChatColor.GOLD + mypet.getRespawnTime() + "sec");
                    }
                    else
                    {
                        lore.add(ChatColor.RESET + Locales.getString("Name.HP", myPetPlayer) + ": " + ChatColor.GOLD + String.format("%1.2f", mypet.getHealth()));
                    }
                    lore.add(ChatColor.RESET + Locales.getString("Name.Exp", myPetPlayer) + ": " + ChatColor.GOLD + String.format("%1.2f", mypet.getExp()));
                    lore.add(ChatColor.RESET + Locales.getString("Name.Type", myPetPlayer) + ": " + ChatColor.GOLD + mypet.getPetType().getTypeName());
                    lore.add(ChatColor.RESET + Locales.getString("Name.Skilltree", myPetPlayer) + ": " + ChatColor.GOLD + (mypet.getSkillTree() != null ? mypet.getSkillTree().getDisplayName() : "-"));
                    int pos = menu.addOption(new ItemStack(Material.MONSTER_EGG, 0, SpawnerEggTypes.getColor(mypet.getPetType())), ChatColor.AQUA + mypet.getPetName(), lore);
                    petSlotList.put(pos, mypet.getUUID());
                }

                menu.open(player);
            }
            return;
        }
        player.sendMessage(Locales.getString("Message.DontHavePet", player));
    }
}