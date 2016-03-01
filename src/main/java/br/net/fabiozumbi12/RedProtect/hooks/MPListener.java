package br.net.fabiozumbi12.RedProtect.hooks;

import org.bukkit.Location;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Golem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;

import br.net.fabiozumbi12.RedProtect.RedProtect;
import br.net.fabiozumbi12.RedProtect.Region;
import br.net.fabiozumbi12.RedProtect.config.RPLang;
import de.Keyle.MyPet.api.entity.MyPetEntity;
import de.Keyle.MyPet.entity.types.MyPet.PetState;
import de.Keyle.MyPet.skill.skills.implementation.Fire;
import de.Keyle.MyPet.skill.skills.implementation.Poison;
import de.Keyle.MyPet.skill.skills.implementation.Ranged;

public class MPListener implements Listener{
	
    
    @EventHandler
    public void onEntityDamageByPet( EntityDamageEvent e) {
    	if (e.isCancelled()) {
            return;
        }
    	
    	Entity ent = e.getEntity();
        if (ent instanceof MyPetEntity){
        	Region r = RedProtect.rm.getTopRegion(ent.getLocation());
        	if (r != null && r.flagExists("invincible")){
        		if (r.getFlagBool("invincible")){
        			e.setCancelled(true);
        			((MyPetEntity)ent).setTarget(null);
        		}
        	}        	
        }
        
    	if (e instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent de = (EntityDamageByEntityEvent)e;
            Entity e1 = de.getEntity();
            Entity e2 = de.getDamager();
            
            Location loc = e1.getLocation();
    		Region r1 = RedProtect.rm.getTopRegion(loc);
    		if (r1 == null){
    			return;
    		}
    		
            if (e1 instanceof Animals || e1 instanceof Villager || e1 instanceof Golem) {   
            	if (e2 instanceof Fire || e2 instanceof Poison || e2 instanceof Ranged || e2 instanceof MyPetEntity) {
            		MyPetEntity mp2 = (MyPetEntity)e2;
                    Player p2 = mp2.getOwner().getPlayer();
                    LivingEntity liv = (LivingEntity) e1;                    
                    if (!r1.canBuild(p2) || !r1.canInteractPassives(p2)) {
                        e.setCancelled(true);
                        mp2.getMyPet().setStatus(PetState.Despawned);
                        for (PotionEffect ef:liv.getActivePotionEffects()){
                        	liv.removePotionEffect(ef.getType());
                        }                   
                        p2.sendMessage(RPLang.get("mplistener.cantattack.passives"));
                        return;
                    }                
            	}
        	}
            
            if (e1 instanceof Player) {   
            	if (e2 instanceof Fire || e2 instanceof Poison || e2 instanceof Ranged || e2 instanceof MyPetEntity) {
            		MyPetEntity mp2 = (MyPetEntity)e2;
                    Player p2 = mp2.getOwner().getPlayer();
                    if (!r1.canPVP(p2)) {
                        e.setCancelled(true);
                        mp2.getMyPet().setStatus(PetState.Despawned);
                        for (PotionEffect ef:p2.getActivePotionEffects()){
                        	p2.removePotionEffect(ef.getType());
                        }
                        p2.sendMessage(RPLang.get("mplistener.cantattack.players"));
                        return;
                    }                
            	}
        	}    	
    	}
    }
}
