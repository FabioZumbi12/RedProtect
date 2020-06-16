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

package br.net.fabiozumbi12.RedProtect.Bukkit.hooks;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.helpers.WorldGuardHelper;
import br.net.fabiozumbi12.RedProtect.Core.helpers.CoreUtil;
import com.earth2me.essentials.Essentials;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import net.sacredlabyrinth.phaed.simpleclans.managers.ClanManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.dynmap.DynmapAPI;

public class HooksManager {
    public boolean bossBar;
    public boolean actionBar;
    public boolean myChunk;
    public boolean myPet;
    public boolean magicCarpet;
    public boolean vault;
    public boolean pvpm;
    public boolean essentials;
    public boolean griefPrev;
    public boolean worldEdit;
    public boolean worldguard;
    public boolean simpleClans;
    public ClanManager clanManager;
    public Essentials pless;
    public boolean Dyn;
    public DynmapHook dynmapHook;
    public WorldGuardHelper worldGuardHelper;
    public TransAPI transAPI;

    public void registerHooksFirst() {
        try {
            bossBar = checkBM();
            actionBar = checkAB();
            myChunk = checkMyChunk();
            myPet = checkMyPet();
            magicCarpet = checkMagicCarpet();
            vault = checkVault();
            pvpm = checkPvPm();
            essentials = checkEss();
            griefPrev = checkGriefPrev();
            worldEdit = checkWe();
            worldguard = checkWG();
            simpleClans = checkSC();
            boolean translationApi = checkTAPI();
            boolean infernalMobs = checkIMobs();
            boolean fac = checkFac();
            boolean placeHolderAPI = checkPHAPI();
            boolean mcMMO = checkMcMMo();
            boolean skillAPI = checkSkillAPI();

            if (vault) {
                // Economy
                RegisteredServiceProvider<Economy> rsp = RedProtect.get().getServer().getServicesManager().getRegistration(Economy.class);
                if (rsp != null) {
                    RedProtect.get().economy = rsp.getProvider();
                    RedProtect.get().logger.info("Vault Economy found. Hooked.");
                } else {
                    RedProtect.get().logger.warning("Could not initialize Vault Economy hook.");
                }
                // Permissions
                RegisteredServiceProvider<Permission> perm = RedProtect.get().getServer().getServicesManager().getRegistration(Permission.class);
                if (perm != null) {
                    RedProtect.get().permission = perm.getProvider();
                    RedProtect.get().logger.info("Vault Permissions found. Hooked.");
                } else {
                    RedProtect.get().logger.warning("Could not initialize Vault Permissions hook.");
                }
            }

            if (infernalMobs) {
                try {
                    RedProtect.get().getServer().getPluginManager().registerEvents(new InfernalMobsHook(), RedProtect.get());
                    RedProtect.get().logger.info("InfernalMobs found. Hooked.");
                } catch (Exception ex) {
                    RedProtect.get().logger.warning("Your InfernalMobs version is NOT compatible and has no API. Look for FabioZumbi12's version on Github!");
                }
            }
            if (pvpm) {
                RedProtect.get().logger.info("PvPManager found. Hooked.");
            }
            if (essentials) {
                pless = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
                RedProtect.get().logger.info("Essentials found. Hooked.");
            }
            if (worldEdit) {
                RedProtect.get().logger.info("WorldEdit found. Hooked.");
            }
            if (worldguard) {
                RedProtect rp = RedProtect.get();
                if (rp.bukkitVersion >= 1130) {
                    worldGuardHelper = (WorldGuardHelper) Class.forName("br.net.fabiozumbi12.RedProtect.Bukkit.helpers.WorldGuardHelperLatest").newInstance();
                } else {
                    worldGuardHelper = (WorldGuardHelper) Class.forName("br.net.fabiozumbi12.RedProtect.Bukkit.helpers.WorldGuardHelper112").newInstance();
                }

                rp.logger.info("WorldGuard version " + worldGuardHelper.getWorldGuardMajorVersion() + " found. Hooked.");
            }
            if (actionBar) {
                RedProtect.get().logger.info("ActionBarAPI found. Hooked.");
            }
            if (bossBar) {
                RedProtect.get().logger.info("BossbarAPI found. Hooked.");
            }
            if (myPet) {
                RedProtect.get().getServer().getPluginManager().registerEvents(new MyPetHook(), RedProtect.get());
                RedProtect.get().logger.info("MyPet found. Hooked.");
            }
            if (mcMMO) {
                RedProtect.get().getServer().getPluginManager().registerEvents(new McMMOHook(), RedProtect.get());
                RedProtect.get().logger.info("mcMMo found. Hooked.");
            }
            if (skillAPI) {
                RedProtect.get().getServer().getPluginManager().registerEvents(new SkillAPIHook(), RedProtect.get());
                RedProtect.get().logger.info("SkillAPI found. Hooked.");
            }
            if (myChunk) {
                RedProtect.get().logger.success("MyChunk found. Ready to convert!");
                RedProtect.get().logger.warning("Use '/rp mychunkconvert' to start MyChunk conversion (This may cause lag during conversion)");
            }
            if (magicCarpet) {
                RedProtect.get().logger.info("MagicCarpet found. Hooked.");
            }
            if (simpleClans) {
                clanManager = SimpleClans.getInstance().getClanManager();
                RedProtect.get().logger.info("SimpleClans found. Hooked.");
            }
            if (placeHolderAPI) {
                new PAPIHook().register();
                RedProtect.get().logger.info("PlaceHolderAPI found. Hooked and registered some chat placeholders.");
            }
            if (fac) {
                RedProtect.get().getServer().getPluginManager().registerEvents(new FactionsHook(), RedProtect.get());
                RedProtect.get().logger.info("Factions found. Hooked.");
            }
            if (translationApi) {
                this.transAPI = new TransAPI();
                RedProtect.get().logger.info("TranslationAPI found. Hooked.");
            }
        } catch (Exception e) {
            CoreUtil.printJarVersion();
            e.printStackTrace();
        }
    }

