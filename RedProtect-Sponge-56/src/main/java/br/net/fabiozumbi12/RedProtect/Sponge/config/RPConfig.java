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

package br.net.fabiozumbi12.RedProtect.Sponge.config;

import br.net.fabiozumbi12.RedProtect.Sponge.RPUtil;
import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import br.net.fabiozumbi12.RedProtect.Sponge.config.Category.FlagGuiCategory;
import br.net.fabiozumbi12.RedProtect.Sponge.config.Category.GlobalFlagsCategory;
import br.net.fabiozumbi12.RedProtect.Sponge.config.Category.MainCategory;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.GuiceObjectMapperFactory;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.google.common.reflect.TypeToken.of;

public class RPConfig {

    public final List<String> AdminFlags = Arrays.asList(
            "spawn-wither",
            "cropsfarm",
            "keep-inventory",
            "keep-levels",
            "can-drop",
            "can-pickup",
            "cmd-onhealth",
            "can-death",
            "max-players",
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
            "enderpearl",
            "enter",
            "up-skills",
            "can-back",
            "for-sale",
            "set-portal",
            "exit",
            "particles",
            "dynmap",
            "deny-exit-items");


    private final File protFile = new File(RedProtect.get().configDir, "protections.conf");
    private final File ecoFile = new File(RedProtect.get().configDir, "economy.conf");
    private final File signFile = new File(RedProtect.get().configDir, "signs.conf");
    private final File guiConfig = new File(RedProtect.get().configDir, "guiconfig.conf");
    private final File gFlagsConfig = new File(RedProtect.get().configDir, "globalflags.conf");
    private ConfigurationLoader<CommentedConfigurationNode> protManager;
    private CommentedConfigurationNode protCfgs;
    private ConfigurationLoader<CommentedConfigurationNode> ecoManager;
    private CommentedConfigurationNode ecoCfgs;
    private ConfigurationLoader<CommentedConfigurationNode> signManager;
    private CommentedConfigurationNode signCfgs;
    private ConfigurationLoader<CommentedConfigurationNode> guiLoader;
    private CommentedConfigurationNode guiCfgRoot;
    private FlagGuiCategory guiRoot;
    private ConfigurationLoader<CommentedConfigurationNode> gFlagsLoader;
    private CommentedConfigurationNode gflagsRoot;
    private GlobalFlagsCategory gflags;
    private File defConfig = new File(RedProtect.get().configDir, "config.conf");
    private CommentedConfigurationNode configRoot;
    private ConfigurationLoader<CommentedConfigurationNode> cfgLoader;
    private MainCategory root;

