/*
 * Copyright (c) 2020 - @FabioZumbi12
 * Last Modified: 02/07/2020 19:25.
 *
 * This class is provided 'as-is', without any express or implied warranty. In no event will the authors be held liable for any
 *  damages arising from the use of this class.
 *
 * Permission is granted to anyone to use this class for any purpose, including commercial plugins, and to alter it and
 * redistribute it freely, subject to the following restrictions:
 * 1 - The origin of this class must not be misrepresented; you must not claim that you wrote the original software. If you
 * use this class in other plugins, an acknowledgment in the plugin documentation would be appreciated but is not required.
 * 2 - Altered source versions must be plainly marked as such, and must not be misrepresented as being the original class.
 * 3 - This notice may not be removed or altered from any source distribution.
 *
 * Esta classe é fornecida "como está", sem qualquer garantia expressa ou implícita. Em nenhum caso os autores serão
 * responsabilizados por quaisquer danos decorrentes do uso desta classe.
 *
 * É concedida permissão a qualquer pessoa para usar esta classe para qualquer finalidade, incluindo plugins pagos, e para
 * alterá-lo e redistribuí-lo livremente, sujeito às seguintes restrições:
 * 1 - A origem desta classe não deve ser deturpada; você não deve afirmar que escreveu a classe original. Se você usar esta
 *  classe em um plugin, uma confirmação de autoria na documentação do plugin será apreciada, mas não é necessária.
 * 2 - Versões de origem alteradas devem ser claramente marcadas como tal e não devem ser deturpadas como sendo a
 * classe original.
 * 3 - Este aviso não pode ser removido ou alterado de qualquer distribuição de origem.
 */

