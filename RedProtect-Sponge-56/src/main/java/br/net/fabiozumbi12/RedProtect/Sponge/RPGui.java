package br.net.fabiozumbi12.RedProtect.Sponge;

import br.net.fabiozumbi12.RedProtect.Sponge.config.RPLang;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Color;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;


public class RPGui {		

	private int size;
	private ItemStack[] guiItens;
	private Player player;
	private Region region;
	private Inventory inv;
	private String name;
	private boolean edit;
	
	public RPGui(String name, Player player, Region region, boolean edit, int MaxSlot){
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

		for (Map.Entry<String, Object> flag:region.getFlags().entrySet()){
			if (!(region.getFlags().get(flag.getKey()) instanceof Boolean) || !RedProtect.get().cfgs.guiRoot().gui_flags.containsKey(flag.getKey())){
				continue;
			}
			try {
				if (RedProtect.get().ph.hasFlagPerm(player, flag.getKey()) && RedProtect.get().cfgs.isFlagEnabled(flag.getKey()) && RPUtil.getRegistryFor(ItemType.class, RedProtect.get().cfgs.guiRoot().gui_flags.get(flag.getKey()).material).isPresent()){
					if (flag.getKey().equals("pvp") && !RedProtect.get().cfgs.root().flags_configuration.enabled_flags.contains("pvp")){
						continue;
					}

					int i = RedProtect.get().cfgs.getGuiSlot(flag.getKey());

					this.guiItens[i] = ItemStack.of((ItemType)RPUtil.getRegistryFor(ItemType.class, RedProtect.get().cfgs.guiRoot().gui_flags.get(flag.getKey()).material).orElse(ItemTypes.GLASS_PANE), 1);

					this.guiItens[i].offer(Keys.DISPLAY_NAME, RPUtil.toText(RedProtect.get().cfgs.guiRoot().gui_flags.get(flag.getKey()).name));

					this.guiItens[i].offer(Keys.ITEM_LORE, Arrays.asList(
							Text.of(RedProtect.get().cfgs.getGuiString("value"),RedProtect.get().cfgs.getGuiString(region.getFlags().get(flag.getKey()).toString())),
							RPUtil.toText("&0"+flag.getKey()),
							RPUtil.toText(RedProtect.get().cfgs.guiRoot().gui_flags.get(flag.getKey()).description),
							RPUtil.toText(RedProtect.get().cfgs.guiRoot().gui_flags.get(flag.getKey()).description1),
							RPUtil.toText(RedProtect.get().cfgs.guiRoot().gui_flags.get(flag.getKey()).description2)));

					if (!this.region.getFlagBool(flag.getKey())){
						this.guiItens[i].remove(Keys.ITEM_ENCHANTMENTS);
					} else {
						this.guiItens[i] = RedProtect.get().getPVHelper().offerEnchantment(this.guiItens[i]);
					}
					this.guiItens[i].offer(Keys.HIDE_ENCHANTMENTS, true);
					this.guiItens[i].offer(Keys.HIDE_ATTRIBUTES, true);
				}
			} catch (Exception ex){
				this.player.sendMessage(Text.of(Color.RED, "Seems Redprotect have a wrong Item Gui or a problem on guiconfig. Report this to server owner."));
				return;
			}
		}
				
		this.inv = RedProtect.get().getPVHelper().newInventory(size, name);

        for (int slotc=0; slotc < this.size; slotc++){
			if (this.guiItens[slotc] == null){
				this.guiItens[slotc] = RedProtect.get().cfgs.getGuiSeparator();
			}			
			int line = 0;
			int slot = slotc;
			if (slotc > 8){
                line = slotc/9;
				slot = slotc-(line*9);
			}
			RedProtect.get().getPVHelper().query(inv, slot, line).set(this.guiItens[slotc]);
		}

		RedProtect.get().game.getEventManager().registerListeners(RedProtect.get().container, this);
	}
	
	@Listener
	public void onCloseInventory(InteractInventoryEvent.Close event){
		if (event.getTargetInventory().getName().get().equals(this.inv.getName().get())){
			if (this.edit){
				for (int i = 0; i < this.size; i++){
					try{
						int line = 0;
						int slot = i;
						if (i > 8){
							line = i/9;
							slot = i-(line*9);
						}
						if (RedProtect.get().getPVHelper().query(event.getTargetInventory(), slot, line).peek().isPresent()){
							final int fi = i;
							ItemStack stack = RedProtect.get().getPVHelper().query(event.getTargetInventory(), slot, line).peek().get();
							stack.get(Keys.ITEM_LORE).ifPresent(ls -> {
								String flag = ls.get(1).toPlain().replace("§0", "");
								if (RedProtect.get().cfgs.getDefFlags().contains(flag))
									RedProtect.get().cfgs.setGuiSlot(flag, fi);
							});
						}
					} catch (Exception e){
						RPLang.sendMessage(this.player, "gui.edit.error");
						close(false);
						e.printStackTrace();
						return;
					}
				}
				RedProtect.get().cfgs.saveGui();
				RPLang.sendMessage(this.player, "gui.edit.ok");
			}
			close(false);
		}
	}
	
