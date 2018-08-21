package br.net.fabiozumbi12.RedProtect.Bukkit;

import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPLang;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

public abstract class RegionBuilder {

    protected Region r;

    public RegionBuilder() {
        this.r = null;
    }

    public boolean ready() {
        return this.r != null;
    }

    public Region build() {
        return this.r;
    }

    protected void setErrorSign(SignChangeEvent e, String error) {
        e.setLine(0, RPLang.get("regionbuilder.signerror"));
        this.setError(e.getPlayer(), error);
    }

    protected void setError(Player p, String error) {
        RPLang.sendMessage(p, RPLang.get("regionbuilder.error") + "(" + error + ")");
    }
}
