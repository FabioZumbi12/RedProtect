package br.net.fabiozumbi12.RedProtect;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class RegionManager{
	
	HashMap<World, WorldRegionManager> regionManagers;
    
    protected RegionManager() {
        this.regionManagers = new HashMap<World, WorldRegionManager>();
    }
    
    public void loadAll() throws Exception {
        for (World w : Bukkit.getWorlds()) {
            if (this.regionManagers.containsKey(w)) {
                continue;
            }
            WorldRegionManager mgr;
            if (RPConfig.getString("file-type").equalsIgnoreCase("mysql")) {
                mgr = new WorldMySQLRegionManager(w);
            } else {
            	mgr = new WorldFlatFileRegionManager(w);
            }
            mgr.load();
            this.regionManagers.put(w, mgr);
        }
    }
    
    public void load(World w) throws Exception {
        if (this.regionManagers.containsKey(w)) {
            return;
        }
        WorldRegionManager mgr;
        if (RPConfig.getString("file-type").equalsIgnoreCase("mysql")) {
            mgr = new WorldMySQLRegionManager(w);
        } else {
            mgr = new WorldFlatFileRegionManager(w);
        }
        mgr.load();
        this.regionManagers.put(w, mgr);
    }
    
    public void unloadAll() {
    	for (World w:RedProtect.serv.getWorlds()){
    		unload(w);
    	}
    }
    
    public void unload(World w) {
        if (!this.regionManagers.containsKey(w)) {
            return;
        }
        WorldRegionManager mgr = this.regionManagers.get(w);
        mgr.save();
        mgr.closeConn();
        this.regionManagers.remove(w);
    }
    
    public void saveAll() {
        Iterator<WorldRegionManager> rms = this.regionManagers.values().iterator();
        while (rms.hasNext()) {
            rms.next().save();
        }
    }
    
    public Region getRegion(String rname, World w) {
        return this.regionManagers.get(w).getRegion(rname);
    }
    
    public int getTotalRegionSize(String uuid) {
        int ret = 0;
        Iterator<WorldRegionManager> rms = this.regionManagers.values().iterator();
        while (rms.hasNext()) {
            ret += rms.next().getTotalRegionSize(uuid);
        }
        return ret;
    }
    
    public Set<Region> getWorldRegions(String player, World w) {
        return this.regionManagers.get(w).getRegions(player);
    }
    
    /**Return a {@code set<region>} of regions by player UUID or Name;
     * 
     * This will return player regions based on raw UUID or Player name, depending if server is running in Online or Offline mode;
     * @param uuid
     * @return set<region>
     */
    public Set<Region> getRegions(String uuid) {
        Set<Region> ret = new HashSet<Region>();
        Iterator<WorldRegionManager> rms = this.regionManagers.values().iterator();
        while (rms.hasNext()) {
            ret.addAll(rms.next().getRegions(uuid));
        }
        return ret;
    }
    
    /**Return a {@code set<region>} of regions by player UUID or Name;
     * 
     * This will return player regions based on raw UUID or Player name, depending if server is running in Online;
     * @param uuid
     * @return set<region>
     */
    public Set<Region> getMemberRegions(String uuid) {
        Set<Region> ret = new HashSet<Region>();
        Iterator<WorldRegionManager> rms = this.regionManagers.values().iterator();
        while (rms.hasNext()) {
            ret.addAll(rms.next().getMemberRegions(uuid));
        }
        return ret;
    }
        
    public Set<Region> getRegionsNear(Player player, int i, World w) {
        return this.regionManagers.get(w).getRegionsNear(player, i);
    }
    
    public Set<Region> getRegions(String player, World w) {
        return this.regionManagers.get(w).getRegions(player);
    }
    
    public void add(Region r, World w) {
        this.regionManagers.get(w).add(r);
    }
    
    public void save(World w){
    	this.regionManagers.get(w).save();
    }
    
    public void remove(Region r) {
        Iterator<WorldRegionManager> rms = this.regionManagers.values().iterator();
        while (rms.hasNext()) {
            rms.next().remove(r);
        }
    }
    
    /*
    public Region isSurroundingRegion(Region r, World w) {
        return this.regionManagers.get(w).isSurroundingRegion(r);
    }
    */
    
    /*
    public boolean regionExists(Block block, World w) {
        return this.regionManagers.get(w).regionExists(block);
    }
    
    public boolean regionExists(int x, int z, World w) {
        return this.regionManagers.get(w).regionExists(x, z);
    }
    */
    
    /*
    public Set<Region> getRegionsForY(int x, int z, int miny, int maxy, World w) {
        return this.regionManagers.get(w).getRegionsForY(x, z, maxy, miny);
    }
    */
      
    public Set<Region> getRegions(Player p, int x, int y, int z){
    	return this.regionManagers.get(p.getWorld()).getRegions(x, y, z);    	
    }

    /**
     * Get the hight priority region in a group region. If no other regions, return the unique region on location.
     * @return {@code Region} - Or null if no regions on this location.
     */
    public Region getTopRegion(Location loc){
    	if (!this.regionManagers.containsKey(loc.getWorld())){
    		return null;
    	}
    	WorldRegionManager rm = this.regionManagers.get(loc.getWorld());    	
		return rm.getTopRegion(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }
    
    /**
     * Get the hight priority region in a group region. If no other regions, return the unique region on location.
     * @return {@code Region} - Or null if no regions on this location.
     */
    public Region getTopRegion(World w, int x, int y, int z){
    	if (!this.regionManagers.containsKey(w)){
    		return null;
    	}
    	WorldRegionManager rm = this.regionManagers.get(w);    	
		return rm.getTopRegion(x, y, z);
    }
    
    /**
     * Get the low priority region in a group region. If no other regions, return the unique region on location.
     * @return {@code Region} - Or null if no regions on this location.
     */
    public Region getLowRegion(World w, int x, int y, int z){
    	if (!this.regionManagers.containsKey(w)){
    		return null;
    	}
    	WorldRegionManager rm = this.regionManagers.get(w);    	
		return rm.getLowRegion(x, y, z);
    }
    
    /**
     * Get the low priority region in a group region. If no other regions, return the unique region on location.
     * @return {@code Region} - Or null if no regions on this location.
     */
    public Region getLowRegion(Location loc){
    	if (!this.regionManagers.containsKey(loc.getWorld())){
    		return null;
    	}
    	WorldRegionManager rm = this.regionManagers.get(loc.getWorld());    	
		return rm.getLowRegion(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }
    
    /** Get regions in a group region. If no other regions, return the unique region on location.
     * @return {@code Map<Integer,Region>} - Indexed by priority
     */
    public Map<Integer,Region> getGroupRegion(World w, int x, int y, int z){
    	if (!this.regionManagers.containsKey(w)){
    		return null;
    	}
    	WorldRegionManager rm = this.regionManagers.get(w);    	
		return rm.getGroupRegion(x, y, z);
    }
    
    /** Get regions in a group region. If no other regions, return the unique region on location.
     * @return {@code Map<Integer,Region>} - Indexed by priority
     */
    public Map<Integer,Region> getGroupRegion(Location loc){
    	if (!this.regionManagers.containsKey(loc.getWorld())){
    		return null;
    	}
    	WorldRegionManager rm = this.regionManagers.get(loc.getWorld());    	
		return rm.getGroupRegion(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }
        
    public Set<Region> getAllRegions(){
    	Set<Region> regions = new HashSet<Region>();
    	for (World w:RedProtect.serv.getWorlds()){
    		WorldRegionManager rm = this.regionManagers.get(w);
    		regions.addAll(rm.getAllRegions());
    	}    	
		return regions;    	
    }
    
    public Set<Region> getRegionsByWorld(World w){
    	Set<Region> regions = new HashSet<Region>();
    	WorldRegionManager rm = this.regionManagers.get(w);
    	regions.addAll(rm.getAllRegions());
    	return regions;
    }
    
    protected void clearDB(){
    	for (World w:RedProtect.serv.getWorlds()){
    		WorldRegionManager rm = this.regionManagers.get(w);
    		rm.clearRegions();
    	}
    	this.regionManagers.clear();
    }

	public void updateLiveRegion(Region r, String columm, String value) {
		WorldRegionManager rm = this.regionManagers.get(Bukkit.getWorld(r.getWorld()));    	
		rm.updateLiveRegion(r.getName(), columm, value);
	}
	
	public void updateLiveFlags(Region r, String flag, String value) {
		WorldRegionManager rm = this.regionManagers.get(Bukkit.getWorld(r.getWorld()));    	
		rm.updateLiveFlags(r.getName(), flag, value);
	}
	
	public void removeLiveFlags(Region r, String flag) {
		WorldRegionManager rm = this.regionManagers.get(Bukkit.getWorld(r.getWorld()));    	
		rm.removeLiveFlags(r.getName(), flag);
	}

	public int getTotalRegionsNum() {
		int total = 0;
		for (World w:RedProtect.serv.getWorlds()){
			WorldRegionManager rm = this.regionManagers.get(w);
			total = total+rm.getTotalRegionNum();
		}
		return total;
	}
	
	@SuppressWarnings("deprecation")
	public void renameRegion(String newName, Region old){
		Region newr = new Region(newName, old.getOwners(), old.getMembers(), old.getCreator(), new int[] {old.getMinMbrX(),old.getMinMbrX(),old.getMaxMbrX(),old.getMaxMbrX()},
				new int[] {old.getMinMbrZ(),old.getMinMbrZ(),old.getMaxMbrZ(),old.getMaxMbrZ()}, old.getMinY(), old.getMaxY(), old.getPrior(), old.getWorld(), old.getDate(), old.flags, old.getWelcome(), old.getValue());
		this.add(newr, RedProtect.serv.getWorld(newr.getWorld()));
		this.remove(old);		
	}
    
}
