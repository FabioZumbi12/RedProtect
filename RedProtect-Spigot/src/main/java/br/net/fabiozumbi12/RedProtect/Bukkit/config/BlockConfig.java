/*
 * Copyright (c) 2020 - @FabioZumbi12
 * Last Modified: 08/08/2020 23:40.
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

package br.net.fabiozumbi12.RedProtect.Bukkit.config;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Core.config.Category.BlockCategory;
import br.net.fabiozumbi12.RedProtect.Core.helpers.CoreUtil;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import static com.google.common.reflect.TypeToken.of;

public class BlockConfig {
    protected final ConfigurationLoader<CommentedConfigurationNode> blockLoader;
    private ConfigurationNode blockRoot;
    private BlockCategory blockCat;

    public BlockConfig() {
        String headerBlock = """
                +--------------------------------------------------------------------+ #
                <               RedProtect Block configuration File                  > #
                <--------------------------------------------------------------------> #
                <       This is the configuration file, feel free to edit it.        > #
                <        For more info about cmds and flags, check our Wiki:         > #
                <         https://github.com/FabioZumbi12/RedProtect/wiki            > #
                +--------------------------------------------------------------------+ #

                Notes:
                Lists are [object1, object2, ...]
                Strings containing the char & always need to be quoted""";
        blockLoader = HoconConfigurationLoader.builder().setFile(new File(RedProtect.get().getDataFolder(), "blocks.conf")).build();
        try {
            blockRoot = blockLoader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true).setHeader(headerBlock));
            this.blockCat = blockRoot.getValue(of(BlockCategory.class), new BlockCategory());
            saveConfig();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long getBlockLimit(Player player) {
        if (!isEnabled()) return 0;

        if (this.blockCat.players.containsKey(player.getUniqueId().toString())) {
            long time = this.blockCat.players.get(player.getUniqueId().toString()).time;
            long added_blocks = this.blockCat.players.get(player.getUniqueId().toString()).added_blocks;

            Date playerDate = new Date(time);
            Date nowDate = Calendar.getInstance().getTime();

            long diff = nowDate.getTime() - playerDate.getTime();

            long diffSeconds = diff / 1000 % 60;
            long diffMinutes = diff / (60 * 1000) % 60;
            long diffHours = diff / (60 * 60 * 1000) % 24;
            long diffDays = diff / (24 * 60 * 60 * 1000);

            long result = 0;

            if ("d".equals(this.blockCat.unit_to_add)) {
                result = diffDays;
            } else if ("h".equals(this.blockCat.unit_to_add)) {
                result = diffHours;
            } else if ("m".equals(this.blockCat.unit_to_add)) {
                result = diffMinutes;
            } else if ("s".equals(this.blockCat.unit_to_add)) {
                result = diffSeconds;
            }

            return result + added_blocks;
        } else {
            addPlayer(player);
        }
        return 0;
    }

    public void addPlayer(Player player) {
        if (!this.blockCat.players.containsKey(player.getUniqueId().toString())) {
            this.blockCat.players.put(player.getUniqueId().toString(), new BlockCategory.PlayerCat(Calendar.getInstance().getTime().getTime(), player.getName()));
            saveConfig();
        }
    }

    public void setBlock(long amount, Player player) {
        if (!this.blockCat.players.containsKey(player.getUniqueId().toString())) {
            addPlayer(player);
        }
        this.blockCat.players.get(player.getUniqueId().toString()).added_blocks = amount;
        saveConfig();
    }

    public long delBlock(long amount, Player player) {
        if (!this.blockCat.players.containsKey(player.getUniqueId().toString())) {
            addPlayer(player);
        }

        long total = this.blockCat.players.get(player.getUniqueId().toString()).added_blocks;
        if (total - amount >= 0)
            this.blockCat.players.get(player.getUniqueId().toString()).added_blocks -= amount;

        saveConfig();
        return this.blockCat.players.get(player.getUniqueId().toString()).added_blocks;
    }

    public long addBlock(long amount, Player player) {
        if (!this.blockCat.players.containsKey(player.getUniqueId().toString())) {
            addPlayer(player);
        }
        this.blockCat.players.get(player.getUniqueId().toString()).added_blocks += amount;
        saveConfig();
        return this.blockCat.players.get(player.getUniqueId().toString()).added_blocks;
    }

    public boolean isEnabled() {
        return this.blockCat.enabled;
    }

    private void saveConfig() {
        try {
            blockRoot.setValue(of(BlockCategory.class), blockCat);
            blockLoader.save(blockRoot);
        } catch (IOException | ObjectMappingException e) {
            CoreUtil.printJarVersion();
            e.printStackTrace();
        }
    }
}
