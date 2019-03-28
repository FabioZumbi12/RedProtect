package br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommands;

import br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPUtil;
import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.region.Region;
import br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommand;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPConfig;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPLang;
import br.net.fabiozumbi12.RedProtect.Bukkit.fanciful.FancyMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPUtil.HandleHelpPage;
import static br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPUtil.getCmd;

public class ListAreasCommand implements SubCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender || !sender.hasPermission("redprotect.command.list-areas")) {
            HandleHelpPage(sender, 1);
            return true;
        }

        if (args.length == 0) {
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

        RPLang.sendCommandHelp(sender, "list-areas", true);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}
