package com.trongcuto.terrain.generator;

import com.trongcuto.terrain.config.TerrainConfig;
import com.trongcuto.terrain.noise.SimplexNoise;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Custom {@link ChunkGenerator} that builds terrain from a true 3D Simplex-noise
 * density field.
 *
 * <p>For every block position the generator evaluates 3D noise and subtracts a
 * vertical "squash" gradient that pushes density negative with altitude. Where
 * the density is positive the block is solid, otherwise it is air (or water
 * below sea level). Because the field is fully 3D — not a single height per
 * column — the terrain naturally produces overhangs, arches, floating crags and
 * caves that a 2D heightmap cannot.</p>
 *
 * <p>Ores ({@link OreGenerator}), biomes ({@link BiomeManager}) and trees
 * ({@link StructureGenerator}) are all layered on top of the same noise
 * pipeline.</p>
 */
public final class CustomChunkGenerator extends ChunkGenerator {

    private volatile SimplexNoise density;
    private volatile SimplexNoise ridge;
    private volatile SimplexNoise surfaceVariation;
    private volatile OreGenerator oreGenerator;
    private volatile long initializedSeed;
    private volatile boolean initialized;

    private synchronized void ensureInitialized(long seed) {
        if (initialized && seed == initializedSeed) {
            return;
        }
        density = new SimplexNoise(seed);
        ridge = new SimplexNoise(seed ^ 0x9E3779B97F4A7C15L);
        surfaceVariation = new SimplexNoise(seed * 31L + 17L);
        oreGenerator = new OreGenerator(seed);
        initializedSeed = seed;
        initialized = true;
    }

    /**
     * 3D density at a world position. Positive values are solid.
     */
    private double densityAt(int x, int y, int z) {
        double base = density.octaves(
                x * TerrainConfig.HORIZONTAL_SCALE,
                y * TerrainConfig.VERTICAL_SCALE,
                z * TerrainConfig.HORIZONTAL_SCALE,
                4, 0.5, 2.0);

        double ridges = ridge.ridged(
                x * TerrainConfig.HORIZONTAL_SCALE * 0.5,
                y * TerrainConfig.VERTICAL_SCALE * 0.5,
                z * TerrainConfig.HORIZONTAL_SCALE * 0.5,
                3, 0.5, 2.2);

        double value = base + (ridges - 0.5) * TerrainConfig.RIDGE_WEIGHT;

        // Altitude gradient gives a recognisable ground/sky split.
        value -= (y - TerrainConfig.TERRAIN_CENTER) * TerrainConfig.SQUASH_FACTOR;

        return value;
    }

    @Override
    public void generateNoise(WorldInfo worldInfo, Random random,
                              int chunkX, int chunkZ, ChunkData chunkData) {
        ensureInitialized(worldInfo.getSeed());

        int minY = worldInfo.getMinHeight();
        int maxY = worldInfo.getMaxHeight();

        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                int worldX = (chunkX << 4) + localX;
                int worldZ = (chunkZ << 4) + localZ;

                // Scan top-down so we always know how far below the exposed
                // surface a solid block sits, even under 3D overhangs. The top
                // of the world counts as open air.
                boolean airAbove = true;
                int depth = 0;

                for (int y = maxY - 1; y >= minY; y--) {
                    boolean solid = densityAt(worldX, y, worldZ) > 0;

                    if (solid) {
                        depth = airAbove ? 0 : depth + 1;
                        Material material = pickSolidMaterial(worldX, y, worldZ, depth);
                        chunkData.setBlock(localX, y, localZ, material);
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
     * blocks between this voxel and the air/water directly above it (0 == the
     * exposed surface block).
     */
    private Material pickSolidMaterial(int x, int y, int z, int depth) {
        // Beach / sea bed near the water line.
        if (depth <= TerrainConfig.DIRT_DEPTH && y <= TerrainConfig.SEA_LEVEL + 2
                && y >= TerrainConfig.SEA_LEVEL - 4) {
            return Material.SAND;
        }

        if (depth == 0) {
            if (y < TerrainConfig.SEA_LEVEL) {
                return Material.GRAVEL;
            }
            double v = surfaceVariation.noise(x * 0.05, 0.0, z * 0.05);
            return v > 0.65 ? Material.COARSE_DIRT : Material.GRASS_BLOCK;
        }

        if (depth <= TerrainConfig.DIRT_DEPTH) {
            return Material.DIRT;
        }

        return stoneOrOre(x, y, z);
    }

    private Material stoneOrOre(int x, int y, int z) {
        Material ore = oreGenerator.oreAt(x, y, z);
        return ore != null ? ore : Material.STONE;
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
        return false;
    }

    @Override
    public boolean shouldGenerateDecorations() {
        return false;
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
        return Collections.singletonList(new StructureGenerator(world.getSeed()));
    }

    @Override
    public Location getFixedSpawnLocation(World world, Random random) {
        ensureInitialized(world.getSeed());
        int x = 0;
        int z = 0;
        int maxY = world.getMaxHeight();
        int minY = world.getMinHeight();
        for (int y = maxY - 1; y >= minY; y--) {
            if (densityAt(x, y, z) > 0) {
                return new Location(world, x + 0.5, y + 1.5, z + 0.5);
            }
        }
        return new Location(world, x + 0.5, TerrainConfig.SEA_LEVEL + 1.5, z + 0.5);
    }
}
