package br.net.fabiozumbi12.RedProtect;

import java.io.File;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class RedProtect extends JavaPlugin {
	static File JarFile = null;
	public static PluginDescriptionFile pdf;
    PluginManager pm;
    public static RedProtect plugin;
    RPGlobalListener gListener;
    RPBlockListener bListener;
    RPPlayerListener pListener;
    RPEntityListener eListener;
    RPWorldListener wListener;
    RPArmorStand aListener;
    MPListener mpListener;
    McMMoListener mcmmoListener;
    RPCommands cManager;
	private int taskid;
	public static boolean Update;
	public static String UptVersion;
	public static String UptLink;
    public static RegionManager rm;
    static RPPermissionHandler ph;
    static RPLogger logger = new RPLogger();
    static String lineSeparator = System.getProperty("line.separator");
    static Server serv;
    static HashMap<Player, Location> firstLocationSelections = new HashMap<Player, Location>();
    static HashMap<Player, Location> secondLocationSelections = new HashMap<Player, Location>();
    static String pathMain = "plugins" + File.separator + "RedProtect" + File.separator;
    static String pathData = String.valueOf(RedProtect.pathMain) + File.separator + "data" + File.separator;
    static String pathConfig = String.valueOf(RedProtect.pathMain) + File.separator + "config.yml";
    static String pathLang = String.valueOf(RedProtect.pathMain) + File.separator + "lang.ini"; 
    static String pathglobalFlags = String.valueOf(RedProtect.pathMain) + File.separator + "globalflags.yml"; 
    static String pathGui = String.valueOf(RedProtect.pathMain) + File.separator + "guiconfig.yml"; 
    static boolean BossBar;
    static boolean MyChunk;
    static boolean MyPet;
    static boolean McMMo;
    static boolean OnlineMode;
    
    
    static enum DROP_TYPE
    {
        drop, 
        remove, 
        keep;
    }
    
    public void onDisable() {
        RedProtect.rm.saveAll();
        RedProtect.rm.unloadAll();
        Bukkit.getScheduler().cancelAllTasks();
        RedProtect.logger.severe(RedProtect.pdf.getFullName() + " disabled.");
    }
    
    public void onEnable() {
        try {
            plugin = this;
            BossBar = checkBM();
            MyChunk = checkMyChunk();
            MyPet = checkMyPet(); 
            McMMo = checkMcMMo();
            JarFile = this.getFile();
            initVars();
            RPUtil.init(this);
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
            if (MyChunk){
            	RedProtect.logger.sucess("MyChunk found. Ready to convert!");
            	RedProtect.logger.warning("Use '/rp mychunkconvert' to start MyChunk conversion (This may cause lag during conversion)");
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
            		RedProtect.logger.sucess("Update available!"); 
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
            RedProtect.logger.severe("Error enabling RedProtect, plugin will shut down.");
            this.disable();
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
					RedProtect.logger.info("Auto-save Scheduler: Saving "+RPConfig.getString("file-type")+" database!");
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
        this.cManager = new RPCommands();
        ph = new RPPermissionHandler();
        rm = new RegionManager();
    }
    
    //check if plugin BossbarAPI is installed
    private boolean checkBM(){
    	Plugin pBM = Bukkit.getPluginManager().getPlugin("BossbarAPI");
    	if (pBM != null){
    		return true;
    	}
    	return false;
    }
    
  //check if plugin MyChunk is installed
    private boolean checkMyChunk(){
    	Plugin pMC = Bukkit.getPluginManager().getPlugin("MyChunk");
    	if (pMC != null){
    		return true;
    	}
    	return false;
    }
    
  //check if plugin MyPet is installed
    private boolean checkMyPet(){
    	Plugin pMP = Bukkit.getPluginManager().getPlugin("MyPet");
    	if (pMP != null){
    		return true;
    	}
    	return false;
    }
    
  //check if plugin McMMo is installed
    private boolean checkMcMMo(){
    	Plugin pMMO = Bukkit.getPluginManager().getPlugin("mcMMO");
    	if (pMMO != null){
    		return true;
    	}
    	return false;
    }
        
}