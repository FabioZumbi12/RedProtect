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

package br.net.fabiozumbi12.RedProtect.Sponge.helpers;

import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import br.net.fabiozumbi12.RedProtect.Sponge.Region;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.PortionTypes;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class DoorManager {

    public static void ChangeDoor(BlockSnapshot b, Region r) {
        if ((!r.flagExists("smart-door") && !RedProtect.get().config.configRoot().flags.get("smart-door")) || !r.getFlagBool("smart-door")) {
            return;
        }

        Location<World> loc = b.getLocation().get();
        World w = loc.getExtent();

        if (isDoor(b)) {
            boolean iron = b.getState().getType() == BlockTypes.IRON_DOOR;
            if (iron) {
                changeDoorState(b);
                if (getDoorState(b)) {
                    w.playSound(SoundTypes.BLOCK_IRON_DOOR_OPEN, loc.getPosition(), 1);
                } else {
                    w.playSound(SoundTypes.BLOCK_IRON_DOOR_CLOSE, loc.getPosition(), 1);
                }
            }

            if (loc.getRelative(Direction.DOWN).getBlock().getType() == b.getState().getType() && loc.get(Keys.PORTION_TYPE).get() == PortionTypes.TOP) {
                loc = loc.getRelative(Direction.DOWN);
            }

            //check side block if is door
            BlockSnapshot[] block = new BlockSnapshot[4];
            block[0] = loc.getRelative(Direction.EAST).createSnapshot();
            block[1] = loc.getRelative(Direction.WEST).createSnapshot();
            block[2] = loc.getRelative(Direction.NORTH).createSnapshot();
            block[3] = loc.getRelative(Direction.SOUTH).createSnapshot();

            for (BlockSnapshot b2 : block) {
                if (b.getState().getType() == b2.getState().getType()) {
                    changeDoorState(b2);
                    break;
                }
            }
        }
    }

    //wooden door
    private static boolean getDoorState(BlockSnapshot b) {
        return b.getLocation().get().get(Keys.OPEN).get();
    }

    private static void changeDoorState(BlockSnapshot b) {
        b.getLocation().get().offer(Keys.OPEN, !getDoorState(b));
    }

    private static boolean isDoor(BlockSnapshot b) {
        return b.getState().getType() == BlockTypes.WOODEN_DOOR || b.getState().getType() == BlockTypes.IRON_DOOR;
    }

    public static boolean isOpenable(BlockSnapshot b) {
        return b.getLocation().isPresent() && b.getLocation().get().get(Keys.OPEN).isPresent();
    }
}
