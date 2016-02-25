package br.net.fabiozumbi12.RedProtect;

import org.bukkit.Effect;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.material.TrapDoor;

import br.net.fabiozumbi12.RedProtect.config.RPConfig;

@SuppressWarnings("deprecation")
public class RPDoor {
	
	public static void ChangeDoor(Block b, Region r){		
		if ((!RPConfig.isFlagEnabled("smart-door") && !RPConfig.getBool("flags.smart-door")) || !r.getFlagBool("smart-door")){
			return;
		}
		
		if (isIronDoor(b) || isIronTrapDoor(b)){
			b.getWorld().playEffect(b.getLocation(), Effect.DOOR_TOGGLE, 0);
			if (isDoorClosed(b)){
				openIronDoor(b);
			} else {
				closeIronDoor(b);	
			}
		}
		
		
		//check side block if is door
		Block[] block = new Block[4];
		block[0] = b.getWorld().getBlockAt(b.getX()+1, b.getY(), b.getZ());
		block[1] = b.getWorld().getBlockAt(b.getX()-1, b.getY(), b.getZ());
		block[2] = b.getWorld().getBlockAt(b.getX(), b.getY(), b.getZ()+1);
		block[3] = b.getWorld().getBlockAt(b.getX(), b.getY(), b.getZ()-1);		
		
		for (Block b2:block){
			if (b.getType().equals(b2.getType()) && (isIronDoor(b) || isDoor(b))){
				//b.getWorld().playEffect(b.getLocation(), Effect.DOOR_TOGGLE, 0);
				if (isDoorClosed(b)){
					closeIronDoor(b2);	
					openDoor(b2);
				} else {
					openIronDoor(b2);	
					closeDoor(b2);	
				}
				break;
			}
		}
	}	
	
	static boolean isDoorClosed(Block block) {
        if (isIronTrapDoor(block)) {
            TrapDoor trapdoor = (TrapDoor)block.getState().getData();
            return !trapdoor.isOpen();
        } else {
            byte data = block.getData();
            if ((data & 0x8) == 0x8) {
                block = block.getRelative(BlockFace.DOWN);
                data = block.getData();
            }
            return ((data & 0x4) == 0);
        }
    }
	
	static void openIronDoor(Block block) {		
        if (isIronTrapDoor(block)) {
            BlockState state = block.getState();
            TrapDoor trapdoor = (TrapDoor)state.getData();
            trapdoor.setOpen(true);
            state.update();
        } else if (isIronDoor(block)){
            byte data = block.getData();
            if ((data & 0x8) == 0x8) {
                block = block.getRelative(BlockFace.DOWN);
                data = block.getData();
            }
            if (isDoorClosed(block)) {
                data = (byte) (data | 0x4);
                block.setData(data, true);                
            }            
        }
    }
	
	static void closeIronDoor(Block block) {
        if (isIronTrapDoor(block)) {
            BlockState state = block.getState();
            TrapDoor trapdoor = (TrapDoor)state.getData();
            trapdoor.setOpen(false);
            state.update();
        } else if (isIronDoor(block)){
            byte data = block.getData();
            if ((data & 0x8) == 0x8) {
                block = block.getRelative(BlockFace.DOWN);
                data = block.getData();
            }
            if (!isDoorClosed(block)) {
                data = (byte) (data & 0xb);
                block.setData(data, true);                
            }
        }
    }
	
	static void openDoor(Block block) {		
        if (isDoor(block)){
            byte data = block.getData();
            if ((data & 0x8) == 0x8 && !isTrapDoor(block)) {
                block = block.getRelative(BlockFace.DOWN);
                data = block.getData();
            }
            if (isDoorClosed(block) && !isTrapDoor(block)) {
                data = (byte) (data | 0x4);
                block.setData(data, true);                
            }            
        }
    }
	
	static void closeDoor(Block block) {
        if (isDoor(block)){
            byte data = block.getData();
            if ((data & 0x8) == 0x8 && !isTrapDoor(block)) {
                block = block.getRelative(BlockFace.DOWN);
                data = block.getData();
            }
            if (!isDoorClosed(block) && !isTrapDoor(block)) {
                data = (byte) (data & 0xb);
                block.setData(data, true);                
            }
        }
    }
	
	public static boolean isIronTrapDoor(Block b){
		return b.getType().name().equals("IRON_TRAPDOOR");		
	}
	
	public static boolean isTrapDoor(Block b){
		return b.getType().name().equals("TRAP_DOOR");		
	}
	
	public static boolean isIronDoor(Block b){
		return b.getType().name().contains("IRON_DOOR");		
	}
	
	public static boolean isDoor(Block b){
		return b.getType().name().contains("_DOOR") && !b.getType().name().contains("IRON_");		
	}
	
	public static boolean isOpenable(Block b){
		return isDoor(b) || isIronDoor(b) || isTrapDoor(b) || isIronTrapDoor(b) || b.getType().name().contains("GATE");		
	}
}
