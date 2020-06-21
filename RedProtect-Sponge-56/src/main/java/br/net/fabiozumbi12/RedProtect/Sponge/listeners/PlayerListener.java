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

package br.net.fabiozumbi12.RedProtect.Sponge.listeners;

import br.net.fabiozumbi12.RedProtect.Core.helpers.LogLevel;
import br.net.fabiozumbi12.RedProtect.Core.region.PlayerRegion;
import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import br.net.fabiozumbi12.RedProtect.Sponge.Region;
import br.net.fabiozumbi12.RedProtect.Sponge.events.EnterExitRegionEvent;
import br.net.fabiozumbi12.RedProtect.Sponge.helpers.ContainerManager;
import br.net.fabiozumbi12.RedProtect.Sponge.helpers.DoorManager;
import br.net.fabiozumbi12.RedProtect.Sponge.hooks.WEHook;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.boss.BossBarColors;
import org.spongepowered.api.boss.BossBarOverlays;
import org.spongepowered.api.boss.ServerBossBar;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.*;
import org.spongepowered.api.entity.hanging.Hanging;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.entity.projectile.ThrownPotion;
import org.spongepowered.api.entity.projectile.source.ProjectileSource;
import org.spongepowered.api.entity.vehicle.Boat;
import org.spongepowered.api.entity.vehicle.minecart.Minecart;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.CollideBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.event.cause.entity.damage.source.BlockDamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.IndirectEntityDamageSource;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.entity.*;
import org.spongepowered.api.event.entity.projectile.LaunchProjectileEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.event.item.inventory.UseItemStackEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.event.world.ConstructPortalEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@SuppressWarnings("deprecation")
public class PlayerListener {

    private static final ContainerManager cont = new ContainerManager();
    private final HashMap<Player, String> Ownerslist = new HashMap<>();
    private final HashMap<Player, String> PlayerCmd = new HashMap<>();
    private final HashMap<String, String> PlayertaskID = new HashMap<>();

    public PlayerListener() {
        RedProtect.get().logger.debug(LogLevel.PLAYER, "Loaded PlayerListener...");
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPressPlateChange(CollideBlockEvent e, @First Player p) {
        if (e.getTargetBlock().getName().contains("pressure_plate")) {
            Location<World> loc = e.getTargetLocation();
            Region r = RedProtect.get().rm.getTopRegion(loc, this.getClass().getName());
            if (r != null && !r.allowPressPlate(p)) {
                e.setCancelled(true);
                RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantpressplate");
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onConsume(UseItemStackEvent.Start e, @First Player p) {
        ItemStack stack = e.getItemStackInUse().createStack();
        RedProtect.get().logger.debug(LogLevel.PLAYER, "Is UseItemStackEvent.Start event. Item: " + RedProtect.get().getVersionHelper().getItemType(stack).getName());

        //deny potion
        List<String> Pots = RedProtect.get().config.globalFlagsRoot().worlds.get(p.getWorld().getName()).deny_potions;

        if (stack.get(Keys.POTION_EFFECTS).isPresent() && Pots.size() > 0) {
            List<PotionEffect> pot = stack.get(Keys.POTION_EFFECTS).get();
            for (PotionEffect pots : pot) {
                if (Pots.contains(pots.getType().getName().toUpperCase()) && !p.hasPermission("redprotect.bypass")) {
                    e.setCancelled(true);
                    RedProtect.get().lang.sendMessage(p, "playerlistener.denypotion");
                }
            }
        }

        Region r = RedProtect.get().rm.getTopRegion(p.getLocation(), this.getClass().getName());

        if (r != null && RedProtect.get().getVersionHelper().getItemType(stack).equals(ItemTypes.POTION) && !r.usePotions(p)) {
            RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantuse");
            e.setCancelled(true);
        }

        if (r != null && RedProtect.get().getVersionHelper().getItemType(stack).getName().equals("minecraft:chorus_fruit") && !r.canTeleport(p)) {
            RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantuse");
            e.setCancelled(true);
        }
    }

    //listen left click
    @Listener(order = Order.FIRST)
    public void onInteractLeft(InteractBlockEvent.Primary event, @First Player p) {
        BlockSnapshot b = event.getTargetBlock();
        Location<World> l;

        RedProtect.get().logger.debug(LogLevel.PLAYER, "PlayerListener - Is InteractBlockEvent.Primary event");

        if (!b.getState().getType().equals(BlockTypes.AIR)) {
            l = b.getLocation().get();
            RedProtect.get().logger.debug(LogLevel.PLAYER, "PlayerListener - Is InteractBlockEvent.Primary event. The block is " + b.getState().getType().getName());
        } else {
            l = p.getLocation();
        }

        ItemType itemInHand = RedProtect.get().getVersionHelper().getItemInHand(p);

        String claimmode = RedProtect.get().config.getWorldClaimType(p.getWorld().getName());
        if (event instanceof InteractBlockEvent.Primary.MainHand && itemInHand.getId().equalsIgnoreCase(RedProtect.get().config.configRoot().wands.adminWandID) && ((claimmode.equalsIgnoreCase("WAND") || claimmode.equalsIgnoreCase("BOTH")) || RedProtect.get().ph.hasPerm(p, "redprotect.admin.claim"))) {
            if (!RedProtect.get().getUtil().canBuildNear(p, l)) {
                event.setCancelled(true);
                return;
            }
            RedProtect.get().firstLocationSelections.put(p, l);
            p.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("playerlistener.wand1") + RedProtect.get().lang.get("general.color") + " (&e" + l.getBlockX() + RedProtect.get().lang.get("general.color") + ", &e" + l.getBlockY() + RedProtect.get().lang.get("general.color") + ", &e" + l.getBlockZ() + RedProtect.get().lang.get("general.color") + ")."));
            event.setCancelled(true);

            //show preview border
            previewSelection(p);
        }
    }

    //listen right click
    @Listener(order = Order.FIRST)
    public void onInteractRight(InteractBlockEvent.Secondary event, @First Player p) {
        BlockSnapshot b = event.getTargetBlock();
        Location<World> l;

        RedProtect.get().logger.debug(LogLevel.PLAYER, "PlayerListener - Is InteractBlockEvent.Secondary event");

        if (!b.getState().getType().equals(BlockTypes.AIR)) {
            l = b.getLocation().get();
            RedProtect.get().logger.debug(LogLevel.PLAYER, "PlayerListener - Is InteractBlockEvent.Secondary event. The block is " + b.getState().getType().getName());
        } else {
            l = p.getLocation();
        }

        Region r = RedProtect.get().rm.getTopRegion(l, this.getClass().getName());
        ItemType itemInHand = RedProtect.get().getVersionHelper().getItemInHand(p);

        String claimmode = RedProtect.get().config.getWorldClaimType(p.getWorld().getName());
        if (event instanceof InteractBlockEvent.Secondary.MainHand && itemInHand.getId().equalsIgnoreCase(RedProtect.get().config.configRoot().wands.adminWandID) && ((claimmode.equalsIgnoreCase("WAND") || claimmode.equalsIgnoreCase("BOTH")) || RedProtect.get().ph.hasPerm(p, "redprotect.admin.claim"))) {
            if (!RedProtect.get().getUtil().canBuildNear(p, l)) {
                event.setCancelled(true);
                return;
            }
            RedProtect.get().secondLocationSelections.put(p, l);
            p.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("playerlistener.wand2") + RedProtect.get().lang.get("general.color") + " (&e" + l.getBlockX() + RedProtect.get().lang.get("general.color") + ", &e" + l.getBlockY() + RedProtect.get().lang.get("general.color") + ", &e" + l.getBlockZ() + RedProtect.get().lang.get("general.color") + ")."));
            event.setCancelled(true);

            //show preview border
            previewSelection(p);
            return;
        }

        //other blocks and interactions
        if (r != null) {
            if ((itemInHand.equals(ItemTypes.ENDER_PEARL) || itemInHand.getName().equals("minecraft:chorus_fruit")) && !r.canTeleport(p)) {
                RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantuse");
                event.setUseItemResult(Tristate.FALSE);
                event.setCancelled(true);
            } else if ((itemInHand.equals(ItemTypes.BOW) || itemInHand.equals(ItemTypes.SNOWBALL) || itemInHand.equals(ItemTypes.EGG)) && !r.canProtectiles(p)) {
                RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantuse");
                event.setUseItemResult(Tristate.FALSE);
                event.setCancelled(true);
            } else if (itemInHand.equals(ItemTypes.POTION) && !r.usePotions(p)) {
                RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantuse");
                event.setUseItemResult(Tristate.FALSE);
                event.setCancelled(true);
            } else if (itemInHand.equals(ItemTypes.MONSTER_EGG) && !r.canInteractPassives(p)) {
                RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantuse");
                event.setUseItemResult(Tristate.FALSE);
                event.setCancelled(true);
            } else if ((itemInHand.equals(ItemTypes.BOAT) || itemInHand.getType().getName().contains("_minecart")) && !r.canMinecart(p)) {
                RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantuse");
                event.setUseItemResult(Tristate.FALSE);
                event.setCancelled(true);
            }
        }
    }

