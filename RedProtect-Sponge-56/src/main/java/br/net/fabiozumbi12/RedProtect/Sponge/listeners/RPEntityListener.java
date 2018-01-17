package br.net.fabiozumbi12.RedProtect.Sponge.listeners;

import java.util.List;
import java.util.Optional;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.explosive.Explosive;
import org.spongepowered.api.entity.hanging.Hanging;
import org.spongepowered.api.entity.living.Ambient;
import org.spongepowered.api.entity.living.Aquatic;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.Villager;
import org.spongepowered.api.entity.living.animal.Animal;
import org.spongepowered.api.entity.living.golem.Golem;
import org.spongepowered.api.entity.living.monster.Monster;
import org.spongepowered.api.entity.living.monster.Wither;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.entity.projectile.ThrownPotion;
import org.spongepowered.api.entity.projectile.explosive.fireball.Fireball;
import org.spongepowered.api.entity.projectile.source.ProjectileSource;
import org.spongepowered.api.entity.weather.Lightning;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.entity.projectile.LaunchProjectileEvent;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.explosion.Explosion;

import br.net.fabiozumbi12.RedProtect.Sponge.RPContainer;
import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import br.net.fabiozumbi12.RedProtect.Sponge.Region;
import br.net.fabiozumbi12.RedProtect.Sponge.config.RPLang;

public class RPEntityListener {
	
	public RPEntityListener(){
		RedProtect.get().logger.debug("entity","Loaded RPEntityListener...");
	}
	
    static final RPContainer cont = new RPContainer();
        
    @Listener(order = Order.FIRST, beforeModifications = true)
    @IsCancelled(Tristate.FALSE)
    public void onCreatureSpawn(SpawnEntityEvent event) {
    	
    	for (Entity e:event.getEntities()){ 
    		if (e == null || e.getType() == null){
        		continue;
        	} 
            if (!(e instanceof Living)){
            	continue;
            }
            
            Optional<SpawnTypes> cause = event.getCause().first(SpawnTypes.class);            
            RedProtect.get().logger.debug("entity","SpawnCause: "+(cause.map(Object::toString).orElse(" null")));
            if (e instanceof Wither && cause.isPresent() && cause.get().equals(SpawnTypes.PLACEMENT)){            	
                Region r = RedProtect.get().rm.getTopRegion(e.getLocation());
                if (r != null && !r.canSpawnWhiter()){
                	event.isCancelled();
                	return;
                }
            }
            
            if (e instanceof Monster) {
            	Location<World> l = e.getLocation();
                Region r = RedProtect.get().rm.getTopRegion(l);
                if (r != null && !r.canSpawnMonsters()){
                	RedProtect.get().logger.debug("entity","Cancelled spawn of monster " +  e.getType().getName());
                    event.setCancelled(true);
                    return;
                }
            }
            if (e instanceof Animal || e instanceof Golem || e instanceof Ambient || e instanceof Aquatic) {
            	Location<World> l = e.getLocation();
                Region r = RedProtect.get().rm.getTopRegion(l);
                if (r != null && !r.canSpawnPassives()) {
                	RedProtect.get().logger.debug("entity","Cancelled spawn of animal " + e.getType().getName());
                    event.setCancelled(true);
                    return;
                }
            }
            RedProtect.get().logger.debug("entity","RPEntityListener - Spawn mob " + e.getType().getName());
    	}    	
    }
    
