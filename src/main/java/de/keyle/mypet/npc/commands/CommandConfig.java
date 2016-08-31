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

import de.Keyle.MyPet.api.MyPetVersion;
import de.Keyle.MyPet.api.commands.CommandOption;
import de.Keyle.MyPet.api.commands.CommandOptionTabCompleter;
import de.Keyle.MyPet.api.player.Permissions;
import de.Keyle.MyPet.commands.CommandAdmin;
import de.keyle.mypet.npc.commands.options.CommandOptionShop;
import de.keyle.mypet.npc.commands.options.CommandOptionWallet;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class CommandConfig implements CommandExecutor, TabCompleter {
    private static List<String> optionsList = new ArrayList<>();
    private static Map<String, CommandOption> commandOptions = new HashMap<>();

    public CommandConfig() {
        commandOptions.put("wallet", new CommandOptionWallet());
        if (MyPetVersion.isPremium()) {
            commandOptions.put("shop", new CommandOptionShop());
        }

        if (optionsList.size() != commandOptions.keySet().size()) {
            optionsList = new ArrayList<>(commandOptions.keySet());
            Collections.sort(optionsList);
        }
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            if (!Permissions.has((Player) sender, "MyPet.npc.admin", false)) {
                return true;
            }
        }

        if (args.length < 1) {
            return false;
        }

        String[] parameter = Arrays.copyOfRange(args, 1, args.length);
        CommandOption option = commandOptions.get(args[0].toLowerCase());

        if (option != null) {
            return option.onCommandOption(sender, parameter);
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof Player) {
            if (!Permissions.has((Player) commandSender, "MyPet.npc.admin", false)) {
                return CommandAdmin.EMPTY_LIST;
            }
        }
        if (strings.length == 1) {
            return optionsList;
        } else if (strings.length >= 1) {
            CommandOption co = commandOptions.get(strings[0]);
            if (co != null) {
                if (co instanceof CommandOptionTabCompleter) {
                    return ((CommandOptionTabCompleter) co).onTabComplete(commandSender, strings);
                }
            }
        }
        return CommandAdmin.EMPTY_LIST;
    }
}