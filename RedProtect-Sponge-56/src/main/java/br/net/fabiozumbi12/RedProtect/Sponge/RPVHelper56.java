package br.net.fabiozumbi12.RedProtect.Sponge;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.animal.Horse;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3i;

public class RPVHelper56 implements RPVHelper {
	
	RPVHelper56(){}
	
	@Override
	public Cause getCause(Player p) {
		return Cause.of(NamedCause.simulated(p));
	}

	@Override
	public void closeInventory(Player p) {
		p.closeInventory(getCause(p));
	}

	@Override
	public void openInventory(Inventory inv, Player p) {
		p.openInventory(inv, Cause.of(NamedCause.of(p.getName(),p)));
	}

	@Override
	public void setBlock(World w, Location<World> loc, BlockType type) {
		w.setBlockType(loc.getBlockPosition(), type, Cause.of(NamedCause.owner(RedProtect.plugin)));
	}

	@Override
	public void setBlock(Location<World> loc, BlockState block) {
		loc.setBlock(block, Cause.of(NamedCause.owner(RedProtect.plugin)));  
	}

	@Override
	public void digBlock(Player p, ItemStack item, Vector3i loc) {
		p.getWorld().digBlockWith(loc, item, Cause.of(NamedCause.owner(RedProtect.plugin)));
	}

	@Override
	public void removeBlock(Location<World> loc) {
		loc.removeBlock(Cause.of(NamedCause.owner(RedProtect.plugin)));
	}

	@Override
	public boolean checkCause(Cause cause, String toCompare) {
		return cause.containsNamed(toCompare);
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean checkHorseOwner(Entity ent, Player p) {
		if (ent instanceof Horse && ((Horse)ent).getHorseData().get(Keys.TAMED_OWNER).isPresent()){
			Horse tam = (Horse) ent;
			Player owner = RedProtect.serv.getPlayer(tam.getHorseData().get(Keys.TAMED_OWNER).get().get()).get();
			if (owner != null && owner.getName().equals(p.getName())){
				return true;
			}
		}
		return false;
	}

	
}
