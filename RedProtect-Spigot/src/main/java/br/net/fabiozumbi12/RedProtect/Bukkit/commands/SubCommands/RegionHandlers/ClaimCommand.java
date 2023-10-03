/*
 * Copyright (c) 2012-2023 - @FabioZumbi12
 * Last Modified: 02/10/2023 22:14
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

package br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommands.RegionHandlers;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import br.net.fabiozumbi12.RedProtect.Bukkit.commands.CommandHandlers;
import br.net.fabiozumbi12.RedProtect.Bukkit.actions.DefineRegionBuilder;
import br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommand;
import br.net.fabiozumbi12.RedProtect.Bukkit.region.RegionBuilder;
import br.net.fabiozumbi12.RedProtect.Core.region.PlayerRegion;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClaimCommand implements SubCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            CommandHandlers.HandleHelpPage(sender, 1);
            return true;
        }

        Player player = (Player) sender;
        String claimmode = RedProtect.get().getConfigManager().getWorldClaimType(player.getWorld().getName());

        if (!claimmode.equalsIgnoreCase("WAND") && !claimmode.equalsIgnoreCase("BOTH") && !RedProtect.get().getPermissionHandler().hasCommandPerm(player, "claim")) {
            RedProtect.get().getLanguageManager().sendMessage(player, "blocklistener.region.blockmode");
            return true;
        }

        if (args.length == 0) {
            String name = RedProtect.get().getUtil().nameGen(player.getName(), player.getWorld().getName());

            RedProtect.get().getLanguageManager().sendMessage(player, "regionbuilder.creating");

            // Run claim async
            Bukkit.getScheduler().runTaskAsynchronously(RedProtect.get(), () -> {
                RegionBuilder rb2 = new DefineRegionBuilder(player, RedProtect.get().firstLocationSelections.get(player), RedProtect.get().secondLocationSelections.get(player), name, new PlayerRegion(player.getUniqueId().toString(), player.getName()), new HashSet<>(), false);
                if (rb2.ready()) {
                    Region r2 = rb2.build();
                    RedProtect.get().getLanguageManager().sendMessage(player, RedProtect.get().getLanguageManager().get("cmdmanager.region.created") + " " + r2.getName() + ".");
                    RedProtect.get().getRegionManager().add(r2, player.getWorld().getName());

                    RedProtect.get().firstLocationSelections.remove(player);
                    RedProtect.get().secondLocationSelections.remove(player);

                    RedProtect.get().logger.addLog("(World " + r2.getWorld() + ") Player " + player.getName() + " CLAIMED region " + r2.getName());
                }
            });
            return true;
        }

        if (args.length == 1) {
            String name = args[0];

            RedProtect.get().getLanguageManager().sendMessage(player, "regionbuilder.creating");

            // Run claim async
            Bukkit.getScheduler().runTaskAsynchronously(RedProtect.get(), () -> {
                RegionBuilder rb2 = new DefineRegionBuilder(player, RedProtect.get().firstLocationSelections.get(player), RedProtect.get().secondLocationSelections.get(player), name, new PlayerRegion(player.getUniqueId().toString(), player.getName()), new HashSet<>(), false);
                if (rb2.ready()) {
                    Region r2 = rb2.build();
                    RedProtect.get().getLanguageManager().sendMessage(player, RedProtect.get().getLanguageManager().get("cmdmanager.region.created") + " " + r2.getName() + ".");
                    RedProtect.get().getRegionManager().add(r2, player.getWorld().getName());

                    RedProtect.get().firstLocationSelections.remove(player);
                    RedProtect.get().secondLocationSelections.remove(player);

                    RedProtect.get().logger.addLog("(World " + r2.getWorld() + ") Player " + player.getName() + " CLAIMED region " + r2.getName());
                }
            });
            return true;
        }

        if (args.length == 2) {
            String name = args[0];
            Set<PlayerRegion> addedAdmins = new HashSet<>();
            addedAdmins.add(new PlayerRegion(RedProtect.get().getUtil().PlayerToUUID(args[1]), args[1]));

            RedProtect.get().getLanguageManager().sendMessage(player, "regionbuilder.creating");

            // Run claim async
            Bukkit.getScheduler().runTaskAsynchronously(RedProtect.get(), () -> {
                RegionBuilder rb2 = new DefineRegionBuilder(player, RedProtect.get().firstLocationSelections.get(player), RedProtect.get().secondLocationSelections.get(player), name, new PlayerRegion(player.getUniqueId().toString(), player.getName()), addedAdmins, false);
                if (rb2.ready()) {
                    Region r2 = rb2.build();
                    RedProtect.get().getLanguageManager().sendMessage(player, RedProtect.get().getLanguageManager().get("cmdmanager.region.created") + " " + r2.getName() + ".");
                    RedProtect.get().getRegionManager().add(r2, player.getWorld().getName());

                    RedProtect.get().firstLocationSelections.remove(player);
                    RedProtect.get().secondLocationSelections.remove(player);

                    RedProtect.get().logger.addLog("(World " + r2.getWorld() + ") Player " + player.getName() + " CLAIMED region " + r2.getName());
                }
            });
            return true;
        }

        RedProtect.get().getLanguageManager().sendCommandHelp(sender, "claim", true);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> tab = new ArrayList<>();
        if (args.length == 2)
            if (args[1].isEmpty())
                tab.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
            else
                tab.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase())).toList());
        return tab;
    }
}