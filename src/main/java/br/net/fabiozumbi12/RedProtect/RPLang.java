package br.net.fabiozumbi12.RedProtect;

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
import org.bukkit.entity.Player;

public class RPLang {
	
	static HashMap<String, String> BaseLang = new HashMap<String, String>();
	static HashMap<String, String> Lang = new HashMap<String, String>();
	
	static SortedSet<String> helpStrings(){
		SortedSet<String> values = new TreeSet<String>();
		for (String help:Lang.keySet()){
			if (help.startsWith("cmdmanager.help.")){
				values.add(help.replace("cmdmanager.help.", ""));
			}
		}
		return values;
	}
	
	static void init(RedProtect plugin) {
		File lang = new File(RedProtect.pathLang);
		if (!lang.exists()) {
			plugin.saveResource("lang.ini", false);//create lang file
            RedProtect.logger.info("Created config file: " + RedProtect.pathLang);
        }		
		loadLang();
		loadBaseLang();
		RedProtect.logger.info("Language file loaded - Using: "+ Lang.get("_lang.code"));	
	}
	
	static void loadBaseLang(){
	    BaseLang.clear();
	    Properties properties = new Properties();
	    try
	    {
	      InputStream fileInput = RedProtect.class.getClassLoader().getResourceAsStream("lang.ini");
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
			FileInputStream fileInput = new FileInputStream(RedProtect.pathLang);
			Reader reader = new InputStreamReader(fileInput, "UTF-8");
			properties.load(reader);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		for (Object key : properties.keySet()) {
			if (!(key instanceof String)) {
				continue;
			}			
			Lang.put((String) key, properties.getProperty((String) key));
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
	      FileReader reader = new FileReader(RedProtect.pathLang);
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
	      properties.store(new OutputStreamWriter(new FileOutputStream(RedProtect.pathLang), "UTF-8"), null);
	    }
	    catch (Exception e)
	    {
	      e.printStackTrace();
	    }
	  }
	

	public static String get(String key){		
		String FMsg = "";

		if (Lang.get(key) == null){
			FMsg = "§c§oMissing language string for "+ ChatColor.GOLD + key;
		} else {
			FMsg = Lang.get(key);
		}

		FMsg = FMsg.replaceAll("(?i)&([a-f0-9k-or])", "§$1");
		
		return FMsg;
	}
	
	static void sendMessage(Player p, String message){
		p.sendMessage(get("_redprotect.prefix")+ " " + message);
	}
}
