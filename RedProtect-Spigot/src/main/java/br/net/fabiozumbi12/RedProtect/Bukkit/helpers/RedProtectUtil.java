/*
 * Copyright (c) 2012-2025 - @FabioZumbi12
 * Last Modified: 18/01/2025 15:59
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
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.command.CommandException;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.text.Normalizer;
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

    private final RedProtect plugin;

    public RedProtectUtil(RedProtect plugin) {
        this.plugin = plugin;
    }

    public String dateNow() {
        return dateNow(RedProtect.get().getConfigManager().configRoot().region_settings.date_format);
    }

    public boolean checkCrops(Block b, boolean breaking) {
        return (b.getBlockData() instanceof Ageable)
                || b.getType().equals(Material.PUMPKIN_STEM)
                || b.getType().equals(Material.MELON_STEM)
                || b.getType().toString().contains("CARROTS")
                || b.getType().toString().contains("_BERRIES")
                || b.getType().toString().contains("_BERRY")
                || b.getType().toString().contains("CROPS")
                || b.getType().toString().contains("SOIL")
                || (b.getType().toString().contains("FARMLAND") && !breaking)
                || b.getType().toString().contains("CHORUS_")
                || b.getType().toString().contains("BEETROOT_")
                || b.getType().toString().contains("BEETROOTS")
                || b.getType().toString().contains("SUGAR_CANE")
                || b.getType().toString().contains("WHEAT");
    }

    public void saveResource(String nameVersioned, String nameOri, File saveTo) {
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
            printJarVersion();
            e.printStackTrace();
        }
    }

    public boolean denyPotion(ItemStack result, World world) {
        List<String> Pots = RedProtect.get().getConfigManager().globalFlagsRoot().worlds.get(world.getName()).deny_potions;
        if (result != null && !Pots.isEmpty() && (result.getType().name().contains("POTION") || result.getType().name().contains("TIPPED"))) {
            PotionMeta pot = (PotionMeta) result.getItemMeta();
            String potname = "";
            try{
                potname = Objects.requireNonNull(pot).getBasePotionType().name();
            } catch (Exception ex){
                try{
                    potname = Objects.requireNonNull(pot).getBasePotionType().name();
                } catch (Exception ignored){}
            }
            return Pots.contains(potname);
        }
        return false;
    }

    public boolean denyPotion(ItemStack result, Player p) {
        List<String> Pots = RedProtect.get().getConfigManager().globalFlagsRoot().worlds.get(p.getWorld().getName()).deny_potions;
        if (result != null && !Pots.isEmpty() && (result.getType().name().contains("POTION") || result.getType().name().contains("TIPPED"))) {
            PotionMeta pot = (PotionMeta) result.getItemMeta();
            String potname = "";
            try{
                potname = Objects.requireNonNull(pot).getBasePotionType().name();
            } catch (Exception ex){
                try{
                    potname = Objects.requireNonNull(pot).getBasePotionType().name();
                } catch (Exception ignored){}
            }
            if (Pots.contains(potname)) {
                RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.denypotion");
                return true;
            }
        }
        return false;
    }

    public boolean isRealPlayer(Player p) {
        return Bukkit.getOnlinePlayers().stream().anyMatch(play -> play.equals(p));
    }

    private boolean isSecure(Location loc) {
        Block b = loc.add(0, -1, 0).getBlock();
        return (!b.isLiquid() && !b.isEmpty()) || b.getType().name().contains("LAVA");
    }

    public Location DenyExitPlayer(Player p, Location from, Location to, Region r) {
        Location setTo = to;
        Region rto = RedProtect.get().getRegionManager().getTopRegion(to);
        if (rto != r) {
            setTo = from;
            RedProtect.get().getLanguageManager().sendMessage(p, "playerlistener.region.cantregionexit");
        }
        return setTo;
    }

    public boolean isBypassBorder(Player p) {
        int diameter = (int) p.getWorld().getWorldBorder().getSize() / 2;
        int centerZ = (int) p.getWorld().getWorldBorder().getCenter().getZ();
        int centerX = (int) p.getWorld().getWorldBorder().getCenter().getX();
        int playerX = (int) p.getLocation().getX();
        int playerZ = (int) p.getLocation().getZ();
        double x = playerX - centerX;
        double z = playerZ - centerZ;
        return ((x > diameter || (-x) > diameter) || (z > diameter || (-z) > diameter));
    }

    public Location DenyEnterPlayer(World wFrom, Location from, Location to, Region r, boolean checkSec) {
        Location setTo = to;
        for (int i = 0; i < r.getArea() + 10; i++) {
            Region r1 = RedProtect.get().getRegionManager().getTopRegion(wFrom.getName(), from.getBlockX() + i, from.getBlockY(), from.getBlockZ());
            Region r2 = RedProtect.get().getRegionManager().getTopRegion(wFrom.getName(), from.getBlockX() - i, from.getBlockY(), from.getBlockZ());
            Region r3 = RedProtect.get().getRegionManager().getTopRegion(wFrom.getName(), from.getBlockX(), from.getBlockY(), from.getBlockZ() + i);
            Region r4 = RedProtect.get().getRegionManager().getTopRegion(wFrom.getName(), from.getBlockX(), from.getBlockY(), from.getBlockZ() - i);
            Region r5 = RedProtect.get().getRegionManager().getTopRegion(wFrom.getName(), from.getBlockX() + i, from.getBlockY(), from.getBlockZ() + i);
            Region r6 = RedProtect.get().getRegionManager().getTopRegion(wFrom.getName(), from.getBlockX() - i, from.getBlockY(), from.getBlockZ() - i);
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
            wFrom.getBlockAt(setTo.clone().add(0, -1, 0)).setType(Material.DIRT);
        }
        return setTo;
    }

    public void performCommand(final ConsoleCommandSender consoleCommandSender, final String command) {
        TaskChain.newChain().add(new TaskChain.GenericTask() {
            public void run() {
                RedProtect.get().getServer().dispatchCommand(consoleCommandSender, command);
            }
        }).execute();
    }

    public boolean isBukkitBlock(Block b) {
        //check if is bukkit 1.8.8 blocks
        try {
            RPBukkitBlocks.valueOf(b.getType().name());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isBukkitEntity(Entity e) {
        //check if is bukkit 1.8.8 Entity
        try {
            RPBukkitEntities.valueOf(e.getType().name());
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private void SaveToZipYML(File file, String ZippedFile, Set<YamlConfiguration> yml) {
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
            printJarVersion();
            e.printStackTrace();
        }
    }

    public File genFileName(String Path, Boolean isBackup) {
        return genFileName(Path, isBackup, RedProtect.get().getConfigManager().configRoot().flat_file.max_backups, dateNow());
    }

    //TODO read all db
    public void ReadAllDB(Set<Region> regions) {
        int purged = 0;
        int sell = 0;
        int skipped = 0;
        int delay = 0;
        Date now = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat(RedProtect.get().getConfigManager().configRoot().region_settings.date_format);

        try {
            now = dateFormat.parse(dateNow());
        } catch (ParseException e1) {
            RedProtect.get().logger.severe("The 'date-format' don't match with date 'now'!!");
        }

        for (Region region : regions) {
            region.updateSigns();
            boolean serverRegion = false;

            if (region.isLeader(RedProtect.get().getConfigManager().configRoot().region_settings.default_leader)) {
                serverRegion = true;
                region.setDate(dateNow());
            }

            //purge regions
            if (RedProtect.get().getConfigManager().configRoot().purge.enabled && !serverRegion && region.canPurge() && region.isOnTop()) {
                Date regionDate = null;
                try {
                    regionDate = dateFormat.parse(region.getDate());
                } catch (ParseException e) {
                    RedProtect.get().logger.severe("The 'date-format' don't match with region date!!");
                    printJarVersion();
                    e.printStackTrace();
                }
                long days = TimeUnit.DAYS.convert(now.getTime() - regionDate.getTime(), TimeUnit.MILLISECONDS);

                boolean ignore = false;
                for (String play : RedProtect.get().getConfigManager().configRoot().purge.ignore_regions_from_players) {
                    if (region.isLeader(play) || region.isAdmin(play)) {
                        ignore = true;
                        break;
                    }
                }

                if (!ignore && days > RedProtect.get().getConfigManager().configRoot().purge.remove_oldest) {
                    // Execute commands
                    String c = "";
                    try {
                        if (!RedProtect.get().getConfigManager().configRoot().purge.execute_commands.isEmpty()) {
                            RedProtect.get().getConfigManager().configRoot().purge.execute_commands.forEach(cmd -> {
                                cmd = cmd
                                        .replace("{world}", region.getWorld())
                                        .replace("{region}", region.getName());
                                if (cmd.contains("{leader}")) {
                                    final String[] cmdf = {cmd};
                                    region.getLeaders().forEach(l -> {
                                        cmdf[0] = cmdf[0].replace("{leader}", l.getPlayerName());
                                        performCommand(Bukkit.getConsoleSender(), cmdf[0]);
                                    });
                                } else if (cmd.contains("{admin}")) {
                                    final String[] cmdf = {cmd};
                                    region.getAdmins().forEach(a -> {
                                        cmdf[0] = cmdf[0].replace("{admin}", a.getPlayerName());
                                        performCommand(Bukkit.getConsoleSender(), cmdf[0]);
                                    });
                                } else if (cmd.contains("{member}")) {
                                    final String[] cmdf = {cmd};
                                    region.getMembers().forEach(m -> {
                                        cmdf[0] = cmdf[0].replace("{member}", m.getPlayerName());
                                        performCommand(Bukkit.getConsoleSender(), cmdf[0]);
                                    });
                                } else {
                                    performCommand(Bukkit.getConsoleSender(), cmd);
                                }
                            });
                        }
                    } catch (Exception e) {
                        RedProtect.get().logger.severe("There's an error on execute the command " + c + " when purging the region " + region.getName());
                        printJarVersion();
                        e.printStackTrace();
                    }

                    // Regen or Purge
                    if (RedProtect.get().hooks.checkWe() && RedProtect.get().getConfigManager().configRoot().purge.regen.enabled) {
                        if (region.getArea() <= RedProtect.get().getConfigManager().configRoot().purge.regen.max_area_regen) {
                            WEHook.regenRegion(region, Bukkit.getWorld(region.getWorld()), region.getMaxLocation(), region.getMinLocation(), delay, null, true);
                            delay = delay + 10;
                        } else {
                            skipped++;
                            continue;
                        }
                    } else {
                        RedProtect.get().getRegionManager().remove(region, region.getWorld());
                        purged++;
                        RedProtect.get().logger.warning("Purging " + region.getName() + " - Days: " + days);
                    }
                    continue;
                }
            }

            // Sell regions
            if (RedProtect.get().getConfigManager().configRoot().sell.enabled && !serverRegion) {
                Date regiondate = null;
                try {
                    regiondate = dateFormat.parse(region.getDate());
                } catch (ParseException e) {
                    RedProtect.get().logger.severe("The 'date-format' don't match with region date!!");
                    printJarVersion();
                    e.printStackTrace();
                }
                long days = TimeUnit.DAYS.convert(now.getTime() - regiondate.getTime(), TimeUnit.MILLISECONDS);

                boolean ignore = false;
                for (String play : RedProtect.get().getConfigManager().configRoot().sell.ignore_regions_from_players) {
                    if (region.isLeader(play) || region.isAdmin(play)) {
                        ignore = true;
                        break;
                    }
                }

                if (!ignore && days > RedProtect.get().getConfigManager().configRoot().sell.sell_oldest) {
                    RedProtect.get().logger.warning("Selling " + region.getName() + " - Days: " + days);
                    EconomyManager.putToSell(region, RedProtect.get().getConfigManager().configRoot().region_settings.default_leader, EconomyManager.getRegionValue(region));
                    sell++;
                    continue;
                }
            }

            if (RedProtect.get().hooks.checkSC()) {
                //remove deleted clans from regions
                if (region.flagExists("clan") && !RedProtect.get().hooks.clanManager.isClan(region.getFlagString("clan"))) {
                    region.setFlag(Bukkit.getConsoleSender(), "clan", "");
                }
            }
        }

        if (delay > 0) {
            RedProtect.get().logger.warning("&c> There's " + delay / 10 + " regions to be regenerated at 2 regions/second.");
            if (RedProtect.get().getConfigManager().configRoot().purge.regen.enable_whitelist_regen) {
                Bukkit.getServer().setWhitelist(true);
                RedProtect.get().logger.warning("&eEnabled whitelist until regen!");
            }
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

    public String PlayerToUUID(String playerName) {
        if (playerName.isEmpty()) return null;

        //check if is already UUID
        if (isUUIDs(playerName) || isDefaultServer(playerName) || (playerName.startsWith("[") && playerName.endsWith("]"))) {
            return playerName;
        }

        if (cachedUUIDs.containsValue(playerName)) {
            return cachedUUIDs.entrySet().stream().filter(e -> e.getValue().equalsIgnoreCase(playerName)).findFirst().get().getKey();
        }

        String uuid = playerName;

        try {
            OfflinePlayer offp = RedProtect.get().getServer().getOfflinePlayer(playerName);
            uuid = offp.getUniqueId().toString();
        } catch (IllegalArgumentException e) {
            Player onp = RedProtect.get().getServer().getPlayer(playerName);
            if (onp != null) {
                uuid = onp.getUniqueId().toString();
            }
        }

        cachedUUIDs.put(uuid, playerName);
        return uuid;
    }

    public String UUIDtoPlayer(String uuid) {
        if (uuid.isEmpty()) return null;

        //check if is UUID
        if (isDefaultServer(uuid) || !isUUIDs(uuid)) {
            return uuid;
        }

        if (cachedUUIDs.containsKey(uuid)) {
            return cachedUUIDs.get(uuid);
        }

        String playerName = uuid;
        UUID uuids;

        try {
            uuids = UUID.fromString(uuid);
            OfflinePlayer offp = RedProtect.get().getServer().getOfflinePlayer(uuids);
            playerName = offp.getName();
        } catch (IllegalArgumentException e) {
            Player onp = RedProtect.get().getServer().getPlayer(uuid);
            if (onp != null) {
                playerName = onp.getName();
            }
        }
        if (playerName == null) {
            playerName = MojangUUIDs.getName(uuid);
        }

        if (playerName != null)
            cachedUUIDs.put(uuid, playerName);
        return playerName;
    }

    private boolean isDefaultServer(String check) {
        return check.equalsIgnoreCase(RedProtect.get().getConfigManager().configRoot().region_settings.default_leader);
    }

    public boolean mysqlToFile() {
        HashMap<String, Region> regions = new HashMap<>();
        int saved = 1;

        try {
            Connection dbcon = DriverManager.getConnection("jdbc:mysql://" + RedProtect.get().getConfigManager().configRoot().mysql.host + "/" + RedProtect.get().getConfigManager().configRoot().mysql.db_name + "?autoReconnect=true", RedProtect.get().getConfigManager().configRoot().mysql.user_name, RedProtect.get().getConfigManager().configRoot().mysql.user_pass);

            for (World world : Bukkit.getWorlds()) {
                String tableName = RedProtect.get().getConfigManager().configRoot().mysql.table_prefix + world.getName();
                PreparedStatement st = dbcon.prepareStatement("SELECT * FROM `" + tableName + "` WHERE world=?");
                st.setString(1, world.getName());
                ResultSet rs = st.executeQuery();
                while (rs.next()) {
                    Set<PlayerRegion> leaders = new HashSet<>();
                    Set<PlayerRegion> admins = new HashSet<>();
                    Set<PlayerRegion> members = new HashSet<>();
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
                    boolean candel = rs.getBoolean("candelete");
                    boolean canPurge = rs.getBoolean("canpurge");

                    Location tppoint = null;
                    if (rs.getString("tppoint") != null && !rs.getString("tppoint").equalsIgnoreCase("")) {
                        String[] tpstring = rs.getString("tppoint").split(",");
                        tppoint = new Location(world, Double.parseDouble(tpstring[0]), Double.parseDouble(tpstring[1]), Double.parseDouble(tpstring[2]),
                                Float.parseFloat(tpstring[3]), Float.parseFloat(tpstring[4]));
                    }

                    for (String member : rs.getString("members").split(", ")) {
                        if (!member.isEmpty()) {
                            String[] p = member.split("@");
                            members.add(new PlayerRegion(p[0], p.length == 2 ? p[1] : p[0]));
                        }
                    }
                    for (String admin : rs.getString("admins").split(", ")) {
                        if (!admin.isEmpty()) {
                            String[] p = admin.split("@");
                            admins.add(new PlayerRegion(p[0], p.length == 2 ? p[1] : p[0]));
                        }
                    }
                    for (String leader : rs.getString("leaders").split(", ")) {
                        if (!leader.isEmpty()) {
                            String[] p = leader.split("@");
                            leaders.add(new PlayerRegion(p[0], p.length == 2 ? p[1] : p[0]));
                        }
                    }

                    for (String flag : rs.getString("flags").split(",")) {
                        String key = flag.split(":")[0];
                        String replace = key + ":";
                        if (replace.length() <= flag.length()) {
                            flags.put(key, parseObject(flag.substring(replace.length())));
                        }
                    }
                    Region newr = new Region(rname, admins, members, leaders, maxMbrX, minMbrX, maxMbrZ, minMbrZ, minY, maxY, flags, wel, prior, world.getName(), date, value, tppoint, candel, canPurge);
                    regions.put(rname, newr);
                }
                st.close();
                rs.close();

                //write to yml
                YamlConfiguration fileDB = new YamlConfiguration();
                File datf = new File(RedProtect.get().getDataFolder() + File.separator + "data", "data_" + world.getName() + ".yml");
                for (Region r : regions.values()) {
                    if (r.getName() == null) {
                        continue;
                    }

                    if (RedProtect.get().getConfigManager().configRoot().flat_file.region_per_file) {
                        if (!r.toSave()) {
                            continue;
                        }
                        fileDB = new YamlConfiguration();
                        datf = new File(RedProtect.get().getDataFolder() + File.separator + "data", world.getName() + File.separator + r.getName() + ".yml");
                    }

                    addProps(fileDB, r);
                    saved++;

                    if (RedProtect.get().getConfigManager().configRoot().flat_file.region_per_file) {
                        saveYaml(fileDB, datf);
                        r.setToSave(false);
                    }
                }

                if (!RedProtect.get().getConfigManager().configRoot().flat_file.region_per_file) {
                    saveYaml(fileDB, datf);
                } else {
                    //remove deleted regions
                    File wfolder = new File(RedProtect.get().getDataFolder() + File.separator + "data", world.getName());
                    if (wfolder.exists()) {
                        File[] listOfFiles = new File(RedProtect.get().getDataFolder() + File.separator + "data", world.getName()).listFiles();
                        if (listOfFiles != null) {
                            for (File region : listOfFiles) {
                                if (region.isFile() && !regions.containsKey(region.getName().replace(".yml", ""))) {
                                    region.delete();
                                }
                            }
                        }
                    }
                }
            }
            dbcon.close();

            if (saved > 0) {
                RedProtect.get().logger.success((saved - 1) + " regions converted to File with success!");
            }
        } catch (SQLException e) {
            printJarVersion();
            e.printStackTrace();
        }

        return true;
    }

    public void backupRegions() {
        File bkpFolder = new File(RedProtect.get().getDataFolder() + File.separator + "backups" + File.separator);
        if (!bkpFolder.exists()) {
            bkpFolder.mkdir();
        }

        try {
            RedProtect.get().getUtil().zipFolder(RedProtect.get().getDataFolder() + File.separator + "data", genFileName(bkpFolder.getPath() + File.separator, true).getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean fileToMysql() throws Exception {
        if (!RedProtect.get().getConfigManager().configRoot().file_type.equalsIgnoreCase("file")) {
            return false;
        }
        RedProtect.get().getRegionManager().saveAll(false);

        initMysql();//Create tables
        int counter = 1;

        for (World world : Bukkit.getWorlds()) {

            String dbname = RedProtect.get().getConfigManager().configRoot().mysql.db_name;
            String url = "jdbc:mysql://" + RedProtect.get().getConfigManager().configRoot().mysql.host + "/";
            String tableName = RedProtect.get().getConfigManager().configRoot().mysql.table_prefix + world.getName();

            Connection dbcon = DriverManager.getConnection(url + dbname, RedProtect.get().getConfigManager().configRoot().mysql.user_name, RedProtect.get().getConfigManager().configRoot().mysql.user_pass);

            for (Region r : RedProtect.get().getRegionManager().getRegionsByWorld(world.getName())) {
                if (!regionExists(dbcon, r.getName(), tableName)) {
                    try {
                        PreparedStatement st = dbcon.prepareStatement("INSERT INTO `" + tableName + "` (name,leaders,admins,members,maxMbrX,minMbrX,maxMbrZ,minMbrZ,minY,maxY,centerX,centerZ,date,wel,prior,world,value,tppoint,candelete,flags,canpurge) "
                                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
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
                        st.setDouble(17, r.getValue());
                        st.setString(18, r.getTPPointString());
                        st.setInt(19, r.canDelete() ? 1 : 0);
                        st.setString(20, r.getFlagStrings());
                        st.setInt(21, r.canPurge() ? 1 : 0);

                        st.executeUpdate();
                        st.close();
                        counter++;
                    } catch (SQLException e) {
                        printJarVersion();
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

    private void initMysql() throws Exception {
        for (World world : Bukkit.getWorlds()) {

            String url = "jdbc:mysql://" + RedProtect.get().getConfigManager().configRoot().mysql.host + "/";
            String reconnect = "?autoReconnect=true";
            String tableName = RedProtect.get().getConfigManager().configRoot().mysql.table_prefix + world.getName();

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
                    Connection con = DriverManager.getConnection(url + RedProtect.get().getConfigManager().configRoot().mysql.table_prefix + reconnect, RedProtect.get().getConfigManager().configRoot().mysql.user_name, RedProtect.get().getConfigManager().configRoot().mysql.user_pass);
                    st = con.prepareStatement("CREATE TABLE `" + tableName + "` (name varchar(20) PRIMARY KEY NOT NULL, leaders longtext, admins longtext, members longtext, maxMbrX int, minMbrX int, maxMbrZ int, minMbrZ int, centerX int, centerZ int, minY int, maxY int, date varchar(10), wel longtext, prior int, world varchar(100), value Long not null, tppoint varchar(100), flags longtext, candelete tinyint(1)) CHARACTER SET utf8 COLLATE utf8_general_ci");
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
                printJarVersion();
                e.printStackTrace();
                RedProtect.get().logger.severe("There was an error while parsing SQL, redProtect will still with actual DB setting until you change the connection options or check if a Mysql service is running. Use /rp reload to try again");
            } finally {
                if (st != null) {
                    st.close();
                }
            }
        }
    }

    private void addNewColumns(String tableName) {
        try {
            String url = "jdbc:mysql://" + RedProtect.get().getConfigManager().configRoot().mysql.host + "/";
            Connection con = DriverManager.getConnection(url + RedProtect.get().getConfigManager().configRoot().mysql.db_name, RedProtect.get().getConfigManager().configRoot().mysql.user_name, RedProtect.get().getConfigManager().configRoot().mysql.user_pass);
            DatabaseMetaData md = con.getMetaData();
            ResultSet rs = md.getColumns(null, null, tableName, "candelete");
            if (!rs.next()) {
                PreparedStatement st = con.prepareStatement("ALTER TABLE `" + tableName + "` ADD `candelete` tinyint(1) NOT NULL default '1'");
                st.executeUpdate();
            }
            rs.close();
            rs = md.getColumns(null, null, tableName, "canpurge");
            if (!rs.next()) {
                PreparedStatement st = con.prepareStatement("ALTER TABLE `" + tableName + "` ADD `canpurge` tinyint(1) NOT NULL default '1'");
                st.executeUpdate();
            }
            rs.close();
            con.close();
        } catch (SQLException e) {
            printJarVersion();
            e.printStackTrace();
        }
    }

    private boolean regionExists(Connection dbcon, String name, String tableName) {
        int total = 0;
        try {
            PreparedStatement st = dbcon.prepareStatement("SELECT COUNT(*) FROM `" + tableName + "` WHERE LOWER(name) = ?");
            st.setString(1, name.toLowerCase());
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                total = rs.getInt("COUNT(*)");
            }
            st.close();
            rs.close();
        } catch (SQLException e) {
            printJarVersion();
            e.printStackTrace();
        }
        return total > 0;
    }

    private boolean checkTableExists(String tableName) {
        try {
            RedProtect.get().logger.debug(LogLevel.DEFAULT, "Checking if table exists... " + tableName);
            Connection con = DriverManager.getConnection("jdbc:mysql://" + RedProtect.get().getConfigManager().configRoot().mysql.host + "/" + RedProtect.get().getConfigManager().configRoot().mysql.db_name, RedProtect.get().getConfigManager().configRoot().mysql.user_name, RedProtect.get().getConfigManager().configRoot().mysql.user_pass);
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
            printJarVersion();
            e.printStackTrace();
        }
        return false;
    }

    public void startFlagChanger(final String r, final String flag, final Player p) {
        RedProtect.get().changeWait.add(r + flag);
        Bukkit.getScheduler().scheduleSyncDelayedTask(RedProtect.get(), () -> RedProtect.get().changeWait.remove(r + flag), RedProtect.get().getConfigManager().configRoot().flags_configuration.change_flag_delay.seconds * 20L);
    }

    public int getUpdatedPrior(Region region) {
        int regionarea = region.getArea();
        int prior = region.getPrior();
        Region topRegion = RedProtect.get().getRegionManager().getTopRegion(region.getWorld(), region.getCenterX(), region.getCenterY(), region.getCenterZ());
        Region lowRegion = RedProtect.get().getRegionManager().getLowRegion(region.getWorld(), region.getCenterX(), region.getCenterY(), region.getCenterZ());

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

    public void addBorder(final Player p, Region r) {
        final String player = p.getName();

        if (borderPlayers.containsKey(player)) {
            int task = (int) borderPlayers.get(player);
            Bukkit.getScheduler().cancelTask(task);
            borderPlayers.remove(player);
        }

        Location min = r.getMinLocation();
        Location max = r.getMaxLocation();

        Set<Location> locations = new LinkedHashSet<>();
        World w = p.getWorld();

        int height = p.getLocation().getBlockY();

        int minX = (int) min.getX();
        int maxX = (int) max.getX();
        int minZ = (int) min.getZ();
        int maxZ = (int) max.getZ();

        if (minX > maxX) {
            int temp = minX;
            minX = maxX;
            maxX = temp;
        }
        if (minZ > maxZ) {
            int temp = minZ;
            minZ = maxZ;
            maxZ = temp;
        }

        String intense = RedProtect.get().getConfigManager().configRoot().region_settings.border.intensity;
        int borderHeight = RedProtect.get().getConfigManager().configRoot().region_settings.border.height;

        for (int x = minX; x <= maxX; x++) {
            if (x % 2 != 0 && intense.equalsIgnoreCase("LOW")) continue;
            for (int z = minZ; z <= maxZ; z++) {
                if (z % 2 != 0 && intense.equalsIgnoreCase("LOW")) continue;
                if (x == min.getX() || x == max.getX() || z == min.getZ() || z == max.getZ()) {
                    for (int y = height - borderHeight; y < height + borderHeight; y++) {
                        if (y % 2 != 0 && (intense.equalsIgnoreCase("LOW") || intense.equalsIgnoreCase("NORMAL")))
                            continue;
                        locations.add(new Location(w, x, y, z));
                    }
                }
            }
        }

        String particleName = RedProtect.get().getConfigManager().configRoot().region_settings.border.particle;
        if (!plugin.getVersionHelper().existParticle(particleName)) {
            particleName = "FLAME";
        }

        final String finalParticleName = particleName;
        int task = Bukkit.getScheduler().scheduleSyncRepeatingTask(RedProtect.get(), () ->
                locations.forEach(l -> plugin.getVersionHelper().spawnParticle(w, finalParticleName, l.getX() + 0.500, l.getY(), l.getZ() + 0.500)), 10, 10);
        borderPlayers.put(player, task);

        Bukkit.getScheduler().runTaskLater(RedProtect.get(), () -> {
            if (borderPlayers.containsKey(player)) {
                int newTask = (int) borderPlayers.get(player);
                Bukkit.getScheduler().cancelTask(newTask);
                borderPlayers.remove(player);
            }
        }, RedProtect.get().getConfigManager().configRoot().region_settings.border.time_showing * 20L);
    }

    public int convertFromGP() {
        int claimed = 0;
        Collection<Claim> claims = GriefPrevention.instance.dataStore.getClaims();
        for (Claim claim : claims) {
            if (Bukkit.getWorlds().contains(claim.getGreaterBoundaryCorner().getWorld())) {
                World w = claim.getGreaterBoundaryCorner().getWorld();
                String pname = claim.getOwnerName().replace(" ", "_").toLowerCase();
                if (claim.ownerID != null) {
                    pname = claim.ownerID.toString();
                }
                Set<PlayerRegion> leaders = new HashSet<>();
                leaders.add(new PlayerRegion(claim.ownerID != null ? claim.ownerID.toString() : pname, pname));
                Location newmin = claim.getGreaterBoundaryCorner();
                Location newmax = claim.getLesserBoundaryCorner();
                newmin.setY(w.getMinHeight());
                newmax.setY(w.getMaxHeight());

                Region r = new Region(nameGen(claim.getOwnerName().replace(" ", "_").toLowerCase(), w.getName()), new HashSet<>(), new HashSet<>(), leaders,
                        newmin, newmax, RedProtect.get().getConfigManager().getDefFlagsValues(), "GriefPrevention region", 0, w.getName(), dateNow(), 0, null, true, true);

                Region other = RedProtect.get().getRegionManager().getTopRegion(w.getName(), r.getCenterX(), r.getCenterY(), r.getCenterZ());
                if (other == null || !r.getWelcome().equals(other.getWelcome())) {
                    RedProtect.get().getRegionManager().add(r, w.getName());
                    RedProtect.get().logger.debug(LogLevel.DEFAULT, "Region: " + r.getName());
                    claimed++;
                }
            }
        }
        return claimed;
    }

    public String getTitleName(Region r) {
        return RedProtect.get().getLanguageManager().get("gui.invflag").replace("{region}", r.getName());
    }

    private void saveYaml(YamlConfiguration fileDB, File file) {
        try {
            fileDB.save(file);
        } catch (IOException e) {
            RedProtect.get().logger.severe("Error during save database file");
            printJarVersion();
            e.printStackTrace();
        }
    }

    public void addProps(YamlConfiguration fileDB, Region r) {
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
        fileDB.set(rname + ".canpurge", r.canPurge());

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

    public int SingleToFiles() {
        int saved = 0;
        for (World w : Bukkit.getWorlds()) {
            Set<Region> regions = RedProtect.get().getRegionManager().getRegionsByWorld(w.getName());
            for (Region r : regions) {
                YamlConfiguration fileDB = new YamlConfiguration();

                File f = new File(RedProtect.get().getDataFolder() + File.separator + "data", w.getName());
                if (!f.exists()) {
                    f.mkdir();
                }
                File wf = new File(RedProtect.get().getDataFolder() + File.separator + "data", w.getName() + File.separator + r.getName() + ".yml");

                saved++;
                addProps(fileDB, r);
                saveYaml(fileDB, wf);
            }

            File oldf = new File(RedProtect.get().getDataFolder() + File.separator + "data", "data_" + w.getName() + ".yml");
            if (oldf.exists()) {
                oldf.delete();
            }
        }

        if (!RedProtect.get().getConfigManager().configRoot().flat_file.region_per_file) {
            RedProtect.get().getConfigManager().configRoot().flat_file.region_per_file = true;
        }
        RedProtect.get().getConfigManager().save();
        return saved;
    }

    public int FilesToSingle() {
        int saved = 0;
        for (World w : Bukkit.getWorlds()) {
            File f = new File(RedProtect.get().getDataFolder() + File.separator + "data", "data_" + w.getName() + ".yml");
            Set<Region> regions = RedProtect.get().getRegionManager().getRegionsByWorld(w.getName());
            YamlConfiguration fileDB = new YamlConfiguration();
            for (Region r : regions) {
                addProps(fileDB, r);
                saved++;
                File oldf = new File(RedProtect.get().getDataFolder() + File.separator + "data", w.getName() + File.separator + r.getName() + ".yml");
                if (oldf.exists()) {
                    oldf.delete();
                }
            }
            File oldf = new File(RedProtect.get().getDataFolder() + File.separator + "data", w.getName());
            if (oldf.exists()) {
                oldf.delete();
            }
            saveYaml(fileDB, f);
        }
        if (RedProtect.get().getConfigManager().configRoot().flat_file.region_per_file) {
            RedProtect.get().getConfigManager().configRoot().flat_file.region_per_file = false;
        }
        RedProtect.get().getConfigManager().save();
        return saved;
    }

    public boolean canBuildNear(Player p, Location loc) {
        if (RedProtect.get().getConfigManager().configRoot().region_settings.deny_build_near <= 0) {
            return true;
        }
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        int radius = RedProtect.get().getConfigManager().configRoot().region_settings.deny_build_near;
        if (radius > 4)
            radius = 4;

        for (int ix = x - radius; ix <= x + radius; ++ix) {
            for (int iy = y - radius; iy <= y + radius; ++iy) {
                for (int iz = z - radius; iz <= z + radius; ++iz) {
                    Region reg = RedProtect.get().getRegionManager().getTopRegion(new Location(p.getWorld(), ix, iy, iz));
                    if (reg != null && !reg.canBuild(p)) {
                        RedProtect.get().getLanguageManager().sendMessage(p, RedProtect.get().getLanguageManager().get("blocklistener.cantbuild.nearrp").replace("{distance}", "" + radius));
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public int simuleTotalRegionSize(String player, Region r2) {
        int total = 0;
        int regs = 0;
        for (Location loc : r2.get4Points(r2.getCenterY())) {
            Map<Integer, Region> pregs = RedProtect.get().getRegionManager().getGroupRegion(loc);
            pregs.remove(r2.getPrior());
            Region other;
            if (!pregs.isEmpty()) {
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

    public String fixRegionName(Player p, String regionName) {

        //filter region name
        if (regionName == null || regionName.isEmpty()) {
            regionName = nameGen(p.getName(), p.getWorld().getName());
        }

        regionName = Normalizer.normalize(regionName.replaceAll("[().+=;:]", ""), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replaceAll("[ ]", "_")
                .replaceAll("[^\\p{L}_0-9]", "");

        //region name conform
        if (regionName.length() < 3 || regionName.length() > 16) {
            RedProtect.get().getLanguageManager().sendMessage(p, "regionbuilder.regionname.invalid");
            return null;
        }

        if (RedProtect.get().getRegionManager().getRegion(regionName, p.getWorld().getName()) != null) {
            RedProtect.get().getLanguageManager().sendMessage(p, "regionbuilder.regionname.existis");
            return null;
        }

        return regionName;
    }

    /**
     * Generate a friendly and unique name for a region based on player name.
     *
     * @param p     Player
     * @param World World
     * @return Name of region
     */
    public String nameGen(String p, String World) {
        String rname;
        int i = 0;
        while (true) {
            int is = String.valueOf(i).length();
            if (p.length() > 13) {
                rname = p.substring(0, 14 - is) + "_" + i;
            } else {
                rname = p + "_" + i;
            }
            if (RedProtect.get().getRegionManager().getRegion(rname, World) == null) {
                break;
            }
            ++i;
        }
        return rname;
    }

    public ItemStack createSkullOld(String texture) {
        Material mat = Material.getMaterial("PLAYER_HEAD");
        ItemStack s;
        if (mat != null) {
            s = new ItemStack(mat);
        } else {
            s = new ItemStack(Material.getMaterial("SKULL_ITEM"), 1, (short) 3);
        }

        SkullMeta meta = (SkullMeta) s.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), "");
        profile.getProperties().put("textures", new Property("textures", texture));
        Field profileField;
        try {
            profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            Bukkit.getLogger().warning("Failed to set base64 skull value!");
        }
        s.setItemMeta(meta);
        return s;
    }

    /* Skull texture example by https://github.com/RRS-9747/HeadDrop */
    public ItemStack createSkull(String texture) {
        Material mat = Material.getMaterial("PLAYER_HEAD");
        ItemStack s;
        if (mat != null) {
            s = new ItemStack(mat);
        } else {
            s = new ItemStack(Material.getMaterial("SKULL_ITEM"), 1, (short) 3);
        }

        try {
            // Only for paper servers
            s.editMeta(SkullMeta.class, skullMeta -> {
                final UUID uuid = UUID.randomUUID();
                final PlayerProfile playerProfile = Bukkit.createProfile(uuid, uuid.toString().substring(0, 16));
                playerProfile.setProperty(new ProfileProperty("textures", texture));

                skullMeta.setPlayerProfile(playerProfile);
            });
        } catch (Exception ex){
            // If not paper
                SkullMeta sm = (SkullMeta) s.getItemMeta();
                mutateItemMeta(sm, texture);
                s.setItemMeta(sm);
            }
        return s;
    }

    private static GameProfile makeProfile(String b64) {
        UUID id = new UUID(
                b64.substring(b64.length() - 20).hashCode(),
                b64.substring(b64.length() - 10).hashCode()
        );
        GameProfile profile = new GameProfile(id, "Player");
        profile.getProperties().put("textures", new Property("textures", b64));
        return profile;
    }

    private static Method metaSetProfileMethod;
    private static Field metaProfileField;
    private static void mutateItemMeta(SkullMeta meta, String b64) {
        try {
            if (metaSetProfileMethod == null) {
                metaSetProfileMethod = meta.getClass().getDeclaredMethod("setProfile", GameProfile.class);
                metaSetProfileMethod.setAccessible(true);
            }
            metaSetProfileMethod.invoke(meta, makeProfile(b64));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            try {
                if (metaProfileField == null) {
                    metaProfileField = meta.getClass().getDeclaredField("profile");
                    metaProfileField.setAccessible(true);
                }
                metaProfileField.set(meta, makeProfile(b64));

            } catch (NoSuchFieldException | IllegalAccessException ex2) {
                ex2.printStackTrace();
            }
        }
    }
}
