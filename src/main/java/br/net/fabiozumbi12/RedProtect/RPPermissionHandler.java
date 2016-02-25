package br.net.fabiozumbi12.RedProtect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import br.net.fabiozumbi12.RedProtect.config.RPConfig;

public class RPPermissionHandler{
      
    public boolean hasPerm(Player p, String perm) {
        return p != null && (p.hasPermission(perm) || p.isOp());
    }
    
    public boolean hasRegionPermMember(Player p, String s, Region poly) {
        return regionPermMember(p, s, poly);
    }
    
    public boolean hasRegionPermAdmin(Player p, String s, Region poly) {
        return regionPermAdmin(p, s, poly);
    }
    
    public boolean hasRegionPermAdmin(CommandSender sender, String s, Region poly) {
    	if (sender instanceof Player){
    		return regionPermAdmin((Player)sender, s, poly);
    	} else {
    		return true;
    	}
        
    }
    
    public boolean hasRegionPermLeader(Player p, String s, Region poly) {
        return regionPermLeader(p, s, poly);
    }
    
    public boolean hasGenPerm(Player p, String s) {
        return GeneralPermHandler(p, s);
    }

    public int getPlayerBlockLimit(Player p) {
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
    	if (limits.size() > 0){
    		limit = Collections.max(limits);
    	} 
		return limit;
    }
    
    private int ClaimLimitHandler(Player p){
    	int limit = RPConfig.getInt("region-settings.claim-amount");  
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
    	if (limits.size() > 0){
    		limit = Collections.max(limits);
    	}     	
		return limit;
    }
    
    private boolean regionPermLeader(Player p, String s, Region poly){
    	String adminperm = "redprotect.admin." + s;
        String userperm = "redprotect.own." + s;
        if (poly == null) {
            return this.hasPerm(p, adminperm) || this.hasPerm(p, userperm);
        }
        return this.hasPerm(p, adminperm) || (this.hasPerm(p, userperm) && (poly.isLeader(p)));
    }
    
    private boolean regionPermAdmin(Player p, String s, Region poly){
    	String adminperm = "redprotect.admin." + s;
        String userperm = "redprotect.own." + s;
        if (poly == null) {
            return this.hasPerm(p, adminperm) || this.hasPerm(p, userperm);
        }
        return this.hasPerm(p, adminperm) || (this.hasPerm(p, userperm) && (poly.isLeader(p) || poly.isAdmin(p)));
    }
    
    private boolean regionPermMember(Player p, String s, Region poly){
    	String adminperm = "redprotect.admin." + s;
        String userperm = "redprotect.own." + s;
        if (poly == null) {
            return this.hasPerm(p, adminperm) || this.hasPerm(p, userperm);
        }
        return this.hasPerm(p, adminperm) || (this.hasPerm(p, userperm) && (poly.isLeader(p) || poly.isAdmin(p) || poly.isMember(p)));
    }
    
    private boolean GeneralPermHandler(Player p, String s) {
        String adminperm = "redprotect.admin." + s;
        String userperm = "redprotect.own." + s;
        String normalperm = "redprotect." + s;
        return this.hasPerm(p, adminperm) || this.hasPerm(p, userperm) || this.hasPerm(p, normalperm);
    }
}
