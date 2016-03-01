package br.net.fabiozumbi12.RedProtect;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import br.net.fabiozumbi12.RedProtect.config.RPConfig;
import br.net.fabiozumbi12.RedProtect.config.RPLang;

public class Region implements Serializable{
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 2861198224185302015L;
    private int[] x;
    private int[] z;
    private int minMbrX;
    private int maxMbrX;
    private int minMbrZ;
    private int maxMbrZ;
    private int minY;
    private int maxY;
    private int prior;
    private String name;
    private List<String> leaders;
    private List<String> admins;
    private List<String> members;
    private String wMessage;
    private String world;
    private String date;
    public Map<String, Object> flags = new HashMap<String,Object>();
    protected boolean[] f = new boolean[10];
	private long value;
	private Location tppoint;
	private boolean tosave = true;
	
	
	/**Get unique ID of region based on name of region + world.
	 * @return {@code id string}
	 */
	public String getID(){
		return this.name+"@"+this.world;
	}
	
	public boolean toSave(){
		return this.tosave;
	}
	
	public void setToSave(boolean save){
		this.tosave = save;
	}
	
    public void setFlag(String Name, Object value) {
    	setToSave(true);
    	this.flags.put(Name, value);
    	RedProtect.rm.updateLiveFlags(this, Name, value.toString());
    }
    
    public void removeFlag(String Name) {
    	setToSave(true);
    	if (this.flags.containsKey(Name)){
    		this.flags.remove(Name); 
    		RedProtect.rm.removeLiveFlags(this, Name);
    	}    	               
    }
    
    public void setDate(String value) {
    	setToSave(true);
        this.date = value;
        RedProtect.rm.updateLiveRegion(this, "date", value);
    }    
    
    public void setTPPoint(Location loc){  
    	setToSave(true);
    	this.tppoint = loc;
    	if (loc != null){
    		double x = loc.getX();
        	double y = loc.getY();
        	double z = loc.getZ();
        	float yaw = loc.getYaw();
        	float pitch = loc.getPitch();
        	RedProtect.rm.updateLiveRegion(this, "tppoint", x+","+y+","+z+","+yaw+","+pitch);
    	} else {
    		RedProtect.rm.updateLiveRegion(this, "tppoint", "");
    	}
    	
    }
    
    public Location getTPPoint(){
    	return this.tppoint;
    }
    
    public String getDate() {
        return this.date;
    }
    
    public int getMaxY() {
        return this.maxY;
    }
    
    public void setMaxY(int y) {
    	setToSave(true);
        this.maxY = y;
        RedProtect.rm.updateLiveRegion(this, "maxy", String.valueOf(y));
    }
    
    public int getMinY() {
        return this.minY;
    }
    
    public void setMinY(int y) {
    	setToSave(true);
        this.minY = y;
        RedProtect.rm.updateLiveRegion(this, "miny", String.valueOf(y));
    }
    
    public void setWorld(String w) {
    	setToSave(true);
        this.world = w;
        RedProtect.rm.updateLiveRegion(this, "world", w);
    }   
    
    public Location getMaxLocation(){
    	return new Location(Bukkit.getWorld(this.world), this.maxMbrX, this.maxY, this.maxMbrZ);
    }
    
    public Location getMinLocation(){
    	return new Location(Bukkit.getWorld(this.world), this.minMbrX, this.minY, this.minMbrZ);
    }
    
    public String getWorld() {
        return this.world;
    }
    
    public void setPrior(int prior) {
    	setToSave(true);
        this.prior = prior;
        RedProtect.rm.updateLiveRegion(this, "prior", ""+prior);
    }
    
    public int getPrior() {
        return this.prior;
    }
    
    public void setWelcome(String s){
    	setToSave(true);
    	this.wMessage = s;
    	RedProtect.rm.updateLiveRegion(this, "wel", s);
    }
    
    public String getWelcome(){
    	if (wMessage == null){
    		return "";
    	}
    	return this.wMessage;
    }
    
    public void setX(int[] x) {
    	setToSave(true);
        this.x = x;
    }
    
    public void setZ(int[] z) {
    	setToSave(true);
        this.z = z;
    }
    
    public void setLeaders(List<String> leaders) {
    	setToSave(true);
        this.leaders = leaders;
        RedProtect.rm.updateLiveRegion(this, "leaders", members.toString().replace("[", "").replace("]", ""));
    }
    
    public void setAdmins(List<String> admins) {
    	setToSave(true);
        this.admins = admins;
        RedProtect.rm.updateLiveRegion(this, "admins", admins.toString().replace("[", "").replace("]", ""));
    }
    
    public void setMembers(List<String> members) {
    	setToSave(true);
        this.members = members;
        RedProtect.rm.updateLiveRegion(this, "members", members.toString().replace("[", "").replace("]", ""));
    }
    
    public int[] getX() {
        return this.x;
    }
    
    public int[] getZ() {
        return this.z;
    }
    
    public String getName() {
        return this.name;
    }
    
    /**
	 * Use this method to get raw admins. This will return UUID if server running in Online mode. Will return player name in lowercase if Offline mode.
	 * 
	 * To check if a player can build on this region use {@code canBuild(p)} instead this method.
	 * @return {@code List<String>}
	 */  
    @Deprecated()
    public List<String> getAdmins() {
        return this.admins;
    }
    
