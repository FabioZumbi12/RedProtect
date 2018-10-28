package br.net.fabiozumbi12.RedProtect.Bukkit.listeners;

import br.net.fabiozumbi12.RedProtect.Bukkit.*;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPConfig;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPLang;
import br.net.fabiozumbi12.RedProtect.Bukkit.events.EnterExitRegionEvent;
import br.net.fabiozumbi12.RedProtect.Bukkit.hooks.WEListener;
import com.earth2me.essentials.User;
import de.Keyle.MyPet.MyPetApi;
import de.Keyle.MyPet.api.entity.MyPet.PetState;
import de.Keyle.MyPet.api.entity.MyPetBukkitEntity;
import de.Keyle.MyPet.api.player.MyPetPlayer;
import me.NoChance.PvPManager.PvPlayer;
import net.digiex.magiccarpet.MagicCarpet;
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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockIterator;
import org.inventivetalent.bossbar.BossBarAPI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("deprecation")
public class RPPlayerListener implements Listener {

    static final RPContainer cont = new RPContainer();
    private final HashMap<String, String> Ownerslist = new HashMap<>();
    private final HashMap<String, String> PlayerCmd = new HashMap<>();
    private final HashMap<String, Boolean> PvPState = new HashMap<>();
    private final HashMap<String, String> PlayertaskID = new HashMap<>();
    private final HashMap<String, HashMap<Integer, Location>> deathLocs = new HashMap<>();
    private final HashMap<String, Integer> dmgp = new HashMap<>();
    private final HashMap<String, Integer> trys = new HashMap<>();
    private final HashMap<String, Integer> matchRate = new HashMap<>();

    public RPPlayerListener() {
        RedProtect.get().logger.debug("Loaded RPPlayerListener...");
    }

    @EventHandler
    public void onBrewing(BrewEvent e) {
        ItemStack[] cont = e.getContents().getContents();
        for (int i = 0; i < cont.length; i++) {
            if (RPUtil.denyPotion(cont[i])) {
                e.getContents().setItem(i, new ItemStack(Material.AIR));
            }
        }
    }

