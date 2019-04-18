/*
 *  Copyright (c) 2019 - @FabioZumbi12
 *  Last Modified: 16/04/19 06:21
 *
 *  This class is provided 'as-is', without any express or implied warranty. In no event will the authors be held liable for any
 *   damages arising from the use of this class.
 *
 *  Permission is granted to anyone to use this class for any purpose, including commercial plugins, and to alter it and
 *  redistribute it freely, subject to the following restrictions:
 *  1 - The origin of this class must not be misrepresented; you must not claim that you wrote the original software. If you
 *  use this class in other plugins, an acknowledgment in the plugin documentation would be appreciated but is not required.
 *  2 - Altered source versions must be plainly marked as such, and must not be misrepresented as being the original class.
 *  3 - This notice may not be removed or altered from any source distribution.
 *
 *  Esta classe é fornecida "como está", sem qualquer garantia expressa ou implícita. Em nenhum caso os autores serão
 *  responsabilizados por quaisquer danos decorrentes do uso desta classe.
 *
 *  É concedida permissão a qualquer pessoa para usar esta classe para qualquer finalidade, incluindo plugins pagos, e para
 *  alterá-lo e redistribuí-lo livremente, sujeito às seguintes restrições:
 *  1 - A origem desta classe não deve ser deturpada; você não deve afirmar que escreveu a classe original. Se você usar esta
 *   classe em um plugin, uma confirmação de autoria na documentação do plugin será apreciada, mas não é necessária.
 *  2 - Versões de origem alteradas devem ser claramente marcadas como tal e não devem ser deturpadas como sendo a
 *  classe original.
 *  3 - Este aviso não pode ser removido ou alterado de qualquer distribuição de origem.
 */

package br.net.fabiozumbi12.RedProtect.Sponge.actions;

