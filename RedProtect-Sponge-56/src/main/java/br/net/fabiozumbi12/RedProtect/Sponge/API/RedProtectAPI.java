package br.net.fabiozumbi12.RedProtect.Sponge.API;

import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import br.net.fabiozumbi12.RedProtect.Sponge.Region;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Map;
import java.util.Set;

public class RedProtectAPI {

    /**
     * Give the Region based on given name and world.
     * <p>
     *
     * @param regionName Region Name.
     * @param world      World where this Region is.
     * @return {@code Region} matching the name or {@code null} if region not found.
     */
    public Region getRegion(String regionName, World world) {
        return RedProtect.get().rm.getRegion(regionName, world);
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
        return RedProtect.get().rm.getRegions(uuid);
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
        return RedProtect.get().rm.getRegions(uuid, world);
    }

    /**
     * A set of regions created by this player.
     * <p>
     *
     * @param player The player.
     * @return {@code set<Region>} with all regions created by given player.
     */
    public Set<Region> getPlayerRegions(Player player) {
        return RedProtect.get().rm.getRegions(Sponge.getServer().getOnlineMode() ? player.getName() : player.getUniqueId().toString(), player.getWorld());
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
        return RedProtect.get().rm.getRegions(player, x, y, z);
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
        return RedProtect.get().rm.getTopRegion(world, x, y, z, this.getClass().getName());
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
        return RedProtect.get().rm.getLowRegion(world, x, y, z);
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
        return RedProtect.get().rm.getGroupRegion(world, x, y, z);
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
        RedProtect.get().rm.add(region, world);
    }

    /**
     * Remove a region.
     * <p>
     *
     * @param region {@code Region} to remove.
     */
    public void removeRegion(Region region) {
        RedProtect.get().rm.remove(region, Sponge.getServer().getWorld(region.getWorld()).get());
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
        RedProtect.get().cfgs.AdminFlags.add(flag);
    }

    /**
     * Add Player flags with this method.
     * This flag need to be added when your custom plugin load. Adding a custom flag, you can define a permission, or leave free for all.
     * Adding a flag, this flag will automatically checked for RedProtect.get() plugin.
     * <p>You need to use the predefined permission {@code "RedProtect.get().flag." + YourCustomFlag} to allow player to change the values of Player flags with commands.
     * <p>
     *
     * @param flag Player Flag to add
     * @param flag Player Flag value to add
     */
    public void addPlayerFlag(String flag, Object defValue) {
        if (defValue instanceof Boolean)
            RedProtect.get().cfgs.root().flags.put(flag, (Boolean) defValue);
    }

    /**
     * Rename a region;
     * <p>
     *
     * @param region  Region to rename.
     * @param newName New name of region;
     */
    public void renameRegion(Region region, String newName) {
        RedProtect.get().rm.renameRegion(newName, region);
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
        return RedProtect.get().cfgs.addFlag(flag, defaultValue, isAdmin);
    }
}
