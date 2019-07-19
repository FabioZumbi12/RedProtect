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

package br.net.fabiozumbi12.RedProtect.Core.config;

import br.net.fabiozumbi12.RedProtect.Core.helpers.CoreUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class LangCore {
    protected final HashMap<String, String> delayedMessage = new HashMap<>();
    protected final Properties loadedLang = new Properties() {
        @Override
        public synchronized Enumeration<Object> keys() {
            return Collections.enumeration(new TreeSet<>(super.keySet()));
        }
    };
    private final Properties baseLang = new Properties() {
        @Override
        public synchronized Enumeration<Object> keys() {
            return Collections.enumeration(new TreeSet<>(super.keySet()));
        }
    };
    protected String pathLang;

    public SortedSet<String> getHelpStrings() {
        SortedSet<String> values = new TreeSet<>();
        for (Object help : loadedLang.keySet()) {
            if (help.toString().startsWith("cmdmanager.help.")) {
                values.add(help.toString().replace("cmdmanager.help.", ""));
            }
        }
        return values;
    }

    protected void loadBaseLang() {
        baseLang.clear();
        try {
            InputStream fileInput = LangCore.class.getResourceAsStream("/assets/redprotect/langEN-US.properties");
            Reader reader = new InputStreamReader(fileInput, StandardCharsets.UTF_8);
            baseLang.load(reader);
        } catch (Exception e) {
            CoreUtil.printJarVersion();
            e.printStackTrace();
        }
    }

    protected void loadDefaultLang() {
        loadedLang.clear();
        try {
            FileInputStream fileInput = new FileInputStream(pathLang);
            Reader reader = new InputStreamReader(fileInput, StandardCharsets.UTF_8);
            loadedLang.load(reader);
        } catch (Exception e) {
            CoreUtil.printJarVersion();
            e.printStackTrace();
        }
    }

    protected boolean updateLang(String pluginVersion) {
        baseLang.forEach((key, value) -> {
            if (!loadedLang.containsKey(key)) {
                loadedLang.put(key, value);
            }
        });

        //remove invalid entries
        boolean updated = loadedLang.entrySet().removeIf(k -> !baseLang.containsKey(k.getKey()) && !k.getKey().equals("_lang.version"));

        if (!loadedLang.containsKey("_lang.version"))
            loadedLang.put("_lang.version", pluginVersion);

        try {
            String header = "===================================================\n" +
                    "   You can translate this file to your language    \n" +
                    "     from our github: https://bit.ly/2IUMc7X       \n" +
                    "===================================================";
            loadedLang.store(new OutputStreamWriter(new FileOutputStream(pathLang), StandardCharsets.UTF_8), header);
        } catch (Exception e) {
            CoreUtil.printJarVersion();
            e.printStackTrace();
        }

        return updated;
    }

    protected String getRaw(String key) {
        return loadedLang.getProperty(key, "&c&oMissing language string for &4" + key).replace("/n", "\n");
    }

    public String translBool(String bool) {
        return getRaw("region." + bool.toLowerCase());
    }

    public String translBool(Boolean bool) {
        return getRaw("region." + bool.toString().toLowerCase());
    }

    public boolean containsValue(String value) {
        return loadedLang.containsValue(value);
    }
}
