/*
 *  Copyright (c) 2019 - @FabioZumbi12
 *  Last Modified: 16/04/19 04:43
 *
 *  This class is provided 'as-is', without any express or implied warranty. In no event will the authors be held liable for any
 *   damages arising from the use of this class.
 *
 *  Permission is granted to anyone to use this class for any purpose, including commercial plugins, and to alter it and
 *  redistribute it freely, subject to the following restrictions:
 *  1 - The origin of this class must not be misrepresented; you must not claim that you wrote the original software. If you
 *  use this class in other plugins, an acknowledgment in the plugin documentation would be appreciated but is not required.
 *  2 - Altered source versions must be plainly marked as such, and must not be misrepresented as being the original class.
 *  3 - This notice may not be removed or altered from any source distribution.
 *
 *  Esta classe é fornecida "como está", sem qualquer garantia expressa ou implícita. Em nenhum caso os autores serão
 *  responsabilizados por quaisquer danos decorrentes do uso desta classe.
 *
 *  É concedida permissão a qualquer pessoa para usar esta classe para qualquer finalidade, incluindo plugins pagos, e para
 *  alterá-lo e redistribuí-lo livremente, sujeito às seguintes restrições:
 *  1 - A origem desta classe não deve ser deturpada; você não deve afirmar que escreveu a classe original. Se você usar esta
 *   classe em um plugin, uma confirmação de autoria na documentação do plugin será apreciada, mas não é necessária.
 *  2 - Versões de origem alteradas devem ser claramente marcadas como tal e não devem ser deturpadas como sendo a
 *  classe original.
 *  3 - Este aviso não pode ser removido ou alterado de qualquer distribuição de origem.
 */

package br.net.fabiozumbi12.RedProtect.Bukkit.fanciful.util;

import org.apache.commons.lang.Validate;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;

/**
 * Represents a wrapper around an array class of an arbitrary reference type,
 * which properly implements "value" hash code and equality functions.
 * <p>
 * This class is intended for use as a uuid to a map.
 * </p>
 *
 * @param <E> The type of elements in the array.
 * @author Glen Husman
 * @see Arrays
 */
public final class ArrayWrapper<E> {

    private E[] _array;

    /**
     * Creates an array wrapper with some elements.
     *
     * @param elements The elements of the array.
     */
    @SafeVarargs
    public ArrayWrapper(E... elements) {
        setArray(elements);
    }

    /**
     * Converts an iterable element collection to an array of elements.
     * The iteration order of the specified object will be used as the array element order.
     *
     * @param list The iterable of objects which will be converted to an array.
     * @param c    The type of the elements of the array.
     * @return An array of elements in the specified iterable.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] toArray(Iterable<? extends T> list, Class<T> c) {
        int size = -1;
        if (list instanceof Collection<?>) {
            @SuppressWarnings("rawtypes")
            Collection coll = (Collection) list;
            size = coll.size();
        }


        if (size < 0) {
            size = 0;
            // Ugly hack: Count it ourselves
            for (@SuppressWarnings("unused") T element : list) {
                size++;
            }
        }

        T[] result = (T[]) Array.newInstance(c, size);
        int i = 0;
        for (T element : list) { // Assumes iteration order is consistent
            result[i++] = element; // Assign array element at index THEN increment counter
        }
        return result;
    }

    /**
     * Retrieves a reference to the wrapped array instance.
     *
     * @return The array wrapped by this instance.
     */
    public E[] getArray() {
        return _array;
    }

    /**
     * Set this wrapper to wrap a new array instance.
     *
     * @param array The new wrapped array.
     */
    public void setArray(E[] array) {
        Validate.notNull(array, "The array must not be null.");
        _array = array;
    }

    /**
     * Determines if this object has a value equivalent to another object.
     *
     * @see Arrays#equals(Object[], Object[])
     */
    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object other) {
        return other instanceof ArrayWrapper && Arrays.equals(_array, ((ArrayWrapper) other)._array);
    }

    /**
     * Gets the hash code represented by this objects value.
     *
     * @return This object's hash code.
     * @see Arrays#hashCode(Object[])
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(_array);
    }
}
