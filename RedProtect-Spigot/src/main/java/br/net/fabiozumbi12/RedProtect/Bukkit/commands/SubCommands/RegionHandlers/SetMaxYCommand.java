/*
 * Copyright (c) 2020 - @FabioZumbi12
 * Last Modified: 02/07/2020 18:59.
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
import br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static br.net.fabiozumbi12.RedProtect.Bukkit.commands.CommandHandlers.HandleHelpPage;

public class SetMaxYCommand implements SubCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            HandleHelpPage(sender, 1);
            return true;
        }

        Player player = (Player) sender;

        Region r;
        //rp setmaxy <size>
        switch (args.length) {
            case 1 -> {
                r = RedProtect.get().getRegionManager().getTopRegion(player.getLocation());
                if (r == null) {
                    RedProtect.get().getLanguageManager().sendMessage(player, "cmdmanager.region.todo.that");
                    return true;
                }
            }
            //rp setmaxy <size> [region]
            case 2 -> {
                r = RedProtect.get().getRegionManager().getRegion(args[1], player.getWorld().getName());
                if (r == null) {
                    RedProtect.get().getLanguageManager().sendMessage(player, RedProtect.get().getLanguageManager().get("cmdmanager.region.doesntexist") + ": " + args[1]);
                    return true;
                }
            }
            //rp setmaxy <size> [region] [world]
            case 3 -> {
                if (Bukkit.getWorld(args[2]) == null) {
                    RedProtect.get().getLanguageManager().sendMessage(player, "cmdmanager.region.invalidworld");
                    return true;
                }
                r = RedProtect.get().getRegionManager().getRegion(args[1], Bukkit.getWorld(args[2]).getName());
                if (r == null) {
                    RedProtect.get().getLanguageManager().sendMessage(player, RedProtect.get().getLanguageManager().get("cmdmanager.region.doesntexist") + ": " + args[1]);
                    return true;
                }
            }
            default -> {
                RedProtect.get().getLanguageManager().sendCommandHelp(sender, "setmaxy", true);
                return true;
            }
        }

        String from = String.valueOf(r.getMaxY());

        try {
            int size = Integer.parseInt(args[0]);
            if ((size - r.getMinY()) <= 1) {
                RedProtect.get().getLanguageManager().sendMessage(player, "cmdmanager.region.ysiszesmatch");
                return true;
            }

            if (!r.isLeader(player) && !r.isAdmin(player) && !RedProtect.get().getPermissionHandler().hasPerm(player, "redprotect.command.admin.setmaxy")) {
                RedProtect.get().getLanguageManager().sendMessage(player, "playerlistener.region.cantuse");
                return true;
            }

            r.setMaxY(size);
            RedProtect.get().getLanguageManager().sendMessage(player, RedProtect.get().getLanguageManager().get("cmdmanager.region.setmaxy.success").replace("{region}", r.getName()).replace("{fromsize}", from).replace("{size}", String.valueOf(size)));
            RedProtect.get().logger.addLog("(World " + r.getWorld() + ") Player " + player.getName() + " SETMAXY of region " + r.getName() + " to " + args[0]);
            return true;
        } catch (NumberFormatException e) {
            RedProtect.get().getLanguageManager().sendMessage(player, "cmdmanager.region.invalid.number");
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> tab = new ArrayList<>();
        if (args.length == 1)
            tab.add(sender instanceof Player ? String.valueOf(((Player) sender).getLocation().getBlockY()) : "0");
        if (args.length == 3)
            if (args[2].isEmpty())
                tab.addAll(Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList()));
            else
                tab.addAll(Bukkit.getWorlds().stream().filter(w -> w.getName().startsWith(args[1])).map(World::getName).collect(Collectors.toList()));
        return tab;
    }
}