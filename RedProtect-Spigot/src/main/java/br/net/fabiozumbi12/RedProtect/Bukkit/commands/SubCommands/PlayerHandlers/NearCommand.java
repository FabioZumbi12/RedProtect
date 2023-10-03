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

package br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommands.PlayerHandlers;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import br.net.fabiozumbi12.RedProtect.Bukkit.commands.CommandHandlers;
import br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class NearCommand implements SubCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            CommandHandlers.HandleHelpPage(sender, 1);
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            Set<Region> regions = RedProtect.get().getRegionManager().getRegionsNear(player, 60);
            if (regions.size() == 0) {
                RedProtect.get().getLanguageManager().sendMessage(player, "cmdmanager.noregions.nearby");
            } else {
                Iterator<Region> i = regions.iterator();
                RedProtect.get().getLanguageManager().sendMessage(player, RedProtect.get().getLanguageManager().get("general.color") + "------------------------------------");
                RedProtect.get().getLanguageManager().sendMessage(player, "cmdmanager.region.near");
                RedProtect.get().getLanguageManager().sendMessage(player, RedProtect.get().getLanguageManager().get("general.color") + "------------------------------------");
                while (i.hasNext()) {
                    Region r = i.next();
                    player.sendMessage(RedProtect.get().getLanguageManager().get("cmdmanager.region.name") + r.getName() + RedProtect.get().getLanguageManager().get("general.color") + ChatColor.translateAlternateColorCodes('&', " | Center (&6X,Z" + RedProtect.get().getLanguageManager().get("general.color") + "): &6") + r.getCenterX() + ", " + r.getCenterZ());
                }
                RedProtect.get().getLanguageManager().sendMessage(player, RedProtect.get().getLanguageManager().get("general.color") + "------------------------------------");
            }
        }

        RedProtect.get().getLanguageManager().sendCommandHelp(sender, "near", true);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}