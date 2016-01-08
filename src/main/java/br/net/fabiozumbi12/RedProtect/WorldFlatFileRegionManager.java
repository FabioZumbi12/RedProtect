package br.net.fabiozumbi12.RedProtect;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

@SuppressWarnings("deprecation")
class WorldFlatFileRegionManager implements WorldRegionManager{

    HashMap<String, Region> regions;
    World world;
    
    public WorldFlatFileRegionManager(World world) {
        super();
        this.regions = new HashMap<String, Region>();
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
    	Set<Region> regionsp = new HashSet<Region>();
		for (Region r:regions.values()){
			if (r.getCreator() != null && r.getCreator().equals(pname)){
				regionsp.add(r);
			}
		}
		return regionsp;
    }
    
    @Override
    public Set<Region> getMemberRegions(String uuid) {
    	Set<Region> regionsp = new HashSet<Region>();
		for (Region r:regions.values()){
			if (r.getMembers().contains(uuid) || r.getOwners().contains(uuid)){
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
    public void save() {
        try {
            RedProtect.logger.debug("RegionManager.Save(): File type is " + RPConfig.getString("file-type"));
            String world = this.getWorld().getName();
            
            File datf = null;
            
            if (RPConfig.getString("file-type").equals("yml")) {
            	datf = new File(RedProtect.pathData, "data_" + world + ".yml");        	
            }                        
                        
            if (RPConfig.getString("file-type").equals("yml"))  {            	
            	RPYaml fileDB = new RPYaml();
        		
        		for (Region r:regions.values()){
        			if (r.getName() == null){
        				continue;
        			}
        			String rname = r.getName().replace(".", "-");					
        			fileDB.createSection(rname);
        			fileDB.set(rname+".name",r.getName());
        			fileDB.set(rname+".lastvisit",r.getDate());
        			fileDB.set(rname+".owners",r.getOwners());
        			fileDB.set(rname+".members",r.getMembers());
        			fileDB.set(rname+".creator",r.getCreator());
        			fileDB.set(rname+".priority",r.getPrior());
        			fileDB.set(rname+".welcome",r.getWelcome());
        			fileDB.set(rname+".world",r.getWorld());
        			fileDB.set(rname+".maxX",r.getMaxMbrX());
        			fileDB.set(rname+".maxZ",r.getMaxMbrZ());
        			fileDB.set(rname+".minX",r.getMinMbrX());
        			fileDB.set(rname+".minZ",r.getMinMbrZ());	
        			fileDB.set(rname+".maxY",r.getMaxY());
        			fileDB.set(rname+".minY",r.getMinY());
        			fileDB.set(rname+".flags",r.flags);	
        			fileDB.set(rname+".value",r.getValue());
        			
        			Location loc = r.getTPPoint();
        			if (loc != null){
        				int x = loc.getBlockX();
            	    	int y = loc.getBlockY();
            	    	int z = loc.getBlockZ();
            	    	float yaw = loc.getYaw();
            	    	float pitch = loc.getPitch();
            			fileDB.set(rname+".tppoint",x+","+y+","+z+","+yaw+","+pitch);
        			} else {
        				fileDB.set(rname+".tppoint","");
        			}        			
        		}	 

        		try {
        			this.backupRegions(fileDB);
        			fileDB.save(datf); 
        		} catch (IOException e) {
        			RedProtect.logger.severe("Error during save database file for world " + world + ": ");
        			e.printStackTrace();
        		}        		
        		
            }
            
        }
        catch (Exception e4) {
            e4.printStackTrace();
        }
    }
    
    private void backupRegions(RPYaml fileDB) {
        if (!RPConfig.getBool("flat-file.backup") || fileDB.getKeys(true).isEmpty()) {
            return;
        }
        
        File bfolder = new File(RedProtect.pathData+"backups"+File.separator);
        if (!bfolder.exists()){
        	bfolder.mkdir();
        }
        
        File folder = new File(RedProtect.pathData+"backups"+File.separator+this.world.getName()+File.separator);
        if (!folder.exists()){
        	folder.mkdir();
        	RedProtect.logger.info("Created folder: " + folder.getPath()); 
        }
        
        //Save backup
        if (RPUtil.genFileName(folder.getPath()+File.separator, true) != null){
        	RPUtil.SaveToZipYML(RPUtil.genFileName(folder.getPath()+File.separator, true), "data_" + this.world.getName() + ".yml", fileDB); 
        }
		       
    }
    
    @Override
    public int getTotalRegionSize(String uuid) {
		Set<Region> regionslist = new HashSet<Region>();
		for (Region r:regions.values()){
			if (r.getCreator().equalsIgnoreCase(uuid)){
				regionslist.add(r);
			}
		}
		int total = 0;
		for (Region r2 : regionslist) {
        	total += r2.getArea();
        }
		return total;
    }
    
    @Override
    public void load() {   
    	try {
            String world = this.getWorld().getName();
            if (RPConfig.getString("file-type").equals("yml")) {        	
            	File oldf = new File(String.valueOf(RedProtect.pathData) + world + ".yml");
            	File newf = new File(String.valueOf(RedProtect.pathData) + "data_" + world + ".yml");
                if (oldf.exists()){
                	oldf.renameTo(newf);
                }            
                this.load(String.valueOf(RedProtect.pathData) + "data_" + world + ".yml");        	
            }
			} catch (FileNotFoundException | ClassNotFoundException e) {
				e.printStackTrace();
			} 
    }
    
	private void load(String path) throws FileNotFoundException, ClassNotFoundException {
        String world = this.getWorld().getName();
        File f = new File(path);
        if (!f.exists()) {
            try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }

        if (RPConfig.getString("file-type").equals("yml")) {
        	RPYaml fileDB = new RPYaml();
        	RedProtect.logger.debug("Load world " + this.world.getName() + ". File type: yml");
        	try {
    			fileDB.load(f);
    		} catch (FileNotFoundException e) {
    			RedProtect.logger.severe("DB file not found!");
    			RedProtect.logger.severe("File:" + f.getName());
    			e.printStackTrace();
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
        	
        	for (String rname:fileDB.getKeys(false)){
        		if (fileDB.getString(rname+".name") == null){
        			continue;
        		}
        		int maxX = fileDB.getInt(rname+".maxX");
        		int maxZ = fileDB.getInt(rname+".maxZ");
        		int minX = fileDB.getInt(rname+".minX");
        		int minZ = fileDB.getInt(rname+".minZ");
    	    	int maxY = fileDB.getInt(rname+".maxY", this.world.getMaxHeight());
    	    	int minY = fileDB.getInt(rname+".minY", 0);
    	    	String name = fileDB.getString(rname+".name");
    	    	List<String> owners = fileDB.getStringList(rname+".owners");
    	    	List<String> members = fileDB.getStringList(rname+".members");
    	    	String creator = fileDB.getString(rname+".creator");	    	  
    	    	String welcome = fileDB.getString(rname+".welcome");
    	    	int prior = fileDB.getInt(rname+".priority");
    	    	String date = fileDB.getString(rname+".lastvisit");
    	    	long value = fileDB.getLong(rname+".value");
    	    	if (owners.size() == 0){
    	    		owners.add(creator);
    	    	}			    	
    	    	
    	    	Location tppoint = null;
                if (!fileDB.getString(rname+".tppoint", "").equalsIgnoreCase("")){
                	String tpstring[] = fileDB.getString(rname+".tppoint").split(",");
                    tppoint = new Location(Bukkit.getWorld(world), Double.parseDouble(tpstring[0]), Double.parseDouble(tpstring[1]), Double.parseDouble(tpstring[2]), 
                    		Float.parseFloat(tpstring[3]), Float.parseFloat(tpstring[4]));
                }
                
    	    	fileDB = RPUtil.fixdbFlags(fileDB, rname);
  	    	    Region newr = new Region(name, owners, members, creator, new int[] {minX,minX,maxX,maxX}, new int[] {minZ,minZ,maxZ,maxZ}, minY, maxY, prior, world, date, RPConfig.getDefFlagsValues(), welcome, value, tppoint);
    	    	for (String flag:RPConfig.getDefFlags()){
    	    		if (fileDB.get(rname+".flags."+flag) != null){
  	    			    newr.flags.put(flag,fileDB.get(rname+".flags."+flag)); 
  	    		    } else {
  	    			    newr.flags.put(flag,RPConfig.getDefFlagsValues().get(flag)); 
  	    		    }    	    		
  	    	    } 
    	    	for (String flag:RPConfig.AdminFlags){
    	    		if (fileDB.get(rname+".flags."+flag) != null){
    	    			newr.flags.put(flag,fileDB.get(rname+".flags."+flag));
    	    		}
    	    	}
        	    regions.put(name,newr);
        	}
        }
    }
        
    @Override
    public Set<Region> getRegionsNear(Player player, int radius) {
    	int px = player.getLocation().getBlockX();
        int pz = player.getLocation().getBlockZ();
        Set<Region> ret = new HashSet<Region>();
        
		for (Region r:regions.values()){
			RedProtect.logger.debug("Radius: " + radius);
			RedProtect.logger.debug("X radius: " + Math.abs(r.getCenterX() - px) + " - Z radius: " + Math.abs(r.getCenterZ() - pz));
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
		Set<Region> regionl = new HashSet<Region>();
		for (Region r:regions.values()){
			if (x <= r.getMaxMbrX() && x >= r.getMinMbrX() && y <= r.getMaxY() && y >= r.getMinY() && z <= r.getMaxMbrZ() && z >= r.getMinMbrZ()){
				regionl.add(r);
			}
		}
		return regionl;
	}

	@Override
	public Region getTopRegion(int x, int y, int z) {
		Map<Integer,Region> regionlist = new HashMap<Integer,Region>();
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
		Map<Integer,Region> regionlist = new HashMap<Integer,Region>();
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
		Map<Integer,Region> regionlist = new HashMap<Integer,Region>();
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
		Set<Region> allregions = new HashSet<Region>();
		allregions.addAll(regions.values());
		return allregions;
	}

	@Override
	public void clearRegions() {
		regions.clear();		
	}

	@Override
	public void updateLiveRegion(String rname, String columm, String value) {}

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
