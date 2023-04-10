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
import br.net.fabiozumbi12.RedProtect.Bukkit.actions.EncompassRegionBuilder;
import br.net.fabiozumbi12.RedProtect.Bukkit.helpers.ContainerManager;
import br.net.fabiozumbi12.RedProtect.Bukkit.region.RegionBuilder;
import br.net.fabiozumbi12.RedProtect.Core.config.CoreConfigManager;
import br.net.fabiozumbi12.RedProtect.Core.helpers.LogLevel;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.world.StructureGrowEvent;

import java.util.ArrayList;
import java.util.List;

public class BlockListener implements Listener {

    private static final ContainerManager cont = new ContainerManager();

    public BlockListener() {
        RedProtect.get().logger.debug(LogLevel.BLOCKS, "Loaded BlockListener...");
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockFade(BlockFadeEvent e) {
        Region r = RedProtect.get().getRegionManager().getTopRegion(e.getBlock().getLocation());
        Material type = e.getBlock().getType();
        if (r != null && !r.blockTransform() && (
                type.name().contains("SNOW") ||
                        type.name().contains("ICE") ||
                        type.name().contains("FIRE") ||
                        type.name().contains("CORAL") ||
                        type.name().contains("POWDER")
        )) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDispenser(BlockDispenseEvent e) {
        if (RedProtect.get().getUtil().denyPotion(e.getItem(), e.getBlock().getWorld())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onSignPlace(SignChangeEvent e) {
        RedProtect.get().logger.debug(LogLevel.BLOCKS, "BlockListener - Is SignChangeEvent event!");

        Block b = e.getBlock();
        Player p = e.getPlayer();
        if (!RedProtect.get().getUtil().isRealPlayer(p)) {
            return;
        }

        Region signr = RedProtect.get().getRegionManager().getTopRegion(b.getLocation());
        if (signr != null && !signr.canSign(p)) {
            RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.cantinteract");
            e.setCancelled(true);
            return;
        }

        String[] lines = e.getLines();
        String line1 = lines[0];

        if (lines.length != 4) {
            this.setErrorSign(e, p, RedProtect.get().getLanguageManager().get("blocklistener.sign.wronglines"));
            return;
        }

        if (RedProtect.get().getConfigManager().configRoot().server_protection.sign_spy.enabled && !(lines[0].isEmpty() && lines[1].isEmpty() && lines[2].isEmpty() && lines[3].isEmpty())) {
            Bukkit.getConsoleSender().sendMessage(RedProtect.get().getLanguageManager().get("blocklistener.signspy.location").replace("{x}", "" + b.getX()).replace("{y}", "" + b.getY()).replace("{z}", "" + b.getZ()).replace("{world}", b.getWorld().getName()));
            Bukkit.getConsoleSender().sendMessage(RedProtect.get().getLanguageManager().get("blocklistener.signspy.player").replace("{player}", e.getPlayer().getName()));
            Bukkit.getConsoleSender().sendMessage(RedProtect.get().getLanguageManager().get("blocklistener.signspy.lines12").replace("{line1}", lines[0]).replace("{line2}", lines[1]));
            Bukkit.getConsoleSender().sendMessage(RedProtect.get().getLanguageManager().get("blocklistener.signspy.lines34").replace("{line3}", lines[2]).replace("{line4}", lines[3]));
            if (!RedProtect.get().getConfigManager().configRoot().server_protection.sign_spy.only_console) {
                for (Player play : Bukkit.getOnlinePlayers()) {
                    if (play.hasPermission("redprotect.signspy")/* && !play.equals(p)*/) {
                        play.sendMessage(RedProtect.get().getLanguageManager().get("blocklistener.signspy.location").replace("{x}", "" + b.getX()).replace("{y}", "" + b.getY()).replace("{z}", "" + b.getZ()).replace("{world}", b.getWorld().getName()));
                        play.sendMessage(RedProtect.get().getLanguageManager().get("blocklistener.signspy.player").replace("{player}", e.getPlayer().getName()));
                        play.sendMessage(RedProtect.get().getLanguageManager().get("blocklistener.signspy.lines12").replace("{line1}", lines[0]).replace("{line2}", lines[1]));
                        play.sendMessage(RedProtect.get().getLanguageManager().get("blocklistener.signspy.lines34").replace("{line3}", lines[2]).replace("{line4}", lines[3]));
                    }
                }
            }
        }

        if ((RedProtect.get().getConfigManager().configRoot().private_cat.use && b.getType().name().endsWith("WALL_SIGN"))) {
            boolean out = RedProtect.get().getConfigManager().configRoot().private_cat.allow_outside;
            if (cont.validatePrivateSign(e.getLines())) {

                if (out || signr != null) {
                    if (cont.isContainer(b)) {
                        // Check sides for other private signs
                        for (BlockFace face : BlockFace.values()) {
                            Block faceBlock = e.getBlock().getRelative(face);
                            if ((faceBlock.getState() instanceof Sign) && cont.validatePrivateSign(((Sign) faceBlock.getState()).getLines())) {
                                e.setLine(0, "Other Sign");
                                e.setLine(1, "NEAR");
                                return;
                            }
                        }

                        int length = p.getName().length();
                        if (length > 16) {
                            length = 16;
                        }
                        e.setLine(1, p.getName().substring(0, length));
                        RedProtect.get().getLanguageManager().sendMessage(p, "blocklistener.container.protected");
                    } else {
                        RedProtect.get().getLanguageManager().sendMessage(p, "blocklistener.container.notprotected");
                        b.breakNaturally();
                    }
                } else {
                    RedProtect.get().getLanguageManager().sendMessage(p, "blocklistener.container.notregion");
                    b.breakNaturally();
                }
                return;
            }
        }

        if (line1.equalsIgnoreCase("[rp]")) {
            String claimmode = RedProtect.get().getConfigManager().getWorldClaimType(p.getWorld().getName());
            if ((!claimmode.equalsIgnoreCase("BLOCK") && !claimmode.equalsIgnoreCase("BOTH")) && !p.hasPermission("redprotect.admin.create")) {
                this.setErrorSign(e, p, RedProtect.get().getLanguageManager().get("blocklistener.region.claimmode"));
                return;
            }

            RedProtect.get().getLanguageManager().sendMessage(p, "regionbuilder.creating");

            // Run claim async
            Sign sign = (Sign) e.getBlock().getState();
            Bukkit.getScheduler().runTaskAsynchronously(RedProtect.get(), () -> {
                RegionBuilder rb = new EncompassRegionBuilder(e);
                if (rb.ready()) {
                    Region r = rb.build();
                    Bukkit.getScheduler().callSyncMethod(RedProtect.get(), () -> {
                        sign.setLine(0, RedProtect.get().getLanguageManager().get("blocklistener.region.signcreated"));
                        sign.setLine(1, r.getName());
                        sign.update();
                        return true;
                    });
                    RedProtect.get().getRegionManager().add(r, r.getWorld());
                }
            });
        } else if (RedProtect.get().getConfigManager().configRoot().region_settings.enable_flag_sign && line1.equalsIgnoreCase("[flag]") && signr != null) {
            if (signr.getFlags().containsKey(lines[1])) {
                String flag = lines[1];
                if (!(signr.getFlags().get(flag) instanceof Boolean)) {
                    RedProtect.get().getLanguageManager().sendMessage(p, RedProtect.get().getLanguageManager().get("playerlistener.region.sign.cantflag"));
                    b.breakNaturally();
                    return;
                }
                if (RedProtect.get().getPermissionHandler().hasFlagPerm(p, flag) && (RedProtect.get().getConfigManager().configRoot().flags.containsKey(flag) || CoreConfigManager.ADMIN_FLAGS.contains(flag))) {
                    if (signr.isAdmin(p) || signr.isLeader(p) || RedProtect.get().getPermissionHandler().hasPerm(p, "redprotect.admin.flag." + flag)) {
                        e.setLine(1, flag);
                        e.setLine(2, ChatColor.DARK_AQUA + "" + ChatColor.BOLD + signr.getName());
                        e.setLine(3, ChatColor.translateAlternateColorCodes('&', RedProtect.get().getLanguageManager().get("region.value") + " " + RedProtect.get().getLanguageManager().translBool(signr.getFlagString(flag))));
                        RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.sign.placed");
                        RedProtect.get().getConfigManager().putSign(signr.getID(), b.getLocation());
                        return;
                    }
                }
                RedProtect.get().getLanguageManager().sendMessage(p, "cmdmanager.region.flag.nopermregion");
            } else {
                RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.sign.invalidflag");
            }
            b.breakNaturally();
        }
    }

    private void setErrorSign(SignChangeEvent e, Player p, String error) {
        e.setLine(0, RedProtect.get().getLanguageManager().get("regionbuilder.signerror"));
        RedProtect.get().getLanguageManager().sendMessage(p, RedProtect.get().getLanguageManager().get("regionbuilder.signerror") + ": " + error);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onFallBlockPlace(EntitySpawnEvent e) {
        if (e.getEntityType().equals(EntityType.FALLING_BLOCK)) {
            Region r = RedProtect.get().getRegionManager().getTopRegion(e.getLocation());
            if (r != null && !r.allowGravity()) {
                e.getEntity().remove();
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent e) {
        RedProtect.get().logger.debug(LogLevel.BLOCKS, "BlockListener - Is BlockPlaceEvent event!");

        Player p = e.getPlayer();
        if (!RedProtect.get().getUtil().isRealPlayer(p)) {
            return;
        }

        Block b = e.getBlockPlaced();
        World w = p.getWorld();
        Material m = e.getItemInHand().getType();

        boolean antih = RedProtect.get().getConfigManager().configRoot().region_settings.anti_hopper;
        Region r = RedProtect.get().getRegionManager().getTopRegion(b.getLocation());

        if (!RedProtect.get().getPermissionHandler().hasPerm(p, "redprotect.bypass") && antih &&
                (m.equals(Material.HOPPER) || m.name().contains("RAIL"))) {
            int x = b.getX();
            int y = b.getY();
            int z = b.getZ();
            Block ib = w.getBlockAt(x, y + 1, z);
            if (!cont.canBreak(p, ib) || !cont.canBreak(p, b)) {
                RedProtect.get().getLanguageManager().sendMessage(p, "blocklistener.container.chestinside");
                e.setCancelled(true);
                return;
            }
        }

        if (r == null && canPlaceList(p.getWorld(), b.getType().name())) {
            return;
        }

        if (r != null) {

            if (!r.canMinecart(p) && (m.name().contains("MINECART") || m.name().contains("BOAT"))) {
                RedProtect.get().getLanguageManager().sendMessage(p, "blocklistener.region.cantplace");
                e.setCancelled(true);
                return;
            }

            try {
                if ((b.getType().name().equals("MOB_SPAWNER") || b.getType().name().equals("SPAWNER")) && r.canPlaceSpawner(p)) {
                    return;
                }
            } catch (Exception ignored) {
            }

            if ((m.name().contains("_HOE") || r.canCrops(b, true)) && r.canCrops()) {
                return;
            }

            Material type = b.getType();
            if (!r.blockTransform() && type.isBlock() && (
                    type.name().contains("SNOW") ||
                            type.name().contains("ICE") ||
                            type.name().contains("CORAL") ||
                            type.name().contains("POWDER"))) {
                b.setType(m);
            }

            if (!r.canBuild(p) && !r.canPlace(b.getType())) {
                RedProtect.get().getLanguageManager().sendMessage(p, "blocklistener.region.cantbuild");
                e.setCancelled(true);
            }
        }
    }

    private boolean canPlaceList(World w, String type) {
        //blacklist
        List<String> blt = RedProtect.get().getConfigManager().globalFlagsRoot().worlds.get(w.getName()).if_build_false.place_blocks.blacklist;
        if (blt.stream().anyMatch(type::matches)) return false;

        //whitelist
        List<String> wlt = RedProtect.get().getConfigManager().globalFlagsRoot().worlds.get(w.getName()).if_build_false.place_blocks.whitelist;
        if (!wlt.isEmpty() && wlt.stream().noneMatch(type::matches)) {
            return false;
        }
        return RedProtect.get().getConfigManager().globalFlagsRoot().worlds.get(w.getName()).build;
    }

    private boolean canBreakList(World w, String type) {
        //blacklist
        List<String> blt = RedProtect.get().getConfigManager().globalFlagsRoot().worlds.get(w.getName()).if_build_false.break_blocks.blacklist;
        if (blt.stream().anyMatch(type::matches)) return false;

        //whitelist
        List<String> wlt = RedProtect.get().getConfigManager().globalFlagsRoot().worlds.get(w.getName()).if_build_false.break_blocks.whitelist;
        if (!wlt.isEmpty() && wlt.stream().noneMatch(type::matches)) {
            return false;
        }
        return RedProtect.get().getConfigManager().globalFlagsRoot().worlds.get(w.getName()).build;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent e) {
        RedProtect.get().logger.debug(LogLevel.BLOCKS, "BlockListener - Is BlockBreakEvent event!");

        Player p = e.getPlayer();
        if (!RedProtect.get().getUtil().isRealPlayer(p)) {
            return;
        }

        Block b = e.getBlock();

        boolean antih = RedProtect.get().getConfigManager().configRoot().region_settings.anti_hopper;
        Region r = RedProtect.get().getRegionManager().getTopRegion(b.getLocation());

        if (!RedProtect.get().getPermissionHandler().hasPerm(p, "redprotect.bypass")) {
            Block ib = b.getRelative(BlockFace.UP);
            if ((antih && !cont.canBreak(p, ib)) || !cont.canBreak(p, b)) {
                RedProtect.get().getLanguageManager().sendMessage(p, "blocklistener.container.breakinside");
                e.setCancelled(true);
                return;
            }
        }

        if (r == null && canBreakList(p.getWorld(), b.getType().name())) {
            return;
        }

        if (r != null && (b.getType().name().equals("MOB_SPAWNER") || b.getType().name().equals("SPAWNER")) && r.canPlaceSpawner(p)) {
            return;
        }

        if (r != null && r.canBuild(p) && b.getType().name().endsWith("_SIGN")) {
            Sign s = (Sign) b.getState();
            if (s.getLine(0).equalsIgnoreCase("[flag]")) {
                RedProtect.get().getConfigManager().removeSign(r.getID(), b.getLocation());
                return;
            }
        }

        if (r != null && !r.canBuild(p) && !r.canTree(b) && !r.canMining(b) && !r.canCrops(b, true) && !r.canBreak(b.getType())) {
            RedProtect.get().getLanguageManager().sendMessage(p, "blocklistener.region.cantbuild");
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent e) {
        RedProtect.get().logger.debug(LogLevel.BLOCKS, "BlockListener - Is PlayerInteractEvent event!");
        if (e.getClickedBlock() == null) return;

        Player p = e.getPlayer();
        if (!RedProtect.get().getUtil().isRealPlayer(p)) {
            return;
        }

        Location l = e.getClickedBlock().getLocation();
        Region r = RedProtect.get().getRegionManager().getTopRegion(l);

        Block b = p.getLocation().getBlock();
        if (r != null && (RedProtect.get().getUtil().checkCrops(b, false)
                || p.getInventory().getItemInHand().getType().name().contains("_HOE"))
                && !r.canCrops() && !r.canBuild(p)) {
            RedProtect.get().getLanguageManager().sendMessage(p, "blocklistener.region.cantbreak");
            e.setCancelled(true);
            return;
        }

        Block relative = e.getClickedBlock().getRelative(e.getBlockFace());
        if (relative.getType().equals(Material.FIRE)) {
            Region r1 = RedProtect.get().getRegionManager().getTopRegion(relative.getLocation());
            if (r1 != null && !r1.canBuild(e.getPlayer())) {
                e.setCancelled(true);
                RedProtect.get().getLanguageManager().sendMessage(e.getPlayer(), "playerlistener.region.cantinteract");
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void EntityBreak(EntityInteractEvent event) {
        if (event.getEntity() instanceof Player) return;

        Region r = RedProtect.get().getRegionManager().getTopRegion(event.getEntity().getLocation());
        if (r != null && !r.canMobLoot() && RedProtect.get().getUtil().checkCrops(event.getBlock(), false)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityExplode(EntityExplodeEvent e) {
        RedProtect.get().logger.debug(LogLevel.BLOCKS, "Is BlockListener - EntityExplodeEvent event");
        List<Block> toRemove = new ArrayList<>();

        Region or = RedProtect.get().getRegionManager().getTopRegion(e.getEntity().getLocation());
        for (Block b : e.blockList()) {
            if (b == null) {
                continue;
            }
            RedProtect.get().logger.debug(LogLevel.BLOCKS, "Blocks: " + b.getType().name());
            Location l = b.getLocation();
            Region r = RedProtect.get().getRegionManager().getTopRegion(l);
            if (r != null && !r.canFire() || !cont.canWorldBreak(b)) {
                RedProtect.get().logger.debug(LogLevel.BLOCKS, "canWorldBreak Called!");
                //e.setCancelled(true);
                toRemove.add(b);
                continue;
            }

            if (r == null) {
                continue;
            }

            if (r != or) {
                toRemove.add(b);
                continue;
            }

            if (e.getEntity() instanceof LivingEntity && !r.canMobLoot()) {
                toRemove.add(b);
            }
        }
        if (!toRemove.isEmpty()) {
            e.blockList().removeAll(toRemove);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFrameBrake(HangingBreakByEntityEvent e) {
        RedProtect.get().logger.debug(LogLevel.BLOCKS, "Is BlockListener - HangingBreakByEntityEvent event");

        Entity remover = e.getRemover();
        Location l = e.getEntity().getLocation();

        if (remover instanceof Monster || remover instanceof Projectile) {
            Region r = RedProtect.get().getRegionManager().getTopRegion(l);

            if (r != null) {
                if (remover instanceof Projectile && ((Projectile) remover).getShooter() instanceof Player player) {
                    if (!r.canBuild(player))
                        e.setCancelled(true);
                } else if (!r.canMobLoot())
                    e.setCancelled(true);
            }
        }

        if (remover instanceof Player) {
            Region r = RedProtect.get().getRegionManager().getTopRegion(l);
            if (r != null && !r.canBuild((Player) remover)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFrameBrake(HangingBreakEvent e) {
        RedProtect.get().logger.debug(LogLevel.BLOCKS, "Is BlockListener - HangingBreakEvent event");

        Entity ent = e.getEntity();
        Location l = ent.getLocation();

        if (e.getCause().toString().equals("PHYSICS") && (l.getBlock()
                .getRelative(e.getEntity().getAttachedFace()).getLocation().getBlock().isEmpty()))
            return;

        Region r = RedProtect.get().getRegionManager().getTopRegion(l);
        if (r != null) {
            if (e.getCause().toString().equals("EXPLOSION") && !r.canFire()) {
                e.setCancelled(true);
            } else if (e.getCause().toString().equals("PHYSICS")) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockStartBurn(BlockIgniteEvent e) {
        RedProtect.get().logger.debug(LogLevel.BLOCKS, "BlockListener - Is BlockIgniteEvent event");

        Block b = e.getBlock();
        Block bignit = e.getIgnitingBlock();
        if (b == null) {
            return;
        }

        Region r = RedProtect.get().getRegionManager().getTopRegion(b.getLocation());
        if (r != null && !r.canFire()) {
            if (e.getIgnitingEntity() != null) {
                if (e.getIgnitingEntity() instanceof Player p) {
                    if (!r.canBuild(p)) {
                        RedProtect.get().getLanguageManager().sendMessage(p, "blocklistener.region.cantplace");
                        e.setCancelled(true);
                        return;
                    }
                } else {
                    e.setCancelled(true);
                    return;
                }
            }

            if (bignit != null && (bignit.getType().equals(Material.FIRE) || bignit.getType().name().contains("LAVA"))) {
                e.setCancelled(true);
                return;
            }
            if (e.getCause().equals(IgniteCause.LIGHTNING) || e.getCause().equals(IgniteCause.EXPLOSION) || e.getCause().equals(IgniteCause.FIREBALL)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent e) {
        RedProtect.get().logger.debug(LogLevel.BLOCKS, "BlockListener - Is BlockBurnEvent event");

        Block b = e.getBlock();

        Region r = RedProtect.get().getRegionManager().getTopRegion(b.getLocation());
        if (r != null && !r.canFire()) {
            e.setCancelled(true);
            return;
        }

        if (!cont.canWorldBreak(b)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFlow(BlockFromToEvent e) {
        RedProtect.get().logger.debug(LogLevel.BLOCKS, "BlockListener - Is BlockFromToEvent event");

        Block bto = e.getToBlock();
        Block bfrom = e.getBlock();
        RedProtect.get().logger.debug(LogLevel.BLOCKS, "BlockListener - Is BlockFromToEvent event is to " + bto.getType().name() + " from " + bfrom.getType().name());
        Region rto = RedProtect.get().getRegionManager().getTopRegion(bto.getLocation());
        Region rfrom = RedProtect.get().getRegionManager().getTopRegion(bfrom.getLocation());
        boolean isLiquid = bfrom.isLiquid() || bfrom.getType().name().contains("BUBBLE_COLUMN") || bfrom.getType().name().contains("KELP");
        if (rto != null && isLiquid && !rto.canFlow()) {
            e.setCancelled(true);
            return;
        }
        if (rfrom != null && isLiquid && !rfrom.canFlow()) {
            e.setCancelled(true);
            return;
        }
        if (rto != null && !bto.isEmpty() && !rto.canFlowDamage()) {
            e.setCancelled(true);
            return;
        }

        //deny blocks spread in/out regions
        if (rfrom != null && rto != null && rfrom != rto && !rfrom.sameLeaders(rto)) {
            e.setCancelled(true);
            return;
        }
        if (rfrom == null && rto != null) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onLightning(LightningStrikeEvent e) {
        RedProtect.get().logger.debug(LogLevel.BLOCKS, "BlockListener - Is LightningStrikeEvent event");
        Location l = e.getLightning().getLocation();
        Region r = RedProtect.get().getRegionManager().getTopRegion(l);
        if (r != null && !r.canFire()) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFireSpread(BlockSpreadEvent e) {
        RedProtect.get().logger.debug(LogLevel.BLOCKS, "BlockListener - Is BlockSpreadEvent event");

        Block bfrom = e.getSource();
        Block bto = e.getBlock();
        RedProtect.get().logger.debug(LogLevel.BLOCKS, "Is BlockSpreadEvent event, source is " + bfrom.getType().name());
        Region rfrom = RedProtect.get().getRegionManager().getTopRegion(bfrom.getLocation());
        Region rto = RedProtect.get().getRegionManager().getTopRegion(bto.getLocation());
        if ((e.getNewState().getType().equals(Material.FIRE) || e.getNewState().getType().name().contains("LAVA")) && rfrom != null && !rfrom.canFire()) {
            e.setCancelled(true);
            return;
        }

        if ((e.getNewState().getType().equals(Material.VINE) ||
                e.getNewState().getType().name().contains("SEAGRASS") ||
                e.getNewState().getType().name().contains("MUSHROOM") ||
                e.getNewState().getType().name().contains("KELP") ||
                e.getNewState().getType().name().contains("BAMBOO") ||
                e.getNewState().getType().name().contains("WEEPING_VINES") ||
                e.getNewState().getType().name().contains("TWISTING_VINES") ||
                e.getNewState().getType().name().contains("SUGAR_CANE"))
                && ((rfrom != null && !rfrom.canGrow()) || (rto != null && !rto.canGrow()))) {
            e.setCancelled(true);
            return;
        }

        //deny blocks spread in/out regions
        if (rfrom != null && rto != null && rfrom != rto && !rfrom.sameLeaders(rto)) {
            e.setCancelled(true);
            return;
        }
        if (rfrom == null && rto != null) {
            e.setCancelled(true);
            return;
        }
        if (rfrom != null && rto == null) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent e) {
        RedProtect.get().logger.debug(LogLevel.BLOCKS, "BlockListener - Is StructureGrowEvent event");
        if (!RedProtect.get().getConfigManager().configRoot().region_settings.deny_structure_bypass_regions) {
            return;
        }
        Region rfrom = RedProtect.get().getRegionManager().getTopRegion(e.getLocation());
        for (BlockState bstt : e.getBlocks()) {
            Region rto = RedProtect.get().getRegionManager().getTopRegion(bstt.getLocation());
            Block bloc = bstt.getLocation().getBlock();
            //deny blocks spread in/out regions
            if (rfrom != null && rto != null && rfrom != rto && !rfrom.sameLeaders(rto)) {
                bstt.setType(bloc.getType());
            }
            if (rfrom == null && rto != null) {
                bstt.setType(bloc.getType());
            }
            if (rfrom != null && rto == null) {
                bstt.setType(bloc.getType());
            }
            bstt.update();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onVehicleBreak(VehicleDestroyEvent e) {
        RedProtect.get().logger.debug(LogLevel.BLOCKS, "BlockListener - Is VehicleDestroyEvent event");

        if (!(e.getAttacker() instanceof Player p)) {
            return;
        }
        Vehicle cart = e.getVehicle();
        if (!RedProtect.get().getUtil().isRealPlayer(p)) {
            return;
        }

        Region r = RedProtect.get().getRegionManager().getTopRegion(cart.getLocation());

        if (r != null && !r.canMinecart(p)) {
            RedProtect.get().getLanguageManager().sendMessage(p, "blocklistener.region.cantbreak");
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent e) {
        RedProtect.get().logger.debug(LogLevel.BLOCKS, "BlockListener - Is BlockPistonExtendEvent event");
        if (RedProtect.get().getConfigManager().configRoot().performance.disable_PistonEvent_handler) {
            return;
        }

        Block piston = e.getBlock();
        List<Block> blocks = e.getBlocks();
        Region pr = RedProtect.get().getRegionManager().getTopRegion(piston.getLocation());
        boolean antih = RedProtect.get().getConfigManager().configRoot().region_settings.anti_hopper;
        World w = e.getBlock().getWorld();
        for (Block b : blocks) {
            RedProtect.get().logger.debug(LogLevel.BLOCKS, "BlockPistonExtendEvent event - Block: " + b.getType().name());
            RedProtect.get().logger.debug(LogLevel.BLOCKS, "BlockPistonExtendEvent event - Relative: " + b.getRelative(e.getDirection()).getType().name());
            Region br = RedProtect.get().getRegionManager().getTopRegion(b.getRelative(e.getDirection()).getLocation());
            if (pr == null && br != null || (pr != null && br != null && pr != br && !pr.sameLeaders(br))) {
                e.setCancelled(true);
                return;
            }
            if (antih) {
                int x = b.getX();
                int y = b.getY();
                int z = b.getZ();
                Block ib = w.getBlockAt(x, y + 1, z);
                if (!cont.canWorldBreak(ib) || !cont.canWorldBreak(b)) {
                    e.setCancelled(true);
                    return;
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler(ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent e) {
        RedProtect.get().logger.debug(LogLevel.BLOCKS, "BlockListener - Is BlockPistonRetractEvent event");
        if (RedProtect.get().getConfigManager().configRoot().performance.disable_PistonEvent_handler) {
            return;
        }

        World w = e.getBlock().getWorld();
        boolean antih = RedProtect.get().getConfigManager().configRoot().region_settings.anti_hopper;
        Block piston = e.getBlock();
        if (!Bukkit.getBukkitVersion().startsWith("1.8.") && !Bukkit.getBukkitVersion().startsWith("1.9.")) {
            Block b = e.getRetractLocation().getBlock();
            RedProtect.get().logger.debug(LogLevel.BLOCKS, "BlockPistonRetractEvent not 1.8 event - Block: " + b.getType().name());
            Region pr = RedProtect.get().getRegionManager().getTopRegion(piston.getLocation());
            Region br = RedProtect.get().getRegionManager().getTopRegion(b.getLocation());
            if (pr == null && br != null || (pr != null && br != null && pr != br && !pr.sameLeaders(br))) {
                e.setCancelled(true);
                return;
            }
            if (antih) {
                int x = b.getX();
                int y = b.getY();
                int z = b.getZ();
                Block ib = w.getBlockAt(x, y + 1, z);
                if (!cont.canWorldBreak(ib) || !cont.canWorldBreak(b)) {
                    e.setCancelled(true);
                }
            }
        } else {
            List<Block> blocks = e.getBlocks();
            Region pr = RedProtect.get().getRegionManager().getTopRegion(piston.getLocation());
            for (Block b : blocks) {
                RedProtect.get().logger.debug(LogLevel.BLOCKS, "BlockPistonRetractEvent 1.8 event - Block: " + b.getType().name());
                Region br = RedProtect.get().getRegionManager().getTopRegion(b.getLocation());
                if (pr == null && br != null || (pr != null && br != null && pr != br && !pr.sameLeaders(br))) {
                    e.setCancelled(true);
                    return;
                }
                if (antih) {
                    int x = b.getX();
                    int y = b.getY();
                    int z = b.getZ();
                    Block ib = w.getBlockAt(x, y + 1, z);
                    if (!cont.canWorldBreak(ib) || !cont.canWorldBreak(b)) {
                        e.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onLeafDecay(LeavesDecayEvent e) {
        RedProtect.get().logger.debug(LogLevel.BLOCKS, "BlockListener - Is LeavesDecayEvent event");

        Region r = RedProtect.get().getRegionManager().getTopRegion(e.getBlock().getLocation());
        if (r != null && !r.leavesDecay()) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockGrow(BlockGrowEvent event) {
        RedProtect.get().logger.debug(LogLevel.BLOCKS, "BlockListener - Is BlockGrowEvent event: " + event.getNewState().getType().name());

        Region r = RedProtect.get().getRegionManager().getTopRegion(event.getBlock().getLocation());
        if (r != null && !r.canGrow()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockForm(BlockFormEvent event) {
        RedProtect.get().logger.debug(LogLevel.BLOCKS, "BlockListener - Is Blockform event!");

        BlockState b = event.getNewState();
        Block oldState = event.getBlock();
        if (b == null) {
            return;
        }

        Region r = RedProtect.get().getRegionManager().getTopRegion(b.getLocation());

        if (r != null && !r.blockTransform() && (
                oldState.getType().name().contains("SNOW") ||
                        oldState.getType().name().contains("ICE") ||
                        oldState.getType().name().contains("FIRE") ||
                        oldState.getType().name().contains("CORAL") ||
                        oldState.getType().name().contains("POWDER")
        )) {
            event.setCancelled(true);
            // Force update block
            oldState.getState().update(false, false);
            return;
        }

        if (b.getType().equals(Material.SNOW) || b.getType().equals(Material.ICE)) {
            r = RedProtect.get().getRegionManager().getTopRegion(b.getLocation());
            if (r != null && !r.canIceForm()) {
                event.setCancelled(true);
            }
        }
    }
}
