/*
 *  Copyright (c) 2019 - @FabioZumbi12
 *  Last Modified: 16/04/19 06:22
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

package br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommands.RegionHandlers;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommand;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPLang;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static br.net.fabiozumbi12.RedProtect.Bukkit.commands.CommandHandlers.HandleHelpPage;

public class ExpandVertCommand implements SubCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            HandleHelpPage(sender, 1);
            return true;
        }

        Player player = (Player) sender;

        Region r;
        switch (args.length) {
            case 0:
                r = RedProtect.get().rm.getTopRegion(player.getLocation());
                if (r == null) {
                    RPLang.sendMessage(player, "cmdmanager.region.todo.that");
                    return true;
                }
                break;
            //rp expand-vert [region]
            case 1:
                r = RedProtect.get().rm.getRegion(args[0], player.getWorld());
                if (r == null) {
                    RPLang.sendMessage(player, RPLang.get("cmdmanager.region.doesntexist") + ": " + args[0]);
                    return true;
                }
                break;
            //rp expand-vert [region] [database]
            case 2:
                if (Bukkit.getWorld(args[1]) == null) {
                    RPLang.sendMessage(player, "cmdmanager.region.invalidworld");
                    return true;
                }
                r = RedProtect.get().rm.getRegion(args[1], Bukkit.getWorld(args[1]));
                if (r == null) {
                    RPLang.sendMessage(player, RPLang.get("cmdmanager.region.doesntexist") + ": " + args[0]);
                    return true;
                }
                break;
            default:
                RPLang.sendCommandHelp(sender, "expand-vert", true);
                return true;
        }

        r.setMaxY(player.getWorld().getMaxHeight());
        r.setMinY(0);
        RPLang.sendMessage(player, RPLang.get("cmdmanager.region.expandvert.success").replace("{region}", r.getName()).replace("{miny}", String.valueOf(r.getMinY())).replace("{maxy}", String.valueOf(r.getMaxY())));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}