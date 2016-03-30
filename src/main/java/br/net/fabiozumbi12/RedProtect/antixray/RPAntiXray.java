package br.net.fabiozumbi12.RedProtect.antixray;

import org.bukkit.event.Listener;

public class RPAntiXray implements Listener{
	/*
	static PacketContainer blockChange = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.BLOCK_CHANGE);
	
	static HashMap<Location,Material> XrayBlocks = new HashMap<Location,Material>();
	static Material matFrom = Material.CHEST;
	static Material matTo = Material.STONE;
	
	@EventHandler
	public void onChunkLoad(ChunkLoadEvent e){
		World w = e.getChunk().getWorld();		
		int bx = e.getChunk().getX()<<4;
		int bz = e.getChunk().getZ()<<4;
				
		for(int xx = bx; xx < bx+16; xx++) {
		    for(int zz = bz; zz < bz+16; zz++) {
		        for(int yy = 0; yy < w.getMaxHeight(); yy++) {	
		        	Location loc = new Location(w,xx,yy,zz);
		            Block block = w.getBlockAt(xx,yy,zz);
		            if(block.getType().equals(matFrom)) {
		            	XrayBlocks.put(loc, matTo);
		            }
		        }
		    }
		}		
	}
	
    @EventHandler
	public void onChunkUnload(ChunkUnloadEvent e){
    	World w = e.getChunk().getWorld();		
		int bx = e.getChunk().getX()<<4;
		int bz = e.getChunk().getZ()<<4;
		
		for(int xx = bx; xx < bx+16; xx++) {
		    for(int zz = bz; zz < bz+16; zz++) {
		        for(int yy = 0; yy < w.getMaxHeight(); yy++) {		        	
		        	Location loc = new Location(w,xx,yy,zz);
		            if(XrayBlocks.containsKey(loc)) {
		            	XrayBlocks.remove(loc);
		            }
		        }
		    }
		}		
	}
    
	public static void sendOtherBlock(Player p){	
		int id = 0;
		for (Location loc:XrayBlocks.keySet()){
			try {
				BlockPosition blockpos = new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
				if (!blockChange.getBlockPositionModifier().getValues().contains(blockpos)){
					blockChange.getBlockPositionModifier().write(id,blockpos);
										
					Double dist = p.getLocation().distance(loc);
					WrappedBlockData wFrom = WrappedBlockData.createData(XrayBlocks.get(loc));
					WrappedBlockData wTo = WrappedBlockData.createData(loc.getBlock().getType());
					
		    		if (dist > 10){
		    			blockChange.getBlockData().write(0, wFrom);
		    		} else if (blockChange.getBlockData().getValues().contains(wFrom)){
		    			blockChange.getBlockData().write(0, wTo);
		    		}
	    		
					ProtocolLibrary.getProtocolManager().sendServerPacket(p, blockChange);
					id++;
				}				
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	} 				
	}*/
}
