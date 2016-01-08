package br.net.fabiozumbi12.RedProtect.hooks;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import br.net.fabiozumbi12.RedProtect.RedProtect;
import br.net.fabiozumbi12.RedProtect.Region;

import com.sucy.skill.api.event.PlayerExperienceGainEvent;
import com.sucy.skill.api.event.PlayerGainSkillPointsEvent;
import com.sucy.skill.api.event.PlayerManaGainEvent;

public class SkillAPIListener implements Listener{
	

	@EventHandler
	public void onPlayerExperience(PlayerExperienceGainEvent e){
		if (e.isCancelled()){
			return;
		}
		
		RedProtect.logger.debug("SkillAPI PlayerExperienceGainEvent event.");
		
		Player p = e.getPlayerData().getPlayer();
		Region r = RedProtect.rm.getTopRegion(p.getLocation());
		if (r != null && !r.canSkill(p)){
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerSkillGain(PlayerGainSkillPointsEvent e){
		if (e.isCancelled()){
			return;
		}
		
		RedProtect.logger.debug("SkillAPI PlayerGainSkillPointsEvent event.");
		
		Player p = e.getPlayerData().getPlayer();
		Region r = RedProtect.rm.getTopRegion(p.getLocation());
		
		if (r != null && !r.canSkill(p)){
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerManaGain(PlayerManaGainEvent e){
		if (e.isCancelled()){
			return;
		}
		
		RedProtect.logger.debug("SkillAPI PlayerManaGainEvent event.");
		
		Player p = e.getPlayerData().getPlayer();
		Region r = RedProtect.rm.getTopRegion(p.getLocation());
		
		if (r != null && !r.canSkill(p)){
			e.setCancelled(true);
		}
	}

}