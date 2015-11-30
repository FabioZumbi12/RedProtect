package br.net.fabiozumbi12.RedProtect;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandException;
import org.bukkit.entity.Player;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.google.common.io.Files;

@SuppressWarnings("deprecation")
class RPUtil {
    static int backup = 0; 
    static HashMap<Player, HashMap<Location, Material>> pBorders = new HashMap<Player, HashMap<Location, Material>>();
        
    /**Generate a friendly and unique name for a region based on player name.
     * 
     * @param p Player
     * @param World World
     * @return Name of region
     */
    static String nameGen(String p, String World){
    	String rname = "";
    	World w = RedProtect.serv.getWorld(World);    	
            int i = 0;
            while (true) {
            	int is = String.valueOf(i).length();
                if (p.length() > 13) {
                	rname = p.substring(0, 14-is) + "_" + i;
                }
                else {
                	rname = p + "_" + i;
                }
                if (RedProtect.rm.getRegion(rname, w) == null) {
                    break;
                }
                ++i;
            }           
        return rname;
    }
    
    static boolean isFileEmpty(String s) {
        File f = new File(s);
        if (!f.isFile()) {
            return true;
        }
        try {
            FileInputStream fis = new FileInputStream(s);
            int b = fis.read();
            if (b != -1) {
                fis.close();
                return false;
            }
            fis.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e2) {
            e2.printStackTrace();
        }
        return true;
    }
    
    static String formatName(String name) {
        String s = name.substring(1).toLowerCase();
        String fs = name.substring(0, 1).toUpperCase();
        String ret = String.valueOf(fs) + s;
        ret = ret.replace("_", " ");
        return ret;
    }
    
    static int[] toIntArray(List<Integer> list) {
        int[] ret = new int[list.size()];
        int i = 0;
        for (Integer e : list) {
            ret[i++] = e;
        }
        return ret;
    }
    
    static String DateNow(){
    	DateFormat df = new SimpleDateFormat(RPConfig.getString("region-settings.date-format"));
        Date today = Calendar.getInstance().getTime(); 
        String now = df.format(today);
		return now;    	
    }
    
    static void fixWorld(String regionname){
    	for (World w:RedProtect.serv.getWorlds()){
    		Region r = RedProtect.rm.getRegion(regionname, w);
    		if (r != null){
    			r.setWorld(w.getName());
    		}
    	}
    }
        
