/*
 * Copyright (c) 2012-2024 - @FabioZumbi12
 * Last Modified: 26/11/2024 17:51
 *
 * This class is provided 'as-is', without any express or implied warranty. In no event will the authors be held liable for any
 *  damages arising from the use of this class.
 *
 * Permission is granted to anyone to use this class for any purpose, including commercial plugins, and to alter it and
 * redistribute it freely, subject to the following restrictions:
 * 1 - The origin of this class must not be misrepresented; you must not claim that you wrote the original software. If you
 * use this class in other plugins, an acknowledgment in the plugin documentation would be appreciated but is not required.
 * 2 - Altered source versions must be plainly marked as such, and must not be misrepresented as being the original class.
 * 3 - This notice may not be removed or altered from any source distribution.
 *
 * Esta classe é fornecida "como está", sem qualquer garantia expressa ou implícita. Em nenhum caso os autores serão
 * responsabilizados por quaisquer danos decorrentes do uso desta classe.
 *
 * É concedida permissão a qualquer pessoa para usar esta classe para qualquer finalidade, incluindo plugins pagos, e para
 * alterá-lo e redistribuí-lo livremente, sujeito às seguintes restrições:
 * 1 - A origem desta classe não deve ser deturpada; você não deve afirmar que escreveu a classe original. Se você usar esta
 *  classe em um plugin, uma confirmação de autoria na documentação do plugin será apreciada, mas não é necessária.
 * 2 - Versões de origem alteradas devem ser claramente marcadas como tal e não devem ser deturpadas como sendo a
 * classe original.
 * 3 - Este aviso não pode ser removido ou alterado de qualquer distribuição de origem.
 */

package br.net.fabiozumbi12.RedProtect.Bukkit.guis;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import br.net.fabiozumbi12.RedProtect.Core.config.CoreConfigManager;
import br.net.fabiozumbi12.RedProtect.Core.helpers.LogLevel;
import br.net.fabiozumbi12.RedProtect.Core.helpers.Replacer;
import java.lang.reflect.Method;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.bukkit.ChatColor.translateAlternateColorCodes;

public class FlagGui implements Listener {

    private final boolean editable;
    private final int size;
    private final Player player;
    private final String name;
    private final ItemStack[] guiItems;
    private final Region region;
    private Inventory inv;

    public FlagGui(String name, Player player, Region region, boolean editable, int maxSlots) {
        this.editable = editable;
        this.name = name;
        this.player = player;
        this.region = region;
        if (maxSlots <= 9) {
            this.size = 9;
        } else if (maxSlots <= 18) {
            this.size = 18;
        } else if (maxSlots <= 27) {
            this.size = 27;
        } else if (maxSlots <= 36) {
            this.size = 36;
        } else if (maxSlots <= 45) {
            this.size = 45;
        } else if (maxSlots <= 54) {
            this.size = 54;
        } else {
            throw new IllegalArgumentException("Parameter size is exceeding size limit (54)");
        }
        this.guiItems = new ItemStack[this.size];

        for (String flag : RedProtect.get().getConfigManager().getDefFlags()) {
            try {
                if (!RedProtect.get().getConfigManager().guiRoot().gui_flags.containsKey(flag)) {
                    continue;
                }
                if (RedProtect.get().getPermissionHandler().hasFlagPerm(player, flag) && (RedProtect.get().getConfigManager().configRoot().flags.containsKey(flag) || CoreConfigManager.ADMIN_FLAGS.contains(flag))) {
                    if (flag.equals("pvp") && !RedProtect.get().getConfigManager().configRoot().flags.containsKey("pvp")) {
                        continue;
                    }

                    int i = RedProtect.get().getConfigManager().getGuiSlot(flag);

                    Object flagValue = region.getFlags().get(flag);

                    String flagString;
                    if (flagValue instanceof Boolean) {
                        flagString = RedProtect.get().guiLang.getFlagString(flagValue.toString());
                    } else {
                        flagString = RedProtect.get().guiLang.getFlagString("list");
                    }

                    if (flag.equalsIgnoreCase("clan")) {
                        if (flagValue.toString().isEmpty()) {
                            flagString = RedProtect.get().guiLang.getFlagString("false");
                        } else {
                            flagString = RedProtect.get().guiLang.getFlagString("true");
                        }
                    }

                    this.guiItems[i] = new ItemStack(Material.getMaterial(RedProtect.get().getConfigManager().guiRoot().gui_flags.get(flag).material));
                    ItemMeta guiMeta = this.guiItems[i].getItemMeta();
                    guiMeta.setDisplayName(translateAlternateColorCodes('&', RedProtect.get().guiLang.getFlagName(flag)));
                    List<String> lore = new ArrayList<>(Arrays.asList(
                            translateAlternateColorCodes('&', RedProtect.get().guiLang.getFlagString("value") + " " + flagString),
                            "§0" + flag));
                    lore.addAll(RedProtect.get().guiLang.getFlagDescription(flag));
                    guiMeta.setLore(lore);
                    Enchantment enchType = Enchantment.getByName("DURABILITY") == null ? Enchantment.getByName("UNBREAKING") : Enchantment.getByName("DURABILITY");
                    if (flagValue.toString().equalsIgnoreCase("true")) {
                        guiMeta.addEnchant(enchType, 0, true);
                    } else {
                        guiMeta.removeEnchant(enchType);
                    }
                    guiMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    this.guiItems[i].setType(Material.getMaterial(RedProtect.get().getConfigManager().guiRoot().gui_flags.get(flag).material));
                    this.guiItems[i].setItemMeta(guiMeta);
                }
            } catch (Exception e) {
                this.player.sendMessage(ChatColor.RED + "Seems RedProtect have a wrong Item Gui or a problem on guiconfig for flag " + flag);
            }
        }

        for (int slotc = 0; slotc < this.size; slotc++) {
            if (this.guiItems[slotc] == null) {
                this.guiItems[slotc] = RedProtect.get().getConfigManager().getGuiSeparator();
            }
        }
    }

