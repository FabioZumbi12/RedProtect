/*
 * Copyright (c) 2020 - @FabioZumbi12
 * Last Modified: 12/07/2020 20:24.
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

package br.net.fabiozumbi12.buyrentregion;

import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import br.net.fabiozumbi12.buyrentregion.config.BuyRentRegionConfig;
import br.net.fabiozumbi12.buyrentregion.config.ChatHelper;
import br.net.fabiozumbi12.buyrentregion.config.DigiFile;
import br.net.fabiozumbi12.buyrentregion.config.LocaleHelper;
import br.net.fabiozumbi12.buyrentregion.region.RentableRegion;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class BuyRentRegion extends JavaPlugin implements Listener, CommandExecutor {

    public static Economy econ = null;
    private static BuyRentRegion instance;
    private final HashMap<String, Boolean> BuyMode = new HashMap<>();
    public BuyRentRegionConfig config;
    public LocaleHelper locale;
    public DigiFile<ConcurrentHashMap<String, Long>> rentedRegionExpirations;
    public DigiFile<ConcurrentHashMap<String, Boolean>> autoRenews;
    public RedProtectHook redProtectHook;
    private DigiFile<HashMap<String, Integer>> regionCounts;
    private DigiFile<ConcurrentHashMap<String, Integer>> rentedRegionCounts;

    public static BuyRentRegion get() {
        return instance;
    }

    private static void save(Object obj, String dataLoc, String file) {
        try {
            File f = new File(dataLoc + file + ".digi");
            if (!f.exists()) {
                f.getParentFile().mkdirs();
                f.createNewFile();
            }
            ObjectOutputStream tmp = new ObjectOutputStream(new FileOutputStream(f));
            tmp.writeObject(obj);
            tmp.flush();
            tmp.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Object load(String dataLoc, String file) {
        try {
            ObjectInputStream tmp = new ObjectInputStream(new FileInputStream(dataLoc + file + ".digi"));
            Object rv = tmp.readObject();
            tmp.close();
            return rv;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onEnable() {
        instance = this;
        try {
            if (!setupEconomy()) {
                getLogger().severe("No Vault-compatible economy plugin found!");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }

            redProtectHook = new RedProtectHook();
            config = new BuyRentRegionConfig();
            locale = new LocaleHelper();

            getServer().getPluginManager().registerEvents(this, this);

            regionCounts = new DigiFile<>("RegionCounts", config.dataLoc, new HashMap<>());
            rentedRegionCounts = new DigiFile<>("RentedRegionCounts", config.dataLoc, new ConcurrentHashMap<>());
            rentedRegionExpirations = new DigiFile<>("RentedRegionExpirations", config.dataLoc, new ConcurrentHashMap<>());
            autoRenews = new DigiFile<>("AutoRenews", config.dataLoc, new ConcurrentHashMap<>());

            saveConfig();

            new RenterTask();

            File file = new File(config.dataLoc + "RegionActivityLog.txt");
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    getLogger().log(Level.SEVERE, "Error creating log file", e);
                }
            }
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "An error occurred while enabling BuyRentRegion", e);
        }
    }

    @Override
    public void onDisable() {
        try {
            regionCounts.save();
            rentedRegionExpirations.save();
            rentedRegionCounts.save();
            autoRenews.save();
            getServer().getScheduler().cancelTasks(this);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "An error occurred during shutdown.", e);
        }
    }

    private void renewRental(String regionName, Player sender) {
        try {
            if (new File(config.signDataLoc + regionName + ".digi").exists() && (this.rentedRegionExpirations.get().containsKey(regionName))) {
                RentableRegion region = loadRegion(regionName);
                if (sender.getName().equalsIgnoreCase(region.renter)) {
                    double regionPrice = Double.parseDouble(region.signLine3);
                    if (econ.getBalance(sender) >= regionPrice) {
                        EconomyResponse response = econ.withdrawPlayer(sender, regionPrice);
                        if (response.transactionSuccess()) {
                            if (config.payRentOwners) {
                                Region pRegion = redProtectHook.getRegion(regionName, sender.getWorld().getName());
                                if (pRegion != null) {
                                    payLeaders(pRegion, regionPrice);
                                }
                            }

                            String[] timeSpan = region.signLine4.split(" ");
                            long currentExpiration = this.rentedRegionExpirations.get().get(regionName);

                            DateResult timeData = parseDateString(Integer.parseInt(timeSpan[0]), timeSpan[1], currentExpiration);

                            this.rentedRegionExpirations.get().put(regionName, timeData.Time);
                            rentedRegionExpirations.save();

                            logActivity(sender.getName(), " RENEW " + regionName);

                            SimpleDateFormat sdf = new SimpleDateFormat(config.dateFormatString);

                            sender.sendMessage(ChatHelper.notice("Renewed", regionName, sdf.format(new Date(timeData.Time))));
                            sender.sendMessage(ChatHelper.notice("Balance", econ.getBalance(sender)));

                            World world = getServer().getWorld(region.worldName);

                            double x = Double.parseDouble(region.signLocationX);
                            double y = Double.parseDouble(region.signLocationY);
                            double z = Double.parseDouble(region.signLocationZ);
                            float pitch = Float.parseFloat(region.signLocationPitch);
                            float yaw = Float.parseFloat(region.signLocationYaw);

                            Location signLoc = new Location(world, x, y, z, pitch, yaw);

                            Block currentBlock = world.getBlockAt(signLoc);
                            if (currentBlock.getType().name().endsWith("_SIGN") || currentBlock.getType().name().endsWith("WALL_SIGN")) {
                                Sign theSign = (Sign) currentBlock.getState();

                                theSign.setLine(0, regionName);
                                theSign.setLine(1, sender.getName());
                                theSign.setLine(2, ChatColor.WHITE + locale.get("SignUntil"));
                                theSign.setLine(3, sdf.format(new Date(timeData.Time)));
                                theSign.update();

                                theSign.update();
                            }
                        } else {
                            sender.sendMessage(ChatHelper.notice("TransFailed"));
                        }
                    } else {
                        sender.sendMessage(ChatHelper.notice("NotEnoughRenew"));
                        sender.sendMessage(ChatHelper.notice("Balance", econ.getBalance(sender)));
                    }
                } else {
                    sender.sendMessage(ChatHelper.notice("NotRenting"));
                }
            } else {
                sender.sendMessage(ChatHelper.notice("NotRented", regionName));
            }
        } catch (Exception e) {
            getLogger().severe("An error has occurred while renewing rental for: " + regionName);
        }
    }

    public void logActivity(String player, String action) {
        try {
            Date tmp = new Date();
            File file = new File(config.dataLoc + "RegionActivityLog.txt");

            FileWriter out = new FileWriter(file, true);
            out.write(String.format("%s [%s] %s\r\n", tmp.toString(), player, action));
            out.flush();
            out.close();
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "An error occurred while trying to log activity.", e);
        }
    }

    private int getBoughtRegionsCount(String playerName) {
        if (regionCounts.get().containsKey(playerName)) {
            return this.regionCounts.get().get(playerName);
        }
        return 0;
    }

    public void removeRentedRegionFromCount(String playerName) {
        try {
            if (this.rentedRegionCounts.get().containsKey(playerName)) {
                int amount = getRentedRegionsCount(playerName);
                if (amount > 0) {
                    amount--;
                }
                if (amount >= 0) {
                    this.rentedRegionCounts.get().put(playerName, amount);
                } else {
                    this.rentedRegionCounts.get().put(playerName, 0);
                }
                rentedRegionCounts.save();
            }
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "An error occurred while removing a rented region from a player's count.", e);
        }
    }

    private int getRentedRegionsCount(String playerName) {
        if (rentedRegionCounts.get().containsKey(playerName)) {
            return rentedRegionCounts.get().get(playerName);
        }
        return 0;
    }

    private void setBoughtRegionsCount(String playerName, int amount, CommandSender sender) {
        try {
            this.regionCounts.get().put(playerName, amount);
            this.regionCounts.save();
            sender.sendMessage(ChatHelper.notice(playerName + " bought regions set to " + amount));
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "An error occurred in setBoughtRegions", e);
        }
    }

    private void setRentedRegionsCount(String playerName, int amount, CommandSender sender) {
        try {
            rentedRegionCounts.get().put(playerName, amount);
            rentedRegionCounts.save();
            sender.sendMessage(ChatHelper.notice(playerName + " rented regions set to " + amount));
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "An error occurred in setRentedRegionsCount", e);
        }
    }

    private void addRentedRegionFile(String playerName, String regionName, Sign sign) {
        RentableRegion region = new RentableRegion();

        Location tmpLoc = sign.getLocation();

        region.regionName = regionName;
        region.signLine1 = sign.getLine(0);
        region.signLine2 = sign.getLine(1);
        region.signLine3 = sign.getLine(2);
        region.signLine4 = sign.getLine(3);
        region.renter = playerName;
        region.signLocationX = String.valueOf(tmpLoc.getBlockX());
        region.signLocationY = String.valueOf(tmpLoc.getBlockY());
        region.signLocationZ = String.valueOf(tmpLoc.getBlockZ());
        region.signLocationPitch = String.valueOf(tmpLoc.getPitch());
        region.signLocationYaw = String.valueOf(tmpLoc.getYaw());
        region.signDirection = tmpLoc.getDirection().toString();
        region.worldName = sign.getWorld().getName();
        if (sign.getType().name().endsWith("WALL_SIGN")) {
            region.signType = Arrays.stream(Material.values()).filter(s -> s.name().endsWith("WALL_SIGN")).findFirst().get().name();
        } else {
            region.signType = Arrays.stream(Material.values()).filter(s -> s.name().endsWith("SIGN")).findFirst().get().name();
        }
        saveRentableRegion(region);
    }

    private void addBoughtRegionToCounts(String playerName) {
        if (this.regionCounts.get().containsKey(playerName)) {
            this.regionCounts.get().put(playerName, getBoughtRegionsCount(playerName) + 1);
        } else {
            this.regionCounts.get().put(playerName, 1);
        }
        regionCounts.save();
    }

    private void addRentedRegionToCounts(String playerName) {
        if (this.rentedRegionCounts.get().containsKey(playerName)) {
            this.rentedRegionCounts.get().put(playerName, getRentedRegionsCount(playerName) + 1);
        } else {
            this.rentedRegionCounts.get().put(playerName, 1);
        }
        rentedRegionCounts.save();
    }

    private void saveAutoRenews() {
        try {
            save(this.autoRenews, config.dataLoc, "autoRenews");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "An error has occurred saving autoRenews", e);
        }
    }

    private void checkPlayerRentedRegionCount(String playerName, CommandSender sender) {
        if (this.rentedRegionCounts.get().containsKey(playerName)) {
            sender.sendMessage(ChatHelper.notice(playerName + " has " + getRentedRegionsCount(playerName) + " rented regions."));
        } else {
            sender.sendMessage(ChatHelper.notice(playerName + " has no rented regions."));
        }
    }

    private void checkPlayerRegionCount(String playerName, CommandSender sender) {
        if (this.regionCounts.get().containsKey(playerName)) {
            sender.sendMessage(ChatHelper.notice(playerName + " has " + getBoughtRegionsCount(playerName) + " bought regions."));
        } else {
            sender.sendMessage(ChatHelper.notice(playerName + " has no bought regions."));
        }
    }

    private void saveRegion(RentableRegion region) {
        save(region.toString(), config.signDataLoc, region.regionName);
    }

    public RentableRegion loadRegion(String regionName) {
        String tmp = (String) load(config.signDataLoc, regionName);

        return new RentableRegion(tmp);
    }

    private void setAutoRenew(String playerName, boolean autoRenew) {
        if (autoRenew) {
            this.autoRenews.get().put(playerName, Boolean.TRUE);
            saveAutoRenews();
        } else {
            this.autoRenews.get().remove(playerName);
            saveAutoRenews();
        }
    }

    @EventHandler
    public void onPunchSign(PlayerInteractEvent event) {
        try {
            if (event.getAction().name().equals("RIGHT_CLICK_BLOCK")) {
                Material blockType = event.getClickedBlock().getType();
                if (blockType.name().endsWith("_SIGN") || blockType.name().endsWith("WALL_SIGN")) {
                    Sign sign = (Sign) event.getClickedBlock().getState();
                    String topLine = sign.getLine(0);
                    if (topLine.length() > 0 && (topLine.equalsIgnoreCase(config.signHeaderBuy))) {
                        Player sender = event.getPlayer();
                        String playerName = sender.getName();
                        if (topLine.equalsIgnoreCase("[WGRSA]")) {
                            sign.setLine(0, config.signHeaderBuy);
                            sign.update();
                        }
                        if (config.requireBuyPerms && !sender.hasPermission("buyrentregion.command.buy") && !sender.isOp()) {
                            sender.sendMessage(ChatHelper.notice("BuyPerms"));
                            return;
                        }
                        if (this.config.buyRegionMax > 0 && getBoughtRegionsCount(playerName) >= this.config.buyRegionMax && !sender.isOp() && (!sender.hasPermission("buyregion.exempt"))) {
                            sender.sendMessage(ChatHelper.notice("BuyMax", this.config.buyRegionMax));
                            return;
                        }
                        if (this.BuyMode.containsKey(playerName) || !config.requireBuyMode) {
                            double regionPrice = Double.parseDouble(sign.getLine(2));

                            String regionName = sign.getLine(1);
                            World world = sender.getWorld();

                            Region region = redProtectHook.getRegion(regionName, world.getName());

                            if (region == null) {
                                sender.sendMessage(ChatHelper.notice("RegionNoExist"));
                                return;
                            }

                            if (region.canBuild(sender)) {
                                sender.sendMessage(ChatHelper.notice("CantSelf"));
                                return;
                            }

                            if (econ.getBalance(sender) >= regionPrice) {
                                EconomyResponse response = econ.withdrawPlayer(sender, regionPrice);
                                if (response.transactionSuccess()) {
                                    payLeaders(region, regionPrice);

                                    region.addLeader(sender.getUniqueId().toString());

                                    addBoughtRegionToCounts(playerName);

                                    sender.sendMessage(ChatHelper.notice("Purchased", regionName));
                                    sender.sendMessage(ChatHelper.notice("NewBalance", econ.getBalance(sender)));

                                    logActivity(playerName, " BUY " + regionName);

                                    sign.setLine(0, ChatColor.translateAlternateColorCodes('&', locale.get("SignSold")));
                                    sign.setLine(1, ChatColor.translateAlternateColorCodes('&', locale.get("SignSoldTo")));
                                    sign.setLine(2, ChatColor.WHITE + playerName);
                                    sign.setLine(3, ChatColor.translateAlternateColorCodes('&', locale.get("SignSold")));
                                    sign.update();

                                    this.BuyMode.remove(playerName);
                                } else {
                                    sender.sendMessage(ChatHelper.notice("TransFailed"));
                                }
                            } else {
                                sender.sendMessage(ChatHelper.warning("NotEnoughBuy"));
                                sender.sendMessage(ChatHelper.warning("Balance", econ.getBalance(sender)));
                            }
                        } else {
                            sender.sendMessage(ChatHelper.warning("BuyModeBuy"));
                            sender.sendMessage(ChatHelper.warning("ToEnterBuyMode"));
                        }
                    } else if (topLine.length() > 0 && (topLine.equalsIgnoreCase(config.signHeaderRent))) {
                        Player sender = event.getPlayer();
                        String regionName = sign.getLine(1);
                        String playerName = sender.getName();
                        if (config.requireRentPerms && !sender.hasPermission("buyrentregion.command.rent") && (!sender.isOp())) {
                            sender.sendMessage(ChatHelper.warning("RentPerms"));
                            return;
                        }
                        if (config.rentRegionMax > 0 && getRentedRegionsCount(playerName) >= config.rentRegionMax && !sender.isOp() && (!sender.hasPermission("buyrentregion.command.exempt"))) {
                            sender.sendMessage(ChatHelper.notice("RentMax", config.rentRegionMax));
                            return;
                        }
                        if (this.BuyMode.containsKey(playerName) || (!config.requireBuyMode)) {
                            if (regionName.length() > 0) {
                                String dateString = sign.getLine(3);
                                double regionPrice;

                                try {
                                    regionPrice = Double.parseDouble(sign.getLine(2));

                                    String[] expiration = dateString.split("\\s");
                                    int i = Integer.parseInt(expiration[0]);
                                    DateResult dateResult = parseDateString(i, expiration[1]);
                                    if (dateResult.IsError) {
                                        throw new Exception();
                                    }
                                } catch (Exception e) {
                                    getLogger().info("Region price or expiration");
                                    sign.setLine(0, "-invalid-");
                                    sign.setLine(1, "<region here>");
                                    sign.setLine(2, "<price here>");
                                    sign.setLine(3, "<timespan>");
                                    sign.update();
                                    getLogger().info("Invalid [RentRegion] sign cleared at " + sign.getLocation().toString());
                                    return;
                                }
                                String[] expiration = sign.getLine(3).split("\\s");
                                DateResult dateResult = parseDateString(Integer.parseInt(expiration[0]), expiration[1]);
                                if (dateResult.IsError) {
                                    throw new Exception();
                                }
                                World world = sender.getWorld();

                                Region region = redProtectHook.getRegion(regionName, world.getName());

                                if (region == null) {
                                    sender.sendMessage(ChatHelper.notice("RegionNoExist"));
                                    sign.setLine(0, "-invalid-");
                                    sign.setLine(1, "<region here>");
                                    sign.setLine(2, "<price here>");
                                    sign.setLine(3, "<timespan>");
                                    sign.update();
                                    getLogger().info("Invalid [RentRegion] sign cleared at " + sign.getLocation().toString());
                                    return;
                                }

                                if (region.canBuild(sender)) {
                                    sender.sendMessage(ChatHelper.notice("CantSelf"));
                                    return;
                                }

                                if (econ.getBalance(sender) >= regionPrice) {
                                    EconomyResponse response = econ.withdrawPlayer(sender, regionPrice);
                                    if (response.transactionSuccess()) {
                                        if (config.payRentOwners) {
                                            payLeaders(region, regionPrice);
                                        }

                                        region.addMember(sender.getUniqueId().toString());

                                        addRentedRegionFile(playerName, regionName, sign);

                                        addRentedRegionToCounts(playerName);

                                        logActivity(playerName, " RENT " + regionName);

                                        SimpleDateFormat sdf = new SimpleDateFormat(config.dateFormatString);

                                        sign.setLine(0, regionName);
                                        sign.setLine(1, playerName);
                                        sign.setLine(2, ChatColor.WHITE + locale.get("SignUntil"));
                                        sign.setLine(3, sdf.format(new Date(dateResult.Time)));
                                        sign.update();

                                        sender.sendMessage(ChatHelper.notice("Rented", regionName, sdf.format(new Date(dateResult.Time))));
                                        sender.sendMessage(ChatHelper.notice("NewBalance", econ.getBalance(sender)));

                                        this.rentedRegionExpirations.get().put(regionName, dateResult.Time);
                                        rentedRegionExpirations.save();

                                        this.BuyMode.remove(playerName);
                                    } else {
                                        sender.sendMessage(ChatHelper.warning("TransFailed"));
                                    }
                                } else {
                                    sender.sendMessage(ChatHelper.warning("NotEnoughRent"));
                                    sender.sendMessage(ChatHelper.warning("Balance", econ.getBalance(sender)));
                                }
                            }
                        } else {
                            sender.sendMessage(ChatHelper.warning("BuyModeRent"));
                            sender.sendMessage(ChatHelper.warning("ToEnterBuyMode"));
                        }
                    }
                }
            }
        } catch (Exception e) {
            getLogger().severe(e.getMessage());
        }
    }

    private void payLeaders(Region region, double regionPrice) {
        double v = regionPrice / region.getLeaders().size();
        region.getLeaders().forEach(o -> {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(o.getUUID());
            try {
                offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(o.getUUID()));
            } catch (Exception ignored) {
            }
            econ.depositPlayer(offlinePlayer, v);
        });
    }

    private DateResult parseDateString(int val, String type) {
        try {
            Date tmp = new Date();
            if (type.equalsIgnoreCase("d") || type.equalsIgnoreCase("day") || (type.equalsIgnoreCase("days"))) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(tmp);
                cal.add(Calendar.DATE, val);

                return new DateResult(cal.getTime().getTime(), val + " days", false);
            }
            if (type.equalsIgnoreCase("h") || type.equalsIgnoreCase("hour") || (type.equalsIgnoreCase("hours"))) {
                return new DateResult(tmp.getTime() + val * 60 * 60 * 1000, val + " hours", false);
            }
            if (type.equalsIgnoreCase("m") || type.equalsIgnoreCase("mins") || type.equalsIgnoreCase("min") || type.equalsIgnoreCase("minutes") || (type.equalsIgnoreCase("minute"))) {
                return new DateResult(tmp.getTime() + val * 60 * 1000, val + " minutes", false);
            }
            if (type.equalsIgnoreCase("s") || type.equalsIgnoreCase("sec") || type.equalsIgnoreCase("secs") || type.equalsIgnoreCase("seconds") || (type.equalsIgnoreCase("second"))) {
                return new DateResult(tmp.getTime() + val * 1000, val + " seconds", false);
            }
            return new DateResult(-1L, "ERROR", true);
        } catch (Exception ignored) {
        }
        return new DateResult(-1L, "ERROR", true);
    }

    public DateResult parseDateString(int val, String type, long start) {
        try {
            Date tmp = new Date(start);
            if (type.equalsIgnoreCase("d") || type.equalsIgnoreCase("day") || (type.equalsIgnoreCase("days"))) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(tmp);
                cal.add(Calendar.DATE, val);

                return new DateResult(cal.getTime().getTime(), val + " days", false);
            }
            if (type.equalsIgnoreCase("h") || type.equalsIgnoreCase("hour") || (type.equalsIgnoreCase("hours"))) {
                return new DateResult(tmp.getTime() + val * 60 * 60 * 1000, val + " hours", false);
            }
            if (type.equalsIgnoreCase("m") || type.equalsIgnoreCase("mins") || type.equalsIgnoreCase("min") || type.equalsIgnoreCase("minutes") || (type.equalsIgnoreCase("minute"))) {
                return new DateResult(tmp.getTime() + val * 60 * 1000, val + " minutes", false);
            }
            if (type.equalsIgnoreCase("s") || type.equalsIgnoreCase("sec") || type.equalsIgnoreCase("secs") || type.equalsIgnoreCase("seconds") || (type.equalsIgnoreCase("second"))) {
                return new DateResult(tmp.getTime() + val * 1000, val + " seconds", false);
            }
            return new DateResult(-1L, "ERROR", true);
        } catch (Exception ignored) {
        }
        return new DateResult(-1L, "ERROR", true);
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null)
            return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null)
            return false;
        econ = rsp.getProvider();
        return true;
    }

    private void toggleBuyMode(CommandSender sender) {
        try {
            String playerName = sender.getName();
            if (!this.BuyMode.containsKey(playerName)) {
                this.BuyMode.put(sender.getName(), Boolean.TRUE);
                sender.sendMessage(ChatHelper.notice("BuyModeEnter"));
            } else {
                this.BuyMode.remove(playerName);
                sender.sendMessage(ChatHelper.notice("BuyModeExit"));
            }
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "An error occurred in toggleBuyMode", e);
        }
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("buyrentregion")) {
            if (args.length == 0) {
                toggleBuyMode(sender);
            } else {
                if (args[0].equalsIgnoreCase("renew") && sender instanceof Player) {
                    if (args.length < 2) {
                        sender.sendMessage(ChatHelper.notice("InvalidRenewArgs"));
                    } else {
                        renewRental(args[1], (Player) sender);
                    }
                    return false;
                }
                if (args[0].equalsIgnoreCase("autorenew")) {
                    if (args.length < 2) {
                        if (this.autoRenews.get().containsKey(sender.getName())) {
                            if (this.autoRenews.get().get(sender.getName())) {
                                sender.sendMessage(ChatHelper.notice("RenewOn"));
                            } else {
                                sender.sendMessage(ChatHelper.notice("RenewOff"));
                            }
                        } else {
                            sender.sendMessage(ChatHelper.notice("RenewOff"));
                        }
                    } else if (args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("yes") || (args[1].equalsIgnoreCase("on"))) {
                        setAutoRenew(sender.getName(), true);
                        sender.sendMessage(ChatHelper.notice("RenewTurnOn"));
                    } else if (args[1].equalsIgnoreCase("false") || args[1].equalsIgnoreCase("no") || (args[1].equalsIgnoreCase("off"))) {
                        setAutoRenew(sender.getName(), false);
                        sender.sendMessage(ChatHelper.notice("RenewTurnOff"));
                    } else {
                        sender.sendMessage(ChatHelper.notice("InvalidArg"));
                    }
                    return false;
                }
                if (args[0].equalsIgnoreCase("help")) {
                    String[] help = {ChatHelper.notice("Help1"), ChatHelper.notice("Help2"), ChatHelper.notice("Help3"), ChatHelper.notice("Help4")};
                    sender.sendMessage(help);
                }
                if (sender.isOp() || (sender.hasPermission("buyrentregion.command.admin"))) {
                    if (args[0].equalsIgnoreCase("reload")) {
                        reloadConfig();
                        config = new BuyRentRegionConfig();
                        locale = new LocaleHelper();
                        sender.sendMessage(ChatHelper.notice("BuyRentRegion reloaded"));
                    } else if (args[0].equalsIgnoreCase("buycheck")) {
                        checkPlayerRegionCount(args[1], sender);
                    } else if (args[0].equalsIgnoreCase("rentcheck")) {
                        checkPlayerRentedRegionCount(args[1], sender);
                    } else if (args[0].equalsIgnoreCase("buyset")) {
                        if (args.length < 3) {
                            sender.sendMessage(ChatHelper.warning("Invalid args - /buyrentregion buyset <player> <amount>"));
                        } else {
                            int amount;

                            try {
                                amount = Integer.parseInt(args[2]);
                                if (amount < 0) {
                                    amount = 0;
                                }
                            } catch (Exception e) {
                                sender.sendMessage(ChatHelper.warning("Invalid amount. Enter a number for the amount."));
                                return false;
                            }
                            setBoughtRegionsCount(args[1], amount, sender);
                        }
                    } else if (args[0].equalsIgnoreCase("rentset")) {
                        if (args.length < 3) {
                            sender.sendMessage(ChatHelper.warning("Invalid args - /buyrentregion rentset <player> <amount>"));
                        } else {
                            int amount;
                            try {
                                amount = Integer.parseInt(args[2]);
                                if (amount < 0)
                                    amount = 0;
                            } catch (Exception e) {
                                sender.sendMessage(ChatHelper.warning("Invalid amount. Enter a number for the amount."));
                                return false;
                            }
                            setRentedRegionsCount(args[1], amount, sender);
                        }
                    } else {
                        if (args[0].equalsIgnoreCase("buymax")) {
                            try {
                                if (args.length < 2) {
                                    sender.sendMessage(ChatHelper.notice("Current config.buyRegionMax: " + this.config.buyRegionMax));
                                } else {
                                    int amount;

                                    try {
                                        amount = Integer.parseInt(args[1]);
                                        if (amount < 0)
                                            amount = 0;
                                    } catch (Exception e) {
                                        sender.sendMessage(ChatHelper.warning("Invalid amount. Enter a number for the amount."));
                                        return false;
                                    }
                                    this.config.buyRegionMax = amount;
                                    getConfig().set("config.buyRegionMax", amount);
                                    saveConfig();

                                    sender.sendMessage(ChatHelper.notice("config.buyRegionMax has been updated to " + amount));
                                }
                            } catch (Exception e) {
                                sender.sendMessage("An error occurred... check all values and try again.");
                            }
                        }
                        if (args[0].equalsIgnoreCase("rentmax")) {
                            try {
                                if (args.length < 2) {
                                    sender.sendMessage(ChatHelper.notice("Current RentRegionMax: " + config.rentRegionMax));
                                } else {
                                    int amount;

                                    try {
                                        amount = Integer.parseInt(args[1]);
                                        if (amount < 0)
                                            amount = 0;
                                    } catch (Exception e) {
                                        sender.sendMessage(ChatHelper.warning("Invalid amount. Enter a number for the amount."));
                                        return false;
                                    }
                                    config.rentRegionMax = amount;
                                    getConfig().set("RentRegionMax", amount);
                                    saveConfig();

                                    sender.sendMessage(ChatHelper.warning("RentRegionMax has been updated to " + amount));
                                }
                            } catch (Exception e) {
                                sender.sendMessage(ChatHelper.warning("An error occurred... check all values and try again."));
                            }
                        }
                        if (args[0].equalsIgnoreCase("buyperms")) {
                            try {
                                if (args.length > 1) {
                                    if (args[1].equalsIgnoreCase("true") || (args[1].equalsIgnoreCase("false"))) {
                                        boolean val = Boolean.parseBoolean(args[1]);
                                        if (val) {
                                            config.requireBuyPerms = true;
                                            getConfig().set("RequireBuyPerms", Boolean.TRUE);
                                        } else {
                                            config.requireBuyPerms = false;
                                            getConfig().set("RequireBuyPerms", Boolean.FALSE);
                                        }
                                        sender.sendMessage(ChatHelper.notice("RequireBuyPerms set."));
                                        saveConfig();
                                    } else {
                                        sender.sendMessage(ChatHelper.warning("Invalid value. Enter 'true' or 'false'"));
                                    }
                                } else {
                                    sender.sendMessage(ChatHelper.notice("RequireBuyPerms: " + getConfig().getBoolean("RequireBuyPerms")));
                                }
                            } catch (Exception e) {
                                sender.sendMessage(ChatHelper.warning("An error occurred... Syntax: /buyrentregion buyperms true/false"));
                                return false;
                            }
                        } else if (args[0].equalsIgnoreCase("rentperms")) {
                            try {
                                if (args.length > 1) {
                                    if (args[1].equalsIgnoreCase("true") || (args[1].equalsIgnoreCase("false"))) {
                                        boolean val = Boolean.parseBoolean(args[1]);
                                        if (val) {
                                            config.requireRentPerms = true;
                                            getConfig().set("RequireRentPerms", Boolean.TRUE);
                                        } else {
                                            config.requireRentPerms = false;
                                            getConfig().set("RequireRentPerms", Boolean.FALSE);
                                        }
                                        sender.sendMessage(ChatHelper.notice("RequireRentPerms set."));
                                        saveConfig();
                                    } else {
                                        sender.sendMessage(ChatHelper.warning("Invalid value. Enter 'true' or 'false'"));
                                    }
                                } else {
                                    sender.sendMessage(ChatHelper.notice("RequireRentPerms: " + getConfig().getBoolean("RequireRentPerms")));
                                }
                            } catch (Exception e) {
                                sender.sendMessage(ChatHelper.warning("An error occurred... Syntax: /buyrentregion rentperms true/false"));
                                return false;
                            }
                        } else if (args[0].equalsIgnoreCase("buymode")) {
                            try {
                                if (args.length > 1) {
                                    if (args[1].equalsIgnoreCase("true") || (args[1].equalsIgnoreCase("false"))) {
                                        boolean val = Boolean.parseBoolean(args[1]);
                                        if (val) {
                                            config.requireBuyMode = true;
                                            getConfig().set("RequireBuyMode", Boolean.TRUE);
                                        } else {
                                            config.requireBuyMode = false;
                                            getConfig().set("RequireBuyMode", Boolean.FALSE);
                                        }
                                        sender.sendMessage(ChatHelper.notice("RequireBuyMode set."));
                                        saveConfig();
                                    } else {
                                        sender.sendMessage(ChatHelper.warning("Invalid value. Enter 'true' or 'false'"));
                                    }
                                } else {
                                    sender.sendMessage(ChatHelper.notice("RequireBuyMode: " + getConfig().getBoolean("RequireBuyMode")));
                                }
                            } catch (Exception e) {
                                sender.sendMessage(ChatHelper.warning("An error occurred... Syntax: /buyrentregion buymode true/false"));
                                return false;
                            }
                        } else if (args[0].equalsIgnoreCase("evict")) {
                            if (args.length > 1) {
                                String regionName = args[1];
                                if (new File(config.signDataLoc + regionName + ".digi").exists()) {
                                    if (evictRegion(regionName)) {
                                        sender.sendMessage(ChatHelper.notice("Region eviction completed!"));
                                    } else {
                                        sender.sendMessage(ChatHelper.warning("Region eviction failed."));
                                    }
                                } else {
                                    sender.sendMessage(ChatHelper.warning("Region is not currently rented!"));
                                }
                            } else {
                                sender.sendMessage(ChatHelper.warning("Invalid syntax: /buyrentregion evict <region>"));
                                return false;
                            }
                        } else {
                            String[] help = {ChatHelper.notice("Admin Commands:"), ChatHelper.notice("/buyrentregion buymode <true/false> - sets RequireBuyMode"), ChatHelper.notice("/buyregion buycheck <player> - checks total bought regions for <player>"), ChatHelper.notice("/buyregion rentcheck <player> - checks total rented regions for <player>"), ChatHelper.notice("/buyregion buyset <player> <amount> - sets total bought regions for <player>"), ChatHelper.notice("/buyregion rentset <player> <amount> - sets total rented regions for <player>"), ChatHelper.notice("/buyregion buymax - displays current config.buyRegionMax"), ChatHelper.notice("/buyregion buymax <amount> - sets config.buyRegionMax"), ChatHelper.notice("/buyregion rentmax - displays current RentRegionMax"), ChatHelper.notice("/buyregion rentmax <amount> - sets RentRegionMax"), ChatHelper.notice("/buyregion evict <region> - evicts renter from <region>")};
                            sender.sendMessage(help);
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean evictRegion(String regionName) {
        try {
            RentableRegion rentedRegion = loadRegion(regionName);

            this.rentedRegionExpirations.get().remove(regionName);
            rentedRegionExpirations.save();

            World world = getServer().getWorld(rentedRegion.worldName);
            Region region = redProtectHook.getRegion(regionName, rentedRegion.worldName);

            if (region == null)
                return false;

            region.removeMember(rentedRegion.renter);

            removeRentedRegionFromCount(rentedRegion.renter);

            double x = Double.parseDouble(rentedRegion.signLocationX);
            double y = Double.parseDouble(rentedRegion.signLocationY);
            double z = Double.parseDouble(rentedRegion.signLocationZ);
            float pitch = Float.parseFloat(rentedRegion.signLocationPitch);
            float yaw = Float.parseFloat(rentedRegion.signLocationYaw);

            Location signLoc = new Location(world, x, y, z, pitch, yaw);

            Block currentBlock = world.getBlockAt(signLoc);
            if (currentBlock.getType().name().endsWith("_SIGN") || currentBlock.getType().name().endsWith("WALL_SIGN")) {
                Sign theSign = (Sign) currentBlock.getState();

                theSign.setLine(0, rentedRegion.signLine1);
                theSign.setLine(1, rentedRegion.signLine2);
                theSign.setLine(2, rentedRegion.signLine3);
                theSign.setLine(3, rentedRegion.signLine4);

                theSign.update();
            } else {
                try {
                    if (rentedRegion.signType.endsWith("WALL_SIGN")) {
                        currentBlock.setType(Arrays.stream(Material.values()).filter(s -> s.name().endsWith("WALL_SIGN")).findFirst().get());
                    } else {
                        currentBlock.setType(Arrays.stream(Material.values()).filter(s -> s.name().endsWith("_SIGN")).findFirst().get());
                    }
                    Sign newSign = (Sign) currentBlock.getState();

                    newSign.setLine(0, rentedRegion.signLine1);
                    newSign.setLine(1, rentedRegion.signLine2);
                    newSign.setLine(2, rentedRegion.signLine3);
                    newSign.setLine(3, rentedRegion.signLine4);

                    newSign.update();
                } catch (Exception e) {
                    getLogger().severe("RentRegion automatic sign creation failed for region " + rentedRegion.regionName);
                }
            }
            File regionFile = new File(config.signDataLoc + regionName + ".digi");
            if (regionFile.exists()) {
                regionFile.delete();
            }
            Player player = getServer().getPlayer(rentedRegion.renter);
            if ((player != null)) {
                player.sendMessage(ChatHelper.notice("EvictedFrom", regionName));
            }
            logActivity(rentedRegion.renter, " EVICTED " + rentedRegion.regionName);

            return true;
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "An error occurred during an eviction.", e);
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void signChangeMonitor(SignChangeEvent event) {
        try {
            Player player = event.getPlayer();
            if (event.getLine(0).equalsIgnoreCase(config.signHeaderBuy) || (event.getLine(0).equalsIgnoreCase(config.signHeaderRent))) {
                if (!player.hasPermission("buyrentregion.command.create") && !player.isOp()) {
                    event.setLine(0, "-restricted-");
                } else {
                    Region region = redProtectHook.getRegion(event.getBlock().getLocation());
                    String regionName = event.getLine(1);

                    if (region != null && regionName.isEmpty()) {
                        regionName = region.getName();
                    } else if (!regionName.isEmpty()) {
                        World world = event.getBlock().getWorld();
                        region = redProtectHook.getRegion(regionName, world.getName());
                    }

                    if (region == null) {
                        event.getPlayer().sendMessage(ChatHelper.warning("RegionNoExist"));
                        event.setLine(0, "-invalid-");
                        return;
                    }

                    if (!region.isLeader(player) && !player.hasPermission("" +
                            "buyrentregion.command.admin")) {
                        event.getPlayer().sendMessage(ChatHelper.warning("NotOwner"));
                        event.setLine(0, "-invalid-");
                        return;
                    }

                    event.setLine(1, regionName);
                    try {
                        String dateString = event.getLine(3);
                        try {
                            double regionPrice = Double.parseDouble(event.getLine(2));
                            if (regionPrice <= 0.0D) {
                                throw new Exception();
                            }
                            if (event.getLine(0).equalsIgnoreCase(config.signHeaderRent)) {
                                String[] expiration = dateString.split("\\s");
                                int i = Integer.parseInt(expiration[0]);
                                DateResult dateResult = parseDateString(i, expiration[1]);
                                if (dateResult.IsError) {
                                    throw new Exception();
                                }
                            }
                        } catch (Exception e) {
                            event.getPlayer().sendMessage(ChatHelper.notice("InvalidPriceTime"));
                            event.setLine(0, "-invalid-");

                            return;
                        }
                        if (!event.getLine(0).equalsIgnoreCase(config.signHeaderRent)) {
                            event.setLine(0, config.signHeaderBuy);
                        } else {
                            event.setLine(0, config.signHeaderRent);
                        }
                    } catch (Exception e) {
                        event.getPlayer().sendMessage(ChatHelper.notice("Invalid amount!"));
                        event.setLine(0, "-invalid-");
                        return;
                    }
                    event.getPlayer().sendMessage(ChatHelper.notice("A BuyRentRegion sign has been created!"));
                }
            }
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "An error occurred in signChangeMonitor", e);
        }
    }

    private void saveRentableRegion(RentableRegion region) {
        try {
            saveRegion(region);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "An error has occurred saving a RentableRegion.", e);
        }
    }

    public class DateResult {
        public long Time;
        String Text;
        boolean IsError;

        DateResult(long time, String text, boolean isError) {
            this.Time = time;
            this.Text = text;
            this.IsError = isError;
        }
    }
}
