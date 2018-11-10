package br.net.fabiozumbi12.RedProtect.Sponge.config.Category;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.HashMap;
import java.util.Map;

@ConfigSerializable
public class FlagGuiCategory {

    @Setting(value = "gui-separator")
    public guiSeparator gui_separator = new guiSeparator();
    @Setting(value = "gui-strings", comment = "Gui general customization.")
    public Map<String, String> gui_strings = createMap();
    @Setting(value = "gui-flags")
    public Map<String, GuiFlag> gui_flags = createGuiMaps();

    public FlagGuiCategory() {
    }

    private Map<String, String> createMap() {
        Map<String, String> map = new HashMap<>();
        map.put("false", "&cfalse");
        map.put("separator", "&7|");
        map.put("true", "&atrue");
        map.put("value", "&bValue: ");
        return map;
    }

    private Map<String, GuiFlag> createGuiMaps() {
        Map<String, GuiFlag> guiMap = new HashMap<>();
        guiMap.put("allow-effects", new GuiFlag("&6Description: &aAllow or cancel all", "&atype of effects for non members", "&aof this region.", "minecraft:blaze_rod", "&e=> Allow Effects", 15));
        guiMap.put("allow-fly", new GuiFlag("&6Description: &aAllow players with", "&a&afly enabled to fly on this region.", "", "minecraft:feather", "&e=> Allow Fly", 7));
        guiMap.put("allow-home", new GuiFlag("&6Description: &aAllow no members to use the", "&acommand /sethome or /home to set or come to", "&athis region.", "minecraft:bed", "&e=> Allow Set Home", 2));
        guiMap.put("allow-potions", new GuiFlag("&6Description: &aAllow players to consume", "&apotions ins this region.", "", "minecraft:potion", "&e=> Allow Potions", 14));
        guiMap.put("allow-spawner", new GuiFlag("&6Description: &aAllow players to interact", "&awith spawners in this region.", "", "minecraft:mob_spawner", "&e=> Allow Interact Spawners", 14));
        guiMap.put("build", new GuiFlag("&6Description: &aAllow any player to build", "&ain this region.", "", "minecraft:grass", "&e=> Allow Build", 8));
        guiMap.put("button", new GuiFlag("&6Description: &aAllow players to press", "&abutons in this region.", "", "minecraft:stone_button", "&e=> Allow Buttons", 6));
        guiMap.put("can-grow", new GuiFlag("&6Description: &aChoose if farms", "&ain this region will grow or not.", "", "minecraft:wheat", "&e=> Allow Blocks to Grow", 18));
        guiMap.put("chest", new GuiFlag("&6Description: &aAllow players to open any type of", "&achests in this region.", "", "minecraft:trapped_chest", "&e=> Allow Open Chest", 3));
        guiMap.put("door", new GuiFlag("&6Description: &aAllow no members to open", "&aand close doors in this region.", "", "minecraft:wooden_door", "&e=> Allow Open Doors", 0));
        guiMap.put("ender-chest", new GuiFlag("&6Description: &aAllow players to", "&ause ender chests on this region.", "", "minecraft:ender_chest", "&e=> Allow Ender Chest", 12));
        guiMap.put("fire", new GuiFlag("&6Description: &aAllow damage blocks by fire", "&aand explosion, and fire spread.", "", "minecraft:blaze_powder", "&e=> Fire Spread and Damage Blocks", 19));
        guiMap.put("flow", new GuiFlag("&6Description: &aEnable water and lava flow", "&ain this region.", "", "minecraft:water_bucket", "&e=> Water and Lava Flow", 20));
        guiMap.put("flow-damage", new GuiFlag("&6Description: &aAllow liquids to", "&aremove blocks on flow.", "", "minecraft:lava_bucket", "&e=> Allow Flow Damage", 21));
        guiMap.put("iceform-player", new GuiFlag("&6Description: &aAllow ice form", "&aby players using frost walk", "&aenchant.", "minecraft:packed_ice", "&e=> Allow Ice Form by Players", 4));
        guiMap.put("iceform-world", new GuiFlag("&6Description: &aAllow ice form", "&aby entities like SnowMan and by", "&aweather like snow.", "minecraft:ice", "&e=> Allow Ice Form by World", 22));
        guiMap.put("leaves-decay", new GuiFlag("&6Description: &aAllow leaves decay naturally", "&ain this region.", "", "minecraft:leaves", "&e=> Allow Leaves decay", 6));
        guiMap.put("lever", new GuiFlag("&6Description: &aAllow no members to use", "&alevers in this region.", "", "minecraft:lever", "&e=> Allow Lever", 5));
        guiMap.put("minecart", new GuiFlag("&6Description: &aAllow no members to place,", "&aenter and break Minecarts in this region.", "", "minecraft:minecart", "&e=> Allow Place Minecarts/Boats", 17));
        guiMap.put("mob-loot", new GuiFlag("&6Description: &aAllow mobs to damage,", "&aexplode or grief blocks on this", "&aregion.", "minecraft:mycelium", "&e=> Allow Mob Grief", 23));
        guiMap.put("passives", new GuiFlag("&6Description: &aAllow no members to hurt,", "&akill or interact with passives mobs in", "&athis region.", "minecraft:saddle", "&e=> Hurt/Interact Passives", 24));
        guiMap.put("pvp", new GuiFlag("&6Description: &aAllow PvP for all players", "&ain this region, including members and", "&ano members.", "minecraft:stone_sword", "&e=> Allow PvP", 11));
        guiMap.put("smart-door", new GuiFlag("&6Description: &aAllow members to open", "&adouble normal and iron doors", "&aand iron trap doors together.", "minecraft:iron_door", "&e=> Open Double and Iron Doors", 1));
        guiMap.put("spawn-animals", new GuiFlag("&6Description: &aAllow natural spawn of", "&apassives mobs in this region.", "", "minecraft:egg", "&e=> Spawn Animals", 25));
        guiMap.put("spawn-monsters", new GuiFlag("&6Description: &aAllow natural spawn of", "&amonsters in this region.", "", "minecraft:pumpkin", "&e=> Allow Spawn Monsters", 26));
        guiMap.put("teleport", new GuiFlag("&6Description: &aAllow players to", "&ateleport on this region using itens", "&alike ender pearls and chorus fruits.", "minecraft:ender_pearl", "&e=> Allow Teleport", 10));
        guiMap.put("use-potions", new GuiFlag("&6Description: &aAllow use or throw", "&aany type of potions for no members", "&aof region.", "minecraft:glass_bottle", "&e=> Use Potions", 16));
        guiMap.put("press-plate", new GuiFlag("&6Description: &aAllow players to", "&awalk on presure plates and interact.", "", "minecraft:light_weighted_pressure_plate", "&e=> Use Pressure Plates", 9));
        return guiMap;
    }

    @ConfigSerializable
    public static class guiSeparator {
        @Setting(comment = "Color? Wood type?")
        public int data = 0;
        @Setting(comment = "The item names is like you see holding \"F3\" and pressing \"H\".")
        public String material = "minecraft:stained_glass_pane";
    }

    @ConfigSerializable
    public static class GuiFlag {
        @Setting
        public String description = "&bDescription: &2Add a flag description here.";
        @Setting
        public String description1 = "";
        @Setting
        public String description2 = "";
        @Setting
        public String material = "golden_apple";
        @Setting
        public String name = "";
        @Setting
        public int slot = 0;

        public GuiFlag() {
        }

        public GuiFlag(String name, int slot) {
            this.name = name;
            this.slot = slot;
        }

        public GuiFlag(String desc, String desc1, String desc2, String mat, String name, int slot) {
            this.description = desc;
            this.description1 = desc1;
            this.description2 = desc2;
            this.material = mat;
            this.name = name;
            this.slot = slot;
        }
    }
}
