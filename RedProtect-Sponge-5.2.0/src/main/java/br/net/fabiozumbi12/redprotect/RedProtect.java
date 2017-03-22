package br.net.fabiozumbi12.redprotect;

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
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import br.net.fabiozumbi12.redprotect.config.RPConfig;
import br.net.fabiozumbi12.redprotect.config.RPLang;
import br.net.fabiozumbi12.redprotect.listeners.RPBlockListener;
import br.net.fabiozumbi12.redprotect.listeners.RPCommands;
import br.net.fabiozumbi12.redprotect.listeners.RPEntityListener;
import br.net.fabiozumbi12.redprotect.listeners.RPGlobalListener;
import br.net.fabiozumbi12.redprotect.listeners.RPMine18;
import br.net.fabiozumbi12.redprotect.listeners.RPPlayerListener;
import br.net.fabiozumbi12.redprotect.listeners.RPWorldListener;

@Plugin(id = "redprotect", 
name = "RedProtect", 
version = "${buildNumber}",
authors="FabioZumbi12", 
description="Complete antigrief plugin")
public class RedProtect {
	public static Game game;
	public static PluginContainer plugin;
	private static UUID taskid;
	private CommandManager cmdService; 
    public static RegionManager rm;
    public static List<String> changeWait = new ArrayList<String>();
    public static List<String> tpWait = new ArrayList<String>();
    public static RPPermHandler ph;
    public static RPLogger logger = new RPLogger();
    public static Server serv;    
    public static HashMap<Player, Location<World>> firstLocationSelections = new HashMap<Player, Location<World>>();
    public static HashMap<Player, Location<World>> secondLocationSelections = new HashMap<Player, Location<World>>();
	public static String configDir;
    public static boolean OnlineMode;
    public static RPConfig cfgs;
    public static EconomyService econ;
    public static HashMap<Player,String> alWait = new HashMap<Player,String>();
        
    @Listener
	public void onStopServer(GameStoppingServerEvent e) {
    	for (Player p:game.getServer().getOnlinePlayers()){
    		p.closeInventory(Cause.of(NamedCause.of(p.getName(),p)));
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
            initVars();               
            OnlineMode = serv.getOnlineMode();           
            
            cmdService.register(plugin, new RPCommands(), Arrays.asList("redprotect","rp","regionp","regp"));
            
            game.getEventManager().registerListeners(plugin, new RPGlobalListener());
            game.getEventManager().registerListeners(plugin, new RPBlockListener());
            game.getEventManager().registerListeners(plugin, new RPPlayerListener());
            game.getEventManager().registerListeners(plugin, new RPEntityListener());
            game.getEventManager().registerListeners(plugin, new RPWorldListener());              
            game.getEventManager().registerListeners(plugin, new RPMine18());
            
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
    	Sponge.getScheduler().getScheduledTasks(plugin).stream().forEach(task->task.cancel());
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
    		p.closeInventory(Cause.of(NamedCause.of(p.getName(),p)));
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
			
			taskid = Sponge.getScheduler().createSyncExecutor(RedProtect.plugin).scheduleWithFixedDelay(new Runnable() { 
				public void run() {
					RedProtect.logger.debug("default","Auto-save Scheduler: Saving "+cfgs.getString("file-type")+" database!");
					rm.saveAll();
					} 
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
        
        ph = new RPPermHandler();
        rm = new RegionManager();
    }
    
}