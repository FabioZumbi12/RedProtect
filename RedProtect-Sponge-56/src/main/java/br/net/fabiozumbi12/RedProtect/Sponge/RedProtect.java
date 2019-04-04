/*
 *
 * Copyright (c) 2019 - @FabioZumbi12
 * Last Modified: 28/03/19 03:30
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
 *
 */

package br.net.fabiozumbi12.RedProtect.Sponge;

import br.net.fabiozumbi12.RedProtect.Sponge.API.RedProtectAPI;
import br.net.fabiozumbi12.RedProtect.Sponge.commands.CommandHandler;
import br.net.fabiozumbi12.RedProtect.Sponge.config.RPConfig;
import br.net.fabiozumbi12.RedProtect.Sponge.config.RPLang;
import br.net.fabiozumbi12.RedProtect.Sponge.config.VersionData;
import br.net.fabiozumbi12.RedProtect.Sponge.helpers.RPUtil;
import br.net.fabiozumbi12.RedProtect.Sponge.helpers.RPVHelper;
import br.net.fabiozumbi12.RedProtect.Sponge.hooks.RPDynmap;
import br.net.fabiozumbi12.RedProtect.Sponge.listeners.*;
import br.net.fabiozumbi12.RedProtect.Sponge.region.RegionManager;
import com.google.inject.Inject;
import ninja.leaping.configurate.objectmapping.GuiceObjectMapperFactory;
import org.spongepowered.api.Game;
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
        dependencies = {@Dependency(id = "worldedit", optional = true), @Dependency(id = "dynmap", optional = true)})
public class RedProtect {
    private static RedProtect instance;
    public final List<String> changeWait = new ArrayList<>();
    public final List<String> tpWait = new ArrayList<>();
    public final RPLogger logger = new RPLogger();
    public final List<String> openGuis = new ArrayList<>();
    public final List<String> confiemStart = new ArrayList<>();
    public final HashMap<Player, Location<World>> firstLocationSelections = new HashMap<>();
    public final HashMap<Player, Location<World>> secondLocationSelections = new HashMap<>();
    public final HashMap<Player, String> alWait = new HashMap<>();
    public final HashMap<String, List<String>> denyEnter = new HashMap<>();
    public boolean WE;
    public boolean Dyn;
    public RegionManager rm;
    public RPPermHandler ph;
    public Server serv;
    public boolean OnlineMode;
    public RPConfig cfgs;
    public EconomyService econ;
    public RPDynmap dynmap;
    @Inject
    @ConfigDir(sharedRoot = false)
    public File configDir;
    @Inject
    private Game game;
    public Game getGame(){
        return this.game;
    }
    @Inject
    public PluginContainer container;
    @Inject
    public GuiceObjectMapperFactory factory;
    private UUID taskid;
    public CommandManager cmdService;
    private RPVHelper pvhelp;
    private RedProtectAPI rpAPI;
    public CommandHandler cmdHandler;

    public static RedProtect get() {
        return instance;
    }

    public RPVHelper getPVHelper() {
        return pvhelp;
    }

    public RedProtectAPI getAPI() {
        return rpAPI;
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

            container = Sponge.getPluginManager().getPlugin("redprotect").get();
            serv = Sponge.getServer();
            cmdService = game.getCommandManager();

            ph = new RPPermHandler();
            rm = new RegionManager();

            //--- Init config, lang, listeners and flags
            startLoad();

            if (v.startsWith("7")) {
                pvhelp = (RPVHelper) Class.forName("br.net.fabiozumbi12.RedProtect.Sponge.RPVHelper7").newInstance();
                game.getEventManager().registerListeners(container, Class.forName("br.net.fabiozumbi12.RedProtect.Sponge.listeners.RPBlockListener7").newInstance());
            } else if (v.startsWith("8")) {
                game.getEventManager().registerListeners(container, Class.forName("br.net.fabiozumbi12.RedProtect.Sponge.listeners.RPBlockListener8").newInstance());
                pvhelp = (RPVHelper) Class.forName("br.net.fabiozumbi12.RedProtect.Sponge.RPVHelper8").newInstance();
            } else {
                game.getEventManager().registerListeners(container, Class.forName("br.net.fabiozumbi12.RedProtect.Sponge.listeners.RPBlockListener56").newInstance());
                pvhelp = (RPVHelper) Class.forName("br.net.fabiozumbi12.RedProtect.Sponge.helpers.RPVHelper56").newInstance();
            }

            if (Dyn) {
                logger.info("Dynmap found. Hooked.");
                logger.info("Loading dynmap markers...");
                try {
                    dynmap = new RPDynmap();
                    game.getEventManager().registerListeners(container, dynmap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                logger.info("Dynmap markers loaded!");
            }

            logger.info("Loading API...");
            this.rpAPI = new RedProtectAPI();
            logger.info("API Loaded!");

            logger.info("Sponge version: " + v);
            logger.clear("\n" +
                    "&4 _   _  _  &c _   _   _  _ _  _  _ _ _  __\n" +
                    "&4|_| |_ | \\ &c|_| |_| | |  |  |_ |   |    / \n" +
                    "&4| \\ |_ |_/ &c|   | \\ |_|  |  |_ |_  |   /\n" +
                    "&a¯ Redprotect " + container.getVersion().get() + " enabled\n" +
                    "");

        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error enabling RedProtect, plugin will shut down.");
            game.getServer().setHasWhitelist(true);
            game.getServer().getOnlinePlayers().forEach(Player::kick);
            logger.warning("RedProtect turned the whitelist on and kicked all players to avoid players to loose your protected regions due an error on load RedProtect!");
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
        }, cfgs.root().region_settings.delay_after_kick_region, TimeUnit.SECONDS);
        return true;
    }

