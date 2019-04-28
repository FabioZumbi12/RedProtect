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

package br.net.fabiozumbi12.RedProtect.Bukkit.helpers;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import br.net.fabiozumbi12.RedProtect.Bukkit.ents.RPBukkitBlocks;
import br.net.fabiozumbi12.RedProtect.Bukkit.ents.RPBukkitEntities;
import br.net.fabiozumbi12.RedProtect.Bukkit.ents.TaskChain;
import br.net.fabiozumbi12.RedProtect.Bukkit.hooks.WEHook;
import br.net.fabiozumbi12.RedProtect.Core.helpers.CoreUtil;
import br.net.fabiozumbi12.RedProtect.Core.helpers.LogLevel;
import br.net.fabiozumbi12.RedProtect.Core.region.PlayerRegion;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.CommandException;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@SuppressWarnings("deprecation")
public class RedProtectUtil extends CoreUtil {
    public static final HashMap<String, HashMap<Location, Material>> pBorders = new HashMap<>();
    private static final HashMap<String, Integer> borderIds = new HashMap<>();
    private static final String pathData = RedProtect.get().getDataFolder() + File.separator + "data" + File.separator;
    public static boolean stopRegen = false;
    private static HashMap<String, String> cachedUUIDs = new HashMap<>();

    public static String dateNow() {
        return dateNow(RedProtect.get().config.configRoot().region_settings.date_format);
    }

    public static void saveResource(String nameVersioned, String nameOri, File saveTo) {
        try {
            InputStream isReader = RedProtect.class.getResourceAsStream(nameVersioned);
            if (isReader == null) isReader = RedProtect.class.getResourceAsStream(nameOri);

            FileOutputStream fos = new FileOutputStream(saveTo);
            while (isReader.available() > 0) {
                fos.write(isReader.read());
            }
            fos.close();
            isReader.close();
        } catch (IOException e) {
            CoreUtil.printJarVersion();
            e.printStackTrace();
        }
    }

    public static Set<Location> get4Points(Location min, Location max, int y) {
        Set<Location> locs = new HashSet<>();
        min.setY(y);
        max.setY(y);
        locs.add(min);
        locs.add(new Location(min.getWorld(), min.getX(), y, min.getZ() + (max.getZ() - min.getZ())));
        locs.add(max);
        locs.add(new Location(min.getWorld(), min.getX() + (max.getX() - min.getX()), y, min.getZ()));
        return locs;
    }

    public static boolean denyPotion(ItemStack result) {
        List<String> Pots = RedProtect.get().config.configRoot().server_protection.deny_potions;
        if (result != null && Pots.size() > 0 && (result.getType().name().contains("POTION") || result.getType().name().contains("TIPPED"))) {
            String potname = "";
            if (RedProtect.get().bukkitVersion >= 190) {
                PotionMeta pot = (PotionMeta) result.getItemMeta();
                potname = pot.getBasePotionData().getType().name();
            }
            if (RedProtect.get().bukkitVersion < 190) {
                potname = Potion.fromItemStack(result).getType().name();
            }
            return Pots.contains(potname);
        }
        return false;
    }

    public static boolean denyPotion(ItemStack result, Player p) {
        List<String> Pots = RedProtect.get().config.configRoot().server_protection.deny_potions;
        if (result != null && Pots.size() > 0 && (result.getType().name().contains("POTION") || result.getType().name().contains("TIPPED"))) {
            String potname = "";
            if (RedProtect.get().bukkitVersion >= 190) {
                PotionMeta pot = (PotionMeta) result.getItemMeta();
                potname = pot.getBasePotionData().getType().name();
            }
            if (RedProtect.get().bukkitVersion <= 180 && Potion.fromItemStack(result) != null) {
                potname = Potion.fromItemStack(result).getType().name();
            }
            if (Pots.contains(potname)) {
                RedProtect.get().lang.sendMessage(p, "playerlistener.denypotion");
                return true;
            }
        }
        return false;
    }

    private static boolean isSecure(Location loc) {
        Block b = loc.add(0, -1, 0).getBlock();
        return (!b.isLiquid() && !b.isEmpty()) || b.getType().name().contains("LAVA");
    }

    public static Location DenyExitPlayer(Player p, Location from, Location to, Region r) {
        Location setTo = to;
        Region rto = RedProtect.get().rm.getTopRegion(to);
        if (rto != r) {
            setTo = from;
            RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantregionexit");
        }
        return setTo;
    }

    public static Location DenyEnterPlayer(World wFrom, Location from, Location to, Region r, boolean checkSec) {
        Location setTo = to;
        for (int i = 0; i < r.getArea() + 10; i++) {
            Region r1 = RedProtect.get().rm.getTopRegion(wFrom, from.getBlockX() + i, from.getBlockY(), from.getBlockZ());
            Region r2 = RedProtect.get().rm.getTopRegion(wFrom, from.getBlockX() - i, from.getBlockY(), from.getBlockZ());
            Region r3 = RedProtect.get().rm.getTopRegion(wFrom, from.getBlockX(), from.getBlockY(), from.getBlockZ() + i);
            Region r4 = RedProtect.get().rm.getTopRegion(wFrom, from.getBlockX(), from.getBlockY(), from.getBlockZ() - i);
            Region r5 = RedProtect.get().rm.getTopRegion(wFrom, from.getBlockX() + i, from.getBlockY(), from.getBlockZ() + i);
            Region r6 = RedProtect.get().rm.getTopRegion(wFrom, from.getBlockX() - i, from.getBlockY(), from.getBlockZ() - i);
            if (r1 != r) {
                setTo = from.add(+i, 0, 0);
                break;
            }
            if (r2 != r) {
                setTo = from.add(-i, 0, 0);
                break;
            }
            if (r3 != r) {
                setTo = from.add(0, 0, +i);
                break;
            }
            if (r4 != r) {
                setTo = from.add(0, 0, -i);
                break;
            }
            if (r5 != r) {
                setTo = from.add(+i, 0, +i);
                break;
            }
            if (r6 != r) {
                setTo = from.add(-i, 0, -i);
                break;
            }
        }
        if (checkSec && !isSecure(setTo)) {
            wFrom.getBlockAt(setTo.clone().add(0, -1, 0)).setType(Material.GRASS);
        }
        return setTo;
    }

