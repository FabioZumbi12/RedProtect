package br.net.fabiozumbi12.RedProtect.Bukkit.config;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class RPCommentedGlobalFlags {

	private final HashMap<String, String> comments;
	public YamlConfiguration gflags;

	RPCommentedGlobalFlags(){
		this.comments = new HashMap<>();
		this.gflags = new YamlConfiguration();
	}

	public void addDef(){
		File config = new File(RedProtect.get().getDataFolder(), "globalflags.yml");
		if (config.exists()){
			try {
				gflags.load(config);
			} catch (IOException | InvalidConfigurationException e) {
				e.printStackTrace();
			}
		}

		for (World w:RedProtect.get().serv.getWorlds()){
			setDefault(w.getName(), null, "Configuration section for world "+w.getName()+"");

			setDefault(w.getName()+".build", true, "Players can build in this world?");
			setDefault(w.getName()+".liquid-flow", true, "Allow any type of liquids to flow?");

			setDefault(w.getName()+".allow-changes-of.water-flow", true, null);
			setDefault(w.getName()+".allow-changes-of.lava-flow", true, null);
			setDefault(w.getName()+".allow-changes-of.leaves-decay", true, null);
			setDefault(w.getName()+".allow-changes-of.flow-damage", true, null);

			setDefault(w.getName()+".if-build-false", null, "" +
					"If build option is false, choose what blocks the player can place/break.\n" +
					"Materials: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html");
			setDefault(w.getName()+".if-build-false.break-blocks", new ArrayList<>(), null);
			setDefault(w.getName()+".if-build-false.place-blocks", new ArrayList<>(), null);

			setDefault(w.getName()+".pvp", true, null);

			setDefault(w.getName()+".iceform-by.player", false, null);
			setDefault(w.getName()+".iceform-by.world", true, null);

			setDefault(w.getName()+".interact", true, null);

			setDefault(w.getName()+".if-interact-false", null, "" +
					"If interact option is false, choose what blocks or entity the player can interact.\n" +
					"EntityTypes: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/EntityType.html");
			setDefault(w.getName()+".if-interact-false.allow-blocks", new ArrayList<>(), null);
			setDefault(w.getName()+".if-interact-false.allow-entities", new ArrayList<>(), null);

			setDefault(w.getName()+".use-minecart", true, "Allow to use minecarts and boats in this world?");
			setDefault(w.getName()+".entity-block-damage", false, "Like creeperds and Endermans.");
			setDefault(w.getName()+".explosion-entity-damage", true, "Explosive entities can explode blocks?");
			setDefault(w.getName()+".fire-block-damage", false, "Block will break on fire?");
			setDefault(w.getName()+".fire-spread", false, null);
			setDefault(w.getName()+".player-hurt-monsters", true, null);
			setDefault(w.getName()+".player-hurt-passives", true, null);

			setDefault(w.getName()+".spawn-allow-on-regions", false, "Allow entities to spawn only inside regions if blacklisted/whitelisted?");
			setDefault(w.getName()+".spawn-blacklist", new ArrayList<>(), "" +
					"spawn-blacklist: This mobs will NOT spawn in this world!\n" +
					"\n" +
					"You can use MONSTERS or PASSIVES groups.\n" +
					"Check the entity types here:\n" +
					"https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/EntityType.html");

			setDefault(w.getName()+".spawn-whitelist", new ArrayList<>(), "" +
					"spawn-whitelist: ONLY this mobs will spawn in this world!\n" +
					"\n" +
					"You can use MONSTERS or PASSIVES groups.\n" +
					"Check the entity types here:\n" +
					"https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/EntityType.html");

			setDefault(w.getName()+".elytra.allow", true, null);
			setDefault(w.getName()+".elytra.boost", 0.5D, null);

			setDefault(w.getName()+".deny-item-usage", null, "Control what items the player can use.");
			setDefault(w.getName()+".deny-item-usage.allow-on-claimed-rps", true, null);
			setDefault(w.getName()+".deny-item-usage.allow-on-wilderness", false, null);
			setDefault(w.getName()+".deny-item-usage.items", new ArrayList<>(), null);

			setDefault(w.getName()+".player-velocity.walk-speed", -1, null);
			setDefault(w.getName()+".player-velocity.fly-speed", -1, null);

			setDefault(w.getName()+".on-enter-cmds", new ArrayList<>(), "" +
					"Execute this command on enter in this world.\n" +
					"You can use this placeholders: {world-from}, {world-to} and {player}");

			setDefault(w.getName()+".on-exit-cmds", new ArrayList<>(), "" +
					"Execute this command on exit this world.\n" +
					"You can use this placeholders: {world-from}, {world-to} and {player}");

			setDefault(w.getName()+".invincible", false, null);
			setDefault(w.getName()+".player-candrop", true, null);
			setDefault(w.getName()+".player-canpickup", true, null);
			setDefault(w.getName()+".rain.trys-before-rain", 3, null);
			setDefault(w.getName()+".rain.duration", 60, null);
			setDefault(w.getName()+".allow-crops-trample", true, null);

			setDefault(w.getName()+".command-ranges", null, "Execute commands in certain coordinate ranges.");
			if (!gflags.contains(w.getName()+".command-ranges")){
				setDefault(w.getName()+".command-ranges.home.min-range", 0, null);
				setDefault(w.getName()+".command-ranges.home.max-range", w.getMaxHeight(), null);
				setDefault(w.getName()+".command-ranges.home.message", "&cYou cant use /home when mining or in caves!", null);
			}
		}
	}

	private void setDefault(String key, Object def, String comment){
		if (def != null){
			gflags.set(key, gflags.get(key, def));
		}
 		if (comment != null){
 			setComment(key, comment);
 		}
 	}

 	private void setComment(String key, String comment){
 		comments.put(key, comment);
 	}

 	public void saveConfig(){
 		StringBuilder b = new StringBuilder();
		gflags.options().header(null);
 		
 		b.append("# +--------------------------------------------------------------------+ #\n" +
				"# <          RedProtect Global Flags configuration File                > #\n" +
				"# <--------------------------------------------------------------------> #\n" +
				"# <         This is the global flags configuration file.               > #\n" +
				"# <                       Feel free to edit it.                        > #\n" +
				"# <         https://github.com/FabioZumbi12/RedProtect/wiki            > #\n" +
				"# +--------------------------------------------------------------------+ #\n" +
				"# \n" +
				"# Notes:\n" +
				"# Lists are [object1, object2, ...]\n" +
				"# Strings containing the char & always need to be quoted").append("\n\n");
 		
 		for (String line:gflags.getKeys(true)){
 			String[] key = line.split("\\"+gflags.options().pathSeparator());
 			StringBuilder spaces = new StringBuilder();
 			for (int i = 0; i < key.length; i++){
 				if (i == 0) continue;
 				spaces.append(" ");
 			} 			
 			if (comments.containsKey(line)){
 				if (spaces.length() == 0){
 					b.append("\n# ").append(comments.get(line).replace("\n", "\n# ")).append('\n');
 				} else {
 					b.append(spaces).append("# ").append(comments.get(line).replace("\n", "\n"+spaces+"# ")).append('\n');
 				}
 			} 			
 			Object value = gflags.get(line);
 			if (!gflags.isConfigurationSection(line)){
 				if (value instanceof String){
 					b.append(spaces).append(key[key.length-1]).append(": '").append(value).append("'\n");
 				} else if (value instanceof List<?>) {
					if (((List<?>)value).isEmpty()){
						b.append(spaces).append(key[key.length-1]).append(": []\n");
					} else {
						b.append(spaces).append(key[key.length-1]).append(":\n");
						for (Object lineCfg:(List<?>)value){
							if (lineCfg instanceof String){
								b.append(spaces).append("- '").append(lineCfg).append("'\n");
							} else {
								b.append(spaces).append("- ").append(lineCfg).append("\n");
							}
						}
					}
 				} else {
 					b.append(spaces).append(key[key.length-1]).append(": ").append(value).append("\n");
 				}
 			} else {
 				b.append(spaces).append(key[key.length-1]).append(":\n");
 			}
 		}
 		
 		try {
 			Files.write(b, new File(RedProtect.get().getDataFolder(), "globalflags.yml"), Charsets.UTF_8);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
}
