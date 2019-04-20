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

package br.net.fabiozumbi12.RedProtect.Sponge.commands.SubCommands;

import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import br.net.fabiozumbi12.RedProtect.Sponge.Region;
import br.net.fabiozumbi12.RedProtect.Sponge.commands.CommandHandler;
import br.net.fabiozumbi12.RedProtect.Sponge.config.RPConfig;
import br.net.fabiozumbi12.RedProtect.Sponge.config.RPLang;
import br.net.fabiozumbi12.RedProtect.Sponge.helpers.RPUtil;
import br.net.fabiozumbi12.RedProtect.Sponge.hooks.WEHook;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static br.net.fabiozumbi12.RedProtect.Sponge.commands.CommandHandlers.*;

public class AdminCommand implements CommandCallable {

    @Override
    public CommandResult process(CommandSource sender, String arguments) {
        if (sender instanceof Player && !RedProtect.get().ph.hasCommandPerm(sender, "admin")) {
            RPLang.sendMessage(sender, "cmdmanager.usefrom.player");
            return CommandResult.success();
        }

        CommandResult cmdr = CommandResult.success();

        String[] args = arguments.split(" ");

        if (args.length == 1) {

            if (args[0].equalsIgnoreCase("clear-kicks")) {
                RedProtect.get().denyEnter.clear();
                RedProtect.get().logger.sucess("All region kicks was clear");
                return cmdr;
            }

            if (args[0].equalsIgnoreCase("single-to-files")) {
                RedProtect.get().logger.sucess("[" + RPUtil.SingleToFiles() + "]" + " regions converted to your own files with success");
                return cmdr;
            }

            if (args[0].equalsIgnoreCase("files-to-single")) {
                RedProtect.get().logger.sucess("[" + RPUtil.FilesToSingle() + "]" + " regions converted to unified world file with success");
                return cmdr;
            }

            if (args[0].equalsIgnoreCase("fileToMysql")) {
                try {
                    if (!RPUtil.fileToMysql()) {
                        RedProtect.get().logger.severe("ERROR: Check if your 'file-type' configuration is set to 'file' before convert from FILE to Mysql.");
                        return cmdr;
                    } else {
                        RedProtect.get().cfgs.root().file_type = "mysql";
                        RedProtect.get().cfgs.save();
                        RedProtect.get().reload();
                        RedProtect.get().logger.sucess("Redprotect reloaded with Mysql as database! Ready to use!");
                        return cmdr;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return cmdr;
                }
            }

            if (args[0].equalsIgnoreCase("mysqlToFile")) {
                try {
                    if (!RPUtil.mysqlToFile()) {
                        RedProtect.get().logger.severe("ERROR: Check if your 'file-type' configuration is set to 'mysql' before convert from MYSQL to File.");
                        return cmdr;
                    } else {
                        RedProtect.get().cfgs.root().file_type = "file";
                        RedProtect.get().cfgs.save();
                        RedProtect.get().reload();
                        RedProtect.get().logger.sucess("Redprotect reloaded with File as database! Ready to use!");
                        return cmdr;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return cmdr;
                }
            }

            if (args[0].isEmpty()) {
                sender.sendMessage(RPUtil.toText(RPLang.get("general.color") + "---------------- " + RedProtect.get().container.getName() + " ----------------"));
                sender.sendMessage(RPUtil.toText(RPLang.get("general.color") + "Developed by &eFabioZumbi12" + RPLang.get("general.color") + "."));
                sender.sendMessage(RPUtil.toText(RPLang.get("general.color") + "For more information about the commands, type [&e/rp ?" + RPLang.get("general.color") + "]."));
                sender.sendMessage(RPUtil.toText(RPLang.get("general.color") + "For a tutorial, type [&e/rp tutorial" + RPLang.get("general.color") + "]."));
                sender.sendMessage(RPUtil.toText(RPLang.get("general.color") + "---------------------------------------------------"));
                return cmdr;
            }

            if (args[0].equalsIgnoreCase("list-all")) {
                int total = 0;
                for (Region r : RedProtect.get().rm.getAllRegions()) {
                    RedProtect.get().logger.info("&a[" + total + "]" + "Region: " + r.getName() + "&r | &3World: " + r.getWorld() + "&r");
                    total++;
                }
                RedProtect.get().logger.sucess(total + " regions for " + Sponge.getServer().getWorlds().size() + " worlds.");
                return cmdr;
            }

            if (args[0].equalsIgnoreCase("load-all")) {
                RedProtect.get().rm.clearDB();
                try {
                    RedProtect.get().rm.loadAll();
                    RPUtil.ReadAllDB(RedProtect.get().rm.getAllRegions());
                } catch (Exception e) {
                    RedProtect.get().logger.severe("Error on load all regions from database files:");
                    e.printStackTrace();
                }
                RedProtect.get().logger.sucess(RedProtect.get().rm.getAllRegions().size() + " regions has been loaded from database files!");
                return cmdr;
            }


            if (checkCmd(args[0], "reload")) {
                for (Player p : RedProtect.get().getGame().getServer().getOnlinePlayers()) {
                    RedProtect.get().getPVHelper().closeInventory(p);
                }
                RedProtect.get().reload();
                RedProtect.get().logger.sucess("Redprotect reloaded with success!");
                return cmdr;
            }

            if (args[0].equalsIgnoreCase("reload-config")) {
                try {
                    RedProtect.get().commandHandler.unregisterAll();

                    RedProtect.get().cfgs = new RPConfig(RedProtect.get().factory);
                    RPLang.init();

                    RedProtect.get().logger.info("Re-registering commands...");
                    RedProtect.get().commandHandler = new CommandHandler(RedProtect.get());

                    RedProtect.get().logger.sucess("Redprotect Plus configs reloaded!");
                } catch (ObjectMappingException e) {
                    RedProtect.get().logger.severe("Redprotect Plus configs NOT reloaded!");
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
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.noneremoved"));
                } else {
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.removed").replace("{regions}", removed + "").replace("{player}", args[1]));
                }
                return cmdr;
            }

            //rp regenall <player>
            if (checkCmd(args[0], "regenall")) {
                int regen = RedProtect.get().rm.regenAll(args[1]);
                if (regen <= 0) {
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.noneregenerated"));
                } else {
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.regenerated").replace("{regions}", regen + "").replace("{player}", args[1]));
                }
                return cmdr;
            }

            //rp regen stop
            if (checkCmd(args[0], "regen") && args[1].equalsIgnoreCase("stop")) {
                if (!RedProtect.get().WE) {
                    return cmdr;
                }
                RPUtil.stopRegen = true;
                RPLang.sendMessage(sender, "&aRegen will stop now. To continue reload the plugin!");
                return cmdr;
            }

            //rp clamilimit player
            if (checkCmd(args[0], "claimlimit")) {
                User offp = RPUtil.getUser(args[1]);

                if (offp == null) {
                    sender.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.noplayer.thisname").replace("{player}", args[1])));
                    return cmdr;
                }
                int limit = RedProtect.get().ph.getPlayerClaimLimit(offp);
                if (limit < 0 || RedProtect.get().ph.hasPerm(offp, "redprotect.limits.claim.unlimited")) {
                    sender.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.nolimit")));
                    return cmdr;
                }

                int currentUsed = RedProtect.get().rm.getRegions(RPUtil.PlayerToUUID(offp.getName())).size();
                sender.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.yourclaims") + currentUsed + RPLang.get("general.color") + "/&e" + limit + RPLang.get("general.color")));
                return cmdr;
            }

            //rp limit player
            if (checkCmd(args[0], "blocklimit")) {
                User offp = RPUtil.getUser(args[1]);

                if (offp == null) {
                    sender.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.noplayer.thisname").replace("{player}", args[1])));
                    return cmdr;
                }
                int limit = RedProtect.get().ph.getPlayerBlockLimit(offp);
                if (limit < 0 || RedProtect.get().ph.hasPerm(offp, "redprotect.limits.blocks.unlimited")) {
                    sender.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.nolimit")));
                    return cmdr;
                }

                int currentUsed = RedProtect.get().rm.getTotalRegionSize(RPUtil.PlayerToUUID(offp.getName()), offp.getPlayer().isPresent() ? offp.getPlayer().get().getWorld().getName() : null);
                sender.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.yourarea") + currentUsed + RPLang.get("general.color") + "/&e" + limit + RPLang.get("general.color")));
                return cmdr;
            }

        }

        if (args.length == 3) {

            //rp regen <region> <database>
            if (checkCmd(args[0], "regen")) {
                if (!RedProtect.get().WE) {
                    return cmdr;
                }
                Optional<World> w = RedProtect.get().getServer().getWorld(args[2]);
                if (!w.isPresent()) {
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.invalidworld"));
                    return cmdr;
                }
                Region r = RedProtect.get().rm.getRegion(args[1], w.get());
                if (r == null) {
                    RPLang.sendMessage(sender, RPLang.get("correct.usage") + " &eInvalid region: " + args[1]);
                    return cmdr;
                }

                WEHook.regenRegion(r, w.get(), r.getMaxLocation(), r.getMinLocation(), 0, sender, false);
                return cmdr;
            }

            //rp undo <region> <database>
            if (args[0].equalsIgnoreCase("undo")) {
                if (!RedProtect.get().WE) {
                    return cmdr;
                }
                Optional<World> w = RedProtect.get().getServer().getWorld(args[2]);
                if (!w.isPresent()) {
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.invalidworld"));
                    return cmdr;
                }
                Region r = RedProtect.get().rm.getRegion(args[1], w.get());
                if (r == null) {
                    RPLang.sendMessage(sender, RPLang.get("correct.usage") + " &eInvalid region: " + args[1]);
                    return cmdr;
                }

                if (WEHook.undo(r.getID())) {
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.regen.undo.sucess").replace("{region}", r.getName()));
                } else {
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.regen.undo.none").replace("{region}", r.getName()));
                }
                return cmdr;
            }

            //rp clamilimit player world
            if (checkCmd(args[0], "claimlimit")) {
                User offp = RPUtil.getUser(args[1]);

                Optional<World> w = RedProtect.get().getServer().getWorld(args[2]);
                if (!w.isPresent()) {
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.invalidworld"));
                    return cmdr;
                }
                if (offp == null) {
                    sender.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.noplayer.thisname").replace("{player}", args[1])));
                    return cmdr;
                }
                int limit = RedProtect.get().ph.getPlayerClaimLimit(offp);
                if (limit < 0 || RedProtect.get().ph.hasPerm(offp, "redprotect.limits.claim.unlimited")) {
                    sender.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.nolimit")));
                    return cmdr;
                }

                int currentUsed = RedProtect.get().rm.getRegions(RPUtil.PlayerToUUID(offp.getName()), w.get()).size();
                sender.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.yourclaims") + currentUsed + RPLang.get("general.color") + "/&e" + limit + RPLang.get("general.color")));
                return cmdr;
            }

            //rp info <region> <world>
            if (checkCmd(args[0], "info")) {
                if (Sponge.getServer().getWorld(args[2]).isPresent()) {
                    Region r = RedProtect.get().rm.getRegion(args[1], Sponge.getServer().getWorld(args[2]).get());
                    if (r != null) {
                        sender.sendMessage(RPUtil.toText(RPLang.get("general.color") + "-----------------------------------------"));
                        sender.sendMessage(r.info());
                        sender.sendMessage(RPUtil.toText(RPLang.get("general.color") + "-----------------------------------------"));
                    } else {
                        sender.sendMessage(RPUtil.toText(RPLang.get("correct.usage") + "&eInvalid region: " + args[1]));
                    }
                } else {
                    sender.sendMessage(RPUtil.toText(RPLang.get("correct.usage") + " " + "&eInvalid World: " + args[2]));
                }
                return cmdr;
            }
        }

        if (args.length == 4) {

            //rp addmember <player> <region> <world>
            if (checkCmd(args[0], "addmember")) {
                if (!RedProtect.get().getServer().getWorld(args[3]).isPresent()) {
                    sender.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.region.invalidworld")));
                    return cmdr;
                }
                Region r = RedProtect.get().rm.getRegion(args[2], args[3]);
                if (r == null) {
                    sender.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.region.doesntexist") + ": " + args[2]));
                    return cmdr;
                }
                handleAddMember(sender, args[1], r);
                return cmdr;
            }

            //rp addadmin <player> <region> <world>
            if (checkCmd(args[0], "addadmin")) {
                if (!RedProtect.get().getServer().getWorld(args[3]).isPresent()) {
                    sender.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.region.invalidworld")));
                    return cmdr;
                }
                Region r = RedProtect.get().rm.getRegion(args[2], args[3]);
                if (r == null) {
                    sender.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.region.doesntexist") + ": " + args[2]));
                    return cmdr;
                }
                handleAddAdmin(sender, args[1], r);
                return cmdr;
            }

            //rp addleader <player> <region> <world>
            if (checkCmd(args[0], "addleader")) {
                if (!RedProtect.get().getServer().getWorld(args[3]).isPresent()) {
                    sender.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.region.invalidworld")));
                    return cmdr;
                }
                Region r = RedProtect.get().rm.getRegion(args[2], args[3]);
                if (r == null) {
                    sender.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.region.doesntexist") + ": " + args[2]));
                    return cmdr;
                }
                handleAddLeader(sender, args[1], r);
                return cmdr;
            }

            //rp removemember <player> <region> <world>
            if (checkCmd(args[0], "removemember")) {
                if (!RedProtect.get().getServer().getWorld(args[3]).isPresent()) {
                    sender.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.region.invalidworld")));
                    return cmdr;
                }
                Region r = RedProtect.get().rm.getRegion(args[2], args[3]);
                if (r == null) {
                    sender.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.region.doesntexist") + ": " + args[2]));
                    return cmdr;
                }
                handleRemoveMember(sender, args[1], r);
                return cmdr;
            }

            //rp removeadmin <player> <region> <world>
            if (checkCmd(args[0], "removeadmin")) {
                if (!RedProtect.get().getServer().getWorld(args[3]).isPresent()) {
                    sender.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.region.invalidworld")));
                    return cmdr;
                }
                Region r = RedProtect.get().rm.getRegion(args[2], args[3]);
                if (r == null) {
                    sender.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.region.doesntexist") + ": " + args[2]));
                    return cmdr;
                }
                handleRemoveAdmin(sender, args[1], r);
                return cmdr;
            }

            //rp removeleader <player> <region> <world>
            if (checkCmd(args[0], "removeleader")) {
                if (!RedProtect.get().getServer().getWorld(args[3]).isPresent()) {
                    sender.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.region.invalidworld")));
                    return cmdr;
                }
                Region r = RedProtect.get().rm.getRegion(args[2], args[3]);
                if (r == null) {
                    sender.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.region.doesntexist") + ": " + args[2]));
                    return cmdr;
                }
                handleRemoveLeader(sender, args[1], r);
                return cmdr;
            }

            //rp kick <player> [region] [database]
            if (checkCmd(args[0], "kick")) {
                Optional<World> w = RedProtect.get().getServer().getWorld(args[3]);
                if (!w.isPresent()) {
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.invalidworld"));
                    return cmdr;
                }
                Region r = RedProtect.get().rm.getRegion(args[2], w.get());
                if (r == null) {
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.doesntexist") + ": " + args[2]);
                    return cmdr;
                }

                Optional<Player> visit = Sponge.getServer().getPlayer(args[1]);
                if (!visit.isPresent()) {
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.noplayer.online"));
                    return cmdr;
                }

                if (r.canBuild(visit.get())) {
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.cantkick.member"));
                    return cmdr;
                }
                Region rv = RedProtect.get().rm.getTopRegion(visit.get().getLocation(), this.getClass().getName());
                if (rv == null || !rv.getID().equals(r.getID())) {
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.notonregion"));
                    return cmdr;
                }

                RPUtil.DenyEnterPlayer(visit.get().getWorld(), visit.get().getTransform(), visit.get().getTransform(), r, true);

                String sec = String.valueOf(RedProtect.get().cfgs.root().region_settings.delay_after_kick_region);
                if (RedProtect.get().denyEnterRegion(r.getID(), visit.get().getName())) {
                    RPUtil.DenyEnterPlayer(visit.get().getWorld(), visit.get().getTransform(), visit.get().getTransform(), r, true);
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.kicked").replace("{player}", visit.get().getName()).replace("{region}", r.getName()).replace("{time", sec));
                } else {
                    RPLang.sendMessage(sender, RPLang.get("cmdmanager.already.cantenter").replace("{time}", sec));
                }
                return cmdr;
            }

            //rp tp <player> <region> <world>
            if (checkCmd(args[0], "teleport")) {
                Player play = null;
                if (RedProtect.get().getServer().getPlayer(args[1]).isPresent()) {
                    play = RedProtect.get().getServer().getPlayer(args[1]).get();
                }
                if (play != null) {
                    World w = null;
                    if (RedProtect.get().getServer().getWorld(args[3]).isPresent()) {
                        w = RedProtect.get().getServer().getWorld(args[3]).get();
                    }
                    if (w == null) {
                        sender.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.region.invalidworld")));
                        return cmdr;
                    }
                    Region region = RedProtect.get().rm.getRegion(args[2], w);
                    if (region == null) {
                        sender.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.region.doesntexist") + ": " + args[2]));
                        return cmdr;
                    }

                    Location<World> loc = null;
                    if (region.getTPPoint() != null) {
                        loc = new Location<>(w, region.getTPPoint().getBlockX() + 0.500, region.getTPPoint().getBlockY(), region.getTPPoint().getBlockZ() + 0.500);
                    } else {
                        int limit = 256;
                        if (w.getDimension().getType().equals(DimensionTypes.NETHER)) {
                            limit = 124;
                        }
                        for (int i = limit; i > 0; i--) {
                            BlockType mat = new Location<>(w, region.getCenterX(), i, region.getCenterZ()).getBlockType();
                            BlockType mat1 = new Location<>(w, region.getCenterX(), i + 1, region.getCenterZ()).getBlockType();
                            BlockType mat2 = new Location<>(w, region.getCenterX(), i + 2, region.getCenterZ()).getBlockType();
                            if (!mat.equals(BlockTypes.LAVA) && !mat.equals(BlockTypes.AIR) && mat1.equals(BlockTypes.AIR) && mat2.equals(BlockTypes.AIR)) {
                                loc = new Location<>(w, region.getCenterX() + 0.500, i + 1, region.getCenterZ() + 0.500);
                                break;
                            }
                        }
                    }

                    play.setLocation(loc);
                    RPLang.sendMessage(play, RPLang.get("cmdmanager.region.tp") + " " + args[2]);
                    sender.sendMessage(RPUtil.toText("&3Player teleported to " + args[2]));
                    return cmdr;
                } else {
                    sender.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.noplayer.thisname").replace("{player}", args[1])));
                    HandleHelpPage(sender, 1);
                    return cmdr;
                }
            }

            //rp flag info <region> <world>
            if (checkCmd(args[0], "flag") && checkCmd(args[1], "info")) {
                if (Sponge.getServer().getWorld(args[3]).isPresent()) {
                    Region r = RedProtect.get().rm.getRegion(args[2], Sponge.getServer().getWorld(args[3]).get());
                    if (r != null) {
                        sender.sendMessage(RPUtil.toText(RPLang.get("general.color") + "------------[" + RPLang.get("cmdmanager.region.flag.values") + "]------------"));
                        sender.sendMessage(r.getFlagInfo());
                        sender.sendMessage(RPUtil.toText(RPLang.get("general.color") + "------------------------------------"));
                    } else {
                        sender.sendMessage(RPUtil.toText(RPLang.get("correct.usage") + " &eInvalid region: " + args[2]));
                    }
                } else {
                    sender.sendMessage(RPUtil.toText(RPLang.get("correct.usage") + " &eInvalid World: " + args[3]));
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
                    sender.sendMessage(RPUtil.toText(RPLang.get("correct.usage") + "&e rp flag <regionName> <flag> <value> <world>"));
                    return cmdr;
                }
                Region r = RedProtect.get().rm.getRegion(args[1], w);
                if (r != null && (RedProtect.get().cfgs.getDefFlags().contains(args[2]) || RedProtect.get().cfgs.AdminFlags.contains(args[2]))) {
                    Object objflag = RPUtil.parseObject(args[3]);
                    if (r.setFlag(RedProtect.get().getPVHelper().getCause(sender), args[2], objflag)) {
                        sender.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.region.flag.set").replace("{flag}", "'" + args[2] + "'") + " " + r.getFlagString(args[2])));
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
            sender.sendMessage(RPUtil.toText(RPLang.get("general.color") + "-------------------------------------------------"));
            int regionsPage = RedProtect.get().cfgs.root().region_settings.region_per_page;
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
                    SimpleDateFormat dateformat = new SimpleDateFormat(RedProtect.get().cfgs.root().region_settings.date_format);
                    Date now = null;
                    try {
                        now = dateformat.parse(RPUtil.dateNow());
                    } catch (ParseException e1) {
                        RedProtect.get().logger.severe("The 'date-format' don't match with date 'now'!!");
                    }
                    Date regiondate = null;
                    try {
                        regiondate = dateformat.parse(r.getDate());
                    } catch (ParseException e) {
                        RedProtect.get().logger.severe("The 'date-format' don't match with region date!!");
                        e.printStackTrace();
                    }
                    long days = TimeUnit.DAYS.convert(now.getTime() - regiondate.getTime(), TimeUnit.MILLISECONDS);
                    for (String play : RedProtect.get().cfgs.root().purge.ignore_regions_from_players) {
                        if (r.isLeader(RPUtil.PlayerToUUID(play)) || r.isAdmin(RPUtil.PlayerToUUID(play))) {
                            break;
                        }
                    }
                    if (!r.isLeader(RedProtect.get().cfgs.root().region_settings.default_leader) && days > RedProtect.get().cfgs.root().purge.remove_oldest && r.getArea() >= RedProtect.get().cfgs.root().purge.regen.max_area_regen) {
                        wregions.add(r);
                    }
                }
                if (wregions.size() == 0) {
                    continue;
                }

                String colorChar = RedProtect.get().cfgs.root().region_settings.world_colors.get(w.getName());

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
                                        .append(RPUtil.toText("&8" + r.getName() + r.getArea()))
                                        .onHover(TextActions.showText(RPUtil.toText(RPLang.get("cmdmanager.list.hover").replace("{region}", r.getName()))))
                                        .onClick(TextActions.runCommand("/rp " + getCmd("teleport") + " " + r.getName() + " " + r.getWorld())).build());
                            } else {
                                worldregions.append(Text.builder()
                                        .append(RPUtil.toText(RPLang.get("general.color") + ", &8" + r.getName() + r.getArea()))
                                        .onHover(TextActions.showText(RPUtil.toText(RPLang.get("cmdmanager.list.hover").replace("{region}", r.getName()))))
                                        .onClick(TextActions.runCommand("/rp " + getCmd("teleport") + " " + r.getName() + " " + r.getWorld())).build());
                            }
                        } else {
                            if (first) {
                                first = false;
                                worldregions.append(Text.builder()
                                        .append(RPUtil.toText("&8" + r.getName() + r.getArea())).build());
                            } else {
                                worldregions.append(Text.builder()
                                        .append(RPUtil.toText(RPLang.get("general.color") + ", &8" + r.getName() + r.getArea())).build());
                            }
                        }
                        lastLocal = count;
                    }
                    //-----------

                    last += lastLocal + 1;
                    sender.sendMessage(RPUtil.toText("-----"));
                    sender.sendMessage(RPUtil.toText(RPLang.get("general.color") + RPLang.get("region.world").replace(":", "") + " " + colorChar + w.getName() + "[" + (min + 1) + "-" + (max + 1) + "/" + wregions.size() + "]&r: "));
                    sender.sendMessages(worldregions.append(RPUtil.toText(RPLang.get("general.color") + ".")).build());
                }
            }
            sender.sendMessage(RPUtil.toText(RPLang.get("general.color") + "---------------- " + last + "/" + total + " -----------------"));
            if (last < total) {
                sender.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.region.listpage.more").replace("{player}", "" + (Page + 1))));
            } else {
                if (Page != 1) {
                    sender.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.region.listpage.nomore")));
                }
            }
            return cmdr;
        }

