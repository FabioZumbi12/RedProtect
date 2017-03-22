package br.net.fabiozumbi12.redprotect.listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.hanging.Hanging;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.entity.projectile.ThrownPotion;
import org.spongepowered.api.entity.projectile.source.ProjectileSource;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.IndirectEntityDamageSource;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.cause.entity.teleport.TeleportCause;
import org.spongepowered.api.event.cause.entity.teleport.TeleportType;
import org.spongepowered.api.event.cause.entity.teleport.TeleportTypes;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.entity.CollideEntityEvent;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.HarvestEntityEvent;
import org.spongepowered.api.event.entity.HealEntityEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.projectile.LaunchProjectileEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.event.item.inventory.UseItemStackEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.event.world.ConstructPortalEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import br.net.fabiozumbi12.redprotect.RPContainer;
import br.net.fabiozumbi12.redprotect.RPDoor;
import br.net.fabiozumbi12.redprotect.RPUtil;
import br.net.fabiozumbi12.redprotect.RedProtect;
import br.net.fabiozumbi12.redprotect.Region;
import br.net.fabiozumbi12.redprotect.config.RPLang;

@SuppressWarnings("deprecation")
public class RPPlayerListener{
	
	static RPContainer cont = new RPContainer();
	private HashMap<Player,String> Ownerslist = new HashMap<Player,String>();
	private HashMap<Player, String> PlayerCmd = new HashMap<Player, String>();
	private HashMap<String, String> PlayertaskID = new HashMap<String, String>();
    
    public RPPlayerListener() {
    	RedProtect.logger.debug("player","Loaded RPPlayerListener...");
    }
    
    @Listener
    public void onConsume(UseItemStackEvent.Start e, @First Player p){
        ItemStack stack = e.getItemStackInUse().createStack();
        RedProtect.logger.debug("player", "Is UseItemStackEvent.Start event. Item: "+stack.getItem().getName());
        
        //deny potion
        List<String> Pots = RedProtect.cfgs.getStringList("server-protection.deny-potions");
        
        if(stack.get(Keys.POTION_EFFECTS).isPresent() && Pots.size() > 0){
        	List<PotionEffect> pot = stack.get(Keys.POTION_EFFECTS).get();   
        	for (PotionEffect pots:pot){
        		if (Pots.contains(pots.getType().getName().toUpperCase()) && !p.hasPermission("redprotect.bypass")){        			
        			e.setCancelled(true);
        			RPLang.sendMessage(p, "playerlistener.denypotion");
        		}
        	}                
        }
        
        Region r = RedProtect.rm.getTopRegion(p.getLocation());
        
        if (r != null && stack.getItem().equals(ItemTypes.POTION) && !r.usePotions(p)){
        	RPLang.sendMessage(p, "playerlistener.region.cantuse");
            e.setCancelled(true);			
		}
        
        if (r != null && stack.getItem().getName().equals("minecraft:chorus_fruit") && !r.canTeleport(p)){
        	RPLang.sendMessage(p, "playerlistener.region.cantuse");
        	e.setCancelled(true);
        }
    }
    
    //listen left click
    @Listener
    public void onInteractLeft(InteractBlockEvent.Primary event, @First Player p) {
        BlockSnapshot b = event.getTargetBlock();
        Location<World> l = null;
        
        RedProtect.logger.debug("player","RPPlayerListener - Is InteractBlockEvent.Primary event");
        
        if (!b.getState().getType().equals(BlockTypes.AIR)){
        	l = b.getLocation().get();
        	RedProtect.logger.debug("player","RPPlayerListener - Is InteractBlockEvent.Primary event. The block is " + b.getState().getType().getName());
        } else {
        	l = p.getLocation();
        }
        
        ItemType itemInHand = ItemTypes.NONE;
        if (p.getItemInHand(HandTypes.MAIN_HAND).isPresent()){
        	itemInHand = p.getItemInHand(HandTypes.MAIN_HAND).get().getItem();
        } else if (p.getItemInHand(HandTypes.OFF_HAND).isPresent()){
        	itemInHand = p.getItemInHand(HandTypes.OFF_HAND).get().getItem();
        }
        
        String claimmode = RedProtect.cfgs.getWorldClaimType(p.getWorld().getName());
    	if (itemInHand.getId().equalsIgnoreCase(RedProtect.cfgs.getString("wands.adminWandID")) && ((claimmode.equalsIgnoreCase("WAND") || claimmode.equalsIgnoreCase("BOTH")) || RedProtect.ph.hasPerm(p, "redprotect.admin.claim"))) {
    		if (!RPUtil.canBuildNear(p, l)){
    			event.setCancelled(true);
    			return;
    		}
    		RedProtect.firstLocationSelections.put(p, l);
            p.sendMessage(RPUtil.toText(RPLang.get("playerlistener.wand1") + RPLang.get("general.color") + " (&e" + l.getBlockX() + RPLang.get("general.color") + ", &e" + l.getBlockY() + RPLang.get("general.color") + ", &e" + l.getBlockZ() + RPLang.get("general.color") + ")."));
            event.setCancelled(true);
            
            //show preview border
            if (RedProtect.firstLocationSelections.containsKey(p) && RedProtect.secondLocationSelections.containsKey(p)){       
            	RPUtil.addBorder(p, get4Points(RedProtect.firstLocationSelections.get(p),RedProtect.secondLocationSelections.get(p),p.getLocation().getBlockY()));                	
            }   
            return;
        }
    }
    
    //listen right click
    @Listener
    public void onInteractRight(InteractBlockEvent.Secondary event, @First Player p) {
    	
        BlockSnapshot b = event.getTargetBlock();
        Location<World> l = null;
        
        RedProtect.logger.debug("player","RPPlayerListener - Is InteractBlockEvent.Secondary event");
        
        if (!b.getState().getType().equals(BlockTypes.AIR)){
        	l = b.getLocation().get();
        	RedProtect.logger.debug("player","RPPlayerListener - Is InteractBlockEvent.Secondary event. The block is " + b.getState().getType().getName());
        } else {
        	l = p.getLocation();
        }
        
        Region r = RedProtect.rm.getTopRegion(l);
        ItemType itemInHand = RPUtil.getItemHand(p);
        
        String claimmode = RedProtect.cfgs.getWorldClaimType(p.getWorld().getName());
    	if (itemInHand.getId().equalsIgnoreCase(RedProtect.cfgs.getString("wands.adminWandID")) && ((claimmode.equalsIgnoreCase("WAND") || claimmode.equalsIgnoreCase("BOTH")) || RedProtect.ph.hasPerm(p, "redprotect.admin.claim"))) {
    		if (!RPUtil.canBuildNear(p, l)){
    			event.setCancelled(true);
    			return;
    		}
    		RedProtect.secondLocationSelections.put(p, l);
            p.sendMessage(RPUtil.toText(RPLang.get("playerlistener.wand2") + RPLang.get("general.color") + " (&e" + l.getBlockX() + RPLang.get("general.color") + ", &e" + l.getBlockY() + RPLang.get("general.color") + ", &e" + l.getBlockZ() + RPLang.get("general.color") + ")."));
            event.setCancelled(true);
            
            //show preview border
            if (RedProtect.firstLocationSelections.containsKey(p) && RedProtect.secondLocationSelections.containsKey(p)){       
            	RPUtil.addBorder(p, get4Points(RedProtect.firstLocationSelections.get(p),RedProtect.secondLocationSelections.get(p),p.getLocation().getBlockY()));                	
            }
            return;  
        }
    	
    	//other blocks and interactions       	
    	if (r != null){
            if ((itemInHand.equals(ItemTypes.ENDER_PEARL) || itemInHand.getName().equals("minecraft:chorus_fruit")) && !r.canTeleport(p)){
            	RPLang.sendMessage(p, "playerlistener.region.cantuse");
            	event.setUseItemResult(Tristate.FALSE);
            	event.setCancelled(true); 
    			return;
    		} else if ((itemInHand.equals(ItemTypes.BOW) || itemInHand.equals(ItemTypes.SNOWBALL) || itemInHand.equals(ItemTypes.EGG)) && !r.canProtectiles(p)){
    			RPLang.sendMessage(p, "playerlistener.region.cantuse");
    			event.setUseItemResult(Tristate.FALSE);
                event.setCancelled(true); 
                return;
    		} else if (itemInHand.equals(ItemTypes.POTION) && !r.usePotions(p)){
    			RPLang.sendMessage(p, "playerlistener.region.cantuse");
    			event.setUseItemResult(Tristate.FALSE);
                event.setCancelled(true); 
                return;
    		} else if (itemInHand.equals(ItemTypes.MONSTER_EGG) && !r.canInteractPassives(p)){
    			RPLang.sendMessage(p, "playerlistener.region.cantuse");
    			event.setUseItemResult(Tristate.FALSE);
                event.setCancelled(true); 
    		}
        }
    }
    