    /**
	 * Use this method to get raw members. This will return UUID if server running in Online mode. Will return player name in lowercase if Offline mode.
	 * 
	 * To check if a player can build on this region use {@code canBuild(Player p)} instead this method.
	 * @return {@code List<String>}
	 */  
    @Deprecated
    public List<String> getMembers() {
        return this.members;
    }
    
    /**
	 * Use this method to get raw leaders. This will return UUID if server running in Online mode. Will return player name in lowercase if Offline mode.
	 * 
	 * To check if a player can build on this region use {@code canBuild(Player p)} instead this method.
	 * @return {@code List<String>}
	 */  
    @Deprecated
    public List<String> getLeaders() {
        return this.leaders;
    }
    
    public int getCenterX() {
        return (this.minMbrX + this.maxMbrX) / 2;
    }
    
    public int getCenterZ() {
        return (this.minMbrZ + this.maxMbrZ) / 2;
    }
    
    public int getCenterY() {
        return (this.minY + this.maxY) / 2;
    }
    
    public int getMaxMbrX() {
        return this.maxMbrX;
    }
    
    public int getMinMbrX() {
        return this.minMbrX;
    }
    
    public int getMaxMbrZ() {
        return this.maxMbrZ;
    }
    
    public int getMinMbrZ() {
        return this.minMbrZ;
    }
    
	public String info() {
		String leadersstring = "";
        String adminstring = "";
        String memberstring = "";
        String wMsgTemp = "";
        String IsTops = RPLang.translBool(isOnTop());
        String today = this.date;
        String wName = this.world;
        String colorChar = "";
        
        if (RPConfig.getString("region-settings.world-colors." + this.world) != null){
        	char c = '&';
        	colorChar = ChatColor.translateAlternateColorCodes(c, RPConfig.getString("region-settings.world-colors." + this.world));
        }
        
        for (int i = 0; i < this.leaders.size(); ++i) {
        	if (this.leaders.get(i) == null){
        		this.leaders.remove(i);
        	}
        	leadersstring = leadersstring + ", " +  RPUtil.UUIDtoPlayer(this.leaders.get(i));           	
        }
        for (int i = 0; i < this.admins.size(); ++i) {
        	if (this.admins.get(i) == null){
        		this.admins.remove(i);
        	}
        	adminstring = adminstring + ", " +  RPUtil.UUIDtoPlayer(this.admins.get(i));
        }        
        for (int i = 0; i < this.members.size(); ++i) {
        	if (this.members.get(i) == null){
        		this.members.remove(i);
        	}
        	memberstring = memberstring + ", " +  RPUtil.UUIDtoPlayer(this.members.get(i));
        }  
        if (this.leaders.size() > 0) {
        	leadersstring = leadersstring.substring(2);
        }
        else {
        	leadersstring = "None";
        }
        if (this.admins.size() > 0) {
        	adminstring = adminstring.substring(2);
        }
        else {
        	adminstring = "None";
        }
        if (this.members.size() > 0) {
            memberstring = memberstring.substring(2);
        }
        else {
            memberstring = "None";
        }               
        if (this.wMessage == null || this.wMessage.equals("")){
        	wMsgTemp = RPLang.get("region.welcome.notset");
        } else {
        	wMsgTemp = wMessage;
        }
         
        if (this.date.equals(RPUtil.DateNow())){        	
        	today = RPLang.get("region.today");
        } else {
        	today = this.date;
        }
        for (String pname:this.leaders){
        	Player play = RedProtect.serv.getPlayer(pname);
            if (RedProtect.OnlineMode && pname != null && !pname.equalsIgnoreCase(RPConfig.getString("region-settings.default-leader"))){
            	play = RedProtect.serv.getPlayer(UUID.fromString(RPUtil.PlayerToUUID(pname)));
        	}            
        	if (pname != null && play != null && play.isOnline()){
        		today = ChatColor.GREEN + "Online!";
        		break;
        	}        	
        } 
        for (String pname:this.admins){
        	Player play = RedProtect.serv.getPlayer(pname);
            if (RedProtect.OnlineMode && pname != null && !pname.equalsIgnoreCase(RPConfig.getString("region-settings.default-leader"))){
            	play = RedProtect.serv.getPlayer(UUID.fromString(RPUtil.PlayerToUUID(pname)));
        	}            
        	if (pname != null && play != null && play.isOnline()){
        		today = ChatColor.GREEN + "Online!";
        		break;
        	}        	
        }
        
        return RPLang.get("region.name") + " " + colorChar+this.name + RPLang.get("general.color") + " | " + RPLang.get("region.priority") + " " + this.prior + "\n" +      
         RPLang.get("region.priority.top") + " "  + IsTops  + RPLang.get("general.color") + " | " + RPLang.get("region.lastvalue") + RPEconomy.getFormatted(this.value) + "\n" +
        RPLang.get("region.world") + " " + colorChar+wName + RPLang.get("general.color") + " | " + RPLang.get("region.center") + " " + this.getCenterX() + ", " + this.getCenterZ() + "\n" +
        RPLang.get("region.ysize") + " " + this.minY + " - " + this.maxY + RPLang.get("general.color") + " | "+ RPLang.get("region.area") + " " + this.getArea() + "\n" +
        RPLang.get("region.leaders") + " " + leadersstring + "\n" +
        RPLang.get("region.admins") + " " + adminstring + RPLang.get("general.color") + " | " + RPLang.get("region.members") + " " + memberstring + "\n" +
        RPLang.get("region.date") + " " + today + "\n" +
        RPLang.get("region.welcome.msg") + " " + (wMsgTemp.equals("hide ")? RPLang.get("region.hiding") : ChatColor.translateAlternateColorCodes('&', wMsgTemp));
        
    }
    
