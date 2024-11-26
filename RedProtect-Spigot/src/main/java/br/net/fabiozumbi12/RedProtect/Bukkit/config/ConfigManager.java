/*
 * Copyright (c) 2012-2024 - @FabioZumbi12
 * Last Modified: 26/11/2024 17:51
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

package br.net.fabiozumbi12.RedProtect.Bukkit.config;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.helpers.MobTextures;
import br.net.fabiozumbi12.RedProtect.Core.config.Category.*;
import br.net.fabiozumbi12.RedProtect.Core.config.CoreConfigManager;
import br.net.fabiozumbi12.RedProtect.Core.helpers.CoreUtil;
import br.net.fabiozumbi12.RedProtect.Core.helpers.LogLevel;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.google.common.reflect.TypeToken.of;

public class ConfigManager extends CoreConfigManager {

    //init
    public ConfigManager() throws ObjectMappingException {
        super(RedProtect.get().getDataFolder());

        try {

            /*--------------------- config.yml ---------------------------*/
            cfgLoader = HoconConfigurationLoader.builder().setFile(new File(RedProtect.get().getDataFolder(), "config.conf")).build();

            if (new File(RedProtect.get().getDataFolder(), "config.yml").exists()) {
                File defConfig = new File(RedProtect.get().getDataFolder(), "config.yml");
                ConfigurationLoader<ConfigurationNode> cfgLoader = YAMLConfigurationLoader.builder().setFile(defConfig).build();
                configRoot = cfgLoader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true).setHeader(headerCfg));
                defConfig.renameTo(new File(RedProtect.get().getDataFolder(), "config_BKP.yml"));
            } else {
                configRoot = cfgLoader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true).setHeader(headerCfg));
            }
            this.root = configRoot.getValue(of(MainCategory.class), new MainCategory(Bukkit.getOnlineMode()));

            if (!configRoot.getNode("region-settings", "border", "material").isVirtual()) {
                configRoot.getNode("region-settings", "border", "material").setValue(null);
            }
            if (!configRoot.getNode("flags-configuration", "enabled-flags").isVirtual()) {
                configRoot.getNode("flags-configuration", "enabled-flags").setValue(null);
            }

            //Defaults per server
            if (this.root.private_cat.allowed_blocks.isEmpty()) {
                this.root.private_cat.allowed_blocks = new ArrayList<>(Arrays.asList(
                        "ANVIL",
                        "DAMAGED_ANVIL",
                        "CHIPPED_ANVIL",
                        "DISPENSER",
                        "DISPENSER",
                        "NOTE_BLOCK",
                        "BED_BLOCK",
                        "CHEST",
                        "CRAFTING_TABLE",
                        "FURNACE",
                        "LIT_FURNACE",
                        "JUKEBOX",
                        "ENCHANTING_TABLE",
                        "BREWING_STAND",
                        "CAULDRON",
                        "ENDER_CHEST",
                        "BEACON",
                        "TRAPPED_CHEST",
                        "HOPPER",
                        "DROPPER",
                        "[A-Z_]+_SHULKER_BOX",
                        "COMPOSTER",
                        "BARREL",
                        "LOOM",
                        "SMOKER",
                        "BLAST_FURNACE",
                        "BURNING_FURNACE",
                        "CARTOGRAPHY_TABLE",
                        "FLETCHING_TABLE",
                        "GRINDSTONE",
                        "SMITHING_TABLE",
                        "STONECUTTER"));
            }
            if (this.root.needed_claim_to_build.allow_break_blocks.isEmpty()) {
                this.root.needed_claim_to_build.allow_break_blocks = List.of(Material.DIRT.name());
            }
            if (this.root.needed_claim_to_build.allow_interact_blocks.isEmpty()) {
                this.root.needed_claim_to_build.allow_interact_blocks = Arrays.asList(
                        Arrays.stream(Material.values()).filter(m -> m.name().contains("BUTTON")).findFirst().get().name(),
                        Arrays.stream(Material.values()).filter(m -> m.name().contains("DOOR")).findFirst().get().name(),
                        Material.SAND.name(),
                        Material.IRON_DOOR.name(),
                        Material.STONE_BUTTON.name(),
                        Material.LEVER.name());
            }
            if (this.root.region_settings.block_id.isEmpty()) {
                this.root.region_settings.block_id = "FENCE";
            }
            if (this.root.wands.adminWandID.isEmpty()) {
                this.root.wands.adminWandID = Material.GLASS_BOTTLE.name();
            }
            if (this.root.wands.infoWandID.isEmpty()) {
                this.root.wands.infoWandID = Material.PAPER.name();
            }
            if (root.debug_messages.isEmpty()) {
                for (LogLevel level : LogLevel.values()) {
                    root.debug_messages.put(level.name().toLowerCase(), false);
                }
            }

            /*--------------------- end config.yml ---------------------------*/
            gFlagsLoader = HoconConfigurationLoader.builder().setFile(new File(RedProtect.get().getDataFolder(), "globalflags.conf")).build();

            if (new File(RedProtect.get().getDataFolder(), "globalflags.yml").exists()) {
                File gFlagsConfig = new File(RedProtect.get().getDataFolder(), "globalflags.yml");
                ConfigurationLoader<ConfigurationNode> gFlagsLoader = YAMLConfigurationLoader.builder().setFile(gFlagsConfig).build();
                gflagsRoot = gFlagsLoader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true).setHeader(headerGf));
                gFlagsConfig.renameTo(new File(RedProtect.get().getDataFolder(), "globalflags_BKP.yml"));
            } else {
                gflagsRoot = gFlagsLoader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true).setHeader(headerGf));
            }
            this.globalFlagsRoot = gflagsRoot.getValue(of(GlobalFlagsCategory.class), new GlobalFlagsCategory());

            /*--------------------- guiconfig.yml ---------------------------*/
            String guiFileName = "guiconfig" + configRoot().language + ".conf";
            if (new File(RedProtect.get().getDataFolder(), guiFileName).exists()) {
                new File(RedProtect.get().getDataFolder(), guiFileName).renameTo(new File(RedProtect.get().getDataFolder(), "guiconfig.conf"));
            }
            guiCfgLoader = HoconConfigurationLoader.builder().setFile(new File(RedProtect.get().getDataFolder(), "guiconfig.conf")).build();

            if (new File(RedProtect.get().getDataFolder(), "guiconfig.yml").exists()) {
                File guiOldCfg = new File(RedProtect.get().getDataFolder(), "guiconfig.yml");
                ConfigurationLoader<ConfigurationNode> guiLoader = YAMLConfigurationLoader.builder().setFile(guiOldCfg).build();
                guiCfgRoot = guiLoader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true).setHeader(headerGui));
                guiOldCfg.renameTo(new File(RedProtect.get().getDataFolder(), "guiconfig_BKP.yml"));
            } else {
                guiCfgRoot = guiCfgLoader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true).setHeader(headerGui));
            }
            this.guiRoot = guiCfgRoot.getValue(of(FlagGuiCategory.class), new FlagGuiCategory());

            // Import old gui translations
            if (guiCfgRoot.getNode("gui-strings").getValue() != null) {
                guiCfgRoot.removeChild("gui-strings");
                for (Map.Entry<Object, ? extends ConfigurationNode> key : guiCfgRoot.getNode("gui-flags").getChildrenMap().entrySet()) {
                    if (key.getValue().getNode("name").getValue() != null) {
                        backupGuiName.put(key.getKey().toString(), key.getValue().getNode("name").getString());
                        key.getValue().removeChild("name");
                    }
                    StringBuilder description = new StringBuilder();
                    if (key.getValue().getNode("description").getValue() != null) {
                        description.append(key.getValue().getNode("description").getString()).append("/n");
                        key.getValue().removeChild("description");
                    }
                    if (key.getValue().getNode("description1").getValue() != null) {
                        description.append(key.getValue().getNode("description1").getString()).append("/n");
                        key.getValue().removeChild("description1");
                    }
                    if (key.getValue().getNode("description2").getValue() != null) {
                        description.append(key.getValue().getNode("description2").getString()).append("/n");
                        key.getValue().removeChild("description2");
                    }
                    if (!description.isEmpty()) {
                        backupGuiDescription.put(key.getKey().toString(), description.substring(0, description.length() - 2));
                    }
                }
            }

            /*------------------- Economy File -------------------*/
            ecoLoader = HoconConfigurationLoader.builder().setPath(new File(RedProtect.get().getDataFolder(), "economy.conf").toPath()).build();
            if (new File(RedProtect.get().getDataFolder(), "economy.yml").exists()) {
                File ecoConfig = new File(RedProtect.get().getDataFolder(), "economy.yml");
                ConfigurationLoader<ConfigurationNode> ecoLoader = YAMLConfigurationLoader.builder().setPath(ecoConfig.toPath()).build();
                ecoCfgRoot = ecoLoader.load();
                ecoConfig.renameTo(new File(RedProtect.get().getDataFolder(), "economy_BKP.yml"));
            } else {
                if (!new File(RedProtect.get().getDataFolder(), "economy.conf").exists()) {
                    RedProtect.get().saveResource("economy.conf", false);
                }
                ecoCfgRoot = ecoLoader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true).setHeader(headerEco));
            }
            this.ecoRoot = ecoCfgRoot.getValue(of(EconomyCategory.class), new EconomyCategory());

            if (ecoRoot.items.values.size() < Material.values().length) {
                for (Material mat : Material.values()) {
                    if (!ecoRoot.items.values.containsKey(mat.name())) {
                        ecoRoot.items.values.put(mat.name(), 10L);
                    }
                }
            }
            if (ecoRoot.enchantments.values.size() < Enchantment.values().length) {
                for (Enchantment ench : Enchantment.values()) {
                    if (!ecoRoot.enchantments.values.containsKey(ench.getName())) {
                        ecoRoot.enchantments.values.put(ench.getName(), 10L);
                    }
                }
            }

            /* ----------------- Head textures config file -----------------*/
            headTextLoader = HoconConfigurationLoader.builder().setPath(new File(RedProtect.get().getDataFolder(), "textures.conf").toPath()).build();
            headTextCfgRoot = headTextLoader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true).setHeader(headerHeadtext));
            this.headTextRoot = headTextCfgRoot.getValue(of(HeadTexturesCategory.class), new HeadTexturesCategory());

            Registry.ENTITY_TYPE.stream().forEach(e -> {
                if (!headTextRoot.mobTextures.containsKey(e.name()))
                    headTextRoot.mobTextures.put(e.name(), MobTextures.getTexture(e));
            });
            saveHeadTextures();

            /* ----------------- Signs file ----------------- */
            signsLoader = HoconConfigurationLoader.builder().setPath(new File(RedProtect.get().getDataFolder(), "signs.conf").toPath()).build();
            if (new File(RedProtect.get().getDataFolder(), "signs.yml").exists()) {
                File signFile = new File(RedProtect.get().getDataFolder(), "signs.yml");
                ConfigurationLoader<ConfigurationNode> signsLoader = YAMLConfigurationLoader.builder().setPath(signFile.toPath()).build();
                signCfgs = signsLoader.load();
                signFile.renameTo(new File(RedProtect.get().getDataFolder(), "signs_BKP.yml"));
            } else {
                signCfgs = signsLoader.load();
            }
        } catch (IOException e1) {
            RedProtect.get().logger.severe("The default configuration could not be loaded or created!");
            e1.printStackTrace();
        }

        RedProtect.get().logger.info("Server version: " + Bukkit.getServer().getBukkitVersion());

        //Load configs per world
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

            if (!root.flags.containsKey("smart-door")) {
                root.flags.put("smart-door", true);
            }
            if (!root.flags.containsKey("allow-potions")) {
                root.flags.put("allow-potions", true);
            }
            if (!root.flags.containsKey("mob-loot")) {
                root.flags.put("mob-loot", false);
            }
            if (!root.flags.containsKey("flow-damage")) {
                root.flags.put("flow-damage", false);
            }
            update++;
        }

        if (root.config_version < 6.9D) {
            root.config_version = 6.9D;
            if (!root.flags.containsKey("iceform-player")) {
                root.flags.put("iceform-player", false);
            }
            if (!root.flags.containsKey("iceform-world")) {
                root.flags.put("iceform-world", true);
            }
            update++;
        }

        if (root.config_version < 7.0D) {
            root.config_version = 7.0D;
            if (!root.flags.containsKey("allow-fly")) {
                root.flags.put("allow-fly", true);
            }
            update++;
        }

        if (root.config_version < 7.1D) {
            root.config_version = 7.1D;
            if (!root.flags.containsKey("teleport")) {
                root.flags.put("teleport", true);
            }
            update++;
        }

        if (root.config_version < 7.3D) {
            root.config_version = 7.3D;
            if (!root.flags.containsKey("ender-chest")) {
                root.flags.put("ender-chest", false);
            }
            update++;
        }

        if (root.config_version < 7.4D) {
            root.config_version = 7.4D;
            root.private_cat.allowed_blocks.add("[A-Z_]+_SHULKER_BOX");
            update++;
        }

        if (root.config_version < 7.5D) {
            root.config_version = 7.5D;
            if (!root.flags.containsKey("can-grow")) {
                root.flags.put("can-grow", true);
            }
            update++;
        }

        if (root.config_version < 7.6D) {
            root.config_version = 7.6D;
            if (!root.flags.containsKey("allow-effects")) {
                root.flags.put("allow-effects", true);
            }
            update++;
        }

        if (root.config_version < 7.7D) {
            root.config_version = 7.7D;
            if (!root.flags.containsKey("allow-spawner")) {
                root.flags.put("allow-spawner", false);
            }
            update++;
        }

        if (root.config_version < 7.8D) {
            root.config_version = 7.8D;
            if (!root.flags.containsKey("leaves-decay")) {
                root.flags.put("leaves-decay", true);
            }
            update++;
        }

        if (root.config_version < 7.9D) {
            root.config_version = 7.9D;
            if (!root.flags.containsKey("build")) {
                root.flags.put("build", false);
            }
            update++;
        }

        if (root.config_version < 8.3D) {
            root.config_version = 8.3D;
            if (!root.flags.containsKey("press-plate")) {
                root.flags.put("press-plate", false);
            }
            if (!root.flags.containsKey("fishing")) {
                root.flags.put("fishing", false);
            }
            update++;
        }

        if (root.config_version < 8.4D) {
            root.config_version = 8.4D;
            root.private_cat.allowed_blocks.add("COMPOSTER");
            root.private_cat.allowed_blocks.add("BARREL");
            root.private_cat.allowed_blocks.add("LOOM");
            root.private_cat.allowed_blocks.add("SMOKER");
            root.private_cat.allowed_blocks.add("BLAST_FURNACE");
            root.private_cat.allowed_blocks.add("CARTOGRAPHY_TABLE");
            root.private_cat.allowed_blocks.add("FLETCHING_TABLE");
            root.private_cat.allowed_blocks.add("GRINDSTONE");
            root.private_cat.allowed_blocks.add("SMITHING_TABLE");
            root.private_cat.allowed_blocks.add("STONECUTTER");
            update++;
        }

        if (root.config_version < 8.5D) {
            root.config_version = 8.5D;
            if (!root.flags.containsKey("gravity")) {
                root.flags.put("gravity", true);
            }
            update++;
        }

        if (root.config_version < 8.6D) {
            root.config_version = 8.6D;
            if (!root.flags.containsKey("redstone")) {
                root.flags.put("redstone", false);
            }
            update++;
        }

        if (root.config_version < 8.7D) {
            root.config_version = 8.7D;
            if (!root.flags.containsKey("block-transform")) {
                root.flags.put("block-transform", true);
            }
            update++;
        }

        if (root.config_version < 8.8D) {
            root.config_version = 8.8D;
            if (root.flags.containsKey("dynmap")) {
                root.flags.put("show-map", true);
            }
            update++;
        }

        if (update > 0) {
            RedProtect.get().logger.warning("Configuration UPDATED!");
        }

        //////////////////////

        //create logs folder
        File logs = new File(RedProtect.get().getDataFolder(), File.separator + "logs");
        if (root.log_actions && !logs.exists()) {
            logs.mkdir();
            RedProtect.get().logger.info("Created folder: " + RedProtect.get().getDataFolder() + File.separator + "logs");
        }

        save();
        RedProtect.get().logger.info("All configurations loaded!");

    }

    public void addWorldProperties(World w) {
        try {
            //add worlds to claim types list
            if (!root.region_settings.claim.world_types.containsKey(w.getName())) {
                root.region_settings.claim.world_types.put(w.getName(), "BLOCK");
                RedProtect.get().logger.warning("Added world to claim types list " + w.getName());
            }
            //add world to globalflags
            if (!globalFlagsRoot.worlds.containsKey(w.getName())) {
                globalFlagsRoot.worlds.put(w.getName(), new GlobalFlagsCategory.WorldProperties());
            }
            //add worlds to color list
            if (!root.region_settings.world_colors.containsKey(w.getName()) && w.getEnvironment() != null) {
                switch (w.getEnvironment()) {
                    case NORMAL:
                        root.region_settings.world_colors.put(w.getName(), "&a&l");
                    case NETHER:
                        root.region_settings.world_colors.put(w.getName(), "&c&l");
                    case THE_END:
                        root.region_settings.world_colors.put(w.getName(), "&5&l");
                    default:
                        root.region_settings.world_colors.put(w.getName(), "&a&l");
                }
                RedProtect.get().logger.warning("Added world to colors list " + w.getName());
            }
        } catch (Exception ex) {
            RedProtect.get().logger.warning("Error on add config properties for world " + w.getName() + ": " + ex.getMessage());
            ex.printStackTrace();
        }
        saveGFlags();
    }

    public void setGuiSlot(/*String mat, */String flag, int slot) {
        guiRoot.gui_flags.get(flag).slot = slot;
        saveGui();
    }

    public ItemStack getGuiSeparator() {
        ItemStack separator;
        try {
            separator = new ItemStack(Material.getMaterial(guiRoot.gui_separator.material), 1, (short) guiRoot.gui_separator.data);
        } catch (Exception ignored) {
            Optional<Material> optMat = Arrays.stream(Material.values()).filter(m -> m.name().contains("PANE") && !m.isBlock()).findFirst();
            separator = optMat.map(material -> new ItemStack(material, 1, (short) guiRoot.gui_separator.data)).orElseGet(() -> new ItemStack(Material.GLASS, 1, (short) guiRoot.gui_separator.data));
        }

        ItemMeta meta = separator.getItemMeta();
        meta.setDisplayName(RedProtect.get().guiLang.getFlagString("separator"));
        meta.setLore(Arrays.asList("", RedProtect.get().guiLang.getFlagString("separator")));
        separator.setItemMeta(meta);
        return separator;
    }

    public boolean isAllowedWorld(Player p) {
        return root.allowed_claim_worlds.contains(p.getWorld().getName()) || p.hasPermission("redprotect.allowed-claim-world." + p.getWorld().getName()) || p.hasPermission("redprotect.bypass.world");
    }

    public boolean needClaimToBuild(Player p, Block b) {
        if (p.hasPermission("redprotect.need-claim-to-build.bypass"))
            return false;

        boolean bool = root.needed_claim_to_build.worlds.contains(p.getWorld().getName());
        if (bool) {
            if (b != null && root.needed_claim_to_build.allow_only_protections_blocks &&
                    (getWorldClaimType(p.getWorld().getName()).equalsIgnoreCase("BLOCK") ||
                            getWorldClaimType(p.getWorld().getName()).equalsIgnoreCase("BOTH"))) {
                boolean blocks = (b.getType().name().contains(root.region_settings.block_id.toUpperCase()) || b.getType().name().contains("SIGN")) ||
                        root.needed_claim_to_build.allow_break_blocks.stream().anyMatch(str -> str.equalsIgnoreCase(b.getType().name()));
                if (!blocks) {
                    RedProtect.get().getLanguageManager().sendMessage(p, "need.claim.blockids");
                } else {
                    return false;
                }
            }
            RedProtect.get().getLanguageManager().sendMessage(p, "need.claim.tobuild");
        }
        return bool;
    }

    public boolean needClaimToInteract(Player p, Block b) {
        if (p.hasPermission("redprotect.need-claim-to-build.bypass"))
            return false;

        if (root.needed_claim_to_build.worlds.contains(p.getWorld().getName())
                && b != null
                && root.needed_claim_to_build.allow_interact_blocks.stream().noneMatch(str -> str.equalsIgnoreCase(b.getType().name()))
                && root.needed_claim_to_build.allow_break_blocks.stream().noneMatch(str -> str.equalsIgnoreCase(b.getType().name()))
                && !b.getType().name().contains(root.region_settings.block_id.toUpperCase())
                && !b.getType().name().contains("SIGN")) {
            RedProtect.get().getLanguageManager().sendMessage(p, "need.claim.tobuild");
            return true;
        }
        return false;
    }

    public List<Location> getSigns(String rid) {
        List<Location> locs = new ArrayList<>();
        try {
            for (String s : signCfgs.getNode(rid).getList(of(String.class))) {
                String[] val = s.split(",");
                if (Bukkit.getServer().getWorld(val[0]) == null) {
                    continue;
                }
                locs.add(new Location(Bukkit.getServer().getWorld(val[0]), Double.parseDouble(val[1]), Double.parseDouble(val[2]), Double.parseDouble(val[3])));
            }
        } catch (ObjectMappingException e) {
            CoreUtil.printJarVersion();
            e.printStackTrace();
        }
        return locs;
    }

    public void putSign(String rid, Location loc) {
        try {
            List<String> lsigns = new ArrayList<>(signCfgs.getNode(rid).getList(of(String.class)));
            String locs = loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ();
            if (!lsigns.contains(locs)) {
                lsigns.add(locs);
                saveSigns(rid, lsigns);
            }
        } catch (ObjectMappingException e) {
            CoreUtil.printJarVersion();
            e.printStackTrace();
        }
    }

    public void removeSign(String rid, Location loc) {
        try {
            List<String> lsigns = new ArrayList<>(signCfgs.getNode(rid).getList(of(String.class)));
            String locs = loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ();
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
            signsLoader.save(signCfgs);
        } catch (IOException e) {
            RedProtect.get().logger.severe("Problems during save file:");
            CoreUtil.printJarVersion();
            e.printStackTrace();
        }
    }
}
   
