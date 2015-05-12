package br.net.fabiozumbi12.RedProtect;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

class RedefineRegionBuilder extends RegionBuilder{
		
    public RedefineRegionBuilder(Player p, Region old, Location l1, Location l2) {
    	super();
        if (l1 == null || l2 == null) {
            this.setError(p, RPLang.get("regionbuilder.selection.notset"));
            return;
        }
        World w = p.getWorld();
        Region region = new Region(old.getName(), old.getOwners(), old.getMembers(), old.getCreator(), new int[] { l1.getBlockX(), l1.getBlockX(), l2.getBlockX(), l2.getBlockX() }, new int[] { l1.getBlockZ(), l1.getBlockZ(), l2.getBlockZ(), l2.getBlockZ() }, old.getPrior(), w.getName(), old.getDate(), old.flags, old.getWelcome());
        String uuid = p.getUniqueId().toString();
    	if (!RedProtect.OnlineMode){
    		uuid = p.getName().toLowerCase();
    	}
    	
        for (Region reg:RedProtect.rm.getPossibleIntersectingRegions(region, w)){
        	if (!reg.isOwner(uuid) || !p.hasPermission("redprotect.admin")){
        		this.setError(p, RPLang.get("regionbuilder.region.overlapping").replace("{player}", RPUtil.UUIDtoPlayer(reg.getCreator())));
                return;
        	}
        }       
        super.r = region;
        return;
    }
}
