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
import br.net.fabiozumbi12.RedProtect.Sponge.config.LangManager;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Arrays;
import java.util.List;

public class ContainerManager {

    public boolean canOpen(BlockSnapshot b, Player p) {
        if (!RedProtect.get().config.configRoot().private_cat.use || p.hasPermission("redprotect.bypass")) {
            return true;
        }

        List<Direction> dirs = Arrays.asList(Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.WEST);
        String blocktype = b.getState().getType().getName();
        Location<World> loc = b.getLocation().get();
        List<String> blocks = RedProtect.get().config.configRoot().private_cat.allowed_blocks;
        boolean deny = true;
        if (blocks.stream().anyMatch(blocktype::matches)) {
            for (Direction dir : dirs) {
                Location<World> loc1 = loc.getBlockRelative(dir);
                if (isSign(loc1.createSnapshot())) {
                    deny = false;
                    if (validateOpenBlock(loc1.createSnapshot(), p)) {
                        return true;
                    }
                }

                if (blocks.stream().anyMatch(loc1.getBlockType().getName()::matches) && loc1.getBlockType().equals(b.getState().getType())) {
                    for (Direction dir2 : dirs) {
                        Location<World> loc3 = loc1.getBlockRelative(dir2);
                        if (isSign(loc3.createSnapshot())) {
                            deny = false;
                            if (validateOpenBlock(loc3.createSnapshot(), p)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return deny;
    }

    public boolean canBreak(Player p, BlockSnapshot b) {
        if (!RedProtect.get().config.configRoot().private_cat.use) {
            return true;
        }

        Region reg = RedProtect.get().rm.getTopRegion(b.getLocation().get(), this.getClass().getName());
        if (reg == null && !RedProtect.get().config.configRoot().private_cat.allow_outside) {
            return true;
        }

        List<Direction> dirs = Arrays.asList(Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN);
        String blocktype = b.getState().getType().getName();
        Location<World> loc = b.getLocation().get();
        List<String> blocks = RedProtect.get().config.configRoot().private_cat.allowed_blocks;

        boolean deny = true;

        if (isSign(loc.createSnapshot()) && validatePrivateSign(b)) {
            deny = false;
            if (validateBreakSign(loc.createSnapshot(), p)) {
                return true;
            }
        }

        if (blocks.stream().anyMatch(blocktype::matches)) {
            for (Direction dir : dirs) {
                Location<World> loc1 = loc.getBlockRelative(dir);
                if (isSign(loc1.createSnapshot())) {
                    deny = false;
                    if (validateBreakSign(loc1.createSnapshot(), p)) {
                        return true;
                    }
                }

                if (blocks.stream().anyMatch(loc1.getBlockType().getName()::matches) && loc1.getBlockType().equals(b.getState().getType())) {
                    for (Direction dir2 : dirs) {
                        Location<World> loc3 = loc1.getBlockRelative(dir2);
                        if (isSign(loc3.createSnapshot())) {
                            deny = false;
                            if (validateBreakSign(loc3.createSnapshot(), p)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return deny;
    }

    public boolean canWorldBreak(BlockSnapshot b) {
        if (!RedProtect.get().config.configRoot().private_cat.use) {
            return true;
        }

        Region reg = RedProtect.get().rm.getTopRegion(b.getLocation().get(), this.getClass().getName());
        if (reg == null && !RedProtect.get().config.configRoot().private_cat.allow_outside) {
            return true;
        }

        List<Direction> dirs = Arrays.asList(Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN);
        String blocktype = b.getState().getType().getName();
        Location<World> loc = b.getLocation().get();
        List<String> blocks = RedProtect.get().config.configRoot().private_cat.allowed_blocks;

        if (isSign(loc.createSnapshot())) {
            BlockSnapshot sign1 = loc.createSnapshot();
            if (validWorldBreak(sign1)) {
                return false;
            }
        }

        if (blocks.stream().anyMatch(blocktype::matches)) {
            for (Direction dir : dirs) {
                Location<World> loc1 = loc.getBlockRelative(dir);
                if (isSign(loc1.createSnapshot())) {
                    BlockSnapshot sign1 = loc1.createSnapshot();
                    if (validWorldBreak(sign1)) {
                        return false;
                    }
                }

                if (blocks.stream().anyMatch(loc1.getBlockType().getName()::matches) && loc1.getBlockType().equals(b.getState().getType())) {
                    for (Direction dir2 : dirs) {
                        Location<World> loc3 = loc1.getBlockRelative(dir2);
                        if (isSign(loc3.createSnapshot())) {
                            BlockSnapshot sign2 = loc3.createSnapshot();
                            if (validWorldBreak(sign2)) {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean validWorldBreak(BlockSnapshot b) {
        return validatePrivateSign(b);
    }

    public boolean validatePrivateSign(String line) {
        String priv = RedProtect.get().lang.get("blocklistener.container.signline");

        return line.equalsIgnoreCase("[private]") ||
                line.equalsIgnoreCase("private") ||
                line.equalsIgnoreCase(priv) ||
                line.equalsIgnoreCase("[" + priv + "]");
    }

    public boolean validatePrivateSign(BlockSnapshot b) {
        String line = b.getLocation().get().get(Keys.SIGN_LINES).get().get(0).toPlain();
        return validatePrivateSign(line);
    }

    private boolean validateBreakSign(BlockSnapshot b, Player p) {
        String line1 = b.getLocation().get().get(Keys.SIGN_LINES).get().get(1).toPlain();
        return (validatePrivateSign(b) && (line1.isEmpty() || line1.equals(p.getName())));
    }

    private boolean validateOpenBlock(BlockSnapshot b, Player p) {
        return testPrivate(b, p);
    }

    private boolean testPrivate(BlockSnapshot b, Player p) {
        List<Text> lines = b.getLocation().get().get(Keys.SIGN_LINES).get();
        return validatePrivateSign(b) &&
                (lines.get(1).toPlain().equals(p.getName()) || lines.get(2).toPlain().equals(p.getName()) || lines.get(3).toPlain().equals(p.getName()));
    }

    public boolean isContainer(BlockSnapshot block) {
        Location<World> loc = block.getLocation().get().getBlockRelative(block.getLocation().get().get(Keys.DIRECTION).get().getOpposite());
        List<String> blocks = RedProtect.get().config.configRoot().private_cat.allowed_blocks;

        return blocks.stream().anyMatch(loc.getBlockType().getName()::matches);
    }

    public boolean isSign(BlockSnapshot b) {
        return (b.getState().getType().equals(BlockTypes.STANDING_SIGN) || b.getState().getType().equals(BlockTypes.WALL_SIGN)) && b.getLocation().get().get(Keys.SIGN_LINES).isPresent();
    }
}
