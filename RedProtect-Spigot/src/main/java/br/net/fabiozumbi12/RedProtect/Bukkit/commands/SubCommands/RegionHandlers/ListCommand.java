package br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommands.RegionHandlers;

import br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPUtil;
import br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommand;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPLang;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static br.net.fabiozumbi12.RedProtect.Bukkit.commands.CommandHandlers.getRegionforList;
import static br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPUtil.HandleHelpPage;
import static br.net.fabiozumbi12.RedProtect.Bukkit.commands.CommandHandlers.handleList;

public class ListCommand implements SubCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            HandleHelpPage(sender, 1);
            return true;
        }

        Player player = (Player) sender;

        //rp list
        if (args.length == 0) {
            handleList(player, RPUtil.PlayerToUUID(player.getName()), 1);
            return true;
        }
        //rp list [player|page]
        if (args.length == 1) {
            try {
                int Page = Integer.parseInt(args[0]);
                getRegionforList(sender, RPUtil.PlayerToUUID(sender.getName()), Page);
                return true;
            } catch (NumberFormatException e) {
                handleList(player, RPUtil.PlayerToUUID(args[0]), 1);
                return true;
            }
        }
        //rp list [player] [page]
        if (args.length == 2) {
            try {
                int Page = Integer.parseInt(args[1]);
                handleList(player, RPUtil.PlayerToUUID(args[0]), Page);
                return true;
            } catch (NumberFormatException e) {
                RPLang.sendMessage(player, "cmdmanager.region.listpage.error");
                return true;
            }
        }

        RPLang.sendCommandHelp(sender, "list", true);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}
