/*
 * Copyright (c) 2019 - @FabioZumbi12
 * Last Modified: 25/04/19 07:02
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

package br.net.fabiozumbi12.RedProtect.Bukkit.helpers;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Core.helpers.LogLevel;
import org.bukkit.Bukkit;

import java.io.File;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.bukkit.ChatColor.translateAlternateColorCodes;

public class RedProtectLogger {
    private final SortedMap<Integer, String> mainLog = new TreeMap<>();

    public void clear(String s) {
        Bukkit.getConsoleSender()
                .sendMessage(translateAlternateColorCodes('&', s));
    }

    public void success(String s) {
        Bukkit.getConsoleSender()
                .sendMessage(translateAlternateColorCodes('&', "[RedProtect] &a&l" + s + "&r"));
    }

    public void info(String s) {
        Bukkit.getConsoleSender()
                .sendMessage(translateAlternateColorCodes('&', "[RedProtect] " + s));
    }

    public void warning(String s) {
        Bukkit.getConsoleSender()
                .sendMessage(translateAlternateColorCodes('&', "[RedProtect] &6" + s + "&r"));
    }

    public void severe(String s) {
        Bukkit.getConsoleSender()
                .sendMessage(translateAlternateColorCodes('&', "[RedProtect] &c&l" + s + "&r"));
    }

    public void log(String s) {
        Bukkit.getConsoleSender()
                .sendMessage(translateAlternateColorCodes('&', "[RedProtect] " + s));
    }

    public void debug(LogLevel level, String s) {
        if (RedProtect.get().config.configRoot().debug_messages.get(level.name().toLowerCase())) {
            Bukkit.getConsoleSender()
                    .sendMessage(translateAlternateColorCodes('&', "[RedProtect] &b" + s + "&r"));
        }
    }

    public void addLog(String logLine) {
        // If log is not enabled return
        if (!RedProtect.get().config.configRoot().log_actions) {
            return;
        }
        int key = mainLog.keySet().size() + 1;
        mainLog.put(key, key + " - " + RedProtectUtil.hourNow() + ": " + translateAlternateColorCodes('&', logLine));
        if (key == 500) {
            saveLogs();
            mainLog.clear();
        }
    }

    public void saveLogs() {
        if (!RedProtect.get().config.configRoot().log_actions) {
            return;
        }

        final StringBuilder sb = new StringBuilder();
        mainLog.forEach((key, entry) -> sb.append(entry).append('\n'));

        if (RedProtectUtil.genFileName(RedProtect.get().getDataFolder() + File.separator + "logs" + File.separator, false) != null) {
            RedProtectUtil.saveSBToZip(RedProtectUtil.genFileName(RedProtect.get().getDataFolder() + File.separator + "logs" + File.separator, false), sb);
        }
    }
}
