/*
 * Copyright (c) 2019 - @FabioZumbi12
 * Last Modified: 07/06/19 02:40.
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

package br.net.fabiozumbi12.RedProtect.Sponge.helpers;

import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import br.net.fabiozumbi12.RedProtect.Sponge.Region;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Arrays;
import java.util.stream.StreamSupport;


public class ItemFlagGui {

    private String flag;
    private ItemStack[] guiItems;
    private Player player;
    private Region region;

    public ItemFlagGui(Player player, Region region, String flag) {
        this.flag = flag;
        this.player = player;
        this.region = region;

        this.guiItems = Arrays.stream(region.getFlagString(flag).split(",")).sorted()
                .filter(item -> Sponge.getRegistry().getType(ItemType.class, item.toUpperCase()).isPresent())
                .map(i -> ItemStack.of(Sponge.getRegistry().getType(ItemType.class, i).get(), 1)).toArray(ItemStack[]::new);
    }

    @Listener
    public void onCloseInventory(InteractInventoryEvent.Close event) {
        if (event.getTargetInventory().getName().get().equals(this.player.getInventory().getName().get())) {
            return;
        }

        StringBuilder str = new StringBuilder();

        event.getTargetInventory().forEach(inv -> {
            if (inv.capacity() == 54) {
                StreamSupport.stream(inv.slots().spliterator(), false)
                        .filter(i -> i.peek().isPresent()).map(i -> i.peek().get().getItem().getName()).distinct()
                        .forEach(i -> str.append(i).append(","));
            }
        });

        if (str.length() > 0) {
            region.setFlag(RedProtect.get().getVersionHelper().getCause(this.player), flag, str.toString().substring(0, str.toString().length() - 1));
            RedProtect.get().lang.sendMessage(player, RedProtect.get().lang.get("cmdmanager.region.flag.set").replace("{flag}", "'" + flag + "'") + " " + region.getFlagString(flag));
            RedProtect.get().logger.addLog("(World " + region.getWorld() + ") Player " + player.getName() + " SET FLAG " + flag + " of region " + region.getName() + " to " + region.getFlagString(flag));
        } else {
            region.removeFlag(flag);
            RedProtect.get().lang.sendMessage(player, RedProtect.get().lang.get("cmdmanager.region.flag.removed").replace("{flag}", flag).replace("{region}", region.getName()));
            RedProtect.get().logger.addLog("(World " + region.getWorld() + ") Player " + player.getName() + " REMOVED FLAG " + flag + " of region " + region.getName());
        }

        close(false);
    }

    @Listener
    public void onDeath(DestructEntityEvent event) {
        if (event.getTargetEntity() instanceof Player) {
            Player p = (Player) event.getTargetEntity();
            if (p.getName().equals(this.player.getName())) {
                close(true);
            }
        }
    }

    @Listener
    public void onPlayerLogout(ClientConnectionEvent.Disconnect event) {
        Player p = event.getTargetEntity();
        if (p.getName().equals(this.player.getName())) {
            close(true);
        }
    }

    @Listener
    public void onPluginDisable(GameStoppingServerEvent event) {
        close(true);
    }

    private void close(boolean close) {
        //Unregister Listener
        Sponge.getEventManager().unregisterListeners(this);

        RedProtect.get().getVersionHelper().removeGuiItem(this.player);

        // Check for items
        Sponge.getGame().getEventManager().unregisterListeners(this);
        if (close) RedProtect.get().getVersionHelper().closeInventory(this.player);

        this.guiItems = null;
        this.player = null;
        this.region = null;
    }

    public void open() {
        //Register Listener
        Sponge.getGame().getEventManager().registerListeners(RedProtect.get().container, this);

        Inventory inv = RedProtect.get().getVersionHelper().newInventory(54, "Item flag GUI");

        int i = 0;
        while (i < this.guiItems.length) {
            int line = 0;
            int slot = i;
            if (i > 8) {
                line = i / 9;
                slot = i - (line * 9);
            }
            RedProtect.get().getVersionHelper().query(inv, slot, line).set(this.guiItems[i]);
            i++;
        }

        RedProtect.get().getVersionHelper().openInventory(inv, this.player);
    }
}