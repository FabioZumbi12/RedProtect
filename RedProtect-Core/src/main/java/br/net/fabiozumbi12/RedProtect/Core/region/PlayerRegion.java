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

package br.net.fabiozumbi12.RedProtect.Core.region;

public class PlayerRegion {

    private String uuid;
    private String playerName;

    public PlayerRegion(String uuid, String playerName) {
        this.uuid = uuid;
        this.playerName = playerName;
    }

    /**
     * Get the UUID or playerName if OnlineMode is false.
     *
     * @return String of UUID or PlayerName
     */
    public String getUUID() {
        return this.uuid;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    /**
     * Get the last know playerName.
     *
     * @return String of PlayerName
     */
    public String getPlayerName() {
        return this.playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public boolean contains(String option){
        return this.uuid.equalsIgnoreCase(option) || this.playerName.equalsIgnoreCase(option);
    }

    @Override
    public String toString() {
        return "(" + uuid + "," + playerName + ")";
    }
}
