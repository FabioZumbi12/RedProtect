package br.net.fabiozumbi12.RedProtect.Bukkit.actions;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import br.net.fabiozumbi12.RedProtect.Bukkit.RPUtil;
import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import br.net.fabiozumbi12.RedProtect.Bukkit.RegionBuilder;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPConfig;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPLang;

public class DefineRegionBuilder extends RegionBuilder{
	
    public DefineRegionBuilder(Player p, Location loc1, Location loc2, String regionName, String leader, List<String> leaders, boolean admin) {  
        if (!RPConfig.isAllowedWorld(p)){
        	this.setError(p, RPLang.get("regionbuilder.region.worldnotallowed"));
            return;
        }

        regionName = regionName.replaceAll("[.+=;\\-]", "");

        //region leader
        String pName = RPUtil.PlayerToUUID(p.getName());
        //for region name
        String pRName = RPUtil.UUIDtoPlayer(p.getName());
        
        String wmsg = "";
        if (leader.equals(RPConfig.getString("region-settings.default-leader"))){
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
            p.sendMessage(RPLang.get("regionbuilder.regionname.invalid.charac").replace("{charac}", "@"));
            return;
        }
        if (loc1 == null || loc2 == null) {
        	RPLang.sendMessage(p, "regionbuilder.selection.notset");
            return;
        }

        //check if distance allowed
        if (loc1.getWorld().equals(loc2.getWorld()) && loc1.distanceSquared(loc2) > RPConfig.getInt("region-settings.define-max-distance") && !RedProtect.get().ph.hasPerm(p,"redprotect.bypass.define-max-distance")){
            Double dist = loc1.distanceSquared(loc2);
            RPLang.sendMessage(p, String.format(RPLang.get("regionbuilder.selection.maxdefine"), RPConfig.getInt("region-settings.define-max-distance"), dist.intValue()));
            return;
        }

        //region name conform
        regionName = regionName.replace("/", "|");        
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
        if (RPConfig.getBool("region-settings.autoexpandvert-ondefine")){
        	miny = 0;
        	maxy = p.getWorld().getMaxHeight();
        }
        
        Region region = new Region(regionName, new ArrayList<>(), new ArrayList<>(), leaders, new int[] { loc1.getBlockX(), loc1.getBlockX(), loc2.getBlockX(), loc2.getBlockX() }, new int[] { loc1.getBlockZ(), loc1.getBlockZ(), loc2.getBlockZ(), loc2.getBlockZ() }, miny, maxy, 0, p.getWorld().getName(), RPUtil.DateNow(), RPConfig.getDefFlagsValues(), wmsg, 0, null, true);
        
        region.setPrior(RPUtil.getUpdatedPrior(region));            
            	
        int claimLimit = RedProtect.get().ph.getPlayerClaimLimit(p);
        int claimused = RedProtect.get().rm.getPlayerRegions(p.getName(),p.getWorld());    
        boolean claimUnlimited = RedProtect.get().ph.hasPerm(p, "redprotect.limit.claim.unlimited");
        if (claimused >= claimLimit && claimLimit >= 0 && !claimUnlimited) {
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
        Region otherrg;
        
        //check if same area
        otherrg = RedProtect.get().rm.getTopRegion(region.getCenterLoc());
        if (otherrg != null && otherrg.get4Points(region.getCenterY()).equals(region.get4Points(region.getCenterY()))){
        	this.setError(p, RPLang.get("regionbuilder.region.overlapping").replace("{location}", "x: " + otherrg.getCenterX() + ", z: " + otherrg.getCenterZ()).replace("{player}", RPUtil.UUIDtoPlayer(otherrg.getLeadersDesc())));
        	return;
        }
        
        //check regions inside region
        for (Region r:RedProtect.get().rm.getRegionsByWorld(p.getWorld())){
        	if (r.getMaxMbrX() <= region.getMaxMbrX() && r.getMaxY() <= region.getMaxY() && r.getMaxMbrZ() <= region.getMaxMbrZ() && r.getMinMbrX() >= region.getMinMbrX() && r.getMinY() >= region.getMinY() && r.getMinMbrZ() >= region.getMinMbrZ()){
        		if (!r.isLeader(p) && !p.hasPermission("redprotect.bypass")){
        			this.setError(p, RPLang.get("regionbuilder.region.overlapping").replace("{location}", "x: " + r.getCenterX() + ", z: " + r.getCenterZ()).replace("{player}", RPUtil.UUIDtoPlayer(r.getLeadersDesc())));
                	return;
            	}
        		if (!othersName.contains(r.getName())){
            		othersName.add(r.getName());
            	}
        	}
        }
        
        //check borders for other regions
        List<Location> limitlocs = region.getLimitLocs(region.getMinY(), region.getMaxY(), true);
        for (Location loc:limitlocs){
        	
        	/*
        	//check regions near
        	if (!RPUtil.canBuildNear(p, loc)){
            	return;    	
            }*/
        	
        	otherrg = RedProtect.get().rm.getTopRegion(loc);        	
        	RedProtect.get().logger.debug("protection Block is: " + loc.getBlock().getType().name());
        	
    		if (otherrg != null){                    			
            	if (!otherrg.isLeader(p) && !p.hasPermission("redprotect.bypass")){
            		this.setError(p, RPLang.get("regionbuilder.region.overlapping").replace("{location}", "x: " + otherrg.getCenterX() + ", z: " + otherrg.getCenterZ()).replace("{player}", RPUtil.UUIDtoPlayer(otherrg.getLeadersDesc())));
                    return;
            	}
            	if (!othersName.contains(otherrg.getName())){
            		othersName.add(otherrg.getName());
            	}
            }
        }
        
        if (RPConfig.getEcoBool("claim-cost-per-block.enable") && RedProtect.get().Vault && !p.hasPermission("redprotect.eco.bypass")){
        	Double peco = RedProtect.get().econ.getBalance(p);
        	long reco = region.getArea() * RPConfig.getEcoInt("claim-cost-per-block.cost-per-block");
        	
        	if (!RPConfig.getEcoBool("claim-cost-per-block.y-is-free")){
        		reco = reco * Math.abs(region.getMaxY()-region.getMinY());
        	}
        	
        	if (peco >= reco){
        		RedProtect.get().econ.withdrawPlayer(p, reco);
        		p.sendMessage(RPLang.get("economy.region.claimed").replace("{price}", RPConfig.getEcoString("economy-symbol")+reco+" "+RPConfig.getEcoString("economy-name")));
        	} else {
        		RPLang.sendMessage(p, RPLang.get("regionbuilder.notenought.money").replace("{price}", RPConfig.getEcoString("economy-symbol")+reco));
        		return;
        	}
        }
        
        p.sendMessage(RPLang.get("general.color") + "------------------------------------");
        if (!admin){
        	p.sendMessage(RPLang.get("regionbuilder.claim.left") + (claimused+1) + RPLang.get("general.color") + "/" + (claimUnlimited ? RPLang.get("regionbuilder.area.unlimited") : claimLimit));
            p.sendMessage(RPLang.get("regionbuilder.area.used") + " " + (regionarea == 0 ? ChatColor.GREEN+""+regionarea:ChatColor.RED+"- "+regionarea) + "\n" + 
            RPLang.get("regionbuilder.area.left") + " " + (areaUnlimited ? RPLang.get("regionbuilder.area.unlimited") : (pLimit - actualArea)));
        }        
        p.sendMessage(RPLang.get("cmdmanager.region.priority.set").replace("{region}", region.getName()) + " " + region.getPrior());
        p.sendMessage(RPLang.get("general.color") + "------------------------------------");
        if (othersName.size() > 0){        	
        	p.sendMessage(RPLang.get("regionbuilder.overlapping"));
        	p.sendMessage(RPLang.get("region.regions") + " " + othersName);
        	p.sendMessage(RPLang.get("general.color") + "------------------------------------");
        }
        
        if (RedProtect.get().rm.getRegions(RPUtil.PlayerToUUID(p.getName()), p.getWorld()).size() == 0){
        	p.sendMessage(RPLang.get("cmdmanager.region.firstwarning"));
        	p.sendMessage(RPLang.get("general.color") + "------------------------------------");
        }                        
                
        this.r = region;
        RedProtect.get().logger.addLog("(World "+region.getWorld()+") Player "+p.getName()+" DEFINED region "+region.getName());
    }
}
