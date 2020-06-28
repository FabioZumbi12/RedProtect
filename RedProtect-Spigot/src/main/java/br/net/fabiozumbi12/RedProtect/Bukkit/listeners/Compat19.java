/*
 * Copyright (c) 2019 - @FabioZumbi12
 * Last Modified: 25/04/19 07:02
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
import br.net.fabiozumbi12.RedProtect.Core.helpers.LogLevel;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TippedArrow;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import java.util.List;

public class Compat19 implements Listener {

    public Compat19() {
        RedProtect.get().logger.debug(LogLevel.DEFAULT, "Loaded Compat19...");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onGliding(EntityToggleGlideEvent event) {
        if(event.getEntity().hasMetadata("swimming") || event.getEntity().hasMetadata("falling")) return;

        if (event.getEntity() instanceof Player && event.isGliding()) {
            Player p = (Player) event.getEntity();
            Region r = RedProtect.get().rm.getTopRegion(p.getLocation());
            if (r == null) {
                if (!RedProtect.get().config.globalFlagsRoot().worlds.get(p.getWorld().getName()).player_glide.allow_glide) {
                    event.setCancelled(true);
                    RedProtect.get().lang.sendMessage(p, "globallistener.elytra.cantglide");
                }
            } else if (!r.canFly(p) && !RedProtect.get().ph.hasPermOrBypass(p, "redprotect.flag.admin.allow-fly")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();

        // Glide options
        if (!p.hasPermission("redprotect.bypass.glide")) {
            if (!RedProtect.get().config.globalFlagsRoot().worlds.get(p.getWorld().getName()).player_glide.allow_elytra) {
                ItemStack item = p.getInventory().getChestplate();
                if (item != null && item.getType().equals(Material.ELYTRA)) {
                    PlayerInventory inv = p.getInventory();
                    inv.setChestplate(new ItemStack(Material.AIR));
                    if (inv.firstEmpty() == -1) {
                        p.getWorld().dropItem(p.getLocation(), item);
                    } else {
                        inv.setItem(inv.firstEmpty(), item);
                    }
                    p.playSound(p.getLocation(), Sound.ENTITY_ITEM_PICKUP, 10, 1);
                    RedProtect.get().lang.sendMessage(p, "globallistener.elytra.cantequip");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        Block b = event.getClickedBlock();
        ItemStack itemInHand = event.getItem();

        Location l;

        if (b != null) {
            l = b.getLocation();
            RedProtect.get().logger.debug(LogLevel.DEFAULT, "PlayerListener - Is PlayerInteractEvent event. The block is " + b.getType().name());
        } else {
            l = p.getLocation();
        }

        if (RedProtect.get().tpWait.contains(p.getName())) {
            RedProtect.get().tpWait.remove(p.getName());
            RedProtect.get().lang.sendMessage(p, "cmdmanager.region.tpcancelled");
        }

        if (itemInHand != null && (event.getAction().name().equals("RIGHT_CLICK_BLOCK") || b == null)) {
            Material hand = itemInHand.getType();
            Region r = RedProtect.get().rm.getTopRegion(l);
            // Deny chorus teleport
            if (r != null && hand.equals(Material.CHORUS_FRUIT) && !r.canTeleport(p)) {
                RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantuse");
                event.setCancelled(true);
                event.setUseItemInHand(Event.Result.DENY);
            }
            // Deny glide boost
            if (r == null && p.isGliding() && itemInHand.getType().name().contains("FIREWORK") && !p.hasPermission("redprotect.bypass.glide") &&
                    !RedProtect.get().config.globalFlagsRoot().worlds.get(p.getWorld().getName()).player_glide.allow_boost) {
                event.setUseItemInHand(Event.Result.DENY);
                event.setCancelled(true);
                RedProtect.get().lang.sendMessage(p, "globallistener.elytra.cantboost");
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        // Deny arrow booster
        Region r = RedProtect.get().rm.getTopRegion(e.getEntity().getLocation());
        if (r == null && e.getEntity() instanceof Player && e.getDamager() instanceof Arrow) {
            Player p = (Player) e.getEntity();
            Arrow arrow = (Arrow)e.getDamager();
            if (arrow.getShooter() instanceof Player && p.isGliding()) {
                if (arrow.getShooter().equals(p) && !p.hasPermission("redprotect.bypass.glide") &&
                        !RedProtect.get().config.globalFlagsRoot().worlds.get(p.getWorld().getName()).player_glide.allow_boost) {
                    e.setCancelled(true);
                    arrow.remove();
                    RedProtect.get().lang.sendMessage(p, "globallistener.elytra.cantboost");
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent e) {

        final Player p = e.getPlayer();
        Location lfrom = e.getFrom();
        Location lto = e.getTo();

        if (e.getCause().equals(PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT)) {
            final Region rfrom = RedProtect.get().rm.getTopRegion(lfrom);
            final Region rto = RedProtect.get().rm.getTopRegion(lto);

            if (rfrom != null && !rfrom.canTeleport(p)) {
                RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantuse");
                e.setCancelled(true);
            }
            if (rto != null && !rto.canTeleport(p)) {
                RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantuse");
                e.setCancelled(true);
            }
        }

        if (p.getInventory().getChestplate() != null &&
                p.getInventory().getChestplate().getType().equals(Material.ELYTRA) &&
                !RedProtect.get().config.globalFlagsRoot().worlds.get(lto.getWorld().getName()).player_glide.allow_elytra) {
            RedProtect.get().lang.sendMessage(p, "globallistener.elytra.cantworld");
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onShootBow(EntityShootBowEvent e) {
        if (!(e.getEntity() instanceof Player)) {
            return;
        }

        Player p = (Player) e.getEntity();
        Entity proj = e.getProjectile();
        List<String> Pots = RedProtect.get().config.globalFlagsRoot().worlds.get(p.getWorld().getName()).deny_potions;

        if ((proj instanceof TippedArrow)) {
            TippedArrow arr = (TippedArrow) proj;
            if (Pots.contains(arr.getBasePotionData().getType().name())) {
                RedProtect.get().lang.sendMessage(p, "playerlistener.denypotion");
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onLingerPotion(LingeringPotionSplashEvent e) {
        if (!(RedProtect.get().getVersionHelper().getPlayerLingPot(e) instanceof Player)) {
            return;
        }

        Player p = (Player) RedProtect.get().getVersionHelper().getPlayerLingPot(e);
        Entity ent = RedProtect.get().getVersionHelper().getEntLingPot(e);

        RedProtect.get().logger.debug(LogLevel.DEFAULT, "Is LingeringPotionSplashEvent event.");

        Region r = RedProtect.get().rm.getTopRegion(ent.getLocation());
        if (r != null && !r.canGetEffects(p)) {
            RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantuse");
            e.setCancelled(true);
            return;
        }

        if (RedProtect.get().getVersionHelper().denyEntLingPot(e, e.getEntity().getWorld())) {
            e.setCancelled(true);
            RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("playerlistener.denypotion"));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent e) {
        if (e.getItem() == null) {
            return;
        }

        Player p = e.getPlayer();
        //deny potion
        if (p == null) {
            return;
        }

        Region r = RedProtect.get().rm.getTopRegion(p.getLocation());
        if (r != null && e.getItem().getType().equals(Material.CHORUS_FRUIT) && !r.canTeleport(p)) {
            RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantuse");
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onChangeBlock(EntityChangeBlockEvent e) {

        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            Block b = e.getBlock();
            Region r = RedProtect.get().rm.getTopRegion(b.getLocation());
            if (r != null && !r.canBuild(p)) {
                RedProtect.get().lang.sendMessage(p, "blocklistener.region.cantbreak");
                e.setCancelled(true);
            }
        }
    }
}
