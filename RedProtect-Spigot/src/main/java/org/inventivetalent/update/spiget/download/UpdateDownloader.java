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

package org.inventivetalent.update.spiget.download;

import org.inventivetalent.update.spiget.ResourceInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class UpdateDownloader {

    public static final String RESOURCE_DOWNLOAD = "http://api.spiget.org/v2/resources/%s/download";

    public static Runnable downloadAsync(final ResourceInfo info, final File file, final String userAgent, final DownloadCallback callback) {
        return () -> {
            try {
                download(info, file, userAgent);
                callback.finished();
            } catch (Exception e) {
                callback.error(e);
            }
        };
    }

    public static void download(ResourceInfo info, File file) {
        download(info, file);
    }

    public static void download(ResourceInfo info, File file, String userAgent) {
        if (info.external) {
            throw new IllegalArgumentException("Cannot download external resource #" + info.id);
        }
        ReadableByteChannel channel;
        try {
            //https://stackoverflow.com/questions/921262/how-to-download-and-save-a-file-from-internet-using-java
            HttpURLConnection connection = (HttpURLConnection) new URL(String.format(RESOURCE_DOWNLOAD, info.id)).openConnection();
            connection.setRequestProperty("User-Agent", userAgent);
            if (connection.getResponseCode() != 200) {
                throw new RuntimeException("Download returned status #" + connection.getResponseCode());
            }
            channel = Channels.newChannel(connection.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException("Download failed", e);
        }
        try {
            FileOutputStream output = new FileOutputStream(file);
            output.getChannel().transferFrom(channel, 0, Long.MAX_VALUE);
            output.flush();
            output.close();
        } catch (IOException e) {
            throw new RuntimeException("Could not save file", e);
        }
    }

}
