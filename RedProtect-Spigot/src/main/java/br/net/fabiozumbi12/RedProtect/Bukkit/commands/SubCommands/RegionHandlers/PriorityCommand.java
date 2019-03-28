package br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommands.RegionHandlers;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommand;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPLang;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPUtil.HandleHelpPage;
import static br.net.fabiozumbi12.RedProtect.Bukkit.commands.CommandHandlers.handlePriority;
import static br.net.fabiozumbi12.RedProtect.Bukkit.commands.CommandHandlers.handlePrioritySingle;

public class PriorityCommand implements SubCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            HandleHelpPage(sender, 1);
            return true;
        }

        Player player = (Player) sender;

        int prior;

        if (args.length == 1) {
            try {
                prior = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                RPLang.sendMessage(player, "cmdmanager.region.notnumber");
                return true;
            }
            handlePriority(player, prior);
            return true;
        }

        if (args.length == 2) {
            try {
                prior = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                RPLang.sendMessage(player, "cmdmanager.region.notnumber");
                return true;
            }
            handlePrioritySingle(player, prior, args[0]);
            return true;
        }

        RPLang.sendCommandHelp(sender, "priority", true);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}