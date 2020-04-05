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

package br.net.fabiozumbi12.RedProtect.Core.helpers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class CoreUtil {
    public boolean stopRegen;
    protected HashMap<String, Object> borderPlayers;
    protected HashMap<String, String> cachedUUIDs;

    public CoreUtil() {
        borderPlayers = new HashMap<>();
        cachedUUIDs = new HashMap<>();
    }

    public static void printJarVersion() {
        String jarVersion = new File(CoreUtil.class.getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .getPath())
                .getName();
        System.out.print("RedProtect jar: " + jarVersion);
    }

    protected String dateNow(String format) {
        DateFormat df = new SimpleDateFormat(format);
        Date today = Calendar.getInstance().getTime();
        return df.format(today);
    }

    public String hourNow() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int min = Calendar.getInstance().get(Calendar.MINUTE);
        int sec = Calendar.getInstance().get(Calendar.SECOND);
        return "[" + hour + ":" + min + ":" + sec + "]";
    }

    public void saveSBToZip(File file, StringBuilder builder) {
        try {
            final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file));
            ZipEntry e = new ZipEntry("RedProtectLogs.txt");
            out.putNextEntry(e);

            byte[] data = builder.toString().getBytes();
            out.write(data, 0, data.length);
            out.closeEntry();
            out.close();
        } catch (Exception e) {
            printJarVersion();
            e.printStackTrace();
        }
    }

    /* public String setName(String name) {
        if (name == null || name.isEmpty()) return name;

        name = Normalizer.normalize(name.replaceAll("[().+=;:]", ""), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replaceAll("[ -]", "_")
                .replaceAll("[^\\p{L}_0-9]", "");
        return name;
    } */

    protected File genFileName(String Path, Boolean isBackup, int maxbackups, String dateFormat) {
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

    public boolean isUUIDs(String uuid) {
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

    public Object parseObject(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                return Boolean.parseBoolean(value);
            }
            if (value.equalsIgnoreCase("allow")) {
                return true;
            }
            if (value.equalsIgnoreCase("deny")) {
                return false;
            }
        }
        return value;
    }

    protected String StripName(String pRName) {
        String regionName;
        if (pRName.length() > 13) {
            regionName = pRName.substring(0, 13);
        } else {
            regionName = pRName;
        }
        return regionName;
    }

    public void zipFolder(String sourceDirPath, String zipFilePath) throws IOException {
        Path p = Files.createFile(Paths.get(zipFilePath));
        try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(p))) {
            Path pp = Paths.get(sourceDirPath);
            Files.walk(pp)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(pp.relativize(path).toString());
                        try {
                            zs.putNextEntry(zipEntry);
                            Files.copy(path, zs);
                            zs.closeEntry();
                        } catch (IOException e) {
                            System.err.println(e);
                        }
                    });
        }
    }
}
