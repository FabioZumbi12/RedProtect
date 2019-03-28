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

package br.net.fabiozumbi12.RedProtect.Bukkit.database;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.region.Region;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPConfig;
import br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPUtil;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class WorldFlatFileRegionManager implements WorldRegionManager {

    private final HashMap<String, Region> regionsMap = new HashMap<>();
    //private final Map<Chunk, Set<Region>> chunksMap = new HashMap<>();
    private final World world;
    private final String pathData = RedProtect.get().getDataFolder() + File.separator + "data" + File.separator;

    public WorldFlatFileRegionManager(World world) {
        super();
        this.world = world;
    }

    @Override
    public void add(Region region) {
        // Add to name-region map
        regionsMap.put(region.getName(), region);
/*
        // Add to chunk-set<region> map
        region.getOccupiedChunks().forEach(chunk -> chunksMap
                .computeIfAbsent(chunk, k -> new HashSet<>())
                .add(region)
        );*/
    }

    @Override
    public void remove(Region region) {
        if (regionsMap.containsValue(region)) {
            regionsMap.remove(region.getName());
        }/*
        region.getOccupiedChunks().forEach(chunk -> chunksMap
                .computeIfAbsent(chunk, k -> new HashSet<>())
                .remove(region)
        );*/
    }

    @Override
    public Set<Region> getRegions(String pname) {
        SortedSet<Region> regionsp = new TreeSet<>(Comparator.comparing(Region::getName));
        for (Region r : regionsMap.values()) {
            if (r.isLeader(pname)) {
                regionsp.add(r);
            }
        }
        return regionsp;
    }

    @Override
    public Set<Region> getMemberRegions(String uuid) {
        SortedSet<Region> regionsp = new TreeSet<>(Comparator.comparing(Region::getName));
        for (Region r : regionsMap.values()) {
            if (r.isLeader(uuid) || r.isAdmin(uuid)) {
                regionsp.add(r);
            }
        }
        return regionsp;
    }

    @Override
    public Region getRegion(String rname) {
        return regionsMap.get(rname);
    }

    @Override
    public int save(boolean force) {
        int saved = 0;
        try {
            RedProtect.get().logger.debug("RegionManager.Save(): File type is " + RPConfig.getString("file-type"));
            String world = this.getWorld().getName();
            File datf;

            if (RPConfig.getString("file-type").equals("yml")) {
                datf = new File(pathData, "data_" + world + ".yml");
                YamlConfiguration fileDB = new YamlConfiguration();
                Set<YamlConfiguration> yamls = new HashSet<>();
                for (Region r : regionsMap.values()) {
                    if (r.getName() == null) {
                        continue;
                    }

                    if (RPConfig.getBool("flat-file.region-per-file")) {
                        if (!r.toSave() && !force) {
                            continue;
                        }
                        fileDB = new YamlConfiguration();
                        datf = new File(pathData, world + File.separator + r.getName() + ".yml");
                    }

                    RPUtil.addProps(fileDB, r);
                    saved++;

                    if (RPConfig.getBool("flat-file.region-per-file")) {
                        yamls.add(fileDB);
                        saveYaml(fileDB, datf);
                        r.setToSave(false);
                    }
                }

                if (!RPConfig.getBool("flat-file.region-per-file")) {
                    saveYaml(fileDB, datf);
                } else {
                    //remove deleted regions
                    File wfolder = new File(pathData + world);
                    if (wfolder.exists()) {
                        File[] listOfFiles = new File(pathData, world).listFiles();
                        if (listOfFiles != null) {
                            for (File region : listOfFiles) {
                                if (region.isFile() && !regionsMap.containsKey(region.getName().replace(".yml", ""))) {
                                    region.delete();
                                }
                            }
                        }
                    }
                }

                if (force) RedProtect.get().logger.info("Saving " + this.world.getName() + "'s regions...");

                //try backup
                if (force && RPConfig.getBool("flat-file.backup")){
                    if (!RPConfig.getBool("flat-file.region-per-file")) {
                        RPUtil.backupRegions(Collections.singleton(fileDB), world, "data_" + world + ".yml");
                    } else {
                        RPUtil.backupRegions(yamls, world, null);
                    }
                }
            }
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        return saved;
    }

    private void saveYaml(YamlConfiguration fileDB, File file) {
        try {
            fileDB.save(file);
        } catch (IOException e) {
            RedProtect.get().logger.severe("Error during save database file for world " + world + ": ");
            e.printStackTrace();
        }
    }

    @Override
    public int getTotalRegionSize(String uuid) {
        Set<Region> regionslist = new HashSet<>();
        for (Region r : regionsMap.values()) {
            if (r.isLeader(uuid)) {
                regionslist.add(r);
            }
        }
        int total = 0;
        for (Region r2 : regionslist) {
            total += RPUtil.simuleTotalRegionSize(uuid, r2);
        }
        return total;
    }

    @Override
    public void load() {
        try {
            String world = this.getWorld().getName();
            RedProtect.get().logger.info("- Loading " + world + "'s regions...");

            if (RPConfig.getString("file-type").equals("yml")) {
                if (RPConfig.getBool("flat-file.region-per-file")) {
                    File f = new File(pathData + world);
                    if (!f.exists()) {
                        f.mkdir();
                    }
                    File[] listOfFiles = f.listFiles();
                    for (File region : listOfFiles) {
                        if (region.getName().endsWith(".yml")) {
                            this.load(region.getPath());
                        }
                    }
                } else {
                    File oldf = new File(pathData + world + ".yml");
                    File newf = new File(pathData + "data_" + world + ".yml");
                    if (oldf.exists()) {
                        oldf.renameTo(newf);
                    }
                    this.load(pathData + "data_" + world + ".yml");
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void load(String path) {
        File f = new File(path);
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (RPConfig.getString("file-type").equals("yml")) {
            YamlConfiguration fileDB = new YamlConfiguration();
            RedProtect.get().logger.debug("Load world " + this.world.getName() + ". File type: yml");
            try {
                fileDB.load(f);
            } catch (FileNotFoundException e) {
                RedProtect.get().logger.severe("DB file not found!");
                RedProtect.get().logger.severe("File:" + f.getName());
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            for (String rname : fileDB.getKeys(false)) {
                Region newr = RPUtil.loadProps(fileDB, rname, this.world);
                if (newr == null) return;

                newr.setToSave(false);
                add(newr);
            }
        }
    }

    @Override
    public Set<Region> getRegionsNear(Player player, int radius) {
        int px = player.getLocation().getBlockX();
        int pz = player.getLocation().getBlockZ();
        SortedSet<Region> ret = new TreeSet<>(Comparator.comparing(Region::getName));

        for (Region r : regionsMap.values()) {
            RedProtect.get().logger.debug("Radius: " + radius);
            RedProtect.get().logger.debug("X radius: " + Math.abs(r.getCenterX() - px) + " - Z radius: " + Math.abs(r.getCenterZ() - pz));
            if (Math.abs(r.getCenterX() - px) <= radius && Math.abs(r.getCenterZ() - pz) <= radius) {
                ret.add(r);
            }
        }
        return ret;
    }
/*
    @Override
    public Set<Region> getRegionsInChunk(Chunk chunk) {
        return chunksMap.getOrDefault(chunk, new HashSet<>());
    }

    @Override
    public boolean regionExists(Region region) {
    	if (regionsMap.containsValue(region)){
			return true;
		}
		return false;
    }
    */

    public World getWorld() {
        return this.world;
    }

    @Override
    public Set<Region> getInnerRegions(Region region) {
        Set<Region> regionl = new HashSet<>();
        regionsMap.values().forEach(r -> {
            if (r.getMaxMbrX() <= region.getMaxMbrX() &&
                        r.getMaxY() <= region.getMaxY() &&
                        r.getMaxMbrZ() <= region.getMaxMbrZ() &&
                        r.getMinMbrX() >= region.getMinMbrX() &&
                        r.getMinY() >= region.getMinY() &&
                        r.getMinMbrZ() >= region.getMinMbrZ()) {
                    regionl.add(r);
            }
        });
        return regionl;
    }

    @Override
    public Set<Region> getRegions(int x, int y, int z) {
        Set<Region> regionl = new HashSet<>();
        regionsMap.values().forEach(r -> {
            if (x <= r.getMaxMbrX() &&
                    x >= r.getMinMbrX() &&
                    y <= r.getMaxY() &&
                    y >= r.getMinY() &&
                    z <= r.getMaxMbrZ() &&
                    z >= r.getMinMbrZ()) {
                regionl.add(r);
            }
        });
        return regionl;
    }

    @Override
    public Region getTopRegion(int x, int y, int z) {
        Map<Integer, Region> regionlist = new HashMap<>();
        int max = 0;
        for (Region r : regionsMap.values()) {
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
        for (Region r : regionsMap.values()) {
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

    @Override
    public Map<Integer, Region> getGroupRegion(int x, int y, int z) {
        Map<Integer, Region> regionlist = new HashMap<>();
        for (Region r : regionsMap.values()) {
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
        SortedSet<Region> allregions = new TreeSet<>(Comparator.comparing(Region::getName));
        allregions.addAll(regionsMap.values());
        return allregions;
    }

    @Override
    public void clearRegions() {
        regionsMap.clear();
    }

    @Override
    public void updateLiveRegion(String rname, String columm, Object value) {
    }

    @Override
    public void closeConn() {
    }

    @Override
    public int getTotalRegionNum() {
        return regionsMap.size();
    }

    @Override
    public void updateLiveFlags(String rname, String flag, String value) {
    }

    @Override
    public void removeLiveFlags(String rname, String flag) {
    }

}
