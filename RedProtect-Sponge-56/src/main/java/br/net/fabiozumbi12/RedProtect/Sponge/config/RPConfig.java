package br.net.fabiozumbi12.RedProtect.Sponge.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import br.net.fabiozumbi12.RedProtect.Sponge.RPUtil;
import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;

import com.google.common.reflect.TypeToken;

public class RPConfig{
	
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
			"deny-exit-items");
	
	
	private final File defConfig = new File(RedProtect.get().configDir,"config.conf");
	private ConfigurationLoader<CommentedConfigurationNode> configManager;
	private CommentedConfigurationNode tempConfig;
	
	private final File guiConfig = new File(RedProtect.get().configDir,"guiconfig.conf");
	private ConfigurationLoader<CommentedConfigurationNode> guiManager;
	private CommentedConfigurationNode gui;
	
	private final File gFlagsConfig = new File(RedProtect.get().configDir,"globalflags.conf");
	private ConfigurationLoader<CommentedConfigurationNode> gFlagsManager;	
	private CommentedConfigurationNode gflags;
	
	private final File protFile = new File(RedProtect.get().configDir,"protections.conf");
	private ConfigurationLoader<CommentedConfigurationNode> protManager;
	private CommentedConfigurationNode protCfgs;
	
	private final File ecoFile = new File(RedProtect.get().configDir,"economy.conf");
	private ConfigurationLoader<CommentedConfigurationNode> ecoManager;
	private CommentedConfigurationNode ecoCfgs;

	private final File signFile = new File(RedProtect.get().configDir,"signs.conf");
	private ConfigurationLoader<CommentedConfigurationNode> signManager;
	private CommentedConfigurationNode signCfgs;
	
	private CommentedConfigurationNode config;
	
	//getters	
	public CommentedConfigurationNode configs(){
		return config;
	}
	
	private CommentedConfigurationNode updateFromIn(CommentedConfigurationNode temp, CommentedConfigurationNode out){
		for (Object key:temp.getChildrenMap().keySet()){          	
        	if (temp.getNode(key).hasMapChildren()){        		
        		for (Object key2:temp.getNode(key).getChildrenMap().keySet()){          			
        			if (temp.getNode(key,key2).hasMapChildren()){		        				
		        		for (Object key3:temp.getNode(key,key2).getChildrenMap().keySet()){  
		        			out.getNode(key,key2,key3).setValue(temp.getNode(key,key2,key3).getValue());
		        		}				        		
		        	}	        			
        			out.getNode(key,key2).setValue(temp.getNode(key,key2).getValue());
        		}
        	}
        	out.getNode(key).setValue(temp.getNode(key).getValue());    	            	   	            	
        }
		return out;
	}
	
	private CommentedConfigurationNode updateFromOut(CommentedConfigurationNode temp, CommentedConfigurationNode out){
		for (Object key:out.getChildrenMap().keySet()){          	
        	if (out.getNode(key).hasMapChildren()){        		
        		for (Object key2:out.getNode(key).getChildrenMap().keySet()){          			
        			if (out.getNode(key,key2).hasMapChildren()){		        				
		        		for (Object key3:out.getNode(key,key2).getChildrenMap().keySet()){  
		        			out.getNode(key,key2,key3).setValue(temp.getNode(key,key2,key3).getValue(out.getNode(key,key2,key3).getValue()));
		        		}				        		
		        	}	        			
        			out.getNode(key,key2).setValue(temp.getNode(key,key2).getValue(out.getNode(key,key2).getValue()));
        		}
        	}
        	out.getNode(key).setValue(temp.getNode(key).getValue(out.getNode(key).getValue()));    	            	   	            	
        }
		return out;
	}
	
	//init
	public RPConfig() {		
		try {			
			if (!RedProtect.get().configDir.exists()){
				RedProtect.get().configDir.mkdir();
			}
			if (!new File(RedProtect.get().configDir+File.separator+"data").exists()){
            	new File(RedProtect.get().configDir+File.separator+"data").mkdir();
            } 
			
			if (!defConfig.exists()) {
		         defConfig.createNewFile();
		         configManager = HoconConfigurationLoader.builder().setURL(RedProtect.get().container.getAsset("config.conf").get().getUrl()).build();
		         config = configManager.load();
		         configManager = HoconConfigurationLoader.builder().setFile(defConfig).build();
		         configManager.save(config);
		     }
			
		 	 if (!guiConfig.exists()) {
			 	 guiConfig.createNewFile();
				 guiManager = HoconConfigurationLoader.builder().setURL(RedProtect.get().container.getAsset("guiconfig.conf").get().getUrl()).build();
				 gui = guiManager.load();
				 guiManager = HoconConfigurationLoader.builder().setFile(guiConfig).build();
				 guiManager.save(gui);
		     }

		 	 if (!gFlagsConfig.exists()) {
		 		gFlagsConfig.createNewFile();
		     }	
		 	 
		 	 if (!ecoFile.exists()) {
		 		 Asset ecoAsset = RedProtect.get().container.getAsset("economy.conf").get();
		 		 ecoAsset.copyToDirectory(RedProtect.get().configDir.toPath());
		     }
		} catch (IOException e1) {			
			RedProtect.get().logger.severe("The default configuration could not be loaded or created!");
			e1.printStackTrace();
		}


		//load configs
		try {
			//tempconfig
			configManager = HoconConfigurationLoader.builder().setURL(RedProtect.get().container.getAsset("config.conf").get().getUrl()).build();
			tempConfig = configManager.load();

			configManager = HoconConfigurationLoader.builder().setPath(defConfig.toPath()).build();
			config = configManager.load();

			guiManager = HoconConfigurationLoader.builder().setPath(guiConfig.toPath()).build();
			gui = guiManager.load();

			gFlagsManager = HoconConfigurationLoader.builder().setPath(gFlagsConfig.toPath()).build();
			gflags = gFlagsManager.load();

			ecoManager = HoconConfigurationLoader.builder().setPath(ecoFile.toPath()).build();
			ecoCfgs = ecoManager.load();

			signManager = HoconConfigurationLoader.builder().setPath(signFile.toPath()).build();
			signCfgs = signManager.load();

			/*--------------------- protections.conf ---------------------------*/
			protManager = HoconConfigurationLoader.builder().setFile(protFile).build();
			protCfgs = protManager.load();

			protCfgs.getNode("chat-protection","chat-enhancement","enable").setValue(protCfgs.getNode("chat-protection","chat-enhancement","enable").getBoolean(true));
			protCfgs.getNode("chat-protection","chat-enhancement","end-with-dot").setValue(protCfgs.getNode("chat-protection","chat-enhancement","end-with-dot").getBoolean(true));
			protCfgs.getNode("chat-protection","chat-enhancement","minimum-lenght").setValue(protCfgs.getNode("chat-protection","chat-enhancement","minimum-lenght").getInt(3));

			protCfgs.getNode("chat-protection","anti-flood","enable").setValue(protCfgs.getNode("chat-protection","anti-flood","enable").getBoolean(true));
			protCfgs.getNode("chat-protection","anti-flood","whitelist-flood-characs")
					.setValue(protCfgs.getNode("chat-protection","anti-flood","whitelist-flood-characs").getList(TypeToken.of(String.class), Collections.singletonList("k")));

			protCfgs.getNode("chat-protection","caps-filter","enable").setValue(protCfgs.getNode("chat-protection","caps-filter","enable").getBoolean(true));
			protCfgs.getNode("chat-protection","caps-filter","minimum-lenght").setValue(protCfgs.getNode("chat-protection","caps-filter","minimum-lenght").getInt(3));

			protCfgs.getNode("chat-protection","antispam","enable").setValue(protCfgs.getNode("chat-protection","antispam","enable").getBoolean(false));
			protCfgs.getNode("chat-protection","antispam","time-beteween-messages").setValue(protCfgs.getNode("chat-protection","antispam","time-beteween-messages").getInt(1));
			protCfgs.getNode("chat-protection","antispam","count-of-same-message").setValue(protCfgs.getNode("chat-protection","antispam","count-of-same-message").getInt(5));
			protCfgs.getNode("chat-protection","antispam","time-beteween-same-messages").setValue(protCfgs.getNode("chat-protection","antispam","time-beteween-same-messages").getInt(10));
			protCfgs.getNode("chat-protection","antispam","colldown-msg").setValue(protCfgs.getNode("chat-protection","antispam","colldown-msg").getString("&6Slow down your messages!"));
			protCfgs.getNode("chat-protection","antispam","wait-message").setValue(protCfgs.getNode("chat-protection","antispam","wait-message").getString("&cWait to send the same message again!"));
			protCfgs.getNode("chat-protection","antispam","cmd-action").setValue(protCfgs.getNode("chat-protection","antispam","cmd-action").getString("kick {player} Relax, slow down your messages frequency ;)"));

			protCfgs.getNode("chat-protection","censor","enable").setValue(protCfgs.getNode("chat-protection","censor","enable").getBoolean(true));
			protCfgs.getNode("chat-protection","censor","replace-by-symbol").setValue(protCfgs.getNode("chat-protection","censor","replace-by-symbol").getBoolean(true));
			protCfgs.getNode("chat-protection","censor","by-symbol").setValue(protCfgs.getNode("chat-protection","censor","by-symbol").getString("*"));
			protCfgs.getNode("chat-protection","censor","by-word").setValue(protCfgs.getNode("chat-protection","censor","by-word").getString("censored"));
			protCfgs.getNode("chat-protection","censor","replace-partial-word").setValue(protCfgs.getNode("chat-protection","censor","replace-partial-word").getBoolean(false));
			protCfgs.getNode("chat-protection","censor","action","cmd").setValue(protCfgs.getNode("chat-protection","censor","action","cmd").getString(""));
			protCfgs.getNode("chat-protection","censor","action","partial-words").setValue(protCfgs.getNode("chat-protection","censor","action","partial-words").getBoolean(false));
			protCfgs.getNode("chat-protection","censor","replace-words")
					.setValue(protCfgs.getNode("chat-protection","censor","replace-words").getList(TypeToken.of(String.class), Collections.singletonList("word")));

			protCfgs.getNode("chat-protection","anti-ip","enable").setValue(protCfgs.getNode("chat-protection","anti-ip","enable").getBoolean(true));
			protCfgs.getNode("chat-protection","anti-ip","custom-ip-regex").setValue(protCfgs.getNode("chat-protection","anti-ip","custom-ip-regex").getString("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"));
			protCfgs.getNode("chat-protection","anti-ip","custom-url-regex").setValue(protCfgs.getNode("chat-protection","anti-ip","custom-url-regex").getString("((http:\\/\\/|https:\\/\\/)?(www.)?(([a-zA-Z0-9-]){2,}\\.){1,4}([a-zA-Z]){2,6}(\\/([a-zA-Z-_\\/\\.0-9#:?=&;,]*)?)?)"));
			protCfgs.getNode("chat-protection","anti-ip","check-for-words")
					.setValue(protCfgs.getNode("chat-protection","anti-ip","check-for-words").getList(TypeToken.of(String.class), Collections.singletonList("www.google.com")));
			protCfgs.getNode("chat-protection","anti-ip","whitelist-words")
					.setValue(protCfgs.getNode("chat-protection","anti-ip","whitelist-words").getList(TypeToken.of(String.class), Arrays.asList("www.myserver.com","prntscr.com","gyazo.com","www.youtube.com")));
			protCfgs.getNode("chat-protection","anti-ip","cancel-or-replace").setValue(protCfgs.getNode("chat-protection","anti-ip","cancel-or-replace").getString("cancel"));
			protCfgs.getNode("chat-protection","anti-ip","cancel-msg").setValue(protCfgs.getNode("chat-protection","anti-ip","cancel-msg").getString("&cYou cant send websites or ips on chat"));
			protCfgs.getNode("chat-protection","anti-ip","replace-by-word").setValue(protCfgs.getNode("chat-protection","anti-ip","replace-by-word").getString("-removed-"));
			protCfgs.getNode("chat-protection","anti-ip","punish","enable").setValue(protCfgs.getNode("chat-protection","anti-ip","punish","enable").getBoolean(false));
			protCfgs.getNode("chat-protection","anti-ip","punish","max-attempts").setValue(protCfgs.getNode("chat-protection","anti-ip","punish","max-attempts").getInt(3));
			protCfgs.getNode("chat-protection","anti-ip","punish","mute-or-cmd").setValue(protCfgs.getNode("chat-protection","anti-ip","punish","mute-or-cmd").getString("mute"));
			protCfgs.getNode("chat-protection","anti-ip","punish","mute-duration").setValue(protCfgs.getNode("chat-protection","anti-ip","punish","mute-duration").getInt(1));
			protCfgs.getNode("chat-protection","anti-ip","punish","mute-msg").setValue(protCfgs.getNode("chat-protection","anti-ip","punish","mute-msg").getString("&cYou have been muted for send IPs or URLs on chat!"));
			protCfgs.getNode("chat-protection","anti-ip","punish","unmute-msg").setValue(protCfgs.getNode("chat-protection","anti-ip","punish","unmute-msg").getString("&aYou can chat again!"));
			protCfgs.getNode("chat-protection","anti-ip","punish","cmd-punish").setValue(protCfgs.getNode("chat-protection","anti-ip","punish","cmd-punish").getString("tempban {player} 10m &cYou have been warned about send links or IPs on chat!"));


		} catch (IOException | ObjectMappingException e1) {
			RedProtect.get().logger.severe("The default configuration could not be loaded or created!");
			e1.printStackTrace();
		}


		//------------------------------ Add default Values ----------------------------//

		config = updateFromIn(tempConfig, config);

		try {
			configManager = HoconConfigurationLoader.builder().setPath(defConfig.toPath()).build();
			tempConfig = configManager.load();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		config = updateFromOut(tempConfig, config);



		//--------------------------------------------------------------------------//


		RedProtect.get().logger.info("Server version: " + RedProtect.get().game.getPlatform().getMinecraftVersion().getName());

		//add allowed claim worlds to config
		try {
			if (getNodes("allowed-claim-worlds").getList(TypeToken.of(String.class)).isEmpty()) {
				List<String> worlds = new ArrayList<>();
				for (World w:RedProtect.get().serv.getWorlds()){
					worlds.add(w.getName());
					RedProtect.get().logger.warning("Added world to claim list " + w.getName());
				}
				worlds.remove("example_world");
				getNodes("allowed-claim-worlds").setValue(worlds);
			}
		} catch (ObjectMappingException e) {
			e.printStackTrace();
		}

		/*------------- ---- Add default config for not updateable configs ------------------*/

		//update new player flags according version
		List<String> flags = new LinkedList<>(Collections.emptyList());
		try {
			flags = getNodes("flags-configuration.enabled-flags").getList(TypeToken.of(String.class));
		} catch (ObjectMappingException e) {
			e.printStackTrace();
		}
		int update = 0;
		if (getNodes("config-version").getDouble() < 6.8D){
			getNodes("config-version").setValue(6.8D);

			if (!flags.contains("smart-door")){
				flags.add("smart-door");
				getNodes("flags.smart-door").setValue(true);
			}
			if (!flags.contains("allow-potions")){
				flags.add("allow-potions");
				getNodes("flags.allow-potions").setValue(true);
			}
			if (!flags.contains("mob-loot")){
				flags.add("mob-loot");
				getNodes("flags.mob-loot").setValue(false);
			}
			if (!flags.contains("flow-damage")){
				flags.add("flow-damage");
				getNodes("flags.flow-damage").setValue(false);
			}
			update++;
		}

		if (getNodes("config-version").getDouble() < 6.9D){
			getNodes("config-version").setValue(6.9D);
			if (!flags.contains("allow-fly")){
				flags.add("allow-fly");
				getNodes("flags.allow-fly").setValue(true);
			}
			if (!flags.contains("can-grow")){
				flags.add("can-grow");
				getNodes("flags.can-grow").setValue(true);
			}
			if (!flags.contains("teleport")){
				flags.add("teleport");
				getNodes("flags.teleport").setValue(false);
			}
			update++;
		}

		if (getNodes("config-version").getDouble() < 7.0D){
			getNodes("config-version").setValue(7.0D);
			if (!flags.contains("allow-effects")){
				flags.add("allow-effects");
				getNodes("flags.allow-effects").setValue(true);
			}
			if (!flags.contains("use-potions")){
				flags.add("use-potions");
				getNodes("flags.use-potions").setValue(true);
			}
			if (flags.contains("allow-potions")){
				flags.remove("allow-potions");
				getNodes("flags").removeChild("allow-potions");
			}
			update++;
		}

		if (getNodes("config-version").getDouble() < 7.1D){
			getNodes("config-version").setValue(7.1D);
			getNodes("language").setValue("en-EN");
			update++;
		}

		if (getNodes("config-version").getDouble() < 7.2D){
			getNodes("config-version").setValue(7.2D);
			if (!flags.contains("allow-spawner")){
				flags.add("allow-spawner");
				getNodes("flags.allow-spawner").setValue(false);
			}
			if (!flags.contains("leaves-decay")){
				flags.add("leaves-decay");
				getNodes("flags.leaves-decay").setValue(false);
			}
			update++;
		}

		if (getNodes("config-version").getDouble() < 7.3D){
			getNodes("config-version").setValue(7.3D);
			if (!flags.contains("build")){
				flags.add("build");
				getNodes("flags.build").setValue(false);
			}
			update++;
		}

		if (getNodes("config-version").getDouble() < 7.4D){
			getNodes("config-version").setValue(7.4D);
			try {
				List<String> blocks = getNodes("private.allowed-blocks").getList(TypeToken.of(String.class));
				blocks.add("minecraft:[a-z_]+_shulker_box");
				getNodes("private.allowed-blocks").setValue(blocks);
			} catch (ObjectMappingException e) {
				e.printStackTrace();
			}

			update++;
		}

		if (update > 0){
			getNodes("flags-configuration.enabled-flags").setValue(flags);
			RedProtect.get().logger.warning("Configuration UPDATED!");
		}
		/*---------------------------------------- Global Flags for worlds loaded --------------------------------------------*/

		for (World w:Sponge.getServer().getWorlds()){
			this.loadPerWorlds(w);
		}

		/*------------------------------------------ Gui Items ------------------------------------------*/

		gui.getNode("gui-strings","value").setValue(gui.getNode("gui-strings","value").getString("&bValue: "));
		gui.getNode("gui-strings","true").setValue(gui.getNode("gui-strings","true").getString("&atrue"));
		gui.getNode("gui-strings","false").setValue(gui.getNode("gui-strings","false").getString("&cfalse"));
		gui.getNode("gui-strings","separator").setValue(gui.getNode("gui-strings","separator").getString("&7|"));

		gui.getNode("gui-separator","material").setValue(gui.getNode("gui-separator","material").getString("stained_glass_pane"));
		gui.getNode("gui-separator","data").setValue(gui.getNode("gui-separator","data").getInt(0));

		for (String key:getDefFlagsValues().keySet()){
			gui.getNode("gui-flags",key,"slot").setValue(gui.getNode("gui-flags",key,"slot").setValue(gui.getNode("gui-flags",key,"slot").getInt(getDefFlagsValues().size())));
			gui.getNode("gui-flags",key,"material").setValue(gui.getNode("gui-flags",key,"material").setValue(gui.getNode("gui-flags",key,"material").getString("golden_apple")));
			gui.getNode("gui-flags",key,"name").setValue(gui.getNode("gui-flags",key,"name").setValue(gui.getNode("gui-flags",key,"name").getString("&e"+key)));
			gui.getNode("gui-flags",key,"description").setValue(gui.getNode("gui-flags",key,"description").setValue(gui.getNode("gui-flags",key,"description").getString("&bDescription: &2Add a flag description here.")));
			gui.getNode("gui-flags",key,"description1").setValue(gui.getNode("gui-flags",key,"description1").setValue(gui.getNode("gui-flags",key,"description1").getString("")));
			gui.getNode("gui-flags",key,"description2").setValue(gui.getNode("gui-flags",key,"description2").setValue(gui.getNode("gui-flags",key,"description2").getString("")));
		}

		List<String> names = new ArrayList<>();
		Sponge.getRegistry().getAllOf(BlockType.class).forEach((type)-> names.add(type.getName()));

		Sponge.getRegistry().getAllOf(ItemType.class).forEach((type)->{
			if (!names.contains(type.getName()))
				names.add(type.getName());
		});

		if (names.size() != ecoCfgs.getNode("items","values").getChildrenList().size()){
			for (String mat:names){
				if (ecoCfgs.getNode("items","values",mat).getValue() == null){
					ecoCfgs.getNode("items","values",mat).setValue(0.0);
				}
			}
		}

		if (RedProtect.get().getPVHelper().getAllEnchants().size() != ecoCfgs.getNode("enchantments","values").getChildrenList().size()){
			for (String ench:RedProtect.get().getPVHelper().getAllEnchants()){
				if (ecoCfgs.getNode("enchantments","values",ench).getValue() == null){
					ecoCfgs.getNode("enchantments","values",ench).setValue(0.0);
				}
			}
		}

		//////////////////////

		//create logs folder
		File logs = new File(RedProtect.get().configDir+File.separator+"logs");
		if(getBool("log-actions") && !logs.exists()){
			logs.mkdir();
			RedProtect.get().logger.info("Created folder: " + RedProtect.get().configDir+File.separator+"logs");
		}

		save();
		RedProtect.get().logger.info("All configurations loaded!");

	}
    
	public void loadPerWorlds(World w) {		
		if (getNodes("region-settings.claim-types.worlds."+w.getName()).getString("").equals("")) {
    		getNodes("region-settings.claim-types.worlds."+w.getName()).setValue("BLOCK");
    	}
		
		if (getNodes("region-settings.world-colors."+w.getName()).getString("").equals("")) {
			if (w.getDimension().getType().equals(DimensionTypes.OVERWORLD)){
				getNodes("region-settings.world-colors."+w.getName()).setValue("&a&l");			            		
			} else
			if (w.getDimension().getType().equals(DimensionTypes.NETHER)){
				getNodes("region-settings.world-colors."+w.getName()).setValue("&c&l");			            		
			} else
			if (w.getDimension().getType().equals(DimensionTypes.THE_END)){
				getNodes("region-settings.world-colors."+w.getName()).setValue("&5&l");			            		
			}
			RedProtect.get().logger.warning("Added world to color list " + w.getName());
		}
		
		try {
			//RedProtect.get().logger.debug("default","Writing global flags for world "+ w.getName() + "...");
			gflags.getNode(w.getName(),"build").setValue(gflags.getNode(w.getName(),"build").getBoolean(true));
        	gflags.getNode(w.getName(),"if-build-false","break-blocks").setValue(gflags.getNode(w.getName(),"if-build-false","break-blocks").getList(TypeToken.of(String.class)));
        	gflags.getNode(w.getName(),"if-build-false","place-blocks").setValue(gflags.getNode(w.getName(),"if-build-false","place-blocks").getList(TypeToken.of(String.class)));
        	gflags.getNode(w.getName(),"pvp").setValue(gflags.getNode(w.getName(),"pvp").getBoolean(false));
        	gflags.getNode(w.getName(),"interact").setValue(gflags.getNode(w.getName(),"interact").getBoolean(true));
        	gflags.getNode(w.getName(),"use-minecart").setValue(gflags.getNode(w.getName(),"use-minecart").getBoolean(true));
        	gflags.getNode(w.getName(),"entity-block-damage").setValue(gflags.getNode(w.getName(),"entity-block-damage").getBoolean(false));
        	gflags.getNode(w.getName(),"explosion-entity-damage").setValue(gflags.getNode(w.getName(),"explosion-entity-damage").getBoolean(true));
        	gflags.getNode(w.getName(),"fire-block-damage").setValue(gflags.getNode(w.getName(),"fire-block-damage").getBoolean(false));
        	gflags.getNode(w.getName(),"fire-spread").setValue(gflags.getNode(w.getName(),"fire-spread").getBoolean(false));
        	gflags.getNode(w.getName(),"player-hurt-monsters").setValue(gflags.getNode(w.getName(),"player-hurt-monsters").getBoolean(true));
        	gflags.getNode(w.getName(),"player-hurt-passives").setValue(gflags.getNode(w.getName(),"player-hurt-passives").getBoolean(true));
        	gflags.getNode(w.getName(),"spawn-monsters").setValue(gflags.getNode(w.getName(),"spawn-monsters").getBoolean(true));
        	gflags.getNode(w.getName(),"spawn-passives").setValue(gflags.getNode(w.getName(),"spawn-passives").getBoolean(true));
        	gflags.getNode(w.getName(),"remove-entities-not-allowed-to-spawn").setValue(gflags.getNode(w.getName(),"remove-entities-not-allowed-to-spawn").getBoolean(false));
        	gflags.getNode(w.getName(),"allow-weather").setValue(gflags.getNode(w.getName(),"allow-weather").getBoolean(true));        	
        	gflags.getNode(w.getName(),"deny-item-usage","allow-on-claimed-rps").setValue(gflags.getNode(w.getName(),"deny-item-usage","allow-on-claimed-rps").getBoolean(true));
        	gflags.getNode(w.getName(),"deny-item-usage","allow-on-wilderness").setValue(gflags.getNode(w.getName(),"deny-item-usage","allow-on-wilderness").getBoolean(true));
        	gflags.getNode(w.getName(),"deny-item-usage","items").setValue(gflags.getNode(w.getName(),"deny-item-usage","items").getList(TypeToken.of(String.class)));  
        	gflags.getNode(w.getName(),"on-enter-cmds").setValue(gflags.getNode(w.getName(),"on-enter-cmds").getList(TypeToken.of(String.class)));  
        	gflags.getNode(w.getName(),"on-exit-cmds").setValue(gflags.getNode(w.getName(),"on-exit-cmds").getList(TypeToken.of(String.class)));
        	gflags.getNode(w.getName(),"allow-changes-of","water-flow").setValue(gflags.getNode(w.getName(),"allow-changes-of","water-flow").getBoolean(true));  
        	gflags.getNode(w.getName(),"allow-changes-of","lava-flow").setValue(gflags.getNode(w.getName(),"allow-changes-of","lava-flow").getBoolean(true)); 
        	gflags.getNode(w.getName(),"allow-changes-of","leaves-decay").setValue(gflags.getNode(w.getName(),"allow-changes-of","leaves-decay").getBoolean(true)); 
        	gflags.getNode(w.getName(),"allow-changes-of","flow-damage").setValue(gflags.getNode(w.getName(),"allow-changes-of","flow-damage").getBoolean(true));         	
        	gflags.getNode(w.getName(),"spawn-wither").setValue(gflags.getNode(w.getName(),"spawn-wither").getBoolean(true)); 
        	gflags.getNode(w.getName(),"invincible").setValue(gflags.getNode(w.getName(),"invincible").getBoolean(false)); 
        	gflags.getNode(w.getName(),"player-candrop").setValue(gflags.getNode(w.getName(),"player-candrop").getBoolean(true)); 
        	gflags.getNode(w.getName(),"player-canpickup").setValue(gflags.getNode(w.getName(),"player-canpickup").getBoolean(true));
			gflags.getNode(w.getName(),"blocks-spawn-items").setValue(gflags.getNode(w.getName(),"blocks-spawn-items").getBoolean(true));
			gflags.getNode(w.getName(),"liquid-flow").setValue(gflags.getNode(w.getName(),"liquid-flow").getBoolean(true));
			if (!gflags.getNode(w.getName(),"command-ranges").hasMapChildren()){
        		gflags.getNode(w.getName(),"command-ranges","home","min-range").setValue(gflags.getNode(w.getName(),"command-ranges","home","min-range").getDouble(0));  
        		gflags.getNode(w.getName(),"command-ranges","home","max-range").setValue(gflags.getNode(w.getName(),"command-ranges","home","max-range").getDouble(w.getBlockMax().getY()));  
        		gflags.getNode(w.getName(),"command-ranges","home","message").setValue(gflags.getNode(w.getName(),"command-ranges","home","message").getString("&cYou cant use /home when mining or in caves!"));        		
        	}      	
            //write gflags to gflags file
            gFlagsManager.save(gflags);
		} catch (IOException | ObjectMappingException e) {
			e.printStackTrace();
		} 
	}
	
    public Boolean getGlobalFlag(String world, String action){		
		return this.gflags.getNode(world,action).getBoolean();
	}
    
    public Boolean getGlobalFlag(Object... key){
		return this.gflags.getNode(key).getBoolean();
	}

    public ItemStack getGuiItemStack(String key){
    	RedProtect.get().logger.debug("default","Gui Material to get: " + key);
    	RedProtect.get().logger.debug("default","Result: " + gui.getNode("gui-flags",key,"material").getString());
    	return ItemStack.of((ItemType)RPUtil.getRegistryFor(ItemType.class, gui.getNode("gui-flags",key,"material").getString()), 1);
    }
    
    public Text getGuiFlagString(String flag, String option){    	
    	//RedProtect.get().logger.severe("Flag: "+flag+" - FlagString: "+this.gui.getNode("gui-flags",flag,option).getString());
    	if (this.gui.getNode("gui-flags",flag,option).getString() == null){
    		return Text.of();
    	}    	
    	return RPUtil.toText(gui.getNode("gui-flags",flag,option).getString());
    }
    
    public Text getGuiString(String string) {
		return RPUtil.toText(gui.getNode("gui-strings",string).getString());
	}
    
    public int getGuiSlot(String flag) {
		return this.gui.getNode("gui-flags",flag,"slot").getInt();
	}
    
    public void setGuiSlot(/*String mat, */String flag, int slot) {
    	this.gui.getNode("gui-flags",flag,"slot").setValue(slot);
		//GuiItems.set("gui-flags."+flag+".material", mat);
		
	}
    
    public ItemStack getGuiSeparator() {
    	ItemStack separator = ItemStack.of((ItemType)RPUtil.getRegistryFor(ItemType.class, gui.getNode("gui-separator","material").getString()), 1);//new ItemStack(Material.getMaterial(guiItems.getString("gui-separator.material")), 1, (short)guiItems.getInt("gui-separator.data"));
    	separator.offer(Keys.DISPLAY_NAME, getGuiString("separator"));
    	separator.offer(Keys.ITEM_DURABILITY, gui.getNode("gui-separator","data").getInt());
    	separator.offer(Keys.ITEM_LORE, Arrays.asList(Text.EMPTY, getGuiString("separator")));
		return separator;
	}
    
    public int getGuiMaxSlot() {
    	SortedSet<Integer> slots = new TreeSet<>(new ArrayList<>());
    	for (CommentedConfigurationNode key:gui.getNode("gui-flags").getChildrenMap().values()){
    		for (Object key1:key.getChildrenMap().keySet()){    			
        		if (key1.toString().contains("slot")){
        			slots.add(key.getChildrenMap().get(key1).getInt());
        			//RedProtect.get().logger.severe("Key: "+key.getChildrenMap().get(key1).getInt());
        		}    		
        	}    		
    	}
		return Collections.max(slots);
	}
    
    public Boolean getBool(String key){
		return getNodes(key).getBoolean(false);
	}
    
    public void setConfig(String key, Object value){
    	getNodes(key).setValue(value);
    }
    
    public HashMap<String, Object> getDefFlagsValues(){
    	HashMap<String,Object> flags = new HashMap<>();
    	for (Object oflag:getNodes("flags").getChildrenMap().keySet()/*getList(TypeToken.of(String.class)*/){
    		if (oflag instanceof String && isFlagEnabled(((String)oflag).replace("flags.", ""))){
    			String flag = (String)oflag;
    			try {
					if (flag.equals("pvp") && !getNodes("flags-configuration.enabled-flags").getList(TypeToken.of(String.class)).contains("pvp")){
						continue;
					}
				} catch (ObjectMappingException e) {
					e.printStackTrace();
				}
    			flags.put(flag, getNodes("flags."+flag).getValue());
    		}
    	}    	
		return flags;
	}
    
    public boolean isFlagEnabled(String flag){    	
    	try {
			return getNodes("flags-configuration.enabled-flags").getList(TypeToken.of(String.class)).contains(flag) || AdminFlags.contains(flag);
		} catch (ObjectMappingException e) {
			e.printStackTrace();
		}
    	return false;
    }
    
    public SortedSet<String> getDefFlags(){
        return new TreeSet<>(getDefFlagsValues().keySet());
    }
    
    public String getString(String key){
		return getNodes(key).getString();
	}
    
    public Integer getInt(String key){	
		return getNodes(key).getInt();
	}
    
    public Object getObject(String key) {		
		return getNodes(key).getValue();
	}
    
    private CommentedConfigurationNode getNodes(String key){    	
    	Object[] args = key.split("\\.");
    	return config.getNode(args);
    }
    
    public List<String> getStringList(String key){
		try {
			return getNodes(key).getList(TypeToken.of(String.class));
		} catch (ObjectMappingException e) {
			e.printStackTrace();
		}
		return null;
	}
    
    public BlockType getMaterial(String key){
    	return BlockTypes.GLOWSTONE;
    }
    
    public void save(){
    	try {
			configManager.save(config);	
			gFlagsManager.save(gflags);
			ecoManager.save(ecoCfgs);
			protManager.save(protCfgs);
			signManager.save(signCfgs);
			saveGui();
		} catch (IOException e) {
			RedProtect.get().logger.severe("Problems during save file:");
			e.printStackTrace();
		}
    }
    
    public void saveGui(){ 
    	try {
    		guiManager.save(gui);
		} catch (IOException e) {
			RedProtect.get().logger.severe("Problems during save gui file:");
			e.printStackTrace();
		}
    }    
    	
    public boolean isAllowedWorld(Player p) {
		try {
			return getNodes("allowed-claim-worlds").getList(TypeToken.of(String.class)).contains(p.getWorld().getName()) || p.hasPermission("redprotect.admin");
		} catch (ObjectMappingException e) {
			e.printStackTrace();
		}
		return false;
	}

	public SortedSet<String> getAllFlags() {
		SortedSet<String> values = new TreeSet<>(getDefFlagsValues().keySet());
		values.addAll(new TreeSet<>(AdminFlags));
		return values;
	}

	public boolean addFlag(String flag, boolean defaultValue, boolean isAdmin){
		if (isAdmin){
			if (!AdminFlags.contains(flag)){
				AdminFlags.add(flag);
				return true;
			}
		} else {
			if (config.getNode("flags",flag).getValue() == null){
				config.getNode("flags",flag).setValue(defaultValue);
                try {
                    List<String> flags = config.getNode("flags-configuration","enabled-flags").getList(TypeToken.of(String.class));
                    flags.add(flag);
                    config.getNode("flags-configuration","enabled-flags").setValue(flags);
                    configManager.save(config);
                } catch (ObjectMappingException | IOException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
			}
		}
		return false;
	}

	public int getBlockCost(String itemName) {
		return ecoCfgs.getNode("items","values",itemName).getInt();
	}
	
	public int getEnchantCost(String enchantment) {
		return ecoCfgs.getNode("enchantments","values",enchantment).getInt();
	}
	

	public boolean hasGlobalKey(Object... path){
		return gflags.getNode(path).hasMapChildren();
	}
	
	public String getGlobalFlagString(Object... string) {		
		return gflags.getNode(string).getString();
	}
	
	public double getGlobalFlagDouble(Object... key){		
		return gflags.getNode(key).getDouble();
	}
	
	public float getGlobalFlagFloat(Object... key){			
		return gflags.getNode(key).getFloat();
	}
	
	public int getGlobalFlagInt(Object... key){		
		return gflags.getNode(key).getInt();
	}
	
    public Boolean getGlobalFlagBool(Object... key){		
		return gflags.getNode(key).getBoolean();
	}
    
    public List<String> getGlobalFlagList(Object... key){
		try {
			return gflags.getNode(key).getList(TypeToken.of(String.class));
		} catch (ObjectMappingException e) {
			e.printStackTrace();
		}
		return new ArrayList<>();
	}
    
	public String getEcoString(String key){
		return ecoCfgs.getNode(key).getString("&4Missing economy string for &c"+key);
	}
	
	public Integer getEcoInt(String key){
		return ecoCfgs.getNode(key).getInt();
	}

	public boolean getEcoBool(String key) {
		return ecoCfgs.getNode(key).getBoolean();
	}

	public String getWorldClaimType(String w) {		
		return getNodes("region-settings.claim-types.worlds."+w).getString("");
	}
	
	public boolean needClaimToBuild(Player p, BlockSnapshot b) {     	
    	boolean bool = getStringList("needed-claim-to-build.worlds").contains(p.getWorld().getName());    	
    	if (bool){
    		if (b != null && getBool("needed-claim-to-build.allow-only-protections-blocks") &&
					(getWorldClaimType(p.getWorld().getName()).equalsIgnoreCase("BLOCK") ||
							getWorldClaimType(p.getWorld().getName()).equalsIgnoreCase("BOTH"))){
    			boolean blocks = b.getState().getName().contains(getString("region-settings.block-id")) || b.getState().getName().contains("SIGN") ||
                        getStringList("needed-claim-to-build.allow-break-blocks").stream().anyMatch(str -> str.equalsIgnoreCase(b.getState().getName()));
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
	
	//protection methods		
	public int getProtInt(Object... key){
		return protCfgs.getNode(key).getInt();
	}
	
	public boolean getProtBool(Object... key){
		return protCfgs.getNode(key).getBoolean();
	}
	
	public List<String> getProtStringList(Object... key){
		try {
			return protCfgs.getNode(key).getList(TypeToken.of(String.class), new ArrayList<>());
		} catch (ObjectMappingException e) {
			e.printStackTrace();
		}
		return new ArrayList<>();
	}
	        
	public String getProtString(Object... key){
		return protCfgs.getNode(key).getString();
	}
	
	public Text getProtMsg(Object... key){
		return RPUtil.toText(protCfgs.getNode(key).getString());
	}
	
	public Text getURLTemplate() {
		return RPUtil.toText(protCfgs.getNode("general","URL-template").getString());
	}


	public List<Location> getSigns(String rid){
		List<Location> locs = new ArrayList<>();
		try {
			for (String s:signCfgs.getNode(rid).getList(TypeToken.of(String.class))){
				String[] val = s.split(",");
				if (!Sponge.getServer().getWorld(val[0]).isPresent()){
					continue;
				}
				locs.add(new Location<>(Sponge.getServer().getWorld(val[0]).get(),Double.valueOf(val[1]),Double.valueOf(val[2]),Double.valueOf(val[3])));
			}
		} catch (ObjectMappingException e) {
			e.printStackTrace();
		}
		return locs;
	}

	public void putSign(String rid, Location<World> loc){
		try {
			List<String> lsigns = signCfgs.getNode(rid).getList(TypeToken.of(String.class));
			String locs = loc.getExtent().getName()+","+loc.getX()+","+loc.getY()+","+loc.getZ();
			if (!lsigns.contains(locs)){
				lsigns.add(locs);
				saveSigns(rid, lsigns);
			}
		} catch (ObjectMappingException e) {
			e.printStackTrace();
		}
	}

	public void removeSign(String rid, Location<World> loc){
		try {
			List<String> lsigns = signCfgs.getNode(rid).getList(TypeToken.of(String.class));
			String locs = loc.getExtent().getName()+","+loc.getX()+","+loc.getY()+","+loc.getZ();
			if (lsigns.contains(locs)){
				lsigns.remove(locs);
				saveSigns(rid, lsigns);
			}
		} catch (ObjectMappingException e) {
			e.printStackTrace();
		}
	}

	private void saveSigns(String rid, List<String> locs){
		if (locs.isEmpty()){
			signCfgs.getNode(rid).setValue(null);
		} else {
			signCfgs.getNode(rid).setValue(locs);
		}
		try {
			signManager.save(signCfgs);
		} catch (IOException e) {
			RedProtect.get().logger.severe("Problems during save file:");
			e.printStackTrace();
		}
	}
}
   
