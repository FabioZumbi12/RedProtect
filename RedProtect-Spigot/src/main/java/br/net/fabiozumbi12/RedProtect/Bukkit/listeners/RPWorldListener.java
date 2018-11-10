/*
 Copyright @FabioZumbi12

 This class is provided 'as-is', without any express or implied warranty. In no event will the authors be held liable for any
  damages arising from the use of this software.

 Permission is granted to anyone to use this class for any purpose, including commercial plugins, and to alter it and
 redistribute it freely, subject to the following restrictions:
 1 - The origin of this class must not be misrepresented; you must not claim that you wrote the original software. If you
 use this class in other plugins, an acknowledgment in the plugin documentation would be appreciated but is not required.
 2 - Altered source versions must be plainly marked as such, and must not be misrepresented as being the original class.
 3 - This notice may not be removed or altered from any source distribution.

 Esta classe é fornecida "como está", sem qualquer garantia expressa ou implícita. Em nenhum caso os autores serão
 responsabilizados por quaisquer danos decorrentes do uso deste software.

 É concedida permissão a qualquer pessoa para usar esta classe para qualquer finalidade, incluindo plugins pagos, e para
 alterá-lo e redistribuí-lo livremente, sujeito às seguintes restrições:
 1 - A origem desta classe não deve ser deturpada; você não deve afirmar que escreveu a classe original. Se você usar esta
  classe em um plugin, uma confirmação de autoria na documentação do plugin será apreciada, mas não é necessária.
 2 - Versões de origem alteradas devem ser claramente marcadas como tal e não devem ser deturpadas como sendo a
 classe original.
 3 - Este aviso não pode ser removido ou alterado de qualquer distribuição de origem.
 */

package br.net.fabiozumbi12.RedProtect.Bukkit.listeners;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPConfig;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import java.util.HashMap;

public class RPWorldListener implements Listener {

    private final HashMap<World, Integer> rainCounter = new HashMap<>();

    public RPWorldListener() {
        RedProtect.get().logger.debug("Loaded RPEntityListener...");
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onWeatherChange(WeatherChangeEvent e) {
        World w = e.getWorld();
        int trys = RPConfig.getGlobalFlagInt(w.getName() + ".rain.trys-before-rain");
        if (e.toWeatherState()) {
            if (!rainCounter.containsKey(w)) {
                rainCounter.put(w, trys);
                e.setCancelled(true);
            } else {
                int acTry = rainCounter.get(w);
                if (acTry - 1 <= 0) {
                    Bukkit.getScheduler().runTaskLater(RedProtect.get(), () -> w.setWeatherDuration(RPConfig.getGlobalFlagInt(w.getName() + ".rain.duration") * 20), 40);
                    rainCounter.put(w, trys);
                } else {
                    rainCounter.put(w, acTry - 1);
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onWorldLoad(WorldLoadEvent e) {
        World w = e.getWorld();
        try {
            RedProtect.get().rm.load(w);
            RPConfig.init();
            RedProtect.get().logger.warning("World loaded: " + w.getName());
        } catch (Exception ex) {
            RedProtect.get().logger.severe("RedProtect problem on load world:");
            ex.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onWorldUnload(WorldUnloadEvent e) {
        World w = e.getWorld();
        try {
            RedProtect.get().rm.unload(w);
            RedProtect.get().logger.warning("World unloaded: " + w.getName());
        } catch (Exception ex) {
            RedProtect.get().logger.severe("RedProtect problem on unload world:");
            ex.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onChunkUnload(ChunkLoadEvent e) {
        if (!RPConfig.getGlobalFlagBool("remove-entities-not-allowed-to-spawn")) {
            return;
        }
        Entity[] ents = e.getChunk().getEntities();
        for (Entity ent : ents) {
            Region entr = RedProtect.get().rm.getTopRegion(ent.getLocation());
            if (entr != null) {
                if (!entr.canSpawnMonsters() && ent instanceof Monster) {
                    ent.remove();
                }
            } else {
                if (ent instanceof Monster) {
                    if (!RPConfig.getGlobalFlagBool("spawn-monsters")) {
                        ent.remove();
                    }
                } else if (!RPConfig.getGlobalFlagBool("spawn-passives")) {
                    if (ent instanceof Tameable) {
                        return;
                    }
                    ent.remove();
                }
            }

        }
    }
}
