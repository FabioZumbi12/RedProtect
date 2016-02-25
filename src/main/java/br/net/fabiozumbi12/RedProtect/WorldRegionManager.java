package br.net.fabiozumbi12.RedProtect;

import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Player;

public interface WorldRegionManager {
	
    void load();
    
    int save();
    
    Region getRegion(String rname);
    
    int getTotalRegionSize(String p0);
    
    Set<Region> getRegions(String pname);
    
    Set<Region> getRegionsNear(Player p0, int p1);

    void add(Region p0);
    
    void remove(Region p0);
    
    Set<Region> getRegions(int x, int y, int z); 
    
    Region getTopRegion(int x, int y, int z);
    
    Region getLowRegion(int x, int y, int z);
    
    Map<Integer,Region> getGroupRegion(int x, int y, int z);
        
    Set<Region> getAllRegions();
    
    void clearRegions();

	Set<Region> getMemberRegions(String pname);

	void updateLiveRegion(String rname, String columm, String value);

	void closeConn();

	int getTotalRegionNum();

	void updateLiveFlags(String rname, String flag, String value);

	void removeLiveFlags(String rname, String flag);	

}
