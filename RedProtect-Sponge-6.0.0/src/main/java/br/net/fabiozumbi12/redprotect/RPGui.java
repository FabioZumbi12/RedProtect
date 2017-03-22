package br.net.fabiozumbi12.redprotect;

import java.util.Arrays;

import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.meta.ItemEnchantment;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.Enchantments;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.text.Text;

import br.net.fabiozumbi12.redprotect.config.RPLang;


public class RPGui {		

	private int size;
	private ItemStack[] guiItens;
	private Player player;
	private Region region;
	private Inventory inv;
	private String name;
	
	public RPGui(String name, Player player, Region region, int MaxSlot){
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
		
		for (String flag:region.flags.keySet()){
			if (!(region.flags.get(flag) instanceof Boolean)){
				continue;
			}
			if (RedProtect.ph.hasFlagPerm(player, flag) && RedProtect.cfgs.isFlagEnabled(flag) && RPUtil.testRegistry(ItemType.class, RedProtect.cfgs.getGuiFlagString(flag,"material").toPlain())){
				if (flag.equals("pvp") && !RedProtect.cfgs.getStringList("flags-configuration.enabled-flags").contains("pvp")){
    				continue;
				}
				
				int i = RedProtect.cfgs.getGuiSlot(flag);
				
				this.guiItens[i] = ItemStack.of((ItemType)RPUtil.getRegistryFor(ItemType.class,RedProtect.cfgs.getGuiFlagString(flag,"material").toPlain()), 1);
				this.guiItens[i].offer(Keys.DISPLAY_NAME, RedProtect.cfgs.getGuiFlagString(flag,"name"));
								
				this.guiItens[i].offer(Keys.ITEM_LORE, Arrays.asList(
						Text.of(RedProtect.cfgs.getGuiString("value"),RedProtect.cfgs.getGuiString(region.flags.get(flag).toString())),
						RPUtil.toText("&0"+flag),
						RedProtect.cfgs.getGuiFlagString(flag,"description"),
						RedProtect.cfgs.getGuiFlagString(flag,"description1"),
						RedProtect.cfgs.getGuiFlagString(flag,"description2")));
												
				if (!this.region.getFlagBool(flag)){
					this.guiItens[i].remove(Keys.ITEM_ENCHANTMENTS);
				} else {
					this.guiItens[i].offer(Keys.ITEM_ENCHANTMENTS, Arrays.asList(new ItemEnchantment(Enchantments.UNBREAKING, 1)));
				}
				this.guiItens[i].offer(Keys.HIDE_ENCHANTMENTS, true);
				this.guiItens[i].offer(Keys.HIDE_ATTRIBUTES, true);				
			}
		}
				
		this.inv = Inventory.builder().of(InventoryArchetypes.DOUBLE_CHEST)
				.property(InventoryDimension.PROPERTY_NAME, new InventoryDimension(9, this.size/9))
				.property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(RPUtil.toText(this.name)))
				.build(RedProtect.plugin);		
		
		
		for (int slotc=0; slotc < this.size; slotc++){			
			if (this.guiItens[slotc] == null){
				this.guiItens[slotc] = RedProtect.cfgs.getGuiSeparator();
			}			
			int linha = 0;
			int slot = slotc;
			if (slotc > 8){
				linha = slotc/9;
				slot = slotc-(linha*9);				
			}			
			this.inv.query(new SlotPos(slot,linha)).set(this.guiItens[slotc]);
		}		
		RedProtect.game.getEventManager().registerListeners(RedProtect.plugin, this);		
	}
	
	@Listener
	public void onDeath(InteractInventoryEvent.Close event){
		if (event.getTargetInventory().getName().get().equals(this.inv.getName().get())){
			close();
		}
	}
	
	@Listener
	public void onDeath(DestructEntityEvent event){
		if (event.getTargetEntity() instanceof Player){
			Player p = (Player) event.getTargetEntity();
			if (p.getName().equals(this.player.getName())){
				close();
			}
		}		
	}
	
	@Listener
	public void onPlayerLogout(ClientConnectionEvent.Disconnect event){
		Player p = event.getTargetEntity();
		if (p.getName().equals(this.player.getName())){
			close();
		}
	}
	
	@Listener
	public void onPluginDisable(GameStoppingServerEvent event){
		close();
	}
		
	@Listener
	public void onInventoryClick(ClickInventoryEvent event){	
		if (event.getTargetInventory().getName().get().equals(this.inv.getName().get())) {
			
			if (event.getTransactions().size() > 0) {
				Transaction<ItemStackSnapshot> clickTransaction = event.getTransactions().get(0);
				
				ItemStack item = clickTransaction.getOriginal().createStack();
								
				if (!item.getItem().equals(ItemTypes.NONE) && item.get(Keys.ITEM_LORE).isPresent()){					
					String flag = item.get(Keys.ITEM_LORE).get().get(1).toPlain().replace("§0", "");
					if (RedProtect.cfgs.getDefFlags().contains(flag)){
						if (RedProtect.cfgs.getBool("flags-configuration.change-flag-delay.enable")){
							if (RedProtect.cfgs.getStringList("flags-configuration.change-flag-delay.flags").contains(flag)){
									if (!RedProtect.changeWait.contains(this.region.getName()+flag)){								
										applyFlag(flag, item, event);	
										RPUtil.startFlagChanger(this.region.getName(), flag, this.player);								
									} else {
										RPLang.sendMessage(player,RPLang.get("gui.needwait.tochange").replace("{seconds}", RedProtect.cfgs.getString("flags-configuration.change-flag-delay.seconds")));	
										event.setCancelled(true);
									}									
									return;
								} else {
									applyFlag(flag, item, event);
									return;
								}					
						} else {
							applyFlag(flag, item, event);
							return;
						}
					}		
					event.setCancelled(true);
					return;
				}
			}
			
		}		
	}
	
	private void applyFlag(String flag, ItemStack item, ClickInventoryEvent event){				
		this.region.setFlag(flag, !this.region.getFlagBool(flag));		
		RPLang.sendMessage(player, RPLang.get("cmdmanager.region.flag.set").replace("{flag}", "'"+flag+"'") + " " + this.region.getFlagBool(flag));
				
		if (!this.region.getFlagBool(flag)){
			item.remove(Keys.ITEM_ENCHANTMENTS);			
		} else {
			item.offer(Keys.ITEM_ENCHANTMENTS, Arrays.asList(new ItemEnchantment(Enchantments.UNBREAKING, 1)));			
		}
		item.offer(Keys.HIDE_ENCHANTMENTS, true);
		item.offer(Keys.HIDE_ATTRIBUTES, true);								
		
		item.offer(Keys.ITEM_LORE, Arrays.asList(
				Text.of(RedProtect.cfgs.getGuiString("value"),RedProtect.cfgs.getGuiString(this.region.getFlagString(flag))),
				RPUtil.toText("&0"+flag),
				RedProtect.cfgs.getGuiFlagString(flag,"description"),
				RedProtect.cfgs.getGuiFlagString(flag,"description1"),
				RedProtect.cfgs.getGuiFlagString(flag,"description2")));
		
		//RedProtect.logger.severe("Item Lore: "+item.get(Keys.ITEM_LORE).get().get(0).toPlain());
			
		event.getCursorTransaction().setCustom(ItemStackSnapshot.NONE);
		event.getTransactions().get(0).getSlot().offer(item);
				
		RPUtil.removeGuiItem(this.player);
		
		RedProtect.logger.addLog("(World "+this.region.getWorld()+") Player "+player.getName()+" CHANGED flag "+flag+" of region "+this.region.getName()+" to "+this.region.getFlagString(flag));
	}
	
	public void close(){
		RPUtil.removeGuiItem(this.player);
		RedProtect.game.getEventManager().unregisterListeners(this);
		this.guiItens = null;
		this.player.closeInventory(Cause.of(NamedCause.of(this.player.getName(),this.player)));	
		this.player = null;
		this.region = null;			
		try {
			this.finalize();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	public void open(){
		this.player.openInventory(this.inv, Cause.of(NamedCause.of(this.player.getName(),this.player)));
	}
	
}