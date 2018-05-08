package br.net.fabiozumbi12.RedProtect.Sponge.listeners;

import java.util.*;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.block.tileentity.TileEntityTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.property.block.MatterProperty;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.hanging.Hanging;
import org.spongepowered.api.entity.living.monster.Monster;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.explosive.fireball.Fireball;
import org.spongepowered.api.entity.vehicle.Boat;
import org.spongepowered.api.entity.vehicle.minecart.Minecart;
import org.spongepowered.api.entity.weather.Lightning;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.action.LightningEvent;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.IgniteEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.explosion.Explosion;

import br.net.fabiozumbi12.RedProtect.Sponge.RPContainer;
import br.net.fabiozumbi12.RedProtect.Sponge.RPUtil;
import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import br.net.fabiozumbi12.RedProtect.Sponge.Region;
import br.net.fabiozumbi12.RedProtect.Sponge.RegionBuilder;
import br.net.fabiozumbi12.RedProtect.Sponge.actions.EncompassRegionBuilder;
import br.net.fabiozumbi12.RedProtect.Sponge.config.RPLang;

public class RPBlockListener {
	
	private static final RPContainer cont = new RPContainer();
	
	public RPBlockListener(){
		RedProtect.get().logger.debug("blocks","Loaded RPBlockListener...");
	}    
    
