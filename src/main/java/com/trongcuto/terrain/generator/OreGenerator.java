package com.trongcuto.terrain.generator;

import org.bukkit.Material;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import java.util.Random;

/**
 * Vein-based ore placement. Runs as a {@link BlockPopulator} after the base
 * terrain exists, so it can only ever convert existing {@link Material#STONE}
 * into ore. Each ore type spawns a vanilla-like number of small veins per chunk
 * within a fixed altitude band, instead of being driven by the terrain noise
 * (which produced huge ore masses that "swallowed" the mountains).
 */
public final class OreGenerator extends BlockPopulator {

    /** One configurable ore: material, altitude band, veins per chunk, blob size. */
    private record Ore(Material material, int minY, int maxY, int veinsPerChunk, int veinSize) {
    }

    private static final Ore[] ORES = {
            new Ore(Material.COAL_ORE, 0, 190, 20, 12),
            new Ore(Material.COPPER_ORE, -16, 112, 8, 10),
            new Ore(Material.IRON_ORE, -24, 72, 12, 8),
            new Ore(Material.LAPIS_ORE, -32, 32, 2, 6),
            new Ore(Material.GOLD_ORE, -64, 30, 3, 8),
            new Ore(Material.REDSTONE_ORE, -64, 16, 8, 7),
            new Ore(Material.DIAMOND_ORE, -64, 14, 1, 4),
    };

    private final long seed;

    public OreGenerator(long seed) {
        this.seed = seed;
    }

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ,
                         LimitedRegion region) {
        // Deterministic per-chunk RNG so a given seed always yields the same ores.
        Random rng = new Random(seed
                ^ (chunkX * 341873128712L)
                ^ (chunkZ * 132897987541L));

        int worldMinY = worldInfo.getMinHeight();
        int worldMaxY = worldInfo.getMaxHeight();
        int originX = chunkX << 4;
        int originZ = chunkZ << 4;

        for (Ore ore : ORES) {
            int loY = Math.max(ore.minY(), worldMinY);
            int hiY = Math.min(ore.maxY(), worldMaxY - 1);
            if (loY > hiY) {
                continue;
            }
            for (int v = 0; v < ore.veinsPerChunk(); v++) {
                int x = originX + rng.nextInt(16);
                int z = originZ + rng.nextInt(16);
                int y = loY + rng.nextInt(hiY - loY + 1);
                placeVein(region, rng, ore.material(), x, y, z, ore.veinSize(), worldMaxY);
            }
        }
    }

    /**
     * Grow a small blob of ore from an origin via a short random walk, only ever
     * replacing stone.
     */
    private void placeVein(LimitedRegion region, Random rng, Material material,
                           int x, int y, int z, int size, int worldMaxY) {
        int cx = x;
        int cy = y;
        int cz = z;
        for (int i = 0; i < size; i++) {
            replaceStone(region, cx, cy, cz, material, worldMaxY);
            cx += rng.nextInt(3) - 1;
            cy += rng.nextInt(3) - 1;
            cz += rng.nextInt(3) - 1;
        }
    }

    private void replaceStone(LimitedRegion region, int x, int y, int z,
                              Material material, int worldMaxY) {
        if (y >= worldMaxY || !region.isInRegion(x, y, z)) {
            return;
        }
        if (region.getType(x, y, z) == Material.STONE) {
            region.setType(x, y, z, material);
        }
    }
}
