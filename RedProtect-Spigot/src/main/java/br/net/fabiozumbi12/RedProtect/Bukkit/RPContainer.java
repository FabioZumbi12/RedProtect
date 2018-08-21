package br.net.fabiozumbi12.RedProtect.Bukkit;

import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPConfig;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPLang;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.util.List;

public class RPContainer {

    @SuppressWarnings("deprecation")
    public boolean canOpen(Block b, Player p) {
        if (!RPConfig.getBool("private.use") || p.hasPermission("redprotect.bypass")) {
            return true;
        }

        String blocktype;
        if (RPConfig.getBool("private.allowed-blocks-use-ids")) {
            blocktype = Integer.toString(b.getType().getId());
        } else {
            blocktype = b.getType().name();
        }

        List<String> blocks = RPConfig.getStringList("private.allowed-blocks");

        boolean deny = true;
        if (blocks.stream().anyMatch(blocktype::matches)) {
            int x = b.getX();
            int y = b.getY();
            int z = b.getZ();
            World w = p.getWorld();

            for (int sx = -1; sx <= 1; sx++) {
                for (int sz = -1; sz <= 1; sz++) {
                    Block bs = w.getBlockAt(x + sx, y, z + sz);
                    if (bs.getState() instanceof Sign && (validatePrivateSign(bs))) {
                        deny = false;
                        if (validateOpenBlock(bs, p)) {
                            return true;
                        }
                    }

                    int x2 = bs.getX();
                    int y2 = bs.getY();
                    int z2 = bs.getZ();

                    String blocktype2;
                    if (RPConfig.getBool("private.allowed-blocks-use-ids")) {
                        blocktype2 = Integer.toString(b.getType().getId());
                    } else {
                        blocktype2 = b.getType().name();
                    }
                    if (blocks.stream().anyMatch(blocktype2::matches)) {
                        for (int ux = -1; ux <= 1; ux++) {
                            for (int uz = -1; uz <= 1; uz++) {
                                Block bu = w.getBlockAt(x2 + ux, y2, z2 + uz);
                                if (bu.getState() instanceof Sign && (validatePrivateSign(bu))) {
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

    @SuppressWarnings("deprecation")
    public boolean canBreak(Player p, Block b) {
        if (!RPConfig.getBool("private.use") || p.hasPermission("redprotect.bypass")) {
            return true;
        }
        Region reg = RedProtect.get().rm.getTopRegion(b.getLocation());
        if (reg == null && !RPConfig.getBool("private.allow-outside")) {
            return true;
        }
        int x = b.getX();
        int y = b.getY();
        int z = b.getZ();
        World w = p.getWorld();

        boolean deny = true;

        if (b.getState() instanceof Sign && validatePrivateSign(b)) {
            deny = false;
            if (validateBreakSign(b, p)) {
                return true;
            }
        }

        String signbtype;
        if (RPConfig.getBool("private.allowed-blocks-use-ids")) {
            signbtype = Integer.toString(b.getType().getId());
        } else {
            signbtype = b.getType().name();
        }

        List<String> blocks = RPConfig.getStringList("private.allowed-blocks");

        if (blocks.stream().anyMatch(signbtype::matches)) {
            for (int sx = -1; sx <= 1; sx++) {
                for (int sy = -1; sy <= 1; sy++) {
                    for (int sz = -1; sz <= 1; sz++) {
                        Block bs = w.getBlockAt(x + sx, y + sy, z + sz);
                        if (bs.getState() instanceof Sign && (validatePrivateSign(bs))) {
                            deny = false;
                            if (validateBreakSign(bs, p)) {
                                return true;
                            }
                        }

                        String blocktype2;
                        if (RPConfig.getBool("private.allowed-blocks-use-ids")) {
                            blocktype2 = Integer.toString(b.getType().getId());
                        } else {
                            blocktype2 = b.getType().name();
                        }

                        int x2 = bs.getX();
                        int y2 = bs.getY();
                        int z2 = bs.getZ();

                        if (blocks.stream().anyMatch(blocktype2::matches)) {
                            for (int ux = -1; ux <= 1; ux++) {
                                for (int uy = -1; uy <= 1; uy++) {
                                    for (int uz = -1; uz <= 1; uz++) {
                                        Block bu = w.getBlockAt(x2 + ux, y2 + uy, z2 + uz);
                                        if (bu.getState() instanceof Sign && (validatePrivateSign(bu))) {
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

    @SuppressWarnings("deprecation")
    public boolean canWorldBreak(Block b) {
        if (!RPConfig.getBool("private.use")) {
            return true;
        }
        Region reg = RedProtect.get().rm.getTopRegion(b.getLocation());
        if (reg == null && !RPConfig.getBool("private.allow-outside")) {
            return true;
        }
        int x = b.getX();
        int y = b.getY();
        int z = b.getZ();
        World w = b.getWorld();

        if (b.getState() instanceof Sign && validWorldBreak(b)) {
            RedProtect.get().logger.debug("Valid Sign on canWorldBreak!");
            return false;
        }

        String signbtype;
        if (RPConfig.getBool("private.allowed-blocks-use-ids")) {
            signbtype = Integer.toString(b.getType().getId());
        } else {
            signbtype = b.getType().name();
        }

        List<String> blocks = RPConfig.getStringList("private.allowed-blocks");

        if (blocks.stream().anyMatch(signbtype::matches)) {
            for (int sx = -1; sx <= 1; sx++) {
                for (int sz = -1; sz <= 1; sz++) {
                    Block bs = w.getBlockAt(x + sx, y, z + sz);
                    if (bs.getState() instanceof Sign && validWorldBreak(bs)) {
                        return false;
                    }

                    String blocktype2;
                    if (RPConfig.getBool("private.allowed-blocks-use-ids")) {
                        blocktype2 = Integer.toString(b.getType().getId());
                    } else {
                        blocktype2 = b.getType().name();
                    }

                    int x2 = bs.getX();
                    int y2 = bs.getY();
                    int z2 = bs.getZ();

                    if (blocks.stream().anyMatch(blocktype2::matches)) {
                        for (int ux = -1; ux <= 1; ux++) {
                            for (int uz = -1; uz <= 1; uz++) {
                                Block bu = w.getBlockAt(x2 + ux, y2, z2 + uz);
                                if (bu.getState() instanceof Sign && validWorldBreak(bu)) {
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

    public Block getBlockRelative(Block block) {
        if (block.getState() instanceof Sign) {
            Sign s = (Sign) block.getState();
            org.bukkit.material.Sign data = (org.bukkit.material.Sign) s.getData();
            return block.getRelative(data.getAttachedFace());
        }
        return null;
    }

    private boolean validWorldBreak(Block b) {
        return validatePrivateSign(b);
    }

    public boolean validatePrivateSign(String[] lines) {
        String priv = RPLang.get("blocklistener.container.signline");
        String line1 = lines[0];
        return line1.equalsIgnoreCase("[private]") ||
                line1.equalsIgnoreCase("private") ||
                line1.equalsIgnoreCase(priv) ||
                line1.equalsIgnoreCase("[" + priv + "]");
    }

    public boolean validatePrivateSign(Block b) {
        Sign s = (Sign) b.getState();
        return validatePrivateSign(s.getLines());
    }

    private boolean validateBreakSign(Block b, Player p) {
        Sign s = (Sign) b.getState();
        return validatePrivateSign(b) && s.getLine(1).equals(p.getName());
    }

    private boolean testPrivate(Block b, Player p) {
        Sign s = (Sign) b.getState();
        return validatePrivateSign(b) &&
                (s.getLine(1).equals(p.getName()) || s.getLine(2).equals(p.getName()) || s.getLine(3).equals(p.getName()));
    }

    private boolean validateOpenBlock(Block b, Player p) {
        return testPrivate(b, p);
    }

    @SuppressWarnings("deprecation")
    public boolean isContainer(Block b) {
        Block container = getBlockRelative(b);
        String signbtype;
        if (RPConfig.getBool("private.allowed-blocks-use-ids")) {
            signbtype = Integer.toString(container.getType().getId());
        } else {
            signbtype = container.getType().name();
        }

        return RPConfig.getStringList("private.allowed-blocks").stream().anyMatch(signbtype::matches);
    }

}
