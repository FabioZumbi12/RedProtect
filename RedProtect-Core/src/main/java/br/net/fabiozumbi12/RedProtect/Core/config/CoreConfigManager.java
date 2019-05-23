/*
 * Copyright (c) 2019 - @FabioZumbi12
 * Last Modified: 22/05/19 23:49
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

package br.net.fabiozumbi12.RedProtect.Core.config;

import br.net.fabiozumbi12.RedProtect.Core.config.Category.FlagGuiCategory;
import br.net.fabiozumbi12.RedProtect.Core.config.Category.GlobalFlagsCategory;
import br.net.fabiozumbi12.RedProtect.Core.config.Category.MainCategory;
import br.net.fabiozumbi12.RedProtect.Core.helpers.CoreUtil;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.IOException;
import java.util.*;

import static com.google.common.reflect.TypeToken.of;

public class CoreConfigManager {
    public ConfigurationNode ecoCfgs;
    protected ConfigurationLoader<CommentedConfigurationNode> ecoLoader;
    protected ConfigurationNode signCfgs;
    protected ConfigurationLoader<CommentedConfigurationNode> signsLoader;
    protected ConfigurationNode guiCfgRoot;
    protected ConfigurationLoader<CommentedConfigurationNode> guiCfgLoader;
    protected FlagGuiCategory guiRoot;
    protected ConfigurationLoader<CommentedConfigurationNode> gFlagsLoader;
    protected ConfigurationNode gflagsRoot;
    protected GlobalFlagsCategory globalFlagsRoot;
    protected ConfigurationNode configRoot;
    protected ConfigurationLoader<CommentedConfigurationNode> cfgLoader;
    protected MainCategory root;

    public HashMap<String, String> backupGuiName = new HashMap<>();
    public HashMap<String, String> backupGuiDescription = new HashMap<>();

    public final List<String> AdminFlags = Arrays.asList(
            "spawn-wither",
            "cropsfarm",
            "keep-inventory",
            "keep-levels",
            "can-drop",
            "can-pickup",
            "cmd-onhealth",
            "can-death",
            "max-players",
            "forcefly",
            "gamemode",
            "player-damage",
            "can-hunger",
            "can-projectiles",
            "allow-place",
            "allow-break",
            "can-pet",
            "allow-cmds",
            "deny-cmds",
            "allow-create-portal",
            "portal-exit",
            "portal-enter",
            "allow-mod",
            "allow-enter-items",
            "deny-enter-items",
            "pvparena",
            "player-enter-command",
            "server-enter-command",
            "player-exit-command",
            "server-exit-command",
            "invincible",
            "effects",
            "treefarm",
            "minefarm",
            "pvp",
            "sign",
            "enderpearl",
            "enter",
            "up-skills",
            "can-back",
            "for-sale",
            "set-portal",
            "exit",
            "particles",
            "dynmap",
            "deny-exit-items");

    public HashMap<String, Object> getDefFlagsValues() {
        return new HashMap<>(root.flags);
    }

    public SortedSet<String> getDefFlags() {
        return new TreeSet<>(getDefFlagsValues().keySet());
    }

    public FlagGuiCategory guiRoot() {
        return this.guiRoot;
    }

    public GlobalFlagsCategory globalFlagsRoot() {
        return this.globalFlagsRoot;
    }

    public MainCategory configRoot() {
        return this.root;
    }

    public int getGuiSlot(String flag) {
        return guiRoot.gui_flags.get(flag).slot;
    }

    public void setGuiSlot(String flag, int slot) {
        guiRoot.gui_flags.get(flag).slot = slot;
        saveGui();
    }

    public int getGuiMaxSlot() {
        SortedSet<Integer> slots = new TreeSet<>(new ArrayList<>());
        for (FlagGuiCategory.GuiFlag key : guiRoot.gui_flags.values()) {
            slots.add(key.slot);
        }
        return Collections.max(slots);
    }


    private void saveConfig() {
        try {
            configRoot.setValue(of(MainCategory.class), root);
            cfgLoader.save(configRoot);
        } catch (IOException | ObjectMappingException e) {
            CoreUtil.printJarVersion();
            e.printStackTrace();
        }
    }

    protected void saveGFlags() {
        try {
            gflagsRoot.setValue(of(GlobalFlagsCategory.class), globalFlagsRoot);
            gFlagsLoader.save(gflagsRoot);
        } catch (IOException | ObjectMappingException e) {
            CoreUtil.printJarVersion();
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            saveConfig();
            saveGFlags();

            ecoLoader.save(ecoCfgs);
            signsLoader.save(signCfgs);
            saveGui();
        } catch (IOException e) {
            CoreUtil.printJarVersion();
            e.printStackTrace();
        }
    }

    public void saveGui() {
        try {
            guiCfgRoot.setValue(of(FlagGuiCategory.class), guiRoot);
            guiCfgLoader.save(guiCfgRoot);
        } catch (IOException | ObjectMappingException e) {
            CoreUtil.printJarVersion();
            e.printStackTrace();
        }
    }

    public boolean addFlag(String flag, boolean defaultValue, boolean isAdmin) {
        if (isAdmin) {
            if (!AdminFlags.contains(flag)) {
                AdminFlags.add(flag);
                return true;
            }
        } else {
            if (!root.flags.containsKey(flag)) {
                root.flags.put(flag, defaultValue);
                saveConfig();
                return true;
            }
        }
        return false;
    }

    public int getBlockCost(String itemName) {
        return ecoCfgs.getNode("items", "values", itemName).getInt();
    }

    public int getEnchantCost(String enchantment) {
        return ecoCfgs.getNode("enchantments", "values", enchantment).getInt(-1);
    }

    public String getEcoString(String key) {
        return ecoCfgs.getNode(key).getString("&4Missing economy string for &c" + key);
    }

    public Integer getEcoInt(String key) {
        return ecoCfgs.getNode(key).getInt();
    }

    public boolean getEcoBool(String key) {
        return ecoCfgs.getNode(key).getBoolean();
    }

    public String getWorldClaimType(String w) {
        return root.region_settings.claim.world_types.getOrDefault(w, "");
    }

}
