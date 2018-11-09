package br.net.fabiozumbi12.RedProtect.Bukkit;

import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPConfig;
import br.net.fabiozumbi12.RedProtect.Bukkit.hooks.WEListener;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Get the region database from here. All functions for manage regions can be found in this variable.
 */
public class RegionManager {

    private final HashMap<World, WorldRegionManager> regionManagers;
    private final HashMap<Location, Region> bLoc = new HashMap<>();

    protected RegionManager() {
        this.regionManagers = new HashMap<>();
    }

    public void loadAll() throws Exception {
        for (World w : Bukkit.getWorlds()) {
            load(w);
        }
    }

    public void load(World w) throws Exception {
        if (this.regionManagers.containsKey(w)) {
            return;
        }
        WorldRegionManager mgr;
        if (RPConfig.getString("file-type").equalsIgnoreCase("mysql")) {
            mgr = new WorldMySQLRegionManager(w);
        } else {
            mgr = new WorldFlatFileRegionManager(w);
        }
        mgr.load();
        this.regionManagers.put(w, mgr);
    }

    public void unloadAll() {
        for (World w : this.regionManagers.keySet()) {
            regionManagers.get(w).clearRegions();
            if (RedProtect.get().Dyn && RPConfig.getBool("hooks.dynmap.enable")) {
                RedProtect.get().dynmap.removeAll(w);
            }
        }
        this.regionManagers.clear();
        this.bLoc.clear();
    }

    public void unload(World w) {
        if (!this.regionManagers.containsKey(w)) {
            return;
        }
        WorldRegionManager mgr = this.regionManagers.get(w);
        mgr.save();
        mgr.closeConn();
        this.regionManagers.remove(w);
    }

    public int saveAll() {
        int saved = 0;
        for (WorldRegionManager worldRegionManager : this.regionManagers.values()) {
            saved = worldRegionManager.save() + saved;
        }
        return saved;
    }

    public Region getRegion(String rname, World w) {
        return this.regionManagers.get(w).getRegion(rname);
    }

    public Region getRegionById(String rid) {
        if (rid == null) return null;
        World w = Bukkit.getWorld(rid.split("@")[1]);
        return this.regionManagers.get(w).getRegion(rid.split("@")[0]);
    }

    public Region getRegion(String rname, String w) {
        return this.regionManagers.get(Bukkit.getWorld(w)).getRegion(rname);
    }

    public int getTotalRegionSize(String uuid, String world) {
        World w = Bukkit.getWorld(world);
        if (w == null) {
            return 0;
        }
        int size = 0;
        if (RPConfig.getBool("region-settings.blocklimit-per-world")) {
            WorldRegionManager rms = this.regionManagers.get(w);
            size = rms.getTotalRegionSize(uuid);
        } else {
            for (World wr : Bukkit.getWorlds()) {
                WorldRegionManager rms = this.regionManagers.get(wr);
                size += rms.getTotalRegionSize(uuid);
            }
        }
        return size;
    }

    public Set<Region> getWorldRegions(String player, World w) {
        return this.regionManagers.get(w).getRegions(player);
    }

    /**
     * Return a {@code set<region>} of regions by player UUID or Name;
     * <p>
     * This will return player regions based on raw UUID or Player name, depending if server is running in Online or Offline mode;
     *
     * @param uuid
     * @return {@code set<region>}
     */
    public Set<Region> getRegions(String uuid) {
        Set<Region> ret = new HashSet<>();
        for (WorldRegionManager worldRegionManager : this.regionManagers.values()) {
            ret.addAll(worldRegionManager.getRegions(uuid));
        }
        return ret;
    }