    //init
    public RPConfig(GuiceObjectMapperFactory factory) throws ObjectMappingException {
        try {
            if (!RedProtect.get().configDir.exists()) {
                RedProtect.get().configDir.mkdir();
            }
            if (!new File(RedProtect.get().configDir, "data").exists()) {
                new File(RedProtect.get().configDir, "data").mkdir();
            }

            /*--------------------- config.conf ---------------------------*/
            String header = ""
                    + "+--------------------------------------------------------------------+ #\n"
                    + "<               RedProtect World configuration File                  > #\n"
                    + "<--------------------------------------------------------------------> #\n"
                    + "<       This is the configuration file, feel free to edit it.        > #\n"
                    + "<        For more info about cmds and flags, check our Wiki:         > #\n"
                    + "<         https://github.com/FabioZumbi12/RedProtect/wiki            > #\n"
                    + "+--------------------------------------------------------------------+ #\n"
                    + "\n"
                    + "Notes:\n"
                    + "Lists are [object1, object2, ...]\n"
                    + "Strings containing the char & always need to be quoted";

            cfgLoader = HoconConfigurationLoader.builder().setFile(defConfig).build();
            configRoot = cfgLoader.load(ConfigurationOptions.defaults().setObjectMapperFactory(factory).setShouldCopyDefaults(true).setHeader(header));
            this.root = configRoot.getValue(of(MainCategory.class), new MainCategory());

            /*--------------------- end config.conf ---------------------------*/

            /*--------------------- globalflags.conf ---------------------------*/
            String headerg = ""
                    + "+--------------------------------------------------------------------+ #\n"
                    + "<          RedProtect Global Flags configuration File                > #\n"
                    + "<--------------------------------------------------------------------> #\n"
                    + "<         This is the global flags configuration file.               > #\n"
                    + "<                       Feel free to edit it.                        > #\n"
                    + "<         https://github.com/FabioZumbi12/RedProtect/wiki            > #\n"
                    + "+--------------------------------------------------------------------+ #\n"
                    + "\n"
                    + "Notes:\n"
                    + "Lists are [object1, object2, ...]\n"
                    + "Strings containing the char & always need to be quoted";

            gFlagsLoader = HoconConfigurationLoader.builder().setFile(gFlagsConfig).build();
            gflagsRoot = gFlagsLoader.load(ConfigurationOptions.defaults().setObjectMapperFactory(factory).setShouldCopyDefaults(true).setHeader(headerg));

            //import old world values
            if (gFlagsConfig.exists() && !gflagsRoot.getNode("worlds").hasMapChildren()) {
                Object values = gflagsRoot.getValue();
                gflagsRoot.setValue(null);
                gflagsRoot.getNode("worlds").setValue(values);
                RedProtect.get().logger.warning("File \"globalflags.conf\" updated with new configurations!");
            }

            this.gflags = gflagsRoot.getValue(of(GlobalFlagsCategory.class), new GlobalFlagsCategory());

            /*--------------------- end globalflags.conf ---------------------------*/

            /*--------------------- guiconfig.conf ---------------------------*/
            String headerGui = ""
                    + "+--------------------------------------------------------------------+ #\n"
                    + "<             RedProtect Gui Flags configuration File                > #\n"
                    + "<--------------------------------------------------------------------> #\n"
                    + "<            This is the gui flags configuration file.               > #\n"
                    + "<                       Feel free to edit it.                        > #\n"
                    + "<         https://github.com/FabioZumbi12/RedProtect/wiki            > #\n"
                    + "+--------------------------------------------------------------------+ #\n"
                    + "\n"
                    + "Notes:\n"
                    + "Lists are [object1, object2, ...]\n"
                    + "Strings containing the char & always need to be quoted";

            guiLoader = HoconConfigurationLoader.builder().setFile(guiConfig).build();
            guiCfgRoot = guiLoader.load(ConfigurationOptions.defaults().setObjectMapperFactory(factory).setShouldCopyDefaults(true).setHeader(headerGui));
            this.guiRoot = guiCfgRoot.getValue(of(FlagGuiCategory.class), new FlagGuiCategory());

            for (String key : getDefFlagsValues().keySet()) {
                if (!guiRoot.gui_flags.containsKey(key)) {
                    guiRoot.gui_flags.put(key, new FlagGuiCategory.GuiFlag("&e" + key, getGuiMaxSlot()));
                }
            }

            /*--------------------- end guiconfig.conf ---------------------------*/

            if (!ecoFile.exists()) {
                Asset ecoAsset = RedProtect.get().container.getAsset("economy.conf").get();
                ecoAsset.copyToDirectory(RedProtect.get().configDir.toPath());
            }
        } catch (IOException e1) {
            RedProtect.get().logger.severe("The default configuration could not be loaded or created!");
            e1.printStackTrace();
        }


        //load configs
        try {
            ecoManager = HoconConfigurationLoader.builder().setPath(ecoFile.toPath()).build();
            ecoCfgs = ecoManager.load();

            signManager = HoconConfigurationLoader.builder().setPath(signFile.toPath()).build();
            signCfgs = signManager.load();

            /*--------------------- protections.conf ---------------------------*/
            protManager = HoconConfigurationLoader.builder().setFile(protFile).build();
            protCfgs = protManager.load();

            protCfgs.getNode("chat-protection", "chat-enhancement", "enable").setValue(protCfgs.getNode("chat-protection", "chat-enhancement", "enable").getBoolean(true));
            protCfgs.getNode("chat-protection", "chat-enhancement", "end-with-dot").setValue(protCfgs.getNode("chat-protection", "chat-enhancement", "end-with-dot").getBoolean(true));
            protCfgs.getNode("chat-protection", "chat-enhancement", "minimum-lenght").setValue(protCfgs.getNode("chat-protection", "chat-enhancement", "minimum-lenght").getInt(3));

            protCfgs.getNode("chat-protection", "anti-flood", "enable").setValue(protCfgs.getNode("chat-protection", "anti-flood", "enable").getBoolean(true));
            protCfgs.getNode("chat-protection", "anti-flood", "whitelist-flood-characs")
                    .setValue(protCfgs.getNode("chat-protection", "anti-flood", "whitelist-flood-characs").getList(of(String.class), Collections.singletonList("k")));

            protCfgs.getNode("chat-protection", "caps-filter", "enable").setValue(protCfgs.getNode("chat-protection", "caps-filter", "enable").getBoolean(true));
            protCfgs.getNode("chat-protection", "caps-filter", "minimum-lenght").setValue(protCfgs.getNode("chat-protection", "caps-filter", "minimum-lenght").getInt(3));

            protCfgs.getNode("chat-protection", "antispam", "enable").setValue(protCfgs.getNode("chat-protection", "antispam", "enable").getBoolean(false));
            protCfgs.getNode("chat-protection", "antispam", "time-beteween-messages").setValue(protCfgs.getNode("chat-protection", "antispam", "time-beteween-messages").getInt(1));
            protCfgs.getNode("chat-protection", "antispam", "count-of-same-message").setValue(protCfgs.getNode("chat-protection", "antispam", "count-of-same-message").getInt(5));
            protCfgs.getNode("chat-protection", "antispam", "time-beteween-same-messages").setValue(protCfgs.getNode("chat-protection", "antispam", "time-beteween-same-messages").getInt(10));
            protCfgs.getNode("chat-protection", "antispam", "colldown-msg").setValue(protCfgs.getNode("chat-protection", "antispam", "colldown-msg").getString("&6Slow down your messages!"));
            protCfgs.getNode("chat-protection", "antispam", "wait-message").setValue(protCfgs.getNode("chat-protection", "antispam", "wait-message").getString("&cWait to send the same message again!"));
            protCfgs.getNode("chat-protection", "antispam", "cmd-action").setValue(protCfgs.getNode("chat-protection", "antispam", "cmd-action").getString("kick {player} Relax, slow down your messages frequency ;)"));

            protCfgs.getNode("chat-protection", "censor", "enable").setValue(protCfgs.getNode("chat-protection", "censor", "enable").getBoolean(true));
            protCfgs.getNode("chat-protection", "censor", "replace-by-symbol").setValue(protCfgs.getNode("chat-protection", "censor", "replace-by-symbol").getBoolean(true));
            protCfgs.getNode("chat-protection", "censor", "by-symbol").setValue(protCfgs.getNode("chat-protection", "censor", "by-symbol").getString("*"));
            protCfgs.getNode("chat-protection", "censor", "by-word").setValue(protCfgs.getNode("chat-protection", "censor", "by-word").getString("censored"));
            protCfgs.getNode("chat-protection", "censor", "replace-partial-word").setValue(protCfgs.getNode("chat-protection", "censor", "replace-partial-word").getBoolean(false));
            protCfgs.getNode("chat-protection", "censor", "action", "cmd").setValue(protCfgs.getNode("chat-protection", "censor", "action", "cmd").getString(""));
            protCfgs.getNode("chat-protection", "censor", "action", "partial-words").setValue(protCfgs.getNode("chat-protection", "censor", "action", "partial-words").getBoolean(false));
            protCfgs.getNode("chat-protection", "censor", "replace-words")
                    .setValue(protCfgs.getNode("chat-protection", "censor", "replace-words").getList(of(String.class), Collections.singletonList("word")));

            protCfgs.getNode("chat-protection", "anti-ip", "enable").setValue(protCfgs.getNode("chat-protection", "anti-ip", "enable").getBoolean(true));
            protCfgs.getNode("chat-protection", "anti-ip", "custom-ip-regex").setValue(protCfgs.getNode("chat-protection", "anti-ip", "custom-ip-regex").getString("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"));
            protCfgs.getNode("chat-protection", "anti-ip", "custom-url-regex").setValue(protCfgs.getNode("chat-protection", "anti-ip", "custom-url-regex").getString("((http:\\/\\/|https:\\/\\/)?(www.)?(([a-zA-Z0-9-]){2,}\\.){1,4}([a-zA-Z]){2,6}(\\/([a-zA-Z-_\\/\\.0-9#:?=&;,]*)?)?)"));
            protCfgs.getNode("chat-protection", "anti-ip", "check-for-words")
                    .setValue(protCfgs.getNode("chat-protection", "anti-ip", "check-for-words").getList(of(String.class), Collections.singletonList("www.google.com")));
            protCfgs.getNode("chat-protection", "anti-ip", "whitelist-words")
                    .setValue(protCfgs.getNode("chat-protection", "anti-ip", "whitelist-words").getList(of(String.class), Arrays.asList("www.myserver.com", "prntscr.com", "gyazo.com", "www.youtube.com")));
            protCfgs.getNode("chat-protection", "anti-ip", "cancel-or-replace").setValue(protCfgs.getNode("chat-protection", "anti-ip", "cancel-or-replace").getString("cancel"));
            protCfgs.getNode("chat-protection", "anti-ip", "cancel-msg").setValue(protCfgs.getNode("chat-protection", "anti-ip", "cancel-msg").getString("&cYou cant send websites or ips on chat"));
            protCfgs.getNode("chat-protection", "anti-ip", "replace-by-word").setValue(protCfgs.getNode("chat-protection", "anti-ip", "replace-by-word").getString("-removed-"));
            protCfgs.getNode("chat-protection", "anti-ip", "punish", "enable").setValue(protCfgs.getNode("chat-protection", "anti-ip", "punish", "enable").getBoolean(false));
            protCfgs.getNode("chat-protection", "anti-ip", "punish", "max-attempts").setValue(protCfgs.getNode("chat-protection", "anti-ip", "punish", "max-attempts").getInt(3));
            protCfgs.getNode("chat-protection", "anti-ip", "punish", "mute-or-cmd").setValue(protCfgs.getNode("chat-protection", "anti-ip", "punish", "mute-or-cmd").getString("mute"));
            protCfgs.getNode("chat-protection", "anti-ip", "punish", "mute-duration").setValue(protCfgs.getNode("chat-protection", "anti-ip", "punish", "mute-duration").getInt(1));
            protCfgs.getNode("chat-protection", "anti-ip", "punish", "mute-msg").setValue(protCfgs.getNode("chat-protection", "anti-ip", "punish", "mute-msg").getString("&cYou have been muted for send IPs or URLs on chat!"));
            protCfgs.getNode("chat-protection", "anti-ip", "punish", "unmute-msg").setValue(protCfgs.getNode("chat-protection", "anti-ip", "punish", "unmute-msg").getString("&aYou can chat again!"));
            protCfgs.getNode("chat-protection", "anti-ip", "punish", "cmd-punish").setValue(protCfgs.getNode("chat-protection", "anti-ip", "punish", "cmd-punish").getString("tempban {player} 10m &cYou have been warned about send links or IPs on chat!"));


        } catch (IOException | ObjectMappingException e1) {
            RedProtect.get().logger.severe("The default configuration could not be loaded or created!");
            e1.printStackTrace();
        }

        RedProtect.get().logger.info("Server version: " + RedProtect.get().game.getPlatform().getMinecraftVersion().getName());

        //add allowed claim worlds to config
        if (root.allowed_claim_worlds.isEmpty()) {
            for (World w : RedProtect.get().serv.getWorlds()) {
                root.allowed_claim_worlds.add(w.getName());
                RedProtect.get().logger.warning("Added world to claim list " + w.getName());
            }
        }

        /*------------- ---- Add default config for not updateable configs ------------------*/

        //update new player flags according version

        int update = 0;
        if (root.config_version < 6.8D) {
            root.config_version = 6.8D;

            if (!root.flags_configuration.enabled_flags.contains("smart-door")) {
                root.flags_configuration.enabled_flags.add("smart-door");
            }
            if (!root.flags_configuration.enabled_flags.contains("allow-potions")) {
                root.flags_configuration.enabled_flags.add("allow-potions");
            }
            if (!root.flags_configuration.enabled_flags.contains("mob-loot")) {
                root.flags_configuration.enabled_flags.add("mob-loot");
            }
            if (!root.flags_configuration.enabled_flags.contains("flow-damage")) {
                root.flags_configuration.enabled_flags.add("flow-damage");
            }
            update++;
        }

        if (root.config_version < 6.9D) {
            root.config_version = 6.9D;
            if (!root.flags_configuration.enabled_flags.contains("allow-fly")) {
                root.flags_configuration.enabled_flags.add("allow-fly");
                root.flags.put("allow-fly", true);
            }
            if (!root.flags_configuration.enabled_flags.contains("can-grow")) {
                root.flags_configuration.enabled_flags.add("can-grow");
                root.flags.put("can-grow", true);
            }
            if (!root.flags_configuration.enabled_flags.contains("teleport")) {
                root.flags_configuration.enabled_flags.add("teleport");
                root.flags.put("teleport", false);
            }
            update++;
        }

        if (root.config_version < 7.0D) {
            root.config_version = 7.0D;
            if (!root.flags_configuration.enabled_flags.contains("allow-effects")) {
                root.flags_configuration.enabled_flags.add("allow-effects");
                root.flags.put("allow-effects", true);
            }
            if (!root.flags_configuration.enabled_flags.contains("use-potions")) {
                root.flags_configuration.enabled_flags.add("use-potions");
                root.flags.put("use-potions", true);
            }
            if (root.flags_configuration.enabled_flags.contains("allow-potions")) {
                root.flags_configuration.enabled_flags.remove("allow-potions");
                root.flags.remove("allow-potions");
            }
            update++;
        }

        if (root.config_version < 7.1D) {
            root.config_version = 7.1D;
            root.language = "EN-US";
            update++;
        }

        if (root.config_version < 7.2D) {
            root.config_version = 7.2D;
            if (!root.flags_configuration.enabled_flags.contains("allow-spawner")) {
                root.flags_configuration.enabled_flags.add("allow-spawner");
                root.flags.put("allow-spawner", false);
            }
            if (!root.flags_configuration.enabled_flags.contains("leaves-decay")) {
                root.flags_configuration.enabled_flags.add("leaves-decay");
                root.flags.put("leaves-decay", false);
            }
            update++;
        }

        if (root.config_version < 7.3D) {
            root.config_version = 7.3D;
            if (!root.flags_configuration.enabled_flags.contains("build")) {
                root.flags_configuration.enabled_flags.add("build");
                root.flags.put("build", false);
            }
            update++;
        }

        if (root.config_version < 7.4D) {
            root.config_version = 7.4D;
            root.private_cat.allowed_blocks.add("minecraft:[a-z_]+_shulker_box");
            update++;
        }

        if (root.config_version < 7.5D) {
            root.config_version = 7.5D;
            root.debug_messages.put("spawn", false);
            update++;
        }

        if (root.config_version < 7.6D) {
            root.config_version = 7.6D;
            if (!root.flags_configuration.enabled_flags.contains("press-plate")) {
                root.flags_configuration.enabled_flags.add("press-plate");
                root.flags.put("press-plate", false);
            }
            update++;
        }

        if (update > 0) {
            RedProtect.get().logger.warning("Configuration UPDATED!");
        }
        /*---------------------------------------- Global Flags for worlds loaded --------------------------------------------*/

        for (World w : Sponge.getServer().getWorlds()) {
            this.loadPerWorlds(w);
        }

        /*------------------------------------------ Gui Items ------------------------------------------*/

        List<String> names = new ArrayList<>();
        Sponge.getRegistry().getAllOf(BlockType.class).forEach((type) -> names.add(type.getName()));

        Sponge.getRegistry().getAllOf(ItemType.class).forEach((type) -> {
            if (!names.contains(type.getName()))
                names.add(type.getName());
        });

        if (names.size() != ecoCfgs.getNode("items", "values").getChildrenList().size()) {
            for (String mat : names) {
                if (ecoCfgs.getNode("items", "values", mat).getValue() == null) {
                    ecoCfgs.getNode("items", "values", mat).setValue(0.0);
                }
            }
        }

        if (RedProtect.get().getPVHelper().getAllEnchants().size() != ecoCfgs.getNode("enchantments", "values").getChildrenList().size()) {
            for (String ench : RedProtect.get().getPVHelper().getAllEnchants()) {
                if (ecoCfgs.getNode("enchantments", "values", ench).getValue() == null) {
                    ecoCfgs.getNode("enchantments", "values", ench).setValue(0.0);
                }
            }
        }

        //////////////////////

        //create logs folder
        File logs = new File(RedProtect.get().configDir + File.separator + "logs");
        if (root.log_actions && !logs.exists()) {
            logs.mkdir();
            RedProtect.get().logger.info("Created folder: " + RedProtect.get().configDir + File.separator + "logs");
        }

        save();
        RedProtect.get().logger.info("All configurations loaded!");

    }

