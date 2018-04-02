package br.net.fabiozumbi12.RedProtect.Sponge.listeners;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import br.net.fabiozumbi12.RedProtect.Sponge.*;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult.Type;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Text.Builder;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import br.net.fabiozumbi12.RedProtect.Sponge.actions.DefineRegionBuilder;
import br.net.fabiozumbi12.RedProtect.Sponge.actions.RedefineRegionBuilder;
import br.net.fabiozumbi12.RedProtect.Sponge.config.RPConfig;
import br.net.fabiozumbi12.RedProtect.Sponge.config.RPLang;
import br.net.fabiozumbi12.RedProtect.Sponge.events.DeleteRegionEvent;
import br.net.fabiozumbi12.RedProtect.Sponge.events.RenameRegionEvent;
import br.net.fabiozumbi12.RedProtect.Sponge.hooks.WEListener;

import javax.swing.text.html.Option;


public class RPCommands implements CommandCallable {
    
	public RPCommands(){
		RedProtect.get().logger.debug("default","Loaded RPCommands...");
	}
	
    private static void sendNotInRegionMessage(Player p) {
        RPLang.sendMessage(p, "cmdmanager.region.todo.that");
    }
    
    private static void sendNoPermissionMessage(Player p) {
        RPLang.sendMessage(p, "no.permission");
    }
    
    private static String getCmd(String cmd){
    	return RPLang.get("cmdmanager.translation."+cmd);
    }
    
    private static String getCmdAlias(String cmd){
    	return RPLang.get("cmdmanager.translation."+cmd+".alias");
    }
    
    private boolean checkCmd(String arg, String cmd){
    	return arg.equalsIgnoreCase(getCmd(cmd)) || arg.equalsIgnoreCase(getCmdAlias(cmd)) || arg.equalsIgnoreCase(cmd);
    }
    
