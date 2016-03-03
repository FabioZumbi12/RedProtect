package br.net.fabiozumbi12.RedProtect.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.FileUtil;

import br.net.fabiozumbi12.RedProtect.config.RPYaml;
import br.net.fabiozumbi12.RedProtect.RedProtect;

public class RPConfig{
	
	static FileConfiguration configs = new RPYaml();
	static YamlConfiguration gflags = new RPYaml();
	static RPYaml GuiItems = new RPYaml();
	static YamlConfiguration Prots = new RPYaml();
	static RPYaml EconomyConfig = new RPYaml();
	public static List<String> AdminFlags = Arrays.asList("forcepvp","can-fly", "gamemode", "player-damage", "can-hunger", "can-projectiles", "allow-place", "allow-break", "can-pet", "allow-cmds", "deny-cmds", "allow-create-portal", "portal-exit", "portal-enter", "allow-mod", "allow-enter-items", "deny-enter-items", "pvparena", "player-enter-command", "server-enter-command", "player-exit-command", "server-exit-command", "invincible", "effects", "treefarm", "minefarm", "pvp", "sign","enderpearl", "enter", "up-skills", "can-back", "for-sale");	
			
	public static void init(RedProtect plugin) {

    	            File main = new File(RedProtect.pathMain);
    	            File data = new File(RedProtect.pathData);
    	            File gui = new File(RedProtect.pathGui);
    	            File config = new File(RedProtect.pathConfig);
    	            File bvalues = new File(RedProtect.pathBlockValues);
    	            File globalflags = new File(RedProtect.pathglobalFlags);
    	            File protections = new File(RedProtect.protections);
    	            File logs = new File(RedProtect.pathLogs);
    	            
    	            if (!main.exists()) {
    	                main.mkdir();
    	                RedProtect.logger.info("Created folder: " + RedProtect.pathMain);
    	            }
    	            
    	            if (!data.exists()) {
    	                data.mkdir();
    	                RedProtect.logger.info("Created folder: " + RedProtect.pathData);
    	            }    	            
    	                	            
    	            if (!config.exists()) {
    	            	plugin.saveResource("config.yml", false);//create config file    	            	
    	                RedProtect.logger.info("Created config file: " + RedProtect.pathConfig);
    	            } 
    	            
    	            if (!globalflags.exists()) {
    	            	try {
							globalflags.createNewFile();//create globalflags file
	    	                RedProtect.logger.info("Created globalflags file: " + RedProtect.pathglobalFlags);
						} catch (IOException e) {
							e.printStackTrace();
						}
    	            }
    	            
    	            if (!gui.exists()) {
    	            	plugin.saveResource("guiconfig.yml", false);//create guiconfig file    	            	
    	                RedProtect.logger.info("Created guiconfig file: " + RedProtect.pathGui);
    	            }
    	            
    	            if (!bvalues.exists()) {
    	            	plugin.saveResource("economy.yml", false);//create blockvalues file    	            	
    	                RedProtect.logger.info("Created economy file: " + RedProtect.pathBlockValues);
    	            }
    	            
    	            if (!protections.exists()) {
    	            	plugin.saveResource("protections.yml", false);//create protections file    	            	
    	                RedProtect.logger.info("Created protections file: " + RedProtect.protections);
    	            }
    	            
    	            //------------------------------ Add default Values ----------------------------//
    	            FileConfiguration temp = new RPYaml();
    	            try {
    	            	temp.load(config);
					} catch (Exception e) {
						e.printStackTrace();
					} 
    	            
    	            if (!temp.contains("config-version")){
    	            	RedProtect.logger.severe("Old config file detected and copied to 'configBKP.yml'. Remember to check your old config file and set the new as you want!");
    	            	File bkpfile = new File(RedProtect.pathMain + File.separator + "configBKP.yml");
    	            	FileUtil.copy(config, bkpfile);
    	            	plugin.saveResource("config.yml", true);  
    	            	RedProtect.plugin.getConfig();
    	            } else {
    	            	try {
    						RedProtect.plugin.getConfig().load(config);
    					} catch (IOException | InvalidConfigurationException e) {
    						e.printStackTrace();
    					}
    	            }
    	            
    	            configs = inputLoader(plugin.getResource("config.yml"));  
                    for (String key:configs.getKeys(true)){                        	
    	            	configs.set(key, RedProtect.plugin.getConfig().get(key));    	            	   	            	
    	            }                        
                    for (String key:configs.getKeys(false)){    
                    	RedProtect.plugin.getConfig().set(key, configs.get(key));
                    	RedProtect.logger.debug("Set key: "+key);
                    }  
                    
                    //--------------------------------------------------------------------------//
                    
                    RedProtect.logger.info("Server version: " + RedProtect.serv.getBukkitVersion());
                    
                    // check if can enable json support
                    if (getBool("region-settings.region-list.hover-and-click-teleport")){                    	
                    	try {
                    		Class.forName("com.google.gson.JsonParser");
                          	if (RedProtect.serv.getBukkitVersion().contains("1.7")){
                          		RedProtect.plugin.getConfig().set("region-settings.region-list.hover-and-click-teleport", false);
                          		RedProtect.logger.warning("Your server version do not support Hover and Clicking region features, only 1.8.+");
                          	}                           	
                       	} catch(ClassNotFoundException e ) {
                       		RedProtect.plugin.getConfig().set("region-settings.region-list.hover-and-click-teleport", false);
                       		RedProtect.logger.warning("Your server version do not support JSON events, disabling Hover and Clicking region features.");
                       	}
                    }                    
                    
                    //add op to ignore list fro purge
                    if (RedProtect.plugin.getConfig().getStringList("purge.ignore-regions-from-players").size() <= 0){     
                    	List<String> ops = RedProtect.plugin.getConfig().getStringList("purge.ignore-regions-from-players");
                        for (OfflinePlayer play:RedProtect.serv.getOperators()){
                        	ops.add(play.getName());
                        }
                        RedProtect.plugin.getConfig().set("purge.ignore-regions-from-players", ops);
                    }
                    
                    //add op to ignore list fro sell
                    if (RedProtect.plugin.getConfig().getStringList("sell.ignore-regions-from-players").size() <= 0){      
                    	List<String> ops = RedProtect.plugin.getConfig().getStringList("sell.ignore-regions-from-players");
                        for (OfflinePlayer play:RedProtect.serv.getOperators()){
                        	ops.add(play.getName());
                        }
                        RedProtect.plugin.getConfig().set("sell.ignore-regions-from-players", ops);
                    }
                                          	                	            
    	            //add allowed claim worlds to config
    	            if (RedProtect.plugin.getConfig().getStringList("allowed-claim-worlds").get(0).equals("example_world")) {
    	            	List<String> worlds = new ArrayList<String>();
    	            	for (World w:RedProtect.serv.getWorlds()){
    	            		worlds.add(w.getName());
    	            		RedProtect.logger.warning("Added world to claim list " + w.getName());
    	            	}
    	            	worlds.remove("example_world");
    	            	RedProtect.plugin.getConfig().set("allowed-claim-worlds", worlds);
    	            }    
    	            
    	            //add worlds to color list
    	            for (World w:RedProtect.serv.getWorlds()){
	            		if (RedProtect.plugin.getConfig().getString("region-settings.world-colors."+w.getName()) == null) {
	            			if (w.getEnvironment().equals(Environment.NORMAL)){
	            				RedProtect.plugin.getConfig().set("region-settings.world-colors."+w.getName(), "&a&l");			            		
	            			} else
	            			if (w.getEnvironment().equals(Environment.NETHER)){
	            				RedProtect.plugin.getConfig().set("region-settings.world-colors."+w.getName(), "&c&l");			            		
	            			} else
	            			if (w.getEnvironment().equals(Environment.THE_END)){
	            				RedProtect.plugin.getConfig().set("region-settings.world-colors."+w.getName(), "&5&l");			            		
	            			}
	            			RedProtect.logger.warning("Added world to color list " + w.getName());
	            		}	            		
	            	}
    	            
                    /*----------------- Add default config for not updateable configs ------------------*/
                    
                    //update new player flags according version
        			if (RedProtect.plugin.getConfig().getDouble("config-version") != 6.8D){
        				RedProtect.plugin.getConfig().set("config-version", 6.8D);        				
        				
        				List<String> flags = RedProtect.plugin.getConfig().getStringList("flags-configuration.enabled-flags");
        				if (!flags.contains("smart-door")){
        					flags.add("smart-door");
        				}
        				if (!flags.contains("allow-potions")){
        					flags.add("allow-potions");            				
        				}
        				if (!flags.contains("mob-loot")){
        					flags.add("mob-loot");            				
        				}
        				if (!flags.contains("flow-damage")){
        					flags.add("flow-damage");            				
        				}
        				RedProtect.plugin.getConfig().set("flags-configuration.enabled-flags", (List<String>) flags);   
        				RedProtect.logger.warning("Configuration UPDATE! We added new flags to &lflags-configuration > enabled-flags&r!");
        			}
        			
        			/*------------------------------------------------------------------------------------*/
        			
        			//load protections file
        			Prots = updateFile(protections, "protections.yml");    	                
                    
        			/*------------------------------------------------------------------------------------*/
        			
    	            //load and write globalflags to global file
                    gflags = RPYaml.loadConfiguration(globalflags);
                    
                    for (World w:RedProtect.serv.getWorlds()){
                    	gflags.set(w.getName()+".build", gflags.getBoolean(w.getName()+".build", true));
                    	gflags.set(w.getName()+".if-build-false.break-blocks", gflags.getStringList(w.getName()+".if-build-false.break-blocks"));
                    	gflags.set(w.getName()+".if-build-false.place-blocks", gflags.getStringList(w.getName()+".if-build-false.place-blocks"));
                    	gflags.set(w.getName()+".pvp", gflags.getBoolean(w.getName()+".pvp", true));
                    	gflags.set(w.getName()+".interact", gflags.getBoolean(w.getName()+".interact", true));
                    	gflags.set(w.getName()+".use-minecart", gflags.getBoolean(w.getName()+".use-minecart", true));
                    	gflags.set(w.getName()+".entity-block-damage", gflags.getBoolean(w.getName()+".entity-block-damage", false));
                    	gflags.set(w.getName()+".explosion-entity-damage", gflags.getBoolean(w.getName()+".explosion-entity-damage", true));
                    	gflags.set(w.getName()+".fire-block-damage", gflags.getBoolean(w.getName()+".fire-block-damage", false));
                    	gflags.set(w.getName()+".fire-spread", gflags.getBoolean(w.getName()+".fire-spread", false));
                    	gflags.set(w.getName()+".player-hurt-monsters", gflags.getBoolean(w.getName()+".player-hurt-monsters", true));
                    	gflags.set(w.getName()+".player-hurt-passives", gflags.getBoolean(w.getName()+".player-hurt-passives", true));
                    	gflags.set(w.getName()+".spawn-monsters", gflags.getBoolean(w.getName()+".spawn-monsters", true));
                    	gflags.set(w.getName()+".spawn-passives", gflags.getBoolean(w.getName()+".spawn-passives", true));
                    	gflags.set(w.getName()+".remove-entities-not-allowed-to-spawn", gflags.getBoolean(w.getName()+".remove-entities-not-allowed-to-spawn", false));
                    	w.setSpawnFlags(gflags.getBoolean(w.getName()+".spawn-monsters"), gflags.getBoolean(w.getName()+".spawn-passives"));
                    	RedProtect.logger.debug("Spawn Animals: " + w.getAllowAnimals() + " | " + "Spawn Monsters: " + w.getAllowMonsters());
                    }
                    
                    
                    /*------------------------------------------------------------------------------------*/
                    
                    //load and write GuiItems to guiconfig file
                    try {
						GuiItems.load(gui);
					} catch (IOException | InvalidConfigurationException e) {
						e.printStackTrace();
					}
                    
                    RPYaml GuiBase = inputLoader(plugin.getResource("guiconfig.yml")); 
                    
                    GuiItems.set("gui-strings.value", GuiItems.getString("gui-strings.value", "&bValue: "));   
                    GuiItems.set("gui-strings.true", GuiItems.getString("gui-strings.true", "&atrue")); 
                    GuiItems.set("gui-strings.false", GuiItems.getString("gui-strings.false", "&cfalse")); 
                    GuiItems.set("gui-strings.separator", GuiItems.getString("gui-strings.separator", "&7|")); 
                    
                    GuiItems.set("gui-separator.material", GuiItems.getString("gui-separator.material", "THIN_GLASS")); 
                    GuiItems.set("gui-separator.data", GuiItems.getInt("gui-separator.data", 0)); 
                    
                    for (String key:getDefFlagsValues().keySet()){
                    	GuiItems.set("gui-flags."+key+".slot", GuiItems.get("gui-flags."+key+".slot", GuiBase.get("gui-flags."+key+".slot", getDefFlagsValues().size())));
                    	GuiItems.set("gui-flags."+key+".material", GuiItems.get("gui-flags."+key+".material", GuiBase.get("gui-flags."+key+".material", "GOLDEN_APPLE")));
                    	GuiItems.set("gui-flags."+key+".name", GuiItems.get("gui-flags."+key+".name", GuiBase.get("gui-flags."+key+".name", "&e"+key)));                    	
                    	GuiItems.set("gui-flags."+key+".description", GuiItems.get("gui-flags."+key+".description", GuiBase.get("gui-flags."+key+".description", "&bDescription: &2Add a flag description here.")));
                    	GuiItems.set("gui-flags."+key+".description1", GuiItems.get("gui-flags."+key+".description1", GuiBase.get("gui-flags."+key+".description1", "")));
                    	GuiItems.set("gui-flags."+key+".description2", GuiItems.get("gui-flags."+key+".description2", GuiBase.get("gui-flags."+key+".description2", "")));
                    }
                    
                    /*------------------------------------------------------------------------------------*/
                    
                    //load blockvalues file
                    try {
                    	EconomyConfig.load(bvalues);
					} catch (IOException | InvalidConfigurationException e) {
						e.printStackTrace();
					}
                    
                    RPYaml tempEco = inputLoader(plugin.getResource("economy.yml"));
                    for (String key:tempEco.getKeys(false)){
                    	if (EconomyConfig.get(key) == null){
                    		EconomyConfig.set(key, tempEco.get(key));
                    	}
                    }
                    
                    for (Material mat:Material.values()){
                    	if (EconomyConfig.getString("items.values."+mat.name()) == null){
                    		EconomyConfig.set("items.values."+mat.name(), 0.0);                		
                    	}
                    }                    
                    for (Enchantment ench:Enchantment.values()){
                    	if (EconomyConfig.getString("enchantments.values."+ench.getName()) == null){
                    		EconomyConfig.set("enchantments.values."+ench.getName(), 0.0);                		
                    	}
                    }
                    
                    
                    //////////////////////
                    /*------------------------------------------------------------------------------------*/
                    
        			String v = RedProtect.serv.getBukkitVersion();
        			if (RedProtect.plugin.getConfig().getString("notify.region-enter-mode").equalsIgnoreCase("TITLE") && (v == null || !v.contains("1.8"))) {
        				RedProtect.plugin.getConfig().set("notify.region-enter-mode", "CHAT");
	                    RedProtect.logger.warning("Title notifications is not suported on servers not running 1.8! Defaulting to CHAT.");
        			}
        			
        			//create logs folder
        			if(getBool("log-actions") && !logs.exists()){
        				logs.mkdir();
    	                RedProtect.logger.info("Created folder: " + RedProtect.pathLogs);        	    		
        	    	}
        			
        			save();        			
    	            RedProtect.logger.info("All configurations loaded!");
	}
    
