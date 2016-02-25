package br.net.fabiozumbi12.RedProtect.hooks;

import org.bukkit.entity.Animals;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import br.net.fabiozumbi12.RedProtect.RedProtect;
import br.net.fabiozumbi12.RedProtect.Region;
import br.net.fabiozumbi12.RedProtect.config.RPLang;

import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.events.experience.McMMOPlayerExperienceEvent;
import com.gmail.nossr50.events.fake.FakeEntityDamageByEntityEvent;
import com.gmail.nossr50.events.fake.FakeEntityDamageEvent;
import com.gmail.nossr50.events.skills.abilities.McMMOPlayerAbilityActivateEvent;
import com.gmail.nossr50.events.skills.secondaryabilities.SecondaryAbilityEvent;
import com.gmail.nossr50.events.skills.secondaryabilities.SecondaryAbilityWeightedActivationCheckEvent;
import com.gmail.nossr50.events.skills.unarmed.McMMOPlayerDisarmEvent;

public class McMMoListener implements Listener{
	

	@EventHandler
	public void onPlayerExperience(McMMOPlayerExperienceEvent e){
		if (e.isCancelled()){
			return;
		}
	
		RedProtect.logger.debug("Mcmmo McMMOPlayerExperienceEvent event.");
		
		Player p = e.getPlayer();
		Region r = RedProtect.rm.getTopRegion(p.getLocation());
		if (r == null){
			return;
		}
		
		if (!r.canSkill(p)){
			e.setCancelled(true);
		}
		/*
		if (!r.canPVP(p) && (e.getSkill().equals(SkillType.SWORDS) || e.getSkill().equals(SkillType.UNARMED))){
			e.setCancelled(true);
		}
		*/
	}
	
	@EventHandler
	public void onPlayerActivateAbillity(McMMOPlayerAbilityActivateEvent e){
		if (e.isCancelled()){
			return;
		}
		
		RedProtect.logger.debug("Mcmmo McMMOPlayerAbilityActivateEvent event.");
		
		Player p = e.getPlayer();
		Region r = RedProtect.rm.getTopRegion(p.getLocation());
		if (r == null){
			return;
		}
		
		if (!r.canSkill(p)){
			p.sendMessage(RPLang.get("mcmmolistener.notallowed"));
			e.setCancelled(true);
		}
		if (!r.canPVP(p) && (e.getSkill().equals(SkillType.SWORDS) || e.getSkill().equals(SkillType.UNARMED))){
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerActivateSecAbillity(SecondaryAbilityWeightedActivationCheckEvent e){
		RedProtect.logger.debug("Mcmmo SecondaryAbilityWeightedActivationCheckEvent event.");
		
		Player p = e.getPlayer();
		Region r = RedProtect.rm.getTopRegion(p.getLocation());
		if (r == null){
			return;
		}
		
		if (!r.canSkill(p)){
			e.setCancelled(true);
		}
		if (!r.canPVP(p) && (e.getSkill().equals(SkillType.SWORDS) || e.getSkill().equals(SkillType.UNARMED) || e.getSkill().equals(SkillType.AXES))){
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerUnarmed(McMMOPlayerDisarmEvent e){
		RedProtect.logger.debug("Mcmmo McMMOPlayerDisarmEvent event.");
		
		Player p = e.getPlayer();
		Region r = RedProtect.rm.getTopRegion(e.getDefender().getLocation());
		if (r == null){
			return;
		}
		
		if (!r.canSkill(p)){
			e.setCancelled(true);
		}
		if (!r.canPVP(p) && (e.getSkill().equals(SkillType.SWORDS) || e.getSkill().equals(SkillType.UNARMED) || e.getSkill().equals(SkillType.AXES))){
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onSecondaryAbilityEvent(SecondaryAbilityEvent e){
		RedProtect.logger.debug("Mcmmo SecondaryAbilityEvent event.");
		
		Player p = e.getPlayer();
		Region r = RedProtect.rm.getTopRegion(e.getPlayer().getLocation());
		if (r == null){
			return;
		}
		if (!r.canSkill(p)){
			e.setCancelled(true);
		}
		if (!r.canPVP(p) && (e.getSkill().equals(SkillType.SWORDS) || e.getSkill().equals(SkillType.UNARMED) || e.getSkill().equals(SkillType.AXES))){
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onFakeEntityDamageByEntityEvent(FakeEntityDamageByEntityEvent e){
		RedProtect.logger.debug("Mcmmo FakeEntityDamageByEntityEvent event.");
		
		if (e.getDamager() instanceof Player){
			Player p = (Player) e.getDamager();
			Region r = RedProtect.rm.getTopRegion(e.getEntity().getLocation());
			
			if (e.getEntity() instanceof Animals){
				if (r != null && !r.canInteractPassives(p)){
					RPLang.sendMessage(p, "entitylistener.region.cantpassive");
					e.setCancelled(true);
				}
			}			
			
			if (e.getEntity() instanceof Player){
				if (r != null && !r.canPVP(p)){
					RPLang.sendMessage(p, "entitylistener.region.cantpvp");
					e.setCancelled(true);
				}
			}
		}		
	}
	
	@EventHandler
	public void onFakeEntityDamageEvent(FakeEntityDamageEvent e){
		RedProtect.logger.debug("Mcmmo FakeEntityDamageEvent event.");

		Region r = RedProtect.rm.getTopRegion(e.getEntity().getLocation());
		
		if (e.getEntity() instanceof Animals){
			if (r != null && !r.getFlagBool("passives")){
				e.setCancelled(true);
			}
		}			
		
		if (e.getEntity() instanceof Player){
			Player p = (Player) e.getEntity();
			if (r != null && !r.canPVP(p)){
				e.setCancelled(true);
			}
		}
	}	
	
}
