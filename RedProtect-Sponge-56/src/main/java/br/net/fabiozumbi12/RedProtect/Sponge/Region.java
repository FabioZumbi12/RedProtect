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

package br.net.fabiozumbi12.RedProtect.Sponge;

import br.net.fabiozumbi12.RedProtect.Core.region.CoreRegion;
import br.net.fabiozumbi12.RedProtect.Core.region.PlayerRegion;
import br.net.fabiozumbi12.RedProtect.Sponge.events.ChangeRegionFlagEvent;
import br.net.fabiozumbi12.RedProtect.Sponge.helpers.EconomyManager;
import br.net.fabiozumbi12.RedProtect.Sponge.helpers.RedProtectUtil;
import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleType;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Represents a 3D Sponge region created by players.
 */
public class Region extends CoreRegion {

    private String dynmapSet = RedProtect.get().config.configRoot().hooks.dynmap.marks_groupname;
    private Task task;

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
     * @param value     Last playername of this region.
     * @param tppoint   Teleport Point
     * @param candel    Can delete?
     */
    public Region(String name, Set<PlayerRegion> admins, Set<PlayerRegion> members, Set<PlayerRegion> leaders, int[] minLoc, int[] maxLoc, HashMap<String, Object> flags, String wMessage, int prior, String worldName, String date, long value, Location tppoint, boolean candel) {
        super(name, admins, members, leaders, minLoc, maxLoc, flags, wMessage, prior, worldName, date, value, tppoint == null ? null : new int[]{tppoint.getBlockX(), tppoint.getBlockY(), tppoint.getBlockZ()}, tppoint == null ? null : new float[]{0, 0}, candel);
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
        super(name, admins, members, leaders, maxMbrX, minMbrX, maxMbrZ, minMbrZ, minY, maxY, flags, wMessage, prior, worldName, date, value, tppoint == null ? null : new int[]{tppoint.getBlockX(), tppoint.getBlockY(), tppoint.getBlockZ()}, tppoint == null ? null : new float[]{0, 0}, candel);
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
     * @param flags     Default flags.
     * @param welcome   Set a welcome message.
     * @param value     A playername in server economy.
     * @param tppoint   Teleport Point
     * @param candel    Can delete?
     */
    public Region(String name, Set<PlayerRegion> admins, Set<PlayerRegion> members, Set<PlayerRegion> leaders, int[] x, int[] z, int miny, int maxy, int prior, String worldName, String date, Map<String, Object> flags, String welcome, long value, Location tppoint, boolean candel) {
        super(name, admins, members, leaders, x, z, miny, maxy, prior, worldName, date, flags, welcome, value, tppoint == null ? null : new int[]{tppoint.getBlockX(), tppoint.getBlockY(), tppoint.getBlockZ()}, tppoint == null ? null : new float[]{0, 0}, candel);
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
        Sponge.getScheduler().createSyncExecutor(RedProtect.get().container).schedule(() -> {
            if (this.flags.containsKey("particles")) {
                if (task == null) {

                    task = Sponge.getScheduler().createTaskBuilder().execute(() -> {
                        if (this.flags.containsKey("particles")) {
                            String[] part = flags.get("particles").toString().split(" ");
                            for (int i = 0; i < Integer.valueOf(part[1]); i++) {
                                Location locMin = getMinLocation();
                                Location locMax = getMaxLocation();

                                int dx = locMax.getBlockX() - locMin.getBlockX();
                                int dy = locMax.getBlockY() - locMin.getBlockY();
                                int dz = locMax.getBlockZ() - locMin.getBlockZ();
                                Random random = new Random();
                                int x = random.nextInt(Math.abs(dx) + 1) + locMin.getBlockX();
                                int y = random.nextInt(Math.abs(dy) + 1) + locMin.getBlockY();
                                int z = random.nextInt(Math.abs(dz) + 1) + locMin.getBlockZ();

                                ParticleType p = Sponge.getRegistry().getType(ParticleType.class, part[0]).get();
                                World w = Sponge.getServer().getWorld(world).get();
                                //ParticleTypes.FIRE
                                Location<World> loc = new Location<>(w, x + new Random().nextDouble(), y + new Random().nextDouble(), z + new Random().nextDouble());
                                if (loc.getBlock().getType().equals(BlockTypes.AIR)) {
                                    ParticleEffect.Builder pf = ParticleEffect.builder().type(p).quantity(1);


                                    if (part.length == 5) {
                                        pf.offset(new Vector3d(Double.parseDouble(part[2]), Double.parseDouble(part[3]), Double.parseDouble(part[4])));
                                    }
                                    if (part.length == 6) {
                                        pf.offset(new Vector3d(Double.parseDouble(part[2]), Double.parseDouble(part[3]), Double.parseDouble(part[4])));
                                        double multi = Double.parseDouble(part[5]);
                                        pf.velocity(new Vector3d((new Random().nextDouble() * 2 - 1) * multi, (new Random().nextDouble() * 2 - 1) * multi, (new Random().nextDouble() * 2 - 1) * multi));
                                    }

                                    if (!w.getEntities(ent -> ent instanceof Player && loc.getPosition().distance(ent.getLocation().getPosition()) <= 30).isEmpty()) {
                                        w.spawnParticles(pf.build(), loc.getPosition());
                                    }
                                }
                            }
                        }
                    }).intervalTicks(1).submit(RedProtect.get().container);
                }
            } else if (task != null) {
                notifyRemove();
            }
        }, 1, TimeUnit.SECONDS);
    }

    public void notifyRemove() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public void updateSigns() {
        for (String s : this.flags.keySet()) {
            updateSigns(s);
        }
    }

    private void updateSigns(String fname) {
        if (!RedProtect.get().config.configRoot().region_settings.enable_flag_sign) {
            return;
        }
        List<Location> locs = RedProtect.get().config.getSigns(this.getID());
        if (locs.size() > 0) {
            for (Location loc : locs) {
                if (loc.getTileEntity().isPresent() && loc.getTileEntity().get() instanceof Sign) {
                    Sign s = (Sign) loc.getTileEntity().get();
                    ListValue<Text> lines = s.lines();
                    if (lines.get(0).toPlain().equalsIgnoreCase("[flag]")) {
                        if (lines.get(1).toPlain().equalsIgnoreCase(fname) && this.name.equalsIgnoreCase(lines.get(2).toPlain())) {
                            lines.set(3, RedProtectUtil.toText(RedProtect.get().lang.get("region.value") + " " + RedProtect.get().lang.translBool(getFlagString(fname))));
                            s.offer(lines);
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

    public boolean setFlag(Cause cause, String fname, Object value) {
        ChangeRegionFlagEvent event = new ChangeRegionFlagEvent(cause, this, fname, value);
        if (Sponge.getEventManager().post(event)) return false;

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

    public Location<World> getTPPoint() {
        if (this.tppoint == null) return null;
        return new Location<>(Sponge.getServer().getWorld(this.world).get(), this.tppoint[0], this.tppoint[1], this.tppoint[2]);
    }

    public void setTPPoint(@Nullable Location<World> loc) {
        setToSave(true);
        if (loc != null) {
            this.tppoint = new int[]{loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()};
            double x = loc.getX();
            double y = loc.getY();
            double z = loc.getZ();
            String pos = loc.getPosition().toString();
            RedProtect.get().rm.updateLiveRegion(this, "tppoint", x + "," + y + "," + z + "," + pos);
        } else {
            this.tppoint = null;
            RedProtect.get().rm.updateLiveRegion(this, "tppoint", "");
        }
    }

    public String getFlagStrings() {
        StringBuilder flags = new StringBuilder();
        for (String flag : this.flags.keySet()) {
            flags.append(",").append(flag).append(":").append(this.flags.get(flag).toString());
        }
        return flags.toString().substring(1);
    }

    public void setDate(String value) {
        setToSave(true);
        this.date = value;
        RedProtect.get().rm.updateLiveRegion(this, "date", value);
    }

    public void setMaxY(int y) {
        setToSave(true);
        this.maxY = y;
        RedProtect.get().rm.updateLiveRegion(this, "maxy", y);
    }

    public void setMinY(int y) {
        setToSave(true);
        this.minY = y;
        RedProtect.get().rm.updateLiveRegion(this, "miny", y);
    }

    public Location<World> getMaxLocation() {
        return new Location<>(Sponge.getServer().getWorld(this.world).get(), this.maxMbrX, this.maxY, this.maxMbrZ);
    }

    public Location<World> getMinLocation() {
        return new Location<>(Sponge.getServer().getWorld(this.world).get(), this.minMbrX, this.minY, this.minMbrZ);
    }

    public void setWorld(String w) {
        setToSave(true);
        this.world = w;
        RedProtect.get().rm.updateLiveRegion(this, "world", w);
    }

    public void setPrior(int prior) {
        setToSave(true);
        this.prior = prior;
        RedProtect.get().rm.updateLiveRegion(this, "prior", prior);
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

    public Text info() {
        final StringBuilder leaderStringBuilder = new StringBuilder();
        final StringBuilder adminStringBuilder = new StringBuilder();
        final StringBuilder memberStringBuilder = new StringBuilder();
        String leaderString = "None";
        String adminString = "None";
        String memberString = "None";

        String wMsgTemp;
        String IsTops = RedProtect.get().lang.translBool(isOnTop());
        String today;
        String wName = this.world;
        String colorChar = "";
        String dynmapInfo = "";

        if (RedProtect.get().config.configRoot().region_settings.world_colors.containsKey(this.world)) {
            colorChar = RedProtect.get().config.configRoot().region_settings.world_colors.get(this.world);
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
            today = RedProtect.get().lang.get("region.today");
        } else {
            today = this.date;
        }
        for (PlayerRegion pname : this.leaders) {
            Optional<Player> play = Sponge.getServer().getPlayer(pname.getPlayerName());
            if (play.isPresent() && !pname.getPlayerName().equalsIgnoreCase(RedProtect.get().config.configRoot().region_settings.default_leader)) {
                today = "&aOnline!";
                break;
            }
        }
        for (PlayerRegion pname : this.admins) {
            Optional<Player> play = Sponge.getServer().getPlayer(pname.getPlayerName());
            if (play.isPresent() && !pname.getPlayerName().equalsIgnoreCase(RedProtect.get().config.configRoot().region_settings.default_leader)) {
                today = "&aOnline!";
                break;
            }
        }

        if (RedProtect.get().hooks.Dyn && RedProtect.get().config.configRoot().hooks.dynmap.enable) {
            dynmapInfo = RedProtect.get().lang.get("region.dynmap") + " " + (this.getFlagBool("dynmap") ? RedProtect.get().lang.get("region.dynmap-showing") : RedProtect.get().lang.get("region.dynmap-hiding")) + ", " + RedProtect.get().lang.get("region.dynmap-set") + " " + this.getDynmapSet() + "\n";
        }

        return RedProtectUtil.toText(RedProtect.get().lang.get("region.name") + " " + colorChar + this.name + RedProtect.get().lang.get("general.color") + " | " + RedProtect.get().lang.get("region.priority") + " " + this.prior + "\n" +
                RedProtect.get().lang.get("region.priority.top") + " " + IsTops + RedProtect.get().lang.get("general.color") + " | " + RedProtect.get().lang.get("region.lastvalue") + " " + EconomyManager.getFormatted(this.value) + "\n" +
                RedProtect.get().lang.get("region.world") + " " + colorChar + wName + RedProtect.get().lang.get("general.color") + " | " + RedProtect.get().lang.get("region.center") + " " + this.getCenterX() + ", " + this.getCenterZ() + "\n" +
                RedProtect.get().lang.get("region.ysize") + " " + this.minY + " - " + this.maxY + RedProtect.get().lang.get("general.color") + " | " + RedProtect.get().lang.get("region.area") + " " + this.getArea() + "\n" +
                RedProtect.get().lang.get("region.leaders") + " " + leaderString + "\n" +
                RedProtect.get().lang.get("region.admins") + " " + adminString + RedProtect.get().lang.get("general.color") + " | " + RedProtect.get().lang.get("region.members") + " " + memberString + "\n" +
                RedProtect.get().lang.get("region.date") + " " + today + "\n" +
                dynmapInfo +
                RedProtect.get().lang.get("region.welcome.msg") + " " + wMsgTemp);
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
        if (!flagExists(key)) {
            if (RedProtect.get().config.getDefFlagsValues().get(key) != null) {
                return (Boolean) RedProtect.get().config.getDefFlagsValues().get(key);
            } else {
                return (Boolean) RedProtect.get().config.configRoot().flags.getOrDefault(key, false);
            }
        }
        return this.flags.get(key) instanceof Boolean && (Boolean) this.flags.get(key);
    }

    public String getFlagString(String key) {
        if (!flagExists(key)) {
            if (RedProtect.get().config.getDefFlagsValues().get(key) != null) {
                return (String) RedProtect.get().config.getDefFlagsValues().get(key);
            } else {
                return RedProtect.get().config.configRoot().flags.get(key).toString();
            }
        }
        return this.flags.get(key).toString();
    }

    public Text getFlagInfo() {
        StringBuilder flaginfo = new StringBuilder();
        for (String flag : this.flags.keySet()) {
            if (RedProtect.get().config.getDefFlags().contains(flag)) {
                String flagValue = this.flags.get(flag).toString();
                if (flagValue.equalsIgnoreCase("true") || flagValue.equalsIgnoreCase("false")) {
                    flaginfo.append(", &b").append(flag).append(": ").append(RedProtect.get().lang.translBool(flagValue));
                } else {
                    flaginfo.append(", &b").append(flag).append(": &8").append(flagValue);
                }
            }

            if (flaginfo.toString().contains(flag)) {
                continue;
            }

            if (RedProtect.get().config.AdminFlags.contains(flag)) {
                String flagValue = this.flags.get(flag).toString();
                if (flagValue.equalsIgnoreCase("true") || flagValue.equalsIgnoreCase("false")) {
                    flaginfo.append(", &b").append(flag).append(": ").append(RedProtect.get().lang.translBool(flagValue));
                } else {
                    flaginfo.append(", &b").append(flag).append(": &8").append(flagValue);
                }
            }
        }
        if (this.flags.keySet().size() > 0) {
            flaginfo = new StringBuilder(flaginfo.substring(2));
        } else {
            flaginfo = new StringBuilder("Default");
        }
        return RedProtectUtil.toText(flaginfo.toString());
    }

    public boolean isOnTop() {
        Region newr = RedProtect.get().rm.getTopRegion(RedProtect.get().getServer().getWorld(this.getWorld()).get(), this.getCenterX(), this.getCenterY(), this.getCenterZ(), this.getClass().getName());
        return newr == null || newr.equals(this);
    }

    //---------------------- Admin Flags --------------------------//

    public boolean canPickup(Player p) {
        return !flagExists("can-pickup") || getFlagBool("can-pickup") || checkAllowedPlayer(p);
    }

    public boolean canDrop(Player p) {
        return !flagExists("can-drop") || getFlagBool("can-drop") || checkAllowedPlayer(p);
    }

    public boolean canSpawnWhiter() {
        return !flagExists("spawn-wither") || getFlagBool("spawn-wither");
    }

    public int maxPlayers() {
        if (!flagExists("max-players")) {
            return -1;
        }
        return new Integer(getFlagString("max-players"));
    }

    public boolean canDeath() {
        return !flagExists("can-death") || getFlagBool("can-death");
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
            if (p.get(Keys.HEALTH).get() <= health && !waiting) {
                Sponge.getGame().getCommandManager().process(RedProtect.get().getServer().getConsole(), cmd.replace("{player}", p.getName()));
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

    public boolean canPlayerDamage() {
        return !flagExists("player-damage") || getFlagBool("player-damage");
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

        List<String> items = Arrays.asList(flags.get("deny-exit-items").toString().replace(" ", "").split(","));
        Iterable<Slot> SlotItems = p.getInventory().slots();

        for (Slot slot : SlotItems) {
            if (slot.peek().isPresent()) {
                if (items.stream().anyMatch(k -> Sponge.getRegistry().getType(ItemType.class, k).orElse(null) == slot.peek().get().getItem())) {
                    return false;
                }
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

        List<String> items = Arrays.asList(flags.get("allow-enter-items").toString().replace(" ", "").split(","));
        Iterable<Slot> SlotItems = p.getInventory().slots();
        for (Slot slot : SlotItems) {
            if (slot.peek().isPresent()) {
                if (items.stream().anyMatch(k -> Sponge.getRegistry().getType(ItemType.class, k).orElse(null) == slot.peek().get().getItem())) {
                    return true;
                }
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

        Iterable<Slot> SlotItems = p.getInventory().slots();
        List<String> items = Arrays.asList(flags.get("deny-enter-items").toString().replace(" ", "").split(","));

        for (Slot slot : SlotItems) {
            if (slot.peek().isPresent()) {
                if (items.stream().anyMatch(k -> Sponge.getRegistry().getType(ItemType.class, k).orElse(null) == slot.peek().get().getItem())) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean canCrops(BlockSnapshot b) {
        if (!flagExists("cropsfarm")) {
            return false;
        }
        return (b.getState().getType().equals(BlockTypes.WHEAT) || b.getState().getType().equals(BlockTypes.POTATOES) || b.getState().getType().equals(BlockTypes.CARROTS) || b.getState().getType().equals(BlockTypes.PUMPKIN_STEM) || b.getState().getType().equals(BlockTypes.MELON_STEM) || b.getState().getType().getName().contains("CHORUS_") || b.getState().getType().getName().contains("BEETROOT_BLOCK") || b.getState().getType().getName().contains("SUGAR_CANE")) && getFlagBool("cropsfarm");
    }

    public boolean canMining(BlockSnapshot b) {
        return flagExists("minefarm") && (b.getState().getType().getName().contains("_ORE") || b.getState().getType().equals(BlockTypes.STONE) || b.getState().getType().equals(BlockTypes.GRASS) || b.getState().getType().equals(BlockTypes.DIRT)) && getFlagBool("minefarm");
    }

    public boolean canPlace(BlockSnapshot b) {
        if (!flagExists("allow-place")) {
            return false;
        }

        String[] blocks = getFlagString("allow-place").replace(" ", "").split(",");
        for (String block : blocks) {
            if (block.equalsIgnoreCase(b.getState().getType().getName())) {
                return true;
            }
        }
        return false;
    }

    public boolean canPlace(EntityType ent) {
        if (!flagExists("allow-place")) {
            return false;
        }

        String[] blocks = getFlagString("allow-place").replace(" ", "").split(",");
        for (String block : blocks) {
            if (block.equalsIgnoreCase(ent.getName())) {
                return true;
            }
        }
        return false;
    }

    public boolean canBreak(BlockSnapshot b) {
        if (!flagExists("allow-break")) {
            return false;
        }
        String[] blocks = getFlagString("allow-break").replace(" ", "").split(",");
        for (String block : blocks) {
            if (block.equalsIgnoreCase(b.getState().getType().getName())) {
                return true;
            }
        }
        return false;
    }

    public boolean canBreak(EntityType ent) {
        if (!flagExists("allow-break")) {
            return false;
        }
        String[] blocks = getFlagString("allow-break").replace(" ", "").split(",");
        for (String block : blocks) {
            if (block.equalsIgnoreCase(ent.getName())) {
                return true;
            }
        }
        return false;
    }

    public boolean canTree(BlockSnapshot b) {
        return flagExists("treefarm") && (b.getState().getType().getName().contains("log") || b.getState().getType().getName().contains("leaves")) && getFlagBool("treefarm");
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
        String[] flagCmds = flags.get("allow-cmds").toString().split(",");
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
        String[] flagCmds = flags.get("deny-cmds").toString().split(",");
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
    public boolean allowPressPlate(Player p) {
        return getFlagBool("press-plate") || checkAllowedPlayer(p);
    }

    public boolean canBuild(Player p) {
        if (flagExists("for-sale") && !RedProtect.get().ph.hasPerm(p, "redprotect.bypass")) {
            return false;
        }
        return getFlagBool("build") || checkAllowedPlayer(p);
    }

    public boolean leavesDecay() {
        return getFlagBool("leaves-decay");
    }

    /**
     * Allow non members of this region to break/place spawners.
     *
     * @return boolean
     */
    public boolean allowSpawner(Player p) {
        return getFlagBool("allow-spawner") || checkAllowedPlayer(p);
    }

    public boolean canTeleport(Player p) {
        return checkAllowedPlayer(p) || getFlagBool("teleport");
    }

    /**
     * Allow players with fly enabled fly on this region.
     *
     * @return boolean
     */
    public boolean canFly(Player p) {
        return getFlagBool("allow-fly") || checkAllowedPlayer(p);
    }

    public boolean FlowDamage() {
        return getFlagBool("flow-damage");
    }

    public boolean canMobLoot() {
        return getFlagBool("mob-loot");
    }

    public boolean allowEffects(Player p) {
        return getFlagBool("allow-effects") || checkAllowedPlayer(p);
    }

    public boolean usePotions(Player p) {
        return getFlagBool("use-potions") || checkAllowedPlayer(p);
    }

    public boolean canPVP(Player p) {
        return getFlagBool("pvp") || RedProtect.get().ph.hasPerm(p, "redprotect.bypass");
    }

    public boolean canChest(Player p) {
        return getFlagBool("chest") || checkAllowedPlayer(p);
    }

    public boolean canLever(Player p) {
        return getFlagBool("lever") || checkAllowedPlayer(p);
    }

    public boolean canButton(Player p) {
        return getFlagBool("button") || checkAllowedPlayer(p);
    }

    public boolean canDoor(Player p) {
        return getFlagBool("door") || checkAllowedPlayer(p);
    }

    public boolean canSpawnMonsters() {
        return getFlagBool("spawn-monsters");
    }

    public boolean canSpawnPassives() {
        return getFlagBool("spawn-animals");
    }

    public boolean canMinecart(Player p) {
        return getFlagBool("minecart") || checkAllowedPlayer(p);
    }

    public boolean canInteractPassives(Player p) {
        return getFlagBool("passives") || checkAllowedPlayer(p);
    }

    public boolean canFlow() {
        return getFlagBool("flow");
    }

    public boolean canFire() {
        return getFlagBool("fire");
    }

    public boolean AllowHome(Player p) {
        return getFlagBool("allow-home") || checkAllowedPlayer(p);
    }

    public boolean canGrow() {
        return getFlagBool("can-grow");
    }
    //--------------------------------------------------------------//

    public void setValue(long value) {
        setToSave(true);
        RedProtect.get().rm.updateLiveRegion(this, "value", value);
        this.value = value;
    }

    private boolean checkAllowedPlayer(Player p) {
        return this.isLeader(p) || this.isAdmin(p) || this.isMember(p) || RedProtect.get().ph.hasPerm(p, "redprotect.command.admin");
    }

    public List<Location<World>> getLimitLocs(int miny, int maxy, boolean define) {
        final List<Location<World>> locBlocks = new ArrayList<>();
        Location<World> loc1 = this.getMinLocation();
        Location<World> loc2 = this.getMaxLocation();
        World w = Sponge.getServer().getWorld(this.getWorld()).get();

        for (int x = loc1.getBlockX(); x <= loc2.getBlockX(); ++x) {
            for (int z = loc1.getBlockZ(); z <= loc2.getBlockZ(); ++z) {
                for (int y = miny; y <= maxy; ++y) {
                    if ((z == loc1.getBlockZ() || z == loc2.getBlockZ() ||
                            x == loc1.getBlockX() || x == loc2.getBlockX())
                            && (define || new Location<>(w, x, y, z).getBlock().getType().getName().contains(RedProtect.get().config.configRoot().region_settings.block_id))) {
                        locBlocks.add(new Location<>(w, x, y, z));
                    }
                }
            }
        }
        return locBlocks;
    }

    public List<Location<World>> get4Points(int y) {
        List<Location<World>> locs = new ArrayList<>();
        locs.add(this.getMinLocation());
        locs.add(new Location<>(this.getMinLocation().getExtent(), this.minMbrX, y, this.minMbrZ + (this.maxMbrZ - this.minMbrZ)));
        locs.add(this.getMaxLocation());
        locs.add(new Location<>(this.getMinLocation().getExtent(), this.minMbrX + (this.maxMbrX - this.minMbrX), y, this.minMbrZ));
        return locs;
    }

    public Location<World> getCenterLoc() {
        return new Location<>(Sponge.getServer().getWorld(this.world).get(), this.getCenterX(), this.getCenterY(), this.getCenterZ());
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
