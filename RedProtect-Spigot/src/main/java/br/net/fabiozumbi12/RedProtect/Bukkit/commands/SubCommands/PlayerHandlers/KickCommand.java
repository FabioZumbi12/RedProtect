/*
 * Copyright (c) 2020 - @FabioZumbi12
 * Last Modified: 02/07/2020 19:01.
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
import br.net.fabiozumbi12.RedProtect.Bukkit.hooks.SimpleClansHook;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class KickCommand implements SubCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 3 && sender instanceof ConsoleCommandSender) {
            World w = RedProtect.get().getServer().getWorld(args[2]);
            if (w == null) {
                RedProtect.get().getLanguageManager().sendMessage(sender, "cmdmanager.region.invalidworld");
                return true;
            }
            Region r = RedProtect.get().getRegionManager().getRegion(args[1], w.getName());
            if (r == null) {
                RedProtect.get().getLanguageManager().sendMessage(sender, RedProtect.get().getLanguageManager().get("cmdmanager.region.doesntexist") + ": " + args[1]);
                return true;
            }

            Player visit = Bukkit.getPlayer(args[0]);
            if (visit == null) {
                RedProtect.get().getLanguageManager().sendMessage(sender, "cmdmanager.noplayer.online");
                return true;
            }

            if (r.canBuild(visit)) {
                RedProtect.get().getLanguageManager().sendMessage(sender, "cmdmanager.cantkick.member");
                return true;
            }

            Region rv = RedProtect.get().getRegionManager().getTopRegion(visit.getLocation());
            if (rv == null || !rv.getID().equals(r.getID())) {
                RedProtect.get().getLanguageManager().sendMessage(sender, RedProtect.get().getLanguageManager().get("cmdmanager.notonregion"));
                return true;
            }

            Location to = RedProtect.get().getUtil().DenyEnterPlayer(visit.getWorld(), visit.getLocation(), visit.getLocation(), r, true).add(0, 1, 0);
            if (visit.isInsideVehicle()) {
                Entity vehicle = visit.getVehicle();
                Objects.requireNonNull(vehicle).eject();
                Bukkit.getScheduler().runTaskLater(RedProtect.get(), () -> vehicle.teleport(to), 1);
            }
            visit.teleport(to);

            String sec = String.valueOf(RedProtect.get().getConfigManager().configRoot().region_settings.delay_after_kick_region);
            if (RedProtect.get().denyEnterRegion(r.getID(), visit.getName())) {
                RedProtect.get().getLanguageManager().sendMessage(sender, RedProtect.get().getLanguageManager().get("cmdmanager.region.kicked").replace("{player}", visit.getName()).replace("{region}", r.getName()).replace("{time}", sec));
            } else {
                RedProtect.get().getLanguageManager().sendMessage(sender, RedProtect.get().getLanguageManager().get("cmdmanager.already.cantenter").replace("{time}", sec));
            }
            return true;
        } else if (sender instanceof Player player) {

            if (args.length == 1 || args.length == 3) {
                Region r = RedProtect.get().getRegionManager().getTopRegion(player.getLocation());

                if (r == null) {
                    RedProtect.get().getLanguageManager().sendMessage(player, "cmdmanager.region.todo.that");
                    return true;
                }

                if (args.length == 3) {
                    r = RedProtect.get().getRegionManager().getRegion(args[1], args[2]);
                    if (r == null) {
                        RedProtect.get().getLanguageManager().sendMessage(player, "cmdmanager.region.todo.that");
                        return true;
                    }
                }

                if (!RedProtect.get().getPermissionHandler().hasRegionPermMember(player, "kick", r)) {
                    RedProtect.get().getLanguageManager().sendMessage(player, "no.permission");
                    return true;
                }

                Player visit = Bukkit.getPlayer(args[0]);
                if (visit == null) {
                    RedProtect.get().getLanguageManager().sendMessage(player, RedProtect.get().getLanguageManager().get("cmdmanager.noplayer.thisname").replace("{player}", args[0]));
                    return true;
                }

                if (r.canBuild(visit)) {
                    RedProtect.get().getLanguageManager().sendMessage(player, "cmdmanager.cantkick.member");
                    return true;
                }

                Entity vehicle = visit;
                if (visit.getVehicle() != null)
                    vehicle = visit.getVehicle();

                Region rv = RedProtect.get().getRegionManager().getTopRegion(vehicle.getLocation());
                if (rv == null || !rv.getID().equals(r.getID())) {
                    RedProtect.get().getLanguageManager().sendMessage(player, "cmdmanager.noplayer.thisregion");
                    return true;
                }

                if (RedProtect.get().hooks.checkSC() && SimpleClansHook.inWar(r, player, visit)) {
                    RedProtect.get().getLanguageManager().sendMessage(player, "cmdmanager.cantkick.war");
                    return true;
                }

                String sec = String.valueOf(RedProtect.get().getConfigManager().configRoot().region_settings.delay_after_kick_region);
                if (RedProtect.get().denyEnterRegion(r.getID(), visit.getName())) {
                    RedProtect.get().getLanguageManager().sendMessage(player, RedProtect.get().getLanguageManager().get("cmdmanager.region.kicked").replace("{player}", args[0]).replace("{region}", r.getName()).replace("{time}", sec));
                } else {
                    RedProtect.get().getLanguageManager().sendMessage(player, RedProtect.get().getLanguageManager().get("cmdmanager.already.cantenter").replace("{time}", sec));
                }
                return true;
            }
        }

        RedProtect.get().getLanguageManager().sendCommandHelp(sender, "kick", true);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> tab = new ArrayList<>();
        if (args.length == 1)
            if (args[0].isEmpty())
                tab.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
            else
                tab.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(name -> name.startsWith(args[0])).toList());
        if (args.length == 3)
            if (args[2].isEmpty())
                tab.addAll(Bukkit.getWorlds().stream().map(World::getName).toList());
            else
                tab.addAll(Bukkit.getWorlds().stream().map(World::getName).filter(name -> name.startsWith(args[1])).toList());
        return tab;
    }
}