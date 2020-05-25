/*
 * Copyright (c) 2019 - @FabioZumbi12
 * Last Modified: 25/04/19 07:02
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

package br.net.fabiozumbi12.RedProtect.Sponge.helpers;

import br.net.fabiozumbi12.RedProtect.Core.helpers.LogLevel;
import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import br.net.fabiozumbi12.RedProtect.Sponge.Region;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PermissionHandler {

    public boolean hasCommandPerm(CommandSource p, String s) {
        String adminPerm = "redprotect.command.admin." + s;
        String userPerm = "redprotect.command." + s;
        return this.hasPerm(p, adminPerm) || this.hasPerm(p, userPerm);
    }

    public boolean hasFlagPerm(Player p, String flag) {
        String adminPerm = "redprotect.flag.admin." + flag;
        String userPerm = "redprotect.flag." + flag;
        return this.hasPerm(p, adminPerm) || this.hasPerm(p, userPerm);
    }

    public boolean hasPermOrBypass(Player p, String perm) {
        return p.hasPermission(perm) || p.hasPermission(perm + ".bypass");
    }

    public boolean hasPerm(User user, String perm) {
        return user != null && (user.hasPermission(perm) || user.hasPermission("redprotect.command.admin"));
    }

    public boolean hasPerm(Player player, String perm) {
        return player != null && (player.hasPermission(perm) || player.hasPermission("redprotect.command.admin"));
    }

    public boolean hasPerm(CommandSource source, String perm) {
        return source != null && (source.hasPermission(perm) || source.hasPermission("redprotect.command.admin"));
    }

    public boolean hasRegionPermMember(Player player, String arg, Region poly) {
        return regionPermMember(player, arg, poly);
    }

    public boolean hasRegionPermAdmin(Player player, String arg, Region poly) {
        return regionPermAdmin(player, arg, poly);
    }

    public boolean hasRegionPermAdmin(CommandSource source, String arg, Region poly) {
        return !(source instanceof Player) || regionPermAdmin((Player) source, arg, poly);
    }

    public boolean hasRegionPermLeader(Player player, String arg, Region poly) {
        return regionPermLeader(player, arg, poly);
    }

    public boolean hasRegionPermLeader(CommandSource source, String arg, Region poly) {
        return !(source instanceof Player) || regionPermLeader((Player) source, arg, poly);
    }

    public int getPlayerBlockLimit(User user) {
        return getBlockLimit(user);
    }

    public int getPlayerClaimLimit(User user) {
        return getClaimLimit(user);
    }

    private boolean regionPermLeader(Player player, String arg, Region poly) {
        String adminperm = "redprotect.command.admin." + arg;
        String userperm = "redprotect.command." + arg;
        if (poly == null) {
            return this.hasPerm(player, adminperm) || this.hasPerm(player, userperm);
        }
        return this.hasPerm(player, adminperm) || (this.hasPerm(player, userperm) && poly.isLeader(player));
    }

    private boolean regionPermAdmin(Player player, String arg, Region poly) {
        String adminperm = "redprotect.command.admin." + arg;
        String userperm = "redprotect.command." + arg;
        if (poly == null) {
            return this.hasPerm(player, adminperm) || this.hasPerm(player, userperm);
        }
        return this.hasPerm(player, adminperm) || (this.hasPerm(player, userperm) && (poly.isLeader(player) || poly.isAdmin(player)));
    }

    private boolean regionPermMember(Player player, String arg, Region poly) {
        String adminperm = "redprotect.command.admin." + arg;
        String userperm = "redprotect.command." + arg;
        if (poly == null) {
            return this.hasPerm(player, adminperm) || this.hasPerm(player, userperm);
        }
        return this.hasPerm(player, adminperm) || (this.hasPerm(player, userperm) && (poly.isLeader(player) || poly.isAdmin(player) || poly.isMember(player)));
    }

    public int getPurgeLimit(User user) {
        return RedProtect.get().config.configRoot().purge.canpurge_limit;
    }

    private int getBlockLimit(User user) {
        int limit = RedProtect.get().config.configRoot().region_settings.limit_amount;
        List<Integer> limits = new ArrayList<>();
        if (limit > 0) {
            if (!user.hasPermission("redprotect.limits.blocks.unlimited")) {
                for (String perm : RedProtect.get().config.configRoot().permissions_limits.blocks) {
                    RedProtect.get().logger.debug(LogLevel.DEFAULT, "Perm: " + perm);
                    if (user.hasPermission(perm)) {
                        RedProtect.get().logger.debug(LogLevel.DEFAULT, "Has block perm: " + perm);
                        String pStr = perm.replaceAll("[^-?0-9]+", "");
                        if (!pStr.isEmpty()) {
                            limits.add(Integer.parseInt(pStr));
                        }
                    }
                }
            } else {
                return -1;
            }
        }
        if (limits.size() > 0) {
            limit = Collections.max(limits);
        }
        return limit;
    }

    private int getClaimLimit(User user) {
        int limit = RedProtect.get().config.configRoot().region_settings.claim.amount_per_player;
        List<Integer> limits = new ArrayList<>();
        if (limit > 0) {
            if (!user.hasPermission("redprotect.limits.claim.unlimited")) {
                for (String perm : RedProtect.get().config.configRoot().permissions_limits.claims) {
                    RedProtect.get().logger.debug(LogLevel.DEFAULT, "Perm: " + perm);
                    if (user.hasPermission(perm)) {
                        RedProtect.get().logger.debug(LogLevel.DEFAULT, "Has claim perm: " + perm);
                        String pStr = perm.replaceAll("[^-?0-9]+", "");
                        if (!pStr.isEmpty()) {
                            limits.add(Integer.parseInt(pStr));
                        }
                    }
                }
            } else {
                return -1;
            }
        }
        if (limits.size() > 0) {
            limit = Collections.max(limits);
        }
        return limit;
    }
}
