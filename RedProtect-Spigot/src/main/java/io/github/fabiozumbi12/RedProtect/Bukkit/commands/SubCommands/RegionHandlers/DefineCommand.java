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
import io.github.fabiozumbi12.RedProtect.Bukkit.actions.DefineRegionBuilder;
import io.github.fabiozumbi12.RedProtect.Bukkit.commands.SubCommand;
import io.github.fabiozumbi12.RedProtect.Bukkit.region.RegionBuilder;
import io.github.fabiozumbi12.RedProtect.Bukkit.commands.CommandHandlers;
import io.github.fabiozumbi12.RedProtect.Core.region.PlayerRegion;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class DefineCommand implements SubCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            CommandHandlers.HandleHelpPage(sender, 1);
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            PlayerRegion serverName = new PlayerRegion(RedProtect.get().getConfigManager().configRoot().region_settings.default_leader, RedProtect.get().getConfigManager().configRoot().region_settings.default_leader);
            String name = RedProtect.get().getUtil().nameGen(RedProtect.get().getConfigManager().configRoot().region_settings.default_leader, player.getWorld().getName());

            RedProtect.get().getLanguageManager().sendMessage(player, "regionbuilder.creating");

            // Run claim async
            Bukkit.getScheduler().runTaskAsynchronously(RedProtect.get(), () -> {
                RegionBuilder rb2 = new DefineRegionBuilder(player, RedProtect.get().firstLocationSelections.get(player), RedProtect.get().secondLocationSelections.get(player), name, serverName, new HashSet<>(), true);
                if (rb2.ready()) {
                    Region r2 = rb2.build();
                    RedProtect.get().getLanguageManager().sendMessage(player, RedProtect.get().getLanguageManager().get("cmdmanager.region.created") + " " + r2.getName() + ".");
                    RedProtect.get().getRegionManager().add(r2, player.getWorld().getName());

                    RedProtect.get().firstLocationSelections.remove(player);
                    RedProtect.get().secondLocationSelections.remove(player);

                    RedProtect.get().logger.addLog("(World " + r2.getWorld() + ") Player " + player.getName() + " DEFINED region " + r2.getName());
                }
            });
            return true;
        }

        if (args.length == 1) {
            PlayerRegion serverName = new PlayerRegion(RedProtect.get().getConfigManager().configRoot().region_settings.default_leader, RedProtect.get().getConfigManager().configRoot().region_settings.default_leader);
            RedProtect.get().getLanguageManager().sendMessage(player, "regionbuilder.creating");

            // Run claim async
            Bukkit.getScheduler().runTaskAsynchronously(RedProtect.get(), () -> {
                RegionBuilder rb2 = new DefineRegionBuilder(player, RedProtect.get().firstLocationSelections.get(player), RedProtect.get().secondLocationSelections.get(player), args[0], serverName, new HashSet<>(), true);
                if (rb2.ready()) {
                    Region r2 = rb2.build();
                    RedProtect.get().getLanguageManager().sendMessage(player, RedProtect.get().getLanguageManager().get("cmdmanager.region.created") + " " + r2.getName() + ".");
                    RedProtect.get().getRegionManager().add(r2, player.getWorld().getName());

                    RedProtect.get().firstLocationSelections.remove(player);
                    RedProtect.get().secondLocationSelections.remove(player);

                    RedProtect.get().logger.addLog("(World " + r2.getWorld() + ") Player " + player.getName() + " DEFINED region " + r2.getName());
                }
            });
            return true;
        }

        RedProtect.get().getLanguageManager().sendCommandHelp(sender, "define", true);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}
