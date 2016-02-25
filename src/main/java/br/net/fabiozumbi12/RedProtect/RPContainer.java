package br.net.fabiozumbi12.RedProtect;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import br.net.fabiozumbi12.RedProtect.config.RPConfig;
import br.net.fabiozumbi12.RedProtect.config.RPLang;

public class RPContainer {

	@SuppressWarnings("deprecation")
	public boolean canOpen(Block b, Player p) {
    	if (!RPConfig.getBool("private.use")){
    		return true;
    	}
    	
        String blocktype;
        if (RPConfig.getBool("private.allowed-blocks-use-ids")){
        	blocktype = Integer.toString(b.getTypeId());
        } else {
        	blocktype = b.getType().name();
        }  
    	
        if (RPConfig.getStringList("private.allowed-blocks").contains(blocktype)){
        	int x = b.getX();
            int y = b.getY();
            int z = b.getZ();
            World w = p.getWorld();        
            
            for (int sx = -1; sx <= 1; sx++){
            	for (int sz = -1; sz <= 1; sz++){
    				Block bs = w.getBlockAt(x+sx, y, z+sz);
    				if (bs.getState() instanceof Sign && !validateOpenBlock(bs, p) && getBlockRelative(bs).getType().equals(b.getType())){
    					return false;
                	}
    		        
    		        int x2 = bs.getX();
    	            int y2 = bs.getY();
    	            int z2 = bs.getZ();
    	            
    	            String blocktype2;
    	            if (RPConfig.getBool("private.allowed-blocks-use-ids")){
    	            	blocktype2 = Integer.toString(b.getTypeId());
    	            } else {
    	            	blocktype2 = b.getType().name();
    	            }
    				if (RPConfig.getStringList("private.allowed-blocks").contains(blocktype2)){    					
    					for (int ux = -1; ux <= 1; ux++){
    						for (int uz = -1; uz <= 1; uz++){
    	        				Block bu = w.getBlockAt(x2+ux, y2, z2+uz);    	        				
    	        				if (bu.getState() instanceof Sign && !validateOpenBlock(bu, p) && getBlockRelative(bu).getType().equals(b.getType())){
    	        					return false;
    	                    	}
    	        			}        	        		
        	        	}
    				}
    			}        		
        	}
        }
		return true;        
    }
	
	@SuppressWarnings("deprecation")
	public boolean canBreak(Player p, Block b){
    	if (!RPConfig.getBool("private.use")){
    		return true;
    	}
    	Region reg = RedProtect.rm.getTopRegion(b.getLocation());
    	if (reg == null && !RPConfig.getBool("private.allow-outside")){
    		return true;
    	}
    	int x = b.getX();
        int y = b.getY();
        int z = b.getZ();
        World w = p.getWorld();

        if (b.getState() instanceof Sign && !validateBreakSign(b, p)){
			return false;
    	}   		
           		
        String signbtype;
        if (RPConfig.getBool("private.allowed-blocks-use-ids")){
        	signbtype = Integer.toString(b.getTypeId());
        } else {
        	signbtype = b.getType().name();
        } 
        
        if (RPConfig.getStringList("private.allowed-blocks").contains(signbtype)){
        	for (int sx = -1; sx <= 1; sx++){
        		for (int sy = -1; sy <= 1; sy++){
        			for (int sz = -1; sz <= 1; sz++){
        				Block bs = w.getBlockAt(x+sx, y+sy, z+sz);
        				if (bs.getState() instanceof Sign && !validateBreakSign(bs, p) && getBlockRelative(bs).getType().equals(b.getType())){
        					return false;
                    	}
        				
        				String blocktype2;
        	            if (RPConfig.getBool("private.allowed-blocks-use-ids")){
        	            	blocktype2 = Integer.toString(b.getTypeId());
        	            } else {
        	            	blocktype2 = b.getType().name();
        	            }
        				
        				int x2 = bs.getX();
        	            int y2 = bs.getY();
        	            int z2 = bs.getZ();
        	            
        				if (RPConfig.getStringList("private.allowed-blocks").contains(blocktype2)){
        					for (int ux = -1; ux <= 1; ux++){
            	        		for (int uy = -1; uy <= 1; uy++){
            	        			for (int uz = -1; uz <= 1; uz++){
            	        				Block bu = w.getBlockAt(x2+ux, y2+uy, z2+uz);
            	        				if (bu.getState() instanceof Sign && !validateBreakSign(bu, p) && getBlockRelative(bu).getType().equals(b.getType())){
            	        					return false;
            	                    	}
            	        			}
            	        		}
            	        	}
        				}        				 
        			}
        		}
        	}                   
        } 
        return true;
    }
    
