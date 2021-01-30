/*
 * Copyright (c) 2020 - @FabioZumbi12
 * Last Modified: 09/08/2020 01:16.
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
public class BlockCategory {

    @Setting(comment = "Enable timed given claim blocks?\nDefault: false")
    public boolean enabled = false;
    @Setting(comment = "Don't touch this.\n" +
            "This is the player times, to calculate the amount of blocks a player have\n" +
            "Values:\n" +
            "- time: 0 - Time played on server\n" +
            "- added-blocks: 0 - Blocks added manually")
    public Map<String, PlayerCat> players = new HashMap<>();

    @Setting(value = "unit-to-add", comment = "The unit type to give claim blocks to players?\n" +
            "Options:\n" +
            "- d = x blocks for every day\n" +
            "- h = x blocks for every hour\n" +
            "- m = x blocks for every minute\n" +
            "- s = x blocks for every second")
    public String unit_to_add = "h";
    @Setting(value = "blocks-to-value", comment = "The amount of blocks to add on every time unit configured")
    public long blocks_to_add = 50;
    @Setting(value = "time-type", comment = "The method to calculate the player time!\n" +
            "Options:\n" +
            "- first-join = Count the time since the first login on server (after ResProtect installation). This includes the time the player is offline.\n" +
            "- online-time = Count only the online time. When the player is offline, the time pauses.\n" +
            "Important: Using 'online-time', we will use a thread to count and add the time for all online players. Using 'first-join' use no new threads.")
    public String time_type = "first-login";

    @ConfigSerializable
    public static class PlayerCat {

        @Setting
        public long time = 0;
        @Setting(value = "added-blocks")
        public long added_blocks = 0;
        @Setting
        public String player;
        public PlayerCat() {
        }
        public PlayerCat(long initialTime, String player) {
            this.time = initialTime;
            this.player = player;
        }
    }
}
