package br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommands.RegionHandlers;

import br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPEconomy;
import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.region.Region;
import br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommand;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPConfig;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPLang;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPUtil.HandleHelpPage;

public class ValueCommand implements SubCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            HandleHelpPage(sender, 1);
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            Region r = RedProtect.get().rm.getTopRegion(player.getLocation());
            if (r != null) {
                if (RedProtect.get().ph.hasRegionPermLeader(player, "value", r)) {
                    if (r.getArea() <= RPConfig.getEcoInt("max-area-toget-value")) {
                        r.setValue(RPEconomy.getRegionValue(r));
                        RPLang.sendMessage(player, RPLang.get("cmdmanager.value.is").replace("{value}", RPEconomy.getFormatted(r.getValue()) + " " + RPConfig.getEcoString("economy-name")));

                        RedProtect.get().logger.debug("Region Value: " + r.getValue());
                        return true;
                    } else {
                        RPLang.sendMessage(player, RPLang.get("cmdmanager.value.areabig").replace("{maxarea}", RPConfig.getEcoInt("max-area-toget-value").toString()));
                        return true;
                    }
                } else {
                    RPLang.sendMessage(player, "playerlistener.region.cantuse");
                    return true;
                }
            } else {
                RPLang.sendMessage(player, "cmdmanager.region.todo.that");
                return true;
            }
        }

        RPLang.sendCommandHelp(sender, "value", true);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}