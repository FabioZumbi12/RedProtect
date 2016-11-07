package br.net.fabiozumbi12.RedProtect.hooks;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import br.net.fabiozumbi12.RedProtect.RPUtil;
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
	
    public static void regenRegion(final br.net.fabiozumbi12.RedProtect.Region r, final World w, final Location p1, final Location p2, final int delay, final CommandSender sender, final boolean remove) {
    	    	
    	Bukkit.getScheduler().scheduleSyncDelayedTask(RedProtect.plugin, new Runnable() { 
			public void run() {
				if (RPUtil.stopRegen){
					return;
				}
				CuboidSelection csel = new CuboidSelection(w , p1, p2);
		    	Region wreg = null;
		    	try {
					wreg = csel.getRegionSelector().getRegion();
				} catch (IncompleteRegionException e1) {
					e1.printStackTrace();
				}
		    	
		    	EditSession esession = new EditSession(LocalWorldAdapter.adapt(wreg.getWorld()), -1);
		    	eSessions.put(r.getID(), esession);
		    	int delayCount = 1+delay/10;
		    	
		    	if (sender != null){
	    			if (wreg.getWorld().regenerate(wreg, esession)){
	    				RPLang.sendMessage(sender,"["+delayCount+"]"+" &aRegion "+r.getID().split("@")[0]+" regenerated with success!");
	    			} else {
	    				RPLang.sendMessage(sender,"["+delayCount+"]"+" &cTheres an error when regen the region "+r.getID().split("@")[0]+"!");
	    			}
	    		} else {
	    			if (wreg.getWorld().regenerate(wreg, esession)){
	    				RedProtect.logger.warning("["+delayCount+"]"+" &aRegion "+r.getID().split("@")[0]+" regenerated with success!");
	    			} else {
	    				RedProtect.logger.warning("["+delayCount+"]"+" &cTheres an error when regen the region "+r.getID().split("@")[0]+"!");
	    			}
	    		}
		    	
		    	if (remove){
		    		RedProtect.rm.remove(r, RedProtect.serv.getWorld(r.getWorld()));
		    	}		    	
		    	
				} 
			},delay); 
	}
}
