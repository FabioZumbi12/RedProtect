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
            	RPLang.sendMessage(p, "regionbuilder.autoname.error");
                return;
            }
        }
        if (loc1 == null || loc2 == null) {
        	RPLang.sendMessage(p, "regionbuilder.selection.notset");
            return;
        }
        if (RedProtect.rm.getRegion(regionName, p.getWorld()) != null) {
        	RPLang.sendMessage(p, "regionbuilder.regionname.existis");
            return;
        }
        if (regionName.length() < 2 || regionName.length() > 16) {
        	RPLang.sendMessage(p, "regionbuilder.regionname.invalid");
            return;
        }

        owners.add(creator);
        if (!pName.equals(creator)) {
            owners.add(pName);
        }
        
        int miny = loc1.getBlockY();
        int maxy = loc2.getBlockY();
        if (RPConfig.getBool("region-settings.autoexpandvert-ondefine")){
        	miny = 0;
        	maxy = p.getWorld().getMaxHeight();
        }
        
        Region region = new Region(regionName, owners, new ArrayList<String>(), creator, new int[] { loc1.getBlockX(), loc1.getBlockX(), loc2.getBlockX(), loc2.getBlockX() }, new int[] { loc1.getBlockZ(), loc1.getBlockZ(), loc2.getBlockZ(), loc2.getBlockZ() }, miny, maxy, 0, p.getWorld().getName(), RPUtil.DateNow(), RPConfig.getDefFlagsValues(), "", 0.0);
        
        region.setPrior(RPUtil.getUpdatedPrior(region));            
            	
        List<String> othersName = new ArrayList<String>();
        Region otherrg = null;
        
        for (int locx = region.getMinMbrX();  locx < region.getMaxMbrX(); locx++){
        	for (int locz = region.getMinMbrZ();  locz < region.getMaxMbrZ(); locz++){
        		for (int locy = region.getMinY();  locy < region.getMaxY(); locy++){
        			otherrg = RedProtect.rm.getTopRegion(new Location(p.getWorld(), locx, p.getLocation().getY(), locz));
            		if (otherrg != null){
                    	if (!otherrg.isOwner(p) && !p.hasPermission("redprotect.admin")){
                    		this.setError(p, RPLang.get("regionbuilder.region.overlapping").replace("{player}", RPUtil.UUIDtoPlayer(otherrg.getCreator())));
                            return;
                    	}
                    	if (!othersName.contains(otherrg.getName())){
                    		othersName.add(otherrg.getName());
                    	}
                    } 
        		}        		 
        	}
        } 
        
        if (othersName.size() > 0){
        	p.sendMessage(RPLang.get("general.color") + "------------------------------------");
        	p.sendMessage(RPLang.get("regionbuilder.overlapping"));
        	p.sendMessage(RPLang.get("region.regions") + " " + othersName);
        }
        
        super.r = region;
        return;
    }
}
