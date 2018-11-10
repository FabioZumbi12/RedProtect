/*
 Copyright @FabioZumbi12

 This class is provided 'as-is', without any express or implied warranty. In no event will the authors be held liable for any
  damages arising from the use of this software.

 Permission is granted to anyone to use this class for any purpose, including commercial plugins, and to alter it and
 redistribute it freely, subject to the following restrictions:
 1 - The origin of this class must not be misrepresented; you must not claim that you wrote the original software. If you
 use this class in other plugins, an acknowledgment in the plugin documentation would be appreciated but is not required.
 2 - Altered source versions must be plainly marked as such, and must not be misrepresented as being the original class.
 3 - This notice may not be removed or altered from any source distribution.

 Este software é fornecido "como está", sem qualquer garantia expressa ou implícita. Em nenhum caso os autores serão
 responsabilizados por quaisquer danos decorrentes do uso deste software.

 É concedida permissão a qualquer pessoa para usar esta classe para qualquer finalidade, incluindo plugins pagos, e para
 alterá-lo e redistribuí-lo livremente, sujeito às seguintes restrições:
 1 - A origem desta classe não deve ser deturpada; você não deve afirmar que escreveu a classe original. Se você usar esta
  classe em um plugin, uma confirmação de autoria na documentação do plugin será apreciada, mas não é necessária.
 2 - Versões de origem alteradas devem ser claramente marcadas como tal e não devem ser deturpadas como sendo a
 classe original.
 3 - Este aviso não pode ser removido ou alterado de qualquer distribuição de origem.
 */

package br.net.fabiozumbi12.RedProtect.Bukkit.listeners;

