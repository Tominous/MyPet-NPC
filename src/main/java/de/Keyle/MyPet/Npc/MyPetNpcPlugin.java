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

import de.Keyle.MyPet.Npc.npc.MyPetNpc;
import de.Keyle.MyPet.Npc.util.MyPetNpcVersion;
import de.Keyle.MyPet.util.MyPetVersion;
import de.Keyle.MyPet.util.logger.DebugLogger;
import de.Keyle.MyPet.util.logger.MyPetLogger;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.TraitInfo;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.MetricsLite;

import java.io.IOException;

public class MyPetNpcPlugin extends JavaPlugin
{
    private static MyPetNpcPlugin plugin;

    public void onEnable()
    {
        plugin = this;

        DebugLogger.info("----------- loading MyPet-NPC ... -----------", "MyPet-NPC");

        if (Integer.parseInt(MyPetVersion.getMyPetBuild()) < Integer.parseInt(MyPetNpcVersion.getRequiredMyPetBuild()))
        {
            MyPetLogger.write(ChatColor.RED + "This version of MyPet-NPC requires MyPet build-#" + MyPetNpcVersion.getRequiredMyPetBuild() + " or higher", "MyPet-NPC");
            this.setEnabled(false);
            return;
        }

        try
        {
            MetricsLite metrics = new MetricsLite(this);
            metrics.start();
            DebugLogger.info("MetricsLite activated", "MyPet-NPC");
        }
        catch (IOException e)
        {
            DebugLogger.info("MetricsLite not activated", "MyPet-NPC");
            DebugLogger.info(e.getMessage(), "MyPet-NPC");
        }

        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(MyPetNpc.class).withName("mypetnpc"));

        MyPetLogger.write("version " + MyPetNpcVersion.getMyPetNpcVersion() + "-b" + MyPetNpcVersion.getMyPetNpcBuild() + ChatColor.GREEN + " ENABLED", "MyPet-NPC");
        DebugLogger.info("----------- MyPet-NPC ready -----------", "MyPet-NPC");
    }

    public static MyPetNpcPlugin getPlugin()
    {
        return plugin;
    }
}
