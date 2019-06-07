/*
 * Copyright (c) 2019 - @FabioZumbi12
 * Last Modified: 25/04/19 07:02
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

package br.net.fabiozumbi12.RedProtect.Sponge;

import br.net.fabiozumbi12.RedProtect.Core.helpers.CoreUtil;
import br.net.fabiozumbi12.RedProtect.Core.helpers.LogLevel;
import br.net.fabiozumbi12.RedProtect.Sponge.API.RedProtectAPI;
import br.net.fabiozumbi12.RedProtect.Sponge.commands.CommandHandler;
import br.net.fabiozumbi12.RedProtect.Sponge.config.ConfigManager;
import br.net.fabiozumbi12.RedProtect.Sponge.config.LangGuiManager;
import br.net.fabiozumbi12.RedProtect.Sponge.config.LangManager;
import br.net.fabiozumbi12.RedProtect.Sponge.config.VersionData;
import br.net.fabiozumbi12.RedProtect.Sponge.helpers.PermissionHandler;
import br.net.fabiozumbi12.RedProtect.Sponge.helpers.RedProtectLogger;
import br.net.fabiozumbi12.RedProtect.Sponge.helpers.RedProtectUtil;
import br.net.fabiozumbi12.RedProtect.Sponge.helpers.VersionHelper;
import br.net.fabiozumbi12.RedProtect.Sponge.hooks.HooksManager;
import br.net.fabiozumbi12.RedProtect.Sponge.listeners.*;
import br.net.fabiozumbi12.RedProtect.Sponge.region.RegionManager;
import com.google.inject.Inject;
import ninja.leaping.configurate.objectmapping.GuiceObjectMapperFactory;
import org.spongepowered.api.Platform.Component;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Plugin(id = "redprotect",
        name = "RedProtect",
        version = VersionData.VERSION,
        authors = "FabioZumbi12",
        description = "Complete antigrief plugin",
        dependencies = {
                @Dependency(id = "worldedit", optional = true),
                @Dependency(id = "dynmap", optional = true)})
public class RedProtect {

    private static RedProtect instance;
    public final List<String> changeWait = new ArrayList<>();
    public final List<String> tpWait = new ArrayList<>();
    public final RedProtectLogger logger = new RedProtectLogger();
    public final HooksManager hooks = new HooksManager();
    public final List<String> confiemStart = new ArrayList<>();
    public final HashMap<Player, Location<World>> firstLocationSelections = new HashMap<>();
    public final HashMap<Player, Location<World>> secondLocationSelections = new HashMap<>();
    public final HashMap<Player, String> alWait = new HashMap<>();
    public final HashMap<String, List<String>> denyEnter = new HashMap<>();
    public final List<String> teleportDelay = new ArrayList<>();
    private RedProtectUtil redProtectUtil = new RedProtectUtil();
    @Inject
    @ConfigDir(sharedRoot = false)
    public File configDir;
    @Inject
    public PluginContainer container;
    @Inject
    public GuiceObjectMapperFactory factory;
    public RegionManager rm;
    public PermissionHandler ph;
    public ConfigManager config;
    public LangManager lang;
    public LangGuiManager guiLang;
    public EconomyService economy;
    public CommandManager commandManager;
    public CommandHandler commandHandler;
    private UUID autoSaveID;
    private VersionHelper rpvHelper;
    private RedProtectAPI redProtectAPI;

    public static RedProtect get() {
        return instance;
    }

    public VersionHelper getVersionHelper() {
        return rpvHelper;
    }

    public RedProtectAPI getAPI() {
        return redProtectAPI;
    }

    public Server getServer() {
        return Sponge.getServer();
    }

    public RedProtectUtil getUtil(){
        return this.redProtectUtil;
    }

    @Listener
    public void onStopServer(GameStoppingServerEvent event) {
        shutDown();
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        try {
            String v = Sponge.getGame().getPlatform().getContainer(Component.API).getVersion().orElse("0");

            instance = this;
            commandManager = Sponge.getGame().getCommandManager();

            ph = new PermissionHandler();
            rm = new RegionManager();

            //Init config, lang, listeners and flags
            startLoad();

            if (v.startsWith("7")) {
                rpvHelper = (VersionHelper) Class.forName("br.net.fabiozumbi12.RedProtect.Sponge.VersionHelper7").newInstance();
                Sponge.getGame().getEventManager().registerListeners(container, Class.forName("br.net.fabiozumbi12.RedProtect.Sponge.listeners.RPBlockListener7").newInstance());
            } else if (v.startsWith("8")) {
                Sponge.getGame().getEventManager().registerListeners(container, Class.forName("br.net.fabiozumbi12.RedProtect.Sponge.listeners.RPBlockListener8").newInstance());
                rpvHelper = (VersionHelper) Class.forName("br.net.fabiozumbi12.RedProtect.Sponge.VersionHelper8").newInstance();
            } else {
                Sponge.getGame().getEventManager().registerListeners(container, Class.forName("br.net.fabiozumbi12.RedProtect.Sponge.listeners.BlockListenerCompat56").newInstance());
                rpvHelper = (VersionHelper) Class.forName("br.net.fabiozumbi12.RedProtect.Sponge.helpers.VersionHelper56").newInstance();
            }

            logger.info("Loading API...");
            this.redProtectAPI = new RedProtectAPI();
            logger.info("API Loaded!");

            logger.info("Sponge version: " + v);
            logger.clear("\n" +
                    "&4 _   _  _  &c _   _   _  _ _  _  _ _ _  __\n" +
                    "&4|_| |_ | \\ &c|_| |_| | |  |  |_ |   |    / \n" +
                    "&4| \\ |_ |_/ &c|   | \\ |_|  |  |_ |_  |   /\n" +
                    "&a¯ Redprotect " + container.getVersion().get() + " enabled\n" +
                    "");

        } catch (Exception e) {
            CoreUtil.printJarVersion();
            e.printStackTrace();

            Sponge.getGame().getServer().setHasWhitelist(true);
            Sponge.getGame().getServer().getOnlinePlayers().forEach(Player::kick);

            logger.severe("Error enabling RedProtect, plugin will shut down.");
            logger.severe("Due to an error in RedProtect loading, the whitelist has been turned on and every player has been kicked.");
            logger.severe("DO NOT LET ANYONE ENTER before fixing the problem, otherwise you risk losing protected regions.");
        }
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

        Sponge.getScheduler().createAsyncExecutor(this.container).schedule(() -> {
            if (denyEnter.containsKey(player)) {
                List<String> regs = denyEnter.get(player);
                regs.remove(rid);
                if (regs.isEmpty()) {
                    denyEnter.remove(player);
                } else {
                    denyEnter.put(player, regs);
                }
            }
        }, config.configRoot().region_settings.delay_after_kick_region, TimeUnit.SECONDS);
        return true;
    }

    private void shutDown() {
        // Unregister commands
        commandHandler.unregisterAll();

        // Save and unload all regions
        rm.saveAll(true);
        rm.unloadAll();

        // Cancel tasks from sponge scheduler and save logs
        for (Task task : Sponge.getScheduler().getScheduledTasks(this)) task.cancel();
        logger.SaveLogs();

        // Unregister listeners
        logger.info("Unregistering listeners...");
        Sponge.getEventManager().unregisterPluginListeners(this.container);
        Sponge.getEventManager().unregisterPluginListeners(this.container);

        logger.info(container.getName() + " turned off...");
    }

    private void startLoad() throws Exception {
        redProtectUtil = new RedProtectUtil();
        config = new ConfigManager(this.factory);
        lang = new LangManager();

        if (RedProtect.get().config.configRoot().purge.regen.enable_whitelist_regen && Sponge.getServer().hasWhitelist()) {
            Sponge.getServer().setHasWhitelist(false);
            RedProtect.get().logger.success("Whitelist disabled!");
        }

        logger.info("Registering commands...");
        commandHandler = new CommandHandler(this);

        logger.info("Registering listeners...");
        Sponge.getGame().getEventManager().registerListeners(container, commandHandler);
        Sponge.getGame().getEventManager().registerListeners(container, new BlockListener());
        Sponge.getGame().getEventManager().registerListeners(container, new GlobalListener());
        Sponge.getGame().getEventManager().registerListeners(container, new PlayerListener());
        Sponge.getGame().getEventManager().registerListeners(container, new EntityListener());
        Sponge.getGame().getEventManager().registerListeners(container, new WorldListener());

        // Register hooks
        this.hooks.registerHooksFirst();

        try {
            rm = new RegionManager();
            rm.loadAll();

            RedProtect.get().getUtil().ReadAllDB(rm.getAllRegions());

            if (!config.configRoot().file_type.equalsIgnoreCase("mysql")) {
                AutoSaveHandler();
            }
            logger.info("There are " + rm.getTotalRegionsNum() + " regions on (" + config.configRoot().file_type + ") database!");
        } catch (Exception e) {
            CoreUtil.printJarVersion();
            e.printStackTrace();
        }

        // Register hooks
        this.hooks.registerHooksLast();

        // Load Gui lang file
        guiLang = new LangGuiManager();

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

    @Listener
    public void onReloadPlugins(GameReloadEvent event) {
        for (Player p : Sponge.getGame().getServer().getOnlinePlayers()) {
            rpvHelper.closeInventory(p);
        }
        reload();
        logger.success("RedProtect reloaded with success!");
    }

    @Listener
    public void onChangeServiceProvider(ChangeServiceProviderEvent event) {
        if (event.getService().equals(EconomyService.class)) {
            economy = (EconomyService) event.getNewProviderRegistration().getProvider();
        }
    }

    private void AutoSaveHandler() {
        if (autoSaveID != null && Sponge.getScheduler().getTaskById(autoSaveID).isPresent()) {
            Sponge.getScheduler().getTaskById(autoSaveID).get().cancel();
        }
        if (config.configRoot().flat_file.auto_save_interval_seconds != 0) {
            logger.info("Auto-save Scheduler: Saving " + config.configRoot().file_type + " database every " + config.configRoot().flat_file.auto_save_interval_seconds / 60 + " minutes!");

            autoSaveID = Sponge.getScheduler().createAsyncExecutor(container).scheduleWithFixedDelay(() -> {
                logger.debug(LogLevel.DEFAULT, "Auto-save Scheduler: Saving " + config.configRoot().file_type + " database!");
                rm.saveAll(config.configRoot().flat_file.backup_on_save);
            }, config.configRoot().flat_file.auto_save_interval_seconds, config.configRoot().flat_file.auto_save_interval_seconds, TimeUnit.SECONDS).getTask().getUniqueId();

        } else {
            logger.info("Auto-save Scheduler: Disabled");
        }
    }
}