/*
 Copyright @FabioZumbi12

 This class is provided 'as-is', without any express or implied warranty. In no event will the authors be held liable for any
  damages arising from the use of this class.

 Permission is granted to anyone to use this class for any purpose, including commercial plugins, and to alter it and
 redistribute it freely, subject to the following restrictions:
 1 - The origin of this class must not be misrepresented; you must not claim that you wrote the original software. If you
 use this class in other plugins, an acknowledgment in the plugin documentation would be appreciated but is not required.
 2 - Altered source versions must be plainly marked as such, and must not be misrepresented as being the original class.
 3 - This notice may not be removed or altered from any source distribution.

 Esta classe é fornecida "como está", sem qualquer garantia expressa ou implícita. Em nenhum caso os autores serão
 responsabilizados por quaisquer danos decorrentes do uso desta classe.

 É concedida permissão a qualquer pessoa para usar esta classe para qualquer finalidade, incluindo plugins pagos, e para
 alterá-lo e redistribuí-lo livremente, sujeito às seguintes restrições:
 1 - A origem desta classe não deve ser deturpada; você não deve afirmar que escreveu a classe original. Se você usar esta
  classe em um plugin, uma confirmação de autoria na documentação do plugin será apreciada, mas não é necessária.
 2 - Versões de origem alteradas devem ser claramente marcadas como tal e não devem ser deturpadas como sendo a
 classe original.
 3 - Este aviso não pode ser removido ou alterado de qualquer distribuição de origem.
 */

package br.net.fabiozumbi12.RedProtect.Sponge.actions;

