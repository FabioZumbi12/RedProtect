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

package br.net.fabiozumbi12.RedProtect.Sponge.helpers;

import br.net.fabiozumbi12.RedProtect.Core.helpers.CoreUtil;
import br.net.fabiozumbi12.RedProtect.Core.helpers.LogLevel;
import br.net.fabiozumbi12.RedProtect.Core.region.PlayerRegion;
import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import br.net.fabiozumbi12.RedProtect.Sponge.Region;
import br.net.fabiozumbi12.RedProtect.Sponge.hooks.WEHook;
import com.flowpowered.math.vector.Vector3d;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

public class RedProtectUtil extends CoreUtil {

    public Text toText(String str) {
        return TextSerializers.FORMATTING_CODE.deserialize(str);
    }

    public boolean isBukkitBlock(BlockState b) {
        RedProtect.get().logger.debug(LogLevel.DEFAULT, "BlockType: " + b.getType().getName());
        return b.getType().getName().startsWith("minecraft:");
    }

    public boolean isBukkitEntity(Entity e) {
        RedProtect.get().logger.debug(LogLevel.DEFAULT, "EntityType: " + e.getType().getName());
        return Sponge.getGame().getRegistry().getType(EntityType.class, e.getType().getName()).isPresent();
    }

    public Transform<World> DenyExitPlayer(Player p, Transform<World> from, Transform<World> to, Region r) {
        Region rto = RedProtect.get().rm.getTopRegion(to.getLocation(), RedProtectUtil.class.getName());
        if (rto != r) {
            to = new Transform<>(from.getLocation()).setRotation(from.getRotation());
            RedProtect.get().lang.sendMessage(p, "playerlistener.region.cantregionexit");
        }
        return to;
    }

    public boolean isBypassBorder(Player p) {
        int diameter = (int) p.getWorld().getWorldBorder().getDiameter() / 2;
        int centerZ = (int) p.getWorld().getWorldBorder().getCenter().getZ();
        int centerX = (int) p.getWorld().getWorldBorder().getCenter().getX();
        int playerX = (int) p.getLocation().getPosition().getX();
        int playerZ = (int) p.getLocation().getPosition().getZ();
        double x = playerX - centerX;
        double z = playerZ - centerZ;
        return ((x > diameter || (-x) > diameter) || (z > diameter || (-z) > diameter));
    }

    public Transform<World> DenyEnterPlayer(World wFrom, Transform<World> from, Transform<World> to, Region r, boolean checkSec) {
        Location<World> setFrom = from.getLocation();
        for (int i = 0; i < r.getArea() + 10; i++) {
            Region r1 = RedProtect.get().rm.getTopRegion(wFrom.getName(), setFrom.getBlockX() + i, setFrom.getBlockY(), setFrom.getBlockZ(), RedProtectUtil.class.getName());
            Region r2 = RedProtect.get().rm.getTopRegion(wFrom.getName(), setFrom.getBlockX() - i, setFrom.getBlockY(), setFrom.getBlockZ(), RedProtectUtil.class.getName());
            Region r3 = RedProtect.get().rm.getTopRegion(wFrom.getName(), setFrom.getBlockX(), setFrom.getBlockY(), setFrom.getBlockZ() + i, RedProtectUtil.class.getName());
            Region r4 = RedProtect.get().rm.getTopRegion(wFrom.getName(), setFrom.getBlockX(), setFrom.getBlockY(), setFrom.getBlockZ() - i, RedProtectUtil.class.getName());
            Region r5 = RedProtect.get().rm.getTopRegion(wFrom.getName(), setFrom.getBlockX() + i, setFrom.getBlockY(), setFrom.getBlockZ() + i, RedProtectUtil.class.getName());
            Region r6 = RedProtect.get().rm.getTopRegion(wFrom.getName(), setFrom.getBlockX() - i, setFrom.getBlockY(), setFrom.getBlockZ() - i, RedProtectUtil.class.getName());
            if (r1 != r) {
                to = new Transform<>(setFrom.add(+i, 0, 0)).setRotation(from.getRotation());
                break;
            }
            if (r2 != r) {
                to = new Transform<>(setFrom.add(-i, 0, 0)).setRotation(from.getRotation());
                break;
            }
            if (r3 != r) {
                to = new Transform<>(setFrom.add(0, 0, +i)).setRotation(from.getRotation());
                break;
            }
            if (r4 != r) {
                to = new Transform<>(setFrom.add(0, 0, -i)).setRotation(from.getRotation());
                break;
            }
            if (r5 != r) {
                to = new Transform<>(setFrom.add(+i, 0, +i)).setRotation(from.getRotation());
                break;
            }
            if (r6 != r) {
                to = new Transform<>(setFrom.add(-i, 0, -i)).setRotation(from.getRotation());
                break;
            }
        }
        if (checkSec && !isSecure(to.getLocation())) {
            RedProtect.get().getVersionHelper().setBlock(to.getLocation().getBlockRelative(Direction.DOWN), BlockTypes.GRASS.getDefaultState());
        }
        return to;
    }

