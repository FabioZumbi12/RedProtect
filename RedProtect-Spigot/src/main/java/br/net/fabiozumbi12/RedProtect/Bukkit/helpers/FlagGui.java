/*
 * Copyright (c) 2019 - @FabioZumbi12
 * Last Modified: 25/04/19 07:02
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

package br.net.fabiozumbi12.RedProtect.Bukkit.helpers;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import br.net.fabiozumbi12.RedProtect.Core.config.Category.FlagGuiCategory;
import br.net.fabiozumbi12.RedProtect.Core.helpers.CoreUtil;
import br.net.fabiozumbi12.RedProtect.Core.helpers.LogLevel;
import br.net.fabiozumbi12.RedProtect.Core.helpers.Replacer;
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
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.bukkit.ChatColor.translateAlternateColorCodes;

public class FlagGui implements Listener {

    private final boolean allowEnchant;
    private final boolean editable;
    private String name;
    private int size;
    private ItemStack[] guiItems;
    private Player player;
    private Region region;
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

        allowEnchant = RedProtect.get().bukkitVersion >= 181;

        for (String flag : region.getFlags().keySet()) {
            try {
                if (!(region.getFlags().get(flag) instanceof Boolean) || !RedProtect.get().config.guiRoot().gui_flags.containsKey(flag)) {
                    continue;
                }
                if (flag.equalsIgnoreCase("clan")) {
                    if (!RedProtect.get().hooks.simpleClans) {
                        continue;
                    }
                    ClanPlayer cp = RedProtect.get().hooks.clanManager.getClanPlayer(player);
                    if (cp == null || !cp.isLeader()) {
                        continue;
                    }
                }
                if ((RedProtect.get().config.getDefFlags().contains(flag) || RedProtect.get().ph.hasFlagPerm(player, flag))) {
                    if (flag.equals("pvp") && !RedProtect.get().config.configRoot().flags.containsKey("pvp")) {
                        continue;
                    }

                    int i = RedProtect.get().config.getGuiSlot(flag);

                    String fvalue;
                    if (flag.equalsIgnoreCase("clan")) {
                        if (region.getFlags().get(flag).toString().equals("")) {
                            fvalue = RedProtect.get().guiLang.getFlagString("false");
                        } else {
                            fvalue = RedProtect.get().guiLang.getFlagString("true");
                        }
                    } else {
                        fvalue = RedProtect.get().guiLang.getFlagString(region.getFlags().get(flag).toString());
                    }

                    this.guiItems[i] = new ItemStack(Material.getMaterial(RedProtect.get().config.guiRoot().gui_flags.get(flag).material));
                    ItemMeta guiMeta = this.guiItems[i].getItemMeta();
                    guiMeta.setDisplayName(translateAlternateColorCodes('&', RedProtect.get().guiLang.getFlagName(flag)));
                    List<String> lore =  new ArrayList<>(Arrays.asList(
                            translateAlternateColorCodes('&', RedProtect.get().guiLang.getFlagString("value") + " " + fvalue),
                            "§0" + flag));
                    lore.addAll(RedProtect.get().guiLang.getFlagDescription(flag));
                    guiMeta.setLore(lore);
                    if (allowEnchant) {
                        if (this.region.getFlagBool(flag)) {
                            guiMeta.addEnchant(Enchantment.DURABILITY, 0, true);
                        } else {
                            guiMeta.removeEnchant(Enchantment.DURABILITY);
                        }
                        guiMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    }
                    this.guiItems[i].setType(Material.getMaterial(RedProtect.get().config.guiRoot().gui_flags.get(flag).material));
                    this.guiItems[i].setItemMeta(guiMeta);
                }
            } catch (Exception e){
                this.player.sendMessage(ChatColor.RED + "Seems RedProtect have a wrong Item Gui or a problem on guiconfig for flag " + flag);
            }
        }

        for (int slotc = 0; slotc < this.size; slotc++) {
            if (this.guiItems[slotc] == null) {
                this.guiItems[slotc] = RedProtect.get().config.getGuiSeparator();
            }
        }
    }

    @EventHandler
    void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getInventory().equals(this.player.getOpenInventory().getTopInventory())) {
            return;
        }
        if (this.editable) {
            for (int i = 0; i < this.size; i++) {
                try {
                    String flag = this.inv.getItem(i).getItemMeta().getLore().get(1).replace("§0", "");
                    if (RedProtect.get().config.getDefFlags().contains(flag)) {
                        RedProtect.get().config.setGuiSlot(/*this.inv.getItem(i).getType().name(),*/ flag, i);
                    }
                } catch (Exception e) {
                    RedProtect.get().lang.sendMessage(this.player, "gui.edit.error");
                    close(false);
                    return;
                }
            }
            RedProtect.get().config.saveGui();
            RedProtect.get().lang.sendMessage(this.player, "gui.edit.ok");
        }
        close(false);
    }

    @EventHandler
    void onDeath(PlayerDeathEvent event) {
        if (event.getEntity().getName().equals(this.player.getName())) {
            close(true);
        }
    }

    @EventHandler
    void onPlayerLogout(PlayerQuitEvent event) {
        if (event.getPlayer().getName().equals(this.player.getName())) {
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

    @EventHandler(priority = EventPriority.LOWEST)
    void onInventoryClick(InventoryClickEvent event) {
        if (event.isCancelled() || !(event.getInventory().getHolder() instanceof Player) || !event.getInventory().equals(this.player.getOpenInventory().getTopInventory())) {
            return;
        }

        if (this.editable) {
            return;
        }

        if (event.getInventory().equals(this.player.getOpenInventory().getTopInventory())) {
            event.setCancelled(true);
            ItemStack item = event.getCurrentItem();
            if (item != null && !item.equals(RedProtect.get().config.getGuiSeparator()) && !item.getType().equals(Material.AIR) && event.getRawSlot() >= 0 && event.getRawSlot() <= this.size - 1) {
                ItemMeta itemMeta = item.getItemMeta();
                String flag = itemMeta.getLore().get(1).replace("§0", "");
                if (RedProtect.get().config.configRoot().flags_configuration.change_flag_delay.enable) {
                    if (RedProtect.get().config.configRoot().flags_configuration.change_flag_delay.flags.contains(flag)) {
                        if (!RedProtect.get().changeWait.contains(this.region.getName() + flag)) {
                            applyFlag(flag, itemMeta, event);
                            RedProtectUtil.startFlagChanger(this.region.getName(), flag, player);
                        } else {
                            RedProtect.get().lang.sendMessage(player, RedProtect.get().lang.get("gui.needwait.tochange").replace("{seconds}", ""+RedProtect.get().config.configRoot().flags_configuration.change_flag_delay.seconds));
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
            ClanPlayer cp = RedProtect.get().hooks.clanManager.getClanPlayer(p);
            if (this.region.getFlagString(flag).equals("")) {
                if (this.region.setFlag(this.player, flag, cp.getTag())) {
                    flagv = true;
                    RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("cmdmanager.region.flag.setclan").replace("{clan}", "'" + cp.getClan().getColorTag() + "'"));
                }
            } else {
                RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("cmdmanager.region.flag.denyclan").replace("{clan}", "'" + this.region.getFlagString(flag) + "'"));
            }
        } else {
            flagv = !this.region.getFlagBool(flag);
            if (this.region.setFlag(this.player, flag, flagv)) {
                RedProtect.get().lang.sendMessage(player, RedProtect.get().lang.get("cmdmanager.region.flag.set").replace("{flag}", "'" + flag + "'") + " " + flagv);
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
        List<String> lore =  new ArrayList<>(Arrays.asList(
                translateAlternateColorCodes('&', RedProtect.get().guiLang.getFlagString("value") + " " + RedProtect.get().guiLang.getFlagString(String.valueOf(flagv))),
                "§0" + flag));
        lore.addAll(RedProtect.get().guiLang.getFlagDescription(flag));
        itemMeta.setLore(lore);
        event.getCurrentItem().setItemMeta(itemMeta);

        RedProtect.get().logger.addLog("(World " + this.region.getWorld() + ") Player " + player.getName() + " CHANGED flag " + flag + " of region " + this.region.getName() + " to " + flagv);
    }

    private void close(boolean close) {
        //Unregister Listener
        HandlerList.unregisterAll(this);

        // Check for items
        this.player.updateInventory();
        Bukkit.getScheduler().runTaskLater(RedProtect.get(), () -> this.player.updateInventory(), 1);
        if (close) this.player.closeInventory();

        this.guiItems = null;
        this.name = null;
        this.region = null;
    }

    public void open() {
        for (Player player:Bukkit.getServer().getOnlinePlayers()){
            if (player.getOpenInventory().getTopInventory().equals(this.inv)){
                Region r = RedProtect.get().rm.getTopRegion(player.getLocation());
                if (r != null && r.equals(this.region) && !player.equals(this.player)){
                    RedProtect.get().lang.sendMessage(this.player, "cmdmanager.region.rpgui-other", new Replacer[]{new Replacer("{player}",player.getName())});
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
}