import br.net.fabiozumbi12.RedProtect.Sponge.*;
import br.net.fabiozumbi12.RedProtect.Sponge.config.RPLang;
import br.net.fabiozumbi12.RedProtect.Sponge.events.CreateRegionEvent;
import br.net.fabiozumbi12.RedProtect.Sponge.hooks.WEListener;
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
        String owner1 = RPUtil.PlayerToUUID(e.getText().asList().get(2).toPlain());
        String owner2 = RPUtil.PlayerToUUID(e.getText().asList().get(3).toPlain());
        World w = e.getTargetTile().getLocation().getExtent();
        BlockSnapshot b = w.createSnapshot(e.getTargetTile().getLocation().getBlockPosition());
        Player p = e.getCause().first(Player.class).get();
        Sign sign = e.getTargetTile();
        String pName = RPUtil.PlayerToUUID(p.getName());
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

        if (!RedProtect.get().cfgs.isAllowedWorld(p)) {
            this.setErrorSign(e, RPLang.get("regionbuilder.region.worldnotallowed"));
            return;
        }

        //filter region name
        regionName = regionName.replace(" ", "_").replaceAll("[^\\p{L}_0-9 ]", "");
        if (regionName == null || regionName.isEmpty() || regionName.length() < 3) {
            regionName = RPUtil.nameGen(p.getName(), p.getWorld().getName());
            if (regionName.length() > 16) {
                this.setErrorSign(e, RPLang.get("regionbuilder.autoname.error"));
                return;
            }
        }

        //region name conform
        if (regionName.length() < 3) {
            RPLang.sendMessage(p, "regionbuilder.regionname.invalid");
            return;
        }

        if (RedProtect.get().rm.getRegion(regionName, p.getWorld()) != null) {
            RPLang.sendMessage(p, "regionbuilder.regionname.existis");
            return;
        }

        int maxby = current.getLocation().get().getBlockY();
        int minby = current.getLocation().get().getBlockY();

        for (int i = 0; i < RedProtect.get().cfgs.root().region_settings.max_scan; ++i) {
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

                boolean validBlock = (block[bi].getState().getType().getName().contains(RedProtect.get().cfgs.root().region_settings.block_id.toLowerCase()));
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
                        if (owner1 == null) {
                            sign.offer(e.getText().set(sign.getValue(Keys.SIGN_LINES).get().set(2, RPUtil.toText("--"))));

                        } else if (pName.equals(owner1)) {
                            sign.offer(e.getText().set(sign.getValue(Keys.SIGN_LINES).get().set(2, RPUtil.toText("--"))));
                            RPLang.sendMessage(p, "regionbuilder.sign.dontneed.name");

                        } else {
                            leaders.add(owner1);
                        }


                        if (owner2 == null) {
                            sign.offer(e.getText().set(sign.getValue(Keys.SIGN_LINES).get().set(3, RPUtil.toText("--"))));
                        } else {
                            if (!(owner2.startsWith("[") && owner2.endsWith("]"))) {
                                if (pName.equals(owner2)) {
                                    sign.offer(e.getText().set(sign.getValue(Keys.SIGN_LINES).get().set(3, RPUtil.toText("--"))));
                                    RPLang.sendMessage(p, "regionbuilder.sign.dontneed.name");
                                } else {
                                    leaders.add(owner2);
                                }
                            } else {
                                sign.offer(e.getText().set(sign.getValue(Keys.SIGN_LINES).get().set(3, RPUtil.toText("--"))));
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

                        int maxy = RedProtect.get().cfgs.root().region_settings.claim.maxy;
                        int miny = RedProtect.get().cfgs.root().region_settings.claim.miny;
                        if (maxy <= -1) {
                            maxy = w.getDimension().getBuildHeight();
                        }
                        if (miny == -1) {
                            miny = 0;
                        }

                        Region region = new Region(regionName, new HashSet<>(), new HashSet<>(), leaders, rx, rz, miny, maxy, 0, w.getName(), RPUtil.DateNow(), RedProtect.get().cfgs.getDefFlagsValues(), "", 0, null, true);

                        List<String> othersName = new ArrayList<>();
                        Region otherrg;
                        List<Location<World>> limitlocs = region.getLimitLocs(minby, maxby, false);

                        //check retangular region
                        for (BlockSnapshot bkloc : blocks) {
                            if (!limitlocs.contains(bkloc.getLocation().get())) {
                                this.setErrorSign(e, RPLang.get("regionbuilder.neeberetangle"));
                                return;
                            }
                        }

                        //check regions inside region
                        for (Region r : RedProtect.get().rm.getRegionsByWorld(w)) {
                            if (r.getMaxMbrX() <= region.getMaxMbrX() && r.getMaxY() <= region.getMaxY() && r.getMaxMbrZ() <= region.getMaxMbrZ() && r.getMinMbrX() >= region.getMinMbrX() && r.getMinY() >= region.getMinY() && r.getMinMbrZ() >= region.getMinMbrZ()) {
                                if (!r.isLeader(p) && !RedProtect.get().ph.hasGenPerm(p, "redprotect.bypass")) {
                                    this.setErrorSign(e, RPLang.get("regionbuilder.region.overlapping").replace("{location}", "x: " + r.getCenterX() + ", z: " + r.getCenterZ()).replace("{player}", RPUtil.UUIDtoPlayer(r.getLeadersDesc())));
                                    return;
                                }
                                if (!othersName.contains(r.getName())) {
                                    othersName.add(r.getName());
                                }
                            }
                        }

                        //check borders for other regions
                        for (Location<World> loc : limitlocs) {
                            otherrg = RedProtect.get().rm.getTopRegion(loc, this.getClass().getName());

                            RedProtect.get().logger.debug(LogLevel.DEFAULT, "protection Block is: " + loc.getBlock().getType().getName());

                            if (otherrg != null) {
                                if (!otherrg.isLeader(p) && !RedProtect.get().ph.hasGenPerm(p, "redprotect.admin")) {
                                    this.setErrorSign(e, RPLang.get("regionbuilder.region.overlapping").replace("{location}", "x: " + otherrg.getCenterX() + ", z: " + otherrg.getCenterZ()).replace("{player}", RPUtil.UUIDtoPlayer(r.getLeadersDesc())));
                                    return;
                                }
                                if (!othersName.contains(otherrg.getName())) {
                                    othersName.add(otherrg.getName());
                                }
                            }
                        }

                        //check if same area
                        otherrg = RedProtect.get().rm.getTopRegion(region.getCenterLoc(), this.getClass().getName());
                        if (otherrg != null && otherrg.get4Points(current.getLocation().get().getBlockY()).equals(region.get4Points(current.getLocation().get().getBlockY()))) {
                            this.setErrorSign(e, RPLang.get("regionbuilder.region.overlapping").replace("{location}", "x: " + otherrg.getCenterX() + ", z: " + otherrg.getCenterZ()).replace("{player}", RPUtil.UUIDtoPlayer(otherrg.getLeadersDesc())));
                            return;
                        }

                        region.setPrior(RPUtil.getUpdatedPrior(region));

                        int claimLimit = RedProtect.get().ph.getPlayerClaimLimit(p);
                        int claimused = RedProtect.get().rm.getPlayerRegions(RPUtil.PlayerToUUID(p.getName()), w);
                        boolean claimUnlimited = RedProtect.get().ph.hasPerm(p, "redprotect.limit.claim.unlimited");
                        if (claimused >= claimLimit && claimLimit != -1) {
                            this.setErrorSign(e, RPLang.get("regionbuilder.claim.limit"));
                            return;
                        }

                        int pLimit = RedProtect.get().ph.getPlayerBlockLimit(p);
                        boolean areaUnlimited = RedProtect.get().ph.hasPerm(p, "redprotect.limit.blocks.unlimited");
                        int totalArea = RedProtect.get().rm.getTotalRegionSize(pName, p.getWorld().getName());
                        int regionarea = RPUtil.simuleTotalRegionSize(RPUtil.PlayerToUUID(p.getName()), region);
                        int actualArea = 0;
                        if (regionarea > 0) {
                            actualArea = totalArea + regionarea;
                        }
                        if (pLimit >= 0 && actualArea > pLimit && !areaUnlimited) {
                            this.setErrorSign(e, RPLang.get("regionbuilder.reach.limit"));
                            return;
                        }

                        if (RedProtect.get().cfgs.getEcoBool("claim-cost-per-block.enable") && !RedProtect.get().ph.hasGenPerm(p, "redprotect.eco.bypass")) {
                            UniqueAccount acc = RedProtect.get().econ.getOrCreateAccount(p.getUniqueId()).get();
                            Double peco = acc.getBalance(RedProtect.get().econ.getDefaultCurrency()).doubleValue();
                            long reco = region.getArea() * RedProtect.get().cfgs.getEcoInt("claim-cost-per-block.cost-per-block");

                            if (!RedProtect.get().cfgs.getEcoBool("claim-cost-per-block.y-is-free")) {
                                reco = reco * Math.abs(region.getMaxY() - region.getMinY());
                            }

                            if (peco >= reco) {
                                acc.withdraw(RedProtect.get().econ.getDefaultCurrency(), BigDecimal.valueOf(reco), RedProtect.get().getPVHelper().getCause(p));
                                p.sendMessage(RPUtil.toText(RPLang.get("economy.region.claimed").replace("{price}", RedProtect.get().cfgs.getEcoString("economy-symbol") + reco + " " + RedProtect.get().cfgs.getEcoString("economy-name"))));
                            } else {
                                this.setErrorSign(e, RPLang.get("regionbuilder.notenought.money").replace("{price}", RedProtect.get().cfgs.getEcoString("economy-symbol") + reco));
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
                                RPLang.get("regionbuilder.area.left") + " " + (areaUnlimited ? RPLang.get("regionbuilder.area.unlimited") : (pLimit - (totalArea + region.getArea())))));
                        p.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.region.priority.set").replace("{region}", region.getName()) + " " + region.getPrior()));
                        p.sendMessage(RPUtil.toText(RPLang.get("general.color") + "------------------------------------"));
                        if (othersName.size() > 0) {
                            p.sendMessage(RPUtil.toText(RPLang.get("regionbuilder.overlapping")));
                            p.sendMessage(RPUtil.toText(RPLang.get("region.regions") + " " + othersName));
                            p.sendMessage(RPUtil.toText(RPLang.get("general.color") + "------------------------------------"));
                        }

                        //Drop types
                        if (owner2 != null && RedProtect.get().cfgs.root().region_settings.claim.modes.allow_player_decide && RPLang.containsValue(owner2)) {
                        	/*if (owner2.equalsIgnoreCase(RPLang.get("region.mode.drop"))){
                        		drop(b , blocks, p);
                        		RPLang.sendMessage(p, "regionbuilder.region.droped");
                        	}*/
                            if (owner2.equalsIgnoreCase(RPLang.get("region.mode.remove"))) {
                                remove(b, blocks, p);
                                RPLang.sendMessage(p, "regionbuilder.region.removed");
                            }
                            if (owner2.equalsIgnoreCase(RPLang.get("region.mode.give"))) {
                                give(b, p, blocks);
                                RPLang.sendMessage(p, "regionbuilder.region.given");
                            }
                        } else {
                        	/*if (RedProtect.get().cfgs.getString("region-settings.claim-modes.mode").equalsIgnoreCase("drop")) {
                                drop(b , blocks, p);
                            }*/
                            if (RedProtect.get().cfgs.root().region_settings.claim.modes.mode.equalsIgnoreCase("remove")) {
                                remove(b, blocks, p);
                            }
                            if (RedProtect.get().cfgs.root().region_settings.claim.modes.mode.equalsIgnoreCase("give")) {
                                give(b, p, blocks);
                            }
                        }

                        if (RedProtect.get().rm.getRegions(RPUtil.PlayerToUUID(p.getName()), p.getWorld()).size() == 0) {
                            p.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.region.firstwarning")));
                            p.sendMessage(RPUtil.toText(RPLang.get("general.color") + "------------------------------------"));
                        }

                        //wecui selection
                        if (RedProtect.get().WE && RedProtect.get().cfgs.root().hooks.useWECUI) {
                            WEListener.setSelectionRP(p, region.getMinLocation(), region.getMaxLocation());
                        }

                        this.r = region;
                        RedProtect.get().logger.addLog("(World " + region.getWorld() + ") Player " + p.getName() + " CREATED region " + region.getName());
                        return;
                    }
                }
            } else if (i == 1 && nearbyCount == 2) {
                //check other regions on blocks
                Region rcurrent = RedProtect.get().rm.getTopRegion(current.getLocation().get(), this.getClass().getName());
                if (rcurrent != null && !rcurrent.canBuild(p)) {
                    this.setErrorSign(e, RPLang.get("regionbuilder.region.overlapping").replace("{location}", "x: " + rcurrent.getCenterX() + ", z: " + rcurrent.getCenterZ()).replace("{player}", RPUtil.UUIDtoPlayer(rcurrent.getLeadersDesc())));
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
                this.setErrorSign(e, RPLang.get("regionbuilder.area.error").replace("{area}", "(x: " + current.getLocation().get().getBlockX() + ", y: " + current.getLocation().get().getBlockY() + ", z: " + current.getLocation().get().getBlockZ() + ")"));
                Location<World> newbl = current.getLocation().get().getBlockRelative(Direction.UP);
                RedProtect.get().getPVHelper().setBlock(newbl, BlockTypes.STANDING_SIGN.getDefaultState());
                Sign errSign = (Sign) newbl.getTileEntity().get();
                SignData data = errSign.getSignData();
                data.get(Keys.SIGN_LINES).get().set(0, Text.of(TextColors.RED, "xxxxxxxxxxxxxx"));
                data.get(Keys.SIGN_LINES).get().set(1, RPUtil.toText(RPLang.get("_redprotect.prefix")));
                data.get(Keys.SIGN_LINES).get().set(2, RPUtil.toText(RPLang.get("blocklistener.postsign.error")));
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
                this.setErrorSign(e, RPLang.get("regionbuilder.area.next"));
                return;
            }
            current = next;
            oldFacing = curFacing;
        }
        String maxsize = String.valueOf(RedProtect.get().cfgs.root().region_settings.max_scan);
        this.setErrorSign(e, RPLang.get("regionbuilder.area.toobig").replace("{maxsize}", maxsize));
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
            RedProtect.get().getPVHelper().removeBlock(rb.getLocation().get());
        }
    }

    private void give(BlockSnapshot sign, Player p, List<BlockSnapshot> blocks) {
        Collection<ItemStackSnapshot> rejected = new ArrayList<>();
        for (BlockSnapshot bb : blocks) {
            rejected.addAll(p.getInventory().offer(ItemStack.builder().fromBlockSnapshot(bb).build()).getRejectedItems());
        }

        //drop rejected
        for (ItemStackSnapshot bb : rejected) {
            RedProtect.get().getPVHelper().digBlock(p, bb.createStack(), p.getLocation().getBlockPosition());
        }

        //p.getWorld().digBlock(sign.getPosition(), Cause.of(NamedCause.simulated(p)));
        for (BlockSnapshot rb : blocks) {
            RedProtect.get().getPVHelper().removeBlock(rb.getLocation().get());
        }
    }
}
