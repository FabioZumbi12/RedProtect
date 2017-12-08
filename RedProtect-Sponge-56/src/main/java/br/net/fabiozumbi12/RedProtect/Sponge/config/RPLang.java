package br.net.fabiozumbi12.RedProtect.Sponge.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;

import br.net.fabiozumbi12.RedProtect.Sponge.RPUtil;
import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;

public class RPLang {
	
	private static final HashMap<Player, String> DelayedMessage = new HashMap<>();
	static final Properties BaseLang = new Properties();
	public static final Properties Lang = new Properties();
    static String pathLang;
    static String resLang;
	
	public static SortedSet<String> helpStrings(){
		SortedSet<String> values = new TreeSet<>();
		for (Object help:Lang.keySet()){
			if (help.toString().startsWith("cmdmanager.help.")){
				values.add(help.toString().replace("cmdmanager.help.", ""));
			}
		}
		return values;
	}
	
	public static void init() {
		resLang = "lang_" + RedProtect.cfgs.getString("language") + ".properties";
		pathLang = RedProtect.configDir + resLang;
		
		File lang = new File(pathLang);				
		if (!lang.exists()) {
			if (!RedProtect.plugin.getAsset(resLang).isPresent()){	
				resLang = "langEN-US.properties";
				pathLang = RedProtect.configDir + resLang;
			}
								
			//create lang file
			try {
				RedProtect.plugin.getAsset(resLang).get().copyToDirectory(new File(RedProtect.configDir).toPath());
			} catch (IOException e) {
				e.printStackTrace();
			}			
            RedProtect.logger.info("Created config file: " + pathLang);
        }
		
		loadLang();
		loadBaseLang();
		RedProtect.logger.info("Language file loaded - Using: "+ RedProtect.cfgs.getString("language"));	
	}
	
	static void loadBaseLang(){
	    BaseLang.clear();
	    try {
	    	BaseLang.load(RedProtect.plugin.getAsset("langEN-US.properties").get().getUrl().openStream());
	    } catch (Exception e){
	      e.printStackTrace();
	    }
	    updateLang();
	  }
	
	static void loadLang() {
		Lang.clear();
		try {
			FileInputStream fileInput = new FileInputStream(pathLang);
			Reader reader = new InputStreamReader(fileInput, "UTF-8");
			Lang.load(reader);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (Lang.get("_lang.version") != null){
			int langv = Integer.parseInt(Lang.get("_lang.version").toString().replace(".", ""));
			int rpv = Integer.parseInt(RedProtect.plugin.getVersion().get().replace(".", ""));
			if (langv < rpv || langv == 0){
				RedProtect.logger.warning("Your lang file is outdated. Probally need strings updates!");
				RedProtect.logger.warning("Lang file version: "+Lang.get("_lang.version"));
				Lang.put("_lang.version", RedProtect.plugin.getVersion().get());
			}
		}		
	}
	
	static void updateLang(){
	    for (Entry<Object, Object> linha : BaseLang.entrySet()) {	    	
	      if (!Lang.containsKey(linha.getKey())) {
	    	  Lang.put(linha.getKey(), linha.getValue());
	      }
	    }
		if (!Lang.containsKey("_lang.version")){
			Lang.put("_lang.version", RedProtect.plugin.getVersion().get());
    	}
	    try {
	       Lang.store(new OutputStreamWriter(new FileOutputStream(pathLang), "UTF-8"), null);
	    }
	    catch (Exception e)
	    {
	      e.printStackTrace();
	    }
	  }
	
	public static String get(String key){		
		String FMsg = "";

		if (Lang.get(key) == null){
			FMsg = "&c&oMissing language string for &4" + key;
		} else {
			FMsg = Lang.get(key).toString();
		}		
		return FMsg;
	}
	
	public static void sendMessage(CommandSource p, String key){
		if (p instanceof Player && DelayedMessage.containsKey(p) && DelayedMessage.get(p).equals(key)){
			return;
		}
		
		if (Lang.get(key) == null){
			p.sendMessage(RPUtil.toText(get("_redprotect.prefix")+" "+key));
		} else if (get(key).equalsIgnoreCase("")){
			return;
		} else {
			p.sendMessage(RPUtil.toText(get("_redprotect.prefix")+" "+get(key)));
		}		
		
		if (p instanceof Player){
			DelayedMessage.put((Player)p, key);
			Sponge.getScheduler().createSyncExecutor(RedProtect.plugin).schedule(() -> {
                if (DelayedMessage.containsKey(p)){
                    DelayedMessage.remove(p);
                }
                },1, TimeUnit.SECONDS);
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
