package br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommands.PlayerHandlers;

import br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPUtil;
import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.region.Region;
import br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommand;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPConfig;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPLang;
import br.net.fabiozumbi12.RedProtect.Bukkit.hooks.SCHook;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPUtil.HandleHelpPage;

public class KickCommand implements SubCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            HandleHelpPage(sender, 1);
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 1 || args.length == 3) {
            Region r = RedProtect.get().rm.getTopRegion(player.getLocation());

            if (r == null) {
                RPLang.sendMessage(player, "cmdmanager.region.todo.that");
                return true;
            }

            if (args.length == 3) {
                r = RedProtect.get().rm.getRegion(args[1], args[2]);
                if (r == null) {
                    RPLang.sendMessage(player, "cmdmanager.region.todo.that");
                    return true;
                }
            }

            if (!RedProtect.get().ph.hasRegionPermMember(player, "kick", r)) {
                RPLang.sendMessage(player, "no.permission");
                return true;
            }

            Player visit = Bukkit.getPlayer(args[1]);
            if (visit == null) {
                RPLang.sendMessage(player, RPLang.get("cmdmanager.noplayer.thisname").replace("{player}", args[1]));
                return true;
            }

            if (r.canBuild(visit)) {
                RPLang.sendMessage(player, "cmdmanager.cantkick.member");
                return true;
            }

            Region rv = RedProtect.get().rm.getTopRegion(visit.getLocation());
            if (rv == null || !rv.getID().equals(r.getID())) {
                RPLang.sendMessage(player, "cmdmanager.noplayer.thisregion");
                return true;
            }

            if (RedProtect.get().SC && SCHook.inWar(r, player, visit)) {
                RPLang.sendMessage(player, "cmdmanager.cantkick.war");
                return true;
            }

            String sec = String.valueOf(RPConfig.getInt("region-settings.delay-after-kick-region"));
            if (RedProtect.get().denyEnterRegion(r.getID(), visit.getName())) {
                RPUtil.DenyEnterPlayer(visit.getWorld(), visit.getLocation(), visit.getLocation(), r, true);
                RPLang.sendMessage(player, RPLang.get("cmdmanager.region.kicked").replace("{player}", args[1]).replace("{region}", r.getName()).replace("{time}", sec));
            } else {
                RPLang.sendMessage(player, RPLang.get("cmdmanager.already.cantenter").replace("{time}", sec));
            }
            return true;
        }

        RPLang.sendCommandHelp(sender, "kick", true);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}