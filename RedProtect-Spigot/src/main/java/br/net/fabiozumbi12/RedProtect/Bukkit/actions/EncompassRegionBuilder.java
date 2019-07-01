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
import br.net.fabiozumbi12.RedProtect.Bukkit.hooks.WEHook;
import br.net.fabiozumbi12.RedProtect.Bukkit.region.RegionBuilder;
import br.net.fabiozumbi12.RedProtect.Core.helpers.LogLevel;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class EncompassRegionBuilder extends RegionBuilder {

    public EncompassRegionBuilder(SignChangeEvent e) {
        String owner1 = RedProtect.get().getUtil().PlayerToUUID(e.getLine(2));
        String owner2 = RedProtect.get().getUtil().PlayerToUUID(e.getLine(3));
        Block b = e.getBlock();
        World w = b.getWorld();
        Player p = e.getPlayer();
        String pName = RedProtect.get().getUtil().PlayerToUUID(p.getName());
        Block last = b;
        Block current = b;
        Block next = null;
        Block first = null;
        String regionName = e.getLine(1);
        List<Integer> px = new LinkedList<>();
        List<Integer> pz = new LinkedList<>();
        Block bFirst1 = null;
        Block bFirst2 = null;
        List<Block> blocks = new LinkedList<>();
        int oldFacing = 0;
        int curFacing = 0;

        if (!RedProtect.get().config.isAllowedWorld(p)) {
            this.setErrorSign(e, RedProtect.get().lang.get("regionbuilder.region.worldnotallowed"));
            return;
        }

        //filter name
        regionName = RedProtect.get().getUtil().setName(regionName);

        //filter region name
        if (regionName == null || regionName.isEmpty() || regionName.length() < 3 || RedProtect.get().rm.getRegion(regionName, p.getWorld().getName()) != null) {
            regionName = RedProtect.get().getUtil().nameGen(p.getName(), p.getWorld().getName());
            if (regionName.length() > 16) {
                this.setErrorSign(e, RedProtect.get().lang.get("regionbuilder.autoname.error"));
                return;
            }
        }

        //region name conform
        if (regionName.length() < 3) {
            RedProtect.get().lang.sendMessage(p, "regionbuilder.regionname.invalid");
            return;
        }

        if (RedProtect.get().rm.getRegion(regionName, p.getWorld().getName()) != null) {
            RedProtect.get().lang.sendMessage(p, "regionbuilder.regionname.existis");
            return;
        }

        int maxby = current.getY();
        int minby = current.getY();

        for (int i = 0; i < RedProtect.get().config.configRoot().region_settings.max_scan; ++i) {
            int nearbyCount = 0;
            int x = current.getX();
            int y = current.getY();
            int z = current.getZ();

            Block[] block = new Block[6];
            block[0] = w.getBlockAt(x, y + 1, z);
            block[1] = w.getBlockAt(x, y - 1, z);
            block[2] = w.getBlockAt(x + 1, y, z);
            block[3] = w.getBlockAt(x - 1, y, z);
            block[4] = w.getBlockAt(x, y, z + 1);
            block[5] = w.getBlockAt(x, y, z - 1);

            for (int bi = 0; bi < block.length; ++bi) {

                boolean validBlock;

                validBlock = (block[bi].getType().name().contains(RedProtect.get().config.configRoot().region_settings.block_id));
                if (validBlock && !block[bi].getLocation().equals(last.getLocation())) {
                    ++nearbyCount;
                    next = block[bi];
                    curFacing = bi % 4;
                    if (i == 1) {
                        if (nearbyCount == 1) {
                            bFirst1 = block[bi];
                        }
                        if (nearbyCount == 2) {
                            bFirst2 = block[bi];
                        }
                    }
                }
            }
            if (nearbyCount == 1) {
                if (i != 0) {
                    blocks.add(current);

                    //set max and min y blocks
                    if (current.getLocation().getBlockY() > maxby) {
                        maxby = current.getLocation().getBlockY();
                    }
                    if (current.getLocation().getBlockY() < minby) {
                        minby = current.getLocation().getBlockY();
                    }

                    if (current.equals(first)) {
                        Set<String> leaders = new HashSet<>();
                        leaders.add(pName);
                        if (owner1 == null || owner1.isEmpty()) {
                            e.setLine(2, "--");

                        } else if (pName.equals(owner1)) {
                            e.setLine(2, "--");
                            RedProtect.get().lang.sendMessage(p, "regionbuilder.sign.dontneed.name");

                        } else {
                            leaders.add(owner1);
                        }


                        if (owner2 == null || owner2.isEmpty()) {
                            e.setLine(3, "--");
                        } else {
                            if (!(owner2.startsWith("[") && owner2.endsWith("]"))) {
                                if (pName.equals(owner2)) {
                                    e.setLine(3, "--");
                                    RedProtect.get().lang.sendMessage(p, "regionbuilder.sign.dontneed.name");
                                } else {
                                    leaders.add(owner2);
                                }
                            } else {
                                e.setLine(3, "--");
                            }
                        }

                        int[] rx = new int[px.size()];
                        int[] rz = new int[pz.size()];
                        int bl = 0;
                        for (int bx : px) {
                            rx[bl] = bx;
                            ++bl;
                        }
                        bl = 0;
                        for (int bz : pz) {
                            rz[bl] = bz;
                            ++bl;
                        }

                        int maxy = RedProtect.get().config.configRoot().region_settings.claim.maxy;
                        int miny = RedProtect.get().config.configRoot().region_settings.claim.miny;
                        if (maxy <= -1) {
                            maxy = w.getMaxHeight();
                        }
                        if (miny == -1) {
                            miny = 0;
                        }

                        Region region = new Region(regionName, new HashSet<>(), new HashSet<>(), new HashSet<>(), rx, rz, miny, maxy, 0, w.getName(), RedProtect.get().getUtil().dateNow(), RedProtect.get().config.getDefFlagsValues(), "", 0, null, true);
                        leaders.forEach(region::addLeader);
                        Set<String> othersName = new HashSet<>();
                        Region otherrg;
                        Set<Location> limitlocs = region.getLimitLocs(minby, maxby, false);

                        //check retangular region
                        for (Block bkloc : blocks) {
                            if (!limitlocs.contains(bkloc.getLocation())) {
                                this.setErrorSign(e, RedProtect.get().lang.get("regionbuilder.neeberetangle"));
                                return;
                            }
                        }

                        //check regions inside region
                        for (Region r : RedProtect.get().rm.getRegionsByWorld(p.getWorld().getName())) {
                            if (r.getMaxMbrX() <= region.getMaxMbrX() && r.getMaxY() <= region.getMaxY() && r.getMaxMbrZ() <= region.getMaxMbrZ() && r.getMinMbrX() >= region.getMinMbrX() && r.getMinY() >= region.getMinY() && r.getMinMbrZ() >= region.getMinMbrZ()) {
                                if (!r.isLeader(p) && !p.hasPermission("redprotect.bypass")) {
                                    this.setErrorSign(e, RedProtect.get().lang.get("regionbuilder.region.overlapping").replace("{location}", "x: " + r.getCenterX() + ", z: " + r.getCenterZ()).replace("{player}", r.getLeadersDesc()));
                                    return;
                                }
                                othersName.add(r.getName());
                            }
                        }

                        //check borders for other regions
                        for (Location loc : limitlocs) {
                        	
                        	/*
                        	//check regions near
                        	if (!CoreUtil.canBuildNear(p, loc)){
                            	return;    	
                            }*/

                            otherrg = RedProtect.get().rm.getTopRegion(loc);
                            RedProtect.get().logger.debug(LogLevel.DEFAULT, "protection Block is: " + loc.getBlock().getType().name());

                            if (otherrg != null) {
                                if (!otherrg.isLeader(p) && !p.hasPermission("redprotect.bypass")) {
                                    this.setErrorSign(e, RedProtect.get().lang.get("regionbuilder.region.overlapping").replace("{location}", "x: " + otherrg.getCenterX() + ", z: " + otherrg.getCenterZ()).replace("{player}", otherrg.getLeadersDesc()));
                                    return;
                                }
                                othersName.add(otherrg.getName());
                            }
                        }

                        //check if same area
                        otherrg = RedProtect.get().rm.getTopRegion(region.getCenterLoc());
                        if (otherrg != null && otherrg.get4Points(current.getY()).equals(region.get4Points(current.getY())) && !p.hasPermission("redprotect.bypass")) {
                            this.setErrorSign(e, RedProtect.get().lang.get("regionbuilder.region.overlapping").replace("{location}", "x: " + otherrg.getCenterX() + ", z: " + otherrg.getCenterZ()).replace("{player}", otherrg.getLeadersDesc()));
                            return;
                        }

                        region.setPrior(RedProtect.get().getUtil().getUpdatedPrior(region));

                        int claimLimit = RedProtect.get().ph.getPlayerClaimLimit(p);
                        int claimUsed = RedProtect.get().rm.getPlayerRegions(p.getUniqueId().toString(), w.getName());
                        boolean claimUnlimited = RedProtect.get().ph.hasPerm(p, "redprotect.limits.claim.unlimited");
                        if (claimUsed >= claimLimit && claimLimit >= 0 && !claimUnlimited) {
                            this.setErrorSign(e, RedProtect.get().lang.get("regionbuilder.claim.limit"));
                            return;
                        }

                        int pLimit = RedProtect.get().ph.getPlayerBlockLimit(p);
                        boolean areaUnlimited = RedProtect.get().ph.hasPerm(p, "redprotect.limits.blocks.unlimited");
                        int totalArea = RedProtect.get().rm.getTotalRegionSize(pName, p.getWorld().getName());
                        int regionArea = RedProtect.get().getUtil().simuleTotalRegionSize(p.getUniqueId().toString(), region);
                        int actualArea = 0;
                        if (regionArea > 0) {
                            actualArea = totalArea + regionArea;
                        }
                        if (pLimit >= 0 && actualArea > pLimit && !areaUnlimited) {
                            this.setErrorSign(e, RedProtect.get().lang.get("regionbuilder.reach.limit"));
                            return;
                        }

                        if (RedProtect.get().config.ecoRoot().claim_cost_per_block.enable && RedProtect.get().hooks.vault && !p.hasPermission("redprotect.eco.bypass")) {
                            double peco = RedProtect.get().economy.getBalance(p);
                            long reco = region.getArea() * RedProtect.get().config.ecoRoot().claim_cost_per_block.cost_per_block;

                            if (!RedProtect.get().config.ecoRoot().claim_cost_per_block.y_is_free) {
                                reco = reco * Math.abs(region.getMaxY() - region.getMinY());
                            }

                            if (peco >= reco) {
                                RedProtect.get().economy.withdrawPlayer(p, reco);
                                p.sendMessage(RedProtect.get().lang.get("economy.region.claimed").replace("{price}", RedProtect.get().config.ecoRoot().economy_symbol + reco + " " + RedProtect.get().config.ecoRoot().economy_name));
                            } else {
                                this.setErrorSign(e, RedProtect.get().lang.get("regionbuilder.notenought.money").replace("{price}", RedProtect.get().config.ecoRoot().economy_symbol + reco));
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
                        p.sendMessage(RedProtect.get().lang.get("regionbuilder.claim.left") + (claimUsed + 1) + RedProtect.get().lang.get("general.color") + "/" + (claimUnlimited ? RedProtect.get().lang.get("regionbuilder.area.unlimited") : claimLimit));
                        p.sendMessage(RedProtect.get().lang.get("regionbuilder.area.used") + " " + (regionArea == 0 ? ChatColor.GREEN + "" + regionArea : ChatColor.RED + "- " + regionArea) + "\n" +
                                RedProtect.get().lang.get("regionbuilder.area.left") + " " + (areaUnlimited ? RedProtect.get().lang.get("regionbuilder.area.unlimited") : (pLimit - actualArea)));
                        p.sendMessage(RedProtect.get().lang.get("cmdmanager.region.priority.set").replace("{region}", region.getName()) + " " + region.getPrior());
                        p.sendMessage(RedProtect.get().lang.get("general.color") + "------------------------------------");
                        if (othersName.size() > 0) {
                            p.sendMessage(RedProtect.get().lang.get("regionbuilder.overlapping"));
                            p.sendMessage(RedProtect.get().lang.get("region.regions") + " " + othersName);
                            p.sendMessage(RedProtect.get().lang.get("general.color") + "------------------------------------");
                        }

                        //Drop types
                        if (owner2 != null && RedProtect.get().config.configRoot().region_settings.claim.modes.allow_player_decide && RedProtect.get().lang.containsValue(owner2)) {
                            if (owner2.equalsIgnoreCase(RedProtect.get().lang.get("region.mode.drop"))) {
                                drop(b, blocks);
                                RedProtect.get().lang.sendMessage(p, "regionbuilder.region.droped");
                            }
                            if (owner2.equalsIgnoreCase(RedProtect.get().lang.get("region.mode.remove"))) {
                                remove(b, blocks);
                                RedProtect.get().lang.sendMessage(p, "regionbuilder.region.removed");
                            }
                            if (owner2.equalsIgnoreCase(RedProtect.get().lang.get("region.mode.give"))) {
                                give(b, p, blocks);
                                RedProtect.get().lang.sendMessage(p, "regionbuilder.region.given");
                            }
                        } else {
                            if (RedProtect.get().config.configRoot().region_settings.claim.modes.mode.equalsIgnoreCase("drop")) {
                                drop(b, blocks);
                            }
                            if (RedProtect.get().config.configRoot().region_settings.claim.modes.mode.equalsIgnoreCase("remove")) {
                                remove(b, blocks);
                            }
                            if (RedProtect.get().config.configRoot().region_settings.claim.modes.mode.equalsIgnoreCase("give")) {
                                give(b, p, blocks);
                            }
                        }


                        if (RedProtect.get().rm.getRegions(p.getUniqueId().toString(), p.getWorld().getName()).size() == 0) {
                            p.sendMessage(RedProtect.get().lang.get("cmdmanager.region.firstwarning"));
                            p.sendMessage(RedProtect.get().lang.get("general.color") + "------------------------------------");
                        }

                        //wecui
                        if (RedProtect.get().hooks.worldEdit && RedProtect.get().config.configRoot().hooks.useWECUI) {
                            WEHook.setSelectionRP(p, region.getMinLocation(), region.getMaxLocation());
                        }

                        this.r = region;
                        RedProtect.get().logger.addLog("(World " + region.getWorld() + ") Player " + p.getName() + " CREATED region " + region.getName());
                        return;
                    }
                }
            } else if (i == 1 && nearbyCount == 2) {
                //check other regions on blocks
                Region rcurrent = RedProtect.get().rm.getTopRegion(current.getLocation());
                if (rcurrent != null && !rcurrent.canBuild(p)) {
                    this.setErrorSign(e, RedProtect.get().lang.get("regionbuilder.region.overlapping").replace("{location}", "x: " + rcurrent.getCenterX() + ", z: " + rcurrent.getCenterZ()).replace("{player}", rcurrent.getLeadersDesc()));
                    return;
                }
                blocks.add(current);
                first = current;
                int x2 = bFirst1.getX();
                int z2 = bFirst1.getZ();
                int x3 = bFirst2.getX();
                int z3 = bFirst2.getZ();
                int distx = Math.abs(x2 - x3);
                int distz = Math.abs(z2 - z3);
                if ((distx != 2 || distz != 0) && (distz != 2 || distx != 0)) {
                    px.add(current.getX());
                    pz.add(current.getZ());
                }
            } else if (i != 0) {
                this.setErrorSign(e, RedProtect.get().lang.get("regionbuilder.area.error").replace("{area}", "(x: " + current.getX() + ", y: " + current.getY() + ", z: " + current.getZ() + ")"));
                Block newb = current.getRelative(BlockFace.UP);
                if (Material.getMaterial("SIGN_POST") != null) {
                    newb.getState().getBlock().setType(Material.getMaterial("SIGN_POST"));
                } else {
                    newb.getState().getBlock().setType(Arrays.stream(Material.values()).filter(m -> m.name().endsWith("_SIGN")).findFirst().get());
                }
                Sign s = (Sign) newb.getState();
                s.setLine(0, "§4xxxxxxxxxxxxxx");
                s.setLine(1, RedProtect.get().lang.get("_redprotect.prefix"));
                s.setLine(2, RedProtect.get().lang.get("blocklistener.postsign.error"));
                s.setLine(3, "§4xxxxxxxxxxxxxx");
                s.update();
                return;
            }
            if (oldFacing != curFacing && i > 1) {
                px.add(current.getX());
                pz.add(current.getZ());
            }
            last = current;
            if (next == null) {
                this.setErrorSign(e, RedProtect.get().lang.get("regionbuilder.area.next"));
                return;
            }
            current = next;
            oldFacing = curFacing;
        }
        String maxsize = String.valueOf(RedProtect.get().config.configRoot().region_settings.max_scan);
        this.setErrorSign(e, RedProtect.get().lang.get("regionbuilder.area.toobig").replace("{maxsize}", maxsize));
    }

    private void drop(Block sign, List<Block> blocks) {
        sign.breakNaturally();
        for (Block rb : blocks) {
            rb.breakNaturally();
        }
    }

    private void remove(Block sign, List<Block> blocks) {
        sign.breakNaturally();
        for (Block rb : blocks) {
            rb.setType(Material.AIR);
        }
    }

    private void give(Block sign, Player p, List<Block> blocks) {
        HashMap<Integer, ItemStack> left = p.getInventory().addItem(new ItemStack(Material.getMaterial(RedProtect.get().config.configRoot().region_settings.block_id), blocks.size()));
        if (!left.isEmpty()) {
            p.getWorld().dropItem(p.getLocation(), new ItemStack(Material.getMaterial(RedProtect.get().config.configRoot().region_settings.block_id), left.get(0).getAmount() - 1));
        }
        p.updateInventory();
        sign.breakNaturally();
        for (Block rb : blocks) {
            rb.setType(Material.AIR);
        }
    }
}
