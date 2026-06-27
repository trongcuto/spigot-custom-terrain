package com.trongcuto.terrain.config;

/**
 * Tunable parameters for the 3D density-field terrain generator.
 *
 * <p>All fields are mutable statics so they can be adjusted at runtime via the
 * {@code /terrain config} command. Changes take effect for worlds generated
 * afterwards (chunks already on disk keep the settings they were built with).</p>
 */
public final class TerrainConfig {

    private TerrainConfig() {
    }

    // --- 3D density noise --------------------------------------------------

    /** Horizontal frequency of the main density noise. Larger == smaller features. */
    public static double HORIZONTAL_SCALE = 0.0125;

    /** Vertical frequency of the main density noise. */
    public static double VERTICAL_SCALE = 0.020;

    /**
     * How strongly altitude pulls the density toward "air". Larger values give
     * flatter, lower terrain; smaller values give taller, more chaotic terrain
     * with more overhangs and floating chunks.
     */
    public static double SQUASH_FACTOR = 0.018;

    /** Altitude the squash gradient pivots around (rough average ground level). */
    public static int TERRAIN_CENTER = 80;

    /** Weight of the ridged-multifractal contribution (sharp mountain ridges). */
    public static double RIDGE_WEIGHT = 0.6;

    // --- World layout ------------------------------------------------------

    /** Water fills empty space at or below this Y. */
    public static int SEA_LEVEL = 63;

    /** Number of soil (dirt/sand) blocks below an exposed surface. */
    public static int DIRT_DEPTH = 4;

    // --- Biomes ------------------------------------------------------------

    /** Frequency of the biome (temperature/humidity) noise. */
    public static double BIOME_SCALE = 0.0035;

    // --- Trees -------------------------------------------------------------

    /**
     * Tree rarity threshold (0..1). Higher == fewer trees. A tree is attempted
     * only where the structure noise exceeds this value.
     */
    public static double TREE_THRESHOLD = 0.82;

    /**
     * Reset every parameter to its default value.
     */
    public static void resetDefaults() {
        HORIZONTAL_SCALE = 0.0125;
        VERTICAL_SCALE = 0.020;
        SQUASH_FACTOR = 0.018;
        TERRAIN_CENTER = 80;
        RIDGE_WEIGHT = 0.6;
        SEA_LEVEL = 63;
        DIRT_DEPTH = 4;
        BIOME_SCALE = 0.0035;
        TREE_THRESHOLD = 0.82;
    }

    /** Names of every parameter accepted by {@link #applySetting(String, double)}. */
    public static final String[] PARAMETERS = {
            "horizontal-scale", "vertical-scale", "squash", "terrain-center",
            "ridge-weight", "sea-level", "dirt-depth",
            "biome-scale", "tree-threshold"
    };

    /**
     * Set a parameter by its hyphenated name.
     *
     * @return {@code true} if the name was recognised, {@code false} otherwise
     */
    public static boolean applySetting(String name, double value) {
        switch (name.toLowerCase()) {
            case "horizontal-scale" -> HORIZONTAL_SCALE = value;
            case "vertical-scale" -> VERTICAL_SCALE = value;
            case "squash" -> SQUASH_FACTOR = value;
            case "terrain-center" -> TERRAIN_CENTER = (int) value;
            case "ridge-weight" -> RIDGE_WEIGHT = value;
            case "sea-level" -> SEA_LEVEL = (int) value;
            case "dirt-depth" -> DIRT_DEPTH = (int) value;
            case "biome-scale" -> BIOME_SCALE = value;
            case "tree-threshold" -> TREE_THRESHOLD = value;
            default -> {
                return false;
            }
        }
        return true;
    }

    public static String getCurrentSettings() {
        return String.format(
                "horizontal-scale=%.4f, vertical-scale=%.4f, squash=%.4f, "
                        + "terrain-center=%d, ridge-weight=%.2f, sea-level=%d, "
                        + "dirt-depth=%d, biome-scale=%.4f, tree-threshold=%.2f",
                HORIZONTAL_SCALE, VERTICAL_SCALE, SQUASH_FACTOR,
                TERRAIN_CENTER, RIDGE_WEIGHT, SEA_LEVEL,
                DIRT_DEPTH, BIOME_SCALE, TREE_THRESHOLD);
    }
}
