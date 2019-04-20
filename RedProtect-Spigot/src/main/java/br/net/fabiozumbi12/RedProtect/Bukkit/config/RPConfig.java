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

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPUtil;
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

    public  final List<String> AdminFlags = Arrays.asList(
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
            "dynmapHook",
            "particles",
            "deny-exit-items");
    private  CommentedConfig comConfig;
    private  CommentedConfig comGflags;
    private  YamlConfiguration signs;
    private  YamlConfiguration GuiItems;
    private  YamlConfiguration EconomyConfig;

    public RPConfig() {
        signs = new YamlConfiguration();
        GuiItems = new YamlConfiguration();
        EconomyConfig = new YamlConfiguration();

        File main = RedProtect.get().getDataFolder();
        File data = new File(main, "data");
        File gui = new File(main, "guiconfig.yml");
        File bvalues = new File(main, "economy.yml");
        File globalflags = new File(main, "globalflags.yml");
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
        String cfgHeader = ""
                + "# +--------------------------------------------------------------------+ #\n"
                + "# <               RedProtect World configuration File                  > #\n"
                + "# <--------------------------------------------------------------------> #\n"
                + "# <       This is the configuration file, feel free to edit it.        > #\n"
                + "# <        For more info about cmds and flags, check our Wiki:         > #\n"
                + "# <         https://github.com/FabioZumbi12/RedProtect/wiki            > #\n"
                + "# +--------------------------------------------------------------------+ #\n"
                + "#\n"
                + "# Notes:\n"
                + "# Lists are [object1, object2, ...]\n"
                + "# Strings containing the char & always need to be quoted\n";

        comConfig = new CommentedConfig(new File(main, "config.yml"), RedProtect.get().getConfig(), cfgHeader);

        comConfig.setDefault("config-version", 8.3D, "Dont touch <3");
        comConfig.setDefault("online-mode", Bukkit.getServer().getOnlineMode(), "This option will define if RedProtect will work with UUIDs or player names.\n" +
                "Use with caution because offline player has no uuids and maybe some offline player is using online nicknames.\n" +
                "Make a backup of your DATABASE before change this setting in a production server.");
        comConfig.setDefault("debug-messages", false, "Enable debug messages");
        comConfig.setDefault("log-actions", true, "Log all commands used by players");
        comConfig.setDefault("language", "EN-US", "Available: EN-US, PT-BR, ZH-CN, DE-DE, RU-RU, FR");
        comConfig.setDefault("file-type", "file", "Available: file and mysql");

        comConfig.setDefault("flat-file", null, "If file-type: yml, configuration:");
        comConfig.setDefault("flat-file.region-per-file", false, "Want to save the regions in your ow files?");
        comConfig.setDefault("flat-file.auto-save-interval-seconds", 3600, null);
        comConfig.setDefault("flat-file.backup", true, null);
        comConfig.setDefault("flat-file.backup-on-save", false, null);
        comConfig.setDefault("flat-file.max-backups", 10, null);

        comConfig.setDefault("mysql", null, "If file-type: mysql, configuration:");
        comConfig.setDefault("mysql.db-name", "redprotect", null);
        comConfig.setDefault("mysql.table-prefix", "rp_", null);
        comConfig.setDefault("mysql.user-name", "root", null);
        comConfig.setDefault("mysql.user-pass", "redprotect", null);
        comConfig.setDefault("mysql.host", "localhost", null);

        comConfig.setDefault("region-settings", null, "General settings about regions.");
        comConfig.setDefault("region-settings.claim-type", "BLOCK", "claim-type: Claim types allowed for normal players (without permission 'redprotect.admin.claim'). Options: BLOCK, WAND or BOTH.\n"
                + "-> If BLOCK, the players needs to surround your house with the block type in configuration, and place a sign under this fence with [rp] on first line.\n"
                + "-> If WAND, the players will need a wand (default glass_bottle), click on two point of your region, and then use /rp claim [name of region] to claim te region.\n"
                + "-> If BOTH, will allow both claim type protections.");
        comConfig.setDefault("region-settings.default-leader", "#server#", "The name of leader for regions created with /rp define or regions without leaders.");
        comConfig.setDefault("region-settings.world-colors", new ArrayList<String>(), "Colors of world to show on /rp info and /rp list.");
        comConfig.setDefault("region-settings.border.material", "GLOWSTONE", "Border block type when use /rp border.");
        comConfig.setDefault("region-settings.border.time-showing", 5, "Seconds before hide the border.");
        comConfig.setDefault("region-settings.region-list.regions-per-page", 50, "Max regions per page.");
        comConfig.setDefault("region-settings.region-list.hover-and-click-teleport", true, "If running server 1.8+ enable hover and teleport click on simple list.");
        comConfig.setDefault("region-settings.region-list.show-area", true, "Show region areas on list?");
        comConfig.setDefault("region-settings.autoexpandvert-ondefine", true, "Automatically set max y to world max size and min y to 0 (sky to bedrock) on define command.");
        comConfig.setDefault("region-settings.claim.miny", -1, "Set the minimum height to region on claim. Default is 0 if set to -1. (can be set lower numbers than -1)");
        comConfig.setDefault("region-settings.claim.maxy", -1, "Set the maximum height to region on claim. Default is world max size if set to -1.");
        comConfig.setDefault("region-settings.anti-hopper", true, "Deny break/place blocks under chests.");
        comConfig.setDefault("region-settings.claim-modes.mode", "keep", "Default modes for claim regions. Modes available: keep, drop, remove or give.\n"
                + "-> keep: Nothing happens\n"
                + "-> drop: Will drop all protection blocks\n"
                + "-> remove: Will remove all protection blocks\n"
                + "-> give: Give back the protection blocks to player, and drop(on player location) if players's inventory is full.");
        comConfig.setDefault("region-settings.claim-modes.allow-player-decide", false, "Allow players to decide what mode to use? If true, the player need to set the line 4 of the sign with [keep], [drop], [remove], [give] or a translation you is using on 'lang.ini'.");
        comConfig.setDefault("region-settings.claim-modes.use-perm", false, "If 'allow-player-decide' is true, player need to have the permission 'redprotect.use-claim-modes' to use modes on signs.");
        comConfig.setDefault("region-settings.limit-amount", 8000, "Limit of blocks until the player have other block permission.");
        comConfig.setDefault("region-settings.claim-amount", 20, "Limit of claims a player can have until have other permission for claims.");
        comConfig.setDefault("region-settings.block-id", "FENCE", "Block used to protect regions.");
        if (Material.getMaterial(RedProtect.get().getConfig().getString("region-settings.block-id")) == null) {
            RedProtect.get().getConfig().set("region-settings.block-id", "FENCE");
        }
        comConfig.setDefault("region-settings.max-scan", 600, "Ammount of blocks to scan on place sign to claim a region. Consider this the max area.");
        comConfig.setDefault("region-settings.define-max-distance", 1200, "When using the command define with wand tool, set the max distance allow to claim from point 1 to point 2 (in blocks)");
        comConfig.setDefault("region-settings.date-format", "dd/MM/yyyy", "Time format to use with data and time infos.");
        comConfig.setDefault("region-settings.record-player-visit-method", "ON-LOGIN", "Register player visits on... Available: ON-LOGIN, ON-REGION-ENTER.");
        comConfig.setDefault("region-settings.allow-sign-interact-tags", Arrays.asList("Admin Shop", "{membername}"), "Allow players without permissions to interact with signs starting with this tags.");
        comConfig.setDefault("region-settings.leadership-request-time", 20, "Time in seconds to wait player accept leadership request.");
        comConfig.setDefault("region-settings.enable-flag-sign", true, "This wiil allow players to create flag signs to change flag states using [flag] on first line and the flag name on second line.");
        comConfig.setDefault("region-settings.deny-build-near", 0, "Deny players to build near other regions. Distance in blocks. 0 to disable and > 0 to enable.");
        comConfig.setDefault("region-settings.first-home.can-delete-after-claims", 10, "Player can remove the protection of first home after this amount of claims. Use -1 to do not allow to delete.");
        comConfig.setDefault("region-settings.delay-after-kick-region", 60, "Delay before a kicked player can back to a region (in seconds).");
        comConfig.setDefault("region-settings.claimlimit-per-world", true, "Use claim limit per worlds?");
        comConfig.setDefault("region-settings.blocklimit-per-world", true, "Use block limit per worlds?");
        comConfig.setDefault("region-settings.teleport-time", 3, "Time to wait before teleport to region (in seconds).");

        comConfig.setDefault("allowed-claim-worlds", Collections.singletonList("example_world"), "World where players can claim regions.");

        comConfig.setDefault("needed-claim-to-build", null, "Worlds where players can't build without claim.");
        comConfig.setDefault("needed-claim-to-build.worlds", Collections.singletonList("example_world"), null);
        comConfig.setDefault("needed-claim-to-build.allow-break-blocks", Arrays.asList("GRASS", "TALL_GRASS"), "Allow more blocks to be break/placed by players.");
        comConfig.setDefault("needed-claim-to-build.allow-only-protections-blocks", true, "Allow player to place only protection blocks, like fences and sign.");

        comConfig.setDefault("wands", null, "Wands configurations");
        comConfig.setDefault("wands.adminWandID", "GLASS_BOTTLE", "Item used to define and redefine regions.");
        if (Material.getMaterial(RedProtect.get().getConfig().getString("wands.adminWandID")) == null) {
            RedProtect.get().getConfig().set("wands.adminWandID", "GLASS_BOTTLE");
        }
        comConfig.setDefault("wands.infoWandID", "PAPER", "Item used to check regions.");
        if (Material.getMaterial(RedProtect.get().getConfig().getString("wands.infoWandID")) == null) {
            RedProtect.get().getConfig().set("wands.infoWandID", "PAPER");
        }
        comConfig.setDefault("private", null, "Private options");
        comConfig.setDefault("private.use", true, "Enable private signs?");
        comConfig.setDefault("private.allow-outside", false, "Allow private signs outside regions");
        comConfig.setDefault("private.allowed-blocks-use-ids", false, "Use number IDs instead item names?");
        comConfig.setDefault("private.allowed-blocks",
                Arrays.asList("DISPENSER",
                        "NOTE_BLOCK",
                        "BED_BLOCK",
                        "CHEST",
                        "CRAFTING_TABLE",
                        "FURNACE",
                        "JUKEBOX",
                        "ENCHANTING_TABLE",
                        "BREWING_STAND",
                        "CAULDRON",
                        "ENDER_CHEST",
                        "BEACON",
                        "TRAPPED_CHEST",
                        "HOPPER",
                        "DROPPER",
                        "[A-Z_]+_SHULKER_BOX"), "Blocks allowed to be locked with private signs or to be used with the flag \"chest\".\n" +
                        "Block names may change between minecraft 1.12 and 1.13!!");

        comConfig.setDefault("notify", null, "Notifications configs.\nYour players can use this placeholders on Welcome Message: {r} and {p}");
        comConfig.setDefault("notify.region-exit", true, "Show region info(or wilderness message) when exit a region.");
        comConfig.setDefault("notify.welcome-region-name", false, "Show region name on Welcome Message?");
        comConfig.setDefault("notify.region-enter-mode", "BOSSBAR", "How to show the messages? Available: BOSSBAR, CHAT or OFF. If plugin BoobarApi not installed, will show on chat.");
        comConfig.setDefault("notify.welcome-mode", "BOSSBAR", "Where to show the welcome message (/rp wel <message>)? Available: BOSSBAR, CHAT or OFF.");

        comConfig.setDefault("netherProtection", null, "Deny players to go to nether roof.");
        comConfig.setDefault("netherProtection.maxYsize", 128, "Max size of your world nether.");
        comConfig.setDefault("netherProtection.execute-cmd", Collections.singletonList("spawn {player}"), "Execute this if player go up to maxYsize of nether.");

        comConfig.setDefault("server-protection", null, "General server protections options.");
        comConfig.setDefault("server-protection.deny-potions", Collections.singletonList("INVISIBILITY"), "List of potions the player cant use on server. Here the PotionTypes: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionType.html");
        comConfig.setDefault("server-protection.deny-playerdeath-by", Collections.singletonList("SUFFOCATION"), " List of causes the player cant die/take damage for. Here the list of DamageCauses: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/entity/EntityDamageEvent.DamageCause.html");
        comConfig.setDefault("server-protection.deny-commands-on-worlds.world", Collections.singletonList("command"), "Deny certain commands on specific worlds.");
        comConfig.setDefault("server-protection.nickname-cap-filter.enable", false, "Deny players with same nick but cap char diferences to join on server (most used on offline severs).");
        comConfig.setDefault("server-protection.sign-spy.enable", false, "Show all lines of a sign when player place signs in any world.");
        comConfig.setDefault("server-protection.sign-spy.only-console", true, "Show only on console or in-game too?");
        comConfig.setDefault("server-protection.teleport-player.on-join.enable", false, "Teleport player on join the server.");
        comConfig.setDefault("server-protection.teleport-player.on-join.need-world-to-teleport", "none", "The player need to be in this world to be teleported? Use 'none' for all worlds.");
        comConfig.setDefault("server-protection.teleport-player.on-join.location", "world, 0, 90, 0", "The location, using as world, x, y, z.");
        comConfig.setDefault("server-protection.teleport-player.on-leave.enable", false, "Teleport player on leave the server.");
        comConfig.setDefault("server-protection.teleport-player.on-leave.need-world-to-teleport", "none", "The player need to be in this world to be teleported? Use 'none' for all worlds.");
        comConfig.setDefault("server-protection.teleport-player.on-leave.location", "world, 0, 90, 0", "The location, using as world, x, y, z.");
        comConfig.setDefault("server-protection.deny-structure-bypass-regions", true, "Deny structures like trees to bypass region borders?");
        comConfig.setDefault("server-protection.check-killaura-freekill.enable", false, "Enable kill aura or freekill checker?");
        comConfig.setDefault("server-protection.check-killaura-freekill.check-rate", 30, "This will count every block the player wall without fail to aim on player.");
        comConfig.setDefault("server-protection.check-killaura-freekill.rate-multiples", 5, "What multiples of check-rate is considered kh or fk?");
        comConfig.setDefault("server-protection.check-killaura-freekill.time-between-trys", 3, "Time to reset checks between attacker hits.");
        comConfig.setDefault("server-protection.check-killaura-freekill.debug-trys", false, "Debug everu try? Used to see the try count on every block the player walk. Will be sequential if the player is using kill aura and will go to more than 60, 80 more than 100 if is free kill.");
        comConfig.setDefault("server-protection.check-player-client", false, "Test client hack (beta)");

        comConfig.setDefault("flags", null, "Default flag values for new regions.");
        comConfig.setDefault("flags.pvp", false, null);
        comConfig.setDefault("flags.chest", false, null);
        comConfig.setDefault("flags.lever", false, null);
        comConfig.setDefault("flags.button", false, null);
        comConfig.setDefault("flags.door", false, null);
        comConfig.setDefault("flags.smart-door", true, null);
        comConfig.setDefault("flags.spawn-monsters", true, null);
        comConfig.setDefault("flags.spawn-animals", true, null);
        comConfig.setDefault("flags.passives", false, null);
        comConfig.setDefault("flags.flow", true, null);
        comConfig.setDefault("flags.fire", true, null);
        comConfig.setDefault("flags.minecart", false, null);
        comConfig.setDefault("flags.allow-home", false, null);
        comConfig.setDefault("flags.allow-magiccarpet", true, null);
        comConfig.setDefault("flags.mob-loot", false, null);
        comConfig.setDefault("flags.flow-damage", false, null);
        comConfig.setDefault("flags.iceform-player", true, null);
        comConfig.setDefault("flags.iceform-world", true, null);
        comConfig.setDefault("flags.allow-fly", false, null);
        comConfig.setDefault("flags.teleport", false, null);
        comConfig.setDefault("flags.clan", "", null);
        comConfig.setDefault("flags.ender-chest", true, null);
        comConfig.setDefault("flags.can-grow", true, null);
        comConfig.setDefault("flags.use-potions", true, null);
        comConfig.setDefault("flags.allow-effects", true, null);
        comConfig.setDefault("flags.allow-spawner", false, null);
        comConfig.setDefault("flags.leaves-decay", false, null);
        comConfig.setDefault("flags.build", false, null);
        comConfig.setDefault("flags.press-plate", false, null);

        comConfig.setDefault("flags-configuration", null, ""
                + "effects-duration: Duration for timed flags like potions effects, jump, etc.\n"
                + "enabled-flags: Flags enabled to players use with commands and flag Gui.\n"
                + "pvparena-nopvp-kick-cmd: Command to use if players with pvp off enter in a region with 'pvparena' enabled.\n"
                + "change-flag-delay: Delay the player can change a flag after last change.\n"
                + "flags: List of flags the player will need to wait to change.");
        comConfig.setDefault("flags-configuration.effects-duration", 5, "Duration for timed flags like potions effects, jump, etc.");
        comConfig.setDefault("flags-configuration.enabled-flags", Arrays.asList(
                "pvp",
                "chest",
                "lever",
                "button",
                "door",
                "smart-door",
                "spawn-monsters",
                "spawn-animals",
                "passives",
                "flow",
                "fire",
                "minecart",
                "allow-potions",
                "allow-home",
                "allow-magiccarpet",
                "mob-loot",
                "flow-damage",
                "iceform-player",
                "iceform-world",
                "allow-fly",
                "teleport",
                "clan",
                "ender-chest",
                "leaves-decay",
                "build",
                "press-plate"), "Flags enabled to players use with commands and flag Gui.");
        comConfig.setDefault("flags-configuration.pvparena-nopvp-kick-cmd", "spawn {player}", "Command to use if players with pvp off enter in a region with 'pvparena' enabled.");
        comConfig.setDefault("flags-configuration.change-flag-delay.enable", true, "Enable delay to change flags.");
        comConfig.setDefault("flags-configuration.change-flag-delay.seconds", 10, "Delay the player can change a flag after last change.");
        comConfig.setDefault("flags-configuration.change-flag-delay.flags", Collections.singletonList("pvp"), "List of flags the player will need to wait to change.");

        comConfig.setDefault("purge", null, null);
        comConfig.setDefault("purge.enabled", false, null);
        comConfig.setDefault("purge.remove-oldest", 90, null);
        comConfig.setDefault("purge.regen.enable", false, "Hook with WorldEdit, will regen only the region areas to bedrock to sky. Theres no undo for this action!");
        comConfig.setDefault("purge.regen.whitelist-server-regen", true, "Enable whitelist when regenerating regions?");
        comConfig.setDefault("purge.regen.max-area-regen", 500, "Max area size to automatic regen the region.");
        comConfig.setDefault("purge.regen.awe-logs", false, "Show regen logs if using AsyncWorldEdit.");
        comConfig.setDefault("purge.regen.stop-server-every", -1, "Stop server on every x regions regenerated (if you is using a script to reboot your server)");
        comConfig.setDefault("purge.ignore-regions-from-players", new ArrayList<String>(), null);

        comConfig.setDefault("sell", null, null);
        comConfig.setDefault("sell.enabled", false, "Put regions to sell after x time the player dont came online.");
        comConfig.setDefault("sell.sell-oldest", 90, null);
        comConfig.setDefault("sell.ignore-regions-from-players", new ArrayList<String>(), null);

        comConfig.setDefault("performance", null, null);
        comConfig.setDefault("performance.disable-onPlayerMoveEvent-handler", false, "Disable player move event to improve performance? Note: Disabling this will make some flags do not work, like deny enter, execute commands and effects.");
        comConfig.setDefault("performance.piston.disable-PistonEvent-handler", false, "Disable piston listener? Disabling this will allow players to get blocks from protected regions to unprotected using pistons.");

        comConfig.setDefault("schematics", null, "This is the schematics configs for RedProtect.\n");
        comConfig.setDefault("schematics.first-house-file", "house1.schem", "Schematic file name to use with /rp start.");

        comConfig.setDefault("hooks", null, null);
        comConfig.setDefault("hooks.useWECUI", true, "Use worldeditCUI to visualize the region limits. (Need WorldEdit on server and WECUI on client)");
        comConfig.setDefault("hooks.dynmapHook.enabled", true, "Enable hook to show all regions on dynmapHook plugin?");
        comConfig.setDefault("hooks.dynmapHook.hide-by-default", true, "Hide the Redprotect tab group by default?");
        comConfig.setDefault("hooks.dynmapHook.marks-groupname", "RedProtect", "Group name to show on hide/show tab map.");
        comConfig.setDefault("hooks.dynmapHook.layer-priority", 10, "If you use another region mark plugin.");
        comConfig.setDefault("hooks.dynmapHook.show-label", true, "Show names under regions.");
        comConfig.setDefault("hooks.dynmapHook.show-icon", true, "Show icons under regions.");

        comConfig.setDefault("hooks.dynmapHook.player.marker-icon", "house", "Icon name to show under regions. All icons are available here: http://i.imgur.com/f61GPoE.png");
        comConfig.setDefault("hooks.dynmapHook.player.fill.opacity", 0.35, null);
        comConfig.setDefault("hooks.dynmapHook.player.fill.color", "#00ff00", "Pick a color: https://www.w3schools.com/colors/colors_picker.asp");
        comConfig.setDefault("hooks.dynmapHook.player.border.opacity", 0.8, null);
        comConfig.setDefault("hooks.dynmapHook.player.border.color", "#00ff00", "Pick a color: https://www.w3schools.com/colors/colors_picker.asp");
        comConfig.setDefault("hooks.dynmapHook.player.border.weight", 1, null);

        comConfig.setDefault("hooks.dynmapHook.server.marker-icon", "star", "Icon name to show under regions. All icons are available here: http://i.imgur.com/f61GPoE.png");
        comConfig.setDefault("hooks.dynmapHook.server.fill.opacity", 0.35, null);
        comConfig.setDefault("hooks.dynmapHook.server.fill.color", "#ff0000", "Pick a color: https://www.w3schools.com/colors/colors_picker.asp");
        comConfig.setDefault("hooks.dynmapHook.server.border.opacity", 0.8, null);
        comConfig.setDefault("hooks.dynmapHook.server.border.color", "#ff0000", "Pick a color: https://www.w3schools.com/colors/colors_picker.asp");
        comConfig.setDefault("hooks.dynmapHook.server.border.weight", 1, null);

        comConfig.setDefault("hooks.dynmapHook.show-leaders-admins", false, "Show leaders and admins on hover?");
        comConfig.setDefault("hooks.dynmapHook.cuboid-region.enable", true, "Cuboid region config.");
        comConfig.setDefault("hooks.dynmapHook.cuboid-region.if-disable-set-center", 60, null);
        comConfig.setDefault("hooks.dynmapHook.min-zoom", 0, null);
        comConfig.setDefault("hooks.magiccarpet.fix-piston-getblocks", true, "Fix pistons allow get mc blocks.");
        comConfig.setDefault("hooks.armor-stands.spawn-arms", true, null);
        comConfig.setDefault("hooks.mcmmo.fix-acrobatics-fireoff-leveling", true, "Fix players leveling with creeper explosions on flag fire disabled.");
        comConfig.setDefault("hooks.mcmmo.fix-berserk-invisibility", true, "Fix the ability berserk making players and mobs invisible around who activated the ability.");
        comConfig.setDefault("hooks.worldedit.use-for-schematics", true, "Use WorldEdit to paste newbie home-schematics (/rp start)? *RedProtect already can paste schematics without WorldEdit, but dont support NBT tags like chest contents and sign messages.");
        comConfig.setDefault("hooks.factions.claim-over-rps", false, "Allow players claim Factions chunks under RedProtect regions?");
        comConfig.setDefault("hooks.simpleclans.use-war", false, "Enable Clan Wars from SimleClans.");
        comConfig.setDefault("hooks.simpleclans.war-on-server-regions", false, "Allow war clans to pvp on #server# regions?");

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

        if (!schema.exists()) {
            new File(main, "schematics").mkdir();
            RPUtil.saveResource("/assets.redprotect/schematics/house1.schematic", null, schema);//save schematic file
            RedProtect.get().logger.info("Saved schematic file: house1.schematic");
        }

        RedProtect.get().logger.info("Server version: " + RedProtect.get().getServer().getBukkitVersion());

        // check if can enable json support
        if (getBool("region-settings.region-list.hover-and-click-teleport")) {
            try {
                Class.forName("com.google.gson.JsonParser");
                if (RedProtect.get().getServer().getBukkitVersion().contains("1.7")) {
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
            for (OfflinePlayer play : RedProtect.get().getServer().getOperators()) {
                ops.add(play.getName());
            }
            RedProtect.get().getConfig().set("purge.ignore-regions-from-players", ops);
        }

        //add op to ignore list fro sell
        if (RedProtect.get().getConfig().getStringList("sell.ignore-regions-from-players").size() <= 0) {
            List<String> ops = RedProtect.get().getConfig().getStringList("sell.ignore-regions-from-players");
            for (OfflinePlayer play : RedProtect.get().getServer().getOperators()) {
                ops.add(play.getName());
            }
            RedProtect.get().getConfig().set("sell.ignore-regions-from-players", ops);
        }

        //add allowed claim worlds to config
        if (RedProtect.get().getConfig().getStringList("allowed-claim-worlds").get(0).equals("example_world")) {
            List<String> worlds = new ArrayList<>();
            for (World w : RedProtect.get().getServer().getWorlds()) {
                worlds.add(w.getName());
                RedProtect.get().logger.warning("Added world to claim list " + w.getName());
            }
            worlds.remove("example_world");
            RedProtect.get().getConfig().set("allowed-claim-worlds", worlds);
        }

        //add worlds to color list
        for (World w : RedProtect.get().getServer().getWorlds()) {
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
        if (RedProtect.get().getConfig().getDouble("config-version") < 8D) {
            RedProtect.get().getConfig().set("config-version", 8D);

            RedProtect.get().getConfig().set("language", RedProtect.get().getConfig().getString("language").toUpperCase());
            configUp++;
        }
        if (RedProtect.get().getConfig().getDouble("config-version") < 8.1D) {
            RedProtect.get().getConfig().set("config-version", 8.1D);

            RedProtect.get().getConfig().set("wands.adminWandID", "GLASS_BOTTLE");
            RedProtect.get().getConfig().set("wands.infoWandID", "PAPER");
            configUp++;
        }
        if (RedProtect.get().getConfig().getDouble("config-version") < 8.2D) {
            RedProtect.get().getConfig().set("config-version", 8.2D);

            List<String> blocks = RedProtect.get().getConfig().getStringList("private.allowed-blocks");
            blocks.add("[A-Z_]+_SHULKER_BOX");
            RedProtect.get().getConfig().set("private.allowed-blocks", blocks);
            configUp++;
        }
        if (RedProtect.get().getConfig().getDouble("config-version") < 8.3D) {
            RedProtect.get().getConfig().set("config-version", 8.3D);

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

        //load and write globalflags to global file
        String gHeader = "# +--------------------------------------------------------------------+ #\n" +
                "# <          RedProtect Global Flags configuration File                > #\n" +
                "# <--------------------------------------------------------------------> #\n" +
                "# <         This is the global flags configuration file.               > #\n" +
                "# <                       Feel free to edit it.                        > #\n" +
                "# <         https://github.com/FabioZumbi12/RedProtect/wiki            > #\n" +
                "# +--------------------------------------------------------------------+ #\n" +
                "# \n" +
                "# Notes:\n" +
                "# Lists are [object1, object2, ...]\n" +
                "# Strings containing the char & always need to be quoted\n";
        comGflags = new CommentedConfig(new File(main, "globalflags.yml"), new YamlConfiguration(), gHeader);

        for (World w : RedProtect.get().getServer().getWorlds()) {
            comGflags.setDefault(w.getName(), null, "Configuration section for world " + w.getName() + "");

            comGflags.setDefault(w.getName() + ".build", true, "Players can build in this world?");
            comGflags.setDefault(w.getName() + ".liquid-flow", true, "Allow any type of liquids to flow?");

            comGflags.setDefault(w.getName() + ".allow-changes-of.water-flow", true, null);
            comGflags.setDefault(w.getName() + ".allow-changes-of.lava-flow", true, null);
            comGflags.setDefault(w.getName() + ".allow-changes-of.leaves-decay", true, null);
            comGflags.setDefault(w.getName() + ".allow-changes-of.flow-damage", true, null);

            comGflags.setDefault(w.getName() + ".if-build-false", null, "" +
                    "If build option is false, choose what blocks the player can place/break.\n" +
                    "Materials: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html");

            comGflags.setDefault(w.getName() + ".if-build-false.break-blocks.blacklist", new ArrayList<>(), "This blocks will not be allowed to be break, all others yes.");
            comGflags.setDefault(w.getName() + ".if-build-false.break-blocks.whitelist", new ArrayList<>(),
                    "Only this blocks will be allowed to break, all others will not.\n" +
                            "You can add this blocks to allow basic exploration (accept regex):\n" +
                            "\"[*]_PLANT\",\"GRASS_BLOCK\", \"TALL_GRASS\", \"POPPY\", \"DANDELION\"");

            comGflags.setDefault(w.getName() + ".if-build-false.place-blocks.blacklist", new ArrayList<>(), "This blocks will not be allowed to be place, all others yes.");
            comGflags.setDefault(w.getName() + ".if-build-false.place-blocks.whitelist", new ArrayList<>(), "Only this blocks will be allowed to place, all others will not.");

            comGflags.setDefault(w.getName() + ".pvp", true, null);

            comGflags.setDefault(w.getName() + ".iceform-by.player", false, null);
            comGflags.setDefault(w.getName() + ".iceform-by.world", true, null);

            comGflags.setDefault(w.getName() + ".interact", true, null);

            comGflags.setDefault(w.getName() + ".if-interact-false", null, "" +
                    "If interact option is false, choose what blocks or entity the player can interact.\n" +
                    "EntityTypes: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/EntityType.html");

            comGflags.setDefault(w.getName() + ".if-interact-false.interact-blocks.blacklist", new ArrayList<>(), "This items will not be allowed to interact, all other items will be.");
            comGflags.setDefault(w.getName() + ".if-interact-false.interact-blocks.whitelist", new ArrayList<>(),
                    "Only this items will allowed to interact, all other item will not be allowed.\n" +
                            "You can add this blocks to allow basic exploration (accept regex):\n" +
                            "\"[*]_PLANT\",\"GRASS_BLOCK\", \"TALL_GRASS\", \"POPPY\", \"DANDELION\"");

            comGflags.setDefault(w.getName() + ".if-interact-false.interact-entities.blacklist", new ArrayList<>(), "Only this entities will not be allowed to interact.");
            comGflags.setDefault(w.getName() + ".if-interact-false.interact-entities.whitelist", Collections.singletonList("VILLAGER"), "Only this entities will be allowed to interact, all others no.");

            comGflags.setDefault(w.getName() + ".use-minecart", true, "Allow to use minecarts and boats in this world?");
            comGflags.setDefault(w.getName() + ".entity-block-damage", false, "Like creeperds and Endermans.");
            comGflags.setDefault(w.getName() + ".explosion-entity-damage", true, "Explosive entities can explode blocks?");
            comGflags.setDefault(w.getName() + ".fire-block-damage", false, "Block will break on fire?");
            comGflags.setDefault(w.getName() + ".fire-spread", false, null);
            comGflags.setDefault(w.getName() + ".player-hurt-monsters", true, null);
            comGflags.setDefault(w.getName() + ".player-hurt-passives", true, null);

            comGflags.setDefault(w.getName() + ".spawn-allow-on-regions", false, "Allow entities to spawn only inside regions if blacklisted/whitelisted?");
            comGflags.setDefault(w.getName() + ".spawn-blacklist", new ArrayList<>(), "" +
                    "spawn-blacklist: This mobs will NOT spawn in this world!\n" +
                    "\n" +
                    "You can use MONSTERS or PASSIVES groups.\n" +
                    "Check the entity types here:\n" +
                    "https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/EntityType.html");

            comGflags.setDefault(w.getName() + ".spawn-whitelist", new ArrayList<>(), "" +
                    "spawn-whitelist: ONLY this mobs will spawn in this world!\n" +
                    "\n" +
                    "You can use MONSTERS or PASSIVES groups.\n" +
                    "Check the entity types here:\n" +
                    "https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/EntityType.html");

            comGflags.setDefault(w.getName() + ".elytra.allow", true, null);
            comGflags.setDefault(w.getName() + ".elytra.boost", 0.5D, null);

            comGflags.setDefault(w.getName() + ".deny-item-usage", null, "Control what items the player can use.");
            comGflags.setDefault(w.getName() + ".deny-item-usage.allow-on-claimed-rps", true, null);
            comGflags.setDefault(w.getName() + ".deny-item-usage.allow-on-wilderness", false, null);
            comGflags.setDefault(w.getName() + ".deny-item-usage.items", new ArrayList<>(), null);

            comGflags.setDefault(w.getName() + ".player-velocity.walk-speed", -1, null);
            comGflags.setDefault(w.getName() + ".player-velocity.fly-speed", -1, null);

            comGflags.setDefault(w.getName() + ".on-enter-cmds", new ArrayList<>(), "" +
                    "Execute this command on enter in this world.\n" +
                    "You can use this placeholders: {world-from}, {world-to} and {player}");

            comGflags.setDefault(w.getName() + ".on-exit-cmds", new ArrayList<>(), "" +
                    "Execute this command on exit this world.\n" +
                    "You can use this placeholders: {world-from}, {world-to} and {player}");

            comGflags.setDefault(w.getName() + ".invincible", false, null);
            comGflags.setDefault(w.getName() + ".player-candrop", true, null);
            comGflags.setDefault(w.getName() + ".player-canpickup", true, null);
            comGflags.setDefault(w.getName() + ".rain.trys-before-rain", 3, null);
            comGflags.setDefault(w.getName() + ".rain.duration", 60, null);
            comGflags.setDefault(w.getName() + ".allow-crops-trample", true, null);

            comGflags.setDefault(w.getName() + ".command-ranges", null, "Execute commands in certain coordinate ranges.");
            if (!comGflags.configurations.contains(w.getName() + ".command-ranges")) {
                comGflags.setDefault(w.getName() + ".command-ranges.home.min-range", 0, null);
                comGflags.setDefault(w.getName() + ".command-ranges.home.max-range", w.getMaxHeight(), null);
                comGflags.setDefault(w.getName() + ".command-ranges.home.message", "&cYou cant use /home when mining or in caves!", null);
            }

            //remove old configs
            comGflags.configurations.set(w.getName() + ".if-interact-false.allow-blocks", null);
            comGflags.configurations.set(w.getName() + ".if-interact-false.allow-entities", null);
        }

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

        String v = RedProtect.get().getServer().getBukkitVersion();
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

    public  String getWorldClaimType(String w) {
        return RedProtect.get().getConfig().getString("region-settings.claim-type.worlds." + w);
    }

    public  boolean hasGlobalKey(String path) {
        return comGflags.configurations.contains(path);
    }

    public  String getGlobalFlagString(String string) {
        return comGflags.configurations.getString(string);
    }

    public  double getGlobalFlagDouble(String key) {
        return comGflags.configurations.getDouble(key);
    }

    public  float getGlobalFlagFloat(String key) {
        return Float.valueOf(comGflags.configurations.getString(key));
    }

    public  int getGlobalFlagInt(String key) {
        return comGflags.configurations.getInt(key);
    }

    public  Boolean getGlobalFlagBool(String key) {
        return comGflags.configurations.getBoolean(key);
    }

    public  List<String> getGlobalFlagList(String key) {
        return comGflags.configurations.getStringList(key);
    }

    public  ItemStack getGuiItemStack(String key) {
        RedProtect.get().logger.debug("Gui Material to get: " + key);
        RedProtect.get().logger.debug("Result: " + GuiItems.getString("gui-flags." + key + ".material"));
        String item = GuiItems.getString("gui-flags." + key + ".material", "WHITE_STAINED_GLASS_PANE");
        return new ItemStack(Material.getMaterial(item));
    }

    public  String getGuiFlagString(String flag, String option) {
        if (GuiItems.getString("gui-flags." + flag + "." + option) == null) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', GuiItems.getString("gui-flags." + flag + "." + option));
    }

    public  String getGuiString(String string) {
        return ChatColor.translateAlternateColorCodes('&', GuiItems.getString("gui-strings." + string));
    }

    public  int getGuiSlot(String flag) {
        return GuiItems.getInt("gui-flags." + flag + ".slot");
    }

    public  void setGuiSlot(/*String mat, */String flag, int slot) {
        GuiItems.set("gui-flags." + flag + ".slot", slot);
    }

    public  ItemStack getGuiSeparator() {
        ItemStack separator = new ItemStack(Material.getMaterial(GuiItems.getString("gui-separator.material", "WHITE_STAINED_GLASS_PANE")), 1, (short) GuiItems.getInt("gui-separator.data"));
        ItemMeta meta = separator.getItemMeta();
        meta.setDisplayName(getGuiString("separator"));
        meta.setLore(Arrays.asList("", getGuiString("separator")));
        separator.setItemMeta(meta);
        return separator;
    }

    public  int getGuiMaxSlot() {
        SortedSet<Integer> slots = new TreeSet<>(new ArrayList<>());
        for (String key : GuiItems.getKeys(true)) {
            if (key.contains(".slot")) {
                slots.add(GuiItems.getInt(key));
            }
        }
        return Collections.max(slots);
    }

    public  Boolean getBool(String key) {
        return RedProtect.get().getConfig().getBoolean(key, false);
    }

    public  void setConfig(String key, Object value) {
        RedProtect.get().getConfig().set(key, value);
    }

    public  HashMap<String, Object> getDefFlagsValues() {
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

    public  boolean isFlagEnabled(String flag) {
        return RedProtect.get().getConfig().getStringList("flags-configuration.enabled-flags").contains(flag) || AdminFlags.contains(flag);
    }

    public  SortedSet<String> getDefFlags() {
        return new TreeSet<>(getDefFlagsValues().keySet());
    }

    public  String getString(String key, String def) {
        return RedProtect.get().getConfig().getString(key, def);
    }

    public  String getString(String key) {
        return RedProtect.get().getConfig().getString(key, "");
    }

    public  Double getDouble(String key) {
        return RedProtect.get().getConfig().getDouble(key);
    }

    public  Integer getInt(String key) {
        return RedProtect.get().getConfig().getInt(key);
    }

    public  List<String> getStringList(String key) {
        return RedProtect.get().getConfig().getStringList(key);
    }

    public  Material getMaterial(String key) {
        return Material.getMaterial(RedProtect.get().getConfig().getString(key));
    }

    public  void save() {
        File main = RedProtect.get().getDataFolder();
        File gui = new File(main, "guiconfig.yml");
        File bvalues = new File(main, "economy.yml");
        File signsf = new File(main, "signs.yml");
        try {
            GuiItems.save(gui);
            EconomyConfig.save(bvalues);
            signs.save(signsf);
            comGflags.saveConfig();
            comConfig.saveConfig();
        } catch (IOException e) {
            RedProtect.get().logger.severe("Problems during save file:");
            e.printStackTrace();
        }
    }

    public  void saveGui() {
        File guiconfig = new File(RedProtect.get().getDataFolder(), "guiconfig.yml");
        try {
            GuiItems.save(guiconfig);
        } catch (IOException e) {
            RedProtect.get().logger.severe("Problems during save gui file:");
            e.printStackTrace();
        }
    }

    private  YamlConfiguration inputLoader(InputStream inp) {
        YamlConfiguration file = new YamlConfiguration();
        try {
            file.load(new InputStreamReader(inp, StandardCharsets.UTF_8));
            inp.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    public  boolean isAllowedWorld(Player p) {
        return getStringList("allowed-claim-worlds").contains(p.getWorld().getName()) || p.hasPermission("redprotect.bypass.world");
    }

    public  boolean needClaimToBuild(Player p, Block b) {
        boolean bool = RedProtect.get().getConfig().getStringList("needed-claim-to-build.worlds").contains(p.getWorld().getName());
        if (bool) {
            if (b != null && getBool("needed-claim-to-build.allow-only-protections-blocks") &&
                    (getWorldClaimType(p.getWorld().getName()).equalsIgnoreCase("BLOCK") ||
                            getWorldClaimType(p.getWorld().getName()).equalsIgnoreCase("BOTH"))) {
                boolean blocks = b.getType().name().contains(getString("region-settings.block-id")) ||
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

    public  boolean addFlag(String flag, boolean defaultValue, boolean isAdmin) {
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

    public  int getBlockCost(String itemName) {
        return EconomyConfig.getInt("items.values." + itemName);
    }

    public  int getEnchantCost(String enchantment) {
        return EconomyConfig.getInt("enchantments.values." + enchantment);
    }

    public  String getEcoString(String key) {
        return EconomyConfig.getString(key);
    }

    public  Integer getEcoInt(String key) {
        return EconomyConfig.getInt(key);
    }

    public  boolean getEcoBool(String key) {
        return EconomyConfig.getBoolean(key);
    }

    public  List<Location> getSigns(String rid) {
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

    public  void putSign(String rid, Location loc) {
        List<String> lsigns = signs.getStringList(rid);
        String locs = loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ();
        if (!lsigns.contains(locs)) {
            lsigns.add(locs);
            saveSigns(rid, lsigns);
        }
    }

    public  void removeSign(String rid, Location loc) {
        List<String> lsigns = signs.getStringList(rid);
        String locs = loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ();
        if (lsigns.contains(locs)) {
            lsigns.remove(locs);
            saveSigns(rid, lsigns);
        }
    }

    private  void saveSigns(String rid, List<String> locs) {
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
   