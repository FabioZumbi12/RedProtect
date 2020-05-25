/*
 * Copyright (c) 2019 - @FabioZumbi12
 * Last Modified: 07/06/19 02:37.
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
import br.net.fabiozumbi12.RedProtect.Core.helpers.LogLevel;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class ItemFlagGui implements Listener {

    private final String flag;
    private ItemStack[] guiItems;
    private final Player player;
    private Region region;

    public ItemFlagGui(Player player, Region region, String flag) {
        this.player = player;
        this.region = region;
        this.flag = flag;

        this.guiItems = Arrays.stream(region.getFlagString(flag).replace(" ", "").toUpperCase().split(",")).sorted()
                .filter(item -> Arrays.stream(Material.values()).anyMatch(i -> i.name().equals(item)))
                .map(i -> new ItemStack(Material.valueOf(i))).toArray(ItemStack[]::new);
    }

    @EventHandler
    void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getView().getTitle().equals(this.player.getOpenInventory().getTitle())) {
            return;
        }

        StringBuilder str = new StringBuilder();
        Arrays.stream(event.getInventory().getContents())
                .filter(item -> item != null && !item.getType().equals(Material.AIR))
                .map(item -> item.getType().name()).distinct()
                .forEach(item -> str.append(item).append(","));

        if (str.length() > 0) {
            region.setFlag(event.getPlayer(), flag, str.toString().substring(0, str.toString().length() - 1));
            RedProtect.get().lang.sendMessage(player, RedProtect.get().lang.get("cmdmanager.region.flag.set").replace("{flag}", "'" + flag + "'") + " " + region.getFlagString(flag));
            RedProtect.get().logger.addLog("(World " + region.getWorld() + ") Player " + player.getName() + " SET FLAG " + flag + " of region " + region.getName() + " to " + region.getFlagString(flag));
        } else {
            region.removeFlag(flag);
            RedProtect.get().lang.sendMessage(player, RedProtect.get().lang.get("cmdmanager.region.flag.removed").replace("{flag}", flag).replace("{region}", region.getName()));
            RedProtect.get().logger.addLog("(World " + region.getWorld() + ") Player " + player.getName() + " REMOVED FLAG " + flag + " of region " + region.getName());
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

    private void close(boolean close) {
        //Unregister Listener
        HandlerList.unregisterAll(this);

        // Check for items
        this.player.updateInventory();
        Bukkit.getScheduler().runTaskLater(RedProtect.get(), () -> this.player.updateInventory(), 1);
        if (close) this.player.closeInventory();

        this.guiItems = null;
        this.region = null;
    }

    public void open() {
        //Register Listener
        RedProtect.get().getServer().getPluginManager().registerEvents(this, RedProtect.get());

        Inventory inv = Bukkit.createInventory(player, 54, "Item flag GUI");
        inv.setContents(this.guiItems);
        player.openInventory(inv);
    }
}