/*
 Copyright @FabioZumbi12

 This class is provided 'as-is', without any express or implied warranty. In no event will the authors be held liable for any
  damages arising from the use of this class.

 Permission is granted to anyone to use this class for any purpose, including commercial plugins, and to alter it and
 redistribute it freely, subject to the following restrictions:
 1 - The origin of this class must not be misrepresented; you must not claim that you wrote the original software. If you
 use this class in other plugins, an acknowledgment in the plugin documentation would be appreciated but is not required.
 2 - Altered source versions must be plainly marked as such, and must not be misrepresented as being the original class.
 3 - This notice may not be removed or altered from any source distribution.

 Esta classe é fornecida "como está", sem qualquer garantia expressa ou implícita. Em nenhum caso os autores serão
 responsabilizados por quaisquer danos decorrentes do uso desta classe.

 É concedida permissão a qualquer pessoa para usar esta classe para qualquer finalidade, incluindo plugins pagos, e para
 alterá-lo e redistribuí-lo livremente, sujeito às seguintes restrições:
 1 - A origem desta classe não deve ser deturpada; você não deve afirmar que escreveu a classe original. Se você usar esta
  classe em um plugin, uma confirmação de autoria na documentação do plugin será apreciada, mas não é necessária.
 2 - Versões de origem alteradas devem ser claramente marcadas como tal e não devem ser deturpadas como sendo a
 classe original.
 3 - Este aviso não pode ser removido ou alterado de qualquer distribuição de origem.
 */

package br.net.fabiozumbi12.RedProtect.Bukkit.config;