    @EventHandler
    public void onCraftItem(PrepareItemCraftEvent e) {
        if (e.getView().getPlayer() instanceof Player) {
            Player p = (Player) e.getView().getPlayer();

            ItemStack result = e.getInventory().getResult();

            if (RPUtil.denyPotion(result, p)) {
                e.getInventory().setResult(new ItemStack(Material.AIR));
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerFrostWalk(EntityBlockFormEvent e) {
        if (!(e.getEntity() instanceof Player)) {
            return;
        }
        RedProtect.get().logger.debug("RPPlayerListener - EntityBlockFormEvent canceled? " + e.isCancelled());
        Player p = (Player) e.getEntity();
        Region r = RedProtect.get().rm.getTopRegion(e.getBlock().getLocation());
        if (r != null && e.getNewState().getType().name().contains("FROSTED_ICE") && !r.canIceForm(p)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent e) {
        if (e.getItem() == null) {
            return;
        }

        Player p = e.getPlayer();
        //deny potion
        if (p == null) {
            return;
        }

        if (RPUtil.denyPotion(e.getItem(), p)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerInteract(PlayerInteractEvent event) {
        RedProtect.get().logger.debug("RPPlayerListener - PlayerInteractEvent canceled? " + event.isCancelled());

        final Player p = event.getPlayer();
        Block b = event.getClickedBlock();
        ItemStack itemInHand = event.getItem();

        Location l;

        if (RedProtect.get().tpWait.contains(p.getName())) {
            RedProtect.get().tpWait.remove(p.getName());
            RPLang.sendMessage(p, "cmdmanager.region.tpcancelled");
        }

        if (b != null) {
            l = b.getLocation();
            RedProtect.get().logger.debug("RPPlayerListener - Is PlayerInteractEvent event. The block is " + b.getType().name());
        } else {
            l = p.getLocation();
        }

        if (itemInHand != null && !itemInHand.getType().equals(Material.AIR)) {
            String claimmode = RPConfig.getWorldClaimType(p.getWorld().getName());
            if (itemInHand.getType().name().equalsIgnoreCase(RPConfig.getString("wands.adminWandID")) && ((claimmode.equalsIgnoreCase("WAND") || claimmode.equalsIgnoreCase("BOTH")) || p.hasPermission("redprotect.admin.claim"))) {

                if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
                    if (!RPUtil.canBuildNear(p, l)) {
                        event.setCancelled(true);
                        return;
                    }
                    RedProtect.get().secondLocationSelections.put(p, l);
                    p.sendMessage(RPLang.get("playerlistener.wand2") + RPLang.get("general.color") + " (" + ChatColor.GOLD + l.getBlockX() + RPLang.get("general.color") + ", " + ChatColor.GOLD + l.getBlockY() + RPLang.get("general.color") + ", " + ChatColor.GOLD + l.getBlockZ() + RPLang.get("general.color") + ").");
                    event.setCancelled(true);
                } else if (event.getAction().equals(Action.LEFT_CLICK_BLOCK) || event.getAction().equals(Action.LEFT_CLICK_AIR)) {
                    if (!RPUtil.canBuildNear(p, l)) {
                        event.setCancelled(true);
                        return;
                    }
                    RedProtect.get().firstLocationSelections.put(p, l);
                    p.sendMessage(RPLang.get("playerlistener.wand1") + RPLang.get("general.color") + " (" + ChatColor.GOLD + l.getBlockX() + RPLang.get("general.color") + ", " + ChatColor.GOLD + l.getBlockY() + RPLang.get("general.color") + ", " + ChatColor.GOLD + l.getBlockZ() + RPLang.get("general.color") + ").");
                    event.setCancelled(true);
                }

                //show preview border
                if (RedProtect.get().firstLocationSelections.containsKey(p) && RedProtect.get().secondLocationSelections.containsKey(p)) {
                    Location loc1 = RedProtect.get().firstLocationSelections.get(p);
                    Location loc2 = RedProtect.get().secondLocationSelections.get(p);
                    if (RedProtect.get().WE && RPConfig.getBool("hooks.useWECUI")) {
                        WEListener.setSelectionRP(p, loc1, loc2);
                    }

                    if (loc1.getWorld().equals(loc2.getWorld()) && loc1.distanceSquared(loc2) > RPConfig.getInt("region-settings.define-max-distance") && !RedProtect.get().ph.hasPerm(p, "redprotect.bypass.define-max-distance")) {
                        Double dist = loc1.distanceSquared(loc2);
                        RPLang.sendMessage(p, String.format(RPLang.get("regionbuilder.selection.maxdefine"), RPConfig.getInt("region-settings.define-max-distance"), dist.intValue()));
                    } else {
                        RPUtil.addBorder(p, RPUtil.get4Points(loc1, loc2, p.getLocation().getBlockY()));
                    }
                }
                return;
            }
            if (itemInHand.getType().name().equalsIgnoreCase(RPConfig.getString("wands.infoWandID"))) {
                Region r = RedProtect.get().rm.getTopRegion(l);
                if (p.hasPermission("redprotect.infowand")) {
                    if (r == null) {
                        RPLang.sendMessage(p, "playerlistener.noregion.atblock");
                    } else if (r.canBuild(p)) {
                        p.sendMessage(RPLang.get("general.color") + "--------------- [" + ChatColor.GOLD + r.getName() + RPLang.get("general.color") + "] ---------------");
                        p.sendMessage(r.info());
                        p.sendMessage(RPLang.get("general.color") + "-----------------------------------------");
                    } else {
                        p.sendMessage(RPLang.get("playerlistener.region.entered").replace("{region}", r.getName()).replace("{leaders}", r.getLeadersDesc()));
                    }
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if (event.isCancelled()) {
            return;
        }

        Region r = RedProtect.get().rm.getTopRegion(l);
        //start player checks
        if (r == null) {
            if (b != null && (b.getType().equals(Material.ANVIL) || b.getState() instanceof InventoryHolder ||
                    RPConfig.getStringList("private.allowed-blocks").stream().anyMatch(b.getType().name()::matches))) {
                Boolean out = RPConfig.getBool("private.allow-outside");
                if (out && !cont.canOpen(b, p)) {
                    RPLang.sendMessage(p, "playerlistener.region.cantopen");
                    event.setCancelled(true);
                }
            }

        } else { //if r != null >>
            //other blocks and interactions
            if (itemInHand != null && (event.getAction().name().equals("RIGHT_CLICK_BLOCK") || b == null)) {
                Material hand = itemInHand.getType();
                if (hand.equals(Material.ENDER_PEARL) && !r.canTeleport(p)) {
                    RPLang.sendMessage(p, "playerlistener.region.cantuse");
                    event.setCancelled(true);
                    event.setUseItemInHand(Event.Result.DENY);
                    return;
                } else if ((hand.equals(Material.BOW) || (hand.name().contains("SNOW") && hand.name().contains("BALL")) || hand.name().contains("FIREWORK") || hand.equals(Material.EGG)) && !r.canProtectiles(p)) {
                    RPLang.sendMessage(p, "playerlistener.region.cantuse");
                    event.setCancelled(true);
                    event.setUseItemInHand(Event.Result.DENY);
                    return;
                } else if (hand.equals(Material.POTION) && !r.usePotions(p)) {
                    RPLang.sendMessage(p, "playerlistener.region.cantuse");
                    event.setCancelled(true);
                    event.setUseItemInHand(Event.Result.DENY);
                    return;
                } else if (hand.name().contains("_EGG") && !r.canInteractPassives(p)) {
                    RPLang.sendMessage(p, "playerlistener.region.cantuse");
                    event.setCancelled(true);
                    event.setUseItemInHand(Event.Result.DENY);
                    return;
                }
            }

            //if (r != null) && (b != null) >>
            if (b != null) {
                if (b.getType().equals(Material.DRAGON_EGG) ||
                        b.getType().name().equalsIgnoreCase("BED") ||
                        b.getType().name().contains("NOTE_BLOCK") ||
                        b.getType().name().contains("CAKE")) {

                    if (!r.canBuild(p)) {
                        RPLang.sendMessage(p, "playerlistener.region.cantinteract");
                        event.setCancelled(true);
                    }
                } else if (b.getState() instanceof Sign && RPConfig.getBool("region-settings.enable-flag-sign")) {
                    Sign s = (Sign) b.getState();
                    String[] lines = s.getLines();
                    if (lines[0].equalsIgnoreCase("[flag]") && r.getFlags().containsKey(lines[1])) {
                        String flag = lines[1];
                        if (!(r.getFlags().get(flag) instanceof Boolean)) {
                            RPLang.sendMessage(p, RPLang.get("playerlistener.region.sign.cantflag"));
                            return;
                        }
                        if (RedProtect.get().ph.hasPerm(p, "redprotect.flag." + flag)) {
                            if (r.isAdmin(p) || r.isLeader(p) || RedProtect.get().ph.hasPerm(p, "redprotect.admin.flag." + flag)) {
                                if (RPConfig.getBool("flags-configuration.change-flag-delay.enable")) {
                                    if (RPConfig.getStringList("flags-configuration.change-flag-delay.flags").contains(flag)) {
                                        if (!RedProtect.get().changeWait.contains(r.getName() + flag)) {
                                            RPUtil.startFlagChanger(r.getName(), flag, p);
                                            changeFlag(r, flag, p, s);
                                            return;
                                        } else {
                                            RPLang.sendMessage(p, RPLang.get("gui.needwait.tochange").replace("{seconds}", RPConfig.getString("flags-configuration.change-flag-delay.seconds")));
                                            return;
                                        }
                                    }
                                }
                                changeFlag(r, flag, p, s);
                                return;
                            }
                        }
                        RPLang.sendMessage(p, "cmdmanager.region.flag.nopermregion");
                    }
                } else if (b.getType().equals(Material.ENDER_CHEST)) {
                    if (!r.canEnderChest(p)) {
                        RPLang.sendMessage(p, "playerlistener.region.cantopen");
                        event.setCancelled(true);
                    }
                } else if (!b.getType().equals(Material.ENDER_CHEST) && (b.getType().equals(Material.ANVIL) || b.getState() instanceof InventoryHolder ||
                        RPConfig.getStringList("private.allowed-blocks").stream().anyMatch(b.getType().name()::matches))) {

                    if ((r.canChest(p) && !cont.canOpen(b, p) || (!r.canChest(p) && cont.canOpen(b, p)) || (!r.canChest(p) && !cont.canOpen(b, p)))) {
                        RPLang.sendMessage(p, "playerlistener.region.cantopen");
                        event.setCancelled(true);
                    }
                } else if (b.getType().name().contains("LEVER") || (b.getType().name().contains("REDSTONE") && !b.getType().equals(Material.REDSTONE_ORE))) {
                    if (!r.canLever(p)) {
                        RPLang.sendMessage(p, "playerlistener.region.cantlever");
                        event.setCancelled(true);
                    }
                } else if (b.getType().name().contains("BUTTON")) {
                    if (!r.canButton(p)) {
                        RPLang.sendMessage(p, "playerlistener.region.cantbutton");
                        event.setCancelled(true);
                    }
                } else if (RPDoor.isOpenable(b) && event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                    if (!r.canDoor(p)/* || (r.canDoor(p) && !cont.canOpen(b, p))*/) {
                        RPLang.sendMessage(p, "playerlistener.region.cantdoor");
                        event.setCancelled(true);
                    } else {
                        RPDoor.ChangeDoor(b, r);
                    }
                } else if (itemInHand != null && (itemInHand.getType().name().startsWith("BOAT") || itemInHand.getType().name().contains("MINECART"))) {
                    if (!r.canMinecart(p)) {
                        RPLang.sendMessage(p, "blocklistener.region.cantplace");
                        event.setUseItemInHand(Event.Result.DENY);
                        event.setCancelled(true);
                    }
                } else if ((event.getAction().equals(Action.LEFT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) &&
                        b.getType().name().contains("SIGN") && !r.canSign(p)) {
                    Sign sign = (Sign) b.getState();
                    for (String tag : RPConfig.getStringList("region-settings.allow-sign-interact-tags")) {
                        //check first rule
                        if (sign != null && tag.equalsIgnoreCase(sign.getLine(0))) {
                            return;
                        }

                        //check if tag is owners or members names
                        if (tag.equalsIgnoreCase("{membername}")) {
                            for (String leader : r.getLeaders()) {
                                if (sign.getLine(0).equalsIgnoreCase(RPUtil.UUIDtoPlayer(leader))) {
                                    return;
                                }
                            }
                            for (String admin : r.getAdmins()) {
                                if (sign.getLine(0).equalsIgnoreCase(RPUtil.UUIDtoPlayer(admin))) {
                                    return;
                                }
                            }
                            for (String member : r.getMembers()) {
                                if (sign.getLine(0).equalsIgnoreCase(RPUtil.UUIDtoPlayer(member))) {
                                    return;
                                }
                            }
                        }

                        //check if tag is player name
                        if (tag.equalsIgnoreCase("{playername}")) {
                            if (sign.getLine(0).equalsIgnoreCase(RPUtil.UUIDtoPlayer(p.getName()))) {
                                return;
                            }
                        }
                    }
                    RPLang.sendMessage(p, "playerlistener.region.cantinteract.signs");
                    event.setUseItemInHand(Event.Result.DENY);
                    event.setCancelled(true);
                } else if ((itemInHand != null && !itemInHand.getType().equals(Material.AIR)) && !r.canBuild(p) && !r.canPlace(itemInHand.getType()) &&
                        (itemInHand.getType().equals(Material.FLINT_AND_STEEL) ||
                                itemInHand.getType().equals(Material.WATER_BUCKET) ||
                                itemInHand.getType().equals(Material.BUCKET) ||
                                itemInHand.getType().equals(Material.LAVA_BUCKET) ||
                                itemInHand.getType().equals(Material.ITEM_FRAME) ||
                                itemInHand.getType().equals(Material.PAINTING))) {
                    RPLang.sendMessage(p, "playerlistener.region.cantuse");
                    event.setCancelled(true);
                    event.setUseItemInHand(Event.Result.DENY);
                    event.setUseInteractedBlock(Event.Result.DENY);
                } else if (!r.allowMod(p) && !RPUtil.isBukkitBlock(b)) {
                    RPLang.sendMessage(p, "playerlistener.region.cantinteract");
                    event.setCancelled(true);
                    event.setUseInteractedBlock(Event.Result.DENY);
                }
            }
        }
    }

    private void changeFlag(Region r, String flag, Player p, Sign s) {
        if (r.setFlag(p, flag, !r.getFlagBool(flag))) {
            RPLang.sendMessage(p, RPLang.get("cmdmanager.region.flag.set").replace("{flag}", "'" + flag + "'") + " " + r.getFlagBool(flag));
            RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + p.getName() + " SET FLAG " + flag + " of region " + r.getName() + " to " + RPLang.translBool(r.getFlagString(flag)));
            s.setLine(3, RPLang.get("region.value") + " " + RPLang.translBool(r.getFlagString(flag)));
            s.update();
            if (!RPConfig.getSigns(r.getID()).contains(s.getLocation())) {
                RPConfig.putSign(r.getID(), s.getLocation());
            }
        }
    }

    @EventHandler
    public void MoveItem(InventoryClickEvent e) {
        if (e.isCancelled()) {
            return;
        }

        Region r = RedProtect.get().rm.getTopRegion(e.getWhoClicked().getLocation());
        if (r != null && e.getInventory().getTitle() != null) {
            if (e.getInventory().getTitle().equals(RPUtil.getTitleName(r)) || e.getInventory().getTitle().equals(RPLang.get("gui.editflag"))) {
                return;
            }
        }
    	/*
    	if (RPUtil.RemoveGuiItem(e.getCurrentItem())){
    		e.setCurrentItem(new ItemStack(Material.AIR));
    	}*/
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        Entity e = event.getRightClicked();
        Player p = event.getPlayer();
        if (e == null) {
            return;
        }

        RedProtect.get().logger.debug("RPPlayerListener - Is PlayerInteractEntityEvent event: " + e.getType().name());
        Location l = e.getLocation();

        if (e instanceof ItemFrame || e instanceof Painting) {
            Region r = RedProtect.get().rm.getTopRegion(l);
            if (r != null && !r.canBuild(p)) {
                RPLang.sendMessage(p, "playerlistener.region.cantedit");
                event.setCancelled(true);
            }
        } else if ((e instanceof Minecart || e instanceof Boat)) {
            Region r = RedProtect.get().rm.getTopRegion(l);
            if (r != null && !r.canMinecart(p)) {
                RPLang.sendMessage(p, "blocklistener.region.cantenter");
                event.setCancelled(true);
            }
        } else if (RedProtect.get().MyPet && e instanceof MyPetBukkitEntity) {
            if (((MyPetBukkitEntity) e).getOwner().getPlayer().equals(p)) {
            }
        } else if (!RPUtil.isBukkitEntity(e) && (!(event.getRightClicked() instanceof Player))) {
            Region r = RedProtect.get().rm.getTopRegion(l);
            if (r != null && !r.allowMod(p)) {
                RedProtect.get().logger.debug("PlayerInteractEntityEvent - Block is " + event.getRightClicked().getType().name());
                RPLang.sendMessage(p, "playerlistener.region.cantinteract");
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) {
            return;
        }

        Player play = (Player) e.getEntity();

        if (RedProtect.get().tpWait.contains(play.getName())) {
            RedProtect.get().tpWait.remove(play.getName());
            RPLang.sendMessage((Player) e.getEntity(), RPLang.get("cmdmanager.region.tpcancelled"));
        }

        //deny damagecauses
        List<String> Causes = RPConfig.getStringList("server-protection.deny-playerdeath-by");
        if (Causes.size() > 0) {
            for (String cause : Causes) {
                cause = cause.toUpperCase();
                try {
                    if (e.getCause().equals(DamageCause.valueOf(cause))) {
                        e.setCancelled(true);
                    }
                } catch (IllegalArgumentException ex) {
                    RedProtect.get().logger.severe("The config 'deny-playerdeath-by' have an unknow damage cause type. Change to a valid damage cause type.");
                }
            }
        }

        Region r = RedProtect.get().rm.getTopRegion(play.getLocation());
        if (r != null) {
            if (!r.canPlayerDamage()) {
                e.setCancelled(true);
            }
            //execute on health
            if (r.cmdOnHealth(play)) {
                RedProtect.get().logger.debug("Cmd on healt: true");
            }

            if (!r.canDeath() && play.getHealth() <= 1) {
                e.setCancelled(true);
            }
        }

    }

    private void startCheckRate(final String pname) {
        int task = Bukkit.getScheduler().runTaskLater(RedProtect.get(), () -> {
            trys.remove(pname);
            matchRate.remove(pname);
            dmgp.remove(pname);
        }, RPConfig.getInt("server-protection.check-killaura-freekill.time-between-trys")).getTaskId();
        if (dmgp.containsKey(pname)) {
            Bukkit.getScheduler().cancelTask(dmgp.get(pname));
        }
        dmgp.put(pname, task);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent e) {
        Player p = null;

        RedProtect.get().logger.debug("RPLayerListener: Is EntityDamageByEntityEvent event");

        if (e.getDamager() instanceof Player) {
            p = (Player) e.getDamager();
        } else if (e.getDamager() instanceof Projectile) {
            Projectile proj = (Projectile) e.getDamager();
            if (proj.getShooter() instanceof Player) {
                p = (Player) proj.getShooter();
            }
        }

        if (p != null) {
            RedProtect.get().logger.debug("Player: " + p.getName());
        } else {
            RedProtect.get().logger.debug("Player: is null");
            return;
        }

        RedProtect.get().logger.debug("Damager: " + e.getDamager().getType().name());

        //check killaura or freekill
        if (RPConfig.getBool("server-protection.check-killaura-freekill.enable")) {
            startCheckRate(p.getName());
        }

        Location l = e.getEntity().getLocation();
        Region r = RedProtect.get().rm.getTopRegion(l);
        if (r == null) {
            return;
        }

        if (RedProtect.get().tpWait.contains(p.getName())) {
            RedProtect.get().tpWait.remove(p.getName());
            RPLang.sendMessage(p, "cmdmanager.region.tpcancelled");
        }

        if (e.getEntity() instanceof Player && !p.equals(e.getEntity()) && r.flagExists("pvp") && !r.canPVP((Player) e.getEntity(), p)) {
            RPLang.sendMessage(p, "entitylistener.region.cantpvp");
            e.setCancelled(true);
        }

        if ((e.getEntity() instanceof Hanging || e.getEntity() instanceof EnderCrystal) && !r.canBuild(p) && !r.canBreak(e.getEntityType())) {
            RPLang.sendMessage(p, "playerlistener.region.cantremove");
            e.setCancelled(true);
        }

        if ((e.getEntity() instanceof Boat || e.getEntity() instanceof Minecart) && !r.canMinecart(p)) {
            RPLang.sendMessage(p, "blocklistener.region.cantbreak");
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent e) {
        if (e.isCancelled()) {
            return;
        }

        final Player p = e.getPlayer();

        if (RedProtect.get().tpWait.contains(p.getName())) {
            RedProtect.get().tpWait.remove(p.getName());
            RPLang.sendMessage(p, "cmdmanager.region.tpcancelled");
        }

        Location lfrom = e.getFrom();
        Location lto = e.getTo();
        final Region rfrom = RedProtect.get().rm.getTopRegion(lfrom);
        final Region rto = RedProtect.get().rm.getTopRegion(lto);

        RedProtect.get().logger.debug("RPPlayerListener - PlayerTeleportEvent from " + lfrom.toString() + " to " + lto.toString());

        //Exit flag
        if (rfrom != null && !rfrom.canExit(p)) {
            e.setTo(RPUtil.DenyExitPlayer(p, lfrom, e.getTo(), rfrom));
            return;
        }

        if (rto != null) {

            //Allow teleport to with items
            if (!rto.canEnterWithItens(p)) {
                RPLang.sendMessage(p, RPLang.get("playerlistener.region.onlyenter.withitems").replace("{items}", rto.getFlags().get("allow-enter-items").toString()));
                e.setCancelled(true);
                return;
            }

            //Deny teleport to with item
            if (!rto.denyEnterWithItens(p)) {
                RPLang.sendMessage(p, RPLang.get("playerlistener.region.denyenter.withitems").replace("{items}", rto.getFlags().get("deny-enter-items").toString()));
                e.setCancelled(true);
                return;
            }

            if (RedProtect.get().PvPm) {
                if (rto.isPvPArena() && !PvPlayer.get(p).hasPvPEnabled() && !rto.canBuild(p)) {
                    RPLang.sendMessage(p, "playerlistener.region.pvpenabled");
                    e.setCancelled(true);
                    return;
                }
            }

            if (!rto.canEnter(p)) {
                RPLang.sendMessage(p, "playerlistener.region.cantregionenter");
                e.setCancelled(true);
                return;
            }

            //enter max players flag
            if (rto.maxPlayers() != -1) {
                if (!checkMaxPlayer(p, rto)) {
                    RPLang.sendMessage(p, RPLang.get("playerlistener.region.maxplayers").replace("{players}", String.valueOf(rto.maxPlayers())));
                    e.setCancelled(true);
                }
            }

            if (PlayerCmd.containsKey(p.getName())) {
                if (!rto.canBack(p) && PlayerCmd.get(p.getName()).startsWith("/back")) {
                    RPLang.sendMessage(p, "playerlistener.region.cantback");
                    e.setCancelled(true);
                }
                if (!rto.AllowHome(p) && PlayerCmd.get(p.getName()).startsWith("/home")) {
                    RPLang.sendMessage(p, "playerlistener.region.canthome");
                    e.setCancelled(true);
                }
                PlayerCmd.remove(p.getName());
            }
        }


        //teleport player to coord/world if playerup 128 y
        int NetherY = RPConfig.getInt("netherProtection.maxYsize");
        if (lto.getWorld().getEnvironment().equals(World.Environment.NETHER) && NetherY != -1 && lto.getBlockY() >= NetherY && !p.hasPermission("redprotect.bypass.nether-roof")) {
            RPLang.sendMessage(p, RPLang.get("playerlistener.upnethery").replace("{location}", NetherY + ""));
            e.setCancelled(true);
        }

        if (e.getCause().equals(PlayerTeleportEvent.TeleportCause.ENDER_PEARL) || e.getCause().name().contains("CHORUS_FRUIT")) {
            if (rfrom != null && !rfrom.canTeleport(p)) {
                RPLang.sendMessage(p, "playerlistener.region.cantteleportitem");
                e.setCancelled(true);
            }
            if (rto != null && !rto.canTeleport(p)) {
                RPLang.sendMessage(p, "playerlistener.region.cantteleportitem");
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

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();

        if (RedProtect.get().tpWait.contains(p.getName())) {
            RedProtect.get().tpWait.remove(p.getName());
            RPLang.sendMessage(p, "cmdmanager.region.tpcancelled");
        }

        String msg = e.getMessage();
        String cmd = msg.split(" ")[0];
        String cmds = cmd.toLowerCase().replace("/", "");
        //RedProtect.get().logger.severe("Command: "+msg);

        if (RPConfig.getStringList("server-protection.deny-commands-on-worlds." + p.getWorld().getName()).contains(msg.split(" ")[0].replace("/", "")) && !p.hasPermission("redprotect.bypass")) {
            RPLang.sendMessage(p, "playerlistener.command-notallowed");
            e.setCancelled(true);
            return;
        }

        if (RPConfig.hasGlobalKey(p.getWorld().getName() + ".command-ranges." + cmd.toLowerCase().replace("/", "")) && !cmds.equals(".")) {
            double min = RPConfig.getGlobalFlagDouble(p.getWorld().getName() + ".command-ranges." + cmds + ".min-range");
            double max = RPConfig.getGlobalFlagDouble(p.getWorld().getName() + ".command-ranges." + cmds + ".max-range");
            String mesg = RPConfig.getGlobalFlagString(p.getWorld().getName() + ".command-ranges." + cmds + ".message");
            double py = p.getLocation().getY();
            if (py < min || py > max) {
                if (mesg != null && !mesg.equals("")) {
                    RPLang.sendMessage(p, mesg);
                }
                e.setCancelled(true);
                return;
            }
        }

        if (cmd.equalsIgnoreCase("/back") || cmd.equalsIgnoreCase("/home")) {
            PlayerCmd.put(p.getName(), msg);
        }

        Region r = RedProtect.get().rm.getTopRegion(p.getLocation());
        if (r != null) {

            if ((cmd.equalsIgnoreCase("/petc") || cmd.equalsIgnoreCase("/petcall")) && RedProtect.get().MyPet && !r.canPet(p)) {
                RPLang.sendMessage(p, "playerlistener.region.cantpet");
                e.setCancelled(true);
                return;
            }

            if (!r.AllowCommands(p, msg)) {
                if (cmd.equalsIgnoreCase("/rp") || cmd.equalsIgnoreCase("/redprotect")) {
                    return;
                }
                RPLang.sendMessage(p, "playerlistener.region.cantcommand");
                e.setCancelled(true);
                return;
            }

            if (!r.DenyCommands(p, msg)) {
                for (String alias : RedProtect.get().getCommand("RedProtect").getAliases()) {
                    if (cmd.equalsIgnoreCase("/" + alias)) {
                        return;
                    }
                }

                RPLang.sendMessage(p, "playerlistener.region.cantcommand");
                e.setCancelled(true);
                return;
            }

            if (cmd.equalsIgnoreCase("/sethome") && !r.AllowHome(p)) {
                RPLang.sendMessage(p, "playerlistener.region.canthome");
                e.setCancelled(true);
                return;
            }

            //Pvp check
            if (cmd.equalsIgnoreCase("/pvp") && RedProtect.get().PvPm) {
                if (r.isPvPArena() && !PvPlayer.get(p).hasPvPEnabled() && !r.canBuild(p)) {
                    RPLang.sendMessage(p, "playerlistener.region.pvpenabled");
                    RedProtect.get().serv.dispatchCommand(RedProtect.get().serv.getConsoleSender(), RPConfig.getString("flags-configuration.pvparena-nopvp-kick-cmd").replace("{player}", p.getName()));
                    return;
                }
            }

            if (RedProtect.get().Mc && !r.getFlagBool("allow-magiccarpet") && (!r.isAdmin(p) && !r.isLeader(p))) {
                if (cmd.equalsIgnoreCase("/magiccarpet")) {
                    e.setCancelled(true);
                    RPLang.sendMessage(p, "playerlistener.region.cantmc");
                } else {
                    for (String cmda : MagicCarpet.getPlugin(MagicCarpet.class).getCommand("MagicCarpet").getAliases()) {
                        if (cmd.equalsIgnoreCase("/" + cmda)) {
                            e.setCancelled(true);
                            RPLang.sendMessage(p, "playerlistener.region.cantmc");
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDie(PlayerDeathEvent e) {
        Player p = e.getEntity();

        if (RedProtect.get().tpWait.contains(p.getName())) {
            RedProtect.get().tpWait.remove(p.getName());
            RPLang.sendMessage(p, "cmdmanager.region.tpcancelled");
        }

        Location loc = p.getLocation();
        Region r = RedProtect.get().rm.getTopRegion(loc);

        if (r != null) {
            if (r.keepInventory()) {
                e.setKeepInventory(true);
            }
            if (r.keepLevels()) {
                e.setKeepLevel(true);
            }
        }

        //check for death listener
        deathListener(p, 0);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();

        //check for death listener
        deathListener(p, 1);
    }

    private void deathListener(Player p, int index) {
        RedProtect.get().logger.debug("Added index " + index);

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

    private static Entity getTarget(final Player player) {
        try {
            BlockIterator iterator = new BlockIterator(player.getWorld(), player
                    .getLocation().toVector(), player.getEyeLocation()
                    .getDirection(), 0, 10);
            while (iterator.hasNext()) {
                Block item = iterator.next();
                for (Entity entity : player.getNearbyEntities(10, 10, 10)) {
                    int acc = 2;
                    for (int y = -acc; y < acc; y++) {
                        if (entity.getLocation().getBlock()
                                .getRelative(0, y, 0).equals(item)) {
                            return entity;
                        }
                    }
                }
            }
        } catch (IllegalStateException ignored) {
        }
        return null;
    }

    @EventHandler
    public void onPlayerMovement(PlayerMoveEvent e) {
        if (e.isCancelled() || RPConfig.getBool("performance.disable-onPlayerMoveEvent-handler")) {
            return;
        }

        Player p = e.getPlayer();

        //test player target
        if (RPConfig.getBool("server-protection.check-killaura-freekill.enable")) {
            Entity pent = getTarget(p);
            int trysp = 0;
            int mrate = 0;
            if (pent instanceof Player && dmgp.containsKey(pent.getName())) {
                if (trys.containsKey(p.getName())) {
                    trysp = trys.get(p.getName());
                }
                trysp++;
                trys.put(p.getName(), trysp);
                if (RPConfig.getBool("server-protection.check-killaura-freekill.debug-trys")) {
                    RedProtect.get().logger.warning("Player: " + p.getName() + " | Try count: " + trysp);
                }
                if (trysp % RPConfig.getInt("server-protection.check-killaura-freekill.check-rate") == 0) {
                    RedProtect.get().logger.warning("Player: " + p.getName() + " | Check Rate Match: (" + trysp + ")");
                    if (matchRate.containsKey(p.getName())) {
                        mrate = matchRate.get(p.getName());
                    }
                    mrate++;
                    matchRate.put(p.getName(), mrate);
                    if (mrate == RPConfig.getInt("server-protection.check-killaura-freekill.rate-multiples")) {
                        RedProtect.get().logger.severe("Player: " + p.getName() + " | MATCH RATE! Possible Hack or FreeKill: (" + mrate + ")");
                        matchRate.put(p.getName(), 0);
                    }
                }
            } else {
                trys.remove(p.getName());
                matchRate.remove(p.getName());
            }
        }


        Location lfrom = e.getFrom();
        Location lto = e.getTo();

        Region rfrom = RedProtect.get().rm.getTopRegion(lfrom);
        //Exit flag
        if (rfrom != null && !rfrom.canExit(p)) {
            e.setTo(RPUtil.DenyExitPlayer(p, lfrom, e.getTo(), rfrom));
            return;
        }

        //RedProtect.get().logger.debug("RPPlayerListener - PlayerMoveEvent from "+lfrom.toString()+" to "+lto.toString());
        if (lto.getWorld().equals(lfrom.getWorld()) && lto.distance(lfrom) > 0.1 && RedProtect.get().tpWait.contains(p.getName())) {
            RedProtect.get().tpWait.remove(p.getName());
            RPLang.sendMessage(p, "cmdmanager.region.tpcancelled");
        }

        //teleport player to coord/world if playerup 128 y
        int NetherY = RPConfig.getInt("netherProtection.maxYsize");
        if (lto.getWorld().getEnvironment().equals(World.Environment.NETHER) && NetherY != -1 && lto.getBlockY() >= NetherY && !p.hasPermission("redprotect.bypass.nether-roof")) {
            for (String cmd : RPConfig.getStringList("netherProtection.execute-cmd")) {
                RedProtect.get().serv.dispatchCommand(RedProtect.get().serv.getConsoleSender(), cmd.replace("{player}", p.getName()));
            }
            RPLang.sendMessage(p, RPLang.get("playerlistener.upnethery").replace("{location}", NetherY + ""));
        }

        Region r = RedProtect.get().rm.getTopRegion(lto);

        World w = lfrom.getWorld();

        if (r != null) {

            //Enter flag
            if (!r.canEnter(p)) {
                e.setTo(RPUtil.DenyEnterPlayer(w, lfrom, e.getTo(), r, false));
                RPLang.sendMessage(p, "playerlistener.region.cantregionenter");
            }

            //enter max players flag
            if (r.maxPlayers() != -1) {
                if (!checkMaxPlayer(p, r)) {
                    e.setTo(RPUtil.DenyEnterPlayer(w, lfrom, e.getTo(), r, false));
                    RPLang.sendMessage(p, RPLang.get("playerlistener.region.maxplayers").replace("{players}", String.valueOf(r.maxPlayers())));
                }
            }

            //remove pots
            if (!r.allowEffects(p)) {
                for (PotionEffect pot : p.getActivePotionEffects()) {
                    if (pot.getDuration() < 36000) {
                        p.removePotionEffect(pot.getType());
                    }
                }
            }

            //Mypet Flag
            if (RedProtect.get().MyPet && !r.canPet(p)) {
                if (MyPetApi.getPlayerManager().isMyPetPlayer(p)) {
                    MyPetPlayer mpp = MyPetApi.getPlayerManager().getMyPetPlayer(p);
                    if (mpp.hasMyPet() && mpp.getMyPet().getStatus() == PetState.Here) {
                        mpp.getMyPet().removePet();
                        RPLang.sendMessage(p, "playerlistener.region.cantpet");
                    }
                }
            }

            //Allow enter with items
            if (!r.canEnterWithItens(p)) {
                e.setTo(RPUtil.DenyEnterPlayer(w, lfrom, e.getTo(), r, false));
                RPLang.sendMessage(p, RPLang.get("playerlistener.region.onlyenter.withitems").replace("{items}", r.getFlags().get("allow-enter-items").toString()));
            }

            //Deny enter with item
            if (!r.denyEnterWithItens(p)) {
                e.setTo(RPUtil.DenyEnterPlayer(w, lfrom, e.getTo(), r, false));
                RPLang.sendMessage(p, RPLang.get("playerlistener.region.denyenter.withitems").replace("{items}", r.getFlags().get("deny-enter-items").toString()));
            }

            //update region admin or leander visit
            if (RPConfig.getString("region-settings.record-player-visit-method").equalsIgnoreCase("ON-REGION-ENTER")) {
                if (r.isLeader(p) || r.isAdmin(p)) {
                    if (r.getDate() == null || (!r.getDate().equals(RPUtil.DateNow()))) {
                        r.setDate(RPUtil.DateNow());
                    }
                }
            }

            //Deny Fly
            if (!p.getGameMode().toString().equalsIgnoreCase("SPECTATOR") && !r.canFly(p) && p.isFlying()) {
                p.setFlying(false);
                //p.setAllowFlight(false);
                RPLang.sendMessage(p, "playerlistener.region.cantfly");
            }

            if (!Ownerslist.containsKey(p.getName()) || !Ownerslist.get(p.getName()).equals(r.getID())) {
                Region er = RedProtect.get().rm.getRegionById(Ownerslist.get(p.getName()));
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
                Region er = RedProtect.get().rm.getRegionById(Ownerslist.get(p.getName()));
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
                    if (!er.getWelcome().equalsIgnoreCase("hide ") && RPConfig.getBool("notify.region-exit")) {
                        if (RedProtect.get().version >= 1110) {
                            SendNotifyMsg(p, RPLang.get("playerlistener.region.wilderness"), "RED");
                        } else {
                            SendNotifyMsg(p, RPLang.get("playerlistener.region.wilderness"), null);
                        }
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
            Region reg = RedProtect.get().rm.getTopRegion(onp.getLocation());
            if (reg != null && reg == r) {
                ttl++;
            }
        }
        return ttl < r.maxPlayers();
    }

    @EventHandler
    public void onPlayerEnterPortal(PlayerPortalEvent e) {
        Player p = e.getPlayer();

        Region rto = null;
        Region from = null;
        if (e.getTo() != null) {
            rto = RedProtect.get().rm.getTopRegion(e.getTo());
        }
        if (e.getFrom() != null) {
            from = RedProtect.get().rm.getTopRegion(e.getFrom());
        }


        if (rto != null && !rto.canExitPortal(p)) {
            RPLang.sendMessage(p, "playerlistener.region.cantteleport");
            e.setCancelled(true);
        }

        if (from != null && !from.canEnterPortal(p)) {
            RPLang.sendMessage(p, "playerlistener.region.cantenterteleport");
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPortalCreate(PortalCreateEvent e) {
        List<Block> blocks = e.getBlocks();
        for (Block b : blocks) {
            Region r = RedProtect.get().rm.getTopRegion(b.getLocation());
            if (r != null && !r.canCreatePortal()) {
                e.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler
    public void onPlayerLogout(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        stopTaskPlayer(p);
        if (RedProtect.get().tpWait.contains(p.getName())) {
            RedProtect.get().tpWait.remove(p.getName());
        }
        String worldneeded = RPConfig.getString("server-protection.teleport-player.on-leave.need-world-to-teleport");
        if (RPConfig.getBool("server-protection.teleport-player.on-leave.enable") &&
                (worldneeded.equals("none") || worldneeded.equals(p.getWorld().getName()))) {
            String[] loc = RPConfig.getString("server-protection.teleport-player.on-leave.location").split(",");
            p.teleport(new Location(Bukkit.getWorld(loc[0]), Double.parseDouble(loc[1]) + 0.500, Double.parseDouble(loc[2]), Double.parseDouble(loc[3]) + 0.500));
        }
    }

    @EventHandler
    public void PlayerLogin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        //Adjust inside region
        p.teleport(new Location(p.getWorld(), p.getLocation().getBlockX(), p.getLocation().getBlockY() + 0.1, p.getLocation().getBlockZ()));

        if (p.hasPermission("redprotect.update") && RedProtect.get().Update && !RPConfig.getBool("update-check.auto-update")) {
            RPLang.sendMessage(p, ChatColor.AQUA + "An update is available for RedProtect: " + RedProtect.get().UptVersion);
            RPLang.sendMessage(p, ChatColor.AQUA + "Use /rp update to download and automatically install this update.");
        }

        if (RPConfig.getString("region-settings.record-player-visit-method").equalsIgnoreCase("ON-LOGIN")) {
            String uuid = p.getUniqueId().toString();
            if (!RedProtect.get().OnlineMode) {
                uuid = p.getName().toLowerCase();
            }
            for (Region r : RedProtect.get().rm.getMemberRegions(uuid)) {
                if (r.getDate() == null || !r.getDate().equals(RPUtil.DateNow())) {
                    r.setDate(RPUtil.DateNow());
                }
            }
        }
        String worldneeded = RPConfig.getString("server-protection.teleport-player.on-join.need-world-to-teleport");
        if (RPConfig.getBool("server-protection.teleport-player.on-join.enable") &&
                (worldneeded.equals("none") || worldneeded.equals(p.getWorld().getName()))) {
            String[] loc = RPConfig.getString("server-protection.teleport-player.on-join.location").split(",");
            e.getPlayer().teleport(new Location(Bukkit.getWorld(loc[0]), Double.parseDouble(loc[1]) + 0.500, Double.parseDouble(loc[2]), Double.parseDouble(loc[3]) + 0.500));
        }

        Region r = RedProtect.get().rm.getTopRegion(p.getLocation());
        if (r != null) {
            RegionFlags(r, null, p, true);
        }
    }

    @EventHandler
    public void PlayerTrownEgg(PlayerEggThrowEvent e) {
        Location l = e.getEgg().getLocation();
        Player p = e.getPlayer();
        Region r = RedProtect.get().rm.getTopRegion(l);

        if (r != null && !r.canBuild(p)) {
            e.setHatching(false);
            RPLang.sendMessage(p, "playerlistener.region.canthatch");
        }
    }

    @EventHandler
    public void PlayerTrownArrow(ProjectileLaunchEvent e) {
        if (!(e.getEntity().getShooter() instanceof Player)) {
            return;
        }

        RedProtect.get().logger.debug("Is ProjectileLaunchEvent event.");

        Location l = e.getEntity().getLocation();
        Player p = (Player) e.getEntity().getShooter();
        Region r = RedProtect.get().rm.getTopRegion(l);

        if (r != null && !r.canProtectiles(p)) {
            e.setCancelled(true);
            RPLang.sendMessage(p, "playerlistener.region.cantuse");
        }
    }

    @EventHandler
    public void PlayerDropItem(PlayerDropItemEvent e) {
        RedProtect.get().logger.debug("Is PlayerDropItemEvent event.");

        Location l = e.getItemDrop().getLocation();
        Player p = e.getPlayer();
        Region r = RedProtect.get().rm.getTopRegion(l);

        if (r != null && ((!r.canExit(p) && !r.canDrop(p)) || !r.canDrop(p))) {
            e.setCancelled(true);
            RPLang.sendMessage(p, "playerlistener.region.cantdrop");
        }
    }

    @EventHandler
    public void PlayerPickup(PlayerPickupItemEvent e) {
        RedProtect.get().logger.debug("Is PlayerPickupItemEvent event.");

        Location l = e.getItem().getLocation();
        Player p = e.getPlayer();
        Region r = RedProtect.get().rm.getTopRegion(l);

        if (r != null && ((!r.canEnter(p) && !r.canPickup(p) || !r.canPickup(p)))) {
            e.setCancelled(true);
            RPLang.sendMessage(p, "playerlistener.region.cantpickup");
        }
    }

    @EventHandler
    public void PlayerTrownPotion(PotionSplashEvent e) {
        if (!(e.getPotion().getShooter() instanceof Player)) {
            return;
        }

        Player p = (Player) e.getPotion().getShooter();
        Entity ent = e.getEntity();

        RedProtect.get().logger.debug("Is PotionSplashEvent event.");

        Region r = RedProtect.get().rm.getTopRegion(ent.getLocation());
        if (r != null && !r.usePotions(p)) {
            RPLang.sendMessage(p, "playerlistener.region.cantuse");
            e.setCancelled(true);
            return;
        }

        //deny potion
        if (RPUtil.denyPotion(e.getPotion().getItem(), p)) {
            e.setCancelled(true);
        }
    }

    public void SendNotifyMsg(Player p, String notify, String color) {
        if (RPConfig.getString("notify.region-enter-mode").equalsIgnoreCase("OFF")) {
            return;
        }
        if (!notify.equals("")) {
            if (RPConfig.getString("notify.region-enter-mode").equalsIgnoreCase("BOSSBAR")) {
                if (RedProtect.get().version >= 1110) {
                    RPMine111.sendBarMsg(notify, color, p);
                } else {
                    if (RedProtect.get().BossBar) {
                        BossBarAPI.setMessage(p, notify);
                    } else {
                        p.sendMessage(notify);
                    }
                }
            }
            if (RPConfig.getString("notify.region-enter-mode").equalsIgnoreCase("CHAT")) {
                p.sendMessage(notify);
            }
        }
    }

    public void SendWelcomeMsg(final Player p, String wel) {
        if (RPConfig.getString("notify.welcome-mode").equalsIgnoreCase("OFF")) {
            return;
        }
        if (RPConfig.getString("notify.welcome-mode").equalsIgnoreCase("BOSSBAR")) {
            if (RedProtect.get().version >= 1110) {
                RPMine111.sendBarMsg(wel, "GREEN", p);
            } else {
                if (RedProtect.get().BossBar) {
                    BossBarAPI.setMessage(p, wel);
                } else {
                    p.sendMessage(wel);
                }
            }
        }
        if (RPConfig.getString("notify.welcome-mode").equalsIgnoreCase("CHAT")) {
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
            RedProtect.get().logger.debug("Removed task ID: " + remove + " for player " + p.getName());
        }
        toremove.clear();
    }

    private void EnterExitNotify(Region r, Player p) {
        if (RPConfig.getString("notify.region-enter-mode").equalsIgnoreCase("OFF")) {
            return;
        }

        if (!r.canEnter(p)) {
            return;
        }

        String leaderstring = "";
        String m = "";
        //Enter-Exit notifications
        if (r.getWelcome().equals("")) {
            if (RPConfig.getString("notify.region-enter-mode").equalsIgnoreCase("BOSSBAR")
                    || RPConfig.getString("notify.region-enter-mode").equalsIgnoreCase("CHAT")) {
                for (int i = 0; i < r.getLeaders().size(); ++i) {
                    leaderstring = leaderstring + ", " + RPUtil.UUIDtoPlayer(r.getLeaders().get(i));
                }

                if (r.getLeaders().size() > 0) {
                    leaderstring = leaderstring.substring(2);
                } else {
                    leaderstring = "None";
                }
                m = RPLang.get("playerlistener.region.entered");
                m = m.replace("{leaders}", leaderstring);
                m = m.replace("{region}", r.getName());
            }
            if (RedProtect.get().version >= 1110) {
                SendNotifyMsg(p, m, "GREEN");
            } else {
                SendNotifyMsg(p, m, null);
            }
        } else {
            String wel = ChatColor.translateAlternateColorCodes('&',
                    r.getWelcome().replace("{r}",r.getName())
                            .replace("{p}", p.getName()));
            if (RPConfig.getBool("notify.welcome-region-name"))
                SendWelcomeMsg(p, ChatColor.GOLD + r.getName() + ": " + wel);
            else
                SendWelcomeMsg(p, wel);
        }
    }

    private void RegionFlags(final Region r, Region er, final Player p, boolean join) {

        if (r.canEnter(p)) {

            //prevent spam commands
            if (join || RedProtect.get().rm.getTopRegion(p.getLocation()) != r) {

                //Enter command as player
                if (r.flagExists("player-enter-command") && !RedProtect.get().ph.hasPermOrBypass(p, "redprotect.admin.flag.player-enter-command")) {
                    String[] cmds = r.getFlagString("player-enter-command").split(",");
                    for (String cmd : cmds) {
                        if (cmd.startsWith("/")) {
                            cmd = cmd.substring(1);
                        }
                        p.getServer().dispatchCommand(p.getPlayer(), cmd.replace("{player}", p.getName()).replace("{region}", r.getName()));
                    }
                }

                //Enter command as console
                if (r.flagExists("server-enter-command") && !RedProtect.get().ph.hasPermOrBypass(p, "redprotect.admin.flag.server-enter-command")) {
                    String[] cmds = r.getFlagString("server-enter-command").split(",");
                    for (String cmd : cmds) {
                        if (cmd.startsWith("/")) {
                            cmd = cmd.substring(1);
                        }
                        RedProtect.get().serv.dispatchCommand(RedProtect.get().serv.getConsoleSender(), cmd.replace("{player}", p.getName()).replace("{region}", r.getName()));
                    }
                }
            }

            //Pvp check to enter on region
            if (RedProtect.get().PvPm) {
                if (r.isPvPArena() && !PvPlayer.get(p).hasPvPEnabled() && !r.canBuild(p)) {
                    RPLang.sendMessage(p, "playerlistener.region.pvpenabled");
                    RedProtect.get().serv.dispatchCommand(RedProtect.get().serv.getConsoleSender(), RPConfig.getString("flags-configuration.pvparena-nopvp-kick-cmd").replace("{player}", p.getName()));
                }
            }

            //enter Gamemode flag
            if (r.flagExists("gamemode") && !RedProtect.get().ph.hasPermOrBypass(p, "redprotect.admin.flag.gamemode")) {
                p.setGameMode(GameMode.valueOf(r.getFlagString("gamemode").toUpperCase()));
            }

            //Check portal (/rp flag set-portal <rp> <world>
            if (r.flagExists("set-portal")) {
                String[] cmds = r.getFlagString("set-portal").split(" ");
                RedProtect.get().serv.dispatchCommand(RedProtect.get().serv.getConsoleSender(), "rp teleport " + p.getName() + " " + cmds[0] + " " + cmds[1]);
            }

            //Enter MagicCarpet
            if (r.flagExists("allow-magiccarpet") && !r.getFlagBool("allow-magiccarpet") && RedProtect.get().Mc) {
                if (MagicCarpet.getCarpets().getCarpet(p) != null) {
                    MagicCarpet.getCarpets().remove(p);
                    RPLang.sendMessage(p, "playerlistener.region.cantmc");
                }
            }
        }

        if (er != null && er.canExit(p)) {

            //Exit gamemode
            if (er.flagExists("gamemode") && !RedProtect.get().ph.hasPermOrBypass(p, "redprotect.admin.flag.gamemode")) {
                p.setGameMode(Bukkit.getServer().getDefaultGameMode());
            }

            //Exit effect
            if (er.flagExists("effects") && !RedProtect.get().ph.hasPermOrBypass(p, "redprotect.admin.flag.effects")) {
                String[] effects = er.getFlagString("effects").split(",");
                for (String effect : effects) {
                    if (PlayertaskID.containsValue(p.getName())) {
                        String eff = effect.split(" ")[0];
                        String amplifier = effect.split(" ")[1];
                        PotionEffect fulleffect = new PotionEffect(PotionEffectType.getByName(eff), RPConfig.getInt("flags-configuration.effects-duration") * 20, Integer.parseInt(amplifier));
                        p.removePotionEffect(fulleffect.getType());
                        List<String> removeTasks = new ArrayList<>();
                        for (String taskId : PlayertaskID.keySet()) {
                            int id = Integer.parseInt(taskId.split("_")[0]);
                            String ideff = id + "_" + eff + er.getName();
                            if (PlayertaskID.containsKey(ideff) && PlayertaskID.get(ideff).equals(p.getName())) {
                                Bukkit.getScheduler().cancelTask(id);
                                removeTasks.add(taskId);
                                RedProtect.get().logger.debug("(RegionFlags-eff)Removed task ID: " + taskId + " for player " + p.getName());
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
                if (er.flagExists("forcefly") && !RedProtect.get().ph.hasPermOrBypass(p, "redprotect.admin.flag.forcefly") && (p.getGameMode().equals(GameMode.SURVIVAL) || p.getGameMode().equals(GameMode.ADVENTURE))) {
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
                                RedProtect.get().logger.debug("(RegionFlags fly)Removed task ID: " + taskId + " for player " + p.getName());
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
            if (er.flagExists("player-exit-command") && !RedProtect.get().ph.hasPermOrBypass(p, "redprotect.admin.flag.player-exit-command")) {
                String[] cmds = er.getFlagString("player-exit-command").split(",");
                for (String cmd : cmds) {
                    if (cmd.startsWith("/")) {
                        cmd = cmd.substring(1);
                    }
                    p.getServer().dispatchCommand(p.getPlayer(), cmd.replace("{player}", p.getName()).replace("{region}", er.getName()));
                }
            }

            //Exit command as console
            if (er.flagExists("server-exit-command") && !RedProtect.get().ph.hasPermOrBypass(p, "redprotect.admin.flag.server-exit-command")) {
                String[] cmds = er.getFlagString("server-exit-command").split(",");
                for (String cmd : cmds) {
                    if (cmd.startsWith("/")) {
                        cmd = cmd.substring(1);
                    }
                    RedProtect.get().serv.dispatchCommand(RedProtect.get().serv.getConsoleSender(), cmd.replace("{player}", p.getName()).replace("{region}", er.getName()));
                }
            }

            //Pvp check to exit region
            if (er.flagExists("forcepvp") && RedProtect.get().PvPm) {
                if (PvPState.containsKey(p.getName()) && !p.hasPermission("redprotect.forcepvp.bypass")) {
                    if (PvPState.get(p.getName()) != PvPlayer.get(p).hasPvPEnabled()) {
                        PvPlayer.get(p).setPvP(PvPState.get(p.getName()));
                    }
                    PvPState.remove(p.getName());
                }
            }
        }

        //2nd checks
        if (r.canEnter(p)) {

            //Enter check forcepvp flag
            if (RedProtect.get().PvPm) {
                if (r.canEnter(p) && r.flagExists("forcepvp") && !p.hasPermission("redprotect.forcepvp.bypass")) {
                    PvPlayer pvpp = PvPlayer.get(p);
                    if (r.forcePVP() != pvpp.hasPvPEnabled()) {
                        PvPState.put(p.getName(), pvpp.hasPvPEnabled());
                        pvpp.setPvP(r.forcePVP());
                    }
                }
            }

            //Enter effect
            if (r.flagExists("effects") && !RedProtect.get().ph.hasPermOrBypass(p, "redprotect.admin.flag.effects")) {
                String[] effects = r.getFlagString("effects").split(",");
                for (String effect : effects) {
                    String eff = effect.split(" ")[0];
                    String amplifier = effect.split(" ")[1];
                    final PotionEffect fulleffect = new PotionEffect(PotionEffectType.getByName(eff), RPConfig.getInt("flags-configuration.effects-duration") * 20, Integer.parseInt(amplifier));
                    int TaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(RedProtect.get(), new Runnable() {
                        public void run() {
                            if (p.isOnline() && r.flagExists("effects")) {
                                p.addPotionEffect(fulleffect, true);
                            } else {
                                p.setAllowFlight(false);
                                try {
                                    this.finalize();
                                } catch (Throwable e) {
                                    RedProtect.get().logger.debug("Effects not finalized...");
                                }
                            }
                        }
                    }, 0, 20);
                    PlayertaskID.put(TaskId + "_" + eff + r.getName(), p.getName());
                    RedProtect.get().logger.debug("Added task ID: " + TaskId + "_" + eff + " for player " + p.getName());
                }
            }

            //enter fly flag
            if (r.flagExists("forcefly") && !RedProtect.get().ph.hasPermOrBypass(p, "redprotect.admin.flag.forcefly") && (p.getGameMode().equals(GameMode.SURVIVAL) || p.getGameMode().equals(GameMode.ADVENTURE))) {
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
                                RedProtect.get().logger.debug("forcefly not finalized...");
                            }
                        }
                    }
                }, 0, 80);
                PlayertaskID.put(TaskId + "_" + "forcefly" + r.getName(), p.getName());
                RedProtect.get().logger.debug("(RegionFlags fly)Added task ID: " + TaskId + "_" + "forcefly" + " for player " + p.getName());
            }
        }
    }

    private void noRegionFlags(Region er, Player p) {

        if (er != null && er.canExit(p)) {

            //Pvp check to exit region
            if (er.flagExists("forcepvp") && RedProtect.get().PvPm) {
                if (PvPState.containsKey(p.getName()) && !p.hasPermission("redprotect.forcepvp.bypass")) {
                    if (PvPState.get(p.getName()) != PvPlayer.get(p).hasPvPEnabled()) {
                        PvPlayer.get(p).setPvP(PvPState.get(p.getName()));
                    }
                    PvPState.remove(p.getName());
                }
            }

            //Exit gamemode
            if (er.flagExists("gamemode") && !RedProtect.get().ph.hasPermOrBypass(p, "redprotect.admin.flag.gamemode")) {
                p.setGameMode(Bukkit.getServer().getDefaultGameMode());
            }

            //Exit effect
            if (er.flagExists("effects") && !RedProtect.get().ph.hasPermOrBypass(p, "redprotect.admin.flag.effects")) {
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
                                RedProtect.get().logger.debug("(noRegionFlags eff)Removed task ID: " + taskId + " for effect " + effect);
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
                if (er.flagExists("forcefly") && !RedProtect.get().ph.hasPermOrBypass(p, "redprotect.admin.flag.forcefly") && (p.getGameMode().equals(GameMode.SURVIVAL) || p.getGameMode().equals(GameMode.ADVENTURE))) {
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
                                RedProtect.get().logger.debug("(noRegionFlags fly)Removed task ID: " + taskId + " for player " + p.getName());
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
            if (er.flagExists("player-exit-command") && !RedProtect.get().ph.hasPermOrBypass(p, "redprotect.admin.flag.player-exit-command")) {
                String[] cmds = er.getFlagString("player-exit-command").split(",");
                for (String cmd : cmds) {
                    if (cmd.startsWith("/")) {
                        cmd = cmd.substring(1);
                    }
                    RedProtect.get().serv.dispatchCommand(p, cmd.replace("{player}", p.getName()));
                }
            }

            //Exit command as console
            if (er.flagExists("server-exit-command") && !RedProtect.get().ph.hasPermOrBypass(p, "redprotect.admin.flag.server-exit-command")) {
                String[] cmds = er.getFlagString("server-exit-command").split(",");
                for (String cmd : cmds) {
                    if (cmd.startsWith("/")) {
                        cmd = cmd.substring(1);
                    }
                    RedProtect.get().serv.dispatchCommand(RedProtect.get().serv.getConsoleSender(), cmd.replace("{player}", p.getName()));
                }
            }
        }
    }

    @EventHandler
    public void PlayerLogin(AsyncPlayerPreLoginEvent e) {
        if (!RPConfig.getBool("server-protection.nickname-cap-filter.enabled")) {
            return;
        }

        if (RedProtect.get().Ess) {
            User essp = RedProtect.get().pless.getOfflineUser(e.getName());
            if (essp != null && !essp.getConfigUUID().equals(e.getUniqueId())) {
                e.setKickMessage(RPLang.get("playerlistener.capfilter.kickmessage").replace("{nick}", essp.getName()));
                e.setLoginResult(Result.KICK_OTHER);
            }
        }
    }


    @EventHandler
    public void onHangingDamaged(HangingBreakByEntityEvent e) {
        if (e.isCancelled()) {
            return;
        }
        RedProtect.get().logger.debug("Is RPPlayerListener - HangingBreakByEntityEvent event");
        Entity ent = e.getRemover();
        Location loc = e.getEntity().getLocation();
        Region r = RedProtect.get().rm.getTopRegion(loc);

        if (ent instanceof Player) {
            Player player = (Player) ent;
            if (r != null && !r.canBuild(player) && !r.canBreak(e.getEntity().getType())) {
                RPLang.sendMessage(player, "blocklistener.region.cantbuild");
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
        Region r = RedProtect.get().rm.getTopRegion(l);

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
        Region r = RedProtect.get().rm.getTopRegion(l);

        if (r != null && !r.canBuild(p) && (p.getItemInHand().getType().name().contains("BUCKET"))) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent e) {
        if (!(e.getEntity() instanceof Player)) {
            return;
        }

        Player p = (Player) e.getEntity();
        Region r = RedProtect.get().rm.getTopRegion(p.getLocation());
        if (r != null && !r.canHunger()) {
            e.setCancelled(true);
        }
    }

}