    public static Boolean getGlobalFlag(String key){		
		return gflags.getBoolean(key);
	}
    
    public static List<String> getGlobalFlagList(String key){		
		return gflags.getStringList(key);
	}
    
    public static ItemStack getGuiItemStack(String key){
    	RedProtect.logger.debug("Gui Material to get: " + key);
    	RedProtect.logger.debug("Result: " + GuiItems.getString("gui-flags."+key+".material"));
    	return new ItemStack(Material.getMaterial(GuiItems.getString("gui-flags."+key+".material")));
    }
    
    public static String getGuiFlagString(String flag, String option){
    	if (GuiItems.getString("gui-flags."+flag+"."+option) == null){
    		return "";
    	}
    	return ChatColor.translateAlternateColorCodes('&', GuiItems.getString("gui-flags."+flag+"."+option));
    }
    
    public static String getGuiString(String string) {
		return ChatColor.translateAlternateColorCodes('&', GuiItems.getString("gui-strings."+string));
	}
    
    public static int getGuiSlot(String flag) {
		return GuiItems.getInt("gui-flags."+flag+".slot");
	}
    
    public static void setGuiSlot(/*String mat, */String flag, int slot) {
		GuiItems.set("gui-flags."+flag+".slot", slot);
		//GuiItems.set("gui-flags."+flag+".material", mat);
		
	}
    
