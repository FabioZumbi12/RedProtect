package br.net.fabiozumbi12.RedProtect.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import br.net.fabiozumbi12.RedProtect.Region;

/**
 * This event is called when a player enter or exit a region.
 * <p>
 * The cancellable state cancels all flags and effects when
 * player enter/exit a region and nothing will happen, but
 * code can be added here and will work normally, and the flags
 * can be used too. Only default actions is cancelled.
 * @returns <b>null</b> if the ExitedRegion or EnteredRegion is wilderness.
 * 
 * @author FabioZumbi12
 *
 */
public class EnterExitRegionEvent extends Event implements Cancellable{

	private static final HandlerList handlers = new HandlerList();
	private Player player;
	private Region ExitedRegion;
	private Region EnteredRegion;
	private boolean cancelled;
	
	
	public EnterExitRegionEvent(Region ExitedRegion, Region EnteredRegion, Player player){
		this.player = player;
		this.ExitedRegion = ExitedRegion;
		this.EnteredRegion = EnteredRegion;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
        return handlers;
    }
	
	public Region getExitedRegion(){
		return this.ExitedRegion;
	}
	
	public Region getEnteredRegion(){
		return this.EnteredRegion;
	}
	
	public Player getPlayer(){
		return this.player;
	}

	@Override
	public boolean isCancelled() {
		return this.cancelled;
	}

	@Override
	public void setCancelled(boolean arg0) {
		this.cancelled = arg0;
		
	}

}
