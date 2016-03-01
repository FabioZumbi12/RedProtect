package br.net.fabiozumbi12.RedProtect;

import java.io.File;
import java.io.FileOutputStream;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandException;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import br.net.fabiozumbi12.RedProtect.Bukkit.RPBukkitBlocks;
import br.net.fabiozumbi12.RedProtect.Bukkit.RPBukkitEntities;
import br.net.fabiozumbi12.RedProtect.Bukkit.TaskChain;
import br.net.fabiozumbi12.RedProtect.config.RPConfig;
import br.net.fabiozumbi12.RedProtect.config.RPLang;
import br.net.fabiozumbi12.RedProtect.config.RPYaml;
import br.net.fabiozumbi12.RedProtect.hooks.WEListener;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;

@SuppressWarnings("deprecation")
public class RPUtil {
    static int backup = 0; 
    public static HashMap<Player, HashMap<Location, Material>> pBorders = new HashMap<Player, HashMap<Location, Material>>();
        
    

	public static void performCommand(final ConsoleCommandSender consoleCommandSender, final String command) {
	    TaskChain.newChain().add(new TaskChain.GenericTask() {
	        public void run() {
	        	RedProtect.serv.dispatchCommand(consoleCommandSender,command);
	        }
	    }).execute();
	}
	
    public static boolean isBukkitBlock(Block b){
    	//check if is bukkit 1.8.8 blocks
    	try{
    		RPBukkitBlocks.valueOf(b.getType().name());     
    		return true;
    	} catch (Exception e){
    		return false;
    	}
    }
    
    public static boolean isBukkitEntity(Entity e){
    	//check if is bukkit 1.8.8 Entity
    	try{
    		RPBukkitEntities.valueOf(e.getType().name());
    		return true;
    	} catch (Exception ex){ 
    		return false;
    	}
    }
    
    static void SaveToZipYML(File file, String ZippedFile, RPYaml yml){
    	try{
    		final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file));
            ZipEntry e = new ZipEntry(ZippedFile);
            out.putNextEntry(e);