	/**
	 * Represents the region created by player.
     * @param name Name of region.
     * @param admins List of admins.
     * @param members List of members.
     * @param leaders List of leaders.
     * @param minLoc Min coord.
     * @param maxLoc Max coord.
     * @param flags Flag names and values.
     * @param wMessage Welcome message.
     * @param prior Priority of region.
     * @param worldName Name of world for this region.
     * @param date Date of latest visit of an admin or leader.
     * @param value Last value of this region.
     */
    public Region(String name, List<String> admins, List<String> members, List<String> leaders, Location minLoc, Location maxLoc, HashMap<String,Object> flags, String wMessage, int prior, String worldName, String date, long value, Location tppoint) {
    	super();        
        this.maxMbrX = maxLoc.getBlockX();
        this.minMbrX = minLoc.getBlockX();
        this.maxMbrZ = maxLoc.getBlockZ();
        this.minMbrZ = minLoc.getBlockZ();
        this.maxY = maxLoc.getBlockY();
        this.minY = minLoc.getBlockY();
        this.x = new int[] {minMbrX,minMbrX,maxMbrX,maxMbrX};
        this.z = new int[] {minMbrZ,minMbrZ,maxMbrZ,maxMbrZ};
        this.name = name;
        this.admins = admins;
        this.members = members;
        this.leaders = leaders;    
        this.flags = flags;
        this.value = value;
        this.tppoint = tppoint;
        
        if (worldName != null){
            this.world = worldName;
        } else {
        	this.world = "";
        }
        
        if (wMessage != null){
            this.wMessage = wMessage;
        } else {
        	this.wMessage = "";
        }
        
        if (date != null){
            this.date = date;
        } else {
        	this.date = RPUtil.DateNow();
        }
    }
    
    /**
	 * Represents the region created by player.
     * @param name Name of region.
     * @param admins List of admins.
     * @param members List of members.
     * @param leaders List of leaders.
     * @param maxMbrX Max coord X
     * @param minMbrX Min coord X
     * @param maxMbrZ Max coord Z
     * @param minMbrZ Min coord Z
     * @param flags Flag names and values.
     * @param wMessage Welcome message.
     * @param prior Priority of region.
     * @param worldName Name of world for this region.
     * @param date Date of latest visit of an admin or leader.
     * @param value Last value of this region.
     */
    public Region(String name, List<String> admins, List<String> members, List<String> leaders, int maxMbrX, int minMbrX, int maxMbrZ, int minMbrZ, int minY, int maxY, HashMap<String,Object> flags, String wMessage, int prior, String worldName, String date, long value, Location tppoint) {
    	super();
        this.x = new int[] {minMbrX,minMbrX,maxMbrX,maxMbrX};
        this.z = new int[] {minMbrZ,minMbrZ,maxMbrZ,maxMbrZ};
        this.maxMbrX = maxMbrX;
        this.minMbrX = minMbrX;
        this.maxMbrZ = maxMbrZ;
        this.minMbrZ = minMbrZ;
        this.maxY = maxY;
        this.minY = minY;
        this.name = name;
        this.admins = admins;
        this.members = members;
        this.leaders = leaders;    
        this.flags = flags;
        this.value = value;
        this.tppoint = tppoint;
        
        if (worldName != null){
            this.world = worldName;
        } else {
        	this.world = "";
        }
        
        if (wMessage != null){
            this.wMessage = wMessage;
        } else {
        	this.wMessage = "";
        }
        
        if (date != null){
            this.date = date;
        } else {
        	this.date = RPUtil.DateNow();
        }
    }
    
    /**
     * Represents the region created by player.
	 * @param name Region name.
	 * @param admins Admins names/uuids.
	 * @param members Members names/uuids.
	 * @param leaders Leaders name/uuid.
	 * @param x Locations of x coords.
	 * @param z Locations of z coords.
	 * @param miny Min coord y of this region.
	 * @param maxy Max coord y of this region.
	 * @param prior Location of x coords.
     * @param worldName Name of world region.
     * @param date Date of latest visit of an admins or leader.
     * @param welcome Set a welcome message.
     * @param value A value in server economy.
     */
    public Region(String name, List<String> admins, List<String> members, List<String> leaders, int[] x, int[] z, int miny, int maxy, int prior, String worldName, String date, Map<String, Object> flags, String welcome, long value, Location tppoint) {
    	super();
        this.prior = prior;
        this.world = worldName;
        this.date = date;
        this.flags = flags;
        this.wMessage = welcome;
        int size = x.length;
        this.value = value;
        this.tppoint = tppoint;
          	    
        if (size != z.length) {
            throw new Error(RPLang.get("region.xy"));
        }
        this.x = x;
        this.z = z;
        if (size < 4) {
            throw new Error(RPLang.get("region.polygon"));
        }
        if (size == 4) {
            this.x = null;
            this.z = null;
        }
        this.admins = admins;
        this.members = members;
        this.name = name;
        this.leaders = leaders;
        this.maxMbrX = x[0];
        this.minMbrX = x[0];
        this.maxMbrZ = z[0];
        this.minMbrZ = z[0];
        this.maxY = maxy;
        this.minY = miny;
        for (int i = 0; i < x.length; ++i) {
            if (x[i] > this.maxMbrX) {
                this.maxMbrX = x[i];
            }
            if (x[i] < this.minMbrX) {
                this.minMbrX = x[i];
            }
            if (z[i] > this.maxMbrZ) {
                this.maxMbrZ = z[i];
            }
            if (z[i] < this.minMbrZ) {
                this.minMbrZ = z[i];
            }
        }
    }

