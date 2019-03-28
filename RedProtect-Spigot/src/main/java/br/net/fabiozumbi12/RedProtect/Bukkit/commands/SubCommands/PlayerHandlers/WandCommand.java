package br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommands.PlayerHandlers;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommand;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPConfig;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPLang;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import static br.net.fabiozumbi12.RedProtect.Bukkit.helpers.RPUtil.HandleHelpPage;

public class WandCommand implements SubCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            HandleHelpPage(sender, 1);
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            Inventory inv = player.getInventory();
            Material mat = Material.getMaterial(RPConfig.getString("wands.adminWandID"));
            ItemStack item = new ItemStack(mat);
            if (!inv.contains(mat) && inv.firstEmpty() != -1) {
                inv.addItem(item);
                RPLang.sendMessage(player, RPLang.get("cmdmanager.wand.given").replace("{item}", item.getType().name()));
            } else {
                RPLang.sendMessage(player, RPLang.get("cmdmanager.wand.nospace").replace("{item}", item.getType().name()));
            }
            return true;
        }

        RPLang.sendCommandHelp(sender, "wand", true);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}