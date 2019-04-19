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

package br.net.fabiozumbi12.RedProtect.Bukkit;

import br.net.fabiozumbi12.RedProtect.Bukkit.API.RedProtectAPI;
import br.net.fabiozumbi12.RedProtect.Bukkit.commands.CommandHandler;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPConfig;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPLang;
import br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPLogger;
import br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPPermissionHandler;
import br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPUtil;
import br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPVHelper;
import br.net.fabiozumbi12.RedProtect.Bukkit.hooks.*;
import br.net.fabiozumbi12.RedProtect.Bukkit.listeners.*;
import br.net.fabiozumbi12.RedProtect.Bukkit.region.RegionManager;
import com.earth2me.essentials.Essentials;
import net.milkbowl.vault.economy.Economy;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import net.sacredlabyrinth.phaed.simpleclans.managers.ClanManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapAPI;

import java.io.File;
import java.util.*;

public class RedProtect extends JavaPlugin {
    static boolean McMMo;
    static boolean SkillAPI;
    private static RedProtect plugin;
    public final RPLogger logger = new RPLogger();
    public final List<String> openGuis = new ArrayList<>();
    public final List<String> confiemStart = new ArrayList<>();
    public final HashMap<String, List<String>> denyEnter = new HashMap<>();
    public File jarFile = null;
    public PluginDescriptionFile pdf;
    public boolean Update;
    public String UptVersion;
    public RegionManager rm;
    public List<String> changeWait = new ArrayList<>();
    public List<String> tpWait = new ArrayList<>();
    public HashMap<Player, String> alWait = new HashMap<>();
    public RPPermissionHandler ph;
    public HashMap<Player, Location> firstLocationSelections = new HashMap<>();
    public HashMap<Player, Location> secondLocationSelections = new HashMap<>();
    public boolean BossBar;
    public boolean MyChunk;
    public boolean MyPet;
    public boolean onlineMode;
    public boolean Mc;
    public boolean Vault;
    public boolean PvPm;
    public boolean Ess;
    public boolean GP;
    public boolean WE;
    //public boolean AWE;
    public boolean SC;
    //public boolean PLib;
    public ClanManager clanManager;
    public Essentials pless;
    public boolean Dyn;
    public RPDynmap dynmap;
    public Economy econ;
    public int version;
    public RPVHelper rpvhelper;
    public CommandHandler cmdHandler;
    private int autoSaveID;
    private RedProtectAPI rpAPI;
    public RPConfig cfgs;

    public static RedProtect get() {
        return plugin;
    }

    public RedProtectAPI getAPI() {
        return rpAPI;
    }

    public void onDisable() {
        shutDown();
    }

    public void onEnable() {
        try {
            plugin = this;
            jarFile = this.getFile();

            pdf = getDescription();

            ph = new RPPermissionHandler();
            rm = new RegionManager();

            //--- Init config, lang, listeners and flags
            startLoad();

            version = getBukkitVersion();
            logger.debug("Version String: " + version);

            if (version >= 180) {
                getServer().getPluginManager().registerEvents(new RPMine18(), this);
            }
            if (version >= 190) {
                getServer().getPluginManager().registerEvents(new RPMine19(), this);
            }

            if (version <= 1122) {
                rpvhelper = (RPVHelper) Class.forName("br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPVHelper112").newInstance();
            }
            if (version >= 1130) {
                rpvhelper = (RPVHelper) Class.forName("br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPVHelper113").newInstance();
            }

            logger.info("Loading API...");
            this.rpAPI = new RedProtectAPI();
            logger.info("API Loaded!");

            logger.clear("&4 _   _  _  &c _   _   _  _ _  _  _ _ _  __");
            logger.clear("&4|_| |_ | \\ &c|_| |_| | |  |  |_ |   |    /");
            logger.clear("&4| \\ |_ |_/ &c|   | \\ |_|  |  |_ |_  |   /");
            logger.clear("&a» " + pdf.getFullName() + " enabled");
            logger.clear("");

        } catch (Exception e) {
            e.printStackTrace();
            if (!cfgs.getString("file-type").equalsIgnoreCase("mysql")) {
                logger.severe("Error enabling RedProtect, plugin will shut down.");
                this.setEnabled(false);
            }
            getServer().setWhitelist(true);
            getServer().getOnlinePlayers().forEach(p -> p.kickPlayer("The server has been whitelisted due to an error while loading plugins!"));
            logger.severe("Due to an error in RedProtect loading, the whitelist has been turned on and every player has been kicked.");
            logger.severe("DO NOT LET ANYONE ENTER before fixing the problem, otherwise you risk losing protected regions.");
        }
    }

