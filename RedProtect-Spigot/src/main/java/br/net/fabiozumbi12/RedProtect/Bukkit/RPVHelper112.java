package br.net.fabiozumbi12.RedProtect.Bukkit;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Openable;

public class RPVHelper112 implements RPVHelper {

    public void toggleDoor(Block b) {
        BlockState state = b.getState();
        Openable op = (Openable) state.getData();
        op.setOpen(!op.isOpen());
        state.setData((MaterialData) op);
        state.update();
    }

    public boolean isOpenable(Block b) {
        return b.getState().getData() instanceof Openable;
    }
}

