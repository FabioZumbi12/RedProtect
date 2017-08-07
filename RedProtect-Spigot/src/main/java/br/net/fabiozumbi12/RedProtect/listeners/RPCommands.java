package br.net.fabiozumbi12.RedProtect.listeners;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import me.ellbristow.mychunk.LiteChunk;
import me.ellbristow.mychunk.MyChunkChunk;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;

import br.net.fabiozumbi12.RedProtect.RPEconomy;
import br.net.fabiozumbi12.RedProtect.RPGui;
import br.net.fabiozumbi12.RedProtect.RPUtil;
import br.net.fabiozumbi12.RedProtect.RedProtect;
import br.net.fabiozumbi12.RedProtect.Region;
import br.net.fabiozumbi12.RedProtect.RegionBuilder;
import br.net.fabiozumbi12.RedProtect.Updater;
import br.net.fabiozumbi12.RedProtect.Fanciful.FancyMessage;
import br.net.fabiozumbi12.RedProtect.actions.DefineRegionBuilder;
import br.net.fabiozumbi12.RedProtect.actions.RedefineRegionBuilder;
import br.net.fabiozumbi12.RedProtect.config.RPConfig;
import br.net.fabiozumbi12.RedProtect.config.RPLang;
import br.net.fabiozumbi12.RedProtect.events.DeleteRegionEvent;
import br.net.fabiozumbi12.RedProtect.events.RenameRegionEvent;
import br.net.fabiozumbi12.RedProtect.hooks.AWEListener;
import br.net.fabiozumbi12.RedProtect.hooks.MojangUUIDs;
import br.net.fabiozumbi12.RedProtect.hooks.SCHook;
import br.net.fabiozumbi12.RedProtect.hooks.WEListener;
import br.net.fabiozumbi12.RedProtect.schematics.RPSchematics;

@SuppressWarnings("deprecation")
public class RPCommands implements CommandExecutor, TabCompleter{
    
