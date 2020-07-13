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
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Locale;
import java.util.logging.Level;

public class LocaleHelper {
    private YamlConfiguration bundle;
    private File folder = new File(String.format("%s/locale", BuyRentRegion.get().getDataFolder()));
    private File file = new File(folder, String.format("%s.yml", Locale.getDefault().getLanguage()));

    public LocaleHelper() {
        String localeDisplay = Locale.getDefault().getDisplayName();

        if (localeDisplay == null) localeDisplay = Locale.getDefault().getDisplayLanguage();
        if (localeDisplay == null) localeDisplay = Locale.getDefault().getDisplayCountry();
        if (localeDisplay == null) localeDisplay = Locale.getDefault().toString();

        BuyRentRegion.get().getLogger().info(String.format("Locale: %s", localeDisplay));

        copyFiles();

        updateBundle();
    }

    private void updateBundle() {
        if (!folder.exists()) {
            BuyRentRegion.get().getLogger().severe(String.format("Unable to create folder %s", folder.getPath()));
            return;
        }

        if (!file.exists()) {
            BuyRentRegion.get().getLogger().severe(String.format("The file %s does not exist, falling back to English", file.getPath()));
            file = new File(folder, "en.yml");
        }

        bundle = YamlConfiguration.loadConfiguration(file);
    }

    private String get(String key) {
        return bundle.getString(key);
    }

    public String get(String key, Object... args) {
        if (!bundle.contains(key)) return key;

        try {
            return String.format(get(key), args);
        } catch (Exception e) {
            BuyRentRegion.get().getLogger().log(Level.SEVERE, String.format("An error occurred while translating '%s' with %d args", key, args.length), e);
        }

        return key;
    }

    private void copyFiles() {
        String[] locales = {"en"};

        for (String locale : locales) {
            String filename = locale + ".yml";
            File file = new File(folder, filename);

            if (file.exists()) continue;

            try {
                file.getParentFile().mkdirs();

                InputStream stream = BuyRentRegion.class.getResourceAsStream("/locale/" + filename);

                Files.copy(stream, file.toPath());
            } catch (IOException e) {
                BuyRentRegion.get().getLogger().log(Level.SEVERE, "Failed copying " + filename + " to the data folder", e);
            }
        }
    }
}