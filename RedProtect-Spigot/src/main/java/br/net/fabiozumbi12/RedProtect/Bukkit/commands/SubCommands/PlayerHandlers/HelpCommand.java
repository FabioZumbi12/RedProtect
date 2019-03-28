package br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommands.PlayerHandlers;

import br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommand;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPLang;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.util.List;

import static br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPUtil.HandleHelpPage;

public class HelpCommand implements SubCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            HandleHelpPage(sender, 1);
            return true;
        }

        if (args.length == 0) {
            HandleHelpPage(sender, 1);
            return true;
        }

        if (args.length == 1) {
            try {
                int page = Integer.parseInt(args[0]);
                HandleHelpPage(sender, page);
                return true;
            } catch (NumberFormatException ignored) {
            }
        }

        RPLang.sendCommandHelp(sender, "help", true);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}