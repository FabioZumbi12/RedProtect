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

package br.net.fabiozumbi12.RedProtect.Core.config.Category;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.*;

@ConfigSerializable
public class GlobalFlagsCategory {

    @Setting
    public Map<String, WorldProperties> worlds = new HashMap<>();

    public GlobalFlagsCategory() {
    }

    @ConfigSerializable
    public static class WorldProperties {

        @Setting(comment = "Players can build in this world?")
        public boolean build = true;

        @Setting(value = "if-build-false", comment = "If build option is false, choose what blocks the player can place/break.\n" +
                "The item names is like you see holding \"F3\" and pressing \"H\".")
        public buildFalse if_build_false = new buildFalse();
        @Setting(comment = "Allow pvp?")
        public boolean pvp = true;
        @Setting(comment = "Allow player interactions, with all entities or blocks?")
        public boolean interact = true;
        @Setting(value = "if-interact-false", comment = "If interact option is false, choose what blocks or entity the player can interact.\n" +
                "The item names is like you see holding \"F3\" and pressing \"H\".\n" +
                "The entity name you can see enabling debug type \"entity\" on debug options and interacting with the entity.")
        public interactFalse if_interact_false = new interactFalse();
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
        @Setting(value = "spawn-allow-on-regions", comment = "Allow entities to spawn only inside regions if blacklisted/whitelisted?")
        public boolean spawn_allow_on_regions = false;
        @Setting(value = "spawn-whitelist", comment = "" +
                "spawn-whitelist: ONLY this mobs will spawn in this world!\n\n" +
                "You can use MONSTERS or PASSIVES groups.\n" +
                "Check the entity types here:\n" +
                "Spigot: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/EntityType.html\n" +
                "Sponge: https://jd.spongepowered.org/7.2.0/org/spongepowered/api/entity/EntityTypes.html")
        public List<String> spawn_whitelist = new ArrayList<>();
        @Setting(value = "spawn-blacklist", comment = "" +
                "spawn-blacklist: This mobs will NOT spawn in this world!\n\n" +
                "You can use MONSTERS or PASSIVES groups.\n" +
                "Check the entity types here:\n" +
                "Spigot: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/EntityType.html\n" +
                "Sponge: https://jd.spongepowered.org/7.2.0/org/spongepowered/api/entity/EntityTypes.html")
        public List<String> spawn_blacklist = new ArrayList<>();
        @Setting(value = "allow-crops-trample")
        public boolean allow_crop_trample = true;
        @Setting(value = "deny-death-by", comment = "Deny player death or get damage by this types of damage. List of types:\n" +
                "Sponge: https://jd.spongepowered.org/7.2.0/org/spongepowered/api/event/cause/entity/damage/DamageTypes.html\n" +
                "Spigot: https://hub.spigotmc.org/javadocs/spigot/index.html?overview-summary.html")
        public List<String> deny_death_by = new ArrayList<>();
        @Setting(value = "deny-potions", comment = "Deny this types of potions to be used on this world.\n" +
                "Sponge: https://jd.spongepowered.org/7.2.0/org/spongepowered/api/effect/potion/PotionEffectTypes.html\n" +
                "Spigot: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/potion/PotionType.html")
        public List<String> deny_potions = Collections.singletonList("INVISIBILITY");
        @Setting(value = "command-on-damage", comment = "Execute commands when a player get damaged by a specific type.\n" +
                "Placeholders: {player} to player name and {damage} to damage type.\n" +
                "List of types:\n" +
                "Sponge: https://jd.spongepowered.org/7.2.0/org/spongepowered/api/event/cause/entity/damage/DamageTypes.html\n" +
                "Spigot: https://hub.spigotmc.org/javadocs/spigot/index.html?overview-summary.html")
        public Map<String, String> command_on_damage = createCmdMap();

        private Map<String, String> createCmdMap() {
            Map<String, String> map = new HashMap<>();
            map.put("VOID", "spawn {player}");
            return map;
        }

        @Setting
        public weatherCat weather = new weatherCat();
        @Setting(value = "deny-item-usage", comment = "Control what items the player can use. Use '/rp debug-item' with the item in your hand to get the type.")
        public denyItemUsage deny_item_usage = new denyItemUsage();
        @Setting(value = "on-enter-cmds", comment = "Execute this command on enter in this world.\nYou can use this placeholders: {world-from}, {world-to} and {player}")
        public List<String> on_enter_cmds = new ArrayList<>();
        @Setting(value = "on-exit-cmds", comment = "Execute this command on exit this world.\nYou can use this placeholders: {world-from}, {world-to} and {player}")
        public List<String> on_exit_cmds = new ArrayList<>();
        @Setting(value = "allow-changes-of")
        public allowChangesOf allow_changes_of = new allowChangesOf();
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
        @Setting(value = "iceform-by")
        public iceFormCat iceform_by = new iceFormCat();
        @Setting(comment = "Deny players to bypass worldborders.")
        public borderCat border = new borderCat();
        @Setting(value = "player-velocity")
        public playerVeloCat player_velocity = new playerVeloCat();
        @Setting(value = "player-glide")
        public playerGlide player_glide = new playerGlide();

