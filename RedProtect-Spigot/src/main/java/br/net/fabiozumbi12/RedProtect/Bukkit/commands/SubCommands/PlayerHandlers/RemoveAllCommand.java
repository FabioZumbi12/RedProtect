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

public class RemoveAllCommand implements SubCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            HandleHelpPage(sender, 1);
            return true;
        }

        if (args.length == 1) {
            if (!RedProtect.get().WE) {
                return true;
            }
            int removed = RedProtect.get().rm.removeAll(args[0]);
            if (removed <= 0) {
                RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.noneremoved"));
            } else {
                RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.removed").replace("{regions}", removed + "").replace("{player}", args[1]));
            }
            return true;
        }

        RPLang.sendCommandHelp(sender, "remove-all", true);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}


