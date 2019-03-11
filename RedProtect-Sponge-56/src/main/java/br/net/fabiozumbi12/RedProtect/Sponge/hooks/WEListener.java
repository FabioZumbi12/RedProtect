/*
 Copyright @FabioZumbi12

 This class is provided 'as-is', without any express or implied warranty. In no event will the authors be held liable for any
  damages arising from the use of this class.

 Permission is granted to anyone to use this class for any purpose, including commercial plugins, and to alter it and
 redistribute it freely, subject to the following restrictions:
 1 - The origin of this class must not be misrepresented; you must not claim that you wrote the original software. If you
 use this class in other plugins, an acknowledgment in the plugin documentation would be appreciated but is not required.
 2 - Altered source versions must be plainly marked as such, and must not be misrepresented as being the original class.
 3 - This notice may not be removed or altered from any source distribution.

 Esta classe é fornecida "como está", sem qualquer garantia expressa ou implícita. Em nenhum caso os autores serão
 responsabilizados por quaisquer danos decorrentes do uso desta classe.

 É concedida permissão a qualquer pessoa para usar esta classe para qualquer finalidade, incluindo plugins pagos, e para
 alterá-lo e redistribuí-lo livremente, sujeito às seguintes restrições:
 1 - A origem desta classe não deve ser deturpada; você não deve afirmar que escreveu a classe original. Se você usar esta
  classe em um plugin, uma confirmação de autoria na documentação do plugin será apreciada, mas não é necessária.
 2 - Versões de origem alteradas devem ser claramente marcadas como tal e não devem ser deturpadas como sendo a
 classe original.
 3 - Este aviso não pode ser removido ou alterado de qualquer distribuição de origem.
 */

package br.net.fabiozumbi12.RedProtect.Sponge.hooks;

import br.net.fabiozumbi12.RedProtect.Sponge.RPUtil;
import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import br.net.fabiozumbi12.RedProtect.Sponge.config.RPLang;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.sponge.SpongeWorld;
import com.sk89q.worldedit.sponge.SpongeWorldEdit;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class WEListener {

    private static final HashMap<String, EditSession> eSessions = new HashMap<>();

    public static boolean undo(String rid) {
        if (eSessions.containsKey(rid)) {
            eSessions.get(rid).undo(eSessions.get(rid));
            return true;
        }
        return false;
    }

    private static void setSelection(SpongeWorld ws, Player p, Location pos1, Location pos2) {
        RegionSelector regs = SpongeWorldEdit.inst().getSession(p).getRegionSelector(ws);
        regs.selectPrimary(BlockVector3.at(pos1.getX(), pos1.getY(), pos1.getZ()), null);
        regs.selectSecondary(BlockVector3.at(pos2.getX(), pos2.getY(), pos2.getZ()), null);
        SpongeWorldEdit.inst().getSession(p).setRegionSelector(ws, regs);
        RPLang.sendMessage(p, RPLang.get("cmdmanager.region.select-we.show")
                .replace("{pos1}", pos1.getBlockX() + "," + pos1.getBlockY() + "," + pos1.getBlockZ())
                .replace("{pos2}", pos2.getBlockX() + "," + pos2.getBlockY() + "," + pos2.getBlockZ())
        );
        SpongeWorldEdit.inst().getSession(p).dispatchCUISelection(SpongeWorldEdit.inst().wrapPlayer(p));
    }

    public static void setSelectionRP(Player p, Location pos1, Location pos2) {
        SpongeWorld ws = SpongeWorldEdit.inst().getWorld(p.getWorld());
        setSelection(ws, p, pos1, pos2);
    }

    public static void setSelectionFromRP(Player p, Location pos1, Location pos2) {
        SpongeWorldEdit worldEdit = SpongeWorldEdit.inst();
        SpongeWorld ws = SpongeWorldEdit.inst().getWorld(p.getWorld());
        if (worldEdit.getSession(p) == null || !worldEdit.getSession(p).isSelectionDefined(ws)) {
            setSelection(ws, p, pos1, pos2);
        } else {
            worldEdit.getSession(p).getRegionSelector(ws).clear();
            RPLang.sendMessage(p, RPLang.get("cmdmanager.region.select-we.hide"));
        }
        worldEdit.getSession(p).dispatchCUISelection(worldEdit.wrapPlayer(p));
    }

    /*
        public static void pasteWithWE(Player p, File f) throws DataException {
            SpongePlayer sp = SpongeWorldEdit.inst().wrapPlayer(p);
            SpongeWorld ws = SpongeWorldEdit.inst().getWorld(p.getWorld());

            LocalSession session = SpongeWorldEdit.inst().getSession(p);

            Closer closer = Closer.create();
            try {
                ClipboardFormat format = ClipboardFormat.findByAlias("schematic");
                FileInputStream fis = closer.register(new FileInputStream(f));
                BufferedInputStream bis = closer.register(new BufferedInputStream(fis));
                ClipboardReader reader = format.getReader(bis);

                Clipboard clipboard = reader.read();
                session.setClipboard(new ClipboardHolder(clipboard));

                ClipboardHolder holder = session.getClipboard();

                Operation op = holder.createPaste(ws).to(session.getPlacementPosition(sp)).build();
                Operations.completeLegacy(op);
            } catch (IOException | MaxChangedBlocksException | EmptyClipboardException | IncompleteRegionException e) {
                e.printStackTrace();
            }
        }
    */
    public static void regenRegion(final br.net.fabiozumbi12.RedProtect.Sponge.Region r, final World w, final Location<World> p1, final Location<World> p2, final int delay, final CommandSource sender, final boolean remove) {
        Sponge.getScheduler().createSyncExecutor(RedProtect.get().container).schedule(() -> {
            if (RPUtil.stopRegen) {
                return;
            }

            RegionSelector regs = new LocalSession().getRegionSelector(SpongeWorldEdit.inst().getWorld(w));
            regs.selectPrimary(BlockVector3.at(p1.getX(), p1.getY(), p1.getZ()), null);
            regs.selectSecondary(BlockVector3.at(p2.getX(), p2.getY(), p2.getZ()), null);

            Region wreg = null;
            try {
                wreg = regs.getRegion();
            } catch (IncompleteRegionException e1) {
                e1.printStackTrace();
            }

            EditSession esession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(SpongeWorldEdit.inst().getWorld(w), -1);

            eSessions.put(r.getID(), esession);
            int delayCount = 1 + delay / 10;

            if (sender != null) {
                if (wreg.getWorld().regenerate(wreg, esession)) {
                    RPLang.sendMessage(sender, "[" + delayCount + "]" + " &aRegion " + r.getID().split("@")[0] + " regenerated with success!");
                } else {
                    RPLang.sendMessage(sender, "[" + delayCount + "]" + " &cTheres an error when regen the region " + r.getID().split("@")[0] + "!");
                }
            } else {
                if (wreg.getWorld().regenerate(wreg, esession)) {
                    RedProtect.get().logger.warning("[" + delayCount + "]" + " &aRegion " + r.getID().split("@")[0] + " regenerated with success!");
                } else {
                    RedProtect.get().logger.warning("[" + delayCount + "]" + " &cTheres an error when regen the region " + r.getID().split("@")[0] + "!");
                }
            }

            if (remove) {
                r.notifyRemove();
                RedProtect.get().rm.remove(r, RedProtect.get().serv.getWorld(r.getWorld()).get());
            }

        }, delay, TimeUnit.MILLISECONDS);
    }
}
