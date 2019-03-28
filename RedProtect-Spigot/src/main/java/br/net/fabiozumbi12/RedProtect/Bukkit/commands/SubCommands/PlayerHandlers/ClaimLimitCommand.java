package br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommands.PlayerHandlers;

import br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPUtil;
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

public class ClaimLimitCommand implements SubCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            HandleHelpPage(sender, 1);
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            int limit = RedProtect.get().ph.getPlayerClaimLimit(player);
            if (limit < 0 || RedProtect.get().ph.hasPerm(player, "redprotect.limits.claim.unlimited")) {
                RPLang.sendMessage(player, "cmdmanager.nolimit");
                return true;
            }

            int currentUsed = RedProtect.get().rm.getRegions(RPUtil.PlayerToUUID(player.getName()), player.getWorld()).size();
            ChatColor color = currentUsed >= limit ? ChatColor.RED : ChatColor.GOLD;
            RPLang.sendMessage(player, RPLang.get("cmdmanager.yourclaims") + color + currentUsed + RPLang.get("general.color") + "/" + color + limit + RPLang.get("general.color"));
            return true;
        }

        if (!RedProtect.get().ph.hasCommandPerm(player, "claimlimit.other")) {
            RPLang.sendMessage(player, "no.permission");
            return true;
        }

        if (args.length == 1) {
            Player offp = RedProtect.get().serv.getOfflinePlayer(args[0]).getPlayer();
            if (offp == null) {
                RPLang.sendMessage(player, RPLang.get("cmdmanager.noplayer.thisname").replace("{player}", args[0]));
                return true;
            }
            int limit = RedProtect.get().ph.getPlayerClaimLimit(offp);
            if (limit < 0 || RedProtect.get().ph.hasPerm(offp, "redprotect.limits.claim.unlimited")) {
                RPLang.sendMessage(player, "cmdmanager.nolimit");
                return true;
            }

            int currentUsed = RedProtect.get().rm.getRegions(RPUtil.PlayerToUUID(offp.getName()), offp.getWorld()).size();
            ChatColor color = currentUsed >= limit ? ChatColor.RED : ChatColor.GOLD;
            RPLang.sendMessage(player, RPLang.get("cmdmanager.yourclaims") + color + currentUsed + RPLang.get("general.color") + "/" + color + limit + RPLang.get("general.color"));
            return true;
        }

        RPLang.sendCommandHelp(sender, "claimlimit", true);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}