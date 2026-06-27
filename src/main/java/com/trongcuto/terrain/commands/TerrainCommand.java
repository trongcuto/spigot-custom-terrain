package com.trongcuto.terrain.commands;

import com.trongcuto.terrain.config.TerrainConfig;
import com.trongcuto.terrain.generator.CustomChunkGenerator;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Handles {@code /terrain create|config|list|reset|help}.
 */
public final class TerrainCommand implements TabExecutor {

    private final JavaPlugin plugin;

    public TerrainCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create" -> handleCreate(sender, args);
            case "config" -> handleConfig(sender, args);
            case "list" -> sender.sendMessage("§eTerrain settings: §f" + TerrainConfig.getCurrentSettings());
            case "reset" -> {
                TerrainConfig.resetDefaults();
                sender.sendMessage("§aTerrain parameters reset to defaults.");
            }
            default -> sendHelp(sender);
        }
        return true;
    }

    private void handleCreate(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /terrain create <world-name>");
            return;
        }
        String worldName = args[1];
        if (plugin.getServer().getWorld(worldName) != null) {
            sender.sendMessage("§cWorld '" + worldName + "' already exists.");
            return;
        }

        sender.sendMessage("§eGenerating world '" + worldName + "' with the 3D terrain generator...");
        WorldCreator creator = new WorldCreator(worldName);
        creator.generator(new CustomChunkGenerator());
        World world = creator.createWorld();

        if (world == null) {
            sender.sendMessage("§cFailed to create world '" + worldName + "'.");
            return;
        }
        sender.sendMessage("§aWorld '" + worldName + "' created!");
        if (sender instanceof Player player) {
            player.teleport(world.getSpawnLocation());
        }
    }

    private void handleConfig(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cUsage: /terrain config <param> <value>");
            sender.sendMessage("§7Params: §f" + String.join(", ", TerrainConfig.PARAMETERS));
            return;
        }
        String param = args[1];
        double value;
        try {
            value = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§c'" + args[2] + "' is not a number.");
            return;
        }
        if (TerrainConfig.applySetting(param, value)) {
            sender.sendMessage("§aSet §f" + param.toLowerCase() + " §a= §f" + value);
            sender.sendMessage("§7Applies to worlds generated from now on.");
        } else {
            sender.sendMessage("§cUnknown parameter '" + param + "'.");
            sender.sendMessage("§7Params: §f" + String.join(", ", TerrainConfig.PARAMETERS));
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6=== Custom Terrain Generator ===");
        sender.sendMessage("§e/terrain create <name> §7- create a world using the 3D generator");
        sender.sendMessage("§e/terrain config <param> <value> §7- tune a generation parameter");
        sender.sendMessage("§e/terrain list §7- show current parameters");
        sender.sendMessage("§e/terrain reset §7- reset parameters to defaults");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(Arrays.asList("create", "config", "list", "reset", "help"), args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("config")) {
            return filter(Arrays.asList(TerrainConfig.PARAMETERS), args[1]);
        }
        return new ArrayList<>();
    }

    private List<String> filter(List<String> options, String prefix) {
        List<String> result = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase().startsWith(prefix.toLowerCase())) {
                result.add(option);
            }
        }
        return result;
    }
}
