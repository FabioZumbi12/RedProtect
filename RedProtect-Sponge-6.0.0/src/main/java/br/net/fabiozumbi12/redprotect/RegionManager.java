package br.net.fabiozumbi12.redprotect;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import br.net.fabiozumbi12.redprotect.hooks.WEListener;

import com.flowpowered.math.vector.Vector3i;

/**
 * Get the region database from here. All functions for manage regions can be found in this variable. 
 */
public class RegionManager{
	
	private HashMap<World, WorldRegionManager> regionManagers;
	private HashMap<Vector3i, Region> bLoc = new HashMap<Vector3i, Region>();
    
    protected RegionManager() {
        this.regionManagers = new HashMap<World, WorldRegionManager>();
    }
    
    public void loadAll() throws Exception {
        for (World w : Sponge.getServer().getWorlds()) {
            load(w);
        }
    }
    
    public void load(World w) throws Exception {
        if (this.regionManagers.containsKey(w)) {
            return;
        }
        WorldRegionManager mgr;
        if (RedProtect.cfgs.getString("file-type").equalsIgnoreCase("mysql")) {
            mgr = new WorldMySQLRegionManager(w);
        } else {
            mgr = new WorldFlatFileRegionManager(w);
        }
        mgr.load();
        this.regionManagers.put(w, mgr);
    }
    
