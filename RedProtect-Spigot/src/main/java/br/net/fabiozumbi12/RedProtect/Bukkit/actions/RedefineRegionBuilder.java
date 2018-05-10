package br.net.fabiozumbi12.RedProtect.Bukkit.actions;

import br.net.fabiozumbi12.RedProtect.Bukkit.RPUtil;
import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import br.net.fabiozumbi12.RedProtect.Bukkit.RegionBuilder;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPConfig;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPLang;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class RedefineRegionBuilder extends RegionBuilder{
		
	private boolean checkID(Region newr, Region oldr){
		return newr.getID().equals(oldr.getID());
	}
	
    @SuppressWarnings("deprecation")
	public RedefineRegionBuilder(Player p, Region old, Location l1, Location l2) {
        if (l1 == null || l2 == null) {
            this.setError(p, RPLang.get("regionbuilder.selection.notset"));
            return;
        }

        //check if distance allowed
        if (l1.getWorld().equals(l2.getWorld()) && l1.distanceSquared(l2) > RPConfig.getInt("region-settings.define-max-distance") && !RedProtect.get().ph.hasPerm(p,"redprotect.bypass.define-max-distance")){
            Double dist = l1.distanceSquared(l2);
            RPLang.sendMessage(p, String.format(RPLang.get("regionbuilder.selection.maxdefine"), RPConfig.getInt("region-settings.define-max-distance"), dist.intValue()));
            return;
        }

        World w = p.getWorld();
        
        int miny = l1.getBlockY();
        int maxy = l2.getBlockY();
        if (RPConfig.getBool("region-settings.autoexpandvert-ondefine")){
        	miny = 0;
        	maxy = w.getMaxHeight();
        }
        
        Region region = new Region(old.getName(), old.getAdmins(), old.getMembers(), old.getLeaders(), new int[] { l1.getBlockX(), l1.getBlockX(), l2.getBlockX(), l2.getBlockX() }, new int[] { l1.getBlockZ(), l1.getBlockZ(), l2.getBlockZ(), l2.getBlockZ() }, miny, maxy, old.getPrior(), w.getName(), old.getDate(), old.flags, old.getWelcome(), old.getValue(), old.getTPPoint(), old.canDelete());
        
        region.setPrior(RPUtil.getUpdatedPrior(region));    
        
        String pName = p.getUniqueId().toString();
        if (!RedProtect.get().OnlineMode){
        	pName = p.getName().toLowerCase();
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
        if (otherrg != null && !checkID(region, otherrg) && otherrg.get4Points(region.getCenterY()).equals(region.get4Points(region.getCenterY()))){
        	this.setError(p, RPLang.get("regionbuilder.region.overlapping").replace("{location}", "x: " + otherrg.getCenterX() + ", z: " + otherrg.getCenterZ()).replace("{player}", RPUtil.UUIDtoPlayer(otherrg.getLeadersDesc())));
        	return;
        }
        
        boolean hasAny = false;
        
        //check regions inside region
        for (Region r:RedProtect.get().rm.getInnerRegions(region, p.getWorld())){
        	if (!r.isLeader(p) && !p.hasPermission("redprotect.bypass")){
    			this.setError(p, RPLang.get("regionbuilder.region.overlapping").replace("{location}", "x: " + r.getCenterX() + ", z: " + r.getCenterZ()).replace("{player}", RPUtil.UUIDtoPlayer(r.getLeadersDesc())));
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
    			if (checkID(region, otherrg)){
            		hasAny = true;
            		continue;
            	}
    			
            	if (!otherrg.isLeader(p) && !p.hasPermission("redprotect.bypass")){
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
        
        if (RPConfig.getEcoBool("claim-cost-per-block.enable") && RedProtect.get().Vault && !p.hasPermission("redprotect.eco.bypass")){
        	Double peco = RedProtect.get().econ.getBalance(p);
        	long reco = (region.getArea() <= old.getArea() ? 0 : region.getArea()-old.getArea())* RPConfig.getEcoInt("claim-cost-per-block.cost-per-block");
        	
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
        
        RedProtect.get().rm.remove(old, w);
        
        int claimLimit = RedProtect.get().ph.getPlayerClaimLimit(p);
        int claimused = RedProtect.get().rm.getPlayerRegions(p.getName(),w);    
        boolean claimUnlimited = RedProtect.get().ph.hasPerm(p, "redprotect.limit.claim.unlimited");
        
        p.sendMessage(RPLang.get("general.color") + "------------------------------------");
        p.sendMessage(RPLang.get("regionbuilder.claim.left") + (claimused+1) + RPLang.get("general.color") + "/" + (claimUnlimited ? RPLang.get("regionbuilder.area.unlimited") : claimLimit));
        p.sendMessage(RPLang.get("regionbuilder.area.used") + " " + (regionarea == 0 ? ChatColor.GREEN+""+regionarea:ChatColor.RED+"- "+regionarea) + "\n" + 
        RPLang.get("regionbuilder.area.left") + " " + (areaUnlimited ? RPLang.get("regionbuilder.area.unlimited") : (pLimit - actualArea)));       
        p.sendMessage(RPLang.get("cmdmanager.region.priority.set").replace("{region}", region.getName()) + " " + region.getPrior());
        p.sendMessage(RPLang.get("general.color") + "------------------------------------");
        if (othersName.size() > 0){
        	p.sendMessage(RPLang.get("general.color") + "------------------------------------");
        	p.sendMessage(RPLang.get("regionbuilder.overlapping"));
        	p.sendMessage(RPLang.get("region.regions") + " " + othersName);
        }        
                
        this.r = region;
        RedProtect.get().logger.addLog("(World "+region.getWorld()+") Player "+p.getName()+" REDEFINED region "+region.getName());
    }
}
