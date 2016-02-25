package br.net.fabiozumbi12.RedProtect.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Tameable;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;
import org.bukkit.World;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.Listener;

import br.net.fabiozumbi12.RedProtect.RedProtect;
import br.net.fabiozumbi12.RedProtect.Region;
import br.net.fabiozumbi12.RedProtect.config.RPConfig;

public class RPWorldListener implements Listener {
    
    public RPWorldListener() {
        RedProtect.logger.debug("Loaded RPEntityListener...");
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onWorldLoad(WorldLoadEvent e) {
        World w = e.getWorld();
        try {
            RedProtect.rm.load(w);
            RPConfig.init(RedProtect.plugin);    
            RedProtect.logger.warning("World loaded: " + w.getName());
            
        }
        catch (Exception ex) {
        	RedProtect.logger.severe("RedProtect problem on load world:");
            ex.printStackTrace();
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onWorldUnload(WorldUnloadEvent e) {
        World w = e.getWorld();
        try {
            RedProtect.rm.unload(w);
            RedProtect.logger.warning("World unloaded: " + w.getName());
        }
        catch (Exception ex) {
        	RedProtect.logger.severe("RedProtect problem on unload world:");
            ex.printStackTrace();
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onChunkUnload(ChunkLoadEvent e) {
    	if (!RPConfig.getGlobalFlag("remove-entities-not-allowed-to-spawn")){
    		return;
    	}
    	Entity[] ents = e.getChunk().getEntities();
    	for (Entity ent:ents){
    		Region entr = RedProtect.rm.getTopRegion(ent.getLocation());
    		if (entr != null){
    			if (!entr.canSpawnMonsters() && ent instanceof Monster){
        			ent.remove();
        		}
    		} else {
    			if (ent instanceof Monster){
    				if (!RPConfig.getGlobalFlag("spawn-monsters")){
    					ent.remove();
    				}    			   				
    			}
    			else if (!RPConfig.getGlobalFlag("spawn-passives")){
    				if (ent instanceof Tameable){
    					return;
    				}
    				ent.remove();
    			} 
    		}
    		
    	}
    }
}
