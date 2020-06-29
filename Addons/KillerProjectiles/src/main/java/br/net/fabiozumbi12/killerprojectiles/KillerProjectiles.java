package br.net.fabiozumbi12.killerprojectiles;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public final class KillerProjectiles extends JavaPlugin implements Listener, CommandExecutor {

    @Override
    public void onEnable() {
        // Register the flag
        RedProtect.get().getAPI().addFlag("killer-projectiles", false, false);

        getServer().getPluginManager().registerEvents(this, this);
        getCommand("killerprojectiles").setExecutor(this);

        getConfig().addDefault("projectile-damage", 0);
        getConfig().addDefault("allowed-types", Arrays.asList("SNOWBALL", "ARROW"));
        getConfig().options().header("" +
                "---- Killer Projectiles Configuration ----\n" +
                "Description: This plugin its a RedProtect extension to change/make projectiles do more damage on regions\n" +
                "Configurations:\n" +
                "- projectile-damage: 0 - The exact damage or percentage (add % after the number like 50%)\n" +
                "- allowed-types:\n  - SNOWBALL\n  - ARROW\n"
        );
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    @Override
    public void onDisable() {
        // Remove the flag
        RedProtect.get().getAPI().removeFlag("killer-projectiles", false);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equals("reload") && sender.hasPermission("killerprojectiles.cmd.reload")) {
            reloadConfig();
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "Killer Projectiles reloaded with success!"));
        }
        return true;
    }

    // For damageable projectiles
    @EventHandler(ignoreCancelled = true)
    public void onEntityDamagePlayer(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Projectile && event.getEntity() instanceof Player) {

            Projectile projectile = (Projectile) event.getDamager();
            Region r = RedProtect.get().rm.getTopRegion(projectile.getLocation());

            if (r != null && r.getFlagBool("killer-projectiles") && getConfig().getStringList("allowed-types").contains(projectile.getType().name())) {
                // Set damage from projectile to 0
                event.setDamage(0);
            }
        }
    }

    // For non damageable projectiles
    @EventHandler(ignoreCancelled = true)
    public void onDamagePlayer(ProjectileHitEvent event) {
        if (event.getHitEntity() instanceof Player) {

            Player player = (Player) event.getHitEntity();
            Projectile projectile = event.getEntity();
            Region r = RedProtect.get().rm.getTopRegion(projectile.getLocation());

            double damage;
            if (getConfig().getString("projectile-damage").endsWith("%")) {
                damage = (player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() / 100) * Double.valueOf(getConfig().getString("projectile-damage", "100%").replace("%", ""));
            } else {
                damage = getConfig().getInt("projectile-damage");
            }

            if (r != null && r.getFlagBool("killer-projectiles") && getConfig().getStringList("allowed-types").contains(projectile.getType().name())) {
                player.setHealth(damage);
            }
        }
    }
}
