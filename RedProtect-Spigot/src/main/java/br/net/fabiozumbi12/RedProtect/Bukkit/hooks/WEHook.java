/*
 * Copyright (c) 2012-2024 - @FabioZumbi12
 * Last Modified: 26/11/2024 17:51
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
import br.net.fabiozumbi12.RedProtect.Bukkit.actions.DefineRegionBuilder;
import br.net.fabiozumbi12.RedProtect.Bukkit.region.RegionBuilder;
import br.net.fabiozumbi12.RedProtect.Core.helpers.CoreUtil;
import br.net.fabiozumbi12.RedProtect.Core.region.PlayerRegion;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

public class WEHook {

    private static final HashMap<String, EditSession> eSessions = new HashMap<>();

    public static boolean undo(String rid) {
        if (eSessions.containsKey(rid)) {
            eSessions.get(rid).undo(eSessions.get(rid));
            return true;
        }
        return false;
    }

    public static Location[] getWESelection(Player player) {
        BukkitWorld bw = new BukkitWorld(player.getWorld());
        WorldEditPlugin worldEdit = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        if (worldEdit.getSession(player) != null && worldEdit.getSession(player).isSelectionDefined(bw)) {
            try {
                com.sk89q.worldedit.regions.Region regs = worldEdit.getSession(player).getRegionSelector(bw).getRegion();
                Location p1 = new Location(player.getWorld(), regs.getMinimumPoint().x(), regs.getMinimumPoint().y(), regs.getMinimumPoint().z());
                Location p2 = new Location(player.getWorld(), regs.getMaximumPoint().x(), regs.getMaximumPoint().y(), regs.getMaximumPoint().z());

                return new Location[]{p1, p2};
            } catch (IncompleteRegionException ignored) {
            }
        }
        return null;
    }

    private static void setSelection(BukkitWorld ws, Player p, Location pos1, Location pos2) {
        WorldEditPlugin worldEdit = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        RegionSelector regs = worldEdit.getSession(p).getRegionSelector(ws);
        regs.selectPrimary(BlockVector3.at(pos1.getX(), pos1.getY(), pos1.getZ()), null);
        regs.selectSecondary(BlockVector3.at(pos2.getX(), pos2.getY(), pos2.getZ()), null);
        worldEdit.getSession(p).setRegionSelector(ws, regs);
        worldEdit.getSession(p).dispatchCUISelection(worldEdit.wrapPlayer(p));
    }

    public static void setSelectionRP(Player p, Location pos1, Location pos2) {
        BukkitWorld ws = new BukkitWorld(p.getWorld());
        setSelection(ws, p, pos1, pos2);
    }

    public static void setSelectionFromRP(Player p, Location pos1, Location pos2) {
        WorldEditPlugin worldEdit = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        BukkitWorld ws = new BukkitWorld(p.getWorld());
        if (worldEdit.getSession(p) == null || !worldEdit.getSession(p).isSelectionDefined(ws)) {
            setSelection(ws, p, pos1, pos2);
        } else {
            worldEdit.getSession(p).getRegionSelector(ws).clear();
            RedProtect.get().getLanguageManager().sendMessage(p, RedProtect.get().getLanguageManager().get("cmdmanager.region.select-we.hide"));
        }
        worldEdit.getSession(p).dispatchCUISelection(worldEdit.wrapPlayer(p));
    }

    public static Region pasteWithWE(Player p, File file) {
        World world = p.getWorld();
        Location loc = p.getLocation();
        final Region[] r = {null};

        if (!p.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().isSolid()) {
            RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.needground");
            return null;
        }

        Clipboard clipboard = null;
        ClipboardFormat format = ClipboardFormats.findByFile(file);
        if (format == null) {
            RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.copyfail");
            return null;
        }

        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            clipboard = reader.read();
        } catch (IOException e) {
            CoreUtil.printJarVersion();
            e.printStackTrace();
        }

        if (clipboard != null) {
            try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(new BukkitWorld(world), -1)) {
                BlockVector3 to = BlockVector3.at(loc.getX(), loc.getY(), loc.getZ());

                // Rotate to player looking direction
                int rotate = getRotate(loc);
                ClipboardHolder holder = new ClipboardHolder(clipboard);
                AffineTransform transform = new AffineTransform();
                transform = transform.rotateY(-rotate);
                holder.setTransform(holder.getTransform().combine(transform));

                Operation operation = holder.createPaste(editSession)
                        .to(to)
                        .ignoreAirBlocks(false).build();
                Operations.complete(operation);

                // Select the region min and max
                BlockVector3 clipboardOffset = clipboard.getRegion().getMinimumPoint().subtract(clipboard.getOrigin());
                Vector3 realTo = to.toVector3().add(holder.getTransform().apply(clipboardOffset.toVector3()));
                Vector3 locMax = realTo.add(holder.getTransform().apply(clipboard.getRegion().getMaximumPoint().subtract(clipboard.getRegion().getMinimumPoint()).toVector3()));

                Location min = new Location(world, realTo.x(), realTo.y(), realTo.z());
                Location max = new Location(world, locMax.x(), locMax.y(), locMax.z());

                if (RedProtect.get().getConfigManager().configRoot().region_settings.autoexpandvert_ondefine) {
                    min.setY(p.getWorld().getMinHeight());
                    max.setY(p.getWorld().getMaxHeight());
                    if (RedProtect.get().getConfigManager().configRoot().region_settings.claim.miny != -1)
                        min.setY(RedProtect.get().getConfigManager().configRoot().region_settings.claim.miny);
                    if (RedProtect.get().getConfigManager().configRoot().region_settings.claim.maxy != -1)
                        max.setY(RedProtect.get().getConfigManager().configRoot().region_settings.claim.maxy);
                }
                RedProtect.get().getLanguageManager().sendMessage(p, "regionbuilder.creating");

                // Run claim async
                Bukkit.getScheduler().runTaskAsynchronously(RedProtect.get(), () -> {
                    RegionBuilder rb2 = new DefineRegionBuilder(p, min, max, "", new PlayerRegion(p.getUniqueId().toString(), p.getName()), new HashSet<>(), false);
                    if (rb2.ready()) {
                        r[0] = rb2.build();
                    }
                });
            } catch (WorldEditException e) {
                CoreUtil.printJarVersion();
                e.printStackTrace();
                r[0] = null;
            }
        }
        return r[0];
    }

    private static int getRotate(Location loc) {
        float yaw = loc.getYaw();
        if (yaw < 0) {
            yaw += 360;
        }
        int rotate = 0;
        if (yaw >= 315 || yaw < 45) {
            rotate = 180;
        } else if (yaw < 135) {
            rotate = 270;
        } else if (yaw < 225) {
            rotate = 0;
        } else if (yaw < 315) {
            rotate = 90;
        }
        return rotate;
    }

    public static void regenRegion(final Region region, final World world, final Location p1, final Location p2, final int delay, final CommandSender sender, final boolean remove) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(RedProtect.get(), () -> {
            if (RedProtect.get().getUtil().stopRegen) {
                return;
            }

            RegionSelector regs = new LocalSession().getRegionSelector(new BukkitWorld(world));
            regs.selectPrimary(BlockVector3.at(p1.getX(), p1.getY(), p1.getZ()), null);
            regs.selectSecondary(BlockVector3.at(p2.getX(), p2.getY(), p2.getZ()), null);

            com.sk89q.worldedit.regions.Region wReg;
            try {
                wReg = regs.getRegion();
            } catch (IncompleteRegionException e1) {
                e1.printStackTrace();
                return;
            }

            try (EditSession eSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(new BukkitWorld(world), -1)) {
                eSessions.put(region.getID(), eSession);
                int delayCount = 1 + delay / 10;

                com.sk89q.worldedit.world.World wRegWorld = wReg.getWorld();
                if (wRegWorld == null) return;

                if (sender != null) {
                    if (wRegWorld.regenerate(wReg, eSession)) {
                        RedProtect.get().getLanguageManager().sendMessage(sender, "[" + delayCount + "]" + " &aRegion " + region.getID().split("@")[0] + " regenerated with success!");
                    } else {
                        RedProtect.get().getLanguageManager().sendMessage(sender, "[" + delayCount + "]" + " &cTheres an error when regen the region " + region.getID().split("@")[0] + "!");
                    }
                } else {
                    if (wRegWorld.regenerate(wReg, eSession)) {
                        eSession.setMask(null);
                        RedProtect.get().logger.warning("[" + delayCount + "]" + " &aRegion " + region.getID().split("@")[0] + " regenerated with success!");
                    } else {
                        RedProtect.get().logger.warning("[" + delayCount + "]" + " &cTheres an error when regen the region " + region.getID().split("@")[0] + "!");
                    }
                }

                if (remove) {
                    region.notifyRemove();
                    RedProtect.get().getRegionManager().remove(region, region.getWorld());
                }

                if (delayCount % 50 == 0) {
                    RedProtect.get().getRegionManager().saveAll(true);
                }

                if (RedProtect.get().getConfigManager().configRoot().purge.regen.stop_server_every > 0 && delayCount > RedProtect.get().getConfigManager().configRoot().purge.regen.stop_server_every) {

                    Bukkit.getScheduler().cancelTasks(RedProtect.get());
                    RedProtect.get().getRegionManager().saveAll(false);

                    Bukkit.getServer().shutdown();
                }
            }
        }, delay);
    }
}
