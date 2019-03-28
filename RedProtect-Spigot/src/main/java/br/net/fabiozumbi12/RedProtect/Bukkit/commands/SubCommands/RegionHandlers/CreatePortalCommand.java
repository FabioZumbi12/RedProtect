package br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommands.RegionHandlers;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.region.Region;
import br.net.fabiozumbi12.RedProtect.Bukkit.region.RegionBuilder;
import br.net.fabiozumbi12.RedProtect.Bukkit.actions.DefineRegionBuilder;
import br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommand;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPConfig;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPLang;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPUtil.HandleHelpPage;

public class CreatePortalCommand implements SubCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            HandleHelpPage(sender, 1);
            return true;
        }

        Player player = (Player) sender;

        //rp createportal <newRegionName> <regionTo> <database>
        if (args.length == 3) {
            World w = RedProtect.get().serv.getWorld(args[2]);
            if (w == null) {
                sender.sendMessage(RPLang.get("cmdmanager.region.invalidworld"));
                return true;
            }
            Region r = RedProtect.get().rm.getRegion(args[1], w);
            if (r == null) {
                RPLang.sendMessage(player, RPLang.get("cmdmanager.createportal.warning").replace("{region}", args[1]));
            }

            String serverName = RPConfig.getString("region-settings.default-leader");
            String name = args[0].replace("/", "|");

            Region r2 = RedProtect.get().rm.getRegion(name, w);

            if (r2 != null) {
                RPLang.sendMessage(player, String.format(RPLang.get("cmdmanager.region.portalcreated"), name, args[1], w.getName()));
                RPLang.sendMessage(player, "cmdmanager.region.portalhint");
                r2.setFlag(sender, "set-portal", args[1] + " " + w.getName());

                RedProtect.get().logger.addLog("(World " + r2.getWorld() + ") Player " + player.getName() + " CREATED A PORTAL " + r2.getName() + " to " + args[1] + " database " + w.getName());
            } else {
                RegionBuilder rb2 = new DefineRegionBuilder(player, RedProtect.get().firstLocationSelections.get(player), RedProtect.get().secondLocationSelections.get(player), name, serverName, new HashSet<>(), true);
                if (rb2.ready()) {
                    r2 = rb2.build();
                    RPLang.sendMessage(player, String.format(RPLang.get("cmdmanager.region.portalcreated"), name, args[1], w.getName()));
                    RPLang.sendMessage(player, "cmdmanager.region.portalhint");

                    r2.setFlag(sender, "set-portal", args[1] + " " + w.getName());
                    RedProtect.get().rm.add(r2, player.getWorld());

                    RedProtect.get().firstLocationSelections.remove(player);
                    RedProtect.get().secondLocationSelections.remove(player);

                    RedProtect.get().logger.addLog("(World " + r2.getWorld() + ") Player " + player.getName() + " CREATED A PORTAL " + r2.getName() + " to " + args[1] + " database " + w.getName());
                }
            }
            return true;
        }

        RPLang.sendCommandHelp(sender, "createportal", true);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}