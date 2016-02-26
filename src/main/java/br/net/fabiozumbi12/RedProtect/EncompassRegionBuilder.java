package br.net.fabiozumbi12.RedProtect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;

import br.net.fabiozumbi12.RedProtect.config.RPConfig;
import br.net.fabiozumbi12.RedProtect.config.RPLang;

public class EncompassRegionBuilder extends RegionBuilder{

    public EncompassRegionBuilder(SignChangeEvent e) { 	
        String owner1 = RPUtil.PlayerToUUID(e.getLine(2));
        String owner2 = RPUtil.PlayerToUUID(e.getLine(3));
        Block b = e.getBlock();
        World w = b.getWorld();
        Player p = e.getPlayer();
        String pName = RPUtil.PlayerToUUID(p.getName());
        Block last = b;
        Block current = b;
        Block next = null;
        Block first = null;
        String regionName = e.getLine(1);
        List<Integer> px = new LinkedList<Integer>();
        List<Integer> pz = new LinkedList<Integer>();
        Block bFirst1 = null;
        Block bFirst2 = null;
        List<Block> blocks = new LinkedList<Block>();
        int oldFacing = 0;
        int curFacing = 0;
        
        if (!RPConfig.isAllowedWorld(p)){
        	this.setErrorSign(e, RPLang.get("regionbuilder.region.worldnotallowed"));
            return;
        }                
        
        if (regionName == null || regionName.equals("")) {
        	regionName = RPUtil.nameGen(p.getName(), p.getWorld().getName());
        	if (regionName.length() > 16) {
                this.setErrorSign(e, RPLang.get("regionbuilder.autoname.error"));
                return;
            }
        }
        
        //region name conform
        regionName = regionName.replace("/", "|");  
        if (RedProtect.rm.getRegion(regionName, w) != null) {
            this.setErrorSign(e, RPLang.get("regionbuilder.regionname.existis"));
            return;
        }
        if (regionName.length() < 2 || regionName.length() > 16) {
            this.setErrorSign(e, RPLang.get("regionbuilder.regionname.invalid"));
            return;
        }
        if (regionName.contains(" ")) {
            this.setErrorSign(e, RPLang.get("regionbuilder.regionname.spaces"));
            return;
        }
        if (regionName.contains("@")) {
            this.setErrorSign(e, RPLang.get("regionbuilder.regionname.invalid.charac").replace("{charac}", "@"));
            return;
        }
            	

        int maxby = current.getY();
        int minby = current.getY();
        
        for (int i = 0; i < RPConfig.getInt("region-settings.max-scan"); ++i) {        	
            int nearbyCount = 0;
            int x = current.getX();
            int y = current.getY();
            int z = current.getZ(); 
            
            Block[] block = new Block[6]; 
            block[0] = w.getBlockAt(x, y + 1, z);
            block[1] = w.getBlockAt(x, y - 1, z);  
            block[2] = w.getBlockAt(x + 1, y, z);
            block[3] = w.getBlockAt(x - 1, y, z);
            block[4] = w.getBlockAt(x, y, z + 1);
            block[5] = w.getBlockAt(x, y, z - 1);  
            
            for (int bi = 0; bi < block.length; ++bi) {
            	
            	boolean validBlock = false;            	
                
                validBlock = (block[bi].getType().name().contains(RPConfig.getString("region-settings.block-id"))); 
                if (validBlock && !block[bi].getLocation().equals((Object)last.getLocation())) {                
                	++nearbyCount;
                    next = block[bi];
                    curFacing = bi % 4;
                    if (i == 1) {
                        if (nearbyCount == 1) {
                            bFirst1 = block[bi];
                        }
                        if (nearbyCount == 2) {
                            bFirst2 = block[bi];
                        }
                    } 
                }
            }
            if (nearbyCount == 1) {
                if (i != 0) {
                    blocks.add(current);
                    
                    //set max and min y blocks
                    if (current.getLocation().getBlockY() > maxby){
                    	maxby = current.getLocation().getBlockY();
                    }                    
                    if (current.getLocation().getBlockY() < minby){
                    	minby = current.getLocation().getBlockY();
                    }
                    
                    if (current.equals(first)) {
                        List<String> leaders = new LinkedList<String>();
                        leaders.add(pName);
                            if (owner1 == null) {
                                e.setLine(2, "--");
                                
                            } else if (pName.equals(owner1)) {
                            	e.setLine(2, "--");
                            	RPLang.sendMessage(p, "regionbuilder.sign.dontneed.name");
                            	
                            } else {
                            	leaders.add(owner1);
                            } 
                                    
                            
                            if (owner2 == null) {
                            	e.setLine(3, "--");
                            } else {
                            	if (!(owner2.startsWith("[") && owner2.endsWith("]"))){
                            		if (pName.equals(owner2)) {
                                    	e.setLine(3, "--");
                                    	RPLang.sendMessage(p, "regionbuilder.sign.dontneed.name");                                    
                                    } else {
                                    	leaders.add(owner2);                                
                                    }
                            	} else {
                            		e.setLine(3, "--");
                            	}                            	
                            }                            
                        
                        int[] rx = new int[px.size()];
                        int[] rz = new int[pz.size()];
                        int bl = 0;
                        for (int bx : px) {
                            rx[bl] = bx;
                            ++bl;
                        }
                        bl = 0;
                        for (int bz : pz) {
                            rz[bl] = bz;
                            ++bl;
                        }
                        
                        
                        Region region = new Region(regionName, new ArrayList<String>(), new ArrayList<String>(), leaders, rx, rz, 0, w.getMaxHeight(), 0, w.getName(), RPUtil.DateNow(), RPConfig.getDefFlagsValues(), "", 0, null);
                        
                        List<String> othersName = new ArrayList<String>();
                        Region otherrg = null;
                        List<Location> limitlocs = region.getLimitLocs(minby, maxby);
                                     
                        //check retangular region
                        for (Block bkloc:blocks){
                        	if (!limitlocs.contains(bkloc.getLocation())){
                        		this.setErrorSign(e, RPLang.get("regionbuilder.neeberetangle"));
                        		return;
                        	}
                        }
                        
                        //check regions inside region
                        for (Region r:RedProtect.rm.getRegionsByWorld(w)){
                        	if (r.getMaxMbrX() <= region.getMaxMbrX() && r.getMaxY() <= region.getMaxY() && r.getMaxMbrZ() <= region.getMaxMbrZ() && r.getMinMbrX() >= region.getMinMbrX() && r.getMinY() >= region.getMinY() && r.getMinMbrZ() >= region.getMinMbrZ()){
                        		if (!r.isLeader(p) && !p.hasPermission("redprotect.admin")){
                            		this.setErrorSign(e, RPLang.get("regionbuilder.region.overlapping").replace("{location}", "x: " + r.getCenterX() + ", z: " + r.getCenterZ()).replace("{player}", RPUtil.UUIDtoPlayer(r.getLeadersDesc())));
                                    return;
                            	}
                        		if (!othersName.contains(r.getName())){
                            		othersName.add(r.getName());
                            	}
                        	}
                        }
                                                
                        //check borders for other regions
                        for (Location loc:limitlocs){
                        	otherrg = RedProtect.rm.getTopRegion(loc);
                        	
                        	RedProtect.logger.debug("protection Block is: " + loc.getBlock().getType().name());
                        	
                    		if (otherrg != null){                    			
                            	if (!otherrg.isLeader(p) && !p.hasPermission("redprotect.admin")){
                            		this.setErrorSign(e, RPLang.get("regionbuilder.region.overlapping").replace("{location}", "x: " + otherrg.getCenterX() + ", z: " + otherrg.getCenterZ()).replace("{player}", RPUtil.UUIDtoPlayer(otherrg.getLeadersDesc())));
                                    return;
                            	}
                            	if (!othersName.contains(otherrg.getName())){
                            		othersName.add(otherrg.getName());
                            	}
                            }
                        }
                        
                        //check if same area
                        otherrg = RedProtect.rm.getTopRegion(region.getCenterLoc());
                        if (otherrg != null && otherrg.get4Points(current.getY()).equals(region.get4Points(current.getY()))){
                        	this.setErrorSign(e, RPLang.get("regionbuilder.region.overlapping").replace("{location}", "x: " + otherrg.getCenterX() + ", z: " + otherrg.getCenterZ()).replace("{player}", RPUtil.UUIDtoPlayer(otherrg.getLeadersDesc())));
                            return;
                        }
                                                                        
                        region.setPrior(RPUtil.getUpdatedPrior(region));
                        
                        int claimLimit = RedProtect.ph.getPlayerClaimLimit(p);
                        int claimused = RedProtect.rm.getPlayerRegions(p.getName(),w);  
                        boolean claimUnlimited = RedProtect.ph.hasPerm(p, "redprotect.limit.claim.unlimited");
                        if (claimused >= claimLimit && claimLimit >= 0) {
                            this.setErrorSign(e, RPLang.get("regionbuilder.claim.limit"));
                            return;
                        }
                        
                        int pLimit = RedProtect.ph.getPlayerBlockLimit(p);
                        boolean areaUnlimited = RedProtect.ph.hasPerm(p, "redprotect.limit.blocks.unlimited");
                        int totalArea = RedProtect.rm.getTotalRegionSize(pName);
                        if (pLimit >= 0 && totalArea + region.getArea() > pLimit) {
                            this.setErrorSign(e, RPLang.get("regionbuilder.reach.limit"));
                            return;
                        }
                        p.sendMessage(RPLang.get("general.color") + "------------------------------------");
                        p.sendMessage(RPLang.get("regionbuilder.claim.left") + (claimused+1) + RPLang.get("general.color") + "/" + (claimUnlimited ? RPLang.get("regionbuilder.area.unlimited") : claimLimit));
                        p.sendMessage(RPLang.get("regionbuilder.area.used") + " " + (totalArea + region.getArea()) + "\n" + 
                        RPLang.get("regionbuilder.area.left") + " " + (areaUnlimited ? RPLang.get("regionbuilder.area.unlimited") : (pLimit - (totalArea + region.getArea()))));
                        p.sendMessage(RPLang.get("cmdmanager.region.priority.set").replace("{region}", region.getName()) + " " + region.getPrior());
                        
                        if (othersName.size() > 0){
                        	p.sendMessage(RPLang.get("general.color") + "------------------------------------");
                        	p.sendMessage(RPLang.get("regionbuilder.overlapping"));
                        	p.sendMessage(RPLang.get("region.regions") + " " + othersName);
                        }
                        
                        //Drop types
                        if (owner2 != null && RPConfig.getBool("region-settings.claim-modes.allow-player-decide") && RPLang.containsValue(owner2)){                        	
                        	if (owner2.equalsIgnoreCase(RPLang.get("region.mode.drop"))){
                        		drop(b , blocks);
                        		RPLang.sendMessage(p, "regionbuilder.region.droped");
                        	}
                        	if (owner2.equalsIgnoreCase(RPLang.get("region.mode.remove"))){
                        		remove(b, blocks);
                        		RPLang.sendMessage(p, "regionbuilder.region.removed");
                        	}
                        	if (owner2.equalsIgnoreCase(RPLang.get("region.mode.give"))){
                        		give(b, p, blocks);
                        		RPLang.sendMessage(p, "regionbuilder.region.given");                        		
                        	}
                        } else {
                        	if (RPConfig.getString("region-settings.claim-modes.mode").equalsIgnoreCase("drop")) {
                                drop(b , blocks);
                            }
                            if (RPConfig.getString("region-settings.claim-modes.mode").equalsIgnoreCase("remove")) {
                                remove(b, blocks);
                            }
                            if (RPConfig.getString("region-settings.claim-modes.mode").equalsIgnoreCase("give")){
                            	give(b, p, blocks);
                            }
                        }
                        
                        
                        if (RedProtect.rm.getRegions(RPUtil.PlayerToUUID(p.getName()), p.getWorld()).size() == 0){
                        	p.sendMessage(RPLang.get("general.color") + "------------------------------------");
                        	p.sendMessage(RPLang.get("cmdmanager.region.firstwarning"));                        	
                        }                        
                        p.sendMessage(RPLang.get("general.color") + "------------------------------------");
                        
                        
                        this.r = region;
                        RedProtect.logger.addLog("(World "+region.getWorld()+") Player "+p.getName()+" CREATED region "+region.getName());
                        return;
                    }
                }
            }
            else if (i == 1 && nearbyCount == 2) {
            	//check other regions on blocks
            	Region rcurrent = RedProtect.rm.getTopRegion(current.getLocation());
            	if (rcurrent != null && !rcurrent.canBuild(p)){
            		this.setErrorSign(e, RPLang.get("regionbuilder.region.overlapping").replace("{location}", "x: " + rcurrent.getCenterX() + ", z: " + rcurrent.getCenterZ()).replace("{player}", RPUtil.UUIDtoPlayer(rcurrent.getLeadersDesc())));
            		return;
            	}
                blocks.add(current);
                first = current;
                int x2 = bFirst1.getX();
                int z2 = bFirst1.getZ();
                int x3 = bFirst2.getX();
                int z3 = bFirst2.getZ();
                int distx = Math.abs(x2 - x3);
                int distz = Math.abs(z2 - z3);
                if ((distx != 2 || distz != 0) && (distz != 2 || distx != 0)) {
                    px.add(current.getX());
                    pz.add(current.getZ());
                }
            }
            else if (i != 0) {
                this.setErrorSign(e, RPLang.get("regionbuilder.area.error").replace("{area}", "(x: " + current.getX() + ", y: " + current.getY() + ", z: " + current.getZ() + ")"));
                return;
            }
            if (oldFacing != curFacing && i > 1) {
                px.add(current.getX());
                pz.add(current.getZ());
            }
            last = current;
            if (next == null) {
                this.setErrorSign(e, RPLang.get("regionbuilder.area.next"));
                return;
            }
            current = next;
            oldFacing = curFacing;
        }
        String maxsize = String.valueOf(RPConfig.getInt("region-settings.max-scan")/2);
        this.setErrorSign(e, RPLang.get("regionbuilder.area.toobig").replace("{maxsize}", maxsize + "x" + maxsize));
    }
    
    private void drop(Block sign, List<Block> blocks){
    	sign.breakNaturally();
        for (Block rb : blocks) {
            rb.breakNaturally();
        }
    }
    
    private void remove(Block sign, List<Block> blocks){
    	sign.breakNaturally();
        for (Block rb : blocks) {
            rb.setType(Material.AIR);
        }
    }
    
    private void give(Block sign, Player p, List<Block> blocks){
    	HashMap<Integer, ItemStack> left = p.getInventory().addItem(new ItemStack(RPConfig.getMaterial("region-settings.block-id"),blocks.size()));
    	if (!left.isEmpty()){
    		p.getWorld().dropItem(p.getLocation(), new ItemStack(RPConfig.getMaterial("region-settings.block-id"),left.get(0).getAmount()-1));
    	}
    	p.updateInventory();
    	sign.breakNaturally();
    	for (Block rb : blocks) {
            rb.setType(Material.AIR);
        }
    }
}
