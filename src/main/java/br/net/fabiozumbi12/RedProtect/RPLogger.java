package br.net.fabiozumbi12.RedProtect;

import java.util.SortedMap;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import br.net.fabiozumbi12.RedProtect.config.RPConfig;

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
    	    	
    	final StringBuilder sb = new StringBuilder();
    	for (int key:MainLog.keySet()){
			  sb.append(MainLog.get(key));
			  sb.append('\n');    			  
    	}
    	if (RPUtil.genFileName(RedProtect.pathLogs, false) != null){
    		RPUtil.SaveToZipSB(RPUtil.genFileName(RedProtect.pathLogs, false), "RedProtectLogs.txt", sb);
    	}    	
    }
}