        private Map<String, CommandRanges> createMap() {
            Map<String, CommandRanges> map = new HashMap<>();
            map.put("home-command", new CommandRanges());
            return map;
        }

        @ConfigSerializable
        public static class borderCat {
            @Setting(value = "deny-bypass")
            public boolean deny_bypass = true;
            @Setting(value = "execute-command")
            public String execute_command = "spawn {player}";
        }

        @ConfigSerializable
        public static class weatherCat {
            @Setting(value = "allow-weather", comment = "Allow weather changes?")
            public boolean allow_weather = true;
            @Setting(value = "rain-time", comment = "The duration of weather rain.")
            public int rain_time = 60;
            @Setting(value = "attempts-before-rain", comment = "Every x attempts will rain.")
            public int attempts_before_rain = 3;
        }

        @ConfigSerializable
        public static class iceFormCat {
            @Setting
            public boolean player = true;
            @Setting
            public boolean entity = true;
        }

        @ConfigSerializable
        public static class playerGlide {
            @Setting(value = "allow-elytra", comment = "Deny player to equip elytra.")
            public boolean allow_elytra = true;
            @Setting(value = "allow-glide", comment = "Deny player to glide.")
            public boolean allow_glide = true;
            @Setting(value = "allow-boost", comment = "Deny player boost using arrow and fireworks.")
            public boolean allow_boost = true;
        }

        @ConfigSerializable
        public static class playerVeloCat {
            @Setting(value = "allow-fly")
            public boolean allow_fly = true;
            @Setting(value = "walk-speed", comment = "Walk speed from 0.0 to 1. Default -1 to disable")
            public float walk_speed = -1;
            @Setting(value = "fly-speed", comment = "Fly speed from 0.0 to 1. Default -1 to disable")
            public float fly_speed = -1;
        }

        @ConfigSerializable
        public static class buildFalse {

            @Setting(value = "break-blocks")
            public breakBlocks break_blocks = new breakBlocks();
            @Setting(value = "place-blocks")
            public placeBlocks place_blocks = new placeBlocks();

            @ConfigSerializable
            public static class breakBlocks {
                @Setting(comment = "This blocks will not be allowed to be break, all others yes.")
                public List<String> blacklist = new ArrayList<>();

                @Setting(comment = "Only this blocks will be allowed to break, all others will not.\n" +
                        "\"minecraft:grass\", \"minecraft:tallgrass\", \"minecraft:red_flower\", \"minecraft:chest\"")
                public List<String> whitelist = new ArrayList<>();
            }

            @ConfigSerializable
            public static class placeBlocks {
                @Setting(comment = "This blocks will not be allowed to be place, all others yes.")
                public List<String> blacklist = new ArrayList<>();

                @Setting(comment = "Only this blocks will be allowed to place, all others will not.")
                public List<String> whitelist = new ArrayList<>();
            }
        }

        @ConfigSerializable
        public static class interactFalse {
            @Setting(value = "interact-blocks")
            public interactBlocks interact_blocks = new interactBlocks();
            @Setting(value = "interact-entities")
            public interactEntities interact_entities = new interactEntities();
            @Setting(value = "entity-passives", comment = "Allow player interactions with passives?")
            public boolean entity_passives = true;
            @Setting(value = "entity-monsters", comment = "Allow player interactions with monsters?")
            public boolean entity_monsters = true;

            @ConfigSerializable
            public static class interactBlocks {
                @Setting(comment = "This items will not be allowed to interact, all other items will be.")
                public List<String> blacklist = new ArrayList<>();

                @Setting(comment = "Only this items will allowed to interact, all other item will not be allowed.\n" +
                        "You can add this blocks to allow basic exploration (accept regex):\n" +
                        "\"minecraft:grass\", \"minecraft:tallgrass\", \"minecraft:red_flower\", \"minecraft:chest\"")
                public List<String> whitelist = new ArrayList<>();
            }

            @ConfigSerializable
            public static class interactEntities {
                @Setting(comment = "Only this entities will not be allowed to interact.")
                public List<String> blacklist = new ArrayList<>();

                @Setting(comment = "Only this entities will be allowed to interact, all others no.")
                public List<String> whitelist = Collections.singletonList("villager");
            }
        }

        @ConfigSerializable
        public static class denyItemUsage {
            @Setting(value = "allow-on-claimed-rps")
            public boolean allow_on_claimed_rps = true;

            @Setting(value = "allow-on-wilderness")
            public boolean allow_on_wilderness = false;

            @Setting(comment = "The item names is like you see holding \"F3\" and pressing \"H\".")
            public List<String> items = new ArrayList<>();
        }

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

        @ConfigSerializable
        public static class CommandRanges {
            @Setting(value = "min-range")
            public double min_range = 0D;

            @Setting(value = "max-range")
            public double max_range = 256D;

            @Setting
            public String message = "&cYou cant use /home when mining or in caves!";

        }
    }
}


