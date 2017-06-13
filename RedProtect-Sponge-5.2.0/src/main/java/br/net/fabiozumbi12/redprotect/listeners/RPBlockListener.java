package br.net.fabiozumbi12.redprotect.listeners;

import java.util.List;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.block.tileentity.TileEntityTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.hanging.Hanging;
import org.spongepowered.api.entity.living.monster.Monster;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.explosive.fireball.Fireball;
import org.spongepowered.api.entity.vehicle.Boat;
import org.spongepowered.api.entity.weather.Lightning;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.action.LightningEvent;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.IgniteEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.explosion.Explosion;

import br.net.fabiozumbi12.redprotect.EncompassRegionBuilder;
import br.net.fabiozumbi12.redprotect.RPContainer;
import br.net.fabiozumbi12.redprotect.RPUtil;
import br.net.fabiozumbi12.redprotect.RedProtect;
import br.net.fabiozumbi12.redprotect.Region;
import br.net.fabiozumbi12.redprotect.RegionBuilder;
import br.net.fabiozumbi12.redprotect.config.RPLang;

public class RPBlockListener{
	
	private static RPContainer cont = new RPContainer();
	
	public RPBlockListener(){
		RedProtect.logger.debug("blocks","Loaded RPBlockListener...");
	}    
    
	@Listener
    public void onSignPlace(ChangeSignEvent e, @First Player p) {   
    	RedProtect.logger.debug("blocks","BlockListener - Is SignChangeEvent event! Cancelled? " + e.isCancelled());
    	
    	Sign s = e.getTargetTile();
    	List<Text> lines = e.getText().asList();
        Location<World> loc = s.getLocation();
        World w = p.getWorld();
        BlockSnapshot b = w.createSnapshot(loc.getBlockPosition());
            	        
        if (b == null) {
            this.setErrorSign(e, p, RPLang.get("blocklistener.block.null"));
            return;
        }
        
        Region signr = RedProtect.rm.getTopRegion(loc);
                
        if (signr != null && !signr.canSign(p)){
        	RPLang.sendMessage(p, "playerlistener.region.cantinteract");
        	e.setCancelled(true);
        	return;
        }
        
        Text line1 = lines.get(0);
        
        if (lines.size() != 4) {
            this.setErrorSign(e, p, RPLang.get("blocklistener.sign.wronglines"));
            return;
        }
        
        if (RedProtect.cfgs.getBool("server-protection.sign-spy.enabled")){
        	Sponge.getServer().getConsole().sendMessage(RPUtil.toText(RPLang.get("blocklistener.signspy.location").replace("{x}", ""+loc.getX()).replace("{y}", ""+loc.getY()).replace("{z}", ""+loc.getZ()).replace("{world}", w.getName())));
        	Sponge.getServer().getConsole().sendMessage(RPUtil.toText(RPLang.get("blocklistener.signspy.player").replace("{player}", p.getName())));
        	Sponge.getServer().getConsole().sendMessage(RPUtil.toText(RPLang.get("blocklistener.signspy.lines12").replace("{line1}", lines.get(0).toPlain()).replace("{line2}", lines.get(1).toPlain())));
        	Sponge.getServer().getConsole().sendMessage(RPUtil.toText(RPLang.get("blocklistener.signspy.lines34").replace("{line3}", lines.get(2).toPlain()).replace("{line4}", lines.get(3).toPlain())));
        	if (!RedProtect.cfgs.getBool("server-protection.sign-spy.only-console")){
        		for (Player play:Sponge.getServer().getOnlinePlayers()){
        			if (RedProtect.ph.hasGenPerm(play, "redprotect.signspy")/* && !play.equals(p)*/){
        				play.sendMessage(RPUtil.toText(RPLang.get("blocklistener.signspy.location").replace("{x}", ""+loc.getX()).replace("{y}", ""+loc.getY()).replace("{z}", ""+loc.getZ()).replace("{world}", w.getName())));
        	        	play.sendMessage(RPUtil.toText(RPLang.get("blocklistener.signspy.player").replace("{player}", p.getName())));
        	        	play.sendMessage(RPUtil.toText(RPLang.get("blocklistener.signspy.lines12").replace("{line1}", lines.get(0).toPlain()).replace("{line2}", lines.get(1).toPlain())));
        	        	play.sendMessage(RPUtil.toText(RPLang.get("blocklistener.signspy.lines34").replace("{line3}", lines.get(2).toPlain()).replace("{line4}", lines.get(3).toPlain())));
        			}
        		}
        	}
        }
        
        if ((RedProtect.cfgs.getBool("private.use") && s.getType().equals(TileEntityTypes.SIGN)) && (line1.toPlain().equalsIgnoreCase("private") || line1.toPlain().equalsIgnoreCase("[private]") || line1.toPlain().equalsIgnoreCase(RPLang.get("blocklistener.container.signline")) || line1.toPlain().equalsIgnoreCase("["+RPLang.get("blocklistener.container.signline")+"]"))) {
        	Region r = RedProtect.rm.getTopRegion(loc);        
        	Boolean out = RedProtect.cfgs.getBool("private.allow-outside");
        	if (out || r != null){
        		if (cont.isContainer(b)){
            		int length = p.getName().length();
                    if (length > 16) {
                      length = 16;
                    }
                    lines.set(1, RPUtil.toText(p.getName().substring(0, length)));
                    e.getText().setElements(lines);
                	RPLang.sendMessage(p, "blocklistener.container.protected");
                    return;
            	} else {
            		RPLang.sendMessage(p, "blocklistener.container.notprotected");
            		//w.digBlock(loc.getBlockPosition(), Cause.of(NamedCause.simulated(p)));
            		return;
            	}
        	} else {
        		RPLang.sendMessage(p, "blocklistener.container.notregion");
        		//w.digBlock(loc.getBlockPosition(), Cause.of(NamedCause.simulated(p)));
        		return;
        	}        	
        }
                
        if (line1.toPlain().equalsIgnoreCase("[rp]")){
        	String claimmode = RedProtect.cfgs.getWorldClaimType(p.getWorld().getName());
            if ((!claimmode.equalsIgnoreCase("BLOCK") && !claimmode.equalsIgnoreCase("BOTH")) && !RedProtect.ph.hasPerm(p, "redprotect.admin.create")) {
                this.setErrorSign(e, p, RPLang.get("blocklistener.region.claimmode"));
                return;
            }
                    	
        	RegionBuilder rb = new EncompassRegionBuilder(e);
        	if (rb.ready()) {
                Region r = rb.build();
                lines.set(0, RPUtil.toText(RPLang.get("blocklistener.region.signcreated")));
                lines.set(1, RPUtil.toText(r.getName()));
                e.getText().setElements(lines);
                //RPLang.sendMessage(p, RPLang.get("blocklistener.region.created").replace("{region}",  r.getName()));                
                RedProtect.rm.add(r, RedProtect.serv.getWorld(r.getWorld()).get());
                return;
            }
        }
        return;
    }
    
