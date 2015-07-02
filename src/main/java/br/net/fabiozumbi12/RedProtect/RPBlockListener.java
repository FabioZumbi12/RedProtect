package br.net.fabiozumbi12.RedProtect;

import java.util.List;

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
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
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
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onSignChange(SignChangeEvent e) {   	
    	if (e.isCancelled()){
    		return;
    	}
    	
        Block b = e.getBlock();
        Player p = e.getPlayer();
        
    	RedProtect.logger.debug("Is SignChangeEvent event!");
    	
        if (b == null) {
            this.setErrorSign(e, p, RPLang.get("blocklistener.block.null"));
            return;
        }
        
        Region signr = RedProtect.rm.getTopRegion(b.getLocation());
        if (signr != null && !signr.canSign(p)){
        	p.sendMessage(RPLang.get("playerlistener.region.cantinteract"));
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
        if ((RPConfig.getBool("private.use") && b.getType().equals(Material.WALL_SIGN)) && (line1.equalsIgnoreCase(RPLang.get("blocklistener.container.signline")) || line1.equalsIgnoreCase("["+RPLang.get("blocklistener.container.signline")+"]"))) {
        	Region r = RedProtect.rm.getTopRegion(b.getLocation());        
        	Boolean out = RPConfig.getBool("private.allow-outside");
        	if (out || r != null){
        		if (cont.isContainer(b)){
            		int length = p.getName().length();
                    if (length > 15) {
                      length = 15;
                    }
                	e.setLine(1, p.getName().substring(0, length));
                    p.sendMessage(RPLang.get("blocklistener.container.protected"));
                    return;
            	} else {
            		p.sendMessage(RPLang.get("blocklistener.container.notprotected"));
            		b.breakNaturally();
            		return;
            	}
        	} else {
        		p.sendMessage(RPLang.get("blocklistener.container.notregion"));
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
                p.sendMessage(RPLang.get("blocklistener.region.created").replace("{region}",  r.getName()));
                p.sendMessage(RPLang.get("general.color") + "------------------------------------");
                RedProtect.rm.add(r, RedProtect.serv.getWorld(r.getWorld()));
                return;
            }
        }
        return;
    }
    
    void setErrorSign(SignChangeEvent e, Player p, String error) {
        e.setLine(0, RPLang.get("regionbuilder.signerror"));
        p.sendMessage(RPLang.get("regionbuilder.signerror") + ": " + error);
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockPlace(BlockPlaceEvent e) {
    	if (e.isCancelled()) {
            return;
        }
    	
        Block b = e.getBlock();
        Player p = e.getPlayer();
        World w = p.getWorld();
        Material m = p.getItemInHand().getType();
        Boolean antih = RPConfig.getBool("region-settings.anti-hopper");
        Region r = RedProtect.rm.getTopRegion(b.getLocation());
        
    	if (r != null && !r.canMinecart(p) && p.getItemInHand().getType().name().contains("MINECART")){
        	p.sendMessage(RPLang.get("blocklistener.region.cantplace"));
            e.setCancelled(true);
        	return;
        }
    	
        try {

            if (r != null && !r.canBuild(p)) {
                p.sendMessage(RPLang.get("blocklistener.region.cantbuild"));
                e.setCancelled(true);
            } else {
            	if (!RedProtect.ph.hasPerm(p, "redprotect.bypass") && antih && 
            			(m.equals(Material.HOPPER) || m.name().contains("RAIL"))){
            		int x = b.getX();
            		int y = b.getY();
            		int z = b.getZ();
            		Block ib = w.getBlockAt(x, y+1, z);
            		if (!cont.canBreak(p, ib) || !cont.canBreak(p, b)){
            			p.sendMessage(RPLang.get("blocklistener.container.chestinside"));
            			e.setCancelled(true);
            			return;
            		} 
            	}
            }
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
    }    	
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBreak(BlockBreakEvent e) {
    	if (e.isCancelled()) {
            return;
        }
        Player p = e.getPlayer();
        Block b = e.getBlock();
        World w = p.getWorld();
        Boolean antih = RPConfig.getBool("region-settings.anti-hopper");
        Region r = RedProtect.rm.getTopRegion(b.getLocation());
        
        if (!RedProtect.ph.hasPerm(p, "redprotect.bypass")){
        	int x = b.getX();
    		int y = b.getY();
    		int z = b.getZ();
    		Block ib = w.getBlockAt(x, y+1, z);
    		if ((antih && !cont.canBreak(p, ib)) || !cont.canBreak(p, b)){
    			p.sendMessage(RPLang.get("blocklistener.container.breakinside"));
    			e.setCancelled(true);
    			return;
    		}
        }
             
        if (r != null && !r.canBuild(p) && !r.canTree(b) && !r.canMining(b)){
        	p.sendMessage(RPLang.get("blocklistener.region.cantbuild"));
            e.setCancelled(true);
        	return;
        }         
                
    }
    
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteract(PlayerInteractEvent e){
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
			p.sendMessage(RPLang.get("blocklistener.region.cantbreak"));
			e.setCancelled(true);
			return;
		}		
	}
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityExplode(EntityExplodeEvent e) {
    	if (e.isCancelled()){
    		return;
    	}
    	RedProtect.logger.debug("Is BlockListener - EntityExplodeEvent event");
        for (int i = 0; i < e.blockList().size(); i++) {
        	Location l = e.blockList().get(i).getLocation();
        	Region r = RedProtect.rm.getTopRegion(l);
        	if (r != null && !r.canFire()){
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
    	if ( b == null || bignit == null){
    		return;
    	}
    	RedProtect.logger.debug("Is BlockIgniteEvent event");
    	Region r = RedProtect.rm.getTopRegion(b.getLocation());
		if ((bignit.getType().equals(Material.FIRE) || bignit.getType().name().contains("LAVA")) && r != null && !r.canFire()){
			e.setCancelled(true);
    		return;
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
		if (e.isCancelled()){
    		return;
    	}
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
			p.sendMessage(RPLang.get("blocklistener.region.cantbreak"));
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
	public void onPistonRetract(BlockPistonRetractEvent e){
		if (RPConfig.getBool("performance.disable-PistonEvent-handler")){
			return;
		}
		Block piston = e.getBlock();
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
