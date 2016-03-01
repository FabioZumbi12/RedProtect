package br.net.fabiozumbi12.RedProtect.listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Fish;
import org.bukkit.entity.Golem;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.SpectralArrow;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.Villager;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;

import br.net.fabiozumbi12.RedProtect.RedProtect;
import br.net.fabiozumbi12.RedProtect.Region;
import br.net.fabiozumbi12.RedProtect.config.RPConfig;

@SuppressWarnings("deprecation")
public class RPGlobalListener implements Listener{
	
	private boolean is19;

	public RPGlobalListener(){
		RedProtect.logger.debug("Loaded RPGlobalListener...");
		is19 = Bukkit.getBukkitVersion().startsWith("1.9");
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onBlockPlace(BlockPlaceEvent e) {
		RedProtect.logger.debug("RPGlobalListener - Is BlockPlaceEvent event! Cancelled? " + e.isCancelled());
		if (e.isCancelled() || e.getItemInHand() == null) {
            return;
        }

		Block b = e.getBlock();
		Player p = e.getPlayer();
		Material item = e.getItemInHand().getType();
		Region r = RedProtect.rm.getTopRegion(e.getBlock().getLocation());
		if (r != null){
			return;
		}
		
		if (item.name().contains("MINECART") || item.name().contains("BOAT")){
			if (!RPConfig.getGlobalFlag(p.getWorld().getName()+".use-minecart") && !p.hasPermission("redprotect.bypass")){
	            e.setCancelled(true);
	            RedProtect.logger.debug("RPGlobalListener - Can't place minecart/boat!");
	            return;
	        }
		} else {
			if (!RPConfig.getGlobalFlag(p.getWorld().getName()+".build") && !p.hasPermission("redprotect.bypass")){
				if (RPConfig.getGlobalFlagList(p.getWorld().getName() + ".if-build-false.place-blocks").contains(b.getType().name())){
					return;
				}
				e.setCancelled(true);
				RedProtect.logger.debug("RPGlobalListener - Can't Build!");
				return;
			}
		}		
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		RedProtect.logger.debug("RPGlobalListener - Is BlockBreakEvent event! Cancelled? " + e.isCancelled());
		if (e.isCancelled()) {
            return;
        }

		Block b = e.getBlock();
		Player p = e.getPlayer();
		World w = p.getWorld();
		Region r = RedProtect.rm.getTopRegion(e.getBlock().getLocation());
		
		if (r == null && !RPConfig.getGlobalFlag(w.getName()+".build") && !p.hasPermission("redprotect.bypass")){
			if (RPConfig.getGlobalFlagList(p.getWorld().getName() + ".if-build-false.break-blocks").contains(b.getType().name())){
				return;
			}
			e.setCancelled(true);
			return;
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e){
		RedProtect.logger.debug("RPGlobalListener - Is PlayerInteractEvent event! Cancelled? " + e.isCancelled());
    	if (e.isCancelled()) {
            return;
        }
		Player p = e.getPlayer();	
		Block b = e.getClickedBlock();
		Region r = RedProtect.rm.getTopRegion(b.getLocation());
		if (r != null){
			return;
		}
		
		if (b.getType().name().contains("RAIL") || b.getType().name().contains("WATER")){
            if (!RPConfig.getGlobalFlag(b.getWorld().getName()+".use-minecart") && !p.hasPermission("redprotect.bypass")){
        		e.setUseItemInHand(Event.Result.DENY);
        		e.setCancelled(true);
    			return;		
        	}
        } else {
        	if (!RPConfig.getGlobalFlag(b.getWorld().getName()+".interact") && !p.hasPermission("redprotect.bypass")){
        		e.setUseItemInHand(Event.Result.DENY);
    			e.setCancelled(true);
    			return;
    		}
        }	
	}
	
	@EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent e) {
    	if (e.isCancelled()) {
            return;
        }

        Entity ent = e.getRightClicked();
        Location l = ent.getLocation();
        Region r = RedProtect.rm.getTopRegion(l);
        if (r != null){
			return;
		}
        
        if (ent.getType().name().contains("MINECART") || ent.getType().name().contains("BOAT")){
        	if (!RPConfig.getGlobalFlag(l.getWorld().getName()+".use-minecart") && !e.getPlayer().hasPermission("redprotect.bypass")) {
                e.setCancelled(true);
                return;
            }
        } else {
        	if (!RPConfig.getGlobalFlag(l.getWorld().getName()+".interact") && !e.getPlayer().hasPermission("redprotect.bypass") && (!(ent instanceof Player))) {
                e.setCancelled(true);
                return;
            }
        }      
	}
	
	@EventHandler
    public void onHangingDamaged(HangingBreakByEntityEvent e) {
    	if (e.isCancelled()) {
            return;
        }
    	
        Entity ent = e.getRemover();
        Location loc = e.getEntity().getLocation();
        Region r = RedProtect.rm.getTopRegion(loc);
        if (r != null){
			return;
		}
        
        if (ent instanceof Player) { 
        	Player p = (Player)ent;
            if (!RPConfig.getGlobalFlag(loc.getWorld().getName()+".build") && !p.hasPermission("redprotect.bypass")) {
                e.setCancelled(true);
            }
        }
    }
	
	@EventHandler
	public void onBucketUse(PlayerBucketEmptyEvent e){
    	if (e.isCancelled()) {
            return;
        }

    	Location l = e.getBlockClicked().getLocation();
		Region r = RedProtect.rm.getTopRegion(l);	
		if (r != null){
			return;
		}
		
    	if (!RPConfig.getGlobalFlag(l.getWorld().getName()+".build") && !e.getPlayer().hasPermission("redprotect.bypass")) {
    		e.setCancelled(true);
			return;
    	}
    }
	
	@EventHandler
    public void onEntityDamageEntity(EntityDamageByEntityEvent e) {
		if (e.isCancelled()) {
            return;
        }
		
        Entity e1 = e.getEntity();
        Entity e2 = e.getDamager();
        
        Location loc = e1.getLocation();
		Region r1 = RedProtect.rm.getTopRegion(loc);
		if (r1 != null){
			return;
		}
		
		if (e2 instanceof Creeper || e2.getType().equals(EntityType.PRIMED_TNT) || e2.getType().equals(EntityType.MINECART_TNT)) {
			if (e1 instanceof Player) {
                if (!RPConfig.getGlobalFlag(loc.getWorld().getName()+".explosion-entity-damage")) {
                    e.setCancelled(true);
                    return;
                }
            }        
        	if (e1 instanceof Animals || e1 instanceof Villager || e1 instanceof Golem) {
            	if (!RPConfig.getGlobalFlag(loc.getWorld().getName()+".explosion-entity-damage")){
                    e.setCancelled(true);
                    return;
                }
            }
        	if (e1 instanceof Monster) {
            	if (!RPConfig.getGlobalFlag(loc.getWorld().getName()+".explosion-entity-damage")){
                    e.setCancelled(true);
                    return;
                }
            }
		}
        
        if (e2 instanceof Player) {
        	Player p = (Player)e2;
        	
        	if (e.getCause().equals(DamageCause.LIGHTNING) || e.getCause().equals(DamageCause.BLOCK_EXPLOSION) || e.getCause().equals(DamageCause.ENTITY_EXPLOSION)){           	
            	if (!RPConfig.getGlobalFlag(loc.getWorld().getName()+".entity-block-damage")){
            		e.setCancelled(true);
            		return;
            	}
            }
        	if ((e1.getType().name().contains("MINECART") || e1.getType().name().contains("BOAT")) && !RPConfig.getGlobalFlag(loc.getWorld().getName()+".use-minecart") && !p.hasPermission("redprotect.bypass")){
                e.setCancelled(true);
            	return;
            }
        	if (e1 instanceof Player) {
                if (!RPConfig.getGlobalFlag(loc.getWorld().getName()+".pvp") && !p.hasPermission("redprotect.bypass")) {
                    e.setCancelled(true);
                    return;
                }
            }        
        	if (e1 instanceof Animals || e1 instanceof Villager || e1 instanceof Golem) {
            	if (!RPConfig.getGlobalFlag(loc.getWorld().getName()+".player-hurt-passives") && !p.hasPermission("redprotect.bypass")){
                    e.setCancelled(true);
                    return;
                }
            }
        	if (e1 instanceof Monster) {
            	if (!RPConfig.getGlobalFlag(loc.getWorld().getName()+".player-hurt-monsters") && !p.hasPermission("redprotect.bypass")){
                    e.setCancelled(true);
                    return;
                }
            }
        }
        
        if (e2 instanceof SmallFireball) {
        	SmallFireball proj = (SmallFireball)e2;
        	if (proj.getShooter() instanceof Player){
        		Player p = (Player)proj.getShooter();  
        		
            	if (e1 instanceof Player) {
                    if (!RPConfig.getGlobalFlag(loc.getWorld().getName()+".pvp") && !p.hasPermission("redprotect.bypass")) {
                        e.setCancelled(true);
                        return;
                    }
                }        
            	if (e1 instanceof Animals || e1 instanceof Villager || e1 instanceof Golem) {
                	if (!RPConfig.getGlobalFlag(loc.getWorld().getName()+".player-hurt-passives") && !p.hasPermission("redprotect.bypass")){
                        e.setCancelled(true);
                        return;
                    }
                }
            	if (e1 instanceof Monster) {
                	if (!RPConfig.getGlobalFlag(loc.getWorld().getName()+".player-hurt-monsters") && !p.hasPermission("redprotect.bypass")){
                        e.setCancelled(true);
                        return;
                    }
                }
        	}        	
        }
        
        if (e2 instanceof Fireball) {
        	Fireball proj = (Fireball)e2;
        	if (proj.getShooter() instanceof Player){
        		Player p = (Player)proj.getShooter();  
        		
            	if (e1 instanceof Player) {
                    if (!RPConfig.getGlobalFlag(loc.getWorld().getName()+".pvp") && !p.hasPermission("redprotect.bypass")) {
                        e.setCancelled(true);
                        return;
                    }
                }        
            	if (e1 instanceof Animals || e1 instanceof Villager || e1 instanceof Golem) {
                	if (!RPConfig.getGlobalFlag(loc.getWorld().getName()+".player-hurt-passives") && !p.hasPermission("redprotect.bypass")){
                        e.setCancelled(true);
                        return;
                    }
                }
            	if (e1 instanceof Monster) {
                	if (!RPConfig.getGlobalFlag(loc.getWorld().getName()+".player-hurt-monsters") && !p.hasPermission("redprotect.bypass")){
                        e.setCancelled(true);
                        return;
                    }
                }
        	}        	
        }
        
        if (e2 instanceof Snowball) {
        	Snowball proj = (Snowball)e2;
        	if (proj.getShooter() instanceof Player){
        		Player p = (Player)proj.getShooter();  
        		
            	if (e1 instanceof Player) {
                    if (!RPConfig.getGlobalFlag(loc.getWorld().getName()+".pvp") && !p.hasPermission("redprotect.bypass")) {
                        e.setCancelled(true);
                        return;
                    }
                }        
            	if (e1 instanceof Animals || e1 instanceof Villager || e1 instanceof Golem) {
                	if (!RPConfig.getGlobalFlag(loc.getWorld().getName()+".player-hurt-passives") && !p.hasPermission("redprotect.bypass")){
                        e.setCancelled(true);
                        return;
                    }
                }
            	if (e1 instanceof Monster) {
                	if (!RPConfig.getGlobalFlag(loc.getWorld().getName()+".player-hurt-monsters") && !p.hasPermission("redprotect.bypass")){
                        e.setCancelled(true);
                        return;
                    }
                }
        	}        	
        }
        
        if (e2 instanceof Arrow) {
        	Arrow proj = (Arrow)e2;
        	if (proj.getShooter() instanceof Player){
        		Player p = (Player)proj.getShooter();  
        		
            	if (e1 instanceof Player) {
                    if (!RPConfig.getGlobalFlag(loc.getWorld().getName()+".pvp") && !p.hasPermission("redprotect.bypass")) {
                        e.setCancelled(true);
                        return;
                    }
                }        
            	if (e1 instanceof Animals || e1 instanceof Villager || e1 instanceof Golem) {
                	if (!RPConfig.getGlobalFlag(loc.getWorld().getName()+".player-hurt-passives") && !p.hasPermission("redprotect.bypass")){
                        e.setCancelled(true);
                        return;
                    }
                }
            	if (e1 instanceof Monster) {
                	if (!RPConfig.getGlobalFlag(loc.getWorld().getName()+".player-hurt-monsters") && !p.hasPermission("redprotect.bypass")){
                        e.setCancelled(true);
                        return;
                    }
                }
        	}        	
        }
        
        if (is19 && e2 instanceof SpectralArrow) {
        	SpectralArrow proj = (SpectralArrow)e2;
        	if (proj.getShooter() instanceof Player){
        		Player p = (Player)proj.getShooter();  
        		
            	if (e1 instanceof Player) {
                    if (!RPConfig.getGlobalFlag(loc.getWorld().getName()+".pvp") && !p.hasPermission("redprotect.bypass")) {
                        e.setCancelled(true);
                        return;
                    }
                }        
            	if (e1 instanceof Animals || e1 instanceof Villager || e1 instanceof Golem) {
                	if (!RPConfig.getGlobalFlag(loc.getWorld().getName()+".player-hurt-passives") && !p.hasPermission("redprotect.bypass")){
                        e.setCancelled(true);
                        return;
                    }
                }
            	if (e1 instanceof Monster) {
                	if (!RPConfig.getGlobalFlag(loc.getWorld().getName()+".player-hurt-monsters") && !p.hasPermission("redprotect.bypass")){
                        e.setCancelled(true);
                        return;
                    }
                }
        	}        	
        }
        
        if (is19 && e2 instanceof ShulkerBullet) {
        	ShulkerBullet proj = (ShulkerBullet)e2;
        	if (proj.getShooter() instanceof Player){
        		Player p = (Player)proj.getShooter();  
        		
            	if (e1 instanceof Player) {
                    if (!RPConfig.getGlobalFlag(loc.getWorld().getName()+".pvp") && !p.hasPermission("redprotect.bypass")) {
                        e.setCancelled(true);
                        return;
                    }
                }        
            	if (e1 instanceof Animals || e1 instanceof Villager || e1 instanceof Golem) {
                	if (!RPConfig.getGlobalFlag(loc.getWorld().getName()+".player-hurt-passives") && !p.hasPermission("redprotect.bypass")){
                        e.setCancelled(true);
                        return;
                    }
                }
            	if (e1 instanceof Monster) {
                	if (!RPConfig.getGlobalFlag(loc.getWorld().getName()+".player-hurt-monsters") && !p.hasPermission("redprotect.bypass")){
                        e.setCancelled(true);
                        return;
                    }
                }
        	}        	
        }
        
        if (e2 instanceof Fish) {
        	Fish fish = (Fish)e2;
        	if (fish.getShooter() instanceof Player){
        		Player p = (Player)fish.getShooter();  
        		
            	if (e1 instanceof Player) {
                    if (!RPConfig.getGlobalFlag(loc.getWorld().getName()+".pvp") && !p.hasPermission("redprotect.bypass")) {
                        e.setCancelled(true);
                        return;
                    }
                }        
            	if (e1 instanceof Animals || e1 instanceof Villager || e1 instanceof Golem) {
                	if (!RPConfig.getGlobalFlag(loc.getWorld().getName()+".player-hurt-passives") && !p.hasPermission("redprotect.bypass")){
                        e.setCancelled(true);
                        return;
                    }
                }
            	if (e1 instanceof Monster) {
                	if (!RPConfig.getGlobalFlag(loc.getWorld().getName()+".player-hurt-monsters") && !p.hasPermission("redprotect.bypass")){
                        e.setCancelled(true);
                        return;
                    }
                }             
        	}
        }		
	}

	@EventHandler
    public void onFrameBrake(HangingBreakEvent e) {
    	if (e.isCancelled()){
    		return;
    	}

    	Location l = e.getEntity().getLocation();
		Region r = RedProtect.rm.getTopRegion(l);
    	if (r != null){
    		return;
    	}
    	
    	if (e.getCause().toString().equals("EXPLOSION") || e.getCause().toString().equals("ENTITY")) {
    		if (!RPConfig.getGlobalFlag(l.getWorld().getName()+".entity-block-damage")){
    			e.setCancelled(true);
        		return;
    		}
        }   
    }
	
	@EventHandler(priority = EventPriority.HIGH)
    public void onEntityExplode(EntityExplodeEvent e) {
    	if (e.isCancelled()){
    		return;
    	}
    	List<Block> toRemove = new ArrayList<Block>();
        for (Block b:e.blockList()) {
        	Location l = b.getLocation();
        	Region r = RedProtect.rm.getTopRegion(l);
        	if (r == null && !RPConfig.getGlobalFlag(l.getWorld().getName()+".entity-block-damage")){
        		toRemove.add(b);
        		continue;
        	} 
        }
        if (!toRemove.isEmpty()){
        	e.blockList().removeAll(toRemove);
        }
    }
	
	@EventHandler
    public void onBlockBurn(BlockBurnEvent e){
    	if (e.isCancelled()){
    		return;
    	}
    	Block b = e.getBlock();
    	Region r = RedProtect.rm.getTopRegion(b.getLocation());
    	if (r != null){
    		return;
    	}
    	
		if (!RPConfig.getGlobalFlag(b.getWorld().getName()+".fire-block-damage")){
			e.setCancelled(true);
    		return;
		}    	
    }
	
	@EventHandler
    public void onFireSpread(BlockSpreadEvent  e){
		if (e.isCancelled()){
    		return;
    	}
		Block b = e.getSource();
		Region r = RedProtect.rm.getTopRegion(b.getLocation());
		if (r != null){
    		return;
    	}
		
		if ((b.getType().equals(Material.FIRE) || b.getType().name().contains("LAVA")) && !RPConfig.getGlobalFlag(b.getWorld().getName()+".fire-spread")){
			e.setCancelled(true);
			return;
		}
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
        if (e instanceof Monster && !RPConfig.getGlobalFlag(e.getWorld().getName()+".spawn-monsters")) {
        	Location l = event.getLocation();
            Region r = RedProtect.rm.getTopRegion(l);
            if (r == null && 
            		(event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.NATURAL)
                    		|| event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.SPAWNER)
                    		|| event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.CHUNK_GEN)
                    		|| event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.DEFAULT))) {
                event.setCancelled(true);
            }
        }
        if ((e instanceof Animals || e instanceof Villager || e instanceof Golem) && !RPConfig.getGlobalFlag(e.getWorld().getName()+".spawn-passives")) {
        	Location l = event.getLocation();
            Region r = RedProtect.rm.getTopRegion(l);
            if (r == null && 
            		(event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.NATURAL)
                    		|| event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.SPAWNER)
                    		|| event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.CHUNK_GEN)
                    		|| event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.DEFAULT))) {
                event.setCancelled(true);
            }
        }
    }
	
	@EventHandler
	public void onVehicleBreak(VehicleDestroyEvent e){
		if (e.isCancelled()){
    		return;
    	}
		if (!(e.getAttacker() instanceof Player)){
			return;
		}
		
		Vehicle cart = e.getVehicle();
		Player p = (Player) e.getAttacker();
		Region r = RedProtect.rm.getTopRegion(cart.getLocation());
		if (r != null){
			return;
		}
		
		if (!RPConfig.getGlobalFlag(p.getWorld().getName()+".use-minecart") && !p.hasPermission("redprotect.bypass")){
			e.setCancelled(true);
			return;
		}
	}
	
    @EventHandler
    public void onBlockStartBurn(BlockIgniteEvent e){
    	if (e.isCancelled()){
    		return;
    	}
    	
    	Block b = e.getBlock();
    	Block bignit = e.getIgnitingBlock(); 
    	if ( b == null || bignit == null){
    		return;
    	}
    	RedProtect.logger.debug("Is BlockIgniteEvent event from global-listener");
    	Region r = RedProtect.rm.getTopRegion(b.getLocation());
    	if (r != null){
    		return;
    	}
    	if ((bignit.getType().equals(Material.FIRE) || bignit.getType().name().contains("LAVA")) && !RPConfig.getGlobalFlag(b.getWorld().getName()+".fire-spread")){
			e.setCancelled(true);
    		return;
		}
    	return;
    }
    
    @EventHandler
    public void MonsterBlockBreak(EntityChangeBlockEvent event) {
    	if (event.isCancelled()) {
            return;
        }
    	
    	RedProtect.logger.debug("Is EntityChangeBlockEvent event");
    	Entity e = event.getEntity();    	
    	if (e instanceof Monster) {
            Region r = RedProtect.rm.getTopRegion(event.getBlock().getLocation());
            if (r != null){
         	   return;
            }
            if (!RPConfig.getGlobalFlag(e.getWorld().getName()+".entity-block-damage")){
            	event.setCancelled(true);
            }
    	}
    }
}
