package br.net.fabiozumbi12.RedProtect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import me.ellbristow.mychunk.LiteChunk;
import me.ellbristow.mychunk.MyChunkChunk;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;

class RPCommands implements CommandExecutor, TabCompleter{
    
    private static void sendNotInRegionMessage(Player p) {
        RPLang.sendMessage(p, "cmdmanager.region.todo.that");
    }
    
    private static void sendNoPermissionMessage(Player p) {
        RPLang.sendMessage(p, "no.permission");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args){
    	List<String> SotTab = new ArrayList<String>();    
    	SortedSet<String> tab = new TreeSet<String>();  
    	if (sender instanceof Player){
    		List<String> cmds = Arrays.asList("value", "buy", "sell", "cancelbuy", "tutorial", "limit", "claimlimit", "list", "delete", "info", "flag", "addmember", "addowner", "removemember", "removeowner", "rename", "welcome", "priority", "near", "panel");
    		List<String> admcmds = Arrays.asList("wand", "tp", "define", "redefine", "setconfig", "reload", "copyflag", "setcreator", "save-all", "reload-all");
    		
    		if (args.length == 1){
    			for (String command:cmds){
    				if (sender.hasPermission("redprotect.user") && command.startsWith(args[0]) && !tab.contains(command)){
    					tab.add(command);
    				}
    			}
    			for (String command:admcmds){
    				if (sender.hasPermission("redprotect.admin") && command.startsWith(args[0]) && !tab.contains(command)){
    					tab.add(command);
    				}
    			}
    			SotTab.addAll(tab);
    			return SotTab;
    		}
    		if (args.length == 2){
        		if (args[0].equalsIgnoreCase("flag")){
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
    		List<String> consolecmds = Arrays.asList("setconfig", "flag", "tp", "ymlTomysql", "setconfig", "reload", "save-all", "reload-all", "limit", "claimlimit", "list-all");
    		for (String command:consolecmds){
				if (command.startsWith(args[0])){
					tab.add(command);
				}
			}
    		SotTab.addAll(tab);
			return SotTab;
    	}
		return null;    	
    }
    
	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, final String[] args) {
		
		if (args.length == 0) {
            sender.sendMessage(RPLang.get("general.color") + "---------------- " + RedProtect.pdf.getFullName() + " ----------------");
            sender.sendMessage(RPLang.get("general.color") + "Developed by " + ChatColor.GOLD + RedProtect.pdf.getAuthors() + RPLang.get("general.color") + ".");
            sender.sendMessage(RPLang.get("general.color") + "For more information about the commands, type [" + ChatColor.GOLD + "/rp ?" + RPLang.get("general.color") + "].");
            sender.sendMessage(RPLang.get("general.color") + "For a tutorial, type [" + ChatColor.GOLD + "/rp tutorial" + RPLang.get("general.color") + "].");
            sender.sendMessage(RPLang.get("general.color") + "---------------------------------------------------");
            return true;
        }
		
        if (!(sender instanceof Player)) {        	
        	if (args.length == 1) {
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
        		if (args[0].equalsIgnoreCase("update")) {
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
        		
        		if (args[0].equalsIgnoreCase("mychunkconvert")) {
            		if (handleMyChunk()){
            			RedProtect.logger.sucess("...converting MyChunk database");
            			RedProtect.logger.sucess("http://dev.bukkit.org/bukkit-plugins/mychunk/");
            			return true;
            		} else {
            			RedProtect.logger.sucess("The plugin MyChunk is not installed or no regions found");
            			return true;
            		}        		
            	}
        		if (args[0].equalsIgnoreCase("save-all")) {            
        			RedProtect.rm.saveAll();
            		RedProtect.logger.sucess(RedProtect.rm.getAllRegions().size() + " regions saved with success!");  
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
        		if (args[0].equalsIgnoreCase("reload")) {
        			RedProtect.plugin.getServer().getPluginManager().disablePlugin((Plugin)RedProtect.plugin);
        			RedProtect.plugin.getServer().getPluginManager().enablePlugin((Plugin)RedProtect.plugin);
            		RedProtect.logger.sucess("RedProtect Plus reloaded!");
            		return true;
            	}          		
        	} 
        	
        	if(args.length == 2){
        		if  (args[0].equalsIgnoreCase("setconfig") && args[1].equalsIgnoreCase("list")){           		
        			sender.sendMessage(ChatColor.AQUA + "=========== Config Sections: ===========");
            		for (String section:RedProtect.plugin.getConfig().getValues(false).keySet()){
            			if (section.contains("debug-messages") || section.contains("file-type")){
            				sender.sendMessage(ChatColor.GOLD + section + " : " + ChatColor.GREEN + RedProtect.plugin.getConfig().get(section).toString());
            			}            			
            		} 
            		sender.sendMessage(ChatColor.AQUA + "====================================");
            		return true;
                }  
        		
        		//rp clamilimit player
        		if  (args[0].equalsIgnoreCase("claimlimit") || args[0].equalsIgnoreCase("climit")  || args[0].equalsIgnoreCase("cl")){ 
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
                    sender.sendMessage(RPLang.get("cmdmanager.yourclaims") + currentUsed + RPLang.get("general.color") + "/" + ChatColor.GOLD + limit + RPLang.get("general.color"));
                    return true;
        		}
        		
        		if (args[0].equalsIgnoreCase("limit") || args[0].equalsIgnoreCase("limitremaining") || args[0].equalsIgnoreCase("remaining") || args[0].equalsIgnoreCase("l")) {
        			Player offp = RedProtect.serv.getOfflinePlayer(args[1]).getPlayer();
                	if (offp == null){
                		sender.sendMessage(RPLang.get("cmdmanager.noplayer.thisname").replace("{player}", args[1]));
                		return true;
                	}
                	int limit = RedProtect.ph.getPlayerLimit(offp);
                    if (limit < 0 || RedProtect.ph.hasPerm(offp, "redprotect.limit.blocks.unlimited")) {
                    	sender.sendMessage(RPLang.get("cmdmanager.nolimit"));
                        return true;
                    }
                    
                    int currentUsed = RedProtect.rm.getTotalRegionSize(RPUtil.PlayerToUUID(offp.getName()));
                    sender.sendMessage(RPLang.get("cmdmanager.yourarea") + currentUsed + RPLang.get("general.color") + "/" + ChatColor.GOLD + limit + RPLang.get("general.color"));
                    return true;
        		}
        		
        	}
               
        	if (args.length == 3){
        		//rp clamilimit player world
        		if  (args[0].equalsIgnoreCase("claimlimit") || args[0].equalsIgnoreCase("climit")  || args[0].equalsIgnoreCase("cl")){ 
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
                    sender.sendMessage(RPLang.get("cmdmanager.yourclaims") + currentUsed + RPLang.get("general.color") + "/" + ChatColor.GOLD + limit + RPLang.get("general.color"));
                    return true;
        		}
        		        		
        		if  (args[0].equalsIgnoreCase("setconfig")){
        			if (args[1].contains("debug-messages") || args[1].contains("file-type")){
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
        		if (args[0].equalsIgnoreCase("info")) {
        			if (Bukkit.getWorld(args[2]) != null){
        				Region r = RedProtect.rm.getRegion(args[1], Bukkit.getWorld(args[2]));
        				if (r != null){
        					sender.sendMessage(RPLang.get("general.color") + "--------------- [" + ChatColor.GOLD + r.getName() + RPLang.get("general.color") + "] ---------------");
        					sender.sendMessage(r.info());
        					sender.sendMessage(RPLang.get("general.color") + "----------------------------------");
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
        		if (args[0].equalsIgnoreCase("tp")){
        			//rp tp <player> <region> <world>
                	Player play = RedProtect.serv.getPlayer(args[1]);
                	if (play != null){                		
                		World w = RedProtect.serv.getWorld(args[3]);                		
                		if (w == null) {
                            sender.sendMessage(RPLang.get("cmdmanager.region.invalidworld"));
                            return true;
                        }
                    	Region region = RedProtect.rm.getRegion(args[2], w);
                    	if (region == null) {
                    		sender.sendMessage(RPLang.get("cmdmanager.region.doesntexist"));
                            return true;
                        } 
                    	
                		Location loc = null;
                    	int limit = w.getMaxHeight();
                    	if (w.getEnvironment().equals(Environment.NETHER)){
                    		limit = 124;
                    	}
                    	for (int i = limit; i > 0; i--){
                    		Material mat = w.getBlockAt(region.getCenterX(), i, region.getCenterZ()).getType();
                    		Material mat1 = w.getBlockAt(region.getCenterX(), i+1, region.getCenterZ()).getType();
                    		Material mat2 = w.getBlockAt(region.getCenterX(), i+2, region.getCenterZ()).getType();
                    		if ((!mat.equals(Material.LAVA) || !mat.equals(Material.STATIONARY_LAVA)) && !mat.equals(Material.AIR) && mat1.equals(Material.AIR) && mat2.equals(Material.AIR)){
                    			loc = new Location(w, region.getCenterX(), i+1, region.getCenterZ());            			
                    			break;
                    		}
                    	}                    	
                    	play.teleport(loc);
            			RPLang.sendMessage(play,RPLang.get("cmdmanager.region.tp") + " " + args[2]);     
            			sender.sendMessage(ChatColor.AQUA + "Player teleported to " + args[2]);
                		return true;
                	} else {
                		sender.sendMessage(RPLang.get("cmdmanager.noplayer.thisname").replace("{player}", args[1]));
                		HandleHelPage(sender, 1);
                		return true;
                	}
        		}          
        		
        		//rp flag info <region> <world>
        		if (args[0].equalsIgnoreCase("flag") && args[1].equalsIgnoreCase("info") ) {
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
    			if  (args[0].equalsIgnoreCase("flag")){
    				World w = RedProtect.serv.getWorld(args[4]);
        			if (w == null){
        				sender.sendMessage(RPLang.get("correct.usage") + ChatColor.YELLOW + " rp flag <regionName> <flag> <value> <world>");
        				return true;
        			}
        			Region r = RedProtect.rm.getRegion(args[1], w);
        			if (r != null && (RPConfig.getDefFlags().contains(args[2]) || RPConfig.AdminFlags.contains(args[2]))){
        				Object objflag = RPUtil.parseObject(args[3]);
        				r.setFlag(args[2], objflag);
        				if (objflag instanceof Boolean){
                			sender.sendMessage(RPLang.get("cmdmanager.region.flag.set").replace("{flag}", "'"+args[2]+"'") + " " + r.getFlagBool(args[2]));
                		} else {
                			sender.sendMessage(RPLang.get("cmdmanager.region.flag.set").replace("{flag}", "'"+args[2]+"'") + " " + r.getFlagString(args[2]));
                		}
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
        	if (args[0].equalsIgnoreCase("update") && player.hasPermission("redprotect.update")){
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
        	
        	if (args[0].equalsIgnoreCase("cancelbuy") && player.hasPermission("redprotect.eco.cancelbuy")){
        		if (!RedProtect.Vault){
        			return true;
        		}
        		Region r = RedProtect.rm.getTopRegion(player.getLocation());
        		if (r == null){
        			RPLang.sendMessage(player, "cmdmanager.region.todo.that");
    				return true;
        		}
        		
        		if (r.getCreator().equalsIgnoreCase(RPUtil.PlayerToUUID(player.getName()))){
        			if (r.isForSale()){
            			r.setFlag("for-sale", false);        			
            			r.setWelcome("");
            			if (r.getCreator() == null){
            				if (RPConfig.getEcoBool("rename-region")){
            					RedProtect.rm.renameRegion(RPUtil.nameGen(player.getName(),r.getWorld()), r);
            				}
            				r.setCreator(RPUtil.PlayerToUUID(player.getName()));        		
            				r.addOwner(r.getCreator());
            			} else {
            				if (RPConfig.getEcoBool("rename-region")){
            					RedProtect.rm.renameRegion(RPUtil.nameGen(RPUtil.UUIDtoPlayer(r.getCreator()),r.getWorld()),r);
            				}
            				r.addOwner(r.getCreator());
            			}        			
            			RPLang.sendMessage(player, "economy.region.cancelbuy");
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
        	        	
        	if (args[0].equalsIgnoreCase("value") && RedProtect.ph.hasPerm(player, "redprotect.admin.value")){
        		Region r = RedProtect.rm.getTopRegion(player.getLocation());
        		if (r != null){
        			if (r.getArea() < RPConfig.getEcoInt("max-area-toget-value")){
        				RPLang.sendMessage(player, RPLang.get("cmdmanager.value.is").replace("{value}", RPConfig.getEcoString("economy-symbol") + RPEconomy.getRegionValue(r) + " " +RPConfig.getEcoString("economy-name")));
            			return true;
        			} else {
        				RPLang.sendMessage(player, RPLang.get("cmdmanager.value.areabig").replace("{maxarea}", RPConfig.getEcoInt("max-area-toget-value").toString()));
        				return true;
        			}
        		} else {
    				RPLang.sendMessage(player, "cmdmanager.region.todo.that");
    				return true;
    			} 
        	}
        	
        	if (args[0].equalsIgnoreCase("panel") || args[0].equalsIgnoreCase("p")) {   
        		if (player.hasPermission("redprotect.own.panel")) {
        			Region r = RedProtect.rm.getTopRegion(player.getLocation());
        			if (r != null){
        				if (r.isOwner(player) || player.hasPermission("redprotect.admin.panel")){
        					if (r.getName().length() > 16){
        						RPGui gui = new RPGui(ChatColor.DARK_GREEN + r.getName().substring(0, 16) + " Flags!", player, r, RedProtect.plugin);
        						gui.open();
        					} else {
        						RPGui gui = new RPGui(ChatColor.DARK_GREEN + r.getName() + " Flags!", player, r, RedProtect.plugin);
        						gui.open();
        					}
                			return true;
        				} 
        			} else {
        				RPLang.sendMessage(player, "cmdmanager.region.todo.that");
        				return true;
        			}    
        		}    			            		       		
        	}
        	if (args[0].equalsIgnoreCase("save-all")) {   
        		if (player.hasPermission("redprotect.admin.save-all")) {
        			RedProtect.rm.saveAll();
        			RPLang.sendMessage(player,ChatColor.GREEN + "" + RedProtect.rm.getAllRegions().size() + " regions saved with success!");
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
        	if (args[0].equalsIgnoreCase("define")){
        		if (!player.hasPermission("redprotect.admin.define")) {
                    RPLang.sendMessage(player, "no.permission");
                    return true;
                }
                String name = RPUtil.nameGen(player.getName(), player.getWorld().getName());
                String creator = player.getUniqueId().toString();
                if (!RedProtect.OnlineMode){
                	creator = player.getName().toLowerCase();
            	}
                RegionBuilder rb2 = new DefineRegionBuilder(player, RedProtect.firstLocationSelections.get(player), RedProtect.secondLocationSelections.get(player), name, creator, new ArrayList<String>());
                if (rb2.ready()) {
                    Region r2 = rb2.build();
                    RPLang.sendMessage(player,RPLang.get("cmdmanager.region.created") + " " + r2.getName() + ".");
                    RedProtect.rm.add(r2, player.getWorld());
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
        	if (args[0].equalsIgnoreCase("reload") && player.hasPermission("redprotect.admin.reload")) {
        		RedProtect.plugin.getServer().getPluginManager().disablePlugin((Plugin)RedProtect.plugin);
    			RedProtect.plugin.getServer().getPluginManager().enablePlugin((Plugin)RedProtect.plugin);
        		RPLang.sendMessage(player, "cmdmanager.reloaded");
        		return true;
        	}
        	if (args[0].equalsIgnoreCase("wand") && player.hasPermission("redprotect.magicwand")) {
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
            if (args[0].equalsIgnoreCase("?") || args[0].equalsIgnoreCase("help")) {
                HandleHelPage(sender, 1);
                return true;
            }          
            if (args[0].equalsIgnoreCase("tutorial") || args[0].equalsIgnoreCase("tut")) {
                RPLang.sendMessage(player,"cmdmanager.tutorial");
                RPLang.sendMessage(player,"cmdmanager.tutorial1");
                RPLang.sendMessage(player,"cmdmanager.tutorial2");
                RPLang.sendMessage(player,"cmdmanager.tutorial3");
                RPLang.sendMessage(player,"cmdmanager.tutorial4");
                RPLang.sendMessage(player,"cmdmanager.tutorial5");
                return true;
            }
            if (args[0].equalsIgnoreCase("near") || args[0].equalsIgnoreCase("nr")) {
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
                            RPLang.sendMessage(player,RPLang.get("cmdmanager.region.name") + r.getName() + RPLang.get("general.color") + " | Center (§6X,Z"+RPLang.get("general.color")+"): §6" +  r.getCenterX() + ", "  + r.getCenterZ());
                            RPLang.sendMessage(player,RPLang.get("region.regions") + " " + regions.size());
                        }
                        RPLang.sendMessage(player,RPLang.get("general.color") + "------------------------------------");
                    }
                }
                else {
                    RPLang.sendMessage(player, "no.permission");
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("flag")) {
            	if (player.hasPermission("redprotect.own.flag")) {
        			Region r = RedProtect.rm.getTopRegion(player.getLocation());
        			if (r != null){
        				if (r.isOwner(player) || player.hasPermission("redprotect.admin.flag")){
        					if (r.getName().length() > 16){
        						RPGui gui = new RPGui(ChatColor.DARK_GREEN + r.getName().substring(0, 16) + " Flags!", player, r, RedProtect.plugin);
        						gui.open();
        					} else {
        						RPGui gui = new RPGui(ChatColor.DARK_GREEN + r.getName() + " Flags!", player, r, RedProtect.plugin);
        						gui.open();
        					}
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
        	if (args[0].equalsIgnoreCase("?") || args[0].equalsIgnoreCase("help")) {
        		try{
        			int page = Integer.parseInt(args[1]);
                    HandleHelPage(sender, page);
        		} catch (NumberFormatException e){
        			RPLang.sendMessage(player,RPLang.get("correct.usage") + " " + ChatColor.YELLOW + "/rp ? [page]");
        		}
                return true;
            }
        	
        	if (args[0].equalsIgnoreCase("define")){
        		if (!player.hasPermission("redprotect.admin.define")) {
                    RPLang.sendMessage(player, "no.permission");
                    return true;
                }
                String name = args[1];
                String creator = player.getUniqueId().toString();
                if (!RedProtect.OnlineMode){
                	creator = player.getName().toLowerCase();
            	}
                RegionBuilder rb2 = new DefineRegionBuilder(player, RedProtect.firstLocationSelections.get(player), RedProtect.secondLocationSelections.get(player), name, creator, new ArrayList<String>());
                if (rb2.ready()) {
                    Region r2 = rb2.build();
                    RPLang.sendMessage(player,RPLang.get("cmdmanager.region.created") + " " + r2.getName() + ".");
                    RedProtect.rm.add(r2, player.getWorld());
                }
                return true;
        	}
        	
            if (args[0].equalsIgnoreCase("redefine")) {
                if (!player.hasPermission("redprotect.admin.redefine")) {
                    RPLang.sendMessage(player, "no.permission");
                    return true;
                }
                String name = args[1];
                Region oldRect = RedProtect.rm.getRegion(name, player.getWorld());
                if (oldRect == null) {
                    RPLang.sendMessage(player, "cmdmanager.region.doesntexist");
                    return true;
                }
                RedefineRegionBuilder rb = new RedefineRegionBuilder(player, oldRect, RedProtect.firstLocationSelections.get(player), RedProtect.secondLocationSelections.get(player));
                if (rb.ready()) {
                    Region r2 = rb.build();
                    RPLang.sendMessage(player,RPLang.get("cmdmanager.region.redefined") + " " + r2.getName() + ".");
                    RedProtect.rm.remove(oldRect);
                    RedProtect.rm.add(r2, player.getWorld());
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
        			if (section.contains("debug-messages") || section.contains("file-type")){
        				RPLang.sendMessage(player,ChatColor.GOLD + section + " : " + ChatColor.GREEN + RedProtect.plugin.getConfig().get(section).toString());
        			}         			
        		} 
        		RPLang.sendMessage(player,ChatColor.AQUA + "====================================");
        		return true;
            }
            
            if (args[0].equalsIgnoreCase("setcreator")) {
            	Region r = RedProtect.rm.getTopRegion(player.getLocation());
            	if (r != null && player.hasPermission("redprotect.admin.setcreator")){
            		String old = RPUtil.UUIDtoPlayer(r.getCreator());
            		r.setCreator(RPUtil.PlayerToUUID(args[1]));   
            		RPLang.sendMessage(player, RPLang.get("cmdmanager.creatorset").replace("{old}", old).replace("{new}", RPUtil.UUIDtoPlayer(r.getCreator())));            		
            	}
            	return true;
        	}
        }
        
        if (args.length == 3) {        	
        	if (args[0].equalsIgnoreCase("define")){
        		if (!player.hasPermission("redprotect.admin.define")) {
                    RPLang.sendMessage(player, "no.permission");
                    return true;
                }
                String name = args[1];
                String creator = player.getUniqueId().toString();
                List<String> addedOwners = new ArrayList<String>();
                addedOwners.add(RPUtil.PlayerToUUID(args[2]));
                if (!RedProtect.OnlineMode){
                	creator = player.getName().toLowerCase();
            	}                
                RegionBuilder rb2 = new DefineRegionBuilder(player, RedProtect.firstLocationSelections.get(player), RedProtect.secondLocationSelections.get(player), name, creator, addedOwners);
                if (rb2.ready()) {
                    Region r2 = rb2.build();
                    RPLang.sendMessage(player,RPLang.get("cmdmanager.region.created") + " " + r2.getName() + ".");
                    RedProtect.rm.add(r2, player.getWorld());
                }
                return true;
        	}
        	
        	// - /rp copyflag from to
    		if  (args[0].equalsIgnoreCase("copyflag")){
    			if (!player.hasPermission("redprotect.admin.copyflag")) {
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
        

    	if (args[0].equalsIgnoreCase("buy") && player.hasPermission("redprotect.eco.buy")){
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
				return true;    			
    		}    				    		
    	}
        
        if (args[0].equalsIgnoreCase("sell") && player.hasPermission("redprotect.eco.sell")){  
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
        		sellHandler(r, player, r.getCreator(), RPEconomy.getRegionValue(r));
        		return true;
        	}        	
        	
        	if (args.length == 2){         
        		// rp sell <value/player>
        		try {
        			Double value = Double.valueOf(args[1]);
    				if (player.hasPermission("redprotect.eco.setvalue")){
    					sellHandler(r, player, RPUtil.PlayerToUUID(r.getCreator()), value);
    					return true;
    				}    				
    			} catch (NumberFormatException e){
    				if (player.hasPermission("redprotect.eco.others")){
    					sellHandler(r, player, RPUtil.PlayerToUUID(args[1]), RPEconomy.getRegionValue(r));
            			return true;
                	}   				
    			}
        	} 
        	
        	if (args.length == 3){   
        		// rp sell player value
        		try {
        			Double value = Double.valueOf(args[2]);
    				if (player.hasPermission("redprotect.eco.setvalue")){
    					sellHandler(r, player, RPUtil.PlayerToUUID(args[1]), value);
    					return true;
    				}    				
    			} catch (NumberFormatException e){    
    				RPLang.sendMessage(player, "cmdmanager.eco.notdouble");
            		return true;
    			}
        	}
        }
        
        if (args[0].equalsIgnoreCase("tp")) {
        	if (args.length == 1) {
        		RPLang.sendMessage(player, "cmdmanager.help.tp");
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
            	// /rp tp <player> <region> <world>
            	Player play = RedProtect.serv.getPlayer(args[1]);
            	if (play != null){
            		handletp(player, args[2], args[3], play);
            		return true;
            	} else {
            		RPLang.sendMessage(player, RPLang.get("cmdmanager.noplayer.thisname").replace("{player}", args[1]));
            		RPLang.sendMessage(player, "cmdmanager.help.tp");
            		return true;
            	}
            }
        }
        
        if (args[0].equalsIgnoreCase("limit") || args[0].equalsIgnoreCase("limitremaining") || args[0].equalsIgnoreCase("remaining") || args[0].equalsIgnoreCase("l")) {
            if (!RedProtect.ph.hasPerm(player, "redprotect.own.limit")) {
                RPLang.sendMessage(player, "no.permission");
                return true;
            }
            
            if (args.length == 1) {
            	int limit = RedProtect.ph.getPlayerLimit(player);
                if (limit < 0 || RedProtect.ph.hasPerm(player, "redprotect.limit.blocks.unlimited")) {
                    RPLang.sendMessage(player,"cmdmanager.nolimit");
                    return true;
                }
                String uuid = player.getUniqueId().toString();
                if (!RedProtect.OnlineMode){
                	uuid = player.getName().toLowerCase();
                }
                int currentUsed = RedProtect.rm.getTotalRegionSize(uuid);
                RPLang.sendMessage(player,RPLang.get("cmdmanager.yourarea") + currentUsed + RPLang.get("general.color") + "/" + ChatColor.GOLD + limit + RPLang.get("general.color"));
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
            	int limit = RedProtect.ph.getPlayerLimit(offp);
                if (limit < 0 || RedProtect.ph.hasPerm(offp, "redprotect.limit.blocks.unlimited")) {
                    RPLang.sendMessage(player, "cmdmanager.nolimit");
                    return true;
                }
                
                int currentUsed = RedProtect.rm.getTotalRegionSize(RPUtil.PlayerToUUID(offp.getName()));
                RPLang.sendMessage(player,RPLang.get("cmdmanager.yourarea") + currentUsed + RPLang.get("general.color") + "/" + ChatColor.GOLD + limit + RPLang.get("general.color"));
                return true;
            }
            RPLang.sendMessage(player,RPLang.get("correct.usage") + " " + RPLang.get("cmdmanager.help.limit"));
            return true;
        }        
        
        if (args[0].equalsIgnoreCase("claimlimit") || args[0].equalsIgnoreCase("climit")  || args[0].equalsIgnoreCase("cl")) {
            if (!RedProtect.ph.hasPerm(player, "redprotect.own.claimlimit")) {
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
                RPLang.sendMessage(player,RPLang.get("cmdmanager.yourclaims") + currentUsed + RPLang.get("general.color") + "/" + ChatColor.GOLD + limit + RPLang.get("general.color"));
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
                RPLang.sendMessage(player,RPLang.get("cmdmanager.yourclaims") + currentUsed + RPLang.get("general.color") + "/" + ChatColor.GOLD + limit + RPLang.get("general.color"));
                return true;
            }
            RPLang.sendMessage(player,RPLang.get("correct.usage") + " " + RPLang.get("cmdmanager.help.claimlimit"));
            return true;
        }      
        
        if (args[0].equalsIgnoreCase("welcome") || args[0].equalsIgnoreCase("wel")) {
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
        if (args[0].equalsIgnoreCase("priority") || args[0].equalsIgnoreCase("prior")) {
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
        
        if (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("del")) {
            if (args.length == 1) {
                handleDelete(player);
                return true;
            }
            if (args.length == 2) {
                handleDeleteName(player, args[1]);
                return true;
            }
            RPLang.sendMessage(player,RPLang.get("correct.usage") + " " + RPLang.get("cmdmanager.help.delete"));
            return true;
        }
        if (args[0].equalsIgnoreCase("i") || args[0].equalsIgnoreCase("info")) {
            if (args.length == 1) {
                handleInfoTop(player);
                return true;
            }
            if (args.length == 2) {
                handleInfo(player, args[1]);
                return true;
            }
            RPLang.sendMessage(player,RPLang.get("correct.usage") + " " + RPLang.get("cmdmanager.help.info"));
            return true;
        }
        if (args[0].equalsIgnoreCase("am") || args[0].equalsIgnoreCase("addmember")) {
            if (args.length == 2) {
                handleAddMember(player, args[1]);
                return true;
            }
            if (args.length == 3) {
                handleAddMember(player, args[1]);
                return true;
            }
            RPLang.sendMessage(player,RPLang.get("correct.usage") + " " + RPLang.get("cmdmanager.help.addmember"));
            return true;
        }
        if (args[0].equalsIgnoreCase("ao") || args[0].equalsIgnoreCase("addowner")) {
            if (args.length == 2) {
                handleAddOwner(player, args[1]);
                return true;
            }
            if (args.length == 3) {
                handleAddOwner(player, args[1]);
                return true;
            }
            RPLang.sendMessage(player,RPLang.get("correct.usage") + " " + RPLang.get("cmdmanager.help.addowner"));
            return true;
        }
        if (args[0].equalsIgnoreCase("rm") || args[0].equalsIgnoreCase("removemember")) {
            if (args.length == 2) {
                handleRemoveMember(player, args[1]);
                return true;
            }
            if (args.length == 3) {
                handleRemoveMember(player, args[1]);
                return true;
            }
            RPLang.sendMessage(player,RPLang.get("correct.usage") + " " + RPLang.get("cmdmanager.help.removemember"));
            return true;
        }
        if (args[0].equalsIgnoreCase("ro") || args[0].equalsIgnoreCase("removeowner")) {
            if (args.length == 2) {
                handleRemoveOwner(player, args[1]);
                return true;
            }
            if (args.length == 3) {
                handleRemoveOwner(player, args[1]);
                return true;
            }
            RPLang.sendMessage(player,RPLang.get("correct.usage") + " " + RPLang.get("cmdmanager.help.removeowner"));
            return true;
        }
        if (args[0].equalsIgnoreCase("rn") || args[0].equalsIgnoreCase("rename")) {
            if (args.length == 2) {
                handleRename(player, args[1]);
                return true;
            }
            RPLang.sendMessage(player,RPLang.get("correct.usage") + " " + RPLang.get("cmdmanager.help.rename"));
            return true;
        }
        if (args[0].equalsIgnoreCase("fl") || args[0].equalsIgnoreCase("flag")) {
        	Region r = RedProtect.rm.getTopRegion(player.getLocation());
        	
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
        if (args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("ls")) {
            if (args.length == 1) {
                handleList(player, RPUtil.PlayerToUUID(player.getName()), 1);
                return true;
            }
            if (args.length == 2) {
                handleList(player, RPUtil.PlayerToUUID(args[1]), 1);
                return true;
            }            
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
        RPLang.sendMessage(player,RPLang.get("correct.command") + " " + ChatColor.YELLOW + "/rp ?");   
        return true;
    }
	
	@SuppressWarnings("deprecation")
	private void buyHandler(Player player, Double value, Region r) {		
		       		
		if (RPUtil.PlayerToUUID(player.getName()).equalsIgnoreCase(r.getCreator())){
			RPLang.sendMessage(player, "economy.region.buy.own");
			return;
		}
		
		Double money = RedProtect.econ.getBalance(player);
		if (money >= value){
			String creator = r.getCreator();
			String rname = r.getName();
			if (RPEconomy.BuyRegion(r, RPUtil.PlayerToUUID(player.getName()))){
				RedProtect.econ.withdrawPlayer(player, value);
				OfflinePlayer offp = RedProtect.serv.getOfflinePlayer(RPUtil.UUIDtoPlayer(creator));
				if (!creator.equals("server") && offp != null){
					RedProtect.econ.depositPlayer(offp, value);
					if (offp.isOnline()){
						RPLang.sendMessage((Player) offp, RPLang.get("economy.region.buy.bought").replace("{player}", player.getName()).replace("{region}", rname).replace("{world}", r.getWorld()));
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

	private void sellHandler(Region r, Player player, String creator, Double value) {       		
		
		if (r.isOwner(player) || player.hasPermission("redprotect.eco.admin")){
			if (RPEconomy.putToSell(r, creator, value)){
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
    	if (RedProtect.ph.hasRegionPerm(p, "delete", r)) {
    		if (r != null){
    			r.setPrior(prior);
    			RPLang.sendMessage(p,RPLang.get("cmdmanager.region.priority.set").replace("{region}", r.getName()) + " " + prior);
    		} else {
    			RPLang.sendMessage(p, "cmdmanager.region.todo.that");
        		return;
    		}
    	}
	}
	
    private static void handlePriority(Player p, int prior) {
    	Region r = RedProtect.rm.getTopRegion(p.getLocation());
    	if (RedProtect.ph.hasRegionPerm(p, "delete", r)) {
    		if (r != null){
    			r.setPrior(prior);
    			RPLang.sendMessage(p,RPLang.get("cmdmanager.region.priority.set").replace("{region}", r.getName()) + " " + prior);
    		} else {
    			RPLang.sendMessage(p, "cmdmanager.region.todo.that");
        		return;
    		}
    	}		
	}

    private static void handleDelete(Player p) {
		Region r = RedProtect.rm.getTopRegion(p.getLocation());
        if (RedProtect.ph.hasRegionPerm(p, "delete", r)) {
            if (r == null) {
                sendNotInRegionMessage(p);
                return;
            }
            String rname = r.getName();
            RedProtect.rm.remove(r);
            RPLang.sendMessage(p,RPLang.get("cmdmanager.region.deleted") +" "+ rname);
        }
        else {
            sendNoPermissionMessage(p);
        }
    }
	
	private static void handleDeleteName(Player p, String rname) {
		Region r = RedProtect.rm.getRegion(rname, p.getWorld());
        if (RedProtect.ph.hasRegionPerm(p, "delete", r)) {
            if (r == null) {
            	RPLang.sendMessage(p, "cmdmanager.region.doesntexist");
                return;
            }
            RedProtect.rm.remove(r);
            RPLang.sendMessage(p,RPLang.get("cmdmanager.region.deleted") +" "+ rname);
        }
        else {
            sendNoPermissionMessage(p);
        }
    }
    
	private static void handleInfoTop(Player p) {  
    	Region r = RedProtect.rm.getTopRegion(p.getLocation());
    	Map<Integer, Region> groupr = RedProtect.rm.getGroupRegion(p.getLocation());
    	if (RedProtect.ph.hasRegionPerm(p, "info", r) || r.isForSale()) {
            if (r == null) {
                sendNotInRegionMessage(p);
                return;
            }
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
    
	private static void handleInfo(Player p, String region) {
    	Region r = RedProtect.rm.getRegion(region, p.getWorld());
    	if (RedProtect.ph.hasRegionPerm(p, "info", r) || r.isForSale()) {
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
    
	private static void handleAddMember(Player p, String sVictim) {
    	Region r = RedProtect.rm.getTopRegion(p.getLocation());
        if (RedProtect.ph.hasRegionPerm(p, "addmember", r)) {
            if (r == null) {
                sendNotInRegionMessage(p);
                return;
            }
            String VictimUUID = RPUtil.PlayerToUUID(sVictim);
            if (RPUtil.UUIDtoPlayer(VictimUUID) == null){
            	RPLang.sendMessage(p,RPLang.get("cmdmanager.noplayer.thisname").replace("{player}", sVictim));
            	return;
            }

            Player pVictim = RedProtect.serv.getPlayer(sVictim);
            
            if (r.getOwners().contains(VictimUUID)) {
                r.removeOwner(VictimUUID);
                r.addMember(VictimUUID);
                RPLang.sendMessage(p,RPLang.get("general.color") + sVictim + " " + RPLang.get("cmdmanager.region.owner.demoted") + " " + r.getName());
                if (pVictim != null && pVictim.isOnline()) {
                    pVictim.sendMessage(RPLang.get("cmdmanager.region.owner.youdemoted").replace("{region}", r.getName()) + " " + p.getName());
                }
            } else if (!r.getMembers().contains(VictimUUID)) {
                r.addMember(VictimUUID);
                RPLang.sendMessage(p,RPLang.get("general.color") + sVictim + " " + RPLang.get("cmdmanager.region.member.added") + " " + r.getName());
                if (pVictim != null && pVictim.isOnline()) {
                    pVictim.sendMessage(RPLang.get("cmdmanager.region.member.youadded").replace("{region}", r.getName()) + " " + p.getName());
                }
            } else {
                RPLang.sendMessage(p,ChatColor.RED + sVictim + " " + RPLang.get("cmdmanager.region.member.already"));
            }
        } else {
            sendNoPermissionMessage(p);
        }
    }
    
	private static void handleAddOwner(Player p, String sVictim) {
    	Region r = RedProtect.rm.getTopRegion(p.getLocation());
        if (RedProtect.ph.hasRegionPerm(p, "addowner", r)) {
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
            if (!r.getOwners().contains(VictimUUID)) {
                r.addOwner(VictimUUID);
                RPLang.sendMessage(p,RPLang.get("general.color") + sVictim + " " + RPLang.get("cmdmanager.region.owner.added") + " " + r.getName());
                if (pVictim != null && pVictim.isOnline()) {
                    pVictim.sendMessage(RPLang.get("cmdmanager.region.owner.youadded").replace("{region}", r.getName()) + " " + p.getName());
                }
            }
            else {
                RPLang.sendMessage(p,ChatColor.RED + sVictim + " " + RPLang.get("cmdmanager.region.owner.already"));
            }
        }
        else {
            sendNoPermissionMessage(p);
        }
    }
    
	private static void handleRemoveMember(Player p, String sVictim) {
    	Region r = RedProtect.rm.getTopRegion(p.getLocation());
        if (RedProtect.ph.hasRegionPerm(p, "removemember", r)) {
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
            if (r.getMembers().contains(VictimUUID) || r.getOwners().contains(VictimUUID)) {
                RPLang.sendMessage(p,RPLang.get("general.color") + sVictim + " " + RPLang.get("cmdmanager.region.member.removed") + " " + r.getName());
                r.removeMember(VictimUUID);
                if (pVictim != null && pVictim.isOnline()) {
                    pVictim.sendMessage(RPLang.get("cmdmanager.region.member.youremoved").replace("{region}", r.getName()) + " " + p.getName());
                }
            } else {
                RPLang.sendMessage(p,ChatColor.RED + sVictim + " " + RPLang.get("cmdmanager.region.member.notmember"));
            }
        }
        else {
            sendNoPermissionMessage(p);
        }
    }
    
	private static void handleRemoveOwner(Player p, String sVictim) {
    	Region r = RedProtect.rm.getTopRegion(p.getLocation());
		Region rLow = RedProtect.rm.getLowRegion(p.getLocation());
		Map<Integer,Region> regions = RedProtect.rm.getGroupRegion(p.getLocation());
        if (RedProtect.ph.hasRegionPerm(p, "removeowner", r)) {
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

            if (!RedProtect.ph.hasRegionPerm(p, "removeowner", rLow) || rLow != r && (regions.size() > 1 && rLow.getOwners().contains(VictimUUID))){
        		RPLang.sendMessage(p,RPLang.get("cmdmanager.region.owner.cantremove.lowregion").replace("{player}", sVictim) + " " +rLow.getName());
            	return;
        	}	  
            if (r.getOwners().contains(VictimUUID)) {
                if (r.ownersSize() > 1) {
                    RPLang.sendMessage(p,RPLang.get("general.color") + sVictim + " " + RPLang.get("cmdmanager.region.member.added") + " " +r.getName());
                    r.removeOwner(VictimUUID);
                    r.addMember(VictimUUID);
                    if (pVictim != null && pVictim.isOnline()) {
                        pVictim.sendMessage(RPLang.get("cmdmanager.region.owner.removed").replace("{region}", r.getName())+ " " + p.getName());
                    }
                }
                else {
                    RPLang.sendMessage(p,RPLang.get("cmdmanager.region.owner.cantremove").replace("{player}", p.getName()));
                }
            }
            else {
                RPLang.sendMessage(p,ChatColor.RED + sVictim + " " + RPLang.get("cmdmanager.region.owner.notowner"));
            }
        }
        else {
            sendNoPermissionMessage(p);
        }
    }
    
	private static void handleRename(Player p, String newName) {
    	Region r = RedProtect.rm.getTopRegion(p.getLocation());
        if (RedProtect.ph.hasRegionPerm(p, "rename", r)) {
            if (r == null) {
                sendNotInRegionMessage(p);
                return;
            }
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
            RedProtect.rm.renameRegion(newName, r);
            RPLang.sendMessage(p,RPLang.get("cmdmanager.region.rename.newname") + " " + newName);
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
            if (r.isOwner(p) || RedProtect.ph.hasPerm(p, "redprotect.admin.flag."+flag)) {
            	
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
            	
            	if (!value.equals("")){
            		if (RPConfig.getDefFlagsValues().containsKey(flag)) {
            			if (objflag instanceof Boolean){
            				r.setFlag(flag, objflag);
                            RPLang.sendMessage(p,RPLang.get("cmdmanager.region.flag.set").replace("{flag}", "'"+flag+"'") + " " + r.getFlagBool(flag));
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
                		if (objflag instanceof Boolean){
                			RPLang.sendMessage(p,RPLang.get("cmdmanager.region.flag.set").replace("{flag}", "'"+flag+"'") + " " + r.getFlagBool(flag));
                		} else {
                			RPLang.sendMessage(p,RPLang.get("cmdmanager.region.flag.set").replace("{flag}", "'"+flag+"'") + " " + r.getFlagString(flag));
                		}                        
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
            		if (RPConfig.getDefFlagsValues().containsKey(flag)) {
            			r.setFlag(flag, !r.getFlagBool(flag));
                        RPLang.sendMessage(p,RPLang.get("cmdmanager.region.flag.set").replace("{flag}", "'"+flag+"'") + " " + r.getFlagBool(flag));
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
		if (flag.equalsIgnoreCase("effects")){                				
			RPLang.sendMessage(p,RPLang.get("cmdmanager.region.flag.usage"+flag));
		} else if (flag.equalsIgnoreCase("allow-enter-items")){                				
			RPLang.sendMessage(p,RPLang.get("cmdmanager.region.flag.usage"+flag));    
		} else if (flag.equalsIgnoreCase("deny-enter-items")){                				
			RPLang.sendMessage(p,RPLang.get("cmdmanager.region.flag.usage"+flag));
		} else if (flag.equalsIgnoreCase("allow-cmds") || flag.equalsIgnoreCase("deny-cmds") || flag.equalsIgnoreCase("allow-break") || flag.equalsIgnoreCase("allow-place")){                				
			RPLang.sendMessage(p,RPLang.get("cmdmanager.region.flag.usage"+flag));
		} else {
			RPLang.sendMessage(p,RPLang.get("cmdmanager.region.flag.usagetruefalse").replace("{flag}", flag));
		} 		
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
		if ((flag.equalsIgnoreCase("treefarm") || flag.equalsIgnoreCase("invincible") || flag.equalsIgnoreCase("minefarm")) && !(value instanceof Boolean)){
			return false;
		}
		if (flag.equalsIgnoreCase("allow-enter-items") || flag.equalsIgnoreCase("deny-enter-items") || flag.equalsIgnoreCase("allow-place") || flag.equalsIgnoreCase("allow-break")){
			String[] valida = ((String)value).replace(" ", "").split(",");
			for (String item:valida){
				if (Material.getMaterial(item.toUpperCase()) == null){
					return false;
				}
			}
		}
		if (flag.equalsIgnoreCase("allow-cmds") || flag.equalsIgnoreCase("deny-cmds")){
			if (!(value instanceof String)){
				return false;
			}
			try{
				String[] cmds = ((String)value).replace(" ", "").split(",");
				return cmds.length > 0;
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
        if (RedProtect.ph.hasPerm(p, "redprotect.admin.list")) {
        	getRegionforList(p, uuid, Page);
        	return;
        } else if (RedProtect.ph.hasPerm(p, "redprotect.own.list")){
        	getRegionforList(p, uuid, Page);
        	return;
        }
        RPLang.sendMessage(p, "no.permission");
    }
    
	private static void getRegionforList(Player p, String uuid, int Page){
    	Set<Region> regions = RedProtect.rm.getRegions(uuid);
    	String pname = RPUtil.UUIDtoPlayer(uuid);
        int length = regions.size();
        if (pname == null || length == 0) {
            RPLang.sendMessage(p, "cmdmanager.player.noregions");
            return;
        }
        else {
            RPLang.sendMessage(p,RPLang.get("cmdmanager.region.created.list") + " " +pname);
            Iterator<Region> i = regions.iterator();
            if (Page == 0){Page = 1;}
            int max = (10*Page);
            int min = max-10;
            int count = 0;
            int last = 0;
            while (i.hasNext()) {
            	String info = i.next().info();
            	if (count >= min && count <= max){
            		p.sendMessage(RPLang.get("general.color") + "------------------------------------");
                    p.sendMessage(RPLang.get("general.color") + "["+(count+1)+"] " + info);     
                    last = count;
                    
            	}
            	count++;
            }      
            if (max > count){min = 0;}
        	p.sendMessage(RPLang.get("general.color") + "------------- "+(min+1)+"-"+(last+1)+"/"+count+" --------------");
        	if (count > max){
            	p.sendMessage(RPLang.get("cmdmanager.region.listpage.more").replace("{player}", pname + " " + (Page+1)));
            } else {
            	if (Page != 1) {p.sendMessage(RPLang.get("cmdmanager.region.listpage.nomore"));}
            }        	
        }
        return;
    }
    
	private static void handleWelcome(Player p, String wMessage) {
    	Region r = RedProtect.rm.getTopRegion(p.getLocation());
    	if (RedProtect.ph.hasRegionPerm(p, "welcome", r)) {    		
        	if (r != null){
        		if (wMessage.equals("")){
        			r.setWelcome("");
        			RPLang.sendMessage(p, "cmdmanager.region.welcomeoff");
        			return;
        		} else if (wMessage.equals("hide ")){
        			r.setWelcome(wMessage);
        			RPLang.sendMessage(p, "cmdmanager.region.welcomehide");
        			return;
        		} else {
        			r.setWelcome(wMessage);
                	RPLang.sendMessage(p,RPLang.get("cmdmanager.region.welcomeset") + " "+ wMessage.replaceAll("(?i)&([a-f0-9k-or])", "§$1"));
                	return;        		
        		}
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
    		RPLang.sendMessage(p, "cmdmanager.region.doesntexist");
            return;
        }          
    	
    	if (play == null) {
    		if (!RedProtect.ph.hasRegionPerm(p, "tp", region)){
    			RPLang.sendMessage(p, "no.permission");
                return;
    		}
    	} else {
    		if (!RedProtect.ph.hasPerm(p, "redprotect.tp.other")) {
        		RPLang.sendMessage(p, "no.permission");
                return;
            }    		
        }      

    	Location loc = null;
    	int limit = w.getMaxHeight();
    	if (w.getEnvironment().equals(Environment.NETHER)){
    		limit = 124;
    	}
    	for (int i = limit; i > 0; i--){
    		Material mat = w.getBlockAt(region.getCenterX(), i, region.getCenterZ()).getType();
    		Material mat1 = w.getBlockAt(region.getCenterX(), i+1, region.getCenterZ()).getType();
    		Material mat2 = w.getBlockAt(region.getCenterX(), i+2, region.getCenterZ()).getType();
    		if ((!mat.equals(Material.LAVA) || !mat.equals(Material.STATIONARY_LAVA)) && !mat.equals(Material.AIR) && mat1.equals(Material.AIR) && mat2.equals(Material.AIR)){
    			loc = new Location(w, region.getCenterX(), i+1, region.getCenterZ());            			
    			break;
    		}
    	}
    	if (loc != null){
    		if (play != null){
    			play.teleport(loc);
    			RPLang.sendMessage(play, RPLang.get("cmdmanager.region.tp") + " " + rname);   			
    			RPLang.sendMessage(p, RPLang.get("cmdmanager.region.tpother") + " " + rname);
    		} else {
    			tpWait(p, loc, rname);
    		}      		
			return;
    	}
    	return;
	}
	
	private static void tpWait(final Player p, final Location loc, final String rname){
		if (p.hasPermission("redprotect.admin.tp")){
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
                		p.teleport(loc);
                		RPLang.sendMessage(p,RPLang.get("cmdmanager.region.tp") + " " + rname);
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
				List<String> owners = new ArrayList<String>();
				String owner = RPUtil.PlayerToUUID(c.getOwner());
				owners.add(owner.toString());
				World w = RedProtect.serv.getWorld(c.getWorldName());
				Chunk chunk = w.getChunkAt(c.getX(), c.getZ());
				int x = chunk.getBlock(7, 50, 7).getX();
				int z = chunk.getBlock(7, 50, 7).getZ();
				String regionName = "";
				
				int in = 0;
	            while (true) {
	            	int is = String.valueOf(in).length();
	                if (RPUtil.UUIDtoPlayer(owner).length() > 13) {
	                    regionName = RPUtil.UUIDtoPlayer(owner).substring(0, 14-is) + "_" + in;
	                }
	                else {
	                    regionName = RPUtil.UUIDtoPlayer(owner) + "_" + in;
	                }
	                if (RedProtect.rm.getRegion(regionName, w) == null) {
	                    break;
	                }
	                ++in;
	            }
	            
				Region r = new Region(regionName, owners, new ArrayList<String>(), owners.get(0), new int[] {x + 8, x + 8, x - 7, x - 7}, new int[] {z + 8, z + 8, z - 7, z - 7}, 0, c.getWorldName(), RPUtil.DateNow(), RPConfig.getDefFlagsValues(), "", 0.0);
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
		sender.sendMessage(ChatColor.DARK_GRAY + "/rp <command>|<alias> | <> = Required | [] = Optional");
        
		if (sender instanceof Player){
			Player player = (Player)sender;		
			int i = 0;
			for (String key:RPLang.helpStrings()){
				if (RedProtect.ph.hasHelpPerm(player, key)) {
					i++;					
					
					if (i > (page*5)-5 && i <= page*5){
						player.sendMessage(RPLang.get("cmdmanager.help."+key));
					} 
					if (i > page*5){
						sender.sendMessage(RPLang.get("general.color") + "------------------------------------");
						player.sendMessage(RPLang.get("cmdmanager.page").replace("{page}", ""+(page+1)));
						break;
					}
				}
			}
		} else {
			sender.sendMessage(ChatColor.GOLD + "rp setconfig list");
			sender.sendMessage(ChatColor.GOLD + "rp setconfig <Config-Section> <Value>");
			sender.sendMessage(ChatColor.GOLD + "rp info <region> <world>");
			sender.sendMessage(ChatColor.GOLD + "rp flag <regionName> <Flag> <Value> <World>");
			sender.sendMessage(ChatColor.GOLD + "rp flag info <region> <world>");
			sender.sendMessage(ChatColor.GOLD + "rp tp <playerName> <regionName> <World>");			
			sender.sendMessage(ChatColor.GOLD + "rp limit <playerName>");
			sender.sendMessage(ChatColor.GOLD + "rp claimlimit <playerName> [world]");
			sender.sendMessage(ChatColor.GOLD + "rp list-all");
			sender.sendMessage(ChatColor.GOLD + "rp ymlTomysql");
			sender.sendMessage(ChatColor.GOLD + "rp save-all");
			sender.sendMessage(ChatColor.GOLD + "rp load-all");
			sender.sendMessage(ChatColor.GOLD + "rp reload");
			
		}
		sender.sendMessage(RPLang.get("general.color") + "------------------------------------");
	}
    
}
