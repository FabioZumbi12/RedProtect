package br.net.fabiozumbi12.RedProtect.Sponge.config.Category;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.*;

@ConfigSerializable
public class GlobalFlagsCategory {

    public GlobalFlagsCategory(){}

    @Setting
    public Map<String, WorldProperties> worlds = new HashMap<>();

    @ConfigSerializable
    public static class WorldProperties {

        @Setting(comment = "Players can build in this world?")
        public boolean build = true;

        @Setting(value = "if-build-false", comment = "If build option is false, choose what blocks the player can place/break.\n" +
                "The item names is like you see holding \"F3\" and pressing \"H\".")
        public buildFalse if_build_false = new buildFalse();
        @ConfigSerializable
        public static class buildFalse {
            @Setting(value = "break-blocks")
            public List<String> break_blocks = Arrays.asList("minecraft:grass", "minecraft:tallgrass", "minecraft:red_flower");

            @Setting(value = "place-blocks")
            public List<String> place_blocks = new ArrayList<>();
        }

        @Setting(comment = "Allow pvp?")
        public boolean pvp = true;

        @Setting(comment = "Allow player interactions, with all entities or blocks?")
        public boolean interact = true;

        @Setting(value = "if-interact-false", comment = "If interact option is false, choose what blocks or entity the player can interact.\n" +
                "The item names is like you see holding \"F3\" and pressing \"H\".")
        public interactFalse if_interact_false = new interactFalse();
        @ConfigSerializable
        public static class interactFalse {
            @Setting(value = "interact-blocks")
            public List<String> interact_blocks = Arrays.asList("minecraft:grass", "minecraft:tallgrass", "minecraft:red_flower");

            @Setting(value = "interact-entities")
            public List<String> interact_entities = Arrays.asList("villager");

            @Setting(comment = "Allow player interactions with passive entities?")
            public boolean entity_passives = true;

            @Setting(comment = "Allow player interactions with monsters entities")
            public boolean entity_monsters = true;
        }

        @Setting(value = "use-minecart", comment = "Allow players to place Minecarts and Boats?")
        public boolean use_minecart = true;

        @Setting(value = "entity-block-damage", comment = "Entities can damage blocks like enderman and creepers?")
        public boolean entity_block_damage = true;

        @Setting(value = "explosion-entity-damage", comment = "Explosions can damage entities?")
        public boolean explosion_entity_damage = true;

        @Setting(value = "fire-block-damage", comment = "Fire can damage blocks like leaves and woods?")
        public boolean fire_block_damage = false;

        @Setting(value = "fire-spread", comment = "Allow fire spread?")
        public boolean fire_spread = false;

        @Setting(value = "player-hurt-monsters", comment = "Players can damage monsters?")
        public boolean player_hurt_monsters = true;

        @Setting(value = "player-hurt-passives", comment = "Players can damage passive entities?")
        public boolean player_hurt_passives = true;

        @Setting(value = "spawn-monsters")
        public boolean spawn_monsters = true;

        @Setting(value = "spawn-passives")
        public boolean spawn_passives = true;

        @Setting(value = "allow-weather", comment = "Allow weather changes?")
        public boolean allow_weather = true;

        @Setting(value = "deny-item-usage", comment = "Control what items the player can use.")
        public denyItemUsage deny_item_usage = new denyItemUsage();
        @ConfigSerializable
        public static class denyItemUsage {
            @Setting(value = "allow-on-claimed-rps")
            public boolean allow_on_claimed_rps = true;

            @Setting(value = "allow-on-wilderness")
            public boolean allow_on_wilderness = false;

            @Setting(comment = "The item names is like you see holding \"F3\" and pressing \"H\".")
            public List<String> items = new ArrayList<>();
        }

        @Setting(value = "on-enter-cmds", comment = "Execute this command on enter in this world.\nYou can use this placeholders: {world-from}, {world-to} and {player}")
        public List<String> on_enter_cmds = new ArrayList<>();

        @Setting(value = "on-exit-cmds", comment = "Execute this command on exit this world.\nYou can use this placeholders: {world-from}, {world-to} and {player}")
        public List<String> on_exit_cmds = new ArrayList<>();

        @Setting(value = "allow-changes-of")
        public allowChangesOf allow_changes_of = new allowChangesOf();
        @ConfigSerializable
        public static class allowChangesOf {
            @Setting(value = "liquid-flow", comment = "Allow any type of liquids to flow? Includes mod liquids.")
            public boolean liquid_flow = true;

            @Setting(value = "water-flow", comment = "This don't bypass liquid-flow option.")
            public boolean water_flow = true;

            @Setting(value = "lava-flow", comment = "This don't bypass liquid-flow option.")
            public boolean lava_flow = true;

            @Setting(value = "leaves-decay")
            public boolean leaves_decay = true;

            @Setting(value = "flow-damage")
            public boolean flow_damage = true;
        }

        @Setting(value = "remove-entities-not-allowed-to-spawn")
        public boolean remove_entities_not_allowed_to_spawn = true;

        @Setting(value = "spawn-wither")
        public boolean spawn_wither = true;

        @Setting(comment = "Entities will be invincible?")
        public boolean invincible = false;

        @Setting(value = "player-candrop")
        public boolean player_candrop = true;

        @Setting(value = "player-canpickup")
        public boolean player_canpickup = true;

        @Setting(value = "block-grow", comment = "Allow blocks to grow like wheat?")
        public boolean block_grow = true;

        @Setting(value = "command-ranges", comment = "Execute commands in certain coordinate ranges.")
        public Map<String, CommandRanges> command_ranges = createMap();
        private Map<String, CommandRanges> createMap(){
            Map<String, CommandRanges> map = new HashMap<>();
            map.put("home-command", new CommandRanges());
            return map;
        }

        @ConfigSerializable
        public static class CommandRanges{
            @Setting(value = "min-range")
            public double min_range = 0D;

            @Setting(value = "max-range")
            public double max_range = 256D;

            @Setting
            public String message = "&cYou cant use /home when mining or in caves!";

        }
    }
}


