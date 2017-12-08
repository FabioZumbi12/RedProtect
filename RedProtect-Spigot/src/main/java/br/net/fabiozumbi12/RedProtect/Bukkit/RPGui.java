package br.net.fabiozumbi12.RedProtect.Bukkit;

import java.util.Arrays;

import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPConfig;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPLang;
import br.net.fabiozumbi12.RedProtect.Bukkit.hooks.RPProtocolLib;

public class RPGui implements Listener{		

	private String name;
	private int size;
	private ItemStack[] guiItens;
	private Player player;
	private Region region;
	private final boolean allowEnchant;
	private final boolean edit;
	private Inventory inv;

	public RPGui(String name, Player player, Region region,Plugin plugin, boolean edit, int MaxSlot){
		this.edit = edit;		
		this.name = name;
		this.player = player;
		this.region = region;
		
		if (MaxSlot <= 9){
			this.size = 9;
			this.guiItens = new ItemStack[this.size];
		} else
		if (MaxSlot >= 10 && MaxSlot <= 18){
			this.size = 18;
			this.guiItens = new ItemStack[this.size];
		} else
		if (MaxSlot >= 19 && MaxSlot <= 27){
			this.size = 27;
			this.guiItens = new ItemStack[this.size];
		}
		if (MaxSlot >= 28 && MaxSlot <= 36){
			this.size = 36;
			this.guiItens = new ItemStack[this.size];
		}
		if (MaxSlot >= 37 && MaxSlot <= 45){
			this.size = 45;
			this.guiItens = new ItemStack[this.size];
		}
		if (MaxSlot >= 46 && MaxSlot <= 54){
			this.size = 54;
			this.guiItens = new ItemStack[this.size];
		}

        allowEnchant = RedProtect.version >= 181;
		
		for (String flag:region.flags.keySet()){
			if (!(region.flags.get(flag) instanceof Boolean) && !flag.equalsIgnoreCase("clan")){
				continue;
			}
			if (flag.equalsIgnoreCase("clan")){
				if (!RedProtect.SC){
					continue;
				}
				ClanPlayer cp = RedProtect.clanManager.getClanPlayer(player);
				if (cp == null || !cp.isLeader()){
					continue;
				} 
			}
			if (RedProtect.ph.hasPerm(player, "redprotect.flag."+flag) && Material.getMaterial(RPConfig.getGuiFlagString(flag,"material")) != null && RPConfig.isFlagEnabled(flag)){
				if (flag.equals("pvp") && !RedProtect.plugin.getConfig().getStringList("flags-configuration.enabled-flags").contains("pvp")){
    				continue;
    			}	
				
				int i = RPConfig.getGuiSlot(flag);
				
				String fvalue;
				if (flag.equalsIgnoreCase("clan")){
					if (region.flags.get(flag).toString().equals("")){
						fvalue = RPConfig.getGuiString("false");
					} else {
						fvalue = RPConfig.getGuiString("true");
					}					
				} else {
					fvalue = RPConfig.getGuiString(region.flags.get(flag).toString());
				}
				
				if (RedProtect.PLib && allowEnchant){
					this.guiItens[i] = RPProtocolLib.removeAttributes(RPConfig.getGuiItemStack(flag));
				} else {
					this.guiItens[i] = RPConfig.getGuiItemStack(flag);					
				}				
				ItemMeta guiMeta = this.guiItens[i].getItemMeta();
				guiMeta.setDisplayName(RPConfig.getGuiFlagString(flag,"name"));
				guiMeta.setLore(Arrays.asList(RPConfig.getGuiString("value")+fvalue,"§0"+flag,RPConfig.getGuiFlagString(flag,"description"),RPConfig.getGuiFlagString(flag,"description1"),RPConfig.getGuiFlagString(flag,"description2")));
				if (allowEnchant){					
					if (this.region.getFlagBool(flag)){
						guiMeta.addEnchant(Enchantment.DURABILITY, 0, true);
					} else {
						guiMeta.removeEnchant(Enchantment.DURABILITY);
					}
					guiMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);	
				}				
				this.guiItens[i].setType(Material.getMaterial(RPConfig.getGuiFlagString(flag,"material")));
				this.guiItens[i].setItemMeta(guiMeta);
			}
		}
		
		for (int slotc=0; slotc < this.size; slotc++){			
			if (this.guiItens[slotc] == null){
				this.guiItens[slotc] = RPConfig.getGuiSeparator();
			}
		}	
		
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	void onInventoryClose(InventoryCloseEvent event){
		if (event.getInventory().getTitle() != null && event.getInventory().getTitle().equals(this.name)) {
			if (this.edit){
				for (int i = 0; i < this.size; i++){
					try{
						String flag = this.inv.getItem(i).getItemMeta().getLore().get(1).replace("§0", "");
						if (RPConfig.getDefFlags().contains(flag)){
							RPConfig.setGuiSlot(/*this.inv.getItem(i).getType().name(),*/ flag, i);
						}
					} catch (Exception e){
						RPLang.sendMessage(this.player, "gui.edit.error");
						close();
						return;
					}					
				}
				RPConfig.saveGui();
				RPLang.sendMessage(this.player, "gui.edit.ok");
			}
			close();
		}
	}
	
