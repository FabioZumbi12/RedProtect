package br.net.fabiozumbi12.redprotect;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;
import org.spongepowered.api.text.Text;

import br.net.fabiozumbi12.redprotect.config.RPLang;

public abstract class RegionBuilder{
	
    Region r;
    
    public RegionBuilder() {
        this.r = null;
    }
    
    public boolean ready() {
        return this.r != null;
    }
    
    public Region build() {
        return this.r;
    }
    
    void setErrorSign(ChangeSignEvent e, String error) {
    	SignData sign = e.getText();
    	sign = sign.set(sign.getValue(Keys.SIGN_LINES).get().set(0, RPUtil.toText(RPLang.get("regionbuilder.signerror"))));
        this.setError(e.getCause().first(Player.class).get(), error);
    }
    
    void setError(Player p, Text error) {
    	RPLang.sendMessage(p, RPLang.get("regionbuilder.error") + "(" + error + ")");
    }
    
    void setError(Player p, String error) {
    	RPLang.sendMessage(p, RPLang.get("regionbuilder.error") + "(" + error + ")");
    }
}