    void setErrorSign(ChangeSignEvent e, Player p, String error) {
        List<Text> lines = e.getTargetTile().get(Keys.SIGN_LINES).get();
        lines.set(0, RPUtil.toText(RPLang.get("regionbuilder.signerror")));
        e.getTargetTile().offer(Keys.SIGN_LINES, lines);
        RPLang.sendMessage(p, RPLang.get("regionbuilder.signerror") + ": " + error);
    }
    
    @Listener
    public void onBlockPlaceGeneric(ChangeBlockEvent.Place e) {
    	if (e.getCause().root().toString().contains("minecraft:fire")){
    		Region r = RedProtect.rm.getTopRegion(e.getTransactions().get(0).getOriginal().getLocation().get());
    		if (r != null && !r.canFire()){    	
    			e.setCancelled(true);
    			RedProtect.logger.debug("blocks", "Tryed to PLACE FIRE!");
    		}
    	}    	
    }
    
    @Listener
    public void onBlockGrow(ChangeBlockEvent.Grow e) {
    	RedProtect.logger.debug("blocks","RPBlockListener - Is ChangeBlockEvent.Grow event");	
		
    	BlockSnapshot b = e.getTransactions().get(0).getOriginal();
		Region r = RedProtect.rm.getTopRegion(b.getLocation().get());
		if (r != null && !r.canGrow()){
			e.setCancelled(true);
			RedProtect.logger.debug("blocks", "Cancel grow "+b.getState().getName());
		}
    }
    
