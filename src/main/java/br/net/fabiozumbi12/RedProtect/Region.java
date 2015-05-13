package br.net.fabiozumbi12.RedProtect;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

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
    private int prior;
    private String name;
    private List<String> owners;
    private List<String> members;
    private String wMessage;
    private String creator;
    private String world;
    private String date;
    protected Map<String, Object> flags = new HashMap<String,Object>();
    protected boolean[] f = new boolean[10];
        
    public void setFlag(String Name, Object value) {
    	this.flags.put(Name, value);
    	RedProtect.rm.updateLiveFlags(this, Name, value.toString());
    }
    
    public void removeFlag(String Name) {
    	if (this.flags.containsKey(Name)){
    		this.flags.remove(Name); 
    		RedProtect.rm.removeLiveFlags(this, Name);
    	}    	               
    }
    
    public void setDate(String value) {
        this.date = value;
        RedProtect.rm.updateLiveRegion(this, "date", value);
    }    
    
    public String getDate() {
        return this.date;
    }
    
    public void setWorld(String w) {
        this.world = w;
        RedProtect.rm.updateLiveRegion(this, "world", w);
    }    
    
    public String getWorld() {
        return this.world;
    }
    
    public void setPrior(int prior) {
        this.prior = prior;
        RedProtect.rm.updateLiveRegion(this, "prior", ""+prior);
    }
    
    public int getPrior() {
        return this.prior;
    }
    
    public void setWelcome(String s){
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
        this.x = x;
    }
    
    public void setZ(int[] z) {
        this.z = z;
    }
    
    public void setOwners(List<String> owners) {
        this.owners = owners;
        RedProtect.rm.updateLiveRegion(this, "owners", owners.toString().replace("[", "").replace("]", ""));
    }
    
    public void setMembers(List<String> members) {
        this.members = members;
        RedProtect.rm.updateLiveRegion(this, "members", members.toString().replace("[", "").replace("]", ""));
    }
    
    public void setCreator(String uuid) {
     	if (!RedProtect.OnlineMode && uuid != null){
    		uuid = uuid.toLowerCase();
    	}
        this.creator = uuid;
        RedProtect.rm.updateLiveRegion(this, "creator", uuid);
    }
    
    public int[] getX() {
        return this.x;
    }
    
    public int[] getZ() {
        return this.z;
    }
    
    public String getCreator() {
        return this.creator;
    }
    
    public String getName() {
        return this.name;
    }
    
    public List<String> getOwners() {
        return this.owners;
    }
    
    public List<String> getMembers() {
        return this.members;
    }
    
    public int getCenterX() {
        return (this.minMbrX + this.maxMbrX) / 2;
    }
    
    public int getCenterZ() {
        return (this.minMbrZ + this.maxMbrZ) / 2;
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
    
    @SuppressWarnings("deprecation")
	public String info() {
        String ownerstring = "";
        String memberstring = "";
        String wMsgTemp = "";
        String IsTops = "";
        String today = this.date;
        String wName = this.world; 
        
        for (int i = 0; i < this.owners.size(); ++i) {
        	if (this.owners.get(i) == null){
        		this.owners.remove(i);
        	}
        	ownerstring = ownerstring + ", " +  RPUtil.UUIDtoPlayer(this.owners.get(i));   
        	
        }
        
        for (int i = 0; i < this.members.size(); ++i) {
        	if (this.members.get(i) == null){
        		this.members.remove(i);
        	}
        	memberstring = memberstring + ", " +  RPUtil.UUIDtoPlayer(this.members.get(i));   
        	
        }
        
        if (this.owners.size() > 0) {
            ownerstring = ownerstring.substring(2);
        }
        else {
            ownerstring = "None";
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
        if (isOnTop()){
        	IsTops = RPLang.get("region.yes");
        } else {
        	IsTops = RPLang.get("region.no");
        }  
        if (this.date.equals(RPUtil.DateNow())){        	
        	today = RPLang.get("region.today");
        } else {
        	today = this.date;
        }
        for (String pname:this.owners){
        	Player play = RedProtect.serv.getPlayer(pname);
            if (RedProtect.OnlineMode && pname != null){
            	play = RedProtect.serv.getPlayer(UUID.fromString(pname));
        	}            
        	if (pname != null && play != null && play.isOnline()){
        		today = ChatColor.GREEN + "Online!";
        		break;
        	}
        	
        } 
        for (String pname:this.members){        	
        	Player play = RedProtect.serv.getPlayer(pname);
            if (RedProtect.OnlineMode && pname != null){
            	play = RedProtect.serv.getPlayer(UUID.fromString(pname));
        	}             
        	if (pname != null && play != null && play.isOnline()){
        		today = ChatColor.GREEN + "Online!";
        		break;
        	}
        } 
        
        return RPLang.get("region.name") + " " + this.name + RPLang.get("general.color") + " | " + RPLang.get("region.creator") + " " + RPUtil.UUIDtoPlayer(this.creator) + "\n" +      
        RPLang.get("region.priority") + " " + this.prior + RPLang.get("general.color") + " | " + RPLang.get("region.priority.top") + " "  + IsTops + "\n" +
        RPLang.get("region.world") + " " + wName + RPLang.get("general.color") + " | " + RPLang.get("region.center") + " " + this.getCenterX() + ", " + this.getCenterZ() + "\n" +
        RPLang.get("region.owners") + " " + ownerstring + RPLang.get("general.color") + " | " + RPLang.get("region.members") + " " + memberstring + "\n" +
        RPLang.get("region.date") + " " + today + "\n" +
        RPLang.get("region.welcome.msg") + " " + (wMsgTemp.equals("hide ")? RPLang.get("region.hiding") : wMsgTemp.replaceAll("(?i)&([a-f0-9k-or])", "§$1"));
        
    }
    
    /**
	 * Represents the region created by player.
	 * @param x Locations of x coords.
	 * @param z Locations of z coords.
     * @param name Name of region.
     * @param owners List of owners.
     * @param members List of owners.
     * @param creator Name of creator.
     * @param maxMbrX Max coord X
     * @param minMbrX Min coord X
     * @param maxMbrZ Max coord Z
     * @param minMbrZ Min coord Z
     * @param flags Flag names and values.
     * @param wMessage Welcome message.
     * @param prior Priority of region.
     * @param worldName Name of world for this region.
     * @param date Date of latest visit of an owner or member.
     */
    public Region(String name, List<String> owners, List<String> members, String creator, int maxMbrX, int minMbrX, int maxMbrZ, int minMbrZ, HashMap<String,Object> flags, String wMessage, int prior, String worldName, String date) {
    	super();
    	this.minMbrX = 0;
        this.maxMbrX = 0;
        this.minMbrZ = 0;
        this.maxMbrZ = 0;
        this.creator = null;
        this.x = new int[] {minMbrX,minMbrX,maxMbrX,maxMbrX};
        this.z = new int[] {minMbrZ,minMbrZ,maxMbrZ,maxMbrZ};
        this.maxMbrX = maxMbrX;
        this.minMbrX = minMbrX;
        this.maxMbrZ = maxMbrZ;
        this.minMbrZ = minMbrZ;
        this.name = name;
        this.owners = owners;
        this.members = members;
        this.creator = creator;    
        this.flags = flags;
        
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
	 * @param owners Owners names.
	 * @param x Locations of x coords.
	 * @param z Locations of z coords.
	 * @param prior Location of x coords.
     * @param worldName Name of world region.
     * @param date Date of latest visit of an owner or member.
     * @param welcome 
     */
    public Region(String name, List<String> owners, List<String> members, String creator, int[] x, int[] z, int prior, String worldName, String date, Map<String, Object> flags, String welcome) {
    	super();
        this.minMbrX = 0;
        this.maxMbrX = 0;
        this.minMbrZ = 0;
        this.maxMbrZ = 0;
        this.creator = null;
        this.prior = prior;
        this.world = worldName;
        this.date = date;
        this.flags = flags;
        this.wMessage = welcome;
        int size = x.length;
          	    
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
        this.owners = owners;
        this.members = members;
        this.name = name;
        this.creator = creator;
        this.maxMbrX = x[0];
        this.minMbrX = x[0];
        this.maxMbrZ = z[0];
        this.minMbrZ = z[0];
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

    public void clearOwners(){
    	this.owners.clear();
    	RedProtect.rm.updateLiveRegion(this, "owners", "");
    }
    
    public void clearMembers(){
    	this.members.clear();
    	RedProtect.rm.updateLiveRegion(this, "members", "");
    }
    
    public void delete() {
        RedProtect.rm.remove(this);
    }
    
    public int getArea() {
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
    }
    
    public boolean inBoundingRect(int bx, int bz) {
        return bx <= this.maxMbrX && bx >= this.minMbrX && bz <= this.maxMbrZ && bz >= this.minMbrZ;
    }
    
    public boolean inBoundingRect(Region other) {
        return other.maxMbrX >= this.minMbrX && other.maxMbrZ >= this.minMbrZ && other.minMbrX <= this.maxMbrX && other.minMbrZ <= this.maxMbrZ;
    }
    
    public boolean intersects(int bx, int bz) {
        if (this.x == null) {
            return true;
        }
        boolean ret = false;
        int i = 0;
        int j = this.x.length - 1;
        while (i < this.x.length) {
            if (((this.z[i] <= bz && bz < this.z[j]) || (this.z[j] <= bz && bz < this.z[i])) && bx < (this.x[j] - this.x[i]) * (bz - this.z[i]) / (this.z[j] - this.z[i]) + this.x[i]) {
                ret = !ret;
            }
            j = i++;
        }
        return ret;
    }
    
	public boolean isOwner(String uuid) {
		String player = uuid;
    	if (!RedProtect.OnlineMode){
    		player = uuid.toLowerCase();
    	}
        return this.owners.contains(player);
    }
    
	public boolean isMember(String uuid) {
		String player = uuid;
    	if (!RedProtect.OnlineMode){
    		player = uuid.toLowerCase();
    	}
        return this.members.contains(player);
    }
    
    public void addMember(String uuid) {
    	String pinfo = uuid;
    	if (!RedProtect.OnlineMode){
    		pinfo = uuid.toLowerCase();
    	}
        if (!this.members.contains(pinfo) && !this.owners.contains(pinfo)) {
            this.members.add(pinfo);            
        }
        RedProtect.rm.updateLiveRegion(this, "members", this.members.toString().replace("[", "").replace("]", ""));
    }
    
    public void addOwner(String uuid) {
    	String pinfo = uuid;
    	if (!RedProtect.OnlineMode){
    		pinfo = uuid.toLowerCase();
    	}
        if (this.members.contains(pinfo)) {
            this.members.remove(pinfo);
        }
        if (!this.owners.contains(pinfo)) {
            this.owners.add(pinfo);            
        }
        RedProtect.rm.updateLiveRegion(this, "owners", this.owners.toString().replace("[", "").replace("]", ""));
        RedProtect.rm.updateLiveRegion(this, "members", this.members.toString().replace("[", "").replace("]", ""));
    }
    
    public void removeMember(String uuid) {
    	String pinfo = uuid;
    	if (!RedProtect.OnlineMode){
    		pinfo = uuid.toLowerCase();
    	}
        if (this.members.contains(pinfo)) {
            this.members.remove(pinfo);
        }
        if (this.owners.contains(pinfo)) {
            this.owners.remove(pinfo);
        }
        RedProtect.rm.updateLiveRegion(this, "owners", this.owners.toString().replace("[", "").replace("]", ""));
        RedProtect.rm.updateLiveRegion(this, "members", this.members.toString().replace("[", "").replace("]", ""));
    }
    
    public void removeOwner(String uuid) {
    	String pinfo = uuid;
    	if (!RedProtect.OnlineMode){
    		pinfo = uuid.toLowerCase();
    	}
        if (this.owners.contains(pinfo)) {
            this.owners.remove(pinfo);
        }
        RedProtect.rm.updateLiveRegion(this, "owners", this.owners.toString().replace("[", "").replace("]", ""));
    }
    
    public boolean getFlagBool(String key) {
    	if (!flagExists(key)){
    		return (boolean) RPConfig.getDefFlagsValues().get(key);
    	}
        return (boolean)this.flags.get(key);
    }
    
    public String getFlagString(String key) {
    	if (!flagExists(key)){
    		return (String) RPConfig.getDefFlagsValues().get(key);
    	}
        return (String)this.flags.get(key);
    }
    
    public boolean canBuild(Player p) {
    	String uuid = p.getUniqueId().toString();
    	if (!RedProtect.OnlineMode){
    		uuid = p.getName().toLowerCase();
    	}
        return p.getLocation().getY() < RPConfig.getInt("region-settings.height-start") || this.isOwner(uuid) || this.isMember(uuid) || RedProtect.ph.hasPerm(p, "redprotect.bypass");
    }
    
    public boolean canPVP(Player p) {
        return getFlagBool("pvp") || RedProtect.ph.hasPerm(p, "redprotect.bypass");
    }
    
    public boolean canChest(Player p) {
    	String uuid = p.getUniqueId().toString();
    	if (!RedProtect.OnlineMode){
    		uuid = p.getName().toLowerCase();
    	}
        return getFlagBool("chest") || this.isOwner(uuid) || this.isMember(uuid) || RedProtect.ph.hasPerm(p, "redprotect.bypass");
    }
    
    public boolean canLever(Player p) {
    	String uuid = p.getUniqueId().toString();
    	if (!RedProtect.OnlineMode){
    		uuid = p.getName().toLowerCase();
    	}
        return getFlagBool("lever") || this.isOwner(uuid) || this.isMember(uuid) || RedProtect.ph.hasPerm(p, "redprotect.bypass");
    }
    
    public boolean canButton(Player p) {
    	String uuid = p.getUniqueId().toString();
    	if (!RedProtect.OnlineMode){
    		uuid = p.getName().toLowerCase();
    	}
        return getFlagBool("button") || this.isOwner(uuid) || this.isMember(uuid) || RedProtect.ph.hasPerm(p, "redprotect.bypass");
    }
    
    public boolean canDoor(Player p) {
    	String uuid = p.getUniqueId().toString();
    	if (!RedProtect.OnlineMode){
    		uuid = p.getName().toLowerCase();
    	}
        return getFlagBool("door") || this.isOwner(uuid) || this.isMember(uuid) || RedProtect.ph.hasPerm(p, "redprotect.bypass");
    }
    
    public boolean canSpawnMonsters() {
        return getFlagBool("spawn-monsters");
    }
    
    public boolean canHurtPassives(Player p) {
    	String uuid = p.getUniqueId().toString();
    	if (!RedProtect.OnlineMode){
    		uuid = p.getName().toLowerCase();
    	}
        return getFlagBool("passives") || this.isOwner(uuid) || this.isMember(uuid) || RedProtect.ph.hasPerm(p, "redprotect.bypass");
    }
    
    public boolean canFlow() {
        return getFlagBool("flow");
    }
    
    public boolean canFire() {
        return getFlagBool("fire");
    }
    
    public boolean canSpawnPassives() {
        return getFlagBool("spawn-animals");
    }
    
    public int ownersSize() {
        return this.owners.size();
    }
    
    public String getFlagInfo() {
    	String flaginfo = "";
    	for (String flag:this.flags.keySet()){
    		flaginfo = flaginfo + ", "+ ChatColor.AQUA + flag + ":" + ChatColor.GRAY +String.valueOf(this.flags.get(flag));
    	}    	
    	if (this.flags.keySet().size() > 0) {
    		flaginfo = flaginfo.substring(2);
        }
        else {
        	flaginfo = "Default";
        }
        return flaginfo;
    }
    
    public void setName(String name) {
    	RedProtect.rm.updateLiveRegion(this, "name", name);
        this.name = name;        
    }
    
    public boolean isOnTop(){
    	Region newr = RedProtect.rm.getTopRegion(RedProtect.serv.getWorld(this.getWorld()), this.getCenterX(), this.getCenterZ());
		return newr == null || newr.equals(this);    	
    }
    
    public boolean flagExists(String key){
    	return flags.containsKey(key);
    }

	public boolean canSign(Player p) {
		String uuid = p.getUniqueId().toString();
    	if (!RedProtect.OnlineMode){
    		uuid = p.getName().toLowerCase();
    	}
		if (!flags.containsKey("sign")){
    		return this.isOwner(uuid) || this.isMember(uuid) || RedProtect.ph.hasPerm(p, "redprotect.bypass");
    	}		
        return getFlagBool("sign") || this.isOwner(uuid) || this.isMember(uuid) || RedProtect.ph.hasPerm(p, "redprotect.bypass");
	}
	
	public boolean canMinecart(Player p) {
		String uuid = p.getUniqueId().toString();
    	if (!RedProtect.OnlineMode){
    		uuid = p.getName().toLowerCase();
    	}
        return getFlagBool("minecart") || this.isOwner(uuid) || this.isMember(uuid) || RedProtect.ph.hasPerm(p, "redprotect.bypass");
	}
	
	public boolean canEnter(Player p) {
		String uuid = p.getUniqueId().toString();
    	if (!RedProtect.OnlineMode){
    		uuid = p.getName().toLowerCase();
    	}
		if (!flags.containsKey("enter")){
    		return true;
    	}
        return getFlagBool("enter") || this.isOwner(uuid) || this.isMember(uuid) || RedProtect.ph.hasPerm(p, "redprotect.bypass");
	}
	
	public boolean canEnderPearl(Player p) {
		String uuid = p.getUniqueId().toString();
    	if (!RedProtect.OnlineMode){
    		uuid = p.getName().toLowerCase();
    	}
		if (!flags.containsKey("enderpearl")){
    		return true;
    	}
        return getFlagBool("enderpearl") || this.isOwner(uuid) || this.isMember(uuid) || RedProtect.ph.hasPerm(p, "redprotect.bypass");
	}
	
    
	public boolean canMining(Block b) {
    	if (!flags.containsKey("minefarm")){
    		return false;
    	}
		if (b.getType().toString().contains("_ORE")){
			return getFlagBool("minefarm");
		}
		return false;
	}

	public boolean canTree(Block b) {
		if (!flags.containsKey("treefarm")){
    		return false;
    	}
		if (b.getType().toString().contains("LOG") || b.getType().toString().contains("LEAVES")){
			return getFlagBool("treefarm");
		}
		return false;
	}
	
	public boolean canMcMMo(Player p) {
		String uuid = p.getUniqueId().toString();
    	if (!RedProtect.OnlineMode){
    		uuid = p.getName().toLowerCase();
    	}
		if (!flags.containsKey("mcmmo")){
    		return true;
    	}
        return getFlagBool("mcmmo") || this.isOwner(uuid) || this.isMember(uuid) || RedProtect.ph.hasPerm(p, "redprotect.bypass");
	}

	public boolean canDeathBack(Player p) {
		String uuid = p.getUniqueId().toString();
    	if (!RedProtect.OnlineMode){
    		uuid = p.getName().toLowerCase();
    	}
		if (!flags.containsKey("death-back")){
    		return true;
    	}
        return getFlagBool("death-back") || this.isOwner(uuid) || this.isMember(uuid) || RedProtect.ph.hasPerm(p, "redprotect.bypass");
	}

	public boolean AllowHome(Player p) {
		String uuid = p.getUniqueId().toString();
    	if (!RedProtect.OnlineMode){
    		uuid = p.getName().toLowerCase();
    	}
		return getFlagBool("allow-home") || this.isOwner(uuid) || this.isMember(uuid) || RedProtect.ph.hasPerm(p, "redprotect.bypass");
	}
	    
}
