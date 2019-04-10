/*
 *
 * Copyright (c) 2019 - @FabioZumbi12
 * Last Modified: 01/04/19 02:35
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
import br.net.fabiozumbi12.RedProtect.Sponge.region.SpongeRegion;
import br.net.fabiozumbi12.RedProtect.Sponge.actions.RedefineRegionBuilder;
import br.net.fabiozumbi12.RedProtect.Sponge.commands.RegionCommandElement;
import br.net.fabiozumbi12.RedProtect.Sponge.config.RPLang;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import static br.net.fabiozumbi12.RedProtect.Sponge.commands.CommandHandlers.HandleHelpPage;

public class RedefineCommand {

    public CommandSpec register() {
        return CommandSpec.builder()
                .description(Text.of("Command to redefine a region."))
                .arguments(new RegionCommandElement(Text.of("region")))
                .permission("redprotect.command.redefine")
                .executor((src, args) -> {
                    if (!(src instanceof Player)) {
                        HandleHelpPage(src, 1);
                    } else {
                        Player player = (Player)src;

                        SpongeRegion oldRect = args.<SpongeRegion>getOne("region").get();
                        /*if (oldRect == null) {
                            RPLang.sendMessage(player, RPLang.get("cmdmanager.region.doesntexist") + ": " + region);
                            return CommandResult.success();
                        }*/

                        if (!RedProtect.get().ph.hasRegionPermLeader(player, "redefine", oldRect)) {
                            RPLang.sendMessage(player, "playerlistener.region.cantuse");
                            return CommandResult.success();
                        }

                        RedefineRegionBuilder rb = new RedefineRegionBuilder(player, oldRect, RedProtect.get().firstLocationSelections.get(player), RedProtect.get().secondLocationSelections.get(player));
                        if (rb.ready()) {
                            SpongeRegion r2 = rb.build();
                            RPLang.sendMessage(player, RPLang.get("cmdmanager.region.redefined") + " " + r2.getName() + ".");
                            RedProtect.get().rm.add(r2, player.getWorld());

                            RedProtect.get().firstLocationSelections.remove(player);
                            RedProtect.get().secondLocationSelections.remove(player);

                            RedProtect.get().logger.addLog("(World " + r2.getWorld() + ") Player " + player.getName() + " REDEFINED region " + r2.getName());
                        }
                    }
                    return CommandResult.success();
                }).build();
    }
}