import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import br.net.fabiozumbi12.RedProtect.Sponge.Region;
import br.net.fabiozumbi12.RedProtect.Sponge.config.RPLang;
import br.net.fabiozumbi12.RedProtect.Sponge.events.CreateRegionEvent;
import br.net.fabiozumbi12.RedProtect.Sponge.helpers.LogLevel;
import br.net.fabiozumbi12.RedProtect.Sponge.helpers.RPUtil;
import br.net.fabiozumbi12.RedProtect.Sponge.region.RegionBuilder;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DefineRegionBuilder extends RegionBuilder {

    public DefineRegionBuilder(Player p, Location<World> loc1, Location<World> loc2, String regionName, String leader, Set<String> leaders, boolean admin) {
        if (!RedProtect.get().cfgs.isAllowedWorld(p)) {
            this.setError(p, RPLang.get("regionbuilder.newRegion.worldnotallowed"));
            return;
        }

        //filter name
        regionName = RPUtil.setName(regionName);

        //newRegion name check
        if (regionName.length() < 3) {
            RPLang.sendMessage(p, "regionbuilder.regionname.invalid");
            return;
        }

        //filter newRegion name
        regionName = RPUtil.nameGen(p.getName(), p.getWorld().getName());
        if (regionName.length() > 16) {
            RPLang.sendMessage(p, "regionbuilder.autoname.error");
            return;
        }

        if (RedProtect.get().rm.getRegion(regionName, p.getWorld()) != null) {
            RPLang.sendMessage(p, "regionbuilder.regionname.existis");
            return;
        }

        //newRegion leader
        String pName = RPUtil.PlayerToUUID(p.getName());

        String wmsg = "";
        if (leader.equals(RedProtect.get().cfgs.root().region_settings.default_leader)) {
            pName = leader;
            wmsg = "hide ";
        }

        if (loc1 == null || loc2 == null) {
            RPLang.sendMessage(p, "regionbuilder.selection.notset");
            return;
        }

        //check if distance allowed
        if (new Region(null, loc1, loc2, null).getArea() > RedProtect.get().cfgs.root().region_settings.wand_max_distance && !RedProtect.get().ph.hasPerm(p, "redprotect.bypass.define-max-distance")) {
            double dist = new Region(null, loc1, loc2, null).getArea();
            RPLang.sendMessage(p, String.format(RPLang.get("regionbuilder.selection.maxdefine"), RedProtect.get().cfgs.root().region_settings.wand_max_distance, dist));
            return;
        }

        leaders.add(leader);
        if (!pName.equals(leader)) {
            leaders.add(pName);
        }

        int miny = loc1.getBlockY();
        int maxy = loc2.getBlockY();
        if (RedProtect.get().cfgs.root().region_settings.autoexpandvert_ondefine) {
            miny = 0;
            maxy = p.getWorld().getBlockMax().getY();
            if (RedProtect.get().cfgs.root().region_settings.claim.miny != -1)
                miny = RedProtect.get().cfgs.root().region_settings.claim.miny;
            if (RedProtect.get().cfgs.root().region_settings.claim.maxy != -1)
                maxy = RedProtect.get().cfgs.root().region_settings.claim.maxy;
        }

        Region newRegion = new Region(regionName, new HashSet<>(), new HashSet<>(), new HashSet<>(), new int[]{loc1.getBlockX(), loc1.getBlockX(), loc2.getBlockX(), loc2.getBlockX()}, new int[]{loc1.getBlockZ(), loc1.getBlockZ(), loc2.getBlockZ(), loc2.getBlockZ()}, miny, maxy, 0, p.getWorld().getName(), RPUtil.dateNow(), RedProtect.get().cfgs.getDefFlagsValues(), wmsg, 0, null, true);
        leaders.forEach(newRegion::addLeader);
        newRegion.setPrior(RPUtil.getUpdatedPrior(newRegion));

        int claimLimit = RedProtect.get().ph.getPlayerClaimLimit(p);
        int claimused = RedProtect.get().rm.getPlayerRegions(RPUtil.PlayerToUUID(p.getName()), p.getWorld());
        boolean claimUnlimited = RedProtect.get().ph.hasPerm(p, "redprotect.limits.claim.unlimited");
        if (claimused >= claimLimit && claimLimit != -1) {
            this.setError(p, RPLang.get("regionbuilder.claim.limit"));
            return;
        }

        int pLimit = RedProtect.get().ph.getPlayerBlockLimit(p);
        int totalArea = RedProtect.get().rm.getTotalRegionSize(pName, p.getWorld().getName());
        boolean areaUnlimited = RedProtect.get().ph.hasPerm(p, "redprotect.limits.blocks.unlimited");
        int regionarea = RPUtil.simuleTotalRegionSize(RPUtil.PlayerToUUID(p.getName()), newRegion);
        int actualArea = 0;
        if (regionarea > 0) {
            actualArea = totalArea + regionarea;
        }
        if (pLimit >= 0 && actualArea > pLimit && !areaUnlimited) {
            this.setError(p, RPLang.get("regionbuilder.reach.limit"));
            return;
        }

        List<String> othersName = new ArrayList<>();
        Region otherrg = null;

        //check if same area
        otherrg = RedProtect.get().rm.getTopRegion(newRegion.getCenterLoc(), this.getClass().getName());
        if (otherrg != null && otherrg.get4Points(newRegion.getCenterY()).equals(newRegion.get4Points(newRegion.getCenterY()))) {
            this.setError(p, RPLang.get("regionbuilder.newRegion.overlapping").replace("{location}", "x: " + otherrg.getCenterX() + ", z: " + otherrg.getCenterZ()).replace("{player}", RPUtil.UUIDtoPlayer(otherrg.getLeadersDesc())));
            return;
        }

        //check regions inside newRegion
        for (Region r : RedProtect.get().rm.getRegionsByWorld(p.getWorld())) {
            if (r.getMaxMbrX() <= newRegion.getMaxMbrX() && r.getMaxY() <= newRegion.getMaxY() && r.getMaxMbrZ() <= newRegion.getMaxMbrZ() && r.getMinMbrX() >= newRegion.getMinMbrX() && r.getMinY() >= newRegion.getMinY() && r.getMinMbrZ() >= newRegion.getMinMbrZ()) {
                if (!r.isLeader(p) && !p.hasPermission("redprotect.bypass")) {
                    this.setError(p, RPLang.get("regionbuilder.newRegion.overlapping").replace("{location}", "x: " + r.getCenterX() + ", z: " + r.getCenterZ()).replace("{player}", RPUtil.UUIDtoPlayer(otherrg.getLeadersDesc())));
                    return;
                }
                if (!othersName.contains(r.getName())) {
                    othersName.add(r.getName());
                }
            }
        }

        //check borders for other regions
        List<Location<World>> limitlocs = newRegion.getLimitLocs(newRegion.getMinY(), newRegion.getMaxY(), true);
        for (Location<World> loc : limitlocs) {
        	
        	/*
        	//check regions near
        	if (!CoreUtil.canBuildNear(p, loc)){
            	return;    	
            }*/

            otherrg = RedProtect.get().rm.getTopRegion(loc, this.getClass().getName());
            RedProtect.get().logger.debug(LogLevel.BLOCKS, "protection Block is: " + loc.getBlock().getType().getName());

            if (otherrg != null) {
                if (!otherrg.isLeader(p) && !p.hasPermission("redprotect.bypass")) {
                    this.setError(p, RPLang.get("regionbuilder.newRegion.overlapping").replace("{location}", "x: " + otherrg.getCenterX() + ", z: " + otherrg.getCenterZ()).replace("{player}", RPUtil.UUIDtoPlayer(otherrg.getLeadersDesc())));
                    return;
                }
                if (!othersName.contains(otherrg.getName())) {
                    othersName.add(otherrg.getName());
                }
            }
        }

        if (RedProtect.get().cfgs.getEcoBool("claim-cost-per-block.enable") && !p.hasPermission("redprotect.eco.bypass")) {
            UniqueAccount acc = RedProtect.get().econ.getOrCreateAccount(p.getUniqueId()).get();
            Double peco = acc.getBalance(RedProtect.get().econ.getDefaultCurrency()).doubleValue();
            long reco = newRegion.getArea() * RedProtect.get().cfgs.getEcoInt("claim-cost-per-block.cost-per-block");

            if (!RedProtect.get().cfgs.getEcoBool("claim-cost-per-block.y-is-free")) {
                reco = reco * Math.abs(newRegion.getMaxY() - newRegion.getMinY());
            }

            if (peco >= reco) {
                acc.withdraw(RedProtect.get().econ.getDefaultCurrency(), BigDecimal.valueOf(reco), RedProtect.get().getPVHelper().getCause(p));
                p.sendMessage(RPUtil.toText(RPLang.get("economy.newRegion.claimed").replace("{price}", RedProtect.get().cfgs.getEcoString("economy-symbol") + reco + " " + RedProtect.get().cfgs.getEcoString("economy-name"))));
            } else {
                this.setError(p, RPLang.get("regionbuilder.notenought.money").replace("{price}", RedProtect.get().cfgs.getEcoString("economy-symbol") + reco));
                return;
            }
        }

        //fire event
        CreateRegionEvent event = new CreateRegionEvent(r, p);
        if (Sponge.getEventManager().post(event)) {
            return;
        }

        p.sendMessage(RPUtil.toText(RPLang.get("general.color") + "------------------------------------"));
        p.sendMessage(RPUtil.toText(RPLang.get("regionbuilder.claim.left") + (claimused + 1) + RPLang.get("general.color") + "/" + (claimUnlimited ? RPLang.get("regionbuilder.area.unlimited") : claimLimit)));
        p.sendMessage(RPUtil.toText(RPLang.get("regionbuilder.area.used") + " " + (regionarea == 0 ? "&a" + regionarea : "&c- " + regionarea) + "\n" +
                RPLang.get("regionbuilder.area.left") + " " + (areaUnlimited ? RPLang.get("regionbuilder.area.unlimited") : (pLimit - actualArea))));
        p.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.newRegion.priority.set").replace("{newRegion}", newRegion.getName()) + " " + newRegion.getPrior()));
        p.sendMessage(RPUtil.toText(RPLang.get("general.color") + "------------------------------------"));
        if (othersName.size() > 0) {
            p.sendMessage(RPUtil.toText(RPLang.get("general.color") + "------------------------------------"));
            p.sendMessage(RPUtil.toText(RPLang.get("regionbuilder.overlapping")));
            p.sendMessage(RPUtil.toText(RPLang.get("newRegion.regions") + " " + othersName));
        }

        if (RedProtect.get().rm.getRegions(RPUtil.PlayerToUUID(p.getName()), p.getWorld()).size() == 0) {
            p.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.newRegion.firstwarning")));
            p.sendMessage(RPUtil.toText(RPLang.get("general.color") + "------------------------------------"));
        }
        this.r = newRegion;
        RedProtect.get().logger.addLog("(World " + newRegion.getWorld() + ") Player " + p.getName() + " DEFINED newRegion " + newRegion.getName());
    }
}