	@SuppressWarnings("deprecation")
	public boolean canWorldBreak(Block b){		
    	if (!RPConfig.getBool("private.use")){
    		return true;
    	}
    	Region reg = RedProtect.rm.getTopRegion(b.getLocation());
    	if (reg == null && !RPConfig.getBool("private.allow-outside")){
    		return true;
    	}
    	int x = b.getX();
        int y = b.getY();
        int z = b.getZ();
        World w = b.getWorld();

        if (b.getState() instanceof Sign && validatePrivateSign(b)){
        	RedProtect.logger.debug("Valid Sign on canWorldBreak!");
			return false;
    	}   		
           		
        String signbtype;
        if (RPConfig.getBool("private.allowed-blocks-use-ids")){
        	signbtype = Integer.toString(b.getTypeId());
        } else {
        	signbtype = b.getType().name();
        } 
        
        if (RPConfig.getStringList("private.allowed-blocks").contains(signbtype)){        	
        	for (int sx = -1; sx <= 1; sx++){
        		for (int sz = -1; sz <= 1; sz++){
    				Block bs = w.getBlockAt(x+sx, y, z+sz);
    				if (bs.getState() instanceof Sign && validatePrivateSign(bs)){
    					return false;
                	}
    				
    				String blocktype2;
    	            if (RPConfig.getBool("private.allowed-blocks-use-ids")){
    	            	blocktype2 = Integer.toString(b.getTypeId());
    	            } else {
    	            	blocktype2 = b.getType().name();
    	            }
    				
    				int x2 = bs.getX();
    	            int y2 = bs.getY();
    	            int z2 = bs.getZ();
    	            
    				if (RPConfig.getStringList("private.allowed-blocks").contains(blocktype2)){
    					for (int ux = -1; ux <= 1; ux++){
    						for (int uz = -1; uz <= 1; uz++){
    	        				Block bu = w.getBlockAt(x2+ux, y2, z2+uz);
    	        				if (bu.getState() instanceof Sign && validatePrivateSign(bu)){
    	        					return false;
    	                    	}
    	        			}        	        		
        	        	}
    				}        				 
    			}	        		
        	}                   
        } 
        return true;
    }
	
	public static Block getBlockRelative(Block block) {
        if (block.getState() instanceof Sign){
        	Sign s = (Sign) block.getState();
        	org.bukkit.material.Sign data = (org.bukkit.material.Sign) s.getData();
        	return block.getRelative(data.getAttachedFace());
        }            
        return null;
    }
	
	private boolean validatePrivateSign(Block b){
		Sign s = (Sign) b.getState();
		if (s.getLine(0).equalsIgnoreCase("[private]") || s.getLine(0).equalsIgnoreCase("private") || s.getLine(0).equalsIgnoreCase(RPLang.get("blocklistener.container.signline")) || s.getLine(0).equalsIgnoreCase("["+RPLang.get("blocklistener.container.signline")+"]")){
		    return true;
		}
		return false;
	}
	
	private boolean validateBreakSign(Block b, Player p){
		Sign s = (Sign) b.getState();
		if ((s.getLine(0).equalsIgnoreCase("[private]") || s.getLine(0).equalsIgnoreCase("private") || s.getLine(0).equalsIgnoreCase(RPLang.get("blocklistener.container.signline")) || s.getLine(0).equalsIgnoreCase("["+RPLang.get("blocklistener.container.signline")+"]")) && 
			!s.getLine(1).equals(p.getName())){
		    return false;
		}
		return true;
	}
	
	private boolean validateOpenBlock(Block b, Player p){
		Sign s = (Sign) b.getState();
		if ((s.getLine(0).equalsIgnoreCase("[private]") || s.getLine(0).equalsIgnoreCase("private") || s.getLine(0).equalsIgnoreCase(RPLang.get("blocklistener.container.signline")) || s.getLine(0).equalsIgnoreCase("["+RPLang.get("blocklistener.container.signline")+"]")) && 
			(!s.getLine(1).equals(p.getName()) &&
			!s.getLine(2).equals(p.getName()) &&
			!s.getLine(3).equals(p.getName()))){
		    return false;
		}
		return true;
	}
	
    @SuppressWarnings("deprecation")
	public boolean isContainer(Block b){
    	Block container = null;
    	int face = b.getData() & 0x7;
	    if (face == 3) {
	    	container = b.getRelative(BlockFace.NORTH);
	    }
	    if (face == 4) {
	    	container = b.getRelative(BlockFace.EAST);
	    }
	    if (face == 2) {
	    	container = b.getRelative(BlockFace.SOUTH);
	    }
	    if (face == 5) {
	    	container = b.getRelative(BlockFace.WEST);
	    }    	    
	    
	    String signbtype;
        if (RPConfig.getBool("private.allowed-blocks-use-ids")){
        	signbtype = Integer.toString(container.getTypeId());
        } else {
        	signbtype = container.getType().name();
        } 
        
	    if (RPConfig.getStringList("private.allowed-blocks").contains(signbtype)){
	    	return true;
	    }
	    return false;
    }  
    
}