    static void ReadAllDB(Set<Region> regions){     	
    	RedProtect.logger.info("Loaded " + regions.size() + " regions (" + RPConfig.getString("file-type") + ")");
    	int i = 0;
    	int pls = 0;
    	int origupdt = 0;
    	int purged = 0;
    	int sell = 0;
    	int dateint = 0;
    	Date now = null;    	   	
    	SimpleDateFormat dateformat = new SimpleDateFormat(RPConfig.getString("region-settings.date-format"));

		try {
			now = dateformat.parse(DateNow());
		} catch (ParseException e1) {
			RedProtect.logger.severe("The 'date-format' don't match with date 'now'!!");
		}
		
        for (Region r:regions){
        	
        	//purge regions
        	if (RPConfig.getBool("purge.enabled")){
        		Date regiondate = null;
            	try {
    				regiondate = dateformat.parse(r.getDate());
    			} catch (ParseException e) {
    				RedProtect.logger.severe("The 'date-format' don't match with region date!!");
    				e.printStackTrace();
    			}
            	Long days = TimeUnit.DAYS.convert(now.getTime() - regiondate.getTime(), TimeUnit.MILLISECONDS);
            	
            	List<String> players = new ArrayList<String>();
            	for (String play:RPConfig.getStringList("purge.ignore-regions-from-players")){
            		players.add(RPUtil.PlayerToUUID(play));
    			}           	
            	
            	if (days > RPConfig.getInt("purge.remove-oldest") && !players.contains(r.getCreator())){        
                	RedProtect.logger.warning("Purging" + r.getName() + " - Days: " + days);
            		r.delete();
            		purged++;
            		continue;
            	}
        	}    
        	
        	//sell rergions
        	if (RPConfig.getBool("sell.enabled")){
        		Date regiondate = null;
            	try {
    				regiondate = dateformat.parse(r.getDate());
    			} catch (ParseException e) {
    				RedProtect.logger.severe("The 'date-format' don't match with region date!!");
    				e.printStackTrace();
    			}
            	Long days = TimeUnit.DAYS.convert(now.getTime() - regiondate.getTime(), TimeUnit.MILLISECONDS);
            	
            	List<String> players = new ArrayList<String>();
            	for (String play:RPConfig.getStringList("sell.ignore-regions-from-players")){
            		players.add(RPUtil.PlayerToUUID(play));
    			}           	
            	
            	if (days > RPConfig.getInt("sell.sell-oldest") && !players.contains(UUIDtoPlayer(r.getCreator()))){        
                	RedProtect.logger.warning("Selling " + r.getName() + " - Days: " + days);
            		RPEconomy.putToSell(r, "server", RPEconomy.getRegionValue(r));
            		sell++;
            	}
        	}
        	
        	//Update player names
        	if (RedProtect.OnlineMode && !r.isForSale()){
        		if (!isUUID(r.getCreator()) && r.getCreator() != null){
        			backup(); 
            		RedProtect.logger.warning("Creator from: " + r.getCreator());
            		RedProtect.logger.warning("To UUID: " + PlayerToUUID(r.getCreator()));
            		r.setCreator(PlayerToUUID(r.getCreator()));      
            		origupdt++;
            	}
            	
            	List<String> ownersl = r.getOwners();
            	List<String> membersl = r.getMembers();        	
            	for (int o = 0; o < ownersl.size(); o++){
            		String pname = ownersl.get(o);
            		if (!isUUID(pname) && pname != null){
            			backup(); 
                		RedProtect.logger.warning("Owner from: " + pname);
            			ownersl.remove(o);
                		ownersl.add(o, PlayerToUUID(pname));
                		RedProtect.logger.warning("To UUID: " + PlayerToUUID(pname));
                		origupdt++;
            		}             		
            	}        	
            	for (int m = 0; m < membersl.size(); m++){
            		String pname = membersl.get(m);     		
            		if (!isUUID(pname) && pname != null){
            			backup(); 
                		RedProtect.logger.warning("Member from: " + pname);   
            			membersl.remove(m);
                		membersl.add(m, PlayerToUUID(pname));
                		RedProtect.logger.warning("To UUID: " + PlayerToUUID(pname));  
                		origupdt++;
            		}              		
            	}
            	r.setOwners(ownersl);
            	r.setMembers(membersl);
            	if (origupdt > 0){
            		pls++;
            	}            	
        	}  
        	
        	//import essentials last visit for player dates
        	if (RPConfig.getBool("hooks.essentials.import-lastvisits") && RedProtect.Ess){
        		List<String> ownersl = r.getOwners();
            	List<String> membersl = r.getMembers();    
            	Essentials ess = (Essentials) Bukkit.getServer().getPluginManager().getPlugin("Essentials");
            	List<Long> dates = new ArrayList<Long>(); 
            	
            	for (int o = 0; o < ownersl.size(); o++){
            		String pname = ownersl.get(o);  
            		User essp = null;
            		if (RedProtect.OnlineMode){
            			essp = ess.getUser(UUID.fromString(pname));
            		} else {
            			essp = ess.getOfflineUser(pname);
            		}
            		if (essp != null){
            			dates.add(essp.getLastLogout());
            			RedProtect.logger.info("Updated user date: "+pname+" - "+dateformat.format(essp.getLastLogout()));
            		}
            		
            	}        	
            	for (int m = 0; m < membersl.size(); m++){
            		String pname = membersl.get(m); 
            		User essp = null;
            		if (RedProtect.OnlineMode){
            			essp = ess.getUser(UUID.fromString(pname));
            		} else {
            			essp = ess.getOfflineUser(pname);
            		}            		
            		if (essp != null){
            			dates.add(essp.getLastLogout());
            			RedProtect.logger.info("Updated user date: "+pname+" - "+dateformat.format(essp.getLastLogout()));
            		}
            	} 
            	
            	if (dates.size() > 0){
            		Date lastvisit = new Date(Collections.max(dates));
            		r.setDate(dateformat.format(lastvisit));
        			RedProtect.logger.info("Updated "+ dates.size() +" last visit users ("+dateformat.format(lastvisit)+")");
        			dates.clear();
            	}
        	}
        	
        	//Update OLD Databases!
        	if (fixFlags(r)) {
        		if (r.getDate() == null) {
                    r.setDate(DateNow());                    
            	}
        		if (r.getWorld() == null) {
                    fixWorld(r.getName());
                }   
                i++;
        	}
        	
        	if (pls > 0){
        		RedProtect.logger.sucess("["+pls+"]Region updated §6§l" + r.getName() + "§a§l. Owner §6§l" + r.getCreator());
            }        	
        }     
        
        if (dateint > 0){
			RedProtect.logger.info("Updated "+ dateint +" last visit users!");
			RedProtect.rm.saveAll();
    	}
                   	        
        if (i > 0 || pls > 0){
        	if (i > pls){
            	RedProtect.logger.sucess("Updated a total of §6§l" + (i-pls) + "§a§l regions!");
        	} else {
            	RedProtect.logger.sucess("Updated a total of §6§l" + (pls-i) + "§a§l regions!");
        	}
        	RedProtect.rm.saveAll();        	
        	RedProtect.logger.sucess("Regions saved!");  
        	pls = 0;
        	i = 0;
        }
        
        if (purged > 0){
        	RedProtect.logger.warning("Purged a total of §6§l" + purged + "§a§l regions!");
        	purged = 0;
        }
        
        if (sell > 0){
        	RedProtect.logger.warning("Put to sell a total of §6§l" + sell + "§a§l regions!");
        	sell = 0;
        }
        regions.clear();   
	}
    
