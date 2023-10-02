/*
 * Copyright (c) 2012-2023 - @FabioZumbi12
 * Last Modified: 02/10/2023 18:03
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

package io.github.fabiozumbi12.RedProtect.Bukkit.region;

import io.github.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import io.github.fabiozumbi12.RedProtect.Bukkit.Region;
import io.github.fabiozumbi12.RedProtect.Bukkit.database.WorldFlatFileRegionManager;
import io.github.fabiozumbi12.RedProtect.Bukkit.database.WorldMySQLRegionManager;
import io.github.fabiozumbi12.RedProtect.Bukkit.database.WorldRegionManager;
import io.github.fabiozumbi12.RedProtect.Bukkit.hooks.WEHook;
import io.github.fabiozumbi12.RedProtect.Core.helpers.LogLevel;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Get the region database from here. All functions for manage regions can be found in this class.
 */
public class RegionManager {

    private final HashMap<String, WorldRegionManager> regionManagers;
    private final HashMap<String, Region> bLoc;

    public RegionManager() {
        this.bLoc = new HashMap<>();
        this.regionManagers = new HashMap<>();
    }

    public void loadAll() throws Exception {
        for (World w : Bukkit.getWorlds()) {
            load(w.getName());
        }
    }

    public void load(String w) throws Exception {
        if (this.regionManagers.containsKey(w) && this.regionManagers.get(w) != null) {
            return;
        }
        WorldRegionManager mgr;
        if (RedProtect.get().getConfigManager().configRoot().file_type.equalsIgnoreCase("mysql")) {
            mgr = new WorldMySQLRegionManager(w);
        } else {
            mgr = new WorldFlatFileRegionManager(w);
        }
        mgr.load();
        this.regionManagers.put(w, mgr);
    }

