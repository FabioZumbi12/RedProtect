package br.net.fabiozumbi12.RedProtect.Sponge.events;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;

import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import br.net.fabiozumbi12.RedProtect.Sponge.Region;

public class DeleteRegionEvent implements Cancellable, Event {
	private Region region;
	private Player player;
	private boolean isCancelled = false;
	
	public DeleteRegionEvent(Region region, Player p){
		this.region = region;
		this.player = p;
	}
	
	public Player getPlayer(){
		return this.player;
	}
	
	public Region getRegion(){
		return this.region;
	}
		
	@Override
	public boolean isCancelled() {
		return this.isCancelled;
	}

	@Override
	public void setCancelled(boolean arg0) {
		this.isCancelled = arg0;
	}

	@Override
	public Cause getCause() {
		return RedProtect.getPVHelper().getCause(player);
	}

}
