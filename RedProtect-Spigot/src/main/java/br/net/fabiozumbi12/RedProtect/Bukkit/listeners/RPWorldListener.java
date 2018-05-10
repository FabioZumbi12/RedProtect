package br.net.fabiozumbi12.RedProtect.Bukkit.listeners;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPConfig;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import java.util.HashMap;

public class RPWorldListener implements Listener {
    
	private final HashMap<World, Integer> rainCounter = new HashMap<>();
	
    public RPWorldListener() {
        RedProtect.get().logger.debug("Loaded RPEntityListener...");
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onWeatherChange(WeatherChangeEvent e) {    
    	World w = e.getWorld();
    	int trys = RPConfig.getGlobalFlagInt(w.getName()+".rain.trys-before-rain");
    	if (e.toWeatherState()){
    		if (!rainCounter.containsKey(w)){
    			rainCounter.put(w, trys);
    			e.setCancelled(true);
    		} else {
    			int acTry = rainCounter.get(w);
    			if (acTry-1 <= 0){    
    				Bukkit.getScheduler().runTaskLater(RedProtect.get(), () -> w.setWeatherDuration(RPConfig.getGlobalFlagInt(w.getName()+".rain.duration")*20), 40);
    				rainCounter.put(w, trys);
    			} else {
    				rainCounter.put(w, acTry-1);
        			e.setCancelled(true);
    			}
    		}    		
    	}
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onWorldLoad(WorldLoadEvent e) {
        World w = e.getWorld();
        try {
            RedProtect.get().rm.load(w);
            RPConfig.init();    
            RedProtect.get().logger.warning("World loaded: " + w.getName());            
        }
        catch (Exception ex) {
        	RedProtect.get().logger.severe("RedProtect problem on load world:");
            ex.printStackTrace();
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onWorldUnload(WorldUnloadEvent e) {
        World w = e.getWorld();
        try {
            RedProtect.get().rm.unload(w);
            RedProtect.get().logger.warning("World unloaded: " + w.getName());
        }
        catch (Exception ex) {
        	RedProtect.get().logger.severe("RedProtect problem on unload world:");
            ex.printStackTrace();
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onChunkUnload(ChunkLoadEvent e) {
    	if (!RPConfig.getGlobalFlagBool("remove-entities-not-allowed-to-spawn")){
    		return;
    	}
    	Entity[] ents = e.getChunk().getEntities();
    	for (Entity ent:ents){
    		Region entr = RedProtect.get().rm.getTopRegion(ent.getLocation());
    		if (entr != null){
    			if (!entr.canSpawnMonsters() && ent instanceof Monster){
        			ent.remove();
        		}
    		} else {
    			if (ent instanceof Monster){
    				if (!RPConfig.getGlobalFlagBool("spawn-monsters")){
    					ent.remove();
    				}    			   				
    			}
    			else if (!RPConfig.getGlobalFlagBool("spawn-passives")){
    				if (ent instanceof Tameable){
    					return;
    				}
    				ent.remove();
    			} 
    		}
    		
    	}
    }
}
