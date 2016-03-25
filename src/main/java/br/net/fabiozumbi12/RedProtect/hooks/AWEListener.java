package br.net.fabiozumbi12.RedProtect.hooks;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.primesoft.asyncworldedit.AsyncWorldEditMain;
import org.primesoft.asyncworldedit.api.blockPlacer.IBlockPlacer;
import org.primesoft.asyncworldedit.api.blockPlacer.IBlockPlacerListener;
import org.primesoft.asyncworldedit.api.blockPlacer.IJobEntryListener;
import org.primesoft.asyncworldedit.blockPlacer.entries.JobEntry;
import org.primesoft.asyncworldedit.playerManager.PlayerEntry;
import org.primesoft.asyncworldedit.worldedit.AsyncEditSessionFactory;
import org.primesoft.asyncworldedit.worldedit.world.AsyncWorld;

import br.net.fabiozumbi12.RedProtect.RedProtect;
import br.net.fabiozumbi12.RedProtect.config.RPLang;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.regions.Region;

public class AWEListener {

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
		    	
		    	AsyncWorldEditMain aweMain = (AsyncWorldEditMain)Bukkit.getPluginManager().getPlugin("AsyncWorldEdit");	    		
	    		IBlockPlacer bPlacer = aweMain.getBlockPlacer();
	            final IJobEntryListener stateListener = new IJobEntryListener() {
	                @Override
	                public void jobStateChanged(JobEntry job) {
	                    String name = job.getName();
	                    RedProtect.logger.info("State: " + name + " of region " + rid + " - " + job.getStatus() + ": " + job.isTaskDone());
	                }
	            };

	            final IBlockPlacerListener listener = new IBlockPlacerListener() {
	                @Override
	                public void jobAdded(JobEntry job) {
	                    String name = job.getName();
	                    job.addStateChangedListener(stateListener);
	                    RedProtect.logger.warning("JobAdded: " + name + " of region " + rid + " - " + job.getStatus() + ": " + job.isTaskDone());
	                }

	                @Override
	                public void jobRemoved(JobEntry job) {
	                    String name = job.getName();
	                    job.addStateChangedListener(stateListener);
	                    RedProtect.logger.sucess("JobDone: " + name + " of region " + rid + " - " + job.getStatus() + ": " + job.isTaskDone());
	                }
	            };
	            
	            bPlacer.addListener(listener);
	    		AsyncEditSessionFactory factory = (AsyncEditSessionFactory) WorldEdit.getInstance().getEditSessionFactory();
	    		EditSession ess = factory.getEditSession(wreg.getWorld(), -1);
	    		eSessions.put(rid,ess);
	    		
                if (sender != null){
		    		
	    			if (AsyncWorld.wrap(wreg.getWorld(), new PlayerEntry("WorldEdit", UUID.randomUUID())).regenerate(wreg, ess)){
	    				RPLang.sendMessage(sender,"&aRegion "+rid.split("@")[0]+" regenerated with success!");
	    			} else {
	    				RPLang.sendMessage(sender,"&cTheres an error when regen the region "+rid.split("@")[0]+"!");
	    			}
	    		} else {
	    			AsyncWorld.wrap(wreg.getWorld(), new PlayerEntry("WorldEdit", UUID.randomUUID())).regenerate(wreg, ess);
	    		}
		    	
				} 
			},delay); 
	}
}
