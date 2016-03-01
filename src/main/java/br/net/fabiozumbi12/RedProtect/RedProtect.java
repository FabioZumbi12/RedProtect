package br.net.fabiozumbi12.RedProtect;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapAPI;

import br.net.fabiozumbi12.RedProtect.config.RPConfig;
import br.net.fabiozumbi12.RedProtect.config.RPLang;
import br.net.fabiozumbi12.RedProtect.hooks.Dynmap;
import br.net.fabiozumbi12.RedProtect.hooks.MPListener;
import br.net.fabiozumbi12.RedProtect.hooks.McMMoListener;
import br.net.fabiozumbi12.RedProtect.hooks.SkillAPIListener;
import br.net.fabiozumbi12.RedProtect.listeners.RPAddProtection;
import br.net.fabiozumbi12.RedProtect.listeners.RPBlockListener;
import br.net.fabiozumbi12.RedProtect.listeners.RPCommands;
import br.net.fabiozumbi12.RedProtect.listeners.RPEntityListener;
import br.net.fabiozumbi12.RedProtect.listeners.RPGlobalListener;
import br.net.fabiozumbi12.RedProtect.listeners.RPMine18;
import br.net.fabiozumbi12.RedProtect.listeners.RPPlayerListener;
import br.net.fabiozumbi12.RedProtect.listeners.RPWorldListener;

public class RedProtect extends JavaPlugin {
	public static File JarFile = null;
	public static PluginDescriptionFile pdf;
    public static RedProtect plugin;
	private int taskid;
	public static boolean Update;
	public static String UptVersion;
	public static String UptLink;    
    public static RegionManager rm;
    public static List<String> changeWait = new ArrayList<String>();
    public static List<String> tpWait = new ArrayList<String>();
    public static HashMap<Player,String> alWait = new HashMap<Player,String>();
    public static RPPermissionHandler ph;
    public static RPLogger logger = new RPLogger();
    public static Server serv;    
    public static HashMap<Player, Location> firstLocationSelections = new HashMap<Player, Location>();
    public static HashMap<Player, Location> secondLocationSelections = new HashMap<Player, Location>();
    public static String pathMain = "plugins" + File.separator + "RedProtect" + File.separator;
    public static String pathLogs = "plugins" + File.separator + "RedProtect" + File.separator + "logs" + File.separator;
    public static String pathData = String.valueOf(RedProtect.pathMain) + File.separator + "data" + File.separator;
    public static String pathConfig = String.valueOf(RedProtect.pathMain) + File.separator + "config.yml";
    public static String pathglobalFlags = String.valueOf(RedProtect.pathMain) + File.separator + "globalflags.yml"; 
    public static String pathGui = String.valueOf(RedProtect.pathMain) + File.separator + "guiconfig.yml"; 
    public static String protections = String.valueOf(RedProtect.pathMain) + File.separator + "protections.yml"; 
    public static String pathBlockValues = String.valueOf(RedProtect.pathMain) + File.separator + "economy.yml";;
    public static boolean BossBar;
    public static boolean MyChunk;
    public static boolean MyPet;
    static boolean McMMo;
    public static boolean OnlineMode;
	public static boolean Mc;
	static boolean SkillAPI;
	public static boolean Vault;
	public static boolean PvPm;
	public static boolean Ess;
	public static boolean GP;
	public static boolean WE;
	public static boolean AWE;
	static boolean Dyn;
	public static Dynmap dynmap;
	public static PlayerHandler PvPmanager;
	public static Economy econ;
    
    public void onDisable() {
        RedProtect.rm.saveAll();
        RedProtect.rm.unloadAll();
        logger.SaveLogs();
        Bukkit.getScheduler().cancelTasks(this);
        RedProtect.logger.severe(RedProtect.pdf.getFullName() + " disabled.");
    }
    
