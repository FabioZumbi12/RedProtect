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
        
        String pRName = RPUtil.UUIDtoPlayer(p.getName());
        String wmsg = "";
        if (creator.equals(RPConfig.getString("region-settings.default-owner"))){
        	pName = creator;
        	pRName = creator;
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
        if (loc1 == null || loc2 == null) {
        	RPLang.sendMessage(p, "regionbuilder.selection.notset");
            return;
        }
        if (RedProtect.rm.getRegion(regionName, p.getWorld()) != null) {
        	RPLang.sendMessage(p, "regionbuilder.regionname.existis");
            return;
        }
        if (regionName.length() < 3 || regionName.length() > 16) {
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
        
        Region region = new Region(regionName, owners, new ArrayList<String>(), creator, new int[] { loc1.getBlockX(), loc1.getBlockX(), loc2.getBlockX(), loc2.getBlockX() }, new int[] { loc1.getBlockZ(), loc1.getBlockZ(), loc2.getBlockZ(), loc2.getBlockZ() }, miny, maxy, 0, p.getWorld().getName(), RPUtil.DateNow(), RPConfig.getDefFlagsValues(), wmsg, 0, null);
        
        region.setPrior(RPUtil.getUpdatedPrior(region));            
            	
        List<String> othersName = new ArrayList<String>();
        Region otherrg = null;
        
        for (int locx = region.getMinMbrX();  locx < region.getMaxMbrX(); locx++){
        	for (int locz = region.getMinMbrZ();  locz < region.getMaxMbrZ(); locz++){
        		otherrg = RedProtect.rm.getTopRegion(new Location(p.getWorld(), locx, p.getLocation().getBlockY(), locz));
        		if (otherrg != null){
                	if (!otherrg.isOwner(p) && !p.hasPermission("redprotect.admin")){
                		this.setError(p, RPLang.get("regionbuilder.region.overlapping").replace("{player}", RPUtil.UUIDtoPlayer(otherrg.getCreator())));
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
        
        super.r = region;
        RedProtect.logger.addLog("(World "+region.getWorld()+") Player "+p.getName()+" DEFINED region "+region.getName());
        return;
    }
}
