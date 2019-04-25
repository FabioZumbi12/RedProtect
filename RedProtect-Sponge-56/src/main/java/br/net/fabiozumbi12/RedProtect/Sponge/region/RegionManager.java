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

package br.net.fabiozumbi12.RedProtect.Sponge.region;

import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import br.net.fabiozumbi12.RedProtect.Sponge.Region;
import br.net.fabiozumbi12.RedProtect.Sponge.database.WorldFlatFileRegionManager;
import br.net.fabiozumbi12.RedProtect.Sponge.database.WorldMySQLRegionManager;
import br.net.fabiozumbi12.RedProtect.Sponge.database.WorldRegionManager;
import br.net.fabiozumbi12.RedProtect.Core.helpers.LogLevel;
import br.net.fabiozumbi12.RedProtect.Sponge.helpers.RPUtil;
import br.net.fabiozumbi12.RedProtect.Sponge.hooks.WEHook;
import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Get the region database from here. All functions for manage regions can be found in this variable.
 */
public class RegionManager {

    private final HashMap<World, WorldRegionManager> regionManagers;
    private final HashMap<Vector3i, Region> bLoc = new HashMap<>();

    public RegionManager() {
        this.regionManagers = new HashMap<>();
    }

    public void loadAll() throws Exception {
        for (World w : Sponge.getServer().getWorlds()) {
            load(w);
        }
    }

