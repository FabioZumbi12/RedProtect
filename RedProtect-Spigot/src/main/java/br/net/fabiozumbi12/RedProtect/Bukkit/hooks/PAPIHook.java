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
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PAPIHook extends PlaceholderExpansion {

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String getRequiredPlugin() {
        return "RedProtect";
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

    @Override
    public String onPlaceholderRequest(Player p, String arg) {
        return onRequest(p, arg);
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, String arg) {
        if (arg == null || RedProtect.get() == null || RedProtect.get().getRegionManager() == null) {
            return null;
        }

        Player p = (offlinePlayer != null && offlinePlayer.isOnline()) ? offlinePlayer.getPlayer() : null;
        String lowerArg = arg.toLowerCase(Locale.ROOT);

        if (lowerArg.equals("player_in_region") || lowerArg.equals("in_region")) {
            if (p == null || p.getLocation() == null) {
                return RedProtect.get().getLanguageManager().get("region.wilderness");
            }
            Region r = RedProtect.get().getRegionManager().getTopRegion(p.getLocation());
            return r == null ? RedProtect.get().getLanguageManager().get("region.wilderness") : r.getName();
        } else if (lowerArg.equals("player_used_claims") || lowerArg.equals("used_claims")) {
            String uuid = offlinePlayer != null ? offlinePlayer.getUniqueId().toString() : (p != null ? p.getUniqueId().toString() : null);
            if (uuid == null) {
                return "0";
            }
            if (p != null && p.getWorld() != null) {
                try {
                    return String.valueOf(RedProtect.get().getRegionManager().getPlayerRegions(uuid, p.getWorld().getName()));
                } catch (Exception ignored) {
                }
            }
            try {
                return String.valueOf(RedProtect.get().getRegionManager().getLeaderRegions(uuid).size());
            } catch (Exception ignored) {
                return "0";
            }
        } else if (lowerArg.equals("player_used_blocks") || lowerArg.equals("used_blocks")) {
            String uuid = offlinePlayer != null ? offlinePlayer.getUniqueId().toString() : (p != null ? p.getUniqueId().toString() : null);
            if (uuid == null) {
                return "0";
            }
            if (p != null && p.getWorld() != null) {
                try {
                    return String.valueOf(RedProtect.get().getRegionManager().getTotalRegionSize(uuid, p.getWorld().getName()));
                } catch (Exception ignored) {
                }
            }
            try {
                int total = 0;
                for (org.bukkit.World w : Bukkit.getWorlds()) {
                    total += RedProtect.get().getRegionManager().getTotalRegionSize(uuid, w.getName());
                }
                return String.valueOf(total);
            } catch (Exception ignored) {
                return "0";
            }
        } else if (lowerArg.equals("player_total_claims") || lowerArg.equals("total_claims")) {
            int l = -1;
            if (p != null) {
                try {
                    l = RedProtect.get().getPermissionHandler().getPlayerClaimLimit(p);
                } catch (Exception ignored) {
                    l = RedProtect.get().getConfigManager().configRoot().region_settings.claim.amount_per_player;
                }
            } else {
                try {
                    l = RedProtect.get().getConfigManager().configRoot().region_settings.claim.amount_per_player;
                } catch (Exception ignored) {
                    l = 20;
                }
            }
            return l == -1 ? RedProtect.get().getLanguageManager().get("regionbuilder.area.unlimited") : String.valueOf(l);
        } else if (lowerArg.equals("player_total_blocks") || lowerArg.equals("total_blocks")) {
            int l = -1;
            if (p != null) {
                try {
                    l = RedProtect.get().getPermissionHandler().getPlayerBlockLimit(p);
                } catch (Exception ignored) {
                    l = RedProtect.get().getConfigManager().configRoot().region_settings.limit_amount;
                }
            } else {
                try {
                    l = RedProtect.get().getConfigManager().configRoot().region_settings.limit_amount;
                } catch (Exception ignored) {
                    l = 8000;
                }
            }
            return l == -1 ? RedProtect.get().getLanguageManager().get("regionbuilder.area.unlimited") : String.valueOf(l);
        } else if (lowerArg.startsWith("region_flag_value_")) {
            if (p == null || p.getLocation() == null) {
                return "--";
            }
            Region r = RedProtect.get().getRegionManager().getTopRegion(p.getLocation());
            if (r != null) {
                String flagName = lowerArg.replace("region_flag_value_", "");
                String value = r.getFlagString(flagName);
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
        } else if (lowerArg.equals("player_region_names_leader")) {
            String uuid = offlinePlayer != null ? offlinePlayer.getUniqueId().toString() : (p != null ? p.getUniqueId().toString() : null);
            if (uuid == null) return "";
            try {
                List<String> regions = RedProtect.get().getRegionManager().getLeaderRegions(uuid)
                        .stream().map(CoreRegion::getName).toList();
                return String.join(", ", regions);
            } catch (Exception ignored) {
                return "";
            }
        } else if (lowerArg.equals("player_region_names_admin")) {
            String uuid = offlinePlayer != null ? offlinePlayer.getUniqueId().toString() : (p != null ? p.getUniqueId().toString() : null);
            if (uuid == null) return "";
            try {
                List<String> regions = RedProtect.get().getRegionManager().getAdminRegions(uuid)
                        .stream().map(CoreRegion::getName).toList();
                return String.join(", ", regions);
            } catch (Exception ignored) {
                return "";
            }
        } else if (lowerArg.equals("player_region_names_member")) {
            String uuid = offlinePlayer != null ? offlinePlayer.getUniqueId().toString() : (p != null ? p.getUniqueId().toString() : null);
            if (uuid == null) return "";
            try {
                List<String> regions = RedProtect.get().getRegionManager().getMemberRegions(uuid)
                        .stream().map(CoreRegion::getName).toList();
                return String.join(", ", regions);
            } catch (Exception ignored) {
                return "";
            }
        } else if (lowerArg.startsWith("player_region_names_leader_")) {
            String uuid = offlinePlayer != null ? offlinePlayer.getUniqueId().toString() : (p != null ? p.getUniqueId().toString() : null);
            if (uuid == null) return "";
            List<String> regions = new ArrayList<>();
            var worldName = Bukkit.getWorld(lowerArg.replace("player_region_names_leader_", ""));
            if (worldName != null) {
                try {
                    regions = RedProtect.get().getRegionManager().getLeaderRegions(uuid, worldName.getName())
                            .stream().map(CoreRegion::getName).toList();
                } catch (Exception ignored) {
                }
            }
            return String.join(", ", regions);
        } else if (lowerArg.startsWith("player_region_names_admin_")) {
            String uuid = offlinePlayer != null ? offlinePlayer.getUniqueId().toString() : (p != null ? p.getUniqueId().toString() : null);
            if (uuid == null) return "";
            List<String> regions = new ArrayList<>();
            var worldName = Bukkit.getWorld(lowerArg.replace("player_region_names_admin_", ""));
            if (worldName != null) {
                try {
                    regions = RedProtect.get().getRegionManager().getAdminRegions(uuid, worldName.getName())
                            .stream().map(CoreRegion::getName).toList();
                } catch (Exception ignored) {
                }
            }
            return String.join(", ", regions);
        } else if (lowerArg.startsWith("player_region_names_member_")) {
            String uuid = offlinePlayer != null ? offlinePlayer.getUniqueId().toString() : (p != null ? p.getUniqueId().toString() : null);
            if (uuid == null) return "";
            List<String> regions = new ArrayList<>();
            var worldName = Bukkit.getWorld(lowerArg.replace("player_region_names_member_", ""));
            if (worldName != null) {
                try {
                    regions = RedProtect.get().getRegionManager().getMemberRegions(uuid, worldName.getName())
                            .stream().map(CoreRegion::getName).toList();
                } catch (Exception ignored) {
                }
            }
            return String.join(", ", regions);
        } else if (lowerArg.equals("region_value")) {
            if (p == null || p.getLocation() == null) return "0";
            Region r = RedProtect.get().getRegionManager().getTopRegion(p.getLocation());
            if (r != null)
                return String.valueOf(r.getValue());
            return "0";
        } else if (lowerArg.equals("region_area")) {
            if (p == null || p.getLocation() == null) return "0";
            Region r = RedProtect.get().getRegionManager().getTopRegion(p.getLocation());
            if (r != null)
                return String.valueOf(r.getArea());
            return "0";
        } else if (lowerArg.equals("region_welcome")) {
            if (p == null || p.getLocation() == null) return "--";
            Region r = RedProtect.get().getRegionManager().getTopRegion(p.getLocation());
            if (r != null)
                return r.getWelcome();
            return "--";
        } else if (lowerArg.equals("region_center_x")) {
            if (p == null || p.getLocation() == null) return "0";
            Region r = RedProtect.get().getRegionManager().getTopRegion(p.getLocation());
            if (r != null)
                return String.valueOf(r.getCenterX());
            return "0";
        } else if (lowerArg.equals("region_center_y")) {
            if (p == null || p.getLocation() == null) return "0";
            Region r = RedProtect.get().getRegionManager().getTopRegion(p.getLocation());
            if (r != null)
                return String.valueOf(r.getCenterY());
            return "0";
        } else if (lowerArg.equals("region_center_z")) {
            if (p == null || p.getLocation() == null) return "0";
            Region r = RedProtect.get().getRegionManager().getTopRegion(p.getLocation());
            if (r != null)
                return String.valueOf(r.getCenterZ());
            return "0";
        }

        return null;
    }
}
