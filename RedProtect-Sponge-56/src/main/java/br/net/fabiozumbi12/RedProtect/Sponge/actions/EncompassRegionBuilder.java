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

package br.net.fabiozumbi12.RedProtect.Sponge.actions;

import br.net.fabiozumbi12.RedProtect.Core.helpers.LogLevel;
import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import br.net.fabiozumbi12.RedProtect.Sponge.Region;
import br.net.fabiozumbi12.RedProtect.Sponge.events.CreateRegionEvent;
import br.net.fabiozumbi12.RedProtect.Sponge.hooks.WEHook;
import br.net.fabiozumbi12.RedProtect.Sponge.region.RegionBuilder;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.math.BigDecimal;
import java.util.*;

public class EncompassRegionBuilder extends RegionBuilder {

    public EncompassRegionBuilder(ChangeSignEvent e) {
        String owner1 = RedProtect.get().getUtil().PlayerToUUID(e.getText().asList().get(2).toPlain());
        String owner2 = RedProtect.get().getUtil().PlayerToUUID(e.getText().asList().get(3).toPlain());
        World w = e.getTargetTile().getLocation().getExtent();
        BlockSnapshot b = w.createSnapshot(e.getTargetTile().getLocation().getBlockPosition());
        Player p = e.getCause().first(Player.class).get();
        Sign sign = e.getTargetTile();
        String pName = RedProtect.get().getUtil().PlayerToUUID(p.getName());
        BlockSnapshot last = b;
        BlockSnapshot current = b;
        BlockSnapshot next = null;
        BlockSnapshot first = null;
        String regionName = e.getText().asList().get(1).toPlain();
        LinkedList<Integer> px = new LinkedList<>();
        LinkedList<Integer> pz = new LinkedList<>();
        BlockSnapshot bFirst1 = null;
        BlockSnapshot bFirst2 = null;
        List<BlockSnapshot> blocks = new LinkedList<>();
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

        int maxby = current.getLocation().get().getBlockY();
        int minby = current.getLocation().get().getBlockY();

        for (int i = 0; i < RedProtect.get().getConfigManager().configRoot().region_settings.max_scan; ++i) {
            int nearbyCount = 0;
            int x = current.getLocation().get().getBlockX();
            int y = current.getLocation().get().getBlockY();
            int z = current.getLocation().get().getBlockZ();

            BlockSnapshot[] block = new BlockSnapshot[6];

            block[0] = w.createSnapshot(x + 1, y, z);
            block[1] = w.createSnapshot(x - 1, y, z);
            block[2] = w.createSnapshot(x, y, z + 1);
            block[3] = w.createSnapshot(x, y, z - 1);
            block[4] = w.createSnapshot(x, y + 1, z);
            block[5] = w.createSnapshot(x, y - 1, z);

            for (int bi = 0; bi < block.length; ++bi) {

                boolean validBlock = (block[bi].getState().getType().getName().contains(RedProtect.get().getConfigManager().configRoot().region_settings.block_id.toLowerCase()));
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
                    if (current.getLocation().get().getBlockY() > maxby) {
                        maxby = current.getLocation().get().getBlockY();
                    }
                    if (current.getLocation().get().getBlockY() < minby) {
                        minby = current.getLocation().get().getBlockY();
                    }

                    if (current.equals(first)) {
                        Set<String> leaders = new HashSet<>();
                        leaders.add(pName);
                        if (owner1 == null || owner1.isEmpty()) {
                            sign.offer(e.getText().set(sign.getValue(Keys.SIGN_LINES).get().set(2, RedProtect.get().getUtil().toText("--"))));

                        } else if (pName.equals(owner1)) {
                            sign.offer(e.getText().set(sign.getValue(Keys.SIGN_LINES).get().set(2, RedProtect.get().getUtil().toText("--"))));
                            RedProtect.get().getLanguageManager().sendMessage(p, "regionbuilder.sign.dontneed.name");

                        } else {
                            leaders.add(owner1);
                        }


                        if (owner2 == null || owner2.isEmpty()) {
                            sign.offer(e.getText().set(sign.getValue(Keys.SIGN_LINES).get().set(3, RedProtect.get().getUtil().toText("--"))));
                        } else {
                            if (!(owner2.startsWith("[") && owner2.endsWith("]"))) {
                                if (pName.equals(owner2)) {
                                    sign.offer(e.getText().set(sign.getValue(Keys.SIGN_LINES).get().set(3, RedProtect.get().getUtil().toText("--"))));
                                    RedProtect.get().getLanguageManager().sendMessage(p, "regionbuilder.sign.dontneed.name");
                                } else {
                                    leaders.add(owner2);
                                }
                            } else {
                                sign.offer(e.getText().set(sign.getValue(Keys.SIGN_LINES).get().set(3, RedProtect.get().getUtil().toText("--"))));
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
                            maxy = w.getDimension().getBuildHeight();
                        }
                        if (miny == -1) {
                            miny = 0;
                        }

                        Region region = new Region(regionName, new HashSet<>(), new HashSet<>(), new HashSet<>(), rx, rz, miny, maxy, 0, w.getName(), RedProtect.get().getUtil().dateNow(), RedProtect.get().getConfigManager().getDefFlagsValues(), "", 0, null, true, true);

                        leaders.forEach(region::addLeader);
                        List<String> othersName = new ArrayList<>();
                        Region otherrg;
                        List<Location<World>> limitlocs = region.getLimitLocs(minby, maxby, false);

                        //check retangular region
                        for (BlockSnapshot bkloc : blocks) {
                            if (!limitlocs.contains(bkloc.getLocation().get())) {
                                this.setErrorSign(e, RedProtect.get().getLanguageManager().get("regionbuilder.neeberetangle"));
                                return;
                            }
                        }

                        //check regions inside region
                        for (Region r : RedProtect.get().getRegionManager().getRegionsByWorld(w.getName())) {
                            if (r.getMaxMbrX() <= region.getMaxMbrX() && r.getMaxY() <= region.getMaxY() && r.getMaxMbrZ() <= region.getMaxMbrZ() && r.getMinMbrX() >= region.getMinMbrX() && r.getMinY() >= region.getMinY() && r.getMinMbrZ() >= region.getMinMbrZ()) {
                                if (!r.isLeader(p) && !p.hasPermission("redprotect.bypass")) {
                                    this.setErrorSign(e, RedProtect.get().getLanguageManager().get("regionbuilder.region.overlapping").replace("{location}", "x: " + r.getCenterX() + ", z: " + r.getCenterZ()).replace("{player}", r.getLeadersDesc()));
                                    return;
                                }
                                if (!othersName.contains(r.getName())) {
                                    othersName.add(r.getName());
                                }
                            }
                        }

                        //check borders for other regions
                        for (Location<World> loc : limitlocs) {
                            otherrg = RedProtect.get().getRegionManager().getTopRegion(loc, this.getClass().getName());

                            RedProtect.get().logger.debug(LogLevel.DEFAULT, "protection Block is: " + loc.getBlock().getType().getName());

                            if (otherrg != null) {
                                if (!otherrg.isLeader(p) && !p.hasPermission("redprotect.admin")) {
                                    this.setErrorSign(e, RedProtect.get().getLanguageManager().get("regionbuilder.region.overlapping").replace("{location}", "x: " + otherrg.getCenterX() + ", z: " + otherrg.getCenterZ()).replace("{player}", r.getLeadersDesc()));
                                    return;
                                }
                                if (!othersName.contains(otherrg.getName())) {
                                    othersName.add(otherrg.getName());
                                }
                            }
                        }

                        //check if same area
                        otherrg = RedProtect.get().getRegionManager().getTopRegion(region.getCenterLoc(), this.getClass().getName());
                        if (otherrg != null && otherrg.get4Points(current.getLocation().get().getBlockY()).equals(region.get4Points(current.getLocation().get().getBlockY()))) {
                            this.setErrorSign(e, RedProtect.get().getLanguageManager().get("regionbuilder.region.overlapping").replace("{location}", "x: " + otherrg.getCenterX() + ", z: " + otherrg.getCenterZ()).replace("{player}", otherrg.getLeadersDesc()));
                            return;
                        }

                        region.setPrior(RedProtect.get().getUtil().getUpdatedPrior(region));

                        int claimLimit = RedProtect.get().getPermissionHandler().getPlayerClaimLimit(p);
                        int claimUsed = RedProtect.get().getRegionManager().getPlayerRegions(p.getUniqueId().toString(), w.getName());
                        boolean claimUnlimited = RedProtect.get().getPermissionHandler().hasPerm(p, "redprotect.limits.claim.unlimited");
                        if (claimUsed >= claimLimit && claimLimit != -1) {
                            this.setErrorSign(e, RedProtect.get().getLanguageManager().get("regionbuilder.claim.limit"));
                            return;
                        }

                        int pLimit = RedProtect.get().getPermissionHandler().getPlayerBlockLimit(p);
                        boolean areaUnlimited = RedProtect.get().getPermissionHandler().hasPerm(p, "redprotect.limits.blocks.unlimited");
                        int totalArea = RedProtect.get().getRegionManager().getTotalRegionSize(pName, p.getWorld().getName());
                        int regionArea = RedProtect.get().getUtil().simuleTotalRegionSize(p.getUniqueId().toString(), region);
                        int actualArea = 0;
                        if (regionArea > 0) {
                            actualArea = totalArea + regionArea;
                        }
                        if (pLimit >= 0 && actualArea > pLimit && !areaUnlimited) {
                            this.setErrorSign(e, RedProtect.get().getLanguageManager().get("regionbuilder.reach.limit"));
                            return;
                        }

                        long reco = 0;
                        if (RedProtect.get().getConfigManager().ecoRoot().claim_cost_per_block.enable && !p.hasPermission("redprotect.eco.bypass")) {
                            UniqueAccount acc = RedProtect.get().economy.getOrCreateAccount(p.getUniqueId()).get();
                            double peco = acc.getBalance(RedProtect.get().economy.getDefaultCurrency()).doubleValue();
                            reco = region.getArea() * RedProtect.get().getConfigManager().ecoRoot().claim_cost_per_block.cost_per_block;

                            if (!RedProtect.get().getConfigManager().ecoRoot().claim_cost_per_block.y_is_free) {
                                reco = reco * Math.abs(region.getMaxY() - region.getMinY());
                            }

                            if (peco >= reco) {
                                acc.withdraw(RedProtect.get().economy.getDefaultCurrency(), BigDecimal.valueOf(reco), RedProtect.get().getVersionHelper().getCause(p));
                                p.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().getLanguageManager().get("economy.region.claimed").replace("{price}", RedProtect.get().getConfigManager().ecoRoot().economy_symbol + reco + " " + RedProtect.get().getConfigManager().ecoRoot().economy_name)));
                            } else {
                                this.setErrorSign(e, RedProtect.get().getLanguageManager().get("regionbuilder.notenought.money").replace("{price}", RedProtect.get().getConfigManager().ecoRoot().economy_symbol + reco));
                                return;
                            }
                        }

                        //fire event
                        CreateRegionEvent event = new CreateRegionEvent(r, p);
                        if (Sponge.getEventManager().post(event)) {
                            return;
                        }

                        p.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().getLanguageManager().get("general.color") + "------------------------------------"));
                        p.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().getLanguageManager().get("regionbuilder.claim.left") + (claimUsed + 1) + RedProtect.get().getLanguageManager().get("general.color") + "/" + (claimUnlimited ? RedProtect.get().getLanguageManager().get("regionbuilder.area.unlimited") : claimLimit)));
                        p.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().getLanguageManager().get("regionbuilder.area.used") + " " + (regionArea == 0 ? "&a" + regionArea : "&c- " + regionArea) + "\n" +
                                RedProtect.get().getLanguageManager().get("regionbuilder.area.left") + " " + (areaUnlimited ? RedProtect.get().getLanguageManager().get("regionbuilder.area.unlimited") : (pLimit - (totalArea + region.getArea())))));
                        p.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().getLanguageManager().get("cmdmanager.region.priority.set").replace("{region}", region.getName()) + " " + region.getPrior()));
                        if (RedProtect.get().getConfigManager().ecoRoot().claim_cost_per_block.enable)
                            p.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().getLanguageManager().get("regionbuilder.block.cost") + reco));
                        p.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().getLanguageManager().get("general.color") + "------------------------------------"));
                        if (othersName.size() > 0) {
                            p.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().getLanguageManager().get("regionbuilder.overlapping")));
                            p.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().getLanguageManager().get("region.regions") + " " + othersName));
                            p.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().getLanguageManager().get("general.color") + "------------------------------------"));
                        }

                        //Drop types
                        if (owner2 != null && RedProtect.get().getConfigManager().configRoot().region_settings.claim.modes.allow_player_decide && RedProtect.get().getLanguageManager().containsValue(owner2)) {
                        	/*if (owner2.equalsIgnoreCase(RedProtect.get().getLanguageManager().get("region.mode.drop"))){
                        		drop(b , blocks, p);
                        		RedProtect.get().getLanguageManager().sendMessage(p, "regionbuilder.region.droped");
                        	}*/
                            if (owner2.equalsIgnoreCase(RedProtect.get().getLanguageManager().get("region.mode.remove"))) {
                                remove(b, blocks, p);
                                RedProtect.get().getLanguageManager().sendMessage(p, "regionbuilder.region.removed");
                            }
                            if (owner2.equalsIgnoreCase(RedProtect.get().getLanguageManager().get("region.mode.give"))) {
                                give(b, p, blocks);
                                RedProtect.get().getLanguageManager().sendMessage(p, "regionbuilder.region.given");
                            }
                        } else {
                        	/*if (RedProtect.get().getConfigManager().getString("region-settings.claim-modes.mode").equalsIgnoreCase("drop")) {
                                drop(b , blocks, p);
                            }*/
                            if (RedProtect.get().getConfigManager().configRoot().region_settings.claim.modes.mode.equalsIgnoreCase("remove")) {
                                remove(b, blocks, p);
                            }
                            if (RedProtect.get().getConfigManager().configRoot().region_settings.claim.modes.mode.equalsIgnoreCase("give")) {
                                give(b, p, blocks);
                            }
                        }

                        if (RedProtect.get().getRegionManager().getRegions(p.getUniqueId().toString(), p.getWorld().getName()).size() == 0) {
                            p.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().getLanguageManager().get("cmdmanager.region.firstwarning")));
                            p.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().getLanguageManager().get("general.color") + "------------------------------------"));
                        }

                        //wecui selection
                        if (RedProtect.get().hooks.WE && RedProtect.get().getConfigManager().configRoot().hooks.useWECUI) {
                            WEHook.setSelectionRP(p, region.getMinLocation(), region.getMaxLocation());
                        }

                        this.r = region;
                        RedProtect.get().logger.addLog("(World " + region.getWorld() + ") Player " + p.getName() + " CREATED region " + region.getName());
                        return;
                    }
                }
            } else if (i == 1 && nearbyCount == 2) {
                //check other regions on blocks
                Region rcurrent = RedProtect.get().getRegionManager().getTopRegion(current.getLocation().get(), this.getClass().getName());
                if (rcurrent != null && !rcurrent.canBuild(p)) {
                    this.setErrorSign(e, RedProtect.get().getLanguageManager().get("regionbuilder.region.overlapping").replace("{location}", "x: " + rcurrent.getCenterX() + ", z: " + rcurrent.getCenterZ()).replace("{player}", rcurrent.getLeadersDesc()));
                    return;
                }
                blocks.add(current);
                first = current;
                int x2 = bFirst1.getLocation().get().getBlockX();
                int z2 = bFirst1.getLocation().get().getBlockZ();
                int x3 = bFirst2.getLocation().get().getBlockX();
                int z3 = bFirst2.getLocation().get().getBlockZ();
                int distx = Math.abs(x2 - x3);
                int distz = Math.abs(z2 - z3);
                if ((distx != 2 || distz != 0) && (distz != 2 || distx != 0)) {
                    px.add(current.getLocation().get().getBlockX());
                    pz.add(current.getLocation().get().getBlockZ());
                }
            } else if (i != 0) {
                this.setErrorSign(e, RedProtect.get().getLanguageManager().get("regionbuilder.area.error").replace("{area}", "(x: " + current.getLocation().get().getBlockX() + ", y: " + current.getLocation().get().getBlockY() + ", z: " + current.getLocation().get().getBlockZ() + ")"));
                Location<World> newbl = current.getLocation().get().getBlockRelative(Direction.UP);
                RedProtect.get().getVersionHelper().setBlock(newbl, BlockTypes.STANDING_SIGN.getDefaultState());
                Sign errSign = (Sign) newbl.getTileEntity().get();
                SignData data = errSign.getSignData();
                data.get(Keys.SIGN_LINES).get().set(0, Text.of(TextColors.RED, "xxxxxxxxxxxxxx"));
                data.get(Keys.SIGN_LINES).get().set(1, RedProtect.get().getUtil().toText(RedProtect.get().getLanguageManager().get("_redprotect.prefix")));
                data.get(Keys.SIGN_LINES).get().set(2, RedProtect.get().getUtil().toText(RedProtect.get().getLanguageManager().get("blocklistener.postsign.error")));
                data.get(Keys.SIGN_LINES).get().set(3, Text.of(TextColors.RED, "xxxxxxxxxxxxxx"));
                errSign.offer(data);
                return;
            }
            if (oldFacing != curFacing && i > 1) {
                px.add(current.getLocation().get().getBlockX());
                pz.add(current.getLocation().get().getBlockZ());
            }
            last = current;
            if (next == null) {
                this.setErrorSign(e, RedProtect.get().getLanguageManager().get("regionbuilder.area.next"));
                return;
            }
            current = next;
            oldFacing = curFacing;
        }
        String maxsize = String.valueOf(RedProtect.get().getConfigManager().configRoot().region_settings.max_scan);
        this.setErrorSign(e, RedProtect.get().getLanguageManager().get("regionbuilder.area.toobig").replace("{maxsize}", maxsize));
    }
    

    /*private void drop(BlockSnapshot sign, List<BlockSnapshot> blocks, Player p){
    	p.getWorld().digBlock(sign.getPosition(), Cause.of(NamedCause.owner(RedProtect.get().plugin)));
        for (BlockSnapshot rb : blocks) {
        	p.getWorld().digBlock(rb.getPosition(), Cause.of(NamedCause.owner(RedProtect.get().plugin)));
        }
    }*/

    private void remove(BlockSnapshot sign, List<BlockSnapshot> blocks, Player p) {
        //p.getWorld().digBlock(sign.getPosition(), Cause.of(NamedCause.owner(RedProtect.get().plugin)));
        for (BlockSnapshot rb : blocks) {
            RedProtect.get().getVersionHelper().removeBlock(rb.getLocation().get());
        }
    }

    private void give(BlockSnapshot sign, Player p, List<BlockSnapshot> blocks) {
        Collection<ItemStackSnapshot> rejected = new ArrayList<>();
        for (BlockSnapshot bb : blocks) {
            rejected.addAll(p.getInventory().offer(ItemStack.builder().fromBlockSnapshot(bb).build()).getRejectedItems());
        }

        //drop rejected
        for (ItemStackSnapshot bb : rejected) {
            RedProtect.get().getVersionHelper().digBlock(p, bb.createStack(), p.getLocation().getBlockPosition());
        }

        //p.getWorld().digBlock(sign.getPosition(), Cause.of(NamedCause.simulated(p)));
        for (BlockSnapshot rb : blocks) {
            RedProtect.get().getVersionHelper().removeBlock(rb.getLocation().get());
        }
    }
}
