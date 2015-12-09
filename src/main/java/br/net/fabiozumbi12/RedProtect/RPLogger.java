package br.net.fabiozumbi12.RedProtect;

import java.io.File;
import java.util.SortedMap;
import java.util.TreeMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class RPLogger{
	private SortedMap<Integer,String> MainLog = new TreeMap<Integer,String>();
	   
	public void sucess(String s) {
    	Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "Redprotect: [&a&l"+s+"&r]"));
    }
	
    public void info(String s) {
    	Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "Redprotect: ["+s+"]"));
    }
    
    public void warning(String s) {
    	Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "Redprotect: [&6"+s+"&r]"));
    }
    
    public void severe(String s) {
    	Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "Redprotect: [&c&l"+s+"&r]"));
    }
    
    public void log(String s) {
    	Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "Redprotect: ["+s+"]"));
    }
    
    public void debug(String s) {
        if (RPConfig.getBool("debug-messages")) {
        	Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "Redprotect: [&b"+s+"&r]"));
        }  
    }
    
    public void addLog(String logLine){
    	if(!RPConfig.getBool("log-actions")){
    		return;
    	}
    	int key = MainLog.keySet().size()+1;
    	MainLog.put(key, key+" - "+RPUtil.HourNow()+": "+ChatColor.translateAlternateColorCodes('&', logLine));
    	if (key == 500){
    		SaveLogs();
    		MainLog.clear();
    	}
    }
    
    public void SaveLogs(){
    	if(!RPConfig.getBool("log-actions")){
    		return;
    	}
    	int count = 0;
    	String date = RPUtil.DateNow().replace("/", "-");
    	File logfile = new File(RedProtect.pathLogs+date+"-"+count+".zip");
    	while(logfile.exists()){    		
    		count++;
    		logfile = new File(RedProtect.pathLogs+date+"-"+count+".zip");
    	}  
    	
    	final StringBuilder sb = new StringBuilder();
    	for (int key:MainLog.keySet()){
			  sb.append(MainLog.get(key));
			  sb.append('\n');    			  
    	}
    	RPUtil.SaveToZip(logfile, "RedProtectLogs.txt", sb);
    }
}
