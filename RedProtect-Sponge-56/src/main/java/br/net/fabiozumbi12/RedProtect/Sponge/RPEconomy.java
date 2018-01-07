package br.net.fabiozumbi12.RedProtect.Sponge;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.meta.ItemEnchantment;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.type.TileEntityInventory;
import org.spongepowered.api.world.World;

import br.net.fabiozumbi12.RedProtect.Sponge.config.RPLang;

public class RPEconomy {

	
	public static long getRegionValue(Region r){
		long regionCost = 0;
		World w = RedProtect.serv.getWorld(r.getWorld()).get();
		int maxX = r.getMaxMbrX();
		int minX = r.getMinMbrX();
		int maxZ = r.getMaxMbrZ();
		int minZ = r.getMinMbrZ();
		for(int x = minX; x < maxX; x++){
		  for(int y = 0; y < 256; y++) {
		    for(int z = minZ; z < maxZ; z++) {
		    	
		      BlockSnapshot b = w.createSnapshot(x, y, z);
		      /*
		      Location<World> loc = new Location<World>(w, x, y, z);		      
		      Collection<Entity> ents = w.getEntities(ent-> ent.getLocation().getBlockPosition().equals(loc.getBlockPosition()));
		      for (Entity ent:ents){
		    	  if (ent instanceof Equipable){
		    		  Equipable equip = (Equipable) ent;
		    		  regionCost += getInvValue(equip.getInventory().slots());
		    	  }
		      }
		      */      
		      if (b.getState().getType().equals(BlockTypes.AIR)){
		    	  continue;
		      }
		      
		      if (b.getLocation().get().getTileEntity().isPresent()){
		    	  TileEntity invTile = b.getLocation().get().getTileEntity().get();
		    	  if (invTile instanceof TileEntityInventory){
		    		  TileEntityInventory<?> inv = (TileEntityInventory<?>) invTile;			    	  
		    		  regionCost += getInvValue(inv.slots());
		    	  }		    	  
		      }	else {
		    	  regionCost += RedProtect.cfgs.getBlockCost(b.getState().getType().getName());
		      }		      
		    }
		  }
		}
		r.setValue(regionCost);
		return regionCost;		
	}

	private static long getInvValue(Iterable<Inventory> inv){
		return RedProtect.getPVHelper().getInvValue(inv);
	}
	
	public static String getCostMessage(Region r){
		return RPLang.get("economy.forsale") + " &6" + getFormatted(r.getValue()) +" &2"+ RedProtect.cfgs.getEcoString("economy-name");
	}
	
	public static String getFormatted(long value){
		return RedProtect.cfgs.getEcoString("economy-symbol") + value;
	}
	
	public static boolean putToSell(Region r, String uuid, long value) {
		try {
			r.clearMembers();
			r.clearAdmins();
			r.setValue(value);
			r.setWelcome(getCostMessage(r));			
			r.setFlag("for-sale", true);
			if (RedProtect.cfgs.getEcoBool("rename-region")){
				RedProtect.rm.renameRegion(RPUtil.nameGen(RPUtil.UUIDtoPlayer(uuid),r.getWorld()),r);
			}			
			return true;
		} catch (Exception e){
			return false;
		}		
	}
	
	public static boolean BuyRegion(Region r, String uuid) {
		try {			
			r.clearMembers();
			r.clearAdmins();
			r.clearLeaders();
			r.addLeader(uuid);
			r.setDate(RPUtil.DateNow());
			r.setWelcome("");
			r.flags = RedProtect.cfgs.getDefFlagsValues();
			if (RedProtect.cfgs.getEcoBool("rename-region")){
				RedProtect.rm.renameRegion(RPUtil.nameGen(RPUtil.UUIDtoPlayer(uuid),r.getWorld()),r);
			}	
			r.removeFlag("for-sale");
			return true;
		} catch (Exception e){
			return false;
		}		
	}
	
}
