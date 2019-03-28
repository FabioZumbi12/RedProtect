package br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommands.PlayerHandlers;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommand;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPLang;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPUtil.HandleHelpPage;
import static br.net.fabiozumbi12.RedProtect.Bukkit.commands.CommandHandlers.handleInfo;
import static br.net.fabiozumbi12.RedProtect.Bukkit.commands.CommandHandlers.handleInfoTop;

public class InfoCommand implements SubCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            HandleHelpPage(sender, 1);
            return true;
        }

        Player player = (Player) sender;

        //rp info [region] [database]
        if (args.length == 0) {
            handleInfoTop(player);
            return true;
        }
        if (args.length == 1) {
            handleInfo(player, args[0], "");
            return true;
        }
        if (args.length == 2) {
            handleInfo(player, args[0], args[1]);
            return true;
        }

        RPLang.sendCommandHelp(sender, "info", true);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}