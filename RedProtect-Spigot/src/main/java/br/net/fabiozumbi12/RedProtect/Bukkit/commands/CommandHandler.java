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

package br.net.fabiozumbi12.RedProtect.Bukkit.commands;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommands.AdminCommand;
import br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommands.PlayerHandlers.*;
import br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommands.RegionHandlers.*;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.LangManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.lang.reflect.Array;
import java.util.*;

import static br.net.fabiozumbi12.RedProtect.Bukkit.commands.CommandHandlers.getCmd;
import static br.net.fabiozumbi12.RedProtect.Bukkit.commands.CommandHandlers.getCmdAlias;

public class CommandHandler implements CommandExecutor, TabCompleter {

    private final RedProtect plugin;
    private HashMap<List<String>, SubCommand> commandMap = new HashMap<>();

    public CommandHandler(RedProtect plugin) {
        this.plugin = plugin;

        //player handlers
        registerCommand(getCmdKeys("addadmin"), new AddAdminCommand());
        registerCommand(getCmdKeys("addleader"), new AddLeaderCommand());
        registerCommand(getCmdKeys("addmember"), new AddMemberCommand());
        registerCommand(getCmdKeys("removemember"), new RemoveMemberCommand());
        registerCommand(getCmdKeys("removeadmin"), new RemoveAdminCommand());
        registerCommand(getCmdKeys("removeleader"), new RemoveLeaderCommand());
        registerCommand(getCmdKeys("blocklimit"), new BlockLimitCommand());
        registerCommand(getCmdKeys("claimlimit"), new ClaimLimitCommand());
        registerCommand(getCmdKeys("help"), new HelpCommand());
        registerCommand(getCmdKeys("info"), new InfoCommand());
        registerCommand(getCmdKeys("kick"), new KickCommand());
        registerCommand(getCmdKeys("laccept"), new LAcceptCommand());
        registerCommand(getCmdKeys("ldeny"), new LDenyCommand());
        registerCommand(getCmdKeys("near"), new NearCommand());
        registerCommand(getCmdKeys("regen-all"), new RegenAllCommand());
        registerCommand(getCmdKeys("regen"), new RegenCommand());
        registerCommand(getCmdKeys("remove-all"), new RemoveAllCommand());
        registerCommand(getCmdKeys("start"), new StartCommand());
        registerCommand(getCmdKeys("tutorial"), new TutorialCommand());
        registerCommand(getCmdKeys("wand"), new WandCommand());

        //region handlers
        registerCommand(getCmdKeys("border"), new BorderCommand());
        registerCommand(getCmdKeys("claim"), new ClaimCommand());
        registerCommand(getCmdKeys("copyflag"), new CopyFlagCommand());
        registerCommand(getCmdKeys("createportal"), new CreatePortalCommand());
        registerCommand(getCmdKeys("define"), new DefineCommand());
        registerCommand(getCmdKeys("delete"), new DeleteCommand());
        registerCommand(getCmdKeys("deltp"), new DelTpCommand());
        registerCommand(getCmdKeys("expand-vert"), new ExpandVertCommand());
        registerCommand(getCmdKeys("flag"), new FlagCommand());
        registerCommand(getCmdKeys("list"), new ListCommand());
        registerCommand(getCmdKeys("pos1"), new Pos1Command());
        registerCommand(getCmdKeys("pos2"), new Pos2Command());
        registerCommand(getCmdKeys("priority"), new PriorityCommand());
        registerCommand(getCmdKeys("redefine"), new RedefineCommand());
        registerCommand(getCmdKeys("rename"), new RenameCommand());
        registerCommand(getCmdKeys("select-we"), new SelectWECommand());
        registerCommand(getCmdKeys("setmaxy"), new SetMaxYCommand());
        registerCommand(getCmdKeys("setminy"), new SetMinYCommand());
        registerCommand(getCmdKeys("settp"), new SetTpCommand());
        registerCommand(getCmdKeys("teleport"), new TeleportCommand());
        registerCommand(getCmdKeys("value"), new ValueCommand());
        registerCommand(getCmdKeys("welcome"), new WelcomeCommand());

        //general commands
        registerCommand(getCmdKeys("admin"), new AdminCommand());

        plugin.getCommand("redprotect").setExecutor(this);
        plugin.getCommand("redprotect").setTabCompleter(this);
    }

