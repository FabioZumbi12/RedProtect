package br.net.fabiozumbi12.RedProtect.Sponge;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.PortionTypes;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class RPDoor {

    public static void ChangeDoor(BlockSnapshot b, Region r) {
        if ((!RedProtect.get().cfgs.isFlagEnabled("smart-door") && !RedProtect.get().cfgs.root().flags.get("smart-door")) || !r.getFlagBool("smart-door")) {
            return;
        }

        Location<World> loc = b.getLocation().get();
        World w = loc.getExtent();

        if (isDoor(b)) {
            boolean iron = b.getState().getType() == BlockTypes.IRON_DOOR;
            if (iron) {
                changeDoorState(b);
                if (getDoorState(b)) {
                    w.playSound(SoundTypes.BLOCK_IRON_DOOR_OPEN, loc.getPosition(), 1);
                } else {
                    w.playSound(SoundTypes.BLOCK_IRON_DOOR_CLOSE, loc.getPosition(), 1);
                }
            }

            if (loc.getRelative(Direction.DOWN).getBlock().getType() == b.getState().getType() && loc.get(Keys.PORTION_TYPE).get() == PortionTypes.TOP) {
                loc = loc.getRelative(Direction.DOWN);
            }

            //check side block if is door
            BlockSnapshot[] block = new BlockSnapshot[4];
            block[0] = loc.getRelative(Direction.EAST).createSnapshot();
            block[1] = loc.getRelative(Direction.WEST).createSnapshot();
            block[2] = loc.getRelative(Direction.NORTH).createSnapshot();
            block[3] = loc.getRelative(Direction.SOUTH).createSnapshot();

            for (BlockSnapshot b2 : block) {
                if (b.getState().getType() == b2.getState().getType()) {
                    changeDoorState(b2);
                    break;
                }
            }
        }
    }

    //wooden door
    private static boolean getDoorState(BlockSnapshot b) {
        return b.getLocation().get().get(Keys.OPEN).get();
    }

    private static void changeDoorState(BlockSnapshot b) {
        b.getLocation().get().offer(Keys.OPEN, !getDoorState(b));
    }

    private static boolean isDoor(BlockSnapshot b) {
        return b.getState().getType() == BlockTypes.WOODEN_DOOR || b.getState().getType() == BlockTypes.IRON_DOOR;
    }

    public static boolean isOpenable(BlockSnapshot b) {
        return b.getLocation().isPresent() && b.getLocation().get().get(Keys.OPEN).isPresent();
    }
}
