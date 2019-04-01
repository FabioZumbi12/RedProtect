/*
 *
 * Copyright (c) 2019 - @FabioZumbi12
 * Last Modified: 24/03/19 13:05
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
 *
 */

package br.net.fabiozumbi12.RedProtect.Sponge.config.Category;

import br.net.fabiozumbi12.RedProtect.Sponge.LogLevel;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.*;

@ConfigSerializable
public class MainCategory {

    @Setting(value = "config-version", comment = "Dont touch <3")
    public double config_version = 7.6;
    @Setting(value = "allowed-claim-worlds", comment = "WorldProperties where players will be allowed to claim regions.")
    public List<String> allowed_claim_worlds = new ArrayList<>();
    @Setting(value = "file-type", comment = "File type to save regions. Values: \"file\" or \"mysql\"")
    public String file_type = "file";
    @Setting(value = "debug-messages")
    public Map<String, Boolean> debug_messages = createMapDebug();
    @Setting(comment = "Default flag values for new regions.\nThis will not change the values for already created regions.")
    public Map<String, Boolean> flags = createMapFlags();
    @Setting(value = "flags-configuration")
    public flagsConfig flags_configuration = new flagsConfig();
    @Setting(value = "flat-file", comment = "Options for flatfile database.")
    public flatFile flat_file = new flatFile();
    @Setting(comment = "Available: EN-US, PT-BR, ZH-CN, DE-DE, RU-RU, FR")
    public String language = "EN-US";
    @Setting(value = "log-actions", comment = "Log all redprotect commands?")
    public boolean log_actions = true;
    @Setting(comment = "Mysql options")
    public mysqlOptions mysql = new mysqlOptions();
    @Setting(value = "needed-claim-to-build", comment = "Adding world names to the list will automatically enable this on that world.")
    public needClaim needed_claim_to_build = new needClaim();
    @Setting(value = "nether-protection")
    public netherProtection nether_protection = new netherProtection();
    @Setting(comment = "Show messages on enter, exit and welcome messages.")
    public notifyCat notify = new notifyCat();
    @Setting
    public performanceCat performance = new performanceCat();
    @Setting(value = "permissions-limits", comment = "Declare here the permissions you want to use for your players and groups.")
    public permLimits permissions_limits = new permLimits();
    @Setting(value = "private", comment = "Private containers using signs.")
    public privateCat private_cat = new privateCat();
    @Setting
    public purgeCat purge = new purgeCat();
    @Setting(value = "region-settings", comment = "general regions settings.")
    public regionSettings region_settings = new regionSettings();
    @Setting
    public sellCat sell = new sellCat();
    @Setting(value = "server-protection")
    public serverProtection server_protection = new serverProtection();
    @Setting
    public wandsCat wands = new wandsCat();
    @Setting
    public hooksCat hooks = new hooksCat();

    @Setting
    public schematicsCat schematics = new schematicsCat();

    @ConfigSerializable
    public class schematicsCat{
        @Setting(value = "first-house-file", comment = "Schematic file name to use with /rp start.")
        public String first_house_file = "house1.schem";
    }

    public MainCategory() {
    }

    private Map<String, Boolean> createMapDebug() {
        Map<String, Boolean> myMap = new HashMap<>();
        for (LogLevel level : LogLevel.values()) {
            myMap.put(level.name().toLowerCase(), false);
        }
        return myMap;
    }

    private Map<String, Boolean> createMapFlags() {
        Map<String, Boolean> myMap = new HashMap<>();
        myMap.put("allow-effects", true);
        myMap.put("allow-fly", true);
        myMap.put("allow-home", false);
        myMap.put("allow-potions", true);
        myMap.put("allow-spawner", false);
        myMap.put("build", false);
        myMap.put("button", false);
        myMap.put("can-grow", true);
        myMap.put("chest", false);
        myMap.put("door", false);
        myMap.put("fire", false);
        myMap.put("flow", true);
        myMap.put("flow-damage", false);
        myMap.put("leaves-decay", false);
        myMap.put("lever", false);
        myMap.put("minecart", false);
        myMap.put("mob-loot", false);
        myMap.put("passives", false);
        myMap.put("press-plate", false);
        myMap.put("pvp", false);
        myMap.put("smart-door", true);
        myMap.put("spawn-animals", true);
        myMap.put("spawn-monsters", true);
        myMap.put("teleport", false);
        myMap.put("use-potions", false);
        return myMap;
    }

