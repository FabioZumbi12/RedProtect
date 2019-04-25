package br.net.fabiozumbi12.RedProtect.Core.config;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class GuiLangCore {

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

    protected void loadBaseLang() {
        baseLang.clear();
        try {
            InputStream fileInput = LangCore.class.getResourceAsStream("/assets/redprotect/guiEN-US.properties");
            Reader reader = new InputStreamReader(fileInput, StandardCharsets.UTF_8);
            baseLang.load(reader);
        } catch (Exception e) {
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
        boolean updated = loadedLang.entrySet().removeIf(k -> !baseLang.containsKey(k.getKey()));

        if (!loadedLang.containsKey("_lang.version"))
            loadedLang.put("_lang.version", pluginVersion);

        try {
            String header = "===================================================\n" +
                            "   You can translate this file to your language    \n" +
                            "     from our github: https://bit.ly/2IUMc7X       \n" +
                            "===================================================";
            loadedLang.store(new OutputStreamWriter(new FileOutputStream(pathLang), StandardCharsets.UTF_8), header);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return updated;
    }

    protected String getRaw(String key) {
        String langLine;

        if (loadedLang.get(key) == null) {
            langLine = "&c&oNo entry for &4" + key;
        } else {
            langLine = loadedLang.get(key).toString();
        }
        return langLine.replace("/n", "\n");
    }
}
