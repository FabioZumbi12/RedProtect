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
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.Crops;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("deprecation")
public class GlobalListener implements Listener {

    private final HashMap<World, Integer> rainCounter = new HashMap<>();

    public GlobalListener() {
        RedProtect.get().logger.debug(LogLevel.DEFAULT, "Loaded GlobalListener...");
    }

    /**
     * @param p   - Player
     * @param b   - Block
     * @param fat - 1 = Place Block | 2 = Break Block
     * @return Boolean - Can build or not.
     */
    private boolean bypassBuild(Player p, Block b, int fat) {
        if (p.hasPermission("redprotect.bypass.world"))
            return true;

        if (RedProtect.get().config.needClaimToBuild(p, b))
            return false;

        return (fat == 1 && canPlaceList(p.getWorld(), b.getType().name())) ||
                (fat == 2 && canBreakList(p.getWorld(), b.getType().name())) ||
                RedProtect.get().config.globalFlagsRoot().worlds.get(p.getWorld().getName()).build;
    }

    private boolean canPlaceList(World w, String type) {
        if (!RedProtect.get().config.globalFlagsRoot().worlds.get(w.getName()).build) {
            //blacklist
            List<String> blt = RedProtect.get().config.globalFlagsRoot().worlds.get(w.getName()).if_build_false.place_blocks.blacklist;
            if (!blt.isEmpty()) return blt.stream().noneMatch(type::matches);

            //whitelist
            List<String> wlt = RedProtect.get().config.globalFlagsRoot().worlds.get(w.getName()).if_build_false.place_blocks.whitelist;
            if (!wlt.isEmpty()) return wlt.stream().anyMatch(type::matches);

            return false;
        }
        return true;
    }

    private boolean canBreakList(World w, String type) {
        if (!RedProtect.get().config.globalFlagsRoot().worlds.get(w.getName()).build) {
            //blacklist
            List<String> blt = RedProtect.get().config.globalFlagsRoot().worlds.get(w.getName()).if_build_false.break_blocks.blacklist;
            if (!blt.isEmpty()) return blt.stream().noneMatch(type::matches);

            //whitelist
            List<String> wlt = RedProtect.get().config.globalFlagsRoot().worlds.get(w.getName()).if_build_false.break_blocks.whitelist;
            if (!wlt.isEmpty()) return wlt.stream().anyMatch(type::matches);

            return false;
        }
        return true;
    }

    private boolean canInteractBlocksList(World w, String type) {
        if (!RedProtect.get().config.globalFlagsRoot().worlds.get(w.getName()).interact) {
            //blacklist
            List<String> blt = RedProtect.get().config.globalFlagsRoot().worlds.get(w.getName()).if_interact_false.interact_blocks.blacklist;
            if (!blt.isEmpty()) return blt.stream().noneMatch(type::matches);

            //whitelist
            List<String> wlt = RedProtect.get().config.globalFlagsRoot().worlds.get(w.getName()).if_interact_false.interact_blocks.whitelist;
            if (!wlt.isEmpty()) return wlt.stream().anyMatch(type::matches);

            return false;
        }
        return true;
    }

