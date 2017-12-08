package br.net.fabiozumbi12.RedProtect.Sponge;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.spongepowered.api.Game;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.Platform.Component;
import org.spongepowered.api.command.CommandManager;
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
	public static boolean WE;
	public static Game game;
	public static PluginContainer plugin;
	private static UUID taskid;
	private CommandManager cmdService;
	public static RegionManager rm;
	public static final List<String> changeWait = new ArrayList<>();
	public static final List<String> tpWait = new ArrayList<>();
	public static RPPermHandler ph;
	public static final RPLogger logger = new RPLogger();
	public static Server serv;    
	public static final HashMap<Player, Location<World>> firstLocationSelections = new HashMap<>();
	public static final HashMap<Player, Location<World>> secondLocationSelections = new HashMap<>();
	public static String configDir;
	public static boolean OnlineMode;
	public static RPConfig cfgs;
	public static EconomyService econ;
	public static final HashMap<Player,String> alWait = new HashMap<>();
	
	private static RPVHelper pvhelp;
	public static RPVHelper getPVHelper(){
		return pvhelp;
	}
     	
    @Listener
	public void onStopServer(GameStoppingServerEvent e) {
    	for (Player p:game.getServer().getOnlinePlayers()){
    		pvhelp.closeInventory(p);
    	}
    	RedProtect.rm.saveAll();
        RedProtect.rm.unloadAll();
        logger.SaveLogs();
        for (Task task:Sponge.getScheduler().getScheduledTasks(this)){
        	task.cancel();
        }
        RedProtect.logger.severe(RedProtect.plugin.getName() + " disabled.");
    }
    
    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        try {
			String v = Sponge.getGame().getPlatform().getContainer(Component.API).getVersion().get();
			if (v.startsWith("5") || v.startsWith("6")){
				pvhelp = (RPVHelper)Class.forName("br.net.fabiozumbi12.RedProtect.Sponge.RPVHelper56").newInstance();
			}
			if (v.startsWith("7")){
				pvhelp = (RPVHelper)Class.forName("br.net.fabiozumbi12.RedProtect.Sponge.RPVHelper7").newInstance();
			}

            initVars();               
            OnlineMode = serv.getOnlineMode();           

            cmdService.register(plugin, new RPCommands(), Arrays.asList("redprotect","rp","regionp","regp"));
            
            game.getEventManager().registerListeners(plugin, new RPGlobalListener());
            game.getEventManager().registerListeners(plugin, new RPBlockListener());
            game.getEventManager().registerListeners(plugin, new RPPlayerListener());
            game.getEventManager().registerListeners(plugin, new RPEntityListener());
            game.getEventManager().registerListeners(plugin, new RPWorldListener());              
            game.getEventManager().registerListeners(plugin, new RPMine18());
            game.getEventManager().registerListeners(plugin, new RPAddProtection());
            
            loadRegions();
            
            RedProtect.logger.clear("&4 _   _  _  &c _   _   _  _ _  _  _ _ _  __");
            RedProtect.logger.clear("&4|_| |_ | \\ &c|_| |_| | |  |  |_ |   |    / ");
            RedProtect.logger.clear("&4| \\ |_ |_/ &c|   | \\ |_|  |  |_ |_  |   /");
            RedProtect.logger.clear("&a¯ Redprotect "+plugin.getVersion().get()+" enabled");
            RedProtect.logger.clear("");
            
        } catch (Exception e) {
    		e.printStackTrace();
    		RedProtect.logger.severe("Error enabling RedProtect, plugin will shut down.");
        }
    }
    
    private static void loadRegions() throws Exception {
    	rm.loadAll();
    	if (cfgs.getString("file-type").equalsIgnoreCase("file")){
        	RPUtil.ReadAllDB(rm.getAllRegions());
        	AutoSaveHandler(); 
    	} else {
    		RedProtect.logger.info("Theres " + rm.getTotalRegionsNum() + " regions on (" + cfgs.getString("file-type") + ") database!");        		
    	}
    }
    
    private static void shutDown(){
    	rm.saveAll();
    	rm.unloadAll();
    	logger.SaveLogs();
    	Sponge.getScheduler().getScheduledTasks(plugin).forEach(Task::cancel);
    	RedProtect.logger.severe(plugin.getName() + " turn off...");
    }
    
    public static void reload(){
    	try {
    		//shutdown
        	shutDown();
        	
    		RedProtect.cfgs = new RPConfig();
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
    	RedProtect.logger.sucess("RedProtect reloaded with success!");
    }
    
    @Listener
	public void onChangeServiceProvider(ChangeServiceProviderEvent event) {
		if (event.getService().equals(EconomyService.class)) {
            econ = (EconomyService) event.getNewProviderRegistration().getProvider();
		}
	}
    
	private static void AutoSaveHandler() {
		if (taskid != null && Sponge.getScheduler().getTaskById(taskid).isPresent()){
			Sponge.getScheduler().getTaskById(taskid).get().cancel();
		}
		if (cfgs.getInt("flat-file.auto-save-interval-seconds") != 0){
			RedProtect.logger.info("Auto-save Scheduler: Saving "+cfgs.getString("file-type")+" database every " + cfgs.getInt("flat-file.auto-save-interval-seconds")/60 + " minutes!");  
			
			taskid = Sponge.getScheduler().createSyncExecutor(RedProtect.plugin).scheduleWithFixedDelay(() -> {
                RedProtect.logger.debug("default","Auto-save Scheduler: Saving "+cfgs.getString("file-type")+" database!");
                rm.saveAll();
                },cfgs.getInt("flat-file.auto-save-interval-seconds"), cfgs.getInt("flat-file.auto-save-interval-seconds"), TimeUnit.SECONDS).getTask().getUniqueId();
			
		} else {
        	RedProtect.logger.info("Auto-save Scheduler: Disabled");
        }
	}
	
    private void initVars() throws Exception {
    	game = Sponge.getGame();
    	plugin = Sponge.getPluginManager().getPlugin("redprotect").get();
    	configDir = game.getConfigManager().getSharedConfig(RedProtect.plugin).getDirectory()+File.separator+"RedProtect"+File.separator;
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