/*
 * Copyright (c) 2020 - @FabioZumbi12
 * Last Modified: 02/07/2020 19:01.
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
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.type.TileEntityInventory;
import org.spongepowered.api.world.World;

public class EconomyManager {


    public static long getRegionValue(Region r) {
        long regionCost = 0;
        World w = RedProtect.get().getServer().getWorld(r.getWorld()).get();
        int maxX = r.getMaxMbrX();
        int minX = r.getMinMbrX();
        int maxZ = r.getMaxMbrZ();
        int minZ = r.getMinMbrZ();
        for (int x = minX; x < maxX; x++) {
            for (int y = 0; y < 256; y++) {
                for (int z = minZ; z < maxZ; z++) {

                    BlockSnapshot b = w.createSnapshot(x, y, z);
                    if (b.getState().getType().equals(BlockTypes.AIR)) {
                        continue;
                    }

                    if (b.getLocation().get().getTileEntity().isPresent()) {
                        TileEntity invTile = b.getLocation().get().getTileEntity().get();
                        if (invTile instanceof TileEntityInventory) {
                            TileEntityInventory<?> inv = (TileEntityInventory<?>) invTile;
                            regionCost += getInvValue(inv.slots());
                        }
                    } else {
                        regionCost += RedProtect.get().getConfigManager().ecoRoot().items.values.get(b.getState().getType().getName());
                    }
                }
            }
        }
        r.setValue(regionCost);
        return regionCost;
    }

    private static long getInvValue(Iterable<Inventory> inv) {
        return RedProtect.get().getVersionHelper().getInvValue(inv);
    }

    public static String getCostMessage(Region r) {
        return RedProtect.get().getLanguageManager().get("economy.forsale") + " &6" + getFormatted(r.getValue()) + " &2" + RedProtect.get().getConfigManager().ecoRoot().economy_name;
    }

    public static String getFormatted(long value) {
        return RedProtect.get().getConfigManager().ecoRoot().economy_symbol + value;
    }

    public static boolean putToSell(Region r, String uuid, long value) {
        try {
            r.clearMembers();
            r.clearAdmins();
            r.setValue(value);
            r.setWelcome(getCostMessage(r));
            r.setFlag(null, "for-sale", true);
            if (RedProtect.get().getConfigManager().ecoRoot().rename_region) {
                RedProtect.get().getRegionManager().renameRegion(RedProtect.get().getUtil().nameGen(RedProtect.get().getUtil().UUIDtoPlayer(uuid), r.getWorld()), r);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
