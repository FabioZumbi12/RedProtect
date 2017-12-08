package br.net.fabiozumbi12.RedProtect.Bukkit.listeners;

import br.net.fabiozumbi12.RedProtect.Bukkit.RPContainer;
import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPConfig;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPLang;
import net.digiex.magiccarpet.Carpet;
import net.digiex.magiccarpet.MagicCarpet;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("deprecation")
public class RPMine18 implements Listener{
	
	public RPMine18(){
		RedProtect.logger.debug("Loaded RPMine18...");
	}
	
	static final RPContainer cont = new RPContainer();
	static HashMap<Player, String> Ownerslist = new HashMap<>();
    
	@EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
    	if (event.isCancelled()) {
            return;
        }
    	
    	Entity e = event.getEntity();
    	
    	//spawn arms on armor stands
        if (e instanceof ArmorStand && RPConfig.getBool("hooks.armor-stands.spawn-arms")) {
        	ArmorStand as = (ArmorStand) e;
        	as.setArms(true);
        }        
	}
	
    @EventHandler
    public void onAttemptInteractAS(PlayerInteractAtEntityEvent e) {
        if (e.isCancelled()) {
            return;
        }
        
        Entity ent = e.getRightClicked();
        Location l = ent.getLocation();
        Region r = RedProtect.rm.getTopRegion(l);
        Player p = e.getPlayer();
        if (r == null){
        	//global flags
        	if (ent instanceof ArmorStand) {
                if (!RPConfig.getGlobalFlagBool(l.getWorld().getName()+".build")) {
                	e.setCancelled(true);
                    return;
                }
            }
        	return;
        }
        
        if (ent instanceof ArmorStand) {
            if (r != null && !r.canBuild(p)) {
            	RPLang.sendMessage(p, "playerlistener.region.cantedit");
                e.setCancelled(true);
			}
        }
    } 
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void entityFire(EntityCombustByEntityEvent e){
    	if (e.isCancelled()) {
            return;
        }
    	
        Entity e1 = e.getEntity();
        Entity e2 = e.getCombuster();
        Location loc = e1.getLocation();
         
        if (e2 instanceof Projectile) {
        	Projectile a = (Projectile)e2;                
            if (a.getShooter() instanceof Entity){
            	e2 = (Entity)a.getShooter(); 
            }
            a = null;
            if (e2 == null) {
                return;
            }
        }
        
		Region r1 = RedProtect.rm.getTopRegion(loc);
		
		if (r1 == null){
			//global flags
			if (e1 instanceof ArmorStand && e2 instanceof Player){
				if (!RPConfig.getGlobalFlagBool(loc.getWorld().getName()+".build")){
                	e.setCancelled(true);
				}
            }
		} else {
			if (e1 instanceof ArmorStand && e2 instanceof Player){
	        	if (!r1.canBuild(((Player)e2)) && !r1.canBreak(e1.getType())){
	            	e.setCancelled(true);
	            	RPLang.sendMessage(e2, "blocklistener.region.cantbreak");
				}
	        }
		}
    }
    
	@EventHandler
    public void onEntityDamageByPet(EntityDamageByEntityEvent e) {
    	if (e.isCancelled()) {
            return;
        }
    	
        Entity e1 = e.getEntity();
        Entity e2 = e.getDamager();
        Location loc = e1.getLocation();
         
        if (e2 instanceof Projectile) {
        	Projectile a = (Projectile)e2;                
            if (a.getShooter() instanceof Entity){
            	e2 = (Entity)a.getShooter(); 
            }
            a = null;
            if (e2 == null) {
                return;
            }
        }
        
		Region r1 = RedProtect.rm.getTopRegion(loc);
		
		if (r1 == null){
			//global flags
			if (e1 instanceof ArmorStand && e2 instanceof Player){
				if (!RPConfig.getGlobalFlagBool(loc.getWorld().getName()+".build")){
                	e.setCancelled(true);
				}
            }
		} else {
			if (e1 instanceof ArmorStand && e2 instanceof Player){
	        	if (!r1.canBuild(((Player)e2)) && !r1.canBreak(e1.getType())){
	            	e.setCancelled(true);
	            	RPLang.sendMessage(e2, "blocklistener.region.cantbreak");
				}
	        }
		}
	}
    
    @EventHandler
    public void onInteractAS(PlayerInteractEvent e){
    	if (e.isCancelled() || e.getClickedBlock() == null) {
            return;
        }
    	
    	if (RedProtect.version <= 180) {
            return;
        }
    	Player p = e.getPlayer();
    	Location l = e.getClickedBlock().getLocation();
		Region r = RedProtect.rm.getTopRegion(l);		
		Material m = p.getItemInHand().getType();
		
		if (RedProtect.version >= 190 && e.getItem() != null){
			m = e.getItem().getType();
		}
		
    	if (m.equals(Material.ARMOR_STAND) || m.equals(Material.END_CRYSTAL)){
        	if (r != null && !r.canBuild(p) && !r.canPlace(m)){
        		e.setCancelled(true);
        		RPLang.sendMessage(p, "blocklistener.region.cantbuild");
			}
    	}
    }
    
	@EventHandler
	public void onBlockExplode(BlockExplodeEvent e){
		RedProtect.logger.debug("Is BlockListener - BlockExplodeEvent event");
		List<Block> toRemove = new ArrayList<>();
		for (Block b:e.blockList()){
			Region r = RedProtect.rm.getTopRegion(b.getLocation());
			if (!cont.canWorldBreak(b)){
				toRemove.add(b);
				continue;
	    	}
			if (r != null && !r.canFire()){
				toRemove.add(b);
            }
		}		
		if (!toRemove.isEmpty()){
			e.blockList().removeAll(toRemove);
		}
	}
        

    @EventHandler
	public void onPistonRetract(BlockPistonRetractEvent e){
    	if (RedProtect.Mc && RPConfig.getBool("hooks.magiccarpet.fix-piston-getblocks")){	
    		List<Block> blocks = e.getBlocks();
    		for (Block block:blocks){
    			for (Carpet carpet:MagicCarpet.getCarpets().all()){
    				if (carpet != null && carpet.isVisible() && carpet.touches(e.getBlock())){
    					block.setType(Material.AIR);
    					RedProtect.logger.debug("Carpet touch block "+block.getType().name());
    					e.setCancelled(true);
    				}
    			}
    		}
		}
	}
}