    public static ItemStack getGuiSeparator() {
    	ItemStack separator = new ItemStack(Material.getMaterial(GuiItems.getString("gui-separator.material")), 1, (short)GuiItems.getInt("gui-separator.data"));
    	ItemMeta meta = separator.getItemMeta();
    	meta.setDisplayName(getGuiString("separator"));
    	meta.setLore(Arrays.asList("", getGuiString("separator")));
    	separator.setItemMeta(meta);
		return separator;
	}
    
    public static int getGuiMaxSlot() {
    	SortedSet<Integer> slots = new TreeSet<Integer>(new ArrayList<Integer>());
    	for (String key:GuiItems.getKeys(true)){
    		if (key.contains(".slot")){
    			slots.add(GuiItems.getInt(key));
    		}    		
    	}
		return Collections.max(slots);
	}
    
    public static Boolean getBool(String key){		
		return RedProtect.plugin.getConfig().getBoolean(key, false);
	}
    
    public static void setConfig(String key, Object value){
    	RedProtect.plugin.getConfig().set(key, value);
    }
    
    public static HashMap<String, Object> getDefFlagsValues(){
    	HashMap<String,Object> flags = new HashMap<String,Object>();
    	for (String flag:RedProtect.plugin.getConfig().getValues(true).keySet()){
    		if (flag.contains("flags.") && isFlagEnabled(flag.replace("flags.", ""))){
    			if (flag.replace("flags.", "").equals("pvp") && !RedProtect.plugin.getConfig().getStringList("flags-configuration.enabled-flags").contains("pvp")){
    				continue;
    			}
    			
    			flags.put(flag.replace("flags.", ""), RedProtect.plugin.getConfig().get(flag));
    			
    			/*
    			if (RedProtect.plugin.getConfig().get(flag) == null){
    				flags.put(flag.replace("flags.", ""), " ");
    			} else {
    				flags.put(flag.replace("flags.", ""), RedProtect.plugin.getConfig().get(flag));
    			}*/		
    		}
    	}    	
		return flags;
	}
    
