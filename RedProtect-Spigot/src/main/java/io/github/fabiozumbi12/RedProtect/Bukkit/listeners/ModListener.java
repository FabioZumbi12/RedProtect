/*
 * Copyright (c) 2012-2023 - @FabioZumbi12
 * Last Modified: 02/10/2023 18:03
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

package io.github.fabiozumbi12.RedProtect.Bukkit.listeners;

import io.github.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.nio.charset.StandardCharsets;

public class ModListener implements PluginMessageListener {

    private final RedProtect plugin;

    String ZIG = "5zig_Set";
    String BSM = "BSM";
    String MCBRAND = "MC|Brand";
    String SCHEMATICA = "schematica";
    String WDLINIT = "WDL|INIT";
    String WDLCONTROL = "WDL|CONTROL";

    public ModListener(RedProtect plugin) {
        this.plugin = plugin;

        try {
            // If newer versions
            Class.forName("org.bukkit.entity.Dolphin");

            ZIG = "the5zigmod:5zig_set";
            BSM = "bsm:settings";
            MCBRAND = "minecraft:brand";
            SCHEMATICA = "dev:null";
            WDLINIT = "wdl:init";
            WDLCONTROL = "wdl:control";
        } catch (ClassNotFoundException ignored) {
        }

        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, ZIG, this);
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, BSM, this);
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, MCBRAND, this);
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, SCHEMATICA, this);
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, WDLINIT, this);
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, ZIG);
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, BSM);
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, SCHEMATICA);
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, WDLCONTROL);

        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, "BungeeCord", this);
    }

    /* Packets */
    private static byte[] getSchematicaPayload() {
        final ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeByte(0);
        output.writeBoolean(false);
        output.writeBoolean(false);
        output.writeBoolean(false);
        return output.toByteArray();
    }

    public static byte[] createWDLPacket0() {
        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeInt(0);
        output.writeBoolean(false);
        return output.toByteArray();
    }

    public static byte[] createWDLPacket1() {
        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeInt(1);

        output.writeBoolean(false);
        output.writeInt(0);
        output.writeBoolean(false);
        output.writeBoolean(false);
        output.writeBoolean(false);
        output.writeBoolean(false);
        return output.toByteArray();
    }

    public void unload() {
        plugin.getServer().getMessenger().unregisterIncomingPluginChannel(plugin);
        plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin);
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] value) {
        if (channel.equalsIgnoreCase(MCBRAND)) {
            String brand = new String(value, StandardCharsets.UTF_8);
            if (plugin.getConfigManager().configRoot().server_protection.mods_permissions.get("fabric").block)
                denyFabric(player, brand);
            if (plugin.getConfigManager().configRoot().server_protection.mods_permissions.get("forge").block)
                denyForge(player, brand);
            if (plugin.getConfigManager().configRoot().server_protection.mods_permissions.get("liteloader").block)
                denyLiteLoader(player, brand);
            if (plugin.getConfigManager().configRoot().server_protection.mods_permissions.get("rift").block)
                denyRift(player, brand);
        }
        if (plugin.getConfigManager().configRoot().server_protection.mods_permissions.get("5zig").block)
            deny5Zig(player, channel);
        if (plugin.getConfigManager().configRoot().server_protection.mods_permissions.get("bettersprinting").block)
            denyBSM(player, channel);
        if (plugin.getConfigManager().configRoot().server_protection.mods_permissions.get("schematica").block)
            denySchematica(player);
        if (plugin.getConfigManager().configRoot().server_protection.mods_permissions.get("worlddownloader").block)
            denyWDL(player, channel);
    }

    /* Actions */
    private void denySchematica(Player player) {
        if (!player.hasPermission("redprotect.mods.schematica.bypass")) {
            player.sendPluginMessage(plugin, SCHEMATICA, getSchematicaPayload());
            //executeAction(player, "schematica");
        }
    }

    private void denyLitematica(Player player) {
        if (!player.hasPermission("redprotect.mods.litematica.bypass")) {
            //executeAction(player, "litematica");
        }
    }

    private void deny5Zig(Player player, String channel) {
        if (channel.equalsIgnoreCase(ZIG) && !player.hasPermission("redprotect.mods.5zig.bypass")) {
            /*
             * 0x1 = Potion HUD
             * 0x2 = Potion Indicator
             * 0x4 = Armor HUD
             * 0x8 = Saturation
             * 0x16 = Unused
             * 0x32 = Auto Reconnect
             */
            player.sendPluginMessage(plugin, channel, new byte[]{0x1 | 0x2 | 0x4 | 0x8 | 0x16 | 0x32});

            executeAction(player, "5zig");
        }
    }

    private void denyBSM(Player player, String channel) {
        if (channel.equalsIgnoreCase(BSM) && !player.hasPermission("redprotect.mods.bettersprinting.bypass")) {
            player.sendPluginMessage(plugin, channel, new byte[]{1});

            executeAction(player, "bettersprinting");
        }
    }

    private void denyFabric(Player player, String channel) {
        if (channel.contains("fabric") && !player.hasPermission("redprotect.mods.fabric.bypass")) {
            executeAction(player, "fabric");
        }
    }

    private void denyForge(Player player, String channel) {
        if ((channel.contains("fml") || channel.contains("forge")) && !player.hasPermission("redprotect.mods.forge.bypass")) {
            executeAction(player, "forge");
        }
    }

    private void denyLiteLoader(Player player, String channel) {
        if ((channel.equalsIgnoreCase("LiteLoader") || channel.contains("Lite")) && !player.hasPermission("redprotect.mods.liteloader.bypass")) {
            executeAction(player, "liteloader");
        }
    }

    private void denyRift(Player player, String channel) {
        if (channel.contains("rift") && !player.hasPermission("redprotect.mods.rift.bypass")) {
            executeAction(player, "rift");
        }
    }

    private void denyWDL(Player player, String channel) {
        if (channel.equalsIgnoreCase(WDLINIT) && !player.hasPermission("redprotect.mods.worlddownloader.bypass")) {
            byte[][] packets = new byte[2][];
            packets[0] = createWDLPacket0();
            packets[1] = createWDLPacket1();
            for (byte[] packet : packets) player.sendPluginMessage(plugin, WDLCONTROL, packet);

            executeAction(player, "worlddownloader");
        }
    }

    private void executeAction(Player player, String mod) {
        String action = plugin.getConfigManager().configRoot().server_protection.mods_permissions.get(mod).action;
        if (!action.isEmpty()) {
            action = action
                    .replace("{p}", player.getName())
                    .replace("{mod}", mod);
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), action);
        }
    }
}
