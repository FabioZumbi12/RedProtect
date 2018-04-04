package br.net.fabiozumbi12.RedProtect.Sponge;

import java.util.Arrays;
import java.util.List;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.TileEntityTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.block.DirectionalData;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import br.net.fabiozumbi12.RedProtect.Sponge.config.RPLang;

public class RPContainer {

	public boolean canOpen(BlockSnapshot b, Player p) {
    	if (!RedProtect.get().cfgs.getBool("private.use")){
    		return true;
    	}
    	
    	List<Direction> dirs = Arrays.asList(Direction.EAST,Direction.NORTH,Direction.SOUTH,Direction.WEST);
    	String blocktype = b.getState().getType().getName();
    	Location<World> loc = b.getLocation().get();
    	World w = loc.getExtent();
    	List<String> blocks = RedProtect.get().cfgs.getStringList("private.allowed-blocks");
    	
        if (blocks.stream().anyMatch(blocktype::matches)){
        	for (Direction dir:dirs){        		
        		Location<World> loc1 = getRelative(loc, dir);        		
        		if (isSign(loc1.getBlockType())){
        			BlockSnapshot sign1 = w.createSnapshot(loc1.getBlockPosition());
            		if (!validateOpenBlock(sign1, p)){
            			return false;
            		}
            	} 
        		        		
        		if (blocks.stream().anyMatch(loc1.getBlockType().getName()::matches) && loc1.getBlockType().equals(b.getState().getType())){
        			for (Direction dir2:dirs){
        				Location<World> loc3 = getRelative(loc1, dir2);              			
            			if (isSign(loc3.getBlockType())){
            				BlockSnapshot sign2 = w.createSnapshot(loc3.getBlockPosition());
            				if (!validateOpenBlock(sign2, p)){
                    			return false;
                    		}
                    	}
        			}
        		}        		
        	}           
        }
		return true;        
    }
	
	public boolean canBreak(Player p, BlockSnapshot b){
    	if (!RedProtect.get().cfgs.getBool("private.use")){
    		return true;
    	}
    	
    	
    	Region reg = RedProtect.get().rm.getTopRegion(b.getLocation().get());
    	if (reg == null && !RedProtect.get().cfgs.getBool("private.allow-outside")){
    		return true;
    	}
    	
    	List<Direction> dirs = Arrays.asList(Direction.EAST,Direction.NORTH,Direction.SOUTH,Direction.WEST,Direction.UP,Direction.DOWN);
    	String blocktype = b.getState().getType().getName();
    	Location<World> loc = b.getLocation().get();
    	World w = loc.getExtent();
    	List<String> blocks = RedProtect.get().cfgs.getStringList("private.allowed-blocks");
    	
    	if (isSign(loc.getBlockType())){
			BlockSnapshot sign1 = w.createSnapshot(loc.getBlockPosition());
    		if (!validateBreakSign(sign1, p)){
    			return false;
    		}
    	}
    	
        if (blocks.stream().anyMatch(blocktype::matches)){
        	for (Direction dir:dirs){        		
        		Location<World> loc1 = getRelative(loc, dir);        		
        		if (isSign(loc1.getBlockType())){
        			BlockSnapshot sign1 = w.createSnapshot(loc1.getBlockPosition());
            		if (!validateBreakSign(sign1, p)){
            			return false;
            		}
            	} 
        		        		
        		if (blocks.stream().anyMatch(loc1.getBlockType().getName()::matches) && loc1.getBlockType().equals(b.getState().getType())){
        			for (Direction dir2:dirs){
        				Location<World> loc3 = getRelative(loc1, dir2);              			
            			if (isSign(loc3.getBlockType())){
            				BlockSnapshot sign2 = w.createSnapshot(loc3.getBlockPosition());
            				if (!validateBreakSign(sign2, p)){
                    			return false;
                    		}
                    	}
        			}
        		}        		
        	}           
        }        
        return true;
    }
    
	public boolean canWorldBreak(BlockSnapshot b){	
		if (!RedProtect.get().cfgs.getBool("private.use")){
    		return true;
    	}    	
    	
    	Region reg = RedProtect.get().rm.getTopRegion(b.getLocation().get());
    	if (reg == null && !RedProtect.get().cfgs.getBool("private.allow-outside")){
    		return true;
    	}
    	
    	List<Direction> dirs = Arrays.asList(Direction.EAST,Direction.NORTH,Direction.SOUTH,Direction.WEST,Direction.UP,Direction.DOWN);
    	String blocktype = b.getState().getType().getName();
    	Location<World> loc = b.getLocation().get();
    	World w = loc.getExtent();
    	List<String> blocks = RedProtect.get().cfgs.getStringList("private.allowed-blocks");
    	
    	if (isSign(loc.getBlockType())){
			BlockSnapshot sign1 = w.createSnapshot(loc.getBlockPosition());
    		if (!validatePrivateSign(sign1)){
    			return false;
    		}
    	}
    	
        if (blocks.stream().anyMatch(blocktype::matches)){
        	for (Direction dir:dirs){        		
        		Location<World> loc1 = getRelative(loc, dir);        		
        		if (isSign(loc1.getBlockType())){
        			BlockSnapshot sign1 = w.createSnapshot(loc1.getBlockPosition());
            		if (!validatePrivateSign(sign1)){
            			return false;
            		}
            	} 
        		        		
        		if (blocks.stream().anyMatch(loc1.getBlockType().getName()::matches) && loc1.getBlockType().equals(b.getState().getType())){
        			for (Direction dir2:dirs){
        				Location<World> loc3 = getRelative(loc1, dir2);              			
            			if (isSign(loc3.getBlockType())){
            				BlockSnapshot sign2 = w.createSnapshot(loc3.getBlockPosition());
            				if (!validatePrivateSign(sign2)){
                    			return false;
                    		}
                    	}
        			}
        		}        		
        	}           
        }
        return true;
    }

