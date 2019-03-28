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

import br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPUtil;
import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.region.Region;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPLang;
import me.clip.placeholderapi.external.EZPlaceholderHook;
import org.bukkit.entity.Player;

public class RPPlaceHoldersAPI extends EZPlaceholderHook {

    public RPPlaceHoldersAPI(RedProtect plugin) {
        super(plugin, "redprotect");
    }

    @Override
    public String onPlaceholderRequest(Player p, String arg) {
        String text = "";
        if (arg.equals("player_in_region")) {
            Region r = RedProtect.get().rm.getTopRegion(p.getLocation());
            text = r == null ? RPLang.get("region.wilderness") : r.getName();
        }
        if (arg.equals("player_used_claims")) {
            text = String.valueOf(RedProtect.get().rm.getPlayerRegions(RPUtil.PlayerToUUID(p.getName()), p.getWorld()));
        }
        if (arg.equals("player_used_blocks")) {
            text = String.valueOf(RedProtect.get().rm.getTotalRegionSize(RPUtil.PlayerToUUID(p.getName()), p.getWorld().getName()));
        }
        if (arg.equals("player_total_claims")) {
            int l = RedProtect.get().ph.getPlayerClaimLimit(p);
            text = l == -1 ? RPLang.get("regionbuilder.area.unlimited") : String.valueOf(l);
        }
        if (arg.equals("player_total_blocks")) {
            int l = RedProtect.get().ph.getPlayerBlockLimit(p);
            text = l == -1 ? RPLang.get("regionbuilder.area.unlimited") : String.valueOf(l);
        }
        if (arg.startsWith("region_flag_value_")) {
            Region r = RedProtect.get().rm.getTopRegion(p.getLocation());
            if (r != null) {
                String value = r.getFlagString(arg.replace("region_flag_value_", ""));
                if (value == null) {
                    return null;
                }
                if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
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
