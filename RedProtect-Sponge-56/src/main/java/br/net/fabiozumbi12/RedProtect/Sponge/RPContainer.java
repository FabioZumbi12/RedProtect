package br.net.fabiozumbi12.RedProtect.Sponge;

import br.net.fabiozumbi12.RedProtect.Sponge.config.RPLang;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.TileEntityTypes;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Arrays;
import java.util.List;

public class RPContainer {

	public boolean canOpen(BlockSnapshot b, Player p) {
    	if (!RedProtect.get().cfgs.root().private_cat.use || p.hasPermission("redprotect.bypass")){
    		return true;
    	}

    	List<Direction> dirs = Arrays.asList(Direction.EAST,Direction.NORTH,Direction.SOUTH,Direction.WEST);
    	String blocktype = b.getState().getType().getName();
    	Location<World> loc = b.getLocation().get();
    	List<String> blocks = RedProtect.get().cfgs.root().private_cat.allowed_blocks;
		boolean deny = true;
        if (blocks.stream().anyMatch(blocktype::matches)){
        	for (Direction dir:dirs){        		
        		Location<World> loc1 = loc.getBlockRelative(dir);
        		if (isSign(loc1.createSnapshot())){
					deny = false;
            		if (validateOpenBlock(loc1.createSnapshot(), p)){
            			return true;
            		}
            	} 
        		        		
        		if (blocks.stream().anyMatch(loc1.getBlockType().getName()::matches) && loc1.getBlockType().equals(b.getState().getType())){
        			for (Direction dir2:dirs){
        				Location<World> loc3 = loc1.getBlockRelative(dir2);
            			if (isSign(loc3.createSnapshot())){
							deny = false;
            				if (validateOpenBlock(loc3.createSnapshot(), p)){
                    			return true;
                    		}
                    	}
        			}
        		}        		
        	}           
        }
		return deny;
    }
	
	public boolean canBreak(Player p, BlockSnapshot b){
    	if (!RedProtect.get().cfgs.root().private_cat.use){
    		return true;
    	}
    	
    	Region reg = RedProtect.get().rm.getTopRegion(b.getLocation().get(), this.getClass().getName());
    	if (reg == null && !RedProtect.get().cfgs.root().private_cat.allow_outside){
    		return true;
    	}
    	
    	List<Direction> dirs = Arrays.asList(Direction.EAST,Direction.NORTH,Direction.SOUTH,Direction.WEST,Direction.UP,Direction.DOWN);
    	String blocktype = b.getState().getType().getName();
    	Location<World> loc = b.getLocation().get();
    	List<String> blocks = RedProtect.get().cfgs.root().private_cat.allowed_blocks;

		boolean deny = true;
    	
    	if (isSign(loc.createSnapshot()) && validatePrivateSign(b)){
			deny = false;
    		if (validateBreakSign(loc.createSnapshot(), p)){
    			return true;
    		}
    	}
    	
        if (blocks.stream().anyMatch(blocktype::matches)){
        	for (Direction dir:dirs){        		
        		Location<World> loc1 = loc.getBlockRelative(dir);
        		if (isSign(loc1.createSnapshot())){
					deny = false;
            		if (validateBreakSign(loc1.createSnapshot(), p)){
            			return true;
            		}
            	} 
        		        		
        		if (blocks.stream().anyMatch(loc1.getBlockType().getName()::matches) && loc1.getBlockType().equals(b.getState().getType())){
        			for (Direction dir2:dirs){
        				Location<World> loc3 = loc1.getBlockRelative(dir2);
            			if (isSign(loc3.createSnapshot())){
							deny = false;
            				if (validateBreakSign(loc3.createSnapshot(), p)){
                    			return true;
                    		}
                    	}
        			}
        		}        		
        	}           
        }        
        return deny;
    }
    