    public static boolean isFlagEnabled(String flag){    	
    	return RedProtect.plugin.getConfig().getStringList("flags-configuration.enabled-flags").contains(flag) || AdminFlags.contains(flag);
    }
    
    public static SortedSet<String> getDefFlags(){
    	SortedSet<String> values = new TreeSet<String>(getDefFlagsValues().keySet());
		return values;    	
    }
    
    public static String getString(String key){		
		return RedProtect.plugin.getConfig().getString(key);
	}
    
    public static Integer getInt(String key){		
		return RedProtect.plugin.getConfig().getInt(key);
	}
    
    public static List<String> getStringList(String key){		
		return RedProtect.plugin.getConfig().getStringList(key);
	}
    
    public static Material getMaterial(String key){
    	return Material.getMaterial(RedProtect.plugin.getConfig().getString(key));
    }
    
    public static void save(){
    	File globalflags = new File(RedProtect.pathglobalFlags);  
    	File guiconfig = new File(RedProtect.pathGui);
    	File blockvalues = new File(RedProtect.pathBlockValues);
    	File protections = new File(RedProtect.protections);
    	try {
			RedProtect.plugin.saveConfig();
			gflags.save(globalflags);
			GuiItems.save(guiconfig);
			EconomyConfig.save(blockvalues);
			Prots.save(protections);
		} catch (IOException e) {
			RedProtect.logger.severe("Problems during save file:");
			e.printStackTrace();
		}
    }
    
