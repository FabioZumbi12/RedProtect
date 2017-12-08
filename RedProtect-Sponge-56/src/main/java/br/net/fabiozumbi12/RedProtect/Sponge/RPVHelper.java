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

import java.util.List;

public interface RPVHelper {
	Cause getCause(Player p);
	
	void closeInventory(Player p);
	
	void openInventory(Inventory inv, Player p);
	
	void setBlock(World w, Location<World> loc, BlockType type);
	
	void setBlock(Location<World> loc, BlockState block);
	
	void digBlock(Player p, ItemStack item, Vector3i loc);
	
	void removeBlock(Location<World> loc);
	
	boolean checkCause(Cause cause, String toCompare);
	
	boolean checkHorseOwner(Entity ent, Player owner);

	List<String> getAllEnchants();

	ItemStack offerEnchantment(ItemStack item);
}