	public boolean canWorldBreak(BlockSnapshot b){	
		if (!RedProtect.get().cfgs.root().private_cat.use){
    		return true;
    	}    	
    	
    	Region reg = RedProtect.get().rm.getTopRegion(b.getLocation().get(), this.getClass().getName());
    	if (reg == null && !RedProtect.get().cfgs.root().private_cat.allow_outside){
    		return true;
    	}
    	
    	List<Direction> dirs = Arrays.asList(Direction.EAST,Direction.NORTH,Direction.SOUTH,Direction.WEST,Direction.UP,Direction.DOWN);
    	String blocktype = b.getState().getType().getName();
    	Location<World> loc = b.getLocation().get();
    	List<String> blocks = RedProtect.get().cfgs.root().private_cat.allowed_blocks;
    	
    	if (isSign(loc.createSnapshot())){
			BlockSnapshot sign1 = loc.createSnapshot();
    		if (validWorldBreak(sign1)){
    			return false;
    		}
    	}
    	
        if (blocks.stream().anyMatch(blocktype::matches)){
        	for (Direction dir:dirs){        		
        		Location<World> loc1 = loc.getBlockRelative(dir);
        		if (isSign(loc1.createSnapshot())){
        			BlockSnapshot sign1 = loc1.createSnapshot();
            		if (validWorldBreak(sign1)){
            			return false;
            		}
            	} 
        		        		
        		if (blocks.stream().anyMatch(loc1.getBlockType().getName()::matches) && loc1.getBlockType().equals(b.getState().getType())){
        			for (Direction dir2:dirs){
        				Location<World> loc3 = loc1.getBlockRelative(dir2);
            			if (isSign(loc3.createSnapshot())){
            				BlockSnapshot sign2 = loc3.createSnapshot();
            				if (validWorldBreak(sign2)){
                    			return false;
                    		}
                    	}
        			}
        		}        		
        	}           
        }
        return true;
    }

	private boolean validWorldBreak(BlockSnapshot b){
		return validatePrivateSign(b);
	}

	public boolean validatePrivateSign(String line){
		String priv = RPLang.get("blocklistener.container.signline");

		return line.equalsIgnoreCase("[private]") ||
				line.equalsIgnoreCase("private") ||
				line.equalsIgnoreCase(priv) ||
				line.equalsIgnoreCase("[" + priv + "]");
	}

	public boolean validatePrivateSign(BlockSnapshot b){
		String line = b.getLocation().get().get(Keys.SIGN_LINES).get().get(0).toPlain();
		return validatePrivateSign(line);
	}

	private boolean validateBreakSign(BlockSnapshot b, Player p){
		String line1 = b.getLocation().get().get(Keys.SIGN_LINES).get().get(1).toPlain();
		return (validatePrivateSign(b) && (line1.isEmpty() || line1.equals(p.getName())));
	}

	private boolean validateOpenBlock(BlockSnapshot b, Player p){
		return testPrivate(b,p);
	}

	private boolean testPrivate(BlockSnapshot b, Player p){
		List<Text> lines = b.getLocation().get().get(Keys.SIGN_LINES).get();
		return  validatePrivateSign(b) &&
				(lines.get(1).toPlain().equals(p.getName()) || lines.get(2).toPlain().equals(p.getName()) || lines.get(3).toPlain().equals(p.getName()));
	}

	public boolean isContainer(BlockSnapshot block){
		Location<World> loc = block.getLocation().get().getBlockRelative(block.getLocation().get().get(Keys.DIRECTION).get().getOpposite());
		List<String> blocks = RedProtect.get().cfgs.root().private_cat.allowed_blocks;

		return blocks.stream().anyMatch(loc.getBlockType().getName()::matches);
	}

	public boolean isSign(BlockSnapshot b){
		return (b.getState().getType().equals(BlockTypes.STANDING_SIGN) || b.getState().getType().equals(BlockTypes.WALL_SIGN)) && b.getLocation().get().get(Keys.SIGN_LINES).isPresent();
	}
}