    private List<Location<World>> get4Points(Location<World> min, Location<World> max, int y){
		List <Location<World>> locs = new ArrayList<Location<World>>();
		locs.add(new Location<World>(min.getExtent(),min.getX(),y,min.getZ()));		
		locs.add(new Location<World>(min.getExtent(),min.getX(),y,min.getZ()+(max.getZ()-min.getZ())));
		locs.add(new Location<World>(max.getExtent(),max.getX(),y,max.getZ()));
		locs.add(new Location<World>(min.getExtent(),min.getX()+(max.getX()-min.getX()),y,min.getZ()));
		return locs;		
	}
    
    //listen all
    @Listener
    public void onInteractBlock(InteractBlockEvent event, @First Player p) {
    	RedProtect.logger.debug("player","RPPlayerListener - InteractBlockEvent canceled? " + event.isCancelled());
    	
        BlockSnapshot b = event.getTargetBlock();
        BlockState bstate = b.getState();
        Location<World> l = null;
        
        if (!b.getState().getType().equals(BlockTypes.AIR)){
        	l = b.getLocation().get();
        	RedProtect.logger.debug("player","RPPlayerListener - Is InteractBlockEvent event. The block is " + bstate.getType().getName());
        } else {
        	l = p.getLocation();
        }
        
        Region r = RedProtect.rm.getTopRegion(l);
        ItemType itemInHand = ItemTypes.NONE;
        ItemStack stack = ItemStack.of(ItemTypes.NONE,1);
        if (p.getItemInHand(HandTypes.MAIN_HAND).isPresent()){
        	stack = p.getItemInHand(HandTypes.MAIN_HAND).get();
        	itemInHand = stack.getItem();
        	if (RPUtil.removeGuiItem(stack)){        	
            	p.setItemInHand(HandTypes.MAIN_HAND,ItemStack.of(ItemTypes.NONE, 1));
            	event.setCancelled(true);
            }
        } else if (p.getItemInHand(HandTypes.OFF_HAND).isPresent()){
        	stack = p.getItemInHand(HandTypes.OFF_HAND).get();
        	itemInHand = stack.getItem();
        	if (RPUtil.removeGuiItem(stack)){        	
            	p.setItemInHand(HandTypes.OFF_HAND,ItemStack.of(ItemTypes.NONE, 1));
            	event.setCancelled(true);
            }
        }
        
        if (itemInHand.getId().equalsIgnoreCase(RedProtect.cfgs.getString("wands.infoWandID"))) {
        	r = RedProtect.rm.getTopRegion(l);
            if (RedProtect.ph.hasUserPerm(p, "redprotect.infowand")) {
                if (r == null) {
                    RPLang.sendMessage(p, "playerlistener.noregion.atblock");
                }
                else if (r.canBuild(p)) {
                    p.sendMessage(RPUtil.toText(RPLang.get("general.color") + "--------------- [&e" + r.getName() + RPLang.get("general.color") + "] ---------------"));
                    p.sendMessage(r.info());
                    p.sendMessage(RPUtil.toText(RPLang.get("general.color") + "-----------------------------------------"));
                } else {
                	p.sendMessage(RPUtil.toText(RPLang.get("playerlistener.region.entered").replace("{region}", r.getName()).replace("{leaders}", RPUtil.UUIDtoPlayer(r.getLeadersDesc()))));
                }
                event.setCancelled(true);
                return;
            }
        } 
        
        //start player checks
        if (r == null){
        	if (b != null && (bstate instanceof Container ||
        			RedProtect.cfgs.getStringList("private.allowed-blocks").contains(bstate.getType().getName()))){ 
        		Boolean out = RedProtect.cfgs.getBool("private.allow-outside");
            	if (out && !cont.canOpen(b, p)) {
        			if (!RedProtect.ph.hasPerm(p, "redprotect.bypass")) {
                        RPLang.sendMessage(p, "playerlistener.region.cantopen");
                        event.setCancelled(true);
                        return;
                    } else {
                    	int x = b.getLocation().get().getBlockX();
                    	int y = b.getLocation().get().getBlockY();
                    	int z = b.getLocation().get().getBlockZ();
                        RPLang.sendMessage(p, RPLang.get("playerlistener.region.opened").replace("{region}", "X:"+x+" Y:"+y+" Z:"+z));
                    }                    
                }
        	}
                    	
        } else { //if r != null >>       	
        	
        	//if (r != null) && (b != null) >>
        	if (b != null) {
        		if (bstate.getType().equals(BlockTypes.DRAGON_EGG) ||
        				bstate.getType().equals(BlockTypes.BED) ||
        				bstate.getType().equals(BlockTypes.NOTEBLOCK) ||
        				bstate.getType().getName().contains("repeater") ||
        				bstate.getType().getName().contains("comparator")){        	
                	
                	if (!r.canBuild(p)){
                		RPLang.sendMessage(p, "playerlistener.region.cantinteract");
                		event.setCancelled(true);
                        return;
                	}
                } 
                else if ((b.getState() instanceof Carrier) ||
                		RedProtect.cfgs.getStringList("private.allowed-blocks").contains(bstate.getType().getName())){   
                	
                	if ((r.canChest(p) && !cont.canOpen(b, p) || (!r.canChest(p) && cont.canOpen(b, p)) || (!r.canChest(p) && !cont.canOpen(b, p)))) {
                            if (!RedProtect.ph.hasPerm(p, "redprotect.bypass")) {
                                RPLang.sendMessage(p, "playerlistener.region.cantopen");
                                event.setCancelled(true);
                                return;
                            }
                            else {
                                RPLang.sendMessage(p, RPLang.get("playerlistener.region.opened").replace("{region}", RPUtil.UUIDtoPlayer(r.getLeadersDesc())));
                            }
                	} 
                }               
                
                else if (bstate.getType().getName().contains("lever")) {
                    if (!r.canLever(p)) {
                        if (!RedProtect.ph.hasPerm(p, "redprotect.bypass")) {
                            RPLang.sendMessage(p, "playerlistener.region.cantlever");
                            event.setCancelled(true);
                        }
                        else {
                            RPLang.sendMessage(p, RPLang.get("playerlistener.region.levertoggled").replace("{region}", RPUtil.UUIDtoPlayer(r.getLeadersDesc())));
                        }
                    }
                }
                else if (bstate.getType().getName().contains("button")) {
                    if (!r.canButton(p)) {
                        if (!RedProtect.ph.hasPerm(p, "redprotect.bypass")) {
                            RPLang.sendMessage(p, "playerlistener.region.cantbutton");
                            event.setCancelled(true);
                        }
                        else {
                            RPLang.sendMessage(p, RPLang.get("playerlistener.region.buttonactivated").replace("{region}", RPUtil.UUIDtoPlayer(r.getLeadersDesc())));
                        }
                    }
                }
                else if (RPDoor.isOpenable(b)) {
                	if (!r.canDoor(p)/* || (r.canDoor(p) && !cont.canOpen(b, p))*/) {
                        if (!RedProtect.ph.hasPerm(p, "redprotect.bypass")) {
                            RPLang.sendMessage(p, "playerlistener.region.cantdoor");                    
                            event.setCancelled(true);
                        } else {
                            RPLang.sendMessage(p, "playerlistener.region.opendoor");
                            RPDoor.ChangeDoor(b, r);
                        }
                    } else {
                    	RPDoor.ChangeDoor(b, r);
                    }            	                
                } 
                else if (bstate.getType().getName().contains("rail")){
                    if (!r.canMinecart(p)){
                		RPLang.sendMessage(p, "blocklistener.region.cantplace");
                		event.setCancelled(true);
            			return;		
                	}
                } 
                else if (bstate.getType().getName().contains("sign") && !r.canSign(p)){
                	      List<Text> sign = b.get(Keys.SIGN_LINES).get();
                	      for (String tag:RedProtect.cfgs.getStringList("region-settings.allow-sign-interact-tags")){
                	    	  //check first rule
                	    	  if (tag.equalsIgnoreCase(sign.get(0).toPlain())){
                    	    	  return;
                    	      }
                	    	  
                	    	  //check if tag is leaders or members names
                	    	  if (tag.equalsIgnoreCase("{membername}")){
                	    		  for (String leader:r.getLeaders()){
                    	    		  if (sign.get(0).toPlain().equalsIgnoreCase(RPUtil.UUIDtoPlayer(leader))){
                    	    			  return;
                    	    		  }
                    	    	  }
                	    		  for (String member:r.getMembers()){
                    	    		  if (sign.get(0).toPlain().equalsIgnoreCase(RPUtil.UUIDtoPlayer(member))){
                    	    			  return;
                    	    		  }
                    	    	  }
                	    		  for (String admin:r.getAdmins()){
                    	    		  if (sign.get(0).toPlain().equalsIgnoreCase(RPUtil.UUIDtoPlayer(admin))){
                    	    			  return;
                    	    		  }
                    	    	  }
                	    	  }  
                	    	  
                	    	  //check if tag is player name
                	    	  if (tag.equalsIgnoreCase("{playername}")){
                	    		  if (sign.get(0).toPlain().equalsIgnoreCase(RPUtil.UUIDtoPlayer(p.getName()))){
                	    			  return;
                	    		  }
                	    	  }
                	      }        	              	      
                	      RPLang.sendMessage(p, "playerlistener.region.cantinteract.signs");
                	      event.setCancelled(true);
                	      return;
                } 
                else if ((itemInHand.equals(ItemTypes.FLINT_AND_STEEL) || 
                		itemInHand.equals(ItemTypes.WATER_BUCKET) || 
                		itemInHand.equals(ItemTypes.BUCKET) || 
                		itemInHand.equals(ItemTypes.LAVA_BUCKET) || 
                		itemInHand.equals(ItemTypes.ITEM_FRAME) || 
                		itemInHand.equals(ItemTypes.PAINTING)) && !r.canBuild(p)) {
                    RPLang.sendMessage(p, RPLang.get("playerlistener.region.cantuse"));
                    event.setCancelled(true);
                    return;
                }                    
                else if (!r.allowMod(p) && !RPUtil.isBukkitBlock(bstate)){
                	RPLang.sendMessage(p, "playerlistener.region.cantinteract");
                	event.setCancelled(true);    
                	return;
                }
        	}             
        }               
    }
    
