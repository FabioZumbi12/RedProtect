package br.net.fabiozumbi12.RedProtect.hooks;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import br.net.fabiozumbi12.RedProtect.RedProtect;
import br.net.fabiozumbi12.RedProtect.Region;
import br.net.fabiozumbi12.RedProtect.config.RPLang;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.internal.LocalWorldAdapter;

@SuppressWarnings("deprecation")
public class WEListener {

    public static void regenRegion(final Region r, int delay, final CommandSender sender) {
    	    	
    	Bukkit.getScheduler().scheduleSyncDelayedTask(RedProtect.plugin, new Runnable() { 
			public void run() {
				CuboidSelection csel = new CuboidSelection(Bukkit.getWorld(r.getWorld()) ,r.getMaxLocation(), r.getMinLocation());
		    	com.sk89q.worldedit.regions.Region wreg = null;
		    	try {
					wreg = csel.getRegionSelector().getRegion();
				} catch (IncompleteRegionException e1) {
					e1.printStackTrace();
				}
		    	EditSession esession = new EditSession(LocalWorldAdapter.adapt(wreg.getWorld()), -1);
		    	Mask mask = esession.getMask();
		    	try {
		    		esession.setMask((Mask)null);		    		
		    		if (sender != null){
		    			if (wreg.getWorld().regenerate(wreg, esession)){
		    				RPLang.sendMessage(sender,"&aRegion "+r.getName()+" regenerated with success!");
		    			} else {
		    				RPLang.sendMessage(sender,"&cTheres an error when regen the region "+r.getName()+"!");
		    			}
		    		} else {
		    			wreg.getWorld().regenerate(wreg, esession);
		    		}
				} finally {
					esession.setMask(mask);
				}
				} 
			},delay); 
	}
}
