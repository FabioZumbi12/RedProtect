package br.net.fabiozumbi12.RedProtect.Sponge;

import java.io.File;
import java.nio.file.Path;
import java.sql.Time;
import java.util.*;
import java.util.concurrent.TimeUnit;

import br.net.fabiozumbi12.RedProtect.Sponge.API.RedProtectAPI;
import com.google.inject.Inject;
import org.spongepowered.api.Game;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.Platform.Component;
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

import br.net.fabiozumbi12.RedProtect.Sponge.config.RPConfig;
import br.net.fabiozumbi12.RedProtect.Sponge.config.RPLang;
import br.net.fabiozumbi12.RedProtect.Sponge.config.VersionData;
import br.net.fabiozumbi12.RedProtect.Sponge.listeners.RPAddProtection;
import br.net.fabiozumbi12.RedProtect.Sponge.listeners.RPBlockListener;
import br.net.fabiozumbi12.RedProtect.Sponge.listeners.RPCommands;
import br.net.fabiozumbi12.RedProtect.Sponge.listeners.RPEntityListener;
import br.net.fabiozumbi12.RedProtect.Sponge.listeners.RPGlobalListener;
import br.net.fabiozumbi12.RedProtect.Sponge.listeners.RPMine18;
import br.net.fabiozumbi12.RedProtect.Sponge.listeners.RPPlayerListener;
import br.net.fabiozumbi12.RedProtect.Sponge.listeners.RPWorldListener;

@Plugin(id = "redprotect", 
name = "RedProtect", 
version = VersionData.VERSION,
authors="FabioZumbi12", 
description="Complete antigrief plugin",
dependencies=@Dependency(id = "worldedit", optional = true))
public class RedProtect {
	public boolean WE;
	private UUID taskid;
	private CommandManager cmdService;
	public RegionManager rm;
	public final List<String> changeWait = new ArrayList<>();
	public final List<String> tpWait = new ArrayList<>();
	public RPPermHandler ph;
	public final RPLogger logger = new RPLogger();
	public Server serv;
	public final HashMap<Player, Location<World>> firstLocationSelections = new HashMap<>();
	public final HashMap<Player, Location<World>> secondLocationSelections = new HashMap<>();
	public boolean OnlineMode;
	public RPConfig cfgs;
	public EconomyService econ;
	public final HashMap<Player,String> alWait = new HashMap<>();
	public final HashMap<String, List<String>> denyEnter = new HashMap<>();
	
	private RPVHelper pvhelp;
	public RPVHelper getPVHelper(){
		return pvhelp;
	}

    private RedProtectAPI rpAPI;
    public RedProtectAPI getAPI(){
        return rpAPI;
    }

    private static RedProtect instance;
    public static RedProtect get(){
        return instance;
    }

    @Inject
    @ConfigDir(sharedRoot = false)
    public File configDir;

    @Inject
    public Game game;

    @Inject
    public PluginContainer container;

