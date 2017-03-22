package br.net.fabiozumbi12.redprotect;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import br.net.fabiozumbi12.redprotect.config.RPLang;

public class EncompassRegionBuilder extends RegionBuilder{

    public EncompassRegionBuilder(ChangeSignEvent e) { 	
        String owner1 = RPUtil.PlayerToUUID(e.getText().asList().get(2).toPlain());
        String owner2 = RPUtil.PlayerToUUID(e.getText().asList().get(3).toPlain());
        World w = e.getTargetTile().getLocation().getExtent();
        BlockSnapshot b = w.createSnapshot(e.getTargetTile().getLocation().getBlockPosition());
        Player p = e.getCause().first(Player.class).get();
        Sign sign = e.getTargetTile();        
        String pName = RPUtil.PlayerToUUID(p.getName());
        BlockSnapshot last = b;
        BlockSnapshot current = b;
        BlockSnapshot next = null;
        BlockSnapshot first = null;
        String regionName = e.getText().asList().get(1).toPlain();
        LinkedList<Integer> px = new LinkedList<Integer>();
        LinkedList<Integer> pz = new LinkedList<Integer>();
        BlockSnapshot bFirst1 = null;
        BlockSnapshot bFirst2 = null;
        List<BlockSnapshot> blocks = new LinkedList<BlockSnapshot>();
        int oldFacing = 0;
        int curFacing = 0;
        
        if (!RedProtect.cfgs.isAllowedWorld(p)){
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
            	
        int maxby = current.getLocation().get().getBlockY();
        int minby = current.getLocation().get().getBlockY();
        
        for (int i = 0; i < RedProtect.cfgs.getInt("region-settings.max-scan"); ++i) {        	
            int nearbyCount = 0;
            int x = current.getLocation().get().getBlockX();
            int y = current.getLocation().get().getBlockY();
            int z = current.getLocation().get().getBlockZ();
            
            BlockSnapshot[] block = new BlockSnapshot[6];    
            
            block[0] = w.createSnapshot(x + 1, y, z);
            block[1] = w.createSnapshot(x - 1, y, z);
            block[2] = w.createSnapshot(x, y, z + 1);
            block[3] = w.createSnapshot(x, y, z - 1);   
            block[4] = w.createSnapshot(x, y + 1, z);
            block[5] = w.createSnapshot(x, y - 1, z); 
            
            for (int bi = 0; bi < block.length; ++bi) {
            	
            	boolean validBlock = false;            	
                
                validBlock = (block[bi].getState().getType().getName().contains(RedProtect.cfgs.getString("region-settings.block-id").toLowerCase())); 
                if (validBlock && !block[bi].getLocation().equals(last.getLocation())) {                
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
                    if (current.getLocation().get().getBlockY() > maxby){
                    	maxby = current.getLocation().get().getBlockY();
                    }                    
                    if (current.getLocation().get().getBlockY() < minby){
                    	minby = current.getLocation().get().getBlockY();
                    }                    
                    
                    if (current.equals(first)) {
                    	LinkedList<String> leaders = new LinkedList<String>();
                    	leaders.add(pName);
                            if (owner1 == null) {
                            	sign.offer(e.getText().set(sign.getValue(Keys.SIGN_LINES).get().set(2, RPUtil.toText("--"))));
                            	
                            } else if (pName.equals(owner1)) {
                            	sign.offer(e.getText().set(sign.getValue(Keys.SIGN_LINES).get().set(2, RPUtil.toText("--"))));
                            	RPLang.sendMessage(p, "regionbuilder.sign.dontneed.name");
                            	
                            } else {
                            	leaders.add(owner1);
                            } 
                                    
                            
                            if (owner2 == null) {
                            	sign.offer(e.getText().set(sign.getValue(Keys.SIGN_LINES).get().set(3, RPUtil.toText("--"))));
                            } else {
                            	if (!(owner2.startsWith("[") && owner2.endsWith("]"))){
                            		if (pName.equals(owner2)) {
                                    	sign.offer(e.getText().set(sign.getValue(Keys.SIGN_LINES).get().set(3, RPUtil.toText("--"))));
                                    	RPLang.sendMessage(p, "regionbuilder.sign.dontneed.name");
                            		} else {
                                    	leaders.add(owner2);                                
                                    }
                            	} else {
                            		sign.offer(e.getText().set(sign.getValue(Keys.SIGN_LINES).get().set(3, RPUtil.toText("--"))));
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
                        
                        Region region = new Region(regionName, new LinkedList<String>(), new LinkedList<String>(), leaders, rx, rz, 0, 256, 0, w.getName(), RPUtil.DateNow(), RedProtect.cfgs.getDefFlagsValues(), "", 0, null, true);
                        
                        List<String> othersName = new ArrayList<String>();
                        Region otherrg = null;
                        List<Location<World>> limitlocs = region.getLimitLocs(minby, maxby, false);
                                   
                        //check retangular region
                        for (BlockSnapshot bkloc:blocks){
                        	if (!limitlocs.contains(bkloc.getLocation().get())){
                        		this.setErrorSign(e, RPLang.get("regionbuilder.neeberetangle"));
                        		return;
                        	}
                        }
                        
                        //check regions inside region
                        for (Region r:RedProtect.rm.getRegionsByWorld(w)){
                        	if (r.getMaxMbrX() <= region.getMaxMbrX() && r.getMaxY() <= region.getMaxY() && r.getMaxMbrZ() <= region.getMaxMbrZ() && r.getMinMbrX() >= region.getMinMbrX() && r.getMinY() >= region.getMinY() && r.getMinMbrZ() >= region.getMinMbrZ()){
                        		if (!r.isLeader(p) && !RedProtect.ph.hasGenPerm(p, "redprotect.bypass")){
                            		this.setErrorSign(e, RPLang.get("regionbuilder.region.overlapping").replace("{location}", "x: " + r.getCenterX() + ", z: " + r.getCenterZ()).replace("{player}", RPUtil.UUIDtoPlayer(r.getLeadersDesc())));
                                    return;
                            	}
                        		if (!othersName.contains(r.getName())){
                            		othersName.add(r.getName());
                            	}
                        	}
                        }
                        
                        //check borders for other regions
                        for (Location<World> loc:limitlocs){
                        	otherrg = RedProtect.rm.getTopRegion(loc);
                        	
                        	RedProtect.logger.debug("default","protection Block is: " + loc.getBlock().getType().getName());
                        	
                    		if (otherrg != null){                    			
                    			if (!otherrg.isLeader(p) && !RedProtect.ph.hasGenPerm(p, "redprotect.admin")){
                            		this.setErrorSign(e, RPLang.get("regionbuilder.region.overlapping").replace("{location}", "x: " + otherrg.getCenterX() + ", z: " + otherrg.getCenterZ()).replace("{player}", RPUtil.UUIDtoPlayer(r.getLeadersDesc())));
                                    return;
                            	}
                            	if (!othersName.contains(otherrg.getName())){
                            		othersName.add(otherrg.getName());
                            	}
                            }
                        }  
                        
                        //check if same area
                        otherrg = RedProtect.rm.getTopRegion(region.getCenterLoc());
                        if (otherrg != null && otherrg.get4Points(current.getLocation().get().getBlockY()).equals(region.get4Points(current.getLocation().get().getBlockY()))){
                        	this.setErrorSign(e, RPLang.get("regionbuilder.region.overlapping").replace("{location}", "x: " + otherrg.getCenterX() + ", z: " + otherrg.getCenterZ()).replace("{player}", RPUtil.UUIDtoPlayer(otherrg.getLeadersDesc())));
                            return;
                        }
                        
                        region.setPrior(RPUtil.getUpdatedPrior(region));
                        
                        int claimLimit = RedProtect.ph.getPlayerClaimLimit(p);
                        int claimused = RedProtect.rm.getRegions(RPUtil.PlayerToUUID(p.getName()),w).size();
                        boolean claimUnlimited = RedProtect.ph.hasPerm(p, "redprotect.limit.claim.unlimited");
                        if (claimused >= claimLimit && claimLimit != -1) {
                            this.setErrorSign(e, RPLang.get("regionbuilder.claim.limit"));
                            return;
                        }
                        
                        int pLimit = RedProtect.ph.getPlayerLimit(p);
                        boolean areaUnlimited = RedProtect.ph.hasPerm(p, "redprotect.limit.blocks.unlimited");
                        int totalArea = RedProtect.rm.getTotalRegionSize(pName);
                        int regionarea = RPUtil.simuleTotalRegionSize(RPUtil.PlayerToUUID(p.getName()), region);
                        int actualArea = totalArea+regionarea;
                        if (pLimit >= 0 && actualArea > pLimit) {
                            this.setErrorSign(e, RPLang.get("regionbuilder.reach.limit"));
                            return;
                        }
                        
                        if (RedProtect.cfgs.getEcoBool("claim-cost-per-block.enable") && !RedProtect.ph.hasGenPerm(p, "redprotect.eco.bypass")){
                        	UniqueAccount acc = RedProtect.econ.getOrCreateAccount(p.getUniqueId()).get();
                        	Double peco = acc.getBalance(RedProtect.econ.getDefaultCurrency()).doubleValue();
                        	long reco = region.getArea() * RedProtect.cfgs.getEcoInt("claim-cost-per-block.cost-per-block");
                        	
                        	if (!RedProtect.cfgs.getEcoBool("claim-cost-per-block.y-is-free")){
                        		reco = reco * Math.abs(region.getMaxY()-region.getMinY());
                        	}
                        	
                        	if (peco >= reco){
                        		acc.withdraw(RedProtect.econ.getDefaultCurrency(), BigDecimal.valueOf(reco), Cause.of(NamedCause.simulated(p)));                        		
                        		p.sendMessage(RPUtil.toText(RPLang.get("economy.region.claimed").replace("{price}", RedProtect.cfgs.getEcoString("economy-symbol")+reco+" "+RedProtect.cfgs.getEcoString("economy-name"))));
                        	} else {
                        		this.setErrorSign(e, RPLang.get("regionbuilder.notenought.money").replace("{price}", RedProtect.cfgs.getEcoString("economy-symbol")+reco));
                        		return;
                        	}
                        }
                        
                        p.sendMessage(RPUtil.toText(RPLang.get("general.color") + "------------------------------------"));
                        p.sendMessage(RPUtil.toText(RPLang.get("regionbuilder.claim.left") + (claimused+1) + RPLang.get("general.color") + "/" + (claimUnlimited ? RPLang.get("regionbuilder.area.unlimited") : claimLimit)));
                        p.sendMessage(RPUtil.toText(RPLang.get("regionbuilder.area.used") + " " + (regionarea == 0 ? "&a"+regionarea:"&c- "+regionarea) + "\n" + 
                        RPLang.get("regionbuilder.area.left") + " " + (areaUnlimited ? RPLang.get("regionbuilder.area.unlimited") : (pLimit - (totalArea + region.getArea())))));
                        p.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.region.priority.set").replace("{region}", region.getName()) + " " + region.getPrior()));
                        p.sendMessage(RPUtil.toText(RPLang.get("general.color") + "------------------------------------"));
                        if (othersName.size() > 0){                        	
                        	p.sendMessage(RPUtil.toText(RPLang.get("regionbuilder.overlapping")));
                        	p.sendMessage(RPUtil.toText(RPLang.get("region.regions") + " " + othersName));
                        	p.sendMessage(RPUtil.toText(RPLang.get("general.color") + "------------------------------------"));
                        }
                        
                        //Drop types
                        if (owner2 != null && RedProtect.cfgs.getBool("region-settings.claim-modes.allow-player-decide") && RPLang.containsValue(owner2)){                        	
                        	/*if (owner2.equalsIgnoreCase(RPLang.get("region.mode.drop"))){
                        		drop(b , blocks, p);
                        		RPLang.sendMessage(p, "regionbuilder.region.droped");
                        	}*/
                        	if (owner2.equalsIgnoreCase(RPLang.get("region.mode.remove"))){
                        		remove(b, blocks, p);
                        		RPLang.sendMessage(p, "regionbuilder.region.removed");
                        	}
                        	if (owner2.equalsIgnoreCase(RPLang.get("region.mode.give"))){
                        		give(b, p, blocks);
                        		RPLang.sendMessage(p, "regionbuilder.region.given");                        		
                        	}
                        } else {
                        	/*if (RedProtect.cfgs.getString("region-settings.claim-modes.mode").equalsIgnoreCase("drop")) {
                                drop(b , blocks, p);
                            }*/
                            if (RedProtect.cfgs.getString("region-settings.claim-modes.mode").equalsIgnoreCase("remove")) {
                                remove(b, blocks, p);
                            }
                            if (RedProtect.cfgs.getString("region-settings.claim-modes.mode").equalsIgnoreCase("give")){
                            	give(b, p, blocks);
                            }
                        }
                                                                        
                        if (RedProtect.rm.getRegions(RPUtil.PlayerToUUID(p.getName()), p.getWorld()).size() == 0){                        	
                        	p.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.region.firstwarning")));    
                        	p.sendMessage(RPUtil.toText(RPLang.get("general.color") + "------------------------------------"));
                        }                        
                        
                        this.r = region;
                        RedProtect.logger.addLog("(World "+region.getWorld()+") Player "+p.getName()+" CREATED region "+region.getName());
                        return;
                    }
                }
            }
            else if (i == 1 && nearbyCount == 2) {
            	//check other regions on blocks
            	Region rcurrent = RedProtect.rm.getTopRegion(current.getLocation().get());
            	if (rcurrent != null && !rcurrent.canBuild(p)){
            		this.setErrorSign(e, RPLang.get("regionbuilder.region.overlapping").replace("{location}", "x: " + rcurrent.getCenterX() + ", z: " + rcurrent.getCenterZ()).replace("{player}", RPUtil.UUIDtoPlayer(rcurrent.getLeadersDesc())));
            		return;
            	}
                blocks.add(current);
                first = current;
                int x2 = bFirst1.getLocation().get().getBlockX();
                int z2 = bFirst1.getLocation().get().getBlockZ();
                int x3 = bFirst2.getLocation().get().getBlockX();
                int z3 = bFirst2.getLocation().get().getBlockZ();
                int distx = Math.abs(x2 - x3);
                int distz = Math.abs(z2 - z3);
                if ((distx != 2 || distz != 0) && (distz != 2 || distx != 0)) {
                    px.add(current.getLocation().get().getBlockX());
                    pz.add(current.getLocation().get().getBlockZ());
                }
            }
            else if (i != 0) {
                this.setErrorSign(e, RPLang.get("regionbuilder.area.error").replace("{area}", "(x: " + current.getLocation().get().getBlockX() + ", y: " + current.getLocation().get().getBlockY() + ", z: " + current.getLocation().get().getBlockZ() + ")"));
                Location<World> newbl = current.getLocation().get();  
                newbl.add(0, 1, 0);
                newbl.setBlockType(BlockTypes.STANDING_SIGN, Cause.of(NamedCause.simulated(p)));
                BlockSnapshot newb = newbl.createSnapshot();
                newb.get(Keys.SIGN_LINES).get().set(0, Text.of(TextColors.RED,"xxxxxxxxxxxxxx"));
                newb.get(Keys.SIGN_LINES).get().set(1, Text.of(RPLang.get("_redprotect.prefix")));
                newb.get(Keys.SIGN_LINES).get().set(2, Text.of(RPLang.get("blocklistener.postsign.error")));
                newb.get(Keys.SIGN_LINES).get().set(3, Text.of(TextColors.RED,"xxxxxxxxxxxxxx"));
                return;
            }
            if (oldFacing != curFacing && i > 1) {
                px.add(current.getLocation().get().getBlockX());
                pz.add(current.getLocation().get().getBlockZ());
            }
            last = current;
            if (next == null) {
                this.setErrorSign(e, RPLang.get("regionbuilder.area.next"));
                return;
            }
            current = next;
            oldFacing = curFacing;
        }
        String maxsize = String.valueOf(RedProtect.cfgs.getInt("region-settings.max-scan")/2);
        this.setErrorSign(e, RPLang.get("regionbuilder.area.toobig").replace("{maxsize}", maxsize + "x" + maxsize));
    }
    

    /*private void drop(BlockSnapshot sign, List<BlockSnapshot> blocks, Player p){
    	p.getWorld().digBlock(sign.getPosition(), Cause.of(NamedCause.owner(RedProtect.plugin)));
        for (BlockSnapshot rb : blocks) {
        	p.getWorld().digBlock(rb.getPosition(), Cause.of(NamedCause.owner(RedProtect.plugin)));
        }
    }*/
    
    private void remove(BlockSnapshot sign, List<BlockSnapshot> blocks, Player p){
    	//p.getWorld().digBlock(sign.getPosition(), Cause.of(NamedCause.owner(RedProtect.plugin)));
        for (BlockSnapshot rb : blocks) {
            rb.getLocation().get().removeBlock(Cause.of(NamedCause.owner(RedProtect.plugin)));
        }
    }
    
    private void give(BlockSnapshot sign, Player p, List<BlockSnapshot> blocks){
    	Collection<ItemStackSnapshot> rejected = new ArrayList<ItemStackSnapshot>();
    	for (BlockSnapshot bb:blocks){
    		rejected.addAll(p.getInventory().offer(ItemStack.builder().fromBlockSnapshot(bb).build()).getRejectedItems());    		
    	}
    	
    	//drop rejected
    	for (ItemStackSnapshot bb: rejected){
    		p.getWorld().digBlockWith(p.getLocation().getBlockPosition(), bb.createStack(), Cause.of(NamedCause.owner(RedProtect.plugin)));
    	}
    	
    	//p.getWorld().digBlock(sign.getPosition(), Cause.of(NamedCause.simulated(p)));
    	for (BlockSnapshot rb : blocks) {
    		rb.getLocation().get().removeBlock(Cause.of(NamedCause.owner(RedProtect.plugin)));
        }
    }
}
