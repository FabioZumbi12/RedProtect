package br.net.fabiozumbi12.RedProtect;

import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface WorldRegionManager {
	
    void load();
    
    void save();
    
    Region getRegion(String rname);
    
    int getTotalRegionSize(String p0);
    
    Set<Region> getRegions(String uuid);
    
    Set<Region> getRegionsNear(Player p0, int p1);
    
    Region getRegion(Player p0);
    
    void add(Region p0);
    
    void remove(Region p0);
    
    boolean canBuild(Player p0, Block p1);
    
    Region isSurroundingRegion(Region p1);
    
    boolean regionExists(Block p0);
    
    Region getRegion(Location p0);
    
    boolean regionExists(Region p0);
    
    void setFlagValue(Region p0, String p1, Object p2);
    
    void setRegionName(Region p0, String p1);
    
    boolean regionExists(int p0, int p1);
    
    Set<Region> getPossibleIntersectingRegions(Region p0);
    
    void setWelcome(Region p0, String p1);
    
    String getWelcome(Region p0);
    
    Set<Region> getRegions(int p0, int p1); 
    
    void setPrior(Region p0, int p1);
    
    int getPrior(Region p0); 
    
    Region getTopRegion(int p0, int p1);
    
    Region getLowRegion(int p0, int p1);
    
    Map<Integer,Region> getGroupRegion(int p0, int p1);
    
    void setWorld(Region p0, String p1);
    
    String getWorld(Region p0);
    
    void setDate(Region p0, String p1);
    
    String getDate(Region p0);
    
    Set<Region> getAllRegions();
    
    void clearRegions();

	Set<Region> getMemberRegions(String uuid);

	void updateLiveRegion(Region r);

	void closeConn();

	int getTotalRegionNum();

}
