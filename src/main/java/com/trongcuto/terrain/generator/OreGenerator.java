package com.trongcuto.terrain.generator;

import com.trongcuto.terrain.noise.SimplexNoise;
import org.bukkit.Material;
import java.util.Random;

/**
 * Ore Generator - Tạo ore tự động theo độ cao
 * Phân bố ore một cách tự nhiên và cân bằng
 */
public class OreGenerator {
    private final SimplexNoise oreNoise;
    private final Random random;

    public OreGenerator(long seed) {
        this.oreNoise = new SimplexNoise(seed + 100);
        this.random = new Random(seed + 100);
    }

    /**
     * Tính toán ore tại vị trí cụ thể
     * @param x tọa độ X
     * @param y tọa độ Y (chiều cao)
     * @param z tọa độ Z
     * @param baseBlock loại khối cơ sở
     * @return Loại ore hoặc stone nếu không phải ore
     */
    public Material getOreAtPosition(int x, int y, int z, Material baseBlock) {
        if (!baseBlock.equals(Material.STONE)) {
            return baseBlock;
        }

        double oreNoise = Math.abs(this.oreNoise.noise(x * 0.1, y * 0.08, z * 0.1));

        // Phân bố ore theo độ cao - từ thấp đến cao
        if (y < 15) {
            // Bedrock zone - Diamond rất hiếm
            if (oreNoise < 0.2) return Material.DIAMOND_ORE;
        } else if (y < 30) {
            // Deep zone - Diamond và Gold
            if (oreNoise < 0.15) return Material.DIAMOND_ORE;
            if (oreNoise < 0.35) return Material.GOLD_ORE;
        } else if (y < 60) {
            // Middle zone - Mix của tất cả
            if (oreNoise < 0.1) return Material.DIAMOND_ORE;
            if (oreNoise < 0.25) return Material.GOLD_ORE;
            if (oreNoise < 0.45) return Material.IRON_ORE;
        } else if (y < 100) {
            // Upper zone - Iron, Lapis, Coal
            if (oreNoise < 0.2) return Material.IRON_ORE;
            if (oreNoise < 0.35) return Material.LAPIS_ORE;
            if (oreNoise < 0.5) return Material.COAL_ORE;
        } else {
            // Surface zone - Chủ yếu Coal
            if (oreNoise < 0.3) return Material.COAL_ORE;
        }

        return Material.STONE;
    }
}