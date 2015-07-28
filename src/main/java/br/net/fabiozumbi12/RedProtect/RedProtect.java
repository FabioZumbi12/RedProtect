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
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import br.net.fabiozumbi12.RedProtect.hooks.MPListener;
import br.net.fabiozumbi12.RedProtect.hooks.McMMoListener;
import br.net.fabiozumbi12.RedProtect.hooks.SkillAPIListener;

public class RedProtect extends JavaPlugin {
	static File JarFile = null;
	public static PluginDescriptionFile pdf;
    private PluginManager pm;
    public static RedProtect plugin;
    private RPGlobalListener gListener;
    private RPBlockListener bListener;
    private RPPlayerListener pListener;
    private RPEntityListener eListener;
    private RPWorldListener wListener;
    private RPArmorStand aListener;
    private MPListener mpListener;
    private McMMoListener mcmmoListener;
    private SkillAPIListener skilstener;
    private RPCommands cManager;
	private int taskid;
	public static boolean Update;
	public static String UptVersion;
	public static String UptLink;
    public static RegionManager rm;
    public static List<String> changeWait = new ArrayList<String>();
    public static List<String> tpWait = new ArrayList<String>();
    static RPPermissionHandler ph;
    public static RPLogger logger = new RPLogger();
    static Server serv;    
    static HashMap<Player, Location> firstLocationSelections = new HashMap<Player, Location>();
    static HashMap<Player, Location> secondLocationSelections = new HashMap<Player, Location>();
    static String pathMain = "plugins" + File.separator + "RedProtect" + File.separator;
    static String pathData = String.valueOf(RedProtect.pathMain) + File.separator + "data" + File.separator;
    static String pathConfig = String.valueOf(RedProtect.pathMain) + File.separator + "config.yml";
    static String pathLang = String.valueOf(RedProtect.pathMain) + File.separator + "lang.ini"; 
    static String pathglobalFlags = String.valueOf(RedProtect.pathMain) + File.separator + "globalflags.yml"; 
    static String pathGui = String.valueOf(RedProtect.pathMain) + File.separator + "guiconfig.yml"; 
    static String pathBlockValues = String.valueOf(RedProtect.pathMain) + File.separator + "economy.yml";;
    static boolean BossBar;
    static boolean MyChunk;
    static boolean MyPet;
    static boolean McMMo;
    static boolean OnlineMode;
	static boolean Mc;
	static boolean SkillAPI;
	static boolean Vault;
	static boolean PvPm;
	public static PlayerHandler PvPmanager;
	public static Economy econ;
    
    static enum DROP_TYPE
    {
        drop, 
        remove, 
        keep;
    }
    
    public void onDisable() {
        RedProtect.rm.saveAll();
        RedProtect.rm.unloadAll();
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
            JarFile = this.getFile();
            initVars();
            RPConfig.init(this);
            RPLang.init(this);
            rm.loadAll();
            OnlineMode = serv.getOnlineMode();
            this.pm.registerEvents(this.gListener, this);
            this.pm.registerEvents(this.bListener, this);
            this.pm.registerEvents(this.pListener, this);
            this.pm.registerEvents(this.eListener, this);
            this.pm.registerEvents(this.wListener, this);            
            
            String v = RedProtect.serv.getBukkitVersion();
            if (v.contains("1.8")){
            	this.pm.registerEvents(this.aListener, this);
            }  
            getCommand("RedProtect").setExecutor(this.cManager);
            
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
            
            if (BossBar){
            	RedProtect.logger.info("BossbarAPI found. Hooked.");
            }
            if (MyPet){
            	this.pm.registerEvents(this.mpListener, this);
            	RedProtect.logger.info("MyPet found. Hooked.");
            }
            if (McMMo){
            	this.pm.registerEvents(this.mcmmoListener, this);
            	RedProtect.logger.info("McMMo found. Hooked.");
            }
            if (SkillAPI){
            	this.pm.registerEvents(this.skilstener, this);
            	RedProtect.logger.info("SkillAPI found. Hooked.");
            }
            if (MyChunk){
            	RedProtect.logger.sucess("MyChunk found. Ready to convert!");
            	RedProtect.logger.warning("Use '/rp mychunkconvert' to start MyChunk conversion (This may cause lag during conversion)");
            } 
            if (Mc){
            	RedProtect.logger.info("MagicCarpet found. Hooked.");
            }
            
            if (!RPConfig.getString("file-type").equalsIgnoreCase("mysql")){
            	RPUtil.ReadAllDB(RedProtect.rm.getAllRegions());
        	} else {
        		RedProtect.logger.info("Theres " + RedProtect.rm.getTotalRegionsNum() + " regions on (" + RPConfig.getString("file-type") + ") database!");        		
        	}
            
            
            if (RPConfig.getString("file-type").equals("oosgz")){
            	RPUtil.backup = 0;
            	RPUtil.backup();
                RPUtil.oosTOyml();
                RedProtect.plugin.getConfig().set("file-type","yml");
                RPConfig.save();
                RedProtect.logger.warning("The database type 'oosgz' is deprecated. Now use only yml ow Mysql!");
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
    
    void initVars() throws Exception {
        serv = getServer();
        pdf = getDescription();
        this.pm = serv.getPluginManager();
        this.gListener = new RPGlobalListener(this);
        this.bListener = new RPBlockListener(this);
        this.pListener = new RPPlayerListener(this);
        this.eListener = new RPEntityListener(this);
        this.wListener = new RPWorldListener(this);        
        String v = RedProtect.serv.getBukkitVersion();
        if (v.contains("1.8")){
        	this.aListener = new RPArmorStand(this);
        }
        if (MyPet){
        	this.mpListener = new MPListener(this);
        }
        if (McMMo){
        	this.mcmmoListener = new McMMoListener(this);
        }
        if (SkillAPI){
        	this.skilstener = new SkillAPIListener(this);
        }
        this.cManager = new RPCommands();
        ph = new RPPermissionHandler();
        rm = new RegionManager();
    }
    
    //check if plugin BossbarAPI is installed
    private boolean checkBM(){
    	Plugin pBM = Bukkit.getPluginManager().getPlugin("BossbarAPI");
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
            
}