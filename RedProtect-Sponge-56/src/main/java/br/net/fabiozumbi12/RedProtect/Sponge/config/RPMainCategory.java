package br.net.fabiozumbi12.RedProtect.Sponge.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.api.world.World;

import java.util.*;

@ConfigSerializable
public class RPMainCategory {

    public RPMainCategory(){}

    @Setting(value="config-version", comment = "Dont touch <3")
    public double config_version = 7.4;

    @Setting(value = "allowed-claim-worlds", comment = "Worlds where players will be allowed to claim regions.")
    public List<String> allowed_claim_worlds = popClaimWorlds();
    private List<String> popClaimWorlds(){
        List<String> list = new ArrayList<>();
        for (World w:Sponge.getServer().getWorlds()){
            list.add(w.getName());
        }
        return list;
    }

    @Setting(value = "file-type", comment = "File type to save regions. Values: \"file\" or \"mysql\"")
    public String file_type = "file";

    @Setting(value="debug-messages")
    public Map<String, Boolean>  debug_messages = createMapDebug();
    private Map<String, Boolean> createMapDebug() {
        Map<String, Boolean> myMap = new HashMap<>();
        myMap.put("blocks", false);
        myMap.put("default", false);
        myMap.put("entity", false);
        myMap.put("player", false);
        myMap.put("world", false);
        myMap.put("spawn", false);
        return myMap;
    }

    @Setting(comment = "Default flag values for new regions.\nThis will not change the values for already created regions.")
    public Map<String, Boolean> flags = createMapFlags();
    private Map<String, Boolean> createMapFlags(){
        Map<String,Boolean> myMap = new HashMap<>();
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
        myMap.put("pvp", false);
        myMap.put("smart-door", true);
        myMap.put("spawn-animals", true);
        myMap.put("spawn-monsters", true);
        myMap.put("teleport", false);
        myMap.put("use-potions", false);
        return myMap;
    }

