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

import static br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPUtil.HandleHelpPage;

public class DefineCommand implements SubCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            HandleHelpPage(sender, 1);
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            String serverName = RPConfig.getString("region-settings.default-leader");
            String name = RPUtil.nameGen(serverName, player.getWorld().getName());

            RegionBuilder rb2 = new DefineRegionBuilder(player, RedProtect.get().firstLocationSelections.get(player), RedProtect.get().secondLocationSelections.get(player), name, serverName, new HashSet<>(), true);
            if (rb2.ready()) {
                Region r2 = rb2.build();
                RPLang.sendMessage(player, RPLang.get("cmdmanager.region.created") + " " + r2.getName() + ".");
                RedProtect.get().rm.add(r2, player.getWorld());

                RedProtect.get().firstLocationSelections.remove(player);
                RedProtect.get().secondLocationSelections.remove(player);

                RedProtect.get().logger.addLog("(World " + r2.getWorld() + ") Player " + player.getName() + " DEFINED region " + r2.getName());
            }
            return true;
        }

        if (args.length == 1) {
            String serverName = RPConfig.getString("region-settings.default-leader");
            String name = args[0].replace("/", "|");

            RegionBuilder rb2 = new DefineRegionBuilder(player, RedProtect.get().firstLocationSelections.get(player), RedProtect.get().secondLocationSelections.get(player), name, serverName, new HashSet<>(), true);
            if (rb2.ready()) {
                Region r2 = rb2.build();
                RPLang.sendMessage(player, RPLang.get("cmdmanager.region.created") + " " + r2.getName() + ".");
                RedProtect.get().rm.add(r2, player.getWorld());

                RedProtect.get().firstLocationSelections.remove(player);
                RedProtect.get().secondLocationSelections.remove(player);

                RedProtect.get().logger.addLog("(World " + r2.getWorld() + ") Player " + player.getName() + " DEFINED region " + r2.getName());
            }
            return true;
        }

        RPLang.sendCommandHelp(sender, "define", true);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}