    public void onEnable() {
        try {
            plugin = this;
            BossBar = checkBM();
            MyChunk = checkMyChunk();
            MyPet = checkMyPet(); 
            McMMo = checkMcMMo();
            Mc = checkMc();
            Vault = checkVault();
            SkillAPI = checkSkillAPI();
            PvPm = checkPvPm();
            Ess = checkEss();
            GP = checkGP();
            Dyn = checkDyn();
            WE = checkWe();
            AWE = checkAWe();
            JarFile = this.getFile();
            initVars();
            RPConfig.init(this);
            RPLang.init(this);
            rm.loadAll();
            OnlineMode = serv.getOnlineMode();
            
            serv.getPluginManager().registerEvents(new RPGlobalListener(), this);
            serv.getPluginManager().registerEvents(new RPBlockListener(), this);
            serv.getPluginManager().registerEvents(new RPPlayerListener(), this);
            serv.getPluginManager().registerEvents(new RPEntityListener(), this);
            serv.getPluginManager().registerEvents(new RPWorldListener(), this);  
            serv.getPluginManager().registerEvents(new RPAddProtection(), this);   
            
            String v = RedProtect.serv.getBukkitVersion();
            if (v.contains("1.8") || v.contains("1.9")){
            	serv.getPluginManager().registerEvents(new RPMine18(), this);
            }
            
            getCommand("RedProtect").setExecutor(new RPCommands());
            
            if (Vault){
            	RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
                if (rsp == null) {
                	RedProtect.logger.warning("Vault found, but for some reason cant be used with RedProtect.");
                	Vault = false;
                } else {
                	econ = rsp.getProvider();
                	RedProtect.logger.info("Vault found. Hooked.");                	
                }
            }
            
            if (PvPm){
            	PvPManager pvp = (PvPManager) Bukkit.getPluginManager().getPlugin("PvPManager");
                if (pvp.getPlayerHandler() != null) {
                	PvPmanager = pvp.getPlayerHandler();
                	RedProtect.logger.info("PvPManager found. Hooked.");  
                } 
            }
            if (Ess){
            	RedProtect.logger.info("Essentials found. Hooked.");
            }
            if (WE){
            	RedProtect.logger.info("WorldEdit found. Hooked.");
            }
            if (AWE){
            	RedProtect.logger.info("AsyncWorldEdit found. Hooked.");
            }
            if (BossBar){
            	RedProtect.logger.info("BossbarAPI found. Hooked.");
            }
            if (MyPet){
            	serv.getPluginManager().registerEvents(new MPListener(), this);
            	RedProtect.logger.info("MyPet found. Hooked.");
            }
            if (McMMo){
            	serv.getPluginManager().registerEvents(new McMMoListener(), this);
            	RedProtect.logger.info("McMMo found. Hooked.");
            }
            if (SkillAPI){
            	serv.getPluginManager().registerEvents(new SkillAPIListener(), this);
            	RedProtect.logger.info("SkillAPI found. Hooked.");
            }
            if (MyChunk){
            	RedProtect.logger.sucess("MyChunk found. Ready to convert!");
            	RedProtect.logger.warning("Use '/rp mychunkconvert' to start MyChunk conversion (This may cause lag during conversion)");
            } 
            if (Mc){
            	RedProtect.logger.info("MagicCarpet found. Hooked.");
            }
            if (Dyn && RPConfig.getBool("hooks.dynmap.enable")){
            	RedProtect.logger.info("Dynmap found. Hooked.");
            	RedProtect.logger.info("Loading dynmap markers...");
            	dynmap = new Dynmap((DynmapAPI) Bukkit.getPluginManager().getPlugin("dynmap"));
            	RedProtect.logger.info("Dynmap markers loaded!");
            }
            
            if (!RPConfig.getString("file-type").equalsIgnoreCase("mysql")){
            	RPUtil.ReadAllDB(rm.getAllRegions());
        	} else {
        		RedProtect.logger.info("Theres " + rm.getTotalRegionsNum() + " regions on (" + RPConfig.getString("file-type") + ") database!");        		
        	}            
            
            RedProtect.logger.sucess(RedProtect.pdf.getFullName() + " enabled.");  
            
            if (RPConfig.getBool("update-check.enable")){
            	RedProtect.logger.info("Checking for update..."); 
            	if (CheckUpdate()){
            		RedProtect.logger.sucess("Update available! Use /rp update to download."); 
            	} else {
            		RedProtect.logger.info("No update available.");
            	}
            }
            if (RPConfig.getString("file-type").equals("yml")){
            	AutoSaveHandler(); 
            }
        }
        catch (Exception e) {
    		e.printStackTrace();
        	if (!RPConfig.getString("file-type").equalsIgnoreCase("mysql")){
                RedProtect.logger.severe("Error enabling RedProtect, plugin will shut down.");
                this.disable();
        	}
        }
    }
    
	private boolean CheckUpdate() {
		Updater updater = null;
		if (RPConfig.getBool("update-check.auto-update")){
			updater = new Updater(this, 87463, JarFile, Updater.UpdateType.DEFAULT, true); // Start Updater but just do a version check
        } else {
        	updater = new Updater(this, 87463, JarFile, Updater.UpdateType.NO_DOWNLOAD, true); // Start Updater but just do a version check
        }		
		Update = updater.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE; // Determine if there is an update ready for us
		UptVersion = updater.getLatestName(); // Get the latest game version
		UptLink = updater.getLatestFileLink(); // Get the latest link		
		return Update;
	}

