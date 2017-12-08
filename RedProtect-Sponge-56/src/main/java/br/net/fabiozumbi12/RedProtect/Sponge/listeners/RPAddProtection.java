package br.net.fabiozumbi12.RedProtect.Sponge.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.text.Text;

import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;

public class RPAddProtection {
	
	private static final HashMap<Player,String> chatSpam = new HashMap<>();
	private static final HashMap<String,Integer> msgSpam = new HashMap<>();
	private static final HashMap<Player,Integer> UrlSpam = new HashMap<>();
	private static final List<String> muted = new ArrayList<>();
	
	public RPAddProtection(){
		RedProtect.logger.debug("default","Loaded RPAddProtection...");
	}
	
	@Listener(order=Order.EARLY)
	public void onChat(MessageChannelEvent.Chat e, @First Player p){
		String msg = e.getFormatter().getBody().toText().toPlain();
		
		if (msg.length() <= 1){
			return;
		}
		
		//mute check
		if (muted.contains(p.getName())){
			p.sendMessage(RedProtect.cfgs.getProtMsg("chat-protection","anti-ip","mute-msg"));
			e.setCancelled(true);
			return;
		}
		
		//antispam
		if (RedProtect.cfgs.getProtBool("chat-protection","antispam","enable") && !p.hasPermission("redprotect.chat.bypass-spam")){	
			
			//check spam messages
			if (!chatSpam.containsKey(p)){
				chatSpam.put(p, msg);				
				Sponge.getScheduler().createSyncExecutor(RedProtect.plugin).schedule(() -> {
                    if (chatSpam.containsKey(p)){
                        chatSpam.remove(p);
                    }
                },RedProtect.cfgs.getProtInt("chat-protection","antispam","time-beteween-messages"), TimeUnit.SECONDS);
			} else if (!chatSpam.get(p).equalsIgnoreCase(msg)){				
				p.sendMessage(RedProtect.cfgs.getProtMsg("chat-protection","antispam","colldown-msg"));
				e.setCancelled(true);
				return;
			}
			
			//check same message frequency
			if (!msgSpam.containsKey(msg)){
				msgSpam.put(msg, 1);
				final String nmsg = msg;
				Sponge.getScheduler().createSyncExecutor(RedProtect.plugin).schedule(() -> {
                    if (msgSpam.containsKey(nmsg)){
                        msgSpam.remove(nmsg);
                    }
                },RedProtect.cfgs.getProtInt("chat-protection","antispam","time-beteween-same-messages"), TimeUnit.SECONDS);
			} else {
				msgSpam.put(msg, msgSpam.get(msg)+1);				
				if (msgSpam.get(msg) >= RedProtect.cfgs.getProtInt("chat-protection","antispam","count-of-same-message")){
					Sponge.getCommandManager().process(Sponge.getServer().getConsole(),RedProtect.cfgs.getProtString("chat-protection","antispam","cmd-action").replace("{player}", p.getName()));
					msgSpam.remove(msg);
				} else {
					p.sendMessage(RedProtect.cfgs.getProtMsg("chat-protection","antispam","wait-message"));					
				}
				e.setCancelled(true);
				return;
			}			
		}
		
		//censor
		if (RedProtect.cfgs.getProtBool("chat-protection","censor","enable") && !p.hasPermission("redprotect.chat.bypass-censor")){
			int act = 0;
			for (String word:RedProtect.cfgs.getProtStringList("chat-protection","censor","replace-words")){
				if (!StringUtils.containsIgnoreCase(msg, word)){
					continue;
				} 				
				String replaceby = RedProtect.cfgs.getProtString("chat-protection","censor","by-word");
				if (RedProtect.cfgs.getProtBool("chat-protection","censor","replace-by-symbol")){
					replaceby = word.replaceAll("(?s).", RedProtect.cfgs.getProtString("chat-protection","censor","by-symbol"));
				}
				
				if (!RedProtect.cfgs.getProtBool("chat-protection","censor","replace-partial-word")){
					msg = msg.replaceAll("(?i)"+"\\b"+Pattern.quote(word)+"\\b", replaceby);
					if (RedProtect.cfgs.getProtBool("chat-protection","censor","action","partial-words")){
						act++;
					}
				} else {
					msg = msg.replaceAll("(?i)"+word, replaceby);
					act++;
				}				
			}
			if (act > 0){
				String action = RedProtect.cfgs.getProtString("chat-protection","censor","action","cmd");
				if (action.length() > 1){
					Sponge.getCommandManager().process(Sponge.getServer().getConsole(), action.replace("{player}", p.getName()));				
				}
			}
		}
		
		String regexIP = RedProtect.cfgs.getProtString("chat-protection","anti-ip","custom-ip-regex");
		String regexUrl = RedProtect.cfgs.getProtString("chat-protection","anti-ip","custom-url-regex");
		
		//check ip and website
		if (RedProtect.cfgs.getProtBool("chat-protection","anti-ip","enable") && !p.hasPermission("redprotect.chat.bypass-anti-ip")){
			
			//check whitelist
			boolean cont = true;
			for (String check:RedProtect.cfgs.getProtStringList("chat-protection","anti-ip","whitelist-words")){
				if (Pattern.compile(check).matcher(msg).find()){
                    cont = false;
                    break;
				}
			}

			if (cont){
                //continue
                if (Pattern.compile(regexIP).matcher(msg).find()){
                    addURLspam(p);
                    if (RedProtect.cfgs.getProtString("chat-protection","anti-ip","cancel-or-replace").equalsIgnoreCase("cancel")){
                        p.sendMessage(RedProtect.cfgs.getProtMsg("chat-protection","anti-ip","cancel-msg"));
                        e.setCancelled(true);
                        return;
                    } else {
                        msg = msg.replaceAll(regexIP, RedProtect.cfgs.getProtString("chat-protection","anti-ip","replace-by-word"));
                    }
                }
                if (Pattern.compile(regexUrl).matcher(msg).find()){
                    addURLspam(p);
                    if (RedProtect.cfgs.getProtString("chat-protection","anti-ip","cancel-or-replace").equalsIgnoreCase("cancel")){
                        p.sendMessage(RedProtect.cfgs.getProtMsg("chat-protection","anti-ip","cancel-msg"));
                        e.setCancelled(true);
                        return;
                    } else {
                        msg = msg.replaceAll(regexUrl, RedProtect.cfgs.getProtString("chat-protection","anti-ip","replace-by-word"));
                    }
                }

                for (String word:RedProtect.cfgs.getProtStringList("chat-protection","anti-ip","check-for-words")){
                    if (Pattern.compile("(?i)"+"\\b"+word+"\\b").matcher(msg).find()){
                        addURLspam(p);
                        if (RedProtect.cfgs.getProtString("chat-protection","anti-ip","cancel-or-replace").equalsIgnoreCase("cancel")){
                            p.sendMessage(RedProtect.cfgs.getProtMsg("chat-protection","anti-ip","cancel-msg"));
                            e.setCancelled(true);
                            return;
                        } else {
                            msg = msg.replaceAll("(?i)"+word, RedProtect.cfgs.getProtString("chat-protection","anti-ip","replace-by-word"));
                        }
                    }
                }
            }
		}	
		
		//capitalization verify
		if (RedProtect.cfgs.getProtBool("chat-protection","chat-enhancement","enable") && !p.hasPermission("redprotect.chat.bypass-enhancement")){
			int lenght = RedProtect.cfgs.getProtInt("chat-protection","chat-enhancement","minimum-lenght");
			if (!Pattern.compile(regexIP).matcher(msg).find() && !Pattern.compile(regexUrl).matcher(msg).find() && msg.length() > lenght){
				msg = msg.replaceAll("([.!?])\\1+", "$1").replaceAll(" +", " ").substring(0, 1).toUpperCase()+msg.substring(1);
				if (RedProtect.cfgs.getProtBool("chat-protection","chat-enhancement","end-with-dot") && !msg.endsWith("?") && !msg.endsWith("!") && !msg.endsWith(".") && msg.split(" ").length > 2){
					msg = msg+".";
				}
			}				
		}
		
		//anti-caps
		if (RedProtect.cfgs.getProtBool("chat-protection","caps-filter","enable") && !p.hasPermission("redprotect.chat.bypass-enhancement")){
			int lenght = RedProtect.cfgs.getProtInt("chat-protection","caps-filter","minimum-lenght");
			int msgUppers = msg.replaceAll("\\p{P}", "").replaceAll("[a-z ]+", "").length();
			if (!Pattern.compile(regexIP).matcher(msg).find() && !Pattern.compile(regexUrl).matcher(msg).find() && msgUppers >= lenght){
				msg = msg.substring(0, 1).toUpperCase()+msg.substring(1).toLowerCase();
			}
		}
		
		//antiflood
		if (RedProtect.cfgs.getProtBool("chat-protection","anti-flood","enable")){						
			for (String flood:RedProtect.cfgs.getProtStringList("chat-protection","anti-flood","whitelist-flood-characs")){
				if (Pattern.compile("(["+flood+"])\\1+").matcher(msg).find()){
					e.getFormatter().setBody(Text.of(msg));
					return;
				}
			}
			msg = msg.replaceAll("([A-Za-z])\\1+", "$1$1");
		}		
		e.getFormatter().setBody(Text.of(msg));
	}
	
