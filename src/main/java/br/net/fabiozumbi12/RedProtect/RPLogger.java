package br.net.fabiozumbi12.RedProtect;

import org.bukkit.Bukkit;

public class RPLogger{
	   
	public void sucess(String s) {
    	Bukkit.getConsoleSender().sendMessage("Redprotect: [§a§l"+s+"§r]");
    }
	
    public void info(String s) {
    	Bukkit.getConsoleSender().sendMessage("Redprotect: ["+s+"]");
    }
    
    public void warning(String s) {
    	Bukkit.getConsoleSender().sendMessage("Redprotect: [§6"+s+"§r]");
    }
    
    public void severe(String s) {
    	Bukkit.getConsoleSender().sendMessage("Redprotect: [§c§l"+s+"§r]");
    }
    
    public void log(String s) {
    	Bukkit.getConsoleSender().sendMessage("Redprotect: ["+s+"]");
    }
    
    public void debug(String s) {
        if (RPConfig.getBool("debug-messages")) {
        	Bukkit.getConsoleSender().sendMessage("Redprotect: [§b"+s+"§r]");
        }  
    }
}
