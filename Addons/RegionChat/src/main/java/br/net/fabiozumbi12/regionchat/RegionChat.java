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

package br.net.fabiozumbi12.regionchat;

import io.github.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import io.github.fabiozumbi12.RedProtect.Bukkit.Region;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class RegionChat extends JavaPlugin implements Listener, CommandExecutor {

    @Override
    public void onEnable() {
        // Register the flag
        RedProtect.get().getAPI().addFlag("chat", false, false);

        getServer().getPluginManager().registerEvents(this, this);
        getCommand("regionchat").setExecutor(this);

        getConfig().addDefault("chat.no-member", "&7[&8N&7]");
        getConfig().addDefault("chat.member", "&7[&aM&7]");
        getConfig().addDefault("chat.admin", "&7[&cA&7]");
        getConfig().addDefault("chat.leader", "&7[&4L&7]");
        getConfig().addDefault("chat.message", "&8{region}&r {member} &f{player}:&b{message}");

        getConfig().addDefault("config.allow-nonmember-chat", false);
        getConfig().addDefault("config.allowed-worlds", new ArrayList<String>());

        getConfig().addDefault("messages.noregion", "&cNo regions here to chat!");
        getConfig().addDefault("messages.nopermchat", "&cYou don't have permission to chat in this region!");
        getConfig().addDefault("messages.worldnotallowed", "&cRegion chat is not allowed in this world!");

        getConfig().options().header("""
                ---- Region Chat Configuration ----
                Description: This plugin its a RedProtect extension to change/make to allow player to chat with other players on same region
                Configurations:
                chat:
                - chat options: Placeholders to be used on chat

                config:
                - allow-nonmember-chat: false - Allow no members to chat on region chat
                - allowed-worlds: [] - Allowed worlds to use region chat

                messages:
                - messages: Messages to show on commands
                """
        );
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equals("reload") && sender.hasPermission("regionchat.cmd.reload")) {
            reloadConfig();
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c[&4RPChat&c] &aRegionChat reloaded with success!"));
            return true;
        }

        if (args.length >= 1 && sender instanceof Player player) {
            Location loc = player.getLocation();
            List<String> worlds = getConfig().getStringList("config.allowed-worlds");
            if (!worlds.isEmpty() && !worlds.contains(player.getWorld().getName())) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c[&4RPChat&c] " + getConfig().getString("messages.worldnotallowed")));
                return true;
            }

            Region region = RedProtect.get().getAPI().getLowPriorityRegion(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            if (region == null) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c[&4RPChat&c] " + getConfig().getString("messages.noregion")));
                return true;
            }

            if (!region.canBuild(player) && !region.getFlagBool("chat")) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c[&4RPChat&c] " + getConfig().getString("messages.nopermchat")));
                return true;
            }

            StringBuilder msg = new StringBuilder();
            for (String arg : args) {
                msg.append(" ").append(arg);
            }
            String message = getConfig().getString("chat.message")
                    .replace("{region}", region.getName())
                    .replace("{player}", player.getName())
                    .replace("{message}", ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', msg.toString())));

            if (region.isMember(player)) {
                message = message.replace("{member}", getConfig().getString("chat.member"));
            } else if (region.isAdmin(player)) {
                message = message.replace("{member}", getConfig().getString("chat.admin"));
            } else if (region.isLeader(player)) {
                message = message.replace("{member}", getConfig().getString("chat.leader"));
            } else {
                message = message.replace("{member}", getConfig().getString("chat.no-member"));
            }

            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));

            for (Player other : Bukkit.getOnlinePlayers()) {
                if (other.equals(player)) continue;

                Location loc2 = other.getLocation();
                Region regionOther = RedProtect.get().getAPI().getLowPriorityRegion(loc2.getWorld(), loc2.getBlockX(), loc2.getBlockY(), loc2.getBlockZ());
                if (region.equals(regionOther) && (region.canBuild(other) || region.getFlagBool("chat"))) {
                    other.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                }
            }
        }
        return true;
    }
}
