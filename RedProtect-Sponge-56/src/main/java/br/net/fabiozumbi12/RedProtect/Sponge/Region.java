package br.net.fabiozumbi12.RedProtect.Sponge;

import br.net.fabiozumbi12.RedProtect.Sponge.config.RPLang;
import br.net.fabiozumbi12.RedProtect.Sponge.events.ChangeRegionFlagEvent;
import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Represents a 3D region created by players.
 */
public class Region implements Serializable{

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
    private Map<String, Object> flags;
	private long value;
	private Location<World> tppoint;
	private final boolean waiting = false;
	private boolean canDelete;
	private boolean tosave = true;

	public Map<String, Object> getFlags() {
		return flags;
	}

	public void setFlags(Map<String, Object> flags) {
		this.flags = flags;
	}

	/**Get unique ID of region based on name of "region + @ + world".
	 * @return {@code id string}
	 */
	@Override
	public String toString(){
		return this.name+"@"+this.world;
	}
	
	/**Get unique ID of region based on name of "region + @ + world".
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

	public boolean canDelete(){
		return this.canDelete;
	}
	
	public void setCanDelete(boolean canDelete){
		setToSave(true);
		this.canDelete = canDelete;
	}
	
    public boolean setFlag(Cause cause, String fname, Object value) {
		ChangeRegionFlagEvent event = new ChangeRegionFlagEvent(cause, this, fname, value);
		if (Sponge.getEventManager().post(event)) return false;

    	setToSave(true);
    	this.flags.put(event.getFlag(), event.getFlagValue());
    	RedProtect.get().rm.updateLiveFlags(this, event.getFlag(), event.getFlagValue().toString());
		updateSigns(event.getFlag());
		checkParticle();
		return true;
    }
    
    public void removeFlag(String Name) {
    	setToSave(true);
    	if (this.flags.containsKey(Name)){
    		this.flags.remove(Name); 
    		RedProtect.get().rm.removeLiveFlags(this, Name);
    	}
		checkParticle();
    }

	private Task task;
	private void checkParticle(){
		Sponge.getScheduler().createSyncExecutor(RedProtect.get().container).schedule(() -> {
			if (this.flags.containsKey("particles")){
				if (task == null){

					task = Sponge.getScheduler().createTaskBuilder().execute(() -> {
						if (this.flags.containsKey("particles")){
							String[] part = flags.get("particles").toString().split(" ");
							for (int i = 0; i < Integer.valueOf(part[1]); i++){
								Location locMin = getMinLocation();
								Location locMax = getMaxLocation();

								int dx = locMax.getBlockX() - locMin.getBlockX();
								int dy = locMax.getBlockY() - locMin.getBlockY();
								int dz = locMax.getBlockZ() - locMin.getBlockZ();
								Random random = new Random();
								int x = random.nextInt(Math.abs(dx)+1) + locMin.getBlockX();
								int y = random.nextInt(Math.abs(dy)+1) + locMin.getBlockY();
								int z = random.nextInt(Math.abs(dz)+1) + locMin.getBlockZ();

								ParticleType p = Sponge.getRegistry().getType(ParticleType.class, part[0]).get();
								World w = Sponge.getServer().getWorld(world).get();
								//ParticleTypes.FIRE
								Location<World> loc = new Location<>(w, x+new Random().nextDouble(), y+new Random().nextDouble(), z+new Random().nextDouble());
								if (loc.getBlock().getType().equals(BlockTypes.AIR)){
									ParticleEffect.Builder pf = ParticleEffect.builder().type(p).quantity(1);


									if (part.length == 5){
										pf.offset(new Vector3d(Double.parseDouble(part[2]), Double.parseDouble(part[3]), Double.parseDouble(part[4])));
									}
									if (part.length == 6){
										pf.offset(new Vector3d(Double.parseDouble(part[2]), Double.parseDouble(part[3]), Double.parseDouble(part[4])));
										double multi = Double.parseDouble(part[5]);
										pf.velocity(new Vector3d((new Random().nextDouble()*2-1)*multi, (new Random().nextDouble()*2-1)*multi, (new Random().nextDouble()*2-1)*multi));
									}

									if (!w.getEntities(ent -> ent instanceof Player && loc.getPosition().distance(ent.getLocation().getPosition()) <= 30).isEmpty()){
										w.spawnParticles(pf.build(), loc.getPosition());
									}
								}
							}
						}
					}).intervalTicks(1).submit(RedProtect.get().container);
				}
			} else if (task != null){
				notifyRemove();
			}
		}, 1, TimeUnit.SECONDS);
	}

	public void notifyRemove(){
		if (task != null){
			task.cancel();
			task = null;
		}
	}

	private void updateSigns(String fname){
		if (!RedProtect.get().cfgs.root().region_settings.enable_flag_sign){
			return;
		}
		List<Location> locs = RedProtect.get().cfgs.getSigns(this.getID());
		if (locs.size() > 0){
			for (Location loc:locs){
				if (loc.getTileEntity().isPresent() && loc.getTileEntity().get() instanceof Sign){
					Sign s = (Sign) loc.getTileEntity().get();
					ListValue<Text> lines = s.lines();
					if (lines.get(0).toPlain().equalsIgnoreCase("[flag]")){
						if (lines.get(1).toPlain().equalsIgnoreCase(fname) && this.name.equalsIgnoreCase(lines.get(2).toPlain())){
							lines.set(3, RPUtil.toText(RPLang.get("region.value")+" "+RPLang.translBool(getFlagString(fname))));
							s.offer(lines);
							RedProtect.get().cfgs.putSign(this.getID(), loc);
						}
					} else {
						RedProtect.get().cfgs.removeSign(this.getID(), loc);
					}
				} else {
					RedProtect.get().cfgs.removeSign(this.getID(), loc);
				}
			}
		}
	}

	public void setDate(String value) {
    	setToSave(true);
        this.date = value;
        RedProtect.get().rm.updateLiveRegion(this, "date", value);
    }    
    
    public void setTPPoint(Location<World> loc){   
    	setToSave(true);
    	this.tppoint = loc;
    	if (loc != null){
    		double x = loc.getX();
        	double y = loc.getY();
        	double z = loc.getZ();
        	String pos = loc.getPosition().toString();
        	RedProtect.get().rm.updateLiveRegion(this, "tppoint", x+","+y+","+z+","+pos);
    	} else {
    		RedProtect.get().rm.updateLiveRegion(this, "tppoint", "");
    	}
    	
    }
    
    public Location<World> getTPPoint(){
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
        RedProtect.get().rm.updateLiveRegion(this, "maxy", y);
    }
    
    public int getMinY() {
        return this.minY;
    }
    
    public void setMinY(int y) {
    	setToSave(true);
        this.minY = y;
        RedProtect.get().rm.updateLiveRegion(this, "miny", y);
    }
    
    public void setWorld(String w) {
    	setToSave(true);
        this.world = w;
        RedProtect.get().rm.updateLiveRegion(this, "world", w);
    }   
    
    public Location<World> getMaxLocation(){
    	return new Location<>(Sponge.getServer().getWorld(this.world).get(), this.maxMbrX, this.maxY, this.maxMbrZ);
    }
    
    public Location<World> getMinLocation(){
    	return new Location<>(Sponge.getServer().getWorld(this.world).get(), this.minMbrX, this.minY, this.minMbrZ);
    }
    
    public String getWorld() {
        return this.world;
    }
    
    public void setPrior(int prior) {
    	setToSave(true);
        this.prior = prior;
        RedProtect.get().rm.updateLiveRegion(this, "prior", prior);
    }
    
    public int getPrior() {
        return this.prior;
    }
    
    public void setWelcome(String s){
    	setToSave(true);
    	this.wMessage = s;
    	RedProtect.get().rm.updateLiveRegion(this, "wel", s);
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
        RedProtect.get().rm.updateLiveRegion(this, "leaders", leaders.toString().replace("[", "").replace("]", ""));
    }
    
    public void setAdmins(List<String> admins) {
    	setToSave(true);
        this.admins = admins;
        RedProtect.get().rm.updateLiveRegion(this, "admins", admins.toString().replace("[", "").replace("]", ""));
    }
    
    public void setMembers(List<String> members) {
    	setToSave(true);
        this.members = members;
        RedProtect.get().rm.updateLiveRegion(this, "members", members.toString().replace("[", "").replace("]", ""));
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
    
    public String getFlagStrings(){
    	StringBuilder flags = new StringBuilder();
        for (String flag:this.flags.keySet()){
        	flags.append(","+flag+":"+this.flags.get(flag).toString());
        }        
    	return flags.toString().substring(1);
    }
    
    public String getTPPointString(){
    	if (tppoint == null){
    		return "";
    	}
    	return this.tppoint.getX()+","+this.tppoint.getY()+","+this.tppoint.getZ()/*+","+this.tppoint.getYaw()+","+this.tppoint.getPitch()*/;
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
    