    @Listener
	public void onStopServer(GameStoppingServerEvent e) {
    	for (Player p:game.getServer().getOnlinePlayers()){
    		pvhelp.closeInventory(p);
    	}
    	rm.saveAll();
        rm.unloadAll();
        logger.SaveLogs();
        for (Task task:Sponge.getScheduler().getScheduledTasks(this)){
        	task.cancel();
        }
        logger.severe(container.getName() + " disabled.");
    }
    
    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        try {
			String v = Sponge.getGame().getPlatform().getContainer(Component.API).getVersion().get();
            instance = this;

			if (v.startsWith("5") || v.startsWith("6")){
				pvhelp = (RPVHelper)Class.forName("br.net.fabiozumbi12.RedProtect.Sponge.RPVHelper56").newInstance();
			}
			if (v.startsWith("7")){
				pvhelp = (RPVHelper)Class.forName("br.net.fabiozumbi12.RedProtect.Sponge.RPVHelper7").newInstance();
			}

            initVars();               
            OnlineMode = serv.getOnlineMode();           

            cmdService.register(container, new RPCommands(), Arrays.asList("redprotect","rp","regionp","regp"));
            
            game.getEventManager().registerListeners(container, new RPGlobalListener());
            game.getEventManager().registerListeners(container, new RPBlockListener());
            game.getEventManager().registerListeners(container, new RPPlayerListener());
            game.getEventManager().registerListeners(container, new RPEntityListener());
            game.getEventManager().registerListeners(container, new RPWorldListener());
            game.getEventManager().registerListeners(container, new RPMine18());
            game.getEventManager().registerListeners(container, new RPAddProtection());
            
            loadRegions();

			logger.info("Loading API...");
			this.rpAPI = new RedProtectAPI();
			logger.info("API Loaded!");
            
            logger.clear("&4 _   _  _  &c _   _   _  _ _  _  _ _ _  __");
            logger.clear("&4|_| |_ | \\ &c|_| |_| | |  |  |_ |   |    / ");
            logger.clear("&4| \\ |_ |_/ &c|   | \\ |_|  |  |_ |_  |   /");
            logger.clear("&a¯ Redprotect "+container.getVersion().get()+" enabled");
            logger.clear("");
            
        } catch (Exception e) {
    		e.printStackTrace();
    		logger.severe("Error enabling RedProtect, plugin will shut down.");
        }
    }
    
    private void loadRegions() throws Exception {
    	rm.loadAll();
    	if (cfgs.getString("file-type").equalsIgnoreCase("file")){
        	RPUtil.ReadAllDB(rm.getAllRegions());
        	AutoSaveHandler(); 
    	} else {
    		logger.info("Theres " + rm.getTotalRegionsNum() + " regions on (" + cfgs.getString("file-type") + ") database!");
    	}
    }

	public boolean denyEnterRegion(String rid, String player){
		if (denyEnter.containsKey(player)){
			if (denyEnter.get(player).contains(rid)){
				return false;
			}
			List<String> regs = denyEnter.get(player);
			regs.add(rid);
			denyEnter.put(player, regs);
		} else {
			denyEnter.put(player, new LinkedList<>(Collections.singletonList(rid)));
		}

		Sponge.getScheduler().createAsyncExecutor(this.container).schedule(() -> {
			if (denyEnter.containsKey(player)){
				List<String> regs = denyEnter.get(player);
				regs.remove(rid);
				if (regs.isEmpty()){
					denyEnter.remove(player);
				} else {
					denyEnter.put(player, regs);
				}
			}
		}, cfgs.getInt("region-settings.delay-after-kick-region"), TimeUnit.SECONDS);
		return true;
	}

    private void shutDown(){
    	rm.saveAll();
    	rm.unloadAll();
    	logger.SaveLogs();
    	Sponge.getScheduler().getScheduledTasks(container).forEach(Task::cancel);
    	logger.severe(container.getName() + " turn off...");
    }
    
    public void reload(){
    	try {
    		//shutdown
        	shutDown();
        	
    		cfgs = new RPConfig();
    		RPLang.init();
    		
    		//start
			loadRegions();
		} catch (Exception e) {
			e.printStackTrace();
		}	
    }
    
    @Listener
    public void onReloadPlugins(GameReloadEvent event) {
    	for (Player p:game.getServer().getOnlinePlayers()){
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
		if (taskid != null && Sponge.getScheduler().getTaskById(taskid).isPresent()){
			Sponge.getScheduler().getTaskById(taskid).get().cancel();
		}
		if (cfgs.getInt("flat-file.auto-save-interval-seconds") != 0){
			logger.info("Auto-save Scheduler: Saving "+cfgs.getString("file-type")+" database every " + cfgs.getInt("flat-file.auto-save-interval-seconds")/60 + " minutes!");
			
			taskid = Sponge.getScheduler().createSyncExecutor(container).scheduleWithFixedDelay(() -> {
                logger.debug("default","Auto-save Scheduler: Saving "+cfgs.getString("file-type")+" database!");
                rm.saveAll();
                },cfgs.getInt("flat-file.auto-save-interval-seconds"), cfgs.getInt("flat-file.auto-save-interval-seconds"), TimeUnit.SECONDS).getTask().getUniqueId();
			
		} else {
        	logger.info("Auto-save Scheduler: Disabled");
        }
	}

    private void initVars() throws Exception {
        container = Sponge.getPluginManager().getPlugin("redprotect").get();
        serv = Sponge.getServer();        
        cmdService = game.getCommandManager();
        cfgs = new RPConfig();
        RPLang.init();
        
        WE = checkWE();
        
        ph = new RPPermHandler();
        rm = new RegionManager();
    }
    
    private boolean checkWE() {
		return Sponge.getPluginManager().getPlugin("worldedit").isPresent();
	}
    
}