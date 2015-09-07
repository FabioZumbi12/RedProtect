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
    
    Set<Region> getRegions(String pname);
    
    Set<Region> getRegionsNear(Player p0, int p1);
    
    Region getRegion(Player p0);
    
    void add(Region p0);
    
    void remove(Region p0);
        
    Region isSurroundingRegion(Region p1);
    
    boolean regionExists(Block p0);
    
    Region getRegion(Location p0);
    
    boolean regionExists(Region p0);
        
    boolean regionExists(int p0, int p1);
    
    Set<Region> getPossibleIntersectingRegions(Region p0);
    
    Set<Region> getRegions(int p0, int p1); 
    
    Region getTopRegion(int p0, int p1);
    
    Region getLowRegion(int p0, int p1);
    
    Map<Integer,Region> getGroupRegion(int p0, int p1);
        
    Set<Region> getAllRegions();
    
    void clearRegions();

	Set<Region> getMemberRegions(String pname);

	void updateLiveRegion(String rname, String columm, String value);

	void closeConn();

	int getTotalRegionNum();

	void updateLiveFlags(String rname, String flag, String value);

	void removeLiveFlags(String rname, String flag);	

}
