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

 Este software é fornecido "como está", sem qualquer garantia expressa ou implícita. Em nenhum caso os autores serão
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

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The {@code TAG_List} tag.
 */
public final class ListTag extends Tag {

    private final Class<? extends Tag> type;
    private final List<Tag> value;

    /**
     * Creates the tag with an empty name.
     *
     * @param type  the type of tag
     * @param value the value of the tag
     */
    public ListTag(Class<? extends Tag> type, List<? extends Tag> value) {
        super();
        checkNotNull(value);
        this.type = type;
        this.value = Collections.unmodifiableList(value);
    }

    /**
     * Gets the type of item in this list.
     *
     * @return The type of item in this list.
     */
    public Class<? extends Tag> getType() {
        return type;
    }

    @Override
    public List<Tag> getValue() {
        return value;
    }

    /**
     * Create a new list tag with this tag's name and type.
     *
     * @param list the new list
     * @return a new list tag
     */
    public ListTag setValue(List<Tag> list) {
        return new ListTag(getType(), list);
    }

    /**
     * Get the tag if it exists at the given index.
     *
     * @param index the index
     * @return the tag or null
     */

    public Tag getIfExists(int index) {
        try {
            return value.get(index);
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    /**
     * Get a byte array named with the given index.
     *
     * <p>If the index does not exist or its value is not a byte array tag,
     * then an empty byte array will be returned.</p>
     *
     * @param index the index
     * @return a byte array
     */
    public byte[] getByteArray(int index) {
        Tag tag = getIfExists(index);
        if (tag instanceof ByteArrayTag) {
            return ((ByteArrayTag) tag).getValue();
        } else {
            return new byte[0];
        }
    }

    /**
     * Get a byte named with the given index.
     *
     * <p>If the index does not exist or its value is not a byte tag,
     * then {@code 0} will be returned.</p>
     *
     * @param index the index
     * @return a byte
     */
    public byte getByte(int index) {
        Tag tag = getIfExists(index);
        if (tag instanceof ByteTag) {
            return ((ByteTag) tag).getValue();
        } else {
            return (byte) 0;
        }
    }

    /**
     * Get a double named with the given index.
     *
     * <p>If the index does not exist or its value is not a double tag,
     * then {@code 0} will be returned.</p>
     *
     * @param index the index
     * @return a double
     */
    public double getDouble(int index) {
        Tag tag = getIfExists(index);
        if (tag instanceof DoubleTag) {
            return ((DoubleTag) tag).getValue();
        } else {
            return 0;
        }
    }

    /**
     * Get a double named with the given index, even if it's another
     * type of number.
     *
     * <p>If the index does not exist or its value is not a number,
     * then {@code 0} will be returned.</p>
     *
     * @param index the index
     * @return a double
     */
    public double asDouble(int index) {
        Tag tag = getIfExists(index);
        if (tag instanceof ByteTag) {
            return ((ByteTag) tag).getValue();

        } else if (tag instanceof ShortTag) {
            return ((ShortTag) tag).getValue();

        } else if (tag instanceof IntTag) {
            return ((IntTag) tag).getValue();

        } else if (tag instanceof LongTag) {
            return ((LongTag) tag).getValue();

        } else if (tag instanceof FloatTag) {
            return ((FloatTag) tag).getValue();

        } else if (tag instanceof DoubleTag) {
            return ((DoubleTag) tag).getValue();

        } else {
            return 0;
        }
    }

    /**
     * Get a float named with the given index.
     *
     * <p>If the index does not exist or its value is not a float tag,
     * then {@code 0} will be returned.</p>
     *
     * @param index the index
     * @return a float
     */
    public float getFloat(int index) {
        Tag tag = getIfExists(index);
        if (tag instanceof FloatTag) {
            return ((FloatTag) tag).getValue();
        } else {
            return 0;
        }
    }

    /**
     * Get a {@code int[]} named with the given index.
     *
     * <p>If the index does not exist or its value is not an int array tag,
     * then an empty array will be returned.</p>
     *
     * @param index the index
     * @return an int array
     */
    public int[] getIntArray(int index) {
        Tag tag = getIfExists(index);
        if (tag instanceof IntArrayTag) {
            return ((IntArrayTag) tag).getValue();
        } else {
            return new int[0];
        }
    }

    /**
     * Get an int named with the given index.
     *
     * <p>If the index does not exist or its value is not an int tag,
     * then {@code 0} will be returned.</p>
     *
     * @param index the index
     * @return an int
     */
    public int getInt(int index) {
        Tag tag = getIfExists(index);
        if (tag instanceof IntTag) {
            return ((IntTag) tag).getValue();
        } else {
            return 0;
        }
    }

    /**
     * Get an int named with the given index, even if it's another
     * type of number.
     *
     * <p>If the index does not exist or its value is not a number,
     * then {@code 0} will be returned.</p>
     *
     * @param index the index
     * @return an int
     */
    public int asInt(int index) {
        Tag tag = getIfExists(index);
        if (tag instanceof ByteTag) {
            return ((ByteTag) tag).getValue();

        } else if (tag instanceof ShortTag) {
            return ((ShortTag) tag).getValue();

        } else if (tag instanceof IntTag) {
            return ((IntTag) tag).getValue();

        } else if (tag instanceof LongTag) {
            return ((LongTag) tag).getValue().intValue();

        } else if (tag instanceof FloatTag) {
            return ((FloatTag) tag).getValue().intValue();

        } else if (tag instanceof DoubleTag) {
            return ((DoubleTag) tag).getValue().intValue();

        } else {
            return 0;
        }
    }

    /**
     * Get a list of tags named with the given index.
     *
     * <p>If the index does not exist or its value is not a list tag,
     * then an empty list will be returned.</p>
     *
     * @param index the index
     * @return a list of tags
     */
    public List<Tag> getList(int index) {
        Tag tag = getIfExists(index);
        if (tag instanceof ListTag) {
            return ((ListTag) tag).getValue();
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Get a {@code TagList} named with the given index.
     *
     * <p>If the index does not exist or its value is not a list tag,
     * then an empty tag list will be returned.</p>
     *
     * @param index the index
     * @return a tag list instance
     */
    public ListTag getListTag(int index) {
        Tag tag = getIfExists(index);
        if (tag instanceof ListTag) {
            return (ListTag) tag;
        } else {
            return new ListTag(StringTag.class, Collections.emptyList());
        }
    }

    /**
     * Get a list of tags named with the given index.
     *
     * <p>If the index does not exist or its value is not a list tag,
     * then an empty list will be returned. If the given index references
     * a list but the list of of a different type, then an empty
     * list will also be returned.</p>
     *
     * @param index    the index
     * @param listType the class of the contained type
     * @param <T>      the NBT type
     * @return a list of tags
     */
    @SuppressWarnings("unchecked")
    public <T extends Tag> List<T> getList(int index, Class<T> listType) {
        Tag tag = getIfExists(index);
        if (tag instanceof ListTag) {
            ListTag listTag = (ListTag) tag;
            if (listTag.getType().equals(listType)) {
                return (List<T>) listTag.getValue();
            } else {
                return Collections.emptyList();
            }
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Get a long named with the given index.
     *
     * <p>If the index does not exist or its value is not a long tag,
     * then {@code 0} will be returned.</p>
     *
     * @param index the index
     * @return a long
     */
    public long getLong(int index) {
        Tag tag = getIfExists(index);
        if (tag instanceof LongTag) {
            return ((LongTag) tag).getValue();
        } else {
            return 0L;
        }
    }

    /**
     * Get a long named with the given index, even if it's another
     * type of number.
     *
     * <p>If the index does not exist or its value is not a number,
     * then {@code 0} will be returned.</p>
     *
     * @param index the index
     * @return a long
     */
    public long asLong(int index) {
        Tag tag = getIfExists(index);
        if (tag instanceof ByteTag) {
            return ((ByteTag) tag).getValue();

        } else if (tag instanceof ShortTag) {
            return ((ShortTag) tag).getValue();

        } else if (tag instanceof IntTag) {
            return ((IntTag) tag).getValue();

        } else if (tag instanceof LongTag) {
            return ((LongTag) tag).getValue();

        } else if (tag instanceof FloatTag) {
            return ((FloatTag) tag).getValue().longValue();

        } else if (tag instanceof DoubleTag) {
            return ((DoubleTag) tag).getValue().longValue();

        } else {
            return 0;
        }
    }

    /**
     * Get a short named with the given index.
     *
     * <p>If the index does not exist or its value is not a short tag,
     * then {@code 0} will be returned.</p>
     *
     * @param index the index
     * @return a short
     */
    public short getShort(int index) {
        Tag tag = getIfExists(index);
        if (tag instanceof ShortTag) {
            return ((ShortTag) tag).getValue();
        } else {
            return 0;
        }
    }

    /**
     * Get a string named with the given index.
     *
     * <p>If the index does not exist or its value is not a string tag,
     * then {@code ""} will be returned.</p>
     *
     * @param index the index
     * @return a string
     */
    public String getString(int index) {
        Tag tag = getIfExists(index);
        if (tag instanceof StringTag) {
            return ((StringTag) tag).getValue();
        } else {
            return "";
        }
    }

    @Override
    public String toString() {
        StringBuilder bldr = new StringBuilder();
        bldr.append("TAG_List").append(": ").append(value.size()).append(" entries of type ").append(NBTUtils.getTypeName(type)).append("\r\n{\r\n");
        for (Tag t : value) {
            bldr.append("   ").append(t.toString().replaceAll("\r\n", "\r\n   ")).append("\r\n");
        }
        bldr.append("}");
        return bldr.toString();
    }

}