    @SuppressWarnings("deprecation")
	public CommandResult process(CommandSource sender, String arguments) throws CommandException {
    	CommandResult cmdr = CommandResult.success();
    	
		String[] args = arguments.split(" ");
				
        if (!(sender instanceof Player)) {        	
        	if (args.length == 1) {
        		if (args[0].equalsIgnoreCase("single-to-files")) {
        			RedProtect.get().logger.sucess("["+RPUtil.SingleToFiles()+"]"+" regions converted to your own files with success");
        			return cmdr;
        		}
        		
        		if (args[0].equalsIgnoreCase("files-to-single")) {
        			RedProtect.get().logger.sucess("["+RPUtil.FilesToSingle()+"]"+" regions converted to unified world file with success");
        			return cmdr;
        		}
        		
        		if (args[0].equalsIgnoreCase("fileToMysql")) {
        			try {
						if (!RPUtil.fileToMysql()){
							RedProtect.get().logger.severe("ERROR: Check if your 'file-type' configuration is set to 'file' before convert from FILE to Mysql.");
							return cmdr;
						} else {
							RedProtect.get().cfgs.setConfig("file-type", "mysql");
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
						if (!RPUtil.mysqlToFile()){
							RedProtect.get().logger.severe("ERROR: Check if your 'file-type' configuration is set to 'mysql' before convert from MYSQL to File.");
							return cmdr;
						} else {
							RedProtect.get().cfgs.setConfig("file-type", "file");
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
        			sender.sendMessage(RPUtil.toText(RPLang.get("general.color")+"---------------- "+RedProtect.get().container.getName()+" ----------------"));
                    sender.sendMessage(RPUtil.toText(RPLang.get("general.color")+"Developed by &eFabioZumbi12"+RPLang.get("general.color")+"."));
                    sender.sendMessage(RPUtil.toText(RPLang.get("general.color")+"For more information about the commands, type [&e/rp ?"+RPLang.get("general.color")+"]."));
                    sender.sendMessage(RPUtil.toText(RPLang.get("general.color")+"For a tutorial, type [&e/rp tutorial"+RPLang.get("general.color")+"]."));
                    sender.sendMessage(RPUtil.toText(RPLang.get("general.color")+"---------------------------------------------------"));
                    return cmdr;
        		}
        		                
        		if (args[0].equalsIgnoreCase("list-all")) {
        			int total = 0;
        			for (Region r:RedProtect.get().rm.getAllRegions()){
        				RedProtect.get().logger.info("&a[" + total + "]" + "Region: " + r.getName() + "&r | &3World: " + r.getWorld() +"&r");
        				total ++;
        			}
        			RedProtect.get().logger.sucess(total + " regions for " + Sponge.getServer().getWorlds().size() + " worlds.");
        			return cmdr;
        		}
        		        		
        		if (args[0].equalsIgnoreCase("save-all")) {            
        			RedProtect.get().rm.saveAll();
        			RedProtect.get().logger.SaveLogs();
            		RedProtect.get().logger.sucess(RedProtect.get().rm.getAllRegions().size() + " regions saved with success!");  
            		return cmdr;
            	}
        		if (args[0].equalsIgnoreCase("load-all")) {            
        			RedProtect.get().rm.clearDB();
        			try {
						RedProtect.get().rm.loadAll();
					} catch (Exception e) {
						RedProtect.get().logger.severe("Error on load all regions from database files:");
						e.printStackTrace();
					}
            		RedProtect.get().logger.sucess(RedProtect.get().rm.getAllRegions().size() + " regions has been loaded from database files!");  
            		return cmdr;
            	}
        		
        		
        		if (checkCmd(args[0], "reload")) {
        			for (Player p:RedProtect.get().game.getServer().getOnlinePlayers()){
        				RedProtect.get().getPVHelper().closeInventory(p);
        	    	}
        			RedProtect.get().reload();
        			RedProtect.get().logger.sucess("Redprotect reloaded with success!");
            		return cmdr;
            	}   
        		
        		if (args[0].equalsIgnoreCase("reload-config")) {
        			RedProtect.get().cfgs = new RPConfig();
                    RPLang.init();
        			RedProtect.get().logger.sucess("Redprotect Plus configs reloaded!");
        			return cmdr;
        		} 		
        	} 
        	
        	if(args.length == 2){
        		
        		//rp removeall <player>
        		if (checkCmd(args[0], "removeall")) {
        			int removed = RedProtect.get().rm.removeAll(args[1]);
        			if (removed <= 0){
        				RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.noneremoved"));
        			} else {
        				RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.removed").replace("{regions}", removed+"").replace("{player}", args[1]));
        			}        			
        			return cmdr;
        		}
        		
        		//rp regenall <player>
        		if (checkCmd(args[0], "regenall")) {
        			int regen = RedProtect.get().rm.regenAll(args[1]);
        			if (regen <= 0){
        				RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.noneregenerated"));
        			} else {
        				RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.regenerated").replace("{regions}", regen+"").replace("{player}", args[1]));
        			}
        			return cmdr;
        		}
        		   		
        		//rp regen stop
        		if (checkCmd(args[0], "regen") && args[1].equalsIgnoreCase("stop")) {
        			if (!RedProtect.get().WE){
        				return cmdr;
        			}
        			RPUtil.stopRegen = true;
        			RPLang.sendMessage(sender, "&aRegen will stop now. To continue reload the plugin!");
        			return cmdr;
        		}
        		
        		//rp list <player>
        		if (checkCmd(args[0], "list")){        			
        			getRegionforList(sender, RPUtil.PlayerToUUID(args[1]), 1);
        			return cmdr;
        		}
        		
        		//rp clamilimit player
        		if  (checkCmd(args[0], "claimlimit")){ 
        			User offp = RPUtil.getUser(args[1]);
        			
                	if (offp == null){
                		sender.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.noplayer.thisname").replace("{player}", args[1])));
                		return cmdr;
                	}
                	int limit = RedProtect.get().ph.getPlayerClaimLimit(offp);
                    if (limit < 0 || RedProtect.get().ph.hasPerm(offp, "redprotect.limit.claim.unlimited")) {
                    	sender.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.nolimit")));
                        return cmdr;
                    }
                    
                    int currentUsed = RedProtect.get().rm.getRegions(RPUtil.PlayerToUUID(offp.getName())).size();
                    sender.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.yourclaims") + currentUsed + RPLang.get("general.color") + "/&e" + limit + RPLang.get("general.color")));
                    return cmdr;
        		}
        		
        		//rp limit player
        		if (checkCmd(args[0], "limit")) {
        			User offp = RPUtil.getUser(args[1]);
        			
                	if (offp == null){
                		sender.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.noplayer.thisname").replace("{player}", args[1])));
                		return cmdr;
                	}
                	int limit = RedProtect.get().ph.getPlayerBlockLimit(offp);
                    if (limit < 0 || RedProtect.get().ph.hasPerm(offp, "redprotect.limit.blocks.unlimited")) {
                    	sender.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.nolimit")));
                        return cmdr;
                    }
                    
                    int currentUsed = RedProtect.get().rm.getTotalRegionSize(RPUtil.PlayerToUUID(offp.getName()), offp.getPlayer().isPresent() ? offp.getPlayer().get().getWorld().getName():null);
                    sender.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.yourarea") + currentUsed + RPLang.get("general.color") + "/&e" + limit + RPLang.get("general.color")));
                    return cmdr;
        		}
        		
        	}
            
        	if (args.length == 3){
        		//rp clamilimit player world
        		if  (checkCmd(args[0], "claimlimit")){ 
        			User offp = RPUtil.getUser(args[1]);
        			
        			World w = null;
        			if (RedProtect.get().serv.getWorld(args[2]).isPresent()){
        				w = RedProtect.get().serv.getWorld(args[2]).get();
        			}
                	if (offp == null){
                		sender.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.noplayer.thisname").replace("{player}", args[1])));
                		return cmdr;
                	}
                	int limit = RedProtect.get().ph.getPlayerClaimLimit(offp);
                    if (limit < 0 || RedProtect.get().ph.hasPerm(offp, "redprotect.limit.claim.unlimited")) {
                    	sender.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.nolimit")));
                        return cmdr;
                    }
                    
                    if (w == null){
                    	sender.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.region.invalidworld")));
                    	return cmdr;
                    }
                    
                    int currentUsed = RedProtect.get().rm.getRegions(RPUtil.PlayerToUUID(offp.getName()), w).size();
                    sender.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.yourclaims") + currentUsed + RPLang.get("general.color") + "/&e" + limit + RPLang.get("general.color")));
                    return cmdr;
        		}
        		 
        		
        		if  (args[0].equalsIgnoreCase("setconfig")){
        			if (args[1].contains("debug-messages") || args[1].contains("file-type")){
        				Object from = RedProtect.get().cfgs.getObject(args[1]); 
            			if (args[2].equals("true") || args[2].equals("false")){
            				RedProtect.get().cfgs.setConfig(args[1], Boolean.parseBoolean(args[2]));
            			} else {
            				try {
                				int value = Integer.parseInt(args[2]);
                				RedProtect.get().cfgs.setConfig(args[1], value);
                		    } catch(NumberFormatException ex){
                		    	RedProtect.get().cfgs.setConfig(args[1], args[2]);
                		    }
            			}
            			sender.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.configset")+" "+from+" > "+args[2]));
            			RedProtect.get().cfgs.save();
            			return cmdr;
            		} else {
            			sender.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.confignotset")+" "+args[1]));
            			return cmdr;
            		}
        		}


        		//rp info <region> <world>
        		if (checkCmd(args[0], "info")) {
        			if (Sponge.getServer().getWorld(args[2]).isPresent()){
        				Region r = RedProtect.get().rm.getRegion(args[1], Sponge.getServer().getWorld(args[2]).get());
        				if (r != null){
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
					Region r = RedProtect.get().rm.getRegion(args[2], args[3]);
					if (r == null){
						sender.sendMessage(RPUtil.toText("No regions by name "+args[2]+" or by world "+args[3]));
						return cmdr;
					}
					handleAddMember(sender, args[1], r);
					return cmdr;
				}

				//rp addadmin <player> <region> <world>
				if (checkCmd(args[0], "addadmin")) {
					Region r = RedProtect.get().rm.getRegion(args[2], args[3]);
					if (r == null){
						sender.sendMessage(RPUtil.toText("No regions by name "+args[2]+" or by world "+args[3]));
						return cmdr;
					}
					handleAddAdmin(sender, args[1], r);
					return cmdr;
				}

				//rp addleader <player> <region> <world>
				if (checkCmd(args[0], "addleader")) {
					Region r = RedProtect.get().rm.getRegion(args[2], args[3]);
					if (r == null){
						sender.sendMessage(RPUtil.toText("No regions by name "+args[2]+" or by world "+args[3]));
						return cmdr;
					}
					handleAddLeader(sender, args[1], r);
					return cmdr;
				}

				//rp removemember <player> <region> <world>
				if (checkCmd(args[0], "removemember")) {
					Region r = RedProtect.get().rm.getRegion(args[2], args[3]);
					if (r == null){
						sender.sendMessage(RPUtil.toText("No regions by name "+args[2]+" or by world "+args[3]));
						return cmdr;
					}
					handleRemoveMember(sender, args[1], r);
					return cmdr;
				}

				//rp removeadmin <player> <region> <world>
				if (checkCmd(args[0], "removeadmin")) {
					Region r = RedProtect.get().rm.getRegion(args[2], args[3]);
					if (r == null){
						sender.sendMessage(RPUtil.toText("No regions by name "+args[2]+" or by world "+args[3]));
						return cmdr;
					}
					handleRemoveAdmin(sender, args[1], r);
					return cmdr;
				}

				//rp removeleader <player> <region> <world>
				if (checkCmd(args[0], "removeleader")) {
					Region r = RedProtect.get().rm.getRegion(args[2], args[3]);
					if (r == null){
						sender.sendMessage(RPUtil.toText("No regions by name "+args[2]+" or by world "+args[3]));
						return cmdr;
					}
					handleRemoveLeader(sender, args[1], r);
					return cmdr;
				}

				//rp tp <player> <region> <world>
        		if (checkCmd(args[0], "teleport")){
                	Player play = null;
                	if (RedProtect.get().serv.getPlayer(args[1]).isPresent()){
                		play = RedProtect.get().serv.getPlayer(args[1]).get();
                	}
                	if (play != null){
                		World w = null;
                		if (RedProtect.get().serv.getWorld(args[3]).isPresent()){
                			w = RedProtect.get().serv.getWorld(args[3]).get();
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
                    	if (region.getTPPoint() != null){
                			loc = new Location<>(w, region.getTPPoint().getBlockX() + 0.500, region.getTPPoint().getBlockY(), region.getTPPoint().getBlockZ() + 0.500);
                    	} else {
                    		int limit = 256;
                        	if (w.getDimension().equals(DimensionTypes.NETHER)){
                        		limit = 124;
                        	}
                        	for (int i = limit; i > 0; i--){
                        		BlockType mat = new Location<>(w, region.getCenterX(), i, region.getCenterZ()).getBlockType();
                        		BlockType mat1 = new Location<>(w, region.getCenterX(), i + 1, region.getCenterZ()).getBlockType();
                        		BlockType mat2 = new Location<>(w, region.getCenterX(), i + 2, region.getCenterZ()).getBlockType();
                        		if (!mat.equals(BlockTypes.LAVA) && !mat.equals(BlockTypes.AIR) && mat1.equals(BlockTypes.AIR) && mat2.equals(BlockTypes.AIR)){
                        			loc = new Location<>(w, region.getCenterX() + 0.500, i + 1, region.getCenterZ() + 0.500);
                        			break;
                        		}
                        	}
                    	}

                    	play.setLocation(loc);
            			RPLang.sendMessage(play,RPLang.get("cmdmanager.region.tp") + " " + args[2]);
            			sender.sendMessage(RPUtil.toText("&3Player teleported to " + args[2]));
                		return cmdr;
                	} else {
                		sender.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.noplayer.thisname").replace("{player}", args[1])));
                		HandleHelPage(sender, 1);
                		return cmdr;
                	}
        		}

        		//rp flag info <region> <world>
        		if (checkCmd(args[0], "flag") && checkCmd(args[1], "info") ) {
        			if (Sponge.getServer().getWorld(args[3]).isPresent()){
        				Region r = RedProtect.get().rm.getRegion(args[2], Sponge.getServer().getWorld(args[3]).get());
        				if (r != null){
        					sender.sendMessage(RPUtil.toText(RPLang.get("general.color") + "------------[" + RPLang.get("cmdmanager.region.flag.values") + "]------------"));
        					sender.sendMessage(r.getFlagInfo());
                            sender.sendMessage(RPUtil.toText(RPLang.get("general.color") + "------------------------------------"));
        				} else {
        					sender.sendMessage(RPUtil.toText(RPLang.get("correct.usage") + "&eInvalid region: " + args[2]));
        				}
        			} else {
        				sender.sendMessage(RPUtil.toText(RPLang.get("correct.usage") + "&eInvalid World: " + args[3]));
        			}
                    return cmdr;
                }
            }

    		if (args.length == 5){
    			/*/rp flag <regionName> <flag> <value> <world>*/
    			if  (checkCmd(args[0], "flag")){
    				World w = null;
    				if (RedProtect.get().serv.getWorld(args[4]).isPresent()){
    					w = RedProtect.get().serv.getWorld(args[4]).get();
    				}

        			if (w == null){
        				sender.sendMessage(RPUtil.toText(RPLang.get("correct.usage") + "&e rp flag <regionName> <flag> <value> <world>"));
        				return cmdr;
        			}
        			Region r = RedProtect.get().rm.getRegion(args[1], w);
        			if (r != null && (RedProtect.get().cfgs.getDefFlags().contains(args[2]) || RedProtect.get().cfgs.AdminFlags.contains(args[2]))){
        				Object objflag = RPUtil.parseObject(args[3]);
        				r.setFlag(args[2], objflag);
        				sender.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.region.flag.set").replace("{flag}", "'"+args[2]+"'") + " " + r.getFlagString(args[2])));
        				RedProtect.get().logger.addLog("Console changed flag "+args[2]+" to "+r.getFlagString(args[2]));
        				return cmdr;
        			}
    			}
    		}
        	HandleHelPage(sender, 1);
            return cmdr;
        }

        //commands as player
        final Player player = (Player)sender;

        if (args.length == 1) {

        	//rp regen
    		if (checkCmd(args[0], "regen") && player.hasPermission("redprotect.regen")) {
    			if (!RedProtect.get().WE){
    				return cmdr;
    			}
    			Region r = RedProtect.get().rm.getTopRegion(player.getLocation());
    			if (r == null){
    				RPLang.sendMessage(player, "cmdmanager.region.doesexists");
    				return cmdr;
    			}

    			WEListener.regenRegion(r, Sponge.getServer().getWorld(r.getWorld()).get(), r.getMaxLocation(), r.getMinLocation(), 0, sender, false);
    			return cmdr;
    		}

    		//rp undo
    		if (args[0].equalsIgnoreCase("undo") && player.hasPermission("redprotect.regen")) {
    			if (!RedProtect.get().WE){
    				return cmdr;
    			}
    			Region r = RedProtect.get().rm.getTopRegion(player.getLocation());
    			if (r == null){
    				RPLang.sendMessage(player, "cmdmanager.region.doesexists");
    				return cmdr;
    			}

    			if (WEListener.undo(r.getID())){
                	RPLang.sendMessage(sender, RPLang.get("cmdmanager.regen.undo.sucess").replace("{region}", r.getName()));
				} else {
					RPLang.sendMessage(sender, RPLang.get("cmdmanager.regen.undo.none").replace("{region}", r.getName()));
				}
    			return cmdr;
    		}

        	String claimmode = RedProtect.get().cfgs.getWorldClaimType(player.getWorld().getName());
        	if (claimmode.equalsIgnoreCase("WAND") || claimmode.equalsIgnoreCase("BOTH") || RedProtect.get().ph.hasGenPerm(player, "redefine")){
        		//rp pos1
        		if (checkCmd(args[0], "pos1")){
                	Location<World> pl = player.getLocation();
                	RedProtect.get().firstLocationSelections.put(player, pl);
            		player.sendMessage(RPUtil.toText(RPLang.get("playerlistener.wand1") + RPLang.get("general.color") + " (&6" + pl.getBlockX() + RPLang.get("general.color") + ", &6" + pl.getBlockY() + RPLang.get("general.color") + ", &6" + pl.getBlockZ() + RPLang.get("general.color") + ")."));
                    //show preview border
                    if (RedProtect.get().firstLocationSelections.containsKey(player) && RedProtect.get().secondLocationSelections.containsKey(player)){
                        Location<World> loc1 = RedProtect.get().firstLocationSelections.get(player);
                        Location<World> loc2 = RedProtect.get().secondLocationSelections.get(player);
                        if (loc1.getPosition().distanceSquared(loc2.getPosition()) > RedProtect.get().cfgs.getInt("region-settings.define-max-distance") && !player.hasPermission("redprotect.bypass.define-max-distance")){
                            Double dist = loc1.getPosition().distanceSquared(loc2.getPosition());
                            RPLang.sendMessage(player, String.format(RPLang.get("regionbuilder.selection.maxdefine"), RedProtect.get().cfgs.getInt("region-settings.define-max-distance"), dist.intValue()));
                        } else {
                            RPUtil.addBorder(player, RPUtil.get4Points(loc1, loc2, player.getLocation().getBlockY()));
                        }
                    }
                    return cmdr;
            	} else

            	//rp pos2
            	if (checkCmd(args[0], "pos2")){
                	Location<World> pl = player.getLocation();
                	RedProtect.get().secondLocationSelections.put(player, pl);
            		player.sendMessage(RPUtil.toText(RPLang.get("playerlistener.wand2") + RPLang.get("general.color") + " (&6" + pl.getBlockX() + RPLang.get("general.color") + ", &6" + pl.getBlockY() + RPLang.get("general.color") + ", &6" + pl.getBlockZ() + RPLang.get("general.color") + ")."));

                    //show preview border
                    if (RedProtect.get().firstLocationSelections.containsKey(player) && RedProtect.get().secondLocationSelections.containsKey(player)){
                        Location<World> loc1 = RedProtect.get().firstLocationSelections.get(player);
                        Location<World> loc2 = RedProtect.get().secondLocationSelections.get(player);
                        if (loc1.getPosition().distanceSquared(loc2.getPosition()) > RedProtect.get().cfgs.getInt("region-settings.define-max-distance") && !RedProtect.get().ph.hasPerm(player,"redprotect.bypass.define-max-distance")){
                            Double dist = loc1.getPosition().distanceSquared(loc2.getPosition());
                            RPLang.sendMessage(player, String.format(RPLang.get("regionbuilder.selection.maxdefine"), RedProtect.get().cfgs.getInt("region-settings.define-max-distance"), dist.intValue()));
                        } else {
                            RPUtil.addBorder(player, RPUtil.get4Points(loc1, loc2, player.getLocation().getBlockY()));
                        }
                    }
                    return cmdr;
            	}
        	}

        	if (args[0].isEmpty()) {
    			sender.sendMessage(RPUtil.toText(RPLang.get("general.color")+"---------------- "+RedProtect.get().container.getName()+" ----------------"));
                sender.sendMessage(RPUtil.toText(RPLang.get("general.color")+"Developed by &eFabioZumbi12"+RPLang.get("general.color")+"."));
                sender.sendMessage(RPUtil.toText(RPLang.get("general.color")+"For more information about the commands, type [&e/rp ?"+RPLang.get("general.color")+"]."));
                sender.sendMessage(RPUtil.toText(RPLang.get("general.color")+"For a tutorial, type [&e/rp tutorial"+RPLang.get("general.color")+"]."));
                sender.sendMessage(RPUtil.toText(RPLang.get("general.color")+"---------------------------------------------------"));
                return cmdr;
    		}

        	if (checkCmd(args[0], "laccept")){
        		if (RedProtect.get().alWait.containsKey(player)){
        			//info = region+world+pname
        			String info = RedProtect.get().alWait.get(player);

        			Player lsender = Sponge.getServer().getPlayer(info.split("@")[2]).get();
        			Region r = RedProtect.get().rm.getRegion(info.split("@")[0],info.split("@")[1]);

        			String VictimUUID = player.getName();
    				if (RedProtect.get().OnlineMode){
    					VictimUUID = player.getUniqueId().toString();
    				}

        			if (r != null){

        				if (RedProtect.get().ph.getPlayerClaimLimit(player) == (RedProtect.get().rm.getRegions(VictimUUID,r.getWorld()).size()+1)){
        					RPLang.sendMessage(player,"regionbuilder.claim.limit");
        					return cmdr;
        				}

        				r.addLeader(VictimUUID);
        				RPLang.sendMessage(player, RPLang.get("cmdmanager.region.leader.youadded").replace("{region}", r.getName()) + " " + lsender.getName());
        				if (lsender != null && lsender.isOnline()){
        					RPLang.sendMessage(lsender, RPLang.get("cmdmanager.region.leader.accepted").replace("{region}", r.getName()).replace("{player}", player.getName()));
        				}
        			} else {
        				RPLang.sendMessage(player, "cmdmanager.region.doesexists");
        			}
        			RedProtect.get().alWait.remove(player);
        			return cmdr;
        		} else {
        			RPLang.sendMessage(player, "cmdmanager.norequests");
        			return cmdr;
        		}
        	}

        	if (checkCmd(args[0], "ldeny")){
        		if (RedProtect.get().alWait.containsKey(player)){
        			//info = region+world+pname
        			String info = RedProtect.get().alWait.get(player);

        			Player lsender = Sponge.getServer().getPlayer(info.split("@")[2]).get();
        			Region r = RedProtect.get().rm.getRegion(info.split("@")[0],info.split("@")[1]);

        			if (r != null){
        				RPLang.sendMessage(player, RPLang.get("cmdmanager.region.leader.youdenied").replace("{region}", r.getName()).replace("{player}", lsender.getName()));
        				if (lsender != null && lsender.isOnline()){
        					RPLang.sendMessage(lsender, RPLang.get("cmdmanager.region.leader.denied").replace("{region}", r.getName()).replace("{player}", player.getName()));
        				}
        			} else {
        				RPLang.sendMessage(player, "cmdmanager.region.doesexists");
        			}
        			RedProtect.get().alWait.remove(player);
        			return cmdr;
        		} else {
        			RPLang.sendMessage(player, "cmdmanager.norequests");
        			return cmdr;
        		}
        	}

        	if (checkCmd(args[0], "settp") && RedProtect.get().ph.hasGenPerm(player, "settp")){
        		Region r = RedProtect.get().rm.getTopRegion(player.getLocation());
        		if (r != null){
        			if (RedProtect.get().ph.hasRegionPermLeader(player, "settp", r)){
        				r.setTPPoint(player.getLocation());
            			RPLang.sendMessage(player, "cmdmanager.region.settp.ok");
            			return cmdr;
        			} else {
        				RPLang.sendMessage(player, "playerlistener.region.cantuse");
        				return cmdr;
        			}
        		} else {
    				RPLang.sendMessage(player, "cmdmanager.region.todo.that");
    				return cmdr;
        		}
        	}

        	if (checkCmd(args[0], "deltp") && RedProtect.get().ph.hasGenPerm(player, "settp")){
        		Region r = RedProtect.get().rm.getTopRegion(player.getLocation());
        		if (r != null){
        			if (RedProtect.get().ph.hasRegionPermLeader(player, "settp", r)){
        				r.setTPPoint(null);
            			RPLang.sendMessage(player, "cmdmanager.region.settp.removed");
            			return cmdr;
        			} else {
        				RPLang.sendMessage(player, "playerlistener.region.cantuse");
        				return cmdr;
        			}
        		} else {
    				RPLang.sendMessage(player, "cmdmanager.region.todo.that");
    				return cmdr;
        		}
        	}

        	if (checkCmd(args[0], "border") && RedProtect.get().ph.hasGenPerm(player, "border")){
        		Region r = RedProtect.get().rm.getTopRegion(player.getLocation());
        		if (r != null){
        			RPUtil.addBorder(player, r.get4Points(player.getLocation().getBlockY()));
        			return cmdr;
        		} else {
    				RPLang.sendMessage(player, "cmdmanager.region.todo.that");
    				return cmdr;
        		}
        	}

        	if (checkCmd(args[0], "cancelbuy") && RedProtect.get().ph.hasGenPerm(player, "redprotect.eco.cancelbuy")){
        		Region r = RedProtect.get().rm.getTopRegion(player.getLocation());
        		if (r == null){
        			RPLang.sendMessage(player, "cmdmanager.region.todo.that");
    				return cmdr;
        		}

        		if (r.isLeader(RPUtil.PlayerToUUID(player.getName()))){
        			if (r.isForSale()){
            			r.setFlag("for-sale", false);
            			r.setWelcome("");
            			if (r.leaderSize() == 0){
            				if (RedProtect.get().cfgs.getEcoBool("rename-region")){
            					RedProtect.get().rm.renameRegion(RPUtil.nameGen(player.getName(),r.getWorld()), r);
            				}
            				r.addLeader(RPUtil.PlayerToUUID(player.getName()));
            			} else {
            				if (RedProtect.get().cfgs.getEcoBool("rename-region")){
            					RedProtect.get().rm.renameRegion(RPUtil.nameGen(RPUtil.UUIDtoPlayer(r.getLeaders().get(0)),r.getWorld()),r);
            				}
            			}
            			RPLang.sendMessage(player, "economy.region.cancelbuy");
            			RedProtect.get().logger.addLog("(World "+r.getWorld()+") Player "+player.getName()+" cancelled buy stat of region "+r.getName());
        				return cmdr;
            		} else {
            			RPLang.sendMessage(player, "economy.region.buy.notforsale");
            			return cmdr;
            		}
        		} else {
        			RPLang.sendMessage(player, "economy.region.sell.own");
        			return cmdr;
        		}
        	}

        	if (checkCmd(args[0], "value") && RedProtect.get().ph.hasGenPerm(player, "value")){
        		Region r = RedProtect.get().rm.getTopRegion(player.getLocation());
        		if (r != null){
        			if (RedProtect.get().ph.hasRegionPermLeader(player, "value", r)){
        				if (r.getArea() < RedProtect.get().cfgs.getEcoInt("max-area-toget-value")){
            				RPLang.sendMessage(player, RPLang.get("cmdmanager.value.is").replace("{value}", RPEconomy.getFormatted(RPEconomy.getRegionValue(r)) + " " +RedProtect.get().cfgs.getEcoString("economy-name")));
            				RedProtect.get().logger.debug("player","Region Value: "+RPEconomy.getRegionValue(r));
                			return cmdr;
            			} else {
            				RPLang.sendMessage(player, RPLang.get("cmdmanager.value.areabig").replace("{maxarea}", RedProtect.get().cfgs.getEcoInt("max-area-toget-value").toString()));
            				return cmdr;
            			}
        			} else {
        				RPLang.sendMessage(player, "playerlistener.region.cantuse");
        				return cmdr;
        			}
        		} else {
    				RPLang.sendMessage(player, "cmdmanager.region.todo.that");
    				return cmdr;
    			}
        	}

        	if (args[0].equalsIgnoreCase("save-all")) {
        		if (RedProtect.get().ph.hasGenPerm(player, "save-all")) {
        			RedProtect.get().rm.saveAll();
        			RedProtect.get().logger.SaveLogs();
        			RPLang.sendMessage(player,"&a" + RedProtect.get().rm.getAllRegions().size() + " regions saved with success!");
        			return cmdr;
        		}
        	}
        	if (args[0].equalsIgnoreCase("load-all")) {
        		if (RedProtect.get().ph.hasGenPerm(player, "load-all")) {
        			RedProtect.get().rm.clearDB();
        			try {
    					RedProtect.get().rm.loadAll();
    				} catch (Exception e) {
    					RPLang.sendMessage(player, "Error on load all regions from database files:");
    					e.printStackTrace();
    				}
        			RPLang.sendMessage(player,"&a" + RedProtect.get().rm.getAllRegions().size() + " regions has been loaded from database files!");
            		return cmdr;
        		}
        	}
        	if (checkCmd(args[0], "define")){
        		if (!RedProtect.get().ph.hasGenPerm(player, "define")) {
                    RPLang.sendMessage(player, "no.permission");
                    return cmdr;
                }
        		String serverName = RedProtect.get().cfgs.getString("region-settings.default-leader");
                String name = RPUtil.nameGen(serverName, player.getWorld().getName());

                RegionBuilder rb2 = new DefineRegionBuilder(player, RedProtect.get().firstLocationSelections.get(player), RedProtect.get().secondLocationSelections.get(player), name, serverName, new LinkedList<>(), true);
                if (rb2.ready()) {
                    Region r2 = rb2.build();
                    RPLang.sendMessage(player,RPLang.get("cmdmanager.region.created") + " " + r2.getName() + ".");
                    RedProtect.get().rm.add(r2, player.getWorld());

                    RedProtect.get().firstLocationSelections.remove(player);
                    RedProtect.get().secondLocationSelections.remove(player);

                    RedProtect.get().logger.addLog("(World "+r2.getWorld()+") Player "+player.getName()+" DEFINED region "+r2.getName());
                }
                return cmdr;
        	}

        	//rp claim
        	if (checkCmd(args[0], "claim")){
        		if ((!claimmode.equalsIgnoreCase("WAND") && !claimmode.equalsIgnoreCase("BOTH")) && !RedProtect.get().ph.hasGenPerm(player, "claim")) {
                    RPLang.sendMessage(player, "blocklistener.region.blockmode");
                    return cmdr;
                }
                String name = RPUtil.nameGen(player.getName(), player.getWorld().getName());
                String leader = player.getUniqueId().toString();
                if (!RedProtect.get().OnlineMode){
                	leader = player.getName().toLowerCase();
            	}
                RegionBuilder rb2 = new DefineRegionBuilder(player, RedProtect.get().firstLocationSelections.get(player), RedProtect.get().secondLocationSelections.get(player), name, leader, new LinkedList<>(), false);
                if (rb2.ready()) {
                    Region r2 = rb2.build();
                    RPLang.sendMessage(player,RPLang.get("cmdmanager.region.created") + " " + r2.getName() + ".");
                    RedProtect.get().rm.add(r2, player.getWorld());

                    RedProtect.get().firstLocationSelections.remove(player);
                    RedProtect.get().secondLocationSelections.remove(player);

                    RedProtect.get().logger.addLog("(World "+r2.getWorld()+") Player "+player.getName()+" CLAIMED region "+r2.getName());
                }
                return cmdr;
        	}

        	if (checkCmd(args[0], "reload") && RedProtect.get().ph.hasGenPerm(player, "reload")) {
        		for (Player p:RedProtect.get().game.getServer().getOnlinePlayers()){
        			RedProtect.get().getPVHelper().closeInventory(p);
    	    	}
        		RedProtect.get().reload();
        		RPLang.sendMessage(player, "cmdmanager.reloaded");
        		return cmdr;
        	}

        	if (checkCmd(args[0], "wand") && player.hasPermission("redprotect.magicwand")) {
        		Inventory inv = player.getInventory();
        		ItemType mat = (ItemType)RPUtil.getRegistryFor(ItemType.class, RedProtect.get().cfgs.getString("wands.adminWandID"));
        		ItemStack item = ItemStack.of(mat, 1);
        		item.offer(Keys.ITEM_ENCHANTMENTS, new ArrayList<>());
        		Iterable<Slot> slotIter = player.getInventory().slots();

        		for (Slot slot:slotIter) {
    			    if (slot.peek().isPresent()) {
    			    	ItemStack stack = slot.peek().get();
    			    	if (stack.getItem().equals(mat)){
    			    		RPLang.sendMessage(player,RPLang.get("cmdmanager.wand.nospace").replace("{item}", mat.getName()));
    			    		return cmdr;
    			    	}
    			    }
    			}

    			if (inv.query(Inventory.class).offer(item).getType().equals(Type.SUCCESS)){
    				RPLang.sendMessage(player,RPLang.get("cmdmanager.wand.given").replace("{item}", mat.getName()));
    			} else {
    				RPLang.sendMessage(player,RPLang.get("cmdmanager.wand.nospace").replace("{item}", mat.getName()));
    			}

		        return cmdr;
        	}

            if (checkCmd(args[0], "help")) {
                HandleHelPage(sender, 1);
                return cmdr;
            }

            if (checkCmd(args[0], "tutorial")) {
                RPLang.sendMessage(player,"cmdmanager.tutorial");
                RPLang.sendMessage(player,"cmdmanager.tutorial1");
                RPLang.sendMessage(player,"cmdmanager.tutorial2");
                RPLang.sendMessage(player,"cmdmanager.tutorial3");
                RPLang.sendMessage(player,"cmdmanager.tutorial4");
                RPLang.sendMessage(player,"cmdmanager.tutorial5");
                return cmdr;
            }
            if (checkCmd(args[0], "near")) {
                if (RedProtect.get().ph.hasUserPerm(player, "redprotect.near")) {
                    Set<Region> regions = RedProtect.get().rm.getRegionsNear(player, 60, player.getWorld());
                    if (regions.size() == 0) {
                        RPLang.sendMessage(player, "cmdmanager.noregions.nearby");
                    }
                    else {
                        Iterator<Region> i = regions.iterator();
                        player.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.regionwith40")));
                        player.sendMessage(RPUtil.toText(RPLang.get("general.color") + "------------------------------------"));
                        while (i.hasNext()) {
                            Region r = i.next();
                            player.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.region.name") + r.getName() + RPLang.get("general.color") + " | "+RPLang.get("region.center")+" (&6X,Z"+RPLang.get("general.color")+"): &6" +  r.getCenterX() + ", "  + r.getCenterZ()));
                        }
                        player.sendMessage(RPUtil.toText(RPLang.get("general.color") + "------------------------------------"));
                    }
                }
                else {
                    RPLang.sendMessage(player, "no.permission");
                }
                return cmdr;
            }


            if (checkCmd(args[0], "flag")) {
            	if (RedProtect.get().ph.hasUserPerm(player, "flaggui")) {
        			Region r = RedProtect.get().rm.getTopRegion(player.getLocation());
        			if (r != null){
        				if (r.isLeader(player) || r.isAdmin(player) || RedProtect.get().ph.hasAdminFlagPerm(player, "redprotect.admin.flaggui")){
        					RPGui gui = new RPGui(r.getName(), player, r, RedProtect.get().cfgs.getGuiMaxSlot());
    						gui.open();
                			return cmdr;
        				} else {
        					sendNoPermissionMessage(player);
        					return cmdr;
        				}
        			} else {
        				RPLang.sendMessage(player, "cmdmanager.region.todo.that");
        				return cmdr;
        			}
        		}
            }

          //rp renew-rent
        	if (checkCmd(args[0], "renew-rent") && RedProtect.get().ph.hasGenPerm(player, "redprotect.rent.renew-rent")) {
        		Region r = RedProtect.get().rm.getTopRegion(player.getLocation());
    			if (r == null){
    				RPLang.sendMessage(player, "cmdmanager.region.doesexists");
    				return cmdr;
    			}
    			String puuid = RPUtil.PlayerToUUID(player.getName());
    			if (!r.isRentFor(puuid)){
    				RPLang.sendMessage(player, "cmdmanager.rent.younotrented");
    				return cmdr;
    			}

    			if (RPUtil.getNowMillis() != r.getRentDateMillis(puuid) && !RedProtect.get().cfgs.getBool("region-settings.rent.renew-anytime")){
    				RPLang.sendMessage(player, RPLang.get("cmdmanager.rent.cantrenewanytime").replace("{renew}", r.getRentDateFormated(puuid)));
    				return cmdr;
    			}

    			UniqueAccount acc = RedProtect.get().econ.getOrCreateAccount(player.getUniqueId()).get();
    			if (acc.getBalance(RedProtect.get().econ.getDefaultCurrency()).doubleValue() >= r.getRentValue(puuid)){
    				Calendar cal = Calendar.getInstance();
    				String[] opts = RedProtect.get().cfgs.getString("region-settings.rent.command-renew-adds").split(":");
    				if (opts[1].equalsIgnoreCase("MONTH")){
    					cal.add(Calendar.MONTH, Integer.valueOf(opts[0]));
    				} else if (opts[1].equalsIgnoreCase("DAY")){
    					cal.add(Calendar.DAY_OF_MONTH, Integer.valueOf(opts[0]));
    				}
    				acc.withdraw(RedProtect.get().econ.getDefaultCurrency(), BigDecimal.valueOf(r.getRentValue(puuid)), RedProtect.get().getPVHelper().getCause(player));
    				r.setRent(puuid, cal.getTimeInMillis());
    				RPLang.sendMessage(player, RPLang.get("cmdmanager.rent.renewsuccess").replace("{region}", r.getName()).replace("{value}", RPEconomy.getFormatted(r.getRentValue(puuid))));
    			} else {
    				RPLang.sendMessage(player, "cmdmanager.rent.renewfail");
    			}
    			return cmdr;
        	}
        }

        if (args.length == 2) {

        	//rp removeall <player>
    		if (checkCmd(args[0], "removeall") && sender.hasPermission("redprotect.removeall")) {
    			if (!RedProtect.get().WE){
    				return cmdr;
    			}
    			int removed = RedProtect.get().rm.removeAll(args[1]);
    			if (removed <= 0){
    				RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.noneremoved"));
    			} else {
    				RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.removed").replace("{regions}", removed+"").replace("{player}", args[1]));
    			}
    			return cmdr;
    		}

        	//rp regenall <player>
    		if (checkCmd(args[0], "regenall") && sender.hasPermission("redprotect.regenall")) {
    			if (!RedProtect.get().WE){
    				return cmdr;
    			}
    			int regen = RedProtect.get().rm.regenAll(args[1]);
    			if (regen <= 0){
    				RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.noneregenerated"));
    			} else {
    				RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.regenerated").replace("{regions}", regen+"").replace("{player}", args[1]));
    			}
    			return cmdr;
    		}

        	//rp regen stop
    		if (checkCmd(args[0], "regen") && args[1].equalsIgnoreCase("stop") && player.hasPermission("redprotect.regen")) {
    			if (!RedProtect.get().WE){
    				return cmdr;
    			}
    			RPUtil.stopRegen = true;
    			RPLang.sendMessage(player, "&aRegen will stop now. To continue reload the plugin!");
    			return cmdr;
    		}

        	if (checkCmd(args[0], "help")) {
        		try{
        			int page = Integer.parseInt(args[1]);
                    HandleHelPage(sender, page);
        		} catch (NumberFormatException e){
        			RPLang.sendMessage(player,RPLang.get("correct.usage") + "&e/rp ? [page]");
        		}
                return cmdr;
            }

        	//rp define [nameOfRegion]
        	if (checkCmd(args[0], "define")){
        		if (!RedProtect.get().ph.hasGenPerm(player, "define")) {
                    RPLang.sendMessage(player, "no.permission");
                    return cmdr;
                }
        		String serverName = RedProtect.get().cfgs.getString("region-settings.default-leader");
                String name = args[1].replace("/", "|");

				RegionBuilder rb2 = new DefineRegionBuilder(player, RedProtect.get().firstLocationSelections.get(player), RedProtect.get().secondLocationSelections.get(player), name, serverName, new LinkedList<>(), true);
                if (rb2.ready()) {
                    Region r2 = rb2.build();
                    RPLang.sendMessage(player,RPLang.get("cmdmanager.region.created") + " " + r2.getName() + ".");
                    RedProtect.get().rm.add(r2, player.getWorld());

                    RedProtect.get().firstLocationSelections.remove(player);
                    RedProtect.get().secondLocationSelections.remove(player);

                    RedProtect.get().logger.addLog("(World "+r2.getWorld()+") Player "+player.getName()+" DEFINED region "+r2.getName());
                }
                return cmdr;
        	}

        	//rp claim [nameOfRegion]
        	if (checkCmd(args[0], "claim")){
        		String claimmode = RedProtect.get().cfgs.getWorldClaimType(player.getWorld().getName());
        		if ((!claimmode.equalsIgnoreCase("WAND") && !claimmode.equalsIgnoreCase("BOTH")) && !player.hasPermission("redprotect.admin.claim")) {
                    RPLang.sendMessage(player, "blocklistener.region.blockmode");
                    return cmdr;
                }
                String name = args[1].replace("/", "|");
				String leader = player.getUniqueId().toString();
                if (!RedProtect.get().OnlineMode){
                	leader = player.getName().toLowerCase();
            	}
                RegionBuilder rb2 = new DefineRegionBuilder(player, RedProtect.get().firstLocationSelections.get(player), RedProtect.get().secondLocationSelections.get(player), name, leader, new LinkedList<>(), false);
                if (rb2.ready()) {
                    Region r2 = rb2.build();
                    RPLang.sendMessage(player,RPLang.get("cmdmanager.region.created") + " " + r2.getName() + ".");
                    RedProtect.get().rm.add(r2, player.getWorld());

                    RedProtect.get().firstLocationSelections.remove(player);
                    RedProtect.get().secondLocationSelections.remove(player);

                    RedProtect.get().logger.addLog("(World "+r2.getWorld()+") Player "+player.getName()+" CLAIMED region "+r2.getName());
                }
                return cmdr;
        	}

            if (checkCmd(args[0], "redefine")) {
            	Region oldRect = RedProtect.get().rm.getRegion(args[1], player.getWorld());
                if (oldRect == null) {
                    RPLang.sendMessage(player, RPLang.get("cmdmanager.region.doesntexist") + ": " + args[1]);
                    return cmdr;
                }

                if (!RedProtect.get().ph.hasRegionPermLeader(player, "redefine", oldRect)) {
                    RPLang.sendMessage(player, "no.permission");
                    return cmdr;
                }

                RedefineRegionBuilder rb = new RedefineRegionBuilder(player, oldRect, RedProtect.get().firstLocationSelections.get(player), RedProtect.get().secondLocationSelections.get(player));
                if (rb.ready()) {
                    Region r2 = rb.build();
                    RPLang.sendMessage(player,RPLang.get("cmdmanager.region.redefined") + " " + r2.getName() + ".");
                    RedProtect.get().rm.add(r2, player.getWorld());

					RedProtect.get().firstLocationSelections.remove(player);
                    RedProtect.get().secondLocationSelections.remove(player);

                    RedProtect.get().logger.addLog("(World "+r2.getWorld()+") Player "+player.getName()+" REDEFINED region "+r2.getName());
                }
                return cmdr;
            }

            //rp del-rent <player>
        	if (checkCmd(args[0], "del-rent") && RedProtect.get().ph.hasUserPerm(player, "del-rent")){
        		Region r = RedProtect.get().rm.getTopRegion(player.getLocation());
        		if (r == null){
        			RPLang.sendMessage(player, "cmdmanager.region.todo.that");
    				return cmdr;
        		}

        		if (!r.isLeader(player) && !RedProtect.get().ph.hasGenPerm(player, "redprotect.rent.others")){
        			sendNoPermissionMessage(player);
        			return cmdr;
        		}

        		if (r.isRentFor(RPUtil.PlayerToUUID(args[1]))){
        			r.removeRent(RPUtil.PlayerToUUID(args[1]));
        			RPLang.sendMessage(player, RPLang.get("cmdmanager.rent.playerremoved").replace("{region}", r.getName()));
        		} else {
        			RPLang.sendMessage(player, RPLang.get("cmdmanager.rent.noplayerrented"));
        		}
        		return cmdr;
        	}
        }

        if (args.length == 3) {

        	//rp regen <region> <world>
    		if (checkCmd(args[0], "regen") && player.hasPermission("redprotect.regen")) {
    			if (!RedProtect.get().WE){
    				return cmdr;
    			}
    			World w = RedProtect.get().serv.getWorld(args[2]).get();
    			if (w == null){
    				RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.invalidworld"));
                	return cmdr;
                }
    			Region r = RedProtect.get().rm.getRegion(args[1], w);
    			if (r == null){
    				RPLang.sendMessage(sender, RPLang.get("correct.usage") + " &eInvalid region: " + args[1]);
    				return cmdr;
    			}

    			WEListener.regenRegion(r, Sponge.getServer().getWorld(r.getWorld()).get(), r.getMaxLocation(), r.getMinLocation(), 0, sender, false);
    			return cmdr;
    		}

    		//rp undo <region> <world>
    		if (args[0].equalsIgnoreCase("undo") && player.hasPermission("redprotect.regen")) {
    			if (!RedProtect.get().WE){
    				return cmdr;
    			}
    			World w = RedProtect.get().serv.getWorld(args[2]).get();
    			if (w == null){
    				RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.invalidworld"));
                	return cmdr;
                }
    			Region r = RedProtect.get().rm.getRegion(args[1], w);
    			if (r == null){
    				RPLang.sendMessage(sender, RPLang.get("correct.usage") + " &eInvalid region: " + args[1]);
    				return cmdr;
    			}

    			if (WEListener.undo(r.getID())){
                	RPLang.sendMessage(sender, RPLang.get("cmdmanager.regen.undo.sucess").replace("{region}", r.getName()));
				} else {
					RPLang.sendMessage(sender, RPLang.get("cmdmanager.regen.undo.none").replace("{region}", r.getName()));
				}
    			return cmdr;
    		}

        	//rp edit-rent <player> <valor/date>
        	if (checkCmd(args[0], "edit-rent") && RedProtect.get().ph.hasUserPerm(player, "edit-rent")){
        		Region r = RedProtect.get().rm.getTopRegion(player.getLocation());
        		if (r == null){
        			RPLang.sendMessage(player, "cmdmanager.region.todo.that");
    				return cmdr;
        		}

        		if (!r.isLeader(player) && !RedProtect.get().ph.hasUserPerm(player, "rent.others")){
        			sendNoPermissionMessage(player);
        			return cmdr;
        		}

        		if (!r.isRentFor(RPUtil.PlayerToUUID(args[1]))){
        			RPLang.sendMessage(player, "cmdmanager.rent.playernotrented");
        			return cmdr;
        		}

        		try {
        			int value = Integer.valueOf(args[2]);
        			r.setRent(RPUtil.PlayerToUUID(args[1]), value);
        			RPLang.sendMessage(player, RPLang.get("cmdmanager.rent.renteditok").replace("{player}", args[1]).replace("{value}", args[2]));
        			return cmdr;
        		} catch (NumberFormatException ex){
        			Calendar cal = Calendar.getInstance();
        			SimpleDateFormat sdf = new SimpleDateFormat(RedProtect.get().cfgs.getString("region-settings.date-format"));
        			try {
						cal.setTime(sdf.parse(args[2]));
						r.setRent(RPUtil.PlayerToUUID(args[1]), cal.getTimeInMillis());
						RPLang.sendMessage(player, RPLang.get("cmdmanager.rent.renteditok").replace("{player}", args[1]).replace("{value}", args[2]));
						return cmdr;
					} catch (ParseException ignored) {
					}
        		}
        	}

        	//rp claim [regionName] [leader]
        	if (checkCmd(args[0], "claim")){
        		String claimmode = RedProtect.get().cfgs.getWorldClaimType(player.getWorld().getName());
        		if ((!claimmode.equalsIgnoreCase("WAND") && !claimmode.equalsIgnoreCase("BOTH")) && !player.hasPermission("redprotect.admin.claim")) {
                    RPLang.sendMessage(player, "blocklistener.region.blockmode");
                    return cmdr;
                }
                String name = args[1].replace("/", "|");
                String leader = player.getUniqueId().toString();
                List<String> addedAdmins = new ArrayList<>();
                addedAdmins.add(RPUtil.PlayerToUUID(args[2]));
                if (!RedProtect.get().OnlineMode){
                	leader = player.getName().toLowerCase();
            	}
                RegionBuilder rb2 = new DefineRegionBuilder(player, RedProtect.get().firstLocationSelections.get(player), RedProtect.get().secondLocationSelections.get(player), name, leader, addedAdmins, false);
                if (rb2.ready()) {
                    Region r2 = rb2.build();
                    RPLang.sendMessage(player,RPLang.get("cmdmanager.region.created") + " " + r2.getName() + ".");
                    RedProtect.get().rm.add(r2, player.getWorld());

                    RedProtect.get().firstLocationSelections.remove(player);
                    RedProtect.get().secondLocationSelections.remove(player);

                    RedProtect.get().logger.addLog("(World "+r2.getWorld()+") Player "+player.getName()+" CLAIMED region "+r2.getName());
                }
                return cmdr;
        	}

        	// - /rp copyflag from to
    		if  (checkCmd(args[0], "copyflag")){
    			if (!RedProtect.get().ph.hasGenPerm(player, "copyflag")) {
                    RPLang.sendMessage(player, "no.permission");
                    return cmdr;
                }
    			World w = player.getWorld();
    			Region from = RedProtect.get().rm.getRegion(args[1], w);
    			Region to = RedProtect.get().rm.getRegion(args[2], w);
    			if (from == null){
    				RPLang.sendMessage(player,RPLang.get("cmdmanager.region.doesntexist") + ": " + args[1]);
    				return cmdr;
    			}
    			if (to == null){
    				RPLang.sendMessage(player,RPLang.get("cmdmanager.region.doesntexist") + ": " + args[2]);
    				return cmdr;
    			}
    			for (String key:from.flags.keySet()){
        			to.setFlag(key, from.flags.get(key));
    			}
    			RPLang.sendMessage(player,RPLang.get("cmdmanager.region.flag.copied") + args[1] + " > " + args[2]);
    			RedProtect.get().logger.addLog("Player "+player.getName()+" Copied FLAGS from "+ args[1] + " to " + args[2]);
    			return cmdr;
    		}
        }

        if (args.length == 4 || args.length == 5){

			//rp createportal <newRegionName> <regionTo> <world>
			if (checkCmd(args[0], "createportal")){
				if (!RedProtect.get().ph.hasGenPerm(player, "createportal")) {
					RPLang.sendMessage(player, "no.permission");
					return cmdr;
				}

				Optional<World> w = RedProtect.get().serv.getWorld(args[3]);
				if (!w.isPresent()){
					RPLang.sendMessage(sender, "cmdmanager.region.invalidworld");
					return cmdr;
				}
				Region r = RedProtect.get().rm.getRegion(args[2], w.get());
				if (r == null){
					RPLang.sendMessage(player, RPLang.get("cmdmanager.createportal.warning").replace("{region}", args[2]));
				}

				String serverName = RedProtect.get().cfgs.getString("region-settings.default-leader");
				String name = args[1].replace("/", "|");

				RegionBuilder rb2 = new DefineRegionBuilder(player, RedProtect.get().firstLocationSelections.get(player), RedProtect.get().secondLocationSelections.get(player), name, serverName, new ArrayList<>(), true);
				if (rb2.ready()) {
					Region r2 = rb2.build();
					RPLang.sendMessage(player, String.format(RPLang.get("cmdmanager.region.portalcreated"), name, args[2], w.get().getName()));
					RPLang.sendMessage(player, "cmdmanager.region.portalhint");

					r2.setFlag("server-enter-command", "rp tp {player} "+args[2]+" "+w.get().getName());
					RedProtect.get().rm.add(r2, player.getWorld());

					RedProtect.get().firstLocationSelections.remove(player);
					RedProtect.get().secondLocationSelections.remove(player);

					RedProtect.get().logger.addLog("(World "+r2.getWorld()+") Player "+player.getName()+" CREATED A PORTAL "+r2.getName()+" to "+args[2]+" world "+w.get().getName());
				}
				return cmdr;
			}

        	//rp add-rent <player> <valor> <date>
        	if (checkCmd(args[0], "add-rent") && player.hasPermission("redprotect.add-rent")){
        		Region r = RedProtect.get().rm.getTopRegion(player.getLocation());
        		if (r == null){
        			RPLang.sendMessage(player, "cmdmanager.region.todo.that");
    				return cmdr;
        		}

        		if (!r.isLeader(player) && !player.hasPermission("redprotect.rent.others")){
        			sendNoPermissionMessage(player);
        			return cmdr;
        		}

        		int value = 0;
        		long renewal = 0;
        		try {
        			Calendar cal = Calendar.getInstance();
        			SimpleDateFormat sdf = new SimpleDateFormat(RedProtect.get().cfgs.getString("region-settings.date-format"));

        			cal.setTime(sdf.parse(args[3]));
        			renewal = cal.getTimeInMillis();
        			value = Integer.valueOf(args[2]);
        		} catch (Exception ex){
        			RPLang.sendMessage(player,RPLang.get("correct.command")+ " &b/rp "+getCmd("help"));
        			ex.printStackTrace();
        			return cmdr;
        		}

        		String play = RPUtil.PlayerToUUID(args[1]);
        		if (args.length == 4){
        			r.addrent(play, value, renewal, RedProtect.get().cfgs.getString("region-settings.rent.default-level"));
        		} else
        		if (args.length == 5){
        			if (args[4].equalsIgnoreCase("member") || args[4].equalsIgnoreCase("admin") || args[4].equalsIgnoreCase("leader")){
        				r.addrent(play, value, renewal, args[4]);
        			} else {
        				RPLang.sendMessage(player, RPLang.get("cmdmanager.rent.validranks").replace("{ranks}", "member, admin, leader"));
        				return cmdr;
        			}
        		}
        		RPLang.sendMessage(player, RPLang.get("cmdmanager.rent.addedrent").replace("{player}", args[1]).replace("{date}", args[3]).replace("{cost}", args[2]));
        		if (RedProtect.get().serv.getPlayer(args[1]).isPresent()){
        			RPLang.sendMessage(RedProtect.get().serv.getPlayer(args[1]).get(), RPLang.get("cmdmanager.rent.playeraddedrent").replace("{region}", r.getName()).replace("{date}", args[3]).replace("{cost}", args[2]));
        		}
        		return cmdr;
        	}
        }

        //rp expand-vert [region] [world]
        if (checkCmd(args[0], "expand-vert")){
        	if (!RedProtect.get().ph.hasGenPerm(player, "expandvert")) {
                RPLang.sendMessage(player, "no.permission");
                return cmdr;
            }
    		Region r = null;
    		//rp expand-vert
    		if (args.length == 1){
    			r = RedProtect.get().rm.getTopRegion(player.getLocation());
    			if (r == null){
        			RPLang.sendMessage(player, "cmdmanager.region.todo.that");
    				return cmdr;
        		}
    		} else
    		//rp expand-vert [region]
    		if (args.length == 2){
    			r = RedProtect.get().rm.getRegion(args[1], player.getWorld());
    			if (r == null){
        			RPLang.sendMessage(player, RPLang.get("cmdmanager.region.doesntexist") + ": " + args[1]);
    				return cmdr;
        		}
    		} else
    		//rp expand-vert [region] [world]
    		if (args.length == 3){
                if (!Sponge.getServer().getWorld(args[2]).isPresent()){
                	RPLang.sendMessage(player, "cmdmanager.region.invalidworld");
                	return cmdr;
    			}
    			r = RedProtect.get().rm.getRegion(args[1], Sponge.getServer().getWorld(args[2]).get());
    			if (r == null){
        			RPLang.sendMessage(player, RPLang.get("cmdmanager.region.doesntexist") + ": " + args[1]);
    				return cmdr;
        		}
    		} else {
    			RPLang.sendMessage(player, RPLang.get("cmdmanager.help.expandvert").replace("{cmd}", getCmd("expandvert")).replace("{alias}", getCmdAlias("expandvert")));
    			return cmdr;
    		}

    		r.setMaxY(256);
    		r.setMinY(0);
    		RPLang.sendMessage(player, RPLang.get("cmdmanager.region.expandvert.success").replace("{region}", r.getName()).replace("{miny}", String.valueOf(r.getMinY())).replace("{maxy}", String.valueOf(r.getMaxY())));
    		return cmdr;
    	}

        //rp setmaxy <size> [region] [world]
    	if (checkCmd(args[0], "setmaxy")){
    		if (!RedProtect.get().ph.hasGenPerm(player, "setmaxy")) {
                RPLang.sendMessage(player, "no.permission");
                return cmdr;
            }
    		Region r = null;
    		//rp setmaxy <size>
			switch (args.length) {
				case 2:
					r = RedProtect.get().rm.getTopRegion(player.getLocation());
					if (r == null) {
						RPLang.sendMessage(player, "cmdmanager.region.todo.that");
						return cmdr;
					}
					break;
				//rp setmaxy <size> [region]
				case 3:
					r = RedProtect.get().rm.getRegion(args[2], player.getWorld());
					if (r == null) {
						RPLang.sendMessage(player, RPLang.get("cmdmanager.region.doesntexist") + ": " + args[2]);
						return cmdr;
					}
					break;
				//rp setmaxy <size> [region] [world]
				case 4:
					if (!Sponge.getServer().getWorld(args[3]).isPresent()) {
						RPLang.sendMessage(player, "cmdmanager.region.invalidworld");
						return cmdr;
					}
					r = RedProtect.get().rm.getRegion(args[2], Sponge.getServer().getWorld(args[3]).get());
					if (r == null) {
						RPLang.sendMessage(player, RPLang.get("cmdmanager.region.doesntexist") + ": " + args[2]);
						return cmdr;
					}
					break;
				default:
					RPLang.sendMessage(player, RPLang.get("cmdmanager.help.setmaxy").replace("{cmd}", getCmd("setmaxy")).replace("{alias}", getCmdAlias("setmaxy")));
					return cmdr;
			}

    		String from = String.valueOf(r.getMaxY());

    		try{
    			int size = Integer.parseInt(args[1]);
    			if ((size - r.getMinY()) <= 1){
        			RPLang.sendMessage(player, "cmdmanager.region.ysiszesmatch");
        			return cmdr;
        		}
    			r.setMaxY(size);
    			RPLang.sendMessage(player, RPLang.get("cmdmanager.region.setmaxy.success").replace("{region}", r.getName()).replace("{fromsize}", from).replace("{size}", String.valueOf(size)));
    			RedProtect.get().logger.addLog("(World "+r.getWorld()+") Player "+player.getName()+" SETMAXY of region "+r.getName()+" to "+args[1]);
    			return cmdr;
    		} catch (NumberFormatException e){
    			RPLang.sendMessage(player, "cmdmanager.region.invalid.number");
    			return cmdr;
    		}
    	}

    	//rp setmaxy <size> [region] [world]
    	if (checkCmd(args[0], "setminy")){
    		if (!RedProtect.get().ph.hasGenPerm(player, "setminy")) {
                RPLang.sendMessage(player, "no.permission");
                return cmdr;
            }
    		Region r = null;
    		//rp setmaxy <size>
			switch (args.length) {
				case 2:
					r = RedProtect.get().rm.getTopRegion(player.getLocation());
					if (r == null) {
						RPLang.sendMessage(player, "cmdmanager.region.todo.that");
						return cmdr;
					}
					break;
				//rp setmaxy <size> [region]
				case 3:
					r = RedProtect.get().rm.getRegion(args[2], player.getWorld());
					if (r == null) {
						RPLang.sendMessage(player, RPLang.get("cmdmanager.region.doesntexist") + ": " + args[2]);
						return cmdr;
					}
					break;
				//rp setmaxy <size> [region] [world]
				case 4:
					if (!Sponge.getServer().getWorld(args[3]).isPresent()) {
						RPLang.sendMessage(player, "cmdmanager.region.invalidworld");
						return cmdr;
					}
					r = RedProtect.get().rm.getRegion(args[2], Sponge.getServer().getWorld(args[3]).get());
					if (r == null) {
						RPLang.sendMessage(player, RPLang.get("cmdmanager.region.doesntexist") + ": " + args[2]);
						return cmdr;
					}
					break;
				default:
					RPLang.sendMessage(player, RPLang.get("cmdmanager.help.setminy").replace("{cmd}", getCmd("setminy")).replace("{alias}", getCmdAlias("setminy")));
					return cmdr;
			}

    		String from = String.valueOf(r.getMinY());

    		try{
    			int size = Integer.parseInt(args[1]);
    			if ((r.getMaxY() - size) <= 1){
        			RPLang.sendMessage(player, "cmdmanager.region.ysiszesmatch");
        			return cmdr;
        		}
    			r.setMinY(size);
        		RPLang.sendMessage(player, RPLang.get("cmdmanager.region.setminy.success").replace("{region}", r.getName()).replace("{fromsize}", from).replace("{size}", String.valueOf(size)));
        		RedProtect.get().logger.addLog("(World "+r.getWorld()+") Player "+player.getName()+" SETMINY of region "+r.getName()+" to "+args[1]);
        		return cmdr;
    		} catch (NumberFormatException e){
    			RPLang.sendMessage(player, "cmdmanager.region.invalid.number");
    			return cmdr;
    		}
    	}


    	if (checkCmd(args[0], "buy") && RedProtect.get().ph.hasGenPerm(player, "redprotect.eco.buy")){

    		Region r = RedProtect.get().rm.getTopRegion(player.getLocation());
        	if (r == null){
    			RPLang.sendMessage(player, "cmdmanager.region.todo.that");
    			return cmdr;
    		}
        	if (!r.isForSale()){
    			RPLang.sendMessage(player, "economy.region.buy.notforsale");
    			return cmdr;
    		}

    		if (args.length == 1){
    			buyHandler(player, r.getValue(), r);
    			RedProtect.get().logger.addLog("(World "+r.getWorld()+") Player "+player.getName()+" BUY region "+r.getName()+" for "+r.getValue());
				return cmdr;
    		}
    	}


        if (checkCmd(args[0], "sell") && RedProtect.get().ph.hasGenPerm(player, "redprotect.eco.sell")){
        	Region r = RedProtect.get().rm.getTopRegion(player.getLocation());
        	if (r == null){
    			RPLang.sendMessage(player, "cmdmanager.region.todo.that");
    			return cmdr;
    		}
        	if (r.isForSale()){
    			RPLang.sendMessage(player, "economy.region.sell.already");
    			return cmdr;
    		}

        	if (args.length == 1){
        		r.setValue(RPEconomy.getRegionValue(r));
        		if (r.isLeader(player)){
        			sellHandler(r, player, RPUtil.PlayerToUUID(player.getName()), r.getValue());
        		} else {
        			sellHandler(r, player, r.getLeaders().get(0), r.getValue());
        		}
        		RedProtect.get().logger.addLog("(World "+r.getWorld()+") Player "+player.getName()+" SELL region "+r.getName()+" for "+r.getValue());
        		return cmdr;
        	}

        	if (args.length == 2){
        		// rp sell <value/player>
        		try {
        			long value = Long.valueOf(args[1]);
    				if (RedProtect.get().ph.hasGenPerm(player, "redprotect.eco.setvalue")){
    					r.setValue(value);
    					if (r.isLeader(player)){
    	        			sellHandler(r, player, RPUtil.PlayerToUUID(player.getName()), r.getValue());
    	        		} else {
    	        			sellHandler(r, player, r.getLeaders().get(0), r.getValue());
    	        		}
    					RedProtect.get().logger.addLog("(World "+r.getWorld()+") Player "+player.getName()+" SELL region "+r.getName()+" for "+r.getValue());
    					return cmdr;
    				}
    			} catch (NumberFormatException e){
    				if (RedProtect.get().ph.hasGenPerm(player, "redprotect.eco.others")){
    					r.setValue(RPEconomy.getRegionValue(r));
    					sellHandler(r, player, RPUtil.PlayerToUUID(args[1]), r.getValue());
    					RedProtect.get().logger.addLog("(World "+r.getWorld()+") Player "+player.getName()+" SELL region "+r.getName()+" in name of player "+args[1]+" for "+r.getValue());
            			return cmdr;
                	}
    			}
        	}

        	if (args.length == 3){
        		// rp sell player value
        		try {
        			long value = Long.valueOf(args[2]);
    				if (RedProtect.get().ph.hasGenPerm(player, "redprotect.eco.setvalue")){
    					r.setValue(value);
    					sellHandler(r, player, RPUtil.PlayerToUUID(args[1]), value);
    					RedProtect.get().logger.addLog("(World "+r.getWorld()+") Player "+player.getName()+" SELL region "+r.getName()+" in name of player "+args[1]+" for "+value);
    					return cmdr;
    				}
    			} catch (NumberFormatException e){
    				RPLang.sendMessage(player, "cmdmanager.eco.notdouble");
            		return cmdr;
    			}
        	}
        }

        if (checkCmd(args[0], "teleport")) {
        	if (args.length == 1) {
        		RPLang.sendMessage(player, RPLang.get("cmdmanager.help.teleport").replace("{cmd}", getCmd("teleport")).replace("{alias}", getCmdAlias("teleport")));
        		return cmdr;
        	}

            if (args.length == 2) {
            	handletp(player, args[1], player.getWorld().getName(), null);
            	return cmdr;
        	}

            if (args.length == 3) {
            	handletp(player, args[1], args[2], null);
            	return cmdr;
            }

            if (args.length == 4) {
            	// /rp tp <player> <region> <world>
            	Player play = null;
            	if (Sponge.getServer().getPlayer(args[1]).isPresent()){
            		play = Sponge.getServer().getPlayer(args[1]).get();
            	}

            	if (play != null){
            		handletp(player, args[2], args[3], play);
            		return cmdr;
            	} else {
            		RPLang.sendMessage(player, RPLang.get("cmdmanager.noplayer.thisname").replace("{player}", args[1]));
            		RPLang.sendMessage(player, RPLang.get("cmdmanager.help.teleport").replace("{cmd}", getCmd("teleport")).replace("{alias}", getCmdAlias("teleport")));
            		return cmdr;
            	}
            }
        }

        if (checkCmd(args[0], "limit")) {
            if (!RedProtect.get().ph.hasUserPerm(player, "limit")) {
                RPLang.sendMessage(player, "no.permission");
                return cmdr;
            }

            if (args.length == 1) {
            	int limit = RedProtect.get().ph.getPlayerBlockLimit(player);
                if (limit < 0 || RedProtect.get().ph.hasPerm(player, "redprotect.limit.blocks.unlimited")) {
                    RPLang.sendMessage(player,"cmdmanager.nolimit");
                    return cmdr;
                }
                String uuid = player.getUniqueId().toString();
                if (!RedProtect.get().OnlineMode){
                	uuid = player.getName().toLowerCase();
                }
                int currentUsed = RedProtect.get().rm.getTotalRegionSize(uuid, player.getPlayer().isPresent() ? player.getPlayer().get().getWorld().getName():null);
                RPLang.sendMessage(player,RPLang.get("cmdmanager.yourarea") + currentUsed + RPLang.get("general.color") + "/&e" + limit + RPLang.get("general.color"));
                return cmdr;
            }

            if (!RedProtect.get().ph.hasPerm(player, "redprotect.other.limit")) {
                RPLang.sendMessage(player, "no.permission");
                return cmdr;
            }

            if (args.length == 2) {
            	User offp = RPUtil.getUser(args[1]);

            	if (offp == null){
            		RPLang.sendMessage(player,RPLang.get("cmdmanager.noplayer.thisname").replace("{player}", args[1]));
            		return cmdr;
            	}
            	int limit = RedProtect.get().ph.getPlayerBlockLimit(offp);
                if (limit < 0 || RedProtect.get().ph.hasPerm(offp, "redprotect.limit.blocks.unlimited")) {
                    RPLang.sendMessage(player, "cmdmanager.nolimit");
                    return cmdr;
                }

                int currentUsed = RedProtect.get().rm.getTotalRegionSize(RPUtil.PlayerToUUID(offp.getName()),offp.getPlayer().isPresent() ? offp.getPlayer().get().getWorld().getName():null);
                RPLang.sendMessage(player,RPLang.get("cmdmanager.yourarea") + currentUsed + RPLang.get("general.color") + "/&e" + limit + RPLang.get("general.color"));
                return cmdr;
            }
            RPLang.sendMessage(player,RPLang.get("correct.usage") + " " + RPLang.get("cmdmanager.help.limit").replace("{cmd}", getCmd("limit")).replace("{alias}", getCmdAlias("limit")));
            return cmdr;
        }

        if (checkCmd(args[0], "claimlimit")) {
            if (!RedProtect.get().ph.hasUserPerm(player, "claimlimit")) {
                RPLang.sendMessage(player, "no.permission");
                return cmdr;
            }

            if (args.length == 1) {
            	int limit = RedProtect.get().ph.getPlayerClaimLimit(player);
                if (limit < 0 || RedProtect.get().ph.hasPerm(player, "redprotect.claimunlimited")) {
                    RPLang.sendMessage(player,"cmdmanager.nolimit");
                    return cmdr;
                }

                int currentUsed = RedProtect.get().rm.getRegions(RPUtil.PlayerToUUID(player.getName()), player.getWorld()).size();
                RPLang.sendMessage(player,RPLang.get("cmdmanager.yourclaims") + currentUsed + RPLang.get("general.color") + "/&e" + limit + RPLang.get("general.color"));
                return cmdr;
            }

            if (!RedProtect.get().ph.hasPerm(player, "redprotect.other.claimlimit")) {
                RPLang.sendMessage(player, "no.permission");
                return cmdr;
            }

            if (args.length == 2) {
            	User offp = RPUtil.getUser(args[1]);

            	if (offp == null){
            		RPLang.sendMessage(player,RPLang.get("cmdmanager.noplayer.thisname").replace("{player}", args[1]));
            		return cmdr;
            	}
            	int limit = RedProtect.get().ph.getPlayerClaimLimit(offp);
                if (limit < 0 || RedProtect.get().ph.hasPerm(offp, "redprotect.limit.claim.unlimited")) {
                    RPLang.sendMessage(player, "cmdmanager.nolimit");
                    return cmdr;
                }

                int currentUsed = RedProtect.get().rm.getRegions(RPUtil.PlayerToUUID(offp.getName()), player.getWorld()).size();
                RPLang.sendMessage(player,RPLang.get("cmdmanager.yourclaims") + currentUsed + RPLang.get("general.color") + "/&e" + limit + RPLang.get("general.color"));
                return cmdr;
            }
            RPLang.sendMessage(player,RPLang.get("correct.usage") + " " + RPLang.get("cmdmanager.help.claimlimit").replace("{cmd}", getCmd("claimlimit")).replace("{alias}", getCmdAlias("claimlimit")));
            return cmdr;
        }

        if (checkCmd(args[0], "welcome")) {
            if (args.length >= 2) {
            	String wMessage = "";
            	if (args[1].equals("off")){
            		handleWelcome(player, wMessage);
            		return cmdr;
            	} else {
            		for (int i = 1; i < args.length; i++){
                		wMessage = wMessage+args[i]+" ";
                	}
                	handleWelcome(player, wMessage);
                    return cmdr;
            	}
            }
            RPLang.sendMessage(player,RPLang.get("correct.usage") + " " + RPLang.get("cmdmanager.help.welcome").replace("{cmd}", getCmd("welcome")).replace("{alias}", getCmdAlias("welcome")));
            return cmdr;
        }

        if (checkCmd(args[0], "priority")) {
        	int prior = 0;

        	if (args.length == 2) {
        		try {
        			prior = Integer.parseInt(args[1]);
            	} catch (NumberFormatException e){
        			RPLang.sendMessage(player, "cmdmanager.region.notnumber");
        			return cmdr;
        		}
        		handlePriority(player, prior);
                return cmdr;
        	}

            if (args.length == 3) {
            	try {
        			prior = Integer.parseInt(args[2]);
            	} catch (NumberFormatException e){
        			RPLang.sendMessage(player, "cmdmanager.region.notnumber");
        			return cmdr;
        		}
        		handlePrioritySingle(player, prior, args[1]);
                return cmdr;
            }
            RPLang.sendMessage(player,RPLang.get("correct.usage") + " " + RPLang.get("cmdmanager.help.priority").replace("{cmd}", getCmd("priority")).replace("{alias}", getCmdAlias("priority")));
            return cmdr;
        }

        if (checkCmd(args[0], "delete")) {
        	//rp del [region] [world]
            if (args.length == 1) {
                handleDelete(player);
                return cmdr;
            }
            if (args.length == 2) {
                handleDeleteName(player, args[1], "");
                return cmdr;
            }
            if (args.length == 3) {
                handleDeleteName(player, args[1], args[2]);
                return cmdr;
            }
            RPLang.sendMessage(player,RPLang.get("correct.usage") + " " + RPLang.get("cmdmanager.help.delete").replace("{cmd}", getCmd("delete")).replace("{alias}", getCmdAlias("delete")));
            return cmdr;
        }

        if (checkCmd(args[0], "info")) {
        	//rp info [region] [world]
            if (args.length == 1) {
                handleInfoTop(player);
                return cmdr;
            }
            if (args.length == 2) {
                handleInfo(player, args[1], "");
                return cmdr;
            }
            if (args.length == 3) {
                handleInfo(player, args[1], args[2]);
                return cmdr;
            }
            RPLang.sendMessage(player,RPLang.get("correct.usage") + " " + RPLang.get("cmdmanager.help.info").replace("{cmd}", getCmd("info")).replace("{alias}", getCmdAlias("info")));
            return cmdr;
        }

        if (checkCmd(args[0], "addmember")) {
            if (args.length == 2) {
                handleAddMember(player, args[1], null);
                return cmdr;
            }
            RPLang.sendMessage(player,RPLang.get("correct.usage") + " " + RPLang.get("cmdmanager.help.addmember").replace("{cmd}", getCmd("addmember")).replace("{alias}", getCmdAlias("addmember")));
            return cmdr;
        }

        if (checkCmd(args[0], "addadmin")) {
            if (args.length == 2) {
                handleAddAdmin(player, args[1], null);
                return cmdr;
            }
            RPLang.sendMessage(player,RPLang.get("correct.usage") + " " + RPLang.get("cmdmanager.help.addadmin").replace("{cmd}", getCmd("addadmin")).replace("{alias}", getCmdAlias("addadmin")));
            return cmdr;
        }

        if (checkCmd(args[0], "addleader")) {
            if (args.length == 2) {
                handleAddLeader(player, args[1], null);
                return cmdr;
            }
            RPLang.sendMessage(player,RPLang.get("correct.usage") + " " + RPLang.get("cmdmanager.help.addleader").replace("{cmd}", getCmd("addleader")).replace("{alias}", getCmdAlias("addleader")));
            return cmdr;
        }

        if (checkCmd(args[0], "removemember")) {
            if (args.length == 2) {
                handleRemoveMember(player, args[1], null);
                return cmdr;
            }
            RPLang.sendMessage(player,RPLang.get("correct.usage") + " " + RPLang.get("cmdmanager.help.removemember").replace("{cmd}", getCmd("removemember")).replace("{alias}", getCmdAlias("removemember")));
            return cmdr;
        }

        if (checkCmd(args[0], "removeadmin")) {
            if (args.length == 2) {
                handleRemoveAdmin(player, args[1], null);
                return cmdr;
            }
            RPLang.sendMessage(player,RPLang.get("correct.usage") + " " + RPLang.get("cmdmanager.help.removeadmin").replace("{cmd}", getCmd("removeadmin")).replace("{alias}", getCmdAlias("removeadmin")));
            return cmdr;
        }

        if (checkCmd(args[0], "removeleader")) {
            if (args.length == 2) {
                handleRemoveLeader(player, args[1], null);
                return cmdr;
            }
            RPLang.sendMessage(player,RPLang.get("correct.usage") + " " + RPLang.get("cmdmanager.help.removeleader").replace("{cmd}", getCmd("removeleader")).replace("{alias}", getCmdAlias("removeleader")));
            return cmdr;
        }

        if (checkCmd(args[0], "rename")) {
            if (args.length == 2) {
                handleRename(player, args[1]);
                return cmdr;
            }
            RPLang.sendMessage(player,RPLang.get("correct.usage") + " " + RPLang.get("cmdmanager.help.rename").replace("{cmd}", getCmd("rename")).replace("{alias}", getCmdAlias("rename")));
            return cmdr;
        }

        if (checkCmd(args[0], "flag")) {
        	Region r = RedProtect.get().rm.getTopRegion(player.getLocation());
        	if (r == null){
        		RPLang.sendMessage(player, "cmdmanager.region.todo.that");
        		return cmdr;
        	}

            if (args.length == 2) {
            	if (RedProtect.get().cfgs.getBool("flags-configuration.change-flag-delay.enable")){
            		if (RedProtect.get().cfgs.getStringList("flags-configuration.change-flag-delay.flags").contains(args[1])){
            			if (!RedProtect.get().changeWait.contains(r.getName()+args[1])){
            				RPUtil.startFlagChanger(r.getName(), args[1], player);
            				handleFlag(player, args[1], "", r);
            				return cmdr;
            			} else {
            				RPLang.sendMessage(player,RPLang.get("gui.needwait.tochange").replace("{seconds}", RedProtect.get().cfgs.getString("flags-configuration.change-flag-delay.seconds")));
							return cmdr;
            			}
            		}
            	}
                handleFlag(player, args[1], "", r);
                return cmdr;
            }

            if (args.length >= 3) {
            	String text = "";
            	for (int i = 2; i < args.length; i++){
            		text = text + " " + args[i];
            	}
            	if (RedProtect.get().cfgs.getBool("flags-configuration.change-flag-delay.enable")){
            		if (RedProtect.get().cfgs.getStringList("flags-configuration.change-flag-delay.flags").contains(args[1])){
            			if (!RedProtect.get().changeWait.contains(r.getName()+args[1])){
            				RPUtil.startFlagChanger(r.getName(), args[1], player);
            				handleFlag(player, args[1], text.substring(1), r);
            				return cmdr;
            			} else {
            				RPLang.sendMessage(player,RPLang.get("gui.needwait.tochange").replace("{seconds}", RedProtect.get().cfgs.getString("flags-configuration.change-flag-delay.seconds")));
							return cmdr;
            			}
            		}
            	}
                handleFlag(player, args[1], text.substring(1), r);
                return cmdr;
            }

            RPLang.sendMessage(player,RPLang.get("correct.usage") + " " + RPLang.get("cmdmanager.help.flag").replace("{cmd}", getCmd("flag")).replace("{alias}", getCmdAlias("flag")));
            return cmdr;
        }

        if (checkCmd(args[0], "list")) {
        	//rp list
            if (args.length == 1) {
                handleList(player, RPUtil.PlayerToUUID(player.getName()), 1);
                return cmdr;
            }
            //rp list [player]
            if (args.length == 2) {
                handleList(player, RPUtil.PlayerToUUID(args[1]), 1);
                return cmdr;
            }
            //rp list [player] [page]
            if (args.length == 3) {
            	try{
                	int Page = Integer.parseInt(args[2]);
                    	handleList(player, RPUtil.PlayerToUUID(args[1]), Page);
                    	return cmdr;
                	} catch(NumberFormatException  e){
                        RPLang.sendMessage(player, "cmdmanager.region.listpage.error");
                        return cmdr;
                }
            }
        }
        RPLang.sendMessage(player,RPLang.get("correct.command") + " &e/rp "+getCmd("help"));
        return cmdr;
    }


	@SuppressWarnings("deprecation")
	private void buyHandler(Player player, long value, Region r) {

		if (r.isLeader(RPUtil.PlayerToUUID(player.getName()))){
			RPLang.sendMessage(player, "economy.region.buy.own");
			return;
		}

		UniqueAccount acc = RedProtect.get().econ.getOrCreateAccount(player.getUniqueId()).get();
		Double money = acc.getBalance(RedProtect.get().econ.getDefaultCurrency()).doubleValue();
		if (money >= value){
			String rname = r.getName();
			ArrayList<String> sellers = new ArrayList<>(r.getLeaders());
			if (RPEconomy.BuyRegion(r, RPUtil.PlayerToUUID(player.getName()))){
				acc.withdraw(RedProtect.get().econ.getDefaultCurrency(), BigDecimal.valueOf(value), RedProtect.get().getPVHelper().getCause(player));
				for (String seller : sellers){
					UserStorageService uss = Sponge.getGame().getServiceManager().provide(UserStorageService.class).get();
					Optional<User> offp = uss.get(RPUtil.UUIDtoPlayer(seller));
					if (!seller.equals(RedProtect.get().cfgs.getString("region-settings.default-leader")) && offp.isPresent()){
						UniqueAccount offAcc = RedProtect.get().econ.getOrCreateAccount(offp.get().getUniqueId()).get();
						offAcc.deposit(RedProtect.get().econ.getDefaultCurrency(), BigDecimal.valueOf(value), RedProtect.get().getPVHelper().getCause(player));
						if (offp.get().isOnline()){
							RPLang.sendMessage(offp.get().getPlayer().get(), RPLang.get("economy.region.buy.bought").replace("{player}", player.getName()).replace("{region}", rname).replace("{world}", r.getWorld()));
						}
					}
				}
				RPLang.sendMessage(player, RPLang.get("economy.region.buy.success").replace("{region}", r.getName()).replace("{value}", String.valueOf(value)).replace("{ecosymbol}", RedProtect.get().cfgs.getEcoString("economy-name")));
            } else {
				RPLang.sendMessage(player, "economy.region.error");
            }
		} else {
			RPLang.sendMessage(player, "economy.region.buy.nomoney");
        }
	}


	private void sellHandler(Region r, Player player, String leader, long value) {

		if (r.isLeader(player) || RedProtect.get().ph.hasGenPerm(player, "redprotect.eco.admin")){
			if (RPEconomy.putToSell(r, leader, value)){
				RPLang.sendMessage(player, "economy.region.sell.success");
			} else {
				RPLang.sendMessage(player, "economy.region.error");
			}
		} else {
			RPLang.sendMessage(player, "economy.region.sell.own");
		}
	}


    private static void handlePrioritySingle(Player p, int prior, String region) {
    	Region r = RedProtect.get().rm.getRegion(region, p.getWorld());
    	if (RedProtect.get().ph.hasRegionPermLeader(p, "priority", r)) {
    		if (r != null){
    			r.setPrior(prior);
    			RPLang.sendMessage(p,RPLang.get("cmdmanager.region.priority.set").replace("{region}", r.getName()) + " " + prior);
    			RedProtect.get().logger.addLog("(World "+r.getWorld()+") Player "+p.getName()+" SET PRIORITY of region "+r.getName()+" to "+prior);
    		} else {
    			RPLang.sendMessage(p, "cmdmanager.region.todo.that");
            }
    	}
	}

    private static void handlePriority(Player p, int prior) {
    	Region r = RedProtect.get().rm.getTopRegion(p.getLocation());
    	if (RedProtect.get().ph.hasRegionPermLeader(p, "priority", r)) {
    		if (r != null){
    			r.setPrior(prior);
    			RPLang.sendMessage(p,RPLang.get("cmdmanager.region.priority.set").replace("{region}", r.getName()) + " " + prior);
    			RedProtect.get().logger.addLog("(World "+r.getWorld()+") Player "+p.getName()+" SET PRIORITY of region "+r.getName()+" to "+prior);
    		} else {
    			RPLang.sendMessage(p, "cmdmanager.region.todo.that");
            }
    	}
	}

    private static void handleDelete(Player p) {
		Region r = RedProtect.get().rm.getTopRegion(p.getLocation());
        if (RedProtect.get().ph.hasRegionPermLeader(p, "delete", r)) {
            if (r == null) {
                sendNotInRegionMessage(p);
                return;
            }

            String puuid = RPUtil.PlayerToUUID(p.getName());
			if (r.isRentFor(puuid)){
				RPLang.sendMessage(p, "cmdmanager.rent.cantdothisrent");
				return;
			}

			int claims = RedProtect.get().cfgs.getInt("region-settings.first-home.can-delete-after-claims");
			if (!r.canDelete() && (claims == -1 || RedProtect.get().rm.getPlayerRegions(p.getName(), p.getWorld()) < claims) && !p.hasPermission("redprotect.bypass")){
				if (claims != -1){
					RPLang.sendMessage(p, RPLang.get("cmdmanager.region.cantdeletefirst-claims").replace("{claims}", ""+claims));
				} else {
					RPLang.sendMessage(p, RPLang.get("cmdmanager.region.cantdeletefirst"));
				}
				return;
			}

            DeleteRegionEvent event = new DeleteRegionEvent(r, p);
			if (Sponge.getEventManager().post(event)){
				return;
			}

            String rname = r.getName();
            String w = r.getWorld();
            RedProtect.get().rm.remove(r, RedProtect.get().serv.getWorld(w).get());
            RPLang.sendMessage(p,RPLang.get("cmdmanager.region.deleted") +" "+ rname);
            RedProtect.get().logger.addLog("(World "+w+") Player "+p.getName()+" REMOVED region "+rname);
        }
        else {
            sendNoPermissionMessage(p);
        }
    }

    private static void handleDeleteName(Player p, String rname, String world) {
		Region r = RedProtect.get().rm.getRegion(rname, p.getWorld());
		if (!world.equals("")){
			if (Sponge.getServer().getWorld(world).isPresent()){
				r = RedProtect.get().rm.getRegion(rname, Sponge.getServer().getWorld(world).get());
			} else {
				RPLang.sendMessage(p, "cmdmanager.region.invalidworld");
				return;
			}
		}

        if (RedProtect.get().ph.hasRegionPermLeader(p, "delete", r)) {
            if (r == null) {
            	RPLang.sendMessage(p, RPLang.get("cmdmanager.region.doesntexist") + ": " + rname);
                return;
            }

            String puuid = RPUtil.PlayerToUUID(p.getName());
			if (r.isRentFor(puuid)){
				RPLang.sendMessage(p, "cmdmanager.rent.cantdothisrent");
				return;
			}

			int claims = RedProtect.get().cfgs.getInt("region-settings.first-home.can-delete-after-claims");
			if (!r.canDelete() && (claims == -1 || RedProtect.get().rm.getPlayerRegions(p.getName(), p.getWorld()) < claims) && !p.hasPermission("redprotect.bypass")){
				if (claims != -1){
					RPLang.sendMessage(p, RPLang.get("cmdmanager.region.cantdeletefirst-claims").replace("{claims}", ""+claims));
				} else {
					RPLang.sendMessage(p, RPLang.get("cmdmanager.region.cantdeletefirst"));
				}
				return;
			}

            DeleteRegionEvent event = new DeleteRegionEvent(r, p);
			if (Sponge.getEventManager().post(event)){
				return;
			}

            RedProtect.get().rm.remove(r, RedProtect.get().serv.getWorld(r.getWorld()).get());
            RPLang.sendMessage(p,RPLang.get("cmdmanager.region.deleted") +" "+ rname);
            RedProtect.get().logger.addLog("(World "+world+") Player "+p.getName()+" REMOVED region "+rname);
        }
        else {
            sendNoPermissionMessage(p);
        }
    }

	private static void handleInfoTop(Player p) {
    	Region r = RedProtect.get().rm.getTopRegion(p.getLocation());
    	if (r == null) {
            sendNotInRegionMessage(p);
            return;
        }
    	Map<Integer, Region> groupr = RedProtect.get().rm.getGroupRegion(p.getLocation());
    	if (RedProtect.get().ph.hasRegionPermAdmin(p, "info", r) || r.isForSale()) {
            p.sendMessage(RPUtil.toText(RPLang.get("general.color") + "--------------- [&e" + r.getName() + RPLang.get("general.color") + "] ---------------"));
            p.sendMessage(r.info());
            p.sendMessage(RPUtil.toText(RPLang.get("general.color") + "----------------------------------"));
            if (groupr.size() > 1){
            	p.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.moreregions")));
                for (Region regs:groupr.values()){
                	if (regs != r){
                		p.sendMessage(RPUtil.toText(RPLang.get("region.name") + " " + regs.getName() + " " + RPLang.get("region.priority") + " " + regs.getPrior()));
                	}
                }
            }
        }
        else {
            sendNoPermissionMessage(p);
        }

    }

	private static void handleInfo(Player p, String region, String world) {
		Region r = RedProtect.get().rm.getRegion(region, p.getWorld());
		if (!world.equals("")){
			if (Sponge.getServer().getWorld(world).isPresent()){
				r = RedProtect.get().rm.getRegion(region, Sponge.getServer().getWorld(world).get());
			} else {
				RPLang.sendMessage(p, "cmdmanager.region.invalidworld");
				return;
			}
		}
    	if (RedProtect.get().ph.hasRegionPermAdmin(p, "info", r) || r.isForSale()) {
            if (r == null) {
                sendNotInRegionMessage(p);
                return;
            }
            p.sendMessage(RPUtil.toText(RPLang.get("general.color") + "--------------- [&e" + r.getName() + RPLang.get("general.color") + "] ---------------"));
            p.sendMessage(r.info());
            p.sendMessage(RPUtil.toText(RPLang.get("general.color") + "----------------------------------"));
        }
        else {
            sendNoPermissionMessage(p);
        }
    }

	private static void handleAddMember(CommandSource src, String sVictim, Region r) {
		if (src instanceof Player){
			Player p = (Player)src;
			r = RedProtect.get().rm.getTopRegion(p.getLocation());
			if (r == null) {
				sendNotInRegionMessage(p);
				return;
			}
		}

		if (RedProtect.get().ph.hasRegionPermAdmin(src, "addmember", r)) {
            if (r.isRentFor(RPUtil.PlayerToUUID(src.getName()))){
            	RPLang.sendMessage(src, "cmdmanager.rent.cantaddmore");
            	return;
            }

            String VictimUUID = RPUtil.PlayerToUUID(sVictim);
            if (RPUtil.UUIDtoPlayer(VictimUUID) == null){
            	RPLang.sendMessage(src,RPLang.get("cmdmanager.noplayer.thisname").replace("{player}", sVictim));
            	return;
            }

            Player pVictim = null;
            if (RedProtect.get().serv.getPlayer(sVictim).isPresent()){
            	pVictim = RedProtect.get().serv.getPlayer(sVictim).get();
            }

            if (r.isAdmin(VictimUUID)) {
                r.addMember(VictimUUID);
                RedProtect.get().logger.addLog("(World "+r.getWorld()+") Player "+ src.getName()+" ADDED MEMBER "+RPUtil.UUIDtoPlayer(VictimUUID)+" to region "+r.getName());
                RPLang.sendMessage(src,RPLang.get("general.color") + sVictim + " " + RPLang.get("cmdmanager.region.member.demoted") + " " + r.getName());
                if (pVictim != null && pVictim.isOnline()) {
                	RPLang.sendMessage(pVictim, RPLang.get("cmdmanager.region.admin.youdemoted").replace("{region}", r.getName()) + " " + src.getName());
                }
            } else if (!r.isMember(VictimUUID)) {
                r.addMember(VictimUUID);
                RedProtect.get().logger.addLog("(World "+r.getWorld()+") Player "+ src.getName()+" ADDED MEMBER "+RPUtil.UUIDtoPlayer(VictimUUID)+" to region "+r.getName());
                RPLang.sendMessage(src,RPLang.get("general.color") + sVictim + " " + RPLang.get("cmdmanager.region.member.added") + " " + r.getName());
                if (pVictim != null && pVictim.isOnline() && !pVictim.equals(src)) {
                    RPLang.sendMessage(pVictim, RPLang.get("cmdmanager.region.member.youadded").replace("{region}", r.getName()) + " " + src.getName());
                }
            } else {
                RPLang.sendMessage(src,"&c" + sVictim + " " + RPLang.get("cmdmanager.region.member.already"));
            }
        } else if (src instanceof Player){
            sendNoPermissionMessage((Player)src);
        }
    }


	private void handleAddLeader(CommandSource src, String sVictim, Region r) {
		if (src instanceof Player){
			Player p = (Player)src;
			r = RedProtect.get().rm.getTopRegion(p.getLocation());
			if (r == null) {
				sendNotInRegionMessage(p);
				return;
			}
		}

        if (RedProtect.get().ph.hasRegionPermLeader(src, "addleader", r)) {
            if (r.isRentFor(RPUtil.PlayerToUUID(src.getName()))){
            	RPLang.sendMessage(src, "cmdmanager.rent.cantaddmore");
            	return;
            }

            Player pVictim = null;
            if (RedProtect.get().serv.getPlayer(sVictim).isPresent()){
            	pVictim = RedProtect.get().serv.getPlayer(sVictim).get();
            }

            final String VictimUUID = RPUtil.PlayerToUUID(sVictim);
            if ((pVictim == null || !pVictim.isOnline()) && !src.hasPermission("redprotect.bypass.addleader")){
        		RPLang.sendMessage(src,RPLang.get("cmdmanager.noplayer.online").replace("{player}", sVictim));
            	return;
        	}

            if (!src.hasPermission("redprotect.bypass.addleader")){
            	int claimLimit = RedProtect.get().ph.getPlayerClaimLimit(pVictim);
                int claimused = RedProtect.get().rm.getPlayerRegions(pVictim.getName(),pVictim.getWorld());
                boolean claimUnlimited = RedProtect.get().ph.hasPerm(src, "redprotect.limit.claim.unlimited");
                if (claimused >= claimLimit && claimLimit >= 0 && !claimUnlimited){
                	RPLang.sendMessage(src, RPLang.get("cmdmanager.region.addleader.limit").replace("{player}", pVictim.getName()));
                	return;
                }
            }

            if (!r.isLeader(VictimUUID)) {
            	if (src.hasPermission("redprotect.bypass.addleader")){
            		r.addLeader(VictimUUID);
                    RedProtect.get().logger.addLog("(World "+r.getWorld()+") Player "+ src.getName()+" ADDED LEADER "+RPUtil.UUIDtoPlayer(VictimUUID)+" to region "+r.getName());
                    RPLang.sendMessage(src,RPLang.get("general.color") + sVictim + " " + RPLang.get("cmdmanager.region.leader.added") + " " + r.getName());
                    if (pVictim != null && pVictim.isOnline() && !pVictim.equals(src)) {
                        RPLang.sendMessage(pVictim, RPLang.get("cmdmanager.region.leader.youadded").replace("{region}", r.getName()) + " " + src.getName());
                    }
            		return;
            	}

                RPLang.sendMessage(src, RPLang.get("cmdmanager.region.leader.yousendrequest").replace("{player}", pVictim.getName()));
                RPLang.sendMessage(pVictim, RPLang.get("cmdmanager.region.leader.sendrequestto").replace("{region}", r.getName()).replace("{player}", src.getName()));

                RedProtect.get().alWait.put(pVictim, r.getID()+"@"+ src.getName());
                final Player pVictimf = pVictim;
                Sponge.getScheduler().createSyncExecutor(RedProtect.get().container).schedule(() -> {
                    if (RedProtect.get().alWait.containsKey(pVictimf)){
                    	RedProtect.get().alWait.remove(pVictimf);
                    	if (src instanceof Player && ((Player)src).isOnline()){
                    	    RPLang.sendMessage(src, RPLang.get("cmdmanager.region.leader.requestexpired").replace("{player}", pVictimf.getName()));
                    	}
                    }
                }, RedProtect.get().cfgs.getInt("region-settings.leadership-request-time"), TimeUnit.SECONDS);
            } else {
                RPLang.sendMessage(src,"&c" + sVictim + " " + RPLang.get("cmdmanager.region.leader.already"));
            }
        }
        else if (src instanceof Player){
            sendNoPermissionMessage((Player)src);
        }
	}


	private static void handleAddAdmin(CommandSource src, String sVictim, Region r) {
		if (src instanceof Player){
			Player p = (Player)src;
			r = RedProtect.get().rm.getTopRegion(p.getLocation());
			if (r == null) {
				sendNotInRegionMessage(p);
				return;
			}
		}

        if (RedProtect.get().ph.hasRegionPermAdmin(src, "addadmin", r)) {
            if (r.isRentFor(RPUtil.PlayerToUUID(src.getName()))){
            	RPLang.sendMessage(src, "cmdmanager.rent.cantaddmore");
            	return;
            }

            Player pVictim = null;
            if (RedProtect.get().serv.getPlayer(sVictim).isPresent()){
            	pVictim = RedProtect.get().serv.getPlayer(sVictim).get();
            }

            String VictimUUID = RPUtil.PlayerToUUID(sVictim);
            if (RPUtil.UUIDtoPlayer(VictimUUID) == null){
            	RPLang.sendMessage(src,RPLang.get("cmdmanager.noplayer.thisname").replace("{player}", sVictim));
            	return;
            }

            if (r.isLeader(VictimUUID)) {
            	RPLang.sendMessage(src,"&c" + sVictim + " " + RPLang.get("cmdmanager.region.leader.already"));
            	return;
            }

            if (!r.isAdmin(VictimUUID)) {
                r.addAdmin(VictimUUID);
                RedProtect.get().logger.addLog("(World "+r.getWorld()+") Player "+ src.getName()+" ADDED ADMIN "+RPUtil.UUIDtoPlayer(VictimUUID)+" to region "+r.getName());
                RPLang.sendMessage(src,RPLang.get("general.color") + sVictim + " " + RPLang.get("cmdmanager.region.admin.added") + " " + r.getName());
                if (pVictim != null && pVictim.isOnline() && !pVictim.equals(src)) {
                    RPLang.sendMessage(pVictim, RPLang.get("cmdmanager.region.admin.youadded").replace("{region}", r.getName()) + " " + src.getName());
                }
            }
            else {
                RPLang.sendMessage(src,"&c" + sVictim + " " + RPLang.get("cmdmanager.region.admin.already"));
            }
        }
        else if (src instanceof Player){
            sendNoPermissionMessage((Player)src);
        }
    }
	private static void handleRemoveMember(CommandSource src, String sVictim, Region r) {
		if (src instanceof Player){
			Player p = (Player)src;
			r = RedProtect.get().rm.getTopRegion(p.getLocation());
			if (r == null) {
				sendNotInRegionMessage(p);
				return;
			}
		}

        if (RedProtect.get().ph.hasRegionPermAdmin(src, "removemember", r)) {
            Player pVictim = null;
            if (RedProtect.get().serv.getPlayer(sVictim).isPresent()){
            	pVictim = RedProtect.get().serv.getPlayer(sVictim).get();
            }

            String VictimUUID = RPUtil.PlayerToUUID(sVictim);
            if (RPUtil.UUIDtoPlayer(VictimUUID) == null){
            	RPLang.sendMessage(src,RPLang.get("cmdmanager.noplayer.thisname").replace("{player}", sVictim));
            	return;
            }

            String victname = RPUtil.UUIDtoPlayer(VictimUUID);

            if (r.isRentFor(RPUtil.PlayerToUUID(src.getName())) || r.isRentFor(VictimUUID)){
            	RPLang.sendMessage(src, "cmdmanager.rent.cantchangerank");
            	return;
            }

            if ((r.isMember(VictimUUID) || r.isAdmin(VictimUUID)) && !r.isLeader(VictimUUID)) {
                RPLang.sendMessage(src,RPLang.get("general.color") + sVictim + " " + RPLang.get("cmdmanager.region.member.removed") + " " + r.getName());
                r.removeMember(VictimUUID);
                RedProtect.get().logger.addLog("(World "+r.getWorld()+") Player "+ src.getName()+" REMOVED MEMBER "+victname+" to region "+r.getName());
                if (pVictim != null && pVictim.isOnline() && !pVictim.equals(src)) {
                    RPLang.sendMessage(pVictim, RPLang.get("cmdmanager.region.member.youremoved").replace("{region}", r.getName()) + " " + src.getName());
                }
            } else {
                RPLang.sendMessage(src,"&c" + sVictim + " " + RPLang.get("cmdmanager.region.member.notmember"));
            }
        }
        else if (src instanceof Player){
            sendNoPermissionMessage((Player)src);
        }
    }

	private static void handleRemoveLeader(CommandSource src, String sVictim, Region r) {
		Region rLow = null;
		Map<Integer,Region> regions = new HashMap<>();
		if (src instanceof Player){
			Player p = (Player)src;
			r = RedProtect.get().rm.getTopRegion(p.getLocation());
			rLow = RedProtect.get().rm.getLowRegion(p.getLocation());
			regions = RedProtect.get().rm.getGroupRegion(p.getLocation());
			if (r == null) {
				sendNotInRegionMessage(p);
				return;
			}
		}

        if (RedProtect.get().ph.hasRegionPermLeader(src, "removeleader", r)) {
            Player pVictim = null;
            if (RedProtect.get().serv.getPlayer(sVictim).isPresent()){
            	pVictim = RedProtect.get().serv.getPlayer(sVictim).get();
            }

            String VictimUUID = RPUtil.PlayerToUUID(sVictim);
            if (RPUtil.UUIDtoPlayer(VictimUUID) == null){
            	RPLang.sendMessage(src,RPLang.get("cmdmanager.noplayer.thisname").replace("{player}", sVictim));
            	return;
            }

            String victname = RPUtil.UUIDtoPlayer(VictimUUID);
            if (r.isRentFor(RPUtil.PlayerToUUID(src.getName())) || r.isRentFor(VictimUUID)){
            	RPLang.sendMessage(src, "cmdmanager.rent.cantchangerank");
            	return;
            }

            if (rLow != null && rLow != r && ((!RedProtect.get().ph.hasRegionPermLeader(src, "removeleader", rLow) || (regions.size() > 1 && rLow.isLeader(VictimUUID))))){
        		RPLang.sendMessage(src,RPLang.get("cmdmanager.region.leader.cantremove.lowregion").replace("{player}", sVictim) + " " +rLow.getName());
            	return;
        	}
            if (r.isLeader(VictimUUID)) {
                if (r.leaderSize() > 1) {
                    RPLang.sendMessage(src,RPLang.get("general.color") + sVictim + " " + RPLang.get("cmdmanager.region.admin.added") + " " +r.getName());
                    r.removeLeader(VictimUUID);
                    RedProtect.get().logger.addLog("(World "+r.getWorld()+") Player "+ src.getName()+" DEMOTED TO ADMIN "+victname+" to region "+r.getName());
                    if (pVictim != null && pVictim.isOnline() && !pVictim.equals(src)) {
                        RPLang.sendMessage(pVictim, RPLang.get("cmdmanager.region.leader.youdemoted").replace("{region}", r.getName())+ " " + src.getName());
                    }
                } else {
                    RPLang.sendMessage(src,RPLang.get("cmdmanager.region.leader.cantremove").replace("{player}", sVictim));
                }
            }
            else {
                RPLang.sendMessage(src,"&c" + sVictim + " " + RPLang.get("cmdmanager.region.leader.notleader"));
            }
        }
        else if (src instanceof Player){
            sendNoPermissionMessage((Player)src);
        }
    }

	private static void handleRemoveAdmin(CommandSource src, String sVictim, Region r) {
		if (src instanceof Player){
			Player p = (Player)src;
			r = RedProtect.get().rm.getTopRegion(p.getLocation());
			if (r == null) {
				sendNotInRegionMessage(p);
				return;
			}
		}

		if (RedProtect.get().ph.hasRegionPermAdmin(src, "removeadmin", r)) {
            Player pVictim = null;
            if (RedProtect.get().serv.getPlayer(sVictim).isPresent()){
            	pVictim = RedProtect.get().serv.getPlayer(sVictim).get();
            }

            String VictimUUID = RPUtil.PlayerToUUID(sVictim);
            if (RPUtil.UUIDtoPlayer(VictimUUID) == null){
            	RPLang.sendMessage(src,RPLang.get("cmdmanager.noplayer.thisname").replace("{player}", sVictim));
            	return;
            }

            String victname = RPUtil.UUIDtoPlayer(VictimUUID);
            if (r.isRentFor(RPUtil.PlayerToUUID(src.getName())) || r.isRentFor(VictimUUID)){
            	RPLang.sendMessage(src, "cmdmanager.rent.cantchangerank");
            	return;
            }

            if (r.isAdmin(VictimUUID)) {
            	RPLang.sendMessage(src,RPLang.get("general.color") + sVictim + " " + RPLang.get("cmdmanager.region.member.added") + " " +r.getName());
                r.removeAdmin(VictimUUID);
                RedProtect.get().logger.addLog("(World "+r.getWorld()+") Player "+ src.getName()+" DEMOTED TO MEMBER "+victname+" to region "+r.getName());
                if (pVictim != null && pVictim.isOnline() && !pVictim.equals(src)) {
                    RPLang.sendMessage(pVictim, RPLang.get("cmdmanager.region.admin.youdemoted").replace("{region}", r.getName())+ " " + src.getName());
                }
            }
            else {
                RPLang.sendMessage(src,"&c" + sVictim + " " + RPLang.get("cmdmanager.region.admin.notadmin"));
            }
        }
        else if (src instanceof Player){
            sendNoPermissionMessage((Player)src);
        }
    }

	private static void handleRename(Player p, String newName) {
    	Region r = RedProtect.get().rm.getTopRegion(p.getLocation());
        if (RedProtect.get().ph.hasRegionPermLeader(p, "rename", r)) {
            if (r == null) {
                sendNotInRegionMessage(p);
                return;
            }

            //region name conform
            newName = newName.replace("/", "|");
            if (RedProtect.get().rm.getRegion(newName, p.getWorld()) != null) {
                RPLang.sendMessage(p, "cmdmanager.region.rename.already");
                return;
            }
            if (newName.length() < 2 || newName.length() > 16) {
                RPLang.sendMessage(p, "cmdmanager.region.rename.invalid");
                return;
            }
            if (newName.contains(" ")) {
                RPLang.sendMessage(p, "cmdmanager.region.rename.spaces");
                return;
            }
            if (newName.contains("@")) {
                p.sendMessage(RPUtil.toText(RPLang.get("regionbuilder.regionname.invalid.charac").replace("{charac}", "@")));
                return;
            }

            RenameRegionEvent event = new RenameRegionEvent(r, newName, r.getName(), p);
			if (Sponge.getEventManager().post(event)){
				return;
			}

			String oldname = event.getOldName();
			newName = event.getNewName();

            RedProtect.get().rm.renameRegion(newName, r);
            RPLang.sendMessage(p,RPLang.get("cmdmanager.region.rename.newname") + " " + newName);
            RedProtect.get().logger.addLog("(World "+r.getWorld()+") Player "+p.getName()+" RENAMED region "+oldname+" to "+newName);
        }
        else {
            RPLang.sendMessage(p, "no.permission");
        }
    }


    // TODO Flag Handler
	private static void handleFlag(Player p, String flag, String value, Region r) {
    	if (flag.equalsIgnoreCase("?")){
    		sendFlagHelp(p);
    		return;
    	}

    	if (r == null) {
            sendNotInRegionMessage(p);
            return;
        }

    	Object objflag = RPUtil.parseObject(value);

    	if (RedProtect.get().ph.hasFlagPerm(p, "redprotect.flag."+ flag) || flag.equalsIgnoreCase("info")) {
            if (r.isAdmin(p) || r.isLeader(p) || RedProtect.get().ph.hasAdminFlagPerm(p, "redprotect.admin.flag."+flag)) {

            	if (flag.equalsIgnoreCase("info") || flag.equalsIgnoreCase("i")) {
                    p.sendMessage(RPUtil.toText(RPLang.get("general.color") + "------------[" + RPLang.get("cmdmanager.region.flag.values") + "]------------"));
                    p.sendMessage(r.getFlagInfo());
                    p.sendMessage(RPUtil.toText(RPLang.get("general.color") + "------------------------------------"));
                    return;
                }

            	if (value.equalsIgnoreCase("remove")){
            		if (RedProtect.get().cfgs.AdminFlags.contains(flag) && r.flags.containsKey(flag)){
            			r.removeFlag(flag);
                        RPLang.sendMessage(p,RPLang.get("cmdmanager.region.flag.removed").replace("{flag}", flag).replace("{region}", r.getName()));
                        RedProtect.get().logger.addLog("(World "+r.getWorld()+") Player "+p.getName()+" REMOVED FLAG "+flag+" of region "+r.getName());
            			return;
            		} else {
                        RPLang.sendMessage(p,RPLang.get("cmdmanager.region.flag.notset").replace("{flag}", flag));
                        return;
            		}
            	}

            	if (r.flagExists("for-sale") && flag.equalsIgnoreCase("for-sale")){
            		RPLang.sendMessage(p, "cmdmanager.eco.changeflag");
            		return;
            	}
            	/*
            	if (RPConfig.getDefFlagsValues().containsKey("clan") && !RedProtect.get().ph.hasPerm(p, "RedProtect.get().admin.flag.clan")){
            		RPLang.sendMessage(p,"cmdmanager.region.flag.clancommand");
            		return;
            	}
            	*/
            	if (!value.equals("")){
            		if (RedProtect.get().cfgs.getDefFlagsValues().containsKey(flag)) {
            			/*
            			//flag clan
            			if (flag.equalsIgnoreCase("clan")){
            				if (!RedProtect.get().SC || !RedProtect.get().ph.hasGenPerm(p, "RedProtect.get().admin.flag.clan")){
            					sendFlagHelp(p);
                            	return;
            				}
            				if (!RedProtect.get().clanManager.isClan(value)){
            					RPLang.sendMessage(p, RPLang.get("cmdmanager.region.flag.invalidclan").replace("{tag}", value));
                        		return;
            				}
            				Clan clan = RedProtect.get().clanManager.getClan(value);
            				if (!clan.isLeader(p)){
            					RPLang.sendMessage(p,"cmdmanager.region.flag.clancommand");
                        		return;
            				}
            				r.setFlag(flag, value);
                            RPLang.sendMessage(p,RPLang.get("cmdmanager.region.flag.set").replace("{flag}", "'"+flag+"'") + " " + r.getFlagString(flag));
                            RedProtect.get().logger.addLog("(World "+r.getWorld()+") Player "+p.getName()+" SET FLAG "+flag+" of region "+r.getName()+" to "+r.getFlagString(flag));
                            return;
                    	}
            			*/
            			if (objflag instanceof Boolean){
            				r.setFlag(flag, objflag);
                            RPLang.sendMessage(p,RPLang.get("cmdmanager.region.flag.set").replace("{flag}", "'"+flag+"'") + " " + r.getFlagBool(flag));
                            RedProtect.get().logger.addLog("(World "+r.getWorld()+") Player "+p.getName()+" SET FLAG "+flag+" of region "+r.getName()+" to "+r.getFlagString(flag));
                            return;
            			} else {
            				RPLang.sendMessage(p,RPLang.get("cmdmanager.region.flag.usage") + " <true/false>");
            				return;
            			}
                	}

                	if (RedProtect.get().cfgs.AdminFlags.contains(flag)) {
                		if (!validate(flag, objflag)){
                			SendFlagUsageMessage(p, flag);
                			return;
                		}
                		r.setFlag(flag, objflag);
                		RPLang.sendMessage(p,RPLang.get("cmdmanager.region.flag.set").replace("{flag}", "'"+flag+"'") + " " + r.getFlagString(flag));
            			RedProtect.get().logger.addLog("(World "+r.getWorld()+") Player "+p.getName()+" SET FLAG "+flag+" of region "+r.getName()+" to "+r.getFlagString(flag));
                        return;
                	}


                	if (RedProtect.get().cfgs.AdminFlags.contains(flag)){
                		SendFlagUsageMessage(p, flag);
            		} else {
                    	RPLang.sendMessage(p,RPLang.get("cmdmanager.region.flag.usage") + " <true/false>");
            		}
                	sendFlagHelp(p);

                } else {
            		/*
            		//flag clan
        			if (flag.equalsIgnoreCase("clan")){
        				if (RedProtect.get().SC){
        					ClanPlayer clan = RedProtect.get().clanManager.getClanPlayer(p);
        					if (clan == null){
            					RPLang.sendMessage(p, "cmdmanager.region.flag.haveclan");
                        		return;
            				}
            				if (!clan.isLeader()){
            					RPLang.sendMessage(p,"cmdmanager.region.flag.clancommand");
                        		return;
            				}
            				if (r.getFlagString(flag).equalsIgnoreCase("")){
            					r.setFlag(flag, clan.getTag());
            					RPLang.sendMessage(p,RPLang.get("cmdmanager.region.flag.setclan").replace("{clan}", "'"+clan.getClan().getColorTag()+"'"));
            				} else {
            					RPLang.sendMessage(p,RPLang.get("cmdmanager.region.flag.denyclan").replace("{clan}", "'"+r.getFlagString(flag)+"'"));
            					r.setFlag(flag, "");
            				}
                            RedProtect.get().logger.addLog("(World "+r.getWorld()+") Player "+p.getName()+" SET FLAG "+flag+" of region "+r.getName()+" to "+r.getFlagString(flag));
                            return;
        				} else {
        					sendFlagHelp(p);
                        	return;
        				}
                	}
        			*/
            		if (RedProtect.get().cfgs.getDefFlagsValues().containsKey(flag)) {
            			r.setFlag(flag, !r.getFlagBool(flag));
                        RPLang.sendMessage(p,RPLang.get("cmdmanager.region.flag.set").replace("{flag}", "'"+flag+"'") + " " + r.getFlagBool(flag));
            			RedProtect.get().logger.addLog("(World "+r.getWorld()+") Player "+p.getName()+" SET FLAG "+flag+" of region "+r.getName()+" to "+r.getFlagString(flag));
                    } else {
            			if (RedProtect.get().cfgs.AdminFlags.contains(flag)){
            				SendFlagUsageMessage(p, flag);
                		} else {
                        	RPLang.sendMessage(p,RPLang.get("cmdmanager.region.flag.usage") + " <true/false>");
                		}
                    	sendFlagHelp(p);
            		}
            	}

            } else {
                RPLang.sendMessage(p,"cmdmanager.region.flag.nopermregion");
            }
        } else {
        	RPLang.sendMessage(p, "cmdmanager.region.flag.noperm");
        }
    }

	private static void SendFlagUsageMessage(Player p, String flag) {
		String message = "";
		if (flag.equalsIgnoreCase("effects") ||
				flag.equalsIgnoreCase("view-distance") ||
				flag.equalsIgnoreCase("allow-enter-items") ||
				flag.equalsIgnoreCase("deny-enter-items") ||
				flag.equalsIgnoreCase("gamemode") ||
				flag.equalsIgnoreCase("view-distance") ||
				flag.equalsIgnoreCase("allow-cmds") ||
				flag.equalsIgnoreCase("deny-cmds") ||
				flag.equalsIgnoreCase("allow-break") ||
				flag.equalsIgnoreCase("allow-place") ||
				flag.equalsIgnoreCase("cmd-onhealth")){
			message = RPLang.get("cmdmanager.region.flag.usage"+flag);
		} else {
			message = RPLang.get("cmdmanager.region.flag.usagetruefalse").replace("{flag}", flag);
		}
		p.sendMessage(RPUtil.toText(message.replace("{cmd}", getCmd("flag"))));
	}

	private static void sendFlagHelp(Player p) {
		p.sendMessage(RPUtil.toText(RPLang.get("general.color") + "-------------[redprotect Flags]------------"));
    	p.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.region.flag.list") + " " + RedProtect.get().cfgs.getDefFlags()));
    	p.sendMessage(RPUtil.toText(RPLang.get("general.color") + "------------------------------------"));
    	if (RedProtect.get().ph.hasAdminFlagPerm(p, "")){
        	p.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.region.flag.admlist") + " " + RedProtect.get().cfgs.AdminFlags));
        	p.sendMessage(RPUtil.toText(RPLang.get("general.color") + "------------------------------------"));
    	}
	}

	private static boolean validate(String flag, Object value) {
		if ((flag.equalsIgnoreCase("forcefly") ||
				flag.equalsIgnoreCase("can-death") ||
				flag.equalsIgnoreCase("can-pickup") ||
				flag.equalsIgnoreCase("can-drop") ||
				flag.equalsIgnoreCase("keep-inventory") ||
				flag.equalsIgnoreCase("keep-levels") ||
				flag.equalsIgnoreCase("allow-fly") ||
				flag.equalsIgnoreCase("door") ||
				flag.equalsIgnoreCase("button") ||
				flag.equalsIgnoreCase("lever") ||
				flag.equalsIgnoreCase("pvp") ||
				flag.equalsIgnoreCase("player-damage") ||
				flag.equalsIgnoreCase("can-hunger") ||
				flag.equalsIgnoreCase("can-projectiles") ||
				flag.equalsIgnoreCase("can-pet") ||
				flag.equalsIgnoreCase("portal-enter") ||
				flag.equalsIgnoreCase("allow-create-portal") ||
				flag.equalsIgnoreCase("allow-mod") ||
				flag.equalsIgnoreCase("portal-exit") ||
				flag.equalsIgnoreCase("enderpearl") ||
				flag.equalsIgnoreCase("can-back") ||
				flag.equalsIgnoreCase("up-skills") ||
				flag.equalsIgnoreCase("enter") ||
				flag.equalsIgnoreCase("treefarm") ||
				flag.equalsIgnoreCase("sign") ||
				flag.equalsIgnoreCase("invincible") ||
				flag.equalsIgnoreCase("flow-damage") ||
				flag.equalsIgnoreCase("mob-loot") ||
				flag.equalsIgnoreCase("allow-potions") ||
				flag.equalsIgnoreCase("smart-door") ||
				flag.equalsIgnoreCase("allow-magiccarpet") ||
				flag.equalsIgnoreCase("allow-home") ||
				flag.equalsIgnoreCase("spawn-monsters") ||
				flag.equalsIgnoreCase("spawn-animals") ||
				flag.equalsIgnoreCase("minecart") ||
				flag.equalsIgnoreCase("forcepvp") ||
				flag.equalsIgnoreCase("minefarm")) && !(value instanceof Boolean)){
			return false;
		}

		if (flag.equalsIgnoreCase("set-portal")){
			if (!(value instanceof String)){
				return false;
			}
			String[] valida = ((String)value).split(" ");
			if (valida.length != 2){
				return false;
			}
			if (!Sponge.getServer().getWorld(valida[1]).isPresent()){
				return false;
			}
			Region r = RedProtect.get().rm.getRegion(valida[0], valida[1]);
			if (r == null){
				return false;
			}
		}

		if (flag.equalsIgnoreCase("gamemode")){
			if (!(value instanceof String)){
				return false;
			}
			if (!RPUtil.testRegistry(GameMode.class, ((String)value))){
				return false;
			}
		}

		if (flag.equalsIgnoreCase("max-players")){
			try {
				Integer.parseInt(value.toString());
			} catch (NumberFormatException e){
				return false;
			}
		}

		if (flag.equalsIgnoreCase("allow-enter-items") || flag.equalsIgnoreCase("deny-enter-items")){
			if (!(value instanceof String)){
				return false;
			}
			String[] valida = ((String)value).replace(" ", "").split(",");
			for (String item:valida){
				if (!RPUtil.testRegistry(ItemType.class, item)){
					return false;
				}
			}
		}

		if (flag.equalsIgnoreCase("allow-place") || flag.equalsIgnoreCase("allow-break")){
			if (!(value instanceof String)){
				return false;
			}
			String[] valida = ((String)value).replace(" ", "").split(",");
			for (String item:valida){
				if (!RPUtil.testRegistry(EntityType.class, item) && !RPUtil.testRegistry(ItemType.class, item)){
					return false;
				}
			}
		}

		if (flag.equalsIgnoreCase("cmd-onhealth")){
			if (!(value instanceof String)){
				return false;
			}
			try{
				String[] args = ((String)value).split(",");
				for (String arg:args){
					if (!arg.split(" ")[0].startsWith("health:") || !arg.split(" ")[1].startsWith("cmd:")){
						return false;
					}
					//test health
					int health = Integer.valueOf(arg.split(" ")[0].substring(7));
					if (health < 0 || health > 20){
						return false;
					}
				}
			} catch(Exception ex){
				return false;
			}
		}

		if (flag.equalsIgnoreCase("allow-cmds") || flag.equalsIgnoreCase("deny-cmds")){
			if (!(value instanceof String)){
				return false;
			}
			try{
				String[] cmds = ((String)value).split(",");
				for (String cmd:cmds){
					if (cmds.length > 0 && (cmd.contains("cmd:") || cmd.contains("arg:"))){
						String[] cmdargs = cmd.split(" ");
						for (String cmd1:cmdargs){
							if (cmd1.startsWith("cmd:")){
								if (cmd1.split(":")[1].length() == 0){
									return false;
								}
							}
                            if (cmd1.startsWith("arg:")){
								if (cmd1.split(":")[1].length() == 0){
									return false;
								}
							}
						}
					} else {
						return false;
					}
				}
			} catch (Exception e){
				return false;
			}
		}

		if (flag.equalsIgnoreCase("effects")){
			if (!(value instanceof String)){
				return false;
			}
			String[] effects = ((String)value).split(",");
			for (String eff:effects){
				String[] effect = eff.split(" ");
				if (effect.length < 2){
					return false;
				}
				if (!RPUtil.testRegistry(PotionEffectType.class, effect[0])){
					return false;
				}
				try {
					Integer.parseInt(effect[1]);
				} catch (NumberFormatException e){
					return false;
				}
			}
		}
		return true;
	}

	private static void handleList(Player p, String uuid, int Page) {
		String pname = RPUtil.PlayerToUUID(p.getName());
        if (RedProtect.get().ph.hasGenPerm(p, "list")) {
        	getRegionforList(p, uuid, Page);
        	return;
        } else if (RedProtect.get().ph.hasUserPerm(p, "list") && pname.equalsIgnoreCase(uuid)){
        	getRegionforList(p, uuid, Page);
        	return;
        }
        RPLang.sendMessage(p, "no.permission");
    }

	private static void getRegionforList(CommandSource p, String uuid, int Page){
    	Set<Region> regions = RedProtect.get().rm.getRegions(uuid);
    	String pname = RPUtil.UUIDtoPlayer(uuid);
        int length = regions.size();
        if (pname == null || length == 0) {
            RPLang.sendMessage(p, "cmdmanager.player.noregions");
		}
        else {
        	p.sendMessage(RPUtil.toText(RPLang.get("general.color") + "-------------------------------------------------"));
        	RPLang.sendMessage(p,RPLang.get("cmdmanager.region.created.list") + " " +pname);
        	p.sendMessage(RPUtil.toText("-----"));
        	if (RedProtect.get().cfgs.getBool("region-settings.region-list.simple-listing")){
        		for (World w:Sponge.getServer().getWorlds()){
        			String colorChar = RedProtect.get().cfgs.getString("region-settings.world-colors." + w.getName());
        			Set<Region> wregions = RedProtect.get().rm.getRegions(uuid, w);
        			if (wregions.size() > 0){
        				Iterator<Region> it = wregions.iterator();
        				Builder worldregions = Text.builder();

        				if (RedProtect.get().ph.hasRegionPermAdmin(p, "teleport", null)){
        					boolean first = true;
                			while (it.hasNext()){
                				Region r = it.next();
                				if (first){
                					first = false;
                					worldregions.append(Text.builder()
            								.append(RPUtil.toText("&8"+r.getName()))
                            				.onHover(TextActions.showText(RPUtil.toText(RPLang.get("cmdmanager.list.hover").replace("{region}", r.getName()))))
                            				.onClick(TextActions.runCommand("/rp "+getCmd("teleport")+" "+r.getName()+" "+r.getWorld())).build());
                				} else {
                					worldregions.append(Text.builder()
            								.append(RPUtil.toText(RPLang.get("general.color")+", &8"+r.getName()))
                            				.onHover(TextActions.showText(RPUtil.toText(RPLang.get("cmdmanager.list.hover").replace("{region}", r.getName()))))
                            				.onClick(TextActions.runCommand("/rp "+getCmd("teleport")+" "+r.getName()+" "+r.getWorld())).build());
                				}
                			}
        				} else {
        					boolean first = true;
                			while (it.hasNext()){
                				Region r = it.next();
                				if (first){
                					first = false;
                					worldregions.append(Text.builder()
            								.append(RPUtil.toText("&8"+r.getName())).build());
                				} else {
                					worldregions.append(Text.builder()
            								.append(RPUtil.toText(RPLang.get("general.color")+", &8"+r.getName())).build());
                				}
                			}
        				}
        				p.sendMessage(RPUtil.toText(RPLang.get("general.color")+RPLang.get("region.world").replace(":", "")+" "+colorChar+w.getName()+"["+wregions.size()+"]&r: "));
            			p.sendMessages(worldregions.build());
            			p.sendMessage(RPUtil.toText("-----"));
        			}
        		}
        	} else {
                Iterator<Region> i = regions.iterator();
                if (Page == 0){Page = 1;}
                int max = (10*Page);
                int min = max-10;
                int count = 0;
                int last = 0;
                while (i.hasNext()) {
                	String info = i.next().info().toPlain();
                	if (count >= min && count <= max){
                		p.sendMessage(RPUtil.toText(RPLang.get("general.color") + "-------------------------------------------------"));
                        p.sendMessage(RPUtil.toText(RPLang.get("general.color") + "["+(count+1)+"] " + info));
                        last = count;

                	}
                	count++;
                }
                if (max > count){min = 0;}
            	p.sendMessage(RPUtil.toText(RPLang.get("general.color") + "------------- "+(min+1)+"-"+(last+1)+"/"+count+" --------------"));
            	if (count > max){
                	p.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.region.listpage.more").replace("{player}", pname + " " + (Page+1))));
                } else {
                	if (Page != 1) {p.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.region.listpage.nomore")));}
                }
        	}
        }
    }

	private static void handleWelcome(Player p, String wMessage) {
    	Region r = RedProtect.get().rm.getTopRegion(p.getLocation());
    	if (RedProtect.get().ph.hasRegionPermAdmin(p, "welcome", r)) {
        	if (r != null){
				switch (wMessage) {
					case "":
						r.setWelcome("");
						RPLang.sendMessage(p, "cmdmanager.region.welcomeoff");
						break;
					case "hide ":
						r.setWelcome(wMessage);
						RPLang.sendMessage(p, "cmdmanager.region.welcomehide");
						break;
					default:
						r.setWelcome(wMessage);
						RPLang.sendMessage(p, RPLang.get("cmdmanager.region.welcomeset") + " " + wMessage);
						break;
				}
        		RedProtect.get().logger.addLog("(World "+r.getWorld()+") Player "+p.getName()+" SET WELCOME of region "+r.getName()+" to "+wMessage);
        		return;
        	} else {
        		RPLang.sendMessage(p, "cmdmanager.region.todo.that");
        		return;
        	}
        }
        RPLang.sendMessage(p, "no.permission");
    }

	private static void handletp(Player p, String rname, String wname, Player play){

		World w = null;
		if (RedProtect.get().serv.getWorld(wname).isPresent()){
			w = RedProtect.get().serv.getWorld(wname).get();
		}

		if (w == null) {
            RPLang.sendMessage(p, "cmdmanager.region.invalidworld");
            return;
        }
    	Region region = RedProtect.get().rm.getRegion(rname, w);
    	if (region == null) {
    		RPLang.sendMessage(p, RPLang.get("cmdmanager.region.doesntexist") + ": " + rname);
            return;
        }

    	if (play == null) {
    		if (!RedProtect.get().ph.hasRegionPermMember(p, "teleport", region)){
    			RPLang.sendMessage(p, "no.permission");
                return;
    		}
    	} else {
    		if (!RedProtect.get().ph.hasPerm(p, "redprotect.teleport.other")) {
        		RPLang.sendMessage(p, "no.permission");
                return;
            }
        }

    	Location<World> loc = null;
    	if (region.getTPPoint() != null){
    		loc = new Location<>(w, region.getTPPoint().getBlockX() + 0.500, region.getTPPoint().getBlockY(), region.getTPPoint().getBlockZ() + 0.500);
    	} else {
    		int limit = w.getBlockMax().getY();
        	if (w.getDimension().equals(DimensionTypes.NETHER)){
        		limit = 124;
        	}
        	for (int i = limit; i > 0; i--){
        		BlockType mat = w.createSnapshot(region.getCenterX(), i, region.getCenterZ()).getState().getType();
        		BlockType mat1 = w.createSnapshot(region.getCenterX(), i+1, region.getCenterZ()).getState().getType();
        		BlockType mat2 = w.createSnapshot(region.getCenterX(), i+2, region.getCenterZ()).getState().getType();
        		if (!mat.equals(BlockTypes.LAVA) && !mat.equals(BlockTypes.AIR) && mat1.equals(BlockTypes.AIR) && mat2.equals(BlockTypes.AIR)){
        			loc = new Location<>(w, region.getCenterX() + 0.500, i + 1, region.getCenterZ() + 0.500);
        			break;
        		}
        	}
    	}

    	if (loc != null){
    		if (play != null){
    			play.setLocation(loc);
    			RPLang.sendMessage(play, RPLang.get("cmdmanager.region.teleport") + " " + rname);
    			RPLang.sendMessage(p, RPLang.get("cmdmanager.region.tpother") + " " + rname);
    		} else {
    			tpWait(p, loc, rname);
    		}
		}
    }

	private static void tpWait(final Player p, final Location<World> loc, final String rname){
		if (RedProtect.get().ph.hasGenPerm(p, "teleport")){
			p.setLocation(loc);
			return;
		}
		if (!RedProtect.get().tpWait.contains(p.getName())){
    		RedProtect.get().tpWait.add(p.getName());
    		RPLang.sendMessage(p, "cmdmanager.region.tpdontmove");
    		Sponge.getScheduler().createSyncExecutor(RedProtect.get().container).schedule(() -> {
                if (RedProtect.get().tpWait.contains(p.getName())){
                    RedProtect.get().tpWait.remove(p.getName());
                    p.setLocation(loc);
                    RPLang.sendMessage(p,RPLang.get("cmdmanager.region.teleport") + " " + rname);
                }
            }, RedProtect.get().cfgs.getInt("region-settings.teleport-time"), TimeUnit.SECONDS);
    	} else {
    		RPLang.sendMessage(p, "cmdmanager.region.tpneedwait");
    	}
	}

	private static void HandleHelPage(CommandSource sender, int page){
		sender.sendMessage(RPUtil.toText(RPLang.get("_redprotect.prefix")+" "+RPLang.get("cmdmanager.available.cmds")));
		sender.sendMessage(RPUtil.toText(RPLang.get("general.color")+"------------------------------------"));
		sender.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.helpheader.alias")));
		if (sender instanceof Player){
			Player player = (Player)sender;
			int i = 0;
			for (String key:RPLang.helpStrings()){
				if (RedProtect.get().ph.hasUserPerm(player, key) || ((key.equals("pos1") || key.equals("pos1")) && RedProtect.get().ph.hasGenPerm(player, "redefine"))) {
					i++;

					if (i > (page*5)-5 && i <= page*5){
						player.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.help."+key).replace("{cmd}", getCmd(key)).replace("{alias}", getCmdAlias(key))));
					}
					if (i > page*5){
						sender.sendMessage(RPUtil.toText(RPLang.get("general.color")+"------------------------------------"));
						player.sendMessage(RPUtil.toText(RPLang.get("cmdmanager.page").replace("{page}", ""+(page+1))));
						break;
					}
				}
			}
		} else {
			sender.sendMessage(RPUtil.toText("&6rp setconfig list &3- List all editable configs"));
			sender.sendMessage(RPUtil.toText("&6rp setconfig <Config-Section> <Value> &3- Set a config option"));
			sender.sendMessage(RPUtil.toText("&6rp info <region> <world> &3- Info about a region"));
			sender.sendMessage(RPUtil.toText("&6rp flag <regionName> <Flag> <Value> <World> &3- Set a flag on region"));
			sender.sendMessage(RPUtil.toText("&6rp flag info <region> <world> &3- Flag info for region"));
			sender.sendMessage(RPUtil.toText("&6rp tp <player> <regionName> <World> &3- Teleport player to a region"));
			sender.sendMessage(RPUtil.toText("&6rp limit <player> &3- Area limit for player"));
			sender.sendMessage(RPUtil.toText("&6rp claimlimit <player> [world] &3- Claim limit for player"));
			sender.sendMessage(RPUtil.toText("&6rp list-all &3- List All regions"));
			sender.sendMessage(RPUtil.toText("&6rp list <player> &3- List All player regions"));
			sender.sendMessage(RPUtil.toText("&6rp single-to-files &3- Convert single world files to regions files"));
			sender.sendMessage(RPUtil.toText("&6rp files-to-single &3- Convert regions files to single world files"));
			sender.sendMessage(RPUtil.toText("&6rp fileTomysql &3- Convert from File to Mysql"));
			sender.sendMessage(RPUtil.toText("&6rp mysqlToFile &3- Convert from Mysql to File"));
			sender.sendMessage(RPUtil.toText("&6rp save-all &3- Save all regions to database"));
			sender.sendMessage(RPUtil.toText("&6rp load-all &3- Load all regions from database"));
			sender.sendMessage(RPUtil.toText("&6rp reload-config &3- Reload only the config"));
			sender.sendMessage(RPUtil.toText("&6rp reload &3- Reload the plugin"));
		}
        sender.sendMessage(RPUtil.toText(RPLang.get("general.color") + "------------------------------------"));
		if (RedProtect.get().ph.hasPerm(sender, "admin")){
            String jarversion = RedProtect.get().container.getSource().get().toFile().getName();
			sender.sendMessage(RPUtil.toText("&8&o- UChat full version: "+jarversion));
		}
	}

	@Override
	public List<String> getSuggestions(CommandSource source, String arguments, Location<World> loc)
			throws CommandException {

		List<String> SotTab = new ArrayList<>();
    	SortedSet<String> tab = new TreeSet<>();

		String[] args = arguments.split(" ");
		if (source instanceof Player){
			if (args.length == 1){
				if (checkCmd(args[0], "flag")){
        			for (String flag:RedProtect.get().cfgs.getDefFlags()){
        				if (RedProtect.get().ph.hasAdminFlagPerm((Player)source, flag) && !tab.contains(flag)){
        					tab.add(flag);
        				}
        			}
        			for (String flag:RedProtect.get().cfgs.AdminFlags){
        				if (RedProtect.get().ph.hasAdminFlagPerm((Player)source, flag) && !tab.contains(flag)){
        					tab.add(flag);
        				}
        			}
        			SotTab.addAll(tab);
        			return SotTab;
        		}
    			for (Object key:RPLang.Lang.keySet()){
        			if (key.toString().startsWith("cmdmanager.translation.") && !key.toString().endsWith(".alias")){
        				String cmdraw = key.toString().replace("cmdmanager.translation.", "");
        				String cmdtrans = RPLang.get(key.toString());
        				
        				if (cmdtrans.startsWith(args[0]) && RedProtect.get().ph.hasUserPerm((Player) source, cmdraw) && !tab.contains(cmdraw)){
        					tab.add(cmdtrans);
        				}
        			}
        		}    			
    			SotTab.addAll(tab);
    			return SotTab;
    		}
    		if (args.length == 2){
        		if (checkCmd(args[0], "flag")){
        			for (String flag:RedProtect.get().cfgs.getDefFlags()){
        				if (flag.startsWith(args[1]) && RedProtect.get().ph.hasAdminFlagPerm((Player)source, flag) && !tab.contains(flag)){
        					tab.add(flag);
        				}
        			} 
        			for (String flag:RedProtect.get().cfgs.AdminFlags){
        				if (flag.startsWith(args[1]) && RedProtect.get().ph.hasAdminFlagPerm((Player)source, flag) && !tab.contains(flag)){
        					tab.add(flag);
        				}
        			}
        			SotTab.addAll(tab);
        			return SotTab;
        		}
        	}
		} else {
			if (args.length == 1){
    			List<String> consolecmds = Arrays.asList("files-to-single", "single-to-files", "flag", "teleport", "filetomysql", "mysqltofile", "reload", "reload-config", "save-all", "load-all", "limit", "claimlimit", "list-all");
        		for (String command:consolecmds){
    				if (command.startsWith(args[0])){
    					tab.add(command);
    				}
    			}
        		SotTab.addAll(tab);
    			return SotTab;
    		} 
		}		
		return SotTab;
	}

	@Override
	public boolean testPermission(CommandSource source) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Optional<Text> getShortDescription(CommandSource source) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<Text> getHelp(CommandSource source) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Text getUsage(CommandSource source) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
