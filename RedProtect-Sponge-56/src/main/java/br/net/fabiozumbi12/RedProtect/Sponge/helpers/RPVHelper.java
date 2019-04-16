/*
 *  Copyright (c) 2019 - @FabioZumbi12
 *  Last Modified: 28/03/19 23:48
 *
 *  This class is provided 'as-is', without any express or implied warranty. In no event will the authors be held liable for any
 *   damages arising from the use of this class.
 *
 *  Permission is granted to anyone to use this class for any purpose, including commercial plugins, and to alter it and
 *  redistribute it freely, subject to the following restrictions:
 *  1 - The origin of this class must not be misrepresented; you must not claim that you wrote the original software. If you
 *  use this class in other plugins, an acknowledgment in the plugin documentation would be appreciated but is not required.
 *  2 - Altered source versions must be plainly marked as such, and must not be misrepresented as being the original class.
 *  3 - This notice may not be removed or altered from any source distribution.
 *
 *  Esta classe é fornecida "como está", sem qualquer garantia expressa ou implícita. Em nenhum caso os autores serão
 *  responsabilizados por quaisquer danos decorrentes do uso desta classe.
 *
 *  É concedida permissão a qualquer pessoa para usar esta classe para qualquer finalidade, incluindo plugins pagos, e para
 *  alterá-lo e redistribuí-lo livremente, sujeito às seguintes restrições:
 *  1 - A origem desta classe não deve ser deturpada; você não deve afirmar que escreveu a classe original. Se você usar esta
 *   classe em um plugin, uma confirmação de autoria na documentação do plugin será apreciada, mas não é necessária.
 *  2 - Versões de origem alteradas devem ser claramente marcadas como tal e não devem ser deturpadas como sendo a
 *  classe original.
 *  3 - Este aviso não pode ser removido ou alterado de qualquer distribuição de origem.
 */

package br.net.fabiozumbi12.RedProtect.Sponge.helpers;

import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public interface RPVHelper {
    Cause getCause(CommandSource p);

    void closeInventory(Player p);

    void openInventory(Inventory inv, Player p);

    void setBlock(Location<World> loc, BlockState block);

    void digBlock(Player p, ItemStack item, Vector3i loc);

    void digBlock(Player p, Vector3i loc);

    void removeBlock(Location<World> loc);

    boolean checkCause(Cause cause, String toCompare);

    boolean checkHorseOwner(Entity ent, Player owner);

    ItemStack offerEnchantment(ItemStack item);

    long getInvValue(Iterable<Inventory> inv);

    Inventory query(Inventory inventory, int x, int y);

    ItemStack getItemMainHand(Player player);

    ItemStack getItemOffHand(Player player);

    ItemType getItemInHand(Player player);

    ItemType getItemType(ItemStack itemStack);

    Inventory newInventory(int size, String name);

    void removeGuiItem(Player p);
}
