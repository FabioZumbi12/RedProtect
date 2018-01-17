package br.net.fabiozumbi12.RedProtect.Bukkit.hooks;

import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPConfig;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPLang;

import com.gmail.nossr50.datatypes.skills.AbilityType;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.events.experience.McMMOPlayerXpGainEvent;
import com.gmail.nossr50.events.fake.FakeEntityDamageByEntityEvent;
import com.gmail.nossr50.events.fake.FakeEntityDamageEvent;
import com.gmail.nossr50.events.skills.abilities.McMMOPlayerAbilityActivateEvent;
import com.gmail.nossr50.events.skills.secondaryabilities.SecondaryAbilityEvent;
import com.gmail.nossr50.events.skills.secondaryabilities.SecondaryAbilityWeightedActivationCheckEvent;
import com.gmail.nossr50.events.skills.unarmed.McMMOPlayerDisarmEvent;

public class McMMoListener implements Listener{
	

	@EventHandler
	public void onPlayerExperience(McMMOPlayerXpGainEvent e){
		if (e.isCancelled()){
			return;
		}
	
		RedProtect.get().logger.debug("Mcmmo McMMOPlayerExperienceEvent event. Skill "+e.getSkill().name());
		
		Player p = e.getPlayer();
		Region r = RedProtect.get().rm.getTopRegion(p.getLocation());
		if (r == null){
			return;
		}
		
		if (!r.canSkill(p)){
			e.setCancelled(true);
		}
		
		if (RPConfig.getBool("hooks.mcmmo.fix-acrobatics-fire-leveling") && e.getSkill().equals(SkillType.ACROBATICS) && (!r.canFire() || !r.canDeath())){
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
				
		RedProtect.get().logger.debug("Mcmmo McMMOPlayerAbilityActivateEvent event.");
		
		Player p = e.getPlayer();
		
		//try to fix invisibility on bersek
		if (RPConfig.getBool("hooks.mcmmo.fix-berserk-invisibility") && e.getAbility().equals(AbilityType.BERSERK)){
			p.damage(0);
			for (Entity ent:p.getNearbyEntities(10, 10, 10)){
				if (ent instanceof LivingEntity){
					((LivingEntity)ent).damage(0);
				}					
			}			
		}		
		
		Region r = RedProtect.get().rm.getTopRegion(p.getLocation());
		if (r == null){
			return;
		}
		
		if (!r.canSkill(p)){
			p.sendMessage(RPLang.get("mcmmolistener.notallowed"));
			e.setCancelled(true);
		}
		if (!r.canPVP(p, null) && (e.getSkill().equals(SkillType.SWORDS) || e.getSkill().equals(SkillType.UNARMED))){
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerActivateSecAbillity(SecondaryAbilityWeightedActivationCheckEvent e){
		RedProtect.get().logger.debug("Mcmmo SecondaryAbilityWeightedActivationCheckEvent event.");
		
		Player p = e.getPlayer();
		Region r = RedProtect.get().rm.getTopRegion(p.getLocation());
		if (r == null){
			return;
		}
		
		if (!r.canSkill(p)){
			e.setCancelled(true);
		}
		if (!r.canPVP(p, null) && (e.getSkill().equals(SkillType.SWORDS) || e.getSkill().equals(SkillType.UNARMED) || e.getSkill().equals(SkillType.AXES))){
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerUnarmed(McMMOPlayerDisarmEvent e){
		RedProtect.get().logger.debug("Mcmmo McMMOPlayerDisarmEvent event.");
		
		Player p = e.getPlayer();
		Region r = RedProtect.get().rm.getTopRegion(e.getDefender().getLocation());
		if (r == null){
			return;
		}
		
		if (!r.canSkill(p)){
			e.setCancelled(true);
		}
		if (!r.canPVP(p, e.getDefender()) && (e.getSkill().equals(SkillType.SWORDS) || e.getSkill().equals(SkillType.UNARMED) || e.getSkill().equals(SkillType.AXES))){
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onSecondaryAbilityEvent(SecondaryAbilityEvent e){
		RedProtect.get().logger.debug("Mcmmo SecondaryAbilityEvent event.");
		
		Player p = e.getPlayer();
		Region r = RedProtect.get().rm.getTopRegion(e.getPlayer().getLocation());
		if (r == null){
			return;
		}
		if (!r.canSkill(p)){
			e.setCancelled(true);
		}
		if (!r.canPVP(p, null) && (e.getSkill().equals(SkillType.SWORDS) || e.getSkill().equals(SkillType.UNARMED) || e.getSkill().equals(SkillType.AXES))){
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onFakeEntityDamageByEntityEvent(FakeEntityDamageByEntityEvent e){
		RedProtect.get().logger.debug("Mcmmo FakeEntityDamageByEntityEvent event.");
		
		if (e.getDamager() instanceof Player){
			Player p = (Player) e.getDamager();
			Region r = RedProtect.get().rm.getTopRegion(e.getEntity().getLocation());
			
			if (e.getEntity() instanceof Animals){
				if (r != null && !r.canInteractPassives(p)){
					RPLang.sendMessage(p, "entitylistener.region.cantpassive");
					e.setCancelled(true);
				}
			}			
			
			if (e.getEntity() instanceof Player){
				if (r != null && !r.canPVP(p, (Player)e.getEntity())){
					RPLang.sendMessage(p, "entitylistener.region.cantpvp");
					e.setCancelled(true);
				}
			}
		}		
	}
	
	@EventHandler
	public void onFakeEntityDamageEvent(FakeEntityDamageEvent e){
		RedProtect.get().logger.debug("Mcmmo FakeEntityDamageEvent event.");

		Region r = RedProtect.get().rm.getTopRegion(e.getEntity().getLocation());
		
		if (e.getEntity() instanceof Animals){
			if (r != null && !r.getFlagBool("passives")){
				e.setCancelled(true);
			}
		}			
		
		if (e.getEntity() instanceof Player){
			Player p = (Player) e.getEntity();
			if (r != null && !r.canPVP(p, null)){
				e.setCancelled(true);
			}
		}
	}	
	
}
