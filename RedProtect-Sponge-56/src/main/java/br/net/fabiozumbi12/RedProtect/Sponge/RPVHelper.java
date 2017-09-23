package br.net.fabiozumbi12.RedProtect.Sponge;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3i;

public interface RPVHelper {
	public Cause getCause(Player p);
	
	public void closeInventory(Player p);
	
	public void openInventory(Inventory inv, Player p);
	
	public void setBlock(World w, Location<World> loc, BlockType type);
	
	public void setBlock(Location<World> loc, BlockState block);
	
	public void digBlock(Player p, ItemStack item, Vector3i loc);
	
	public void removeBlock(Location<World> loc);
	
	public boolean checkCause(Cause cause, String toCompare);
	
	public boolean checkHorseOwner(Entity ent, Player owner);
}
