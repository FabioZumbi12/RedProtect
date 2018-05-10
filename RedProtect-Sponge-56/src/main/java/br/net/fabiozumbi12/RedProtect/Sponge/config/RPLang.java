package br.net.fabiozumbi12.RedProtect.Sponge.config;

import br.net.fabiozumbi12.RedProtect.Sponge.RPUtil;
import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;

import java.io.*;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

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
		resLang = "lang" + RedProtect.get().cfgs.root().language + ".properties";
		pathLang = RedProtect.get().configDir + File.separator + resLang;
		
		File lang = new File(pathLang);
		if (!lang.exists()) {
			if (!RedProtect.get().container.getAsset(resLang).isPresent()){
				resLang = "langEN-US.properties";
				pathLang = RedProtect.get().configDir + File.separator + resLang;
			}
								
			//create lang file
			try {
				RedProtect.get().container.getAsset(resLang).get().copyToDirectory(RedProtect.get().configDir.toPath());
			} catch (IOException e) {
				e.printStackTrace();
			}			
            RedProtect.get().logger.info("Created config file: " + pathLang);
        }
		
		loadLang();
		loadBaseLang();
		RedProtect.get().logger.info("Language file loaded - Using: "+ RedProtect.get().cfgs.root().language);
	}
	
	static void loadBaseLang(){
	    BaseLang.clear();
	    try {
	    	BaseLang.load(RedProtect.get().container.getAsset("langEN-US.properties").get().getUrl().openStream());
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
			int rpv = Integer.parseInt(RedProtect.get().container.getVersion().get().replace(".", ""));
			if (langv < rpv || langv == 0){
				RedProtect.get().logger.warning("Your lang file is outdated. Probally need strings updates!");
				RedProtect.get().logger.warning("Lang file version: "+Lang.get("_lang.version"));
				Lang.put("_lang.version", RedProtect.get().container.getVersion().get());
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
			Lang.put("_lang.version", RedProtect.get().container.getVersion().get());
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
			Sponge.getScheduler().createSyncExecutor(RedProtect.get().container).schedule(() -> {
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
