package br.net.fabiozumbi12.RedProtect.Sponge.events;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;
import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import br.net.fabiozumbi12.RedProtect.Sponge.Region;


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
public class EnterExitRegionEvent  implements Cancellable, Event{

	private Player player;
	private Region ExitedRegion;
	private Region EnteredRegion;
	private boolean cancelled;
	
	
	public EnterExitRegionEvent(Region ExitedRegion, Region EnteredRegion, Player player){
		this.player = player;
		this.ExitedRegion = ExitedRegion;
		this.EnteredRegion = EnteredRegion;
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

	@Override
	public Cause getCause() {
		return RedProtect.getPVHelper().getCause(player);
	}   
}
