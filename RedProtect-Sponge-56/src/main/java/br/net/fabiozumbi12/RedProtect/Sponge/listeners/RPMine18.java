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

package br.net.fabiozumbi12.RedProtect.Sponge.listeners;

import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import br.net.fabiozumbi12.RedProtect.Sponge.Region;
import br.net.fabiozumbi12.RedProtect.Sponge.config.RPLang;
import br.net.fabiozumbi12.RedProtect.Sponge.helpers.LogLevel;
import br.net.fabiozumbi12.RedProtect.Sponge.helpers.RPContainer;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class RPMine18 {

    static final RPContainer cont = new RPContainer();

    public RPMine18() {
        RedProtect.get().logger.debug(LogLevel.DEFAULT, "Loaded RPMine18...");
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onAttemptInteractAS(InteractEntityEvent e, @First Player p) {

        Entity ent = e.getTargetEntity();
        Location<World> l = ent.getLocation();
        Region r = RedProtect.get().rm.getTopRegion(l, this.getClass().getName());

        if (r == null) {
            //global flags
            if (ent.getType().equals(EntityTypes.ARMOR_STAND)) {
                if (!RedProtect.get().config.gFlags().worlds.get(l.getExtent().getName()).build) {
                    e.setCancelled(true);
                    return;
                }
            }
            return;
        }

        ItemType itemInHand = RedProtect.get().getPVHelper().getItemInHand(p);

        if (!itemInHand.equals(ItemTypes.NONE) && itemInHand.getType().equals(ItemTypes.ARMOR_STAND)) {
            if (!r.canBuild(p)) {
                e.setCancelled(true);
                RPLang.sendMessage(p, "blocklistener.region.cantbuild");
                return;
            }
        }

        //TODO Not working!
        if (ent.getType().equals(EntityTypes.ARMOR_STAND)) {
            if (!r.canBuild(p)) {
                if (!RedProtect.get().ph.hasPerm(p, "redprotect.bypass")) {
                    RPLang.sendMessage(p, "playerlistener.region.cantedit");
                    e.setCancelled(true);
                }
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onEntityDamage(DamageEntityEvent e, @First Entity e2) {

        Entity e1 = e.getTargetEntity();
        Location<World> loc = e1.getLocation();

        Player damager = null;
        if (e2 instanceof Projectile) {
            Projectile proj = (Projectile) e2;
            if (proj.getShooter() instanceof Player) {
                damager = (Player) proj.getShooter();
            }
        } else if (e2 instanceof Player) {
            damager = (Player) e2;
        }

        Region r1 = RedProtect.get().rm.getTopRegion(loc, this.getClass().getName());

        if (r1 == null) {
            //global flags
            if (e1 instanceof ArmorStand) {
                if (e2 instanceof Player) {
                    if (!RedProtect.get().config.gFlags().worlds.get(loc.getExtent().getName()).build) {
                        e.setCancelled(true);
                        return;
                    }
                }
            }
            return;
        }

        if (e1 instanceof ArmorStand) {
            if (!r1.canBuild(damager)) {
                e.setCancelled(true);
                RPLang.sendMessage(damager, "blocklistener.region.cantbreak");
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockExplode(ExplosionEvent.Detonate e) {
        RedProtect.get().logger.debug(LogLevel.DEFAULT, "Is BlockListener - BlockExplodeEvent event");

        for (Location<World> bex : e.getAffectedLocations()) {
            Region r = RedProtect.get().rm.getTopRegion(bex, this.getClass().getName());
            if (!cont.canWorldBreak(bex.createSnapshot())) {
                e.setCancelled(true);
                return;
            }
            if (r != null && !r.canFire()) {
                e.setCancelled(true);
                return;
            }
        }
    }
}
