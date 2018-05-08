package br.net.fabiozumbi12.RedProtect.Sponge.listeners;

import br.net.fabiozumbi12.RedProtect.Sponge.RPContainer;
import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import br.net.fabiozumbi12.RedProtect.Sponge.Region;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;

public class RPBlockListener56 {

    private static final RPContainer cont = new RPContainer();

    public RPBlockListener56(){
        RedProtect.get().logger.debug("blocks","Loaded RPBlockListener56...");
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPiston(ChangeBlockEvent.Pre e){
        Location<World> piston = null;
        Location<World> block = null;
        boolean antih = RedProtect.get().cfgs.getBool("region-settings.anti-hopper");

        if (RedProtect.get().getPVHelper().checkCause(e.getCause(), "PISTON_EXTEND")) {
            if (RedProtect.get().cfgs.getBool("performance.disable-PistonEvent-handler")){
                return;
            }

            List<Location<World>> locs = e.getLocations();
            for (Location<World> loc:locs){
                if (piston == null){
                    piston = loc;
                    continue;
                }
                block = loc;
            }
        }

        if (RedProtect.get().getPVHelper().checkCause(e.getCause(), "PISTON_RETRACT")) {
            if (RedProtect.get().cfgs.getBool("performance.disable-PistonEvent-handler")){
                return;
            }

            List<Location<World>> locs = e.getLocations();
            for (Location<World> loc:locs){
                if (piston == null){
                    piston = loc;
                    continue;
                }
                block = loc;
            }
        }

        //process
        if (piston != null && block != null){
            Region rPi = RedProtect.get().rm.getTopRegion(piston);
            Region rB = RedProtect.get().rm.getTopRegion(block);
            if (rPi == null && rB != null || (rPi != null && rB != null && rPi != rB && !rPi.sameLeaders(rB))){
                e.setCancelled(true);
                return;
            }

            if (antih){
                BlockSnapshot ib = block.add(0, 1, 0).createSnapshot();
                if (!cont.canWorldBreak(ib) || !cont.canWorldBreak(block.createSnapshot())){
                    e.setCancelled(true);
                }
            }
        }
    }
}