    public void clearLeaders(){
    	setToSave(true);
    	this.leaders.clear();
    	RedProtect.rm.updateLiveRegion(this, "leaders", "");
    }
    
    public void clearAdmins(){
    	setToSave(true);
    	this.admins.clear();
    	RedProtect.rm.updateLiveRegion(this, "admins", "");
    }
    
    public void clearMembers(){
    	setToSave(true);
    	this.members.clear();
    	RedProtect.rm.updateLiveRegion(this, "members", "");
    }
    
    public void delete() {
        RedProtect.rm.remove(this);
    }
    
    public int getArea() {
    	return Math.abs(this.maxMbrX - this.minMbrX) * Math.abs(this.maxMbrZ - this.minMbrZ);
    	/*
        if (this.x == null) {
            return (this.maxMbrX - this.minMbrX) * (this.maxMbrZ - this.minMbrZ);
        }
        int area = 0;
        for (int i = 0; i < this.x.length; ++i) {
            int j = (i + 1) % this.x.length;
            area += this.x[i] * this.z[j] - this.z[i] * this.x[j];
        }
        area = Math.abs(area / 2);
        return area;
        */
    }
    
    /*
    public boolean inBoundingRect(int bx, int bz) {
        return bx <= this.maxMbrX && bx >= this.minMbrX && bz <= this.maxMbrZ && bz >= this.minMbrZ;
    }
    */
    
    public boolean inBoundingRect(Region other) {
        return other.maxMbrX >= this.minMbrX && other.minMbrZ >= this.minMbrZ && other.minMbrX <= this.maxMbrX && other.minMbrZ <= this.maxMbrZ;
    }
     
	public boolean isLeader(Player player) {
        return this.leaders.contains(RPUtil.PlayerToUUID(player.getName()));
    }
	
	public boolean isLeader(String player) {
        return this.leaders.contains(RPUtil.PlayerToUUID(player));
    }
	
	public boolean isAdmin(Player player) {
        return this.admins.contains(RPUtil.PlayerToUUID(player.getName()));
    }
	
	public boolean isAdmin(String player) {
        return this.admins.contains(RPUtil.PlayerToUUID(player));
    }
    
	public boolean isMember(Player player) {
        return this.members.contains(RPUtil.PlayerToUUID(player.getName()));
    }
	
	public boolean isMember(String player) {
        return this.members.contains(RPUtil.PlayerToUUID(player));
    }
    
	/** Add an leader to the Region. The string need to be UUID if Online Mode, or Player Name if Offline Mode.
     * @param uuid - UUID or Player Name.
     */
    public void addLeader(String uuid) {
    	setToSave(true);
    	String pinfo = uuid;
    	if (!RedProtect.OnlineMode){
    		pinfo = uuid.toLowerCase();
    	}
        if (this.members.contains(pinfo)) {
            this.members.remove(pinfo);
        }
        if (this.admins.contains(pinfo)) {
            this.admins.remove(pinfo);            
        }
        if (!this.leaders.contains(pinfo)) {
            this.leaders.add(pinfo);            
        }
        RedProtect.rm.updateLiveRegion(this, "leaders", this.members.toString().replace("[", "").replace("]", ""));
        RedProtect.rm.updateLiveRegion(this, "admins", this.admins.toString().replace("[", "").replace("]", ""));
        RedProtect.rm.updateLiveRegion(this, "members", this.members.toString().replace("[", "").replace("]", ""));
    }
    
	/** Add a member to the Region. The string need to be UUID if Online Mode, or Player Name if Offline Mode.
     * @param uuid - UUID or Player Name.
     */
    public void addMember(String uuid) {
    	setToSave(true);
    	String pinfo = uuid;
    	if (!RedProtect.OnlineMode){
    		pinfo = uuid.toLowerCase();
    	}
    	if (this.admins.contains(pinfo)) {
            this.admins.remove(pinfo);
        }
        if (this.leaders.contains(pinfo)) {
            this.leaders.remove(pinfo);
        }
        if (!this.members.contains(pinfo)) {
            this.members.add(pinfo);            
        }
        RedProtect.rm.updateLiveRegion(this, "leaders", this.members.toString().replace("[", "").replace("]", ""));
        RedProtect.rm.updateLiveRegion(this, "admins", this.admins.toString().replace("[", "").replace("]", ""));
        RedProtect.rm.updateLiveRegion(this, "members", this.members.toString().replace("[", "").replace("]", ""));
    }
    