    /*
    @Listener
    public void MoveItem(ClickInventoryEvent e, @First Player p){
    	Container cont = e.getTargetInventory().getProperties(Names.);
    	
    	Region r = RedProtect.rm.getTopRegion(p.getLocation());
    	if (r != null && e.getInventory().getTitle() != null){
    		if (r.getName().length() > 16){
        		if (e.getInventory().getTitle().equals(RPLang.get("gui.invflag").replace("{region}", r.getName().substring(0, 16)))){
            		return;
            	}
        	} else {
        		if (e.getInventory().getTitle().equals(RPLang.get("gui.invflag").replace("{region}", r.getName())) || e.getInventory().getTitle().equals(RPLang.get("gui.editflag"))){
            		return;
            	}
        	}
    	}    	
    	
    	if (RPUtil.RemoveGuiItem(e.getCurrentItem())){
    		e.setCurrentItem(new ItemStack(Material.AIR));
    	}
    }
    	*/
    
    @Listener
    public void onPlayerInteract(InteractEntityEvent e, @First Player p) {
        Entity ent = e.getTargetEntity();
        RedProtect.logger.debug("player","RPPlayerListener - Is InteractEntityEvent event: " + ent.getType().getName());
        
        Location<World> l = ent.getLocation();
        Region r = RedProtect.rm.getTopRegion(l);
        if (r == null){
        	return;
        }
        
        if (RedProtect.tpWait.contains(p.getName())){
    		RedProtect.tpWait.remove(p.getName());
    		RPLang.sendMessage(p, "cmdmanager.region.tpcancelled");
    	}
                
        if (ent instanceof Hanging || ent.getType().equals(EntityTypes.ARMOR_STAND)) {        	
            if (!r.canBuild(p)) {
                RPLang.sendMessage(p, "playerlistener.region.cantedit");
                e.setCancelled(true);
                return;
            }
        } 
        
        else if ((ent.getType().getName().contains("minecart") || ent.getType().getName().contains("boat")) && !r.canMinecart(p)) {
        	RPLang.sendMessage(p, "blocklistener.region.cantenter");
            e.setCancelled(true);
            return;
        } 
                
        else if (!r.allowMod(p) && !RPUtil.isBukkitEntity(ent) && (!(ent instanceof Player))){
        	RedProtect.logger.debug("player","PlayerInteractEntityEvent - Block is " + ent.getType().getName());
        	RPLang.sendMessage(p, "playerlistener.region.cantinteract");
        	e.setCancelled(true);        	       
        }        
    }
    
    @Listener
    public void onEntityDamageEvent(DamageEntityEvent e) { 
    	//victim
    	Entity e1 = e.getTargetEntity(); 
    	
    	//damager
    	Entity e2 = null;
    	    	
    	if (e.getCause().first(IndirectEntityDamageSource.class).isPresent()){
    		e2 = e.getCause().first(IndirectEntityDamageSource.class).get().getSource();
    		
    		RedProtect.logger.debug("player","RPLayerListener: Is DamageEntityEvent event. Damager "+e2.getType().getName()); 
    	}
    	
    	Player damager = null;
    	if (e2 instanceof Projectile){
    		Projectile proj = (Projectile)e2;
    		if (proj.getShooter() instanceof Player){
    			damager = (Player) proj.getShooter();
    		}
    	} else if (e2 instanceof Player){
    		damager = (Player) e2;
    	}
    	
    	Location<World> l = e1.getLocation();
        Region r = RedProtect.rm.getTopRegion(l);
        if (r == null){
        	return;
        }     
        
        RedProtect.logger.debug("player","RPLayerListener: Is DamageEntityEvent event. Victim "+e1.getType().getName()); 
        
        if (damager instanceof Player){
        	if (e1 instanceof Hanging && !r.canBuild(damager)){
            	RPLang.sendMessage(damager, "entitylistener.region.cantinteract");
                e.setCancelled(true);
                return;
            }         
            if (e1 instanceof Player && r.flagExists("pvp") && !r.canPVP(damager)){
            	RPLang.sendMessage(damager, "entitylistener.region.cantpvp");
                e.setCancelled(true);
                return;
            }
        }
        
        //return if not player
    	if (!(e1 instanceof Player)){
    		return;
    	}

    	Player play = (Player) e.getTargetEntity();
    	
		if (RedProtect.tpWait.contains(play.getName())){
    		RedProtect.tpWait.remove(play.getName());
    		RPLang.sendMessage(play, RPLang.get("cmdmanager.region.tpcancelled"));
    	}
		
    	if (r != null && !r.canPlayerDamage()){
    		e.setCancelled(true);
    	}
    	
    	//execute on health
      	if (r.cmdOnHealth(play)){
      		RedProtect.logger.debug("player","Cmd on healt: true");
      	}
    	
    	if (!r.canDeath() && play.get(Keys.HEALTH).get() <= 1){
    		e.setCancelled(true);
    	} 
    	
        //deny damagecauses
        List<String> Causes = RedProtect.cfgs.getStringList("server-protection.deny-playerdeath-by");
        if(e.getCause().containsType(DamageSource.class) && Causes.size() > 0){
        	DamageType damagec = e.getCause().first(DamageSource.class).get().getType();
        	for (String cause:Causes){
        		if (damagec.getName().equalsIgnoreCase(cause)){
        			e.setCancelled(true);
        		}       		
        	}                    
        }        
    }
    
