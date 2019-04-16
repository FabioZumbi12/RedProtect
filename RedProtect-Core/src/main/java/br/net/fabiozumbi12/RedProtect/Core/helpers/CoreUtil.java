/*
 *  Copyright (c) 2019 - @FabioZumbi12
 *  Last Modified: 16/04/19 06:21
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

package br.net.fabiozumbi12.RedProtect.Core.helpers;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class CoreUtil {

    protected static String DateNow(String format) {
        DateFormat df = new SimpleDateFormat(format);
        Date today = Calendar.getInstance().getTime();
        return df.format(today);
    }

    public static String HourNow() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int min = Calendar.getInstance().get(Calendar.MINUTE);
        int sec = Calendar.getInstance().get(Calendar.SECOND);
        return "[" + hour + ":" + min + ":" + sec + "]";
    }

    public static void SaveToZipSB(File file, StringBuilder sb) {
        try {
            final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file));
            ZipEntry e = new ZipEntry("RedProtectLogs.txt");
            out.putNextEntry(e);

            byte[] data = sb.toString().getBytes();
            out.write(data, 0, data.length);
            out.closeEntry();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String setName(String name) {
        if (name == null || name.isEmpty()) return name;

        name = Normalizer.normalize(name.replaceAll("[().+=;:]", ""), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replaceAll("[ -]", "_")
                .replaceAll("[^\\p{L}_0-9]", "");
        return name;
    }

    protected static File genFileName(String Path, Boolean isBackup, int maxbackups, String dateFormat) {
        int count = 1;
        String date = dateFormat.replace("/", "-");
        File logfile = new File(Path + date + "-" + count + ".zip");
        File[] files = new File(Path).listFiles();
        HashMap<Long, File> keyFiles = new HashMap<>();
        if (files != null && files.length >= maxbackups && isBackup) {
            for (File key : files) {
                keyFiles.put(key.lastModified(), key);
            }
            keyFiles.get(Collections.min(keyFiles.keySet())).delete();
        }

        while (logfile.exists()) {
            count++;
            logfile = new File(Path + date + "-" + count + ".zip");
        }

        return logfile;
    }

    public static boolean isUUIDs(String uuid) {
        if (uuid == null) {
            return false;
        }

        try {
            UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static Object parseObject(String value) {
        Object obj = value;
        try {
            obj = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                obj = Boolean.parseBoolean(value);
            }
        }
        return obj;
    }

    public static String StripName(String pRName) {
        String regionName;
        if (pRName.length() > 13) {
            regionName = pRName.substring(0, 13);
        } else {
            regionName = pRName;
        }
        return regionName;
    }
}
