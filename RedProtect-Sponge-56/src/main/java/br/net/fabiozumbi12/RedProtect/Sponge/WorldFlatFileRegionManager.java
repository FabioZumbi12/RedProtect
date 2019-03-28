/*
 *
 * Copyright (c) 2019 - @FabioZumbi12
 * Last Modified: 28/03/19 19:14
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
 *
 */

package br.net.fabiozumbi12.RedProtect.Sponge;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.World;

import java.io.File;
import java.io.IOException;
import java.util.*;

class WorldFlatFileRegionManager implements WorldRegionManager {

    final HashMap<String, Region> regions;
    final World world;

    public WorldFlatFileRegionManager(World world) {
        super();
        this.regions = new HashMap<>();
        this.world = world;
    }

    @Override
    public void add(Region r) {
        this.regions.put(r.getName(), r);
    }

    @Override
    public void remove(Region r) {
        this.regions.remove(r.getName());
    }

    @Override
    public Set<Region> getRegions(String pname) {
        SortedSet<Region> regionsp = new TreeSet<>(Comparator.comparing(Region::getName));
        for (Region r : regions.values()) {
            if (r.isLeader(pname)) {
                regionsp.add(r);
            }
        }
        return regionsp;
    }

    @Override
    public Set<Region> getMemberRegions(String uuid) {
        SortedSet<Region> regionsp = new TreeSet<>(Comparator.comparing(Region::getName));
        for (Region r : regions.values()) {
            if (r.isLeader(uuid) || r.isAdmin(uuid)) {
                regionsp.add(r);
            }
        }
        return regionsp;
    }

    @Override
    public Region getRegion(String rname) {
        return regions.get(rname);
    }

