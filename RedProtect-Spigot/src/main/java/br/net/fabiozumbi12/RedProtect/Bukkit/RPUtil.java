package br.net.fabiozumbi12.RedProtect.Bukkit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import java.util.Map;
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
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;

import br.net.fabiozumbi12.RedProtect.Bukkit.Ents.RPBukkitBlocks;
import br.net.fabiozumbi12.RedProtect.Bukkit.Ents.RPBukkitEntities;
import br.net.fabiozumbi12.RedProtect.Bukkit.Ents.TaskChain;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPConfig;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPLang;
import br.net.fabiozumbi12.RedProtect.Bukkit.hooks.AWEListener;
import br.net.fabiozumbi12.RedProtect.Bukkit.hooks.MojangUUIDs;
import br.net.fabiozumbi12.RedProtect.Bukkit.hooks.WEListener;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;

@SuppressWarnings("deprecation")
public class RPUtil {
    public static final HashMap<String, HashMap<Location, Material>> pBorders = new HashMap<>();
	public static boolean stopRegen = false;
	private static final HashMap<String, Integer> borderIds = new HashMap<>();
	private static final String pathData = RedProtect.get().getDataFolder() + File.separator + "data" + File.separator;
    	
	public static void saveResource(String name, File saveTo){
		try {
			InputStream isReader = RedProtect.class.getResourceAsStream(name);
			FileOutputStream fos = new FileOutputStream(saveTo);
			while (isReader.available() > 0) {
		        fos.write(isReader.read());
		    }
		    fos.close();
		    isReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static List<Location> get4Points(Location min, Location max, int y){
		List <Location> locs = new ArrayList<>();
		min.setY(y);
		max.setY(y);
		locs.add(min);		
		locs.add(new Location(min.getWorld(),min.getX(),y,min.getZ()+(max.getZ()-min.getZ())));
		locs.add(max);
		locs.add(new Location(min.getWorld(),min.getX()+(max.getX()-min.getX()),y,min.getZ()));
		return locs;		
	}
	
    public static boolean denyPotion(ItemStack result){
    	List<String> Pots = RPConfig.getStringList("server-protection.deny-potions");
    	if (result != null && Pots.size() > 0 && (result.getType().name().contains("POTION") || result.getType().name().contains("TIPPED"))){
    		String potname = "";
    		if (RedProtect.get().version >= 190){
    			PotionMeta pot = (PotionMeta) result.getItemMeta();
    			potname = pot.getBasePotionData().getType().name();
    		}
    		if (RedProtect.get().version < 190 && Potion.fromItemStack(result) != null && Potion.fromItemStack(result).getType() != null){
    			potname = Potion.fromItemStack(result).getType().name();
    		}
			return Pots.contains(potname);
    	}
    	return false;
    }
    
    public static boolean denyPotion(ItemStack result, Player p){
    	List<String> Pots = RPConfig.getStringList("server-protection.deny-potions");
    	if (result != null && Pots.size() > 0 && (result.getType().name().contains("POTION") || result.getType().name().contains("TIPPED"))){
    		String potname = "";
    		if (RedProtect.get().version >= 190){
    			PotionMeta pot = (PotionMeta) result.getItemMeta();
    			potname = pot.getBasePotionData().getType().name();
    		}
    		if (RedProtect.get().version <= 180 && Potion.fromItemStack(result) != null){
    			potname = Potion.fromItemStack(result).getType().name();
    		}    		
    		if (Pots.contains(potname)){    			
    			RPLang.sendMessage(p, "playerlistener.denypotion");
    			return true;
    		}        	    		
    	}
    	return false;
    }
    
    private static boolean isSecure(Location loc){
    	Block b = loc.add(0, -1, 0).getBlock();
    	return (!b.isLiquid() && !b.isEmpty()) || b.getType().name().contains("LAVA");
    }

	public static Location DenyExitPlayer(Player p, Location from, Location to, Region r) {
		Location setTo = to;
		Region rto = RedProtect.get().rm.getTopRegion(to);
		if (rto != r){
			setTo = from;
			RPLang.sendMessage(p, "playerlistener.region.cantregionexit");
		}
		return setTo;
	}

    public static Location DenyEnterPlayer(World wFrom, Location from, Location to, Region r, boolean checkSec) {
    	Location setTo = to;
    	for (int i = 0; i < r.getArea()+10; i++){
    		Region r1 = RedProtect.get().rm.getTopRegion(wFrom, from.getBlockX()+i, from.getBlockY(), from.getBlockZ());
    		Region r2 = RedProtect.get().rm.getTopRegion(wFrom, from.getBlockX()-i, from.getBlockY(), from.getBlockZ());
    		Region r3 = RedProtect.get().rm.getTopRegion(wFrom, from.getBlockX(), from.getBlockY(), from.getBlockZ()+i);
    		Region r4 = RedProtect.get().rm.getTopRegion(wFrom, from.getBlockX(), from.getBlockY(), from.getBlockZ()-i);
    		Region r5 = RedProtect.get().rm.getTopRegion(wFrom, from.getBlockX()+i, from.getBlockY(), from.getBlockZ()+i);
    		Region r6 = RedProtect.get().rm.getTopRegion(wFrom, from.getBlockX()-i, from.getBlockY(), from.getBlockZ()-i);
    		if (r1 != r){
    			setTo = from.add(+i, 0, 0);    			  
    			break;
    		} 
    		if (r2 != r){
    			setTo = from.add(-i, 0, 0);
    			break;
    		} 
    		if (r3 != r){
    			setTo = from.add(0, 0, +i);
    			break;
    		} 
    		if (r4 != r){
    			setTo = from.add(0, 0, -i);
    			break;
    		} 
    		if (r5 != r){
    			setTo = from.add(+i, 0, +i);
    			break;
    		} 
    		if (r6 != r){
    			setTo = from.add(-i, 0, -i);
    			break;
    		} 
		}    	
    	if (checkSec && !isSecure(setTo)){
			wFrom.getBlockAt(setTo.clone().add(0, -1, 0)).setType(Material.GRASS);
		} 
    	return setTo;
	}
    
    public static long getNowMillis(){
    	SimpleDateFormat sdf = new SimpleDateFormat(RPConfig.getString("region-settings.date-format"));			
		Calendar cal = Calendar.getInstance();
		try {
			cal.setTime(sdf.parse(sdf.format(cal.getTime())));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return cal.getTimeInMillis();
    }

	public static void performCommand(final ConsoleCommandSender consoleCommandSender, final String command) {
	    TaskChain.newChain().add(new TaskChain.GenericTask() {
	        public void run() {
	        	RedProtect.get().serv.dispatchCommand(consoleCommandSender,command);
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
        
    private static void SaveToZipYML(File file, String ZippedFile, YamlConfiguration yml){
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
    
    static void SaveToZipSB(File file, StringBuilder sb){
    	try{
    		final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file));
            ZipEntry e = new ZipEntry("RedProtectLogs.txt");
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
		HashMap<Long, File> keyFiles = new HashMap<>();
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
    	String rname;
    	World w = RedProtect.get().serv.getWorld(World);    	
            int i = 0;
            while (true) {
            	int is = String.valueOf(i).length();
                if (p.length() > 13) {
                	rname = p.substring(0, 14-is) + "_" + i;
                }
                else {
                	rname = p + "_" + i;
                }
                if (RedProtect.get().rm.getRegion(rname, w) == null) {
                    break;
                }
                ++i;
            }           
        return rname.replaceAll("[.+=;\\-]", "");
    }

    public static String DateNow(){
    	DateFormat df = new SimpleDateFormat(RPConfig.getString("region-settings.date-format"));
        Date today = Calendar.getInstance().getTime();
		return df.format(today);
    }
    
    static String HourNow(){
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int min = Calendar.getInstance().get(Calendar.MINUTE);
        int sec = Calendar.getInstance().get(Calendar.SECOND);        
		return "["+hour+":"+min+":"+sec+"]";    	
    }
    
    static void fixWorld(String regionname){
    	for (World w:RedProtect.get().serv.getWorlds()){
    		Region r = RedProtect.get().rm.getRegion(regionname, w);
    		if (r != null){
    			r.setWorld(w.getName());
    		}
    	}
    }
           
    //TODO read all db
    static void ReadAllDB(Set<Region> regions) {     	
    	RedProtect.get().logger.info("Loaded " + regions.size() + " regions (" + RPConfig.getString("file-type") + ")");
    	int i = 0;
    	int pls = 0;
    	int origupdt = 0;
    	int namesupdt = 0;
    	int purged = 0;
    	int sell = 0;
    	int dateint = 0;
    	int cfm = 0;
    	int delay = 0;
    	int skipped = 0;
    	Date now = null;    	   	
    	SimpleDateFormat dateformat = new SimpleDateFormat(RPConfig.getString("region-settings.date-format"));
    	
    	boolean checkNames = RPConfig.getBool("hooks.check-uuid-names-onstart");        	
    	    	
		try {
			now = dateformat.parse(DateNow());
		} catch (ParseException e1) {
			RedProtect.get().logger.severe("The 'date-format' don't match with date 'now'!!");
		}
		
        for (Region r:regions){    
        	r.updateSigns();
        	boolean serverRegion = false;
        	
        	if (r.isLeader(RPConfig.getString("region-settings.default-leader"))){
        		serverRegion = true;
        		r.setDate(DateNow());
        	} 
        	
        	//purge regions
        	if (RPConfig.getBool("purge.enabled") && !serverRegion){
        		Date regiondate = null;
            	try {
    				regiondate = dateformat.parse(r.getDate());
    			} catch (ParseException e) {
    				RedProtect.get().logger.severe("The 'date-format' don't match with region date!!");
    				e.printStackTrace();
    			}
            	Long days = TimeUnit.DAYS.convert(now.getTime() - regiondate.getTime(), TimeUnit.MILLISECONDS);            	

            	boolean ignore = false;
            	for (String play:RPConfig.getStringList("purge.ignore-regions-from-players")){
            		if (r.isLeader(RPUtil.PlayerToUUID(play)) || r.isAdmin(RPUtil.PlayerToUUID(play))){
            			ignore = true;
            			break;
            		}
    			}           	
            	
            	if (!ignore && days > RPConfig.getInt("purge.remove-oldest")){
            		if (RedProtect.get().WE && RPConfig.getBool("purge.regen.enable")){
            			if (r.getArea() <= RPConfig.getInt("purge.regen.max-area-regen")){
            				if (RedProtect.get().AWE && RPConfig.getBool("hooks.asyncworldedit.use-for-regen")){
                    			AWEListener.regenRegion(r, Bukkit.getWorld(r.getWorld()), r.getMaxLocation(), r.getMinLocation(), delay, null, true);
                    		} else {
                    			WEListener.regenRegion(r, Bukkit.getWorld(r.getWorld()), r.getMaxLocation(), r.getMinLocation(), delay, null, true);
                    		}                		
                    		delay=delay+10;
            			} else {
            				skipped++;
            				continue;
            			}                			
            		} else {
            			RedProtect.get().rm.remove(r, RedProtect.get().serv.getWorld(r.getWorld()));
            			//r.delete();
            			purged++;
            			RedProtect.get().logger.warning("Purging " + r.getName() + " - Days: " + days);
            		}
            		continue;
            	}
        	}    
        	
        	//sell rergions
        	if (RPConfig.getBool("sell.enabled") && !serverRegion){
        		Date regiondate = null;
            	try {
    				regiondate = dateformat.parse(r.getDate());
    			} catch (ParseException e) {
    				RedProtect.get().logger.severe("The 'date-format' don't match with region date!!");
    				e.printStackTrace();
    			}
            	Long days = TimeUnit.DAYS.convert(now.getTime() - regiondate.getTime(), TimeUnit.MILLISECONDS);

            	boolean ignore = false;
            	for (String play:RPConfig.getStringList("sell.ignore-regions-from-players")){
            		if (r.isLeader(RPUtil.PlayerToUUID(play)) || r.isAdmin(RPUtil.PlayerToUUID(play))){
            			ignore = true;
            			break;
            		}
    			}

            	if (!ignore && days > RPConfig.getInt("sell.sell-oldest")){
                	RedProtect.get().logger.warning("Selling " + r.getName() + " - Days: " + days);
            		RPEconomy.putToSell(r, RPConfig.getString("region-settings.default-leader"), RPEconomy.getRegionValue(r));
            		sell++;
            		RedProtect.get().rm.saveAll();
            		continue;
            	}
        	}
        	
        	//Update player names
        	List<String> leadersl = r.getLeaders();
        	List<String> adminsl = r.getAdmins();
        	List<String> membersl = r.getMembers(); 
        	
        	if (origupdt >= 90 || namesupdt >= 90){
        		try {
					Thread.sleep(100L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
        	}
        	
        	if (!serverRegion && checkNames){
        		if (RedProtect.get().OnlineMode){
                	for (int l = 0; l < leadersl.size(); l++){
                		String pname = leadersl.get(l).replace("[", "").replace("]", "");
                		if (!isUUIDs(pname) && pname != null && !RPUtil.isDefaultServer(pname)){
                			String uuid = MojangUUIDs.getUUID(pname);
                			if (uuid == null){
                				uuid = PlayerToUUID(pname);
                			}
                    		RedProtect.get().logger.warning("Leader from: " + pname);
                    		leadersl.set(l, uuid);
                    		RedProtect.get().logger.warning("To UUID: " + uuid);
                    		origupdt++;
                		}             		
                	} 
                	for (int o = 0; o < adminsl.size(); o++){
                		String pname = adminsl.get(o).replace("[", "").replace("]", "");
                		if (!isUUIDs(pname) && pname != null && !RPUtil.isDefaultServer(pname)){
                			String uuid = MojangUUIDs.getUUID(pname);
                			if (uuid == null){
                				uuid = PlayerToUUID(pname);
                			}
                    		RedProtect.get().logger.warning("Admin from: " + pname);
                    		adminsl.set(o, uuid);
                    		RedProtect.get().logger.warning("To UUID: " + uuid);
                    		origupdt++;
                		}             		
                	}        	
                	for (int m = 0; m < membersl.size(); m++){
                		String pname = membersl.get(m).replace("[", "").replace("]", "");     		
                		if (!isUUIDs(pname) && pname != null && !RPUtil.isDefaultServer(pname)){
                			String uuid = MojangUUIDs.getUUID(pname);
                			if (uuid == null){
                				uuid = PlayerToUUID(pname);
                			}
                    		RedProtect.get().logger.warning("Member from: " + pname);
                    		membersl.set(m, uuid);
                    		RedProtect.get().logger.warning("To UUID: " + uuid);  
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
            	//if Offline Mode
            	else {
            		for (int l = 0; l < leadersl.size(); l++){
            			if (isUUIDs(leadersl.get(l)) && !RPUtil.isDefaultServer(leadersl.get(l))){
            				try {
    							String name = MojangUUIDs.getName(leadersl.get(l));
    							if (name == null){
    								name = UUIDtoPlayer(leadersl.get(l));
    							}
    							RedProtect.get().logger.warning("Leader from: " + leadersl.get(l));
    							leadersl.set(l, name.toLowerCase());
    							RedProtect.get().logger.warning("To UUID: " + name); 
    							namesupdt++;
    						} catch (Exception e) {
    							e.printStackTrace();
    						}
            			}
            		}
            		
            		for (int a = 0; a < adminsl.size(); a++){
            			if (isUUIDs(adminsl.get(a)) && !RPUtil.isDefaultServer(adminsl.get(a))){
            				try {
    							String name = MojangUUIDs.getName(adminsl.get(a));
    							if (name == null){
    								name = UUIDtoPlayer(adminsl.get(a));
    							}
    							RedProtect.get().logger.warning("Admin from: " + adminsl.get(a));
    							adminsl.set(a, name.toLowerCase());
    							RedProtect.get().logger.warning("To UUID: " + name); 
    							namesupdt++;
    						} catch (Exception e) {
    							e.printStackTrace();
    						}
            			}
            		}
            		
            		for (int m = 0; m < membersl.size(); m++){
            			if (isUUIDs(membersl.get(m)) && !RPUtil.isDefaultServer(membersl.get(m))){
            				try {
    							String name = MojangUUIDs.getName(membersl.get(m));
    							if (name == null){
    								name = UUIDtoPlayer(membersl.get(m));
    							}
    							RedProtect.get().logger.warning("Member from: " + membersl.get(m));
    							membersl.set(m, name.toLowerCase());
    							RedProtect.get().logger.warning("To UUID: " + name); 
    							namesupdt++;
    						} catch (Exception e) {
    							e.printStackTrace();
    						}
            			}
            		}
            		r.setLeaders(leadersl);
                	r.setAdmins(adminsl);
                	r.setMembers(membersl);
                	if (namesupdt > 0){
                		pls++;
                	} 
            	}
        		
        		//import essentials last visit for player dates
            	if (RPConfig.getBool("hooks.essentials.import-lastvisits") && RedProtect.get().Ess){ 
                	Essentials ess = (Essentials) Bukkit.getServer().getPluginManager().getPlugin("Essentials");
                	List<Long> dates = new ArrayList<>();
                	
                	for (String pname:adminsl){
                		User essp;
                		if (RedProtect.get().OnlineMode){
                			essp = ess.getUser(UUID.fromString(pname));
                		} else {
                			essp = ess.getOfflineUser(pname);
                		}
                		if (essp != null){
                			dates.add(essp.getLastLogout());
                			RedProtect.get().logger.info("Updated user date: "+pname+" - "+dateformat.format(essp.getLastLogout()));
                		}            		
                	}
                	for (String pname:leadersl){
                		User essp;
                		if (RedProtect.get().OnlineMode){
                			essp = ess.getUser(UUID.fromString(pname));
                		} else {
                			essp = ess.getOfflineUser(pname);
                		}            		
                		if (essp != null){
                			dates.add(essp.getLastLogout());
                			RedProtect.get().logger.info("Updated user date: "+pname+" - "+dateformat.format(essp.getLastLogout()));
                		}
                	} 
                	
                	if (dates.size() > 0){
                		Date lastvisit = new Date(Collections.max(dates));
                		r.setDate(dateformat.format(lastvisit));
            			RedProtect.get().logger.info("Updated "+ dates.size() +" last visit users ("+dateformat.format(lastvisit)+")");
            			dates.clear();
                	}
            	}
        	}
        	        	
        	if (pls > 0){
        		RedProtect.get().logger.sucess("["+pls+"] Region updated &6&l" + r.getName() + "&a&l. Leaders &6&l" + r.getLeadersDesc());
            }      
        	
        	//conform region names        	
        	if (r.getName().contains("/")){
    			String rname = r.getName().replace("/", "|");
    			RedProtect.get().rm.renameRegion(rname, r);
    			cfm++;
    		}       
        	
        	if (RedProtect.get().SC){
        		//remove deleted clans from regions
        		if (r.flagExists("clan")  && !RedProtect.get().clanManager.isClan(r.getFlagString("clan"))){
        			r.setFlag("clan", "");
        		}        		
        	}
        }     
        
        if (delay > 0){
    		RedProtect.get().logger.warning("&c> There's "+delay/10+" regions to be regenerated at 2 regions/second.");
    		RedProtect.get().logger.severe("&cRegen can take long time, but your players can join and play normally!");
        }
        
        if (cfm > 0){
    		RedProtect.get().logger.sucess("["+cfm+"] Region names conformed!");
        }
        
        if (dateint > 0){
			RedProtect.get().logger.info("Updated "+ dateint +" last visit users!");
			RedProtect.get().rm.saveAll();
    	}
                   	        
        if (i > 0 || pls > 0){
        	if (i > pls){
            	RedProtect.get().logger.sucess("Updated a total of &6&l" + (i-pls) + "&a&l regions!");
        	} else {
            	RedProtect.get().logger.sucess("Updated a total of &6&l" + (pls-i) + "&a&l regions!");
        	}
        	RedProtect.get().rm.saveAll();        	
        	RedProtect.get().logger.sucess("Regions saved!");  
        	pls = 0;
        	i = 0;
        }
        
        if (skipped > 0){
        	RedProtect.get().logger.sucess(skipped + " regions skipped due to max size limit to regen!");
        	skipped = 0;
        }
        
        if (purged > 0){
        	RedProtect.get().logger.sucess("Purged a total of &6" + purged + "&a regions!");
        	purged = 0;
        }
        
        if (sell > 0){
        	RedProtect.get().logger.sucess("Put to sell a total of &6" + sell + "&a regions!");
        	sell = 0;
        }
        regions.clear();   
	}
        
	public static String PlayerToUUID(String PlayerName){
    	if (PlayerName == null || PlayerName.equals("")){
    		return null;
    	}
    	
    	//check if is already UUID
    	if (isUUIDs(PlayerName) || isDefaultServer(PlayerName) || (PlayerName.startsWith("[") && PlayerName.endsWith("]"))){
    		return PlayerName;
    	}
    	
    	String uuid = PlayerName;

    	if (!RedProtect.get().OnlineMode){
    		uuid = uuid.toLowerCase();
    		return uuid;
    	}
    	
    	try{
    		OfflinePlayer offp = RedProtect.get().serv.getOfflinePlayer(PlayerName);    		
    		uuid = offp.getUniqueId().toString();
		} catch (IllegalArgumentException e){	
	    	Player onp = RedProtect.get().serv.getPlayer(PlayerName);
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
    	if (isDefaultServer(uuid) || !isUUIDs(uuid)){
    		return uuid;
    	}
    	
    	String PlayerName = uuid;
    	UUID uuids;
    	
    	if (!RedProtect.get().OnlineMode){
	    	PlayerName = uuid.toLowerCase();	    	
    		return PlayerName;
    	}
    	
    	try{
    		uuids = UUID.fromString(uuid);
    		OfflinePlayer offp = RedProtect.get().serv.getOfflinePlayer(uuids);
    		PlayerName = offp.getName();
		} catch (IllegalArgumentException e){	
			Player onp = RedProtect.get().serv.getPlayer(uuid);
	    	if (onp != null){
	    		PlayerName = onp.getName();
	    	}
		}  	
		return PlayerName;    	
    }
    
	public static boolean isDefaultServer(String check){
		return check.equalsIgnoreCase(RPConfig.getString("region-settings.default-leader"));
	}
	
	public static boolean isUUIDs(String uuid){
    	if (uuid == null){
    		return false;
    	}
    	
    	try{
    		UUID.fromString(uuid);
    		return true;
    	} catch (IllegalArgumentException e){
    		return false;
    	}		
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
    
    private static YamlConfiguration fixdbFlags(YamlConfiguration db, String rname){
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
    
    public static boolean mysqlToYml(){
    	HashMap<String,Region> regions = new HashMap<>();
    	int saved = 1;
    	
        try {
        	Connection dbcon = DriverManager.getConnection("jdbc:mysql://"+RPConfig.getString("mysql.host")+"/"+RPConfig.getString("mysql.db-name")+"?autoReconnect=true", RPConfig.getString("mysql.user-name"), RPConfig.getString("mysql.user-pass"));
            	
        	for (World world:Bukkit.getWorlds()){
            	String tableName = RPConfig.getString("mysql.table-prefix")+world.getName();
                PreparedStatement st = dbcon.prepareStatement("SELECT * FROM `"+tableName+"` WHERE world=?");
                st.setString(1, world.getName());
                ResultSet rs = st.executeQuery();            
                while (rs.next()){ 
                	List<String> leaders = new ArrayList<>();
                	List<String> admins = new ArrayList<>();
                    List<String> members = new ArrayList<>();
                    HashMap<String, Object> flags = new HashMap<>();
                    
                    int maxMbrX = rs.getInt("maxMbrX");
                    int minMbrX = rs.getInt("minMbrX");
                    int maxMbrZ = rs.getInt("maxMbrZ");
                    int minMbrZ = rs.getInt("minMbrZ");
                    int maxY = rs.getInt("maxY");
                    int minY = rs.getInt("minY");
                    int prior = rs.getInt("prior");
                    String rname = rs.getString("name");
                    String date = rs.getString("date");
                    String wel = rs.getString("wel");
                    long value = rs.getLong("value");
                    
                    Location tppoint = null;
                    if (rs.getString("tppoint") != null && !rs.getString("tppoint").equalsIgnoreCase("")){
                    	String tpstring[] = rs.getString("tppoint").split(",");
                        tppoint = new Location(world, Double.parseDouble(tpstring[0]), Double.parseDouble(tpstring[1]), Double.parseDouble(tpstring[2]), 
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
                    for (String flag:rs.getString("flags").split(",")){
                    	String key = flag.split(":")[0];
                    	String replace = key+":";
                    	if (replace.length() <= flag.length()){
                    		flags.put(key, RPUtil.parseObject(flag.substring(replace.length())));  
                    	} 
                    }                    
                    Region newr = new Region(rname, admins, members, leaders, maxMbrX, minMbrX, maxMbrZ, minMbrZ, minY, maxY, flags, wel, prior, world.getName(), date, value, tppoint, true);
                    regions.put(rname, newr);
                } 
                st.close(); 
                rs.close();  
                
                //write to yml
                YamlConfiguration fileDB = new YamlConfiguration();
                File datf = new File(pathData, "data_" + world.getName() + ".yml");
                
                for (Region r:regions.values()){
        			if (r.getName() == null){
        				continue;
        			}
        			
        			if (RPConfig.getBool("flat-file.region-per-file")) {
        				if (!r.toSave()){
            				continue;
            			}
        				fileDB = new YamlConfiguration();
                    	datf = new File(pathData, world.getName()+File.separator+r.getName()+".yml");        	
                    }
        			
        			fileDB = RPUtil.addProps(fileDB, r);   
        			saved++;
        			
        			if (RPConfig.getBool("flat-file.region-per-file")) {
        				saveYaml(fileDB, datf);
        				r.setToSave(false);
        			}
        		}	 
        		
        		if (!RPConfig.getBool("flat-file.region-per-file")) {
        			backupRegions(fileDB, world.getName());
        			saveYaml(fileDB, datf);
        		} else {
        			//remove deleted regions
        			File wfolder = new File(pathData + world.getName());
        			if (wfolder.exists()){
        				File[] listOfFiles = new File(pathData + world.getName()).listFiles();    				
                		for (File region:listOfFiles){
                			if (region.isFile() && !regions.containsKey(region.getName().replace(".yml", ""))){
                				region.delete();
                			}
                		}
        			}        			
        		}
            }  
        	dbcon.close();
        	
        	if (saved > 0){
    			RedProtect.get().logger.sucess((saved-1) + " regions converted to Yml with sucess!");
    		}        	
    	} catch (SQLException e) {
            e.printStackTrace();
        }
        
    	return true;
    }
    
    public static void backupRegions(YamlConfiguration fileDB, String world) {
        if (!RPConfig.getBool("flat-file.backup") || fileDB.getKeys(true).isEmpty()) {
            return;
        }
        
        File bfolder = new File(pathData+"backups"+File.separator);
        if (!bfolder.exists()){
        	bfolder.mkdir();
        }
        
        File folder = new File(pathData+"backups"+File.separator+world+File.separator);
        if (!folder.exists()){
        	folder.mkdir();
        	RedProtect.get().logger.info("Created folder: " + folder.getPath()); 
        }
        
        //Save backup
        if (RPUtil.genFileName(folder.getPath()+File.separator, true) != null){
        	RPUtil.SaveToZipYML(RPUtil.genFileName(folder.getPath()+File.separator, true), "data_" + world + ".yml", fileDB); 
        }		       
    }
    
	public static boolean ymlToMysql() throws Exception{
		if (!RPConfig.getString("file-type").equalsIgnoreCase("yml")){
			return false;
		}
		RedProtect.get().rm.saveAll();
		
		initMysql();//Create tables
		int counter = 1;
		
		for (World world:Bukkit.getWorlds()){
			
			String dbname = RPConfig.getString("mysql.db-name");
		    String url = "jdbc:mysql://"+RPConfig.getString("mysql.host")+"/";
		    String tableName = RPConfig.getString("mysql.table-prefix")+world.getName();
		    
			Connection dbcon = DriverManager.getConnection(url + dbname, RPConfig.getString("mysql.user-name"), RPConfig.getString("mysql.user-pass"));
			
			for (Region r:RedProtect.get().rm.getRegionsByWorld(world)){
				if (!regionExists(dbcon, r.getName(), tableName)) {
					try {                
		                PreparedStatement st = dbcon.prepareStatement("INSERT INTO `"+tableName+"` (name,leaders,admins,members,maxMbrX,minMbrX,maxMbrZ,minMbrZ,minY,maxY,centerX,centerZ,date,wel,prior,world,value,tppoint,candelete,flags) "
		                		+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");    
		                st.setString(1, r.getName());
		                st.setString(2, r.getLeaders().toString().replace("[", "").replace("]", ""));
		                st.setString(3, r.getAdmins().toString().replace("[", "").replace("]", ""));
		                st.setString(4, r.getMembers().toString().replace("[", "").replace("]", ""));
		                st.setInt(5, r.getMaxMbrX());
		                st.setInt(6, r.getMinMbrX());
		                st.setInt(7, r.getMaxMbrZ());
		                st.setInt(8, r.getMinMbrZ());
		                st.setInt(9, r.getMinY());
		                st.setInt(10, r.getMaxY());
		                st.setInt(11, r.getCenterX());
		                st.setInt(12, r.getCenterZ());
		                st.setString(13, r.getDate());
		                st.setString(14, r.getWelcome());
		                st.setInt(15, r.getPrior());
		                st.setString(16, r.getWorld());
		                st.setLong(17, r.getValue());
		                st.setString(18, r.getTPPointString());
		                st.setInt(20, r.canDelete() ? 1 : 0);
		                st.setString(21, r.getFlagStrings());
						
		                st.executeUpdate();
		                st.close();
		                counter++;
		            }
		            catch (SQLException e) {
		                e.printStackTrace();
		            }
		        }
			}
			dbcon.close();
		}		
		if (counter > 0){
			RedProtect.get().logger.sucess((counter-1) + " regions converted to Mysql with sucess!");
		}
		return true;		
	}
	
	private static void initMysql() throws Exception{
		for (World world:Bukkit.getWorlds()){
			
		    String url = "jdbc:mysql://"+RPConfig.getString("mysql.host")+"/";
		    String reconnect = "?autoReconnect=true";
		    String tableName = RPConfig.getString("mysql.table-prefix")+world.getName();
		    
	        try {
	            Class.forName("com.mysql.jdbc.Driver");
	        }
	        catch (ClassNotFoundException e2) {
	            RedProtect.get().logger.severe("Couldn't find the driver for MySQL! com.mysql.jdbc.Driver.");
	            return;
	        }
	        PreparedStatement st = null;	        
	        try {
	        	if (!checkTableExists(tableName)) {
	        		//create db
	                Connection con = DriverManager.getConnection(url+RPConfig.getString("mysql.db-name")+reconnect, RPConfig.getString("mysql.user-name"), RPConfig.getString("mysql.user-pass"));  
	                st = con.prepareStatement("CREATE TABLE `"+tableName+"` (name varchar(20) PRIMARY KEY NOT NULL, leaders longtext, admins longtext, members longtext, maxMbrX int, minMbrX int, maxMbrZ int, minMbrZ int, centerX int, centerZ int, minY int, maxY int, date varchar(10), wel longtext, prior int, world varchar(100), value Long not null, tppoint mediumtext, flags longtext, candelete tinyint(1)) CHARACTER SET utf8 COLLATE utf8_general_ci");
	                st.executeUpdate();
	                st.close();
	                st = null;
	                RedProtect.get().logger.info("Created table: "+tableName+"!");  
	            }
	        	addNewColumns(tableName);
	        }
	        catch (CommandException e3) {
	            RedProtect.get().logger.severe("Couldn't connect to mysql! Make sure you have mysql turned on and installed properly, and the service is started.");
	            throw new Exception("Couldn't connect to mysql!");
	        }
	        catch (SQLException e) {
	            e.printStackTrace();
	            RedProtect.get().logger.severe("There was an error while parsing SQL, redProtect will still with actual DB setting until you change the connection options or check if a Mysql service is running. Use /rp reload to try again");
	        }
	        finally {
	            if (st != null) {
	                st.close();
	            }
	        }
		}	    
	}
		
	private static void addNewColumns(String tableName){
		try {
			String url = "jdbc:mysql://"+RPConfig.getString("mysql.host")+"/";
			Connection con = DriverManager.getConnection(url + RPConfig.getString("mysql.db-name"), RPConfig.getString("mysql.user-name"), RPConfig.getString("mysql.user-pass"));
			DatabaseMetaData md = con.getMetaData();
			ResultSet rs = md.getColumns(null, null, tableName, "candelete");
			if (!rs.next()) {				
				PreparedStatement st = con.prepareStatement("ALTER TABLE `"+tableName+"` ADD `candelete` tinyint(1) NOT NULL default '1'");
				st.executeUpdate();
			}
			rs.close();
			con.close();			
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}
	
	private static boolean regionExists(Connection dbcon, String name, String tableName) {
        int total = 0;
        try {
        	PreparedStatement st = dbcon.prepareStatement("SELECT COUNT(*) FROM `"+tableName+"` WHERE name = ?");
        	st.setString(1, name);
            ResultSet rs = st.executeQuery();
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
	
	private static boolean checkTableExists(String tableName) {
        try {
        	RedProtect.get().logger.debug("Checking if table exists... " + tableName);
        	Connection con = DriverManager.getConnection("jdbc:mysql://"+RPConfig.getString("mysql.host")+"/"+RPConfig.getString("mysql.db-name"),RPConfig.getString("mysql.user-name"), RPConfig.getString("mysql.user-pass"));
        	DatabaseMetaData meta = con.getMetaData();
            ResultSet rs = meta.getTables(null, null, tableName, null);
            if (rs.next()) {
            	con.close();
            	rs.close();
            	return true;               
            }    
            con.close();
        	rs.close();
        } catch (SQLException e){
        	e.printStackTrace();
        }        
        return false;
    }
	
	public static void startFlagChanger(final String r, final String flag, final Player p){
		RedProtect.get().changeWait.add(r+flag);
		Bukkit.getScheduler().scheduleSyncDelayedTask(RedProtect.get().get(), () -> {
            if (RedProtect.get().changeWait.contains(r+flag)){
                /*if (p != null && p.isOnline()){
                    RPLang.sendMessage(p, RPLang.get("gui.needwait.ready").replace("{flag}", flag));
                }*/
                RedProtect.get().changeWait.remove(r+flag);
            }
        }, RPConfig.getInt("flags-configuration.change-flag-delay.seconds")*20);
	}
	
	public static int getUpdatedPrior(Region region) {
		int regionarea = region.getArea();  
		int prior = region.getPrior();
        Region topRegion = RedProtect.get().rm.getTopRegion(RedProtect.get().serv.getWorld(region.getWorld()), region.getCenterX(), region.getCenterY(), region.getCenterZ());
        Region lowRegion = RedProtect.get().rm.getLowRegion(RedProtect.get().serv.getWorld(region.getWorld()), region.getCenterX(), region.getCenterY(), region.getCenterZ());
        
        if ((topRegion != null && topRegion.getID().equals(region.getID())) || (lowRegion != null && lowRegion.getID().equals(region.getID()))){
        	return prior;
        }
        
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
	 * @param p Player.
	 * @param locs {@code List<Location>}.
	 */
	public static void addBorder(final Player p, List<Location> locs) {	
		final World w = p.getWorld();
		boolean msg = true;
		if (pBorders.containsKey(p.getName())){
    		for (Location loc:pBorders.get(p.getName()).keySet()){
    			w.getBlockAt(loc).setType(pBorders.get(p.getName()).get(loc));            			
    		}	    
    		if (borderIds.containsKey(p.getName())){
    			Bukkit.getScheduler().cancelTask(borderIds.get(p.getName()));
    			borderIds.remove(p.getName());
    		}	            		
    		pBorders.remove(p.getName());
    		msg = false;
		}
		
		final HashMap<Location, Material> borderBlocks = new HashMap<>();
		
		for (Location loc:locs){
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
			if (msg){
				RPLang.sendMessage(p, "cmdmanager.addingborder");
			}			
			pBorders.put(p.getName(), borderBlocks);
			int taskid = Bukkit.getScheduler().scheduleSyncDelayedTask(RedProtect.get().get(), () -> {
                if (pBorders.containsKey(p.getName())){
                    for (Location loc:pBorders.get(p.getName()).keySet()){
                        w.getBlockAt(loc).setType(pBorders.get(p.getName()).get(loc));
                    }
                    if (borderIds.containsKey(p.getName())){
                        borderIds.remove(p.getName());
                    }
                    pBorders.remove(p.getName());
                    RPLang.sendMessage(p, "cmdmanager.removingborder");
                }
            }, RPConfig.getInt("region-settings.border.time-showing")*20);
			borderIds.put(p.getName(), taskid);
		}		             
    }		
	
	public static int convertFromGP(){		
		int claimed = 0;
		Collection<Claim> claims = GriefPrevention.instance.dataStore.getClaims();
		for (Claim claim:claims){
			if (Bukkit.getWorlds().contains(claim.getGreaterBoundaryCorner().getWorld())){
				World w = claim.getGreaterBoundaryCorner().getWorld();
				String pname = claim.getOwnerName().replace(" ", "_").toLowerCase();
				if (RedProtect.get().OnlineMode && claim.ownerID != null){
					pname = claim.ownerID.toString();
				}
				List<String> leaders = new ArrayList<>();
				leaders.add(pname);
				Location newmin = claim.getGreaterBoundaryCorner();
				Location newmax = claim.getLesserBoundaryCorner();
				newmin.setY(0);
				newmax.setY(w.getMaxHeight());
				
				Region r = new Region(nameGen(claim.getOwnerName().replace(" ", "_"), w.getName()), new ArrayList<>(), new ArrayList<>(), leaders,
						newmin, newmax, RPConfig.getDefFlagsValues(), "GriefPrevention region", 0, w.getName(), DateNow(), 0, null, true);				
				
				Region other = RedProtect.get().rm.getTopRegion(w, r.getCenterX(), r.getCenterY(), r.getCenterZ());
				if (other != null && r.getWelcome().equals(other.getWelcome())){
				} else {
					RedProtect.get().rm.add(r, w);
					RedProtect.get().logger.debug("Region: " + r.getName());
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

	public static String getTitleName(Region r){
		String name = RPLang.get("gui.invflag").replace("{region}", r.getName());
		if (name.length() > 16){
			name = name.substring(0, 16);    			
		}
		return name;
	}
	/*
	public static boolean RemoveGuiItem(ItemStack item) {
    	if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()){
    		try{
    			String lore = item.getItemMeta().getLore().get(1);
    			if (RPConfig.getDefFlags().contains(lore.replace("§0", "")) || lore.equals(RPConfig.getGuiString("separator"))){
    				return true;
    			}
    		} catch (IndexOutOfBoundsException ignored){
    		}    		
    	}
    	return false;
	}
*/
	private static void saveYaml(YamlConfiguration fileDB, File file){
    	try {         			   
    		fileDB.save(file);         			
		} catch (IOException e) {
			RedProtect.get().logger.severe("Error during save database file");
			e.printStackTrace();
		}
    }
	
	public static Region loadProps(YamlConfiguration fileDB, String rname, World world){
		//rname = rname.replaceAll("[.+=;_\\-]", "");
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
    	String welcome = fileDB.getString(rname+".welcome", "");
    	int prior = fileDB.getInt(rname+".priority", 0);
    	String date = fileDB.getString(rname+".lastvisit", "");
    	long value = fileDB.getLong(rname+".value", 0);
    	boolean candel = fileDB.getBoolean(rname+".candelete", true);
    	
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
  	    Region newr = new Region(name, admins, members, leaders, new int[] {minX,minX,maxX,maxX}, new int[] {minZ,minZ,maxZ,maxZ}, minY, maxY, prior, world.getName(), date, RPConfig.getDefFlagsValues(), welcome, value, tppoint, candel);
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
	
	public static YamlConfiguration addProps(YamlConfiguration fileDB, Region r){
		RedProtect.get().logger.debug("Region ID: "+r.getID());
		RedProtect.get().logger.debug("Region: "+r.getName());
		String rname = r.getName().replaceAll("[.+=;\\-]", "");
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
		fileDB.set(rname+".value",r.getValue());	
		fileDB.set(rname+".flags",r.flags);	
		fileDB.set(rname+".candelete",r.canDelete());	
		
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
			Set<Region> regions = RedProtect.get().rm.getRegionsByWorld(w);			
			for (Region r:regions){
				YamlConfiguration fileDB = new YamlConfiguration();
				
				File f = new File(pathData + w.getName());
        		if (!f.exists()){
        			f.mkdir();
        		}
				File wf = new File(pathData, w.getName()+File.separator+r.getName()+".yml");  
												
    			saved++;
    			saveYaml(addProps(fileDB, r), wf);    			
			} 
			
			File oldf = new File(pathData + "data_" + w.getName() + ".yml");
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
			File f = new File(pathData, "data_" + w.getName() + ".yml");	
			Set<Region> regions = RedProtect.get().rm.getRegionsByWorld(w);	
			YamlConfiguration fileDB = new YamlConfiguration();
			for (Region r:regions){
				addProps(fileDB, r);
				saved++;
				File oldf = new File(pathData, w.getName()+File.separator+r.getName()+".yml");
				if (oldf.exists()){
					oldf.delete();
				}
			}
			File oldf = new File(pathData, w.getName());
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

	public static boolean canBuildNear(Player p, Location loc) {
		if (RPConfig.getInt("region-settings.deny-build-near") == 0){
			return true;
		}
		int x = loc.getBlockX();
		int y = loc.getBlockY();
		int z = loc.getBlockZ();
		int radius = RPConfig.getInt("region-settings.deny-build-near");
		
		for (int ix = x-radius; ix <= x+radius; ++ix) {
			for (int iy = y-radius; iy <= y+radius; ++iy) {
				for (int iz = z-radius; iz <= z+radius; ++iz) {
					Region reg = RedProtect.get().rm.getTopRegion(new Location(p.getWorld(),ix, iy, iz));
					if (reg != null && !reg.canBuild(p)){
						RPLang.sendMessage(p, RPLang.get("blocklistener.cantbuild.nearrp").replace("{distance}", ""+radius));
						return false;
					}
				}
            }
		}		
		return true;
	}

	public static int simuleTotalRegionSize(String player, Region r2) {
		int total = 0;
		int regs = 0;			
		for (Location loc:r2.get4Points(r2.getCenterY())){		
			Map<Integer, Region> pregs = RedProtect.get().rm.getGroupRegion(loc);
			pregs.remove(r2.getPrior());				
			Region other = null;
			if (pregs.size() > 0){
				other = pregs.get(Collections.max(pregs.keySet()));
			} else {
				continue;
			}				
			//RedProtect.get().logger.severe("Reg: "+other.getName());
			if (!r2.getID().equals(other.getID()) && r2.getPrior() > other.getPrior() && other.isLeader(player)){
				regs++;
				//RedProtect.get().logger.severe("Reg added: "+other.getName());
			}
		}			
		//RedProtect.get().logger.severe("Regs size: "+regs);
		if (regs == 0 || regs != 4){
			total += r2.getArea();
		}
		
		return total;
	}
		
	public static String regionNameConfiorm(String regionName, Player p){		
		String pRName = RPUtil.UUIDtoPlayer(p.getName());
		if (regionName.equals("")) {
            int i = 0;            
            regionName = RPUtil.StripName(pRName)+"_"+0;            
            while (RedProtect.get().rm.getRegion(regionName, p.getWorld()) != null) {
            	++i;
            	regionName = RPUtil.StripName(pRName)+"_"+i;   
            }            
            if (regionName.length() > 16) {
            	RPLang.sendMessage(p, "regionbuilder.autoname.error");
                return null;
            }
        }
        if (regionName.contains("@")) {
            p.sendMessage(RPLang.get("regionbuilder.regionname.invalid.charac").replace("{charac}", "@"));
            return null;
        }
        
        //region name conform
        regionName = regionName.replace("/", "|");        
        if (RedProtect.get().rm.getRegion(regionName, p.getWorld()) != null) {
        	RPLang.sendMessage(p, "regionbuilder.regionname.existis");
            return null;
        }
        if (regionName.length() < 3 || regionName.length() > 16) {
        	RPLang.sendMessage(p, "regionbuilder.regionname.invalid");
            return null;
        }
        
        return regionName;
	}	
}