        //rp list <player> [page]
        if (args.length >= 2 && checkCmd(args[0], "list")) {
            //rp list [player]
            if (args.length == 2) {
                getRegionforList(sender, RPUtil.PlayerToUUID(args[1]), 1);
                return cmdr;
            }
            //rp list [player] [page]
            if (args.length == 3) {
                try {
                    int Page = Integer.parseInt(args[2]);
                    getRegionforList(sender, RPUtil.PlayerToUUID(args[1]), Page);
                    return cmdr;
                } catch (NumberFormatException e) {
                    RPLang.sendMessage(sender, "cmdmanager.region.listpage.error");
                    return cmdr;
                }
            }
        }

        if (args[0].equalsIgnoreCase("save-all")) {
            RedProtect.get().logger.SaveLogs();
            RedProtect.get().logger.sucess(RedProtect.get().rm.saveAll(args.length == 2 && args[1].equalsIgnoreCase("-f")) + " regions saved with success!");
            return cmdr;
        }

        HandleHelpPage(sender, 1);
        return cmdr;
    }

    @Override
    public List<String> getSuggestions(@Nullable CommandSource source, String arguments, @Nullable Location<World> targetPosition) {
        List<String> consolecmds = Arrays.asList("list-areas", "clear-kicks", "kick", "files-to-single", "single-to-files", "flag", "teleport", "filetomysql", "mysqltofile", "reload", "reload-config", "save-all", "load-all", "blocklimit", "claimlimit", "list-all");

        String[] args = arguments.split(" ");
        if (args.length == 0) {
            return consolecmds;
        }

        if (args.length == 1) {
            SortedSet<String> tab = new TreeSet<>();
            for (String command : consolecmds) {
                if (command.startsWith(args[0])) {
                    tab.add(command);
                }
            }
            return new ArrayList<>(tab);
        }
        return null;
    }

    @Override
    public boolean testPermission(CommandSource source) {
        return source.hasPermission("redprotect.command.admin");
    }

    @Override
    public Optional<Text> getShortDescription(CommandSource source) {
        return Optional.of(Text.of("Admin commands"));
    }

    @Override
    public Optional<Text> getHelp(CommandSource source) {
        return Optional.of(Text.of("Commands for console or admin usage!"));
    }

    @Override
    public Text getUsage(CommandSource source) {
        return Text.of("<subcommand>");
    }
}
