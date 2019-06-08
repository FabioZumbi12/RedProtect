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
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import static br.net.fabiozumbi12.RedProtect.Sponge.commands.CommandHandlers.getRegionforList;
import static br.net.fabiozumbi12.RedProtect.Sponge.commands.CommandHandlers.handleList;

public class ListCommand {

    public CommandSpec register() {
        return CommandSpec.builder()
                .description(Text.of("Command to list player regions."))
                .arguments(
                        GenericArguments.optional(GenericArguments.string(Text.of("player|page"))),
                        GenericArguments.optional(GenericArguments.integer(Text.of("page")))
                )
                .permission("redprotect.command.list")
                .executor((src, args) -> {
                    if (!(src instanceof Player) && args.hasAny("player|page")) {
                        if (args.hasAny("page")) {
                            int Page = args.<Integer>getOne("page").get();
                            getRegionforList(src, args.<String>getOne("player|page").get(), Page);
                            return CommandResult.success();
                        } else {
                            getRegionforList(src, args.<String>getOne("player|page").get(), 1);
                            return CommandResult.success();
                        }
                    } else if (src instanceof Player) {
                        Player player = (Player) src;

                        if (args.hasAny("player|page")) {
                            String playerPage = args.<String>getOne("player|page").get();

                            try {
                                int Page = Integer.parseInt(playerPage);
                                handleList(player, RedProtect.get().getUtil().PlayerToUUID(player.getName()), Page);
                            } catch (NumberFormatException e) {

                                int page = 1;
                                if (args.hasAny("page")) {
                                    page = args.<Integer>getOne("page").get();
                                }
                                handleList(player, RedProtect.get().getUtil().PlayerToUUID(playerPage), page);
                            }

                            return CommandResult.success();
                        } else {
                            handleList(player, RedProtect.get().getUtil().PlayerToUUID(player.getName()), 1);
                            return CommandResult.success();
                        }
                    }

                    RedProtect.get().lang.sendCommandHelp(src, "list", true);
                    return CommandResult.success();
                }).build();
    }
}
