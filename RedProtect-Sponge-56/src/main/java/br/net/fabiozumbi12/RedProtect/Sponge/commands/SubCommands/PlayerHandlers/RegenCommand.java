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
import br.net.fabiozumbi12.RedProtect.Sponge.helpers.RedProtectUtil;
import br.net.fabiozumbi12.RedProtect.Sponge.hooks.WEHook;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.World;

import java.util.Optional;

import static br.net.fabiozumbi12.RedProtect.Sponge.commands.CommandHandlers.HandleHelpPage;

public class RegenCommand {

    public CommandSpec register() {
        return CommandSpec.builder()
                .description(Text.of("Command to regenerate all player regions."))
                .arguments(
                        GenericArguments.optional(GenericArguments.string(Text.of("region"))),
                        GenericArguments.optional(GenericArguments.string(Text.of("world")))
                )
                .permission("redprotect.command.regen")
                .executor((src, args) -> {
                    if (!(src instanceof Player) && args.hasAny("region") && args.hasAny("world")) {
                        if (!RedProtect.get().hooks.WE) {
                            return CommandResult.success();
                        }
                        Optional<World> w = RedProtect.get().getServer().getWorld(args.<String>getOne("world").get());
                        if (!w.isPresent()) {
                            RedProtect.get().lang.sendMessage(src, RedProtect.get().lang.get("cmdmanager.region.invalidworld"));
                            return CommandResult.success();
                        }
                        Region r = RedProtect.get().rm.getRegion(args.<String>getOne("region").get(), w.get().getName());
                        if (r == null) {
                            RedProtect.get().lang.sendMessage(src, RedProtect.get().lang.get("correct.usage") + " &eInvalid region: " + args.<String>getOne("region").get());
                            return CommandResult.success();
                        }

                        WEHook.regenRegion(r, w.get(), r.getMaxLocation(), r.getMinLocation(), 0, src, false);
                        return CommandResult.success();
                    } else if (src instanceof Player) {
                        Player player = (Player) src;

                        if (!RedProtect.get().hooks.WE) {
                            return CommandResult.success();
                        }

                        Region r = RedProtect.get().rm.getTopRegion(player.getLocation(), this.getClass().getName());
                        if (r == null) {
                            RedProtect.get().lang.sendMessage(player, "cmdmanager.region.doesexists");
                            return CommandResult.success();
                        }

                        WEHook.regenRegion(r, Sponge.getServer().getWorld(r.getWorld()).get(), r.getMaxLocation(), r.getMinLocation(), 0, player, false);
                        return CommandResult.success();
                    }

                    RedProtect.get().lang.sendCommandHelp(src, "regen", true);
                    return CommandResult.success();
                })

                //rp regen stop
                .child(CommandSpec.builder()
                        .executor((src, args) -> {
                            if (!RedProtect.get().hooks.WE) {
                                return CommandResult.success();
                            }

                            RedProtectUtil.stopRegen = true;
                            RedProtect.get().lang.sendMessage(src, "&aRegen will stop now. To continue reload the plugin!");
                            return CommandResult.success();
                        }).build(), "stop")
                .build();
    }

}

