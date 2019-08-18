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
import br.net.fabiozumbi12.RedProtect.Core.region.PlayerRegion;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static br.net.fabiozumbi12.RedProtect.Bukkit.commands.CommandHandlers.handleRemoveAdmin;

public class RemoveAdminCommand implements SubCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 3 && (sender instanceof ConsoleCommandSender || RedProtect.get().ph.hasPerm(sender, "redprotect.command.admin.removeadmin"))) {
            World w = RedProtect.get().getServer().getWorld(args[2]);
            if (w == null) {
                RedProtect.get().lang.sendMessage(sender, RedProtect.get().lang.get("cmdmanager.region.invalidworld"));
                return true;
            }
            Region r = RedProtect.get().rm.getRegion(args[1], w.getName());
            if (r == null) {
                RedProtect.get().lang.sendMessage(sender, RedProtect.get().lang.get("cmdmanager.region.doesntexist") + ": " + args[1]);
                return true;
            }
            handleRemoveAdmin(sender, args[0], r);
            return true;
        } else if (sender instanceof Player) {
            Player player = (Player) sender;

            if (args.length == 1) {
                handleRemoveAdmin(player, args[0], null);
                return true;
            }
        }

        RedProtect.get().lang.sendCommandHelp(sender, "removeadmin", true);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (sender instanceof Player){
            Region r = RedProtect.get().rm.getTopRegion(((Player) sender).getLocation());
            if (r != null && args.length == 1){
                if (args[0].isEmpty())
                    return r.getAdmins().stream().map(PlayerRegion::getPlayerName).collect(Collectors.toList());
                else
                    return r.getAdmins().stream().filter(p->p.getPlayerName().toLowerCase().startsWith(args[0].toLowerCase())).map(PlayerRegion::getPlayerName).collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }
}