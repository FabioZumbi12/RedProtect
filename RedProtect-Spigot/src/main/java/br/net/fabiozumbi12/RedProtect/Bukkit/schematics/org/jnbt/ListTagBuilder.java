/*
 Copyright @FabioZumbi12

 This class is provided 'as-is', without any express or implied warranty. In no event will the authors be held liable for any
  damages arising from the use of this class.

 Permission is granted to anyone to use this class for any purpose, including commercial plugins, and to alter it and
 redistribute it freely, subject to the following restrictions:
 1 - The origin of this class must not be misrepresented; you must not claim that you wrote the original software. If you
 use this class in other plugins, an acknowledgment in the plugin documentation would be appreciated but is not required.
 2 - Altered source versions must be plainly marked as such, and must not be misrepresented as being the original class.
 3 - This notice may not be removed or altered from any source distribution.

 Esta classe é fornecida "como está", sem qualquer garantia expressa ou implícita. Em nenhum caso os autores serão
 responsabilizados por quaisquer danos decorrentes do uso desta classe.

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Helps create list tags.
 */
public class ListTagBuilder {

    private final Class<? extends Tag> type;
    private final List<Tag> entries;

    /**
     * Create a new instance.
     *
     * @param type of tag contained in this list
     */
    ListTagBuilder(Class<? extends Tag> type) {
        checkNotNull(type);
        this.type = type;
        this.entries = new ArrayList<>();
    }

    /**
     * Create a new builder instance.
     *
     * @return a new builder
     */
    public static ListTagBuilder create(Class<? extends Tag> type) {
        return new ListTagBuilder(type);
    }

    /**
     * Create a new builder instance.
     *
     * @return a new builder
     */
    @SafeVarargs
    public static <T extends Tag> ListTagBuilder createWith(T... entries) {
        checkNotNull(entries);

        if (entries.length == 0) {
            throw new IllegalArgumentException("This method needs an array of at least one entry");
        }

        Class<? extends Tag> type = entries[0].getClass();
        for (int i = 1; i < entries.length; i++) {
            if (!type.isInstance(entries[i])) {
                throw new IllegalArgumentException("An array of different tag types was provided");
            }
        }

        ListTagBuilder builder = new ListTagBuilder(type);
        builder.addAll(Arrays.asList(entries));
        return builder;
    }

    /**
     * Add the given tag.
     *
     * @param value the tag
     * @return this object
     */
    public void add(Tag value) {
        checkNotNull(value);
        if (!type.isInstance(value)) {
            throw new IllegalArgumentException(value.getClass().getCanonicalName() + " is not of expected type " + type.getCanonicalName());
        }
        entries.add(value);
    }

    /**
     * Add all the tags in the given list.
     *
     * @param value a list of tags
     * @return this object
     */
    public void addAll(Collection<? extends Tag> value) {
        checkNotNull(value);
        for (Tag v : value) {
            add(v);
        }
    }

    /**
     * Build an unnamed list tag with this builder's entries.
     *
     * @return the new list tag
     */
    public ListTag build() {
        return new ListTag(type, new ArrayList<>(entries));
    }

}
