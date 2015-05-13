package br.net.fabiozumbi12.RedProtect;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandException;
import org.bukkit.entity.Player;

class WorldMySQLRegionManager implements WorldRegionManager{

	static String url;
    static String baseurl = "jdbc:mysql://";
    static String driver = "com.mysql.jdbc.Driver";
    static String dbname;
    static boolean dbexists;
    Connection dbcon;
    
    static {
        WorldMySQLRegionManager.url = "jdbc:mysql://"+RPConfig.getString("mysql.host")+"/";
        WorldMySQLRegionManager.dbexists = false;
    }
    
    HashMap<String, Region> regions;
    World world;
    //HashMap<Long, LargeChunkObject> regionslco;
    
    public WorldMySQLRegionManager(World world) throws Exception{
        super();
        //this.regionslco = new HashMap<Long, LargeChunkObject>(100);
        this.regions = new HashMap<String, Region>();
        this.world = world;
        
        this.dbcon = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
        }
        catch (ClassNotFoundException e2) {
            RedProtect.logger.severe("Couldn't find the driver for MySQL! com.mysql.jdbc.Driver.");
            RedProtect.plugin.disable();
            return;
        }
        WorldMySQLRegionManager.dbname = RPConfig.getString("mysql.db-name") + "_" + world.getName();
        Statement st = null;
        try {
            if (!this.checkDBExists()) {
                Connection con = DriverManager.getConnection(WorldMySQLRegionManager.url, RPConfig.getString("mysql.user-name"), RPConfig.getString("mysql.user-pass"));
                st = con.createStatement();
                st.executeUpdate("CREATE DATABASE " + WorldMySQLRegionManager.dbname);
                RedProtect.logger.info("Created database '" + WorldMySQLRegionManager.dbname + "'!");
                st.close();
                st = null;
                con = DriverManager.getConnection(String.valueOf(WorldMySQLRegionManager.url) + WorldMySQLRegionManager.dbname, RPConfig.getString("mysql.user-name"), RPConfig.getString("mysql.user-pass"));
                st = con.createStatement();
                st.executeUpdate("CREATE TABLE region(name varchar(16) PRIMARY KEY NOT NULL, creator varchar(16), owners varchar(255), members varchar(255), maxMbrX int, minMbrX int, maxMbrZ int, minMbrZ int, centerX int, centerZ int, date varchar(10), wel varchar(64), prior int, world varchar(16))");
                st.close();
                st = null;
                RedProtect.logger.info("Created table: 'Region'!");    
                st = con.createStatement();
                st.executeUpdate("CREATE TABLE region_flags(region varchar(16) NOT NULL, flag varchar(20) NOT NULL, value varchar(255) NOT NULL)");
                st.close();
                st = null;
                RedProtect.logger.info("Created table: 'Region Flags'!"); 
            }
            this.dbcon = DriverManager.getConnection(WorldMySQLRegionManager.url + WorldMySQLRegionManager.dbname, RPConfig.getString("mysql.user-name"), RPConfig.getString("mysql.user-pass"));
        }
        catch (CommandException e3) {
            RedProtect.logger.severe("Couldn't connect to mysql! Make sure you have mysql turned on and installed properly, and the service is started.");
            throw new Exception("Couldn't connect to mysql!");
        }
        catch (SQLException e) {
            e.printStackTrace();
            RedProtect.logger.severe("There was an error while parsing SQL, redProtect will shut down to avoid further damage.");
            throw new Exception("SQLException!");
        }
        finally {
            if (st != null) {
                st.close();
            }
        }
    }
    
    private boolean checkDBExists() throws SQLException {
        if (WorldMySQLRegionManager.dbexists) {
            return true;
        }
        try {
        	Connection con = DriverManager.getConnection(WorldMySQLRegionManager.url, RPConfig.getString("mysql.user-name"), RPConfig.getString("mysql.user-pass"));
            DatabaseMetaData meta = con.getMetaData();
            ResultSet rs = meta.getCatalogs();
            while (rs.next()) {
                String listOfDatabases = rs.getString("TABLE_CAT");
                if (listOfDatabases.equalsIgnoreCase(WorldMySQLRegionManager.dbname)) {
                    return WorldMySQLRegionManager.dbexists = true;
                }
            }
        } catch (SQLException e){
        	e.printStackTrace();
        }        
        return false;
    }
    
    @Override
    public void remove(Region r) {
    	removeLiveRegion(r);
        if (this.regions.containsKey(r.getName())){
        	this.regions.remove(r.getName());
        }
    }   
    private void removeLiveRegion(Region r) {
        if (this.regionExists(r)) {
            try {
                Statement st = this.dbcon.createStatement();
                st = this.dbcon.createStatement();
                st.executeUpdate("DELETE FROM region WHERE name = '" + r.getName() + "'");
                st.close();
                st = this.dbcon.createStatement();
                st.executeUpdate("DELETE FROM region_flags WHERE region = '" + r.getName() + "'");
                st.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    public Set<Region> getRegionsIntersecting(int bx, int bz) {
		Set<Region> ret = new HashSet<Region>();
		try {
            Statement st = this.dbcon.createStatement();
            ResultSet rs = st.executeQuery("SELECT name FROM region WHERE " + bx + "<=maxMbrX AND " + bx + ">=minMbrX AND " + bz + "<=maxMbrZ AND " + bz + ">=minMbrZ");
            while (rs.next()) {
            	ret.add(this.getRegion(rs.getString("name")));
            }
            st.close();
            rs.close();
        }
		catch (SQLException e) {
            e.printStackTrace();
        }
		/*
		for (Region r:regions.values()){
			if (bx <= r.getMaxMbrX() && bx >= r.getMinMbrX() && bz <= r.getMaxMbrZ() && bz >= r.getMinMbrZ()){
				ret.add(r);
			}
		}*/
		RedProtect.logger.debug("Rects intersecting " + bx + ", " + bz + ": ");
        for (Region r : ret) {
            RedProtect.logger.debug(String.valueOf(r.getName()) + r.info());
        }
        return ret;
	}
    
    @Override
    public Set<Region> getRegions(String uuid) {
    	Set<Region> regionsp = new HashSet<Region>();
    	try {
            Statement st = this.dbcon.createStatement();
            ResultSet rs = st.executeQuery("SELECT name FROM region WHERE creator='"+uuid+"'");
            while (rs.next()) {
            	regionsp.add(this.getRegion(rs.getString("name")));
            }
            st.close();
            rs.close();
        }
		catch (SQLException e) {
            e.printStackTrace();
        }
    	/*
		for (Region r:regions.values()){
			if (r.getCreator().equals(uuid)){
				regionsp.add(r);
			}
		}*/
		return regionsp;
    }
    
    @Override
    public Set<Region> getMemberRegions(String uuid) {
    	Set<Region> regionsp = new HashSet<Region>();
    	try {
            Statement st = this.dbcon.createStatement();
            ResultSet rs = st.executeQuery("SELECT name FROM region WHERE members LIKE '%"+uuid+"%' OR owners LIKE '%"+uuid+"%'");
            while (rs.next()) {
            	regionsp.add(this.getRegion(rs.getString("name")));
            }
            st.close();
            rs.close();
        }
		catch (SQLException e) {
            e.printStackTrace();
        }
    	/*
		for (Region r:regions.values()){
			if (r.isMember(uuid) || r.isOwner(uuid)){
				regionsp.add(r);
			}
		}*/
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
    public Region getRegion(final String rname) {
    	if (!regions.containsKey(rname)){
    		if (rname == null){
    			return null;
    		}
    		try {
                Statement st = this.dbcon.createStatement();
                ResultSet rs = st.executeQuery("SELECT * FROM region WHERE name='"+rname+"'");            
                if (rs.next()){ 
                	LinkedList<String> owners = new LinkedList<String>();
                    List<String> members = new ArrayList<String>();
                    HashMap<String, Object> flags = new HashMap<String, Object>();  
                    
                    String creator = rs.getString("creator");
                    int maxMbrX = rs.getInt("maxMbrX");
                    int minMbrX = rs.getInt("minMbrX");
                    int maxMbrZ = rs.getInt("maxMbrZ");
                    int minMbrZ = rs.getInt("minMbrZ");
                    int prior = rs.getInt("prior");
                    String world = rs.getString("world");
                    String date = rs.getString("date");
                    String wel = rs.getString("wel");
                    
                    for (String member:rs.getString("members").split(", ")){
                    	if (member.length() > 0){
                    		members.add(member);
                    	}                	
                    }
                    for (String owner:rs.getString("owners").split(", ")){
                    	if (owner.length() > 0){
                    		owners.add(owner);
                    	}                	
                    }
                    
                    Statement fst = this.dbcon.createStatement();
                    ResultSet frs = fst.executeQuery("SELECT value,flag FROM region_flags WHERE region = '" + rname + "'");
                    while (frs.next()){
                    	flags.put(frs.getString("flag"), RPUtil.parseObject(frs.getString("value")));
                    }   
                    fst.close();
                    frs.close();
                    
                    regions.put(rname, new Region(rname, owners, members, creator, maxMbrX, minMbrX, maxMbrZ, minMbrZ, flags, wel, prior, world, date));
                }    
                st.close(); 
                rs.close();
                RedProtect.logger.debug("Adding region to cache: "+rname);
                Bukkit.getScheduler().runTaskLater(RedProtect.plugin, new Runnable(){
                		@Override
                		public void run(){
                		if (regions.containsKey(rname)){
                			regions.remove(rname);
                			RedProtect.logger.debug("Removed cached region: "+rname);
                		}
                		}                	
                }, (20*60)*RPConfig.getInt("mysql.region-cache-minutes"));
                
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
    	} 
    	return regions.get(rname);
    	/*
    	return regions.get(rname);*/
    }
    
    @Override
    public void save() {
    	/*
    	for (Region r:this.regions.values()){   
    		addLiveRegion(r);
    	}*/
    }    

	@Override
    public void add(Region r) {
		addLiveRegion(r);
        //this.regions.put(r.getName(), r);        
    }
	
    private void addLiveRegion(Region r){
    	if (!this.regionExists(r)) {
            try {                
                Statement st = this.dbcon.createStatement();
                for (String flag:r.flags.keySet()){
                	st = this.dbcon.createStatement();       
                	st.executeUpdate("INSERT INTO region_flags (region,flag,value) VALUES ('" + r.getName() + "', '" + flag + "', '" + r.flags.get(flag).toString()+"')");
                	st.close();
                }          
                st = this.dbcon.createStatement();
                st.executeUpdate("INSERT INTO region (name,creator,owners,members,maxMbrX,minMbrX,maxMbrZ,minMbrZ,centerX,centerZ,date,wel,prior,world) VALUES "
                		+ "('" +r.getName() + "', '" + 
                		r.getCreator() + "', '" + 
                		r.getOwners().toString().replace("[", "").replace("]", "")  + "', '" + 
                		r.getMembers().toString().replace("[", "").replace("]", "") + "', '" + 
                		r.getMaxMbrX() + "', '" + 
                		r.getMinMbrX() + "', '" + 
                		r.getMaxMbrZ() + "', '" + 
                		r.getMinMbrZ() + "', '" + 
                		r.getCenterX() + "', '" + 
                		r.getCenterZ() + "', '" + 
                		r.getDate() + "', '" +
                		r.getWelcome() + "', '" + 
                		r.getPrior() + "', '" + 
                		r.getWorld()+"')");                    
                st.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        } 
    }
    
    @Override
    public void updateLiveFlags(String rname, String flag, String value){
    	try{
    	Statement st = null;      
    	if (flagExists(rname,flag)){
    		st = this.dbcon.createStatement();       
        	st.executeUpdate("UPDATE region_flags SET value='"+value+"' WHERE region='"+rname+"' AND flag ='"+flag+"'");
        	st.close();
    	} else {
    		st = this.dbcon.createStatement();       
        	st.executeUpdate("INSERT INTO region_flags (region,flag,value) VALUES ('"+rname+"', '"+flag+"', '"+value+"')");
        	st.close();
    	}
    	} catch (SQLException e){
    		RedProtect.logger.severe("RedProtect can't update the region " + rname + ", please verify the Mysql Connection and table structures.");
            e.printStackTrace();
    	}        
    }
    
    @Override
    public void removeLiveFlags(String rname, String flag){
    	try{
    	Statement st = null;      
    	if (flagExists(rname,flag)){
    		st = this.dbcon.createStatement();
            st.executeUpdate("DELETE FROM region_flags WHERE region = '" + rname + "' AND flag = '"+flag+"'");
            st.close();
    	} 
    	} catch (SQLException e){
    		RedProtect.logger.severe("RedProtect can't remove flag " + flag + " from " + rname + ", please verify the Mysql Connection and table structures.");
            e.printStackTrace();
    	}        
    }
    
    @Override
    public void updateLiveRegion(String rname, String columm, String value){
    	try {                
            Statement st = this.dbcon.createStatement();
            st = this.dbcon.createStatement();
            st.executeUpdate("UPDATE region SET "+columm+"='"+value+"' WHERE name='" + rname + "'");
            st.close();
        }
        catch (SQLException e) {
        	RedProtect.logger.severe("RedProtect can't save the region " + rname + ", please verify the Mysql Connection and table structures.");
            e.printStackTrace();
        } 
    }
        
    @Override
    public int getTotalRegionSize(String uuid) {		
		/*
		for (Region r:regions.values()){
			if (r.getCreator().equalsIgnoreCase(uuid)){
				regionslist.add(r);
			}
		}*/
		int total = 0;
		for (Region r2 : this.getRegions(uuid)) {
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
    	/*
        try {
            Statement st = this.dbcon.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM region");            
            while (rs.next()){ 
            	LinkedList<String> owners = new LinkedList<String>();
                List<String> members = new ArrayList<String>();
                HashMap<String, Object> flags = new HashMap<String, Object>();  
                
                String rname = rs.getString("name");
                String creator = rs.getString("creator");
                int maxMbrX = rs.getInt("maxMbrX");
                int minMbrX = rs.getInt("minMbrX");
                int maxMbrZ = rs.getInt("maxMbrZ");
                int minMbrZ = rs.getInt("minMbrZ");
                int prior = rs.getInt("prior");
                String world = rs.getString("world");
                String date = rs.getString("date");
                String wel = rs.getString("wel");
                
                for (String member:rs.getString("members").split(", ")){
                	if (member.length() > 0){
                		members.add(member);
                	}                	
                }
                for (String owner:rs.getString("owners").split(", ")){
                	if (owner.length() > 0){
                		owners.add(owner);
                	}                	
                }
                
                Statement fst = this.dbcon.createStatement();
                ResultSet frs = fst.executeQuery("SELECT value,flag FROM region_flags WHERE region = '" + rname + "'");
                while (frs.next()){
                	flags.put(frs.getString("flag"), RPUtil.parseObject(frs.getString("value")));
                }   
                fst.close();
                frs.close();
                
                this.regions.put(rname, new Region(rname, owners, members, creator, maxMbrX, minMbrX, maxMbrZ, minMbrZ, flags, wel, prior, world, date));
            }    
            st.close(); 
            rs.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        } 
        */
    }
    
    @Override
    public Set<Region> getRegionsNear(Player player, int radius) {
    	int px = player.getLocation().getBlockX();
        int pz = player.getLocation().getBlockZ();
        Set<Region> ret = new HashSet<Region>();
        
        try {
            Statement st = this.dbcon.createStatement();
            ResultSet rs = st.executeQuery("SELECT name FROM region WHERE ABS(centerX-" + px + ")<=" + radius + " AND ABS(centerZ-" + pz + ")<=" + radius);
            while (rs.next()) {
                ret.add(this.getRegion(rs.getString("name")));
            }
            st.close();
            rs.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        /*
		for (Region r:regions.values()){
			RedProtect.logger.debug("Radius: " + radius);
			RedProtect.logger.debug("X radius: " + Math.abs(r.getCenterX() - px) + " - Z radius: " + Math.abs(r.getCenterZ() - pz));
			if (Math.abs(r.getCenterX() - px) <= radius && Math.abs(r.getCenterZ() - pz) <= radius){
				ret.add(r);
			}
		}*/
        return ret;
    }
    
    private boolean regionExists(String name) {
        int total = 0;
        try {
            Statement st = this.dbcon.createStatement();
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM region WHERE name = '" + name + "'");
            if (rs.next()) {
                total = rs.getInt("COUNT(*)");
            }
            st.close();
            rs.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return total > 0;
    }
    
    private boolean flagExists(String rname, String flag) {
        int total = 0;
        try {
            Statement st = this.dbcon.createStatement();
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM region_flags WHERE region = '"+rname+"' AND flag='"+flag+"'");
            if (rs.next()) {
                total = rs.getInt("COUNT(*)");
            }
            st.close();
            rs.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return total > 0;
    }
    
    @Override
    public boolean regionExists(Region region) {
        return this.regionExists(region.getName());
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
		try {
            Statement st = this.dbcon.createStatement();
            ResultSet rs = st.executeQuery("SELECT name FROM region WHERE " + x + "<=maxMbrX AND " + x + ">=minMbrX AND " + z + "<=maxMbrZ AND " + z + ">=minMbrZ");
            while (rs.next()) {
            	regionl.add(this.getRegion(rs.getString("name")));
            }
            st.close();
            rs.close();
        }
		catch (SQLException e) {
            e.printStackTrace();
        }
		/*
		for (Region r:regions.values()){
			if (x <= r.getMaxMbrX() && x >= r.getMinMbrX() && z <= r.getMaxMbrZ() && z >= r.getMinMbrZ()){
				regionl.add(r);
			}
		}*/
		return regionl;
	}

	@Override
	public Region getTopRegion(int x, int z) {
		Map<Integer,Region> regionlist = new HashMap<Integer,Region>();
		int max = 0;
		
		for (Region r:this.getRegions(x, z)){
				regionlist.put(r.getPrior(), r);
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

		for (Region r:this.getRegions(x, z)){
			regionlist.put(r.getPrior(), r);
	    }
		
		if (regionlist.size() > 0){
			min = Collections.min(regionlist.keySet());
        }
		return regionlist.get(min);
	}
	
	public Map<Integer,Region> getGroupRegion(int x, int z) {
		Map<Integer,Region> regionlist = new HashMap<Integer,Region>();
		
		for (Region r:this.getRegions(x, z)){
			regionlist.put(r.getPrior(), r);
	    }
		return regionlist;
	}
	
	@Override
	public Set<Region> getAllRegions() {		
		Set<Region> allregions = new HashSet<Region>();		
		//allregions.addAll(regions.values());
		return allregions;
	}

	@Override
	public void clearRegions() {
		try {
            Statement st = this.dbcon.createStatement();
            st.executeUpdate("DELETE FROM region_flags WHERE region = '*'");

            st = this.dbcon.createStatement();
            st.executeUpdate("DELETE FROM region WHERE name = '*'");
            st.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
		//regions.clear();		
	}

	@Override
	public void closeConn() {
		try {
			this.dbcon.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public int getTotalRegionNum(){
		int total = 0;
		try {
            Statement st = this.dbcon.createStatement();
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM region");
            if (rs.next()) {
                total = rs.getInt("COUNT(*)");
            }
            st.close();
            rs.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
		return total;
	}
}
