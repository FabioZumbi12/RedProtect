package br.net.fabiozumbi12.RedProtect.Sponge;

import org.spongepowered.api.Sponge;

import java.io.File;
import java.util.SortedMap;
import java.util.TreeMap;

public class RPLogger{
	private final SortedMap<Integer,String> MainLog = new TreeMap<>();
	   
	public void sucess(String s) {
    	Sponge.getServer().getConsole().sendMessage(RPUtil.toText("Redprotect: [&a&l"+s+"&r]"));
    }
	
    public void info(String s) {
    	Sponge.getServer().getConsole().sendMessage(RPUtil.toText("Redprotect: ["+s+"]"));
    }
    
    public void warning(String s) {
    	Sponge.getServer().getConsole().sendMessage(RPUtil.toText("Redprotect: [&6"+s+"&r]"));
    }
    
    public void severe(String s) {
    	Sponge.getServer().getConsole().sendMessage(RPUtil.toText("Redprotect: [&c&l"+s+"&r]"));
    }
    
    public void log(String s) {
    	Sponge.getServer().getConsole().sendMessage(RPUtil.toText("Redprotect: ["+s+"]"));
    }
    
    public void clear(String s) {
    	Sponge.getServer().getConsole().sendMessage(RPUtil.toText(s));
	}
    
    public void debug(LogLevel level, String s) {
        if (RedProtect.get().cfgs.root().debug_messages.get(level.name().toLowerCase())) {
        	Sponge.getServer().getConsole().sendMessage(RPUtil.toText("Redprotect: [&b"+s+"&r]"));
        }  
    }
    
    public void addLog(String logLine){
    	if(!RedProtect.get().cfgs.root().log_actions){
    		return;
    	}
    	int key = MainLog.keySet().size()+1;
    	MainLog.put(key, key+" - "+RPUtil.HourNow()+": "+RPUtil.toText(logLine));
    	if (key == 500){
    		SaveLogs();
    		MainLog.clear();
    	}
    }
    
    public void SaveLogs(){
    	if(!RedProtect.get().cfgs.root().log_actions){
    		return;
    	}
    	    	
    	final StringBuilder sb = new StringBuilder();
    	for (int key:MainLog.keySet()){
			  sb.append(MainLog.get(key));
			  sb.append('\n');    			  
    	}
    	if (RPUtil.genFileName(RedProtect.get().configDir+ File.separator +"logs"+File.separator, false) != null){
    		RPUtil.SaveToZipSB(RPUtil.genFileName(RedProtect.get().configDir+File.separator+"logs"+File.separator, false), "RedProtectLogs.txt", sb);
    	}    	
    }
}