package br.net.fabiozumbi12.RedProtect.hooks;

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
import com.sk89q.worldedit.internal.LocalWorldAdapter;
import com.sk89q.worldedit.regions.Region;

@SuppressWarnings("deprecation")
public class WEListener {

    public static void regenRegion(final String rname, final World w, final Location p1, final Location p2, int delay, final CommandSender sender) {
    	    	
    	Bukkit.getScheduler().scheduleSyncDelayedTask(RedProtect.plugin, new Runnable() { 
			public void run() {
				CuboidSelection csel = new CuboidSelection(w , p1, p2);
		    	Region wreg = null;
		    	try {
					wreg = csel.getRegionSelector().getRegion();
				} catch (IncompleteRegionException e1) {
					e1.printStackTrace();
				}
		    	
		    	if (RedProtect.AWE){
		    		
		    		AsyncWorldEditMain aweMain = (AsyncWorldEditMain)Bukkit.getPluginManager().getPlugin("AsyncWorldEdit");	    		
		    		IBlockPlacer bPlacer = aweMain.getBlockPlacer();
		            final IJobEntryListener stateListener = new IJobEntryListener() {
		                @Override
		                public void jobStateChanged(JobEntry job) {
		                    String name = job.getName();
		                    RedProtect.logger.info("State: " + name + " of region " + rname + " - " + job.getStatus() + ": " + job.isTaskDone());
		                }
		            };

		            final IBlockPlacerListener listener = new IBlockPlacerListener() {
		                @Override
		                public void jobAdded(JobEntry job) {
		                    String name = job.getName();
		                    job.addStateChangedListener(stateListener);
		                    RedProtect.logger.warning("JobAdded: " + name + " of region " + rname + " - " + job.getStatus() + ": " + job.isTaskDone());
		                }

		                @Override
		                public void jobRemoved(JobEntry job) {
		                    String name = job.getName();
		                    job.addStateChangedListener(stateListener);
		                    RedProtect.logger.sucess("JobDone: " + name + " of region " + rname + " - " + job.getStatus() + ": " + job.isTaskDone());
		                }
		            };
		            
		            bPlacer.addListener(listener);
		    		AsyncEditSessionFactory factory = (AsyncEditSessionFactory) WorldEdit.getInstance().getEditSessionFactory();
		    		AsyncWorld.wrap(wreg.getWorld(), new PlayerEntry("WorldEdit", UUID.randomUUID())).regenerate(wreg, factory.getEditSession(wreg.getWorld(), -1));
		    				    		
			    } else {
			    	
			    	EditSession esession = new EditSession(LocalWorldAdapter.adapt(wreg.getWorld()), -1);
			    	if (sender != null){
		    			if (wreg.getWorld().regenerate(wreg, esession)){
		    				RPLang.sendMessage(sender,"&aRegion "+rname+" regenerated with success!");
		    			} else {
		    				RPLang.sendMessage(sender,"&cTheres an error when regen the region "+rname+"!");
		    			}
		    		} else {
		    			wreg.getWorld().regenerate(wreg, esession);
		    		}
		    	}	
		    	
				} 
			},delay); 
	}
}
