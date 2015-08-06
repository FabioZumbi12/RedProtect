package br.net.fabiozumbi12.RedProtect;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
	private Double value;
        
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
    
	public String info() {
        String ownerstring = "";
        String memberstring = "";
        String wMsgTemp = "";
        String IsTops = RPLang.translBool(isOnTop());
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
         
        if (this.date.equals(RPUtil.DateNow())){        	
        	today = RPLang.get("region.today");
        } else {
        	today = this.date;
        }
        for (String pname:this.owners){
        	Player play = RedProtect.serv.getPlayer(pname);
            if (RedProtect.OnlineMode && pname != null){
            	play = RedProtect.serv.getPlayer(UUID.fromString(RPUtil.PlayerToUUID(pname)));
        	}            
        	if (pname != null && play != null && play.isOnline()){
        		today = ChatColor.GREEN + "Online!";
        		break;
        	}        	
        } 
        for (String pname:this.members){        	
        	Player play = RedProtect.serv.getPlayer(pname);
            if (RedProtect.OnlineMode && pname != null){
            	play = RedProtect.serv.getPlayer(UUID.fromString(RPUtil.PlayerToUUID(pname)));
        	}             
        	if (pname != null && play != null && play.isOnline()){
        		today = ChatColor.GREEN + "Online!";
        		break;
        	}
        } 
        
        return RPLang.get("region.name") + " " + this.name + RPLang.get("general.color") + " | " + RPLang.get("region.creator") + " " + RPUtil.UUIDtoPlayer(this.creator) + "\n" +      
        RPLang.get("region.priority") + " " + this.prior + RPLang.get("general.color") + " | " + RPLang.get("region.priority.top") + " "  + IsTops  + RPLang.get("general.color") + " | " + RPLang.get("region.lastvalue") + RPConfig.getEcoString("economy-symbol") + this.value + "\n" +
        RPLang.get("region.world") + " " + wName + RPLang.get("general.color") + " | " + RPLang.get("region.center") + " " + this.getCenterX() + ", " + this.getCenterZ() + RPLang.get("general.color") +  " | " + RPLang.get("region.area") + " " + this.getArea() + "\n" +
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
     * @param value Last value of this region.
     */
    public Region(String name, List<String> owners, List<String> members, String creator, int maxMbrX, int minMbrX, int maxMbrZ, int minMbrZ, HashMap<String,Object> flags, String wMessage, int prior, String worldName, String date, Double value) {
    	super();
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
        this.value = value;
        
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
	 * @param owners Owners names/uuids.
	 * @param members Members names/uuids.
	 * @param creator Creator name/uuid.
	 * @param x Locations of x coords.
	 * @param z Locations of z coords.
	 * @param prior Location of x coords.
     * @param worldName Name of world region.
     * @param date Date of latest visit of an owner or member.
     * @param welcome Set a welcome message.
     * @param value A value in server economy.
     */
    public Region(String name, List<String> owners, List<String> members, String creator, int[] x, int[] z, int prior, String worldName, String date, Map<String, Object> flags, String welcome, Double value) {
    	super();
        this.prior = prior;
        this.world = worldName;
        this.date = date;
        this.flags = flags;
        this.wMessage = welcome;
        int size = x.length;
        this.value = value;
          	    
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
    
	public boolean isOwner(Player player) {
        return this.owners.contains(RPUtil.PlayerToUUID(player.getName()));
    }
    
	public boolean isMember(Player player) {
        return this.members.contains(RPUtil.PlayerToUUID(player.getName()));
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
    	if (!flagExists(key) || !RPConfig.isFlagEnabled(key)){
    		return (boolean) RPConfig.getDefFlagsValues().get(key);
    	}
        return this.flags.get(key) instanceof Boolean && (boolean)this.flags.get(key);
    }
    
    public String getFlagString(String key) {
    	if (!flagExists(key)){
    		return (String) RPConfig.getDefFlagsValues().get(key);
    	}
        return this.flags.get(key).toString();
    }
    
    public boolean canBuild(Player p) {
        return p.getLocation().getY() < RPConfig.getInt("region-settings.height-start") || checkAllowedPlayer(p);
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
    
    public boolean canHurtPassives(Player p) {
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
    
    public boolean canSpawnPassives() {
    	if (!RPConfig.isFlagEnabled("spawn-animals")){
    		return RPConfig.getBool("flags.spawn-animals");
    	}
        return getFlagBool("spawn-animals");
    }
    
	public boolean AllowHome(Player p) {
		if (!RPConfig.isFlagEnabled("allow-home")){
    		return RPConfig.getBool("flags.allow-home") || RedProtect.ph.hasPerm(p, "redprotect.bypass");
    	}
		return getFlagBool("allow-home") || checkAllowedPlayer(p);
	}
    
    public int ownersSize() {
        return this.owners.size();
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
    	Region newr = RedProtect.rm.getTopRegion(RedProtect.serv.getWorld(this.getWorld()), this.getCenterX(), this.getCenterZ());
		return newr == null || newr.equals(this);    	
    }
    
    public boolean flagExists(String key){
    	return flags.containsKey(key);
    }

	public boolean canSign(Player p) {
		if (!flags.containsKey("sign")){
    		return checkAllowedPlayer(p);
    	}		
        return getFlagBool("sign") || checkAllowedPlayer(p);
	}
	
	public boolean canMinecart(Player p) {
		if (!flags.containsKey("minecart")){
    		return checkAllowedPlayer(p);
    	}
        return getFlagBool("minecart") || checkAllowedPlayer(p);
	}
	
	public boolean canEnter(Player p) {
		if (!flags.containsKey("enter")){
    		return checkAllowedPlayer(p);
    	}
        return getFlagBool("enter") || RedProtect.ph.hasPerm(p, "redprotect.region-enter."+this.name) || checkAllowedPlayer(p);
	}
	
	public boolean canEnterWithItens(Player p) {
		if (!flags.containsKey("allow-enter-items")){
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
		if (!flags.containsKey("deny-enter-items")){
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
		if (!flags.containsKey("enderpearl")){
    		return checkAllowedPlayer(p);
    	}
        return getFlagBool("enderpearl") || checkAllowedPlayer(p);
	}
	
    
	public boolean canMining(Block b) {
    	if (!flags.containsKey("minefarm")){
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

	public boolean canTree(Block b) {
		if (!flags.containsKey("treefarm")){
    		return false;
    	}
		if (b.getType().toString().contains("LOG") || b.getType().toString().contains("LEAVES")){
			return getFlagBool("treefarm");
		}
		return false;
	}
	
	public boolean canSkill(Player p) {
		if (!flags.containsKey("up-skills")){
    		return true;
    	}
        return getFlagBool("up-skills") || checkAllowedPlayer(p);
	}

	public boolean canDeathBack(Player p) {
		if (!flags.containsKey("death-back")){
    		return true;
    	}
        return getFlagBool("death-back") || checkAllowedPlayer(p);
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
	
	public boolean allowMod() {
		if (!flagExists("allow-mod")){
			return false;
		}
		return getFlagBool("allow-mod");
	}

	public Double getValue() {	
		return this.value;
	}
	
	public void setValue(Double value) {	
		RedProtect.rm.updateLiveRegion(this, "value", value.toString());
		this.value = value;
	}
	    
	private boolean checkAllowedPlayer(Player p){
		return this.isOwner(p) || this.isMember(p) || RedProtect.ph.hasPerm(p, "redprotect.bypass");
	}
}
