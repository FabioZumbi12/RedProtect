package br.net.fabiozumbi12.RedProtect.Bukkit.updater;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Core.helpers.LogLevel;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.inventivetalent.update.spiget.SpigetUpdate;
import org.inventivetalent.update.spiget.UpdateCallback;
import org.inventivetalent.update.spiget.comparator.VersionComparator;

import java.io.File;

public class SpigetUpdater {

    private RedProtect plugin;

    public SpigetUpdater(RedProtect plugin) {
        this.plugin = plugin;
        if (!new File(plugin.getDataFolder(), "backup").exists())
            new File(plugin.getDataFolder(), "backup").mkdir();
    }

    private static SpigetUpdate spigetUpdate = null;
    private UpdateStatus updateAvailable = UpdateStatus.UNKNOWN;
    private String currentJarFile = "";
    private String newDownloadVersion = "";

    public SpigetUpdate getSpigetUpdate() {
        return spigetUpdate;
    }

    public UpdateStatus getUpdateAvailable() {
        return updateAvailable;
    }

    public void setUpdateAvailable(UpdateStatus b) {
        updateAvailable = b;
    }

    public String getCurrentJarFile() {
        return currentJarFile;
    }

    public void setCurrentJarFile(String name) {
        currentJarFile = name;
    }

    public String getNewDownloadVersion() {
        return newDownloadVersion;
    }

    public void setNewDownloadVersion(String newDownloadVersion) {
        this.newDownloadVersion = newDownloadVersion;
    }

    private int taskId;
    public int getTaskId() {
        return this.taskId;
    }

