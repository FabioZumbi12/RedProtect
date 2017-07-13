package br.net.fabiozumbi12.RedProtect.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.bukkit.configuration.InvalidConfigurationException;

import br.net.fabiozumbi12.RedProtect.RedProtect;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class RPCommentedConfig {
	
	private HashMap<String, String> comments;

	RPCommentedConfig(){
		this.comments = new HashMap<String, String>();		
	}
	
	public void addDef(){
		File config = new File(RedProtect.pathConfig);
		if (config.exists()){
			try {
				RedProtect.plugin.getConfig().load(config);
			} catch (IOException | InvalidConfigurationException e) {
				e.printStackTrace();
			}
		}
				
		setDefault("config-version", 7.9, "Dont touch <3");
		setDefault("debug-messages", false, "Enable debug messages");
		setDefault("log-actions", true, "Log all commands used by players");
		setDefault("language", "en-EN", "Available: en-EN, pt-BR, zh-CN, de-DE, ru-RU");
		setDefault("file-type", "yml", "Available: yml and mysql");
		
		setDefault("flat-file", null, ""
				+ "If file-type: yml, configuration:\n"
				+ "region-per-file: Want to save the regions in your ow files?\n"
				+ "This can improve performance because theres a new system where the autosave will save only modified regions and not all on auto-save.");
		setDefault("flat-file.region-per-file", false, null);
		setDefault("flat-file.auto-save-interval-seconds", 3600, null);
		setDefault("flat-file.backup", true, null);
		setDefault("flat-file.max-backups", 10, null);
		
		setDefault("mysql", null, "If file-type: mysql, configuration:");
		setDefault("mysql.db-name", "redprotect", null);
		setDefault("mysql.table-prefix", "rp_", null);
		setDefault("mysql.user-name", "root", null);
		setDefault("mysql.user-pass", "redprotect", null);
		setDefault("mysql.host", "localhost", null);
		
		setDefault("region-settings", null, ""
				+ "region-settings:\n"
				+ "claim-type: Claim types allowed for normal players (without permission 'redprotect.admin.claim'). Options: BLOCK, WAND or BOTH.\n"
				+ "-> If BLOCK, the players needs to surround your house with the block type in configuration, and place a sign under this fence with [rp] on first line.\n"
				+ "-> If WAND, the players will need a wand (default glass_bottle), click on two point of your region, and then use /rp claim [name of region] to claim te region.\n"
				+ "-> If BOTH, will allow both claim type protections.\n"
				+ "\n"
				+ "default-leader: The name of leader for regions created with /rp define or regions without leaders.\n"
				+ "\n"
				+ "world-colors: Colors of world to show on /rp info and /rp list.\n"
				+ "\n"
				+ "border: Border block type when use /rp border.\n"
				+ "\n"
				+ "region-list:\n"
				+ "simple-listing: Show simple list with only name of region or all region info.\n"
				+ "hover-and-click-teleport: If running server 1.8+ enable hover and teleport click on simple list.\n"
				+ "show-area: Show region areas on list?\n"
				+ "\n"
				+ "autoexpandvert-ondefine: Automatically set max y to 256 and min y to 0 (sky to bedrock).\n"
				+ "\n"
				+ "anti-hopper: Deny break/place blocks under chests.\n"
				+ "\n"
				+ "claim-modes: Default modes for claim regions. Modes available: keep, drop, remove or give.\n"
				+ "-> keep: Nothing happens\n"
				+ "-> drop: Will drop all protection blocks\n"
				+ "-> remove: Will remove all protection blocks\n"
				+ "-> give: Give back the protection blocks to player, and drop(on player location) if players's inventory is full\n"
				+ "allow-player-decide: Allow players to decide what mode to use? If true, the player need to set the line 4 of the sign with [keep], [drop], [remove], [give] or a translation you is using on 'lang.ini'.\n"
				+ "use-perm: If 'allow-player-decide' is true, player need to have the permission 'redprotect.use-claim-modes' to use modes on signs.\n"
				+ "\n"
				+ "limit-amount: Limit of blocks until the player have other block permission.\n"
				+ "\n"
				+ "claim-amount: Limit of claims a player can have until have other permission for claims.\n"
				+ "\n"
				+ "block-id: Block used to protect regions.\n"
				+ "\n"
				+ "max-scan: Ammount of blocks to scan on place sign to claim a region. Consider this the max area.\n"
				+ "\n"
				+ "date-format: Time format to use with data and time infos.\n"
				+ "\n"
				+ "record-player-visit-method: Register player visits on... Available: ON-LOGIN, ON-REGION-ENTER.\n"
				+ "\n"
				+ "allow-sign-interact-tags: Allow players without permissions to interact with signs starting with this tags.\n"
				+ "\n"
				+ "leadership-request-time: Time in seconds to wait player accept leadership request\n"
				+ "\n"
				+ "enable-flag-sign: Enable flag signs.\n"
				+ "\n"
				+ "deny-build-near: Deny players to build near other regions. Distance in blocks. 0 to disable and > 0 to enable.\n"
				+ "\n"
				+ "rent:\n"
				+ "default-level: Set the default rent level of players renting region. Options: member, admin, leader.\n"
				+ "add-player: Allow who pay for rent to add more players in rent regions?\n"
				+ "command-renew-adds: The amount of days or mounts the command to renew will be added. The sintax is: <number>:<type>. The types are: DAY or MONTH.\n"
				+ "renew-anytime: Renew in anytime or only on renew date?\n"
				+ "\n"
				+ "first-home:\n"
				+ "can-delete-after-claims: Player can remove the protection of first home after this amount of claims. Use -1 to do not allow to delete.\n"
				+ "\n"
				+ "delay-after-kick-region: Delay before a kicked player can back to a region (in seconds).\n"
				+ "\n"
				+ "claimlimit-per-world: Use claim limit per worlds?\n"
				+ "\n"
				+ "blocklimit-per-world: Use block limit per worlds?");
		
		setDefault("region-settings.claim-type", "BLOCK", null);
		setDefault("region-settings.default-leader", "#server#", null);
		setDefault("region-settings.world-colors", new ArrayList<String>(), null);
		setDefault("region-settings.border.material", "GLOWSTONE", null);
		setDefault("region-settings.border.time-showing", "BLOCK", null);
		setDefault("region-settings.region-list.simple-listing", true, null);
		setDefault("region-settings.region-list.hover-and-click-teleport", true, null);
		setDefault("region-settings.region-list.show-area", true, null);
		setDefault("region-settings.autoexpandvert-ondefine", true, null);
		setDefault("region-settings.anti-hopper", true, null);
		setDefault("region-settings.claim-modes.mode", "keep", null);
		setDefault("region-settings.claim-modes.allow-player-decide", false, null);
		setDefault("region-settings.claim-modes.use-perm", false, null);
		setDefault("region-settings.limit-amount", 8000, null);
		setDefault("region-settings.claim-amount", 20, null);
		setDefault("region-settings.block-id", "FENCE", null);
		setDefault("region-settings.max-scan", 600, null);
		setDefault("region-settings.date-format", "dd/MM/yyyy", null);
		setDefault("region-settings.record-player-visit-method", "ON-LOGIN", null);
		setDefault("region-settings.allow-sign-interact-tags", Arrays.asList("Admin Shop", "{membername}"), null);
		setDefault("region-settings.leadership-request-time", 20, null);
		setDefault("region-settings.enable-flag-sign", true, null);
		setDefault("region-settings.deny-build-near", 0, null);
		setDefault("region-settings.rent.default-level", "admin", null);
		setDefault("region-settings.rent.add-player", false, null);
		setDefault("region-settings.rent.command-renew-adds", "1:MONTH", null);
		setDefault("region-settings.rent.renew-anytime", false, null);
		setDefault("region-settings.first-home.can-delete-after-claims", 10, null);
		setDefault("region-settings.delay-after-kick-region", 60, null);
		setDefault("region-settings.claimlimit-per-world", true, null);
		setDefault("region-settings.blocklimit-per-world", true, null);
		
		setDefault("allowed-claim-worlds", Arrays.asList("example_world"), "World that player can claim regions.");
		
		setDefault("needed-claim-to-build", null, "Worlds where players can't build without claim.");
		setDefault("needed-claim-to-build.worlds", Arrays.asList("example_world"), null);
		setDefault("needed-claim-to-build.allow-only-protections-blocks", true, null);
		
		setDefault("wands", null, ""
				+ "wands:\n"
				+ "adminWandID: Item used to define and redefine regions.\n"
				+ "infoWandID: Item used to check regions.");
		setDefault("wands.adminWandID", 374, null);
		setDefault("wands.infoWandID", 339, null);
		
		setDefault("private", null, ""
				+ "private:\n"
				+ "use: Enable private signs?\n"
				+ "allow-outside: Allow private signs outside regions\n"
				+ "allowed-blocks-use-ids: Use number IDs instead item names?\n"
				+ "allowed-blocks: Blocks allowed to be locked with private signs.");
		setDefault("private.use", true, null);
		setDefault("private.allow-outside", false, null);
		setDefault("private.allowed-blocks-use-ids", false, null);
		setDefault("private.allowed-blocks", 
				Arrays.asList("DISPENSER", 
						"NOTE_BLOCK", 
						"BED_BLOCK", 
						"CHEST", 
						"WORKBENCH", 
						"FURNACE", 
						"JUKEBOX", 
						"ENCHANTMENT_TABLE", 
						"BREWING_STAND", 
						"CAULDRON",
						"ENDER_CHEST",
						"BEACON",
						"TRAPPED_CHEST",
						"HOPPER",
						"DROPPER"), null);
		
		setDefault("notify", null, ""
				+ "notify:\n"
				+ "region-enter: Show region info when enter a region.\n"
				+ "region-exit: Show region info(or wilderness message) when exit a region.\n"
				+ "region-enter-mode: How to show the messages? Available: BOSSBAR, CHAT. If plugin BoobarApi not installed, will show on chat.\n"
				+ "welcome-mode: Where to show the welcome message (/rp wel <message>)? Available: BOSSBAR, CHAT.");		
		setDefault("notify.region-enter", true, null);
		setDefault("notify.region-exit", true, null);
		setDefault("notify.region-enter-mode", "BOSSBAR", null);
		setDefault("notify.welcome-mode", "BOSSBAR", null);
		
		setDefault("netherProtection", null, ""
				+ "netherProtection:\n"
				+ "maxYsize: Max size of your world nether.\n"
				+ "execute-cmd: Execute this if player go up to maxYsize of nether.");		
		setDefault("netherProtection.maxYsize", 128, null);
		setDefault("netherProtection.execute-cmd", Arrays.asList("spawn {player}"), null);
		
		setDefault("server-protection", null, ""
				+ "server-protection:\n"
				+ "deny-potions: List of potions the player cant use on server. Here the PotioTypes: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionType.html\n"
				+ "deny-playerdeath-by: List of causes the player cant die/take damage for. here the list of DamageCauses: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/entity/EntityDamageEvent.DamageCause.html\n"
				+ "deny-commands-on-worlds: Deny certain commands on specific worlds.\n"
				+ "nickname-cap-filter: Deny players with same nick but cap diferences to join on server (most used on offline severs).\n"
				+ "sign-spy: \n"
				+ "enable: Show all lines of a sign when player place signs in any world.\n"
				+ "only-console: Show only on console or in-game too?\n"
				+ "\n"
				+ "teleport-player:\n"
				+ "on-join: and on-leave: Teleport player on join the server.\n"
				+ "need-world-to-teleport: The player need to be in this world to be teleported? Use 'none' for all worlds.\n"
				+ "location: The location, using as world, x, y, z.\n"
				+ "\n"
				+ "deny-structure-bypass-regions: Deny structures like trees to bypass region borders?\n"
				+ "\n"
				+ "check-killaura-freekill:\n"
				+ "enable: Enable kill aura or freekill checker?\n"
				+ "check-rate: This will count every block the player wall without fail to aim on player.\n"
				+ "rate-multiples: What multiples of check-rate is considered kh or fk?\n"
				+ "time-between-trys: Time to reset checks between attacker hits.\n"
				+ "debug-trys: Debug everu try? Used to see the try count on every block the player walk. Will be sequential if the player is using kill aura and will go to more than 60, 80 more than 100 if is free kill.\n"
				+ "\n"
				+ "check-player-client: Test client hack (beta)");		
		setDefault("server-protection.deny-potions", Arrays.asList("INVISIBILITY"), null);
		setDefault("server-protection.deny-playerdeath-by", Arrays.asList("SUFFOCATION"), null);
		setDefault("server-protection.deny-commands-on-worlds.world", Arrays.asList("command"), null);
		setDefault("server-protection.nickname-cap-filter.enable", false, null);
		setDefault("server-protection.sign-spy.enable", false, null);
		setDefault("server-protection.sign-spy.only-console", true, null);
		setDefault("server-protection.teleport-player.on-join.enable", false, null);
		setDefault("server-protection.teleport-player.on-join.need-world-to-teleport", "none", null);
		setDefault("server-protection.teleport-player.on-join.location", "world, 0, 90, 0", null);
		setDefault("server-protection.teleport-player.on-leave.enable", false, null);
		setDefault("server-protection.teleport-player.on-leave.need-world-to-teleport", "none", null);
		setDefault("server-protection.teleport-player.on-leave.location", "world, 0, 90, 0", null);
		setDefault("server-protection.deny-structure-bypass-regions", true, null);
		setDefault("server-protection.check-killaura-freekill.enable", false, null);
		setDefault("server-protection.check-killaura-freekill.check-rate", 30, null);
		setDefault("server-protection.check-killaura-freekill.rate-multiples", 5, null);
		setDefault("server-protection.check-killaura-freekill.time-between-trys", 3, null);
		setDefault("server-protection.check-killaura-freekill.debug-trys", false, null);
		setDefault("server-protection.check-player-client", false, null);
		
		setDefault("flags", null, "Default values for new regions.");
		setDefault("flags.pvp", false, null);
		setDefault("flags.chest", false, null);
		setDefault("flags.lever", false, null);
		setDefault("flags.button", false, null);
		setDefault("flags.door", false, null);
		setDefault("flags.smart-door", true, null);
		setDefault("flags.spawn-monsters", true, null);
		setDefault("flags.spawn-animals", true, null);
		setDefault("flags.passives", false, null);
		setDefault("flags.flow", true, null);
		setDefault("flags.fire", true, null);
		setDefault("flags.minecart", false, null);
		setDefault("flags.allow-home", false, null);
		setDefault("flags.allow-magiccarpet", true, null);
		setDefault("flags.mob-loot", false, null);
		setDefault("flags.flow-damage", false, null);
		setDefault("flags.iceform-player", true, null);
		setDefault("flags.iceform-world", true, null);
		setDefault("flags.allow-fly", false, null);
		setDefault("flags.teleport", false, null);
		setDefault("flags.clan", "", null);
		setDefault("flags.ender-chest", true, null);
		setDefault("flags.can-grow", true, null);
		setDefault("flags.use-potions", true, null);
		setDefault("flags.allow-effects", true, null);
		setDefault("flags.allow-spawner", false, null);
		setDefault("flags.leaves-decay", false, null);
		setDefault("flags.build", false, null);
		
		setDefault("flags-configuration", null, ""
				+ "effects-duration: Duration for timed flags like potions effects, jump, etc.\n"
				+ "enabled-flags: Flags enabled to players use with commands and flag Gui.\n"
				+ "pvparena-nopvp-kick-cmd: Command to use if players with pvp off enter in a region with 'pvparena' enabled.\n"
				+ "change-flag-delay: Delay the player can change a flag after last change.\n"
				+ "flags: List of flags the player will need to wait to change.");
		setDefault("flags-configuration.effects-duration", 5, null);
		setDefault("flags-configuration.enabled-flags", Arrays.asList(
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
				"build"), null);		
		setDefault("flags-configuration.pvparena-nopvp-kick-cmd", "spawn {player}", null);
		setDefault("flags-configuration.change-flag-delay.enable", true, null);
		setDefault("flags-configuration.change-flag-delay.seconds", 10, null);
		setDefault("flags-configuration.change-flag-delay.flags", Arrays.asList("pvp"), null);
		
		setDefault("purge", null, ""
				+ "purge:\n"
				+ "Remove regions after x time the player dont came online.\n"
				+ "regen: Hook with WorldEdit, will regen only the region areas to bedrock to sky. Theres no undo for this action!\n"
				+ "max-area-regen: Max area size to automatic regen the region.\n"
				+ "awe-logs: Show regen logs if using AsyncWorldEdit.");
		setDefault("purge.enabled", false, null);
		setDefault("purge.remove-oldest", 90, null);
		setDefault("purge.regen.enable", false, null);
		setDefault("purge.regen.max-area-regen", 500, null);
		setDefault("purge.regen.awe-logs", false, null);
		setDefault("purge.ignore-regions-from-players", new ArrayList<String>(), null);
		
		setDefault("sell", null, ""
				+ "sell:\n"
				+ "Put regions to sell after x time the player dont came online.");
		setDefault("sell.enabled", false, null);
		setDefault("sell.sell-oldest", 90, null);
		setDefault("sell.ignore-regions-from-players", new ArrayList<String>(), null);
		
		setDefault("performance", null, ""
				+ "performance:\n"
				+ "disable-onPlayerMoveEvent-handler: Disable player move event to improve performance? Note: Disabling this will make some flags do not work, like deny enter, execute commands and effects.\n"
				+ "disable-PistonEvent-handler: Disable piston listener? Disabling this will allow players to get blocks from protected regions to unprotected using pistons.\n"
				+ "restrict-piston-event: Fire the piston extract/retract every x ticks. Server default is 1 tick/event. Value in ticks."
				+ "");
		setDefault("performance.disable-onPlayerMoveEvent-handler", false, null);
		setDefault("performance.piston.disable-PistonEvent-handler", false, null);
		setDefault("performance.piston.use-piston-restricter", false, null);
		setDefault("performance.piston.restrict-piston-event", 10, null);
		
		setDefault("schematics", null, ""
				+ "schematics:\n"
				+ "This is the schematics configs for RedProtect.\n"
				+ "first-house-file: Schematic file name to use with /rp start.");
		setDefault("schematics.first-house-file", "house1.schematic", null);
		
		setDefault("hooks", null, ""
				+ "hooks:\n"
				+ "check-uuid-names-onstart: Convert/check names if need to update to/from UUID/names on server start? Disable for Bungeecoord.\n"
				+ "\n"
				+ "essentials: Import last visits from Essentials to RedProtect Regions.\n"
				+ "\n"
				+ "dynmap:\n"
				+ "enable: Enable hook to show all regions on dynmap plugin?\n"
				+ "hide-by-default: Hide the Redprotect tab group by default?\n"
				+ "marks-groupname: Group to show on hide/show tab map.\n"
				+ "layer-priority: If you use another region mark plugin.\n"
				+ "show-label: Show names under regions.\n"
				+ "show-icon: Show icons under regions.\n"
				+ "marker-icon: Icon name to show under regions. All icons are available here: http://i.imgur.com/f61GPoE.png\n"
				+ "show-leaders-admins: Show leaders and admin on hover?\n"
				+ "cuboid-region: Cuboid region config.\n"
				+ "\n"
				+ "magiccarpet: Fix pistons allow get mc blocks.\n"
				+ "\n"
				+ "mcmmo:\n"
				+ "fix-acrobatics-fireoff-leveling: Fix players leveling with creeper explosions on flag fire disabled.\n"
				+ "fix-berserk-invisibility: Fix the ability berserk making players and mobs invisible around who activated the ability.\n"
				+ "\n"
				+ "worldedit: Use WorldEdit to paste newbie home-schematics (/rp start)? *RedProtect already can paste schematics without WorldEdit, but dont support NBT tags like chest contents and sign messages.\n"
				+ "\n"
				+ "asyncworldedit: Use AsyncWorldEdit for regen regions? (need WorldEdit).\n"
				+ "\n"
				+ "factions: Allow players claim Factions chunks under RedProtect regions?\n"
				+ "\n"
				+ "simpleclans:\n"
				+ "use-war: Enable Clan Wars from SimleClans.\n"
				+ "war-on-server-regions: Allow war clans to pvp on #server# regions?");
		setDefault("hooks.check-uuid-names-onstart", false, null);
		setDefault("hooks.essentials.import-lastvisits", false, null);
		setDefault("hooks.dynmap.enable", true, null);
		setDefault("hooks.dynmap.hide-by-default", true, null);
		setDefault("hooks.dynmap.marks-groupname", "RedProtect", null);
		setDefault("hooks.dynmap.layer-priority", 10, null);
		setDefault("hooks.dynmap.show-label", true, null);
		setDefault("hooks.dynmap.show-icon", true, null);
		setDefault("hooks.dynmap.marker-icon", "shield", null);
		setDefault("hooks.dynmap.show-leaders-admins", false, null);
		setDefault("hooks.dynmap.cuboid-region.enable", true, null);
		setDefault("hooks.dynmap.cuboid-region.if-disable-set-center", 60, null);
		setDefault("hooks.dynmap.min-zoom", 0, null);
		setDefault("hooks.magiccarpet.fix-piston-getblocks", true, null);
		setDefault("hooks.armor-stands.spawn-arms", true, null);
		setDefault("hooks.mcmmo.fix-acrobatics-fireoff-leveling", true, null);
		setDefault("hooks.mcmmo.fix-berserk-invisibility", true, null);
		setDefault("hooks.worldedit.use-for-schematics", true, null);
		setDefault("hooks.asyncworldedit.use-for-regen", false, null);
		setDefault("hooks.factions.claim-over-rps", false, null);		
		setDefault("hooks.simpleclans.use-war", false, null);	
		setDefault("hooks.simpleclans.war-on-server-regions", false, null);
	}
	
	private void setDefault(String key, Object def, String comment){
		if (def != null){
			RedProtect.plugin.getConfig().set(key, RedProtect.plugin.getConfig().get(key, def));
		}		
 		if (comment != null){
 			setComment(key, "# "+comment);
 		}
 	}
 	
 	private void setComment(String key, String comment){
 		comments.put(key, comment);
 	}
 	
 	public void saveConfig(){
 		StringBuilder b = new StringBuilder();
 		RedProtect.plugin.getConfig().options().header(null);
 		
 		b.append(""
					+ "# +--------------------------------------------------------------------+ #\n"
					+ "# <               RedProtect World configuration File                  > #\n"
	                + "# <--------------------------------------------------------------------> #\n"
	                + "# <       This is the configuration file, feel free to edit it.        > #\n"
	                + "# <        For more info about cmds and flags, check our Wiki:         > #\n"
	                + "# <         https://github.com/FabioZumbi12/RedProtect/wiki            > #\n"
	                + "# +--------------------------------------------------------------------+ #\n").append('\n');
 		
 		for (String line:RedProtect.plugin.getConfig().saveToString().split("\\r?\\n")){
 			String cLine = line.split(":")[0];
 			if (comments.containsKey(cLine)){
 				b.append('\n').append(comments.get(cLine).replace("\n", "\n# ")).append('\n');
 			}
 			b.append(line).append('\n');
 		}
 		
 		try {
 			Files.write(b, new File(RedProtect.pathMain, "config.yml"), Charsets.UTF_8);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}

}
