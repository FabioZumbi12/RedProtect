/*
 * Copyright (c) 2012-2023 - @FabioZumbi12
 * Last Modified: 10/05/2023 14:49
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

package br.net.fabiozumbi12.RedProtect.Bukkit.API;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.LangManager;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;

public class RedProtectAPI {

    /**
     * Return language api to send messages using RedProtect language;
     * <p>
     *
     * @return {@code LangManager} with language api.
     */
    public LangManager getMessageApi() {
        return RedProtect.get().getLanguageManager();
    }

    /**
     * Return all regions for all loaded worlds;
     * <p>
     *
     * @return {@code Set<Region>} with all regions. Empty list if no regions.
     */
    public Set<Region> getAllRegions() {
        return RedProtect.get().getRegionManager().getAllRegions();
    }

    /**
     * Give the Region based on given name and world.
     * <p>
     *
     * @param regionName Region Name.
     * @param world      World where this Region is.
     * @return {@code Region} matching the name or {@code null} if region not found.
     */
    public Region getRegion(String regionName, World world) {
        return RedProtect.get().getRegionManager().getRegion(regionName, world.getName());
    }

    /**
     * Return the Region on location.
     * <p>
     * *Note: If theres more region in this same location this method will
     * return only the high priority region. To get all possible region in
     * one location use {@code getGroupRegions()}
     * <p>
     *
     * @param location Player location.
     * @return {@code Region} of location or {@code null} if no regions on player location.
     */
    public Region getRegion(Location location) {
        return getHighPriorityRegion(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    /**
     * A set of regions created by this player based on uuid or player name.
     * <p>
     *
     * @param uuid The uuid of player in String format!
     *             Make a way to give player name for servers using 'offline mode'.
     * @return {@code set<Region>} with all regions created by given player.
     */
    public Set<Region> getPlayerRegions(String uuid) {
        return RedProtect.get().getRegionManager().getLeaderRegions(uuid);
    }

    /**
     * A set of regions created by this player based on uuid or player name and specific world.
     * <p>
     *
     * @param uuid  The uuid of player in String format!
     *              Make a way to give player name for servers using 'offline mode'.
     * @param world to search for given region.
     * @return {@code set<Region>} with all regions created by given player.
     */
    public Set<Region> getPlayerRegions(String uuid, World world) {
        return RedProtect.get().getRegionManager().getRegions(uuid, world.getName());
    }

    /**
     * A set of regions created by this player.
     * <p>
     *
     * @param player The player.
     * @return {@code set<Region>} with all regions created by given player.
     */
    public Set<Region> getPlayerRegions(Player player) {
        return RedProtect.get().getRegionManager().getRegions(Bukkit.getServer().getOnlineMode() ? player.getName() : player.getUniqueId().toString(), player.getWorld().getName());
    }

    /**
     * A set of regions created by this player based on x and z.
     * <p>
     *
     * @param player Owner of regions.
     * @param x      Coord x of a location.
     * @param y      Coord y of a location.
     * @param z      Coord z of a location.
     * @return {@code set<Region>} with all regions created by given player.
     */
    public Set<Region> getPlayerRegions(Player player, int x, int y, int z) {
        return RedProtect.get().getRegionManager().getRegions(player, x, y, z);
    }

    /**
     * In a location with more than one region, use to get the upper region with high priority.
     * <p>
     *
     * @param world World to search for regions.
     * @param x     Coord x of a location.
     * @param y     Coord y of a location.
     * @param z     Coord z of a location.
     * @return The high priority {@code Region} in a group of regions.
     */
    public Region getHighPriorityRegion(World world, int x, int y, int z) {
        return RedProtect.get().getRegionManager().getTopRegion(world.getName(), x, y, z);
    }

    /**
     * In a location with more than one region, use to get the lower region with low priority.
     * <p>
     *
     * @param world World to search for regions.
     * @param x     Coord x of a location.
     * @param y     Coord y of a location.
     * @param z     Coord z of a location.
     * @return The lower priority {@code Region} in a group of regions.
     */
    public Region getLowPriorityRegion(World world, int x, int y, int z) {
        return RedProtect.get().getRegionManager().getLowRegion(world.getName(), x, y, z);
    }

    /**
     * The group of Regions on given location x and z.
     * <p>
     *
     * @param world World to search for regions.
     * @param x     Coord x of a location.
     * @param y     Coord y of a location.
     * @param z     Coord z of a location.
     * @return {@code Map<Integer, Region>} with {@code Integer} as priority and the corresponding {@code Region}.
     */
    public Map<Integer, Region> getGroupRegions(World world, int x, int y, int z) {
        return RedProtect.get().getRegionManager().getGroupRegion(world.getName(), x, y, z);
    }

    /**
     * The group of Regions on given location x and z.
     * <p>
     *
     * @param loc {@code Location} to check the regions
     * @return {@code Map<Integer, Region>} with {@code Integer} as priority and the corresponding {@code Region}.
     */
    public Map<Integer, Region> getGroupRegions(Location loc) {
        return RedProtect.get().getRegionManager().getGroupRegion(loc);
    }

    /**
     * Return all regions present on provided chunk, including low and hight priority.
     * <p>
     *
     * @param chunk Chunk to get Regions.
     * @return {@code Set<Region>} with all regions on provided chunk.
     */
    public Set<Region> getChunkRegions(Chunk chunk) {
        return RedProtect.get().getRegionManager().getRegionsForChunk(chunk);
    }

    /**
     * Add a region.
     * <p>
     *
     * @param region {@code Region} to add.
     * @param world  {@code World} of {@code Region} to add.
     */
    public void addRegion(Region region, World world) {
        RedProtect.get().getRegionManager().add(region, world.getName());
    }

    /**
     * Remove a region.
     * <p>
     *
     * @param region {@code Region} to remove.
     */
    public void removeRegion(Region region) {
        RedProtect.get().getRegionManager().remove(region, region.getWorld());
    }

    /**
     * Rename a region;
     * <p>
     *
     * @param region  Region to rename.
     * @param newName New name of region;
     */
    public void renameRegion(Region region, String newName) {
        RedProtect.get().getRegionManager().renameRegion(newName, region);
    }

    /**
     * Add custom flags.
     *
     * @param flag         The name of flag.
     * @param defaultValue Default value if not admin flag.
     * @param isAdmin      Is flag admin? If admin, will require admin permission (redprotect.admin.flag.FlagName)
     * @return true if added or false if the flag already exists.
     */
    public boolean addFlag(String flag, boolean defaultValue, boolean isAdmin) {
        return RedProtect.get().getConfigManager().addFlag(flag, defaultValue, isAdmin);
    }

    /**
     * @param flag    The name of flag.
     * @param isAdmin Is Admin flag?
     * @return true if the flag was found and has been removed, false otherwise.
     */
    public boolean removeFlag(String flag, boolean isAdmin) {
        return RedProtect.get().getConfigManager().removeFlag(flag, isAdmin);
    }
}
