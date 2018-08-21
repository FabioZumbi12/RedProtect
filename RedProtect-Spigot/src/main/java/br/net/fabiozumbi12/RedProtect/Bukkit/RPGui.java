package br.net.fabiozumbi12.RedProtect.Bukkit;

import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPConfig;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPLang;
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

import java.util.Arrays;

public class RPGui implements Listener {

    private String name;
    private int size;
    private ItemStack[] guiItens;
    private Player player;
    private Region region;
    private final boolean allowEnchant;
    private final boolean edit;
    private Inventory inv;

    public RPGui(String name, Player player, Region region, boolean edit, int MaxSlot) {
        this.edit = edit;
        this.name = name;
        this.player = player;
        this.region = region;

        if (MaxSlot <= 9) {
            this.size = 9;
            this.guiItens = new ItemStack[this.size];
        } else if (MaxSlot >= 10 && MaxSlot <= 18) {
            this.size = 18;
            this.guiItens = new ItemStack[this.size];
        } else if (MaxSlot >= 19 && MaxSlot <= 27) {
            this.size = 27;
            this.guiItens = new ItemStack[this.size];
        }
        if (MaxSlot >= 28 && MaxSlot <= 36) {
            this.size = 36;
            this.guiItens = new ItemStack[this.size];
        }
        if (MaxSlot >= 37 && MaxSlot <= 45) {
            this.size = 45;
            this.guiItens = new ItemStack[this.size];
        }
        if (MaxSlot >= 46 && MaxSlot <= 54) {
            this.size = 54;
            this.guiItens = new ItemStack[this.size];
        }

        allowEnchant = RedProtect.get().version >= 181;

        for (String flag : region.getFlags().keySet()) {
            if (!(region.getFlags().get(flag) instanceof Boolean) && !flag.equalsIgnoreCase("clan")) {
                continue;
            }
            if (flag.equalsIgnoreCase("clan")) {
                if (!RedProtect.get().SC) {
                    continue;
                }
                ClanPlayer cp = RedProtect.get().clanManager.getClanPlayer(player);
                if (cp == null || !cp.isLeader()) {
                    continue;
                }
            }
            if (RedProtect.get().ph.hasPerm(player, "redprotect.flag." + flag) && Material.getMaterial(RPConfig.getGuiFlagString(flag, "material")) != null && RPConfig.isFlagEnabled(flag)) {
                if (flag.equals("pvp") && !RedProtect.get().getConfig().getStringList("flags-configuration.enabled-flags").contains("pvp")) {
                    continue;
                }

                int i = RPConfig.getGuiSlot(flag);

                String fvalue;
                if (flag.equalsIgnoreCase("clan")) {
                    if (region.getFlags().get(flag).toString().equals("")) {
                        fvalue = RPConfig.getGuiString("false");
                    } else {
                        fvalue = RPConfig.getGuiString("true");
                    }
                } else {
                    fvalue = RPConfig.getGuiString(region.getFlags().get(flag).toString());
                }

                this.guiItens[i] = RPConfig.getGuiItemStack(flag);
				/*
				if (RedProtect.get().PLib && allowEnchant){
					this.guiItens[i] = RPProtocolLib.removeAttributes(RPConfig.getGuiItemStack(flag));
				} else {
					this.guiItens[i] = RPConfig.getGuiItemStack(flag);					
				}*/
                ItemMeta guiMeta = this.guiItens[i].getItemMeta();
                guiMeta.setDisplayName(RPConfig.getGuiFlagString(flag, "name"));
                guiMeta.setLore(Arrays.asList(RPConfig.getGuiString("value") + fvalue, "§0" + flag, RPConfig.getGuiFlagString(flag, "description"), RPConfig.getGuiFlagString(flag, "description1"), RPConfig.getGuiFlagString(flag, "description2")));
                if (allowEnchant) {
                    if (this.region.getFlagBool(flag)) {
                        guiMeta.addEnchant(Enchantment.DURABILITY, 0, true);
                    } else {
                        guiMeta.removeEnchant(Enchantment.DURABILITY);
                    }
                    guiMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }
                this.guiItens[i].setType(Material.getMaterial(RPConfig.getGuiFlagString(flag, "material")));
                this.guiItens[i].setItemMeta(guiMeta);
            }
        }

        for (int slotc = 0; slotc < this.size; slotc++) {
            if (this.guiItens[slotc] == null) {
                this.guiItens[slotc] = RPConfig.getGuiSeparator();
            }
        }

        RedProtect.get().getServer().getPluginManager().registerEvents(this, RedProtect.get());
    }