    private void shutDown() {
        cmdHandler.unregisterAll();

        rm.saveAll(true);
        rm.unloadAll();

        openGuis.clear();

        for (Task task : Sponge.getScheduler().getScheduledTasks(this)) task.cancel();
        logger.SaveLogs();

        logger.info("Unregistering listeners...");
        Sponge.getEventManager().unregisterPluginListeners(this.container);

        logger.info(container.getName() + " turn off...");
    }

    private void startLoad() throws Exception {
        cfgs = new RPConfig(this.factory);
        RPLang.init();

        if (RedProtect.get().cfgs.root().purge.regen.enable_whitelist_regen && Sponge.getServer().hasWhitelist()){
            Sponge.getServer().setHasWhitelist(false);
            RedProtect.get().logger.sucess("Whitelist disabled!");
        }

        //set online mode
        OnlineMode = cfgs.root().online_mode;

        logger.info("Registering commands...");
        cmdHandler = new CommandHandler(this);

        logger.info("Registering listeners...");
        game.getEventManager().registerListeners(container, new RPBlockListener());
        game.getEventManager().registerListeners(container, new RPGlobalListener());
        game.getEventManager().registerListeners(container, new RPPlayerListener());
        game.getEventManager().registerListeners(container, new RPEntityListener());
        game.getEventManager().registerListeners(container, new RPWorldListener());
        game.getEventManager().registerListeners(container, new RPMine18());
        game.getEventManager().registerListeners(container, new RPAddProtection());

        //-- hooks
        WE = checkWE();
        Dyn = checkDM();

        try{
            rm = new RegionManager();
            rm.loadAll();

            RPUtil.ReadAllDB(rm.getAllRegions());

            if (cfgs.root().file_type.equalsIgnoreCase("file")) {
                AutoSaveHandler();
            }
            logger.info("Theres " + rm.getTotalRegionsNum() + " regions on (" + cfgs.root().file_type + ") database!");
        } catch (Exception e){
            e.printStackTrace();
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

    @Listener
    public void onReloadPlugins(GameReloadEvent event) {
        for (Player p : game.getServer().getOnlinePlayers()) {
            pvhelp.closeInventory(p);
        }
        reload();
        logger.sucess("RedProtect reloaded with success!");
    }

    @Listener
    public void onChangeServiceProvider(ChangeServiceProviderEvent event) {
        if (event.getService().equals(EconomyService.class)) {
            econ = (EconomyService) event.getNewProviderRegistration().getProvider();
        }
    }

    private void AutoSaveHandler() {
        if (taskid != null && Sponge.getScheduler().getTaskById(taskid).isPresent()) {
            Sponge.getScheduler().getTaskById(taskid).get().cancel();
        }
        if (cfgs.root().flat_file.auto_save_interval_seconds != 0) {
            logger.info("Auto-save Scheduler: Saving " + cfgs.root().file_type + " database every " + cfgs.root().flat_file.auto_save_interval_seconds / 60 + " minutes!");

            taskid = Sponge.getScheduler().createAsyncExecutor(container).scheduleWithFixedDelay(() -> {
                logger.debug(LogLevel.DEFAULT, "Auto-save Scheduler: Saving " + cfgs.root().file_type + " database!");
                rm.saveAll(cfgs.root().flat_file.backup_on_save);
            }, cfgs.root().flat_file.auto_save_interval_seconds, cfgs.root().flat_file.auto_save_interval_seconds, TimeUnit.SECONDS).getTask().getUniqueId();

        } else {
            logger.info("Auto-save Scheduler: Disabled");
        }
    }

    private boolean checkWE() {
        return Sponge.getPluginManager().getPlugin("worldedit").isPresent();
    }

    private boolean checkDM() {
        return Sponge.getPluginManager().getPlugin("dynmap").isPresent();
    }


}