	public Text info() {
		String leadersstring = "";
        String adminstring = "";
        String memberstring = "";
        String wMsgTemp;
        String IsTops = RPLang.translBool(isOnTop());
        String today;
        String wName = this.world;
        String colorChar = "";
        
        if (RedProtect.get().cfgs.root().region_settings.world_colors.containsKey(this.world)){
        	colorChar = RedProtect.get().cfgs.root().region_settings.world_colors.get(this.world);
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
        	if (RedProtect.get().OnlineMode){
        		User play = null;
        		if (pname != null && !pname.equalsIgnoreCase(RedProtect.get().cfgs.root().region_settings.default_leader)){
                	play = RPUtil.getUser(pname);
            	}            
            	if (pname != null && play != null && play.isOnline()){
            		today = "&aOnline!";
            		break;
            	} 
        	} else if (RedProtect.get().serv.getPlayer(pname).isPresent()){
        		today = "&aOnline!";
        		break; 
        	}                   	
        } 
        for (String pname:this.admins){        	
        	if (RedProtect.get().OnlineMode){
        		User play = null;
        		if (pname != null && !pname.equalsIgnoreCase(RedProtect.get().cfgs.root().region_settings.default_leader)){
                	play = RPUtil.getUser(pname);
            	}            
            	if (pname != null && play != null && play.isOnline()){
            		today = "&aOnline!";
            		break;
            	} 
        	} else if (RedProtect.get().serv.getPlayer(pname).isPresent()){
        		today = "&aOnline!";
        		break; 
        	}  
        } 

        return RPUtil.toText(RPLang.get("region.name") + " " + colorChar+this.name + RPLang.get("general.color") + " | " + RPLang.get("region.priority") + " " + this.prior + "\n" +      
        RPLang.get("region.priority.top") + " "  + IsTops  + RPLang.get("general.color") + " | " + RPLang.get("region.lastvalue") + RPEconomy.getFormatted(this.value) + "\n" +
       RPLang.get("region.world") + " " + colorChar+wName + RPLang.get("general.color") + " | " + RPLang.get("region.center") + " " + this.getCenterX() + ", " + this.getCenterZ() + "\n" +
       RPLang.get("region.ysize") + " " + this.minY + " - " + this.maxY + RPLang.get("general.color") + " | "+ RPLang.get("region.area") + " " + this.getArea() + "\n" +
       RPLang.get("region.leaders") + " " + leadersstring + "\n" +
       RPLang.get("region.admins") + " " + adminstring + RPLang.get("general.color") + " | " + RPLang.get("region.members") + " " + memberstring + "\n" +
       RPLang.get("region.date") + " " + today + "\n" +
       RPLang.get("region.welcome.msg") + " " + wMsgTemp);
    }
	
