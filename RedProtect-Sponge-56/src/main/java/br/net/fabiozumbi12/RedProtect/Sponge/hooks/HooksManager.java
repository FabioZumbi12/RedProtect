package br.net.fabiozumbi12.RedProtect.Sponge.hooks;

import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import org.spongepowered.api.Sponge;

public class HooksManager {
    public DynmapHook dynmapHook;
    public boolean WE;
    public boolean Dyn;

    public void registerHooks(){
        WE = checkWE();
        Dyn = checkDM();

        if (WE){
            RedProtect.get().logger.info("WorldEdit found. Hooked.");
        }

        if (Dyn) {
            RedProtect.get().logger.info("Dynmap found. Hooked.");
            RedProtect.get().logger.info("Loading Dynmap markers...");
            try {
                dynmapHook = new DynmapHook();
                Sponge.getGame().getEventManager().registerListeners(RedProtect.get().container, dynmapHook);
            } catch (Exception e) {
                e.printStackTrace();
            }
            RedProtect.get().logger.info("Dynmap markers loaded!");
        }
    }

    private boolean checkWE() {
        return RedProtect.get().container.getDependencies().stream().anyMatch(d-> d.getId().equals("worldedit") &&
                Sponge.getPluginManager().getPlugin("worldedit").isPresent() &&
                Sponge.getPluginManager().getPlugin("worldedit").get().getVersion().get().startsWith("6.1.9"));
    }

    private boolean checkDM() {
        return Sponge.getPluginManager().getPlugin("dynmap").isPresent();
    }
}
