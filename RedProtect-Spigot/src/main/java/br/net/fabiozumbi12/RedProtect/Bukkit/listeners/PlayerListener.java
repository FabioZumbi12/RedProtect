/*
 * Copyright (c) 2012-2025 - @FabioZumbi12
 * Last Modified: 02/07/2025 09:45
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

package br.net.fabiozumbi12.RedProtect.Bukkit.listeners;

import br.net.fabiozumbi12.RedProtect.Bukkit.API.events.EnterExitRegionEvent;
import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import br.net.fabiozumbi12.RedProtect.Bukkit.helpers.ContainerManager;
import br.net.fabiozumbi12.RedProtect.Bukkit.helpers.DoorManager;
import br.net.fabiozumbi12.RedProtect.Bukkit.helpers.SpigotHelper;
import br.net.fabiozumbi12.RedProtect.Bukkit.hooks.WEHook;
import br.net.fabiozumbi12.RedProtect.Core.config.CoreConfigManager;
import br.net.fabiozumbi12.RedProtect.Core.helpers.LogLevel;
import br.net.fabiozumbi12.RedProtect.Core.region.PlayerRegion;
import com.connorlinfoot.actionbarapi.ActionBarAPI;
import de.Keyle.MyPet.MyPetApi;
import de.Keyle.MyPet.api.entity.MyPet.PetState;
import de.Keyle.MyPet.api.entity.MyPetBukkitEntity;
import de.Keyle.MyPet.api.player.MyPetPlayer;
import me.NoChance.PvPManager.PvPlayer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("deprecation")
public class PlayerListener implements Listener {

    private static final ContainerManager cont = new ContainerManager();
    private final HashMap<String, String> Ownerslist = new HashMap<>();
    private final HashMap<String, String> PlayerCmd = new HashMap<>();
    private final HashMap<String, Boolean> PvPState = new HashMap<>();
    private final HashMap<String, String> PlayertaskID = new HashMap<>();
    private final HashMap<String, HashMap<Integer, Location>> deathLocs = new HashMap<>();

    public PlayerListener() {
        RedProtect.get().logger.debug(LogLevel.PLAYER, "Loaded PlayerListener...");
    }

    @EventHandler(ignoreCancelled = true)
    public void onBrewing(BrewEvent e) {
        ItemStack[] cont = e.getContents().getContents();
        for (int i = 0; i < cont.length; i++) {
            if (RedProtect.get().getUtil().denyPotion(cont[i], e.getBlock().getWorld())) {
                e.getContents().setItem(i, new ItemStack(Material.AIR));
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCraftItem(PrepareItemCraftEvent e) {
        if (e.getView().getPlayer() instanceof Player p) {

            ItemStack result = e.getInventory().getResult();

            if (RedProtect.get().getUtil().denyPotion(result, p)) {
                e.getInventory().setResult(new ItemStack(Material.AIR));
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onPlayerFrostWalk(EntityBlockFormEvent e) {
        if (!(e.getEntity() instanceof Player p)) {
            return;
        }

        Region r = RedProtect.get().getRegionManager().getTopRegion(e.getBlock().getLocation());
        if (r != null && e.getNewState().getType().name().contains("FROSTED_ICE") && !r.canIceForm(p)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent e) {
        Player p = e.getPlayer();

        Region r = RedProtect.get().getRegionManager().getTopRegion(p.getLocation());
        if (r != null && !r.usePotions(p) && p.getItemInHand().getType().name().contains("POTION")) {
            e.setCancelled(true);
            RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.cantuse");
            return;
        }

        if (RedProtect.get().getUtil().denyPotion(e.getItem(), p)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerInteract(PlayerInteractEvent event) {
        RedProtect.get().logger.debug(LogLevel.PLAYER, "PlayerListener - PlayerInteractEvent canceled? " + event.isCancelled());

        final Player p = event.getPlayer();
        Block b = event.getClickedBlock();
        ItemStack itemInHand = event.getItem();

        Location l;

        if (RedProtect.get().tpWait.contains(p.getName())) {
            RedProtect.get().tpWait.remove(p.getName());
            RedProtect.get().getLanguageManager().sendMessage(p, "cmdmanager.region.tpcancelled");
        }

        if (b != null) {
            l = b.getLocation();
        } else {
            l = p.getLocation();
        }

        if (itemInHand != null && !itemInHand.getType().equals(Material.AIR)) {
            String claimmode = RedProtect.get().getConfigManager().getWorldClaimType(p.getWorld().getName());
            if (itemInHand.getType().name().equalsIgnoreCase(RedProtect.get().getConfigManager().configRoot().wands.adminWandID) && (p.hasPermission("redprotect.command.admin.wand") || (claimmode.equalsIgnoreCase("WAND") || claimmode.equalsIgnoreCase("BOTH")))) {

                if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
                    if (!RedProtect.get().getUtil().canBuildNear(p, l)) {
                        event.setCancelled(true);
                        return;
                    }
                    RedProtect.get().secondLocationSelections.put(p, l);
                    p.sendMessage(RedProtect.get().getLanguageManager().get("playerlistener.wand2") + RedProtect.get().getLanguageManager().get("general.color") + " (" + ChatColor.GOLD + l.getBlockX() + RedProtect.get().getLanguageManager().get("general.color") + ", " + ChatColor.GOLD + l.getBlockY() + RedProtect.get().getLanguageManager().get("general.color") + ", " + ChatColor.GOLD + l.getBlockZ() + RedProtect.get().getLanguageManager().get("general.color") + ").");
                    event.setCancelled(true);
                }
                if (event.getAction().equals(Action.LEFT_CLICK_BLOCK) || event.getAction().equals(Action.LEFT_CLICK_AIR)) {
                    if (!RedProtect.get().getUtil().canBuildNear(p, l)) {
                        event.setCancelled(true);
                        return;
                    }
                    RedProtect.get().firstLocationSelections.put(p, l);
                    p.sendMessage(RedProtect.get().getLanguageManager().get("playerlistener.wand1") + RedProtect.get().getLanguageManager().get("general.color") + " (" + ChatColor.GOLD + l.getBlockX() + RedProtect.get().getLanguageManager().get("general.color") + ", " + ChatColor.GOLD + l.getBlockY() + RedProtect.get().getLanguageManager().get("general.color") + ", " + ChatColor.GOLD + l.getBlockZ() + RedProtect.get().getLanguageManager().get("general.color") + ").");
                    event.setCancelled(true);
                }

                //show preview border
                if (RedProtect.get().firstLocationSelections.containsKey(p) && RedProtect.get().secondLocationSelections.containsKey(p)) {
                    Location loc1 = RedProtect.get().firstLocationSelections.get(p);
                    Location loc2 = RedProtect.get().secondLocationSelections.get(p);

                    Region reference = new Region("", loc1, loc2, p.getWorld().getName());
                    RedProtect.get().getLanguageManager().sendMessage(p, RedProtect.get().getLanguageManager().get("cmdmanager.region.distance") + reference.getArea());

                    if (RedProtect.get().hooks.checkWe() && RedProtect.get().getConfigManager().configRoot().hooks.useWECUI) {
                        WEHook.setSelectionRP(p, loc1, loc2);
                    }

                    if (loc1.getWorld().equals(loc2.getWorld()) && reference.getArea() > RedProtect.get().getConfigManager().configRoot().region_settings.max_scan && !RedProtect.get().getPermissionHandler().hasPerm(p, "redprotect.bypass.define-max-distance")) {
                        RedProtect.get().getLanguageManager().sendMessage(p, String.format(RedProtect.get().getLanguageManager().get("regionbuilder.selection.maxdefine"), RedProtect.get().getConfigManager().configRoot().region_settings.max_scan, reference.getArea()));
                    } else {
                        RedProtect.get().getUtil().addBorder(p, reference);
                    }
                }
                return;
            }

            if (itemInHand.getType().name().equalsIgnoreCase(RedProtect.get().getConfigManager().configRoot().wands.infoWandID)) {
                Region r = RedProtect.get().getRegionManager().getTopRegion(l);
                if (r == null) {
                    RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.noregion.atblock");
                } else if (RedProtect.get().getPermissionHandler().hasRegionPermMember(p, "infowand", r)) {
                    p.sendMessage(RedProtect.get().getLanguageManager().get("general.color") + "--------------- [" + ChatColor.GOLD + r.getName() + RedProtect.get().getLanguageManager().get("general.color") + "] ---------------");
                    p.sendMessage(r.info());
                    p.sendMessage(RedProtect.get().getLanguageManager().get("general.color") + "-----------------------------------------");
                } else {
                    p.sendMessage(RedProtect.get().getLanguageManager().get("playerlistener.region.entered").replace("{region}", r.getName()).replace("{leaders}", r.getLeadersDesc()));
                }
                event.setCancelled(true);
                return;
            }

            if ((itemInHand.getType().name().contains("_HOE") || (!itemInHand.getType().isBlock() && b != null && b.getType().name().equals("FARMLAND")))
                    && event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                Region r = RedProtect.get().getRegionManager().getTopRegion(l);
                if (r != null && r.canCrops()) {
                    return;
                }
            }
        }

        if (event.isCancelled()) {
            return;
        }

        Region r = RedProtect.get().getRegionManager().getTopRegion(l);
        //start player checks
        if (r == null) {
            try {
                if (b != null && (b.getType().equals(Material.ANVIL) || b.getState() instanceof InventoryHolder ||
                        RedProtect.get().getConfigManager().configRoot().private_cat.allowed_blocks.stream().anyMatch(b.getType().name()::matches))) {
                    boolean out = RedProtect.get().getConfigManager().configRoot().private_cat.allow_outside;
                    if (out && !cont.canOpen(b, p, false)) {
                        RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.cantopen");
                        event.setCancelled(true);
                    }
                }
            } catch (Exception ignored) {
            }

        } else { //if r != null >>
            //other blocks and interactions
            if (itemInHand != null && (event.getAction().name().equals("RIGHT_CLICK_BLOCK") || b == null)) {
                Material hand = itemInHand.getType();
                if (hand.equals(Material.ENDER_PEARL) && !r.canTeleport(p)) {
                    RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.cantuse");
                    event.setCancelled(true);
                    event.setUseItemInHand(Event.Result.DENY);
                    return;
                } else if ((hand.equals(Material.BOW) || (hand.name().contains("SNOW") && hand.name().contains("BALL")) || hand.name().contains("FIREWORK") || hand.equals(Material.EGG)) && !r.canProtectiles(p)) {
                    RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.cantuse");
                    event.setCancelled(true);
                    event.setUseItemInHand(Event.Result.DENY);
                    return;
                } else if (hand.equals(Material.POTION) && !r.usePotions(p)) {
                    RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.cantuse");
                    event.setCancelled(true);
                    event.setUseItemInHand(Event.Result.DENY);
                    return;
                } else if (hand.name().contains("_EGG") && !r.canInteractPassives(p)) {
                    RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.cantuse");
                    event.setCancelled(true);
                    event.setUseItemInHand(Event.Result.DENY);
                    return;
                }
            }

            //if (r != null) && (b != null) >>
            try {
                if (b != null) {
                    if (b.getType().name().endsWith("PRESSURE_PLATE")) {
                        if (!r.canPressPlate(p)) {
                            RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.cantpressplate");
                            event.setCancelled(true);
                        }
                    } else if (b.getType().equals(Material.DRAGON_EGG) ||
                            b.getType().name().contains("NOTE_BLOCK") ||
                            b.getType().name().contains("CAKE")) {

                        if (!r.canBuild(p)) {
                            RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.cantinteract");
                            event.setCancelled(true);
                        }
                    } else if (b.getType().name().equalsIgnoreCase("BED") || b.getType().name().contains("_BED")) {
                        if (!r.isHomeAllowed(p) && !r.canBuild(p)) {
                            RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.cantinteract");
                            event.setCancelled(true);
                        }
                    } else if (b.getState() instanceof Sign s && RedProtect.get().getConfigManager().configRoot().region_settings.enable_flag_sign) {
                        String[] lines = s.getLines();
                        if (lines[0].equalsIgnoreCase("[flag]") && r.getFlags().containsKey(lines[1])) {
                            String flag = lines[1];
                            if (!(r.getFlags().get(flag) instanceof Boolean)) {
                                RedProtect.get().getLanguageManager().sendMessage(p, RedProtect.get().getLanguageManager().get("playerlistener.region.sign.cantflag"));
                                return;
                            }
                            if (RedProtect.get().getPermissionHandler().hasFlagPerm(p, flag) && (RedProtect.get().getConfigManager().configRoot().flags.containsKey(flag) || CoreConfigManager.ADMIN_FLAGS.contains(flag))) {
                                if (r.isAdmin(p) || r.isLeader(p) || RedProtect.get().getPermissionHandler().hasPerm(p, "redprotect.flag.admin." + flag)) {
                                    if (RedProtect.get().getConfigManager().configRoot().flags_configuration.change_flag_delay.enable) {
                                        if (RedProtect.get().getConfigManager().configRoot().flags_configuration.change_flag_delay.flags.contains(flag)) {
                                            if (!RedProtect.get().changeWait.contains(r.getName() + flag)) {
                                                RedProtect.get().getUtil().startFlagChanger(r.getName(), flag, p);
                                                changeFlag(r, flag, p, s);
                                            } else {
                                                RedProtect.get().getLanguageManager().sendMessage(p, RedProtect.get().getLanguageManager().get("gui.needwait.tochange").replace("{seconds}", "" + RedProtect.get().getConfigManager().configRoot().flags_configuration.change_flag_delay.seconds));
                                            }
                                            return;
                                        }
                                    }
                                    changeFlag(r, flag, p, s);
                                    return;
                                }
                            }
                            RedProtect.get().getLanguageManager().sendMessage(p, "cmdmanager.region.flag.nopermregion");
                        }
                    } else if (b.getType().name().contains("LEAVES") || b.getType().name().contains("LOG") || b.getType().name().contains("_WOOD")) {
                        if (!r.canTree() && !r.canBuild(p)) {
                            RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.cantinteract");
                            event.setCancelled(true);
                        }
                    } else if (b.getType().equals(Material.ENDER_CHEST)) {
                        if (!r.canEnderChest(p)) {
                            RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.cantopen");
                            event.setCancelled(true);
                        }
                    } else if (b.getType().name().contains("SPAWNER")) {
                        if (!r.canPlaceSpawner(p)) {
                            RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.cantuse");
                            event.setCancelled(true);
                        }
                    } else if (b.getState() instanceof InventoryHolder ||
                            RedProtect.get().getConfigManager().configRoot().private_cat.allowed_blocks.stream().anyMatch(b.getType().name()::matches)) {
                        var open = cont.canOpen(b, p, true);
                        if (!open){
                            if ((r.canChest(p) && !cont.canOpen(b, p, false)) || (!r.canChest(p) && cont.canOpen(b, p, false)) || (!r.canChest(p) && !cont.canOpen(b, p, false))) {
                                RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.cantopen");
                                event.setCancelled(true);
                            }
                        }
                    } else if (b.getType().name().contains("DAYLIGHT") || b.getType().name().contains("COMPARATOR") || b.getType().name().contains("REPEATER") || (b.getType().name().contains("REDSTONE") && !b.getType().equals(Material.REDSTONE_ORE))) {
                        if (!r.canRedstone(p)) {
                            RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.cantinteract");
                            event.setCancelled(true);
                        }
                    } else if (b.getType().name().contains("LEVER")) {
                        if (!r.canLever(p)) {
                            RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.cantlever");
                            event.setCancelled(true);
                        }
                    } else if (b.getType().name().contains("LECTERN")) { // Do nothing to allow read books
                    } else if (b.getType().name().contains("BUTTON")) {
                        if (!r.canButton(p)) {
                            RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.cantbutton");
                            event.setCancelled(true);
                        }
                    } else if (DoorManager.isOpenable(b) && event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                        if (!r.canDoor(p)/* || (r.canDoor(p) && !cont.canOpen(b, p))*/) {
                            RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.cantdoor");
                            event.setCancelled(true);
                        } else {
                            DoorManager.ChangeDoor(b, r);
                        }
                    } else if (itemInHand != null && (itemInHand.getType().name().startsWith("BOAT") || itemInHand.getType().name().contains("MINECART"))) {
                        if (!r.canMinecart(p)) {
                            RedProtect.get().getLanguageManager().sendMessage(p, "blocklistener.region.cantplace");
                            event.setUseItemInHand(Event.Result.DENY);
                            event.setCancelled(true);
                        }
                    } else if (itemInHand != null && itemInHand.getType().equals(Material.WATER_BUCKET)) {
                        if (!r.canFish(p)) {
                            RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.cantinteract");
                            event.setUseItemInHand(Event.Result.DENY);
                            event.setCancelled(true);
                        }
                    } else if ((event.getAction().equals(Action.LEFT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) &&
                            b.getType().name().contains("SIGN") && !r.canSign(p)) {
                        Sign sign = (Sign) b.getState();
                        for (String tag : RedProtect.get().getConfigManager().configRoot().region_settings.allow_sign_interact_tags) {
                            //check first rule
                            if (tag.equalsIgnoreCase(sign.getLine(0))) {
                                return;
                            }

                            //check if tag is owners or members names
                            if (tag.equalsIgnoreCase("{membername}")) {
                                for (PlayerRegion leader : r.getLeaders()) {
                                    if (sign.getLine(0).equalsIgnoreCase(leader.getPlayerName())) {
                                        return;
                                    }
                                }
                                for (PlayerRegion admin : r.getAdmins()) {
                                    if (sign.getLine(0).equalsIgnoreCase(admin.getPlayerName())) {
                                        return;
                                    }
                                }
                                for (PlayerRegion member : r.getMembers()) {
                                    if (sign.getLine(0).equalsIgnoreCase(member.getPlayerName())) {
                                        return;
                                    }
                                }
                            }

                            //check if tag is player name
                            if (tag.equalsIgnoreCase("{playername}")) {
                                if (sign.getLine(0).equalsIgnoreCase(p.getName())) {
                                    return;
                                }
                            }
                        }
                        RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.cantinteract.signs");
                        event.setUseItemInHand(Event.Result.DENY);
                        event.setCancelled(true);
                    } else if ((itemInHand != null && !itemInHand.getType().equals(Material.AIR)) && !r.canBuild(p) && !r.canPlace(itemInHand.getType()) && !r.canBreak(itemInHand.getType()) &&
                            (itemInHand.getType().equals(Material.FLINT_AND_STEEL) ||
                                    itemInHand.getType().equals(Material.BUCKET) ||
                                    itemInHand.getType().equals(Material.LAVA_BUCKET) ||
                                    itemInHand.getType().equals(Material.ITEM_FRAME) ||
                                    itemInHand.getType().name().equals("END_CRYSTAL") ||
                                    (!r.canFish(p) && itemInHand.getType().equals(Material.WATER_BUCKET)) ||
                                    itemInHand.getType().equals(Material.PAINTING))) {
                        RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.cantuse");
                        event.setCancelled(true);
                        event.setUseItemInHand(Event.Result.DENY);
                        event.setUseInteractedBlock(Event.Result.DENY);
                    } else if (!r.allowMod(p) && !RedProtect.get().getUtil().isBukkitBlock(b) && !r.canBreak(b.getType()) && !r.canPlace(b.getType())) {
                        RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.cantinteract");
                        event.setCancelled(true);
                        event.setUseItemInHand(Event.Result.DENY);
                        event.setUseInteractedBlock(Event.Result.DENY);
                    }
                }
            } catch (Exception ex) {
                RedProtect.get().logger.warning("Theres an error on PlayerInteractEvent event: " + ex.getLocalizedMessage());
            }
        }
    }

    private void changeFlag(Region r, String flag, Player p, Sign s) {
        if (r.setFlag(p, flag, !r.getFlagBool(flag))) {
            RedProtect.get().getLanguageManager().sendMessage(p, RedProtect.get().getLanguageManager().get("cmdmanager.region.flag.set").replace("{flag}", "'" + flag + "'") + " " + r.getFlagBool(flag));
            RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + p.getName() + " SET FLAG " + flag + " of region " + r.getName() + " to " + RedProtect.get().getLanguageManager().translBool(r.getFlagString(flag)));
            s.setLine(3, ChatColor.translateAlternateColorCodes('&', RedProtect.get().getLanguageManager().get("region.value") + " " + RedProtect.get().getLanguageManager().translBool(r.getFlagString(flag))));
            s.update();
            if (!RedProtect.get().getConfigManager().getSigns(r.getID()).contains(s.getLocation())) {
                RedProtect.get().getConfigManager().putSign(r.getID(), s.getLocation());
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        Entity e = event.getRightClicked();
        Player p = event.getPlayer();
        if (!RedProtect.get().getUtil().isRealPlayer(p)) {
            return;
        }

        RedProtect.get().logger.debug(LogLevel.PLAYER, "PlayerListener - Is PlayerInteractEntityEvent event: " + e.getType().name());
        Location l = e.getLocation();

        if (e instanceof ItemFrame || e instanceof Painting) {
            Region r = RedProtect.get().getRegionManager().getTopRegion(l);
            if (r != null && !r.canBuild(p)) {
                RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.cantedit");
                event.setCancelled(true);
            }
        } else if (e instanceof Minecart || e instanceof Boat) {
            Region r = RedProtect.get().getRegionManager().getTopRegion(l);
            if (r != null && !r.canMinecart(p)) {
                RedProtect.get().getLanguageManager().sendMessage(p, "blocklistener.region.cantenter");
                event.setCancelled(true);
            }
        } else if (RedProtect.get().hooks.checkMyPet() && e instanceof MyPetBukkitEntity) {
            Region r = RedProtect.get().getRegionManager().getTopRegion(l);
            if (r != null && !((MyPetBukkitEntity) e).getOwner().getPlayer().equals(p)) {
                RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.cantinteract");
                event.setCancelled(true);
            }
        } else if (!RedProtect.get().getUtil().isBukkitEntity(e) && (!(event.getRightClicked() instanceof Player))) {
            Region r = RedProtect.get().getRegionManager().getTopRegion(l);
            if (r != null && !r.allowMod(p)) {
                RedProtect.get().logger.debug(LogLevel.PLAYER, "PlayerInteractEntityEvent - Block is " + event.getRightClicked().getType().name());
                RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.cantinteract");
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageEvent(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player play)) {
            return;
        }

        if (!RedProtect.get().getUtil().isRealPlayer(play)) {
            return;
        }

        if (RedProtect.get().tpWait.contains(play.getName())) {
            RedProtect.get().tpWait.remove(play.getName());
            RedProtect.get().getLanguageManager().sendMessage(e.getEntity(), RedProtect.get().getLanguageManager().get("cmdmanager.region.tpcancelled"));
        }

        //deny damagecauses
        List<String> Causes = RedProtect.get().getConfigManager().globalFlagsRoot().worlds.get(play.getWorld().getName()).deny_death_by;
        if (!Causes.isEmpty()) {
            for (String cause : Causes) {
                cause = cause.toUpperCase();
                try {
                    if (e.getCause().equals(DamageCause.valueOf(cause))) {
                        e.setCancelled(true);
                    }
                } catch (IllegalArgumentException ex) {
                    RedProtect.get().logger.severe("The config 'deny_death_by' have an unknown damage cause type. Change to a valid damage cause type.");
                }
            }
        }

        Map<String, String> causeCommand = RedProtect.get().getConfigManager().globalFlagsRoot().worlds.get(play.getWorld().getName()).command_on_damage;
        if (causeCommand.containsKey(e.getCause().name())) {
            String cmd = causeCommand.get(e.getCause().name())
                    .replace("{player}", play.getName())
                    .replace("{damage}", e.getCause().name());
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd);
            RedProtect.get().logger.info("Player " + play.getName() + " damaged by " + e.getCause().name() + " - Running command:\n - Command: " + cmd);
        }

        Region r = RedProtect.get().getRegionManager().getTopRegion(play.getLocation());
        if (r != null) {
            if (!r.canPlayerDamage()) {
                e.setCancelled(true);
            }
            //execute on health
            if (r.cmdOnHealth(play)) {
                RedProtect.get().logger.debug(LogLevel.PLAYER, "Cmd on healt: true");
            }

            if (!r.canDeath() && play.getHealth() <= 1) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent e) {
        Player p = null;

        RedProtect.get().logger.debug(LogLevel.PLAYER, "RPLayerListener: Is EntityDamageByEntityEvent event");

        if (e.getDamager() instanceof Player) {
            p = (Player) e.getDamager();
        } else if (e.getDamager() instanceof Projectile proj) {
            if (proj.getShooter() instanceof Player) {
                p = (Player) proj.getShooter();
            }
        }

        if (p != null) {
            RedProtect.get().logger.debug(LogLevel.PLAYER, "Player: " + p.getName());
        } else {
            RedProtect.get().logger.debug(LogLevel.PLAYER, "Player: is null");
            return;
        }

        if (!RedProtect.get().getUtil().isRealPlayer(p)) {
            return;
        }

        RedProtect.get().logger.debug(LogLevel.PLAYER, "Damager: " + e.getDamager().getType().name());

        Location l = e.getEntity().getLocation();
        Region r = RedProtect.get().getRegionManager().getTopRegion(l);
        if (r == null) {
            return;
        }

        if (RedProtect.get().tpWait.contains(p.getName())) {
            RedProtect.get().tpWait.remove(p.getName());
            RedProtect.get().getLanguageManager().sendMessage(p, "cmdmanager.region.tpcancelled");
        }

        if (e.getEntity() instanceof Player && !p.equals(e.getEntity()) && r.flagExists("pvp") && !r.canPVP((Player) e.getEntity(), p)) {
            RedProtect.get().getLanguageManager().sendMessage(p, "entitylistener.region.cantpvp");
            e.setCancelled(true);
        }

        if ((e.getEntity() instanceof Hanging || e.getEntity() instanceof EnderCrystal) && !r.canBuild(p) && !r.canBreak(e.getEntityType())) {
            RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.cantremove");
            e.setCancelled(true);
        }

        if ((e.getEntity() instanceof Boat || e.getEntity() instanceof Minecart) && !r.canMinecart(p)) {
            RedProtect.get().getLanguageManager().sendMessage(p, "blocklistener.region.cantbreak");
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent e) {
        final Player p = e.getPlayer();
        if (!RedProtect.get().getUtil().isRealPlayer(p)) {
            return;
        }

        if (RedProtect.get().tpWait.contains(p.getName())) {
            RedProtect.get().tpWait.remove(p.getName());
            RedProtect.get().getLanguageManager().sendMessage(p, "cmdmanager.region.tpcancelled");
        }

        Location lfrom = e.getFrom();
        Location lto = e.getTo();
        final Region rfrom = RedProtect.get().getRegionManager().getTopRegion(lfrom);
        final Region rto = RedProtect.get().getRegionManager().getTopRegion(lto);

        RedProtect.get().logger.debug(LogLevel.PLAYER, "PlayerListener - PlayerTeleportEvent from " + lfrom + " to " + lto);

        if (rfrom != null) {

            //Exit flag
            if (!rfrom.canExit(p) && !RedProtect.get().getPermissionHandler().hasPermOrBypass(p, "redprotect.flag.admin.exit")) {
                e.setTo(RedProtect.get().getUtil().DenyExitPlayer(p, lfrom, e.getTo(), rfrom));
                return;
            }

            //canMove flag
            if (!rfrom.canMove(p) && !RedProtect.get().getPermissionHandler().hasPermOrBypass(p, "redprotect.flag.admin.move")) {
                RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.cantmove");
                e.setCancelled(true);
                return;
            }
        }

        if (rto != null) {

            //Allow teleport to with items
            if (!rto.canEnterWithItens(p)) {
                RedProtect.get().getLanguageManager().sendMessage(p, RedProtect.get().getLanguageManager().get("playerlistener.region.onlyenter.withitems").replace("{items}", rto.getFlags().get("allow-enter-items").toString()));
                e.setCancelled(true);
                return;
            }

            //Deny teleport to with item
            if (!rto.denyEnterWithItens(p)) {
                RedProtect.get().getLanguageManager().sendMessage(p, RedProtect.get().getLanguageManager().get("playerlistener.region.denyenter.withitems").replace("{items}", rto.getFlags().get("deny-enter-items").toString()));
                e.setCancelled(true);
                return;
            }

            if (RedProtect.get().hooks.checkPvPm()) {
                if (rto.isPvPArena() && !PvPlayer.get(p).hasPvPEnabled() && !rto.canBuild(p)) {
                    RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.pvpenabled");
                    e.setCancelled(true);
                    return;
                }
            }

            if (!rto.canEnter(p)) {
                RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.cantregionenter");
                e.setCancelled(true);
                return;
            }

            //enter max players flag
            if (rto.getMaxPlayers() != -1) {
                if (!checkMaxPlayer(p, rto) && rfrom != rto) {
                    RedProtect.get().getLanguageManager().sendMessage(p, RedProtect.get().getLanguageManager().get("playerlistener.region.maxplayers").replace("{players}", String.valueOf(rto.getMaxPlayers())));
                    e.setCancelled(true);
                }
            }

            if (PlayerCmd.containsKey(p.getName())) {
                if (!rto.canBack(p) && PlayerCmd.get(p.getName()).toLowerCase().contains("back")) {
                    RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.cantback");
                    e.setCancelled(true);
                }
                if (!rto.isHomeAllowed(p) && PlayerCmd.get(p.getName()).toLowerCase().contains("home")) {
                    RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.canthome");
                    e.setCancelled(true);
                }
                PlayerCmd.remove(p.getName());
            }
        }


        //teleport player to coord/world if playerup 128 y
        int NetherY = RedProtect.get().getConfigManager().configRoot().nether_protection.maxYsize;
        if (lto.getWorld().getEnvironment().equals(World.Environment.NETHER) && NetherY != -1 && lto.getBlockY() >= NetherY && !p.hasPermission("redprotect.bypass.nether-roof")) {
            RedProtect.get().getLanguageManager().sendMessage(p, RedProtect.get().getLanguageManager().get("playerlistener.upnethery").replace("{location}", NetherY + ""));
            e.setCancelled(true);
        }

        if (e.getCause().equals(PlayerTeleportEvent.TeleportCause.ENDER_PEARL) || e.getCause().name().contains("CHORUS_FRUIT")) {
            if (rfrom != null && !rfrom.canTeleport(p)) {
                RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.cantteleportitem");
                e.setCancelled(true);
            }
            if (rto != null && !rto.canTeleport(p)) {
                RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.cantteleportitem");
                e.setCancelled(true);
            }
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(RedProtect.get(), () -> {
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
                            p.setFlying(false);
                            p.setAllowFlight(false);
                        } else {
                            for (PotionEffect pot : p.getActivePotionEffects()) {
                                p.removePotionEffect(pot.getType());
                            }
                        }
                        toRemove.add(taskId);
                        stopTaskPlayer(taskId);
                    }
                }
                for (String key : toRemove) {
                    PlayertaskID.remove(key);
                }
            }
        }, 20L);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();

        if (RedProtect.get().tpWait.contains(p.getName())) {
            RedProtect.get().tpWait.remove(p.getName());
            RedProtect.get().getLanguageManager().sendMessage(p, "cmdmanager.region.tpcancelled");
        }

        String msg = e.getMessage();
        String cmds = msg.split(" ")[0].toLowerCase().replace("/", "");
        //RedProtect.get().logger.severe("Command: "+msg);

        if (RedProtect.get().getConfigManager().configRoot().server_protection.deny_commands_on_worlds.getOrDefault(p.getWorld().getName(), new ArrayList<>()).contains(cmds) && !p.hasPermission("redprotect.bypass")) {
            RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.command-notallowed");
            e.setCancelled(true);
            return;
        }

        Region r = RedProtect.get().getRegionManager().getTopRegion(p.getLocation());

        if (RedProtect.get().getConfigManager().globalFlagsRoot().worlds.get(p.getWorld().getName()).command_ranges.containsKey(cmds.toLowerCase().replace("/", "")) && !cmds.equals(".")) {
            double min = RedProtect.get().getConfigManager().globalFlagsRoot().worlds.get(p.getWorld().getName()).command_ranges.get(cmds).min_range;
            double max = RedProtect.get().getConfigManager().globalFlagsRoot().worlds.get(p.getWorld().getName()).command_ranges.get(cmds).max_range;
            String mesg = RedProtect.get().getConfigManager().globalFlagsRoot().worlds.get(p.getWorld().getName()).command_ranges.get(cmds).message;
            boolean allowInside = RedProtect.get().getConfigManager().globalFlagsRoot().worlds.get(p.getWorld().getName()).command_ranges.get(cmds).allowInsideRegion;
            double py = p.getLocation().getY();
            if ((py < min || py > max) && (r == null || !allowInside)) {
                if (mesg != null && !mesg.isEmpty()) {
                    RedProtect.get().getLanguageManager().sendMessage(p, mesg);
                }
                e.setCancelled(true);
                return;
            }
        }

        if (cmds.startsWith("back") || cmds.startsWith("home") || cmds.contains(":back") || cmds.contains(":home")) {
            PlayerCmd.put(p.getName(), msg);
        }

        if (r != null) {

            if ((cmds.equalsIgnoreCase("petc") || cmds.equalsIgnoreCase("petcall") || cmds.contains(":petc") || cmds.contains(":petcall"))
                    && RedProtect.get().hooks.checkMyPet() && !r.canPet(p)) {
                RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.cantpet");
                e.setCancelled(true);
                return;
            }

            if (!r.isCmdAllowed(msg)) {
                if (cmds.equalsIgnoreCase("rp") || cmds.equalsIgnoreCase("redprotect")) {
                    return;
                }
                RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.cantcommand");
                e.setCancelled(true);
                return;
            }

            if (!r.isCmdDenied(msg)) {
                for (String alias : RedProtect.get().getCommand("RedProtect").getAliases()) {
                    if (cmds.equalsIgnoreCase(alias)) {
                        return;
                    }
                }

                RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.cantcommand");
                e.setCancelled(true);
                return;
            }

            if ((cmds.equalsIgnoreCase("sethome") || cmds.contains(":sethome")) && !r.isHomeAllowed(p)) {
                RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.canthome");
                e.setCancelled(true);
                return;
            }

            //Pvp check
            if ((cmds.equalsIgnoreCase("pvp") || cmds.contains(":pvp")) && RedProtect.get().hooks.checkPvPm()) {
                if (r.isPvPArena() && !PvPlayer.get(p).hasPvPEnabled() && !r.canBuild(p)) {
                    RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.pvpenabled");
                    RedProtect.get().getServer().dispatchCommand(RedProtect.get().getServer().getConsoleSender(), RedProtect.get().getConfigManager().configRoot().flags_configuration.pvparena_nopvp_kick_cmd.replace("{player}", p.getName()));
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDie(PlayerDeathEvent e) {
        Player p = e.getEntity();
        if (!RedProtect.get().getUtil().isRealPlayer(p)) {
            return;
        }

        if (RedProtect.get().tpWait.contains(p.getName())) {
            RedProtect.get().tpWait.remove(p.getName());
            RedProtect.get().getLanguageManager().sendMessage(p, "cmdmanager.region.tpcancelled");
        }

        Location loc = p.getLocation();
        Region r = RedProtect.get().getRegionManager().getTopRegion(loc);

        if (r != null) {
            if (r.isKeepInventory()) {
                e.setKeepInventory(true);
                e.getDrops().clear();
            }
            if (r.isKeepLevels()) {
                e.setKeepLevel(true);
                e.setDroppedExp(0);
            }
        }

        //check for death listener
        deathListener(p, 0);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        if (!RedProtect.get().getUtil().isRealPlayer(p)) {
            return;
        }

        //check for death listener
        deathListener(p, 1);
    }

    private void deathListener(Player p, int index) {
        RedProtect.get().logger.debug(LogLevel.PLAYER, "Added index " + index);

        HashMap<Integer, Location> loc1 = new HashMap<>();
        if (!deathLocs.containsKey(p.getName())) {
            loc1.put(index, p.getLocation());
            deathLocs.put(p.getName(), loc1);
        } else {
            loc1 = deathLocs.get(p.getName());

            loc1.put(index, p.getLocation());
            deathLocs.put(p.getName(), loc1);

            if (loc1.size() == 2) {
                Location from = deathLocs.get(p.getName()).get(0);
                Location to = deathLocs.get(p.getName()).get(1);
                deathLocs.remove(p.getName());
                PlayerTeleportEvent televent = new PlayerTeleportEvent(p, from, to, TeleportCause.PLUGIN);
                Bukkit.getPluginManager().callEvent(televent);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMovement(PlayerMoveEvent e) {
        if (RedProtect.get().getConfigManager().configRoot().performance.disable_onPlayerMoveEvent_handler) {
            return;
        }

        Player p = e.getPlayer();
        if (!RedProtect.get().getUtil().isRealPlayer(p)) {
            return;
        }

        Location lfrom = e.getFrom();
        Location lto = e.getTo();

        Region rfrom = RedProtect.get().getRegionManager().getTopRegion(lfrom);

        //Exit flag
        if (rfrom != null && !rfrom.canExit(p) && !RedProtect.get().getPermissionHandler().hasPermOrBypass(p, "redprotect.flag.admin.exit")) {
            e.setTo(RedProtect.get().getUtil().DenyExitPlayer(p, lfrom, e.getTo(), rfrom));
            return;
        }

        if (lto.getWorld().equals(lfrom.getWorld()) && lto.distance(lfrom) > 0.1 && RedProtect.get().tpWait.contains(p.getName())) {
            RedProtect.get().tpWait.remove(p.getName());
            RedProtect.get().getLanguageManager().sendMessage(p, "cmdmanager.region.tpcancelled");
        }

        //teleport player to coord/world if playerup 128 y
        int NetherY = RedProtect.get().getConfigManager().configRoot().nether_protection.maxYsize;
        if (lto.getWorld().getEnvironment().equals(World.Environment.NETHER) && NetherY != -1 && lto.getBlockY() >= NetherY && !p.hasPermission("redprotect.bypass.nether-roof")) {
            for (String cmd : RedProtect.get().getConfigManager().configRoot().nether_protection.execute_cmd) {
                RedProtect.get().getServer().dispatchCommand(RedProtect.get().getServer().getConsoleSender(), cmd.replace("{player}", p.getName()));
            }
            RedProtect.get().getLanguageManager().sendMessage(p, RedProtect.get().getLanguageManager().get("playerlistener.upnethery").replace("{location}", NetherY + ""));
        }

        Region r = RedProtect.get().getRegionManager().getTopRegion(lto);

        World w = lfrom.getWorld();

        if (r != null) {

            //Enter flag
            if (!r.canEnter(p) && !RedProtect.get().getPermissionHandler().hasPermOrBypass(p, "redprotect.flag.admin.enter")) {
                Location loc = RedProtect.get().getUtil().DenyEnterPlayer(w, lfrom, e.getTo(), r, false);
                if (p.isInsideVehicle()) {
                    Entity vehicle = p.getVehicle();
                    vehicle.eject();
                    Bukkit.getScheduler().runTaskLater(RedProtect.get(), () -> vehicle.teleport(loc), 1);
                }
                e.setTo(loc);
                RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.cantregionenter");
                return;
            }

            //canMove flag
            if (!r.canMove(p) && !RedProtect.get().getPermissionHandler().hasPermOrBypass(p, "redprotect.flag.admin.move")) {
                RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.cantmove");
                e.setCancelled(true);
                return;
            }

            //enter max players flag
            if (r.getMaxPlayers() != -1 && !RedProtect.get().getPermissionHandler().hasPermOrBypass(p, "redprotect.flag.admin.max-players")) {
                if (!checkMaxPlayer(p, r) && rfrom != r) {
                    e.setTo(RedProtect.get().getUtil().DenyEnterPlayer(w, lfrom, e.getTo(), r, false));
                    RedProtect.get().getLanguageManager().sendMessage(p, RedProtect.get().getLanguageManager().get("playerlistener.region.maxplayers").replace("{players}", String.valueOf(r.getMaxPlayers())));
                    return;
                }
            }

            //remove pots
            if (!r.canGetEffects(p) && !RedProtect.get().getPermissionHandler().hasPermOrBypass(p, "redprotect.flag.admin.allow-effect")) {
                for (PotionEffect pot : p.getActivePotionEffects()) {
                    if (pot.getDuration() < 36000) {
                        p.removePotionEffect(pot.getType());
                    }
                }
            }

            //Mypet Flag
            if (RedProtect.get().hooks.checkMyPet() && !r.canPet(p) && !RedProtect.get().getPermissionHandler().hasPermOrBypass(p, "redprotect.flag.admin.can-pet")) {
                if (MyPetApi.getPlayerManager().isMyPetPlayer(p)) {
                    MyPetPlayer mpp = MyPetApi.getPlayerManager().getMyPetPlayer(p);
                    if (mpp.hasMyPet() && mpp.getMyPet().getStatus() == PetState.Here) {
                        mpp.getMyPet().removePet();
                        RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.cantpet");
                    }
                }
            }

            //Allow to enter with items
            if (!r.canEnterWithItens(p) && !RedProtect.get().getPermissionHandler().hasPermOrBypass(p, "redprotect.flag.admin.allow-enter-items")) {
                e.setTo(RedProtect.get().getUtil().DenyEnterPlayer(w, lfrom, e.getTo(), r, false));
                RedProtect.get().getLanguageManager().sendMessage(p, RedProtect.get().getLanguageManager().get("playerlistener.region.onlyenter.withitems").replace("{items}", r.getFlags().get("allow-enter-items").toString()));
                return;
            }

            //Deny enter with item
            if (!r.denyEnterWithItens(p) && !RedProtect.get().getPermissionHandler().hasPermOrBypass(p, "redprotect.flag.admin.deny-enter-items")) {
                e.setTo(RedProtect.get().getUtil().DenyEnterPlayer(w, lfrom, e.getTo(), r, false));
                RedProtect.get().getLanguageManager().sendMessage(p, RedProtect.get().getLanguageManager().get("playerlistener.region.denyenter.withitems").replace("{items}", r.getFlags().get("deny-enter-items").toString()));
                return;
            }

            //Deny Fly
            if (!p.getGameMode().toString().equalsIgnoreCase("SPECTATOR") && !r.canFly(p) && p.isFlying() && !RedProtect.get().getPermissionHandler().hasPermOrBypass(p, "redprotect.flag.admin.allow-fly")) {
                p.setFlying(false);
                if (!p.isOnGround()) { // Prevent glitch
                    e.setTo(e.getFrom());
                }
                //p.setAllowFlight(false);
                RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.cantfly");
            }

            //update region admin or leader visit
            if (RedProtect.get().getConfigManager().configRoot().region_settings.record_player_visit_method.equalsIgnoreCase("ON-REGION-ENTER")) {
                if (r.isLeader(p) || r.isAdmin(p)) {
                    if (r.getDate() == null || (!r.getDate().equals(RedProtect.get().getUtil().dateNow()))) {
                        r.setDate(RedProtect.get().getUtil().dateNow());
                    }
                }
            }

            if (!Ownerslist.containsKey(p.getName()) || !Ownerslist.get(p.getName()).equals(r.getID())) {
                Region er = RedProtect.get().getRegionManager().getRegionById(Ownerslist.get(p.getName()));
                Ownerslist.put(p.getName(), r.getID());

                //Execute listener:
                EnterExitRegionEvent event = new EnterExitRegionEvent(er, r, p);
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) {
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
            if (Ownerslist.get(p.getName()) != null) {
                Region er = RedProtect.get().getRegionManager().getRegionById(Ownerslist.get(p.getName()));
                Ownerslist.remove(p.getName());

                //Execute listener:
                EnterExitRegionEvent event = new EnterExitRegionEvent(er, null, p);
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    return;
                }
                //---
                if (er == null) {
                    //remove all if no regions
                    List<String> toRemove = new ArrayList<>();
                    for (String taskId : PlayertaskID.keySet()) {
                        if (PlayertaskID.get(taskId).equals(p.getName())) {
                            if (taskId.contains("forcefly")) {
                                p.setFlying(false);
                                p.setAllowFlight(false);
                            } else {
                                for (PotionEffect pot : p.getActivePotionEffects()) {
                                    p.removePotionEffect(pot.getType());
                                }
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
                    if (!er.getWelcome().equalsIgnoreCase("hide ") && RedProtect.get().getConfigManager().configRoot().notify.region_exit) {
                        SendNotifyMsg(p, RedProtect.get().getLanguageManager().get("playerlistener.region.wilderness"), "RED");
                    }
                }
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
            Region reg = RedProtect.get().getRegionManager().getTopRegion(onp.getLocation());
            if (reg != null && reg == r) {
                ttl++;
            }
        }
        return ttl < r.getMaxPlayers();
    }

    @EventHandler
    public void onPlayerEnterPortal(PlayerPortalEvent e) {
        Player p = e.getPlayer();

        Region rto = null;
        Region from = RedProtect.get().getRegionManager().getTopRegion(e.getFrom());
        if (e.getTo() != null) {
            rto = RedProtect.get().getRegionManager().getTopRegion(e.getTo());
        }

        if (rto != null && !rto.canExitPortal(p)) {
            RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.cantteleport");
            e.setCancelled(true);
        }

        if (from != null && !from.canEnterPortal(p)) {
            RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.cantenterteleport");
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPortalCreate(PortalCreateEvent e) {
        RedProtect.get().getVersionHelper().getPortalLocations(e).forEach(l -> {
            Region r = RedProtect.get().getRegionManager().getTopRegion(l);
            if (r != null && !r.canCreatePortal()) {
                e.setCancelled(true);
            }
        });
    }

    @EventHandler
    public void PlayerLogin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        Bukkit.getScheduler().runTaskAsynchronously(RedProtect.get(), () -> {

            if (RedProtect.get().getConfigManager().configRoot().online_mode) {

                String uuid = p.getUniqueId().toString();
                RedProtect.get().getRegionManager().getMemberRegions(uuid).forEach(r -> {
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
                RedProtect.get().getRegionManager().getMemberRegions(pName).forEach(r -> {
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

            RedProtect.get().getRegionManager().getAdminRegions(p.getUniqueId().toString()).forEach(r -> {
                if (RedProtect.get().getConfigManager().configRoot().region_settings.record_player_visit_method.equalsIgnoreCase("ON-LOGIN")
                        && (r.isAdmin(p.getUniqueId().toString()) || r.isLeader(p.getUniqueId().toString()))) {
                    if (r.getDate() == null || !r.getDate().equals(RedProtect.get().getUtil().dateNow())) {
                        r.setDate(RedProtect.get().getUtil().dateNow());
                    }
                }
            });
        });

        Region r = RedProtect.get().getRegionManager().getTopRegion(p.getLocation());
        if (r != null) {
            RegionFlags(r, null, p, true);
        }

        // Add to block manager
        if (RedProtect.get().getBlockManager().isEnabled())
            RedProtect.get().getBlockManager().addPlayer(p);

        EnterExitRegionEvent event = new EnterExitRegionEvent(null, r, p);
        Bukkit.getPluginManager().callEvent(event);
    }

    @EventHandler
    public void PlayerTrownEgg(PlayerEggThrowEvent e) {
        Location l = e.getEgg().getLocation();
        Player p = e.getPlayer();
        Region r = RedProtect.get().getRegionManager().getTopRegion(l);

        if (r != null && !r.canBuild(p)) {
            e.setHatching(false);
            RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.canthatch");
        }
    }

    @EventHandler
    public void PlayerTrownArrow(ProjectileLaunchEvent e) {
        if (!(e.getEntity().getShooter() instanceof Player p)) {
            return;
        }

        RedProtect.get().logger.debug(LogLevel.PLAYER, "Is ProjectileLaunchEvent event.");

        Location l = e.getEntity().getLocation();
        Region r = RedProtect.get().getRegionManager().getTopRegion(l);

        ItemStack hand = p.getItemInHand();
        if (r != null) {
            if (hand.getType().equals(Material.FISHING_ROD)) {
                if (!r.canFish(p)) {
                    e.setCancelled(true);
                    RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.cantuse");
                }
                return;
            }

            if (hand.getType().name().contains("POTION") && !r.usePotions(p)) {
                e.setCancelled(true);
                RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.cantuse");
            }

            if (!r.canProtectiles(p)) {
                e.setCancelled(true);
                RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.cantuse");
            }
        }
    }

    @EventHandler
    public void PlayerDropItem(PlayerDropItemEvent e) {
        RedProtect.get().logger.debug(LogLevel.PLAYER, "Is PlayerDropItemEvent event.");

        Location l = e.getItemDrop().getLocation();
        Player p = e.getPlayer();
        Region r = RedProtect.get().getRegionManager().getTopRegion(l);

        if (r != null && ((!r.canExit(p) && !r.canDrop(p)) || !r.canDrop(p))) {
            e.setCancelled(true);
            RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.cantdrop");
        }
    }

    @EventHandler
    public void PlayerPickup(PlayerPickupItemEvent e) {
        RedProtect.get().logger.debug(LogLevel.PLAYER, "Is PlayerPickupItemEvent event.");

        Location l = e.getItem().getLocation();
        Player p = e.getPlayer();
        Region r = RedProtect.get().getRegionManager().getTopRegion(l);

        if (r != null && ((!r.canEnter(p) && !r.canPickup(p) || !r.canPickup(p)))) {
            e.setCancelled(true);
            RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.cantpickup");
        }
    }

    @EventHandler
    public void PlayerTrownPotion(PotionSplashEvent e) {
        if (!(e.getPotion().getShooter() instanceof Player p)) {
            return;
        }

        Entity ent = e.getEntity();

        RedProtect.get().logger.debug(LogLevel.PLAYER, "Is PotionSplashEvent event.");

        Region r = RedProtect.get().getRegionManager().getTopRegion(ent.getLocation());
        if (r != null && !r.usePotions(p)) {
            RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.cantuse");
            e.setCancelled(true);
            return;
        }

        //deny potion
        if (RedProtect.get().getUtil().denyPotion(e.getPotion().getItem(), p)) {
            e.setCancelled(true);
        }
    }

    private void SendNotifyMsg(Player p, String notify, String color) {
        if (RedProtect.get().getConfigManager().configRoot().notify.region_enter_mode.equalsIgnoreCase("OFF")) {
            return;
        }
        if (!notify.isEmpty()) {
            if (RedProtect.get().getConfigManager().configRoot().notify.region_enter_mode.equalsIgnoreCase("BOSSBAR")) {
                Compat111.sendBarMsg(notify, color, p);
            }
            if (RedProtect.get().getConfigManager().configRoot().notify.region_enter_mode.equalsIgnoreCase("ACTIONBAR")) {
                try {
                    Class.forName("net.md_5.bungee.api.chat.BaseComponent");
                    SpigotHelper.sendSpigotActionBar(p, notify);
                } catch (Exception ignored) {
                    if (RedProtect.get().hooks.checkAB()) {
                        ActionBarAPI.sendActionBar(p, notify);
                    } else {
                        p.sendMessage(notify);
                    }
                }
            }
            if (RedProtect.get().getConfigManager().configRoot().notify.region_enter_mode.equalsIgnoreCase("CHAT")) {
                p.sendMessage(notify);
            }
        }
    }

    private void SendWelcomeMsg(final Player p, String wel) {
        if (RedProtect.get().getConfigManager().configRoot().notify.welcome_mode.equalsIgnoreCase("OFF")) {
            return;
        }
        if (RedProtect.get().getConfigManager().configRoot().notify.welcome_mode.equalsIgnoreCase("BOSSBAR")) {
            Compat111.sendBarMsg(wel, "GREEN", p);
        }
        if (RedProtect.get().getConfigManager().configRoot().notify.region_enter_mode.equalsIgnoreCase("ACTIONBAR")) {
            try {
                Class.forName("net.md_5.bungee.api.chat.BaseComponent");
                SpigotHelper.sendSpigotActionBar(p, wel);
            } catch (Exception ignored) {
                if (RedProtect.get().hooks.checkAB()) {
                    ActionBarAPI.sendActionBar(p, wel);
                } else {
                    p.sendMessage(wel);
                }
            }
        }
        if (RedProtect.get().getConfigManager().configRoot().notify.welcome_mode.equalsIgnoreCase("CHAT")) {
            p.sendMessage(wel);
        }
    }

    private void stopTaskPlayer(String taskId) {
        Bukkit.getScheduler().cancelTask(Integer.parseInt(taskId.split("_")[0]));
    }

    private void stopTaskPlayer(Player p) {
        List<String> toremove = new ArrayList<>();
        for (String taskId : PlayertaskID.keySet()) {
            if (PlayertaskID.get(taskId).equals(p.getName())) {
                Bukkit.getScheduler().cancelTask(Integer.parseInt(taskId.split("_")[0]));
                toremove.add(taskId);
            }
        }
        for (String remove : toremove) {
            PlayertaskID.remove(remove);
            RedProtect.get().logger.debug(LogLevel.PLAYER, "Removed task ID: " + remove + " for player " + p.getName());
        }
        toremove.clear();
    }

    private void EnterExitNotify(Region r, Player p) {
        if (RedProtect.get().getConfigManager().configRoot().notify.region_enter_mode.equalsIgnoreCase("OFF")) {
            return;
        }

        if (!r.canEnter(p)) {
            return;
        }

        String leaderstring;
        String m = "";
        //Enter-Exit notifications
        if (r.getWelcome().isEmpty()) {
            if (RedProtect.get().getConfigManager().configRoot().notify.region_enter_mode.equalsIgnoreCase("BOSSBAR")
                    || RedProtect.get().getConfigManager().configRoot().notify.region_enter_mode.equalsIgnoreCase("ACTIONBAR")
                    || RedProtect.get().getConfigManager().configRoot().notify.region_enter_mode.equalsIgnoreCase("CHAT")) {
                StringBuilder leaderstringBuilder = new StringBuilder();
                for (PlayerRegion leader : r.getLeaders()) {
                    leaderstringBuilder.append(", ").append(leader.getPlayerName());
                }
                leaderstring = leaderstringBuilder.toString();

                if (!r.getLeaders().isEmpty()) {
                    leaderstring = leaderstring.substring(2);
                } else {
                    leaderstring = "None";
                }
                m = RedProtect.get().getLanguageManager().get("playerlistener.region.entered");
                m = m.replace("{leaders}", leaderstring);
                m = m.replace("{region}", r.getName());
            }
            SendNotifyMsg(p, m, "GREEN");
        } else {
            String wel = ChatColor.translateAlternateColorCodes('&',
                    r.getWelcome().replace("{r}", r.getName())
                            .replace("{player}", p.getName()));
            SendWelcomeMsg(p, RedProtect.get().getLanguageManager().get("playerlistener.region.welcome")
                    .replace("{region}", r.getName()).replace("{message}", wel));
        }
    }

    private void RegionFlags(final Region r, Region er, final Player p, boolean join) {

        if (r.canEnter(p) || RedProtect.get().getPermissionHandler().hasPermOrBypass(p, "redprotect.flag.admin.enter")) {

            //prevent spam commands
            if (join || RedProtect.get().getRegionManager().getTopRegion(p.getLocation()) != r) {

                //Enter command as player
                if (r.flagExists("player-enter-command") && !RedProtect.get().getPermissionHandler().hasPermOrBypass(p, "redprotect.flag.admin.player-enter-command")) {
                    String[] cmds = r.getFlagString("player-enter-command").split(",");
                    for (String cmd : cmds) {
                        if (cmd.startsWith("/")) {
                            cmd = cmd.substring(1);
                        }
                        p.getServer().dispatchCommand(p, cmd.replace("{player}", p.getName()).replace("{region}", r.getName()));
                    }
                }

                //Enter command as console
                if (r.flagExists("server-enter-command") && !RedProtect.get().getPermissionHandler().hasPermOrBypass(p, "redprotect.flag.admin.server-enter-command")) {
                    String[] cmds = r.getFlagString("server-enter-command").split(",");
                    for (String cmd : cmds) {
                        if (cmd.startsWith("/")) {
                            cmd = cmd.substring(1);
                        }
                        RedProtect.get().getServer().dispatchCommand(RedProtect.get().getServer().getConsoleSender(), cmd.replace("{player}", p.getName()).replace("{region}", r.getName()));
                    }
                }
            }

            //Pvp check to enter on region
            if (RedProtect.get().hooks.checkPvPm() && !RedProtect.get().getPermissionHandler().hasPermOrBypass(p, "redprotect.flag.admin.forcepvp")) {
                if (r.isPvPArena() && !PvPlayer.get(p).hasPvPEnabled() && !r.canBuild(p)) {
                    RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.pvpenabled");
                    RedProtect.get().getServer().dispatchCommand(RedProtect.get().getServer().getConsoleSender(), RedProtect.get().getConfigManager().configRoot().flags_configuration.pvparena_nopvp_kick_cmd.replace("{player}", p.getName()));
                }
            }

            //Enter Gamemode flag
            if (r.flagExists("gamemode") && !RedProtect.get().getPermissionHandler().hasPermOrBypass(p, "redprotect.flag.admin.gamemode")) {
                p.setGameMode(GameMode.valueOf(r.getFlagString("gamemode").toUpperCase()));
            }

            //Check portal (/rp flag set-portal <rp> <world>
            if (r.flagExists("set-portal")) {
                if (RedProtect.get().teleportDelay.contains(p.getName())) {
                    //RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.portal.wait");
                    return;
                } else {
                    String[] cmds = r.getFlagString("set-portal").split(" ");
                    RedProtect.get().teleportDelay.add(p.getName());
                    RedProtect.get().getServer().dispatchCommand(RedProtect.get().getServer().getConsoleSender(), "rp teleport " + cmds[0] + " " + cmds[1] + " " + p.getName());
                    Bukkit.getScheduler().runTaskLater(RedProtect.get(), () -> RedProtect.get().teleportDelay.remove(p.getName()), RedProtect.get().getConfigManager().configRoot().region_settings.portal_delay * 20L);
                }
            }

            // Collision Flag
            p.setCollidable(r.canCollide());
        }

        if (er != null && (er.canExit(p) || RedProtect.get().getPermissionHandler().hasPermOrBypass(p, "redprotect.flag.admin.exit"))) {

            //Exit gamemode
            if (er.flagExists("gamemode") && !RedProtect.get().getPermissionHandler().hasPermOrBypass(p, "redprotect.flag.admin.gamemode")) {
                p.setGameMode(Bukkit.getServer().getDefaultGameMode());
            }

            //Exit effect
            if (er.flagExists("effects") && !RedProtect.get().getPermissionHandler().hasPermOrBypass(p, "redprotect.flag.admin.effects")) {
                String[] effects = er.getFlagString("effects").split(",");
                for (String effect : effects) {
                    if (PlayertaskID.containsValue(p.getName())) {
                        String eff = effect.split(" ")[0];
                        String amplifier = effect.split(" ")[1];
                        PotionEffect fulleffect = new PotionEffect(PotionEffectType.getByName(eff), RedProtect.get().getConfigManager().configRoot().flags_configuration.effects_duration * 20, Integer.parseInt(amplifier));
                        p.removePotionEffect(fulleffect.getType());
                        List<String> removeTasks = new ArrayList<>();
                        for (String taskId : PlayertaskID.keySet()) {
                            int id = Integer.parseInt(taskId.split("_")[0]);
                            String ideff = id + "_" + eff + er.getName();
                            if (PlayertaskID.containsKey(ideff) && PlayertaskID.get(ideff).equals(p.getName())) {
                                Bukkit.getScheduler().cancelTask(id);
                                removeTasks.add(taskId);
                                RedProtect.get().logger.debug(LogLevel.PLAYER, "(RegionFlags-eff)Removed task ID: " + taskId + " for player " + p.getName());
                            }
                        }
                        for (String key : removeTasks) {
                            PlayertaskID.remove(key);
                        }
                        removeTasks.clear();
                    }
                }
            } else
                //exit fly flag
                if (er.flagExists("forcefly") && !RedProtect.get().getPermissionHandler().hasPermOrBypass(p, "redprotect.flag.admin.forcefly") && (p.getGameMode().equals(GameMode.SURVIVAL) || p.getGameMode().equals(GameMode.ADVENTURE))) {
                    if (PlayertaskID.containsValue(p.getName())) {
                        if (r.flagExists("forcefly")) {
                            p.setAllowFlight(r.getFlagBool("forcefly"));
                            p.setFlying(r.getFlagBool("forcefly"));
                        } else {
                            p.setAllowFlight(false);
                            p.setFlying(false);
                        }
                        List<String> removeTasks = new ArrayList<>();
                        for (String taskId : PlayertaskID.keySet()) {
                            int id = Integer.parseInt(taskId.split("_")[0]);
                            String ideff = id + "_" + "forcefly" + er.getName();
                            if (PlayertaskID.containsKey(ideff) && PlayertaskID.get(ideff).equals(p.getName())) {
                                Bukkit.getScheduler().cancelTask(id);
                                removeTasks.add(taskId);
                                RedProtect.get().logger.debug(LogLevel.PLAYER, "(RegionFlags fly)Removed task ID: " + taskId + " for player " + p.getName());
                            }
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
            if (er.flagExists("player-exit-command") && !RedProtect.get().getPermissionHandler().hasPermOrBypass(p, "redprotect.flag.admin.player-exit-command")) {
                String[] cmds = er.getFlagString("player-exit-command").split(",");
                for (String cmd : cmds) {
                    if (cmd.startsWith("/")) {
                        cmd = cmd.substring(1);
                    }
                    p.getServer().dispatchCommand(p.getPlayer(), cmd.replace("{player}", p.getName()).replace("{region}", er.getName()));
                }
            }

            //Exit command as console
            if (er.flagExists("server-exit-command") && !RedProtect.get().getPermissionHandler().hasPermOrBypass(p, "redprotect.flag.admin.server-exit-command")) {
                String[] cmds = er.getFlagString("server-exit-command").split(",");
                for (String cmd : cmds) {
                    if (cmd.startsWith("/")) {
                        cmd = cmd.substring(1);
                    }
                    RedProtect.get().getServer().dispatchCommand(RedProtect.get().getServer().getConsoleSender(), cmd.replace("{player}", p.getName()).replace("{region}", er.getName()));
                }
            }

            //Pvp check to exit region
            if (er.flagExists("forcepvp") && RedProtect.get().hooks.checkPvPm() && !RedProtect.get().getPermissionHandler().hasPermOrBypass(p, "redprotect.flag.admin.forcepvp")) {
                if (PvPState.containsKey(p.getName()) && !p.hasPermission("redprotect.forcepvp.bypass")) {
                    if (PvPState.get(p.getName()) != PvPlayer.get(p).hasPvPEnabled()) {
                        PvPlayer.get(p).setPvP(PvPState.get(p.getName()));
                    }
                    PvPState.remove(p.getName());
                }
            }
        }

        //2nd checks
        if (r.canEnter(p) || RedProtect.get().getPermissionHandler().hasPermOrBypass(p, "redprotect.flag.admin.enter")) {

            //Enter check forcepvp flag
            if (r.flagExists("forcepvp") && RedProtect.get().hooks.checkPvPm() && !RedProtect.get().getPermissionHandler().hasPermOrBypass(p, "redprotect.flag.admin.forcepvp")) {
                PvPlayer pvpp = PvPlayer.get(p);
                if (r.isForcePVP() != pvpp.hasPvPEnabled()) {
                    PvPState.put(p.getName(), pvpp.hasPvPEnabled());
                    pvpp.setPvP(r.isForcePVP());
                }
            }

            //Enter effect
            if (r.flagExists("effects") && !RedProtect.get().getPermissionHandler().hasPermOrBypass(p, "redprotect.flag.admin.effects")) {
                String[] effects = r.getFlagString("effects").split(",");
                for (String effect : effects) {
                    String eff = effect.split(" ")[0];
                    String amplifier = effect.split(" ")[1];
                    final PotionEffect fulleffect = new PotionEffect(PotionEffectType.getByName(eff), RedProtect.get().getConfigManager().configRoot().flags_configuration.effects_duration * 20, Integer.parseInt(amplifier));
                    int TaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(RedProtect.get(), new Runnable() {
                        public void run() {
                            if (p.isOnline() && r.flagExists("effects")) {
                                p.addPotionEffect(fulleffect, true);
                            } else {
                                p.setAllowFlight(false);
                                try {
                                    this.finalize();
                                } catch (Throwable e) {
                                    RedProtect.get().logger.debug(LogLevel.PLAYER, "Effects not finalized...");
                                }
                            }
                        }
                    }, 0, 20);
                    PlayertaskID.put(TaskId + "_" + eff + r.getName(), p.getName());
                    RedProtect.get().logger.debug(LogLevel.PLAYER, "Added task ID: " + TaskId + "_" + eff + " for player " + p.getName());
                }
            }

            //Enter fly flag
            if (r.flagExists("forcefly") && !RedProtect.get().getPermissionHandler().hasPermOrBypass(p, "redprotect.flag.admin.forcefly") && (p.getGameMode().equals(GameMode.SURVIVAL) || p.getGameMode().equals(GameMode.ADVENTURE))) {
                p.setAllowFlight(r.getFlagBool("forcefly"));
                p.setFlying(r.getFlagBool("forcefly"));
                int TaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(RedProtect.get(), new Runnable() {
                    public void run() {
                        if (p.isOnline() && r.flagExists("forcefly")) {
                            p.setAllowFlight(r.getFlagBool("forcefly"));
                            p.setFlying(r.getFlagBool("forcefly"));
                        } else {
                            p.setAllowFlight(false);
                            p.setFlying(false);
                            try {
                                this.finalize();
                            } catch (Throwable e) {
                                RedProtect.get().logger.debug(LogLevel.PLAYER, "forcefly not finalized...");
                            }
                        }
                    }
                }, 0, 80);
                PlayertaskID.put(TaskId + "_" + "forcefly" + r.getName(), p.getName());
                RedProtect.get().logger.debug(LogLevel.PLAYER, "(RegionFlags fly)Added task ID: " + TaskId + "_" + "forcefly" + " for player " + p.getName());
            }
        }
    }

    private void noRegionFlags(Region er, Player p) {

        if (er != null && (er.canExit(p) || RedProtect.get().getPermissionHandler().hasPermOrBypass(p, "redprotect.flag.admin.exit"))) {

            //Pvp check to exit region
            if (er.flagExists("forcepvp") && RedProtect.get().hooks.checkPvPm() && !RedProtect.get().getPermissionHandler().hasPermOrBypass(p, "redprotect.flag.admin.forcepvp")) {
                if (PvPState.containsKey(p.getName()) && !p.hasPermission("redprotect.forcepvp.bypass")) {
                    if (PvPState.get(p.getName()) != PvPlayer.get(p).hasPvPEnabled()) {
                        PvPlayer.get(p).setPvP(PvPState.get(p.getName()));
                    }
                    PvPState.remove(p.getName());
                }
            }

            //Exit gamemode
            if (er.flagExists("gamemode") && !RedProtect.get().getPermissionHandler().hasPermOrBypass(p, "redprotect.flag.admin.gamemode")) {
                p.setGameMode(Bukkit.getServer().getDefaultGameMode());
            }

            //Exit effect
            if (er.flagExists("effects") && !RedProtect.get().getPermissionHandler().hasPermOrBypass(p, "redprotect.flag.admin.effects")) {
                String[] effects = er.getFlagString("effects").split(",");
                for (String effect : effects) {
                    if (PlayertaskID.containsValue(p.getName())) {
                        String eff = effect.split(" ")[0];
                        for (PotionEffect pot : p.getActivePotionEffects()) {
                            p.removePotionEffect(pot.getType());
                        }
                        List<String> removeTasks = new ArrayList<>();
                        for (String taskId : PlayertaskID.keySet()) {
                            int id = Integer.parseInt(taskId.split("_")[0]);
                            String ideff = id + "_" + eff + er.getName();
                            if (PlayertaskID.containsKey(ideff) && PlayertaskID.get(ideff).equals(p.getName())) {
                                Bukkit.getScheduler().cancelTask(id);
                                removeTasks.add(taskId);
                                RedProtect.get().logger.debug(LogLevel.PLAYER, "(noRegionFlags eff)Removed task ID: " + taskId + " for effect " + effect);
                            }
                        }
                        for (String key : removeTasks) {
                            PlayertaskID.remove(key);
                        }
                        removeTasks.clear();
                    }
                }
            } else

                //exit fly flag
                if (er.flagExists("forcefly") && !RedProtect.get().getPermissionHandler().hasPermOrBypass(p, "redprotect.flag.admin.forcefly") && (p.getGameMode().equals(GameMode.SURVIVAL) || p.getGameMode().equals(GameMode.ADVENTURE))) {
                    if (PlayertaskID.containsValue(p.getName())) {
                        p.setAllowFlight(false);
                        p.setFlying(false);
                        List<String> removeTasks = new ArrayList<>();
                        for (String taskId : PlayertaskID.keySet()) {
                            int id = Integer.parseInt(taskId.split("_")[0]);
                            String ideff = id + "_" + "forcefly" + er.getName();
                            if (PlayertaskID.containsKey(ideff) && PlayertaskID.get(ideff).equals(p.getName())) {
                                Bukkit.getScheduler().cancelTask(id);
                                removeTasks.add(taskId);
                                RedProtect.get().logger.debug(LogLevel.PLAYER, "(noRegionFlags fly)Removed task ID: " + taskId + " for player " + p.getName());
                            }
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
            if (er.flagExists("player-exit-command") && !RedProtect.get().getPermissionHandler().hasPermOrBypass(p, "redprotect.flag.admin.player-exit-command")) {
                String[] cmds = er.getFlagString("player-exit-command").split(",");
                for (String cmd : cmds) {
                    if (cmd.startsWith("/")) {
                        cmd = cmd.substring(1);
                    }
                    RedProtect.get().getServer().dispatchCommand(p, cmd.replace("{player}", p.getName()));
                }
            }

            //Exit command as console
            if (er.flagExists("server-exit-command") && !RedProtect.get().getPermissionHandler().hasPermOrBypass(p, "redprotect.flag.admin.server-exit-command")) {
                String[] cmds = er.getFlagString("server-exit-command").split(",");
                for (String cmd : cmds) {
                    if (cmd.startsWith("/")) {
                        cmd = cmd.substring(1);
                    }
                    RedProtect.get().getServer().dispatchCommand(RedProtect.get().getServer().getConsoleSender(), cmd.replace("{player}", p.getName()));
                }
            }

            // Set back to Collide
            p.setCollidable(true);
        }
    }

    @EventHandler
    public void onHangingDamaged(HangingBreakByEntityEvent e) {
        if (e.isCancelled()) {
            return;
        }
        RedProtect.get().logger.debug(LogLevel.PLAYER, "Is PlayerListener - HangingBreakByEntityEvent event");
        Entity ent = e.getRemover();
        Location loc = e.getEntity().getLocation();
        Region r = RedProtect.get().getRegionManager().getTopRegion(loc);

        if (ent instanceof Player player) {
            if (r != null && !r.canBuild(player) && !r.canBreak(e.getEntity().getType())) {
                RedProtect.get().getLanguageManager().sendMessage(player, "blocklistener.region.cantbuild");
                e.setCancelled(true);
            }
        }
        if (ent instanceof Monster) {
            if (r != null && !r.canMobLoot()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBucketUse(PlayerBucketEmptyEvent e) {
        if (e.isCancelled()) {
            return;
        }
        Player p = e.getPlayer();
        Location l = e.getBlockClicked().getLocation();
        Region r = RedProtect.get().getRegionManager().getTopRegion(l);

        if (r != null && !r.canBuild(p) && (p.getItemInHand().getType().name().contains("BUCKET"))) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent e) {
        if (e.isCancelled()) {
            return;
        }
        Player p = e.getPlayer();
        Location l = e.getBlockClicked().getLocation();
        Region r = RedProtect.get().getRegionManager().getTopRegion(l);

        if (r != null && !r.canBuild(p) && (p.getItemInHand().getType().name().contains("BUCKET"))) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent e) {
        if (!(e.getEntity() instanceof Player p)) {
            return;
        }

        Region r = RedProtect.get().getRegionManager().getTopRegion(p.getLocation());
        if (r != null && !r.canHunger()) {
            e.setCancelled(true);
        }
    }

}
