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

package br.net.fabiozumbi12.RedProtect.Bukkit;

import br.net.fabiozumbi12.RedProtect.Bukkit.API.events.ChangeRegionFlagEvent;
import br.net.fabiozumbi12.RedProtect.Bukkit.helpers.EconomyManager;
import br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RedProtectUtil;
import br.net.fabiozumbi12.RedProtect.Bukkit.hooks.SimpleClansHook;
import br.net.fabiozumbi12.RedProtect.Core.region.CoreRegion;
import br.net.fabiozumbi12.RedProtect.Core.region.PlayerRegion;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Crops;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Represents a 3D Bukkit region created by players.
 */
public class Region extends CoreRegion {


    private String dynmapSet = RedProtect.get().config.configRoot().hooks.dynmap.marks_groupname;
    private int particleID = 0;

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
     * @param tppoint   Teleport Point
     * @param candel    Can delete?
     */
    public Region(String name, Set<PlayerRegion> admins, Set<PlayerRegion> members, Set<PlayerRegion> leaders, Location minLoc, Location maxLoc, HashMap<String, Object> flags, String wMessage, int prior, String worldName, String date, long value, Location tppoint, boolean candel) {
        super(name, admins, members, leaders, new int[]{minLoc.getBlockX(), minLoc.getBlockY(), minLoc.getBlockZ()}, new int[]{maxLoc.getBlockX(), maxLoc.getBlockY(), maxLoc.getBlockZ()}, flags, wMessage, prior, worldName, date, value, tppoint == null ? null : new int[]{tppoint.getBlockX(), tppoint.getBlockY(), tppoint.getBlockZ()}, tppoint == null ? null : new float[]{tppoint.getPitch(), tppoint.getYaw()}, candel);
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
     * @param minY      Min location
     * @param maxY      Max Location
     * @param flags     Flag names and values.
     * @param wMessage  Welcome message.
     * @param prior     Priority of region.
     * @param worldName Name of world for this region.
     * @param date      Date of latest visit of an admin or leader.
     * @param value     Last playername of this region.
     * @param tppoint   Teleport Point
     * @param candel    Can delete?
     */
    public Region(String name, Set<PlayerRegion> admins, Set<PlayerRegion> members, Set<PlayerRegion> leaders, int maxMbrX, int minMbrX, int maxMbrZ, int minMbrZ, int minY, int maxY, HashMap<String, Object> flags, String wMessage, int prior, String worldName, String date, long value, Location tppoint, boolean candel) {
        super(name, admins, members, leaders, maxMbrX, minMbrX, maxMbrZ, minMbrZ, minY, maxY, flags, wMessage, prior, worldName, date, value, tppoint == null ? null : new int[]{tppoint.getBlockX(), tppoint.getBlockY(), tppoint.getBlockZ()}, tppoint == null ? null : new float[]{tppoint.getPitch(), tppoint.getYaw()}, candel);
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
     * @param flags     Default flags
     * @param welcome   Set a welcome message.
     * @param value     A playername in server economy.
     * @param tppoint   Teleport Point
     * @param candel    Can delete?
     */
    public Region(String name, Set<PlayerRegion> admins, Set<PlayerRegion> members, Set<PlayerRegion> leaders, int[] x, int[] z, int miny, int maxy, int prior, String worldName, String date, Map<String, Object> flags, String welcome, long value, Location tppoint, boolean candel) {
        super(name, admins, members, leaders, x, z, miny, maxy, prior, worldName, date, flags, welcome, value, tppoint == null ? null : new int[]{tppoint.getBlockX(), tppoint.getBlockY(), tppoint.getBlockZ()}, tppoint == null ? null : new float[]{tppoint.getPitch(), tppoint.getYaw()}, candel);
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
        super(name, new int[]{min.getBlockX(), min.getBlockY(), min.getBlockZ()}, new int[]{max.getBlockX(), max.getBlockY(), max.getBlockZ()}, world, RedProtect.get().config.configRoot().region_settings.default_leader, RedProtect.get().config.getDefFlagsValues());
        checkParticle();
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
        if (!RedProtect.get().config.configRoot().region_settings.enable_flag_sign) {
            return;
        }
        List<Location> locs = RedProtect.get().config.getSigns(this.getID());
        if (locs.size() > 0) {
            for (Location loc : locs) {
                if (loc.getBlock().getState() instanceof Sign) {
                    Sign s = (Sign) loc.getBlock().getState();
                    String[] lines = s.getLines();
                    if (lines[0].equalsIgnoreCase("[flag]")) {
                        if (lines[1].equalsIgnoreCase(fname) && this.name.equalsIgnoreCase(ChatColor.stripColor(lines[2]))) {
                            s.setLine(3, RedProtect.get().lang.get("region.value") + " " + ChatColor.translateAlternateColorCodes('&', RedProtect.get().lang.translBool(getFlagString(fname))));
                            s.update();
                            RedProtect.get().config.putSign(this.getID(), loc);
                        }
                    } else {
                        RedProtect.get().config.removeSign(this.getID(), loc);
                    }
                } else {
                    RedProtect.get().config.removeSign(this.getID(), loc);
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

    public Location getTPPoint() {
        if (this.tppoint == null) return null;
        return new Location(Bukkit.getWorld(this.world), this.tppoint[0], this.tppoint[1], this.tppoint[2]);
    }

    public void setTPPoint(@Nullable Location loc) {
        setToSave(true);
        if (loc != null) {
            this.tppoint = new int[]{loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()};
            this.tppointYaw = new float[]{loc.getPitch(), loc.getYaw()};
            double x = loc.getX();
            double y = loc.getY();
            double z = loc.getZ();
            float yaw = loc.getYaw();
            float pitch = loc.getPitch();
            RedProtect.get().rm.updateLiveRegion(this, "tppoint", x + "," + y + "," + z + "," + yaw + "," + pitch);
        } else {
            this.tppoint = null;
            RedProtect.get().rm.updateLiveRegion(this, "tppoint", "");
        }
    }

    public void setDate(String value) {
        setToSave(true);
        this.date = value;
        RedProtect.get().rm.updateLiveRegion(this, "date", value);
    }

    public void setMaxY(int y) {
        setToSave(true);
        this.maxY = y;
        RedProtect.get().rm.updateLiveRegion(this, "maxy", String.valueOf(y));
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

    public void setWorld(String w) {
        setToSave(true);
        this.world = w;
        RedProtect.get().rm.updateLiveRegion(this, "world", w);
    }

    public void setPrior(int prior) {
        setToSave(true);
        this.prior = prior;
        RedProtect.get().rm.updateLiveRegion(this, "prior", "" + prior);
    }

    public void setWelcome(String s) {
        setToSave(true);
        this.wMessage = s;
        RedProtect.get().rm.updateLiveRegion(this, "wel", s);
    }

    public String getDynmapSet() {
        return this.dynmapSet;
    }

    public void setAdmins(Set<PlayerRegion> admins) {
        setToSave(true);
        this.admins = admins;
        RedProtect.get().rm.updateLiveRegion(this, "admins", serializeMembers(admins));
    }

    public void setMembers(Set<PlayerRegion> members) {
        setToSave(true);
        this.members = members;
        RedProtect.get().rm.updateLiveRegion(this, "members", serializeMembers(members));
    }

    public void setLeaders(Set<PlayerRegion> leaders) {
        setToSave(true);
        this.leaders = leaders;
        RedProtect.get().rm.updateLiveRegion(this, "leaders", serializeMembers(leaders));
    }

    public String info() {
        final StringBuilder leaderStringBuilder = new StringBuilder();
        final StringBuilder adminStringBuilder = new StringBuilder();
        final StringBuilder memberStringBuilder = new StringBuilder();
        final StringBuilder today = new StringBuilder();
        String leaderString = "None";
        String adminString = "None";
        String memberString = "None";

        String wMsgTemp;
        String IsTops = ChatColor.translateAlternateColorCodes('&', RedProtect.get().lang.translBool(isOnTop()));
        String wName = this.world;
        String colorChar = "";
        String dynmapInfo = "";

        if (RedProtect.get().config.configRoot().region_settings.world_colors.containsKey(this.world)) {
            colorChar = ChatColor.translateAlternateColorCodes('&', RedProtect.get().config.configRoot().region_settings.world_colors.get(this.world));
        }

        leaders.removeIf(Objects::isNull);
        leaders.forEach(leader -> leaderStringBuilder.append(", ").append(leader.getPlayerName()));

        admins.removeIf(Objects::isNull);
        admins.forEach(admin -> adminStringBuilder.append(", ").append(admin.getPlayerName()));

        members.removeIf(Objects::isNull);
        members.forEach(member -> memberStringBuilder.append(", ").append(member.getPlayerName()));

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
            wMsgTemp = RedProtect.get().lang.get("region.welcome.notset");
        } else {
            wMsgTemp = wMessage;
        }

        if (this.date.equals(RedProtectUtil.dateNow())) {
            today.append(RedProtect.get().lang.get("region.today"));
        } else {
            today.append(this.date);
        }
        for (PlayerRegion pname : this.leaders) {
            Player play = RedProtect.get().getServer().getPlayer(pname.getPlayerName());
            if (play != null && play.isOnline() && pname.getPlayerName().equalsIgnoreCase(RedProtect.get().config.configRoot().region_settings.default_leader)) {
                today.append(ChatColor.GREEN).append("Online!");
                break;
            }
        }
        for (PlayerRegion pname : this.admins) {
            Player play = RedProtect.get().getServer().getPlayer(pname.getPlayerName());
            if (play != null && play.isOnline()) {
                today.append(ChatColor.GREEN).append("Online!");
                break;
            }
        }

        if (RedProtect.get().hooks.Dyn && RedProtect.get().config.configRoot().hooks.dynmap.enable) {
            dynmapInfo = RedProtect.get().lang.get("region.dynmap") + " " + (this.getFlagBool("dynmap") ? RedProtect.get().lang.get("region.dynmap-showing") : RedProtect.get().lang.get("region.dynmap-hiding")) + ", " + RedProtect.get().lang.get("region.dynmap-set") + " " + this.getDynmapSet() + "\n";
        }

        return RedProtect.get().lang.get("region.name") + " " + colorChar + this.name + RedProtect.get().lang.get("general.color") + " | " + RedProtect.get().lang.get("region.priority") + " " + this.prior + "\n" +
                RedProtect.get().lang.get("region.priority.top") + " " + IsTops + RedProtect.get().lang.get("general.color") + " | " + RedProtect.get().lang.get("region.lastvalue") + " " + EconomyManager.getFormatted(this.value) + "\n" +
                RedProtect.get().lang.get("region.world") + " " + colorChar + wName + RedProtect.get().lang.get("general.color") + " | " + RedProtect.get().lang.get("region.center") + " " + this.getCenterX() + ", " + this.getCenterZ() + "\n" +
                RedProtect.get().lang.get("region.ysize") + " " + this.minY + " - " + this.maxY + RedProtect.get().lang.get("general.color") + " | " + RedProtect.get().lang.get("region.area") + " " + this.getArea() + "\n" +
                RedProtect.get().lang.get("region.leaders") + " " + leaderString + "\n" +
                RedProtect.get().lang.get("region.admins") + " " + adminString + RedProtect.get().lang.get("general.color") + " | " + RedProtect.get().lang.get("region.members") + " " + memberString + "\n" +
                RedProtect.get().lang.get("region.date") + " " + today.toString() + "\n" +
                dynmapInfo +
                RedProtect.get().lang.get("region.welcome.msg") + " " + (wMsgTemp.equals("hide ") ? RedProtect.get().lang.get("region.hiding") : ChatColor.translateAlternateColorCodes('&', wMsgTemp));
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

    public boolean isLeader(Player player) {
        return isLeader(player.getUniqueId().toString()) || isLeader(player.getName());
    }

    public boolean isAdmin(Player player) {
        return isAdmin(player.getUniqueId().toString()) || isAdmin(player.getName());
    }

    public boolean isMember(Player player) {
        boolean cs = RedProtect.get().hooks.simpleClans && SimpleClansHook.getPlayerClan(this, player);
        if (cs) return true;

        return isMember(player.getUniqueId().toString()) || isMember(player.getName());
    }

    /**
     * Add an leader to the Region. The string need to be UUID if Online Mode, or Player Name if Offline Mode.
     *
     * @param uuid - UUID or Player Name.
     */
    public void addLeader(String uuid) {
        setToSave(true);
        String name = RedProtectUtil.UUIDtoPlayer(uuid).toLowerCase();
        PlayerRegion pInfo = new PlayerRegion(RedProtectUtil.PlayerToUUID(uuid), name);

        this.members.removeIf(m -> m.getUUID().equalsIgnoreCase(uuid) || m.getPlayerName().equalsIgnoreCase(uuid));
        this.admins.removeIf(m -> m.getUUID().equalsIgnoreCase(uuid) || m.getPlayerName().equalsIgnoreCase(uuid));
        this.leaders.add(pInfo);

        RedProtect.get().rm.updateLiveRegion(this, "leaders", serializeMembers(leaders));
        RedProtect.get().rm.updateLiveRegion(this, "admins", serializeMembers(admins));
        RedProtect.get().rm.updateLiveRegion(this, "members", serializeMembers(members));
    }

    /**
     * Add a member to the Region. The string need to be UUID if Online Mode, or Player Name if Offline Mode.
     *
     * @param uuid - UUID or Player Name.
     */
    public void addMember(String uuid) {
        setToSave(true);
        String name = RedProtectUtil.UUIDtoPlayer(uuid).toLowerCase();
        PlayerRegion pInfo = new PlayerRegion(RedProtectUtil.PlayerToUUID(uuid), name);

        this.admins.removeIf(m -> m.getUUID().equalsIgnoreCase(uuid) || m.getPlayerName().equalsIgnoreCase(uuid));
        this.leaders.removeIf(m -> m.getUUID().equalsIgnoreCase(uuid) || m.getPlayerName().equalsIgnoreCase(uuid));
        this.members.add(pInfo);

        RedProtect.get().rm.updateLiveRegion(this, "leaders", serializeMembers(leaders));
        RedProtect.get().rm.updateLiveRegion(this, "admins", serializeMembers(admins));
        RedProtect.get().rm.updateLiveRegion(this, "members", serializeMembers(members));
    }

    /**
     * Add an admin to the Region. The string need to be UUID if Online Mode, or Player Name if Offline Mode.
     *
     * @param uuid - UUID or Player Name.
     */
    public void addAdmin(String uuid) {
        setToSave(true);
        String name = RedProtectUtil.UUIDtoPlayer(uuid).toLowerCase();
        PlayerRegion pInfo = new PlayerRegion(RedProtectUtil.PlayerToUUID(uuid), name);

        this.members.removeIf(m -> m.getUUID().equalsIgnoreCase(uuid) || m.getPlayerName().equalsIgnoreCase(uuid));
        this.leaders.removeIf(m -> m.getUUID().equalsIgnoreCase(uuid) || m.getPlayerName().equalsIgnoreCase(uuid));
        this.admins.add(pInfo);

        RedProtect.get().rm.updateLiveRegion(this, "leaders", serializeMembers(leaders));
        RedProtect.get().rm.updateLiveRegion(this, "admins", serializeMembers(admins));
        RedProtect.get().rm.updateLiveRegion(this, "members", serializeMembers(members));
    }

    /**
     * Remove an member to the Region. The string need to be UUID if Online Mode, or Player Name if Offline Mode.
     *
     * @param uuid - UUID or Player Name.
     */
    public void removeMember(String uuid) {
        setToSave(true);

        this.members.removeIf(m -> m.getUUID().equalsIgnoreCase(uuid) || m.getPlayerName().equalsIgnoreCase(uuid));
        this.admins.removeIf(m -> m.getUUID().equalsIgnoreCase(uuid) || m.getPlayerName().equalsIgnoreCase(uuid));
        this.leaders.removeIf(m -> m.getUUID().equalsIgnoreCase(uuid) || m.getPlayerName().equalsIgnoreCase(uuid));

        RedProtect.get().rm.updateLiveRegion(this, "leaders", serializeMembers(leaders));
        RedProtect.get().rm.updateLiveRegion(this, "admins", serializeMembers(admins));
        RedProtect.get().rm.updateLiveRegion(this, "members", serializeMembers(members));
    }

    /**
     * Remove an admin to the Region. The string need to be UUID if Online Mode, or Player Name if Offline Mode.
     *
     * @param uuid - UUID or Player Name.
     */
    public void removeAdmin(String uuid) {
        setToSave(true);
        String name = RedProtectUtil.UUIDtoPlayer(uuid).toLowerCase();
        PlayerRegion pInfo = new PlayerRegion(RedProtectUtil.PlayerToUUID(uuid), name);

        this.leaders.removeIf(m -> m.getUUID().equalsIgnoreCase(uuid) || m.getPlayerName().equalsIgnoreCase(uuid));
        this.admins.removeIf(m -> m.getUUID().equalsIgnoreCase(uuid) || m.getPlayerName().equalsIgnoreCase(uuid));
        this.members.add(pInfo);

        RedProtect.get().rm.updateLiveRegion(this, "leaders", serializeMembers(leaders));
        RedProtect.get().rm.updateLiveRegion(this, "admins", serializeMembers(admins));
        RedProtect.get().rm.updateLiveRegion(this, "members", serializeMembers(members));
    }

    /**
     * Remove an leader to the Region. The string need to be UUID if Online Mode, or Player Name if Offline Mode.
     *
     * @param uuid - UUID or Player Name.
     */
    public void removeLeader(String uuid) {
        setToSave(true);
        String name = RedProtectUtil.UUIDtoPlayer(uuid).toLowerCase();
        PlayerRegion pInfo = new PlayerRegion(RedProtectUtil.PlayerToUUID(uuid), name);

        this.members.removeIf(m -> m.getUUID().equalsIgnoreCase(uuid) || m.getPlayerName().equalsIgnoreCase(uuid));
        this.leaders.removeIf(m -> m.getUUID().equalsIgnoreCase(uuid) || m.getPlayerName().equalsIgnoreCase(uuid));
        this.admins.add(pInfo);

        RedProtect.get().rm.updateLiveRegion(this, "leaders", serializeMembers(leaders));
        RedProtect.get().rm.updateLiveRegion(this, "admins", serializeMembers(admins));
        RedProtect.get().rm.updateLiveRegion(this, "members", serializeMembers(members));
    }

    public boolean getFlagBool(String key) {
        if (!flagExists(key) || !RedProtect.get().config.isFlagEnabled(key)) {
            if (RedProtect.get().config.getDefFlagsValues().get(key) != null) {
                return (Boolean) RedProtect.get().config.getDefFlagsValues().get(key);
            } else {
                return (Boolean) RedProtect.get().config.configRoot().flags.getOrDefault(key, false);
            }
        }
        return this.flags.get(key) instanceof Boolean && (Boolean) this.flags.get(key);
    }

    public String getFlagString(String key) {
        if (!flagExists(key) || !RedProtect.get().config.isFlagEnabled(key)) {
            if (RedProtect.get().config.getDefFlagsValues().get(key) != null) {
                return (String) RedProtect.get().config.getDefFlagsValues().get(key);
            } else {
                return RedProtect.get().config.configRoot().flags.get(key).toString();
            }
        }
        return this.flags.get(key).toString();
    }

    public String getFlagInfo() {
        StringBuilder flaginfo = new StringBuilder();
        for (String flag : this.flags.keySet()) {
            if (RedProtect.get().config.getDefFlags().contains(flag)) {
                String flagValue = getFlagString(flag);
                if (flagValue.equalsIgnoreCase("true") || flagValue.equalsIgnoreCase("false")) {
                    flaginfo.append(", ").append(ChatColor.AQUA).append(flag).append(": ").append(ChatColor.translateAlternateColorCodes('&', RedProtect.get().lang.translBool(flagValue)));
                } else {
                    flaginfo.append(", ").append(ChatColor.AQUA).append(flag).append(": ").append(ChatColor.GRAY).append(flagValue);
                }
            }

            if (flaginfo.toString().contains(flag)) {
                continue;
            }

            if (RedProtect.get().config.AdminFlags.contains(flag)) {
                String flagValue = getFlagString(flag);
                if (flagValue.equalsIgnoreCase("true") || flagValue.equalsIgnoreCase("false")) {
                    flaginfo.append(", ").append(ChatColor.AQUA).append(flag).append(": ").append(ChatColor.translateAlternateColorCodes('&', RedProtect.get().lang.translBool(flagValue)));
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
        Region newr = RedProtect.get().rm.getTopRegion(RedProtect.get().getServer().getWorld(this.getWorld()), this.getCenterX(), this.getCenterY(), this.getCenterZ());
        return newr == null || newr.equals(this);
    }

    //---------------------- Admin Flags --------------------------//

    public boolean canSpawnWhiter() {
        return !flagExists("spawn-wither") || getFlagBool("spawn-wither");
    }

    public int getMaxPlayers() {
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
            boolean waiting = false;
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

    public boolean isKeepInventory() {
        return flagExists("keep-inventory") && getFlagBool("keep-inventory");
    }

    public boolean isKeepLevels() {
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

    public boolean isForcePVP() {
        return flagExists("forcepvp") && getFlagBool("forcepvp");
    }

    public boolean canHunger() {
        return !flagExists("can-hunger") || getFlagBool("can-hunger");
    }

    public boolean canSign(Player p) {
        if (!flagExists("sign")) {
            return isPlayerAllowed(p);
        }
        return getFlagBool("sign") || isPlayerAllowed(p);
    }

    public boolean canExit(Player p) {
        if (!canExitWithItens(p)) {
            return false;
        }
        return !flagExists("exit") || getFlagBool("exit") || RedProtect.get().ph.hasPerm(p, "redprotect.region-exit." + this.name) || isPlayerAllowed(p);
    }

    public boolean canEnter(Player p) {
        if (RedProtect.get().denyEnter.containsKey(p.getName()) && RedProtect.get().denyEnter.get(p.getName()).contains(this.getID())) {
            return isPlayerAllowed(p);
        }

        return !flagExists("enter") || getFlagBool("enter") || RedProtect.get().ph.hasPerm(p, "redprotect.region-enter." + this.name) || isPlayerAllowed(p);
    }

    public boolean canExitWithItens(Player p) {
        if (!flagExists("deny-exit-items")) {
            return true;
        }

        if (isPlayerAllowed(p)) {
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

        if (isPlayerAllowed(p)) {
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
        if (isPlayerAllowed(p)) {
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
            return isPlayerAllowed(p);
        }
        return getFlagBool("teleport") || isPlayerAllowed(p);
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
        return !flagExists("up-skills") || getFlagBool("up-skills") || isPlayerAllowed(p);
    }

    public boolean canBack(Player p) {
        return !flagExists("can-back") || getFlagBool("can-back") || isPlayerAllowed(p);
    }

    public boolean isPvPArena() {
        return flagExists("pvparena") && getFlagBool("pvparena");
    }

    public boolean allowMod(Player p) {
        if (!flagExists("allow-mod")) {
            return isPlayerAllowed(p);
        }
        return getFlagBool("allow-mod") || isPlayerAllowed(p);
    }

    public boolean canEnterPortal(Player p) {
        return !flagExists("portal-enter") || getFlagBool("portal-enter") || isPlayerAllowed(p);
    }

    public boolean canExitPortal(Player p) {
        return !flagExists("portal-exit") || getFlagBool("portal-exit") || isPlayerAllowed(p);
    }

    public boolean canPet(Player p) {
        return !flagExists("can-pet") || getFlagBool("can-pet") || isPlayerAllowed(p);
    }

    public boolean canProtectiles(Player p) {
        return !flagExists("can-projectiles") || getFlagBool("can-projectiles") || isPlayerAllowed(p);
    }

    public boolean canDrop(Player p) {
        return !flagExists("can-drop") || getFlagBool("can-drop") || isPlayerAllowed(p);
    }

    public boolean canPickup(Player p) {
        return !flagExists("can-pickup") || getFlagBool("can-pickup") || isPlayerAllowed(p);
    }

    public boolean canCreatePortal() {
        return !flagExists("allow-create-portal") || getFlagBool("allow-create-portal");
    }

    public boolean isCmdAllowed(Player p, String fullcmd) {
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

    public boolean isCmdDenied(String fullcmd) {
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
    public boolean canFish(Player p) {
        return getFlagBool("fishing") || isPlayerAllowed(p);
    }

    public boolean canPressPlate(Player p) {
        return getFlagBool("press-plate") || isPlayerAllowed(p);
    }

    public boolean canBuild(Player p) {
        if (flagExists("for-sale") && !RedProtect.get().ph.hasPerm(p, "redprotect.bypass")) {
            return false;
        }
        return (getFlagBool("build") || isPlayerAllowed(p));
    }

    public boolean leavesDecay() {
        return getFlagBool("leaves-decay");
    }

    /**
     * Check if the specified player can place a spawner in this region.
     *
     * @param p The player to check for permission.
     * @return boolean if the player can place o not a spawner.
     */
    public boolean canPlaceSpawner(Player p) {
        return getFlagBool("allow-spawner") || isPlayerAllowed(p);
    }

    /**
     * Check if the specified player can fly in this region.
     *
     * @param p The player to check for permission.
     * @return boolean if the player can fly or not.
     */
    public boolean canFly(Player p) {
        return getFlagBool("allow-fly") || isPlayerAllowed(p);
    }

    /**
     * Allow ice form by players.
     *
     * @return boolean
     */
    public boolean canIceForm(Player p) {
        return getFlagBool("iceform-player") || isPlayerAllowed(p);
    }

    /**
     * Allow ice form by entity and by world.
     *
     * @return boolean
     */
    public boolean canIceForm() {
        return getFlagBool("iceform-world");
    }

    public boolean allowGravity() {
        return getFlagBool("gravity");
    }

    public boolean canFlowDamage() {
        return getFlagBool("flow-damage");
    }

    public boolean canMobLoot() {
        return getFlagBool("mob-loot");
    }

    public boolean usePotions(Player p) {
        return getFlagBool("use-potions") || isPlayerAllowed(p);
    }

    public boolean canGetEffects(Player p) {
        return getFlagBool("allow-effects") || isPlayerAllowed(p);
    }

    public boolean canPVP(Player attacker, Player defender) {
        if (defender != null && RedProtect.get().hooks.simpleClans && SimpleClansHook.inWar(this, attacker, defender)) {
            return true;
        }
        return getFlagBool("pvp") || RedProtect.get().ph.hasPerm(attacker, "redprotect.bypass");
    }

    public boolean canEnderChest(Player p) {
        return getFlagBool("ender-chest") || isPlayerAllowed(p);
    }

    public boolean canChest(Player p) {
        return getFlagBool("chest") || isPlayerAllowed(p);
    }

    public boolean canLever(Player p) {
        return getFlagBool("lever") || isPlayerAllowed(p);
    }

    public boolean canButton(Player p) {
        return getFlagBool("button") || isPlayerAllowed(p);
    }

    public boolean canDoor(Player p) {
        return getFlagBool("door") || isPlayerAllowed(p);
    }

    public boolean canSpawnMonsters() {
        return getFlagBool("spawn-monsters");
    }

    public boolean canSpawnPassives() {
        return getFlagBool("spawn-animals");
    }

    public boolean canMinecart(Player p) {
        return getFlagBool("minecart") || isPlayerAllowed(p);
    }

    public boolean canInteractPassives(Player p) {
        return getFlagBool("passives") || isPlayerAllowed(p);
    }

    public boolean canFlow() {
        return getFlagBool("flow");
    }

    public boolean canFire() {
        return getFlagBool("fire");
    }

    public boolean isHomeAllowed(Player p) {
        return getFlagBool("allow-home") || isPlayerAllowed(p);
    }

    public boolean canGrow() {
        return getFlagBool("can-grow");
    }
    //--------------------------------------------------------------//

    public void setValue(long value) {
        setToSave(true);
        RedProtect.get().rm.updateLiveRegion(this, "value", String.valueOf(value));
        this.value = value;
    }

    private boolean isPlayerAllowed(Player p) {
        return this.isLeader(p) || this.isAdmin(p) || this.isMember(p) || RedProtect.get().ph.hasPerm(p, "redprotect.command.admin");
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
                            && (define || new Location(w, x, y, z).getBlock().getType().name().contains(RedProtect.get().config.configRoot().region_settings.block_id))) {
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
            return RedProtect.get().lang.get("region.none");
        }
        StringBuilder adminsList = new StringBuilder();
        for (PlayerRegion admin : this.admins) {
            adminsList.append(", ").append(admin.getPlayerName());
        }
        return "[" + adminsList.toString().substring(2) + "]";
    }

    public String getLeadersDesc() {
        if (this.leaders.size() == 0) {
            addLeader(RedProtect.get().config.configRoot().region_settings.default_leader);
        }
        StringBuilder leaderList = new StringBuilder();
        for (PlayerRegion leader : this.leaders) {
            leaderList.append(", ").append(leader.getPlayerName());
        }
        return "[" + leaderList.delete(0, 2).toString() + "]";
    }
}