    private static <T> T[] Arrays_copyOfRange(T[] original, int end) {
        int start = 1;
        if (original.length >= start) {
            if (start <= end) {
                int length = end - start;
                int copyLength = Math.min(length, original.length - start);
                T[] copy = (T[]) Array.newInstance(original.getClass().getComponentType(), length);

                System.arraycopy(original, start, copy, 0, copyLength);
                return copy;
            }
            throw new IllegalArgumentException();
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    private List<String> getCmdKeys(String cmd) {
        if (getCmd(cmd).equalsIgnoreCase(cmd))
            return Arrays.asList(getCmd(cmd), getCmdAlias(cmd));
        return Arrays.asList(cmd, getCmd(cmd), getCmdAlias(cmd));
    }

    private void registerCommand(List<String> command, SubCommand commandExecutor) {
        this.commandMap.put(command, commandExecutor);
    }

    public void unregisterAll() {
        plugin.getCommand("redprotect").unregister(null);
    }

    private SubCommand getCommandSubCommand(String cmd) {
        return this.commandMap.entrySet().stream().filter(k -> k.getKey().contains(cmd)).findFirst().get().getValue();
    }

    private String getCmdFromAlias(String alias) {
        return this.commandMap.keySet().stream().filter(k -> k.contains(alias)).findFirst().get().get(0);
    }

    private boolean hasCommand(String cmd) {
        return this.commandMap.keySet().stream().anyMatch(k -> k.contains(cmd));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && hasCommand(args[0])) {
            CommandExecutor executor = this.getCommandSubCommand(args[0]);
            if (!RedProtect.get().ph.hasCommandPerm(sender, getCmdFromAlias(args[0]))) {
                RedProtect.get().lang.sendMessage(sender, "no.permission");
                return true;
            }
            return executor.onCommand(sender, command, label, Arrays_copyOfRange(args, args.length));
        } else {
            sender.sendMessage(RedProtect.get().lang.get("general.color") + "---------------- " + plugin.getDescription().getFullName() + " ----------------");
            sender.sendMessage(RedProtect.get().lang.get("general.color") + "Developed by " + ChatColor.GOLD + plugin.getDescription().getAuthors() + RedProtect.get().lang.get("general.color") + ".");
            sender.sendMessage(RedProtect.get().lang.get("general.color") + "For more information about the commands, type [" + ChatColor.GOLD + "/rp " + getCmd("help") + RedProtect.get().lang.get("general.color") + "].");
            sender.sendMessage(RedProtect.get().lang.get("general.color") + "For a tutorial, type [" + ChatColor.GOLD + "/rp " + getCmd("tutorial") + RedProtect.get().lang.get("general.color") + "].");
            sender.sendMessage(RedProtect.get().lang.get("general.color") + "---------------------------------------------------");
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (sender instanceof Player) {
            if (args.length > 0 && hasCommand(args[0])) {
                TabCompleter tabCompleter = this.getCommandSubCommand(args[0]);
                return tabCompleter.onTabComplete(sender, command, alias, Arrays_copyOfRange(args, args.length));
            } else {
                SortedSet<String> tab = new TreeSet<>();
                for (List<String> cmds : commandMap.keySet()) {
                    String key = cmds.get(0);
                    String cmdtrans = RedProtect.get().lang.get("cmdmanager.translation." + key);
                    if (cmdtrans.startsWith(args[0]) && RedProtect.get().ph.hasCommandPerm(sender, key) && !tab.contains(key)) {
                        tab.add(cmdtrans);
                    }
                }
                return new ArrayList<>(tab);
            }
        } else {
            if (args.length > 0 && hasCommand(args[0])) {
                TabCompleter tabCompleter = this.getCommandSubCommand(args[0]);
                return tabCompleter.onTabComplete(sender, command, alias, Arrays_copyOfRange(args, args.length));
            } else {
                return Collections.singletonList("admin");
            }
        }
    }
}