    public void unloadAll() {
        for (String w : this.regionManagers.keySet()) {
            regionManagers.get(w).clearRegions();
            if (RedProtect.get().hooks.checkDyn() && RedProtect.get().getConfigManager().configRoot().hooks.dynmap.enable) {
                try {
                    RedProtect.get().hooks.dynmapHook.removeAll(w);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        this.regionManagers.clear();
        this.bLoc.clear();
    }

    public void unload(String w) {
        if (!this.regionManagers.containsKey(w)) {
            return;
        }
        WorldRegionManager mgr = this.regionManagers.get(w);
        mgr.save(false);
        mgr.closeConn();
        this.regionManagers.remove(w);
    }

    public int saveAll(boolean force) {
        int saved = 0;
        for (WorldRegionManager worldRegionManager : this.regionManagers.values()) {
            saved = worldRegionManager.save(force) + saved;
        }
        if (force && RedProtect.get().getConfigManager().configRoot().flat_file.backup && !RedProtect.get().getConfigManager().configRoot().file_type.equalsIgnoreCase("mysql")) {
            RedProtect.get().getUtil().backupRegions();
        }
        return saved;
    }

    public Region getRegionById(String rid) {
        if (rid == null) return null;
        if (!regionManagers.containsKey(rid.split("@")[1])) return null;
        return this.regionManagers.get(rid.split("@")[1]).getRegion(rid.split("@")[0]);
    }

    public Region getRegion(String rname, String w) {
        if (!regionManagers.containsKey(w)) return null;
        return this.regionManagers.get(w).getRegion(rname);
    }

    public int getTotalRegionSize(String uuid, String world) {
        World w = Bukkit.getWorld(world);
        if (w == null) {
            return 0;
        }
        int size = 0;
        if (RedProtect.get().getConfigManager().configRoot().region_settings.blocklimit_per_world) {
            WorldRegionManager rms = this.regionManagers.get(world);
            size = rms.getTotalRegionSize(uuid);
        } else {
            for (World wr : Bukkit.getWorlds()) {
                WorldRegionManager rms = this.regionManagers.get(wr.getName());
                if (rms == null) {
                    try {
                        this.load(wr.getName());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                assert rms != null;
                size += rms.getTotalRegionSize(uuid);
            }
        }
        return size;
    }

    /**
     * Return a set of regions by player UUID or Name if this player is Leader;
     * <p>
     * This will return player regions based on raw UUID or Player name, depending if server is running in Online or Offline mode;
     *
     * @param uuid the UUID of the player.
     * @return {@code Set<Region>} with regions
     */
    public Set<Region> getLeaderRegions(String uuid) {
        Set<Region> ret = new HashSet<>();
        for (WorldRegionManager worldRegionManager : this.regionManagers.values()) {
            ret.addAll(worldRegionManager.getLeaderRegions(uuid));
        }
        return ret;
    }

    /**
     * Return a set of regions by player UUID or Name if this player is Admin or Leader;
     * <p>
     * This will return player regions based on raw UUID or Player name, depending if server is running in Online;
     *
     * @param uuid the UUID of the player.
     * @return {@code Set<Region>} with regions
     */
    public Set<Region> getAdminRegions(String uuid) {
        Set<Region> ret = new HashSet<>();
        for (WorldRegionManager worldRegionManager : this.regionManagers.values()) {
            ret.addAll(worldRegionManager.getAdminRegions(uuid));
        }
        return ret;
    }

    /**
     * Return a set of regions by player UUID or Name if this player is Member, Admin or Leader;
     * <p>
     * This will return player regions based on raw UUID or Player name, depending if server is running in Online;
     *
     * @param uuid the UUID of the player.
     * @return {@code Set<Region>} with regions
     */
    public Set<Region> getMemberRegions(String uuid) {
        Set<Region> ret = new HashSet<>();
        for (WorldRegionManager worldRegionManager : this.regionManagers.values()) {
            ret.addAll(worldRegionManager.getMemberRegions(uuid));
        }
        return ret;
    }

    public Set<Region> getRegionsForChunk(Chunk chunk) {
        Set<Region> regions = new HashSet<>();
        for (Region region : RedProtect.get().getRegionManager().getRegionsByWorld(chunk.getWorld().getName())) {
            int minChunkX = (int) Math.floor(region.getMinMbrX() / 16f);
            int maxChunkX = (int) Math.floor(region.getMaxMbrX() / 16f);
            int minChunkZ = (int) Math.floor(region.getMinMbrZ() / 16f);
            int maxChunkZ = (int) Math.floor(region.getMaxMbrZ() / 16f);
            if (chunk.getX() >= minChunkX && chunk.getX() <= maxChunkX && chunk.getZ() >= minChunkZ && chunk.getZ() <= maxChunkZ) {
                regions.add(region);
            }
        }
        return regions;
    }

    public Set<Region> getRegionsNear(Player player, int i) {
        return regionManagers.get(player.getWorld().getName()).getRegionsNear(player.getLocation().getBlockX(), player.getLocation().getBlockZ(), i);
    }

    public Set<Region> getRegions(String player, String w) {
        if (regionManagers.containsKey(w))
            return this.regionManagers.get(w).getLeaderRegions(player);
        return new HashSet<>();
    }

    public int getPlayerRegions(String player, String w) {
        int size;
        if (RedProtect.get().getConfigManager().configRoot().region_settings.claim.claimlimit_per_world) {
            size = getRegions(player, w).size();
        } else {
            size = getLeaderRegions(player).size();
        }
        return size;
    }

    public void add(Region r, String w) {
        this.regionManagers.get(w).add(r);
        if (RedProtect.get().hooks.checkDyn() && RedProtect.get().getConfigManager().configRoot().hooks.dynmap.enable) {
            try {
                RedProtect.get().hooks.dynmapHook.addMark(r);
            } catch (Exception ex) {
                ex.printStackTrace();
                RedProtect.get().logger.severe("Problems when add marks to Dynmap. Dynmap is updated?");
            }
        }
    }

    public long getCanPurgePlayer(String player, String world) {
        if (RedProtect.get().getConfigManager().configRoot().purge.purge_limit_perworld) {
            return regionManagers.get(world).getCanPurgeCount(player, false);
        } else {
            long total = 0;
            for (World wr : Bukkit.getWorlds()) {
                total += regionManagers.get(wr.getName()).getCanPurgeCount(player, false);
            }
            return total;
        }
    }

    public void save(String w) {
        this.regionManagers.get(w).save(false);
    }

    public void remove(Region r, String w) {
        r.notifyRemove();
        WorldRegionManager rms = this.regionManagers.get(w);
        rms.remove(r);
        if (RedProtect.get().hooks.checkDyn() && RedProtect.get().getConfigManager().configRoot().hooks.dynmap.enable) {
            try {
                RedProtect.get().hooks.dynmapHook.removeMark(r);
            } catch (Exception ex) {
                RedProtect.get().logger.severe("Problems when remove marks to Dynmap. Is Dynmap updated?");
                ex.printStackTrace();
            }
        }
        removeCache(r);
    }

    public Set<Region> getRegions(Player p, int x, int y, int z) {
        return this.regionManagers.get(p.getWorld().getName()).getRegions(x, y, z);
    }

    public Set<Region> getInnerRegions(Region region, String w) {
        return this.regionManagers.get(w).getInnerRegions(region);
    }

    private void removeCache(Region r) {
        try {
            bLoc.values().removeIf(v -> v == r);
        } catch (Exception ex) {
            RedProtect.get().logger.severe("Problems when remove cache for region " + r.getName() + ": " + ex.getLocalizedMessage());
        }
    }

    public int removeAll(String player) {
        int qtd = 0;
        for (WorldRegionManager wrm : this.regionManagers.values()) {
            for (Region r : wrm.getLeaderRegions(player)) {
                r.notifyRemove();
                wrm.remove(r);
                removeCache(r);
                qtd++;
            }
        }
        return qtd;
    }

    public int regenAll(String player) {
        int delay = 0;
        for (Region r : getLeaderRegions(player)) {
            if (r.getArea() <= RedProtect.get().getConfigManager().configRoot().purge.regen.max_area_regen) {
                WEHook.regenRegion(r, Bukkit.getWorld(r.getWorld()), r.getMaxLocation(), r.getMinLocation(), delay, null, true);
                delay = delay + 10;
            }
        }
        return delay / 10;
    }

    /**
     * Get the high priority region in a group region. If no other regions, return the unique region on location.
     *
     * @param loc Location
     * @return {@code Region} - Or null if no regions on this location.
     */
    public Region getTopRegion(Location loc) {
        Region region = bLoc.get(loc.getBlock().getLocation().toString());
        if (region != null) {
            RedProtect.get().logger.debug(LogLevel.DEFAULT, "Get from cache");
            return region;
        } else {
            if (!this.regionManagers.containsKey(loc.getWorld().getName())) {
                return null;
            }

            WorldRegionManager rm = this.regionManagers.get(loc.getWorld().getName());
            region = rm.getTopRegion(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            try {
                if (region != null) {
                    bLoc.put(loc.getBlock().getLocation().toString(), region);
                    Bukkit.getScheduler().runTaskLaterAsynchronously(RedProtect.get(), () -> bLoc.remove(loc.getBlock().getLocation().toString()), 200/*10 seconds*/);
                    RedProtect.get().logger.debug(LogLevel.DEFAULT, "Get from DB");
                }
            } catch (Exception ignored) {
            }
        }
        return region;
    }

    /**
     * Get the high priority region in a group region. If no other regions, return the unique region on location.
     *
     * @param w World
     * @param x Location x
     * @param y Location y
     * @param z Location z
     * @return {@code Region} - Or null if no regions on this location.
     */
    public Region getTopRegion(String w, int x, int y, int z) {
        return getTopRegion(new Location(Bukkit.getWorld(w), x, y, z));
    }

    /**
     * Get the low priority region in a group region. If no other regions, return the unique region on location.
     *
     * @param w World
     * @param x Location x
     * @param y Location y
     * @param z Location z
     * @return {@code Region} - Or null if no regions on this location.
     */
    public Region getLowRegion(String w, int x, int y, int z) {
        if (!this.regionManagers.containsKey(w)) {
            return null;
        }
        WorldRegionManager rm = this.regionManagers.get(w);
        return rm.getLowRegion(x, y, z);
    }

    /**
     * Get the low priority region in a group region. If no other regions, return the unique region on location.
     *
     * @param loc location to get region.
     * @return {@code Region} - Or null if no regions on this location.
     */
    public Region getLowRegion(Location loc) {
        if (!this.regionManagers.containsKey(loc.getWorld().getName())) {
            return null;
        }
        WorldRegionManager rm = this.regionManagers.get(loc.getWorld().getName());
        return rm.getLowRegion(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    /**
     * Get regions in a group region. If no other regions, return the unique region on location.
     *
     * @param w World
     * @param x Location x
     * @param y Location y
     * @param z Location z
     * @return {@code Map<Integer,Region>} - Indexed by priority
     */
    public Map<Integer, Region> getGroupRegion(String w, int x, int y, int z) {
        if (!this.regionManagers.containsKey(w)) {
            return null;
        }
        WorldRegionManager rm = this.regionManagers.get(w);
        return rm.getGroupRegion(x, y, z);
    }

    /**
     * Get regions in a group region. If no other regions, return the unique region on location.
     *
     * @return {@code Map<Integer,Region>} - Indexed by priority
     */
    public Map<Integer, Region> getGroupRegion(Location loc) {
        if (!this.regionManagers.containsKey(loc.getWorld().getName())) {
            return null;
        }
        WorldRegionManager rm = this.regionManagers.get(loc.getWorld().getName());
        return rm.getGroupRegion(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public Set<Region> getAllRegions() {
        Set<Region> regions = new HashSet<>();
        for (World w : RedProtect.get().getServer().getWorlds()) {
            WorldRegionManager rm = this.regionManagers.get(w.getName());
            regions.addAll(rm.getAllRegions());
        }
        return regions;
    }

    public Set<Region> getRegionsByWorld(String w) {
        WorldRegionManager rm = this.regionManagers.get(w);
        return rm.getAllRegions();
    }

    public void clearDB() {
        for (World w : RedProtect.get().getServer().getWorlds()) {
            WorldRegionManager rm = this.regionManagers.get(w.getName());
            rm.clearRegions();
        }
        this.regionManagers.clear();
    }

    public void updateLiveRegion(Region r, String columm, Object value) {
        WorldRegionManager rm = this.regionManagers.get(r.getWorld());
        rm.updateLiveRegion(r.getName(), columm, value);
    }

    public void updateLiveFlags(Region r, String flag, String value) {
        WorldRegionManager rm = this.regionManagers.get(r.getWorld());
        rm.updateLiveFlags(r.getName(), flag, value);
    }

    public void removeLiveFlags(Region r, String flag) {
        WorldRegionManager rm = this.regionManagers.get(r.getWorld());
        rm.removeLiveFlags(r.getName(), flag);
    }

    public int getTotalRegionsNum() {
        int total = 0;
        for (World w : RedProtect.get().getServer().getWorlds()) {
            WorldRegionManager rm = this.regionManagers.get(w.getName());
            total = total + rm.getTotalRegionNum();
        }
        return total;
    }

    public Region renameRegion(String newName, Region old) {
        Region newr = new Region(newName, old.getAdmins(), old.getMembers(), old.getLeaders(), new int[]{old.getMinMbrX(), old.getMinMbrX(), old.getMaxMbrX(), old.getMaxMbrX()},
                new int[]{old.getMinMbrZ(), old.getMinMbrZ(), old.getMaxMbrZ(), old.getMaxMbrZ()}, old.getMinY(), old.getMaxY(), old.getPrior(), old.getWorld(), old.getDate(), old.getFlags(), old.getWelcome(), old.getValue(), old.getTPPoint(), old.canDelete(), old.canPurge());

        this.add(newr, newr.getWorld());
        this.remove(old, old.getWorld());
        return newr;
    }

}
