package br.net.fabiozumbi12.RedProtect.Sponge.listeners;

import br.net.fabiozumbi12.RedProtect.Sponge.LogLevel;
import br.net.fabiozumbi12.RedProtect.Sponge.RPContainer;
import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import br.net.fabiozumbi12.RedProtect.Sponge.Region;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class RPBlockListener78 {

    private static final RPContainer cont = new RPContainer();

    public RPBlockListener78(){
        RedProtect.get().logger.debug(LogLevel.BLOCKS,"Loaded RPBlockListener78...");
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onPiston(ChangeBlockEvent.Pre e){
        RedProtect.get().logger.debug(LogLevel.BLOCKS,"RPBlockListener78 - Is onChangeBlock event");

        EventContext context = e.getContext();
        Cause cause = e.getCause();
        LocatableBlock sourceLoc = cause.first(LocatableBlock.class).orElse(null);

        if (sourceLoc != null){
            RedProtect.get().logger.debug(LogLevel.BLOCKS,"sourceLoc");

            if (context.containsKey(EventContextKeys.PISTON_EXTEND) || context.containsKey(EventContextKeys.PISTON_RETRACT)){
                if (RedProtect.get().cfgs.root().performance.disable_PistonEvent_handler){
                    return;
                }

                Region r = RedProtect.get().rm.getTopRegion(sourceLoc.getLocation(), this.getClass().getName());
                for (Location<World> pistonLoc: e.getLocations()){
                    Region targetr = RedProtect.get().rm.getTopRegion(pistonLoc, this.getClass().getName());

                    boolean antih = RedProtect.get().cfgs.root().region_settings.anti_hopper;
                    RedProtect.get().logger.debug(LogLevel.BLOCKS,"getLocations");

                    if (targetr != null && (r == null || r != targetr)){
                        if (cause.first(Player.class).isPresent() && targetr.canBuild(cause.first(Player.class).get())){
                            continue;
                        }

                        if (antih){
                            BlockSnapshot ib = e.getLocations().get(0).add(0, 1, 0).createSnapshot();
                            if (cont.canWorldBreak(ib) || cont.canWorldBreak(e.getLocations().get(0).createSnapshot())){
                                continue;
                            }
                        }
                        e.setCancelled(true);
                        break;
                    }
                }
            }
        }
    }
}
