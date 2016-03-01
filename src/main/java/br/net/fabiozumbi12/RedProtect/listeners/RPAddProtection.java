package br.net.fabiozumbi12.RedProtect.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import br.net.fabiozumbi12.RedProtect.RPUtil;
import br.net.fabiozumbi12.RedProtect.RedProtect;
import br.net.fabiozumbi12.RedProtect.config.RPConfig;

public class RPAddProtection implements Listener{
	
	private HashMap<Player,String> chatSpam = new HashMap<Player,String>();
	private HashMap<String,Integer> msgSpam = new HashMap<String,Integer>();
	private HashMap<Player,Integer> UrlSpam = new HashMap<Player,Integer>();
	private List<String> muted = new ArrayList<String>();

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlayerChat(AsyncPlayerChatEvent e){
		final Player p = e.getPlayer();
		final String msg = e.getMessage();
		
		//mute check
		if (muted.contains(p.getName())){
			p.sendMessage(RPConfig.getProtMsg("chat-protection.anti-ip.mute-msg"));
			e.setCancelled(true);
			return;
		}
		
		//antispam
		if (RPConfig.getProtBool("chat-protection.antispam.enabled")){	
			
			//check spam messages
			if (!chatSpam.containsKey(p)){
				chatSpam.put(p, msg);				
				Bukkit.getScheduler().scheduleSyncDelayedTask(RedProtect.plugin, new Runnable() { 
					public void run() {
						if (chatSpam.containsKey(p)){
							chatSpam.remove(p);
						}						
					}						
				},RPConfig.getProtInt("chat-protection.antispam.time-beteween-messages")*20);
			} else if (!chatSpam.get(p).equalsIgnoreCase(msg)){				
				p.sendMessage(RPConfig.getProtMsg("chat-protection.antispam.colldown-msg"));
				e.setCancelled(true);
				return;
			}
			
			//check same message frequency
			if (!msgSpam.containsKey(msg)){
				msgSpam.put(msg, 1);
				Bukkit.getScheduler().scheduleSyncDelayedTask(RedProtect.plugin, new Runnable() { 
					public void run() {
						if (msgSpam.containsKey(msg)){
							msgSpam.remove(msg);
						}						
					}						
					},RPConfig.getProtInt("chat-protection.antispam.time-beteween-same-messages")*20);
			} else {
				msgSpam.put(msg, msgSpam.get(msg)+1);
				e.setCancelled(true);				
				if (msgSpam.get(msg) >= RPConfig.getProtInt("chat-protection.antispam.count-of-same-message")){
					RPUtil.performCommand(RedProtect.serv.getConsoleSender(),RPConfig.getProtString("chat-protection.antispam.cmd-action").replace("{player}", p.getName()));
					msgSpam.remove(msg);
				} else {
					p.sendMessage(RPConfig.getProtMsg("chat-protection.antispam.wait-message"));
				}
				return;
			}			
		}
		
		String nmsg = msg;
		
		//censor
		if (RPConfig.getProtBool("chat-protection.censor.enabled")){
			for (String word:RPConfig.getProtStringList("chat-protection.censor.replace-words")){
				if (!StringUtils.containsIgnoreCase(nmsg, word)){
					continue;
				} 				
				String replaceby = RPConfig.getProtString("chat-protection.censor.by-word");
				if (RPConfig.getProtBool("chat-protection.censor.replace-by-symbol")){
					replaceby = word.replaceAll("(?s).", RPConfig.getProtString("chat-protection.censor.by-symbol"));
				}
				
				if (!RPConfig.getProtBool("chat-protection.censor.replace-partial-word")){
					nmsg = nmsg.replaceAll("(?i)"+"\\b"+Pattern.quote(word)+"\\b", replaceby);
				} else {
					nmsg = nmsg.replaceAll("(?i)"+word, replaceby);
				}
			}
		}
		
		//check ip and website
		if (RPConfig.getProtBool("chat-protection.anti-ip.enabled")){
			
			//check whitelist
			for (String check:RPConfig.getProtStringList("chat-protection.anti-ip.whitelist-words")){
				if (Pattern.compile(check).matcher(nmsg).find()){	
					return;
				}
			}
			
			String regexIP = RPConfig.getProtString("chat-protection.anti-ip.custom-ip-regex");
			String regexUrl = RPConfig.getProtString("chat-protection.anti-ip.custom-url-regex");			
			
			//continue
			if (Pattern.compile(regexIP).matcher(nmsg).find()){	
				addURLspam(p);
				if (RPConfig.getProtString("chat-protection.anti-ip.cancel-or-replace").equalsIgnoreCase("cancel")){
					e.setCancelled(true);
					p.sendMessage(RPConfig.getProtMsg("chat-protection.anti-ip.cancel-msg"));
					return;
				} else {
					nmsg = nmsg.replaceAll(regexIP, RPConfig.getProtMsg("chat-protection.anti-ip.replace-by-word"));
				}
			}
			if (Pattern.compile(regexUrl).matcher(nmsg).find()){
				addURLspam(p);
				if (RPConfig.getProtString("chat-protection.anti-ip.cancel-or-replace").equalsIgnoreCase("cancel")){
					e.setCancelled(true);
					p.sendMessage(RPConfig.getProtMsg("chat-protection.anti-ip.cancel-msg"));
					return;
				} else {
					nmsg = nmsg.replaceAll(regexUrl, RPConfig.getProtMsg("chat-protection.anti-ip.replace-by-word"));
				}
			}
			
			for (String word:RPConfig.getProtStringList("chat-protection.anti-ip.check-for-words")){
				if (Pattern.compile("(?i)"+"\\b"+word+"\\b").matcher(nmsg).find()){
					addURLspam(p);
					if (RPConfig.getProtString("chat-protection.anti-ip.cancel-or-replace").equalsIgnoreCase("cancel")){
						e.setCancelled(true);
						p.sendMessage(RPConfig.getProtMsg("chat-protection.anti-ip.cancel-msg"));
						return;
					} else {
						nmsg = nmsg.replaceAll("(?i)"+word, RPConfig.getProtMsg("chat-protection.anti-ip.replace-by-word"));
					}
				}
			}			
			
			//capitalization verify
			if (RPConfig.getProtBool("chat-protection.chat-enhancement.enabled")){
				if (!Pattern.compile(regexIP).matcher(nmsg).find() && !Pattern.compile(regexUrl).matcher(nmsg).find()){
					nmsg = nmsg.replaceAll("([.!?])\\1+", "$1").replaceAll(" +", " ");
					String[] messages = nmsg.split("(?<=[.!?])");
					StringBuilder finalmsg = new StringBuilder(); 
					boolean first = true;
					for (String msgw:messages){
						if (msgw.length() <= 0){
							continue;
						}
						if (first){
							finalmsg.append(msgw.substring(0, 1).toUpperCase()+msgw.substring(1).toLowerCase());
							first = false;
						} else if (msgw.startsWith(" ")){
							finalmsg.append(msgw.substring(0, 2).toUpperCase()+msgw.substring(2).toLowerCase());
						} else {
							finalmsg.append(" "+msgw.substring(0, 1).toUpperCase()+msgw.substring(1).toLowerCase());
						}
					}					
					nmsg = finalmsg.toString();
					if (RPConfig.getProtBool("chat-protection.chat-enhancement.end-with-dot") && !nmsg.endsWith("?") && !nmsg.endsWith("!") && !nmsg.endsWith(".") && nmsg.split(" ").length > 2){
						nmsg = nmsg+".";
					}
					
					if (RPConfig.getProtBool("chat-protection.chat-enhancement.colorize-playernames")){
						for (Player play:Bukkit.getOnlinePlayers()){
							if (StringUtils.containsIgnoreCase(nmsg, play.getName()) && !play.equals(p)){
								nmsg = nmsg.replaceAll("(?i)\\b"+play.getName()+"\\b",
										RPConfig.getProtMsg("chat-protection.chat-enhancement.colorize-prefix-color")+play.getName()+ChatColor.RESET);
								break;
							}
						}
					}
					
					if (RPConfig.getProtBool("chat-protection.chat-enhancement.anti-flood.enable")){						
						for (String flood:RPConfig.getProtStringList("chat-protection.chat-enhancement.anti-flood.whitelist-flood-characs")){
							if (Pattern.compile("(["+flood+"])\\1+").matcher(nmsg).find()){
								e.setMessage(nmsg);	
								return;
							}
						}
						nmsg = nmsg.replaceAll("([A-Za-z])\\1+", "$1$1");
					}
				}				
			}
			
			e.setMessage(nmsg);			
		}
		
	}	
	
