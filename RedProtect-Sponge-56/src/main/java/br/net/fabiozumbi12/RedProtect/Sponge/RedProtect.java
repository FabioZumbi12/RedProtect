package br.net.fabiozumbi12.RedProtect.Sponge;

import br.net.fabiozumbi12.RedProtect.Sponge.API.RedProtectAPI;
import br.net.fabiozumbi12.RedProtect.Sponge.config.RPConfig;
import br.net.fabiozumbi12.RedProtect.Sponge.config.RPLang;
import br.net.fabiozumbi12.RedProtect.Sponge.config.VersionData;
import br.net.fabiozumbi12.RedProtect.Sponge.listeners.*;
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

	@Inject
	public GuiceObjectMapperFactory factory;

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
			boolean newApi = false;
			if (v.startsWith("7") || v.startsWith("8")){
				newApi = true;
			}
            instance = this;

			if (newApi){
				pvhelp = (RPVHelper)Class.forName("br.net.fabiozumbi12.RedProtect.Sponge.RPVHelper7").newInstance();
			} else {
				pvhelp = (RPVHelper)Class.forName("br.net.fabiozumbi12.RedProtect.Sponge.RPVHelper56").newInstance();
			}

            initVars();               
            OnlineMode = serv.getOnlineMode();
            cmdService.register(container, new RPCommands(), Arrays.asList("redprotect","rp","regionp","regp"));

			if (newApi){
				game.getEventManager().registerListeners(container, Class.forName("br.net.fabiozumbi12.RedProtect.Sponge.listeners.RPBlockListener78").newInstance());
			} else {
				game.getEventManager().registerListeners(container, Class.forName("br.net.fabiozumbi12.RedProtect.Sponge.listeners.RPBlockListener56").newInstance());
			}

			game.getEventManager().registerListeners(container, new RPBlockListener());
            game.getEventManager().registerListeners(container, new RPGlobalListener());
            game.getEventManager().registerListeners(container, new RPPlayerListener());
            game.getEventManager().registerListeners(container, new RPEntityListener());
            game.getEventManager().registerListeners(container, new RPWorldListener());
            game.getEventManager().registerListeners(container, new RPMine18());
            game.getEventManager().registerListeners(container, new RPAddProtection());
            
            loadRegions();

			logger.info("Loading API...");
			this.rpAPI = new RedProtectAPI();
			logger.info("API Loaded!");

			logger.info("Sponge version: "+v);
            logger.clear("\n" +
					"&4 _   _  _  &c _   _   _  _ _  _  _ _ _  __\n" +
					"&4|_| |_ | \\ &c|_| |_| | |  |  |_ |   |    / \n" +
					"&4| \\ |_ |_/ &c|   | \\ |_|  |  |_ |_  |   /\n" +
					"&a¯ Redprotect "+container.getVersion().get()+" enabled\n" +
					"");
            
        } catch (Exception e) {
    		e.printStackTrace();
    		logger.severe("Error enabling RedProtect, plugin will shut down.");
        }
    }
    
    private void loadRegions() throws Exception {
    	rm.loadAll();
    	if (cfgs.root().file_type.equalsIgnoreCase("file")){
        	RPUtil.ReadAllDB(rm.getAllRegions());
        	AutoSaveHandler(); 
    	} else {
    		logger.info("Theres " + rm.getTotalRegionsNum() + " regions on (" + cfgs.root().file_type + ") database!");
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
		}, cfgs.root().region_settings.delay_after_kick_region, TimeUnit.SECONDS);
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
        	
    		cfgs = new RPConfig(this.factory);
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
		if (cfgs.root().flat_file.auto_save_interval_seconds != 0){
			logger.info("Auto-save Scheduler: Saving "+cfgs.root().file_type+" database every " + cfgs.root().flat_file.auto_save_interval_seconds/60 + " minutes!");
			
			taskid = Sponge.getScheduler().createSyncExecutor(container).scheduleWithFixedDelay(() -> {
                logger.debug("default","Auto-save Scheduler: Saving "+cfgs.root().file_type+" database!");
                rm.saveAll();
                },cfgs.root().flat_file.auto_save_interval_seconds, cfgs.root().flat_file.auto_save_interval_seconds, TimeUnit.SECONDS).getTask().getUniqueId();
			
		} else {
        	logger.info("Auto-save Scheduler: Disabled");
        }
	}

    private void initVars() throws Exception {
        container = Sponge.getPluginManager().getPlugin("redprotect").get();
        serv = Sponge.getServer();        
        cmdService = game.getCommandManager();
        cfgs = new RPConfig(factory);
        RPLang.init();
        
        WE = checkWE();
        
        ph = new RPPermHandler();
        rm = new RegionManager();
    }
    
    private boolean checkWE() {
		return Sponge.getPluginManager().getPlugin("worldedit").isPresent();
	}
    
}