    @EventHandler
    void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getTitle() != null && event.getInventory().getTitle().equals(this.name)) {
            if (this.edit) {
                for (int i = 0; i < this.size; i++) {
                    try {
                        String flag = this.inv.getItem(i).getItemMeta().getLore().get(1).replace("§0", "");
                        if (RPConfig.getDefFlags().contains(flag)) {
                            RPConfig.setGuiSlot(/*this.inv.getItem(i).getType().name(),*/ flag, i);
                        }
                    } catch (Exception e) {
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
    void onDeath(PlayerDeathEvent event) {
        if (event.getEntity().getOpenInventory().getTitle().equals(this.name)) {
            close();
        }
    }

    @EventHandler
    void onPlayerLogout(PlayerQuitEvent event) {
        if (event.getPlayer().getInventory().getTitle().equals(this.name)) {
            close();
        }
    }

    @EventHandler
    void onPluginDisable(PluginDisableEvent event) {
        RedProtect.get().logger.debug("Is PluginDisableEvent event.");
        for (Player play : event.getPlugin().getServer().getOnlinePlayers()) {
            play.closeInventory();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    void onInventoryClick(InventoryClickEvent event) {
        if (event.isCancelled() || !(event.getInventory().getHolder() instanceof Player) || !event.getInventory().getTitle().equals(this.name)) {
            return;
        }

        if (this.edit) {
            return;
        }

        if (event.getInventory().getTitle() != null && event.getInventory().getTitle().equals(this.name)) {
            event.setCancelled(true);
            ItemStack item = event.getCurrentItem();
            if (item != null && !item.equals(RPConfig.getGuiSeparator()) && !item.getType().equals(Material.AIR) && event.getRawSlot() >= 0 && event.getRawSlot() <= this.size - 1) {
                ItemMeta itemMeta = item.getItemMeta();
                String flag = itemMeta.getLore().get(1).replace("§0", "");
                if (RPConfig.getBool("flags-configuration.change-flag-delay.enable")) {
                    if (RPConfig.getStringList("flags-configuration.change-flag-delay.flags").contains(flag)) {
                        if (!RedProtect.get().changeWait.contains(this.region.getName() + flag)) {
                            applyFlag(flag, itemMeta, event);
                            RPUtil.startFlagChanger(this.region.getName(), flag, player);
                        } else {
                            RPLang.sendMessage(player, RPLang.get("gui.needwait.tochange").replace("{seconds}", RPConfig.getString("flags-configuration.change-flag-delay.seconds")));
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

    private void applyFlag(String flag, ItemMeta itemMeta, InventoryClickEvent event) {
        boolean flagv = false;
        if (flag.equalsIgnoreCase("clan")) {
            Player p = (Player) event.getInventory().getHolder();
            ClanPlayer cp = RedProtect.get().clanManager.getClanPlayer(p);
            if (this.region.getFlagString(flag).equals("")) {
                if (this.region.setFlag(this.player, flag, cp.getTag())) {
                    flagv = true;
                    RPLang.sendMessage(p, RPLang.get("cmdmanager.region.flag.setclan").replace("{clan}", "'" + cp.getClan().getColorTag() + "'"));
                }
            } else {
                RPLang.sendMessage(p, RPLang.get("cmdmanager.region.flag.denyclan").replace("{clan}", "'" + this.region.getFlagString(flag) + "'"));
                if (this.region.setFlag(this.player, flag, "")) {
                    flagv = false;
                }
            }
        } else {
            flagv = !this.region.getFlagBool(flag);
            if (this.region.setFlag(this.player, flag, flagv)) {
                RPLang.sendMessage(player, RPLang.get("cmdmanager.region.flag.set").replace("{flag}", "'" + flag + "'") + " " + flagv);
            }
        }

        if (allowEnchant) {
            if (flagv) {
                itemMeta.addEnchant(Enchantment.DURABILITY, 0, true);
            } else {
                itemMeta.removeEnchant(Enchantment.DURABILITY);
            }
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        itemMeta.setLore(Arrays.asList(RPConfig.getGuiString("value") + RPConfig.getGuiString(String.valueOf(flagv)), "§0" + flag, RPConfig.getGuiFlagString(flag, "description"), RPConfig.getGuiFlagString(flag, "description1"), RPConfig.getGuiFlagString(flag, "description2")));
        event.getCurrentItem().setItemMeta(itemMeta);
        RedProtect.get().logger.addLog("(World " + this.region.getWorld() + ") Player " + player.getName() + " CHANGED flag " + flag + " of region " + this.region.getName() + " to " + String.valueOf(flagv));
    }

    public void close() {
        //check for itens
        this.player.updateInventory();
        Bukkit.getScheduler().runTaskLater(RedProtect.get(), () -> this.player.updateInventory(), 1);

        this.guiItens = null;
        this.name = null;
        RedProtect.get().openGuis.remove(this.region.getID());
        this.region = null;
        try {
            this.finalize();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void open() {
        if (RedProtect.get().openGuis.contains(this.region.getID())) {
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
        RedProtect.get().openGuis.add(this.region.getID());
    }
}