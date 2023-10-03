/*
 * Copyright (c) 2012-2023 - @FabioZumbi12
 * Last Modified: 10/05/2023 14:49
 *
 * This class is provided 'as-is', without any express or implied warranty. In no event will the authors be held liable for any
 *  damages arising from the use of this class.
 *
 * Permission is granted to anyone to use this class for any purpose, including commercial plugins, and to alter it and
 * redistribute it freely, subject to the following restrictions:
 * 1 - The origin of this class must not be misrepresented; you must not claim that you wrote the original software. If you
 * use this class in other plugins, an acknowledgment in the plugin documentation would be appreciated but is not required.
 * 2 - Altered source versions must be plainly marked as such, and must not be misrepresented as being the original class.
 * 3 - This notice may not be removed or altered from any source distribution.
 *
 * Esta classe é fornecida "como está", sem qualquer garantia expressa ou implícita. Em nenhum caso os autores serão
 * responsabilizados por quaisquer danos decorrentes do uso desta classe.
 *
 * É concedida permissão a qualquer pessoa para usar esta classe para qualquer finalidade, incluindo plugins pagos, e para
 * alterá-lo e redistribuí-lo livremente, sujeito às seguintes restrições:
 * 1 - A origem desta classe não deve ser deturpada; você não deve afirmar que escreveu a classe original. Se você usar esta
 *  classe em um plugin, uma confirmação de autoria na documentação do plugin será apreciada, mas não é necessária.
 * 2 - Versões de origem alteradas devem ser claramente marcadas como tal e não devem ser deturpadas como sendo a
 * classe original.
 * 3 - Este aviso não pode ser removido ou alterado de qualquer distribuição de origem.
 */

package br.net.fabiozumbi12.killerprojectiles;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
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
        getConfig().options().header("""
                ---- Killer Projectiles Configuration ----
                Description: This plugin its a RedProtect extension to change/make projectiles do more damage on regions
                Configurations:
                - projectile-damage: 0 - The exact damage or percentage (add % after the number like 50%)
                - allowed-types:
                  - SNOWBALL
                  - ARROW
                """
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
        if (event.getDamager() instanceof Projectile projectile && event.getEntity() instanceof Player) {

            Region r = RedProtect.get().getAPI().getRegion(projectile.getLocation());

            if (r != null && r.getFlagBool("killer-projectiles") && getConfig().getStringList("allowed-types").contains(projectile.getType().name())) {
                // Set damage from projectile to 0
                event.setDamage(0);
            }
        }
    }

    // For non damageable projectiles
    @EventHandler(ignoreCancelled = true)
    public void onDamagePlayer(ProjectileHitEvent event) {
        if (event.getHitEntity() instanceof Player player) {

            if (!player.getGameMode().equals(GameMode.SURVIVAL) && !player.getGameMode().equals(GameMode.ADVENTURE)) {
                return;
            }

            Projectile projectile = event.getEntity();
            Region r = RedProtect.get().getAPI().getRegion(projectile.getLocation());

            if (r != null && r.getFlagBool("killer-projectiles") && getConfig().getStringList("allowed-types").contains(projectile.getType().name())) {
                double damage;
                if (getConfig().getString("projectile-damage").endsWith("%")) {
                    damage = (player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() / 100) * Double.parseDouble(getConfig().getString("projectile-damage", "100%").replace("%", ""));
                } else {
                    damage = getConfig().getInt("projectile-damage");
                }
                double dmgPlayer = player.getHealth() - damage;
                if (dmgPlayer < 0)
                    dmgPlayer = 0;

                player.setHealth(dmgPlayer);
            }
        }
    }
}
