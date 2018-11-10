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
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.type.TileEntityInventory;
import org.spongepowered.api.world.World;

public class RPEconomy {


    public static long getRegionValue(Region r) {
        long regionCost = 0;
        World w = RedProtect.get().serv.getWorld(r.getWorld()).get();
        int maxX = r.getMaxMbrX();
        int minX = r.getMinMbrX();
        int maxZ = r.getMaxMbrZ();
        int minZ = r.getMinMbrZ();
        for (int x = minX; x < maxX; x++) {
            for (int y = 0; y < 256; y++) {
                for (int z = minZ; z < maxZ; z++) {

                    BlockSnapshot b = w.createSnapshot(x, y, z);
		      /*
		      Location<World> loc = new Location<World>(w, x, y, z);		      
		      Collection<Entity> ents = w.getEntities(ent-> ent.getLocation().getBlockPosition().equals(loc.getBlockPosition()));
		      for (Entity ent:ents){
		    	  if (ent instanceof Equipable){
		    		  Equipable equip = (Equipable) ent;
		    		  regionCost += getInvValue(equip.getInventory().slots());
		    	  }
		      }
		      */
                    if (b.getState().getType().equals(BlockTypes.AIR)) {
                        continue;
                    }

                    if (b.getLocation().get().getTileEntity().isPresent()) {
                        TileEntity invTile = b.getLocation().get().getTileEntity().get();
                        if (invTile instanceof TileEntityInventory) {
                            TileEntityInventory<?> inv = (TileEntityInventory<?>) invTile;
                            regionCost += getInvValue(inv.slots());
                        }
                    } else {
                        regionCost += RedProtect.get().cfgs.getBlockCost(b.getState().getType().getName());
                    }
                }
            }
        }
        r.setValue(regionCost);
        return regionCost;
    }

    private static long getInvValue(Iterable<Inventory> inv) {
        return RedProtect.get().getPVHelper().getInvValue(inv);
    }

    public static String getCostMessage(Region r) {
        return RPLang.get("economy.forsale") + " &6" + getFormatted(r.getValue()) + " &2" + RedProtect.get().cfgs.getEcoString("economy-name");
    }

    public static String getFormatted(long value) {
        return RedProtect.get().cfgs.getEcoString("economy-symbol") + value;
    }

    public static boolean putToSell(Region r, String uuid, long value) {
        try {
            r.clearMembers();
            r.clearAdmins();
            r.setValue(value);
            r.setWelcome(getCostMessage(r));
            r.setFlag(null, "for-sale", true);
            if (RedProtect.get().cfgs.getEcoBool("rename-region")) {
                RedProtect.get().rm.renameRegion(RPUtil.nameGen(RPUtil.UUIDtoPlayer(uuid), r.getWorld()), r);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean BuyRegion(Region r, String uuid) {
        try {
            r.clearMembers();
            r.clearAdmins();
            r.clearLeaders();
            r.addLeader(uuid);
            r.setDate(RPUtil.DateNow());
            r.setWelcome("");
            r.setFlags(RedProtect.get().cfgs.getDefFlagsValues());
            if (RedProtect.get().cfgs.getEcoBool("rename-region")) {
                RedProtect.get().rm.renameRegion(RPUtil.nameGen(RPUtil.UUIDtoPlayer(uuid), r.getWorld()), r);
            }
            r.removeFlag("for-sale");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
