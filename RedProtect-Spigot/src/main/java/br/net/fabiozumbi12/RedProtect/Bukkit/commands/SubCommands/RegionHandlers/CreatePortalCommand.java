/*
 *
 * Copyright (c) 2019 - @FabioZumbi12
 * Last Modified: 28/03/19 20:18
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
 *
 */

package br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommands.RegionHandlers;

import br.net.fabiozumbi12.RedProtect.Bukkit.region.BukkitRegion;
import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
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

import java.util.HashSet;
import java.util.List;

import static br.net.fabiozumbi12.RedProtect.Bukkit.commands.CommandHandlers.HandleHelpPage;

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
            BukkitRegion r = RedProtect.get().rm.getRegion(args[1], w);
            if (r == null) {
                RPLang.sendMessage(player, RPLang.get("cmdmanager.createportal.warning").replace("{region}", args[1]));
            }

            String serverName = RPConfig.getString("region-settings.default-leader");
            String name = args[0].replace(" ", "_").replaceAll("[^\\p{L}_0-9 ]", "");

            BukkitRegion r2 = RedProtect.get().rm.getRegion(name, w);

            if (r2 != null) {
                if (!r2.isLeader(player) || !r2.isAdmin(player)){
                    RPLang.sendMessage(player, "no.permission");
                    return true;
                }
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