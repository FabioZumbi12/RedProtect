package br.net.fabiozumbi12.RedProtect.Sponge.events;

import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import br.net.fabiozumbi12.RedProtect.Sponge.Region;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;

public class ChangeRegionFlagEvent extends AbstractEvent implements Cancellable, Event {
	private final Region region;
	private String flag;
	private Object value;
	private Cause cause;
	private boolean isCancelled = false;

	public ChangeRegionFlagEvent(Cause cause, Region region, String flag, Object value){
		this.region = region;
		this.flag = flag;
		this.value = value;
		this.cause = cause;
	}

	public Region getRegion(){
		return this.region;
	}

	public String getFlag() { return this.flag; }

	public Object getFlagValue() { return this.value; }

	public void setFlagValue(Object value) { this.value = value; }

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
		return cause;
	}

}
