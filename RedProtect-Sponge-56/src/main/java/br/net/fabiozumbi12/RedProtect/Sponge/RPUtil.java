/*
 Copyright @FabioZumbi12

 This class is provided 'as-is', without any express or implied warranty. In no event will the authors be held liable for any
  damages arising from the use of this class.

 Permission is granted to anyone to use this class for any purpose, including commercial plugins, and to alter it and
 redistribute it freely, subject to the following restrictions:
 1 - The origin of this class must not be misrepresented; you must not claim that you wrote the original software. If you
 use this class in other plugins, an acknowledgment in the plugin documentation would be appreciated but is not required.
 2 - Altered source versions must be plainly marked as such, and must not be misrepresented as being the original class.
 3 - This notice may not be removed or altered from any source distribution.

 Esta classe é fornecida "como está", sem qualquer garantia expressa ou implícita. Em nenhum caso os autores serão
 responsabilizados por quaisquer danos decorrentes do uso desta classe.

 É concedida permissão a qualquer pessoa para usar esta classe para qualquer finalidade, incluindo plugins pagos, e para
 alterá-lo e redistribuí-lo livremente, sujeito às seguintes restrições:
 1 - A origem desta classe não deve ser deturpada; você não deve afirmar que escreveu a classe original. Se você usar esta
  classe em um plugin, uma confirmação de autoria na documentação do plugin será apreciada, mas não é necessária.
 2 - Versões de origem alteradas devem ser claramente marcadas como tal e não devem ser deturpadas como sendo a
 classe original.
 3 - Este aviso não pode ser removido ou alterado de qualquer distribuição de origem.
 */

package br.net.fabiozumbi12.RedProtect.Sponge;

import br.net.fabiozumbi12.RedProtect.Sponge.config.RPLang;
import br.net.fabiozumbi12.RedProtect.Sponge.hooks.WEListener;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@SuppressWarnings("deprecation")
public class RPUtil {
    public static boolean stopRegen;

    public static Text toText(String str) {
        return TextSerializers.FORMATTING_CODE.deserialize(str);
    }

    public static boolean isBukkitBlock(BlockState b) {
        RedProtect.get().logger.debug(LogLevel.DEFAULT, "BlockType: " + b.getType().getName());
        return b.getType().getName().startsWith("minecraft:");
    }

    public static boolean isBukkitEntity(Entity e) {
        RedProtect.get().logger.debug(LogLevel.DEFAULT, "EntityType: " + e.getType().getName());
        return Sponge.getGame().getRegistry().getType(EntityType.class, e.getType().getName()).isPresent();
    }

    public static Transform<World> DenyExitPlayer(Player p, Transform<World> from, Transform<World> to, Region r) {
        Region rto = RedProtect.get().rm.getTopRegion(to.getLocation(), RPUtil.class.getName());
        if (rto != r) {
            to = new Transform<>(from.getLocation()).setRotation(from.getRotation());
            RPLang.sendMessage(p, "playerlistener.region.cantregionexit");
        }
        return to;
    }

    public static Transform<World> DenyEnterPlayer(World wFrom, Transform<World> from, Transform<World> to, Region r, boolean checkSec) {
        Location<World> setFrom = from.getLocation();
        for (int i = 0; i < r.getArea() + 10; i++) {
            Region r1 = RedProtect.get().rm.getTopRegion(wFrom, setFrom.getBlockX() + i, setFrom.getBlockY(), setFrom.getBlockZ(), RPUtil.class.getName());
            Region r2 = RedProtect.get().rm.getTopRegion(wFrom, setFrom.getBlockX() - i, setFrom.getBlockY(), setFrom.getBlockZ(), RPUtil.class.getName());
            Region r3 = RedProtect.get().rm.getTopRegion(wFrom, setFrom.getBlockX(), setFrom.getBlockY(), setFrom.getBlockZ() + i, RPUtil.class.getName());
            Region r4 = RedProtect.get().rm.getTopRegion(wFrom, setFrom.getBlockX(), setFrom.getBlockY(), setFrom.getBlockZ() - i, RPUtil.class.getName());
            Region r5 = RedProtect.get().rm.getTopRegion(wFrom, setFrom.getBlockX() + i, setFrom.getBlockY(), setFrom.getBlockZ() + i, RPUtil.class.getName());
            Region r6 = RedProtect.get().rm.getTopRegion(wFrom, setFrom.getBlockX() - i, setFrom.getBlockY(), setFrom.getBlockZ() - i, RPUtil.class.getName());
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
            RedProtect.get().getPVHelper().setBlock(to.getLocation().getBlockRelative(Direction.DOWN), BlockTypes.GRASS.getDefaultState());
        }
        return to;
    }

    private static boolean isSecure(Location loc) {
        BlockState b = loc.add(0, -1, 0).getBlock();
        return (!b.getType().equals(BlockTypes.AIR) && !b.getType().equals(BlockTypes.WATER)) || b.getType().getName().contains("LAVA");
    }

