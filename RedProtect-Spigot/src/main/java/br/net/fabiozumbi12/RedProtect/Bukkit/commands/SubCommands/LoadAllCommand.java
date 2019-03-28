package br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommands;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommand;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPLang;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPUtil.HandleHelpPage;

public class LoadAllCommand implements SubCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            HandleHelpPage(sender, 1);
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            RedProtect.get().rm.clearDB();
            try {
                RedProtect.get().rm.loadAll();
            } catch (Exception e) {
                RPLang.sendMessage(player, "Error on load all regions from database files:");
                e.printStackTrace();
            }
            RPLang.sendMessage(player, ChatColor.GREEN + "" + RedProtect.get().rm.getAllRegions().size() + " regions has been loaded from database files!");
            return true;
        }

        RPLang.sendCommandHelp(sender, "load-all", true);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}