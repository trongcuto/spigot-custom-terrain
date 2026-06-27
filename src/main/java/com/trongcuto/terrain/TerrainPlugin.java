package com.trongcuto.terrain;

import com.trongcuto.terrain.commands.TerrainCommand;
import com.trongcuto.terrain.generator.CustomChunkGenerator;
import org.bukkit.command.PluginCommand;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Entry point for the Custom Terrain Generator.
 *
 * <p>Generates Minecraft terrain from a true 3D Simplex-noise density field
 * (see {@link CustomChunkGenerator}) for Spigot 1.21.x.</p>
 */
public final class TerrainPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("Custom Terrain Generator (3D Simplex density field) enabled.");

        PluginCommand command = getCommand("terrain");
        if (command != null) {
            command.setExecutor(new TerrainCommand(this));
        } else {
            getLogger().warning("Command 'terrain' is missing from plugin.yml; commands disabled.");
        }

        getLogger().info("Use /terrain create <name> to generate a world, /terrain config to tune it.");
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return new CustomChunkGenerator();
    }

    @Override
    public void onDisable() {
        getLogger().info("Custom Terrain Generator disabled.");
    }
}
