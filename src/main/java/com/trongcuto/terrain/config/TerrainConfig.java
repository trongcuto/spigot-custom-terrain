package com.trongcuto.terrain.config;

/**
 * Cấu hình chung cho Terrain Generator
 * Tất cả các tham số có thể được điều chỉnh qua commands
 */
public class TerrainConfig {
    // Terrain generation parameters
    public static double TERRAIN_SCALE = 0.015;        // Độ lớn địa hình chính
    public static double TERRAIN_SCALE_2 = 0.005;      // Secondary terrain scale
    public static double DETAIL_SCALE = 0.05;          // Chi tiết nhỏ

    // Cave parameters
    public static double CAVE_SCALE = 0.08;            // Độ lớn hang động
    public static double CAVE_THRESHOLD = 0.4;         // Ngưỡng hang động
    public static int MIN_CAVE_HEIGHT = 15;            // Chiều cao tối thiểu hang động
    public static int MAX_CAVE_HEIGHT = 100;           // Chiều cao tối đa hang động

    // World parameters
    public static int SEA_LEVEL = 63;                  // Mức nước
    public static int MIN_HEIGHT = 40;                 // Chiều cao tối thiểu terrain
    public static int MAX_HEIGHT = 140;                // Chiều cao tối đa terrain

    // Biome parameters
    public static double BIOME_SCALE = 0.01;           // Tỷ lệ biome

    // Structure parameters
    public static double STRUCTURE_RARITY = 0.7;       // Tần suất cấu trúc (cây, v.v.)

    /**
     * Reset tất cả về mặc định
     */
    public static void resetDefaults() {
        TERRAIN_SCALE = 0.015;
        TERRAIN_SCALE_2 = 0.005;
        DETAIL_SCALE = 0.05;
        CAVE_SCALE = 0.08;
        CAVE_THRESHOLD = 0.4;
        MIN_CAVE_HEIGHT = 15;
        MAX_CAVE_HEIGHT = 100;
        SEA_LEVEL = 63;
        MIN_HEIGHT = 40;
        MAX_HEIGHT = 140;
        BIOME_SCALE = 0.01;
        STRUCTURE_RARITY = 0.7;
    }

    /**
     * In ra tất cả cài đặt hiện tại
     */
    public static String getCurrentSettings() {
        return String.format(
            "TERRAIN_SCALE: %.4f, " +
            "CAVE_SCALE: %.4f, " +
            "CAVE_THRESHOLD: %.4f, " +
            "DETAIL_SCALE: %.4f, " +
            "SEA_LEVEL: %d, " +
            "MIN_HEIGHT: %d, " +
            "MAX_HEIGHT: %d",
            TERRAIN_SCALE, CAVE_SCALE, CAVE_THRESHOLD, DETAIL_SCALE,
            SEA_LEVEL, MIN_HEIGHT, MAX_HEIGHT
        );
    }
}