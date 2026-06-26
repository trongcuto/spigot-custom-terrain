package com.trongcuto.terrain.generator;

import com.trongcuto.terrain.noise.SimplexNoise;
import org.bukkit.block.Biome;

/**
 * Biome Manager - Quản lý biome dựa trên tọa độ
 * Tạo ra các biome đa dạng và tự nhiên
 */
public class BiomeManager {
    private final SimplexNoise biomeNoise;

    public BiomeManager(long seed) {
        this.biomeNoise = new SimplexNoise(seed + 200);
    }

    /**
     * Xác định biome tại vị trí dựa trên tọa độ và độ cao
     * @param x tọa độ X
     * @param z tọa độ Z
     * @param height độ cao terrain
     * @return Biome thích hợp
     */
    public Biome getBiomeAtPosition(int x, int z, int height) {
        double noise = biomeNoise.noise(x * 0.01, 0, z * 0.01);
        double temperature = noise;

        // Phân loại biome theo nhiệt độ và độ cao
        if (height > 120) {
            // Núi rất cao
            return temperature > 0.3 ? Biome.SNOWY_PEAKS : Biome.JAGGED_PEAKS;
        } else if (height > 100) {
            // Cao nguyên
            return temperature > 0.5 ? Biome.MEADOW : Biome.TAIGA;
        } else if (height > 80) {
            // Độ cao vừa phải
            return temperature > 0.6 ? Biome.SAVANNA : Biome.FOREST;
        } else if (height > 65) {
            // Bề mặt
            return temperature > 0.7 ? Biome.PLAINS : Biome.BIRCH_FOREST;
        } else if (height == 63) {
            // Bãi biển
            return Biome.BEACH;
        } else {
            // Dưới nước
            return Biome.OCEAN;
        }
    }
}