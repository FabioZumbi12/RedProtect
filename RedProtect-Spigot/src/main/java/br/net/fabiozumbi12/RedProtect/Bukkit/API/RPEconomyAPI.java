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

package br.net.fabiozumbi12.RedProtect.Bukkit.API;

import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPEconomy;

public class RPEconomyAPI {

    /**
     * Put a region to sell.
     * <p>
     *
     * @param region - Region to sell.
     * @param owner  - Owner of the region;
     * @param value  - Value to sell this region;
     * @return {@code true} if successfull sell flag. {@code false} if theres an error on sell the region and the money will return to player.
     */
    public static boolean SellRegion(Region region, String owner, long value) {
        return RPEconomy.putToSell(region, owner, value);
    }

    /**
     * Buy a region.
     * <p>
     *
     * @param region - Region to buy.
     * @param owner  - Owner of the region;
     * @return {@code true} if successfull buy. {@code false} if theres an error on buy the region and the money will return to player.
     */
    public static boolean BuyRegion(Region region, String owner) {
        return RPEconomy.BuyRegion(region, owner);
    }

    /**
     * Get the region value based on blocks, chests, itens inside chests and item enchantements inside chests too.
     * <p>
     *
     * @param region - Region to get value.
     * @return {@code Long} value of the region.
     */
    public static long getRegionValue(Region region) {
        return RPEconomy.getRegionValue(region);
    }
}