    /**
     * Return a {@code set<region>} of regions by player UUID or Name if this player is Admin or Leader;
     * <p>
     * This will return player regions based on raw UUID or Player name, depending if server is running in Online;
     *
     * @param uuid
     * @return {@code set<region>}
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
        for (Region region : RedProtect.get().rm.getRegionsByWorld(chunk.getWorld())) {
            int minChunkX = (int) Math.floor(region.getMinMbrX() / 16f);
            int maxChunkX = (int) Math.floor(region.getMaxMbrX() / 16f);
            int minChunkZ = (int) Math.floor(region.getMinMbrZ() / 16f);
            int maxChunkZ = (int) Math.floor(region.getMaxMbrZ() / 16f);

            if (chunk.getX() >= minChunkX && chunk.getX() <= maxChunkX && chunk.getZ() >= minChunkZ && chunk.getZ() <= maxChunkZ) {
                if (!regions.contains(region)) {
                    regions.add(region);
                }
            }
        }
        return regions;
    }

    public Set<Region> getRegionsNear(Player player, int i, World w) {
        return this.regionManagers.get(w).getRegionsNear(player, i);
    }

    public Set<Region> getRegions(String player, World w) {
        player = RPUtil.PlayerToUUID(player);
        return this.regionManagers.get(w).getRegions(player);
    }

    public Set<Region> getRegions(String player, String w) {
        player = RPUtil.PlayerToUUID(player);
        World world = Bukkit.getWorld(w);
        return this.regionManagers.get(world).getRegions(player);
    }

    public int getPlayerRegions(String player, World w) {
        player = RPUtil.PlayerToUUID(player);
        int size = 0;
        if (RPConfig.getBool("region-settings.claimlimit-per-world")) {
            size = getRegions(player, w).size();
        } else {
            size = getRegions(player).size();
        }
        return size;
    }

    public void add(Region r, World w) {
        this.regionManagers.get(w).add(r);
        if (RedProtect.get().Dyn && RPConfig.getBool("hooks.dynmap.enable")) {
            try {
                RedProtect.get().dynmap.addMark(r);
            } catch (Exception ex) {
                ex.printStackTrace();
                RedProtect.get().logger.severe("Problems when add marks to Dynmap. Dynmap is updated?");
            }
        }
    }

    public void save(World w) {
        this.regionManagers.get(w).save();
    }

    public void remove(Region r, World w) {
        r.notifyRemove();
        WorldRegionManager rms = this.regionManagers.get(w);
        rms.remove(r);
        removeCache(r);
        if (RedProtect.get().Dyn && RPConfig.getBool("hooks.dynmap.enable")) {
            try {
                RedProtect.get().dynmap.removeMark(r);
            } catch (Exception ex) {
                ex.printStackTrace();
                RedProtect.get().logger.severe("Problems when remove marks to Dynmap. Dynmap is updated?");
            }
        }
    }

    public Set<Region> getRegions(Player p, int x, int y, int z) {
        return this.regionManagers.get(p.getWorld()).getRegions(x, y, z);
    }

    public Set<Region> getInnerRegions(Region region, World w) {
        return this.regionManagers.get(w).getInnerRegions(region);
    }

    private void removeCache(Region r) {
        Set<Location> itloc = bLoc.keySet();
        List<Location> toRemove = new ArrayList<>();
        for (Location loc : itloc) {
            if (bLoc.containsKey(loc) && bLoc.get(loc).getID().equals(r.getID())) {
                toRemove.add(loc);
            }
        }
        for (Location loc : toRemove) {
            bLoc.remove(loc);
        }
    }

    public int removeAll(String player) {
        int qtd = 0;
        for (WorldRegionManager wrm : this.regionManagers.values()) {
            for (Region r : wrm.getRegions(player)) {
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
        for (Region r : getRegions(player)) {
            if (r.getArea() <= RPConfig.getInt("purge.regen.max-area-regen")) {
                WEListener.regenRegion(r, Bukkit.getWorld(r.getWorld()), r.getMaxLocation(), r.getMinLocation(), delay, null, true);
                delay = delay + 10;
            }
        }
        return delay / 10;
    }

    /**
     * Get the hight priority region in a group region. If no other regions, return the unique region on location.
     *
     * @param loc Location
     * @return {@code Region} - Or null if no regions on this location.
     */
    public Region getTopRegion(Location loc) {
        if (bLoc.containsKey(loc.getBlock().getLocation())) {
            RedProtect.get().logger.debug("Get from cache");
            return bLoc.get(loc.getBlock().getLocation());
        } else {
            if (!this.regionManagers.containsKey(loc.getWorld())) {
                return null;
            }

            WorldRegionManager rm = this.regionManagers.get(loc.getWorld());
            Region r = rm.getTopRegion(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            try {
                bLoc.entrySet().removeIf(k -> k.getValue().equals(r));

                if (r != null) {
                    bLoc.put(loc.getBlock().getLocation(), r);
                    RedProtect.get().logger.debug("Get from DB");
                }
            } catch (Exception ignored) {
            }
            return r;
        }
    }

    /**
     * Get the hight priority region in a group region. If no other regions, return the unique region on location.
     *
     * @param w World
     * @param x Location x
     * @param y Location y
     * @param z Location z
     * @return {@code Region} - Or null if no regions on this location.
     */
    public Region getTopRegion(World w, int x, int y, int z) {
        return getTopRegion(new Location(w, x, y, z));
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
    public Region getLowRegion(World w, int x, int y, int z) {
        if (!this.regionManagers.containsKey(w)) {
            return null;
        }
        WorldRegionManager rm = this.regionManagers.get(w);
        return rm.getLowRegion(x, y, z);
    }

    /**
     * Get the low priority region in a group region. If no other regions, return the unique region on location.
     *
     * @return {@code Region} - Or null if no regions on this location.
     */
    public Region getLowRegion(Location loc) {
        if (!this.regionManagers.containsKey(loc.getWorld())) {
            return null;
        }
        WorldRegionManager rm = this.regionManagers.get(loc.getWorld());
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
    public Map<Integer, Region> getGroupRegion(World w, int x, int y, int z) {
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
        if (!this.regionManagers.containsKey(loc.getWorld())) {
            return null;
        }
        WorldRegionManager rm = this.regionManagers.get(loc.getWorld());
        return rm.getGroupRegion(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public Set<Region> getAllRegions() {
        Set<Region> regions = new HashSet<>();
        for (World w : RedProtect.get().serv.getWorlds()) {
            WorldRegionManager rm = this.regionManagers.get(w);
            regions.addAll(rm.getAllRegions());
        }
        return regions;
    }

    public Set<Region> getRegionsByWorld(World w) {
        WorldRegionManager rm = this.regionManagers.get(w);
        return rm.getAllRegions();
    }

    public void clearDB() {
        for (World w : RedProtect.get().serv.getWorlds()) {
            WorldRegionManager rm = this.regionManagers.get(w);
            rm.clearRegions();
        }
        this.regionManagers.clear();
    }

    public void updateLiveRegion(Region r, String columm, Object value) {
        WorldRegionManager rm = this.regionManagers.get(Bukkit.getWorld(r.getWorld()));
        rm.updateLiveRegion(r.getName(), columm, value);
    }

    public void updateLiveFlags(Region r, String flag, String value) {
        WorldRegionManager rm = this.regionManagers.get(Bukkit.getWorld(r.getWorld()));
        rm.updateLiveFlags(r.getName(), flag, value);
    }

    public void removeLiveFlags(Region r, String flag) {
        WorldRegionManager rm = this.regionManagers.get(Bukkit.getWorld(r.getWorld()));
        rm.removeLiveFlags(r.getName(), flag);
    }

    public int getTotalRegionsNum() {
        int total = 0;
        for (World w : RedProtect.get().serv.getWorlds()) {
            WorldRegionManager rm = this.regionManagers.get(w);
            total = total + rm.getTotalRegionNum();
        }
        return total;
    }

    @SuppressWarnings("deprecation")
    public void renameRegion(String newName, Region old) {
        Region newr = new Region(newName, old.getAdmins(), old.getMembers(), old.getLeaders(), new int[]{old.getMinMbrX(), old.getMinMbrX(), old.getMaxMbrX(), old.getMaxMbrX()},
                new int[]{old.getMinMbrZ(), old.getMinMbrZ(), old.getMaxMbrZ(), old.getMaxMbrZ()}, old.getMinY(), old.getMaxY(), old.getPrior(), old.getWorld(), old.getDate(), old.getFlags(), old.getWelcome(), old.getValue(), old.getTPPoint(), old.canDelete());

        this.add(newr, RedProtect.get().serv.getWorld(newr.getWorld()));
        this.remove(old, RedProtect.get().serv.getWorld(old.getWorld()));
    }

}
