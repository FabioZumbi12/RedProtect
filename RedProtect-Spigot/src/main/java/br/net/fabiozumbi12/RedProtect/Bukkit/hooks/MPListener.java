package br.net.fabiozumbi12.RedProtect.Bukkit.hooks;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPLang;
import de.Keyle.MyPet.api.entity.MyPetBukkitEntity;
import de.Keyle.MyPet.api.skill.skills.ranged.CraftMyPetProjectile;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;

public class MPListener implements Listener {
   
    @EventHandler
    public void onEntityDamageByPet(EntityDamageEvent e) {
        if (e.isCancelled()) {
            return;
        }

        Entity ent = e.getEntity();
        if (ent instanceof MyPetBukkitEntity) {
            Region r = RedProtect.get().rm.getTopRegion(ent.getLocation());
            if (r != null && r.flagExists("invincible")) {
                if (r.getFlagBool("invincible")) {
                    e.setCancelled(true);
                    ((MyPetBukkitEntity) ent).forgetTarget();
                }
            }
        }

        if (e instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent de = (EntityDamageByEntityEvent) e;
            Entity e1 = de.getEntity();
            Entity e2 = de.getDamager();

            Location loc = e1.getLocation();
            Region r1 = RedProtect.get().rm.getTopRegion(loc);
            if (r1 == null) {
                return;
            }

            if (e1 instanceof Animals || e1 instanceof Villager || e1 instanceof Golem) {
                Entity damager = e2;
                if (damager instanceof CraftMyPetProjectile) {
                    damager = ((CraftMyPetProjectile) damager).getMyPetProjectile().getShooter().getBukkitEntity();
                }
                if (damager instanceof MyPetBukkitEntity) {
                    MyPetBukkitEntity mp2 = (MyPetBukkitEntity) damager;
                    Player p2 = mp2.getOwner().getPlayer();
                    LivingEntity liv = (LivingEntity) e1;
                    if (!r1.canBuild(p2) || !r1.canInteractPassives(p2)) {
                        e.setCancelled(true);
                        mp2.getMyPet().removePet();
                        for (PotionEffect ef : liv.getActivePotionEffects()) {
                            liv.removePotionEffect(ef.getType());
                        }
                        p2.sendMessage(RPLang.get("mplistener.cantattack.passives"));
                        return;
                    }
                }
            }

            if (e1 instanceof Player) {
                Entity damager = e2;
                if (damager instanceof CraftMyPetProjectile) {
                    damager = ((CraftMyPetProjectile) damager).getMyPetProjectile().getShooter().getBukkitEntity();
                }
                if (damager instanceof MyPetBukkitEntity) {
                    MyPetBukkitEntity mp2 = (MyPetBukkitEntity) damager;
                    Player p2 = mp2.getOwner().getPlayer();
                    if (!r1.canPVP((Player)e1, p2)) {
                        e.setCancelled(true);
                        mp2.getMyPet().removePet();
                        for (PotionEffect ef : p2.getActivePotionEffects()) {
                            p2.removePotionEffect(ef.getType());
                        }
                        p2.sendMessage(RPLang.get("mplistener.cantattack.players"));
                    }
                }
            }
        }
    }
}
