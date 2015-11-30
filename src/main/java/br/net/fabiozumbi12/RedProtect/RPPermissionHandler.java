package br.net.fabiozumbi12.RedProtect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

public class RPPermissionHandler{
      
    public boolean hasPerm(Player p, String perm) {
        return p != null && (p.hasPermission(perm) || p.isOp());
    }
    
    public boolean hasRegionPerm(Player p, String s, Region poly) {
        return regionPermHandler(p, s, poly);
    }
    
    public boolean hasHelpPerm(Player p, String s) {
        return HelpPermHandler(p, s);
    }

    public int getPlayerLimit(Player p) {
        return LimitHandler(p);
    }
    
    public int getPlayerClaimLimit(Player p) {
        return ClaimLimitHandler(p);
    }
    
    private int LimitHandler(Player p){
    	int limit = RPConfig.getInt("region-settings.limit-amount");
    	List<Integer> limits = new ArrayList<Integer>();
    	Set<PermissionAttachmentInfo> perms = p.getEffectivePermissions();
    	if (limit > 0){
    		if (!p.hasPermission("redprotect.limit.blocks.unlimited")){
    			for (PermissionAttachmentInfo perm:perms){    			
        			if (perm.getPermission().startsWith("redprotect.limit.blocks.")){
        				limits.add(Integer.parseInt(perm.getPermission().replaceAll("[^-?0-9]+", "")));    				
        			}  
        		}
    		} else {
    			return -1;
    		}
    	}
    	if (limits.size() > 1){
    		limit = Collections.max(limits);
    	} 
		return limit;
    }
    
    private int ClaimLimitHandler(Player p){
    	int limit = RPConfig.getInt("region-settings.claim-amount-per-world");  
    	List<Integer> limits = new ArrayList<Integer>();
    	Set<PermissionAttachmentInfo> perms = p.getEffectivePermissions();
    	if (limit > 0){
    		if (!p.hasPermission("redprotect.limit.claim.unlimited")){
    			for (PermissionAttachmentInfo perm:perms){
        			if (perm.getPermission().startsWith("redprotect.limit.claim.")){
        				limits.add(Integer.parseInt(perm.getPermission().replaceAll("[^-?0-9]+", "")));    				
        			}  
        		}
    		} else {
    			return -1;
    		}  		
    	}
    	if (limits.size() > 1){
    		limit = Collections.max(limits);
    	}     	
		return limit;
    }
    
    private boolean regionPermHandler(Player p, String s, Region poly){
    	String adminperm = "redprotect.admin." + s;
        String userperm = "redprotect.own." + s;
        if (poly == null) {
            return this.hasPerm(p, adminperm) || this.hasPerm(p, userperm);
        }
        return this.hasPerm(p, adminperm) || (this.hasPerm(p, userperm) && (poly.isOwner(p)));
    }
    
    private boolean HelpPermHandler(Player p, String s) {
        String adminperm = "redprotect.admin." + s;
        String userperm = "redprotect.own." + s;
        String normalperm = "redprotect." + s;
        return this.hasPerm(p, adminperm) || this.hasPerm(p, userperm) || this.hasPerm(p, normalperm);
    }
}
