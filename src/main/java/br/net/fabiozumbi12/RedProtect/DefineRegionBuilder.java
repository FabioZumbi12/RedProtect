package br.net.fabiozumbi12.RedProtect;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import br.net.fabiozumbi12.RedProtect.config.RPConfig;
import br.net.fabiozumbi12.RedProtect.config.RPLang;

public class DefineRegionBuilder extends RegionBuilder{
	
    public DefineRegionBuilder(Player p, Location loc1, Location loc2, String regionName, String leader, List<String> leaders) {  	
        String pName = p.getUniqueId().toString();
        if (!RedProtect.OnlineMode){
        	pName = p.getName().toLowerCase();
    	}
        
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
            while (RedProtect.rm.getRegion(regionName, p.getWorld()) != null) {
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
        
        //region name conform
        regionName = regionName.replace("/", "|");        
        if (RedProtect.rm.getRegion(regionName, p.getWorld()) != null) {
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
        
        Region region = new Region(regionName, new ArrayList<String>(), new ArrayList<String>(), leaders, new int[] { loc1.getBlockX(), loc1.getBlockX(), loc2.getBlockX(), loc2.getBlockX() }, new int[] { loc1.getBlockZ(), loc1.getBlockZ(), loc2.getBlockZ(), loc2.getBlockZ() }, miny, maxy, 0, p.getWorld().getName(), RPUtil.DateNow(), RPConfig.getDefFlagsValues(), wmsg, 0, null);
        
        region.setPrior(RPUtil.getUpdatedPrior(region));            
            	
        int claimLimit = RedProtect.ph.getPlayerClaimLimit(p);
        int claimused = RedProtect.rm.getPlayerRegions(p.getName(),region.getWorld());               
        if (claimused >= claimLimit && claimLimit >= 0) {
        	this.setError(p, RPLang.get("regionbuilder.claim.limit"));
            return;
        }
        
        int pLimit = RedProtect.ph.getPlayerBlockLimit(p);
        int totalArea = RedProtect.rm.getTotalRegionSize(pName);
        if (pLimit >= 0 && totalArea + region.getArea() > pLimit) {
        	this.setError(p, RPLang.get("regionbuilder.reach.limit"));
            return;
        }
        
        List<String> othersName = new ArrayList<String>();
        Region otherrg = null;
        
        for (int locx = region.getMinMbrX();  locx < region.getMaxMbrX(); locx++){
        	for (int locz = region.getMinMbrZ();  locz < region.getMaxMbrZ(); locz++){
        		otherrg = RedProtect.rm.getTopRegion(new Location(p.getWorld(), locx, p.getLocation().getBlockY(), locz));
        		if (otherrg != null){
                	if (!otherrg.isLeader(p) && !p.hasPermission("redprotect.admin")){                		
                		this.setError(p, RPLang.get("regionbuilder.region.overlapping").replace("{player}", RPUtil.UUIDtoPlayer(otherrg.getLeadersDesc())));
                        return;
                	}
                	if (!othersName.contains(otherrg.getName())){
                		othersName.add(otherrg.getName());
                	}
                }
        		/*
        		for (int locy = region.getMinY();  locy < region.getMaxY(); locy++){
        			 
        		}        	
        		*/	 
        	}
        } 
        
        if (othersName.size() > 0){
        	p.sendMessage(RPLang.get("general.color") + "------------------------------------");
        	p.sendMessage(RPLang.get("regionbuilder.overlapping"));
        	p.sendMessage(RPLang.get("region.regions") + " " + othersName);
        }
        
        this.r = region;
        RedProtect.logger.addLog("(World "+region.getWorld()+") Player "+p.getName()+" DEFINED region "+region.getName());
        return;
    }
}
