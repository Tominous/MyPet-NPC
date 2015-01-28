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

package de.keyle.mypet.npc.commands;

import de.Keyle.MyPet.util.BukkitUtil;
import de.Keyle.MyPet.util.hooks.Economy;
import de.Keyle.MyPet.util.hooks.Permissions;
import de.keyle.mypet.npc.traits.MyPetWalletTrait;
import de.keyle.mypet.npc.traits.MyPetWalletTrait.WalletType;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandConfig implements CommandExecutor, TabCompleter {
    private static List<String> optionsList = new ArrayList<String>();
    private static List<String> walletTypeList = new ArrayList<String>();
    private static List<String> emptyList = new ArrayList<String>();

    static {
        optionsList.add("wallet");

        for (WalletType walletType : WalletType.values()) {
            walletTypeList.add(walletType.name());
        }
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String lang = "en";
        if (sender instanceof Player) {
            if (!Permissions.has((Player) sender, "MyPet.npc.admin", false)) {
                return true;
            }
            lang = BukkitUtil.getPlayerLanguage((Player) sender);
        }
        if (args.length < 1) {
            return false;
        }
        String option = args[0];
        String[] parameter = Arrays.copyOfRange(args, 1, args.length);

        if (option.equalsIgnoreCase("wallet") && parameter.length >= 1) {
            NPC selectedNPC = CitizensAPI.getDefaultNPCSelector().getSelected(sender);
            if (selectedNPC == null) {
                sender.sendMessage("[" + ChatColor.AQUA + "MyPet-NPC" + ChatColor.RESET + "] No NPC seleced!");
                return true;
            }

            if (!selectedNPC.hasTrait(MyPetWalletTrait.class)) {
                sender.sendMessage("[" + ChatColor.AQUA + "MyPet-NPC" + ChatColor.RESET + "] This NPC doesn't has the " + ChatColor.GOLD + "mypet-wallet" + ChatColor.RESET + " trait!");
                return true;
            }

            WalletType newWalletType = WalletType.getByName(parameter[0]);
            if (newWalletType == null) {
                sender.sendMessage("[" + ChatColor.AQUA + "MyPet-NPC" + ChatColor.RESET + "] Invalid wallet type!");
                return true;
            }

            MyPetWalletTrait trait = selectedNPC.getTrait(MyPetWalletTrait.class);

            if (!Economy.canUseEconomy()) {
                if (newWalletType == WalletType.Bank || newWalletType == WalletType.Owner) {
                    sender.sendMessage("[" + ChatColor.AQUA + "MyPet-NPC" + ChatColor.RESET + "] You can not use the \"Owner\" and \"Bank\" wallet types without an economy plugin installed!");
                    return true;
                }
            } else {
                if (newWalletType == WalletType.Bank && !Economy.getEconomy().hasBankSupport()) {
                    sender.sendMessage("[" + ChatColor.AQUA + "MyPet-NPC" + ChatColor.RESET + "] Your economy plugin doesn't has \"Banks\" support!");
                    return true;
                }
            }

            trait.setWalletType(newWalletType);

            if (parameter.length >= 2) {
                trait.setAccount(parameter[1]);
            }

            sender.sendMessage("[" + ChatColor.AQUA + "MyPet-NPC" + ChatColor.RESET + "] wallet trait updated.");
        } else if (option.equalsIgnoreCase("test")) {

        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!Permissions.has((Player) commandSender, "MyPet.npc.admin", false)) {
            return emptyList;
        }
        if (strings.length == 1) {
            return optionsList;
        } else if (strings.length >= 1) {
            if (strings[0].equalsIgnoreCase("wallet")) {
                if (strings.length == 2) {
                    return walletTypeList;
                }
            }
        }
        return emptyList;
    }
}