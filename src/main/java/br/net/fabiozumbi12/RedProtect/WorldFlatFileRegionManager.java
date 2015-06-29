package br.net.fabiozumbi12.RedProtect;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipException;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;

class WorldFlatFileRegionManager implements WorldRegionManager{

    HashMap<String, Region> regions;
    World world;
    HashMap<Long, LargeChunkObject> regionslco;
    
    public WorldFlatFileRegionManager(World world) {
        super();
        this.regionslco = new HashMap<Long, LargeChunkObject>(100);
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
    
    public Set<Region> getRegionsIntersecting(int bx, int bz) {
		Set<Region> ret = new HashSet<Region>();
		for (Region r:regions.values()){
			if (bx <= r.getMaxMbrX() && bx >= r.getMinMbrX() && bz <= r.getMaxMbrZ() && bz >= r.getMinMbrZ()){
				ret.add(r);
			}
		}
		RedProtect.logger.debug("Rects intersecting " + bx + ", " + bz + ": ");
        for (Region r : ret) {
            RedProtect.logger.debug(String.valueOf(r.getName()) + r.info());
        }
        return ret;
	}
    
    @Override
    public Set<Region> getRegions(String uuid) {
    	Set<Region> regionsp = new HashSet<Region>();
		for (Region r:regions.values()){
			if (r.getCreator().equals(uuid)){
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
    public boolean regionExists(Block b) {
        return this.regionExists(b.getX(), b.getZ());
    }
    
    @Override
    public boolean regionExists(int x, int z) {
    	for (Region poly : this.getRegionsIntersecting(x, z)) {
            if (poly.intersects(x, z)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public Region getRegion(Location l) {
        int x = l.getBlockX();
        int z = l.getBlockZ();
        return this.getRegion(x, z);
    }
    
    private Region getRegion(int x, int z) {
    	for (Region poly : this.getRegionsIntersecting(x, z)) {
            if (poly.intersects(x, z)) {
                return poly;
            }
        }
        return null;
    }
    
    @Override
    public Region getRegion(Player p) {
        return this.getRegion(p.getLocation());
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
            
            if (RPConfig.getString("file-type").equals("oosgz")) {
            	datf = new File(RedProtect.pathData, "data_" + world + ".regions");        	
            } else if (RPConfig.getString("file-type").equals("yml")) {
            	datf = new File(RedProtect.pathData, "data_" + world + ".yml");        	
            }
                        
            if (RPConfig.getString("file-type").equals("oosgz")) {
            	HashMap<Long, Set<String>> lcos = new HashMap<Long, Set<String>>(this.regions.size());
                for (Map.Entry<Long, LargeChunkObject> e : this.regionslco.entrySet()) {
                  if ((e != null) && (e.getValue() != null)) {
                    if (((LargeChunkObject)e.getValue()).regions != null)
                    {
                      HashSet<String> newRegions = new HashSet<String>();
                      for (Region r : ((LargeChunkObject)e.getValue()).regions) {
                        newRegions.add(r.getName());
                      }
                      lcos.put(e.getKey(), newRegions);
                    }
                  }
                }
                HashMap<String, Region> newRegions2 = new HashMap<String, Region>();
                for (LargeChunkObject lco : this.regionslco.values()) {
                  if (lco.regions != null) {
                    for (Region r : lco.regions) {
                      newRegions2.put(r.getName(), r);
                    }
                  }
                }
                this.backupRegions(datf);
                ObjectOutputStream oosgz = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(datf)));
                oosgz.writeObject(lcos);
                oosgz.writeObject(newRegions2);
                oosgz.close();
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
        			fileDB.set(rname+".flags",r.flags);	
        			fileDB.set(rname+".value",r.getValue());	
        		}	 

        		try {
        			this.backupRegions(datf);
        			fileDB.save(datf);
        		} catch (IOException e) {
        			RedProtect.logger.severe("Error during save database file for world " + world + ": ");
        			e.printStackTrace();
        		}        		
        		
            }
            
        }
        catch (FileNotFoundException e2) {
            e2.printStackTrace();
        }
        catch (IOException e3) {
            e3.printStackTrace();
        }
        catch (Exception e4) {
            e4.printStackTrace();
        }
    }
    
    private void backupRegions(File datf) {
        if (!RPConfig.getBool("flat-file.backup")) {
            return;
        }
        File dataBackup = new File(RedProtect.pathData, "data_" + this.getWorld().getName() + ".backup");
        dataBackup.delete();
        datf.renameTo(dataBackup);
        try {
            datf.createNewFile();
        }
        catch (IOException e) {
            e.printStackTrace();
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
    public Region isSurroundingRegion(Region r) {
    	for (Region other : this.getRegionLcos(r)) {  
			if (other != null){
            	if (other != null && r.inBoundingRect(other.getCenterX(), other.getCenterZ()) && r.intersects(other.getCenterX(), other.getCenterZ())) {
                    return other;
            	}
            }
		}
        return null;
    }
    
    @Override
    public void load() {
        String world = this.getWorld().getName();
        if (RPConfig.getString("file-type").equals("oosgz")) {
            this.load(String.valueOf(RedProtect.pathData) + "data_" + world + ".regions");        	
        } else if (RPConfig.getString("file-type").equals("yml")) {        	
        	File oldf = new File(String.valueOf(RedProtect.pathData) + world + ".yml");
        	File newf = new File(String.valueOf(RedProtect.pathData) + "data_" + world + ".yml");
            if (oldf.exists()){
            	oldf.renameTo(newf);
            }            
            this.load(String.valueOf(RedProtect.pathData) + "data_" + world + ".yml");        	
        }
    }
    
    @SuppressWarnings("unchecked")
	private void load(String path) {
        String world = this.getWorld().getName();
        String datbackf = String.valueOf(RedProtect.pathData) + "data_" + world + ".backup";
        File f = new File(path);
        if (!f.exists()) {
            try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        try {
            if (!RPUtil.isFileEmpty(path)) {
                ObjectInputStream ois = null;       
                
                //oosgz type file
                if (RPConfig.getString("file-type").equals("oosgz")) {
                    RedProtect.logger.debug("Load world " + this.world.getName() + ". File type: oosgz");
                    if (ois == null) {
                      ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream(path)));
                    }
                    Object oois = ois.readObject();
                    HashMap<Long, Set<String>> lcos;
                    if ((oois instanceof HashMap)) {
                      lcos = (HashMap<Long, Set<String>>)oois;
                    } else {
                      lcos = null;
                    }
                    oois = ois.readObject();
                    HashMap<String, Region> newRegions;
                    if ((oois instanceof HashMap)) {
                      newRegions = (HashMap<String, Region>)oois;
                    } else {
                      newRegions = null;
                    }
                    ois.close();
                    this.regionslco = new HashMap<>(lcos.size());
                    for (Map.Entry<Long, Set<String>> ss : lcos.entrySet()) {
                      this.regionslco.put(ss.getKey(), new LargeChunkObject(newRegions, (Set<String>)ss.getValue()));
                    }
                    
                    for (LargeChunkObject lco : this.regionslco.values()) {
                        for (Region r : lco.regions) {
                          regions.put(r.getName(), r);
                        }
                    }
                }
                
                //yml type file
                if (RPConfig.getString("file-type").equals("yml")) {
                	RPYaml fileDB = new RPYaml();
                	RedProtect.logger.debug("Load world " + this.world.getName() + ". File type: yml");
                	try {
            			fileDB.load(f);
            		} catch (FileNotFoundException e) {
            			RedProtect.logger.severe("DB file not found!");
            			RedProtect.logger.severe("File:" + f.getName());
            			e.printStackTrace();
            		} catch (IOException e) {
            			e.printStackTrace();
            		} catch (InvalidConfigurationException e) {
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
            	    	  String name = fileDB.getString(rname+".name");
            	    	  List<String> owners = fileDB.getStringList(rname+".owners");
            	    	  List<String> members = fileDB.getStringList(rname+".members");
            	    	  String creator = fileDB.getString(rname+".creator");	    	  
            	    	  String welcome = fileDB.getString(rname+".welcome");
            	    	  int prior = fileDB.getInt(rname+".priority");
            	    	  String date = fileDB.getString(rname+".lastvisit");
            	    	  Double value = fileDB.getDouble(rname+".value");
            	    	  if (owners.size() == 0){
            	    		  owners.add(creator);
            	    	  }			    	
            	    	  fileDB = RPUtil.fixdbFlags(fileDB, rname);
            	    	  Region newr = new Region(name, owners, members, creator, new int[] {minX,minX,maxX,maxX}, new int[] {minZ,minZ,maxZ,maxZ}, prior, world, date, RPConfig.getDefFlagsValues(), welcome, value);
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
                
            } else {
                if (RPConfig.getBool("flat-file.backup") && this.backupExists() && !path.equalsIgnoreCase(datbackf)) {
                    this.load(datbackf);
                    RedProtect.logger.info("Data file is blank, Reading from " + datbackf);
                    return;
                }
                RedProtect.logger.info("Creating a new data file" + datbackf);
            }
            
        }
        catch (FileNotFoundException e2) {
            e2.printStackTrace();
        }
        catch (ZipException e5) {
            if (RPConfig.getBool("flat-file.backup") && this.backupExists() && !path.equalsIgnoreCase(datbackf)) {
                this.load(datbackf);
                RedProtect.logger.info("The data file is corrupt. Loading from " + datbackf);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (ClassNotFoundException e3) {
            e3.printStackTrace();
        }
        catch (Exception e4) {
            e4.printStackTrace();
        }
    }
    
    private boolean backupExists() {
        String world = this.getWorld().getName();
        String datbackf = String.valueOf(RedProtect.pathData) + "data_" + world + ".backup";
        return new File(datbackf).exists();
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
    
    @Override
    public boolean regionExists(Region region) {
    	if (regions.containsValue(region)){
			return true;
		}
		return false;
    }
    
    public World getWorld() {
        return this.world;
    }
    
    @Override
    public Set<Region> getPossibleIntersectingRegions(Region r) {
    	Set<Region> ret = new HashSet<Region>();
		int cmaxX = LargeChunkObject.convertBlockToLCO(r.getMaxMbrX());
        int cmaxZ = LargeChunkObject.convertBlockToLCO(r.getMaxMbrZ());
        int cminX = LargeChunkObject.convertBlockToLCO(r.getMinMbrX());
        int cminZ = LargeChunkObject.convertBlockToLCO(r.getMinMbrZ());
        for (int xl = cminX; xl <= cmaxX; ++xl) {
            for (int zl = cminZ; zl <= cmaxZ; ++zl) {
            	Region regs = this.getRegion(xl, zl);
                if (regs != null) {
                	if (r.inBoundingRect(regs)) {
                        ret.add(regs);
                    }
                }
            }            
        }
        return ret;
    }
    
    public List<Region> getRegionLcos(Region r) {
    	List<Region> ret = new LinkedList<Region>();
        int cmaxX = LargeChunkObject.convertBlockToLCO(r.getMaxMbrX());
        int cmaxZ = LargeChunkObject.convertBlockToLCO(r.getMaxMbrZ());
        int cminX = LargeChunkObject.convertBlockToLCO(r.getMinMbrX());
        int cminZ = LargeChunkObject.convertBlockToLCO(r.getMinMbrZ());
        for (int xl = cminX; xl <= cmaxX; ++xl) {
            for (int zl = cminZ; zl <= cmaxZ; ++zl) {
            	Region regs = this.getRegion(xl, zl);
                if (regs != null) {
                      ret.add(regs);
                    }
                }
            }
        return ret;
    }
    
	@Override
	public Set<Region> getRegions(int x, int z) {
		Set<Region> regionl = new HashSet<Region>();
		for (Region r:regions.values()){
			if (x <= r.getMaxMbrX() && x >= r.getMinMbrX() && z <= r.getMaxMbrZ() && z >= r.getMinMbrZ()){
				regionl.add(r);
			}
		}
		return regionl;
	}

	@Override
	public Region getTopRegion(int x, int z) {
		Map<Integer,Region> regionlist = new HashMap<Integer,Region>();
		int max = 0;
		for (Region r:regions.values()){
			if (x <= r.getMaxMbrX() && x >= r.getMinMbrX() && z <= r.getMaxMbrZ() && z >= r.getMinMbrZ()){
				regionlist.put(r.getPrior(), r);
			}
		}
		if (regionlist.size() > 0){
			max = Collections.max(regionlist.keySet());
        }
		return regionlist.get(max);
	}
	
	@Override
	public Region getLowRegion(int x, int z) {
		Map<Integer,Region> regionlist = new HashMap<Integer,Region>();
		int min = 0;
		for (Region r:regions.values()){
			if (x <= r.getMaxMbrX() && x >= r.getMinMbrX() && z <= r.getMaxMbrZ() && z >= r.getMinMbrZ()){
				regionlist.put(r.getPrior(), r);
			}
		}
		if (regionlist.size() > 0){
			min = Collections.min(regionlist.keySet());
        }
		return regionlist.get(min);
	}
	
	public Map<Integer,Region> getGroupRegion(int x, int z) {
		Map<Integer,Region> regionlist = new HashMap<Integer,Region>();
		for (Region r:regions.values()){
			if (x <= r.getMaxMbrX() && x >= r.getMinMbrX() && z <= r.getMaxMbrZ() && z >= r.getMinMbrZ()){
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