	private void AutoSaveHandler() {
		Bukkit.getScheduler().cancelTask(taskid);
		if (RPConfig.getInt("flat-file.auto-save-interval-seconds") != 0){
			RedProtect.logger.info("Auto-save Scheduler: Saving "+RPConfig.getString("file-type")+" database every " + RPConfig.getInt("flat-file.auto-save-interval-seconds")/60 + " minutes!");  
			
			taskid = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() { 
				public void run() {
					RedProtect.logger.debug("Auto-save Scheduler: Saving "+RPConfig.getString("file-type")+" database!");
					rm.saveAll();
					} 
				},RPConfig.getInt("flat-file.auto-save-interval-seconds")*20, RPConfig.getInt("flat-file.auto-save-interval-seconds")*20);	
			
		} else {
        	RedProtect.logger.info("Auto-save Scheduler: Disabled");
        }
	}
	
	public void disable() {
        super.setEnabled(false);
    }
    
    private void initVars() throws Exception {
        serv = getServer();
        pdf = getDescription();
        
        ph = new RPPermissionHandler();
        rm = new RegionManager();
    }
    
  //check if plugin GriefPrevention is installed
    private boolean checkGP() {
    	Plugin pGP = Bukkit.getPluginManager().getPlugin("GriefPrevention");
    	if (pGP != null && pGP.isEnabled()){
    		return true;
    	}
		return false;
	}
    
    //check if plugin BossbarAPI is installed
    private boolean checkBM(){
    	Plugin pBM = Bukkit.getPluginManager().getPlugin("BossBarAPI");
    	if (pBM != null && pBM.isEnabled()){
    		return true;
    	}
    	return false;
    }
    
  //check if plugin MyChunk is installed
    private boolean checkMyChunk(){
    	Plugin pMC = Bukkit.getPluginManager().getPlugin("MyChunk");
    	if (pMC != null && pMC.isEnabled()){
    		return true;
    	}
    	return false;
    }
    
  //check if plugin MyPet is installed
    private boolean checkMyPet(){
    	Plugin pMP = Bukkit.getPluginManager().getPlugin("MyPet");
    	if (pMP != null && pMP.isEnabled()){
    		return true;
    	}
    	return false;
    }
    
  //check if plugin McMMo is installed
    private boolean checkMcMMo(){
    	Plugin pMMO = Bukkit.getPluginManager().getPlugin("mcMMO");
    	if (pMMO != null && pMMO.isEnabled()){
    		return true;
    	}
    	return false;
    }
    
  //check if plugin MagicCarpet is installed
    private boolean checkMc(){
    	Plugin pMC = Bukkit.getPluginManager().getPlugin("MagicCarpet");
    	if (pMC != null && pMC.isEnabled()){
    		return true;
    	}
    	return false;
    }
    
    //check if plugin SkillAPI is installed
    private boolean checkSkillAPI(){
    	Plugin pSK = Bukkit.getPluginManager().getPlugin("SkillAPI");
    	if (pSK != null && pSK.isEnabled()){
    		return true;
    	}
    	return false;
    }
    
    //check if plugin Vault is installed
    private boolean checkVault(){
    	Plugin pVT = Bukkit.getPluginManager().getPlugin("Vault");
    	if (pVT != null && pVT.isEnabled()){
    		return true;
    	}
    	return false;
    }
    
    //check if plugin PvPManager is installed
    private boolean checkPvPm(){
    	Plugin pPvp = Bukkit.getPluginManager().getPlugin("PvPManager");
    	if (pPvp != null && pPvp.isEnabled()){
    		return true;
    	}
    	return false;
    }
            
	private boolean checkEss() {
		Plugin pEss = Bukkit.getPluginManager().getPlugin("Essentials");
    	if (pEss != null && pEss.isEnabled()){
    		return true;
    	}
    	return false;
	}
	
	private boolean checkDyn() {
		Plugin pDyn = Bukkit.getPluginManager().getPlugin("dynmap");
    	if (pDyn != null && pDyn.isEnabled()){
    		return true;
    	}
    	return false;
	}
	
	private boolean checkWe() {
		Plugin pWe = Bukkit.getPluginManager().getPlugin("WorldEdit");
    	if (pWe != null && pWe.isEnabled()){
    		return true;
    	}
    	return false;
	}
	
	private boolean checkAWe() {
		Plugin pAWe = Bukkit.getPluginManager().getPlugin("AsyncWorldEdit");
    	if (pAWe != null && pAWe.isEnabled()){
    		return true;
    	}
    	return false;
	}
}