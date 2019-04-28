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
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class EconomyManager {

    public static long getRegionValue(Region r) {
        long regionCost = 0;
        World w = RedProtect.get().getServer().getWorld(r.getWorld());
        int maxX = r.getMaxMbrX();
        int minX = r.getMinMbrX();
        int maxZ = r.getMaxMbrZ();
        int minZ = r.getMinMbrZ();
        int factor;
        for (int x = minX; x < maxX; x++) {
            for (int y = 0; y < 256; y++) {
                for (int z = minZ; z < maxZ; z++) {

                    Block b = w.getBlockAt(x, y, z);
                    if (b.isEmpty()) {
                        continue;
                    }

                    if (b.getState() instanceof InventoryHolder) {
                        Inventory inv = ((InventoryHolder) b.getState()).getInventory();

                        if (inv.getSize() == 54) {
                            factor = 2;
                        } else {
                            factor = 1;
                        }

                        for (ItemStack item : inv.getContents()) {
                            if (item == null || item.getAmount() == 0) {
                                continue;
                            }
                            regionCost = regionCost + ((RedProtect.get().config.getBlockCost(item.getType().name()) * item.getAmount()) / factor);
                            if (item.getEnchantments().size() > 0) {
                                for (Enchantment enchant : item.getEnchantments().keySet()) {
                                    regionCost = regionCost + ((RedProtect.get().config.getEnchantCost(enchant.getName()) * item.getEnchantments().get(enchant)) / factor);
                                }
                            }
                        }
                    } else {
                        regionCost = regionCost + RedProtect.get().config.getBlockCost(b.getType().name());
                    }
                }
            }
        }
        return regionCost;
    }

    public static String getCostMessage(Region r) {
        return RedProtect.get().lang.get("economy.forsale") + " &6" + getFormatted(r.getValue()) + " &2" + RedProtect.get().config.getEcoString("economy-name");
    }

    public static String getFormatted(long value) {
        return RedProtect.get().config.getEcoString("economy-symbol") + value;
    }

    public static boolean putToSell(Region r, String uuid, long value) {
        try {
            r.clearMembers();
            r.clearAdmins();
            r.setValue(value);
            r.setWelcome(getCostMessage(r));
            r.setFlag(Bukkit.getConsoleSender(), "for-sale", true);
            if (RedProtect.get().config.getEcoBool("rename-region")) {
                RedProtect.get().rm.renameRegion(RedProtectUtil.nameGen(RedProtectUtil.UUIDtoPlayer(uuid), r.getWorld()), r);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean BuyRegion(Region r, String uuid) {
        try {
            r.clearMembers();
            r.clearAdmins();
            r.clearLeaders();
            r.addLeader(uuid);
            r.setDate(RedProtectUtil.dateNow());
            r.setWelcome("");
            r.setFlags(RedProtect.get().config.getDefFlagsValues());
            if (RedProtect.get().config.getEcoBool("rename-region")) {
                RedProtect.get().rm.renameRegion(RedProtectUtil.nameGen(RedProtectUtil.UUIDtoPlayer(uuid), r.getWorld()), r);
            }
            r.removeFlag("for-sale");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
