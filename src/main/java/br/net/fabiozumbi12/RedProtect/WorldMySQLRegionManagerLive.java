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
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandException;
import org.bukkit.entity.Player;

class WorldMySQLRegionManagerLive implements WorldRegionManager{
    static String url;
    static String baseurl = "jdbc:mysql://";
    static String driver = "com.mysql.jdbc.Driver";
    static String dbname;
    static boolean dbexists;
    Connection dbcon;
    
    static {
        WorldMySQLRegionManagerLive.url = "jdbc:mysql://"+RPConfig.getString("mysql-host")+"/";
        WorldMySQLRegionManagerLive.dbexists = false;
    }
    
    public WorldMySQLRegionManagerLive(World w) throws Exception {
        super();
        this.dbcon = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
        }
        catch (ClassNotFoundException e2) {
            RedProtect.logger.severe("Couldn't find the driver for MySQL! com.mysql.jdbc.Driver.");
            RedProtect.plugin.disable();
            return;
        }
        WorldMySQLRegionManagerLive.dbname = RPConfig.getString("mysql-db-name") + "_" + w.getName();
        Statement st = null;
        try {
            if (!this.checkDBExists()) {
                Connection con = DriverManager.getConnection(WorldMySQLRegionManagerLive.url, RPConfig.getString("mysql-user-name"), RPConfig.getString("mysql-user-pass"));
                st = con.createStatement();
                st.executeUpdate("CREATE DATABASE " + WorldMySQLRegionManagerLive.dbname);
                RedProtect.logger.info("Created database '" + WorldMySQLRegionManagerLive.dbname + "'!");
                st.close();
                st = null;
                con = DriverManager.getConnection(String.valueOf(WorldMySQLRegionManagerLive.url) + WorldMySQLRegionManagerLive.dbname, RPConfig.getString("mysql-user-name"), RPConfig.getString("mysql-user-pass"));
                st = con.createStatement();
                st.executeUpdate("CREATE TABLE region(uid int AUTO_INCREMENT PRIMARY KEY, name varchar(16), creator varchar(16), owners varchar(255), members varchar(255), maxMbrX int, minMbrX int, maxMbrZ int, minMbrZ int, centerX int, centerZ int, date varchar(10), wel varchar(64), prior int, world varchar(16))");
                st.close();
                st = null;
                RedProtect.logger.info("Created table: 'Region'!");    
                st = con.createStatement();
                st.executeUpdate("CREATE TABLE region_flags(uid int AUTO_INCREMENT PRIMARY KEY, region varchar(16), flag varchar(255), value varchar(255))");
                st.close();
                st = null;
                RedProtect.logger.info("Created table: 'Region Flags'!"); 
            }
            this.dbcon = DriverManager.getConnection(WorldMySQLRegionManagerLive.url + WorldMySQLRegionManagerLive.dbname, RPConfig.getString("mysql-user-name"), RPConfig.getString("mysql-user-pass"));
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
        if (WorldMySQLRegionManagerLive.dbexists) {
            return true;
        }
        try {
        	Connection con = DriverManager.getConnection(WorldMySQLRegionManagerLive.url, RPConfig.getString("mysql-user-name"), RPConfig.getString("mysql-user-pass"));
            DatabaseMetaData meta = con.getMetaData();
            ResultSet rs = meta.getCatalogs();
            while (rs.next()) {
                String listOfDatabases = rs.getString("TABLE_CAT");
                if (listOfDatabases.equalsIgnoreCase(WorldMySQLRegionManagerLive.dbname)) {
                    return WorldMySQLRegionManagerLive.dbexists = true;
                }
            }
        } catch (SQLException e){
        	e.printStackTrace();
        }        
        return false;
    }
     
    @Override
    public void add(Region r) {
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
                		r.getWorld()+"')", 1);
                ResultSet rs = st.getGeneratedKeys();
                if (!rs.next()) {
                    RedProtect.logger.warning("Couldn't generate Primary Key for SQLManager.add(Region r). Region " + r.getName() + " will not be saved.");
                    return;
                }
                st.close();
                rs.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
        	
