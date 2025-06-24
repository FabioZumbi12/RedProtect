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

    private final String flag;
    private final Player player;
    private final int size;
    private ItemStack[] guiItems = new ItemStack[0];
    private Region region;
    private String name;

    public MobFlagGui(Player player, Region region, String flag) {
        this.player = player;
        this.region = region;
        this.flag = flag;

        if (flag.equalsIgnoreCase("spawn-monsters")) {
            this.name = "Spawn Monsters";
            List<EntityType> entities = Registry.ENTITY_TYPE.stream()
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
            this.guiItems = getItemList(entities, region.getFlagString(flag), true);
        }

        if (flag.equalsIgnoreCase("spawn-animals")) {
            this.name = "Spawn Animals";
            List<EntityType> entities = Registry.ENTITY_TYPE.stream()
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
            this.guiItems = getItemList(entities, region.getFlagString(flag), false);
        }

        int maxSlots = this.guiItems.length;
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
    }

    @EventHandler
    void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getView().getPlayer().equals(this.player)) {
            return;
        }

        StringBuilder str = new StringBuilder();
        Arrays.stream(event.getInventory().getContents())
                .filter(item -> item != null && !item.getType().equals(Material.AIR) && item.getItemMeta().hasLore())
                .map(item -> item.getItemMeta().getLore())
                .forEach(lore -> {
                    if (lore.get(0).equalsIgnoreCase(translateAlternateColorCodes('&', RedProtect.get().guiLang.getFlagString("value") + " " + RedProtect.get().guiLang.getFlagString("true"))))
                        str.append(lore.get(1).replace("§0", "")).append(",");
                });

        String value = str.toString();
        if (!value.isEmpty()) {
            value = value.substring(0, str.toString().length() - 1);
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

                if (event.getRawSlot() == 0) {
                    setFlagValue(true);
                    close(true);
                    return;
                }

                if (event.getRawSlot() == 1) {
                    setFlagValue(false);
                    close(true);
                    return;
                }

                ItemStack item = event.getCurrentItem();
                if (item != null && !item.equals(RedProtect.get().getConfigManager().getGuiSeparator()) && !item.getType().equals(Material.AIR) && event.getRawSlot() >= 0 && event.getRawSlot() <= this.size - 1) {
                    ItemMeta itemMeta = item.getItemMeta();
                    List<String> lore = itemMeta.getLore();
                    if (lore.get(0).equalsIgnoreCase(translateAlternateColorCodes('&', RedProtect.get().guiLang.getFlagString("value") + " " + RedProtect.get().guiLang.getFlagString("true")))) {
                        lore.set(0, translateAlternateColorCodes('&', RedProtect.get().guiLang.getFlagString("value") + " " + RedProtect.get().guiLang.getFlagString("false")));
                        item.setAmount(1);
                    } else {
                        lore.set(0, translateAlternateColorCodes('&', RedProtect.get().guiLang.getFlagString("value") + " " + RedProtect.get().guiLang.getFlagString("true")));
                        item.setAmount(2);
                    }
                    itemMeta.setLore(lore);
                    item.setItemMeta(itemMeta);
                }
            }
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

        this.guiItems = null;
        this.region = null;
    }

    public void open() {
        //Register Listener
        RedProtect.get().getServer().getPluginManager().registerEvents(this, RedProtect.get());

        Inventory inv = Bukkit.createInventory(player, this.size, this.name);
        inv.setContents(this.guiItems);
        player.openInventory(inv);
    }

    private void setFlagValue(Object value) {
        region.setFlag(this.player, flag, value);
        RedProtect.get().getLanguageManager().sendMessage(player, RedProtect.get().getLanguageManager().get("cmdmanager.region.flag.set").replace("{flag}", "'" + flag + "'") + " " + region.getFlagString(flag));
        RedProtect.get().logger.addLog("(World " + region.getWorld() + ") Player " + player.getName() + " SET FLAG " + flag + " of region " + region.getName() + " to " + region.getFlagString(flag));
    }

    private ItemStack[] getItemList(List<EntityType> entities, String flagValue, boolean monster) {
        List<String> split = Arrays.asList(flagValue.trim().split(","));
        List<ItemStack> items = new ArrayList<>();

        ItemStack greenWool = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta greenMeta = greenWool.getItemMeta();

        Enchantment enchType = Enchantment.getByName("DURABILITY") == null ? Enchantment.getByName("UNBREAKING") : Enchantment.getByName("DURABILITY");

        if (flagValue.equalsIgnoreCase("true")) {
            greenMeta.addEnchant(enchType, 0, true);
            greenMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        greenMeta.setDisplayName(translateAlternateColorCodes('&', RedProtect.get().getLanguageManager().get("gui.selectall")));
        greenWool.setItemMeta(greenMeta);
        items.add(greenWool);

        ItemStack redWool = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta redMeta = redWool.getItemMeta();
        if (flagValue.equalsIgnoreCase("false")) {
            redMeta.addEnchant(enchType, 0, true);
            redMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        redMeta.setDisplayName(translateAlternateColorCodes('&', RedProtect.get().getLanguageManager().get("gui.selectnone")));
        redWool.setItemMeta(redMeta);
        items.add(redWool);

        for (EntityType ent : entities) {
            ItemStack head;
            try {
                var texture = RedProtect.get().getConfigManager().headTextRoot().mobTextures.get(ent.name());
                try{
                    head = RedProtect.get().getUtil().createSkullOld(texture);
                } catch (Exception ex){
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

            // Item name
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

        List<ItemStack> subList = items;
        if (items.size() > 54)
            subList = items.subList(0, 54);

        ItemStack[] listItems = new ItemStack[subList.size()];
        return subList.toArray(listItems);
    }
}