    public void registerHooksLast() {
        try {
            Dyn = checkDyn();
            if (Dyn && RedProtect.get().config.configRoot().hooks.dynmap.enable) {
                RedProtect.get().logger.info("Dynmap found. Hooked.");
                RedProtect.get().logger.info("Loading dynmap markers...");
                dynmapHook = new DynmapHook((DynmapAPI) Bukkit.getPluginManager().getPlugin("dynmap"));
                RedProtect.get().getServer().getPluginManager().registerEvents(dynmapHook, RedProtect.get());
                RedProtect.get().logger.info("Dynmap markers loaded!");
            }
        } catch (Exception e) {
            CoreUtil.printJarVersion();
            e.printStackTrace();
        }
    }

    private boolean checkTAPI() {
        Plugin pTAPI = Bukkit.getPluginManager().getPlugin("TranslationAPI");
        return pTAPI != null && pTAPI.isEnabled();
    }

    private boolean checkWG() {
        Plugin pWG = Bukkit.getPluginManager().getPlugin("WorldGuard");
        return pWG != null && pWG.isEnabled();
    }

    private boolean checkIMobs() {
        Plugin pIM = Bukkit.getPluginManager().getPlugin("InfernalMobs");
        return pIM != null && pIM.isEnabled();
    }

    private boolean checkAB() {
        Plugin pAB = Bukkit.getPluginManager().getPlugin("ActionBarAPI");
        return pAB != null && pAB.isEnabled();
    }

    private boolean checkBM() {
        Plugin pBM = Bukkit.getPluginManager().getPlugin("BossBarAPI");
        return pBM != null && pBM.isEnabled();
    }

    private boolean checkDyn() {
        Plugin pDyn = Bukkit.getPluginManager().getPlugin("dynmap");
        return pDyn != null && pDyn.isEnabled();
    }

    private boolean checkEss() {
        Plugin pEss = Bukkit.getPluginManager().getPlugin("Essentials");
        return pEss != null && pEss.isEnabled();
    }

    private boolean checkFac() {
        Plugin p = Bukkit.getPluginManager().getPlugin("Factions");
        return p != null && p.isEnabled();
    }

    //check if plugin GriefPrevention is installed
    private boolean checkGriefPrev() {
        Plugin pGP = Bukkit.getPluginManager().getPlugin("GriefPrevention");
        return pGP != null && pGP.isEnabled();
    }

    //check if plugin mcMMo is installed
    private boolean checkMcMMo() {
        Plugin pMMO = Bukkit.getPluginManager().getPlugin("mcMMO");
        return pMMO != null && pMMO.isEnabled();
    }

    //check if plugin MagicCarpet is installed
    private boolean checkMagicCarpet() {
        Plugin pMC = Bukkit.getPluginManager().getPlugin("MagicCarpet");
        return pMC != null && pMC.isEnabled();
    }

    //check if plugin MyChunk is installed
    private boolean checkMyChunk() {
        Plugin pMC = Bukkit.getPluginManager().getPlugin("MyChunk");
        return pMC != null && pMC.isEnabled();
    }

    //check if plugin MyPet is installed
    private boolean checkMyPet() {
        Plugin pMP = Bukkit.getPluginManager().getPlugin("MyPet");
        return pMP != null && pMP.isEnabled();
    }

    private boolean checkPHAPI() {
        Plugin p = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
        return p != null && p.isEnabled();
    }

    //check if plugin PvPManager is installed
    private boolean checkPvPm() {
        Plugin pPvp = Bukkit.getPluginManager().getPlugin("PvPManager");
        return pPvp != null && pPvp.isEnabled();
    }

    private boolean checkSC() {
        Plugin p = Bukkit.getPluginManager().getPlugin("SimpleClans");
        return p != null && p.isEnabled();
    }

    //check if plugin SkillAPI is installed
    private boolean checkSkillAPI() {
        Plugin pSK = Bukkit.getPluginManager().getPlugin("SkillAPI");
        return pSK != null && pSK.isEnabled();
    }

    //check if plugin Vault is installed
    private boolean checkVault() {
        Plugin pVT = Bukkit.getPluginManager().getPlugin("Vault");
        return pVT != null && pVT.isEnabled();
    }

    private boolean checkWe() {
        Plugin pWe = Bukkit.getPluginManager().getPlugin("WorldEdit");
        if (pWe != null) {
            try {
                int v = Integer.parseInt(pWe.getDescription().getVersion().split("\\.")[0]);
                return (v >= 7) && pWe.isEnabled();
            } catch (Exception ignored) {
            }
        }
        return false;
    }
}