    @Setting(value = "flags-configuration")
    public flagsConfig flags_configuration = new flagsConfig();
    @ConfigSerializable
    public static class flagsConfig {
        @Setting(value = "change-flag-delay", comment = "Delay to change the same flag again, if listed.")
        public flagsDelay change_flag_delay = new flagsDelay();
        @ConfigSerializable
        public static class flagsDelay {
            @Setting
            public boolean enable = true;
            @Setting
            public List<String> flags = Collections.singletonList("pvp");
            @Setting
            public int seconds = 10;
        }
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
                "build");
    }

    @Setting(value = "flat-file", comment = "Options for flatfile database.")
    public flatFile flat_file = new flatFile();
    @ConfigSerializable
    public static class flatFile {
        @Setting(value = "auto-save-interval-seconds")
        public int auto_save_interval_seconds = 3600;
        @Setting
        public boolean backup = true;
        @Setting(value = "max-backups")
        public int max_backups = 10;
        @Setting(value = "region-per-file", comment = "Save every region in your own file? More safer way to save regions, \nbut will create multiple files (where is not a problem).")
        public boolean region_per_file = false;
    }

    @Setting(comment = "Available: EN-US, PT-BR, ZH-CN, DE-DE, RU-RU, FR")
    public String language = "EN-US";
    @Setting(value = "log-actions",comment = "Log all redprotect commands?")
    public boolean log_actions = true;

    @Setting(comment = "Mysql options")
    public mysqlOptions mysql = new mysqlOptions();
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

    @Setting(value = "needed-claim-to-build", comment = "Adding world names to the list will automatically enable this on that world.")
    public needClaim needed_claim_to_build = new needClaim();
    @ConfigSerializable
    public static class needClaim {
        @Setting(value = "allow-break-blocks", comment = "Allow break only this blocks on worlds where this is enabled.")
        public List<String> allow_break_blocks = Arrays.asList("minecraft:grass", "minecraft:tall_grass");
        @Setting(value = "allow-only-protections-blocks", comment = "Allow place only protection blocks like the sign and the \"block-id\".")
        public boolean allow_only_protections_blocks = true;
        @Setting(comment = "Add your worlds here to allow players to place blocks only in your own claims.")
        public List<String> worlds = Collections.singletonList("example_world");
    }

    @Setting(value = "nether-protection")
    public netherProtection nether_protection = new netherProtection();
    @ConfigSerializable
    public static class netherProtection {
        @Setting(value = "execute-cmd", comment = "If the player go to your world nether roof, this commands be fired.")
        public List<String> execute_cmd = Collections.singletonList("spawn other {player}");
        @Setting(comment = "The size of your netehr world(s).")
        public int maxYsize = 128;
    }

    @Setting(comment = "Show messages on enter, exit and welcome messages.")
    public notifyCat notify = new notifyCat();
    @ConfigSerializable
    public static class notifyCat {
        @Setting(value = "region-enter-mode", comment = "Available: \"BOSSBAR\", \"OFF\" or \"CHAT\"")
        public String region_enter_mode = "CHAT";
        @Setting(value = "region-exit", comment = "Notify on exit region? (in most cases will be wilderness)")
        public boolean region_exit = true;
        @Setting(value = "welcome-mode", comment = "Available: \"BOSSBAR\", \"OFF\" or \"CHAT\"")
        public String welcome_mode = "CHAT";
    }

    @Setting
    public performanceCat performance = new performanceCat();
    @ConfigSerializable
    public static class performanceCat {
        @Setting(value = "disable-PistonEvent-handler")
        public boolean disable_PistonEvent_handler = false;
        @Setting(value = "disable-onPlayerMoveEvent-handler")
        public boolean disable_onPlayerMoveEvent_handler = false;
    }

    @Setting(value = "permissions-limits", comment = "Declare here the permissions you want to use for your players and groups.")
    public permLimits permissions_limits = new permLimits();
    @ConfigSerializable
    public static class permLimits {
        @Setting
        public List<String> blocks = Collections.singletonList("redprotect.limit.blocks.8000");
        @Setting
        public List<String> claims = Collections.singletonList("redprotect.limit.claim.20");
    }

    @Setting(value = "private", comment = "Private containers using signs.")
    public privateCat private_cat = new privateCat();
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

    @Setting
    public purgeCat purge = new purgeCat();
    @ConfigSerializable
    public static class purgeCat {
        @Setting
        public boolean enabled = false;
        @Setting(value = "ignore-regions-from-players", comment = "Names or UUIDs if in online mode, to bypass purge and regen." +
                "\n*That default uuid its a random one and you can remove*")
        public List<String> ignore_regions_from_players = Collections.singletonList(UUID.randomUUID().toString());

        @Setting
        public regenCat regen = new regenCat();
        @ConfigSerializable
        public static class regenCat {
            @Setting(comment = "If worldedit is installed, regen the region instead remove?")
            public boolean enable = false;
            @Setting(value = "max-area-regen", comment = "Regions with an area greater than this will be ignored.")
            public int max_area_regen = 500;
        }

        @Setting(value = "remove-oldest", comment = "Remove regions where the leader not logged in for more than x days?")
        public int remove_oldest = 90;
    }

    @Setting(value = "region-settings", comment = "general regions settings.")
    public regionSettings region_settings = new regionSettings();
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
        @ConfigSerializable
        public static class borderCat {
            @Setting
            public String material = "minecraft:glowstone";
            @Setting(value = "time-showing")
            public int time_showing = 5;
        }

        @Setting
        public claimCat claim = new claimCat();
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

            @Setting(value = "world-types", comment = "Claim types allowed for normal players. Options: BLOCK, WAND or BOTH.\n"
                    + "-> If BLOCK, the players needs to surround your house with the block type in configuration,\nand place a sign under this fence with [rp] on first line.\n"
                    + "-> If WAND, the players will need a wand (default glass_bottle), click on two point of your region,\nand then use /rp claim [name of region] to claim te region.\n"
                    + "-> If BOTH, will allow both claim type protections.")
            public Map<String, String> world_types = createMapWorldType();
            private Map<String, String> createMapWorldType(){
                Map<String, String> myMap = new HashMap<>();
                for (World w:Sponge.getServer().getWorlds()){
                    myMap.put(w.getName(), "BLOCK");
                }
                return myMap;
            }

            @Setting(value = "claimlimit-per-world", comment = "Split the claim limits per world.")
            public boolean claimlimit_per_world = true;
        }

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

        @Setting(value = "region-list")
        public regionListing region_list = new regionListing();
        @ConfigSerializable
        public static class regionListing {
            @Setting(value = "simple-listing", comment = "Recommended to use true. Its a legacy option.")
            public boolean simple_listing = true;
        }

        @Setting(value = "teleport-time", comment = "Delay to teleport command.")
        public int teleport_time = 3;
        @Setting(value = "world-colors", comment = "Sets the world colors for list command.")
        public Map<String, String> world_colors = createMapWorldColors();
        private Map<String, String> createMapWorldColors(){
            Map<String, String> myMap = new HashMap<>();
            for (World w:Sponge.getServer().getWorlds()){
                if (w.getDimension().getType().equals(DimensionTypes.OVERWORLD)){
                    myMap.put(w.getName(), "&a&l");
                } else
                if (w.getDimension().getType().equals(DimensionTypes.NETHER)){
                    myMap.put(w.getName(), "&c&l");
                } else
                if (w.getDimension().getType().equals(DimensionTypes.THE_END)){
                    myMap.put(w.getName(), "&5&l");
                }
            }
            return myMap;
        }
    }

    @Setting
    public sellCat sell = new sellCat();
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

    @Setting(value = "server-protection")
    public serverProtection server_protection = new serverProtection();
    @ConfigSerializable
    public static class serverProtection {

        @Setting(value = "deny-command-on-worlds", comment = "Deny a command in specific world.")
        public Map<String, List<String>> deny_commands_on_worlds = createMapCmdWorld();
        private Map<String, List<String>> createMapCmdWorld() {
            Map<String, List<String>> myMap = new HashMap<>();
            myMap.put("world", Collections.singletonList("command"));
            return myMap;
        }

        @Setting(value = "deny-playerdeath-by", comment = "Deny player death or get damage by this types of damage. \n" +
                "List of types: https://goo.gl/9EyhSd")
        public List<String> deny_playerdeath_by = Collections.singletonList("SUFFOCATE");
        @Setting(value = "deny-potions", comment = "Deny this types of potions to be used on server. \n" +
                "List of types: https://goo.gl/qKufWT")
        public List<String> deny_potions = Collections.singletonList("INVISIBILITY");

        @Setting(value = "sign-spy", comment = "Show every placed sign for who have the permission \"redprotect.signspy\" and for console.")
        public signSpy sign_spy = new signSpy();
        @ConfigSerializable
        public static class signSpy {
            @Setting
            public boolean enabled = true;
            @Setting(value = "only-console")
            public boolean only_console = true;
        }
    }
    @Setting
    public wandsCat wands = new wandsCat();
    @ConfigSerializable
    public static class wandsCat {
        @Setting(comment = "Its used to define regions and for players to claim regions.")
        public String adminWandID = "minecraft:glass_bottle";
        @Setting(comment = "Checks the clicked block with this on hand to see if theres a region on that block.")
        public String infoWandID = "minecraft:paper";
    }

    @Setting
    public hooksCat hooks = new hooksCat();
    @ConfigSerializable
    public static class hooksCat {
        @Setting(value = "check-uuid-names-onstart", comment = "This will try to convert your player UUIDs to Player Names" +
                "\nif you recently changed your server from Online mode to Offline, and from the other way too.")
        public boolean check_uuid_names_onstart = false;
    }
}
