package br.net.fabiozumbi12.RedProtect.API;

import java.util.Map;
import java.util.Set;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import br.net.fabiozumbi12.RedProtect.RedProtect;
import br.net.fabiozumbi12.RedProtect.Region;

public class RedProtectAPI {
	
	/**
	 * Return all regions for all loaded worlds;
	 * <p> 
	 * @return {@code Set<Region>} with all regions. Empty list if no regions.
	 */
	public static Set<Region> getAllRegions(){
		return RedProtect.rm.getAllRegions();
	}
	
	/**
	 * Give the Region based on given name and world.
	 * <p>
	 * @param regionName Region Name.
	 * @param world World where this Region is.
	 * @return {@code Region} matching the name or {@code null} if region not found.
	 */
	public static Region getRegion(String regionName, World world){
		return RedProtect.rm.getRegion(regionName, world);
	}
	
	/**
	 * Return the Region on location. 
	 * <p>
	 * *Note: If theres more region in this same location this method will 
	 * return only the high priority region. To get all possible region in	 
	 * one location use {@code getGroupRegions()}
	 * <p>
	 * @param location Player location.
	 * @return {@code Region} of location or {@code null} if no regions on player location.
	 */
	public static Region getRegion(Location location){				
		return getHighPriorityRegion(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}
	
	/**
	 * A set of regions created by this player based on uuid or player name.
	 * <p>
	 * @param uuid The uuid of player in String format!
	 * Make a way to give player name for servers using 'offline mode'.
	 * @return {@code set<Region>} with all regions created by given player.
	 */
	public static Set<Region> getPlayerRegions(String uuid){
		return RedProtect.rm.getRegions(uuid);
	}
	
	/**
	 * A set of regions created by this player based on uuid or player name and specific world.
	 * <p>
	 * @param uuid The uuid of player in String format!
	 * Make a way to give player name for servers using 'offline mode'.
	 * @param world to search for given region.
	 * @return {@code set<Region>} with all regions created by given player.
	 */
	public static Set<Region> getPlayerRegions(String uuid, World world){
		return RedProtect.rm.getRegions(uuid, world);
	}
	
	/**
	 * A set of regions created by this player based on x and z.
	 * <p>
	 * @param player Owner of regions.
	 * @param x Coord x of a location.
	 * @param y Coord y of a location.
	 * @param z Coord z of a location.
	 * @return {@code set<Region>} with all regions created by given player.
	 */
	public static Set<Region> getPlayerRegions(Player player, int x, int y, int z){
		return RedProtect.rm.getRegions(player, x, y, z);
	}
	
	/**
	 * In a location with more than one region, use to get the upper region with high priority.
	 * <p>
	 * @param world World to search for regions.
	 * @param x Coord x of a location.
	 * @param y Coord y of a location.
	 * @param z Coord z of a location.
	 * @return The high priority {@code Region} in a group of regions.
	 */
	public static Region getHighPriorityRegion(World world, int x, int y, int z){
		return RedProtect.rm.getTopRegion(world, x, y, z);
	}
	
	/**
	 * In a location with more than one region, use to get the lower region with low priority.
	 * <p>
	 * @param world World to search for regions.
	 * @param x Coord x of a location.
	 * @param y Coord y of a location.
	 * @param z Coord z of a location.
	 * @return The lower priority {@code Region} in a group of regions.
	 */
	public static Region getLowPriorytyRegion(World world, int x, int y, int z){
		return RedProtect.rm.getLowRegion(world, x, y, z);
	}
	
	/**
	 * The group of Regions on given location x and z.
	 * <p>
	 * @param world World to search for regions.
	 * @param x Coord x of a location.
	 * @param y Coord y of a location.
	 * @param z Coord z of a location.
	 * @return {@code Map<Integer, Region>} with {@code Integer} as priority and the corresponding {@code Region}.
	 */
	public static Map<Integer, Region> getGroupRegions(World world, int x, int y, int z){
		return RedProtect.rm.getGroupRegion(world, x, y, z);
	}
	
	/**
	 * The group of Regions on given location x and z.
	 * <p>
	 * @param loc {@code Location} to check the regions
	 * @return {@code Map<Integer, Region>} with {@code Integer} as priority and the corresponding {@code Region}.
	 */
	public static Map<Integer, Region> getGroupRegions(Location loc){
		return RedProtect.rm.getGroupRegion(loc);
	}
	
	/**
	 * Return all regions present on provided chunk, including low and hight priority.
	 * <p>
	 * @param chunk Chunk to get Regions.
	 * @return {@code Set<Region>} with all regions on provided chunk.
	 */
	public static Set<Region> getChunkRegions(Chunk chunk){
		return RedProtect.rm.getRegionsForChunk(chunk);
	}
	
	/**
	 * Add a region.
	 * <p>
	 * @param region {@code Region} to add.
	 * @param world {@code World} of {@code Region} to add.
	 */
	public static void addRegion(Region region, World world){
		RedProtect.rm.add(region, world);
	}
	
	/**
	 * Remove a region.
	 * <p>
	 * @param region {@code Region} to remove.
	 */
	public static void removeRegion(Region region){
		RedProtect.rm.remove(region);
	}
	
	/**
	 * Rename a region;
	 * <p>
	 * @param region Region to rename.
	 * @param newName New name of region;
	 */
	public static void renameRegion(Region region, String newName){
		RedProtect.rm.renameRegion(newName, region);
	}
}