    public FlagGuiCategory guiRoot() {
        return this.guiRoot;
    }

    public GlobalFlagsCategory gFlags() {
        return this.gflags;
    }

    public MainCategory root() {
        return this.root;
    }

    public void loadPerWorlds(World w) {
        if (!root.region_settings.claim.world_types.containsKey(w.getName())) {
            root.region_settings.claim.world_types.put(w.getName(), "BLOCK");
            saveConfig();
        }

        if (!root.region_settings.world_colors.containsKey(w.getName())) {
            if (w.getDimension().getType().equals(DimensionTypes.OVERWORLD)) {
                root.region_settings.world_colors.put(w.getName(), "&a&l");
            } else if (w.getDimension().getType().equals(DimensionTypes.NETHER)) {
                root.region_settings.world_colors.put(w.getName(), "&c&l");
            } else if (w.getDimension().getType().equals(DimensionTypes.THE_END)) {
                root.region_settings.world_colors.put(w.getName(), "&5&l");
            } else {
                root.region_settings.world_colors.put(w.getName(), "&a&l");
            }
            RedProtect.get().logger.warning("Added world to color list " + w.getName());
            saveConfig();
        }

        if (!gflags.worlds.containsKey(w.getName())) {
            gflags.worlds.put(w.getName(), new GlobalFlagsCategory.WorldProperties());
            saveGFlags();
        }
    }

