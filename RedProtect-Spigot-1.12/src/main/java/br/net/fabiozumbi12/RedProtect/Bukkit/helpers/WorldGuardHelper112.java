package br.net.fabiozumbi12.RedProtect.Bukkit.helpers;

import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.List;

public class WorldGuardHelper112 implements WorldGuardHelper {

    @Override
    public String getWorldGuardMajorVersion() {
        return "6";
    }

    @Override
    public List<RegionManager> getLoaded() {
        RegionContainer rc = WorldGuardPlugin.inst().getRegionContainer();
        return rc.getLoaded();
    }

    @Override
    public Location getMinimumPoint(ProtectedRegion region, World world) {
        return new Location(
                world,
                region.getMinimumPoint().getX(),
                region.getMinimumPoint().getY(),
                region.getMinimumPoint().getZ()
        );
    }

    @Override
    public Location getMaximumPoint(ProtectedRegion region, World world) {
        return new Location(
                world,
                region.getMaximumPoint().getX(),
                region.getMaximumPoint().getY(),
                region.getMaximumPoint().getZ()
        );
    }
}