    static boolean fixFlags(Region r){
      if (r.f != null && r.f.length < 10){
        backup();
        boolean[] flags = { r.f[0], r.f[1], r.f[2], r.f[3], r.f[4], r.f[5], r.f[6], RPConfig.getBool("flags.flow").booleanValue(), RPConfig.getBool("flags.fire").booleanValue(), RPConfig.getBool("flags.spawnpassives").booleanValue() };
        r.f = flags;
        return true;
      }
      return false;
    }
    
    static void backup(){
    	if (backup == 0){
    		RedProtect.logger.warning("Making backup of your database before update the database...");
    		File source = new File(RedProtect.pathData);
    		File dest = new File(RedProtect.pathMain + "backupUpdate" + RedProtect.pdf.getVersion());
    		if (source.exists()){
    			try {
       		        Files.copy(source, dest);
        		} catch (IOException e) {
        			RedProtect.logger.severe("Error on create a backup of your database: ");
        		    e.printStackTrace();
        		    RedProtect.logger.severe("Operation canceled!");
        		}
        		RedProtect.logger.sucess("Backup created!");
    			backup++;
    		}    		
		}
    }
    
	static String PlayerToUUID(String PlayerName){
    	if (PlayerName == null || PlayerName.equals("")){
    		return null;
    	}
    	String uuid = PlayerName;

    	if (!RedProtect.OnlineMode){
    		uuid = uuid.toLowerCase();
    		return uuid;
    	}
    	
    	try{
    		OfflinePlayer offp = RedProtect.serv.getOfflinePlayer(PlayerName);
    		uuid = offp.getUniqueId().toString();
		} catch (IllegalArgumentException e){	
	    	Player onp = RedProtect.serv.getPlayer(PlayerName);
	    	if (onp != null){
	    		uuid = onp.getUniqueId().toString();
	    	}
		}
    	
		return uuid;    	
    }
    
	static String UUIDtoPlayer(String uuid){
    	if (uuid == null){
    		return null;
    	}
    	String PlayerName = null;
    	UUID uuids = null;
    	
    	if (!RedProtect.OnlineMode){
	    	PlayerName = uuid.toLowerCase();	    	
    		return PlayerName;
    	}
    	try{
    		uuids = UUID.fromString(uuid);
    		OfflinePlayer offp = RedProtect.serv.getOfflinePlayer(uuids);
    		PlayerName = offp.getName();
		} catch (IllegalArgumentException e){	
			Player onp = RedProtect.serv.getPlayer(uuid);
	    	if (onp != null){
	    		PlayerName = onp.getName();
	    	}
		}
    	
		return PlayerName;    	
    }
    
