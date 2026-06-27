package com.trongcuto.terrain.generator;

import com.trongcuto.terrain.config.TerrainConfig;
import com.trongcuto.terrain.noise.SimplexNoise;

/**
 * Computes the macro shape and climate of the world from several low-frequency
 * noise fields, in the spirit of Minecraft 1.18+ multi-noise generation.
 *
 * <p>Instead of a single uniform 3D density field (which makes every region look
 * the same), the terrain character varies by location:</p>
 * <ul>
 *   <li><b>continentalness</b> – the broad land elevation, giving lowlands and
 *       highlands;</li>
 *   <li><b>relief</b> – how mountainous a region is, so plains stay flat while
 *       mountain regions rise into tall, steep peaks;</li>
 *   <li><b>rivers</b> – a separate field carves river channels down toward the
 *       water table in lowland areas;</li>
 *   <li><b>temperature / humidity</b> – climate fields shared with the biome
 *       provider so biomes match the land (and peaks turn snowy).</li>
 * </ul>
 *
 * <p>The same instance (same seed) is used by both the chunk generator and the
 * {@link BiomeManager} so terrain and biomes always agree.</p>
 */
public final class TerrainShape {

    private final SimplexNoise continent;
    private final SimplexNoise relief;
    private final SimplexNoise mountain;
    private final SimplexNoise river;
    private final SimplexNoise detail;
    private final SimplexNoise temperature;
    private final SimplexNoise humidity;

    public TerrainShape(long seed) {
        this.continent = new SimplexNoise(seed + 1);
        this.relief = new SimplexNoise(seed + 2);
        this.mountain = new SimplexNoise(seed + 3);
        this.river = new SimplexNoise(seed + 4);
        this.detail = new SimplexNoise(seed + 5);
        this.temperature = new SimplexNoise(seed + 6);
        this.humidity = new SimplexNoise(seed + 7);
    }

    /**
     * Per-column terrain and climate summary.
     *
     * @param height      target surface height (Y) for this column
     * @param ruggedness  0 (flat) .. 1 (steep mountain), used to scale overhangs
     * @param temperature climate temperature (~[-1, 1], colder at altitude)
     * @param humidity    climate humidity (~[-1, 1])
     * @param river       whether this column sits in a river channel
     */
    public record Column(double height, double ruggedness,
                         double temperature, double humidity, boolean river) {
    }

    public Column columnAt(int x, int z) {
        // Broad land elevation.
        double cont = continent.octaves(
                x * TerrainConfig.CONTINENT_SCALE, 0.0, z * TerrainConfig.CONTINENT_SCALE,
                3, 0.5, 2.0);

        // How mountainous this region is. Cubing biases most of the map toward
        // flat lowland, with mountains only where the field is high.
        double relief01 = (relief.octaves(
                x * TerrainConfig.RELIEF_SCALE, 0.0, z * TerrainConfig.RELIEF_SCALE,
                2, 0.5, 2.0) + 1.0) * 0.5;
        double reliefShaped = relief01 * relief01 * relief01;

        // Sharp ridged peaks for the mountainous regions.
        double mtn = mountain.ridged(
                x * TerrainConfig.RELIEF_SCALE * 1.3, 0.0, z * TerrainConfig.RELIEF_SCALE * 1.3,
                4, 0.5, 2.1);

        double rugged = reliefShaped * mtn;

        double height = TerrainConfig.SEA_LEVEL + TerrainConfig.BASE_LAND
                + cont * TerrainConfig.CONTINENT_AMPLITUDE
                + rugged * TerrainConfig.MOUNTAIN_AMPLITUDE;

        // Carve rivers into lowland terrain only (so they don't slice mountains
        // into canyons).
        boolean isRiver = false;
        double riverRaw = Math.abs(river.octaves(
                x * TerrainConfig.RIVER_SCALE, 0.0, z * TerrainConfig.RIVER_SCALE,
                2, 0.5, 2.0));
        if (riverRaw < TerrainConfig.RIVER_WIDTH) {
            double edge = 1.0 - riverRaw / TerrainConfig.RIVER_WIDTH;     // 0..1
            double lowland = clamp((TerrainConfig.SEA_LEVEL + 22 - height) / 22.0, 0.0, 1.0);
            double strength = edge * edge * lowland;
            double target = TerrainConfig.SEA_LEVEL - 4;
            if (target < height) {
                height += (target - height) * strength;
            }
            isRiver = strength > 0.35 && height <= TerrainConfig.SEA_LEVEL + 1;
        }

        double temp = temperature.octaves(
                x * TerrainConfig.BIOME_SCALE, 0.0, z * TerrainConfig.BIOME_SCALE, 2, 0.5, 2.0);
        double humid = humidity.octaves(
                x * TerrainConfig.BIOME_SCALE, 0.0, z * TerrainConfig.BIOME_SCALE, 2, 0.5, 2.0);

        // Higher ground is colder.
        double altitude = height - (TerrainConfig.SEA_LEVEL + 35);
        if (altitude > 0) {
            temp -= altitude * 0.007;
        }

        return new Column(height, rugged, temp, humid, isRiver);
    }

    /**
     * Local 3D noise used to wobble the surface into overhangs and cliffs. Kept
     * small on flat terrain and stronger on mountains via the caller's amplitude.
     */
    public double detail(int x, int y, int z) {
        return detail.octaves(
                x * TerrainConfig.DETAIL_SCALE, y * TerrainConfig.DETAIL_SCALE,
                z * TerrainConfig.DETAIL_SCALE, 3, 0.5, 2.0);
    }

    private static double clamp(double v, double lo, double hi) {
        return v < lo ? lo : Math.min(v, hi);
    }
}
