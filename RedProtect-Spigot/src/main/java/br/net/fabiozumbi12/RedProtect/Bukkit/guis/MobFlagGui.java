/*
 * Copyright (c) 2012-2025 - @FabioZumbi12
 * Last Modified: 18/01/2025 16:00
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
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.bukkit.ChatColor.translateAlternateColorCodes;

public class MobFlagGui implements Listener {

    private static final int GUI_SIZE = 54;
    private static final int MOBS_PER_PAGE = 43;
    private static final int NAV_PREV_SLOT = 45;
    private static final int NAV_INFO_SLOT = 49;
    private static final int NAV_NEXT_SLOT = 53;

    private final String flag;
    private final Player player;
    private Region region;
    private String name;
    private List<ItemStack> allMobItems;
    private int currentPage = 0;
    private int totalPages;

    public MobFlagGui(Player player, Region region, String flag) {
        this.player = player;
        this.region = region;
        this.flag = flag;
        this.currentPage = 0;

        List<EntityType> entities = new ArrayList<>();

        if (flag.equalsIgnoreCase("spawn-monsters")) {
            this.name = "Spawn Monsters";
            entities = Registry.ENTITY_TYPE.stream()
                    .filter(ent -> ent.getEntityClass() != null &&
                            ent.getKey().getNamespace().startsWith("minecraft") &&
                            Monster.class.isAssignableFrom(ent.getEntityClass()))
                    .sorted(Comparator.comparing(EntityType::name)).collect(toList());
            List<EntityType> modEntities = Registry.ENTITY_TYPE.stream()
                    .filter(ent ->
                            !ent.getKey().getNamespace().startsWith("minecraft") &&
                            RedProtect.get().getConfigManager().configRoot().flags_configuration.modEntities.monsters.contains(ent.name()))
                    .sorted(Comparator.comparing(EntityType::name)).toList();
            entities.addAll(modEntities);
        }

        if (flag.equalsIgnoreCase("spawn-animals")) {
            this.name = "Spawn Animals";
            entities = Registry.ENTITY_TYPE.stream()
                    .filter(ent -> {
                        Class<? extends Entity> entityClass = ent.getEntityClass();
                        if (entityClass == null) return false;
                        return (ent.getKey().getNamespace().startsWith("minecraft") &&
                                !Monster.class.isAssignableFrom(entityClass) &&
                                !Player.class.isAssignableFrom(entityClass) &&
                                !ArmorStand.class.isAssignableFrom(entityClass) &&
                                LivingEntity.class.isAssignableFrom(entityClass));
                    })
                    .sorted(Comparator.comparing(EntityType::name)).collect(toList());
            List<EntityType> modEntities = Registry.ENTITY_TYPE.stream()
                    .filter(ent ->
                            !ent.getKey().getNamespace().startsWith("minecraft") &&
                                    RedProtect.get().getConfigManager().configRoot().flags_configuration.modEntities.animals.contains(ent.name()))
                    .sorted(Comparator.comparing(EntityType::name)).toList();
            entities.addAll(modEntities);
        }

        boolean monster = flag.equalsIgnoreCase("spawn-monsters");
        this.allMobItems = buildAllMobItems(entities, region.getFlagString(flag), monster);
        this.totalPages = Math.max(1, (int) Math.ceil((double) allMobItems.size() / MOBS_PER_PAGE));
    }

    @EventHandler
    void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getView().getPlayer().equals(this.player)) {
            return;
        }

        StringBuilder str = new StringBuilder();
        String trueValue = translateAlternateColorCodes('&', RedProtect.get().guiLang.getFlagString("value") + " " + RedProtect.get().guiLang.getFlagString("true"));

        for (ItemStack item : allMobItems) {
            if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
                List<String> lore = item.getItemMeta().getLore();
                if (lore.get(0).equalsIgnoreCase(trueValue)) {
                    str.append(lore.get(1).replace("§0", "")).append(",");
                }
            }
        }

        String value = str.toString();
        if (!value.isEmpty()) {
            value = value.substring(0, value.length() - 1);
        } else {
            value = "false";
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
                    List<String> lore = itemMeta.getLore();

                    String entityName = null;
                    if (lore.size() > 1 && lore.get(1).startsWith("§0")) {
                        entityName = lore.get(1).substring(2);
                    }

                    if (lore.get(0).equalsIgnoreCase(translateAlternateColorCodes('&', RedProtect.get().guiLang.getFlagString("value") + " " + RedProtect.get().guiLang.getFlagString("true")))) {
                        lore.set(0, translateAlternateColorCodes('&', RedProtect.get().guiLang.getFlagString("value") + " " + RedProtect.get().guiLang.getFlagString("false")));
                        item.setAmount(1);
                    } else {
                        lore.set(0, translateAlternateColorCodes('&', RedProtect.get().guiLang.getFlagString("value") + " " + RedProtect.get().guiLang.getFlagString("true")));
                        item.setAmount(2);
                    }
                    itemMeta.setLore(lore);
                    item.setItemMeta(itemMeta);

                    if (entityName != null) {
                        syncMobItem(entityName, lore.get(0));
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
        // Cancel any drag that involves the top inventory (our GUI)
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

        this.allMobItems = null;
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

    private List<ItemStack> buildAllMobItems(List<EntityType> entities, String flagValue, boolean monster) {
        List<String> split = Arrays.asList(flagValue.trim().split(","));
        List<ItemStack> items = new ArrayList<>();

        for (EntityType ent : entities) {
            ItemStack head;
            try {
                var texture = RedProtect.get().getConfigManager().headTextRoot().mobTextures.get(ent.name());
                try {
                    head = RedProtect.get().getUtil().createSkullOld(texture);
                } catch (Exception ex) {
                    head = RedProtect.get().getUtil().createSkull(texture);
                }
            } catch (Exception ex) {
                head = new ItemStack(monster ? Material.MAGMA_CREAM : Material.BONE);
                RedProtect.get().logger.log("Error on open GUI: " + ex.getMessage());
            }

            ItemMeta itemMeta = head.getItemMeta();
            String valueStr = RedProtect.get().guiLang.getFlagString("false");
            if (split.contains(ent.name())) {
                valueStr = RedProtect.get().guiLang.getFlagString("true");
                head.setAmount(2);
            }

            String display = translateAlternateColorCodes('&', "&6" + ent.name());
            if (RedProtect.get().hooks.transAPI != null) {
                display = translateAlternateColorCodes('&', "&6" + RedProtect.get().hooks.transAPI.getApi().translateEntity(ent, "en-us", true));
            }
            itemMeta.setDisplayName(display);

            List<String> lore = new ArrayList<>(Arrays.asList(
                    translateAlternateColorCodes('&', RedProtect.get().guiLang.getFlagString("value") + " " + valueStr),
                    "§0" + ent.name()));
            itemMeta.setLore(lore);
            head.setItemMeta(itemMeta);
            items.add(head);
        }

        return items;
    }

    private ItemStack[] buildPageContents(int page) {
        ItemStack[] contents = new ItemStack[GUI_SIZE];

        Enchantment enchType = Enchantment.getByName("DURABILITY") == null ? Enchantment.getByName("UNBREAKING") : Enchantment.getByName("DURABILITY");
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

        // Slots 2-44: mob items for this page
        int start = page * MOBS_PER_PAGE;
        int end = Math.min(start + MOBS_PER_PAGE, allMobItems.size());
        int slot = 2;
        for (int i = start; i < end; i++) {
            contents[slot++] = allMobItems.get(i);
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
                prevMeta.setDisplayName(translateAlternateColorCodes('&', "&ePágina Anterior"));
                prev.setItemMeta(prevMeta);
                contents[NAV_PREV_SLOT] = prev;
            }

            ItemStack info = new ItemStack(Material.PAPER);
            ItemMeta infoMeta = info.getItemMeta();
            infoMeta.setDisplayName(translateAlternateColorCodes('&', "&ePágina " + (page + 1) + "/" + totalPages));
            info.setItemMeta(infoMeta);
            contents[NAV_INFO_SLOT] = info;

            if (page < totalPages - 1) {
                ItemStack next = new ItemStack(Material.ARROW);
                ItemMeta nextMeta = next.getItemMeta();
                nextMeta.setDisplayName(translateAlternateColorCodes('&', "&ePróxima Página"));
                next.setItemMeta(nextMeta);
                contents[NAV_NEXT_SLOT] = next;
            }
        }

        return contents;
    }

    private void syncMobItem(String entityName, String newLoreLine) {
        String hiddenName = "§0" + entityName;
        for (ItemStack mobItem : allMobItems) {
            if (mobItem != null && mobItem.hasItemMeta() && mobItem.getItemMeta().hasLore()) {
                List<String> mobLore = mobItem.getItemMeta().getLore();
                if (mobLore.size() > 1 && mobLore.get(1).equals(hiddenName)) {
                    ItemMeta meta = mobItem.getItemMeta();
                    List<String> lore = meta.getLore();
                    lore.set(0, newLoreLine);
                    meta.setLore(lore);
                    mobItem.setAmount(newLoreLine.contains(RedProtect.get().guiLang.getFlagString("true")) ? 2 : 1);
                    mobItem.setItemMeta(meta);
                    break;
                }
            }
        }
    }
}