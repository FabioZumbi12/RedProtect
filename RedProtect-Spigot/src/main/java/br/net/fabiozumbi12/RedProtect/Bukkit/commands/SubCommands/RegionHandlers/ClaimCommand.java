package br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommands.RegionHandlers;

import br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPUtil;
import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.region.Region;
import br.net.fabiozumbi12.RedProtect.Bukkit.region.RegionBuilder;
import br.net.fabiozumbi12.RedProtect.Bukkit.actions.DefineRegionBuilder;
import br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommand;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPConfig;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPLang;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPUtil.HandleHelpPage;

public class ClaimCommand implements SubCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            HandleHelpPage(sender, 1);
            return true;
        }

        Player player = (Player) sender;
        String claimmode = RPConfig.getWorldClaimType(player.getWorld().getName());

        if (!claimmode.equalsIgnoreCase("WAND") && !claimmode.equalsIgnoreCase("BOTH") && !RedProtect.get().ph.hasCommandPerm(player, "claim")) {
            RPLang.sendMessage(player, "blocklistener.region.blockmode");
            return true;
        }

        if (args.length == 0) {
            String name = RPUtil.nameGen(player.getName(), player.getWorld().getName());
            String leader = player.getUniqueId().toString();
            if (!RedProtect.get().OnlineMode) {
                leader = player.getName().toLowerCase();
            }
            RegionBuilder rb2 = new DefineRegionBuilder(player, RedProtect.get().firstLocationSelections.get(player), RedProtect.get().secondLocationSelections.get(player), name, leader, new HashSet<>(), false);
            if (rb2.ready()) {
                Region r2 = rb2.build();
                RPLang.sendMessage(player, RPLang.get("cmdmanager.region.created") + " " + r2.getName() + ".");
                RedProtect.get().rm.add(r2, player.getWorld());

                RedProtect.get().firstLocationSelections.remove(player);
                RedProtect.get().secondLocationSelections.remove(player);

                RedProtect.get().logger.addLog("(World " + r2.getWorld() + ") Player " + player.getName() + " CLAIMED region " + r2.getName());
            }
            return true;
        }

        if (args.length == 1) {
            String name = args[0].replace("/", "|");
            String leader = player.getUniqueId().toString();
            if (!RedProtect.get().OnlineMode) {
                leader = player.getName().toLowerCase();
            }
            RegionBuilder rb2 = new DefineRegionBuilder(player, RedProtect.get().firstLocationSelections.get(player), RedProtect.get().secondLocationSelections.get(player), name, leader, new HashSet<>(), false);
            if (rb2.ready()) {
                Region r2 = rb2.build();
                RPLang.sendMessage(player, RPLang.get("cmdmanager.region.created") + " " + r2.getName() + ".");
                RedProtect.get().rm.add(r2, player.getWorld());

                RedProtect.get().firstLocationSelections.remove(player);
                RedProtect.get().secondLocationSelections.remove(player);

                RedProtect.get().logger.addLog("(World " + r2.getWorld() + ") Player " + player.getName() + " CLAIMED region " + r2.getName());
            }
            return true;
        }

        if (args.length == 2) {
            String name = args[0].replace("/", "|");
            String leader = player.getUniqueId().toString();
            Set<String> addedAdmins = new HashSet<>();
            addedAdmins.add(RPUtil.PlayerToUUID(args[1]));
            if (!RedProtect.get().OnlineMode) {
                leader = player.getName().toLowerCase();
            }
            RegionBuilder rb2 = new DefineRegionBuilder(player, RedProtect.get().firstLocationSelections.get(player), RedProtect.get().secondLocationSelections.get(player), name, leader, addedAdmins, false);
            if (rb2.ready()) {
                Region r2 = rb2.build();
                RPLang.sendMessage(player, RPLang.get("cmdmanager.region.created") + " " + r2.getName() + ".");
                RedProtect.get().rm.add(r2, player.getWorld());

                RedProtect.get().firstLocationSelections.remove(player);
                RedProtect.get().secondLocationSelections.remove(player);

                RedProtect.get().logger.addLog("(World " + r2.getWorld() + ") Player " + player.getName() + " CLAIMED region " + r2.getName());
            }
            return true;
        }

        RPLang.sendCommandHelp(sender, "claim", true);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}