package com.trongcuto.terrain.generator;

import com.trongcuto.terrain.config.TerrainConfig;
import com.trongcuto.terrain.noise.SimplexNoise;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;

import java.util.Random;

/**
 * Custom Chunk Generator - Chunk Generator chính sử dụng Simplex Noise 3D
 * Tạo địa hình, ore, biomes, structures tất cả trong một class
 */
public class CustomChunkGenerator extends ChunkGenerator {
    private final SimplexNoise terrainNoise;
    private final SimplexNoise caveNoise;
    private final SimplexNoise detailNoise;
    private final OreGenerator oreGenerator;
    private final BiomeManager biomeManager;
    private final StructureGenerator structureGenerator;

    public CustomChunkGenerator(long seed) {
        this.terrainNoise = new SimplexNoise(seed);
        this.caveNoise = new SimplexNoise(seed + 1);
        this.detailNoise = new SimplexNoise(seed + 2);
        this.oreGenerator = new OreGenerator(seed);
        this.biomeManager = new BiomeManager(seed);
        this.structureGenerator = new StructureGenerator(seed);
    }

    @Override
    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
        ChunkData chunk = createChunkData(world);

        // Tạo heightmap cho chunk
        int[][] heightMap = new int[16][16];
        Biome[][] biomeMap = new Biome[16][16];

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = chunkX * 16 + x;
                int worldZ = chunkZ * 16 + z;
                heightMap[x][z] = getHeight(worldX, worldZ);
                biomeMap[x][z] = biomeManager.getBiomeAtPosition(worldX, worldZ, heightMap[x][z]);
                biome.setBiome(x, z, biomeMap[x][z]);
            }
        }

        // Điền khối vào chunk
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = chunkX * 16 + x;
                int worldZ = chunkZ * 16 + z;
                int height = heightMap[x][z];

                for (int y = 0; y < 256; y++) {
                    Material block = getBlockMaterial(worldX, y, worldZ, height);
                    chunk.setBlock(x, y, z, block);
                }

                // Tạo cây
                structureGenerator.generateTree(chunkX, chunkZ, x, z, height, chunk);
            }
        }

        return chunk;
    }

    /**
     * Tính chiều cao terrain tại vị trí X, Z
     */
    private int getHeight(int x, int z) {
        // Tạo terrain với multiple octaves của noise
        double noise1 = terrainNoise.noise(x * TerrainConfig.TERRAIN_SCALE, 0, z * TerrainConfig.TERRAIN_SCALE);
        double noise2 = terrainNoise.noise(x * TerrainConfig.TERRAIN_SCALE_2, 0, z * TerrainConfig.TERRAIN_SCALE_2) * 0.5;
        double noise3 = detailNoise.noise(x * TerrainConfig.DETAIL_SCALE, 0, z * TerrainConfig.DETAIL_SCALE) * 0.3;

        double combined = noise1 + noise2 + noise3;
        int baseHeight = (int) ((combined + 1) * 32) + TerrainConfig.SEA_LEVEL;

        return Math.max(TerrainConfig.MIN_HEIGHT, Math.min(TerrainConfig.MAX_HEIGHT, baseHeight));
    }

    /**
     * Xác định loại khối tại vị trí
     */
    private Material getBlockMaterial(int x, int y, int z, int terrainHeight) {
        // Hang động
        if (y >= TerrainConfig.MIN_CAVE_HEIGHT && y <= TerrainConfig.MAX_CAVE_HEIGHT) {
            double caveNoise = Math.abs(this.caveNoise.noise(x * TerrainConfig.CAVE_SCALE, y * 0.05, z * TerrainConfig.CAVE_SCALE));
            if (caveNoise < TerrainConfig.CAVE_THRESHOLD || structureGenerator.isCavern(x, y, z)) {
                return Material.CAVE_AIR;
            }
        }

        // Trên mặt đất
        if (y >= terrainHeight) {
            if (y == terrainHeight) {
                return Material.GRASS_BLOCK;
            } else if (y < TerrainConfig.SEA_LEVEL) {
                return Material.WATER;
            } else {
                return Material.AIR;
            }
        }

        // Dưới mặt đất
        Material baseBlock;
        if (y > terrainHeight - 4) {
            baseBlock = Material.DIRT;
        } else if (y > terrainHeight - 10) {
            baseBlock = Material.STONE;
        } else if (y > 10) {
            baseBlock = Material.STONE;
        } else {
            baseBlock = Material.BEDROCK;
        }

        // Áp dụng ore generation
        if (baseBlock.equals(Material.STONE)) {
            return oreGenerator.getOreAtPosition(x, y, z, baseBlock);
        }

        return baseBlock;
    }

    @Override
    public boolean canSpawn(World world, int x, int z) {
        return true;
    }
}