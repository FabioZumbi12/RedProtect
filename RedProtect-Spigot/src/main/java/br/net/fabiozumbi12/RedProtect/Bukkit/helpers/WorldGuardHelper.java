package br.net.fabiozumbi12.RedProtect.Bukkit.helpers;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.List;

public interface WorldGuardHelper {

    String getWorldGuardMajorVersion();

    List<RegionManager> getLoaded();

    Location getMinimumPoint(ProtectedRegion region, World world);

    Location getMaximumPoint(ProtectedRegion region, World world);
}