	private boolean validatePrivateSign(BlockSnapshot b){
		if (!isSign(b.getState().getType()) || !b.get(Keys.SIGN_LINES).isPresent()){
			return true;
		}
		String line = b.get(Keys.SIGN_LINES).get().get(0).toPlain();
		return line.equalsIgnoreCase("[private]") || line.equalsIgnoreCase("private") || line.equalsIgnoreCase(RPLang.get("blocklistener.container.signline")) || line.equalsIgnoreCase("[" + RPLang.get("blocklistener.container.signline") + "]");
	}
	
	private boolean validateBreakSign(BlockSnapshot b, Player p){
		if (!isSign(b.getState().getType()) || !b.get(Keys.SIGN_LINES).isPresent()){
			return true;
		}
		String line = b.get(Keys.SIGN_LINES).get().get(0).toPlain();
		String line1 = b.get(Keys.SIGN_LINES).get().get(1).toPlain();
		return (!line.equalsIgnoreCase("[private]") && !line.equalsIgnoreCase("private") && !line.equalsIgnoreCase(RPLang.get("blocklistener.container.signline")) && !line.equalsIgnoreCase("[" + RPLang.get("blocklistener.container.signline") + "]")) ||
				line1.equals(p.getName());
	}
	
	private boolean validateOpenBlock(BlockSnapshot b, Player p){
		if (!isSign(b.getState().getType()) || !b.get(Keys.SIGN_LINES).isPresent()){
			return true;
		}
		List<Text> lines = b.get(Keys.SIGN_LINES).get();
		return (!lines.get(0).toPlain().equalsIgnoreCase("[private]") && !lines.get(0).toPlain().equalsIgnoreCase("private") && !lines.get(0).toPlain().equalsIgnoreCase(RPLang.get("blocklistener.container.signline")) && !lines.get(0).toPlain().equalsIgnoreCase("[" + RPLang.get("blocklistener.container.signline") + "]")) ||
				(lines.get(1).toPlain().equals(p.getName()) ||
						lines.get(2).toPlain().equals(p.getName()) ||
						lines.get(3).toPlain().equals(p.getName()));
	}
	    
	public boolean isContainer(BlockSnapshot block){
		Location<World> loc = block.getLocation().get();
		List<String> blocks = RedProtect.get().cfgs.getStringList("private.allowed-blocks");

		return blocks.stream().anyMatch(b -> getRelative(loc, Direction.UP).getBlockType().getName().matches(b)) ||
		blocks.stream().anyMatch(b -> getRelative(loc, Direction.DOWN).getBlockType().getName().matches(b)) ||
		blocks.stream().anyMatch(b -> getRelative(loc, Direction.EAST).getBlockType().getName().matches(b)) ||
		blocks.stream().anyMatch(b -> getRelative(loc, Direction.NORTH).getBlockType().getName().matches(b)) ||
		blocks.stream().anyMatch(b -> getRelative(loc, Direction.SOUTH).getBlockType().getName().matches(b)) ||
		blocks.stream().anyMatch(b -> getRelative(loc, Direction.WEST).getBlockType().getName().matches(b));
		/*return blocks.contains(getRelative(loc, Direction.DOWN).getBlockType().getName()) ||
				blocks.contains(getRelative(loc, Direction.UP).getBlockType().getName()) ||
				blocks.contains(getRelative(loc, Direction.EAST).getBlockType().getName()) ||
				blocks.contains(getRelative(loc, Direction.NORTH).getBlockType().getName()) ||
				blocks.contains(getRelative(loc, Direction.SOUTH).getBlockType().getName()) ||
				blocks.contains(getRelative(loc, Direction.WEST).getBlockType().getName());*/
	}

	private boolean isSign(BlockType type){
		return type.equals(BlockTypes.STANDING_SIGN) || type.equals(BlockTypes.WALL_SIGN);
	}

	private Location<World> getRelative(Location<World> loc,Direction dir){
		return loc.getRelative(dir);
	}
}
