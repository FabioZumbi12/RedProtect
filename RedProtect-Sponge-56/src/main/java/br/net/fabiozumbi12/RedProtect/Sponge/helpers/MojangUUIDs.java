/*
 *
 * Copyright (c) 2019 - @FabioZumbi12
 * Last Modified: 28/03/19 20:20
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
 *
 */

package br.net.fabiozumbi12.RedProtect.Sponge.helpers;

import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

public class MojangUUIDs {

    public static String getName(String UUID) {
        try {
            URL url = new URL("https://api.mojang.com/user/profiles/" + UUID.replaceAll("-", "") + "/names");
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String line = in.readLine();
            if (line == null) {
                return null;
            }
            JsonArray array = (JsonArray) new JsonParser().parse(line);
            HashMap<Long, String> names = new HashMap<>();
            String name = "";
            for (Object profile : array) {
                JsonObject jsonProfile = (JsonObject) profile;
                if (jsonProfile.has("changedToAt")) {
                    names.put(jsonProfile.get("changedToAt").getAsLong(), jsonProfile.get("name").getAsString());
                    continue;
                }
                name = jsonProfile.get("name").getAsString();
            }
            if (!names.isEmpty()) {
                Long key = Collections.max(names.keySet());
                return names.get(key);
            } else {
                return name;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String getUUID(String player) {
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + player);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String line = in.readLine();
            if (line == null) {
                return null;
            }
            JsonObject jsonProfile = (JsonObject) new JsonParser().parse(line);
            String name = jsonProfile.get("id").getAsString();
            return toUUID(name);
        } catch (Exception ex) {
            RedProtect.get().logger.warning("Mojang response: "+ex.getMessage());
        }
        return null;
    }

    private static String toUUID(String uuid) {
        return uuid.substring(0, 8) + "-" + uuid.substring(8, 12) + "-"
                + uuid.substring(12, 16) + "-" + uuid.substring(16, 20)
                + "-" + uuid.substring(20, 32);
    }

    public static String getName(UUID uuid) {
        return getName(uuid.toString());
    }

}