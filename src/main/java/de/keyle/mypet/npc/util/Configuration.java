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

package de.keyle.mypet.npc.util;

import de.Keyle.MyPet.api.util.configuration.ConfigurationYAML;
import org.bukkit.configuration.file.FileConfiguration;

public class Configuration {
    public static ConfigurationYAML yamlConfig;

    public static double NPC_STORAGE_COSTS_FIXED = 5;
    public static double NPC_STORAGE_COSTS_FACTOR = 1;

    public static boolean CHECK = true;
    public static boolean DOWNLOAD = false;
    public static boolean REPLACE_OLD = false;

    public static void setDefault() {
        FileConfiguration config = yamlConfig.getConfig();

        config.addDefault("Trait.Storage.Costs.Fixed", 5.0);
        config.addDefault("Trait.Storage.Costs.Factor", 5.0);
        config.addDefault("Update.Check", CHECK);
        config.addDefault("Update.Download", DOWNLOAD);
        config.addDefault("Update.ReplaceOld", REPLACE_OLD);

        config.options().copyDefaults(true);
        yamlConfig.saveConfig();
    }

    public static void loadConfiguration() {
        FileConfiguration config = yamlConfig.getConfig();

        NPC_STORAGE_COSTS_FIXED = config.getDouble("Trait.Storage.Costs.Fixed", 5.0);
        NPC_STORAGE_COSTS_FACTOR = config.getDouble("Trait.Storage.Costs.Factor", 1.0);
        CHECK = config.getBoolean("Update.Check", CHECK);
        DOWNLOAD = config.getBoolean("Update.Download", DOWNLOAD);
        REPLACE_OLD = config.getBoolean("Update.ReplaceOld", REPLACE_OLD);
    }
}