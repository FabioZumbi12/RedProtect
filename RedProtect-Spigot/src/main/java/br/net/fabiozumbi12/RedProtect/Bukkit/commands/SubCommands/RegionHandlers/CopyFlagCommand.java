package br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommands.RegionHandlers;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.region.Region;
import br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommand;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPLang;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

import static br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPUtil.HandleHelpPage;

public class CopyFlagCommand implements SubCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            HandleHelpPage(sender, 1);
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 2) {
            World w = player.getWorld();
            Region from = RedProtect.get().rm.getRegion(args[0], w);
            Region to = RedProtect.get().rm.getRegion(args[1], w);
            if (from == null || !from.isLeader(player)) {
                RPLang.sendMessage(player, RPLang.get("cmdmanager.region.doesntexist") + ": " + args[0]);
                return true;
            }
            if (to == null || !to.isLeader(player)) {
                RPLang.sendMessage(player, RPLang.get("cmdmanager.region.doesntexist") + ": " + args[1]);
                return true;
            }
            for (Map.Entry<String, Object> key : from.getFlags().entrySet()) {
                to.setFlag(sender, key.getKey(), key.getValue());
            }
            RPLang.sendMessage(player, RPLang.get("cmdmanager.region.flag.copied") + args[0] + " > " + args[1]);
            RedProtect.get().logger.addLog("Player " + player.getName() + " Copied FLAGS from " + args[0] + " to " + args[1]);
            return true;
        }

        RPLang.sendCommandHelp(sender, "copyflag", true);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}