    @Listener
    public void onEntityDamageByEntityEvent(InteractEntityEvent.Primary e, @First Player p) {
        Entity e1 = e.getTargetEntity();        
        RedProtect.logger.debug("player","RPLayerListener: Is EntityDamageByEntityEvent event. Victim: "+e.getTargetEntity().getType().getName()); 
                        
        Location<World> l = e1.getLocation();
        Region r = RedProtect.rm.getTopRegion(l);
        if (r == null || p == null){
        	return;
        }
        
        if (e1 instanceof Player && r.flagExists("pvp") && !r.canPVP(p)){
        	RPLang.sendMessage(p, "entitylistener.region.cantpvp");
            e.setCancelled(true);
            return;
        }        
	}
    
    @Listener
    public void onPlayerMovement(MoveEntityEvent e){
    	if (!(e.getTargetEntity() instanceof Player) || RedProtect.cfgs.getBool("performance.disable-onPlayerMoveEvent-handler")) {
            return;
        }
    	
    	RedProtect.logger.debug("player", "PlayerMoveEvent - Entity name: "+e.getTargetEntity().getType().getName());    
    	
    	Player p = (Player) e.getTargetEntity();
    	    	
    	if (e.getFromTransform() != e.getToTransform() && RedProtect.tpWait.contains(p.getName())){
    		RedProtect.tpWait.remove(p.getName());
    		RPLang.sendMessage(p, "cmdmanager.region.tpcancelled");
    	}
    	 
    	Transform<World> lfromForm = e.getFromTransform();
    	Transform<World> ltoForm = e.getToTransform();
    	
    	Location<World> lfrom = e.getFromTransform().getLocation();
    	Location<World> lto = e.getToTransform().getLocation();
    	
    	
    	//teleport player to coord/world if playerup 128 y
    	int NetherY = RedProtect.cfgs.getInt("netherProtection.maxYsize");
    	if (lto.getExtent().getDimension().getType().equals(DimensionTypes.NETHER) && NetherY != -1 && lto.getBlockY() >= NetherY && !p.hasPermission("redprotect.bypass.nether-roof")){
    		for (String cmd:RedProtect.cfgs.getStringList("netherProtection.execute-cmd")){
        		RedProtect.game.getCommandManager().process(RedProtect.serv.getConsole(), cmd.replace("{player}", p.getName()));
    		}
    		RPLang.sendMessage(p, RPLang.get("playerlistener.upnethery").replace("{location}", NetherY+""));
    	}
    	
    	
        Region r = RedProtect.rm.getTopRegion(lto);
        
        /*
        //deny enter if no perm doors
    	String door = lto.getBlock().getType().getName();
    	if (r != null && (door.contains("DOOR") || door.contains("_GATE")) && !r.canDoor(p)){
    		if (RPDoor.isDoorClosed(p.getWorld().getBlockAt(lto))){
    			e.setCancelled(true);
    		}
    	}*/
    	World w = lfrom.getExtent();
    	
    	if (r != null){
    		
    		//enter max players flag
            if (r.maxPlayers() != -1){
            	if (!checkMaxPlayer(p, r)){
            		e.setToTransform(DenyEnterPlayer(w, lfromForm, ltoForm, p, r));
            		RPLang.sendMessage(p, RPLang.get("playerlistener.region.maxplayers").replace("{players}", String.valueOf(r.maxPlayers())));	
            	}
            } 
            
            //remove pots
            if (!r.allowEffects(p) && p.get(Keys.POTION_EFFECTS).isPresent()){
            	for (PotionEffect pot:p.get(Keys.POTION_EFFECTS).get()){
            		if (pot.getDuration() < 36000){
            			p.offer(Keys.POTION_EFFECTS, new ArrayList<PotionEffect>());
            		}           		
            	}            	
            }
            
            //Enter flag
            if (!r.canEnter(p)){
        		e.setToTransform(DenyEnterPlayer(w, lfromForm, ltoForm, p, r));
        		RPLang.sendMessage(p, "playerlistener.region.cantregionenter");			
        	}
            
            //Allow enter with items
            if (!r.canEnterWithItens(p)){
        		e.setToTransform(DenyEnterPlayer(w, lfromForm, ltoForm, p, r));
        		RPLang.sendMessage(p, RPLang.get("playerlistener.region.onlyenter.withitems").replace("{items}", r.flags.get("allow-enter-items").toString()));			
        	}
            
            //Deny enter with item
            if (!r.denyEnterWithItens(p)){
        		e.setToTransform(DenyEnterPlayer(w, lfromForm, ltoForm, p, r));
        		RPLang.sendMessage(p, RPLang.get("playerlistener.region.denyenter.withitems").replace("{items}", r.flags.get("deny-enter-items").toString()));			
        	}
            
            //Deny Fly
            if (!p.get(Keys.GAME_MODE).get().getName().equalsIgnoreCase("SPECTATOR") && !r.canFly(p) && p.get(Keys.IS_FLYING).get()){
            	p.offer(Keys.IS_FLYING, false);
        		//p.setAllowFlight(false);
        		RPLang.sendMessage(p, "playerlistener.region.cantfly");
        	} 
            
            //update region admin or leander visit
            if (RedProtect.cfgs.getString("region-settings.record-player-visit-method").equalsIgnoreCase("ON-REGION-ENTER")){
        		if (r.isLeader(p) || r.isAdmin(p)){
                	if (r.getDate() == null || (r.getDate() != RPUtil.DateNow())){
                		r.setDate(RPUtil.DateNow());
                	}        	
        		}
        	}
            
            if (Ownerslist.get(p) != r.getName()){ 
    			Region er = RedProtect.rm.getRegion(Ownerslist.get(p), p.getWorld());			
    			Ownerslist.put(p, r.getName());
    			
    			//Execute listener:
    			//EnterExitRegionEvent event = new EnterExitRegionEvent(er, r, p);
    			//Sponge.getPluginManager().callEvent(event);
    			if (e.isCancelled()){
    				return;
    			}
    			//--
    			RegionFlags(r, er, p);	
    			if (!r.getWelcome().equalsIgnoreCase("hide ")){
    				EnterExitNotify(r, p);
    			}        		
        	}
    	} else {
    		//if (r == null) >>
    		if (Ownerslist.get(p) != null) { 
    			Region er = RedProtect.rm.getRegion(Ownerslist.get(p), p.getWorld());    
    			if (Ownerslist.containsKey(p)){
            		Ownerslist.remove(p);
            	}
    			
    			//Execute listener:
    			//EnterExitRegionEvent event = new EnterExitRegionEvent(er, r, p);
    			//Bukkit.getPluginManager().callEvent(event);    			
    			if (e.isCancelled()){
    				return;
    			}
    			//---
    			noRegionFlags(er, p);    	
    			if (er != null && !er.getWelcome().equalsIgnoreCase("hide ") && RedProtect.cfgs.getBool("notify.region-exit")){
    				SendNotifyMsg(p, RPLang.get("playerlistener.region.wilderness"));
    			}    			
        	}   			
    	}  	
    }
    