import br.net.fabiozumbi12.RedProtect.Bukkit.*;
import br.net.fabiozumbi12.RedProtect.Bukkit.actions.EncompassRegionBuilder;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPConfig;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPLang;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.material.Crops;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class RPBlockListener implements Listener {

    private static final RPContainer cont = new RPContainer();
    private final List<String> pistonExtendDelay = new ArrayList<>();
    private final List<String> pistonRetractDelay = new ArrayList<>();

    public RPBlockListener() {
        RedProtect.get().logger.debug("Loaded RPBlockListener...");
    }

    @EventHandler
    public void onDispenser(BlockDispenseEvent e) {
        if (RPUtil.denyPotion(e.getItem())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignPlace(SignChangeEvent e) {
        RedProtect.get().logger.debug("BlockListener - Is SignChangeEvent event! Cancelled? " + e.isCancelled());
        if (e.isCancelled()) {
            return;
        }

        Block b = e.getBlock();
        Player p = e.getPlayer();

        if (b == null) {
            this.setErrorSign(e, p, RPLang.get("blocklistener.block.null"));
            return;
        }

        Region signr = RedProtect.get().rm.getTopRegion(b.getLocation());

        if (signr != null && !signr.canSign(p)) {
            RPLang.sendMessage(p, "playerlistener.region.cantinteract");
            e.setCancelled(true);
            return;
        }

        String[] lines = e.getLines();
        String line1 = lines[0];

        if (lines.length != 4) {
            this.setErrorSign(e, p, RPLang.get("blocklistener.sign.wronglines"));
            return;
        }

        if (RPConfig.getBool("server-protection.sign-spy.enabled") && !(lines[0].isEmpty() && lines[1].isEmpty() && lines[2].isEmpty() && lines[3].isEmpty())) {
            Bukkit.getConsoleSender().sendMessage(RPLang.get("blocklistener.signspy.location").replace("{x}", "" + b.getX()).replace("{y}", "" + b.getY()).replace("{z}", "" + b.getZ()).replace("{world}", b.getWorld().getName()));
            Bukkit.getConsoleSender().sendMessage(RPLang.get("blocklistener.signspy.player").replace("{player}", e.getPlayer().getName()));
            Bukkit.getConsoleSender().sendMessage(RPLang.get("blocklistener.signspy.lines12").replace("{line1}", lines[0]).replace("{line2}", lines[1]));
            Bukkit.getConsoleSender().sendMessage(RPLang.get("blocklistener.signspy.lines34").replace("{line3}", lines[2]).replace("{line4}", lines[3]));
            if (!RPConfig.getBool("server-protection.sign-spy.only-console")) {
                for (Player play : Bukkit.getOnlinePlayers()) {
                    if (play.hasPermission("redprotect.signspy")/* && !play.equals(p)*/) {
                        play.sendMessage(RPLang.get("blocklistener.signspy.location").replace("{x}", "" + b.getX()).replace("{y}", "" + b.getY()).replace("{z}", "" + b.getZ()).replace("{world}", b.getWorld().getName()));
                        play.sendMessage(RPLang.get("blocklistener.signspy.player").replace("{player}", e.getPlayer().getName()));
                        play.sendMessage(RPLang.get("blocklistener.signspy.lines12").replace("{line1}", lines[0]).replace("{line2}", lines[1]));
                        play.sendMessage(RPLang.get("blocklistener.signspy.lines34").replace("{line3}", lines[2]).replace("{line4}", lines[3]));
                    }
                }
            }
        }

        if ((RPConfig.getBool("private.use") && b.getType().equals(Material.WALL_SIGN))) {
            Boolean out = RPConfig.getBool("private.allow-outside");
            if (cont.validatePrivateSign(e.getLines())) {
                if (out || signr != null) {
                    if (cont.isContainer(b)) {
                        int length = p.getName().length();
                        if (length > 16) {
                            length = 16;
                        }
                        e.setLine(1, p.getName().substring(0, length));
                        RPLang.sendMessage(p, "blocklistener.container.protected");
                        return;
                    } else {
                        RPLang.sendMessage(p, "blocklistener.container.notprotected");
                        b.breakNaturally();
                        return;
                    }
                } else {
                    RPLang.sendMessage(p, "blocklistener.container.notregion");
                    b.breakNaturally();
                    return;
                }
            }
        }

        if (line1.equalsIgnoreCase("[rp]")) {
            String claimmode = RPConfig.getWorldClaimType(p.getWorld().getName());
            if ((!claimmode.equalsIgnoreCase("BLOCK") && !claimmode.equalsIgnoreCase("BOTH")) && !p.hasPermission("redprotect.admin.create")) {
                this.setErrorSign(e, p, RPLang.get("blocklistener.region.claimmode"));
                return;
            }

            RegionBuilder rb = new EncompassRegionBuilder(e);
            if (rb.ready()) {
                Region r = rb.build();
                e.setLine(0, RPLang.get("blocklistener.region.signcreated"));
                e.setLine(1, r.getName());
                //RPLang.sendMessage(p, RPLang.get("blocklistener.region.created").replace("{region}",  r.getName()));                
                RedProtect.get().rm.add(r, RedProtect.get().serv.getWorld(r.getWorld()));
            }
        } else if (RPConfig.getBool("region-settings.enable-flag-sign") && line1.equalsIgnoreCase("[flag]") && signr != null) {
            if (signr.getFlags().containsKey(lines[1])) {
                String flag = lines[1];
                if (!(signr.getFlags().get(flag) instanceof Boolean)) {
                    RPLang.sendMessage(p, RPLang.get("playerlistener.region.sign.cantflag"));
                    b.breakNaturally();
                    return;
                }
                if (RedProtect.get().ph.hasPerm(p, "redprotect.flag." + flag)) {
                    if (signr.isAdmin(p) || signr.isLeader(p) || RedProtect.get().ph.hasPerm(p, "redprotect.admin.flag." + flag)) {
                        e.setLine(1, flag);
                        e.setLine(2, ChatColor.DARK_AQUA + "" + ChatColor.BOLD + signr.getName());
                        e.setLine(3, RPLang.get("region.value") + " " + RPLang.translBool(signr.getFlagString(flag)));
                        RPLang.sendMessage(p, "playerlistener.region.sign.placed");
                        RPConfig.putSign(signr.getID(), b.getLocation());
                        return;
                    }
                }
                RPLang.sendMessage(p, "cmdmanager.region.flag.nopermregion");
                b.breakNaturally();
            } else {
                RPLang.sendMessage(p, "playerlistener.region.sign.invalidflag");
                b.breakNaturally();
            }
        }
    }

    private void setErrorSign(SignChangeEvent e, Player p, String error) {
        e.setLine(0, RPLang.get("regionbuilder.signerror"));
        RPLang.sendMessage(p, RPLang.get("regionbuilder.signerror") + ": " + error);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent e) {
        RedProtect.get().logger.debug("BlockListener - Is BlockPlaceEvent event! Cancelled? " + e.isCancelled());
        if (e.isCancelled()) {
            return;
        }

        Player p = e.getPlayer();
        Block b = e.getBlockPlaced();
        World w = p.getWorld();
        Material m = null;
        if (e.getItemInHand() != null) {
            m = e.getItemInHand().getType();
        }

        Boolean antih = RPConfig.getBool("region-settings.anti-hopper");
        Region r = RedProtect.get().rm.getTopRegion(b.getLocation());

        if (!RedProtect.get().ph.hasPerm(p, "redprotect.bypass") && antih && m != null &&
                (m.equals(Material.HOPPER) || m.name().contains("RAIL"))) {
            int x = b.getX();
            int y = b.getY();
            int z = b.getZ();
            Block ib = w.getBlockAt(x, y + 1, z);
            if (!cont.canBreak(p, ib) || !cont.canBreak(p, b)) {
                RPLang.sendMessage(p, "blocklistener.container.chestinside");
                e.setCancelled(true);
                return;
            }
        }

        if (r == null && canPlaceList(p.getWorld(), b.getType().name())) {
            return;
        }

        if (r != null) {

            if (m != null && !r.canMinecart(p) && (m.name().contains("MINECART") || m.name().contains("BOAT"))) {
                RPLang.sendMessage(p, "blocklistener.region.cantplace");
                e.setCancelled(true);
                return;
            }

            if ((b.getType().name().equals("MOB_SPAWNER") || b.getType().name().equals("SPAWNER")) && r.allowSpawner(p)) {
                return;
            }

            if (!r.canBuild(p) && !r.canPlace(b.getType())) {
                RPLang.sendMessage(p, "blocklistener.region.cantbuild");
                e.setCancelled(true);
            }
        }
    }

    private boolean canPlaceList(World w, String type) {
        //blacklist
        List<String> blt = RPConfig.getGlobalFlagList(w.getName() + ".if-build-false.place-blocks.blacklist");
        if (blt.stream().anyMatch(type::matches)) return false;

        //whitelist
        List<String> wlt = RPConfig.getGlobalFlagList(w.getName() + ".if-build-false.place-blocks.whitelist");
        if (!wlt.isEmpty() && wlt.stream().noneMatch(type::matches)) {
            return false;
        }
        return RPConfig.getGlobalFlagBool(w.getName() + ".build");
    }

    private boolean canBreakList(World w, String type) {
        //blacklist
        List<String> blt = RPConfig.getGlobalFlagList(w.getName() + ".if-build-false.break-blocks.blacklist");
        if (blt.stream().anyMatch(type::matches)) return false;

        //whitelist
        List<String> wlt = RPConfig.getGlobalFlagList(w.getName() + ".if-build-false.break-blocks.whitelist");
        if (!wlt.isEmpty() && wlt.stream().noneMatch(type::matches)) {
            return false;
        }
        return RPConfig.getGlobalFlagBool(w.getName() + ".build");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent e) {
        RedProtect.get().logger.debug("BlockListener - Is BlockBreakEvent event! Cancelled? " + e.isCancelled());
        if (e.isCancelled()) {
            return;
        }

        Player p = e.getPlayer();
        Block b = e.getBlock();

        if (RPUtil.pBorders.containsKey(p.getName()) && b != null && b.getType().equals(RPConfig.getMaterial("region-settings.border.material"))) {
            RPLang.sendMessage(p, "blocklistener.cantbreak.borderblock");
            e.setCancelled(true);
            return;
        }

        Boolean antih = RPConfig.getBool("region-settings.anti-hopper");
        Region r = RedProtect.get().rm.getTopRegion(b.getLocation());

        if (!RedProtect.get().ph.hasPerm(p, "redprotect.bypass")) {
            Block ib = b.getRelative(BlockFace.UP);
            if ((antih && !cont.canBreak(p, ib)) || !cont.canBreak(p, b)) {
                RPLang.sendMessage(p, "blocklistener.container.breakinside");
                e.setCancelled(true);
                return;
            }
        }

        if (r == null && canBreakList(p.getWorld(), b.getType().name())) {
            return;
        }

        if (r != null && (b.getType().name().equals("MOB_SPAWNER") || b.getType().name().equals("SPAWNER")) && r.allowSpawner(p)) {
            return;
        }

        if (r != null && !r.canBuild(p) && !r.canTree(b) && !r.canMining(b) && !r.canCrops(b) && !r.canBreak(b.getType())) {
            RPLang.sendMessage(p, "blocklistener.region.cantbuild");
            e.setCancelled(true);
        }

    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        RedProtect.get().logger.debug("BlockListener - Is PlayerInteractEvent event! Cancelled? " + e.isCancelled());
        if (e.isCancelled()) {
            return;
        }

        Player p = e.getPlayer();
        Location l = e.getClickedBlock().getLocation();
        Region r = RedProtect.get().rm.getTopRegion(l);

        Block b = p.getLocation().getBlock();
        if (r != null &&
                (b instanceof Crops
                        || b.getType().equals(Material.PUMPKIN_STEM)
                        || b.getType().equals(Material.MELON_STEM)
                        || b.getType().toString().contains("CROPS")
                        || b.getType().toString().contains("SOIL")
                        || b.getType().toString().contains("CHORUS_")
                        || b.getType().toString().contains("BEETROOT_")
                        || b.getType().toString().contains("SUGAR_CANE")) && !r.canCrops(b) && !r.canBuild(p)) {
            RPLang.sendMessage(p, "blocklistener.region.cantbreak");
            e.setCancelled(true);
            return;
        }

        try {
            for (Block block : p.getLineOfSight(new HashSet<Material>(), 8)) {
                if (block == null) {
                    continue;
                }
                if (r != null && block.getType().equals(Material.FIRE) && !r.canBuild(p)) {
                    RPLang.sendMessage(p, "blocklistener.region.cantbreak");
                    e.setCancelled(true);
                    return;
                }
            }
        } catch (Throwable ignored) {
        }

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityExplode(EntityExplodeEvent e) {
        RedProtect.get().logger.debug("Is BlockListener - EntityExplodeEvent event");
        List<Block> toRemove = new ArrayList<>();
        if (e.getEntity() == null) {
            return;
        }
        Region or = RedProtect.get().rm.getTopRegion(e.getEntity().getLocation());
        for (Block b : e.blockList()) {
            if (b == null) {
                continue;
            }
            RedProtect.get().logger.debug("Blocks: " + b.getType().name());
            Location l = b.getLocation();
            Region r = RedProtect.get().rm.getTopRegion(l);
            if (r != null && !r.canFire() || !cont.canWorldBreak(b)) {
                RedProtect.get().logger.debug("canWorldBreak Called!");
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

    @EventHandler
    public void onFrameBrake(HangingBreakByEntityEvent e) {
        RedProtect.get().logger.debug("Is BlockListener - HangingBreakByEntityEvent event");
        if (e.isCancelled()) {
            return;
        }

        Entity remover = e.getRemover();
        Entity ent = e.getEntity();
        Location l = e.getEntity().getLocation();

        if ((ent instanceof ItemFrame || ent instanceof Painting) && remover instanceof Monster) {
            Region r = RedProtect.get().rm.getTopRegion(l);
            if (r != null && !r.canMobLoot()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onFrameBrake(HangingBreakEvent e) {
        RedProtect.get().logger.debug("Is BlockListener - HangingBreakEvent event");
        if (e.isCancelled()) {
            return;
        }

        Entity ent = e.getEntity();
        Location l = e.getEntity().getLocation();

        if ((ent instanceof ItemFrame || ent instanceof Painting) && (e.getCause().toString().equals("EXPLOSION"))) {
            Region r = RedProtect.get().rm.getTopRegion(l);
            if (r != null && !r.canFire()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockStartBurn(BlockIgniteEvent e) {
        RedProtect.get().logger.debug("RPBlockListener - Is BlockIgniteEvent event");
        if (e.isCancelled()) {
            return;
        }

        Block b = e.getBlock();
        Block bignit = e.getIgnitingBlock();
        if (b == null) {
            return;
        }

        RedProtect.get().logger.debug("Is BlockIgniteEvent event. Canceled? " + e.isCancelled());

        Region r = RedProtect.get().rm.getTopRegion(b.getLocation());
        if (r != null && !r.canFire()) {
            if (e.getIgnitingEntity() != null) {
                if (e.getIgnitingEntity() instanceof Player) {
                    Player p = (Player) e.getIgnitingEntity();
                    if (!r.canBuild(p)) {
                        RPLang.sendMessage(p, "blocklistener.region.cantplace");
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

    @EventHandler
    public void onBlockBurn(BlockBurnEvent e) {
        RedProtect.get().logger.debug("RPBlockListener - Is BlockBurnEvent event");
        if (e.isCancelled()) {
            return;
        }

        Block b = e.getBlock();

        Region r = RedProtect.get().rm.getTopRegion(b.getLocation());
        if (r != null && !r.canFire()) {
            e.setCancelled(true);
            return;
        }

        if (!cont.canWorldBreak(b)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onFlow(BlockFromToEvent e) {
        RedProtect.get().logger.debug("RPBlockListener - Is BlockFromToEvent event");
        if (e.isCancelled()) {
            return;
        }
        Block bto = e.getToBlock();
        Block bfrom = e.getBlock();
        RedProtect.get().logger.debug("RPBlockListener - Is BlockFromToEvent event is to " + bto.getType().name() + " from " + bfrom.getType().name());
        Region rto = RedProtect.get().rm.getTopRegion(bto.getLocation());
        Region rfrom = RedProtect.get().rm.getTopRegion(bfrom.getLocation());
        if (rto != null && bfrom.isLiquid() && !rto.canFlow()) {
            e.setCancelled(true);
            return;
        }
        if (rfrom != null && bfrom.isLiquid() && !rfrom.canFlow()) {
            e.setCancelled(true);
            return;
        }

        if (rto != null && !bto.isEmpty() && !rto.FlowDamage()) {
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

    @EventHandler
    public void onLightning(LightningStrikeEvent e) {
        RedProtect.get().logger.debug("RPBlockListener - Is LightningStrikeEvent event");
        Location l = e.getLightning().getLocation();
        Region r = RedProtect.get().rm.getTopRegion(l);
        if (r != null && !r.canFire()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onFireSpread(BlockSpreadEvent e) {
        RedProtect.get().logger.debug("RPBlockListener - Is BlockSpreadEvent event");
        if (e.isCancelled() || e.getNewState() == null) {
            return;
        }

        Block bfrom = e.getSource();
        Block bto = e.getBlock();
        RedProtect.get().logger.debug("Is BlockSpreadEvent event, source is " + bfrom.getType().name());
        Region rfrom = RedProtect.get().rm.getTopRegion(bfrom.getLocation());
        Region rto = RedProtect.get().rm.getTopRegion(bto.getLocation());
        if ((e.getNewState().getType().equals(Material.FIRE) || e.getNewState().getType().name().contains("LAVA")) && rfrom != null && !rfrom.canFire()) {
            e.setCancelled(true);
            return;
        }

        if ((e.getNewState().getType().equals(Material.VINE) || e.getNewState().getType().name().contains("SEAGRASS")) && ((rfrom != null && !rfrom.canGrow()) || !rto.canGrow())) {
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

    @EventHandler
    public void onStructureGrow(StructureGrowEvent e) {
        RedProtect.get().logger.debug("RPBlockListener - Is StructureGrowEvent event");
        if (!RPConfig.getBool("deny-structure-bypass-regions")) {
            return;
        }
        Region rfrom = RedProtect.get().rm.getTopRegion(e.getLocation());
        for (BlockState bstt : e.getBlocks()) {
            Region rto = RedProtect.get().rm.getTopRegion(bstt.getLocation());
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

    @EventHandler
    public void onVehicleBreak(VehicleDestroyEvent e) {
        RedProtect.get().logger.debug("RPBlockListener - Is VehicleDestroyEvent event");
        if (e.isCancelled()) {
            return;
        }
        if (!(e.getAttacker() instanceof Player)) {
            return;
        }
        Vehicle cart = e.getVehicle();
        Player p = (Player) e.getAttacker();
        Region r = RedProtect.get().rm.getTopRegion(cart.getLocation());

        if (r != null && !r.canMinecart(p)) {
            RPLang.sendMessage(p, "blocklistener.region.cantbreak");
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent e) {
        RedProtect.get().logger.debug("RPBlockListener - Is BlockPistonExtendEvent event");
        if (RPConfig.getBool("performance.disable-PistonEvent-handler")) {
            return;
        }

        //delay piston
        if (RPConfig.getBool("performance.piston.use-piston-restricter")) {
            if (pistonExtendDelay.contains(e.getBlock().getLocation().toString())) {
                e.setCancelled(true);
                return;
            } else {
                delayExtendPiston(e.getBlock().getLocation().toString());
            }
        }

        Block piston = e.getBlock();
        List<Block> blocks = e.getBlocks();
        Region pr = RedProtect.get().rm.getTopRegion(piston.getLocation());
        Boolean antih = RPConfig.getBool("region-settings.anti-hopper");
        World w = e.getBlock().getWorld();
        for (Block b : blocks) {
            RedProtect.get().logger.debug("BlockPistonExtendEvent event - Block: " + b.getType().name());
            RedProtect.get().logger.debug("BlockPistonExtendEvent event - Relative: " + b.getRelative(e.getDirection()).getType().name());
            Region br = RedProtect.get().rm.getTopRegion(b.getRelative(e.getDirection()).getLocation());
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

    private void delayExtendPiston(final String location) {
        pistonExtendDelay.add(location);
        Bukkit.getScheduler().scheduleSyncDelayedTask(RedProtect.get(), () -> pistonExtendDelay.remove(location), RPConfig.getInt("performance.piston.restrict-piston-event"));
    }

    private void delayRetractPiston(final String location) {
        pistonRetractDelay.add(location);
        Bukkit.getScheduler().scheduleSyncDelayedTask(RedProtect.get(), () -> pistonRetractDelay.remove(location), RPConfig.getInt("performance.piston.restrict-piston-event"));
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent e) {
        RedProtect.get().logger.debug("RPBlockListener - Is BlockPistonRetractEvent event");
        if (RPConfig.getBool("performance.disable-PistonEvent-handler")) {
            return;
        }

        //delay piston
        if (RPConfig.getBool("performance.piston.use-piston-restricter")) {
            if (pistonRetractDelay.contains(e.getBlock().getLocation().toString())) {
                e.setCancelled(true);
                return;
            } else {
                delayRetractPiston(e.getBlock().getLocation().toString());
            }
        }


        World w = e.getBlock().getWorld();
        Boolean antih = RPConfig.getBool("region-settings.anti-hopper");
        Block piston = e.getBlock();
        if (!Bukkit.getBukkitVersion().startsWith("1.8.") && !Bukkit.getBukkitVersion().startsWith("1.9.")) {
            Block b = e.getRetractLocation().getBlock();
            RedProtect.get().logger.debug("BlockPistonRetractEvent not 1.8 event - Block: " + b.getType().name());
            Region pr = RedProtect.get().rm.getTopRegion(piston.getLocation());
            Region br = RedProtect.get().rm.getTopRegion(b.getLocation());
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
            Region pr = RedProtect.get().rm.getTopRegion(piston.getLocation());
            for (Block b : blocks) {
                RedProtect.get().logger.debug("BlockPistonRetractEvent 1.8 event - Block: " + b.getType().name());
                Region br = RedProtect.get().rm.getTopRegion(b.getLocation());
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

    @EventHandler
    public void onLeafDecay(LeavesDecayEvent e) {
        RedProtect.get().logger.debug("RPBlockListener - Is LeavesDecayEvent event");
        if (e.isCancelled()) {
            return;
        }
        Region r = RedProtect.get().rm.getTopRegion(e.getBlock().getLocation());
        if (r != null && !r.leavesDecay()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockGrow(BlockGrowEvent event) {
        RedProtect.get().logger.debug("RPBlockListener - Is BlockGrowEvent event: " + event.getNewState().getType().name());
        if (event.isCancelled()) {
            return;
        }
        Region r = RedProtect.get().rm.getTopRegion(event.getBlock().getLocation());
        if (r != null && !r.canGrow()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockForm(BlockFormEvent event) {
        RedProtect.get().logger.debug("RPBlockListener - Is Blockform event!");
        if (event.isCancelled()) {
            return;
        }

        BlockState b = event.getNewState();
        if (b == null) {
            return;
        }
        RedProtect.get().logger.debug("Is Blockform event: " + b.getType().name());

        if (b.getType().equals(Material.SNOW) || b.getType().equals(Material.ICE)) {
            Region r = RedProtect.get().rm.getTopRegion(b.getLocation());
            if (r != null && !r.canIceForm()) {
                event.setCancelled(true);
            }
        }
    }
}