package br.net.fabiozumbi12.redbackups;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public final class RedBackups extends JavaPlugin implements Listener, CommandExecutor, TabCompleter {

    private RedBackups plugin;

    @Override
    public void onDisable() {
        if (taskAfterStart != null) {
            taskAfterStart.cancel();
            taskAfterStart = null;
        }
        if (taskInterval != null) {
            taskInterval.cancel();
            taskInterval = null;
        }
    }

    @Override
    public void onEnable() {
        this.plugin = this;

        getServer().getPluginManager().registerEvents(this, this);
        getCommand("redbackups").setExecutor(this);

        getConfig().addDefault("backup.enabled", false);
        getConfig().addDefault("backup.worlds", Bukkit.getServer().getWorlds().stream().map(World::getName).collect(Collectors.toList()));
        getConfig().addDefault("backup.mode", "server-start");
        getConfig().addDefault("backup.modes.server-start.delay-after-start", 10);
        getConfig().addDefault("backup.modes.interval.minutes", 120);
        getConfig().addDefault("backup.modes.timed.time", "03:00");

        getConfig().options().header("" +
                "---- RedBackups Configuration ----\n" +
                "Description: This plugin makes backups fo redprotect player regions\n" +
                "Configurations:\n" +
                "- backup-mode = Backup modes to use.\n" +
                "- - Values: \n" +
                "- - server-start = Backup on every server start with delay (in minutes).\n" +
                "- - interval = Backup on every interval in minutes (start counting on server start/not persistent on server reboot).\n" +
                "- - timed = Backup on exact time and minute.\n"
        );
        getConfig().options().copyDefaults(true);
        saveConfig();

        // Init backup task
        startBackupScheduler();
    }

    private BukkitTask taskAfterStart;
    private BukkitTask taskInterval;

    private void startBackupScheduler() {
        if (!getConfig().getBoolean("backup.enabled", false)) return;

        String mode = getConfig().getString("backup.mode", "server-start");

        if (mode.equals("server-start") && taskAfterStart == null) {
            taskAfterStart = Bukkit.getScheduler().runTaskLaterAsynchronously(this.plugin,  () -> createBackup(Bukkit.getConsoleSender(), null), getConfig().getInt("backup.modes.server-start.delay-after-start") * 60 * 20);
        }

        if (mode.equals("interval")) {
            int delay = getConfig().getInt("backup.modes.interval.minutes") * 60 * 20;
            taskInterval = Bukkit.getScheduler().runTaskTimerAsynchronously(this.plugin, () -> createBackup(Bukkit.getConsoleSender(), null), delay, delay);
        }

        if (mode.equals("timed")) {
            String timed = getConfig().getString("backup.modes.timed.time");
            final String[] lastBackup = {""};

            taskInterval = Bukkit.getScheduler().runTaskTimerAsynchronously(this.plugin, () -> {
                String timeStamp = new SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime());
                if (timeStamp.equals(timed) && !lastBackup[0].equals(timeStamp)) {
                    lastBackup[0] = timeStamp;
                    createBackup(Bukkit.getConsoleSender(), null);
                }
            }, 600, 600);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args){
        List<String> tab = new ArrayList<>();
        if (args.length == 0) {
            if (sender.hasPermission("redbackups.cmd.reload")) {
                tab.add("reload");
            }
            if (sender.hasPermission("redbackups.cmd.backup")) {
                tab.add("backup");
            }
        }
        if (args.length == 1) {
            if ("reload".startsWith(args[0]) && sender.hasPermission("redbackups.cmd.reload")) {
                tab.add("reload");
            }
            if ("backup".startsWith(args[0]) && sender.hasPermission("redbackups.cmd.backup")) {
                tab.add("backup");
            }
        }
        return tab;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            if (args[0].equals("reload") && sender.hasPermission("redbackups.cmd.reload")) {
                if (taskAfterStart != null && !getConfig().getString("backup.mode", "server-start").equals("server-start")) {
                    taskAfterStart.cancel();
                    taskAfterStart = null;
                }

                if (taskInterval != null) {
                    taskInterval.cancel();
                    taskInterval = null;
                }

                reloadConfig();
                startBackupScheduler();

                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&4Red&cBackups&7] &aRedBackups reloaded with success!"));
                return true;
            }

            if (args[0].equals("backup") && sender.hasPermission("redbackups.cmd.backup")) {
                createBackup(sender,null);
                return true;
            }
        }

        if (args.length == 2 && args[0].equals("backup") && args[1].equals("here") && sender.hasPermission("redbackups.cmd.backup")) {
            if (sender instanceof Player) {
                createBackup(sender, ((Player)sender).getLocation());
            } else {
                sender.sendMessage(ChatColor.RED + "Only players can use this command with argument 'here'!");
            }
            return true;
        }

        return false;
    }

    private final List<String> backupList = new ArrayList<>();
    private void createBackup(CommandSender sender, Location location) {
        List<String> worlds = getConfig().getStringList("backup.worlds");

        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&4Red&cBackups&7] &2Backup started..."));
            String mainWorld = Bukkit.getWorlds().get(0).getName();
            // Clear last backups
            backupList.clear();

            if (location != null) {
                if (!worlds.contains(location.getWorld().getName())) return;

                String file = location.getWorld().getName() + File.separator + "region" + File.separator + "r." + (location.getBlockX() >> 4 >> 5) + "." + (location.getBlockZ() >> 4 >> 5) + ".mca";
                backupList.add(file);
            } else {
                Set<Region> regionSet = RedProtect.get().getAPI().getAllRegions();

                for (Region region:regionSet.stream().filter(r -> worlds.contains(r.getWorld())).collect(Collectors.toList())) {
                    for (int x = region.getMinMbrX(); x <= region.getMaxMbrX(); x++) {
                        for (int z = region.getMinMbrZ(); z <= region.getMaxMbrZ(); z++) {

                            String file = mainWorld + File.separator + "region" + File.separator + "r." + (x >> 4 >> 5) + "." + (z >> 4 >> 5) + ".mca";
                            if (!backupList.contains(file)) {
                                backupList.add(file);
                            }

                            file = mainWorld + File.separator + region.getWorld() + File.separator + "region" + File.separator + "r." + (x >> 4 >> 5) + "." + (z >> 4 >> 5) + ".mca";
                            if (!backupList.contains(file)) {
                                backupList.add(file);
                            }
                        }
                    }
                }
            }

            // Start backup files
            backupList.forEach(file -> {
                try {

                    File fileFromCopy = new File(getServer().getWorldContainer().getAbsolutePath() + "\\.." , file);
                    Bukkit.getLogger().severe("file1: " + fileFromCopy);
                    if (!fileFromCopy.exists() || fileFromCopy.length() == 0) return;

                    if (!new File(getDataFolder(), "backups").exists()) {
                        new File(getDataFolder(), "backups").mkdir();
                    }

                    File fileToCopy = new File(getDataFolder(), "backups" + File.separator + file);

                    // Create child directories
                    fileToCopy.getParentFile().mkdirs();

                    Files.copy(fileFromCopy.toPath(), fileToCopy.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            // Clear backups
            backupList.clear();
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&4Red&cBackups&7] &2Backup finished with success!"));
        });
    }
}
