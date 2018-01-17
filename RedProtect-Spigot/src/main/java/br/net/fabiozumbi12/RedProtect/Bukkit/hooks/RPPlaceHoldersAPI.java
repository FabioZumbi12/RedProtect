package br.net.fabiozumbi12.RedProtect.Bukkit.hooks;

import me.clip.placeholderapi.external.EZPlaceholderHook;

import org.bukkit.entity.Player;

import br.net.fabiozumbi12.RedProtect.Bukkit.RPUtil;
import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPLang;

public class RPPlaceHoldersAPI extends EZPlaceholderHook {

	public RPPlaceHoldersAPI(RedProtect plugin) {
		super(plugin, "redprotect");
	}

	@Override
	public String onPlaceholderRequest(Player p, String arg) {
		String text = "";
		if (arg.equals("player_in_region")){
			Region r = RedProtect.get().rm.getTopRegion(p.getLocation());
			text = r == null ? RPLang.get("region.wilderness"):r.getName();
		}
		if (arg.equals("player_used_claims")){			
			text = String.valueOf(RedProtect.get().rm.getPlayerRegions(RPUtil.PlayerToUUID(p.getName()), p.getWorld()));
		}
		if (arg.equals("player_used_blocks")){
			text = String.valueOf(RedProtect.get().rm.getTotalRegionSize(RPUtil.PlayerToUUID(p.getName()), p.getWorld().getName()));
		}
		if (arg.equals("player_total_claims")){
			int l = RedProtect.get().ph.getPlayerClaimLimit(p);
			text = l == -1 ? RPLang.get("regionbuilder.area.unlimited"):String.valueOf(l);
		}
		if (arg.equals("player_total_blocks")){
			int l = RedProtect.get().ph.getPlayerBlockLimit(p);
			text = l == -1 ? RPLang.get("regionbuilder.area.unlimited"):String.valueOf(l);
		}	
		if (arg.startsWith("region_flag_value_")){
			Region r = RedProtect.get().rm.getTopRegion(p.getLocation());			
			if (r != null){
				String value = r.getFlagString(arg.replace("region_flag_value_", ""));
				if (value == null){
					return null;
				}				
				if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")){
					return RPLang.translBool(value);
				}
				text = value;
			} else {
				text = "--";
			}
		}
		
		return text;
	}
}
