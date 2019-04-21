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

package br.net.fabiozumbi12.RedProtect.Sponge.commands.SubCommands.PlayerHandlers;

import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import br.net.fabiozumbi12.RedProtect.Sponge.Region;
import br.net.fabiozumbi12.RedProtect.Sponge.config.RPLang;
import br.net.fabiozumbi12.RedProtect.Sponge.helpers.RPUtil;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.World;

import static br.net.fabiozumbi12.RedProtect.Sponge.commands.CommandHandlers.HandleHelpPage;

public class KickCommand {

    public CommandSpec register() {
        return CommandSpec.builder()
                .description(Text.of("Command to kick players from regions."))
                .arguments(
                        GenericArguments.player(Text.of("player")),
                        GenericArguments.optional(GenericArguments.string(Text.of("region"))),
                        GenericArguments.optional(GenericArguments.world(Text.of("world"))))
                .permission("redprotect.command.kick")
                .executor((src, args) -> {
                    if (!(src instanceof Player)) {
                        HandleHelpPage(src, 1);
                    } else {
                        Player player = (Player) src;

                        Region r = RedProtect.get().rm.getTopRegion(player.getLocation(), this.getClass().getName());

                        if (r == null) {
                            RPLang.sendMessage(player, "cmdmanager.region.todo.that");
                            return CommandResult.success();
                        }

                        if (args.hasAny("region") && args.hasAny("world")) {
                            r = RedProtect.get().rm.getRegion(args.<String>getOne("regions").get(), args.<World>getOne("world").get().getName());
                            if (r == null) {
                                RPLang.sendMessage(player, "cmdmanager.region.todo.that");
                                return CommandResult.success();
                            }
                        }

                        if (!RedProtect.get().ph.hasRegionPermMember(player, "kick", r)) {
                            RPLang.sendMessage(player, "no.permission");
                            return CommandResult.success();
                        }

                        Player visit = args.<Player>getOne("player").get();

                        if (r.canBuild(visit)) {
                            RPLang.sendMessage(player, "cmdmanager.cantkick.member");
                            return CommandResult.success();
                        }

                        Region rv = RedProtect.get().rm.getTopRegion(visit.getLocation(), this.getClass().getName());
                        if (rv == null || !rv.getID().equals(r.getID())) {
                            RPLang.sendMessage(player, "cmdmanager.noplayer.thisregion");
                            return CommandResult.success();
                        }

                        String sec = String.valueOf(RedProtect.get().config.configRoot().region_settings.delay_after_kick_region);
                        if (RedProtect.get().denyEnterRegion(r.getID(), visit.getName())) {
                            RPUtil.DenyEnterPlayer(visit.getWorld(), new Transform<>(visit.getLocation()), new Transform<>(visit.getLocation()), r, true);
                            RPLang.sendMessage(player, RPLang.get("cmdmanager.region.kicked").replace("{player}", visit.getName()).replace("{region}", r.getName()).replace("{time}", sec));
                        } else {
                            RPLang.sendMessage(player, RPLang.get("cmdmanager.already.cantenter").replace("{time}", sec));
                        }
                    }
                    return CommandResult.success();
                }).build();
    }

}
