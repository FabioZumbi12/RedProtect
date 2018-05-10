package br.net.fabiozumbi12.RedProtect.Sponge.listeners;

import br.net.fabiozumbi12.RedProtect.Sponge.RPContainer;
import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import br.net.fabiozumbi12.RedProtect.Sponge.Region;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
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
        RedProtect.get().logger.debug("blocks","RPBlockListener78 - Is onChangeBlock event");

        Location<World> piston = null;
        Location<World> block = null;
        boolean antih = RedProtect.get().cfgs.root().region_settings.anti_hopper;

        if (RedProtect.get().getPVHelper().checkCause(e.getCause(), "PISTON_EXTEND")) {
            if (RedProtect.get().cfgs.root().performance.disable_PistonEvent_handler){
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
            if (RedProtect.get().cfgs.root().performance.disable_PistonEvent_handler){
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
            Region rPi = RedProtect.get().rm.getTopRegion(piston, this.getClass().getName());
            Region rB = RedProtect.get().rm.getTopRegion(block, this.getClass().getName());
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
