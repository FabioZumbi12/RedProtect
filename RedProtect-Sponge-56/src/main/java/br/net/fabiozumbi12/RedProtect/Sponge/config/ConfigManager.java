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

package br.net.fabiozumbi12.RedProtect.Sponge.config;

import br.net.fabiozumbi12.RedProtect.Core.config.Category.FlagGuiCategory;
import br.net.fabiozumbi12.RedProtect.Core.config.Category.GlobalFlagsCategory;
import br.net.fabiozumbi12.RedProtect.Core.config.Category.MainCategory;
import br.net.fabiozumbi12.RedProtect.Core.helpers.CoreUtil;
import br.net.fabiozumbi12.RedProtect.Core.helpers.LogLevel;
import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import ninja.leaping.configurate.ConfigurationNode;
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

public class ConfigManager {

    HashMap<String, String> backupGuiName = new HashMap<>();
    HashMap<String, String> backupGuiDescription = new HashMap<>();

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
    public CommentedConfigurationNode ecoCfgs;
    private ConfigurationLoader<CommentedConfigurationNode> ecoManager;
    private ConfigurationLoader<CommentedConfigurationNode> signManager;
    private CommentedConfigurationNode signCfgs;
    private ConfigurationLoader<CommentedConfigurationNode> guiLoader;
    private CommentedConfigurationNode guiCfgRoot;
    private FlagGuiCategory guiRoot;
    private ConfigurationLoader<CommentedConfigurationNode> gFlagsLoader;
    private CommentedConfigurationNode gflagsRoot;
    private GlobalFlagsCategory globalFlagsRoot;
    private CommentedConfigurationNode configRoot;
    private ConfigurationLoader<CommentedConfigurationNode> cfgLoader;
    private MainCategory root;