    @ConfigSerializable
    public static class flagsConfig {
        @Setting(value = "change-flag-delay", comment = "Delay to change the same flag again, if listed.")
        public flagsDelay change_flag_delay = new flagsDelay();
        @Setting(value = "effects-duration", comment = "Delay for effects flags.")
        public int effects_duration = 5;
        @Setting(value = "enabled-flags", comment = "This flags will be available to player who have the flags permissions, \nto change the flag state via command on flag gui.")
        public List<String> enabled_flags = Arrays.asList(
                "pvp",
                "chest",
                "lever",
                "button",
                "door",
                "smart-door",
                "spawn-monsters",
                "spawn-animals",
                "passives",
                "press-plate",
                "flow",
                "fire",
                "minecart",
                "allow-potions",
                "allow-home",
                "mob-loot",
                "flow-damage",
                "allow-fly",
                "allow-effects",
                "use-potions",
                "teleport",
                "can-grow",
                "allow-spawner",
                "leaves-decay",
                "ender-chest",
                "build");

        @ConfigSerializable
        public static class flagsDelay {
            @Setting
            public boolean enable = true;
            @Setting
            public List<String> flags = Collections.singletonList("pvp");
            @Setting
            public int seconds = 10;
        }
    }

    @ConfigSerializable
    public static class flatFile {
        @Setting(value = "auto-save-interval-seconds")
        public int auto_save_interval_seconds = 3600;
        @Setting
        public boolean backup = true;
        @Setting(value = "backup-on-save")
        public boolean backup_on_save = false;
        @Setting(value = "max-backups")
        public int max_backups = 10;
        @Setting(value = "region-per-file", comment = "Save every region in your own file? More safer way to save regions, \nbut will create multiple files (where is not a problem).")
        public boolean region_per_file = false;
    }

    @ConfigSerializable
    public static class mysqlOptions {
        @Setting(value = "db-name")
        public String db_name = "redprotect";
        @Setting
        public String host = "localhost";
        @Setting(value = "region-cache-minutes")
        public int region_cache_minutes = 2;
        @Setting(value = "table-prefix")
        public String table_prefix = "rp_";
        @Setting(value = "user-name")
        public String user_name = "root";
        @Setting(value = "user-pass")
        public String user_pass = "redprotect";
    }

    @ConfigSerializable
    public static class needClaim {
        @Setting(value = "allow-break-blocks", comment = "Allow break only this blocks on worlds where this is enabled.")
        public List<String> allow_break_blocks = Arrays.asList("minecraft:grass", "minecraft:tall_grass");
        @Setting(value = "allow-only-protections-blocks", comment = "Allow place only protection blocks like the sign and the \"block-id\".")
        public boolean allow_only_protections_blocks = true;
        @Setting(comment = "Add your worlds here to allow players to place blocks only in your own claims.")
        public List<String> worlds = Collections.singletonList("example_world");
    }

    @ConfigSerializable
    public static class netherProtection {
        @Setting(value = "execute-cmd", comment = "If the player go to your world nether roof, this commands be fired.")
        public List<String> execute_cmd = Collections.singletonList("spawn other {player}");
        @Setting(comment = "The size of your netehr world(s).")
        public int maxYsize = 128;
    }

    @ConfigSerializable
    public static class notifyCat {
        @Setting(value = "region-enter-mode", comment = "Available: \"BOSSBAR\", \"OFF\" or \"CHAT\"")
        public String region_enter_mode = "CHAT";
        @Setting(value = "region-exit", comment = "Notify on exit region? (in most cases will be wilderness)")
        public boolean region_exit = true;
        @Setting(value = "welcome-mode", comment = "Available: \"BOSSBAR\", \"OFF\" or \"CHAT\"")
        public String welcome_mode = "CHAT";
    }

    @ConfigSerializable
    public static class performanceCat {
        @Setting(value = "disable-PistonEvent-handler")
        public boolean disable_PistonEvent_handler = false;
        @Setting(value = "disable-onPlayerMoveEvent-handler")
        public boolean disable_onPlayerMoveEvent_handler = false;
    }

    @ConfigSerializable
    public static class permLimits {
        @Setting
        public List<String> blocks = Collections.singletonList("redprotect.limit.blocks.8000");
        @Setting
        public List<String> claims = Collections.singletonList("redprotect.limit.claim.20");
    }