    /** Add an admin to the Region. The string need to be UUID if Online Mode, or Player Name if Offline Mode.
     * @param uuid - UUID or Player Name.
     */
    public void addAdmin(String uuid) {
    	setToSave(true);
    	String pinfo = uuid;
    	if (!RedProtect.OnlineMode){
    		pinfo = uuid.toLowerCase();
    	}
        if (this.members.contains(pinfo)) {
            this.members.remove(pinfo);
        }
        if (this.leaders.contains(pinfo)) {
            this.leaders.remove(pinfo);
        }
        if (!this.admins.contains(pinfo)) {
            this.admins.add(pinfo);            
        }
        RedProtect.rm.updateLiveRegion(this, "leaders", this.members.toString().replace("[", "").replace("]", ""));
        RedProtect.rm.updateLiveRegion(this, "admins", this.admins.toString().replace("[", "").replace("]", ""));
        RedProtect.rm.updateLiveRegion(this, "members", this.members.toString().replace("[", "").replace("]", ""));
    }
    
    /** Remove an member to the Region. The string need to be UUID if Online Mode, or Player Name if Offline Mode.
     * @param uuid - UUID or Player Name.
     */
    public void removeMember(String uuid) {
    	setToSave(true);
    	String pinfo = uuid;
    	if (!RedProtect.OnlineMode){
    		pinfo = uuid.toLowerCase();
    	}
        if (this.members.contains(pinfo)) {
            this.members.remove(pinfo);
        }
        if (this.admins.contains(pinfo)) {
            this.admins.remove(pinfo);
        }
        if (this.leaders.contains(pinfo)) {
            this.leaders.remove(pinfo);
        }
        RedProtect.rm.updateLiveRegion(this, "admins", this.admins.toString().replace("[", "").replace("]", ""));
        RedProtect.rm.updateLiveRegion(this, "members", this.members.toString().replace("[", "").replace("]", ""));
        RedProtect.rm.updateLiveRegion(this, "leaders", this.members.toString().replace("[", "").replace("]", ""));
    }
    
    /** Remove an admin to the Region. The string need to be UUID if Online Mode, or Player Name if Offline Mode.
     * @param uuid - UUID or Player Name.
     */
    public void removeAdmin(String uuid) {
    	setToSave(true);
    	String pinfo = uuid;
    	if (!RedProtect.OnlineMode){
    		pinfo = uuid.toLowerCase();
    	}
    	if (this.leaders.contains(pinfo)) {
            this.leaders.remove(pinfo);
        }
        if (this.admins.contains(pinfo)) {
            this.admins.remove(pinfo);
        }
        if (!this.members.contains(pinfo)) {
            this.members.add(pinfo);
        }
        RedProtect.rm.updateLiveRegion(this, "admins", this.admins.toString().replace("[", "").replace("]", ""));
    }
    
    /** Remove an leader to the Region. The string need to be UUID if Online Mode, or Player Name if Offline Mode.
     * @param uuid - UUID or Player Name.
     */
    public void removeLeader(String uuid) {
    	setToSave(true);
    	String pinfo = uuid;
    	if (!RedProtect.OnlineMode){
    		pinfo = uuid.toLowerCase();
    	}
    	if (this.members.contains(pinfo)) {
            this.members.remove(pinfo);
        }
        if (this.leaders.contains(pinfo)) {
            this.leaders.remove(pinfo);
        }
        if (!this.admins.contains(pinfo)) {
            this.admins.add(pinfo);
        }
        RedProtect.rm.updateLiveRegion(this, "admins", this.admins.toString().replace("[", "").replace("]", ""));
    }
    
    public boolean getFlagBool(String key) {
    	if (!flagExists(key) || !RPConfig.isFlagEnabled(key)){
    		if (RPConfig.getDefFlagsValues().get(key) != null){
    			return (Boolean) RPConfig.getDefFlagsValues().get(key);
    		} else {
    			return RPConfig.getBool("flags."+key);
    		}  		
    	}
        return this.flags.get(key) instanceof Boolean && (Boolean)this.flags.get(key);
    }
    
    public String getFlagString(String key) {
    	if (!flagExists(key) || !RPConfig.isFlagEnabled(key)){
    		if (RPConfig.getDefFlagsValues().get(key) != null){
    			return (String) RPConfig.getDefFlagsValues().get(key);
    		} else {
    			return RPConfig.getString("flags."+key);
    		}
    	}
        return this.flags.get(key).toString();
    }
    
    public boolean canBuild(Player p) {
    	if (flagExists("for-sale") && !RedProtect.ph.hasPerm(p, "redprotect.bypass")){
    		return false;
    	}
        return checkAllowedPlayer(p);
    }
    
    public int adminSize() {
        return this.admins.size();
    }
    
    public int leaderSize() {
        return this.leaders.size();
    }
    