    public Text getGuiString(String string) {
        return RPUtil.toText(guiRoot.gui_strings.get(string));
    }

    public int getGuiSlot(String flag) {
        return guiRoot.gui_flags.get(flag).slot;
    }

    public void setGuiSlot(/*String mat, */String flag, int slot) {
        guiRoot.gui_flags.get(flag).slot = slot;
        //guiRoot.gui_flags.get(flag).material = mat;
        saveGui();
    }

    public ItemStack getGuiSeparator() {
        ItemStack separator = ItemStack.of(Sponge.getRegistry().getType(ItemType.class, guiRoot.gui_separator.material).orElse(ItemTypes.GLASS_PANE), 1);
        separator.offer(Keys.DISPLAY_NAME, getGuiString("separator"));
        separator.offer(Keys.ITEM_DURABILITY, guiRoot.gui_separator.data);
        separator.offer(Keys.ITEM_LORE, Arrays.asList(Text.EMPTY, getGuiString("separator")));
        return separator;
    }

    public int getGuiMaxSlot() {
        SortedSet<Integer> slots = new TreeSet<>(new ArrayList<>());
        for (FlagGuiCategory.GuiFlag key : guiRoot.gui_flags.values()) {
            slots.add(key.slot);
        }
        return Collections.max(slots);
    }