    @Override
    public int save(boolean force) {
        int saved = 0;
        try {
            RedProtect.get().logger.debug(LogLevel.DEFAULT, "RegionManager.Save(): File type is " + RedProtect.get().cfgs.root().file_type);
            String world = this.getWorld().getName();

            if (RedProtect.get().cfgs.root().file_type.equalsIgnoreCase("file")) {

                File datf = new File(RedProtect.get().configDir + File.separator + "data", "data_" + world + ".conf");
                ConfigurationLoader<CommentedConfigurationNode> regionManager = HoconConfigurationLoader.builder().setPath(datf.toPath()).build();
                CommentedConfigurationNode fileDB = regionManager.createEmptyNode();
                Set<CommentedConfigurationNode> dbs = new HashSet<>();
                for (Region r : regions.values()) {
                    if (r.getName() == null) {
                        continue;
                    }

                    if (RedProtect.get().cfgs.root().flat_file.region_per_file) {
                        if (!r.toSave() && !force) {
                            continue;
                        }
                        datf = new File(RedProtect.get().configDir + File.separator + "data", world + File.separator + r.getName() + ".conf");
                        regionManager = HoconConfigurationLoader.builder().setPath(datf.toPath()).build();
                        fileDB = regionManager.createEmptyNode();
                    }

                    RPUtil.addProps(fileDB, r);
                    saved++;

                    if (RedProtect.get().cfgs.root().flat_file.region_per_file) {
                        dbs.add(fileDB);
                        saveConf(fileDB, regionManager);
                        r.setToSave(false);
                    }
                }

                if (!RedProtect.get().cfgs.root().flat_file.region_per_file) {
                    saveConf(fileDB, regionManager);
                } else {
                    //remove deleted regions
                    File wfolder = new File(RedProtect.get().configDir + File.separator + "data", world);
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

                if (force) RedProtect.get().logger.info("Saving " + this.world.getName() + "'s regions...");

                //try backup
                if (force && RedProtect.get().cfgs.root().flat_file.backup){
                    if (!RedProtect.get().cfgs.root().flat_file.region_per_file) {
                        RPUtil.backupRegions(Collections.singleton(fileDB), world, "data_" + world + ".conf");
                    } else {
                        RPUtil.backupRegions(dbs, world, null);
                    }
                }
            }
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        return saved;
    }

    private void saveConf(CommentedConfigurationNode fileDB, ConfigurationLoader<CommentedConfigurationNode> regionManager) {
        try {
            regionManager.save(fileDB);
        } catch (IOException e) {
            RedProtect.get().logger.severe("Error during save database file for world " + world + ": ");
            e.printStackTrace();
        }
    }

    @Override
    public int getTotalRegionSize(String uuid) {
        Set<Region> regionslist = new HashSet<>();
        for (Region r : regions.values()) {
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

            if (RedProtect.get().cfgs.root().file_type.equalsIgnoreCase("file")) {
                if (RedProtect.get().cfgs.root().flat_file.region_per_file) {
                    File f = new File(RedProtect.get().configDir + File.separator + "data" + File.separator + world);
                    if (!f.exists()) {
                        f.mkdir();
                    }
                    File[] listOfFiles = f.listFiles();
                    for (File region : listOfFiles) {
                        if (region.getName().endsWith(".conf")) {
                            this.load(region.getPath());
                        }
                    }
                } else {
                    File oldf = new File(RedProtect.get().configDir + File.separator + "data" + File.separator + world + ".conf");
                    File newf = new File(RedProtect.get().configDir + File.separator + "data" + File.separator + "data_" + world + ".conf");
                    if (oldf.exists()) {
                        oldf.renameTo(newf);
                    }
                    this.load(RedProtect.get().configDir + File.separator + "data" + File.separator + "data_" + world + ".conf");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void load(String path) {
        World world = this.getWorld();

        if (RedProtect.get().cfgs.root().file_type.equalsIgnoreCase("file")) {
            RedProtect.get().logger.debug(LogLevel.DEFAULT, "Load world " + this.world.getName() + ". File type: conf");

            try {
                File tempRegionFile = new File(path);
                if (!tempRegionFile.exists()) {
                    tempRegionFile.createNewFile();
                }

                ConfigurationLoader<CommentedConfigurationNode> regionManager = HoconConfigurationLoader.builder().setPath(tempRegionFile.toPath()).build();
                CommentedConfigurationNode region = regionManager.load();

                for (Object key : region.getChildrenMap().keySet()) {
                    String rname = key.toString();
                    if (!region.getNode(rname).hasMapChildren()) {
                        continue;
                    }
                    Region newr = RPUtil.loadRegion(region, rname, world);
                    newr.setToSave(false);
                    regions.put(rname, newr);
                }
            } catch (IOException | ObjectMappingException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Set<Region> getRegionsNear(Player player, int radius) {
        int px = player.getLocation().getBlockX();
        int pz = player.getLocation().getBlockZ();
        SortedSet<Region> ret = new TreeSet<>(Comparator.comparing(Region::getName));

        for (Region r : regions.values()) {
            RedProtect.get().logger.debug(LogLevel.DEFAULT, "Radius: " + radius);
            RedProtect.get().logger.debug(LogLevel.DEFAULT, "X radius: " + Math.abs(r.getCenterX() - px) + " - Z radius: " + Math.abs(r.getCenterZ() - pz));
            if (Math.abs(r.getCenterX() - px) <= radius && Math.abs(r.getCenterZ() - pz) <= radius) {
                ret.add(r);
            }
        }
        return ret;
    }
    
    /*
    @Override
    public boolean regionExists(Region region) {
    	if (regions.containsValue(region)){
			return true;
		}
		return false;
    }
    */

    public World getWorld() {
        return this.world;
    }

    @Override
    public Set<Region> getRegions(int x, int y, int z) {
        Set<Region> regionl = new HashSet<>();
        for (Region r : regions.values()) {
            if (x <= r.getMaxMbrX() && x >= r.getMinMbrX() && y <= r.getMaxY() && y >= r.getMinY() && z <= r.getMaxMbrZ() && z >= r.getMinMbrZ()) {
                regionl.add(r);
            }
        }
        return regionl;
    }

    @Override
    public Region getTopRegion(int x, int y, int z) {
        Map<Integer, Region> regionlist = new HashMap<>();
        int max = 0;
        for (Region r : regions.values()) {
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
        for (Region r : regions.values()) {
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
        for (Region r : regions.values()) {
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
        allregions.addAll(regions.values());
        return allregions;
    }

    @Override
    public void clearRegions() {
        regions.clear();
    }

    @Override
    public void updateLiveRegion(String rname, String columm, Object value) {
    }

    @Override
    public void closeConn() {
    }

    @Override
    public int getTotalRegionNum() {
        return regions.size();
    }

    @Override
    public void updateLiveFlags(String rname, String flag, String value) {
    }

    @Override
    public void removeLiveFlags(String rname, String flag) {
    }

}
