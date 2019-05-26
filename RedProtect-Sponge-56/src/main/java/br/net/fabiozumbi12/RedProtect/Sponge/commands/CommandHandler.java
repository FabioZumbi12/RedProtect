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
import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import br.net.fabiozumbi12.RedProtect.Sponge.Region;
import br.net.fabiozumbi12.RedProtect.Sponge.commands.SubCommands.PlayerHandlers.*;
import br.net.fabiozumbi12.RedProtect.Sponge.commands.SubCommands.RegionHandlers.*;
import br.net.fabiozumbi12.RedProtect.Sponge.config.ConfigManager;
import br.net.fabiozumbi12.RedProtect.Sponge.config.LangGuiManager;
import br.net.fabiozumbi12.RedProtect.Sponge.config.LangManager;
import br.net.fabiozumbi12.RedProtect.Sponge.helpers.RedProtectUtil;
import br.net.fabiozumbi12.RedProtect.Sponge.hooks.WEHook;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static br.net.fabiozumbi12.RedProtect.Sponge.commands.CommandHandlers.*;

public class CommandHandler {

    private final RedProtect plugin;

    public CommandHandler(RedProtect plugin) {
        this.plugin = plugin;
        CommandSpec redProtect = CommandSpec.builder()
                .description(Text.of("Main command for RedProtect."))
                .arguments(GenericArguments.optional(GenericArguments.remainingJoinedStrings(Text.of("args"))))
                .executor((sender, arguments) -> {

                    CommandResult cmdr = CommandResult.empty();
                    String[] args = arguments.getAll("args").toArray(new String[arguments.getAll("args").size()]);

                    if (args.length == 0 || !RedProtect.get().ph.hasCommandPerm(sender, "admin")){
                        HandleHelpPage(sender, 1);
                        return cmdr;
                    }

                    if (args.length == 1) {
                        if (args[0].equalsIgnoreCase("reset-uuids")) {
                            final boolean[] save = {false};

                            // Reset uuids
                            RedProtect.get().rm.getAllRegions().forEach(r->{
                                r.getLeaders().forEach(rp->{
                                    if (RedProtectUtil.isUUIDs(rp.getUUID())){
                                        rp.setUUID(rp.getPlayerName());
                                        save[0] = true;
                                    }
                                });
                                r.getAdmins().forEach(rp->{
                                    if (RedProtectUtil.isUUIDs(rp.getUUID())){
                                        rp.setUUID(rp.getPlayerName());
                                        save[0] = true;
                                    }
                                });
                                r.getMembers().forEach(rp->{
                                    if (RedProtectUtil.isUUIDs(rp.getUUID())){
                                        rp.setUUID(rp.getPlayerName());
                                        save[0] = true;
                                    }
                                });

                                // Set uuids for online players
                                Sponge.getServer().getOnlinePlayers().forEach(p->{

                                    if (RedProtect.get().config.configRoot().online_mode){

                                        // Update player names based on uuids
                                        r.getLeaders().forEach(rp->{
                                            if (rp.getUUID().equalsIgnoreCase(p.getUniqueId().toString()) && !rp.getPlayerName().equalsIgnoreCase(p.getName())){
                                                rp.setPlayerName(p.getName().toLowerCase());
                                                r.setToSave(true);
                                            }
                                        });
                                        r.getAdmins().forEach(rp->{
                                            if (rp.getUUID().equalsIgnoreCase(p.getUniqueId().toString()) && !rp.getPlayerName().equalsIgnoreCase(p.getName())){
                                                rp.setPlayerName(p.getName().toLowerCase());
                                                r.setToSave(true);
                                            }
                                        });
                                        r.getMembers().forEach(rp->{
                                            if (rp.getUUID().equalsIgnoreCase(p.getUniqueId().toString()) && !rp.getPlayerName().equalsIgnoreCase(p.getName())){
                                                rp.setPlayerName(p.getName().toLowerCase());
                                                r.setToSave(true);
                                            }
                                        });

                                    } else {

                                        // Update uuids based on player names
                                        r.getLeaders().forEach(rp->{
                                            if (rp.getPlayerName().equalsIgnoreCase(p.getName()) && !rp.getUUID().equalsIgnoreCase(p.getUniqueId().toString())){
                                                rp.setUUID(p.getUniqueId().toString());
                                                r.setToSave(true);
                                            }
                                        });
                                        r.getAdmins().forEach(rp->{
                                            if (rp.getPlayerName().equalsIgnoreCase(p.getName()) && !rp.getUUID().equalsIgnoreCase(p.getUniqueId().toString())){
                                                rp.setUUID(p.getUniqueId().toString());
                                                r.setToSave(true);
                                            }
                                        });
                                        r.getMembers().forEach(rp->{
                                            if (rp.getPlayerName().equalsIgnoreCase(p.getName()) && !rp.getUUID().equalsIgnoreCase(p.getUniqueId().toString())){
                                                rp.setUUID(p.getUniqueId().toString());
                                                r.setToSave(true);
                                            }
                                        });
                                    }
                                });
                            });
                            if (save[0]){
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
                            RedProtect.get().logger.success("[" + RedProtectUtil.SingleToFiles() + "]" + " regions converted to your own files with success");
                            return cmdr;
                        }

                        if (args[0].equalsIgnoreCase("files-to-single")) {
                            RedProtect.get().logger.success("[" + RedProtectUtil.FilesToSingle() + "]" + " regions converted to unified world file with success");
                            return cmdr;
                        }

                        if (args[0].equalsIgnoreCase("fileToMysql")) {
                            try {
                                if (!RedProtectUtil.fileToMysql()) {
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
                                if (!RedProtectUtil.mysqlToFile()) {
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
                            sender.sendMessage(RedProtectUtil.toText(RedProtect.get().lang.get("general.color") + "---------------- " + RedProtect.get().container.getName() + " ----------------"));
                            sender.sendMessage(RedProtectUtil.toText(RedProtect.get().lang.get("general.color") + "Developed by &eFabioZumbi12" + RedProtect.get().lang.get("general.color") + "."));
                            sender.sendMessage(RedProtectUtil.toText(RedProtect.get().lang.get("general.color") + "For more information about the commands, type [&e/rp ?" + RedProtect.get().lang.get("general.color") + "]."));
                            sender.sendMessage(RedProtectUtil.toText(RedProtect.get().lang.get("general.color") + "For a tutorial, type [&e/rp tutorial" + RedProtect.get().lang.get("general.color") + "]."));
                            sender.sendMessage(RedProtectUtil.toText(RedProtect.get().lang.get("general.color") + "---------------------------------------------------"));
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
                                RedProtectUtil.ReadAllDB(RedProtect.get().rm.getAllRegions());
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

                    if (args.length == 2) {

                        //rp removeall <player>
                        if (checkCmd(args[0], "removeall")) {
                            int removed = RedProtect.get().rm.removeAll(args[1]);
                            if (removed <= 0) {
                                RedProtect.get().lang.sendMessage(sender, RedProtect.get().lang.get("cmdmanager.region.noneremoved"));
                            } else {
                                RedProtect.get().lang.sendMessage(sender, RedProtect.get().lang.get("cmdmanager.region.removed").replace("{regions}", removed + "").replace("{player}", args[1]));
                            }
                            return cmdr;
                        }

                        //rp regenall <player>
                        if (checkCmd(args[0], "regenall")) {
                            int regen = RedProtect.get().rm.regenAll(args[1]);
                            if (regen <= 0) {
                                RedProtect.get().lang.sendMessage(sender, RedProtect.get().lang.get("cmdmanager.region.noneregenerated"));
                            } else {
                                RedProtect.get().lang.sendMessage(sender, RedProtect.get().lang.get("cmdmanager.region.regenerated").replace("{regions}", regen + "").replace("{player}", args[1]));
                            }
                            return cmdr;
                        }

                        //rp regen stop
                        if (checkCmd(args[0], "regen") && args[1].equalsIgnoreCase("stop")) {
                            if (!RedProtect.get().hooks.WE) {
                                return cmdr;
                            }
                            RedProtectUtil.stopRegen = true;
                            RedProtect.get().lang.sendMessage(sender, "&aRegen will stop now. To continue reload the plugin!");
                            return cmdr;
                        }

                        //rp clamilimit player
                        if (checkCmd(args[0], "claimlimit")) {
                            User offp = RedProtectUtil.getUser(args[1]);

                            if (offp == null) {
                                sender.sendMessage(RedProtectUtil.toText(RedProtect.get().lang.get("cmdmanager.noplayer.thisname").replace("{player}", args[1])));
                                return cmdr;
                            }
                            int limit = RedProtect.get().ph.getPlayerClaimLimit(offp);
                            if (limit < 0 || RedProtect.get().ph.hasPerm(offp, "redprotect.limits.claim.unlimited")) {
                                sender.sendMessage(RedProtectUtil.toText(RedProtect.get().lang.get("cmdmanager.nolimit")));
                                return cmdr;
                            }

                            int currentUsed = RedProtect.get().rm.getLeaderRegions(offp.getUniqueId().toString()).size();
                            sender.sendMessage(RedProtectUtil.toText(RedProtect.get().lang.get("cmdmanager.yourclaims") + currentUsed + RedProtect.get().lang.get("general.color") + "/&e" + limit + RedProtect.get().lang.get("general.color")));
                            return cmdr;
                        }

                        //rp limit player
                        if (checkCmd(args[0], "blocklimit")) {
                            User offp = RedProtectUtil.getUser(args[1]);

                            if (offp == null) {
                                sender.sendMessage(RedProtectUtil.toText(RedProtect.get().lang.get("cmdmanager.noplayer.thisname").replace("{player}", args[1])));
                                return cmdr;
                            }
                            int limit = RedProtect.get().ph.getPlayerBlockLimit(offp);
                            if (limit < 0 || RedProtect.get().ph.hasPerm(offp, "redprotect.limits.blocks.unlimited")) {
                                sender.sendMessage(RedProtectUtil.toText(RedProtect.get().lang.get("cmdmanager.nolimit")));
                                return cmdr;
                            }

                            int currentUsed = RedProtect.get().rm.getTotalRegionSize(offp.getUniqueId().toString(), offp.getPlayer().isPresent() ? offp.getPlayer().get().getWorld().getName() : null);
                            sender.sendMessage(RedProtectUtil.toText(RedProtect.get().lang.get("cmdmanager.yourarea") + currentUsed + RedProtect.get().lang.get("general.color") + "/&e" + limit + RedProtect.get().lang.get("general.color")));
                            return cmdr;
                        }

                    }

                    if (args.length == 3) {

                        //rp regen <region> <database>
                        if (checkCmd(args[0], "regen")) {
                            if (!RedProtect.get().hooks.WE) {
                                return cmdr;
                            }
                            Optional<World> w = RedProtect.get().getServer().getWorld(args[2]);
                            if (!w.isPresent()) {
                                RedProtect.get().lang.sendMessage(sender, RedProtect.get().lang.get("cmdmanager.region.invalidworld"));
                                return cmdr;
                            }
                            Region r = RedProtect.get().rm.getRegion(args[1], w.get());
                            if (r == null) {
                                RedProtect.get().lang.sendMessage(sender, RedProtect.get().lang.get("correct.usage") + " &eInvalid region: " + args[1]);
                                return cmdr;
                            }

                            WEHook.regenRegion(r, w.get(), r.getMaxLocation(), r.getMinLocation(), 0, sender, false);
                            return cmdr;
                        }

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
                            Region r = RedProtect.get().rm.getRegion(args[1], w.get());
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

                        //rp clamilimit player world
                        if (checkCmd(args[0], "claimlimit")) {
                            User offp = RedProtectUtil.getUser(args[1]);

                            Optional<World> w = RedProtect.get().getServer().getWorld(args[2]);
                            if (!w.isPresent()) {
                                RedProtect.get().lang.sendMessage(sender, RedProtect.get().lang.get("cmdmanager.region.invalidworld"));
                                return cmdr;
                            }
                            if (offp == null) {
                                sender.sendMessage(RedProtectUtil.toText(RedProtect.get().lang.get("cmdmanager.noplayer.thisname").replace("{player}", args[1])));
                                return cmdr;
                            }
                            int limit = RedProtect.get().ph.getPlayerClaimLimit(offp);
                            if (limit < 0 || RedProtect.get().ph.hasPerm(offp, "redprotect.limits.claim.unlimited")) {
                                sender.sendMessage(RedProtectUtil.toText(RedProtect.get().lang.get("cmdmanager.nolimit")));
                                return cmdr;
                            }

                            int currentUsed = RedProtect.get().rm.getRegions(offp.getUniqueId().toString(), w.get()).size();
                            sender.sendMessage(RedProtectUtil.toText(RedProtect.get().lang.get("cmdmanager.yourclaims") + currentUsed + RedProtect.get().lang.get("general.color") + "/&e" + limit + RedProtect.get().lang.get("general.color")));
                            return cmdr;
                        }

                        //rp info <region> <world>
                        if (checkCmd(args[0], "info")) {
                            if (Sponge.getServer().getWorld(args[2]).isPresent()) {
                                Region r = RedProtect.get().rm.getRegion(args[1], Sponge.getServer().getWorld(args[2]).get());
                                if (r != null) {
                                    sender.sendMessage(RedProtectUtil.toText(RedProtect.get().lang.get("general.color") + "-----------------------------------------"));
                                    sender.sendMessage(r.info());
                                    sender.sendMessage(RedProtectUtil.toText(RedProtect.get().lang.get("general.color") + "-----------------------------------------"));
                                } else {
                                    sender.sendMessage(RedProtectUtil.toText(RedProtect.get().lang.get("correct.usage") + "&eInvalid region: " + args[1]));
                                }
                            } else {
                                sender.sendMessage(RedProtectUtil.toText(RedProtect.get().lang.get("correct.usage") + " " + "&eInvalid World: " + args[2]));
                            }
                            return cmdr;
                        }
                    }

                    if (args.length == 4) {

                        //rp addmember <player> <region> <world>
                        if (checkCmd(args[0], "addmember")) {
                            if (!RedProtect.get().getServer().getWorld(args[3]).isPresent()) {
                                sender.sendMessage(RedProtectUtil.toText(RedProtect.get().lang.get("cmdmanager.region.invalidworld")));
                                return cmdr;
                            }
                            Region r = RedProtect.get().rm.getRegion(args[2], args[3]);
                            if (r == null) {
                                sender.sendMessage(RedProtectUtil.toText(RedProtect.get().lang.get("cmdmanager.region.doesntexist") + ": " + args[2]));
                                return cmdr;
                            }
                            handleAddMember(sender, args[1], r);
                            return cmdr;
                        }

                        //rp addadmin <player> <region> <world>
                        if (checkCmd(args[0], "addadmin")) {
                            if (!RedProtect.get().getServer().getWorld(args[3]).isPresent()) {
                                sender.sendMessage(RedProtectUtil.toText(RedProtect.get().lang.get("cmdmanager.region.invalidworld")));
                                return cmdr;
                            }
                            Region r = RedProtect.get().rm.getRegion(args[2], args[3]);
                            if (r == null) {
                                sender.sendMessage(RedProtectUtil.toText(RedProtect.get().lang.get("cmdmanager.region.doesntexist") + ": " + args[2]));
                                return cmdr;
                            }
                            handleAddAdmin(sender, args[1], r);
                            return cmdr;
                        }

                        //rp addleader <player> <region> <world>
                        if (checkCmd(args[0], "addleader")) {
                            if (!RedProtect.get().getServer().getWorld(args[3]).isPresent()) {
                                sender.sendMessage(RedProtectUtil.toText(RedProtect.get().lang.get("cmdmanager.region.invalidworld")));
                                return cmdr;
                            }
                            Region r = RedProtect.get().rm.getRegion(args[2], args[3]);
                            if (r == null) {
                                sender.sendMessage(RedProtectUtil.toText(RedProtect.get().lang.get("cmdmanager.region.doesntexist") + ": " + args[2]));
                                return cmdr;
                            }
                            handleAddLeader(sender, args[1], r);
                            return cmdr;
                        }

                        //rp removemember <player> <region> <world>
                        if (checkCmd(args[0], "removemember")) {
                            if (!RedProtect.get().getServer().getWorld(args[3]).isPresent()) {
                                sender.sendMessage(RedProtectUtil.toText(RedProtect.get().lang.get("cmdmanager.region.invalidworld")));
                                return cmdr;
                            }
                            Region r = RedProtect.get().rm.getRegion(args[2], args[3]);
                            if (r == null) {
                                sender.sendMessage(RedProtectUtil.toText(RedProtect.get().lang.get("cmdmanager.region.doesntexist") + ": " + args[2]));
                                return cmdr;
                            }
                            handleRemoveMember(sender, args[1], r);
                            return cmdr;
                        }

                        //rp removeadmin <player> <region> <world>
                        if (checkCmd(args[0], "removeadmin")) {
                            if (!RedProtect.get().getServer().getWorld(args[3]).isPresent()) {
                                sender.sendMessage(RedProtectUtil.toText(RedProtect.get().lang.get("cmdmanager.region.invalidworld")));
                                return cmdr;
                            }
                            Region r = RedProtect.get().rm.getRegion(args[2], args[3]);
                            if (r == null) {
                                sender.sendMessage(RedProtectUtil.toText(RedProtect.get().lang.get("cmdmanager.region.doesntexist") + ": " + args[2]));
                                return cmdr;
                            }
                            handleRemoveAdmin(sender, args[1], r);
                            return cmdr;
                        }

                        //rp removeleader <player> <region> <world>
                        if (checkCmd(args[0], "removeleader")) {
                            if (!RedProtect.get().getServer().getWorld(args[3]).isPresent()) {
                                sender.sendMessage(RedProtectUtil.toText(RedProtect.get().lang.get("cmdmanager.region.invalidworld")));
                                return cmdr;
                            }
                            Region r = RedProtect.get().rm.getRegion(args[2], args[3]);
                            if (r == null) {
                                sender.sendMessage(RedProtectUtil.toText(RedProtect.get().lang.get("cmdmanager.region.doesntexist") + ": " + args[2]));
                                return cmdr;
                            }
                            handleRemoveLeader(sender, args[1], r);
                            return cmdr;
                        }

                        //rp flag info <region> <world>
                        if (checkCmd(args[0], "flag") && checkCmd(args[1], "info")) {
                            if (Sponge.getServer().getWorld(args[3]).isPresent()) {
                                Region r = RedProtect.get().rm.getRegion(args[2], Sponge.getServer().getWorld(args[3]).get());
                                if (r != null) {
                                    sender.sendMessage(RedProtectUtil.toText(RedProtect.get().lang.get("general.color") + "------------[" + RedProtect.get().lang.get("cmdmanager.region.flag.values") + "]------------"));
                                    sender.sendMessage(r.getFlagInfo());
                                    sender.sendMessage(RedProtectUtil.toText(RedProtect.get().lang.get("general.color") + "------------------------------------"));
                                } else {
                                    sender.sendMessage(RedProtectUtil.toText(RedProtect.get().lang.get("correct.usage") + " &eInvalid region: " + args[2]));
                                }
                            } else {
                                sender.sendMessage(RedProtectUtil.toText(RedProtect.get().lang.get("correct.usage") + " &eInvalid World: " + args[3]));
                            }
                            return cmdr;
                        }
                    }

                    if (args.length == 5) {
                        /*/rp flag <regionName> <flag> <value> <world>*/
                        if (checkCmd(args[0], "flag")) {
                            World w = null;
                            if (RedProtect.get().getServer().getWorld(args[4]).isPresent()) {
                                w = RedProtect.get().getServer().getWorld(args[4]).get();
                            }

                            if (w == null) {
                                sender.sendMessage(RedProtectUtil.toText(RedProtect.get().lang.get("correct.usage") + "&e rp flag <regionName> <flag> <value> <world>"));
                                return cmdr;
                            }
                            Region r = RedProtect.get().rm.getRegion(args[1], w);
                            if (r != null && (RedProtect.get().config.getDefFlags().contains(args[2]) || RedProtect.get().config.AdminFlags.contains(args[2]))) {
                                Object objflag = RedProtectUtil.parseObject(args[3]);
                                if (r.setFlag(RedProtect.get().getVersionHelper().getCause(sender), args[2], objflag)) {
                                    sender.sendMessage(RedProtectUtil.toText(RedProtect.get().lang.get("cmdmanager.region.flag.set").replace("{flag}", "'" + args[2] + "'") + " " + r.getFlagString(args[2])));
                                    RedProtect.get().logger.addLog("Console changed flag " + args[2] + " to " + r.getFlagString(args[2]));
                                }
                                return cmdr;
                            }
                        }
                    }

                    if (args[0].equalsIgnoreCase("list-areas")) {
                        int Page = 1;
                        if (args.length == 2){
                            try {
                                Page = Integer.parseInt(args[1]);
                            } catch (Exception ignored){}
                        }
                        sender.sendMessage(RedProtectUtil.toText(RedProtect.get().lang.get("general.color") + "-------------------------------------------------"));
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
                            for (Region r : RedProtect.get().rm.getRegionsByWorld(w)) {
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
                                                    .append(RedProtectUtil.toText("&8" + r.getName() + "(" + r.getArea() + ")"))
                                                    .onHover(TextActions.showText(RedProtectUtil.toText(RedProtect.get().lang.get("cmdmanager.list.hover").replace("{region}", r.getName()))))
                                                    .onClick(TextActions.runCommand("/rp " + getCmd("teleport") + " " + r.getName() + " " + r.getWorld())).build());
                                        } else {
                                            worldregions.append(Text.builder()
                                                    .append(RedProtectUtil.toText(RedProtect.get().lang.get("general.color") + ", &8" + r.getName() + "(" + r.getArea() + ")"))
                                                    .onHover(TextActions.showText(RedProtectUtil.toText(RedProtect.get().lang.get("cmdmanager.list.hover").replace("{region}", r.getName()))))
                                                    .onClick(TextActions.runCommand("/rp " + getCmd("teleport") + " " + r.getName() + " " + r.getWorld())).build());
                                        }
                                    } else {
                                        if (first) {
                                            first = false;
                                            worldregions.append(Text.builder()
                                                    .append(RedProtectUtil.toText("&8" + r.getName() + r.getArea())).build());
                                        } else {
                                            worldregions.append(Text.builder()
                                                    .append(RedProtectUtil.toText(RedProtect.get().lang.get("general.color") + ", &8" + r.getName() + r.getArea())).build());
                                        }
                                    }
                                    lastLocal = count;
                                }
                                //-----------

                                last += lastLocal + 1;
                                sender.sendMessage(RedProtectUtil.toText("-----"));
                                sender.sendMessage(RedProtectUtil.toText(RedProtect.get().lang.get("general.color") + RedProtect.get().lang.get("region.world").replace(":", "") + " " + colorChar + w.getName() + "[" + (min + 1) + "-" + (max + 1) + "/" + wregions.size() + "]&r: "));
                                sender.sendMessages(worldregions.append(RedProtectUtil.toText(RedProtect.get().lang.get("general.color") + ".")).build());
                            }
                        }
                        sender.sendMessage(RedProtectUtil.toText(RedProtect.get().lang.get("general.color") + "---------------- " + last + "/" + total + " -----------------"));
                        if (last < total) {
                            sender.sendMessage(RedProtectUtil.toText(RedProtect.get().lang.get("cmdmanager.region.listpage.more").replace("{player}", "" + (Page + 1))));
                        } else {
                            if (Page != 1) {
                                sender.sendMessage(RedProtectUtil.toText(RedProtect.get().lang.get("cmdmanager.region.listpage.nomore")));
                            }
                        }
                        return cmdr;
                    }

                    //rp list <player> [page]
                    if (args.length >= 2 && checkCmd(args[0], "list")) {
                        //rp list [player]
                        if (args.length == 2) {
                            getRegionforList(sender, args[1], 1);
                            return cmdr;
                        }
                        //rp list [player] [page]
                        if (args.length == 3) {
                            try {
                                int Page = Integer.parseInt(args[2]);
                                getRegionforList(sender, args[1], Page);
                                return cmdr;
                            } catch (NumberFormatException e) {
                                RedProtect.get().lang.sendMessage(sender, "cmdmanager.region.listpage.error");
                                return cmdr;
                            }
                        }
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

                .build();

        plugin.commandManager.register(plugin, redProtect, Arrays.asList("redprotect", "rp"));
    }

    public void unregisterAll() {
        plugin.commandManager.getOwnedBy(plugin.container).forEach(p -> plugin.commandManager.removeMapping(p));
    }

    private String[] getCmdKeys(String cmd) {
        if (getCmd(cmd).equalsIgnoreCase(cmd))
            return new String[]{getCmd(cmd), getCmdAlias(cmd)};
        return new String[]{cmd, getCmd(cmd), getCmdAlias(cmd)};
    }
}
