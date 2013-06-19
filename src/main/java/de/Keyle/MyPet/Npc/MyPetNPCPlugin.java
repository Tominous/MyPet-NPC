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

package de.Keyle.MyPet.Npc;

import de.Keyle.MyPet.Npc.util.MyPetNpcVersion;
import de.Keyle.MyPet.util.logger.DebugLogger;
import de.Keyle.MyPet.util.logger.MyPetLogger;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.MetricsLite;

import java.io.IOException;

public class MyPetNPCPlugin extends JavaPlugin
{
    private static MyPetNPCPlugin plugin;

    public void onDisable()
    {
    }

    public void onEnable()
    {
        plugin = this;

        DebugLogger.info("----------- loading MyPet-NPC ... -----------", "MyPet-Npc");

        try
        {
            MetricsLite metrics = new MetricsLite(this);
            metrics.start();
            DebugLogger.info("MetricsLite activated", "MyPet-Npc");
        }
        catch (IOException e)
        {
            DebugLogger.info("MetricsLite not activated", "MyPet-Npc");
            DebugLogger.info(e.getMessage(), "MyPet-Npc");
        }

        MyPetLogger.write("version " + MyPetNpcVersion.getMyPetNpcVersion() + "-b" + MyPetNpcVersion.getMyPetNpcBuild() + ChatColor.GREEN + " ENABLED", "MyPet-Npc");
        DebugLogger.info("----------- MyPet-Npc ready -----------", "MyPet-Npc");
    }

    public static MyPetNPCPlugin getPlugin()
    {
        return plugin;
    }
}
