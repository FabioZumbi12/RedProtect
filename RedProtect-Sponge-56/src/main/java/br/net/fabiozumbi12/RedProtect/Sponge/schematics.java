/*
 *
 * Copyright (c) 2019 - @FabioZumbi12
 * Last Modified: 29/03/19 05:01
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
 *
 */

package br.net.fabiozumbi12.RedProtect.Sponge;

import br.net.fabiozumbi12.RedProtect.Sponge.config.RPLang;
import br.net.fabiozumbi12.RedProtect.Sponge.helpers.RPUtil;
import br.net.fabiozumbi12.RedProtect.Sponge.hooks.WEListener;
import org.spongepowered.api.entity.living.player.Player;

import java.io.File;

public class schematics {
    public static class RPSchematics {

        public static void pasteSchematic(Player p) {
            File file = new File(RedProtect.get().configDir, "schematics" + File.separator + RedProtect.get().cfgs.root().schematics.first_house_file);


            Region region = WEListener.pasteWithWE(p, file);
            if (region == null) return;

            p.sendMessage(RPUtil.toText(RPLang.get("general.color") + "------------------------------------"));
            RPLang.sendMessage(p, "playerlistener.region.startdone");
            p.sendMessage(RPUtil.toText(RPLang.get("general.color") + "------------------------------------"));
            RPLang.sendMessage(p, "cmdmanager.region.firstwarning");
            p.sendMessage(RPUtil.toText(RPLang.get("general.color") + "------------------------------------"));


            RedProtect.get().logger.addLog("(World " + region.getWorld() + ") Player " + p.getName() + " CREATED(SCHEMATIC) region " + region.getName());
            RedProtect.get().rm.add(region, p.getWorld());
        }
    }
}
