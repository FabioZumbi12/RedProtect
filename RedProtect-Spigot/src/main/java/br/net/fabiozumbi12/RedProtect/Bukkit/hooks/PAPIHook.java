/*
 * Copyright (c) 2012-2024 - @FabioZumbi12
 * Last Modified: 26/11/2024 17:51
 *
 * This class is provided 'as-is', without any express or implied warranty. In no event will the authors be held liable for any
 *  damages arising from the use of this class.
 *
 * Permission is granted to anyone to use this class for any purpose, including commercial plugins, and to alter it and
 * redistribute it freely, subject to the following restrictions:
 * 1 - The origin of this class must not be misrepresented; you must not claim that you wrote the original software. If you
 * use this class in other plugins, an acknowledgment in the plugin documentation would be appreciated but is not required.
 * 2 - Altered source versions must be plainly marked as such, and must not be misrepresented as being the original class.
 * 3 - This notice may not be removed or altered from any source distribution.
 *
 * Esta classe é fornecida "como está", sem qualquer garantia expressa ou implícita. Em nenhum caso os autores serão
 * responsabilizados por quaisquer danos decorrentes do uso desta classe.
 *
 * É concedida permissão a qualquer pessoa para usar esta classe para qualquer finalidade, incluindo plugins pagos, e para
 * alterá-lo e redistribuí-lo livremente, sujeito às seguintes restrições:
 * 1 - A origem desta classe não deve ser deturpada; você não deve afirmar que escreveu a classe original. Se você usar esta
 *  classe em um plugin, uma confirmação de autoria na documentação do plugin será apreciada, mas não é necessária.
 * 2 - Versões de origem alteradas devem ser claramente marcadas como tal e não devem ser deturpadas como sendo a
 * classe original.
 * 3 - Este aviso não pode ser removido ou alterado de qualquer distribuição de origem.
 */

package br.net.fabiozumbi12.RedProtect.Bukkit.hooks;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import br.net.fabiozumbi12.RedProtect.Core.region.CoreRegion;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PAPIHook extends PlaceholderExpansion {

    @Override
    public String onPlaceholderRequest(Player p, String arg) {
        if (arg.equals("player_in_region")) {
            Region r = RedProtect.get().getRegionManager().getTopRegion(p.getLocation());
            return r == null ? RedProtect.get().getLanguageManager().get("region.wilderness") : r.getName();
        } else if (arg.equals("player_used_claims")) {
            return String.valueOf(RedProtect.get().getRegionManager().getPlayerRegions(p.getUniqueId().toString(), p.getWorld().getName()));
        } else if (arg.equals("player_used_blocks")) {
            return String.valueOf(RedProtect.get().getRegionManager().getTotalRegionSize(p.getUniqueId().toString(), p.getWorld().getName()));
        } else if (arg.equals("player_total_claims")) {
            int l = RedProtect.get().getPermissionHandler().getPlayerClaimLimit(p);
            return l == -1 ? RedProtect.get().getLanguageManager().get("regionbuilder.area.unlimited") : String.valueOf(l);
        } else if (arg.equals("player_total_blocks")) {
            int l = RedProtect.get().getPermissionHandler().getPlayerBlockLimit(p);
            return l == -1 ? RedProtect.get().getLanguageManager().get("regionbuilder.area.unlimited") : String.valueOf(l);
        } else if (arg.startsWith("region_flag_value_")) {
            Region r = RedProtect.get().getRegionManager().getTopRegion(p.getLocation());
            if (r != null) {
                String value = r.getFlagString(arg.replace("region_flag_value_", ""));
                if (value == null) {
                    return null;
                }
                if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                    return RedProtect.get().getLanguageManager().translBool(value);
                }
                return value;
            } else {
                return "--";
            }
        } else if (arg.equals("player_region_names_leader")) {
            List<String> regions = RedProtect.get().getRegionManager().getLeaderRegions(p.getUniqueId().toString())
                    .stream().map(CoreRegion::getName).toList();
            return String.join(", ", regions);
        } else if (arg.equals("player_region_names_admin")) {
            List<String> regions = RedProtect.get().getRegionManager().getAdminRegions(p.getUniqueId().toString())
                    .stream().map(CoreRegion::getName).toList();
            return String.join(", ", regions);
        } else if (arg.equals("player_region_names_member")) {
            List<String> regions = RedProtect.get().getRegionManager().getMemberRegions(p.getUniqueId().toString())
                    .stream().map(CoreRegion::getName).toList();
            return String.join(", ", regions);
        } else if (arg.startsWith("player_region_names_leader_")) {
            List<String> regions = new ArrayList<>();
            var worldName = Bukkit.getWorld(arg.replace("player_region_names_leader_", ""));
            if (worldName != null){
                regions = RedProtect.get().getRegionManager().getLeaderRegions(p.getUniqueId().toString(), worldName.getName())
                        .stream().map(CoreRegion::getName).toList();
            }
            return String.join(", ", regions);
        } else if (arg.startsWith("player_region_names_admin_")) {
            List<String> regions = new ArrayList<>();
            var worldName = Bukkit.getWorld(arg.replace("player_region_names_admin_", ""));
            if (worldName != null){
                regions = RedProtect.get().getRegionManager().getAdminRegions(p.getUniqueId().toString(), worldName.getName())
                        .stream().map(CoreRegion::getName).toList();
            }
            return String.join(", ", regions);
        } else if (arg.startsWith("player_region_names_member_")) {
            List<String> regions = new ArrayList<>();
            var worldName = Bukkit.getWorld(arg.replace("player_region_names_member_", ""));
            if (worldName != null){
                regions = RedProtect.get().getRegionManager().getMemberRegions(p.getUniqueId().toString(), worldName.getName())
                        .stream().map(CoreRegion::getName).toList();
            }
            return String.join(", ", regions);
        } else if (arg.equals("region_value")) {
            Region r = RedProtect.get().getRegionManager().getTopRegion(p.getLocation());
            if (r != null)
                return String.valueOf(r.getValue());
            return "0";
        } else if (arg.equals("region_area")) {
            Region r = RedProtect.get().getRegionManager().getTopRegion(p.getLocation());
            if (r != null)
                return String.valueOf(r.getArea());
            return "0";
        } else if (arg.equals("region_welcome")) {
            Region r = RedProtect.get().getRegionManager().getTopRegion(p.getLocation());
            if (r != null)
                return r.getWelcome();
            return "--";
        } else if (arg.equals("region_center_x")) {
            Region r = RedProtect.get().getRegionManager().getTopRegion(p.getLocation());
            if (r != null)
                return String.valueOf(r.getCenterX());
            return "0";
        } else if (arg.equals("region_center_y")) {
            Region r = RedProtect.get().getRegionManager().getTopRegion(p.getLocation());
            if (r != null)
                return String.valueOf(r.getCenterY());
            return "0";
        } else if (arg.equals("region_center_z")) {
            Region r = RedProtect.get().getRegionManager().getTopRegion(p.getLocation());
            if (r != null)
                return String.valueOf(r.getCenterZ());
            return "0";
        } else {
            return null;
        }
    }

    @Override
    public String getIdentifier() {
        return "redprotect";
    }

    @Override
    public String getAuthor() {
        return String.join(", ", RedProtect.get().getDescription().getAuthors());
    }

    @Override
    public String getVersion() {
        return RedProtect.get().getDescription().getVersion();
    }
}
