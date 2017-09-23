package br.net.fabiozumbi12.RedProtect.Sponge;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class RPDoor {
	
	public static void ChangeDoor(BlockSnapshot b, Region r){		
		if ((!RedProtect.cfgs.isFlagEnabled("smart-door") && !RedProtect.cfgs.getBool("flags.smart-door")) || !r.getFlagBool("smart-door")){
			return;
		}
		
		Location<World> loc = b.getLocation().get();
		World w = loc.getExtent();
		
		if (isDoor(b)){
			changeDoorState(b);
			if (getDoorState(b)){
				w.playSound(SoundTypes.BLOCK_CHEST_CLOSE, loc.getPosition(), 1);
			} else {
				w.playSound(SoundTypes.BLOCK_CHEST_OPEN, loc.getPosition(), 1);
			}
		}		
		
		//check side block if is door
		BlockSnapshot[] block = new BlockSnapshot[4];
		block[0] = w.createSnapshot(loc.getBlockX()+1, loc.getBlockY(), loc.getBlockZ());
		block[1] = w.createSnapshot(loc.getBlockX()-1, loc.getBlockY(), loc.getBlockZ());
		block[2] = w.createSnapshot(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()+1);
		block[3] = w.createSnapshot(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()-1);		
		
		for (BlockSnapshot b2:block){
			if (b.getExtendedState().getType().getName().equals(b2.getExtendedState().getType().getName())){
				//b.getWorld().playEffect(b.getLocation(), Effect.DOOR_TOGGLE, 0);
				changeDoorState(b);
				break;
			}
		}
	}	
	
	static boolean getDoorState(BlockSnapshot b) {
		return b.getLocation().get().get(Keys.OPEN).orElse(false);
    }
	
	static void changeDoorState(BlockSnapshot b) {
		b.getLocation().get().offer(Keys.OPEN, !getDoorState(b));		
    }
	
	public static boolean isDoor(BlockSnapshot b){
		return b.getExtendedState().getType().getName().contains("_door") && !b.getExtendedState().getType().getName().contains("iron_");		
	}
	
	public static boolean isOpenable(BlockSnapshot b){
		return b.getLocation().isPresent() && b.getLocation().get().get(Keys.OPEN).isPresent();		
	}
}
