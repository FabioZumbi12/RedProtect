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

package br.net.fabiozumbi12.RedProtect.Sponge.commands.SubCommands.PlayerHandlers;

import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import br.net.fabiozumbi12.RedProtect.Sponge.Region;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.storage.WorldProperties;

import static br.net.fabiozumbi12.RedProtect.Sponge.commands.CommandHandlers.handleAddMember;

public class AddMemberCommand {

    public CommandSpec register() {
        return CommandSpec.builder()
                .description(Text.of("Command to add members to regions."))
                .arguments(GenericArguments.string(Text.of("player")),
                        GenericArguments.optional(GenericArguments.requiringPermission(GenericArguments.string(Text.of("region")), "redprotect.command.admin.addmember")),
                        GenericArguments.optional(GenericArguments.requiringPermission(GenericArguments.world(Text.of("world")), "redprotect.command.admin.addmember")))
                .permission("redprotect.command.addmember")
                .executor((src, args) -> {
                    if (args.hasAny("region") && args.hasAny("world")) {
                        String region = args.<String>getOne("region").get();
                        WorldProperties worldProperties = args.<WorldProperties>getOne("world").get();

                        if (!RedProtect.get().getServer().getWorld(worldProperties.getWorldName()).isPresent()) {
                            src.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("cmdmanager.region.invalidworld")));
                            return CommandResult.success();
                        }
                        Region r = RedProtect.get().rm.getRegion(region, worldProperties.getWorldName());
                        if (r == null) {
                            src.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("cmdmanager.region.doesntexist") + ": " + region));
                            return CommandResult.success();
                        }
                        handleAddMember(src, args.<String>getOne("player").get(), r);
                        return CommandResult.success();
                    } else if (src instanceof Player) {
                        Player player = (Player) src;
                        handleAddMember(player, args.<String>getOne("player").get(), null);
                        return CommandResult.success();
                    }

                    RedProtect.get().lang.sendCommandHelp(src, "addmember", true);
                    return CommandResult.success();
                }).build();
    }
}
