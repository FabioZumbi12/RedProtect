package br.net.fabiozumbi12.RedProtect.Bukkit.hooks;

import br.net.fabiozumbi12.RedProtect.Bukkit.RPUtil;
import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPConfig;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPLang;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;

@SuppressWarnings("deprecation")
public class WEListener {

    private static final HashMap<String, EditSession> eSessions = new HashMap<>();

    public static boolean undo(String rid) {
        if (eSessions.containsKey(rid)) {
            eSessions.get(rid).undo(eSessions.get(rid));
            return true;
        }
        return false;
    }

    private static void setSelection(BukkitWorld ws, Player p, Location pos1, Location pos2) {
        WorldEditPlugin worldEdit = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        RegionSelector regs = worldEdit.getSession(p).getRegionSelector(ws);
        regs.selectPrimary(new Vector(pos1.getX(), pos1.getY(), pos1.getZ()), null);
        regs.selectSecondary(new Vector(pos2.getX(), pos2.getY(), pos2.getZ()), null);
        worldEdit.getSession(p).setRegionSelector(ws, regs);
        RPLang.sendMessage(p, RPLang.get("cmdmanager.region.select-we.show")
                .replace("{pos1}", pos1.getBlockX() + "," + pos1.getBlockY() + "," + pos1.getBlockZ())
                .replace("{pos2}", pos2.getBlockX() + "," + pos2.getBlockY() + "," + pos2.getBlockZ())
        );
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
            RPLang.sendMessage(p, RPLang.get("cmdmanager.region.select-we.hide"));
        }
        worldEdit.getSession(p).dispatchCUISelection(worldEdit.wrapPlayer(p));
    }
/*
    public static void pasteWithWE(Player p, File file) {
        World world = p.getWorld();
        Location loc = p.getLocation();

        EditSession es = WorldEdit.getInstance().getEditSessionFactory().getEditSession(new BukkitWorld(world), -1);

        try {
            CuboidClipboard cc = CuboidClipboard.loadSchematic(file);
            cc.paste(es, new com.sk89q.worldedit.Vector(loc.getX(), loc.getY(), loc.getZ()), false);
        } catch (DataException | IOException | MaxChangedBlocksException e) {
            e.printStackTrace();
        }
    }
*/
    public static void regenRegion(final br.net.fabiozumbi12.RedProtect.Bukkit.Region r, final World w, final Location p1, final Location p2, final int delay, final CommandSender sender, final boolean remove) {

        Bukkit.getScheduler().scheduleSyncDelayedTask(RedProtect.get(), () -> {
            if (RPUtil.stopRegen) {
                return;
            }

            RegionSelector regs = new LocalSession().getRegionSelector(new BukkitWorld(w));
            regs.selectPrimary(new Vector(p1.getX(), p1.getY(), p1.getZ()), null);
            regs.selectSecondary(new Vector(p2.getX(), p2.getY(), p2.getZ()), null);

            Region wreg = null;
            try {
                wreg = regs.getRegion();
            } catch (IncompleteRegionException e1) {
                e1.printStackTrace();
            }

            EditSession esession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(new BukkitWorld(w), -1);

            eSessions.put(r.getID(), esession);
            int delayCount = 1 + delay / 10;

            if (sender != null) {
                if (wreg.getWorld().regenerate(wreg, esession)) {
                    RPLang.sendMessage(sender, "[" + delayCount + "]" + " &aRegion " + r.getID().split("@")[0] + " regenerated with success!");
                } else {
                    RPLang.sendMessage(sender, "[" + delayCount + "]" + " &cTheres an error when regen the region " + r.getID().split("@")[0] + "!");
                }
            } else {
                if (wreg.getWorld().regenerate(wreg, esession)) {
                    RedProtect.get().logger.warning("[" + delayCount + "]" + " &aRegion " + r.getID().split("@")[0] + " regenerated with success!");
                } else {
                    RedProtect.get().logger.warning("[" + delayCount + "]" + " &cTheres an error when regen the region " + r.getID().split("@")[0] + "!");
                }
            }

            if (remove) {
                RedProtect.get().rm.remove(r, RedProtect.get().serv.getWorld(r.getWorld()));
            }

            if (RPConfig.getInt("purge.regen.stop-server-every") > 0 && delayCount > RPConfig.getInt("purge.regen.stop-server-every")) {

                Bukkit.getScheduler().cancelTasks(RedProtect.get());
                RedProtect.get().rm.saveAll();

                Bukkit.getServer().shutdown();
            }
        }, delay);
    }
}
