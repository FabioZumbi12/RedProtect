package br.net.fabiozumbi12.RedProtect;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Fish;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import de.Keyle.MyPet.api.entity.MyPetEntity;
import de.Keyle.MyPet.entity.types.MyPet.PetState;
import de.Keyle.MyPet.skill.skills.implementation.Fire;
import de.Keyle.MyPet.skill.skills.implementation.Poison;
import de.Keyle.MyPet.skill.skills.implementation.Ranged;

@SuppressWarnings("deprecation")
class RPMine18 implements Listener{
	
	static RPContainer cont = new RPContainer();
	static HashMap<Player, String> Ownerslist = new HashMap<Player, String>();
    RedProtect plugin;
    
    public RPMine18(RedProtect plugin) {
        this.plugin = plugin;
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
        	if (ent.getType().equals(EntityType.ARMOR_STAND)) {
                if (!RPConfig.getGlobalFlag(l.getWorld().getName()+".build")) {
                	e.setCancelled(true);
                    return;
                }
            }
        	return;
        }
        
        if (ent.getType().equals(EntityType.ARMOR_STAND)) {
            if (r != null && !r.canBuild(p)) {
                if (!RedProtect.ph.hasPerm(p, "redprotect.bypass")) {
                	RPLang.sendMessage(p, "playerlistener.region.cantedit");
                    e.setCancelled(true);
                    return;
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
        
        Player p = null;        
        if (e.getDamager() instanceof Player){
        	p = (Player)e.getDamager();
        } else if (e.getDamager() instanceof Arrow){
        	Arrow proj = (Arrow)e.getDamager();
        	if (proj.getShooter() instanceof Player){
        		p = (Player) proj.getShooter();
        	}        	
        } else if (e.getDamager() instanceof Fish){
        	Fish fish = (Fish)e.getDamager();
        	if (fish.getShooter() instanceof Player){
        		p = (Player) fish.getShooter();
        	} 
        } else if (e.getDamager() instanceof Egg){
        	Egg Egg = (Egg)e.getDamager();
        	if (Egg.getShooter() instanceof Player){
        		p = (Player) Egg.getShooter();
        	} 
        } else if (e.getDamager() instanceof Snowball){
        	Snowball Snowball = (Snowball)e.getDamager();
        	if (Snowball.getShooter() instanceof Player){
        		p = (Player) Snowball.getShooter();
        	} 
        } else if (e.getDamager() instanceof Fireball){
        	Fireball Fireball = (Fireball)e.getDamager();
        	if (Fireball.getShooter() instanceof Player){
        		p = (Player) Fireball.getShooter();
        	} 
        } else if (e.getDamager() instanceof SmallFireball){
        	SmallFireball SmallFireball = (SmallFireball)e.getDamager();
        	if (SmallFireball.getShooter() instanceof Player){
        		p = (Player) SmallFireball.getShooter();
        	} 
        } else {
        	e.isCancelled();
        	return;
        }         

        if (p == null){
        	return;
        }
        
		Region r1 = RedProtect.rm.getTopRegion(loc);
		
		if (r1 == null){
			//global flags
			if (e1 instanceof ArmorStand){
            	if (e2 instanceof Player) {
                    if (!RPConfig.getGlobalFlag(loc.getWorld().getName()+".build")){
                    	e.setCancelled(true);
                    	return;
                    }
                }                  
            }
            
            if (e1 instanceof ArmorStand && RedProtect.MyPet) {   
            	if (e2 instanceof Fire || e2 instanceof Poison || e2 instanceof Ranged || e2 instanceof MyPetEntity) {
            		MyPetEntity mp2 = (MyPetEntity)e2;
                    if (!RPConfig.getGlobalFlag(loc.getWorld().getName()+".build")) {
                        e.setCancelled(true);
                        mp2.getMyPet().setStatus(PetState.Despawned);
                        return;
                    }                
            	}
        	}
            return;
		} 
		
		if (e1 instanceof ArmorStand){
        	if (r1 != null && !r1.canBuild(p)){
            	e.setCancelled(true);
            	RPLang.sendMessage(p, "blocklistener.region.cantbreak");
            	return;
            }                                  
        }
        
        if (e1 instanceof ArmorStand && RedProtect.MyPet) {   
        	if (e2 instanceof Fire || e2 instanceof Poison || e2 instanceof Ranged || e2 instanceof MyPetEntity) {
        		MyPetEntity mp2 = (MyPetEntity)e2;
                Player p2 = mp2.getOwner().getPlayer();
                if (!r1.canBuild(p2)) {
                    e.setCancelled(true);
                    RPLang.sendMessage(p2, "mplistener.cantdo");
                    mp2.getMyPet().setStatus(PetState.Despawned);
                    return;
                }                
        	}
    	}
	} 
    
    @EventHandler
    public void onInteractAS(PlayerInteractEvent e){
    	if (e.isCancelled()) {
            return;
        }
    	String v = RedProtect.serv.getBukkitVersion(); 
    	if (!v.contains("1.8")) {
            return;
        }
    	Player p = e.getPlayer();
    	Location l = e.getClickedBlock().getLocation();
		Region r = RedProtect.rm.getTopRegion(l);		
		
    	if (p.getItemInHand().getType().equals(Material.ARMOR_STAND)){
        	if (r != null && !r.canBuild(p)){
        		e.setCancelled(true);
        		RPLang.sendMessage(p, "blocklistener.region.cantbuild");
            	return;
        	}    	
    	}
    }
    
	@EventHandler
	public void onBlockExplode(BlockExplodeEvent e){
		RedProtect.logger.debug("Is BlockExplodeEvent event");		
		Location l = e.getBlock().getLocation();
		Region r = RedProtect.rm.getTopRegion(l);
		if (r != null && !r.canFire()){
			e.setCancelled(true);
			return;
		}
	}
        
}
