package org.cockshott;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;
import java.sql.Connection;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import com.zjyl1994.minecraftplugin.multicurrency.MultiCurrencyPlugin;


public class BlockDepreciationPlugin extends JavaPlugin {
    // 块管理器，负责处理方块的记录和折旧
    private BlockManager blockManager;
    private DataSource hikari;
    private DatabaseOperations DBOP;
    private List<BlockRecord> blocks;

    @Override
    public void onEnable() {
        // 确保插件启动时加载默认配置文件
        saveDefaultConfig(); 
        // 连接sql服务器
        linkedServer();
        DBOP = new DatabaseOperations(hikari);
        blocks = DBOP.loadBlockRecords();
        // 初始化BlockManager实例
        blockManager = new BlockManager(this,blocks);
        // 注册BlockPlaceEvent事件的监听器
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(this, blockManager), this);
        
        // 设置一个定时任务，定期检查并执行折旧逻辑
        long intervalInSeconds = this.getConfig().getLong("detectiontime", 60); // 从设置里读取间隔，默认60秒
        long intervalInTicks = intervalInSeconds * 20; // 将秒转换为刻，1秒=20刻
        new BukkitRunnable() {
            @Override
            public void run() {
                blockManager.checkAndDepreciateBlocks();
                DBOP.addingData(blocks);
            }
        }.runTaskTimer(this, 0L, intervalInTicks); 
    }

    @Override
    public void onDisable() {
        // 插件禁用时可以执行的逻辑（如果需要）
    }

    public BlockManager getBlockManager() {
        return blockManager;
    }

    public DataSource getHikariDataSource() {
        return hikari;
    }

    public DatabaseOperations getDatabaseOperations() {
        return DBOP;
    }

    public void linkedServer(){
        hikari = MultiCurrencyPlugin.getInstance().getHikari();

        try (Connection connection = hikari.getConnection()) {
            String[] create = {
                "CREATE TABLE IF NOT EXISTS `depreciation_table` ("+
                 "`id` INT(11) NOT NULL AUTO_INCREMENT,"+
                 "`world` VARCHAR(255) NOT NULL,"+
                 "`x` INT NOT NULL,"+
                 "`y` INT NOT NULL,"+
                 "`z` INT NOT NULL,"+
                 "`materialName` VARCHAR(255) NOT NULL,"+
                 "`expireTime` BIGINT NOT NULL,"+
                 "PRIMARY KEY (`id`),"+
                 "UNIQUE INDEX idx_unique_block (world, x, y, z, materialName)"+
                 ") ENGINE=INNODB DEFAULT CHARSET=utf8 COMMENT='折旧表';"
            };
            for (String s : create) {
                PreparedStatement preparedStatement = connection.prepareStatement(s);
                preparedStatement.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
