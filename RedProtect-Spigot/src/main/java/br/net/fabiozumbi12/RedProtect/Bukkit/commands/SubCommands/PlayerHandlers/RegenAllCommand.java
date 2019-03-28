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

public class RegenAllCommand implements SubCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            HandleHelpPage(sender, 1);
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 1) {
            if (!RedProtect.get().WE) {
                return true;
            }
            int regen = RedProtect.get().rm.regenAll(args[0]);
            if (regen <= 0) {
                RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.noneregenerated"));
            } else {
                RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.regenerated").replace("{regions}", regen + "").replace("{player}", args[0]));
            }
            return true;
        }

        RPLang.sendCommandHelp(sender, "regen-all", true);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}