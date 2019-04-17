/*
 *  Copyright (c) 2019 - @FabioZumbi12
 *  Last Modified: 16/04/19 06:21
 *
 *  This class is provided 'as-is', without any express or implied warranty. In no event will the authors be held liable for any
 *   damages arising from the use of this class.
 *
 *  Permission is granted to anyone to use this class for any purpose, including commercial plugins, and to alter it and
 *  redistribute it freely, subject to the following restrictions:
 *  1 - The origin of this class must not be misrepresented; you must not claim that you wrote the original software. If you
 *  use this class in other plugins, an acknowledgment in the plugin documentation would be appreciated but is not required.
 *  2 - Altered source versions must be plainly marked as such, and must not be misrepresented as being the original class.
 *  3 - This notice may not be removed or altered from any source distribution.
 *
 *  Esta classe é fornecida "como está", sem qualquer garantia expressa ou implícita. Em nenhum caso os autores serão
 *  responsabilizados por quaisquer danos decorrentes do uso desta classe.
 *
 *  É concedida permissão a qualquer pessoa para usar esta classe para qualquer finalidade, incluindo plugins pagos, e para
 *  alterá-lo e redistribuí-lo livremente, sujeito às seguintes restrições:
 *  1 - A origem desta classe não deve ser deturpada; você não deve afirmar que escreveu a classe original. Se você usar esta
 *   classe em um plugin, uma confirmação de autoria na documentação do plugin será apreciada, mas não é necessária.
 *  2 - Versões de origem alteradas devem ser claramente marcadas como tal e não devem ser deturpadas como sendo a
 *  classe original.
 *  3 - Este aviso não pode ser removido ou alterado de qualquer distribuição de origem.
 */

package br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommands.PlayerHandlers;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommand;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPLang;
import br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static br.net.fabiozumbi12.RedProtect.Bukkit.commands.CommandHandlers.HandleHelpPage;

public class BlockLimitCommand implements SubCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            HandleHelpPage(sender, 1);
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            int limit = RedProtect.get().ph.getPlayerBlockLimit(player);
            if (limit < 0 || RedProtect.get().ph.hasPerm(player, "redprotect.limits.blocks.unlimited")) {
                RPLang.sendMessage(player, "cmdmanager.nolimit");
                return true;
            }
            String uuid = player.getUniqueId().toString();
            if (!RedProtect.get().OnlineMode) {
                uuid = player.getName().toLowerCase();
            }
            int currentUsed = RedProtect.get().rm.getTotalRegionSize(uuid, player.getWorld().getName());
            ChatColor color = currentUsed >= limit ? ChatColor.RED : ChatColor.GOLD;
            RPLang.sendMessage(player, RPLang.get("cmdmanager.yourarea") + color + currentUsed + RPLang.get("general.color") + "/" + color + limit + RPLang.get("general.color"));
            return true;
        }

        if (args.length == 1 && RedProtect.get().ph.hasPerm(player, "redprotect.command.admin.blocklimit")) {
            Player offp = RedProtect.get().getServer().getOfflinePlayer(args[0]).getPlayer();
            if (offp == null) {
                RPLang.sendMessage(player, RPLang.get("cmdmanager.noplayer.thisname").replace("{player}", args[0]));
                return true;
            }
            int limit = RedProtect.get().ph.getPlayerBlockLimit(offp);
            if (limit < 0 || RedProtect.get().ph.hasPerm(offp, "redprotect.limits.blocks.unlimited")) {
                RPLang.sendMessage(player, "cmdmanager.nolimit");
                return true;
            }

            int currentUsed = RedProtect.get().rm.getTotalRegionSize(RPUtil.PlayerToUUID(offp.getName()), offp.getWorld().getName());
            ChatColor color = currentUsed >= limit ? ChatColor.RED : ChatColor.GOLD;
            RPLang.sendMessage(player, RPLang.get("cmdmanager.yourarea") + color + currentUsed + RPLang.get("general.color") + "/" + color + limit + RPLang.get("general.color"));
            return true;
        }

        RPLang.sendCommandHelp(sender, "blocklimit", true);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}