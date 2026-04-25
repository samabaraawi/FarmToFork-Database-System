package com.farmtofork.phase3prototype;

import java.sql.*;
import java.util.*;

public class WarehouseDAO {

    public static List<WarehouseRow> getAll() throws Exception {
        String sql = "SELECT warehouseID, location, capacity FROM Warehouse ORDER BY warehouseID DESC";
        List<WarehouseRow> rows = new ArrayList<>();

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                rows.add(new WarehouseRow(
                        rs.getInt("warehouseID"),
                        rs.getString("location"),
                        rs.getInt("capacity")
                ));
            }
        }
        return rows;
    }

    public static WarehouseRow getByID(int warehouseID) throws Exception {
        String sql = "SELECT warehouseID, location, capacity FROM Warehouse WHERE warehouseID = ?";

        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql)) {

            ps.setInt(1, warehouseID);

            try (var rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                return new WarehouseRow(
                        rs.getInt("warehouseID"),
                        rs.getString("location"),
                        rs.getInt("capacity")
                );
            }
        }
    }

    public static void insert(String location, int capacity) throws Exception {
        String sql = "INSERT INTO Warehouse(location, capacity) VALUES (?, ?)";
        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql)) {

            ps.setString(1, location);
            ps.setInt(2, capacity);
            ps.executeUpdate();
        }
    }

    //  NEW: update warehouse
    public static void update(int warehouseID, String location, int capacity) throws Exception {
        String sql = "UPDATE Warehouse SET location = ?, capacity = ? WHERE warehouseID = ?";

        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql)) {

            ps.setString(1, location);
            ps.setInt(2, capacity);
            ps.setInt(3, warehouseID);

            int rows = ps.executeUpdate();
            if (rows == 0) throw new Exception("Warehouse not found");
        }
    }

    public static void delete(int warehouseID) throws Exception {
        String sql = "DELETE FROM Warehouse WHERE warehouseID = ?";

        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql)) {

            ps.setInt(1, warehouseID);

            int rows = ps.executeUpdate();
            if (rows == 0) throw new Exception("Warehouse not found");
        }
    }
}
