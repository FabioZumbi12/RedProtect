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

package br.net.fabiozumbi12.RedProtect.Bukkit.commands;

import br.net.fabiozumbi12.RedProtect.Bukkit.API.events.DeleteRegionEvent;
import br.net.fabiozumbi12.RedProtect.Bukkit.API.events.RenameRegionEvent;
import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import br.net.fabiozumbi12.RedProtect.Bukkit.fanciful.FancyMessage;
import br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RedProtectUtil;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class CommandHandlers {

    // TODO Region handlers
    public static void handleAddLeader(CommandSender src, String sVictim, Region r) {
        if (src instanceof Player) {
            Player p = (Player) src;
            r = RedProtect.get().rm.getTopRegion(p.getLocation());
            if (r == null) {
                RedProtect.get().lang.sendMessage(p, "cmdmanager.region.todo.that");
                return;
            }
        }

        if (RedProtect.get().ph.hasRegionPermLeader(src, "addleader", r)) {
            final Player pVictim = RedProtect.get().getServer().getPlayer(sVictim);

            final String VictimUUID = RedProtectUtil.PlayerToUUID(sVictim);
            if ((pVictim == null || !pVictim.isOnline()) && !src.hasPermission("redprotect.command.admin.addleader")) {
                RedProtect.get().lang.sendMessage(src, RedProtect.get().lang.get("cmdmanager.noplayer.online").replace("{player}", sVictim));
                return;
            }

            if (!src.hasPermission("redprotect.command.admin.addleader")) {
                int claimLimit = RedProtect.get().ph.getPlayerClaimLimit(pVictim);
                int claimused = RedProtect.get().rm.getPlayerRegions(pVictim.getUniqueId().toString(), pVictim.getWorld());
                boolean claimUnlimited = RedProtect.get().ph.hasPerm(src, "redprotect.limits.claim.unlimited");
                if (claimused >= claimLimit && claimLimit >= 0 && !claimUnlimited) {
                    RedProtect.get().lang.sendMessage(src, RedProtect.get().lang.get("cmdmanager.region.addleader.limit").replace("{player}", pVictim.getName()));
                    return;
                }
            }

            if (!r.isLeader(VictimUUID)) {

                if (src.hasPermission("redprotect.command.admin.addleader")) {
                    r.addLeader(VictimUUID);
                    RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + src.getName() + " ADDED LEADER " + RedProtectUtil.UUIDtoPlayer(VictimUUID) + " to region " + r.getName());
                    RedProtect.get().lang.sendMessage(src, RedProtect.get().lang.get("general.color") + sVictim + " " + RedProtect.get().lang.get("cmdmanager.region.leader.added") + " " + r.getName());
                    if (pVictim != null && pVictim.isOnline() && !pVictim.equals(src)) {
                        RedProtect.get().lang.sendMessage(pVictim, RedProtect.get().lang.get("cmdmanager.region.leader.youadded").replace("{region}", r.getName()) + " " + src.getName());
                    }
                    return;
                }

                RedProtect.get().lang.sendMessage(src, RedProtect.get().lang.get("cmdmanager.region.leader.yousendrequest").replace("{player}", pVictim.getName()));
                RedProtect.get().lang.sendMessage(pVictim, RedProtect.get().lang.get("cmdmanager.region.leader.sendrequestto").replace("{region}", r.getName()).replace("{player}", src.getName()));

                RedProtect.get().alWait.put(pVictim, r.getID() + "@" + src.getName());
                Bukkit.getScheduler().scheduleSyncDelayedTask(RedProtect.get(), () -> {
                    if (RedProtect.get().alWait.containsKey(pVictim)) {
                        RedProtect.get().alWait.remove(pVictim);
                        if (src instanceof Player && ((Player) src).isOnline()) {
                            RedProtect.get().lang.sendMessage(src, RedProtect.get().lang.get("cmdmanager.region.leader.requestexpired").replace("{player}", pVictim.getName()));
                        }
                    }
                }, RedProtect.get().config.configRoot().region_settings.leadership_request_time * 20);
            } else {
                RedProtect.get().lang.sendMessage(src, ChatColor.RED + sVictim + " " + RedProtect.get().lang.get("cmdmanager.region.leader.already"));
            }
        } else if (src instanceof Player) {
            RedProtect.get().lang.sendMessage(src, "no.permission");
        }
    }

    public static void handleRemoveLeader(CommandSender src, String sVictim, Region r) {
        Region rLow = null;
        Map<Integer, Region> regions = new HashMap<>();
        if (src instanceof Player) {
            Player p = (Player) src;
            r = RedProtect.get().rm.getTopRegion(p.getLocation());
            rLow = RedProtect.get().rm.getLowRegion(p.getLocation());
            regions = RedProtect.get().rm.getGroupRegion(p.getLocation());
            if (r == null) {
                RedProtect.get().lang.sendMessage(p, "cmdmanager.region.todo.that");
                return;
            }
        }

        if (RedProtect.get().ph.hasRegionPermLeader(src, "removeleader", r)) {
            Player pVictim = RedProtect.get().getServer().getPlayer(sVictim);

            String VictimUUID = RedProtectUtil.PlayerToUUID(sVictim);
            if (RedProtectUtil.UUIDtoPlayer(VictimUUID) == null) {
                RedProtect.get().lang.sendMessage(src, RedProtect.get().lang.get("cmdmanager.noplayer.thisname").replace("{player}", sVictim));
                return;
            }

            if (rLow != null && rLow != r && ((!RedProtect.get().ph.hasRegionPermLeader(src, "removeleader", rLow) || (regions.size() > 1 && rLow.isLeader(VictimUUID))))) {
                RedProtect.get().lang.sendMessage(src, RedProtect.get().lang.get("cmdmanager.region.leader.cantremove.lowregion").replace("{player}", sVictim) + " " + rLow.getName());
                return;
            }

            String victname = RedProtectUtil.UUIDtoPlayer(VictimUUID);
            if (r.isLeader(VictimUUID)) {
                if (r.leaderSize() > 1) {
                    RedProtect.get().lang.sendMessage(src, RedProtect.get().lang.get("general.color") + sVictim + " " + RedProtect.get().lang.get("cmdmanager.region.admin.added") + " " + r.getName());
                    r.removeLeader(VictimUUID);
                    RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + src.getName() + " DEMOTED TO ADMIN " + victname + " to region " + r.getName());
                    if (pVictim != null && pVictim.isOnline() && !pVictim.equals(src)) {
                        RedProtect.get().lang.sendMessage(pVictim, RedProtect.get().lang.get("cmdmanager.region.leader.youdemoted").replace("{region}", r.getName()) + " " + src.getName());
                    }
                } else {
                    RedProtect.get().lang.sendMessage(src, RedProtect.get().lang.get("cmdmanager.region.leader.cantremove").replace("{player}", sVictim));
                }
            } else {
                RedProtect.get().lang.sendMessage(src, ChatColor.RED + sVictim + " " + RedProtect.get().lang.get("cmdmanager.region.leader.notleader"));
            }
        } else if (src instanceof Player) {
            RedProtect.get().lang.sendMessage(src, "no.permission");
        }
    }

    public static void handleAddAdmin(CommandSender src, String sVictim, Region r) {
        if (src instanceof Player) {
            Player p = (Player) src;
            r = RedProtect.get().rm.getTopRegion(p.getLocation());
            if (r == null) {
                RedProtect.get().lang.sendMessage(p, "cmdmanager.region.todo.that");
                return;
            }
        }

        if (RedProtect.get().ph.hasRegionPermAdmin(src, "addadmin", r)) {
            Player pVictim = RedProtect.get().getServer().getPlayer(sVictim);

            String VictimUUID = RedProtectUtil.PlayerToUUID(sVictim);
            if (RedProtectUtil.UUIDtoPlayer(VictimUUID) == null) {
                RedProtect.get().lang.sendMessage(src, RedProtect.get().lang.get("cmdmanager.noplayer.thisname").replace("{player}", sVictim));
                return;
            }

            if (r.isLeader(VictimUUID)) {
                RedProtect.get().lang.sendMessage(src, ChatColor.RED + sVictim + " " + RedProtect.get().lang.get("cmdmanager.region.leader.already"));
                return;
            }

            if (!r.isAdmin(VictimUUID)) {
                r.addAdmin(VictimUUID);
                RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + src.getName() + " ADDED ADMIN " + RedProtectUtil.UUIDtoPlayer(VictimUUID) + " to region " + r.getName());
                RedProtect.get().lang.sendMessage(src, RedProtect.get().lang.get("general.color") + sVictim + " " + RedProtect.get().lang.get("cmdmanager.region.admin.added") + " " + r.getName());
                if (pVictim != null && pVictim.isOnline() && !pVictim.equals(src)) {
                    RedProtect.get().lang.sendMessage(pVictim, RedProtect.get().lang.get("cmdmanager.region.admin.youadded").replace("{region}", r.getName()) + " " + src.getName());
                }
            } else {
                RedProtect.get().lang.sendMessage(src, ChatColor.RED + sVictim + " " + RedProtect.get().lang.get("cmdmanager.region.admin.already"));
            }
        } else if (src instanceof Player) {
            RedProtect.get().lang.sendMessage(src, "no.permission");
        }
    }

    public static void handleRemoveAdmin(CommandSender src, String sVictim, Region r) {
        if (src instanceof Player) {
            Player p = (Player) src;
            r = RedProtect.get().rm.getTopRegion(p.getLocation());
            if (r == null) {
                RedProtect.get().lang.sendMessage(p, "cmdmanager.region.todo.that");
                return;
            }
        }

        if (RedProtect.get().ph.hasRegionPermAdmin(src, "removeadmin", r)) {
            Player pVictim = RedProtect.get().getServer().getPlayer(sVictim);

            String VictimUUID = RedProtectUtil.PlayerToUUID(sVictim);
            if (RedProtectUtil.UUIDtoPlayer(VictimUUID) == null) {
                RedProtect.get().lang.sendMessage(src, RedProtect.get().lang.get("cmdmanager.noplayer.thisname").replace("{player}", sVictim));
                return;
            }

            String victname = RedProtectUtil.UUIDtoPlayer(VictimUUID);
            if (r.isAdmin(VictimUUID)) {
                RedProtect.get().lang.sendMessage(src, RedProtect.get().lang.get("general.color") + sVictim + " " + RedProtect.get().lang.get("cmdmanager.region.member.added") + " " + r.getName());
                r.removeAdmin(VictimUUID);
                RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + src.getName() + " DEMOTED TO MEMBER " + victname + " to region " + r.getName());
                if (pVictim != null && pVictim.isOnline() && !pVictim.equals(src)) {
                    RedProtect.get().lang.sendMessage(pVictim, RedProtect.get().lang.get("cmdmanager.region.admin.youdemoted").replace("{region}", r.getName()) + " " + src.getName());
                }
            } else {
                RedProtect.get().lang.sendMessage(src, ChatColor.RED + sVictim + " " + RedProtect.get().lang.get("cmdmanager.region.admin.notadmin"));
            }
        } else if (src instanceof Player) {
            RedProtect.get().lang.sendMessage(src, "no.permission");
        }
    }

    public static void handleAddMember(CommandSender src, String sVictim, Region r) {
        if (src instanceof Player) {
            Player p = (Player) src;
            r = RedProtect.get().rm.getTopRegion(p.getLocation());
            if (r == null) {
                RedProtect.get().lang.sendMessage(p, "cmdmanager.region.todo.that");
                return;
            }
        }

        if (RedProtect.get().ph.hasRegionPermAdmin(src, "addmember", r)) {
            String VictimUUID = RedProtectUtil.PlayerToUUID(sVictim);
            if (RedProtectUtil.UUIDtoPlayer(VictimUUID) == null) {
                RedProtect.get().lang.sendMessage(src, RedProtect.get().lang.get("cmdmanager.noplayer.thisname").replace("{player}", sVictim));
                return;
            }

            Player pVictim = RedProtect.get().getServer().getPlayer(sVictim);

            if (r.isLeader(VictimUUID)) {
                RedProtect.get().lang.sendMessage(src, ChatColor.RED + sVictim + " " + RedProtect.get().lang.get("cmdmanager.region.leader.already"));
                return;
            }

            if (r.isAdmin(VictimUUID)) {
                r.addMember(VictimUUID);
                RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + src.getName() + " ADDED MEMBER " + RedProtectUtil.UUIDtoPlayer(VictimUUID) + " to region " + r.getName());
                RedProtect.get().lang.sendMessage(src, RedProtect.get().lang.get("general.color") + sVictim + " " + RedProtect.get().lang.get("cmdmanager.region.member.demoted") + " " + r.getName());
                if (pVictim != null && pVictim.isOnline()) {
                    RedProtect.get().lang.sendMessage(pVictim, RedProtect.get().lang.get("cmdmanager.region.admin.youdemoted").replace("{region}", r.getName()) + " " + src.getName());
                }
            } else if (!r.isMember(VictimUUID)) {
                r.addMember(VictimUUID);
                RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + src.getName() + " ADDED MEMBER " + RedProtectUtil.UUIDtoPlayer(VictimUUID) + " to region " + r.getName());
                RedProtect.get().lang.sendMessage(src, RedProtect.get().lang.get("general.color") + sVictim + " " + RedProtect.get().lang.get("cmdmanager.region.member.added") + " " + r.getName());
                if (pVictim != null && pVictim.isOnline() && !pVictim.equals(src)) {
                    RedProtect.get().lang.sendMessage(pVictim, RedProtect.get().lang.get("cmdmanager.region.member.youadded").replace("{region}", r.getName()) + " " + src.getName());
                }
            } else {
                RedProtect.get().lang.sendMessage(src, ChatColor.RED + sVictim + " " + RedProtect.get().lang.get("cmdmanager.region.member.already"));
            }
        } else if (src instanceof Player) {
            RedProtect.get().lang.sendMessage(src, "no.permission");
        }
    }

    public static void handleRemoveMember(CommandSender src, String sVictim, Region r) {
        if (src instanceof Player) {
            Player p = (Player) src;
            r = RedProtect.get().rm.getTopRegion(p.getLocation());
            if (r == null) {
                RedProtect.get().lang.sendMessage(p, "cmdmanager.region.todo.that");
                return;
            }
        }

        if (RedProtect.get().ph.hasRegionPermAdmin(src, "removemember", r)) {

            Player pVictim = RedProtect.get().getServer().getPlayer(sVictim);

            String VictimUUID = RedProtectUtil.PlayerToUUID(sVictim);
            if (RedProtectUtil.UUIDtoPlayer(VictimUUID) == null) {
                RedProtect.get().lang.sendMessage(src, RedProtect.get().lang.get("cmdmanager.noplayer.thisname").replace("{player}", sVictim));
                return;
            }

            String victname = RedProtectUtil.UUIDtoPlayer(VictimUUID);

            if ((r.isMember(VictimUUID) || r.isAdmin(VictimUUID)) && !r.isLeader(VictimUUID)) {
                RedProtect.get().lang.sendMessage(src, RedProtect.get().lang.get("general.color") + sVictim + " " + RedProtect.get().lang.get("cmdmanager.region.member.removed") + " " + r.getName());
                r.removeMember(VictimUUID);
                RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + src.getName() + " REMOVED MEMBER " + victname + " to region " + r.getName());
                if (pVictim != null && pVictim.isOnline() && !pVictim.equals(src)) {
                    RedProtect.get().lang.sendMessage(pVictim, RedProtect.get().lang.get("cmdmanager.region.member.youremoved").replace("{region}", r.getName()) + " " + src.getName());
                }
            } else {
                RedProtect.get().lang.sendMessage(src, ChatColor.RED + sVictim + " " + RedProtect.get().lang.get("cmdmanager.region.member.notmember"));
            }
        } else if (src instanceof Player) {
            RedProtect.get().lang.sendMessage(src, "no.permission");
        }
    }

    public static void handleDelete(Player p) {
        Region r = RedProtect.get().rm.getTopRegion(p.getLocation());
        if (RedProtect.get().ph.hasRegionPermLeader(p, "delete", r)) {
            if (r == null) {
                RedProtect.get().lang.sendMessage(p, "cmdmanager.region.todo.that");
                return;
            }

            int claims = RedProtect.get().config.configRoot().region_settings.can_delete_first_home_after_claims;
            if (!r.canDelete() && (claims == -1 || RedProtect.get().rm.getPlayerRegions(p.getUniqueId().toString(), p.getWorld()) < claims) && !p.hasPermission("redprotect.bypass")) {
                if (claims != -1) {
                    RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("cmdmanager.region.cantdeletefirst-claims").replace("{claims}", "" + claims));
                } else {
                    RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("cmdmanager.region.cantdeletefirst"));
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
            RedProtect.get().rm.remove(r, RedProtect.get().getServer().getWorld(w));
            RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("cmdmanager.region.deleted") + " " + rname);
            RedProtect.get().logger.addLog("(World " + w + ") Player " + p.getName() + " REMOVED region " + rname);
        } else {
            RedProtect.get().lang.sendMessage(p, "no.permission");
        }
    }

    public static void handleDeleteName(Player p, String rname, String world) {
        Region r = RedProtect.get().rm.getRegion(rname, p.getWorld());
        if (!world.equals("")) {
            if (Bukkit.getWorld(world) != null) {
                r = RedProtect.get().rm.getRegion(rname, Bukkit.getWorld(world));
            } else {
                RedProtect.get().lang.sendMessage(p, "cmdmanager.region.invalidworld");
                return;
            }
        }

        if (RedProtect.get().ph.hasRegionPermLeader(p, "delete", r)) {
            if (r == null) {
                RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("cmdmanager.region.doesntexist") + ": " + rname);
                return;
            }

            int claims = RedProtect.get().config.configRoot().region_settings.can_delete_first_home_after_claims;
            if (!r.canDelete() && (claims == -1 || RedProtect.get().rm.getPlayerRegions(p.getUniqueId().toString(), p.getWorld()) < claims) && !p.hasPermission("redprotect.bypass")) {
                if (claims != -1) {
                    RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("cmdmanager.region.cantdeletefirst-claims").replace("{claims}", "" + claims));
                } else {
                    RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("cmdmanager.region.cantdeletefirst"));
                }
                return;
            }

            DeleteRegionEvent event = new DeleteRegionEvent(r, p);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }

            RedProtect.get().rm.remove(r, RedProtect.get().getServer().getWorld(r.getWorld()));
            RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("cmdmanager.region.deleted") + " " + rname);
            RedProtect.get().logger.addLog("(World " + world + ") Player " + p.getName() + " REMOVED region " + rname);
        } else {
            RedProtect.get().lang.sendMessage(p, "no.permission");
        }
    }

    public static void handleRename(Player p, String newName) {
        Region r = RedProtect.get().rm.getTopRegion(p.getLocation());
        if (RedProtect.get().ph.hasRegionPermLeader(p, "rename", r)) {
            if (r == null) {
                RedProtect.get().lang.sendMessage(p, "cmdmanager.region.todo.that");
                return;
            }

            //filter name
            newName = RedProtectUtil.setName(newName);

            //filter region name
            if (newName.isEmpty() || newName.length() < 4) {
                newName = RedProtectUtil.nameGen(p.getName(), p.getWorld().getName());
                if (newName.length() > 16) {
                    RedProtect.get().lang.sendMessage(p, "cmdmanager.region.rename.invalid");
                    return;
                }
            }

            //region name conform
            if (newName.length() < 3) {
                RedProtect.get().lang.sendMessage(p, "regionbuilder.regionname.invalid");
                return;
            }

            if (RedProtect.get().rm.getRegion(newName, p.getWorld()) != null) {
                RedProtect.get().lang.sendMessage(p, "regionbuilder.regionname.existis");
                return;
            }

            RenameRegionEvent event = new RenameRegionEvent(r, newName, r.getName(), p);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }

            String oldname = event.getOldName();
            newName = event.getNewName();

            Region newRegion = RedProtect.get().rm.renameRegion(newName, r);
            RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("cmdmanager.region.rename.newname") + " " + newRegion.getName());
            RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + p.getName() + " RENAMED region " + oldname + " to " + newRegion.getName());
        } else {
            RedProtect.get().lang.sendMessage(p, "no.permission");
        }
    }

    // TODO Other Handlers
    public static void handlePrioritySingle(Player p, int prior, String region) {
        Region r = RedProtect.get().rm.getRegion(region, p.getWorld());
        if (RedProtect.get().ph.hasRegionPermLeader(p, "priority", r)) {
            if (r != null) {
                r.setPrior(prior);
                RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("cmdmanager.region.priority.set").replace("{region}", r.getName()) + " " + prior);
                RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + p.getName() + " SET PRIORITY of region " + r.getName() + " to " + prior);
            } else {
                RedProtect.get().lang.sendMessage(p, "cmdmanager.region.todo.that");
            }
        }
    }

    public static void handlePriority(Player p, int prior) {
        Region r = RedProtect.get().rm.getTopRegion(p.getLocation());
        if (RedProtect.get().ph.hasRegionPermLeader(p, "priority", r)) {
            if (r != null) {
                r.setPrior(prior);
                RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("cmdmanager.region.priority.set").replace("{region}", r.getName()) + " " + prior);
                RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + p.getName() + " SET PRIORITY of region " + r.getName() + " to " + prior);
            } else {
                RedProtect.get().lang.sendMessage(p, "cmdmanager.region.todo.that");
            }
        }
    }

    public static void handleInfoTop(Player p) {
        Region r = RedProtect.get().rm.getTopRegion(p.getLocation());
        if (r == null) {
            RedProtect.get().lang.sendMessage(p, "cmdmanager.region.todo.that");
            return;
        }
        Map<Integer, Region> groupr = RedProtect.get().rm.getGroupRegion(p.getLocation());
        if (RedProtect.get().ph.hasRegionPermAdmin(p, "info", r)) {
            p.sendMessage(RedProtect.get().lang.get("general.color") + "--------------- [" + ChatColor.GOLD + r.getName() + RedProtect.get().lang.get("general.color") + "] ---------------");
            p.sendMessage(r.info());
            p.sendMessage(RedProtect.get().lang.get("general.color") + "----------------------------------");
            if (groupr.size() > 1) {
                p.sendMessage(RedProtect.get().lang.get("cmdmanager.moreregions"));
                for (Region regs : groupr.values()) {
                    if (regs != r) {
                        p.sendMessage(RedProtect.get().lang.get("region.name") + " " + regs.getName() + " " + RedProtect.get().lang.get("region.priority") + " " + regs.getPrior());
                    }
                }
            }
        } else {
            RedProtect.get().lang.sendMessage(p, "no.permission");
        }
    }

    public static void handleInfo(Player p, String region, String world) {
        Region r = RedProtect.get().rm.getRegion(region, p.getWorld());
        if (!world.equals("")) {
            if (Bukkit.getWorld(world) != null) {
                r = RedProtect.get().rm.getRegion(region, Bukkit.getWorld(world));
            } else {
                RedProtect.get().lang.sendMessage(p, "cmdmanager.region.invalidworld");
                return;
            }
        }
        if (RedProtect.get().ph.hasRegionPermAdmin(p, "info", r)) {
            if (r == null) {
                RedProtect.get().lang.sendMessage(p, "cmdmanager.region.todo.that");
                return;
            }
            p.sendMessage(RedProtect.get().lang.get("general.color") + "--------------- [" + ChatColor.GOLD + r.getName() + RedProtect.get().lang.get("general.color") + "] ---------------");
            p.sendMessage(r.info());
            p.sendMessage(RedProtect.get().lang.get("general.color") + "----------------------------------");
        } else {
            RedProtect.get().lang.sendMessage(p, "no.permission");
        }
    }

    public static void handletp(Player p, String rname, String wname, Player play) {
        World w = RedProtect.get().getServer().getWorld(wname);
        if (w == null) {
            RedProtect.get().lang.sendMessage(p, "cmdmanager.region.invalidworld");
            return;
        }
        Region region = RedProtect.get().rm.getRegion(rname, w);
        if (region == null) {
            RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("cmdmanager.region.doesntexist") + ": " + rname);
            return;
        }

        if (play == null) {
            if (!RedProtect.get().ph.hasRegionPermMember(p, "teleport", region)) {
                RedProtect.get().lang.sendMessage(p, "no.permission");
                return;
            }
        } else {
            if (!RedProtect.get().ph.hasPerm(p, "redprotect.command.admin.teleport")) {
                RedProtect.get().lang.sendMessage(p, "no.permission");
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
                if (RedProtect.get().hooks.essentials) {
                    RedProtect.get().hooks.pless.getUser(p).setLastLocation();
                }
                play.teleport(loc);
                RedProtect.get().lang.sendMessage(play, RedProtect.get().lang.get("cmdmanager.region.teleport") + " " + rname);
                RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("cmdmanager.region.tpother") + " " + rname);
            } else {
                tpWait(p, loc, rname);
            }
        }
    }

    private static void tpWait(final Player p, final Location loc, final String rname) {
        if (p.hasPermission("redprotect.command.admin.teleport")) {
            p.teleport(loc);
            return;
        }
        if (!RedProtect.get().tpWait.contains(p.getName())) {
            RedProtect.get().tpWait.add(p.getName());
            RedProtect.get().lang.sendMessage(p, "cmdmanager.region.tpdontmove");
            Bukkit.getScheduler().scheduleSyncDelayedTask(RedProtect.get(), () -> {
                if (RedProtect.get().tpWait.contains(p.getName())) {
                    RedProtect.get().tpWait.remove(p.getName());
                    if (RedProtect.get().hooks.essentials) {
                        RedProtect.get().hooks.pless.getUser(p).setLastLocation();
                    }
                    p.teleport(loc);
                    RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("cmdmanager.region.teleport") + " " + rname);
                }
            }, RedProtect.get().config.configRoot().region_settings.teleport_time * 20);
        } else {
            RedProtect.get().lang.sendMessage(p, "cmdmanager.region.tpneedwait");
        }
    }

    public static void handleWelcome(Player p, String wMessage) {
        Region r = RedProtect.get().rm.getTopRegion(p.getLocation());
        if (RedProtect.get().ph.hasRegionPermAdmin(p, "welcome", r)) {
            if (r != null) {
                switch (wMessage) {
                    case "":
                        r.setWelcome("");
                        RedProtect.get().lang.sendMessage(p, "cmdmanager.region.welcomeoff");
                        break;
                    case "hide ":
                        r.setWelcome(wMessage);
                        RedProtect.get().lang.sendMessage(p, "cmdmanager.region.welcomehide");
                        break;
                    default:
                        r.setWelcome(wMessage);
                        RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("cmdmanager.region.welcomeset") + " " + ChatColor.translateAlternateColorCodes('&', wMessage));
                        break;
                }
                RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + p.getName() + " SET WELCOME of region " + r.getName() + " to " + wMessage);
                return;
            } else {
                RedProtect.get().lang.sendMessage(p, "cmdmanager.region.todo.that");
                return;
            }
        }
        RedProtect.get().lang.sendMessage(p, "no.permission");
    }

    public static void handleList(Player p, String uuid, int Page) {
        if (RedProtect.get().ph.hasPerm(p, "redprotect.command.admin.list")) {
            getRegionforList(p, uuid, Page);
            return;
        } else if (RedProtect.get().ph.hasPerm(p, "redprotect.command.list") && p.getUniqueId().toString().equalsIgnoreCase(uuid)) {
            getRegionforList(p, uuid, Page);
            return;
        }
        RedProtect.get().lang.sendMessage(p, "no.permission");
    }

    public static void getRegionforList(CommandSender sender, String uuid, int nPage) {
        Bukkit.getScheduler().runTaskAsynchronously(RedProtect.get(), () -> {
            int Page = nPage;
            Set<Region> regions = RedProtect.get().rm.getRegions(uuid);
            int length = regions.size();
            if (length == 0) {
                RedProtect.get().lang.sendMessage(sender, "cmdmanager.player.noregions");
            } else {
                sender.sendMessage(RedProtect.get().lang.get("general.color") + "-------------------------------------------------");
                RedProtect.get().lang.sendMessage(sender, RedProtect.get().lang.get("cmdmanager.region.created.list") + " " + RedProtectUtil.UUIDtoPlayer(uuid));

                int regionsPage = RedProtect.get().config.configRoot().region_settings.region_list.region_per_page;
                int total = 0;
                int last = 0;

                for (World w : Bukkit.getWorlds()) {
                    boolean first = true;

                    if (Page == 0) {
                        Page = 1;
                    }
                    int max = (regionsPage * Page);
                    int min = max - regionsPage;
                    int count;

                    String colorChar = ChatColor.translateAlternateColorCodes('&', RedProtect.get().config.configRoot().region_settings.world_colors.get(w.getName()));
                    Set<Region> wregions = RedProtect.get().rm.getRegions(uuid, w);
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
                        if (max > it.size()) max = (it.size() - 1);
                        //-------------
                        if (RedProtect.get().config.configRoot().region_settings.region_list.hover_and_click_teleport && RedProtect.get().ph.hasRegionPermAdmin(sender, "teleport", null)) {
                            FancyMessage fancy = new FancyMessage();
                            for (int i = min; i <= max; i++) {
                                count = i;
                                Region r = it.get(i);
                                String area = RedProtect.get().config.configRoot().region_settings.region_list.shpw_area ? "(" + RedProtectUtil.simuleTotalRegionSize(RedProtectUtil.PlayerToUUID(uuid), r) + ")" : "";
                                String rname = RedProtect.get().lang.get("general.color") + ", " + ChatColor.GRAY + r.getName() + area;
                                if (first) {
                                    rname = rname.substring(3);
                                    first = false;
                                }
                                if (count == max) {
                                    rname = rname + RedProtect.get().lang.get("general.color") + ".";
                                }
                                fancy.text(rname).color(ChatColor.DARK_GRAY)
                                        .tooltip(RedProtect.get().lang.get("cmdmanager.list.hover").replace("{region}", r.getName()))
                                        .command("/rp " + getCmd("teleport") + " " + r.getName() + " " + r.getWorld())
                                        .then(" ");
                                lastLocal = count;
                            }
                            last += lastLocal + 1;
                            sender.sendMessage("-----");
                            sender.sendMessage(RedProtect.get().lang.get("general.color") + RedProtect.get().lang.get("region.world").replace(":", "") + " " + colorChar + w.getName() + "[" + (min + 1) + "-" + (max + 1) + "/" + wregions.size() + "]" + ChatColor.RESET + ": ");
                            fancy.send(sender);
                        } else {
                            StringBuilder worldregions = new StringBuilder();
                            for (int i = min; i <= max; i++) {
                                count = i;
                                Region r = it.get(i);
                                String area = RedProtect.get().config.configRoot().region_settings.region_list.shpw_area ? "(" + RedProtectUtil.simuleTotalRegionSize(RedProtectUtil.PlayerToUUID(uuid), r) + ")" : "";
                                worldregions.append(RedProtect.get().lang.get("general.color")).append(", ").append(ChatColor.GRAY).append(r.getName()).append(area);
                                lastLocal = count;
                            }
                            last += lastLocal + 1;
                            sender.sendMessage("-----");
                            sender.sendMessage(RedProtect.get().lang.get("general.color") + RedProtect.get().lang.get("region.world").replace(":", "") + " " + colorChar + w.getName() + "[" + (min + 1) + "-" + (max + 1) + "/" + wregions.size() + "]" + ChatColor.RESET + ": ");
                            sender.sendMessage(worldregions.substring(3) + RedProtect.get().lang.get("general.color") + ".");
                        }
                        //-----------
                    }
                }
                sender.sendMessage(RedProtect.get().lang.get("general.color") + "---------------- " + last + "/" + total + " -----------------");
                if (last < total) {
                    sender.sendMessage(RedProtect.get().lang.get("cmdmanager.region.listpage.more").replace("{player}", RedProtectUtil.UUIDtoPlayer(uuid) + " " + (Page + 1)));
                } else {
                    if (Page != 1) {
                        sender.sendMessage(RedProtect.get().lang.get("cmdmanager.region.listpage.nomore"));
                    }
                }
            }
        });
    }

    public static void handleFlag(Player p, String flag, String value, Region r) {
        if (checkCmd(flag, "help")) {
            sendFlagHelp(p);
            return;
        }

        if (r == null) {
            RedProtect.get().lang.sendMessage(p, "cmdmanager.region.todo.that");
            return;
        }

        Object objflag = RedProtectUtil.parseObject(value);

        if ((RedProtect.get().config.getDefFlags().contains(flag) || RedProtect.get().ph.hasFlagPerm(p, flag)) || flag.equalsIgnoreCase("info")) {
            if (r.isAdmin(p) || r.isLeader(p) || RedProtect.get().ph.hasPerm(p, "redprotect.command.admin.flag")) {
                if (checkCmd(flag, "info")) {
                    p.sendMessage(RedProtect.get().lang.get("general.color") + "------------[" + RedProtect.get().lang.get("cmdmanager.region.flag.values") + "]------------");
                    p.sendMessage(r.getFlagInfo());
                    p.sendMessage(RedProtect.get().lang.get("general.color") + "------------------------------------");
                    return;
                }

                if (value.equalsIgnoreCase("remove")) {
                    if (RedProtect.get().config.AdminFlags.contains(flag) && r.getFlags().containsKey(flag)) {
                        r.removeFlag(flag);
                        RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("cmdmanager.region.flag.removed").replace("{flag}", flag).replace("{region}", r.getName()));
                        RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + p.getName() + " REMOVED FLAG " + flag + " of region " + r.getName());
                        return;
                    } else {
                        RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("cmdmanager.region.flag.notset").replace("{flag}", flag));
                        return;
                    }
                }

            	/*
            	if (RedProtect.get().config.getDefFlagsValues().containsKey("clan") && !RedProtect.get().ph.hasPerm(p, "RedProtect.get().admin.flag.clan")){
            		RedProtect.get().lang.sendMessage(p,"cmdmanager.region.flag.clancommand");
            		return;
            	}
            	*/
                if (!value.equals("")) {
                    if (RedProtect.get().config.getDefFlagsValues().containsKey(flag)) {

                        //flag clan
                        if (flag.equalsIgnoreCase("clan")) {
                            if (!RedProtect.get().hooks.simpleClans || !RedProtect.get().ph.hasPerm(p, "redprotect.admin.flag.clan")) {
                                sendFlagHelp(p);
                                return;
                            }
                            if (!RedProtect.get().hooks.clanManager.isClan(value)) {
                                RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("cmdmanager.region.flag.invalidclan").replace("{tag}", value));
                                return;
                            }
                            Clan clan = RedProtect.get().hooks.clanManager.getClan(value);
                            if (!clan.isLeader(p)) {
                                RedProtect.get().lang.sendMessage(p, "cmdmanager.region.flag.clancommand");
                                return;
                            }
                            if (r.setFlag(p, flag, value)) {
                                RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("cmdmanager.region.flag.set").replace("{flag}", "'" + flag + "'") + " " + r.getFlagString(flag));
                                RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + p.getName() + " SET FLAG " + flag + " of region " + r.getName() + " to " + r.getFlagString(flag));
                            }
                            return;
                        }

                        if (objflag instanceof Boolean) {
                            if (r.setFlag(p, flag, objflag)) {
                                RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("cmdmanager.region.flag.set").replace("{flag}", "'" + flag + "'") + " " + r.getFlagBool(flag));
                                RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + p.getName() + " SET FLAG " + flag + " of region " + r.getName() + " to " + r.getFlagString(flag));
                            }
                            return;
                        } else {
                            RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("cmdmanager.region.flag.usage") + " <true/false>");
                            return;
                        }
                    }

                    if (RedProtect.get().config.AdminFlags.contains(flag)) {
                        if (!validate(flag, objflag)) {
                            SendFlagUsageMessage(p, flag);
                            return;
                        }
                        if (r.setFlag(p, flag, objflag)) {
                            RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("cmdmanager.region.flag.set").replace("{flag}", "'" + flag + "'") + " " + r.getFlagString(flag));
                            RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + p.getName() + " SET FLAG " + flag + " of region " + r.getName() + " to " + r.getFlagString(flag));
                        }
                        return;
                    }

                    sendFlagHelp(p);
                } else {

                    //flag clan
                    if (flag.equalsIgnoreCase("clan")) {
                        if (RedProtect.get().hooks.simpleClans) {
                            ClanPlayer clan = RedProtect.get().hooks.clanManager.getClanPlayer(p);
                            if (clan == null) {
                                RedProtect.get().lang.sendMessage(p, "cmdmanager.region.flag.haveclan");
                                return;
                            }
                            if (!clan.isLeader()) {
                                RedProtect.get().lang.sendMessage(p, "cmdmanager.region.flag.clancommand");
                                return;
                            }
                            if (r.getFlagString(flag).equalsIgnoreCase("")) {
                                if (r.setFlag(p, flag, clan.getTag())) {
                                    RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("cmdmanager.region.flag.setclan").replace("{clan}", "'" + clan.getClan().getColorTag() + "'"));
                                }
                            } else {
                                RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("cmdmanager.region.flag.denyclan").replace("{clan}", "'" + r.getFlagString(flag) + "'"));
                                r.setFlag(p, flag, "");
                            }
                            RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + p.getName() + " SET FLAG " + flag + " of region " + r.getName() + " to " + r.getFlagString(flag));
                            return;
                        } else {
                            sendFlagHelp(p);
                            return;
                        }
                    }

                    if (RedProtect.get().config.getDefFlagsValues().containsKey(flag)) {
                        if (r.setFlag(p, flag, !r.getFlagBool(flag))) {
                            RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("cmdmanager.region.flag.set").replace("{flag}", "'" + flag + "'") + " " + r.getFlagBool(flag));
                            RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + p.getName() + " SET FLAG " + flag + " of region " + r.getName() + " to " + r.getFlagString(flag));
                        }
                    } else {
                        if (RedProtect.get().config.AdminFlags.contains(flag)) {
                            SendFlagUsageMessage(p, flag);
                        } else {
                            RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("cmdmanager.region.flag.usage") + " <true/false>");
                        }
                        sendFlagHelp(p);
                    }
                }

            } else {
                RedProtect.get().lang.sendMessage(p, "cmdmanager.region.flag.nopermregion");
            }
        } else {
            RedProtect.get().lang.sendMessage(p, "cmdmanager.region.flag.noperm");
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
                flag.equalsIgnoreCase("cmd-onhealth")) {
            message = RedProtect.get().lang.get("cmdmanager.region.flag.usage" + flag);
        } else {
            message = RedProtect.get().lang.get("cmdmanager.region.flag.usagetruefalse").replace("{flag}", flag);
        }
        p.sendMessage(message.replace("{cmd}", getCmd("flag")));
    }

    private static void sendFlagHelp(Player p) {
        p.sendMessage(RedProtect.get().lang.get("general.color") + "-------------[RedProtect Flags]------------");
        p.sendMessage(RedProtect.get().lang.get("cmdmanager.region.flag.list") + " " + RedProtect.get().config.getDefFlags());
        p.sendMessage(RedProtect.get().lang.get("general.color") + "------------------------------------");

        StringBuilder sb = new StringBuilder();
        for (String flag : RedProtect.get().config.AdminFlags) {
            if (RedProtect.get().ph.hasFlagPerm(p, flag))
                sb.append(flag).append(", ");
        }
        if (sb.length() > 1) {
            p.sendMessage(RedProtect.get().lang.get("cmdmanager.region.flag.admlist") + " [" + sb.toString().substring(0, sb.length() - 2) + "]");
            p.sendMessage(RedProtect.get().lang.get("general.color") + "------------------------------------");
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
                flag.equalsIgnoreCase("spawn-monsters") ||
                flag.equalsIgnoreCase("spawn-animals") ||
                flag.equalsIgnoreCase("minecart") ||
                flag.equalsIgnoreCase("forcepvp") ||
                flag.equalsIgnoreCase("dynmap") ||
                flag.equalsIgnoreCase("minefarm")) && !(value instanceof Boolean)) {
            return false;
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

        if (flag.equalsIgnoreCase("setclan") && RedProtect.get().hooks.simpleClans) {
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
            Region r = RedProtect.get().rm.getRegion(valida[0], valida[1]);
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
        String[] valida = value.toString().replace(" ", "").split(",");
        if (flag.equalsIgnoreCase("deny-exit-items") || flag.equalsIgnoreCase("allow-enter-items") || flag.equalsIgnoreCase("deny-enter-items")) {
            if (!(value instanceof String)) {
                return false;
            }
            for (String item : valida) {
                if (Material.getMaterial(item.toUpperCase()) == null) {
                    return false;
                }
            }
        }
        if (flag.equalsIgnoreCase("allow-place") || flag.equalsIgnoreCase("allow-break")) {
            if (!(value instanceof String)) {
                return false;
            }
            for (String item : valida) {
                Material mat = Material.getMaterial(item.toUpperCase());
                try {
                    EntityType.valueOf(item.toUpperCase());
                } catch (Exception ex) {
                    if (mat == null)
                        return false;
                }
                if (mat == null)
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
                    int health = Integer.valueOf(arg.split(" ")[0].substring(7));
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

    public static void HandleHelpPage(CommandSender sender, int page) {
        sender.sendMessage(RedProtect.get().lang.get("_redprotect.prefix") + " " + RedProtect.get().lang.get("cmdmanager.available.cmds"));
        sender.sendMessage(RedProtect.get().lang.get("general.color") + "------------------------------------");
        sender.sendMessage(RedProtect.get().lang.get("cmdmanager.helpheader.alias"));

        if (sender instanceof Player) {
            Player player = (Player) sender;
            int i = 0;
            for (String key : RedProtect.get().lang.getHelpStrings()) {
                if (RedProtect.get().ph.hasCommandPerm(player, key) || ((key.equals("pos1") || key.equals("pos2")) && RedProtect.get().ph.hasCommandPerm(player, "redefine"))) {
                    if (key.equalsIgnoreCase("flaggui")) {
                        continue;
                    }
                    i++;

                    if (i > (page * 5) - 5 && i <= page * 5) {
                        player.sendMessage(RedProtect.get().lang.get("cmdmanager.help." + key).replace("{cmd}", getCmd(key)).replace("{alias}", getCmdAlias(key)));
                    }
                    if (i > page * 5) {
                        player.sendMessage(RedProtect.get().lang.get("general.color") + "------------------------------------");
                        player.sendMessage(RedProtect.get().lang.get("cmdmanager.page").replace("{page}", "" + (page + 1)));
                        break;
                    }
                }
            }
        } else {
            sender.sendMessage(ChatColor.GOLD + "rp " + ChatColor.RED + "admin " + ChatColor.GOLD + "kick <player> <region> <world> " + ChatColor.DARK_AQUA + "- Kick a player from a region");
            sender.sendMessage(ChatColor.GOLD + "rp " + ChatColor.RED + "admin " + ChatColor.GOLD + "clear-kicks " + ChatColor.DARK_AQUA + "- Clear all pendent kicks");
            sender.sendMessage(ChatColor.GOLD + "rp " + ChatColor.RED + "admin " + ChatColor.GOLD + "info <region> <world> " + ChatColor.DARK_AQUA + "- Info about a region");
            sender.sendMessage(ChatColor.GOLD + "rp " + ChatColor.RED + "admin " + ChatColor.GOLD + "flag <regionName> <Flag> <Value> <World> " + ChatColor.DARK_AQUA + "- Set a flag on region");
            sender.sendMessage(ChatColor.GOLD + "rp " + ChatColor.RED + "admin " + ChatColor.GOLD + "flag info <region> <world> " + ChatColor.DARK_AQUA + "- Flag info for region");
            sender.sendMessage(ChatColor.GOLD + "rp " + ChatColor.RED + "admin " + ChatColor.GOLD + "addmember <player> <region> <world> " + ChatColor.DARK_AQUA + "- Add player as member on region");
            sender.sendMessage(ChatColor.GOLD + "rp " + ChatColor.RED + "admin " + ChatColor.GOLD + "addadmin <player> <region> <world> " + ChatColor.DARK_AQUA + "- Add player as admin on region");
            sender.sendMessage(ChatColor.GOLD + "rp " + ChatColor.RED + "admin " + ChatColor.GOLD + "addleader <player> <region> <world> " + ChatColor.DARK_AQUA + "- Add player as leader on region");
            sender.sendMessage(ChatColor.GOLD + "rp " + ChatColor.RED + "admin " + ChatColor.GOLD + "removemember <player> <region> <world> " + ChatColor.DARK_AQUA + "- Remove a player as member on region");
            sender.sendMessage(ChatColor.GOLD + "rp " + ChatColor.RED + "admin " + ChatColor.GOLD + "removeadmin <player> <region> <world> " + ChatColor.DARK_AQUA + "- Remove a player as admin on region");
            sender.sendMessage(ChatColor.GOLD + "rp " + ChatColor.RED + "admin " + ChatColor.GOLD + "removeleader <player> <region> <world> " + ChatColor.DARK_AQUA + "- Remove a player as leader on region");
            sender.sendMessage(ChatColor.GOLD + "rp " + ChatColor.RED + "admin " + ChatColor.GOLD + "teleport <playerName> <regionName> <World> " + ChatColor.DARK_AQUA + "- Teleport player to a region");
            sender.sendMessage(ChatColor.GOLD + "rp " + ChatColor.RED + "admin " + ChatColor.GOLD + "blocklimit <playerName> " + ChatColor.DARK_AQUA + "- Area limit for player");
            sender.sendMessage(ChatColor.GOLD + "rp " + ChatColor.RED + "admin " + ChatColor.GOLD + "claimlimit <playerName> [world] " + ChatColor.DARK_AQUA + "- Claim limit for player");
            sender.sendMessage(ChatColor.GOLD + "rp " + ChatColor.RED + "admin " + ChatColor.GOLD + "list-areas " + ChatColor.DARK_AQUA + "- List All regions exceeding regen limit");
            sender.sendMessage(ChatColor.GOLD + "rp " + ChatColor.RED + "admin " + ChatColor.GOLD + "list-all " + ChatColor.DARK_AQUA + "- List All regions");
            sender.sendMessage(ChatColor.GOLD + "rp " + ChatColor.RED + "admin " + ChatColor.GOLD + "list <player> [page] " + ChatColor.DARK_AQUA + "- List All regions from player");
            sender.sendMessage(ChatColor.GOLD + "rp " + ChatColor.RED + "admin " + ChatColor.GOLD + "ymlTomysql " + ChatColor.DARK_AQUA + "- Convert from Yml to Mysql");
            sender.sendMessage(ChatColor.GOLD + "rp " + ChatColor.RED + "admin " + ChatColor.GOLD + "mychunktorp " + ChatColor.DARK_AQUA + "- Convert from MyChunk to RedProtect");
            sender.sendMessage(ChatColor.GOLD + "rp " + ChatColor.RED + "admin " + ChatColor.GOLD + "single-to-files " + ChatColor.DARK_AQUA + "- Convert single world files to regions files");
            sender.sendMessage(ChatColor.GOLD + "rp " + ChatColor.RED + "admin " + ChatColor.GOLD + "files-to-single " + ChatColor.DARK_AQUA + "- Convert regions files to single world files");
            sender.sendMessage(ChatColor.GOLD + "rp " + ChatColor.RED + "admin " + ChatColor.GOLD + "gpTorp " + ChatColor.DARK_AQUA + "- Convert from GriefPrevention to RedProtect");
            sender.sendMessage(ChatColor.GOLD + "rp " + ChatColor.RED + "admin " + ChatColor.GOLD + "save-all [-f]" + ChatColor.DARK_AQUA + "- Save all regions to world");
            sender.sendMessage(ChatColor.GOLD + "rp " + ChatColor.RED + "admin " + ChatColor.GOLD + "load-all " + ChatColor.DARK_AQUA + "- Load all regions from world");
            sender.sendMessage(ChatColor.GOLD + "rp " + ChatColor.RED + "admin " + ChatColor.GOLD + "reload-config " + ChatColor.DARK_AQUA + "- Reload only the config");
            sender.sendMessage(ChatColor.GOLD + "rp " + ChatColor.RED + "admin " + ChatColor.GOLD + "reload " + ChatColor.DARK_AQUA + "- Reload the plugin");
        }
        sender.sendMessage(RedProtect.get().lang.get("general.color") + "------------------------------------");
        if (RedProtect.get().ph.hasPerm(sender, "")) {
            String jarversion = new java.io.File(RedProtect.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .getPath())
                    .getName();
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&o- Full version: " + jarversion));
        }
    }

    public static String getCmd(String cmd) {
        return RedProtect.get().lang.get("cmdmanager.translation." + cmd);
    }

    public static String getCmdAlias(String cmd) {
        return RedProtect.get().lang.get("cmdmanager.translation." + cmd + ".alias");
    }

    public static boolean checkCmd(String arg, String cmd) {
        return arg.equalsIgnoreCase(getCmd(cmd)) || arg.equalsIgnoreCase(getCmdAlias(cmd)) || arg.equalsIgnoreCase(cmd);
    }
}
