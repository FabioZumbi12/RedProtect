package br.net.fabiozumbi12.RedProtect;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.FileUtil;

import br.net.fabiozumbi12.RedProtect.RedProtect.DROP_TYPE;

public class RPConfig{
	
	static HashMap<String, DROP_TYPE> DropType = new HashMap<String, DROP_TYPE>();
	static FileConfiguration configs = new RPYaml();
	static YamlConfiguration gflags = new RPYaml();
	static YamlConfiguration GuiItems = new RPYaml();
	public static List<String> AdminFlags = Arrays.asList("player-enter-command", "server-enter-command", "player-exit-command", "server-exit-command", "invincible", "effects", "treefarm", "minefarm", "pvp", "sign","enderpearl", "enter", "mcmmo", "death-back");	
			
	static void init(RedProtect plugin) {

    	            File main = new File(RedProtect.pathMain);
    	            File data = new File(RedProtect.pathData);
    	            File gui = new File(RedProtect.pathGui);
    	            File config = new File(RedProtect.pathConfig);
    	            File globalflags = new File(RedProtect.pathglobalFlags);

    	            
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
							globalflags.createNewFile();//create config file
	    	                RedProtect.logger.info("Created globalflags file: " + RedProtect.pathglobalFlags);
						} catch (IOException e) {
							e.printStackTrace();
						}
    	            }
    	            
    	            if (!gui.exists()) {
    	            	plugin.saveResource("guiconfig.yml", false);//create config file    	            	
    	                RedProtect.logger.info("Created guiconfig file: " + RedProtect.pathGui);
    	            }
    	            
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
    	            } 
    	            	
    	            RedProtect.plugin.getConfig();  
    	            
    	            configs = inputLoader(plugin.getResource("config.yml"));  
                    for (String key:configs.getKeys(true)){
    	            	configs.set(key, RedProtect.plugin.getConfig().get(key));    	            	   	            	
    	            }                        
                    for (String key:configs.getKeys(false)){
                    	RedProtect.plugin.getConfig().set(key, configs.get(key));
                    	RedProtect.logger.debug("Set key: "+key);
                    }  
                    
                    if (RedProtect.plugin.getConfig().getStringList("purge.ignore-regions-from-players").size() <= 0){
                    	List<String> ops = RedProtect.plugin.getConfig().getStringList("purge.ignore-regions-from-players");
                        for (OfflinePlayer play:RedProtect.serv.getOperators()){
                        	ops.add(play.getName());
                        }                                                     
                        RedProtect.plugin.getConfig().set("purge.ignore-regions-from-players", ops);
                    }
                    
    	            if (RedProtect.plugin.getConfig().getString("region-settings.drop-type") != null) {
    	                if (RedProtect.plugin.getConfig().getString("region-settings.drop-type").equalsIgnoreCase("keep")) {
    	                    DropType.put("drop-type", DROP_TYPE.keep);
    	                }
    	                else if (RedProtect.plugin.getConfig().getString("region-settings.drop-type").equalsIgnoreCase("remove")) {
    	                	DropType.put("drop-type", DROP_TYPE.remove);
    	                }
    	                else if (RedProtect.plugin.getConfig().getString("region-settings.drop-type").equalsIgnoreCase("drop")) {
    	                	DropType.put("drop-type", DROP_TYPE.drop);
    	                }
    	                else {
    	                	DropType.put("drop-type", DROP_TYPE.keep);
    	                    RedProtect.logger.warning("There is an error in your configuration: drop-type! Defaulting to 'Keep'.");
    	                }
    	            } 
    	                	            
    	            //add allowed claim worlds to config
    	            if (RedProtect.plugin.getConfig().getStringList("allowed-claim-worlds").get(0).equals("example_world")) {
    	            	List<String> worlds = new ArrayList<String>();
    	            	for (World w:RedProtect.serv.getWorlds()){
    	            		worlds.add(w.getName());
    	            		RedProtect.logger.warning("Added world " + w.getName());
    	            	}
    	            	RedProtect.plugin.getConfig().set("allowed-claim-worlds", worlds);
    	            }
    	            
    	            //load and write globalflags to global file
                    gflags = RPYaml.loadConfiguration(globalflags);
                    
                    for (World w:RedProtect.serv.getWorlds()){
                    	gflags.set(w.getName()+".build", gflags.getBoolean(w.getName()+".build", true));
                    	gflags.set(w.getName()+".pvp", gflags.getBoolean(w.getName()+".pvp", true));
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
                    
                  //load and write GuiItems to guiconfig file
                    GuiItems = RPYaml.loadConfiguration(gui);
                    
                    for (String key:getDefFlagsValues().keySet()){
                    	GuiItems.set("gui-flags."+key+".material", GuiItems.get("gui-flags."+key+".material", "GOLDEN_APPLE"));
                    	GuiItems.set("gui-flags."+key+".name", GuiItems.get("gui-flags."+key+".name", "&e"+key));
                    	GuiItems.set("gui-flags."+key+".value-string", GuiItems.get("gui-flags."+key+".value-string", "&bValue: &7"));
                    	GuiItems.set("gui-flags."+key+".description", GuiItems.get("gui-flags."+key+".description", "&bDescription: &2Add a flag description here."));
                    	GuiItems.set("gui-flags."+key+".description1", GuiItems.get("gui-flags."+key+".description1", ""));
                    	GuiItems.set("gui-flags."+key+".description2", GuiItems.get("gui-flags."+key+".description2", ""));
                    }
                    
                    
        			String v = RedProtect.serv.getBukkitVersion();
        			if (RedProtect.plugin.getConfig().getString("notify.region-enter-mode").equalsIgnoreCase("TITLE") && (v == null || !v.contains("1.8"))) {
        				RedProtect.plugin.getConfig().set("notify.region-enter-mode", "CHAT");
	                    RedProtect.logger.warning("Title notifications is not suported on servers not running 1.8! Defaulting to CHAT.");
        			}

        			save();        			
    	            RedProtect.logger.info("All configurations loaded!");
	}
    
    public static Boolean getGlobalFlag(String key){		
		return gflags.getBoolean(key);
	}
    
    public static ItemStack getGuiItemStack(String key){
    	RedProtect.logger.debug("Gui Material to get: " + key);
    	RedProtect.logger.debug("Result: " + GuiItems.getString("gui-flags."+key+".material"));
    	return new ItemStack(Material.getMaterial(GuiItems.getString("gui-flags."+key+".material")));
    }
    
    public static String getGuiString(String flag, String option){
    	if (GuiItems.getString("gui-flags."+flag+"."+option) == null){
    		return "";
    	}
    	return GuiItems.getString("gui-flags."+flag+"."+option).replaceAll("(?i)&([a-f0-9k-or])", "§$1");
    }
    
    public static Boolean getBool(String key){		
		return RedProtect.plugin.getConfig().getBoolean(key, false);
	}
    
    public static HashMap<String, Object> getDefFlagsValues(){
    	HashMap<String,Object> flags = new HashMap<String,Object>();
    	for (String flag:RedProtect.plugin.getConfig().getValues(true).keySet()){
    		if (flag.contains("flags.")){
    			flags.put(flag.replace("flags", "").replace(".", ""), RedProtect.plugin.getConfig().get(flag));
    		}
    	}    	
		return flags;
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
    
    public static DROP_TYPE getDropType(String key){		
		return DropType.get(key);
	}
    
    public static void save(){
    	File globalflags = new File(RedProtect.pathglobalFlags);  
    	File guiconfig = new File(RedProtect.pathGui);
    	try {
			RedProtect.plugin.saveConfig();
			gflags.save(globalflags);
			GuiItems.save(guiconfig);
		} catch (IOException e) {
			RedProtect.logger.severe("Problems during save file:");
			e.printStackTrace();
		}
    }
    
    
    private static RPYaml inputLoader(InputStream inp) {
		RPYaml file = new RPYaml();
		try {
			file.load(new InputStreamReader(inp, StandardCharsets.UTF_8));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		} catch (InvalidConfigurationException e3) {
			e3.printStackTrace();
		}
		try {
			inp.close();
		} catch (IOException e2) {
			e2.printStackTrace();
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
    
}
   
