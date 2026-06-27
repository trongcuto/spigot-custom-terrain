package com.trongcuto.terrain.config;

/**
 * Tunable parameters for the multi-noise terrain generator.
 *
 * <p>All fields are mutable statics so they can be adjusted at runtime via the
 * {@code /terrain config} command. Changes take effect for worlds (and chunks)
 * generated afterwards.</p>
 */
public final class TerrainConfig {

    private TerrainConfig() {
    }

    // --- Macro shape (low-frequency control fields) ------------------------

    /** Frequency of the continentalness field (overall land elevation). */
    public static double CONTINENT_SCALE = 0.0011;

    /** Frequency of the relief field (how mountainous a region is). */
    public static double RELIEF_SCALE = 0.0019;

    /** Frequency of the river field. */
    public static double RIVER_SCALE = 0.0015;

    /** Frequency of the local 3D detail noise (overhangs / cliffs). */
    public static double DETAIL_SCALE = 0.0175;

    /** Frequency of the temperature / humidity climate fields. */
    public static double BIOME_SCALE = 0.0016;

    // --- Heights -----------------------------------------------------------

    /** Average height of flat land above {@link #SEA_LEVEL}. */
    public static int BASE_LAND = 6;

    /** Vertical range contributed by continentalness (lowlands vs highlands). */
    public static double CONTINENT_AMPLITUDE = 16.0;

    /** Extra height of mountain regions above the base land. */
    public static double MOUNTAIN_AMPLITUDE = 95.0;

    /** Overhang strength on flat terrain. */
    public static double DETAIL_AMPLITUDE_BASE = 3.0;

    /** Additional overhang strength on the most mountainous terrain. */
    public static double DETAIL_AMPLITUDE_MOUNTAIN = 12.0;

    /** River channel half-width (larger == wider rivers). */
    public static double RIVER_WIDTH = 0.045;

    // --- World layout ------------------------------------------------------

    /** Water fills empty space at or below this Y. */
    public static int SEA_LEVEL = 63;

    /** Number of soil (dirt/sand) blocks below an exposed surface. */
    public static int DIRT_DEPTH = 4;

    /**
     * Reset every parameter to its default value.
     */
    public static void resetDefaults() {
        CONTINENT_SCALE = 0.0011;
        RELIEF_SCALE = 0.0019;
        RIVER_SCALE = 0.0015;
        DETAIL_SCALE = 0.0175;
        BIOME_SCALE = 0.0016;
        BASE_LAND = 6;
        CONTINENT_AMPLITUDE = 16.0;
        MOUNTAIN_AMPLITUDE = 95.0;
        DETAIL_AMPLITUDE_BASE = 3.0;
        DETAIL_AMPLITUDE_MOUNTAIN = 12.0;
        RIVER_WIDTH = 0.045;
        SEA_LEVEL = 63;
        DIRT_DEPTH = 4;
    }

    /** Names of every parameter accepted by {@link #applySetting(String, double)}. */
    public static final String[] PARAMETERS = {
            "continent-scale", "relief-scale", "river-scale", "detail-scale",
            "biome-scale", "base-land", "continent-amplitude", "mountain-amplitude",
            "detail-amplitude-base", "detail-amplitude-mountain", "river-width",
            "sea-level", "dirt-depth"
    };

    /**
     * Set a parameter by its hyphenated name.
     *
     * @return {@code true} if the name was recognised, {@code false} otherwise
     */
    public static boolean applySetting(String name, double value) {
        switch (name.toLowerCase()) {
            case "continent-scale" -> CONTINENT_SCALE = value;
            case "relief-scale" -> RELIEF_SCALE = value;
            case "river-scale" -> RIVER_SCALE = value;
            case "detail-scale" -> DETAIL_SCALE = value;
            case "biome-scale" -> BIOME_SCALE = value;
            case "base-land" -> BASE_LAND = (int) value;
            case "continent-amplitude" -> CONTINENT_AMPLITUDE = value;
            case "mountain-amplitude" -> MOUNTAIN_AMPLITUDE = value;
            case "detail-amplitude-base" -> DETAIL_AMPLITUDE_BASE = value;
            case "detail-amplitude-mountain" -> DETAIL_AMPLITUDE_MOUNTAIN = value;
            case "river-width" -> RIVER_WIDTH = value;
            case "sea-level" -> SEA_LEVEL = (int) value;
            case "dirt-depth" -> DIRT_DEPTH = (int) value;
            default -> {
                return false;
            }
        }
        return true;
    }

    public static String getCurrentSettings() {
        return String.format(
                "continent-scale=%.4f, relief-scale=%.4f, river-scale=%.4f, "
                        + "detail-scale=%.4f, biome-scale=%.4f, base-land=%d, "
                        + "continent-amplitude=%.1f, mountain-amplitude=%.1f, "
                        + "detail-amplitude-base=%.1f, detail-amplitude-mountain=%.1f, "
                        + "river-width=%.3f, sea-level=%d, dirt-depth=%d",
                CONTINENT_SCALE, RELIEF_SCALE, RIVER_SCALE, DETAIL_SCALE, BIOME_SCALE,
                BASE_LAND, CONTINENT_AMPLITUDE, MOUNTAIN_AMPLITUDE,
                DETAIL_AMPLITUDE_BASE, DETAIL_AMPLITUDE_MOUNTAIN,
                RIVER_WIDTH, SEA_LEVEL, DIRT_DEPTH);
    }
}
