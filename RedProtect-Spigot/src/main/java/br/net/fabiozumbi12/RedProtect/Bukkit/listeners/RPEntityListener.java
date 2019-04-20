/*
 *  Copyright (c) 2019 - @FabioZumbi12
 *  Last Modified: 16/04/19 06:21
 *
 *  This class is provided 'as-is', without any express or implied warranty. In no event will the authors be held liable for any
 *   damages arising from the use of this class.
 *
 *  Permission is granted to anyone to use this class for any purpose, including commercial plugins, and to alter it and
 *  redistribute it freely, subject to the following restrictions:
 *  1 - The origin of this class must not be misrepresented; you must not claim that you wrote the original software. If you
 *  use this class in other plugins, an acknowledgment in the plugin documentation would be appreciated but is not required.
 *  2 - Altered source versions must be plainly marked as such, and must not be misrepresented as being the original class.
 *  3 - This notice may not be removed or altered from any source distribution.
 *
 *  Esta classe é fornecida "como está", sem qualquer garantia expressa ou implícita. Em nenhum caso os autores serão
 *  responsabilizados por quaisquer danos decorrentes do uso desta classe.
 *
 *  É concedida permissão a qualquer pessoa para usar esta classe para qualquer finalidade, incluindo plugins pagos, e para
 *  alterá-lo e redistribuí-lo livremente, sujeito às seguintes restrições:
 *  1 - A origem desta classe não deve ser deturpada; você não deve afirmar que escreveu a classe original. Se você usar esta
 *   classe em um plugin, uma confirmação de autoria na documentação do plugin será apreciada, mas não é necessária.
 *  2 - Versões de origem alteradas devem ser claramente marcadas como tal e não devem ser deturpadas como sendo a
 *  classe original.
 *  3 - Este aviso não pode ser removido ou alterado de qualquer distribuição de origem.
 */

package br.net.fabiozumbi12.RedProtect.Bukkit.listeners;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPLang;
import br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPContainer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

public class RPEntityListener implements Listener {

    static final RPContainer cont = new RPContainer();

