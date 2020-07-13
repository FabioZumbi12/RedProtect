/*
 * Copyright (c) 2020 - @FabioZumbi12
 * Last Modified: 12/07/2020 19:33.
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

package br.net.fabiozumbi12.buyrentregion.config;

import br.net.fabiozumbi12.buyrentregion.BuyRentRegion;
import org.bukkit.ChatColor;

public class ChatHelper {
    private static final String name = BuyRentRegion.get().getName();

    public static String notice(String msg, Object... args) {
        return format(ChatColor.AQUA, msg, args);
    }

    public static String warning(String msg, Object... args) {
        return format(ChatColor.RED, msg, args);
    }

    private static String format(ChatColor color, String msg, Object... args) {
        return String.format("%s[%s] %s%s", color, name, ChatColor.YELLOW, BuyRentRegion.get().locale.get(msg, args));
    }
}
