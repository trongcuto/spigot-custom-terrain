package com.trongcuto.terrain.generator;

import com.trongcuto.terrain.config.TerrainConfig;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

import java.util.List;
import java.util.Random;

/**
 * Custom {@link ChunkGenerator} built on a multi-noise {@link TerrainShape}.
 *
 * <p>Each column gets a target surface height that varies by region: flat
 * lowlands, rolling hills and tall, steep mountains, with rivers carved into the
 * lowlands. A small amount of local 3D noise wobbles the surface into overhangs
 * and cliffs (stronger on mountains) so the world keeps a 3D feel without
 * breaking into floating islands.</p>
 *
 * <p>Surface blocks are biome-aware (sand deserts, snowy peaks, etc.). Vanilla
 * biome decoration and caves are left enabled so each biome gets its authentic
 * vegetation, trees, ores and cave systems from the shared {@link BiomeManager}
 * climate.</p>
 */
public final class CustomChunkGenerator extends ChunkGenerator {

    private volatile TerrainShape shape;
    private volatile long initializedSeed;
    private volatile boolean initialized;

    private synchronized TerrainShape ensureInitialized(long seed) {
        if (!initialized || seed != initializedSeed) {
            shape = new TerrainShape(seed);
            initializedSeed = seed;
            initialized = true;
        }
        return shape;
    }

    @Override
    public void generateNoise(WorldInfo worldInfo, Random random,
                              int chunkX, int chunkZ, ChunkData chunkData) {
        TerrainShape terrain = ensureInitialized(worldInfo.getSeed());

        int minY = worldInfo.getMinHeight();
        int maxY = worldInfo.getMaxHeight();

        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                int worldX = (chunkX << 4) + localX;
                int worldZ = (chunkZ << 4) + localZ;

                TerrainShape.Column col = terrain.columnAt(worldX, worldZ);
                double overhangAmp = TerrainConfig.DETAIL_AMPLITUDE_BASE
                        + col.ruggedness() * TerrainConfig.DETAIL_AMPLITUDE_MOUNTAIN;

                boolean airAbove = true;
                int depth = 0;

                for (int y = maxY - 1; y >= minY; y--) {
                    double density = (col.height() - y)
                            + terrain.detail(worldX, y, worldZ) * overhangAmp;

                    if (density > 0) {
                        depth = airAbove ? 0 : depth + 1;
                        chunkData.setBlock(localX, y, localZ,
                                pickSolidMaterial(col, worldX, y, worldZ, depth));
                        airAbove = false;
                    } else {
                        if (y <= TerrainConfig.SEA_LEVEL) {
                            chunkData.setBlock(localX, y, localZ, Material.WATER);
                        }
                        airAbove = true;
                    }
                }
            }
        }
    }

    /**
     * Chooses the block for a solid voxel. {@code depth} is the number of solid
     * blocks between this voxel and the air/water directly above (0 == exposed
     * surface). Surface material is biome-aware so the terrain reads correctly
     * even before vanilla decoration runs.
     */
    private Material pickSolidMaterial(TerrainShape.Column col, int x, int y, int z, int depth) {
        boolean desert = col.temperature() > 0.45 && col.humidity() < -0.1;
        boolean cold = col.temperature() < -0.35;

        // Beach / sea bed near the water line.
        if (depth <= TerrainConfig.DIRT_DEPTH && y <= TerrainConfig.SEA_LEVEL + 2
                && y >= TerrainConfig.SEA_LEVEL - 4) {
            return Material.SAND;
        }

        if (depth == 0) {
            if (y < TerrainConfig.SEA_LEVEL) {
                return Material.GRAVEL;
            }
            if (desert) {
                return Material.SAND;
            }
            // Bare rock on the steepest, highest peaks.
            if (col.ruggedness() > 0.55 && y > TerrainConfig.SEA_LEVEL + 80) {
                return Material.STONE;
            }
            if (cold && y > TerrainConfig.SEA_LEVEL + 55) {
                return Material.SNOW_BLOCK;
            }
            return Material.GRASS_BLOCK;
        }

        if (depth <= TerrainConfig.DIRT_DEPTH) {
            return desert ? Material.SANDSTONE : Material.DIRT;
        }

        // Ores are added by vanilla biome decoration.
        return Material.STONE;
    }

    @Override
    public void generateBedrock(WorldInfo worldInfo, Random random,
                                int chunkX, int chunkZ, ChunkData chunkData) {
        int minY = worldInfo.getMinHeight();
        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                chunkData.setBlock(localX, minY, localZ, Material.BEDROCK);
                if (random.nextInt(2) == 0) {
                    chunkData.setBlock(localX, minY + 1, localZ, Material.BEDROCK);
                }
            }
        }
    }

    @Override
    public boolean shouldGenerateNoise() {
        return false;
    }

    @Override
    public boolean shouldGenerateSurface() {
        return false;
    }

    @Override
    public boolean shouldGenerateBedrock() {
        return false;
    }

    @Override
    public boolean shouldGenerateCaves() {
        return true;
    }

    @Override
    public boolean shouldGenerateDecorations() {
        return true;
    }

    @Override
    public boolean shouldGenerateMobs() {
        return true;
    }

    @Override
    public boolean shouldGenerateStructures() {
        return false;
    }

    @Override
    public BiomeProvider getDefaultBiomeProvider(WorldInfo worldInfo) {
        return new BiomeManager(worldInfo.getSeed());
    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        return List.of();
    }

    @Override
    public Location getFixedSpawnLocation(World world, Random random) {
        TerrainShape terrain = ensureInitialized(world.getSeed());
        TerrainShape.Column col = terrain.columnAt(0, 0);
        int y = (int) Math.ceil(col.height());
        y = Math.max(y, TerrainConfig.SEA_LEVEL);
        return new Location(world, 0.5, y + 1.5, 0.5);
    }
}