    public void unloadAll() {
    	for (World w:this.regionManagers.keySet()){
    		regionManagers.get(w).clearRegions();
    	}
    	this.regionManagers.clear();
		this.bLoc.clear();
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
    
    public int saveAll() {
    	int saved = 0;
        Iterator<WorldRegionManager> rms = this.regionManagers.values().iterator();
        while (rms.hasNext()) {
            saved = rms.next().save()+saved;
        }
        return saved;
    }
    
    public Region getRegionById(String rid) {
    	World w = Sponge.getServer().getWorld(rid.split("@")[1]).get();
        return this.regionManagers.get(w).getRegion(rid.split("@")[0]);
    }
    
    public Region getRegion(String rname, String w) {
        return this.regionManagers.get(Sponge.getServer().getWorld(w).get()).getRegion(rname);
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
    	player = RPUtil.PlayerToUUID(player);
        return this.regionManagers.get(w).getRegions(player);
    }

    public Set<Region> getRegions(String player, String w) {
    	player = RPUtil.PlayerToUUID(player);
    	World world = Sponge.getServer().getWorld(w).get();    	
        return this.regionManagers.get(world).getRegions(player);
    }
    
    public int getPlayerRegions(String player, String w){  
    	player = RPUtil.PlayerToUUID(player);
    	return getRegions(player, w).size();
    }
    
    public int getPlayerRegions(String player, World w){
    	player = RPUtil.PlayerToUUID(player);
    	return getRegions(player, w).size();
    }
    
    public void add(Region r, World w) {
        this.regionManagers.get(w).add(r);
    }
    
    public void save(World w){
    	this.regionManagers.get(w).save();
    }
    
    public void remove(Region r, World w) {    	
    	WorldRegionManager rms = this.regionManagers.get(w);
    	rms.remove(r);
        removeCache(r);
    }
    	
	private void removeCache(Region r){
    	Set<Vector3i> itloc = bLoc.keySet();
    	for (Vector3i loc:itloc){
    		if (bLoc.containsKey(loc) && bLoc.get(loc).getID().equals(r.getID())){
    			bLoc.remove(loc);
    		}
    	}
    }
	
    public Set<Region> getRegions(Player p, int x, int y, int z){
    	return this.regionManagers.get(p.getLocation()).getRegions(x, y, z);    	
    }
		
    /**
     * Get the hight priority region in a group region. If no other regions, return the unique region on location.
     * @return {@code Region} - Or null if no regions on this location.
     */
    public Region getTopRegion(Location<World> loc){ 
    	if (bLoc.containsKey(loc.getBlockPosition())){
    		RedProtect.logger.debug("blocks", "Get from cache: "+loc.getBlockPosition().toString());
    		return bLoc.get(loc.getBlockPosition());
    	} else {
        	if (!this.regionManagers.containsKey(loc.getExtent())){
        		return null;
        	}
        	WorldRegionManager rm = this.regionManagers.get(loc.getExtent());  
        	Region r = rm.getTopRegion(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        	Vector3i remove = null;
        	for (Vector3i locKey:bLoc.keySet()){
        		if (r != null && bLoc.get(locKey).equals(r)){
        			remove = locKey;
        			break;
        		}
        	}
        	if (remove != null){
        		bLoc.remove(remove);
        	}
        	
        	if (r != null){
        		bLoc.put(loc.getBlockPosition(), r);
        		RedProtect.logger.debug("blocks", "Get from DB: "+loc.getBlockPosition().toString());
        	}        	
        	return r;
    	}
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
    
    public int removeAll(String player){
    	int qtd = 0;
    	for (WorldRegionManager wrm:this.regionManagers.values()){
    		Iterator<Region> it = wrm.getRegions(player).iterator();
    		while (it.hasNext()){
    			Region r = it.next();
    			wrm.remove(r);
    			removeCache(r);
    			qtd++;
    		}
    	}
    	return qtd;
    }
    
    public int regenAll(String player){
    	int delay = 0;
    	Iterator<Region> it = getRegions(player).iterator();
    	while (it.hasNext()){
    		Region r = it.next();
    		if (r.getArea() <= RedProtect.cfgs.getInt("purge.regen.max-area-regen")){
    			WEListener.regenRegion(r, Sponge.getServer().getWorld(r.getWorld()).get(), r.getMaxLocation(), r.getMinLocation(), delay, null, true);               		
        		delay=delay+10;
			}
    	}
    	return delay/10;
    }
    
    /**
     * Get the low priority region in a group region. If no other regions, return the unique region on location.
     * @return {@code Region} - Or null if no regions on this location.
     */
    public Region getLowRegion(Location<World> loc){
    	if (!this.regionManagers.containsKey(loc.getExtent())){
    		return null;
    	}
    	WorldRegionManager rm = this.regionManagers.get(loc.getExtent());    	
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
    public Map<Integer,Region> getGroupRegion(Location<World> loc){
    	if (!this.regionManagers.containsKey(loc.getExtent())){
    		return null;
    	}
    	WorldRegionManager rm = this.regionManagers.get(loc.getExtent());    	
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
    
    public void clearDB(){
    	for (World w:RedProtect.serv.getWorlds()){
    		WorldRegionManager rm = this.regionManagers.get(w);
    		rm.clearRegions();
    	}
    	this.regionManagers.clear();
    }

	public void updateLiveRegion(Region r, String columm, Object value) {
		WorldRegionManager rm = this.regionManagers.get(Sponge.getServer().getWorld(r.getWorld()).get());    	
		rm.updateLiveRegion(r.getName(), columm, value);
	}
	
	public void updateLiveFlags(Region r, String flag, String value) {
		WorldRegionManager rm = this.regionManagers.get(Sponge.getServer().getWorld(r.getWorld()).get());    	
		rm.updateLiveFlags(r.getName(), flag, value);
	}
	
	public void removeLiveFlags(Region r, String flag) {
		WorldRegionManager rm = this.regionManagers.get(Sponge.getServer().getWorld(r.getWorld()).get());    	
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
		Region newr = new Region(newName, old.getAdmins(), old.getMembers(), old.getLeaders(), new int[] {old.getMinMbrX(),old.getMinMbrX(),old.getMaxMbrX(),old.getMaxMbrX()},
				new int[] {old.getMinMbrZ(),old.getMinMbrZ(),old.getMaxMbrZ(),old.getMaxMbrZ()}, old.getMinY(), old.getMaxY(), old.getPrior(), old.getWorld(), old.getDate(), old.flags, old.getWelcome(), old.getValue(), old.getTPPoint(), old.canDelete());
		if (old.getRentString().split(":").length >= 3){
			newr.setRentString(old.getRentString());
		}
		this.add(newr, RedProtect.serv.getWorld(newr.getWorld()).get());		
		this.remove(old, RedProtect.serv.getWorld(old.getWorld()).get());		
	}
    
}
