/*
 Copyright @FabioZumbi12

 This class is provided 'as-is', without any express or implied warranty. In no event will the authors be held liable for any
  damages arising from the use of this software.

 Permission is granted to anyone to use this class for any purpose, including commercial plugins, and to alter it and
 redistribute it freely, subject to the following restrictions:
 1 - The origin of this class must not be misrepresented; you must not claim that you wrote the original software. If you
 use this class in other plugins, an acknowledgment in the plugin documentation would be appreciated but is not required.
 2 - Altered source versions must be plainly marked as such, and must not be misrepresented as being the original class.
 3 - This notice may not be removed or altered from any source distribution.

 Esta classe é fornecida "como está", sem qualquer garantia expressa ou implícita. Em nenhum caso os autores serão
 responsabilizados por quaisquer danos decorrentes do uso deste software.

 É concedida permissão a qualquer pessoa para usar esta classe para qualquer finalidade, incluindo plugins pagos, e para
 alterá-lo e redistribuí-lo livremente, sujeito às seguintes restrições:
 1 - A origem desta classe não deve ser deturpada; você não deve afirmar que escreveu a classe original. Se você usar esta
  classe em um plugin, uma confirmação de autoria na documentação do plugin será apreciada, mas não é necessária.
 2 - Versões de origem alteradas devem ser claramente marcadas como tal e não devem ser deturpadas como sendo a
 classe original.
 3 - Este aviso não pode ser removido ou alterado de qualquer distribuição de origem.
 */

/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package br.net.fabiozumbi12.RedProtect.Bukkit.schematics.org.jnbt;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Helps create compound tags.
 */
public class CompoundTagBuilder {

    private final Map<String, Tag> entries;

    /**
     * Create a new instance.
     */
    CompoundTagBuilder() {
        this.entries = new HashMap<>();
    }

    /**
     * Create a new instance and use the given map (which will be modified).
     *
     * @param value the value
     */
    CompoundTagBuilder(Map<String, Tag> value) {
        checkNotNull(value);
        this.entries = value;
    }

    /**
     * Create a new builder instance.
     *
     * @return a new builder
     */
    public static CompoundTagBuilder create() {
        return new CompoundTagBuilder();
    }

    /**
     * Put the given key and tag into the compound tag.
     *
     * @param key   they key
     * @param value the value
     * @return this object
     */
    public CompoundTagBuilder put(String key, Tag value) {
        checkNotNull(key);
        checkNotNull(value);
        entries.put(key, value);
        return this;
    }

    /**
     * Put the given key and value into the compound tag as a
     * {@code ByteArrayTag}.
     *
     * @param key   they key
     * @param value the value
     * @return this object
     */
    public CompoundTagBuilder putByteArray(String key, byte[] value) {
        return put(key, new ByteArrayTag(value));
    }

    /**
     * Put the given key and value into the compound tag as a
     * {@code ByteTag}.
     *
     * @param key   they key
     * @param value the value
     * @return this object
     */
    public CompoundTagBuilder putByte(String key, byte value) {
        return put(key, new ByteTag(value));
    }

    /**
     * Put the given key and value into the compound tag as a
     * {@code DoubleTag}.
     *
     * @param key   they key
     * @param value the value
     * @return this object
     */
    public CompoundTagBuilder putDouble(String key, double value) {
        return put(key, new DoubleTag(value));
    }

    /**
     * Put the given key and value into the compound tag as a
     * {@code FloatTag}.
     *
     * @param key   they key
     * @param value the value
     * @return this object
     */
    public CompoundTagBuilder putFloat(String key, float value) {
        return put(key, new FloatTag(value));
    }

    /**
     * Put the given key and value into the compound tag as a
     * {@code IntArrayTag}.
     *
     * @param key   they key
     * @param value the value
     * @return this object
     */
    public CompoundTagBuilder putIntArray(String key, int[] value) {
        return put(key, new IntArrayTag(value));
    }

    /**
     * Put the given key and value into the compound tag as an {@code IntTag}.
     *
     * @param key   they key
     * @param value the value
     * @return this object
     */
    public CompoundTagBuilder putInt(String key, int value) {
        return put(key, new IntTag(value));
    }

    /**
     * Put the given key and value into the compound tag as a
     * {@code LongTag}.
     *
     * @param key   they key
     * @param value the value
     * @return this object
     */
    public CompoundTagBuilder putLong(String key, long value) {
        return put(key, new LongTag(value));
    }

    /**
     * Put the given key and value into the compound tag as a
     * {@code ShortTag}.
     *
     * @param key   they key
     * @param value the value
     * @return this object
     */
    public CompoundTagBuilder putShort(String key, short value) {
        return put(key, new ShortTag(value));
    }

    /**
     * Put the given key and value into the compound tag as a
     * {@code StringTag}.
     *
     * @param key   they key
     * @param value the value
     * @return this object
     */
    public CompoundTagBuilder putString(String key, String value) {
        return put(key, new StringTag(value));
    }

    /**
     * Put all the entries from the given map into this map.
     *
     * @param value the map of tags
     * @return this object
     */
    public CompoundTagBuilder putAll(Map<String, ? extends Tag> value) {
        checkNotNull(value);
        for (Map.Entry<String, ? extends Tag> entry : value.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
        return this;
    }

    /**
     * Build an unnamed compound tag with this builder's entries.
     *
     * @return the new compound tag
     */
    public CompoundTag build() {
        return new CompoundTag(new HashMap<>(entries));
    }

}
