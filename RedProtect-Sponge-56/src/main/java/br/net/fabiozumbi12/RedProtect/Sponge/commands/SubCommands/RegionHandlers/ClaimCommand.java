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

import br.net.fabiozumbi12.RedProtect.Core.region.PlayerRegion;
import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import br.net.fabiozumbi12.RedProtect.Sponge.Region;
import br.net.fabiozumbi12.RedProtect.Sponge.actions.DefineRegionBuilder;
import br.net.fabiozumbi12.RedProtect.Sponge.helpers.RedProtectUtil;
import br.net.fabiozumbi12.RedProtect.Sponge.region.RegionBuilder;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.HashSet;
import java.util.Set;

import static br.net.fabiozumbi12.RedProtect.Sponge.commands.CommandHandlers.HandleHelpPage;

public class ClaimCommand {

    public CommandSpec register() {
        return CommandSpec.builder()
                .description(Text.of("Command to claim a region."))
                .arguments(
                        GenericArguments.optional(GenericArguments.string(Text.of("regionName"))),
                        GenericArguments.optional(GenericArguments.string(Text.of("playerAdmin")))
                )
                .permission("redprotect.command.claim")
                .executor((src, args) -> {
                    if (!(src instanceof Player)) {
                        HandleHelpPage(src, 1);
                    } else {
                        Player player = (Player) src;

                        String claimmode = RedProtect.get().config.getWorldClaimType(player.getWorld().getName());
                        if (!claimmode.equalsIgnoreCase("WAND") && !claimmode.equalsIgnoreCase("BOTH")) {
                            RedProtect.get().lang.sendMessage(player, "blocklistener.region.blockmode");
                            return CommandResult.success();
                        }

                        String name = RedProtect.get().getUtil().nameGen(player.getName(), player.getWorld().getName());
                        if (args.hasAny("regionName")) {
                            name = args.<String>getOne("regionName").get();
                        }

                        Set<PlayerRegion> addedAdmins = new HashSet<>();

                        if (args.hasAny("playerAdmin"))
                            addedAdmins.add(new PlayerRegion(RedProtect.get().getUtil().PlayerToUUID(args.<String>getOne("playerAdmin").get()), args.<String>getOne("playerAdmin").get()));

                        RegionBuilder rb2 = new DefineRegionBuilder(player, RedProtect.get().firstLocationSelections.get(player), RedProtect.get().secondLocationSelections.get(player), name, new PlayerRegion(player.getUniqueId().toString(), player.getName()), addedAdmins, false);
                        if (rb2.ready()) {
                            Region r2 = rb2.build();
                            RedProtect.get().lang.sendMessage(player, RedProtect.get().lang.get("cmdmanager.region.created") + " " + r2.getName() + ".");
                            RedProtect.get().rm.add(r2, player.getWorld().getName());

                            RedProtect.get().firstLocationSelections.remove(player);
                            RedProtect.get().secondLocationSelections.remove(player);

                            RedProtect.get().logger.addLog("(World " + r2.getWorld() + ") Player " + player.getName() + " CLAIMED region " + r2.getName());
                        }
                    }
                    return CommandResult.success();
                }).build();
    }
}
