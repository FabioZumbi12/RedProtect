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

package br.net.fabiozumbi12.RedProtect.Bukkit.config;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPUtil;
import br.net.fabiozumbi12.RedProtect.Core.helpers.Replacer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static br.net.fabiozumbi12.RedProtect.Bukkit.commands.CommandHandlers.getCmd;
import static br.net.fabiozumbi12.RedProtect.Bukkit.commands.CommandHandlers.getCmdAlias;

public class RPLang {

    private static final Properties Lang = new Properties() {
        @Override
        public synchronized Enumeration<Object> keys() {
            return Collections.enumeration(new TreeSet<>(super.keySet()));
        }
    };
    private static final Properties BaseLang = new Properties() {
        @Override
        public synchronized Enumeration<Object> keys() {
            return Collections.enumeration(new TreeSet<>(super.keySet()));
        }
    };
    private static final HashMap<Player, String> DelayedMessage = new HashMap<>();
    private static String pathLang;

    public static SortedSet<String> helpStrings() {
        SortedSet<String> values = new TreeSet<>();
        for (Object help : Lang.keySet()) {
            if (help.toString().startsWith("cmdmanager.help.")) {
                values.add(help.toString().replace("cmdmanager.help.", ""));
            }
        }
        return values;
    }

    public static void init() {
        String resLang = "lang" + RedProtect.get().cfgs.getString("language") + ".properties";
        pathLang = RedProtect.get().getDataFolder() + File.separator + resLang;

        File lang = new File(pathLang);
        if (!lang.exists()) {
            if (RedProtect.get().getResource("assets/redprotect/" + resLang) == null) {
                resLang = "langEN-US.properties";
                pathLang = RedProtect.get().getDataFolder() + File.separator + resLang;
            }
            RPUtil.saveResource("/assets/redprotect/" + resLang, null, new File(RedProtect.get().getDataFolder(), resLang));
            RedProtect.get().logger.info("Created language file: " + pathLang);
        }

        loadLang();
        loadBaseLang();
        RedProtect.get().logger.info("Language file loaded - Using: " + RedProtect.get().cfgs.getString("language"));
    }

    private static void loadBaseLang() {
        BaseLang.clear();
        try {
            InputStream fileInput = RedProtect.get().getResource("assets/redprotect/langEN-US.properties");
            Reader reader = new InputStreamReader(fileInput, StandardCharsets.UTF_8);
            BaseLang.load(reader);
        } catch (Exception e) {
            e.printStackTrace();
        }
        updateLang();
    }

    private static void loadLang() {
        Lang.clear();
        try {
            FileInputStream fileInput = new FileInputStream(pathLang);
            Reader reader = new InputStreamReader(fileInput, StandardCharsets.UTF_8);
            Lang.load(reader);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (Lang.get("_lang.version") != null) {
            int langv = Integer.parseInt(Lang.get("_lang.version").toString().replace(".", ""));
            int rpv = Integer.parseInt(RedProtect.get().getDescription().getVersion().replace(".", ""));
            if (RedProtect.get().getDescription().getVersion().length() > Lang.get("_lang.version").toString().length()) {
                langv = Integer.parseInt(Lang.get("_lang.version").toString().replace(".", "") + 0);
            }
            if (langv < rpv || langv == 0) {
                RedProtect.get().logger.warning("Your lang file is outdated. Probably need strings updates!");
                RedProtect.get().logger.warning("Lang file version: " + Lang.get("_lang.version"));
                Lang.put("_lang.version", RedProtect.get().getDescription().getVersion());
            }
        }
    }

    private static void updateLang() {
        BaseLang.forEach((key, value) -> {
            if (!Lang.containsKey(key)) {
                Lang.put(key, value);
            }
        });

        //remove invalid entries
        if (Lang.entrySet().removeIf(k -> !BaseLang.containsKey(k.getKey())))
            RedProtect.get().logger.warning("- Removed invalid entries from language files");

        if (!Lang.containsKey("_lang.version"))
            Lang.put("_lang.version", RedProtect.get().getDescription().getVersion());

        try {
            Lang.store(new OutputStreamWriter(new FileOutputStream(pathLang), StandardCharsets.UTF_8), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String get(String key) {
        String FMsg;

        if (Lang.get(key) == null) {
            FMsg = "&c&oMissing language string for " + ChatColor.GOLD + key;
        } else {
            FMsg = Lang.get(key).toString();
        }

        FMsg = ChatColor.translateAlternateColorCodes('&', FMsg);

        return FMsg;
    }

    public static void sendMessage(CommandSender sender, String key) {
        sendMessage(sender, key, new Replacer[0]);
    }

    public static void sendMessage(CommandSender sender, String key, Replacer<String, String>[] replaces) {
        if (sender instanceof Player && DelayedMessage.containsKey(sender) && DelayedMessage.get(sender).equals(key)) {
            return;
        }

        if (Lang.get(key) == null) {
            sender.sendMessage(get("_redprotect.prefix") + " " + ChatColor.translateAlternateColorCodes('&', key));
        } else if (get(key).equalsIgnoreCase("")) {
            return;
        } else {
            String message = get(key);
            for (Replacer<String, String> replacer:replaces){
                message = message.replace(replacer.getPlaceholder(), replacer.getValue());
            }
            sender.sendMessage(get("_redprotect.prefix") + " " + message);
        }

        if (sender instanceof Player) {
            final Player p = (Player) sender;
            DelayedMessage.put(p, key);
            Bukkit.getScheduler().scheduleSyncDelayedTask(RedProtect.get(), () -> DelayedMessage.remove(p), 20);
        }
    }

    public static void sendCommandHelp(CommandSender sender, String cmd, boolean usage) {
        if (usage) sendMessage(sender, "correct.usage");
        sender.sendMessage(get("cmdmanager.help." + cmd).replace("{cmd}", getCmd(cmd)).replace("{alias}", getCmdAlias(cmd)));
    }

    public static String translBool(String bool) {
        return get("region." + bool);
    }

    public static String translBool(Boolean bool) {
        return get("region." + bool.toString());
    }

    public static boolean containsValue(String value) {
        return Lang.containsValue(value);
    }
}
