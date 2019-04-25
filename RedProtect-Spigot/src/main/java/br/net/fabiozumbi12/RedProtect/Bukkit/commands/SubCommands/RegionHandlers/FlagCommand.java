/*
 *  Copyright (c) 2019 - @FabioZumbi12
 *  Last Modified: 16/04/19 06:21
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
import br.net.fabiozumbi12.RedProtect.Bukkit.config.LangManager;
import br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPGui;
import br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static br.net.fabiozumbi12.RedProtect.Bukkit.commands.CommandHandlers.HandleHelpPage;
import static br.net.fabiozumbi12.RedProtect.Bukkit.commands.CommandHandlers.handleFlag;

public class FlagCommand implements SubCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            HandleHelpPage(sender, 1);
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            Region r = RedProtect.get().rm.getTopRegion(player.getLocation());
            if (r != null) {
                if (r.isLeader(player) || r.isAdmin(player) || RedProtect.get().ph.hasPerm(sender, "redprotect.command.admin.flag")) {
                    RPGui gui = new RPGui(RPUtil.getTitleName(r), player, r, false, RedProtect.get().config.getGuiMaxSlot());
                    gui.open();
                } else {
                    RedProtect.get().lang.sendMessage(player, "cmdmanager.region.flag.nopermregion");
                }
            } else {
                RedProtect.get().lang.sendMessage(player, "cmdmanager.region.todo.that");
            }
            return true;
        }

        if (args.length == 1) {
            Region r = RedProtect.get().rm.getTopRegion(player.getLocation());
            if (r == null) {
                RedProtect.get().lang.sendMessage(player, "cmdmanager.region.todo.that");
                return true;
            }

            if (args[0].equalsIgnoreCase("gui-edit")) {
                if (RedProtect.get().ph.hasCommandPerm(player, "gui-edit")) {
                    RPGui gui = new RPGui(RedProtect.get().lang.get("gui.editflag"), player, r, true, RedProtect.get().config.getGuiMaxSlot());
                    gui.open();
                } else {
                    RedProtect.get().lang.sendMessage(player, "no.permission");
                }
                return true;
            }

            if (RedProtect.get().config.configRoot().flags_configuration.change_flag_delay.enable) {
                if (RedProtect.get().config.configRoot().flags_configuration.change_flag_delay.flags.contains(args[0])) {
                    if (!RedProtect.get().changeWait.contains(r.getName() + args[0])) {
                        RPUtil.startFlagChanger(r.getName(), args[0], player);
                        handleFlag(player, args[0], "", r);
                        return true;
                    } else {
                        RedProtect.get().lang.sendMessage(player, RedProtect.get().lang.get("gui.needwait.tochange").replace("{seconds}", ""+RedProtect.get().config.configRoot().flags_configuration.change_flag_delay.seconds));
                        return true;
                    }
                }
            }
            handleFlag(player, args[0], "", r);
            return true;
        }


        Region r = RedProtect.get().rm.getTopRegion(player.getLocation());
        if (r == null) {
            RedProtect.get().lang.sendMessage(player, "cmdmanager.region.todo.that");
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("gui-edit")) {
            if (RedProtect.get().ph.hasCommandPerm(player, "gui-edit")) {
                int MaxSlot;
                try {
                    MaxSlot = 9 * Integer.parseInt(args[1]);
                    if (MaxSlot > 54 || MaxSlot < RedProtect.get().config.getGuiMaxSlot()) {
                        RedProtect.get().lang.sendMessage(player, "gui.edit.invalid-lines");
                        return true;
                    }
                } catch (NumberFormatException e) {
                    RedProtect.get().lang.sendMessage(player, "cmdmanager.region.invalid.number");
                    return true;
                }
                RPGui gui = new RPGui(RedProtect.get().lang.get("gui.editflag"), player, r, true, MaxSlot);
                gui.open();
            } else {
                RedProtect.get().lang.sendMessage(player, "no.permission");
            }
            return true;
        }

        //if (args.length >= 2)
        StringBuilder text = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            text.append(" ").append(args[i]);
        }
        if (RedProtect.get().config.configRoot().flags_configuration.change_flag_delay.enable) {
            if (RedProtect.get().config.configRoot().flags_configuration.change_flag_delay.flags.contains(args[1])) {
                if (!RedProtect.get().changeWait.contains(r.getName() + args[1])) {
                    RPUtil.startFlagChanger(r.getName(), args[1], player);
                    handleFlag(player, args[1], text.substring(1), r);
                    return true;
                } else {
                    RedProtect.get().lang.sendMessage(player, RedProtect.get().lang.get("gui.needwait.tochange").replace("{seconds}", ""+RedProtect.get().config.configRoot().flags_configuration.change_flag_delay.seconds));
                    return true;
                }
            }
        }
        handleFlag(player, args[0], text.substring(1), r);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 0) {
            SortedSet<String> tab = new TreeSet<>(RedProtect.get().config.getDefFlags());
            for (String flag : RedProtect.get().config.AdminFlags) {
                if (RedProtect.get().ph.hasFlagPerm((Player) sender, flag)) {
                    tab.add(flag);
                }
            }
            return new ArrayList<>(tab);
        }
        if (args.length == 1) {
            SortedSet<String> tab = new TreeSet<>();
            for (String flag : RedProtect.get().config.getDefFlags()) {
                if (flag.startsWith(args[0])) {
                    tab.add(flag);
                }
            }
            for (String flag : RedProtect.get().config.AdminFlags) {
                if (flag.startsWith(args[0]) && RedProtect.get().ph.hasFlagPerm((Player) sender, flag)) {
                    tab.add(flag);
                }
            }
            return new ArrayList<>(tab);
        }
        return null;
    }
}