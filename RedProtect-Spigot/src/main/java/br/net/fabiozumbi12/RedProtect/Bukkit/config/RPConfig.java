package br.net.fabiozumbi12.RedProtect.Bukkit.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import br.net.fabiozumbi12.RedProtect.Bukkit.RPUtil;
import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;

public class RPConfig{
	
	private static RPCommentedConfig comConfig;
	private static YamlConfiguration gflags;
	private static YamlConfiguration signs;
	private static YamlConfiguration GuiItems;
	private static YamlConfiguration Prots;
	private static YamlConfiguration EconomyConfig;
	public static final List<String> AdminFlags = Arrays.asList(
			"spawn-wither",
			"cropsfarm",
			"max-players",
			"can-death", 
			"cmd-onhealth" ,
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
			"set-portal");	
			
	public static void init() {

		gflags = new YamlConfiguration();
		signs = new YamlConfiguration();
		GuiItems = new YamlConfiguration();
		Prots = new YamlConfiguration();
		EconomyConfig = new YamlConfiguration();		
		comConfig = new RPCommentedConfig();

        File main = RedProtect.plugin.getDataFolder();
        File data = new File(main, "data");
        File gui = new File(main, "guiconfig.yml");
        File bvalues = new File(main, "economy.yml");
        File globalflags = new File(main, "globalflags.yml");
        File protections = new File(main, "protections.yml");
        File logs = new File(main, "logs");
        File signsf = new File(main, "signs.yml");
        File schema = new File(main, "schematics"+File.separator+"house1.schematic");

        if (!main.exists()) {
            main.mkdir();
            RedProtect.logger.info("Created folder: " + main);
        }

        if (!data.exists()) {
            data.mkdir();
            RedProtect.logger.info("Created folder: " + main);
        }

        //init config
        comConfig.addDef();

        if (!globalflags.exists()) {
            try {
                globalflags.createNewFile();//create globalflags file
                RedProtect.logger.info("Created globalflags file: " + globalflags);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!signsf.exists()) {
            try {
                signsf.createNewFile();//create PathSigns file
                RedProtect.logger.info("Created signs file: " + signsf);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!gui.exists()) {
            RPUtil.saveResource("/assets/redprotect/guiconfig.yml", gui);//create guiconfig file
            RedProtect.logger.info("Created guiconfig file: " + gui);
        }

        if (!bvalues.exists()) {
            RPUtil.saveResource("/assets/redprotect/economy.yml", bvalues);//create blockvalues file
            RedProtect.logger.info("Created economy file: " + bvalues);
        }

        if (!protections.exists()) {
            RPUtil.saveResource("/assets/redprotect/protections.yml", protections);//create protections file
            RedProtect.logger.info("Created protections file: " + protections);
        }

        if (!schema.exists()) {
            new File(main, "schematics").mkdir();
            RPUtil.saveResource("/assets/redprotect/schematics/house1.schematic", schema);//save schematic file
            RedProtect.logger.info("Saved schematic file: house1.schematic");
        }

        RedProtect.logger.info("Server version: " + RedProtect.serv.getBukkitVersion());

        // check if can enable json support
        if (getBool("region-settings.region-list.hover-and-click-teleport")){
            try {
                Class.forName("com.google.gson.JsonParser");
                if (RedProtect.serv.getBukkitVersion().contains("1.7")){
                    RedProtect.plugin.getConfig().set("region-settings.region-list.hover-and-click-teleport", false);
                    RedProtect.logger.warning("Your server version do not support Hover and Clicking region features, only 1.8.+");
                }
            } catch(ClassNotFoundException e ) {
                RedProtect.plugin.getConfig().set("region-settings.region-list.hover-and-click-teleport", false);
                RedProtect.logger.warning("Your server version do not support JSON events, disabling Hover and Clicking region features.");
            }
        }

        //add op to ignore list fro purge
        if (RedProtect.plugin.getConfig().getStringList("purge.ignore-regions-from-players").size() <= 0){
            List<String> ops = RedProtect.plugin.getConfig().getStringList("purge.ignore-regions-from-players");
            for (OfflinePlayer play:RedProtect.serv.getOperators()){
                ops.add(play.getName());
            }
            RedProtect.plugin.getConfig().set("purge.ignore-regions-from-players", ops);
        }

        //add op to ignore list fro sell
        if (RedProtect.plugin.getConfig().getStringList("sell.ignore-regions-from-players").size() <= 0){
            List<String> ops = RedProtect.plugin.getConfig().getStringList("sell.ignore-regions-from-players");
            for (OfflinePlayer play:RedProtect.serv.getOperators()){
                ops.add(play.getName());
            }
            RedProtect.plugin.getConfig().set("sell.ignore-regions-from-players", ops);
        }

        //add allowed claim worlds to config
        if (RedProtect.plugin.getConfig().getStringList("allowed-claim-worlds").get(0).equals("example_world")) {
            List<String> worlds = new ArrayList<>();
            for (World w:RedProtect.serv.getWorlds()){
                worlds.add(w.getName());
                RedProtect.logger.warning("Added world to claim list " + w.getName());
            }
            worlds.remove("example_world");
            RedProtect.plugin.getConfig().set("allowed-claim-worlds", worlds);
        }

        //add worlds to color list
        for (World w:RedProtect.serv.getWorlds()){
            if (RedProtect.plugin.getConfig().getString("region-settings.claim-type.worlds."+w.getName()) == null) {
                RedProtect.plugin.getConfig().set("region-settings.claim-type.worlds."+w.getName(), "BLOCK");
            }

            if (RedProtect.plugin.getConfig().getString("region-settings.world-colors."+w.getName()) == null) {
                if (w.getEnvironment().equals(Environment.NORMAL)){
                    RedProtect.plugin.getConfig().set("region-settings.world-colors."+w.getName(), "&a&l");
                } else
                if (w.getEnvironment().equals(Environment.NETHER)){
                    RedProtect.plugin.getConfig().set("region-settings.world-colors."+w.getName(), "&c&l");
                } else
                if (w.getEnvironment().equals(Environment.THE_END)){
                    RedProtect.plugin.getConfig().set("region-settings.world-colors."+w.getName(), "&5&l");
                }
                RedProtect.logger.warning("Added world to color list " + w.getName());
            }
        }

                    /*----------------- Add default config for not updateable configs ------------------*/

        //update new player flags according version

        List<String> flags = RedProtect.plugin.getConfig().getStringList("flags-configuration.enabled-flags");
        int configUp = 0;
        if (RedProtect.plugin.getConfig().getDouble("config-version") < 6.8D){
            RedProtect.plugin.getConfig().set("config-version", 6.8D);

            if (!flags.contains("smart-door")){
                flags.add("smart-door");
            }
            if (!flags.contains("allow-potions")){
                flags.add("allow-potions");
            }
            if (!flags.contains("mob-loot")){
                flags.add("mob-loot");
            }
            if (!flags.contains("flow-damage")){
                flags.add("flow-damage");
            }
            configUp++;
        }
        if (RedProtect.plugin.getConfig().getDouble("config-version") < 6.9D){
            RedProtect.plugin.getConfig().set("config-version", 6.9D);

            if (!flags.contains("iceform-player")){
                flags.add("iceform-player");
            }
            if (!flags.contains("iceform-entity")){
                flags.add("iceform-entity");
            }
            configUp++;
        }
        if (RedProtect.plugin.getConfig().getDouble("config-version") < 7.0D){
            RedProtect.plugin.getConfig().set("config-version", 7.0D);

            if (!flags.contains("allow-fly")){
                flags.add("allow-fly");
            }
            configUp++;
        }
        if (RedProtect.plugin.getConfig().getDouble("config-version") < 7.1D){
            RedProtect.plugin.getConfig().set("config-version", 7.1D);

            if (!flags.contains("teleport")){
                flags.add("teleport");
            }
            configUp++;
        }
        if (RedProtect.plugin.getConfig().getDouble("config-version") < 7.2D){
            RedProtect.plugin.getConfig().set("config-version", 7.2D);

            if (!flags.contains("clan")){
                flags.add("clan");
            }
            configUp++;
        }
        if (RedProtect.plugin.getConfig().getDouble("config-version") < 7.3D){
            RedProtect.plugin.getConfig().set("config-version", 7.3D);

            if (!flags.contains("ender-chest")){
                flags.add("ender-chest");
            }
            configUp++;
        }
        if (RedProtect.plugin.getConfig().getDouble("config-version") < 7.5D){
            RedProtect.plugin.getConfig().set("config-version", 7.5D);

            if (flags.contains("iceform-entity")){
                flags.add("iceform-world");
                flags.remove("iceform-entity");
            }
            if (!flags.contains("can-grow")){
                flags.add("can-grow");
            }
            configUp++;
        }
        if (RedProtect.plugin.getConfig().getDouble("config-version") < 7.6D){
            RedProtect.plugin.getConfig().set("config-version", 7.6D);

            if (flags.contains("allow-potions")){
                flags.remove("allow-potions");
            }
            if (!flags.contains("use-potions")){
                flags.add("use-potions");
            }
            if (!flags.contains("allow-effects")){
                flags.add("allow-effects");
            }
            configUp++;
        }
        if (RedProtect.plugin.getConfig().getDouble("config-version") < 7.7D){
            RedProtect.plugin.getConfig().set("config-version", 7.7D);

            if (!flags.contains("allow-spawner")){
                flags.add("allow-spawner");
            }
            configUp++;
        }
        if (RedProtect.plugin.getConfig().getDouble("config-version") < 7.8D){
            RedProtect.plugin.getConfig().set("config-version", 7.8D);

            if (!flags.contains("leaves-decay")){
                flags.add("leaves-decay");
            }
            configUp++;
        }
        if (RedProtect.plugin.getConfig().getDouble("config-version") < 7.9D){
            RedProtect.plugin.getConfig().set("config-version", 7.9D);

            if (!flags.contains("build")){
                flags.add("build");
            }
            configUp++;
        }
        if (RedProtect.plugin.getConfig().getDouble("config-version") < 7.10D){
            RedProtect.plugin.getConfig().set("config-version", 7.10D);

            RedProtect.plugin.getConfig().set("language", RedProtect.plugin.getConfig().getString("language").toUpperCase());
            configUp++;
        }
        if (configUp > 0){
            RedProtect.plugin.getConfig().set("flags-configuration.enabled-flags", flags);
            RedProtect.logger.warning("Configuration UPDATE! We added new flags to &lflags-configuration > enabled-flags&r!");
        }

        			/*------------------------------------------------------------------------------------*/

        //load protections file
        Prots = updateFile(protections);

        			/*------------------------------------------------------------------------------------*/

        //load and write globalflags to global file
        gflags = YamlConfiguration.loadConfiguration(globalflags);

        for (World w:RedProtect.serv.getWorlds()){
            gflags.set(w.getName()+".build", gflags.getBoolean(w.getName()+".build", true));
            gflags.set(w.getName()+".liquid-flow", gflags.getBoolean(w.getName()+".liquid-flow", true));
            gflags.set(w.getName()+".allow-changes-of.water-flow", gflags.getBoolean(w.getName()+".allow-changes-of.water-flow", true));
            gflags.set(w.getName()+".allow-changes-of.lava-flow", gflags.getBoolean(w.getName()+".allow-changes-of.lava-flow", true));
            gflags.set(w.getName()+".allow-changes-of.leaves-decay", gflags.getBoolean(w.getName()+".allow-changes-of.leaves-decay", true));
            gflags.set(w.getName()+".allow-changes-of.flow-damage", gflags.getBoolean(w.getName()+".allow-changes-of.flow-damage", true));
            gflags.set(w.getName()+".if-build-false.break-blocks", gflags.getStringList(w.getName()+".if-build-false.break-blocks"));
            gflags.set(w.getName()+".if-build-false.place-blocks", gflags.getStringList(w.getName()+".if-build-false.place-blocks"));
            gflags.set(w.getName()+".pvp", gflags.getBoolean(w.getName()+".pvp", true));
            gflags.set(w.getName()+".iceform-by.player", gflags.getBoolean(w.getName()+".iceform-by.player", false));
            gflags.set(w.getName()+".iceform-by.world", gflags.getBoolean(w.getName()+".iceform-by.world", true));
            gflags.set(w.getName()+".interact", gflags.getBoolean(w.getName()+".interact", true));
            gflags.set(w.getName()+".if-interact-false.allow-blocks", gflags.getStringList(w.getName()+".if-interact-false.allow-blocks"));
            gflags.set(w.getName()+".if-interact-false.allow-entities", gflags.getStringList(w.getName()+".if-interact-false.allow-entities"));
            gflags.set(w.getName()+".use-minecart", gflags.getBoolean(w.getName()+".use-minecart", true));
            gflags.set(w.getName()+".entity-block-damage", gflags.getBoolean(w.getName()+".entity-block-damage", false));
            gflags.set(w.getName()+".explosion-entity-damage", gflags.getBoolean(w.getName()+".explosion-entity-damage", true));
            gflags.set(w.getName()+".fire-block-damage", gflags.getBoolean(w.getName()+".fire-block-damage", false));
            gflags.set(w.getName()+".fire-spread", gflags.getBoolean(w.getName()+".fire-spread", false));
            gflags.set(w.getName()+".player-hurt-monsters", gflags.getBoolean(w.getName()+".player-hurt-monsters", true));
            gflags.set(w.getName()+".player-hurt-passives", gflags.getBoolean(w.getName()+".player-hurt-passives", true));
            gflags.set(w.getName()+".spawn-monsters", gflags.getBoolean(w.getName()+".spawn-monsters", true));
            gflags.set(w.getName()+".spawn-passives", gflags.getBoolean(w.getName()+".spawn-passives", true));
            gflags.set(w.getName()+".remove-entities-not-allowed-to-spawn", gflags.getBoolean(w.getName()+".remove-entities-not-allowed-to-spawn", false));
            gflags.set(w.getName()+".elytra.allow", gflags.getBoolean(w.getName()+".elytra.allow", true));
            gflags.set(w.getName()+".elytra.boost", gflags.getDouble(w.getName()+".elytra.boost", 0.5D));
            gflags.set(w.getName()+".deny-item-usage.allow-on-claimed-rps", gflags.getBoolean(w.getName()+".deny-item-usage.allow-on-claimed-rps", true));
            gflags.set(w.getName()+".deny-item-usage.allow-on-wilderness", gflags.getBoolean(w.getName()+".deny-item-usage.allow-on-wilderness", true));
            gflags.set(w.getName()+".deny-item-usage.items", gflags.getStringList(w.getName()+".deny-item-usage.items"));
            gflags.set(w.getName()+".player-velocity.walk-speed", gflags.getDouble(w.getName()+".player-velocity.walk-speed", -1));
            gflags.set(w.getName()+".player-velocity.fly-speed", gflags.getDouble(w.getName()+".player-velocity.fly-speed", -1));
            gflags.set(w.getName()+".on-enter-cmds", gflags.getStringList(w.getName()+".on-enter-cmds"));
            gflags.set(w.getName()+".on-exit-cmds", gflags.getStringList(w.getName()+".on-exit-cmds"));
            gflags.set(w.getName()+".spawn-wither", gflags.getBoolean(w.getName()+".spawn-wither", true));
            gflags.set(w.getName()+".invincible", gflags.getBoolean(w.getName()+".invincible", false));
            gflags.set(w.getName()+".player-candrop", gflags.getBoolean(w.getName()+".player-candrop", true));
            gflags.set(w.getName()+".player-canpickup", gflags.getBoolean(w.getName()+".player-canpickup", true));
            gflags.set(w.getName()+".rain.trys-before-rain", gflags.getInt(w.getName()+".rain.trys-before-rain", 3));
            gflags.set(w.getName()+".rain.duration", gflags.getInt(w.getName()+".rain.duration", 60));
            gflags.set(w.getName()+".allow-crops-trample", gflags.getBoolean(w.getName()+".allow-crops-trample", true));
            if (!gflags.contains(w.getName()+".command-ranges")){
                gflags.set(w.getName()+".command-ranges.home.min-range", gflags.getDouble(w.getName()+".command-ranges.home.min-range", 0));
                gflags.set(w.getName()+".command-ranges.home.max-range", gflags.getDouble(w.getName()+".command-ranges.home.max-range", w.getMaxHeight()));
                gflags.set(w.getName()+".command-ranges.home.message", gflags.getString(w.getName()+".command-ranges.home.message", "&cYou cant use /home when mining or in caves!"));
            }

            w.setSpawnFlags(gflags.getBoolean(w.getName()+".spawn-monsters"), gflags.getBoolean(w.getName()+".spawn-passives"));
            RedProtect.logger.debug("Spawn Animals: " + w.getAllowAnimals() + " | " + "Spawn Monsters: " + w.getAllowMonsters());
        }


                    /*------------------------------------------------------------------------------------*/

        //load and write GuiItems to guiconfig file
        try {
            GuiItems.load(gui);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        YamlConfiguration GuiBase = inputLoader(RedProtect.plugin.getResource("assets/redprotect/guiconfig.yml"));

        GuiItems.set("gui-strings.value", GuiItems.getString("gui-strings.value", "&bValue: "));
        GuiItems.set("gui-strings.true", GuiItems.getString("gui-strings.true", "&atrue"));
        GuiItems.set("gui-strings.false", GuiItems.getString("gui-strings.false", "&cfalse"));
        GuiItems.set("gui-strings.separator", GuiItems.getString("gui-strings.separator", "&7|"));

        GuiItems.set("gui-separator.material", GuiItems.getString("gui-separator.material", "THIN_GLASS"));
        GuiItems.set("gui-separator.data", GuiItems.getInt("gui-separator.data", 0));

        for (String key:getDefFlagsValues().keySet()){
            GuiItems.set("gui-flags."+key+".slot", GuiItems.get("gui-flags."+key+".slot", GuiBase.get("gui-flags."+key+".slot", getDefFlagsValues().size())));
            GuiItems.set("gui-flags."+key+".material", GuiItems.get("gui-flags."+key+".material", GuiBase.get("gui-flags."+key+".material", "GOLDEN_APPLE")));
            GuiItems.set("gui-flags."+key+".name", GuiItems.get("gui-flags."+key+".name", GuiBase.get("gui-flags."+key+".name", "&e"+key)));
            GuiItems.set("gui-flags."+key+".description", GuiItems.get("gui-flags."+key+".description", GuiBase.get("gui-flags."+key+".description", "&bDescription: &2Add a flag description here.")));
            GuiItems.set("gui-flags."+key+".description1", GuiItems.get("gui-flags."+key+".description1", GuiBase.get("gui-flags."+key+".description1", "")));
            GuiItems.set("gui-flags."+key+".description2", GuiItems.get("gui-flags."+key+".description2", GuiBase.get("gui-flags."+key+".description2", "")));
        }

                    /*------------------------------------------------------------------------------------*/

        //load blockvalues file
        try {
            EconomyConfig.load(bvalues);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        YamlConfiguration tempEco = inputLoader(RedProtect.plugin.getResource("assets/redprotect/economy.yml"));
        for (String key:tempEco.getKeys(false)){
            if (EconomyConfig.get(key) == null){
                EconomyConfig.set(key, tempEco.get(key));
            }
        }

        if (EconomyConfig.getConfigurationSection("items.values").getKeys(false).size() != Material.values().length){
            for (Material mat:Material.values()){
                if (EconomyConfig.getString("items.values."+mat.name()) == null){
                    EconomyConfig.set("items.values."+mat.name(), 0.0);
                }
            }
        }

        if (EconomyConfig.getConfigurationSection("enchantments.values").getKeys(false).size() != Enchantment.values().length){
            for (Enchantment ench:Enchantment.values()){
                if (EconomyConfig.getString("enchantments.values."+ench.getName()) == null){
                    EconomyConfig.set("enchantments.values."+ench.getName(), 0.0);
                }
            }
        }

        //////////////////////
                    /*------------------------------------------------------------------------------------*/

        String v = RedProtect.serv.getBukkitVersion();
        if (RedProtect.plugin.getConfig().getString("notify.region-enter-mode").equalsIgnoreCase("TITLE") && (v == null || !v.contains("1.8"))) {
            RedProtect.plugin.getConfig().set("notify.region-enter-mode", "CHAT");
            RedProtect.logger.warning("Title notifications is not suported on servers not running 1.8! Defaulting to CHAT.");
        }

        //create logs folder
        if(getBool("log-actions") && !logs.exists()){
            logs.mkdir();
            RedProtect.logger.info("Created folder: " + logs);
        }

        //Load signs file
        try {
            signs.load(signsf);
        } catch (Exception e) {
            e.printStackTrace();
        }

        save();
        RedProtect.logger.info("All configurations loaded!");
	}
    
	public static String getWorldClaimType(String w){
		return RedProtect.plugin.getConfig().getString("region-settings.claim-type.worlds."+w);
	}
	
	public static boolean hasGlobalKey(String path){
		return gflags.contains(path);
	}
	
	public static String getGlobalFlagString(String string) {		
		return gflags.getString(string);
	}
	
	public static double getGlobalFlagDouble(String key){		
		return gflags.getDouble(key);
	}
	
	public static float getGlobalFlagFloat(String key){			
		return Float.valueOf(gflags.getString(key));
	}
	
	public static int getGlobalFlagInt(String key){		
		return gflags.getInt(key);
	}
	
    public static Boolean getGlobalFlagBool(String key){		
		return gflags.getBoolean(key);
	}
    
    public static List<String> getGlobalFlagList(String key){		
		return gflags.getStringList(key);
	}
    
    public static ItemStack getGuiItemStack(String key){
    	RedProtect.logger.debug("Gui Material to get: " + key);
    	RedProtect.logger.debug("Result: " + GuiItems.getString("gui-flags."+key+".material"));
    	return new ItemStack(Material.getMaterial(GuiItems.getString("gui-flags."+key+".material")));
    }
    
    public static String getGuiFlagString(String flag, String option){
    	if (GuiItems.getString("gui-flags."+flag+"."+option) == null){
    		return "";
    	}
    	return ChatColor.translateAlternateColorCodes('&', GuiItems.getString("gui-flags."+flag+"."+option));
    }
    
    public static String getGuiString(String string) {
		return ChatColor.translateAlternateColorCodes('&', GuiItems.getString("gui-strings."+string));
	}
    
    public static int getGuiSlot(String flag) {
		return GuiItems.getInt("gui-flags."+flag+".slot");
	}
    
    public static void setGuiSlot(/*String mat, */String flag, int slot) {
		GuiItems.set("gui-flags."+flag+".slot", slot);
		//GuiItems.set("gui-flags."+flag+".material", mat);
		
	}
    
    public static ItemStack getGuiSeparator() {
    	ItemStack separator = new ItemStack(Material.getMaterial(GuiItems.getString("gui-separator.material")), 1, (short)GuiItems.getInt("gui-separator.data"));
    	ItemMeta meta = separator.getItemMeta();
    	meta.setDisplayName(getGuiString("separator"));
    	meta.setLore(Arrays.asList("", getGuiString("separator")));
    	separator.setItemMeta(meta);
		return separator;
	}
    
    public static int getGuiMaxSlot() {
    	SortedSet<Integer> slots = new TreeSet<>(new ArrayList<>());
    	for (String key:GuiItems.getKeys(true)){
    		if (key.contains(".slot")){
    			slots.add(GuiItems.getInt(key));
    		}    		
    	}
		return Collections.max(slots);
	}
    
    public static Boolean getBool(String key){		
		return RedProtect.plugin.getConfig().getBoolean(key, false);
	}
    
    public static void setConfig(String key, Object value){
    	RedProtect.plugin.getConfig().set(key, value);
    }
    
    public static HashMap<String, Object> getDefFlagsValues(){	
    	HashMap<String,Object> flags = new HashMap<>();
    	for (String flag:RedProtect.plugin.getConfig().getValues(true).keySet()){
    		if (flag.contains("flags.") && isFlagEnabled(flag.replace("flags.", ""))){
    			if (flag.replace("flags.", "").equals("pvp") && !RedProtect.plugin.getConfig().getStringList("flags-configuration.enabled-flags").contains("pvp")){
    				continue;
    			}    			
    			flags.put(flag.replace("flags.", ""), RedProtect.plugin.getConfig().get(flag));
    		}
    	}
    	return flags;
	}
    
    public static boolean isFlagEnabled(String flag){    	
    	return RedProtect.plugin.getConfig().getStringList("flags-configuration.enabled-flags").contains(flag) || AdminFlags.contains(flag);
    }
    
    public static SortedSet<String> getDefFlags(){
        return new TreeSet<>(getDefFlagsValues().keySet());
    }
    
    public static String getString(String key, String def){		
		return RedProtect.plugin.getConfig().getString(key, def);
	}
    
    public static String getString(String key){		
		return RedProtect.plugin.getConfig().getString(key,"");
	}
    
    public static Integer getInt(String key){		
		return RedProtect.plugin.getConfig().getInt(key);
	}
    
    public static List<String> getStringList(String key){		
		return RedProtect.plugin.getConfig().getStringList(key);
	}
    
    public static Material getMaterial(String key){
    	return Material.getMaterial(RedProtect.plugin.getConfig().getString(key));
    }
    
    public static void save(){
    	File main = RedProtect.plugin.getDataFolder();
        File gui = new File(main, "guiconfig.yml");
        File bvalues = new File(main, "economy.yml");
        File globalflags = new File(main, "globalflags.yml");
        File protections = new File(main, "protections.yml");
        File signsf = new File(main, "signs.yml");       
    	try {
			gflags.save(globalflags);
			GuiItems.save(gui);
			EconomyConfig.save(bvalues);
			Prots.save(protections);
			signs.save(signsf);
			comConfig.saveConfig();
		} catch (IOException e) {
			RedProtect.logger.severe("Problems during save file:");
			e.printStackTrace();
		}
    }
    
    public static void saveGui(){ 
    	File guiconfig = new File(RedProtect.plugin.getDataFolder(),"guiconfig.yml");
    	try {
			GuiItems.save(guiconfig);
		} catch (IOException e) {
			RedProtect.logger.severe("Problems during save gui file:");
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
    	boolean bool = RedProtect.plugin.getConfig().getStringList("needed-claim-to-build.worlds").contains(p.getWorld().getName());    	
    	if (bool){
    		if (b != null && getBool("needed-claim-to-build.allow-only-protections-blocks") &&
                    (getWorldClaimType(p.getWorld().getName()).equalsIgnoreCase("BLOCK") ||
                            getWorldClaimType(p.getWorld().getName()).equalsIgnoreCase("BOTH"))){
    			boolean blocks = b.getType().name().contains(getString("region-settings.block-id")) || b.getType().name().contains("SIGN") ||
                        getStringList("needed-claim-to-build.allow-break-blocks").stream().anyMatch(str -> str.equalsIgnoreCase(b.getType().name()));
    			if (!blocks){
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

	public static boolean addFlag(String flag, boolean defaultValue, boolean isAdmin){
	    if (isAdmin){
	        if (!AdminFlags.contains(flag)){
                AdminFlags.add(flag);
                return true;
            }
        } else {
	        if (RedProtect.plugin.getConfig().get("flags."+flag) == null){
                RedProtect.plugin.getConfig().set("flags."+flag, defaultValue);
                List<String> flags = RedProtect.plugin.getConfig().getStringList("flags-configuration.enabled-flags");
                flags.add(flag);
                RedProtect.plugin.getConfig().set("flags-configuration.enabled-flags", flags);
                RedProtect.plugin.saveConfig();
                return true;
            }
        }
        return false;
    }

	public static int getProtInt(String key){
		return Prots.getInt(key);
	}
	
	public static boolean getProtBool(String key){
		return Prots.getBoolean(key);
	}
	
	public static List<String> getProtStringList(String key){
		return Prots.getStringList(key);
	}
	
    /*public static boolean containsProtKey(String key){
		return Prots.contains(key);
	}*/
    
	public static String getProtString(String key){
		return Prots.getString(key);
	}
	
	public static String getProtMsg(String key){
		return ChatColor.translateAlternateColorCodes('&',Prots.getString(key));
	}
		
	public static int getBlockCost(String itemName) {
		return EconomyConfig.getInt("items.values."+itemName);
	}
	
	public static int getEnchantCost(String enchantment) {
		return EconomyConfig.getInt("enchantments.values."+enchantment);
	}
	
	public static String getEcoString(String key){
		return EconomyConfig.getString(key);
	}
	
	public static Integer getEcoInt(String key){
		return EconomyConfig.getInt(key);
	}

	public static boolean getEcoBool(String key) {
		return EconomyConfig.getBoolean(key);
	}
	
	private static YamlConfiguration updateFile(File saved){
		YamlConfiguration finalyml = new YamlConfiguration();    			
        try {
        	finalyml.load(saved);
		} catch (Exception e) {
			e.printStackTrace();
		} 
                			
        YamlConfiguration tempProts = inputLoader(RedProtect.plugin.getResource("assets/redprotect/protections.yml"));
        for (String key:tempProts.getKeys(true)){    
        	Object obj = tempProts.get(key);
        	if (finalyml.get(key) != null){
        		obj = finalyml.get(key);
        	}
        	finalyml.set(key, obj);    	            	   	            	
        }  
        return finalyml;
	}
	
	public static List<Location> getSigns(String rid){
		List<Location> locs = new ArrayList<>();
		for (String s:signs.getStringList(rid)){
			String[] val = s.split(",");
			if (Bukkit.getWorld(val[0]) == null){
				continue;
			}
			locs.add(new Location(Bukkit.getWorld(val[0]),Double.valueOf(val[1]),Double.valueOf(val[2]),Double.valueOf(val[3])));
		}
		return locs;
	}
	
	public static void putSign(String rid, Location loc){
		List<String> lsigns = signs.getStringList(rid);
		String locs = loc.getWorld().getName()+","+loc.getX()+","+loc.getY()+","+loc.getZ();
		if (!lsigns.contains(locs)){
			lsigns.add(locs);
			saveSigns(rid, lsigns);
		}
	}
	
	public static void removeSign(String rid, Location loc){
		List<String> lsigns = signs.getStringList(rid);
		String locs = loc.getWorld().getName()+","+loc.getX()+","+loc.getY()+","+loc.getZ();
		if (lsigns.contains(locs)){
			lsigns.remove(locs);
			saveSigns(rid, lsigns);
		}
	}
	
	private static void saveSigns(String rid, List<String> locs){
		if (locs.isEmpty()){
			signs.set(rid, null);
		} else {
			signs.set(rid, locs);
		}		
		try {
			signs.save(new File(RedProtect.plugin.getDataFolder(),"signs.yml"));
		} catch (IOException e) {
			RedProtect.logger.severe("Problems during save file:");
			e.printStackTrace();
		}
	}
}
   