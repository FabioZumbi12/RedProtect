package br.net.fabiozumbi12.RedProtect.Sponge.hooks;

import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import br.net.fabiozumbi12.RedProtect.Sponge.Region;
import br.net.fabiozumbi12.RedProtect.Sponge.config.RPLang;
import br.net.fabiozumbi12.RedProtect.Sponge.events.ChangeRegionFlagEvent;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.DynmapCommonAPIListener;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class RPDynmap {

	private static DynmapCommonAPI Dyn;
	static MarkerSet MSet;
	private static MarkerAPI MApi;

	@Listener
	public void onChangeFlag(ChangeRegionFlagEvent event){
		if (event.getFlag().equalsIgnoreCase("dynmap")){
			boolean value = (boolean) event.getFlagValue();
			if (value){
				addMark(event.getRegion());
			} else {
				removeMark(event.getRegion());
			}
		}
	}
	  
	public RPDynmap(){
		DynmapCommonAPIListener.register(new DynmapCommonAPIListener() {
			@Override
			public void apiEnabled(DynmapCommonAPI api) {
				Dyn = api;
				MApi = Dyn.getMarkerAPI();
				MSet = MApi.getMarkerSet(RedProtect.get().cfgs.root().hooks.dynmap.marks_groupname);
				if (MSet == null){
					MSet = MApi.createMarkerSet("RedProtect", RedProtect.get().cfgs.root().hooks.dynmap.marks_groupname, null, false);
				}
				MSet.setHideByDefault(RedProtect.get().cfgs.root().hooks.dynmap.hide_by_default);
				MSet.setLayerPriority(RedProtect.get().cfgs.root().hooks.dynmap.layer_priority);
				MSet.setLabelShow(RedProtect.get().cfgs.root().hooks.dynmap.show_label);
				MSet.setDefaultMarkerIcon(MApi.getMarkerIcon(RedProtect.get().cfgs.root().hooks.dynmap.marker_icon));
				int minzoom = RedProtect.get().cfgs.root().hooks.dynmap.min_zoom;
				if (minzoom > 0) {
					MSet.setMinZoom(minzoom);
				} else {
					MSet.setMinZoom(0);
				}

				//start set markers
				for (World w:RedProtect.get().serv.getWorlds()){
					for (Region r:RedProtect.get().rm.getRegionsByWorld(w)){
						if (!r.allowDynmap()) continue;
						addMark(r);
					}
				}
			}
		});
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
		for (Location<World> l:r.get4Points(90)){
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
		am.setDescription(TextSerializers.FORMATTING_CODE.stripCodes(rName+area));
		
		if (RedProtect.get().cfgs.root().hooks.dynmap.show_leaders_admins){
			String leader = RPLang.get("region.leaders")+" <span style=\"font-weight:bold;\">"+r.getLeadersDesc()+"</span><br>"; 
			String admin = RPLang.get("region.admins")+" <span style=\"font-weight:bold;\">"+r.getAdminDesc()+"</span><br>"; 						
			am.setDescription(TextSerializers.FORMATTING_CODE.stripCodes(rName+leader+admin+area));
		}		
		
		int center = -1;
		if (RedProtect.get().cfgs.root().hooks.dynmap.cuboid_region.enabled){
			am.setRangeY(r.getMinLocation().getBlockY()+0.500, r.getMaxLocation().getBlockY()+0.500);
		} else {
			center = RedProtect.get().cfgs.root().hooks.dynmap.cuboid_region.if_disable_set_center;
			am.setRangeY(center, center);			
		}
		
		
		if (RedProtect.get().cfgs.root().hooks.dynmap.show_icon){
			Marker m = MSet.findMarker(r.getID());
			if (center == -1){
				center = r.getCenterY();
			}
			if (m == null){		    				
				MSet.createMarker(r.getID(), r.getName(), r.getWorld(), r.getCenterX(), center, r.getCenterZ(), MApi.getMarkerIcon(RedProtect.get().cfgs.root().hooks.dynmap.marker_icon), true);
			} else {
				m.setLocation(r.getWorld(), r.getCenterX(), center, r.getCenterZ());
			}
		}
	}
}
