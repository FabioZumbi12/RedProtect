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

package br.net.fabiozumbi12.RedProtect.Sponge.config;

import br.net.fabiozumbi12.RedProtect.Core.helpers.Replacer;
import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import br.net.fabiozumbi12.RedProtect.Sponge.helpers.RPUtil;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static br.net.fabiozumbi12.RedProtect.Sponge.commands.CommandHandlers.getCmd;
import static br.net.fabiozumbi12.RedProtect.Sponge.commands.CommandHandlers.getCmdAlias;

public class RPLang {
    private RPLang() {
    }

    private static final Properties loadedLang = new Properties() {
        @Override
        public synchronized Enumeration<Object> keys() {
            return Collections.enumeration(new TreeSet<>(super.keySet()));
        }
    };
    private static final Properties baseLang = new Properties() {
        @Override
        public synchronized Enumeration<Object> keys() {
            return Collections.enumeration(new TreeSet<>(super.keySet()));
        }
    };
    private static final HashMap<Player, String> delayedMessage = new HashMap<>();
    private static String pathLang;

    public static SortedSet<String> helpStrings() {
        SortedSet<String> values = new TreeSet<>();
        for (Object help : loadedLang.keySet()) {
            if (help.toString().startsWith("cmdmanager.help.")) {
                values.add(help.toString().replace("cmdmanager.help.", ""));
            }
        }
        return values;
    }

    public static void init() {
        String resLang = "lang" + RedProtect.get().config.root().language + ".properties";
        pathLang = RedProtect.get().configDir + File.separator + resLang;

        File lang = new File(pathLang);
        if (!lang.exists()) {
            if (!RedProtect.get().container.getAsset(resLang).isPresent()) {
                resLang = "langEN-US.properties";
                pathLang = RedProtect.get().configDir + File.separator + resLang;
            }

            //create lang file
            try {
                RedProtect.get().container.getAsset(resLang).get().copyToDirectory(RedProtect.get().configDir.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            RedProtect.get().logger.info("Created config file: " + pathLang);
        }

        loadLang();
        loadBaseLang();
        RedProtect.get().logger.info("Language file loaded - Using: " + RedProtect.get().config.root().language);
    }

    private static void loadBaseLang() {
        baseLang.clear();
        try {
            baseLang.load(RedProtect.get().container.getAsset("langEN-US.properties").get().getUrl().openStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
        updateLang();
    }

    private static void loadLang() {
        loadedLang.clear();
        try {
            FileInputStream fileInput = new FileInputStream(pathLang);
            Reader reader = new InputStreamReader(fileInput, StandardCharsets.UTF_8);
            loadedLang.load(reader);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (loadedLang.get("_lang.version") != null) {
            int langv = Integer.parseInt(loadedLang.get("_lang.version").toString().replace(".", ""));
            int rpv = Integer.parseInt(RedProtect.get().container.getVersion().get().replace(".", ""));
            if (langv < rpv || langv == 0) {
                RedProtect.get().logger.warning("Your lang file is outdated. Probably need strings updates!");
                RedProtect.get().logger.warning("Lang file version: " + loadedLang.get("_lang.version"));
                loadedLang.put("_lang.version", RedProtect.get().container.getVersion().get());
            }
        }
    }

    private static void updateLang() {
        baseLang.forEach((key, value) -> {
            if (!loadedLang.containsKey(key)) {
                loadedLang.put(key, value);
            }
        });

        //remove invalid entries
        if (loadedLang.entrySet().removeIf(k -> !baseLang.containsKey(k.getKey())))
            RedProtect.get().logger.warning("- Removed invalid entries from language files");

        if (!loadedLang.containsKey("_lang.version"))
            loadedLang.put("_lang.version", RedProtect.get().container.getVersion().get());

        try {
            loadedLang.store(new OutputStreamWriter(new FileOutputStream(pathLang), StandardCharsets.UTF_8), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String get(String key) {
        String FMsg;

        if (loadedLang.get(key) == null) {
            FMsg = "&c&oMissing language string for &4" + key;
        } else {
            FMsg = loadedLang.get(key).toString();
        }
        return FMsg;
    }

    public static void sendMessage(CommandSource sender, String key) {
        sendMessage(sender, key, new Replacer[0]);
    }

    public static void sendMessage(CommandSource sender, String key, Replacer[] replaces) {
        if (sender instanceof Player && delayedMessage.containsKey(sender) && delayedMessage.get(sender).equals(key)) {
            return;
        }

        if (loadedLang.get(key) == null) {
            sender.sendMessage(RPUtil.toText(get("_redprotect.prefix") + " " + key));
        } else if (get(key).equalsIgnoreCase("")) {
            return;
        } else {
            String message = get(key);
            for (Replacer replacer : replaces) {
                message = message.replace(replacer.getPlaceholder(), replacer.getValue());
            }
            sender.sendMessage(RPUtil.toText(get("_redprotect.prefix") + " " + message));
        }

        if (sender instanceof Player) {
            delayedMessage.put((Player) sender, key);
            Sponge.getScheduler().createSyncExecutor(RedProtect.get().container).schedule(() -> {
                delayedMessage.remove(sender);
            }, 1, TimeUnit.SECONDS);
        }
    }

    public static void sendCommandHelp(CommandSource sender, String cmd, boolean usage) {
        if (usage) sendMessage(sender, "correct.usage");
        sender.sendMessage(RPUtil.toText(get("cmdmanager.help." + cmd).replace("{cmd}", getCmd(cmd)).replace("{alias}", getCmdAlias(cmd))));
    }

    public static String translBool(String bool) {
        return get("region." + bool);
    }

    public static String translBool(Boolean bool) {
        return get("region." + bool.toString());
    }

    public static boolean containsValue(String value) {
        return loadedLang.containsValue(value);
    }
}
