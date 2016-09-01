package br.net.fabiozumbi12.RedProtect.listeners;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TippedArrow;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

import br.net.fabiozumbi12.RedProtect.RPUtil;
import br.net.fabiozumbi12.RedProtect.RedProtect;
import br.net.fabiozumbi12.RedProtect.Region;
import br.net.fabiozumbi12.RedProtect.config.RPConfig;
import br.net.fabiozumbi12.RedProtect.config.RPLang;

public class RPMine19 implements Listener{

	public RPMine19(){
		RedProtect.logger.debug("Loaded RPMine19...");
	}
	
	@EventHandler(priority = EventPriority.LOW)
    public void onPlayerInteract(PlayerInteractEvent event) { 
		Player p = event.getPlayer();
		Block b = event.getClickedBlock();
		ItemStack itemInHand = event.getItem();
		
        Location l = null;
        
        if (b != null){
        	l = b.getLocation();
        	RedProtect.logger.debug("RPPlayerListener - Is PlayerInteractEvent event. The block is " + b.getType().name());
        } else {
        	l = p.getLocation();
        }
        
        if (RedProtect.tpWait.contains(p.getName())){
    		RedProtect.tpWait.remove(p.getName());
    		RPLang.sendMessage(p, "cmdmanager.region.tpcancelled");
    	}
        
        if (RPUtil.RemoveGuiItem(itemInHand)){ 
        	if (itemInHand.equals(p.getInventory().getItemInMainHand())){
    			p.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
    		} else if (itemInHand.equals(p.getInventory().getItemInOffHand())){
    			p.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
    		} 
        	event.setCancelled(true);
        	return;
        }
        
        if (itemInHand != null && (event.getAction().name().equals("RIGHT_CLICK_BLOCK") || b == null)){ 
    		Material hand = itemInHand.getType();
    		Region r = RedProtect.rm.getTopRegion(l);
    		if (r != null && hand.equals(Material.CHORUS_FRUIT) && !r.canTeleport(p)){
    			RPLang.sendMessage(p, "playerlistener.region.cantuse");
                event.setCancelled(true); 
                event.setUseItemInHand(Event.Result.DENY);
    			return;
    		}
        }
	}
	
	@EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent e){
    	if (e.isCancelled()) {
            return;
        }
    	
    	final Player p = e.getPlayer();
    	Location lfrom = e.getFrom();
    	Location lto = e.getTo();    	
    	
    	if (e.getCause().equals(PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT)){
    		final Region rfrom = RedProtect.rm.getTopRegion(lfrom);
        	final Region rto = RedProtect.rm.getTopRegion(lto);
        	
    		if (rfrom != null && !rfrom.canTeleport(p)){
        		RPLang.sendMessage(p, "playerlistener.region.cantuse");
                e.setCancelled(true);    		
        	}
        	if (rto != null && !rto.canTeleport(p)){
        		RPLang.sendMessage(p, "playerlistener.region.cantuse");
                e.setCancelled(true);    		
        	}
    	}
	}
	
	@EventHandler
	public void onShootBow(EntityShootBowEvent e){
		if (e.isCancelled() || !(e.getEntity() instanceof Player)){
			return;
		}
		
		Player p = (Player) e.getEntity();		
		Entity proj = e.getProjectile();
		List<String> Pots = RPConfig.getStringList("server-protection.deny-potions");
		
		if (proj != null && (proj instanceof TippedArrow)){
			TippedArrow arr = (TippedArrow) proj;
			if (Pots.contains(arr.getBasePotionData().getType().name())){
				RPLang.sendMessage(p, "playerlistener.denypotion");
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onLingerPotion(LingeringPotionSplashEvent e){
		if (!(e.getEntity().getShooter() instanceof Player)){
    		return;
    	}
    	
    	Player p = (Player)e.getEntity().getShooter();
    	Entity ent = e.getEntity();
    	
    	RedProtect.logger.debug("Is LingeringPotionSplashEvent event.");
        
    	Region r = RedProtect.rm.getTopRegion(ent.getLocation());
    	if (r != null && !r.allowPotions(p)){
    		RPLang.sendMessage(p, "playerlistener.region.cantuse");
    		e.setCancelled(true);
    		return;
    	}    
    	
    	if (RPUtil.denyPotion(e.getEntity().getItem())){
    		e.setCancelled(true);
			if (e.getEntity().getShooter() instanceof Player){
				RPLang.sendMessage((Player)e.getEntity().getShooter(), RPLang.get("playerlistener.denypotion"));
			}
    	}
	}
	
	@EventHandler
    public void onConsume(PlayerItemConsumeEvent e){
        if(e.getItem() == null){
            return;
        }
        
    	Player p = e.getPlayer();
        //deny potion
    	if (p == null){
    		return;
    	}
        
        Region r = RedProtect.rm.getTopRegion(p.getLocation());
        if (r != null && e.getItem().getType().equals(Material.CHORUS_FRUIT) && !r.canTeleport(p)){
        	RPLang.sendMessage(p, "playerlistener.region.cantuse");
        	e.setCancelled(true);
        }
    }
	
	@EventHandler
	public void onChangeBlock(EntityChangeBlockEvent e){	
		if (e.isCancelled()){
			return;
		}
		
		if (e.getEntity() instanceof Player){
			Player p = (Player) e.getEntity();    	
	    	Block b = e.getBlock();
	    	Region r = RedProtect.rm.getTopRegion(b.getLocation());
    		if (r != null && !r.canBuild(p)){
    			RPLang.sendMessage(p, "blocklistener.region.cantbreak");
        		e.setCancelled(true);
    		}
    	}
	}
}
