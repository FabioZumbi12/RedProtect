/*
 * Copyright (c) 2020 - @FabioZumbi12
 * Last Modified: 02/07/2020 19:01.
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
import java.util.concurrent.ExecutionException;

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

        if (!RedProtect.get().getConfigManager().isAllowedWorld(p)) {
            this.setErrorSign(e, RedProtect.get().getLanguageManager().get("regionbuilder.region.worldnotallowed"));
            return;
        }

        //filter name
        regionName = RedProtect.get().getUtil().fixRegionName(p, regionName);
        if (regionName == null) {
            this.setErrorSign(e, RedProtect.get().getLanguageManager().get("regionbuilder.error"));
            return;
        }

        int maxby = current.getY();
        int minby = current.getY();

        for (int i = 0; i < RedProtect.get().getConfigManager().configRoot().region_settings.max_scan; ++i) {
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

                boolean validBlock = (block[bi].getType().name().contains(RedProtect.get().getConfigManager().configRoot().region_settings.block_id));

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
                        Set<String> admins = new HashSet<>();
                        // admins.add(pName);
                        if (owner1 == null || owner1.isEmpty()) {
                            e.setLine(2, "--");

                        } else if (pName.equals(owner1)) {
                            e.setLine(2, "--");
                            RedProtect.get().getLanguageManager().sendMessage(p, "regionbuilder.sign.dontneed.name");
                        } else {
                            admins.add(owner1);
                        }


                        if (owner2 == null || owner2.isEmpty()) {
                            e.setLine(3, "--");
                        } else {
                            if (!(owner2.startsWith("[") && owner2.endsWith("]"))) {
                                if (pName.equals(owner2)) {
                                    e.setLine(3, "--");
                                    RedProtect.get().getLanguageManager().sendMessage(p, "regionbuilder.sign.dontneed.name");
                                } else {
                                    admins.add(owner2);
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

                        int maxy = RedProtect.get().getConfigManager().configRoot().region_settings.claim.maxy;
                        int miny = RedProtect.get().getConfigManager().configRoot().region_settings.claim.miny;
                        if (maxy <= -1) {
                            maxy = w.getMaxHeight();
                        }
                        if (miny == -1) {
                            miny = w.getMinHeight();
                        }

                        Region newRegion = new Region(regionName, new HashSet<>(), new HashSet<>(), new HashSet<>(), rx, rz, miny, maxy, 0, w.getName(), RedProtect.get().getUtil().dateNow(), RedProtect.get().getConfigManager().getDefFlagsValues(), "", 0, null, true, true);
                        newRegion.addLeader(pName);
                        admins.forEach(newRegion::addAdmin);
                        Set<String> othersName = new HashSet<>();
                        Region otherrg;
                        Set<Location> limitlocs = newRegion.getLimitLocs(minby, maxby, false);

                        //check retangular region
                        for (Block bkloc : blocks) {
                            if (!limitlocs.contains(bkloc.getLocation())) {
                                setErrorSign(e, RedProtect.get().getLanguageManager().get("regionbuilder.neeberetangle"));
                                return;
                            }
                        }

                        //check regions inside region
                        for (Region r : RedProtect.get().getRegionManager().getRegionsByWorld(p.getWorld().getName())) {
                            if (r.getMaxMbrX() <= newRegion.getMaxMbrX() && r.getMaxY() <= newRegion.getMaxY() && r.getMaxMbrZ() <= newRegion.getMaxMbrZ() && r.getMinMbrX() >= newRegion.getMinMbrX() && r.getMinY() >= newRegion.getMinY() && r.getMinMbrZ() >= newRegion.getMinMbrZ()) {
                                if (!r.isLeader(p) && !p.hasPermission("redprotect.bypass")) {
                                    setErrorSign(e, RedProtect.get().getLanguageManager().get("regionbuilder.region.overlapping").replace("{location}", "x: " + r.getCenterX() + ", z: " + r.getCenterZ()).replace("{player}", r.getLeadersDesc()));
                                    return;
                                }
                                othersName.add(r.getName());
                            }
                        }

                        //check borders for other regions
                        for (Location loc : limitlocs) {

                            otherrg = RedProtect.get().getRegionManager().getTopRegion(loc);
                            RedProtect.get().logger.debug(LogLevel.DEFAULT, "protection Block is: " + loc.getBlock().getType().name());

                            if (otherrg != null) {
                                if (!otherrg.isLeader(p) && !p.hasPermission("redprotect.bypass")) {
                                    setErrorSign(e, RedProtect.get().getLanguageManager().get("regionbuilder.region.overlapping").replace("{location}", "x: " + otherrg.getCenterX() + ", z: " + otherrg.getCenterZ()).replace("{player}", otherrg.getLeadersDesc()));
                                    return;
                                }
                                othersName.add(otherrg.getName());
                            }
                        }

                        //check if same area
                        otherrg = RedProtect.get().getRegionManager().getTopRegion(newRegion.getCenterLoc());
                        if (otherrg != null && otherrg.get4Points(current.getY()).equals(newRegion.get4Points(current.getY())) && !p.hasPermission("redprotect.bypass")) {
                            setErrorSign(e, RedProtect.get().getLanguageManager().get("regionbuilder.region.overlapping").replace("{location}", "x: " + otherrg.getCenterX() + ", z: " + otherrg.getCenterZ()).replace("{player}", otherrg.getLeadersDesc()));
                            return;
                        }

                        newRegion.setPrior(RedProtect.get().getUtil().getUpdatedPrior(newRegion));

                        int claimLimit = RedProtect.get().getPermissionHandler().getPlayerClaimLimit(p);
                        int claimUsed = RedProtect.get().getRegionManager().getPlayerRegions(p.getUniqueId().toString(), w.getName());
                        boolean claimUnlimited = RedProtect.get().getPermissionHandler().hasPerm(p, "redprotect.limits.claim.unlimited");
                        if (claimUsed >= claimLimit && claimLimit >= 0 && !claimUnlimited) {
                            setErrorSign(e, RedProtect.get().getLanguageManager().get("regionbuilder.claim.limit"));
                            return;
                        }

                        int pLimit = RedProtect.get().getPermissionHandler().getPlayerBlockLimit(p);
                        boolean areaUnlimited = RedProtect.get().getPermissionHandler().hasPerm(p, "redprotect.limits.blocks.unlimited");
                        int totalArea = RedProtect.get().getRegionManager().getTotalRegionSize(pName, p.getWorld().getName());
                        int regionArea = RedProtect.get().getUtil().simuleTotalRegionSize(p.getUniqueId().toString(), newRegion);
                        int actualArea = 0;
                        if (regionArea > 0) {
                            actualArea = totalArea + regionArea;
                        }
                        if (pLimit >= 0 && actualArea > pLimit && !areaUnlimited) {
                            setErrorSign(e, RedProtect.get().getLanguageManager().get("regionbuilder.reach.limit"));
                            return;
                        }

                        long reco = 0;
                        if (RedProtect.get().getConfigManager().ecoRoot().claim_cost_per_block.enable && RedProtect.get().hooks.vault && !p.hasPermission("redprotect.eco.bypass")) {
                            double peco = RedProtect.get().economy.getBalance(p);
                            reco = (long) newRegion.getArea() * RedProtect.get().getConfigManager().ecoRoot().claim_cost_per_block.cost_per_block;

                            if (!RedProtect.get().getConfigManager().ecoRoot().claim_cost_per_block.y_is_free) {
                                reco = reco * Math.abs(newRegion.getMaxY() - newRegion.getMinY());
                            }

                            if (peco >= reco) {
                                RedProtect.get().economy.withdrawPlayer(p, reco);
                                p.sendMessage(RedProtect.get().getLanguageManager().get("economy.region.claimed").replace("{price}", RedProtect.get().getConfigManager().ecoRoot().economy_symbol + reco + " " + RedProtect.get().getConfigManager().ecoRoot().economy_name));
                            } else {
                                setErrorSign(e, RedProtect.get().getLanguageManager().get("regionbuilder.notenought.money").replace("{price}", RedProtect.get().getConfigManager().ecoRoot().economy_symbol + reco));
                                return;
                            }
                        }

                        //fire event
                        CreateRegionEvent event = new CreateRegionEvent(newRegion, p);
                        try {
                            CreateRegionEvent finalEvent = event;
                            event = Bukkit.getScheduler().callSyncMethod(RedProtect.get(), () -> {
                                Bukkit.getPluginManager().callEvent(finalEvent);
                                return finalEvent;
                            }).get();
                        } catch (InterruptedException | ExecutionException interruptedException) {
                            interruptedException.printStackTrace();
                        }
                        if (event.isCancelled()) {
                            return;
                        }

                        newRegion = event.getRegion();

                        p.sendMessage(RedProtect.get().getLanguageManager().get("general.color") + "------------------------------------");
                        p.sendMessage(RedProtect.get().getLanguageManager().get("regionbuilder.claim.left") + (claimUsed + 1) + RedProtect.get().getLanguageManager().get("general.color") + "/" + (claimUnlimited ? RedProtect.get().getLanguageManager().get("regionbuilder.area.unlimited") : claimLimit));
                        p.sendMessage(RedProtect.get().getLanguageManager().get("regionbuilder.area.used") + " " + (regionArea == 0 ? ChatColor.GREEN + "" + regionArea : ChatColor.RED + "- " + regionArea) + "\n" +
                                RedProtect.get().getLanguageManager().get("regionbuilder.area.left") + " " + (areaUnlimited ? RedProtect.get().getLanguageManager().get("regionbuilder.area.unlimited") : (pLimit - actualArea)));
                        p.sendMessage(RedProtect.get().getLanguageManager().get("cmdmanager.region.priority.set").replace("{region}", newRegion.getName()) + " " + newRegion.getPrior());
                        if (RedProtect.get().getConfigManager().ecoRoot().claim_cost_per_block.enable)
                            p.sendMessage(RedProtect.get().getLanguageManager().get("regionbuilder.block.cost") + reco);
                        p.sendMessage(RedProtect.get().getLanguageManager().get("general.color") + "------------------------------------");
                        if (othersName.size() > 0) {
                            p.sendMessage(RedProtect.get().getLanguageManager().get("regionbuilder.overlapping"));
                            p.sendMessage(RedProtect.get().getLanguageManager().get("region.regions") + " " + othersName);
                            p.sendMessage(RedProtect.get().getLanguageManager().get("general.color") + "------------------------------------");
                        }

                        //Drop types
                        Bukkit.getScheduler().callSyncMethod(RedProtect.get(), () -> {
                            if (owner2 != null && RedProtect.get().getConfigManager().configRoot().region_settings.claim.modes.allow_player_decide && RedProtect.get().getLanguageManager().containsValue(owner2)) {
                                if (owner2.equalsIgnoreCase(RedProtect.get().getLanguageManager().get("region.mode.drop"))) {
                                    drop(b, blocks);
                                    RedProtect.get().getLanguageManager().sendMessage(p, "regionbuilder.region.droped");
                                }
                                if (owner2.equalsIgnoreCase(RedProtect.get().getLanguageManager().get("region.mode.remove"))) {
                                    remove(b, blocks);
                                    RedProtect.get().getLanguageManager().sendMessage(p, "regionbuilder.region.removed");
                                }
                                if (owner2.equalsIgnoreCase(RedProtect.get().getLanguageManager().get("region.mode.give"))) {
                                    give(b, p, blocks);
                                    RedProtect.get().getLanguageManager().sendMessage(p, "regionbuilder.region.given");
                                }
                            } else {
                                if (RedProtect.get().getConfigManager().configRoot().region_settings.claim.modes.mode.equalsIgnoreCase("drop")) {
                                    drop(b, blocks);
                                }
                                if (RedProtect.get().getConfigManager().configRoot().region_settings.claim.modes.mode.equalsIgnoreCase("remove")) {
                                    remove(b, blocks);
                                }
                                if (RedProtect.get().getConfigManager().configRoot().region_settings.claim.modes.mode.equalsIgnoreCase("give")) {
                                    give(b, p, blocks);
                                }
                            }
                            return true;
                        });

                        if (RedProtect.get().getRegionManager().getRegions(p.getUniqueId().toString(), p.getWorld().getName()).size() == 0) {
                            p.sendMessage(RedProtect.get().getLanguageManager().get("cmdmanager.region.firstwarning"));
                            p.sendMessage(RedProtect.get().getLanguageManager().get("general.color") + "------------------------------------");
                        }

                        //wecui
                        if (RedProtect.get().hooks.worldEdit && RedProtect.get().getConfigManager().configRoot().hooks.useWECUI) {
                            WEHook.setSelectionRP(p, newRegion.getMinLocation(), newRegion.getMaxLocation());
                        }

                        this.r = newRegion;
                        RedProtect.get().logger.addLog("(World " + newRegion.getWorld() + ") Player " + p.getName() + " CREATED region " + newRegion.getName());
                        return;
                    }
                }
            } else if (i == 1 && nearbyCount == 2) {
                //check other regions on blocks
                Region rcurrent = RedProtect.get().getRegionManager().getTopRegion(current.getLocation());
                if (rcurrent != null && !rcurrent.canBuild(p)) {
                    setErrorSign(e, RedProtect.get().getLanguageManager().get("regionbuilder.region.overlapping").replace("{location}", "x: " + rcurrent.getCenterX() + ", z: " + rcurrent.getCenterZ()).replace("{player}", rcurrent.getLeadersDesc()));
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
                setErrorSign(e, RedProtect.get().getLanguageManager().get("regionbuilder.area.error").replace("{area}", "(x: " + current.getX() + ", y: " + current.getY() + ", z: " + current.getZ() + ")"));
                Block finalCurrent = current;
                Bukkit.getScheduler().callSyncMethod(RedProtect.get(), () -> {
                    Block newb = finalCurrent.getRelative(BlockFace.UP);
                    if (Material.getMaterial("SIGN_POST") != null) {
                        newb.getState().getBlock().setType(Material.getMaterial("SIGN_POST"));
                    } else {
                        newb.getState().getBlock().setType(Arrays.stream(Material.values()).filter(m -> m.name().endsWith("_SIGN")).findFirst().get());
                    }
                    Sign s = (Sign) newb.getState();
                    s.setLine(0, "§4xxxxxxxxxxxxxx");
                    s.setLine(1, RedProtect.get().getLanguageManager().get("_redprotect.prefix"));
                    s.setLine(2, RedProtect.get().getLanguageManager().get("blocklistener.postsign.error"));
                    s.setLine(3, "§4xxxxxxxxxxxxxx");
                    s.update();
                    return true;
                });
                return;
            }
            if (oldFacing != curFacing && i > 1) {
                px.add(current.getX());
                pz.add(current.getZ());
            }
            last = current;
            if (next == null) {
                setErrorSign(e, RedProtect.get().getLanguageManager().get("regionbuilder.area.next") + " " + RedProtect.get().getConfigManager().configRoot().region_settings.block_id);
                return;
            }
            current = next;
            oldFacing = curFacing;
        }
        String maxsize = String.valueOf(RedProtect.get().getConfigManager().configRoot().region_settings.max_scan);
        setErrorSign(e, RedProtect.get().getLanguageManager().get("regionbuilder.area.toobig").replace("{maxsize}", maxsize));
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
        HashMap<Integer, ItemStack> left = p.getInventory().addItem(new ItemStack(Material.getMaterial(RedProtect.get().getConfigManager().configRoot().region_settings.block_id), blocks.size()));
        if (!left.isEmpty()) {
            p.getWorld().dropItem(p.getLocation(), new ItemStack(Material.getMaterial(RedProtect.get().getConfigManager().configRoot().region_settings.block_id), left.get(0).getAmount() - 1));
        }
        p.updateInventory();
        sign.breakNaturally();
        for (Block rb : blocks) {
            rb.setType(Material.AIR);
        }
    }
}
