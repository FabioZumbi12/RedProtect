package br.net.fabiozumbi12.RedProtect;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
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
    
    public Set<Region> getRegions(String uuid) {
        Set<Region> ret = new HashSet<Region>();
        Iterator<WorldRegionManager> rms = this.regionManagers.values().iterator();
        while (rms.hasNext()) {
            ret.addAll(rms.next().getRegions(uuid));
        }
        return ret;
    }
    
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
    
    public boolean canBuild(Player p, Block b, World w) {
        return this.regionManagers.get(w).canBuild(p, b);
    }
    
    public Region isSurroundingRegion(Region r, World w) {
        return this.regionManagers.get(w).isSurroundingRegion(r);
    }
    
    public boolean regionExists(Block block, World w) {
        return this.regionManagers.get(w).regionExists(block);
    }
    
    public boolean regionExists(int x, int z, World w) {
        return this.regionManagers.get(w).regionExists(x, z);
    }
    
    public Set<Region> getPossibleIntersectingRegions(Region r, World w) {
        return this.regionManagers.get(w).getPossibleIntersectingRegions(r);
    }
    
    public void rename(Region r, String newname, World world) {
        WorldRegionManager rm = this.regionManagers.get(world);
        if (!rm.regionExists(r)) {
            return;
        }
        rm.setRegionName(r, newname);
    }
    
    public void setFlag(Region r, String flag, Object value) {
        WorldRegionManager rm = this.regionManagers.get(RedProtect.serv.getWorld(r.getWorld()));
        if (!rm.regionExists(r)) {
            return;
        }
        rm.setFlagValue(r, flag, value);
    }
    
    public void setWelcome(Region r, String wMessage, World world){
    	WorldRegionManager rm = this.regionManagers.get(world);
        if (!rm.regionExists(r)) {
            return;
        }
        rm.setWelcome(r, wMessage);
    }
    
    public Set<Region> getRegions(Player p, int x, int z){
    	return this.regionManagers.get(p.getWorld()).getRegions(x, z);    	
    }
    
    public void setPrior(Region r, int prior, World w){
    	WorldRegionManager rm = this.regionManagers.get(w);
        if (!rm.regionExists(r)) {
            return;
        }
        rm.setPrior(r, prior);
    }
    
    public Region getTopRegion(Location loc){
    	if (!this.regionManagers.containsKey(loc.getWorld())){
    		return null;
    	}
    	WorldRegionManager rm = this.regionManagers.get(loc.getWorld());    	
		return rm.getTopRegion(loc.getBlockX(), loc.getBlockZ());
    }
    
    public Region getTopRegion(World w, int x, int z){
    	if (!this.regionManagers.containsKey(w)){
    		return null;
    	}
    	WorldRegionManager rm = this.regionManagers.get(w);    	
		return rm.getTopRegion(x, z);
    }
    
    public Region getLowRegion(World w, int x, int z){
    	if (!this.regionManagers.containsKey(w)){
    		return null;
    	}
    	WorldRegionManager rm = this.regionManagers.get(w);    	
		return rm.getLowRegion(x, z);
    }
    
    public Region getLowRegion(Location loc){
    	if (!this.regionManagers.containsKey(loc.getWorld())){
    		return null;
    	}
    	WorldRegionManager rm = this.regionManagers.get(loc.getWorld());    	
		return rm.getLowRegion(loc.getBlockX(), loc.getBlockZ());
    }
    
    public Map<Integer,Region> getGroupRegion(World w, int x, int z){
    	if (!this.regionManagers.containsKey(w)){
    		return null;
    	}
    	WorldRegionManager rm = this.regionManagers.get(w);    	
		return rm.getGroupRegion(x, z);
    }
    
    public Map<Integer,Region> getGroupRegion(Location loc){
    	if (!this.regionManagers.containsKey(loc.getWorld())){
    		return null;
    	}
    	WorldRegionManager rm = this.regionManagers.get(loc.getWorld());    	
		return rm.getGroupRegion(loc.getBlockX(), loc.getBlockZ());
    }
    
    public void setDate(World w, Region r, String date){
    	WorldRegionManager rm = this.regionManagers.get(w);    	
		rm.setDate(r, date);
    }
    
    public String getDate(World w, Region r){
    	WorldRegionManager rm = this.regionManagers.get(w);    	
		return rm.getDate(r);
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

	public void updateLiveRegion(Region r) {
		WorldRegionManager rm = this.regionManagers.get(Bukkit.getWorld(r.getWorld()));    	
		rm.updateLiveRegion(r);
	}

	public int getTotalRegionsNum() {
		int total = 0;
		for (World w:RedProtect.serv.getWorlds()){
			WorldRegionManager rm = this.regionManagers.get(w);
			total = total+rm.getTotalRegionNum();
		}
		return total;
	}
    
}