	private static boolean isUUID(String uuid){
    	if (uuid == null){
    		return false;
    	}
    	try{
    		UUID.fromString(uuid);
    		return true;
    	} catch (IllegalArgumentException e){
    	}
		return false;
    }
    
    static void addRegion(List<Region> regions, World w){    	
    	for (int i = 0; i < regions.size(); i++){
    		if (!RedProtect.rm.getRegionsByWorld(w).contains(regions.get(i))){
    			RedProtect.logger.warning("["+(i+1)+"/"+regions.size()+"]Adding regions to database! This may take some time...");
        		RedProtect.rm.add(regions.get(i), w);       		                		
    		}
		}	 
    	regions.clear();
    }
    
    static Object parseObject(String value){
    	Object obj = value;
    	try {
    		obj = Integer.parseInt(value);
    	} catch(NumberFormatException e){
    		if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")){
    			obj = Boolean.parseBoolean(value);
        	}
    	}
    	return obj;
    }
    
    static RPYaml fixdbFlags(RPYaml db, String rname){
		if (db.contains(rname+".flags.mobs")){
			db.set("spawn-monsters", db.get(rname+".flags.mobs"));
			db.set(rname+".flags.mobs", null);
		}
		if (db.contains(rname+".flags.spawnpassives")){
			db.set("spawn-animals", db.get(rname+".flags.spawnpassives"));
			db.set(rname+".flags.spawnpassives", null);
		}
		return db;
	}
    
	static boolean ymlToMysql() throws Exception{
		if (!RPConfig.getString("file-type").equalsIgnoreCase("yml")){
			return false;
		}
		
		initMysql();//Create tables
		int counter = 1;
		
		for (World world:Bukkit.getWorlds()){
			
			String dbname = RPConfig.getString("mysql.db-name") + "_" + world.getName();
		    String url = "jdbc:mysql://"+RPConfig.getString("mysql.host")+"/";
		    
			Connection dbcon = DriverManager.getConnection(url + dbname, RPConfig.getString("mysql.user-name"), RPConfig.getString("mysql.user-pass"));
			
			for (Region r:RedProtect.rm.getRegionsByWorld(world)){
				if (!regionExists(r.getName(),dbname)) {
		            try {                
		                Statement st = null;
		                for (String flag:r.flags.keySet()){
		                	st = dbcon.createStatement();       
		                	st.executeUpdate("INSERT INTO region_flags (region,flag,value) VALUES ('" + r.getName() + "', '" + flag + "', '" + r.flags.get(flag).toString()+"')");
		                	st.close();
		                }          
		                st = dbcon.createStatement();
		                RedProtect.logger.debug("Region info - Region: "+ r.getName() +" | Creator:" + r.getCreator() + "(Size: "+r.getCreator().length()+")");
		                st.executeUpdate("INSERT INTO region (name,creator,owners,members,maxMbrX,minMbrX,maxMbrZ,minMbrZ,centerX,centerZ,date,wel,prior,value,world) VALUES "
		                		+ "('" +r.getName() + "', '" + 
		                		r.getCreator().toString() + "', '" + 
		                		r.getOwners().toString().replace("[", "").replace("]", "")  + "', '" + 
		                		r.getMembers().toString().replace("[", "").replace("]", "") + "', '" + 
		                		r.getMaxMbrX() + "', '" + 
		                		r.getMinMbrX() + "', '" + 
		                		r.getMaxMbrZ() + "', '" + 
		                		r.getMinMbrZ() + "', '" + 
		                		r.getCenterX() + "', '" + 
		                		r.getCenterZ() + "', '" + 
		                		r.getDate().toString() + "', '" +
		                		r.getWelcome().toString() + "', '" + 
		                		r.getPrior() + "', '" + 
		                		r.getValue() + "', '" + 
		                		r.getWorld().toString()+"')");                    
		                st.close();
		                RedProtect.logger.sucess("["+counter+"]Converted region to Mysql: " + r.getName());
		                counter++;
		            }
		            catch (SQLException e) {
		                e.printStackTrace();
		            }
		        } else {
		        	//if exists jump
		        	continue;
		        }
			}
			dbcon.close();
		}		
		if (counter > 0){
			RedProtect.logger.sucess((counter-1) + " regions converted to Mysql with sucess!");
		}
		return true;		
	}
	
	private static void initMysql() throws Exception{
		for (World world:Bukkit.getWorlds()){
			
		    String dbname = RPConfig.getString("mysql.db-name") + "_" + world.getName().toLowerCase();
		    String url = "jdbc:mysql://"+RPConfig.getString("mysql.host")+"/";
		    String reconnect = "?autoReconnect=true";
		    
	        try {
	            Class.forName("com.mysql.jdbc.Driver");
	        }
	        catch (ClassNotFoundException e2) {
	            RedProtect.logger.severe("Couldn't find the driver for MySQL! com.mysql.jdbc.Driver.");
	            return;
	        }
	        Statement st = null;
	        
	        try {
	            if (!checkDBExists(dbname)) {
	                Connection con = DriverManager.getConnection(url, RPConfig.getString("mysql.user-name"), RPConfig.getString("mysql.user-pass"));
	                st = con.createStatement();
	                st.executeUpdate("CREATE DATABASE " + dbname);
	                RedProtect.logger.info("Created database '" + dbname + "'!");
	                st.close();
	                st = null;
	                con = DriverManager.getConnection(url + dbname + reconnect, RPConfig.getString("mysql.user-name"), RPConfig.getString("mysql.user-pass"));
	                st = con.createStatement();
	                st.executeUpdate("CREATE TABLE region(name varchar(20) PRIMARY KEY NOT NULL, creator varchar(36), owners varchar(255), members varchar(255), maxMbrX int, minMbrX int, maxMbrZ int, minMbrZ int, centerX int, centerZ int, minY int, maxY int, date varchar(10), wel varchar(64), prior int, world varchar(16), value Long not null default '0.0')");
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
	        }
	        catch (CommandException e3) {
	            RedProtect.logger.severe("Couldn't connect to mysql! Make sure you have mysql turned on and installed properly, and the service is started.");
	            throw new Exception("Couldn't connect to mysql!");
	        }
	        catch (SQLException e) {
	            e.printStackTrace();
	            RedProtect.logger.severe("There was an error while parsing SQL, redProtect will still with actual DB setting until you change the connection options or check if a Mysql service is running. Use /rp reload to try again");
	        }
	        finally {
	            if (st != null) {
	                st.close();
	            }
	        }
		}
	    
	}
	
	private static boolean regionExists(String name, String dbname) {
        int total = 0;
        String reconnect = "?autoReconnect=true";
        try {
        	Connection dbcon = DriverManager.getConnection("jdbc:mysql://"+RPConfig.getString("mysql.host")+"/"+dbname+reconnect,RPConfig.getString("mysql.user-name"), RPConfig.getString("mysql.user-pass"));
            Statement st = dbcon.createStatement();
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM region WHERE name = '" + name + "'");
            if (rs.next()) {
                total = rs.getInt("COUNT(*)");
            }
            st.close();
            rs.close();
            dbcon.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return total > 0;
    }		
	
	private static boolean checkDBExists(String dbname) throws SQLException {
        try {
        	Connection con = DriverManager.getConnection("jdbc:mysql://"+RPConfig.getString("mysql.host")+"/",RPConfig.getString("mysql.user-name"), RPConfig.getString("mysql.user-pass"));
            DatabaseMetaData meta = con.getMetaData();
            ResultSet rs = meta.getCatalogs();
            while (rs.next()) {
                String listOfDatabases = rs.getString("TABLE_CAT");
                if (listOfDatabases.equalsIgnoreCase(dbname)) {
                    return true;
                }
            }
            rs.close();
            con.close();
        } catch (SQLException e){
        	e.printStackTrace();
        }        
        return false;
    }
	
	public static void startFlagChanger(final String r, final String flag, final Player p){
		RedProtect.changeWait.add(r+flag);
		Bukkit.getScheduler().scheduleSyncDelayedTask(RedProtect.plugin, new Runnable() { 
			public void run() {
				if (RedProtect.changeWait.contains(r+flag)){
					/*if (p != null && p.isOnline()){
						RPLang.sendMessage(p, RPLang.get("gui.needwait.ready").replace("{flag}", flag));
					}*/
					RedProtect.changeWait.remove(r+flag);				
				} 
			}
			}, RPConfig.getInt("flags-configuration.change-flag-delay.seconds")*20);
	}
	
	public static int getUpdatedPrior(Region region) {
		int regionarea = region.getArea();  
		int prior = region.getPrior();
        Region topRegion = RedProtect.rm.getTopRegion(RedProtect.serv.getWorld(region.getWorld()), region.getCenterX(), region.getCenterY(), region.getCenterZ());
        Region lowRegion = RedProtect.rm.getLowRegion(RedProtect.serv.getWorld(region.getWorld()), region.getCenterX(), region.getCenterY(), region.getCenterZ());
        
        if (lowRegion != null){
        	if (regionarea > lowRegion.getArea()){
        		prior = lowRegion.getPrior() - 1;
        	} else if (regionarea < lowRegion.getArea() && regionarea < topRegion.getArea() ){
        		prior = topRegion.getPrior() + 1;
        	} else if (regionarea < topRegion.getArea()){
        		prior = topRegion.getPrior() + 1;
        	} 
        }
		return prior;
	}
	
	
	/** Show the border of region for defined seconds.
	 * @param p
	 * @param loc1
	 * @param loc2
	 */
	public static void addBorder(final Player p, Region r) {		
		if (pBorders.containsKey(p)){
			RPLang.sendMessage(p, "cmdmanager.showingborder");
			return;
		}
		
		final World w = p.getWorld();
		final HashMap<Location, Material> borderBlocks = new HashMap<Location, Material>();				
		
		for (Location loc:r.get4Points(p.getLocation().getBlockY())){
			loc.setY(p.getLocation().getBlockY());
			Block b = w.getBlockAt(loc);
        	if (b.isEmpty() || b.isLiquid()){
        		borderBlocks.put(b.getLocation(), b.getType());
        		w.getBlockAt(loc).setType(RPConfig.getMaterial("region-settings.border.material"));
        	} 
		}		
		if (borderBlocks.isEmpty()){
			RPLang.sendMessage(p, "cmdmanager.bordernospace");
		} else {
			RPLang.sendMessage(p, "cmdmanager.addingborder");
			pBorders.put(p, borderBlocks);
			Bukkit.getScheduler().scheduleSyncDelayedTask(RedProtect.plugin, new Runnable(){
				@Override
				public void run() {
					if (pBorders.containsKey(p)){
	            		for (Location loc:pBorders.get(p).keySet()){
	            			w.getBlockAt(loc).setType(pBorders.get(p).get(loc));            			
	            		}
	            		pBorders.remove(p);
	            		RPLang.sendMessage(p, "cmdmanager.removingborder");
					}
				}    		
	    	}, RPConfig.getInt("region-settings.border.time-showing")*20); 
		}		             
    }		
	
	public static int convertFromGP(){		
		int claimed = 0;
		Collection<Claim> claims = GriefPrevention.instance.dataStore.getClaims();
		for (Claim claim:claims){
			if (Bukkit.getWorlds().contains(claim.getGreaterBoundaryCorner().getWorld())){
				World w = claim.getGreaterBoundaryCorner().getWorld();
				String pname = claim.getOwnerName().replace(" ", "_").toLowerCase();
				if (RedProtect.OnlineMode){
					pname = claim.ownerID.toString();
				}
				List<String> owners = new ArrayList<String>();
				owners.add(pname);
				Location newmin = claim.getGreaterBoundaryCorner();
				Location newmax = claim.getLesserBoundaryCorner();
				newmin.setY(0);
				newmax.setY(w.getMaxHeight());
				
				Region r = new Region(nameGen(claim.getOwnerName().replace(" ", "_"), w.getName()), owners, new ArrayList<String>(), pname, 
						newmin, newmax, RPConfig.getDefFlagsValues(), "GriefPrevention region", 0, w.getName(), DateNow(), 0);				
				
				Region other = RedProtect.rm.getTopRegion(w, r.getCenterX(), r.getCenterY(), r.getCenterZ());
				if (other != null && r.getWelcome().equals(other.getWelcome())){
					continue;
				} else {
					RedProtect.rm.add(r, w);
					RedProtect.logger.debug("Region: " + r.getName());
					claimed++;
				}				
			}
		}		
		return claimed;		
	}
}
