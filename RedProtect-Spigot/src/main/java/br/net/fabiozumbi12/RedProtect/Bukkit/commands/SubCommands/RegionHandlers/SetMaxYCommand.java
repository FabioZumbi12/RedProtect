package br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommands.RegionHandlers;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.region.Region;
import br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommand;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPLang;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPUtil.HandleHelpPage;

public class SetMaxYCommand implements SubCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            HandleHelpPage(sender, 1);
            return true;
        }

        Player player = (Player) sender;

        Region r;
        //rp setmaxy <size>
        switch (args.length) {
            case 1:
                r = RedProtect.get().rm.getTopRegion(player.getLocation());
                if (r == null) {
                    RPLang.sendMessage(player, "cmdmanager.region.todo.that");
                    return true;
                }
                break;
            //rp setmaxy <size> [region]
            case 2:
                r = RedProtect.get().rm.getRegion(args[1], player.getWorld());
                if (r == null) {
                    RPLang.sendMessage(player, RPLang.get("cmdmanager.region.doesntexist") + ": " + args[1]);
                    return true;
                }
                break;
            //rp setmaxy <size> [region] [database]
            case 3:
                if (Bukkit.getWorld(args[2]) == null) {
                    RPLang.sendMessage(player, "cmdmanager.region.invalidworld");
                    return true;
                }
                r = RedProtect.get().rm.getRegion(args[1], Bukkit.getWorld(args[2]));
                if (r == null) {
                    RPLang.sendMessage(player, RPLang.get("cmdmanager.region.doesntexist") + ": " + args[1]);
                    return true;
                }
                break;
            default:
                RPLang.sendCommandHelp(sender, "setmaxy", true);
                return true;
        }

        String from = String.valueOf(r.getMaxY());

        try {
            int size = Integer.parseInt(args[1]);
            if ((size - r.getMinY()) <= 1) {
                RPLang.sendMessage(player, "cmdmanager.region.ysiszesmatch");
                return true;
            }
            r.setMaxY(size);
            RPLang.sendMessage(player, RPLang.get("cmdmanager.region.setmaxy.success").replace("{region}", r.getName()).replace("{fromsize}", from).replace("{size}", String.valueOf(size)));
            RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + player.getName() + " SETMAXY of region " + r.getName() + " to " + args[1]);
            return true;
        } catch (NumberFormatException e) {
            RPLang.sendMessage(player, "cmdmanager.region.invalid.number");
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}