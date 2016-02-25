package br.net.fabiozumbi12.RedProtect.hooks;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

import br.net.fabiozumbi12.RedProtect.RedProtect;
import br.net.fabiozumbi12.RedProtect.Region;
import br.net.fabiozumbi12.RedProtect.config.RPConfig;

public class Dynmap {

	private static DynmapAPI Dyn;
	static MarkerSet MSet;
	private static MarkerAPI MApi;

	  
	public Dynmap(DynmapAPI dyn){
		Dyn = dyn;
		MApi = Dyn.getMarkerAPI();
		MSet = MApi.getMarkerSet(RPConfig.getString("hooks.dynmap.marks-groupname"));
		if (MSet == null){
			MSet = MApi.createMarkerSet("RedProtect", RPConfig.getString("hooks.dynmap.marks-groupname"), null, false);
		}
		MSet.setLayerPriority(RPConfig.getInt("hooks.dynmap.layer-priority"));
		MSet.setLabelShow(RPConfig.getBool("hooks.dynmap.show-label"));
		MSet.setDefaultMarkerIcon(MApi.getMarkerIcon(RPConfig.getString("hooks.dynmap.marker-icon")));
	    int minzoom = RPConfig.getInt("hooks.dynmap.min-zoom");
	    if (minzoom > 0) {
	    	MSet.setMinZoom(0);
	    }
	    MSet.setHideByDefault(false);		
	    
	    //start set markers
		for (World w:Bukkit.getWorlds()){
			for (Region r:RedProtect.rm.getRegionsByWorld(w)){
    			addMark(r);
    		}
		}		
		
		if (RPConfig.getInt("hooks.dynmap.check-invalid-marks") != -1){
			Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(RedProtect.plugin, new Runnable() {
		    	public void run(){	    		
		    		removeInvalidMarks();
		    	}
		    },RPConfig.getInt("hooks.dynmap.check-invalid-marks")*20,RPConfig.getInt("hooks.dynmap.check-invalid-marks")*20);
		}
	    
	    
	}
	
	public void removeInvalidMarks(){
		for (AreaMarker m : MSet.getAreaMarkers()) {
			if (RedProtect.rm.getRegion(m.getLabel(), m.getWorld()) == null){
				m.deleteMarker();
			}
		}	
		for (Marker m : MSet.getMarkers()) {
			if (RedProtect.rm.getRegion(m.getLabel(), m.getWorld()) == null || !RPConfig.getBool("hooks.dynmap.show-icon")){
				m.deleteMarker();
			}
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
			x[i] = l.getX();
			z[i] = l.getZ();
			i++;
		}
			
		if (am == null){		    				
			am = MSet.createAreaMarker(r.getID(), r.getName(), false, r.getWorld(), x, z, true);	
		} else {
			am.setCornerLocations(x, z);
			am.setRangeY(r.getMinLocation().getY(), r.getMaxLocation().getY());
		}		
		
		if (RPConfig.getBool("hooks.dynmap.show-icon")){
			Marker m = MSet.findMarker(r.getID());
			if (m == null){		    				
				m = MSet.createMarker(r.getID(), r.getName(), r.getWorld(), r.getCenterX(), r.getCenterY(), r.getCenterZ(), MApi.getMarkerIcon(RPConfig.getString("hooks.dynmap.marker-icon")), true);
			} else {
				m.setLocation(r.getWorld(), r.getCenterX(), r.getCenterY(), r.getCenterZ());
			}
		}
	}
}
