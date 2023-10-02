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

package io.github.fabiozumbi12.RedProtect.Bukkit.commands.SubCommands.RegionHandlers;

import io.github.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import io.github.fabiozumbi12.RedProtect.Bukkit.Region;
import io.github.fabiozumbi12.RedProtect.Bukkit.actions.RedefineRegionBuilder;
import io.github.fabiozumbi12.RedProtect.Bukkit.commands.SubCommand;
import io.github.fabiozumbi12.RedProtect.Bukkit.commands.CommandHandlers;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class RedefineCommand implements SubCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            CommandHandlers.HandleHelpPage(sender, 1);
            return true;
        }

        Player player = (Player) sender;
        Region oldRect = null;

        if (args.length == 0) {
            oldRect = RedProtect.get().getRegionManager().getTopRegion(player.getLocation());
            if (oldRect == null) {
                RedProtect.get().getLanguageManager().sendMessage(player, "cmdmanager.region.todo.that");
                return true;
            }
        } else if (args.length == 1) {
            oldRect = RedProtect.get().getRegionManager().getRegion(args[0], player.getWorld().getName());
            if (oldRect == null) {
                RedProtect.get().getLanguageManager().sendMessage(player, RedProtect.get().getLanguageManager().get("cmdmanager.region.doesntexist") + ": " + args[0]);
                return true;
            }
        } else {
            RedProtect.get().getLanguageManager().sendCommandHelp(sender, "redefine", true);
            return true;
        }

        if (!RedProtect.get().getPermissionHandler().hasRegionPermLeader(player, "redefine", oldRect)) {
            RedProtect.get().getLanguageManager().sendMessage(player, "playerlistener.region.cantuse");
            return true;
        }

        RedProtect.get().getLanguageManager().sendMessage(player, "regionbuilder.creating");
        final Region oldRegion = oldRect;
        
        // Run claim async
        Bukkit.getScheduler().runTaskAsynchronously(RedProtect.get(), () -> {
            RedefineRegionBuilder rb = new RedefineRegionBuilder(player, oldRegion, RedProtect.get().firstLocationSelections.get(player), RedProtect.get().secondLocationSelections.get(player));
            if (rb.ready()) {
                Region r2 = rb.build();
                RedProtect.get().getLanguageManager().sendMessage(player, RedProtect.get().getLanguageManager().get("cmdmanager.region.redefined") + " " + r2.getName() + ".");
                RedProtect.get().getRegionManager().add(r2, player.getWorld().getName());

                RedProtect.get().firstLocationSelections.remove(player);
                RedProtect.get().secondLocationSelections.remove(player);

                RedProtect.get().logger.addLog("(World " + r2.getWorld() + ") Player " + player.getName() + " REDEFINED region " + r2.getName());
            }
        });

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}
