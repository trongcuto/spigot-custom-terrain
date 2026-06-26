package com.trongcuto.terrain.commands;

import com.trongcuto.terrain.config.TerrainConfig;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Terrain Command - Xử lý tất cả terrain-related commands
 */
public class TerrainCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("terrain.admin")) {
            sender.sendMessage("§c❌ Bạn không có quyền sử dụng lệnh này!");
            return true;
        }

        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        String subcommand = args[0].toLowerCase();

        switch (subcommand) {
            case "create":
                return handleCreate(sender, args);
            case "config":
                return handleConfig(sender, args);
            case "reset":
                return handleReset(sender);
            case "list":
                return handleList(sender);
            case "help":
                showHelp(sender);
                return true;
            default:
                sender.sendMessage("§c❌ Lệnh không tồn tại! Gõ /terrain help để xem trợ giúp.");
                return true;
        }
    }

    private boolean handleCreate(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§6Sử dụng: /terrain create <tên_world>");
            return true;
        }

        String worldName = args[1];
        sender.sendMessage("§a✓ Đang tạo world: §f" + worldName);
        sender.sendMessage("§e⏳ Vui lòng chờ...");

        // Thử tạo world với multiverse trước
        try {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                "mv create " + worldName + " NORMAL -g CustomTerrainGenerator");
        } catch (Exception e) {
            // Nếu không có multiverse, tạo bằng cách khác
            sender.sendMessage("§e💡 Multiverse không được tìm thấy. Vui lòng cài đặt Multiverse-Core.");
        }

        return true;
    }

    private boolean handleConfig(CommandSender sender, String[] args) {
        if (args.length < 3) {
            showConfigHelp(sender);
            return true;
        }

        String param = args[1].toLowerCase();
        String value = args[2];

        try {
            double doubleValue = Double.parseDouble(value);

            switch (param) {
                case "terrain-scale":
                    TerrainConfig.TERRAIN_SCALE = doubleValue;
                    break;
                case "terrain-scale-2":
                    TerrainConfig.TERRAIN_SCALE_2 = doubleValue;
                    break;
                case "cave-scale":
                    TerrainConfig.CAVE_SCALE = doubleValue;
                    break;
                case "cave-threshold":
                    TerrainConfig.CAVE_THRESHOLD = doubleValue;
                    break;
                case "detail-scale":
                    TerrainConfig.DETAIL_SCALE = doubleValue;
                    break;
                case "sea-level":
                    TerrainConfig.SEA_LEVEL = (int) doubleValue;
                    break;
                case "min-height":
                    TerrainConfig.MIN_HEIGHT = (int) doubleValue;
                    break;
                case "max-height":
                    TerrainConfig.MAX_HEIGHT = (int) doubleValue;
                    break;
                default:
                    sender.sendMessage("§c❌ Tham số không tồn tại!");
                    showConfigHelp(sender);
                    return true;
            }

            sender.sendMessage("§a✓ " + param + " đã được thiết lập thành: §f" + value);
            sender.sendMessage("§e💡 Những world mới sẽ sử dụng cài đặt này!");
            return true;

        } catch (NumberFormatException e) {
            sender.sendMessage("§c❌ Giá trị không hợp lệ! Vui lòng nhập số.");
            return true;
        }
    }

    private boolean handleReset(CommandSender sender) {
        TerrainConfig.resetDefaults();
        sender.sendMessage("§a✓ Tất cả cài đặt đã được reset về mặc định!");
        return true;
    }

    private boolean handleList(CommandSender sender) {
        sender.sendMessage("§6═════════════════════════════════════════════════════════");
        sender.sendMessage("§6 Cài đặt Terrain hiện tại:");
        sender.sendMessage("§6═════════════════════════════════════════════════════════");
        sender.sendMessage("§eTerrainScale: §f" + TerrainConfig.TERRAIN_SCALE);
        sender.sendMessage("§eTerrainScale2: §f" + TerrainConfig.TERRAIN_SCALE_2);
        sender.sendMessage("§eCaveScale: §f" + TerrainConfig.CAVE_SCALE);
        sender.sendMessage("§eCaveThreshold: §f" + TerrainConfig.CAVE_THRESHOLD);
        sender.sendMessage("§eDetailScale: §f" + TerrainConfig.DETAIL_SCALE);
        sender.sendMessage("§eSeaLevel: §f" + TerrainConfig.SEA_LEVEL);
        sender.sendMessage("§eMinHeight: §f" + TerrainConfig.MIN_HEIGHT);
        sender.sendMessage("§eMaxHeight: §f" + TerrainConfig.MAX_HEIGHT);
        sender.sendMessage("§6═════════════════════════════════════════════════════════");
        return true;
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage("\n§6═════════════════════════════════════════════════════════");
        sender.sendMessage("§6 Terrain Generator Commands:");
        sender.sendMessage("§6═════════════════════════════════════════════════════════");
        sender.sendMessage("§e/terrain create <tên> §8- Tạo world mới");
        sender.sendMessage("§e/terrain config <param> <giá_trị> §8- Cài đặt terrain");
        sender.sendMessage("§e/terrain list §8- Xem cài đặt hiện tại");
        sender.sendMessage("§e/terrain reset §8- Reset về mặc định");
        sender.sendMessage("§e/terrain help §8- Xem trợ giúp này");
        sender.sendMessage("§6═════════════════════════════════════════════════════════\n");
    }

    private void showConfigHelp(CommandSender sender) {
        sender.sendMessage("\n§6Các tham số cấu hình:");
        sender.sendMessage("§e• terrain-scale §8(0.005 - 0.05) - Độ lớn địa hình chính");
        sender.sendMessage("§e• terrain-scale-2 §8(0.001 - 0.02) - Địa hình phụ");
        sender.sendMessage("§e• cave-scale §8(0.05 - 0.15) - Độ lớn hang động");
        sender.sendMessage("§e• cave-threshold §8(0.2 - 0.6) - Tần suất hang động");
        sender.sendMessage("§e• detail-scale §8(0.01 - 0.1) - Chi tiết nhỏ");
        sender.sendMessage("§e• sea-level §8(50 - 80) - Mực nước");
        sender.sendMessage("§e• min-height §8(30 - 60) - Chiều cao tối thiểu");
        sender.sendMessage("§e• max-height §8(120 - 200) - Chiều cao tối đa\n");
    }
}