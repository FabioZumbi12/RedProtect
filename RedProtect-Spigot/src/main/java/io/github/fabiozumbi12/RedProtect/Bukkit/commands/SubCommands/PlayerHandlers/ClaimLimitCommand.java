/*
 * Copyright (c) 2012-2023 - @FabioZumbi12
 * Last Modified: 02/10/2023 18:03
 *
 * This class is provided 'as-is', without any express or implied warranty. In no event will the authors be held liable for any
 *  damages arising from the use of this class.
 *
 * Permission is granted to anyone to use this class for any purpose, including commercial plugins, and to alter it and
 * redistribute it freely, subject to the following restrictions:
 * 1 - The origin of this class must not be misrepresented; you must not claim that you wrote the original software. If you
 * use this class in other plugins, an acknowledgment in the plugin documentation would be appreciated but is not required.
 * 2 - Altered source versions must be plainly marked as such, and must not be misrepresented as being the original class.
 * 3 - This notice may not be removed or altered from any source distribution.
 *
 * Esta classe é fornecida "como está", sem qualquer garantia expressa ou implícita. Em nenhum caso os autores serão
 * responsabilizados por quaisquer danos decorrentes do uso desta classe.
 *
 * É concedida permissão a qualquer pessoa para usar esta classe para qualquer finalidade, incluindo plugins pagos, e para
 * alterá-lo e redistribuí-lo livremente, sujeito às seguintes restrições:
 * 1 - A origem desta classe não deve ser deturpada; você não deve afirmar que escreveu a classe original. Se você usar esta
 *  classe em um plugin, uma confirmação de autoria na documentação do plugin será apreciada, mas não é necessária.
 * 2 - Versões de origem alteradas devem ser claramente marcadas como tal e não devem ser deturpadas como sendo a
 * classe original.
 * 3 - Este aviso não pode ser removido ou alterado de qualquer distribuição de origem.
 */

package io.github.fabiozumbi12.RedProtect.Bukkit.commands.SubCommands.PlayerHandlers;

import io.github.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import io.github.fabiozumbi12.RedProtect.Bukkit.commands.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClaimLimitCommand implements SubCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 2 && (sender instanceof ConsoleCommandSender || RedProtect.get().getPermissionHandler().hasPerm(sender, "redprotect.command.admin.claimlimit"))) {
            Player offp = RedProtect.get().getServer().getPlayer(args[0]);
            World w = RedProtect.get().getServer().getWorld(args[1]);
            if (w == null) {
                RedProtect.get().getLanguageManager().sendMessage(sender, RedProtect.get().getLanguageManager().get("cmdmanager.region.invalidworld"));
                return true;
            }
            if (offp == null) {
                RedProtect.get().getLanguageManager().sendMessage(sender, RedProtect.get().getLanguageManager().get("cmdmanager.noplayer.thisname").replace("{player}", args[0]));
                return true;
            }
            int limit = RedProtect.get().getPermissionHandler().getPlayerClaimLimit(offp);
            if (limit < 0 || RedProtect.get().getPermissionHandler().hasPerm(offp, "redprotect.limits.claim.unlimited")) {
                RedProtect.get().getLanguageManager().sendMessage(sender, RedProtect.get().getLanguageManager().get("cmdmanager.nolimit"));
                return true;
            }

            int currentUsed = RedProtect.get().getRegionManager().getRegions(offp.getUniqueId().toString(), w.getName()).size();
            ChatColor color = currentUsed >= limit ? ChatColor.RED : ChatColor.GOLD;
            RedProtect.get().getLanguageManager().sendMessage(sender, RedProtect.get().getLanguageManager().get("cmdmanager.yourclaims") + color + currentUsed + RedProtect.get().getLanguageManager().get("general.color") + "/" + color + limit + RedProtect.get().getLanguageManager().get("general.color"));
            return true;
        } else if (sender instanceof Player player) {

            if (args.length == 0) {
                int limit = RedProtect.get().getPermissionHandler().getPlayerClaimLimit(player);
                if (limit < 0 || RedProtect.get().getPermissionHandler().hasPerm(player, "redprotect.limits.claim.unlimited")) {
                    RedProtect.get().getLanguageManager().sendMessage(player, "cmdmanager.nolimit");
                    return true;
                }

                int currentUsed = RedProtect.get().getRegionManager().getRegions(player.getUniqueId().toString(), player.getWorld().getName()).size();
                ChatColor color = currentUsed >= limit ? ChatColor.RED : ChatColor.GOLD;
                RedProtect.get().getLanguageManager().sendMessage(player, RedProtect.get().getLanguageManager().get("cmdmanager.yourclaims") + color + currentUsed + RedProtect.get().getLanguageManager().get("general.color") + "/" + color + limit + RedProtect.get().getLanguageManager().get("general.color"));
                return true;
            }
        }

        RedProtect.get().getLanguageManager().sendCommandHelp(sender, "claimlimit", true);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && RedProtect.get().getPermissionHandler().hasPerm(sender, "redprotect.command.admin.claimlimit"))
            if (args[0].isEmpty())
                return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            else
                return Bukkit.getOnlinePlayers().stream().filter(p -> p.getName().toLowerCase().startsWith(args[0].toLowerCase())).map(Player::getName).collect(Collectors.toList());
        return new ArrayList<>();
    }
}