    public String getFlagInfo() {
    	String flaginfo = "";
    	for (String flag:this.flags.keySet()){    		
    		if (RPConfig.getDefFlags().contains(flag)){
    			String flagValue = this.flags.get(flag).toString();
    			if (flagValue.equalsIgnoreCase("true") || flagValue.equalsIgnoreCase("false")){
    				flaginfo = flaginfo + ", " + ChatColor.AQUA + flag + ": " + RPLang.translBool(flagValue);
    			} else {
    				flaginfo = flaginfo + ", " + ChatColor.AQUA + flag + ": "  + ChatColor.GRAY + flagValue;
    			}    			
    		} 
    		
    		if (flaginfo.contains(flag)){
				continue;
			}
    		
    		if (RPConfig.AdminFlags.contains(flag)){    			
    			String flagValue = this.flags.get(flag).toString();
    			if (flagValue.equalsIgnoreCase("true") || flagValue.equalsIgnoreCase("false")){
    				flaginfo = flaginfo + ", " + ChatColor.AQUA + flag + ": " + RPLang.translBool(flagValue);
    			} else {
    				flaginfo = flaginfo + ", " + ChatColor.AQUA + flag + ": " + ChatColor.GRAY + flagValue;
    			} 
    		} 
    	}    	
    	if (this.flags.keySet().size() > 0) {
    		flaginfo = flaginfo.substring(2);
        }
        else {
        	flaginfo = "Default";
        }
        return flaginfo;
    }
        
    public boolean isOnTop(){
    	Region newr = RedProtect.rm.getTopRegion(RedProtect.serv.getWorld(this.getWorld()), this.getCenterX(), this.getCenterY(), this.getCenterZ());
		return newr == null || newr.equals(this);    	
    }
    
    public boolean flagExists(String key){
    	return flags.containsKey(key);
    }	
	
	//---------------------- Admin Flags --------------------------// 
    public boolean canPlayerDamage() {
    	if (!flagExists("player-damage")){
    		return true;
    	}
		return getFlagBool("player-damage");
	}
    
    public boolean forcePVP() {
    	if (!flagExists("forcepvp")){
    		return false;
    	}
		return getFlagBool("forcepvp");
	}
    
    public boolean canHunger() {
    	if (!flagExists("can-hunger")){
    		return true;
    	}
		return getFlagBool("can-hunger");
	}
    
    public boolean canSign(Player p) {
		if (!flagExists("sign")){
    		return checkAllowedPlayer(p);
    	}		
        return getFlagBool("sign") || checkAllowedPlayer(p);
	}
    
	public boolean canEnter(Player p) {
		if (!flagExists("enter")){
    		return true;
    	}
        return getFlagBool("enter") || RedProtect.ph.hasPerm(p, "redprotect.region-enter."+this.name) || checkAllowedPlayer(p);
	}
	
	public boolean canEnterWithItens(Player p) {
		if (!flagExists("allow-enter-items")){
    		return true;
    	}		
		
		if (checkAllowedPlayer(p)){
			return true;
		}
		
		String[] items = flags.get("allow-enter-items").toString().replace(" ", "").split(",");
		List<String> inv = new ArrayList<String>();
		List<String> mats = new ArrayList<String>();
		for (ItemStack slot:p.getInventory()){
			if (slot == null || slot.getType().equals(Material.AIR)){
				continue;
			}
			if (!inv.contains(slot.getType().name())){
				inv.add(slot.getType().name());
			}
			
		}		
		for (String item:items){
			if (!mats.contains(item)){
				mats.add(item.toUpperCase());
			}
			
		}				
		if (!(mats.containsAll(inv) && inv.containsAll(mats))){
			return false;
		}
        return true;
	}
	
	public boolean denyEnterWithItens(Player p) {
		if (!flagExists("deny-enter-items")){
    		return true;
    	}		
		if (checkAllowedPlayer(p)){
			return true;
		}
				
		for (ItemStack slot:p.getInventory().getContents()){
			if (slot == null){
				continue;
			}
					
			String SlotType = slot.getType().name();
			if (SlotType.equalsIgnoreCase("AIR")){
				continue;
			}
			
			String[] items = flags.get("deny-enter-items").toString().replace(" ", "").split(",");
			for (String comp:items){
				if (SlotType.equalsIgnoreCase(comp)){
					return false;
				}
			}
		}
        return true;
	}
	
	public boolean canEnderPearl(Player p) {
		if (!flagExists("enderpearl")){
    		return checkAllowedPlayer(p);
    	}
        return getFlagBool("enderpearl") || checkAllowedPlayer(p);
	}
	
    
	public boolean canMining(Block b) {
    	if (!flagExists("minefarm")){
    		return false;
    	}
		if (b.getType().toString().contains("_ORE") ||
				b.getType().equals(Material.STONE) || 
				b.getType().equals(Material.GRASS)||
				b.getType().equals(Material.DIRT)){
			return getFlagBool("minefarm");
		}
		return false;
	}
	
	public boolean canPlace(Block b) {
    	if (!flagExists("allow-place")){
    		return false;
    	}
    	String[] blocks = getFlagString("allow-place").replace(" ", "").split(",");
		for (String block:blocks){
			if (block.toUpperCase().equals(b.getType().name())){
				return true;
			}
		}
		return false;
	}
	
	public boolean canBreak(Block b) {
    	if (!flagExists("allow-break")){
    		return false;
    	}
    	String[] blocks = getFlagString("allow-break").replace(" ", "").split(",");
		for (String block:blocks){
			if (block.toUpperCase().equals(b.getType().name())){
				return true;
			}
		}
		return false;
	}

