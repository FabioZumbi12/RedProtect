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

package br.net.fabiozumbi12.RedProtect.Bukkit;

import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPConfig;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPLang;
import br.net.fabiozumbi12.RedProtect.Bukkit.events.ChangeRegionFlagEvent;
import br.net.fabiozumbi12.RedProtect.Bukkit.hooks.SCHook;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Crops;
import org.bukkit.util.Vector;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a 3D region created by players.
 */
public class Region implements Serializable {

    private static final long serialVersionUID = 2861198224185302015L;
    private final boolean waiting = false;
    private int[] x;
    private int[] z;
    private int minMbrX;
    private int maxMbrX;
    private int minMbrZ;
    private int maxMbrZ;
    private int minY;
    private int maxY;
    private int prior;
    private String name;
    private Set<String> leaders;
    private Set<String> admins;
    private Set<String> members;
    private String wMessage;
    private String world;
    private String date;
    private Map<String, Object> flags;
    private long value;
    private Location tppoint;
    private boolean tosave = true;
    private boolean canDelete;
    private int particleID = 0;
    private String dynmapSet = RPConfig.getString("hooks.dynmap.marks-groupname");

    /**
     * Represents the region created by player.
     *
     * @param name      Name of region.
     * @param admins    List of admins.
     * @param members   List of members.
     * @param leaders   List of leaders.
     * @param minLoc    Min coord.
     * @param maxLoc    Max coord.
     * @param flags     Flag names and values.
     * @param wMessage  Welcome message.
     * @param prior     Priority of region.
     * @param worldName Name of world for this region.
     * @param date      Date of latest visit of an admin or leader.
     * @param value     Last value of this region.
     */
    public Region(String name, Set<String> admins, Set<String> members, Set<String> leaders, Location minLoc, Location maxLoc, HashMap<String, Object> flags, String wMessage, int prior, String worldName, String date, long value, Location tppoint, boolean candel) {
        super();
        this.maxMbrX = maxLoc.getBlockX();
        this.minMbrX = minLoc.getBlockX();
        this.maxMbrZ = maxLoc.getBlockZ();
        this.minMbrZ = minLoc.getBlockZ();
        this.maxY = maxLoc.getBlockY();
        this.minY = minLoc.getBlockY();
        this.x = new int[]{minMbrX, minMbrX, maxMbrX, maxMbrX};
        this.z = new int[]{minMbrZ, minMbrZ, maxMbrZ, maxMbrZ};
        this.admins = admins;
        this.members = members;
        this.leaders = leaders;
        this.flags = flags;
        this.value = value;
        this.tppoint = tppoint;
        this.canDelete = candel;

        if (worldName != null) {
            this.world = worldName;
        } else {
            this.world = "";
        }

        if (wMessage != null) {
            this.wMessage = wMessage;
        } else {
            this.wMessage = "";
        }

        if (date != null) {
            this.date = date;
        } else {
            this.date = RPUtil.DateNow();
        }
        this.name = conformName(name);
        checkParticle();
    }

    /**
     * Represents the region created by player.
     *
     * @param name      Name of region.
     * @param admins    List of admins.
     * @param members   List of members.
     * @param leaders   List of leaders.
     * @param maxMbrX   Max coord X
     * @param minMbrX   Min coord X
     * @param maxMbrZ   Max coord Z
     * @param minMbrZ   Min coord Z
     * @param flags     Flag names and values.
     * @param wMessage  Welcome message.
     * @param prior     Priority of region.
     * @param worldName Name of world for this region.
     * @param date      Date of latest visit of an admin or leader.
     * @param value     Last value of this region.
     */
    public Region(String name, Set<String> admins, Set<String> members, Set<String> leaders, int maxMbrX, int minMbrX, int maxMbrZ, int minMbrZ, int minY, int maxY, HashMap<String, Object> flags, String wMessage, int prior, String worldName, String date, long value, Location tppoint, boolean candel) {
        super();
        this.x = new int[]{minMbrX, minMbrX, maxMbrX, maxMbrX};
        this.z = new int[]{minMbrZ, minMbrZ, maxMbrZ, maxMbrZ};
        this.maxMbrX = maxMbrX;
        this.minMbrX = minMbrX;
        this.maxMbrZ = maxMbrZ;
        this.minMbrZ = minMbrZ;
        this.maxY = maxY;
        this.minY = minY;
        this.admins = admins;
        this.members = members;
        this.leaders = leaders;
        this.flags = flags;
        this.value = value;
        this.tppoint = tppoint;
        this.canDelete = candel;

        if (worldName != null) {
            this.world = worldName;
        } else {
            this.world = "";
        }

        if (wMessage != null) {
            this.wMessage = wMessage;
        } else {
            this.wMessage = "";
        }

        if (date != null) {
            this.date = date;
        } else {
            this.date = RPUtil.DateNow();
        }
        this.name = conformName(name);
        checkParticle();
    }

    /**
     * Represents the region created by player.
     *
     * @param name      Region name.
     * @param admins    Admins names/uuids.
     * @param members   Members names/uuids.
     * @param leaders   Leaders name/uuid.
     * @param x         Locations of x coords.
     * @param z         Locations of z coords.
     * @param miny      Min coord y of this region.
     * @param maxy      Max coord y of this region.
     * @param prior     Location of x coords.
     * @param worldName Name of world region.
     * @param date      Date of latest visit of an admins or leader.
     * @param welcome   Set a welcome message.
     * @param value     A value in server economy.
     */
    public Region(String name, Set<String> admins, Set<String> members, Set<String> leaders, int[] x, int[] z, int miny, int maxy, int prior, String worldName, String date, Map<String, Object> flags, String welcome, long value, Location tppoint, boolean candel) {
        super();
        this.prior = prior;
        this.world = worldName;
        this.date = date;
        this.flags = flags;
        this.wMessage = welcome;
        int size = x.length;
        this.value = value;
        this.tppoint = tppoint;
        this.canDelete = candel;

        if (size != z.length) {
            throw new Error(RPLang.get("region.xy"));
        }
        this.x = x;
        this.z = z;
        if (size < 4) {
            throw new Error(RPLang.get("region.polygon"));
        }
        if (size == 4) {
            this.x = null;
            this.z = null;
        }
        this.admins = admins;
        this.members = members;
        this.leaders = leaders;
        this.maxMbrX = x[0];
        this.minMbrX = x[0];
        this.maxMbrZ = z[0];
        this.minMbrZ = z[0];
        this.maxY = maxy;
        this.minY = miny;
        for (int i = 0; i < x.length; ++i) {
            if (x[i] > this.maxMbrX) {
                this.maxMbrX = x[i];
            }
            if (x[i] < this.minMbrX) {
                this.minMbrX = x[i];
            }
            if (z[i] > this.maxMbrZ) {
                this.maxMbrZ = z[i];
            }
            if (z[i] < this.minMbrZ) {
                this.minMbrZ = z[i];
            }
        }
        this.name = conformName(name);
        checkParticle();
    }