    private void registerHooks() {
        BossBar = checkBM();
        MyChunk = checkMyChunk();
        MyPet = checkMyPet();
        McMMo = checkMcMMo();
        Mc = checkMc();
        Vault = checkVault();
        SkillAPI = checkSkillAPI();
        PvPm = checkPvPm();
        Ess = checkEss();
        GP = checkGP();
        Dyn = checkDyn();
        WE = checkWe();
        //AWE = checkAWe();
        SC = checkSP();
        boolean fac = checkFac();
        //PLib = checkPLib();
        boolean placeHolderAPI = checkPHAPI();

        if (Vault) {
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                econ = rsp.getProvider();
                logger.info("Vault found. Hooked.");
            } else {
                logger.warning("Could not initialize Vault hook.");
                Vault = false;
            }
        }

       /* if (PLib) {
            logger.info("ProtocolLib found. Hiding Gui Flag item stats.");
        }*/
        if (PvPm) {
            logger.info("PvPManager found. Hooked.");
        }
        if (Ess) {
            pless = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
            logger.info("Essentials found. Hooked.");
        }
        if (WE) {
            logger.info("WorldEdit found. Hooked.");
        }
        /*if (AWE) {
            logger.info("AsyncWorldEdit found. Hooked.");
        }*/
        if (BossBar) {
            logger.info("BossbarAPI found. Hooked.");
        }
        if (MyPet) {
            getServer().getPluginManager().registerEvents(new MPListener(), this);
            logger.info("MyPet found. Hooked.");
        }
        if (McMMo) {
            getServer().getPluginManager().registerEvents(new McMMoListener(), this);
            logger.info("McMMo found. Hooked.");
        }
        if (SkillAPI) {
            getServer().getPluginManager().registerEvents(new SkillAPIListener(), this);
            logger.info("SkillAPI found. Hooked.");
        }
        if (MyChunk) {
            logger.sucess("MyChunk found. Ready to convert!");
            logger.warning("Use '/rp mychunkconvert' to start MyChunk conversion (This may cause lag during conversion)");
        }
        if (Mc) {
            logger.info("MagicCarpet found. Hooked.");
        }
        if (SC) {
            clanManager = SimpleClans.getInstance().getClanManager();
            logger.info("SimpleClans found. Hooked.");
        }
        if (Dyn && cfgs.getBool("hooks.dynmap.enabled")) {
            logger.info("Dynmap found. Hooked.");
            logger.info("Loading dynmap markers...");
            dynmap = new RPDynmap((DynmapAPI) Bukkit.getPluginManager().getPlugin("dynmap"));
            getServer().getPluginManager().registerEvents(dynmap, this);
            logger.info("Dynmap markers loaded!");
        }
        if (placeHolderAPI) {
            new RPPlaceHoldersAPI(this).hook();
            logger.info("PlaceHolderAPI found. Hooked and registered some chat placeholders.");
        }
        if (fac) {
            getServer().getPluginManager().registerEvents(new RPFactions(), this);
            logger.info("Factions found. Hooked.");
        }
    }

    public void reload() {
        try {
            //shutdown
            shutDown();

            //start
            startLoad();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startLoad() {
        cfgs = new RPConfig();
        RPLang.init();

        if (cfgs.getBool("purge.regen.whitelist-server-regen") && Bukkit.getServer().hasWhitelist()) {
            Bukkit.getServer().setWhitelist(false);
            RedProtect.get().logger.sucess("Whitelist disabled!");
        }

        // Set online mode
        onlineMode = cfgs.getBool("online-mode");

        logger.info("Registering commands...");
        cmdHandler = new CommandHandler(this);

        logger.info("Registering listeners...");
        getServer().getPluginManager().registerEvents(new RPGlobalListener(), this);
        getServer().getPluginManager().registerEvents(new RPBlockListener(), this);
        getServer().getPluginManager().registerEvents(new RPPlayerListener(), this);
        getServer().getPluginManager().registerEvents(new RPEntityListener(), this);
        getServer().getPluginManager().registerEvents(new RPWorldListener(), this);

        // Register hooks
        registerHooks();

        try {
            rm = new RegionManager();
            rm.loadAll();

            RPUtil.ReadAllDB(rm.getAllRegions());

            if (!cfgs.getString("file-type").equalsIgnoreCase("mysql")) {
                startAutoSave();
            }
            logger.info("There are " + rm.getTotalRegionsNum() + " regions on (" + cfgs.getString("file-type") + ") database!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void shutDown() {
        // Unregister commands
        cmdHandler.unregisterAll();

        // Save and unload all regions
        rm.saveAll(true);
        rm.unloadAll();

        // Close all open inventories/GUIs
        openGuis.clear();

        // Cancel tasks from bukkit scheduler and save logs
        Bukkit.getScheduler().cancelTasks(this);
        logger.saveLogs();

        // Unregister listeners
        logger.info("Unregistering listeners...");
        HandlerList.unregisterAll(this);

        logger.info(pdf.getFullName() + " turned off...");
    }

    public boolean denyEnterRegion(String rid, String player) {
        if (denyEnter.containsKey(player)) {
            if (denyEnter.get(player).contains(rid)) {
                return false;
            }
            List<String> regs = denyEnter.get(player);
            regs.add(rid);
            denyEnter.put(player, regs);
        } else {
            denyEnter.put(player, new LinkedList<>(Collections.singletonList(rid)));
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (denyEnter.containsKey(player)) {
                List<String> regs = denyEnter.get(player);
                regs.remove(rid);
                if (regs.isEmpty()) {
                    denyEnter.remove(player);
                } else {
                    denyEnter.put(player, regs);
                }
            }
        }, cfgs.getInt("region-settings.delay-after-kick-region") * 20);
        return true;
    }

    private int getBukkitVersion() {
        String name = Bukkit.getServer().getClass().getPackage().getName();
        String v = name.substring(name.lastIndexOf('.') + 1) + ".";
        String[] version = v.replace('_', '.').split("\\.");

        int lesserVersion = 0;
        try {
            lesserVersion = Integer.parseInt(version[2]);
        } catch (NumberFormatException ignored) {
        }
        return Integer.parseInt((version[0] + version[1]).substring(1) + lesserVersion);
    }

    private void startAutoSave() {
        Bukkit.getScheduler().cancelTask(autoSaveID);
        if (cfgs.getInt("flat-file.auto-save-interval-seconds") != 0) {
            logger.info("Auto-save Scheduler: Saving " + cfgs.getString("file-type") + " database every " + cfgs.getInt("flat-file.auto-save-interval-seconds") / 60 + " minutes!");

            autoSaveID = Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
                logger.debug("Auto-save Scheduler: Saving " + cfgs.getString("file-type") + " database!");
                rm.saveAll(cfgs.getBool("flat-file.backup-on-save"));
            }, cfgs.getInt("flat-file.auto-save-interval-seconds") * 20, cfgs.getInt("flat-file.auto-save-interval-seconds") * 20).getTaskId();

        } else {
            logger.info("Auto-save Scheduler: Disabled");
        }
    }

    //check if plugin GriefPrevention is installed
    private boolean checkGP() {
        Plugin pGP = Bukkit.getPluginManager().getPlugin("GriefPrevention");
        return pGP != null && pGP.isEnabled();
    }

    //check if plugin BossbarAPI is installed
    private boolean checkBM() {
        Plugin pBM = Bukkit.getPluginManager().getPlugin("BossBarAPI");
        return pBM != null && pBM.isEnabled();
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

    //check if plugin McMMo is installed
    private boolean checkMcMMo() {
        Plugin pMMO = Bukkit.getPluginManager().getPlugin("mcMMO");
        return pMMO != null && pMMO.isEnabled();
    }

    //check if plugin MagicCarpet is installed
    private boolean checkMc() {
        Plugin pMC = Bukkit.getPluginManager().getPlugin("MagicCarpet");
        return pMC != null && pMC.isEnabled();
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

    //check if plugin PvPManager is installed
    private boolean checkPvPm() {
        Plugin pPvp = Bukkit.getPluginManager().getPlugin("PvPManager");
        return pPvp != null && pPvp.isEnabled();
    }

    private boolean checkEss() {
        Plugin pEss = Bukkit.getPluginManager().getPlugin("Essentials");
        return pEss != null && pEss.isEnabled();
    }

    private boolean checkDyn() {
        Plugin pDyn = Bukkit.getPluginManager().getPlugin("dynmap");
        return pDyn != null && pDyn.isEnabled();
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

    private boolean checkAWe() {
        Plugin pAWe = Bukkit.getPluginManager().getPlugin("AsyncWorldEdit");
        return pAWe != null && pAWe.isEnabled();
    }

    private boolean checkSP() {
        Plugin p = Bukkit.getPluginManager().getPlugin("SimpleClans");
        return p != null && p.isEnabled();
    }

    private boolean checkPHAPI() {
        Plugin p = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
        return p != null && p.isEnabled();
    }

    private boolean checkFac() {
        Plugin p = Bukkit.getPluginManager().getPlugin("Factions");
        return p != null && p.isEnabled();
    }

    private boolean checkPLib() {
        Plugin p = Bukkit.getPluginManager().getPlugin("ProtocolLib");
        return p != null && p.isEnabled();
    }
}