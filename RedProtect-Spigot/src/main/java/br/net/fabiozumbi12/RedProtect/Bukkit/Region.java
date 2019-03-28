package br.net.fabiozumbi12.RedProtect.Bukkit;

import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Just here to do not break APIs
 */
public class Region extends br.net.fabiozumbi12.RedProtect.Bukkit.region.Region {
    /**
     * Represents the region created by player.
     *
     * @param name      Name of region.
     * @param admins    List of admins.
     * @param members   List of members.
     * @param leaders   List of leaders.
     * @param minLoc    Min coord.
     * @param maxLoc    Max coord.
     * @param flags     Flag names and values.
     * @param wMessage  Welcome message.
     * @param prior     Priority of region.
     * @param worldName Name of world for this region.
     * @param date      Date of latest visit of an admin or leader.
     * @param value     Last value of this region.
     * @param tppoint
     * @param candel
     */
    public Region(String name, Set<String> admins, Set<String> members, Set<String> leaders, Location minLoc, Location maxLoc, HashMap<String, Object> flags, String wMessage, int prior, String worldName, String date, long value, Location tppoint, boolean candel) {
        super(name, admins, members, leaders, minLoc, maxLoc, flags, wMessage, prior, worldName, date, value, tppoint, candel);
    }

    /**
     * Represents the region created by player.
     *
     * @param name      Name of region.
     * @param admins    List of admins.
     * @param members   List of members.
     * @param leaders   List of leaders.
     * @param maxMbrX   Max coord X
     * @param minMbrX   Min coord X
     * @param maxMbrZ   Max coord Z
     * @param minMbrZ   Min coord Z
     * @param minY
     * @param maxY
     * @param flags     Flag names and values.
     * @param wMessage  Welcome message.
     * @param prior     Priority of region.
     * @param worldName Name of world for this region.
     * @param date      Date of latest visit of an admin or leader.
     * @param value     Last value of this region.
     * @param tppoint
     * @param candel
     */
    public Region(String name, Set<String> admins, Set<String> members, Set<String> leaders, int maxMbrX, int minMbrX, int maxMbrZ, int minMbrZ, int minY, int maxY, HashMap<String, Object> flags, String wMessage, int prior, String worldName, String date, long value, Location tppoint, boolean candel) {
        super(name, admins, members, leaders, maxMbrX, minMbrX, maxMbrZ, minMbrZ, minY, maxY, flags, wMessage, prior, worldName, date, value, tppoint, candel);
    }

    /**
     * Represents the region created by player.
     *
     * @param name      Region name.
     * @param admins    Admins names/uuids.
     * @param members   Members names/uuids.
     * @param leaders   Leaders name/uuid.
     * @param x         Locations of x coords.
     * @param z         Locations of z coords.
     * @param miny      Min coord y of this region.
     * @param maxy      Max coord y of this region.
     * @param prior     Location of x coords.
     * @param worldName Name of world region.
     * @param date      Date of latest visit of an admins or leader.
     * @param flags
     * @param welcome   Set a welcome message.
     * @param value     A value in server economy.
     * @param tppoint
     * @param candel
     */
    public Region(String name, Set<String> admins, Set<String> members, Set<String> leaders, int[] x, int[] z, int miny, int maxy, int prior, String worldName, String date, Map<String, Object> flags, String welcome, long value, Location tppoint, boolean candel) {
        super(name, admins, members, leaders, x, z, miny, maxy, prior, worldName, date, flags, welcome, value, tppoint, candel);
    }

    /**
     * Represents the region created by player.
     *
     * @param name  Region name.
     * @param min   Min Location.
     * @param max   Max Location.
     * @param world World name.
     */
    public Region(String name, Location min, Location max, String world) {
        super(name, min, max, world);
    }
}
