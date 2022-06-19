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

package org.inventivetalent.update.spiget;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.inventivetalent.update.spiget.comparator.VersionComparator;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class SpigetUpdateAbstract {

    public static final String RESOURCE_INFO = "https://api.spiget.org/v2/resources/%s?ut=%s";
    public static final String RESOURCE_VERSION = "https://api.spiget.org/v2/resources/%s/versions/latest?ut=%s";

    protected final int resourceId;
    protected final String currentVersion;
    protected final Logger log;
    protected String userAgent = "SpigetResourceUpdater";
    protected VersionComparator versionComparator = VersionComparator.EQUAL;

    protected ResourceInfo latestResourceInfo;

    public SpigetUpdateAbstract(int resourceId, String currentVersion, Logger log) {
        this.resourceId = resourceId;
        this.currentVersion = currentVersion;
        this.log = log;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public void setVersionComparator(VersionComparator comparator) {
        this.versionComparator = comparator;
    }

    public ResourceInfo getLatestResourceInfo() {
        return latestResourceInfo;
    }

    protected abstract void dispatch(Runnable runnable);

    public boolean isVersionNewer(String oldVersion, String newVersion) {
        return versionComparator.isNewer(oldVersion, newVersion);
    }

    public void checkForUpdate(final UpdateCallback callback) {
        dispatch(() -> {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(String.format(RESOURCE_INFO, resourceId, System.currentTimeMillis())).openConnection();
                connection.setRequestProperty("User-Agent", getUserAgent());
                JsonObject jsonObject = new JsonParser().parse(new InputStreamReader(connection.getInputStream())).getAsJsonObject();
                latestResourceInfo = new Gson().fromJson(jsonObject, ResourceInfo.class);

                connection = (HttpURLConnection) new URL(String.format(RESOURCE_VERSION, resourceId, System.currentTimeMillis())).openConnection();
                connection.setRequestProperty("User-Agent", getUserAgent());
                jsonObject = new JsonParser().parse(new InputStreamReader(connection.getInputStream())).getAsJsonObject();
                latestResourceInfo.latestVersion = new Gson().fromJson(jsonObject, ResourceVersion.class);

                if (isVersionNewer(currentVersion, latestResourceInfo.latestVersion.name)) {
                    callback.updateAvailable(latestResourceInfo.latestVersion.name, "https://spigotmc.org/" + latestResourceInfo.file.url, !latestResourceInfo.external && !latestResourceInfo.premium);
                } else {
                    callback.upToDate();
                }
            } catch (Exception e) {
                log.log(Level.WARNING, "Failed to get resource info from spiget.org", e);
            }
        });
    }

}
