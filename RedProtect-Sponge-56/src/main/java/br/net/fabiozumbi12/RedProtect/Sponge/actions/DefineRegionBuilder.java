package br.net.fabiozumbi12.RedProtect.Sponge.actions;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import br.net.fabiozumbi12.RedProtect.Sponge.RPUtil;
import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import br.net.fabiozumbi12.RedProtect.Sponge.Region;
import br.net.fabiozumbi12.RedProtect.Sponge.RegionBuilder;
import br.net.fabiozumbi12.RedProtect.Sponge.config.RPLang;

public class DefineRegionBuilder extends RegionBuilder{
	
	public DefineRegionBuilder(Player p, Location<World> loc1, Location<World> loc2, String regionName, String leader, List<String> leaders, boolean admin) {  
    	if (!RedProtect.get().cfgs.isAllowedWorld(p)){
        	this.setError(p, RPLang.get("regionbuilder.region.worldnotallowed"));
            return;
        }
    	
    	//region leader
        String pName = RPUtil.PlayerToUUID(p.getName());
        //for region name
        String pRName = RPUtil.UUIDtoPlayer(p.getName());
        
        String wmsg = "";
        if (leader.equals(RedProtect.get().cfgs.getString("region-settings.default-leader"))){
        	pName = leader;
        	pRName = leader;
        	wmsg = "hide ";
        }
        
        if (regionName.equals("")) {
            int i = 0;            
            regionName = RPUtil.StripName(pRName)+"_"+0;            
            while (RedProtect.get().rm.getRegion(regionName, p.getWorld()) != null) {
            	++i;
            	regionName = RPUtil.StripName(pRName)+"_"+i;   
            }            
            if (regionName.length() > 16) {
            	RPLang.sendMessage(p, "regionbuilder.autoname.error");
                return;
            }
        }
        if (regionName.contains("@")) {
        	RPLang.sendMessage(p, RPLang.get("regionbuilder.regionname.invalid.charac").replace("{charac}", "@"));
            return;
        }
        if (loc1 == null || loc2 == null) {
        	RPLang.sendMessage(p, "regionbuilder.selection.notset");
            return;
        }

        //check if distance allowed
        if (loc1.getPosition().distanceSquared(loc2.getPosition()) > RedProtect.get().cfgs.getInt("region-settings.define-max-distance") && !RedProtect.get().ph.hasPerm(p,"redprotect.bypass.define-max-distance")){
            Double dist = loc1.getPosition().distanceSquared(loc2.getPosition());
            RPLang.sendMessage(p, String.format(RPLang.get("regionbuilder.selection.maxdefine"), RedProtect.get().cfgs.getInt("region-settings.define-max-distance"), dist.intValue()));
            return;
        }
        
        //region name conform
        regionName = regionName.replace(File.pathSeparator, "|");  
        if (RedProtect.get().rm.getRegion(regionName, p.getWorld()) != null) {
        	RPLang.sendMessage(p, "regionbuilder.regionname.existis");
            return;
        }
        if (regionName.length() < 3 || regionName.length() > 16) {
        	RPLang.sendMessage(p, "regionbuilder.regionname.invalid");
            return;
        }

        leaders.add(leader);
        if (!pName.equals(leader)) {
        	leaders.add(pName);
        }
        
        int miny = loc1.getBlockY();
        int maxy = loc2.getBlockY();
        if (RedProtect.get().cfgs.getBool("region-settings.autoexpandvert-ondefine")){
        	miny = 0;
        	maxy = p.getWorld().getBlockMax().getY();
        }
        
        Region region = new Region(regionName, new ArrayList<>(), new ArrayList<>(), leaders, new int[] { loc1.getBlockX(), loc1.getBlockX(), loc2.getBlockX(), loc2.getBlockX() }, new int[] { loc1.getBlockZ(), loc1.getBlockZ(), loc2.getBlockZ(), loc2.getBlockZ() }, miny, maxy, 0, p.getWorld().getName(), RPUtil.DateNow(), RedProtect.get().cfgs.getDefFlagsValues(), wmsg, 0, null, true);
        
        region.setPrior(RPUtil.getUpdatedPrior(region));            
            	
        int claimLimit = RedProtect.get().ph.getPlayerClaimLimit(p);
        int claimused = RedProtect.get().rm.getPlayerRegions(RPUtil.PlayerToUUID(p.getName()),p.getWorld());
        boolean claimUnlimited = RedProtect.get().ph.hasPerm(p, "redprotect.limit.claim.unlimited");
        if (claimused >= claimLimit && claimLimit != -1) {
        	this.setError(p, RPLang.get("regionbuilder.claim.limit"));
            return;
        }
        
        int pLimit = RedProtect.get().ph.getPlayerBlockLimit(p);
        int totalArea = RedProtect.get().rm.getTotalRegionSize(pName, p.getWorld().getName());
        boolean areaUnlimited = RedProtect.get().ph.hasPerm(p, "redprotect.limit.blocks.unlimited");
        int regionarea = RPUtil.simuleTotalRegionSize(RPUtil.PlayerToUUID(p.getName()), region);
        int actualArea = 0;
        if (regionarea > 0){
        	actualArea = totalArea+regionarea;
        }  
        if (pLimit >= 0 && actualArea > pLimit && !areaUnlimited) {
        	this.setError(p, RPLang.get("regionbuilder.reach.limit"));
            return;
        }
        
        List<String> othersName = new ArrayList<>();
        Region otherrg = null;
        
        //check if same area
        otherrg = RedProtect.get().rm.getTopRegion(region.getCenterLoc());
        if (otherrg != null && otherrg.get4Points(region.getCenterY()).equals(region.get4Points(region.getCenterY()))){
        	this.setError(p, RPLang.get("regionbuilder.region.overlapping").replace("{location}", "x: " + otherrg.getCenterX() + ", z: " + otherrg.getCenterZ()).replace("{player}", RPUtil.UUIDtoPlayer(otherrg.getLeadersDesc())));
        	return;
        }
        
        //check regions inside region
        for (Region r:RedProtect.get().rm.getRegionsByWorld(p.getWorld())){
        	if (r.getMaxMbrX() <= region.getMaxMbrX() && r.getMaxY() <= region.getMaxY() && r.getMaxMbrZ() <= region.getMaxMbrZ() && r.getMinMbrX() >= region.getMinMbrX() && r.getMinY() >= region.getMinY() && r.getMinMbrZ() >= region.getMinMbrZ()){
        		if (!r.isLeader(p) && !RedProtect.get().ph.hasGenPerm(p, "redprotect.bypass")){
        			this.setError(p, RPLang.get("regionbuilder.region.overlapping").replace("{location}", "x: " + r.getCenterX() + ", z: " + r.getCenterZ()).replace("{player}", RPUtil.UUIDtoPlayer(otherrg.getLeadersDesc())));
                	return;
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
        	
        	otherrg = RedProtect.get().rm.getTopRegion(loc);        	
        	RedProtect.get().logger.debug("blocks", "protection Block is: " + loc.getBlock().getType().getName());
        	
    		if (otherrg != null){                    			
    			if (!otherrg.isLeader(p) && !RedProtect.get().ph.hasGenPerm(p, "redprotect.bypass")){
            		this.setError(p, RPLang.get("regionbuilder.region.overlapping").replace("{location}", "x: " + otherrg.getCenterX() + ", z: " + otherrg.getCenterZ()).replace("{player}", RPUtil.UUIDtoPlayer(otherrg.getLeadersDesc())));
                    return;
            	}
            	if (!othersName.contains(otherrg.getName())){
            		othersName.add(otherrg.getName());
            	}
            }
        }
        
        if (RedProtect.get().cfgs.getEcoBool("claim-cost-per-block.enable") && !RedProtect.get().ph.hasGenPerm(p, "redprotect.eco.bypass")){
        	UniqueAccount acc = RedProtect.get().econ.getOrCreateAccount(p.getUniqueId()).get();
        	Double peco = acc.getBalance(RedProtect.get().econ.getDefaultCurrency()).doubleValue();
        	long reco = region.getArea() * RedProtect.get().cfgs.getEcoInt("claim-cost-per-block.cost-per-block");
        	
        	if (!RedProtect.get().cfgs.getEcoBool("claim-cost-per-block.y-is-free")){
        		reco = reco * Math.abs(region.getMaxY()-region.getMinY());
        	}
        	
        	if (peco >= reco){
        		acc.withdraw(RedProtect.get().econ.getDefaultCurrency(), BigDecimal.valueOf(reco), RedProtect.get().getPVHelper().getCause(p));                        		
        		p.sendMessage(RPUtil.toText(RPLang.get("economy.region.claimed").replace("{price}", RedProtect.get().cfgs.getEcoString("economy-symbol")+reco+" "+RedProtect.get().cfgs.getEcoString("economy-name"))));
        	} else {
        		this.setError(p, RPLang.get("regionbuilder.notenought.money").replace("{price}", RedProtect.get().cfgs.getEcoString("economy-symbol")+reco));
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
        
        if (RedProtect.get().rm.getRegions(RPUtil.PlayerToUUID(p.getName()), p.getWorld()).size() == 0){
        	p.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.region.firstwarning")));
        	p.sendMessage(RPUtil.toText(RPLang.get("general.color") + "------------------------------------"));
        }  
        this.r = region;
        RedProtect.get().logger.addLog("(World "+region.getWorld()+") Player "+p.getName()+" DEFINED region "+region.getName());
    }
}
