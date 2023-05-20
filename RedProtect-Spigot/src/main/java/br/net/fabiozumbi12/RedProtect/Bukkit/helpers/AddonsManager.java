/*
 * Copyright (c) 2012-2023 - @FabioZumbi12
 * Last Modified: 12/05/2023 01:05
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

package br.net.fabiozumbi12.RedProtect.Bukkit.helpers;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Stream;

public class AddonsManager {
    public static void EnableAddons(){
        File[] filePaths = new File(Paths.get(RedProtect.get().getDataFolder().getPath(), "addons").toUri()).listFiles();
        if (filePaths != null) {
            try {
                RedProtect.get().logger.info("Updating Addons...");
                for (File p: Stream.of(filePaths)
                        .filter(file -> !file.isDirectory() && file.getName().startsWith("new-"))
                        .map(File::getAbsoluteFile).toList()){
                    File newFile = new File(Path.of(RedProtect.get().getDataFolder().getPath(),"addons", p.getName().replace("new-","")).toUri());
                    RedProtect.get().logger.info("Updating " + newFile.getName());
                    if (newFile.exists()){
                        if (!newFile.delete()){
                            RedProtect.get().logger.warning(newFile.getName() + " will be updated only on next reboot");
                        }
                    }
                    p.renameTo(newFile);
                }
            } catch (Exception e){
                RedProtect.get().logger.warning("Error on update addons: " + e.getMessage());
            }

            try {
                RedProtect.get().logger.info("Loading Addons...");
                for (File p:Stream.of(filePaths)
                        .filter(file -> !file.isDirectory() && !file.getName().contains("new-"))
                        .map(File::getAbsoluteFile).toList()){
                    RedProtect.get().logger.info("Loading " + p.getName());
                    Plugin pl = RedProtect.get().getServer().getPluginManager().getPlugin(p.getName().split("-")[0]);
                    if (pl == null) {
                        pl = RedProtect.get().getPluginLoader().loadPlugin(p);
                        if (!pl.isEnabled())
                            RedProtect.get().getPluginLoader().enablePlugin(pl);
                    } else {
                        RedProtect.get().logger.warning(p.getName().split("-")[0] + " has been loaded before, ignoring");
                    }
                }
            } catch (Exception e){
                RedProtect.get().logger.warning("Error on enable addons: " + e.getMessage());
            }
        }
    }

    public static boolean DownloadAddOn(String name, CommandSender sender, File folderFilename) {
        RedProtect.get().getLanguageManager().sendMessage(sender,
                RedProtect.get().getLanguageManager().get("cmdmanager.addon.downloading").replace("{name}",name));

        try {
            URL website = new URL( "https://github.com/FabioZumbi12/RedProtect/raw/master/add-ons/" + folderFilename.getName());
            try (InputStream inputStream = website.openStream()) {
                if (folderFilename.exists()){
                    File updateFile = new File(Path.of(RedProtect.get().getDataFolder().getPath(),"addons", "new-" + folderFilename.getName()).toUri());
                    Plugin plugman = RedProtect.get().getServer().getPluginManager().getPlugin("PlugMan");
                    if (plugman != null && plugman.isEnabled()){
                        RedProtect.get().getServer().dispatchCommand(Bukkit.getConsoleSender(), "plugman unload " + name);
                        if (updateFile.exists()) updateFile.delete();
                    } else {
                        folderFilename = updateFile;
                    }
                }
                Files.copy(inputStream, folderFilename.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (IOException e) {
            RedProtect.get().getLanguageManager().sendMessage(sender,
                    RedProtect.get().getLanguageManager().get("cmdmanager.addon.error").replace("{name}",name) + "\n" + e.getMessage());
            return false;
        }
    }
}
