package br.net.fabiozumbi12.RedProtect.Bukkit.events;

import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ChangeRegionFlagEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	private final Region region;
	private String flag;
	private Object value;
	private CommandSender cause;
	private boolean isCancelled = false;

	public ChangeRegionFlagEvent(CommandSender cause, Region region, String flag, Object value){
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

	public CommandSender getCause() {
		return cause;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