    @ConfigSerializable
    public static class privateCat {
        @Setting(value = "allow-outside", comment = "Allow player to create private container outside regions? (on wilderness)")
        public boolean allow_outside = true;
        @Setting(value = "allowed-blocks", comment = "Blocks allowed to be locked with private signs.\n" +
                "Accept mod blocks, eg.: Pixelmon Healers or PCs.\n" +
                "Accept regex to match a group of blocks, like shulker boxes.")
        public List<String> allowed_blocks = Arrays.asList(
                "minecraft:dispenser",
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
                "minecraft:[a-z_]+_shulker_box");
        @Setting
        public boolean use = true;
    }

    @ConfigSerializable
    public static class purgeCat {
        @Setting
        public boolean enabled = false;
        @Setting(value = "ignore-regions-from-players", comment = "Names or UUIDs if in online mode, to bypass purge and regen." +
                "\n*That default uuid its a random one and you can remove*")
        public List<String> ignore_regions_from_players = Collections.singletonList(UUID.randomUUID().toString());

        @Setting
        public regenCat regen = new regenCat();
        @Setting(value = "remove-oldest", comment = "Remove regions where the leader not logged in for more than x days?")
        public int remove_oldest = 90;

        @ConfigSerializable
        public static class regenCat {
            @Setting(comment = "If worldedit is installed, regen the region instead remove?")
            public boolean enable = false;
            @Setting(value = "max-area-regen", comment = "Regions with an area greater than this will be ignored.")
            public int max_area_regen = 500;
            @Setting(value = "stop-server-every", comment = "Stop server on every x regions regenerated (if you is using a script to reboot your server)")
            public int stop_server_every = -1;
            @Setting(value = "whitelist-server-regen", comment = "Enable whitelist when regenerating regions?")
            public boolean enable_whitelist_regen = true;
        }
    }

    @ConfigSerializable
    public static class regionSettings {
        @Setting(value = "allow-sign-interact-tags", comment = "Allow non meber of regions to interact with signs with this headers. (line 1 of the sign)")
        public List<String> allow_sign_interact_tags = Arrays.asList(
                "[Buy]",
                "[Sell]",
                "[Trade]",
                "Shop Header",
                "{membername}");
        @Setting(value = "anti-hopper", comment = "Deny player to put hoppers or minecraft hoppers under chest locked with private signs?")
        public boolean anti_hopper = true;
        @Setting(value = "autoexpandvert-ondefine", comment = "Auto expand the vertical region size on claim or on define a region? If false, the region will be flat.")
        public boolean autoexpandvert_ondefine = true;
        @Setting(value = "block-id", comment = "The block id to use for claim regions (not wand). If \"fence\", will work for all fence types.")
        public String block_id = "fence";
        @Setting(value = "blocklimit-per-world", comment = "Split the block limits per world? This is not amount of claims and yes for blocks!")
        public boolean blocklimit_per_world = true;

        @Setting
        public borderCat border = new borderCat();
        @Setting
        public claimCat claim = new claimCat();
        @Setting(value = "enable-flag-sign", comment = "Allow players to create signs to change your regions flags states?")
        public boolean enable_flag_sign = true;
        @Setting(value = "date-format", comment = "Time format to use with data and time infos.")
        public String date_format = "dd/MM/yyyy";
        @Setting(value = "default-leader", comment = "The leader for regions created using the define command.\nNormally used for server regions like spawn.")
        public String default_leader = "#server#";
        @Setting(value = "wand-max-distance", comment = "Max distance an area can have on try to claim a region or redefine using wand tool.")
        public int wand_max_distance = 1200;
        @Setting(value = "delay-after-kick-region", comment = "Time the player can back to region from where was kicked.")
        public int delay_after_kick_region = 60;
        @Setting(value = "deny-build-near", comment = "Deny player to build/break blocks near x block of a region.")
        public int deny_build_near = 2;
        @Setting(value = "can-delete-first-home-after-claim", comment = "The player can delete the first home after claim this amount of regions.")
        public int can_delete_first_home_after_claims = 10;
        @Setting(value = "leadership-request-time", comment = "Time in seconds the request to be a leader of a region will expire.")
        public int leadership_request_time = 20;
        @Setting(value = "limit-amount", comment = "The default total of blocks a player can claim. \n" +
                "The permission \"redprotect.limit.blocks.<amount>\" overrides this setting.")
        public int limit_amount = 8000;
        @Setting(value = "max-scan", comment = "If using blocks for claim, this is the max blocks the plugin will scan before claim a region.")
        public int max_scan = 600;
        @Setting(value = "record-player-visit-method", comment = "Method to record the player visit on regions. Available: \"ON-LOGIN\" or \"ON-REGION-ENTER\"")
        public String record_player_visit_method = "ON-LOGIN";
        @Setting(value = "teleport-time", comment = "Delay to teleport command.")
        public int teleport_time = 3;
        @Setting(value = "world-colors", comment = "Sets the world colors for list command.")
        public Map<String, String> world_colors = new HashMap<>();
        @Setting(value = "regions-per-page")
        public int region_per_page = 50;