    public void hourlyUpdateCheck(final CommandSender sender, boolean updateCheck, final boolean silent) {
        long minutes = plugin.config.configRoot().update.check_interval;
        if (minutes < 15) {
            plugin.logger.warning("[Warning] check-interval in your config.yml is too low. A low number can cause server crashes. The number is raised to 15 minutes.");
            minutes = 15;
        }
        if (updateCheck) {
            taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, ()-> checkForUpdate(sender, silent), 40L, (minutes * 60) * 20);
        }
    }

    /**
     * Download a new version, add version number to the downloaded filename
     * (filename-n.n.n) , and the rename the old version to ???.jar.oldnnn
     *
     * @param sender
     * @return
     */
    public boolean downloadAndUpdateJar(CommandSender sender) {
        final String OS = System.getProperty("os.name");
        boolean success = spigetUpdate.downloadUpdate();

        new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {
                if (count++ > 20) {
                    Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "[RedProtect]" + ChatColor.RED
                            + " No updates found. (No response from server after 20s)");
                    plugin.logger.warning("The plugin could not be updated. You must do it manually." + spigetUpdate.getFailReason().toString());
                    plugin.logger.debug(LogLevel.DEFAULT, "Update error: " + spigetUpdate.getFailReason().toString());
                    this.cancel();
                } else {
                    // Wait for the response
                    if (success) {
                        if (OS.contains("Win")) {
                            File downloadedJar = new File("plugins/update/" + currentJarFile);
                            File newJar = new File("plugins/update/RedProtect-" + newDownloadVersion + ".jar");
                            if (newJar.exists())
                                newJar.delete();
                            downloadedJar.renameTo(newJar);
                            updateAvailable = UpdateStatus.RESTART_NEEDED;
                            plugin.logger.success("Plugin updated. Restart server to complete the update.");
                        } else {
                            if (updateAvailable != UpdateStatus.RESTART_NEEDED) {
                                File currentJar = new File("plugins/" + currentJarFile);
                                File disabledJar = new File("plugins/" + currentJarFile + ".old");
                                int count = 0;
                                while (disabledJar.exists() && count++ < 100) {
                                    disabledJar = new File("plugins/" + currentJarFile + ".old" + count);
                                }
                                if (!disabledJar.exists()) {
                                    currentJar.renameTo(disabledJar);
                                    File downloadedJar = new File("plugins/update/" + currentJarFile);
                                    File newJar = new File("plugins/RedProtect-" + newDownloadVersion + ".jar");
                                    downloadedJar.renameTo(newJar);
                                    plugin.logger.debug(LogLevel.DEFAULT, "Moved plugins/update/" + currentJarFile
                                            + " to plugins/RedProtect-" + newDownloadVersion + ".jar");
                                    updateAvailable = UpdateStatus.RESTART_NEEDED;
                                    plugin.logger.success("Plugin updated. Restart server to complete the update.");
                                }
                            }
                        }
                        this.cancel();
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
        return true;
    }

    /**
     * Check is a new version is available
     *
     * @param sender
     * @param silent
     *            - if true the player will not get the status in Game
     */
    public void checkForUpdate(final CommandSender sender, final boolean silent) {
        if (!silent)
            sender.sendMessage("[RedProtect] Checking for updates ...");
        if (updateAvailable != UpdateStatus.RESTART_NEEDED) {
            spigetUpdate = new SpigetUpdate(plugin, 15841);
            spigetUpdate.setVersionComparator(VersionComparator.EQUAL);
            spigetUpdate.setUserAgent("RedProtect-" + plugin.getDescription().getVersion());

            spigetUpdate.checkForUpdate(new UpdateCallback() {

                @Override
                public void updateAvailable(String newVersion, String downloadUrl, boolean hasDirectDownload) {
                    //// VersionComparator.EQUAL handles all updates as new, so I have to check the
                    //// version number manually
                    updateAvailable = isUpdateNewerVersion(newVersion);
                    if (updateAvailable == UpdateStatus.AVAILABLE) {
                        newDownloadVersion = newVersion;
                        sender.sendMessage(ChatColor.GOLD + "[RedProtect] New update found: " + ChatColor.GREEN + newVersion);
                        if (plugin.config.configRoot().update.auto_update) {
                            downloadAndUpdateJar(sender);
                            sender.sendMessage(ChatColor.GOLD + "[RedProtect] " + ChatColor.GREEN + "Plugin updated. Restart server to complete the update.");
                        } else
                            sender.sendMessage(ChatColor.GOLD + "[RedProtect] " + ChatColor.GREEN + "Please use '/rp update' to update to " + newDownloadVersion);
                    }
                }

                @Override
                public void upToDate() {
                    //// Plugin is up-to-date
                    if (!silent)
                        sender.sendMessage("[RedProtect] " + ChatColor.RESET + "No update available.");
                }
            });
        }
    }

    /**
     * Check if "newVersion" is newer than plugin's current version
     *
     * @param newVersion
     * @return
     */
    public UpdateStatus isUpdateNewerVersion(String newVersion) {
        // Version format on Spigot.org & Spiget.org: "n.n.n"
        // Version format in jar file: "n.n.n" | "n.n.n-SNAPSHOT-Bn"

        int updateCheck = 0, pluginCheck = 0;
        String[] updateVer = newVersion.split("\\.");

        // Check the version #'s
        String[] pluginVer = plugin.getDescription().getVersion().split("\\.");
        // Run through major, minor, sub - version numbers
        for (int i = 0; i < Math.max(updateVer.length, pluginVer.length); i++) {
            try {
                updateCheck = 0;
                if (i < updateVer.length)
                    updateCheck = Integer.valueOf(updateVer[i]);
                pluginCheck = 0;
                if (i < pluginVer.length)
                    pluginCheck = Integer.valueOf(pluginVer[i]);
                if (updateCheck > pluginCheck) {
                    return UpdateStatus.AVAILABLE;
                } else if (updateCheck < pluginCheck)
                    return UpdateStatus.NOT_AVAILABLE;
            } catch (Exception e) {
                plugin.getLogger().warning("Could not determine update's version # ");
                plugin.getLogger().warning("Installed plugin version: " + plugin.getDescription().getVersion());
                plugin.getLogger().warning("Newest version on Spiget.org: " + newVersion);
                return UpdateStatus.UNKNOWN;
            }
        }
        if (updateCheck == pluginCheck)
            return UpdateStatus.AVAILABLE;
        else
            return UpdateStatus.NOT_AVAILABLE;
    }

    public enum UpdateStatus {
        UNKNOWN, NO_RESPONSE, NOT_AVAILABLE, AVAILABLE, RESTART_NEEDED, FORCED_DOWNLOAD
    }
}