/*
 * Copyright (c) 2019 - @FabioZumbi12
 * Last Modified: 25/04/19 07:02
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

package br.net.fabiozumbi12.RedProtect.Sponge.commands.SubCommands.RegionHandlers;

import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import br.net.fabiozumbi12.RedProtect.Sponge.Region;
import br.net.fabiozumbi12.RedProtect.Sponge.helpers.FlagGui;
import br.net.fabiozumbi12.RedProtect.Sponge.helpers.RedProtectUtil;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.*;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.util.*;

import static br.net.fabiozumbi12.RedProtect.Sponge.commands.CommandHandlers.HandleHelpPage;
import static br.net.fabiozumbi12.RedProtect.Sponge.commands.CommandHandlers.handleFlag;

public class FlagCommand {

    public CommandSpec register() {
        return CommandSpec.builder()
                .description(Text.of("Command to handle region flags."))
                .arguments(
                        GenericArguments.optional(new FlagCommandElement(Text.of("flag"))),
                        GenericArguments.optional(GenericArguments.remainingJoinedStrings(Text.of("value")))
                )
                .permission("redprotect.command.flag")
                .executor((src, args) -> {
                    if (!(src instanceof Player) && args.hasAny("flag") && args.<String>getOne("flag").get().equalsIgnoreCase("info") && args.hasAny("value")) {
                        String[] argsInfo = args.<String>getOne("value").get().split(" ");
                        if (argsInfo.length == 2) {
                            Optional<World> w = Sponge.getServer().getWorld(argsInfo[1]);
                            if (!w.isPresent()) {
                                src.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("correct.usage") + " &eInvalid World: " + argsInfo[1]));
                                return CommandResult.success();
                            }
                            Region r = RedProtect.get().rm.getRegion(argsInfo[0], w.get().getName());
                            if (r == null) {
                                src.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("correct.usage") + " &eInvalid region: " + argsInfo[0]));
                                return CommandResult.success();
                            }
                            src.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("general.color") + "------------[" + RedProtect.get().lang.get("cmdmanager.region.flag.values") + "]------------"));
                            src.sendMessage(r.getFlagInfo());
                            src.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("general.color") + "------------------------------------"));
                            return CommandResult.success();
                        }
                    } else if (!(src instanceof Player) && args.hasAny("flag") && args.hasAny("value")) {
                        String[] argsInfo = args.<String>getOne("value").get().split(" ");
                        if (argsInfo.length >= 3) {
                            String flag = argsInfo[0];
                            Optional<World> w = Sponge.getServer().getWorld(argsInfo[2]);
                            if (!w.isPresent()) {
                                src.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("correct.usage") + " &eInvalid World: " + argsInfo[2]));
                                return CommandResult.success();
                            }
                            Region r = RedProtect.get().rm.getRegion(args.<String>getOne("flag").get(), w.get().getName());
                            if (r == null) {
                                src.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("correct.usage") + " &eInvalid region: " + argsInfo[0]));
                                return CommandResult.success();
                            }
                            Object objflag = RedProtect.get().getUtil().parseObject(argsInfo[1]);
                            if (r.setFlag(RedProtect.get().getVersionHelper().getCause(src), flag, objflag)) {
                                src.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("cmdmanager.region.flag.set").replace("{flag}", "'" + flag + "'") + " " + r.getFlagString(flag)));
                                RedProtect.get().logger.addLog("Console changed flag " + flag + " to " + r.getFlagString(flag));
                            }
                            return CommandResult.success();
                        }
                    } else if (src instanceof Player) {
                        Player player = (Player) src;

                        Region r = RedProtect.get().rm.getTopRegion(player.getLocation(), this.getClass().getName());
                        if (r == null) {
                            RedProtect.get().lang.sendMessage(player, "cmdmanager.region.todo.that");
                            return CommandResult.success();
                        }

                        if (!r.isLeader(player) && !r.isAdmin(player) && !RedProtect.get().ph.hasPerm(player, "redprotect.command.admin.flag")) {
                            RedProtect.get().lang.sendMessage(player, "no.permission");
                            return CommandResult.success();
                        }

                        if (args.hasAny("flag")) {
                            String flag = args.<String>getOne("flag").get();
                            String value = "";
                            if (args.hasAny("value")) {
                                value = args.<String>getOne("value").get();
                            }

                            RedProtect.get().logger.severe("Flag: " + flag);
                            RedProtect.get().logger.severe("Value: " + value);

                            if (RedProtect.get().config.configRoot().flags_configuration.change_flag_delay.enable) {
                                if (RedProtect.get().config.configRoot().flags_configuration.change_flag_delay.flags.contains(flag)) {
                                    if (!RedProtect.get().changeWait.contains(r.getName() + flag)) {
                                        RedProtect.get().getUtil().startFlagChanger(r.getName(), flag, player);
                                        handleFlag(player, flag, value, r);
                                    } else {
                                        RedProtect.get().lang.sendMessage(player, RedProtect.get().lang.get("gui.needwait.tochange").replace("{seconds}", RedProtect.get().config.configRoot().flags_configuration.change_flag_delay.seconds + ""));
                                    }
                                    return CommandResult.success();
                                }
                            }
                            handleFlag(player, flag, value, r);
                            return CommandResult.success();
                        } else {
                            FlagGui gui = new FlagGui(r.getName(), player, r, false, RedProtect.get().config.getGuiMaxSlot());
                            gui.open();
                            return CommandResult.success();
                        }
                    }

                    RedProtect.get().lang.sendCommandHelp(src, "flag", true);
                    return CommandResult.success();
                }).build();
    }
}

class FlagCommandElement extends CommandElement {

    public FlagCommandElement(Text key) {
        super(key);
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource sender, CommandArgs args) throws ArgumentParseException {
        return args.next();
    }

    @Override
    public List<String> complete(CommandSource sender, CommandArgs argss, CommandContext context) {
        if (!argss.hasNext()) return null;

        String[] args = argss.getRaw().split(" ");
        if (args.length == 1) {
            SortedSet<String> tab = new TreeSet<>(RedProtect.get().config.getDefFlags());
            for (String flag : RedProtect.get().config.AdminFlags) {
                if (RedProtect.get().ph.hasFlagPerm((Player) sender, flag)) {
                    tab.add(flag);
                }
            }
            return new ArrayList<>(tab);
        }
        if (args.length == 2) {
            SortedSet<String> tab = new TreeSet<>();
            for (String flag : RedProtect.get().config.getDefFlags()) {
                if (flag.startsWith(args[1])) {
                    tab.add(flag);
                }
            }
            for (String flag : RedProtect.get().config.AdminFlags) {
                if (flag.startsWith(args[1]) && RedProtect.get().ph.hasFlagPerm((Player) sender, flag)) {
                    tab.add(flag);
                }
            }
            return new ArrayList<>(tab);
        }
        return null;
    }
}