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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandException;
import org.bukkit.entity.Player;

import br.net.fabiozumbi12.RedProtect.config.RPConfig;

@SuppressWarnings("deprecation")
class WorldMySQLRegionManager implements WorldRegionManager{

	private String url = "jdbc:mysql://"+RPConfig.getString("mysql.host")+"/";
	private String reconnect = "?autoReconnect=true";
	private String dbname;
	private boolean dbexists = false;
    private Connection dbcon;

    private HashMap<String, Region> regions;
    private World world;
    
    public WorldMySQLRegionManager(World world){
        super();
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
        this.dbname = RPConfig.getString("mysql.db-name") + "_" + world.getName();
        Statement st = null;
        try {
            if (!this.checkDBExists()) {
                Connection con = DriverManager.getConnection(this.url, RPConfig.getString("mysql.user-name"), RPConfig.getString("mysql.user-pass"));
                st = con.createStatement();                
                st.executeUpdate("CREATE DATABASE " + this.dbname);
                RedProtect.logger.info("Created database '" + this.dbname + "'!");
                st.close();
                st = null;
                con = DriverManager.getConnection(this.url + this.dbname + this.reconnect, RPConfig.getString("mysql.user-name"), RPConfig.getString("mysql.user-pass"));
                st = con.createStatement();
                st.executeUpdate("CREATE TABLE region(name varchar(20) PRIMARY KEY NOT NULL, leaders varchar(36), admins varchar(255), members varchar(255), maxMbrX int, minMbrX int, maxMbrZ int, minMbrZ int, centerX int, centerZ int, minY int, maxY int, date varchar(10), wel varchar(64), prior int, world varchar(16), value Long not null, tppoint varchar(16))");
                st.close();
                st = null;
                RedProtect.logger.info("Created table: 'Region'!");    
                st = con.createStatement();
                st.executeUpdate("CREATE TABLE region_flags(region varchar(20) NOT NULL, flag varchar(20) NOT NULL, value varchar(255) NOT NULL)");
                st.close();
                st = null;
                RedProtect.logger.info("Created table: 'Region Flags'!"); 
                con.close();
            }
            ConnectDB();
            addNewColumns();
        }
        catch (CommandException e3) {
            RedProtect.logger.severe("Couldn't connect to mysql! Make sure you have mysql turned on and installed properly, and the service is started. Reload the Redprotect plugin after you fix or change your DB configurations");
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            if (st != null) {
                try {
					st.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
            }
        }
    }
    
	private boolean checkDBExists() {
        if (this.dbexists) {            
            return true;
        }     
        try {   
        	RedProtect.logger.debug("Checking if database exists... " + this.url + this.dbname);
        	Connection con = DriverManager.getConnection(this.url, RPConfig.getString("mysql.user-name"), RPConfig.getString("mysql.user-pass"));
            DatabaseMetaData meta = con.getMetaData();
            ResultSet rs = meta.getCatalogs();
            while (rs.next()) {
                String listOfDatabases = rs.getString("TABLE_CAT");
                if (listOfDatabases.equalsIgnoreCase(this.dbname)) {
                	con.close();
                	rs.close();
                    return true;
                }                
            }    
            con.close();
        	rs.close();
        } catch (SQLException e){
        	e.printStackTrace();
        }        
        return false;
    }
    
	private void addNewColumns(){
		try{
			DatabaseMetaData meta = this.dbcon.getMetaData();			
			ResultSet rs = meta.getColumns(null, null, "region", "value");
	    	if (!rs.next()){
	    		Statement st = this.dbcon.createStatement();        			
			    st.executeUpdate("ALTER TABLE region ADD value Long not null");
			    st.close();
			    RedProtect.logger.info("Created column 'value'!");
	    	}
	    	rs.close();	    	
	    	rs = meta.getColumns(null, null, "region", "maxY");
	    	if (!rs.next()){
	    		Statement st = this.dbcon.createStatement();        			
			    st.executeUpdate("ALTER TABLE region ADD maxY int default '255'");
			    st.close();
			    RedProtect.logger.info("Created column 'maxY'!");
	    	}
	    	rs.close();	    	
	    	rs = meta.getColumns(null, null, "region", "minY");
	    	if (!rs.next()){
	    		Statement st = this.dbcon.createStatement();        			
			    st.executeUpdate("ALTER TABLE region ADD minY int default '0'");
			    st.close();
			    RedProtect.logger.info("Created column 'minY'!");
	    	}
	    	rs.close();
	    	rs = meta.getColumns(null, null, "region", "tppoint");
	    	if (!rs.next()){
	    		Statement st = this.dbcon.createStatement();        			
			    st.executeUpdate("ALTER TABLE region ADD tppoint varchar(20)");
			    st.close();
			    RedProtect.logger.info("Created column 'tppoint'!");
	    	}
	    	rs.close();	    	
	    	rs = meta.getColumns(null, null, "region", "leaders");
	    	if (!rs.next()){
	    		Statement st = this.dbcon.createStatement();        			
			    st.executeUpdate("ALTER TABLE region ADD leaders varchar(255)");
			    st.close();
			    RedProtect.logger.info("Created column 'leaders'!");
	    	}
	    	rs.close();
		} catch(Exception ex){
			RedProtect.logger.severe("Cold not add the colluns to table region.");        		        
		}    		 
	}
	
	/*-------------------------------------- Live Actions -------------------------------------------*/
	
    @Override
    public void remove(Region r) {
    	removeLiveRegion(r);
        if (this.regions.containsKey(r.getName())){
        	this.regions.remove(r.getName());
        }
    }   
    private void removeLiveRegion(Region r) {
        if (this.regionExists(r.getName())) {
            try {
                Statement st = this.dbcon.createStatement();
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
    
    @Override
    public void add(Region r) {
		addLiveRegion(r);       
    }
	
    private void addLiveRegion(Region r){
    	if (!this.regionExists(r.getName())) {
            try {                
                Statement st = this.dbcon.createStatement();
                for (String flag:r.flags.keySet()){
                	st = this.dbcon.createStatement();       
                	st.executeUpdate("INSERT INTO region_flags (region,flag,value) VALUES ('" + r.getName() + "', '" + flag + "', '" + r.flags.get(flag).toString()+"')");
                	st.close();
                }          
                st = this.dbcon.createStatement();
                st.executeUpdate("INSERT INTO region (name,leaders,admins,members,maxMbrX,minMbrX,maxMbrZ,minMbrZ,minY,maxY,centerX,centerZ,date,wel,prior,world,value) VALUES "
                		+ "('" +r.getName() + "', '" + 
                		r.getLeaders().toString().replace("[", "").replace("]", "") + "', '" + 
                		r.getAdmins().toString().replace("[", "").replace("]", "") + "', '" + 
                		r.getMembers().toString().replace("[", "").replace("]", "") + "', '" + 
                		r.getMaxMbrX() + "', '" + 
                		r.getMinMbrX() + "', '" + 
                		r.getMaxMbrZ() + "', '" + 
                		r.getMinMbrZ() + "', '" + 
                		r.getMinY() + "', '" +
                		r.getMaxY() + "', '" +
                		r.getCenterX() + "', '" + 
                		r.getCenterZ() + "', '" + 
                		r.getDate() + "', '" +
                		r.getWelcome() + "', '" + 
                		r.getPrior() + "', '" + 
                		r.getWorld() + "', '" + 
                		r.getValue()+"')");                    
                st.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
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
    public void load() {
    	if (this.dbcon == null){
    		ConnectDB();
    	}
    	try {
            Statement st = this.dbcon.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM region WHERE world='"+this.world.getName()+"'");            
            if (rs.next()){ 
            	List<String> leaders = new ArrayList<String>();
            	List<String> admins = new ArrayList<String>();
                List<String> members = new ArrayList<String>();
                HashMap<String, Object> flags = new HashMap<String, Object>();                      
                
                int maxMbrX = rs.getInt("maxMbrX");
                int minMbrX = rs.getInt("minMbrX");
                int maxMbrZ = rs.getInt("maxMbrZ");
                int minMbrZ = rs.getInt("minMbrZ");
                int maxY = rs.getInt("maxY");
                int minY = rs.getInt("minY");
                int prior = rs.getInt("prior");
                String rname = rs.getString("name");
                String world = rs.getString("world");
                String date = rs.getString("date");
                String wel = rs.getString("wel");
                long value = rs.getLong("value");
                
                Location tppoint = null;
                if (rs.getString("tppoint") != null && !rs.getString("tppoint").equalsIgnoreCase("")){
                	String tpstring[] = rs.getString("tppoint").split(",");
                    tppoint = new Location(Bukkit.getWorld(world), Double.parseDouble(tpstring[0]), Double.parseDouble(tpstring[1]), Double.parseDouble(tpstring[2]), 
                    		Float.parseFloat(tpstring[3]), Float.parseFloat(tpstring[4]));
                }                    
                                    
                for (String member:rs.getString("members").split(", ")){
                	if (member.length() > 0){
                		members.add(member);
                	}                	
                }
                for (String admin:rs.getString("admins").split(", ")){
                	if (admin.length() > 0){
                		admins.add(admin);
                	}                	
                }
                for (String leader:rs.getString("leaders").split(", ")){
                	if (leader.length() > 0){
                		leaders.add(leader);
                	}                	
                }
                
                //------------> Compatibility
                DatabaseMetaData meta = this.dbcon.getMetaData();			
                ResultSet rsc = meta.getColumns(null, null, "region", "owners");
    	    	if (rsc.next()){
    	    		for (String admin:rs.getString("owners").split(", ")){
    	    			admins.add(admin);               	
                    }
    	    		st.executeUpdate("ALTER TABLE region DROP COLUMN owners");
    	    	}
    	    	rsc.close();
    			rsc = meta.getColumns(null, null, "region", "creator");
    	    	if (rsc.next()){
    	    		String creator = rs.getString("creator");
    	    		leaders.add(creator);
    	    		if (admins.contains(creator)){
    	    			admins.remove(creator);
    	    		}
    	    		st.executeUpdate("ALTER TABLE region DROP COLUMN creator");
    	    	}
    	    	rsc.close();
    	    	//<----------- Compatibility
    	    	
                Statement fst = this.dbcon.createStatement();
                ResultSet frs = fst.executeQuery("SELECT value,flag FROM region_flags WHERE region = '" + rname + "'");
                while (frs.next()){
                	flags.put(frs.getString("flag"), RPUtil.parseObject(frs.getString("value")));
                }   
                fst.close();
                frs.close();
                
                regions.put(rname, new Region(rname, admins, members, leaders, maxMbrX, minMbrX, maxMbrZ, minMbrZ, minY, maxY, flags, wel, prior, world, date, value, tppoint));
            } 
            st.close(); 
            rs.close();                           
        }
        catch (SQLException e) {
            e.printStackTrace();
        } 
    }
    
    /*---------------------------------------------------------------------------------*/
    
    @Override
    public Set<Region> getRegions(String uuid) {
    	Set<Region> regionsp = new HashSet<Region>();
    	try {
            Statement st = this.dbcon.createStatement();
            ResultSet rs = st.executeQuery("SELECT name FROM region WHERE leaders LIKE ='%"+uuid+"%'");
            while (rs.next()) {
            	regionsp.add(this.getRegion(rs.getString("name")));
            }
            st.close();
            rs.close();
        }
		catch (SQLException e) {
            e.printStackTrace();
        }    	
		return regionsp;
    }
    
    @Override
    public Set<Region> getMemberRegions(String uuid) {
    	Set<Region> regionsp = new HashSet<Region>();
    	try {
            Statement st = this.dbcon.createStatement();
            ResultSet rs = st.executeQuery("SELECT name FROM region WHERE leaders LIKE '%"+uuid+"%' OR admins LIKE '%"+uuid+"%'");
            while (rs.next()) {
            	regionsp.add(this.getRegion(rs.getString("name")));
            }
            st.close();
            rs.close();
        }
		catch (SQLException e) {
            e.printStackTrace();
        }
		return regionsp;
    }
    
    @Override
    public Region getRegion(final String rname){
    	if (this.dbcon == null){
    		ConnectDB();
    	}
    	if (!regions.containsKey(rname)){
    		if (rname == null){
    			return null;
    		}
    		try {
                Statement st = this.dbcon.createStatement();
                ResultSet rs = st.executeQuery("SELECT * FROM region WHERE name='"+rname+"' AND world='"+this.world.getName()+"'");            
                if (rs.next()){ 
                	List<String> leaders = new ArrayList<String>();
                	List<String> admins = new ArrayList<String>();
                    List<String> members = new ArrayList<String>();
                    HashMap<String, Object> flags = new HashMap<String, Object>();                      
                    
                    int maxMbrX = rs.getInt("maxMbrX");
                    int minMbrX = rs.getInt("minMbrX");
                    int maxMbrZ = rs.getInt("maxMbrZ");
                    int minMbrZ = rs.getInt("minMbrZ");
                    int maxY = rs.getInt("maxY");
                    int minY = rs.getInt("minY");
                    int prior = rs.getInt("prior");
                    String world = rs.getString("world");
                    String date = rs.getString("date");
                    String wel = rs.getString("wel");
                    long value = rs.getLong("value");
                    
                    Location tppoint = null;
                    if (rs.getString("tppoint") != null && !rs.getString("tppoint").equalsIgnoreCase("")){
                    	String tpstring[] = rs.getString("tppoint").split(",");
                        tppoint = new Location(Bukkit.getWorld(world), Double.parseDouble(tpstring[0]), Double.parseDouble(tpstring[1]), Double.parseDouble(tpstring[2]), 
                        		Float.parseFloat(tpstring[3]), Float.parseFloat(tpstring[4]));
                    }                    
                                        
                    for (String member:rs.getString("members").split(", ")){
                    	if (member.length() > 0){
                    		members.add(member);
                    	}                	
                    }
                    for (String admin:rs.getString("admins").split(", ")){
                    	if (admin.length() > 0){
                    		admins.add(admin);
                    	}                	
                    }
                    for (String leader:rs.getString("leaders").split(", ")){
                    	if (leader.length() > 0){
                    		leaders.add(leader);
                    	}                	
                    }
                    
                    //------------> Compatibility
                    DatabaseMetaData meta = this.dbcon.getMetaData();			
                    ResultSet rsc = meta.getColumns(null, null, "region", "owners");
        	    	if (rsc.next()){
        	    		for (String admin:rs.getString("owners").split(", ")){
        	    			admins.add(admin);               	
                        }
        	    		st.executeUpdate("ALTER TABLE region DROP COLUMN owners");
        	    	}
        	    	rsc.close();
        			rsc = meta.getColumns(null, null, "region", "creator");
        	    	if (rsc.next()){
        	    		String creator = rs.getString("creator");
        	    		leaders.add(creator);
        	    		if (admins.contains(creator)){
        	    			admins.remove(creator);
        	    		}
        	    		st.executeUpdate("ALTER TABLE region DROP COLUMN creator");
        	    	}
        	    	rsc.close();
        	    	//<----------- Compatibility
        	    	
                    Statement fst = this.dbcon.createStatement();
                    ResultSet frs = fst.executeQuery("SELECT value,flag FROM region_flags WHERE region = '" + rname + "'");
                    while (frs.next()){
                    	flags.put(frs.getString("flag"), RPUtil.parseObject(frs.getString("value")));
                    }   
                    fst.close();
                    frs.close();
                    
                    regions.put(rname, new Region(rname, admins, members, leaders, maxMbrX, minMbrX, maxMbrZ, minMbrZ, minY, maxY, flags, wel, prior, world, date, value, tppoint));
                } else {
                	return null;
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
    }
    
    @Override
    public int save() {
		return 0;
		}    

	
    
    
            
    @Override
    public int getTotalRegionSize(String uuid) {
		int total = 0;
		for (Region r2 : this.getRegions(uuid)) {
        	total += r2.getArea();
        }
		return total;
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
        
    public World getWorld() {
        return this.world;
    }    
        
	@Override
	public Set<Region> getRegions(int x, int y, int z) {
		Set<Region> regionl = new HashSet<Region>();		
		try {
            Statement st = this.dbcon.createStatement();
            ResultSet rs = st.executeQuery("SELECT name FROM region WHERE " + x + "<=maxMbrX AND " + x + ">=minMbrX AND " + z + "<=maxMbrZ AND " + z + ">=minMbrZ AND " + y + "<=maxY AND " + y + ">=minY");
            while (rs.next()) {
            	regionl.add(this.getRegion(rs.getString("name")));
            }
            st.close();
            rs.close();
        }
		catch (SQLException e) {
            e.printStackTrace();
        }
		return regionl;
	}

	@Override
	public Region getTopRegion(int x, int y, int z) {
		Map<Integer,Region> regionlist = new HashMap<Integer,Region>();
		int max = 0;
		
		for (Region r:this.getRegions(x, y, z)){
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
	public Region getLowRegion(int x, int y, int z) {
		Map<Integer,Region> regionlist = new HashMap<Integer,Region>();
		int min = 0;

		for (Region r:this.getRegions(x, y, z)){
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
	
	public Map<Integer,Region> getGroupRegion(int x, int y, int z) {
		Map<Integer,Region> regionlist = new HashMap<Integer,Region>();
		
		for (Region r:this.getRegions(x, y, z)){
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
	}

	@Override
	public void closeConn() {
		try {
			if (this.dbcon != null && !this.dbcon.isClosed()){
				this.dbcon.close();				
			}
		} catch (SQLException e) {
			RedProtect.logger.severe("No connections to close! Forget this message ;)");
		}
	}
	
    private boolean ConnectDB() {
    	try {
			this.dbcon = DriverManager.getConnection(this.url + this.dbname+ this.reconnect, RPConfig.getString("mysql.user-name"), RPConfig.getString("mysql.user-pass"));
			RedProtect.logger.info("Conected to "+this.dbname+" via Mysql!");
			return true;
		} catch (SQLException e) {			
			e.printStackTrace();
			RedProtect.logger.severe("["+dbname+"] Theres was an error while connecting to Mysql database! RedProtect will try to connect again in 15 seconds. If still not connecting, check the DB configurations and reload.");
			return false;
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
        	RedProtect.logger.severe("Error on get total of regions for "+dbname+"!");
            e.printStackTrace();
        }
		return total;
	}

}
