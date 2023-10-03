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
import br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommand;
import br.net.fabiozumbi12.RedProtect.Bukkit.guis.FlagGui;
import br.net.fabiozumbi12.RedProtect.Core.config.CoreConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.stream.Collectors;

public class FlagCommand implements SubCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 3 && (sender instanceof ConsoleCommandSender || RedProtect.get().getPermissionHandler().hasPerm(sender, "redprotect.command.admin.flag")) && args[2].equalsIgnoreCase("info")) {
            if (Bukkit.getWorld(args[2]) != null) {
                Region r = RedProtect.get().getRegionManager().getRegion(args[1], Bukkit.getWorld(args[2]).getName());
                if (r != null) {
                    sender.sendMessage(RedProtect.get().getLanguageManager().get("general.color") + "------------[" + RedProtect.get().getLanguageManager().get("cmdmanager.region.flag.values") + "]------------");
                    sender.sendMessage(r.getFlagInfo());
                    sender.sendMessage(RedProtect.get().getLanguageManager().get("general.color") + "------------------------------------");
                } else {
                    RedProtect.get().getLanguageManager().sendMessage(sender, RedProtect.get().getLanguageManager().get("correct.usage") + " " + ChatColor.YELLOW + "Invalid region: " + args[1]);
                }
            } else {
                RedProtect.get().getLanguageManager().sendMessage(sender, RedProtect.get().getLanguageManager().get("correct.usage") + " " + ChatColor.YELLOW + "Invalid World: " + args[2]);
            }
            return true;
        } else if (args.length == 4 && (sender instanceof ConsoleCommandSender || RedProtect.get().getPermissionHandler().hasPerm(sender, "redprotect.command.admin.flag"))) {
            World w = RedProtect.get().getServer().getWorld(args[3]);
            if (w == null) {
                RedProtect.get().getLanguageManager().sendMessage(sender, RedProtect.get().getLanguageManager().get("correct.usage") + ChatColor.YELLOW + " rp " + CommandHandlers.getCmd("flag") + " <regionName> <flag> <value> <database>");
                return true;
            }
            Region r = RedProtect.get().getRegionManager().getRegion(args[0], w.getName());
            if (r != null && (RedProtect.get().getConfigManager().getDefFlags().contains(args[1]) || CoreConfigManager.ADMIN_FLAGS.contains(args[1]))) {
                Object objflag = RedProtect.get().getUtil().parseObject(args[2]);
                r.setFlag(sender, args[1], objflag);
                RedProtect.get().getLanguageManager().sendMessage(sender, RedProtect.get().getLanguageManager().get("cmdmanager.region.flag.set").replace("{flag}", "'" + args[1] + "'") + " " + r.getFlagString(args[1]));
                RedProtect.get().logger.addLog("Console changed flag " + args[1] + " to " + r.getFlagString(args[1]));
                return true;
            }
        } else if (sender instanceof Player player) {

            if (args.length == 0) {
                Region r = RedProtect.get().getRegionManager().getTopRegion(player.getLocation());
                if (r != null) {
                    if (r.isLeader(player) || r.isAdmin(player) || RedProtect.get().getPermissionHandler().hasPerm(sender, "redprotect.command.admin.flag")) {
                        FlagGui gui = new FlagGui(RedProtect.get().getUtil().getTitleName(r), player, r, false, RedProtect.get().getConfigManager().getGuiMaxSlot());
                        gui.open();
                    } else {
                        RedProtect.get().getLanguageManager().sendMessage(player, "cmdmanager.region.flag.nopermregion");
                    }
                } else {
                    RedProtect.get().getLanguageManager().sendMessage(player, "cmdmanager.region.todo.that");
                }
                return true;
            }

            if (args.length == 1) {
                Region r = RedProtect.get().getRegionManager().getTopRegion(player.getLocation());
                if (r == null) {
                    RedProtect.get().getLanguageManager().sendMessage(player, "cmdmanager.region.todo.that");
                    return true;
                }

                if (args[0].equalsIgnoreCase("gui-edit")) {
                    if (RedProtect.get().getPermissionHandler().hasCommandPerm(player, "gui-edit")) {
                        FlagGui gui = new FlagGui(RedProtect.get().getLanguageManager().get("gui.editflag"), player, r, true, RedProtect.get().getConfigManager().getGuiMaxSlot());
                        gui.open();
                    } else {
                        RedProtect.get().getLanguageManager().sendMessage(player, "no.permission");
                    }
                    return true;
                }

                if (RedProtect.get().getConfigManager().configRoot().flags_configuration.change_flag_delay.enable) {
                    if (RedProtect.get().getConfigManager().configRoot().flags_configuration.change_flag_delay.flags.contains(args[0])) {
                        if (!RedProtect.get().changeWait.contains(r.getName() + args[0])) {
                            RedProtect.get().getUtil().startFlagChanger(r.getName(), args[0], player);
                            CommandHandlers.handleFlag(player, args[0], "", r);
                        } else {
                            RedProtect.get().getLanguageManager().sendMessage(player, RedProtect.get().getLanguageManager().get("gui.needwait.tochange").replace("{seconds}", "" + RedProtect.get().getConfigManager().configRoot().flags_configuration.change_flag_delay.seconds));
                        }
                        return true;
                    }
                }
                CommandHandlers.handleFlag(player, args[0], "", r);
                return true;
            }


            Region r = RedProtect.get().getRegionManager().getTopRegion(player.getLocation());
            if (r == null) {
                RedProtect.get().getLanguageManager().sendMessage(player, "cmdmanager.region.todo.that");
                return true;
            }

            if (args.length == 2 && args[0].equalsIgnoreCase("gui-edit")) {
                if (RedProtect.get().getPermissionHandler().hasCommandPerm(player, "gui-edit")) {
                    int MaxSlot;
                    try {
                        MaxSlot = 9 * Integer.parseInt(args[1]);
                        if (MaxSlot > 54 || MaxSlot < RedProtect.get().getConfigManager().getGuiMaxSlot()) {
                            RedProtect.get().getLanguageManager().sendMessage(player, "gui.edit.invalid-lines");
                            return true;
                        }
                    } catch (NumberFormatException e) {
                        RedProtect.get().getLanguageManager().sendMessage(player, "cmdmanager.region.invalid.number");
                        return true;
                    }
                    FlagGui gui = new FlagGui(RedProtect.get().getLanguageManager().get("gui.editflag"), player, r, true, MaxSlot);
                    gui.open();
                } else {
                    RedProtect.get().getLanguageManager().sendMessage(player, "no.permission");
                }
                return true;
            }

            StringBuilder text = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                text.append(" ").append(args[i]);
            }
            if (RedProtect.get().getConfigManager().configRoot().flags_configuration.change_flag_delay.enable) {
                if (RedProtect.get().getConfigManager().configRoot().flags_configuration.change_flag_delay.flags.contains(args[1])) {
                    if (!RedProtect.get().changeWait.contains(r.getName() + args[1])) {
                        RedProtect.get().getUtil().startFlagChanger(r.getName(), args[1], player);
                        CommandHandlers.handleFlag(player, args[1], text.substring(1), r);
                    } else {
                        RedProtect.get().getLanguageManager().sendMessage(player, RedProtect.get().getLanguageManager().get("gui.needwait.tochange").replace("{seconds}", "" + RedProtect.get().getConfigManager().configRoot().flags_configuration.change_flag_delay.seconds));
                    }
                    return true;
                }
            }
            CommandHandlers.handleFlag(player, args[0], text.substring(1), r);
            return true;
        }

        RedProtect.get().getLanguageManager().sendCommandHelp(sender, "flag", true);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 0) {
            SortedSet<String> tab = new TreeSet<>(RedProtect.get().getConfigManager().getDefFlags());
            for (String flag : CoreConfigManager.ADMIN_FLAGS) {
                if (RedProtect.get().getPermissionHandler().hasFlagPerm((Player) sender, flag)) {
                    tab.add(flag);
                }
            }
            return new ArrayList<>(tab);
        }
        if (args.length == 1) {
            SortedSet<String> tab = new TreeSet<>();
            for (String flag : RedProtect.get().getConfigManager().getDefFlags()) {
                if (flag.startsWith(args[0])) {
                    tab.add(flag);
                }
            }
            for (String flag : CoreConfigManager.ADMIN_FLAGS) {
                if (flag.startsWith(args[0]) && RedProtect.get().getPermissionHandler().hasFlagPerm((Player) sender, flag)) {
                    tab.add(flag);
                }
            }
            return new ArrayList<>(tab);
        }

        // Flag completions
        if (args[0].equalsIgnoreCase("effects")) {
            if (args.length == 2) {
                if (args[1].isEmpty())
                    return Arrays.stream(PotionEffectType.values()).map(PotionEffectType::getName).collect(Collectors.toList());
                else
                    return Arrays.stream(PotionEffectType.values()).filter(e -> e.getName().startsWith(args[1].toUpperCase())).map(PotionEffectType::getName).collect(Collectors.toList());
            }
            if (args.length == 3) {
                return Collections.singletonList(String.valueOf(1));
            }
        }
        if (args[0].equalsIgnoreCase("particles")) {
            if (args.length == 2) {
                if (args[1].isEmpty())
                    return Arrays.stream(Particle.values()).map(Particle::name).collect(Collectors.toList());
                else
                    return Arrays.stream(Particle.values()).filter(p -> p.name().startsWith(args[1].toUpperCase())).map(Particle::name).collect(Collectors.toList());
            }
            if (args.length <= 6) {
                return Collections.singletonList(String.valueOf(10));
            }
        }
        return new ArrayList<>();
    }
}