	@Listener(order = Order.FIRST, beforeModifications = true)
    public void onSignPlace(ChangeSignEvent e, @First Player p) {   
    	RedProtect.get().logger.debug("blocks","BlockListener56 - Is SignChangeEvent event! Cancelled? " + e.isCancelled());
    	
    	Sign s = e.getTargetTile();
    	List<Text> lines = e.getText().asList();
        Location<World> loc = s.getLocation();
        World w = p.getWorld();
        BlockSnapshot b = w.createSnapshot(loc.getBlockPosition());

        Region signr = RedProtect.get().rm.getTopRegion(loc);
                
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
        
        if (RedProtect.get().cfgs.getBool("server-protection.sign-spy.enabled")){
        	Sponge.getServer().getConsole().sendMessage(RPUtil.toText(RPLang.get("blocklistener.signspy.location").replace("{x}", ""+loc.getX()).replace("{y}", ""+loc.getY()).replace("{z}", ""+loc.getZ()).replace("{world}", w.getName())));
        	Sponge.getServer().getConsole().sendMessage(RPUtil.toText(RPLang.get("blocklistener.signspy.player").replace("{player}", p.getName())));
        	Sponge.getServer().getConsole().sendMessage(RPUtil.toText(RPLang.get("blocklistener.signspy.lines12").replace("{line1}", lines.get(0).toPlain()).replace("{line2}", lines.get(1).toPlain())));
        	Sponge.getServer().getConsole().sendMessage(RPUtil.toText(RPLang.get("blocklistener.signspy.lines34").replace("{line3}", lines.get(2).toPlain()).replace("{line4}", lines.get(3).toPlain())));
        	if (!RedProtect.get().cfgs.getBool("server-protection.sign-spy.only-console")){
        		for (Player play:Sponge.getServer().getOnlinePlayers()){
        			if (RedProtect.get().ph.hasGenPerm(play, "redprotect.signspy")/* && !play.equals(p)*/){
        				play.sendMessage(RPUtil.toText(RPLang.get("blocklistener.signspy.location").replace("{x}", ""+loc.getX()).replace("{y}", ""+loc.getY()).replace("{z}", ""+loc.getZ()).replace("{world}", w.getName())));
        	        	play.sendMessage(RPUtil.toText(RPLang.get("blocklistener.signspy.player").replace("{player}", p.getName())));
        	        	play.sendMessage(RPUtil.toText(RPLang.get("blocklistener.signspy.lines12").replace("{line1}", lines.get(0).toPlain()).replace("{line2}", lines.get(1).toPlain())));
        	        	play.sendMessage(RPUtil.toText(RPLang.get("blocklistener.signspy.lines34").replace("{line3}", lines.get(2).toPlain()).replace("{line4}", lines.get(3).toPlain())));
        			}
        		}
        	}
        }
        
        if ((RedProtect.get().cfgs.getBool("private.use") && s.getType().equals(TileEntityTypes.SIGN)) && (line1.toPlain().equalsIgnoreCase("private") || line1.toPlain().equalsIgnoreCase("[private]") || line1.toPlain().equalsIgnoreCase(RPLang.get("blocklistener.container.signline")) || line1.toPlain().equalsIgnoreCase("["+RPLang.get("blocklistener.container.signline")+"]"))) {
        	Region r = RedProtect.get().rm.getTopRegion(loc);        
        	Boolean out = RedProtect.get().cfgs.getBool("private.allow-outside");
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
            		RedProtect.get().getPVHelper().getCause(p);
            		return;
            	}
        	} else {
        		RPLang.sendMessage(p, "blocklistener.container.notregion");
				RedProtect.get().getPVHelper().getCause(p);
        		return;
        	}        	
        }
                
        if (line1.toPlain().equalsIgnoreCase("[rp]")){
        	String claimmode = RedProtect.get().cfgs.getWorldClaimType(p.getWorld().getName());
            if ((!claimmode.equalsIgnoreCase("BLOCK") && !claimmode.equalsIgnoreCase("BOTH")) && !RedProtect.get().ph.hasPerm(p, "redprotect.admin.create")) {
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
                RedProtect.get().rm.add(r, RedProtect.get().serv.getWorld(r.getWorld()).get());
            }
        }
    }
    
    void setErrorSign(ChangeSignEvent e, Player p, String error) {
        List<Text> lines = e.getTargetTile().get(Keys.SIGN_LINES).get();
        lines.set(0, RPUtil.toText(RPLang.get("regionbuilder.signerror")));
        e.getTargetTile().offer(Keys.SIGN_LINES, lines);
        RPLang.sendMessage(p, RPLang.get("regionbuilder.signerror") + ": " + error);
    }
    
	@Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockPlace(ChangeBlockEvent.Place e, @First Player p) {
    	RedProtect.get().logger.debug("blocks","BlockListener - Is BlockPlaceEvent event! Cancelled? " + e.isCancelled());
    	    	
    	BlockSnapshot b = e.getTransactions().get(0).getOriginal();
    	Location<World> bloc = b.getLocation().get();
    	World w = bloc.getExtent();
    	
        ItemType m = ItemTypes.NONE;
        if (p.getItemInHand(HandTypes.MAIN_HAND).isPresent()){
        	m = p.getItemInHand(HandTypes.MAIN_HAND).get().getItem();
        } else if (p.getItemInHand(HandTypes.OFF_HAND).isPresent()){
        	m = p.getItemInHand(HandTypes.OFF_HAND).get().getItem();
        }
        Boolean antih = RedProtect.get().cfgs.getBool("region-settings.anti-hopper");
        Region r = RedProtect.get().rm.getTopRegion(b.getLocation().get());
        
        if (r == null && RedProtect.get().cfgs.getGlobalFlagList(w.getName(),"if-build-false","place-blocks").contains(b.getState().getType().getName())){
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
                	if (!RedProtect.get().ph.hasPerm(p, "redprotect.bypass") && antih && 
                			(m.equals(ItemTypes.HOPPER) || m.getName().contains("rail"))){
                		int x = bloc.getBlockX();
                		int y = bloc.getBlockY();
                		int z = bloc.getBlockZ();
                		BlockSnapshot ib = w.createSnapshot(x, y+1, z);
                		if (!cont.canBreak(p, ib) || !cont.canBreak(p, b)){
                			RPLang.sendMessage(p, "blocklistener.container.chestinside");
                			e.setCancelled(true);
                        }
                	}
                }
            } catch (Throwable t) {
                t.printStackTrace();
            } 
        }    
    }    	
    
    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockBreak(ChangeBlockEvent.Break e, @First Player p) {
    	RedProtect.get().logger.debug("blocks","BlockListener - Is ChangeBlockEvent.Break event!");
    	
    	BlockSnapshot b = e.getTransactions().get(0).getOriginal();
    	Location<World> bloc = b.getLocation().get();
    	World w = bloc.getExtent();

        Boolean antih = RedProtect.get().cfgs.getBool("region-settings.anti-hopper");
        Region r = RedProtect.get().rm.getTopRegion(bloc);
        
        if (!RedProtect.get().ph.hasPerm(p, "redprotect.bypass")){
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
        
        if (r == null && RedProtect.get().cfgs.getGlobalFlagList(p.getWorld().getName(),"if-build-false","break-blocks").contains(b.getState().getType().getName())){
        	return;
        }
        
        if (r != null && b.getState().getType().equals(BlockTypes.MOB_SPAWNER) && r.allowSpawner(p)){
    		return;
    	}
        
        if (r != null && !r.canBuild(p) && !r.canTree(b) && !r.canMining(b) && !r.canCrops(b) && !r.canBreak(b)){
        	RPLang.sendMessage(p, "blocklistener.region.cantbuild");
            e.setCancelled(true);
        }
    }
    
    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockPlaceGeneric(ChangeBlockEvent.Place e) {
    	if (e.getCause().root().toString().contains("minecraft:fire")){
    		Region r = RedProtect.get().rm.getTopRegion(e.getTransactions().get(0).getOriginal().getLocation().get());
    		if (r != null && !r.canFire()){    	
    			e.setCancelled(true);
    			RedProtect.get().logger.debug("blocks", "Tryed to PLACE FIRE!");
    		}
    	}    	
    }
    
    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockGrow(ChangeBlockEvent.Grow e) {
    	RedProtect.get().logger.debug("blocks","RPBlockListener - Is ChangeBlockEvent.Grow event");
		
    	BlockSnapshot b = e.getTransactions().get(0).getOriginal();
		Region r = RedProtect.get().rm.getTopRegion(b.getLocation().get());
		if (r != null && !r.canGrow()){
			e.setCancelled(true);
			RedProtect.get().logger.debug("blocks", "Cancel grow "+b.getState().getName());
		}
    }
    
    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockBreakGeneric(ChangeBlockEvent.Break e) {
		RedProtect.get().logger.debug("blocks","RPBlockListener - Is onBlockBreakGeneric event");

    	if (e.getCause().root().toString().contains("minecraft:fire")){
    		BlockSnapshot b = e.getTransactions().get(0).getOriginal();
    		Region r = RedProtect.get().rm.getTopRegion(b.getLocation().get());    		
    		if (r != null && !r.canFire() && !b.getState().getType().equals(BlockTypes.FIRE)){    	
    			e.setCancelled(true);
    			RedProtect.get().logger.debug("blocks", "Tryed to break from FIRE!");
    		}
    	}
		LocatableBlock locatable = e.getCause().first(LocatableBlock.class).orElse(null);
		if (locatable != null) {
			BlockState sourceState = locatable.getBlockState();
			//liquid check
			MatterProperty mat = sourceState.getProperty(MatterProperty.class).orElse(null);
			if (mat != null && mat.getValue() == MatterProperty.Matter.LIQUID){
				Region r = RedProtect.get().rm.getTopRegion(locatable.getLocation());
				if (r != null && !r.FlowDamage() && locatable.getLocation().getBlockType() != BlockTypes.AIR){
					e.setCancelled(true);
					RedProtect.get().logger.debug("blocks", "Tryed to break from "+sourceState.getType().getName());
				}
			}
		}
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onFrameAndBoatBrake(DamageEntityEvent e) {
    	
    	RedProtect.get().logger.debug("blocks","Is BlockListener - DamageEntityEvent event");
    	Entity ent = e.getTargetEntity();
    	Location<World> l = e.getTargetEntity().getLocation();
    	    
    	Region r = RedProtect.get().rm.getTopRegion(l);
		if (r == null) return;

    	if (ent instanceof Hanging && e.getCause().first(Monster.class).isPresent()) {    		
    		if (!r.canFire()){
    			e.setCancelled(true);
        		return;
    		}
        }   
    	
    	if ((ent instanceof Boat || ent instanceof Minecart)  && e.getCause().first(Player.class).isPresent()){
    		Player p = e.getCause().first(Player.class).get();
    		if (!r.canMinecart(p)){
    			RPLang.sendMessage(p, "blocklistener.region.cantbreak");
    			e.setCancelled(true);
            }
    	}
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockStartBurn(IgniteEntityEvent e){
    	
    	Entity b = e.getTargetEntity();
    	Cause ignit = e.getCause(); 

    	RedProtect.get().logger.debug("blocks","Is BlockIgniteEvent event. Canceled? " + e.isCancelled());
    	
    	Region r = RedProtect.get().rm.getTopRegion(b.getLocation());
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
            }
		}
    }

	@Listener(order = Order.FIRST, beforeModifications = true)
    public void onFlow(NotifyNeighborBlockEvent e){
		LocatableBlock locatable = e.getCause().first(LocatableBlock.class).orElse(null);
		if (locatable != null){
			BlockState sourceState = locatable.getBlockState();
			//liquid check
			MatterProperty mat = sourceState.getProperty(MatterProperty.class).orElse(null);
			if (mat != null && mat.getValue() == MatterProperty.Matter.LIQUID){
				//set source as not flowing
				Region r1 = RedProtect.get().rm.getTopRegion(locatable.getLocation());
				if (r1 != null && !r1.canFlow() && sourceState.getType().getName().contains("flowing_")) {
					changeBlockLiquid(locatable.getLocation(), sourceState.getType());
				}
				//remove others
				Iterator<Direction> it = e.getNeighbors().keySet().iterator();
				while (it.hasNext()){
					Direction dir = it.next();
					Location<World> newLoc = locatable.getLocation().getBlockRelative(dir);
					Region r = RedProtect.get().rm.getTopRegion(newLoc);
					//flow check
					if (r != null && !r.canFlow()) {
						it.remove();
					} else
						//TODO temp fix for pixelmon infinite berry bug
						if (newLoc.getBlockType().getName().contains("_berry")){
						it.remove();
						RedProtect.get().getPVHelper().setBlock(locatable.getLocation(), BlockTypes.AIR.getDefaultState());
					}
				}
			} else {
				//remove notify blocks
				Iterator<Direction> it = e.getNeighbors().keySet().iterator();
				while (it.hasNext()){
					Direction dir = it.next();
					Location<World> newLoc = locatable.getLocation().getBlockRelative(dir);
					Region r = RedProtect.get().rm.getTopRegion(newLoc);
					if (r != null && !r.canFlow()) {
						//flow check
						MatterProperty mat2 = newLoc.getBlock().getProperty(MatterProperty.class).orElse(null);
						if (mat2 != null && mat2.getValue() == MatterProperty.Matter.LIQUID){
							it.remove();
							//TODO temp fix for pixelmon infinite berry bug
							if (sourceState.getName().contains("_berry")){
								RedProtect.get().getPVHelper().setBlock(locatable.getLocation(), BlockTypes.AIR.getDefaultState());
							}
						}
					}
				}
			}
		}
    }

    private void changeBlockLiquid(Location<World> local, BlockType blockType){
		Optional<BlockType> type = Sponge.getRegistry().getType(BlockType.class, blockType.getName().replace("flowing_", ""));
		type.ifPresent(bt -> RedProtect.get().getPVHelper().setBlock(local, bt.getDefaultState()));
	}

	@Listener(order = Order.FIRST, beforeModifications = true)
	public void onLightning(LightningEvent.Pre e, @First Lightning light){
		RedProtect.get().logger.debug("blocks","Is LightningStrikeEvent event");
		Location<World> l = light.getLocation();
		Region r = RedProtect.get().rm.getTopRegion(l);
		if (r != null && !r.canFire()){
			e.setCancelled(true);
        }
	}

	@Listener(order = Order.FIRST, beforeModifications = true)
	public void onDecay(ChangeBlockEvent.Decay e){
		BlockSnapshot bfrom = e.getTransactions().get(0).getOriginal();
		RedProtect.get().logger.debug("blocks","Is BlockFromToEvent.Decay event is to " + bfrom.getState().getType().getName() + " from " + bfrom.getState().getType().getName());
		Region r = RedProtect.get().rm.getTopRegion(bfrom.getLocation().get());
		if (r != null && !r.leavesDecay()){
			e.setCancelled(true);
		}
	}

	@Listener(order = Order.FIRST, beforeModifications = true)
    public void onInteractBlock(InteractBlockEvent event, @First Player p) {
    	BlockSnapshot b = event.getTargetBlock();
        Location<World> l;
        
        RedProtect.get().logger.debug("player","RPBlockListener - Is InteractBlockEvent event");
        
        if (!b.getState().getType().equals(BlockTypes.AIR)){
        	l = b.getLocation().get();
        	RedProtect.get().logger.debug("player","RPBlockListener - Is InteractBlockEvent event. The block is " + b.getState().getType().getName());
        } else {
        	l = p.getLocation();
        }
        
        Region r = RedProtect.get().rm.getTopRegion(l);
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
    
    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onInteractPrimBlock(InteractBlockEvent.Primary event, @First Player p) {
    	BlockSnapshot b = event.getTargetBlock();
        
        RedProtect.get().logger.debug("player","RPBlockListener - Is InteractBlockEvent.Primary event");
        
    	if (!RedProtect.get().ph.hasPerm(p, "redprotect.bypass")){        	
    		if (b.getState().getType().getName().contains("sign") && !cont.canBreak(p, b)){
    			RPLang.sendMessage(p, "blocklistener.container.breakinside");
    			event.setCancelled(true);
            }
        }
    }
}