	@Listener
    public void onPlayerTeleport(MoveEntityEvent.Teleport e, @First Player p){
    	TeleportType tcause = TeleportTypes.UNKNOWN;
    	if (e.getCause().containsType(TeleportCause.class)){
    		tcause = e.getCause().first(TeleportCause.class).get().getTeleportType();
    	}
    	
    	RedProtect.logger.debug("player","RPLayerListener: Is MoveEntityEvent.Teleport event. Player: "+p.getName()+", cause: "+tcause.getId()); 
    	
    	if (RedProtect.tpWait.contains(p.getName())){
    		RedProtect.tpWait.remove(p.getName());
    		RPLang.sendMessage(p, "cmdmanager.region.tpcancelled");
    	}
    	
    	Location<World> lfrom = e.getFromTransform().getLocation();
    	Location<World> lto = e.getToTransform().getLocation();
    	final Region rfrom = RedProtect.rm.getTopRegion(lfrom);
    	final Region rto = RedProtect.rm.getTopRegion(lto);
    	   	
    	Sponge.getScheduler().createAsyncExecutor(RedProtect.plugin).scheduleWithFixedDelay(new Runnable(){
			@Override
			public void run() {
				if (rto != null && rfrom != null){
		    		RegionFlags(rto, rfrom, p);    		
		    	}
		    	
		    	if (rto == null && rfrom != null){
		    		noRegionFlags(rfrom, p);
		    	}
		    	
		    	if (rfrom == null && rto != null){
		    		noRegionFlags(rto, p);
		    	}				
			}    		
    	}, 2, 2, TimeUnit.SECONDS);
    	
    	if (rto != null){    		    	
        	
    		//enter max players flag
            if (rto.maxPlayers() != -1){
            	if (!checkMaxPlayer(p, rto)){
            		RPLang.sendMessage(p, RPLang.get("playerlistener.region.maxplayers").replace("{players}", String.valueOf(rto.maxPlayers())));	
            		e.setCancelled(true); 
            	}
            } 
            
        	if (!rto.canEnter(p)){
        		RPLang.sendMessage(p, "playerlistener.region.cantregionenter");
        		e.setCancelled(true); 
        		return;
        	}
        	
        	//Allow enter with items
            if (!rto.canEnterWithItens(p)){
            	RPLang.sendMessage(p, RPLang.get("playerlistener.region.onlyenter.withitems").replace("{items}", rto.flags.get("allow-enter-items").toString()));	
        		e.setCancelled(true);	
        		return;
        	}
            
            //Deny enter with item
            if (!rto.denyEnterWithItens(p)){
            	RPLang.sendMessage(p, RPLang.get("playerlistener.region.denyenter.withitems").replace("{items}", rto.flags.get("deny-enter-items").toString()));
        		e.setCancelled(true);
        		return;		
        	}
            
        	if (PlayerCmd.containsKey(p)){
        		if (!rto.canBack(p) && PlayerCmd.get(p).startsWith("/back")){
            		RPLang.sendMessage(p, "playerlistener.region.cantback");
            		e.setCancelled(true);
            	}
        		if (!rto.AllowHome(p) && PlayerCmd.get(p).startsWith("/home")){
            		RPLang.sendMessage(p, "playerlistener.region.canthome");
            		e.setCancelled(true);
            	}
        		PlayerCmd.remove(p);    		
        	}
    	}
    	
    	
    	//teleport player to coord/world if playerup 128 y
    	int NetherY = RedProtect.cfgs.getInt("netherProtection.maxYsize");
    	if (lto.getExtent().getDimension().getType().equals(DimensionTypes.NETHER) && NetherY != -1 && lto.getBlockY() >= NetherY && !p.hasPermission("redprotect.bypass.nether-roof")){
    		RPLang.sendMessage(p, RPLang.get("playerlistener.upnethery").replace("{location}", NetherY+""));
    		e.setCancelled(true); 
    	}
    	
    	if (tcause.equals(TeleportTypes.ENTITY_TELEPORT)){
    		if (rfrom != null && !rfrom.canTeleport(p)){
        		RPLang.sendMessage(p, "playerlistener.region.cantuse");
                e.setCancelled(true);    		
        	}
        	if (rto != null && !rto.canTeleport(p)){
        		RPLang.sendMessage(p, "playerlistener.region.cantuse");
                e.setCancelled(true);    		
        	}
    	}   
    	
    	if (tcause.equals(TeleportTypes.PORTAL)){        	
        	if (rto != null && !rto.canExitPortal(p)){
        		RPLang.sendMessage(p, "playerlistener.region.cantteleport");
        		e.setCancelled(true);
        	}    
        	
        	if (rfrom != null && !rfrom.canEnterPortal(p)){
        		RPLang.sendMessage(p, "playerlistener.region.cantenterteleport");
        		e.setCancelled(true);
        	}
    	}
    }
    
	private boolean checkMaxPlayer(Player p, Region r) {  
    	if (r.canBuild(p)){
    		return true;
    	}
    	int ttl = 0;
    	for (Player onp:p.getWorld().getPlayers()){
    		if (onp == p){continue;}
        	Region reg = RedProtect.rm.getTopRegion(onp.getLocation());
        	if (reg != null && reg == r){
        		ttl++;
        	}
        }
    	if (ttl >= r.maxPlayers()){
    		return false;
    	}
		return true;
	}

    @Listener
    public void onPlayerCommand(SendCommandEvent e, @First Player p){
    	
    	if (RedProtect.tpWait.contains(p.getName())){
    		RedProtect.tpWait.remove(p.getName());
    		RPLang.sendMessage(p, "cmdmanager.region.tpcancelled");
    	}
    	
    	String cmd = e.getCommand();
    	    	
    	if (RedProtect.cfgs.getStringList("server-protection.deny-commands-on-worlds." + p.getWorld().getName()).contains(cmd) && !p.hasPermission("redprotect.bypass")){
    		RPLang.sendMessage(p, "playerlistener.command-notallowed");
    		e.setCancelled(true);
    		return;
    	}
    	
    	if (RedProtect.cfgs.hasGlobalKey(p.getWorld().getName(),"command-ranges",cmd.toLowerCase()) && !cmd.equals(".")){    		
    		double min = RedProtect.cfgs.getGlobalFlagDouble(p.getWorld().getName(),"command-ranges",cmd,"min-range");
    		double max = RedProtect.cfgs.getGlobalFlagDouble(p.getWorld().getName(),"command-ranges",cmd,"max-range");
    		String mesg = RedProtect.cfgs.getGlobalFlagString(p.getWorld().getName(),"command-ranges",cmd,"message");
    		double py = p.getLocation().getY();
    		if (py < min || py > max){
    			if (mesg != null && !mesg.equals("")){
    				RPLang.sendMessage(p, mesg);
    			}    			
    			e.setCancelled(true);
    			return;
    		}
    	}
    	    	
    	if (cmd.startsWith("back") || cmd.startsWith("home")){
    		PlayerCmd.put(p, cmd);
    	}
    	
       	Region r = RedProtect.rm.getTopRegion(p.getLocation());
       	if (r != null){
       		
           	if (!r.AllowCommands(p, cmd)){
           		if (cmd.startsWith("rp") || cmd.startsWith("redprotect")){
           			return;
           		}
           		RPLang.sendMessage(p, "playerlistener.region.cantcommand");
        		e.setCancelled(true);
        		return;
           	}
           	
        	if (!r.DenyCommands(p, cmd)){
           		if (cmd.startsWith("rp") || cmd.startsWith("redprotect")){
           			return;
           		}
           		RPLang.sendMessage(p, "playerlistener.region.cantcommand");
        		e.setCancelled(true);
        		return;
           	}
           	
        	if (cmd.startsWith("sethome") && !r.AllowHome(p)){
        		RPLang.sendMessage(p, "playerlistener.region.canthome");
        		e.setCancelled(true);
        		return;
        	} 
        	        	
       	}    	
    }     
        
