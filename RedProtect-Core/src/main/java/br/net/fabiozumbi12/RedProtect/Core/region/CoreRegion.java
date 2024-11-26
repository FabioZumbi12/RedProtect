/*
 * Copyright (c) 2012-2024 - @FabioZumbi12
 * Last Modified: 26/11/2024 18:02
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

package br.net.fabiozumbi12.RedProtect.Core.region;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

public class CoreRegion implements Serializable {

    @Serial
    private static final long serialVersionUID = 2861198224185302015L;
    protected int minMbrX;
    protected int maxMbrX;
    protected int minMbrZ;
    protected int maxMbrZ;
    protected int minY;
    protected int maxY;
    protected int prior;
    protected final String name;
    protected Set<PlayerRegion> leaders;
    protected Set<PlayerRegion> admins;
    protected Set<PlayerRegion> members;
    protected String wMessage;
    protected String world;
    protected String date;
    protected Map<String, Object> flags;
    protected double value;
    protected int[] tppoint;
    protected float[] tppointYaw;
    protected final boolean canDelete;
    protected boolean canPurge;
    private boolean tosave = true;

    /**
     * Represents the region created by player.
     *
     * @param name       Name of region.
     * @param admins     List of admins.
     * @param members    List of members.
     * @param leaders    List of leaders.
     * @param minLoc     Min coord.
     * @param maxLoc     Max coord.
     * @param flags      Flag names and values.
     * @param wMessage   Welcome message.
     * @param prior      Priority of region.
     * @param worldName  Name of world for this region.
     * @param date       Date of latest visit of an admin or leader.
     * @param value      Last value of this region.
     * @param tppoint    Teleport Point
     * @param tppointYaw Teleport Pitch and Yam
     */
    public CoreRegion(String name, Set<PlayerRegion> admins, Set<PlayerRegion> members, Set<PlayerRegion> leaders, int[] minLoc, int[] maxLoc, Map<String, Object> flags, String wMessage, int prior, String worldName, String date, double value, int[] tppoint, float[] tppointYaw, boolean candel, boolean canPurge) {
        this.name = name;
        this.maxMbrX = maxLoc[0];
        this.minMbrX = minLoc[0];
        this.maxMbrZ = maxLoc[2];
        this.minMbrZ = minLoc[2];
        this.maxY = maxLoc[1];
        this.minY = minLoc[1];
        this.admins = admins;
        this.members = members;
        this.leaders = leaders;
        this.flags = flags;
        this.value = value;
        this.prior = prior;
        this.tppoint = tppoint;
        this.tppointYaw = tppointYaw;
        this.canDelete = candel;
        this.canPurge = canPurge;
        this.date = date;

        this.world = Objects.requireNonNullElse(worldName, "");

        this.wMessage = Objects.requireNonNullElse(wMessage, "");
    }

    /**
     * Represents the region created by player.
     *
     * @param name       Name of region.
     * @param admins     List of admins.
     * @param members    List of members.
     * @param leaders    List of leaders.
     * @param maxMbrX    Max coord X
     * @param minMbrX    Min coord X
     * @param maxMbrZ    Max coord Z
     * @param minMbrZ    Min coord Z
     * @param flags      Flag names and values.
     * @param wMessage   Welcome message.
     * @param prior      Priority of region.
     * @param worldName  Name of world for this region.
     * @param date       Date of latest visit of an admin or leader.
     * @param value      Last value of this region.
     * @param tppoint    Teleport Point
     * @param tppointYaw Teleport Pitch and Yam
     */
    public CoreRegion(String name, Set<PlayerRegion> admins, Set<PlayerRegion> members, Set<PlayerRegion> leaders, int maxMbrX, int minMbrX, int maxMbrZ, int minMbrZ, int minY, int maxY, Map<String, Object> flags, String wMessage, int prior, String worldName, String date, double value, int[] tppoint, float[] tppointYaw, boolean candel, boolean canPurge) {
        this.name = name;
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
        this.tppointYaw = tppointYaw;
        this.canDelete = candel;
        this.canPurge = canPurge;
        this.date = date;
        this.prior = prior;

        this.world = Objects.requireNonNullElse(worldName, "");

        this.wMessage = Objects.requireNonNullElse(wMessage, "");
    }

    /**
     * Represents the region created by player.
     *
     * @param name       Region name.
     * @param admins     Admins names/uuids.
     * @param members    Members names/uuids.
     * @param leaders    Leaders name/uuid.
     * @param x          Locations of x coords.
     * @param z          Locations of z coords.
     * @param miny       Min coord y of this region.
     * @param maxy       Max coord y of this region.
     * @param prior      Location of x coords.
     * @param worldName  Name of world region.
     * @param date       Date of latest visit of an admins or leader.
     * @param welcome    Set a welcome message.
     * @param value      A value in server economy.
     * @param tppoint    Teleport Point
     * @param tppointYaw Teleport Pitch and Yam
     */
    public CoreRegion(String name, Set<PlayerRegion> admins, Set<PlayerRegion> members, Set<PlayerRegion> leaders, int[] x, int[] z, int miny, int maxy, int prior, String worldName, String date, Map<String, Object> flags, String welcome, double value, int[] tppoint, float[] tppointYaw, boolean candel, boolean canPurge) {
        this.name = name;
        this.prior = prior;
        this.world = worldName;
        this.date = date;
        this.flags = flags;
        this.wMessage = welcome;
        this.value = value;
        this.tppoint = tppoint;
        this.tppointYaw = tppointYaw;
        this.canDelete = candel;
        this.canPurge = canPurge;

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
    }

    /**
     * Represents the region created by player.
     *
     * @param name  Region name.
     * @param min   Min Location.
     * @param max   Max Location.
     * @param world World name.
     */
    public CoreRegion(String name, int[] min, int[] max, String world, String defLeader, Map<String, Object> defFlags) {
        this.name = name;
        this.maxMbrX = max[0];
        this.minMbrX = min[0];
        this.maxMbrZ = max[2];
        this.minMbrZ = min[2];
        this.maxY = max[1];
        this.minY = min[1];
        this.admins = new HashSet<>();
        this.members = new HashSet<>();
        this.leaders = Collections.singleton(new PlayerRegion(defLeader, defLeader));
        this.flags = defFlags;
        this.canDelete = true;
        this.canPurge = true;
        this.world = world;
        this.wMessage = "";
    }

    public Map<String, Object> getFlags() {
        return flags;
    }

    public void setFlags(Map<String, Object> flags) {
        this.flags = flags;
    }

    public boolean canPurge() {
        return this.canPurge;
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

    public String getFlagStrings() {
        StringBuilder flags = new StringBuilder();
        for (String flag : this.flags.keySet()) {
            flags.append(",").append(flag).append(":").append(this.flags.get(flag).toString());
        }
        return flags.substring(1);
    }

    public String getTPPointString() {
        if (tppoint == null) {
            return "";
        }
        return this.tppoint[0] + "," + this.tppoint[1] + "," + this.tppoint[2] + "," + this.tppointYaw[0] + "," + this.tppointYaw[0];
    }

    public String getDate() {
        return this.date;
    }

    public int getMaxY() {
        return this.maxY;
    }

    public int getMinY() {
        return this.minY;
    }

    public String getWorld() {
        return this.world;
    }

    public int getPrior() {
        return this.prior;
    }

    public String getWelcome() {
        if (wMessage == null) {
            return "";
        }
        return this.wMessage;
    }

    public String getName() {
        return this.name;
    }

    /**
     * Use this method to get raw admins. This will return UUID if server running in Online mode. Will return player name in lowercase if Offline mode.
     * <p>
     * To check if a player can build on this region use {@code canBuild(p)} instead this method.
     *
     * @return {@code Set<PlayerRegion<String, String>>}
     */
    public Set<PlayerRegion> getAdmins() {
        return this.admins;
    }

    /**
     * Use this method to get raw members. This will return UUID if server running in Online mode. Will return player name in lowercase if Offline mode.
     * <p>
     * To check if a player can build on this region use {@code canBuild(Player p)} instead this method.
     *
     * @return {@code Set<PlayerRegion<String, String>>}
     */
    public Set<PlayerRegion> getMembers() {
        return this.members;
    }

    /**
     * Use this method to get raw leaders. This will return UUID if server running in Online mode. Will return player name in lowercase if Offline mode.
     * <p>
     * To check if a player can build on this region use {@code canBuild(Player p)} instead this method.
     *
     * @return {@code Set<PlayerRegion<String, String>>}
     */
    public Set<PlayerRegion> getLeaders() {
        return this.leaders;
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

    public int getArea() {
        int areaX = (Math.abs(this.maxMbrX - this.minMbrX) + 1);
        int areaZ = (Math.abs(this.maxMbrZ - this.minMbrZ) + 1);
        return areaX * areaZ;
    }

    public boolean isLeaderByUUID(String player) {
        return this.leaders.stream().anyMatch(l -> l.getUUID().equalsIgnoreCase(player));
    }

    public boolean isAdminByUUID(String player) {
        return this.admins.stream().anyMatch(l -> l.getUUID().equalsIgnoreCase(player));
    }

    public boolean isMemberByUUID(String player) {
        return this.members.stream().anyMatch(l -> l.getUUID().equalsIgnoreCase(player));
    }

    public boolean isLeaderByName(String player) {
        return this.leaders.stream().anyMatch(l -> l.getPlayerName().equalsIgnoreCase(player));
    }

    public boolean isAdminByName(String player) {
        return this.admins.stream().anyMatch(l -> l.getPlayerName().equalsIgnoreCase(player));
    }

    public boolean isMemberByName(String player) {
        return this.members.stream().anyMatch(l -> l.getPlayerName().equalsIgnoreCase(player));
    }

    public boolean isLeader(String player) {
        return this.leaders.stream().anyMatch(l -> l.getUUID().equalsIgnoreCase(player)) ||
                this.leaders.stream().anyMatch(l -> l.getPlayerName().equalsIgnoreCase(player));
    }

    public boolean isAdmin(String player) {
        return this.admins.stream().anyMatch(l -> l.getUUID().equalsIgnoreCase(player)) ||
                this.admins.stream().anyMatch(l -> l.getPlayerName().equalsIgnoreCase(player));
    }

    public boolean isMember(String player) {
        return this.members.stream().anyMatch(l -> l.getUUID().equalsIgnoreCase(player)) ||
                this.members.stream().anyMatch(l -> l.getPlayerName().equalsIgnoreCase(player));
    }

    public int adminSize() {
        return this.admins.size();
    }

    public int leaderSize() {
        return this.leaders.size();
    }

    public boolean flagExists(String key) {
        return flags.containsKey(key);
    }

    public double getValue() {
        return this.value;
    }

    protected String serializeMembers(Set<PlayerRegion> pairs) {
        StringBuilder list = new StringBuilder();
        pairs.forEach(l -> list.append(",").append(l.getUUID()).append("@").append(l.getPlayerName()));
        return !list.isEmpty() ? list.substring(1) : "";
    }

    public boolean sameLeaders(CoreRegion r) {
        for (PlayerRegion l : this.leaders) {
            if (r.leaders.contains(l)) {
                return true;
            }
        }
        return false;
    }

    public String getLeadersString() {
        return serializeMembers(this.leaders);
    }

    public String getAdminString() {
        return serializeMembers(this.admins);
    }

    public String getMembersString() {
        return serializeMembers(this.members);
    }
}
