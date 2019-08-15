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

package br.net.fabiozumbi12.RedProtect.Bukkit.actions;

import br.net.fabiozumbi12.RedProtect.Bukkit.API.events.CreateRegionEvent;
import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import br.net.fabiozumbi12.RedProtect.Bukkit.region.RegionBuilder;
import br.net.fabiozumbi12.RedProtect.Core.helpers.LogLevel;
import br.net.fabiozumbi12.RedProtect.Core.helpers.Replacer;
import br.net.fabiozumbi12.RedProtect.Core.region.PlayerRegion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class DefineRegionBuilder extends RegionBuilder {

    public DefineRegionBuilder(Player p, Location loc1, Location loc2, String regionName, PlayerRegion leader, Set<PlayerRegion> leaders, boolean admin) {
        if (!RedProtect.get().config.isAllowedWorld(p)) {
            this.setError(p, RedProtect.get().lang.get("regionbuilder.region.worldnotallowed"));
            return;
        }

        //filter name
        regionName = RedProtect.get().getUtil().setName(regionName);

        //region name conform
        if (regionName.length() < 3 || RedProtect.get().rm.getRegion(regionName, p.getWorld().getName()) != null) {
            //filter region name
            regionName = RedProtect.get().getUtil().nameGen(p.getName(), p.getWorld().getName());
            if (regionName.length() > 16) {
                RedProtect.get().lang.sendMessage(p, "regionbuilder.autoname.error");
                return;
            }
        }

        if (RedProtect.get().rm.getRegion(regionName, p.getWorld().getName()) != null) {
            RedProtect.get().lang.sendMessage(p, "regionbuilder.regionname.existis");
            return;
        }

        String wmsg = "";
        if (leader.contains(RedProtect.get().config.configRoot().region_settings.default_leader)) {
            wmsg = "hide ";
        }

        if (loc1 == null || loc2 == null) {
            RedProtect.get().lang.sendMessage(p, "regionbuilder.selection.notset");
            return;
        }

        //check if distance allowed
        if (loc1.getWorld().equals(loc2.getWorld()) && new Region(null, loc1, loc2, null).getArea() > RedProtect.get().config.configRoot().region_settings.max_scan && !RedProtect.get().ph.hasPerm(p, "redprotect.bypass.define-max-distance")) {
            double dist = new Region(null, loc1, loc2, null).getArea();
            RedProtect.get().lang.sendMessage(p, String.format(RedProtect.get().lang.get("regionbuilder.selection.maxdefine"), RedProtect.get().config.configRoot().region_settings.max_scan, dist));
            return;
        }

        leaders.add(leader);

        int miny = loc1.getBlockY();
        int maxy = loc2.getBlockY();
        if (RedProtect.get().config.configRoot().region_settings.autoexpandvert_ondefine) {
            miny = 0;
            maxy = p.getWorld().getMaxHeight();
            if (RedProtect.get().config.configRoot().region_settings.claim.miny != -1)
                miny = RedProtect.get().config.configRoot().region_settings.claim.miny;
            if (RedProtect.get().config.configRoot().region_settings.claim.maxy != -1)
                maxy = RedProtect.get().config.configRoot().region_settings.claim.maxy;
        }

        Region newRegion = new Region(regionName, new HashSet<>(), new HashSet<>(), leaders, new int[]{loc1.getBlockX(), loc1.getBlockX(), loc2.getBlockX(), loc2.getBlockX()}, new int[]{loc1.getBlockZ(), loc1.getBlockZ(), loc2.getBlockZ(), loc2.getBlockZ()}, miny, maxy, 0, p.getWorld().getName(), RedProtect.get().getUtil().dateNow(), RedProtect.get().config.getDefFlagsValues(), wmsg, 0, null, true);
        newRegion.setPrior(RedProtect.get().getUtil().getUpdatedPrior(newRegion));

        int claimLimit = RedProtect.get().ph.getPlayerClaimLimit(p);
        int claimUsed = RedProtect.get().rm.getPlayerRegions(p.getUniqueId().toString(), p.getWorld().getName());
        boolean claimUnlimited = RedProtect.get().ph.hasPerm(p, "redprotect.limits.claim.unlimited");
        if (claimUsed >= claimLimit && claimLimit >= 0 && !claimUnlimited) {
            this.setError(p, RedProtect.get().lang.get("regionbuilder.claim.limit"));
            return;
        }

        int pLimit = RedProtect.get().ph.getPlayerBlockLimit(p);
        int totalArea = RedProtect.get().rm.getTotalRegionSize(leader.getUUID(), p.getWorld().getName());
        boolean areaUnlimited = RedProtect.get().ph.hasPerm(p, "redprotect.limits.blocks.unlimited");
        int regionArea = RedProtect.get().getUtil().simuleTotalRegionSize(p.getUniqueId().toString(), newRegion);
        int actualArea = 0;
        if (regionArea > 0) {
            actualArea = totalArea + regionArea;
        }
        if (pLimit >= 0 && actualArea > pLimit && !areaUnlimited) {
            this.setError(p, RedProtect.get().lang.get("regionbuilder.reach.limit"));
            return;
        }

        Set<String> othersName = new HashSet<>();
        Region otherrg;

        //check if same area
        otherrg = RedProtect.get().rm.getTopRegion(newRegion.getCenterLoc());
        if (otherrg != null && otherrg.get4Points(newRegion.getCenterY()).equals(newRegion.get4Points(newRegion.getCenterY()))) {
            this.setError(p, RedProtect.get().lang.get("regionbuilder.region.overlapping").replace("{location}", "x: " + otherrg.getCenterX() + ", z: " + otherrg.getCenterZ()).replace("{player}", otherrg.getLeadersDesc()));
            return;
        }

        //check regions inside region
        for (Region r : RedProtect.get().rm.getRegionsByWorld(p.getWorld().getName())) {
            if (r.getMaxMbrX() <= newRegion.getMaxMbrX() && r.getMaxY() <= newRegion.getMaxY() && r.getMaxMbrZ() <= newRegion.getMaxMbrZ() && r.getMinMbrX() >= newRegion.getMinMbrX() && r.getMinY() >= newRegion.getMinY() && r.getMinMbrZ() >= newRegion.getMinMbrZ()) {
                if (!r.isLeader(p) && !p.hasPermission("redprotect.bypass")) {
                    this.setError(p, RedProtect.get().lang.get("regionbuilder.region.overlapping").replace("{location}", "x: " + r.getCenterX() + ", z: " + r.getCenterZ()).replace("{player}", r.getLeadersDesc()));
                    return;
                }
                othersName.add(r.getName());
            }
        }

        //check borders for other regions
        Set<Location> limitlocs = newRegion.getLimitLocs(newRegion.getMinY(), newRegion.getMaxY(), true);
        for (Location loc : limitlocs) {
            otherrg = RedProtect.get().rm.getTopRegion(loc);
            RedProtect.get().logger.debug(LogLevel.DEFAULT, "protection Block is: " + loc.getBlock().getType().name());

            if (otherrg != null) {
                if (!otherrg.isLeader(p) && !p.hasPermission("redprotect.bypass")) {
                    this.setError(p, RedProtect.get().lang.get("regionbuilder.region.overlapping").replace("{location}", "x: " + otherrg.getCenterX() + ", z: " + otherrg.getCenterZ()).replace("{player}", otherrg.getLeadersDesc()));
                    return;
                }
                othersName.add(otherrg.getName());
            }
        }

        long reco = 0;
        if (RedProtect.get().config.ecoRoot().claim_cost_per_block.enable && RedProtect.get().hooks.vault && !p.hasPermission("redprotect.eco.bypass")) {
            double peco = RedProtect.get().economy.getBalance(p);
            reco = newRegion.getArea() * RedProtect.get().config.ecoRoot().claim_cost_per_block.cost_per_block;

            if (!RedProtect.get().config.ecoRoot().claim_cost_per_block.y_is_free) {
                reco = reco * Math.abs(newRegion.getMaxY() - newRegion.getMinY());
            }

            if (peco >= reco) {
                RedProtect.get().economy.withdrawPlayer(p, reco);
                p.sendMessage(RedProtect.get().lang.get("economy.region.claimed").replace("{price}", RedProtect.get().config.ecoRoot().economy_symbol + reco + " " + RedProtect.get().config.ecoRoot().economy_name));
            } else {
                RedProtect.get().lang.sendMessage(p, "regionbuilder.notenought.money", new Replacer[]{new Replacer("{price}", RedProtect.get().config.ecoRoot().economy_symbol + reco)});
                return;
            }
        }

        //fire event
        CreateRegionEvent event = new CreateRegionEvent(r, p);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        p.sendMessage(RedProtect.get().lang.get("general.color") + "------------------------------------");
        if (!admin) {
            p.sendMessage(RedProtect.get().lang.get("regionbuilder.claim.left") + (claimUsed + 1) + RedProtect.get().lang.get("general.color") + "/" + (claimUnlimited ? RedProtect.get().lang.get("regionbuilder.area.unlimited") : claimLimit));
            p.sendMessage(RedProtect.get().lang.get("regionbuilder.area.used") + " " + (regionArea == 0 ? ChatColor.GREEN + "" + regionArea : ChatColor.RED + "- " + regionArea) + "\n" +
                    RedProtect.get().lang.get("regionbuilder.area.left") + " " + (areaUnlimited ? RedProtect.get().lang.get("regionbuilder.area.unlimited") : (pLimit - actualArea)));
        }
        p.sendMessage(RedProtect.get().lang.get("cmdmanager.region.priority.set").replace("{region}", newRegion.getName()) + " " + newRegion.getPrior());
        if (RedProtect.get().config.ecoRoot().claim_cost_per_block.enable) p.sendMessage(RedProtect.get().lang.get("regionbuilder.block.cost") + reco);
        p.sendMessage(RedProtect.get().lang.get("general.color") + "------------------------------------");
        if (othersName.size() > 0) {
            p.sendMessage(RedProtect.get().lang.get("regionbuilder.overlapping"));
            p.sendMessage(RedProtect.get().lang.get("region.regions") + " " + othersName);
            p.sendMessage(RedProtect.get().lang.get("general.color") + "------------------------------------");
        }

        if (RedProtect.get().rm.getRegions(p.getUniqueId().toString(), p.getWorld().getName()).size() == 0) {
            p.sendMessage(RedProtect.get().lang.get("cmdmanager.region.firstwarning"));
            p.sendMessage(RedProtect.get().lang.get("general.color") + "------------------------------------");
        }

        this.r = newRegion;
        RedProtect.get().logger.addLog("(World " + newRegion.getWorld() + ") Player " + p.getName() + " DEFINED region " + newRegion.getName());
    }
}
