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

package br.net.fabiozumbi12.RedProtect.Bukkit.schematics;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import br.net.fabiozumbi12.RedProtect.Bukkit.hooks.WEHook;
import org.bukkit.entity.Player;

import java.io.File;

public class RPSchematics {

    public static void pasteSchematic(Player p) {
        File file = new File(RedProtect.get().getDataFolder(), "schematics" + File.separator + RedProtect.get().config.configRoot().schematics.first_house_file);


        Region region = WEHook.pasteWithWE(p, file);
        if (region == null) return;

        p.sendMessage(RedProtect.get().lang.get("general.color") + "------------------------------------");
        RedProtect.get().lang.sendMessage(p, "playerlistener.region.startdone");
        p.sendMessage(RedProtect.get().lang.get("general.color") + "------------------------------------");
        RedProtect.get().lang.sendMessage(p, "cmdmanager.region.firstwarning");
        p.sendMessage(RedProtect.get().lang.get("general.color") + "------------------------------------");


        RedProtect.get().logger.addLog("(World " + region.getWorld() + ") Player " + p.getName() + " CREATED(SCHEMATIC) region " + region.getName());
        RedProtect.get().rm.add(region, p.getWorld().getName());
        RedProtect.get().getUtil().addBorder(p, region);
    }
}
