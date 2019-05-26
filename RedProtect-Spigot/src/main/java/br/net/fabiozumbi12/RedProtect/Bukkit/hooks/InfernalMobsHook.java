package br.net.fabiozumbi12.RedProtect.Bukkit.hooks;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import io.hotmail.com.jacob_vejvoda.infernal_mobs.InfernalSpawnEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class InfernalMobsHook implements Listener {

    InfernalMobsHook() throws ClassNotFoundException {
        Class.forName("io.hotmail.com.jacob_vejvoda.infernal_mobs.InfernalSpawnEvent");
    }

    @EventHandler
    public void onInfernalSpawn(InfernalSpawnEvent event){
        Region r = RedProtect.get().rm.getTopRegion(event.getEntity().getLocation());
        if (r != null){
            if (r.isLeader(RedProtect.get().config.configRoot().region_settings.default_leader)){
                if (!RedProtect.get().config.configRoot().hooks.infernal_mobs.allow_server_regions)
                    event.setCancelled(true);
            } else if (!RedProtect.get().config.configRoot().hooks.infernal_mobs.allow_player_regions){
                event.setCancelled(true);
            }
        }
    }
}
