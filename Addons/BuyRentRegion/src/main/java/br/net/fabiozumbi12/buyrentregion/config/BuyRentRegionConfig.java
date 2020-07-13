/*
 * Copyright (c) 2020 - @FabioZumbi12
 * Last Modified: 12/07/2020 20:24.
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

package br.net.fabiozumbi12.buyrentregion.config;

import br.net.fabiozumbi12.buyrentregion.BuyRentRegion;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.Locale;

public class BuyRentRegionConfig {
    public final String dataLoc = "plugins" + File.separator + "BuyRentRegion" + File.separator;
    public final String signDataLoc = "plugins" + File.separator + "BuyRentRegion" + File.separator + "rent" + File.separator;

    public int buyRegionMax;
    public int rentRegionMax;
    public boolean requireBuyMode;
    public long tickRate;
    public boolean requireBuyPerms;
    public boolean requireRentPerms;
    public String dateFormatString;
    public String signHeaderBuy;
    public String signHeaderRent;
    public boolean payRentOwners;

    public BuyRentRegionConfig() {
        FileConfiguration config = BuyRentRegion.get().getConfig();

        try {
            buyRegionMax = config.getInt("BuyRegionMax", 0);
        } catch (Exception e) {
            buyRegionMax = 0;
        }
        try {
            rentRegionMax = config.getInt("RentRegionMax", 0);
        } catch (Exception e) {
            rentRegionMax = 0;
        }
        try {
            requireBuyMode = config.getBoolean("RequireBuyMode", true);
        } catch (Exception e) {
            requireBuyMode = true;
        }
        try {
            tickRate = config.getLong("CheckExpirationsInMins") * 60L * 20L;
        } catch (Exception e) {
            tickRate = 6000L;
        }
        try {
            requireBuyPerms = config.getBoolean("RequireBuyPerms", false);
        } catch (Exception e) {
            requireBuyPerms = false;
        }
        try {
            requireRentPerms = config.getBoolean("RequireRentPerms", false);
        } catch (Exception e) {
            requireRentPerms = false;
        }
        try {
            payRentOwners = config.getBoolean("PayRentOwners", true);
        } catch (Exception e) {
            payRentOwners = true;
        }
        try {
            signHeaderBuy = config.getString("SignHeaderBuy", "[BuyRegion]");
        } catch (Exception e) {
            signHeaderBuy = "[BuyRegion]";
        }
        try {
            signHeaderRent = config.getString("SignHeaderRent", "[RentRegion]");
        } catch (Exception e) {
            signHeaderRent = "[RentRegion]";
        }
        try {
            setFormatString(config.getString("DateFormat", "Default"));
        } catch (Exception e) {
            dateFormatString = "yy/MM/dd h:mma";
        }
        Locale.setDefault(Locale.forLanguageTag(config.getString("Locale", "en")));

        config.options().copyDefaults(true);
    }

    private void setFormatString(String input) {
        try {
            if (input.equalsIgnoreCase("US")) {
                dateFormatString = "MM/dd/yy h:mma";
            } else if (input.equalsIgnoreCase("EU")) {
                dateFormatString = "dd/MM/yy h:mma";
            } else {
                dateFormatString = "yy/MM/dd h:mma";
            }
        } catch (Exception e) {
            dateFormatString = "yy/MM/dd h:mma";
        }
    }

}
