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

package br.net.fabiozumbi12.RedProtect.Sponge.commands;

import br.net.fabiozumbi12.RedProtect.Core.helpers.CoreUtil;
import br.net.fabiozumbi12.RedProtect.Core.helpers.Replacer;
import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import br.net.fabiozumbi12.RedProtect.Sponge.Region;
import br.net.fabiozumbi12.RedProtect.Sponge.commands.SubCommands.PlayerHandlers.*;
import br.net.fabiozumbi12.RedProtect.Sponge.commands.SubCommands.RegionHandlers.*;
import br.net.fabiozumbi12.RedProtect.Sponge.config.ConfigManager;
import br.net.fabiozumbi12.RedProtect.Sponge.config.LangGuiManager;
import br.net.fabiozumbi12.RedProtect.Sponge.config.LangManager;
import br.net.fabiozumbi12.RedProtect.Sponge.hooks.WEHook;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.world.World;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static br.net.fabiozumbi12.RedProtect.Sponge.commands.CommandHandlers.*;

public class CommandHandler {

    private final RedProtect plugin;
    private Map<String, String> cmdConfirm = new HashMap<>();

    public CommandHandler(RedProtect plugin) {
        this.plugin = plugin;
        CommandSpec redProtect = CommandSpec.builder()
                .description(Text.of("Main command for RedProtect."))
                .arguments(
                        GenericArguments.optional(GenericArguments.choices(Text.of("command"), getConsoleCmds())),
                        GenericArguments.optional(GenericArguments.remainingJoinedStrings(Text.of("subCommands"))))
                .executor((sender, arguments) -> {

                    CommandResult cmdr = CommandResult.empty();
                    Collection<String> all = new ArrayList<>(arguments.getAll("command"));
                    all.addAll(arguments.getAll("subCommands"));
                    String[] args = all.toArray(new String[0]);

                    if (args.length == 0 || !RedProtect.get().ph.hasCommandPerm(sender, "admin")) {
                        HandleHelpPage(sender, 1);
                        return cmdr;
                    }

                    if (args.length == 1) {
                        if (args[0].equalsIgnoreCase("reset-uuids")) {
                            final boolean[] save = {false};

                            // Reset uuids
                            RedProtect.get().rm.getAllRegions().forEach(r -> {
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

                                // Set uuids for online players
                                Sponge.getServer().getOnlinePlayers().forEach(p -> {

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
                            return cmdr;
                        }

                        if (args[0].equalsIgnoreCase("clear-kicks")) {
                            RedProtect.get().denyEnter.clear();
                            RedProtect.get().logger.success("All region kicks was clear");
                            return cmdr;
                        }

                        if (args[0].equalsIgnoreCase("single-to-files")) {
                            RedProtect.get().logger.success("[" + RedProtect.get().getUtil().SingleToFiles() + "]" + " regions converted to your own files with success");
                            return cmdr;
                        }

                        if (args[0].equalsIgnoreCase("files-to-single")) {
                            RedProtect.get().logger.success("[" + RedProtect.get().getUtil().FilesToSingle() + "]" + " regions converted to unified world file with success");
                            return cmdr;
                        }

                        if (args[0].equalsIgnoreCase("fileToMysql")) {
                            try {
                                if (!RedProtect.get().getUtil().fileToMysql()) {
                                    RedProtect.get().logger.severe("ERROR: Check if your 'file-type' configuration is set to 'file' before convert from FILE to Mysql.");
                                    return cmdr;
                                } else {
                                    RedProtect.get().config.configRoot().file_type = "mysql";
                                    RedProtect.get().config.save();
                                    RedProtect.get().reload();
                                    RedProtect.get().logger.success("Redprotect reloaded with Mysql as database! Ready to use!");
                                    return cmdr;
                                }
                            } catch (Exception e) {
                                CoreUtil.printJarVersion();
                                e.printStackTrace();
                                return cmdr;
                            }
                        }

                        if (args[0].equalsIgnoreCase("mysqlToFile")) {
                            try {
                                if (!RedProtect.get().getUtil().mysqlToFile()) {
                                    RedProtect.get().logger.severe("ERROR: Check if your 'file-type' configuration is set to 'mysql' before convert from MYSQL to File.");
                                    return cmdr;
                                } else {
                                    RedProtect.get().config.configRoot().file_type = "file";
                                    RedProtect.get().config.save();
                                    RedProtect.get().reload();
                                    RedProtect.get().logger.success("Redprotect reloaded with File as database! Ready to use!");
                                    return cmdr;
                                }
                            } catch (Exception e) {
                                CoreUtil.printJarVersion();
                                e.printStackTrace();
                                return cmdr;
                            }
                        }

                        if (args[0].isEmpty()) {
                            sender.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("general.color") + "---------------- " + RedProtect.get().container.getName() + " ----------------"));
                            sender.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("general.color") + "Developed by &eFabioZumbi12" + RedProtect.get().lang.get("general.color") + "."));
                            sender.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("general.color") + "For more information about the commands, type [&e/rp ?" + RedProtect.get().lang.get("general.color") + "]."));
                            sender.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("general.color") + "For a tutorial, type [&e/rp tutorial" + RedProtect.get().lang.get("general.color") + "]."));
                            sender.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("general.color") + "---------------------------------------------------"));
                            return cmdr;
                        }

                        if (args[0].equalsIgnoreCase("list-all")) {
                            int total = 0;
                            for (Region r : RedProtect.get().rm.getAllRegions()) {
                                RedProtect.get().logger.info("&a[" + total + "]" + "Region: " + r.getName() + "&r | &3World: " + r.getWorld() + "&r");
                                total++;
                            }
                            RedProtect.get().logger.success(total + " regions for " + Sponge.getServer().getWorlds().size() + " worlds.");
                            return cmdr;
                        }

                        if (args[0].equalsIgnoreCase("load-all")) {
                            RedProtect.get().rm.clearDB();
                            try {
                                RedProtect.get().rm.loadAll();
                                RedProtect.get().getUtil().ReadAllDB(RedProtect.get().rm.getAllRegions());
                            } catch (Exception e) {
                                RedProtect.get().logger.severe("Error on load all regions from database files:");
                                CoreUtil.printJarVersion();
                                e.printStackTrace();
                            }
                            RedProtect.get().logger.success(RedProtect.get().rm.getAllRegions().size() + " regions has been loaded from database files!");
                            return cmdr;
                        }


                        if (checkCmd(args[0], "reload")) {
                            for (Player p : Sponge.getGame().getServer().getOnlinePlayers()) {
                                RedProtect.get().getVersionHelper().closeInventory(p);
                            }
                            RedProtect.get().reload();
                            RedProtect.get().logger.success("Redprotect reloaded with success!");
                            return cmdr;
                        }

                        if (args[0].equalsIgnoreCase("reload-config")) {
                            try {
                                RedProtect.get().commandHandler.unregisterAll();

                                RedProtect.get().config = new ConfigManager(RedProtect.get().factory);

                                RedProtect.get().lang = new LangManager();
                                RedProtect.get().guiLang = new LangGuiManager();

                                RedProtect.get().logger.info("Re-registering commands...");
                                RedProtect.get().commandHandler = new CommandHandler(RedProtect.get());

                                RedProtect.get().logger.success("Redprotect Plus configs reloaded!");
                            } catch (ObjectMappingException e) {
                                RedProtect.get().logger.severe("Redprotect Plus configs NOT reloaded!");
                                CoreUtil.printJarVersion();
                                e.printStackTrace();
                            }
                            return cmdr;
                        }
                    }

                    if (args.length == 3) {

                        //rp undo <region> <database>
                        if (args[0].equalsIgnoreCase("undo")) {
                            if (!RedProtect.get().hooks.WE) {
                                return cmdr;
                            }
                            Optional<World> w = RedProtect.get().getServer().getWorld(args[2]);
                            if (!w.isPresent()) {
                                RedProtect.get().lang.sendMessage(sender, RedProtect.get().lang.get("cmdmanager.region.invalidworld"));
                                return cmdr;
                            }
                            Region r = RedProtect.get().rm.getRegion(args[1], w.get().getName());
                            if (r == null) {
                                RedProtect.get().lang.sendMessage(sender, RedProtect.get().lang.get("correct.usage") + " &eInvalid region: " + args[1]);
                                return cmdr;
                            }

                            if (WEHook.undo(r.getID())) {
                                RedProtect.get().lang.sendMessage(sender, RedProtect.get().lang.get("cmdmanager.regen.undo.sucess").replace("{region}", r.getName()));
                            } else {
                                RedProtect.get().lang.sendMessage(sender, RedProtect.get().lang.get("cmdmanager.regen.undo.none").replace("{region}", r.getName()));
                            }
                            return cmdr;
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
                        sender.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("general.color") + "-------------------------------------------------"));
                        int regionsPage = RedProtect.get().config.configRoot().region_settings.region_list.region_per_page;
                        int total = 0;
                        int last = 0;

                        for (World w : Sponge.getServer().getWorlds()) {
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

                            String colorChar = RedProtect.get().config.configRoot().region_settings.world_colors.get(w.getName());

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
                                //-----------
                                Text.Builder worldregions = Text.builder();
                                for (int i = min; i <= max; i++) {
                                    count = i;
                                    Region r = it.get(i);

                                    if (RedProtect.get().ph.hasRegionPermAdmin(sender, "teleport", null)) {
                                        if (first) {
                                            first = false;
                                            worldregions.append(Text.builder()
                                                    .append(RedProtect.get().getUtil().toText("&8" + r.getName() + "(" + r.getArea() + ")"))
                                                    .onHover(TextActions.showText(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("cmdmanager.list.hover").replace("{region}", r.getName()))))
                                                    .onClick(TextActions.runCommand("/rp " + getCmd("teleport") + " " + r.getName() + " " + r.getWorld())).build());
                                        } else {
                                            worldregions.append(Text.builder()
                                                    .append(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("general.color") + ", &8" + r.getName() + "(" + r.getArea() + ")"))
                                                    .onHover(TextActions.showText(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("cmdmanager.list.hover").replace("{region}", r.getName()))))
                                                    .onClick(TextActions.runCommand("/rp " + getCmd("teleport") + " " + r.getName() + " " + r.getWorld())).build());
                                        }
                                    } else {
                                        if (first) {
                                            first = false;
                                            worldregions.append(Text.builder()
                                                    .append(RedProtect.get().getUtil().toText("&8" + r.getName() + r.getArea())).build());
                                        } else {
                                            worldregions.append(Text.builder()
                                                    .append(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("general.color") + ", &8" + r.getName() + r.getArea())).build());
                                        }
                                    }
                                    lastLocal = count;
                                }
                                //-----------

                                last += lastLocal + 1;
                                sender.sendMessage(RedProtect.get().getUtil().toText("-----"));
                                sender.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("general.color") + RedProtect.get().lang.get("region.world").replace(":", "") + " " + colorChar + w.getName() + "[" + (min + 1) + "-" + (max + 1) + "/" + wregions.size() + "]&r: "));
                                sender.sendMessages(worldregions.append(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("general.color") + ".")).build());
                            }
                        }
                        sender.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("general.color") + "---------------- " + last + "/" + total + " -----------------"));
                        if (last < total) {
                            sender.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("cmdmanager.region.listpage.more").replace("{player}", "" + (Page + 1))));
                        } else {
                            if (Page != 1) {
                                sender.sendMessage(RedProtect.get().getUtil().toText(RedProtect.get().lang.get("cmdmanager.region.listpage.nomore")));
                            }
                        }
                        return cmdr;
                    }

                    if (args[0].equalsIgnoreCase("save-all")) {
                        RedProtect.get().logger.SaveLogs();
                        RedProtect.get().logger.success(RedProtect.get().rm.saveAll(args.length == 2 && args[1].equalsIgnoreCase("-f")) + " regions saved with success!");
                        return cmdr;
                    }

                    HandleHelpPage(sender, 1);
                    return cmdr;
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
                .child(new RegenAllCommand().register(), getCmdKeys("regenall"))
                .child(new RegenCommand().register(), getCmdKeys("regen"))
                .child(new RemoveAllCommand().register(), getCmdKeys("removeall"))
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
                .child(new KillCommand().register(), getCmdKeys("kill"))
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

                .build();

        plugin.commandManager.register(plugin, redProtect, Arrays.asList("redprotect", "rp"));
    }

    @Listener(order = Order.EARLY)
    public void onCommand(SendCommandEvent e, @First CommandSource source) {

        String[] args = e.getArguments().split(" ");

        StringBuilder commandArgsAbr = new StringBuilder();
        Arrays.stream(args).forEach(arg -> commandArgsAbr.append(arg).append(" "));
        String commandArgs = commandArgsAbr.substring(0, commandArgsAbr.length() - 1);

        if (args.length >= 1 && (e.getCommand().equals("redprotect") || e.getCommand().equals("rp"))) {

            List<String> conditions = RedProtect.get().config.configRoot().command_confirm;
            conditions.addAll(Arrays.asList(getCmd("yes"), getCmd("no")));

            if (conditions.stream().anyMatch(cmd -> checkCmd(args[0], cmd))) {
                String cmd = conditions.stream().filter(c -> checkCmd(args[0], c)).findFirst().get();
                if (!cmdConfirm.containsKey(source.getName()) && !checkCmd(cmd, "yes") && !checkCmd(cmd, "no")) {

                    // Segure delete command
                    if (source instanceof Player && cmd.equalsIgnoreCase("delete") && commandArgs.split(" ").length == 2) {
                        if (RedProtect.get().rm.getTopRegion(((Player) source).getLocation(), CommandHandler.class.getName()) == null)
                            return;

                        Region r = RedProtect.get().rm.getTopRegion(((Player) source).getLocation(), CommandHandler.class.getName());
                        commandArgs = commandArgs + " " + r.getName() + " " + r.getWorld();
                    }
                    cmdConfirm.put(source.getName(), commandArgs);
                    RedProtect.get().lang.sendMessage(source, "cmdmanager.confirm",
                            new Replacer[]{
                                    new Replacer("{cmd}", "/" + e.getCommand() + " " + cmd),
                                    new Replacer("{cmd-yes}", getCmd("yes")),
                                    new Replacer("{cmd-no}", getCmd("no"))});
                    e.setCancelled(true);
                }
            }
            if (cmdConfirm.containsKey(source.getName())) {
                if (checkCmd(args[0], "yes")) {
                    String cmd1 = cmdConfirm.get(source.getName());
                    e.setArguments(cmd1);
                    cmdConfirm.remove(source.getName());
                } else if (checkCmd(args[0], "no")) {
                    cmdConfirm.remove(source.getName());
                    RedProtect.get().lang.sendMessage(source, "cmdmanager.usagecancelled");
                    e.setCancelled(true);
                } else {
                    RedProtect.get().lang.sendMessage(source, "cmdmanager.confirm",
                            new Replacer[]{
                                    new Replacer("{cmd}", "/" + e.getCommand() + " " + cmdConfirm.get(source.getName())),
                                    new Replacer("{cmd-yes}", getCmd("yes")),
                                    new Replacer("{cmd-no}", getCmd("no"))});
                    e.setCancelled(true);
                }
            }
        }
    }

    public void unregisterAll() {
        plugin.commandManager.getOwnedBy(plugin.container).forEach(p -> plugin.commandManager.removeMapping(p));
    }

    private String[] getCmdKeys(String cmd) {
        if (getCmd(cmd).equalsIgnoreCase(cmd))
            return new String[]{getCmd(cmd), getCmdAlias(cmd)};
        return new String[]{cmd, getCmd(cmd), getCmdAlias(cmd)};
    }

    private HashMap<String, String> getConsoleCmds() {
        HashMap<String, String> map = new HashMap<>();
        for (String cmd : Arrays.asList("update", "reset-uuids", "list-areas", "clear-kicks", "files-to-single", "single-to-files", "filetomysql", "mysqltofile", "setconfig", "reload", "reload-config", "save-all", "load-all", "blocklimit", "claimlimit", "list-all"))
            map.put(cmd, cmd);
        return map;
    }
}
