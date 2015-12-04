package br.net.fabiozumbi12.RedProtect;

import java.util.HashSet;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.weather.LightningStrikeEvent;

class RPBlockListener implements Listener{
	
	static RPContainer cont = new RPContainer();
    RedProtect plugin;
    
    public RPBlockListener(RedProtect plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onSignChange(SignChangeEvent e) {   
    	RedProtect.logger.debug("BlockListener - Is SignChangeEvent event! Cancelled? " + e.isCancelled());
    	if (e.isCancelled()){
    		return;
    	}
    	
        Block b = e.getBlock();
        Player p = e.getPlayer();
            	
        if (b == null) {
            this.setErrorSign(e, p, RPLang.get("blocklistener.block.null"));
            return;
        }
        
        Region signr = RedProtect.rm.getTopRegion(b.getLocation());
                
        if (signr != null && !signr.canSign(p)){
        	RPLang.sendMessage(p, "playerlistener.region.cantinteract");
        	e.setCancelled(true);
        	return;
        }
        
        String[] lines = e.getLines();
        String line1 = lines[0];
        
        if (lines.length != 4) {
            this.setErrorSign(e, p, RPLang.get("blocklistener.sign.wronglines"));
            return;
        }
        
        if (!RedProtect.ph.hasPerm(p, "redprotect.create")) {
            this.setErrorSign(e, p, RPLang.get("blocklistener.region.nopem"));
            return;
        }
        

        if (RPConfig.getBool("server-protection.sign-spy.enabled")){
        	Bukkit.getConsoleSender().sendMessage("§cSign Spy §b- Location: §rx: " + b.getX() + ", y: " + b.getY() + ", z: " + b.getZ() + ", world: " + b.getWorld().getName());
        	Bukkit.getConsoleSender().sendMessage("§bPlayer: §e" + e.getPlayer().getName());
        	Bukkit.getConsoleSender().sendMessage("§bLines - §e1: §r" + lines[0].toString() + " - §e2: §r" + lines[1].toString());
        	Bukkit.getConsoleSender().sendMessage("§bLines - §e3: §r" + lines[2].toString() + " - §e4: §r" + lines[3].toString());
        	if (!RPConfig.getBool("server-protection.sign-spy.only-console")){
        		for (Player play:Bukkit.getOnlinePlayers()){
        			if (play.hasPermission("redprotect.signspy")/* && !play.equals(p)*/){
        				play.sendMessage("§cSign Spy §b- Location: §rx: " + b.getX() + ", y: " + b.getY() + ", z: " + b.getZ() + ", world: " + b.getWorld().getName());
        	        	play.sendMessage("§bPlayer: §e" + e.getPlayer().getName());
        	        	play.sendMessage("§bLines - §e1: §r" + lines[0].toString() + " - §e2: §r" + lines[1].toString());
        	        	play.sendMessage("§bLines - §e3: §r" + lines[2].toString() + " - §e4: §r" + lines[3].toString());
        			}
        		}
        	}
        }
        
        if ((RPConfig.getBool("private.use") && b.getType().equals(Material.WALL_SIGN)) && (line1.equalsIgnoreCase("private") || line1.equalsIgnoreCase("[private]") || line1.equalsIgnoreCase(RPLang.get("blocklistener.container.signline")) || line1.equalsIgnoreCase("["+RPLang.get("blocklistener.container.signline")+"]"))) {
        	Region r = RedProtect.rm.getTopRegion(b.getLocation());        
        	Boolean out = RPConfig.getBool("private.allow-outside");
        	if (out || r != null){
        		if (cont.isContainer(b)){
            		int length = p.getName().length();
                    if (length > 16) {
                      length = 16;
                    }
                	e.setLine(1, p.getName().substring(0, length));
                	RPLang.sendMessage(p, "blocklistener.container.protected");
                    return;
            	} else {
            		RPLang.sendMessage(p, "blocklistener.container.notprotected");
            		b.breakNaturally();
            		return;
            	}
        	} else {
        		RPLang.sendMessage(p, "blocklistener.container.notregion");
        		b.breakNaturally();
        		return;
        	}        	
        }
        
        if (line1.equalsIgnoreCase("[rp]")){
        	RegionBuilder rb = new EncompassRegionBuilder(e);
        	if (rb.ready()) {
                Region r = rb.build();
                e.setLine(0, RPLang.get("blocklistener.region.signcreated"));
                e.setLine(1, r.getName());
                //RPLang.sendMessage(p, RPLang.get("blocklistener.region.created").replace("{region}",  r.getName()));                
                RedProtect.rm.add(r, RedProtect.serv.getWorld(r.getWorld()));
                return;
            }
        }
        return;
    }
    
    void setErrorSign(SignChangeEvent e, Player p, String error) {
        e.setLine(0, RPLang.get("regionbuilder.signerror"));
        RPLang.sendMessage(p, RPLang.get("regionbuilder.signerror") + ": " + error);
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent e) {
    	RedProtect.logger.debug("BlockListener - Is BlockPlaceEvent event! Cancelled? " + e.isCancelled());
    	if (e.isCancelled()) {
            return;
        }
    	
    	Player p = e.getPlayer();
        Block b = e.getBlockPlaced();       
        World w = p.getWorld();
        Material m = p.getItemInHand().getType();
        Boolean antih = RPConfig.getBool("region-settings.anti-hopper");
        Region r = RedProtect.rm.getTopRegion(b.getLocation());
        
        if (r != null && RPConfig.getGlobalFlagList(p.getWorld().getName() + ".if-build-false.place-blocks").contains(b.getType().name())){
        	return;
        }
        
    	if (r != null && !r.canMinecart(p) && p.getItemInHand().getType().name().contains("MINECART")){
    		RPLang.sendMessage(p, "blocklistener.region.cantplace");
            e.setCancelled(true);
        	return;
        }
    	
        try {

            if (r != null && !r.canBuild(p) && !r.canPlace(b)) {
            	RPLang.sendMessage(p, "blocklistener.region.cantbuild");
                e.setCancelled(true);
            } else {
            	if (!RedProtect.ph.hasPerm(p, "redprotect.bypass") && antih && 
            			(m.equals(Material.HOPPER) || m.name().contains("RAIL"))){
            		int x = b.getX();
            		int y = b.getY();
            		int z = b.getZ();
            		Block ib = w.getBlockAt(x, y+1, z);
            		if (!cont.canBreak(p, ib) || !cont.canBreak(p, b)){
            			RPLang.sendMessage(p, "blocklistener.container.chestinside");
            			e.setCancelled(true);
            			return;
            		} 
            	}
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }    	
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent e) {
    	RedProtect.logger.debug("BlockListener - Is BlockBreakEvent event! Cancelled? " + e.isCancelled());
    	if (e.isCancelled()) {
            return;
        }
    	Player p = e.getPlayer();
    	Block b = e.getBlock();
    	if (RPUtil.pBorders.containsKey(p) && b.getType().equals(RPConfig.getMaterial("region-settings.border.material"))){
    		RPLang.sendMessage(p, "blocklistener.cantbreak.borderblock");
    		e.setCancelled(true);
    		return;
    	}                
        World w = p.getWorld();
        Boolean antih = RPConfig.getBool("region-settings.anti-hopper");
        Region r = RedProtect.rm.getTopRegion(b.getLocation());
        
        if (r != null && RPConfig.getGlobalFlagList(p.getWorld().getName() + ".if-build-false.break-blocks").contains(b.getType().name())){
        	return;
        }
        
        if (!RedProtect.ph.hasPerm(p, "redprotect.bypass")){
        	int x = b.getX();
    		int y = b.getY();
    		int z = b.getZ();
    		Block ib = w.getBlockAt(x, y+1, z);
    		if ((antih && !cont.canBreak(p, ib)) || !cont.canBreak(p, b)){
    			RPLang.sendMessage(p, "blocklistener.container.breakinside");
    			e.setCancelled(true);
    			return;
    		}
        }
             
        if (r != null && !r.canBuild(p) && !r.canTree(b) && !r.canMining(b) && !r.canBreak(b)){
        	RPLang.sendMessage(p, "blocklistener.region.cantbuild");
            e.setCancelled(true);
        	return;
        }         
                
    }
    
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteract(PlayerInteractEvent e){
		RedProtect.logger.debug("BlockListener - Is PlayerInteractEvent event! Cancelled? " + e.isCancelled());
    	if (e.isCancelled()) {
            return;
        }
    	
		Player p = e.getPlayer();
		Block b = p.getLocation().getBlock();
		Location l = e.getClickedBlock().getLocation();
		Region r = RedProtect.rm.getTopRegion(l);
		if ((b.getType().equals(Material.CROPS)
				|| b.getType().equals(Material.CARROT)
				|| b.getType().equals(Material.POTATO)
				|| b.getType().equals(Material.CARROT)
				 || b.getType().equals(Material.PUMPKIN_STEM)
				 || b.getType().equals(Material.MELON_STEM)) && r != null && !r.canBuild(p)){
			RPLang.sendMessage(p, "blocklistener.region.cantbreak");
			e.setCancelled(true);
			return;
		}		
		
		try {
			for (Block block:p.getLineOfSight((HashSet<Byte>)null, 8)){
				if (block == null){
					continue;
				}
				if (r != null && block.getType().equals(Material.FIRE) && !r.canBuild(p)){
					RPLang.sendMessage(p, "blocklistener.region.cantbreak");
					e.setCancelled(true);
					return;
				}
			}
		} catch (Exception ex){			
		}
		
	}
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityExplode(EntityExplodeEvent e) {
    	RedProtect.logger.debug("Is BlockListener - EntityExplodeEvent event");
        for (int i = 0; i < e.blockList().size(); i++) {
        	Location l = e.blockList().get(i).getLocation();
        	Region r = RedProtect.rm.getTopRegion(l);
        	if (!cont.canWorldBreak(e.blockList().get(i))){        		        		
        		e.setCancelled(true);
        		return;
        	}        	
        	if (r != null && !r.canMobLoot()){
        		e.setCancelled(true);
        		return;
        	}
        }
    }
    
    @EventHandler
    public void onFrameBrake(HangingBreakByEntityEvent e) {
    	if (e.isCancelled()){
    		return;
    	}
    	RedProtect.logger.debug("Is BlockListener - HangingBreakByEntityEvent event");
    	Entity remover = e.getRemover();
    	Entity ent = e.getEntity();
    	Location l = e.getEntity().getLocation();
    	    	
    	if ((ent instanceof ItemFrame || ent instanceof Painting) && remover instanceof Monster) {
    		Region r = RedProtect.rm.getTopRegion(l);
    		if (r != null && !r.canFire()){
    			e.setCancelled(true);
        		return;
    		}
        }    
    }
    
    @EventHandler
    public void onFrameBrake(HangingBreakEvent e) {
    	if (e.isCancelled()){
    		return;
    	}
    	RedProtect.logger.debug("Is BlockListener - HangingBreakEvent event");
    	Entity ent = e.getEntity();
    	Location l = e.getEntity().getLocation();		
    	
    	if ((ent instanceof ItemFrame || ent instanceof Painting) && (e.getCause().toString().equals("EXPLOSION"))) {
    		Region r = RedProtect.rm.getTopRegion(l);
    		if (r != null && !r.canFire()){
    			e.setCancelled(true);
        		return;
    		}
        }    
    }
        
    @EventHandler
    public void onBlockStartBurn(BlockIgniteEvent e){
    	if (e.isCancelled()){
    		return;
    	}
    	
    	Block b = e.getBlock();
    	Block bignit = e.getIgnitingBlock(); 
    	if (b == null){
    		return;
    	}
    	
    	RedProtect.logger.debug("Is BlockIgniteEvent event. Canceled? " + e.isCancelled());
    	
    	Region r = RedProtect.rm.getTopRegion(b.getLocation());
		if (r != null && !r.canFire()){
			if (e.getIgnitingEntity() != null){
				if (e.getIgnitingEntity() instanceof Player){
					Player p = (Player) e.getIgnitingEntity();
					if (!r.canBuild(p)){
						RPLang.sendMessage(p, "blocklistener.region.cantplace");
						e.setCancelled(true);
						return;
					}
				} else {
					e.setCancelled(true);
		    		return;
				}
			}
			
			if (bignit != null && (bignit.getType().equals(Material.FIRE) || bignit.getType().name().contains("LAVA"))){
				e.setCancelled(true);
	    		return;
			} 
			if (e.getCause().equals(IgniteCause.LIGHTNING) || e.getCause().equals(IgniteCause.EXPLOSION) || e.getCause().equals(IgniteCause.FIREBALL)){
				e.setCancelled(true);
	    		return;
			}			
		}
    	return;
    }
    
    @EventHandler
    public void onBlockBurn(BlockBurnEvent e){
    	if (e.isCancelled()){
    		return;
    	}
    	RedProtect.logger.debug("Is BlockBurnEvent event");
    	Block b = e.getBlock();

    	Region r = RedProtect.rm.getTopRegion(b.getLocation());
    	if (!cont.canWorldBreak(b)){
    		e.setCancelled(true);
    		return;
    	}    	
		if (r != null && !r.canFire()){
			e.setCancelled(true);
    		return;
		}
    	return;
    }
    
	@EventHandler
    public void onFlow(BlockFromToEvent e){
		if (e.isCancelled()){
    		return;
    	}		
    	Block b = e.getToBlock();
    	Block bfrom = e.getBlock();
		RedProtect.logger.debug("Is BlockFromToEvent event is to " + b.getType().name() + " from " + bfrom.getType().name());
    	Region r = RedProtect.rm.getTopRegion(b.getLocation());
    	if (r != null && bfrom.isLiquid() && !r.canFlow()){
          	 e.setCancelled(true);           	  
    	}
    }
	    
	@EventHandler
	public void onLightning(LightningStrikeEvent e){
		RedProtect.logger.debug("Is LightningStrikeEvent event");
		Location l = e.getLightning().getLocation();
		Region r = RedProtect.rm.getTopRegion(l);
		if (r != null && !r.canFire()){
			e.setCancelled(true);
			return;
		}
	}
	
	@EventHandler
    public void onFireSpread(BlockSpreadEvent  e){
		if (e.isCancelled()){
    		return;
    	}
		Block b = e.getSource();
		RedProtect.logger.debug("Is BlockSpreadEvent event, source is " + b.getType().name());
		Region r = RedProtect.rm.getTopRegion(b.getLocation());
		if ((b.getType().equals(Material.FIRE) || b.getType().name().contains("LAVA")) && r != null && !r.canFire()){
			e.setCancelled(true);
			return;
		}
	}
	
	@EventHandler
	public void onVehicleBreak(VehicleDestroyEvent e){
		if (e.isCancelled()){
    		return;
    	}
		if (!(e.getAttacker() instanceof Player)){
			return;
		}
		Vehicle cart = e.getVehicle();
		Player p = (Player) e.getAttacker();
		Region r = RedProtect.rm.getTopRegion(cart.getLocation());
		if (r == null){
			return;
		}
		
		if (!r.canMinecart(p)){
			RPLang.sendMessage(p, "blocklistener.region.cantbreak");
			e.setCancelled(true);
			return;
		}
	}
	
	@EventHandler
	public void onPistonExtend(BlockPistonExtendEvent e){
		if (RPConfig.getBool("performance.disable-PistonEvent-handler")){
			return;
		}
		Block piston = e.getBlock();
		List<Block> blocks = e.getBlocks();
		Region pr = RedProtect.rm.getTopRegion(piston.getLocation());
		for (Block b:blocks){
			Region br = RedProtect.rm.getTopRegion(b.getRelative(e.getDirection()).getLocation());
			if (pr == null && br != null || (pr != null && br != null && pr != br)){
				e.setCancelled(true);
			}
		}	
	}
	
	@EventHandler
	public void onBlockExplode(BlockExplodeEvent e){
		List<Block> blocks = e.blockList();
		for (Block b:blocks){
			Region r = RedProtect.rm.getTopRegion(b.getLocation());
			if (!cont.canWorldBreak(b)){
				e.blockList().remove(b);
	    	}
			if (r != null && !r.canFire()){
				e.blockList().remove(b);				
			}
		}		
	}
	
	@EventHandler
	public void onPistonRetract(BlockPistonRetractEvent e){
		if (RPConfig.getBool("performance.disable-PistonEvent-handler")){
			return;
		}
		Block piston = e.getBlock();
		if (Bukkit.getVersion().contains("1.7")){
			Block block = e.getBlock();
			Region pr = RedProtect.rm.getTopRegion(piston.getLocation());
			Region br = RedProtect.rm.getTopRegion(block.getLocation());
			if (pr == null && br != null || (pr != null && br != null && pr != br)){
				e.setCancelled(true);				
			}
		} else {
			List<Block> blocks = e.getBlocks();
			Region pr = RedProtect.rm.getTopRegion(piston.getLocation());
			for (Block b:blocks){
				Region br = RedProtect.rm.getTopRegion(b.getLocation());
				if (pr == null && br != null || (pr != null && br != null && pr != br)){
					e.setCancelled(true);				
				}
			}
		}
	}
	
	@EventHandler
	public void onLeafDecay(LeavesDecayEvent e){
		Region r = RedProtect.rm.getTopRegion(e.getBlock().getLocation());		
		if (r != null && !r.canFlow()){
         	 e.setCancelled(true);           	  
		}		
	}	
	/*
	@EventHandler
	public void onHopperPickup(InventoryMoveItemEvent e) {
		InventoryHolder frominv = e.getSource().getHolder();
		InventoryHolder toinv = e.getDestination().getHolder();
		
		if (toinv instanceof Hopper){
			Location loc = null;
			
			if (frominv instanceof BlockState) {
	            BlockState blockState = (BlockState) frominv;	 
	            loc = blockState.getLocation();
	        } else if (frominv instanceof DoubleChest) {
	            DoubleChest chest = (DoubleChest) frominv;
	            loc = chest.getLocation();
	        }
	 
	        if (loc != null) {	        	
	            if (!cont.canWorldBreak(loc.getBlock())){
					RedProtect.logger.debug("Cancelled transfer itens by Hopper");
					e.setCancelled(true);
				}
	        }
		}
	}*/
}
