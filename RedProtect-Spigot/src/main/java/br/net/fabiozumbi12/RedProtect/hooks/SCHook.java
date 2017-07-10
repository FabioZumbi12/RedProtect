package br.net.fabiozumbi12.RedProtect.hooks;

import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;

import org.bukkit.entity.Player;

import br.net.fabiozumbi12.RedProtect.RedProtect;
import br.net.fabiozumbi12.RedProtect.Region;

public class SCHook {
	public static boolean getPlayerClan(Region r,Player p){		
		ClanPlayer clan = RedProtect.clanManager.getClanPlayer(p);
		return clan != null && clan.getTag().equalsIgnoreCase(r.getFlagString("clan"));
	}
		
	public static boolean inWar(Region r, Player attack, Player defend){
		ClanPlayer atClan = RedProtect.clanManager.getClanPlayer(attack);
		if (atClan == null){
			return false;
		}
		ClanPlayer defCclan = RedProtect.clanManager.getClanPlayer(defend);
		if (defCclan == null){
			return false;
		}		
		return atClan.getClan().isWarring(defCclan.getClan());
	}
}
