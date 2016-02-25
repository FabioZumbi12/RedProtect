package br.net.fabiozumbi12.RedProtect.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import br.net.fabiozumbi12.RedProtect.RedProtect;

public class RPLang {
	
	static HashMap<String, String> BaseLang = new HashMap<String, String>();
	public static HashMap<String, String> Lang = new HashMap<String, String>();
	//static List<String> langString = new ArrayList<String>();
    static String pathLang; 
    static String resLang; 
    static RedProtect plugin;
	
	public static SortedSet<String> helpStrings(){
		SortedSet<String> values = new TreeSet<String>();
		for (String help:Lang.keySet()){
			if (help.startsWith("cmdmanager.help.")){
				values.add(help.replace("cmdmanager.help.", ""));
			}
		}
		return values;
	}
	
	public static void init(RedProtect plugin) {
		
		RPLang.plugin = plugin;
		pathLang = String.valueOf(RedProtect.pathMain) + File.separator + "lang" + RPConfig.getString("language") + ".ini";
		resLang = "lang" + RPConfig.getString("language") + ".ini";
		
		File lang = new File(pathLang);			
		if (!lang.exists()) {
			if (plugin.getResource(resLang) == null){		
				RPConfig.setConfig("language", "EN-US");
				RPConfig.save();
				resLang = "langEN-US.ini";	
				pathLang = String.valueOf(RedProtect.pathMain) + File.separator + "langEN-US.ini";
			}
			plugin.saveResource(resLang, false);//create lang file
            RedProtect.logger.info("Created config file: " + pathLang);
        }
		
		loadLang();
		loadBaseLang();
		RedProtect.logger.info("Language file loaded - Using: "+ RPConfig.getString("language"));	
	}
	
	static void loadBaseLang(){
	    BaseLang.clear();
	    Properties properties = new Properties();
	    try {
	    	InputStream fileInput = RedProtect.class.getClassLoader().getResourceAsStream("langEN-US.ini");	      
	        Reader reader = new InputStreamReader(fileInput, "UTF-8");
	        properties.load(reader);
	    }
	    catch (Exception e)
	    {
	      e.printStackTrace();
	    }
	    for (Object key : properties.keySet()) {
	      if ((key instanceof String)) {
	    	  BaseLang.put((String)key, properties.getProperty((String)key));
	      }
	    }
	    updateLang();
	  }
	
	static void loadLang() {
		Lang.clear();
		Properties properties = new Properties();
		try {
			FileInputStream fileInput = new FileInputStream(pathLang);
			Reader reader = new InputStreamReader(fileInput, "UTF-8");
			properties.load(reader);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		for (Object key : properties.keySet()) {
			if (!(key instanceof String)) {
				continue;
			}			
			String keylang = properties.getProperty((String) key);
			Lang.put((String) key, keylang.replace("owner", "leader"));
		}		
		
		if (Lang.get("_lang.version") != null){
			int langv = Integer.parseInt(Lang.get("_lang.version").replace(".", ""));
			int rpv = Integer.parseInt(RedProtect.pdf.getVersion().replace(".", ""));
			if (langv < rpv || langv == 0){
				RedProtect.logger.warning("Your lang file is outdated. Probally need strings updates!");
				RedProtect.logger.warning("Lang file version: "+Lang.get("_lang.version"));
				Lang.put("_lang.version", RedProtect.pdf.getVersion());
			}
		}		
	}
	
	static void updateLang(){
	    for (String linha : BaseLang.keySet()) {	    	
	      if (!Lang.containsKey(linha)) {
	    	  Lang.put(linha, BaseLang.get(linha));
	      }
	    }
		if (!Lang.containsKey("_lang.version")){
			Lang.put("_lang.version", RedProtect.pdf.getVersion());
    	}
	    try {
	      Properties properties = new Properties()
	      {
	        private static final long serialVersionUID = 1L;	        
	        public synchronized Enumeration<Object> keys(){
	          return Collections.enumeration(new TreeSet<Object>(super.keySet()));
	        }
	      };
	      FileReader reader = new FileReader(pathLang);
	      BufferedReader bufferedReader = new BufferedReader(reader);
	      properties.load(bufferedReader);
	      bufferedReader.close();
	      reader.close();
	      properties.clear();
	      for (String key : Lang.keySet()) {
	        if ((key instanceof String)) {
	          properties.put(key, Lang.get(key));
	        }
	      }
	      properties.store(new OutputStreamWriter(new FileOutputStream(pathLang), "UTF-8"), null);
	    }
	    catch (Exception e)
	    {
	      e.printStackTrace();
	    }
	  }
	
	public static String get(String key){		
		String FMsg = "";

		if (Lang.get(key) == null){
			FMsg = "&c&oMissing language string for "+ ChatColor.GOLD + key;
		} else {
			FMsg = Lang.get(key);
		}
				
		FMsg = ChatColor.translateAlternateColorCodes('&', FMsg);
		
		return FMsg;
	}
	
	public static void sendMessage(Player p, String key){
		if (Lang.get(key) == null){
			p.sendMessage(get("_redprotect.prefix")+ " " + ChatColor.translateAlternateColorCodes('&', key));
		} else if (get(key).equalsIgnoreCase("")){
			return;
		} else {
			p.sendMessage(get("_redprotect.prefix")+ " " + get(key));
		}		
	}
	
	public static void sendMessage(CommandSender p, String key){
		if (Lang.get(key) == null){
			p.sendMessage(get("_redprotect.prefix")+ " " + ChatColor.translateAlternateColorCodes('&', key));
		} else if (get(key).equalsIgnoreCase("")){
			return;
		} else {
			p.sendMessage(get("_redprotect.prefix")+ " " + get(key));
		}		
	}
	
	public static String translBool(String bool){		
		return get("region."+bool);
	}
	
	public static String translBool(Boolean bool){		
		return get("region."+bool.toString());
	}
	
	public static boolean containsValue(String value){
		return Lang.containsValue(value);
	}
}