            byte[] data = yml.saveToString().getBytes();
            out.write(data, 0, data.length);
            out.closeEntry();
            out.close();
    	} catch (Exception e){
    		e.printStackTrace();
    	}    	
    }
    
    static void SaveToZipSB(File file, String ZippedFile, StringBuilder sb){
    	try{
    		final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file));
            ZipEntry e = new ZipEntry(ZippedFile);
            out.putNextEntry(e);

            byte[] data = sb.toString().getBytes();
            out.write(data, 0, data.length);
            out.closeEntry();
            out.close();
    	} catch (Exception e){
    		e.printStackTrace();
    	}    	
    }
    
    
    static File genFileName(String Path, Boolean isBackup){
    	int count = 1;
		String date = DateNow().replace("/", "-");
    	File logfile = new File(Path+date+"-"+count+".zip");
    	File files[] = new File(Path).listFiles();
		HashMap<Long, File> keyFiles = new HashMap<Long, File>();
    	if (files.length >= RPConfig.getInt("flat-file.max-backups") && isBackup){
    		for (File key:files){
    			keyFiles.put(key.lastModified(), key);
    		}
    		keyFiles.get(Collections.min(keyFiles.keySet())).delete();    		 
    	}
    	
    	while(logfile.exists()){     		
    		count++;
    		logfile = new File(Path+date+"-"+count+".zip");
    	}
    	
    	return logfile;
    }
    
    /**Generate a friendly and unique name for a region based on player name.
     * 
     * @param p Player
     * @param World World
     * @return Name of region
     */
    public static String nameGen(String p, String World){
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
    
    public static String DateNow(){
    	DateFormat df = new SimpleDateFormat(RPConfig.getString("region-settings.date-format"));
        Date today = Calendar.getInstance().getTime(); 
        String now = df.format(today);
		return now;    	
    }
    
    static String HourNow(){
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int min = Calendar.getInstance().get(Calendar.MINUTE);
        int sec = Calendar.getInstance().get(Calendar.SECOND);        
		return "["+hour+":"+min+":"+sec+"]";    	
    }
    
    static void fixWorld(String regionname){
    	for (World w:RedProtect.serv.getWorlds()){
    		Region r = RedProtect.rm.getRegion(regionname, w);
    		if (r != null){
    			r.setWorld(w.getName());
    		}
    	}
    }
            
    static void ReadAllDB(Set<Region> regions) {     	
    	RedProtect.logger.info("Loaded " + regions.size() + " regions (" + RPConfig.getString("file-type") + ")");
    	int i = 0;
    	int pls = 0;
    	int origupdt = 0;
    	int purged = 0;
    	int sell = 0;
    	int dateint = 0;
    	int cfm = 0;
    	int delay = 0;
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
            	
            	for (String play:RPConfig.getStringList("purge.ignore-regions-from-players")){
            		if (r.isLeader(RPUtil.PlayerToUUID(play)) || r.isLeader(RPConfig.getString("region-settings.default-leader"))){
            			continue;
            		}
    			}           	
            	
            	if (days > RPConfig.getInt("purge.remove-oldest")){        
                	RedProtect.logger.warning("Purging" + r.getName() + " - Days: " + days);
                	if (RedProtect.WE && RPConfig.getBool("purge.regen.enable") && r.getArea() <= RPConfig.getInt("purge.regen.max-area-regen")){
                		WEListener.regenRegion(r.getName(), Bukkit.getWorld(r.getWorld()), r.getMaxLocation(), r.getMinLocation(), delay, null);
                		delay=delay+40;
                	}
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
            	
            	for (String play:RPConfig.getStringList("sell.ignore-regions-from-players")){
            		if (r.isLeader(RPUtil.PlayerToUUID(play)) || r.isLeader(RPConfig.getString("region-settings.default-leader"))){
            			continue;
            		}
    			}           	
            	
            	if (days > RPConfig.getInt("sell.sell-oldest")){        
                	RedProtect.logger.warning("Selling " + r.getName() + " - Days: " + days);
            		RPEconomy.putToSell(r, RPConfig.getString("region-settings.default-leader"), RPEconomy.getRegionValue(r));
            		sell++;
            		RedProtect.rm.saveAll();
            		continue;
            	}
        	}
        	
        	//Update player names
        	if (RedProtect.OnlineMode){
        		/*if (!isUUID(r.getCreator()) && r.getCreator() != null){
            		RedProtect.logger.warning("Creator from: " + r.getCreator());
            		RedProtect.logger.warning("To UUID: " + PlayerToUUID(r.getCreator()));
            		r.setCreator(PlayerToUUID(r.getCreator()));      
            		origupdt++;
            	}*/
            	
        		List<String> leadersl = r.getLeaders();
            	List<String> adminsl = r.getAdmins();
            	List<String> membersl = r.getMembers(); 
            	for (int l = 0; l < leadersl.size(); l++){
            		String pname = leadersl.get(l);
            		if (!isUUID(pname) && pname != null){
                		RedProtect.logger.warning("Leader from: " + pname);
                		leadersl.remove(l);
                		leadersl.add(l, PlayerToUUID(pname));
                		RedProtect.logger.warning("To UUID: " + PlayerToUUID(pname));
                		origupdt++;
            		}             		
            	} 
            	for (int o = 0; o < adminsl.size(); o++){
            		String pname = adminsl.get(o);
            		if (!isUUID(pname) && pname != null){
                		RedProtect.logger.warning("Admin from: " + pname);
                		adminsl.remove(o);
                		adminsl.add(o, PlayerToUUID(pname));
                		RedProtect.logger.warning("To UUID: " + PlayerToUUID(pname));
                		origupdt++;
            		}             		
            	}        	
            	for (int m = 0; m < membersl.size(); m++){
            		String pname = membersl.get(m);     		
            		if (!isUUID(pname) && pname != null){
                		RedProtect.logger.warning("Member from: " + pname);   
            			membersl.remove(m);
                		membersl.add(m, PlayerToUUID(pname));
                		RedProtect.logger.warning("To UUID: " + PlayerToUUID(pname));  
                		origupdt++;
            		}              		
            	}
            	r.setLeaders(leadersl);
            	r.setAdmins(adminsl);
            	r.setMembers(membersl);
            	if (origupdt > 0){
            		pls++;
            	}            	
        	}  
        	
        	//import essentials last visit for player dates
        	if (RPConfig.getBool("hooks.essentials.import-lastvisits") && RedProtect.Ess){
        		List<String> adminsl = r.getAdmins();
            	List<String> leadersl = r.getLeaders();    
            	Essentials ess = (Essentials) Bukkit.getServer().getPluginManager().getPlugin("Essentials");
            	List<Long> dates = new ArrayList<Long>(); 
            	
            	for (int o = 0; o < adminsl.size(); o++){
            		String pname = adminsl.get(o);  
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
            	for (int m = 0; m < leadersl.size(); m++){
            		String pname = leadersl.get(m); 
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
        	        	
        	if (pls > 0){
        		RedProtect.logger.sucess("["+pls+"] Region updated &6&l" + r.getName() + "&a&l. Leaders &6&l" + r.getLeadersDesc());
            }      
        	
        	//conform region names        	
        	if (r.getName().contains("/")){
    			String rname = r.getName().replace("/", "|");
    			RedProtect.rm.renameRegion(rname, r);
    			cfm++;
    		}        	
        }     
        
        if (delay > 0){
    		RedProtect.logger.warning("Theres "+delay/40+" regions to be regenerated...");
        }
        
        if (cfm > 0){
    		RedProtect.logger.sucess("["+cfm+"] Region names conformed!");
        }
        
        if (dateint > 0){
			RedProtect.logger.info("Updated "+ dateint +" last visit users!");
			RedProtect.rm.saveAll();
    	}
                   	        
        if (i > 0 || pls > 0){
        	if (i > pls){
            	RedProtect.logger.sucess("Updated a total of &6&l" + (i-pls) + "&a&l regions!");
        	} else {
            	RedProtect.logger.sucess("Updated a total of &6&l" + (pls-i) + "&a&l regions!");
        	}
        	RedProtect.rm.saveAll();        	
        	RedProtect.logger.sucess("Regions saved!");  
        	pls = 0;
        	i = 0;
        }
        
        if (purged > 0){
        	RedProtect.logger.warning("Purged a total of &6&l" + purged + "&a&l regions!");
        	purged = 0;
        }
        
        if (sell > 0){
        	RedProtect.logger.warning("Put to sell a total of &6&l" + sell + "&a&l regions!");
        	sell = 0;
        }
        regions.clear();   
	}
        
	public static String PlayerToUUID(String PlayerName){
    	if (PlayerName == null || PlayerName.equals("")){
    		return null;
    	}
    	
    	//check if is already UUID
    	if (isUUID(PlayerName) || RPConfig.getString("region-settings.default-leader").equalsIgnoreCase(PlayerName) || (PlayerName.startsWith("[") && PlayerName.endsWith("]"))){
    		return PlayerName;
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
    
	public static String UUIDtoPlayer(String uuid){
    	if (uuid == null){
    		return null;
    	}
    	
    	//check if is UUID
    	if (uuid.equalsIgnoreCase(RPConfig.getString("region-settings.default-leader")) || !isUUID(uuid)){
    		return uuid;
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
    	if (uuid.equalsIgnoreCase(RPConfig.getString("region-settings.default-leader"))){
    		return true;
    	}
    	try{
    		UUID.fromString(uuid);
    		return true;
    	} catch (IllegalArgumentException e){
    		return false;
    	}		
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
    
    public static Object parseObject(String value){
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
    
	public static boolean ymlToMysql() throws Exception{
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
		                RedProtect.logger.debug("Region info - Region: "+ r.getName() +" | Leaders:" + r.getLeadersDesc() + "(Size: "+r.getArea()+")");
		                st.executeUpdate("INSERT INTO region (name,leaders,admins,members,maxMbrX,minMbrX,maxMbrZ,minMbrZ,maxy,miny,centerX,centerZ,date,wel,prior,value,world) VALUES "
		                		+ "('" +r.getName() + "', '" + 
		                		r.getLeaders().toString().replace("[", "").replace("]", "")  + "', '" + 
		                		r.getAdmins().toString().replace("[", "").replace("]", "")  + "', '" + 
		                		r.getMembers().toString().replace("[", "").replace("]", "") + "', '" + 
		                		r.getMaxMbrX() + "', '" + 
		                		r.getMinMbrX() + "', '" + 
		                		r.getMaxMbrZ() + "', '" + 
		                		r.getMinMbrZ() + "', '" + 
		                		r.getMaxY() + "', '" + 
		                		r.getMinY() + "', '" + 
		                		r.getCenterX() + "', '" + 
		                		r.getCenterZ() + "', '" + 
		                		r.getDate() + "', '" +
		                		r.getWelcome() + "', '" + 
		                		r.getPrior() + "', '" + 
		                		r.getValue() + "', '" + 
		                		r.getWorld()+"')");                    
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
			
		    String dbname = RPConfig.getString("mysql.db-name") + "_" + world.getName();
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
	        	ConnectDB(url,dbname,reconnect);
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
		
	private static boolean ConnectDB(final String url,final String dbname,final String reconnect) {
    	try {
    		@SuppressWarnings("unused")
			Connection dbcon = DriverManager.getConnection(url + dbname+ reconnect, RPConfig.getString("mysql.user-name"), RPConfig.getString("mysql.user-pass"));
			RedProtect.logger.info("Conected to "+dbname+" via Mysql!");
			return true;
		} catch (SQLException e) {			
			e.printStackTrace();
			RedProtect.logger.severe("["+dbname+"] Theres was an error while connecting to Mysql database! RedProtect will try to connect again in 15 seconds. If still not connecting, check the DB configurations and reload.");
			return false;
		}		
	}
	
	private static boolean regionExists(String name, String dbname) {
        int total = 0;
        String reconnect = "?autoReconnect=true";
        try {
        	Connection dbcon = DriverManager.getConnection("jdbc:mysql://"+RPConfig.getString("mysql.host")+"/"+dbname+reconnect,RPConfig.getString("mysql.user-name"), RPConfig.getString("mysql.user-pass"));
            Statement st = dbcon.createStatement();
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM region WHERE name='"+name+"'");
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
        	RedProtect.logger.debug("Checking if database exists... " + dbname);
        	Connection con = DriverManager.getConnection("jdbc:mysql://"+RPConfig.getString("mysql.host")+"/",RPConfig.getString("mysql.user-name"), RPConfig.getString("mysql.user-pass"));
            DatabaseMetaData meta = con.getMetaData();
            ResultSet rs = meta.getCatalogs();
            while (rs.next()) {
                String listOfDatabases = rs.getString("TABLE_CAT");
                if (listOfDatabases.equalsIgnoreCase(dbname)) {
                	con.close();
                	rs.close();
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
				List<String> leaders = new ArrayList<String>();
				leaders.add(pname);
				Location newmin = claim.getGreaterBoundaryCorner();
				Location newmax = claim.getLesserBoundaryCorner();
				newmin.setY(0);
				newmax.setY(w.getMaxHeight());
				
				Region r = new Region(nameGen(claim.getOwnerName().replace(" ", "_"), w.getName()), new ArrayList<String>(), new ArrayList<String>(), leaders, 
						newmin, newmax, RPConfig.getDefFlagsValues(), "GriefPrevention region", 0, w.getName(), DateNow(), 0, null);				
				
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

	public static String StripName(String pRName) {
        String regionName;
		if (pRName.length() > 13) {
            regionName = pRName.substring(0, 13);
        } else {
        	regionName = pRName;
        } 
		return regionName;
	}

	public static boolean RemoveGuiItem(ItemStack item) {    	
    	if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()){
    		try{
    			String lore = item.getItemMeta().getLore().get(1);
    			if (RPConfig.getDefFlags().contains(lore.replace("§0", "")) || lore.equals(RPConfig.getGuiString("separator"))){
    				return true;
    			}
    		} catch (IndexOutOfBoundsException ex){    			
    		}    		
    	}
    	return false;
	}

	private static void saveYaml(RPYaml fileDB, File file){
    	try {         			   
    		fileDB.save(file);         			
		} catch (IOException e) {
			RedProtect.logger.severe("Error during save database file");
			e.printStackTrace();
		}
    }
	
	public static Region loadProps(RPYaml fileDB, String rname, World world){
		if (fileDB.getString(rname+".name") == null){
			return null;
		}
		int maxX = fileDB.getInt(rname+".maxX");
		int maxZ = fileDB.getInt(rname+".maxZ");
		int minX = fileDB.getInt(rname+".minX");
		int minZ = fileDB.getInt(rname+".minZ");
    	int maxY = fileDB.getInt(rname+".maxY", world.getMaxHeight());
    	int minY = fileDB.getInt(rname+".minY", 0);
    	String name = fileDB.getString(rname+".name");
    	List<String> leaders = fileDB.getStringList(rname+".leaders");
    	List<String> admins = fileDB.getStringList(rname+".admins");
    	List<String> members = fileDB.getStringList(rname+".members");
    	//String creator = fileDB.getString(rname+".creator");	    	  
    	String welcome = fileDB.getString(rname+".welcome");
    	int prior = fileDB.getInt(rname+".priority");
    	String date = fileDB.getString(rname+".lastvisit");
    	long value = fileDB.getLong(rname+".value");
    	
    	Location tppoint = null;
        if (!fileDB.getString(rname+".tppoint", "").equalsIgnoreCase("")){
        	String tpstring[] = fileDB.getString(rname+".tppoint").split(",");
            tppoint = new Location(world, Double.parseDouble(tpstring[0]), Double.parseDouble(tpstring[1]), Double.parseDouble(tpstring[2]), 
            		Float.parseFloat(tpstring[3]), Float.parseFloat(tpstring[4]));
        }
        //compatibility ------>                
        if (fileDB.contains(rname+".creator")){
        	String creator = fileDB.getString(rname+".creator");
        	if (!leaders.contains(creator)){
        		leaders.add(creator);
        	}                	
        }                
        if (fileDB.contains(rname+".owners")){
        	admins.addAll(fileDB.getStringList(rname+".owners"));
        	if (admins.contains(fileDB.getString(rname+".creator"))){
        		admins.remove(fileDB.getString(rname+".creator"));
        	}                	
        }
        //compatibility <------
    	fileDB = RPUtil.fixdbFlags(fileDB, rname);    	
  	    Region newr = new Region(name, admins, members, leaders, new int[] {minX,minX,maxX,maxX}, new int[] {minZ,minZ,maxZ,maxZ}, minY, maxY, prior, world.getName(), date, RPConfig.getDefFlagsValues(), welcome, value, tppoint);
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
    	
    	return newr;
	}
	
	public static RPYaml addProps(RPYaml fileDB, Region r){
		String rname = r.getName().replace(".", "-");					
		fileDB.createSection(rname);
		fileDB.set(rname+".name",rname);
		fileDB.set(rname+".lastvisit",r.getDate());
		fileDB.set(rname+".admins",r.getAdmins());
		fileDB.set(rname+".members",r.getMembers());
		fileDB.set(rname+".leaders",r.getLeaders());
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
		return fileDB;
	}
	
	public static int SingleToFiles() {
		int saved = 0;
		for (World w:Bukkit.getWorlds()){
			Set<Region> regions = RedProtect.rm.getRegionsByWorld(w);			
			for (Region r:regions){
				RPYaml fileDB = new RPYaml();
				
				File f = new File(RedProtect.pathData + w.getName());
        		if (!f.exists()){
        			f.mkdir();
        		}
				File wf = new File(RedProtect.pathData, w.getName()+File.separator+r.getName()+".yml");  
												
    			saved++;
    			saveYaml(addProps(fileDB, r), wf);    			
			} 
			
			File oldf = new File(RedProtect.pathData + "data_" + w.getName() + ".yml");
			if (oldf.exists()){
				oldf.delete();
			}
		}		
		
		if (!RPConfig.getBool("flat-file.region-per-file")){
			RPConfig.setConfig("flat-file.region-per-file", true);
		}
		RPConfig.save();
		return saved;
	}

	public static int FilesToSingle() {
		int saved = 0;		
		for (World w:Bukkit.getWorlds()){
			File f = new File(RedProtect.pathData, "data_" + w.getName() + ".yml");	
			Set<Region> regions = RedProtect.rm.getRegionsByWorld(w);	
			RPYaml fileDB = new RPYaml();
			for (Region r:regions){
				addProps(fileDB, r);
				saved++;
				File oldf = new File(RedProtect.pathData, w.getName()+File.separator+r.getName()+".yml");
				if (oldf.exists()){
					oldf.delete();
				}
			}
			File oldf = new File(RedProtect.pathData, w.getName());
			if (oldf.exists()){
				oldf.delete();
			}
			saveYaml(fileDB, f);			
		}
		if (RPConfig.getBool("flat-file.region-per-file")){
			RPConfig.setConfig("flat-file.region-per-file", false);
		}
		RPConfig.save();
		return saved;
	}
}