    private boolean canInteractEntitiesList(World w, String type) {
        if (!RedProtect.get().config.globalFlagsRoot().worlds.get(w.getName()).interact) {
            //blacklist
            List<String> blt = RedProtect.get().config.globalFlagsRoot().worlds.get(w.getName()).if_interact_false.interact_entities.blacklist;
            if (!blt.isEmpty()) return blt.stream().noneMatch(type::matches);

            //whitelist
            List<String> wlt = RedProtect.get().config.globalFlagsRoot().worlds.get(w.getName()).if_interact_false.interact_entities.whitelist;
            if (!wlt.isEmpty()) return wlt.stream().anyMatch(type::matches);

            return false;
        }
        return true;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onWeatherChange(WeatherChangeEvent e) {
        RedProtect.get().logger.debug(LogLevel.DEFAULT, "GlobalListener - Is onChangeWeather event");

        World w = e.getWorld();
        if (!RedProtect.get().config.globalFlagsRoot().worlds.get(w.getName()).weather.allow_weather && !e.toWeatherState()) {
            e.setCancelled(true);
        }

        int attempts = RedProtect.get().config.globalFlagsRoot().worlds.get(w.getName()).weather.attempts_before_rain;
        if (e.toWeatherState()) {
            if (!rainCounter.containsKey(w)) {
                rainCounter.put(w, attempts);
                e.setCancelled(true);
            } else {
                int acTry = rainCounter.get(w);
                if (acTry - 1 <= 0) {
                    Bukkit.getScheduler().runTaskLater(RedProtect.get(), () -> w.setWeatherDuration(RedProtect.get().config.globalFlagsRoot().worlds.get(w.getName()).weather.rain_time * 20), 40);
                    rainCounter.put(w, attempts);
                } else {
                    rainCounter.put(w, acTry - 1);
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onLeafDecay(LeavesDecayEvent e) {
        RedProtect.get().logger.debug(LogLevel.DEFAULT, "BlockListener - Is LeavesDecayEvent event");
        Region r = RedProtect.get().rm.getTopRegion(e.getBlock().getLocation());
        if (r == null && !RedProtect.get().config.globalFlagsRoot().worlds.get(e.getBlock().getWorld().getName()).allow_changes_of.leaves_decay) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFlow(BlockFromToEvent e) {
        RedProtect.get().logger.debug(LogLevel.DEFAULT, "GlobalListener - Is BlockFromToEvent event");

        Block b = e.getToBlock();
        Block bfrom = e.getBlock();
        RedProtect.get().logger.debug(LogLevel.DEFAULT, "GlobalListener - Is BlockFromToEvent event is to " + b.getType().name() + " from " + bfrom.getType().name());
        Region r = RedProtect.get().rm.getTopRegion(b.getLocation());
        if (r != null) {
            return;
        }
        if (bfrom.isLiquid() && !RedProtect.get().config.globalFlagsRoot().worlds.get(b.getWorld().getName()).allow_changes_of.liquid_flow) {
            e.setCancelled(true);
            return;
        }

        if ((bfrom.getType().equals(Material.WATER) || (bfrom.getType().name().contains("WATER") && (bfrom.getType().name().contains("STATIONARY") || bfrom.getType().name().contains("FLOWING"))))
                && !RedProtect.get().config.globalFlagsRoot().worlds.get(b.getWorld().getName()).allow_changes_of.water_flow) {
            e.setCancelled(true);
            return;
        }

        if ((bfrom.getType().equals(Material.LAVA) || (bfrom.getType().name().contains("LAVA") && (bfrom.getType().name().contains("STATIONARY") || bfrom.getType().name().contains("FLOWING"))))
                && !RedProtect.get().config.globalFlagsRoot().worlds.get(b.getWorld().getName()).allow_changes_of.lava_flow) {
            e.setCancelled(true);
            return;
        }

        if (!b.isEmpty() && !RedProtect.get().config.globalFlagsRoot().worlds.get(b.getWorld().getName()).allow_changes_of.flow_damage) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent e) {
        Region r = RedProtect.get().rm.getTopRegion(e.getEntity().getLocation());
        if (r != null) {
            return;
        }
        Entity ent = e.getEntity();
        if (ent instanceof LivingEntity && !(ent instanceof Monster)) {
            if (RedProtect.get().config.globalFlagsRoot().worlds.get(ent.getWorld().getName()).invincible) {
                if (ent instanceof Animals) {
                    ((Animals) ent).setTarget(null);
                }
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void PlayerDropItem(PlayerDropItemEvent e) {
        Location l = e.getItemDrop().getLocation();
        Player p = e.getPlayer();
        Region r = RedProtect.get().rm.getTopRegion(l);

        if (r == null && !RedProtect.get().config.globalFlagsRoot().worlds.get(p.getWorld().getName()).player_candrop) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void PlayerPickup(PlayerPickupItemEvent e) {
        Location l = e.getItem().getLocation();
        Player p = e.getPlayer();
        Region r = RedProtect.get().rm.getTopRegion(l);

        if (r == null && !RedProtect.get().config.globalFlagsRoot().worlds.get(p.getWorld().getName()).player_canpickup) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerFrostWalk(EntityBlockFormEvent e) {
        Region r = RedProtect.get().rm.getTopRegion(e.getBlock().getLocation());
        if (r != null) {
            return;
        }
        RedProtect.get().logger.debug(LogLevel.DEFAULT, "GlobalListener - EntityBlockFormEvent canceled? ");
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (!RedProtect.get().config.globalFlagsRoot().worlds.get(p.getWorld().getName()).iceform_by.player && !p.hasPermission("redprotect.bypass.world")) {
                e.setCancelled(true);
            }
        } else if (!RedProtect.get().config.globalFlagsRoot().worlds.get(e.getEntity().getWorld().getName()).iceform_by.entity) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();

        //set velocities
        if (!p.hasPermission("redprotect.bypass.velocity")) {
            if (RedProtect.get().config.globalFlagsRoot().worlds.get(p.getWorld().getName()).player_velocity.walk_speed >= 0) {
                p.setWalkSpeed(RedProtect.get().config.globalFlagsRoot().worlds.get(p.getWorld().getName()).player_velocity.walk_speed);
            }
            if (RedProtect.get().config.globalFlagsRoot().worlds.get(p.getWorld().getName()).player_velocity.fly_speed >= 0) {
                p.setFlySpeed(RedProtect.get().config.globalFlagsRoot().worlds.get(p.getWorld().getName()).player_velocity.fly_speed);
            }
        }

        if (RedProtect.get().bukkitVersion >= 191) {
            if (!RedProtect.get().config.globalFlagsRoot().worlds.get(p.getWorld().getName()).allow_elytra) {
                ItemStack item = p.getInventory().getChestplate();
                if (item != null && item.getType().name().equals("ELYTRA")) {
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

        if (RedProtect.get().config.globalFlagsRoot().worlds.get(p.getWorld().getName()).border.deny_bypass && RedProtect.get().getUtil().isBypassBorder(p)) {
            RedProtect.get().lang.sendMessage(p, "globallistener.border.cantbypass");

            boolean result = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), RedProtect.get().config.globalFlagsRoot().worlds.get(p.getWorld().getName()).border.execute_command
                    .replace("{player}", p.getName()));

            // If not success, send to spawn
            if (!result)
                Bukkit.getScheduler().runTaskLater(RedProtect.get(), () -> e.setTo(p.getWorld().getSpawnLocation()), 1);
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent e) {
        Player p = e.getPlayer();

        if (RedProtect.get().bukkitVersion >= 190) {
            Location to = e.getTo();
            if (p.getInventory().getChestplate() != null &&
                    p.getInventory().getChestplate().getType().name().equals("ELYTRA") &&
                    !RedProtect.get().config.globalFlagsRoot().worlds.get(to.getWorld().getName()).allow_elytra) {
                RedProtect.get().lang.sendMessage(p, "globallistener.elytra.cantworld");
                e.setCancelled(true);
            }
        }

        RedProtect.get().logger.debug(LogLevel.DEFAULT, "TeleportCause: " + e.getCause().name());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        RedProtect.get().logger.debug(LogLevel.DEFAULT, "GlobalListener - Is BlockPlaceEvent event!");

        Block b = e.getBlock();
        Player p = e.getPlayer();
        Material item = e.getItemInHand().getType();
        Region r = RedProtect.get().rm.getTopRegion(e.getBlock().getLocation());
        if (r != null) {
            return;
        }

        if (!RedProtect.get().getUtil().canBuildNear(p, b.getLocation())) {
            e.setCancelled(true);
            return;
        }

        if (item.name().contains("MINECART") || item.name().contains("BOAT")) {
            if (!RedProtect.get().config.globalFlagsRoot().worlds.get(p.getWorld().getName()).use_minecart && !p.hasPermission("redprotect.bypass.world")) {
                e.setCancelled(true);
                RedProtect.get().logger.debug(LogLevel.DEFAULT, "GlobalListener - Can't place minecart/boat!");
            }
        } else {
            if (!bypassBuild(p, b, 1)) {
                e.setCancelled(true);
                RedProtect.get().logger.debug(LogLevel.DEFAULT, "GlobalListener - Can't Build!");
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        RedProtect.get().logger.debug(LogLevel.DEFAULT, "GlobalListener - Is BlockBreakEvent event!");
        Block b = e.getBlock();
        Player p = e.getPlayer();
        Region r = RedProtect.get().rm.getTopRegion(b.getLocation());
        if (r != null) {
            return;
        }

        if (!RedProtect.get().getUtil().canBuildNear(p, b.getLocation())) {
            e.setCancelled(true);
            return;
        }

        if (!bypassBuild(p, b, 2)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent e) {
        RedProtect.get().logger.debug(LogLevel.DEFAULT, "GlobalListener - Is PlayerInteractEvent event!");

        Player p = e.getPlayer();
        Block b = e.getClickedBlock();
        ItemStack itemInHand = e.getItem();
        Location l;

        if (b != null) {
            l = b.getLocation();
            RedProtect.get().logger.debug(LogLevel.DEFAULT, "GlobalListener - Is PlayerInteractEvent event. The block is " + b.getType().name());
        } else {
            l = p.getLocation();
        }

        if (b != null && b.getState() instanceof Sign) {
            Sign s = (Sign) b.getState();
            if (ChatColor.stripColor(s.getLine(1)).equals(ChatColor.stripColor(RedProtect.get().lang.get("_redprotect.prefix")))) {
                b.setType(Material.AIR);
                e.setUseInteractedBlock(Result.DENY);
                e.setUseItemInHand(Result.DENY);
                e.setCancelled(true);
                return;
            }
        }

        Region r = RedProtect.get().rm.getTopRegion(l);

        //deny item usage
        List<String> items = RedProtect.get().config.globalFlagsRoot().worlds.get(p.getWorld().getName()).deny_item_usage.items;
        if (e.getItem() != null && items.stream().anyMatch(e.getItem().getType().name()::matches)) {
            if (r != null && ((!RedProtect.get().config.globalFlagsRoot().worlds.get(p.getWorld().getName()).deny_item_usage.allow_on_claimed_rps && r.canBuild(p)) ||
                    (RedProtect.get().config.globalFlagsRoot().worlds.get(p.getWorld().getName()).deny_item_usage.allow_on_claimed_rps && !r.canBuild(p)))) {
                RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantuse");
                e.setUseInteractedBlock(Event.Result.DENY);
                e.setUseItemInHand(Event.Result.DENY);
                e.setCancelled(true);
                return;
            }
            if (r == null && !RedProtect.get().config.globalFlagsRoot().worlds.get(p.getWorld().getName()).deny_item_usage.allow_on_wilderness && !RedProtect.get().ph.hasPerm(p, "redprotect.bypass.world")) {
                RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantuse");
                e.setUseInteractedBlock(Event.Result.DENY);
                e.setUseItemInHand(Event.Result.DENY);
                e.setCancelled(true);
                return;
            }
        }

        if (b == null || r != null) {
            return;
        }

        if ((b instanceof Crops
                || b.getType().equals(Material.PUMPKIN_STEM)
                || b.getType().equals(Material.MELON_STEM)
                || b.getType().toString().contains("CROPS")
                || b.getType().toString().contains("SOIL")
                || b.getType().toString().contains("CHORUS_")
                || b.getType().toString().contains("BEETROOT_")
                || b.getType().toString().contains("SUGAR_CANE")) &&
                !RedProtect.get().config.globalFlagsRoot().worlds.get(p.getWorld().getName()).allow_crop_trample && !p.hasPermission("redprotect.bypass.world")) {
            e.setCancelled(true);
            return;
        }

        if (b.getType().equals(Material.DRAGON_EGG) ||
                b.getType().name().equalsIgnoreCase("BED") ||
                b.getType().name().contains("NOTE_BLOCK") ||
                b.getType().name().contains("CAKE")) {

            if ((!canBreakList(p.getWorld(), b.getType().name())
                    || !canInteractBlocksList(p.getWorld(), b.getType().name())) &&
                    !bypassBuild(p, null, 0)) {
                RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantinteract");
                e.setCancelled(true);
                return;
            }
        }

        if (itemInHand != null) {
            if (itemInHand.getType().name().startsWith("BOAT") || itemInHand.getType().name().contains("MINECART")) {
                if (!RedProtect.get().config.globalFlagsRoot().worlds.get(p.getWorld().getName()).use_minecart && !p.hasPermission("redprotect.bypass.world")) {
                    e.setUseItemInHand(Event.Result.DENY);
                    e.setCancelled(true);
                    return;
                }
            }
            if (itemInHand.getType().equals(Material.PAINTING) || itemInHand.getType().equals(Material.ITEM_FRAME) || itemInHand.getType().name().equals("ARMOR_STAND")) {
                if (canPlaceList(p.getWorld(), itemInHand.getType().name()) && !bypassBuild(p, null, 0)) {
                    e.setUseItemInHand(Event.Result.DENY);
                    e.setCancelled(true);
                    return;
                }
            }
        }

        if (!RedProtect.get().config.globalFlagsRoot().worlds.get(p.getWorld().getName()).interact && !p.hasPermission("redprotect.bypass.world")) {
            if (canInteractBlocksList(p.getWorld(), b.getType().name())) {
                return;
            }
            e.setUseItemInHand(Event.Result.DENY);
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEntityEvent e) {
        Player p = e.getPlayer();
        Entity ent = e.getRightClicked();
        Location l = ent.getLocation();
        Region r = RedProtect.get().rm.getTopRegion(l);
        if (r != null) {
            return;
        }

        if (ent instanceof ItemFrame || ent instanceof Painting) {
            if (!bypassBuild(p, null, 0)) {
                e.setCancelled(true);
                return;
            }
        }

        if (ent instanceof Minecart || ent instanceof Boat) {
            if (!RedProtect.get().config.globalFlagsRoot().worlds.get(l.getWorld().getName()).use_minecart && !p.hasPermission("redprotect.bypass.world")) {
                e.setCancelled(true);
                return;
            }
        }

        if (!RedProtect.get().config.globalFlagsRoot().worlds.get(l.getWorld().getName()).interact && !p.hasPermission("redprotect.bypass.world") && (!(ent instanceof Player))) {
            if (canInteractEntitiesList(p.getWorld(), ent.getType().name())) {
                return;
            }
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onHangingDamaged(HangingBreakByEntityEvent e) {
        Entity ent = e.getRemover();
        Location loc = e.getEntity().getLocation();
        Region r = RedProtect.get().rm.getTopRegion(loc);
        if (r != null) {
            return;
        }

        if (ent instanceof Player) {
            Player p = (Player) ent;
            if (!bypassBuild(p, null, 0)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucketUse(PlayerBucketEmptyEvent e) {
        Location l = e.getBlockClicked().getLocation();
        Region r = RedProtect.get().rm.getTopRegion(l);
        if (r != null) {
            return;
        }

        if (!RedProtect.get().getUtil().canBuildNear(e.getPlayer(), l)) {
            e.setCancelled(true);
            return;
        }

        if (!bypassBuild(e.getPlayer(), null, 0)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent e) {
        Location l = e.getBlockClicked().getLocation();
        Region r = RedProtect.get().rm.getTopRegion(l);
        if (r != null) {
            return;
        }

        if (!RedProtect.get().getUtil().canBuildNear(e.getPlayer(), l)) {
            e.setCancelled(true);
            return;
        }

        if (!bypassBuild(e.getPlayer(), null, 0)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageEntity(EntityDamageByEntityEvent e) {
        Entity e1 = e.getEntity();
        Entity e2 = e.getDamager();

        Location loc = e1.getLocation();
        Region r1 = RedProtect.get().rm.getTopRegion(loc);
        if (r1 != null) {
            return;
        }

        if (e2 instanceof Creeper || e2.getType().equals(EntityType.PRIMED_TNT) || e2.getType().equals(EntityType.MINECART_TNT)) {
            if (e1 instanceof Player) {
                if (!RedProtect.get().config.globalFlagsRoot().worlds.get(loc.getWorld().getName()).explosion_entity_damage) {
                    e.setCancelled(true);
                    return;
                }
            }
            if (e1 instanceof Animals || e1 instanceof Villager || e1 instanceof Golem) {
                if (!RedProtect.get().config.globalFlagsRoot().worlds.get(loc.getWorld().getName()).explosion_entity_damage) {
                    e.setCancelled(true);
                    return;
                }
            }
            if (e1 instanceof Monster) {
                if (!RedProtect.get().config.globalFlagsRoot().worlds.get(loc.getWorld().getName()).explosion_entity_damage) {
                    e.setCancelled(true);
                    return;
                }
            }
        }

        if (e2 instanceof Player) {
            Player p = (Player) e2;

            if (e.getCause().equals(DamageCause.LIGHTNING) || e.getCause().equals(DamageCause.BLOCK_EXPLOSION) || e.getCause().equals(DamageCause.ENTITY_EXPLOSION)) {
                if (!RedProtect.get().config.globalFlagsRoot().worlds.get(loc.getWorld().getName()).entity_block_damage) {
                    e.setCancelled(true);
                    return;
                }
            }
            if ((e1 instanceof Minecart || e1 instanceof Boat) && !RedProtect.get().config.globalFlagsRoot().worlds.get(loc.getWorld().getName()).use_minecart && !p.hasPermission("redprotect.bypass.world")) {
                e.setCancelled(true);
                return;
            }
            if (e1 instanceof Player) {
                if (!RedProtect.get().config.globalFlagsRoot().worlds.get(loc.getWorld().getName()).pvp && !p.hasPermission("redprotect.bypass.world")) {
                    e.setCancelled(true);
                    return;
                }
            }
            if (e1 instanceof Animals || e1 instanceof Villager || e1 instanceof Golem) {
                if (!RedProtect.get().config.globalFlagsRoot().worlds.get(loc.getWorld().getName()).player_hurt_passives && !p.hasPermission("redprotect.bypass.world")) {
                    e.setCancelled(true);
                    return;
                }
            }
            if (e1 instanceof Monster) {
                if (!RedProtect.get().config.globalFlagsRoot().worlds.get(loc.getWorld().getName()).player_hurt_monsters && !p.hasPermission("redprotect.bypass.world")) {
                    e.setCancelled(true);
                    return;
                }
            }
            if (e1 instanceof Hanging || e1 instanceof EnderCrystal || e1.getType().name().contains("ARMOR_STAND")) {
                if (!canBreakList(p.getWorld(), e1.getType().name()) && !bypassBuild(p, null, 0)) {
                    e.setCancelled(true);
                    return;
                }
            }
        }

        if (e2 instanceof Projectile) {
            Projectile proj = (Projectile) e2;
            if (proj.getShooter() instanceof Player) {
                Player p = (Player) proj.getShooter();

                if (e1 instanceof Player) {
                    if (!RedProtect.get().config.globalFlagsRoot().worlds.get(loc.getWorld().getName()).pvp && !p.hasPermission("redprotect.bypass.world")) {
                        e.setCancelled(true);
                        return;
                    }
                }
                if (e1 instanceof Animals || e1 instanceof Villager || e1 instanceof Golem) {
                    if (!RedProtect.get().config.globalFlagsRoot().worlds.get(loc.getWorld().getName()).player_hurt_passives && !p.hasPermission("redprotect.bypass.world")) {
                        e.setCancelled(true);
                        return;
                    }
                }
                if (e1 instanceof Monster) {
                    if (!RedProtect.get().config.globalFlagsRoot().worlds.get(loc.getWorld().getName()).player_hurt_monsters && !p.hasPermission("redprotect.bypass.world")) {
                        e.setCancelled(true);
                        return;
                    }
                }
                if (e1 instanceof Hanging || e1 instanceof EnderCrystal || e1.getType().name().contains("ARMOR_STAND")) {
                    if (!canBreakList(p.getWorld(), e1.getType().name()) && !bypassBuild(p, null, 0)) {
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFrameBrake(HangingBreakEvent e) {
        Location l = e.getEntity().getLocation();
        Region r = RedProtect.get().rm.getTopRegion(l);
        if (r != null) {
            return;
        }

        if (e.getCause().toString().equals("EXPLOSION")) {
            if (!RedProtect.get().config.globalFlagsRoot().worlds.get(l.getWorld().getName()).entity_block_damage) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent e) {
        List<Block> toRemove = new ArrayList<>();
        for (Block b : e.blockList()) {
            Location l = b.getLocation();
            Region r = RedProtect.get().rm.getTopRegion(l);
            if (r == null && !RedProtect.get().config.globalFlagsRoot().worlds.get(l.getWorld().getName()).entity_block_damage) {
                toRemove.add(b);
            }
        }
        if (!toRemove.isEmpty()) {
            e.blockList().removeAll(toRemove);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent e) {
        Block b = e.getBlock();
        Region r = RedProtect.get().rm.getTopRegion(b.getLocation());
        if (r != null) {
            return;
        }

        if (!RedProtect.get().config.globalFlagsRoot().worlds.get(b.getWorld().getName()).fire_block_damage) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFireSpread(BlockSpreadEvent e) {
        Block b = e.getSource();
        Region r = RedProtect.get().rm.getTopRegion(b.getLocation());
        if (r != null) {
            return;
        }

        if ((b.getType().equals(Material.FIRE) || b.getType().name().contains("LAVA")) && !RedProtect.get().config.globalFlagsRoot().worlds.get(b.getWorld().getName()).fire_spread) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        RedProtect.get().logger.debug(LogLevel.DEFAULT, "GlobalListener - Is CreatureSpawnEvent event!");

        Entity e = event.getEntity();
        if (e == null) {
            return;
        }

        Location l = event.getLocation();
        Region r = RedProtect.get().rm.getTopRegion(l);
        if (r != null && RedProtect.get().config.globalFlagsRoot().worlds.get(e.getWorld().getName()).spawn_allow_on_regions) {
            return;
        }

        //blacklist
        List<String> blacklist = RedProtect.get().config.globalFlagsRoot().worlds.get(e.getWorld().getName()).spawn_blacklist;
        if (e instanceof Monster && blacklist.contains("MONSTERS")) {
            event.setCancelled(true);
            return;
        }
        if ((e instanceof Animals || e instanceof Villager || e instanceof Golem || e instanceof Ambient || e instanceof WaterMob) && blacklist.contains("PASSIVES")) {
            event.setCancelled(true);
            return;
        }
        if (blacklist.stream().anyMatch(e.getType().name()::matches)) {
            event.setCancelled(true);
            return;
        }

        //whitelist
        List<String> wtl = RedProtect.get().config.globalFlagsRoot().worlds.get(e.getWorld().getName()).spawn_whitelist;
        if (!wtl.isEmpty()) {
            if (e instanceof Monster && !wtl.contains("MONSTERS")) {
                event.setCancelled(true);
                return;
            }
            if ((e instanceof Animals || e instanceof Villager || e instanceof Golem || e instanceof Ambient || e instanceof WaterMob) && !wtl.contains("PASSIVES")) {
                event.setCancelled(true);
                return;
            }
            if (wtl.stream().noneMatch(e.getType().name()::matches)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onVehicleBreak(VehicleDestroyEvent e) {
        if (!(e.getAttacker() instanceof Player)) {
            return;
        }

        Vehicle cart = e.getVehicle();
        Player p = (Player) e.getAttacker();
        Region r = RedProtect.get().rm.getTopRegion(cart.getLocation());
        if (r != null) {
            return;
        }

        if (!RedProtect.get().config.globalFlagsRoot().worlds.get(p.getWorld().getName()).use_minecart && !p.hasPermission("redprotect.bypass.world")) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockStartBurn(BlockIgniteEvent e) {
        Block b = e.getBlock();
        Block bignit = e.getIgnitingBlock();
        if (b == null || bignit == null) {
            return;
        }
        RedProtect.get().logger.debug(LogLevel.DEFAULT, "Is BlockIgniteEvent event from global-listener");
        Region r = RedProtect.get().rm.getTopRegion(b.getLocation());
        if (r != null) {
            return;
        }
        if ((bignit.getType().equals(Material.FIRE) || bignit.getType().name().contains("LAVA")) && !RedProtect.get().config.globalFlagsRoot().worlds.get(b.getWorld().getName()).fire_spread) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void MonsterBlockBreak(EntityChangeBlockEvent event) {
        Entity e = event.getEntity();
        Block b = event.getBlock();
        Region r = RedProtect.get().rm.getTopRegion(event.getBlock().getLocation());
        if (r != null) {
            return;
        }

        if (b != null) {
            RedProtect.get().logger.debug(LogLevel.DEFAULT, "GlobalListener - Is EntityChangeBlockEvent event. Block: " + b.getType().name());
        }

        if (e instanceof Monster) {
            if (!RedProtect.get().config.globalFlagsRoot().worlds.get(e.getWorld().getName()).entity_block_damage) {
                event.setCancelled(true);
            }
        }
        if (e instanceof Player) {
            Player p = (Player) e;
            if (!bypassBuild(p, b, 2)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent e) {
        if (e.getItem() == null) {
            return;
        }

        Player p = e.getPlayer();
        Location l = p.getLocation();

        Region r = RedProtect.get().rm.getTopRegion(l);

        //deny item usage
        List<String> items = RedProtect.get().config.globalFlagsRoot().worlds.get(p.getWorld().getName()).deny_item_usage.items;
        if (e.getItem() != null && items.stream().anyMatch(e.getItem().getType().name()::matches)) {
            if (r != null && ((!RedProtect.get().config.globalFlagsRoot().worlds.get(p.getWorld().getName()).deny_item_usage.allow_on_claimed_rps && r.canBuild(p)) ||
                    (RedProtect.get().config.globalFlagsRoot().worlds.get(p.getWorld().getName()).deny_item_usage.allow_on_claimed_rps && !r.canBuild(p)))) {
                RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantuse");
                e.setCancelled(true);
                return;
            }
            if (r == null && !RedProtect.get().config.globalFlagsRoot().worlds.get(p.getWorld().getName()).deny_item_usage.allow_on_wilderness && !RedProtect.get().ph.hasPerm(p, "redprotect.bypass.world")) {
                RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantuse");
                e.setCancelled(true);
            }
        }
    }
}
