/*
 *  Copyright (c) 2019 - @FabioZumbi12
 *  Last Modified: 28/03/19 20:20
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

package br.net.fabiozumbi12.RedProtect.Core.config.Category;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.HashMap;
import java.util.Map;

@ConfigSerializable
public class FlagGuiCategory {

    @Setting(value = "gui-separator")
    public guiSeparator gui_separator = new guiSeparator();
    @Setting(value = "gui-flags")
    public Map<String, GuiFlag> gui_flags = new HashMap<>();

    public FlagGuiCategory() {
    }

    @ConfigSerializable
    public static class guiSeparator {
        @Setting(comment = "Color? Wood type?")
        public int data = 0;
        @Setting(comment = "The item names is like you see holding \"F3\" and pressing \"H\".")
        public String material = "";
    }

    @ConfigSerializable
    public static class GuiFlag {
        @Setting
        public String material = "golden_apple";
        @Setting
        public int slot = 0;

        public GuiFlag() {
        }

        public GuiFlag(String mat, int slot) {
            this.material = mat;
            this.slot = slot;
        }
    }
}
