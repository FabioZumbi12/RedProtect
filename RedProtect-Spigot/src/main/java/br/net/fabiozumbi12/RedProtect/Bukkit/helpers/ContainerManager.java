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
import br.net.fabiozumbi12.RedProtect.Core.helpers.LogLevel;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.util.List;

public class ContainerManager {

    public boolean canOpen(Block b, Player p) {
        if (!RedProtect.get().config.configRoot().private_cat.use || p.hasPermission("redprotect.bypass")) {
            return true;
        }

        String blocktype = b.getType().name();
        List<String> blocks = RedProtect.get().config.configRoot().private_cat.allowed_blocks;

        boolean deny = true;
        if (blocks.stream().anyMatch(blocktype::matches)) {
            int x = b.getX();
            int y = b.getY();
            int z = b.getZ();
            World w = p.getWorld();

            for (int sx = -1; sx <= 1; sx++) {
                for (int sz = -1; sz <= 1; sz++) {
                    Block bs = w.getBlockAt(x + sx, y, z + sz);
                    if (isSign(bs) && (validatePrivateSign(bs))) {
                        deny = false;
                        if (validateOpenBlock(bs, p)) {
                            return true;
                        }
                    }

                    int x2 = bs.getX();
                    int y2 = bs.getY();
                    int z2 = bs.getZ();

                    String blocktype2 = b.getType().name();
                    if (blocks.stream().anyMatch(blocktype2::matches)) {
                        for (int ux = -1; ux <= 1; ux++) {
                            for (int uz = -1; uz <= 1; uz++) {
                                Block bu = w.getBlockAt(x2 + ux, y2, z2 + uz);
                                if (isSign(bu) && (validatePrivateSign(bu))) {
                                    deny = false;
                                    if (validateOpenBlock(bu, p)) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return deny;
    }

    public boolean canBreak(Player p, Block b) {
        if (!RedProtect.get().config.configRoot().private_cat.use || p.hasPermission("redprotect.bypass")) {
            return true;
        }
        Region reg = RedProtect.get().rm.getTopRegion(b.getLocation());
        if (reg == null && !RedProtect.get().config.configRoot().private_cat.allow_outside) {
            return true;
        }
        int x = b.getX();
        int y = b.getY();
        int z = b.getZ();
        World w = p.getWorld();

        boolean deny = true;

        if (isSign(b) && validatePrivateSign(b)) {
            deny = false;
            if (validateBreakSign(b, p)) {
                return true;
            }
        }

        String signbtype = b.getType().name();
        List<String> blocks = RedProtect.get().config.configRoot().private_cat.allowed_blocks;

        if (blocks.stream().anyMatch(signbtype::matches)) {
            for (int sx = -1; sx <= 1; sx++) {
                for (int sy = -1; sy <= 1; sy++) {
                    for (int sz = -1; sz <= 1; sz++) {
                        Block bs = w.getBlockAt(x + sx, y + sy, z + sz);
                        if (isSign(bs) && (validatePrivateSign(bs))) {
                            deny = false;
                            if (validateBreakSign(bs, p)) {
                                return true;
                            }
                        }

                        String blocktype2 = b.getType().name();

                        int x2 = bs.getX();
                        int y2 = bs.getY();
                        int z2 = bs.getZ();

                        if (blocks.stream().anyMatch(blocktype2::matches)) {
                            for (int ux = -1; ux <= 1; ux++) {
                                for (int uy = -1; uy <= 1; uy++) {
                                    for (int uz = -1; uz <= 1; uz++) {
                                        Block bu = w.getBlockAt(x2 + ux, y2 + uy, z2 + uz);
                                        if (isSign(bu) && (validatePrivateSign(bu))) {
                                            deny = false;
                                            if (validateBreakSign(bu, p)) {
                                                return true;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return deny;
    }

    public boolean canWorldBreak(Block b) {
        if (!RedProtect.get().config.configRoot().private_cat.use) {
            return true;
        }
        Region reg = RedProtect.get().rm.getTopRegion(b.getLocation());
        if (reg == null && !RedProtect.get().config.configRoot().private_cat.allow_outside) {
            return true;
        }
        int x = b.getX();
        int y = b.getY();
        int z = b.getZ();
        World w = b.getWorld();

        if (isSign(b) && validWorldBreak(b)) {
            RedProtect.get().logger.debug(LogLevel.DEFAULT, "Valid Sign on canWorldBreak!");
            return false;
        }

        String signbtype = b.getType().name();
        List<String> blocks = RedProtect.get().config.configRoot().private_cat.allowed_blocks;

        if (blocks.stream().anyMatch(signbtype::matches)) {
            for (int sx = -1; sx <= 1; sx++) {
                for (int sz = -1; sz <= 1; sz++) {
                    Block bs = w.getBlockAt(x + sx, y, z + sz);
                    if (isSign(bs) && validWorldBreak(bs)) {
                        return false;
                    }

                    String blocktype2 = b.getType().name();

                    int x2 = bs.getX();
                    int y2 = bs.getY();
                    int z2 = bs.getZ();

                    if (blocks.stream().anyMatch(blocktype2::matches)) {
                        for (int ux = -1; ux <= 1; ux++) {
                            for (int uz = -1; uz <= 1; uz++) {
                                Block bu = w.getBlockAt(x2 + ux, y2, z2 + uz);
                                if (isSign(bu) && validWorldBreak(bu)) {
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private Block getBlockRelative(Block block) {
        return RedProtect.get().getVersionHelper().getBlockRelative(block);
    }

    private boolean validWorldBreak(Block b) {
        return validatePrivateSign(b);
    }

    public boolean validatePrivateSign(String[] lines) {
        String priv = RedProtect.get().lang.get("blocklistener.container.signline");
        String line1 = lines[0];
        return line1.equalsIgnoreCase("[private]") ||
                line1.equalsIgnoreCase("private") ||
                line1.equalsIgnoreCase(priv) ||
                line1.equalsIgnoreCase("[" + priv + "]");
    }

    private boolean validatePrivateSign(Block b) {
        Sign s = (Sign) b.getState();
        return validatePrivateSign(s.getLines());
    }

    private boolean validateBreakSign(Block b, Player p) {
        Sign s = (Sign) b.getState();
        return validatePrivateSign(b) && (s.getLine(1).isEmpty() || s.getLine(1).equals(p.getName()));
    }

    private boolean testPrivate(Block b, Player p) {
        Sign s = (Sign) b.getState();
        return validatePrivateSign(b) &&
                (s.getLine(1).equals(p.getName()) || s.getLine(2).equals(p.getName()) || s.getLine(3).equals(p.getName()));
    }

    private boolean validateOpenBlock(Block b, Player p) {
        return testPrivate(b, p);
    }

    public boolean isContainer(Block b) {
        Block container = getBlockRelative(b);
        String signbtype = container.getType().name();
        return RedProtect.get().config.configRoot().private_cat.allowed_blocks.stream().anyMatch(signbtype::matches);
    }

    public boolean isSign(Block b) {
        return b.getType().name().contains("WALL_SIGN");
    }

}
