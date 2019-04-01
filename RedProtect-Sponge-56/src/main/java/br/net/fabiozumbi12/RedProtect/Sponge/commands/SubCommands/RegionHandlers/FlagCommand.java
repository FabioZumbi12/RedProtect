/*
 *
 * Copyright (c) 2019 - @FabioZumbi12
 * Last Modified: 01/04/19 01:54
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

package br.net.fabiozumbi12.RedProtect.Sponge.commands.SubCommands.RegionHandlers;

import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import br.net.fabiozumbi12.RedProtect.Sponge.Region;
import br.net.fabiozumbi12.RedProtect.Sponge.config.RPLang;
import br.net.fabiozumbi12.RedProtect.Sponge.helpers.RPGui;
import br.net.fabiozumbi12.RedProtect.Sponge.helpers.RPUtil;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import static br.net.fabiozumbi12.RedProtect.Sponge.commands.CommandHandlers.HandleHelpPage;
import static br.net.fabiozumbi12.RedProtect.Sponge.commands.CommandHandlers.handleFlag;

public class FlagCommand {

    public CommandSpec register() {
        return CommandSpec.builder()
                .description(Text.of("Command to handle region flags."))
                .arguments(
                        GenericArguments.optional(GenericArguments.string(Text.of("flag"))),
                        GenericArguments.optional(GenericArguments.remainingJoinedStrings(Text.of("value")))
                )
                .permission("redprotect.command.flag")
                .executor((src, args) -> {
                    if (!(src instanceof Player)) {
                        HandleHelpPage(src, 1);
                    } else {
                        Player player = (Player)src;

                        Region r = RedProtect.get().rm.getTopRegion(player.getLocation(), this.getClass().getName());
                        if (r == null) {
                            RPLang.sendMessage(player, "cmdmanager.region.todo.that");
                            return CommandResult.success();
                        }

                        if (!r.isLeader(player) && !r.isAdmin(player)){
                            RPLang.sendMessage(player, "no.permission");
                            return CommandResult.success();
                        }

                        if (args.hasAny("flag")) {
                            String flag = args.<String>getOne("flag").get();
                            String value = "";
                            if (args.hasAny("value")) {
                                value = args.<String>getOne("value").get();
                            }

                            if (RedProtect.get().cfgs.root().flags_configuration.change_flag_delay.enable) {
                                if (RedProtect.get().cfgs.root().flags_configuration.change_flag_delay.flags.contains(flag)) {
                                    if (!RedProtect.get().changeWait.contains(r.getName() + flag)) {
                                        RPUtil.startFlagChanger(r.getName(), flag, player);
                                        handleFlag(player, flag, value, r);
                                    } else {
                                        RPLang.sendMessage(player, RPLang.get("gui.needwait.tochange").replace("{seconds}", RedProtect.get().cfgs.root().flags_configuration.change_flag_delay.seconds + ""));
                                    }
                                    return CommandResult.success();
                                }
                            }
                            handleFlag(player, flag, value, r);
                        } else {
                            RPGui gui = new RPGui(r.getName(), player, r, false, RedProtect.get().cfgs.getGuiMaxSlot());
                            gui.open();
                        }
                    }
                    return CommandResult.success();
                }).build();
    }
}