    public HashMap<String, Object> getDefFlagsValues() {
        HashMap<String, Object> flags = new HashMap<>();
        for (Object oflag : root.flags.keySet()) {
            if (oflag instanceof String && isFlagEnabled(((String) oflag).replace("flags.", ""))) {
                String flag = (String) oflag;
                if (flag.equals("pvp") && !root.flags_configuration.enabled_flags.contains("pvp")) {
                    continue;
                }
                flags.put(flag, root.flags.get(flag));
            }
        }
        return flags;
    }

    public boolean isFlagEnabled(String flag) {
        return root.flags_configuration.enabled_flags.contains(flag) || AdminFlags.contains(flag);
    }

    public SortedSet<String> getDefFlags() {
        return new TreeSet<>(getDefFlagsValues().keySet());
    }

    public BlockType getBorderMaterial() {
        if (Sponge.getRegistry().getType(BlockType.class, root.region_settings.border.material).isPresent()) {
            return Sponge.getRegistry().getType(BlockType.class, root.region_settings.border.material).get();
        }
        return BlockTypes.GLOWSTONE;
    }

    private void saveConfig() {
        try {
            configRoot.setValue(of(MainCategory.class), root);
            cfgLoader.save(configRoot);
        } catch (IOException | ObjectMappingException e) {
            e.printStackTrace();
        }
    }

