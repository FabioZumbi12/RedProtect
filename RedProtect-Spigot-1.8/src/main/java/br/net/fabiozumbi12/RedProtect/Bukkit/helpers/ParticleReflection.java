/*
 * Copyright (c) 2019 - @FabioZumbi12
 * Last Modified: 15/08/2019 14:16.
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

package br.net.fabiozumbi12.RedProtect.Bukkit.helpers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ParticleReflection {

    private static String serverVersion;
    private static Class craftPlayerClass;
    private static Method handleMethod;
    private static Field playerConnectionField;
    private static Class packetClass;
    private static Method sendPacketMethod;

    private static Class enumParticleClass;
    private static Method enumParticleByIdMethod;

    private static Class packetParticleClass;
    private static Constructor packetParticleConstructor;
    private static Field packetParticleTypeField;
    private static Field packetParticleXField;
    private static Field packetParticleYField;
    private static Field packetParticleZField;
    private static Field packetParticleOffsetXField;
    private static Field packetParticleOffsetYField;
    private static Field packetParticleOffsetZField;
    private static Field packetParticleSpeedField;
    private static Field packetParticleCountField;

    public static String getServerVersion() {
        if (serverVersion == null) {
            String pack = Bukkit.getServer().getClass().getPackage().getName();
            serverVersion = pack.substring(pack.lastIndexOf('.'))
                    .replaceFirst(".", "");
        }

        return serverVersion;
    }

    // Work just for <= 1.9
    public static int getServerVersionNumber() {
        return Integer.parseInt(Character.toString(getServerVersion().charAt(3)));
    }

    public static String getNMSClassName(String name) {
        return "net.minecraft.server." + getServerVersion() + "." + name;
    }

    public static String getCraftBukkitClassName(String name) {
        return "org.bukkit.craftbukkit." + getServerVersion() + "." + name;
    }

    public static Class getCraftPlayerClass() {
        if (craftPlayerClass == null) {
            try {
                craftPlayerClass = Class.forName(getCraftBukkitClassName("entity.CraftPlayer"));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        return craftPlayerClass;
    }

    public static Object getPlayerNMS(Player player) {
        try {
            if (handleMethod == null) {
                handleMethod = getCraftPlayerClass().getDeclaredMethod("getHandle");
            }

            handleMethod.setAccessible(true);
            return handleMethod.invoke(player);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object getPlayerConnection(Player player) {
        try {
            Object nms = getPlayerNMS(player);
            if (playerConnectionField == null) {
                playerConnectionField = nms.getClass().getDeclaredField("playerConnection");
            }

            playerConnectionField.setAccessible(true);
            return playerConnectionField.get(nms);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void sendPacket(Player player, Object packet) {
        try {
            Object connection = getPlayerConnection(player);
            if (sendPacketMethod == null) {
                packetClass = Class.forName(getNMSClassName("Packet"));
                sendPacketMethod = connection.getClass().getDeclaredMethod("sendPacket", packetClass);
            }

            sendPacketMethod.invoke(connection, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Class getEnumParticleClass() {
        try {
            if (enumParticleClass == null) {
                enumParticleClass = Class.forName(getNMSClassName("EnumParticle"));
            }

            return enumParticleClass;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object getParticleById(int id) {
        try {
            if (enumParticleByIdMethod == null) {
                enumParticleByIdMethod = getEnumParticleClass().getMethod("a", int.class);
            }

            return enumParticleByIdMethod.invoke(null, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object createParticlePacket(
            Particle particle,
            double x,
            double y,
            double z,
            int count,
            double offsetX,
            double offsetY,
            double offsetZ
    ) {
        try {
            if (packetParticleClass == null) {
                packetParticleClass = Class.forName(getNMSClassName("PacketPlayOutWorldParticles"));
            }

            if (packetParticleConstructor == null) {
                packetParticleConstructor = packetParticleClass.getConstructor();
                packetParticleTypeField = packetParticleClass.getDeclaredField("a");
                packetParticleXField = packetParticleClass.getDeclaredField("b");
                packetParticleYField = packetParticleClass.getDeclaredField("c");
                packetParticleZField = packetParticleClass.getDeclaredField("d");
                packetParticleOffsetXField = packetParticleClass.getDeclaredField("e");
                packetParticleOffsetYField = packetParticleClass.getDeclaredField("f");
                packetParticleOffsetZField = packetParticleClass.getDeclaredField("g");
                packetParticleSpeedField = packetParticleClass.getDeclaredField("h");
                packetParticleCountField = packetParticleClass.getDeclaredField("i");
            }

            Object packet = packetParticleConstructor.newInstance();

            if (getServerVersionNumber() >= 8) {
                setValue(packetParticleTypeField, packet, getParticleById(particle.getId()));
            } else {
                setValue(packetParticleTypeField, packet, particle.getClientName());
            }
            setValue(packetParticleXField, packet, (float) x);
            setValue(packetParticleYField, packet, (float) y);
            setValue(packetParticleZField, packet, (float) z);
            setValue(packetParticleOffsetXField, packet, (float) offsetX);
            setValue(packetParticleOffsetYField, packet, (float) offsetY);
            setValue(packetParticleOffsetZField, packet, (float) offsetZ);
            setValue(packetParticleSpeedField, packet, 0);
            setValue(packetParticleCountField, packet, count);

            return packet;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void setValue(Field field, Object instance, Object value) {
        field.setAccessible(true);
        try {
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
