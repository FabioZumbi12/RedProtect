package br.net.fabiozumbi12.RedProtect.hooks;

import me.clip.placeholderapi.external.EZPlaceholderHook;

import org.bukkit.entity.Player;

import br.net.fabiozumbi12.RedProtect.RPUtil;
import br.net.fabiozumbi12.RedProtect.RedProtect;
import br.net.fabiozumbi12.RedProtect.Region;
import br.net.fabiozumbi12.RedProtect.config.RPLang;

public class RPPlaceHoldersAPI extends EZPlaceholderHook {

	public RPPlaceHoldersAPI(RedProtect plugin) {
		super(plugin, "redprotect");
	}

	@Override
	public String onPlaceholderRequest(Player p, String arg) {
		if (arg.equals("player_in_region")){
			return RedProtect.rm.getTopRegion(p.getLocation()).getName();
		}
		if (arg.equals("player_used_claims")){
			return String.valueOf(RedProtect.rm.getPlayerRegions(RPUtil.PlayerToUUID(p.getName()), p.getWorld().getName()));
		}
		if (arg.equals("player_used_blocks")){
			return String.valueOf(RedProtect.rm.getTotalRegionSize(RPUtil.PlayerToUUID(p.getName())));
		}
		if (arg.equals("player_total_claims")){
			return String.valueOf(RedProtect.ph.getPlayerClaimLimit(p));
		}
		if (arg.equals("player_total_blocks")){
			return String.valueOf(RedProtect.ph.getPlayerBlockLimit(p));
		}	
		if (arg.startsWith("region_flag_value_")){
			Region r = RedProtect.rm.getTopRegion(p.getLocation());
			String value = r.getFlagString(arg.replace("region_flag_value_", ""));
			if (r != null && value != null){				
				if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")){
					return RPLang.translBool(value);
				}
				return value;
			}
		}
		return null;
	}
}
