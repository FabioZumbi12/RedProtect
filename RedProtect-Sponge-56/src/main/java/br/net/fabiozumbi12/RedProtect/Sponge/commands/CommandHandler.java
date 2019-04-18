/*
 *  Copyright (c) 2019 - @FabioZumbi12
 *  Last Modified: 16/04/19 06:21
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

package br.net.fabiozumbi12.RedProtect.Sponge.commands;

import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import br.net.fabiozumbi12.RedProtect.Sponge.commands.SubCommands.AdminCommand;
import br.net.fabiozumbi12.RedProtect.Sponge.commands.SubCommands.PlayerHandlers.*;
import br.net.fabiozumbi12.RedProtect.Sponge.commands.SubCommands.RegionHandlers.*;
import br.net.fabiozumbi12.RedProtect.Sponge.config.RPLang;
import br.net.fabiozumbi12.RedProtect.Sponge.helpers.RPUtil;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

import java.util.Arrays;

import static br.net.fabiozumbi12.RedProtect.Sponge.commands.CommandHandlers.getCmd;
import static br.net.fabiozumbi12.RedProtect.Sponge.commands.CommandHandlers.getCmdAlias;

public class CommandHandler {

    private final RedProtect plugin;

    public CommandHandler(RedProtect plugin) {
        this.plugin = plugin;

        CommandSpec redProtect = CommandSpec.builder()
                .description(Text.of("Main command for RedProtect."))
                .executor((src, args) -> {
                    src.sendMessage(RPUtil.toText(RPLang.get("general.color") + "---------------- " + RedProtect.get().container.getName() + " ----------------"));
                    src.sendMessage(RPUtil.toText(RPLang.get("general.color") + "Developed by &eFabioZumbi12" + RPLang.get("general.color") + "."));
                    src.sendMessage(RPUtil.toText(RPLang.get("general.color") + "For more information about the commands, type [&e/rp " + getCmd("help") + RPLang.get("general.color") + "]."));
                    src.sendMessage(RPUtil.toText(RPLang.get("general.color") + "For a tutorial, type [&e/rp " + getCmd("tutorial") + RPLang.get("general.color") + "]."));
                    src.sendMessage(RPUtil.toText(RPLang.get("general.color") + "---------------------------------------------------"));
                    return CommandResult.success();
                })
                //player handlers
                .child(new AddAdminCommand().register(), getCmdKeys("addadmin"))
                .child(new AddLeaderCommand().register(), getCmdKeys("addleader"))
                .child(new AddMemberCommand().register(), getCmdKeys("addmember"))
                .child(new RemoveMemberCommand().register(), getCmdKeys("removemember"))
                .child(new RemoveAdminCommand().register(), getCmdKeys("removeadmin"))
                .child(new RemoveLeaderCommand().register(), getCmdKeys("removeleader"))
                .child(new BlockLimitCommand().register(), getCmdKeys("blocklimit"))
                .child(new ClaimLimitCommand().register(), getCmdKeys("claimlimit"))
                .child(new HelpCommand().register(), getCmdKeys("help"))
                .child(new InfoCommand().register(), getCmdKeys("info"))
                .child(new KickCommand().register(), getCmdKeys("kick"))
                .child(new LAcceptCommand().register(), getCmdKeys("laccept"))
                .child(new LDenyCommand().register(), getCmdKeys("ldeny"))
                .child(new NearCommand().register(), getCmdKeys("near"))
                .child(new RegenAllCommand().register(), getCmdKeys("regen-all"))
                .child(new RegenCommand().register(), getCmdKeys("regen"))
                .child(new RemoveAllCommand().register(), getCmdKeys("remove-all"))
                .child(new StartCommand().register(), getCmdKeys("start"))
                .child(new TutorialCommand().register(), getCmdKeys("tutorial"))
                .child(new WandCommand().register(), getCmdKeys("wand"))

                //region handlers
                .child(new BorderCommand().register(), getCmdKeys("border"))
                .child(new ClaimCommand().register(), getCmdKeys("claim"))
                .child(new CopyFlagCommand().register(), getCmdKeys("copyflag"))
                .child(new CreatePortalCommand().register(), getCmdKeys("createportal"))
                .child(new DefineCommand().register(), getCmdKeys("define"))
                .child(new DeleteCommand().register(), getCmdKeys("delete"))
                .child(new DelTpCommand().register(), getCmdKeys("deltp"))
                .child(new ExpandVertCommand().register(), getCmdKeys("expand-vert"))
                .child(new FlagCommand().register(), getCmdKeys("flag"))
                .child(new ListCommand().register(), getCmdKeys("list"))
                .child(new Pos1Command().register(), getCmdKeys("pos1"))
                .child(new Pos2Command().register(), getCmdKeys("pos2"))
                .child(new PriorityCommand().register(), getCmdKeys("priority"))
                .child(new RedefineCommand().register(), getCmdKeys("redefine"))
                .child(new RenameCommand().register(), getCmdKeys("rename"))
                .child(new SelectWECommand().register(), getCmdKeys("select-we"))
                .child(new SetMaxYCommand().register(), getCmdKeys("setmaxy"))
                .child(new SetMinYCommand().register(), getCmdKeys("setminy"))
                .child(new SetTpCommand().register(), getCmdKeys("settp"))
                .child(new TeleportCommand().register(), getCmdKeys("teleport"))
                .child(new ValueCommand().register(), getCmdKeys("value"))
                .child(new WelcomeCommand().register(), getCmdKeys("welcome"))

                .child(new AdminCommand(), getCmdKeys("admin"))

                .build();

        plugin.commandManager.register(plugin, redProtect, Arrays.asList("redprotect", "rp"));
    }

    public void unregisterAll() {
        plugin.commandManager.getOwnedBy(plugin.container).forEach(p -> plugin.commandManager.removeMapping(p));
    }

    private String[] getCmdKeys(String cmd) {
        /*if (getCmd(cmd).equalsIgnoreCase(cmd))
            return new String[]{getCmd(cmd), getCmdAlias(cmd)};*/
        return new String[]{getCmd(cmd), getCmdAlias(cmd)};
    }
}
