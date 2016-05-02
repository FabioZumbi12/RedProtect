package br.net.fabiozumbi12.RedProtect.listeners;

import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TippedArrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import br.net.fabiozumbi12.RedProtect.RPUtil;
import br.net.fabiozumbi12.RedProtect.RedProtect;
import br.net.fabiozumbi12.RedProtect.Region;
import br.net.fabiozumbi12.RedProtect.config.RPConfig;
import br.net.fabiozumbi12.RedProtect.config.RPLang;

public class RPMine19 implements Listener{

	public RPMine19(){
		RedProtect.logger.debug("Loaded RPMine19...");
	}
	
	@EventHandler
	public void onShootBow(EntityShootBowEvent e){
		if (e.isCancelled() || !(e.getEntity() instanceof Player)){
			return;
		}
		
		Player p = (Player) e.getEntity();		
		Entity proj = e.getProjectile();
		List<String> Pots = RPConfig.getStringList("server-protection.deny-potions");
		
		if (proj != null && (proj instanceof TippedArrow)){
			TippedArrow arr = (TippedArrow) proj;
			if (Pots.contains(arr.getBasePotionData().getType().name())){
				RPLang.sendMessage(p, "playerlistener.denypotion");
				e.setCancelled(true);
			}
		}
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
    	
    	if (RPUtil.denyPotion(e.getEntity().getItem())){
    		e.setCancelled(true);
			if (e.getEntity().getShooter() instanceof Player){
				RPLang.sendMessage((Player)e.getEntity().getShooter(), RPLang.get("playerlistener.denypotion"));
			}
    	}
	}
}