	@Listener
	public void onDeath(DestructEntityEvent event){
		if (event.getTargetEntity() instanceof Player){
			Player p = (Player) event.getTargetEntity();
			if (p.getName().equals(this.player.getName())){
				close(true);
			}
		}		
	}
	
	@Listener
	public void onPlayerLogout(ClientConnectionEvent.Disconnect event){
		Player p = event.getTargetEntity();
		if (p.getName().equals(this.player.getName())){
			close(true);
		}
	}
	
	@Listener
	public void onPluginDisable(GameStoppingServerEvent event){
		close(true);
	}
		
	@Listener
	public void onInventoryClick(ClickInventoryEvent event){	
		if (event.getTargetInventory().getName().get().equals(this.inv.getName().get())) {

			if (this.edit){
				return;
			}

			if (event.getTransactions().size() > 0) {
				Transaction<ItemStackSnapshot> clickTransaction = event.getTransactions().get(0);
				
				ItemStack item = clickTransaction.getOriginal().createStack();
								
				if (!RedProtect.get().getPVHelper().getItemType(item).equals(ItemTypes.NONE) && item.get(Keys.ITEM_LORE).isPresent()){
					String flag = item.get(Keys.ITEM_LORE).get().get(1).toPlain().replace("§0", "");
					if (RedProtect.get().cfgs.getDefFlags().contains(flag)){
						if (RedProtect.get().cfgs.root().flags_configuration.change_flag_delay.enable){
							if (RedProtect.get().cfgs.root().flags_configuration.change_flag_delay.flags.contains(flag)){
									if (!RedProtect.get().changeWait.contains(this.region.getName()+flag)){								
										applyFlag(flag, item, event);	
										RPUtil.startFlagChanger(this.region.getName(), flag, this.player);								
									} else {
										RPLang.sendMessage(player,RPLang.get("gui.needwait.tochange").replace("{seconds}", RedProtect.get().cfgs.root().flags_configuration.change_flag_delay.seconds+""));
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
				}
			}
			
		}		
	}
	
	private void applyFlag(String flag, ItemStack item, ClickInventoryEvent event){				
		if (this.region.setFlag(RedProtect.get().getPVHelper().getCause(this.player), flag, !this.region.getFlagBool(flag))){
			RPLang.sendMessage(player, RPLang.get("cmdmanager.region.flag.set").replace("{flag}", "'"+flag+"'") + " " + this.region.getFlagBool(flag));

			if (!this.region.getFlagBool(flag)){
				item.remove(Keys.ITEM_ENCHANTMENTS);
			} else {
				item = RedProtect.get().getPVHelper().offerEnchantment(item);
			}
			item.offer(Keys.HIDE_ENCHANTMENTS, true);
			item.offer(Keys.HIDE_ATTRIBUTES, true);

			item.offer(Keys.ITEM_LORE, Arrays.asList(
					Text.of(RedProtect.get().cfgs.getGuiString("value"),RedProtect.get().cfgs.getGuiString(this.region.getFlagString(flag))),
					RPUtil.toText("&0"+flag),
					RPUtil.toText(RedProtect.get().cfgs.guiRoot().gui_flags.get(flag).description),
					RPUtil.toText(RedProtect.get().cfgs.guiRoot().gui_flags.get(flag).description1),
					RPUtil.toText(RedProtect.get().cfgs.guiRoot().gui_flags.get(flag).description2)));

			//RedProtect.get().logger.severe("Item Lore: "+item.get(Keys.ITEM_LORE).get().get(0).toPlain());

			event.getCursorTransaction().setCustom(ItemStackSnapshot.NONE);
			event.getTransactions().get(0).getSlot().offer(item);

			RedProtect.get().getPVHelper().removeGuiItem(this.player);

			RedProtect.get().logger.addLog("(World "+this.region.getWorld()+") Player "+player.getName()+" CHANGED flag "+flag+" of region "+this.region.getName()+" to "+this.region.getFlagString(flag));
		}
	}
	
	public void close(boolean close){
		RedProtect.get().getPVHelper().removeGuiItem(this.player);
		RedProtect.get().game.getEventManager().unregisterListeners(this);
		this.guiItens = null;
		if (close) RedProtect.get().getPVHelper().closeInventory(this.player);
		this.player = null;
		this.region = null;
		try {
			this.finalize();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	public void open(){
		RedProtect.get().getPVHelper().openInventory(this.inv, this.player);
	}
	
}