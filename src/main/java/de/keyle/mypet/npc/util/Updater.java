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

package de.keyle.mypet.npc.util;

import de.Keyle.MyPet.MyPetApi;
import de.Keyle.MyPet.api.Util;
import de.keyle.mypet.npc.MyPetNpcPlugin;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Optional;

public class Updater {
    public class Update {
        String version;
        int build;

        public Update(String version, int build) {
            this.version = version;
            this.build = build;
        }

        public String getVersion() {
            return version;
        }

        public int getBuild() {
            return build;
        }

        @Override
        public String toString() {
            return version + " #" + build;
        }
    }

    protected static Updater.Update latest = null;
    protected String plugin;
    protected Thread thread;

    public Updater(String plugin) {
        this.plugin = plugin;
        latest = null;
    }

    public void update() {
        if (Configuration.CHECK) {
            Optional<Update> update = check();
            if (update.isPresent()) {
                latest = update.get();

                notifyVersion();

                if (Configuration.DOWNLOAD) {
                    download();
                }
            }
        }
    }

    protected Optional<Updater.Update> check() {
        try {
            String parameter = "";
            parameter += "&package=" + MyPetApi.getCompatUtil().getInternalVersion();
            parameter += "&build=" + MyPetNpcVersion.getBuild();
            parameter += "&dev=" + MyPetNpcVersion.isDevBuild();

            String url = "http://update.mypet-plugin.de/" + plugin + "?" + parameter;

            // no data will be saved on the server
            String content = Util.readUrlContent(url);
            JSONParser parser = new JSONParser();
            JSONObject result = (JSONObject) parser.parse(content);

            if (result.containsKey("latest")) {
                String version = result.get("latest").toString();
                int build = ((Long) result.get("build")).intValue();
                return Optional.of(new Updater.Update(version, build));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    private void notifyVersion() {
        String m = "#  A new ";
        m += MyPetNpcVersion.isDevBuild() ? "build" : "version";
        m += " is available: " + latest + " #";
        MyPetNpcPlugin.getPluginLogger().info(StringUtils.repeat("#", m.length()));
        MyPetNpcPlugin.getPluginLogger().info(m);
        MyPetNpcPlugin.getPluginLogger().info("#  https://mypet-plugin.de/download" + StringUtils.repeat(" ", m.length() - 35) + "#");
        MyPetNpcPlugin.getPluginLogger().info(StringUtils.repeat("#", m.length()));
    }

    public void download() {
        String url = "https://mypet-plugin.de/download/" + plugin + "/";
        if (MyPetNpcVersion.isDevBuild()) {
            url += "dev";
        } else {
            url += "release";
        }
        File pluginFile;
        if (Configuration.REPLACE_OLD) {
            pluginFile = new File(MyPetNpcPlugin.getPlugin().getFile().getParentFile().getAbsolutePath(), "update/" + MyPetNpcPlugin.getPlugin().getFile().getName());
        } else {
            pluginFile = new File(MyPetNpcPlugin.getPlugin().getFile().getParentFile().getAbsolutePath(), "update/" + plugin + "-" + latest.getVersion() + ".jar");
        }

        String finalUrl = url;
        thread = new Thread(() -> {
            try {
                MyPetNpcPlugin.getPluginLogger().info(ChatColor.RED + "Start update download: " + ChatColor.RESET + latest);
                URL website = new URL(finalUrl);
                ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                FileOutputStream fos = new FileOutputStream(pluginFile);
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                fos.close();
                rbc.close();
                String message = "Finished update download.";
                if (Configuration.REPLACE_OLD || MyPetNpcPlugin.getPlugin().getFile().getName().equals("MyPet-" + latest.getVersion() + ".jar")) {
                    message += " The update will be loaded on the next server start.";
                } else {
                    message += " The file was stored in the \"update\" folder.";
                }
                MyPetNpcPlugin.getPluginLogger().info(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    public void waitForDownload() {
        if (thread != null && thread.isAlive()) {
            MyPetNpcPlugin.getPluginLogger().info("Wait for the update download to finish...");
            try {
                thread.join();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static Updater.Update getLatest() {
        return latest;
    }

    public static boolean isUpdateAvailable() {
        return latest != null;
    }
}