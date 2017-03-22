package br.net.fabiozumbi12.redprotect;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import br.net.fabiozumbi12.redprotect.config.RPLang;

public class RedefineRegionBuilder extends RegionBuilder{
		
    @SuppressWarnings("deprecation")
	public RedefineRegionBuilder(Player p, Region old, Location<World> l1, Location<World> l2) {
        if (l1 == null || l2 == null) {
            this.setError(p, RPLang.get("regionbuilder.selection.notset"));
            return;
        }
        World w = p.getWorld();
        
        int miny = l1.getBlockY();
        int maxy = l2.getBlockY();
        if (RedProtect.cfgs.getBool("region-settings.autoexpandvert-ondefine")){
        	miny = 0;
        	maxy = 256;
        }
        
        Region region = new Region(old.getName(), old.getAdmins(), old.getMembers(), old.getLeaders(), new int[] { l1.getBlockX(), l1.getBlockX(), l2.getBlockX(), l2.getBlockX() }, new int[] { l1.getBlockZ(), l1.getBlockZ(), l2.getBlockZ(), l2.getBlockZ() }, miny, maxy, old.getPrior(), w.getName(), old.getDate(), old.flags, old.getWelcome(), old.getValue(), old.getTPPoint(), old.canDelete());
        List<String> othersName = new ArrayList<String>();
        Region otherrg = null;
        
        for (int locx = region.getMinMbrX();  locx < region.getMaxMbrX(); locx++){
        	for (int locz = region.getMinMbrZ();  locz < region.getMaxMbrZ(); locz++){
        		otherrg = RedProtect.rm.getTopRegion(new Location<World>(p.getWorld(), locx, p.getLocation().getBlockY(), locz));
        		if (otherrg != null){
                	if (!otherrg.isLeader(p) && !RedProtect.ph.hasGenPerm(p, "redprotect.admin")){
                		this.setError(p, RPLang.get("regionbuilder.region.overlapping").toString().replace("{player}", RPUtil.UUIDtoPlayer(otherrg.getLeadersDesc())));
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
        	p.sendMessage(RPUtil.toText(RPLang.get("general.color") + "------------------------------------"));
        	p.sendMessage(RPUtil.toText(RPLang.get("regionbuilder.overlapping")));
        	p.sendMessage(RPUtil.toText(RPLang.get("region.regions") + " " + othersName));
        }
        
        
        this.r = region;
        RedProtect.logger.addLog("(World "+region.getWorld()+") Player "+p.getName()+" REDEFINED region "+region.getName());
        return;
    }
}
