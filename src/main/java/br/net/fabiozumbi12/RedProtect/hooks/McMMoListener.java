package br.net.fabiozumbi12.RedProtect.hooks;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import br.net.fabiozumbi12.RedProtect.RPLang;
import br.net.fabiozumbi12.RedProtect.RedProtect;
import br.net.fabiozumbi12.RedProtect.Region;

import com.gmail.nossr50.events.experience.McMMOPlayerExperienceEvent;
import com.gmail.nossr50.events.skills.abilities.McMMOPlayerAbilityActivateEvent;
import com.gmail.nossr50.events.skills.secondaryabilities.SecondaryAbilityWeightedActivationCheckEvent;
import com.gmail.nossr50.events.skills.unarmed.McMMOPlayerDisarmEvent;

public class McMMoListener implements Listener{

	RedProtect plugin;
	
	public McMMoListener(RedProtect redProtect) {
		plugin = redProtect;
	}

	@EventHandler
	public void onPlayerExperience(McMMOPlayerExperienceEvent e){
		if (e.isCancelled()){
			return;
		}
		
		RedProtect.logger.debug("Mcmmo McMMOPlayerExperienceEvent event.");
		
		Player p = e.getPlayer();
		Region r = RedProtect.rm.getTopRegion(p.getLocation());
		
		if (r != null && !r.canSkill(p)){
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerActivateAbillity(McMMOPlayerAbilityActivateEvent e){
		if (e.isCancelled()){
			return;
		}
		
		RedProtect.logger.debug("Mcmmo McMMOPlayerAbilityActivateEvent event.");
		
		Player p = e.getPlayer();
		Region r = RedProtect.rm.getTopRegion(p.getLocation());
		
		if (r != null && !r.canSkill(p)){
			p.sendMessage(RPLang.get("mcmmolistener.notallowed"));
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerActivateSecAbillity(SecondaryAbilityWeightedActivationCheckEvent e){
		RedProtect.logger.debug("Mcmmo SecondaryAbilityWeightedActivationCheckEvent event.");
		
		Player p = e.getPlayer();
		Region r = RedProtect.rm.getTopRegion(p.getLocation());
		
		if (r != null && !r.canSkill(p)){
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerUnarmed(McMMOPlayerDisarmEvent e){
		RedProtect.logger.debug("Mcmmo McMMOPlayerDisarmEvent event.");
		
		Player p = e.getPlayer();
		Region r = RedProtect.rm.getTopRegion(e.getDefender().getLocation());
		
		if (r != null && !r.canSkill(p)){
			e.setCancelled(true);
		}
	}
}
