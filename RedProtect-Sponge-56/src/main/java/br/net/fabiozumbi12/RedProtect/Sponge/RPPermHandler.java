package br.net.fabiozumbi12.RedProtect.Sponge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;

public class RPPermHandler{
	
	public boolean hasPermOrBypass(Player p, String perm){
		return p.hasPermission(perm) || p.hasPermission(perm+".bypass");
	}
	
	public boolean hasPerm(Player p, String perm) {
        return p != null && (p.hasPermission(perm) || p.hasPermission("redprotect.admin"));
    }
    
	public boolean hasPerm(User p, String perm) {
        return p != null && (p.hasPermission(perm) || p.hasPermission("redprotect.admin"));
    }
	
    public boolean hasRegionPermMember(Player p, String s, Region poly) {
        return regionPermMember(p, s, poly);
    }
    
    public boolean hasRegionPermAdmin(Player p, String s, Region poly) {
        return regionPermAdmin(p, s, poly);
    }
    
    public boolean hasRegionPermAdmin(CommandSource sender, String s, Region poly) {
		return !(sender instanceof Player) || regionPermAdmin((Player) sender, s, poly);
	}
    
    public boolean hasRegionPermLeader(Player p, String s, Region poly) {
        return regionPermLeader(p, s, poly);
    }
    
    public boolean hasGenPerm(Player p, String s) {
        return GeneralPermHandler(p, s);
    }
    
    public int getPlayerBlockLimit(User p) {
        return LimitHandler(p);
    }
    
    public int getPlayerClaimLimit(User p) {
        return ClaimLimitHandler(p);
    }
    
    private int LimitHandler(User p){
    	int limit = RedProtect.cfgs.getInt("region-settings.limit-amount");
    	List<Integer> limits = new ArrayList<>();
    	if (limit > 0){
    		if (!p.hasPermission("redprotect.limit.blocks.unlimited")){
    			for (String perm:RedProtect.cfgs.getStringList("permissions-limits.permissions.blocks")){
    				RedProtect.logger.debug("default","Perm: "+perm);
    				if (p.hasPermission(perm)){
    					RedProtect.logger.debug("default","Has block perm: "+perm);
						String pStr = perm.replaceAll("[^-?0-9]+", "");
						if (!pStr.isEmpty()){
							limits.add(Integer.parseInt(pStr));
						}
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
    
    private int ClaimLimitHandler(User p){
    	int limit = RedProtect.cfgs.getInt("region-settings.claim-amount");  
    	List<Integer> limits = new ArrayList<>();
    	if (limit > 0){
    		if (!p.hasPermission("redprotect.limit.claim.unlimited")){
    			for (String perm:RedProtect.cfgs.getStringList("permissions-limits.permissions.claims")){
    				RedProtect.logger.debug("default","Perm: "+perm);
    				if (p.hasPermission(perm)){
    					RedProtect.logger.debug("default","Has claim perm: "+perm);
						String pStr = perm.replaceAll("[^-?0-9]+", "");
						if (!pStr.isEmpty()){
							limits.add(Integer.parseInt(pStr));
						}
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
        return this.hasPerm(p, adminperm) || ((this.hasPerm(p, "redprotect.user") || this.hasPerm(p, userperm)) && poly.isLeader(p));
    }
    
    private boolean regionPermAdmin(Player p, String s, Region poly){
    	String adminperm = "redprotect.admin." + s;
        String userperm = "redprotect.own." + s;        
        if (poly == null) {
            return this.hasPerm(p, adminperm) || this.hasPerm(p, userperm);
        }
        return this.hasPerm(p, adminperm) || ((this.hasPerm(p, "redprotect.user") || this.hasPerm(p, userperm)) && (poly.isLeader(p) || poly.isAdmin(p)));
    }
    
    private boolean regionPermMember(Player p, String s, Region poly){
    	String adminperm = "redprotect.admin." + s;
        String userperm = "redprotect.own." + s;
        if (poly == null) {
            return this.hasPerm(p, adminperm) || this.hasPerm(p, userperm);
        }
        return this.hasPerm(p, adminperm) || ((this.hasPerm(p, "redprotect.user") || this.hasPerm(p, userperm))  && (poly.isLeader(p) || poly.isAdmin(p) || poly.isMember(p)));
    }
    
    private boolean GeneralPermHandler(Player p, String s) {
        String adminperm = "redprotect.admin." + s;
        String userperm = "redprotect.own." + s;
        String normalperm = "redprotect." + s;
        return this.hasPerm(p, s) || this.hasPerm(p, adminperm) || this.hasPerm(p, userperm) || this.hasPerm(p, normalperm);
    }

	public boolean hasUserPerm(Player player, String string) {		
		return this.hasPerm(player, "redprotect.user") || this.GeneralPermHandler(player, string);
	}

	public boolean hasFlagPerm(Player player, String string) {
		return this.hasPerm(player, "redprotect.flag.all") || this.hasUserPerm(player, string);
	}

	public boolean hasAdminFlagPerm(Player player, String string) {
		return this.hasPerm(player, "redprotect.flag.special") || this.GeneralPermHandler(player, string);
	}
}
