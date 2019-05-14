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

package br.net.fabiozumbi12.RedProtect.Sponge.listeners;

import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import br.net.fabiozumbi12.RedProtect.Sponge.Region;
import br.net.fabiozumbi12.RedProtect.Core.helpers.LogLevel;
import br.net.fabiozumbi12.RedProtect.Sponge.helpers.ContainerManager;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;

public class BlockListenerCompat56 {

    private static final ContainerManager cont = new ContainerManager();

    public BlockListenerCompat56() {
        RedProtect.get().logger.debug(LogLevel.BLOCKS, "Loaded BlockListenerCompat56...");
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPiston(ChangeBlockEvent.Pre e) {
        RedProtect.get().logger.debug(LogLevel.BLOCKS, "RPBlockListener78 - Is onChangeBlock event");

        Location<World> piston = null;
        Location<World> block = null;
        boolean antih = RedProtect.get().config.configRoot().region_settings.anti_hopper;

        if (RedProtect.get().getPVHelper().checkCause(e.getCause(), "PISTON_EXTEND")) {
            if (RedProtect.get().config.configRoot().performance.disable_PistonEvent_handler) {
                return;
            }

            List<Location<World>> locs = e.getLocations();
            for (Location<World> loc : locs) {
                if (piston == null) {
                    piston = loc;
                    continue;
                }
                block = loc;
            }
        }

        if (RedProtect.get().getPVHelper().checkCause(e.getCause(), "PISTON_RETRACT")) {
            if (RedProtect.get().config.configRoot().performance.disable_PistonEvent_handler) {
                return;
            }

            List<Location<World>> locs = e.getLocations();
            for (Location<World> loc : locs) {
                if (piston == null) {
                    piston = loc;
                    continue;
                }
                block = loc;
            }
        }

        //process
        if (piston != null && block != null) {
            Region rPi = RedProtect.get().rm.getTopRegion(piston, this.getClass().getName());
            Region rB = RedProtect.get().rm.getTopRegion(block, this.getClass().getName());
            if (rPi == null && rB != null || (rPi != null && rB != null && rPi != rB && !rPi.sameLeaders(rB))) {
                e.setCancelled(true);
                return;
            }

            if (antih) {
                BlockSnapshot ib = block.add(0, 1, 0).createSnapshot();
                if (!cont.canWorldBreak(ib) || !cont.canWorldBreak(block.createSnapshot())) {
                    e.setCancelled(true);
                }
            }
        }
    }
}
