/*
 * Copyright (c) 2012-2025 - @FabioZumbi12
 * Last Modified: 24/06/2025 19:06
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

package br.net.fabiozumbi12.RedProtect.Bukkit;

import br.net.fabiozumbi12.RedProtect.Bukkit.API.RedProtectAPI;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.LangGuiManager;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.LangManager;
import br.net.fabiozumbi12.RedProtect.Bukkit.commands.CommandHandler;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.BlockConfig;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.ConfigManager;
import br.net.fabiozumbi12.RedProtect.Bukkit.helpers.*;
import br.net.fabiozumbi12.RedProtect.Bukkit.listeners.*;
import br.net.fabiozumbi12.RedProtect.Bukkit.hooks.HooksManager;
import br.net.fabiozumbi12.RedProtect.Bukkit.metrics.Metrics;
import br.net.fabiozumbi12.RedProtect.Bukkit.region.RegionManager;
import br.net.fabiozumbi12.RedProtect.Bukkit.schematics.RPSchematics;
import br.net.fabiozumbi12.RedProtect.Bukkit.updater.SpigetUpdater;
import br.net.fabiozumbi12.RedProtect.Core.helpers.CoreUtil;
import br.net.fabiozumbi12.RedProtect.Core.helpers.LogLevel;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class RedProtect extends JavaPlugin {
    private static RedProtect plugin;
    public final List<String> confirmStart = new ArrayList<>();
    public final HashMap<String, List<String>> denyEnter = new HashMap<>();
    public final HashMap<Player, Location> firstLocationSelections = new HashMap<>();
    public final HashMap<Player, Location> secondLocationSelections = new HashMap<>();
    public final HashMap<Player, String> alWait = new HashMap<>();
    public final List<String> changeWait = new ArrayList<>();
    public final List<String> tpWait = new ArrayList<>();
    public final HooksManager hooks = new HooksManager();
    public final RedProtectLogger logger = new RedProtectLogger();
    public final List<String> teleportDelay = new ArrayList<>();
    public RPSchematics schematic;
    public double bukkitVersion;
    public Economy economy;
    public Permission permission;
    public LangGuiManager guiLang;
    protected CommandHandler cmdHandler;
    private RedProtectUtil redProtectUtil = new RedProtectUtil(this);
    private RedProtectAPI redProtectAPI;
    private int autoSaveID;
    private VersionHelper rpvHelper;
    private SpigetUpdater updater;
    private ModListener modListener;
    private RegionManager rm;
    private PermissionHandler ph;
    private LangManager lang;
    private ConfigManager config;
    private BlockConfig blockConfig;

    public static RedProtect get() {
        return plugin;
    }

    public RegionManager getRegionManager() {
        return this.rm;
    }

    public PermissionHandler getPermissionHandler() {
        return this.ph;
    }

    public LangManager getLanguageManager() {
        return this.lang;
    }

    public ConfigManager getConfigManager() {
        return this.config;
    }

    public BlockConfig getBlockManager() {
        return this.blockConfig;
    }

    public VersionHelper getVersionHelper() {
        return rpvHelper;
    }

    public RedProtectAPI getAPI() {
        return redProtectAPI;
    }

    public SpigetUpdater getUpdater() {
        return this.updater;
    }

    public RedProtectUtil getUtil() {
        return this.redProtectUtil;
    }

    public void onDisable() {
        shutDown();
    }

    public void onEnable() {
        try {
            plugin = this;

            ph = new PermissionHandler();
            rm = new RegionManager();

            // Init schematic
            schematic = new RPSchematics();

            //Init config, lang, listeners and flags
            startLoad();

            logger.info("Loading API...");
            this.redProtectAPI = new RedProtectAPI();
            logger.info("API Loaded!");

            logger.clear("&4 _   _  _  &c _   _   _  _ _  _  _ _ _  _");
            logger.clear("&4|_| |_ | \\ &c|_| |_| | |  |  |_ |   |  |_|");
            logger.clear("&4| \\ |_ |_/ &c|   | \\ |_|  |  |_ |_  |  |_|");
            logger.clear("&a» " + getDescription().getFullName() + " enabled");
            logger.clear("");

            if (config.configRoot().enable_addons){
                AddonsManager.EnableAddons();
            }

        } catch (Exception e) {
            CoreUtil.printJarVersion();
            e.printStackTrace();

            getServer().setWhitelist(true);
            getServer().getOnlinePlayers().forEach(p -> p.kickPlayer("The server has been whitelisted due to an error while loading plugins!"));
            this.setEnabled(false);

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
            CoreUtil.printJarVersion();
            e.printStackTrace();
        }
    }

    public void reloadConfigs() {
        cmdHandler.unregisterAll();

        try {
            config = new ConfigManager();
        } catch (ObjectMappingException e) {
            CoreUtil.printJarVersion();
            e.printStackTrace();
        }

        logger.info("Loading language files...");
        lang = new LangManager();
        guiLang = new LangGuiManager();
        blockConfig = new BlockConfig();

        logger.info("Re-registering commands...");
        cmdHandler = new CommandHandler(this);
    }

    private void startLoad() throws Exception {
        redProtectUtil = new RedProtectUtil(this);
        config = new ConfigManager();
        lang = new LangManager();
        blockConfig = new BlockConfig();

        if (config.configRoot().purge.regen.enabled && config.configRoot().purge.regen.enable_whitelist_regen && Bukkit.getServer().hasWhitelist()) {
            Bukkit.getServer().setWhitelist(false);
            RedProtect.get().logger.success("Whitelist disabled!");
        }

        logger.info("Registering commands...");
        cmdHandler = new CommandHandler(this);

        logger.info("Registering listeners...");
        getServer().getPluginManager().registerEvents(cmdHandler, this);
        getServer().getPluginManager().registerEvents(new GlobalListener(), this);
        getServer().getPluginManager().registerEvents(new BlockListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new EntityListener(), this);
        getServer().getPluginManager().registerEvents(new WorldListener(), this);

        // Register hooks
        hooks.registerHooksFirst();

        rm = new RegionManager();
        rm.loadAll();

        redProtectUtil.ReadAllDB(rm.getAllRegions());

        rm.saveAll(false);

        if (!config.configRoot().file_type.equalsIgnoreCase("mysql")) {
            startAutoSave();
        }
        logger.info("There are " + rm.getTotalRegionsNum() + " regions on (" + config.configRoot().file_type + ") database!");

        // Register hooks
        hooks.registerHooksLast();

        // Load Gui lang file
        guiLang = new LangGuiManager();

        // Update Manager
        if (updater != null) {
            Bukkit.getScheduler().cancelTask(updater.getTaskId());
            updater = null;
        }
        if (config.configRoot().update.enable) {
            updater = new SpigetUpdater(this);
            updater.setCurrentJarFile(this.getFile().getName());
            updater.hourlyUpdateCheck(getServer().getConsoleSender(), config.configRoot().update.enable, false);
        }

        bukkitVersion = getBukkitVersion();
        logger.debug(LogLevel.DEFAULT, "Version String: " + bukkitVersion);

        // Load all version compats classes
        getServer().getPluginManager().registerEvents(new Compat18(), this);
        getServer().getPluginManager().registerEvents(new Compat19(), this);
        getServer().getPluginManager().registerEvents(new Compat111(), this);
        getServer().getPluginManager().registerEvents(new Compat114(), this);

        if (bukkitVersion >= 1.19) {
            rpvHelper = (VersionHelper) Class.forName("br.net.fabiozumbi12.RedProtect.Bukkit.helpers.VersionHelperLatest").newInstance();
        } else {
            logger.severe("RedProtect 8+ is not compatible with version 1.18 or lower. Download the latest RedProtect 7 from spigot or dev builds.");
            shutDown();
            return;
        }
        // Register as listener
        getServer().getPluginManager().registerEvents(rpvHelper, this);

        // Print helper version
        logger.info("Helper version: " + rpvHelper.getVersion());

        // Load gui items for Minecraft version
        config.setGuiRoot(rpvHelper.setGuiItems(config.guiRoot()));

        // Load mod permissions
        if (config.configRoot().server_protection.modsPermissionsEnable)
            modListener = new ModListener(this);

        // Metrics
        try {
            Metrics metrics = new Metrics(this);
            metrics.addCustomChart(new Metrics.SingleLineChart("server_regions", () -> rm.getAllRegions().size()));
            if (metrics.isEnabled())
                logger.info("Metrics enabled! See our stats here: https://bstats.org/plugin/bukkit/RedProtect");
        } catch (Exception ex) {
            logger.info("Metrics not enabled due errors: " + ex.getLocalizedMessage());
        }
    }

    private void shutDown() {
        // Unload messages listener
        modListener.unload();

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

        getServer().getScheduler().cancelTasks(this);

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
        }, config.configRoot().region_settings.delay_after_kick_region * 20L);
        return true;
    }

    private double getBukkitVersion() {
        var name = Bukkit.getServer().getBukkitVersion().split("-")[0].split("\\.");
        var version = name[0] + "." + name[1];
        if (name.length == 3)
            version += name[2];
        return Double.parseDouble(version);
    }

    private void startAutoSave() {
        Bukkit.getScheduler().cancelTask(autoSaveID);
        if (config.configRoot().flat_file.auto_save_interval_seconds != 0) {
            logger.info("Auto-save Scheduler: Saving " + config.configRoot().file_type + " database every " + config.configRoot().flat_file.auto_save_interval_seconds / 60 + " minutes!");

            autoSaveID = Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
                logger.debug(LogLevel.DEFAULT, "Auto-save Scheduler: Saving " + config.configRoot().file_type + " database!");
                rm.saveAll(config.configRoot().flat_file.backup_on_save);
            }, config.configRoot().flat_file.auto_save_interval_seconds * 20L, config.configRoot().flat_file.auto_save_interval_seconds * 20L).getTaskId();

        } else {
            logger.info("Auto-save Scheduler: Disabled");
        }
    }
}