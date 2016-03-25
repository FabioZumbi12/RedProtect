package br.net.fabiozumbi12.RedProtect.hooks;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import br.net.fabiozumbi12.RedProtect.RedProtect;
import br.net.fabiozumbi12.RedProtect.config.RPLang;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.internal.LocalWorldAdapter;
import com.sk89q.worldedit.regions.Region;

@SuppressWarnings("deprecation")
public class WEListener {

    private static HashMap<String, EditSession> eSessions = new HashMap<String, EditSession>();
	
	public static boolean undo(String rid){
		if (eSessions.containsKey(rid)){
			eSessions.get(rid).undo(eSessions.get(rid));
			return true;
		}
		return false;
	}
	
    public static void regenRegion(final String rid, final World w, final Location p1, final Location p2, int delay, final CommandSender sender) {
    	    	
    	Bukkit.getScheduler().scheduleSyncDelayedTask(RedProtect.plugin, new Runnable() { 
			public void run() {
				CuboidSelection csel = new CuboidSelection(w , p1, p2);
		    	Region wreg = null;
		    	try {
					wreg = csel.getRegionSelector().getRegion();
				} catch (IncompleteRegionException e1) {
					e1.printStackTrace();
				}
		    	
		    	EditSession esession = new EditSession(LocalWorldAdapter.adapt(wreg.getWorld()), -1);
		    	eSessions.put(rid, esession);
		    	
		    	if (sender != null){
		    		
	    			if (wreg.getWorld().regenerate(wreg, esession)){
	    				RPLang.sendMessage(sender,"&aRegion "+rid.split("@")[0]+" regenerated with success!");
	    			} else {
	    				RPLang.sendMessage(sender,"&cTheres an error when regen the region "+rid.split("@")[0]+"!");
	    			}
	    		} else {
	    			wreg.getWorld().regenerate(wreg, esession);
	    		}
		    	
				} 
			},delay); 
	}
}
