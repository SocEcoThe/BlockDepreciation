package org.cockshott;
import org.bukkit.Location;

public class BlockRecord {
    // 方块的位置
    private Location location;
    // 方块的名字
    private String materialName;
    // 方块到期的时间戳
    private long expireTime;

    public BlockRecord(Location location, String materialName, long expireTime) {
        this.location = location;
        this.materialName = materialName;
        this.expireTime = expireTime;
    }

    public Location getLocation() {
        return location;
    }

    public String getMaterialName() {
        return materialName;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }
}