	public boolean canTree(Block b) {
		if (!flagExists("treefarm")){
    		return false;
    	}
		if (b.getType().toString().contains("LOG") || b.getType().toString().contains("LEAVES")){
			return getFlagBool("treefarm");
		}
		return false;
	}
	
	public boolean canSkill(Player p) {
		if (!flagExists("up-skills")){
    		return true;
    	}
        return getFlagBool("up-skills") || checkAllowedPlayer(p);
	}

	public boolean canBack(Player p) {
		if (!flagExists("can-back")){
    		return true;
    	}
        return getFlagBool("can-back") || checkAllowedPlayer(p);
	}
	
	public boolean isForSale() {
		if (!flagExists("for-sale")){
			return false;
		}
		return getFlagBool("for-sale");
	}
	
	public boolean isPvPArena() {
		if (!flagExists("pvparena")){
			return false;
		}
		return getFlagBool("pvparena");
	}
	
	public boolean allowMod(Player p) {
		if (!flagExists("allow-mod")){
			return checkAllowedPlayer(p);
		}		
		return getFlagBool("allow-mod") || checkAllowedPlayer(p);
	}
		
	public boolean canEnterPortal(Player p) {
		if (!flagExists("portal-enter")){
			return true;
		}
		return getFlagBool("portal-enter") || checkAllowedPlayer(p);
	}
	
	public boolean canExitPortal(Player p) {
		if (!flagExists("portal-exit")){
			return true;
		}
		return getFlagBool("portal-exit") || checkAllowedPlayer(p);
	}
	
	public boolean canPet(Player p) {
		if (!flagExists("can-pet")){
			return true;
		}
		return getFlagBool("can-pet") || checkAllowedPlayer(p);
	}
	
	public boolean canProtectiles(Player p) {
		if (!flagExists("can-projectiles")){
			return true;
		}
		return getFlagBool("can-projectiles") || checkAllowedPlayer(p);
	}
	
	public boolean canCreatePortal() {
		if (!flagExists("allow-create-portal")){
			return true;
		}
		return getFlagBool("allow-create-portal");
	}
	
	public boolean AllowCommands(Player p, String Command) {
		if (!flagExists("allow-cmds")){
			return true;
		}
		
		Command = Command.replace("/", "");
		//As Whitelist
		String[] cmds = flags.get("allow-cmds").toString().replace(" ", "").split(",");
		for (String cmd:cmds){
			if (cmd.replace("/", "").equalsIgnoreCase(Command)){
				return true;
			}
		}
		return false;
	}
	
	public boolean DenyCommands(Player p, String Command) {
		if (!flagExists("deny-cmds")){
			return true;
		}
		
		Command = Command.replace("/", "");
		//As BlackList
		String[] cmds = flags.get("deny-cmds").toString().replace(" ", "").split(",");
		for (String cmd:cmds){
			if (cmd.replace("/", "").equalsIgnoreCase(Command)){
				return false;
			}
		}		
		return true;
	}
	
	
	//---------------------- Player Flags --------------------------//
	public boolean FlowDamage() {
		if (!RPConfig.isFlagEnabled("flow-damage")){
    		return RPConfig.getBool("flags.flow-damage");
    	}
		return getFlagBool("flow-damage");
	}
	
	public boolean canMobLoot() {
    	if (!RPConfig.isFlagEnabled("mob-loot")){
    		return RPConfig.getBool("flags.mob-loot");
    	}
        return getFlagBool("mob-loot");
    }
	
	public boolean allowPotions(Player p) {
    	if (!RPConfig.isFlagEnabled("allow-potions")){
    		return RPConfig.getBool("flags.allow-potions");
    	}    	
        return getFlagBool("allow-potions");
    }
    
    public boolean canPVP(Player p) {
    	if (!RPConfig.isFlagEnabled("pvp")){
    		return RPConfig.getBool("flags.pvp") || RedProtect.ph.hasPerm(p, "redprotect.bypass");
    	}
        return getFlagBool("pvp") || RedProtect.ph.hasPerm(p, "redprotect.bypass");
    }
    
    public boolean canChest(Player p) {
    	if (!RPConfig.isFlagEnabled("chest")){
    		return RPConfig.getBool("flags.chest") || checkAllowedPlayer(p);
    	}
        return getFlagBool("chest") || checkAllowedPlayer(p);
    }
    
    public boolean canLever(Player p) {
    	if (!RPConfig.isFlagEnabled("lever")){
    		return RPConfig.getBool("flags.lever") || checkAllowedPlayer(p);
    	}
        return getFlagBool("lever") || checkAllowedPlayer(p);
    }
    
    public boolean canButton(Player p) {
    	if (!RPConfig.isFlagEnabled("button")){
    		return RPConfig.getBool("flags.button") || checkAllowedPlayer(p);
    	}
        return getFlagBool("button") || checkAllowedPlayer(p);
    }
    
    public boolean canDoor(Player p) {
    	if (!RPConfig.isFlagEnabled("door")){
    		return RPConfig.getBool("flags.door") || checkAllowedPlayer(p);
    	}
        return getFlagBool("door") || checkAllowedPlayer(p);
    }
    
