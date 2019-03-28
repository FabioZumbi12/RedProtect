package br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommands.RegionHandlers;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.region.Region;
import br.net.fabiozumbi12.RedProtect.Bukkit.actions.RedefineRegionBuilder;
import br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommand;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPLang;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPUtil.HandleHelpPage;

public class RedefineCommand implements SubCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            HandleHelpPage(sender, 1);
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 1) {
            Region oldRect = RedProtect.get().rm.getRegion(args[0], player.getWorld());
            if (oldRect == null) {
                RPLang.sendMessage(player, RPLang.get("cmdmanager.region.doesntexist") + ": " + args[0]);
                return true;
            }

            if (!RedProtect.get().ph.hasRegionPermLeader(player, "redefine", oldRect)) {
                RPLang.sendMessage(player, "no.permission");
                return true;
            }

            RedefineRegionBuilder rb = new RedefineRegionBuilder(player, oldRect, RedProtect.get().firstLocationSelections.get(player), RedProtect.get().secondLocationSelections.get(player));
            if (rb.ready()) {
                Region r2 = rb.build();
                RPLang.sendMessage(player, RPLang.get("cmdmanager.region.redefined") + " " + r2.getName() + ".");
                RedProtect.get().rm.add(r2, player.getWorld());

                RedProtect.get().firstLocationSelections.remove(player);
                RedProtect.get().secondLocationSelections.remove(player);

                RedProtect.get().logger.addLog("(World " + r2.getWorld() + ") Player " + player.getName() + " REDEFINED region " + r2.getName());
            }
            return true;
        }

        RPLang.sendCommandHelp(sender, "redefine", true);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}
