/*
 * Copyright (c) 2020 - @FabioZumbi12
 * Last Modified: 02/07/2020 20:17.
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

package br.net.fabiozumbi12.RedProtect.Sponge.API;

import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import br.net.fabiozumbi12.RedProtect.Sponge.Region;
import br.net.fabiozumbi12.RedProtect.Sponge.config.LangManager;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

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
     * Return the Region on player location.
     * <p>
     *
     * @param location Player location.
     * @return {@code Region} of player location or {@code null} if no regions on player location.
     */
    public Region getRegion(Location<World> location) {
        return getHighPriorityRegion(location.getExtent(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
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
        return RedProtect.get().getRegionManager().getRegions(Sponge.getServer().getOnlineMode() ? player.getName() : player.getUniqueId().toString(), player.getWorld().getName());
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
        return RedProtect.get().getRegionManager().getTopRegion(world.getName(), x, y, z, this.getClass().getName());
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
    public Region getLowPriorytyRegion(World world, int x, int y, int z) {
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
     * Set a flag for the given Region with flag name and value.
     * <p>
     *
     * @param region Region to set the flag.
     * @param flag   String with flag name.
     * @param value  Object to define the flag. This Object need to be a {@code Boolean}, {@code String} or {@code Integer}.
     *               <p>Use cast to convert the non Object to Object:
     *               <p>{@code Object value = (String)MyValue;}
     * @see #equals(Object)
     */
    public void setRegionFlag(Region region, String flag, Object value) {
        setRegionFlag(null, region, flag, value);
    }

    /**
     * Set a flag for the given Region with flag name and value.
     * <p>
     *
     * @param cause  Who changes this flag.
     * @param region Region to set the flag.
     * @param flag   String with flag name.
     * @param value  Object to define the flag. This Object need to be a {@code Boolean}, {@code String} or {@code Integer}.
     *               <p>Use cast to convert the non Object to Object:
     *               <p>{@code Object value = (String)MyValue;}
     * @see #equals(Object)
     */
    public void setRegionFlag(Cause cause, Region region, String flag, Object value) {
        region.setFlag(cause, flag, value);
    }

    /**
     * Get a boolean value from given region of booleans flags.
     * <p>
     *
     * @param region Region to get flags.
     * @param flag   Flag name.
     * @return {@code Boolean} value of flag. Return {@code false} if flag not found.
     */
    public boolean getBoolFlag(Region region, String flag) {
        return region.getFlagBool(flag);
    }

    /**
     * Get a String value from given region of strings flags.
     * <p>
     *
     * @param region Region to get flags.
     * @param flag   Flag name.
     * @return {@code String} value of flag. Return {@code null} if flag not found.
     */
    public String getStringFlag(Region region, String flag) {
        return region.getFlagString(flag);
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
     * Add Admin flags with this method.
     * This flag need to be added when your custom plugin load. Adding a Admin flag, you can define a permission, or leave free for all.
     * Adding a flag, this flag will automatically checked for RedProtect.get() plugin.
     * <p>You need to use the predefined permission {@code "RedProtect.get().flag.admin." + YourCustomFlag} to allow player to change the values of Admin flags with commands.
     * <p>
     *
     * @param flag Admin Flag to add
     */
    public void addAdminFlag(String flag) {
        RedProtect.get().getConfigManager().AdminFlags.add(flag);
    }

    /**
     * Add Player flags with this method.
     * This flag need to be added when your custom plugin load. Adding a custom flag, you can define a permission, or leave free for all.
     * Adding a flag, this flag will automatically checked for RedProtect.get() plugin.
     * <p>You need to use the predefined permission {@code "RedProtect.get().flag." + YourCustomFlag} to allow player to change the values of Player flags with commands.
     * <p>
     *
     * @param flag     Player Flag to add
     * @param defValue Player Flag value to add
     */
    public void addPlayerFlag(String flag, Object defValue) {
        if (defValue instanceof Boolean)
            RedProtect.get().getConfigManager().configRoot().flags.put(flag, (Boolean) defValue);
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
     * @param isAdmin      Is flag admin? If admin, will require admin permission (RedProtect.get().admin.flag.FlagName)
     * @return true if added or false if the flag already exists.
     */
    public boolean addFlag(String flag, boolean defaultValue, boolean isAdmin) {
        return RedProtect.get().getConfigManager().addFlag(flag, defaultValue, isAdmin);
    }

    /**
     *
     * @param flag    The name of flag.
     * @param isAdmin Is Admin flag?
     * @return true if the flag was found and has been removed, false otherwise.
     */
    public boolean removeFlag(String flag, boolean isAdmin) {
        return RedProtect.get().getConfigManager().removeFlag(flag, isAdmin);
    }
}
