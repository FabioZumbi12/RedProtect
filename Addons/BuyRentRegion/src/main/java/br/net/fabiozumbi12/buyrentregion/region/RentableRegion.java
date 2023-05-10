/*
 * Copyright (c) 2012-2023 - @FabioZumbi12
 * Last Modified: 10/05/2023 14:49
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

package br.net.fabiozumbi12.buyrentregion.region;

import br.net.fabiozumbi12.buyrentregion.BuyRentRegion;

public class RentableRegion {
    public String worldName;
    public String regionName;
    public String renter;
    public String signLocationX;
    public String signLocationY;
    public String signLocationZ;
    public String signLocationPitch;
    public String signLocationYaw;
    public String signDirection;
    public String signLine1;
    public String signLine2;
    public String signLine3;
    public String signLine4;
    public String signType;

    public RentableRegion() {
        this.worldName = "";
        this.regionName = "";
        this.renter = "";
        this.signLocationX = "";
        this.signLocationY = "";
        this.signLocationZ = "";
        this.signLocationPitch = "";
        this.signLocationYaw = "";
        this.signDirection = "";
        this.signLine1 = "";
        this.signLine2 = "";
        this.signLine3 = "";
        this.signLine4 = "";
        this.signType = "";
    }

    public RentableRegion(String input) {
        try {
            String[] tmp = input.split("%%%");
            this.worldName = tmp[0];
            this.regionName = tmp[1];
            this.renter = tmp[2];
            this.signLocationX = tmp[3];
            this.signLocationY = tmp[4];
            this.signLocationZ = tmp[5];
            this.signLocationPitch = tmp[6];
            this.signLocationYaw = tmp[7];
            this.signDirection = tmp[8];
            this.signLine1 = tmp[9];
            this.signLine2 = tmp[10];
            this.signLine3 = tmp[11];
            this.signLine4 = tmp[12];
            this.signType = tmp[13];
        } catch (Exception e) {
            BuyRentRegion.get().getLogger().severe("An error occurred while instantiating a RentableRegion.");
        }
    }

    public String toString() {
        return this.worldName + "%%%" + this.regionName + "%%%" + this.renter + "%%%" + this.signLocationX + "%%%" + this.signLocationY + "%%%" + this.signLocationZ + "%%%" + this.signLocationPitch + "%%%" + this.signLocationYaw + "%%%" + this.signDirection + "%%%" + this.signLine1 + "%%%" + this.signLine2 + "%%%" + this.signLine3 + "%%%" + this.signLine4 + "%%%" + this.signType;
    }
}
