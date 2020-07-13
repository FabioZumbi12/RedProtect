/*
 * Copyright (c) 2020 - @FabioZumbi12
 * Last Modified: 25/10/2019 22:04.
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

package org.inventivetalent.update.spiget.comparator;

public abstract class VersionComparator {

    /**
     * Compares versions by checking if the version strings are equal
     */
    public static final VersionComparator EQUAL = new VersionComparator() {
        @Override
        public boolean isNewer(String currentVersion, String checkVersion) {
            return !currentVersion.equals(checkVersion);
        }
    };

    /**
     * Compares versions by their Sematic Version (<code>Major.Minor.Patch</code>, <a href="http://semver.org/">semver.org</a>). Removes dots and compares the resulting Integer values
     */
    public static final VersionComparator SEM_VER = new VersionComparator() {
        @Override
        public boolean isNewer(String currentVersion, String checkVersion) {
            currentVersion = currentVersion.replace(".", "");
            checkVersion = checkVersion.replace(".", "");

            try {
                int current = Integer.parseInt(currentVersion);
                int check = Integer.parseInt(checkVersion);

                return check > current;
            } catch (NumberFormatException e) {
                System.err.println("[SpigetUpdate] Invalid SemVer versions specified [" + currentVersion + "] [" + checkVersion + "]");
            }
            return false;
        }
    };

    /**
     * Same as {@link VersionComparator#SEM_VER}, but supports version names with '-SNAPSHOT' suffixes
     */
    public static final VersionComparator SEM_VER_SNAPSHOT = new VersionComparator() {
        @Override
        public boolean isNewer(String currentVersion, String checkVersion) {
            currentVersion = currentVersion.replace("-SNAPSHOT", "");
            checkVersion = checkVersion.replace("-SNAPSHOT", "");

            return SEM_VER.isNewer(currentVersion, checkVersion);
        }
    };

    /**
     * Called to check if a version is newer
     *
     * @param currentVersion Current version of the plugin
     * @param checkVersion   Version to check
     * @return <code>true</code> if the checked version is newer
     */
    public abstract boolean isNewer(String currentVersion, String checkVersion);

}
