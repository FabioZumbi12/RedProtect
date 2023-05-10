/*
 * Copyright (c) 2012-2023 - @FabioZumbi12
 * Last Modified: 10/05/2023 14:49
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

package br.net.fabiozumbi12.RedProtect.Bukkit.commands;

import br.net.fabiozumbi12.RedProtect.Bukkit.API.events.DeleteRegionEvent;
import br.net.fabiozumbi12.RedProtect.Bukkit.API.events.RenameRegionEvent;
import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import br.net.fabiozumbi12.RedProtect.Bukkit.guis.ItemFlagGui;
import br.net.fabiozumbi12.RedProtect.Bukkit.guis.MobFlagGui;
import br.net.fabiozumbi12.RedProtect.Core.config.CoreConfigManager;
import br.net.fabiozumbi12.RedProtect.Core.helpers.Replacer;
import br.net.fabiozumbi12.UltimateFancy.UltimateFancy;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.stream.Collectors;

public class CommandHandlers {

    // TODO Region handlers
    public static void handleAddLeader(CommandSender src, String sVictim, Region r) {
        if (src instanceof Player p) {
            r = RedProtect.get().getRegionManager().getTopRegion(p.getLocation());
            if (r == null) {
                RedProtect.get().getLanguageManager().sendMessage(p, "cmdmanager.region.todo.that");
                return;
            }
        }

        if (RedProtect.get().getPermissionHandler().hasRegionPermLeader(src, "addleader", r)) {
            final Player pVictim = RedProtect.get().getServer().getPlayer(sVictim);

            if ((pVictim == null || !pVictim.isOnline()) && !src.hasPermission("redprotect.command.admin.addleader")) {
                RedProtect.get().getLanguageManager().sendMessage(src, RedProtect.get().getLanguageManager().get("cmdmanager.noplayer.online").replace("{player}", sVictim));
                return;
            }

            if (!src.hasPermission("redprotect.command.admin.addleader")) {
                int claimLimit = RedProtect.get().getPermissionHandler().getPlayerClaimLimit(pVictim);
                assert pVictim != null;
                int claimused = RedProtect.get().getRegionManager().getPlayerRegions(pVictim.getUniqueId().toString(), pVictim.getWorld().getName());
                boolean claimUnlimited = RedProtect.get().getPermissionHandler().hasPerm(src, "redprotect.limits.claim.unlimited");
                if (claimused >= claimLimit && claimLimit >= 0 && !claimUnlimited) {
                    RedProtect.get().getLanguageManager().sendMessage(src, RedProtect.get().getLanguageManager().get("cmdmanager.region.addleader.limit").replace("{player}", pVictim.getName()));
                    return;
                }

                int pLimit = RedProtect.get().getPermissionHandler().getPlayerBlockLimit(pVictim);
                boolean areaUnlimited = RedProtect.get().getPermissionHandler().hasPerm(pVictim, "redprotect.limits.blocks.unlimited");
                int totalArea = RedProtect.get().getRegionManager().getTotalRegionSize(pVictim.getName(), pVictim.getWorld().getName());
                int regionArea = RedProtect.get().getUtil().simuleTotalRegionSize(pVictim.getUniqueId().toString(), r);
                int actualArea = 0;
                if (regionArea > 0) {
                    actualArea = totalArea + regionArea;
                }
                if (pLimit >= 0 && actualArea > pLimit && !areaUnlimited) {
                    RedProtect.get().getLanguageManager().sendMessage(src, RedProtect.get().getLanguageManager().get("cmdmanager.region.addleader.blocklimit").replace("{player}", pVictim.getName()));
                    return;
                }
            }

            if (!r.isLeader(sVictim)) {
                if (src.hasPermission("redprotect.command.admin.addleader")) {
                    r.addLeader(sVictim);
                    RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + src.getName() + " ADDED LEADER " + sVictim + " to region " + r.getName());
                    RedProtect.get().getLanguageManager().sendMessage(src, RedProtect.get().getLanguageManager().get("general.color") + sVictim + " " + RedProtect.get().getLanguageManager().get("cmdmanager.region.leader.added") + " " + r.getName());
                    if (pVictim != null && pVictim.isOnline() && !pVictim.equals(src)) {
                        RedProtect.get().getLanguageManager().sendMessage(pVictim, RedProtect.get().getLanguageManager().get("cmdmanager.region.leader.youadded").replace("{region}", r.getName()) + " " + src.getName());
                    }
                    return;
                }

                RedProtect.get().getLanguageManager().sendMessage(src, RedProtect.get().getLanguageManager().get("cmdmanager.region.leader.yousendrequest").replace("{player}", pVictim.getName()));
                RedProtect.get().getLanguageManager().sendMessage(pVictim, RedProtect.get().getLanguageManager().get("cmdmanager.region.leader.sendrequestto").replace("{region}", r.getName()).replace("{player}", src.getName()));

                RedProtect.get().alWait.put(pVictim, r.getID() + "@" + src.getName());
                Bukkit.getScheduler().scheduleSyncDelayedTask(RedProtect.get(), () -> {
                    if (RedProtect.get().alWait.containsKey(pVictim)) {
                        RedProtect.get().alWait.remove(pVictim);
                        if (src instanceof Player && ((Player) src).isOnline()) {
                            RedProtect.get().getLanguageManager().sendMessage(src, RedProtect.get().getLanguageManager().get("cmdmanager.region.leader.requestexpired").replace("{player}", pVictim.getName()));
                        }
                    }
                }, RedProtect.get().getConfigManager().configRoot().region_settings.leadership_request_time * 20L);
            } else {
                RedProtect.get().getLanguageManager().sendMessage(src, ChatColor.RED + sVictim + " " + RedProtect.get().getLanguageManager().get("cmdmanager.region.leader.already"));
            }
        } else if (src instanceof Player) {
            RedProtect.get().getLanguageManager().sendMessage(src, "no.permission");
        }
    }

    public static void handleRemoveLeader(CommandSender src, String sVictim, Region r) {
        Region rLow = null;
        Map<Integer, Region> regions = new HashMap<>();
        if (src instanceof Player p) {
            r = RedProtect.get().getRegionManager().getTopRegion(p.getLocation());
            rLow = RedProtect.get().getRegionManager().getLowRegion(p.getLocation());
            regions = RedProtect.get().getRegionManager().getGroupRegion(p.getLocation());
            if (r == null) {
                RedProtect.get().getLanguageManager().sendMessage(p, "cmdmanager.region.todo.that");
                return;
            }
        }

        if (RedProtect.get().getPermissionHandler().hasRegionPermLeader(src, "removeleader", r)) {
            Player pVictim = RedProtect.get().getServer().getPlayer(sVictim);

            if (RedProtect.get().getUtil().PlayerToUUID(sVictim) == null) {
                RedProtect.get().getLanguageManager().sendMessage(src, RedProtect.get().getLanguageManager().get("cmdmanager.noplayer.thisname").replace("{player}", sVictim));
                return;
            }

            if (rLow != null && rLow != r && ((!RedProtect.get().getPermissionHandler().hasRegionPermLeader(src, "removeleader", rLow) || (regions.size() > 1 && rLow.isLeader(sVictim))))) {
                RedProtect.get().getLanguageManager().sendMessage(src, RedProtect.get().getLanguageManager().get("cmdmanager.region.leader.cantremove.lowregion").replace("{player}", sVictim) + " " + rLow.getName());
                return;
            }

            if (r.isLeader(sVictim)) {
                if (r.leaderSize() > 1) {
                    RedProtect.get().getLanguageManager().sendMessage(src, RedProtect.get().getLanguageManager().get("general.color") + sVictim + " " + RedProtect.get().getLanguageManager().get("cmdmanager.region.admin.added") + " " + r.getName());
                    r.removeLeader(sVictim);
                    RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + src.getName() + " DEMOTED TO ADMIN " + sVictim + " to region " + r.getName());
                    if (pVictim != null && pVictim.isOnline() && !pVictim.equals(src)) {
                        RedProtect.get().getLanguageManager().sendMessage(pVictim, RedProtect.get().getLanguageManager().get("cmdmanager.region.leader.youdemoted").replace("{region}", r.getName()) + " " + src.getName());
                    }
                } else {
                    RedProtect.get().getLanguageManager().sendMessage(src, RedProtect.get().getLanguageManager().get("cmdmanager.region.leader.cantremove").replace("{player}", sVictim));
                }
            } else {
                RedProtect.get().getLanguageManager().sendMessage(src, ChatColor.RED + sVictim + " " + RedProtect.get().getLanguageManager().get("cmdmanager.region.leader.notleader"));
            }
        } else if (src instanceof Player) {
            RedProtect.get().getLanguageManager().sendMessage(src, "no.permission");
        }
    }

    public static void handleAddAdmin(CommandSender src, String sVictim, Region r) {
        if (src instanceof Player p) {
            r = RedProtect.get().getRegionManager().getTopRegion(p.getLocation());
            if (r == null) {
                RedProtect.get().getLanguageManager().sendMessage(p, "cmdmanager.region.todo.that");
                return;
            }
        }

        if (RedProtect.get().getPermissionHandler().hasRegionPermAdmin(src, "addadmin", r)) {
            Player pVictim = RedProtect.get().getServer().getPlayer(sVictim);

            if (RedProtect.get().getUtil().PlayerToUUID(sVictim) == null) {
                RedProtect.get().getLanguageManager().sendMessage(src, RedProtect.get().getLanguageManager().get("cmdmanager.noplayer.thisname").replace("{player}", sVictim));
                return;
            }

            if (r.isLeader(sVictim)) {
                RedProtect.get().getLanguageManager().sendMessage(src, ChatColor.RED + sVictim + " " + RedProtect.get().getLanguageManager().get("cmdmanager.region.leader.already"));
                return;
            }

            if (!r.isAdmin(sVictim)) {
                r.addAdmin(sVictim);
                RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + src.getName() + " ADDED ADMIN " + sVictim + " to region " + r.getName());
                RedProtect.get().getLanguageManager().sendMessage(src, RedProtect.get().getLanguageManager().get("general.color") + sVictim + " " + RedProtect.get().getLanguageManager().get("cmdmanager.region.admin.added") + " " + r.getName());
                if (pVictim != null && pVictim.isOnline() && !pVictim.equals(src)) {
                    RedProtect.get().getLanguageManager().sendMessage(pVictim, RedProtect.get().getLanguageManager().get("cmdmanager.region.admin.youadded").replace("{region}", r.getName()) + " " + src.getName());
                }
            } else {
                RedProtect.get().getLanguageManager().sendMessage(src, ChatColor.RED + sVictim + " " + RedProtect.get().getLanguageManager().get("cmdmanager.region.admin.already"));
            }
        } else if (src instanceof Player) {
            RedProtect.get().getLanguageManager().sendMessage(src, "no.permission");
        }
    }

    public static void handleRemoveAdmin(CommandSender src, String sVictim, Region r) {
        if (src instanceof Player p) {
            r = RedProtect.get().getRegionManager().getTopRegion(p.getLocation());
            if (r == null) {
                RedProtect.get().getLanguageManager().sendMessage(p, "cmdmanager.region.todo.that");
                return;
            }
        }

        if (RedProtect.get().getPermissionHandler().hasRegionPermAdmin(src, "removeadmin", r)) {
            Player pVictim = RedProtect.get().getServer().getPlayer(sVictim);

            if (RedProtect.get().getUtil().PlayerToUUID(sVictim) == null) {
                RedProtect.get().getLanguageManager().sendMessage(src, RedProtect.get().getLanguageManager().get("cmdmanager.noplayer.thisname").replace("{player}", sVictim));
                return;
            }

            if (r.isAdmin(sVictim)) {
                RedProtect.get().getLanguageManager().sendMessage(src, RedProtect.get().getLanguageManager().get("general.color") + sVictim + " " + RedProtect.get().getLanguageManager().get("cmdmanager.region.member.added") + " " + r.getName());
                r.removeAdmin(sVictim);
                RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + src.getName() + " DEMOTED TO MEMBER " + sVictim + " to region " + r.getName());
                if (pVictim != null && pVictim.isOnline() && !pVictim.equals(src)) {
                    RedProtect.get().getLanguageManager().sendMessage(pVictim, RedProtect.get().getLanguageManager().get("cmdmanager.region.admin.youdemoted").replace("{region}", r.getName()) + " " + src.getName());
                }
            } else {
                RedProtect.get().getLanguageManager().sendMessage(src, ChatColor.RED + sVictim + " " + RedProtect.get().getLanguageManager().get("cmdmanager.region.admin.notadmin"));
            }
        } else if (src instanceof Player) {
            RedProtect.get().getLanguageManager().sendMessage(src, "no.permission");
        }
    }

    public static void handleAddMember(CommandSender src, String sVictim, Region r) {
        if (src instanceof Player p) {
            r = RedProtect.get().getRegionManager().getTopRegion(p.getLocation());
            if (r == null) {
                RedProtect.get().getLanguageManager().sendMessage(p, "cmdmanager.region.todo.that");
                return;
            }
        }

        if (RedProtect.get().getPermissionHandler().hasRegionPermAdmin(src, "addmember", r)) {
            if (RedProtect.get().getUtil().PlayerToUUID(sVictim) == null) {
                RedProtect.get().getLanguageManager().sendMessage(src, RedProtect.get().getLanguageManager().get("cmdmanager.noplayer.thisname").replace("{player}", sVictim));
                return;
            }

            Player pVictim = RedProtect.get().getServer().getPlayer(sVictim);

            if (r.isLeader(sVictim)) {
                RedProtect.get().getLanguageManager().sendMessage(src, ChatColor.RED + sVictim + " " + RedProtect.get().getLanguageManager().get("cmdmanager.region.leader.already"));
                return;
            }

            if (r.isAdmin(sVictim)) {
                r.addMember(sVictim);
                RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + src.getName() + " ADDED MEMBER " + sVictim + " to region " + r.getName());
                RedProtect.get().getLanguageManager().sendMessage(src, RedProtect.get().getLanguageManager().get("general.color") + sVictim + " " + RedProtect.get().getLanguageManager().get("cmdmanager.region.member.demoted") + " " + r.getName());
                if (pVictim != null && pVictim.isOnline()) {
                    RedProtect.get().getLanguageManager().sendMessage(pVictim, RedProtect.get().getLanguageManager().get("cmdmanager.region.admin.youdemoted").replace("{region}", r.getName()) + " " + src.getName());
                }
            } else if (!r.isMember(sVictim)) {
                r.addMember(sVictim);
                RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + src.getName() + " ADDED MEMBER " + sVictim + " to region " + r.getName());
                RedProtect.get().getLanguageManager().sendMessage(src, RedProtect.get().getLanguageManager().get("general.color") + sVictim + " " + RedProtect.get().getLanguageManager().get("cmdmanager.region.member.added") + " " + r.getName());
                if (pVictim != null && pVictim.isOnline() && !pVictim.equals(src)) {
                    RedProtect.get().getLanguageManager().sendMessage(pVictim, RedProtect.get().getLanguageManager().get("cmdmanager.region.member.youadded").replace("{region}", r.getName()) + " " + src.getName());
                }
            } else {
                RedProtect.get().getLanguageManager().sendMessage(src, ChatColor.RED + sVictim + " " + RedProtect.get().getLanguageManager().get("cmdmanager.region.member.already"));
            }
        } else if (src instanceof Player) {
            RedProtect.get().getLanguageManager().sendMessage(src, "no.permission");
        }
    }

    public static void handleRemoveMember(CommandSender src, String sVictim, Region r) {
        if (src instanceof Player p) {
            r = RedProtect.get().getRegionManager().getTopRegion(p.getLocation());
            if (r == null) {
                RedProtect.get().getLanguageManager().sendMessage(p, "cmdmanager.region.todo.that");
                return;
            }
        }

        if (RedProtect.get().getPermissionHandler().hasRegionPermAdmin(src, "removemember", r)) {

            Player pVictim = RedProtect.get().getServer().getPlayer(sVictim);

            if (RedProtect.get().getUtil().PlayerToUUID(sVictim) == null) {
                RedProtect.get().getLanguageManager().sendMessage(src, RedProtect.get().getLanguageManager().get("cmdmanager.noplayer.thisname").replace("{player}", sVictim));
                return;
            }

            if ((r.isMember(sVictim) || r.isAdmin(sVictim)) && !r.isLeader(sVictim)) {
                RedProtect.get().getLanguageManager().sendMessage(src, RedProtect.get().getLanguageManager().get("general.color") + sVictim + " " + RedProtect.get().getLanguageManager().get("cmdmanager.region.member.removed") + " " + r.getName());
                r.removeMember(sVictim);
                RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + src.getName() + " REMOVED MEMBER " + sVictim + " to region " + r.getName());
                if (pVictim != null && pVictim.isOnline() && !pVictim.equals(src)) {
                    RedProtect.get().getLanguageManager().sendMessage(pVictim, RedProtect.get().getLanguageManager().get("cmdmanager.region.member.youremoved").replace("{region}", r.getName()) + " " + src.getName());
                }
            } else {
                RedProtect.get().getLanguageManager().sendMessage(src, ChatColor.RED + sVictim + " " + RedProtect.get().getLanguageManager().get("cmdmanager.region.member.notmember"));
            }
        } else if (src instanceof Player) {
            RedProtect.get().getLanguageManager().sendMessage(src, "no.permission");
        }
    }

    public static void handleDelete(Player p) {
        Region r = RedProtect.get().getRegionManager().getTopRegion(p.getLocation());
        if (RedProtect.get().getPermissionHandler().hasRegionPermLeader(p, "delete", r)) {
            if (r == null) {
                RedProtect.get().getLanguageManager().sendMessage(p, "cmdmanager.region.todo.that");
                return;
            }

            int claims = RedProtect.get().getConfigManager().configRoot().region_settings.can_delete_first_home_after_claims;
            if (!r.canDelete() && (claims == -1 || RedProtect.get().getRegionManager().getPlayerRegions(p.getUniqueId().toString(), p.getWorld().getName()) < claims) && !p.hasPermission("redprotect.bypass")) {
                if (claims != -1) {
                    RedProtect.get().getLanguageManager().sendMessage(p, RedProtect.get().getLanguageManager().get("cmdmanager.region.cantdeletefirst-claims").replace("{claims}", "" + claims));
                } else {
                    RedProtect.get().getLanguageManager().sendMessage(p, RedProtect.get().getLanguageManager().get("cmdmanager.region.cantdeletefirst"));
                }
                return;
            }

            DeleteRegionEvent event = new DeleteRegionEvent(r, p);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }

            String rname = r.getName();
            String w = r.getWorld();
            RedProtect.get().getRegionManager().remove(r, w);
            RedProtect.get().getLanguageManager().sendMessage(p, RedProtect.get().getLanguageManager().get("cmdmanager.region.deleted") + " " + rname);
            RedProtect.get().logger.addLog("(World " + w + ") Player " + p.getName() + " REMOVED region " + rname);

            // Handle money
            handleDeleteRegionEconomy(r, p);
        } else {
            RedProtect.get().getLanguageManager().sendMessage(p, "no.permission");
        }
    }

    public static void handleDeleteName(Player p, String rname, String world) {
        Region r = RedProtect.get().getRegionManager().getRegion(rname, p.getWorld().getName());
        if (!world.equals("")) {
            if (Bukkit.getWorld(world) != null) {
                r = RedProtect.get().getRegionManager().getRegion(rname, world);
            } else {
                RedProtect.get().getLanguageManager().sendMessage(p, "cmdmanager.region.invalidworld");
                return;
            }
        }

        if (RedProtect.get().getPermissionHandler().hasRegionPermLeader(p, "delete", r)) {
            if (r == null) {
                RedProtect.get().getLanguageManager().sendMessage(p, RedProtect.get().getLanguageManager().get("cmdmanager.region.doesntexist") + ": " + rname);
                return;
            }

            int claims = RedProtect.get().getConfigManager().configRoot().region_settings.can_delete_first_home_after_claims;
            if (!r.canDelete() && (claims == -1 || RedProtect.get().getRegionManager().getPlayerRegions(p.getUniqueId().toString(), p.getWorld().getName()) < claims) && !p.hasPermission("redprotect.bypass")) {
                if (claims != -1) {
                    RedProtect.get().getLanguageManager().sendMessage(p, RedProtect.get().getLanguageManager().get("cmdmanager.region.cantdeletefirst-claims").replace("{claims}", "" + claims));
                } else {
                    RedProtect.get().getLanguageManager().sendMessage(p, RedProtect.get().getLanguageManager().get("cmdmanager.region.cantdeletefirst"));
                }
                return;
            }

            DeleteRegionEvent event = new DeleteRegionEvent(r, p);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }

            RedProtect.get().getRegionManager().remove(r, r.getWorld());
            RedProtect.get().getLanguageManager().sendMessage(p, RedProtect.get().getLanguageManager().get("cmdmanager.region.deleted") + " " + rname);
            RedProtect.get().logger.addLog("(World " + world + ") Player " + p.getName() + " REMOVED region " + rname);

            // Handle money
            handleDeleteRegionEconomy(r, p);
        } else {
            RedProtect.get().getLanguageManager().sendMessage(p, "no.permission");
        }
    }

    private static void handleDeleteRegionEconomy(Region region, Player player) {
        // Handle money
        if (RedProtect.get().getConfigManager().ecoRoot().claim_cost_per_block.enable && RedProtect.get().hooks.checkVault() && !player.hasPermission("redprotect.eco.bypass")) {
            long reco = (long) region.getArea() * RedProtect.get().getConfigManager().ecoRoot().claim_cost_per_block.cost_per_block;

            if (!RedProtect.get().getConfigManager().ecoRoot().claim_cost_per_block.y_is_free) {
                reco = reco * Math.abs(region.getMaxY() - region.getMinY());
            }

            if (reco > 0) {
                RedProtect.get().economy.depositPlayer(player, reco);
                player.sendMessage(RedProtect.get().getLanguageManager().get("economy.region.deleted").replace("{price}", RedProtect.get().getConfigManager().ecoRoot().economy_symbol + reco + " " + RedProtect.get().getConfigManager().ecoRoot().economy_name));
            }
        }
    }

    public static void handleRename(Player p, String newName) {
        Region r = RedProtect.get().getRegionManager().getTopRegion(p.getLocation());
        if (RedProtect.get().getPermissionHandler().hasRegionPermLeader(p, "rename", r)) {
            if (r == null) {
                RedProtect.get().getLanguageManager().sendMessage(p, "cmdmanager.region.todo.that");
                return;
            }

            //filter name
            newName = RedProtect.get().getUtil().fixRegionName(p, newName);
            if (newName == null) return;

            RenameRegionEvent event = new RenameRegionEvent(r, newName, r.getName(), p);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }

            String oldname = event.getOldName();
            newName = event.getNewName();

            Region newRegion = RedProtect.get().getRegionManager().renameRegion(newName, r);
            RedProtect.get().getLanguageManager().sendMessage(p, RedProtect.get().getLanguageManager().get("cmdmanager.region.rename.newname") + " " + newRegion.getName());
            RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + p.getName() + " RENAMED region " + oldname + " to " + newRegion.getName());
        } else {
            RedProtect.get().getLanguageManager().sendMessage(p, "no.permission");
        }
    }

    // TODO Other Handlers
    public static void handlePrioritySingle(Player p, int prior, String region) {
        Region r = RedProtect.get().getRegionManager().getRegion(region, p.getWorld().getName());
        if (RedProtect.get().getPermissionHandler().hasRegionPermLeader(p, "priority", r)) {
            if (r != null) {
                r.setPrior(prior);
                RedProtect.get().getLanguageManager().sendMessage(p, RedProtect.get().getLanguageManager().get("cmdmanager.region.priority.set").replace("{region}", r.getName()) + " " + prior);
                RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + p.getName() + " SET PRIORITY of region " + r.getName() + " to " + prior);
            } else {
                RedProtect.get().getLanguageManager().sendMessage(p, "cmdmanager.region.todo.that");
            }
        }
    }

    public static void handlePriority(Player p, int prior) {
        Region r = RedProtect.get().getRegionManager().getTopRegion(p.getLocation());
        if (RedProtect.get().getPermissionHandler().hasRegionPermLeader(p, "priority", r)) {
            if (r != null) {
                r.setPrior(prior);
                RedProtect.get().getLanguageManager().sendMessage(p, RedProtect.get().getLanguageManager().get("cmdmanager.region.priority.set").replace("{region}", r.getName()) + " " + prior);
                RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + p.getName() + " SET PRIORITY of region " + r.getName() + " to " + prior);
            } else {
                RedProtect.get().getLanguageManager().sendMessage(p, "cmdmanager.region.todo.that");
            }
        }
    }

    public static void handleInfoTop(Player p) {
        Region r = RedProtect.get().getRegionManager().getTopRegion(p.getLocation());
        if (r == null) {
            RedProtect.get().getLanguageManager().sendMessage(p, "cmdmanager.region.doesntexist");
            return;
        }
        Map<Integer, Region> groupr = RedProtect.get().getRegionManager().getGroupRegion(p.getLocation());
        if (RedProtect.get().getPermissionHandler().hasRegionPermAdmin(p, "info", r)) {
            p.sendMessage(RedProtect.get().getLanguageManager().get("general.color") + "--------------- [" + ChatColor.GOLD + r.getName() + RedProtect.get().getLanguageManager().get("general.color") + "] ---------------");
            p.sendMessage(r.info());
            p.sendMessage(RedProtect.get().getLanguageManager().get("general.color") + "----------------------------------");
            if (groupr.size() > 1) {
                p.sendMessage(RedProtect.get().getLanguageManager().get("cmdmanager.moreregions"));
                for (Region regs : groupr.values()) {
                    if (regs != r) {
                        p.sendMessage(RedProtect.get().getLanguageManager().get("region.name") + " " + regs.getName() + " " + RedProtect.get().getLanguageManager().get("region.priority") + " " + regs.getPrior());
                    }
                }
            }
        } else {
            RedProtect.get().getLanguageManager().sendMessage(p, "no.permission");
        }
    }

    public static void handleInfo(Player p, String region, String world) {
        Region r = RedProtect.get().getRegionManager().getRegion(region, p.getWorld().getName());
        if (!world.equals("")) {
            if (Bukkit.getWorld(world) != null) {
                r = RedProtect.get().getRegionManager().getRegion(region, world);
            } else {
                RedProtect.get().getLanguageManager().sendMessage(p, "cmdmanager.region.invalidworld");
                return;
            }
        }
        if (RedProtect.get().getPermissionHandler().hasRegionPermAdmin(p, "info", r)) {
            if (r == null) {
                RedProtect.get().getLanguageManager().sendMessage(p, "cmdmanager.region.todo.that");
                return;
            }
            p.sendMessage(RedProtect.get().getLanguageManager().get("general.color") + "--------------- [" + ChatColor.GOLD + r.getName() + RedProtect.get().getLanguageManager().get("general.color") + "] ---------------");
            p.sendMessage(r.info());
            p.sendMessage(RedProtect.get().getLanguageManager().get("general.color") + "----------------------------------");
        } else {
            RedProtect.get().getLanguageManager().sendMessage(p, "no.permission");
        }
    }

    public static void handletp(CommandSender sender, String rname, String wname, Player play) {
        World w = RedProtect.get().getServer().getWorld(wname);
        if (w == null) {
            RedProtect.get().getLanguageManager().sendMessage(sender, "cmdmanager.region.invalidworld");
            return;
        }
        Region region = RedProtect.get().getRegionManager().getRegion(rname, w.getName());
        if (region == null) {
            RedProtect.get().getLanguageManager().sendMessage(sender, RedProtect.get().getLanguageManager().get("cmdmanager.region.doesntexist") + ": " + rname);
            return;
        }

        if (play == null) {
            if (sender instanceof Player && !RedProtect.get().getPermissionHandler().hasRegionPermMember((Player) sender, "teleport", region)) {
                RedProtect.get().getLanguageManager().sendMessage(sender, "no.permission");
                return;
            }
        } else {
            if (!RedProtect.get().getPermissionHandler().hasPerm(sender, "redprotect.command.admin.teleport")) {
                RedProtect.get().getLanguageManager().sendMessage(sender, "no.permission");
                return;
            }
        }

        Location loc = null;
        if (region.getTPPoint() != null) {
            loc = region.getTPPoint();
            loc.setX(loc.getBlockX() + 0.500);
            loc.setZ(loc.getBlockZ() + 0.500);
        } else {
            int limit = w.getMaxHeight();
            if (w.getEnvironment().equals(World.Environment.NETHER)) {
                limit = 124;
            }
            for (int i = limit; i > 0; i--) {
                Material mat = w.getBlockAt(region.getCenterX(), i, region.getCenterZ()).getType();
                Material mat1 = w.getBlockAt(region.getCenterX(), i + 1, region.getCenterZ()).getType();
                Material mat2 = w.getBlockAt(region.getCenterX(), i + 2, region.getCenterZ()).getType();
                if (!mat.name().contains("LAVA") && !mat.equals(Material.AIR) && mat1.equals(Material.AIR) && mat2.equals(Material.AIR)) {
                    loc = new Location(w, region.getCenterX() + 0.500, i + 1, region.getCenterZ() + 0.500);
                    break;
                }
            }
        }

        if (loc != null) {
            if (play != null) {
                if (RedProtect.get().hooks.checkEss() && sender instanceof Player) {
                    RedProtect.get().hooks.pless.getUser(sender).setLastLocation();
                }
                play.teleport(loc);
                RedProtect.get().getLanguageManager().sendMessage(play, RedProtect.get().getLanguageManager().get("cmdmanager.region.teleport") + " " + rname);
                RedProtect.get().getLanguageManager().sendMessage(sender, RedProtect.get().getLanguageManager().get("cmdmanager.region.tpother") + " " + rname);
            } else if (sender instanceof Player) {
                tpWait((Player) sender, loc, rname);
            }
        }
    }

    private static void tpWait(final Player p, final Location loc, final String rname) {
        if (p.hasPermission("redprotect.command.admin.teleport")) {
            p.teleport(loc);
            return;
        }

        int delay = RedProtect.get().getConfigManager().configRoot().region_settings.teleport_time;

        if (delay < 1) {
            p.teleport(loc);
            RedProtect.get().getLanguageManager().sendMessage(p, RedProtect.get().getLanguageManager().get("cmdmanager.region.teleport") + " " + rname);
            return;
        }

        if (!RedProtect.get().tpWait.contains(p.getName())) {
            RedProtect.get().tpWait.add(p.getName());
            RedProtect.get().getLanguageManager().sendMessage(p, "cmdmanager.region.tpdontmove");
            Bukkit.getScheduler().scheduleSyncDelayedTask(RedProtect.get(), () -> {
                if (RedProtect.get().tpWait.contains(p.getName())) {
                    RedProtect.get().tpWait.remove(p.getName());
                    if (RedProtect.get().hooks.checkEss()) {
                        RedProtect.get().hooks.pless.getUser(p).setLastLocation();
                    }
                    p.teleport(loc);
                    RedProtect.get().getLanguageManager().sendMessage(p, RedProtect.get().getLanguageManager().get("cmdmanager.region.teleport") + " " + rname);
                }
            }, delay * 20L);
        } else {
            RedProtect.get().getLanguageManager().sendMessage(p, "cmdmanager.region.tpneedwait");
        }
    }

    public static void handleWelcome(Player p, String wMessage) {
        Region r = RedProtect.get().getRegionManager().getTopRegion(p.getLocation());
        if (RedProtect.get().getPermissionHandler().hasRegionPermAdmin(p, "welcome", r)) {
            if (r != null) {
                switch (wMessage) {
                    case "" -> RedProtect.get().getLanguageManager().sendMessage(p, "cmdmanager.region.welcomeoff");
                    case "hide " ->
                            RedProtect.get().getLanguageManager().sendMessage(p, "cmdmanager.region.welcomehide");
                    default ->
                            RedProtect.get().getLanguageManager().sendMessage(p, RedProtect.get().getLanguageManager().get("cmdmanager.region.welcomeset") + " " + ChatColor.translateAlternateColorCodes('&', wMessage));
                }
                r.setWelcome(wMessage);
                RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + p.getName() + " SET WELCOME of region " + r.getName() + " to " + wMessage);
            } else {
                RedProtect.get().getLanguageManager().sendMessage(p, "cmdmanager.region.todo.that");
            }
            return;
        }
        RedProtect.get().getLanguageManager().sendMessage(p, "no.permission");
    }

    public static void handleList(Player p, String uuid, int Page) {
        if (RedProtect.get().getPermissionHandler().hasPerm(p, "redprotect.command.admin.list")) {
            getRegionforList(p, uuid, Page);
            return;
        } else if (RedProtect.get().getPermissionHandler().hasPerm(p, "redprotect.command.list") && RedProtect.get().getUtil().PlayerToUUID(p.getName()).equalsIgnoreCase(uuid)) {
            getRegionforList(p, uuid, Page);
            return;
        }
        RedProtect.get().getLanguageManager().sendMessage(p, "no.permission");
    }

    public static void getRegionforList(CommandSender sender, String uuid, int nPage) {
        Bukkit.getScheduler().runTaskAsynchronously(RedProtect.get(), () -> {
            int Page = nPage;
            Set<Region> regions = RedProtect.get().getRegionManager().getLeaderRegions(uuid);
            int length = regions.size();
            if (length == 0) {
                RedProtect.get().getLanguageManager().sendMessage(sender, "cmdmanager.player.noregions");
            } else {
                sender.sendMessage(RedProtect.get().getLanguageManager().get("general.color") + "-------------------------------------------------");
                RedProtect.get().getLanguageManager().sendMessage(sender, RedProtect.get().getLanguageManager().get("cmdmanager.region.created.list") + " " + RedProtect.get().getUtil().UUIDtoPlayer(uuid));

                int regionsPage = RedProtect.get().getConfigManager().configRoot().region_settings.region_list.region_per_page;
                int total = 0;
                int last = 0;

                UltimateFancy fancy = new UltimateFancy(RedProtect.get());
                for (World w : Bukkit.getWorlds()) {
                    boolean first = true;

                    if (Page == 0) {
                        Page = 1;
                    }
                    int max = (regionsPage * Page);
                    int min = max - regionsPage;
                    int count;

                    String colorChar = ChatColor.translateAlternateColorCodes('&', RedProtect.get().getConfigManager().configRoot().region_settings.world_colors.get(w.getName()));
                    Set<Region> wregions = RedProtect.get().getRegionManager().getRegions(uuid, w.getName());
                    int totalLocal = wregions.size();
                    total += totalLocal;

                    int lastLocal = 0;

                    if (wregions.size() > 0) {
                        List<Region> it = new ArrayList<>(wregions);
                        if (min > totalLocal) {
                            int diff = (totalLocal / regionsPage);
                            min = regionsPage * diff;
                            max = (regionsPage * diff) + regionsPage;
                        }
                        if (max >= it.size()) max = (it.size() - 1);
                        //-------------
                        UltimateFancy tempFancy = new UltimateFancy(RedProtect.get());
                        if (RedProtect.get().getConfigManager().configRoot().region_settings.region_list.hover_and_click_teleport && RedProtect.get().getPermissionHandler().hasRegionPermAdmin(sender, "teleport", null)) {
                            for (int i = min; i <= max; i++) {
                                count = i;
                                Region r = it.get(i);
                                String area = RedProtect.get().getConfigManager().configRoot().region_settings.region_list.shpw_area ? "(" + RedProtect.get().getUtil().simuleTotalRegionSize(RedProtect.get().getUtil().PlayerToUUID(uuid), r) + ")" : "";
                                String rname = RedProtect.get().getLanguageManager().get("general.color") + ", " + ChatColor.GRAY + r.getName() + area;
                                if (first) {
                                    rname = rname.substring(3);
                                    first = false;
                                }
                                if (count == max) {
                                    rname = rname + RedProtect.get().getLanguageManager().get("general.color") + ".";
                                }
                                tempFancy.text(rname)
                                        .hoverShowText(RedProtect.get().getLanguageManager().get("cmdmanager.list.hover").replace("{region}", r.getName()))
                                        .clickRunCmd("/rp " + getCmd("teleport") + " " + r.getName() + " " + r.getWorld())
                                        .next();
                                lastLocal = count;
                            }
                        } else {
                            for (int i = min; i <= max; i++) {
                                count = i;
                                Region r = it.get(i);
                                String area = RedProtect.get().getConfigManager().configRoot().region_settings.region_list.shpw_area ? "(" + RedProtect.get().getUtil().simuleTotalRegionSize(RedProtect.get().getUtil().PlayerToUUID(uuid), r) + ")" : "";
                                String rname = RedProtect.get().getLanguageManager().get("general.color") + ", " + ChatColor.GRAY + r.getName() + area;
                                if (first) {
                                    rname = rname.substring(3);
                                    first = false;
                                }
                                if (count == max) {
                                    rname = rname + RedProtect.get().getLanguageManager().get("general.color") + ".";
                                }
                                tempFancy.textAndNext(rname);
                                lastLocal = count;
                            }
                        }
                        last += lastLocal + 1;
                        fancy.textAndNext("\n" + RedProtect.get().getLanguageManager().get("general.color") + "-----");
                        fancy.textAndNext("\n" + RedProtect.get().getLanguageManager().get("general.color") + RedProtect.get().getLanguageManager().get("region.world").replace(":", "") + " " + colorChar + w.getName() + "[" + (min + 1) + "-" + (max + 1) + "/" + wregions.size() + "]" + ChatColor.RESET + ":");
                        fancy.appendFancy(tempFancy);
                        fancy.next();
                    }
                }
                fancy.textAndNext("\n" + RedProtect.get().getLanguageManager().get("general.color") + "---------------- " + last + "/" + total + " -----------------");
                if (last < total) {
                    fancy.text("\n" + RedProtect.get().getLanguageManager().get("cmdmanager.region.listpage.more").replace("{player}", RedProtect.get().getUtil().UUIDtoPlayer(uuid) + " " + (Page + 1)))
                            .clickRunCmd("/rp list " + RedProtect.get().getUtil().UUIDtoPlayer(uuid) + " " + (Page + 1))
                            .hoverShowText(RedProtect.get().getLanguageManager().get("general.color") + "/rp list " + RedProtect.get().getUtil().UUIDtoPlayer(uuid) + " " + (Page + 1));
                } else {
                    if (Page != 1) {
                        fancy.textAndNext("\n" + RedProtect.get().getLanguageManager().get("cmdmanager.region.listpage.nomore"));
                    }
                }
                fancy.send(sender);
            }
        });
    }

    public static void handleFlag(Player p, String flag, String value, Region r) {
        if (checkCmd(flag, "help")) {
            sendFlagHelp(p);
            return;
        }

        if (r == null) {
            RedProtect.get().getLanguageManager().sendMessage(p, "cmdmanager.region.todo.that");
            return;
        }

        Object objflag = RedProtect.get().getUtil().parseObject(value);

        if ((RedProtect.get().getPermissionHandler().hasFlagPerm(p, flag) && (RedProtect.get().getConfigManager().configRoot().flags.containsKey(flag) || CoreConfigManager.ADMIN_FLAGS.contains(flag))) || flag.equalsIgnoreCase("info")) {
            if (r.isAdmin(p) || r.isLeader(p) || RedProtect.get().getPermissionHandler().hasPerm(p, "redprotect.command.admin.flag")) {
                if (checkCmd(flag, "info")) {
                    p.sendMessage(RedProtect.get().getLanguageManager().get("general.color") + "------------[" + RedProtect.get().getLanguageManager().get("cmdmanager.region.flag.values") + "]------------");
                    p.sendMessage(r.getFlagInfo());
                    p.sendMessage(RedProtect.get().getLanguageManager().get("general.color") + "------------------------------------");
                    return;
                }

                if (value.equalsIgnoreCase("remove")) {
                    if (CoreConfigManager.ADMIN_FLAGS.contains(flag) && r.getFlags().containsKey(flag)) {
                        r.removeFlag(flag);
                        RedProtect.get().getLanguageManager().sendMessage(p, RedProtect.get().getLanguageManager().get("cmdmanager.region.flag.removed").replace("{flag}", flag).replace("{region}", r.getName()));
                        RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + p.getName() + " REMOVED FLAG " + flag + " of region " + r.getName());
                    } else {
                        RedProtect.get().getLanguageManager().sendMessage(p, RedProtect.get().getLanguageManager().get("cmdmanager.region.flag.notset").replace("{flag}", flag));
                    }
                    return;
                }

                if (!value.equals("")) {
                    if (RedProtect.get().getConfigManager().getDefFlagsValues().containsKey(flag) && !CoreConfigManager.ADMIN_FLAGS.contains(flag)) {

                        //flag clan
                        if (flag.equalsIgnoreCase("clan")) {
                            if (!RedProtect.get().hooks.checkSC() || !RedProtect.get().getPermissionHandler().hasPerm(p, "redprotect.admin.flag.clan")) {
                                sendFlagHelp(p);
                                return;
                            }
                            if (!RedProtect.get().hooks.clanManager.isClan(value)) {
                                RedProtect.get().getLanguageManager().sendMessage(p, RedProtect.get().getLanguageManager().get("cmdmanager.region.flag.invalidclan").replace("{tag}", value));
                                return;
                            }
                            Clan clan = RedProtect.get().hooks.clanManager.getClan(value);
                            if (!clan.isLeader(p)) {
                                RedProtect.get().getLanguageManager().sendMessage(p, "cmdmanager.region.flag.clancommand");
                                return;
                            }
                            if (r.setFlag(p, flag, value)) {
                                RedProtect.get().getLanguageManager().sendMessage(p, RedProtect.get().getLanguageManager().get("cmdmanager.region.flag.set").replace("{flag}", "'" + flag + "'") + " " + r.getFlagString(flag));
                                RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + p.getName() + " SET FLAG " + flag + " of region " + r.getName() + " to " + r.getFlagString(flag));
                            }
                            return;
                        }

                        if (objflag instanceof Boolean) {
                            if (r.setFlag(p, flag, objflag)) {
                                RedProtect.get().getLanguageManager().sendMessage(p, RedProtect.get().getLanguageManager().get("cmdmanager.region.flag.set").replace("{flag}", "'" + flag + "'") + " " + r.getFlagBool(flag));
                                RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + p.getName() + " SET FLAG " + flag + " of region " + r.getName() + " to " + r.getFlagString(flag));
                            }
                        } else {
                            RedProtect.get().getLanguageManager().sendMessage(p, RedProtect.get().getLanguageManager().get("cmdmanager.region.flag.usage") + " <true/false>");
                        }
                        return;
                    }

                    if (CoreConfigManager.ADMIN_FLAGS.contains(flag)) {
                        if (!validate(flag, objflag)) {
                            SendFlagUsageMessage(p, flag);
                            return;
                        }
                        if (r.setFlag(p, flag, objflag)) {
                            RedProtect.get().getLanguageManager().sendMessage(p, RedProtect.get().getLanguageManager().get("cmdmanager.region.flag.set").replace("{flag}", "'" + flag + "'") + " " + r.getFlagString(flag));
                            RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + p.getName() + " SET FLAG " + flag + " of region " + r.getName() + " to " + r.getFlagString(flag));
                        }
                        return;
                    }

                    sendFlagHelp(p);
                } else {

                    // Flag clan
                    if (flag.equalsIgnoreCase("clan")) {
                        if (RedProtect.get().hooks.checkSC()) {
                            ClanPlayer clan = RedProtect.get().hooks.clanManager.getClanPlayer(p);
                            if (clan == null) {
                                RedProtect.get().getLanguageManager().sendMessage(p, "cmdmanager.region.flag.haveclan");
                                return;
                            }
                            if (!clan.isLeader()) {
                                RedProtect.get().getLanguageManager().sendMessage(p, "cmdmanager.region.flag.clancommand");
                                return;
                            }
                            if (r.getFlagString(flag).equalsIgnoreCase("")) {
                                if (r.setFlag(p, flag, clan.getTag())) {
                                    RedProtect.get().getLanguageManager().sendMessage(p, RedProtect.get().getLanguageManager().get("cmdmanager.region.flag.setclan").replace("{clan}", "'" + clan.getClan().getColorTag() + "'"));
                                }
                            } else {
                                RedProtect.get().getLanguageManager().sendMessage(p, RedProtect.get().getLanguageManager().get("cmdmanager.region.flag.denyclan").replace("{clan}", "'" + r.getFlagString(flag) + "'"));
                                r.setFlag(p, flag, "");
                            }
                            RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + p.getName() + " SET FLAG " + flag + " of region " + r.getName() + " to " + r.getFlagString(flag));
                            return;
                        } else {
                            sendFlagHelp(p);
                            return;
                        }
                    }

                    // Item flags
                    if (RedProtect.get().getPermissionHandler().hasPerm(p, "redprotect.flag.item-gui")) {
                        if (flag.equalsIgnoreCase("deny-enter-items") ||
                                flag.equalsIgnoreCase("allow-enter-items") ||
                                flag.equalsIgnoreCase("allow-place") ||
                                flag.equalsIgnoreCase("allow-break")) {

                            ItemFlagGui itemGui = new ItemFlagGui(p, r, flag);
                            itemGui.open();
                            return;
                        }
                    }


                    // Mob flags
                    if (RedProtect.get().getPermissionHandler().hasPerm(p, "redprotect.flag.spawn-mob-gui")) {
                        if (flag.equalsIgnoreCase("spawn-animals") ||
                                flag.equalsIgnoreCase("spawn-monsters")) {

                            MobFlagGui mobFlagGui = new MobFlagGui(p, r, flag);
                            mobFlagGui.open();
                            return;
                        }
                    }

                    if (RedProtect.get().getConfigManager().getDefFlagsValues().containsKey(flag)) {
                        if (r.setFlag(p, flag, !r.getFlagBool(flag))) {
                            RedProtect.get().getLanguageManager().sendMessage(p, RedProtect.get().getLanguageManager().get("cmdmanager.region.flag.set").replace("{flag}", "'" + flag + "'") + " " + r.getFlagBool(flag));
                            RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + p.getName() + " SET FLAG " + flag + " of region " + r.getName() + " to " + r.getFlagString(flag));
                        }
                    } else {
                        if (CoreConfigManager.ADMIN_FLAGS.contains(flag)) {
                            SendFlagUsageMessage(p, flag);
                        } else {
                            RedProtect.get().getLanguageManager().sendMessage(p, RedProtect.get().getLanguageManager().get("cmdmanager.region.flag.usage") + " <true/false>");
                        }
                        sendFlagHelp(p);
                    }
                }

            } else {
                RedProtect.get().getLanguageManager().sendMessage(p, "cmdmanager.region.flag.nopermregion");
            }
        } else {
            RedProtect.get().getLanguageManager().sendMessage(p, "cmdmanager.region.flag.noperm");
        }
    }

    private static void SendFlagUsageMessage(Player p, String flag) {
        String message;
        if (flag.equalsIgnoreCase("effects") ||
                flag.equalsIgnoreCase("allow-enter-items") ||
                flag.equalsIgnoreCase("deny-exit-items") ||
                flag.equalsIgnoreCase("deny-enter-items") ||
                flag.equalsIgnoreCase("gamemode") ||
                flag.equalsIgnoreCase("allow-cmds") ||
                flag.equalsIgnoreCase("deny-cmds") ||
                flag.equalsIgnoreCase("allow-break") ||
                flag.equalsIgnoreCase("allow-place") ||
                flag.equalsIgnoreCase("set-portal") ||
                flag.equalsIgnoreCase("particles") ||
                flag.equalsIgnoreCase("spawn-animals") ||
                flag.equalsIgnoreCase("spawn-monsters") ||
                flag.equalsIgnoreCase("cmd-onhealth")) {
            message = RedProtect.get().getLanguageManager().get("cmdmanager.region.flag.usage" + flag);
        } else {
            message = RedProtect.get().getLanguageManager().get("cmdmanager.region.flag.usagetruefalse").replace("{flag}", flag);
        }
        p.sendMessage(message.replace("{cmd}", getCmd("flag")));
    }

    private static void sendFlagHelp(Player p) {
        p.sendMessage(RedProtect.get().getLanguageManager().get("general.color") + "-------------[RedProtect Flags]------------");
        p.sendMessage(RedProtect.get().getLanguageManager().get("cmdmanager.region.flag.list") + " " + RedProtect.get().getConfigManager().getDefFlags());
        p.sendMessage(RedProtect.get().getLanguageManager().get("general.color") + "------------------------------------");

        StringBuilder sb = new StringBuilder();
        for (String flag : CoreConfigManager.ADMIN_FLAGS) {
            if (RedProtect.get().getPermissionHandler().hasFlagPerm(p, flag))
                sb.append(flag).append(", ");
        }
        if (sb.length() > 1) {
            p.sendMessage(RedProtect.get().getLanguageManager().get("cmdmanager.region.flag.admlist") + " [" + sb.substring(0, sb.length() - 2) + "]");
            p.sendMessage(RedProtect.get().getLanguageManager().get("general.color") + "------------------------------------");
        }
    }

    private static boolean validate(String flag, Object value) {
        if ((flag.equalsIgnoreCase("forcefly") ||
                flag.equalsIgnoreCase("can-death") ||
                flag.equalsIgnoreCase("can-pickup") ||
                flag.equalsIgnoreCase("can-drop") ||
                flag.equalsIgnoreCase("keep-inventory") ||
                flag.equalsIgnoreCase("keep-levels") ||
                flag.equalsIgnoreCase("allow-fly") ||
                flag.equalsIgnoreCase("door") ||
                flag.equalsIgnoreCase("button") ||
                flag.equalsIgnoreCase("lever") ||
                flag.equalsIgnoreCase("pvp") ||
                flag.equalsIgnoreCase("player-damage") ||
                flag.equalsIgnoreCase("can-hunger") ||
                flag.equalsIgnoreCase("can-projectiles") ||
                flag.equalsIgnoreCase("can-pet") ||
                flag.equalsIgnoreCase("portal-enter") ||
                flag.equalsIgnoreCase("allow-create-portal") ||
                flag.equalsIgnoreCase("allow-mod") ||
                flag.equalsIgnoreCase("portal-exit") ||
                flag.equalsIgnoreCase("enderpearl") ||
                flag.equalsIgnoreCase("can-back") ||
                flag.equalsIgnoreCase("up-skills") ||
                flag.equalsIgnoreCase("enter") ||
                flag.equalsIgnoreCase("treefarm") ||
                flag.equalsIgnoreCase("sign") ||
                flag.equalsIgnoreCase("invincible") ||
                flag.equalsIgnoreCase("flow-damage") ||
                flag.equalsIgnoreCase("mob-loot") ||
                flag.equalsIgnoreCase("allow-potions") ||
                flag.equalsIgnoreCase("smart-door") ||
                flag.equalsIgnoreCase("allow-magiccarpet") ||
                flag.equalsIgnoreCase("allow-home") ||
                flag.equalsIgnoreCase("minecart") ||
                flag.equalsIgnoreCase("forcepvp") ||
                flag.equalsIgnoreCase("dynmap") ||
                flag.equalsIgnoreCase("can-purge") ||
                flag.equalsIgnoreCase("minefarm")) && !(value instanceof Boolean)) {
            return false;
        }

        final String[] split = value.toString().trim().split(",");
        if (flag.equalsIgnoreCase("spawn-monsters")) {
            if (value instanceof Boolean) return true;

            if (!(value instanceof String)) {
                return false;
            }

            for (String val : split) {
                try {
                    EntityType entityType = EntityType.valueOf(val.toUpperCase());
                    if (!Monster.class.isAssignableFrom(entityType.getEntityClass())) {
                        return false;
                    }
                } catch (Exception ignored) {
                    return false;
                }
            }

            return true;
        }

        if (flag.equalsIgnoreCase("spawn-animals")) {
            if (value instanceof Boolean) return true;

            if (!(value instanceof String)) {
                return false;
            }

            for (String val : split) {
                try {
                    EntityType entityType = EntityType.valueOf(val.toUpperCase());
                    Class<? extends Entity> entityClass = entityType.getEntityClass();
                    if (!((!Monster.class.isAssignableFrom(entityClass) &&
                            !Player.class.isAssignableFrom(entityClass)) &&
                            !ArmorStand.class.isAssignableFrom(entityClass) &&
                            LivingEntity.class.isAssignableFrom(entityClass))) {
                        return false;
                    }
                } catch (Exception ignored) {
                    return false;
                }
            }
            return true;
        }

        if (flag.equalsIgnoreCase("particles")) {
            if (!(value instanceof String)) {
                return false;
            }
            String[] val = value.toString().split(" ");
            if (val.length != 2 && val.length != 5 && val.length != 6) {
                return false;
            }
            try {
                Particle.valueOf(val[0].toUpperCase());
            } catch (IllegalArgumentException e) {
                return false;
            }
            try {
                Integer.valueOf(val[1]);
            } catch (NumberFormatException e) {
                return false;
            }
            if (val.length >= 5) {
                try {
                    Double.parseDouble(val[2]);
                    Double.parseDouble(val[3]);
                    Double.parseDouble(val[4]);
                } catch (NumberFormatException e) {
                    return false;
                }
            }
            if (val.length == 6) {
                try {
                    Double.parseDouble(val[5]);
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        }

        if (flag.equalsIgnoreCase("gamemode")) {
            if (!(value instanceof String)) {
                return false;
            }
            try {
                GameMode.valueOf(value.toString().toUpperCase());
            } catch (IllegalArgumentException e) {
                return false;
            }
        }

        if (flag.equalsIgnoreCase("setclan") && RedProtect.get().hooks.checkSC()) {
            if (!(value instanceof String)) {
                return false;
            }
            if (RedProtect.get().hooks.clanManager.getClan(value.toString()) == null) {
                return false;
            }
        }

        if (flag.equalsIgnoreCase("set-portal")) {
            if (!(value instanceof String)) {
                return false;
            }
            String[] valida = value.toString().split(" ");
            if (valida.length != 2) {
                return false;
            }
            if (Bukkit.getWorld(valida[1]) == null) {
                return false;
            }
            Region r = RedProtect.get().getRegionManager().getRegion(valida[0], valida[1]);
            if (r == null) {
                return false;
            }
        }

        if (flag.equalsIgnoreCase("max-players")) {
            try {
                Integer.parseInt(value.toString());
            } catch (NumberFormatException e) {
                return false;
            }
        }
        if (flag.equalsIgnoreCase("cmd-onhealth")) {
            if (!(value instanceof String)) {
                return false;
            }
            try {
                String[] args = value.toString().split(",");
                for (String arg : args) {
                    if (!arg.split(" ")[0].startsWith("health:") || !arg.split(" ")[1].startsWith("cmd:")) {
                        return false;
                    }
                    //test health
                    int health = Integer.parseInt(arg.split(" ")[0].substring(7));
                    if (health < 0 || health > 20) {
                        return false;
                    }
                }
            } catch (Exception ex) {
                return false;
            }
        }
        if (flag.equalsIgnoreCase("allow-cmds") || flag.equalsIgnoreCase("deny-cmds")) {
            if (!(value instanceof String)) {
                return false;
            }
            try {
                String[] cmds = ((String) value).split(",");
                for (String cmd : cmds) {
                    if (cmd.contains("cmd:") || cmd.contains("arg:")) {
                        String[] cmdargs = cmd.split(" ");
                        for (String cmd1 : cmdargs) {
                            if (cmd1.startsWith("cmd:")) {
                                if (cmd1.split(":")[1].length() == 0) {
                                    return false;
                                }
                            }
                            if (cmd1.startsWith("arg:")) {
                                if (cmd1.split(":")[1].length() == 0) {
                                    return false;
                                }
                            }
                        }
                    } else {
                        return false;
                    }
                }
            } catch (Exception e) {
                return false;
            }
        }
        if (flag.equalsIgnoreCase("effects")) {
            if (!(value instanceof String)) {
                return false;
            }
            String[] effects = value.toString().split(",");
            for (String eff : effects) {
                String[] effect = eff.split(" ");
                if (effect.length < 2) {
                    return false;
                }
                if (PotionEffectType.getByName(effect[0]) == null) {
                    return false;
                }
                try {
                    Integer.parseInt(effect[1]);
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void handleKillWorld(CommandSender sender, World world, EntityType type) {
        int killed = 0;
        int total = 0;
        int living = 0;
        for (Entity e : world.getEntities().stream().filter(entity ->
                !(entity instanceof Player) &&
                        ((entity instanceof LivingEntity && type == null) || entity.getType().equals(type))
        ).collect(Collectors.toList())) {
            total++;
            if (RedProtect.get().getRegionManager().getTopRegion(e.getLocation()) == null) {
                e.remove();
                killed++;
            } else if (e instanceof LivingEntity && type == null) {
                living++;
            } else if (e.getType().equals(type)) {
                living++;
            }
        }
        RedProtect.get().getLanguageManager().sendMessage(sender, "cmdmanager.kill", new Replacer[]{
                new Replacer("{total}", String.valueOf(total)),
                new Replacer("{living}", String.valueOf(living)),
                new Replacer("{killed}", String.valueOf(killed)),
                new Replacer("{world}", world.getName())
        });
    }

    public static void HandleHelpPage(CommandSender sender, int page) {
        sender.sendMessage(RedProtect.get().getLanguageManager().get("_redprotect.prefix") + " " + RedProtect.get().getLanguageManager().get("cmdmanager.available.cmds"));
        sender.sendMessage(RedProtect.get().getLanguageManager().get("general.color") + "------------------------------------");
        sender.sendMessage(RedProtect.get().getLanguageManager().get("cmdmanager.helpheader.alias"));

        if (sender instanceof Player player) {
            int i = 0;
            for (String key : RedProtect.get().getLanguageManager().getHelpStrings()) {
                if (RedProtect.get().getPermissionHandler().hasCommandPerm(player, key) || ((key.equals("pos1") || key.equals("pos2")) && RedProtect.get().getPermissionHandler().hasCommandPerm(player, "redefine"))) {
                    if (key.equalsIgnoreCase("flaggui")) {
                        continue;
                    }
                    i++;

                    if (i > (page * 10) - 10 && i <= page * 10) {
                        player.sendMessage(RedProtect.get().getLanguageManager().get("cmdmanager.help." + key).replace("{cmd}", getCmd(key)).replace("{alias}", getCmdAlias(key)));
                    }
                    if (i > page * 10) {
                        player.sendMessage(RedProtect.get().getLanguageManager().get("general.color") + "------------------------------------");
                        player.sendMessage(RedProtect.get().getLanguageManager().get("cmdmanager.page").replace("{page}", "" + (page + 1)));
                        break;
                    }
                }
            }
        } else {
            sender.sendMessage(ChatColor.GOLD + "rp " + ChatColor.RED + "kick " + ChatColor.GOLD + "<player> <region> <world> " + ChatColor.DARK_AQUA + "- Kick a player from a region");
            sender.sendMessage(ChatColor.GOLD + "rp " + ChatColor.RED + "clear-kicks " + ChatColor.DARK_AQUA + "- Clear all pendent kicks");
            sender.sendMessage(ChatColor.GOLD + "rp " + ChatColor.RED + "info " + ChatColor.GOLD + "<region> <world> " + ChatColor.DARK_AQUA + "- Info about a region");
            sender.sendMessage(ChatColor.GOLD + "rp " + ChatColor.RED + "flag " + ChatColor.GOLD + "<regionName> <Flag> <Value> <World> " + ChatColor.DARK_AQUA + "- Set a flag on region");
            sender.sendMessage(ChatColor.GOLD + "rp " + ChatColor.RED + "flag " + ChatColor.GOLD + "info <region> <world> " + ChatColor.DARK_AQUA + "- Flag info for region");
            sender.sendMessage(ChatColor.GOLD + "rp " + ChatColor.RED + "addmember " + ChatColor.GOLD + "<player> <region> <world> " + ChatColor.DARK_AQUA + "- Add player as member on region");
            sender.sendMessage(ChatColor.GOLD + "rp " + ChatColor.RED + "addadmin " + ChatColor.GOLD + "<player> <region> <world> " + ChatColor.DARK_AQUA + "- Add player as admin on region");
            sender.sendMessage(ChatColor.GOLD + "rp " + ChatColor.RED + "addleader " + ChatColor.GOLD + "<player> <region> <world> " + ChatColor.DARK_AQUA + "- Add player as leader on region");
            sender.sendMessage(ChatColor.GOLD + "rp " + ChatColor.RED + "removemember " + ChatColor.GOLD + "<player> <region> <world> " + ChatColor.DARK_AQUA + "- Remove a player as member on region");
            sender.sendMessage(ChatColor.GOLD + "rp " + ChatColor.RED + "removeadmin " + ChatColor.GOLD + "<player> <region> <world> " + ChatColor.DARK_AQUA + "- Remove a player as admin on region");
            sender.sendMessage(ChatColor.GOLD + "rp " + ChatColor.RED + "removeleader " + ChatColor.GOLD + "<player> <region> <world> " + ChatColor.DARK_AQUA + "- Remove a player as leader on region");
            sender.sendMessage(ChatColor.GOLD + "rp " + ChatColor.RED + "teleport " + ChatColor.GOLD + "<playerName> <regionName> <World> " + ChatColor.DARK_AQUA + "- Teleport player to a region");
            sender.sendMessage(ChatColor.GOLD + "rp " + ChatColor.RED + "blocklimit " + ChatColor.GOLD + "<playerName> " + ChatColor.DARK_AQUA + "- Area limit for player");
            sender.sendMessage(ChatColor.GOLD + "rp " + ChatColor.RED + "claimlimit " + ChatColor.GOLD + "<playerName> [world] " + ChatColor.DARK_AQUA + "- Claim limit for player");
            sender.sendMessage(ChatColor.GOLD + "rp " + ChatColor.RED + "list-areas " + ChatColor.DARK_AQUA + "- List All regions exceeding regen limit");
            sender.sendMessage(ChatColor.GOLD + "rp " + ChatColor.RED + "list-all " + ChatColor.DARK_AQUA + "- List All regions");
            sender.sendMessage(ChatColor.GOLD + "rp " + ChatColor.RED + "list " + ChatColor.GOLD + "<player> [page] " + ChatColor.DARK_AQUA + "- List All regions from player");
            sender.sendMessage(ChatColor.GOLD + "rp " + ChatColor.RED + "fileTomysql " + ChatColor.DARK_AQUA + "- Convert from File to Mysql");
            sender.sendMessage(ChatColor.GOLD + "rp " + ChatColor.RED + "kill [world] " + ChatColor.DARK_AQUA + "- Kill all entities in a world outside protected regions");
            sender.sendMessage(ChatColor.GOLD + "rp " + ChatColor.RED + "mychunktorp " + ChatColor.DARK_AQUA + "- Convert from MyChunk to RedProtect");
            sender.sendMessage(ChatColor.GOLD + "rp " + ChatColor.RED + "single-to-files " + ChatColor.DARK_AQUA + "- Convert single world files to regions files");
            sender.sendMessage(ChatColor.GOLD + "rp " + ChatColor.RED + "files-to-single " + ChatColor.DARK_AQUA + "- Convert regions files to single world files");
            sender.sendMessage(ChatColor.GOLD + "rp " + ChatColor.RED + "gpTorp " + ChatColor.DARK_AQUA + "- Convert from GriefPrevention to RedProtect");
            sender.sendMessage(ChatColor.GOLD + "rp " + ChatColor.RED + "save-all " + ChatColor.GOLD + "[-f]" + ChatColor.DARK_AQUA + "- Save all regions to database");
            sender.sendMessage(ChatColor.GOLD + "rp " + ChatColor.RED + "load-all " + ChatColor.DARK_AQUA + "- Load all regions from database");
            sender.sendMessage(ChatColor.GOLD + "rp " + ChatColor.RED + "reload-config " + ChatColor.DARK_AQUA + "- Reload only the configs");
            sender.sendMessage(ChatColor.GOLD + "rp " + ChatColor.RED + "reload " + ChatColor.DARK_AQUA + "- Reload the plugin");
        }
        sender.sendMessage(RedProtect.get().getLanguageManager().get("general.color") + "------------------------------------");
        if (RedProtect.get().getPermissionHandler().hasPerm(sender, "")) {
            String jarversion = new java.io.File(RedProtect.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .getPath())
                    .getName();
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&o- Full version: " + jarversion));
        }
    }

    public static String getCmd(String cmd) {
        return RedProtect.get().getLanguageManager().get("cmdmanager.translation." + cmd);
    }

    public static String getCmdAlias(String cmd) {
        return RedProtect.get().getLanguageManager().get("cmdmanager.translation." + cmd + ".alias");
    }

    public static boolean checkCmd(String arg, String cmd) {
        return arg.equalsIgnoreCase(getCmd(cmd)) || arg.equalsIgnoreCase(getCmdAlias(cmd)) || arg.equalsIgnoreCase(cmd);
    }
}
