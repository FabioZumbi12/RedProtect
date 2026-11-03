/*
 * Copyright (c) 2012-2024 - @FabioZumbi12
 * Last Modified: 26/11/2024 17:51
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
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.Chest;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ContainerManager {

    private static final BlockFace[] SIDES = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};

    public boolean canOpen(Block b, Player p, boolean pubOnly) {
        if (!RedProtect.get().getConfigManager().configRoot().private_cat.use || p.hasPermission("redprotect.bypass")) {
            return true;
        }

        String blocktype = b.getType().name();
        List<String> blocks = RedProtect.get().getConfigManager().configRoot().private_cat.allowed_blocks;

        boolean deny = !pubOnly;
        if (blocks.stream().anyMatch(blocktype::matches) && b.getState() instanceof Container) {
            for (Block bs : getLockSigns(b)) {
                if (validatePublicSign(bs)) return true;
                if (!pubOnly && validatePrivateSign(bs)) {
                    deny = false;
                    if (validateOpenBlock(bs, p)) {
                        return true;
                    }
                }
            }
        }
        return deny;
    }

    public boolean canAddLock(Block sign, Player p) {
        if (p.hasPermission("redprotect.bypass") || RedProtect.get().getPermissionHandler().hasPermOrBypass(p, "redprotect.bypass.private")) {
            return true;
        }
        Block container = getBlockRelative(sign);
        if (container == null) {
            return true;
        }
        for (Block bs : getLockSigns(container)) {
            if (!bs.equals(sign) && bs.getState() instanceof Sign s && isProtectedSign(s)
                    && !s.getSide(Side.FRONT).getLine(1).equals(p.getName())) {
                return false;
            }
        }
        return true;
    }

    public boolean isProtectedSign(Sign sign) {
        if (!RedProtect.get().getConfigManager().configRoot().private_cat.use || !isSign(sign.getBlock())) {
            return false;
        }
        String[] lines = sign.getSide(Side.FRONT).getLines();
        return validatePrivateSign(lines) || validatePublicSign(lines);
    }

    public boolean canEditSign(Sign sign, Player p) {
        if (!isProtectedSign(sign)) {
            return true;
        }
        String owner = sign.getSide(Side.FRONT).getLine(1);
        return (!owner.isEmpty() && owner.equals(p.getName()))
                || p.hasPermission("redprotect.bypass")
                || RedProtect.get().getPermissionHandler().hasPermOrBypass(p, "redprotect.bypass.private");
    }

    // Wall signs hanging on this block or, for a double chest, on its other half.
    private List<Block> getLockSigns(Block b) {
        List<Block> parts = new ArrayList<>();
        parts.add(b);
        Block other = getOtherChestHalf(b);
        if (other != null) {
            parts.add(other);
        }
        List<Block> signs = new ArrayList<>();
        for (Block part : parts) {
            for (BlockFace face : SIDES) {
                Block bs = part.getRelative(face);
                if (bs.getBlockData() instanceof WallSign sign && sign.getFacing() == face) {
                    signs.add(bs);
                }
            }
        }
        return signs;
    }

    private Block getOtherChestHalf(Block b) {
        if (!(b.getBlockData() instanceof Chest chest) || chest.getType() == Chest.Type.SINGLE) {
            return null;
        }
        // Same rule as vanilla: a LEFT half joins the block clockwise of its facing, a RIGHT half the one counterclockwise.
        BlockFace toOther = clockwise(chest.getFacing());
        if (chest.getType() == Chest.Type.RIGHT) {
            toOther = toOther.getOppositeFace();
        }
        Block other = b.getRelative(toOther);
        return other.getType() == b.getType() ? other : null;
    }

    private static BlockFace clockwise(BlockFace face) {
        return switch (face) {
            case NORTH -> BlockFace.EAST;
            case EAST -> BlockFace.SOUTH;
            case SOUTH -> BlockFace.WEST;
            default -> BlockFace.NORTH;
        };
    }

    public boolean canBreak(Player p, Block b) {
        if (!RedProtect.get().getConfigManager().configRoot().private_cat.use || p.hasPermission("redprotect.bypass")) {
            return true;
        }
        Region reg = RedProtect.get().getRegionManager().getTopRegion(b.getLocation());
        if (reg == null && !RedProtect.get().getConfigManager().configRoot().private_cat.allow_outside) {
            return true;
        }
        boolean deny = true;

        if (isSign(b) && validatePrivateSign(b)) {
            deny = false;
            if (validateBreakSign(b, p)) {
                return true;
            }
        }

        String signbtype = b.getType().name();
        List<String> blocks = RedProtect.get().getConfigManager().configRoot().private_cat.allowed_blocks;

        if (blocks.stream().anyMatch(signbtype::matches)) {
            for (Block bs : getLockSigns(b)) {
                if (validatePrivateSign(bs)) {
                    deny = false;
                    if (validateBreakSign(bs, p)) {
                        return true;
                    }
                }
            }
        }
        return deny;
    }

    public boolean canWorldBreak(Block b) {
        if (!RedProtect.get().getConfigManager().configRoot().private_cat.use) {
            return true;
        }
        Region reg = RedProtect.get().getRegionManager().getTopRegion(b.getLocation());
        if (reg == null && !RedProtect.get().getConfigManager().configRoot().private_cat.allow_outside) {
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
        List<String> blocks = RedProtect.get().getConfigManager().configRoot().private_cat.allowed_blocks;

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
        String priv = RedProtect.get().getLanguageManager().get("blocklistener.container.signline");
        priv = ChatColor.stripColor(priv).replaceAll("[^a-zA-Z]","");
        String line1 = ChatColor.stripColor(lines[0]).replaceAll("[^a-zA-Z]","");
        return line1.equalsIgnoreCase("[private]") ||
                line1.equalsIgnoreCase("private") ||
                line1.equalsIgnoreCase(priv) ||
                line1.equalsIgnoreCase("[" + priv + "]");
    }

    public boolean validatePublicSign(String[] lines) {
        String pub = RedProtect.get().getLanguageManager().get("blocklistener.container.signline.public");
        pub = ChatColor.stripColor(pub).replaceAll("[^a-zA-Z]","");
        String line1 = ChatColor.stripColor(lines[0]).replaceAll("[^a-zA-Z]","");
        return line1.equalsIgnoreCase("[public]") ||
                line1.equalsIgnoreCase("public") ||
                line1.equalsIgnoreCase(pub) ||
                line1.equalsIgnoreCase("[" + pub + "]");
    }

    private boolean validatePublicSign(Block b) {
        Sign s = (Sign) b.getState();
        return validatePublicSign(s.getSide(Side.FRONT).getLines());
    }

    private boolean validatePrivateSign(Block b) {
        Sign s = (Sign) b.getState();
        return validatePrivateSign(s.getSide(Side.FRONT).getLines());
    }

    private boolean validateBreakSign(Block b, Player p) {
        Sign s = (Sign) b.getState();
        return validatePrivateSign(b) && (s.getSide(Side.FRONT).getLine(1).isEmpty() || s.getSide(Side.FRONT).getLine(1).equals(p.getName()));
    }

    private boolean testPrivate(Block b, Player p) {
        Sign s = (Sign) b.getState();
        return validatePrivateSign(b) &&
                (s.getSide(Side.FRONT).getLine(1).equals(p.getName()) || s.getSide(Side.FRONT).getLine(2).equals(p.getName()) || s.getSide(Side.FRONT).getLine(3).equals(p.getName()));
    }

    private boolean validateOpenBlock(Block b, Player p) {
        return testPrivate(b, p);
    }

    public boolean isContainer(Block b) {
        Block container = getBlockRelative(b);
        String signbtype = container.getType().name();
        return RedProtect.get().getConfigManager().configRoot().private_cat.allowed_blocks.stream().anyMatch(signbtype::matches);
    }

    public boolean isSign(Block b) {
        return b.getType().name().contains("WALL_SIGN");
    }

}
