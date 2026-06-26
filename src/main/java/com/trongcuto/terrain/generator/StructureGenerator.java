package com.trongcuto.terrain.generator;

import com.trongcuto.terrain.noise.SimplexNoise;
import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator;

/**
 * Structure Generator - Tạo các cấu trúc như cây, hang động, v.v.
 */
public class StructureGenerator {
    private final SimplexNoise structureNoise;

    public StructureGenerator(long seed) {
        this.structureNoise = new SimplexNoise(seed + 300);
    }

    /**
     * Tạo cây (Tree) tại vị trí
     * @param chunkX chunk X
     * @param chunkZ chunk Z
     * @param x tọa độ X trong chunk
     * @param z tọa độ Z trong chunk
     * @param height chiều cao bề mặt
     * @param chunk ChunkData để điền khối
     */
    public void generateTree(int chunkX, int chunkZ, int x, int z, int height, ChunkGenerator.ChunkData chunk) {
        if (height > 100 || height < 63) return; // Chỉ tạo cây trên bề mặt bình thường

        double noise = Math.abs(structureNoise.noise(chunkX * 16 + x, 0, chunkZ * 16 + z));

        if (noise > 0.7) {
            // Tạo thân cây
            int treeHeight = 5 + (int)(noise * 5);
            for (int y = height; y < height + treeHeight && y < 255; y++) {
                chunk.setBlock(x, y, z, Material.OAK_LOG);
            }

            // Tạo lá cây
            int leafHeight = height + treeHeight;
            for (int dy = -2; dy <= 0; dy++) {
                for (int dx = -2; dx <= 2; dx++) {
                    for (int dz = -2; dz <= 2; dz++) {
                        if (dx*dx + dz*dz <= 4) {
                            int ly = leafHeight + dy;
                            if (ly < 256 && ly > 0 && x+dx >= 0 && x+dx < 16 && z+dz >= 0 && z+dz < 16) {
                                chunk.setBlock(x+dx, ly, z+dz, Material.OAK_LEAVES);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Kiểm tra xem vị trí có phải cavern không
     * @param x tọa độ X
     * @param y tọa độ Y
     * @param z tọa độ Z
     * @return true nếu là cavern
     */
    public boolean isCavern(int x, int y, int z) {
        double caveNoise = Math.abs(this.structureNoise.noise(x * 0.08, y * 0.05, z * 0.08));
        return caveNoise < 0.35;
    }
}