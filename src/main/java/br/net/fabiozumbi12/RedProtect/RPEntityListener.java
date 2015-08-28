package br.net.fabiozumbi12.RedProtect;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
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
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

class RPEntityListener implements Listener{
	
    RedProtect plugin;
        
    public RPEntityListener(RedProtect plugin) {
        this.plugin = plugin;
    }
        
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
        if (e instanceof Animals || e instanceof Golem) {
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
        	RedProtect.logger.debug("RPEntityListener - Is EntityDamageByEntityEvent event.");   
        	
            EntityDamageByEntityEvent de = (EntityDamageByEntityEvent)e;
            //check player listener
            de.setCancelled(RPPlayerListener.CheckPlayerEvent(de));
            
            Entity e1 = de.getEntity();
            Entity e2 = de.getDamager();
            
            if (e2 == null) {
                return;
            }
            
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
                    	if (p2.getItemInHand().getType().equals(Material.EGG) && !r1.canBuild(p2)){
                    		e.setCancelled(true);
                    		RPLang.sendMessage(p2, "playerlistener.region.cantuse");
                            return;
                    	}
                        if (r2 != null) {
                        	if (p2.getItemInHand().getType().equals(Material.EGG) && !r2.canBuild(p2)){
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
                    if (!r1.canHurtPassives(p2)) {
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
        }
    }
    
    @EventHandler
    public void onHangingDamaged(HangingBreakByEntityEvent e) {
    	if (e.isCancelled()) {
            return;
        }
    	RedProtect.logger.debug("Is Entity Listener - HangingBreakByEntityEvent event");
        Entity ent = e.getRemover();
        Location loc = e.getEntity().getLocation();
        Region r = RedProtect.rm.getTopRegion(loc);
        
        if (ent instanceof Player) {
            Player player = (Player)ent; 
            if (r != null && !r.canBuild(player)) {
            	RPLang.sendMessage(player, "blocklistener.region.cantbuild");
                e.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
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
            	if (r != null && !r.canHurtPassives(shooter)) {
                    event.setCancelled(true);
                    return;
                }
            }            
        }
    }
    
    @EventHandler
	public void onInteractEvent(PlayerInteractEntityEvent e){
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
		if (r != null && !r.canHurtPassives(p) && (et instanceof Animals || et instanceof Villager)) {
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
	public void onBucketUse(PlayerBucketEmptyEvent e){
    	if (e.isCancelled()) {
            return;
        }
    	Player p = e.getPlayer();
    	Location l = e.getBlockClicked().getLocation();
		Region r = RedProtect.rm.getTopRegion(l);	
    	if (r != null && !r.canBuild(p) && 
    			(p.getItemInHand().getType().name().contains("BUCKET"))) {
    			e.setCancelled(true);
    			return;
    		}
    }
    
    @EventHandler
	public void onBucketFill(PlayerBucketFillEvent e){
    	if (e.isCancelled()) {
            return;
        }
    	Player p = e.getPlayer();
    	Location l = e.getBlockClicked().getLocation();
		Region r = RedProtect.rm.getTopRegion(l);	
    	if (r != null && !r.canBuild(p) && 
    			(p.getItemInHand().getType().name().contains("BUCKET"))) {
    			e.setCancelled(true);
    			return;
    		}
    }
      
    @EventHandler
    public void WitherBlockBreak(EntityChangeBlockEvent event) {
    	if (event.isCancelled()) {
            return;
        }
    	RedProtect.logger.debug("Is EntityChangeBlockEvent event");
    	Entity e = event.getEntity();    	
    	if (e instanceof Monster) {
            Region r = RedProtect.rm.getTopRegion(event.getEntity().getLocation());
            if (r != null && !r.canFire()){
         	   event.setCancelled(true);
            }
    	}
    }

}
