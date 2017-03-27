package br.net.fabiozumbi12.redprotect.listeners;

import java.util.List;

import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.monster.Monster;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.SpawnEntityEvent.ChunkLoad;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.event.world.UnloadWorldEvent;
import org.spongepowered.api.world.World;

import br.net.fabiozumbi12.redprotect.RedProtect;
import br.net.fabiozumbi12.redprotect.Region;

public class RPWorldListener {
    
    public RPWorldListener() {
        RedProtect.logger.debug("world","Loaded RPEntityListener...");
    }
    
    @Listener
    public void onWorldLoad(LoadWorldEvent e) {
        World w = e.getTargetWorld();
        try {
            RedProtect.rm.load(w);
            RedProtect.cfgs.loadPerWorlds(w);  
            RedProtect.logger.warning("World loaded: " + w.getName());
            
        }
        catch (Exception ex) {
        	RedProtect.logger.severe("RedProtect problem on load world:");
            ex.printStackTrace();
        }
    }
    
    @Listener
    public void onWorldUnload(UnloadWorldEvent e) {
        World w = e.getTargetWorld();
        try {
            RedProtect.rm.unload(w);
            RedProtect.logger.warning("World unloaded: " + w.getName());
        }
        catch (Exception ex) {
        	RedProtect.logger.severe("RedProtect problem on unload world:");
            ex.printStackTrace();
        }
    }
    
    @Listener
    public void onChunkUnload(ChunkLoad e) {
    	World w = e.getEntities().get(0).getWorld();
    	if (!RedProtect.cfgs.getGlobalFlag(w.getName(), "remove-entities-not-allowed-to-spawn")){
    		return;
    	}
    	List<Entity> ents = e.getEntities();
    	for (Entity ent:ents){
    		Region entr = RedProtect.rm.getTopRegion(ent.getLocation());
    		if (entr != null){
    			if (!entr.canSpawnMonsters() && ent instanceof Monster){
        			ent.remove();
        		}
    		} else {
    			if (ent instanceof Monster){
    				if (!RedProtect.cfgs.getGlobalFlag(w.getName(), "spawn-monsters")){
    					ent.remove();
    				}    			   				
    			}
    			else if (!RedProtect.cfgs.getGlobalFlag(w.getName(), "spawn-passives")){
    				if (ent.getCreator().isPresent()){
    					return;
    				}
    				ent.remove();
    			} 
    		}
    		
    	}
    }
}
