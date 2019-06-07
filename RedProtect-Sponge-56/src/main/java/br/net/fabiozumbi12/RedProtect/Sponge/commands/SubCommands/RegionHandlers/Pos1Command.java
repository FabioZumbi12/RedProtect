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
import br.net.fabiozumbi12.RedProtect.Sponge.helpers.RedProtectUtil;
import br.net.fabiozumbi12.RedProtect.Sponge.hooks.WEHook;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import static br.net.fabiozumbi12.RedProtect.Sponge.commands.CommandHandlers.HandleHelpPage;

public class Pos1Command {

    public CommandSpec register() {
        return CommandSpec.builder()
                .description(Text.of("Command to set selected points."))
                .permission("redprotect.command.pos1")
                .executor((src, args) -> {
                    if (!(src instanceof Player)) {
                        HandleHelpPage(src, 1);
                    } else {
                        Player player = (Player) src;

                        String claimmode = RedProtect.get().config.getWorldClaimType(player.getWorld().getName());
                        if (!claimmode.equalsIgnoreCase("WAND") && !claimmode.equalsIgnoreCase("BOTH") && !RedProtect.get().ph.hasCommandPerm(player, "redefine")) {
                            return CommandResult.success();
                        }

                        Location<World> pl = player.getLocation();
                        RedProtect.get().firstLocationSelections.put(player, pl);
                        player.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("playerlistener.wand1") + RedProtect.get().lang.get("general.color") + " (&6" + pl.getBlockX() + RedProtect.get().lang.get("general.color") + ", &6" + pl.getBlockY() + RedProtect.get().lang.get("general.color") + ", &6" + pl.getBlockZ() + RedProtect.get().lang.get("general.color") + ")."));

                        //show preview border
                        if (RedProtect.get().firstLocationSelections.containsKey(player) && RedProtect.get().secondLocationSelections.containsKey(player)) {
                            Location<World> loc1 = RedProtect.get().firstLocationSelections.get(player);
                            Location<World> loc2 = RedProtect.get().secondLocationSelections.get(player);
                            if (RedProtect.get().hooks.WE && RedProtect.get().config.configRoot().hooks.useWECUI) {
                                WEHook.setSelectionRP(player, loc1, loc2);
                            }

                            if (loc1.getPosition().distanceSquared(loc2.getPosition()) > RedProtect.get().config.configRoot().region_settings.wand_max_distance && !player.hasPermission("redprotect.bypass.define-max-distance")) {
                                double dist = loc1.getPosition().distanceSquared(loc2.getPosition());
                                RedProtect.get().lang.sendMessage(player, String.format(RedProtect.get().lang.get("regionbuilder.selection.maxdefine"), RedProtect.get().config.configRoot().region_settings.wand_max_distance, (int) dist));
                            } else {
                                RedProtect.get().getUtil().addBorder(player, loc1, loc2);
                            }
                        }
                    }
                    return CommandResult.success();
                }).build();
    }
}
