/*
 * Copyright (c) 2012-2023 - @FabioZumbi12
 * Last Modified: 02/10/2023 22:14
 *
 * This class is provided 'as-is', without any express or implied warranty. In no event will the authors be held liable for any
 *  damages arising from the use of this class.
 *
 * Permission is granted to anyone to use this class for any purpose, including commercial plugins, and to alter it and
 * redistribute it freely, subject to the following restrictions:
 * 1 - The origin of this class must not be misrepresented; you must not claim that you wrote the original software. If you
 * use this class in other plugins, an acknowledgment in the plugin documentation would be appreciated but is not required.
 * 2 - Altered source versions must be plainly marked as such, and must not be misrepresented as being the original class.
 * 3 - This notice may not be removed or altered from any source distribution.
 *
 * Esta classe é fornecida "como está", sem qualquer garantia expressa ou implícita. Em nenhum caso os autores serão
 * responsabilizados por quaisquer danos decorrentes do uso desta classe.
 *
 * É concedida permissão a qualquer pessoa para usar esta classe para qualquer finalidade, incluindo plugins pagos, e para
 * alterá-lo e redistribuí-lo livremente, sujeito às seguintes restrições:
 * 1 - A origem desta classe não deve ser deturpada; você não deve afirmar que escreveu a classe original. Se você usar esta
 *  classe em um plugin, uma confirmação de autoria na documentação do plugin será apreciada, mas não é necessária.
 * 2 - Versões de origem alteradas devem ser claramente marcadas como tal e não devem ser deturpadas como sendo a
 * classe original.
 * 3 - Este aviso não pode ser removido ou alterado de qualquer distribuição de origem.
 */

package br.net.fabiozumbi12.RedProtect.Bukkit.hooks;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import de.Keyle.MyPet.api.entity.MyPetBukkitEntity;
import de.Keyle.MyPet.api.entity.skill.ranged.CraftMyPetProjectile;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;

public class MyPetHook implements Listener {

    @EventHandler
    public void onEntityDamageByPet(EntityDamageEvent e) {
        if (e.isCancelled()) {
            return;
        }

        Entity ent = e.getEntity();
        if (ent instanceof MyPetBukkitEntity) {
            Region r = RedProtect.get().getRegionManager().getTopRegion(ent.getLocation());
            if (r != null && r.flagExists("invincible")) {
                if (r.getFlagBool("invincible")) {
                    e.setCancelled(true);
                    ((MyPetBukkitEntity) ent).forgetTarget();
                }
            }
        }

        if (e instanceof EntityDamageByEntityEvent de) {
            Entity e1 = de.getEntity();
            Entity e2 = de.getDamager();

            Location loc = e1.getLocation();
            Region r1 = RedProtect.get().getRegionManager().getTopRegion(loc);
            if (r1 == null) {
                return;
            }

            if (e1 instanceof Animals || e1 instanceof Villager || e1 instanceof Golem) {
                Entity damager = e2;
                if (damager instanceof CraftMyPetProjectile) {
                    damager = ((CraftMyPetProjectile) damager).getMyPetProjectile().getShooter().getBukkitEntity();
                }
                if (damager instanceof MyPetBukkitEntity mp2) {
                    Player p2 = mp2.getOwner().getPlayer();
                    LivingEntity liv = (LivingEntity) e1;
                    if (!r1.canBuild(p2) || !r1.canInteractPassives(p2)) {
                        e.setCancelled(true);
                        mp2.getMyPet().removePet();
                        for (PotionEffect ef : liv.getActivePotionEffects()) {
                            liv.removePotionEffect(ef.getType());
                        }
                        p2.sendMessage(RedProtect.get().getLanguageManager().get("mplistener.cantattack.passives"));
                        return;
                    }
                }
            }

            if (e1 instanceof Player) {
                Entity damager = e2;
                if (damager instanceof CraftMyPetProjectile) {
                    damager = ((CraftMyPetProjectile) damager).getMyPetProjectile().getShooter().getBukkitEntity();
                }
                if (damager instanceof MyPetBukkitEntity mp2) {
                    Player p2 = mp2.getOwner().getPlayer();
                    if (!r1.canPVP((Player) e1, p2)) {
                        e.setCancelled(true);
                        mp2.getMyPet().removePet();
                        for (PotionEffect ef : p2.getActivePotionEffects()) {
                            p2.removePotionEffect(ef.getType());
                        }
                        p2.sendMessage(RedProtect.get().getLanguageManager().get("mplistener.cantattack.players"));
                    }
                }
            }
        }
    }
}
