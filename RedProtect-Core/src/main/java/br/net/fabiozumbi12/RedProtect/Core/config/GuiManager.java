package br.net.fabiozumbi12.RedProtect.Core.config;

public class GuiManager {
/*
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
    private static String pathLang;

    public static void init() {
        //Add gui config loader



        // Load language
        String resLang = "gui" + RedProtect.get().config.configRoot().language + ".properties";
        pathLang = RedProtect.get().getDataFolder() + File.separator + resLang;

        File lang = new File(pathLang);
        if (!lang.exists()) {
            if (RedProtect.get().getResource("assets/redprotect/" + resLang) == null) {
                resLang = "guiEN-US.properties";
                pathLang = RedProtect.get().getDataFolder() + File.separator + resLang;
            }
            RPUtil.saveResource("/assets/redprotect/" + resLang, null, new File(RedProtect.get().getDataFolder(), resLang));
            RedProtect.get().logger.info("Created GUI language file: " + pathLang);
        }

        loadLang();
        loadBaseLang();
    }

    private static void loadBaseLang() {
        baseLang.clear();
        try {
            InputStream fileInput = RedProtect.get().getResource("assets/redprotect/guiEN-US.properties");
            Reader reader = new InputStreamReader(fileInput, StandardCharsets.UTF_8);
            baseLang.load(reader);
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
            int rpv = Integer.parseInt(RedProtect.get().getDescription().getVersion().replace(".", ""));
            if (RedProtect.get().getDescription().getVersion().length() > loadedLang.get("_lang.version").toString().length()) {
                langv = Integer.parseInt(loadedLang.get("_lang.version").toString().replace(".", "") + 0);
            }
            if (langv < rpv || langv == 0) {
                loadedLang.put("_lang.version", RedProtect.get().getDescription().getVersion());
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
            loadedLang.put("_lang.version", RedProtect.get().getDescription().getVersion());

        try {
            loadedLang.store(new OutputStreamWriter(new FileOutputStream(pathLang), StandardCharsets.UTF_8), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String get(String key) {
        String FMsg;

        if (loadedLang.get(key) == null) {
            FMsg = "&c&oMissing language string for " + ChatColor.GOLD + key;
        } else {
            FMsg = loadedLang.get(key).toString();
        }

        FMsg = ChatColor.translateAlternateColorCodes('&', FMsg);

        return FMsg.replace("/n", "\n");
    }

    public static void sendMessage(CommandSender sender, String key) {
        sendMessage(sender, key, new Replacer[0]);
    }

    public static void sendMessage(CommandSender sender, String key, Replacer[] replaces) {
        if (sender instanceof Player && delayedMessage.containsKey(sender) && delayedMessage.get(sender).equals(key)) {
            return;
        }

        if (loadedLang.get(key) == null) {
            sender.sendMessage(get("_redprotect.prefix") + " " + ChatColor.translateAlternateColorCodes('&', key));
        } else if (get(key).equalsIgnoreCase("")) {
            return;
        } else {
            String message = get(key);
            for (Replacer replacer : replaces) {
                message = message.replace(replacer.getPlaceholder(), replacer.getValue());
            }
            sender.sendMessage(get("_redprotect.prefix") + " " + message);
        }

        if (sender instanceof Player) {
            final Player p = (Player) sender;
            delayedMessage.put(p, key);
            Bukkit.getScheduler().scheduleSyncDelayedTask(RedProtect.get(), () -> delayedMessage.remove(p), 20);
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
        return loadedLang.containsValue(value);
    }*/
}