    @Listener
    public void onPlayerHarvest(HarvestEntityEvent.TargetPlayer e){
    	RedProtect.logger.debug("player","RPLayerListener: Is HarvestEntityEvent");
    	
    	Player p = e.getTargetEntity();
    	Region r = RedProtect.rm.getTopRegion(p.getLocation());
    	
    	if (r != null){
    		if (r.keepInventory()){
        		e.setKeepsInventory(true);
        	}
        	if (r.keepLevels()){
        		e.setKeepsLevel(true);
        	}
    	}  
    }
    
    @Listener
    public void onPlayerDie(DestructEntityEvent.Death e){    	 
    	if (!(e.getTargetEntity() instanceof Player)){
    		return;
    	}
    	    	
    	RedProtect.logger.debug("player","RPLayerListener: Is DestructEntityEvent.Death");
    	
    	Player p = (Player) e.getTargetEntity();
    	
    	if (RedProtect.tpWait.contains(p.getName())){
    		RedProtect.tpWait.remove(p.getName());
    		RPLang.sendMessage(p, "cmdmanager.region.tpcancelled");
    	}    	
    }
        
    private Transform<World> DenyEnterPlayer(World wFrom, Transform<World> from, Transform<World> to, Player p, Region r) {
    	Location<World> setFrom = from.getLocation();
    	Location<World> setTo = to.getLocation();
    	for (int i = 0; i < r.getArea()+10; i++){
    		Region r1 = RedProtect.rm.getTopRegion(wFrom, setFrom.getBlockX()+i, setFrom.getBlockY(), setFrom.getBlockZ());
    		Region r2 = RedProtect.rm.getTopRegion(wFrom, setFrom.getBlockX()-i, setFrom.getBlockY(), setFrom.getBlockZ());
    		Region r3 = RedProtect.rm.getTopRegion(wFrom, setFrom.getBlockX(), setFrom.getBlockY(), setFrom.getBlockZ()+i);
    		Region r4 = RedProtect.rm.getTopRegion(wFrom, setFrom.getBlockX(), setFrom.getBlockY(), setFrom.getBlockZ()-i);
    		Region r5 = RedProtect.rm.getTopRegion(wFrom, setFrom.getBlockX()+i, setFrom.getBlockY(), setFrom.getBlockZ()+i);
    		Region r6 = RedProtect.rm.getTopRegion(wFrom, setFrom.getBlockX()-i, setFrom.getBlockY(), setFrom.getBlockZ()-i);
    		if (r1 != r){
    			setTo = setFrom.add(+i, 0, 0);
    			break;
    		} 
    		if (r2 != r){
    			setTo = setFrom.add(-i, 0, 0);
    			break;
    		} 
    		if (r3 != r){
    			setTo = setFrom.add(0, 0, +i);
    			break;
    		} 
    		if (r4 != r){
    			setTo = setFrom.add(0, 0, -i);
    			break;
    		} 
    		if (r5 != r){
    			setTo = setFrom.add(+i, 0, +i);
    			break;
    		} 
    		if (r6 != r){
    			setTo = setFrom.add(-i, 0, -i);
    			break;
    		} 
		}
    	return new Transform<World>(setTo).setRotation(to.getRotation());
	}
    
    @Listener
    public void onPortalCreate(ConstructPortalEvent e){
    	Region r = RedProtect.rm.getTopRegion(e.getPortalLocation());
		if (r != null && !r.canCreatePortal()){
			e.setCancelled(true);
		}   	
    }
    
	@Listener
    public void onPlayerLogout(ClientConnectionEvent.Disconnect e){
    	stopTaskPlayer(e.getTargetEntity());
    	if (RedProtect.tpWait.contains(e.getTargetEntity().getName())){
    		RedProtect.tpWait.remove(e.getTargetEntity().getName());
    	}
    }
    
    @Listener
    public void PlayerLogin(ClientConnectionEvent.Login e){
    	RedProtect.logger.debug("player","Is ClientConnectionEvent.Login event. Player "+e.getTargetUser().getName());
    	
    	User p = e.getTargetUser();
    	//Location<World> l = e.getFromTransform().getLocation();
    	//Adjust inside region
    	//e.setToTransform(new Transform<World>(new Location<World>(l.getExtent(), l.getBlockX(), l.getBlockY()+0.1, l.getBlockZ())));
    	
    	RedProtect.logger.debug("player","Is ClientConnectionEvent.Login event.");
    	/*
    	if (p.hasPermission("redprotect.update") && RedProtect.Update && !RedProtect.cfgs.getBool("update-check.auto-update")){
    		RPLang.sendMessage(p, "&bAn update is available for RedProtect: " + RedProtect.UptVersion);
    		RPLang.sendMessage(p, "&bUse /rp update to download and automatically install this update.");
    	}
    	*/
    	if (RedProtect.cfgs.getString("region-settings.record-player-visit-method").equalsIgnoreCase("ON-LOGIN")){    		
        	String uuid = p.getUniqueId().toString();
        	if (!RedProtect.OnlineMode){
        		uuid = p.getName().toLowerCase();
        	}
        	for (Region r:RedProtect.rm.getMemberRegions(uuid)){
        		if (r.getDate() == null || !r.getDate().equals(RPUtil.DateNow())){
        			r.setDate(RPUtil.DateNow());
        		}
        	}
    	}    	    	
    }
    
    @Listener
    public void PlayerTrownPotion(LaunchProjectileEvent e){ 
    	
    	Entity ent = e.getTargetEntity();    	
    	RedProtect.logger.debug("player","Is PotionSplashEvent event.");
        
    	Region r = RedProtect.rm.getTopRegion(ent.getLocation());    	        
        if (ent instanceof ThrownPotion){ 
        	
    		ThrownPotion potion = (ThrownPotion) e.getTargetEntity();
    		ProjectileSource thrower = potion.getShooter();    		
    		
    		if (thrower instanceof Player){
    			if (r != null && !r.usePotions((Player)thrower)){
            		RPLang.sendMessage((Player)thrower, "playerlistener.region.cantuse");
            		e.setCancelled(true);
            		return;
            	}
    		}
    		
    		List<PotionEffect> pottypes = potion.get(Keys.POTION_EFFECTS).get();
    		//deny potion
            List<String> Pots = RedProtect.cfgs.getStringList("server-protection.deny-potions");
    		for (PotionEffect t:pottypes){
    			if (Pots.size() > 0 && Pots.contains(t.getType().getName().toUpperCase())){
    				e.setCancelled(true);
    				if (thrower instanceof Player){
        				RPLang.sendMessage((Player)thrower, RPLang.get("playerlistener.denypotion"));
        			}
                    return;
                }
    		}            
    	}
    }
            
    public void SendNotifyMsg(Player p, String notify){
    	if (!notify.equals("")){
    		/*if (RedProtect.cfgs.getString("notify.region-enter-mode").equalsIgnoreCase("BOSSBAR")){
    			if (RedProtect.BossBar){
    				BossbarAPI.setMessage(p,notify);
    			} else {
    				p.sendMessage(RPUtil.toText(notify));
    			}
    		} */
    		if (RedProtect.cfgs.getString("notify.region-enter-mode").equalsIgnoreCase("CHAT")){
    			p.sendMessage(RPUtil.toText(notify));
    		}
    	}
    }

    public void SendWelcomeMsg(Player p, String wel){
		/*if (RedProtect.cfgs.getString("notify.welcome-mode").equalsIgnoreCase("BOSSBAR")){
			if (RedProtect.BossBar){
				BossbarAPI.setMessage(p,wel);
			} else {
				p.sendMessage(wel);
			}
		} */
		if (RedProtect.cfgs.getString("notify.welcome-mode").equalsIgnoreCase("CHAT")){
			p.sendMessage(RPUtil.toText(wel));
		}
    }
    
    private void stopTaskPlayer(Player p){
    	List<String> toremove = new ArrayList<String>();
    	for (String taskId:PlayertaskID.keySet()){
    		if (PlayertaskID.get(taskId).equals(p.getName())){
    			Sponge.getScheduler().getTaskById(UUID.fromString(taskId.split("_")[0])).get().cancel();  
    			toremove.add(taskId);    			
    		}    		  			
    	}
    	for (String remove:toremove){
    		PlayertaskID.remove(remove);
    		RedProtect.logger.debug("player","Removed task ID: " + remove + " for player " + p.getName());
    	}
    	toremove.clear();
    }
    