    public void load(World w) throws Exception {
        if (this.regionManagers.containsKey(w)) {
            return;
        }
        WorldRegionManager mgr;
        if (RedProtect.get().config.configRoot().file_type.equalsIgnoreCase("mysql")) {
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
            if (RedProtect.get().hooks.Dyn && RedProtect.get().config.configRoot().hooks.dynmap.enable) {
                RedProtect.get().hooks.dynmapHook.removeAll(w);
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
        mgr.save(false);
        mgr.closeConn();
        this.regionManagers.remove(w);
    }

    public int saveAll(boolean force) {
        int saved = 0;
        for (WorldRegionManager worldRegionManager : this.regionManagers.values()) {
            saved = worldRegionManager.save(force) + saved;
        }
        return saved;
    }

    public @Nullable
    Region getRegionById(String rid) {
        if (rid == null) return null;
        World w = Sponge.getServer().getWorld(rid.split("@")[1]).get();
        return this.regionManagers.get(w).getRegion(rid.split("@")[0]);
    }

    public @Nullable
    Region getRegion(String rname, String w) {
        return this.regionManagers.get(Sponge.getServer().getWorld(w).get()).getRegion(rname);
    }

    public @Nullable
    Region getRegion(String rname, World w) {
        return this.regionManagers.get(w).getRegion(rname);
    }

    public int getTotalRegionSize(String uuid, String world) {
        Optional<World> w = Sponge.getServer().getWorld(world);
        int size = 0;
        if (RedProtect.get().config.configRoot().region_settings.blocklimit_per_world && w.isPresent()) {
            WorldRegionManager rms = this.regionManagers.get(w.get());
            size = rms.getTotalRegionSize(uuid);
        } else {
            for (World wr : Sponge.getServer().getWorlds()) {
                WorldRegionManager rms = this.regionManagers.get(wr);
                size += rms.getTotalRegionSize(uuid);
            }
        }
        return size;
    }
    /*
    public Set<Region> getWorldRegions(String player, World w) {
        return this.regionManagers.get(w).getRegions(player);
    }
    */

    /**
     * Return a {@link Set<Region>} of regions by player UUID or Name;
     * <p>
     * This will return player regions based on raw UUID or Player name, depending if server is running in Online or Offline mode;
     *
     * @param uuid the UUID of the player.
     * @return set<region>
     */
    public Set<Region> getRegions(String uuid) {
        Set<Region> ret = new HashSet<>();
        for (WorldRegionManager worldRegionManager : this.regionManagers.values()) {
            ret.addAll(worldRegionManager.getRegions(uuid));
        }
        return ret;
    }

    /**
     * Return a {@link Set<Region>} of regions by player UUID or Name;
     * <p>
     * This will return player regions based on raw UUID or Player name, depending if server is running in Online;
     *
     * @param uuid The UUID of the player
     * @return {@link Set<Region>}
     */
    public Set<Region> getMemberRegions(String uuid) {
        Set<Region> ret = new HashSet<>();
        for (WorldRegionManager worldRegionManager : this.regionManagers.values()) {
            ret.addAll(worldRegionManager.getMemberRegions(uuid));
        }
        return ret;
    }

    public Set<Region> getRegionsNear(Player player, int i, World w) {
        return this.regionManagers.get(w).getRegionsNear(player.getLocation().getBlockX(), player.getLocation().getBlockZ(), i);
    }

    public Set<Region> getRegions(String player, World w) {
        player = RPUtil.PlayerToUUID(player);
        return this.regionManagers.get(w).getRegions(player);
    }

    public Set<Region> getRegions(String player, String w) {
        player = RPUtil.PlayerToUUID(player);
        World world = Sponge.getServer().getWorld(w).get();
        return this.regionManagers.get(world).getRegions(player);
    }

    public int getPlayerRegions(String player, World w) {
        player = RPUtil.PlayerToUUID(player);
        int size;
        if (RedProtect.get().config.configRoot().region_settings.claim.claimlimit_per_world) {
            size = getRegions(player, w).size();
        } else {
            size = getRegions(player).size();
        }
        return size;
    }

    public void add(Region r, World w) {
        this.regionManagers.get(w).add(r);
        if (RedProtect.get().hooks.Dyn && RedProtect.get().config.configRoot().hooks.dynmap.enable) {
            try {
                RedProtect.get().hooks.dynmapHook.addMark(r);
            } catch (Exception ex) {
                ex.printStackTrace();
                RedProtect.get().logger.severe("Problems when add marks to Dynmap. Dynmap is updated?");
            }
        }
    }

    public void save(World w) {
        this.regionManagers.get(w).save(false);
    }

    public void remove(Region r, World w) {
        r.notifyRemove();
        WorldRegionManager rms = this.regionManagers.get(w);
        rms.remove(r);
        removeCache(r);
        if (RedProtect.get().hooks.Dyn && RedProtect.get().config.configRoot().hooks.dynmap.enable) {
            try {
                RedProtect.get().hooks.dynmapHook.removeMark(r);
            } catch (Exception ex) {
                ex.printStackTrace();
                RedProtect.get().logger.severe("Problems when remove marks to Dynmap. Dynmap is updated?");
            }
        }
    }

    private void removeCache(Region r) {
        Set<Vector3i> itloc = bLoc.keySet();
        List<Vector3i> toRemove = new ArrayList<>();
        for (Vector3i loc : itloc) {
            if (bLoc.containsKey(loc) && bLoc.get(loc).getID().equals(r.getID())) {
                toRemove.add(loc);
            }
        }
        for (Vector3i loc : toRemove) {
            bLoc.remove(loc);
        }
    }

    public Set<Region> getRegions(Player p, int x, int y, int z) {
        return this.regionManagers.get(p.getWorld()).getRegions(x, y, z);
    }

    /**
     * Get the hight priority region in a group region. If no other regions, return the unique region on location.
     *
     * @return {@code Region} - Or null if no regions on this location.
     */
    public @Nullable
    Region getTopRegion(Location<World> loc, String caller) {
        if (bLoc.containsKey(loc.getBlockPosition())) {
            RedProtect.get().logger.debug(LogLevel.BLOCKS, "Get from cache: " + loc.getBlockPosition().toString() + " - [" + caller + "]");
            return bLoc.get(loc.getBlockPosition());
        } else {
            if (!this.regionManagers.containsKey(loc.getExtent())) {
                return null;
            }
            WorldRegionManager rm = this.regionManagers.get(loc.getExtent());
            Region r = rm.getTopRegion(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            try {
                bLoc.entrySet().removeIf(k -> k.getValue().equals(r));
                if (r != null) {
                    bLoc.put(loc.getBlockPosition(), r);
                    RedProtect.get().logger.debug(LogLevel.BLOCKS, "Get from DB - [" + caller + "]");
                }
            } catch (Exception ignored) {
            }
            return r;
        }
    }

    /**
     * Get the hight priority region in a group region. If no other regions, return the unique region on location.
     *
     * @return {@code Region} - Or null if no regions on this location.
     */
    public @Nullable
    Region getTopRegion(World w, int x, int y, int z, String caller) {
        return getTopRegion(new Location<>(w, x, y, z), caller);
    }

    /**
     * Get the low priority region in a group region. If no other regions, return the unique region on location.
     *
     * @return {@code Region} - Or null if no regions on this location.
     */
    public @Nullable
    Region getLowRegion(World w, int x, int y, int z) {
        if (!this.regionManagers.containsKey(w)) {
            return null;
        }
        WorldRegionManager rm = this.regionManagers.get(w);
        return rm.getLowRegion(x, y, z);
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
            if (r.getArea() <= RedProtect.get().config.configRoot().purge.regen.max_area_regen) {
                WEHook.regenRegion(r, Sponge.getServer().getWorld(r.getWorld()).get(), r.getMaxLocation(), r.getMinLocation(), delay, null, true);
                delay = delay + 10;
            }
        }
        return delay / 10;
    }

    /**
     * Get the low priority region in a group region. If no other regions, return the unique region on location.
     *
     * @return {@code Region} - Or null if no regions on this location.
     */
    public @Nullable
    Region getLowRegion(Location<World> loc) {
        if (!this.regionManagers.containsKey(loc.getExtent())) {
            return null;
        }
        WorldRegionManager rm = this.regionManagers.get(loc.getExtent());
        return rm.getLowRegion(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    /**
     * Get regions in a group region. If no other regions, return the unique region on location.
     *
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
    public Map<Integer, Region> getGroupRegion(Location<World> loc) {
        if (!this.regionManagers.containsKey(loc.getExtent())) {
            return null;
        }
        WorldRegionManager rm = this.regionManagers.get(loc.getExtent());
        return rm.getGroupRegion(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public Set<Region> getAllRegions() {
        Set<Region> regions = new HashSet<>();
        for (World w : RedProtect.get().getServer().getWorlds()) {
            WorldRegionManager rm = this.regionManagers.get(w);
            regions.addAll(rm.getAllRegions());
        }
        return regions;
    }

    public Set<Region> getRegionsByWorld(World w) {
        WorldRegionManager rm = this.regionManagers.get(w);
        return new HashSet<>(rm.getAllRegions());
    }

    public void clearDB() {
        for (World w : RedProtect.get().getServer().getWorlds()) {
            WorldRegionManager rm = this.regionManagers.get(w);
            rm.clearRegions();
        }
        this.regionManagers.clear();
    }

    public void updateLiveRegion(Region r, String columm, Object value) {
        WorldRegionManager rm = this.regionManagers.get(Sponge.getServer().getWorld(r.getWorld()).get());
        rm.updateLiveRegion(r.getName(), columm, value);
    }

    public void updateLiveFlags(Region r, String flag, String value) {
        WorldRegionManager rm = this.regionManagers.get(Sponge.getServer().getWorld(r.getWorld()).get());
        rm.updateLiveFlags(r.getName(), flag, value);
    }

    public void removeLiveFlags(Region r, String flag) {
        WorldRegionManager rm = this.regionManagers.get(Sponge.getServer().getWorld(r.getWorld()).get());
        rm.removeLiveFlags(r.getName(), flag);
    }

    public int getTotalRegionsNum() {
        int total = 0;
        for (World w : RedProtect.get().getServer().getWorlds()) {
            WorldRegionManager rm = this.regionManagers.get(w);
            total = total + rm.getTotalRegionNum();
        }
        return total;
    }

    @SuppressWarnings("deprecation")
    public Region renameRegion(String newName, Region old) {
        Region newr = new Region(newName, old.getAdmins(), old.getMembers(), old.getLeaders(), new int[]{old.getMinMbrX(), old.getMinMbrX(), old.getMaxMbrX(), old.getMaxMbrX()},
                new int[]{old.getMinMbrZ(), old.getMinMbrZ(), old.getMaxMbrZ(), old.getMaxMbrZ()}, old.getMinY(), old.getMaxY(), old.getPrior(), old.getWorld(), old.getDate(), old.getFlags(), old.getWelcome(), old.getValue(), old.getTPPoint(), old.canDelete());

        this.add(newr, RedProtect.get().getServer().getWorld(newr.getWorld()).get());
        this.remove(old, RedProtect.get().getServer().getWorld(old.getWorld()).get());
        return newr;
    }

}
