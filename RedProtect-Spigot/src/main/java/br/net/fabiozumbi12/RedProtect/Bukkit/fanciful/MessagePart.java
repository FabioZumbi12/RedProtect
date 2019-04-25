/*
 * Copyright (c) 2019 - @FabioZumbi12
 * Last Modified: 25/04/19 07:02
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

package br.net.fabiozumbi12.RedProtect.Bukkit.fanciful;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.gson.stream.JsonWriter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Internal class: Represents a component of a JSON-serializable {@link FancyMessage}.
 */
final class MessagePart implements JsonRepresentedObject, ConfigurationSerializable, Cloneable {

    static final BiMap<ChatColor, String> stylesToNames;

    static {
        ImmutableBiMap.Builder<ChatColor, String> builder = ImmutableBiMap.builder();
        for (final ChatColor style : ChatColor.values()) {
            if (!style.isFormat()) {
                continue;
            }

            String styleName;
            switch (style) {
                case MAGIC:
                    styleName = "obfuscated";
                    break;
                case UNDERLINE:
                    styleName = "underlined";
                    break;
                default:
                    styleName = style.name().toLowerCase();
                    break;
            }

            builder.put(style, styleName);
        }
        stylesToNames = builder.build();
    }

    static {
        ConfigurationSerialization.registerClass(MessagePart.class);
    }

    ChatColor color = ChatColor.WHITE;
    ArrayList<ChatColor> styles = new ArrayList<>();
    String clickActionName = null, clickActionData = null,
            hoverActionName = null;
    JsonRepresentedObject hoverActionData = null;
    TextualComponent text;
    String insertionData = null;
    ArrayList<JsonRepresentedObject> translationReplacements = new ArrayList<>();

    MessagePart(final TextualComponent text) {
        this.text = text;
    }

    MessagePart() {
        this.text = null;
    }

    @SuppressWarnings("unchecked")
    public static MessagePart deserialize(Map<String, Object> serialized) {
        MessagePart part = new MessagePart((TextualComponent) serialized.get("text"));
        part.styles = (ArrayList<ChatColor>) serialized.get("styles");
        part.color = ChatColor.getByChar(serialized.get("color").toString());
        part.hoverActionName = (String) serialized.get("hoverActionName");
        part.hoverActionData = (JsonRepresentedObject) serialized.get("hoverActionData");
        part.clickActionName = (String) serialized.get("clickActionName");
        part.clickActionData = (String) serialized.get("clickActionData");
        part.insertionData = (String) serialized.get("insertion");
        part.translationReplacements = (ArrayList<JsonRepresentedObject>) serialized.get("translationReplacements");
        return part;
    }

    boolean hasText() {
        return text != null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public MessagePart clone() throws CloneNotSupportedException {
        MessagePart obj = (MessagePart) super.clone();
        obj.styles = (ArrayList<ChatColor>) styles.clone();
        if (hoverActionData instanceof JsonString) {
            obj.hoverActionData = new JsonString(((JsonString) hoverActionData).getValue());
        } else if (hoverActionData instanceof FancyMessage) {
            obj.hoverActionData = ((FancyMessage) hoverActionData).clone();
        }
        obj.translationReplacements = (ArrayList<JsonRepresentedObject>) translationReplacements.clone();
        return obj;

    }

    public void writeJson(JsonWriter json) {
        try {
            json.beginObject();
            text.writeJson(json);
            json.name("color").value(color.name().toLowerCase());
            for (final ChatColor style : styles) {
                json.name(stylesToNames.get(style)).value(true);
            }
            if (clickActionName != null && clickActionData != null) {
                json.name("clickEvent")
                        .beginObject()
                        .name("action").value(clickActionName)
                        .name("value").value(clickActionData)
                        .endObject();
            }
            if (hoverActionName != null && hoverActionData != null) {
                json.name("hoverEvent")
                        .beginObject()
                        .name("action").value(hoverActionName)
                        .name("value");
                hoverActionData.writeJson(json);
                json.endObject();
            }
            if (insertionData != null) {
                json.name("insertion").value(insertionData);
            }
            if (translationReplacements.size() > 0 && TextualComponent.isTranslatableText(text)) {
                json.name("with").beginArray();
                for (JsonRepresentedObject obj : translationReplacements) {
                    obj.writeJson(json);
                }
                json.endArray();
            }
            json.endObject();
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.WARNING, "A problem occured during writing of JSON string", e);
        }
    }

    public Map<String, Object> serialize() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("text", text);
        map.put("styles", styles);
        map.put("color", color.getChar());
        map.put("hoverActionName", hoverActionName);
        map.put("hoverActionData", hoverActionData);
        map.put("clickActionName", clickActionName);
        map.put("clickActionData", clickActionData);
        map.put("insertion", insertionData);
        map.put("translationReplacements", translationReplacements);
        return map;
    }

}
