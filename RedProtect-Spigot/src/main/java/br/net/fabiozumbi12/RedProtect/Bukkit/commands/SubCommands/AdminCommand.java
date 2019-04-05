/*
 *
 * Copyright (c) 2019 - @FabioZumbi12
 * Last Modified: 28/03/19 20:18
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
 *
 */

package br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommands;

import br.net.fabiozumbi12.RedProtect.Bukkit.commands.CommandHandler;
import br.net.fabiozumbi12.RedProtect.Bukkit.fanciful.FancyMessage;
import br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPUtil;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommand;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPConfig;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPLang;
import br.net.fabiozumbi12.RedProtect.Bukkit.hooks.MojangUUIDs;
import br.net.fabiozumbi12.RedProtect.Bukkit.hooks.WEListener;
import me.ellbristow.mychunk.LiteChunk;
import me.ellbristow.mychunk.MyChunkChunk;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static br.net.fabiozumbi12.RedProtect.Bukkit.commands.CommandHandlers.*;

public class AdminCommand implements SubCommand {

    private static boolean handleMyChunk() {
        if (!RedProtect.get().MyChunk) {
            return false;
        }
        Set<LiteChunk> allchunks = new HashSet<>();

        for (World w : RedProtect.get().serv.getWorlds()) {
            Set<LiteChunk> chunks = MyChunkChunk.getChunks(w);
            allchunks.addAll(chunks);
        }

        if (allchunks.size() != 0) {
            int i = 0;
            for (LiteChunk c : allchunks) {
                Set<String> leaders = new HashSet<>();
                String admin = RPUtil.PlayerToUUID(c.getOwner());
                leaders.add(admin);
                World w = RedProtect.get().serv.getWorld(c.getWorldName());
                Chunk chunk = w.getChunkAt(c.getX(), c.getZ());
                int x = chunk.getBlock(7, 50, 7).getX();
                int z = chunk.getBlock(7, 50, 7).getZ();
                String regionName;

                int in = 0;
                while (true) {
                    int is = String.valueOf(in).length();
                    if (RPUtil.UUIDtoPlayer(admin).length() > 13) {
                        regionName = RPUtil.UUIDtoPlayer(admin).substring(0, 14 - is) + "_" + in;
                    } else {
                        regionName = RPUtil.UUIDtoPlayer(admin) + "_" + in;
                    }
                    if (RedProtect.get().rm.getRegion(regionName, w) == null) {
                        break;
                    }
                    ++in;
                }

                Region r = new Region(regionName, new HashSet<>(), new HashSet<>(), new HashSet<>(), new int[]{x + 8, x + 8, x - 7, x - 7}, new int[]{z + 8, z + 8, z - 7, z - 7}, 0, w.getMaxHeight(), 0, c.getWorldName(), RPUtil.DateNow(), RPConfig.getDefFlagsValues(), "", 0, null, true);
                leaders.forEach(r::addLeader);
                MyChunkChunk.unclaim(chunk);
                RedProtect.get().rm.add(r, w);
                RedProtect.get().logger.warning("Region converted and named to " + r.getName());
                i++;
            }
            RedProtect.get().logger.sucess(i + " MyChunk regions converted!");
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player && !RedProtect.get().ph.hasCommandPerm(sender, "admin")) {
            RPLang.sendMessage(sender, "cmdmanager.usefrom.player");
            return true;
        }

        if (args.length == 1) {

            if (args[0].equalsIgnoreCase("list-areas")) {
                sender.sendMessage(RPLang.get("general.color") + "-------------------------------------------------");
                RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.created.area-list"));
                sender.sendMessage("-----");
                for (World w : Bukkit.getWorlds()) {
                    Set<Region> wregions = new HashSet<>();
                    for (Region r : RedProtect.get().rm.getRegionsByWorld(w)) {
                        SimpleDateFormat dateformat = new SimpleDateFormat(RPConfig.getString("region-settings.date-format"));
                        Date now = null;
                        try {
                            now = dateformat.parse(RPUtil.DateNow());
                        } catch (ParseException e1) {
                            RedProtect.get().logger.severe("The 'date-format' don't match with date 'now'!!");
                        }
                        Date regiondate = null;
                        try {
                            regiondate = dateformat.parse(r.getDate());
                        } catch (ParseException e) {
                            RedProtect.get().logger.severe("The 'date-format' don't match with region date!!");
                            e.printStackTrace();
                        }
                        long days = TimeUnit.DAYS.convert(now.getTime() - regiondate.getTime(), TimeUnit.MILLISECONDS);
                        for (String play : RPConfig.getStringList("purge.ignore-regions-from-players")) {
                            if (r.isLeader(RPUtil.PlayerToUUID(play)) || r.isAdmin(RPUtil.PlayerToUUID(play))) {
                                break;
                            }
                        }
                        if (!r.isLeader(RPConfig.getString("region-settings.default-leader")) && days > RPConfig.getInt("purge.remove-oldest") && r.getArea() >= RPConfig.getInt("purge.regen.max-area-regen")) {
                            wregions.add(r);
                        }
                    }
                    if (wregions.size() == 0) {
                        continue;
                    }
                    Iterator<Region> it = wregions.iterator();
                    String colorChar = ChatColor.translateAlternateColorCodes('&', RPConfig.getString("region-settings.database-colors." + w.getName(), "&a"));
                    if (RPConfig.getBool("region-settings.region-list.hover-and-click-teleport") && RedProtect.get().ph.hasRegionPermAdmin(sender, "teleport", null)) {
                        boolean first = true;
                        FancyMessage fancy = new FancyMessage();
                        while (it.hasNext()) {
                            Region r = it.next();
                            String rname = RPLang.get("general.color") + ", " + ChatColor.GRAY + r.getName() + "(" + r.getArea() + ")";
                            if (first) {
                                rname = rname.substring(3);
                                first = false;
                            }
                            if (!it.hasNext()) {
                                rname = rname + RPLang.get("general.color") + ".";
                            }
                            fancy.text(rname).color(ChatColor.DARK_GRAY)
                                    .tooltip(RPLang.get("cmdmanager.list.hover").replace("{region}", r.getName()))
                                    .command("/rp " + getCmd("teleport") + " " + r.getName() + " " + r.getWorld())
                                    .then(" ");
                        }
                        sender.sendMessage(RPLang.get("general.color") + RPLang.get("region.world").replace(":", "") + " " + colorChar + w.getName() + "[" + wregions.size() + "]" + ChatColor.RESET + ": ");
                        fancy.send(sender);
                        sender.sendMessage("-----");
                    } else {
                        String worldregions = "";
                        while (it.hasNext()) {
                            Region r = it.next();
                            worldregions = worldregions + RPLang.get("general.color") + ", " + ChatColor.GRAY + r.getName() + "(" + r.getArea() + ")";
                        }
                        sender.sendMessage(RPLang.get("general.color") + RPLang.get("region.world").replace(":", "") + " " + colorChar + w.getName() + "[" + wregions.size() + "]" + ChatColor.RESET + ": ");
                        sender.sendMessage(worldregions.substring(3) + RPLang.get("general.color") + ".");
                        sender.sendMessage("-----");
                    }
                }
                return true;
            }

            if (args[0].equalsIgnoreCase("clear-kicks")) {
                RedProtect.get().denyEnter.clear();
                RedProtect.get().logger.sucess("All region kicks was clear");
                return true;
            }

            if (args[0].equalsIgnoreCase("single-to-files")) {
                RedProtect.get().logger.sucess("[" + RPUtil.SingleToFiles() + "]" + " regions converted to your own files with success");
                return true;
            }

            if (args[0].equalsIgnoreCase("files-to-single")) {
                RedProtect.get().logger.sucess("[" + RPUtil.FilesToSingle() + "]" + " regions converted to unified database file with success");
                return true;
            }

            if (args[0].equalsIgnoreCase("ymlToMysql")) {
                try {
                    if (!RPUtil.ymlToMysql()) {
                        RedProtect.get().logger.severe("ERROR: Check if your 'file-type' configuration is set to 'yml' before convert from YML to Mysql.");
                        return true;
                    } else {
                        RedProtect.get().getConfig().set("file-type", "mysql");
                        RedProtect.get().saveConfig();
                        RedProtect.get().getServer().getPluginManager().disablePlugin(RedProtect.get());
                        RedProtect.get().getServer().getPluginManager().enablePlugin(RedProtect.get());
                        RedProtect.get().logger.sucess("RedProtect reloaded with Mysql as database! Ready to use!");
                        return true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return true;
                }
            }

            if (args[0].equalsIgnoreCase("mysqlToYml")) {
                try {
                    if (!RPUtil.mysqlToYml()) {
                        RedProtect.get().logger.severe("ERROR: Check if your 'file-type' configuration is set to 'mysql' before convert from MYSQL to Yml.");
                        return true;
                    } else {
                        RedProtect.get().getConfig().set("file-type", "yml");
                        RedProtect.get().saveConfig();
                        RedProtect.get().getServer().getPluginManager().disablePlugin(RedProtect.get());
                        RedProtect.get().getServer().getPluginManager().enablePlugin(RedProtect.get());
                        RedProtect.get().logger.sucess("RedProtect reloaded with Yml as database! Ready to use!");
                        return true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return true;
                }
            }

            if (args[0].equalsIgnoreCase("gpTorp")) {
                if (!RedProtect.get().GP) {
                    RedProtect.get().logger.sucess("The plugin GriefPrevention is not installed or is disabled");
                    return true;
                }
                if (RPUtil.convertFromGP() == 0) {
                    RedProtect.get().logger.severe("No region converted from GriefPrevention.");
                    return true;
                } else {
                    RedProtect.get().rm.saveAll(true);
                    RedProtect.get().logger.info(ChatColor.AQUA + "[" + RPUtil.convertFromGP() + "] regions converted from GriefPrevention with success");
                    RedProtect.get().getServer().getPluginManager().disablePlugin(RedProtect.get());
                    RedProtect.get().getServer().getPluginManager().enablePlugin(RedProtect.get());
                    return true;
                }
            }

            if (args[0].equalsIgnoreCase("list-all")) {
                int total = 0;
                for (Region r : RedProtect.get().rm.getAllRegions()) {
                    RedProtect.get().logger.info(ChatColor.GREEN + "[" + total + "]" + "Region: " + r.getName() + ChatColor.RESET + " | " + ChatColor.AQUA + "World: " + r.getWorld() + ChatColor.RESET);
                    total++;
                }
                RedProtect.get().logger.sucess(total + " regions for " + Bukkit.getWorlds().size() + " worlds.");
                return true;
            }

            if (args[0].equalsIgnoreCase("mychunktorp")) {
                if (handleMyChunk()) {
                    RedProtect.get().rm.saveAll(true);
                    RedProtect.get().getServer().getPluginManager().disablePlugin(RedProtect.get());
                    RedProtect.get().getServer().getPluginManager().enablePlugin(RedProtect.get());
                    RedProtect.get().logger.sucess("...converting MyChunk database");
                    RedProtect.get().logger.sucess("http://dev.bukkit.org/bukkit-plugins/mychunk/");
                    return true;
                } else {
                    RedProtect.get().logger.sucess("The plugin MyChunk is not installed or no regions found");
                    return true;
                }
            }

            if (args[0].equalsIgnoreCase("load-all")) {
                RedProtect.get().rm.clearDB();
                try {
                    RedProtect.get().rm.loadAll();
                } catch (Exception e) {
                    RedProtect.get().logger.severe("Error on load all regions from database files:");
                    e.printStackTrace();
                }
                RedProtect.get().logger.sucess(RedProtect.get().rm.getAllRegions().size() + " regions has been loaded from database files!");
                return true;
            }

            if (checkCmd(args[0], "reload")) {
                RedProtect.get().reload();
                RPLang.sendMessage(sender, "RedProtect Plus reloaded!");
                return true;
            }

            if (args[0].equalsIgnoreCase("reload-config")) {
                RedProtect.get().cmdHandler.unregisterAll();

                RPConfig.init();
                RPLang.init();

                RedProtect.get().logger.info("Re-registering commands...");
                RedProtect.get().cmdHandler = new CommandHandler(RedProtect.get());

                RPLang.sendMessage(sender, "RedProtect configs reloaded!");
                return true;
            }
        }

        if (args.length == 2) {

            if (args[0].equalsIgnoreCase("test-uuid")) {
                try {
                    String name = MojangUUIDs.getUUID(args[1]);
                    RedProtect.get().logger.warning("Leader from: " + args[1]);
                    RedProtect.get().logger.warning("UUID To name: " + name);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }

            //rp removeall <player>
            if (checkCmd(args[0], "removeall")) {
                int removed = RedProtect.get().rm.removeAll(args[1]);
                if (removed <= 0) {
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.noneremoved"));
                } else {
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.removed").replace("{regions}", removed + "").replace("{player}", args[1]));
                }
                return true;
            }

            //rp regenall <player>
            if (checkCmd(args[0], "regenall")) {
                int regen = RedProtect.get().rm.regenAll(args[1]);
                if (regen <= 0) {
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.noneregenerated"));
                } else {
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.regenerated").replace("{regions}", regen + "").replace("{player}", args[1]));
                }
                return true;
            }

            //rp regen stop
            if (checkCmd(args[0], "regenall") && args[1].equalsIgnoreCase("stop")) {
                if (!RedProtect.get().WE) {
                    return true;
                }
                RPUtil.stopRegen = true;
                RPLang.sendMessage(sender, "&aRegen will stop now. To continue reload the plugin!");
                return true;
            }

            if (args[0].equalsIgnoreCase("setconfig") && args[1].equalsIgnoreCase("list")) {
                RPLang.sendMessage(sender, ChatColor.AQUA + "=========== Config Sections: ===========");
                for (String section : RedProtect.get().getConfig().getValues(false).keySet()) {
                    if (section.contains("debug-messages") ||
                            section.contains("file-type") ||
                            section.contains("language")) {
                        sender.sendMessage(ChatColor.GOLD + section + " : " + ChatColor.GREEN + RedProtect.get().getConfig().get(section).toString());
                    }
                }
                sender.sendMessage(ChatColor.AQUA + "====================================");
                return true;
            }

            //rp clamilimit player
            if (checkCmd(args[0], "claimlimit")) {
                Player offp = RedProtect.get().serv.getOfflinePlayer(args[1]).getPlayer();
                if (offp == null) {
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.noplayer.thisname").replace("{player}", args[1]));
                    return true;
                }
                int limit = RedProtect.get().ph.getPlayerClaimLimit(offp);
                if (limit < 0 || RedProtect.get().ph.hasPerm(offp, "redprotect.limits.claim.unlimited")) {
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.nolimit"));
                    return true;
                }

                int currentUsed = RedProtect.get().rm.getRegions(RPUtil.PlayerToUUID(offp.getName()), offp.getWorld()).size();
                ChatColor color = currentUsed >= limit ? ChatColor.RED : ChatColor.GOLD;
                RPLang.sendMessage(sender, RPLang.get("cmdmanager.yourclaims") + color + currentUsed + RPLang.get("general.color") + "/" + color + limit + RPLang.get("general.color"));
                return true;
            }

            //rp limit player
            if (checkCmd(args[0], "blocklimit")) {
                Player offp = RedProtect.get().serv.getOfflinePlayer(args[1]).getPlayer();
                if (offp == null) {
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.noplayer.thisname").replace("{player}", args[1]));
                    return true;
                }
                int limit = RedProtect.get().ph.getPlayerBlockLimit(offp);
                if (limit < 0 || RedProtect.get().ph.hasPerm(offp, "redprotect.limits.blocks.unlimited")) {
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.nolimit"));
                    return true;
                }

                int currentUsed = RedProtect.get().rm.getTotalRegionSize(RPUtil.PlayerToUUID(offp.getName()), offp.getWorld().getName());
                ChatColor color = currentUsed >= limit ? ChatColor.RED : ChatColor.GOLD;
                RPLang.sendMessage(sender, RPLang.get("cmdmanager.yourarea") + color + currentUsed + RPLang.get("general.color") + "/" + color + limit + RPLang.get("general.color"));
                return true;
            }
        }

        if (args.length == 3) {

            //rp regen <region> <database>
            if (checkCmd(args[0], "regen")) {
                if (!RedProtect.get().WE) {
                    return true;
                }
                World w = RedProtect.get().serv.getWorld(args[2]);
                if (w == null) {
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.invalidworld"));
                    return true;
                }
                Region r = RedProtect.get().rm.getRegion(args[1], w);
                if (r == null) {
                    RPLang.sendMessage(sender, RPLang.get("correct.usage") + " " + ChatColor.YELLOW + "Invalid region: " + args[1]);
                    return true;
                }

                WEListener.regenRegion(r, Bukkit.getWorld(r.getWorld()), r.getMaxLocation(), r.getMinLocation(), 0, sender, false);
                return true;
            }

            //rp undo <region> <database>
            if (args[0].equalsIgnoreCase("undo")) {
                if (!RedProtect.get().WE) {
                    return true;
                }
                World w = RedProtect.get().serv.getWorld(args[2]);
                if (w == null) {
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.invalidworld"));
                    return true;
                }
                Region r = RedProtect.get().rm.getRegion(args[1], w);
                if (r == null) {
                    RPLang.sendMessage(sender, RPLang.get("correct.usage") + " " + ChatColor.YELLOW + "Invalid region: " + args[1]);
                    return true;
                }

                if (WEListener.undo(r.getID())) {
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.regen.undo.sucess").replace("{region}", r.getName()));
                } else {
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.regen.undo.none").replace("{region}", r.getName()));
                }
                return true;
            }

            //rp clamilimit player database
            if (checkCmd(args[0], "claimlimit")) {
                Player offp = RedProtect.get().serv.getOfflinePlayer(args[1]).getPlayer();
                World w = RedProtect.get().serv.getWorld(args[2]);
                if (offp == null) {
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.noplayer.thisname").replace("{player}", args[1]));
                    return true;
                }
                int limit = RedProtect.get().ph.getPlayerClaimLimit(offp);
                if (limit < 0 || RedProtect.get().ph.hasPerm(offp, "redprotect.limits.claim.unlimited")) {
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.nolimit"));
                    return true;
                }

                if (w == null) {
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.invalidworld"));
                    return true;
                }

                int currentUsed = RedProtect.get().rm.getRegions(RPUtil.PlayerToUUID(offp.getName()), w).size();
                ChatColor color = currentUsed >= limit ? ChatColor.RED : ChatColor.GOLD;
                RPLang.sendMessage(sender, RPLang.get("cmdmanager.yourclaims") + color + currentUsed + RPLang.get("general.color") + "/" + color + limit + RPLang.get("general.color"));
                return true;
            }

            if (args[0].equalsIgnoreCase("setconfig")) {
                if (args[1].equals("debug-messages") ||
                        args[1].equals("file-type") ||
                        args[1].equals("language")) {
                    Object from = RedProtect.get().getConfig().get(args[1]);
                    if (args[2].equals("true") || args[2].equals("false")) {
                        RedProtect.get().getConfig().set(args[1], Boolean.parseBoolean(args[2]));
                    } else {
                        try {
                            int value = Integer.parseInt(args[2]);
                            RedProtect.get().getConfig().set(args[1], value);
                        } catch (NumberFormatException ex) {
                            RedProtect.get().getConfig().set(args[1], args[2]);
                        }
                    }
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.configset") + " " + from.toString() + " > " + args[2]);
                    RPConfig.save();
                    return true;
                } else {
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.confignotset") + " " + args[1]);
                    return true;
                }
            }

            //rp info <region> <database>
            if (checkCmd(args[0], "info")) {
                if (Bukkit.getWorld(args[2]) != null) {
                    Region r = RedProtect.get().rm.getRegion(args[1], Bukkit.getWorld(args[2]));
                    if (r != null) {
                        sender.sendMessage(RPLang.get("general.color") + "-----------------------------------------");
                        sender.sendMessage(r.info());
                        sender.sendMessage(RPLang.get("general.color") + "-----------------------------------------");
                    } else {
                        RPLang.sendMessage(sender, RPLang.get("correct.usage") + " " + ChatColor.YELLOW + "Invalid region: " + args[1]);
                    }
                } else {
                    RPLang.sendMessage(sender, RPLang.get("correct.usage") + " " + ChatColor.YELLOW + "Invalid World: " + args[2]);
                }
                return true;
            }
        }

        if (args.length == 4) {

            //rp addmember <player> <region> <database>
            if (checkCmd(args[0], "addmember")) {
                World w = RedProtect.get().serv.getWorld(args[3]);
                if (w == null) {
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.invalidworld"));
                    return true;
                }
                Region r = RedProtect.get().rm.getRegion(args[2], w);
                if (r == null) {
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.doesntexist") + ": " + args[2]);
                    return true;
                }
                handleAddMember(sender, args[1], r);
                return true;
            }

            //rp addadmin <player> <region> <database>
            if (checkCmd(args[0], "addadmin")) {
                World w = RedProtect.get().serv.getWorld(args[3]);
                if (w == null) {
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.invalidworld"));
                    return true;
                }
                Region r = RedProtect.get().rm.getRegion(args[2], w);
                if (r == null) {
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.doesntexist") + ": " + args[2]);
                    return true;
                }
                handleAddAdmin(sender, args[1], r);
                return true;
            }

            //rp addleader <player> <region> <database>
            if (checkCmd(args[0], "addleader")) {
                World w = RedProtect.get().serv.getWorld(args[3]);
                if (w == null) {
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.invalidworld"));
                    return true;
                }
                Region r = RedProtect.get().rm.getRegion(args[2], w);
                if (r == null) {
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.doesntexist") + ": " + args[2]);
                    return true;
                }
                handleAddLeader(sender, args[1], r);
                return true;
            }

            //rp removemember <player> <region> <database>
            if (checkCmd(args[0], "removemember")) {
                World w = RedProtect.get().serv.getWorld(args[3]);
                if (w == null) {
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.invalidworld"));
                    return true;
                }
                Region r = RedProtect.get().rm.getRegion(args[2], w);
                if (r == null) {
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.doesntexist") + ": " + args[2]);
                    return true;
                }
                handleRemoveMember(sender, args[1], r);
                return true;
            }

            //rp removeadmin <player> <region> <database>
            if (checkCmd(args[0], "removeadmin")) {
                World w = RedProtect.get().serv.getWorld(args[3]);
                if (w == null) {
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.invalidworld"));
                    return true;
                }
                Region r = RedProtect.get().rm.getRegion(args[2], w);
                if (r == null) {
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.doesntexist") + ": " + args[2]);
                    return true;
                }
                handleRemoveAdmin(sender, args[1], r);
                return true;
            }

            //rp removeleader <player> <region> <database>
            if (checkCmd(args[0], "removeleader")) {
                World w = RedProtect.get().serv.getWorld(args[3]);
                if (w == null) {
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.invalidworld"));
                    return true;
                }
                Region r = RedProtect.get().rm.getRegion(args[2], w);
                if (r == null) {
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.doesntexist") + ": " + args[2]);
                    return true;
                }
                handleRemoveLeader(sender, args[1], r);
                return true;
            }

            //rp kick <player> [region] [database]
            if (checkCmd(args[0], "kick")) {
                World w = RedProtect.get().serv.getWorld(args[3]);
                if (w == null) {
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.invalidworld"));
                    return true;
                }
                Region r = RedProtect.get().rm.getRegion(args[2], w);
                if (r == null) {
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.doesntexist") + ": " + args[2]);
                    return true;
                }

                Player visit = Bukkit.getPlayer(args[1]);
                if (visit == null) {
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.noplayer.online"));
                    return true;
                }

                if (r.canBuild(visit)) {
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.cantkick.member"));
                    return true;
                }
                Region rv = RedProtect.get().rm.getTopRegion(visit.getLocation());
                if (rv == null || !rv.getID().equals(r.getID())) {
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.notonregion"));
                    return true;
                }

                RPUtil.DenyEnterPlayer(visit.getWorld(), visit.getLocation(), visit.getLocation(), r, true);

                String sec = String.valueOf(RPConfig.getInt("region-settings.delay-after-kick-region"));
                if (RedProtect.get().denyEnterRegion(r.getID(), visit.getName())) {
                    RPUtil.DenyEnterPlayer(visit.getWorld(), visit.getLocation(), visit.getLocation(), r, true);
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.kicked").replace("{player}", visit.getName()).replace("{region}", r.getName()).replace("{time", sec));
                } else {
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.already.cantenter").replace("{time}", sec));
                }
                return true;
            }

            if (checkCmd(args[0], "teleport")) {
                //rp teleport <player> <region> <database>
                Player play = RedProtect.get().serv.getPlayer(args[1]);
                if (play != null) {
                    World w = RedProtect.get().serv.getWorld(args[3]);
                    if (w == null) {
                        RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.invalidworld"));
                        return true;
                    }
                    Region region = RedProtect.get().rm.getRegion(args[2], w);
                    if (region == null) {
                        RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.doesntexist") + ": " + args[2]);
                        return true;
                    }

                    Location loc = null;
                    if (region.getTPPoint() != null) {
                        loc = region.getTPPoint();
                        loc.setX(loc.getBlockX() + 0.500);
                        loc.setZ(loc.getBlockZ() + 0.500);
                    } else {
                        int limit = w.getMaxHeight();
                        if (w.getEnvironment().equals(World.Environment.NETHER)) {
                            limit = 124;
                        }
                        for (int i = limit; i > 0; i--) {
                            Material mat = w.getBlockAt(region.getCenterX(), i, region.getCenterZ()).getType();
                            Material mat1 = w.getBlockAt(region.getCenterX(), i + 1, region.getCenterZ()).getType();
                            Material mat2 = w.getBlockAt(region.getCenterX(), i + 2, region.getCenterZ()).getType();
                            if (!mat.name().contains("LAVA") && !mat.equals(Material.AIR) && mat1.equals(Material.AIR) && mat2.equals(Material.AIR)) {
                                loc = new Location(w, region.getCenterX() + 0.500, i + 1, region.getCenterZ() + 0.500);
                                break;
                            }
                        }
                    }

                    if (RedProtect.get().Ess) {
                        RedProtect.get().pless.getUser(play).setLastLocation();
                    }
                    play.teleport(loc);
                    RPLang.sendMessage(play, RPLang.get("cmdmanager.region.teleport") + " " + args[2]);
                    sender.sendMessage(ChatColor.AQUA + "Player " + play.getName() + " teleported to " + args[2]);
                    return true;
                } else {
                    sender.sendMessage(RPLang.get("cmdmanager.noplayer.thisname").replace("{player}", args[1]));
                    HandleHelpPage(sender, 1);
                    return true;
                }
            }

            //rp flag info <region> <database>
            if (checkCmd(args[0], "flag") && checkCmd(args[1], "info")) {
                if (Bukkit.getWorld(args[3]) != null) {
                    Region r = RedProtect.get().rm.getRegion(args[2], Bukkit.getWorld(args[3]));
                    if (r != null) {
                        sender.sendMessage(RPLang.get("general.color") + "------------[" + RPLang.get("cmdmanager.region.flag.values") + "]------------");
                        sender.sendMessage(r.getFlagInfo());
                        sender.sendMessage(RPLang.get("general.color") + "------------------------------------");
                    } else {
                        RPLang.sendMessage(sender, RPLang.get("correct.usage") + " " + ChatColor.YELLOW + "Invalid region: " + args[2]);
                    }
                } else {
                    RPLang.sendMessage(sender, RPLang.get("correct.usage") + " " + ChatColor.YELLOW + "Invalid World: " + args[3]);
                }
                return true;
            }
        }

        if (args.length == 5) {
            /*/rp flag <regionName> <flag> <value> <database>*/
            if (checkCmd(args[0], "flag")) {
                World w = RedProtect.get().serv.getWorld(args[4]);
                if (w == null) {
                    RPLang.sendMessage(sender, RPLang.get("correct.usage") + ChatColor.YELLOW + " rp " + getCmd("flag") + " <regionName> <flag> <value> <database>");
                    return true;
                }
                Region r = RedProtect.get().rm.getRegion(args[1], w);
                if (r != null && (RPConfig.getDefFlags().contains(args[2]) || RPConfig.AdminFlags.contains(args[2]))) {
                    Object objflag = RPUtil.parseObject(args[3]);
                    r.setFlag(sender, args[2], objflag);
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.flag.set").replace("{flag}", "'" + args[2] + "'") + " " + r.getFlagString(args[2]));
                    RedProtect.get().logger.addLog("Console changed flag " + args[2] + " to " + r.getFlagString(args[2]));
                    return true;
                }
            }
        }

        //rp list <player> [page]
        if (args.length >= 2 && checkCmd(args[0], "list")) {
            //rp list [player]
            if (args.length == 2) {
                getRegionforList(sender, RPUtil.PlayerToUUID(args[1]), 1);
                return true;
            }
            //rp list [player] [page]
            if (args.length == 3) {
                try {
                    int Page = Integer.parseInt(args[2]);
                    getRegionforList(sender, RPUtil.PlayerToUUID(args[1]), Page);
                    return true;
                } catch (NumberFormatException e) {
                    RPLang.sendMessage(sender, "cmdmanager.region.listpage.error");
                    return true;
                }
            }
        }

        if (args[0].equalsIgnoreCase("save-all")) {
            RedProtect.get().logger.SaveLogs();
            RedProtect.get().logger.sucess(RedProtect.get().rm.saveAll(args.length == 2 && args[1].equalsIgnoreCase("-f")) + " regions saved with success!");
            return true;
        }

        HandleHelpPage(sender, 1);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> consolecmds = Arrays.asList("list-areas", "clear-kicks", "kick", "files-to-single", "single-to-files", "flag", "list", "teleport", "ymltomysql", "mysqltoyml", "setconfig", "reload", "reload-config", "save-all", "load-all", "blocklimit", "claimlimit", "list-all");

        if (args.length == 0) {
            return consolecmds;
        }

        if (args.length == 1){
            SortedSet<String> tab = new TreeSet<>();
           for (String cmd : consolecmds) {
                if (cmd.startsWith(args[0])) {
                    tab.add(cmd);
                }
            }
            return new ArrayList<>(tab);
        }
        return null;
    }
}