        @ConfigSerializable
        public static class borderCat {
            @Setting
            public String material = "minecraft:glowstone";
            @Setting(value = "time-showing")
            public int time_showing = 5;
        }

        @ConfigSerializable
        public static class claimCat {
            @Setting(comment = "Default vertical max size of a region on claim/define. Set -1 to set to world size.")
            public int maxy = -1;
            @Setting(comment = "Default vertical min size of a region on claim/define. Set -1 to set to world size.")
            public int miny = -1;
            @Setting(value = "amount-per-player", comment = "Default claim amount per player if no claim permissions set. The permission \"redprotect.limit.claim.<limit>\" overrides this setting.")
            public int amount_per_player = 20;

            @Setting
            public modesCat modes = new modesCat();
            @Setting(value = "world-types", comment = "Claim types allowed for normal players. Options: BLOCK, WAND or BOTH.\n"
                    + "-> If BLOCK, the players needs to surround your house with the block type in configuration,\nand place a sign under this fence with [rp] on first line.\n"
                    + "-> If WAND, the players will need a wand (default glass_bottle), click on two point of your region,\nand then use /rp claim [name of region] to claim te region.\n"
                    + "-> If BOTH, will allow both claim type protections.")
            public Map<String, String> world_types = new HashMap<>();
            @Setting(value = "claimlimit-per-world", comment = "Split the claim limits per world.")
            public boolean claimlimit_per_world = true;

            @ConfigSerializable
            public static class modesCat {
                @Setting(value = "allow-player-decide", comment = "Allow players to decide what mode to use? \n" +
                        "If true, the player need to set the line 4 of the sign with \n" +
                        "[keep], [drop], [remove], [give] or a translation from \"lang*.properties\".")
                public boolean allow_player_decide = false;
                @Setting(comment = "Default modes for claim regions. Modes available: keep, drop, remove or give.\n"
                        + "-> keep or none: Nothing happens\n"
                        + "-> drop: Will drop all protection blocks\n"
                        + "-> remove: Will remove all protection blocks\n"
                        + "-> give: Give back the protection blocks to player, and drop(on player location) if players's inventory is full.")
                public String mode = "none";
                @Setting(value = "use-perm", comment = "If \"allow-player-decide\" is true, player need to have the permission \"redprotect.use-claim-modes\" to use modes on signs.")
                public boolean use_perm = false;
            }
        }
    }

    @ConfigSerializable
    public static class sellCat {
        @Setting
        public boolean enabled = false;
        @Setting(value = "ignore-regions-from-players", comment = "Names or UUIDs if in online mode, to bypass purge and regen." +
                "\n*That default uuid its a random one and you can remove*")
        public List<String> ignore_regions_from_players = Collections.singletonList(UUID.randomUUID().toString());
        @Setting(value = "sell-oldest", comment = "Put to sell regions not visited by their leader after x days.")
        public int sell_oldest = 90;
    }

    @ConfigSerializable
    public static class serverProtection {

        @Setting(value = "deny-command-on-worlds", comment = "Deny a command in specific world.")
        public Map<String, List<String>> deny_commands_on_worlds = createMapCmdWorld();
        @Setting(value = "deny-playerdeath-by", comment = "Deny player death or get damage by this types of damage. \n" +
                "List of types: https://goo.gl/9EyhSd")
        public List<String> deny_playerdeath_by = Collections.singletonList("SUFFOCATE");
        @Setting(value = "deny-potions", comment = "Deny this types of potions to be used on server. \n" +
                "List of types: https://goo.gl/qKufWT")
        public List<String> deny_potions = Collections.singletonList("INVISIBILITY");
        @Setting(value = "sign-spy", comment = "Show every placed sign for who have the permission \"redprotect.signspy\" and for console.")
        public signSpy sign_spy = new signSpy();

