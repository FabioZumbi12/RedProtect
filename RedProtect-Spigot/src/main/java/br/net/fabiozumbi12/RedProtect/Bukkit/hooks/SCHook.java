package br.net.fabiozumbi12.RedProtect.Bukkit.hooks;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPConfig;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import org.bukkit.entity.Player;

public class SCHook {
    public static boolean getPlayerClan(Region r, Player p) {
        ClanPlayer clan = RedProtect.get().clanManager.getClanPlayer(p);
        return clan != null && clan.getTag().equalsIgnoreCase(r.getFlagString("clan"));
    }

    @SuppressWarnings("deprecation")
    public static boolean inWar(Region r, Player attack, Player defend) {
        if (!RPConfig.getBool("hooks.simpleclans.use-war")) {
            return false;
        }
        if (!RPConfig.getBool("hooks.simpleclans.war-on-server-regions") && r.getLeaders().contains(RPConfig.getString("region-settings.default-leader"))) {
            return false;
        }
        ClanPlayer atClan = RedProtect.get().clanManager.getClanPlayer(attack);
        if (atClan == null) {
            return false;
        }
        ClanPlayer defCclan = RedProtect.get().clanManager.getClanPlayer(defend);
        return defCclan != null && atClan.getClan().isWarring(defCclan.getClan());
    }
}