    private void previewSelection(Player p) {
        if (RedProtect.get().firstLocationSelections.containsKey(p) && RedProtect.get().secondLocationSelections.containsKey(p)) {
            Location<World> loc1 = RedProtect.get().firstLocationSelections.get(p);
            Location<World> loc2 = RedProtect.get().secondLocationSelections.get(p);

            int area = new Region("", loc1, loc2, p.getWorld().getName()).getArea();
            RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("cmdmanager.region.distance") + area);

            if (RedProtect.get().hooks.WE && RedProtect.get().config.configRoot().hooks.useWECUI) {
                WEHook.setSelectionRP(p, loc1, loc2);
            }

            if (area > RedProtect.get().config.configRoot().region_settings.max_scan && !RedProtect.get().ph.hasPerm(p, "redprotect.bypass.define-max-distance")) {
                RedProtect.get().lang.sendMessage(p, String.format(RedProtect.get().lang.get("regionbuilder.selection.maxdefine"), RedProtect.get().config.configRoot().region_settings.max_scan, area));
            } else {
                RedProtect.get().getUtil().addBorder(p, loc1, loc2);
            }
        }
    }

    //listen all
    @Listener(order = Order.FIRST)
    public void onInteractBlock(InteractBlockEvent event, @First Player p) {
        BlockSnapshot b = event.getTargetBlock();
        BlockState bstate = b.getState();
        Location<World> l;

        if (!b.getState().getType().equals(BlockTypes.AIR)) {
            l = b.getLocation().get();
            RedProtect.get().logger.debug(LogLevel.PLAYER, "PlayerListener - Is InteractBlockEvent event. The block is " + bstate.getType().getName());
        } else {
            l = p.getLocation();
        }

        Region r = RedProtect.get().rm.getTopRegion(l, this.getClass().getName());
        ItemType itemInHand = ItemTypes.NONE;
        ItemStack stack;
        if (!RedProtect.get().getVersionHelper().getItemMainHand(p).isEmpty()) {
            stack = RedProtect.get().getVersionHelper().getItemMainHand(p);
            itemInHand = RedProtect.get().getVersionHelper().getItemType(stack);
            if (RedProtect.get().getUtil().removeGuiItem(stack)) {
                p.setItemInHand(HandTypes.MAIN_HAND, ItemStack.of(ItemTypes.NONE, 1));
                event.setCancelled(true);
            }
        } else if (!RedProtect.get().getVersionHelper().getItemOffHand(p).isEmpty()) {
            stack = RedProtect.get().getVersionHelper().getItemOffHand(p);
            itemInHand = RedProtect.get().getVersionHelper().getItemType(stack);
            if (RedProtect.get().getUtil().removeGuiItem(stack)) {
                p.setItemInHand(HandTypes.OFF_HAND, ItemStack.of(ItemTypes.NONE, 1));
                event.setCancelled(true);
            }
        }

        if (itemInHand.getId().equalsIgnoreCase(RedProtect.get().config.configRoot().wands.infoWandID)) {
            r = RedProtect.get().rm.getTopRegion(l, this.getClass().getName());
            if (r == null) {
                RedProtect.get().lang.sendMessage(p, "playerlistener.noregion.atblock");
            } else if (RedProtect.get().ph.hasRegionPermMember(p, "infowand", r)) {
                p.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("general.color") + "--------------- [&e" + r.getName() + RedProtect.get().lang.get("general.color") + "] ---------------"));
                p.sendMessage(r.info());
                p.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("general.color") + "-----------------------------------------"));
            } else {
                p.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("playerlistener.region.entered").replace("{region}", r.getName()).replace("{leaders}", r.getLeadersDesc())));
            }
            event.setCancelled(true);
            return;
        }

        //start player checks
        if (r == null) {
            if (bstate instanceof Container ||
                    RedProtect.get().config.configRoot().private_cat.allowed_blocks.stream().anyMatch(bstate.getType().getName()::matches)) {
                boolean out = RedProtect.get().config.configRoot().private_cat.allow_outside;
                if (out && !cont.canOpen(b, p)) {
                    if (!RedProtect.get().ph.hasPerm(p, "redprotect.bypass")) {
                        RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantopen");
                        event.setCancelled(true);
                    } else {
                        int x = b.getLocation().get().getBlockX();
                        int y = b.getLocation().get().getBlockY();
                        int z = b.getLocation().get().getBlockZ();
                        RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("playerlistener.region.opened").replace("{region}", "X:" + x + " Y:" + y + " Z:" + z));
                    }
                }
            }

        } else { //if r != null >>       	

            //if (r != null) && (b != null) >>
            if (b.getState().getType().equals(BlockTypes.LOG) || b.getState().getType().equals(BlockTypes.LOG2) ||
                    b.getState().getType().equals(BlockTypes.LEAVES) || b.getState().getType().equals(BlockTypes.LEAVES2)) {
                if (!r.canTree(b) && !r.canBuild(p)) {
                    event.setCancelled(true);
                    RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantinteract");
                }
            } else if (b.getState().getType().getName().contains("_pressure_plate")) {
                if (!r.allowPressPlate(p)) {
                    event.setCancelled(true);
                    RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantpressplate");
                }
            } else if (bstate.getType().equals(BlockTypes.DRAGON_EGG) ||
                    bstate.getType().equals(BlockTypes.BED) ||
                    bstate.getType().equals(BlockTypes.NOTEBLOCK) ||
                    bstate.getType().getName().contains("repeater") ||
                    bstate.getType().getName().contains("comparator")) {

                if (!r.canBuild(p)) {
                    RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantinteract");
                    event.setCancelled(true);
                }
            } else if (l.getTileEntity().isPresent() && l.getTileEntity().get() instanceof Sign && RedProtect.get().config.configRoot().region_settings.enable_flag_sign) {
                Sign s = (Sign) l.getTileEntity().get();
                if (s.lines().get(0).toPlain().equalsIgnoreCase("[flag]") && r.getFlags().containsKey(s.lines().get(1).toPlain())) {
                    String flag = s.lines().get(1).toPlain();
                    if (!(r.getFlags().get(flag) instanceof Boolean)) {
                        RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("playerlistener.region.sign.cantflag"));
                        return;
                    }
                    if (RedProtect.get().ph.hasFlagPerm(p, flag) && (RedProtect.get().config.configRoot().flags.containsKey(flag) || RedProtect.get().config.AdminFlags.contains(flag))) {
                        if (r.isAdmin(p) || r.isLeader(p) || RedProtect.get().ph.hasPerm(p, "redprotect.admin.flag." + flag)) {
                            if (RedProtect.get().config.configRoot().flags_configuration.change_flag_delay.enable) {
                                if (RedProtect.get().config.configRoot().flags_configuration.change_flag_delay.flags.contains(flag)) {
                                    if (!RedProtect.get().changeWait.contains(r.getName() + flag)) {
                                        RedProtect.get().getUtil().startFlagChanger(r.getName(), flag, p);
                                        changeFlag(r, flag, p, s);
                                        return;
                                    } else {
                                        RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("gui.needwait.tochange").replace("{seconds}", "" + RedProtect.get().config.configRoot().flags_configuration.change_flag_delay.seconds));
                                        return;
                                    }
                                }
                            }
                            changeFlag(r, flag, p, s);
                            return;
                        }
                    }
                    RedProtect.get().lang.sendMessage(p, "cmdmanager.region.flag.nopermregion");
                }
            } else if (bstate.getType().equals(BlockTypes.ENDER_CHEST)) {
                if (!r.canEnderChest(p)) {
                    RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantopen");
                    event.setCancelled(true);
                }
            } else if (bstate.getType().getName().contains("spawner")) {
                if (!r.canPlaceSpawner(p)) {
                    RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantuse");
                    event.setCancelled(true);
                }
            } else if (b.getState() instanceof Container ||
                    RedProtect.get().config.configRoot().private_cat.allowed_blocks.stream().anyMatch(bstate.getType().getName()::matches)) {
                if ((r.canChest(p) && !cont.canOpen(b, p) || (!r.canChest(p) && cont.canOpen(b, p)) || (!r.canChest(p) && !cont.canOpen(b, p)))) {
                    RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantopen");
                    event.setCancelled(true);
                }
            } else if (bstate.getType().getName().contains("lever")) {
                if (!r.canLever(p)) {
                    RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantlever");
                    event.setCancelled(true);
                }
            } else if (bstate.getType().getName().contains("redstone") || bstate.getType().getName().contains("daylight") || bstate.getType().getName().contains("comparator") || bstate.getType().getName().contains("repeater")) {
                if (!r.canRedstone(p)) {
                    RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantinteract");
                    event.setCancelled(true);
                }
            } else if (bstate.getType().getName().contains("button")) {
                if (!r.canButton(p)) {
                    RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantbutton");
                    event.setCancelled(true);
                }
            } else if (DoorManager.isOpenable(b)) {
                if (!r.canDoor(p)/* || (r.canDoor(p) && !cont.canOpen(b, p))*/) {
                    RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantdoor");
                    event.setCancelled(true);
                } else {
                    DoorManager.ChangeDoor(b, r);
                }
            } else if (bstate.getType().getName().contains("rail")) {
                if (!r.canMinecart(p)) {
                    RedProtect.get().lang.sendMessage(p, "blocklistener.region.cantplace");
                    event.setCancelled(true);
                }
            } else if (bstate.getType().getName().contains("sign") && event instanceof InteractBlockEvent.Secondary) {
                Sign sign = (Sign) b.getLocation().get().getTileEntity().get();
                if (sign.get(Keys.SIGN_LINES).isPresent() && sign.get(Keys.SIGN_LINES).get().get(0).toPlain().equalsIgnoreCase("[flag]") && r.getFlags().containsKey(sign.get(Keys.SIGN_LINES).get().get(1).toPlain())) {
                    String flag = sign.get(Keys.SIGN_LINES).get().get(1).toPlain();
                    if (!(r.getFlags().get(flag) instanceof Boolean)) {
                        RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("playerlistener.region.sign.cantflag"));
                        return;
                    }
                    if (RedProtect.get().ph.hasFlagPerm(p, flag) && (RedProtect.get().config.configRoot().flags.containsKey(flag) || RedProtect.get().config.AdminFlags.contains(flag))) {
                        if (r.isAdmin(p) || r.isLeader(p) || RedProtect.get().ph.hasPerm(p, "redprotect.admin.flag." + flag)) {
                            if (RedProtect.get().config.configRoot().flags_configuration.change_flag_delay.enable) {
                                if (RedProtect.get().config.configRoot().flags_configuration.change_flag_delay.flags.contains(flag)) {
                                    if (!RedProtect.get().changeWait.contains(r.getName() + flag)) {
                                        RedProtect.get().getUtil().startFlagChanger(r.getName(), flag, p);
                                        changeFlag(r, flag, p, sign);
                                        return;
                                    } else {
                                        RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("gui.needwait.tochange").replace("{seconds}", "" + RedProtect.get().config.configRoot().flags_configuration.change_flag_delay.seconds));
                                        return;
                                    }
                                }
                            }
                            changeFlag(r, flag, p, sign);
                            return;
                        }
                    }
                    RedProtect.get().lang.sendMessage(p, "cmdmanager.region.flag.nopermregion");
                    event.setCancelled(true);
                } else if (!r.canSign(p)) {
                    if (b.get(Keys.SIGN_LINES).isPresent()) {
                        List<Text> lines = b.get(Keys.SIGN_LINES).get();
                        for (String tag : RedProtect.get().config.configRoot().region_settings.allow_sign_interact_tags) {
                            //check first rule
                            if (tag.equalsIgnoreCase(lines.get(0).toPlain())) {
                                return;
                            }

                            //check if tag is leaders or members names
                            if (tag.equalsIgnoreCase("{membername}")) {
                                for (PlayerRegion leader : r.getLeaders()) {
                                    if (lines.get(0).toPlain().equalsIgnoreCase(leader.getPlayerName())) {
                                        return;
                                    }
                                }
                                for (PlayerRegion member : r.getMembers()) {
                                    if (lines.get(0).toPlain().equalsIgnoreCase(member.getPlayerName())) {
                                        return;
                                    }
                                }
                                for (PlayerRegion admin : r.getAdmins()) {
                                    if (lines.get(0).toPlain().equalsIgnoreCase(admin.getPlayerName())) {
                                        return;
                                    }
                                }
                            }

                            //check if tag is player name
                            if (tag.equalsIgnoreCase("{playername}")) {
                                if (lines.get(0).toPlain().equalsIgnoreCase(RedProtect.get().getUtil().UUIDtoPlayer(p.getName()))) {
                                    return;
                                }
                            }
                        }
                    }
                    RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantinteract.signs");
                    event.setCancelled(true);
                }
            } else if ((itemInHand.equals(ItemTypes.FLINT_AND_STEEL) ||
                    itemInHand.equals(ItemTypes.WATER_BUCKET) ||
                    itemInHand.equals(ItemTypes.BUCKET) ||
                    itemInHand.equals(ItemTypes.LAVA_BUCKET) ||
                    itemInHand.equals(ItemTypes.ITEM_FRAME) ||
                    itemInHand.equals(ItemTypes.END_CRYSTAL) ||
                    itemInHand.equals(ItemTypes.PAINTING)) && !r.canBuild(p)) {
                RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("playerlistener.region.cantuse"));
                event.setCancelled(true);
            } else if (!r.allowMod(p) && !RedProtect.get().getUtil().isBukkitBlock(bstate)) {
                RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantinteract");
                event.setCancelled(true);
            }
        }
    }

    private void changeFlag(Region r, String flag, Player p, Sign s) {
        if (r.setFlag(RedProtect.get().getVersionHelper().getCause(p), flag, !r.getFlagBool(flag))) {
            RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("cmdmanager.region.flag.set").replace("{flag}", "'" + flag + "'") + " " + r.getFlagBool(flag));
            RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + p.getName() + " SET FLAG " + flag + " of region " + r.getName() + " to " + RedProtect.get().lang.translBool(r.getFlagString(flag)));
            s.lines().set(3, RedProtect.get().getUtil().toText(RedProtect.get().lang.get("region.value") + " " + RedProtect.get().lang.translBool(r.getFlagString(flag))));
            if (!RedProtect.get().config.getSigns(r.getID()).contains(s.getLocation())) {
                RedProtect.get().config.putSign(r.getID(), s.getLocation());
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPlayerInteract(InteractEntityEvent e, @First Player p) {
        Entity ent = e.getTargetEntity();
        RedProtect.get().logger.debug(LogLevel.PLAYER, "PlayerListener - Is InteractEntityEvent event: " + ent.getType().getName());

        Location<World> l = ent.getLocation();
        Region r = RedProtect.get().rm.getTopRegion(l, this.getClass().getName());
        if (r == null) {
            return;
        }

        if (RedProtect.get().tpWait.contains(p.getName())) {
            RedProtect.get().tpWait.remove(p.getName());
            RedProtect.get().lang.sendMessage(p, "cmdmanager.region.tpcancelled");
        }

        if (ent instanceof Hanging || ent.getType().equals(EntityTypes.ARMOR_STAND) || ent.getType().equals(EntityTypes.ENDER_CRYSTAL)) {
            if (!r.canBuild(p)) {
                RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantinteract");
                e.setCancelled(true);
            }
        } else if (ent instanceof Minecart || ent instanceof Boat) {
            if (!r.canMinecart(p)) {
                RedProtect.get().lang.sendMessage(p, "blocklistener.region.cantenter");
                e.setCancelled(true);
            }
        } else if (!r.allowMod(p) && !RedProtect.get().getUtil().isBukkitEntity(ent) && (!(ent instanceof Player))) {
            RedProtect.get().logger.debug(LogLevel.PLAYER, "PlayerInteractEntityEvent - Block is " + ent.getType().getName());
            RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantinteract");
            e.setCancelled(true);
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onEntityDamageEvent(DamageEntityEvent e) {
        //victim
        Entity e1 = e.getTargetEntity();

        //damager
        Entity e2 = null;

        if (e.getCause().first(IndirectEntityDamageSource.class).isPresent()) {
            e2 = e.getCause().first(IndirectEntityDamageSource.class).get().getSource();

            RedProtect.get().logger.debug(LogLevel.PLAYER, "RPLayerListener: Is DamageEntityEvent event. Damager " + e2.getType().getName());
        }

        Player damager = null;
        if (e2 instanceof Projectile) {
            Projectile proj = (Projectile) e2;
            if (proj.getShooter() instanceof Player) {
                damager = (Player) proj.getShooter();
            }
        } else if (e2 instanceof Player) {
            damager = (Player) e2;
        }

        Location<World> l = e1.getLocation();
        Region r = RedProtect.get().rm.getTopRegion(l, this.getClass().getName());
        if (r == null) {
            return;
        }

        RedProtect.get().logger.debug(LogLevel.PLAYER, "RPLayerListener: Is DamageEntityEvent event. Victim " + e1.getType().getName());

        if (damager != null) {
            if (e1 instanceof Hanging && !r.canBuild(damager)) {
                RedProtect.get().lang.sendMessage(damager, "entitylistener.region.cantinteract");
                e.setCancelled(true);
                return;
            }
            if ((e1 instanceof Boat || e1 instanceof Minecart) && !r.canMinecart(damager)) {
                RedProtect.get().lang.sendMessage(damager, "entitylistener.region.cantbreak");
                e.setCancelled(true);
                return;
            }
            if (e1 instanceof Player && !e1.equals(damager) && r.flagExists("pvp") && !r.canPVP(damager, (Player) e1)) {
                RedProtect.get().lang.sendMessage(damager, "entitylistener.region.cantpvp");
                e.setCancelled(true);
                return;
            }
        }

        //return if not player
        if (!(e1 instanceof Player)) {
            return;
        }

        Player play = (Player) e.getTargetEntity();

        if (RedProtect.get().tpWait.contains(play.getName())) {
            RedProtect.get().tpWait.remove(play.getName());
            RedProtect.get().lang.sendMessage(play, RedProtect.get().lang.get("cmdmanager.region.tpcancelled"));
        }

        if (!r.canPlayerDamage()) {
            e.setCancelled(true);
        }

        //execute on health
        if (r.cmdOnHealth(play)) {
            RedProtect.get().logger.debug(LogLevel.PLAYER, "Cmd on healt: true");
        }

        if (!r.canDeath() && play.get(Keys.HEALTH).get() <= 1) {
            e.setCancelled(true);
        }

        //deny damagecauses
        List<String> Causes = RedProtect.get().config.globalFlagsRoot().worlds.get(play.getWorld().getName()).deny_death_by;
        if (Causes.size() > 0) {
            DamageType damagec = null;
            if (e.getCause().containsType(EntityDamageSource.class)) {
                damagec = e.getCause().first(EntityDamageSource.class).get().getType();
            }
            if (e.getCause().containsType(DamageSource.class)) {
                damagec = e.getCause().first(DamageSource.class).get().getType();
            }
            if (e.getCause().containsType(BlockDamageSource.class)) {
                damagec = e.getCause().first(BlockDamageSource.class).get().getType();
            }
            if (damagec != null && Causes.contains(damagec.getName())) {
                e.setCancelled(true);
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onEntityDamageByEntityEvent(InteractEntityEvent.Primary e, @First Player p) {
        Entity e1 = e.getTargetEntity();
        RedProtect.get().logger.debug(LogLevel.PLAYER, "RPLayerListener: Is EntityDamageByEntityEvent event. Victim: " + e.getTargetEntity().getType().getName());

        Location<World> l = e1.getLocation();
        Region r = RedProtect.get().rm.getTopRegion(l, this.getClass().getName());
        if (r == null || p == null) {
            return;
        }

        if (e1 instanceof Player && r.flagExists("pvp") && !r.canPVP(p, (Player) e1)) {
            RedProtect.get().lang.sendMessage(p, "entitylistener.region.cantpvp");
            e.setCancelled(true);
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPlayerMovement(MoveEntityEvent e) {
        if (RedProtect.get().config.configRoot().performance.disable_onPlayerMoveEvent_handler) {
            return;
        }

        if (e instanceof MoveEntityEvent.Teleport) {
            return;
        }

        Entity ent = e.getTargetEntity();

        Player p = null;

        if (ent instanceof Player) {
            p = (Player) ent;
        } else if (ent.get(Keys.PASSENGERS).isPresent()) {
            for (Object uuidEnt : ent.get(Keys.PASSENGERS).get()) {
                if (uuidEnt instanceof UUID) {
                    if (Sponge.getServer().getPlayer((UUID) uuidEnt).isPresent()) {
                        p = Sponge.getServer().getPlayer((UUID) uuidEnt).get();
                    }
                } else if (uuidEnt instanceof EntitySnapshot) {
                    if (Sponge.getServer().getPlayer(((EntitySnapshot) uuidEnt).getUniqueId().get()).isPresent()) {
                        p = Sponge.getServer().getPlayer(((EntitySnapshot) uuidEnt).getUniqueId().get()).get();
                    }
                }
            }
        } else {
            return;
        }

        if (p == null) {
            return;
        }

        RedProtect.get().logger.debug(LogLevel.PLAYER, "PlayerMoveEvent - Entity name: " + ent.getType().getName());

        if (e.getFromTransform() != e.getToTransform() && RedProtect.get().tpWait.contains(p.getName())) {
            RedProtect.get().tpWait.remove(p.getName());
            RedProtect.get().lang.sendMessage(p, "cmdmanager.region.tpcancelled");
        }

        Transform<World> lfromForm = e.getFromTransform();
        Transform<World> ltoForm = e.getToTransform();

        Location<World> lfrom = e.getFromTransform().getLocation();
        Location<World> lto = e.getToTransform().getLocation();

        //Exit flag
        Region rfrom = RedProtect.get().rm.getTopRegion(lfrom, this.getClass().getName());
        if (rfrom != null && !rfrom.canExit(p) && !RedProtect.get().ph.hasPermOrBypass(p, "redprotect.flag.admin.exit")) {
            e.setToTransform(RedProtect.get().getUtil().DenyExitPlayer(p, lfromForm, ltoForm, rfrom));
            return;
        }

        //teleport player to coord/world if playerup 128 y
        int NetherY = RedProtect.get().config.configRoot().nether_protection.maxYsize;
        if (lto.getExtent().getDimension().getType().equals(DimensionTypes.NETHER) && NetherY != -1 && lto.getBlockY() >= NetherY && !p.hasPermission("redprotect.bypass.nether-roof")) {
            for (String cmd : RedProtect.get().config.configRoot().nether_protection.execute_cmd) {
                Sponge.getGame().getCommandManager().process(RedProtect.get().getServer().getConsole(), cmd.replace("{player}", p.getName()));
            }
            RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("playerlistener.upnethery").replace("{location}", NetherY + ""));
        }

        Region r = RedProtect.get().rm.getTopRegion(lto, this.getClass().getName());

        World w = lfrom.getExtent();

        if (r != null) {

            //Enter flag
            if (!r.canEnter(p) && !RedProtect.get().ph.hasPermOrBypass(p, "redprotect.flag.admin.enter")) {
                e.setToTransform(RedProtect.get().getUtil().DenyEnterPlayer(w, lfromForm, ltoForm, r, false));
                RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantregionenter");
                return;
            }

            //canMove flag
            if (!r.canMove(p) && !RedProtect.get().ph.hasPermOrBypass(p, "redprotect.flag.admin.move")) {
                RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantmove");
                e.setCancelled(true);
                return;
            }

            //enter max players flag
            if (r.maxPlayers() != -1 && !RedProtect.get().ph.hasPermOrBypass(p, "redprotect.flag.admin.max-players")) {
                if (!checkMaxPlayer(p, r)) {
                    e.setToTransform(RedProtect.get().getUtil().DenyEnterPlayer(w, lfromForm, ltoForm, r, false));
                    RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("playerlistener.region.maxplayers").replace("{players}", String.valueOf(r.maxPlayers())));
                    return;
                }
            }

            //remove pots
            if (!r.allowEffects(p) && p.get(Keys.POTION_EFFECTS).isPresent() && !RedProtect.get().ph.hasPermOrBypass(p, "redprotect.flag.admin.allow-effect")) {
                for (PotionEffect pot : p.get(Keys.POTION_EFFECTS).get()) {
                    if (pot.getDuration() < 36000) {
                        p.offer(Keys.POTION_EFFECTS, new ArrayList<>());
                    }
                }
            }

            //Allow enter with items
            if (!r.canEnterWithItens(p) && !RedProtect.get().ph.hasPermOrBypass(p, "redprotect.flag.admin.deny-enter-items")) {
                e.setToTransform(RedProtect.get().getUtil().DenyEnterPlayer(w, lfromForm, ltoForm, r, false));
                RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("playerlistener.region.onlyenter.withitems").replace("{items}", r.getFlags().get("allow-enter-items").toString()));
                return;
            }

            //Deny enter with item
            if (!r.denyEnterWithItens(p)) {
                e.setToTransform(RedProtect.get().getUtil().DenyEnterPlayer(w, lfromForm, ltoForm, r, false));
                RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("playerlistener.region.denyenter.withitems").replace("{items}", r.getFlags().get("deny-enter-items").toString()));
                return;
            }

            //Deny Fly
            if (!p.get(Keys.GAME_MODE).get().getName().equalsIgnoreCase("SPECTATOR") && !r.canFly(p) && p.get(Keys.IS_FLYING).get() && !RedProtect.get().ph.hasPermOrBypass(p, "redprotect.flag.admin.allow-fly")) {
                p.offer(Keys.IS_FLYING, false);
                if (!p.isOnGround()) {
                    e.setToTransform(e.getFromTransform());
                }
                RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantfly");
            }

            //update region admin or leader visit
            if (RedProtect.get().config.configRoot().region_settings.record_player_visit_method.equalsIgnoreCase("ON-REGION-ENTER")) {
                if (r.isLeader(p) || r.isAdmin(p)) {
                    if (r.getDate() == null || (!r.getDate().equals(RedProtect.get().getUtil().dateNow()))) {
                        r.setDate(RedProtect.get().getUtil().dateNow());
                    }
                }
            }

            if (!Ownerslist.containsKey(p) || !Ownerslist.get(p).equals(r.getID())) {
                Region er = RedProtect.get().rm.getRegionById(Ownerslist.get(p));
                Ownerslist.put(p, r.getID());

                //Execute listener:
                EnterExitRegionEvent event = new EnterExitRegionEvent(er, r, p);
                if (Sponge.getEventManager().post(event)) {
                    return;
                }

                //--
                RegionFlags(r, er, p, false);
                if (!r.getWelcome().equalsIgnoreCase("hide ")) {
                    EnterExitNotify(r, p);
                }
            } else {
                RegionFlags(r, null, p, false);
            }
        } else {
            //if (r == null) >>
            if (Ownerslist.get(p) != null) {
                Region er = RedProtect.get().rm.getRegionById(Ownerslist.get(p));
                Ownerslist.remove(p);

                //Execute listener:
                EnterExitRegionEvent event = new EnterExitRegionEvent(er, null, p);
                if (Sponge.getEventManager().post(event)) {
                    return;
                }
                //---
                if (er == null) {
                    //remove all if no regions
                    List<String> toRemove = new ArrayList<>();
                    for (String taskId : PlayertaskID.keySet()) {
                        if (PlayertaskID.get(taskId).equals(p.getName())) {
                            if (taskId.contains("forcefly")) {
                                p.offer(Keys.CAN_FLY, false);
                                p.offer(Keys.IS_FLYING, false);
                            } else {
                                p.remove(Keys.POTION_EFFECTS);
                            }
                            toRemove.add(taskId);
                            stopTaskPlayer(taskId);
                        }
                    }
                    for (String key : toRemove) {
                        PlayertaskID.remove(key);
                    }
                } else {
                    noRegionFlags(er, p);
                    if (!er.getWelcome().equalsIgnoreCase("hide ") && RedProtect.get().config.configRoot().notify.region_exit) {
                        SendNotifyMsg(p, RedProtect.get().lang.get("playerlistener.region.wilderness"));
                    }
                }
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPlayerTeleport(MoveEntityEvent.Teleport e, @First Player p) {

        if (RedProtect.get().tpWait.contains(p.getName())) {
            RedProtect.get().tpWait.remove(p.getName());
            RedProtect.get().lang.sendMessage(p, "cmdmanager.region.tpcancelled");
        }

        Location<World> lfrom = e.getFromTransform().getLocation();
        Location<World> lto = e.getToTransform().getLocation();
        final Region rfrom = RedProtect.get().rm.getTopRegion(lfrom, this.getClass().getName());
        final Region rto = RedProtect.get().rm.getTopRegion(lto, this.getClass().getName());

        if (rfrom != null) {
            if (!rfrom.canExit(p) && !RedProtect.get().ph.hasPermOrBypass(p, "redprotect.flag.admin.exit")) {
                e.setToTransform(RedProtect.get().getUtil().DenyExitPlayer(p, e.getFromTransform(), e.getToTransform(), rfrom));
                return;
            }

            //canMove flag
            if (!rfrom.canMove(p) && !RedProtect.get().ph.hasPermOrBypass(p, "redprotect.flag.admin.move")) {
                RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantmove");
                e.setCancelled(true);
                return;
            }
        }

        Sponge.getScheduler().createAsyncExecutor(RedProtect.get().container).scheduleWithFixedDelay(() -> {
            if (rto != null && rfrom == null) {
                RegionFlags(rto, null, p, false);
            }
            if (rto != null && rfrom != null) {
                if (rto == rfrom) {
                    RegionFlags(rto, null, p, false);
                } else {
                    RegionFlags(rto, rfrom, p, false);
                }
            }
            if (rto == null && rfrom != null) {
                noRegionFlags(rfrom, p);
            }
            if (rfrom == null && rto != null) {
                noRegionFlags(rto, p);
            }
            if (rfrom == null && rto == null) {
                //remove all if no regions
                List<String> toRemove = new ArrayList<>();
                for (String taskId : PlayertaskID.keySet()) {
                    if (PlayertaskID.get(taskId).equals(p.getName())) {
                        if (taskId.contains("forcefly")) {
                            p.offer(Keys.CAN_FLY, false);
                            p.offer(Keys.IS_FLYING, false);
                        } else {
                            p.remove(Keys.POTION_EFFECTS);
                        }
                        toRemove.add(taskId);
                        stopTaskPlayer(taskId);
                    }
                }
                for (String key : toRemove) {
                    PlayertaskID.remove(key);
                }
            }
        }, 2, 2, TimeUnit.SECONDS);

        if (rto != null) {

            //enter max players flag
            if (rto.maxPlayers() != -1) {
                if (!checkMaxPlayer(p, rto)) {
                    RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("playerlistener.region.maxplayers").replace("{players}", String.valueOf(rto.maxPlayers())));
                    e.setCancelled(true);
                }
            }

            if (!rto.canEnter(p)) {
                RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantregionenter");
                e.setCancelled(true);
                return;
            }

            //Allow enter with items
            if (!rto.canEnterWithItens(p)) {
                RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("playerlistener.region.onlyenter.withitems").replace("{items}", rto.getFlags().get("allow-enter-items").toString()));
                e.setCancelled(true);
                return;
            }

            //Deny enter with item
            if (!rto.denyEnterWithItens(p)) {
                RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("playerlistener.region.denyenter.withitems").replace("{items}", rto.getFlags().get("deny-enter-items").toString()));
                e.setCancelled(true);
                return;
            }

            if (PlayerCmd.containsKey(p)) {
                if (!rto.canBack(p) && PlayerCmd.get(p).startsWith("back")) {
                    RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantback");
                    e.setCancelled(true);
                }
                if (!rto.AllowHome(p) && PlayerCmd.get(p).startsWith("home")) {
                    RedProtect.get().lang.sendMessage(p, "playerlistener.region.canthome");
                    e.setCancelled(true);
                }
                PlayerCmd.remove(p);
            }
        }


        //teleport player to coord/world if playerup 128 y
        int NetherY = RedProtect.get().config.configRoot().nether_protection.maxYsize;
        if (lto.getExtent().getDimension().getType().equals(DimensionTypes.NETHER) && NetherY != -1 && lto.getBlockY() >= NetherY && !p.hasPermission("redprotect.bypass.nether-roof")) {
            RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("playerlistener.upnethery").replace("{location}", NetherY + ""));
            e.setCancelled(true);
        }

        if (e instanceof MoveEntityEvent.Teleport.Portal) {
            if (rto != null && !rto.canExitPortal(p)) {
                RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantteleport");
                e.setCancelled(true);
            }

            if (rfrom != null && !rfrom.canEnterPortal(p)) {
                RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantenterteleport");
                e.setCancelled(true);
            }
        } else {
            if (rfrom != null && !rfrom.canTeleport(p)) {
                RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantuse");
                e.setCancelled(true);
            }
            if (rto != null && !rto.canTeleport(p)) {
                RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantuse");
                e.setCancelled(true);
            }
        }
    }

    private boolean checkMaxPlayer(Player p, Region r) {
        if (r.canBuild(p)) {
            return true;
        }
        int ttl = 0;
        for (Player onp : p.getWorld().getPlayers()) {
            if (onp == p) {
                continue;
            }
            Region reg = RedProtect.get().rm.getTopRegion(onp.getLocation(), this.getClass().getName());
            if (reg != null && reg == r) {
                ttl++;
            }
        }
        return ttl < r.maxPlayers();
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPlayerCommand(SendCommandEvent e, @First Player p) {

        if (RedProtect.get().tpWait.contains(p.getName())) {
            RedProtect.get().tpWait.remove(p.getName());
            RedProtect.get().lang.sendMessage(p, "cmdmanager.region.tpcancelled");
        }

        String cmd = e.getCommand();

        if (RedProtect.get().config.configRoot().server_protection.deny_commands_on_worlds.getOrDefault(p.getWorld().getName(), new ArrayList<>()).contains(cmd) && !p.hasPermission("redprotect.bypass")) {
            RedProtect.get().lang.sendMessage(p, "playerlistener.command-notallowed");
            e.setCancelled(true);
            return;
        }

        if (RedProtect.get().config.globalFlagsRoot().worlds.get(p.getWorld().getName()).command_ranges.containsKey(cmd) && !cmd.equals(".")) {
            double min = RedProtect.get().config.globalFlagsRoot().worlds.get(p.getWorld().getName()).command_ranges.get(cmd).min_range;
            double max = RedProtect.get().config.globalFlagsRoot().worlds.get(p.getWorld().getName()).command_ranges.get(cmd).max_range;
            String mesg = RedProtect.get().config.globalFlagsRoot().worlds.get(p.getWorld().getName()).command_ranges.get(cmd).message;
            double py = p.getLocation().getY();
            if (py < min || py > max) {
                if (mesg != null && !mesg.equals("")) {
                    RedProtect.get().lang.sendMessage(p, mesg);
                }
                e.setCancelled(true);
                return;
            }
        }

        if (cmd.startsWith("back") || cmd.startsWith("home")) {
            PlayerCmd.put(p, cmd);
        }

        Region r = RedProtect.get().rm.getTopRegion(p.getLocation(), this.getClass().getName());
        if (r != null) {

            if (!r.AllowCommands(p, cmd)) {
                if (cmd.startsWith("rp") || cmd.startsWith("redprotect")) {
                    return;
                }
                RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantcommand");
                e.setCancelled(true);
                return;
            }

            if (!r.DenyCommands(p, cmd)) {
                if (cmd.startsWith("rp") || cmd.startsWith("redprotect")) {
                    return;
                }
                RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantcommand");
                e.setCancelled(true);
                return;
            }

            if (cmd.startsWith("home") && !r.AllowHome(p)) {
                RedProtect.get().lang.sendMessage(p, "playerlistener.region.canthome");
                e.setCancelled(true);
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPlayerHarvest(HarvestEntityEvent.TargetPlayer e) {
        RedProtect.get().logger.debug(LogLevel.PLAYER, "RPLayerListener: Is HarvestEntityEvent");

        Player p = e.getTargetEntity();
        Region r = RedProtect.get().rm.getTopRegion(p.getLocation(), this.getClass().getName());

        if (r != null) {
            if (r.keepInventory()) {
                e.setKeepsInventory(true);
            }
            if (r.keepLevels()) {
                e.setKeepsLevel(true);
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPlayerDie(DestructEntityEvent.Death e) {
        if (!(e.getTargetEntity() instanceof Player)) {
            return;
        }

        RedProtect.get().logger.debug(LogLevel.PLAYER, "RPLayerListener: Is DestructEntityEvent.Death");

        Player p = (Player) e.getTargetEntity();

        if (RedProtect.get().tpWait.contains(p.getName())) {
            RedProtect.get().tpWait.remove(p.getName());
            RedProtect.get().lang.sendMessage(p, "cmdmanager.region.tpcancelled");
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPortalCreate(ConstructPortalEvent e) {
        Region r = RedProtect.get().rm.getTopRegion(e.getPortalLocation(), this.getClass().getName());
        if (r != null && !r.canCreatePortal()) {
            e.setCancelled(true);
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPlayerLogout(ClientConnectionEvent.Disconnect e) {
        stopTaskPlayer(e.getTargetEntity());
        RedProtect.get().tpWait.remove(e.getTargetEntity().getName());
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void PlayerLogin(ClientConnectionEvent.Login e) {
        RedProtect.get().logger.debug(LogLevel.PLAYER, "Is ClientConnectionEvent.Login event. Player " + e.getTargetUser().getName());

        User p = e.getTargetUser();
        Sponge.getScheduler().createAsyncExecutor(RedProtect.get().container).execute(() -> {

            if (RedProtect.get().config.configRoot().online_mode) {

                String uuid = p.getUniqueId().toString();
                RedProtect.get().rm.getMemberRegions(uuid).forEach(r -> {
                    // Update player names based on uuids
                    r.getLeaders().forEach(rp -> {
                        if (rp.getUUID().equalsIgnoreCase(uuid) && !rp.getPlayerName().equalsIgnoreCase(p.getName())) {
                            rp.setPlayerName(p.getName().toLowerCase());
                            r.setToSave(true);
                        }
                    });
                    r.getAdmins().forEach(rp -> {
                        if (rp.getUUID().equalsIgnoreCase(uuid) && !rp.getPlayerName().equalsIgnoreCase(p.getName())) {
                            rp.setPlayerName(p.getName().toLowerCase());
                            r.setToSave(true);
                        }
                    });
                    r.getMembers().forEach(rp -> {
                        if (rp.getUUID().equalsIgnoreCase(uuid) && !rp.getPlayerName().equalsIgnoreCase(p.getName())) {
                            rp.setPlayerName(p.getName().toLowerCase());
                            r.setToSave(true);
                        }
                    });
                });


            } else {

                String pName = p.getName();
                RedProtect.get().rm.getMemberRegions(pName).forEach(r -> {
                    // Update uuids based on player names
                    r.getLeaders().forEach(rp -> {
                        if (rp.getPlayerName().equalsIgnoreCase(pName) && !rp.getUUID().equalsIgnoreCase(p.getUniqueId().toString())) {
                            rp.setUUID(p.getUniqueId().toString());
                            r.setToSave(true);
                        }
                    });
                    r.getAdmins().forEach(rp -> {
                        if (rp.getPlayerName().equalsIgnoreCase(pName) && !rp.getUUID().equalsIgnoreCase(p.getUniqueId().toString())) {
                            rp.setUUID(p.getUniqueId().toString());
                            r.setToSave(true);
                        }
                    });
                    r.getMembers().forEach(rp -> {
                        if (rp.getPlayerName().equalsIgnoreCase(pName) && !rp.getUUID().equalsIgnoreCase(p.getUniqueId().toString())) {
                            rp.setUUID(p.getUniqueId().toString());
                            r.setToSave(true);
                        }
                    });
                });

            }

            RedProtect.get().rm.getAdminRegions(p.getUniqueId().toString()).forEach(r -> {
                if (RedProtect.get().config.configRoot().region_settings.record_player_visit_method.equalsIgnoreCase("ON-LOGIN") && (r.isAdmin(p.getUniqueId().toString()) || r.isLeader(p.getUniqueId().toString()))) {
                    if (r.getDate() == null || !r.getDate().equals(RedProtect.get().getUtil().dateNow())) {
                        r.setDate(RedProtect.get().getUtil().dateNow());
                    }
                }
            });
        });

        if (p.getPlayer().isPresent()) {
            Region r = RedProtect.get().rm.getTopRegion(p.getPlayer().get().getLocation(), this.getClass().getName());
            if (r != null) {
                RegionFlags(r, null, p.getPlayer().get(), true);
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void PlayerTrownPotion(LaunchProjectileEvent e) {

        Entity ent = e.getTargetEntity();
        RedProtect.get().logger.debug(LogLevel.PLAYER, "Is PotionSplashEvent event.");

        Region r = RedProtect.get().rm.getTopRegion(ent.getLocation(), this.getClass().getName());
        if (ent instanceof ThrownPotion) {

            ThrownPotion potion = (ThrownPotion) e.getTargetEntity();
            ProjectileSource thrower = potion.getShooter();

            if (thrower instanceof Player) {
                if (r != null && !r.usePotions((Player) thrower)) {
                    RedProtect.get().lang.sendMessage((Player) thrower, "playerlistener.region.cantuse");
                    e.setCancelled(true);
                    return;
                }
            }

            List<PotionEffect> pottypes = potion.get(Keys.POTION_EFFECTS).get();
            //deny potion
            List<String> Pots = RedProtect.get().config.globalFlagsRoot().worlds.get(ent.getWorld().getName()).deny_potions;
            if (!Pots.isEmpty()) {
                for (PotionEffect t : pottypes) {
                    if (Pots.contains(t.getType().getName().toUpperCase())) {
                        e.setCancelled(true);
                        if (thrower instanceof Player) {
                            RedProtect.get().lang.sendMessage((Player) thrower, RedProtect.get().lang.get("playerlistener.denypotion"));
                        }
                        break;
                    }
                }
            }

        }
    }

    public void SendNotifyMsg(Player p, String notify) {
        if (RedProtect.get().config.configRoot().notify.region_enter_mode.equalsIgnoreCase("OFF")) {
            return;
        }
        if (!notify.equals("")) {
            if (RedProtect.get().config.configRoot().notify.region_enter_mode.equalsIgnoreCase("BOSSBAR")) {
                ServerBossBar boss = ServerBossBar.builder()
                        .name(RedProtect.get().getUtil().toText(notify))
                        .overlay(BossBarOverlays.NOTCHED_12)
                        .color(BossBarColors.YELLOW)
                        .percent(1).build();
                boss.addPlayer(p);
                //start timer
                Task.builder()
                        .interval(1, TimeUnit.SECONDS)
                        .execute(new BossBarTimer(boss))
                        .submit(RedProtect.get().container);
            }
            if (RedProtect.get().config.configRoot().notify.region_enter_mode.equalsIgnoreCase("CHAT")) {
                p.sendMessage(RedProtect.get().getUtil().toText(notify));
            }
        }
    }

    public void SendWelcomeMsg(Player p, String wel) {
        if (RedProtect.get().config.configRoot().notify.welcome_mode.equalsIgnoreCase("OFF")) {
            return;
        }
        if (RedProtect.get().config.configRoot().notify.welcome_mode.equalsIgnoreCase("BOSSBAR")) {
            ServerBossBar boss = ServerBossBar.builder()
                    .name(RedProtect.get().getUtil().toText(wel))
                    .overlay(BossBarOverlays.NOTCHED_12)
                    .color(BossBarColors.GREEN)
                    .percent(1).build();
            boss.addPlayer(p);
            //start timer
            Task.builder()
                    .interval(1, TimeUnit.SECONDS)
                    .execute(new BossBarTimer(boss))
                    .submit(RedProtect.get().container);
        }
        if (RedProtect.get().config.configRoot().notify.welcome_mode.equalsIgnoreCase("CHAT")) {
            p.sendMessage(RedProtect.get().getUtil().toText(wel));
        }
    }

    private void stopTaskPlayer(String taskId) {
        Sponge.getScheduler().getTaskById(UUID.fromString(taskId.split("_")[0])).ifPresent(Task::cancel);
    }

    private void stopTaskPlayer(Player p) {
        List<String> toremove = new ArrayList<>();
        for (String taskId : PlayertaskID.keySet()) {
            Sponge.getScheduler().getTaskById(UUID.fromString(taskId.split("_")[0])).ifPresent(t -> {
                if (PlayertaskID.get(taskId).equals(p.getName())) {
                    t.cancel();
                    toremove.add(taskId);
                }
            });

        }
        for (String remove : toremove) {
            PlayertaskID.remove(remove);
            RedProtect.get().logger.debug(LogLevel.PLAYER, "Removed task ID: " + remove + " for player " + p.getName());
        }
        toremove.clear();
    }

    private void EnterExitNotify(Region r, Player p) {
        if (RedProtect.get().config.configRoot().notify.region_enter_mode.equalsIgnoreCase("OFF")) {
            return;
        }

        if (!r.canEnter(p)) {
            return;
        }

        String leaderstring;
        String m = "";
        //Enter-Exit notifications
        if (r.getWelcome().equals("")) {
            if (RedProtect.get().config.configRoot().notify.region_enter_mode.equalsIgnoreCase("CHAT") ||
                    RedProtect.get().config.configRoot().notify.region_enter_mode.equalsIgnoreCase("BOSSBAR")) {
                StringBuilder leaderstringBuilder = new StringBuilder();
                for (PlayerRegion leader : r.getLeaders()) {
                    leaderstringBuilder.append(", ").append(leader.getPlayerName());
                }
                leaderstring = leaderstringBuilder.toString();

                if (r.getLeaders().size() > 0) {
                    leaderstring = leaderstring.substring(2);
                } else {
                    leaderstring = "None";
                }
                m = RedProtect.get().lang.get("playerlistener.region.entered");
                m = m.replace("{leaders}", leaderstring);
                m = m.replace("{region}", r.getName());
            }
            SendNotifyMsg(p, m);
        } else {
            String wel = r.getWelcome().replace("{r}", r.getName())
                    .replace("{player}", p.getName());
            SendWelcomeMsg(p, RedProtect.get().lang.get("playerlistener.region.welcome")
                    .replace("{region}", r.getName()).replace("{message}", wel));
        }
    }

    private void RegionFlags(final Region r, Region er, final Player p, boolean join) {

        if (r.canEnter(p) || RedProtect.get().ph.hasPermOrBypass(p, "redprotect.admin.flag.enter")) {

            //prevent spam commands
            if (join || RedProtect.get().rm.getTopRegion(p.getLocation(), this.getClass().getName()) != r) {

                //Enter command as player
                if (r.flagExists("player-enter-command") && !RedProtect.get().ph.hasPermOrBypass(p, "redprotect.admin.flag.server-enter-command")) {
                    String[] cmds = r.getFlagString("player-enter-command").split(",");
                    for (String cmd : cmds) {
                        if (cmd.startsWith("/")) {
                            cmd = cmd.substring(1);
                        }
                        Sponge.getGame().getCommandManager().process(p, cmd.replace("{player}", p.getName()).replace("{region}", r.getName()));
                    }
                }

                //Enter command as console
                if (r.flagExists("server-enter-command") && !RedProtect.get().ph.hasPermOrBypass(p, "redprotect.admin.flag.server-enter-command")) {
                    String[] cmds = r.getFlagString("server-enter-command").split(",");
                    for (String cmd : cmds) {
                        if (cmd.startsWith("/")) {
                            cmd = cmd.substring(1);
                        }
                        Sponge.getGame().getCommandManager().process(RedProtect.get().getServer().getConsole(), cmd.replace("{player}", p.getName()).replace("{region}", r.getName()));
                    }
                }
            }

            //enter Gamemode flag
            if (r.flagExists("gamemode") && !RedProtect.get().ph.hasPermOrBypass(p, "redprotect.admin.flag.gamemode")) {
                p.offer(Keys.GAME_MODE, Sponge.getRegistry().getType(GameMode.class, r.getFlagString("gamemode")).orElse(GameModes.SURVIVAL));
            }

            //Check portal (/rp flag set-portal <rp> <world>
            if (r.flagExists("set-portal")) {
                if (RedProtect.get().teleportDelay.contains(p.getName())) {
                    //RedProtect.get().lang.sendMessage(p, "playerlistener.portal.wait");
                    return;
                } else {
                    String[] cmds = r.getFlagString("set-portal").split(" ");
                    Sponge.getGame().getCommandManager().process(RedProtect.get().getServer().getConsole(), "rp teleport " + cmds[0] + " " + cmds[1] + " " + p.getName());
                    RedProtect.get().teleportDelay.add(p.getName());
                    Sponge.getScheduler().createSyncExecutor(RedProtect.get().container).schedule(() -> {
                        RedProtect.get().teleportDelay.remove(p.getName());
                    }, RedProtect.get().config.configRoot().region_settings.portal_delay, TimeUnit.SECONDS);
                }
            }
        }

        if (er != null && (er.canExit(p) || RedProtect.get().ph.hasPermOrBypass(p, "redprotect.admin.flag.exit"))) {

            //Exit gamemode
            if (er.flagExists("gamemode") && !RedProtect.get().ph.hasPermOrBypass(p, "redprotect.admin.flag.gamemode")) {
                p.offer(Keys.GAME_MODE, p.getWorld().getProperties().getGameMode());
            }

            //Exit effect
            if (er.flagExists("effects") && !RedProtect.get().ph.hasPermOrBypass(p, "redprotect.admin.flag.effects")) {
                String[] effects = er.getFlagString("effects").split(",");
                for (String effect : effects) {
                    if (PlayertaskID.containsValue(p.getName())) {
                        String eff = effect.split(" ")[0];
						/*String amplifier = effect.split(" ")[1];
						PotionEffect fulleffect = PotionEffect.builder()
								.particles(false)
								.potionType(CoreUtil.getPotType(eff))
								.amplifier(Integer.parseInt(amplifier))
								.build();*/
                        p.remove(Keys.POTION_EFFECTS);
                        List<String> removeTasks = new ArrayList<>();
                        for (String taskId : PlayertaskID.keySet()) {
                            String id = taskId.split("_")[0];
                            String ideff = id + "_" + eff + er.getName();
                            Sponge.getScheduler().getTaskById(UUID.fromString(id)).ifPresent(t -> {
                                if (PlayertaskID.containsKey(ideff) && PlayertaskID.get(ideff).equals(p.getName())) {
                                    t.cancel();
                                    removeTasks.add(taskId);
                                    RedProtect.get().logger.debug(LogLevel.PLAYER, "(RegionFlags-eff)Removed task ID: " + taskId + " for player " + p.getName());
                                }
                            });
                        }
                        for (String key : removeTasks) {
                            PlayertaskID.remove(key);
                        }
                        removeTasks.clear();
                    }
                }
            } else
                //exit fly flag
                if (er.flagExists("forcefly") && !RedProtect.get().ph.hasPermOrBypass(p, "redprotect.admin.flag.forcefly") && (p.gameMode().get().equals(GameModes.SURVIVAL) || p.gameMode().get().equals(GameModes.ADVENTURE))) {
                    if (PlayertaskID.containsValue(p.getName())) {
                        if (r.flagExists("forcefly")) {
                            p.offer(Keys.CAN_FLY, r.getFlagBool("forcefly"));
                            p.offer(Keys.IS_FLYING, r.getFlagBool("forcefly"));
                        } else {
                            p.offer(Keys.CAN_FLY, false);
                            p.offer(Keys.IS_FLYING, false);
                        }
                        List<String> removeTasks = new ArrayList<>();
                        for (String taskId : PlayertaskID.keySet()) {
                            String id = taskId.split("_")[0];
                            String ideff = id + "_" + "forcefly" + er.getName();
                            Sponge.getScheduler().getTaskById(UUID.fromString(id)).ifPresent(t -> {
                                if (PlayertaskID.containsKey(ideff) && PlayertaskID.get(ideff).equals(p.getName())) {
                                    t.cancel();
                                    removeTasks.add(taskId);
                                    RedProtect.get().logger.debug(LogLevel.PLAYER, "(RegionFlags fly)Removed task ID: " + taskId + " for player " + p.getName());
                                }
                            });
                        }
                        for (String key : removeTasks) {
                            PlayertaskID.remove(key);
                        }
                        removeTasks.clear();
                    }
                } else {
                    stopTaskPlayer(p);
                }

            //Exit command as player
            if (er.flagExists("player-exit-command") && !RedProtect.get().ph.hasPermOrBypass(p, "redprotect.admin.flag.player-exit-command")) {
                String[] cmds = er.getFlagString("player-exit-command").split(",");
                for (String cmd : cmds) {
                    if (cmd.startsWith("/")) {
                        cmd = cmd.substring(1);
                    }
                    Sponge.getGame().getCommandManager().process(p, cmd.replace("{player}", p.getName()).replace("{region}", er.getName()));
                }
            }

            //Exit command as console
            if (er.flagExists("server-exit-command") && !RedProtect.get().ph.hasPermOrBypass(p, "redprotect.admin.flag.server-exit-command")) {
                String[] cmds = er.getFlagString("server-exit-command").split(",");
                for (String cmd : cmds) {
                    if (cmd.startsWith("/")) {
                        cmd = cmd.substring(1);
                    }
                    Sponge.getGame().getCommandManager().process(RedProtect.get().getServer().getConsole(), cmd.replace("{player}", p.getName()).replace("{region}", er.getName()));
                }
            }
        }

        //2nd checks
        if (r.canEnter(p) || RedProtect.get().ph.hasPermOrBypass(p, "redprotect.admin.flag.enter")) {

            //Enter effect
            if (r.flagExists("effects") && !RedProtect.get().ph.hasPermOrBypass(p, "redprotect.admin.flag.effects")) {
                String[] effects = r.getFlagString("effects").split(",");
                for (String effect : effects) {
                    String eff = effect.split(" ")[0];
                    String amplifier = effect.split(" ")[1];
                    PotionEffect fulleffect = PotionEffect.builder()
                            .particles(false)
                            .potionType(Sponge.getRegistry().getType(PotionEffectType.class, eff).orElse(PotionEffectTypes.STRENGTH))
                            .amplifier(Integer.parseInt(amplifier))
                            .duration(RedProtect.get().config.configRoot().flags_configuration.effects_duration)
                            .build();
                    String TaskId = Sponge.getScheduler().createAsyncExecutor(RedProtect.get().container).scheduleWithFixedDelay(new Runnable() {
                        public void run() {
                            if (p.isOnline() && r.flagExists("effects")) {
                                p.offer(Keys.POTION_EFFECTS, Collections.singletonList(fulleffect));
                            } else {
                                p.offer(Keys.CAN_FLY, false);
                                try {
                                    this.finalize();
                                } catch (Throwable e) {
                                    RedProtect.get().logger.debug(LogLevel.PLAYER, "Effects not finalized...");
                                }
                            }
                        }
                    }, 0, 20, TimeUnit.SECONDS).getTask().getUniqueId().toString();
                    PlayertaskID.put(TaskId + "_" + eff + r.getName(), p.getName());
                    RedProtect.get().logger.debug(LogLevel.PLAYER, "Added task ID: " + TaskId + "_" + eff + " for player " + p.getName());
                }
            }

            //enter fly flag
            if (r.flagExists("forcefly") && !RedProtect.get().ph.hasPermOrBypass(p, "redprotect.admin.flag.forcefly") && (p.gameMode().get().equals(GameModes.SURVIVAL) || p.gameMode().get().equals(GameModes.ADVENTURE))) {
                p.offer(Keys.CAN_FLY, r.getFlagBool("forcefly"));
                p.offer(Keys.IS_FLYING, r.getFlagBool("forcefly"));
                String TaskId = Sponge.getScheduler().createAsyncExecutor(RedProtect.get().container).scheduleWithFixedDelay(new Runnable() {
                    public void run() {
                        if (p.isOnline() && r.flagExists("forcefly")) {
                            p.offer(Keys.CAN_FLY, r.getFlagBool("forcefly"));
                            p.offer(Keys.IS_FLYING, r.getFlagBool("forcefly"));
                        } else {
                            p.offer(Keys.CAN_FLY, false);
                            p.offer(Keys.IS_FLYING, false);
                            try {
                                this.finalize();
                            } catch (Throwable e) {
                                RedProtect.get().logger.debug(LogLevel.PLAYER, "forcefly not finalized...");
                            }
                        }
                    }
                }, 0, 80, TimeUnit.SECONDS).getTask().getUniqueId().toString();
                PlayertaskID.put(TaskId + "_" + "forcefly" + r.getName(), p.getName());
                RedProtect.get().logger.debug(LogLevel.PLAYER, "(RegionFlags fly)Added task ID: " + TaskId + "_" + "forcefly" + " for player " + p.getName());
            }
        }
    }

    private void noRegionFlags(Region er, Player p) {

        if (er != null && (er.canExit(p) || RedProtect.get().ph.hasPermOrBypass(p, "redprotect.admin.flag.exit"))) {

            //Exit gamemode
            if (er.flagExists("gamemode") && !RedProtect.get().ph.hasPermOrBypass(p, "redprotect.admin.flag.gamemode")) {
                p.offer(Keys.GAME_MODE, p.getWorld().getProperties().getGameMode());
            }

            //Exit effect
            if (er.flagExists("effects") && !RedProtect.get().ph.hasPermOrBypass(p, "redprotect.admin.flag.effects")) {
                String[] effects = er.getFlagString("effects").split(",");
                for (String effect : effects) {
                    if (PlayertaskID.containsValue(p.getName())) {
                        String eff = effect.split(" ")[0];
                        p.remove(Keys.POTION_EFFECTS);
                        List<String> removeTasks = new ArrayList<>();
                        for (String taskId : PlayertaskID.keySet()) {
                            String id = taskId.split("_")[0];
                            String ideff = id + "_" + eff + er.getName();
                            Sponge.getScheduler().getTaskById(UUID.fromString(id)).ifPresent(t -> {
                                if (PlayertaskID.containsKey(ideff) && PlayertaskID.get(ideff).equals(p.getName())) {
                                    t.cancel();
                                    removeTasks.add(taskId);
                                    RedProtect.get().logger.debug(LogLevel.PLAYER, "(noRegionFlags eff)Removed task ID: " + taskId + " for effect " + effect);
                                }
                            });
                        }
                        for (String key : removeTasks) {
                            PlayertaskID.remove(key);
                        }
                        removeTasks.clear();
                    }
                }
            } else

                //exit fly flag
                if (er.flagExists("forcefly") && !RedProtect.get().ph.hasPermOrBypass(p, "redprotect.admin.flag.forcefly") && (p.gameMode().get().equals(GameModes.SURVIVAL) || p.gameMode().get().equals(GameModes.ADVENTURE))) {
                    if (PlayertaskID.containsValue(p.getName())) {
                        p.offer(Keys.CAN_FLY, false);
                        p.offer(Keys.IS_FLYING, false);
                        List<String> removeTasks = new ArrayList<>();
                        for (String taskId : PlayertaskID.keySet()) {
                            String id = taskId.split("_")[0];
                            String ideff = id + "_" + "forcefly" + er.getName();
                            Sponge.getScheduler().getTaskById(UUID.fromString(id)).ifPresent(t -> {
                                if (PlayertaskID.containsKey(ideff) && PlayertaskID.get(ideff).equals(p.getName())) {
                                    t.cancel();
                                    removeTasks.add(taskId);
                                    RedProtect.get().logger.debug(LogLevel.PLAYER, "(noRegionFlags fly)Removed task ID: " + taskId + " for player " + p.getName());
                                }
                            });
                        }
                        for (String key : removeTasks) {
                            PlayertaskID.remove(key);
                        }
                        removeTasks.clear();
                    }
                } else {
                    stopTaskPlayer(p);
                }

            //Exit command as player
            if (er.flagExists("player-exit-command") && !RedProtect.get().ph.hasPermOrBypass(p, "redprotect.admin.flag.player-exit-command")) {
                String[] cmds = er.getFlagString("player-exit-command").split(",");
                for (String cmd : cmds) {
                    if (cmd.startsWith("/")) {
                        cmd = cmd.substring(1);
                    }
                    Sponge.getGame().getCommandManager().process(p, cmd.replace("{player}", p.getName()));
                }
            }

            //Exit command as console
            if (er.flagExists("server-exit-command") && !RedProtect.get().ph.hasPermOrBypass(p, "redprotect.admin.flag.server-exit-command")) {
                String[] cmds = er.getFlagString("server-exit-command").split(",");
                for (String cmd : cmds) {
                    if (cmd.startsWith("/")) {
                        cmd = cmd.substring(1);
                    }
                    Sponge.getGame().getCommandManager().process(RedProtect.get().getServer().getConsole(), cmd.replace("{player}", p.getName()));
                }
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onHunger(HealEntityEvent e) {
        if (!(e.getTargetEntity() instanceof Player)) {
            return;
        }

        Player p = (Player) e.getTargetEntity();

        Region r = RedProtect.get().rm.getTopRegion(p.getLocation(), this.getClass().getName());
        if (r != null && !r.canHunger()) {
            e.setCancelled(true);
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onItemPickup(CollideEntityEvent event, @Root Player p) {
        RedProtect.get().logger.debug(LogLevel.PLAYER, "Is CollideEntityEvent(ItemPickup) event.");
        for (Entity ent : event.getEntities()) {
            if (!(ent instanceof Item)) {
                continue;
            }
            Region r = RedProtect.get().rm.getTopRegion(ent.getLocation(), this.getClass().getName());
            if (r != null && ((!r.canEnter(p) && !r.canPickup(p) || !r.canPickup(p)))) {
                event.setCancelled(true);
                RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantpickup");
                return;
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void PlayerDropItemGui(DropItemEvent.Pre e, @Root Player p) {
        e.getDroppedItems().forEach(item -> {
            if (RedProtect.get().getUtil().isGuiItem(item.createStack())) {
                RedProtect.get().getVersionHelper().removeGuiItem(p);
                e.setCancelled(true);
            }
        });
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void PlayerDropItem(DropItemEvent.Dispense e, @Root Player p) {
        RedProtect.get().logger.debug(LogLevel.PLAYER, "Is DropItemEvent.Dispense event.");

        for (Entity ent : e.getEntities()) {
            Location<World> l = ent.getLocation();
            Region r = RedProtect.get().rm.getTopRegion(l, this.getClass().getName());

            if (r != null && ((!r.canExit(p) && !r.canDrop(p)) || !r.canDrop(p))) {
                e.setCancelled(true);
                RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantdrop");
            }
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void PlayerMoveInv(InteractInventoryEvent.Close e, @Root Player p) {
        RedProtect.get().getVersionHelper().removeGuiItem(p);
    }

    public class BossBarTimer implements Consumer<Task> {
        ServerBossBar boss;

        public BossBarTimer(ServerBossBar boss) {
            this.boss = boss;
        }

        @Override
        public void accept(Task task) {
            float diff = boss.getPercent() - 0.2f;
            if (diff > 0) {
                boss.setPercent(diff);
            } else {
                boss.setVisible(false);
                boss.removePlayer(boss.getPlayers().stream().findFirst().get());
                task.cancel();
            }
        }
    }
}