    public static void performCommand(final ConsoleCommandSender consoleCommandSender, final String command) {
        TaskChain.newChain().add(new TaskChain.GenericTask() {
            public void run() {
                RedProtect.get().getServer().dispatchCommand(consoleCommandSender, command);
            }
        }).execute();
    }

    public static boolean isBukkitBlock(Block b) {
        //check if is bukkit 1.8.8 blocks
        try {
            RPBukkitBlocks.valueOf(b.getType().name());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isBukkitEntity(Entity e) {
        //check if is bukkit 1.8.8 Entity
        try {
            RPBukkitEntities.valueOf(e.getType().name());
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private static void SaveToZipYML(File file, String ZippedFile, Set<YamlConfiguration> yml) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            ZipOutputStream zos = new ZipOutputStream(fos);
            if (ZippedFile == null) {
                for (YamlConfiguration y : yml) {
                    try {
                        ZipEntry e = new ZipEntry(y.getKeys(false).stream().findFirst().get() + ".yml");
                        zos.putNextEntry(e);
                        byte[] data = y.saveToString().getBytes();
                        zos.write(data, 0, data.length);
                        zos.closeEntry();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            } else {
                ZipEntry e = new ZipEntry(ZippedFile);
                zos.putNextEntry(e);
                for (YamlConfiguration y : yml) {
                    byte[] data = y.saveToString().getBytes();
                    zos.write(data, 0, data.length);
                }
                zos.closeEntry();
            }
            zos.close();
        } catch (Exception e) {
            CoreUtil.printJarVersion();
            e.printStackTrace();
        }
    }

    public static File genFileName(String Path, Boolean isBackup) {
        return genFileName(Path, isBackup, RedProtect.get().config.configRoot().flat_file.max_backups, dateNow());
    }

    /**
     * Generate a friendly and unique name for a region based on player name.
     *
     * @param p     Player
     * @param World World
     * @return Name of region
     */
    public static String nameGen(String p, String World) {
        String rname;
        World w = RedProtect.get().getServer().getWorld(World);
        int i = 0;
        while (true) {
            int is = String.valueOf(i).length();
            if (p.length() > 13) {
                rname = p.substring(0, 14 - is) + "_" + i;
            } else {
                rname = p + "_" + i;
            }
            if (RedProtect.get().rm.getRegion(rname, w) == null) {
                break;
            }
            ++i;
        }
        return rname;
    }

    //TODO read all db
    public static void ReadAllDB(Set<Region> regions) {
        int purged = 0;
        int sell = 0;
        int cfm = 0;
        int skipped = 0;
        int delay = 0;
        Date now = null;
        SimpleDateFormat dateformat = new SimpleDateFormat(RedProtect.get().config.configRoot().region_settings.date_format);

        try {
            now = dateformat.parse(dateNow());
        } catch (ParseException e1) {
            RedProtect.get().logger.severe("The 'date-format' don't match with date 'now'!!");
        }

        for (Region region : regions) {
            region.updateSigns();
            boolean serverRegion = false;

            if (region.isLeader(RedProtect.get().config.configRoot().region_settings.default_leader)) {
                serverRegion = true;
                region.setDate(dateNow());
            }

            //purge regions
            if (RedProtect.get().config.configRoot().purge.enabled && !serverRegion) {
                Date regiondate = null;
                try {
                    regiondate = dateformat.parse(region.getDate());
                } catch (ParseException e) {
                    RedProtect.get().logger.severe("The 'date-format' don't match with region date!!");
                    CoreUtil.printJarVersion();
            e.printStackTrace();
                }
                long days = TimeUnit.DAYS.convert(now.getTime() - regiondate.getTime(), TimeUnit.MILLISECONDS);

                boolean ignore = false;
                for (String play : RedProtect.get().config.configRoot().purge.ignore_regions_from_players) {
                    if (region.isLeader(PlayerToUUID(play)) || region.isAdmin(PlayerToUUID(play))) {
                        ignore = true;
                        break;
                    }
                }

                if (!ignore && days > RedProtect.get().config.configRoot().purge.remove_oldest) {
                    if (RedProtect.get().hooks.worldEdit && RedProtect.get().config.configRoot().purge.regen.enable) {
                        if (region.getArea() <= RedProtect.get().config.configRoot().purge.regen.max_area_regen) {
                            WEHook.regenRegion(region, Bukkit.getWorld(region.getWorld()), region.getMaxLocation(), region.getMinLocation(), delay, null, true);
                            delay = delay + 10;
                        } else {
                            skipped++;
                            continue;
                        }
                    } else {
                        RedProtect.get().rm.remove(region, RedProtect.get().getServer().getWorld(region.getWorld()));
                        purged++;
                        RedProtect.get().logger.warning("Purging " + region.getName() + " - Days: " + days);
                    }
                    continue;
                }
            }

            //sell rergions
            if (RedProtect.get().config.configRoot().sell.enabled && !serverRegion) {
                Date regiondate = null;
                try {
                    regiondate = dateformat.parse(region.getDate());
                } catch (ParseException e) {
                    RedProtect.get().logger.severe("The 'date-format' don't match with region date!!");
                    CoreUtil.printJarVersion();
            e.printStackTrace();
                }
                long days = TimeUnit.DAYS.convert(now.getTime() - regiondate.getTime(), TimeUnit.MILLISECONDS);

                boolean ignore = false;
                for (String play : RedProtect.get().config.configRoot().sell.ignore_regions_from_players) {
                    if (region.isLeader(PlayerToUUID(play)) || region.isAdmin(PlayerToUUID(play))) {
                        ignore = true;
                        break;
                    }
                }

                if (!ignore && days > RedProtect.get().config.configRoot().sell.sell_oldest) {
                    RedProtect.get().logger.warning("Selling " + region.getName() + " - Days: " + days);
                    EconomyManager.putToSell(region, RedProtect.get().config.configRoot().region_settings.default_leader, EconomyManager.getRegionValue(region));
                    sell++;
                    RedProtect.get().rm.saveAll(false);
                    continue;
                }
            }

            if (RedProtect.get().hooks.simpleClans) {
                //remove deleted clans from regions
                if (region.flagExists("clan") && !RedProtect.get().hooks.clanManager.isClan(region.getFlagString("clan"))) {
                    region.setFlag(Bukkit.getConsoleSender(), "clan", "");
                }
            }

            //filter name
            String rname = RedProtectUtil.setName(region.getName());
            if (rname.length() < 4) {
                rname = RedProtectUtil.nameGen(region.getLeaders().stream().findFirst().get().getPlayerName(), region.getWorld());
                RedProtect.get().rm.renameRegion(rname, region);
                cfm++;
            }
        }

        if (delay > 0) {
            RedProtect.get().logger.warning("&c> There's " + delay / 10 + " regions to be regenerated at 2 regions/second.");
            if (RedProtect.get().config.configRoot().purge.regen.enable_whitelist_regen) {
                Bukkit.getServer().setWhitelist(true);
                RedProtect.get().logger.warning("&eEnabled whitelist until regen!");
            }
        }

        if (cfm > 0) {
            RedProtect.get().logger.success("[" + cfm + "] Region names conformed!");
        }

        if (skipped > 0) {
            RedProtect.get().logger.success(skipped + " regions skipped due to max size limit to regen!");
        }

        if (purged > 0) {
            RedProtect.get().logger.success("Purged a total of &6" + purged + "&a regions!");
        }

        if (sell > 0) {
            RedProtect.get().logger.success("Put to sell a total of &6" + sell + "&a regions!");
        }
        regions.clear();
    }

    public static String PlayerToUUID(String PlayerName) {
        if (PlayerName == null || PlayerName.equals("")) {
            return null;
        }

        //check if is already UUID
        if (isUUIDs(PlayerName) || isDefaultServer(PlayerName) || (PlayerName.startsWith("[") && PlayerName.endsWith("]"))) {
            return PlayerName;
        }

        if (cachedUUIDs.containsValue(PlayerName)) {
            return cachedUUIDs.entrySet().stream().filter(e -> e.getValue().equalsIgnoreCase(PlayerName)).findFirst().get().getKey();
        }

        String uuid = PlayerName;

        try {
            OfflinePlayer offp = RedProtect.get().getServer().getOfflinePlayer(PlayerName);
            uuid = offp.getUniqueId().toString();
        } catch (IllegalArgumentException e) {
            Player onp = RedProtect.get().getServer().getPlayer(PlayerName);
            if (onp != null) {
                uuid = onp.getUniqueId().toString();
            }
        }

        cachedUUIDs.put(uuid, PlayerName);
        return uuid;
    }

    public static String UUIDtoPlayer(String uuid) {
        if (uuid == null) {
            return null;
        }

        //check if is UUID
        if (isDefaultServer(uuid) || !isUUIDs(uuid)) {
            return uuid;
        }

        if (cachedUUIDs.containsKey(uuid)) {
            return cachedUUIDs.get(uuid);
        }

        String PlayerName = uuid;
        UUID uuids;

        try {
            uuids = UUID.fromString(uuid);
            OfflinePlayer offp = RedProtect.get().getServer().getOfflinePlayer(uuids);
            PlayerName = offp.getName();
        } catch (IllegalArgumentException e) {
            Player onp = RedProtect.get().getServer().getPlayer(uuid);
            if (onp != null) {
                PlayerName = onp.getName();
            }
        }
        if (PlayerName == null) {
            PlayerName = MojangUUIDs.getName(uuid);
        }

        cachedUUIDs.put(uuid, PlayerName);
        return PlayerName;
    }

    public static boolean isDefaultServer(String check) {
        return check.equalsIgnoreCase(RedProtect.get().config.configRoot().region_settings.default_leader);
    }

    private static void fixdbFlags(YamlConfiguration db, String rname) {
        if (db.contains(rname + ".flags.mobs")) {
            db.set("spawn-monsters", db.get(rname + ".flags.mobs"));
            db.set(rname + ".flags.mobs", null);
        }
        if (db.contains(rname + ".flags.spawnpassives")) {
            db.set("spawn-animals", db.get(rname + ".flags.spawnpassives"));
            db.set(rname + ".flags.spawnpassives", null);
        }
    }

    public static boolean mysqlToFile() {
        HashMap<String, Region> regions = new HashMap<>();
        int saved = 1;

        try {
            Connection dbcon = DriverManager.getConnection("jdbc:mysql://" + RedProtect.get().config.configRoot().mysql.host + "/" + RedProtect.get().config.configRoot().mysql.db_name + "?autoReconnect=true", RedProtect.get().config.configRoot().mysql.user_name, RedProtect.get().config.configRoot().mysql.user_pass);

            for (World world : Bukkit.getWorlds()) {
                String tableName = RedProtect.get().config.configRoot().mysql.table_prefix + world.getName();
                PreparedStatement st = dbcon.prepareStatement("SELECT * FROM `" + tableName + "` WHERE world=?");
                st.setString(1, world.getName());
                ResultSet rs = st.executeQuery();
                while (rs.next()) {
                    Set<PlayerRegion<String, String>> leaders = new HashSet<>();
                    Set<PlayerRegion<String, String>> admins = new HashSet<>();
                    Set<PlayerRegion<String, String>> members = new HashSet<>();
                    HashMap<String, Object> flags = new HashMap<>();

                    int maxMbrX = rs.getInt("maxMbrX");
                    int minMbrX = rs.getInt("minMbrX");
                    int maxMbrZ = rs.getInt("maxMbrZ");
                    int minMbrZ = rs.getInt("minMbrZ");
                    int maxY = rs.getInt("maxY");
                    int minY = rs.getInt("minY");
                    int prior = rs.getInt("prior");
                    String rname = rs.getString("name");
                    String date = rs.getString("date");
                    String wel = rs.getString("wel");
                    long value = rs.getLong("value");

                    Location tppoint = null;
                    if (rs.getString("tppoint") != null && !rs.getString("tppoint").equalsIgnoreCase("")) {
                        String[] tpstring = rs.getString("tppoint").split(",");
                        tppoint = new Location(world, Double.parseDouble(tpstring[0]), Double.parseDouble(tpstring[1]), Double.parseDouble(tpstring[2]),
                                Float.parseFloat(tpstring[3]), Float.parseFloat(tpstring[4]));
                    }

                    for (String member : rs.getString("members").split(", ")) {
                        if (member.length() > 0) {
                            String[] p = member.split("@");
                            members.add(new PlayerRegion<>(p[0], p.length == 2 ? p[1] : p[0]));
                        }
                    }
                    for (String admin : rs.getString("admins").split(", ")) {
                        if (admin.length() > 0) {
                            String[] p = admin.split("@");
                            admins.add(new PlayerRegion<>(p[0], p.length == 2 ? p[1] : p[0]));
                        }
                    }
                    for (String leader : rs.getString("leaders").split(", ")) {
                        if (leader.length() > 0) {
                            String[] p = leader.split("@");
                            leaders.add(new PlayerRegion<>(p[0], p.length == 2 ? p[1] : p[0]));
                        }
                    }

                    for (String flag : rs.getString("flags").split(",")) {
                        String key = flag.split(":")[0];
                        String replace = key + ":";
                        if (replace.length() <= flag.length()) {
                            flags.put(key, parseObject(flag.substring(replace.length())));
                        }
                    }
                    Region newr = new Region(rname, admins, members, leaders, maxMbrX, minMbrX, maxMbrZ, minMbrZ, minY, maxY, flags, wel, prior, world.getName(), date, value, tppoint, true);
                    regions.put(rname, newr);
                }
                st.close();
                rs.close();

                //write to yml
                YamlConfiguration fileDB = new YamlConfiguration();
                File datf = new File(pathData, "data_" + world.getName() + ".yml");
                Set<YamlConfiguration> yamls = new HashSet<>();
                for (Region r : regions.values()) {
                    if (r.getName() == null) {
                        continue;
                    }

                    if (RedProtect.get().config.configRoot().flat_file.region_per_file) {
                        if (!r.toSave()) {
                            continue;
                        }
                        fileDB = new YamlConfiguration();
                        datf = new File(pathData, world.getName() + File.separator + r.getName() + ".yml");
                    }

                    addProps(fileDB, r);
                    saved++;

                    if (RedProtect.get().config.configRoot().flat_file.region_per_file) {
                        yamls.add(fileDB);
                        saveYaml(fileDB, datf);
                        r.setToSave(false);
                    }
                }

                if (!RedProtect.get().config.configRoot().flat_file.region_per_file) {
                    saveYaml(fileDB, datf);
                } else {
                    //remove deleted regions
                    File wfolder = new File(pathData + world.getName());
                    if (wfolder.exists()) {
                        File[] listOfFiles = new File(pathData + world.getName()).listFiles();
                        if (listOfFiles != null) {
                            for (File region : listOfFiles) {
                                if (region.isFile() && !regions.containsKey(region.getName().replace(".yml", ""))) {
                                    region.delete();
                                }
                            }
                        }
                    }
                }

                //try backup
                if (!RedProtect.get().config.configRoot().flat_file.region_per_file) {
                    backupRegions(Collections.singleton(fileDB), world.getName(), "data_" + world + ".yml");
                } else {
                    backupRegions(yamls, world.getName(), null);
                }
            }
            dbcon.close();

            if (saved > 0) {
                RedProtect.get().logger.success((saved - 1) + " regions converted to Yml with success!");
            }
        } catch (SQLException e) {
            CoreUtil.printJarVersion();
            e.printStackTrace();
        }

        return true;
    }

    public static void backupRegions(Set<YamlConfiguration> fileDB, String world, String saveFile) {
        if (!RedProtect.get().config.configRoot().flat_file.backup || fileDB.isEmpty()) {
            return;
        }

        File bfolder = new File(pathData + "backups" + File.separator);
        if (!bfolder.exists()) {
            bfolder.mkdir();
        }

        File folder = new File(pathData + "backups" + File.separator + world + File.separator);
        if (!folder.exists()) {
            folder.mkdir();
            RedProtect.get().logger.info("Created folder: " + folder.getPath());
        }

        //Save backup
        if (genFileName(folder.getPath() + File.separator, true) != null) {
            SaveToZipYML(genFileName(folder.getPath() + File.separator, true), saveFile, fileDB);
        }
    }

    public static boolean fileToMysql() throws Exception {
        if (!RedProtect.get().config.configRoot().file_type.equalsIgnoreCase("file")) {
            return false;
        }
        RedProtect.get().rm.saveAll(false);

        initMysql();//Create tables
        int counter = 1;

        for (World world : Bukkit.getWorlds()) {

            String dbname = RedProtect.get().config.configRoot().mysql.db_name;
            String url = "jdbc:mysql://" + RedProtect.get().config.configRoot().mysql.host + "/";
            String tableName = RedProtect.get().config.configRoot().mysql.table_prefix + world.getName();

            Connection dbcon = DriverManager.getConnection(url + dbname, RedProtect.get().config.configRoot().mysql.user_name, RedProtect.get().config.configRoot().mysql.user_pass);

            for (Region r : RedProtect.get().rm.getRegionsByWorld(world)) {
                if (!regionExists(dbcon, r.getName(), tableName)) {
                    try {
                        PreparedStatement st = dbcon.prepareStatement("INSERT INTO `" + tableName + "` (name,leaders,admins,members,maxMbrX,minMbrX,maxMbrZ,minMbrZ,minY,maxY,centerX,centerZ,date,wel,prior,world,value,tppoint,candelete,flags) "
                                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
                        st.setString(1, r.getName());
                        st.setString(2, r.getLeaders().toString().replace("[", "").replace("]", ""));
                        st.setString(3, r.getAdmins().toString().replace("[", "").replace("]", ""));
                        st.setString(4, r.getMembers().toString().replace("[", "").replace("]", ""));
                        st.setInt(5, r.getMaxMbrX());
                        st.setInt(6, r.getMinMbrX());
                        st.setInt(7, r.getMaxMbrZ());
                        st.setInt(8, r.getMinMbrZ());
                        st.setInt(9, r.getMinY());
                        st.setInt(10, r.getMaxY());
                        st.setInt(11, r.getCenterX());
                        st.setInt(12, r.getCenterZ());
                        st.setString(13, r.getDate());
                        st.setString(14, r.getWelcome());
                        st.setInt(15, r.getPrior());
                        st.setString(16, r.getWorld());
                        st.setLong(17, r.getValue());
                        st.setString(18, r.getTPPointString());
                        st.setInt(19, r.canDelete() ? 1 : 0);
                        st.setString(20, r.getFlagStrings());

                        st.executeUpdate();
                        st.close();
                        counter++;
                    } catch (SQLException e) {
                        CoreUtil.printJarVersion();
            e.printStackTrace();
                    }
                }
            }
            dbcon.close();
        }
        if (counter > 0) {
            RedProtect.get().logger.success((counter - 1) + " regions converted to Mysql with success!");
        }
        return true;
    }

    private static void initMysql() throws Exception {
        for (World world : Bukkit.getWorlds()) {

            String url = "jdbc:mysql://" + RedProtect.get().config.configRoot().mysql.host + "/";
            String reconnect = "?autoReconnect=true";
            String tableName = RedProtect.get().config.configRoot().mysql.table_prefix + world.getName();

            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException e2) {
                RedProtect.get().logger.severe("Couldn't find the driver for MySQL! com.mysql.jdbc.Driver.");
                return;
            }
            PreparedStatement st = null;
            try {
                if (!checkTableExists(tableName)) {
                    //create db
                    Connection con = DriverManager.getConnection(url + RedProtect.get().config.configRoot().mysql.table_prefix + reconnect, RedProtect.get().config.configRoot().mysql.user_name, RedProtect.get().config.configRoot().mysql.user_pass);
                    st = con.prepareStatement("CREATE TABLE `" + tableName + "` (name varchar(20) PRIMARY KEY NOT NULL, leaders longtext, admins longtext, members longtext, maxMbrX int, minMbrX int, maxMbrZ int, minMbrZ int, centerX int, centerZ int, minY int, maxY int, date varchar(10), wel longtext, prior int, world varchar(100), value Long not null, tppoint mediumtext, flags longtext, candelete tinyint(1)) CHARACTER SET utf8 COLLATE utf8_general_ci");
                    st.executeUpdate();
                    st.close();
                    st = null;
                    RedProtect.get().logger.info("Created table: " + tableName + "!");
                }
                addNewColumns(tableName);
            } catch (CommandException e3) {
                RedProtect.get().logger.severe("Couldn't connect to mysql! Make sure you have mysql turned on and installed properly, and the service is started.");
                throw new Exception("Couldn't connect to mysql!");
            } catch (SQLException e) {
                CoreUtil.printJarVersion();
            e.printStackTrace();
                RedProtect.get().logger.severe("There was an error while parsing SQL, redProtect will still with actual DB setting until you change the connection options or check if a Mysql service is running. Use /rp reload to try again");
            } finally {
                if (st != null) {
                    st.close();
                }
            }
        }
    }

    private static void addNewColumns(String tableName) {
        try {
            String url = "jdbc:mysql://" + RedProtect.get().config.configRoot().mysql.host + "/";
            Connection con = DriverManager.getConnection(url + RedProtect.get().config.configRoot().mysql.db_name, RedProtect.get().config.configRoot().mysql.user_name, RedProtect.get().config.configRoot().mysql.user_pass);
            DatabaseMetaData md = con.getMetaData();
            ResultSet rs = md.getColumns(null, null, tableName, "candelete");
            if (!rs.next()) {
                PreparedStatement st = con.prepareStatement("ALTER TABLE `" + tableName + "` ADD `candelete` tinyint(1) NOT NULL default '1'");
                st.executeUpdate();
            }
            rs.close();
            con.close();
        } catch (SQLException e) {
            CoreUtil.printJarVersion();
            e.printStackTrace();
        }
    }

    private static boolean regionExists(Connection dbcon, String name, String tableName) {
        int total = 0;
        try {
            PreparedStatement st = dbcon.prepareStatement("SELECT COUNT(*) FROM `" + tableName + "` WHERE name = ?");
            st.setString(1, name);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                total = rs.getInt("COUNT(*)");
            }
            st.close();
            rs.close();
        } catch (SQLException e) {
            CoreUtil.printJarVersion();
            e.printStackTrace();
        }
        return total > 0;
    }

    private static boolean checkTableExists(String tableName) {
        try {
            RedProtect.get().logger.debug(LogLevel.DEFAULT, "Checking if table exists... " + tableName);
            Connection con = DriverManager.getConnection("jdbc:mysql://" + RedProtect.get().config.configRoot().mysql.host + "/" + RedProtect.get().config.configRoot().mysql.db_name, RedProtect.get().config.configRoot().mysql.user_name, RedProtect.get().config.configRoot().mysql.user_pass);
            DatabaseMetaData meta = con.getMetaData();
            ResultSet rs = meta.getTables(null, null, tableName, null);
            if (rs.next()) {
                con.close();
                rs.close();
                return true;
            }
            con.close();
            rs.close();
        } catch (SQLException e) {
            CoreUtil.printJarVersion();
            e.printStackTrace();
        }
        return false;
    }

    public static void startFlagChanger(final String r, final String flag, final Player p) {
        RedProtect.get().changeWait.add(r + flag);
        Bukkit.getScheduler().scheduleSyncDelayedTask(RedProtect.get(), () -> {
            /*if (p != null && p.isOnline()){
                    RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("gui.needwait.ready").replace("{flag}", flag));
                }*/
            RedProtect.get().changeWait.remove(r + flag);
        }, RedProtect.get().config.configRoot().flags_configuration.change_flag_delay.seconds * 20);
    }

    public static int getUpdatedPrior(Region region) {
        int regionarea = region.getArea();
        int prior = region.getPrior();
        Region topRegion = RedProtect.get().rm.getTopRegion(RedProtect.get().getServer().getWorld(region.getWorld()), region.getCenterX(), region.getCenterY(), region.getCenterZ());
        Region lowRegion = RedProtect.get().rm.getLowRegion(RedProtect.get().getServer().getWorld(region.getWorld()), region.getCenterX(), region.getCenterY(), region.getCenterZ());

        if ((topRegion != null && topRegion.getID().equals(region.getID())) || (lowRegion != null && lowRegion.getID().equals(region.getID()))) {
            return prior;
        }

        if (lowRegion != null) {
            if (regionarea > lowRegion.getArea()) {
                prior = lowRegion.getPrior() - 1;
            } else if (regionarea < lowRegion.getArea() && regionarea < topRegion.getArea()) {
                prior = topRegion.getPrior() + 1;
            } else if (regionarea < topRegion.getArea()) {
                prior = topRegion.getPrior() + 1;
            }
        }
        return prior;
    }

    /**
     * Show the border of region for defined seconds.
     *
     * @param p    Player.
     * @param locs {@code List<Location>}.
     */
    public static void addBorder(final Player p, Set<Location> locs) {
        final World w = p.getWorld();
        boolean msg = true;
        if (pBorders.containsKey(p.getName())) {
            for (Location loc : pBorders.get(p.getName()).keySet()) {
                w.getBlockAt(loc).setType(pBorders.get(p.getName()).get(loc));
            }
            if (borderIds.containsKey(p.getName())) {
                Bukkit.getScheduler().cancelTask(borderIds.get(p.getName()));
                borderIds.remove(p.getName());
            }
            pBorders.remove(p.getName());
            msg = false;
        }

        final HashMap<Location, Material> borderBlocks = new HashMap<>();

        for (Location loc : locs) {
            loc.setY(p.getLocation().getBlockY());
            Block b = w.getBlockAt(loc);
            if (b.isEmpty() || b.isLiquid()) {
                borderBlocks.put(b.getLocation(), b.getType());
                w.getBlockAt(loc).setType(RedProtect.get().config.getBorderMaterial());
            }
        }
        if (borderBlocks.isEmpty()) {
            RedProtect.get().lang.sendMessage(p, "cmdmanager.bordernospace");
        } else {
            if (msg) {
                RedProtect.get().lang.sendMessage(p, "cmdmanager.addingborder");
            }
            pBorders.put(p.getName(), borderBlocks);
            int taskid = Bukkit.getScheduler().scheduleSyncDelayedTask(RedProtect.get(), () -> {
                if (pBorders.containsKey(p.getName())) {
                    for (Location loc : pBorders.get(p.getName()).keySet()) {
                        w.getBlockAt(loc).setType(pBorders.get(p.getName()).get(loc));
                    }
                    borderIds.remove(p.getName());
                    pBorders.remove(p.getName());
                    RedProtect.get().lang.sendMessage(p, "cmdmanager.removingborder");
                }
            }, RedProtect.get().config.configRoot().region_settings.border.time_showing * 20);
            borderIds.put(p.getName(), taskid);
        }
    }

    public static int convertFromGP() {
        int claimed = 0;
        Collection<Claim> claims = GriefPrevention.instance.dataStore.getClaims();
        for (Claim claim : claims) {
            if (Bukkit.getWorlds().contains(claim.getGreaterBoundaryCorner().getWorld())) {
                World w = claim.getGreaterBoundaryCorner().getWorld();
                String pname = claim.getOwnerName().replace(" ", "_").toLowerCase();
                if (RedProtect.get().config.configRoot().online_mode && claim.ownerID != null) {
                    pname = claim.ownerID.toString();
                }
                Set<PlayerRegion<String, String>> leaders = new HashSet<>();
                leaders.add(new PlayerRegion<>(claim.ownerID != null ? claim.ownerID.toString() : pname, pname));
                Location newmin = claim.getGreaterBoundaryCorner();
                Location newmax = claim.getLesserBoundaryCorner();
                newmin.setY(0);
                newmax.setY(w.getMaxHeight());

                Region r = new Region(nameGen(claim.getOwnerName().replace(" ", "_").toLowerCase(), w.getName()), new HashSet<>(), new HashSet<>(), leaders,
                        newmin, newmax, RedProtect.get().config.getDefFlagsValues(), "GriefPrevention region", 0, w.getName(), dateNow(), 0, null, true);

                Region other = RedProtect.get().rm.getTopRegion(w, r.getCenterX(), r.getCenterY(), r.getCenterZ());
                if (other == null || !r.getWelcome().equals(other.getWelcome())) {
                    RedProtect.get().rm.add(r, w);
                    RedProtect.get().logger.debug(LogLevel.DEFAULT, "Region: " + r.getName());
                    claimed++;
                }
            }
        }
        return claimed;
    }

    public static String getTitleName(Region r) {
        String name = RedProtect.get().lang.get("gui.invflag").replace("{region}", r.getName());
        if (name.length() > 16) {
            name = name.substring(0, 16);
        }
        return name;
    }

    private static void saveYaml(YamlConfiguration fileDB, File file) {
        try {
            fileDB.save(file);
        } catch (IOException e) {
            RedProtect.get().logger.severe("Error during save database file");
            CoreUtil.printJarVersion();
            e.printStackTrace();
        }
    }

    public static Region loadRegion(YamlConfiguration fileDB, String rname, World world) {
        if (fileDB.getString(rname + ".name") == null) {
            return null;
        }
        int maxX = fileDB.getInt(rname + ".maxX");
        int maxZ = fileDB.getInt(rname + ".maxZ");
        int minX = fileDB.getInt(rname + ".minX");
        int minZ = fileDB.getInt(rname + ".minZ");
        int maxY = fileDB.getInt(rname + ".maxY", world.getMaxHeight());
        int minY = fileDB.getInt(rname + ".minY", 0);
        String name = fileDB.getString(rname + ".name");
        String serverName = RedProtect.get().config.configRoot().region_settings.default_leader;

        Set<PlayerRegion<String, String>> leaders = new HashSet<>(fileDB.getStringList(rname + ".leaders")).stream().map(s -> {
            String[] pi = s.split("@");
            String[] p = new String[]{pi[0], pi.length == 2 ? pi[1] : pi[0]};
            if (RedProtect.get().config.configRoot().online_mode && !RedProtectUtil.isUUIDs(p[0]) && !p[0].equalsIgnoreCase(serverName)) {
                String before = p[0];
                p[0] = RedProtectUtil.PlayerToUUID(p[0]);
                RedProtect.get().logger.success("Updated region " + rname + ", player &6" + before + " &ato &6" + p[0]);
            }
            return new PlayerRegion<>(p[0], p[1]);
        }).collect(Collectors.toSet());

        Set<PlayerRegion<String, String>> admins = new HashSet<>(fileDB.getStringList(rname + ".admins")).stream().map(s -> {
            String[] pi = s.split("@");
            String[] p = new String[]{pi[0], pi.length == 2 ? pi[1] : pi[0]};
            if (RedProtect.get().config.configRoot().online_mode && !RedProtectUtil.isUUIDs(p[0]) && !p[0].equalsIgnoreCase(serverName)) {
                String before = p[0];
                p[0] = RedProtectUtil.PlayerToUUID(p[0]);
                RedProtect.get().logger.success("Updated region " + rname + ", player &6" + before + " &ato &6" + p[0]);
            }
            return new PlayerRegion<>(p[0], p[1]);
        }).collect(Collectors.toSet());

        Set<PlayerRegion<String, String>> members = new HashSet<>(fileDB.getStringList(rname + ".members")).stream().map(s -> {
            String[] pi = s.split("@");
            String[] p = new String[]{pi[0], pi.length == 2 ? pi[1] : pi[0]};
            if (RedProtect.get().config.configRoot().online_mode && !RedProtectUtil.isUUIDs(p[0]) && !p[0].equalsIgnoreCase(serverName)) {
                String before = p[0];
                p[0] = RedProtectUtil.PlayerToUUID(p[0]);
                RedProtect.get().logger.success("Updated region " + rname + ", player &6" + before + " &ato &6" + p[0]);
            }
            return new PlayerRegion<>(p[0], p[1]);
        }).collect(Collectors.toSet());

        String welcome = fileDB.getString(rname + ".welcome", "");
        int prior = fileDB.getInt(rname + ".priority", 0);
        String date = fileDB.getString(rname + ".lastvisit", "");
        long value = fileDB.getLong(rname + ".value", 0);
        boolean candel = fileDB.getBoolean(rname + ".candelete", true);

        Location tppoint = null;
        if (!fileDB.getString(rname + ".tppoint", "").equalsIgnoreCase("")) {
            String[] tpstring = fileDB.getString(rname + ".tppoint").split(",");
            tppoint = new Location(world, Double.parseDouble(tpstring[0]), Double.parseDouble(tpstring[1]), Double.parseDouble(tpstring[2]),
                    Float.parseFloat(tpstring[3]), Float.parseFloat(tpstring[4]));
        }

        fixdbFlags(fileDB, rname);
        Region newr = new Region(name, admins, members, leaders, new int[]{minX, minX, maxX, maxX}, new int[]{minZ, minZ, maxZ, maxZ}, minY, maxY, prior, world.getName(), date, RedProtect.get().config.getDefFlagsValues(), welcome, value, tppoint, candel);

        for (String flag : RedProtect.get().config.getDefFlags()) {
            if (fileDB.get(rname + ".flags." + flag) != null) {
                newr.getFlags().put(flag, fileDB.get(rname + ".flags." + flag));
            } else {
                newr.getFlags().put(flag, RedProtect.get().config.getDefFlagsValues().get(flag));
            }
        }
        for (String flag : RedProtect.get().config.AdminFlags) {
            if (fileDB.get(rname + ".flags." + flag) != null) {
                newr.getFlags().put(flag, fileDB.get(rname + ".flags." + flag));
            }
        }
        return newr;
    }

    public static void addProps(YamlConfiguration fileDB, Region r) {
        String rname = r.getName();
        fileDB.createSection(rname);
        fileDB.set(rname + ".name", rname);
        fileDB.set(rname + ".lastvisit", r.getDate());
        fileDB.set(rname + ".admins", r.getAdmins().stream().map(t -> t.getUUID() + "@" + t.getPlayerName()).collect(Collectors.toList()));
        fileDB.set(rname + ".members", r.getMembers().stream().map(t -> t.getUUID() + "@" + t.getPlayerName()).collect(Collectors.toList()));
        fileDB.set(rname + ".leaders", r.getLeaders().stream().map(t -> t.getUUID() + "@" + t.getPlayerName()).collect(Collectors.toList()));
        fileDB.set(rname + ".priority", r.getPrior());
        fileDB.set(rname + ".welcome", r.getWelcome());
        fileDB.set(rname + ".world", r.getWorld());
        fileDB.set(rname + ".maxX", r.getMaxMbrX());
        fileDB.set(rname + ".maxZ", r.getMaxMbrZ());
        fileDB.set(rname + ".minX", r.getMinMbrX());
        fileDB.set(rname + ".minZ", r.getMinMbrZ());
        fileDB.set(rname + ".maxY", r.getMaxY());
        fileDB.set(rname + ".minY", r.getMinY());
        fileDB.set(rname + ".value", r.getValue());
        fileDB.set(rname + ".flags", r.getFlags());
        fileDB.set(rname + ".candelete", r.canDelete());

        Location loc = r.getTPPoint();
        if (loc != null) {
            int x = loc.getBlockX();
            int y = loc.getBlockY();
            int z = loc.getBlockZ();
            float yaw = loc.getYaw();
            float pitch = loc.getPitch();
            fileDB.set(rname + ".tppoint", x + "," + y + "," + z + "," + yaw + "," + pitch);
        } else {
            fileDB.set(rname + ".tppoint", "");
        }
    }

    public static int SingleToFiles() {
        int saved = 0;
        for (World w : Bukkit.getWorlds()) {
            Set<Region> regions = RedProtect.get().rm.getRegionsByWorld(w);
            for (Region r : regions) {
                YamlConfiguration fileDB = new YamlConfiguration();

                File f = new File(pathData + w.getName());
                if (!f.exists()) {
                    f.mkdir();
                }
                File wf = new File(pathData, w.getName() + File.separator + r.getName() + ".yml");

                saved++;
                addProps(fileDB, r);
                saveYaml(fileDB, wf);
            }

            File oldf = new File(pathData + "data_" + w.getName() + ".yml");
            if (oldf.exists()) {
                oldf.delete();
            }
        }

        if (!RedProtect.get().config.configRoot().flat_file.region_per_file) {
            RedProtect.get().config.configRoot().flat_file.region_per_file = true;
        }
        RedProtect.get().config.save();
        return saved;
    }

    public static int FilesToSingle() {
        int saved = 0;
        for (World w : Bukkit.getWorlds()) {
            File f = new File(pathData, "data_" + w.getName() + ".yml");
            Set<Region> regions = RedProtect.get().rm.getRegionsByWorld(w);
            YamlConfiguration fileDB = new YamlConfiguration();
            for (Region r : regions) {
                addProps(fileDB, r);
                saved++;
                File oldf = new File(pathData, w.getName() + File.separator + r.getName() + ".yml");
                if (oldf.exists()) {
                    oldf.delete();
                }
            }
            File oldf = new File(pathData, w.getName());
            if (oldf.exists()) {
                oldf.delete();
            }
            saveYaml(fileDB, f);
        }
        if (RedProtect.get().config.configRoot().flat_file.region_per_file) {
            RedProtect.get().config.configRoot().flat_file.region_per_file = false;
        }
        RedProtect.get().config.save();
        return saved;
    }

    public static boolean canBuildNear(Player p, Location loc) {
        if (RedProtect.get().config.configRoot().region_settings.deny_build_near <= 0) {
            return true;
        }
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        int radius = RedProtect.get().config.configRoot().region_settings.deny_build_near;

        for (int ix = x - radius; ix <= x + radius; ++ix) {
            for (int iy = y - radius; iy <= y + radius; ++iy) {
                for (int iz = z - radius; iz <= z + radius; ++iz) {
                    Region reg = RedProtect.get().rm.getTopRegion(new Location(p.getWorld(), ix, iy, iz));
                    if (reg != null && !reg.canBuild(p)) {
                        RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("blocklistener.cantbuild.nearrp").replace("{distance}", "" + radius));
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static int simuleTotalRegionSize(String player, Region r2) {
        int total = 0;
        int regs = 0;
        for (Location loc : r2.get4Points(r2.getCenterY())) {
            Map<Integer, Region> pregs = RedProtect.get().rm.getGroupRegion(loc);
            pregs.remove(r2.getPrior());
            Region other;
            if (pregs.size() > 0) {
                other = pregs.get(Collections.max(pregs.keySet()));
            } else {
                continue;
            }
            if (!r2.getID().equals(other.getID()) && r2.getPrior() > other.getPrior() && other.isLeader(player)) {
                regs++;
            }
        }
        if (regs != 4) {
            total += r2.getArea();
        }

        return total;
    }

    public static String regionNameConform(String regionName, Player p) {
        String pRName = UUIDtoPlayer(p.getName());
        if (regionName.equals("")) {
            int i = 0;
            regionName = StripName(pRName) + "_" + 0;
            while (RedProtect.get().rm.getRegion(regionName, p.getWorld()) != null) {
                ++i;
                regionName = StripName(pRName) + "_" + i;
            }
            if (regionName.length() > 16) {
                RedProtect.get().lang.sendMessage(p, "regionbuilder.autoname.error");
                return null;
            }
        }
        if (regionName.contains("@")) {
            p.sendMessage(RedProtect.get().lang.get("regionbuilder.regionname.invalid.charac").replace("{charac}", "@"));
            return null;
        }

        //region name conform
        regionName = regionName.replace("/", "|");
        if (RedProtect.get().rm.getRegion(regionName, p.getWorld()) != null) {
            RedProtect.get().lang.sendMessage(p, "regionbuilder.regionname.existis");
            return null;
        }
        if (regionName.length() < 3 || regionName.length() > 16) {
            RedProtect.get().lang.sendMessage(p, "regionbuilder.regionname.invalid");
            return null;
        }

        return regionName;
    }
}
