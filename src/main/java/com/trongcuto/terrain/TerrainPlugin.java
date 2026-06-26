package com.trongcuto.terrain;

import com.trongcuto.terrain.commands.TerrainCommand;
import com.trongcuto.terrain.generator.CustomChunkGenerator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class - Custom Terrain Generator
 * Tạo địa hình Minecraft tùy chỉnh với Simplex Noise 3D
 */
public class TerrainPlugin extends JavaPlugin {
    private CustomChunkGenerator generator;

    @Override
    public void onEnable() {
        getLogger().info("╔════════════════════════════════════════╗");
        getLogger().info("║ Custom Terrain Generator Plugin v1.0.0  ║");
        getLogger().info("║ Loading...                             ║");
        getLogger().info("╚════════════════════════════════════════╝");

        try {
            // Tạo generator
            generator = new CustomChunkGenerator(System.currentTimeMillis());
            getLogger().info("✓ SimplexNoise 3D engine khởi tạo thành công!");

            // Đăng ký command
            TerrainCommand terrainCmd = new TerrainCommand();
            if (getCommand("terrain") != null) {
                getCommand("terrain").setExecutor(terrainCmd);
                getLogger().info("✓ Terrain commands đã được đăng ký!");
            }

            getLogger().info("");
            getLogger().info("─────────────────────────────────────────────────────────");
            getLogger().info("  🏔️ Plugin đã sẵn sàng!");
            getLogger().info("  📝 Sử dụng: /terrain create <tên_world>");
            getLogger().info("  ⚙️  Cấu hình: /terrain config <param> <giá_trị>");
            getLogger().info("  📊 Xem cài đặt: /terrain list");
            getLogger().info("─────────────────────────────────────────────────────────");
            getLogger().info("");

        } catch (Exception e) {
            getLogger().severe("❌ Lỗi khi khởi tạo plugin!");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return generator;
    }

    @Override
    public void onDisable() {
        getLogger().info("\n─────────────────────────────────────────────────────────");
        getLogger().info("  👋 Custom Terrain Generator Disabled!");
        getLogger().info("─────────────────────────────────────────────────────────\n");
    }
}