package br.net.fabiozumbi12.RedProtect.hooks;

import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;

public class RPProtocolLib {
	public static ItemStack removeAttributes(ItemStack item) {
	    if (!MinecraftReflection.isCraftItemStack(item)) {
	        item = MinecraftReflection.getBukkitItemStack(item);
	    }
	    NbtCompound compound = (NbtCompound) NbtFactory.fromItemTag(item);
	    compound.put(NbtFactory.ofList("AttributeModifiers"));
	    return item;
	}
}
