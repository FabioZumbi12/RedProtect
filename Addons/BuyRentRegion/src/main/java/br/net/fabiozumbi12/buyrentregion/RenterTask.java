/*
 * Copyright (c) 2012-2023 - @FabioZumbi12
 * Last Modified: 10/05/2023 14:49
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

import io.github.fabiozumbi12.RedProtect.Bukkit.Region;
import br.net.fabiozumbi12.buyrentregion.config.ChatHelper;
import br.net.fabiozumbi12.buyrentregion.region.RentableRegion;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RenterTask {
    private final BuyRentRegion plugin;

    public RenterTask() {
        plugin = BuyRentRegion.get();
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this::run, plugin.config.tickRate, plugin.config.tickRate);
    }

    private void run() {
        try {
            long now = new Date().getTime();
            ConcurrentHashMap<String, Long> expirations = plugin.rentedRegionExpirations.get();
            for (String regionName : expirations.keySet()) {
                if (expirations.containsKey(regionName)) {
                    long regionExp = expirations.get(regionName);
                    if (regionExp <= now) {
                        boolean renewed = false;

                        RentableRegion rentedRegion = plugin.loadRegion(regionName);
                        if (plugin.autoRenews.get().containsKey(rentedRegion.renter)) {
                            if (plugin.autoRenews.get().get(rentedRegion.renter)) {
                                Player player = plugin.getServer().getPlayer(rentedRegion.renter);

                                double regionPrice = Double.parseDouble(rentedRegion.signLine3);
                                if (BuyRentRegion.econ.getBalance(rentedRegion.renter) >= regionPrice) {
                                    EconomyResponse response = BuyRentRegion.econ.withdrawPlayer(rentedRegion.renter, regionPrice);
                                    if (response.transactionSuccess()) {
                                        if (plugin.config.payRentOwners) {
                                            Region pRegion = plugin.redProtectHook.getRegion(regionName, rentedRegion.worldName);
                                            if (pRegion != null) {
                                                double v = regionPrice / pRegion.leaderSize();
                                                pRegion.getLeaders().forEach(o -> {
                                                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(o.getUUID());
                                                    try {
                                                        offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(o.getUUID()));
                                                    } catch (Exception ignored) {
                                                    }
                                                    BuyRentRegion.econ.depositPlayer(offlinePlayer, v);
                                                });
                                            }
                                        }

                                        renewed = true;

                                        String[] timeSpan = rentedRegion.signLine4.split(" ");
                                        long currentExpiration = expirations.get(regionName);

                                        BuyRentRegion.DateResult timeData = plugin.parseDateString(Integer.parseInt(timeSpan[0]), timeSpan[1], currentExpiration);
                                        expirations.put(regionName, timeData.Time);
                                        plugin.rentedRegionExpirations.save();

                                        plugin.logActivity(rentedRegion.renter, " AUTORENEW " + regionName);

                                        SimpleDateFormat sdf = new SimpleDateFormat(plugin.config.dateFormatString);
                                        if (player != null) {
                                            player.sendMessage(ChatHelper.notice("Renewed", regionName, sdf.format(new Date(timeData.Time))));
                                            player.sendMessage(ChatHelper.notice("NewBalance", BuyRentRegion.econ.getBalance(rentedRegion.renter)));
                                        }
                                        World world = plugin.getServer().getWorld(rentedRegion.worldName);

                                        double x = Double.parseDouble(rentedRegion.signLocationX);
                                        double y = Double.parseDouble(rentedRegion.signLocationY);
                                        double z = Double.parseDouble(rentedRegion.signLocationZ);
                                        float pitch = Float.parseFloat(rentedRegion.signLocationPitch);
                                        float yaw = Float.parseFloat(rentedRegion.signLocationYaw);

                                        Location signLoc = new Location(world, x, y, z, pitch, yaw);

                                        Block currentBlock = world.getBlockAt(signLoc);
                                        if (currentBlock.getType().name().endsWith("_SIGN") || currentBlock.getType().name().endsWith("WALL_SIGN")) {
                                            Sign theSign = (Sign) currentBlock.getState();

                                            theSign.setLine(0, regionName);
                                            theSign.setLine(1, rentedRegion.renter);
                                            theSign.setLine(2, ChatColor.WHITE + BuyRentRegion.get().locale.get("SignUntil"));
                                            theSign.setLine(3, sdf.format(new Date(timeData.Time)));
                                            theSign.update();

                                            theSign.update();
                                        }
                                    }
                                } else if (player != null) {
                                    player.sendMessage(ChatHelper.notice("NotEnoughRenew", regionName));
                                    player.sendMessage(ChatHelper.notice("Balance", BuyRentRegion.econ.getBalance(rentedRegion.renter)));
                                }
                            }
                        }
                        if (!renewed) {
                            expirations.remove(regionName);
                            plugin.rentedRegionExpirations.save();

                            World world = plugin.getServer().getWorld(rentedRegion.worldName);
                            Region region = plugin.redProtectHook.getRegion(regionName, rentedRegion.worldName);

                            if (region == null)
                                return;
                            region.removeMember(rentedRegion.renter);

                            plugin.removeRentedRegionFromCount(rentedRegion.renter);

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
                                    plugin.getLogger().severe("RentRegion automatic sign creation failed for region " + rentedRegion.regionName);
                                }
                            }
                            File regionFile = new File(plugin.config.signDataLoc + regionName + ".digi");
                            if (regionFile.exists()) {
                                regionFile.delete();
                            }
                            Player player = plugin.getServer().getPlayer(rentedRegion.renter);
                            if ((player != null)) {
                                player.sendMessage(ChatHelper.notice("Expired", regionName));
                            }
                            plugin.logActivity(rentedRegion.renter, " EXPIRED " + rentedRegion.regionName);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

