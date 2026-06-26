package com.trongcuto.terrain.generator;

import com.trongcuto.terrain.config.TerrainConfig;
import com.trongcuto.terrain.noise.SimplexNoise;
import org.bukkit.Material;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import java.util.Random;

/**
 * Places trees on the generated surface. Runs as a {@link BlockPopulator} after
 * the base terrain exists, so it correctly sits on top of the 3D surface
 * (including overhangs) rather than a precomputed 2D heightmap.
 */
public final class StructureGenerator extends BlockPopulator {

    private final SimplexNoise treeNoise;

    public StructureGenerator(long seed) {
        this.treeNoise = new SimplexNoise(seed + 300);
    }

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ,
                         LimitedRegion region) {
        int minY = worldInfo.getMinHeight();
        int maxY = worldInfo.getMaxHeight();

        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                int worldX = (chunkX << 4) + localX;
                int worldZ = (chunkZ << 4) + localZ;

                double n = Math.abs(treeNoise.noise(worldX * 0.05, 0.0, worldZ * 0.05));
                if (n < TerrainConfig.TREE_THRESHOLD) {
                    continue;
                }

                int surfaceY = highestGrass(region, worldX, worldZ, minY, maxY);
                if (surfaceY == Integer.MIN_VALUE) {
                    continue;
                }
                if (surfaceY <= TerrainConfig.SEA_LEVEL) {
                    continue;
                }
                placeTree(region, worldX, surfaceY + 1, worldZ, random, maxY);
            }
        }
    }

    private int highestGrass(LimitedRegion region, int x, int z, int minY, int maxY) {
        for (int y = maxY - 1; y >= minY; y--) {
            if (!region.isInRegion(x, y, z)) {
                continue;
            }
            Material type = region.getType(x, y, z);
            if (type == Material.GRASS_BLOCK) {
                return y;
            }
            if (type != Material.AIR && type != Material.WATER && type != Material.OAK_LEAVES) {
                // Hit a non-soil surface (stone/sand/etc.) before grass.
                return Integer.MIN_VALUE;
            }
        }
        return Integer.MIN_VALUE;
    }

    private void placeTree(LimitedRegion region, int x, int baseY, int z, Random random, int maxY) {
        int trunkHeight = 4 + random.nextInt(3);
        int topY = baseY + trunkHeight;

        // Leaves: two wide layers, then a narrow cap.
        placeLeafLayer(region, x, z, topY - 2, 2, maxY);
        placeLeafLayer(region, x, z, topY - 1, 2, maxY);
        placeLeafLayer(region, x, z, topY, 1, maxY);
        setIfAir(region, x, topY + 1, z, Material.OAK_LEAVES, maxY);

        // Trunk last so it overwrites any leaves in the centre column.
        for (int y = baseY; y < topY; y++) {
            if (y < maxY && region.isInRegion(x, y, z)) {
                region.setType(x, y, z, Material.OAK_LOG);
            }
        }
    }

    private void placeLeafLayer(LimitedRegion region, int cx, int cz, int y, int radius, int maxY) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz > radius * radius + 1) {
                    continue;
                }
                setIfAir(region, cx + dx, y, cz + dz, Material.OAK_LEAVES, maxY);
            }
        }
    }

    private void setIfAir(LimitedRegion region, int x, int y, int z, Material material, int maxY) {
        if (y >= maxY || !region.isInRegion(x, y, z)) {
            return;
        }
        if (region.getType(x, y, z) == Material.AIR) {
            region.setType(x, y, z, material);
        }
    }
}
