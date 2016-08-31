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

package de.keyle.mypet.npc;

import de.Keyle.MyPet.MyPetApi;
import de.Keyle.MyPet.api.MyPetVersion;
import de.Keyle.MyPet.api.util.configuration.ConfigurationYAML;
import de.Keyle.MyPet.util.Metrics;
import de.Keyle.MyPet.util.logger.MyPetLogger;
import de.keyle.mypet.npc.commands.CommandConfig;
import de.keyle.mypet.npc.traits.ShopTrait;
import de.keyle.mypet.npc.traits.StorageTrait;
import de.keyle.mypet.npc.traits.WalletTrait;
import de.keyle.mypet.npc.traits.dummy.DummyShopTrait;
import de.keyle.mypet.npc.traits.dummy.DummyStorageTrait;
import de.keyle.mypet.npc.traits.dummy.DummyWalletTrait;
import de.keyle.mypet.npc.util.Configuration;
import de.keyle.mypet.npc.util.MyPetNpcVersion;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.TraitInfo;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

public class MyPetNpcPlugin extends JavaPlugin {
    private static MyPetNpcPlugin plugin;

    public void onDisable() {
        if (getLogger() instanceof MyPetLogger) {
            ((MyPetLogger) getLogger()).disableDebugLogger();
        }
    }

    public void onEnable() {
        plugin = this;

        MyPetNpcVersion.reset();

        if (!Bukkit.getPluginManager().isPluginEnabled("MyPet")) {
            getLogger().info("MyPet is not installed/enabled. Activating dummy traits!");

            CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(DummyStorageTrait.class).withName("mypet-storage"));
            CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(DummyWalletTrait.class).withName("mypet-wallet"));
            CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(DummyShopTrait.class).withName("mypet-shop"));

            return;
        }

        replaceLogger();

        if (Integer.parseInt(MyPetVersion.getBuild()) < Integer.parseInt(MyPetNpcVersion.getRequiredMyPetBuild())) {
            boolean premium = false;
            try {
                premium = MyPetVersion.isPremium();
            } catch (NoSuchMethodError ignored) {
            }

            if (premium) {
                if (Integer.parseInt(MyPetVersion.getBuild()) < Integer.parseInt(MyPetNpcVersion.getRequiredMyPetPremiumBuild())) {
                    getLogger().warning(ChatColor.RED + "This version of MyPet-NPC requires MyPet-Premium build-#" + MyPetNpcVersion.getRequiredMyPetPremiumBuild() + " or higher");
                    this.setEnabled(false);
                    return;
                }
            } else {
                getLogger().warning(ChatColor.RED + "This version of MyPet-NPC requires MyPet build-#" + MyPetNpcVersion.getRequiredMyPetBuild() + " or higher");
                this.setEnabled(false);
                return;
            }
        }

        try {
            Metrics metrics = new Metrics(this);
            if (!metrics.isOptOut()) {
                metrics.start();
            }
        } catch (IOException ignored) {
        }

        File configFile = new File(MyPetApi.getPlugin().getDataFolder().getPath() + File.separator + "plugins" + File.separator + "NPC" + File.separator + "config.yml");
        configFile.getParentFile().mkdirs();
        Configuration.yamlConfig = new ConfigurationYAML(configFile);

        Configuration.setDefault();
        Configuration.loadConfiguration();

        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(StorageTrait.class).withName("mypet-storage"));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(WalletTrait.class).withName("mypet-wallet"));
        if (MyPetVersion.isPremium()) {
            CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(ShopTrait.class).withName("mypet-shop"));
        }

        getCommand("mypetnpcconfig").setExecutor(new CommandConfig());

        getLogger().info("version " + MyPetNpcVersion.getVersion() + "-b" + MyPetNpcVersion.getBuild() + ChatColor.GREEN + " ENABLED");
    }

    public static MyPetNpcPlugin getPlugin() {
        return plugin;
    }

    private void replaceLogger() {
        try {
            Field logger = JavaPlugin.class.getDeclaredField("logger");
            logger.setAccessible(true);
            logger.set(this, new MyPetLogger(this));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}