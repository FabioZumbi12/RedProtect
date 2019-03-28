package br.net.fabiozumbi12.RedProtect.Bukkit.commands;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommands.*;
import br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommands.PlayerHandlers.*;
import br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommands.RegionHandlers.*;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPLang;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.lang.reflect.Array;
import java.util.*;

import static br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPUtil.getCmd;
import static br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPUtil.getCmdAlias;

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
        registerCommand(getCmdKeys("list-areas"), new ListAreasCommand());
        registerCommand(getCmdKeys("load-all"), new LoadAllCommand());
        registerCommand(getCmdKeys("reload"), new ReloadCommand());
        registerCommand(getCmdKeys("reload-config"), new ReloadConfigCommand());
        registerCommand(getCmdKeys("save-all"), new SaveAllCommand());

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
        return Arrays.asList(getCmd(cmd), getCmdAlias(cmd));
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

    private String getCmdFromAlias(String alias){
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
                RPLang.sendMessage(sender, "no.permission");
                return true;
            }
            return executor.onCommand(sender, command, label, Arrays_copyOfRange(args, args.length));
        } else {
            sender.sendMessage(RPLang.get("general.color") + "---------------- " + plugin.pdf.getFullName() + " ----------------");
            sender.sendMessage(RPLang.get("general.color") + "Developed by " + ChatColor.GOLD + plugin.pdf.getAuthors() + RPLang.get("general.color") + ".");
            sender.sendMessage(RPLang.get("general.color") + "For more information about the commands, type [" + ChatColor.GOLD + "/rp " + getCmd("help") + RPLang.get("general.color") + "].");
            sender.sendMessage(RPLang.get("general.color") + "For a tutorial, type [" + ChatColor.GOLD + "/rp " + getCmd("tutorial") + RPLang.get("general.color") + "].");
            sender.sendMessage(RPLang.get("general.color") + "---------------------------------------------------");
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length > 0 && hasCommand(args[0])) {
            TabCompleter tabCompleter = this.getCommandSubCommand(args[0]);
            return tabCompleter.onTabComplete(sender, command, alias, Arrays_copyOfRange(args, args.length));
        } else {
            SortedSet<String> tab = new TreeSet<>();
            if (sender instanceof Player) {
                for (List<String> cmds : commandMap.keySet()) {
                    String key = cmds.get(0);
                    String cmdtrans = RPLang.get("cmdmanager.translation." + key);
                    if (cmdtrans.startsWith(args[0]) && RedProtect.get().ph.hasCommandPerm(sender, key) && !tab.contains(key)) {
                        tab.add(cmdtrans);
                    }
                }
            } else {
                tab.add("admin");
            }
            return new ArrayList<>(tab);
        }
    }
}
