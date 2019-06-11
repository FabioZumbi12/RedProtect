package br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommands.RegionHandlers;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.commands.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static br.net.fabiozumbi12.RedProtect.Bukkit.commands.CommandHandlers.handleKillWorld;

public class KillCommand implements SubCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        World world;
        EntityType entity = null;

        if (args.length == 0) {
            world = sender instanceof Player ? ((Player) sender).getWorld() : null;
        } else if (args.length == 1) {
            world = Bukkit.getWorld(args[0]);
        } else if (args.length == 2) {
            world = Bukkit.getWorld(args[0]);
            try {
                entity = EntityType.valueOf(args[1].toUpperCase());
            } catch (Exception ignored) {
                RedProtect.get().lang.sendCommandHelp(sender, "kill", true);
                return true;
            }
        } else {
            RedProtect.get().lang.sendCommandHelp(sender, "kill", true);
            return true;
        }

        if (world == null) {
            RedProtect.get().lang.sendMessage(sender, "cmdmanager.region.invalidworld");
            return true;
        }

        handleKillWorld(sender, world, entity);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> tab = new ArrayList<>();
        if (args.length == 1) {
            if (args[0].isEmpty())
                tab.addAll(Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList()));
            else
                tab.addAll(Bukkit.getWorlds().stream().filter(e -> e.getName().startsWith(args[0])).map(World::getName).collect(Collectors.toList()));
        }
        if (args.length == 2) {
            if (args[1].isEmpty())
                tab.addAll(Arrays.stream(EntityType.values()).map(EntityType::name).collect(Collectors.toList()));
            else
                tab.addAll(Arrays.stream(EntityType.values()).filter(e -> e.name().startsWith(args[1].toUpperCase())).map(EntityType::name).collect(Collectors.toList()));
        }
        return tab;
    }
}
