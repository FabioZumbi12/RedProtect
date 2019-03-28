package br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommands.RegionHandlers;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommand;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPLang;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPUtil.*;
import static br.net.fabiozumbi12.RedProtect.Bukkit.commands.CommandHandlers.handletp;

public class TeleportCommand implements SubCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            HandleHelpPage(sender, 1);
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            RPLang.sendMessage(player, RPLang.get("cmdmanager.help.teleport").replace("{cmd}", getCmd("teleport")).replace("{alias}", getCmdAlias("teleport")));
            return true;
        }

        if (args.length == 1) {
            handletp(player, args[0], player.getWorld().getName(), null);
            return true;
        }

        if (args.length == 2) {
            handletp(player, args[0], args[1], null);
            return true;
        }

        if (args.length == 3) {
            // /rp teleport <player> <region> <database>
            Player play = RedProtect.get().serv.getPlayer(args[0]);
            if (play != null) {
                handletp(player, args[1], args[2], play);
                return true;
            } else {
                RPLang.sendMessage(player, RPLang.get("cmdmanager.noplayer.thisname").replace("{player}", args[1]));
                RPLang.sendMessage(player, RPLang.get("cmdmanager.help.teleport").replace("{cmd}", getCmd("teleport")).replace("{alias}", getCmdAlias("teleport")));
                return true;
            }
        }

        RPLang.sendCommandHelp(sender, "teleport", true);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}