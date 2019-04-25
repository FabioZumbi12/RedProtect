/*
 *  Copyright (c) 2019 - @FabioZumbi12
 *  Last Modified: 16/04/19 06:21
 *
 *  This class is provided 'as-is', without any express or implied warranty. In no event will the authors be held liable for any
 *   damages arising from the use of this class.
 *
 *  Permission is granted to anyone to use this class for any purpose, including commercial plugins, and to alter it and
 *  redistribute it freely, subject to the following restrictions:
 *  1 - The origin of this class must not be misrepresented; you must not claim that you wrote the original software. If you
 *  use this class in other plugins, an acknowledgment in the plugin documentation would be appreciated but is not required.
 *  2 - Altered source versions must be plainly marked as such, and must not be misrepresented as being the original class.
 *  3 - This notice may not be removed or altered from any source distribution.
 *
 *  Esta classe é fornecida "como está", sem qualquer garantia expressa ou implícita. Em nenhum caso os autores serão
 *  responsabilizados por quaisquer danos decorrentes do uso desta classe.
 *
 *  É concedida permissão a qualquer pessoa para usar esta classe para qualquer finalidade, incluindo plugins pagos, e para
 *  alterá-lo e redistribuí-lo livremente, sujeito às seguintes restrições:
 *  1 - A origem desta classe não deve ser deturpada; você não deve afirmar que escreveu a classe original. Se você usar esta
 *   classe em um plugin, uma confirmação de autoria na documentação do plugin será apreciada, mas não é necessária.
 *  2 - Versões de origem alteradas devem ser claramente marcadas como tal e não devem ser deturpadas como sendo a
 *  classe original.
 *  3 - Este aviso não pode ser removido ou alterado de qualquer distribuição de origem.
 */

package br.net.fabiozumbi12.RedProtect.Bukkit;