        	try {                
                Statement st = this.dbcon.createStatement();
                for (String flag:r.flags.keySet()){
                	st = this.dbcon.createStatement();       
                	st.executeUpdate("UPDATE region_flags (flag,value) VALUES ('" + flag + "', '" + r.flags.get(flag).toString()+"') WHERE region='" + r.getName() + "'");
                	st.close();
                }          
                st = this.dbcon.createStatement();
                st.executeUpdate("UPDATE region (creator,owners,members,maxMbrX,minMbrX,maxMbrZ,minMbrZ,centerX,centerZ,date,wel,prior,world) VALUES "
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
                		r.getWorld()+"') WHERE region='" + r.getName() + "'");
                ResultSet rs = st.getGeneratedKeys();
                if (!rs.next()) {
                    RedProtect.logger.warning("Couldn't generate Primary Key for SQLManager.add(Region r). Region " + r.getName() + " will not be saved.");
                    return;
                }
                st.close();
                rs.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }        	
        }
    }
    
    @Override
    public void remove(Region r) {
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
                String name = rs.getString("name");
                ret.add(this.getRegion(name));
            }
            rs.close();
            st.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        RedProtect.logger.debug("Rects intersecting " + bx + ", " + bz + ": ");
        for (Region r : ret) {
            RedProtect.logger.debug(String.valueOf(r.getName()));
        }
        return ret;
    }
    
    @Override
    public boolean canBuild(Player p, Block b) {
        int bx = b.getX();
        int bz = b.getZ();
        for (Region poly : this.getRegionsIntersecting(bx, bz)) {
            if (poly.intersects(bx, bz)) {
                return poly.canBuild(p);
            }
        }
        return true;
    }
    
    @Override
    public Set<Region> getRegions(String uuid) {
        Set<Region> ls = new HashSet<Region>();
        try {
            Statement st = this.dbcon.createStatement();
            ResultSet rs = st.executeQuery("SELECT name FROM region WHERE creator = '" + uuid + "'");
            while (rs.next()) {
                ls.add(this.getRegion(rs.getString("name")));
            }
            st.close();
            rs.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return ls;
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
    
    public boolean regionExists(String name) {
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
    
    @Override
    public boolean regionExists(Region region) {
        return this.regionExists(region.getName());
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
        if (!this.regionExists(rname)) {
            return null;
        }
        Region ret = null;
        LinkedList<String> owners = new LinkedList<String>();
        List<String> members = new ArrayList<String>();
        int maxMbrX = 0;
        int minMbrX = 0;
        int maxMbrZ = 0;
        int minMbrZ = 0;
        int prior = 0;
        String world = "";
        String date = "";
        String wel = "";
        HashMap<String, Object> flags = new HashMap<String, Object>();
        String creator = "";
        try {
            Statement st = this.dbcon.createStatement();
            ResultSet rs = st.executeQuery("SELECT creator, owners, members, maxMbrX, minMbrX, maxMbrZ, minMbrZ, date, wel, prior, world FROM region WHERE name = '" + rname + "'");
            if (rs.next()){
            	creator = rs.getString("creator");
                maxMbrX = rs.getInt("maxMbrX");
                minMbrX = rs.getInt("minMbrX");
                maxMbrZ = rs.getInt("maxMbrZ");
                minMbrZ = rs.getInt("minMbrZ");
                prior = rs.getInt("prior");
                world = rs.getString("world");
                date = rs.getString("date");
                wel = rs.getString("wel");
                
                for (String member:new String[]{rs.getString("members")}){
                	members.add(member);
                }
                for (String owner:new String[]{rs.getString("owners")}){
                	owners.add(owner);
                }
                                
                st.close();
                rs.close();
            }
            
            for (String flag:RPConfig.getAllFlags()){
            	st = this.dbcon.createStatement();
                rs = st.executeQuery("SELECT value from region_flags WHERE region = '" + rname + "' AND flag = '" +  flag + "'");
                if (rs.next()){
                	if (rs.getObject("value") != null){
                		flags.put(flag, RPUtil.parseObject(rs.getString("value")));
                	} else {
                		continue;
                	}
                }
                st.close();
                rs.close();
            }      
            ret = new Region(rname, owners, members, creator, maxMbrX, minMbrX, maxMbrZ, minMbrZ, flags, wel, prior, world, date);                
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return ret;
    }
    
    @Override
    public int getTotalRegionSize(String uuid) {
        if (uuid == null) {
            return 0;
        }
        int total = 0;
        Set<Region> regions = new HashSet<Region>();
        try {
            Statement st = this.dbcon.createStatement();
            ResultSet rs = st.executeQuery("SELECT name FROM region WHERE creator = '" + uuid + "'");
            if (rs.next()) {
                Region r = this.getRegion(rs.getString("name"));
                int X = r.getCenterX();
            	int Z = r.getCenterZ();
            	int group = getGroupRegion(X,Z).size();
            	if (group > 1){
            		if (getLowRegion(X,Z).equals(r)){
            			regions.add(r);
            		}            		
            	} else {
            		regions.add(r);
            	}
            }
            st.close();
            rs.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        for (Region r2 : regions) {
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
    public void load() {
    }
    
    @Override
    public void save() {
    }
    
    @Override
    public Set<Region> getRegionsNear(Player player, int radius) {
        int px = (int)player.getLocation().getX();
        int pz = (int)player.getLocation().getZ();
        Set<Region> ret = new HashSet<Region>();
        try {
            Statement st = this.dbcon.createStatement();
            ResultSet rs = st.executeQuery("SELECT name FROM region WHERE ABS(centerX-" + px + ")<" + radius + 1 + " AND ABS(centerZ-" + pz + ")<" + radius + 1);
            while (rs.next()) {
                ret.add(this.getRegion(rs.getString("name")));
            }
            st.close();
            rs.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return ret;
    }
    
    @Override
    public void setFlagValue(Region r, String flag, Object value) {
    	RedProtect.logger.debug("Call setFlagValue in WorldMySQLRegionManager.class");
    	try {
			Statement st = this.dbcon.createStatement();
			st.executeUpdate("UPDATE region_flags SET value='" + value.toString() + "' WHERE region='" + r.getName() + "' AND flag='" + flag + "'");
	        st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	r.setFlag(flag, value);
    }
        
    @Override
    public void setRegionName(Region r, String name) {
    	RedProtect.logger.debug("Call setRegionName in WorldMySQLRegionManager.class");
    	try {
			Statement st = this.dbcon.createStatement();
			st.executeUpdate("UPDATE region SET name = '"+name+"' WHERE name ='"+r.getName()+"'");
	        st.close();
	        st = this.dbcon.createStatement();
	        st.executeUpdate("UPDATE region_flags SET region = '"+name+"' WHERE region ='"+r.getName()+"'");
	        st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}    	
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

	@Override
	public void setWelcome(Region r, String msg) {	
		RedProtect.logger.debug("Call setWelcome in WorldMySQLRegionManager.class");
		try {
			Statement st = this.dbcon.createStatement();
			st.executeUpdate("UPDATE region SET wel = '"+msg+"' WHERE name ='"+r.getName()+"'");
	        st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		r.setWelcome(msg);
	}

	@Override
	public String getWelcome(Region r) {
		RedProtect.logger.debug("Call getWelcome in WorldMySQLRegionManager.class");
		String wel = "";
		try {
			Statement st = this.dbcon.createStatement();
			ResultSet rs = st.executeQuery("SELECT wel FROM region WHERE name ='"+r.getName()+"'");
			while (rs.next()) {
            	wel = rs.getString("wel");
            }
	        st.close();
	        rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return wel;
	}

	@Override
	public Set<Region> getRegions(int x, int z) {
		Set<Region> regions = new HashSet<Region>();
		try {
            Statement st = this.dbcon.createStatement();
            ResultSet rs = st.executeQuery("SELECT name FROM Region WHERE " + x + "<=maxMbrX AND " + x + ">=minMbrX AND " + z + "<=maxMbrZ AND " + z + ">=minMbrZ");
            while (rs.next()) {
            	regions.add(this.getRegion(rs.getString("name")));
            }
            st.close();
            rs.close();
        }
		catch (SQLException e) {
            e.printStackTrace();
        }
		return regions;
	}

	@Override
	public void setPrior(Region r, int prior) {
		RedProtect.logger.debug("Call setPrior in WorldMySQLRegionManager.class");
		try {
			Statement st = this.dbcon.createStatement();
			st.executeUpdate("UPDATE region SET prior = '"+prior+"' WHERE name ='"+r.getName()+"'");
	        st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}	
		r.setPrior(prior);
	}

	@Override
	public int getPrior(Region r) {		
		RedProtect.logger.debug("Call getPrior in WorldMySQLRegionManager.class");
		int prior = 0;
		try {
            Statement st = this.dbcon.createStatement();
            ResultSet rs = st.executeQuery("SELECT prior FROM Region WHERE name ='"+r.getName()+"'");
            while (rs.next()) {
            	prior = rs.getInt("prior");
            }
            st.close();
            rs.close();
        }
		catch (SQLException e) {
            e.printStackTrace();
        }
		return prior;
	}

	@Override
	public Region getTopRegion(int x, int z) {
		Map<Integer,Region> regions = new HashMap<Integer,Region>();
		int max = 0;
		try {
            Statement st = this.dbcon.createStatement();
            ResultSet rs = st.executeQuery("SELECT name,prior FROM region WHERE " + x + "<=maxMbrX AND " + x + ">=minMbrX AND " + z + "<=maxMbrZ AND " + z + ">=minMbrZ");
            while (rs.next()) {
            	regions.put(rs.getInt("prior"),this.getRegion(rs.getString("name")));
            }
            st.close();
            rs.close();
        }
		catch (SQLException e) {
            e.printStackTrace();
        }
		if (regions.size() > 0){
        	max = Collections.max(regions.keySet());
        }
        return regions.get(max);
	}
	
	@Override
	public Region getLowRegion(int x, int z) {
		Map<Integer,Region> regions = new HashMap<Integer,Region>();
		int min = 0;
		try {
            Statement st = this.dbcon.createStatement();
            ResultSet rs = st.executeQuery("SELECT name,prior FROM region WHERE " + x + "<=maxMbrX AND " + x + ">=minMbrX AND " + z + "<=maxMbrZ AND " + z + ">=minMbrZ");
            while (rs.next()) {
            	regions.put(rs.getInt("prior"),this.getRegion(rs.getString("name")));
            }
            st.close();
            rs.close();
        }
		catch (SQLException e) {
            e.printStackTrace();
        }
		if (regions.size() > 0){
			min = Collections.min(regions.keySet());
        }
        return regions.get(min);
	}
	
	@Override
	public Map<Integer,Region> getGroupRegion(int x, int z) {
		Map<Integer,Region> regions = new HashMap<Integer,Region>();
		try {
            Statement st = this.dbcon.createStatement();
            ResultSet rs = st.executeQuery("SELECT name FROM region WHERE " + x + "<=maxMbrX AND " + x + ">=minMbrX AND " + z + "<=maxMbrZ AND " + z + ">=minMbrZ");
            while (rs.next()) {
            	regions.put(this.getRegion(rs.getString("name")).getPrior(),this.getRegion(rs.getString("name")));
            }
            st.close();
            rs.close();
        }
		catch (SQLException e) {
            e.printStackTrace();
        }
        return regions;
	}
	
	@Override
	public void setWorld(Region r, String w) {
		RedProtect.logger.debug("Call setWorld in WorldMySQLRegionManager.class");
		try {
			Statement st = this.dbcon.createStatement();
			st.executeUpdate("UPDATE region SET world = '"+w+"' WHERE name ='"+r.getName()+"'");
	        st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getWorld(Region r) {
		RedProtect.logger.debug("Call getWorld in WorldMySQLRegionManager.class");
		String world = "";
		try {
            Statement st = this.dbcon.createStatement();
            ResultSet rs = st.executeQuery("SELECT world FROM region WHERE name ='"+r.getName()+"'");
            while (rs.next()) {
            	world = rs.getString("world");
            }
            st.close();
            rs.close();
        }
		catch (SQLException e) {
            e.printStackTrace();
        }
		return world;
	}

	@Override
	public void setDate(Region r, String date) {
		RedProtect.logger.debug("Call setDate in WorldMySQLRegionManager.class");
		try {
			Statement st = this.dbcon.createStatement();
			st.executeQuery("UPDATE region SET date = '"+date+"' WHERE name ='"+r.getName()+"'");
	        st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		r.setDate(date);
	}
	
	@Override
	public String getDate(Region r) {
		RedProtect.logger.debug("Call getDate in WorldMySQLRegionManager.class");
		String date = "";
		try {
            Statement st = this.dbcon.createStatement();
            ResultSet rs = st.executeQuery("SELECT date FROM region WHERE name ='"+r.getName()+"'");
            while (rs.next()) {
            	date = rs.getString("date");
            }
            st.close();
            rs.close();
        }
		catch (SQLException e) {
            e.printStackTrace();
        }
		return date;
	}

	@Override
	public Set<Region> getAllRegions() {
		Set<Region> regions = new HashSet<Region>();
		try {
            Statement st = this.dbcon.createStatement();
            ResultSet rs = st.executeQuery("SELECT name FROM region");
            while (rs.next()) {
            	regions.add(this.getRegion(rs.getString("name")));
            }
            st.close();
            rs.close();
        }
		catch (SQLException e) {
            e.printStackTrace();
        }
		return regions;
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
        
    }

	@Override
    public Set<Region> getMemberRegions(String uuid) {
        Set<Region> ls = new HashSet<Region>();
        for (Region r:this.getAllRegions()){
        	if (r.isOwner(uuid) || r.isMember(uuid)){
        		ls.add(r);
        	}
        }
        return ls;
    }

	@Override
	public void updateLiveRegion(Region r) {
		
	}

	@Override
	public void closeConn() {
		try {
			this.dbcon.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public int getTotalRegionNum() {
		// TODO Auto-generated method stub
		return 0;
	}
}
