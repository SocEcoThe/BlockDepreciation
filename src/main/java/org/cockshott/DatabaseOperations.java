package org.cockshott;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.Location;

public class DatabaseOperations {
    private DataSource hikari;

    public DatabaseOperations(DataSource hikari) {
        this.hikari = hikari;
    }

    public void addingData(List<BlockRecord> blocks) {
        if(blocks.isEmpty()) return;
        String sql = "INSERT INTO depreciation_table (world, x, y, z, materialName, expireTime) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE expireTime = LEAST(expireTime, VALUES(expireTime))";
        Connection connection = null;
        try {
            connection = hikari.getConnection();
            connection.setAutoCommit(false);
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                for (BlockRecord block : blocks) {
                    stmt.setString(1, block.getLocation().getWorld().getName());
                    stmt.setInt(2, block.getLocation().getBlockX());
                    stmt.setInt(3, block.getLocation().getBlockY());
                    stmt.setInt(4, block.getLocation().getBlockZ());
                    stmt.setString(5, block.getMaterialName());
                    stmt.setLong(6, block.getExpireTime());
                    stmt.executeUpdate();
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                e.printStackTrace();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void removeBlockRecord(BlockRecord block) {
        String sql = "DELETE FROM depreciation_table WHERE world = ? AND x = ? AND y = ? AND z = ? AND materialName = ?";
        Connection connection = null;

        try {
            connection = hikari.getConnection();
            connection.setAutoCommit(false);
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, block.getLocation().getWorld().getName());
                stmt.setInt(2, block.getLocation().getBlockX());
                stmt.setInt(3, block.getLocation().getBlockY());
                stmt.setInt(4, block.getLocation().getBlockZ());
                stmt.setString(5, block.getMaterialName());
                stmt.executeUpdate();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                e.printStackTrace();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public List<BlockRecord> loadBlockRecords() {
        List<BlockRecord> records = new ArrayList<>();
        String sql = "SELECT world, x, y, z, materialName, expireTime FROM depreciation_table";
        try (Connection conn = hikari.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String worldName = rs.getString("world");
                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    continue;
                }
                int x = rs.getInt("x");
                int y = rs.getInt("y");
                int z = rs.getInt("z");
                Location location = new Location(world, x, y, z);
                String materialName = rs.getString("materialName");
                long expireTime = rs.getLong("expireTime");

                records.add(new BlockRecord(location, materialName, expireTime));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return records;
    }
}
