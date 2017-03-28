package br.net.fabiozumbi12.redprotect;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import br.net.fabiozumbi12.redprotect.config.RPLang;

public class RedefineRegionBuilder extends RegionBuilder{
	
	private boolean checkID(Region newr, Region oldr){
		return newr.getID().equals(oldr.getID());
	}
	
    @SuppressWarnings("deprecation")
	public RedefineRegionBuilder(Player p, Region old, Location<World> l1, Location<World> l2) {
        if (l1 == null || l2 == null) {
            this.setError(p, RPLang.get("regionbuilder.selection.notset"));
            return;
        }
        World w = p.getWorld();
        
        int miny = l1.getBlockY();
        int maxy = l2.getBlockY();
        if (RedProtect.cfgs.getBool("region-settings.autoexpandvert-ondefine")){
        	miny = 0;
        	maxy = 256;
        }
        
        Region region = new Region(old.getName(), old.getAdmins(), old.getMembers(), old.getLeaders(), new int[] { l1.getBlockX(), l1.getBlockX(), l2.getBlockX(), l2.getBlockX() }, new int[] { l1.getBlockZ(), l1.getBlockZ(), l2.getBlockZ(), l2.getBlockZ() }, miny, maxy, old.getPrior(), w.getName(), old.getDate(), old.flags, old.getWelcome(), old.getValue(), old.getTPPoint(), old.canDelete());
        
        region.setPrior(RPUtil.getUpdatedPrior(region));    
        
        String pName = p.getUniqueId().toString();
        if (!RedProtect.OnlineMode){
        	pName = p.getName().toLowerCase();
    	}
        
        int claimLimit = RedProtect.ph.getPlayerClaimLimit(p);
        int claimused = RedProtect.rm.getPlayerRegions(p.getName(),region.getWorld());    
        boolean claimUnlimited = RedProtect.ph.hasPerm(p, "redprotect.limit.claim.unlimited");
        if (claimused >= claimLimit && claimLimit >= 0 && !claimUnlimited) {
        	this.setError(p, RPLang.get("regionbuilder.claim.limit"));
            return;
        }
        
        int pLimit = RedProtect.ph.getPlayerBlockLimit(p);
        int totalArea = RedProtect.rm.getTotalRegionSize(pName);
        boolean areaUnlimited = RedProtect.ph.hasPerm(p, "redprotect.limit.blocks.unlimited");
        int regionarea = RPUtil.simuleTotalRegionSize(RPUtil.PlayerToUUID(p.getName()), region);
        int actualArea = totalArea+regionarea;        
        if (pLimit >= 0 && actualArea > pLimit && !areaUnlimited) {
        	this.setError(p, RPLang.get("regionbuilder.reach.limit"));
            return;
        }
        
        List<String> othersName = new ArrayList<String>();
        Region otherrg = null;

        //check if same area
        otherrg = RedProtect.rm.getTopRegion(region.getCenterLoc());
        if (otherrg != null && !checkID(region, otherrg) && otherrg.get4Points(region.getCenterY()).equals(region.get4Points(region.getCenterY()))){
        	this.setError(p, RPLang.get("regionbuilder.region.overlapping").replace("{location}", "x: " + otherrg.getCenterX() + ", z: " + otherrg.getCenterZ()).replace("{player}", RPUtil.UUIDtoPlayer(otherrg.getLeadersDesc())));
        	return;
        }
        
        boolean hasAny = false;
        
        //check regions inside region
        for (Region r:RedProtect.rm.getRegionsByWorld(p.getWorld())){
        	if (r.getMaxMbrX() <= region.getMaxMbrX() && r.getMaxY() <= region.getMaxY() && r.getMaxMbrZ() <= region.getMaxMbrZ() && r.getMinMbrX() >= region.getMinMbrX() && r.getMinY() >= region.getMinY() && r.getMinMbrZ() >= region.getMinMbrZ()){
        		if (!r.isLeader(p) && !RedProtect.ph.hasGenPerm(p, "redprotect.bypass")){
        			this.setError(p, RPLang.get("regionbuilder.region.overlapping").replace("{location}", "x: " + r.getCenterX() + ", z: " + r.getCenterZ()).replace("{player}", RPUtil.UUIDtoPlayer(otherrg.getLeadersDesc())));
                	return;
            	}
        		if (checkID(region, r)){
            		hasAny = true;
            		continue;
            	}
        		if (!othersName.contains(r.getName())){
            		othersName.add(r.getName());
            	}
        	}
        }
        
        //check borders for other regions
        List<Location<World>> limitlocs = region.getLimitLocs(region.getMinY(), region.getMaxY(), true);
        for (Location<World> loc:limitlocs){
        	
        	/*
        	//check regions near
        	if (!RPUtil.canBuildNear(p, loc)){
            	return;    	
            }*/
        	
        	otherrg = RedProtect.rm.getTopRegion(loc);        	
        	RedProtect.logger.debug("blocks", "protection Block is: " + loc.getBlock().getType().getName());
        	
    		if (otherrg != null){        
    			if (checkID(region, otherrg)){
            		hasAny = true;
            		continue;
            	}
    			if (!otherrg.isLeader(p) && !RedProtect.ph.hasGenPerm(p, "redprotect.bypass")){
            		this.setError(p, RPLang.get("regionbuilder.region.overlapping").replace("{location}", "x: " + otherrg.getCenterX() + ", z: " + otherrg.getCenterZ()).replace("{player}", RPUtil.UUIDtoPlayer(otherrg.getLeadersDesc())));
                    return;
            	}
            	if (!othersName.contains(otherrg.getName())){
            		othersName.add(otherrg.getName());
            	}
            }
        }
        
        if (!hasAny){
        	this.setError(p, RPLang.get("regionbuilder.needinside"));
        	return;
        }
        
        if (RedProtect.cfgs.getEcoBool("claim-cost-per-block.enable") && !RedProtect.ph.hasGenPerm(p, "redprotect.eco.bypass")){
        	UniqueAccount acc = RedProtect.econ.getOrCreateAccount(p.getUniqueId()).get();
        	Double peco = acc.getBalance(RedProtect.econ.getDefaultCurrency()).doubleValue();
        	long reco = (region.getArea() <= old.getArea() ? 0 : region.getArea()-old.getArea())* RedProtect.cfgs.getEcoInt("claim-cost-per-block.cost-per-block");
        	
        	if (!RedProtect.cfgs.getEcoBool("claim-cost-per-block.y-is-free")){
        		reco = reco * Math.abs(region.getMaxY()-region.getMinY());
        	}
        	
        	if (peco >= reco){
        		acc.withdraw(RedProtect.econ.getDefaultCurrency(), BigDecimal.valueOf(reco), Cause.of(NamedCause.simulated(p)));                        		
        		p.sendMessage(RPUtil.toText(RPLang.get("economy.region.claimed").replace("{price}", RedProtect.cfgs.getEcoString("economy-symbol")+reco+" "+RedProtect.cfgs.getEcoString("economy-name"))));
        	} else {
        		this.setError(p, RPLang.get("regionbuilder.notenought.money").replace("{price}", RedProtect.cfgs.getEcoString("economy-symbol")+reco));
        		return;
        	}
        }
        
        p.sendMessage(RPUtil.toText(RPLang.get("general.color") + "------------------------------------"));
        p.sendMessage(RPUtil.toText(RPLang.get("regionbuilder.claim.left") + (claimused+1) + RPLang.get("general.color") + "/" + (claimUnlimited ? RPLang.get("regionbuilder.area.unlimited") : claimLimit)));
        p.sendMessage(RPUtil.toText(RPLang.get("regionbuilder.area.used") + " " + (regionarea == 0 ? "&a"+regionarea:"&c- "+regionarea) + "\n" +
        RPLang.get("regionbuilder.area.left") + " " + (areaUnlimited ? RPLang.get("regionbuilder.area.unlimited") : (pLimit - actualArea))));
        p.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.region.priority.set").replace("{region}", region.getName()) + " " + region.getPrior()));
        p.sendMessage(RPUtil.toText(RPLang.get("general.color") + "------------------------------------"));                
        if (othersName.size() > 0){
        	p.sendMessage(RPUtil.toText(RPLang.get("general.color") + "------------------------------------"));
        	p.sendMessage(RPUtil.toText(RPLang.get("regionbuilder.overlapping")));
        	p.sendMessage(RPUtil.toText(RPLang.get("region.regions") + " " + othersName));
        }
        
        this.r = region;
        RedProtect.logger.addLog("(World "+region.getWorld()+") Player "+p.getName()+" REDEFINED region "+region.getName());
        return;
    }
}