    public RPEntityListener() {
        RedProtect.get().logger.debug("Loaded RPEntityListener...");
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerFrostWalk(EntityBlockFormEvent e) {
        if (e.getEntity() instanceof Player) {
            return;
        }
        RedProtect.get().logger.debug("RPEntityListener - EntityBlockFormEvent");
        Region r = RedProtect.get().rm.getTopRegion(e.getBlock().getLocation());
        if (r != null && !r.canIceForm()) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {

        Entity e = event.getEntity();
        if (e == null) {
            return;
        }

        RedProtect.get().logger.debug("Spawn monster " + event.getEntityType().name());

        if (e instanceof Wither && event.getSpawnReason().equals(SpawnReason.BUILD_WITHER)) {
            Location l = event.getLocation();
            Region r = RedProtect.get().rm.getTopRegion(l);
            if (r != null && !r.canSpawnWhiter()) {
                event.setCancelled(true);
                return;
            }
        }

        if (e instanceof Monster) {
            Location l = event.getLocation();
            Region r = RedProtect.get().rm.getTopRegion(l);
            if (r != null && !r.canSpawnMonsters()) {
                RedProtect.get().logger.debug("Cancelled spawn of monster " + event.getEntityType().name());
                event.setCancelled(true);
            }
        }

        if ((!(e instanceof Monster) && !(e instanceof Player)) && (RedProtect.get().version >= 180 && !(e instanceof ArmorStand)) && !(e instanceof Hanging)) {
            Location l = event.getLocation();
            Region r = RedProtect.get().rm.getTopRegion(l);
            if (r != null && !r.canSpawnPassives()) {
                RedProtect.get().logger.debug("Cancelled spawn of animal " + event.getEntityType().name());
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void entityFire(EntityCombustByEntityEvent e) {
        Entity e1 = e.getEntity();
        Entity e2 = e.getCombuster();

        if (e2 == null) {
            return;
        }

        RedProtect.get().logger.debug("EntityCombustByEntityEvent - Is EntityCombustByEntityEvent event.");


        if (e2 instanceof Projectile) {
            Projectile a = (Projectile) e2;
            if (a.getShooter() instanceof Entity) {
                e2 = (Entity) a.getShooter();
            }
            if (e2 == null) {
                return;
            }
        }

        Region r1 = RedProtect.get().rm.getTopRegion(e1.getLocation());
        Region r2 = RedProtect.get().rm.getTopRegion(e2.getLocation());

        if (r1 != null && !r1.canFire() && !(e2 instanceof Player)) {
            e.setCancelled(true);
            return;
        }

        if (e1 instanceof Player) {
            if (e2 instanceof Player && !e1.equals(e2)) {
                Player p2 = (Player) e2;
                if (r1 != null) {
                    if (r2 != null) {
                        if ((r1.flagExists("pvp") && !r1.canPVP((Player) e1, p2)) || (r1.flagExists("pvp") && !r2.canPVP((Player) e1, p2))) {
                            e.setCancelled(true);
                            RPLang.sendMessage(p2, "entitylistener.region.cantpvp");
                        }
                    } else if (r1.flagExists("pvp") && !r1.canPVP((Player) e1, p2)) {
                        e.setCancelled(true);
                        RPLang.sendMessage(p2, "entitylistener.region.cantpvp");
                    }
                } else if (r2 != null && r2.flagExists("pvp") && !r2.canPVP((Player) e1, p2)) {
                    e.setCancelled(true);
                    RPLang.sendMessage(p2, "entitylistener.region.cantpvp");
                }
            }
        } else if (e1 instanceof Animals || e1 instanceof Villager || e1 instanceof Golem || e1 instanceof WaterMob) {
            if (r1 != null && e2 instanceof Player) {
                Player p2 = (Player) e2;
                if (e1 instanceof WaterMob && r1.canFish(p2)) return;
                if (!r1.canInteractPassives(p2)) {
                    e.setCancelled(true);
                    RPLang.sendMessage(p2, "entitylistener.region.cantpassive");
                }
            }
        } else if (e1 instanceof Hanging && e2 instanceof Player) {
            Player p2 = (Player) e2;
            if (r1 != null && !r1.canBuild(p2) && !r1.canBreak(e1.getType())) {
                e.setCancelled(true);
                RPLang.sendMessage(p2, "playerlistener.region.cantuse");
                return;
            }
            if (r2 != null && !r2.canBuild(p2) && !r2.canBreak(e1.getType())) {
                e.setCancelled(true);
                RPLang.sendMessage(p2, "playerlistener.region.cantuse");
            }
        } else if (e1 instanceof Hanging && e2 instanceof Monster) {
            if (r1 != null || r2 != null) {
                RedProtect.get().logger.debug("Cancelled ItemFrame drop Item");
                e.setCancelled(true);
            }
        } else if (e2 instanceof Explosive) {
            if ((r1 != null && !r1.canFire()) || (r2 != null && !r2.canFire())) {
                e.setCancelled(true);
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent e) {

        Entity ent = e.getEntity();
        Region r = RedProtect.get().rm.getTopRegion(ent.getLocation());

        if (ent instanceof LivingEntity && !(ent instanceof Monster)) {
            if (r != null && r.flagExists("invincible")) {
                if (r.getFlagBool("invincible")) {
                    if (ent instanceof Animals) {
                        ((Animals) ent).setTarget(null);
                    }
                    e.setCancelled(true);
                }
            }
        }

        if (e instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent de = (EntityDamageByEntityEvent) e;

            Entity e1 = de.getEntity();
            Entity e2 = de.getDamager();

            if (e2 == null) {
                return;
            }

            RedProtect.get().logger.debug("RPEntityListener - Is EntityDamageByEntityEvent event. Damager: " + e2.getType().name());


            if (e2 instanceof Projectile) {
                Projectile a = (Projectile) e2;
                if (a.getShooter() instanceof Entity) {
                    e2 = (Entity) a.getShooter();
                }
                if (e2 == null) {
                    return;
                }
            }

            RedProtect.get().logger.debug("EntityDamageByEntityEvent event. Damager Player: " + e2.getType().name());
            RedProtect.get().logger.debug("Cause: " + de.getCause().name());

            Region r1 = RedProtect.get().rm.getTopRegion(e1.getLocation());
            Region r2 = RedProtect.get().rm.getTopRegion(e2.getLocation());

            if (de.getCause().equals(DamageCause.LIGHTNING) || de.getCause().equals(DamageCause.BLOCK_EXPLOSION) || de.getCause().equals(DamageCause.FIRE) || de.getCause().equals(DamageCause.WITHER) || de.getCause().equals(DamageCause.CUSTOM) || de.getCause().equals(DamageCause.ENTITY_EXPLOSION)) {
                if (r1 != null && !r1.canFire() && !(e2 instanceof Player)) {
                    e.setCancelled(true);
                    return;
                }
            }

            if (e1 instanceof Player) {
                if (e2 instanceof Player && !e1.equals(e2)) {
                    Player p2 = (Player) e2;
                    if (r1 != null) {
                        Material mp2 = p2.getItemInHand().getType();
                        if (RedProtect.get().version >= 190) {
                            if (p2.getInventory().getItemInMainHand() != null) {
                                mp2 = p2.getInventory().getItemInMainHand().getType();
                            } else {
                                mp2 = p2.getInventory().getItemInOffHand().getType();
                            }
                        }
                        if (mp2.equals(Material.EGG) && !r1.canProtectiles(p2)) {
                            e.setCancelled(true);
                            RPLang.sendMessage(p2, "playerlistener.region.cantuse");
                            return;
                        }
                        if (r2 != null) {
                            if (mp2.equals(Material.EGG) && !r2.canProtectiles(p2)) {
                                e.setCancelled(true);
                                RPLang.sendMessage(p2, "playerlistener.region.cantuse");
                                return;
                            }
                            if ((r1.flagExists("pvp") && !r1.canPVP((Player) e1, p2)) || (r1.flagExists("pvp") && !r2.canPVP((Player) e1, p2))) {
                                e.setCancelled(true);
                                RPLang.sendMessage(p2, "entitylistener.region.cantpvp");
                            }
                        } else if (r1.flagExists("pvp") && !r1.canPVP((Player) e1, p2)) {
                            e.setCancelled(true);
                            RPLang.sendMessage(p2, "entitylistener.region.cantpvp");
                        }
                    } else if (r2 != null && r2.flagExists("pvp") && !r2.canPVP((Player) e1, p2)) {
                        e.setCancelled(true);
                        RPLang.sendMessage(p2, "entitylistener.region.cantpvp");
                    }
                }
            } else if (e1 instanceof Animals || e1 instanceof Villager || e1 instanceof Golem || e1 instanceof WaterMob) {
                if (r1 != null && e2 instanceof Player) {
                    Player p2 = (Player) e2;
                    if (e1 instanceof WaterMob && r1.canFish(p2)) return;
                    if (!r1.canInteractPassives(p2)) {
                        e.setCancelled(true);
                        RPLang.sendMessage(p2, "entitylistener.region.cantpassive");
                    }
                }
            } else if (e1 instanceof Hanging && e2 instanceof Player) {
                Player p2 = (Player) e2;
                if (r1 != null && !r1.canBuild(p2) && !r1.canBreak(e1.getType())) {
                    e.setCancelled(true);
                    RPLang.sendMessage(p2, "playerlistener.region.cantuse");
                    return;
                }
                if (r2 != null && !r2.canBuild(p2) && !r2.canBreak(e1.getType())) {
                    e.setCancelled(true);
                    RPLang.sendMessage(p2, "playerlistener.region.cantuse");
                }
            } else if (e1 instanceof Hanging && e2 instanceof Monster) {
                if (r1 != null || r2 != null) {
                    RedProtect.get().logger.debug("Cancelled ItemFrame drop Item");
                    e.setCancelled(true);
                }
            } else if (e2 instanceof Explosive) {
                if ((r1 != null && !r1.canFire()) || (r2 != null && !r2.canFire())) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPotionSplash(PotionSplashEvent event) {
        RedProtect.get().logger.debug("RPEntityListener - Is PotionSplashEvent");

        ProjectileSource thrower = event.getPotion().getShooter();
        for (PotionEffect e : event.getPotion().getEffects()) {
            PotionEffectType t = e.getType();
            if (!t.equals(PotionEffectType.BLINDNESS) && !t.equals(PotionEffectType.CONFUSION) && !t.equals(PotionEffectType.HARM) && !t.equals(PotionEffectType.HUNGER) && !t.equals(PotionEffectType.POISON) && !t.equals(PotionEffectType.SLOW) && !t.equals(PotionEffectType.SLOW_DIGGING) && !t.equals(PotionEffectType.WEAKNESS) && !t.equals(PotionEffectType.WITHER)) {
                return;
            }
        }
        Player shooter;
        if (thrower instanceof Player) {
            shooter = (Player) thrower;
        } else {
            return;
        }
        for (Entity e2 : event.getAffectedEntities()) {
            Region r = RedProtect.get().rm.getTopRegion(e2.getLocation());
            if (event.getEntity() instanceof Player) {
                if (r != null && r.flagExists("pvp") && !r.canPVP((Player) event.getEntity(), shooter)) {
                    event.setCancelled(true);
                    return;
                }
            } else {
                if (r != null && !r.canInteractPassives(shooter)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteractEvent(PlayerInteractEntityEvent e) {
        RedProtect.get().logger.debug("RPEntityListener - Is PlayerInteractEntityEvent");

        Player p = e.getPlayer();
        if (p == null) {
            return;
        }
        Location l = e.getRightClicked().getLocation();
        Region r = RedProtect.get().rm.getTopRegion(l);
        Entity et = e.getRightClicked();
        if (r != null && !r.canInteractPassives(p) && (et instanceof Animals || et instanceof Villager || et instanceof Golem || (et instanceof WaterMob && !r.canFish(p)))) {
            if (et instanceof Tameable) {
                Tameable tam = (Tameable) et;
                if (tam.isTamed() && tam.getOwner() != null && tam.getOwner().getName().equals(p.getName())) {
                    return;
                }
            }
            e.setCancelled(true);
            RPLang.sendMessage(p, "entitylistener.region.cantinteract");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void WitherBlockBreak(EntityChangeBlockEvent event) {
        RedProtect.get().logger.debug("RPEntityListener - Is EntityChangeBlockEvent");

        Entity e = event.getEntity();
        if (e instanceof Monster) {
            Region r = RedProtect.get().rm.getTopRegion(event.getBlock().getLocation());
            if (!cont.canWorldBreak(event.getBlock())) {
                event.setCancelled(true);
                return;
            }
            if (r != null && !r.canMobLoot()) {
                event.setCancelled(true);
            }
        }
    }

}