	public RPCommands(){
		RedProtect.logger.debug("Loaded RPCommands...");
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
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args){
    	List<String> SotTab = new ArrayList<String>();    
    	SortedSet<String> tab = new TreeSet<String>();  
    	if (sender instanceof Player){
    		
    		if (args.length == 1){
    			for (String key:RPLang.Lang.keySet()){    			
        			if (key.startsWith("cmdmanager.translation.") && !key.endsWith(".alias")){
        				String cmdraw = key.replace("cmdmanager.translation.", "");
        				String cmdtrans = RPLang.get(key);
        				
        				if (cmdtrans.startsWith(args[0]) && RedProtect.ph.hasGenPerm((Player) sender, cmdraw) && !tab.contains(cmdraw)){
        					tab.add(cmdtrans);
        				}
        			}
        		}    			
    			SotTab.addAll(tab);
    			return SotTab;
    		}
    		if (args.length == 2){
        		if (checkCmd(args[0], "flag")){
        			for (String flag:RPConfig.getDefFlags()){
        				if (flag.startsWith(args[1]) && sender.hasPermission("redprotect.flag."+ flag) && !tab.contains(flag)){
        					tab.add(flag);
        				}
        			} 
        			for (String flag:RPConfig.AdminFlags){
        				if (flag.startsWith(args[1]) && sender.hasPermission("redprotect.admin.flag."+ flag) && !tab.contains(flag)){
        					tab.add(flag);
        				}
        			}
        			SotTab.addAll(tab);
        			return SotTab;
        		}
        	}
    	} else {
    		if (args.length == 1){
    			List<String> consolecmds = Arrays.asList("files-to-single", "single-to-files", "flag", "teleport", "ymltomysql", "mysqltoyml", "setconfig", "reload", "reload-config", "save-all", "load-all", "limit", "claimlimit", "list-all");
        		for (String command:consolecmds){
    				if (command.startsWith(args[0])){
    					tab.add(command);
    				}
    			}
        		SotTab.addAll(tab);
    			return SotTab;
    		}    		
    	}
		return null;    	
    }    
	
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, final String[] args) {
		
		if (args.length == 0) {
            sender.sendMessage(RPLang.get("general.color") + "---------------- " + RedProtect.pdf.getFullName() + " ----------------");
            sender.sendMessage(RPLang.get("general.color") + "Developed by " + ChatColor.GOLD + RedProtect.pdf.getAuthors() + RPLang.get("general.color") + ".");
            sender.sendMessage(RPLang.get("general.color") + "For more information about the commands, type [" + ChatColor.GOLD + "/rp "+ getCmd("help") + RPLang.get("general.color") + "].");
            sender.sendMessage(RPLang.get("general.color") + "For a tutorial, type [" + ChatColor.GOLD + "/rp "+ getCmd("tutorial") + RPLang.get("general.color") + "].");
            sender.sendMessage(RPLang.get("general.color") + "---------------------------------------------------");
            return true;
        }
		
		if (args.length == 2){
			if (args[0].equalsIgnoreCase("test-uuid")){
				
				try {
					String name = MojangUUIDs.getUUID(args[1]);
					RedProtect.logger.warning("Leader from: " + args[1]);
					RedProtect.logger.warning("UUID To name: " + name); 
				} catch (Exception e) {
					e.printStackTrace();
				}				
				return true;
			}
		}
		
        if (!(sender instanceof Player)) {        	
        	if (args.length == 1) {    
        		
        		if (args[0].equalsIgnoreCase("clear-kicks")){
        			RedProtect.denyEnter.clear();
        			RedProtect.logger.sucess("All region kicks was clear");
        			return true;
                }
        		
        		if (args[0].equalsIgnoreCase("single-to-files")) {
        			RedProtect.logger.sucess("["+RPUtil.SingleToFiles()+"]"+" regions converted to your own files with success");
        			return true;
        		}
        		
        		if (args[0].equalsIgnoreCase("files-to-single")) {
        			RedProtect.logger.sucess("["+RPUtil.FilesToSingle()+"]"+" regions converted to unified world file with success");
        			return true;
        		}
        		        		
        		if (args[0].equalsIgnoreCase("ymlToMysql")) {
        			try {
						if (!RPUtil.ymlToMysql()){
							RedProtect.logger.severe("ERROR: Check if your 'file-type' configuration is set to 'yml' before convert from YML to Mysql.");
							return true;
						} else {
							RedProtect.plugin.getConfig().set("file-type", "mysql");
							RedProtect.plugin.saveConfig();
							RedProtect.plugin.getServer().getPluginManager().disablePlugin((Plugin)RedProtect.plugin);
		        			RedProtect.plugin.getServer().getPluginManager().enablePlugin((Plugin)RedProtect.plugin);
		        			RedProtect.logger.sucess("RedProtect reloaded with Mysql as database! Ready to use!");
		        			return true;
						}
					} catch (Exception e) {
						e.printStackTrace();
						return true;
					}
        		}
        		
        		if (args[0].equalsIgnoreCase("mysqlToYml")) {
        			try {
						if (!RPUtil.mysqlToYml()){
							RedProtect.logger.severe("ERROR: Check if your 'file-type' configuration is set to 'mysql' before convert from MYSQL to Yml.");
							return true;
						} else {
							RedProtect.plugin.getConfig().set("file-type", "yml");
							RedProtect.plugin.saveConfig();
							RedProtect.plugin.getServer().getPluginManager().disablePlugin((Plugin)RedProtect.plugin);
		        			RedProtect.plugin.getServer().getPluginManager().enablePlugin((Plugin)RedProtect.plugin);
		        			RedProtect.logger.sucess("RedProtect reloaded with Yml as database! Ready to use!");
		        			return true;
						}
					} catch (Exception e) {
						e.printStackTrace();
						return true;
					}
        		}
        		if (args[0].equalsIgnoreCase("gpTorp")) {
        			if (!RedProtect.GP){
        				RedProtect.logger.sucess("The plugin GriefPrevention is not installed or is disabled");
        				return true;
        			}
        			if (RPUtil.convertFromGP() == 0){
						RedProtect.logger.severe("No region converted from GriefPrevention.");
						return true;
					} else {
						RedProtect.rm.saveAll();
						RedProtect.logger.info(ChatColor.AQUA + "[" + RPUtil.convertFromGP() + "] regions converted from GriefPrevention with success");
						RedProtect.plugin.getServer().getPluginManager().disablePlugin((Plugin)RedProtect.plugin);
	        			RedProtect.plugin.getServer().getPluginManager().enablePlugin((Plugin)RedProtect.plugin);						
	        			return true;
					}
        		}
        		if (checkCmd(args[0], "update")) {
        			if (RedProtect.Update){
            			RedProtect.logger.info(ChatColor.AQUA + "Starting download update...");
            			new Updater(RedProtect.plugin, 87463, RedProtect.JarFile, Updater.UpdateType.NO_VERSION_CHECK, true);
            			RedProtect.logger.sucess("Download completed! Restart your server to use the new version.");
            			return true;
            		} else {
            			RedProtect.logger.info(ChatColor.AQUA + "No updates to download!");
            			return true;
            		}
        		}        		
        		
        		if (args[0].equalsIgnoreCase("list-all")) {
        			int total = 0;
        			for (Region r:RedProtect.rm.getAllRegions()){
        				RedProtect.logger.info(ChatColor.GREEN + "[" + total + "]" + "Region: " + r.getName() + ChatColor.RESET + " | " + ChatColor.AQUA + "World: " + r.getWorld() + ChatColor.RESET);
        				total ++;
        			}
        			RedProtect.logger.sucess(total + " regions for " + Bukkit.getWorlds().size() + " worlds.");
        			return true;
        		}
        		
        		if (args[0].equalsIgnoreCase("mychunktorp")) {
            		if (handleMyChunk()){            			
            			RedProtect.rm.saveAll();
            			RedProtect.plugin.getServer().getPluginManager().disablePlugin((Plugin)RedProtect.plugin);
	        			RedProtect.plugin.getServer().getPluginManager().enablePlugin((Plugin)RedProtect.plugin);
	        			RedProtect.logger.sucess("...converting MyChunk database");
            			RedProtect.logger.sucess("http://dev.bukkit.org/bukkit-plugins/mychunk/");
            			return true;
            		} else {
            			RedProtect.logger.sucess("The plugin MyChunk is not installed or no regions found");
            			return true;
            		}        		
            	}
        		if (args[0].equalsIgnoreCase("save-all")) {
        			RedProtect.logger.SaveLogs();
            		RedProtect.logger.sucess(RedProtect.rm.saveAll() + " regions saved with success!");  
            		return true;
            	}
        		if (args[0].equalsIgnoreCase("load-all")) {            
        			RedProtect.rm.clearDB();
        			try {
						RedProtect.rm.loadAll();
					} catch (Exception e) {
						RedProtect.logger.severe("Error on load all regions from database files:");
						e.printStackTrace();
					}
            		RedProtect.logger.sucess(RedProtect.rm.getAllRegions().size() + " regions has been loaded from database files!");  
            		return true;
            	}
        		if (checkCmd(args[0], "reload")) {
        			RedProtect.plugin.getServer().getPluginManager().disablePlugin((Plugin)RedProtect.plugin);
        			RedProtect.plugin.getServer().getPluginManager().enablePlugin((Plugin)RedProtect.plugin);
            		RedProtect.logger.sucess("RedProtect Plus reloaded!");
            		return true;
            	}      
        		if (args[0].equalsIgnoreCase("reload-config")) {
        			RPConfig.init();
        			RPLang.init();
        			RedProtect.logger.sucess("RedProtect Plus configs reloaded!");
        			return true;
        		}
        	} 
        	
        	if(args.length == 2){
        		
        		//rp removeall <player>
        		if (checkCmd(args[0], "removeall")) {
        			int removed = RedProtect.rm.removeAll(args[1]);
        			if (removed <= 0){
        				RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.noneremoved"));
        			} else {
        				RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.removed").replace("{regions}", removed+"").replace("{player}", args[1]));
        			}        			
        			return true;
        		}
        		
        		//rp regenall <player>
        		if (checkCmd(args[0], "regenall")) {
        			int regen = RedProtect.rm.regenAll(args[1]);
        			if (regen <= 0){
        				RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.noneregenerated"));
        			} else {
        				RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.regenerated").replace("{regions}", regen+"").replace("{player}", args[1]));
        			}
        			return true;
        		}
        		
        		//rp regen stop
        		if (args[0].equalsIgnoreCase("regen") && args[1].equalsIgnoreCase("stop")) {
        			if (!RedProtect.WE){
        				return true;
        			}
        			RPUtil.stopRegen = true;
        			RPLang.sendMessage(sender, "&aRegen will stop now. To continue reload the plugin!");
        			return true;
        		}
        		
        		//rp list <player>
        		if (checkCmd(args[0], "list")){        			
        			getRegionforList(sender, RPUtil.PlayerToUUID(args[1]), 1);
        			return true;
        		}
        		
        		if  (args[0].equalsIgnoreCase("setconfig") && args[1].equalsIgnoreCase("list")){           		
        			sender.sendMessage(ChatColor.AQUA + "=========== Config Sections: ===========");
            		for (String section:RedProtect.plugin.getConfig().getValues(false).keySet()){
            			if (section.contains("debug-messages") || 
            					section.contains("file-type") ||
            					section.contains("language")){
            				sender.sendMessage(ChatColor.GOLD + section + " : " + ChatColor.GREEN + RedProtect.plugin.getConfig().get(section).toString());
            			}            			
            		} 
            		sender.sendMessage(ChatColor.AQUA + "====================================");
            		return true;
                }  
        		
        		//rp clamilimit player
        		if  (checkCmd(args[0], "claimlimit")){ 
        			Player offp = RedProtect.serv.getOfflinePlayer(args[1]).getPlayer();
                	if (offp == null){
                		sender.sendMessage(RPLang.get("cmdmanager.noplayer.thisname").replace("{player}", args[1]));
                		return true;
                	}
                	int limit = RedProtect.ph.getPlayerClaimLimit(offp);
                    if (limit < 0 || RedProtect.ph.hasPerm(offp, "redprotect.limit.claim.unlimited")) {
                    	sender.sendMessage(RPLang.get("cmdmanager.nolimit"));
                        return true;
                    }
                    
                    int currentUsed = RedProtect.rm.getRegions(RPUtil.PlayerToUUID(offp.getName()), offp.getWorld()).size();
                    ChatColor color = currentUsed >= limit ? ChatColor.RED:ChatColor.GOLD;
                    sender.sendMessage(RPLang.get("cmdmanager.yourclaims") + color + currentUsed + RPLang.get("general.color") + "/" + color + limit + RPLang.get("general.color"));
                    return true;
        		}
        		
        		if (checkCmd(args[0], "limit")) {
        			Player offp = RedProtect.serv.getOfflinePlayer(args[1]).getPlayer();
                	if (offp == null){
                		sender.sendMessage(RPLang.get("cmdmanager.noplayer.thisname").replace("{player}", args[1]));
                		return true;
                	}
                	int limit = RedProtect.ph.getPlayerBlockLimit(offp);
                    if (limit < 0 || RedProtect.ph.hasPerm(offp, "redprotect.limit.blocks.unlimited")) {
                    	sender.sendMessage(RPLang.get("cmdmanager.nolimit"));
                        return true;
                    }
                    
                    int currentUsed = RedProtect.rm.getTotalRegionSize(RPUtil.PlayerToUUID(offp.getName()), offp.getWorld().getName());
                    ChatColor color = currentUsed >= limit ? ChatColor.RED:ChatColor.GOLD;
                    sender.sendMessage(RPLang.get("cmdmanager.yourarea") + color + currentUsed + RPLang.get("general.color") + "/" + color + limit + RPLang.get("general.color"));
                    return true;
        		}
        		
        	}
               
        	if (args.length == 3){        		
        		//rp regen <region> <world>
        		if (args[0].equalsIgnoreCase("regen")) {
        			if (!RedProtect.WE){
        				return true;
        			}
        			World w = RedProtect.serv.getWorld(args[2]);
        			if (w == null){
                    	sender.sendMessage(RPLang.get("cmdmanager.region.invalidworld"));
                    	return true;
                    }
        			Region r = RedProtect.rm.getRegion(args[1], w);
        			if (r == null){
        				sender.sendMessage(RPLang.get("correct.usage") + " " + ChatColor.YELLOW + "Invalid region: " + args[1]);
        				return true;
        			}
        			
        			if (RedProtect.AWE && RPConfig.getBool("hooks.asyncworldedit.use-for-regen")){
        				AWEListener.regenRegion(r, Bukkit.getWorld(r.getWorld()), r.getMaxLocation(), r.getMinLocation(), 0,sender, false);        				
        			} else {
        				WEListener.regenRegion(r, Bukkit.getWorld(r.getWorld()), r.getMaxLocation(), r.getMinLocation(), 0, sender, false);
        			}        			
        			return true;
        		}
        		
        		//rp undo <region> <world>
        		if (args[0].equalsIgnoreCase("undo")) {
        			if (!RedProtect.WE){
        				return true;
        			}
        			World w = RedProtect.serv.getWorld(args[2]);
        			if (w == null){
                    	sender.sendMessage(RPLang.get("cmdmanager.region.invalidworld"));
                    	return true;
                    }
        			Region r = RedProtect.rm.getRegion(args[1], w);
        			if (r == null){
        				sender.sendMessage(RPLang.get("correct.usage") + " " + ChatColor.YELLOW + "Invalid region: " + args[1]);
        				return true;
        			}
        			
        			if (RedProtect.AWE && RPConfig.getBool("hooks.asyncworldedit.use-for-regen")){
        				if (AWEListener.undo(r.getID())){
        					RPLang.sendMessage(sender, RPLang.get("cmdmanager.regen.undo.sucess").replace("{region}", r.getName()));
        				} else {
        					RPLang.sendMessage(sender, RPLang.get("cmdmanager.regen.undo.none").replace("{region}", r.getName()));
        				}        				      				
        			} else {
                        if (WEListener.undo(r.getID())){
                        	RPLang.sendMessage(sender, RPLang.get("cmdmanager.regen.undo.sucess").replace("{region}", r.getName()));
        				} else {
        					RPLang.sendMessage(sender, RPLang.get("cmdmanager.regen.undo.none").replace("{region}", r.getName()));
        				} 
        			}        			
        			return true;
        		}
        		
        		//rp list <player> [page]
        		if (checkCmd(args[0], "list")){        			
        			try{
                    	int Page = Integer.parseInt(args[2]);
                        	getRegionforList(sender, RPUtil.PlayerToUUID(args[1]), Page);
                        	return true;
                    	} catch(NumberFormatException  e){
                            RPLang.sendMessage(sender, "cmdmanager.region.listpage.error");   
                            return true;
                    } 
        		}
        		
        		//rp clamilimit player world
        		if  (checkCmd(args[0], "claimlimit")){ 
        			Player offp = RedProtect.serv.getOfflinePlayer(args[1]).getPlayer();
        			World w = RedProtect.serv.getWorld(args[2]);
                	if (offp == null){
                		sender.sendMessage(RPLang.get("cmdmanager.noplayer.thisname").replace("{player}", args[1]));
                		return true;
                	}
                	int limit = RedProtect.ph.getPlayerClaimLimit(offp);
                    if (limit < 0 || RedProtect.ph.hasPerm(offp, "redprotect.limit.claim.unlimited")) {
                    	sender.sendMessage(RPLang.get("cmdmanager.nolimit"));
                        return true;
                    }
                    
                    if (w == null){
                    	sender.sendMessage(RPLang.get("cmdmanager.region.invalidworld"));
                    	return true;
                    }
                    
                    int currentUsed = RedProtect.rm.getRegions(RPUtil.PlayerToUUID(offp.getName()), w).size();
                    ChatColor color = currentUsed >= limit ? ChatColor.RED:ChatColor.GOLD;
                    sender.sendMessage(RPLang.get("cmdmanager.yourclaims") + color + currentUsed + RPLang.get("general.color") + "/" + color + limit + RPLang.get("general.color"));
                    return true;
        		}
        		        		
        		if  (args[0].equalsIgnoreCase("setconfig")){
        			if (args[1].equals("debug-messages") || 
        					args[1].equals("file-type") ||
        					args[1].equals("language")){
        				Object from = RedProtect.plugin.getConfig().get(args[1]); 
            			if (args[2].equals("true") || args[2].equals("false")){
            				RedProtect.plugin.getConfig().set(args[1], Boolean.parseBoolean(args[2]));
            			} else {
            				try {
                				int value = Integer.parseInt(args[2]);
                				RedProtect.plugin.getConfig().set(args[1], value);
                		    } catch(NumberFormatException ex){
                		    	RedProtect.plugin.getConfig().set(args[1], args[2]);
                		    }
            			}
            			sender.sendMessage(RPLang.get("cmdmanager.configset") + " " + from.toString() + " > " + args[2]);
            			RPConfig.save();
            			return true;
            		} else {
            			sender.sendMessage(RPLang.get("cmdmanager.confignotset") + " " + args[1]);
            			return true;
            		}
        		}
        		
        		//rp info <region> <world>
        		if (checkCmd(args[0], "info")) {
        			if (Bukkit.getWorld(args[2]) != null){
        				Region r = RedProtect.rm.getRegion(args[1], Bukkit.getWorld(args[2]));
        				if (r != null){
        					sender.sendMessage(RPLang.get("general.color") + "-----------------------------------------");
        					sender.sendMessage(r.info());
        					sender.sendMessage(RPLang.get("general.color") + "-----------------------------------------");
        				} else {
        					sender.sendMessage(RPLang.get("correct.usage") + " " + ChatColor.YELLOW + "Invalid region: " + args[1]);
        				}
        			} else {
        				sender.sendMessage(RPLang.get("correct.usage") + " " + ChatColor.YELLOW + "Invalid World: " + args[2]);
        			}
                    return true;
                }
        	}
        	        	
        	if (args.length == 4) {        		
        		//rp kick <player> [region] [world]
                if (checkCmd(args[0], "kick")){
                	Region r = RedProtect.rm.getRegion(args[2], args[3]); 
    				
    				if (r == null){
    	    			sender.sendMessage("No regions by name "+args[2]+" or by world "+args[3]);
    					return true;
    	    		}
        			
        			Player visit = Bukkit.getPlayer(args[1]);
    				if (visit == null){
    					sender.sendMessage("No online players with name "+args[1]);
    					return true;
    				}	
            		
            		if (r.canBuild(visit)){
            			sender.sendMessage("You cant kick members of a region");
        				return true;
        			}
        			Region rv = RedProtect.rm.getTopRegion(visit.getLocation());
        			if (rv == null || rv != null && !rv.getID().equals(r.getID())){
        				sender.sendMessage("This player is not on this region");
        				return true;
        			}
        			
        			RPUtil.DenyEnterPlayer(visit.getWorld(), visit.getLocation(), visit.getLocation(), visit, r, true);
        			
        			String sec = String.valueOf(RPConfig.getInt("region-settings.delay-after-kick-region"));
        			if (RedProtect.plugin.denyEnterRegion(r.getID(), visit.getName())){
        				RPUtil.DenyEnterPlayer(visit.getWorld(), visit.getLocation(), visit.getLocation(), visit, r, true);
        				sender.sendMessage("The player "+visit.getName()+" has been kicked from "+r.getName()+" by "+sec+" seconds.");
        			} else {
        				sender.sendMessage("This player is already kicked from this region by "+sec);
        			}
        			return true;			
                }
                
        		if (checkCmd(args[0], "teleport")){
        			//rp teleport <player> <region> <world>
                	Player play = RedProtect.serv.getPlayer(args[1]);
                	if (play != null){                		
                		World w = RedProtect.serv.getWorld(args[3]);                		
                		if (w == null) {
                            sender.sendMessage(RPLang.get("cmdmanager.region.invalidworld"));
                            return true;
                        }
                    	Region region = RedProtect.rm.getRegion(args[2], w);
                    	if (region == null) {
                    		sender.sendMessage(RPLang.get("cmdmanager.region.doesntexist") + ": " + args[2]);
                            return true;
                        } 
                    	
                    	Location loc = null;
                    	if (region.getTPPoint() != null){
                    		loc = region.getTPPoint();
                    		loc.setX(loc.getBlockX()+0.500);
                			loc.setZ(loc.getBlockZ()+0.500);
                    	} else {
                    		int limit = w.getMaxHeight();
                        	if (w.getEnvironment().equals(Environment.NETHER)){
                        		limit = 124;
                        	}
                        	for (int i = limit; i > 0; i--){
                        		Material mat = w.getBlockAt(region.getCenterX(), i, region.getCenterZ()).getType();
                        		Material mat1 = w.getBlockAt(region.getCenterX(), i+1, region.getCenterZ()).getType();
                        		Material mat2 = w.getBlockAt(region.getCenterX(), i+2, region.getCenterZ()).getType();
                        		if ((!mat.equals(Material.LAVA) || !mat.equals(Material.STATIONARY_LAVA)) && !mat.equals(Material.AIR) && mat1.equals(Material.AIR) && mat2.equals(Material.AIR)){
                        			loc = new Location(w, region.getCenterX()+0.500, i+1, region.getCenterZ()+0.500);            			
                        			break;
                        		}
                        	}
                    	}               		
                    	                
                    	if (RedProtect.Ess){
                    		RedProtect.pless.getUser(play).setLastLocation();
                    	}
                    	play.teleport(loc);
            			RPLang.sendMessage(play,RPLang.get("cmdmanager.region.teleport") + " " + args[2]);     
            			sender.sendMessage(ChatColor.AQUA + "Player "+play.getName()+" teleported to " + args[2]);
                		return true;
                	} else {
                		sender.sendMessage(RPLang.get("cmdmanager.noplayer.thisname").replace("{player}", args[1]));
                		HandleHelPage(sender, 1);
                		return true;
                	}
        		}          
        		
        		//rp flag info <region> <world>
        		if (checkCmd(args[0], "flag") && checkCmd(args[1], "info")) {
        			if (Bukkit.getWorld(args[3]) != null){
        				Region r = RedProtect.rm.getRegion(args[2], Bukkit.getWorld(args[3]));
        				if (r != null){
        					sender.sendMessage(RPLang.get("general.color") + "------------[" + RPLang.get("cmdmanager.region.flag.values") + "]------------");
        					sender.sendMessage(r.getFlagInfo());
                            sender.sendMessage(RPLang.get("general.color") + "------------------------------------");
        				} else {
        					sender.sendMessage(RPLang.get("correct.usage") + " " + ChatColor.YELLOW + "Invalid region: " + args[2]);
        				}
        			} else {
        				sender.sendMessage(RPLang.get("correct.usage") + " " + ChatColor.YELLOW + "Invalid World: " + args[3]);
        			}
                    return true;
                }
            }
        	
    		if (args.length == 5){
    			/*/rp flag <regionName> <flag> <value> <world>*/
    			if  (checkCmd(args[0], "flag")){
    				World w = RedProtect.serv.getWorld(args[4]);
        			if (w == null){
        				sender.sendMessage(RPLang.get("correct.usage") + ChatColor.YELLOW + " rp "+getCmd("flag")+" <regionName> <flag> <value> <world>");
        				return true;
        			}
        			Region r = RedProtect.rm.getRegion(args[1], w);
        			if (r != null && (RPConfig.getDefFlags().contains(args[2]) || RPConfig.AdminFlags.contains(args[2]))){
        				Object objflag = RPUtil.parseObject(args[3]);
        				r.setFlag(args[2], objflag);
        				sender.sendMessage(RPLang.get("cmdmanager.region.flag.set").replace("{flag}", "'"+args[2]+"'") + " " + r.getFlagString(args[2]));
        				RedProtect.logger.addLog("Console changed flag "+args[2]+" to "+r.getFlagString(args[2]));
        				return true;
        			}
    			}    			
    		}    		
        	HandleHelPage(sender, 1);
            return true;            
        }
        
        //commands as player
        final Player player = (Player)sender;
        
        if (args.length == 1) {        	
        	
        	String claimmode = RPConfig.getWorldClaimType(player.getWorld().getName());
        	if (claimmode.equalsIgnoreCase("WAND") || claimmode.equalsIgnoreCase("BOTH") || RedProtect.ph.hasGenPerm(player, "redefine")){
        		//rp pos1
        		if (checkCmd(args[0], "pos1")){
                	Location pl = player.getLocation();
                	RedProtect.firstLocationSelections.put(player, pl);
            		player.sendMessage(RPLang.get("playerlistener.wand1") + RPLang.get("general.color") + " (" + ChatColor.GOLD + pl.getBlockX() + RPLang.get("general.color") + ", " + ChatColor.GOLD + pl.getBlockY() + RPLang.get("general.color") + ", " + ChatColor.GOLD + pl.getBlockZ() + RPLang.get("general.color") + ").");
            		//show preview border
                    if (RedProtect.firstLocationSelections.containsKey(player) && RedProtect.secondLocationSelections.containsKey(player)){       
                    	RPUtil.addBorder(player, RPUtil.get4Points(RedProtect.firstLocationSelections.get(player),RedProtect.secondLocationSelections.get(player),player.getLocation().getBlockY()));                	
                    }
                    return true;
        		}
            	
            	//rp pos2
            	if (checkCmd(args[0], "pos2")){
                	Location pl = player.getLocation();
                	RedProtect.secondLocationSelections.put(player, pl);
            		player.sendMessage(RPLang.get("playerlistener.wand2") + RPLang.get("general.color") + " (" + ChatColor.GOLD + pl.getBlockX() + RPLang.get("general.color") + ", " + ChatColor.GOLD + pl.getBlockY() + RPLang.get("general.color") + ", " + ChatColor.GOLD + pl.getBlockZ() + RPLang.get("general.color") + ").");
            		//show preview border
                    if (RedProtect.firstLocationSelections.containsKey(player) && RedProtect.secondLocationSelections.containsKey(player)){       
                    	RPUtil.addBorder(player, RPUtil.get4Points(RedProtect.firstLocationSelections.get(player),RedProtect.secondLocationSelections.get(player),player.getLocation().getBlockY()));                	
                    }
                    return true;
            	}
        	}
        	        	
        	//rp list-areas
        	if (checkCmd(args[0], "list-areas") && player.hasPermission("redprotect.list-areas")) {
        		sender.sendMessage(RPLang.get("general.color") + "-------------------------------------------------");
            	RPLang.sendMessage(sender,RPLang.get("cmdmanager.region.created.area-list"));
            	sender.sendMessage("-----");    
        		for (World w:Bukkit.getWorlds()){
        			Set<Region> wregions = new HashSet<Region>();
        			for (Region r:RedProtect.rm.getRegionsByWorld(w)){
        				SimpleDateFormat dateformat = new SimpleDateFormat(RPConfig.getString("region-settings.date-format"));
        				Date now = null;
        				try {
        					now = dateformat.parse(RPUtil.DateNow());
        				} catch (ParseException e1) {
        					RedProtect.logger.severe("The 'date-format' don't match with date 'now'!!");
        				}
        				Date regiondate = null;
                    	try {
            				regiondate = dateformat.parse(r.getDate());
            			} catch (ParseException e) {
            				RedProtect.logger.severe("The 'date-format' don't match with region date!!");
            				e.printStackTrace();
            			}
        				Long days = TimeUnit.DAYS.convert(now.getTime() - regiondate.getTime(), TimeUnit.MILLISECONDS);
                    	for (String play:RPConfig.getStringList("purge.ignore-regions-from-players")){
                    		if (r.isLeader(RPUtil.PlayerToUUID(play)) || r.isAdmin(RPUtil.PlayerToUUID(play))){
                    			continue;
                    		}
            			}
                    	if (!r.isLeader(RPConfig.getString("region-settings.default-leader")) && days > RPConfig.getInt("purge.remove-oldest") && r.getArea() >= RPConfig.getInt("purge.regen.max-area-regen")){  
                    		wregions.add(r);
                    	}
        			}
        			if (wregions.size() == 0){
    					continue;
    				}
        			Iterator<Region> it = wregions.iterator();
        			String colorChar = ChatColor.translateAlternateColorCodes('&', RPConfig.getString("region-settings.world-colors." + w.getName(), "&a"));   
        			if (RPConfig.getBool("region-settings.region-list.hover-and-click-teleport") && RedProtect.ph.hasRegionPermAdmin(sender, "teleport", null)){        				
        				boolean first = true;            			         			         			
            			FancyMessage fancy = new FancyMessage();
            			while (it.hasNext()){
            				Region r = it.next();
            				String rname = RPLang.get("general.color")+", "+ChatColor.GRAY+r.getName()+"("+r.getArea()+")";
            				if (first){
            					rname = rname.substring(3);
            					first = false;
            				}
            				if (!it.hasNext()){
            					rname = rname+RPLang.get("general.color")+".";
            				}
            				fancy.text(rname).color(ChatColor.DARK_GRAY)
                    				.tooltip(RPLang.get("cmdmanager.list.hover").replace("{region}", r.getName()))
                    				.command("/rp "+getCmd("teleport")+" "+r.getName()+" "+r.getWorld())
                    				.then(" ");
            			} 
            			sender.sendMessage(RPLang.get("general.color")+RPLang.get("region.world").replace(":", "")+" "+colorChar+w.getName()+"["+wregions.size()+"]"+ChatColor.RESET+": ");
            			fancy.send(sender);
            			sender.sendMessage("-----");
        			} else {
        				String worldregions = "";
            			while (it.hasNext()){
            				Region r = it.next();
            				worldregions = worldregions+RPLang.get("general.color")+", "+ChatColor.GRAY+r.getName()+"("+r.getArea()+")";
            			}
            			sender.sendMessage(RPLang.get("general.color")+RPLang.get("region.world").replace(":", "")+" "+colorChar+w.getName()+"["+wregions.size()+"]"+ChatColor.RESET+": "); 
            			sender.sendMessage(worldregions.substring(3)+RPLang.get("general.color")+".");
            			sender.sendMessage("-----");  
        			}        			
        		}
        		return true;
        	}
        	
        	//rp start
        	if (checkCmd(args[0], "start") && player.hasPermission("redprotect.start")) {
        		if (!RPConfig.isAllowedWorld(player)){
                	RPLang.sendMessage(player, "regionbuilder.region.worldnotallowed");
                    return true;
                } 
        		
        		RedProtect.confiemStart.add(player.getName());
        		RPLang.sendMessage(player, RPLang.get("cmdmanager.region.confirm").replace("{cmd}", RPLang.get("cmdmanager.translation.confirm.alias")));
        		
        		Bukkit.getScheduler().runTaskLater(RedProtect.plugin, new Runnable() {
					@Override
					public void run() {
						RedProtect.confiemStart.remove(player.getName());
					}
        		}, 600);
        		return true;
        	}
        	
        	//rp confirm
        	if (checkCmd(args[0], "confirm") && player.hasPermission("redprotect.start")) {
        		if (!RedProtect.confiemStart.contains(player.getName())){
                	player.sendMessage(RPLang.get("cmdmanager.region.noconfirm").replace("{cmd}", RPLang.get("cmdmanager.translation.start")));
                    return true;
                } 
        		
        		try {
					RPSchematics.pasteSchematic(player);
				} catch (IOException e) {
					e.printStackTrace();
				}
        		return true;
        	}
        	
        	//rp renew-rent
        	if (checkCmd(args[0], "renew-rent") && player.hasPermission("redprotect.renew-rent") && RedProtect.Vault) {
        		Region r = RedProtect.rm.getTopRegion(player.getLocation());
    			if (r == null){
    				RPLang.sendMessage(player, "cmdmanager.region.doesexists");
    				return true;
    			}
    			String puuid = RPUtil.PlayerToUUID(player.getName());
    			if (!r.isRentFor(puuid)){
    				RPLang.sendMessage(player, "cmdmanager.rent.younotrented");
    				return true;
    			}
    			
    			if (RPUtil.getNowMillis() != r.getRentDateMillis(puuid) && !RPConfig.getBool("region-settings.rent.renew-anytime")){
    				RPLang.sendMessage(player, RPLang.get("cmdmanager.rent.cantrenewanytime").replace("{renew}", r.getRentDateFormated(puuid)));
    				return true;
    			}
    			
    			if (RedProtect.econ.getBalance(player) >= r.getRentValue(puuid)){    				
    				Calendar cal = Calendar.getInstance();
    				String[] opts = RPConfig.getString("region-settings.rent.command-renew-adds").split(":");
    				if (opts[1].equalsIgnoreCase("MONTH")){
    					cal.add(Calendar.MONTH, Integer.valueOf(opts[0]));
    				} else if (opts[1].equalsIgnoreCase("DAY")){
    					cal.add(Calendar.DAY_OF_MONTH, Integer.valueOf(opts[0]));
    				}
    				RedProtect.econ.withdrawPlayer(player, r.getRentValue(puuid));
    				r.setRent(puuid, cal.getTimeInMillis());
    				RPLang.sendMessage(player, RPLang.get("cmdmanager.rent.renewsuccess").replace("{region}", r.getName()).replace("{value}", RPEconomy.getFormatted(r.getRentValue(puuid))));
    			} else {
    				RPLang.sendMessage(player, "cmdmanager.rent.renewfail");
    			}
    			return true;
        	}
        	
        	//rp regen
    		if (checkCmd(args[0], "regen") && player.hasPermission("redprotect.regen")) {
    			if (!RedProtect.WE){
    				return true;
    			}
    			Region r = RedProtect.rm.getTopRegion(player.getLocation());
    			if (r == null){
    				RPLang.sendMessage(player, "cmdmanager.region.doesexists");
    				return true;
    			}
    			
    			if (RedProtect.AWE && RPConfig.getBool("hooks.asyncworldedit.use-for-regen")){
    				AWEListener.regenRegion(r, Bukkit.getWorld(r.getWorld()), r.getMaxLocation(), r.getMinLocation(), 0,sender, false);        				
    			} else {
    				WEListener.regenRegion(r, Bukkit.getWorld(r.getWorld()), r.getMaxLocation(), r.getMinLocation(), 0, sender, false);
    			} 
    			return true;
    		}
    		
    		//rp undo
    		if (args[0].equalsIgnoreCase("undo") && player.hasPermission("redprotect.regen")) {
    			if (!RedProtect.WE){
    				return true;
    			}
    			Region r = RedProtect.rm.getTopRegion(player.getLocation());
    			if (r == null){
    				RPLang.sendMessage(player, "cmdmanager.region.doesexists");
    				return true;
    			}
    			
    			if (RedProtect.AWE && RPConfig.getBool("hooks.asyncworldedit.use-for-regen")){
    				if (AWEListener.undo(r.getID())){
    					RPLang.sendMessage(sender, RPLang.get("cmdmanager.regen.undo.sucess").replace("{region}", r.getName()));
    				} else {
    					RPLang.sendMessage(sender, RPLang.get("cmdmanager.regen.undo.none").replace("{region}", r.getName()));
    				}        				      				
    			} else {
                    if (WEListener.undo(r.getID())){
                    	RPLang.sendMessage(sender, RPLang.get("cmdmanager.regen.undo.sucess").replace("{region}", r.getName()));
    				} else {
    					RPLang.sendMessage(sender, RPLang.get("cmdmanager.regen.undo.none").replace("{region}", r.getName()));
    				} 
    			}        			
    			return true;
    		}
    		
        	if (checkCmd(args[0], "update") && player.hasPermission("redprotect.update")){
        		if (RedProtect.Update){
            		RPLang.sendMessage(player, ChatColor.AQUA + "Starting download update...");
        			new Updater(RedProtect.plugin, 87463, RedProtect.JarFile, Updater.UpdateType.NO_VERSION_CHECK, true);
        			RPLang.sendMessage(player, ChatColor.AQUA + "Update downloaded! Will take effect on next server reboot.");
        			return true;
        		} else {
        			RPLang.sendMessage(player, ChatColor.AQUA + "No updates to download!");
        			return true;
        		}
        	}
        	
        	if (checkCmd(args[0], "laccept")){
        		if (RedProtect.alWait.containsKey(player)){
        			//info = region+world+pname
        			String info = RedProtect.alWait.get(player);
        			
        			Player lsender = Bukkit.getPlayer(info.split("@")[2]);
        			Region r = RedProtect.rm.getRegion(info.split("@")[0],info.split("@")[1]);
        			
        			String VictimUUID = player.getName();
    				if (RedProtect.OnlineMode){
    					VictimUUID = player.getUniqueId().toString();
    				}
    				
        			if (r != null){
        				
        				if (RedProtect.ph.getPlayerClaimLimit(player) == (RedProtect.rm.getRegions(VictimUUID,r.getWorld()).size()+1)){
        					RPLang.sendMessage(player,"regionbuilder.claim.limit");
        					return true;
        				}
        				
        				r.addLeader(VictimUUID);                        
        				RPLang.sendMessage(player, RPLang.get("cmdmanager.region.leader.youadded").replace("{region}", r.getName()) + " " + lsender.getName());
        				if (lsender != null && lsender.isOnline()){
        					RPLang.sendMessage(lsender, RPLang.get("cmdmanager.region.leader.accepted").replace("{region}", r.getName()).replace("{player}", player.getName()));
        				}
        			} else {        				
        				RPLang.sendMessage(player, "cmdmanager.region.doesexists");
        			}
        			RedProtect.alWait.remove(player);
        			return true;
        		} else {
        			RPLang.sendMessage(player, "cmdmanager.norequests");
        			return true;
        		}
        	}
        	
        	if (checkCmd(args[0], "ldeny")){
        		if (RedProtect.alWait.containsKey(player)){
        			//info = region+world+pname
        			String info = RedProtect.alWait.get(player);
        			
        			Player lsender = Bukkit.getPlayer(info.split("@")[2]);
        			Region r = RedProtect.rm.getRegion(info.split("@")[0],info.split("@")[1]);
        			
        			if (r != null){                      
        				RPLang.sendMessage(player, RPLang.get("cmdmanager.region.leader.youdenied").replace("{region}", r.getName()).replace("{player}", lsender.getName()));
        				if (lsender != null && lsender.isOnline()){
        					RPLang.sendMessage(lsender, RPLang.get("cmdmanager.region.leader.denied").replace("{region}", r.getName()).replace("{player}", player.getName()));
        				}
        			} else {        				
        				RPLang.sendMessage(player, "cmdmanager.region.doesexists");
        			}
        			RedProtect.alWait.remove(player);
        			return true;
        		} else {
        			RPLang.sendMessage(player, "cmdmanager.norequests");
        			return true;
        		}
        	}
        	
        	if (checkCmd(args[0], "settp") && RedProtect.ph.hasGenPerm(player, "settp")){
        		Region r = RedProtect.rm.getTopRegion(player.getLocation());
        		if (r != null){
        			if (RedProtect.ph.hasRegionPermLeader(player, "settp", r)){
        				r.setTPPoint(player.getLocation());
            			RPLang.sendMessage(player, "cmdmanager.region.settp.ok");
            			return true;
        			} else {
        				RPLang.sendMessage(player, "playerlistener.region.cantuse");
        				return true;    
        			}        			
        		} else {
    				RPLang.sendMessage(player, "cmdmanager.region.todo.that");
    				return true;    
        		}
        	}
        	
        	if (checkCmd(args[0], "deltp") && RedProtect.ph.hasGenPerm(player, "deltp")){
        		Region r = RedProtect.rm.getTopRegion(player.getLocation());
        		
        		if (r != null){
        			if (RedProtect.ph.hasRegionPermLeader(player, "deltp", r)){
        				r.setTPPoint(null);
            			RPLang.sendMessage(player, "cmdmanager.region.settp.removed");
            			return true;
            		} else {
            			RPLang.sendMessage(player, "playerlistener.region.cantuse");
        				return true;    
            		}
        		} else {
    				RPLang.sendMessage(player, "cmdmanager.region.todo.that");
    				return true;    
        		}
        	}
        	
        	if (checkCmd(args[0], "border") && RedProtect.ph.hasGenPerm(player, "border")){
        		Region r = RedProtect.rm.getTopRegion(player.getLocation());
        		if (r != null){
        			RPUtil.addBorder(player, r.get4Points(player.getLocation().getBlockY()));
        			return true;
        		} else {
    				RPLang.sendMessage(player, "cmdmanager.region.todo.that");
    				return true;    
        		}
        	}
        	
        	if (checkCmd(args[0], "cancelbuy") && player.hasPermission("redprotect.eco.cancelbuy")){
        		if (!RedProtect.Vault){
        			return true;
        		}
        		Region r = RedProtect.rm.getTopRegion(player.getLocation());
        		if (r == null){
        			RPLang.sendMessage(player, "cmdmanager.region.todo.that");
    				return true;
        		}
        		
        		if (r.isLeader(RPUtil.PlayerToUUID(player.getName()))){
        			if (r.isForSale()){
            			r.removeFlag("for-sale");        			
            			r.setWelcome("");
            			if (r.leaderSize() == 0){
            				if (RPConfig.getEcoBool("rename-region")){
            					RedProtect.rm.renameRegion(RPUtil.nameGen(player.getName(),r.getWorld()), r);
            				}            				        		
            				r.addLeader(RPUtil.PlayerToUUID(player.getName()));
            			} else {
            				if (RPConfig.getEcoBool("rename-region")){
            					RedProtect.rm.renameRegion(RPUtil.nameGen(RPUtil.UUIDtoPlayer(r.getLeaders().get(0)),r.getWorld()),r);
            				}
            			}        			
            			RPLang.sendMessage(player, "economy.region.cancelbuy");
            			RedProtect.logger.addLog("(World "+r.getWorld()+") Player "+player.getName()+" cancelled buy stat of region "+r.getName());
        				return true;
            		} else {
            			RPLang.sendMessage(player, "economy.region.buy.notforsale");
            			return true;
            		}
        		} else {
        			RPLang.sendMessage(player, "economy.region.sell.own");
        			return true;
        		}
        	}
        	        	
        	if (checkCmd(args[0], "value") && RedProtect.ph.hasGenPerm(player, "value")){
        		Region r = RedProtect.rm.getTopRegion(player.getLocation());
        		if (r != null){
        			if (RedProtect.ph.hasRegionPermLeader(player, "value", r)){
        				if (r.getArea() < RPConfig.getEcoInt("max-area-toget-value")){
            				r.setValue(RPEconomy.getRegionValue(r));
            				RPLang.sendMessage(player, RPLang.get("cmdmanager.value.is").replace("{value}", RPEconomy.getFormatted(r.getValue()) + " " +RPConfig.getEcoString("economy-name")));
            				
            				RedProtect.logger.debug("Region Value: "+r.getValue());
                			return true;
            			} else {
            				RPLang.sendMessage(player, RPLang.get("cmdmanager.value.areabig").replace("{maxarea}", RPConfig.getEcoInt("max-area-toget-value").toString()));
            				return true;
            			}
        			} else {
        				RPLang.sendMessage(player, "playerlistener.region.cantuse");
        				return true;  
        			}        			
        		} else {
    				RPLang.sendMessage(player, "cmdmanager.region.todo.that");
    				return true;
    			} 
        	}        	
        	
        	if (args[0].equalsIgnoreCase("save-all")) {   
        		if (player.hasPermission("redprotect.admin.save-all")) {
        			RedProtect.logger.SaveLogs();
        			RPLang.sendMessage(player,ChatColor.GREEN + "" + RedProtect.rm.saveAll() + " regions saved with success!");
        			return true;
        		}    			            		       		
        	}
        	if (args[0].equalsIgnoreCase("load-all")) {   
        		if (player.hasPermission("redprotect.admin.load-all")) {
        			RedProtect.rm.clearDB();
        			try {
    					RedProtect.rm.loadAll();
    				} catch (Exception e) {
    					RPLang.sendMessage(player, "Error on load all regions from database files:");
    					e.printStackTrace();
    				}
        			RPLang.sendMessage(player,ChatColor.GREEN + "" + RedProtect.rm.getAllRegions().size() + " regions has been loaded from database files!");  
            		return true;
        		}    			
        	}
        	if (checkCmd(args[0], "define")){
        		if (!RedProtect.ph.hasGenPerm(player, "define")) {
                    RPLang.sendMessage(player, "no.permission");
                    return true;
                }
        		String serverName = RPConfig.getString("region-settings.default-leader");
                String name = RPUtil.nameGen(serverName, player.getWorld().getName());
                
                RegionBuilder rb2 = new DefineRegionBuilder(player, RedProtect.firstLocationSelections.get(player), RedProtect.secondLocationSelections.get(player), name, serverName, new ArrayList<String>(), true);
                if (rb2.ready()) {
                    Region r2 = rb2.build();
                    RPLang.sendMessage(player,RPLang.get("cmdmanager.region.created") + " " + r2.getName() + ".");
                    RedProtect.rm.add(r2, player.getWorld());
                    
                    RedProtect.firstLocationSelections.remove(player);
                    RedProtect.secondLocationSelections.remove(player);
                    
                    RedProtect.logger.addLog("(World "+r2.getWorld()+") Player "+player.getName()+" DEFINED region "+r2.getName());
                }
                return true;
        	}
        	
        	//rp claim
        	if (checkCmd(args[0], "claim")){
        		if ((!claimmode.equalsIgnoreCase("WAND") && !claimmode.equalsIgnoreCase("BOTH")) && !player.hasPermission("redprotect.admin.claim")) {
                    RPLang.sendMessage(player, "blocklistener.region.blockmode");
                    return true;
                }
                String name = RPUtil.nameGen(player.getName(), player.getWorld().getName());
                String leader = player.getUniqueId().toString();
                if (!RedProtect.OnlineMode){
                	leader = player.getName().toLowerCase();
            	}
                RegionBuilder rb2 = new DefineRegionBuilder(player, RedProtect.firstLocationSelections.get(player), RedProtect.secondLocationSelections.get(player), name, leader, new ArrayList<String>(), false);
                if (rb2.ready()) {
                    Region r2 = rb2.build();
                    RPLang.sendMessage(player,RPLang.get("cmdmanager.region.created") + " " + r2.getName() + ".");
                    RedProtect.rm.add(r2, player.getWorld());

                    RedProtect.firstLocationSelections.remove(player);
                    RedProtect.secondLocationSelections.remove(player);
                    
                    RedProtect.logger.addLog("(World "+r2.getWorld()+") Player "+player.getName()+" CLAIMED region "+r2.getName());
                }
                return true;
        	}
        	
        	if (args[0].equalsIgnoreCase("mychunkconvert") && player.isOp()) {
        		if (handleMyChunk()){
        			RPLang.sendMessage(player,ChatColor.GREEN + "...converting" + ChatColor.AQUA + " MyChunk " + ChatColor.GREEN + "database!");
        			RPLang.sendMessage(player,ChatColor.GOLD + "See all the process on console!");
            		return true;
        		} else {
        			RPLang.sendMessage(player,ChatColor.RED + "The plugin MyChunk is not installed or no regions found");
        			return true;
        		}        		
        	}
        	if (checkCmd(args[0], "reload") && player.hasPermission("redprotect.admin.reload")) {
        		RedProtect.plugin.getServer().getPluginManager().disablePlugin((Plugin)RedProtect.plugin);
    			RedProtect.plugin.getServer().getPluginManager().enablePlugin((Plugin)RedProtect.plugin);
        		RPLang.sendMessage(player, "cmdmanager.reloaded");
        		return true;
        	}
        	
        	if (args[0].equalsIgnoreCase("reload-config") && player.hasPermission("redprotect.admin.reload")) {
    			RPConfig.init();
    			RPLang.init();
    			RPLang.sendMessage(player, "cmdmanager.reloaded");
    			return true;
    		}
        	
        	if (checkCmd(args[0], "wand") && player.hasPermission("redprotect.magicwand")) {
        		Inventory inv = player.getInventory();
        		Material mat = Material.getMaterial(RPConfig.getInt("wands.adminWandID"));
        		ItemStack item = new ItemStack(mat);                
                if (!inv.contains(mat) && inv.firstEmpty() != -1){                	
                	inv.addItem(item);
            		RPLang.sendMessage(player,RPLang.get("cmdmanager.wand.given").replace("{item}", item.getType().name()));
                } else {
                	RPLang.sendMessage(player,RPLang.get("cmdmanager.wand.nospace").replace("{item}", item.getType().name()));
                }
        		return true;
        	}
        	
            if (checkCmd(args[0], "help")) {
                HandleHelPage(sender, 1);
                return true;
            }          
            
            if (checkCmd(args[0], "tutorial")) {
                RPLang.sendMessage(player,"cmdmanager.tutorial");
                RPLang.sendMessage(player,"cmdmanager.tutorial1");
                RPLang.sendMessage(player,"cmdmanager.tutorial2");
                RPLang.sendMessage(player,"cmdmanager.tutorial3");
                RPLang.sendMessage(player,"cmdmanager.tutorial4");
                RPLang.sendMessage(player,"cmdmanager.tutorial5");
                RPLang.sendMessage(player,"cmdmanager.tutorial6");
                return true;
            }
            
            if (checkCmd(args[0], "near")) {
                if (RedProtect.ph.hasPerm(player, "redprotect.near")) {
                    Set<Region> regions = RedProtect.rm.getRegionsNear(player, 60, player.getWorld());
                    if (regions.size() == 0) {
                        RPLang.sendMessage(player, "cmdmanager.noregions.nearby");
                    }
                    else {
                        Iterator<Region> i = regions.iterator();
                        RPLang.sendMessage(player,RPLang.get("general.color") + "------------------------------------");
                        RPLang.sendMessage(player,RPLang.get("cmdmanager.regionwith40"));
                        RPLang.sendMessage(player,RPLang.get("general.color") + "------------------------------------");
                        while (i.hasNext()) {
                            Region r = i.next();
                            player.sendMessage(RPLang.get("cmdmanager.region.name") + r.getName() + RPLang.get("general.color") + ChatColor.translateAlternateColorCodes('&', " | Center (&6X,Z"+RPLang.get("general.color")+"): &6") +  r.getCenterX() + ", "  + r.getCenterZ());
                            
                        }
                        RPLang.sendMessage(player,RPLang.get("general.color") + "------------------------------------");
                    }
                }
                else {
                    RPLang.sendMessage(player, "no.permission");
                }
                return true;
            }
            
            if (checkCmd(args[0], "flag")) {
            	if (RedProtect.ph.hasGenPerm(player, "flaggui")) {
        			Region r = RedProtect.rm.getTopRegion(player.getLocation());
        			if (r != null){
        				if (RedProtect.ph.hasRegionPermAdmin(player, "flaggui", r)){
        					RPGui gui = new RPGui(RPUtil.getTitleName(r), player, r, RedProtect.plugin, false, RPConfig.getGuiMaxSlot());        					
        					gui.open();
                			return true;
        				} else {
        					sendNoPermissionMessage(player);
        					return true;
        				}
        			} else {
        				RPLang.sendMessage(player, "cmdmanager.region.todo.that");
        				return true;
        			}    
        		} 
            }
        }
        
        if (args.length == 2) {      
        	
        	//rp del-rent <player>
        	if (checkCmd(args[0], "del-rent") && player.hasPermission("redprotect.del-rent") && RedProtect.Vault){
        		Region r = RedProtect.rm.getTopRegion(player.getLocation());
        		if (r == null){
        			RPLang.sendMessage(player, "cmdmanager.region.todo.that");
    				return true;
        		}
        		
        		if (!r.isLeader(player) && !player.hasPermission("redprotect.rent.others")){
        			sendNoPermissionMessage(player);
        			return true;
        		}
        		
        		if (r.isRentFor(RPUtil.PlayerToUUID(args[1]))){
        			r.removeRent(RPUtil.PlayerToUUID(args[1]));
        			RPLang.sendMessage(player, RPLang.get("cmdmanager.rent.playerremoved").replace("{region}", r.getName()));
        		} else {
        			RPLang.sendMessage(player, RPLang.get("cmdmanager.rent.noplayerrented"));
        		}
        		return true;
        	}
        	
        	//rp removeall <player>
    		if (checkCmd(args[0], "removeall") && sender.hasPermission("redprotect.removeall")) {
    			if (!RedProtect.WE){
    				return true;
    			}
    			int removed = RedProtect.rm.removeAll(args[1]);
    			if (removed <= 0){
    				RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.noneremoved"));
    			} else {
    				RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.removed").replace("{regions}", removed+"").replace("{player}", args[1]));
    			}        			
    			return true;
    		}
    		
    		//rp regenall <player>
    		if (checkCmd(args[0], "regenall") && sender.hasPermission("redprotect.regenall")) {
    			if (!RedProtect.WE){
    				return true;
    			}
    			int regen = RedProtect.rm.regenAll(args[1]);
    			if (regen <= 0){
    				RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.noneregenerated"));
    			} else {
    				RPLang.sendMessage(sender, RPLang.get("cmdmanager.region.regenerated").replace("{regions}", regen+"").replace("{player}", args[1]));
    			}
    			return true;
    		}
    		
        	//rp regen stop
    		if (args[0].equalsIgnoreCase("regen") && args[1].equalsIgnoreCase("stop") && player.hasPermission("redprotect.regen")) {
    			if (!RedProtect.WE){
    				return true;
    			}
    			RPUtil.stopRegen = true;
    			RPLang.sendMessage(player, "&aRegen will stop now. To continue reload the plugin!");
    			return true;
    		}
    		
        	if (checkCmd(args[0], "flag") && args[1].equalsIgnoreCase("gui-edit")) {
        		if (player.hasPermission("redprotect.gui.edit")){
        			Region r = RedProtect.rm.getTopRegion(player.getLocation());
        			if (r != null){
        				RPGui gui = new RPGui(RPLang.get("gui.editflag"), player, r, RedProtect.plugin, true, RPConfig.getGuiMaxSlot());
    					gui.open();
        			} else {
        				RPLang.sendMessage(player, "cmdmanager.region.todo.that");
        			}
        			return true;
        		}        		
        	}
        	
        	if (checkCmd(args[0], "help")) {
        		try{
        			int page = Integer.parseInt(args[1]);
                    HandleHelPage(sender, page);
        		} catch (NumberFormatException e){
        			RPLang.sendMessage(player,RPLang.get("correct.usage") + " " + ChatColor.DARK_AQUA + "/rp "+getCmd("help")+ "[page]");
        		}
                return true;
            }        	
        	        	
        	if (checkCmd(args[0], "define")){
        		if (!RedProtect.ph.hasGenPerm(player, "define")) {
                    RPLang.sendMessage(player, "no.permission");
                    return true;
                }
        		String serverName = RPConfig.getString("region-settings.default-leader");
                String name = args[1].replace("/", "|");
                
                RegionBuilder rb2 = new DefineRegionBuilder(player, RedProtect.firstLocationSelections.get(player), RedProtect.secondLocationSelections.get(player), name, serverName, new ArrayList<String>(), true);
                if (rb2.ready()) {
                    Region r2 = rb2.build();
                    RPLang.sendMessage(player,RPLang.get("cmdmanager.region.created") + " " + r2.getName() + ".");
                    RedProtect.rm.add(r2, player.getWorld());

                    RedProtect.firstLocationSelections.remove(player);
                    RedProtect.secondLocationSelections.remove(player);
                    
                    RedProtect.logger.addLog("(World "+r2.getWorld()+") Player "+player.getName()+" DEFINED region "+r2.getName());
                }
                return true;
        	}
        	
        	//rp claim [nameOfRegion]
        	if (checkCmd(args[0], "claim")){
        		String claimmode = RPConfig.getWorldClaimType(player.getWorld().getName());
        		if ((!claimmode.equalsIgnoreCase("WAND") && !claimmode.equalsIgnoreCase("BOTH")) && !player.hasPermission("redprotect.admin.claim")) {
                    RPLang.sendMessage(player, "blocklistener.region.blockmode");
                    return true;
                }
                String name = args[1].replace("/", "|");
                String leader = player.getUniqueId().toString();
                if (!RedProtect.OnlineMode){
                	leader = player.getName().toLowerCase();
            	}
                RegionBuilder rb2 = new DefineRegionBuilder(player, RedProtect.firstLocationSelections.get(player), RedProtect.secondLocationSelections.get(player), name, leader, new ArrayList<String>(), false);
                if (rb2.ready()) {
                    Region r2 = rb2.build();
                    RPLang.sendMessage(player,RPLang.get("cmdmanager.region.created") + " " + r2.getName() + ".");
                    RedProtect.rm.add(r2, player.getWorld());

                    RedProtect.firstLocationSelections.remove(player);
                    RedProtect.secondLocationSelections.remove(player);
                    
                    RedProtect.logger.addLog("(World "+r2.getWorld()+") Player "+player.getName()+" CLAIMED region "+r2.getName());
                }
                return true;
        	}
        	
            if (checkCmd(args[0], "redefine")) {                                
                Region oldRect = RedProtect.rm.getRegion(args[1], player.getWorld());
                if (oldRect == null) {
                    RPLang.sendMessage(player, RPLang.get("cmdmanager.region.doesntexist") + ": " + args[1]);
                    return true;
                }
                
                if (!RedProtect.ph.hasRegionPermLeader(player, "redefine", oldRect)) {
                    RPLang.sendMessage(player, "no.permission");
                    return true;
                }
                                
                RedefineRegionBuilder rb = new RedefineRegionBuilder(player, oldRect, RedProtect.firstLocationSelections.get(player), RedProtect.secondLocationSelections.get(player));
                if (rb.ready()) {
                    Region r2 = rb.build();
                    RPLang.sendMessage(player,RPLang.get("cmdmanager.region.redefined") + " " + r2.getName() + ".");
                    RedProtect.rm.add(r2, player.getWorld());
                    
                    RedProtect.firstLocationSelections.remove(player);
                    RedProtect.secondLocationSelections.remove(player);
                                        
                    RedProtect.logger.addLog("(World "+r2.getWorld()+") Player "+player.getName()+" REDEFINED region "+r2.getName());
                }
                return true;
            }
                        
            if  (args[0].equalsIgnoreCase("setconfig") && args[1].equalsIgnoreCase("list")){
        		if (!player.hasPermission("redprotect.admin.setconfig")) {
                    RPLang.sendMessage(player, "no.permission");
                    return true;
                }
        		
    			RPLang.sendMessage(player,ChatColor.AQUA + "=========== Config Sections: ===========");
        		for (String section:RedProtect.plugin.getConfig().getValues(false).keySet()){
        			if (section.equals("debug-messages") || 
        					section.equals("file-type") ||
        					section.equals("language")){
        				RPLang.sendMessage(player,ChatColor.GOLD + section + " : " + ChatColor.GREEN + RedProtect.plugin.getConfig().get(section).toString());
        			}         			
        		} 
        		RPLang.sendMessage(player,ChatColor.AQUA + "====================================");
        		return true;
            }
        }
        
        if (args.length == 3) { 
        	
        	//rp edit-rent <player> <valor/date>
        	if (checkCmd(args[0], "edit-rent") && player.hasPermission("redprotect.edit-rent") && RedProtect.Vault){
        		Region r = RedProtect.rm.getTopRegion(player.getLocation());
        		if (r == null){
        			RPLang.sendMessage(player, "cmdmanager.region.todo.that");
    				return true;
        		}
        		
        		if (!r.isLeader(player) && !player.hasPermission("redprotect.rent.others")){
        			sendNoPermissionMessage(player);
        			return true;
        		}
        		
        		if (!r.isRentFor(RPUtil.PlayerToUUID(args[1]))){
        			RPLang.sendMessage(player, "cmdmanager.rent.playernotrented");
        			return true;
        		}
        		        		
        		try {  			
        			int value = Integer.valueOf(args[2]);
        			r.setRent(RPUtil.PlayerToUUID(args[1]), value);
        			RPLang.sendMessage(player, RPLang.get("cmdmanager.rent.renteditok").replace("{player}", args[1]).replace("{value}", args[2]));
        			return true;
        		} catch (Exception ex){
        			Calendar cal = Calendar.getInstance();
        			SimpleDateFormat sdf = new SimpleDateFormat(RPConfig.getString("region-settings.date-format"));
        			try {
						cal.setTime(sdf.parse(args[2]));						
						r.setRent(RPUtil.PlayerToUUID(args[1]), cal.getTimeInMillis());
						RPLang.sendMessage(player, RPLang.get("cmdmanager.rent.renteditok").replace("{player}", args[1]).replace("{value}", args[2]));
						return true;
					} catch (ParseException e) {						
					}   
        		}       		
        	}
        	
        	//rp regen <region> <world>
    		if (args[0].equalsIgnoreCase("regen") && player.hasPermission("redprotect.regen")) {
    			if (!RedProtect.WE){
    				return true;
    			}
    			World w = RedProtect.serv.getWorld(args[2]);
    			if (w == null){
                	sender.sendMessage(RPLang.get("cmdmanager.region.invalidworld"));
                	return true;
                }
    			Region r = RedProtect.rm.getRegion(args[1], w);
    			if (r == null){
    				sender.sendMessage(RPLang.get("correct.usage") + " " + ChatColor.YELLOW + "Invalid region: " + args[1]);
    				return true;
    			}
    			
    			if (RedProtect.AWE && RPConfig.getBool("hooks.asyncworldedit.use-for-regen")){
    				AWEListener.regenRegion(r, Bukkit.getWorld(r.getWorld()), r.getMaxLocation(), r.getMinLocation(), 0,sender, false);        				
    			} else {
    				WEListener.regenRegion(r, Bukkit.getWorld(r.getWorld()), r.getMaxLocation(), r.getMinLocation(), 0, sender, false);
    			}   			
    			return true;
    		}
    		
    		//rp undo <region> <world>
    		if (args[0].equalsIgnoreCase("undo") && player.hasPermission("redprotect.regen")) {
    			if (!RedProtect.WE){
    				return true;
    			}
    			World w = RedProtect.serv.getWorld(args[2]);
    			if (w == null){
                	sender.sendMessage(RPLang.get("cmdmanager.region.invalidworld"));
                	return true;
                }
    			Region r = RedProtect.rm.getRegion(args[1], w);
    			if (r == null){
    				sender.sendMessage(RPLang.get("correct.usage") + " " + ChatColor.YELLOW + "Invalid region: " + args[1]);
    				return true;
    			}
    			
    			if (RedProtect.AWE && RPConfig.getBool("hooks.asyncworldedit.use-for-regen")){
    				if (AWEListener.undo(r.getID())){
    					RPLang.sendMessage(sender, RPLang.get("cmdmanager.regen.undo.sucess").replace("{region}", r.getName()));
    				} else {
    					RPLang.sendMessage(sender, RPLang.get("cmdmanager.regen.undo.none").replace("{region}", r.getName()));
    				}        				      				
    			} else {
                    if (WEListener.undo(r.getID())){
                    	RPLang.sendMessage(sender, RPLang.get("cmdmanager.regen.undo.sucess").replace("{region}", r.getName()));
    				} else {
    					RPLang.sendMessage(sender, RPLang.get("cmdmanager.regen.undo.none").replace("{region}", r.getName()));
    				} 
    			}        			
    			return true;
    		}
    		
        	if (checkCmd(args[0], "flag") && args[1].equalsIgnoreCase("gui-edit")) {
        		if (player.hasPermission("redprotect.gui.edit")){
        			int MaxSlot = 0;
        			try{
        				MaxSlot = 9*Integer.parseInt(args[2]);
        				if (MaxSlot > 54 || MaxSlot < RPConfig.getGuiMaxSlot()){
        					RPLang.sendMessage(player, "gui.edit.invalid-lines");
        					return true;
        				}
        			} catch(NumberFormatException e){
        				RPLang.sendMessage(player, "cmdmanager.region.invalid.number");
        				return true;
        			}
        			Region r = RedProtect.rm.getTopRegion(player.getLocation());
        			if (r != null){
        				RPGui gui = new RPGui(RPLang.get("gui.editflag"), player, r, RedProtect.plugin, true, MaxSlot);
    					gui.open();
        			} else {
        				RPLang.sendMessage(player, "cmdmanager.region.todo.that");
        			}
        			return true;
        		}        		
        	}
        	
        	//rp claim [regionName] [leader]
        	if (checkCmd(args[0], "claim")){
        		String claimmode = RPConfig.getWorldClaimType(player.getWorld().getName());
        		if ((!claimmode.equalsIgnoreCase("WAND") && !claimmode.equalsIgnoreCase("BOTH")) && !player.hasPermission("redprotect.admin.claim")) {
                    RPLang.sendMessage(player, "blocklistener.region.blockmode");
                    return true;
                }
                String name = args[1].replace("/", "|");
                String leader = player.getUniqueId().toString();
                List<String> addedAdmins = new ArrayList<String>();
                addedAdmins.add(RPUtil.PlayerToUUID(args[2]));
                if (!RedProtect.OnlineMode){
                	leader = player.getName().toLowerCase();
            	}                
                RegionBuilder rb2 = new DefineRegionBuilder(player, RedProtect.firstLocationSelections.get(player), RedProtect.secondLocationSelections.get(player), name, leader, addedAdmins, false);
                if (rb2.ready()) {
                    Region r2 = rb2.build();
                    RPLang.sendMessage(player,RPLang.get("cmdmanager.region.created") + " " + r2.getName() + ".");
                    RedProtect.rm.add(r2, player.getWorld());

                    RedProtect.firstLocationSelections.remove(player);
                    RedProtect.secondLocationSelections.remove(player);
                    
                    RedProtect.logger.addLog("(World "+r2.getWorld()+") Player "+player.getName()+" CLAIMED region "+r2.getName());
                }
                return true;
        	}
        	
        	// - /rp copyflag from to
    		if  (checkCmd(args[0], "copyflag")){
    			if (!RedProtect.ph.hasGenPerm(player, "copyflag")) {
                    RPLang.sendMessage(player, "no.permission");
                    return true;
                }
    			World w = player.getWorld();
    			Region from = RedProtect.rm.getRegion(args[1], w);
    			Region to = RedProtect.rm.getRegion(args[2], w);
    			if (from == null){    				
    				RPLang.sendMessage(player,RPLang.get("cmdmanager.region.doesntexist") + ": " + args[1]);
    				return true;
    			}
    			if (to == null){    				
    				RPLang.sendMessage(player,RPLang.get("cmdmanager.region.doesntexist") + ": " + args[2]);
    				return true;
    			}
    			for (String key:from.flags.keySet()){
        			to.setFlag(key, from.flags.get(key));
    			}
    			RPLang.sendMessage(player,RPLang.get("cmdmanager.region.flag.copied") + args[1] + " > " + args[2]);
    			RedProtect.logger.addLog("Player "+player.getName()+" Copied FLAGS from "+ args[1] + " to " + args[2]);
    			return true;
    		}
    		
        	if  (args[0].equalsIgnoreCase("setconfig")){
        		if (!player.hasPermission("redprotect.admin.setconfig")) {
                    RPLang.sendMessage(player, "no.permission");
                    return true;
                }
        		if (RedProtect.plugin.getConfig().contains(args[1])){
        			Object from = RedProtect.plugin.getConfig().get(args[1]); 
        			if (args[2].equals("true") || args[2].equals("false")){
        				RedProtect.plugin.getConfig().set(args[1], Boolean.parseBoolean(args[2]));
        			} else {
        				try {
            				int value = Integer.parseInt(args[2]);
            				RedProtect.plugin.getConfig().set(args[1], value);
            		    } catch(NumberFormatException ex){
            		    	RedProtect.plugin.getConfig().set(args[1], args[2]);
            		    }
        			}
        			RPLang.sendMessage(player,RPLang.get("cmdmanager.configset") + " " + from.toString() + " > " + args[2]);
        			RPConfig.save();
        			return true;
        		} else {
        			RPLang.sendMessage(player,RPLang.get("cmdmanager.confignotset") + " " + args[1]);
        			return true;
        		}
        	}
        }   
        
        if (args.length == 4 || args.length == 5){
        	
        	//rp add-rent <player> <valor> <date>
        	if (checkCmd(args[0], "add-rent") && player.hasPermission("redprotect.add-rent") && RedProtect.Vault){
        		Region r = RedProtect.rm.getTopRegion(player.getLocation());
        		if (r == null){
        			RPLang.sendMessage(player, "cmdmanager.region.todo.that");
    				return true;
        		}
        		
        		if (!r.isLeader(player) && !player.hasPermission("redprotect.rent.others")){
        			sendNoPermissionMessage(player);
        			return true;
        		}
        		
        		int value = 0;
        		long renewal = 0;
        		try {
        			Calendar cal = Calendar.getInstance();
        			SimpleDateFormat sdf = new SimpleDateFormat(RPConfig.getString("region-settings.date-format"));
        			
        			cal.setTime(sdf.parse(args[3]));  
        			renewal = cal.getTimeInMillis();
        			value = Integer.valueOf(args[2]);
        		} catch (Exception ex){        			
        			RPLang.sendMessage(player,RPLang.get("correct.command") + " " + ChatColor.DARK_AQUA + "/rp "+getCmd("help"));   
        			ex.printStackTrace();
        			return true;
        		}
        		
        		String play = RPUtil.PlayerToUUID(args[1]);
        		if (args.length == 4){
        			r.addrent(play, value, renewal, RPConfig.getString("region-settings.rent.default-level"));
        		} else
        		if (args.length == 5){
        			if (args[4].equalsIgnoreCase("member") || args[4].equalsIgnoreCase("admin") || args[4].equalsIgnoreCase("leader")){
        				r.addrent(play, value, renewal, args[4]);
        			} else {
        				RPLang.sendMessage(player, RPLang.get("cmdmanager.rent.validranks").replace("{ranks}", "member, admin, leader"));
        				return true;
        			}			
        		} 
        		RPLang.sendMessage(player, RPLang.get("cmdmanager.rent.addedrent").replace("{player}", args[1]).replace("{date}", args[3]).replace("{cost}", args[2]));
        		if (RedProtect.serv.getPlayer(args[1]) != null && RedProtect.serv.getPlayer(args[1]).isOnline()){
        			RPLang.sendMessage(RedProtect.serv.getPlayer(args[1]), RPLang.get("cmdmanager.rent.playeraddedrent").replace("{region}", r.getName()).replace("{date}", args[3]).replace("{cost}", args[2]));
        		}
        		return true;
        	}
        }
        
        //rp kick <player> [region] [world]
        if (checkCmd(args[0], "kick")){
        	if (args.length == 2 || args.length == 4){        		
            	Region r = RedProtect.rm.getTopRegion(player.getLocation()); 
            	
				if (r == null){
	    			RPLang.sendMessage(player, "cmdmanager.region.todo.that");
					return true;
	    		}
				
    			if (args.length == 4){
    				r = RedProtect.rm.getRegion(args[2], args[3]); 
    				if (r == null){
    	    			RPLang.sendMessage(player, "cmdmanager.region.todo.that");
    					return true;
    	    		}    							
    			} 
    			
    			if (!RedProtect.ph.hasRegionPermMember(player, "kick", r)) {
	                RPLang.sendMessage(player, "no.permission");
	                return true;
	            }	
    			
    			Player visit = Bukkit.getPlayer(args[1]);
				if (visit == null){
					RPLang.sendMessage(player, RPLang.get("cmdmanager.noplayer.thisname").replace("{player}", args[1]));
					return true;
				}	
        		
        		if (r.canBuild(visit)){
    				RPLang.sendMessage(player, "cmdmanager.cantkick.member");
    				return true;
    			}
        		
    			Region rv = RedProtect.rm.getTopRegion(visit.getLocation());
    			if (rv == null || rv != null && !rv.getID().equals(r.getID())){
    				RPLang.sendMessage(player, "cmdmanager.noplayer.thisregion");
    				return true;
    			}
    			
    			if (RedProtect.SC && SCHook.inWar(r, player, visit)){
    				RPLang.sendMessage(player, "cmdmanager.cantkick.war");
        			return true;
        		}
    			
    			String sec = String.valueOf(RPConfig.getInt("region-settings.delay-after-kick-region"));
    			if (RedProtect.plugin.denyEnterRegion(r.getID(), visit.getName())){
    				RPUtil.DenyEnterPlayer(visit.getWorld(), visit.getLocation(), visit.getLocation(), visit, r, true);
    				RPLang.sendMessage(player, RPLang.get("cmdmanager.region.kicked").replace("{player}", args[1]).replace("{region}", r.getName()).replace("{time}", sec));
    			} else {
    				RPLang.sendMessage(player, RPLang.get("cmdmanager.already.cantenter").replace("{time}", sec));
    			}
    			return true;
        	}			
        }
        
        //rp expand-vert [region] [world]
        if (checkCmd(args[0], "expand-vert")){
    		if (!RedProtect.ph.hasGenPerm(player, "expandvert")) {
                RPLang.sendMessage(player, "no.permission");
                return true;
            }
    		Region r = null;
    		//rp expand-vert
    		if (args.length == 1){
    			r = RedProtect.rm.getTopRegion(player.getLocation());
    			if (r == null){
        			RPLang.sendMessage(player, "cmdmanager.region.todo.that");
    				return true;
        		}
    		} else 
    		//rp expand-vert [region]	
    		if (args.length == 2){
    			r = RedProtect.rm.getRegion(args[1], player.getWorld());
    			if (r == null){
        			RPLang.sendMessage(player, RPLang.get("cmdmanager.region.doesntexist") + ": " + args[1]);
    				return true;
        		}	
    		} else
    		//rp expand-vert [region] [world]
    		if (args.length == 3){
                if (Bukkit.getWorld(args[2]) == null){
                	RPLang.sendMessage(player, "cmdmanager.region.invalidworld");
                	return true;
    			}
    			r = RedProtect.rm.getRegion(args[1], Bukkit.getWorld(args[2])); 
    			if (r == null){
        			RPLang.sendMessage(player, RPLang.get("cmdmanager.region.doesntexist") + ": " + args[1]);
    				return true;
        		}	
    		} else {
    			RPLang.sendMessage(player, "cmdmanager.help.expandvert");
    			return true;
    		}
    		
    		r.setMaxY(player.getWorld().getMaxHeight());
    		r.setMinY(0);
    		RPLang.sendMessage(player, RPLang.get("cmdmanager.region.expandvert.success").replace("{region}", r.getName()).replace("{miny}", String.valueOf(r.getMinY())).replace("{maxy}", String.valueOf(r.getMaxY())));
    		return true;
    	}
        
        //rp setmaxy <size> [region] [world]
    	if (checkCmd(args[0], "setmaxy")){
    		if (!RedProtect.ph.hasGenPerm(player, "setmaxy")) {
                RPLang.sendMessage(player, "no.permission");
                return true;
            }
    		
    		Region r = null;
    		//rp setmaxy <size>
    		if (args.length == 2){
    			r = RedProtect.rm.getTopRegion(player.getLocation()); 
    			if (r == null){
        			RPLang.sendMessage(player, "cmdmanager.region.todo.that");
    				return true;
        		}
    		} else
    		//rp setmaxy <size> [region]
    		if (args.length == 3){
    			r = RedProtect.rm.getRegion(args[2], player.getWorld()); 
    			if (r == null){
        			RPLang.sendMessage(player, RPLang.get("cmdmanager.region.doesntexist") + ": " + args[2]);
    				return true;
        		}
    		} else
    		//rp setmaxy <size> [region] [world]
    		if (args.length == 4){
    			if (Bukkit.getWorld(args[3]) == null){
    				RPLang.sendMessage(player, "cmdmanager.region.invalidworld");
                	return true;
    			}
    			r = RedProtect.rm.getRegion(args[2], Bukkit.getWorld(args[3])); 
    			if (r == null){
        			RPLang.sendMessage(player, RPLang.get("cmdmanager.region.doesntexist") + ": " + args[2]);
    				return true;
        		}
    		} else {
    			RPLang.sendMessage(player, "cmdmanager.help.setmaxy");
    			return true;
    		}    		
    		
    		String from = String.valueOf(r.getMaxY());
    		
    		try{
    			int size = Integer.parseInt(args[1]);
    			if ((size - r.getMinY()) <= 1){
        			RPLang.sendMessage(player, "cmdmanager.region.ysiszesmatch");
        			return true;
        		}
    			r.setMaxY(size);
    			RPLang.sendMessage(player, RPLang.get("cmdmanager.region.setmaxy.success").replace("{region}", r.getName()).replace("{fromsize}", from).replace("{size}", String.valueOf(size)));
    			RedProtect.logger.addLog("(World "+r.getWorld()+") Player "+player.getName()+" SETMAXY of region "+r.getName()+" to "+args[1]);
    			return true;
    		} catch (NumberFormatException e){
    			RPLang.sendMessage(player, "cmdmanager.region.invalid.number");
    			return true;
    		}
    	}
    	
    	//rp setmaxy <size> [region] [world]
    	if (checkCmd(args[0], "setminy")){
    		if (!RedProtect.ph.hasGenPerm(player, "setminy")) {
                RPLang.sendMessage(player, "no.permission");
                return true;
            }
    		
    		Region r = null;
    		//rp setmaxy <size>
    		if (args.length == 2){
    			r = RedProtect.rm.getTopRegion(player.getLocation()); 
    			if (r == null){
        			RPLang.sendMessage(player, "cmdmanager.region.todo.that");
    				return true;
        		}
    		} else
    		//rp setmaxy <size> [region]
    		if (args.length == 3){
    			r = RedProtect.rm.getRegion(args[2], player.getWorld()); 
    			if (r == null){
        			RPLang.sendMessage(player, RPLang.get("cmdmanager.region.doesntexist") + ": " + args[2]);
    				return true;
        		}
    		} else
    		//rp setmaxy <size> [region] [world]
    		if (args.length == 4){
    			if (Bukkit.getWorld(args[3]) == null){
    				RPLang.sendMessage(player, "cmdmanager.region.invalidworld");
                	return true;
    			}
    			r = RedProtect.rm.getRegion(args[2], Bukkit.getWorld(args[3])); 
    			if (r == null){
        			RPLang.sendMessage(player, RPLang.get("cmdmanager.region.doesntexist") + ": " + args[2]);
    				return true;
        		}
    		} else {
    			RPLang.sendMessage(player, "cmdmanager.help.setminy");
    			return true;
    		}
    		    		
    		String from = String.valueOf(r.getMinY());
    		try{
    			int size = Integer.parseInt(args[1]);
    			if ((r.getMaxY() - size) <= 1){
        			RPLang.sendMessage(player, "cmdmanager.region.ysiszesmatch");
        			return true;
        		}
    			r.setMinY(size);
        		RPLang.sendMessage(player, RPLang.get("cmdmanager.region.setminy.success").replace("{region}", r.getName()).replace("{fromsize}", from).replace("{size}", String.valueOf(size)));
        		RedProtect.logger.addLog("(World "+r.getWorld()+") Player "+player.getName()+" SETMINY of region "+r.getName()+" to "+args[1]);
        		return true;
    		} catch (NumberFormatException e){
    			RPLang.sendMessage(player, "cmdmanager.region.invalid.number");
    			return true;
    		}        		
    	}
        

    	if (checkCmd(args[0], "buy") && player.hasPermission("redprotect.eco.buy")){
    		if (!RedProtect.Vault){
    			return true;
    		}    		
    		Region r = RedProtect.rm.getTopRegion(player.getLocation());
        	if (r == null){
    			RPLang.sendMessage(player, "cmdmanager.region.todo.that");
    			return true;
    		}        	
        	if (!r.isForSale()){
    			RPLang.sendMessage(player, "economy.region.buy.notforsale");
    			return true;
    		} 
        	
    		if (args.length == 1){
    			buyHandler(player, r.getValue(), r);
    			RedProtect.logger.addLog("(World "+r.getWorld()+") Player "+player.getName()+" BUY region "+r.getName()+" for "+r.getValue());
				return true;    			
    		}    				    		
    	}
        
        if (checkCmd(args[0], "sell") && player.hasPermission("redprotect.eco.sell")){  
        	if (!RedProtect.Vault){
    			return true;
    		}        	
        	Region r = RedProtect.rm.getTopRegion(player.getLocation());
        	if (r == null){
    			RPLang.sendMessage(player, "cmdmanager.region.todo.that");
    			return true;
    		}        	
        	if (r.isForSale()){
    			RPLang.sendMessage(player, "economy.region.sell.already");
    			return true;
    		} 
        	
        	if (args.length == 1){
        		r.setValue(RPEconomy.getRegionValue(r));
        		if (r.isLeader(player)){
        			sellHandler(r, player, RPUtil.PlayerToUUID(player.getName()), r.getValue());
        		} else {
        			sellHandler(r, player, r.getLeaders().get(0), r.getValue());            		
        		}
        		RedProtect.logger.addLog("(World "+r.getWorld()+") Player "+player.getName()+" SELL region "+r.getName()+" for "+r.getValue());
        		return true;
        		
        	}        	
        	
        	if (args.length == 2){         
        		// rp sell <value/player>
        		try {
        			long value = Long.valueOf(args[1]);
    				if (player.hasPermission("redprotect.eco.setvalue")){
    					r.setValue(value);
    					if (r.isLeader(player)){
    	        			sellHandler(r, player, RPUtil.PlayerToUUID(player.getName()), r.getValue());
    	        		} else {
    	        			sellHandler(r, player, r.getLeaders().get(0), r.getValue());            		
    	        		}
    					RedProtect.logger.addLog("(World "+r.getWorld()+") Player "+player.getName()+" SELL region "+r.getName()+" for "+r.getValue());
    					return true;
    				}    				
    			} catch (NumberFormatException e){
    				if (player.hasPermission("redprotect.eco.others")){
    					r.setValue(RPEconomy.getRegionValue(r));
    					sellHandler(r, player, RPUtil.PlayerToUUID(args[1]), r.getValue());
    					RedProtect.logger.addLog("(World "+r.getWorld()+") Player "+player.getName()+" SELL region "+r.getName()+" in name of player "+args[1]+" for "+r.getValue());
            			return true;
                	}   				
    			}
        	} 
        	
        	if (args.length == 3){   
        		// rp sell player value
        		try {
        			long value = Long.valueOf(args[2]);
    				if (player.hasPermission("redprotect.eco.setvalue")){
    					r.setValue(value);
    					sellHandler(r, player, RPUtil.PlayerToUUID(args[1]), value);
    					RedProtect.logger.addLog("(World "+r.getWorld()+") Player "+player.getName()+" SELL region "+r.getName()+" in name of player "+args[1]+" for "+value);
    					return true;
    				}    				
    			} catch (NumberFormatException e){    
    				RPLang.sendMessage(player, "cmdmanager.eco.notdouble");
            		return true;
    			}
        	}
        }
        
        if (checkCmd(args[0], "teleport")) {
        	if (args.length == 1) {
        		RPLang.sendMessage(player, "cmdmanager.help.teleport");
        		return true;
        	}
        	
            if (args.length == 2) {
            	handletp(player, args[1], player.getWorld().getName(), null);
            	return true;
        	}

            if (args.length == 3) {
            	handletp(player, args[1], args[2], null);
            	return true;
            }
            
            if (args.length == 4) {
            	// /rp teleport <player> <region> <world>
            	Player play = RedProtect.serv.getPlayer(args[1]);
            	if (play != null){
            		handletp(player, args[2], args[3], play);
            		return true;
            	} else {
            		RPLang.sendMessage(player, RPLang.get("cmdmanager.noplayer.thisname").replace("{player}", args[1]));
            		RPLang.sendMessage(player, "cmdmanager.help.teleport");
            		return true;
            	}
            }
        }
        
        if (checkCmd(args[0], "limit")) {
            if (!RedProtect.ph.hasGenPerm(player, "limit")) {
                RPLang.sendMessage(player, "no.permission");
                return true;
            }
            
            if (args.length == 1) {
            	int limit = RedProtect.ph.getPlayerBlockLimit(player);
                if (limit < 0 || RedProtect.ph.hasPerm(player, "redprotect.limit.blocks.unlimited")) {
                    RPLang.sendMessage(player,"cmdmanager.nolimit");
                    return true;
                }
                String uuid = player.getUniqueId().toString();
                if (!RedProtect.OnlineMode){
                	uuid = player.getName().toLowerCase();
                }
                int currentUsed = RedProtect.rm.getTotalRegionSize(uuid, player.getWorld().getName());
                ChatColor color = currentUsed >= limit ? ChatColor.RED:ChatColor.GOLD;
                RPLang.sendMessage(player,RPLang.get("cmdmanager.yourarea") + color + currentUsed + RPLang.get("general.color") + "/" + color + limit + RPLang.get("general.color"));
                return true;
            }            

            if (!RedProtect.ph.hasPerm(player, "redprotect.other.limit")) {
                RPLang.sendMessage(player, "no.permission");
                return true;
            }
            
            if (args.length == 2) {          	
            	Player offp = RedProtect.serv.getOfflinePlayer(args[1]).getPlayer();
            	if (offp == null){
            		RPLang.sendMessage(player,RPLang.get("cmdmanager.noplayer.thisname").replace("{player}", args[1]));
            		return true;
            	}
            	int limit = RedProtect.ph.getPlayerBlockLimit(offp);
                if (limit < 0 || RedProtect.ph.hasPerm(offp, "redprotect.limit.blocks.unlimited")) {
                    RPLang.sendMessage(player, "cmdmanager.nolimit");
                    return true;
                }
                
                int currentUsed = RedProtect.rm.getTotalRegionSize(RPUtil.PlayerToUUID(offp.getName()), offp.getWorld().getName());
                ChatColor color = currentUsed >= limit ? ChatColor.RED:ChatColor.GOLD;
                RPLang.sendMessage(player,RPLang.get("cmdmanager.yourarea") + color + currentUsed + RPLang.get("general.color") + "/" + color + limit + RPLang.get("general.color"));
                return true;
            }
            RPLang.sendMessage(player,RPLang.get("correct.usage") + " " + RPLang.get("cmdmanager.help.limit"));
            return true;
        }        
        
        if (checkCmd(args[0], "claimlimit")) {
            if (!RedProtect.ph.hasGenPerm(player, "claimlimit")) {
                RPLang.sendMessage(player, "no.permission");
                return true;
            }
            
            if (args.length == 1) {
            	int limit = RedProtect.ph.getPlayerClaimLimit(player);
                if (limit < 0 || RedProtect.ph.hasPerm(player, "redprotect.claimunlimited")) {
                    RPLang.sendMessage(player,"cmdmanager.nolimit");
                    return true;
                }

                int currentUsed = RedProtect.rm.getRegions(RPUtil.PlayerToUUID(player.getName()), player.getWorld()).size();
                ChatColor color = currentUsed >= limit ? ChatColor.RED:ChatColor.GOLD;
                RPLang.sendMessage(player,RPLang.get("cmdmanager.yourclaims") + color + currentUsed + RPLang.get("general.color") + "/" + color + limit + RPLang.get("general.color"));
                return true;
            }            

            if (!RedProtect.ph.hasPerm(player, "redprotect.other.claimlimit")) {
                RPLang.sendMessage(player, "no.permission");
                return true;
            }
            
            if (args.length == 2) {          	
            	Player offp = RedProtect.serv.getOfflinePlayer(args[1]).getPlayer();
            	if (offp == null){
            		RPLang.sendMessage(player,RPLang.get("cmdmanager.noplayer.thisname").replace("{player}", args[1]));
            		return true;
            	}
            	int limit = RedProtect.ph.getPlayerClaimLimit(offp);
                if (limit < 0 || RedProtect.ph.hasPerm(offp, "redprotect.limit.claim.unlimited")) {
                    RPLang.sendMessage(player, "cmdmanager.nolimit");
                    return true;
                }
                
                int currentUsed = RedProtect.rm.getRegions(RPUtil.PlayerToUUID(offp.getName()), offp.getWorld()).size();
                ChatColor color = currentUsed >= limit ? ChatColor.RED:ChatColor.GOLD;
                RPLang.sendMessage(player,RPLang.get("cmdmanager.yourclaims") + color + currentUsed + RPLang.get("general.color") + "/" + color + limit + RPLang.get("general.color"));
                return true;
            }
            RPLang.sendMessage(player,RPLang.get("correct.usage") + " " + RPLang.get("cmdmanager.help.claimlimit"));
            return true;
        }      
        
        if (checkCmd(args[0], "welcome")) {
            if (args.length >= 2) {
            	String wMessage = "";
            	if (args[1].equals("off")){
            		handleWelcome(player, wMessage);
            		return true;
            	} else {
            		for (int i = 1; i < args.length; i++){
                		wMessage = wMessage+args[i]+" ";
                	}
                	handleWelcome(player, wMessage);
                    return true;
            	}            	
            }
            RPLang.sendMessage(player,RPLang.get("correct.usage") + " " + RPLang.get("cmdmanager.help.welcome"));
            return true;
        }         
        
        if (checkCmd(args[0], "priority")) {
        	int prior = 0;    	
    			
        	if (args.length == 2) {
        		try {
        			prior = Integer.parseInt(args[1]);
            	} catch (NumberFormatException e){ 
        			RPLang.sendMessage(player, "cmdmanager.region.notnumber");
        			return true; 
        		} 
        		handlePriority(player, prior);
                return true;                  
        	}
        	
            if (args.length == 3) {
            	try {
        			prior = Integer.parseInt(args[2]);
            	} catch (NumberFormatException e){ 
        			RPLang.sendMessage(player, "cmdmanager.region.notnumber");
        			return true; 
        		} 
        		handlePrioritySingle(player, prior, args[1]);
                return true;         
            }
            RPLang.sendMessage(player,RPLang.get("correct.usage") + " " + RPLang.get("cmdmanager.help.priority"));
            return true;
        }
        
        if (checkCmd(args[0], "delete")) {
        	//rp del [region] [world]
            if (args.length == 1) {
                handleDelete(player);
                return true;
            }
            if (args.length == 2) {
                handleDeleteName(player, args[1], "");
                return true;
            }
            if (args.length == 3) {
                handleDeleteName(player, args[1], args[2]);
                return true;
            }
            RPLang.sendMessage(player,RPLang.get("correct.usage") + " " + RPLang.get("cmdmanager.help.delete"));
            return true;
        }
        
        if (checkCmd(args[0], "info")) {
        	//rp info [region] [world]
            if (args.length == 1) {
                handleInfoTop(player);
                return true;
            }
            if (args.length == 2) {
                handleInfo(player, args[1], "");
                return true;
            }
            if (args.length == 3) {
                handleInfo(player, args[1], args[2]);
                return true;
            }
            RPLang.sendMessage(player,RPLang.get("correct.usage") + " " + RPLang.get("cmdmanager.help.info"));
            return true;
        }
        
        if (checkCmd(args[0], "addmember")) {
            if (args.length == 2) {
                handleAddMember(player, args[1]);
                return true;
            }
            RPLang.sendMessage(player,RPLang.get("correct.usage") + " " + RPLang.get("cmdmanager.help.addmember"));
            return true;
        }
        
        if (checkCmd(args[0], "addadmin")) {
            if (args.length == 2) {
                handleAddAdmin(player, args[1]);
                return true;
            }
            RPLang.sendMessage(player,RPLang.get("correct.usage") + " " + RPLang.get("cmdmanager.help.addadmin"));
            return true;
        }
        
        if (checkCmd(args[0], "addleader")) {
            if (args.length == 2) {
                handleAddLeader(player, args[1]);
                return true;
            }
            RPLang.sendMessage(player,RPLang.get("correct.usage") + " " + RPLang.get("cmdmanager.help.addleader"));
            return true;
        }
        
        if (checkCmd(args[0], "removemember")) {
            if (args.length == 2) {
                handleRemoveMember(player, args[1]);
                return true;
            }
            RPLang.sendMessage(player,RPLang.get("correct.usage") + " " + RPLang.get("cmdmanager.help.removemember"));
            return true;
        }
        
        if (checkCmd(args[0], "removeadmin")) {
            if (args.length == 2) {
                handleRemoveAdmin(player, args[1]);
                return true;
            }
            RPLang.sendMessage(player,RPLang.get("correct.usage") + " " + RPLang.get("cmdmanager.help.removeadmin"));
            return true;
        }
        
        if (checkCmd(args[0], "removeleader")) {
            if (args.length == 2) {
                handleRemoveLeader(player, args[1]);
                return true;
            }
            RPLang.sendMessage(player,RPLang.get("correct.usage") + " " + RPLang.get("cmdmanager.help.removeleader"));
            return true;
        }
        
        if (checkCmd(args[0], "rename")) {
            if (args.length == 2) {
                handleRename(player, args[1]);
                return true;
            }
            RPLang.sendMessage(player,RPLang.get("correct.usage") + " " + RPLang.get("cmdmanager.help.rename"));
            return true;
        }
        
        if (checkCmd(args[0], "flag")) {
        	Region r = RedProtect.rm.getTopRegion(player.getLocation());
        	if (r == null){
        		RPLang.sendMessage(player, "cmdmanager.region.todo.that");
        		return true;
        	}
        	
            if (args.length == 2) {            	
            	if (RPConfig.getBool("flags-configuration.change-flag-delay.enable")){
            		if (RPConfig.getStringList("flags-configuration.change-flag-delay.flags").contains(args[1])){
            			if (!RedProtect.changeWait.contains(r.getName()+args[1])){
            				RPUtil.startFlagChanger(r.getName(), args[1], player);
            				handleFlag(player, args[1], "", r);
            				return true;
            			} else {
            				RPLang.sendMessage(player,RPLang.get("gui.needwait.tochange").replace("{seconds}", RPConfig.getString("flags-configuration.change-flag-delay.seconds")));	
							return true;
            			}
            		}
            	}            	
                handleFlag(player, args[1], "", r);
                return true;
            }
            
            if (args.length >= 3) {
            	String text = "";
            	for (int i = 2; i < args.length; i++){
            		text = text + " " + args[i];
            	}            	
            	if (RPConfig.getBool("flags-configuration.change-flag-delay.enable")){
            		if (RPConfig.getStringList("flags-configuration.change-flag-delay.flags").contains(args[1])){
            			if (!RedProtect.changeWait.contains(r.getName()+args[1])){
            				RPUtil.startFlagChanger(r.getName(), args[1], player);
            				handleFlag(player, args[1], text.substring(1), r);
            				return true;
            			} else {
            				RPLang.sendMessage(player,RPLang.get("gui.needwait.tochange").replace("{seconds}", RPConfig.getString("flags-configuration.change-flag-delay.seconds")));	
							return true;
            			}
            		}
            	}             	
                handleFlag(player, args[1], text.substring(1), r);
                return true;
            }         
            
            RPLang.sendMessage(player,RPLang.get("correct.usage") + " " + RPLang.get("cmdmanager.help.flag"));
            return true;
        }
        
        if (checkCmd(args[0], "list")) {
        	//rp list
            if (args.length == 1) {
                handleList(player, RPUtil.PlayerToUUID(player.getName()), 1);
                return true;
            }
            //rp list [player]
            if (args.length == 2) {
                handleList(player, RPUtil.PlayerToUUID(args[1]), 1);
                return true;
            }   
            //rp list [player] [page]
            if (args.length == 3) {  
            	try{
                	int Page = Integer.parseInt(args[2]);                  
                    	handleList(player, RPUtil.PlayerToUUID(args[1]), Page);
                    	return true;
                	} catch(NumberFormatException  e){
                        RPLang.sendMessage(player, "cmdmanager.region.listpage.error");   
                        return true;
                }                 
            }
        }
        RPLang.sendMessage(player,RPLang.get("correct.command") + " " + ChatColor.DARK_AQUA + "/rp "+getCmd("help"));   
        return true;
    }

	private void buyHandler(Player player, long value, Region r) {		
		       		
		if (r.isLeader(RPUtil.PlayerToUUID(player.getName()))){
			RPLang.sendMessage(player, "economy.region.buy.own");
			return;
		}
		
		Double money = RedProtect.econ.getBalance(player);
		if (money >= value){
			String rname = r.getName();
			ArrayList<String> sellers = new ArrayList<>(r.getLeaders()); 
			if (RPEconomy.BuyRegion(r, RPUtil.PlayerToUUID(player.getName()))){
				RedProtect.econ.withdrawPlayer(player, value);				
				for (String seller : sellers){	
					OfflinePlayer offp = RedProtect.serv.getOfflinePlayer(RPUtil.UUIDtoPlayer(seller));
					if (!seller.equals(RPConfig.getString("region-settings.default-leader")) && offp != null){
						RedProtect.econ.depositPlayer(offp, value / sellers.size());
						if (offp.isOnline()){
							RPLang.sendMessage(offp.getPlayer(), RPLang.get("economy.region.buy.bought").replace("{player}", player.getName()).replace("{region}", rname).replace("{world}", r.getWorld()));
						}
					}
				}				
				RPLang.sendMessage(player, RPLang.get("economy.region.buy.success").replace("{region}", r.getName()).replace("{value}", String.valueOf(value)).replace("{ecosymbol}", RPConfig.getEcoString("economy-name")));
				return;
			} else {
				RPLang.sendMessage(player, "economy.region.error");
				return;
			}
		} else {
			RPLang.sendMessage(player, "economy.region.buy.nomoney");
			return;
		} 		
	}

	private void sellHandler(Region r, Player player, String leader, long value) {       		
		
		if (r.isLeader(player) || player.hasPermission("redprotect.eco.admin")){
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
    	Region r = RedProtect.rm.getRegion(region, p.getWorld());
    	if (RedProtect.ph.hasRegionPermLeader(p, "priority", r)) {
    		if (r != null){
    			r.setPrior(prior);
    			RPLang.sendMessage(p,RPLang.get("cmdmanager.region.priority.set").replace("{region}", r.getName()) + " " + prior);
    			RedProtect.logger.addLog("(World "+r.getWorld()+") Player "+p.getName()+" SET PRIORITY of region "+r.getName()+" to "+prior);
    		} else {
    			RPLang.sendMessage(p, "cmdmanager.region.todo.that");
        		return;
    		}
    	}
	}
	
    private static void handlePriority(Player p, int prior) {
    	Region r = RedProtect.rm.getTopRegion(p.getLocation());
    	if (RedProtect.ph.hasRegionPermLeader(p, "priority", r)) {
    		if (r != null){
    			r.setPrior(prior);
    			RPLang.sendMessage(p,RPLang.get("cmdmanager.region.priority.set").replace("{region}", r.getName()) + " " + prior);
    			RedProtect.logger.addLog("(World "+r.getWorld()+") Player "+p.getName()+" SET PRIORITY of region "+r.getName()+" to "+prior);
    		} else {
    			RPLang.sendMessage(p, "cmdmanager.region.todo.that");
        		return;
    		}
    	}		
	}

    private static void handleDelete(Player p) {
		Region r = RedProtect.rm.getTopRegion(p.getLocation());
        if (RedProtect.ph.hasRegionPermLeader(p, "delete", r)) {
            if (r == null) {
                sendNotInRegionMessage(p);
                return;
            }
            
            String puuid = RPUtil.PlayerToUUID(p.getName());
			if (r.isRentFor(puuid)){
				RPLang.sendMessage(p, "cmdmanager.rent.cantdothisrent");
				return;
			}
			
			int claims = RPConfig.getInt("region-settings.first-home.can-delete-after-claims");
			if (!r.canDelete() && (claims == -1 || RedProtect.rm.getPlayerRegions(p.getName(), p.getWorld()) < claims) && !p.hasPermission("redprotect.bypass")){
				if (claims != -1){
					RPLang.sendMessage(p, RPLang.get("cmdmanager.region.cantdeletefirst-claims").replace("{claims}", ""+claims));
				} else {
					RPLang.sendMessage(p, RPLang.get("cmdmanager.region.cantdeletefirst"));
				}				
				return;
			}
			
            DeleteRegionEvent event = new DeleteRegionEvent(r, p);
			Bukkit.getPluginManager().callEvent(event);    			
			if (event.isCancelled()){
				return;
			}
			
            String rname = r.getName();
            String w = r.getWorld();
            RedProtect.rm.remove(r, RedProtect.serv.getWorld(w));
            RPLang.sendMessage(p,RPLang.get("cmdmanager.region.deleted") +" "+ rname);
            RedProtect.logger.addLog("(World "+w+") Player "+p.getName()+" REMOVED region "+rname);
        }
        else {
            sendNoPermissionMessage(p);
        }
    }
	
	private static void handleDeleteName(Player p, String rname, String world) {
		Region r = RedProtect.rm.getRegion(rname, p.getWorld());
		if (!world.equals("")){
			if (Bukkit.getWorld(world) != null){
				r = RedProtect.rm.getRegion(rname, Bukkit.getWorld(world));
			} else {
				RPLang.sendMessage(p, "cmdmanager.region.invalidworld");
				return;
			}
		}
		
        if (RedProtect.ph.hasRegionPermLeader(p, "delete", r)) {
            if (r == null) {
            	RPLang.sendMessage(p, RPLang.get("cmdmanager.region.doesntexist") + ": " + rname);
                return;
            }
            
            String puuid = RPUtil.PlayerToUUID(p.getName());
			if (r.isRentFor(puuid)){
				RPLang.sendMessage(p, "cmdmanager.rent.cantdothisrent");
				return;
			}
			
			int claims = RPConfig.getInt("region-settings.first-home.can-delete-after-claims");
			if (!r.canDelete() && (claims == -1 || RedProtect.rm.getPlayerRegions(p.getName(), p.getWorld()) < claims) && !p.hasPermission("redprotect.bypass")){
				if (claims != -1){
					RPLang.sendMessage(p, RPLang.get("cmdmanager.region.cantdeletefirst-claims").replace("{claims}", ""+claims));
				} else {
					RPLang.sendMessage(p, RPLang.get("cmdmanager.region.cantdeletefirst"));
				}				
				return;
			}
			
            DeleteRegionEvent event = new DeleteRegionEvent(r, p);
			Bukkit.getPluginManager().callEvent(event);    			
			if (event.isCancelled()){
				return;
			}

            RedProtect.rm.remove(r, RedProtect.serv.getWorld(r.getWorld()));
            RPLang.sendMessage(p,RPLang.get("cmdmanager.region.deleted") +" "+ rname);
            RedProtect.logger.addLog("(World "+world+") Player "+p.getName()+" REMOVED region "+rname);
        }
        else {
            sendNoPermissionMessage(p);
        }
    }
    
	private static void handleInfoTop(Player p) {  
    	Region r = RedProtect.rm.getTopRegion(p.getLocation());
		if (r == null) {
                sendNotInRegionMessage(p);
                return;
        }
    	Map<Integer, Region> groupr = RedProtect.rm.getGroupRegion(p.getLocation());
    	if (RedProtect.ph.hasRegionPermAdmin(p, "info", r) || r.isForSale()) {            
            p.sendMessage(RPLang.get("general.color") + "--------------- [" + ChatColor.GOLD + r.getName() + RPLang.get("general.color") + "] ---------------");
            p.sendMessage(r.info());
            p.sendMessage(RPLang.get("general.color") + "----------------------------------");
            if (groupr.size() > 1){
            	p.sendMessage(RPLang.get("cmdmanager.moreregions"));
                for (Region regs:groupr.values()){
                	if (regs != r){
                		p.sendMessage(RPLang.get("region.name") + " " + regs.getName() + " " + RPLang.get("region.priority") + " " + regs.getPrior());
                	}            	
                }
            }
        }
        else {
            sendNoPermissionMessage(p);
        }
       
    }
    
	private static void handleInfo(Player p, String region, String world) {
		Region r = RedProtect.rm.getRegion(region, p.getWorld());
		if (!world.equals("")){
			if (Bukkit.getWorld(world) != null){
				r = RedProtect.rm.getRegion(region, Bukkit.getWorld(world));
			} else {
				RPLang.sendMessage(p, "cmdmanager.region.invalidworld");
				return;
			}
		}
    	if (RedProtect.ph.hasRegionPermAdmin(p, "info", r) || r.isForSale()) {
            if (r == null) {
                sendNotInRegionMessage(p);
                return;
            }
            p.sendMessage(RPLang.get("general.color") + "--------------- [" + ChatColor.GOLD + r.getName() + RPLang.get("general.color") + "] ---------------");
            p.sendMessage(r.info());
            p.sendMessage(RPLang.get("general.color") + "----------------------------------");
        }
        else {
            sendNoPermissionMessage(p);
        }        
    }
    
	private void handleAddMember(Player p, String sVictim) {
    	Region r = RedProtect.rm.getTopRegion(p.getLocation());
        if (RedProtect.ph.hasRegionPermAdmin(p, "addmember", r)) {
            if (r == null) {
                sendNotInRegionMessage(p);
                return;
            }
            
            if (r.isRentFor(RPUtil.PlayerToUUID(p.getName()))){
            	RPLang.sendMessage(p, "cmdmanager.rent.cantaddmore");
            	return;
            }
            
            String VictimUUID = RPUtil.PlayerToUUID(sVictim);
            if (RPUtil.UUIDtoPlayer(VictimUUID) == null){
            	RPLang.sendMessage(p,RPLang.get("cmdmanager.noplayer.thisname").replace("{player}", sVictim));
            	return;
            }
            
            Player pVictim = RedProtect.serv.getPlayer(sVictim);
                                    
            if (r.isLeader(VictimUUID)) {
            	RPLang.sendMessage(p,ChatColor.RED + sVictim + " " + RPLang.get("cmdmanager.region.leader.already"));
            	return;
            }
            
            if (r.isAdmin(VictimUUID)) {
                r.addMember(VictimUUID);
                RedProtect.logger.addLog("(World "+r.getWorld()+") Player "+p.getName()+" ADDED MEMBER "+RPUtil.UUIDtoPlayer(VictimUUID)+" to region "+r.getName());
                RPLang.sendMessage(p,RPLang.get("general.color") + sVictim + " " + RPLang.get("cmdmanager.region.member.demoted") + " " + r.getName());
                if (pVictim != null && pVictim.isOnline()) {
                	RPLang.sendMessage(pVictim, RPLang.get("cmdmanager.region.admin.youdemoted").replace("{region}", r.getName()) + " " + p.getName());
                }
            } else if (!r.isMember(VictimUUID)) {
                r.addMember(VictimUUID);
                RedProtect.logger.addLog("(World "+r.getWorld()+") Player "+p.getName()+" ADDED MEMBER "+RPUtil.UUIDtoPlayer(VictimUUID)+" to region "+r.getName());
                RPLang.sendMessage(p,RPLang.get("general.color") + sVictim + " " + RPLang.get("cmdmanager.region.member.added") + " " + r.getName());
                if (pVictim != null && pVictim.isOnline() && !pVictim.equals(p)) {
                    RPLang.sendMessage(pVictim, RPLang.get("cmdmanager.region.member.youadded").replace("{region}", r.getName()) + " " + p.getName());
                }
            } else {
                RPLang.sendMessage(p,ChatColor.RED + sVictim + " " + RPLang.get("cmdmanager.region.member.already"));
            }
        } else {
            sendNoPermissionMessage(p);
        }
    }
    
	private void handleAddLeader(final Player p, final String sVictim) {
		final Region r = RedProtect.rm.getTopRegion(p.getLocation());
        if (RedProtect.ph.hasRegionPermLeader(p, "addleader", r)) {
            if (r == null) {
                sendNotInRegionMessage(p);
                return;
            }
            
            if (r.isRentFor(RPUtil.PlayerToUUID(p.getName()))){
            	RPLang.sendMessage(p, "cmdmanager.rent.cantaddmore");
            	return;
            }
            
            final Player pVictim = RedProtect.serv.getPlayer(sVictim);            

            final String VictimUUID = RPUtil.PlayerToUUID(sVictim);
            if ((pVictim == null || pVictim != null && !pVictim.isOnline()) && !p.hasPermission("redprotect.bypass.addleader")){
        		RPLang.sendMessage(p,RPLang.get("cmdmanager.noplayer.online").replace("{player}", sVictim));
            	return;
        	}
            
            if (!p.hasPermission("redprotect.bypass.addleader")){
            	int claimLimit = RedProtect.ph.getPlayerClaimLimit(pVictim);
                int claimused = RedProtect.rm.getPlayerRegions(pVictim.getName(),pVictim.getWorld()); 
                boolean claimUnlimited = RedProtect.ph.hasPerm(p, "redprotect.limit.claim.unlimited");
                if (claimused >= claimLimit && claimLimit >= 0 && !claimUnlimited){            	
                	RPLang.sendMessage(p, RPLang.get("cmdmanager.region.addleader.limit").replace("{player}", pVictim.getName()));
                	return;
                } 
            }                   
            
            if (!r.isLeader(VictimUUID)) {            	
                
            	if (p.hasPermission("redprotect.bypass.addleader")){
            		r.addLeader(VictimUUID);
                    RedProtect.logger.addLog("(World "+r.getWorld()+") Player "+p.getName()+" ADDED LEADER "+RPUtil.UUIDtoPlayer(VictimUUID)+" to region "+r.getName());
                    RPLang.sendMessage(p,RPLang.get("general.color") + sVictim + " " + RPLang.get("cmdmanager.region.leader.added") + " " + r.getName());
                    if (pVictim != null && pVictim.isOnline() && !pVictim.equals(p)) {
                        RPLang.sendMessage(pVictim, RPLang.get("cmdmanager.region.leader.youadded").replace("{region}", r.getName()) + " " + p.getName());
                    }
            		return;
            	}
            	
                RPLang.sendMessage(p, RPLang.get("cmdmanager.region.leader.yousendrequest").replace("{player}", pVictim.getName()));
                RPLang.sendMessage(pVictim, RPLang.get("cmdmanager.region.leader.sendrequestto").replace("{region}", r.getName()).replace("{player}", p.getName()));
                
                RedProtect.alWait.put(pVictim, r.getID()+"@"+p.getName());
                Bukkit.getScheduler().scheduleSyncDelayedTask(RedProtect.plugin, new Runnable(){
        			@Override
        			public void run() {
        				if (RedProtect.alWait.containsKey(pVictim)){        					
                            RedProtect.alWait.remove(pVictim); 
                            if (p.isOnline()){
                            	RPLang.sendMessage(p, RPLang.get("cmdmanager.region.leader.requestexpired").replace("{player}", pVictim.getName()));
                            }                            
        				}        				
        			}    		
            	}, RPConfig.getInt("region-settings.leadership-request-time")*20);
            } else {
                RPLang.sendMessage(p,ChatColor.RED + sVictim + " " + RPLang.get("cmdmanager.region.leader.already"));
            }
        }
        else {
            sendNoPermissionMessage(p);
        }		
	}
	
	private static void handleAddAdmin(Player p, String sVictim) {
    	Region r = RedProtect.rm.getTopRegion(p.getLocation());
        if (RedProtect.ph.hasRegionPermAdmin(p, "addadmin", r)) {
            if (r == null) {
                sendNotInRegionMessage(p);
                return;
            }
            
            if (r.isRentFor(RPUtil.PlayerToUUID(p.getName()))){
            	RPLang.sendMessage(p, "cmdmanager.rent.cantaddmore");
            	return;
            }
            
            Player pVictim = RedProtect.serv.getPlayer(sVictim);
            
            String VictimUUID = RPUtil.PlayerToUUID(sVictim);
            if (RPUtil.UUIDtoPlayer(VictimUUID) == null){
            	RPLang.sendMessage(p,RPLang.get("cmdmanager.noplayer.thisname").replace("{player}", sVictim));
            	return;
            }
            
            if (r.isLeader(VictimUUID)) {
            	RPLang.sendMessage(p,ChatColor.RED + sVictim + " " + RPLang.get("cmdmanager.region.leader.already"));
            	return;
            }
            
            if (!r.isAdmin(VictimUUID)) {
                r.addAdmin(VictimUUID);
                RedProtect.logger.addLog("(World "+r.getWorld()+") Player "+p.getName()+" ADDED ADMIN "+RPUtil.UUIDtoPlayer(VictimUUID)+" to region "+r.getName());
                RPLang.sendMessage(p,RPLang.get("general.color") + sVictim + " " + RPLang.get("cmdmanager.region.admin.added") + " " + r.getName());
                if (pVictim != null && pVictim.isOnline() && !pVictim.equals(p)) {
                    RPLang.sendMessage(pVictim, RPLang.get("cmdmanager.region.admin.youadded").replace("{region}", r.getName()) + " " + p.getName());
                }
            }
            else {
                RPLang.sendMessage(p,ChatColor.RED + sVictim + " " + RPLang.get("cmdmanager.region.admin.already"));
            }
        }
        else {
            sendNoPermissionMessage(p);
        }
    }
    
	private static void handleRemoveMember(Player p, String sVictim) {
    	Region r = RedProtect.rm.getTopRegion(p.getLocation());
        if (RedProtect.ph.hasRegionPermAdmin(p, "removemember", r)) {
            if (r == null) {
                sendNotInRegionMessage(p);
                return;
            }
            
            Player pVictim = RedProtect.serv.getPlayer(sVictim);
            
            String VictimUUID = RPUtil.PlayerToUUID(sVictim);
            if (RPUtil.UUIDtoPlayer(VictimUUID) == null){
            	RPLang.sendMessage(p,RPLang.get("cmdmanager.noplayer.thisname").replace("{player}", sVictim));
            	return;
            }
            
            String victname = RPUtil.UUIDtoPlayer(VictimUUID);
            
            if (r.isRentFor(RPUtil.PlayerToUUID(p.getName())) || r.isRentFor(VictimUUID)){
            	RPLang.sendMessage(p, "cmdmanager.rent.cantchangerank");
            	return;
            }
            
            if ((r.isMember(VictimUUID) || r.isAdmin(VictimUUID)) && !r.isLeader(VictimUUID)) {
                RPLang.sendMessage(p,RPLang.get("general.color") + sVictim + " " + RPLang.get("cmdmanager.region.member.removed") + " " + r.getName());
                r.removeMember(VictimUUID);
                RedProtect.logger.addLog("(World "+r.getWorld()+") Player "+p.getName()+" REMOVED MEMBER "+victname+" to region "+r.getName());
                if (pVictim != null && pVictim.isOnline() && !pVictim.equals(p)) {
                    RPLang.sendMessage(pVictim, RPLang.get("cmdmanager.region.member.youremoved").replace("{region}", r.getName()) + " " + p.getName());
                }
            } else {
                RPLang.sendMessage(p,ChatColor.RED + sVictim + " " + RPLang.get("cmdmanager.region.member.notmember"));                
            }
        }
        else {
            sendNoPermissionMessage(p);
        }
    }
    
	private static void handleRemoveLeader(Player p, String sVictim) {
    	Region r = RedProtect.rm.getTopRegion(p.getLocation());
		Region rLow = RedProtect.rm.getLowRegion(p.getLocation());
		Map<Integer,Region> regions = RedProtect.rm.getGroupRegion(p.getLocation());
        if (RedProtect.ph.hasRegionPermLeader(p, "removeleader", r)) {
            if (r == null) {
                sendNotInRegionMessage(p);
                return;
            }
            
            Player pVictim = RedProtect.serv.getPlayer(sVictim);
            
            String VictimUUID = RPUtil.PlayerToUUID(sVictim);
            if (RPUtil.UUIDtoPlayer(VictimUUID) == null){
            	RPLang.sendMessage(p,RPLang.get("cmdmanager.noplayer.thisname").replace("{player}", sVictim));
            	return;
            }

            String victname = RPUtil.UUIDtoPlayer(VictimUUID);
            if (r.isRentFor(RPUtil.PlayerToUUID(p.getName())) || r.isRentFor(VictimUUID)){
            	RPLang.sendMessage(p, "cmdmanager.rent.cantchangerank");
            	return;
            }
            
            if (rLow != r && ((!RedProtect.ph.hasRegionPermLeader(p, "removeleader", rLow) || (regions.size() > 1 && rLow.isLeader(VictimUUID))))){
        		RPLang.sendMessage(p,RPLang.get("cmdmanager.region.leader.cantremove.lowregion").replace("{player}", sVictim) + " " +rLow.getName());
            	return;
        	}	  
            if (r.isLeader(VictimUUID)) {
                if (r.leaderSize() > 1) {
                    RPLang.sendMessage(p,RPLang.get("general.color") + sVictim + " " + RPLang.get("cmdmanager.region.admin.added") + " " +r.getName());
                    r.removeLeader(VictimUUID);
                    RedProtect.logger.addLog("(World "+r.getWorld()+") Player "+p.getName()+" DEMOTED TO ADMIN "+victname+" to region "+r.getName());
                    if (pVictim != null && pVictim.isOnline() && !pVictim.equals(p)) {
                        RPLang.sendMessage(pVictim, RPLang.get("cmdmanager.region.leader.youdemoted").replace("{region}", r.getName())+ " " + p.getName());
                    }
                } else {
                    RPLang.sendMessage(p,RPLang.get("cmdmanager.region.leader.cantremove").replace("{player}", sVictim));
                }
            }
            else {
                RPLang.sendMessage(p,ChatColor.RED + sVictim + " " + RPLang.get("cmdmanager.region.leader.notleader"));
            }
        }
        else {
            sendNoPermissionMessage(p);
        }
    }
	
	private static void handleRemoveAdmin(Player p, String sVictim) {
    	Region r = RedProtect.rm.getTopRegion(p.getLocation());
        if (RedProtect.ph.hasRegionPermAdmin(p, "removeadmin", r)) {
            if (r == null) {
                sendNotInRegionMessage(p);
                return;
            }
            
            Player pVictim = RedProtect.serv.getPlayer(sVictim);
            
            String VictimUUID = RPUtil.PlayerToUUID(sVictim);
            if (RPUtil.UUIDtoPlayer(VictimUUID) == null){
            	RPLang.sendMessage(p,RPLang.get("cmdmanager.noplayer.thisname").replace("{player}", sVictim));
            	return;
            }
            
            String victname = RPUtil.UUIDtoPlayer(VictimUUID);
            if (r.isRentFor(RPUtil.PlayerToUUID(p.getName())) || r.isRentFor(VictimUUID)){
            	RPLang.sendMessage(p, "cmdmanager.rent.cantchangerank");
            	return;
            }
            
            if (r.isAdmin(VictimUUID)) {
            	RPLang.sendMessage(p,RPLang.get("general.color") + sVictim + " " + RPLang.get("cmdmanager.region.member.added") + " " +r.getName());
                r.removeAdmin(VictimUUID);
                RedProtect.logger.addLog("(World "+r.getWorld()+") Player "+p.getName()+" DEMOTED TO MEMBER "+victname+" to region "+r.getName());
                if (pVictim != null && pVictim.isOnline() && !pVictim.equals(p)) {
                    RPLang.sendMessage(pVictim, RPLang.get("cmdmanager.region.admin.youdemoted").replace("{region}", r.getName())+ " " + p.getName());
                }
            }
            else {
                RPLang.sendMessage(p,ChatColor.RED + sVictim + " " + RPLang.get("cmdmanager.region.admin.notadmin"));
            }
        }
        else {
            sendNoPermissionMessage(p);
        }
    }
    
	private static void handleRename(Player p, String newName) {
    	Region r = RedProtect.rm.getTopRegion(p.getLocation());
        if (RedProtect.ph.hasRegionPermLeader(p, "rename", r)) {
            if (r == null) {
                sendNotInRegionMessage(p);
                return;
            }
            
            //region name conform
            newName = newName.replace("/", "|");
            if (RedProtect.rm.getRegion(newName, p.getWorld()) != null) {
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
                p.sendMessage(RPLang.get("regionbuilder.regionname.invalid.charac").replace("{charac}", "@"));
                return;
            }
            
            RenameRegionEvent event = new RenameRegionEvent(r, newName, r.getName(), p);
			Bukkit.getPluginManager().callEvent(event);    			
			if (event.isCancelled()){
				return;
			}
			
			String oldname = event.getOldName();
			newName = event.getNewName();
			
            RedProtect.rm.renameRegion(newName, r);
            RPLang.sendMessage(p,RPLang.get("cmdmanager.region.rename.newname") + " " + newName);
            RedProtect.logger.addLog("(World "+r.getWorld()+") Player "+p.getName()+" RENAMED region "+oldname+" to "+newName);
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
    	
    	if (RedProtect.ph.hasPerm(p, "redprotect.flag."+ flag) || flag.equalsIgnoreCase("info")) {                
            if (r.isAdmin(p) || r.isLeader(p) || RedProtect.ph.hasRegionPermAdmin(p, "redprotect.admin.flag."+flag, r)) {            	
            	if (flag.equalsIgnoreCase("info") || flag.equalsIgnoreCase("i")) {            
                    p.sendMessage(RPLang.get("general.color") + "------------[" + RPLang.get("cmdmanager.region.flag.values") + "]------------");
                    p.sendMessage(r.getFlagInfo());
                    p.sendMessage(RPLang.get("general.color") + "------------------------------------");
                    return;
                }  
            	
            	if (value.equalsIgnoreCase("remove")){
            		if (RPConfig.AdminFlags.contains(flag) && r.flags.containsKey(flag)){
            			r.removeFlag(flag);
                        RPLang.sendMessage(p,RPLang.get("cmdmanager.region.flag.removed").replace("{flag}", flag).replace("{region}", r.getName())); 
                        RedProtect.logger.addLog("(World "+r.getWorld()+") Player "+p.getName()+" REMOVED FLAG "+flag+" of region "+r.getName());
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
            	if (RPConfig.getDefFlagsValues().containsKey("clan") && !RedProtect.ph.hasPerm(p, "redprotect.admin.flag.clan")){
            		RPLang.sendMessage(p,"cmdmanager.region.flag.clancommand");
            		return;
            	}
            	*/
            	if (!value.equals("")){
            		if (RPConfig.getDefFlagsValues().containsKey(flag)) {
            			
            			//flag clan
            			if (flag.equalsIgnoreCase("clan")){
            				if (!RedProtect.SC || !RedProtect.ph.hasGenPerm(p, "redprotect.admin.flag.clan")){
            					sendFlagHelp(p);
                            	return; 
            				}
            				if (!RedProtect.clanManager.isClan(value)){
            					RPLang.sendMessage(p, RPLang.get("cmdmanager.region.flag.invalidclan").replace("{tag}", value));
                        		return;
            				}            				
            				Clan clan = RedProtect.clanManager.getClan(value);
            				if (!clan.isLeader(p)){
            					RPLang.sendMessage(p,"cmdmanager.region.flag.clancommand");
                        		return;
            				}  
            				r.setFlag(flag, value);
                            RPLang.sendMessage(p,RPLang.get("cmdmanager.region.flag.set").replace("{flag}", "'"+flag+"'") + " " + r.getFlagString(flag));
                            RedProtect.logger.addLog("(World "+r.getWorld()+") Player "+p.getName()+" SET FLAG "+flag+" of region "+r.getName()+" to "+r.getFlagString(flag));
                            return;
                    	}
            			
            			if (objflag instanceof Boolean){
            				r.setFlag(flag, objflag);
                            RPLang.sendMessage(p,RPLang.get("cmdmanager.region.flag.set").replace("{flag}", "'"+flag+"'") + " " + r.getFlagBool(flag));
                            RedProtect.logger.addLog("(World "+r.getWorld()+") Player "+p.getName()+" SET FLAG "+flag+" of region "+r.getName()+" to "+r.getFlagString(flag));
                            return;
            			} else {
            				RPLang.sendMessage(p,RPLang.get("cmdmanager.region.flag.usage") + " <true/false>");
            				return;
            			}                		
                	} 
                	
                	if (RPConfig.AdminFlags.contains(flag)) {
                		if (!validate(flag, objflag)){
                			SendFlagUsageMessage(p, flag);               			
                			return;
                		}
                		r.setFlag(flag, objflag);
                		RPLang.sendMessage(p,RPLang.get("cmdmanager.region.flag.set").replace("{flag}", "'"+flag+"'") + " " + r.getFlagString(flag));
            			RedProtect.logger.addLog("(World "+r.getWorld()+") Player "+p.getName()+" SET FLAG "+flag+" of region "+r.getName()+" to "+r.getFlagString(flag));                     
                        return;               		
                	} 

                	
                	if (RPConfig.AdminFlags.contains(flag)){
                		SendFlagUsageMessage(p, flag); 
            		} else {
                    	RPLang.sendMessage(p,RPLang.get("cmdmanager.region.flag.usage") + " <true/false>");
            		}
                	sendFlagHelp(p);
                	return; 

            	} else {
            		
            		//flag clan
        			if (flag.equalsIgnoreCase("clan")){
        				if (RedProtect.SC){
        					ClanPlayer clan = RedProtect.clanManager.getClanPlayer(p);
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
                            RedProtect.logger.addLog("(World "+r.getWorld()+") Player "+p.getName()+" SET FLAG "+flag+" of region "+r.getName()+" to "+r.getFlagString(flag)); 
                            return;
        				} else {
        					sendFlagHelp(p);
                        	return; 
        				}        				
                	}
        			
            		if (RPConfig.getDefFlagsValues().containsKey(flag)) {
            			r.setFlag(flag, !r.getFlagBool(flag));
                        RPLang.sendMessage(p,RPLang.get("cmdmanager.region.flag.set").replace("{flag}", "'"+flag+"'") + " " + r.getFlagBool(flag));
            			RedProtect.logger.addLog("(World "+r.getWorld()+") Player "+p.getName()+" SET FLAG "+flag+" of region "+r.getName()+" to "+r.getFlagString(flag));
                        return;
            		} else {
            			if (RPConfig.AdminFlags.contains(flag)){
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
				flag.equalsIgnoreCase("allow-enter-items") ||
				flag.equalsIgnoreCase("deny-enter-items") ||
				flag.equalsIgnoreCase("gamemode") ||
				flag.equalsIgnoreCase("allow-cmds") || 
				flag.equalsIgnoreCase("deny-cmds") || 
				flag.equalsIgnoreCase("allow-break") || 
				flag.equalsIgnoreCase("allow-place") ||
				flag.equalsIgnoreCase("set-portal") ||
				flag.equalsIgnoreCase("cmd-onhealth")){                				
			message = RPLang.get("cmdmanager.region.flag.usage"+flag);
		} else {
			message = RPLang.get("cmdmanager.region.flag.usagetruefalse").replace("{flag}", flag);
		} 	
		p.sendMessage(message.replace("{cmd}", getCmd("flag")));
	}

	private static void sendFlagHelp(Player p) {
		p.sendMessage(RPLang.get("general.color") + "-------------[RedProtect Flags]------------");
    	p.sendMessage(RPLang.get("cmdmanager.region.flag.list") + " " + RPConfig.getDefFlags());
    	p.sendMessage(RPLang.get("general.color") + "------------------------------------");
    	if (RedProtect.ph.hasPerm(p, "redprotect.flag.special")){                		
        	p.sendMessage(RPLang.get("cmdmanager.region.flag.admlist") + " " + RPConfig.AdminFlags);    
        	p.sendMessage(RPLang.get("general.color") + "------------------------------------");
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
		
		if (flag.equalsIgnoreCase("gamemode")){
			if (!(value instanceof String)){
				return false;
			}
			try {
				GameMode.valueOf(((String)value).toUpperCase());
			} catch (IllegalArgumentException e){
				return false;
			}			
		}
		
		if (flag.equalsIgnoreCase("setclan") && RedProtect.SC){
			if (!(value instanceof String)){
				return false;
			}
			if (RedProtect.clanManager.getClan((String)value) == null){
				return false;
			}
		}
		
		if (flag.equalsIgnoreCase("set-portal")){
			if (!(value instanceof String)){
				return false;
			}
			String[] valida = ((String)value).split(" ");
			if (valida.length != 2){
				return false;
			}
			if (Bukkit.getWorld(valida[1]) == null){
				return false;
			}
			Region r = RedProtect.rm.getRegion(valida[0], valida[1]);
			if (r == null){
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
				if (Material.getMaterial(item.toUpperCase()) == null){
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
				if (Material.getMaterial(item.toUpperCase()) == null && EntityType.valueOf(item.toUpperCase()) == null){
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
				if (PotionEffectType.getByName(effect[0]) == null){
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
        if (RedProtect.ph.hasPerm(p, "redprotect.admin.list")) {
        	getRegionforList(p, uuid, Page);
        	return;
        } else if (RedProtect.ph.hasPerm(p, "redprotect.own.list") && pname.equalsIgnoreCase(uuid)){
        	getRegionforList(p, uuid, Page);
        	return;
        }
        RPLang.sendMessage(p, "no.permission");
    }
	    
	private static void getRegionforList(CommandSender sender, String uuid, int Page){
    	Set<Region> regions = RedProtect.rm.getRegions(RPUtil.PlayerToUUID(uuid));
    	String pname = RPUtil.UUIDtoPlayer(uuid);
        int length = regions.size();
        if (pname == null || length == 0) {
            RPLang.sendMessage(sender, "cmdmanager.player.noregions");
            return;
        }
        else {
        	sender.sendMessage(RPLang.get("general.color") + "-------------------------------------------------");
        	RPLang.sendMessage(sender,RPLang.get("cmdmanager.region.created.list") + " " +pname);
        	sender.sendMessage("-----");        	
        	if (RPConfig.getBool("region-settings.region-list.simple-listing")){
        		for (World w:Bukkit.getWorlds()){
        			String colorChar = ChatColor.translateAlternateColorCodes('&', RPConfig.getString("region-settings.world-colors." + w.getName(), "&a"));
        			Set<Region> wregions = RedProtect.rm.getRegions(RPUtil.PlayerToUUID(uuid), w);
        			if (wregions.size() > 0){
        				Iterator<Region> it = wregions.iterator();        				
        				if (RPConfig.getBool("region-settings.region-list.hover-and-click-teleport") && RedProtect.ph.hasRegionPermAdmin(sender, "teleport", null) && new FancyMessage() != null){
        					FancyMessage fancy = new FancyMessage();
                			boolean first = true;
                			while (it.hasNext()){
                				Region r = it.next();
                				String area = RPConfig.getBool("region-settings.region-list.show-area") ? "("+RPUtil.simuleTotalRegionSize(RPUtil.PlayerToUUID(uuid), r)+")":"";
                				
                				String rname = RPLang.get("general.color")+", "+ChatColor.GRAY+r.getName()+area;
                				if (first){
                					rname = rname.substring(3);
                					first = false;
                				}
                				if (!it.hasNext()){
                					rname = rname+RPLang.get("general.color")+".";
                				}
                				fancy.text(rname).color(ChatColor.DARK_GRAY)
                        				.tooltip(RPLang.get("cmdmanager.list.hover").replace("{region}", r.getName()))
                        				.command("/rp "+getCmd("teleport")+" "+r.getName()+" "+r.getWorld())
                        				.then(" ");
                			} 
                			sender.sendMessage(RPLang.get("general.color")+RPLang.get("region.world").replace(":", "")+" "+colorChar+w.getName()+"["+wregions.size()+"]"+ChatColor.RESET+": ");
                			fancy.send(sender);
                			sender.sendMessage("-----");
        				} else {
        					String worldregions = "";
                			while (it.hasNext()){
                				Region r = it.next();
                				String area = RPConfig.getBool("region-settings.region-list.show-area") ? "("+RPUtil.simuleTotalRegionSize(RPUtil.PlayerToUUID(uuid), r)+")":"";
                				worldregions = worldregions+RPLang.get("general.color")+", "+ChatColor.GRAY+r.getName()+area;
                			}
                			sender.sendMessage(RPLang.get("general.color")+RPLang.get("region.world").replace(":", "")+" "+colorChar+w.getName()+"["+wregions.size()+"]"+ChatColor.RESET+": "); 
                			sender.sendMessage(worldregions.substring(3)+RPLang.get("general.color")+".");
                			sender.sendMessage("-----");                			
        				}        				           			
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
                	String info = i.next().info();
                	if (count >= min && count <= max){
                		sender.sendMessage(RPLang.get("general.color") + "-------------------------------------------------");
                        sender.sendMessage(RPLang.get("general.color") + "["+(count+1)+"] " + info);     
                        last = count;
                        
                	}
                	count++;
                }      
                if (max > count){min = 0;}
            	sender.sendMessage(RPLang.get("general.color") + "------------- "+(min+1)+"-"+(last+1)+"/"+count+" --------------");
            	if (count > max){
                	sender.sendMessage(RPLang.get("cmdmanager.region.listpage.more").replace("{player}", pname + " " + (Page+1)));
                } else {
                	if (Page != 1) {sender.sendMessage(RPLang.get("cmdmanager.region.listpage.nomore"));}
                }
        	}                    	
        }
        return;
    }
    
	private static void handleWelcome(Player p, String wMessage) {
    	Region r = RedProtect.rm.getTopRegion(p.getLocation());
    	if (RedProtect.ph.hasRegionPermAdmin(p, "welcome", r)) {    		
        	if (r != null){
        		if (wMessage.equals("")){
        			r.setWelcome("");
        			RPLang.sendMessage(p, "cmdmanager.region.welcomeoff");
        		} else if (wMessage.equals("hide ")){
        			r.setWelcome(wMessage);
        			RPLang.sendMessage(p, "cmdmanager.region.welcomehide");
        		} else {
        			r.setWelcome(wMessage);
                	RPLang.sendMessage(p,RPLang.get("cmdmanager.region.welcomeset") + " "+ ChatColor.translateAlternateColorCodes('&', wMessage));                	       		
        		}
        		RedProtect.logger.addLog("(World "+r.getWorld()+") Player "+p.getName()+" SET WELCOME of region "+r.getName()+" to "+wMessage);
        		return; 
        	} else {
        		RPLang.sendMessage(p, "cmdmanager.region.todo.that");
        		return;
        	}
        } 
        RPLang.sendMessage(p, "no.permission");
    }
	
	private static void handletp(Player p, String rname, String wname, Player play){
		World w = RedProtect.serv.getWorld(wname);
		if (w == null) {
            RPLang.sendMessage(p, "cmdmanager.region.invalidworld");
            return;
        }
    	Region region = RedProtect.rm.getRegion(rname, w);
    	if (region == null) {
    		RPLang.sendMessage(p, RPLang.get("cmdmanager.region.doesntexist") + ": " + rname);
            return;
        }          
    	
    	if (play == null) {
    		if (!RedProtect.ph.hasRegionPermMember(p, "teleport", region)){
    			RPLang.sendMessage(p, "no.permission");
                return;
    		}
    	} else {
    		if (!RedProtect.ph.hasPerm(p, "redprotect.teleport.other")) {
        		RPLang.sendMessage(p, "no.permission");
                return;
            }    		
        }      

    	Location loc = null;
    	if (region.getTPPoint() != null){
    		loc = region.getTPPoint();
    		loc.setX(loc.getBlockX()+0.500);
			loc.setZ(loc.getBlockZ()+0.500);
    	} else {
    		int limit = w.getMaxHeight();
        	if (w.getEnvironment().equals(Environment.NETHER)){
        		limit = 124;
        	}
        	for (int i = limit; i > 0; i--){
        		Material mat = w.getBlockAt(region.getCenterX(), i, region.getCenterZ()).getType();
        		Material mat1 = w.getBlockAt(region.getCenterX(), i+1, region.getCenterZ()).getType();
        		Material mat2 = w.getBlockAt(region.getCenterX(), i+2, region.getCenterZ()).getType();
        		if ((!mat.equals(Material.LAVA) || !mat.equals(Material.STATIONARY_LAVA)) && !mat.equals(Material.AIR) && mat1.equals(Material.AIR) && mat2.equals(Material.AIR)){
        			loc = new Location(w, region.getCenterX()+0.500, i+1, region.getCenterZ()+0.500);            			
        			break;
        		}
        	}
    	}
    	
    	if (loc != null){
    		if (play != null){
    			if (RedProtect.Ess){
    				RedProtect.pless.getUser(p).setLastLocation();
            	}
    			play.teleport(loc);
    			RPLang.sendMessage(play, RPLang.get("cmdmanager.region.teleport") + " " + rname);   			
    			RPLang.sendMessage(p, RPLang.get("cmdmanager.region.tpother") + " " + rname);
    		} else {
    			tpWait(p, loc, rname);
    		}      		
			return;
    	}
    	return;
	}
	
	private static void tpWait(final Player p, final Location loc, final String rname){
		if (p.hasPermission("redprotect.admin.teleport")){
			p.teleport(loc);
			return;
		}
		if (!RedProtect.tpWait.contains(p.getName())){
    		RedProtect.tpWait.add(p.getName());
    		RPLang.sendMessage(p, "cmdmanager.region.tpdontmove");
    		Bukkit.getScheduler().scheduleSyncDelayedTask(RedProtect.plugin, new Runnable(){
    			@Override
    			public void run() {
    				if (RedProtect.tpWait.contains(p.getName())){
                		RedProtect.tpWait.remove(p.getName());
                		if (RedProtect.Ess){
            				RedProtect.pless.getUser(p).setLastLocation();
                		}                		
                		p.teleport(loc);
                		RPLang.sendMessage(p,RPLang.get("cmdmanager.region.teleport") + " " + rname);
    				}
    			}    		
        	}, 60L);
    	} else {
    		RPLang.sendMessage(p, "cmdmanager.region.tpneedwait");
    	}
	}
	
	private static boolean handleMyChunk(){
		if (!RedProtect.MyChunk){
			return false;
		}
		Set<LiteChunk> allchunks = new HashSet<LiteChunk>();
		
		for (World w:RedProtect.serv.getWorlds()){
			Set<LiteChunk> chunks = MyChunkChunk.getChunks(w);
			allchunks.addAll(chunks);
		}
		
		if (allchunks.size() != 0){
			int i = 0;
			for (LiteChunk c:allchunks){
				List<String> leaders = new ArrayList<String>();
				String admin = RPUtil.PlayerToUUID(c.getOwner());
				leaders.add(admin.toString());
				World w = RedProtect.serv.getWorld(c.getWorldName());
				Chunk chunk = w.getChunkAt(c.getX(), c.getZ());
				int x = chunk.getBlock(7, 50, 7).getX();
				int z = chunk.getBlock(7, 50, 7).getZ();
				String regionName = "";
				
				int in = 0;
	            while (true) {
	            	int is = String.valueOf(in).length();
	                if (RPUtil.UUIDtoPlayer(admin).length() > 13) {
	                    regionName = RPUtil.UUIDtoPlayer(admin).substring(0, 14-is) + "_" + in;
	                }
	                else {
	                    regionName = RPUtil.UUIDtoPlayer(admin) + "_" + in;
	                }
	                if (RedProtect.rm.getRegion(regionName, w) == null) {
	                    break;
	                }
	                ++in;
	            }
	            
				Region r = new Region(regionName, new ArrayList<String>(), new ArrayList<String>(), leaders, new int[] {x + 8, x + 8, x - 7, x - 7}, new int[] {z + 8, z + 8, z - 7, z - 7}, 0, w.getMaxHeight(), 0, c.getWorldName(), RPUtil.DateNow(), RPConfig.getDefFlagsValues(), "", 0, null, true);
				MyChunkChunk.unclaim(chunk);
				RedProtect.rm.add(r, w);
				RedProtect.logger.warning("Region converted and named to "+ r.getName());
				i++;
			}
			RedProtect.logger.sucess(i + " MyChunk regions converted!");
			return true;
		} else {
			return false;
		}		
	}
	
	private static void HandleHelPage(CommandSender sender, int page){
		sender.sendMessage(RPLang.get("_redprotect.prefix") + " " + RPLang.get("cmdmanager.available.cmds"));
		sender.sendMessage(RPLang.get("general.color") + "------------------------------------");
		sender.sendMessage(RPLang.get("cmdmanager.helpheader.alias"));
        
		if (sender instanceof Player){
			Player player = (Player)sender;		
			int i = 0;
			for (String key:RPLang.helpStrings()){
				if (RedProtect.ph.hasGenPerm(player, key) || ((key.equals("pos1") || key.equals("pos1")) && RedProtect.ph.hasGenPerm(player, "redefine"))) {
					if (key.equalsIgnoreCase("flaggui")){
						continue;
					}
					i++;					
					
					if (i > (page*5)-5 && i <= page*5){
						player.sendMessage(RPLang.get("cmdmanager.help."+key).replace("{cmd}", getCmd(key)).replace("{alias}", getCmdAlias(key)));
					} 
					if (i > page*5){
						player.sendMessage(RPLang.get("general.color") + "------------------------------------");
						player.sendMessage(RPLang.get("cmdmanager.page").replace("{page}", ""+(page+1)));
						break;
					}
				}
			}
		} else {
			sender.sendMessage(ChatColor.GOLD + "rp setconfig list " + ChatColor.DARK_AQUA + "- List all editable configs");
			sender.sendMessage(ChatColor.GOLD + "rp setconfig <Config-Section> <Value> " + ChatColor.DARK_AQUA + "- Set a config option");
			sender.sendMessage(ChatColor.GOLD + "rp info <region> <world> " + ChatColor.DARK_AQUA + "- Info about a region");
			sender.sendMessage(ChatColor.GOLD + "rp flag <regionName> <Flag> <Value> <World> " + ChatColor.DARK_AQUA + "- Set a flag on region");
			sender.sendMessage(ChatColor.GOLD + "rp flag info <region> <world> " + ChatColor.DARK_AQUA + "- Flag info for region");
			sender.sendMessage(ChatColor.GOLD + "rp teleport <playerName> <regionName> <World> " + ChatColor.DARK_AQUA + "- Teleport player to a region");			
			sender.sendMessage(ChatColor.GOLD + "rp limit <playerName> " + ChatColor.DARK_AQUA + "- Area limit for player");
			sender.sendMessage(ChatColor.GOLD + "rp claimlimit <playerName> [world] " + ChatColor.DARK_AQUA + "- Claim limit for player");
			sender.sendMessage(ChatColor.GOLD + "rp list-all " + ChatColor.DARK_AQUA + "- List All regions");		
			sender.sendMessage(ChatColor.GOLD + "rp list <player> [page] " + ChatColor.DARK_AQUA + "- List All regions from player");		
			sender.sendMessage(ChatColor.GOLD + "rp ymlTomysql " + ChatColor.DARK_AQUA + "- Convert from Yml to Mysql");
			sender.sendMessage(ChatColor.GOLD + "rp mychunktorp " + ChatColor.DARK_AQUA + "- Convert from MyChunk to RedProtect");
			sender.sendMessage(ChatColor.GOLD + "rp single-to-files " + ChatColor.DARK_AQUA + "- Convert single world files to regions files");
			sender.sendMessage(ChatColor.GOLD + "rp files-to-single " + ChatColor.DARK_AQUA + "- Convert regions files to single world files");
			sender.sendMessage(ChatColor.GOLD + "rp gpTorp " + ChatColor.DARK_AQUA + "- Convert from GriefPrevention to RedProtect");
			sender.sendMessage(ChatColor.GOLD + "rp save-all " + ChatColor.DARK_AQUA + "- Save all regions to database");
			sender.sendMessage(ChatColor.GOLD + "rp load-all " + ChatColor.DARK_AQUA + "- Load all regions from database");
			sender.sendMessage(ChatColor.GOLD + "rp reload-config " + ChatColor.DARK_AQUA + "- Reload only the config");
			sender.sendMessage(ChatColor.GOLD + "rp reload " + ChatColor.DARK_AQUA + "- Reload the plugin");
		}
		sender.sendMessage(RPLang.get("general.color") + "------------------------------------");
	}
    
}
