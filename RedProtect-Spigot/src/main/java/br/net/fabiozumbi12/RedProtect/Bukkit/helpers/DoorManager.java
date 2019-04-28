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
import org.bukkit.Effect;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class DoorManager {

    public static void ChangeDoor(Block b, Region r) {
        if ((!RedProtect.get().config.isFlagEnabled("smart-door") && !RedProtect.get().config.configRoot().flags.get("smart-door")) || !r.getFlagBool("smart-door")) {
            return;
        }

        if (b.getType().name().contains("IRON")) {
            b.getWorld().playEffect(b.getLocation(), Effect.DOOR_TOGGLE, 0);
            toggleDoor(b);
        }

        if (b.getType().name().contains("TRAP")) {
            return;
        }

        //check side block if is door
        Block[] block = new Block[4];
        block[0] = b.getRelative(BlockFace.EAST);
        block[1] = b.getRelative(BlockFace.WEST);
        block[2] = b.getRelative(BlockFace.NORTH);
        block[3] = b.getRelative(BlockFace.SOUTH);

        for (Block b2 : block) {
            if (b.getType().equals(b2.getType())) {
                toggleDoor(b2);
                break;
            }
        }
    }

    private static void toggleDoor(Block b) {
        if (b.getRelative(BlockFace.DOWN).getType().equals(b.getType())) {
            b = b.getRelative(BlockFace.DOWN);
        }
        RedProtect.get().getPVHelper().toggleDoor(b);
    }

    public static boolean isOpenable(Block b) {
        return RedProtect.get().getPVHelper().isOpenable(b);
    }
}
