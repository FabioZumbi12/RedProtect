package br.net.fabiozumbi12.RedProtect.listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Explosive;
import org.bukkit.entity.Golem;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import br.net.fabiozumbi12.RedProtect.RPContainer;
import br.net.fabiozumbi12.RedProtect.RedProtect;
import br.net.fabiozumbi12.RedProtect.Region;
import br.net.fabiozumbi12.RedProtect.config.RPLang;

public class RPEntityListener implements Listener{
		
	public RPEntityListener(){
		RedProtect.logger.debug("Loaded RPEntityListener...");
	}
	
    static RPContainer cont = new RPContainer();     
        
    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
    	if (event.isCancelled()) {
            return;
        }
    	
        Entity e = (Entity)event.getEntity();
        if (e == null) {
            return;
        }
           
        RedProtect.logger.debug("Spawn monster " + event.getEntityType().name());
        
        if (!(e instanceof Creature)){
        	return;
        }
        
        if (e instanceof Monster || e instanceof Skeleton) {
        	Location l = event.getLocation();
            Region r = RedProtect.rm.getTopRegion(l);
            if (r != null && !r.canSpawnMonsters() && 
            (event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.NATURAL)
            		|| event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.SPAWNER)
            		|| event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.CHUNK_GEN)
            		|| event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.REINFORCEMENTS)
            		|| event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.DEFAULT))) {
            	RedProtect.logger.debug("Cancelled spawn of monster " + event.getEntityType().name());
                event.setCancelled(true);
            }
        }
        if (e instanceof Animals || e instanceof Villager || e instanceof Golem) {
        	Location l = event.getLocation();
            Region r = RedProtect.rm.getTopRegion(l);
            if (r != null && !r.canSpawnPassives() && 
            		(event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.NATURAL)
                    		|| event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.SPAWNER)
                    		|| event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.CHUNK_GEN)
                    		|| event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.REINFORCEMENTS)
                    		|| event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.DEFAULT))) {
            	RedProtect.logger.debug("Cancelled spawn of animal " + event.getEntityType().name());
                event.setCancelled(true);
            }
        }
    }
    
    @SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.isCancelled()) {
            return;
        }        
        
        Entity ent = e.getEntity();
        Region r = RedProtect.rm.getTopRegion(ent.getLocation());
        if (ent instanceof Player){        	
        	if (r != null && r.flagExists("invincible")){
        		if (r.getFlagBool("invincible")){
        			e.setCancelled(true);        			
        		}
        	}        	
        }
        
        if (ent instanceof Animals || ent instanceof Villager || ent instanceof Golem) {
        	if (r != null && r.flagExists("invincible")){
        		if (r.getFlagBool("invincible")){
        			if (ent instanceof Animals){
        				((Animals)ent).setTarget(null);
        			}
        			e.setCancelled(true);        			
        		}
        	}
        }

        if (e instanceof EntityDamageByEntityEvent) {          	
            EntityDamageByEntityEvent de = (EntityDamageByEntityEvent)e;
            
            Entity e1 = de.getEntity();
            Entity e2 = de.getDamager();
            
            if (e2 == null) {
                return;
            }
            
            RedProtect.logger.debug("RPEntityListener - Is EntityDamageByEntityEvent event."); 
            
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
            
            Region r1 = RedProtect.rm.getTopRegion(e1.getLocation());
            Region r2 = RedProtect.rm.getTopRegion(e2.getLocation());
                        
            if (de.getCause().equals(DamageCause.LIGHTNING) || de.getCause().equals(DamageCause.BLOCK_EXPLOSION) || de.getCause().equals(DamageCause.FIRE) || de.getCause().equals(DamageCause.WITHER) || de.getCause().equals(DamageCause.CUSTOM) || de.getCause().equals(DamageCause.ENTITY_EXPLOSION)){           	
            	if (r1 != null && !r1.canFire() && !(e2 instanceof Player)){
            		e.setCancelled(true);
            		return;
            	}
            } 
            
            if (e1 instanceof Player) {
                if (e2 instanceof Player) {                	
                    Player p2 = (Player)e2; 
                    if (r1 != null) {
                    	Material mp2 = p2.getItemInHand().getType();
                    	if (Bukkit.getVersion().startsWith("1.9")){
                    		mp2 = p2.getItemOnCursor().getType();
                        } 
                    	if (mp2.equals(Material.EGG) && !r1.canProtectiles(p2)){
                    		e.setCancelled(true);
                    		RPLang.sendMessage(p2, "playerlistener.region.cantuse");
                            return;
                    	}
                        if (r2 != null) {
                        	if (mp2.equals(Material.EGG) && !r2.canProtectiles(p2)){
                        		e.setCancelled(true);
                        		RPLang.sendMessage(p2, "playerlistener.region.cantuse");
                                return;
                        	}
                            if ((r1.flagExists("pvp") && !r1.canPVP(p2)) || (r1.flagExists("pvp") && !r2.canPVP(p2))) {
                                e.setCancelled(true);
                                RPLang.sendMessage(p2, "entitylistener.region.cantpvp");
                                return;
                            }
                        }
                        else if (r1.flagExists("pvp") && !r1.canPVP(p2)) {
                            e.setCancelled(true);
                            RPLang.sendMessage(p2, "entitylistener.region.cantpvp");
                            return;
                        }
                    }
                    else if (r2 != null && r2.flagExists("pvp") && !r2.canPVP(p2)) {
                        e.setCancelled(true);
                        RPLang.sendMessage(p2, "entitylistener.region.cantpvp");
                        return;
                    }
                }                
            }
            else if (e1 instanceof Animals || e1 instanceof Villager || e1 instanceof Golem) {
            	if (r1 != null && e2 instanceof Player) {
                    Player p2 = (Player)e2;
                    if (!r1.canInteractPassives(p2)) {
                        e.setCancelled(true);
                        RPLang.sendMessage(p2, "entitylistener.region.cantpassive");
                        return;
                    }
                }                
            } 
            else if ((e1 instanceof Hanging) && e2 instanceof Player){
            	Player p2 = (Player)e2;
            	if (r1 != null && !r1.canBuild(p2)){
            		e.setCancelled(true);
            		RPLang.sendMessage(p2, "playerlistener.region.cantuse");
                    return;
            	}                
                if (r2 != null && !r2.canBuild(p2)){
                	e.setCancelled(true);
                	RPLang.sendMessage(p2, "playerlistener.region.cantuse");
                    return;
                }                
            } 
            else if ((e1 instanceof Hanging) && e2 instanceof Monster){
            	if (r1 != null || r2 != null){
            		RedProtect.logger.debug("Cancelled ItemFrame drop Item");
            		e.setCancelled(true);
                    return;
            	}
            }
            else if ((e1 instanceof Explosive)){
            	if ((r1 != null && !r1.canFire()) || (r2 != null && !r2.canFire())){
            		e.setCancelled(true);
                    return;
            	}
            }
        }
    }
        
    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
    	RedProtect.logger.debug("RPEntityListener - Is PotionSplashEvent");
    	if (event.isCancelled()) {
            return;
        }
        ProjectileSource thrower = event.getPotion().getShooter();
        for (PotionEffect e : event.getPotion().getEffects()) {
            PotionEffectType t = e.getType();
            if (!t.equals((Object)PotionEffectType.BLINDNESS) && !t.equals((Object)PotionEffectType.CONFUSION) && !t.equals((Object)PotionEffectType.HARM) && !t.equals((Object)PotionEffectType.HUNGER) && !t.equals((Object)PotionEffectType.POISON) && !t.equals((Object)PotionEffectType.SLOW) && !t.equals((Object)PotionEffectType.SLOW_DIGGING) && !t.equals((Object)PotionEffectType.WEAKNESS) && !t.equals((Object)PotionEffectType.WITHER)) {
                return;
            }
        }
        Player shooter;
        if (thrower instanceof Player) {
            shooter = (Player)thrower;
        } else {
            return;
        }
        for (Entity e2 : event.getAffectedEntities()) {
            Region r = RedProtect.rm.getTopRegion(e2.getLocation());
            if (event.getEntity() instanceof Player){
            	if (r != null && r.flagExists("pvp") && !r.canPVP(shooter)) {
                    event.setCancelled(true);
                    return;
                }
            } else {
            	if (r != null && !r.canInteractPassives(shooter)) {
                    event.setCancelled(true);
                    return;
                }
            }            
        }
    }
    
    @EventHandler
	public void onInteractEvent(PlayerInteractEntityEvent e){
    	RedProtect.logger.debug("RPEntityListener - Is PlayerInteractEntityEvent");
    	if (e.isCancelled()) {
            return;
        }
		Player p = e.getPlayer();
		if (p == null){
			return;
		}
		Location l = e.getRightClicked().getLocation();
		Region r = RedProtect.rm.getTopRegion(l);	
		Entity et = e.getRightClicked();
		if (r != null && !r.canInteractPassives(p) && (et instanceof Animals || et instanceof Villager || et instanceof Golem)) {
			if (et instanceof Tameable){
				Tameable tam = (Tameable) et;
				if (tam.isTamed() && tam.getOwner() != null && tam.getOwner().getName().equals(p.getName())){
					return;
				}
			}
		    e.setCancelled(true);
			RPLang.sendMessage(p, "entitylistener.region.cantinteract");
		}
	}
      
    @EventHandler
    public void WitherBlockBreak(EntityChangeBlockEvent event) {
    	RedProtect.logger.debug("RPEntityListener - Is EntityChangeBlockEvent");
    	if (event.isCancelled()) {
            return;
        }
    	Entity e = event.getEntity();    	
    	if (e instanceof Monster) {
            Region r = RedProtect.rm.getTopRegion(event.getBlock().getLocation());
            if (!cont.canWorldBreak(event.getBlock())){        		        		
        		event.setCancelled(true);
        		return;
        	} 
            if (r != null && !r.canMobLoot()){
         	   event.setCancelled(true);
            }
    	}
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityExplode(EntityExplodeEvent e) {
    	RedProtect.logger.debug("RPEntityListener - Is EntityExplodeEvent");
    	if (e.isCancelled()){
    		return;
    	}
    	List<Block> toRemove = new ArrayList<Block>();
        for (Block b:e.blockList()) {
        	Location l = b.getLocation();
        	Region r = RedProtect.rm.getTopRegion(l);
        	if (r != null && !r.canFire()){
        		toRemove.add(b);
        		continue;
        	}        	
        }
        if (!toRemove.isEmpty()){
        	e.blockList().removeAll(toRemove);
        }
    }
    
    @EventHandler
    public void onEntityEvent(EntityInteractEvent e) {
    	RedProtect.logger.debug("RPEntityListener - Is EntityInteractEvent");
    }
    
    @EventHandler
    public void onBreakDoor(EntityBreakDoorEvent e) {
    	RedProtect.logger.debug("RPEntityListener - Is EntityBreakDoorEvent");
    }
}