    private void saveGFlags() {
        try {
            gflagsRoot.setValue(of(GlobalFlagsCategory.class), gflags);
            gFlagsLoader.save(gflagsRoot);
        } catch (IOException | ObjectMappingException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            saveConfig();
            saveGFlags();

            ecoManager.save(ecoCfgs);
            protManager.save(protCfgs);
            signManager.save(signCfgs);
            saveGui();
        } catch (IOException e) {
            RedProtect.get().logger.severe("Problems during save file:");
            e.printStackTrace();
        }
    }

    public void saveGui() {
        try {
            guiCfgRoot.setValue(of(FlagGuiCategory.class), guiRoot);
            guiLoader.save(guiCfgRoot);
        } catch (IOException | ObjectMappingException e) {
            RedProtect.get().logger.severe("Problems during save gui file:");
            e.printStackTrace();
        }
    }

    public boolean isAllowedWorld(Player p) {
        return root.allowed_claim_worlds.contains(p.getWorld().getName()) || p.hasPermission("redprotect.admin");
    }

    /*
        public SortedSet<String> getAllFlags() {
            SortedSet<String> values = new TreeSet<>(getDefFlagsValues().keySet());
            values.addAll(new TreeSet<>(AdminFlags));
            return values;
        }
    */
    public boolean addFlag(String flag, boolean defaultValue, boolean isAdmin) {
        if (isAdmin) {
            if (!AdminFlags.contains(flag)) {
                AdminFlags.add(flag);
                return true;
            }
        } else {
            if (!root.flags.containsKey(flag)) {
                root.flags.put(flag, defaultValue);
                root.flags_configuration.enabled_flags.add(flag);
                saveConfig();
                return true;
            }
        }
        return false;
    }

