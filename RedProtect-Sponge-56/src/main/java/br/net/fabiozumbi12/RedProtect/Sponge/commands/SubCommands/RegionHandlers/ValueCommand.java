/*
 *  Copyright (c) 2019 - @FabioZumbi12
 *  Last Modified: 16/04/19 06:22
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

package br.net.fabiozumbi12.RedProtect.Sponge.commands.SubCommands.RegionHandlers;

import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import br.net.fabiozumbi12.RedProtect.Sponge.Region;
import br.net.fabiozumbi12.RedProtect.Sponge.config.RPLang;
import br.net.fabiozumbi12.RedProtect.Sponge.helpers.LogLevel;
import br.net.fabiozumbi12.RedProtect.Sponge.helpers.RPEconomy;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import static br.net.fabiozumbi12.RedProtect.Sponge.commands.CommandHandlers.HandleHelpPage;

public class ValueCommand {

    public CommandSpec register() {
        return CommandSpec.builder()
                .description(Text.of("Command to get the value of a region."))
                .permission("redprotect.command.value")
                .executor((src, args) -> {
                    if (!(src instanceof Player)) {
                        HandleHelpPage(src, 1);
                    } else {
                        Player player = (Player) src;

                        Region r = RedProtect.get().rm.getTopRegion(player.getLocation(), this.getClass().getName());
                        if (r != null) {
                            if (RedProtect.get().ph.hasRegionPermLeader(player, "value", r)) {
                                if (r.getArea() < RedProtect.get().cfgs.getEcoInt("max-area-toget-value")) {
                                    RPLang.sendMessage(player, RPLang.get("cmdmanager.value.is").replace("{value}", RPEconomy.getFormatted(RPEconomy.getRegionValue(r)) + " " + RedProtect.get().cfgs.getEcoString("economy-name")));
                                    RedProtect.get().logger.debug(LogLevel.PLAYER, "Region Value: " + RPEconomy.getRegionValue(r));
                                } else {
                                    RPLang.sendMessage(player, RPLang.get("cmdmanager.value.areabig").replace("{maxarea}", RedProtect.get().cfgs.getEcoInt("max-area-toget-value").toString()));
                                }
                            } else {
                                RPLang.sendMessage(player, "playerlistener.region.cantuse");
                            }
                        } else {
                            RPLang.sendMessage(player, "cmdmanager.region.todo.that");
                        }
                    }
                    return CommandResult.success();
                }).build();
    }
}
