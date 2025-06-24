/*
 * Copyright (c) 2012-2025 - @FabioZumbi12
 * Last Modified: 24/06/2025 19:02
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

package br.net.fabiozumbi12.RedProtect.Bukkit.listeners;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.nio.charset.StandardCharsets;

public class ModListener implements PluginMessageListener {

    private final RedProtect plugin;

    String MCBRAND = "MC|Brand";

    public ModListener(RedProtect plugin) {
        this.plugin = plugin;

        try {
            // If newer versions
            Class.forName("org.bukkit.entity.Dolphin");
            MCBRAND = "minecraft:brand";

            plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, MCBRAND, this);
            plugin.getConfigManager().configRoot().server_protection.mods_permissions.forEach((k,v) -> {
                if (v.registerPacket){
                    v.modId.forEach(i -> {
                        try{
                            plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, i, this);
                            plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, i);
                        } catch (Exception ex){
                            RedProtect.get().logger.severe("Error on register MOD packet listener: " + ex.getMessage());
                        }
                    });
                }
            });

            plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, "BungeeCord", this);
        } catch (ClassNotFoundException ex) {
            RedProtect.get().logger.severe("This server don't allow packet listener for MOD restrictions: " + ex.getMessage());
        }
    }

    public void unload() {
        plugin.getServer().getMessenger().unregisterIncomingPluginChannel(plugin);
        plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin);
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] value) {
        if (player != null && channel != null && channel.equalsIgnoreCase(MCBRAND)) {
            String brand = new String(value, StandardCharsets.UTF_8);
            plugin.getConfigManager().configRoot().server_protection.mods_permissions.forEach((k,v) -> {
                if (v.block){
                    v.modId.forEach(i ->{
                        if (brand.contains(i) && !player.hasPermission("redprotect.mods."+i+".bypass")) {
                            v.bytes.forEach(b -> player.sendPluginMessage(plugin, brand, b.getBytes(StandardCharsets.UTF_8)));
                            var act = v.action;
                            if (!act.isEmpty()) {
                                act = act
                                        .replace("{p}", player.getName())
                                        .replace("{mod}", i);
                                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), act);
                            }
                        } else if (channel.contains(i) && !player.hasPermission("redprotect.mods."+i+".bypass")) {
                            v.bytes.forEach(b -> player.sendPluginMessage(plugin, brand, b.getBytes(StandardCharsets.UTF_8)));
                            var act = v.action;
                            if (!act.isEmpty()) {
                                act = act
                                        .replace("{p}", player.getName())
                                        .replace("{mod}", i);
                                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), act);
                            }
                        }
                    });
                }
            });
        }
    }
}