    public int getBlockCost(String itemName) {
        return ecoCfgs.getNode("items", "values", itemName).getInt();
    }

    public int getEnchantCost(String enchantment) {
        return ecoCfgs.getNode("enchantments", "values", enchantment).getInt();
    }

    public String getEcoString(String key) {
        return ecoCfgs.getNode(key).getString("&4Missing economy string for &c" + key);
    }

    public Integer getEcoInt(String key) {
        return ecoCfgs.getNode(key).getInt();
    }

    public boolean getEcoBool(String key) {
        return ecoCfgs.getNode(key).getBoolean();
    }

    public String getWorldClaimType(String w) {
        return root.region_settings.claim.world_types.getOrDefault(w, "");
    }

    public boolean needClaimToBuild(Player p, BlockSnapshot b) {
        boolean bool = root.needed_claim_to_build.worlds.contains(p.getWorld().getName());
        if (bool) {
            if (b != null && root.needed_claim_to_build.allow_only_protections_blocks &&
                    (getWorldClaimType(p.getWorld().getName()).equalsIgnoreCase("BLOCK") ||
                            getWorldClaimType(p.getWorld().getName()).equalsIgnoreCase("BOTH"))) {
                boolean blocks = b.getState().getName().contains(root.region_settings.block_id) ||
                        root.needed_claim_to_build.allow_break_blocks.stream().anyMatch(str -> str.equalsIgnoreCase(b.getState().getId()));
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

    //protection methods
    public int getProtInt(Object... key) {
        return protCfgs.getNode(key).getInt();
    }

    public boolean getProtBool(Object... key) {
        return protCfgs.getNode(key).getBoolean();
    }

    public List<String> getProtStringList(Object... key) {
        try {
            return protCfgs.getNode(key).getList(of(String.class), new ArrayList<>());
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public String getProtString(Object... key) {
        return protCfgs.getNode(key).getString();
    }

    public Text getProtMsg(Object... key) {
        return RPUtil.toText(protCfgs.getNode(key).getString());
    }

    public Text getURLTemplate() {
        return RPUtil.toText(protCfgs.getNode("general", "URL-template").getString());
    }


    public List<Location> getSigns(String rid) {
        List<Location> locs = new ArrayList<>();
        try {
            for (String s : signCfgs.getNode(rid).getList(of(String.class))) {
                String[] val = s.split(",");
                if (!Sponge.getServer().getWorld(val[0]).isPresent()) {
                    continue;
                }
                locs.add(new Location<>(Sponge.getServer().getWorld(val[0]).get(), Double.valueOf(val[1]), Double.valueOf(val[2]), Double.valueOf(val[3])));
            }
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }
        return locs;
    }

    public void putSign(String rid, Location<World> loc) {
        try {
            List<String> lsigns = signCfgs.getNode(rid).getList(of(String.class));
            String locs = loc.getExtent().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ();
            if (!lsigns.contains(locs)) {
                lsigns.add(locs);
                saveSigns(rid, lsigns);
            }
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }
    }

    public void removeSign(String rid, Location<World> loc) {
        try {
            List<String> lsigns = signCfgs.getNode(rid).getList(of(String.class));
            String locs = loc.getExtent().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ();
            if (lsigns.contains(locs)) {
                lsigns.remove(locs);
                saveSigns(rid, lsigns);
            }
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }
    }

    private void saveSigns(String rid, List<String> locs) {
        if (locs.isEmpty()) {
            signCfgs.getNode(rid).setValue(null);
        } else {
            signCfgs.getNode(rid).setValue(locs);
        }
        try {
            signManager.save(signCfgs);
        } catch (IOException e) {
            RedProtect.get().logger.severe("Problems during save file:");
            e.printStackTrace();
        }
    }
}
   
