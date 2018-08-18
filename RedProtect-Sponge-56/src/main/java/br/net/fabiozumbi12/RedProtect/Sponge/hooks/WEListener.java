package br.net.fabiozumbi12.RedProtect.Sponge.hooks;

import br.net.fabiozumbi12.RedProtect.Sponge.RPUtil;
import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import br.net.fabiozumbi12.RedProtect.Sponge.config.RPLang;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.selector.limit.SelectorLimits;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.sponge.SpongePlayer;
import com.sk89q.worldedit.sponge.SpongeWorld;
import com.sk89q.worldedit.sponge.SpongeWorldEdit;
import com.sk89q.worldedit.util.io.Closer;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.registry.WorldData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class WEListener {

    private static final HashMap<String, EditSession> eSessions = new HashMap<>();
	
	public static boolean undo(String rid){
		if (eSessions.containsKey(rid)){
			eSessions.get(rid).undo(eSessions.get(rid));
			return true;
		}
		return false;
	}

	private static void setSelection(SpongeWorld ws, Player p, Location pos1, Location pos2){
		SpongeWorldEdit worldEdit = SpongeWorldEdit.inst();
		RegionSelector regs = worldEdit.getSession(p).getRegionSelector(ws);
		regs.selectPrimary(new Vector(pos1.getX(),pos1.getY(),pos1.getZ()), null);
		regs.selectSecondary(new Vector(pos2.getX(),pos2.getY(),pos2.getZ()), null);
		worldEdit.getSession(p).setRegionSelector(ws, regs);
		RPLang.sendMessage(p ,RPLang.get("cmdmanager.region.select-we.show")
				.replace("{pos1}",pos1.getBlockX()+","+pos1.getBlockY()+","+pos1.getBlockZ())
				.replace("{pos2}",pos2.getBlockX()+","+pos2.getBlockY()+","+pos2.getBlockZ())
		);
		worldEdit.getSession(p).dispatchCUISelection(worldEdit.wrapPlayer(p));
	}

	public static void setSelectionRP(Player p, Location pos1, Location pos2){
		SpongeWorld ws = SpongeWorldEdit.inst().getWorld(p.getWorld());
		setSelection(ws, p, pos1, pos2);
	}

	public static void setSelectionFromRP(Player p, Location pos1, Location pos2){
		SpongeWorldEdit worldEdit = SpongeWorldEdit.inst();
		SpongeWorld ws = SpongeWorldEdit.inst().getWorld(p.getWorld());
		if (worldEdit.getSession(p) == null || !worldEdit.getSession(p).isSelectionDefined(ws)){
			setSelection(ws, p, pos1, pos2);
		} else {
			worldEdit.getSession(p).getRegionSelector(ws).clear();
			RPLang.sendMessage(p,RPLang.get("cmdmanager.region.select-we.hide"));
		}
		worldEdit.getSession(p).dispatchCUISelection(worldEdit.wrapPlayer(p));
	}

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
		    		    
		    WorldData worldData = ws.getWorldData();
		    Clipboard clipboard = reader.read(ws.getWorldData());
		    session.setClipboard(new ClipboardHolder(clipboard, worldData));
		    
		    ClipboardHolder holder = session.getClipboard();
		    
		    Operation op = holder.createPaste(session.createEditSession(sp), ws.getWorldData()).to(session.getPlacementPosition(sp)).build();
		    Operations.completeLegacy(op);
		} catch (IOException | MaxChangedBlocksException | EmptyClipboardException | IncompleteRegionException e) {
			e.printStackTrace();
		}		
	}
	
    public static void regenRegion(final br.net.fabiozumbi12.RedProtect.Sponge.Region r, final World w, final Location<World> p1, final Location<World> p2, final int delay, final CommandSource sender, final boolean remove) {
    	Sponge.getScheduler().createSyncExecutor(RedProtect.get().container).schedule(() -> {
            if (RPUtil.stopRegen){
                return;
            }
            Region wreg = new CuboidRegion(new Vector(p1.getX(),p1.getY(),p1.getZ()), new Vector(p2.getX(),p2.getY(),p2.getZ())).getFaces();
            SpongeWorld ws = SpongeWorldEdit.inst().getWorld(w);
            EditSession esession = new EditSessionFactory().getEditSession(ws, -1);

            eSessions.put(r.getID(), esession);
            int delayCount = 1+delay/10;

            if (sender != null){
                if (ws.regenerate(wreg, esession)){
                    RPLang.sendMessage(sender,"["+delayCount+"]"+" &aRegion "+r.getID().split("@")[0]+" regenerated with success!");
                } else {
                    RPLang.sendMessage(sender,"["+delayCount+"]"+" &cTheres an error when regen the region "+r.getID().split("@")[0]+"!");
                }
            } else {
                if (ws.regenerate(wreg, esession)){
                    RedProtect.get().logger.warning("["+delayCount+"]"+" &aRegion "+r.getID().split("@")[0]+" regenerated with success!");
                } else {
                    RedProtect.get().logger.warning("["+delayCount+"]"+" &cTheres an error when regen the region "+r.getID().split("@")[0]+"!");
                }
            }

            if (remove){
            	r.notifyRemove();
                RedProtect.get().rm.remove(r, RedProtect.get().serv.getWorld(r.getWorld()).get());
            }

            },delay, TimeUnit.MILLISECONDS);
	}
}
