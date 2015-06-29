package br.net.fabiozumbi12.RedProtect;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;

class DefineRegionBuilder extends RegionBuilder{
	
    public DefineRegionBuilder(Player p, Location loc1, Location loc2, String regionName, String creator, List<String> owners) {  	
    	super();
        String pName = p.getUniqueId().toString();
        if (!RedProtect.OnlineMode){
        	pName = p.getName().toLowerCase();
    	}
        if (regionName.equals("")) {
            int i = 0;
            while (true) {
                if (p.getName().length() > 13) {
                    regionName = String.valueOf(p.getName().substring(0, 13)) + "_" + i;
                }
                else {
                    regionName = String.valueOf(p.getName()) + "_" + i;
                }
                if (RedProtect.rm.getRegion(regionName, p.getWorld()) == null) {
                    break;
                }
                ++i;
            }
            if (regionName.length() > 16) {
                p.sendMessage(RPLang.get("regionbuilder.autoname.error"));
                return;
            }
        }
        if (loc1 == null || loc2 == null) {
            p.sendMessage(RPLang.get("regionbuilder.selection.notset"));
            return;
        }
        if (RedProtect.rm.getRegion(regionName, p.getWorld()) != null) {
            p.sendMessage(RPLang.get("regionbuilder.regionname.existis"));
            return;
        }
        if (regionName.length() < 2 || regionName.length() > 16) {
            p.sendMessage(RPLang.get("regionbuilder.regionname.invalid"));
            return;
        }

        owners.add(creator);
        if (!pName.equals(creator)) {
            owners.add(pName);
        }
        Region region = new Region(regionName, owners, new ArrayList<String>(), creator, new int[] { loc1.getBlockX(), loc1.getBlockX(), loc2.getBlockX(), loc2.getBlockX() }, new int[] { loc1.getBlockZ(), loc1.getBlockZ(), loc2.getBlockZ(), loc2.getBlockZ() }, 0, p.getWorld().getName(), RPUtil.DateNow(), RPConfig.getDefFlagsValues(), "", 0.0);
        
        int regionarea = region.getArea();                        
        Region topRegion = RedProtect.rm.getTopRegion(RedProtect.serv.getWorld(region.getWorld()), region.getCenterX(), region.getCenterZ());
        Region lowRegion = RedProtect.rm.getLowRegion(RedProtect.serv.getWorld(region.getWorld()), region.getCenterX(), region.getCenterZ());
        
        if (lowRegion != null){
        	if (regionarea > lowRegion.getArea()){
        		region.setPrior(lowRegion.getPrior() - 1);
        	} else if (regionarea < lowRegion.getArea() && regionarea < topRegion.getArea() ){
        		region.setPrior(topRegion.getPrior() + 1);
        	} else if (regionarea < topRegion.getArea()){
        		region.setPrior(topRegion.getPrior() + 1);
        	} 
        }              
            	
        for (Region reg:RedProtect.rm.getPossibleIntersectingRegions(region, RedProtect.serv.getWorld(region.getWorld()))){        	
        	if (!reg.isOwner(p) || !p.hasPermission("redprotect.admin")){
        		this.setError(p, RPLang.get("regionbuilder.region.overlapping").replace("{player}", RPUtil.UUIDtoPlayer(reg.getCreator())));
                return;
        	}
        }
        super.r = region;
        return;
    }
}
