package br.net.fabiozumbi12.RedProtect.hooks;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.primesoft.asyncworldedit.AsyncWorldEditBukkit;
import org.primesoft.asyncworldedit.api.blockPlacer.IBlockPlacer;
import org.primesoft.asyncworldedit.api.blockPlacer.IBlockPlacerListener;
import org.primesoft.asyncworldedit.api.blockPlacer.entries.IJobEntry;
import org.primesoft.asyncworldedit.playerManager.PlayerManager;
import org.primesoft.asyncworldedit.worldedit.AsyncEditSessionFactory;
import org.primesoft.asyncworldedit.worldedit.world.AsyncWorld;

import br.net.fabiozumbi12.RedProtect.RPUtil;
import br.net.fabiozumbi12.RedProtect.RedProtect;
import br.net.fabiozumbi12.RedProtect.config.RPConfig;
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
					return;
				}
		    	
		    	AsyncWorldEditBukkit aweMain = (AsyncWorldEditBukkit)Bukkit.getPluginManager().getPlugin("AsyncWorldEdit");	    		
	    		IBlockPlacer bPlacer = aweMain.getBlockPlacer();
	    		/*
	            final IJobEntryListener stateListener = new IJobEntryListener() {
	                @Override
	                public void jobStateChanged(IJobEntry job) {
	                	if (job.getPlayer().getName().equals("redprotect")){
	                		String name = job.getName();
		                    RedProtect.logger.info("State: " + name + " of region " + r.getName() + " - " + job.getStatus() + ": " + job.isTaskDone());
	                	}	                    
	                }
	            };*/

	            final IBlockPlacerListener listener = new IBlockPlacerListener() {
	                @Override
	                public void jobAdded(IJobEntry job) {
	                	/*if (job.getPlayer().getName().equals("redprotect")){
	                		String name = job.getName();
		                    //job.addStateChangedListener(stateListener);
		                    RedProtect.logger.warning("JobAdded: " + name + " of region " + r.getName() + " - " + job.getStatus() + ": " + job.isTaskDone());
	                	}*/	                    
	                }

	                @Override
	                public void jobRemoved(IJobEntry job) {
	                	if (job.getPlayer().getName().equals("redprotect")){
	                		String name = job.getName();
		                    //job.addStateChangedListener(stateListener);
	                		if (RPConfig.getBool("purge.regen.awe-logs") && job.isTaskDone()){
	                			RedProtect.logger.sucess("JobDone: " + name + " of region " + r.getName() + " - " + job.getStatus() + ": " + job.getStatusString());
	                		}		                    
	                	}
	                	
	                }
	            };
	            
	            bPlacer.addListener(listener);
	    		AsyncEditSessionFactory factory = (AsyncEditSessionFactory) WorldEdit.getInstance().getEditSessionFactory();
	    		EditSession ess = factory.getEditSession(wreg.getWorld(), -1);
	    		eSessions.put(r.getID(),ess);
	    		int delayCount = 1+delay/10;
	    		
                if (sender != null){		    		
	    			if (AsyncWorld.wrap(wreg.getWorld(), new PlayerManager(aweMain).createFakePlayer("redprotect", UUID.randomUUID())).regenerate(wreg, ess)){
	    				RPLang.sendMessage(sender,"["+delayCount+"]"+" &aRegion "+r.getID().split("@")[0]+" regenerated with success!");
	    			} else {
	    				RPLang.sendMessage(sender,"["+delayCount+"]"+" &cTheres an error when regen the region "+r.getID().split("@")[0]+"!");
	    			}
	    		} else {
	    			if (AsyncWorld.wrap(wreg.getWorld(), new PlayerManager(aweMain).createFakePlayer("redprotect", UUID.randomUUID())).regenerate(wreg, ess)){
	    				RedProtect.logger.warning("["+delayCount+"]"+" &aRegion "+r.getID().split("@")[0]+" regenerated with success!");
	    			} else {
	    				RedProtect.logger.warning("["+delayCount+"]"+" &cTheres an error when regen the region "+r.getID().split("@")[0]+"!");
	    			}
	    		}
                
                if (remove){
		    		RedProtect.rm.remove(r);
		    	}
		    	
				} 
			},delay); 
	}
}
