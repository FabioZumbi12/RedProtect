package br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommands.PlayerHandlers;

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

public class LAcceptCommand implements SubCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            HandleHelpPage(sender, 1);
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            if (RedProtect.get().alWait.containsKey(player)) {
                //info = region+database+pname
                String info = RedProtect.get().alWait.get(player);

                Player lsender = Bukkit.getPlayer(info.split("@")[2]);
                Region r = RedProtect.get().rm.getRegion(info.split("@")[0], info.split("@")[1]);

                String VictimUUID = player.getName();
                if (RedProtect.get().OnlineMode) {
                    VictimUUID = player.getUniqueId().toString();
                }

                if (r != null) {

                    if (RedProtect.get().ph.getPlayerClaimLimit(player) == (RedProtect.get().rm.getRegions(VictimUUID, r.getWorld()).size() + 1)) {
                        RPLang.sendMessage(player, "regionbuilder.claim.limit");
                        return true;
                    }

                    r.addLeader(VictimUUID);
                    RPLang.sendMessage(player, RPLang.get("cmdmanager.region.leader.youadded").replace("{region}", r.getName()) + " " + lsender.getName());
                    if (lsender.isOnline()) {
                        RPLang.sendMessage(lsender, RPLang.get("cmdmanager.region.leader.accepted").replace("{region}", r.getName()).replace("{player}", player.getName()));
                    }
                } else {
                    RPLang.sendMessage(player, "cmdmanager.region.doesexists");
                }
                RedProtect.get().alWait.remove(player);
                return true;
            } else {
                RPLang.sendMessage(player, "cmdmanager.norequests");
                return true;
            }
        }

        RPLang.sendCommandHelp(sender, "laccept", true);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}