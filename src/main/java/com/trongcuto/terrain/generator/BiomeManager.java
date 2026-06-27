package com.trongcuto.terrain.generator;

import com.trongcuto.terrain.config.TerrainConfig;
import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;

import java.util.Arrays;
import java.util.List;

/**
 * Assigns biomes from the shared {@link TerrainShape}, so biomes always match
 * the terrain that was generated: rivers sit in carved channels, swamps in warm
 * wet lowlands, deserts in hot dry lowlands, and the climate turns colder with
 * altitude so tall mountains become snowy peaks. Vanilla decoration then dresses
 * each biome with its authentic vegetation.
 */
public final class BiomeManager extends BiomeProvider {

    private final TerrainShape shape;

    private static final List<Biome> PALETTE = Arrays.asList(
            Biome.PLAINS, Biome.SUNFLOWER_PLAINS, Biome.FOREST, Biome.BIRCH_FOREST,
            Biome.DARK_FOREST, Biome.SWAMP, Biome.SAVANNA, Biome.DESERT,
            Biome.TAIGA, Biome.SNOWY_TAIGA, Biome.SNOWY_PLAINS, Biome.MEADOW,
            Biome.GROVE, Biome.SNOWY_SLOPES, Biome.JAGGED_PEAKS, Biome.STONY_PEAKS,
            Biome.WINDSWEPT_HILLS, Biome.BEACH, Biome.RIVER, Biome.OCEAN);

    public BiomeManager(long seed) {
        this.shape = new TerrainShape(seed);
    }

    @Override
    public Biome getBiome(WorldInfo worldInfo, int x, int y, int z) {
        TerrainShape.Column col = shape.columnAt(x, z);
        double h = col.height();
        double temp = col.temperature();
        double humid = col.humidity();
        int sea = TerrainConfig.SEA_LEVEL;

        // Water bodies.
        if (col.river() && h <= sea + 1) {
            return Biome.RIVER;
        }
        if (h < sea - 6) {
            return Biome.OCEAN;
        }
        if (h <= sea + 1) {
            return Biome.BEACH;
        }

        // Mountains (climate already cooled by altitude in TerrainShape).
        if (h > sea + 95) {
            return temp < -0.1 ? Biome.JAGGED_PEAKS : Biome.STONY_PEAKS;
        }
        if (h > sea + 70) {
            if (temp < -0.2) {
                return Biome.SNOWY_SLOPES;
            }
            return humid > 0.0 ? Biome.GROVE : Biome.MEADOW;
        }
        if (h > sea + 45) {
            return temp < -0.2 ? Biome.WINDSWEPT_HILLS : Biome.MEADOW;
        }

        // Lowland climate.
        if (temp < -0.4) {
            return humid > 0.0 ? Biome.SNOWY_TAIGA : Biome.SNOWY_PLAINS;
        }
        if (temp < -0.15) {
            return Biome.TAIGA;
        }
        if (temp > 0.45) {
            return humid < -0.1 ? Biome.DESERT : Biome.SAVANNA;
        }
        // Warm, wet, low ground -> swamp.
        if (humid > 0.3 && h < sea + 5) {
            return Biome.SWAMP;
        }
        if (humid > 0.35) {
            return Biome.DARK_FOREST;
        }
        if (humid > 0.05) {
            return temp > 0.1 ? Biome.FOREST : Biome.BIRCH_FOREST;
        }
        return temp > 0.2 ? Biome.SUNFLOWER_PLAINS : Biome.PLAINS;
    }

    @Override
    public List<Biome> getBiomes(WorldInfo worldInfo) {
        return PALETTE;
    }
}