    @EventHandler
    void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getPlayer().equals(this.player)) {
            return;
        }

        if (this.editable) {
            for (int i = 0; i < this.size; i++) {
                try {
                    String flag = this.inv.getItem(i).getItemMeta().getLore().get(1).replace("§0", "");
                    if (RedProtect.get().getConfigManager().getDefFlags().contains(flag)) {
                        RedProtect.get().getConfigManager().setGuiSlot(/*this.inv.getItem(i).getType().name(),*/ flag, i);
                    }
                } catch (Exception e) {
                    RedProtect.get().getLanguageManager().sendMessage(this.player, "gui.edit.error");
                    close(false);
                    return;
                }
            }
            RedProtect.get().getConfigManager().saveGui();
            RedProtect.get().getLanguageManager().sendMessage(this.player, "gui.edit.ok");
        }

        close(false);
    }

    @EventHandler
    void onDeath(PlayerDeathEvent event) {
        if (event.getEntity().equals(this.player)) {
            close(true);
        }
    }

    @EventHandler
    void onPlayerLogout(PlayerQuitEvent event) {
        if (event.getPlayer().equals(this.player)) {
            close(true);
        }
    }

    @EventHandler
    void onPluginDisable(PluginDisableEvent event) {
        RedProtect.get().logger.debug(LogLevel.DEFAULT, "Is PluginDisableEvent event.");
        for (Player play : event.getPlugin().getServer().getOnlinePlayers()) {
            play.closeInventory();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof Player && holder.equals(this.player)) {

            if (this.editable) {
                return;
            }

            if (event.getInventory().equals(this.player.getOpenInventory().getTopInventory())) {
                event.setCancelled(true);
                ItemStack item = event.getCurrentItem();
                if (item != null && !item.equals(RedProtect.get().getConfigManager().getGuiSeparator()) && !item.getType().equals(Material.AIR) && event.getRawSlot() >= 0 && event.getRawSlot() <= this.size - 1) {
                    ItemMeta itemMeta = item.getItemMeta();
                    String flag = itemMeta.getLore().get(1).replace("§0", "");
                    if (RedProtect.get().getConfigManager().configRoot().flags_configuration.change_flag_delay.enable) {
                        if (RedProtect.get().getConfigManager().configRoot().flags_configuration.change_flag_delay.flags.contains(flag)) {
                            if (!RedProtect.get().changeWait.contains(this.region.getName() + flag)) {
                                applyFlag(flag, itemMeta, event);
                                RedProtect.get().getUtil().startFlagChanger(this.region.getName(), flag, player);
                            } else {
                                RedProtect.get().getLanguageManager().sendMessage(player, RedProtect.get().getLanguageManager().get("gui.needwait.tochange").replace("{seconds}", "" + RedProtect.get().getConfigManager().configRoot().flags_configuration.change_flag_delay.seconds));
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
    }

    private void applyFlag(String flag, ItemMeta itemMeta, InventoryClickEvent event) {
        Object flagValue = RedProtect.get().getUtil().parseObject(this.region.getFlagString(flag));

        if (flag.equalsIgnoreCase("clan")) {
            ClanPlayer cp = RedProtect.get().hooks.clanManager.getClanPlayer(this.player);
            if (this.region.getFlagString(flag).isEmpty()) {
                if (this.region.setFlag(this.player, flag, cp.getTag())) {
                    RedProtect.get().getLanguageManager().sendMessage(this.player, RedProtect.get().getLanguageManager().get("cmdmanager.region.flag.setclan").replace("{clan}", "'" + cp.getClan().getColorTag() + "'"));
                }
            } else {
                RedProtect.get().getLanguageManager().sendMessage(this.player, RedProtect.get().getLanguageManager().get("cmdmanager.region.flag.denyclan").replace("{clan}", "'" + this.region.getFlagString(flag) + "'"));
            }
        } else {
            if ((flag.equalsIgnoreCase("spawn-animals") ||
                    flag.equalsIgnoreCase("spawn-monsters")) &&
                    RedProtect.get().getPermissionHandler().hasPerm(this.player, "redprotect.flag.spawn-mob-gui")) {
                close(true);
                new MobFlagGui(this.player, this.region, flag).open();
                return;
            }
            if ((flag.equalsIgnoreCase("deny-enter-items") ||
                    flag.equalsIgnoreCase("allow-enter-items") ||
                    flag.equalsIgnoreCase("allow-place") ||
                    flag.equalsIgnoreCase("allow-break")) &&
                    RedProtect.get().getPermissionHandler().hasPerm(this.player, "redprotect.flag.item-gui")) {
                close(true);
                new ItemFlagGui(this.player, this.region, flag).open();
                return;
            }
            if (flagValue instanceof Boolean) {
                if (this.region.setFlag(this.player, flag, !this.region.getFlagBool(flag))) {
                    RedProtect.get().getLanguageManager().sendMessage(player, RedProtect.get().getLanguageManager().get("cmdmanager.region.flag.set").replace("{flag}", "'" + flag + "'") + " " + this.region.getFlagBool(flag));
                }
            }
        }

        Enchantment enchType = Enchantment.getByName("DURABILITY") == null ? Enchantment.getByName("UNBREAKING") : Enchantment.getByName("DURABILITY");
        if (this.region.getFlagBool(flag)) {
            itemMeta.addEnchant(enchType, 0, true);
        } else {
            itemMeta.removeEnchant(enchType);
        }
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        String flagString = RedProtect.get().guiLang.getFlagString(this.region.getFlagString(flag));

        List<String> lore = new ArrayList<>(Arrays.asList(
                translateAlternateColorCodes('&', RedProtect.get().guiLang.getFlagString("value") + " " + flagString),
                "§0" + flag));
        lore.addAll(RedProtect.get().guiLang.getFlagDescription(flag));
        itemMeta.setLore(lore);
        event.getCurrentItem().setItemMeta(itemMeta);

        RedProtect.get().logger.addLog("(World " + this.region.getWorld() + ") Player " + player.getName() + " CHANGED flag " + flag + " of region " + this.region.getName() + " to " + flagString);
    }

    private void close(boolean close) {
        //Unregister Listener
        HandlerList.unregisterAll(this);

        // Check for items
        this.player.updateInventory();
        Bukkit.getScheduler().runTaskLater(RedProtect.get(), this.player::updateInventory, 1);
        if (close) this.player.closeInventory();
    }

    public void open() {
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {

            /**
             * paper on 1.20.6 build 60+ changed the API to be a interface only, so we can abstract getTopInventory to get the class,
             * as describe by Rumsfield here https://www.spigotmc.org/threads/inventoryview-changed-to-interface-backwards-compatibility.651754/#post-4747875
             */
            Inventory topInv;
            try {
                InventoryView inventoryView = player.getOpenInventory();
                Method getTopInventory = inventoryView.getClass().getMethod("getTopInventory");
                topInv = (Inventory) getTopInventory.invoke(inventoryView);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            if (topInv.equals(this.inv)) {
                Region r = RedProtect.get().getRegionManager().getTopRegion(player.getLocation());
                if (r != null && r.equals(this.region) && !player.equals(this.player)) {
                    RedProtect.get().getLanguageManager().sendMessage(this.player, "cmdmanager.region.rpgui-other", new Replacer[]{new Replacer("{player}", player.getName())});
                    return;
                }
            }
        }
        //Register Listener
        RedProtect.get().getServer().getPluginManager().registerEvents(this, RedProtect.get());

        this.inv = Bukkit.createInventory(player, this.size, this.name);
        inv.setContents(this.guiItems);
        player.openInventory(inv);
    }

    public static Inventory getTopInventory(InventoryEvent event) {
        try {
            Object view = event.getView();
            Method getTopInventory = view.getClass().getMethod("getTopInventory");
            getTopInventory.setAccessible(true);
            return (Inventory) getTopInventory.invoke(view);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}