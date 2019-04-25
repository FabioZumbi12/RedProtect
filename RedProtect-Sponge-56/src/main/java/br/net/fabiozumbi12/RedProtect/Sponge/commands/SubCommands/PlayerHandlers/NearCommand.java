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
import br.net.fabiozumbi12.RedProtect.Sponge.config.LangManager;
import br.net.fabiozumbi12.RedProtect.Sponge.helpers.RPUtil;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.Iterator;
import java.util.Set;

import static br.net.fabiozumbi12.RedProtect.Sponge.commands.CommandHandlers.HandleHelpPage;

public class NearCommand {

    public CommandSpec register() {
        return CommandSpec.builder()
                .description(Text.of("Command to check for regions near."))
                .permission("redprotect.command.near")
                .executor((src, args) -> {
                    if (!(src instanceof Player)) {
                        HandleHelpPage(src, 1);
                    } else {
                        Player player = (Player) src;

                        Set<Region> regions = RedProtect.get().rm.getRegionsNear(player, 60, player.getWorld());
                        if (regions.size() == 0) {
                            RedProtect.get().lang.sendMessage(player, "cmdmanager.noregions.nearby");
                        } else {
                            Iterator<Region> i = regions.iterator();
                            player.sendMessage(RPUtil.toText(RedProtect.get().lang.get("cmdmanager.regionwith40")));
                            player.sendMessage(RPUtil.toText(RedProtect.get().lang.get("general.color") + "------------------------------------"));
                            while (i.hasNext()) {
                                Region r = i.next();
                                player.sendMessage(RPUtil.toText(RedProtect.get().lang.get("cmdmanager.region.name") + r.getName() + RedProtect.get().lang.get("general.color") + " | " + RedProtect.get().lang.get("region.center") + " (&6X,Z" + RedProtect.get().lang.get("general.color") + "): &6" + r.getCenterX() + ", " + r.getCenterZ()));
                            }
                            player.sendMessage(RPUtil.toText(RedProtect.get().lang.get("general.color") + "------------------------------------"));
                        }
                    }
                    return CommandResult.success();
                }).build();
    }

}