    public static void saveGui(){ 
    	File guiconfig = new File(RedProtect.pathGui);
    	try {
			GuiItems.save(guiconfig);
		} catch (IOException e) {
			RedProtect.logger.severe("Problems during save gui file:");
			e.printStackTrace();
		}
    }    
    
    private static RPYaml inputLoader(InputStream inp) {
		RPYaml file = new RPYaml();
		try {
			file.load(new InputStreamReader(inp, StandardCharsets.UTF_8));
			inp.close();
		} catch (Exception e) {
			e.printStackTrace();
		} 		
		return file;
	}
	
    public static boolean isAllowedWorld(Player p) {
		return RedProtect.plugin.getConfig().getStringList("allowed-claim-worlds").contains(p.getWorld().getName()) || p.hasPermission("redprotect.admin");
	}

	public static SortedSet<String> getAllFlags() {
		SortedSet<String> values = new TreeSet<String>(getDefFlagsValues().keySet());
		values.addAll(new TreeSet<String>(AdminFlags));
		return values;
	}

	public static int getProtInt(String key){
		return Prots.getInt(key);
	}
	
	public static boolean getProtBool(String key){
		return Prots.getBoolean(key);
	}
	
	public static List<String> getProtStringList(String key){
		return Prots.getStringList(key);
	}
	
