package org.cockshott;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.List;

public class BlockManager {
    private BlockDepreciationPlugin plugin;
    private List<BlockRecord> blocks;

    public BlockManager(BlockDepreciationPlugin plugin,List<BlockRecord> blocks) {
        this.plugin = plugin;
        this.blocks=blocks;
    }

    public void addBlock(Location location, String name, long expireTime) {
        // 向列表添加一个新的方块记录
        blocks.add(new BlockRecord(location, name,expireTime));
    }

    public void checkAndDepreciateBlocks() {
        if(blocks.isEmpty()) return;
        // 迭代所有记录的方块
        for (BlockRecord block:blocks) {
            Location loc = block.getLocation();
            if (loc.getWorld().isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)) {
                // 区块被加载，执行折旧逻辑
                if (block.getExpireTime() == 0) {
                    // 折旧时间已到，进行折旧操作，例如将方块变为空气
                    Block locBloic = loc.getBlock();
                    if (locBloic.getType().name() == block.getMaterialName()){
                        locBloic.setType(org.bukkit.Material.AIR);
                    }
                    // 从列表或数据库中移除此记录
                    plugin.getDatabaseOperations().removeBlockRecord(block);
                    blocks.remove(block);
                }else{
                    long interval = plugin.getConfig().getLong("detectiontime", 60);
                    block.setExpireTime(block.getExpireTime() - interval);
                }
            }
        }
    }
}
