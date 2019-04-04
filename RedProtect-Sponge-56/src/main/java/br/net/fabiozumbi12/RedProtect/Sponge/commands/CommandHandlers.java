/*
 *
 * Copyright (c) 2019 - @FabioZumbi12
 * Last Modified: 29/03/19 02:00
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

package br.net.fabiozumbi12.RedProtect.Sponge.commands;

import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import br.net.fabiozumbi12.RedProtect.Sponge.config.RPLang;
import br.net.fabiozumbi12.RedProtect.Sponge.events.DeleteRegionEvent;
import br.net.fabiozumbi12.RedProtect.Sponge.events.RenameRegionEvent;
import br.net.fabiozumbi12.RedProtect.Sponge.helpers.RPUtil;
import br.net.fabiozumbi12.RedProtect.Sponge.Region;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class CommandHandlers {

    // TODO Region handlers
    public static void handleAddLeader(CommandSource src, String sVictim, Region r) {
        if (src instanceof Player) {
            Player p = (Player) src;
            r = RedProtect.get().rm.getTopRegion(p.getLocation(), CommandHandlers.class.getName());
            if (r == null) {
                RPLang.sendMessage(p, "cmdmanager.region.todo.that");
                return;
            }
        }

        if (RedProtect.get().ph.hasRegionPermLeader(src, "addleader", r)) {

            Player pVictim = null;
            if (RedProtect.get().serv.getPlayer(sVictim).isPresent()) {
                pVictim = RedProtect.get().serv.getPlayer(sVictim).get();
            }

            final String VictimUUID = RPUtil.PlayerToUUID(sVictim);
            if ((pVictim == null || !pVictim.isOnline()) && !src.hasPermission("redprotect.command.admin.addleader")) {
                RPLang.sendMessage(src, RPLang.get("cmdmanager.noplayer.online").replace("{player}", sVictim));
                return;
            }

            if (!src.hasPermission("redprotect.command.admin.addleader")) {
                int claimLimit = RedProtect.get().ph.getPlayerClaimLimit(pVictim);
                int claimused = RedProtect.get().rm.getPlayerRegions(pVictim.getName(), pVictim.getWorld());
                boolean claimUnlimited = RedProtect.get().ph.hasPerm(src, "redprotect.limits.claim.unlimited");
                if (claimused >= claimLimit && claimLimit >= 0 && !claimUnlimited) {
                    RPLang.sendMessage(src, RPLang.get("cmdmanager.region.addleader.limit").replace("{player}", pVictim.getName()));
                    return;
                }
            }

            if (!r.isLeader(VictimUUID)) {
                if (src.hasPermission("redprotect.command.admin.addleader")) {
                    r.addLeader(VictimUUID);
                    RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + src.getName() + " ADDED LEADER " + RPUtil.UUIDtoPlayer(VictimUUID) + " to region " + r.getName());
                    RPLang.sendMessage(src, RPLang.get("general.color") + sVictim + " " + RPLang.get("cmdmanager.region.leader.added") + " " + r.getName());
                    if (pVictim != null && pVictim.isOnline() && !pVictim.equals(src)) {
                        RPLang.sendMessage(pVictim, RPLang.get("cmdmanager.region.leader.youadded").replace("{region}", r.getName()) + " " + src.getName());
                    }
                    return;
                }

                RPLang.sendMessage(src, RPLang.get("cmdmanager.region.leader.yousendrequest").replace("{player}", pVictim.getName()));
                RPLang.sendMessage(pVictim, RPLang.get("cmdmanager.region.leader.sendrequestto").replace("{region}", r.getName()).replace("{player}", src.getName()));

                RedProtect.get().alWait.put(pVictim, r.getID() + "@" + src.getName());
                final Player pVictimf = pVictim;
                Sponge.getScheduler().createSyncExecutor(RedProtect.get().container).schedule(() -> {
                    if (RedProtect.get().alWait.containsKey(pVictimf)) {
                        RedProtect.get().alWait.remove(pVictimf);
                        if (src instanceof Player && ((Player) src).isOnline()) {
                            RPLang.sendMessage(src, RPLang.get("cmdmanager.region.leader.requestexpired").replace("{player}", pVictimf.getName()));
                        }
                    }
                }, RedProtect.get().cfgs.root().region_settings.leadership_request_time, TimeUnit.SECONDS);
            } else {
                RPLang.sendMessage(src, "&c" + sVictim + " " + RPLang.get("cmdmanager.region.leader.already"));
            }
        } else if (src instanceof Player) {
            RPLang.sendMessage(src, "no.permission");
        }
    }

    public static void handleRemoveLeader(CommandSource src, String sVictim, Region r) {
        Region rLow = null;
        Map<Integer, Region> regions = new HashMap<>();
        if (src instanceof Player) {
            Player p = (Player) src;
            r = RedProtect.get().rm.getTopRegion(p.getLocation(), CommandHandlers.class.getName());
            rLow = RedProtect.get().rm.getLowRegion(p.getLocation());
            regions = RedProtect.get().rm.getGroupRegion(p.getLocation());
            if (r == null) {
                RPLang.sendMessage(p, "cmdmanager.region.todo.that");
                return;
            }
        }

        if (RedProtect.get().ph.hasRegionPermLeader(src, "removeleader", r)) {
            Player pVictim = null;
            if (RedProtect.get().serv.getPlayer(sVictim).isPresent()) {
                pVictim = RedProtect.get().serv.getPlayer(sVictim).get();
            }

            String VictimUUID = RPUtil.PlayerToUUID(sVictim);
            if (RPUtil.UUIDtoPlayer(VictimUUID) == null) {
                RPLang.sendMessage(src, RPLang.get("cmdmanager.noplayer.thisname").replace("{player}", sVictim));
                return;
            }

            String victname = RPUtil.UUIDtoPlayer(VictimUUID);

            if (rLow != null && rLow != r && ((!RedProtect.get().ph.hasRegionPermLeader(src, "removeleader", rLow) || (regions.size() > 1 && rLow.isLeader(VictimUUID))))) {
                RPLang.sendMessage(src, RPLang.get("cmdmanager.region.leader.cantremove.lowregion").replace("{player}", sVictim) + " " + rLow.getName());
                return;
            }
            if (r.isLeader(VictimUUID)) {
                if (r.leaderSize() > 1) {
                    RPLang.sendMessage(src, RPLang.get("general.color") + sVictim + " " + RPLang.get("cmdmanager.region.admin.added") + " " + r.getName());
                    r.removeLeader(VictimUUID);
                    RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + src.getName() + " DEMOTED TO ADMIN " + victname + " to region " + r.getName());
                    if (pVictim != null && pVictim.isOnline() && !pVictim.equals(src)) {
                        RPLang.sendMessage(pVictim, RPLang.get("cmdmanager.region.leader.youdemoted").replace("{region}", r.getName()) + " " + src.getName());
                    }
                } else {
                    RPLang.sendMessage(src, RPLang.get("cmdmanager.region.leader.cantremove").replace("{player}", sVictim));
                }
            } else {
                RPLang.sendMessage(src, "&c" + sVictim + " " + RPLang.get("cmdmanager.region.leader.notleader"));
            }
        } else if (src instanceof Player) {
            RPLang.sendMessage(src, "no.permission");
        }
    }

    public static void handleAddAdmin(CommandSource src, String sVictim, Region r) {
        if (src instanceof Player) {
            Player p = (Player) src;
            r = RedProtect.get().rm.getTopRegion(p.getLocation(), CommandHandlers.class.getName());
            if (r == null) {
                RPLang.sendMessage(p, "cmdmanager.region.todo.that");
                return;
            }
        }

        if (RedProtect.get().ph.hasRegionPermAdmin(src, "addadmin", r)) {

            Player pVictim = null;
            if (RedProtect.get().serv.getPlayer(sVictim).isPresent()) {
                pVictim = RedProtect.get().serv.getPlayer(sVictim).get();
            }

            String VictimUUID = RPUtil.PlayerToUUID(sVictim);
            if (RPUtil.UUIDtoPlayer(VictimUUID) == null) {
                RPLang.sendMessage(src, RPLang.get("cmdmanager.noplayer.thisname").replace("{player}", sVictim));
                return;
            }

            if (r.isLeader(VictimUUID)) {
                RPLang.sendMessage(src, "&c" + sVictim + " " + RPLang.get("cmdmanager.region.leader.already"));
                return;
            }

            if (!r.isAdmin(VictimUUID)) {
                r.addAdmin(VictimUUID);
                RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + src.getName() + " ADDED ADMIN " + RPUtil.UUIDtoPlayer(VictimUUID) + " to region " + r.getName());
                RPLang.sendMessage(src, RPLang.get("general.color") + sVictim + " " + RPLang.get("cmdmanager.region.admin.added") + " " + r.getName());
                if (pVictim != null && pVictim.isOnline() && !pVictim.equals(src)) {
                    RPLang.sendMessage(pVictim, RPLang.get("cmdmanager.region.admin.youadded").replace("{region}", r.getName()) + " " + src.getName());
                }
            } else {
                RPLang.sendMessage(src, "&c" + sVictim + " " + RPLang.get("cmdmanager.region.admin.already"));
            }
        } else if (src instanceof Player) {
            RPLang.sendMessage(src, "no.permission");
        }
    }

    public static void handleRemoveAdmin(CommandSource src, String sVictim, Region r) {
        if (src instanceof Player) {
            Player p = (Player) src;
            r = RedProtect.get().rm.getTopRegion(p.getLocation(), CommandHandlers.class.getName());
            if (r == null) {
                RPLang.sendMessage(p, "cmdmanager.region.todo.that");
                return;
            }
        }

        if (RedProtect.get().ph.hasRegionPermAdmin(src, "removeadmin", r)) {
            Player pVictim = null;
            if (RedProtect.get().serv.getPlayer(sVictim).isPresent()) {
                pVictim = RedProtect.get().serv.getPlayer(sVictim).get();
            }

            String VictimUUID = RPUtil.PlayerToUUID(sVictim);
            if (RPUtil.UUIDtoPlayer(VictimUUID) == null) {
                RPLang.sendMessage(src, RPLang.get("cmdmanager.noplayer.thisname").replace("{player}", sVictim));
                return;
            }

            String victname = RPUtil.UUIDtoPlayer(VictimUUID);

            if (r.isAdmin(VictimUUID)) {
                RPLang.sendMessage(src, RPLang.get("general.color") + sVictim + " " + RPLang.get("cmdmanager.region.member.added") + " " + r.getName());
                r.removeAdmin(VictimUUID);
                RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + src.getName() + " DEMOTED TO MEMBER " + victname + " to region " + r.getName());
                if (pVictim != null && pVictim.isOnline() && !pVictim.equals(src)) {
                    RPLang.sendMessage(pVictim, RPLang.get("cmdmanager.region.admin.youdemoted").replace("{region}", r.getName()) + " " + src.getName());
                }
            } else {
                RPLang.sendMessage(src, "&c" + sVictim + " " + RPLang.get("cmdmanager.region.admin.notadmin"));
            }
        } else if (src instanceof Player) {
            RPLang.sendMessage(src, "no.permission");
        }
    }

    public static void handleAddMember(CommandSource src, String sVictim, Region r) {
        if (src instanceof Player) {
            Player p = (Player) src;
            r = RedProtect.get().rm.getTopRegion(p.getLocation(), CommandHandlers.class.getName());
            if (r == null) {
                RPLang.sendMessage(p, "cmdmanager.region.todo.that");
                return;
            }
        }

        if (RedProtect.get().ph.hasRegionPermAdmin(src, "addmember", r)) {

            String VictimUUID = RPUtil.PlayerToUUID(sVictim);
            if (RPUtil.UUIDtoPlayer(VictimUUID) == null) {
                RPLang.sendMessage(src, RPLang.get("cmdmanager.noplayer.thisname").replace("{player}", sVictim));
                return;
            }

            Player pVictim = null;
            if (RedProtect.get().serv.getPlayer(sVictim).isPresent()) {
                pVictim = RedProtect.get().serv.getPlayer(sVictim).get();
            }

            if (r.isAdmin(VictimUUID)) {
                r.addMember(VictimUUID);
                RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + src.getName() + " ADDED MEMBER " + RPUtil.UUIDtoPlayer(VictimUUID) + " to region " + r.getName());
                RPLang.sendMessage(src, RPLang.get("general.color") + sVictim + " " + RPLang.get("cmdmanager.region.member.demoted") + " " + r.getName());
                if (pVictim != null && pVictim.isOnline()) {
                    RPLang.sendMessage(pVictim, RPLang.get("cmdmanager.region.admin.youdemoted").replace("{region}", r.getName()) + " " + src.getName());
                }
            } else if (!r.isMember(VictimUUID)) {
                r.addMember(VictimUUID);
                RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + src.getName() + " ADDED MEMBER " + RPUtil.UUIDtoPlayer(VictimUUID) + " to region " + r.getName());
                RPLang.sendMessage(src, RPLang.get("general.color") + sVictim + " " + RPLang.get("cmdmanager.region.member.added") + " " + r.getName());
                if (pVictim != null && pVictim.isOnline() && !pVictim.equals(src)) {
                    RPLang.sendMessage(pVictim, RPLang.get("cmdmanager.region.member.youadded").replace("{region}", r.getName()) + " " + src.getName());
                }
            } else {
                RPLang.sendMessage(src, "&c" + sVictim + " " + RPLang.get("cmdmanager.region.member.already"));
            }
        } else if (src instanceof Player) {
            RPLang.sendMessage(src, "no.permission");
        }
    }

    public static void handleRemoveMember(CommandSource src, String sVictim, Region r) {
        if (src instanceof Player) {
            Player p = (Player) src;
            r = RedProtect.get().rm.getTopRegion(p.getLocation(), CommandHandlers.class.getName());
            if (r == null) {
                RPLang.sendMessage(p, "cmdmanager.region.todo.that");
                return;
            }
        }

        if (RedProtect.get().ph.hasRegionPermAdmin(src, "removemember", r)) {
            Player pVictim = null;
            if (RedProtect.get().serv.getPlayer(sVictim).isPresent()) {
                pVictim = RedProtect.get().serv.getPlayer(sVictim).get();
            }

            String VictimUUID = RPUtil.PlayerToUUID(sVictim);
            if (RPUtil.UUIDtoPlayer(VictimUUID) == null) {
                RPLang.sendMessage(src, RPLang.get("cmdmanager.noplayer.thisname").replace("{player}", sVictim));
                return;
            }

            String victname = RPUtil.UUIDtoPlayer(VictimUUID);

            if ((r.isMember(VictimUUID) || r.isAdmin(VictimUUID)) && !r.isLeader(VictimUUID)) {
                RPLang.sendMessage(src, RPLang.get("general.color") + sVictim + " " + RPLang.get("cmdmanager.region.member.removed") + " " + r.getName());
                r.removeMember(VictimUUID);
                RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + src.getName() + " REMOVED MEMBER " + victname + " to region " + r.getName());
                if (pVictim != null && pVictim.isOnline() && !pVictim.equals(src)) {
                    RPLang.sendMessage(pVictim, RPLang.get("cmdmanager.region.member.youremoved").replace("{region}", r.getName()) + " " + src.getName());
                }
            } else {
                RPLang.sendMessage(src, "&c" + sVictim + " " + RPLang.get("cmdmanager.region.member.notmember"));
            }
        } else if (src instanceof Player) {
            RPLang.sendMessage(src, "no.permission");
        }
    }

    public static void handleDelete(Player p) {
        Region r = RedProtect.get().rm.getTopRegion(p.getLocation(), CommandHandlers.class.getName());
        if (RedProtect.get().ph.hasRegionPermLeader(p, "delete", r)) {
            if (r == null) {
                RPLang.sendMessage(p, "cmdmanager.region.todo.that");
                return;
            }

            int claims = RedProtect.get().cfgs.root().region_settings.can_delete_first_home_after_claims;
            if (!r.canDelete() && (claims == -1 || RedProtect.get().rm.getPlayerRegions(p.getName(), p.getWorld()) < claims) && !p.hasPermission("redprotect.bypass")) {
                if (claims != -1) {
                    RPLang.sendMessage(p, RPLang.get("cmdmanager.region.cantdeletefirst-claims").replace("{claims}", "" + claims));
                } else {
                    RPLang.sendMessage(p, RPLang.get("cmdmanager.region.cantdeletefirst"));
                }
                return;
            }

            DeleteRegionEvent event = new DeleteRegionEvent(r, p);
            if (Sponge.getEventManager().post(event)) {
                return;
            }

            String rname = r.getName();
            String w = r.getWorld();
            RedProtect.get().rm.remove(r, RedProtect.get().serv.getWorld(w).get());
            RPLang.sendMessage(p, RPLang.get("cmdmanager.region.deleted") + " " + rname);
            RedProtect.get().logger.addLog("(World " + w + ") Player " + p.getName() + " REMOVED region " + rname);
        } else {
            RPLang.sendMessage(p, "no.permission");
        }
    }

    public static void handleDeleteName(Player p, String rname, String world) {
        Region r = RedProtect.get().rm.getRegion(rname, p.getWorld());
        if (!world.equals("")) {
            if (Sponge.getServer().getWorld(world).isPresent()) {
                r = RedProtect.get().rm.getRegion(rname, Sponge.getServer().getWorld(world).get());
            } else {
                RPLang.sendMessage(p, "cmdmanager.region.invalidworld");
                return;
            }
        }

        if (RedProtect.get().ph.hasRegionPermLeader(p, "delete", r)) {
            if (r == null) {
                RPLang.sendMessage(p, RPLang.get("cmdmanager.region.doesntexist") + ": " + rname);
                return;
            }

            int claims = RedProtect.get().cfgs.root().region_settings.can_delete_first_home_after_claims;
            if (!r.canDelete() && (claims == -1 || RedProtect.get().rm.getPlayerRegions(p.getName(), p.getWorld()) < claims) && !p.hasPermission("redprotect.bypass")) {
                if (claims != -1) {
                    RPLang.sendMessage(p, RPLang.get("cmdmanager.region.cantdeletefirst-claims").replace("{claims}", "" + claims));
                } else {
                    RPLang.sendMessage(p, RPLang.get("cmdmanager.region.cantdeletefirst"));
                }
                return;
            }

            DeleteRegionEvent event = new DeleteRegionEvent(r, p);
            if (Sponge.getEventManager().post(event)) {
                return;
            }

            RedProtect.get().rm.remove(r, RedProtect.get().serv.getWorld(r.getWorld()).get());
            RPLang.sendMessage(p, RPLang.get("cmdmanager.region.deleted") + " " + rname);
            RedProtect.get().logger.addLog("(World " + world + ") Player " + p.getName() + " REMOVED region " + rname);
        } else {
            RPLang.sendMessage(p, "no.permission");
        }
    }

    public static void handleRename(Player p, String newName) {
        Region r = RedProtect.get().rm.getTopRegion(p.getLocation(), CommandHandlers.class.getName());
        if (RedProtect.get().ph.hasRegionPermLeader(p, "rename", r)) {
            if (r == null) {
                RPLang.sendMessage(p, "cmdmanager.region.todo.that");
                return;
            }

            //filter region name
            newName = newName.replaceAll("[^\\p{L}_0-9 ]", "");
            if (newName.isEmpty() || newName.length() < 3) {
                newName = RPUtil.nameGen(p.getName(), p.getWorld().getName());
                if (newName.length() > 16) {
                    RPLang.sendMessage(p, "cmdmanager.region.rename.invalid");
                    return;
                }
            }

            //region name conform
            if (newName.length() < 3) {
                RPLang.sendMessage(p, "regionbuilder.regionname.invalid");
                return;
            }

            if (RedProtect.get().rm.getRegion(newName, p.getWorld()) != null) {
                RPLang.sendMessage(p, "regionbuilder.regionname.existis");
                return;
            }

            RenameRegionEvent event = new RenameRegionEvent(r, newName, r.getName(), p);
            if (Sponge.getEventManager().post(event)) {
                return;
            }

            String oldname = event.getOldName();
            newName = event.getNewName();

            RedProtect.get().rm.renameRegion(newName, r);
            RPLang.sendMessage(p, RPLang.get("cmdmanager.region.rename.newname") + " " + newName);
            RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + p.getName() + " RENAMED region " + oldname + " to " + newName);
        } else {
            RPLang.sendMessage(p, "no.permission");
        }
    }

    // TODO Other Handlers
    public static void handlePrioritySingle(Player p, int prior, String region) {
        Region r = RedProtect.get().rm.getRegion(region, p.getWorld());
        if (RedProtect.get().ph.hasRegionPermLeader(p, "priority", r)) {
            if (r != null) {
                r.setPrior(prior);
                RPLang.sendMessage(p, RPLang.get("cmdmanager.region.priority.set").replace("{region}", r.getName()) + " " + prior);
                RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + p.getName() + " SET PRIORITY of region " + r.getName() + " to " + prior);
            } else {
                RPLang.sendMessage(p, "cmdmanager.region.todo.that");
            }
        }
    }

    public static void handlePriority(Player p, int prior) {
        Region r = RedProtect.get().rm.getTopRegion(p.getLocation(), CommandHandlers.class.getName());
        if (RedProtect.get().ph.hasRegionPermLeader(p, "priority", r)) {
            if (r != null) {
                r.setPrior(prior);
                RPLang.sendMessage(p, RPLang.get("cmdmanager.region.priority.set").replace("{region}", r.getName()) + " " + prior);
                RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + p.getName() + " SET PRIORITY of region " + r.getName() + " to " + prior);
            } else {
                RPLang.sendMessage(p, "cmdmanager.region.todo.that");
            }
        }
    }

    public static void handleInfoTop(Player p) {
        Region r = RedProtect.get().rm.getTopRegion(p.getLocation(), CommandHandlers.class.getName());
        if (r == null) {
            RPLang.sendMessage(p, "cmdmanager.region.todo.that");
            return;
        }
        Map<Integer, Region> groupr = RedProtect.get().rm.getGroupRegion(p.getLocation());
        if (RedProtect.get().ph.hasRegionPermAdmin(p, "info", r)) {
            p.sendMessage(RPUtil.toText(RPLang.get("general.color") + "--------------- [&e" + r.getName() + RPLang.get("general.color") + "] ---------------"));
            p.sendMessage(r.info());
            p.sendMessage(RPUtil.toText(RPLang.get("general.color") + "----------------------------------"));
            if (groupr.size() > 1) {
                p.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.moreregions")));
                for (Region regs : groupr.values()) {
                    if (regs != r) {
                        p.sendMessage(RPUtil.toText(RPLang.get("region.name") + " " + regs.getName() + " " + RPLang.get("region.priority") + " " + regs.getPrior()));
                    }
                }
            }
        } else {
            RPLang.sendMessage(p, "no.permission");
        }
    }

    public static void handleInfo(Player p, String region, String world) {
        Region r = RedProtect.get().rm.getRegion(region, p.getWorld());
        if (!world.equals("")) {
            if (Sponge.getServer().getWorld(world).isPresent()) {
                r = RedProtect.get().rm.getRegion(region, Sponge.getServer().getWorld(world).get());
            } else {
                RPLang.sendMessage(p, "cmdmanager.region.invalidworld");
                return;
            }
        }
        if (RedProtect.get().ph.hasRegionPermAdmin(p, "info", r)) {
            if (r == null) {
                RPLang.sendMessage(p, "cmdmanager.region.todo.that");
                return;
            }
            p.sendMessage(RPUtil.toText(RPLang.get("general.color") + "--------------- [&e" + r.getName() + RPLang.get("general.color") + "] ---------------"));
            p.sendMessage(r.info());
            p.sendMessage(RPUtil.toText(RPLang.get("general.color") + "----------------------------------"));
        } else {
            RPLang.sendMessage(p, "no.permission");
        }
    }

    public static void handletp(Player p, String rname, World world, Player play) {
        Region region = RedProtect.get().rm.getRegion(rname, world);
        if (region == null) {
            RPLang.sendMessage(p, RPLang.get("cmdmanager.region.doesntexist") + ": " + rname);
            return;
        }

        if (play == null) {
            if (!RedProtect.get().ph.hasRegionPermMember(p, "teleport", region)) {
                RPLang.sendMessage(p, "no.permission");
                return;
            }
        } else {
            if (!RedProtect.get().ph.hasPerm(p, "redprotect.command.admin.teleport")) {
                RPLang.sendMessage(p, "no.permission");
                return;
            }
        }

        Location<World> loc = null;
        if (region.getTPPoint() != null) {
            loc = new Location<>(world, region.getTPPoint().getBlockX() + 0.500, region.getTPPoint().getBlockY(), region.getTPPoint().getBlockZ() + 0.500);
        } else {
            int limit = world.getBlockMax().getY();
            if (world.getDimension().getType().equals(DimensionTypes.NETHER)) {
                limit = 124;
            }
            for (int i = limit; i > 0; i--) {
                BlockType mat = world.createSnapshot(region.getCenterX(), i, region.getCenterZ()).getState().getType();
                BlockType mat1 = world.createSnapshot(region.getCenterX(), i + 1, region.getCenterZ()).getState().getType();
                BlockType mat2 = world.createSnapshot(region.getCenterX(), i + 2, region.getCenterZ()).getState().getType();
                if (!mat.equals(BlockTypes.LAVA) && !mat.equals(BlockTypes.AIR) && mat1.equals(BlockTypes.AIR) && mat2.equals(BlockTypes.AIR)) {
                    loc = new Location<>(world, region.getCenterX() + 0.500, i + 1, region.getCenterZ() + 0.500);
                    break;
                }
            }
        }

        if (loc != null) {
            if (play != null) {
                play.setLocation(loc);
                RPLang.sendMessage(play, RPLang.get("cmdmanager.region.teleport") + " " + rname);
                RPLang.sendMessage(p, RPLang.get("cmdmanager.region.tpother") + " " + rname);
            } else {
                tpWait(p, loc, rname);
            }
        }
    }

    private static void tpWait(final Player p, final Location<World> loc, final String rname) {
        if (p.hasPermission("redprotect.command.admin.teleport")) {
            p.setLocation(loc);
            return;
        }
        if (!RedProtect.get().tpWait.contains(p.getName())) {
            RedProtect.get().tpWait.add(p.getName());
            RPLang.sendMessage(p, "cmdmanager.region.tpdontmove");
            Sponge.getScheduler().createSyncExecutor(RedProtect.get().container).schedule(() -> {
                if (RedProtect.get().tpWait.contains(p.getName())) {
                    RedProtect.get().tpWait.remove(p.getName());
                    p.setLocation(loc);
                    RPLang.sendMessage(p, RPLang.get("cmdmanager.region.teleport") + " " + rname);
                }
            }, RedProtect.get().cfgs.root().region_settings.teleport_time, TimeUnit.SECONDS);
        } else {
            RPLang.sendMessage(p, "cmdmanager.region.tpneedwait");
        }
    }

    public static void handleWelcome(Player p, String wMessage) {
        Region r = RedProtect.get().rm.getTopRegion(p.getLocation(), CommandHandlers.class.getName());
        if (RedProtect.get().ph.hasRegionPermAdmin(p, "welcome", r)) {
            if (r != null) {
                switch (wMessage) {
                    case "":
                        r.setWelcome("");
                        RPLang.sendMessage(p, "cmdmanager.region.welcomeoff");
                        break;
                    case "hide ":
                        r.setWelcome(wMessage);
                        RPLang.sendMessage(p, "cmdmanager.region.welcomehide");
                        break;
                    default:
                        r.setWelcome(wMessage);
                        RPLang.sendMessage(p, RPLang.get("cmdmanager.region.welcomeset") + " " + wMessage);
                        break;
                }
                RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + p.getName() + " SET WELCOME of region " + r.getName() + " to " + wMessage);
                return;
            } else {
                RPLang.sendMessage(p, "cmdmanager.region.todo.that");
                return;
            }
        }
        RPLang.sendMessage(p, "no.permission");
    }

    public static void handleList(Player p, String uuid, int Page) {
        String pname = RPUtil.PlayerToUUID(p.getName());
        if (RedProtect.get().ph.hasPerm(p, "redprotect.command.admin.list")) {
            getRegionforList(p, uuid, Page);
            return;
        } else if (RedProtect.get().ph.hasPerm(p, "redprotect.command.list") && pname.equalsIgnoreCase(uuid)) {
            getRegionforList(p, uuid, Page);
            return;
        }
        RPLang.sendMessage(p, "no.permission");
    }

    public static void getRegionforList(CommandSource p, String uuid, int nPage) {
        Sponge.getScheduler().createAsyncExecutor(RedProtect.get()).execute(()->{
            int Page = nPage;
            Set<Region> regions = RedProtect.get().rm.getRegions(uuid);
            String pname = RPUtil.UUIDtoPlayer(uuid);
            int length = regions.size();
            if (pname == null || length == 0) {
                RPLang.sendMessage(p, "cmdmanager.player.noregions");
            } else {
                p.sendMessage(RPUtil.toText(RPLang.get("general.color") + "-------------------------------------------------"));
                RPLang.sendMessage(p, RPLang.get("cmdmanager.region.created.list") + " " + pname);

                int regionsPage = RedProtect.get().cfgs.root().region_settings.region_per_page;
                int total = 0;
                int last = 0;

                for (World w : Sponge.getServer().getWorlds()) {
                    boolean first = true;

                    if (Page == 0) {
                        Page = 1;
                    }
                    int max = (regionsPage * Page);
                    int min = max - regionsPage;
                    int count;

                    String colorChar = RedProtect.get().cfgs.root().region_settings.world_colors.get(w.getName());
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
                        //-----------
                        Text.Builder worldregions = Text.builder();
                        for (int i = min; i <= max; i++){
                            count = i;
                            Region r = it.get(i);
                            String area = "(" + RPUtil.simuleTotalRegionSize(RPUtil.PlayerToUUID(uuid), r) + ")";

                            if (RedProtect.get().ph.hasRegionPermAdmin(p, "teleport", null)) {
                                if (first) {
                                    first = false;
                                    worldregions.append(Text.builder()
                                            .append(RPUtil.toText("&8" + r.getName() + area))
                                            .onHover(TextActions.showText(RPUtil.toText(RPLang.get("cmdmanager.list.hover").replace("{region}", r.getName()))))
                                            .onClick(TextActions.runCommand("/rp " + getCmd("teleport") + " " + r.getName() + " " + r.getWorld())).build());
                                } else {
                                    worldregions.append(Text.builder()
                                            .append(RPUtil.toText(RPLang.get("general.color") + ", &8" + r.getName() + area))
                                            .onHover(TextActions.showText(RPUtil.toText(RPLang.get("cmdmanager.list.hover").replace("{region}", r.getName()))))
                                            .onClick(TextActions.runCommand("/rp " + getCmd("teleport") + " " + r.getName() + " " + r.getWorld())).build());
                                }
                            } else {
                                if (first) {
                                    first = false;
                                    worldregions.append(Text.builder()
                                            .append(RPUtil.toText("&8" + r.getName() + area)).build());
                                } else {
                                    worldregions.append(Text.builder()
                                            .append(RPUtil.toText(RPLang.get("general.color") + ", &8" + r.getName() + area)).build());
                                }
                            }
                            lastLocal = count;
                        }
                        //-----------

                        last += lastLocal+1;
                        p.sendMessage(RPUtil.toText("-----"));
                        p.sendMessage(RPUtil.toText(RPLang.get("general.color") + RPLang.get("region.world").replace(":", "") + " " + colorChar + w.getName() + "[" + (min+1) + "-" + (max+1) + "/" + wregions.size() + "]&r: "));
                        p.sendMessages(worldregions.append(RPUtil.toText(RPLang.get("general.color")+".")).build());
                    }
                }
                p.sendMessage(RPUtil.toText(RPLang.get("general.color") + "---------------- " + last + "/" + total + " -----------------"));
                if (last < total) {
                    p.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.region.listpage.more").replace("{player}", pname + " " + (Page + 1))));
                } else {
                    if (Page != 1) {
                        p.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.region.listpage.nomore")));
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
            RPLang.sendMessage(p, "cmdmanager.region.todo.that");
            return;
        }

        Object objflag = RPUtil.parseObject(value);

        if ((RedProtect.get().cfgs.getDefFlags().contains(flag) || RedProtect.get().ph.hasFlagPerm(p, flag)) || flag.equalsIgnoreCase("info")) {
            if (r.isAdmin(p) || r.isLeader(p) ||  RedProtect.get().ph.hasPerm(p, "redprotect.command.admin.flag")) {
                if (checkCmd(flag, "info")) {
                    p.sendMessage(RPUtil.toText(RPLang.get("general.color") + "------------[" + RPLang.get("cmdmanager.region.flag.values") + "]------------"));
                    p.sendMessage(r.getFlagInfo());
                    p.sendMessage(RPUtil.toText(RPLang.get("general.color") + "------------------------------------"));
                    return;
                }

                if (value.equalsIgnoreCase("remove")) {
                    if (RedProtect.get().cfgs.AdminFlags.contains(flag) && r.getFlags().containsKey(flag)) {
                        r.removeFlag(flag);
                        RPLang.sendMessage(p, RPLang.get("cmdmanager.region.flag.removed").replace("{flag}", flag).replace("{region}", r.getName()));
                        RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + p.getName() + " REMOVED FLAG " + flag + " of region " + r.getName());
                        return;
                    } else {
                        RPLang.sendMessage(p, RPLang.get("cmdmanager.region.flag.notset").replace("{flag}", flag));
                        return;
                    }
                }

                if (r.flagExists("for-sale") && flag.equalsIgnoreCase("for-sale")) {
                    RPLang.sendMessage(p, "cmdmanager.eco.changeflag");
                    return;
                }

                if (!value.equals("")) {
                    if (RedProtect.get().cfgs.getDefFlagsValues().containsKey(flag)) {
                        if (objflag instanceof Boolean) {
                            if (r.setFlag(RedProtect.get().getPVHelper().getCause(p), flag, objflag)) {
                                RPLang.sendMessage(p, RPLang.get("cmdmanager.region.flag.set").replace("{flag}", "'" + flag + "'") + " " + r.getFlagBool(flag));
                                RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + p.getName() + " SET FLAG " + flag + " of region " + r.getName() + " to " + r.getFlagString(flag));
                            }
                            return;
                        } else {
                            RPLang.sendMessage(p, RPLang.get("cmdmanager.region.flag.usage") + " <true/false>");
                            return;
                        }
                    }

                    if (RedProtect.get().cfgs.AdminFlags.contains(flag)) {
                        if (!validate(flag, objflag)) {
                            SendFlagUsageMessage(p, flag);
                            return;
                        }
                        if (r.setFlag(RedProtect.get().getPVHelper().getCause(p), flag, objflag)) {
                            RPLang.sendMessage(p, RPLang.get("cmdmanager.region.flag.set").replace("{flag}", "'" + flag + "'") + " " + r.getFlagString(flag));
                            RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + p.getName() + " SET FLAG " + flag + " of region " + r.getName() + " to " + r.getFlagString(flag));
                        }
                        return;
                    }

                    sendFlagHelp(p);
                } else {
                    if (RedProtect.get().cfgs.getDefFlagsValues().containsKey(flag)) {
                        if (r.setFlag(RedProtect.get().getPVHelper().getCause(p), flag, !r.getFlagBool(flag))) {
                            RPLang.sendMessage(p, RPLang.get("cmdmanager.region.flag.set").replace("{flag}", "'" + flag + "'") + " " + r.getFlagBool(flag));
                            RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + p.getName() + " SET FLAG " + flag + " of region " + r.getName() + " to " + r.getFlagString(flag));
                        }
                    } else {
                        if (RedProtect.get().cfgs.AdminFlags.contains(flag)) {
                            SendFlagUsageMessage(p, flag);
                        } else {
                            RPLang.sendMessage(p, RPLang.get("cmdmanager.region.flag.usage") + " <true/false>");
                        }
                        sendFlagHelp(p);
                    }
                }

            } else {
                RPLang.sendMessage(p, "cmdmanager.region.flag.nopermregion");
            }
        } else {
            RPLang.sendMessage(p, "cmdmanager.region.flag.noperm");
        }
    }

    private static void SendFlagUsageMessage(Player p, String flag) {
        String message;
        if (flag.equalsIgnoreCase("effects") ||
                flag.equalsIgnoreCase("view-distance") ||
                flag.equalsIgnoreCase("deny-exit-items") ||
                flag.equalsIgnoreCase("allow-enter-items") ||
                flag.equalsIgnoreCase("deny-enter-items") ||
                flag.equalsIgnoreCase("gamemode") ||
                flag.equalsIgnoreCase("set-portal") ||
                flag.equalsIgnoreCase("allow-cmds") ||
                flag.equalsIgnoreCase("deny-cmds") ||
                flag.equalsIgnoreCase("allow-break") ||
                flag.equalsIgnoreCase("allow-place") ||
                flag.equalsIgnoreCase("particles") ||
                flag.equalsIgnoreCase("cmd-onhealth")) {
            message = RPLang.get("cmdmanager.region.flag.usage" + flag);
        } else {
            message = RPLang.get("cmdmanager.region.flag.usagetruefalse").replace("{flag}", flag);
        }
        p.sendMessage(RPUtil.toText(message.replace("{cmd}", getCmd("flag"))));
    }


    private static void sendFlagHelp(Player p) {
        p.sendMessage(RPUtil.toText(RPLang.get("general.color") + "-------------[redprotect Flags]------------"));
        p.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.region.flag.list") + " " + RedProtect.get().cfgs.getDefFlags()));
        p.sendMessage(RPUtil.toText(RPLang.get("general.color") + "------------------------------------"));

        StringBuilder sb = new StringBuilder();
        for (String flag:RedProtect.get().cfgs.AdminFlags){
            if (RedProtect.get().ph.hasFlagPerm(p, flag))
                sb.append(flag).append(", ");
        }
        if (sb.length() > 1) {
            p.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.region.flag.admlist") + " [" + sb.toString().substring(0,sb.length()-2) + "]"));
            p.sendMessage(RPUtil.toText(RPLang.get("general.color") + "------------------------------------"));
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
            if (!Sponge.getRegistry().getType(ParticleType.class, val[0]).isPresent()) {
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
            if (val.length >= 6) {
                try {
                    Double.parseDouble(val[5]);
                } catch (NumberFormatException e) {
                    return false;
                }
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
            if (!Sponge.getServer().getWorld(valida[1]).isPresent()) {
                return false;
            }
            Region r = RedProtect.get().rm.getRegion(valida[0], valida[1]);
            if (r == null) {
                return false;
            }
        }

        if (flag.equalsIgnoreCase("gamemode")) {
            if (!(value instanceof String)) {
                return false;
            }
            if (!Sponge.getRegistry().getType(GameMode.class, value.toString()).isPresent()) {
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

        String[] s = value.toString().replace(" ", "").split(",");
        if (flag.equalsIgnoreCase("deny-exit-items") || flag.equalsIgnoreCase("allow-enter-items") || flag.equalsIgnoreCase("deny-enter-items")) {
            if (!(value instanceof String)) {
                return false;
            }
            for (String item : s) {
                if (Sponge.getRegistry().getType(ItemType.class, item).isPresent()) {
                    return true;
                }
            }
            return false;
        }

        if (flag.equalsIgnoreCase("allow-place") || flag.equalsIgnoreCase("allow-break")) {
            if (!(value instanceof String)) {
                return false;
            }
            for (String item : s) {
                if (Sponge.getRegistry().getType(EntityType.class, item).isPresent() || Sponge.getRegistry().getType(ItemType.class, item).isPresent()) {
                    return true;
                }
            }
            return false;
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
                String[] cmds = value.toString().split(",");
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
                if (!Sponge.getRegistry().getType(PotionEffectType.class, effect[0]).isPresent()) {
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

    public static String getCmd(String cmd) {
        return RPLang.get("cmdmanager.translation." + cmd);
    }

    public static String getCmdAlias(String cmd) {
        return RPLang.get("cmdmanager.translation." + cmd + ".alias");
    }

    public static boolean checkCmd(String arg, String cmd) {
        return arg.equalsIgnoreCase(getCmd(cmd)) || arg.equalsIgnoreCase(getCmdAlias(cmd)) || arg.equalsIgnoreCase(cmd);
    }

    public static void HandleHelpPage(CommandSource sender, int page) {
        sender.sendMessage(RPUtil.toText(RPLang.get("_redprotect.prefix") + " " + RPLang.get("cmdmanager.available.cmds")));
        sender.sendMessage(RPUtil.toText(RPLang.get("general.color") + "------------------------------------"));
        sender.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.helpheader.alias")));

        if (sender instanceof Player) {
            Player player = (Player) sender;
            int i = 0;
            for (String key : RPLang.helpStrings()) {
                if (RedProtect.get().ph.hasCommandPerm(player, key) || ((key.equals("pos1") || key.equals("pos2")) && RedProtect.get().ph.hasCommandPerm(player, "redefine"))) {
                    if (key.equalsIgnoreCase("flaggui")) {
                        continue;
                    }
                    i++;

                    if (i > (page * 5) - 5 && i <= page * 5) {
                        player.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.help." + key).replace("{cmd}", getCmd(key)).replace("{alias}", getCmdAlias(key))));
                    }
                    if (i > page * 5) {
                        sender.sendMessage(RPUtil.toText(RPLang.get("general.color") + "------------------------------------"));
                        player.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.page").replace("{page}", "" + (page + 1))));
                        break;
                    }
                }
            }
        } else {
            sender.sendMessage(RPUtil.toText("&6rp &cadmin &6kick <player> <region> <world> &3- Kicks a player from a region"));
            sender.sendMessage(RPUtil.toText("&6rp &cadmin &6info <region> <world> &3- Info about a region"));
            sender.sendMessage(RPUtil.toText("&6rp &cadmin &6flag <regionName> <Flag> <Value> <World> &3- Set a flag on region"));
            sender.sendMessage(RPUtil.toText("&6rp &cadmin &6flag info <region> <world> &3- Flag info for region"));
            sender.sendMessage(RPUtil.toText("&6rp &cadmin &6addmember <player> <region> <world> &3- Add player as member on region"));
            sender.sendMessage(RPUtil.toText("&6rp &cadmin &6addadmin <player> <region> <world> &3- Add player as admin on region"));
            sender.sendMessage(RPUtil.toText("&6rp &cadmin &6addleader <player> <region> <world> &3- Add player as leader on region"));
            sender.sendMessage(RPUtil.toText("&6rp &cadmin &6removemember <player> <region> <world> &3- Remove a player as member on region"));
            sender.sendMessage(RPUtil.toText("&6rp &cadmin &6removeadmin <player> <region> <world> &3- Remove a player as admin on region"));
            sender.sendMessage(RPUtil.toText("&6rp &cadmin &6removeleader <player> <region> <world> &3- Remove a player as leader on region"));
            sender.sendMessage(RPUtil.toText("&6rp &cadmin &6tp <player> <regionName> <World> &3- Teleport player to a region"));
            sender.sendMessage(RPUtil.toText("&6rp &cadmin &6blocklimit <player> &3- Area limit for player"));
            sender.sendMessage(RPUtil.toText("&6rp &cadmin &6claimlimit <player> [world] &3- Claim limit for player"));
            sender.sendMessage(RPUtil.toText("&6rp &cadmin &6list-all &3- List All regions"));
            sender.sendMessage(RPUtil.toText("&6rp &cadmin &6list <player> &3- List All player regions"));
            sender.sendMessage(RPUtil.toText("&6rp &cadmin &6list-areas &3- List All area exceeding regen limit"));
            sender.sendMessage(RPUtil.toText("&6rp &cadmin &6single-to-files &3- Convert single world files to regions files"));
            sender.sendMessage(RPUtil.toText("&6rp &cadmin &6files-to-single &3- Convert regions files to single world files"));
            sender.sendMessage(RPUtil.toText("&6rp &cadmin &6fileTomysql &3- Convert from File to Mysql"));
            sender.sendMessage(RPUtil.toText("&6rp &cadmin &6mysqlToFile &3- Convert from Mysql to File"));
            sender.sendMessage(RPUtil.toText("&6rp &cadmin &6save-all [-f] &3- Save all regions to database"));
            sender.sendMessage(RPUtil.toText("&6rp &cadmin &6load-all &3- Load all regions from database"));
            sender.sendMessage(RPUtil.toText("&6rp &cadmin &6reload-config &3- Reload only the config"));
            sender.sendMessage(RPUtil.toText("&6rp &cadmin &6reload &3- Reload the plugin"));
        }
        sender.sendMessage(RPUtil.toText(RPLang.get("general.color") + "------------------------------------"));
        if (RedProtect.get().ph.hasPerm(sender, "")) {
            String jarversion = RedProtect.get().container.getSource().get().toFile().getName();
            sender.sendMessage(RPUtil.toText("&8&o- Full version: " + jarversion));
        }
    }
}