    //init
    public ConfigManager(GuiceObjectMapperFactory factory) throws ObjectMappingException {
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

            File defConfig = new File(RedProtect.get().configDir, "config.conf");
            cfgLoader = HoconConfigurationLoader.builder().setFile(defConfig).build();
            configRoot = cfgLoader.load(ConfigurationOptions.defaults().setObjectMapperFactory(factory).setShouldCopyDefaults(true).setHeader(header));
            this.root = configRoot.getValue(of(MainCategory.class), new MainCategory(Sponge.getServer().getOnlineMode()));

            //Defaults per server
            if (this.root.private_cat.allowed_blocks.isEmpty()) {
                this.root.private_cat.allowed_blocks = new ArrayList<>(Arrays.asList(
                        "minecraft:dispenser",
                        "minecraft:anvil",
                        "minecraft:note_block",
                        "minecraft:bed_block",
                        "minecraft:chest",
                        "minecraft:workbench",
                        "minecraft:furnace",
                        "minecraft:jukebox",
                        "minecraft:enchantment_table",
                        "minecraft:brewing_stand",
                        "minecraft:cauldron",
                        "minecraft:ender_chest",
                        "minecraft:beacon",
                        "minecraft:trapped_chest",
                        "minecraft:hopper",
                        "minecraft:dropper",
                        "minecraft:[a-z_]+_shulker_box"));
            }
            if (this.root.needed_claim_to_build.allow_break_blocks.isEmpty()) {
                this.root.needed_claim_to_build.allow_break_blocks = Arrays.asList(BlockTypes.GRASS.getId(), BlockTypes.TALLGRASS.getId());
            }
            if (this.root.region_settings.block_id.isEmpty()) {
                this.root.region_settings.block_id = BlockTypes.FENCE.getId();
            }
            if (this.root.region_settings.border.material.isEmpty()) {
                this.root.region_settings.border.material = BlockTypes.GLOWSTONE.getId();
            }
            if (this.root.wands.adminWandID.isEmpty()) {
                this.root.wands.adminWandID = ItemTypes.GLASS_BOTTLE.getId();
            }
            if (this.root.wands.infoWandID.isEmpty()) {
                this.root.wands.infoWandID = ItemTypes.PAPER.getId();
            }
            if (root.debug_messages.isEmpty()) {
                for (LogLevel level : LogLevel.values()) {
                    root.debug_messages.put(level.name().toLowerCase(), false);
                }
            }

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

            File gFlagsConfig = new File(RedProtect.get().configDir, "globalflags.conf");
            gFlagsLoader = HoconConfigurationLoader.builder().setFile(gFlagsConfig).build();
            gflagsRoot = gFlagsLoader.load(ConfigurationOptions.defaults().setObjectMapperFactory(factory).setShouldCopyDefaults(true).setHeader(headerg));

            //import old world values
            if (gFlagsConfig.exists() && !gflagsRoot.getNode("worlds").hasMapChildren()) {
                Object values = gflagsRoot.getValue();
                gflagsRoot.setValue(null);
                gflagsRoot.getNode("worlds").setValue(values);
                RedProtect.get().logger.warning("File \"globalflags.conf\" updated with new configurations!");
            }

            this.globalFlagsRoot = gflagsRoot.getValue(of(GlobalFlagsCategory.class), new GlobalFlagsCategory());

            /*--------------------- end globalflags.conf ---------------------------*/

            /*--------------------- guiconfig.conf ---------------------------*/
            String headerGui = ""
                    + "+--------------------------------------------------------------------+ #\n"
                    + "<             RedProtect Gui Flags configuration File                > #\n"
                    + "<--------------------------------------------------------------------> #\n"
                    + "<            This is the gui flags configuration file.               > #\n"
                    + "<                       Feel free to edit it.                        > #\n"
                    + "<         https://github.com/FabioZumbi12/RedProtect/wiki            > #\n"
                    + "+--------------------------------------------------------------------+ #\n";

            String guiFileName = "guiconfig" + configRoot().language + ".conf";
            if (new File(RedProtect.get().configDir, guiFileName).exists()){
                new File(RedProtect.get().configDir, guiFileName).renameTo(new File(RedProtect.get().configDir, "guiconfig.conf"));
            }
            guiLoader = HoconConfigurationLoader.builder().setFile(new File(RedProtect.get().configDir, "guiconfig.conf")).build();
            guiCfgRoot = guiLoader.load(ConfigurationOptions.defaults().setObjectMapperFactory(factory).setShouldCopyDefaults(true).setHeader(headerGui));
            this.guiRoot = guiCfgRoot.getValue(of(FlagGuiCategory.class), new FlagGuiCategory());

            // Import old gui translations
            if (guiCfgRoot.getNode("gui-strings").getValue() != null){
                guiCfgRoot.removeChild("gui-strings");
                for (Map.Entry<Object, ? extends ConfigurationNode> key:guiCfgRoot.getNode("gui-flags").getChildrenMap().entrySet()){
                    if (key.getValue().getNode("name").getValue() != null){
                        backupGuiName.put(key.getKey().toString(), key.getValue().getNode("name").getString());
                        key.getValue().removeChild("name");
                    }
                    StringBuilder description = new StringBuilder();
                    if (key.getValue().getNode("description").getValue() != null){
                        description.append(key.getValue().getNode("description").getString()).append("/n");
                        key.getValue().removeChild("description");
                    }
                    if (key.getValue().getNode("description1").getValue() != null){
                        description.append(key.getValue().getNode("description1").getString()).append("/n");
                        key.getValue().removeChild("description1");
                    }
                    if (key.getValue().getNode("description2").getValue() != null){
                        description.append(key.getValue().getNode("description2").getString()).append("/n");
                        key.getValue().removeChild("description2");
                    }
                    if (description.length() > 0){
                        backupGuiDescription.put(key.getKey().toString(), description.substring(0, description.length()-2));
                    }
                }
            }

            if (this.guiRoot.gui_separator.material.isEmpty())
                this.guiRoot.gui_separator.material = ItemTypes.STAINED_GLASS_PANE.getId();

            if (this.guiRoot.gui_flags.isEmpty()) {
                this.guiRoot.gui_flags.put("allow-effects", new FlagGuiCategory.GuiFlag(ItemTypes.BLAZE_ROD.getId(), 16));
                this.guiRoot.gui_flags.put("allow-fly", new FlagGuiCategory.GuiFlag(ItemTypes.FEATHER.getId(), 8));
                this.guiRoot.gui_flags.put("allow-home", new FlagGuiCategory.GuiFlag(ItemTypes.COMPASS.getId(), 2));
                this.guiRoot.gui_flags.put("allow-potions", new FlagGuiCategory.GuiFlag(ItemTypes.POTION.getId(), 26));
                this.guiRoot.gui_flags.put("allow-spawner", new FlagGuiCategory.GuiFlag(ItemTypes.MOB_SPAWNER.getId(), 10));
                this.guiRoot.gui_flags.put("build", new FlagGuiCategory.GuiFlag(ItemTypes.GRASS.getId(), 13));
                this.guiRoot.gui_flags.put("button", new FlagGuiCategory.GuiFlag(ItemTypes.STONE_BUTTON.getId(), 6));
                this.guiRoot.gui_flags.put("can-grow", new FlagGuiCategory.GuiFlag(ItemTypes.WHEAT.getId(), 27));
                this.guiRoot.gui_flags.put("chest", new FlagGuiCategory.GuiFlag(ItemTypes.TRAPPED_CHEST.getId(), 3));
                this.guiRoot.gui_flags.put("door", new FlagGuiCategory.GuiFlag(ItemTypes.WOODEN_DOOR.getId(), 0));
                this.guiRoot.gui_flags.put("ender-chest", new FlagGuiCategory.GuiFlag(ItemTypes.ENDER_CHEST.getId(), 22));
                this.guiRoot.gui_flags.put("fire", new FlagGuiCategory.GuiFlag(ItemTypes.BLAZE_POWDER.getId(), 9));
                this.guiRoot.gui_flags.put("fishing", new FlagGuiCategory.GuiFlag(ItemTypes.FISHING_ROD.getId(), 28));
                this.guiRoot.gui_flags.put("flow", new FlagGuiCategory.GuiFlag(ItemTypes.WATER_BUCKET.getId(), 29));
                this.guiRoot.gui_flags.put("flow-damage", new FlagGuiCategory.GuiFlag(ItemTypes.LAVA_BUCKET.getId(), 30));
                this.guiRoot.gui_flags.put("iceform-player", new FlagGuiCategory.GuiFlag(ItemTypes.PACKED_ICE.getId(), 4));
                this.guiRoot.gui_flags.put("iceform-world", new FlagGuiCategory.GuiFlag(ItemTypes.ICE.getId(), 31));
                this.guiRoot.gui_flags.put("leaves-decay", new FlagGuiCategory.GuiFlag(ItemTypes.LEAVES.getId(), 18));
                this.guiRoot.gui_flags.put("lever", new FlagGuiCategory.GuiFlag(ItemTypes.LEVER.getId(), 5));
                this.guiRoot.gui_flags.put("minecart", new FlagGuiCategory.GuiFlag(ItemTypes.MINECART.getId(), 25));
                this.guiRoot.gui_flags.put("mob-loot", new FlagGuiCategory.GuiFlag(ItemTypes.MYCELIUM.getId(), 32));
                this.guiRoot.gui_flags.put("passives", new FlagGuiCategory.GuiFlag(ItemTypes.SADDLE.getId(), 33));
                this.guiRoot.gui_flags.put("pvp", new FlagGuiCategory.GuiFlag(ItemTypes.STONE_SWORD.getId(), 17));
                this.guiRoot.gui_flags.put("press-plate", new FlagGuiCategory.GuiFlag(ItemTypes.LIGHT_WEIGHTED_PRESSURE_PLATE.getId(), 20));
                this.guiRoot.gui_flags.put("smart-door", new FlagGuiCategory.GuiFlag(ItemTypes.IRON_DOOR.getId(), 1));
                this.guiRoot.gui_flags.put("spawn-animals", new FlagGuiCategory.GuiFlag(ItemTypes.EGG.getId(), 34));
                this.guiRoot.gui_flags.put("spawn-monsters", new FlagGuiCategory.GuiFlag(ItemTypes.PUMPKIN.getId(), 35));
                this.guiRoot.gui_flags.put("teleport", new FlagGuiCategory.GuiFlag(ItemTypes.ENDER_PEARL.getId(), 19));
                this.guiRoot.gui_flags.put("use-potions", new FlagGuiCategory.GuiFlag(ItemTypes.GLASS_BOTTLE.getId(), 26));
            }

            for (String key: getDefFlagsValues().keySet()) {
                this.guiRoot.gui_flags.putIfAbsent(key, new FlagGuiCategory.GuiFlag("golden_apple", 0));
            }

            //Economy file
            File ecoFile = new File(RedProtect.get().configDir, "economy.conf");
            if (!ecoFile.exists()) {
                Asset ecoAsset = RedProtect.get().container.getAsset("economy.conf").get();
                ecoAsset.copyToDirectory(RedProtect.get().configDir.toPath());
            }
            ecoManager = HoconConfigurationLoader.builder().setPath(ecoFile.toPath()).build();
            ecoCfgs = ecoManager.load();

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

            //Signs file
            File signFile = new File(RedProtect.get().configDir, "signs.conf");
            signManager = HoconConfigurationLoader.builder().setPath(signFile.toPath()).build();
            signCfgs = signManager.load();
        } catch (IOException e1) {
            RedProtect.get().logger.severe("The default configuration could not be loaded or created!");
            e1.printStackTrace();
        }

