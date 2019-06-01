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
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommands.PlayerHandlers.*;
import br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommands.RegionHandlers.*;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.ConfigManager;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.LangGuiManager;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.LangManager;
import br.net.fabiozumbi12.RedProtect.Bukkit.fanciful.FancyMessage;
import br.net.fabiozumbi12.RedProtect.Bukkit.helpers.MojangUUIDs;
import br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RedProtectUtil;
import br.net.fabiozumbi12.RedProtect.Bukkit.hooks.WEHook;
import br.net.fabiozumbi12.RedProtect.Core.helpers.CoreUtil;
import me.ellbristow.mychunk.LiteChunk;
import me.ellbristow.mychunk.MyChunkChunk;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static br.net.fabiozumbi12.RedProtect.Bukkit.commands.CommandHandlers.*;

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
        registerCommand(getCmdKeys("regenall"), new RegenAllCommand());
        registerCommand(getCmdKeys("regen"), new RegenCommand());
        registerCommand(getCmdKeys("removeall"), new RemoveAllCommand());
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

    private static boolean handleMyChunk() {
        if (!RedProtect.get().hooks.myChunk) {
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
                String admin = RedProtectUtil.PlayerToUUID(c.getOwner());
                leaders.add(admin);
                World w = RedProtect.get().getServer().getWorld(c.getWorldName());
                Chunk chunk = w.getChunkAt(c.getX(), c.getZ());
                int x = chunk.getBlock(7, 50, 7).getX();
                int z = chunk.getBlock(7, 50, 7).getZ();
                String regionName;

                int in = 0;
                while (true) {
                    int is = String.valueOf(in).length();
                    if (RedProtectUtil.UUIDtoPlayer(admin).length() > 13) {
                        regionName = RedProtectUtil.UUIDtoPlayer(admin).substring(0, 14 - is) + "_" + in;
                    } else {
                        regionName = RedProtectUtil.UUIDtoPlayer(admin) + "_" + in;
                    }
                    if (RedProtect.get().rm.getRegion(regionName, c.getWorldName()) == null) {
                        break;
                    }
                    ++in;
                }

                Region r = new Region(regionName, new HashSet<>(), new HashSet<>(), new HashSet<>(), new int[]{x + 8, x + 8, x - 7, x - 7}, new int[]{z + 8, z + 8, z - 7, z - 7}, 0, w.getMaxHeight(), 0, c.getWorldName(), RedProtectUtil.dateNow(), RedProtect.get().config.getDefFlagsValues(), "", 0, null, true);
                leaders.forEach(r::addLeader);
                MyChunkChunk.unclaim(chunk);
                RedProtect.get().rm.add(r, c.getWorldName());
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
            if (args.length == 0 || !RedProtect.get().ph.hasCommandPerm(sender, "admin")) {
                HandleHelpPage(sender, 1);
                return true;
            }

            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("reset-uuids")) {
                    final boolean[] save = {false};

                    // Reset uuids
                    RedProtect.get().rm.getAllRegions().forEach(r -> {
                        r.getLeaders().forEach(rp -> {
                            if (RedProtectUtil.isUUIDs(rp.getUUID())) {
                                rp.setUUID(rp.getPlayerName());
                                save[0] = true;
                            }
                        });
                        r.getAdmins().forEach(rp -> {
                            if (RedProtectUtil.isUUIDs(rp.getUUID())) {
                                rp.setUUID(rp.getPlayerName());
                                save[0] = true;
                            }
                        });
                        r.getMembers().forEach(rp -> {
                            if (RedProtectUtil.isUUIDs(rp.getUUID())) {
                                rp.setUUID(rp.getPlayerName());
                                save[0] = true;
                            }
                        });

                        // Set uudis for online players
                        Bukkit.getOnlinePlayers().forEach(p -> {

                            if (RedProtect.get().config.configRoot().online_mode) {

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
                        RedProtect.get().rm.saveAll(true);
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
                    RedProtect.get().logger.success("[" + RedProtectUtil.SingleToFiles() + "]" + " regions converted to your own files with success");
                    return true;
                }

                if (args[0].equalsIgnoreCase("files-to-single")) {
                    RedProtect.get().logger.success("[" + RedProtectUtil.FilesToSingle() + "]" + " regions converted to unified database file with success");
                    return true;
                }

                if (args[0].equalsIgnoreCase("fileToMysql")) {
                    try {
                        if (!RedProtectUtil.fileToMysql()) {
                            RedProtect.get().logger.severe("ERROR: Check if your 'file-type' configuration is set to 'yml' before convert from YML to Mysql.");
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
                        if (!RedProtectUtil.mysqlToFile()) {
                            RedProtect.get().logger.severe("ERROR: Check if your 'file-type' configuration is set to 'mysql' before convert from MYSQL to Yml.");
                            return true;
                        } else {
                            RedProtect.get().getConfig().set("file-type", "file");
                            RedProtect.get().saveConfig();
                            RedProtect.get().getServer().getPluginManager().disablePlugin(RedProtect.get());
                            RedProtect.get().getServer().getPluginManager().enablePlugin(RedProtect.get());
                            RedProtect.get().logger.success("RedProtect reloaded with Yml as database! Ready to use!");
                            return true;
                        }
                    } catch (Exception e) {
                        CoreUtil.printJarVersion();
                        e.printStackTrace();
                        return true;
                    }
                }

                if (args[0].equalsIgnoreCase("gpTorp")) {
                    if (!RedProtect.get().hooks.griefPrev) {
                        RedProtect.get().logger.success("The plugin GriefPrevention is not installed or is disabled");
                        return true;
                    }
                    if (RedProtectUtil.convertFromGP() == 0) {
                        RedProtect.get().logger.severe("No region converted from GriefPrevention.");
                        return true;
                    } else {
                        RedProtect.get().rm.saveAll(true);
                        RedProtect.get().logger.info(ChatColor.AQUA + "[" + RedProtectUtil.convertFromGP() + "] regions converted from GriefPrevention with success");
                        RedProtect.get().getServer().getPluginManager().disablePlugin(RedProtect.get());
                        RedProtect.get().getServer().getPluginManager().enablePlugin(RedProtect.get());
                        return true;
                    }
                }

                if (args[0].equalsIgnoreCase("list-all")) {
                    int total = 0;
                    for (Region r : RedProtect.get().rm.getAllRegions()) {
                        RedProtect.get().logger.info(ChatColor.GREEN + "[" + total + "]" + "Region: " + r.getName() + ChatColor.RESET + " | " + ChatColor.AQUA + "World: " + r.getWorld() + ChatColor.RESET);
                        total++;
                    }
                    RedProtect.get().logger.success(total + " regions for " + Bukkit.getWorlds().size() + " worlds.");
                    return true;
                }

                if (args[0].equalsIgnoreCase("mychunktorp")) {
                    if (handleMyChunk()) {
                        RedProtect.get().rm.saveAll(true);
                        RedProtect.get().getServer().getPluginManager().disablePlugin(RedProtect.get());
                        RedProtect.get().getServer().getPluginManager().enablePlugin(RedProtect.get());
                        RedProtect.get().logger.success("...converting MyChunk database");
                        RedProtect.get().logger.success("http://dev.bukkit.org/bukkit-plugins/mychunk/");
                        return true;
                    } else {
                        RedProtect.get().logger.success("The plugin MyChunk is not installed or no regions found");
                        return true;
                    }
                }

                if (args[0].equalsIgnoreCase("load-all")) {
                    RedProtect.get().rm.clearDB();
                    try {
                        RedProtect.get().rm.loadAll();
                        RedProtectUtil.ReadAllDB(RedProtect.get().rm.getAllRegions());
                    } catch (Exception e) {
                        RedProtect.get().logger.severe("Error on load all regions from database files:");
                        CoreUtil.printJarVersion();
                        e.printStackTrace();
                    }
                    RedProtect.get().logger.success(RedProtect.get().rm.getAllRegions().size() + " regions has been loaded from database files!");
                    return true;
                }

                if (checkCmd(args[0], "reload")) {
                    RedProtect.get().reload();
                    RedProtect.get().lang.sendMessage(sender, "RedProtect Plus reloaded!");
                    return true;
                }

                if (args[0].equalsIgnoreCase("reload-config")) {
                    RedProtect.get().cmdHandler.unregisterAll();

                    try {
                        RedProtect.get().config = new ConfigManager();
                    } catch (ObjectMappingException e) {
                        CoreUtil.printJarVersion();
                        e.printStackTrace();
                    }

                    RedProtect.get().logger.info("Loading language files...");
                    RedProtect.get().lang = new LangManager();
                    RedProtect.get().guiLang = new LangGuiManager();

                    RedProtect.get().logger.info("Re-registering commands...");
                    RedProtect.get().cmdHandler = new CommandHandler(RedProtect.get());

                    RedProtect.get().lang.sendMessage(sender, "RedProtect configs reloaded!");
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
                if (checkCmd(args[0], "regenall") && args[1].equalsIgnoreCase("stop")) {
                    if (!RedProtect.get().hooks.worldEdit) {
                        return true;
                    }
                    RedProtectUtil.stopRegen = true;
                    RedProtect.get().lang.sendMessage(sender, "&aRegen will stop now. To continue reload the plugin!");
                    return true;
                }

                if (args[0].equalsIgnoreCase("setconfig") && args[1].equalsIgnoreCase("list")) {
                    RedProtect.get().lang.sendMessage(sender, ChatColor.AQUA + "=========== Config Sections: ===========");
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
                if (checkCmd(args[0], "blocklimit")) {
                    Player offp = RedProtect.get().getServer().getOfflinePlayer(args[1]).getPlayer();
                    if (offp == null) {
                        RedProtect.get().lang.sendMessage(sender, RedProtect.get().lang.get("cmdmanager.noplayer.thisname").replace("{player}", args[1]));
                        return true;
                    }
                    int limit = RedProtect.get().ph.getPlayerBlockLimit(offp);
                    if (limit < 0 || RedProtect.get().ph.hasPerm(offp, "redprotect.limits.blocks.unlimited")) {
                        RedProtect.get().lang.sendMessage(sender, RedProtect.get().lang.get("cmdmanager.nolimit"));
                        return true;
                    }

                    int currentUsed = RedProtect.get().rm.getTotalRegionSize(offp.getUniqueId().toString(), offp.getWorld().getName());
                    ChatColor color = currentUsed >= limit ? ChatColor.RED : ChatColor.GOLD;
                    RedProtect.get().lang.sendMessage(sender, RedProtect.get().lang.get("cmdmanager.yourarea") + color + currentUsed + RedProtect.get().lang.get("general.color") + "/" + color + limit + RedProtect.get().lang.get("general.color"));
                    return true;
                }
            }

            if (args.length == 3) {
                //rp undo <region> <database>
                if (args[0].equalsIgnoreCase("undo")) {
                    if (!RedProtect.get().hooks.worldEdit) {
                        return true;
                    }
                    World w = RedProtect.get().getServer().getWorld(args[2]);
                    if (w == null) {
                        RedProtect.get().lang.sendMessage(sender, RedProtect.get().lang.get("cmdmanager.region.invalidworld"));
                        return true;
                    }
                    Region r = RedProtect.get().rm.getRegion(args[1], w.getName());
                    if (r == null) {
                        RedProtect.get().lang.sendMessage(sender, RedProtect.get().lang.get("correct.usage") + " " + ChatColor.YELLOW + "Invalid region: " + args[1]);
                        return true;
                    }

                    if (WEHook.undo(r.getID())) {
                        RedProtect.get().lang.sendMessage(sender, RedProtect.get().lang.get("cmdmanager.regen.undo.sucess").replace("{region}", r.getName()));
                    } else {
                        RedProtect.get().lang.sendMessage(sender, RedProtect.get().lang.get("cmdmanager.regen.undo.none").replace("{region}", r.getName()));
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
                        RedProtect.get().lang.sendMessage(sender, RedProtect.get().lang.get("cmdmanager.configset") + " " + from.toString() + " > " + args[2]);
                        RedProtect.get().config.save();
                        return true;
                    } else {
                        RedProtect.get().lang.sendMessage(sender, RedProtect.get().lang.get("cmdmanager.confignotset") + " " + args[1]);
                        return true;
                    }
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
                sender.sendMessage(RedProtect.get().lang.get("general.color") + "-------------------------------------------------");
                int regionsPage = RedProtect.get().config.configRoot().region_settings.region_list.region_per_page;
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
                    for (Region r : RedProtect.get().rm.getRegionsByWorld(w.getName())) {
                        SimpleDateFormat dateformat = new SimpleDateFormat(RedProtect.get().config.configRoot().region_settings.date_format);
                        Date now = null;
                        try {
                            now = dateformat.parse(RedProtectUtil.dateNow());
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
                        for (String play : RedProtect.get().config.configRoot().purge.ignore_regions_from_players) {
                            if (r.isLeader(play) || r.isAdmin(play)) {
                                break;
                            }
                        }
                        if (!r.isLeader(RedProtect.get().config.configRoot().region_settings.default_leader) && days > RedProtect.get().config.configRoot().purge.remove_oldest && r.getArea() >= RedProtect.get().config.configRoot().purge.regen.max_area_regen) {
                            wregions.add(r);
                        }
                    }
                    if (wregions.size() == 0) {
                        continue;
                    }

                    String colorChar = ChatColor.translateAlternateColorCodes('&', RedProtect.get().config.configRoot().region_settings.world_colors.get(w.getName()));

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
                        if (RedProtect.get().config.configRoot().region_settings.region_list.hover_and_click_teleport && RedProtect.get().ph.hasRegionPermAdmin(sender, "teleport", null)) {
                            FancyMessage fancy = new FancyMessage();
                            for (int i = min; i <= max; i++) {
                                count = i;
                                Region r = it.get(i);
                                String rname = RedProtect.get().lang.get("general.color") + ", " + ChatColor.GRAY + r.getName() + "(" + r.getArea() + ")";
                                if (first) {
                                    rname = rname.substring(3);
                                    first = false;
                                }
                                if (count == max) {
                                    rname = rname + RedProtect.get().lang.get("general.color") + ".";
                                }
                                fancy.text(rname).color(ChatColor.DARK_GRAY)
                                        .tooltip(RedProtect.get().lang.get("cmdmanager.list.hover").replace("{region}", r.getName()))
                                        .command("/rp " + getCmd("teleport") + " " + r.getName() + " " + r.getWorld())
                                        .then(" ");
                                lastLocal = count;
                            }
                            last += lastLocal + 1;
                            sender.sendMessage("-----");
                            sender.sendMessage(RedProtect.get().lang.get("general.color") + RedProtect.get().lang.get("region.world").replace(":", "") + " " + colorChar + w.getName() + "[" + (min + 1) + "-" + (max + 1) + "/" + wregions.size() + "]" + ChatColor.RESET + ": ");
                            fancy.send(sender);
                        } else {
                            StringBuilder worldregions = new StringBuilder();
                            for (int i = min; i <= max; i++) {
                                count = i;
                                Region r = it.get(i);
                                worldregions.append(RedProtect.get().lang.get("general.color")).append(", ").append(ChatColor.GRAY).append(r.getName()).append(r.getArea());
                                lastLocal = count;
                            }
                            last += lastLocal + 1;
                            sender.sendMessage("-----");
                            sender.sendMessage(RedProtect.get().lang.get("general.color") + RedProtect.get().lang.get("region.world").replace(":", "") + " " + colorChar + w.getName() + "[" + (min + 1) + "-" + (max + 1) + "/" + wregions.size() + "]" + ChatColor.RESET + ": ");
                            sender.sendMessage(worldregions.substring(3) + RedProtect.get().lang.get("general.color") + ".");
                        }
                        //-----------
                    }
                }
                sender.sendMessage(RedProtect.get().lang.get("general.color") + "---------------- " + last + "/" + total + " -----------------");
                if (last < total) {
                    sender.sendMessage(RedProtect.get().lang.get("cmdmanager.region.listpage.more").replace("{player}", "" + (Page + 1)));
                } else {
                    if (Page != 1) {
                        sender.sendMessage(RedProtect.get().lang.get("cmdmanager.region.listpage.nomore"));
                    }
                }
                return true;
            }

            if (args[0].equalsIgnoreCase("save-all")) {
                RedProtect.get().logger.saveLogs();
                RedProtect.get().logger.success(RedProtect.get().rm.saveAll(args.length == 2 && args[1].equalsIgnoreCase("-f")) + " regions saved with success!");
                return true;
            }

            HandleHelpPage(sender, 1);
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> consoleCmds = Arrays.asList("reset-uuids", "list-areas", "clear-kicks", "kick", "files-to-single", "single-to-files", "flag", "list", "teleport", "ymltomysql", "mysqltoyml", "setconfig", "reload", "reload-config", "save-all", "load-all", "blocklimit", "claimlimit", "list-all");
        if (sender instanceof Player){
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
                for (String admCmd : consoleCmds) {
                    if (admCmd.startsWith(args[0]) && RedProtect.get().ph.hasCommandPerm(sender, admCmd)) {
                        tab.add(admCmd);
                    }
                }
                return new ArrayList<>(tab);
            }
        } else {
            SortedSet<String> tab = new TreeSet<>();
            if (args.length == 0){
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
