/*
 Copyright @FabioZumbi12

 This class is provided 'as-is', without any express or implied warranty. In no event will the authors be held liable for any
  damages arising from the use of this software.

 Permission is granted to anyone to use this class for any purpose, including commercial plugins, and to alter it and
 redistribute it freely, subject to the following restrictions:
 1 - The origin of this class must not be misrepresented; you must not claim that you wrote the original software. If you
 use this class in other plugins, an acknowledgment in the plugin documentation would be appreciated but is not required.
 2 - Altered source versions must be plainly marked as such, and must not be misrepresented as being the original class.
 3 - This notice may not be removed or altered from any source distribution.

 Este software é fornecido "como está", sem qualquer garantia expressa ou implícita. Em nenhum caso os autores serão
 responsabilizados por quaisquer danos decorrentes do uso deste software.

 É concedida permissão a qualquer pessoa para usar esta classe para qualquer finalidade, incluindo plugins pagos, e para
 alterá-lo e redistribuí-lo livremente, sujeito às seguintes restrições:
 1 - A origem desta classe não deve ser deturpada; você não deve afirmar que escreveu a classe original. Se você usar esta
  classe em um plugin, uma confirmação de autoria na documentação do plugin será apreciada, mas não é necessária.
 2 - Versões de origem alteradas devem ser claramente marcadas como tal e não devem ser deturpadas como sendo a
 classe original.
 3 - Este aviso não pode ser removido ou alterado de qualquer distribuição de origem.
 */

package br.net.fabiozumbi12.RedProtect.Bukkit;

import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.io.File;
import java.util.SortedMap;
import java.util.TreeMap;

public class RPLogger {
    private final SortedMap<Integer, String> MainLog = new TreeMap<>();

    public void clear(String s) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', s));
    }

    public void sucess(String s) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "Redprotect: [&a&l" + s + "&r]"));
    }

    public void info(String s) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "Redprotect: [" + s + "]"));
    }

    public void warning(String s) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "Redprotect: [&6" + s + "&r]"));
    }

    public void severe(String s) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "Redprotect: [&c&l" + s + "&r]"));
    }

    public void log(String s) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "Redprotect: [" + s + "]"));
    }

    public void debug(String s) {
        if (RPConfig.getBool("debug-messages")) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "Redprotect: [&b" + s + "&r]"));
        }
    }

    public void addLog(String logLine) {
        if (!RPConfig.getBool("log-actions")) {
            return;
        }
        int key = MainLog.keySet().size() + 1;
        MainLog.put(key, key + " - " + RPUtil.HourNow() + ": " + ChatColor.translateAlternateColorCodes('&', logLine));
        if (key == 500) {
            SaveLogs();
            MainLog.clear();
        }
    }

    public void SaveLogs() {
        if (!RPConfig.getBool("log-actions")) {
            return;
        }

        final StringBuilder sb = new StringBuilder();
        for (int key : MainLog.keySet()) {
            sb.append(MainLog.get(key));
            sb.append('\n');
        }
        if (RPUtil.genFileName(RedProtect.get().getDataFolder() + File.separator + "logs" + File.separator, false) != null) {
            RPUtil.SaveToZipSB(RPUtil.genFileName(RedProtect.get().getDataFolder() + File.separator + "logs" + File.separator, false), sb);
        }
    }
}
