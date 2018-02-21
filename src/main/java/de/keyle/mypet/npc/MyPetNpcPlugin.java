/*
 * This file is part of MyPet
 *
 * Copyright © 2011-2018 Keyle
 * MyPet is licensed under the GNU Lesser General Public License.
 *
 * MyPet is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyPet is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

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
import de.Keyle.MyPet.commands.CommandAdmin;
import de.Keyle.MyPet.util.logger.MyPetLogger;
import de.keyle.mypet.npc.commands.CommandNpcConfig;
import de.keyle.mypet.npc.traits.ShopTrait;
import de.keyle.mypet.npc.traits.StorageTrait;
import de.keyle.mypet.npc.traits.WalletTrait;
import de.keyle.mypet.npc.traits.dummy.DummyShopTrait;
import de.keyle.mypet.npc.traits.dummy.DummyStorageTrait;
import de.keyle.mypet.npc.traits.dummy.DummyWalletTrait;
import de.keyle.mypet.npc.util.Configuration;
import de.keyle.mypet.npc.util.MyPetNpcVersion;
import de.keyle.mypet.npc.util.Updater;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.TraitInfo;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Field;
import java.util.logging.Logger;

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

        File configFile = new File(MyPetApi.getPlugin().getDataFolder().getPath() + File.separator + "plugins" + File.separator + "NPC" + File.separator + "config.yml");
        configFile.getParentFile().mkdirs();
        Configuration.yamlConfig = new ConfigurationYAML(configFile);
        Configuration.setDefault();
        Configuration.loadConfiguration();

        Updater updater = new Updater("MyPet-NPC");
        updater.update();

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

        Metrics metrics = new Metrics(this);
        metrics.addCustomChart(new Metrics.SimplePie("build", MyPetNpcVersion::getBuild));

        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(StorageTrait.class).withName("mypet-storage"));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(WalletTrait.class).withName("mypet-wallet"));
        if (MyPetVersion.isPremium()) {
            CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(ShopTrait.class).withName("mypet-shop"));
        } else {
            CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(DummyShopTrait.class).withName("mypet-shop"));
        }

        CommandAdmin.COMMAND_OPTIONS.put("npc", new CommandNpcConfig());

        updater.waitForDownload();

        getLogger().info("version " + MyPetNpcVersion.getVersion() + "-b" + MyPetNpcVersion.getBuild() + ChatColor.GREEN + " ENABLED");
    }

    public static MyPetNpcPlugin getPlugin() {
        return plugin;
    }

    public static Logger getPluginLogger() {
        return plugin.getLogger();
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

    public File getFile() {
        return super.getFile();
    }
}