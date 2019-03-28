package br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommands.RegionHandlers;

import br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPUtil;
import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommand;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPConfig;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPLang;
import br.net.fabiozumbi12.RedProtect.Bukkit.hooks.WEListener;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPUtil.HandleHelpPage;

public class Pos1Command implements SubCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            HandleHelpPage(sender, 1);
            return true;
        }

        Player player = (Player) sender;

        String claimmode = RPConfig.getWorldClaimType(player.getWorld().getName());
        if (!claimmode.equalsIgnoreCase("WAND") && !claimmode.equalsIgnoreCase("BOTH") && !RedProtect.get().ph.hasCommandPerm(player, "redefine")) {
            return true;
        }

        if (args.length == 0) {
            Location pl = player.getLocation();
            RedProtect.get().firstLocationSelections.put(player, pl);
            player.sendMessage(RPLang.get("playerlistener.wand1") + RPLang.get("general.color") + " (" + ChatColor.GOLD + pl.getBlockX() + RPLang.get("general.color") + ", " + ChatColor.GOLD + pl.getBlockY() + RPLang.get("general.color") + ", " + ChatColor.GOLD + pl.getBlockZ() + RPLang.get("general.color") + ").");

            //show preview border
            if (RedProtect.get().firstLocationSelections.containsKey(player) && RedProtect.get().secondLocationSelections.containsKey(player)) {
                Location loc1 = RedProtect.get().firstLocationSelections.get(player);
                Location loc2 = RedProtect.get().secondLocationSelections.get(player);
                if (RedProtect.get().WE && RPConfig.getBool("hooks.useWECUI")) {
                    WEListener.setSelectionRP(player, loc1, loc2);
                }

                if (loc1.getWorld().equals(loc2.getWorld()) && loc1.distanceSquared(loc2) > RPConfig.getInt("region-settings.define-max-distance")) {
                    Double dist = loc1.distanceSquared(loc2);
                    RPLang.sendMessage(player, String.format(RPLang.get("regionbuilder.selection.maxdefine"), RPConfig.getInt("region-settings.define-max-distance"), dist.intValue()));
                } else {
                    RPUtil.addBorder(player, RPUtil.get4Points(loc1, loc2, player.getLocation().getBlockY()));
                }
            }
            return true;
        }

        RPLang.sendCommandHelp(sender, "pos1", true);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}
