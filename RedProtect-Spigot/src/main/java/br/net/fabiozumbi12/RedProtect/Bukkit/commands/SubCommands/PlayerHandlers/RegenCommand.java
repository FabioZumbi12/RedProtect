package br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommands.PlayerHandlers;

import br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPUtil;
import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.region.Region;
import br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommand;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPLang;
import br.net.fabiozumbi12.RedProtect.Bukkit.hooks.WEListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPUtil.HandleHelpPage;

public class RegenCommand implements SubCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            HandleHelpPage(sender, 1);
            return true;
        }

        Player player = (Player) sender;

        if (!RedProtect.get().WE) {
            RPLang.sendMessage(player, "cmdmanager.wenotloaded");
            return true;
        }

        if (args.length == 0) {
            Region r = RedProtect.get().rm.getTopRegion(player.getLocation());
            if (r == null) {
                RPLang.sendMessage(player, "cmdmanager.region.doesexists");
                return true;
            }
            WEListener.regenRegion(r, Bukkit.getWorld(r.getWorld()), r.getMaxLocation(), r.getMinLocation(), 0, sender, false);
            return true;
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("undo")) {
                Region r = RedProtect.get().rm.getTopRegion(player.getLocation());
                if (r == null) {
                    RPLang.sendMessage(player, "cmdmanager.region.doesexists");
                    return true;
                }

                if (WEListener.undo(r.getID())) {
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.regen.undo.sucess").replace("{region}", r.getName()));
                } else {
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.regen.undo.none").replace("{region}", r.getName()));
                }
                return true;
            }

            if (args[0].equalsIgnoreCase("stop")) {
                RPUtil.stopRegen = true;
                RPLang.sendMessage(player, "&aRegen will stop now. To continue reload the plugin!");
                return true;
            }
        }

        if (args.length == 2) {
            World w = RedProtect.get().serv.getWorld(args[1]);
            if (w == null) {
                sender.sendMessage(RPLang.get("cmdmanager.region.invalidworld"));
                return true;
            }
            Region r = RedProtect.get().rm.getRegion(args[0], w);
            if (r == null) {
                sender.sendMessage(RPLang.get("correct.usage") + " " + ChatColor.YELLOW + "Invalid region: " + args[0]);
                return true;
            }

            WEListener.regenRegion(r, Bukkit.getWorld(r.getWorld()), r.getMaxLocation(), r.getMinLocation(), 0, sender, false);
            return true;
        }

        RPLang.sendCommandHelp(sender, "regen", true);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}