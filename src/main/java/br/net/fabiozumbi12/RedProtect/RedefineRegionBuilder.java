package br.net.fabiozumbi12.RedProtect;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

class RedefineRegionBuilder extends RegionBuilder{
		
    @SuppressWarnings("deprecation")
	public RedefineRegionBuilder(Player p, Region old, Location l1, Location l2) {
    	super();
        if (l1 == null || l2 == null) {
            this.setError(p, RPLang.get("regionbuilder.selection.notset"));
            return;
        }
        World w = p.getWorld();
        
        int miny = l1.getBlockY();
        int maxy = l2.getBlockY();
        if (RPConfig.getBool("region-settings.autoexpandvert-ondefine")){
        	miny = 0;
        	maxy = w.getMaxHeight();
        }
        
        Region region = new Region(old.getName(), old.getOwners(), old.getMembers(), old.getCreator(), new int[] { l1.getBlockX(), l1.getBlockX(), l2.getBlockX(), l2.getBlockX() }, new int[] { l1.getBlockZ(), l1.getBlockZ(), l2.getBlockZ(), l2.getBlockZ() }, miny, maxy, old.getPrior(), w.getName(), old.getDate(), old.flags, old.getWelcome(), old.getValue());
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
        return;
    }
}
