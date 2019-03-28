package br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommands.PlayerHandlers;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.region.Region;
import br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommand;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPLang;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPUtil.HandleHelpPage;

public class NearCommand implements SubCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            HandleHelpPage(sender, 1);
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            Set<Region> regions = RedProtect.get().rm.getRegionsNear(player, 60);
            if (regions.size() == 0) {
                RPLang.sendMessage(player, "cmdmanager.noregions.nearby");
            } else {
                Iterator<Region> i = regions.iterator();
                RPLang.sendMessage(player, RPLang.get("general.color") + "------------------------------------");
                RPLang.sendMessage(player, RPLang.get("cmdmanager.region.near"));
                RPLang.sendMessage(player, RPLang.get("general.color") + "------------------------------------");
                while (i.hasNext()) {
                    Region r = i.next();
                    player.sendMessage(RPLang.get("cmdmanager.region.name") + r.getName() + RPLang.get("general.color") + ChatColor.translateAlternateColorCodes('&', " | Center (&6X,Z" + RPLang.get("general.color") + "): &6") + r.getCenterX() + ", " + r.getCenterZ());
                }
                RPLang.sendMessage(player, RPLang.get("general.color") + "------------------------------------");
            }
        }

        RPLang.sendCommandHelp(sender, "near", true);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}