import br.net.fabiozumbi12.RedProtect.Bukkit.RPUtil;
import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import org.bukkit.*;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class RPConfig {

    public static final List<String> AdminFlags = Arrays.asList(
            "spawn-wither",
            "cropsfarm",
            "max-players",
            "can-death",
            "cmd-onhealth",
            "keep-inventory",
            "keep-levels",
            "can-pickup",
            "can-drop",
            "forcepvp",
            "forcefly",
            "gamemode",
            "player-damage",
            "can-hunger",
            "can-projectiles",
            "allow-place",
            "allow-break",
            "can-pet",
            "allow-cmds",
            "deny-cmds",
            "allow-create-portal",
            "portal-exit",
            "portal-enter",
            "allow-mod",
            "allow-enter-items",
            "deny-enter-items",
            "pvparena",
            "player-enter-command",
            "server-enter-command",
            "player-exit-command",
            "server-exit-command",
            "invincible",
            "effects",
            "treefarm",
            "minefarm",
            "pvp",
            "sign",
            "teleport",
            "enter",
            "up-skills",
            "can-back",
            "for-sale",
            "exit",
            "set-portal",
            "dynmap",
            "particles",
            "deny-exit-items");
    private static RPCommentedConfig comConfig;
    private static RPCommentedGlobalFlags comGflags;
    private static YamlConfiguration signs;
    private static YamlConfiguration GuiItems;
    private static YamlConfiguration Prots;
    private static YamlConfiguration EconomyConfig;

    public static void init() {

        signs = new YamlConfiguration();
        GuiItems = new YamlConfiguration();
        Prots = new YamlConfiguration();
        EconomyConfig = new YamlConfiguration();
        comConfig = new RPCommentedConfig();

        File main = RedProtect.get().getDataFolder();
        File data = new File(main, "data");
        File gui = new File(main, "guiconfig.yml");
        File bvalues = new File(main, "economy.yml");
        File globalflags = new File(main, "globalflags.yml");
        File protections = new File(main, "protections.yml");
        File logs = new File(main, "logs");
        File signsf = new File(main, "signs.yml");
        File schema = new File(main, "schematics" + File.separator + "house1.schematic");

        if (!main.exists()) {
            main.mkdir();
            RedProtect.get().logger.info("Created folder: " + main);
        }

        if (!data.exists()) {
            data.mkdir();
            RedProtect.get().logger.info("Created folder: " + main);
        }

        //init config
        comConfig.addDef();

        if (!globalflags.exists()) {
            try {
                globalflags.createNewFile();//create globalflags file
                RedProtect.get().logger.info("Created globalflags file: " + globalflags);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!signsf.exists()) {
            try {
                signsf.createNewFile();//create PathSigns file
                RedProtect.get().logger.info("Created signs file: " + signsf);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!gui.exists()) {
            RPUtil.saveResource("/assets/redprotect/guiconfig" + RedProtect.get().version + ".yml", "/assets/redprotect/guiconfig.yml", gui);//create guiconfig file
            RedProtect.get().logger.info("Created guiconfig file: " + gui);
        }

        if (!bvalues.exists()) {
            RPUtil.saveResource("/assets/redprotect/economy.yml", null, bvalues);//create blockvalues file
            RedProtect.get().logger.info("Created economy file: " + bvalues);
        }

        if (!protections.exists()) {
            RPUtil.saveResource("/assets/redprotect/protections.yml", null, protections);//create protections file
            RedProtect.get().logger.info("Created protections file: " + protections);
        }

        if (!schema.exists()) {
            new File(main, "schematics").mkdir();
            RPUtil.saveResource("/assets/redprotect/schematics/house1.schematic", null, schema);//save schematic file
            RedProtect.get().logger.info("Saved schematic file: house1.schematic");
        }

        RedProtect.get().logger.info("Server version: " + RedProtect.get().serv.getBukkitVersion());

        // check if can enable json support
        if (getBool("region-settings.region-list.hover-and-click-teleport")) {
            try {
                Class.forName("com.google.gson.JsonParser");
                if (RedProtect.get().serv.getBukkitVersion().contains("1.7")) {
                    RedProtect.get().getConfig().set("region-settings.region-list.hover-and-click-teleport", false);
                    RedProtect.get().logger.warning("Your server version do not support Hover and Clicking region features, only 1.8.+");
                }
            } catch (ClassNotFoundException e) {
                RedProtect.get().getConfig().set("region-settings.region-list.hover-and-click-teleport", false);
                RedProtect.get().logger.warning("Your server version do not support JSON events, disabling Hover and Clicking region features.");
            }
        }

        //add op to ignore list fro purge
        if (RedProtect.get().getConfig().getStringList("purge.ignore-regions-from-players").size() <= 0) {
            List<String> ops = RedProtect.get().getConfig().getStringList("purge.ignore-regions-from-players");
            for (OfflinePlayer play : RedProtect.get().serv.getOperators()) {
                ops.add(play.getName());
            }
            RedProtect.get().getConfig().set("purge.ignore-regions-from-players", ops);
        }

        //add op to ignore list fro sell
        if (RedProtect.get().getConfig().getStringList("sell.ignore-regions-from-players").size() <= 0) {
            List<String> ops = RedProtect.get().getConfig().getStringList("sell.ignore-regions-from-players");
            for (OfflinePlayer play : RedProtect.get().serv.getOperators()) {
                ops.add(play.getName());
            }
            RedProtect.get().getConfig().set("sell.ignore-regions-from-players", ops);
        }

        //add allowed claim worlds to config
        if (RedProtect.get().getConfig().getStringList("allowed-claim-worlds").get(0).equals("example_world")) {
            List<String> worlds = new ArrayList<>();
            for (World w : RedProtect.get().serv.getWorlds()) {
                worlds.add(w.getName());
                RedProtect.get().logger.warning("Added world to claim list " + w.getName());
            }
            worlds.remove("example_world");
            RedProtect.get().getConfig().set("allowed-claim-worlds", worlds);
        }

        //add worlds to color list
        for (World w : RedProtect.get().serv.getWorlds()) {
            if (RedProtect.get().getConfig().getString("region-settings.claim-type.worlds." + w.getName()) == null) {
                RedProtect.get().getConfig().set("region-settings.claim-type.worlds." + w.getName(), "BLOCK");
            }

            if (RedProtect.get().getConfig().getString("region-settings.world-colors." + w.getName()) == null) {
                if (w.getEnvironment().equals(Environment.NORMAL)) {
                    RedProtect.get().getConfig().set("region-settings.world-colors." + w.getName(), "&a&l");
                } else if (w.getEnvironment().equals(Environment.NETHER)) {
                    RedProtect.get().getConfig().set("region-settings.world-colors." + w.getName(), "&c&l");
                } else if (w.getEnvironment().equals(Environment.THE_END)) {
                    RedProtect.get().getConfig().set("region-settings.world-colors." + w.getName(), "&5&l");
                }
                RedProtect.get().logger.warning("Added world to color list " + w.getName());
            }
        }

        /*----------------- Add default config for not updateable configs ------------------*/

        //update new player flags according version

        List<String> flags = RedProtect.get().getConfig().getStringList("flags-configuration.enabled-flags");
        int configUp = 0;
        if (RedProtect.get().getConfig().getDouble("config-version") < 6.8D) {
            RedProtect.get().getConfig().set("config-version", 6.8D);

            if (!flags.contains("smart-door")) {
                flags.add("smart-door");
            }
            if (!flags.contains("allow-potions")) {
                flags.add("allow-potions");
            }
            if (!flags.contains("mob-loot")) {
                flags.add("mob-loot");
            }
            if (!flags.contains("flow-damage")) {
                flags.add("flow-damage");
            }
            configUp++;
        }
        if (RedProtect.get().getConfig().getDouble("config-version") < 6.9D) {
            RedProtect.get().getConfig().set("config-version", 6.9D);

            if (!flags.contains("iceform-player")) {
                flags.add("iceform-player");
            }
            if (!flags.contains("iceform-entity")) {
                flags.add("iceform-entity");
            }
            configUp++;
        }
        if (RedProtect.get().getConfig().getDouble("config-version") < 7.0D) {
            RedProtect.get().getConfig().set("config-version", 7.0D);

            if (!flags.contains("allow-fly")) {
                flags.add("allow-fly");
            }
            configUp++;
        }
        if (RedProtect.get().getConfig().getDouble("config-version") < 7.1D) {
            RedProtect.get().getConfig().set("config-version", 7.1D);

            if (!flags.contains("teleport")) {
                flags.add("teleport");
            }
            configUp++;
        }
        if (RedProtect.get().getConfig().getDouble("config-version") < 7.2D) {
            RedProtect.get().getConfig().set("config-version", 7.2D);

            if (!flags.contains("clan")) {
                flags.add("clan");
            }
            configUp++;
        }
        if (RedProtect.get().getConfig().getDouble("config-version") < 7.3D) {
            RedProtect.get().getConfig().set("config-version", 7.3D);

            if (!flags.contains("ender-chest")) {
                flags.add("ender-chest");
            }
            configUp++;
        }
        if (RedProtect.get().getConfig().getDouble("config-version") < 7.5D) {
            RedProtect.get().getConfig().set("config-version", 7.5D);

            if (flags.contains("iceform-entity")) {
                flags.add("iceform-world");
                flags.remove("iceform-entity");
            }
            if (!flags.contains("can-grow")) {
                flags.add("can-grow");
            }
            configUp++;
        }
        if (RedProtect.get().getConfig().getDouble("config-version") < 7.6D) {
            RedProtect.get().getConfig().set("config-version", 7.6D);

            flags.remove("allow-potions");
            if (!flags.contains("use-potions")) {
                flags.add("use-potions");
            }
            if (!flags.contains("allow-effects")) {
                flags.add("allow-effects");
            }
            configUp++;
        }
        if (RedProtect.get().getConfig().getDouble("config-version") < 7.7D) {
            RedProtect.get().getConfig().set("config-version", 7.7D);

            if (!flags.contains("allow-spawner")) {
                flags.add("allow-spawner");
            }
            configUp++;
        }
        if (RedProtect.get().getConfig().getDouble("config-version") < 7.8D) {
            RedProtect.get().getConfig().set("config-version", 7.8D);

            if (!flags.contains("leaves-decay")) {
                flags.add("leaves-decay");
            }
            configUp++;
        }
        if (RedProtect.get().getConfig().getDouble("config-version") < 7.9D) {
            RedProtect.get().getConfig().set("config-version", 7.9D);

            if (!flags.contains("build")) {
                flags.add("build");
            }
            configUp++;
        }
        if (RedProtect.get().getConfig().getDouble("config-version") < 7.10D) {
            RedProtect.get().getConfig().set("config-version", 7.10D);

            RedProtect.get().getConfig().set("language", RedProtect.get().getConfig().getString("language").toUpperCase());
            configUp++;
        }
        if (RedProtect.get().getConfig().getDouble("config-version") < 7.11D) {
            RedProtect.get().getConfig().set("config-version", 7.11D);

            RedProtect.get().getConfig().set("wands.adminWandID", "GLASS_BOTTLE");
            RedProtect.get().getConfig().set("wands.infoWandID", "PAPER");
            configUp++;
        }
        if (RedProtect.get().getConfig().getDouble("config-version") < 7.12D) {
            RedProtect.get().getConfig().set("config-version", 7.12D);

            List<String> blocks = RedProtect.get().getConfig().getStringList("private.allowed-blocks");
            blocks.add("[A-Z_]+_SHULKER_BOX");
            RedProtect.get().getConfig().set("private.allowed-blocks", blocks);
            configUp++;
        }
        if (RedProtect.get().getConfig().getDouble("config-version") < 7.13D) {
            RedProtect.get().getConfig().set("config-version", 7.13D);

            if (!flags.contains("press-plate")) {
                flags.add("press-plate");
            }
            if (!flags.contains("fishing")) {
                flags.add("fishing");
            }
            configUp++;
        }
        if (configUp > 0) {
            RedProtect.get().getConfig().set("flags-configuration.enabled-flags", flags);
            RedProtect.get().logger.warning("Configuration UPDATE! We added new flags or new options, or just updated some other config. See change log for details.");
            comConfig.saveConfig();
        }

        /*------------------------------------------------------------------------------------*/

        //load protections file
        Prots = updateFile(protections);

        /*------------------------------------------------------------------------------------*/

        //load and write globalflags to global file
        comGflags = new RPCommentedGlobalFlags();
        comGflags.addDef();


        /*------------------------------------------------------------------------------------*/

        //load and write GuiItems to guiconfig file
        try {
            GuiItems.load(gui);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        YamlConfiguration GuiBase = inputLoader(RedProtect.get().getResource("assets/redprotect/guiconfig.yml"));

        GuiItems.set("gui-strings.value", GuiItems.getString("gui-strings.value", "&bValue: "));
        GuiItems.set("gui-strings.true", GuiItems.getString("gui-strings.true", "&atrue"));
        GuiItems.set("gui-strings.false", GuiItems.getString("gui-strings.false", "&cfalse"));
        GuiItems.set("gui-strings.separator", GuiItems.getString("gui-strings.separator", "&7|"));

        GuiItems.set("gui-separator.material", GuiItems.getString("gui-separator.material", "WHITE_STAINED_GLASS_PANE"));
        GuiItems.set("gui-separator.data", GuiItems.getInt("gui-separator.data", 0));

        for (String key : getDefFlagsValues().keySet()) {
            GuiItems.set("gui-flags." + key + ".slot", GuiItems.get("gui-flags." + key + ".slot", GuiBase.get("gui-flags." + key + ".slot", getDefFlagsValues().size())));
            GuiItems.set("gui-flags." + key + ".material", GuiItems.get("gui-flags." + key + ".material", GuiBase.get("gui-flags." + key + ".material", "GOLDEN_APPLE")));
            GuiItems.set("gui-flags." + key + ".name", GuiItems.get("gui-flags." + key + ".name", GuiBase.get("gui-flags." + key + ".name", "&e" + key)));
            GuiItems.set("gui-flags." + key + ".description", GuiItems.get("gui-flags." + key + ".description", GuiBase.get("gui-flags." + key + ".description", "&bDescription: &2Add a flag description here.")));
            GuiItems.set("gui-flags." + key + ".description1", GuiItems.get("gui-flags." + key + ".description1", GuiBase.get("gui-flags." + key + ".description1", "")));
            GuiItems.set("gui-flags." + key + ".description2", GuiItems.get("gui-flags." + key + ".description2", GuiBase.get("gui-flags." + key + ".description2", "")));
        }

        /*------------------------------------------------------------------------------------*/

        //load blockvalues file
        try {
            EconomyConfig.load(bvalues);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        YamlConfiguration tempEco = inputLoader(RedProtect.get().getResource("assets/redprotect/economy.yml"));
        for (String key : tempEco.getKeys(false)) {
            if (EconomyConfig.get(key) == null) {
                EconomyConfig.set(key, tempEco.get(key));
            }
        }

        if (EconomyConfig.getConfigurationSection("items.values").getKeys(false).size() != Material.values().length) {
            for (Material mat : Material.values()) {
                if (EconomyConfig.getString("items.values." + mat.name()) == null) {
                    EconomyConfig.set("items.values." + mat.name(), 0.0);
                }
            }
        }

        if (EconomyConfig.getConfigurationSection("enchantments.values").getKeys(false).size() != Enchantment.values().length) {
            for (Enchantment ench : Enchantment.values()) {
                if (EconomyConfig.getString("enchantments.values." + ench.getName()) == null) {
                    EconomyConfig.set("enchantments.values." + ench.getName(), 0.0);
                }
            }
        }

        //////////////////////
        /*------------------------------------------------------------------------------------*/

        String v = RedProtect.get().serv.getBukkitVersion();
        if (RedProtect.get().getConfig().getString("notify.region-enter-mode").equalsIgnoreCase("TITLE") && (v == null || !v.contains("1.8"))) {
            RedProtect.get().getConfig().set("notify.region-enter-mode", "CHAT");
            RedProtect.get().logger.warning("Title notifications is not suported on servers not running 1.8! Defaulting to CHAT.");
        }

        //create logs folder
        if (getBool("log-actions") && !logs.exists()) {
            logs.mkdir();
            RedProtect.get().logger.info("Created folder: " + logs);
        }

        //Load signs file
        try {
            signs.load(signsf);
        } catch (Exception e) {
            e.printStackTrace();
        }

        save();
        RedProtect.get().logger.info("All configurations loaded!");
    }

    public static String getWorldClaimType(String w) {
        return RedProtect.get().getConfig().getString("region-settings.claim-type.worlds." + w);
    }

    public static boolean hasGlobalKey(String path) {
        return comGflags.gflags.contains(path);
    }

    public static String getGlobalFlagString(String string) {
        return comGflags.gflags.getString(string);
    }

    public static double getGlobalFlagDouble(String key) {
        return comGflags.gflags.getDouble(key);
    }

    public static float getGlobalFlagFloat(String key) {
        return Float.valueOf(comGflags.gflags.getString(key));
    }

    public static int getGlobalFlagInt(String key) {
        return comGflags.gflags.getInt(key);
    }

    public static Boolean getGlobalFlagBool(String key) {
        return comGflags.gflags.getBoolean(key);
    }

    public static List<String> getGlobalFlagList(String key) {
        return comGflags.gflags.getStringList(key);
    }

    public static ItemStack getGuiItemStack(String key) {
        RedProtect.get().logger.debug("Gui Material to get: " + key);
        RedProtect.get().logger.debug("Result: " + GuiItems.getString("gui-flags." + key + ".material"));
        String item = GuiItems.getString("gui-flags." + key + ".material", "WHITE_STAINED_GLASS_PANE");
        return new ItemStack(Material.getMaterial(item));
    }

    public static String getGuiFlagString(String flag, String option) {
        if (GuiItems.getString("gui-flags." + flag + "." + option) == null) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', GuiItems.getString("gui-flags." + flag + "." + option));
    }

    public static String getGuiString(String string) {
        return ChatColor.translateAlternateColorCodes('&', GuiItems.getString("gui-strings." + string));
    }

    public static int getGuiSlot(String flag) {
        return GuiItems.getInt("gui-flags." + flag + ".slot");
    }

    public static void setGuiSlot(/*String mat, */String flag, int slot) {
        GuiItems.set("gui-flags." + flag + ".slot", slot);
        //GuiItems.set("gui-flags."+flag+".material", mat);

    }

    public static ItemStack getGuiSeparator() {
        ItemStack separator = new ItemStack(Material.getMaterial(GuiItems.getString("gui-separator.material", "WHITE_STAINED_GLASS_PANE")), 1, (short) GuiItems.getInt("gui-separator.data"));
        ItemMeta meta = separator.getItemMeta();
        meta.setDisplayName(getGuiString("separator"));
        meta.setLore(Arrays.asList("", getGuiString("separator")));
        separator.setItemMeta(meta);
        return separator;
    }

    public static int getGuiMaxSlot() {
        SortedSet<Integer> slots = new TreeSet<>(new ArrayList<>());
        for (String key : GuiItems.getKeys(true)) {
            if (key.contains(".slot")) {
                slots.add(GuiItems.getInt(key));
            }
        }
        return Collections.max(slots);
    }

    public static Boolean getBool(String key) {
        return RedProtect.get().getConfig().getBoolean(key, false);
    }

    public static void setConfig(String key, Object value) {
        RedProtect.get().getConfig().set(key, value);
    }

    public static HashMap<String, Object> getDefFlagsValues() {
        HashMap<String, Object> flags = new HashMap<>();
        for (String flag : RedProtect.get().getConfig().getValues(true).keySet()) {
            if (flag.contains("flags.") && isFlagEnabled(flag.replace("flags.", ""))) {
                if (flag.replace("flags.", "").equals("pvp") && !RedProtect.get().getConfig().getStringList("flags-configuration.enabled-flags").contains("pvp")) {
                    continue;
                }
                flags.put(flag.replace("flags.", ""), RedProtect.get().getConfig().get(flag));
            }
        }
        return flags;
    }

    public static boolean isFlagEnabled(String flag) {
        return RedProtect.get().getConfig().getStringList("flags-configuration.enabled-flags").contains(flag) || AdminFlags.contains(flag);
    }

    public static SortedSet<String> getDefFlags() {
        return new TreeSet<>(getDefFlagsValues().keySet());
    }

    public static String getString(String key, String def) {
        return RedProtect.get().getConfig().getString(key, def);
    }

    public static String getString(String key) {
        return RedProtect.get().getConfig().getString(key, "");
    }

    public static Integer getInt(String key) {
        return RedProtect.get().getConfig().getInt(key);
    }

    public static List<String> getStringList(String key) {
        return RedProtect.get().getConfig().getStringList(key);
    }

    public static Material getMaterial(String key) {
        return Material.getMaterial(RedProtect.get().getConfig().getString(key));
    }

    public static void save() {
        File main = RedProtect.get().getDataFolder();
        File gui = new File(main, "guiconfig.yml");
        File bvalues = new File(main, "economy.yml");
        File protections = new File(main, "protections.yml");
        File signsf = new File(main, "signs.yml");
        try {
            GuiItems.save(gui);
            EconomyConfig.save(bvalues);
            Prots.save(protections);
            signs.save(signsf);
            comGflags.saveConfig();
            comConfig.saveConfig();
        } catch (IOException e) {
            RedProtect.get().logger.severe("Problems during save file:");
            e.printStackTrace();
        }
    }

    public static void saveGui() {
        File guiconfig = new File(RedProtect.get().getDataFolder(), "guiconfig.yml");
        try {
            GuiItems.save(guiconfig);
        } catch (IOException e) {
            RedProtect.get().logger.severe("Problems during save gui file:");
            e.printStackTrace();
        }
    }

    private static YamlConfiguration inputLoader(InputStream inp) {
        YamlConfiguration file = new YamlConfiguration();
        try {
            file.load(new InputStreamReader(inp, StandardCharsets.UTF_8));
            inp.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    public static boolean isAllowedWorld(Player p) {
        return getStringList("allowed-claim-worlds").contains(p.getWorld().getName()) || p.hasPermission("redprotect.bypass.world");
    }

    public static boolean needClaimToBuild(Player p, Block b) {
        boolean bool = RedProtect.get().getConfig().getStringList("needed-claim-to-build.worlds").contains(p.getWorld().getName());
        if (bool) {
            if (b != null && getBool("needed-claim-to-build.allow-only-protections-blocks") &&
                    (getWorldClaimType(p.getWorld().getName()).equalsIgnoreCase("BLOCK") ||
                            getWorldClaimType(p.getWorld().getName()).equalsIgnoreCase("BOTH"))) {
                boolean blocks = b.getType().name().contains(getString("region-settings.block-id")) || b.getType().name().contains("SIGN") ||
                        getStringList("needed-claim-to-build.allow-break-blocks").stream().anyMatch(str -> str.equalsIgnoreCase(b.getType().name()));
                if (!blocks) {
                    RPLang.sendMessage(p, "need.claim.blockids");
                } else {
                    return false;
                }
            }
            RPLang.sendMessage(p, "need.claim.tobuild");
        }
        return bool;
    }

    public static SortedSet<String> getAllFlags() {
        SortedSet<String> values = new TreeSet<>(getDefFlagsValues().keySet());
        values.addAll(new TreeSet<>(AdminFlags));
        return values;
    }

    public static boolean addFlag(String flag, boolean defaultValue, boolean isAdmin) {
        if (isAdmin) {
            if (!AdminFlags.contains(flag)) {
                AdminFlags.add(flag);
                return true;
            }
        } else {
            if (RedProtect.get().getConfig().get("flags." + flag) == null) {
                RedProtect.get().getConfig().set("flags." + flag, defaultValue);
                List<String> flags = RedProtect.get().getConfig().getStringList("flags-configuration.enabled-flags");
                flags.add(flag);
                RedProtect.get().getConfig().set("flags-configuration.enabled-flags", flags);
                RedProtect.get().saveConfig();
                return true;
            }
        }
        return false;
    }

    public static int getProtInt(String key) {
        return Prots.getInt(key);
    }

    public static boolean getProtBool(String key) {
        return Prots.getBoolean(key);
    }

    public static List<String> getProtStringList(String key) {
        return Prots.getStringList(key);
    }
	
    /*public static boolean containsProtKey(String key){
		return Prots.contains(key);
	}*/

    public static String getProtString(String key) {
        return Prots.getString(key);
    }

    public static String getProtMsg(String key) {
        return ChatColor.translateAlternateColorCodes('&', Prots.getString(key));
    }

    public static int getBlockCost(String itemName) {
        return EconomyConfig.getInt("items.values." + itemName);
    }

    public static int getEnchantCost(String enchantment) {
        return EconomyConfig.getInt("enchantments.values." + enchantment);
    }

    public static String getEcoString(String key) {
        return EconomyConfig.getString(key);
    }

    public static Integer getEcoInt(String key) {
        return EconomyConfig.getInt(key);
    }

    public static boolean getEcoBool(String key) {
        return EconomyConfig.getBoolean(key);
    }

    private static YamlConfiguration updateFile(File saved) {
        YamlConfiguration finalyml = new YamlConfiguration();
        try {
            finalyml.load(saved);
        } catch (Exception e) {
            e.printStackTrace();
        }

        YamlConfiguration tempProts = inputLoader(RedProtect.get().getResource("assets/redprotect/protections.yml"));
        for (String key : tempProts.getKeys(true)) {
            Object obj = tempProts.get(key);
            if (finalyml.get(key) != null) {
                obj = finalyml.get(key);
            }
            finalyml.set(key, obj);
        }
        return finalyml;
    }

    public static List<Location> getSigns(String rid) {
        List<Location> locs = new ArrayList<>();
        for (String s : signs.getStringList(rid)) {
            String[] val = s.split(",");
            if (Bukkit.getWorld(val[0]) == null) {
                continue;
            }
            locs.add(new Location(Bukkit.getWorld(val[0]), Double.valueOf(val[1]), Double.valueOf(val[2]), Double.valueOf(val[3])));
        }
        return locs;
    }

    public static void putSign(String rid, Location loc) {
        List<String> lsigns = signs.getStringList(rid);
        String locs = loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ();
        if (!lsigns.contains(locs)) {
            lsigns.add(locs);
            saveSigns(rid, lsigns);
        }
    }

    public static void removeSign(String rid, Location loc) {
        List<String> lsigns = signs.getStringList(rid);
        String locs = loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ();
        if (lsigns.contains(locs)) {
            lsigns.remove(locs);
            saveSigns(rid, lsigns);
        }
    }

    private static void saveSigns(String rid, List<String> locs) {
        if (locs.isEmpty()) {
            signs.set(rid, null);
        } else {
            signs.set(rid, locs);
        }
        try {
            signs.save(new File(RedProtect.get().getDataFolder(), "signs.yml"));
        } catch (IOException e) {
            RedProtect.get().logger.severe("Problems during save file:");
            e.printStackTrace();
        }
    }
}
   