import br.net.fabiozumbi12.RedProtect.Bukkit.API.RedProtectAPI;
import br.net.fabiozumbi12.RedProtect.Bukkit.commands.CommandHandler;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.ConfigLoader;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.LangManager;
import br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPLogger;
import br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPPermissionHandler;
import br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPUtil;
import br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPVHelper;
import br.net.fabiozumbi12.RedProtect.Bukkit.hooks.HooksManager;
import br.net.fabiozumbi12.RedProtect.Bukkit.listeners.*;
import br.net.fabiozumbi12.RedProtect.Bukkit.region.RegionManager;
import br.net.fabiozumbi12.RedProtect.Core.helpers.LogLevel;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class RedProtect extends JavaPlugin {
    private static RedProtect plugin;
    private RedProtectAPI redProtectAPI;
    private int autoSaveID;
    private RPVHelper rpvHelper;

    public final List<String> confirmStart = new ArrayList<>();
    public final HashMap<String, List<String>> denyEnter = new HashMap<>();
    public Economy economy;
    public HashMap<Player, Location> firstLocationSelections = new HashMap<>();
    public HashMap<Player, Location> secondLocationSelections = new HashMap<>();
    public int bukkitVersion;
    public HashMap<Player, String> alWait = new HashMap<>();
    public List<String> changeWait = new ArrayList<>();
    public List<String> tpWait = new ArrayList<>();
    public final HooksManager hooks = new HooksManager();
    public final RPLogger logger = new RPLogger();
    public RegionManager rm;
    public RPPermissionHandler ph;
    public ConfigLoader config;
    public LangManager lang;

    public CommandHandler cmdHandler;

    public static RedProtect get() {
        return plugin;
    }

    public RPVHelper getPVHelper() {
        return rpvHelper;
    }

    public RedProtectAPI getAPI() {
        return redProtectAPI;
    }

    public void onDisable() {
        shutDown();
    }

    public void onEnable() {
        try {
            plugin = this;

            ph = new RPPermissionHandler();
            rm = new RegionManager();

            //Init config, lang, listeners and flags
            startLoad();

            bukkitVersion = getBukkitVersion();
            logger.debug(LogLevel.DEFAULT, "Version String: " + bukkitVersion);

            if (bukkitVersion >= 180) {
                getServer().getPluginManager().registerEvents(new RPMine18(), this);
            }
            if (bukkitVersion >= 190) {
                getServer().getPluginManager().registerEvents(new RPMine19(), this);
            }

            if (bukkitVersion <= 1122) {
                rpvHelper = (RPVHelper) Class.forName("br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPVHelper112").newInstance();
            }
            if (bukkitVersion >= 1130) {
                rpvHelper = (RPVHelper) Class.forName("br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPVHelper113").newInstance();
            }

            logger.info("Loading API...");
            this.redProtectAPI = new RedProtectAPI();
            logger.info("API Loaded!");

            logger.clear("&4 _   _  _  &c _   _   _  _ _  _  _ _ _  __");
            logger.clear("&4|_| |_ | \\ &c|_| |_| | |  |  |_ |   |    /");
            logger.clear("&4| \\ |_ |_/ &c|   | \\ |_|  |  |_ |_  |   /");
            logger.clear("&a» " + getDescription().getFullName() + " enabled");
            logger.clear("");

        } catch (Exception e) {
            e.printStackTrace();
            if (!config.configRoot().file_type.equalsIgnoreCase("mysql")) {
                logger.severe("Error enabling RedProtect, plugin will shut down.");
                this.setEnabled(false);
            }
            getServer().setWhitelist(true);
            getServer().getOnlinePlayers().forEach(p -> p.kickPlayer("The server has been whitelisted due to an error while loading plugins!"));
            logger.severe("Due to an error in RedProtect loading, the whitelist has been turned on and every player has been kicked.");
            logger.severe("DO NOT LET ANYONE ENTER before fixing the problem, otherwise you risk losing protected regions.");
        }
    }

    public void reload() {
        try {
            //shutdown
            shutDown();

            //start
            startLoad();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startLoad() throws Exception {
        config = new ConfigLoader();
        lang = new LangManager();

        if (config.configRoot().purge.regen.enable_whitelist_regen && Bukkit.getServer().hasWhitelist()) {
            Bukkit.getServer().setWhitelist(false);
            RedProtect.get().logger.success("Whitelist disabled!");
        }

        logger.info("Registering commands...");
        cmdHandler = new CommandHandler(this);

        logger.info("Registering listeners...");
        getServer().getPluginManager().registerEvents(new RPGlobalListener(), this);
        getServer().getPluginManager().registerEvents(new RPBlockListener(), this);
        getServer().getPluginManager().registerEvents(new RPPlayerListener(), this);
        getServer().getPluginManager().registerEvents(new RPEntityListener(), this);
        getServer().getPluginManager().registerEvents(new RPWorldListener(), this);

        try {
            rm = new RegionManager();
            rm.loadAll();

            RPUtil.ReadAllDB(rm.getAllRegions());

            if (!config.configRoot().file_type.equalsIgnoreCase("mysql")) {
                startAutoSave();
            }
            logger.info("There are " + rm.getTotalRegionsNum() + " regions on (" + config.configRoot().file_type + ") database!");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Register hooks
        hooks.registerHooks();
    }

    private void shutDown() {
        // Unregister commands
        cmdHandler.unregisterAll();

        // Save and unload all regions
        rm.saveAll(true);
        rm.unloadAll();

        // Cancel tasks from bukkit scheduler and save logs
        Bukkit.getScheduler().cancelTasks(this);
        logger.saveLogs();

        // Unregister listeners
        logger.info("Unregistering listeners...");
        HandlerList.unregisterAll(this);

        logger.info(getDescription().getFullName() + " turned off...");
    }

    public boolean denyEnterRegion(String rid, String player) {
        if (denyEnter.containsKey(player)) {
            if (denyEnter.get(player).contains(rid)) {
                return false;
            }
            List<String> regs = denyEnter.get(player);
            regs.add(rid);
            denyEnter.put(player, regs);
        } else {
            denyEnter.put(player, new LinkedList<>(Collections.singletonList(rid)));
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (denyEnter.containsKey(player)) {
                List<String> regs = denyEnter.get(player);
                regs.remove(rid);
                if (regs.isEmpty()) {
                    denyEnter.remove(player);
                } else {
                    denyEnter.put(player, regs);
                }
            }
        }, config.configRoot().region_settings.delay_after_kick_region * 20);
        return true;
    }

    private int getBukkitVersion() {
        String name = Bukkit.getServer().getClass().getPackage().getName();
        String v = name.substring(name.lastIndexOf('.') + 1) + ".";
        String[] version = v.replace('_', '.').split("\\.");

        int lesserVersion = 0;
        try {
            lesserVersion = Integer.parseInt(version[2]);
        } catch (NumberFormatException ignored) {
        }
        return Integer.parseInt((version[0] + version[1]).substring(1) + lesserVersion);
    }

    private void startAutoSave() {
        Bukkit.getScheduler().cancelTask(autoSaveID);
        if (config.configRoot().flat_file.auto_save_interval_seconds != 0) {
            logger.info("Auto-save Scheduler: Saving " + config.configRoot().file_type + " database every " + config.configRoot().flat_file.auto_save_interval_seconds / 60 + " minutes!");

            autoSaveID = Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
                logger.debug(LogLevel.DEFAULT, "Auto-save Scheduler: Saving " + config.configRoot().file_type + " database!");
                rm.saveAll(config.configRoot().flat_file.backup_on_save);
            }, config.configRoot().flat_file.auto_save_interval_seconds * 20, config.configRoot().flat_file.auto_save_interval_seconds * 20).getTaskId();

        } else {
            logger.info("Auto-save Scheduler: Disabled");
        }
    }
}