	@EventHandler
	void onDeath(PlayerDeathEvent event){
		if (event.getEntity().getOpenInventory().getTitle().equals(this.name)) {
			close();
		}
	}
	
	@EventHandler
	void onPlayerLogout(PlayerQuitEvent event){
		if (event.getPlayer().getInventory().getTitle().equals(this.name)) {
			close();
		}
	}
	
	@EventHandler
	void onPluginDisable(PluginDisableEvent event){
		RedProtect.logger.debug("Is PluginDisableEvent event.");
		for (Player play:event.getPlugin().getServer().getOnlinePlayers()){
			play.closeInventory();
		}
	}
		
	@EventHandler(priority = EventPriority.LOWEST)
	void onInventoryClick(InventoryClickEvent event){	
		if (event.isCancelled() || !(event.getInventory().getHolder() instanceof Player) || !event.getInventory().getTitle().equals(this.name)){
    		return;
    	}
		
		if (this.edit){			
			return;
		}
				
		if (event.getInventory().getTitle() != null && event.getInventory().getTitle().equals(this.name)){
			event.setCancelled(true);	
			ItemStack item = event.getCurrentItem();
			if (item != null && !item.equals(RPConfig.getGuiSeparator()) && !item.getType().equals(Material.AIR) && event.getRawSlot() >= 0 && event.getRawSlot() <= this.size-1){
				ItemMeta itemMeta = item.getItemMeta();
				String flag = itemMeta.getLore().get(1).replace("§0", "");
				if (RPConfig.getBool("flags-configuration.change-flag-delay.enable")){
					if (RPConfig.getStringList("flags-configuration.change-flag-delay.flags").contains(flag)){
							if (!RedProtect.changeWait.contains(this.region.getName()+flag)){								
								applyFlag(flag, itemMeta, event);	
								RPUtil.startFlagChanger(this.region.getName(), flag, player);								
							} else {
								RPLang.sendMessage(player,RPLang.get("gui.needwait.tochange").replace("{seconds}", RPConfig.getString("flags-configuration.change-flag-delay.seconds")));	
							}
						} else {
							applyFlag(flag, itemMeta, event);
						}					
				} else {
					applyFlag(flag, itemMeta, event);
				}
			}			
	    }
	}
		
	private void applyFlag(String flag, ItemMeta itemMeta, InventoryClickEvent event){
		boolean flagv = false;
		if (flag.equalsIgnoreCase("clan")){
			Player p = (Player) event.getInventory().getHolder();
			ClanPlayer cp = RedProtect.clanManager.getClanPlayer(p);			
			if (this.region.getFlagString(flag).equals("")){	
				this.region.setFlag(flag, cp.getTag());
				flagv = true;
				RPLang.sendMessage(p,RPLang.get("cmdmanager.region.flag.setclan").replace("{clan}", "'"+cp.getClan().getColorTag()+"'"));
			} else {            					
				RPLang.sendMessage(p,RPLang.get("cmdmanager.region.flag.denyclan").replace("{clan}", "'"+this.region.getFlagString(flag)+"'"));
				this.region.setFlag(flag, "");
				flagv = false;
			}
		} else {
			flagv = !this.region.getFlagBool(flag);
			this.region.setFlag(flag, flagv);
			RPLang.sendMessage(player, RPLang.get("cmdmanager.region.flag.set").replace("{flag}", "'"+flag+"'") + " " + flagv);
		}
		
		if (allowEnchant){
			if (flagv){
				itemMeta.addEnchant(Enchantment.DURABILITY, 0, true);
			} else {
				itemMeta.removeEnchant(Enchantment.DURABILITY);
			}
			itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		}								
		itemMeta.setLore(Arrays.asList(RPConfig.getGuiString("value")+RPConfig.getGuiString(String.valueOf(flagv)),"§0"+flag,RPConfig.getGuiFlagString(flag,"description"),RPConfig.getGuiFlagString(flag,"description1"),RPConfig.getGuiFlagString(flag,"description2")));
		event.getCurrentItem().setItemMeta(itemMeta);
		RedProtect.logger.addLog("(World "+this.region.getWorld()+") Player "+player.getName()+" CHANGED flag "+flag+" of region "+this.region.getName()+" to "+String.valueOf(flagv));
	}
	
	public void close(){
        //check for itens
        this.player.updateInventory();
        Bukkit.getScheduler().runTaskLater(RedProtect.plugin, ()-> this.player.updateInventory(), 1);

		this.guiItens = null;
		this.name = null;
		RedProtect.openGuis.remove(this.region.getID());
		this.region = null;
		try {
			this.finalize();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	public void open(){	
		if (RedProtect.openGuis.contains(this.region.getID())){
			RPLang.sendMessage(player, "cmdmanager.region.rpgui-open");
			try {
				this.finalize();
			} catch (Throwable e) {
				e.printStackTrace();
			}
			return;
		}
		Inventory inv = Bukkit.createInventory(player, this.size, this.name);
	    inv.setContents(this.guiItens);
	    player.openInventory(inv);
	    this.inv = inv;
	    RedProtect.openGuis.add(this.region.getID());
	}
}