	private void addURLspam(final Player p){
		if (RPConfig.getProtBool("chat-protection.anti-ip.punish.enable")){
			if (!UrlSpam.containsKey(p)){
				UrlSpam.put(p, 1);
			} else {
				UrlSpam.put(p, UrlSpam.get(p)+1);
				p.sendMessage("UrlSpam: "+UrlSpam.get(p));
				if (UrlSpam.get(p) >= RPConfig.getProtInt("chat-protection.anti-ip.punish.max-attempts")){
					if (RPConfig.getProtString("chat-protection.anti-ip.punish.mute-or-cmd").equalsIgnoreCase("mute")){
						muted.add(p.getName());
						p.sendMessage(RPConfig.getProtMsg("chat-protection.anti-ip.punish.mute-msg"));
						Bukkit.getScheduler().scheduleSyncDelayedTask(RedProtect.plugin, new Runnable() { 
							public void run() {
								if (muted.contains(p.getName())){						
									muted.remove(p.getName());
									p.sendMessage(RPConfig.getProtMsg("chat-protection.anti-ip.punish.unmute-msg"));
								}
							}						
						},(RPConfig.getProtInt("chat-protection.anti-ip.punish.mute-duration")*60)*20);
					} else {
						RPUtil.performCommand(RedProtect.serv.getConsoleSender(),RPConfig.getProtString("chat-protection.anti-ip.punish.cmd-punish"));
					}	
					UrlSpam.remove(p);
				}
			}
		}		
	}
}
