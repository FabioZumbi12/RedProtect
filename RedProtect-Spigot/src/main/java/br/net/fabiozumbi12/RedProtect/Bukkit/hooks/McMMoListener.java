/*
 Copyright @FabioZumbi12

 This class is provided 'as-is', without any express or implied warranty. In no event will the authors be held liable for any
  damages arising from the use of this class.

 Permission is granted to anyone to use this class for any purpose, including commercial plugins, and to alter it and
 redistribute it freely, subject to the following restrictions:
 1 - The origin of this class must not be misrepresented; you must not claim that you wrote the original software. If you
 use this class in other plugins, an acknowledgment in the plugin documentation would be appreciated but is not required.
 2 - Altered source versions must be plainly marked as such, and must not be misrepresented as being the original class.
 3 - This notice may not be removed or altered from any source distribution.

 Esta classe é fornecida "como está", sem qualquer garantia expressa ou implícita. Em nenhum caso os autores serão
 responsabilizados por quaisquer danos decorrentes do uso desta classe.

 É concedida permissão a qualquer pessoa para usar esta classe para qualquer finalidade, incluindo plugins pagos, e para
 alterá-lo e redistribuí-lo livremente, sujeito às seguintes restrições:
 1 - A origem desta classe não deve ser deturpada; você não deve afirmar que escreveu a classe original. Se você usar esta
  classe em um plugin, uma confirmação de autoria na documentação do plugin será apreciada, mas não é necessária.
 2 - Versões de origem alteradas devem ser claramente marcadas como tal e não devem ser deturpadas como sendo a
 classe original.
 3 - Este aviso não pode ser removido ou alterado de qualquer distribuição de origem.
 */

package br.net.fabiozumbi12.RedProtect.Bukkit.hooks;

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
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class McMMoListener implements Listener {


    @EventHandler
    public void onPlayerExperience(McMMOPlayerXpGainEvent e) {
        if (e.isCancelled()) {
            return;
        }

        RedProtect.get().logger.debug("Mcmmo McMMOPlayerExperienceEvent event. Skill " + e.getSkill().name());

        Player p = e.getPlayer();
        Region r = RedProtect.get().rm.getTopRegion(p.getLocation());
        if (r == null) {
            return;
        }

        if (!r.canSkill(p)) {
            e.setCancelled(true);
        }

        if (RPConfig.getBool("hooks.mcmmo.fix-acrobatics-fire-leveling") && e.getSkill().equals(SkillType.ACROBATICS) && (!r.canFire() || !r.canDeath())) {
            e.setCancelled(true);
        }
		/*
		if (!r.canPVP(p) && (e.getSkill().equals(SkillType.SWORDS) || e.getSkill().equals(SkillType.UNARMED))){
			e.setCancelled(true);
		}
		*/
    }

    @EventHandler
    public void onPlayerActivateAbillity(McMMOPlayerAbilityActivateEvent e) {
        if (e.isCancelled()) {
            return;
        }

        RedProtect.get().logger.debug("Mcmmo McMMOPlayerAbilityActivateEvent event.");

        Player p = e.getPlayer();

        //try to fix invisibility on bersek
        if (RPConfig.getBool("hooks.mcmmo.fix-berserk-invisibility") && e.getAbility().equals(AbilityType.BERSERK)) {
            p.damage(0);
            for (Entity ent : p.getNearbyEntities(10, 10, 10)) {
                if (ent instanceof LivingEntity) {
                    ((LivingEntity) ent).damage(0);
                }
            }
        }

        Region r = RedProtect.get().rm.getTopRegion(p.getLocation());
        if (r == null) {
            return;
        }

        if (!r.canSkill(p)) {
            p.sendMessage(RPLang.get("mcmmolistener.notallowed"));
            e.setCancelled(true);
        }
        if (!r.canPVP(p, null) && (e.getSkill().equals(SkillType.SWORDS) || e.getSkill().equals(SkillType.UNARMED))) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerActivateSecAbillity(SecondaryAbilityWeightedActivationCheckEvent e) {
        RedProtect.get().logger.debug("Mcmmo SecondaryAbilityWeightedActivationCheckEvent event.");

        Player p = e.getPlayer();
        Region r = RedProtect.get().rm.getTopRegion(p.getLocation());
        if (r == null) {
            return;
        }

        if (!r.canSkill(p)) {
            e.setCancelled(true);
        }
        if (!r.canPVP(p, null) && (e.getSkill().equals(SkillType.SWORDS) || e.getSkill().equals(SkillType.UNARMED) || e.getSkill().equals(SkillType.AXES))) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerUnarmed(McMMOPlayerDisarmEvent e) {
        RedProtect.get().logger.debug("Mcmmo McMMOPlayerDisarmEvent event.");

        Player p = e.getPlayer();
        Region r = RedProtect.get().rm.getTopRegion(e.getDefender().getLocation());
        if (r == null) {
            return;
        }

        if (!r.canSkill(p)) {
            e.setCancelled(true);
        }
        if (!r.canPVP(p, e.getDefender()) && (e.getSkill().equals(SkillType.SWORDS) || e.getSkill().equals(SkillType.UNARMED) || e.getSkill().equals(SkillType.AXES))) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onSecondaryAbilityEvent(SecondaryAbilityEvent e) {
        RedProtect.get().logger.debug("Mcmmo SecondaryAbilityEvent event.");

        Player p = e.getPlayer();
        Region r = RedProtect.get().rm.getTopRegion(e.getPlayer().getLocation());
        if (r == null) {
            return;
        }
        if (!r.canSkill(p)) {
            e.setCancelled(true);
        }
        if (!r.canPVP(p, null) && (e.getSkill().equals(SkillType.SWORDS) || e.getSkill().equals(SkillType.UNARMED) || e.getSkill().equals(SkillType.AXES))) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onFakeEntityDamageByEntityEvent(FakeEntityDamageByEntityEvent e) {
        RedProtect.get().logger.debug("Mcmmo FakeEntityDamageByEntityEvent event.");

        if (e.getDamager() instanceof Player) {
            Player p = (Player) e.getDamager();
            Region r = RedProtect.get().rm.getTopRegion(e.getEntity().getLocation());

            if (e.getEntity() instanceof Animals) {
                if (r != null && !r.canInteractPassives(p)) {
                    RPLang.sendMessage(p, "entitylistener.region.cantpassive");
                    e.setCancelled(true);
                }
            }

            if (e.getEntity() instanceof Player) {
                if (r != null && !r.canPVP(p, (Player) e.getEntity())) {
                    RPLang.sendMessage(p, "entitylistener.region.cantpvp");
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onFakeEntityDamageEvent(FakeEntityDamageEvent e) {
        RedProtect.get().logger.debug("Mcmmo FakeEntityDamageEvent event.");

        Region r = RedProtect.get().rm.getTopRegion(e.getEntity().getLocation());

        if (e.getEntity() instanceof Animals) {
            if (r != null && !r.getFlagBool("passives")) {
                e.setCancelled(true);
            }
        }

        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (r != null && !r.canPVP(p, null)) {
                e.setCancelled(true);
            }
        }
    }

}
