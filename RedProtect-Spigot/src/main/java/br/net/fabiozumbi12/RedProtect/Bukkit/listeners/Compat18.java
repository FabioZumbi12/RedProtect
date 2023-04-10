/*
 * Copyright (c) 2020 - @FabioZumbi12
 * Last Modified: 02/07/2020 19:01.
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

package br.net.fabiozumbi12.RedProtect.Bukkit.listeners;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import br.net.fabiozumbi12.RedProtect.Bukkit.helpers.ContainerManager;
import br.net.fabiozumbi12.RedProtect.Core.helpers.LogLevel;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class Compat18 implements Listener {

    private static final ContainerManager cont = new ContainerManager();

    public Compat18() {
        RedProtect.get().logger.debug(LogLevel.DEFAULT, "Loaded Compat18...");
    }

    @EventHandler(ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {

        Entity e = event.getEntity();

        //spawn arms on armor stands
        if (e instanceof ArmorStand as && RedProtect.get().getConfigManager().configRoot().hooks.armor_stand_arms) {
            as.setArms(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onAttemptInteractAS(PlayerInteractAtEntityEvent e) {

        Entity ent = e.getRightClicked();
        Location l = ent.getLocation();
        Region r = RedProtect.get().getRegionManager().getTopRegion(l);
        Player p = e.getPlayer();
        if (r == null) {
            //global flags
            if (ent instanceof ArmorStand) {
                if (!RedProtect.get().getConfigManager().globalFlagsRoot().worlds.get(l.getWorld().getName()).build) {
                    e.setCancelled(true);
                    return;
                }
            }
            return;
        }

        if (ent instanceof ArmorStand) {
            if (!r.canBuild(p)) {
                RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.cantedit");
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void entityFire(EntityCombustByEntityEvent e) {

        Entity e1 = e.getEntity();
        Entity e2 = e.getCombuster();
        Location loc = e1.getLocation();

        if (e2 instanceof Projectile a) {
            if (a.getShooter() instanceof Entity) {
                e2 = (Entity) a.getShooter();
            }
            if (e2 == null) {
                return;
            }
        }

        Region r1 = RedProtect.get().getRegionManager().getTopRegion(loc);

        if (r1 == null) {
            //global flags
            if (e1 instanceof ArmorStand && e2 instanceof Player) {
                if (!RedProtect.get().getConfigManager().globalFlagsRoot().worlds.get(loc.getWorld().getName()).build) {
                    e.setCancelled(true);
                }
            }
        } else {
            if (e1 instanceof ArmorStand && e2 instanceof Player) {
                if (!r1.canBuild(((Player) e2)) && !r1.canBreak(e1.getType())) {
                    e.setCancelled(true);
                    RedProtect.get().getLanguageManager().sendMessage(e2, "blocklistener.region.cantbreak");
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageByPet(EntityDamageByEntityEvent e) {

        Entity e1 = e.getEntity();
        Entity e2 = e.getDamager();
        Location loc = e1.getLocation();

        if (e2 instanceof Projectile a) {
            if (a.getShooter() instanceof Entity) {
                e2 = (Entity) a.getShooter();
            }
            if (e2 == null) {
                return;
            }
        }

        Region r1 = RedProtect.get().getRegionManager().getTopRegion(loc);

        if (r1 == null) {
            //global flags
            if (e1 instanceof ArmorStand && e2 instanceof Player) {
                if (!RedProtect.get().getConfigManager().globalFlagsRoot().worlds.get(loc.getWorld().getName()).build) {
                    e.setCancelled(true);
                }
            }
        } else {
            if (e1 instanceof ArmorStand) {
                if (e2 instanceof Player) {
                    if (!r1.canBuild(((Player) e2)) && !r1.canBreak(e1.getType())) {
                        e.setCancelled(true);
                        RedProtect.get().getLanguageManager().sendMessage(e2, "blocklistener.region.cantbreak");
                    }
                } else {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteractAS(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null) {
            return;
        }

        Player p = e.getPlayer();
        Location l = e.getClickedBlock().getLocation();
        Region r = RedProtect.get().getRegionManager().getTopRegion(l);
        Material m = p.getItemInHand().getType();

        if (e.getItem() != null) {
            m = e.getItem().getType();
        }

        if (m.equals(Material.ARMOR_STAND) || m.equals(Material.END_CRYSTAL)) {
            if (r != null && !r.canBuild(p) && !r.canPlace(m) && !r.canBreak(m)) {
                e.setCancelled(true);
                RedProtect.get().getLanguageManager().sendMessage(p, "blocklistener.region.cantbuild");
            }
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent e) {
        RedProtect.get().logger.debug(LogLevel.DEFAULT, "Is BlockListener - BlockExplodeEvent event");
        List<Block> toRemove = new ArrayList<>();
        for (Block b : e.blockList()) {
            Region r = RedProtect.get().getRegionManager().getTopRegion(b.getLocation());
            if (!cont.canWorldBreak(b)) {
                toRemove.add(b);
                continue;
            }
            if (r != null && !r.canFire()) {
                toRemove.add(b);
            }
        }
        if (!toRemove.isEmpty()) {
            e.blockList().removeAll(toRemove);
        }
    }
}
