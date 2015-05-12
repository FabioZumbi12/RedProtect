package br.net.fabiozumbi12.RedProtect;

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
    
    private int LimitHandler(Player p){
    	int limit = RPConfig.getInt("limit-amount");   	
    	Set<PermissionAttachmentInfo> perms = p.getEffectivePermissions();
    	if (limit > 0 && !p.hasPermission("redprotect.unlimited")){
    		for (PermissionAttachmentInfo perm:perms){
    			if (perm.getPermission().startsWith("redprotect.limit.amount.")){
        			limit = Integer.parseInt(perm.getPermission().replaceAll("[^-?0-9]+", ""));    				
    			}  
    		}
    	}
		return limit;
    }
    
    private boolean regionPermHandler(Player p, String s, Region poly){
    	String puuid = p.getUniqueId().toString();
    	if (!RedProtect.OnlineMode){
    		puuid = p.getName().toLowerCase();
    	} 
    	String adminperm = "redprotect.admin." + s;
        String userperm = "redprotect.own." + s;
        if (poly == null) {
            return this.hasPerm(p, adminperm) || this.hasPerm(p, userperm);
        }
        return this.hasPerm(p, adminperm) || (this.hasPerm(p, userperm) && (poly.isOwner(puuid)));
    }
    
    private boolean HelpPermHandler(Player p, String s) {
        String adminperm = "redprotect.admin." + s;
        String userperm = "redprotect.own." + s;
        String normalperm = "redprotect." + s;
        return this.hasPerm(p, adminperm) || this.hasPerm(p, userperm) || this.hasPerm(p, normalperm);
    }
}