    @Listener
    public void onBlockBreakGeneric(ChangeBlockEvent.Break e) {
    	if (e.getCause().root().toString().contains("minecraft:fire")){
    		BlockSnapshot b = e.getTransactions().get(0).getOriginal();
    		Region r = RedProtect.rm.getTopRegion(b.getLocation().get());    		
    		if (r != null && !r.canFire() && !b.getState().getType().equals(BlockTypes.FIRE)){    	
    			e.setCancelled(true);
    			RedProtect.logger.debug("blocks", "Tryed to break from FIRE!");
    		}
    	}   	
    }
    
    @Listener
    public void onBlockPlace(ChangeBlockEvent.Place e, @First Player p) {
    	RedProtect.logger.debug("blocks","BlockListener - Is BlockPlaceEvent event! Cancelled? " + e.isCancelled());
    	World w = e.getTargetWorld();
    	
    	BlockSnapshot b = e.getTransactions().get(0).getOriginal();
    	Location<World> bloc = b.getLocation().get();
    	
        ItemType m = ItemTypes.NONE;
        if (p.getItemInHand(HandTypes.MAIN_HAND).isPresent()){
        	m = p.getItemInHand(HandTypes.MAIN_HAND).get().getItem();
        } else if (p.getItemInHand(HandTypes.OFF_HAND).isPresent()){
        	m = p.getItemInHand(HandTypes.OFF_HAND).get().getItem();
        }
        Boolean antih = RedProtect.cfgs.getBool("region-settings.anti-hopper");
        Region r = RedProtect.rm.getTopRegion(b.getLocation().get());
        
        if (r == null && RedProtect.cfgs.getGlobalFlagList(w.getName(),"if-build-false","place-blocks").contains(b.getState().getType().getName())){
        	return;
        }
        
        if (r != null){
        	
        	if (!r.canMinecart(p) && m.getName().contains("minecart")){
        		RPLang.sendMessage(p, "blocklistener.region.cantplace");
                e.setCancelled(true);
            	return;
            }
        	
        	if (b.getState().getType().equals(BlockTypes.MOB_SPAWNER) && r.allowSpawner(p)){
        		return;
        	}
        	
        	try {
                if (!r.canBuild(p) && !r.canPlace(b)) {
                	RPLang.sendMessage(p, "blocklistener.region.cantbuild");
                    e.setCancelled(true);
                } else {
                	if (!RedProtect.ph.hasPerm(p, "redprotect.bypass") && antih && 
                			(m.equals(ItemTypes.HOPPER) || m.getName().contains("rail"))){
                		int x = bloc.getBlockX();
                		int y = bloc.getBlockY();
                		int z = bloc.getBlockZ();
                		BlockSnapshot ib = w.createSnapshot(x, y+1, z);
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
    }    	
    
    @Listener
    public void onBlockBreak(ChangeBlockEvent.Break e, @First Player p) {
    	RedProtect.logger.debug("blocks","BlockListener - Is ChangeBlockEvent.Break event!");
    	World w = e.getTargetWorld();
    	BlockSnapshot b = e.getTransactions().get(0).getOriginal();
    	Location<World> bloc = b.getLocation().get();
        
    	if (RPUtil.pBorders.containsKey(p) && b.getState().getType().equals(RedProtect.cfgs.getMaterial("region-settings.border.material"))){
    		RPLang.sendMessage(p, "blocklistener.cantbreak.borderblock");
    		e.setCancelled(true);
    		return;
    	}                
    	
        Boolean antih = RedProtect.cfgs.getBool("region-settings.anti-hopper");
        Region r = RedProtect.rm.getTopRegion(bloc);
        
        if (!RedProtect.ph.hasPerm(p, "redprotect.bypass")){
        	int x = bloc.getBlockX();
    		int y = bloc.getBlockY();
    		int z = bloc.getBlockZ();
    		BlockSnapshot ib = w.createSnapshot(x, y+1, z);
    		if ((antih && !cont.canBreak(p, ib)) || !cont.canBreak(p, b)){
    			RPLang.sendMessage(p, "blocklistener.container.breakinside");
    			e.setCancelled(true);
    			return;
    		}
        }
        
        if (r == null && RedProtect.cfgs.getGlobalFlagList(p.getWorld().getName(),"if-build-false","break-blocks").contains(b.getState().getType().getName())){
        	return;
        }
        
        if (r != null && b.getState().getType().equals(BlockTypes.MOB_SPAWNER) && r.allowSpawner(p)){
    		return;
    	}
        
        if (r != null && !r.canBuild(p) && !r.canTree(b) && !r.canMining(b) && !r.canCrops(b) && !r.canBreak(b)){
        	RPLang.sendMessage(p, "blocklistener.region.cantbuild");
            e.setCancelled(true);
        	return;
        }       
    }
        /*
    @Listener
    public void onEntityExplode(ExplosionEvent.Detonate e) {
    	RedProtect.logger.debug("Is BlockListener - ExplosionEvent.Detonate event");
    	
        for (Transaction<BlockSnapshot> bl:e.getTransactions()) {
        	BlockSnapshot b = bl.getOriginal();
        	RedProtect.logger.debug("Blocks: "+b.getState().getType().getName());
        	
        	Location<World> l = b.getLocation().get();
        	Region r = RedProtect.rm.getTopRegion(l);
        	if (!cont.canWorldBreak(b)){
        		RedProtect.logger.debug("canWorldBreak Called!");
        		bl.setValid(false);
        		//toRemove.add(bl);
        		continue;
        	}        	
        	if (r == null){
        		continue;
        	}
        	
        	if ((b.getState().getType().getName().contains("tnt") || e.getCause().first(Lightning.class).isPresent()) && !r.canFire()){
        		//toRemove.add(bl);
        		bl.setValid(false);
    			continue;
        	}  
        	
        	if (e.getCause().first(Living.class).isPresent() && !r.canMobLoot()){
        		//toRemove.add(bl);
        		bl.setValid(false);
        		continue;
        	}
        	
        }
        /*if (!toRemove.isEmpty()){
        	e.getTransactions().removeAll(toRemove);
        }
        
    }*/
    
    @Listener
    public void onFrameAndBoatBrake(DamageEntityEvent e) {
    	
    	RedProtect.logger.debug("blocks","Is BlockListener - DamageEntityEvent event");
    	Entity ent = e.getTargetEntity();
    	Location<World> l = e.getTargetEntity().getLocation();
    	    
    	Region r = RedProtect.rm.getTopRegion(l);
    	
    	if (ent instanceof Hanging && e.getCause().first(Monster.class).isPresent()) {    		
    		if (r != null && !r.canFire()){
    			e.setCancelled(true);
        		return;
    		}
        }   
    	
    	if (ent instanceof Boat && e.getCause().first(Player.class).isPresent()){
    		Player p = e.getCause().first(Player.class).get();
    		if (!r.canMinecart(p)){
    			RPLang.sendMessage(p, "blocklistener.region.cantbreak");
    			e.setCancelled(true);
    			return;
    		}
    	}
    }
    /*
    @Listener
    public void onFrameBrake(HangingBreakEvent e) {
    	if (e.isCancelled()){
    		return;
    	}
    	RedProtect.logger.debug("Is BlockListener - HangingBreakEvent event");
    	Entity ent = e.getEntity();
    	Location l = e.getEntity().getLocation();		
    	
    	if ((ent instanceof ItemFrame || ent instanceof Painting) && (e.getCause().toPlain().equals("EXPLOSION"))) {
    		Region r = RedProtect.rm.getTopRegion(l);
    		if (r != null && !r.canFire()){
    			e.setCancelled(true);
        		return;
    		}
        }    
    }
    */
    @Listener
    public void onBlockStartBurn(IgniteEntityEvent e){
    	
    	Entity b = e.getTargetEntity();
    	Cause ignit = e.getCause(); 
    	if (b == null){
    		return;
    	}
    	
    	RedProtect.logger.debug("blocks","Is BlockIgniteEvent event. Canceled? " + e.isCancelled());
    	
    	Region r = RedProtect.rm.getTopRegion(b.getLocation());
		if (r != null && !r.canFire()){
			if (ignit.first(Player.class).isPresent()){
				Player p = ignit.first(Player.class).get();
				if (!r.canBuild(p)){
					RPLang.sendMessage(p, "blocklistener.region.cantplace");
					e.setCancelled(true);
					return;
				}
			} else {
				e.setCancelled(true);
	    		return;
			}
			
			if (ignit.first(BlockSnapshot.class).isPresent() && (ignit.first(BlockSnapshot.class).get().getState().getType().equals(BlockTypes.FIRE) || ignit.first(BlockSnapshot.class).get().getState().getType().getName().contains("lava"))){
				e.setCancelled(true);
	    		return;
			} 
			if (ignit.first(Lightning.class).isPresent() || ignit.first(Explosion.class).isPresent() || ignit.first(Fireball.class).isPresent()){
				e.setCancelled(true);
	    		return;
			}			
		}
    	return;
    }
    
    /*
    @Listener
    public void onBlockBurn(NotifyNeighborBlockEvent e, @First BlockSnapshot source){
    	
    	RedProtect.logger.debug("Is ChangeBlockEvent.Modify event");
    	Map<Direction, BlockState> dirs = e.getNeighbors();
    	    	
    	for (Direction dir:dirs.keySet()){
    		BlockSnapshot b = source.getLocation().get().getRelative(dir).createSnapshot();
    		BlockState bstate = source.getState();
        	Region r = RedProtect.rm.getTopRegion(b.getLocation().get());        	
        	
        	if ((bstate.getType().equals(BlockTypes.FIRE) || bstate.getType().getName().contains("LAVA")) && r != null && !r.canFire()){
    			e.setCancelled(true);
    			return;
    		}
        	
        	if (!cont.canWorldBreak(b)){
        		e.setCancelled(true);
        		return;
        	}
    	}
    	return;
    }
    */
	@Listener
    public void onFlow(ChangeBlockEvent.Place e, @First BlockSnapshot source){		
		BlockSnapshot bfrom = e.getTransactions().get(0).getOriginal();
		
		RedProtect.logger.debug("blocks","Is BlockFromToEvent.Place event is to " + source.getState().getType().getName() + " from " + bfrom.getState().getType().getName());
		Region r = RedProtect.rm.getTopRegion(bfrom.getLocation().get());
    	if (r != null && (
    			source.getState().getType().equals(BlockTypes.WATER) ||
    			source.getState().getType().equals(BlockTypes.LAVA) ||
    			source.getState().getType().equals(BlockTypes.FLOWING_LAVA) ||
    			source.getState().getType().equals(BlockTypes.FLOWING_WATER)
    			)){
    		if (!r.canFlow()){
    			e.setCancelled(true);  
	          	return;
    		}	          	 
          	 
    		String bfType = bfrom.getState().getType().getName();
    		
          	if (!r.FlowDamage() && !bfrom.getState().getType().equals(BlockTypes.AIR) && !bfType.contains("water") && !bfType.contains("lava")){
	    		e.setCancelled(true);       
	         	return;
	   	    }
    	}    	
    }
	
	@Listener
    public void onDecay(ChangeBlockEvent.Decay e, @First BlockSnapshot source){		
		BlockSnapshot bfrom = e.getTransactions().get(0).getOriginal();
		RedProtect.logger.debug("blocks","Is BlockFromToEvent.Decay event is to " + source.getState().getType().getName() + " from " + bfrom.getState().getType().getName());
		Region r = RedProtect.rm.getTopRegion(bfrom.getLocation().get());
    	if (r != null && !r.leavesDecay() && source.getState().getType().getName().contains("leaves")){
          	 e.setCancelled(true);  
          	 return;
    	}    	
    }
	    
	@Listener
	public void onLightning(LightningEvent.Pre e, @First Lightning light){
		RedProtect.logger.debug("blocks","Is LightningStrikeEvent event");
		Location<World> l = light.getLocation();
		Region r = RedProtect.rm.getTopRegion(l);
		if (r != null && !r.canFire()){
			e.setCancelled(true);
			return;
		}
	}
	
    @Listener
    public void onInteractBlock(InteractBlockEvent event, @First Player p) {
    	BlockSnapshot b = event.getTargetBlock();
        Location<World> l = null;
        
        RedProtect.logger.debug("player","RPBlockListener - Is InteractBlockEvent event");
        
        if (!b.getState().getType().equals(BlockTypes.AIR)){
        	l = b.getLocation().get();
        	RedProtect.logger.debug("player","RPBlockListener - Is InteractBlockEvent event. The block is " + b.getState().getType().getName());
        } else {
        	l = p.getLocation();
        }
        
        Region r = RedProtect.rm.getTopRegion(l);
        if (r != null){
        	ItemType itemInHand = ItemTypes.NONE;                
            if (p.getItemInHand(HandTypes.MAIN_HAND).isPresent()){
            	itemInHand = p.getItemInHand(HandTypes.MAIN_HAND).get().getItem();
            } else if (p.getItemInHand(HandTypes.OFF_HAND).isPresent()){
            	itemInHand = p.getItemInHand(HandTypes.OFF_HAND).get().getItem();
            }
            if (itemInHand.equals(ItemTypes.ARMOR_STAND) && !r.canBuild(p)){
    			RPLang.sendMessage(p, "blocklistener.region.cantbuild");
                event.setCancelled(true); 
    		}
        }        
    }
    
    @Listener
    public void onInteractPrimBlock(InteractBlockEvent.Primary event, @First Player p) {
    	BlockSnapshot b = event.getTargetBlock();
        
        RedProtect.logger.debug("player","RPBlockListener - Is InteractBlockEvent.Primary event");
        
    	if (!RedProtect.ph.hasPerm(p, "redprotect.bypass")){        	
    		if (b.getState().getType().getName().contains("sign") && !cont.canBreak(p, b)){
    			RPLang.sendMessage(p, "blocklistener.container.breakinside");
    			event.setCancelled(true);
    			return;
    		}
        }
    }
    
    //TODO Test events
    /*
    @Listener
    public void onListenEvent(TargetLivingEvent event) {    	
    	RedProtect.logger.severe("Event: "+ event.toString());
    }
    
    */
    
	/*
	@Listener
	public void onVehicleBreak(DamageEntityEvent e){
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
	*/
	
	/*
	@Listener
	public void onPistonExtend(BlockPistonExtendEvent e){
		if (RedProtect.cfgs.getBool("performance.disable-PistonEvent-handler")){
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
		
	@Listener
	public void onPistonRetract(BlockPistonRetractEvent e){
		if (RedProtect.cfgs.getBool("performance.disable-PistonEvent-handler")){
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
	*/
	/*
	@Listener
	public void onLeafDecay(ChangeBlockEvent.Decay e){		
		for (Transaction<BlockSnapshot> t:e.getTransactions()){
			Location<World> loc = t.getOriginal().getLocation().get();
			Region r = RedProtect.rm.getTopRegion(loc);		
			if (r != null && !r.canFlow()){
	         	 t.setValid(false);           	  
			}
		}	
	}*/
}
