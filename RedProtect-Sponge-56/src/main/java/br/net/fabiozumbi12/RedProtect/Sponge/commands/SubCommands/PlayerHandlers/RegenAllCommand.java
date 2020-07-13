/*
 * Copyright (c) 2020 - @FabioZumbi12
 * Last Modified: 02/07/2020 18:59.
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
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

public class RegenAllCommand {

    public CommandSpec register() {
        return CommandSpec.builder()
                .arguments(GenericArguments.string(Text.of("player")))
                .description(Text.of("Command to regenerate all player regions."))
                .permission("redprotect.command.regenall")
                .executor((src, args) -> {
                    if (!RedProtect.get().hooks.WE) {
                        return CommandResult.success();
                    }
                    int regen = RedProtect.get().getRegionManager().regenAll(args.<String>getOne("player").get());
                    if (regen <= 0) {
                        RedProtect.get().getLanguageManager().sendMessage(src, RedProtect.get().getLanguageManager().get("cmdmanager.region.noneregenerated"));
                    } else {
                        RedProtect.get().getLanguageManager().sendMessage(src, RedProtect.get().getLanguageManager().get("cmdmanager.region.regenerated").replace("{regions}", regen + "").replace("{player}", args.<String>getOne("player").get()));
                    }
                    return CommandResult.success();
                }).build();
    }

}
