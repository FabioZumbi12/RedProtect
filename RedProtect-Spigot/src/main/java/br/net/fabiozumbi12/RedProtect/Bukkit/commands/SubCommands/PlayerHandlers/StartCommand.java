package br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommands.PlayerHandlers;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.region.Region;
import br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommand;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPConfig;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPLang;
import br.net.fabiozumbi12.RedProtect.Bukkit.schematics.RPSchematics;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPUtil.HandleHelpPage;
import static br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPUtil.getCmd;

public class StartCommand implements SubCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            HandleHelpPage(sender, 1);
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            if (!RPConfig.isAllowedWorld(player)) {
                RPLang.sendMessage(player, "regionbuilder.region.worldnotallowed");
                return true;
            }

            Region r = RedProtect.get().rm.getTopRegion(player.getLocation());
            if (r != null && r.isMember(player)) {
                RPLang.sendMessage(player, "playerlistener.region.claimlimit.start");
                return true;
            }

            RedProtect.get().confiemStart.add(player.getName());
            RPLang.sendMessage(player, RPLang.get("cmdmanager.region.confirm").replace("{cmd}", getCmd("start")));

            Bukkit.getScheduler().runTaskLater(RedProtect.get(), () -> RedProtect.get().confiemStart.remove(player.getName()), 600);
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("ok")) {
            if (!RedProtect.get().confiemStart.contains(player.getName())) {
                player.sendMessage(RPLang.get("cmdmanager.region.noconfirm").replace("{cmd}", getCmd("start")));
                return true;
            }
            RPSchematics.pasteSchematic(player);
            return true;
        }

        RPLang.sendCommandHelp(sender, "start", true);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}