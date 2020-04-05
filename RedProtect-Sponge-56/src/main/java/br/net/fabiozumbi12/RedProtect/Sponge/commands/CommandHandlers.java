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

package br.net.fabiozumbi12.RedProtect.Sponge.commands;

import br.net.fabiozumbi12.RedProtect.Core.helpers.Replacer;
import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import br.net.fabiozumbi12.RedProtect.Sponge.Region;
import br.net.fabiozumbi12.RedProtect.Sponge.events.DeleteRegionEvent;
import br.net.fabiozumbi12.RedProtect.Sponge.events.RenameRegionEvent;
import br.net.fabiozumbi12.RedProtect.Sponge.helpers.ItemFlagGui;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CommandHandlers {

    // TODO Region handlers
    public static void handleAddLeader(CommandSource src, String sVictim, Region r) {
        if (src instanceof Player) {
            Player p = (Player) src;
            r = RedProtect.get().rm.getTopRegion(p.getLocation(), CommandHandlers.class.getName());
            if (r == null) {
                RedProtect.get().lang.sendMessage(p, "cmdmanager.region.todo.that");
                return;
            }
        }

        if (RedProtect.get().ph.hasRegionPermLeader(src, "addleader", r)) {

            Player pVictim = null;
            if (RedProtect.get().getServer().getPlayer(sVictim).isPresent()) {
                pVictim = RedProtect.get().getServer().getPlayer(sVictim).get();
            }

            if ((pVictim == null || !pVictim.isOnline()) && !src.hasPermission("redprotect.command.admin.addleader")) {
                RedProtect.get().lang.sendMessage(src, RedProtect.get().lang.get("cmdmanager.noplayer.online").replace("{player}", sVictim));
                return;
            }

            if (!src.hasPermission("redprotect.command.admin.addleader")) {
                int claimLimit = RedProtect.get().ph.getPlayerClaimLimit(pVictim);
                int claimused = RedProtect.get().rm.getPlayerRegions(pVictim.getName(), pVictim.getWorld().getName());
                boolean claimUnlimited = RedProtect.get().ph.hasPerm(src, "redprotect.limits.claim.unlimited");
                if (claimused >= claimLimit && claimLimit >= 0 && !claimUnlimited) {
                    RedProtect.get().lang.sendMessage(src, RedProtect.get().lang.get("cmdmanager.region.addleader.limit").replace("{player}", pVictim.getName()));
                    return;
                }
            }

            if (!r.isLeader(sVictim)) {
                if (src.hasPermission("redprotect.command.admin.addleader")) {
                    r.addLeader(sVictim);
                    RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + src.getName() + " ADDED LEADER " + sVictim + " to region " + r.getName());
                    RedProtect.get().lang.sendMessage(src, RedProtect.get().lang.get("general.color") + sVictim + " " + RedProtect.get().lang.get("cmdmanager.region.leader.added") + " " + r.getName());
                    if (pVictim != null && pVictim.isOnline() && !pVictim.equals(src)) {
                        RedProtect.get().lang.sendMessage(pVictim, RedProtect.get().lang.get("cmdmanager.region.leader.youadded").replace("{region}", r.getName()) + " " + src.getName());
                    }
                    return;
                }

                RedProtect.get().lang.sendMessage(src, RedProtect.get().lang.get("cmdmanager.region.leader.yousendrequest").replace("{player}", pVictim.getName()));
                RedProtect.get().lang.sendMessage(pVictim, RedProtect.get().lang.get("cmdmanager.region.leader.sendrequestto").replace("{region}", r.getName()).replace("{player}", src.getName()));

                RedProtect.get().alWait.put(pVictim, r.getID() + "@" + src.getName());
                final Player pVictimf = pVictim;
                Sponge.getScheduler().createSyncExecutor(RedProtect.get().container).schedule(() -> {
                    if (RedProtect.get().alWait.containsKey(pVictimf)) {
                        RedProtect.get().alWait.remove(pVictimf);
                        if (src instanceof Player && ((Player) src).isOnline()) {
                            RedProtect.get().lang.sendMessage(src, RedProtect.get().lang.get("cmdmanager.region.leader.requestexpired").replace("{player}", pVictimf.getName()));
                        }
                    }
                }, RedProtect.get().config.configRoot().region_settings.leadership_request_time, TimeUnit.SECONDS);
            } else {
                RedProtect.get().lang.sendMessage(src, "&c" + sVictim + " " + RedProtect.get().lang.get("cmdmanager.region.leader.already"));
            }
        } else if (src instanceof Player) {
            RedProtect.get().lang.sendMessage(src, "no.permission");
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
                RedProtect.get().lang.sendMessage(p, "cmdmanager.region.todo.that");
                return;
            }
        }

        if (RedProtect.get().ph.hasRegionPermLeader(src, "removeleader", r)) {
            Player pVictim = null;
            if (RedProtect.get().getServer().getPlayer(sVictim).isPresent()) {
                pVictim = RedProtect.get().getServer().getPlayer(sVictim).get();
            }

            if (RedProtect.get().getUtil().PlayerToUUID(sVictim) == null) {
                RedProtect.get().lang.sendMessage(src, RedProtect.get().lang.get("cmdmanager.noplayer.thisname").replace("{player}", sVictim));
                return;
            }

            if (rLow != null && rLow != r && ((!RedProtect.get().ph.hasRegionPermLeader(src, "removeleader", rLow) || (regions.size() > 1 && rLow.isLeader(sVictim))))) {
                RedProtect.get().lang.sendMessage(src, RedProtect.get().lang.get("cmdmanager.region.leader.cantremove.lowregion").replace("{player}", sVictim) + " " + rLow.getName());
                return;
            }
            if (r.isLeader(sVictim)) {
                if (r.leaderSize() > 1) {
                    RedProtect.get().lang.sendMessage(src, RedProtect.get().lang.get("general.color") + sVictim + " " + RedProtect.get().lang.get("cmdmanager.region.admin.added") + " " + r.getName());
                    r.removeLeader(sVictim);
                    RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + src.getName() + " DEMOTED TO ADMIN " + sVictim + " to region " + r.getName());
                    if (pVictim != null && pVictim.isOnline() && !pVictim.equals(src)) {
                        RedProtect.get().lang.sendMessage(pVictim, RedProtect.get().lang.get("cmdmanager.region.leader.youdemoted").replace("{region}", r.getName()) + " " + src.getName());
                    }
                } else {
                    RedProtect.get().lang.sendMessage(src, RedProtect.get().lang.get("cmdmanager.region.leader.cantremove").replace("{player}", sVictim));
                }
            } else {
                RedProtect.get().lang.sendMessage(src, "&c" + sVictim + " " + RedProtect.get().lang.get("cmdmanager.region.leader.notleader"));
            }
        } else if (src instanceof Player) {
            RedProtect.get().lang.sendMessage(src, "no.permission");
        }
    }

    public static void handleAddAdmin(CommandSource src, String sVictim, Region r) {
        if (src instanceof Player) {
            Player p = (Player) src;
            r = RedProtect.get().rm.getTopRegion(p.getLocation(), CommandHandlers.class.getName());
            if (r == null) {
                RedProtect.get().lang.sendMessage(p, "cmdmanager.region.todo.that");
                return;
            }
        }

        if (RedProtect.get().ph.hasRegionPermAdmin(src, "addadmin", r)) {

            Player pVictim = null;
            if (RedProtect.get().getServer().getPlayer(sVictim).isPresent()) {
                pVictim = RedProtect.get().getServer().getPlayer(sVictim).get();
            }

            if (RedProtect.get().getUtil().PlayerToUUID(sVictim) == null) {
                RedProtect.get().lang.sendMessage(src, RedProtect.get().lang.get("cmdmanager.noplayer.thisname").replace("{player}", sVictim));
                return;
            }

            if (r.isLeader(sVictim)) {
                RedProtect.get().lang.sendMessage(src, "&c" + sVictim + " " + RedProtect.get().lang.get("cmdmanager.region.leader.already"));
                return;
            }

            if (!r.isAdmin(sVictim)) {
                r.addAdmin(sVictim);
                RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + src.getName() + " ADDED ADMIN " + sVictim + " to region " + r.getName());
                RedProtect.get().lang.sendMessage(src, RedProtect.get().lang.get("general.color") + sVictim + " " + RedProtect.get().lang.get("cmdmanager.region.admin.added") + " " + r.getName());
                if (pVictim != null && pVictim.isOnline() && !pVictim.equals(src)) {
                    RedProtect.get().lang.sendMessage(pVictim, RedProtect.get().lang.get("cmdmanager.region.admin.youadded").replace("{region}", r.getName()) + " " + src.getName());
                }
            } else {
                RedProtect.get().lang.sendMessage(src, "&c" + sVictim + " " + RedProtect.get().lang.get("cmdmanager.region.admin.already"));
            }
        } else if (src instanceof Player) {
            RedProtect.get().lang.sendMessage(src, "no.permission");
        }
    }

    public static void handleRemoveAdmin(CommandSource src, String sVictim, Region r) {
        if (src instanceof Player) {
            Player p = (Player) src;
            r = RedProtect.get().rm.getTopRegion(p.getLocation(), CommandHandlers.class.getName());
            if (r == null) {
                RedProtect.get().lang.sendMessage(p, "cmdmanager.region.todo.that");
                return;
            }
        }

        if (RedProtect.get().ph.hasRegionPermAdmin(src, "removeadmin", r)) {
            Player pVictim = null;
            if (RedProtect.get().getServer().getPlayer(sVictim).isPresent()) {
                pVictim = RedProtect.get().getServer().getPlayer(sVictim).get();
            }

            if (RedProtect.get().getUtil().PlayerToUUID(sVictim) == null) {
                RedProtect.get().lang.sendMessage(src, RedProtect.get().lang.get("cmdmanager.noplayer.thisname").replace("{player}", sVictim));
                return;
            }

            if (r.isAdmin(sVictim)) {
                RedProtect.get().lang.sendMessage(src, RedProtect.get().lang.get("general.color") + sVictim + " " + RedProtect.get().lang.get("cmdmanager.region.member.added") + " " + r.getName());
                r.removeAdmin(sVictim);
                RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + src.getName() + " DEMOTED TO MEMBER " + sVictim + " to region " + r.getName());
                if (pVictim != null && pVictim.isOnline() && !pVictim.equals(src)) {
                    RedProtect.get().lang.sendMessage(pVictim, RedProtect.get().lang.get("cmdmanager.region.admin.youdemoted").replace("{region}", r.getName()) + " " + src.getName());
                }
            } else {
                RedProtect.get().lang.sendMessage(src, "&c" + sVictim + " " + RedProtect.get().lang.get("cmdmanager.region.admin.notadmin"));
            }
        } else if (src instanceof Player) {
            RedProtect.get().lang.sendMessage(src, "no.permission");
        }
    }

    public static void handleAddMember(CommandSource src, String sVictim, Region r) {
        if (src instanceof Player) {
            Player p = (Player) src;
            r = RedProtect.get().rm.getTopRegion(p.getLocation(), CommandHandlers.class.getName());
            if (r == null) {
                RedProtect.get().lang.sendMessage(p, "cmdmanager.region.todo.that");
                return;
            }
        }

        if (RedProtect.get().ph.hasRegionPermAdmin(src, "addmember", r)) {

            if (RedProtect.get().getUtil().PlayerToUUID(sVictim) == null) {
                RedProtect.get().lang.sendMessage(src, RedProtect.get().lang.get("cmdmanager.noplayer.thisname").replace("{player}", sVictim));
                return;
            }

            Player pVictim = null;
            if (RedProtect.get().getServer().getPlayer(sVictim).isPresent()) {
                pVictim = RedProtect.get().getServer().getPlayer(sVictim).get();
            }

            if (r.isAdmin(sVictim)) {
                r.addMember(sVictim);
                RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + src.getName() + " ADDED MEMBER " + sVictim + " to region " + r.getName());
                RedProtect.get().lang.sendMessage(src, RedProtect.get().lang.get("general.color") + sVictim + " " + RedProtect.get().lang.get("cmdmanager.region.member.demoted") + " " + r.getName());
                if (pVictim != null && pVictim.isOnline()) {
                    RedProtect.get().lang.sendMessage(pVictim, RedProtect.get().lang.get("cmdmanager.region.admin.youdemoted").replace("{region}", r.getName()) + " " + src.getName());
                }
            } else if (!r.isMember(sVictim)) {
                r.addMember(sVictim);
                RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + src.getName() + " ADDED MEMBER " + sVictim + " to region " + r.getName());
                RedProtect.get().lang.sendMessage(src, RedProtect.get().lang.get("general.color") + sVictim + " " + RedProtect.get().lang.get("cmdmanager.region.member.added") + " " + r.getName());
                if (pVictim != null && pVictim.isOnline() && !pVictim.equals(src)) {
                    RedProtect.get().lang.sendMessage(pVictim, RedProtect.get().lang.get("cmdmanager.region.member.youadded").replace("{region}", r.getName()) + " " + src.getName());
                }
            } else {
                RedProtect.get().lang.sendMessage(src, "&c" + sVictim + " " + RedProtect.get().lang.get("cmdmanager.region.member.already"));
            }
        } else if (src instanceof Player) {
            RedProtect.get().lang.sendMessage(src, "no.permission");
        }
    }

    public static void handleRemoveMember(CommandSource src, String sVictim, Region r) {
        if (src instanceof Player) {
            Player p = (Player) src;
            r = RedProtect.get().rm.getTopRegion(p.getLocation(), CommandHandlers.class.getName());
            if (r == null) {
                RedProtect.get().lang.sendMessage(p, "cmdmanager.region.todo.that");
                return;
            }
        }

        if (RedProtect.get().ph.hasRegionPermAdmin(src, "removemember", r)) {
            Player pVictim = null;
            if (RedProtect.get().getServer().getPlayer(sVictim).isPresent()) {
                pVictim = RedProtect.get().getServer().getPlayer(sVictim).get();
            }

            if (RedProtect.get().getUtil().PlayerToUUID(sVictim) == null) {
                RedProtect.get().lang.sendMessage(src, RedProtect.get().lang.get("cmdmanager.noplayer.thisname").replace("{player}", sVictim));
                return;
            }

            if ((r.isMember(sVictim) || r.isAdmin(sVictim)) && !r.isLeader(sVictim)) {
                RedProtect.get().lang.sendMessage(src, RedProtect.get().lang.get("general.color") + sVictim + " " + RedProtect.get().lang.get("cmdmanager.region.member.removed") + " " + r.getName());
                r.removeMember(sVictim);
                RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + src.getName() + " REMOVED MEMBER " + sVictim + " to region " + r.getName());
                if (pVictim != null && pVictim.isOnline() && !pVictim.equals(src)) {
                    RedProtect.get().lang.sendMessage(pVictim, RedProtect.get().lang.get("cmdmanager.region.member.youremoved").replace("{region}", r.getName()) + " " + src.getName());
                }
            } else {
                RedProtect.get().lang.sendMessage(src, "&c" + sVictim + " " + RedProtect.get().lang.get("cmdmanager.region.member.notmember"));
            }
        } else if (src instanceof Player) {
            RedProtect.get().lang.sendMessage(src, "no.permission");
        }
    }

    public static void handleDelete(Player p) {
        Region r = RedProtect.get().rm.getTopRegion(p.getLocation(), CommandHandlers.class.getName());
        if (RedProtect.get().ph.hasRegionPermLeader(p, "delete", r)) {
            if (r == null) {
                RedProtect.get().lang.sendMessage(p, "cmdmanager.region.todo.that");
                return;
            }

            int claims = RedProtect.get().config.configRoot().region_settings.can_delete_first_home_after_claims;
            if (!r.canDelete() && (claims == -1 || RedProtect.get().rm.getPlayerRegions(p.getName(), p.getWorld().getName()) < claims) && !p.hasPermission("redprotect.bypass")) {
                if (claims != -1) {
                    RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("cmdmanager.region.cantdeletefirst-claims").replace("{claims}", "" + claims));
                } else {
                    RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("cmdmanager.region.cantdeletefirst"));
                }
                return;
            }

            DeleteRegionEvent event = new DeleteRegionEvent(r, p);
            if (Sponge.getEventManager().post(event)) {
                return;
            }

            String rname = r.getName();
            String w = r.getWorld();
            RedProtect.get().rm.remove(r, w);
            RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("cmdmanager.region.deleted") + " " + rname);
            RedProtect.get().logger.addLog("(World " + w + ") Player " + p.getName() + " REMOVED region " + rname);

            // Handle money
            if (RedProtect.get().config.ecoRoot().claim_cost_per_block.enable && !p.hasPermission("redprotect.eco.bypass")) {
                UniqueAccount acc = RedProtect.get().economy.getOrCreateAccount(p.getUniqueId()).get();
                long reco = r.getArea() * RedProtect.get().config.ecoRoot().claim_cost_per_block.cost_per_block;

                if (!RedProtect.get().config.ecoRoot().claim_cost_per_block.y_is_free) {
                    reco = reco * Math.abs(r.getMaxY() - r.getMinY());
                }

                if (reco > 0) {
                    acc.deposit(RedProtect.get().economy.getDefaultCurrency(), BigDecimal.valueOf(reco), RedProtect.get().getVersionHelper().getCause(p));
                    p.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("economy.region.deleted").replace("{price}", RedProtect.get().config.ecoRoot().economy_symbol + reco + " " + RedProtect.get().config.ecoRoot().economy_name)));
                }
            }
        } else {
            RedProtect.get().lang.sendMessage(p, "no.permission");
        }
    }

    public static void handleDeleteName(Player p, String rname, String world) {
        Region r = RedProtect.get().rm.getRegion(rname, p.getWorld().getName());
        if (!world.equals("")) {
            if (Sponge.getServer().getWorld(world).isPresent()) {
                r = RedProtect.get().rm.getRegion(rname, world);
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
            if (!r.canDelete() && (claims == -1 || RedProtect.get().rm.getPlayerRegions(p.getName(), p.getWorld().getName()) < claims) && !p.hasPermission("redprotect.bypass")) {
                if (claims != -1) {
                    RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("cmdmanager.region.cantdeletefirst-claims").replace("{claims}", "" + claims));
                } else {
                    RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("cmdmanager.region.cantdeletefirst"));
                }
                return;
            }

            DeleteRegionEvent event = new DeleteRegionEvent(r, p);
            if (Sponge.getEventManager().post(event)) {
                return;
            }

            RedProtect.get().rm.remove(r, r.getWorld());
            RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("cmdmanager.region.deleted") + " " + rname);
            RedProtect.get().logger.addLog("(World " + world + ") Player " + p.getName() + " REMOVED region " + rname);
        } else {
            RedProtect.get().lang.sendMessage(p, "no.permission");
        }
    }

    public static void handleRename(Player p, String newName) {
        Region r = RedProtect.get().rm.getTopRegion(p.getLocation(), CommandHandlers.class.getName());
        if (RedProtect.get().ph.hasRegionPermLeader(p, "rename", r)) {
            if (r == null) {
                RedProtect.get().lang.sendMessage(p, "cmdmanager.region.todo.that");
                return;
            }

            //filter name
            newName = RedProtect.get().getUtil().fixRegionName(p, newName);

            RenameRegionEvent event = new RenameRegionEvent(r, newName, r.getName(), p);
            if (Sponge.getEventManager().post(event)) {
                return;
            }

            String oldName = event.getOldName();
            newName = event.getNewName();

            Region newRegion = RedProtect.get().rm.renameRegion(newName, r);
            RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("cmdmanager.region.rename.newname") + " " + newRegion.getName());
            RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + p.getName() + " RENAMED region " + oldName + " to " + newRegion.getName());
        } else {
            RedProtect.get().lang.sendMessage(p, "no.permission");
        }
    }

    // TODO Other Handlers
    public static void handlePrioritySingle(Player p, int prior, String region) {
        Region r = RedProtect.get().rm.getRegion(region, p.getWorld().getName());
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
        Region r = RedProtect.get().rm.getTopRegion(p.getLocation(), CommandHandlers.class.getName());
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
        Region r = RedProtect.get().rm.getTopRegion(p.getLocation(), CommandHandlers.class.getName());
        if (r == null) {
            RedProtect.get().lang.sendMessage(p, "cmdmanager.region.todo.that");
            return;
        }
        Map<Integer, Region> groupr = RedProtect.get().rm.getGroupRegion(p.getLocation());
        if (RedProtect.get().ph.hasRegionPermAdmin(p, "info", r)) {
            p.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("general.color") + "--------------- [&e" + r.getName() + RedProtect.get().lang.get("general.color") + "] ---------------"));
            p.sendMessage(r.info());
            p.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("general.color") + "----------------------------------"));
            if (groupr.size() > 1) {
                p.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("cmdmanager.moreregions")));
                for (Region regs : groupr.values()) {
                    if (regs != r) {
                        p.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("region.name") + " " + regs.getName() + " " + RedProtect.get().lang.get("region.priority") + " " + regs.getPrior()));
                    }
                }
            }
        } else {
            RedProtect.get().lang.sendMessage(p, "no.permission");
        }
    }

    public static void handleInfo(Player p, String region, String world) {
        Region r = RedProtect.get().rm.getRegion(region, p.getWorld().getName());
        if (!world.equals("")) {
            if (Sponge.getServer().getWorld(world).isPresent()) {
                r = RedProtect.get().rm.getRegion(region, world);
            } else {
                RedProtect.get().lang.sendMessage(p, "cmdmanager.region.invalidworld");
                return;
            }
        }
        if (RedProtect.get().ph.hasRegionPermAdmin(p, "info", r)) {
            if (r == null) {
                RedProtect.get().lang.sendMessage(p, "cmdmanager.region.doesntexist");
                return;
            }
            p.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("general.color") + "--------------- [&e" + r.getName() + RedProtect.get().lang.get("general.color") + "] ---------------"));
            p.sendMessage(r.info());
            p.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("general.color") + "----------------------------------"));
        } else {
            RedProtect.get().lang.sendMessage(p, "no.permission");
        }
    }

    public static void handletp(CommandSource source, String rname, World world, Player play) {
        Region region = RedProtect.get().rm.getRegion(rname, world.getName());
        if (region == null) {
            RedProtect.get().lang.sendMessage(source, RedProtect.get().lang.get("cmdmanager.region.doesntexist") + ": " + rname);
            return;
        }

        if (play == null) {
            if (source instanceof Player && !RedProtect.get().ph.hasRegionPermMember((Player) source, "teleport", region)) {
                RedProtect.get().lang.sendMessage(source, "no.permission");
                return;
            }
        } else {
            if (!RedProtect.get().ph.hasPerm(source, "redprotect.command.admin.teleport")) {
                RedProtect.get().lang.sendMessage(source, "no.permission");
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
                RedProtect.get().lang.sendMessage(play, RedProtect.get().lang.get("cmdmanager.region.teleport") + " " + rname);
                RedProtect.get().lang.sendMessage(source, RedProtect.get().lang.get("cmdmanager.region.tpother") + " " + rname);
            } else if (source instanceof Player) {
                tpWait((Player) source, loc, rname);
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
            RedProtect.get().lang.sendMessage(p, "cmdmanager.region.tpdontmove");
            Sponge.getScheduler().createSyncExecutor(RedProtect.get().container).schedule(() -> {
                if (RedProtect.get().tpWait.contains(p.getName())) {
                    RedProtect.get().tpWait.remove(p.getName());
                    p.setLocation(loc);
                    RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("cmdmanager.region.teleport") + " " + rname);
                }
            }, RedProtect.get().config.configRoot().region_settings.teleport_time, TimeUnit.SECONDS);
        } else {
            RedProtect.get().lang.sendMessage(p, "cmdmanager.region.tpneedwait");
        }
    }

    public static void handleWelcome(Player p, String wMessage) {
        Region r = RedProtect.get().rm.getTopRegion(p.getLocation(), CommandHandlers.class.getName());
        if (RedProtect.get().ph.hasRegionPermAdmin(p, "welcome", r)) {
            if (r != null) {
                switch (wMessage) {
                    case "":
                        RedProtect.get().lang.sendMessage(p, "cmdmanager.region.welcomeoff");
                        break;
                    case "hide ":
                        RedProtect.get().lang.sendMessage(p, "cmdmanager.region.welcomehide");
                        break;
                    default:
                        RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("cmdmanager.region.welcomeset") + " " + wMessage);
                        break;
                }
                r.setWelcome(wMessage);
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
        } else if (RedProtect.get().ph.hasPerm(p, "redprotect.command.list") && RedProtect.get().getUtil().PlayerToUUID(p.getName()).equalsIgnoreCase(uuid)) {
            getRegionforList(p, uuid, Page);
        } else {
            RedProtect.get().lang.sendMessage(p, "no.permission");
        }
    }

    public static void getRegionforList(CommandSource p, String uuid, int nPage) {
        Sponge.getScheduler().createAsyncExecutor(RedProtect.get()).execute(() -> {
            int Page = nPage;
            Set<Region> regions = RedProtect.get().rm.getLeaderRegions(uuid);
            String pname = RedProtect.get().getUtil().UUIDtoPlayer(uuid);
            int length = regions.size();
            if (pname == null || length == 0) {
                RedProtect.get().lang.sendMessage(p, "cmdmanager.player.noregions");
            } else {
                p.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("general.color") + "-------------------------------------------------"));
                RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("cmdmanager.region.created.list") + " " + pname);

                int regionsPage = RedProtect.get().config.configRoot().region_settings.region_list.region_per_page;
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

                    String colorChar = RedProtect.get().config.configRoot().region_settings.world_colors.get(w.getName());
                    Set<Region> wregions = RedProtect.get().rm.getRegions(uuid, w.getName());
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
                        //-----------
                        Text.Builder worldregions = Text.builder();
                        for (int i = min; i <= max; i++) {
                            count = i;
                            Region r = it.get(i);
                            String area = RedProtect.get().config.configRoot().region_settings.region_list.shpw_area ? "(" + RedProtect.get().getUtil().simuleTotalRegionSize(RedProtect.get().getUtil().PlayerToUUID(uuid), r) + ")" : "";

                            if (RedProtect.get().ph.hasRegionPermAdmin(p, "teleport", null)) {
                                if (first) {
                                    first = false;
                                    worldregions.append(Text.builder()
                                            .append(RedProtect.get().getUtil().toText("&8" + r.getName() + area))
                                            .onHover(TextActions.showText(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("cmdmanager.list.hover").replace("{region}", r.getName()))))
                                            .onClick(TextActions.runCommand("/rp " + getCmd("teleport") + " " + r.getName() + " " + r.getWorld())).build());
                                } else {
                                    worldregions.append(Text.builder()
                                            .append(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("general.color") + ", &8" + r.getName() + area))
                                            .onHover(TextActions.showText(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("cmdmanager.list.hover").replace("{region}", r.getName()))))
                                            .onClick(TextActions.runCommand("/rp " + getCmd("teleport") + " " + r.getName() + " " + r.getWorld())).build());
                                }
                            } else {
                                if (first) {
                                    first = false;
                                    worldregions.append(Text.builder()
                                            .append(RedProtect.get().getUtil().toText("&8" + r.getName() + area)).build());
                                } else {
                                    worldregions.append(Text.builder()
                                            .append(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("general.color") + ", &8" + r.getName() + area)).build());
                                }
                            }
                            lastLocal = count;
                        }
                        //-----------

                        last += lastLocal + 1;
                        p.sendMessage(RedProtect.get().getUtil().toText("-----"));
                        p.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("general.color") + RedProtect.get().lang.get("region.world").replace(":", "") + " " + colorChar + w.getName() + "[" + (min + 1) + "-" + (max + 1) + "/" + wregions.size() + "]&r: "));
                        p.sendMessages(worldregions.append(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("general.color") + ".")).build());
                    }
                }
                p.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("general.color") + "---------------- " + last + "/" + total + " -----------------"));
                if (last < total) {
                    p.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("cmdmanager.region.listpage.more").replace("{player}", pname + " " + (Page + 1))));
                } else {
                    if (Page != 1) {
                        p.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("cmdmanager.region.listpage.nomore")));
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

        Object objflag = RedProtect.get().getUtil().parseObject(value);

        if ((RedProtect.get().ph.hasFlagPerm(p, flag) && (RedProtect.get().config.configRoot().flags.containsKey(flag) || RedProtect.get().config.AdminFlags.contains(flag))) || flag.equalsIgnoreCase("info")) {
            if (r.isAdmin(p) || r.isLeader(p) || RedProtect.get().ph.hasPerm(p, "redprotect.command.admin.flag")) {
                if (checkCmd(flag, "info")) {
                    p.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("general.color") + "------------[" + RedProtect.get().lang.get("cmdmanager.region.flag.values") + "]------------"));
                    p.sendMessage(r.getFlagInfo());
                    p.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("general.color") + "------------------------------------"));
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

                if (!value.equals("")) {
                    if (RedProtect.get().config.getDefFlagsValues().containsKey(flag)) {
                        if (objflag instanceof Boolean) {
                            if (r.setFlag(RedProtect.get().getVersionHelper().getCause(p), flag, objflag)) {
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
                        if (r.setFlag(RedProtect.get().getVersionHelper().getCause(p), flag, objflag)) {
                            RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("cmdmanager.region.flag.set").replace("{flag}", "'" + flag + "'") + " " + r.getFlagString(flag));
                            RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + p.getName() + " SET FLAG " + flag + " of region " + r.getName() + " to " + r.getFlagString(flag));
                        }
                        return;
                    }

                    sendFlagHelp(p);
                } else {

                    // Item flags
                    if (flag.equalsIgnoreCase("deny-enter-items") ||
                            flag.equalsIgnoreCase("allow-enter-items") ||
                            flag.equalsIgnoreCase("allow-place") ||
                            flag.equalsIgnoreCase("allow-break")) {

                        ItemFlagGui itemGui = new ItemFlagGui(p, r, flag);
                        itemGui.open();
                        return;
                    }

                    if (RedProtect.get().config.getDefFlagsValues().containsKey(flag)) {
                        if (r.setFlag(RedProtect.get().getVersionHelper().getCause(p), flag, !r.getFlagBool(flag))) {
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
            message = RedProtect.get().lang.get("cmdmanager.region.flag.usage" + flag);
        } else {
            message = RedProtect.get().lang.get("cmdmanager.region.flag.usagetruefalse").replace("{flag}", flag);
        }
        p.sendMessage(RedProtect.get().getUtil().toText(message.replace("{cmd}", getCmd("flag"))));
    }


    private static void sendFlagHelp(Player p) {
        p.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("general.color") + "-------------[redprotect Flags]------------"));
        p.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("cmdmanager.region.flag.list") + " " + RedProtect.get().config.getDefFlags()));
        p.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("general.color") + "------------------------------------"));

        StringBuilder sb = new StringBuilder();
        for (String flag : RedProtect.get().config.AdminFlags) {
            if (RedProtect.get().ph.hasFlagPerm(p, flag))
                sb.append(flag).append(", ");
        }
        if (sb.length() > 1) {
            p.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("cmdmanager.region.flag.admlist") + " [" + sb.toString().substring(0, sb.length() - 2) + "]"));
            p.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("general.color") + "------------------------------------"));
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
        return RedProtect.get().lang.get("cmdmanager.translation." + cmd);
    }

    public static String getCmdAlias(String cmd) {
        return RedProtect.get().lang.get("cmdmanager.translation." + cmd + ".alias");
    }

    public static boolean checkCmd(String arg, String cmd) {
        return arg.equalsIgnoreCase(getCmd(cmd)) || arg.equalsIgnoreCase(getCmdAlias(cmd)) || arg.equalsIgnoreCase(cmd);
    }

    public static void handleKillWorld(CommandSource sender, World world, EntityType type) {
        int killed = 0;
        int total = 0;
        int living = 0;
        for (Entity e : world.getEntities().stream().filter(entity ->
                !(entity instanceof Player) &&
                        ((entity instanceof Living && type == null) || entity.getType().equals(type))
        ).collect(Collectors.toList())) {
            total++;
            if (RedProtect.get().rm.getTopRegion(e.getLocation(), CommandHandlers.class.getName()) == null) {
                e.remove();
                killed++;
            } else if (e instanceof Living && type == null) {
                living++;
            } else if (e.getType().equals(type)) {
                living++;
            }
        }
        RedProtect.get().lang.sendMessage(sender, "cmdmanager.kill", new Replacer[]{
                new Replacer("{total}", String.valueOf(total)),
                new Replacer("{living}", String.valueOf(living)),
                new Replacer("{killed}", String.valueOf(killed)),
                new Replacer("{world}", world.getName())
        });
    }

    public static void HandleHelpPage(CommandSource sender, int page) {
        sender.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("_redprotect.prefix") + " " + RedProtect.get().lang.get("cmdmanager.available.cmds")));
        sender.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("general.color") + "------------------------------------"));
        sender.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("cmdmanager.helpheader.alias")));

        if (sender instanceof Player) {
            Player player = (Player) sender;
            int i = 0;
            for (String key : RedProtect.get().lang.getHelpStrings()) {
                if (RedProtect.get().ph.hasCommandPerm(player, key) || ((key.equals("pos1") || key.equals("pos2")) && RedProtect.get().ph.hasCommandPerm(player, "redefine"))) {
                    if (key.equalsIgnoreCase("flaggui")) {
                        continue;
                    }
                    i++;

                    if (i > (page * 10) - 10 && i <= page * 10) {
                        player.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("cmdmanager.help." + key).replace("{cmd}", getCmd(key)).replace("{alias}", getCmdAlias(key))));
                    }
                    if (i > page * 10) {
                        sender.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("general.color") + "------------------------------------"));
                        player.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("cmdmanager.page").replace("{page}", "" + (page + 1))));
                        break;
                    }
                }
            }
        } else {
            sender.sendMessage(RedProtect.get().getUtil().toText("&6rp &ckick &6<player> <region> <world> &3- Kicks a player from a region"));
            sender.sendMessage(RedProtect.get().getUtil().toText("&6rp &cinfo &6<region> <world> &3- Info about a region"));
            sender.sendMessage(RedProtect.get().getUtil().toText("&6rp &cflag &6<regionName> <Flag> <Value> <World> &3- Set a flag on region"));
            sender.sendMessage(RedProtect.get().getUtil().toText("&6rp &cflag &6info <region> <world> &3- Flag info for region"));
            sender.sendMessage(RedProtect.get().getUtil().toText("&6rp &caddmember &6<player> <region> <world> &3- Add player as member on region"));
            sender.sendMessage(RedProtect.get().getUtil().toText("&6rp &caddadmin &6<player> <region> <world> &3- Add player as admin on region"));
            sender.sendMessage(RedProtect.get().getUtil().toText("&6rp &caddleader &6<player> <region> <world> &3- Add player as leader on region"));
            sender.sendMessage(RedProtect.get().getUtil().toText("&6rp &cremovemember &6<player> <region> <world> &3- Remove a player as member on region"));
            sender.sendMessage(RedProtect.get().getUtil().toText("&6rp &cremoveadmin &6<player> <region> <world> &3- Remove a player as admin on region"));
            sender.sendMessage(RedProtect.get().getUtil().toText("&6rp &cremoveleader &6<player> <region> <world> &3- Remove a player as leader on region"));
            sender.sendMessage(RedProtect.get().getUtil().toText("&6rp &ctp &6<player> <regionName> <World> &3- Teleport player to a region"));
            sender.sendMessage(RedProtect.get().getUtil().toText("&6rp &cblocklimit &6<player> &3- Area limit for player"));
            sender.sendMessage(RedProtect.get().getUtil().toText("&6rp &cclaimlimit &6<player> [world] &3- Claim limit for player"));
            sender.sendMessage(RedProtect.get().getUtil().toText("&6rp &clist-all &3- List All regions"));
            sender.sendMessage(RedProtect.get().getUtil().toText("&6rp &clist &6<player> &3- List All player regions"));
            sender.sendMessage(RedProtect.get().getUtil().toText("&6rp &clist-areas &3- List All area exceeding regen limit"));
            sender.sendMessage(RedProtect.get().getUtil().toText("&6rp &csingle-to-files &3- Convert single world files to regions files"));
            sender.sendMessage(RedProtect.get().getUtil().toText("&6rp &cfiles-to-single &3- Convert regions files to single world files"));
            sender.sendMessage(RedProtect.get().getUtil().toText("&6rp &cfileTomysql &3- Convert from File to Mysql"));
            sender.sendMessage(RedProtect.get().getUtil().toText("&6rp &cmysqlToFile &3- Convert from Mysql to File"));
            sender.sendMessage(RedProtect.get().getUtil().toText("&6rp &csave-all &6[-f] &3- Save all regions to database"));
            sender.sendMessage(RedProtect.get().getUtil().toText("&6rp &cload-all &3- Load all regions from database"));
            sender.sendMessage(RedProtect.get().getUtil().toText("&6rp &creload-config &3- Reload only the configs"));
            sender.sendMessage(RedProtect.get().getUtil().toText("&6rp &creload &3- Reload the plugin"));
        }
        sender.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("general.color") + "------------------------------------"));
        if (RedProtect.get().ph.hasPerm(sender, "")) {
            String jarversion = RedProtect.get().container.getSource().get().toFile().getName();
            sender.sendMessage(RedProtect.get().getUtil().toText("&8&o- Full version: " + jarversion));
        }
    }
}
