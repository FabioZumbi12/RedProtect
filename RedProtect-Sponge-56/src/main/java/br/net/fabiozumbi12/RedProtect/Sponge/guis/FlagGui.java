/*
 * Copyright (c) 2020 - @FabioZumbi12
 * Last Modified: 07/06/2019 02:40.
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

package br.net.fabiozumbi12.RedProtect.Sponge.guis;

import br.net.fabiozumbi12.RedProtect.Core.helpers.CoreUtil;
import br.net.fabiozumbi12.RedProtect.Core.helpers.Replacer;
import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import br.net.fabiozumbi12.RedProtect.Sponge.Region;
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
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class FlagGui {

    private int size;
    private ItemStack[] guiItems;
    private Player player;
    private Region region;
    private Inventory inv;
    private boolean editable;

    public FlagGui(String name, Player player, Region region, boolean editable, int maxSlots) {
        this.editable = editable;
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

        for (String flag : region.getFlags().keySet()) {
            try {
                if (!(region.getFlags().get(flag) instanceof Boolean) || !RedProtect.get().getConfigManager().guiRoot().gui_flags.containsKey(flag)) {
                    continue;
                }
                if (RedProtect.get().getPermissionHandler().hasFlagPerm(player, flag) && (RedProtect.get().getConfigManager().configRoot().flags.containsKey(flag) || RedProtect.get().getConfigManager().AdminFlags.contains(flag))) {
                    if (flag.equals("pvp") && !RedProtect.get().getConfigManager().configRoot().flags.containsKey("pvp")) {
                        continue;
                    }

                    int i = RedProtect.get().getConfigManager().getGuiSlot(flag);

                    this.guiItems[i] = ItemStack.of(Sponge.getRegistry().getType(ItemType.class, RedProtect.get().getConfigManager().guiRoot().gui_flags.get(flag).material).orElse(ItemTypes.GLASS_PANE), 1);
                    this.guiItems[i].offer(Keys.DISPLAY_NAME, RedProtect.get().getUtil().toText(RedProtect.get().guiLang.getFlagName(flag)));
                    List<Text> lore = new ArrayList<>(Arrays.asList(
                            Text.joinWith(Text.of(" "), RedProtect.get().guiLang.getFlagString("value"), RedProtect.get().guiLang.getFlagString(region.getFlags().get(flag).toString())),
                            RedProtect.get().getUtil().toText("&0" + flag)));
                    lore.addAll(RedProtect.get().guiLang.getFlagDescription(flag));
                    this.guiItems[i].offer(Keys.ITEM_LORE, lore);

                    if (!this.region.getFlagBool(flag)) {
                        this.guiItems[i].remove(Keys.ITEM_ENCHANTMENTS);
                    } else {
                        this.guiItems[i] = RedProtect.get().getVersionHelper().offerEnchantment(this.guiItems[i]);
                    }
                    this.guiItems[i].offer(Keys.HIDE_ENCHANTMENTS, true);
                    this.guiItems[i].offer(Keys.HIDE_ATTRIBUTES, true);
                }
            } catch (Exception ex) {
                this.player.sendMessage(Text.of(Color.RED, "Seems RedProtect have a wrong Item Gui or a problem on guiconfig for flag " + flag));
            }
        }

        this.inv = RedProtect.get().getVersionHelper().newInventory(size, name);

        for (int slotc = 0; slotc < this.size; slotc++) {
            if (this.guiItems[slotc] == null) {
                this.guiItems[slotc] = RedProtect.get().getConfigManager().getGuiSeparator();
            }
            int line = 0;
            int slot = slotc;
            if (slotc > 8) {
                line = slotc / 9;
                slot = slotc - (line * 9);
            }
            RedProtect.get().getVersionHelper().query(inv, slot, line).set(this.guiItems[slotc]);
        }
    }

    @Listener
    public void onCloseInventory(InteractInventoryEvent.Close event) {
        if (event.getTargetInventory().getName().get().equals(this.inv.getName().get())) {
            if (this.editable) {
                for (int i = 0; i < this.size; i++) {
                    try {
                        int line = 0;
                        int slot = i;
                        if (i > 8) {
                            line = i / 9;
                            slot = i - (line * 9);
                        }
                        if (RedProtect.get().getVersionHelper().query(event.getTargetInventory(), slot, line).peek().isPresent()) {
                            final int fi = i;
                            ItemStack stack = RedProtect.get().getVersionHelper().query(event.getTargetInventory(), slot, line).peek().get();
                            stack.get(Keys.ITEM_LORE).ifPresent(ls -> {
                                String flag = ls.get(1).toPlain().replace("§0", "");
                                if (RedProtect.get().getConfigManager().getDefFlags().contains(flag))
                                    RedProtect.get().getConfigManager().setGuiSlot(flag, fi);
                            });
                        }
                    } catch (Exception e) {
                        RedProtect.get().getLanguageManager().sendMessage(this.player, "gui.edit.error");
                        close(false);
                        CoreUtil.printJarVersion();
                        e.printStackTrace();
                        return;
                    }
                }
                RedProtect.get().getConfigManager().saveGui();
                RedProtect.get().getLanguageManager().sendMessage(this.player, "gui.edit.ok");
            }
            close(false);
        }
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

    @Listener
    public void onInventoryClick(ClickInventoryEvent event) {
        if (event.getTargetInventory().getName().get().equals(this.inv.getName().get())) {

            if (this.editable) {
                return;
            }

            if (event.getTransactions().size() > 0) {
                Transaction<ItemStackSnapshot> clickTransaction = event.getTransactions().get(0);

                ItemStack item = clickTransaction.getOriginal().createStack();

                if (!RedProtect.get().getVersionHelper().getItemType(item).equals(ItemTypes.NONE) && item.get(Keys.ITEM_LORE).isPresent()) {
                    String flag = item.get(Keys.ITEM_LORE).get().get(1).toPlain().replace("§0", "");
                    if (RedProtect.get().getConfigManager().getDefFlags().contains(flag)) {
                        if (RedProtect.get().getConfigManager().configRoot().flags_configuration.change_flag_delay.enable) {
                            if (RedProtect.get().getConfigManager().configRoot().flags_configuration.change_flag_delay.flags.contains(flag)) {
                                if (!RedProtect.get().changeWait.contains(this.region.getName() + flag)) {
                                    applyFlag(flag, item, event);
                                    RedProtect.get().getUtil().startFlagChanger(this.region.getName(), flag, this.player);
                                } else {
                                    RedProtect.get().getLanguageManager().sendMessage(player, RedProtect.get().getLanguageManager().get("gui.needwait.tochange").replace("{seconds}", RedProtect.get().getConfigManager().configRoot().flags_configuration.change_flag_delay.seconds + ""));
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

    private void applyFlag(String flag, ItemStack item, ClickInventoryEvent event) {
        if (this.region.setFlag(RedProtect.get().getVersionHelper().getCause(this.player), flag, !this.region.getFlagBool(flag))) {
            RedProtect.get().getLanguageManager().sendMessage(player, RedProtect.get().getLanguageManager().get("cmdmanager.region.flag.set").replace("{flag}", "'" + flag + "'") + " " + this.region.getFlagBool(flag));

            if (!this.region.getFlagBool(flag)) {
                item.remove(Keys.ITEM_ENCHANTMENTS);
            } else {
                item = RedProtect.get().getVersionHelper().offerEnchantment(item);
            }
            item.offer(Keys.HIDE_ENCHANTMENTS, true);
            item.offer(Keys.HIDE_ATTRIBUTES, true);

            List<Text> lore = new ArrayList<>(Arrays.asList(
                    Text.joinWith(Text.of(" "), RedProtect.get().guiLang.getFlagString("value"), RedProtect.get().guiLang.getFlagString(region.getFlags().get(flag).toString())),
                    RedProtect.get().getUtil().toText("&0" + flag)));
            lore.addAll(RedProtect.get().guiLang.getFlagDescription(flag));
            item.offer(Keys.ITEM_LORE, lore);

            event.getCursorTransaction().setCustom(ItemStackSnapshot.NONE);
            event.getTransactions().get(0).getSlot().offer(item);

            RedProtect.get().getVersionHelper().removeGuiItem(this.player);

            RedProtect.get().logger.addLog("(World " + this.region.getWorld() + ") Player " + player.getName() + " CHANGED flag " + flag + " of region " + this.region.getName() + " to " + this.region.getFlagString(flag));
        }
    }

    public void close(boolean close) {
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
        for (Player player : Sponge.getServer().getOnlinePlayers()) {
            if (player.getOpenInventory().isPresent() && player.getOpenInventory().get().getName().get().equals(this.inv.getName().get())) {
                Region r = RedProtect.get().getRegionManager().getTopRegion(player.getLocation(), this.getClass().getName());
                if (r != null && r.equals(this.region) && !player.equals(this.player)) {
                    RedProtect.get().getLanguageManager().sendMessage(this.player, "cmdmanager.region.rpgui-other", new Replacer[]{new Replacer("{player}", player.getName())});
                    return;
                }
            }
        }
        //Register Listener
        Sponge.getGame().getEventManager().registerListeners(RedProtect.get().container, this);

        RedProtect.get().getVersionHelper().openInventory(this.inv, this.player);
    }

}