        private Map<String, List<String>> createMapCmdWorld() {
            Map<String, List<String>> myMap = new HashMap<>();
            myMap.put("world", Collections.singletonList("command"));
            return myMap;
        }

        @ConfigSerializable
        public static class signSpy {
            @Setting
            public boolean enabled = true;
            @Setting(value = "only-console")
            public boolean only_console = true;
        }
    }

    @ConfigSerializable
    public static class wandsCat {
        @Setting(comment = "Its used to define regions and for players to claim regions.")
        public String adminWandID = "minecraft:glass_bottle";
        @Setting(comment = "Checks the clicked block with this on hand to see if theres a region on that block.")
        public String infoWandID = "minecraft:paper";
    }

    @ConfigSerializable
    public static class hooksCat {
        @Setting(value = "check-uuid-names-onstart", comment = "This will try to convert your player UUIDs to Player Names" +
                "\nif you recently changed your server from Online mode to Offline, and from the other way too.")
        public boolean check_uuid_names_onstart = false;

        @Setting(comment = "Use worldeditCUI to visualize the region limits. (Need WorldEdit on server and WECUI on client)")
        public boolean useWECUI = true;

        @Setting
        public dynmapCat dynmap = new dynmapCat();

        @ConfigSerializable
        public static class dynmapCat {
            @Setting(comment = "Enable hook to show all regions on dynmap plugin?")
            public boolean enable = true;
            @Setting(value = "hide-by-default", comment = "Hide the Redprotect tab group by default?")
            public boolean hide_by_default = true;
            @Setting(value = "marks-groupname", comment = "Group name to show on hide/show tab map.")
            public String marks_groupname = "RedProtect";
            @Setting(value = "layer-priority", comment = "If you use another region mark plugin.")
            public int layer_priority = 10;
            @Setting(value = "show-label", comment = "Show names under regions.")
            public boolean show_label = true;
            @Setting(value = "show-icon", comment = "Show icons under regions.")
            public boolean show_icon = true;

            @Setting
            public Map<String, iconCat> marker = createMap();

            private Map<String, iconCat> createMap() {
                Map<String, iconCat> myMap = new HashMap<>();
                myMap.put("player", new iconCat("house", 0.35, "#00ff00", 0.8, "#00ff00", 1));
                myMap.put("server", new iconCat("star", 0.35, "#ff0000", 0.8, "#ff0000", 1));
                return myMap;
            }

            @ConfigSerializable
            public static class iconCat {
                public iconCat(){}

                public iconCat(String marker, double fill_op, String fill_col, double bord_op, String bord_col, int bord_weight){
                    marker_icon = marker;
                    fill_color = fill_col;
                    fill_opacity = fill_op;
                    border_opacity = bord_op;
                    border_color = bord_col;
                    border_weight = bord_weight;
                }

                @Setting(value = "marker-icon", comment = "Icon name to show under regions. All icons are available here: http://i.imgur.com/f61GPoE.png")
                public String marker_icon;
                @Setting(value = "fill-opacity")
                public double fill_opacity;
                @Setting(value = "fill-color", comment = "Pick a color: https://www.w3schools.com/colors/colors_picker.asp")
                public String fill_color;

                @Setting(value = "border-opacity")
                public double border_opacity;
                @Setting(value = "border-color", comment = "Pick a color: https://www.w3schools.com/colors/colors_picker.asp")
                public String border_color;
                @Setting(value = "border-weight")
                public int border_weight;
            }

            @Setting(value = "show-leaders-admins", comment = "Show leaders and admins on hover?")
            public boolean show_leaders_admins = true;
            @Setting(value = "cuboid-region")
            public cuboidCat cuboid_region = new cuboidCat();
            @Setting(value = "min-zoom")
            public int min_zoom = 0;

            @ConfigSerializable
            public static class cuboidCat {
                @Setting(comment = "Cuboid region config.")
                public boolean enabled = true;
                @Setting(value = "if-disable-set-center")
                public int if_disable_set_center = 60;
            }
        }
    }
}
