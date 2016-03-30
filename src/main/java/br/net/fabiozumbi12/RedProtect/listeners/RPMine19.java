package br.net.fabiozumbi12.RedProtect.listeners;

import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import br.net.fabiozumbi12.RedProtect.RedProtect;
import br.net.fabiozumbi12.RedProtect.Region;
import br.net.fabiozumbi12.RedProtect.config.RPConfig;
import br.net.fabiozumbi12.RedProtect.config.RPLang;

public class RPMine19 implements Listener{

	public RPMine19(){
		RedProtect.logger.debug("Loaded RPMine19...");
	}
	
	@EventHandler
	public void onLingerPotion(LingeringPotionSplashEvent e){
		if (!(e.getEntity().getShooter() instanceof Player)){
    		return;
    	}
    	
    	Player p = (Player)e.getEntity().getShooter();
    	Entity ent = e.getEntity();
    	
    	RedProtect.logger.debug("Is LingeringPotionSplashEvent event.");
        
    	Region r = RedProtect.rm.getTopRegion(ent.getLocation());
    	if (r != null && !r.allowPotions(p)){
    		RPLang.sendMessage(p, "playerlistener.region.cantuse");
    		e.setCancelled(true);
    		return;
    	}    
    	
    	//deny potion
        List<String> Pots = RPConfig.getStringList("server-protection.deny-potions");
        if(Pots.size() > 0){        	
        	PotionMeta pot = (PotionMeta) e.getEntity().getItem().getItemMeta();     	
        	for (String potion:Pots){
        		try{
        			if (pot.getBasePotionData().getType().equals(PotionType.valueOf(potion.toUpperCase()))){
            			e.setCancelled(true);
            			if (e.getEntity().getShooter() instanceof Player){
            				RPLang.sendMessage((Player)e.getEntity().getShooter(), RPLang.get("playerlistener.denypotion"));
            			}            			
            		}
        		} catch(IllegalArgumentException ex){
        			RedProtect.logger.severe("The config 'deny-potions' have a unknow potion type. Change to a valid potion type to really deny the usage.");
        		}
        	}                    
        }
	}
}
