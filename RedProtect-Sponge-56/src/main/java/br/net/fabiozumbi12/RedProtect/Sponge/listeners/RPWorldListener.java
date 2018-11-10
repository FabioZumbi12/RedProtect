package br.net.fabiozumbi12.RedProtect.Sponge.listeners;

import br.net.fabiozumbi12.RedProtect.Sponge.LogLevel;
import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.event.world.UnloadWorldEvent;
import org.spongepowered.api.world.World;

public class RPWorldListener {

    public RPWorldListener() {
        RedProtect.get().logger.debug(LogLevel.WORLD, "Loaded RPEntityListener...");
    }

    @Listener
    public void onWorldLoad(LoadWorldEvent e) {
        World w = e.getTargetWorld();
        try {
            RedProtect.get().rm.load(w);
            RedProtect.get().cfgs.loadPerWorlds(w);
            RedProtect.get().logger.warning("World loaded: " + w.getName());

        } catch (Exception ex) {
            RedProtect.get().logger.severe("redprotect problem on load world:");
            ex.printStackTrace();
        }
    }

    @Listener
    public void onWorldUnload(UnloadWorldEvent e) {
        World w = e.getTargetWorld();
        try {
            RedProtect.get().rm.unload(w);
            RedProtect.get().logger.warning("World unloaded: " + w.getName());
        } catch (Exception ex) {
            RedProtect.get().logger.severe("redprotect problem on unload world:");
            ex.printStackTrace();
        }
    }
}
