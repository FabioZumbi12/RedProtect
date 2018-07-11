package br.net.fabiozumbi12.RedProtect.Sponge.actions;

import br.net.fabiozumbi12.RedProtect.Sponge.*;
import br.net.fabiozumbi12.RedProtect.Sponge.config.RPLang;
import br.net.fabiozumbi12.RedProtect.Sponge.events.CreateRegionEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DefineRegionBuilder extends RegionBuilder{
	
	public DefineRegionBuilder(Player p, Location<World> loc1, Location<World> loc2, String regionName, String leader, List<String> leaders, boolean admin) {  
    	if (!RedProtect.get().cfgs.isAllowedWorld(p)){
        	this.setError(p, RPLang.get("regionbuilder.region.worldnotallowed"));
            return;
        }

        //region name conform
        if (regionName.length() < 3) {
            RPLang.sendMessage(p, "regionbuilder.regionname.invalid");
            return;
        }

        //filter region name
        regionName = regionName.replace(" ","_").replaceAll("[^\\p{L}_0-9 ]", "");
        if (regionName == null || regionName.isEmpty() || regionName.length() < 3) {
            regionName = RPUtil.nameGen(p.getName(), p.getWorld().getName());
            if (regionName.length() > 16) {
                RPLang.sendMessage(p, "regionbuilder.autoname.error");
                return;
            }
        }

        if (RedProtect.get().rm.getRegion(regionName, p.getWorld()) != null) {
            RPLang.sendMessage(p, "regionbuilder.regionname.existis");
            return;
        }

    	//region leader
        String pName = RPUtil.PlayerToUUID(p.getName());

        String wmsg = "";
        if (leader.equals(RedProtect.get().cfgs.root().region_settings.default_leader)){
        	pName = leader;
        	wmsg = "hide ";
        }

        if (loc1 == null || loc2 == null) {
        	RPLang.sendMessage(p, "regionbuilder.selection.notset");
            return;
        }

        //check if distance allowed
        if (loc1.getPosition().distanceSquared(loc2.getPosition()) > RedProtect.get().cfgs.root().region_settings.wand_max_distance && !RedProtect.get().ph.hasPerm(p,"redprotect.bypass.define-max-distance")){
            Double dist = loc1.getPosition().distanceSquared(loc2.getPosition());
            RPLang.sendMessage(p, String.format(RPLang.get("regionbuilder.selection.maxdefine"), RedProtect.get().cfgs.root().region_settings.wand_max_distance, dist.intValue()));
            return;
        }

        leaders.add(leader);
        if (!pName.equals(leader)) {
        	leaders.add(pName);
        }
        
        int miny = loc1.getBlockY();
        int maxy = loc2.getBlockY();
        if (RedProtect.get().cfgs.root().region_settings.autoexpandvert_ondefine){
            miny = 0;
            maxy = p.getWorld().getBlockMax().getY();
            if (RedProtect.get().cfgs.root().region_settings.claim.miny != -1)
                miny = RedProtect.get().cfgs.root().region_settings.claim.miny;
            if (RedProtect.get().cfgs.root().region_settings.claim.maxy != -1)
                miny = RedProtect.get().cfgs.root().region_settings.claim.maxy;
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
        otherrg = RedProtect.get().rm.getTopRegion(region.getCenterLoc(), this.getClass().getName());
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
        	
        	otherrg = RedProtect.get().rm.getTopRegion(loc, this.getClass().getName());
        	RedProtect.get().logger.debug(LogLevel.BLOCKS, "protection Block is: " + loc.getBlock().getType().getName());
        	
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

        //fire event
        CreateRegionEvent event = new CreateRegionEvent(r, p);
        if (Sponge.getEventManager().post(event)){
            return;
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
