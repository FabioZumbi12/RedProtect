package br.net.fabiozumbi12.RedProtect.Bukkit.hooks;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPConfig;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPLang;

public class RPDynmap {

	private static DynmapAPI Dyn;
	static MarkerSet MSet;
	private static MarkerAPI MApi;

	  
	public RPDynmap(DynmapAPI dyn){
		Dyn = dyn;
		MApi = Dyn.getMarkerAPI();
		MSet = MApi.getMarkerSet(RPConfig.getString("hooks.dynmap.marks-groupname"));
		if (MSet == null){			
			MSet = MApi.createMarkerSet("RedProtect", RPConfig.getString("hooks.dynmap.marks-groupname"), null, false);
		}
		MSet.setHideByDefault(RPConfig.getBool("hooks.dynmap.hide-by-default"));
		MSet.setLayerPriority(RPConfig.getInt("hooks.dynmap.layer-priority"));
		MSet.setLabelShow(RPConfig.getBool("hooks.dynmap.show-label"));
		MSet.setDefaultMarkerIcon(MApi.getMarkerIcon(RPConfig.getString("hooks.dynmap.marker-icon")));
	    int minzoom = RPConfig.getInt("hooks.dynmap.min-zoom");
	    if (minzoom > 0) {
	    	MSet.setMinZoom(minzoom);
	    } else {
	    	MSet.setMinZoom(0);
	    }
	    
	    //start set markers
		for (World w:RedProtect.get().serv.getWorlds()){
			for (Region r:RedProtect.get().rm.getRegionsByWorld(w)){
    			addMark(r);
    		}
		}
	}
	
	public void removeAll(World w){
		for (Region r:RedProtect.get().rm.getRegionsByWorld(w)){
			removeMark(r);
		}
	}
	
	public void removeMark(Region r){		
		AreaMarker am = MSet.findAreaMarker(r.getID());
		if (am != null){
			am.deleteMarker();
		}
		Marker m = MSet.findMarker(r.getID());
		if (m != null){
			m.deleteMarker();
		}
	}
	
	public void addMark(Region r){
		AreaMarker am = MSet.findAreaMarker(r.getID());
		
		double[] x = new double[4];
		double[] z = new double[4];
		int i = 0;
		for (Location l:r.get4Points(90)){
			x[i] = l.getBlockX()+0.500;
			z[i] = l.getBlockZ()+0.500;
			i++;
		}
			
		if (am == null){		    				
			am = MSet.createAreaMarker(r.getID(), r.getName(), false, r.getWorld(), x, z, true);	
		} else {
			am.setCornerLocations(x, z);
		}		
		
		String rName = RPLang.get("region.name")+" <span style=\"font-weight:bold;\">"+r.getName()+"</span><br>";
		String area = RPLang.get("region.area")+" <span style=\"font-weight:bold;\">"+r.getArea()+"</span>"; 		
		am.setDescription(ChatColor.stripColor(rName+area));
		
		if (RPConfig.getBool("hooks.dynmap.show-leaders-admins")){			
			String leader = RPLang.get("region.leaders")+" <span style=\"font-weight:bold;\">"+r.getLeadersDesc()+"</span><br>"; 
			String admin = RPLang.get("region.admins")+" <span style=\"font-weight:bold;\">"+r.getAdminDesc()+"</span><br>"; 						
			am.setDescription(ChatColor.stripColor(rName+leader+admin+area));
		}		
		
		int center = -1;
		if (RPConfig.getBool("hooks.dynmap.cuboid-region.enabled")){
			am.setRangeY(r.getMinLocation().getBlockY()+0.500, r.getMaxLocation().getBlockY()+0.500);
		} else {
			center = RPConfig.getInt("hooks.dynmap.cuboid-region.if-disable-set-center");
			am.setRangeY(center, center);			
		}
		
		
		if (RPConfig.getBool("hooks.dynmap.show-icon")){
			Marker m = MSet.findMarker(r.getID());
			if (center == -1){
				center = r.getCenterY();
			}
			if (m == null){		    				
				MSet.createMarker(r.getID(), r.getName(), r.getWorld(), r.getCenterX(), center, r.getCenterZ(), MApi.getMarkerIcon(RPConfig.getString("hooks.dynmap.marker-icon")), true);
			} else {
				m.setLocation(r.getWorld(), r.getCenterX(), center, r.getCenterZ());
			}
		}
	}
}
