/*
 *
 * Copyright (c) 2019 - @FabioZumbi12
 * Last Modified: 28/03/19 20:18
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
 *
 */

package br.net.fabiozumbi12.RedProtect.Bukkit;

import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPConfig;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class RPPermissionHandler {

    public boolean hasCommandPerm(CommandSender p, String s) {
        String adminperm = "redprotect.command.admin." + s;
        String userperm = "redprotect.command." + s;
        return this.hasPerm(p, adminperm) || this.hasPerm(p, userperm);
    }

    public boolean hasFlagPerm(Player p, String flag){
        String adminperm = "redprotect.flag.admin." + flag;
        String userperm = "redprotect.flag." + flag;
        return this.hasPerm(p, adminperm) || this.hasPerm(p, userperm);
    }

    public boolean hasPermOrBypass(Player p, String perm) {
        return p.hasPermission(perm) || p.hasPermission(perm + ".bypass");
    }

    public boolean hasPerm(Player p, String perm) {
        return p != null && (p.hasPermission(perm) || p.isOp());
    }

    public boolean hasPerm(CommandSender p, String perm) {
        return p != null && (p.hasPermission(perm) || p.isOp());
    }

    public boolean hasRegionPermMember(Player p, String s, Region poly) {
        return regionPermMember(p, s, poly);
    }

    public boolean hasRegionPermAdmin(Player p, String s, Region poly) {
        return regionPermAdmin(p, s, poly);
    }

    public boolean hasRegionPermAdmin(CommandSender sender, String s, Region poly) {
        return !(sender instanceof Player) || regionPermAdmin((Player) sender, s, poly);
    }

    public boolean hasRegionPermLeader(Player p, String s, Region poly) {
        return regionPermLeader(p, s, poly);
    }

    public boolean hasRegionPermLeader(CommandSender sender, String s, Region poly) {
        return !(sender instanceof Player) || regionPermLeader((Player) sender, s, poly);
    }

    public int getPlayerBlockLimit(Player p) {
        return BlockLimitHandler(p);
    }

    public int getPlayerClaimLimit(Player p) {
        return ClaimLimitHandler(p);
    }

    private boolean regionPermLeader(Player p, String s, Region poly) {
        String adminperm = "redprotect.command.admin." + s;
        String userperm = "redprotect.command." + s;
        if (poly == null) {
            return this.hasPerm(p, adminperm) || this.hasPerm(p, userperm);
        }
        return this.hasPerm(p, adminperm) || (this.hasPerm(p, userperm) && poly.isLeader(p));
    }

    private boolean regionPermAdmin(Player p, String s, Region poly) {
        String adminperm = "redprotect.command.admin." + s;
        String userperm = "redprotect.command." + s;
        if (poly == null) {
            return this.hasPerm(p, adminperm) || this.hasPerm(p, userperm);
        }
        return this.hasPerm(p, adminperm) || (this.hasPerm(p, userperm) && (poly.isLeader(p) || poly.isAdmin(p)));
    }

    private boolean regionPermMember(Player p, String s, Region poly) {
        String adminperm = "redprotect.command.admin." + s;
        String userperm = "redprotect.command." + s;
        if (poly == null) {
            return this.hasPerm(p, adminperm) || this.hasPerm(p, userperm);
        }
        return this.hasPerm(p, adminperm) || (this.hasPerm(p, userperm) && (poly.isLeader(p) || poly.isAdmin(p) || poly.isMember(p)));
    }

    private int BlockLimitHandler(Player p) {
        int limit = RPConfig.getInt("region-settings.limit-amount");
        List<Integer> limits = new ArrayList<>();
        Set<PermissionAttachmentInfo> perms = p.getEffectivePermissions();
        if (limit > 0) {
            if (!p.hasPermission("redprotect.limits.blocks.unlimited")) {
                for (PermissionAttachmentInfo perm : perms) {
                    if (perm.getPermission().startsWith("redprotect.limits.blocks.")) {
                        String pStr = perm.getPermission().replaceAll("[^-?0-9]+", "");
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

    private int ClaimLimitHandler(Player p) {
        int limit = RPConfig.getInt("region-settings.claim-amount");
        List<Integer> limits = new ArrayList<>();
        Set<PermissionAttachmentInfo> perms = p.getEffectivePermissions();
        if (limit > 0) {
            if (!p.hasPermission("redprotect.limits.claim.unlimited")) {
                for (PermissionAttachmentInfo perm : perms) {
                    if (perm.getPermission().startsWith("redprotect.limits.claim.")) {
                        String pStr = perm.getPermission().replaceAll("[^-?0-9]+", "");
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
