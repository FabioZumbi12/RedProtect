package br.net.fabiozumbi12.RedProtect.Bukkit;

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

import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPConfig;

class WorldFlatFileRegionManager implements WorldRegionManager{

    private final HashMap<String, Region> regions;
    private final World world;
    private final String pathData = RedProtect.get().getDataFolder() + File.separator + "data" + File.separator;

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
        if (this.regions.containsValue(r)){
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
            RedProtect.get().logger.debug("RegionManager.Save(): File type is " + RPConfig.getString("file-type"));
            String world = this.getWorld().getName();
            File datf;

            if (RPConfig.getString("file-type").equals("yml"))  {
            	datf = new File(pathData, "data_" + world + ".yml");
            	YamlConfiguration fileDB = new YamlConfiguration();

        		for (Region r:regions.values()){
        			if (r.getName() == null){
        				continue;
        			}

        			if (RPConfig.getBool("flat-file.region-per-file")) {
        				if (!r.toSave()){
            				continue;
            			}
        				fileDB = new YamlConfiguration();
                    	datf = new File(pathData, world+File.separator+r.getName()+".yml");
                    }

        			fileDB = RPUtil.addProps(fileDB, r);
        			saved++;

        			if (RPConfig.getBool("flat-file.region-per-file")) {
        				saveYaml(fileDB, datf);
        				r.setToSave(false);
        			}
        		}

        		if (!RPConfig.getBool("flat-file.region-per-file")) {
        			RPUtil.backupRegions(fileDB, world);
        			saveYaml(fileDB, datf);
    			} else {
    				//remove deleted regions
    				File wfolder = new File(pathData + world);
    				if (wfolder.exists()){
    					File[] listOfFiles = new File(pathData + world).listFiles();
                		for (File region:listOfFiles){
                			if (region.isFile() && !regions.containsKey(region.getName().replace(".yml", ""))){
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

    private void saveYaml(YamlConfiguration fileDB, File file){
    	try {
    		fileDB.save(file);
		} catch (IOException e) {
			RedProtect.get().logger.severe("Error during save database file for world " + world + ": ");
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

            if (RPConfig.getString("file-type").equals("yml")) {
            	if (RPConfig.getBool("flat-file.region-per-file")) {
            		File f = new File(pathData + world);
            		if (!f.exists()){
            			f.mkdir();
            		}
            		File[] listOfFiles = f.listFiles();
            		for (File region:listOfFiles){
            			if (region.getName().endsWith(".yml")){
            				this.load(region.getPath());
            			}
            		}
    			} else {
    				File oldf = new File(pathData + world + ".yml");
                	File newf = new File(pathData + "data_" + world + ".yml");
                    if (oldf.exists()){
                    	oldf.renameTo(newf);
                    }
                    this.load(pathData + "data_" + world + ".yml");
    			}

            }
		} catch (FileNotFoundException | ClassNotFoundException e) {
				e.printStackTrace();
		}
    }

	private void load(String path) throws FileNotFoundException, ClassNotFoundException {
        File f = new File(path);
        if (!f.exists()) {
            try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }

        if (RPConfig.getString("file-type").equals("yml")) {
        	YamlConfiguration fileDB = new YamlConfiguration();
        	RedProtect.get().logger.debug("Load world " + this.world.getName() + ". File type: yml");
        	try {
    			fileDB.load(f);
    		} catch (FileNotFoundException e) {
    			RedProtect.get().logger.severe("DB file not found!");
    			RedProtect.get().logger.severe("File:" + f.getName());
    			e.printStackTrace();
    		} catch (Exception e) {
    			e.printStackTrace();
    		}

        	for (String rname:fileDB.getKeys(false)){
        		Region newr = RPUtil.loadProps(fileDB, rname, this.world);
    	    	newr.setToSave(false);
        	    regions.put(rname,newr);
        	}
        }
    }

    @Override
    public Set<Region> getRegionsNear(Player player, int radius) {
    	int px = player.getLocation().getBlockX();
        int pz = player.getLocation().getBlockZ();
        SortedSet<Region> ret = new TreeSet<>(Comparator.comparing(Region::getName));

		for (Region r:regions.values()){
			RedProtect.get().logger.debug("Radius: " + radius);
			RedProtect.get().logger.debug("X radius: " + Math.abs(r.getCenterX() - px) + " - Z radius: " + Math.abs(r.getCenterZ() - pz));
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
	public Set<Region> getInnerRegions(Region region) {
		Set<Region> regionl = new HashSet<>();
		for (Region r:regions.values()){
			if (r.getMaxMbrX() <= region.getMaxMbrX() && r.getMaxY() <= region.getMaxY() && r.getMaxMbrZ() <= region.getMaxMbrZ() && r.getMinMbrX() >= region.getMinMbrX() && r.getMinY() >= region.getMinY() && r.getMinMbrZ() >= region.getMinMbrZ()){
				regionl.add(r);
			}
		}
		return regionl;
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
