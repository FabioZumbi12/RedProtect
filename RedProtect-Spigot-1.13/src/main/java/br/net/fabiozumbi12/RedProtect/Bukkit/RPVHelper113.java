package br.net.fabiozumbi12.RedProtect.Bukkit;

import org.bukkit.block.Block;
import org.bukkit.block.data.Openable;

public class RPVHelper113 implements RPVHelper {

    public void toggleDoor(Block b) {
        Openable openable = (Openable) b.getBlockData();
        openable.setOpen(!openable.isOpen());
        b.setBlockData(openable);
    }

    public boolean isOpenable(Block b) {
        return b.getBlockData() instanceof Openable;
    }
}