        RedProtect.get().logger.info("Server version: " + Sponge.getGame().getPlatform().getMinecraftVersion().getName());

        for (World w : RedProtect.get().getServer().getWorlds()) {
            this.addWorldProperties(w);
        }

        //add allowed claim worlds to config
        if (root.allowed_claim_worlds.isEmpty()) {
            for (World w : RedProtect.get().getServer().getWorlds()) {
                root.allowed_claim_worlds.add(w.getName());
                RedProtect.get().logger.warning("Added world to allowed claim list " + w.getName());
            }
        }

        /*------------- ---- Add default config for not updatable configs ------------------*/

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

        //create logs folder
        File logs = new File(RedProtect.get().configDir + File.separator + "logs");
        if (root.log_actions && !logs.exists()) {
            logs.mkdir();
            RedProtect.get().logger.info("Created folder: " + RedProtect.get().configDir + File.separator + "logs");
        }

        save();
        RedProtect.get().logger.info("All configurations loaded!");
    }

    public void addWorldProperties(World w) {
        //add worlds to claim types list
        if (!root.region_settings.claim.world_types.containsKey(w.getName())) {
            root.region_settings.claim.world_types.put(w.getName(), "BLOCK");
            RedProtect.get().logger.warning("Added world to claim types list " + w.getName());
        }
        //add worlds to color list
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
            RedProtect.get().logger.warning("Added world to colors list " + w.getName());
            saveConfig();
        }
        //add world to globalflags
        if (!globalFlagsRoot.worlds.containsKey(w.getName())) {
            globalFlagsRoot.worlds.put(w.getName(), new GlobalFlagsCategory.WorldProperties());
            saveGFlags();
        }
    }

    public FlagGuiCategory guiRoot() {
        return this.guiRoot;
    }

    public GlobalFlagsCategory globalFlagsRoot() {
        return this.globalFlagsRoot;
    }

    public MainCategory configRoot() {
        return this.root;
    }

    public int getGuiSlot(String flag) {
        return guiRoot.gui_flags.get(flag).slot;
    }

    public void setGuiSlot(String flag, int slot) {
        guiRoot.gui_flags.get(flag).slot = slot;
        saveGui();
    }

    public ItemStack getGuiSeparator() {
        ItemStack separator = ItemStack.of(Sponge.getRegistry().getType(ItemType.class, guiRoot.gui_separator.material).orElse(ItemTypes.GLASS_PANE), 1);
        separator.offer(Keys.DISPLAY_NAME, RedProtect.get().guiLang.getFlagString("separator"));
        separator.offer(Keys.ITEM_DURABILITY, guiRoot.gui_separator.data);
        separator.offer(Keys.ITEM_LORE, Arrays.asList(Text.EMPTY, RedProtect.get().guiLang.getFlagString("separator")));
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
        for (Map.Entry<String, Boolean> flag : root.flags.entrySet()) {
            if (isFlagEnabled(flag.getKey())) {
                if (flag.getKey().equals("pvp") && !root.flags_configuration.enabled_flags.contains("pvp")) {
                    continue;
                }
                flags.put(flag.getKey(), flag.getValue());
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
            CoreUtil.printJarVersion();
            e.printStackTrace();
        }
    }

    private void saveGFlags() {
        try {
            gflagsRoot.setValue(of(GlobalFlagsCategory.class), globalFlagsRoot);
            gFlagsLoader.save(gflagsRoot);
        } catch (IOException | ObjectMappingException e) {
            CoreUtil.printJarVersion();
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            saveConfig();
            saveGFlags();

            ecoManager.save(ecoCfgs);
            signManager.save(signCfgs);
            saveGui();
        } catch (IOException e) {
            RedProtect.get().logger.severe("Problems during save file:");
            CoreUtil.printJarVersion();
            CoreUtil.printJarVersion();
            e.printStackTrace();
        }
    }

    public void saveGui() {
        try {
            guiCfgRoot.setValue(of(FlagGuiCategory.class), guiRoot);
            guiLoader.save(guiCfgRoot);
        } catch (IOException | ObjectMappingException e) {
            RedProtect.get().logger.severe("Problems during save gui file:");
            CoreUtil.printJarVersion();
            CoreUtil.printJarVersion();
            e.printStackTrace();
        }
    }

    public boolean isAllowedWorld(Player p) {
        return root.allowed_claim_worlds.contains(p.getWorld().getName()) || p.hasPermission("redprotect.bypass.world");
    }

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
        return ecoCfgs.getNode("enchantments", "values", enchantment).getInt(-1);
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
                    RedProtect.get().lang.sendMessage(p, "need.claim.blockids");
                } else {
                    return false;
                }
            }
            RedProtect.get().lang.sendMessage(p, "need.claim.tobuild");
        }
        return bool;
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
            CoreUtil.printJarVersion();
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
            CoreUtil.printJarVersion();
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
            CoreUtil.printJarVersion();
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
            CoreUtil.printJarVersion();
            e.printStackTrace();
        }
    }
}
   
