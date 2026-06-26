package com.trongcuto.terrain.generator;

import com.trongcuto.terrain.config.TerrainConfig;
import com.trongcuto.terrain.noise.SimplexNoise;
import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;

import java.util.Arrays;
import java.util.List;

/**
 * Assigns biomes from two low-frequency noise fields (temperature and humidity)
 * plus altitude, so vanilla decoration (grass, flowers, foliage colour) matches
 * the terrain. Implemented as a {@link BiomeProvider} for the modern generator
 * API.
 */
public final class BiomeManager extends BiomeProvider {

    private final SimplexNoise temperatureNoise;
    private final SimplexNoise humidityNoise;

    private static final List<Biome> PALETTE = Arrays.asList(
            Biome.PLAINS, Biome.FOREST, Biome.BIRCH_FOREST, Biome.DARK_FOREST,
            Biome.SAVANNA, Biome.DESERT, Biome.TAIGA, Biome.SNOWY_TAIGA,
            Biome.MEADOW, Biome.JAGGED_PEAKS, Biome.SNOWY_SLOPES,
            Biome.BEACH, Biome.OCEAN);

    public BiomeManager(long seed) {
        this.temperatureNoise = new SimplexNoise(seed + 200);
        this.humidityNoise = new SimplexNoise(seed + 250);
    }

    @Override
    public Biome getBiome(WorldInfo worldInfo, int x, int y, int z) {
        if (y < TerrainConfig.SEA_LEVEL - 2) {
            return Biome.OCEAN;
        }
        if (y <= TerrainConfig.SEA_LEVEL + 2) {
            return Biome.BEACH;
        }

        double temperature = temperatureNoise.noise(x * TerrainConfig.BIOME_SCALE, z * TerrainConfig.BIOME_SCALE);
        double humidity = humidityNoise.noise(x * TerrainConfig.BIOME_SCALE, z * TerrainConfig.BIOME_SCALE);

        // High altitude -> mountain biomes regardless of climate.
        if (y > 140) {
            return temperature < -0.2 ? Biome.JAGGED_PEAKS : Biome.SNOWY_SLOPES;
        }
        if (y > 115) {
            return temperature > 0.2 ? Biome.MEADOW : Biome.SNOWY_SLOPES;
        }

        if (temperature < -0.35) {
            return humidity > 0.0 ? Biome.SNOWY_TAIGA : Biome.TAIGA;
        }
        if (temperature > 0.4) {
            return humidity < -0.1 ? Biome.DESERT : Biome.SAVANNA;
        }
        if (humidity > 0.35) {
            return Biome.DARK_FOREST;
        }
        if (humidity > 0.0) {
            return temperature > 0.1 ? Biome.FOREST : Biome.BIRCH_FOREST;
        }
        return Biome.PLAINS;
    }

    @Override
    public List<Biome> getBiomes(WorldInfo worldInfo) {
        return PALETTE;
    }
}