	private static void addURLspam(final Player p){
		if (RedProtect.cfgs.getProtBool("chat-protection","anti-ip","punish","enable")){
			if (!UrlSpam.containsKey(p)){
				UrlSpam.put(p, 1);
			} else {
				UrlSpam.put(p, UrlSpam.get(p)+1);
				//p.sendMessage(RPUtil.toText("UrlSpam: "+UrlSpam.get(p)));
				if (UrlSpam.get(p) >= RedProtect.cfgs.getProtInt("chat-protection","anti-ip","punish","max-attempts")){
					if (RedProtect.cfgs.getProtString("chat-protection","anti-ip","punish","mute-or-cmd").equalsIgnoreCase("mute")){
						muted.add(p.getName());
						p.sendMessage(RedProtect.cfgs.getProtMsg("chat-protection","anti-ip","punish","mute-msg"));
						Sponge.getScheduler().createSyncExecutor(RedProtect.plugin).schedule(() -> {
                            if (muted.contains(p.getName())){
                                muted.remove(p.getName());
                                p.sendMessage(RedProtect.cfgs.getProtMsg("chat-protection","anti-ip","punish","unmute-msg"));
                            }
                        },RedProtect.cfgs.getProtInt("chat-protection","anti-ip","punish","mute-duration"),TimeUnit.MINUTES);
					} else {
						Sponge.getCommandManager().process(Sponge.getServer().getConsole(),RedProtect.cfgs.getProtString("chat-protection","anti-ip","punish","cmd-punish"));
					}	
					UrlSpam.remove(p);
				}
			}
		}		
	}
}
