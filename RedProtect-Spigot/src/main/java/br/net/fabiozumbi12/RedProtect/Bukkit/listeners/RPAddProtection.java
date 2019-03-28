/*
 *
 * Copyright (c) 2019 - @FabioZumbi12
 * Last Modified: 28/03/19 20:18
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
 *
 */

package br.net.fabiozumbi12.RedProtect.Bukkit.listeners;

import br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPUtil;
import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPConfig;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class RPAddProtection implements Listener {

    private final HashMap<Player, String> chatSpam = new HashMap<>();
    private final HashMap<String, Integer> msgSpam = new HashMap<>();
    private final HashMap<Player, Integer> UrlSpam = new HashMap<>();
    private final List<String> muted = new ArrayList<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        final Player p = e.getPlayer();
        final String msg = e.getMessage();

        //mute check
        if (muted.contains(p.getName())) {
            p.sendMessage(RPConfig.getProtMsg("chat-protection.anti-ip.mute-msg"));
            e.setCancelled(true);
            return;
        }

        //antispam
        if (RPConfig.getProtBool("chat-protection.antispam.enabled") && !p.hasPermission("redprotect.chat.bypass-spam")) {

            //check spam messages
            if (!chatSpam.containsKey(p)) {
                chatSpam.put(p, msg);
                Bukkit.getScheduler().scheduleSyncDelayedTask(RedProtect.get(), () -> chatSpam.remove(p), RPConfig.getProtInt("chat-protection.antispam.time-beteween-messages") * 20);
            } else if (!chatSpam.get(p).equalsIgnoreCase(msg)) {
                p.sendMessage(RPConfig.getProtMsg("chat-protection.antispam.colldown-msg"));
                e.setCancelled(true);
                return;
            }

            //check same message frequency
            if (!msgSpam.containsKey(msg)) {
                msgSpam.put(msg, 1);
                Bukkit.getScheduler().scheduleSyncDelayedTask(RedProtect.get(), () -> {
                    msgSpam.remove(msg);
                }, RPConfig.getProtInt("chat-protection.antispam.time-beteween-same-messages") * 20);
            } else {
                msgSpam.put(msg, msgSpam.get(msg) + 1);
                e.setCancelled(true);
                if (msgSpam.get(msg) >= RPConfig.getProtInt("chat-protection.antispam.count-of-same-message")) {
                    RPUtil.performCommand(RedProtect.get().serv.getConsoleSender(), RPConfig.getProtString("chat-protection.antispam.cmd-action").replace("{player}", p.getName()));
                    msgSpam.remove(msg);
                } else {
                    p.sendMessage(RPConfig.getProtMsg("chat-protection.antispam.wait-message"));
                }
                e.setCancelled(true);
                return;
            }
        }

        String nmsg = msg;

        //censor
        if (RPConfig.getProtBool("chat-protection.censor.enabled") && !p.hasPermission("redprotect.chat.bypass-censor")) {
            int act = 0;
            for (String word : RPConfig.getProtStringList("chat-protection.censor.replace-words")) {
                if (!StringUtils.containsIgnoreCase(nmsg, word)) {
                    continue;
                }
                String replaceby = RPConfig.getProtString("chat-protection.censor.by-word");
                if (RPConfig.getProtBool("chat-protection.censor.replace-by-symbol")) {
                    replaceby = word.replaceAll("(?s).", RPConfig.getProtString("chat-protection.censor.by-symbol"));
                }

                if (!RPConfig.getProtBool("chat-protection.censor.replace-partial-word")) {
                    nmsg = nmsg.replaceAll("(?i)" + "\\b" + Pattern.quote(word) + "\\b", replaceby);
                } else {
                    nmsg = nmsg.replaceAll("(?i)" + word, replaceby);
                }
                act++;
            }
            if (act > 0) {
                String action = RPConfig.getProtString("chat-protection.censor.cmd-action");
                if (!action.isEmpty()) {
                    RPUtil.performCommand(RedProtect.get().serv.getConsoleSender(), action);
                }
            }
        }

        String regexIP = RPConfig.getProtString("chat-protection.anti-ip.custom-ip-regex");
        String regexUrl = RPConfig.getProtString("chat-protection.anti-ip.custom-url-regex");

        //check ip and website
        if (RPConfig.getProtBool("chat-protection.anti-ip.enabled") && !p.hasPermission("redprotect.chat.bypass-anti-ip")) {

            //check whitelist
            for (String check : RPConfig.getProtStringList("chat-protection.anti-ip.whitelist-words")) {
                if (Pattern.compile(check).matcher(nmsg).find()) {
                    return;
                }
            }

            //continue
            if (Pattern.compile(regexIP).matcher(nmsg).find()) {
                addURLspam(p);
                if (RPConfig.getProtString("chat-protection.anti-ip.cancel-or-replace").equalsIgnoreCase("cancel")) {
                    e.setCancelled(true);
                    p.sendMessage(RPConfig.getProtMsg("chat-protection.anti-ip.cancel-msg"));
                    return;
                } else {
                    nmsg = nmsg.replaceAll(regexIP, RPConfig.getProtMsg("chat-protection.anti-ip.replace-by-word"));
                }
            }
            if (Pattern.compile(regexUrl).matcher(nmsg).find()) {
                addURLspam(p);
                if (RPConfig.getProtString("chat-protection.anti-ip.cancel-or-replace").equalsIgnoreCase("cancel")) {
                    e.setCancelled(true);
                    p.sendMessage(RPConfig.getProtMsg("chat-protection.anti-ip.cancel-msg"));
                    return;
                } else {
                    nmsg = nmsg.replaceAll(regexUrl, RPConfig.getProtMsg("chat-protection.anti-ip.replace-by-word"));
                }
            }

            for (String word : RPConfig.getProtStringList("chat-protection.anti-ip.check-for-words")) {
                if (Pattern.compile("(?i)" + "\\b" + word + "\\b").matcher(nmsg).find()) {
                    addURLspam(p);
                    if (RPConfig.getProtString("chat-protection.anti-ip.cancel-or-replace").equalsIgnoreCase("cancel")) {
                        e.setCancelled(true);
                        p.sendMessage(RPConfig.getProtMsg("chat-protection.anti-ip.cancel-msg"));
                        return;
                    } else {
                        nmsg = nmsg.replaceAll("(?i)" + word, RPConfig.getProtMsg("chat-protection.anti-ip.replace-by-word"));
                    }
                }
            }
        }

        //capitalization verify
        if (RPConfig.getProtBool("chat-protection.chat-enhancement.enabled") && !p.hasPermission("redprotect.chat.bypass-enhancement")) {
            if (!Pattern.compile(regexIP).matcher(nmsg).find() && !Pattern.compile(regexUrl).matcher(nmsg).find()) {
                nmsg = nmsg.replaceAll("([.!?])\\1+", "$1").replaceAll(" +", " ").substring(0, 1).toUpperCase() + nmsg.substring(1).toLowerCase();
                if (RPConfig.getProtBool("chat-protection.chat-enhancement.end-with-dot") && !nmsg.endsWith("?") && !nmsg.endsWith("!") && !nmsg.endsWith(".") && nmsg.split(" ").length > 2) {
                    nmsg = nmsg + ".";
                }

                if (RPConfig.getProtBool("chat-protection.chat-enhancement.colorize-playernames")) {
                    for (Player play : Bukkit.getOnlinePlayers()) {
                        if (StringUtils.containsIgnoreCase(nmsg, play.getName()) && !play.equals(p)) {
                            nmsg = nmsg.replaceAll("(?i)\\b" + play.getName() + "\\b",
                                    RPConfig.getProtMsg("chat-protection.chat-enhancement.colorize-prefix-color") + play.getName() + ChatColor.RESET);
                            break;
                        }
                    }
                }

                if (RPConfig.getProtBool("chat-protection.chat-enhancement.anti-flood.enable")) {
                    for (String flood : RPConfig.getProtStringList("chat-protection.chat-enhancement.anti-flood.whitelist-flood-characs")) {
                        if (Pattern.compile("([" + flood + "])\\1+").matcher(nmsg).find()) {
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

    private void addURLspam(final Player p) {
        if (RPConfig.getProtBool("chat-protection.anti-ip.punish.enable")) {
            if (!UrlSpam.containsKey(p)) {
                UrlSpam.put(p, 1);
            } else {
                UrlSpam.put(p, UrlSpam.get(p) + 1);
                //p.sendMessage("UrlSpam: "+UrlSpam.get(p));
                if (UrlSpam.get(p) >= RPConfig.getProtInt("chat-protection.anti-ip.punish.max-attempts")) {
                    if (RPConfig.getProtString("chat-protection.anti-ip.punish.mute-or-cmd").equalsIgnoreCase("mute")) {
                        muted.add(p.getName());
                        p.sendMessage(RPConfig.getProtMsg("chat-protection.anti-ip.punish.mute-msg"));
                        Bukkit.getScheduler().scheduleSyncDelayedTask(RedProtect.get(), () -> {
                            if (muted.contains(p.getName())) {
                                muted.remove(p.getName());
                                p.sendMessage(RPConfig.getProtMsg("chat-protection.anti-ip.punish.unmute-msg"));
                            }
                        }, (RPConfig.getProtInt("chat-protection.anti-ip.punish.mute-duration") * 60) * 20);
                    } else {
                        RPUtil.performCommand(RedProtect.get().serv.getConsoleSender(), RPConfig.getProtString("chat-protection.anti-ip.punish.cmd-punish"));
                    }
                    UrlSpam.remove(p);
                }
            }
        }
    }
}
