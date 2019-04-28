/*
 * Copyright (c) 2019 - @FabioZumbi12
 * Last Modified: 25/04/19 07:02
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

package br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommands.PlayerHandlers;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommand;
import br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RedProtectUtil;
import br.net.fabiozumbi12.RedProtect.Bukkit.hooks.SimpleClansHook;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static br.net.fabiozumbi12.RedProtect.Bukkit.commands.CommandHandlers.HandleHelpPage;

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
                RedProtect.get().lang.sendMessage(player, "cmdmanager.region.todo.that");
                return true;
            }

            if (args.length == 3) {
                r = RedProtect.get().rm.getRegion(args[1], args[2]);
                if (r == null) {
                    RedProtect.get().lang.sendMessage(player, "cmdmanager.region.todo.that");
                    return true;
                }
            }

            if (!RedProtect.get().ph.hasRegionPermMember(player, "kick", r)) {
                RedProtect.get().lang.sendMessage(player, "no.permission");
                return true;
            }

            Player visit = Bukkit.getPlayer(args[1]);
            if (visit == null) {
                RedProtect.get().lang.sendMessage(player, RedProtect.get().lang.get("cmdmanager.noplayer.thisname").replace("{player}", args[1]));
                return true;
            }

            if (r.canBuild(visit)) {
                RedProtect.get().lang.sendMessage(player, "cmdmanager.cantkick.member");
                return true;
            }

            Region rv = RedProtect.get().rm.getTopRegion(visit.getLocation());
            if (rv == null || !rv.getID().equals(r.getID())) {
                RedProtect.get().lang.sendMessage(player, "cmdmanager.noplayer.thisregion");
                return true;
            }

            if (RedProtect.get().hooks.simpleClans && SimpleClansHook.inWar(r, player, visit)) {
                RedProtect.get().lang.sendMessage(player, "cmdmanager.cantkick.war");
                return true;
            }

            String sec = String.valueOf(RedProtect.get().config.configRoot().region_settings.delay_after_kick_region);
            if (RedProtect.get().denyEnterRegion(r.getID(), visit.getName())) {
                RedProtectUtil.DenyEnterPlayer(visit.getWorld(), visit.getLocation(), visit.getLocation(), r, true);
                RedProtect.get().lang.sendMessage(player, RedProtect.get().lang.get("cmdmanager.region.kicked").replace("{player}", args[1]).replace("{region}", r.getName()).replace("{time}", sec));
            } else {
                RedProtect.get().lang.sendMessage(player, RedProtect.get().lang.get("cmdmanager.already.cantenter").replace("{time}", sec));
            }
            return true;
        }

        RedProtect.get().lang.sendCommandHelp(sender, "kick", true);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}