    public boolean canSpawnMonsters() {
    	if (!RPConfig.isFlagEnabled("spawn-monsters")){
    		return RPConfig.getBool("flags.spawn-monsters");
    	}
        return getFlagBool("spawn-monsters");
    }
        
    public boolean canSpawnPassives() {
    	if (!RPConfig.isFlagEnabled("spawn-animals")){
    		return RPConfig.getBool("flags.spawn-animals");
    	}
        return getFlagBool("spawn-animals");
    }
    
	public boolean canMinecart(Player p) {
		if (!RPConfig.isFlagEnabled("minecart")){
    		return RPConfig.getBool("flags.minecart") || checkAllowedPlayer(p);
    	}
        return getFlagBool("minecart") || checkAllowedPlayer(p);
	}
	
	public boolean canInteractPassives(Player p) {
    	if (!RPConfig.isFlagEnabled("passives")){
    		return RPConfig.getBool("flags.passives") || checkAllowedPlayer(p);
    	}
        return getFlagBool("passives") || checkAllowedPlayer(p);
    }
    
    public boolean canFlow() {
    	if (!RPConfig.isFlagEnabled("flow")){
    		return RPConfig.getBool("flags.flow");
    	}
        return getFlagBool("flow");
    }
    
    public boolean canFire() {
    	if (!RPConfig.isFlagEnabled("fire")){
    		return RPConfig.getBool("flags.fire");
    	}
        return getFlagBool("fire");
    }
    
    public boolean AllowHome(Player p) {
		if (!RPConfig.isFlagEnabled("allow-home")){
    		return RPConfig.getBool("flags.allow-home") || checkAllowedPlayer(p);
    	}
		return getFlagBool("allow-home") || checkAllowedPlayer(p);
	}
	//--------------------------------------------------------------//
	
	public long getValue() {	
		return this.value;
	}
	
	public void setValue(long value) {	
		setToSave(true);
		RedProtect.rm.updateLiveRegion(this, "value", String.valueOf(value));
		this.value = value;
	}
	    
	private boolean checkAllowedPlayer(Player p){
		return this.isLeader(p) || this.isAdmin(p) || this.isMember(p) || RedProtect.ph.hasPerm(p, "redprotect.bypass");
	}
	
	public List<Location> getLimitLocs(int locy){
		final List<Location> locBlocks = new ArrayList<Location>();
		Location loc1 = this.getMinLocation();
		Location loc2 = this.getMaxLocation();
		World w = Bukkit.getWorld(this.getWorld());
		
		for (int x = (int) loc1.getX(); x <= (int) loc2.getX(); ++x) {
            for (int z = (int) loc1.getZ(); z <= (int) loc2.getZ(); ++z) {
                //for (int y = (int) loc1.getY(); y <= (int) loc2.getY(); ++y) {
                    if (z == loc1.getZ() || z == loc2.getZ() ||
                        x == loc1.getX() || x == loc2.getX() ) {
                    	locBlocks.add(new Location(w,x,locy,z));                    	                   	
                    }
                //}
            }
        } 
		return locBlocks;
	}
	
	public List<Location> getLimitLocs(int miny, int maxy){
		final List<Location> locBlocks = new ArrayList<Location>();
		Location loc1 = this.getMinLocation();
		Location loc2 = this.getMaxLocation();
		World w = Bukkit.getWorld(this.getWorld());
		
		for (int x = (int) loc1.getX(); x <= (int) loc2.getX(); ++x) {
            for (int z = (int) loc1.getZ(); z <= (int) loc2.getZ(); ++z) {
                for (int y = (int) miny; y <= (int) maxy; ++y) {
                    if ((z == loc1.getZ() || z == loc2.getZ() ||
                        x == loc1.getX() || x == loc2.getX())
                        && new Location(w,x,y,z).getBlock().getType().name().contains(RPConfig.getString("region-settings.block-id"))) {
                    	locBlocks.add(new Location(w,x,y,z));                    	                   	
                    }
                }
            }
        } 
		return locBlocks;
	}
	
	public List<Location> get4Points(int y){
		List <Location> locs = new ArrayList<Location>();
		locs.add(this.getMinLocation());		
		locs.add(new Location(this.getMinLocation().getWorld(),this.minMbrX,y,this.minMbrZ+(this.maxMbrZ-this.minMbrZ)));
		locs.add(this.getMaxLocation());
		locs.add(new Location(this.getMinLocation().getWorld(),this.minMbrX+(this.maxMbrX-this.minMbrX),y,this.minMbrZ));
		return locs;		
	}

	public Location getCenterLoc() {		
		return new Location(Bukkit.getWorld(this.world), this.getCenterX(), this.getCenterY(), this.getCenterZ());
	}

	public String getAdminDesc() {
		if (this.admins.size() == 0){
			return "[none]";
		}
		StringBuilder adminsList = new StringBuilder();
		for (String admin:this.admins){
			adminsList.append(", "+admin);
		}
		return "["+adminsList.toString().substring(2)+"]";
	}
	
	public String getLeadersDesc() {
		if (this.leaders.size() == 0){
			return "[none]";
		}
		StringBuilder leaderList = new StringBuilder();
		for (String leader:this.leaders){
			leaderList.append(", "+leader);
		}
		return "["+leaderList.toString().substring(2)+"]";
	}
	
}