	@Listener(order = Order.FIRST, beforeModifications = true)
    public void onEntityDamage(DamageEntityEvent e) {
    	
        //victim
        Entity e1 = e.getTargetEntity();
        RedProtect.get().logger.debug("entity","RPEntityListener - DamageEntityEvent entity target "+e1.getType().getName());
        Region r = RedProtect.get().rm.getTopRegion(e1.getLocation());
        if (e1 instanceof Living && !(e1 instanceof Monster)){        	
        	if (r != null && r.flagExists("invincible")){
        		if (r.getFlagBool("invincible")){
        			e.setCancelled(true);        			
        		}
        	}        	
        }
        
        if (e1 instanceof Animal || e1 instanceof Villager || e1 instanceof Golem || e1 instanceof Ambient) {
        	if (r != null && r.flagExists("invincible")){
        		if (r.getFlagBool("invincible")){
        			if (e1 instanceof Animal){
        				((Animal)e1).setTarget(null);
        			}
        			e.setCancelled(true);        			
        		}
        	}
        }

        //damager
        if (!e.getCause().first(Living.class).isPresent()) {
            return;
        }
        Entity e2 = e.getCause().first(Living.class).get();
        RedProtect.get().logger.debug("entity","RPEntityListener - DamageEntityEvent damager "+e2.getType().getName()); 
        
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
        
        Region r1 = RedProtect.get().rm.getTopRegion(e1.getLocation());
        Region r2 = RedProtect.get().rm.getTopRegion(e2.getLocation());
                    
        if (e.getCause().containsType(Lightning.class) || 
        		e.getCause().containsType(Explosive.class) || 
        		e.getCause().containsType(Fireball.class) || 
        		e.getCause().containsType(Explosion.class)){           	
        	if (r1 != null && !r1.canFire() && !(e2 instanceof Player)){
        		e.setCancelled(true);
        		return;
        	}
        } 
        
        if (e1 instanceof Player) {
            if (e2 instanceof Player) {                	
                Player p2 = (Player)e2; 
                if (r1 != null) {
                	
                	ItemType itemInHand = ItemTypes.NONE;                
                    if (p2.getItemInHand(HandTypes.MAIN_HAND).isPresent()){
                    	itemInHand = p2.getItemInHand(HandTypes.MAIN_HAND).get().getItem();
                    } else if (p2.getItemInHand(HandTypes.OFF_HAND).isPresent()){
                    	itemInHand = p2.getItemInHand(HandTypes.OFF_HAND).get().getItem();
                    }
                    
                	if (itemInHand.getType().equals(ItemTypes.EGG) && !r1.canProtectiles(p2)){
                		e.setCancelled(true);
                		RPLang.sendMessage(p2, "playerlistener.region.cantuse");
                        return;
                	}
                    if (r2 != null) {
                    	if (itemInHand.getType().equals(ItemTypes.EGG) && !r2.canProtectiles(p2)){
                    		e.setCancelled(true);
                    		RPLang.sendMessage(p2, "playerlistener.region.cantuse");
                            return;
                    	}
                        if ((r1.flagExists("pvp") && !r1.canPVP(p2)) || (r1.flagExists("pvp") && !r2.canPVP(p2))) {
                            e.setCancelled(true);
                            RPLang.sendMessage(p2, "entitylistener.region.cantpvp");
                        }
                    }
                    else if (r1.flagExists("pvp") && !r1.canPVP(p2)) {
                        e.setCancelled(true);
                        RPLang.sendMessage(p2, "entitylistener.region.cantpvp");
                    }
                }
                else if (r2 != null && r2.flagExists("pvp") && !r2.canPVP(p2)) {
                    e.setCancelled(true);
                    RPLang.sendMessage(p2, "entitylistener.region.cantpvp");
                }
            }                
        }
        else if (e1 instanceof Animal || e1 instanceof Villager || e1 instanceof Golem || e instanceof Ambient) {
        	if (r1 != null && e2 instanceof Player) {
                Player p2 = (Player)e2;
                if (!r1.canInteractPassives(p2)) {
                    e.setCancelled(true);
                    RPLang.sendMessage(p2, "entitylistener.region.cantpassive");
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
            }
        } 
        else if ((e1 instanceof Hanging) && e2 instanceof Monster){
        	if (r1 != null || r2 != null){
        		RedProtect.get().logger.debug("entity","Cancelled ItemFrame drop Item");
        		e.setCancelled(true);
            }
        }
        else if ((e1 instanceof Explosive)){
        	if ((r1 != null && !r1.canFire()) || (r2 != null && !r2.canFire())){
        		e.setCancelled(true);
            }
        }
    }
        
    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPotionSplash(LaunchProjectileEvent event) {
    	
    	if (event.getTargetEntity() instanceof ThrownPotion){
    		ThrownPotion potion = (ThrownPotion) event.getTargetEntity();
    		ProjectileSource thrower = potion.getShooter();    		
    		
    		RedProtect.get().logger.debug("entity","RPEntityListener - LaunchProjectileEvent entity "+event.getTargetEntity().getType().getName()); 
    		
    		List<PotionEffect> pottypes = potion.get(Keys.POTION_EFFECTS).get();
    		for (PotionEffect t:pottypes){
    			if (!t.getType().equals(PotionEffectTypes.BLINDNESS) && 
    					!t.equals(PotionEffectTypes.WEAKNESS) && 
    					!t.equals(PotionEffectTypes.NAUSEA) && 
    					!t.equals(PotionEffectTypes.HUNGER) && 
    					!t.equals(PotionEffectTypes.POISON) && 
    					!t.equals(PotionEffectTypes.MINING_FATIGUE) && 
    					!t.equals(PotionEffectTypes.HASTE) &&
    					!t.equals(PotionEffectTypes.SLOWNESS) &&
    					!t.equals(PotionEffectTypes.WITHER)) {
                    return;
                }
    		}
            
            
            Player shooter;
            if (thrower instanceof Player) {
                shooter = (Player)thrower;
            } else {
                return;
            }
            
            RedProtect.get().logger.debug("entity","RPEntityListener - LaunchProjectileEvent shooter "+shooter.getName()); 
            
            Entity e2 = event.getTargetEntity();
            Region r = RedProtect.get().rm.getTopRegion(e2.getLocation());
            if (e2 instanceof Player){
            	if (r != null && r.flagExists("pvp") && !r.canPVP(shooter)) {
                    event.setCancelled(true);
                }
            } else {
            	if (r != null && !r.canInteractPassives(shooter)) {
                    event.setCancelled(true);
                }
            }
    	} 
    }
    
	@Listener(order = Order.FIRST, beforeModifications = true)
	public void onInteractEvent(InteractEntityEvent.Secondary e, @First Player p){
		Entity et = e.getTargetEntity();
		Location<World> l = et.getLocation();
		Region r = RedProtect.get().rm.getTopRegion(l);	
		
		RedProtect.get().logger.debug("entity","RPEntityListener - InteractEntityEvent.Secondary entity "+et.getType().getName()); 
		
		if (r != null && !r.canInteractPassives(p) && (et instanceof Animal || et instanceof Villager || et instanceof Golem || et instanceof Ambient)) {
			if (RedProtect.get().getPVHelper().checkHorseOwner(et, p)){
				return;
			}
		    e.setCancelled(true);
			RPLang.sendMessage(p, "entitylistener.region.cantinteract");
		}
	}
      
    @Listener(order = Order.FIRST, beforeModifications = true)
    public void WitherBlockBreak(ChangeBlockEvent.Break event, @First Entity e) {    	    	
    	if (e instanceof Monster) {
    		BlockSnapshot b = event.getTransactions().get(0).getOriginal();
			RedProtect.get().logger.debug("entity","RPEntityListener - Is EntityChangeBlockEvent event! Block "+b.getState().getType().getName());
			Region r = RedProtect.get().rm.getTopRegion(b.getLocation().get());
            if (!cont.canWorldBreak(b)){        		        		
        		event.setCancelled(true);
        		return;
        	} 
            if (r != null && !r.canMobLoot()){
         	   event.setCancelled(true);
            }            
    	}
    }
    
    /*
    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onEntityExplode(EntityExplodeEvent e) {
    	if (e.isCancelled()){
    		return;
    	}
    	List<Block> toRemove = new ArrayList<Block>();
        for (Block b:e.blockList()) {
        	Location l = b.getLocation();
        	Region r = RedProtect.get().rm.getTopRegion(l);
        	if (r != null && !r.canFire()){
        		toRemove.add(b);
        		continue;
        	}        	
        }
        if (!toRemove.isEmpty()){
        	e.blockList().removeAll(toRemove);
        }
    }
    */
}
