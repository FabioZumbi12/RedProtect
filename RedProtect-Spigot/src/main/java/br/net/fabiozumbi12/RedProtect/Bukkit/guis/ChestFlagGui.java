/*
 * Copyright (c) 2012-2025 - @FabioZumbi12
 * Last Modified: 13/10/2026
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
import br.net.fabiozumbi12.RedProtect.Core.helpers.LogLevel;
import org.bukkit.Bukkit;
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
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

import static org.bukkit.ChatColor.translateAlternateColorCodes;

public class ChestFlagGui implements Listener {

    private static final int GUI_SIZE = 54;
    private static final int ITEMS_PER_PAGE = 43;
    private static final int NAV_PREV_SLOT = 45;
    private static final int NAV_INFO_SLOT = 49;
    private static final int NAV_NEXT_SLOT = 53;

    public static final List<String> CONTAINER_BLOCKS = Collections.unmodifiableList(Arrays.asList(
            // Storage
            "CHEST",
            "TRAPPED_CHEST",
            "BARREL",
            "SHULKER_BOX",
            // Crafting & Workstations
            "CRAFTING_TABLE",
            "FURNACE",
            "BLAST_FURNACE",
            "SMOKER",
            "BREWING_STAND",
            "ANVIL",
            "CHIPPED_ANVIL",
            "DAMAGED_ANVIL",
            "SMITHING_TABLE",
            "STONECUTTER",
            "GRINDSTONE",
            "LOOM",
            "CARTOGRAPHY_TABLE",
            "FLETCHING_TABLE",
            "ENCHANTING_TABLE",
            // Redstone & Utility
            "DISPENSER",
            "DROPPER",
            "HOPPER",
            "BEACON",
            "JUKEBOX",
            "COMPOSTER",
            "CAULDRON",
            "CHISELED_BOOKSHELF",
            "CRAFTER",
            "CAMPFIRE",
            "SOUL_CAMPFIRE"
    ));

    private final String flag;
    private final Player player;
    private Region region;
    private final String name;
    private List<ItemStack> allContainerItems;
    private int currentPage = 0;
    private final int totalPages;

    public ChestFlagGui(Player player, Region region, String flag) {
        this.player = player;
        this.region = region;
        this.flag = flag;
        this.currentPage = 0;
        this.name = RedProtect.get().getLanguageManager().get("gui.title.chest");

        List<String> allBlocks = new ArrayList<>(CONTAINER_BLOCKS);
        List<String> customBlocks = RedProtect.get().getConfigManager().configRoot().flags_configuration.custom_containers;
        if (customBlocks != null) {
            for (String custom : customBlocks) {
                if (custom != null && !custom.trim().isEmpty() && !allBlocks.contains(custom.trim().toUpperCase())) {
                    allBlocks.add(custom.trim().toUpperCase());
                }
            }
        }

        List<Material> materials = allBlocks.stream()
                .map(Material::getMaterial)
                .filter(Objects::nonNull)
                .filter(Material::isItem)
                .collect(Collectors.toList());

        this.allContainerItems = buildAllContainerItems(materials, region.getFlagString(flag));
        this.totalPages = Math.max(1, (int) Math.ceil((double) allContainerItems.size() / ITEMS_PER_PAGE));
    }

    @EventHandler
    void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getView().getPlayer().equals(this.player)) {
            return;
        }

        StringBuilder str = new StringBuilder();
        String trueValue = translateAlternateColorCodes('&', RedProtect.get().guiLang.getFlagString("value") + " " + RedProtect.get().guiLang.getFlagString("true"));

        int selectedCount = 0;
        int totalItems = 0;
        for (ItemStack item : allContainerItems) {
            if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
                totalItems++;
                List<String> lore = item.getItemMeta().getLore();
                if (lore.get(0).equalsIgnoreCase(trueValue)) {
                    str.append(lore.get(1).replace("§0", "")).append(",");
                    selectedCount++;
                }
            }
        }

        Object value;
        if (selectedCount == 0) {
            value = false;
        } else if (selectedCount == totalItems) {
            value = true;
        } else {
            String s = str.toString();
            value = s.substring(0, s.length() - 1);
        }

        setFlagValue(value);
        close(false);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof Player && holder.equals(this.player)) {

            if (event.getInventory().equals(this.player.getOpenInventory().getTopInventory())) {
                event.setCancelled(true);

                int slot = event.getRawSlot();

                if (slot == 0) {
                    setFlagValue(true);
                    close(true);
                    return;
                }

                if (slot == 1) {
                    setFlagValue(false);
                    close(true);
                    return;
                }

                if (slot == NAV_PREV_SLOT && currentPage > 0) {
                    openPage(currentPage - 1);
                    return;
                }

                if (slot == NAV_NEXT_SLOT && currentPage < totalPages - 1) {
                    openPage(currentPage + 1);
                    return;
                }

                ItemStack item = event.getCurrentItem();
                if (item != null && !item.equals(RedProtect.get().getConfigManager().getGuiSeparator()) && !item.getType().equals(Material.AIR) && slot >= 2 && slot <= 44) {
                    ItemMeta itemMeta = item.getItemMeta();
                    if (itemMeta == null || !itemMeta.hasLore()) return;
                    List<String> lore = itemMeta.getLore();

                    String blockName = null;
                    if (lore.size() > 1 && lore.get(1).startsWith("§0")) {
                        blockName = lore.get(1).substring(2);
                    }

                    String trueVal = translateAlternateColorCodes('&', RedProtect.get().guiLang.getFlagString("value") + " " + RedProtect.get().guiLang.getFlagString("true"));
                    String falseVal = translateAlternateColorCodes('&', RedProtect.get().guiLang.getFlagString("value") + " " + RedProtect.get().guiLang.getFlagString("false"));

                    if (lore.get(0).equalsIgnoreCase(trueVal)) {
                        lore.set(0, falseVal);
                        itemMeta.removeEnchant(Enchantment.UNBREAKING);
                    } else {
                        lore.set(0, trueVal);
                        itemMeta.addEnchant(Enchantment.UNBREAKING, 0, true);
                        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    }
                    itemMeta.setLore(lore);
                    item.setItemMeta(itemMeta);

                    if (blockName != null) {
                        syncContainerItem(blockName, lore.get(0));
                    }
                }
            }
        }
    }

    @EventHandler
    void onDrag(InventoryDragEvent event) {
        if (!event.getView().getPlayer().equals(this.player)) {
            return;
        }
        if (event.getView().getTopInventory().equals(this.player.getOpenInventory().getTopInventory())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    void onCreative(InventoryCreativeEvent event) {
        if (!event.getWhoClicked().equals(this.player)) {
            return;
        }
        if (event.getInventory().equals(this.player.getOpenInventory().getTopInventory())) {
            event.setCancelled(true);
        }
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

    private void close(boolean close) {
        //Unregister Listener
        HandlerList.unregisterAll(this);

        // Check for items
        this.player.updateInventory();
        Bukkit.getScheduler().runTaskLater(RedProtect.get(), this.player::updateInventory, 1);
        if (close) this.player.closeInventory();

        this.allContainerItems = null;
        this.region = null;
    }

    public void open() {
        //Register Listener
        RedProtect.get().getServer().getPluginManager().registerEvents(this, RedProtect.get());
        openPage(0);
    }

    private void openPage(int page) {
        this.currentPage = page;
        ItemStack[] contents = buildPageContents(page);
        Inventory topInv = this.player.getOpenInventory().getTopInventory();
        if (topInv != null && topInv.getSize() == GUI_SIZE) {
            topInv.setContents(contents);
        } else {
            Inventory inv = Bukkit.createInventory(player, GUI_SIZE, this.name);
            inv.setContents(contents);
            player.openInventory(inv);
        }
    }

    private void setFlagValue(Object value) {
        region.setFlag(this.player, flag, value);
        RedProtect.get().getLanguageManager().sendMessage(player, RedProtect.get().getLanguageManager().get("cmdmanager.region.flag.set").replace("{flag}", "'" + flag + "'") + " " + region.getFlagString(flag));
        RedProtect.get().logger.addLog("(World " + region.getWorld() + ") Player " + player.getName() + " SET FLAG " + flag + " of region " + region.getName() + " to " + region.getFlagString(flag));
    }

    private List<ItemStack> buildAllContainerItems(List<Material> materials, String flagValue) {
        boolean isAll = flagValue.equalsIgnoreCase("true");
        boolean isNone = flagValue.equalsIgnoreCase("false");
        List<String> split = Arrays.stream(flagValue.trim().split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .collect(Collectors.toList());

        List<ItemStack> items = new ArrayList<>();

        for (Material mat : materials) {
            ItemStack item = new ItemStack(mat);
            ItemMeta itemMeta = item.getItemMeta();
            if (itemMeta == null) continue;

            boolean selected = isAll || (!isNone && split.contains(mat.name()));

            String valueStr;
            if (selected) {
                valueStr = RedProtect.get().guiLang.getFlagString("true");
                itemMeta.addEnchant(Enchantment.UNBREAKING, 0, true);
                itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            } else {
                valueStr = RedProtect.get().guiLang.getFlagString("false");
            }

            String display = translateAlternateColorCodes('&', "&6" + formatName(mat.name()));
            if (RedProtect.get().hooks.transAPI != null) {
                try {
                    String lang = RedProtect.get().getConfigManager().configRoot().language.toLowerCase();
                    String translated = RedProtect.get().hooks.transAPI.getApi().translateItem(mat, lang, true);
                    if (translated != null && !translated.isEmpty()) {
                        display = translateAlternateColorCodes('&', "&6" + translated);
                    }
                } catch (Throwable ignored) {
                }
            }
            itemMeta.setDisplayName(display);

            List<String> lore = new ArrayList<>(Arrays.asList(
                    translateAlternateColorCodes('&', RedProtect.get().guiLang.getFlagString("value") + " " + valueStr),
                    "§0" + mat.name()));
            itemMeta.setLore(lore);
            item.setItemMeta(itemMeta);
            items.add(item);
        }

        return items;
    }

    private ItemStack[] buildPageContents(int page) {
        ItemStack[] contents = new ItemStack[GUI_SIZE];

        Enchantment enchType = Enchantment.UNBREAKING;
        String flagValue = region.getFlagString(flag);

        // Slot 0: Select All
        ItemStack greenWool = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta greenMeta = greenWool.getItemMeta();
        if (flagValue.equalsIgnoreCase("true")) {
            greenMeta.addEnchant(enchType, 0, true);
            greenMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        greenMeta.setDisplayName(translateAlternateColorCodes('&', RedProtect.get().getLanguageManager().get("gui.selectall")));
        greenWool.setItemMeta(greenMeta);
        contents[0] = greenWool;

        // Slot 1: Select None
        ItemStack redWool = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta redMeta = redWool.getItemMeta();
        if (flagValue.equalsIgnoreCase("false")) {
            redMeta.addEnchant(enchType, 0, true);
            redMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        redMeta.setDisplayName(translateAlternateColorCodes('&', RedProtect.get().getLanguageManager().get("gui.selectnone")));
        redWool.setItemMeta(redMeta);
        contents[1] = redWool;

        // Slots 2-44: container items for this page
        int start = page * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, allContainerItems.size());
        int slot = 2;
        for (int i = start; i < end; i++) {
            contents[slot++] = allContainerItems.get(i);
        }

        // Navigation row (only if more than 1 page)
        if (totalPages > 1) {
            ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta fillerMeta = filler.getItemMeta();
            fillerMeta.setDisplayName(" ");
            filler.setItemMeta(fillerMeta);

            for (int i = NAV_PREV_SLOT; i <= NAV_NEXT_SLOT; i++) {
                contents[i] = filler;
            }

            if (page > 0) {
                ItemStack prev = new ItemStack(Material.ARROW);
                ItemMeta prevMeta = prev.getItemMeta();
                prevMeta.setDisplayName(RedProtect.get().getLanguageManager().get("gui.page.previous"));
                prev.setItemMeta(prevMeta);
                contents[NAV_PREV_SLOT] = prev;
            }

            ItemStack info = new ItemStack(Material.PAPER);
            ItemMeta infoMeta = info.getItemMeta();
            String pageInfo = RedProtect.get().getLanguageManager().get("gui.page.number")
                    .replace("{page}", String.valueOf(page + 1))
                    .replace("{total}", String.valueOf(totalPages));
            infoMeta.setDisplayName(pageInfo);
            info.setItemMeta(infoMeta);
            contents[NAV_INFO_SLOT] = info;

            if (page < totalPages - 1) {
                ItemStack next = new ItemStack(Material.ARROW);
                ItemMeta nextMeta = next.getItemMeta();
                nextMeta.setDisplayName(RedProtect.get().getLanguageManager().get("gui.page.next"));
                next.setItemMeta(nextMeta);
                contents[NAV_NEXT_SLOT] = next;
            }
        }

        return contents;
    }

    private void syncContainerItem(String blockName, String newLoreLine) {
        String hiddenName = "§0" + blockName;
        for (ItemStack item : allContainerItems) {
            if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
                List<String> lore = item.getItemMeta().getLore();
                if (lore.size() > 1 && lore.get(1).equals(hiddenName)) {
                    ItemMeta meta = item.getItemMeta();
                    List<String> newLore = meta.getLore();
                    newLore.set(0, newLoreLine);
                    meta.setLore(newLore);
                    boolean isTrue = newLoreLine.contains(RedProtect.get().guiLang.getFlagString("true"));
                    if (isTrue) {
                        meta.addEnchant(Enchantment.UNBREAKING, 0, true);
                        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    } else {
                        meta.removeEnchant(Enchantment.UNBREAKING);
                    }
                    item.setItemMeta(meta);
                    break;
                }
            }
        }
    }

    private static String formatName(String name) {
        return Arrays.stream(name.toLowerCase().split("_"))
                .map(s -> s.isEmpty() ? "" : Character.toUpperCase(s.charAt(0)) + s.substring(1))
                .collect(Collectors.joining(" "));
    }
}