	private String conformName(String name){
		return RPUtil.toText(name).toPlain();
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
    /*public Region(String name, List<String> admins, List<String> members, List<String> leaders, Location<World> minLoc, Location<World> maxLoc, HashMap<String,Object> flags, String wMessage, int prior, String worldName, String date, long value, Location<World> tppoint, boolean candel) {
    	super();        
        this.maxMbrX = maxLoc.getBlockX();
        this.minMbrX = minLoc.getBlockX();
        this.maxMbrZ = maxLoc.getBlockZ();
        this.minMbrZ = minLoc.getBlockZ();
        this.maxY = maxLoc.getBlockY();
        this.minY = minLoc.getBlockY();
        this.x = new int[] {minMbrX,minMbrX,maxMbrX,maxMbrX};
        this.z = new int[] {minMbrZ,minMbrZ,maxMbrZ,maxMbrZ};
        this.name = conformName(name);
        this.admins = admins;
        this.members = members;
        this.leaders = leaders;    
        this.flags = flags;
        this.value = value;
        this.tppoint = tppoint;
        this.canDelete = candel;
        this.prior = prior;
        
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
    }*/
    
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
    public Region(String name, List<String> admins, List<String> members, List<String> leaders, int maxMbrX, int minMbrX, int maxMbrZ, int minMbrZ, int minY, int maxY, HashMap<String,Object> flags, String wMessage, int prior, String worldName, String date, long value, Location<World> tppoint, boolean candel) {
    	super();
        this.x = new int[] {minMbrX,minMbrX,maxMbrX,maxMbrX};
        this.z = new int[] {minMbrZ,minMbrZ,maxMbrZ,maxMbrZ};
        this.maxMbrX = maxMbrX;
        this.minMbrX = minMbrX;
        this.maxMbrZ = maxMbrZ;
        this.minMbrZ = minMbrZ;
        this.maxY = maxY;
        this.minY = minY;
        this.name = conformName(name);
        this.admins = admins;
        this.members = members;
        this.leaders = leaders;    
        this.flags = flags;
        this.value = value;
        this.tppoint = tppoint;
        this.canDelete = candel;
        
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
		checkParticle();
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
    public Region(String name, List<String> admins, List<String> members, List<String> leaders, int[] x, int[] z, int miny, int maxy, int prior, String worldName, String date, Map<String, Object> flags, String welcome, long value, Location<World> tppoint, boolean candel) {
    	super();
        this.prior = prior;
        this.world = worldName;
        this.date = date;
        this.flags = flags;
        this.wMessage = welcome;
        int size = x.length;
        this.value = value;
        this.tppoint = tppoint;
        this.canDelete = candel;
          	    
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
        this.name = conformName(name);
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
		checkParticle();
    }

    public void clearLeaders(){
    	setToSave(true);
    	this.leaders.clear();
    	RedProtect.get().rm.updateLiveRegion(this, "leaders", "");
    }
    
    public void clearAdmins(){
    	setToSave(true);
    	this.admins.clear();
    	RedProtect.get().rm.updateLiveRegion(this, "admins", "");
    }
    
    public void clearMembers(){
    	setToSave(true);
    	this.members.clear();
    	RedProtect.get().rm.updateLiveRegion(this, "members", "");
    }
    /*
    public void delete() {
        RedProtect.get().rm.remove(this);
    }
    */
    public int getArea() {
    	return Math.abs((this.maxMbrX - this.minMbrX)+1) * Math.abs((this.maxMbrZ - this.minMbrZ)+1);  	
    }
    
    public boolean inBoundingRect(Region other) {
        return other.maxMbrX >= this.minMbrX && other.minMbrZ >= this.minMbrZ && other.minMbrX <= this.maxMbrX && other.minMbrZ <= this.maxMbrZ;
    }
     
    public boolean isLeader(String player) {
        return this.leaders.contains(RPUtil.PlayerToUUID(player));
    }
    
	public boolean isLeader(Player player) {
        return this.leaders.contains(RPUtil.PlayerToUUID(player.getName()));
    }
	/*
	public boolean isLeader(User player) {
        return this.leaders.contains(RPUtil.PlayerToUUID(player.getName()));
    }
    */
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
    	if (!RedProtect.get().OnlineMode){
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
        RedProtect.get().rm.updateLiveRegion(this, "leaders", this.leaders.toString().replace("[", "").replace("]", ""));
        RedProtect.get().rm.updateLiveRegion(this, "admins", this.admins.toString().replace("[", "").replace("]", ""));
        RedProtect.get().rm.updateLiveRegion(this, "members", this.members.toString().replace("[", "").replace("]", ""));
    }
    
	/** Add a member to the Region. The string need to be UUID if Online Mode, or Player Name if Offline Mode.
     * @param uuid - UUID or Player Name.
     */
    public void addMember(String uuid) {
    	setToSave(true);
    	String pinfo = uuid;
    	if (!RedProtect.get().OnlineMode){
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
        RedProtect.get().rm.updateLiveRegion(this, "leaders", this.leaders.toString().replace("[", "").replace("]", ""));
        RedProtect.get().rm.updateLiveRegion(this, "admins", this.admins.toString().replace("[", "").replace("]", ""));
        RedProtect.get().rm.updateLiveRegion(this, "members", this.members.toString().replace("[", "").replace("]", ""));
    }
    
    /** Add an admin to the Region. The string need to be UUID if Online Mode, or Player Name if Offline Mode.
     * @param uuid - UUID or Player Name.
     */
    public void addAdmin(String uuid) {
    	setToSave(true);
    	String pinfo = uuid;
    	if (!RedProtect.get().OnlineMode){
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
        RedProtect.get().rm.updateLiveRegion(this, "leaders", this.leaders.toString().replace("[", "").replace("]", ""));
        RedProtect.get().rm.updateLiveRegion(this, "admins", this.admins.toString().replace("[", "").replace("]", ""));
        RedProtect.get().rm.updateLiveRegion(this, "members", this.members.toString().replace("[", "").replace("]", ""));
    }
    
    /** Remove an member to the Region. The string need to be UUID if Online Mode, or Player Name if Offline Mode.
     * @param uuid - UUID or Player Name.
     */
    public void removeMember(String uuid) {
    	setToSave(true);
    	String pinfo = uuid;
    	if (!RedProtect.get().OnlineMode){
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
        RedProtect.get().rm.updateLiveRegion(this, "admins", this.admins.toString().replace("[", "").replace("]", ""));
        RedProtect.get().rm.updateLiveRegion(this, "members", this.members.toString().replace("[", "").replace("]", ""));
        RedProtect.get().rm.updateLiveRegion(this, "leaders", this.leaders.toString().replace("[", "").replace("]", ""));
    }
    
    /** Remove an admin to the Region. The string need to be UUID if Online Mode, or Player Name if Offline Mode.
     * @param uuid - UUID or Player Name.
     */
    public void removeAdmin(String uuid) {
    	setToSave(true);
    	String pinfo = uuid;
    	if (!RedProtect.get().OnlineMode){
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
        RedProtect.get().rm.updateLiveRegion(this, "admins", this.admins.toString().replace("[", "").replace("]", ""));
        RedProtect.get().rm.updateLiveRegion(this, "members", this.members.toString().replace("[", "").replace("]", ""));
        RedProtect.get().rm.updateLiveRegion(this, "leaders", this.leaders.toString().replace("[", "").replace("]", ""));
    }
    
    /** Remove an leader to the Region. The string need to be UUID if Online Mode, or Player Name if Offline Mode.
     * @param uuid - UUID or Player Name.
     */
    public void removeLeader(String uuid) {
    	setToSave(true);
    	String pinfo = uuid;
    	if (!RedProtect.get().OnlineMode){
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
        RedProtect.get().rm.updateLiveRegion(this, "admins", this.admins.toString().replace("[", "").replace("]", ""));
        RedProtect.get().rm.updateLiveRegion(this, "members", this.members.toString().replace("[", "").replace("]", ""));
        RedProtect.get().rm.updateLiveRegion(this, "leaders", this.leaders.toString().replace("[", "").replace("]", ""));
    }
    
    public boolean getFlagBool(String key) {
    	if (!flagExists(key) || !RedProtect.get().cfgs.isFlagEnabled(key)){
    		if (RedProtect.get().cfgs.getDefFlagsValues().get(key) != null){
    			return (Boolean) RedProtect.get().cfgs.getDefFlagsValues().get(key);
    		} else {
    			return RedProtect.get().cfgs.root().flags.get(key);
    		}  		
    	}
        return this.flags.get(key) instanceof Boolean && (Boolean)this.flags.get(key);
    }
    
    public String getFlagString(String key) {
    	if (!flagExists(key) || !RedProtect.get().cfgs.isFlagEnabled(key)){
    		if (RedProtect.get().cfgs.getDefFlagsValues().get(key) != null){
    			return (String) RedProtect.get().cfgs.getDefFlagsValues().get(key);
    		} else {
    			return RedProtect.get().cfgs.root().flags.get(key).toString();
    		}
    	}
        return this.flags.get(key).toString();
    }
    
    public int adminSize() {
        return this.admins.size();
    }
    
    public int leaderSize() {
        return this.leaders.size();
    }
    
    public Text getFlagInfo() {
    	String flaginfo = "";
    	for (String flag:this.flags.keySet()){
    		if (RedProtect.get().cfgs.getDefFlags().contains(flag)){
    			String flagValue = this.flags.get(flag).toString();
    			if (flagValue.equalsIgnoreCase("true") || flagValue.equalsIgnoreCase("false")){
    				flaginfo = flaginfo + ", &b" + flag + ": " + RPLang.translBool(flagValue);
    			} else {
    				flaginfo = flaginfo + ", &b" + flag + ": &8" + flagValue;
    			}    			
    		} 
    		
    		if (flaginfo.contains(flag)){
				continue;
			}
    		
    		if (RedProtect.get().cfgs.AdminFlags.contains(flag)){    			
    			String flagValue = this.flags.get(flag).toString();
    			if (flagValue.equalsIgnoreCase("true") || flagValue.equalsIgnoreCase("false")){
    				flaginfo = flaginfo + ", &b" + flag + ": " + RPLang.translBool(flagValue);
    			} else {
    				flaginfo = flaginfo + ", &b" + flag + ": &8" + flagValue;
    			} 
    		} 
    	}    	
    	if (this.flags.keySet().size() > 0) {
    		flaginfo = flaginfo.substring(2);
        }
        else {
        	flaginfo = "Default";
        }
        return RPUtil.toText(flaginfo);
    }
        
    public boolean isOnTop(){
    	Region newr = RedProtect.get().rm.getTopRegion(RedProtect.get().serv.getWorld(this.getWorld()).get(), this.getCenterX(), this.getCenterY(), this.getCenterZ(), this.getClass().getName());
		return newr == null || newr.equals(this);    	
    }
    
    public boolean flagExists(String key){
    	return flags.containsKey(key);
    }	
	
	//---------------------- Admin Flags --------------------------// 
    
    public boolean canPickup(Player p) {
		return !flagExists("can-pickup") || getFlagBool("can-pickup") || checkAllowedPlayer(p);
	}
    
    public boolean canDrop(Player p) {
		return !flagExists("can-drop") || getFlagBool("can-drop") || checkAllowedPlayer(p);
	}
    
    public boolean canSpawnWhiter() {
		return !flagExists("spawn-wither") || getFlagBool("spawn-wither");
	}
    
    public int maxPlayers() {
		if (!flagExists("max-players")){
    		return -1;
    	}
		return new Integer(getFlagString("max-players"));
	}
    
    public boolean canDeath() {
		return !flagExists("can-death") || getFlagBool("can-death");
	}
    
    public boolean keepInventory() {
		return flagExists("keep-inventory") && getFlagBool("keep-inventory");
	}
	
	public boolean keepLevels() {
		return flagExists("keep-levels") && getFlagBool("keep-levels");
	}
	
    public boolean cmdOnHealth(Player p){
    	if (!flagExists("cmd-onhealth")){
    		return false;
    	}
    	
    	boolean run = false;
    	//rp flag cmd-onhealth health:<number> cmd:<cmd>, ...
    	for (String group:getFlagString("cmd-onhealth").split(",")){
    		int health = Integer.parseInt(group.split(" ")[0].substring(7));
    		String cmd = group.replace(group.split(" ")[0]+" ", "").substring(4);
    		if (cmd.startsWith("/")){
        		cmd = cmd.substring(1);
        	}
    		if (p.get(Keys.HEALTH).get() <= health && !waiting){
    			RedProtect.get().game.getCommandManager().process(RedProtect.get().serv.getConsole(), cmd.replace("{player}", p.getName()));
    			/*waiting = true;
    			Bukkit.getScheduler().runTaskLater(RedProtect.get().plugin, new Runnable(){
					@Override
					public void run() {
						waiting = false;
					}    				
    			}, 20);*/
    			run = true;
    		}    		
    	}
    	return run;
    }
    
    public boolean canPlayerDamage() {
		return !flagExists("player-damage") || getFlagBool("player-damage");
	}
    
    public boolean canHunger() {
		return !flagExists("can-hunger") || getFlagBool("can-hunger");
	}
    
    public boolean canSign(Player p) {
		if (!flagExists("sign")){
    		return checkAllowedPlayer(p);
    	}		
        return getFlagBool("sign") || checkAllowedPlayer(p);
	}

	public boolean canExit(Player p) {
    	if (!canExitWithItens(p)){
    		return false;
		}
		return !flagExists("exit") || getFlagBool("exit") || RedProtect.get().ph.hasPerm(p, "redprotect.region-exit." + this.name) || checkAllowedPlayer(p);
	}

	public boolean canEnter(Player p) {
		if (RedProtect.get().denyEnter.containsKey(p.getName()) && RedProtect.get().denyEnter.get(p.getName()).contains(this.getID())) {
			return checkAllowedPlayer(p);
		}

		return !flagExists("enter") || getFlagBool("enter") || RedProtect.get().ph.hasPerm(p, "redprotect.region-enter." + this.name) || checkAllowedPlayer(p);
	}

	public boolean canExitWithItens(Player p) {
		if (!flagExists("deny-exit-items")){
			return true;
		}

		if (checkAllowedPlayer(p)){
			return true;
		}

		List<String> items = Arrays.asList(flags.get("deny-exit-items").toString().replace(" ", "").split(","));
		Iterable<Slot> SlotItems =  p.getInventory().slots();

		for (Slot slot:SlotItems) {
			if (slot.peek().isPresent()) {
				if (items.stream().anyMatch(k -> k.equalsIgnoreCase(slot.peek().get().getItem().getName()))){
					return false;
				}
			}
		}
		return true;
	}

	public boolean canEnterWithItens(Player p) {
		if (!flagExists("allow-enter-items")){
    		return true;
    	}		
		
		if (checkAllowedPlayer(p)){
			return true;
		}
		
		List<String> items = Arrays.asList(flags.get("allow-enter-items").toString().replace(" ", "").split(","));
		Iterable<Slot> SlotItems =  p.getInventory().slots();
		for (Slot slot:SlotItems) {
		    if (slot.peek().isPresent()) {
				if (items.stream().anyMatch(k -> k.equalsIgnoreCase(slot.peek().get().getItem().getName()))){
					return true;
				}
		    }
		}
        return false;
	}
	
	public boolean denyEnterWithItens(Player p) {
		if (!flagExists("deny-enter-items")){
    		return true;
    	}		
		if (checkAllowedPlayer(p)){
			return true;
		}
		
		Iterable<Slot> SlotItems =  p.getInventory().slots();
		List<String> items = Arrays.asList(flags.get("deny-enter-items").toString().replace(" ", "").split(","));
		
		for (Slot slot:SlotItems){
			if (slot.peek().isPresent()) {
				if (items.stream().anyMatch(k -> k.equalsIgnoreCase(slot.peek().get().getItem().getName()))){
					return false;
				}
			}
		}
        return true;
	}
	
	public boolean canCrops(BlockSnapshot b) {
		if (!flagExists("cropsfarm")) {
			return false;
		}
		return (b.getState().getType().equals(BlockTypes.WHEAT) || b.getState().getType().equals(BlockTypes.POTATOES) || b.getState().getType().equals(BlockTypes.CARROTS) || b.getState().getType().equals(BlockTypes.PUMPKIN_STEM) || b.getState().getType().equals(BlockTypes.MELON_STEM) || b.getState().getType().getName().contains("CHORUS_") || b.getState().getType().getName().contains("BEETROOT_BLOCK") || b.getState().getType().getName().contains("SUGAR_CANE")) && getFlagBool("cropsfarm");
	}
    
	public boolean canMining(BlockSnapshot b) {
		return flagExists("minefarm") && (b.getState().getType().getName().contains("_ORE") || b.getState().getType().equals(BlockTypes.STONE) || b.getState().getType().equals(BlockTypes.GRASS) || b.getState().getType().equals(BlockTypes.DIRT)) && getFlagBool("minefarm");
	}
	
	public boolean canPlace(BlockSnapshot b) {
    	if (!flagExists("allow-place")){
    		return false;
    	}
    	
    	String[] blocks = getFlagString("allow-place").replace(" ", "").split(",");
		for (String block:blocks){
			if (block.equalsIgnoreCase(b.getState().getType().getName())){
				return true;
			}
		}
		return false;
	}
	
	public boolean canBreak(BlockSnapshot b) {
    	if (!flagExists("allow-break")){
    		return false;
    	}
    	String[] blocks = getFlagString("allow-break").replace(" ", "").split(",");
		for (String block:blocks){
			if (block.equalsIgnoreCase(b.getState().getType().getName())){
				return true;
			}
		}
		return false;
	}

	public boolean canTree(BlockSnapshot b) {
		return flagExists("treefarm") && (b.getState().getType().getName().contains("log") || b.getState().getType().getName().contains("leaves")) && getFlagBool("treefarm");
	}
	
	public boolean canSkill(Player p) {
		return !flagExists("up-skills") || getFlagBool("up-skills") || checkAllowedPlayer(p);
	}

	public boolean canBack(Player p) {
		return !flagExists("can-back") || getFlagBool("can-back") || checkAllowedPlayer(p);
	}
	
	public boolean isForSale() {
		return flagExists("for-sale") && getFlagBool("for-sale");
	}
	
	public boolean isPvPArena() {
		return flagExists("pvparena") && getFlagBool("pvparena");
	}
	
	public boolean allowMod(Player p) {
		if (!flagExists("allow-mod")){
			return checkAllowedPlayer(p);
		}		
		return getFlagBool("allow-mod") || checkAllowedPlayer(p);
	}
		
	public boolean canEnterPortal(Player p) {
		return !flagExists("portal-enter") || getFlagBool("portal-enter") || checkAllowedPlayer(p);
	}
	
	public boolean canExitPortal(Player p) {
		return !flagExists("portal-exit") || getFlagBool("portal-exit") || checkAllowedPlayer(p);
	}
	
	public boolean canPet(Player p) {
		return !flagExists("can-pet") || getFlagBool("can-pet") || checkAllowedPlayer(p);
	}
	
	public boolean canProtectiles(Player p) {
		return !flagExists("can-projectiles") || getFlagBool("can-projectiles") || checkAllowedPlayer(p);
	}
	
	public boolean canCreatePortal() {
		return !flagExists("allow-create-portal") || getFlagBool("allow-create-portal");
	}
	
	public boolean AllowCommands(Player p, String fullcmd) {
		if (!flagExists("allow-cmds")){
			return true;
		}
		
		String Command = fullcmd.replace("/", "").split(" ")[0];
		List<String> argsRaw = Arrays.asList(fullcmd.replace("/"+Command+" ", "").split(" "));
		
		//As Whitelist
		String[] flagCmds = flags.get("allow-cmds").toString().split(",");
		for (String cmd:flagCmds){
			if (cmd.startsWith(" ")){
				cmd = cmd.substring(1);
			}
			String[] cmdarg = cmd.split(" ");
			if (cmdarg.length == 2){
				if (cmdarg[0].startsWith("cmd:") && cmdarg[0].split(":")[1].equalsIgnoreCase(Command) && 
						cmdarg[1].startsWith("arg:") && argsRaw.contains(cmdarg[1].split(":")[1])){
					return true;
				}
				if (cmdarg[1].startsWith("cmd:") && cmdarg[1].split(":")[1].equalsIgnoreCase(Command) && 
						cmdarg[0].startsWith("arg:") && argsRaw.contains(cmdarg[0].split(":")[1])){
					return true;
				}
			} else {
				if (cmdarg[0].startsWith("cmd:") && cmdarg[0].split(":")[1].equalsIgnoreCase(Command)){
					return true;
				}
				if (cmdarg[0].startsWith("arg:") && argsRaw.contains(cmdarg[0].split(":")[1])){
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean DenyCommands(Player p, String fullcmd) {
		if (!flagExists("deny-cmds")){
			return true;
		}
		
		String Command = fullcmd.replace("/", "").split(" ")[0];
		List<String> argsRaw = Arrays.asList(fullcmd.replace("/"+Command+" ", "").split(" "));
		
		//As Blacklist
		String[] flagCmds = flags.get("deny-cmds").toString().split(",");
		for (String cmd:flagCmds){
			
			if (cmd.startsWith(" ")){
				cmd = cmd.substring(1);
			}
			String[] cmdarg = cmd.split(" ");
			if (cmdarg.length == 1){
				if (cmdarg[0].startsWith("cmd:") && cmdarg[0].split(":")[1].equalsIgnoreCase(Command)){
					return false;
				}
				if (cmdarg[0].startsWith("arg:") && argsRaw.contains(cmdarg[0].split(":")[1])){
					return false;
				}
			} else {
				if (cmdarg[0].startsWith("cmd:") && cmdarg[0].split(":")[1].equalsIgnoreCase(Command) && 
						cmdarg[1].startsWith("arg:") && argsRaw.contains(cmdarg[1].split(":")[1])){
					return false;
				}
				if (cmdarg[1].startsWith("cmd:") && cmdarg[1].split(":")[1].equalsIgnoreCase(Command) && 
						cmdarg[0].startsWith("arg:") && argsRaw.contains(cmdarg[0].split(":")[1])){
					return false;
				}
			}
		}		
		return true;
	}
	
	
	//---------------------- Player Flags --------------------------//
    public boolean allowPressPlate(Player p){
        if (!RedProtect.get().cfgs.isFlagEnabled("press-plate")){
            return RedProtect.get().cfgs.root().flags.get("press-plate") || checkAllowedPlayer(p);
        }
        return getFlagBool("press-plate") || checkAllowedPlayer(p);
    }

	public boolean canBuild(Player p) {
    	if (flagExists("for-sale") && !RedProtect.get().ph.hasPerm(p, "redprotect.bypass")){
    		return false;
    	}
    	if (!RedProtect.get().cfgs.isFlagEnabled("build")){
    		return RedProtect.get().cfgs.root().flags.get("build") || checkAllowedPlayer(p);
    	}
        return getFlagBool("build") || checkAllowedPlayer(p);
    }
	
	public boolean leavesDecay() {
		if (!RedProtect.get().cfgs.isFlagEnabled("leaves-decay")){
    		return RedProtect.get().cfgs.root().flags.get("leaves-decay");
    	}
		return getFlagBool("leaves-decay");
	}
	
	/**Allow non members of this region to break/place spawners.
	 * 
	 * @return boolean
	 */
	public boolean allowSpawner(Player p) {
		if (!RedProtect.get().cfgs.isFlagEnabled("allow-spawner")){
    		return RedProtect.get().cfgs.root().flags.get("allow-spawner");
    	}
		return getFlagBool("allow-spawner") || checkAllowedPlayer(p);
	}
	
	public boolean canTeleport(Player p) {
		if (!RedProtect.get().cfgs.isFlagEnabled("teleport")){
    		return checkAllowedPlayer(p) || RedProtect.get().cfgs.root().flags.get("teleport");
    	}
        return checkAllowedPlayer(p) || getFlagBool("teleport");
	}
	
	/**Allow players with fly enabled fly on this region.
	 * 
	 * @return boolean
	 */
	public boolean canFly(Player p) {
		if (!RedProtect.get().cfgs.isFlagEnabled("allow-fly")){
    		return RedProtect.get().cfgs.root().flags.get("allow-fly");
    	}
		return getFlagBool("allow-fly") || checkAllowedPlayer(p);
	}
	
	public boolean FlowDamage() {
		if (!RedProtect.get().cfgs.isFlagEnabled("flow-damage")){
    		return RedProtect.get().cfgs.root().flags.get("flow-damage");
    	}
		return getFlagBool("flow-damage");
	}
	
	public boolean canMobLoot() {
    	if (!RedProtect.get().cfgs.isFlagEnabled("mob-loot")){
    		return RedProtect.get().cfgs.root().flags.get("mob-loot");
    	}
        return getFlagBool("mob-loot");
    }
	
	public boolean allowEffects(Player p) {
    	if (!RedProtect.get().cfgs.isFlagEnabled("allow-effects")){
    		return RedProtect.get().cfgs.root().flags.get("allow-effects");
    	}    	
        return getFlagBool("allow-effects") || checkAllowedPlayer(p);
    }
	
	public boolean usePotions(Player p) {
    	if (!RedProtect.get().cfgs.isFlagEnabled("use-potions")){
    		return RedProtect.get().cfgs.root().flags.get("use-potions");
    	}    	
        return getFlagBool("use-potions") || checkAllowedPlayer(p);
    }
    
    public boolean canPVP(Player p) {
    	if (!RedProtect.get().cfgs.isFlagEnabled("pvp")){
    		return RedProtect.get().cfgs.root().flags.get("pvp") || RedProtect.get().ph.hasPerm(p, "redprotect.bypass");
    	}
        return getFlagBool("pvp") || RedProtect.get().ph.hasPerm(p, "redprotect.bypass");
    }
    
    public boolean canChest(Player p) {
    	if (!RedProtect.get().cfgs.isFlagEnabled("chest")){
    		return RedProtect.get().cfgs.root().flags.get("chest") || checkAllowedPlayer(p);
    	}
        return getFlagBool("chest") || checkAllowedPlayer(p);
    }
    
    public boolean canLever(Player p) {
    	if (!RedProtect.get().cfgs.isFlagEnabled("lever")){
    		return RedProtect.get().cfgs.root().flags.get("lever") || checkAllowedPlayer(p);
    	}
        return getFlagBool("lever") || checkAllowedPlayer(p);
    }
    
    public boolean canButton(Player p) {
    	if (!RedProtect.get().cfgs.isFlagEnabled("button")){
    		return RedProtect.get().cfgs.root().flags.get("button") || checkAllowedPlayer(p);
    	}
        return getFlagBool("button") || checkAllowedPlayer(p);
    }
    
    public boolean canDoor(Player p) {
    	if (!RedProtect.get().cfgs.isFlagEnabled("door")){
    		return RedProtect.get().cfgs.root().flags.get("door") || checkAllowedPlayer(p);
    	}
        return getFlagBool("door") || checkAllowedPlayer(p);
    }
    
    public boolean canSpawnMonsters() {
    	if (!RedProtect.get().cfgs.isFlagEnabled("spawn-monsters")){
    		return RedProtect.get().cfgs.root().flags.get("spawn-monsters");
    	}
        return getFlagBool("spawn-monsters");
    }
        
    public boolean canSpawnPassives() {
    	if (!RedProtect.get().cfgs.isFlagEnabled("spawn-animals")){
    		return RedProtect.get().cfgs.root().flags.get("spawn-animals");
    	}
        return getFlagBool("spawn-animals");
    }
    
	public boolean canMinecart(Player p) {
		if (!RedProtect.get().cfgs.isFlagEnabled("minecart")){
    		return RedProtect.get().cfgs.root().flags.get("minecart") || checkAllowedPlayer(p);
    	}
        return getFlagBool("minecart") || checkAllowedPlayer(p);
	}
	
	public boolean canInteractPassives(Player p) {
    	if (!RedProtect.get().cfgs.isFlagEnabled("passives")){
    		return RedProtect.get().cfgs.root().flags.get("passives") || checkAllowedPlayer(p);
    	}
        return getFlagBool("passives") || checkAllowedPlayer(p);
    }
    
    public boolean canFlow() {
    	if (!RedProtect.get().cfgs.isFlagEnabled("flow")){
    		return RedProtect.get().cfgs.root().flags.get("flow");
    	}
        return getFlagBool("flow");
    }
    
    public boolean canFire() {
    	if (!RedProtect.get().cfgs.isFlagEnabled("fire")){
    		return RedProtect.get().cfgs.root().flags.get("fire");
    	}
        return getFlagBool("fire");
    }
    
    public boolean AllowHome(Player p) {
		if (!RedProtect.get().cfgs.isFlagEnabled("allow-home")){
    		return RedProtect.get().cfgs.root().flags.get("allow-home") || checkAllowedPlayer(p);
    	}
		return getFlagBool("allow-home") || checkAllowedPlayer(p);
	}
    
    public boolean canGrow() {
    	if (!RedProtect.get().cfgs.isFlagEnabled("can-grow")){
    		return RedProtect.get().cfgs.root().flags.get("can-grow");
    	}
		return getFlagBool("can-grow");
	}
	//--------------------------------------------------------------//
	
	public long getValue() {	
		return this.value;
	}
	
	public void setValue(long value) {	
		setToSave(true);
		RedProtect.get().rm.updateLiveRegion(this, "value", value);
		this.value = value;
	}
	    
	private boolean checkAllowedPlayer(Player p){
		return this.isLeader(p) || this.isAdmin(p) || this.isMember(p) || RedProtect.get().ph.hasPerm(p, "redprotect.bypass");
	}
	
	public List<Location<World>> getLimitLocs(int locy){
		final List<Location<World>> locBlocks = new ArrayList<>();
		Location<World> loc1 = this.getMinLocation();
		Location<World> loc2 = this.getMaxLocation();
		World w = Sponge.getServer().getWorld(this.getWorld()).get();
		
		for (int x = (int) loc1.getX(); x <= (int) loc2.getX(); ++x) {
            for (int z = (int) loc1.getZ(); z <= (int) loc2.getZ(); ++z) {
                //for (int y = (int) loc1.getY(); y <= (int) loc2.getY(); ++y) {
                    if (z == loc1.getZ() || z == loc2.getZ() ||
                        x == loc1.getX() || x == loc2.getX() ) {
                    	locBlocks.add(new Location<>(w, x, locy, z));
                    }
                //}
            }
        } 
		return locBlocks;
	}
	
	public List<Location<World>> getLimitLocs(int miny, int maxy, boolean define){
		final List<Location<World>> locBlocks = new ArrayList<>();
		Location<World> loc1 = this.getMinLocation();
		Location<World> loc2 = this.getMaxLocation();
		World w = Sponge.getServer().getWorld(this.getWorld()).get();
		
		for (int x = loc1.getBlockX(); x <= loc2.getBlockX(); ++x) {
            for (int z = loc1.getBlockZ(); z <= loc2.getBlockZ(); ++z) {
                for (int y = miny; y <= maxy; ++y) {
                    if ((z == loc1.getBlockZ() || z == loc2.getBlockZ() ||
                        x == loc1.getBlockX() || x == loc2.getBlockX())
                        && (define || new Location<>(w, x, y, z).getBlock().getType().getName().contains(RedProtect.get().cfgs.root().region_settings.block_id))) {
                    	locBlocks.add(new Location<>(w, x, y, z));
                    }
                }
            }
        } 
		return locBlocks;
	}
	
	public List<Location<World>> get4Points(int y){
		List <Location<World>> locs = new ArrayList<>();
		locs.add(this.getMinLocation());
		locs.add(new Location<>(this.getMinLocation().getExtent(), this.minMbrX, y, this.minMbrZ + (this.maxMbrZ - this.minMbrZ)));
		locs.add(this.getMaxLocation());		
		locs.add(new Location<>(this.getMinLocation().getExtent(), this.minMbrX + (this.maxMbrX - this.minMbrX), y, this.minMbrZ));
		return locs;		
	}

	public Location<World> getCenterLoc() {
		return new Location<>(Sponge.getServer().getWorld(this.world).get(), this.getCenterX(), this.getCenterY(), this.getCenterZ());
	}
	
	public String getAdminDesc() {
		if (this.admins.size() == 0){
			return "[none]";
		}
		StringBuilder adminsList = new StringBuilder();
		for (String admin:this.admins){
			adminsList.append(", ").append(RPUtil.UUIDtoPlayer(admin));
		}
		return "["+adminsList.toString().substring(2)+"]";
	}
	
	public String getLeadersDesc() {
		if (this.leaders.isEmpty()){
			addLeader(RedProtect.get().cfgs.root().region_settings.default_leader);
			return this.leaders.get(0);
		}
		StringBuilder leaderList = new StringBuilder();
		for (String leader:this.leaders){
			leaderList.append(", ").append(RPUtil.UUIDtoPlayer(leader));
		}
		return "["+leaderList.toString().substring(2)+"]";
	}

	public boolean sameLeaders(Region r) {
		for (String l:this.leaders){
			if (r.getLeaders().contains(l)){
				return true;
			}
		}
		return false;
	}

    public boolean allowDynmap() {
		return !flagExists("dynmap") || getFlagBool("dynmap");
    }
}
