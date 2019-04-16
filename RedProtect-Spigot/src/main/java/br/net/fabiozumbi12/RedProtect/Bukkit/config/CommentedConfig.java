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

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class CommentedConfig {

    private final HashMap<String, String> comments;
    public FileConfiguration configurations;
    private String header;
    private File config;

    CommentedConfig(File config, FileConfiguration configurations, String header) {
        this.config = config;
        this.comments = new HashMap<>();
        this.configurations = configurations;
        this.header = header;

        if (config.exists()) {
            try {
                configurations.load(config);
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }
        }
    }

    public void setDefault(String key, Object def, String comment) {
        if (def != null) {
            configurations.set(key, configurations.get(key, def));
        }
        if (comment != null) {
            setComment(key, comment);
        }
    }

    private void setComment(String key, String comment) {
        comments.put(key, comment);
    }

    public void saveConfig() {
        StringBuilder b = new StringBuilder();
        configurations.options().header(null);
        b.append(header).append("\n\n");

        for (String line : configurations.getKeys(true)) {
            String[] key = line.split("\\" + configurations.options().pathSeparator());
            StringBuilder spaces = new StringBuilder();
            for (int i = 0; i < key.length; i++) {
                if (i == 0) continue;
                spaces.append("  ");
            }
            if (comments.containsKey(line)) {
                if (spaces.length() == 0) {
                    b.append("\n# ").append(comments.get(line).replace("\n", "\n# ")).append('\n');
                } else {
                    b.append(spaces).append("# ").append(comments.get(line).replace("\n", "\n" + spaces + "# ")).append('\n');
                }
            }
            Object value = configurations.get(line);
            if (!configurations.isConfigurationSection(line)) {
                if (value instanceof String) {
                    b.append(spaces).append(key[key.length - 1]).append(": '").append(value).append("'\n");
                } else if (value instanceof List<?>) {
                    if (((List<?>) value).isEmpty()) {
                        b.append(spaces).append(key[key.length - 1]).append(": []\n");
                    } else {
                        b.append(spaces).append(key[key.length - 1]).append(":\n");
                        for (Object lineCfg : (List<?>) value) {
                            if (lineCfg instanceof String) {
                                b.append(spaces).append("- '").append(lineCfg).append("'\n");
                            } else {
                                b.append(spaces).append("- ").append(lineCfg).append("\n");
                            }
                        }
                    }
                } else {
                    b.append(spaces).append(key[key.length - 1]).append(": ").append(value).append("\n");
                }
            } else {
                b.append(spaces).append(key[key.length - 1]).append(":\n");
            }
        }

        try {
            Files.write(b, config, Charsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
