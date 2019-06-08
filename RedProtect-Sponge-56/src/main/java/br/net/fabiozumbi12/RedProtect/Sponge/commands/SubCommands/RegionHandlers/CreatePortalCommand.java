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
import br.net.fabiozumbi12.RedProtect.Sponge.region.RegionBuilder;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.storage.WorldProperties;

import java.text.Normalizer;
import java.util.HashSet;

import static br.net.fabiozumbi12.RedProtect.Sponge.commands.CommandHandlers.HandleHelpPage;

public class CreatePortalCommand {

    public CommandSpec register() {
        return CommandSpec.builder()
                .description(Text.of("Command to create a portal based on regions."))
                .arguments(
                        GenericArguments.string(Text.of("regionNameFrom")),
                        GenericArguments.string(Text.of("regionNameTo")),
                        GenericArguments.world(Text.of("world"))
                )
                .permission("redprotect.command.createportal")
                .executor((src, args) -> {
                    if (!(src instanceof Player)) {
                        HandleHelpPage(src, 1);
                    } else {
                        Player player = (Player) src;

                        WorldProperties w = args.<WorldProperties>getOne("world").get();
                        String regionFrom = args.<String>getOne("regionNameFrom").get();
                        String regionTo = args.<String>getOne("regionNameTo").get();

                        Region r = RedProtect.get().rm.getRegion(regionTo, w.getWorldName());
                        if (r == null) {
                            RedProtect.get().lang.sendMessage(player, RedProtect.get().lang.get("cmdmanager.createportal.warning").replace("{region}", regionTo));
                        }

                        PlayerRegion serverName = new PlayerRegion(RedProtect.get().config.configRoot().region_settings.default_leader, RedProtect.get().config.configRoot().region_settings.default_leader);
                        String name = Normalizer.normalize(regionFrom.replace(" ", "_"), Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "").replaceAll("[^\\p{L}0-9 ]", "");

                        Region r2 = RedProtect.get().rm.getRegion(name, w.getWorldName());

                        if (r2 != null) {
                            if (!r2.isLeader(player) || !r2.isAdmin(player)) {
                                RedProtect.get().lang.sendMessage(player, "no.permission");
                                return CommandResult.success();
                            }
                            RedProtect.get().lang.sendMessage(player, String.format(RedProtect.get().lang.get("cmdmanager.region.portalcreated"), name, regionTo, w.getWorldName()));
                            RedProtect.get().lang.sendMessage(player, "cmdmanager.region.portalhint");
                            r2.setFlag(RedProtect.get().getVersionHelper().getCause(src), "set-portal", regionTo + " " + w.getWorldName());

                            RedProtect.get().logger.addLog("(World " + r2.getWorld() + ") Player " + player.getName() + " CREATED A PORTAL " + r2.getName() + " to " + regionTo + " world " + w.getWorldName());
                        } else {
                            RegionBuilder rb2 = new DefineRegionBuilder(player, RedProtect.get().firstLocationSelections.get(player), RedProtect.get().secondLocationSelections.get(player), name, serverName, new HashSet<>(), true);
                            if (rb2.ready()) {
                                r2 = rb2.build();
                                RedProtect.get().lang.sendMessage(player, String.format(RedProtect.get().lang.get("cmdmanager.region.portalcreated"), name, regionTo, w.getWorldName()));
                                RedProtect.get().lang.sendMessage(player, "cmdmanager.region.portalhint");

                                r2.setFlag(RedProtect.get().getVersionHelper().getCause(src), "set-portal", regionTo + " " + w.getWorldName());
                                RedProtect.get().rm.add(r2, player.getWorld().getName());

                                RedProtect.get().firstLocationSelections.remove(player);
                                RedProtect.get().secondLocationSelections.remove(player);

                                RedProtect.get().logger.addLog("(World " + r2.getWorld() + ") Player " + player.getName() + " CREATED A PORTAL " + r2.getName() + " to " + regionTo + " world " + w.getWorldName());
                            }
                        }
                    }
                    return CommandResult.success();
                }).build();
    }
}
