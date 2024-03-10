package org.cockshott;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.List;
import java.util.Map;

public class BlockPlaceListener implements Listener {
    private BlockDepreciationPlugin plugin;
    private BlockManager blockManager;

    public BlockPlaceListener(BlockDepreciationPlugin plugin, BlockManager blockManager) {
        this.plugin = plugin;
        this.blockManager = blockManager;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        // 获取放置的方块类型
        String placedBlockTypeName = event.getBlockPlaced().toString();
        String blockName = BlockTypeExtractor.extractBlockType(placedBlockTypeName).toLowerCase();
        // 读取配置中的方块类型及其对应的到期时间
        Map<String, Object> depreciationBlocks = plugin.getConfig().getConfigurationSection("depreciationBlocks").getValues(false);

        for (String key : depreciationBlocks.keySet()) {
            // 检查放置的方块是否是配置中指定的需要折旧的方块之一
            if (blockName.equalsIgnoreCase(key.toLowerCase())) {
                // 读取该方块对应的到期时间
                long expireTime = Long.parseLong(depreciationBlocks.get(key).toString())*12*60;
                // 添加方块记录
                blockManager.addBlock(event.getBlock().getLocation(), blockName, expireTime);
                break; // 匹配到配置中的一种方块后即可停止检查
            }
        }
    }
}
