/*
 * Copyright (c) 2020 - @FabioZumbi12
 * Last Modified: 27/10/2019 02:17.
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

package br.net.fabiozumbi12.RedProtect.Core.config.Category;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.HashMap;
import java.util.Map;

@ConfigSerializable
public class EconomyCategory {

    @Setting(value = "claim-cost-per-block")
    public claimCostPerBlockCat claim_cost_per_block = new claimCostPerBlockCat();
    @Setting(value = "economy-name")
    public String economy_name = "Coins";
    @Setting(value = "economy-symbol")
    public String economy_symbol = "$";
    @Setting(value = "max-area-toget-value")
    public int max_area_toget_value = 100000;
    @Setting(value = "rename-region")
    public boolean rename_region = false;
    @Setting
    public enchantmentsCat enchantments = new enchantmentsCat();
    @Setting
    public itemsCat items = new itemsCat();

    public EconomyCategory() {
    }

    @ConfigSerializable
    public static class claimCostPerBlockCat {

        @Setting(value = "cost-per-block")
        public int cost_per_block = 10;
        @Setting
        public boolean enable = false;
        @Setting(value = "y-is-free")
        public boolean y_is_free = true;
    }

    @ConfigSerializable
    public static class enchantmentsCat {

        @Setting
        public Map<String, Long> values = new HashMap<>();
    }

    @ConfigSerializable
    public static class itemsCat {

        @Setting
        public Map<String, Long> values = new HashMap<>();
    }
}
