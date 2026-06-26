package com.trongcuto.terrain.generator;

import com.trongcuto.terrain.config.TerrainConfig;
import com.trongcuto.terrain.noise.SimplexNoise;
import org.bukkit.Material;

/**
 * Noise-driven ore placement. Ore type is chosen by altitude band (tuned for
 * the 1.21 world height range of -64..320) and a dedicated 3D noise so that
 * ores form connected veins rather than random speckles.
 */
public final class OreGenerator {

    private final SimplexNoise oreNoise;

    public OreGenerator(long seed) {
        this.oreNoise = new SimplexNoise(seed + 100);
    }

    /**
     * Possibly replace a stone block with an ore.
     *
     * @return the ore material, or {@link Material#STONE} when no ore applies
     */
    public Material oreAt(int x, int y, int z) {
        double n = Math.abs(oreNoise.noise(
                x * TerrainConfig.ORE_SCALE,
                y * (TerrainConfig.ORE_SCALE * 0.8),
                z * TerrainConfig.ORE_SCALE));

        if (y < -48) {
            if (n < 0.20) return Material.DIAMOND_ORE;
        } else if (y < -16) {
            if (n < 0.15) return Material.DIAMOND_ORE;
            if (n < 0.35) return Material.GOLD_ORE;
            if (n < 0.50) return Material.REDSTONE_ORE;
        } else if (y < 24) {
            if (n < 0.10) return Material.DIAMOND_ORE;
            if (n < 0.28) return Material.GOLD_ORE;
            if (n < 0.48) return Material.IRON_ORE;
            if (n < 0.62) return Material.COPPER_ORE;
        } else if (y < 80) {
            if (n < 0.20) return Material.IRON_ORE;
            if (n < 0.34) return Material.LAPIS_ORE;
            if (n < 0.52) return Material.COPPER_ORE;
            if (n < 0.68) return Material.COAL_ORE;
        } else {
            if (n < 0.30) return Material.COAL_ORE;
        }

        return Material.STONE;
    }
}
