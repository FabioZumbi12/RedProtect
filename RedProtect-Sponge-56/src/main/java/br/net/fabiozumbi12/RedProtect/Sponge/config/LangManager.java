/*
 * Copyright (c) 2020 - @FabioZumbi12
 * Last Modified: 02/07/2020 19:10.
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

package br.net.fabiozumbi12.RedProtect.Sponge.config;

import br.net.fabiozumbi12.RedProtect.Core.config.LangCore;
import br.net.fabiozumbi12.RedProtect.Core.helpers.CoreUtil;
import br.net.fabiozumbi12.RedProtect.Core.helpers.Replacer;
import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import br.net.fabiozumbi12.RedProtect.Sponge.commands.CommandHandlers;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.entity.living.player.Player;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static br.net.fabiozumbi12.RedProtect.Sponge.commands.CommandHandlers.getCmd;
import static br.net.fabiozumbi12.RedProtect.Sponge.commands.CommandHandlers.getCmdAlias;

public class LangManager extends LangCore {

    public LangManager() {
        String resLang = "lang" + RedProtect.get().getConfigManager().configRoot().language + ".properties";
        pathLang = RedProtect.get().configDir + File.separator + resLang;

        File lang = new File(pathLang);
        if (!lang.exists()) {
            try {
                if (RedProtect.get().container.getAsset(resLang).isPresent()) {
                    RedProtect.get().container.getAsset(resLang).get().copyToDirectory(RedProtect.get().configDir.toPath());
                } else {
                    RedProtect.get().container.getAsset("langEN-US.properties").get().copyToDirectory(RedProtect.get().configDir.toPath());
                    new File(RedProtect.get().configDir, "langEN-US.properties").renameTo(lang);
                }
            } catch (IOException e) {
                CoreUtil.printJarVersion();
                e.printStackTrace();
            }
            RedProtect.get().logger.info("Created config file: " + pathLang);
        }

        loadLang();
        loadBaseLang();
        updateLang();

        RedProtect.get().logger.info("Language file loaded - Using: " + RedProtect.get().getConfigManager().configRoot().language);
    }

    private void loadLang() {
        loadDefaultLang();

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

    private void updateLang() {
        if (updateLang(RedProtect.get().container.getVersion().get())) {
            RedProtect.get().logger.warning("- Removed invalid entries from language files");
        }
    }

    public String get(String key) {
        return getRaw(key);
    }

    public void sendMessage(CommandSource sender, String key) {
        sendMessage(sender, key, new Replacer[0]);
    }

    public void sendMessage(CommandSource sender, String key, Replacer[] replaces) {
        if (sender instanceof Player && delayedMessage.containsKey(sender.getName()) && delayedMessage.get(sender.getName()).equals(key)) {
            return;
        }

        if (loadedLang.get(key) == null) {
            sender.sendMessage(RedProtect.get().getUtil().toText(get("_redprotect.prefix") + " " + key));
        } else if (get(key).equalsIgnoreCase("")) {
            return;
        } else {
            String message = get(key);
            for (Replacer replacer : replaces) {
                message = message.replace(replacer.getPlaceholder(), replacer.getValue());
            }
            sender.sendMessage(RedProtect.get().getUtil().toText(get("_redprotect.prefix") + " " + message));
        }

        if (sender instanceof Player) {
            delayedMessage.put(sender.getName(), key);
            Sponge.getScheduler().createSyncExecutor(RedProtect.get().container).schedule(() -> {
                delayedMessage.remove(sender.getName());
            }, 1, TimeUnit.SECONDS);
        }
    }

    public void sendCommandHelp(CommandSource sender, String cmd, boolean usage) {
        if (sender instanceof ConsoleSource) {
            CommandHandlers.HandleHelpPage(sender, 1);
            return;
        }
        if (usage) sendMessage(sender, "correct.usage");
        sender.sendMessage(RedProtect.get().getUtil().toText(get("cmdmanager.help." + cmd).replace("{cmd}", getCmd(cmd)).replace("{alias}", getCmdAlias(cmd))));
    }
}
