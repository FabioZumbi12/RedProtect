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

package br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommands;

import br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPUtil;
import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommand;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPConfig;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPLang;
import br.net.fabiozumbi12.RedProtect.Bukkit.fanciful.FancyMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static br.net.fabiozumbi12.RedProtect.Bukkit.commands.CommandHandlers.HandleHelpPage;
import static br.net.fabiozumbi12.RedProtect.Bukkit.commands.CommandHandlers.getCmd;

public class ListAreasCommand implements SubCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender || !sender.hasPermission("redprotect.command.list-areas")) {
            HandleHelpPage(sender, 1);
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(RPLang.get("general.color") + "-------------------------------------------------");
            RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.created.area-list"));
            sender.sendMessage("-----");
            for (World w : Bukkit.getWorlds()) {
                Set<Region> wregions = new HashSet<>();
                for (Region r : RedProtect.get().rm.getRegionsByWorld(w)) {
                    SimpleDateFormat dateformat = new SimpleDateFormat(RPConfig.getString("region-settings.date-format"));
                    Date now = null;
                    try {
                        now = dateformat.parse(RPUtil.DateNow());
                    } catch (ParseException e1) {
                        RedProtect.get().logger.severe("The 'date-format' don't match with date 'now'!!");
                    }
                    Date regiondate = null;
                    try {
                        regiondate = dateformat.parse(r.getDate());
                    } catch (ParseException e) {
                        RedProtect.get().logger.severe("The 'date-format' don't match with region date!!");
                        e.printStackTrace();
                    }
                    long days = TimeUnit.DAYS.convert(now.getTime() - regiondate.getTime(), TimeUnit.MILLISECONDS);
                    for (String play : RPConfig.getStringList("purge.ignore-regions-from-players")) {
                        if (r.isLeader(RPUtil.PlayerToUUID(play)) || r.isAdmin(RPUtil.PlayerToUUID(play))) {
                            break;
                        }
                    }
                    if (!r.isLeader(RPConfig.getString("region-settings.default-leader")) && days > RPConfig.getInt("purge.remove-oldest") && r.getArea() >= RPConfig.getInt("purge.regen.max-area-regen")) {
                        wregions.add(r);
                    }
                }
                if (wregions.size() == 0) {
                    continue;
                }
                Iterator<Region> it = wregions.iterator();
                String colorChar = ChatColor.translateAlternateColorCodes('&', RPConfig.getString("region-settings.database-colors." + w.getName(), "&a"));
                if (RPConfig.getBool("region-settings.region-list.hover-and-click-teleport") && RedProtect.get().ph.hasRegionPermAdmin(sender, "teleport", null)) {
                    boolean first = true;
                    FancyMessage fancy = new FancyMessage();
                    while (it.hasNext()) {
                        Region r = it.next();
                        String rname = RPLang.get("general.color") + ", " + ChatColor.GRAY + r.getName() + "(" + r.getArea() + ")";
                        if (first) {
                            rname = rname.substring(3);
                            first = false;
                        }
                        if (!it.hasNext()) {
                            rname = rname + RPLang.get("general.color") + ".";
                        }
                        fancy.text(rname).color(ChatColor.DARK_GRAY)
                                .tooltip(RPLang.get("cmdmanager.list.hover").replace("{region}", r.getName()))
                                .command("/rp " + getCmd("teleport") + " " + r.getName() + " " + r.getWorld())
                                .then(" ");
                    }
                    sender.sendMessage(RPLang.get("general.color") + RPLang.get("region.world").replace(":", "") + " " + colorChar + w.getName() + "[" + wregions.size() + "]" + ChatColor.RESET + ": ");
                    fancy.send(sender);
                    sender.sendMessage("-----");
                } else {
                    String worldregions = "";
                    while (it.hasNext()) {
                        Region r = it.next();
                        worldregions = worldregions + RPLang.get("general.color") + ", " + ChatColor.GRAY + r.getName() + "(" + r.getArea() + ")";
                    }
                    sender.sendMessage(RPLang.get("general.color") + RPLang.get("region.world").replace(":", "") + " " + colorChar + w.getName() + "[" + wregions.size() + "]" + ChatColor.RESET + ": ");
                    sender.sendMessage(worldregions.substring(3) + RPLang.get("general.color") + ".");
                    sender.sendMessage("-----");
                }
            }
            return true;
        }

        RPLang.sendCommandHelp(sender, "list-areas", true);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}