    private void EnterExitNotify(Region r, Player p){
    	if (!RedProtect.cfgs.getBool("notify.region-enter")){
    		return;
    	}
    	
    	if (r.flagExists("enter") && !r.canEnter(p)){
    		return;
    	}
    	
    	String ownerstring = "";
    	String m = "";
    	//Enter-Exit notifications    
        if (r.getWelcome().equals("")){
			if (RedProtect.cfgs.getString("notify.region-enter-mode").equalsIgnoreCase("CHAT")){
				for (int i = 0; i < r.getLeaders().size(); ++i) {
    				ownerstring = ownerstring + ", " + RPUtil.UUIDtoPlayer(r.getLeaders().get(i)); 
    	        }
				
				if (r.getLeaders().size() > 0) {
		            ownerstring = ownerstring.substring(2);
		        }
		        else {
		            ownerstring = "None";
		        }
    			m = RPLang.get("playerlistener.region.entered"); 
        		m = m.replace("{leaders}", ownerstring);
        		m = m.replace("{region}", r.getName());
			} 
			SendNotifyMsg(p, m);
		} else {
			SendWelcomeMsg(p, "&6" + r.getName() + ": &r" + r.getWelcome());
    		return;        			
		}
    }
    
    private void RegionFlags(final Region r, Region er, final Player p){  
    	
    	//enter Gamemode flag
    	if (r.flagExists("gamemode") && !p.hasPermission("redprotect.admin.flag.gamemode")){    		
    		p.offer(Keys.GAME_MODE, (GameMode)RPUtil.getRegistryFor(GameMode.class, r.getFlagString("gamemode")));
    	}
    	
    	//Exit gamemode
		if (er != null && er.flagExists("gamemode") && !p.hasPermission("redprotect.admin.flag.gamemode")){
			p.offer(Keys.GAME_MODE, p.getWorld().getProperties().getGameMode());
		}
		
		//Enter command as player
        if (r.flagExists("player-enter-command") && !p.hasPermission("redprotect.admin.flag.server-enter-command")){
        	String[] cmds = r.getFlagString("player-enter-command").split(",");
        	for (String cmd:cmds){
        		if (cmd.startsWith("/")){
            		cmd = cmd.substring(1);
            	}
        		RedProtect.game.getCommandManager().process(p, cmd.replace("{player}", p.getName()).replace("{region}", r.getName()));
        	}                	
        }
        
        //Enter command as console
        if (r.flagExists("server-enter-command") && !p.hasPermission("redprotect.admin.flag.server-enter-command")){
        	String[] cmds = r.getFlagString("server-enter-command").split(",");
        	for (String cmd:cmds){
        		if (cmd.startsWith("/")){
            		cmd = cmd.substring(1);
            	}
        		RedProtect.game.getCommandManager().process(RedProtect.serv.getConsole(), cmd.replace("{player}", p.getName()).replace("{region}", r.getName()));
        	}                	
        }
        
        //Enter MagicCarpet
        /*if (r.flagExists("allow-magiccarpet") && !r.getFlagBool("allow-magiccarpet") && RedProtect.Mc){
        	if (MagicCarpet.getCarpets().getCarpet(p) != null){
        		MagicCarpet.getCarpets().remove(p);
        		RPLang.sendMessage(p, "playerlistener.region.cantmc");
        	}        	
        }*/
        
        if (er != null){                	
        	//Exit effect
			if (er.flagExists("effects") && !p.hasPermission("redprotect.admin.flag.effects")){
				String[] effects = er.getFlagString("effects").split(",");
				for (String effect:effects){
					if (PlayertaskID.containsValue(p.getName())){						
						String eff = effect.split(" ")[0];
						/*String amplifier = effect.split(" ")[1];
						PotionEffect fulleffect = PotionEffect.builder()
								.particles(false)
								.potionType(RPUtil.getPotType(eff))
								.amplifier(Integer.parseInt(amplifier))
								.build();*/
						p.remove(Keys.POTION_EFFECTS);
						List<String> removeTasks = new ArrayList<String>();
						for (String taskId:PlayertaskID.keySet()){
							String id = taskId.split("_")[0];
							String ideff = id+"_"+eff+er.getName();
							if (PlayertaskID.containsKey(ideff) && PlayertaskID.get(ideff).equals(p.getName())){
								Sponge.getScheduler().getTaskById(UUID.fromString(id)).get().cancel();
								removeTasks.add(taskId);
								RedProtect.logger.debug("player","(RegionFlags-eff)Removed task ID: " + taskId + " for player " + p.getName());
							}
						}
						for (String key:removeTasks){
							PlayertaskID.remove(key);
						}
						removeTasks.clear();
					}					
				}
			} else
			//exit fly flag
	    	if (er.flagExists("forcefly") && !p.hasPermission("redprotect.admin.flag.forcefly") && (p.gameMode().get().equals(GameModes.SURVIVAL) || p.gameMode().get().equals(GameModes.ADVENTURE))){
	    		if (PlayertaskID.containsValue(p.getName())){
	    			if (r.flagExists("forcefly")){
	    				p.offer(Keys.CAN_FLY, r.getFlagBool("forcefly"));	    				
	    			} else {
	    				p.offer(Keys.CAN_FLY, false);	
	    			}	    			
					List<String> removeTasks = new ArrayList<String>();
					for (String taskId:PlayertaskID.keySet()){
						String id = taskId.split("_")[0];
						String ideff = id+"_"+"forcefly"+er.getName();
						if (PlayertaskID.containsKey(ideff) && PlayertaskID.get(ideff).equals(p.getName())){
							Sponge.getScheduler().getTaskById(UUID.fromString(id)).get().cancel();
							removeTasks.add(taskId);
							RedProtect.logger.debug("player","(RegionFlags fly)Removed task ID: " + taskId + " for player " + p.getName());
						}
					}
					for (String key:removeTasks){
						PlayertaskID.remove(key);
					}
					removeTasks.clear();
				}    		
	    	} else {
				stopTaskPlayer(p);
			}
			
        	//Exit command as player
            if (er.flagExists("player-exit-command") && !p.hasPermission("redprotect.admin.flag.player-exit-command")){
            	String[] cmds = er.getFlagString("player-exit-command").split(",");
            	for (String cmd:cmds){
            		if (cmd.startsWith("/")){
                		cmd = cmd.substring(1);
                	}
            		RedProtect.game.getCommandManager().process(p, cmd.replace("{player}", p.getName()).replace("{region}", er.getName()));
            	}                	
            }
            
            //Exit command as console
            if (er.flagExists("server-exit-command") && !p.hasPermission("redprotect.admin.flag.server-exit-command")){
            	String[] cmds = er.getFlagString("server-exit-command").split(",");
            	for (String cmd:cmds){
            		if (cmd.startsWith("/")){
                		cmd = cmd.substring(1);
                	}
            		RedProtect.game.getCommandManager().process(RedProtect.serv.getConsole(), cmd.replace("{player}", p.getName()).replace("{region}", er.getName()));
            	}                	
            }
        }
        
        //Enter effect
        if (r.flagExists("effects") && !p.hasPermission("redprotect.admin.flag.effects")){
  			String[] effects = r.getFlagString("effects").split(",");
  			for (String effect:effects){
  				String eff = effect.split(" ")[0];
  				String amplifier = effect.split(" ")[1];
  				PotionEffect fulleffect = PotionEffect.builder()
						.particles(false)
						.potionType((PotionEffectType)RPUtil.getRegistryFor(PotionEffectType.class, eff))
						.amplifier(Integer.parseInt(amplifier))
						.build();
  				String TaskId = Sponge.getScheduler().createAsyncExecutor(RedProtect.plugin).scheduleWithFixedDelay(new Runnable() { 
  					public void run() {
  						if (p.isOnline() && r.flagExists("effects")){
  							p.offer(Keys.POTION_EFFECTS, Arrays.asList(fulleffect)); 
  						} else {
							p.offer(Keys.CAN_FLY, false); 
							try {
								this.finalize();
							} catch (Throwable e) {
								RedProtect.logger.debug("player","Effects not finalized...");
							}							
						}  						
  						} 
  					},0, 20, TimeUnit.SECONDS).getTask().getUniqueId().toString();	
  				PlayertaskID.put(TaskId+"_"+eff+r.getName(), p.getName());
  				RedProtect.logger.debug("player","Added task ID: " + TaskId+"_"+eff + " for player " + p.getName());
  			}
  		}
        
      //enter fly flag
    	if (r.flagExists("forcefly") && !p.hasPermission("redprotect.admin.flag.forcefly") && (p.gameMode().get().equals(GameModes.SURVIVAL) || p.gameMode().get().equals(GameModes.ADVENTURE))){
    		p.offer(Keys.IS_FLYING, r.getFlagBool("forcefly")); 
    		String TaskId = Sponge.getScheduler().createAsyncExecutor(RedProtect.plugin).scheduleWithFixedDelay(new Runnable() { 
					public void run() {
						if (p.isOnline() && r.flagExists("forcefly")){
							p.offer(Keys.IS_FLYING, r.getFlagBool("forcefly")); 
						} else {
							p.offer(Keys.IS_FLYING, false); 
							try {
								this.finalize();
							} catch (Throwable e) {
								RedProtect.logger.debug("player","forcefly not finalized...");
							}							
						}
						} 
					},0, 80, TimeUnit.SECONDS).getTask().getUniqueId().toString();		
				PlayertaskID.put(TaskId+"_"+"forcefly"+r.getName(), p.getName());
				RedProtect.logger.debug("player","(RegionFlags fly)Added task ID: " + TaskId+"_"+"forcefly"+ " for player " + p.getName());
    	}
    }
        
