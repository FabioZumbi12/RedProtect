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

package br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommands.PlayerHandlers;

import br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPUtil;
import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.region.Region;
import br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommand;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPLang;
import br.net.fabiozumbi12.RedProtect.Bukkit.hooks.WEListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPUtil.HandleHelpPage;

public class RegenCommand implements SubCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            HandleHelpPage(sender, 1);
            return true;
        }

        Player player = (Player) sender;

        if (!RedProtect.get().WE) {
            RPLang.sendMessage(player, "cmdmanager.wenotloaded");
            return true;
        }

        if (args.length == 0) {
            Region r = RedProtect.get().rm.getTopRegion(player.getLocation());
            if (r == null) {
                RPLang.sendMessage(player, "cmdmanager.region.doesexists");
                return true;
            }
            WEListener.regenRegion(r, Bukkit.getWorld(r.getWorld()), r.getMaxLocation(), r.getMinLocation(), 0, sender, false);
            return true;
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("undo")) {
                Region r = RedProtect.get().rm.getTopRegion(player.getLocation());
                if (r == null) {
                    RPLang.sendMessage(player, "cmdmanager.region.doesexists");
                    return true;
                }

                if (WEListener.undo(r.getID())) {
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.regen.undo.sucess").replace("{region}", r.getName()));
                } else {
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.regen.undo.none").replace("{region}", r.getName()));
                }
                return true;
            }

            if (args[0].equalsIgnoreCase("stop")) {
                RPUtil.stopRegen = true;
                RPLang.sendMessage(player, "&aRegen will stop now. To continue reload the plugin!");
                return true;
            }
        }

        if (args.length == 2) {
            World w = RedProtect.get().serv.getWorld(args[1]);
            if (w == null) {
                sender.sendMessage(RPLang.get("cmdmanager.region.invalidworld"));
                return true;
            }
            Region r = RedProtect.get().rm.getRegion(args[0], w);
            if (r == null) {
                sender.sendMessage(RPLang.get("correct.usage") + " " + ChatColor.YELLOW + "Invalid region: " + args[0]);
                return true;
            }

            WEListener.regenRegion(r, Bukkit.getWorld(r.getWorld()), r.getMaxLocation(), r.getMinLocation(), 0, sender, false);
            return true;
        }

        RPLang.sendCommandHelp(sender, "regen", true);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}