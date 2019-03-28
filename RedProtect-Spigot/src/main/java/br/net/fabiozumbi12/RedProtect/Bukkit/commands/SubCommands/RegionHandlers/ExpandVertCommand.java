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

public class ExpandVertCommand implements SubCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            HandleHelpPage(sender, 1);
            return true;
        }

        Player player = (Player) sender;

        Region r;
        switch (args.length) {
            case 0:
                r = RedProtect.get().rm.getTopRegion(player.getLocation());
                if (r == null) {
                    RPLang.sendMessage(player, "cmdmanager.region.todo.that");
                    return true;
                }
                break;
            //rp expand-vert [region]
            case 1:
                r = RedProtect.get().rm.getRegion(args[0], player.getWorld());
                if (r == null) {
                    RPLang.sendMessage(player, RPLang.get("cmdmanager.region.doesntexist") + ": " + args[0]);
                    return true;
                }
                break;
            //rp expand-vert [region] [database]
            case 2:
                if (Bukkit.getWorld(args[1]) == null) {
                    RPLang.sendMessage(player, "cmdmanager.region.invalidworld");
                    return true;
                }
                r = RedProtect.get().rm.getRegion(args[1], Bukkit.getWorld(args[1]));
                if (r == null) {
                    RPLang.sendMessage(player, RPLang.get("cmdmanager.region.doesntexist") + ": " + args[0]);
                    return true;
                }
                break;
            default:
                RPLang.sendCommandHelp(sender, "expand-vert", true);
                return true;
        }

        r.setMaxY(player.getWorld().getMaxHeight());
        r.setMinY(0);
        RPLang.sendMessage(player, RPLang.get("cmdmanager.region.expandvert.success").replace("{region}", r.getName()).replace("{miny}", String.valueOf(r.getMinY())).replace("{maxy}", String.valueOf(r.getMaxY())));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}