    private boolean isSecure(Location loc) {
        BlockState b = loc.add(0, -1, 0).getBlock();
        return (!b.getType().equals(BlockTypes.AIR) && !b.getType().equals(BlockTypes.WATER)) || b.getType().getName().contains("LAVA");
    }

    private void saveToZipFile(File file, String ZippedFile, Set<CommentedConfigurationNode> conf) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            ZipOutputStream zos = new ZipOutputStream(fos);
            if (ZippedFile == null) {
                for (CommentedConfigurationNode c : conf) {
                    try {
                        ZipEntry e = new ZipEntry(c.getAppendedNode().getNode("name").getString() + ".conf");
                        zos.putNextEntry(e);
                        byte[] data = c.toString().getBytes();
                        zos.write(data, 0, data.length);
                        zos.closeEntry();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            } else {
                ZipEntry e = new ZipEntry(ZippedFile);
                zos.putNextEntry(e);
                for (CommentedConfigurationNode c : conf) {
                    byte[] data = c.toString().getBytes();
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

    public boolean removeGuiItem(ItemStack item) {
        if (item.get(Keys.ITEM_LORE).isPresent()) {
            try {
                String lore = item.get(Keys.ITEM_LORE).get().get(1).toPlain();
                if (RedProtect.get().config.getDefFlags().contains(lore.replace("§0", "")) || lore.equals(RedProtect.get().guiLang.getFlagString("separator").toPlain())) {
                    return true;
                }
            } catch (IndexOutOfBoundsException ignored) {
            }
        }
        return false;
    }

    public boolean isGuiItem(ItemStack pitem) {
        if (pitem.get(Keys.ITEM_LORE).isPresent() &&
                pitem.get(Keys.ITEM_LORE).get().size() >= 2 &&
                RedProtect.get().config.getDefFlags().contains(pitem.get(Keys.ITEM_LORE).get().get(1).toPlain().replace("§0", ""))) {
            return true;
        }
        if (pitem.get(Keys.ITEM_LORE).isPresent()) {
            List<Text> lore = pitem.get(Keys.ITEM_LORE).get();
            return RedProtect.get().config.getGuiSeparator().get(Keys.ITEM_LORE).get().equals(lore);
        }
        return false;
    }

    public File genFileName(String Path, Boolean isBackup) {
        return genFileName(Path, isBackup, RedProtect.get().config.configRoot().flat_file.max_backups, dateNow());
    }

    public String dateNow() {
        return dateNow(RedProtect.get().config.configRoot().region_settings.date_format);
    }

    //TODO read all db
    public void ReadAllDB(Set<Region> regions) {
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
            boolean serverRegion = false;

            if (region.isLeader(RedProtect.get().config.configRoot().region_settings.default_leader)) {
                serverRegion = true;
                region.setDate(dateNow());
            }

            //purge regions
            if (RedProtect.get().config.configRoot().purge.enabled && !serverRegion && region.canPurge() && region.isOnTop()) {
                Date regiondate = null;
                try {
                    regiondate = dateformat.parse(region.getDate());
                } catch (ParseException e) {
                    RedProtect.get().logger.severe("The 'date-format' don't match with region date!!");
                    printJarVersion();
                    e.printStackTrace();
                }
                long days = TimeUnit.DAYS.convert(now.getTime() - regiondate.getTime(), TimeUnit.MILLISECONDS);

                boolean ignore = false;
                for (String play : RedProtect.get().config.configRoot().purge.ignore_regions_from_players) {
                    if (region.isLeader(play) || region.isAdmin(play)) {
                        ignore = true;
                        break;
                    }
                }

                if (!ignore && days > RedProtect.get().config.configRoot().purge.remove_oldest) {
                    // Execute commands
                    String c = "";
                    try {
                        if (!RedProtect.get().config.configRoot().purge.execute_commands.isEmpty()) {
                            RedProtect.get().config.configRoot().purge.execute_commands.forEach(cmd -> {
                                cmd = cmd
                                        .replace("{world}", region.getWorld())
                                        .replace("{region}", region.getName());
                                if (cmd.contains("{leader}")) {
                                    final String[] cmdf = {cmd};
                                    region.getLeaders().forEach(l -> {
                                        cmdf[0] = cmdf[0].replace("{leader}", l.getPlayerName());
                                        Sponge.getCommandManager().process(Sponge.getServer().getConsole(), cmdf[0]);
                                    });
                                } else if (cmd.contains("{admin}")) {
                                    final String[] cmdf = {cmd};
                                    region.getAdmins().forEach(a -> {
                                        cmdf[0] = cmdf[0].replace("{admin}", a.getPlayerName());
                                        Sponge.getCommandManager().process(Sponge.getServer().getConsole(), cmdf[0]);
                                    });
                                } else if (cmd.contains("{member}")) {
                                    final String[] cmdf = {cmd};
                                    region.getMembers().forEach(m -> {
                                        cmdf[0] = cmdf[0].replace("{member}", m.getPlayerName());
                                        Sponge.getCommandManager().process(Sponge.getServer().getConsole(), cmdf[0]);
                                    });
                                } else {
                                    Sponge.getCommandManager().process(Sponge.getServer().getConsole(), cmd);
                                }
                            });
                        }
                    } catch (Exception e) {
                        RedProtect.get().logger.severe("There's an error on execute the command " + c + " when purging the region " + region.getName());
                        printJarVersion();
                        e.printStackTrace();
                    }

                    // Purge or Regen
                    if (RedProtect.get().hooks.WE && RedProtect.get().config.configRoot().purge.regen.enabled) {
                        if (region.getArea() <= RedProtect.get().config.configRoot().purge.regen.max_area_regen) {
                            WEHook.regenRegion(region, Sponge.getServer().getWorld(region.getWorld()).get(), region.getMaxLocation(), region.getMinLocation(), delay, null, true);
                            delay = delay + 10;
                        } else {
                            skipped++;
                            continue;
                        }
                    } else {
                        RedProtect.get().rm.remove(region, region.getWorld());
                        purged++;
                        RedProtect.get().logger.warning("Purging " + region.getName() + " - Days: " + days);
                    }
                    continue;
                }
            }


            // Sell regions
            if (RedProtect.get().config.configRoot().sell.enabled && !serverRegion) {
                Date regiondate = null;
                try {
                    regiondate = dateformat.parse(region.getDate());
                } catch (ParseException e) {
                    RedProtect.get().logger.severe("The 'date-format' don't match with region date!!");
                    printJarVersion();
                    e.printStackTrace();
                }
                long days = TimeUnit.DAYS.convert(now.getTime() - regiondate.getTime(), TimeUnit.MILLISECONDS);

                boolean ignore = false;
                for (String play : RedProtect.get().config.configRoot().sell.ignore_regions_from_players) {
                    if (region.isLeader(play) || region.isAdmin(play)) {
                        ignore = true;
                        break;
                    }
                }

                if (!ignore && days > RedProtect.get().config.configRoot().sell.sell_oldest) {
                    RedProtect.get().logger.warning("Selling " + region.getName() + " - Days: " + days);
                    EconomyManager.putToSell(region, RedProtect.get().config.configRoot().region_settings.default_leader, EconomyManager.getRegionValue(region));
                    sell++;
                }
            }
        }

        if (delay > 0) {
            RedProtect.get().logger.warning("&c> There's " + delay / 10 + " regions to be regenerated at 2 regions/second.");
            if (RedProtect.get().config.configRoot().purge.regen.enable_whitelist_regen) {
                Sponge.getServer().setHasWhitelist(true);
                RedProtect.get().logger.warning("&eEnabled whitelist until regen!");
            }
        }

        if (skipped > 0) {
            RedProtect.get().logger.success(skipped + " regions skipped due to max size limit to regen!");
        }

        if (purged > 0) {
            RedProtect.get().logger.warning("Purged a total of &6&l" + purged + "&a&l regions!");
        }

        if (sell > 0) {
            RedProtect.get().logger.warning("Put to sell a total of &6&l" + sell + "&a&l regions!");
        }
        regions.clear();
    }


    private boolean isDefaultServer(String check) {
        return check.equalsIgnoreCase(RedProtect.get().config.configRoot().region_settings.default_leader);
    }

    public String PlayerToUUID(@Nonnull String playerName) {
        if (playerName.isEmpty()) return null;

        //check if is already UUID
        if (isUUIDs(playerName) || isDefaultServer(playerName) || (playerName.startsWith("[") && playerName.endsWith("]"))) {
            return playerName;
        }

        if (cachedUUIDs.containsValue(playerName)) {
            return cachedUUIDs.entrySet().stream().filter(e -> e.getValue().equalsIgnoreCase(playerName)).findFirst().get().getKey();
        }

        String uuid = playerName;

        UserStorageService uss = Sponge.getGame().getServiceManager().provide(UserStorageService.class).get();

        Optional<GameProfile> ogpName = uss.getAll().stream().filter(f -> f.getName().isPresent() && f.getName().get().equalsIgnoreCase(playerName)).findFirst();
        if (ogpName.isPresent()) {
            uuid = ogpName.get().getUniqueId().toString();
        } else {
            Optional<Player> p = RedProtect.get().getServer().getPlayer(playerName);
            if (p.isPresent()) {
                uuid = p.get().getUniqueId().toString();
            }
        }

        cachedUUIDs.put(uuid, playerName);
        return uuid;
    }

    public String UUIDtoPlayer(@Nonnull String uuid) {
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
            UserStorageService uss = Sponge.getGame().getServiceManager().provide(UserStorageService.class).get();
            if (uss.get(uuids).isPresent()) {
                playerName = uss.get(uuids).get().getName();
            }
        } catch (IllegalArgumentException e) {
            playerName = MojangUUIDs.getName(uuid);
        }

        if (playerName != null && playerName.isEmpty())
            cachedUUIDs.put(uuid, playerName);
        return playerName;
    }

    public User getUser(String name) {
        UserStorageService uss = Sponge.getGame().getServiceManager().provide(UserStorageService.class).get();
        if (isUUIDs(name)) {
            UUID uuid = UUID.fromString(name);
            if (uss.get(uuid).isPresent()) {
                return uss.get(uuid).get();
            }
        } else {
            if (uss.get(name).isPresent()) {
                return uss.get(name).get();
            }
        }
        return null;
    }

    public boolean mysqlToFile() {
        HashMap<String, Region> regions = new HashMap<>();
        int saved = 1;

        try {
            Connection dbcon = DriverManager.getConnection("jdbc:mysql://" + RedProtect.get().config.configRoot().mysql.host + "/" + RedProtect.get().config.configRoot().mysql.db_name + "?autoReconnect=true", RedProtect.get().config.configRoot().mysql.user_name, RedProtect.get().config.configRoot().mysql.user_pass);

            for (World world : Sponge.getServer().getWorlds()) {
                String tableName = RedProtect.get().config.configRoot().mysql.table_prefix + world.getName();
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
                    boolean candelete = rs.getBoolean("candelete");
                    boolean canPurge = rs.getBoolean("canpurge");

                    Location<World> tppoint = null;
                    if (rs.getString("tppoint") != null && !rs.getString("tppoint").equalsIgnoreCase("")) {
                        String[] tpstring = rs.getString("tppoint").split(",");
                        tppoint = new Location<>(world, Double.parseDouble(tpstring[0]), Double.parseDouble(tpstring[1]), Double.parseDouble(tpstring[2]))/*,
                        		Float.parseFloat(tpstring[3]), Float.parseFloat(tpstring[4]))*/;
                    }

                    for (String member : rs.getString("members").split(", ")) {
                        if (member.length() > 0) {
                            String[] p = member.split("@");
                            members.add(new PlayerRegion(p[0], p.length == 2 ? p[1] : p[0]));
                        }
                    }
                    for (String admin : rs.getString("admins").split(", ")) {
                        if (admin.length() > 0) {
                            String[] p = admin.split("@");
                            admins.add(new PlayerRegion(p[0], p.length == 2 ? p[1] : p[0]));
                        }
                    }
                    for (String leader : rs.getString("leaders").split(", ")) {
                        if (leader.length() > 0) {
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
                    Region newr = new Region(rname, admins, members, leaders, maxMbrX, minMbrX, maxMbrZ, minMbrZ, minY, maxY, flags, wel, prior, world.getName(), date, value, tppoint, candelete, canPurge);
                    regions.put(rname, newr);
                }
                st.close();
                rs.close();

                File datf = new File(RedProtect.get().configDir + File.separator + "data", "data_" + world.getName() + ".conf");
                ConfigurationLoader<CommentedConfigurationNode> regionManager = HoconConfigurationLoader.builder().setPath(datf.toPath()).build();
                CommentedConfigurationNode fileDB = regionManager.createEmptyNode();
                Set<CommentedConfigurationNode> dbs = new HashSet<>();
                for (Region r : regions.values()) {
                    if (r.getName() == null) {
                        continue;
                    }

                    if (RedProtect.get().config.configRoot().flat_file.region_per_file) {
                        if (!r.toSave()) {
                            continue;
                        }
                        datf = new File(RedProtect.get().configDir + File.separator + "data", world.getName() + File.separator + r.getName() + ".conf");
                        regionManager = HoconConfigurationLoader.builder().setPath(datf.toPath()).build();
                        fileDB = regionManager.createEmptyNode();
                    }

                    addProps(fileDB, r);
                    saved++;

                    if (RedProtect.get().config.configRoot().flat_file.region_per_file) {
                        dbs.add(fileDB);
                        saveConf(fileDB, regionManager);
                        r.setToSave(false);
                    }
                }

                if (!RedProtect.get().config.configRoot().flat_file.region_per_file) {
                    saveConf(fileDB, regionManager);
                } else {
                    //remove deleted regions
                    File wfolder = new File(RedProtect.get().configDir + File.separator + "data", world.getName());
                    if (wfolder.exists()) {
                        File[] listOfFiles = wfolder.listFiles();
                        if (listOfFiles != null) {
                            for (File region : listOfFiles) {
                                if (region.isFile() && !regions.containsKey(region.getName().replace(".conf", ""))) {
                                    region.delete();
                                }
                            }
                        }
                    }
                }

                //try backup
                /*if (!RedProtect.get().config.configRoot().flat_file.region_per_file) {
                    backupRegions(Collections.singleton(fileDB), world.getName(), "data_" + world + ".conf");
                } else {
                    backupRegions(dbs, world.getName(), null);
                }*/
            }
            backupRegions();
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

    public boolean fileToMysql() throws Exception {
        if (!RedProtect.get().config.configRoot().file_type.equalsIgnoreCase("file")) {
            return false;
        }
        RedProtect.get().rm.saveAll(false);

        initMysql();//Create tables
        int counter = 1;

        for (World world : Sponge.getServer().getWorlds()) {

            String dbname = RedProtect.get().config.configRoot().mysql.db_name;
            String url = "jdbc:mysql://" + RedProtect.get().config.configRoot().mysql.host + "/";
            String tableName = RedProtect.get().config.configRoot().mysql.table_prefix + world.getName();

            Connection dbcon = DriverManager.getConnection(url + dbname, RedProtect.get().config.configRoot().mysql.user_name, RedProtect.get().config.configRoot().mysql.user_pass);

            for (Region r : RedProtect.get().rm.getRegionsByWorld(world.getName())) {
                if (!regionExists(dbcon, r.getName(), tableName)) {
                    try {
                        PreparedStatement st = dbcon.prepareStatement("INSERT INTO `" + tableName + "` (name,leaders,admins,members,maxMbrX,minMbrX,maxMbrZ,minMbrZ,minY,maxY,centerX,centerZ,date,wel,prior,world,value,tppoint,candelete,flags,canpurge) "
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
        for (World world : Sponge.getServer().getWorlds()) {

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
                    Connection con = DriverManager.getConnection(url + RedProtect.get().config.configRoot().mysql.db_name + reconnect, RedProtect.get().config.configRoot().mysql.user_name, RedProtect.get().config.configRoot().mysql.user_pass);
                    st = con.prepareStatement("CREATE TABLE `" + tableName + "` "
                            + "(name varchar(20) PRIMARY KEY NOT NULL, leaders varchar(36), admins longtext, members longtext, maxMbrX int, minMbrX int, maxMbrZ int, minMbrZ int, centerX int, centerZ int, minY int, maxY int, date varchar(10), wel longtext, prior int, world varchar(100), value Long not null, tppoint mediumtext, rent longtext, flags longtext, candelete tinyint(1), canpurge tinyint(1)) CHARACTER SET utf8 COLLATE utf8_general_ci");
                    st.executeUpdate();
                    st.close();
                    st = null;
                    RedProtect.get().logger.info("Created table: " + tableName + "!");
                }
                addNewColumns(tableName);
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
            String url = "jdbc:mysql://" + RedProtect.get().config.configRoot().mysql.host + "/";
            Connection con = DriverManager.getConnection(url + RedProtect.get().config.configRoot().mysql.db_name, RedProtect.get().config.configRoot().mysql.user_name, RedProtect.get().config.configRoot().mysql.user_pass);
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

    public void backupRegions(){
        File bkpFolder = new File(RedProtect.get().configDir + File.separator + "backups" + File.separator);
        if (!bkpFolder.exists()) {
            bkpFolder.mkdir();
        }

        try {
            RedProtect.get().getUtil().zipFolder(RedProtect.get().configDir + File.separator + "data" , genFileName(bkpFolder.getPath() + File.separator, true).getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean regionExists(Connection dbcon, String name, String tableName) {
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
            printJarVersion();
            e.printStackTrace();
        }
        return total > 0;
    }

    private boolean checkTableExists(String tableName) throws SQLException {
        RedProtect.get().logger.debug(LogLevel.DEFAULT, "Checking if table exists... " + tableName);
        Connection con = DriverManager.getConnection("jdbc:mysql://" + RedProtect.get().config.configRoot().mysql.host + "/" + RedProtect.get().config.configRoot().mysql.db_name, RedProtect.get().config.configRoot().mysql.user_name, RedProtect.get().config.configRoot().mysql.user_pass);
        DatabaseMetaData meta = con.getMetaData();
        ResultSet rs = meta.getTables(null, null, tableName, null);
        boolean exists = rs.next();

        con.close();
        rs.close();
        return exists;
    }

    public void startFlagChanger(final String r, final String flag, final Player p) {
        RedProtect.get().changeWait.add(r + flag);
        Sponge.getScheduler().createSyncExecutor(RedProtect.get().container).schedule(() -> {
            /*if (p != null && p.isOnline()){
                    RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("gui.needwait.ready").replace("{flag}", flag));
                }*/
            RedProtect.get().changeWait.remove(r + flag);
        }, RedProtect.get().config.configRoot().flags_configuration.change_flag_delay.seconds, TimeUnit.SECONDS);
    }

    public int getUpdatedPrior(Region region) {
        int regionarea = region.getArea();
        int prior = region.getPrior();
        Region topRegion = RedProtect.get().rm.getTopRegion(region.getWorld(), region.getCenterX(), region.getCenterY(), region.getCenterZ(), RedProtectUtil.class.getName());
        Region lowRegion = RedProtect.get().rm.getLowRegion(region.getWorld(), region.getCenterX(), region.getCenterY(), region.getCenterZ());

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

    public void addBorder(final Player p, Location<World> min, Location<World> max) {
        final String player = p.getName();

        if (borderPlayers.containsKey(p.getName())) {
            Task task = (Task) borderPlayers.get(player);
            task.cancel();
            borderPlayers.remove(player);
        }

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

        String intense = RedProtect.get().config.configRoot().region_settings.border.intensity;
        int borderHeight = RedProtect.get().config.configRoot().region_settings.border.height;

        for (int x = minX; x <= maxX; x++) {
            if (x % 2 != 0 && intense.equalsIgnoreCase("LOW")) continue;
            for (int z = minZ; z <= maxZ; z++) {
                if (z % 2 != 0 && intense.equalsIgnoreCase("LOW")) continue;
                if (x == min.getX() || x == max.getX() || z == min.getZ() || z == max.getZ()) {
                    for (int y = height - borderHeight; y < height + borderHeight; y++) {
                        if (y % 2 != 0 && (intense.equalsIgnoreCase("LOW") || intense.equalsIgnoreCase("NORMAL")))
                            continue;
                        locations.add(new Location<>(w, x, y, z));
                    }
                }
            }
        }

        ParticleType particle;
        try {
            particle = Sponge.getRegistry().getType(ParticleType.class, RedProtect.get().config.configRoot().region_settings.border.particle).get();
        } catch (Exception ignored) {
            particle = ParticleTypes.FLAME;
        }
        ParticleType finalParticle = particle;
        Task task = Sponge.getScheduler().createSyncExecutor(RedProtect.get()).scheduleAtFixedRate(() ->
                locations.forEach(l -> w.spawnParticles(ParticleEffect.builder().quantity(1).type(finalParticle).velocity(new Vector3d(0, 0, 0)).build(), new Vector3d(l.getBlockX() + 0.500, l.getBlockY(), l.getBlockZ() + 0.500))
                ), 500, 500, TimeUnit.MILLISECONDS).getTask();
        borderPlayers.put(player, task);

        Sponge.getScheduler().createSyncExecutor(RedProtect.get()).schedule(() -> {
            if (borderPlayers.containsKey(player)) {
                Task newTask = (Task) borderPlayers.get(player);
                newTask.cancel();
                borderPlayers.remove(player);
            }
        }, RedProtect.get().config.configRoot().region_settings.border.time_showing, TimeUnit.SECONDS);
    }

    public int simuleTotalRegionSize(String player, Region r2) {
        int total = 0;
        int regs = 0;
        for (Location<World> loc : r2.get4Points(r2.getCenterY())) {
            Map<Integer, Region> pregs = RedProtect.get().rm.getGroupRegion(loc.getExtent().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
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

    public void addProps(CommentedConfigurationNode fileDB, Region r) {
        String rname = r.getName();
        fileDB.getNode(rname, "name").setValue(rname);
        fileDB.getNode(rname, "lastvisit").setValue(r.getDate());
        fileDB.getNode(rname, "leaders").setValue(r.getLeaders().stream().map(t -> t.getUUID() + "@" + t.getPlayerName()).collect(Collectors.toList()));
        fileDB.getNode(rname, "admins").setValue(r.getAdmins().stream().map(t -> t.getUUID() + "@" + t.getPlayerName()).collect(Collectors.toList()));
        fileDB.getNode(rname, "members").setValue(r.getMembers().stream().map(t -> t.getUUID() + "@" + t.getPlayerName()).collect(Collectors.toList()));
        fileDB.getNode(rname, "priority").setValue(r.getPrior());
        fileDB.getNode(rname, "welcome").setValue(r.getWelcome());
        fileDB.getNode(rname, "world").setValue(r.getWorld());
        fileDB.getNode(rname, "maxX").setValue(r.getMaxMbrX());
        fileDB.getNode(rname, "maxZ").setValue(r.getMaxMbrZ());
        fileDB.getNode(rname, "minX").setValue(r.getMinMbrX());
        fileDB.getNode(rname, "minZ").setValue(r.getMinMbrZ());
        fileDB.getNode(rname, "maxY").setValue(r.getMaxY());
        fileDB.getNode(rname, "minY").setValue(r.getMinY());
        fileDB.getNode(rname, "candelete").setValue(r.canDelete());
        for (Map.Entry<String, Object> flag : r.getFlags().entrySet()) {
            fileDB.getNode(rname, "flags", flag.getKey()).setValue(flag.getValue());
        }
        fileDB.getNode(rname, "value").setValue(r.getValue());

        Location<World> loc = r.getTPPoint();
        if (loc != null) {
            int x = loc.getBlockX();
            int y = loc.getBlockY();
            int z = loc.getBlockZ();
            //float yaw = loc.getYaw();
            //float pitch = loc.getPitch();
            fileDB.getNode(rname, "tppoint").setValue(x + "," + y + "," + z/*+","+yaw+","+pitch*/);
        } else {
            fileDB.getNode(rname, "tppoint").setValue("");
        }
    }

    public int SingleToFiles() {
        int saved = 0;
        for (World w : Sponge.getServer().getWorlds()) {
            Set<Region> regions = RedProtect.get().rm.getRegionsByWorld(w.getName());
            for (Region r : regions) {
                File wf = new File(RedProtect.get().configDir + File.separator + "data", w.getName() + File.separator + r.getName() + ".conf");
                ConfigurationLoader<CommentedConfigurationNode> regionManager = HoconConfigurationLoader.builder().setPath(wf.toPath()).build();
                CommentedConfigurationNode fileDB = regionManager.createEmptyNode();

                File f = new File(RedProtect.get().configDir + File.separator + "data", w.getName());
                if (!f.exists()) {
                    f.mkdir();
                }

                saved++;
                addProps(fileDB, r);
                saveConf(fileDB, regionManager);
            }

            File oldf = new File(RedProtect.get().configDir + File.separator + "data", "data_" + w.getName() + ".conf");
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

    public int FilesToSingle() {
        int saved = 0;
        for (World w : Sponge.getServer().getWorlds()) {
            File f = new File(RedProtect.get().configDir + File.separator + "data", "data_" + w.getName() + ".conf");
            Set<Region> regions = RedProtect.get().rm.getRegionsByWorld(w.getName());
            ConfigurationLoader<CommentedConfigurationNode> regionManager = HoconConfigurationLoader.builder().setPath(f.toPath()).build();
            CommentedConfigurationNode fileDB = regionManager.createEmptyNode();
            for (Region r : regions) {
                addProps(fileDB, r);
                saved++;
                File oldf = new File(RedProtect.get().configDir + File.separator + "data", w.getName() + File.separator + r.getName() + ".conf");
                if (oldf.exists()) {
                    oldf.delete();
                }
            }
            File oldf = new File(RedProtect.get().configDir + File.separator + "data", w.getName());
            if (oldf.exists()) {
                oldf.delete();
            }
            saveConf(fileDB, regionManager);
        }
        if (RedProtect.get().config.configRoot().flat_file.region_per_file) {
            RedProtect.get().config.configRoot().flat_file.region_per_file = false;
        }
        RedProtect.get().config.save();
        return saved;
    }

    private void saveConf(CommentedConfigurationNode fileDB, ConfigurationLoader<CommentedConfigurationNode> regionManager) {
        try {
            regionManager.save(fileDB);
        } catch (IOException e) {
            printJarVersion();
            e.printStackTrace();
        }
    }

    public boolean canBuildNear(Player p, Location<World> loc) {
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
                    Region reg = RedProtect.get().rm.getTopRegion(new Location<>(p.getWorld(), ix, iy, iz), RedProtectUtil.class.getName());
                    if (reg != null && !reg.canBuild(p)) {
                        RedProtect.get().lang.sendMessage(p, RedProtect.get().lang.get("blocklistener.cantbuild.nearrp").replace("{distance}", "" + radius));
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public String fixRegionName(Player p, String regionName) {
        //filter region name
        if (regionName == null || regionName.isEmpty() || regionName.length() < 3 || RedProtect.get().rm.getRegion(regionName, p.getWorld().getName()) != null) {
            RedProtect.get().lang.sendMessage(p, "cmdmanager.region.rename.exists");
            regionName = nameGen(p.getName(), p.getWorld().getName());
        }

        regionName = Normalizer.normalize(regionName.replaceAll("[().+=;:]", ""), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replaceAll("[ -]", "_")
                .replaceAll("[^\\p{L}_0-9]", "");

        //region name conform
        if (regionName.length() < 3 || regionName.length() > 16) {
            RedProtect.get().lang.sendMessage(p, "regionbuilder.regionname.invalid");
            return null;
        }

        if (RedProtect.get().rm.getRegion(regionName, p.getWorld().getName()) != null) {
            RedProtect.get().lang.sendMessage(p, "regionbuilder.regionname.existis");
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
            if (RedProtect.get().rm.getRegion(rname, World) == null) {
                break;
            }
            ++i;
        }
        return rname;
    }
}
