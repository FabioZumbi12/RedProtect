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

package br.net.fabiozumbi12.RedProtect.Bukkit.schematics;

import br.net.fabiozumbi12.RedProtect.Bukkit.RPUtil;
import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPConfig;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPLang;
import br.net.fabiozumbi12.RedProtect.Bukkit.schematics.org.jnbt.*;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.GZIPInputStream;

@SuppressWarnings("deprecation")
public class RPSchematics {

    public static void pasteSchematic(Player p) throws IOException {
        File file = new File(RedProtect.get().getDataFolder(), "schematics" + File.separator + RPConfig.getString("schematics.first-house-file"));
        FileInputStream stream = new FileInputStream(file);

        World world = p.getWorld();
        Location loc = p.getLocation();

        NBTInputStream nbtStream = new NBTInputStream(new GZIPInputStream(stream));

        NamedTag rootTag = nbtStream.readNamedTag();
        nbtStream.close();
        if (!rootTag.getName().equals("Schematic")) {
            throw new IllegalArgumentException("Tag \"Schematic\" does not exist or is not first");
        }

        CompoundTag schematicTag = (CompoundTag) rootTag.getTag();

        Map<String, Tag> schematic = schematicTag.getValue();
        if (!schematic.containsKey("Blocks")) {
            throw new IllegalArgumentException("Schematic file is missing a \"Blocks\" tag");
        }

        short width = getChildTag(schematic, "Width", ShortTag.class).getValue();
        short length = getChildTag(schematic, "Length", ShortTag.class).getValue();
        short height = getChildTag(schematic, "Height", ShortTag.class).getValue();

        String materials = getChildTag(schematic, "Materials", StringTag.class).getValue();
        if (!materials.equals("Alpha")) {
            throw new IllegalArgumentException("Schematic file is not an Alpha schematic");
        }

        // Get blocks
        byte[] blockId = getChildTag(schematic, "Blocks", ByteArrayTag.class).getValue();
        byte[] blockData = getChildTag(schematic, "Data", ByteArrayTag.class).getValue();
        byte[] addId = new byte[0];
        short[] blocks = new short[blockId.length]; // Have to later combine IDs

        // We support 4096 block IDs using the same method as vanilla Minecraft, where
        // the highest 4 bits are stored in a separate byte array.
        if (schematic.containsKey("AddBlocks")) {
            addId = getChildTag(schematic, "AddBlocks", ByteArrayTag.class).getValue();
        }

        // Combine the AddBlocks data with the first 8-bit block ID
        for (int index = 0; index < blockId.length; index++) {
            if ((index >> 1) >= addId.length) { // No corresponding AddBlocks index
                blocks[index] = (short) (blockId[index] & 0xFF);
            } else {
                if ((index & 1) == 0) {
                    blocks[index] = (short) (((addId[index >> 1] & 0x0F) << 8) + (blockId[index] & 0xFF));
                } else {
                    blocks[index] = (short) (((addId[index >> 1] & 0xF0) << 4) + (blockId[index] & 0xFF));
                }
            }
        }


        int offsetX = 0;
        int offsetY = 0;
        int offsetZ = 0;

        try {
            offsetX = getChildTag(schematic, "WEOffsetX", IntTag.class).getValue();
            offsetY = getChildTag(schematic, "WEOffsetY", IntTag.class).getValue();
            offsetZ = getChildTag(schematic, "WEOffsetZ", IntTag.class).getValue();
        } catch (Exception e) {
            // No offset data
        }

        Map<Integer, BlockState> blist = new HashMap<>();
        Location pos1 = loc;
        Location pos2 = loc;
        int order = 0;
        int base = 0;
        int validBlock = 0;

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                for (int z = 0; z < length; ++z) {
                    int index = y * width * length + z * width + x;
                    Location locblock = new Location(world, x + loc.getX() + offsetX, y + loc.getY() + offsetY, z + loc.getZ() + offsetZ);
                    Block block = locblock.getBlock();
                    BlockState bstate = block.getState();

                    blist.put(index, bstate);

                    if (order == 0) {
                        pos1 = locblock;
                    } else {
                        pos2 = locblock;
                    }

                    //get ground blocks
                    if (block.getLocation().getBlockY() == pos1.getBlockY()) {
                        base++;
                        if (block.getType().isSolid()) {
                            validBlock++;
                        }
                    }
                    order++;
                }
            }
        }

        //check if can place the schematic
        if (validBlock < base / 2) {
            RPLang.sendMessage(p, "playerlistener.region.needground");
            return;
        }

        String regionName = RPUtil.regionNameConfiorm("", p);
        String pName = RPUtil.PlayerToUUID(p.getName());

        //check if player already have claims
        int claimused = RedProtect.get().rm.getPlayerRegions(p.getName(), p.getWorld());
        if (claimused > 0 && !p.hasPermission("redprotect.bypass")) {
            RPLang.sendMessage(p, "playerlistener.region.claimlimit.start");
            return;
        }

        Region region = new Region(regionName, new ArrayList<>(), new ArrayList<>(), Collections.singletonList(pName), new int[]{pos1.getBlockX(), pos1.getBlockX(), pos2.getBlockX(), pos2.getBlockX()}, new int[]{pos1.getBlockZ(), pos1.getBlockZ(), pos2.getBlockZ(), pos2.getBlockZ()}, 0, p.getWorld().getMaxHeight(), 0, p.getWorld().getName(), RPUtil.DateNow(), RPConfig.getDefFlagsValues(), "", 0, null, false);

        List<String> othersName = new ArrayList<>();
        Region otherrg = null;

        //check if same area
        otherrg = RedProtect.get().rm.getTopRegion(region.getCenterLoc());
        if (otherrg != null && otherrg.get4Points(region.getCenterY()).equals(region.get4Points(region.getCenterY()))) {
            p.sendMessage(RPLang.get("regionbuilder.region.overlapping").replace("{location}", "x: " + otherrg.getCenterX() + ", z: " + otherrg.getCenterZ()).replace("{player}", otherrg.getLeadersDesc()));
            return;
        }

        //check regions inside region
        for (Region r : RedProtect.get().rm.getRegionsByWorld(p.getWorld())) {
            if (r.getMaxMbrX() <= region.getMaxMbrX() && r.getMaxY() <= region.getMaxY() && r.getMaxMbrZ() <= region.getMaxMbrZ() && r.getMinMbrX() >= region.getMinMbrX() && r.getMinY() >= region.getMinY() && r.getMinMbrZ() >= region.getMinMbrZ()) {
                if (!r.isLeader(p) && !p.hasPermission("redprotect.bypass")) {
                    p.sendMessage(RPLang.get("regionbuilder.region.overlapping").replace("{location}", "x: " + otherrg.getCenterX() + ", z: " + otherrg.getCenterZ()).replace("{player}", otherrg.getLeadersDesc()));
                    return;
                }
                if (!othersName.contains(r.getName())) {
                    othersName.add(r.getName());
                }
            }
        }

        //check borders for other regions
        List<Location> limitlocs = region.getLimitLocs(region.getMinY(), region.getMaxY(), true);
        for (Location locr : limitlocs) {
        	
        	/*
        	//check regions near
        	if (!RPUtil.canBuildNear(p, loc)){
            	return;    	
            }*/

            otherrg = RedProtect.get().rm.getTopRegion(locr);
            RedProtect.get().logger.debug("protection Block is: " + locr.getBlock().getType().name());

            if (otherrg != null) {
                if (!otherrg.isLeader(p) && !p.hasPermission("redprotect.bypass")) {
                    p.sendMessage(RPLang.get("regionbuilder.region.overlapping").replace("{location}", "x: " + otherrg.getCenterX() + ", z: " + otherrg.getCenterZ()).replace("{player}", otherrg.getLeadersDesc()));
                    return;
                }
                if (!othersName.contains(otherrg.getName())) {
                    othersName.add(otherrg.getName());
                }
            }
        }

        //check cost per block
        if (RPConfig.getEcoBool("claim-cost-per-block.enable") && RedProtect.get().Vault && !p.hasPermission("redprotect.eco.bypass")) {
            Double peco = RedProtect.get().econ.getBalance(p);
            long reco = region.getArea() * RPConfig.getEcoInt("claim-cost-per-block.cost-per-block");

            if (!RPConfig.getEcoBool("claim-cost-per-block.y-is-free")) {
                reco = reco * Math.abs(region.getMaxY() - region.getMinY());
            }

            if (peco >= reco) {
                RedProtect.get().econ.withdrawPlayer(p, reco);
                p.sendMessage(RPLang.get("economy.region.claimed").replace("{price}", RPConfig.getEcoString("economy-symbol") + reco + " " + RPConfig.getEcoString("economy-name")));
            } else {
                RPLang.sendMessage(p, RPLang.get("regionbuilder.notenought.money").replace("{price}", RPConfig.getEcoString("economy-symbol") + reco));
                return;
            }
        }    
        /*
        if (RPConfig.getBool("hooks.worldedit.use-for-schematics") && RedProtect.get().WE){
        	WEListener.pasteWithWE(p, file);
        } else {
        	for (Integer key:blist.keySet()){
        		Block b = blist.get(key).getBlock();
        		//paste schematic
        		b.setTypeIdAndData(blocks[key], blockData[key], true);
        	}
        }*/

        p.sendMessage(RPLang.get("general.color") + "------------------------------------");
        RPLang.sendMessage(p, "playerlistener.region.startdone");
        p.sendMessage(RPLang.get("general.color") + "------------------------------------");
        RPLang.sendMessage(p, "cmdmanager.region.firstwarning");
        p.sendMessage(RPLang.get("general.color") + "------------------------------------");


        RedProtect.get().logger.addLog("(World " + region.getWorld() + ") Player " + p.getName() + " CREATED(SCHEMATIC) region " + region.getName());
        RedProtect.get().rm.add(region, p.getWorld());
    }

    /**
     * Get child tag of a NBT structure.
     *
     * @param items    The parent tag map
     * @param key      The name of the tag to get
     * @param expected The expected type of the tag
     * @return child tag casted to the expected type
     * @throws IllegalArgumentException if the tag does not exist or the tag is not of the
     *                                  expected type
     */
    private static <T extends Tag> T getChildTag(Map<String, Tag> items, String key, Class<T> expected) throws IllegalArgumentException {
        if (!items.containsKey(key)) {
            throw new IllegalArgumentException("Schematic file is missing a \"" + key + "\" tag");
        }
        Tag tag = items.get(key);
        if (!expected.isInstance(tag)) {
            throw new IllegalArgumentException(key + " tag is not of tag type " + expected.getName());
        }
        return expected.cast(tag);
    }
}