    public static List<Location<World>> get4Points(Location<World> min, Location<World> max, int y) {
        List<Location<World>> locs = new ArrayList<>();
        locs.add(new Location<>(min.getExtent(), min.getX(), y, min.getZ()));
        locs.add(new Location<>(min.getExtent(), min.getX(), y, min.getZ() + (max.getZ() - min.getZ())));
        locs.add(new Location<>(max.getExtent(), max.getX(), y, max.getZ()));
        locs.add(new Location<>(min.getExtent(), min.getX() + (max.getX() - min.getX()), y, min.getZ()));
        return locs;
    }

    /*
        public static long getNowMillis(){
            SimpleDateFormat sdf = new SimpleDateFormat(RedProtect.get().cfgs.root().region_settings.date_format);
            Calendar cal = Calendar.getInstance();
            try {
                cal.setTime(sdf.parse(sdf.format(cal.getTime())));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return cal.getTimeInMillis();
        }
        */
    private static void saveToZipFile(File file, String ZippedFile, CommentedConfigurationNode conf) {
        try {
            final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file));
            ZipEntry e = new ZipEntry(ZippedFile);
            out.putNextEntry(e);

            byte[] data = conf.toString().getBytes();
            out.write(data, 0, data.length);
            out.closeEntry();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean removeGuiItem(ItemStack item) {
        if (item.get(Keys.ITEM_LORE).isPresent()) {
            try {
                String lore = item.get(Keys.ITEM_LORE).get().get(1).toPlain();
                if (RedProtect.get().cfgs.getDefFlags().contains(lore.replace("§0", "")) || lore.equals(RedProtect.get().cfgs.getGuiString("separator").toPlain())) {
                    return true;
                }
            } catch (IndexOutOfBoundsException ignored) {
            }
        }
        return false;
    }

    public static boolean isGuiItem(ItemStack pitem) {
        if (pitem.get(Keys.ITEM_LORE).isPresent() &&
                pitem.get(Keys.ITEM_LORE).get().size() >= 1 &&
                RedProtect.get().cfgs.getDefFlags().contains(pitem.get(Keys.ITEM_LORE).get().get(1).toPlain().replace("§0", ""))) {
            return true;
        }
        if (pitem.get(Keys.ITEM_LORE).isPresent()) {
            List<Text> lore = pitem.get(Keys.ITEM_LORE).get();
            return RedProtect.get().cfgs.getGuiSeparator().get(Keys.ITEM_LORE).get().equals(lore);
        }
        return false;
    }

    static void SaveToZipSB(File file, String ZippedFile, StringBuilder sb) {
        try {
            final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file));
            ZipEntry e = new ZipEntry(ZippedFile);
            out.putNextEntry(e);

