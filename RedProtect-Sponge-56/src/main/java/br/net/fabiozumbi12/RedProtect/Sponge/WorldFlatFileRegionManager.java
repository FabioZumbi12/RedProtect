package br.net.fabiozumbi12.RedProtect.Sponge;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.World;

class WorldFlatFileRegionManager implements WorldRegionManager{

    final HashMap<String, Region> regions;
    final World world;
    
    public WorldFlatFileRegionManager(World world) {
        super();
        this.regions = new HashMap<>();
        this.world = world;
    }
    
    @Override
    public void add(Region r) {
        this.regions.put(r.getName(), r);
    }
    
    @Override
    public void remove(Region r) {
        if (this.regions.containsKey(r.getName())){
        	this.regions.remove(r.getName());
        }
    }
        
    @Override
    public Set<Region> getRegions(String pname) {
    	SortedSet<Region> regionsp = new TreeSet<>(Comparator.comparing(Region::getName));
		for (Region r:regions.values()){
			if (r.isLeader(pname)){
				regionsp.add(r);
			}
		}
		return regionsp;
    }
    
    @Override
    public Set<Region> getMemberRegions(String uuid) {
    	SortedSet<Region> regionsp = new TreeSet<>(Comparator.comparing(Region::getName));
		for (Region r:regions.values()){
			if (r.isLeader(uuid) || r.isAdmin(uuid)){
				regionsp.add(r);
			}
		}
		return regionsp;
    }
        
    @Override
    public Region getRegion(String rname) {
    	return regions.get(rname);
    }
    
    @Override
    public int save() {
    	int saved = 0;
        try {
            RedProtect.logger.debug("default","RegionManager.Save(): File type is " + RedProtect.cfgs.getString("file-type"));
            String world = this.getWorld().getName();
                  
            if (RedProtect.cfgs.getString("file-type").equals("file")) {            	
            	
            	File datf  = new File(RedProtect.configDir+"data", "data_" + world + ".conf");
            	ConfigurationLoader<CommentedConfigurationNode> regionManager = HoconConfigurationLoader.builder().setPath(datf.toPath()).build();
            	CommentedConfigurationNode fileDB = regionManager.createEmptyNode();
        		
            	for (Region r:regions.values()){
        			if (r.getName() == null){
        				continue;
        			}
        			
        			if (RedProtect.cfgs.getBool("flat-file.region-per-file")) {
        				if (!r.toSave()){
        					continue;
        				}
        				datf  = new File(RedProtect.configDir+"data", world+File.separator+ r.getName() + ".conf"); 
        				regionManager = HoconConfigurationLoader.builder().setPath(datf.toPath()).build();
        				fileDB = regionManager.createEmptyNode();
            		}
        			
        			fileDB = RPUtil.addProps(fileDB, r); 
        			saved++;
        			
        			if (RedProtect.cfgs.getBool("flat-file.region-per-file")) { 
        				saveConf(fileDB, regionManager);
        				r.setToSave(false);        				 				
        			}
        		}
            	
            	if (!RedProtect.cfgs.getBool("flat-file.region-per-file")) {
            		RPUtil.backupRegions(fileDB, world);
    				saveConf(fileDB, regionManager);    				
    			} else {
    				//remove deleted regions
    				File wfolder = new File(RedProtect.configDir+"data", world);
    				if (wfolder.exists()){
    					File[] listOfFiles = wfolder.listFiles();    				
                		for (File region:listOfFiles){
                			if (region.isFile() && !regions.containsKey(region.getName().replace(".conf", ""))){
                				region.delete();
                			}
                		}
    				} 
    			}
            }     
        }
        catch (Exception e4) {
            e4.printStackTrace();
        }
        return saved;
    }
    
    private void saveConf(CommentedConfigurationNode fileDB, ConfigurationLoader<CommentedConfigurationNode> regionManager){
    	try {			
			regionManager.save(fileDB);			
		} catch (IOException e) {
			RedProtect.logger.severe("Error during save database file for world " + world + ": ");
			e.printStackTrace();
		} 
    }
        
    @Override
    public int getTotalRegionSize(String uuid) {
		Set<Region> regionslist = new HashSet<>();
		for (Region r:regions.values()){
			if (r.isLeader(uuid)){
				regionslist.add(r);
			}
		}
		int total = 0;
		for (Region r2 : regionslist) {
        	total += RPUtil.simuleTotalRegionSize(uuid, r2);
        }
		return total;
    }
    
    @Override
    public void load() {  
    	
    	try {
            String world = this.getWorld().getName();
            
            if (RedProtect.cfgs.getString("file-type").equals("file")) {    
            	if (RedProtect.cfgs.getBool("flat-file.region-per-file")) {
            		File f = new File(RedProtect.configDir+"data"+File.separator + world);
            		if (!f.exists()){
            			f.mkdir();
            		}
            		File[] listOfFiles = f.listFiles();
            		for (File region:listOfFiles){
            			if (region.getName().endsWith(".conf")){
            				this.load(region.getPath()); 
            			}
            		}
    			} else {
    				File oldf = new File(RedProtect.configDir+"data"+File.separator + world + ".conf");
                	File newf = new File(RedProtect.configDir+"data"+File.separator + "data_" + world + ".conf");
                    if (oldf.exists()){
                    	oldf.renameTo(newf);
                    }            
                    this.load(RedProtect.configDir+"data"+File.separator + "data_" + world + ".conf"); 
    			}            	       	
            }
		} catch (FileNotFoundException | ClassNotFoundException e) {
				e.printStackTrace();
		}
    }
    
