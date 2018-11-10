/*
 Copyright @FabioZumbi12

 This class is provided 'as-is', without any express or implied warranty. In no event will the authors be held liable for any
  damages arising from the use of this class.

 Permission is granted to anyone to use this class for any purpose, including commercial plugins, and to alter it and
 redistribute it freely, subject to the following restrictions:
 1 - The origin of this class must not be misrepresented; you must not claim that you wrote the original software. If you
 use this class in other plugins, an acknowledgment in the plugin documentation would be appreciated but is not required.
 2 - Altered source versions must be plainly marked as such, and must not be misrepresented as being the original class.
 3 - This notice may not be removed or altered from any source distribution.

 Esta classe é fornecida "como está", sem qualquer garantia expressa ou implícita. Em nenhum caso os autores serão
 responsabilizados por quaisquer danos decorrentes do uso desta classe.

 É concedida permissão a qualquer pessoa para usar esta classe para qualquer finalidade, incluindo plugins pagos, e para
 alterá-lo e redistribuí-lo livremente, sujeito às seguintes restrições:
 1 - A origem desta classe não deve ser deturpada; você não deve afirmar que escreveu a classe original. Se você usar esta
  classe em um plugin, uma confirmação de autoria na documentação do plugin será apreciada, mas não é necessária.
 2 - Versões de origem alteradas devem ser claramente marcadas como tal e não devem ser deturpadas como sendo a
 classe original.
 3 - Este aviso não pode ser removido ou alterado de qualquer distribuição de origem.
 */

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