    /**
     * Represents the region created by player.
     *
     * @param name  Region name.
     * @param min   Min Location.
     * @param max   Max Location.
     * @param world World name.
     */
    public Region(String name, Location min, Location max, String world) {
        super();
        this.x = new int[]{min.getBlockX(), min.getBlockX(), max.getBlockX(), max.getBlockX()};
        this.z = new int[]{min.getBlockZ(), min.getBlockZ(), max.getBlockZ(), max.getBlockZ()};
        this.maxMbrX = max.getBlockX();
        this.minMbrX = min.getBlockX();
        this.maxMbrZ = max.getBlockZ();
        this.minMbrZ = min.getBlockZ();
        this.maxY = max.getBlockY();
        this.minY = min.getBlockY();
        this.admins = new HashSet<>();
        this.members = new HashSet<>();
        this.leaders = Collections.singleton(RPConfig.getString("region-settings.default-leader"));
        this.flags = RPConfig.getDefFlagsValues();
        this.canDelete = true;
        this.world = world;
        this.wMessage = "";
        this.date = RPUtil.DateNow();
        this.name = name == null ? "" : conformName(name);
        checkParticle();
    }

    public Map<String, Object> getFlags() {
        return flags;
    }

    public void setFlags(Map<String, Object> flags) {
        this.flags = flags;
    }

    public boolean canDelete() {
        return this.canDelete;
    }

    /**
     * Get unique ID of region based on name of "region + @ + world".
     *
     * @return {@code id string}
     */
    @Override
    public String toString() {
        return this.name + "@" + this.world;
    }

    /**
     * Get unique ID of region based on name of "region + @ + world".
     *
     * @return {@code id string}
     */
    public String getID() {
        return this.name + "@" + this.world;
    }

    public boolean toSave() {
        return this.tosave;
    }

    public void setToSave(boolean save) {
        this.tosave = save;
    }

    private void checkParticle() {
        Bukkit.getScheduler().runTaskLater(RedProtect.get(), () -> {
            if (this.flags.containsKey("particles")) {
                if (particleID <= 0) {

                    particleID = Bukkit.getScheduler().scheduleSyncRepeatingTask(RedProtect.get(), () -> {
                        if (this.flags.containsKey("particles")) {
                            String[] part = flags.get("particles").toString().split(" ");
                            for (int i = 0; i < Integer.valueOf(part[1]); i++) {
                                Vector max = Vector.getMaximum(getMinLocation().toVector(), getMaxLocation().toVector());
                                Vector min = Vector.getMinimum(getMinLocation().toVector(), getMaxLocation().toVector());

                                int dx = max.getBlockX() - min.getBlockX();
                                int dy = max.getBlockY() - min.getBlockY();
                                int dz = max.getBlockZ() - min.getBlockZ();
                                Random random = new Random();
                                int x = random.nextInt(Math.abs(dx) + 1) + min.getBlockX();
                                int y = random.nextInt(Math.abs(dy) + 1) + min.getBlockY();
                                int z = random.nextInt(Math.abs(dz) + 1) + min.getBlockZ();


                                Particle p = Particle.valueOf(part[0].toUpperCase());
                                World w = Bukkit.getServer().getWorld(world);

                                Location loc = new Location(w, x + new Random().nextDouble(), y + new Random().nextDouble(), z + new Random().nextDouble());
                                if (w.getNearbyEntities(loc, 30, 30, 30).stream().anyMatch(ent -> ent instanceof Player)) {
                                    if (loc.getBlock().isEmpty()) {
                                        if (part.length == 2) {
                                            w.spawnParticle(p, loc, 1);
                                        }
                                        if (part.length == 5) {
                                            w.spawnParticle(p, loc, 1, Double.parseDouble(part[2]), Double.parseDouble(part[3]), Double.parseDouble(part[4]));
                                        }
                                        if (part.length == 6) {
                                            w.spawnParticle(p, loc, 1, Double.parseDouble(part[2]), Double.parseDouble(part[3]), Double.parseDouble(part[4]), Double.parseDouble(part[5]), null);
                                        }
                                    }
                                }
                            }
                        }
                    }, 1, 1);
                }
            } else if (particleID > 0) {
                notifyRemove();
            }
        }, 20);
    }

    public void notifyRemove() {
        if (particleID > 0) {
            Bukkit.getScheduler().cancelTask(particleID);
            particleID = 0;
        }
    }

    public void updateSigns() {
        for (String s : this.flags.keySet()) {
            updateSigns(s);
        }
    }

    public void updateSigns(String fname) {
        if (!RPConfig.getBool("region-settings.enable-flag-sign")) {
            return;
        }
        List<Location> locs = RPConfig.getSigns(this.getID());
        if (locs.size() > 0) {
            for (Location loc : locs) {
                if (loc.getBlock().getState() instanceof Sign) {
                    Sign s = (Sign) loc.getBlock().getState();
                    String[] lines = s.getLines();
                    if (lines[0].equalsIgnoreCase("[flag]")) {
                        if (lines[1].equalsIgnoreCase(fname) && this.name.equalsIgnoreCase(ChatColor.stripColor(lines[2]))) {
                            s.setLine(3, RPLang.get("region.value") + " " + RPLang.translBool(getFlagString(fname)));
                            s.update();
                            RPConfig.putSign(this.getID(), loc);
                        }
                    } else {
                        RPConfig.removeSign(this.getID(), loc);
                    }
                } else {
                    RPConfig.removeSign(this.getID(), loc);
                }
            }
        }
    }

    public boolean setFlag(CommandSender cause, String fname, Object value) {
        ChangeRegionFlagEvent event = new ChangeRegionFlagEvent(cause, this, fname, value);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }

        setToSave(true);
        this.flags.put(event.getFlag(), event.getFlagValue());
        RedProtect.get().rm.updateLiveFlags(this, event.getFlag(), event.getFlagValue().toString());
        updateSigns(event.getFlag());
        checkParticle();
        return true;
    }

    public void removeFlag(String Name) {
        setToSave(true);
        if (this.flags.containsKey(Name)) {
            this.flags.remove(Name);
            RedProtect.get().rm.removeLiveFlags(this, Name);
        }
        checkParticle();
    }

    public String getFlagStrings() {
        StringBuilder flags = new StringBuilder();
        for (String flag : this.flags.keySet()) {
            flags.append(",").append(flag).append(":").append(this.flags.get(flag).toString());
        }
        return flags.toString().substring(1);
    }

    public Location getTPPoint() {
        return this.tppoint;
    }

    public void setTPPoint(Location loc) {
        setToSave(true);
        this.tppoint = loc;
        if (loc != null) {
            double x = loc.getX();
            double y = loc.getY();
            double z = loc.getZ();
            float yaw = loc.getYaw();
            float pitch = loc.getPitch();
            RedProtect.get().rm.updateLiveRegion(this, "tppoint", x + "," + y + "," + z + "," + yaw + "," + pitch);
        } else {
            RedProtect.get().rm.updateLiveRegion(this, "tppoint", "");
        }

    }

    public String getTPPointString() {
        if (tppoint == null) {
            return "";
        }
        return this.tppoint.getX() + "," + this.tppoint.getY() + "," + this.tppoint.getZ() + "," + this.tppoint.getYaw() + "," + this.tppoint.getPitch();
    }

    public String getDate() {
        return this.date;
    }

    public void setDate(String value) {
        setToSave(true);
        this.date = value;
        RedProtect.get().rm.updateLiveRegion(this, "date", value);
    }

    public int getMaxY() {
        return this.maxY;
    }

    public void setMaxY(int y) {
        setToSave(true);
        this.maxY = y;
        RedProtect.get().rm.updateLiveRegion(this, "maxy", String.valueOf(y));
    }

    public int getMinY() {
        return this.minY;
    }

    public void setMinY(int y) {
        setToSave(true);
        this.minY = y;
        RedProtect.get().rm.updateLiveRegion(this, "miny", String.valueOf(y));
    }

    public Location getMaxLocation() {
        return new Location(Bukkit.getWorld(this.world), this.maxMbrX, this.maxY, this.maxMbrZ);
    }

    public Location getMinLocation() {
        return new Location(Bukkit.getWorld(this.world), this.minMbrX, this.minY, this.minMbrZ);
    }

    public String getWorld() {
        return this.world;
    }

    public void setWorld(String w) {
        setToSave(true);
        this.world = w;
        RedProtect.get().rm.updateLiveRegion(this, "world", w);
    }

    public int getPrior() {
        return this.prior;
    }

    public void setPrior(int prior) {
        setToSave(true);
        this.prior = prior;
        RedProtect.get().rm.updateLiveRegion(this, "prior", "" + prior);
    }

    public String getWelcome() {
        if (wMessage == null) {
            return "";
        }
        return this.wMessage;
    }

    public void setWelcome(String s) {
        setToSave(true);
        this.wMessage = s;
        RedProtect.get().rm.updateLiveRegion(this, "wel", s);
    }

    public int[] getX() {
        return this.x;
    }

    public void setX(int[] x) {
        setToSave(true);
        this.x = x;
    }

    public int[] getZ() {
        return this.z;
    }

    public void setZ(int[] z) {
        setToSave(true);
        this.z = z;
    }

    public String getName() {
        return this.name;
    }

    public String getDynmapSet() {
        return this.dynmapSet;
    }

    /**
     * Use this method to get raw admins. This will return UUID if server running in Online mode. Will return player name in lowercase if Offline mode.
     * <p>
     * To check if a player can build on this region use {@code canBuild(p)} instead this method.
     *
     * @return {@code Set<String>}
     */
    @Deprecated()
    public Set<String> getAdmins() {
        return this.admins;
    }

    public void setAdmins(Set<String> admins) {
        setToSave(true);
        this.admins = admins;
        RedProtect.get().rm.updateLiveRegion(this, "admins", admins.toString().replace("[", "").replace("]", ""));
    }

    /**
     * Use this method to get raw members. This will return UUID if server running in Online mode. Will return player name in lowercase if Offline mode.
     * <p>
     * To check if a player can build on this region use {@code canBuild(Player p)} instead this method.
     *
     * @return {@code Set<String>}
     */
    @Deprecated
    public Set<String> getMembers() {
        return this.members;
    }

    public void setMembers(Set<String> members) {
        setToSave(true);
        this.members = members;
        RedProtect.get().rm.updateLiveRegion(this, "members", members.toString().replace("[", "").replace("]", ""));
    }

    /**
     * Use this method to get raw leaders. This will return UUID if server running in Online mode. Will return player name in lowercase if Offline mode.
     * <p>
     * To check if a player can build on this region use {@code canBuild(Player p)} instead this method.
     *
     * @return {@code Set<String>}
     */
    @Deprecated
    public Set<String> getLeaders() {
        return this.leaders;
    }

    public void setLeaders(Set<String> leaders) {
        setToSave(true);
        this.leaders = leaders;
        RedProtect.get().rm.updateLiveRegion(this, "leaders", members.toString().replace("[", "").replace("]", ""));
    }

    public int getCenterX() {
        return (this.minMbrX + this.maxMbrX) / 2;
    }

    public int getCenterZ() {
        return (this.minMbrZ + this.maxMbrZ) / 2;
    }

    public int getCenterY() {
        return (this.minY + this.maxY) / 2;
    }

    public int getMaxMbrX() {
        return this.maxMbrX;
    }

    public int getMinMbrX() {
        return this.minMbrX;
    }

    public int getMaxMbrZ() {
        return this.maxMbrZ;
    }

    public int getMinMbrZ() {
        return this.minMbrZ;
    }

    public String info() {
        final StringBuilder leaderStringBuilder = new StringBuilder();
        final StringBuilder adminStringBuilder = new StringBuilder();
        final StringBuilder memberStringBuilder = new StringBuilder();
        String leaderString = "None";
        String adminString = "None";
        String memberString = "None";

        String wMsgTemp;
        String IsTops = RPLang.translBool(isOnTop());
        String today;
        String wName = this.world;
        String colorChar = "";
        String dynmapInfo = "";

        if (RPConfig.getString("region-settings.world-colors." + this.world) != null) {
            colorChar = ChatColor.translateAlternateColorCodes('&', RPConfig.getString("region-settings.world-colors." + this.world));
        }

        leaders.removeIf(Objects::isNull);
        leaders.forEach(leader -> leaderStringBuilder.append(", ").append(RPUtil.UUIDtoPlayer(leader)));

        admins.removeIf(Objects::isNull);
        admins.forEach(admin -> adminStringBuilder.append(", ").append(RPUtil.UUIDtoPlayer(admin)));

        members.removeIf(Objects::isNull);
        members.forEach(member -> memberStringBuilder.append(", ").append(RPUtil.UUIDtoPlayer(member)));

        if (this.leaders.size() > 0) {
            leaderString = leaderStringBuilder.delete(0, 2).toString();
        }
        if (this.admins.size() > 0) {
            adminString = adminStringBuilder.delete(0, 2).toString();
        }
        if (this.members.size() > 0) {
            memberString = memberStringBuilder.delete(0, 2).toString();
        }
        if (this.wMessage == null || this.wMessage.equals("")) {
            wMsgTemp = RPLang.get("region.welcome.notset");
        } else {
            wMsgTemp = wMessage;
        }

        if (this.date.equals(RPUtil.DateNow())) {
            today = RPLang.get("region.today");
        } else {
            today = this.date;
        }
        for (String pname : this.leaders) {
            Player play = RedProtect.get().serv.getPlayer(pname);
            if (RedProtect.get().OnlineMode && pname != null && !pname.equalsIgnoreCase(RPConfig.getString("region-settings.default-leader"))) {
                try {
                    play = RedProtect.get().serv.getPlayer(UUID.fromString(RPUtil.PlayerToUUID(pname)));
                } catch (Exception ignored) {
                }
            }
            if (pname != null && play != null && play.isOnline()) {
                today = ChatColor.GREEN + "Online!";
                break;
            }
        }
        for (String pname : this.admins) {
            Player play = RedProtect.get().serv.getPlayer(pname);
            try {
                play = RedProtect.get().serv.getPlayer(UUID.fromString(RPUtil.PlayerToUUID(pname)));
            } catch (Exception ignored) {
            }
            if (pname != null && play != null && play.isOnline()) {
                today = ChatColor.GREEN + "Online!";
                break;
            }
        }

        if (RedProtect.get().Dyn && RPConfig.getBool("hooks.dynmap.enable")) {
            dynmapInfo = RPLang.get("region.dynmap") + " " + (this.getFlagBool("dynmap") ? RPLang.get("region.dynmap-showing") : RPLang.get("region.dynmap-hiding")) + ", " + RPLang.get("region.dynmap-set") + " " + this.getDynmapSet() + "\n";
        }

        return RPLang.get("region.name") + " " + colorChar + this.name + RPLang.get("general.color") + " | " + RPLang.get("region.priority") + " " + this.prior + "\n" +
                RPLang.get("region.priority.top") + " " + IsTops + RPLang.get("general.color") + "\n" +
                RPLang.get("region.world") + " " + colorChar + wName + RPLang.get("general.color") + " | " + RPLang.get("region.center") + " " + this.getCenterX() + ", " + this.getCenterZ() + "\n" +
                RPLang.get("region.ysize") + " " + this.minY + " - " + this.maxY + RPLang.get("general.color") + " | " + RPLang.get("region.area") + " " + this.getArea() + "\n" +
                RPLang.get("region.leaders") + " " + leaderString + "\n" +
                RPLang.get("region.admins") + " " + adminString + RPLang.get("general.color") + " | " + RPLang.get("region.members") + " " + memberString + "\n" +
                RPLang.get("region.date") + " " + today + "\n" +
                dynmapInfo +
                RPLang.get("region.welcome.msg") + " " + (wMsgTemp.equals("hide ") ? RPLang.get("region.hiding") : ChatColor.translateAlternateColorCodes('&', wMsgTemp));
    }

    private String conformName(String name) {
        name = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', name));
        return name;
    }

    public void clearLeaders() {
        setToSave(true);
        this.leaders.clear();
        RedProtect.get().rm.updateLiveRegion(this, "leaders", "");
    }

    public void clearAdmins() {
        setToSave(true);
        this.admins.clear();
        RedProtect.get().rm.updateLiveRegion(this, "admins", "");
    }

    public void clearMembers() {
        setToSave(true);
        this.members.clear();
        RedProtect.get().rm.updateLiveRegion(this, "members", "");
    }

    /*
    public void delete() {
        RedProtect.get().rm.remove(this);
    }
    */
    public int getArea() {
        return Math.abs((this.maxMbrX - this.minMbrX) + 1) * Math.abs((this.maxMbrZ - this.minMbrZ) + 1);
    }

    public boolean inBoundingRect(Region other) {
        return other.maxMbrX >= this.minMbrX && other.minMbrZ >= this.minMbrZ && other.minMbrX <= this.maxMbrX && other.minMbrZ <= this.maxMbrZ;
    }

    public boolean isLeader(Player player) {
        return this.leaders.contains(RPUtil.PlayerToUUID(player.getName()));
    }

    public boolean isLeader(String player) {
        return this.leaders.contains(RPUtil.PlayerToUUID(player));
    }

    public boolean isAdmin(Player player) {
        return this.admins.contains(RPUtil.PlayerToUUID(player.getName()));
    }

    public boolean isAdmin(String player) {
        return this.admins.contains(RPUtil.PlayerToUUID(player));
    }

    public boolean isMember(Player player) {
        return (RedProtect.get().SC && SCHook.getPlayerClan(this, player)) || this.members.contains(RPUtil.PlayerToUUID(player.getName()));
    }

    public boolean isMember(String player) {
        return this.members.contains(RPUtil.PlayerToUUID(player));
    }

    /**
     * Add an leader to the Region. The string need to be UUID if Online Mode, or Player Name if Offline Mode.
     *
     * @param uuid - UUID or Player Name.
     */
    public void addLeader(String uuid) {
        setToSave(true);
        String pinfo = uuid;
        if (!RedProtect.get().OnlineMode) {
            pinfo = uuid.toLowerCase();
        }
        this.members.remove(pinfo);
        this.admins.remove(pinfo);
        this.leaders.add(pinfo);

        RedProtect.get().rm.updateLiveRegion(this, "leaders", this.leaders.toString().replace("[", "").replace("]", ""));
        RedProtect.get().rm.updateLiveRegion(this, "admins", this.admins.toString().replace("[", "").replace("]", ""));
        RedProtect.get().rm.updateLiveRegion(this, "members", this.members.toString().replace("[", "").replace("]", ""));
    }

    /**
     * Add a member to the Region. The string need to be UUID if Online Mode, or Player Name if Offline Mode.
     *
     * @param uuid - UUID or Player Name.
     */
    public void addMember(String uuid) {
        setToSave(true);
        String pinfo = uuid;
        if (!RedProtect.get().OnlineMode) {
            pinfo = uuid.toLowerCase();
        }
        this.admins.remove(pinfo);
        this.leaders.remove(pinfo);
        this.members.add(pinfo);
        RedProtect.get().rm.updateLiveRegion(this, "leaders", this.leaders.toString().replace("[", "").replace("]", ""));
        RedProtect.get().rm.updateLiveRegion(this, "admins", this.admins.toString().replace("[", "").replace("]", ""));
        RedProtect.get().rm.updateLiveRegion(this, "members", this.members.toString().replace("[", "").replace("]", ""));
    }

    /**
     * Add an admin to the Region. The string need to be UUID if Online Mode, or Player Name if Offline Mode.
     *
     * @param uuid - UUID or Player Name.
     */
    public void addAdmin(String uuid) {
        setToSave(true);
        String pinfo = uuid;
        if (!RedProtect.get().OnlineMode) {
            pinfo = uuid.toLowerCase();
        }
        this.members.remove(pinfo);
        this.leaders.remove(pinfo);
        this.admins.add(pinfo);
        RedProtect.get().rm.updateLiveRegion(this, "leaders", this.leaders.toString().replace("[", "").replace("]", ""));
        RedProtect.get().rm.updateLiveRegion(this, "admins", this.admins.toString().replace("[", "").replace("]", ""));
        RedProtect.get().rm.updateLiveRegion(this, "members", this.members.toString().replace("[", "").replace("]", ""));
    }

    /**
     * Remove an member to the Region. The string need to be UUID if Online Mode, or Player Name if Offline Mode.
     *
     * @param uuid - UUID or Player Name.
     */
    public void removeMember(String uuid) {
        setToSave(true);
        String pinfo = uuid;
        if (!RedProtect.get().OnlineMode) {
            pinfo = uuid.toLowerCase();
        }
        this.members.remove(pinfo);
        this.admins.remove(pinfo);
        this.leaders.remove(pinfo);
        RedProtect.get().rm.updateLiveRegion(this, "admins", this.admins.toString().replace("[", "").replace("]", ""));
        RedProtect.get().rm.updateLiveRegion(this, "members", this.members.toString().replace("[", "").replace("]", ""));
        RedProtect.get().rm.updateLiveRegion(this, "leaders", this.leaders.toString().replace("[", "").replace("]", ""));
    }

    /**
     * Remove an admin to the Region. The string need to be UUID if Online Mode, or Player Name if Offline Mode.
     *
     * @param uuid - UUID or Player Name.
     */
    public void removeAdmin(String uuid) {
        setToSave(true);
        String pinfo = uuid;
        if (!RedProtect.get().OnlineMode) {
            pinfo = uuid.toLowerCase();
        }
        this.leaders.remove(pinfo);
        this.admins.remove(pinfo);
        this.members.add(pinfo);
        RedProtect.get().rm.updateLiveRegion(this, "admins", this.admins.toString().replace("[", "").replace("]", ""));
        RedProtect.get().rm.updateLiveRegion(this, "members", this.members.toString().replace("[", "").replace("]", ""));
        RedProtect.get().rm.updateLiveRegion(this, "leaders", this.leaders.toString().replace("[", "").replace("]", ""));
    }

    /**
     * Remove an leader to the Region. The string need to be UUID if Online Mode, or Player Name if Offline Mode.
     *
     * @param uuid - UUID or Player Name.
     */
    public void removeLeader(String uuid) {
        setToSave(true);
        String pinfo = uuid;
        if (!RedProtect.get().OnlineMode) {
            pinfo = uuid.toLowerCase();
        }
        this.members.remove(pinfo);
        this.leaders.remove(pinfo);
        this.admins.add(pinfo);
        RedProtect.get().rm.updateLiveRegion(this, "admins", this.admins.toString().replace("[", "").replace("]", ""));
        RedProtect.get().rm.updateLiveRegion(this, "members", this.members.toString().replace("[", "").replace("]", ""));
        RedProtect.get().rm.updateLiveRegion(this, "leaders", this.leaders.toString().replace("[", "").replace("]", ""));
    }

    public boolean getFlagBool(String key) {
        if (!flagExists(key) || !RPConfig.isFlagEnabled(key)) {
            if (RPConfig.getDefFlagsValues().get(key) != null) {
                return (Boolean) RPConfig.getDefFlagsValues().get(key);
            } else {
                return RPConfig.getBool("flags." + key);
            }
        }
        return this.flags.get(key) instanceof Boolean && (Boolean) this.flags.get(key);
    }

    public String getFlagString(String key) {
        if (!flagExists(key) || !RPConfig.isFlagEnabled(key)) {
            if (RPConfig.getDefFlagsValues().get(key) != null) {
                return (String) RPConfig.getDefFlagsValues().get(key);
            } else {
                return RPConfig.getString("flags." + key);
            }
        }
        return this.flags.get(key).toString();
    }

    public int adminSize() {
        return this.admins.size();
    }

    public int leaderSize() {
        return this.leaders.size();
    }

    public String getFlagInfo() {
        StringBuilder flaginfo = new StringBuilder();
        for (String flag : this.flags.keySet()) {
            if (RPConfig.getDefFlags().contains(flag)) {
                String flagValue = getFlagString(flag);
                if (flagValue.equalsIgnoreCase("true") || flagValue.equalsIgnoreCase("false")) {
                    flaginfo.append(", ").append(ChatColor.AQUA).append(flag).append(": ").append(RPLang.translBool(flagValue));
                } else {
                    flaginfo.append(", ").append(ChatColor.AQUA).append(flag).append(": ").append(ChatColor.GRAY).append(flagValue);
                }
            }

            if (flaginfo.toString().contains(flag)) {
                continue;
            }

            if (RPConfig.AdminFlags.contains(flag)) {
                String flagValue = getFlagString(flag);
                if (flagValue.equalsIgnoreCase("true") || flagValue.equalsIgnoreCase("false")) {
                    flaginfo.append(", ").append(ChatColor.AQUA).append(flag).append(": ").append(RPLang.translBool(flagValue));
                } else {
                    flaginfo.append(", ").append(ChatColor.AQUA).append(flag).append(": ").append(ChatColor.GRAY).append(flagValue);
                }
            }
        }
        if (this.flags.keySet().size() > 0) {
            flaginfo = new StringBuilder(flaginfo.substring(2));
        } else {
            flaginfo = new StringBuilder("Default");
        }
        return flaginfo.toString();
    }

    public boolean isOnTop() {
        Region newr = RedProtect.get().rm.getTopRegion(RedProtect.get().serv.getWorld(this.getWorld()), this.getCenterX(), this.getCenterY(), this.getCenterZ());
        return newr == null || newr.equals(this);
    }

    public boolean flagExists(String key) {
        return flags.containsKey(key);
    }

    //---------------------- Admin Flags --------------------------//

    public boolean canSpawnWhiter() {
        return !flagExists("spawn-wither") || getFlagBool("spawn-wither");
    }

    public int maxPlayers() {
        if (!flagExists("max-players")) {
            return -1;
        }
        return Integer.valueOf(getFlagString("max-players"));
    }

    public boolean canDeath() {
        return !flagExists("can-death") || getFlagBool("can-death");
    }

    public boolean cmdOnHealth(Player p) {
        if (!flagExists("cmd-onhealth")) {
            return false;
        }

        boolean run = false;
        //rp flag cmd-onhealth health:<number> cmd:<cmd>, ...
        for (String group : getFlagString("cmd-onhealth").split(",")) {
            int health = Integer.parseInt(group.split(" ")[0].substring(7));
            String cmd = group.replace(group.split(" ")[0] + " ", "").substring(4);
            if (cmd.startsWith("/")) {
                cmd = cmd.substring(1);
            }
            if (p.getHealth() <= health && !waiting) {
                p.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{player}", p.getName()));
    			/*waiting = true;
    			Bukkit.getScheduler().runTaskLater(RedProtect.get().plugin, new Runnable(){
					@Override
					public void run() {
						waiting = false;
					}    				
    			}, 20);*/
                run = true;
            }
        }
        return run;
    }

    public boolean allowDynmap() {
        return !flagExists("dynmap") || getFlagBool("dynmap");
    }

    public boolean keepInventory() {
        return flagExists("keep-inventory") && getFlagBool("keep-inventory");
    }

    public boolean keepLevels() {
        return flagExists("keep-levels") && getFlagBool("keep-levels");
    }

    public String getClan() {
        if (!flagExists("clan")) {
            return "";
        }
        return getFlagString("clan");
    }

    public int getViewDistance() {
        if (!flagExists("view-distance")) {
            return 0;
        }
        return new Integer(getFlagString("view-distance"));
    }

    public boolean canPlayerDamage() {
        return !flagExists("player-damage") || getFlagBool("player-damage");
    }

    public boolean forcePVP() {
        return flagExists("forcepvp") && getFlagBool("forcepvp");
    }

    public boolean canHunger() {
        return !flagExists("can-hunger") || getFlagBool("can-hunger");
    }

    public boolean canSign(Player p) {
        if (!flagExists("sign")) {
            return checkAllowedPlayer(p);
        }
        return getFlagBool("sign") || checkAllowedPlayer(p);
    }

    public boolean canExit(Player p) {
        if (!canExitWithItens(p)) {
            return false;
        }
        return !flagExists("exit") || getFlagBool("exit") || RedProtect.get().ph.hasPerm(p, "redprotect.region-exit." + this.name) || checkAllowedPlayer(p);
    }

    public boolean canEnter(Player p) {
        if (RedProtect.get().denyEnter.containsKey(p.getName()) && RedProtect.get().denyEnter.get(p.getName()).contains(this.getID())) {
            return checkAllowedPlayer(p);
        }

        return !flagExists("enter") || getFlagBool("enter") || RedProtect.get().ph.hasPerm(p, "redprotect.region-enter." + this.name) || checkAllowedPlayer(p);
    }

    public boolean canExitWithItens(Player p) {
        if (!flagExists("deny-exit-items")) {
            return true;
        }

        if (checkAllowedPlayer(p)) {
            return true;
        }

        List<String> mats = Arrays.asList(getFlagString("deny-exit-items").replace(" ", "").split(","));
        for (ItemStack slot : p.getInventory()) {
            if (slot == null || slot.getType().equals(Material.AIR)) {
                continue;
            }

            if (mats.stream().anyMatch(k -> k.equalsIgnoreCase(slot.getType().name()))) {
                return false;
            }
        }
        return true;
    }

    public boolean canEnterWithItens(Player p) {
        if (!flagExists("allow-enter-items")) {
            return true;
        }

        if (checkAllowedPlayer(p)) {
            return true;
        }

        List<String> mats = Arrays.asList(getFlagString("deny-exit-items").replace(" ", "").split(","));
        for (ItemStack slot : p.getInventory()) {
            if (slot == null || slot.getType().equals(Material.AIR)) {
                continue;
            }
            if (mats.stream().anyMatch(k -> k.equalsIgnoreCase(slot.getType().name()))) {
                return true;
            }
        }
        return false;
    }

    public boolean denyEnterWithItens(Player p) {
        if (!flagExists("deny-enter-items")) {
            return true;
        }
        if (checkAllowedPlayer(p)) {
            return true;
        }

        List<String> items = Arrays.asList(getFlagString("deny-enter-items").replace(" ", "").split(","));
        for (ItemStack slot : p.getInventory().getContents()) {
            if (slot == null || slot.getType().equals(Material.AIR)) {
                continue;
            }

            if (items.stream().anyMatch(k -> k.equalsIgnoreCase(slot.getType().name()))) {
                return false;
            }
        }
        return true;
    }

    public boolean canTeleport(Player p) {
        if (!flagExists("teleport")) {
            return checkAllowedPlayer(p);
        }
        return getFlagBool("teleport") || checkAllowedPlayer(p);
    }


    public boolean canMining(Block b) {
        return flagExists("minefarm") && (b.getType().toString().contains("_ORE") || b.getType().equals(Material.STONE) || b.getType().equals(Material.GRASS) || b.getType().equals(Material.DIRT)) && getFlagBool("minefarm");
    }

    public boolean canPlace(Material b) {
        if (!flagExists("allow-place")) {
            return false;
        }
        String[] blocks = getFlagString("allow-place").replace(" ", "").split(",");
        for (String block : blocks) {
            if (block.toUpperCase().equals(b.name())) {
                return true;
            }
        }
        return false;
    }

    public boolean canBreak(EntityType e) {
        if (!flagExists("allow-break")) {
            return false;
        }
        String[] blocks = getFlagString("allow-break").replace(" ", "").split(",");
        for (String block : blocks) {
            if (block.toUpperCase().equals(e.name())) {
                return true;
            }
        }
        return false;
    }

    public boolean canBreak(Material b) {
        if (!flagExists("allow-break")) {
            return false;
        }
        String[] blocks = getFlagString("allow-break").replace(" ", "").split(",");
        for (String block : blocks) {
            if (block.toUpperCase().equals(b.name())) {
                return true;
            }
        }
        return false;
    }

    public boolean canTree(Block b) {
        return flagExists("treefarm") && (b.getType().toString().contains("LOG") || b.getType().toString().contains("LEAVES")) && getFlagBool("treefarm");
    }

    public boolean canCrops(Block b) {
        return flagExists("cropsfarm") && (b instanceof Crops || b.getType().equals(Material.PUMPKIN_STEM) || b.getType().equals(Material.MELON_STEM) || b.getType().toString().contains("CROPS") || b.getType().toString().contains("SOIL") || b.getType().toString().contains("CHORUS_") || b.getType().toString().contains("BEETROOT_") || b.getType().toString().contains("SUGAR_CANE")) && getFlagBool("cropsfarm");
    }

    public boolean canSkill(Player p) {
        return !flagExists("up-skills") || getFlagBool("up-skills") || checkAllowedPlayer(p);
    }

    public boolean canBack(Player p) {
        return !flagExists("can-back") || getFlagBool("can-back") || checkAllowedPlayer(p);
    }

    public boolean isPvPArena() {
        return flagExists("pvparena") && getFlagBool("pvparena");
    }

    public boolean allowMod(Player p) {
        if (!flagExists("allow-mod")) {
            return checkAllowedPlayer(p);
        }
        return getFlagBool("allow-mod") || checkAllowedPlayer(p);
    }

    public boolean canEnterPortal(Player p) {
        return !flagExists("portal-enter") || getFlagBool("portal-enter") || checkAllowedPlayer(p);
    }

    public boolean canExitPortal(Player p) {
        return !flagExists("portal-exit") || getFlagBool("portal-exit") || checkAllowedPlayer(p);
    }

    public boolean canPet(Player p) {
        return !flagExists("can-pet") || getFlagBool("can-pet") || checkAllowedPlayer(p);
    }

    public boolean canProtectiles(Player p) {
        return !flagExists("can-projectiles") || getFlagBool("can-projectiles") || checkAllowedPlayer(p);
    }

    public boolean canDrop(Player p) {
        return !flagExists("can-drop") || getFlagBool("can-drop") || checkAllowedPlayer(p);
    }

    public boolean canPickup(Player p) {
        return !flagExists("can-pickup") || getFlagBool("can-pickup") || checkAllowedPlayer(p);
    }

    public boolean canCreatePortal() {
        return !flagExists("allow-create-portal") || getFlagBool("allow-create-portal");
    }

    public boolean AllowCommands(Player p, String fullcmd) {
        if (!flagExists("allow-cmds")) {
            return true;
        }

        String Command = fullcmd.replace("/", "").split(" ")[0];
        List<String> argsRaw = Arrays.asList(fullcmd.replace("/" + Command + " ", "").split(" "));

        //As Whitelist
        String[] flagCmds = getFlagString("allow-cmds").split(",");
        for (String cmd : flagCmds) {
            if (cmd.startsWith(" ")) {
                cmd = cmd.substring(1);
            }
            String[] cmdarg = cmd.split(" ");
            if (cmdarg.length == 2) {
                if (cmdarg[0].startsWith("cmd:") && cmdarg[0].split(":")[1].equalsIgnoreCase(Command) &&
                        cmdarg[1].startsWith("arg:") && argsRaw.contains(cmdarg[1].split(":")[1])) {
                    return true;
                }
                if (cmdarg[1].startsWith("cmd:") && cmdarg[1].split(":")[1].equalsIgnoreCase(Command) &&
                        cmdarg[0].startsWith("arg:") && argsRaw.contains(cmdarg[0].split(":")[1])) {
                    return true;
                }
            } else {
                if (cmdarg[0].startsWith("cmd:") && cmdarg[0].split(":")[1].equalsIgnoreCase(Command)) {
                    return true;
                }
                if (cmdarg[0].startsWith("arg:") && argsRaw.contains(cmdarg[0].split(":")[1])) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean DenyCommands(Player p, String fullcmd) {
        if (!flagExists("deny-cmds")) {
            return true;
        }

        String Command = fullcmd.replace("/", "").split(" ")[0];
        List<String> argsRaw = Arrays.asList(fullcmd.replace("/" + Command + " ", "").split(" "));

        //As Blacklist
        String[] flagCmds = getFlagString("deny-cmds").split(",");
        for (String cmd : flagCmds) {

            if (cmd.startsWith(" ")) {
                cmd = cmd.substring(1);
            }
            String[] cmdarg = cmd.split(" ");
            if (cmdarg.length == 1) {
                if (cmdarg[0].startsWith("cmd:") && cmdarg[0].split(":")[1].equalsIgnoreCase(Command)) {
                    return false;
                }
                if (cmdarg[0].startsWith("arg:") && argsRaw.contains(cmdarg[0].split(":")[1])) {
                    return false;
                }
            } else {
                if (cmdarg[0].startsWith("cmd:") && cmdarg[0].split(":")[1].equalsIgnoreCase(Command) &&
                        cmdarg[1].startsWith("arg:") && argsRaw.contains(cmdarg[1].split(":")[1])) {
                    return false;
                }
                if (cmdarg[1].startsWith("cmd:") && cmdarg[1].split(":")[1].equalsIgnoreCase(Command) &&
                        cmdarg[0].startsWith("arg:") && argsRaw.contains(cmdarg[0].split(":")[1])) {
                    return false;
                }
            }
        }

        return true;
    }


    //---------------------- Player Flags --------------------------//
    public boolean allowFishing(Player p) {
        if (!RPConfig.isFlagEnabled("fishing")) {
            return RPConfig.getBool("flags.fishing") || checkAllowedPlayer(p);
        }
        return getFlagBool("fishing") || checkAllowedPlayer(p);
    }

    public boolean allowPressPlate(Player p) {
        if (!RPConfig.isFlagEnabled("press-plate")) {
            return RPConfig.getBool("flags.press-plate") || checkAllowedPlayer(p);
        }
        return getFlagBool("press-plate") || checkAllowedPlayer(p);
    }

    public boolean canBuild(Player p) {
        if (flagExists("for-sale") && !RedProtect.get().ph.hasPerm(p, "redprotect.bypass")) {
            return false;
        }
        if (!RPConfig.isFlagEnabled("build")) {
            return RPConfig.getBool("flags.build") || checkAllowedPlayer(p);
        }
        return (getFlagBool("build") || checkAllowedPlayer(p));
    }

    public boolean leavesDecay() {
        if (!RPConfig.isFlagEnabled("leaves-decay")) {
            return RPConfig.getBool("flags.leaves-decay");
        }
        return getFlagBool("leaves-decay");
    }

    /**
     * Allow non members of this region to break/place spawners.
     *
     * @return boolean
     */
    public boolean allowSpawner(Player p) {
        if (!RPConfig.isFlagEnabled("allow-spawner")) {
            return RPConfig.getBool("flags.allow-spawner");
        }
        return getFlagBool("allow-spawner") || checkAllowedPlayer(p);
    }

    /**
     * Allow players with fly enabled fly on this region.
     *
     * @return boolean
     */
    public boolean canFly(Player p) {
        if (!RPConfig.isFlagEnabled("allow-fly")) {
            return RPConfig.getBool("flags.allow-fly");
        }
        return getFlagBool("allow-fly") || checkAllowedPlayer(p);
    }

    /**
     * Allow ice form by players.
     *
     * @return boolean
     */
    public boolean canIceForm(Player p) {
        if (!RPConfig.isFlagEnabled("iceform-player")) {
            return RPConfig.getBool("flags.iceform-player");
        }
        return getFlagBool("iceform-player");
    }

    /**
     * Allow ice form by entity and by world.
     *
     * @return boolean
     */
    public boolean canIceForm() {
        if (!RPConfig.isFlagEnabled("iceform-world")) {
            return RPConfig.getBool("flags.iceform-world");
        }
        return getFlagBool("iceform-world");
    }

    public boolean FlowDamage() {
        if (!RPConfig.isFlagEnabled("flow-damage")) {
            return RPConfig.getBool("flags.flow-damage");
        }
        return getFlagBool("flow-damage");
    }

    public boolean canMobLoot() {
        if (!RPConfig.isFlagEnabled("mob-loot")) {
            return RPConfig.getBool("flags.mob-loot");
        }
        return getFlagBool("mob-loot");
    }

    public boolean usePotions(Player p) {
        if (!RPConfig.isFlagEnabled("use-potions")) {
            return RPConfig.getBool("flags.use-potions");
        }
        return getFlagBool("use-potions") || checkAllowedPlayer(p);
    }

    public boolean allowEffects(Player p) {
        if (!RPConfig.isFlagEnabled("allow-effects")) {
            return RPConfig.getBool("flags.allow-effects");
        }
        return getFlagBool("allow-effects") || checkAllowedPlayer(p);
    }

    public boolean canPVP(Player attack, Player defend) {
        if (defend != null && RedProtect.get().SC && SCHook.inWar(this, attack, defend)) {
            return true;
        }
        if (!RPConfig.isFlagEnabled("pvp")) {
            return RPConfig.getBool("flags.pvp") || RedProtect.get().ph.hasPerm(attack, "redprotect.bypass");
        }
        return getFlagBool("pvp") || RedProtect.get().ph.hasPerm(attack, "redprotect.bypass");
    }

    public boolean canEnderChest(Player p) {
        if (!RPConfig.isFlagEnabled("ender-chest")) {
            return RPConfig.getBool("flags.ender-chest") || checkAllowedPlayer(p);
        }
        return getFlagBool("ender-chest") || checkAllowedPlayer(p);
    }

    public boolean canChest(Player p) {
        if (!RPConfig.isFlagEnabled("chest")) {
            return RPConfig.getBool("flags.chest") || checkAllowedPlayer(p);
        }
        return getFlagBool("chest") || checkAllowedPlayer(p);
    }

    public boolean canLever(Player p) {
        if (!RPConfig.isFlagEnabled("lever")) {
            return RPConfig.getBool("flags.lever") || checkAllowedPlayer(p);
        }
        return getFlagBool("lever") || checkAllowedPlayer(p);
    }

    public boolean canButton(Player p) {
        if (!RPConfig.isFlagEnabled("button")) {
            return RPConfig.getBool("flags.button") || checkAllowedPlayer(p);
        }
        return getFlagBool("button") || checkAllowedPlayer(p);
    }

    public boolean canDoor(Player p) {
        if (!RPConfig.isFlagEnabled("door")) {
            return RPConfig.getBool("flags.door") || checkAllowedPlayer(p);
        }
        return getFlagBool("door") || checkAllowedPlayer(p);
    }

    public boolean canSpawnMonsters() {
        if (!RPConfig.isFlagEnabled("spawn-monsters")) {
            return RPConfig.getBool("flags.spawn-monsters");
        }
        return getFlagBool("spawn-monsters");
    }

    public boolean canSpawnPassives() {
        if (!RPConfig.isFlagEnabled("spawn-animals")) {
            return RPConfig.getBool("flags.spawn-animals");
        }
        return getFlagBool("spawn-animals");
    }

    public boolean canMinecart(Player p) {
        if (!RPConfig.isFlagEnabled("minecart")) {
            return RPConfig.getBool("flags.minecart") || checkAllowedPlayer(p);
        }
        return getFlagBool("minecart") || checkAllowedPlayer(p);
    }

    public boolean canInteractPassives(Player p) {
        if (!RPConfig.isFlagEnabled("passives")) {
            return RPConfig.getBool("flags.passives") || checkAllowedPlayer(p);
        }
        return getFlagBool("passives") || checkAllowedPlayer(p);
    }

    public boolean canFlow() {
        if (!RPConfig.isFlagEnabled("flow")) {
            return RPConfig.getBool("flags.flow");
        }
        return getFlagBool("flow");
    }

    public boolean canFire() {
        if (!RPConfig.isFlagEnabled("fire")) {
            return RPConfig.getBool("flags.fire");
        }
        return getFlagBool("fire");
    }

    public boolean AllowHome(Player p) {
        if (!RPConfig.isFlagEnabled("allow-home")) {
            return RPConfig.getBool("flags.allow-home") || checkAllowedPlayer(p);
        }
        return getFlagBool("allow-home") || checkAllowedPlayer(p);
    }

    public boolean canGrow() {
        if (!RPConfig.isFlagEnabled("can-grow")) {
            return RPConfig.getBool("flags.can-grow");
        }
        return getFlagBool("can-grow");
    }
    //--------------------------------------------------------------//

    public long getValue() {
        return this.value;
    }

    public void setValue(long value) {
        setToSave(true);
        RedProtect.get().rm.updateLiveRegion(this, "value", String.valueOf(value));
        this.value = value;
    }

    private boolean checkAllowedPlayer(Player p) {
        return this.isLeader(p) || this.isAdmin(p) || this.isMember(p) || RedProtect.get().ph.hasPerm(p, "redprotect.bypass");
    }

    public Set<Location> getLimitLocs(int locy) {
        final Set<Location> locBlocks = new HashSet<>();
        Location loc1 = this.getMinLocation();
        Location loc2 = this.getMaxLocation();
        World w = Bukkit.getWorld(this.getWorld());

        for (int x = (int) loc1.getX(); x <= (int) loc2.getX(); ++x) {
            for (int z = (int) loc1.getZ(); z <= (int) loc2.getZ(); ++z) {
                //for (int y = (int) loc1.getY(); y <= (int) loc2.getY(); ++y) {
                if (z == loc1.getZ() || z == loc2.getZ() ||
                        x == loc1.getX() || x == loc2.getX()) {
                    locBlocks.add(new Location(w, x, locy, z));
                }
                //}
            }
        }
        return locBlocks;
    }

    public Set<Location> getLimitLocs(int miny, int maxy, boolean define) {
        final Set<Location> locBlocks = new HashSet<>();
        Location loc1 = this.getMinLocation();
        Location loc2 = this.getMaxLocation();
        World w = Bukkit.getWorld(this.getWorld());

        for (int x = (int) loc1.getX(); x <= (int) loc2.getX(); ++x) {
            for (int z = (int) loc1.getZ(); z <= (int) loc2.getZ(); ++z) {
                for (int y = miny; y <= maxy; ++y) {
                    if ((z == loc1.getZ() || z == loc2.getZ() ||
                            x == loc1.getX() || x == loc2.getX())
                            && (define || new Location(w, x, y, z).getBlock().getType().name().contains(RPConfig.getString("region-settings.block-id")))) {
                        locBlocks.add(new Location(w, x, y, z));
                    }
                }
            }
        }
        return locBlocks;
    }

    public List<Location> get4Points(int y) {
        List<Location> locs = new ArrayList<>();
        locs.add(this.getMinLocation());
        locs.add(new Location(this.getMinLocation().getWorld(), this.minMbrX, y, this.minMbrZ + (this.maxMbrZ - this.minMbrZ)));
        locs.add(this.getMaxLocation());
        locs.add(new Location(this.getMinLocation().getWorld(), this.minMbrX + (this.maxMbrX - this.minMbrX), y, this.minMbrZ));
        return locs;
    }

    public Location getCenterLoc() {
        return new Location(Bukkit.getWorld(this.world), this.getCenterX(), this.getCenterY(), this.getCenterZ());
    }

    public String getAdminDesc() {
        if (this.admins.size() == 0) {
            return "[none]";
        }
        StringBuilder adminsList = new StringBuilder();
        for (String admin : this.admins) {
            adminsList.append(", ").append(RPUtil.UUIDtoPlayer(admin));
        }
        return "[" + adminsList.toString().substring(2) + "]";
    }

    public String getLeadersDesc() {
        if (this.leaders.size() == 0) {
            addLeader(RPConfig.getString("region-settings.default-leader"));
        }
        StringBuilder leaderList = new StringBuilder();
        for (String leader : this.leaders) {
            leaderList.append(", ").append(RPUtil.UUIDtoPlayer(leader));
        }
        return "[" + leaderList.delete(0, 2).toString() + "]";
    }

    public boolean sameLeaders(Region r) {
        for (String l : this.leaders) {
            if (r.getLeaders().contains(l)) {
                return true;
            }
        }
        return false;
    }
}
