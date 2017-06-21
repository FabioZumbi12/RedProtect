package br.net.fabiozumbi12.redprotect.config;

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
import org.spongepowered.api.item.Enchantment;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.api.world.World;

import br.net.fabiozumbi12.redprotect.RPUtil;
import br.net.fabiozumbi12.redprotect.RedProtect;

import com.google.common.reflect.TypeToken;

public class RPConfig{
	
	public List<String> AdminFlags = Arrays.asList(
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
			"for-sale");	
	
	
	private File defConfig = new File(RedProtect.configDir+"config.conf");
	private ConfigurationLoader<CommentedConfigurationNode> configManager;
	private CommentedConfigurationNode tempConfig;
	
	private File guiConfig = new File(RedProtect.configDir+"guiconfig.conf");
	private ConfigurationLoader<CommentedConfigurationNode> guiManager;
	private CommentedConfigurationNode gui;
	
	private File gFlagsConfig = new File(RedProtect.configDir+"globalflags.conf");	
	private ConfigurationLoader<CommentedConfigurationNode> gFlagsManager;	
	private CommentedConfigurationNode gflags;
	
	private File ecoFile = new File(RedProtect.configDir+"economy.conf");
	private ConfigurationLoader<CommentedConfigurationNode> ecoManager;
	private CommentedConfigurationNode ecoCfgs;
	
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
		        			continue;
		        		}				        		
		        	}	        			
        			out.getNode(key,key2).setValue(temp.getNode(key,key2).getValue());  
        			continue;
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
		        			continue;
		        		}				        		
		        	}	        			
        			out.getNode(key,key2).setValue(temp.getNode(key,key2).getValue(out.getNode(key,key2).getValue()));  
        			continue;
        		}
        	}
        	out.getNode(key).setValue(temp.getNode(key).getValue(out.getNode(key).getValue()));    	            	   	            	
        }
		return out;
	}
	
	//init
	public RPConfig() {		
		try {			
			if (!new File(RedProtect.configDir).exists()){
				new File(RedProtect.configDir).mkdir();
			}
			if (!new File(RedProtect.configDir+"data").exists()){
            	new File(RedProtect.configDir+"data").mkdir();
            } 
			
			if (!defConfig.exists()) {
		         defConfig.createNewFile();
		         configManager = HoconConfigurationLoader.builder().setURL(RedProtect.plugin.getAsset("config.conf").get().getUrl()).build();
		         config = configManager.load();
		         configManager = HoconConfigurationLoader.builder().setFile(defConfig).build();
		         configManager.save(config);
		     }
			
		 	 if (!guiConfig.exists()) {
			 	 guiConfig.createNewFile();
				 guiManager = HoconConfigurationLoader.builder().setURL(RedProtect.plugin.getAsset("guiconfig.conf").get().getUrl()).build();
				 gui = guiManager.load();
				 guiManager = HoconConfigurationLoader.builder().setFile(guiConfig).build();
				 guiManager.save(gui);
		     }
		 	 
		 	 if (!gFlagsConfig.exists()) {
		 		gFlagsConfig.createNewFile();
		     }	
		 	 
		 	 if (!ecoFile.exists()) {
		 		 Asset ecoAsset = RedProtect.plugin.getAsset("economy.conf").get();
		 		 ecoAsset.copyToDirectory(new File(RedProtect.configDir).toPath());
		     }
		 	 
		} catch (IOException e1) {			
			RedProtect.logger.severe("The default configuration could not be loaded or created!");
			e1.printStackTrace();
		}
		
		
		        //load configs
		        try {
		        	//tempconfig
		        	configManager = HoconConfigurationLoader.builder().setURL(RedProtect.plugin.getAsset("config.conf").get().getUrl()).build();
		        	tempConfig = configManager.load();
		        	
		        	configManager = HoconConfigurationLoader.builder().setPath(defConfig.toPath()).build();
		        	config = configManager.load();
					
					guiManager = HoconConfigurationLoader.builder().setPath(guiConfig.toPath()).build();
					gui = guiManager.load();
					
					gFlagsManager = HoconConfigurationLoader.builder().setPath(gFlagsConfig.toPath()).build();
					gflags = gFlagsManager.load();
					
					ecoManager = HoconConfigurationLoader.builder().setPath(ecoFile.toPath()).build();
					ecoCfgs = ecoManager.load();
					
				} catch (IOException e1) {
					RedProtect.logger.severe("The default configuration could not be loaded or created!");
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
                    
				
                    RedProtect.logger.info("Server version: " + RedProtect.game.getPlatform().getMinecraftVersion().getName());
                    
    	            //add allowed claim worlds to config
    	            try {
						if (getNodes("allowed-claim-worlds").getList(TypeToken.of(String.class)).isEmpty()) {
							List<String> worlds = new ArrayList<String>();
							for (World w:RedProtect.serv.getWorlds()){
								worlds.add(w.getName());
								RedProtect.logger.warning("Added world to claim list " + w.getName());
							}
							worlds.remove("example_world");
							getNodes("allowed-claim-worlds").setValue(worlds);
						}
					} catch (ObjectMappingException e) {
						e.printStackTrace();
					}    
    	                	            
                    /*------------- ---- Add default config for not updateable configs ------------------*/
                    
                    //update new player flags according version
    	            List<String> flags = new LinkedList<String>(Arrays.asList());    	            
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
        			
        			if (update > 0){
        				getNodes("flags-configuration.enabled-flags").setValue(flags);
        				RedProtect.logger.warning("Configuration UPDATED!");
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
                    
                    for (BlockType mat:Sponge.getRegistry().getAllOf(BlockType.class)){
                    	if (ecoCfgs.getNode("items","values",mat.getName()).getValue() == null){
                    		ecoCfgs.getNode("items","values",mat.getName()).setValue(0.0);                		
                    	}
                    }
                    
                    for (ItemType mat:Sponge.getRegistry().getAllOf(ItemType.class)){
                    	if (ecoCfgs.getNode("items","values",mat.getName()).getValue() == null){
                    		ecoCfgs.getNode("items","values",mat.getName()).setValue(0.0);                		
                    	}
                    }                    
                    for (Enchantment ench:Sponge.getRegistry().getAllOf(Enchantment.class)){
                    	if (ecoCfgs.getNode("enchantments","values",ench.getId()).getValue() == null){
                    		ecoCfgs.getNode("enchantments","values",ench.getId()).setValue(0.0);                		
                    	}
                    }                    
                    
                    //////////////////////
                    
        			//create logs folder
        			File logs = new File(RedProtect.configDir+"logs");
        			if(getBool("log-actions") && !logs.exists()){
        				logs.mkdir();
    	                RedProtect.logger.info("Created folder: " + RedProtect.configDir+"logs");        	    		
        	    	}
        			
        			save();        			
    	            RedProtect.logger.info("All configurations loaded!");
    	            
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
			RedProtect.logger.warning("Added world to color list " + w.getName());
		}
		
		try {
			//RedProtect.logger.debug("default","Writing global flags for world "+ w.getName() + "...");
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
    
    public Boolean getGlobalFlag(String world, String action1, String action2){		
		return this.gflags.getNode(world,action1,action2).getBoolean();
	}
    
    public List<String> getGlobalFlagList(String world, String action){		
		try {
			return this.gflags.getNode(world, action).getList(TypeToken.of(String.class));
		} catch (ObjectMappingException e) {			
			e.printStackTrace();
			return null;
		}
	}
    
    public List<String> getGlobalFlagList(String world, String action1, String action2){		
		try {
			return this.gflags.getNode(world, action1, action2).getList(TypeToken.of(String.class));
		} catch (ObjectMappingException e) {			
			e.printStackTrace();
			return null;
		}
	}
    
    public ItemStack getGuiItemStack(String key){
    	RedProtect.logger.debug("default","Gui Material to get: " + key);
    	RedProtect.logger.debug("default","Result: " + gui.getNode("gui-flags",key,"material").getString());
    	return ItemStack.of((ItemType)RPUtil.getRegistryFor(ItemType.class, gui.getNode("gui-flags",key,"material").getString()), 1);
    }
    
    public Text getGuiFlagString(String flag, String option){    	
    	//RedProtect.logger.severe("Flag: "+flag+" - FlagString: "+this.gui.getNode("gui-flags",flag,option).getString());
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
    	SortedSet<Integer> slots = new TreeSet<Integer>(new ArrayList<Integer>());
    	for (CommentedConfigurationNode key:gui.getNode("gui-flags").getChildrenMap().values()){
    		for (Object key1:key.getChildrenMap().keySet()){    			
        		if (key1.toString().contains("slot")){
        			slots.add(key.getChildrenMap().get(key1).getInt());
        			//RedProtect.logger.severe("Key: "+key.getChildrenMap().get(key1).getInt());
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
    	HashMap<String,Object> flags = new HashMap<String,Object>();
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
    			
    			/*
    			if (RedProtect.plugin.getConfig().get(flag) == null){
    				flags.put(flag.replace("flags.", ""), " ");
    			} else {
    				flags.put(flag.replace("flags.", ""), RedProtect.plugin.getConfig().get(flag));
    			}*/		
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
    	SortedSet<String> values = new TreeSet<String>(getDefFlagsValues().keySet());
		return values;    	
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
    	return BlockTypes.GLOWSTONE;//Material.getMaterial(RedProtect.plugin.getConfig().getString(key));
    }
    
    public void save(){
    	try {
			configManager.save(config);	
			gFlagsManager.save(gflags);
			ecoManager.save(ecoCfgs);
			saveGui();
		} catch (IOException e) {
			RedProtect.logger.severe("Problems during save file:");
			e.printStackTrace();
		}
    }
    
    public void saveGui(){ 
    	try {
    		guiManager.save(gui);
		} catch (IOException e) {
			RedProtect.logger.severe("Problems during save gui file:");
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
		SortedSet<String> values = new TreeSet<String>(getDefFlagsValues().keySet());
		values.addAll(new TreeSet<String>(AdminFlags));
		return values;
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
    
    public List<String> getGlobalFlagList(String key){		
		try {
			return gflags.getNode(key).getList(TypeToken.of(String.class));
		} catch (ObjectMappingException e) {
			e.printStackTrace();
		}
		return new ArrayList<String>();
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
    		if (b != null && getBool("needed-claim-to-build.allow-only-protections-blocks") && (getWorldClaimType(p.getWorld().getName()).equalsIgnoreCase("BLOCK"))){   
    			boolean blocks = b.getState().getName().contains(getString("region-settings.block-id")) || b.getState().getName().contains("SIGN");
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
}
   
