package br.net.fabiozumbi12.RedProtect.Bukkit.listeners;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;

public class RPMine111{

	public static void sendBarMsg(String msg, String color, Player p) {
		BossBar bar = Bukkit.createBossBar(msg, BarColor.valueOf(color), BarStyle.SEGMENTED_10, new BarFlag[0]);
		bar.addPlayer(p);
		removeBar(bar, p);
	}

    private static void removeBar(final BossBar bar, final Player p){
    	final int task = Bukkit.getScheduler().scheduleSyncRepeatingTask(RedProtect.plugin, new Runnable(){
			@Override
			public void run() {
				double d = bar.getProgress();
				if (d >= 0.2){
					bar.setProgress(d - 0.2);
				}
			}					
		}, 20, 20);    	
    	Bukkit.getScheduler().runTaskLater(RedProtect.plugin, new Runnable(){
			@Override
			public void run() {
				bar.removePlayer(p);
				Bukkit.getScheduler().cancelTask(task);
			}					
		}, 120);
    }

}
