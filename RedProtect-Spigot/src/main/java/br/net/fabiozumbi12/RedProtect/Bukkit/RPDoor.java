package br.net.fabiozumbi12.RedProtect.Bukkit;

import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPConfig;
import org.bukkit.Effect;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Openable;

public class RPDoor {
	
	public static void ChangeDoor(Block b, Region r){		
		if ((!RPConfig.isFlagEnabled("smart-door") && !RPConfig.getBool("flags.smart-door")) || !r.getFlagBool("smart-door")){
			return;
		}

		if (b.getType().name().contains("IRON")){
			b.getWorld().playEffect(b.getLocation(), Effect.DOOR_TOGGLE, 0);
			toggleDoor(b);
		}		
				
		if (b.getType().name().contains("TRAP")){
			return;
		}
		
		//check side block if is door
		Block[] block = new Block[4];
		block[0] = b.getRelative(BlockFace.EAST);
		block[1] = b.getRelative(BlockFace.WEST);
		block[2] = b.getRelative(BlockFace.NORTH);
		block[3] = b.getRelative(BlockFace.SOUTH);
		
		for (Block b2:block){
			if (b.getType().equals(b2.getType())){
				toggleDoor(b2);
				break;
			}
		}
	}	
	
	private static void toggleDoor(Block b){
		if (b.getRelative(BlockFace.DOWN).getType().equals(b.getType())){
			b = b.getRelative(BlockFace.DOWN);			
		}
		Openable openable = (Openable) b.getBlockData();
		openable.setOpen(!openable.isOpen());
		b.setBlockData(openable);
	}
	
	public static boolean isOpenable(Block b){
		return b.getBlockData() instanceof Openable;
	}	
}