    public static boolean containsProtKey(String key){		
		return Prots.contains(key);
	}
    
	public static String getProtString(String key){
		return Prots.getString(key);
	}
	
	public static String getProtMsg(String key){
		return ChatColor.translateAlternateColorCodes('&',Prots.getString(key));
	}
		
	public static int getBlockCost(String itemName) {
		return EconomyConfig.getInt("items.values."+itemName);
	}
	
	public static int getEnchantCost(String enchantment) {
		return EconomyConfig.getInt("enchantments.values."+enchantment);
	}
	
	public static String getEcoString(String key){
		return EconomyConfig.getString(key);
	}
	
	public static Integer getEcoInt(String key){
		return EconomyConfig.getInt(key);
	}

	public static boolean getEcoBool(String key) {
		return EconomyConfig.getBoolean(key);
	}
	
	private static RPYaml updateFile(File saved, String filename){
		RPYaml finalyml = new RPYaml();    			
        try {
        	finalyml.load(saved);
		} catch (Exception e) {
			e.printStackTrace();
		} 
                			
		RPYaml tempProts = inputLoader(RedProtect.plugin.getResource(filename));  
        for (String key:tempProts.getKeys(true)){    
        	Object obj = tempProts.get(key);
        	if (finalyml.get(key) != null){
        		obj = finalyml.get(key);
        	}
        	finalyml.set(key, obj);    	            	   	            	
        }  
        return finalyml;
	}
}
   