            byte[] data = sb.toString().getBytes();
            out.write(data, 0, data.length);
            out.closeEntry();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static File genFileName(String Path, Boolean isBackup) {
        File f = new File(Path);
        if (!f.exists()) {
            f.mkdir();
        }
        int count = 1;
        String date = DateNow().replace("/", "-");
        File logfile = new File(f, date + "-" + count + ".zip");
        File[] files = f.listFiles();
        HashMap<Long, File> keyFiles = new HashMap<>();
        if (files.length >= RedProtect.get().cfgs.root().flat_file.max_backups && isBackup) {
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

    /**
     * Generate a friendly and unique name for a region based on player name.
     *
     * @param p     Player
     * @param World World
     * @return Name of region
     */
    public static String nameGen(String p, String World) {
        String rname;
        World w = RedProtect.get().serv.getWorld(World).get();
        int i = 0;
        while (true) {
            int is = String.valueOf(i).length();
            if (p.length() > 13) {
                rname = p.substring(0, 14 - is) + "_" + i;
            } else {
                rname = p + "_" + i;
            }
            rname = rname.replace(" ", "_").replaceAll("[^\\p{L}_0-9 ]", "");
            if (RedProtect.get().rm.getRegion(rname, w) == null) {
                break;
            }
            ++i;
        }
        return rname;
    }

    /*
    static String formatName(String name) {
        String s = name.substring(1).toLowerCase();
        String fs = name.substring(0, 1).toUpperCase();
        String ret = String.valueOf(fs) + s;
        ret = ret.replace("_", " ");
        return ret;
    }
    
    static int[] toIntArray(List<Integer> list) {
        int[] ret = new int[list.size()];
        int i = 0;
        for (Integer e : list) {
            ret[i++] = e;
        }
        return ret;
    }
    */
    public static String DateNow() {
        DateFormat df = new SimpleDateFormat(RedProtect.get().cfgs.root().region_settings.date_format);
        Date today = Calendar.getInstance().getTime();
        return df.format(today);
    }

    static String HourNow() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int min = Calendar.getInstance().get(Calendar.MINUTE);
        int sec = Calendar.getInstance().get(Calendar.SECOND);
        return "[" + hour + ":" + min + ":" + sec + "]";
    }

    /*
    static void fixWorld(String regionname){
    	for (World w:RedProtect.get().serv.getWorlds()){
    		Region r = RedProtect.get().rm.getRegion(regionname, w);
    		if (r != null){
    			r.setWorld(w.getName());
    		}
    	}
    }
    */
    //TODO read all db
    static void ReadAllDB(Set<Region> regions) {
        int i = 0;
        int pls = 0;
        int origupdt = 0;
        int namesupdt = 0;
        int purged = 0;
        int sell = 0;
        int dateint = 0;
        int cfm = 0;
        int delay = 0;
        int skipped = 0;
        Date now = null;
        SimpleDateFormat dateformat = new SimpleDateFormat(RedProtect.get().cfgs.root().region_settings.date_format);

        boolean checkNames = RedProtect.get().cfgs.root().hooks.check_uuid_names_onstart;

        try {
            now = dateformat.parse(DateNow());
        } catch (ParseException e1) {
            RedProtect.get().logger.severe("The 'date-format' don't match with date 'now'!!");
        }

        for (Region r : regions) {
            boolean serverRegion = false;

            if (r.isLeader(RedProtect.get().cfgs.root().region_settings.default_leader)) {
                serverRegion = true;
                r.setDate(DateNow());
            }

            //purge regions
            if (RedProtect.get().cfgs.root().purge.enabled && !serverRegion) {
                Date regiondate = null;
                try {
                    regiondate = dateformat.parse(r.getDate());
                } catch (ParseException e) {
                    RedProtect.get().logger.severe("The 'date-format' don't match with region date!!");
                    e.printStackTrace();
                }
                Long days = TimeUnit.DAYS.convert(now.getTime() - regiondate.getTime(), TimeUnit.MILLISECONDS);

                boolean ignore = false;
                for (String play : RedProtect.get().cfgs.root().purge.ignore_regions_from_players) {
                    if (r.isLeader(RPUtil.PlayerToUUID(play)) || r.isAdmin(RPUtil.PlayerToUUID(play))) {
                        ignore = true;
                        break;
                    }
                }

                if (!ignore && days > RedProtect.get().cfgs.root().purge.remove_oldest) {
                    RedProtect.get().rm.remove(r, RedProtect.get().serv.getWorld(r.getWorld()).get());
                    //r.delete();
                    purged++;
                    RedProtect.get().logger.warning("Purging " + r.getName() + " - Days: " + days);

                    if (RedProtect.get().WE && RedProtect.get().cfgs.root().purge.regen.enable) {
                        if (r.getArea() <= RedProtect.get().cfgs.root().purge.regen.max_area_regen) {
                            WEListener.regenRegion(r, Sponge.getServer().getWorld(r.getWorld()).get(), r.getMaxLocation(), r.getMinLocation(), delay, null, true);
                            delay = delay + 10;
                        } else {
                            skipped++;
                            continue;
                        }
                    } else {
                        RedProtect.get().rm.remove(r, RedProtect.get().serv.getWorld(r.getWorld()).get());
                        //r.delete();
                        purged++;
                        RedProtect.get().logger.warning("Purging " + r.getName() + " - Days: " + days);
                    }
                    continue;
                }
            }


            //sell rergions
            if (RedProtect.get().cfgs.root().sell.enabled && !serverRegion) {
                Date regiondate = null;
                try {
                    regiondate = dateformat.parse(r.getDate());
                } catch (ParseException e) {
                    RedProtect.get().logger.severe("The 'date-format' don't match with region date!!");
                    e.printStackTrace();
                }
                Long days = TimeUnit.DAYS.convert(now.getTime() - regiondate.getTime(), TimeUnit.MILLISECONDS);

                boolean ignore = false;
                for (String play : RedProtect.get().cfgs.root().sell.ignore_regions_from_players) {
                    if (r.isLeader(RPUtil.PlayerToUUID(play)) || r.isAdmin(RPUtil.PlayerToUUID(play))) {
                        ignore = true;
                        break;
                    }
                }

                if (!ignore && days > RedProtect.get().cfgs.root().sell.sell_oldest) {
                    RedProtect.get().logger.warning("Selling " + r.getName() + " - Days: " + days);
                    RPEconomy.putToSell(r, RedProtect.get().cfgs.root().region_settings.default_leader, RPEconomy.getRegionValue(r));
                    sell++;
                    RedProtect.get().rm.saveAll();
                    continue;
                }
            }

            //Update player names
            Set<String> leadersl = r.getLeaders();
            Set<String> adminsl = r.getAdmins();
            Set<String> membersl = r.getMembers();

            if (origupdt >= 90 || namesupdt >= 90) {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (!serverRegion && checkNames) {
                if (RedProtect.get().OnlineMode) {
                    for (String pname : leadersl) {
                        pname = pname.replace("[", "").replace("]", "");
                        if (!isUUIDs(pname) && !RPUtil.isDefaultServer(pname)) {
                            String uuid = MojangUUIDs.getUUID(pname);
                            if (uuid == null) {
                                uuid = PlayerToUUID(pname);
                            }
                            RedProtect.get().logger.warning("Leader from: " + pname);
                            leadersl.remove(pname);
                            leadersl.add(uuid);
                            RedProtect.get().logger.warning("To UUID: " + uuid);
                            origupdt++;
                        }
                    }
                    for (String pname : adminsl) {
                        pname = pname.replace("[", "").replace("]", "");
                        if (!isUUIDs(pname) && !RPUtil.isDefaultServer(pname)) {
                            String uuid = MojangUUIDs.getUUID(pname);
                            if (uuid == null) {
                                uuid = PlayerToUUID(pname);
                            }
                            RedProtect.get().logger.warning("Admin from: " + pname);
                            adminsl.remove(pname);
                            adminsl.add(uuid);
                            RedProtect.get().logger.warning("To UUID: " + uuid);
                            origupdt++;
                        }
                    }
                    for (String pname : membersl) {
                        pname = pname.replace("[", "").replace("]", "");
                        if (!isUUIDs(pname) && !RPUtil.isDefaultServer(pname)) {
                            String uuid = MojangUUIDs.getUUID(pname);
                            if (uuid == null) {
                                uuid = PlayerToUUID(pname);
                            }
                            RedProtect.get().logger.warning("Member from: " + pname);
                            membersl.remove(pname);
                            membersl.add(uuid);
                            RedProtect.get().logger.warning("To UUID: " + uuid);
                            origupdt++;
                        }
                    }
                    r.setLeaders(leadersl);
                    r.setAdmins(adminsl);
                    r.setMembers(membersl);
                    if (origupdt > 0) {
                        pls++;
                    }
                }
                //if Offline Mode
                else {
                    for (String pname : leadersl) {
                        if (isUUIDs(pname) && !RPUtil.isDefaultServer(pname)) {
                            try {
                                String name = MojangUUIDs.getName(pname);
                                if (name == null) {
                                    name = UUIDtoPlayer(pname);
                                }
                                RedProtect.get().logger.warning("Leader from: " + pname);
                                leadersl.remove(pname);
                                leadersl.add(name.toLowerCase());
                                RedProtect.get().logger.warning("To UUID: " + name);
                                namesupdt++;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    for (String pname : adminsl) {
                        if (isUUIDs(pname) && !RPUtil.isDefaultServer(pname)) {
                            try {
                                String name = MojangUUIDs.getName(pname);
                                if (name == null) {
                                    name = UUIDtoPlayer(pname);
                                }
                                RedProtect.get().logger.warning("Admin from: " + pname);
                                adminsl.remove(pname);
                                adminsl.add(name.toLowerCase());
                                RedProtect.get().logger.warning("To UUID: " + name);
                                namesupdt++;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    for (String pname : membersl) {
                        if (isUUIDs(pname) && !RPUtil.isDefaultServer(pname)) {
                            try {
                                String name = MojangUUIDs.getName(pname);
                                if (name == null) {
                                    name = UUIDtoPlayer(pname);
                                }
                                RedProtect.get().logger.warning("Member from: " + pname);
                                membersl.remove(pname);
                                membersl.add(name.toLowerCase());
                                RedProtect.get().logger.warning("To UUID: " + name);
                                namesupdt++;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    r.setLeaders(leadersl);
                    r.setAdmins(adminsl);
                    r.setMembers(membersl);
                    if (namesupdt > 0) {
                        pls++;
                    }
                }
            }

            if (pls > 0) {
                RedProtect.get().logger.sucess("[" + pls + "]Region updated &6&l" + r.getName() + "&a&l. Leaders &6&l" + r.getLeadersDesc());
            }

            //conform region names
            if (r.getName().contains("/")) {
                String rname = r.getName().replace("/", "|");
                RedProtect.get().rm.renameRegion(rname, r);
                cfm++;
            }
        }

        if (delay > 0) {
            RedProtect.get().logger.warning("&c> There's " + delay / 10 + " regions to be regenerated at 2 regions/second.");
            RedProtect.get().logger.severe("&cRegen can take long time, but your players can join and play normally!");
        }

        if (cfm > 0) {
            RedProtect.get().logger.sucess("[" + cfm + "] Region names conformed!");
        }

        if (pls > 0) {
            RedProtect.get().logger.sucess("Updated a total of &6&l" + (pls) + "&a&l regions!");
            RedProtect.get().rm.saveAll();
            RedProtect.get().logger.sucess("Regions saved!");
        }

        if (skipped > 0) {
            RedProtect.get().logger.sucess(skipped + " regions skipped due to max size limit to regen!");
        }

        if (purged > 0) {
            RedProtect.get().logger.warning("Purged a total of &6&l" + purged + "&a&l regions!");
        }

        if (sell > 0) {
            RedProtect.get().logger.warning("Put to sell a total of &6&l" + sell + "&a&l regions!");
        }
        regions.clear();
    }


    private static boolean isDefaultServer(String check) {
        return check.equalsIgnoreCase(RedProtect.get().cfgs.root().region_settings.default_leader);
    }

    public static String PlayerToUUID(String PlayerName) {
        if (PlayerName == null || PlayerName.equals("")) {
            return null;
        }

        //check if is already UUID
        if (isUUIDs(PlayerName) || isDefaultServer(PlayerName) || (PlayerName.startsWith("[") && PlayerName.endsWith("]"))) {
            return PlayerName;
        }

        String uuid = PlayerName;

        if (!RedProtect.get().OnlineMode) {
            uuid = uuid.toLowerCase();
            return uuid;
        }

        UserStorageService uss = Sponge.getGame().getServiceManager().provide(UserStorageService.class).get();

        Optional<GameProfile> ogpName = uss.getAll().stream().filter(f -> f.getName().isPresent() && f.getName().get().equalsIgnoreCase(PlayerName)).findFirst();
        if (ogpName.isPresent()) {
            return ogpName.get().getUniqueId().toString();
        } else {
            Optional<Player> p = RedProtect.get().serv.getPlayer(PlayerName);
            if (p.isPresent()) {
                return p.get().getUniqueId().toString();
            }
        }

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

        String PlayerName = "UnknowPlayer";
        UUID uuids = UUID.fromString(uuid);

        if (!RedProtect.get().OnlineMode) {
            return uuid.toLowerCase();
        }

        UserStorageService uss = Sponge.getGame().getServiceManager().provide(UserStorageService.class).get();
    	/*
		Optional<GameProfile> ogpName = uss.getAll().stream().filter(f -> f.getUniqueId().equals(uuids)).findFirst();
		if (ogpName.isPresent()){
			return ogpName.get().getName().get();
		}
		*/
        if (uss.get(uuids).isPresent()) {
            return uss.get(uuids).get().getName();
        }

        return PlayerName;
    }

    public static User getUser(String name) {
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

    private static boolean isUUIDs(String uuid) {
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

    /*
    static void addRegion(List<Region> regions, World w){    	
    	for (int i = 0; i < regions.size(); i++){
    		if (!RedProtect.get().rm.getRegionsByWorld(w).contains(regions.get(i))){
    			RedProtect.get().logger.warning("["+(i+1)+"/"+regions.size()+"]Adding regions to database! This may take some time...");
        		RedProtect.get().rm.add(regions.get(i), w);       		                		
    		}
		}	 
    	regions.clear();
    }
    */
    public static Object parseObject(String value) {
        Object obj = value;
        try {
            obj = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                obj = Boolean.parseBoolean(value);
            }
        }
        return obj;
    }

    public static boolean mysqlToFile() {
        HashMap<String, Region> regions = new HashMap<>();
        int saved = 1;

        try {
            Connection dbcon = DriverManager.getConnection("jdbc:mysql://" + RedProtect.get().cfgs.root().mysql.host + "/" + RedProtect.get().cfgs.root().mysql.db_name + "?autoReconnect=true", RedProtect.get().cfgs.root().mysql.user_name, RedProtect.get().cfgs.root().mysql.user_pass);

            for (World world : Sponge.getServer().getWorlds()) {
                String tableName = RedProtect.get().cfgs.root().mysql.table_prefix + world.getName();
                PreparedStatement st = dbcon.prepareStatement("SELECT * FROM `" + tableName + "` WHERE world=?");
                st.setString(1, world.getName());
                ResultSet rs = st.executeQuery();
                while (rs.next()) {
                    Set<String> leaders = new HashSet<>();
                    Set<String> admins = new HashSet<>();
                    Set<String> members = new HashSet<>();
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

                    Location<World> tppoint = null;
                    if (rs.getString("tppoint") != null && !rs.getString("tppoint").equalsIgnoreCase("")) {
                        String[] tpstring = rs.getString("tppoint").split(",");
                        tppoint = new Location<>(world, Double.parseDouble(tpstring[0]), Double.parseDouble(tpstring[1]), Double.parseDouble(tpstring[2]))/*,
                        		Float.parseFloat(tpstring[3]), Float.parseFloat(tpstring[4]))*/;
                    }

                    for (String member : rs.getString("members").split(", ")) {
                        if (member.length() > 0) {
                            members.add(member);
                        }
                    }
                    for (String admin : rs.getString("admins").split(", ")) {
                        if (admin.length() > 0) {
                            admins.add(admin);
                        }
                    }
                    for (String leader : rs.getString("leaders").split(", ")) {
                        if (leader.length() > 0) {
                            leaders.add(leader);
                        }
                    }
                    for (String flag : rs.getString("flags").split(",")) {
                        String key = flag.split(":")[0];
                        String replace = key + ":";
                        if (replace.length() <= flag.length()) {
                            flags.put(key, RPUtil.parseObject(flag.substring(replace.length())));
                        }
                    }
                    Region newr = new Region(rname, admins, members, leaders, maxMbrX, minMbrX, maxMbrZ, minMbrZ, minY, maxY, flags, wel, prior, world.getName(), date, value, tppoint, true);

                    regions.put(rname, newr);
                }
                st.close();
                rs.close();

                File datf = new File(RedProtect.get().configDir + File.separator + "data", "data_" + world.getName() + ".conf");
                ConfigurationLoader<CommentedConfigurationNode> regionManager = HoconConfigurationLoader.builder().setPath(datf.toPath()).build();
                CommentedConfigurationNode fileDB = regionManager.createEmptyNode();

                for (Region r : regions.values()) {
                    if (r.getName() == null) {
                        continue;
                    }

                    if (RedProtect.get().cfgs.root().flat_file.region_per_file) {
                        if (!r.toSave()) {
                            continue;
                        }
                        datf = new File(RedProtect.get().configDir + File.separator + "data", world.getName() + File.separator + r.getName() + ".conf");
                        regionManager = HoconConfigurationLoader.builder().setPath(datf.toPath()).build();
                        fileDB = regionManager.createEmptyNode();
                    }

                    RPUtil.addProps(fileDB, r);
                    saved++;

                    if (RedProtect.get().cfgs.root().flat_file.region_per_file) {
                        saveConf(fileDB, regionManager);
                        r.setToSave(false);
                    }
                }

                if (!RedProtect.get().cfgs.root().flat_file.region_per_file) {
                    backupRegions(fileDB, world.getName());
                    saveConf(fileDB, regionManager);
                } else {
                    //remove deleted regions
                    File wfolder = new File(RedProtect.get().configDir + File.separator + "data", world.getName());
                    if (wfolder.exists()) {
                        File[] listOfFiles = wfolder.listFiles();
                        for (File region : listOfFiles) {
                            if (region.isFile() && !regions.containsKey(region.getName().replace(".conf", ""))) {
                                region.delete();
                            }
                        }
                    }
                }
            }
            dbcon.close();

            if (saved > 0) {
                RedProtect.get().logger.sucess((saved - 1) + " regions converted to File with sucess!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return true;
    }

    public static boolean fileToMysql() throws Exception {
        if (!RedProtect.get().cfgs.root().file_type.equalsIgnoreCase("file")) {
            return false;
        }
        RedProtect.get().rm.saveAll();

        initMysql();//Create tables
        int counter = 1;

        for (World world : Sponge.getServer().getWorlds()) {

            String dbname = RedProtect.get().cfgs.root().mysql.db_name;
            String url = "jdbc:mysql://" + RedProtect.get().cfgs.root().mysql.host + "/";
            String tableName = RedProtect.get().cfgs.root().mysql.table_prefix + world.getName();

            Connection dbcon = DriverManager.getConnection(url + dbname, RedProtect.get().cfgs.root().mysql.user_name, RedProtect.get().cfgs.root().mysql.user_pass);

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
                        e.printStackTrace();
                    }
                }
            }
            dbcon.close();
        }
        if (counter > 0) {
            RedProtect.get().logger.sucess((counter - 1) + " regions converted to Mysql with sucess!");
        }
        return true;
    }

    private static void initMysql() throws Exception {
        for (World world : Sponge.getServer().getWorlds()) {

            String url = "jdbc:mysql://" + RedProtect.get().cfgs.root().mysql.host + "/";
            String reconnect = "?autoReconnect=true";
            String tableName = RedProtect.get().cfgs.root().mysql.table_prefix + world.getName();

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
                    Connection con = DriverManager.getConnection(url + RedProtect.get().cfgs.root().mysql.db_name + reconnect, RedProtect.get().cfgs.root().mysql.user_name, RedProtect.get().cfgs.root().mysql.user_pass);
                    st = con.prepareStatement("CREATE TABLE `" + tableName + "` "
                            + "(name varchar(20) PRIMARY KEY NOT NULL, leaders varchar(36), admins longtext, members longtext, maxMbrX int, minMbrX int, maxMbrZ int, minMbrZ int, centerX int, centerZ int, minY int, maxY int, date varchar(10), wel longtext, prior int, world varchar(100), value Long not null, tppoint mediumtext, rent longtext, flags longtext, candelete tinyint(1)) CHARACTER SET utf8 COLLATE utf8_general_ci");
                    st.executeUpdate();
                    st.close();
                    st = null;
                    RedProtect.get().logger.info("Created table: " + tableName + "!");
                }
                addNewColumns(tableName);
            } catch (SQLException e) {
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
            String url = "jdbc:mysql://" + RedProtect.get().cfgs.root().mysql.host + "/";
            Connection con = DriverManager.getConnection(url + RedProtect.get().cfgs.root().mysql.db_name, RedProtect.get().cfgs.root().mysql.user_name, RedProtect.get().cfgs.root().mysql.user_pass);
            DatabaseMetaData md = con.getMetaData();
            ResultSet rs = md.getColumns(null, null, tableName, "candelete");
            if (!rs.next()) {
                PreparedStatement st = con.prepareStatement("ALTER TABLE `" + tableName + "` ADD `candelete` tinyint(1) NOT NULL default '1'");
                st.executeUpdate();
            }
            rs.close();
            rs = md.getColumns(null, null, tableName, "rent");
            if (!rs.next()) {
                PreparedStatement st = con.prepareStatement("ALTER TABLE `" + tableName + "` ADD `rent` longtext");
                st.executeUpdate();
            }
            rs.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void backupRegions(CommentedConfigurationNode fileDB, String world) {
        if (!RedProtect.get().cfgs.root().flat_file.backup) {
            return;
        }

        File bfolder = new File(RedProtect.get().configDir + File.separator + "backups" + File.separator);
        if (!bfolder.exists()) {
            bfolder.mkdir();
        }

        File folder = new File(RedProtect.get().configDir + File.separator + "backups" + File.separator + world + File.separator);
        if (!folder.exists()) {
            folder.mkdir();
            RedProtect.get().logger.info("Created folder: " + folder.getPath());
        }

        //Save backup
        if (RPUtil.genFileName(folder.getPath() + File.separator, true) != null) {
            RPUtil.saveToZipFile(RPUtil.genFileName(folder.getPath() + File.separator, true), "data_" + world + ".conf", fileDB);
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
            e.printStackTrace();
        }
        return total > 0;
    }

    private static boolean checkTableExists(String tableName) throws SQLException {
        try {
            RedProtect.get().logger.debug(LogLevel.DEFAULT, "Checking if table exists... " + tableName);
            Connection con = DriverManager.getConnection("jdbc:mysql://" + RedProtect.get().cfgs.root().mysql.host + "/" + RedProtect.get().cfgs.root().mysql.db_name, RedProtect.get().cfgs.root().mysql.user_name, RedProtect.get().cfgs.root().mysql.user_pass);
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
            e.printStackTrace();
        }
        return false;
    }

    public static void startFlagChanger(final String r, final String flag, final Player p) {
        RedProtect.get().changeWait.add(r + flag);
        Sponge.getScheduler().createSyncExecutor(RedProtect.get().container).schedule(() -> {
            /*if (p != null && p.isOnline()){
                    RPLang.sendMessage(p, RPLang.get("gui.needwait.ready").replace("{flag}", flag));
                }*/
            RedProtect.get().changeWait.remove(r + flag);
        }, RedProtect.get().cfgs.root().flags_configuration.change_flag_delay.seconds, TimeUnit.SECONDS);
    }

    public static int getUpdatedPrior(Region region) {
        int regionarea = region.getArea();
        int prior = region.getPrior();
        Region topRegion = RedProtect.get().rm.getTopRegion(RedProtect.get().serv.getWorld(region.getWorld()).get(), region.getCenterX(), region.getCenterY(), region.getCenterZ(), RPUtil.class.getName());
        Region lowRegion = RedProtect.get().rm.getLowRegion(RedProtect.get().serv.getWorld(region.getWorld()).get(), region.getCenterX(), region.getCenterY(), region.getCenterZ());

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
     * @param p    player
     * @param locs {@code List<Location>}.
     */
    public static void addBorder(final Player p, List<Location<World>> locs) {
        for (final Location<World> loc : locs) {
            p.sendBlockChange(loc.getBlockPosition(), RedProtect.get().cfgs.getBorderMaterial().getDefaultState());

            Sponge.getScheduler().createSyncExecutor(RedProtect.get().container).schedule(() -> {
                p.resetBlockChange(loc.getBlockPosition());
            }, RedProtect.get().cfgs.root().region_settings.border.time_showing, TimeUnit.SECONDS);
        }
    }

    public static int simuleTotalRegionSize(String player, Region r2) {
        int total = 0;
        int regs = 0;
        for (Location<World> loc : r2.get4Points(r2.getCenterY())) {
            Map<Integer, Region> pregs = RedProtect.get().rm.getGroupRegion(loc.getExtent(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
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
        if (regs == 0 || regs != 4) {
            total += r2.getArea();
        }
        return total;
    }

    static void addProps(CommentedConfigurationNode fileDB, Region r) {
        String rname = r.getName().replaceAll("[.+=;\\-]", "");
        fileDB.getNode(rname, "name").setValue(rname);
        fileDB.getNode(rname, "lastvisit").setValue(r.getDate());
        fileDB.getNode(rname, "leaders").setValue(r.getLeaders());
        fileDB.getNode(rname, "admins").setValue(r.getAdmins());
        fileDB.getNode(rname, "members").setValue(r.getMembers());
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

    static Region loadRegion(CommentedConfigurationNode region, String rname, World world) throws ObjectMappingException {
        int maxX = region.getNode(rname, "maxX").getInt();
        int maxZ = region.getNode(rname, "maxZ").getInt();
        int minX = region.getNode(rname, "minX").getInt();
        int minZ = region.getNode(rname, "minZ").getInt();
        int maxY = region.getNode(rname, "maxY").getInt(world.getBlockMax().getY());
        int minY = region.getNode(rname, "minY").getInt(0);

        Set<String> leaders = new HashSet<>(region.getNode(rname, "leaders").getList(TypeToken.of(String.class)));
        Set<String> admins = new HashSet<>(region.getNode(rname, "admins").getList(TypeToken.of(String.class)));
        Set<String> members = new HashSet<>(region.getNode(rname, "members").getList(TypeToken.of(String.class)));

        //compatibility ------>
        if (region.getNode(rname, "creator").getValue() != null) {
            String creator = region.getNode(rname, "creator").getString();
            if (region.getNode(rname, "owners").getValue() != null) {
                leaders.addAll(region.getNode(rname, "owners").getList(TypeToken.of(String.class)));
            }
            leaders.add(creator);
            region.getNode(rname).removeChild("creator");
            region.getNode(rname).removeChild("owners");
        }
        //compatibility <------

        String welcome = region.getNode(rname, "welcome").getString();
        int prior = region.getNode(rname, "priority").getInt();
        String date = region.getNode(rname, "lastvisit").getString();
        long value = region.getNode(rname, "value").getLong();
        boolean candel = region.getNode(rname, "candelete").getBoolean(true);

        Location<World> tppoint = null;
        if (!region.getNode(rname, "tppoint").getString().equalsIgnoreCase("")) {
            String[] tpstring = region.getNode(rname, "tppoint").getString().split(",");
            tppoint = new Location<>(world, Double.parseDouble(tpstring[0]), Double.parseDouble(tpstring[1]), Double.parseDouble(tpstring[2]));
        }

        Region newr = new Region(rname, admins, members, leaders, new int[]{minX, minX, maxX, maxX}, new int[]{minZ, minZ, maxZ, maxZ}, minY, maxY, prior, world.getName(), date, RedProtect.get().cfgs.getDefFlagsValues(), welcome, value, tppoint, candel);

        for (String flag : RedProtect.get().cfgs.getDefFlags()) {
            if (region.getNode(rname, "flags", flag) != null) {
                newr.getFlags().put(flag, region.getNode(rname, "flags", flag).getBoolean());
            } else {
                newr.getFlags().put(flag, RedProtect.get().cfgs.getDefFlagsValues().get(flag));
            }
        }
        for (String flag : RedProtect.get().cfgs.AdminFlags) {
            if (region.getNode(rname, "flags", flag).getString() != null) {
                newr.getFlags().put(flag, region.getNode(rname, "flags", flag).getValue());
            }
        }
        return newr;
    }

    public static int SingleToFiles() {
        int saved = 0;
        for (World w : Sponge.getServer().getWorlds()) {
            Set<Region> regions = RedProtect.get().rm.getRegionsByWorld(w);
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

        if (!RedProtect.get().cfgs.root().flat_file.region_per_file) {
            RedProtect.get().cfgs.root().flat_file.region_per_file = true;
        }
        RedProtect.get().cfgs.save();
        return saved;
    }

    public static int FilesToSingle() {
        int saved = 0;
        for (World w : Sponge.getServer().getWorlds()) {
            File f = new File(RedProtect.get().configDir + File.separator + "data", "data_" + w.getName() + ".conf");
            Set<Region> regions = RedProtect.get().rm.getRegionsByWorld(w);
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
        if (RedProtect.get().cfgs.root().flat_file.region_per_file) {
            RedProtect.get().cfgs.root().flat_file.region_per_file = false;
        }
        RedProtect.get().cfgs.save();
        return saved;
    }

    private static void saveConf(CommentedConfigurationNode fileDB, ConfigurationLoader<CommentedConfigurationNode> regionManager) {
        try {
            regionManager.save(fileDB);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean canBuildNear(Player p, Location<World> loc) {
        if (RedProtect.get().cfgs.root().region_settings.deny_build_near == 0) {
            return true;
        }
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        int radius = RedProtect.get().cfgs.root().region_settings.deny_build_near;

        for (int ix = x - radius; ix <= x + radius; ++ix) {
            for (int iy = y - radius; iy <= y + radius; ++iy) {
                for (int iz = z - radius; iz <= z + radius; ++iz) {
                    Region reg = RedProtect.get().rm.getTopRegion(new Location<>(p.getWorld(), ix, iy, iz), RPUtil.class.getName());
                    if (reg != null && !reg.canBuild(p)) {
                        RPLang.sendMessage(p, RPLang.get("blocklistener.cantbuild.nearrp").replace("{distance}", "" + radius));
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
