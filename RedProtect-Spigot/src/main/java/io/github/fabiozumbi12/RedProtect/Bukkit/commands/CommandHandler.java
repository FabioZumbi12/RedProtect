/*
 * Copyright (c) 2012-2023 - @FabioZumbi12
 * Last Modified: 02/10/2023 18:05
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

package io.github.fabiozumbi12.RedProtect.Bukkit.commands;

import io.github.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import io.github.fabiozumbi12.RedProtect.Bukkit.Region;
import io.github.fabiozumbi12.RedProtect.Bukkit.commands.SubCommands.PlayerHandlers.*;
import io.github.fabiozumbi12.RedProtect.Bukkit.commands.SubCommands.RegionHandlers.*;
import io.github.fabiozumbi12.RedProtect.Bukkit.helpers.AddonsManager;
import io.github.fabiozumbi12.RedProtect.Bukkit.helpers.MojangUUIDs;
import io.github.fabiozumbi12.RedProtect.Bukkit.helpers.WorldGuardHelper;
import io.github.fabiozumbi12.RedProtect.Bukkit.hooks.WEHook;
import io.github.fabiozumbi12.RedProtect.Bukkit.updater.SpigetUpdater;
import io.github.fabiozumbi12.RedProtect.Core.helpers.CoreUtil;
import io.github.fabiozumbi12.RedProtect.Core.helpers.Replacer;
import io.github.fabiozumbi12.RedProtect.Core.region.PlayerRegion;
import br.net.fabiozumbi12.UltimateFancy.UltimateFancy;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionType;
import me.ellbristow.mychunk.LiteChunk;
import me.ellbristow.mychunk.MyChunkChunk;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandHandler implements CommandExecutor, TabCompleter, Listener {

    private final RedProtect plugin;
    private final HashMap<List<String>, SubCommand> commandMap = new HashMap<>();
    private final Map<String, String> cmdConfirm = new HashMap<>();

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
        registerCommand(getCmdKeys("regenall"), new RegenAllCommand());
        registerCommand(getCmdKeys("regen"), new RegenCommand());
        registerCommand(getCmdKeys("removeall"), new RemoveAllCommand());
        registerCommand(getCmdKeys("start"), new StartCommand());
        registerCommand(getCmdKeys("tutorial"), new TutorialCommand());
        registerCommand(getCmdKeys("wand"), new WandCommand());

        //region handlers
        registerCommand(getCmdKeys("addblock"), new AddBlockCommand());
        registerCommand(getCmdKeys("border"), new BorderCommand());
        registerCommand(getCmdKeys("claim"), new ClaimCommand());
        registerCommand(getCmdKeys("can-purge"), new CanPurgeCommand());
        registerCommand(getCmdKeys("copyflag"), new CopyFlagCommand());
        registerCommand(getCmdKeys("createportal"), new CreatePortalCommand());
        registerCommand(getCmdKeys("define"), new DefineCommand());
        registerCommand(getCmdKeys("delblock"), new DelBlockCommand());
        registerCommand(getCmdKeys("delete"), new DeleteCommand());
        registerCommand(getCmdKeys("deltp"), new DelTpCommand());
        registerCommand(getCmdKeys("expand-vert"), new ExpandVertCommand());
        registerCommand(getCmdKeys("flag"), new FlagCommand());
        registerCommand(getCmdKeys("kill"), new KillCommand());
        registerCommand(getCmdKeys("list"), new ListCommand());
        registerCommand(getCmdKeys("pos1"), new Pos1Command());
        registerCommand(getCmdKeys("pos2"), new Pos2Command());
        registerCommand(getCmdKeys("purge-limit"), new PurgeLimitCommand());
        registerCommand(getCmdKeys("priority"), new PriorityCommand());
        registerCommand(getCmdKeys("redefine"), new RedefineCommand());
        registerCommand(getCmdKeys("rename"), new RenameCommand());
        registerCommand(getCmdKeys("select-we"), new SelectWECommand());
        registerCommand(getCmdKeys("setblock"), new SetBlockCommand());
        registerCommand(getCmdKeys("setmaxy"), new SetMaxYCommand());
        registerCommand(getCmdKeys("setminy"), new SetMinYCommand());
        registerCommand(getCmdKeys("settp"), new SetTpCommand());
        registerCommand(getCmdKeys("teleport"), new TeleportCommand());
        registerCommand(getCmdKeys("value"), new ValueCommand());
        registerCommand(getCmdKeys("welcome"), new WelcomeCommand());

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

    private static boolean handleWGRegions() {
        if (!RedProtect.get().hooks.checkWG()) {
            return false;
        }

        WorldGuardHelper helper = RedProtect.get().hooks.worldGuardHelper;

        List<RegionManager> loaded = helper.getLoaded();
        if (loaded.isEmpty()) return false;

        int i = 0;
        for (RegionManager rm : loaded) {
            if (rm.getRegions().isEmpty()) continue;

            String w = rm.getName();
            for (Map.Entry<String, ProtectedRegion> pr : rm.getRegions().entrySet()) {
                if (!pr.getValue().getType().equals(RegionType.CUBOID)) continue;
                if (RedProtect.get().getRegionManager().getRegion(pr.getKey(), w) != null) continue;

                Set<PlayerRegion> leaders;
                Set<PlayerRegion> members;

                if (!pr.getValue().getOwners().getUniqueIds().isEmpty())
                    leaders = pr.getValue().getOwners().getUniqueIds().stream().filter(p -> Bukkit.getPlayer(p) != null).map(o -> new PlayerRegion(o.toString(), Bukkit.getPlayer(o).getName())).collect(Collectors.toSet());
                else
                    leaders = pr.getValue().getOwners().getPlayers().stream().map(o -> new PlayerRegion(o, o)).collect(Collectors.toSet());

                if (!pr.getValue().getMembers().getUniqueIds().isEmpty())
                    members = pr.getValue().getMembers().getUniqueIds().stream().filter(p -> Bukkit.getPlayer(p) != null).map(o -> new PlayerRegion(o.toString(), Bukkit.getPlayer(o).getName())).collect(Collectors.toSet());
                else
                    members = pr.getValue().getMembers().getPlayers().stream().map(o -> new PlayerRegion(o, o)).collect(Collectors.toSet());

                if (leaders.isEmpty()) {
                    if (members.isEmpty())
                        leaders.add(new PlayerRegion(RedProtect.get().getConfigManager().configRoot().region_settings.default_leader, RedProtect.get().getConfigManager().configRoot().region_settings.default_leader));
                    else
                        leaders.addAll(members);
                }

                Location min = helper.getMinimumPoint(pr.getValue(), Bukkit.getWorld(w));
                Location max = helper.getMaximumPoint(pr.getValue(), Bukkit.getWorld(w));

                Region r = new Region(pr.getKey(), new HashSet<>(), members, leaders, min, max, RedProtect.get().getConfigManager().getDefFlagsValues(), "", pr.getValue().getPriority(), w, RedProtect.get().getUtil().dateNow(), 0, null, true, true);

                for (Map.Entry<Flag<?>, Object> flag : pr.getValue().getFlags().entrySet()) {
                    if (r.flagExists(flag.getKey().getName()) && RedProtect.get().getUtil().parseObject(flag.getValue().toString()) != null) {
                        r.setFlag(Bukkit.getConsoleSender(), flag.getKey().getName(), RedProtect.get().getUtil().parseObject(flag.getValue().toString()));
                    }
                }
                RedProtect.get().getRegionManager().add(r, w);
                RedProtect.get().logger.warning("Region converted and named to " + r.getName());
                i++;
            }
            RedProtect.get().logger.success(i + " WorldGuard regions imported for world " + w);
        }
        return i > 0;
    }

    private static boolean handleMyChunk() {
        if (!RedProtect.get().hooks.checkMyChunk()) {
            return false;
        }
        Set<LiteChunk> allchunks = new HashSet<>();

        for (World w : RedProtect.get().getServer().getWorlds()) {
            Set<LiteChunk> chunks = MyChunkChunk.getChunks(w);
            allchunks.addAll(chunks);
        }

        if (allchunks.size() != 0) {
            int i = 0;
            for (LiteChunk c : allchunks) {
                Set<String> leaders = new HashSet<>();
                String admin = RedProtect.get().getUtil().PlayerToUUID(c.getOwner());
                leaders.add(admin);
                World w = RedProtect.get().getServer().getWorld(c.getWorldName());
                Chunk chunk = w.getChunkAt(c.getX(), c.getZ());
                int x = chunk.getBlock(7, 50, 7).getX();
                int z = chunk.getBlock(7, 50, 7).getZ();
                String regionName;

                int in = 0;
                while (true) {
                    int is = String.valueOf(in).length();
                    if (RedProtect.get().getUtil().UUIDtoPlayer(admin).length() > 13) {
                        regionName = RedProtect.get().getUtil().UUIDtoPlayer(admin).substring(0, 14 - is) + "_" + in;
                    } else {
                        regionName = RedProtect.get().getUtil().UUIDtoPlayer(admin) + "_" + in;
                    }
                    if (RedProtect.get().getRegionManager().getRegion(regionName, c.getWorldName()) == null) {
                        break;
                    }
                    ++in;
                }

                Region r = new Region(regionName, new HashSet<>(), new HashSet<>(), new HashSet<>(), new int[]{x + 8, x + 8, x - 7, x - 7}, new int[]{z + 8, z + 8, z - 7, z - 7}, w.getMinHeight(), w.getMaxHeight(), 0, c.getWorldName(), RedProtect.get().getUtil().dateNow(), RedProtect.get().getConfigManager().getDefFlagsValues(), "", 0, null, true, true);
                leaders.forEach(r::addLeader);
                MyChunkChunk.unclaim(chunk);
                RedProtect.get().getRegionManager().add(r, c.getWorldName());
                RedProtect.get().logger.warning("Region converted and named to " + r.getName());
                i++;
            }
            RedProtect.get().logger.success(i + " MyChunk regions converted!");
            return true;
        } else {
            return false;
        }
    }

    private List<String> getCmdKeys(String cmd) {
        if (CommandHandlers.getCmd(cmd).equalsIgnoreCase(cmd))
            return Arrays.asList(CommandHandlers.getCmd(cmd), CommandHandlers.getCmdAlias(cmd));
        return Arrays.asList(cmd, CommandHandlers.getCmd(cmd), CommandHandlers.getCmdAlias(cmd));
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

    @EventHandler
    public void onPreCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        String[] args = e.getMessage().split(" ");

        StringBuilder commandArgsAbr = new StringBuilder();
        Arrays.stream(args).forEach(arg -> commandArgsAbr.append(arg).append(" "));
        String commandArgs = commandArgsAbr.substring(1, commandArgsAbr.length() - 1);

        if (args.length >= 2 && (args[0].equals("/redprotect") || args[0].equals("/rp"))) {

            // Check perms
            if (hasCommand(args[1]) && !RedProtect.get().getPermissionHandler().hasCommandPerm(e.getPlayer(), getCmdFromAlias(args[1]))) {
                RedProtect.get().getLanguageManager().sendMessage(e.getPlayer(), "no.permission");
                e.setCancelled(true);
                return;
            }

            List<String> conditions = RedProtect.get().getConfigManager().configRoot().command_confirm;
            conditions.addAll(Arrays.asList(CommandHandlers.getCmd("yes"), CommandHandlers.getCmd("no")));

            if (conditions.stream().anyMatch(cmd -> CommandHandlers.checkCmd(args[1], cmd))) {
                String cmd = conditions.stream().filter(c -> CommandHandlers.checkCmd(args[1], c)).findFirst().get();
                if (!cmdConfirm.containsKey(p.getName()) && !CommandHandlers.checkCmd(cmd, "yes") && !CommandHandlers.checkCmd(cmd, "no")) {

                    // Secure delete command
                    if (cmd.equalsIgnoreCase("delete") && commandArgs.split(" ").length == 2) {
                        if (RedProtect.get().getRegionManager().getTopRegion(p.getLocation()) == null) return;

                        Region r = RedProtect.get().getRegionManager().getTopRegion(p.getLocation());
                        commandArgs = commandArgs + " " + r.getName() + " " + r.getWorld();
                    }
                    cmdConfirm.put(p.getName(), commandArgs);
                    RedProtect.get().getLanguageManager().sendMessage(p, "cmdmanager.confirm",
                            new Replacer[]{
                                    new Replacer("{cmd}", "/" + args[0] + " " + cmd),
                                    new Replacer("{cmd-yes}", CommandHandlers.getCmd("yes")),
                                    new Replacer("{cmd-no}", CommandHandlers.getCmd("no"))});
                    e.setCancelled(true);
                }
            }
            if (cmdConfirm.containsKey(p.getName())) {
                if (CommandHandlers.checkCmd(args[1], "yes")) {
                    String cmd1 = cmdConfirm.get(p.getName());
                    e.setMessage("/" + cmd1);
                    cmdConfirm.remove(p.getName());
                } else if (CommandHandlers.checkCmd(args[1], "no")) {
                    cmdConfirm.remove(p.getName());
                    RedProtect.get().getLanguageManager().sendMessage(p, "cmdmanager.usagecancelled");
                    e.setCancelled(true);
                } else {
                    RedProtect.get().getLanguageManager().sendMessage(p, "cmdmanager.confirm",
                            new Replacer[]{
                                    new Replacer("{cmd}", "/" + cmdConfirm.get(p.getName())),
                                    new Replacer("{cmd-yes}", CommandHandlers.getCmd("yes")),
                                    new Replacer("{cmd-no}", CommandHandlers.getCmd("no"))});
                    e.setCancelled(true);
                }
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && hasCommand(args[0])) {
            CommandExecutor executor = this.getCommandSubCommand(args[0]);
            return executor.onCommand(sender, command, label, Arrays_copyOfRange(args, args.length));
        } else {
            if (args.length == 0 || !RedProtect.get().getPermissionHandler().hasCommandPerm(sender, "admin")) {
                CommandHandlers.HandleHelpPage(sender, 1);
                return true;
            }

            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("debug-item")) {
                    if (sender instanceof Player) {
                        ItemStack hand = ((Player) sender).getItemInHand();
                        plugin.getLanguageManager().sendMessage(sender, "&aMaterial name: " + hand.getType().name());
                    } else {
                        plugin.getLanguageManager().sendMessage(sender, "&cThis command can be used only by online players holding an item!");
                    }
                    return true;
                }

                if (args[0].equalsIgnoreCase("update")) {
                    if (plugin.getUpdater() == null) {
                        plugin.getLanguageManager().sendMessage(sender, "&aPlugin updates is disabled on config.");
                        return true;
                    }
                    if (plugin.getUpdater().getUpdateAvailable() == SpigetUpdater.UpdateStatus.AVAILABLE)
                        plugin.getUpdater().downloadAndUpdateJar(sender);
                    else if (plugin.getUpdater().getUpdateAvailable() == SpigetUpdater.UpdateStatus.RESTART_NEEDED)
                        plugin.getLanguageManager().sendMessage(sender, "&aPlugin updated. Restart server to complete the update.");
                    else
                        plugin.getUpdater().checkForUpdate(sender, false);
                    return true;
                }

                if (args[0].equalsIgnoreCase("reset-uuids")) {
                    final boolean[] save = {false};

                    // Reset uuids
                    RedProtect.get().getRegionManager().getAllRegions().forEach(r -> {
                        r.getLeaders().forEach(rp -> {
                            if (RedProtect.get().getUtil().isUUIDs(rp.getUUID())) {
                                rp.setUUID(rp.getPlayerName());
                                save[0] = true;
                            }
                        });
                        r.getAdmins().forEach(rp -> {
                            if (RedProtect.get().getUtil().isUUIDs(rp.getUUID())) {
                                rp.setUUID(rp.getPlayerName());
                                save[0] = true;
                            }
                        });
                        r.getMembers().forEach(rp -> {
                            if (RedProtect.get().getUtil().isUUIDs(rp.getUUID())) {
                                rp.setUUID(rp.getPlayerName());
                                save[0] = true;
                            }
                        });

                        // Set uudis for online players
                        Bukkit.getOnlinePlayers().forEach(p -> {

                            if (RedProtect.get().getConfigManager().configRoot().online_mode) {

                                // Update player names based on uuids
                                r.getLeaders().forEach(rp -> {
                                    if (rp.getUUID().equalsIgnoreCase(p.getUniqueId().toString()) && !rp.getPlayerName().equalsIgnoreCase(p.getName())) {
                                        rp.setPlayerName(p.getName().toLowerCase());
                                        r.setToSave(true);
                                    }
                                });
                                r.getAdmins().forEach(rp -> {
                                    if (rp.getUUID().equalsIgnoreCase(p.getUniqueId().toString()) && !rp.getPlayerName().equalsIgnoreCase(p.getName())) {
                                        rp.setPlayerName(p.getName().toLowerCase());
                                        r.setToSave(true);
                                    }
                                });
                                r.getMembers().forEach(rp -> {
                                    if (rp.getUUID().equalsIgnoreCase(p.getUniqueId().toString()) && !rp.getPlayerName().equalsIgnoreCase(p.getName())) {
                                        rp.setPlayerName(p.getName().toLowerCase());
                                        r.setToSave(true);
                                    }
                                });

                            } else {

                                // Update uuids based on player names
                                r.getLeaders().forEach(rp -> {
                                    if (rp.getPlayerName().equalsIgnoreCase(p.getName()) && !rp.getUUID().equalsIgnoreCase(p.getUniqueId().toString())) {
                                        rp.setUUID(p.getUniqueId().toString());
                                        r.setToSave(true);
                                    }
                                });
                                r.getAdmins().forEach(rp -> {
                                    if (rp.getPlayerName().equalsIgnoreCase(p.getName()) && !rp.getUUID().equalsIgnoreCase(p.getUniqueId().toString())) {
                                        rp.setUUID(p.getUniqueId().toString());
                                        r.setToSave(true);
                                    }
                                });
                                r.getMembers().forEach(rp -> {
                                    if (rp.getPlayerName().equalsIgnoreCase(p.getName()) && !rp.getUUID().equalsIgnoreCase(p.getUniqueId().toString())) {
                                        rp.setUUID(p.getUniqueId().toString());
                                        r.setToSave(true);
                                    }
                                });
                            }
                        });
                    });
                    if (save[0]) {
                        RedProtect.get().getRegionManager().saveAll(true);
                        RedProtect.get().logger.success("Fixed some online players uuids!");
                    } else {
                        RedProtect.get().logger.success("No uuids fixed!");
                    }
                    return true;
                }

                if (args[0].equalsIgnoreCase("clear-kicks")) {
                    RedProtect.get().denyEnter.clear();
                    RedProtect.get().logger.success("All region kicks was clear");
                    return true;
                }

                if (args[0].equalsIgnoreCase("single-to-files")) {
                    RedProtect.get().logger.success("[" + RedProtect.get().getUtil().SingleToFiles() + "]" + " regions converted to your own files with success");
                    return true;
                }

                if (args[0].equalsIgnoreCase("files-to-single")) {
                    RedProtect.get().logger.success("[" + RedProtect.get().getUtil().FilesToSingle() + "]" + " regions converted to unified database file with success");
                    return true;
                }

                if (args[0].equalsIgnoreCase("fileToMysql")) {
                    try {
                        if (!RedProtect.get().getUtil().fileToMysql()) {
                            RedProtect.get().logger.severe("ERROR: Check if your 'file-type' configuration is set to 'file' before convert from FILE to Mysql.");
                            return true;
                        } else {
                            RedProtect.get().getConfig().set("file-type", "mysql");
                            RedProtect.get().saveConfig();
                            RedProtect.get().getServer().getPluginManager().disablePlugin(RedProtect.get());
                            RedProtect.get().getServer().getPluginManager().enablePlugin(RedProtect.get());
                            RedProtect.get().logger.success("RedProtect reloaded with Mysql as database! Ready to use!");
                            return true;
                        }
                    } catch (Exception e) {
                        CoreUtil.printJarVersion();
                        e.printStackTrace();
                        return true;
                    }
                }

                if (args[0].equalsIgnoreCase("mysqlToFile")) {
                    try {
                        if (!RedProtect.get().getUtil().mysqlToFile()) {
                            RedProtect.get().logger.severe("ERROR: Check if your 'file-type' configuration is set to 'mysql' before convert from MYSQL to FILE.");
                            return true;
                        } else {
                            RedProtect.get().getConfig().set("file-type", "file");
                            RedProtect.get().saveConfig();
                            RedProtect.get().getServer().getPluginManager().disablePlugin(RedProtect.get());
                            RedProtect.get().getServer().getPluginManager().enablePlugin(RedProtect.get());
                            RedProtect.get().logger.success("RedProtect reloaded with File as database! Ready to use!");
                            return true;
                        }
                    } catch (Exception e) {
                        CoreUtil.printJarVersion();
                        e.printStackTrace();
                        return true;
                    }
                }

                if (args[0].equalsIgnoreCase("gpTorp")) {
                    if (!RedProtect.get().hooks.checkGriefPrev()) {
                        RedProtect.get().logger.success("The plugin GriefPrevention is not installed or is disabled");
                        return true;
                    }
                    if (RedProtect.get().getUtil().convertFromGP() == 0) {
                        RedProtect.get().logger.severe("No region converted from GriefPrevention.");
                        return true;
                    } else {
                        RedProtect.get().getRegionManager().saveAll(true);
                        RedProtect.get().logger.info(ChatColor.AQUA + "[" + RedProtect.get().getUtil().convertFromGP() + "] regions converted from GriefPrevention with success");
                        RedProtect.get().getServer().getPluginManager().disablePlugin(RedProtect.get());
                        RedProtect.get().getServer().getPluginManager().enablePlugin(RedProtect.get());
                        return true;
                    }
                }

                if (args[0].equalsIgnoreCase("list-all")) {
                    int total = 0;
                    for (Region r : RedProtect.get().getRegionManager().getAllRegions()) {
                        RedProtect.get().logger.info(ChatColor.GREEN + "[" + total + "]" + "Region: " + r.getName() + ChatColor.RESET + " | " + ChatColor.AQUA + "World: " + r.getWorld() + ChatColor.RESET);
                        total++;
                    }
                    RedProtect.get().logger.success(total + " regions for " + Bukkit.getWorlds().size() + " worlds.");
                    return true;
                }

                if (args[0].equalsIgnoreCase("mychunktorp")) {
                    RedProtect.get().logger.success("...converting MyChunk database");
                    if (handleMyChunk()) {
                        RedProtect.get().getRegionManager().saveAll(true);
                        RedProtect.get().getServer().getPluginManager().disablePlugin(RedProtect.get());
                        RedProtect.get().getServer().getPluginManager().enablePlugin(RedProtect.get());
                        return true;
                    } else {
                        RedProtect.get().logger.success("The plugin MyChunk is not installed or no regions found");
                        return true;
                    }
                }

                if (args[0].equalsIgnoreCase("wgtorp")) {
                    RedProtect.get().logger.success("...converting WorldGuard database");
                    if (handleWGRegions()) {
                        RedProtect.get().getRegionManager().saveAll(true);
                        RedProtect.get().getServer().getPluginManager().disablePlugin(RedProtect.get());
                        RedProtect.get().getServer().getPluginManager().enablePlugin(RedProtect.get());
                        return true;
                    } else {
                        RedProtect.get().logger.success("The plugin WorldGuard is not installed or no regions found");
                        return true;
                    }
                }

                if (args[0].equalsIgnoreCase("load-all")) {
                    RedProtect.get().getRegionManager().clearDB();
                    try {
                        RedProtect.get().getRegionManager().loadAll();
                        RedProtect.get().getUtil().ReadAllDB(RedProtect.get().getRegionManager().getAllRegions());
                    } catch (Exception e) {
                        RedProtect.get().logger.severe("Error on load all regions from database files:");
                        CoreUtil.printJarVersion();
                        e.printStackTrace();
                    }
                    RedProtect.get().logger.success(RedProtect.get().getRegionManager().getAllRegions().size() + " regions has been loaded from database files!");
                    return true;
                }

                if (CommandHandlers.checkCmd(args[0], "reload")) {
                    RedProtect.get().reload();
                    RedProtect.get().getLanguageManager().sendMessage(sender, "RedProtect Plus reloaded!");
                    return true;
                }

                if (args[0].equalsIgnoreCase("reload-config")) {
                    RedProtect.get().reloadConfigs();
                    RedProtect.get().getLanguageManager().sendMessage(sender, "RedProtect configs reloaded!");
                    return true;
                }
            }

            if (args.length == 2) {

                if (args[0].equalsIgnoreCase("test-uuid")) {
                    try {
                        String name = MojangUUIDs.getUUID(args[1]);
                        RedProtect.get().logger.warning("Leader from: " + args[1]);
                        RedProtect.get().logger.warning("UUID To name: " + name);
                    } catch (Exception e) {
                        CoreUtil.printJarVersion();
                        e.printStackTrace();
                    }
                    return true;
                }

                //rp regen stop
                if (CommandHandlers.checkCmd(args[0], "regenall") && args[1].equalsIgnoreCase("stop")) {
                    if (!RedProtect.get().hooks.checkWe()) {
                        return true;
                    }
                    RedProtect.get().getUtil().stopRegen = true;
                    RedProtect.get().getLanguageManager().sendMessage(sender, "&aRegen will stop now. To continue reload the plugin!");
                    return true;
                }

                if (args[0].equalsIgnoreCase("setconfig") && args[1].equalsIgnoreCase("list")) {
                    RedProtect.get().getLanguageManager().sendMessage(sender, ChatColor.AQUA + "=========== Config Sections: ===========");
                    for (String section : RedProtect.get().getConfig().getValues(false).keySet()) {
                        if (section.contains("debug-messages") ||
                                section.contains("file-type") ||
                                section.contains("language")) {
                            sender.sendMessage(ChatColor.GOLD + section + " : " + ChatColor.GREEN + RedProtect.get().getConfig().get(section).toString());
                        }
                    }
                    sender.sendMessage(ChatColor.AQUA + "====================================");
                    return true;
                }

                //rp limit player
                if (CommandHandlers.checkCmd(args[0], "blocklimit")) {
                    Player offp = RedProtect.get().getServer().getOfflinePlayer(args[1]).getPlayer();
                    if (offp == null) {
                        RedProtect.get().getLanguageManager().sendMessage(sender, RedProtect.get().getLanguageManager().get("cmdmanager.noplayer.thisname").replace("{player}", args[1]));
                        return true;
                    }
                    int limit = RedProtect.get().getPermissionHandler().getPlayerBlockLimit(offp);
                    if (limit < 0 || RedProtect.get().getPermissionHandler().hasPerm(offp, "redprotect.limits.blocks.unlimited")) {
                        RedProtect.get().getLanguageManager().sendMessage(sender, RedProtect.get().getLanguageManager().get("cmdmanager.nolimit"));
                        return true;
                    }

                    int currentUsed = RedProtect.get().getRegionManager().getTotalRegionSize(offp.getUniqueId().toString(), offp.getWorld().getName());
                    ChatColor color = currentUsed >= limit ? ChatColor.RED : ChatColor.GOLD;
                    RedProtect.get().getLanguageManager().sendMessage(sender, RedProtect.get().getLanguageManager().get("cmdmanager.yourarea") + color + currentUsed + RedProtect.get().getLanguageManager().get("general.color") + "/" + color + limit + RedProtect.get().getLanguageManager().get("general.color"));
                    return true;
                }
            }

            if (args.length == 3) {
                //rp addon <addon-name> <download/enable/disable>
                if (args[0].equalsIgnoreCase("addon")) {
                    String fileName = args[1] + "-" + RedProtect.get().getDescription().getVersion() + ".jar";
                    File addOnfolder = new File(Path.of(RedProtect.get().getDataFolder().getPath(),"addons").toUri());
                    addOnfolder.mkdirs();
                    File folderFilename = new File(addOnfolder, fileName);
                    Plugin addonPl = RedProtect.get().getServer().getPluginManager().getPlugin(args[1]);

                    if (args[2].equalsIgnoreCase("download")){
                        if (AddonsManager.DownloadAddOn(args[1], sender, folderFilename)){
                            boolean update = new File(addOnfolder, "new-" + fileName).exists();
                            try {
                                if (!update){
                                    addonPl = RedProtect.get().getPluginLoader().loadPlugin(folderFilename);
                                    RedProtect.get().getPluginLoader().enablePlugin(addonPl);
                                }
                                RedProtect.get().getLanguageManager().sendMessage(sender, "cmdmanager.addon." + (update ? "updated":"loaded"), new Replacer[]{new Replacer("{name}", args[1])});
                                return true;
                            } catch (InvalidPluginException e) {
                                RedProtect.get().getLanguageManager().sendMessage(sender,
                                        RedProtect.get().getLanguageManager().get("cmdmanager.addon.error").replace("{name}",args[1]) + "\n" + e.getMessage());
                            }
                        }
                        return true;
                    }

                    if (addonPl != null){
                        if (args[2].equalsIgnoreCase("enable")){
                            if (!addonPl.isEnabled()){
                                RedProtect.get().getServer().getPluginManager().enablePlugin(addonPl);
                            }
                        }
                        if (args[2].equalsIgnoreCase("disable")){
                            if (addonPl.isEnabled()){
                                RedProtect.get().getServer().getPluginManager().disablePlugin(addonPl);
                            }
                        }
                        RedProtect.get().getLanguageManager().sendMessage(sender,"cmdmanager.addon.status", new Replacer[]{new Replacer("{name}",args[1]), new Replacer("{status}", String.valueOf(addonPl.isEnabled()))});
                    } else {
                        RedProtect.get().getLanguageManager().sendMessage(sender,"cmdmanager.addon.invalid", new Replacer[]{new Replacer("{name}",args[1])});
                    }
                    return true;
                }

                //rp undo <region> <database>
                if (args[0].equalsIgnoreCase("undo")) {
                    if (!RedProtect.get().hooks.checkWe()) {
                        return true;
                    }
                    World w = RedProtect.get().getServer().getWorld(args[2]);
                    if (w == null) {
                        RedProtect.get().getLanguageManager().sendMessage(sender, RedProtect.get().getLanguageManager().get("cmdmanager.region.invalidworld"));
                        return true;
                    }
                    Region r = RedProtect.get().getRegionManager().getRegion(args[1], w.getName());
                    if (r == null) {
                        RedProtect.get().getLanguageManager().sendMessage(sender, RedProtect.get().getLanguageManager().get("correct.usage") + " " + ChatColor.YELLOW + "Invalid region: " + args[1]);
                        return true;
                    }

                    if (WEHook.undo(r.getID())) {
                        RedProtect.get().getLanguageManager().sendMessage(sender, RedProtect.get().getLanguageManager().get("cmdmanager.regen.undo.sucess").replace("{region}", r.getName()));
                    } else {
                        RedProtect.get().getLanguageManager().sendMessage(sender, RedProtect.get().getLanguageManager().get("cmdmanager.regen.undo.none").replace("{region}", r.getName()));
                    }
                    return true;
                }

                if (args[0].equalsIgnoreCase("setconfig")) {
                    if (args[1].equals("debug-messages") ||
                            args[1].equals("file-type") ||
                            args[1].equals("language")) {
                        Object from = RedProtect.get().getConfig().get(args[1]);
                        if (args[2].equals("true") || args[2].equals("false")) {
                            RedProtect.get().getConfig().set(args[1], Boolean.parseBoolean(args[2]));
                        } else {
                            try {
                                int value = Integer.parseInt(args[2]);
                                RedProtect.get().getConfig().set(args[1], value);
                            } catch (NumberFormatException ex) {
                                RedProtect.get().getConfig().set(args[1], args[2]);
                            }
                        }
                        RedProtect.get().getLanguageManager().sendMessage(sender, RedProtect.get().getLanguageManager().get("cmdmanager.configset") + " " + from.toString() + " > " + args[2]);
                        RedProtect.get().getConfigManager().save();
                        return true;
                    } else {
                        RedProtect.get().getLanguageManager().sendMessage(sender, RedProtect.get().getLanguageManager().get("cmdmanager.confignotset") + " " + args[1]);

                    }
                    return true;
                }
            }

            if (args[0].equalsIgnoreCase("list-areas")) {
                int Page = 1;
                if (args.length == 2) {
                    try {
                        Page = Integer.parseInt(args[1]);
                    } catch (Exception ignored) {
                    }
                }
                sender.sendMessage(RedProtect.get().getLanguageManager().get("general.color") + "-------------------------------------------------");
                int regionsPage = RedProtect.get().getConfigManager().configRoot().region_settings.region_list.region_per_page;
                int total = 0;
                int last = 0;

                for (World w : Bukkit.getWorlds()) {
                    boolean first = true;

                    if (Page == 0) {
                        Page = 1;
                    }
                    int max = (regionsPage * Page);
                    int min = max - regionsPage;
                    int count;

                    Set<Region> wregions = new HashSet<>();
                    for (Region r : RedProtect.get().getRegionManager().getRegionsByWorld(w.getName())) {
                        SimpleDateFormat dateformat = new SimpleDateFormat(RedProtect.get().getConfigManager().configRoot().region_settings.date_format);
                        Date now = null;
                        try {
                            now = dateformat.parse(RedProtect.get().getUtil().dateNow());
                        } catch (ParseException e1) {
                            RedProtect.get().logger.severe("The 'date-format' don't match with date 'now'!!");
                        }
                        Date regiondate = null;
                        try {
                            regiondate = dateformat.parse(r.getDate());
                        } catch (ParseException e) {
                            RedProtect.get().logger.severe("The 'date-format' don't match with region date!!");
                            CoreUtil.printJarVersion();
                            e.printStackTrace();
                        }
                        long days = TimeUnit.DAYS.convert(now.getTime() - regiondate.getTime(), TimeUnit.MILLISECONDS);
                        for (String play : RedProtect.get().getConfigManager().configRoot().purge.ignore_regions_from_players) {
                            if (r.isLeader(play) || r.isAdmin(play)) {
                                break;
                            }
                        }
                        if (!r.isLeader(RedProtect.get().getConfigManager().configRoot().region_settings.default_leader) && days > RedProtect.get().getConfigManager().configRoot().purge.remove_oldest && r.getArea() >= RedProtect.get().getConfigManager().configRoot().purge.regen.max_area_regen) {
                            wregions.add(r);
                        }
                    }
                    if (wregions.size() == 0) {
                        continue;
                    }

                    String colorChar = ChatColor.translateAlternateColorCodes('&', RedProtect.get().getConfigManager().configRoot().region_settings.world_colors.get(w.getName()));

                    int totalLocal = wregions.size();
                    total += totalLocal;

                    int lastLocal = 0;

                    if (wregions.size() > 0) {
                        List<Region> it = new ArrayList<>(wregions);
                        if (min > totalLocal) {
                            int diff = (totalLocal / regionsPage);
                            min = regionsPage * diff;
                            max = (regionsPage * diff) + regionsPage;
                        }
                        if (max > it.size()) max = (it.size() - 1);
                        //-------------
                        if (RedProtect.get().getConfigManager().configRoot().region_settings.region_list.hover_and_click_teleport && RedProtect.get().getPermissionHandler().hasRegionPermAdmin(sender, "teleport", null)) {
                            UltimateFancy fancy = new UltimateFancy(RedProtect.get());
                            for (int i = min; i <= max; i++) {
                                count = i;
                                Region r = it.get(i);
                                String rname = RedProtect.get().getLanguageManager().get("general.color") + ", " + ChatColor.GRAY + r.getName() + "(" + r.getArea() + ")";
                                if (first) {
                                    rname = rname.substring(3);
                                    first = false;
                                }
                                if (count == max) {
                                    rname = rname + RedProtect.get().getLanguageManager().get("general.color") + ".";
                                }
                                fancy.text(rname)
                                        .hoverShowText(RedProtect.get().getLanguageManager().get("cmdmanager.list.hover").replace("{region}", r.getName()))
                                        .clickRunCmd("/rp " + CommandHandlers.getCmd("teleport") + " " + r.getName() + " " + r.getWorld())
                                        .next();
                                lastLocal = count;
                            }
                            last += lastLocal + 1;
                            sender.sendMessage("-----");
                            sender.sendMessage(RedProtect.get().getLanguageManager().get("general.color") + RedProtect.get().getLanguageManager().get("region.world").replace(":", "") + " " + colorChar + w.getName() + "[" + (min + 1) + "-" + (max + 1) + "/" + wregions.size() + "]" + ChatColor.RESET + ": ");
                            fancy.send(sender);
                        } else {
                            StringBuilder worldregions = new StringBuilder();
                            for (int i = min; i <= max; i++) {
                                count = i;
                                Region r = it.get(i);
                                worldregions.append(RedProtect.get().getLanguageManager().get("general.color")).append(", ").append(ChatColor.GRAY).append(r.getName()).append(r.getArea());
                                lastLocal = count;
                            }
                            last += lastLocal + 1;
                            sender.sendMessage("-----");
                            sender.sendMessage(RedProtect.get().getLanguageManager().get("general.color") + RedProtect.get().getLanguageManager().get("region.world").replace(":", "") + " " + colorChar + w.getName() + "[" + (min + 1) + "-" + (max + 1) + "/" + wregions.size() + "]" + ChatColor.RESET + ": ");
                            sender.sendMessage(worldregions.substring(3) + RedProtect.get().getLanguageManager().get("general.color") + ".");
                        }
                        //-----------
                    }
                }
                sender.sendMessage(RedProtect.get().getLanguageManager().get("general.color") + "---------------- " + last + "/" + total + " -----------------");
                if (last < total) {
                    sender.sendMessage(RedProtect.get().getLanguageManager().get("cmdmanager.region.listpage.more").replace("{player}", "" + (Page + 1)));
                } else {
                    if (Page != 1) {
                        sender.sendMessage(RedProtect.get().getLanguageManager().get("cmdmanager.region.listpage.nomore"));
                    }
                }
                return true;
            }

            if (args[0].equalsIgnoreCase("save-all")) {
                RedProtect.get().logger.saveLogs();
                RedProtect.get().logger.success(RedProtect.get().getRegionManager().saveAll(args.length == 2 && args[1].equalsIgnoreCase("-f")) + " regions saved with success!");
                return true;
            }

            CommandHandlers.HandleHelpPage(sender, 1);
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> consoleCmds = Stream.of("debug-item", "update", "reset-uuids", "list-areas", "clear-kicks", "kick", "files-to-single", "single-to-files", "flag", "list", "kill", "teleport", "fileToMysql", "mysqlToFile", "setconfig", "reload", "reload-config", "save-all", "load-all", "blocklimit", "claimlimit", "list-all", "wgtorp", "addon").sorted().toList();
        if (sender instanceof Player) {
            if (args.length > 0 && hasCommand(args[0])) {
                TabCompleter tabCompleter = this.getCommandSubCommand(args[0]);
                return tabCompleter.onTabComplete(sender, command, alias, Arrays_copyOfRange(args, args.length));
            } else {
                SortedSet<String> tab = new TreeSet<>();
                for (List<String> cmds : commandMap.keySet()) {
                    String key = cmds.get(0);
                    String cmdtrans = RedProtect.get().getLanguageManager().get("cmdmanager.translation." + key);
                    if (cmdtrans.startsWith(args[0]) && RedProtect.get().getPermissionHandler().hasCommandPerm(sender, key) && !tab.contains(key)) {
                        tab.add(cmdtrans);
                    }
                }
                for (String admCmd : consoleCmds) {
                    if (admCmd.startsWith(args[0]) && RedProtect.get().getPermissionHandler().hasCommandPerm(sender, admCmd)) {
                        tab.add(admCmd);
                    }
                }
                return new ArrayList<>(tab);
            }
        } else {
            SortedSet<String> tab = new TreeSet<>();
            if (args.length == 0) {
                tab.addAll(consoleCmds);
            } else {
                for (String admCmd : consoleCmds) {
                    if (admCmd.startsWith(args[0])) {
                        tab.add(admCmd);
                    }
                }
            }
            return new ArrayList<>(tab);
        }
    }
}
