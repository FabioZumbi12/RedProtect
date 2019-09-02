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

package br.net.fabiozumbi12.RedProtect.Sponge.database;

import br.net.fabiozumbi12.RedProtect.Core.helpers.CoreUtil;
import br.net.fabiozumbi12.RedProtect.Core.helpers.LogLevel;
import br.net.fabiozumbi12.RedProtect.Core.region.PlayerRegion;
import br.net.fabiozumbi12.RedProtect.Sponge.RedProtect;
import br.net.fabiozumbi12.RedProtect.Sponge.Region;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.sql.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class WorldMySQLRegionManager implements WorldRegionManager {

    private final String url = "jdbc:mysql://" + RedProtect.get().config.configRoot().mysql.host + "/";
    private final String reconnect = "?autoReconnect=true";
    private final String dbname = RedProtect.get().config.configRoot().mysql.db_name;
    private final String tableName;
    private final HashMap<String, Region> regions;
    private final String world;
    private Connection dbcon;

    public WorldMySQLRegionManager(String world) throws SQLException {
        super();
        this.regions = new HashMap<>();
        this.world = world;
        this.tableName = RedProtect.get().config.configRoot().mysql.table_prefix + world;

        this.dbcon = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e1) {
            try {
                Class.forName("org.mariadb.jdbc.Driver");
            } catch (ClassNotFoundException e2) {
                RedProtect.get().logger.severe("Couldn't find the driver for MySQL! com.mysql.jdbc.Driver or org.mariadb.jdbc.Driver.");
                return;
            }
        }

        PreparedStatement st = null;
        try {
            if (!this.checkTableExists()) {
                Connection con = DriverManager.getConnection(this.url + this.dbname + this.reconnect + (RedProtect.get().config.configRoot().mysql.ssl ? "&useSSL=true&requireSSL=true":"")
                        , RedProtect.get().config.configRoot().mysql.user_name, RedProtect.get().config.configRoot().mysql.user_pass);

                st = con.prepareStatement("CREATE TABLE `" + tableName + "` "
                        + "(name varchar(20) PRIMARY KEY NOT NULL, leaders varchar(200), admins varchar(200), members varchar(200), maxMbrX int, minMbrX int, maxMbrZ int, minMbrZ int, centerX int, centerZ int, minY int, maxY int, date varchar(10), wel varchar(200), prior int, world varchar(100), value Long not null, tppoint varchar(20), flags longtext, candelete tinyint(1)) CHARACTER SET utf8 COLLATE utf8_general_ci");
                st.executeUpdate();
                st.close();
                st = null;
                RedProtect.get().logger.info("Created table: " + tableName + "!");

            }
            ConnectDB();
            addNewColumns();
        } catch (SQLException e) {
            CoreUtil.printJarVersion();
            e.printStackTrace();
        } finally {
            if (st != null) {
                st.close();
            }
        }
    }

    private boolean checkTableExists() {
        try {
            RedProtect.get().logger.debug(LogLevel.DEFAULT, "Checking if table exists... " + tableName);
            Connection con = DriverManager.getConnection(this.url + this.dbname, RedProtect.get().config.configRoot().mysql.user_name, RedProtect.get().config.configRoot().mysql.user_pass);
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

    private void addNewColumns() {
        try {
            Connection con = DriverManager.getConnection(this.url + this.dbname, RedProtect.get().config.configRoot().mysql.user_name, RedProtect.get().config.configRoot().mysql.user_pass);
            DatabaseMetaData md = con.getMetaData();
            ResultSet rs = md.getColumns(null, null, tableName, "candelete");
            if (!rs.next()) {
                PreparedStatement st = this.dbcon.prepareStatement("ALTER TABLE `" + tableName + "` ADD `candelete` tinyint(1) NOT NULL default '1'");
                st.executeUpdate();
            }
            rs.close();
            rs = md.getColumns(null, null, tableName, "value");
            if (!rs.next()) {
                PreparedStatement st = this.dbcon.prepareStatement("ALTER TABLE `" + tableName + "` ADD `value` Long not null default '0'");
                st.executeUpdate();
            }
            rs.close();
            con.close();
        } catch (SQLException e) {
            CoreUtil.printJarVersion();
            e.printStackTrace();
        }

    }


    /*-------------------------------------- Live Actions -------------------------------------------*/
    @Override
    public void remove(Region r) {
        removeLiveRegion(r);
        if (this.regions.containsValue(r)) {
            this.regions.remove(r.getName());
        }
    }

    private void removeLiveRegion(Region r) {
        if (this.regionExists(r.getName())) {
            try {
                PreparedStatement st = this.dbcon.prepareStatement("DELETE FROM `" + tableName + "` WHERE name = ?");
                st.setString(1, r.getName());
                st.executeUpdate();
                st.close();
            } catch (SQLException e) {
                CoreUtil.printJarVersion();
                e.printStackTrace();
            }
        }
    }

    @Override
    public void add(Region r) {
        addLiveRegion(r);
    }

    private void addLiveRegion(Region r) {
        if (!this.regionExists(r.getName())) {
            try {
                PreparedStatement st = dbcon.prepareStatement("INSERT INTO `" + tableName + "` (name,leaders,admins,members,maxMbrX,minMbrX,maxMbrZ,minMbrZ,minY,maxY,centerX,centerZ,date,wel,prior,world,value,tppoint,candelete,flags) "
                        + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
                st.setString(1, r.getName());
                st.setString(2, r.getLeadersString());
                st.setString(3, r.getAdminString());
                st.setString(4, r.getMembersString());
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
            } catch (SQLException e) {
                CoreUtil.printJarVersion();
                e.printStackTrace();
            }
        }
    }

    @Override
    public void removeLiveFlags(String rname, String flag) {
        try {
            PreparedStatement st = this.dbcon.prepareStatement("SELECT flags FROM `" + tableName + "` WHERE name = ? AND world = ?");
            st.setString(1, rname);
            st.setString(2, this.world);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                String flags = rs.getString("flags");
                String flagsStrings = flags;
                for (String flago : flags.split(",")) {
                    String key = flago.split(":")[0];
                    if (key.equals(flag)) {
                        flagsStrings = flagsStrings.replace(flago, "").replace(",,", ",");
                        st = this.dbcon.prepareStatement("UPDATE `" + tableName + "` SET flags = ? WHERE name = ?");
                        st.setString(1, flagsStrings);
                        st.setString(2, rname);
                        st.executeUpdate();
                        break;
                    }
                }
            }
            st.close();
            rs.close();
        } catch (SQLException e) {
            RedProtect.get().logger.severe("RedProtect can't save flag for region " + rname + ", please verify the Mysql Connection and table structures.");
            CoreUtil.printJarVersion();
            e.printStackTrace();
        }
    }

    @Override
    public void updateLiveRegion(String rname, String column, Object value) {
        try {
            PreparedStatement st = this.dbcon.prepareStatement("UPDATE `" + tableName + "` SET " + column + " = ? WHERE name = ? ");
            st.setObject(1, value);
            st.setString(2, rname);
            st.executeUpdate();
            st.close();
        } catch (SQLException e) {
            RedProtect.get().logger.severe("RedProtect can't save the region " + rname + ", please verify the Mysql Connection and table structures.");
            CoreUtil.printJarVersion();
            e.printStackTrace();
        }
    }

    @Override
    public void updateLiveFlags(String rname, String flag, String value) {
        try {
            PreparedStatement st = this.dbcon.prepareStatement("SELECT flags FROM `" + tableName + "` WHERE name = ? AND world = ?");
            st.setString(1, rname);
            st.setString(2, this.world);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                String flags = rs.getString("flags");
                String flagsStrings = flags;
                for (String flago : flags.split(",")) {
                    String key = flago.split(":")[0];
                    if (key.equals(flag)) {
                        flagsStrings = flagsStrings.replace(flago, key + ":" + value);
                        st = this.dbcon.prepareStatement("UPDATE `" + tableName + "` SET flags = ? WHERE name = ?");
                        st.setString(1, flagsStrings);
                        st.setString(2, rname);
                        st.executeUpdate();
                        break;
                    }
                }
            }
            st.close();
            rs.close();
        } catch (SQLException e) {
            RedProtect.get().logger.severe("RedProtect can't save flag for region " + rname + ", please verify the Mysql Connection and table structures.");
            CoreUtil.printJarVersion();
            e.printStackTrace();
        }
    }

    @Override
    public void load() {
        if (this.dbcon == null) {
            ConnectDB();
        }
        try {
            PreparedStatement st = this.dbcon.prepareStatement("SELECT * FROM `" + tableName + "` WHERE world = ?");
            st.setString(1, this.world);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                String finalName = "";
                try {
                    RedProtect.get().logger.debug(LogLevel.DEFAULT, "Load Region: " + rs.getString("name") + ", World: " + this.world);
                    HashMap<String, Object> flags = new HashMap<>();

                    String rname = rs.getString("name");
                    finalName = rname;

                    int maxMbrX = rs.getInt("maxMbrX");
                    int minMbrX = rs.getInt("minMbrX");
                    int maxMbrZ = rs.getInt("maxMbrZ");
                    int minMbrZ = rs.getInt("minMbrZ");
                    int maxY = rs.getInt("maxY");
                    int minY = rs.getInt("minY");
                    int prior = rs.getInt("prior");
                    String world = rs.getString("world");
                    String date = rs.getString("date");
                    String wel = rs.getString("wel");
                    long value = rs.getLong("value");
                    boolean candel = rs.getBoolean("candelete");

                    Location<World> tppoint = null;
                    if (rs.getString("tppoint") != null && !rs.getString("tppoint").equalsIgnoreCase("")) {
                        String[] tpstring = rs.getString("tppoint").split(",");
                        tppoint = new Location<>(Sponge.getServer().getWorld(world).get(), Double.parseDouble(tpstring[0]), Double.parseDouble(tpstring[1]), Double.parseDouble(tpstring[2]));
                    }

                    String serverName = RedProtect.get().config.configRoot().region_settings.default_leader;
                    Set<PlayerRegion> leaders;
                    if (!rs.getString("leaders").isEmpty()) {
                        leaders = new HashSet<>(Arrays.asList(rs.getString("leaders").split(","))).stream().map(s -> {
                            String[] pi = s.split("@");
                            String[] p = new String[]{pi[0], pi.length == 2 ? pi[1] : pi[0]};
                            if (!p[0].equalsIgnoreCase(serverName) && !p[1].equalsIgnoreCase(serverName)) {
                                if (RedProtect.get().getUtil().isUUIDs(p[1])) {
                                    String before = p[1];
                                    p[1] = RedProtect.get().getUtil().UUIDtoPlayer(p[1]) == null ? p[1] : RedProtect.get().getUtil().UUIDtoPlayer(p[1]).toLowerCase();
                                    RedProtect.get().logger.success("Updated region " + rname + ", player &6" + before + " &ato &6" + p[1]);
                                }
                            }
                            return new PlayerRegion(p[0], p[1]);
                        }).collect(Collectors.toSet());
                    } else {
                        leaders = new HashSet<>();
                    }

                    Set<PlayerRegion> admins;
                    if (!rs.getString("admins").isEmpty()) {
                        admins = new HashSet<>(Arrays.asList(rs.getString("admins").split(","))).stream().map(s -> {
                            String[] pi = s.split("@");
                            String[] p = new String[]{pi[0], pi.length == 2 ? pi[1] : pi[0]};
                            if (!p[0].equalsIgnoreCase(serverName) && !p[1].equalsIgnoreCase(serverName)) {
                                if (RedProtect.get().getUtil().isUUIDs(p[1])) {
                                    String before = p[1];
                                    p[1] = RedProtect.get().getUtil().UUIDtoPlayer(p[1]) == null ? p[1] : RedProtect.get().getUtil().UUIDtoPlayer(p[1]).toLowerCase();
                                    RedProtect.get().logger.success("Updated region " + rname + ", player &6" + before + " &ato &6" + p[1]);
                                }
                            }
                            return new PlayerRegion(p[0], p[1]);
                        }).collect(Collectors.toSet());
                    } else {
                        admins = new HashSet<>();
                    }

                    Set<PlayerRegion> members;
                    if (!rs.getString("members").isEmpty()) {
                        members = new HashSet<>(Arrays.asList(rs.getString("members").split(","))).stream().map(s -> {
                            String[] pi = s.split("@");
                            String[] p = new String[]{pi[0], pi.length == 2 ? pi[1] : pi[0]};
                            if (!p[0].equalsIgnoreCase(serverName) && !p[1].equalsIgnoreCase(serverName)) {
                                if (RedProtect.get().getUtil().isUUIDs(p[1])) {
                                    String before = p[1];
                                    p[1] = RedProtect.get().getUtil().UUIDtoPlayer(p[1]) == null ? p[1] : RedProtect.get().getUtil().UUIDtoPlayer(p[1]).toLowerCase();
                                    RedProtect.get().logger.success("Updated region " + rname + ", player &6" + before + " &ato &6" + p[1]);
                                }
                            }
                            return new PlayerRegion(p[0], p[1]);
                        }).collect(Collectors.toSet());
                    } else {
                        members = new HashSet<>();
                    }

                    for (String flag : rs.getString("flags").split(",")) {
                        String key = flag.split(":")[0];
                        String replace = key + ":";
                        if (replace.length() <= flag.length()) {
                            flags.put(key, RedProtect.get().getUtil().parseObject(flag.substring(replace.length())));
                        }
                    }
                    Region newr = new Region(rname, admins, members, leaders, maxMbrX, minMbrX, maxMbrZ, minMbrZ, minY, maxY, flags, wel, prior, world, date, value, tppoint, candel);
                    regions.put(rname, newr);
                } catch (Exception e) {
                    RedProtect.get().logger.severe("Error on load region " + finalName);
                    CoreUtil.printJarVersion();
                    e.printStackTrace();
                }
            }
            st.close();
            rs.close();
        } catch (SQLException e) {
            CoreUtil.printJarVersion();
            e.printStackTrace();
        }
    }

    /*---------------------------------------------------------------------------------*/

    @Override
    public Set<Region> getLeaderRegions(String uuid) {
        Set<Region> regionsp = new HashSet<>();
        try {
            PreparedStatement st = this.dbcon.prepareStatement("SELECT name FROM `" + tableName + "` WHERE leaders LIKE ?");
            st.setString(1, "%" + uuid + "%");
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                regionsp.add(this.getRegion(rs.getString("name")));
            }
            st.close();
            rs.close();
        } catch (SQLException e) {
            CoreUtil.printJarVersion();
            e.printStackTrace();
        }
        return regionsp;
    }

    @Override
    public Set<Region> getAdminRegions(String uuid) {
        Set<Region> regionsp = new HashSet<>();
        try {
            PreparedStatement st = this.dbcon.prepareStatement("SELECT name FROM `" + tableName + "` WHERE leaders LIKE ? OR admins LIKE ?");
            st.setString(1, "%" + uuid + "%");
            st.setString(2, "%" + uuid + "%");
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                regionsp.add(this.getRegion(rs.getString("name")));
            }
            st.close();
            rs.close();
        } catch (SQLException e) {
            CoreUtil.printJarVersion();
            e.printStackTrace();
        }
        return regionsp;
    }

    @Override
    public Set<Region> getMemberRegions(String uuid) {
        Set<Region> regionsp = new HashSet<>();
        try {
            PreparedStatement st = this.dbcon.prepareStatement("SELECT name FROM `" + tableName + "` WHERE leaders LIKE ? OR admins LIKE ? OR members LIKE ?");
            st.setString(1, "%" + uuid + "%");
            st.setString(2, "%" + uuid + "%");
            st.setString(3, "%" + uuid + "%");
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                regionsp.add(this.getRegion(rs.getString("name")));
            }
            st.close();
            rs.close();
        } catch (SQLException e) {
            CoreUtil.printJarVersion();
            e.printStackTrace();
        }
        return regionsp;
    }

    @Override
    public Region getRegion(final String rname) {
        if (this.dbcon == null) {
            ConnectDB();
        }
        if (!regions.containsKey(rname)) {
            if (rname == null) {
                return null;
            }
            try {
                PreparedStatement st = this.dbcon.prepareStatement("SELECT * FROM `" + tableName + "` WHERE name=? AND world=?");
                st.setString(1, rname);
                st.setString(2, this.world);
                ResultSet rs = st.executeQuery();
                if (rs.next()) {
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
                    String world = rs.getString("world");
                    String date = rs.getString("date");
                    String wel = rs.getString("wel");
                    long value = rs.getLong("value");
                    boolean candel = rs.getBoolean("candelete");
                    Location<World> tppoint = null;
                    if (rs.getString("tppoint") != null && !rs.getString("tppoint").equalsIgnoreCase("")) {
                        String[] tpstring = rs.getString("tppoint").split(",");
                        tppoint = new Location<>(Sponge.getServer().getWorld(world).get(), Double.parseDouble(tpstring[0]), Double.parseDouble(tpstring[1]), Double.parseDouble(tpstring[2])/*,
                        		Float.parseFloat(tpstring[3]), Float.parseFloat(tpstring[4])*/);
                    }

                    String serverName = RedProtect.get().config.configRoot().region_settings.default_leader;

                    for (String member : rs.getString("members").split(", ")) {
                        String[] pi = member.split("@");
                        String[] p = new String[]{pi[0], pi.length == 2 ? pi[1] : pi[0]};
                        if (p[1].isEmpty()) continue;
                        if (!p[0].equalsIgnoreCase(serverName) && !p[1].equalsIgnoreCase(serverName)) {
                            if (RedProtect.get().getUtil().isUUIDs(p[1])) {
                                String before = p[1];
                                p[1] = RedProtect.get().getUtil().UUIDtoPlayer(p[1]) == null ? p[1] : RedProtect.get().getUtil().UUIDtoPlayer(p[1]).toLowerCase();
                                RedProtect.get().logger.success("Updated region " + rname + ", player &6" + before + " &ato &6" + p[1]);
                            }
                        }
                        members.add(new PlayerRegion(p[0], p[1]));
                    }

                    for (String admin : rs.getString("admins").split(", ")) {
                        String[] pi = admin.split("@");
                        String[] p = new String[]{pi[0], pi.length == 2 ? pi[1] : pi[0]};
                        if (p[1].isEmpty()) continue;
                        if (!p[0].equalsIgnoreCase(serverName) && !p[1].equalsIgnoreCase(serverName)) {
                            if (RedProtect.get().getUtil().isUUIDs(p[1])) {
                                String before = p[1];
                                p[1] = RedProtect.get().getUtil().UUIDtoPlayer(p[1]) == null ? p[1] : RedProtect.get().getUtil().UUIDtoPlayer(p[1]).toLowerCase();
                                RedProtect.get().logger.success("Updated region " + rname + ", player &6" + before + " &ato &6" + p[1]);
                            }
                        }
                        admins.add(new PlayerRegion(p[0], p[1]));
                    }
                    for (String leader : rs.getString("leaders").split(", ")) {
                        String[] pi = leader.split("@");
                        String[] p = new String[]{pi[0], pi.length == 2 ? pi[1] : pi[0]};
                        if (p[1].isEmpty()) continue;
                        if (!p[0].equalsIgnoreCase(serverName) && !p[1].equalsIgnoreCase(serverName)) {
                            if (RedProtect.get().getUtil().isUUIDs(p[1])) {
                                String before = p[1];
                                p[1] = RedProtect.get().getUtil().UUIDtoPlayer(p[1]) == null ? p[1] : RedProtect.get().getUtil().UUIDtoPlayer(p[1]).toLowerCase();
                                RedProtect.get().logger.success("Updated region " + rname + ", player &6" + before + " &ato &6" + p[1]);
                            }
                        }
                        leaders.add(new PlayerRegion(p[0], p[1]));
                    }

                    for (String flag : rs.getString("flags").split(",")) {
                        String key = flag.split(":")[0];
                        flags.put(key, RedProtect.get().getUtil().parseObject(flag.substring((key + ":").length())));
                    }

                    Region reg = new Region(rname, admins, members, leaders, maxMbrX, minMbrX, maxMbrZ, minMbrZ, minY, maxY, flags, wel, prior, world, date, value, tppoint, candel);
                    regions.put(rname, reg);
                } else {
                    return null;
                }
                st.close();
                rs.close();
                RedProtect.get().logger.debug(LogLevel.DEFAULT, "Adding region to cache: " + rname);
                Sponge.getScheduler().createSyncExecutor(RedProtect.get().container).schedule(() -> {
                    if (regions.containsKey(rname)) {
                        regions.remove(rname);
                        RedProtect.get().logger.debug(LogLevel.DEFAULT, "Removed cached region: " + rname);
                    }
                }, RedProtect.get().config.configRoot().mysql.region_cache_minutes, TimeUnit.MINUTES);
            } catch (SQLException e) {
                CoreUtil.printJarVersion();
                e.printStackTrace();
            }
        }
        return regions.get(rname);
    }

    @Override
    public int save(boolean force) {
        return 0;
    }

    @Override
    public int getTotalRegionSize(String uuid) {
        int total = 0;
        for (Region r2 : this.getLeaderRegions(uuid)) {
            total += RedProtect.get().getUtil().simuleTotalRegionSize(uuid, r2);
        }
        return total;
    }

    @Override
    public Set<Region> getRegionsNear(int px, int pz, int radius) {
        SortedSet<Region> ret = new TreeSet<>(Comparator.comparing(Region::getName));
        try {
            PreparedStatement st = this.dbcon.prepareStatement("SELECT name FROM `" + tableName + "` WHERE ABS(centerX-?)<=? AND ABS(centerZ-?)<=?");
            st.setInt(1, px);
            st.setInt(2, radius);
            st.setInt(3, pz);
            st.setInt(4, radius);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                ret.add(this.getRegion(rs.getString("name")));
            }
            st.close();
            rs.close();
        } catch (SQLException e) {
            CoreUtil.printJarVersion();
            e.printStackTrace();
        }
        return ret;
    }

    private boolean regionExists(String name) {
        int total = 0;
        try {
            PreparedStatement st = this.dbcon.prepareStatement("SELECT COUNT(*) FROM `" + tableName + "` WHERE name = ?");
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

    public String getWorld() {
        return this.world;
    }

    @Override
    public Set<Region> getRegions(int x, int y, int z) {
        Set<Region> regionl = new HashSet<>();
        try {
            PreparedStatement st = this.dbcon.prepareStatement("SELECT name FROM `" + tableName + "` WHERE ?<=maxMbrX AND ?>=minMbrX AND ?<=maxMbrZ AND ?>=minMbrZ AND ?<=maxY AND ?>=minY");
            st.setInt(1, x);
            st.setInt(2, x);
            st.setInt(3, z);
            st.setInt(4, z);
            st.setInt(5, y);
            st.setInt(6, y);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                regionl.add(this.getRegion(rs.getString("name")));
            }
            st.close();
            rs.close();
        } catch (SQLException e) {
            CoreUtil.printJarVersion();
            e.printStackTrace();
        }
        return regionl;
    }

    @Override
    public Region getTopRegion(int x, int y, int z) {
        Map<Integer, Region> regionlist = new HashMap<>();
        int max = 0;

        for (Region r : this.getRegions(x, y, z)) {
            if (x <= r.getMaxMbrX() && x >= r.getMinMbrX() && y <= r.getMaxY() && y >= r.getMinY() && z <= r.getMaxMbrZ() && z >= r.getMinMbrZ()) {
                if (regionlist.containsKey(r.getPrior())) {
                    Region reg1 = regionlist.get(r.getPrior());
                    int Prior = r.getPrior();
                    if (reg1.getArea() >= r.getArea()) {
                        r.setPrior(Prior + 1);
                    } else {
                        reg1.setPrior(Prior + 1);
                    }
                }
                regionlist.put(r.getPrior(), r);
            }
        }

        if (regionlist.size() > 0) {
            max = Collections.max(regionlist.keySet());
        }
        return regionlist.get(max);
    }

    @Override
    public Region getLowRegion(int x, int y, int z) {
        Map<Integer, Region> regionlist = new HashMap<>();
        int min = 0;

        for (Region r : this.getRegions(x, y, z)) {
            if (x <= r.getMaxMbrX() && x >= r.getMinMbrX() && y <= r.getMaxY() && y >= r.getMinY() && z <= r.getMaxMbrZ() && z >= r.getMinMbrZ()) {
                if (regionlist.containsKey(r.getPrior())) {
                    Region reg1 = regionlist.get(r.getPrior());
                    int Prior = r.getPrior();
                    if (reg1.getArea() >= r.getArea()) {
                        r.setPrior(Prior + 1);
                    } else {
                        reg1.setPrior(Prior + 1);
                    }
                }
                regionlist.put(r.getPrior(), r);
            }
        }

        if (regionlist.size() > 0) {
            min = Collections.min(regionlist.keySet());
        }
        return regionlist.get(min);
    }

    public Map<Integer, Region> getGroupRegion(int x, int y, int z) {
        Map<Integer, Region> regionlist = new HashMap<>();

        for (Region r : this.getRegions(x, y, z)) {
            if (x <= r.getMaxMbrX() && x >= r.getMinMbrX() && y <= r.getMaxY() && y >= r.getMinY() && z <= r.getMaxMbrZ() && z >= r.getMinMbrZ()) {
                if (regionlist.containsKey(r.getPrior())) {
                    Region reg1 = regionlist.get(r.getPrior());
                    int Prior = r.getPrior();
                    if (reg1.getArea() >= r.getArea()) {
                        r.setPrior(Prior + 1);
                    } else {
                        reg1.setPrior(Prior + 1);
                    }
                }
                regionlist.put(r.getPrior(), r);
            }
        }
        return regionlist;
    }

    @Override
    public Set<Region> getAllRegions() {
        Set<Region> allregions = new HashSet<>();
        try {
            PreparedStatement st = this.dbcon.prepareStatement("SELECT name FROM `" + tableName + "`");
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                allregions.add(getRegion(rs.getString("name")));
            }
            st.close();
            rs.close();
        } catch (SQLException e) {
            CoreUtil.printJarVersion();
            e.printStackTrace();
        }
        return allregions;
    }

    @Override
    public void clearRegions() {
        regions.clear();
		/*
		try {
            PreparedStatement st = this.dbcon.prepareStatement();
            st.executeUpdate("DELETE FROM region_flags WHERE region = '*'");

            st = this.dbcon.prepareStatement();
            st.executeUpdate("DELETE FROM region WHERE name = '*'");
            st.close();
        }
        catch (SQLException e) {
            CoreUtil.printJarVersion();
            e.printStackTrace();
        }		*/
    }

    @Override
    public void closeConn() {
        try {
            if (this.dbcon != null && !this.dbcon.isClosed()) {
                this.dbcon.close();
            }
        } catch (SQLException e) {
            RedProtect.get().logger.severe("No connections to close! Forget this message ;)");
        }
    }

    private void ConnectDB() {
        try {
            this.dbcon = DriverManager.getConnection(this.url + this.dbname + this.reconnect + (RedProtect.get().config.configRoot().mysql.ssl ? "&useSSL=true&requireSSL=true":"")
                    , RedProtect.get().config.configRoot().mysql.user_name, RedProtect.get().config.configRoot().mysql.user_pass);
            RedProtect.get().logger.info("Conected to " + this.tableName + " via Mysql!");
        } catch (SQLException e) {
            CoreUtil.printJarVersion();
            e.printStackTrace();
            RedProtect.get().logger.severe("[" + dbname + "] Theres was an error while connecting to Mysql database! RedProtect will try to connect again in 15 seconds. If still not connecting, check the DB configurations and reload.");
        }
    }

    @Override
    public int getTotalRegionNum() {
        int total = 0;
        try {
            PreparedStatement st = this.dbcon.prepareStatement("SELECT COUNT(*) FROM `" + tableName + "`");
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                total = rs.getInt("COUNT(*)");
            }
            st.close();
            rs.close();
        } catch (SQLException e) {
            RedProtect.get().logger.severe("Error on get total of regions for " + tableName + "!");
            CoreUtil.printJarVersion();
            e.printStackTrace();
        }
        return total;
    }

}