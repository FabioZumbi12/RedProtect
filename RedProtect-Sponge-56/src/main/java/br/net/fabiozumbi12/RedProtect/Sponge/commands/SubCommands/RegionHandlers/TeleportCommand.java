/*
 * Copyright (c) 2020 - @FabioZumbi12
 * Last Modified: 08/06/2019 04:44.
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

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.storage.WorldProperties;

import static br.net.fabiozumbi12.RedProtect.Sponge.commands.CommandHandlers.handletp;

public class TeleportCommand {

    public CommandSpec register() {
        return CommandSpec.builder()
                .description(Text.of("Command to teleport to a region."))
                .arguments(
                        GenericArguments.string(Text.of("region")),
                        GenericArguments.optional(GenericArguments.world(Text.of("world"))),
                        GenericArguments.optional(GenericArguments.player(Text.of("player")))
                )
                .permission("redprotect.command.teleport")
                .executor((src, args) -> {
                    if (args.hasAny("player")) {
                        Player play = args.<Player>getOne("player").get();
                        WorldProperties world = args.<WorldProperties>getOne("world").get();
                        String region = args.<String>getOne("region").get();

                        handletp(src, region, Sponge.getServer().getWorld(world.getUniqueId()).get(), play);
                    } else if (args.hasAny("world")) {
                        String region = args.<String>getOne("region").get();
                        WorldProperties world = args.<WorldProperties>getOne("world").get();

                        handletp(src, region, Sponge.getServer().getWorld(world.getUniqueId()).get(), null);
                    } else if (src instanceof Player) {
                        String region = args.<String>getOne("region").get();

                        handletp(src, region, ((Player) src).getWorld(), null);
                    }
                    return CommandResult.success();
                }).build();
    }
}
