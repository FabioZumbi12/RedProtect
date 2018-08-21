package br.net.fabiozumbi12.RedProtect.Bukkit.hooks;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPConfig;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPLang;
import com.massivecraft.factions.event.EventFactionsChunksChange;
import com.massivecraft.factions.event.EventFactionsExpansions;
import com.massivecraft.massivecore.ps.PS;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Set;

public class RPFactions implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onCreateFac(EventFactionsChunksChange event) {
        if (RPConfig.getBool("hooks.factions.claim-over-rps")) {
            return;
        }
        for (PS chunk : event.getChunks()) {
            Player p = event.getMPlayer().getPlayer();
            Set<Region> regs = RedProtect.get().rm.getRegionsForChunk(chunk.asBukkitChunk());
            if (regs.size() > 0 && !p.hasPermission("redprotect.bypass")) {
                event.setCancelled(true);
                RPLang.sendMessage(p, "rpfactions.cantclaim");
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onExpandFac(EventFactionsExpansions event) {
        if (RPConfig.getBool("hooks.factions.claim-over-rps")) {
            return;
        }
        Player p = event.getMPlayer().getPlayer();
        Set<Region> regs = RedProtect.get().rm.getRegionsForChunk(p.getLocation().getChunk());
        if (regs.size() > 0 && !p.hasPermission("redprotect.bypass")) {
            RPLang.sendMessage(p, "rpfactions.cantclaim");
            event.setCancelled(true);
        }
    }
}
