package br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommands.PlayerHandlers;

import br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommand;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPLang;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPUtil.HandleHelpPage;

public class TutorialCommand implements SubCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            HandleHelpPage(sender, 1);
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            RPLang.sendMessage(player, "cmdmanager.tutorial");
            RPLang.sendMessage(player, "cmdmanager.tutorial1");
            RPLang.sendMessage(player, "cmdmanager.tutorial2");
            RPLang.sendMessage(player, "cmdmanager.tutorial3");
            RPLang.sendMessage(player, "cmdmanager.tutorial4");
            RPLang.sendMessage(player, "cmdmanager.tutorial5");
            RPLang.sendMessage(player, "cmdmanager.tutorial6");
            return true;
        }

        RPLang.sendCommandHelp(sender, "tutorial", true);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}