	private void load(String path) throws FileNotFoundException, ClassNotFoundException {
        String world = this.getWorld().getName();        

        if (RedProtect.cfgs.getString("file-type").equals("file")) {        	
        	RedProtect.logger.debug("default","Load world " + this.world.getName() + ". File type: conf");
        	
        	try {
        		File tempRegionFile = new File(path);    
            	if (!tempRegionFile.exists()) {            		
    				tempRegionFile.createNewFile();    				       		
            	}
            	
            	ConfigurationLoader<CommentedConfigurationNode> regionManager = HoconConfigurationLoader.builder().setPath(tempRegionFile.toPath()).build();
            	CommentedConfigurationNode region = regionManager.load();
            	        	
            	for (Object key:region.getChildrenMap().keySet()){
            		String rname = key.toString();            		
            		if (!region.getNode(rname).hasMapChildren()){
            			continue;
            		}
            		Region newr = RPUtil.loadRegion(region, rname, world);
            		newr.setToSave(false);
            	    regions.put(rname,newr);
            	}
        	} catch (IOException | ObjectMappingException e) {
				e.printStackTrace();
			} 
        }
    }
        
    @Override
    public Set<Region> getRegionsNear(Player player, int radius) {
    	int px = player.getLocation().getBlockX();
        int pz = player.getLocation().getBlockZ();
        SortedSet<Region> ret = new TreeSet<>(Comparator.comparing(Region::getName));
        
		for (Region r:regions.values()){
			RedProtect.logger.debug("default","Radius: " + radius);
			RedProtect.logger.debug("default","X radius: " + Math.abs(r.getCenterX() - px) + " - Z radius: " + Math.abs(r.getCenterZ() - pz));
			if (Math.abs(r.getCenterX() - px) <= radius && Math.abs(r.getCenterZ() - pz) <= radius){
				ret.add(r);
			}
		}
        return ret;
    }
    
    /*
    @Override
    public boolean regionExists(Region region) {
    	if (regions.containsValue(region)){
			return true;
		}
		return false;
    }
    */
    
    public World getWorld() {
        return this.world;
    }       
    
	@Override
	public Set<Region> getRegions(int x, int y, int z) {
		Set<Region> regionl = new HashSet<>();
		for (Region r:regions.values()){
			if (x <= r.getMaxMbrX() && x >= r.getMinMbrX() && y <= r.getMaxY() && y >= r.getMinY() && z <= r.getMaxMbrZ() && z >= r.getMinMbrZ()){
				regionl.add(r);
			}
		}
		return regionl;
	}

	@Override
	public Region getTopRegion(int x, int y, int z) {
		Map<Integer,Region> regionlist = new HashMap<>();
		int max = 0;
		for (Region r:regions.values()){
			if (x <= r.getMaxMbrX() && x >= r.getMinMbrX() && y <= r.getMaxY() && y >= r.getMinY() && z <= r.getMaxMbrZ() && z >= r.getMinMbrZ()){
				if (regionlist.containsKey(r.getPrior())){
					Region reg1 = regionlist.get(r.getPrior());
					int Prior = r.getPrior();
					if (reg1.getArea() >= r.getArea()){
						r.setPrior(Prior+1);
					} else {
						reg1.setPrior(Prior+1);
					}					
				}
				regionlist.put(r.getPrior(), r);
			}
		}
		if (regionlist.size() > 0){
			max = Collections.max(regionlist.keySet());
        }
		return regionlist.get(max);
	}
	
	@Override
	public Region getLowRegion(int x, int y ,int z) {
		Map<Integer,Region> regionlist = new HashMap<>();
		int min = 0;
		for (Region r:regions.values()){
			if (x <= r.getMaxMbrX() && x >= r.getMinMbrX() && y <= r.getMaxY() && y >= r.getMinY() && z <= r.getMaxMbrZ() && z >= r.getMinMbrZ()){
				if (regionlist.containsKey(r.getPrior())){
					Region reg1 = regionlist.get(r.getPrior());
					int Prior = r.getPrior();
					if (reg1.getArea() >= r.getArea()){
						r.setPrior(Prior+1);
					} else {
						reg1.setPrior(Prior+1);
					}					
				}
				regionlist.put(r.getPrior(), r);
			}
		}
		if (regionlist.size() > 0){
			min = Collections.min(regionlist.keySet());
        }
		return regionlist.get(min);
	}
	
	@Override
	public Map<Integer,Region> getGroupRegion(int x, int y, int z) {
		Map<Integer,Region> regionlist = new HashMap<>();
		for (Region r:regions.values()){
			if (x <= r.getMaxMbrX() && x >= r.getMinMbrX() && y <= r.getMaxY() && y >= r.getMinY() && z <= r.getMaxMbrZ() && z >= r.getMinMbrZ()){
				if (regionlist.containsKey(r.getPrior())){
					Region reg1 = regionlist.get(r.getPrior());
					int Prior = r.getPrior();
					if (reg1.getArea() >= r.getArea()){
						r.setPrior(Prior+1);
					} else {
						reg1.setPrior(Prior+1);
					}					
				}
				regionlist.put(r.getPrior(), r);
			}
		}
		return regionlist;
	}

	@Override
	public Set<Region> getAllRegions() {
		SortedSet<Region> allregions = new TreeSet<>(Comparator.comparing(Region::getName));
		allregions.addAll(regions.values());
		return allregions;
	}

	@Override
	public void clearRegions() {
		regions.clear();		
	}

	@Override
	public void updateLiveRegion(String rname, String columm, Object value) {}

	@Override
	public void closeConn() {
	}

	@Override
	public int getTotalRegionNum() {
		return 0;
	}

	@Override
	public void updateLiveFlags(String rname, String flag, String value) {}

	@Override
	public void removeLiveFlags(String rname, String flag) {}
	
}