    private void noRegionFlags(Region er, Player p){
    	if (er != null){
    		        	
    		//Exit gamemode
    		if (er.flagExists("gamemode") && !p.hasPermission("redprotect.admin.flag.gamemode")){
    			p.offer(Keys.GAME_MODE, p.getWorld().getProperties().getGameMode());
    		}
    		
			//Exit effect
			if (er.flagExists("effects") && !p.hasPermission("redprotect.admin.flag.effects")){
				String[] effects = er.getFlagString("effects").split(",");
				for (String effect:effects){
					if (PlayertaskID.containsValue(p.getName())){						
						String eff = effect.split(" ")[0];
						//String amplifier = effect.split(" ")[1];
						//PotionEffect fulleffect = new PotionEffect(PotionEffectType.getByName(eff), RedProtect.cfgs.getInt("flags-configuration.effects-duration")*20, Integer.parseInt(amplifier));
						p.remove(Keys.POTION_EFFECTS);
						List<String> removeTasks = new ArrayList<String>();
						for (String taskId:PlayertaskID.keySet()){
							String id = taskId.split("_")[0];
							String ideff = id+"_"+eff+er.getName();
							if (PlayertaskID.containsKey(ideff) && PlayertaskID.get(ideff).equals(p.getName())){
								Sponge.getScheduler().getTaskById(UUID.fromString(id)).get().cancel();
								removeTasks.add(taskId);
								RedProtect.logger.debug("player","(noRegionFlags eff)Removed task ID: " + taskId + " for effect " + effect);
							}
						}
						for (String key:removeTasks){
							PlayertaskID.remove(key);
						}
						removeTasks.clear();
					}
				}
			} else
			
			//exit fly flag
        	if (er.flagExists("forcefly") && !p.hasPermission("redprotect.admin.flag.forcefly") && (p.gameMode().get().equals(GameModes.SURVIVAL) || p.gameMode().get().equals(GameModes.ADVENTURE))){
        		if (PlayertaskID.containsValue(p.getName())){
        			p.offer(Keys.IS_FLYING, false);
    				List<String> removeTasks = new ArrayList<String>();
    				for (String taskId:PlayertaskID.keySet()){
    					String id = taskId.split("_")[0];
    					String ideff = id+"_"+"forcefly"+er.getName();
    					if (PlayertaskID.containsKey(ideff) && PlayertaskID.get(ideff).equals(p.getName())){
    						Sponge.getScheduler().getTaskById(UUID.fromString(id)).get().cancel();
    						removeTasks.add(taskId);
    						RedProtect.logger.debug("player","(noRegionFlags fly)Removed task ID: " + taskId + " for player " + p.getName());
    					}
    				}
    				for (String key:removeTasks){
    					PlayertaskID.remove(key);
    				}
    				removeTasks.clear();
    			}    		
        	} else {
				stopTaskPlayer(p);
			}
			
			//Exit command as player
            if (er.flagExists("player-exit-command") && !p.hasPermission("redprotect.admin.flag.player-exit-command")){
            	String[] cmds = er.getFlagString("player-exit-command").split(",");
            	for (String cmd:cmds){
            		if (cmd.startsWith("/")){
                		cmd = cmd.substring(1);
                	}
            		RedProtect.game.getCommandManager().process(p, cmd.replace("{player}", p.getName()));
            	}                	
            }
            
            //Exit command as console
            if (er.flagExists("server-exit-command") && !p.hasPermission("redprotect.admin.flag.server-exit-command")){
            	String[] cmds = er.getFlagString("server-exit-command").split(",");
            	for (String cmd:cmds){
            		if (cmd.startsWith("/")){
                		cmd = cmd.substring(1);
                	}
            		RedProtect.game.getCommandManager().process(RedProtect.serv.getConsole(), cmd.replace("{player}", p.getName()));
            	}                	
            }
		}
    }
        
    @Listener
	public void onHunger(HealEntityEvent e){
    	if (!(e.getTargetEntity() instanceof Player)){
    		return;
    	}
    	
    	Player p = (Player) e.getTargetEntity();
    	
    	Region r = RedProtect.rm.getTopRegion(p.getLocation());
    	if (r != null && !r.canHunger()){
    		e.setCancelled(true);
    	}
    }
    
    @Listener
    public void onItemPickup(CollideEntityEvent event, @Root Player p) {
    	RedProtect.logger.debug("player","Is CollideEntityEvent(ItemPickup) event.");
    	for (Entity ent:event.getEntities()){
    		if (!(ent instanceof Item)){
    			continue;
    		}    		
    		Region r = RedProtect.rm.getTopRegion(ent.getLocation());
    		if (r != null && !r.canPickup(p)){
    			event.setCancelled(true);
    			RPLang.sendMessage(p, "playerlistener.region.cantpickup");
    			return;
    		}
    	}
    }
    
    @Listener
    public void PlayerDropItemGui(DropItemEvent.Pre e, @Root Player p){
    	e.getDroppedItems().forEach(item -> {
    		if (RPUtil.isGuiItem(item.createStack())){
    			RPUtil.removeGuiItem(p);
    			e.setCancelled(true);
    		}
    	});
    }
    
    @Listener
    public void PlayerDropItem(DropItemEvent.Dispense e, @Root EntitySpawnCause cause){
    	if (!(cause.getEntity() instanceof Player)) {
            return;
        }
    	RedProtect.logger.debug("player","Is DropItemEvent.Dispense event.");
    	Player p = (Player) cause.getEntity();
    	
    	for (Entity ent:e.getEntities()){
    		Location<World> l = ent.getLocation();
    		Region r = RedProtect.rm.getTopRegion(l);
	    	
	    	if (r != null && !r.canDrop(p)){
	    		e.setCancelled(true);
	    		RPLang.sendMessage(p, "playerlistener.region.cantdrop");
	    	}
    	}    	
    }
    
    @Listener
    public void PlayerMoveInv(InteractInventoryEvent.Close e, @Root Player p){
    	RPUtil.removeGuiItem(p);
    }
}
