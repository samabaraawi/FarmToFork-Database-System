package com.farmtofork.phase3prototype;

import java.sql.*;
import java.util.*;

public class FarmerDAO {

    public static List<FarmerRow> getAll() throws Exception {
        String sql = "SELECT farmerID, name, region, contactInfo FROM Farmer ORDER BY farmerID DESC";
        List<FarmerRow> rows = new ArrayList<>();

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                rows.add(new FarmerRow(
                        rs.getInt("farmerID"),
                        rs.getString("name"),
                        rs.getString("region"),
                        rs.getString("contactInfo")
                ));
            }
        }
        return rows;
    }

    public static void insert(String name, String region, String contactInfo) throws Exception {
        String sql = "INSERT INTO Farmer(name, region, contactInfo) VALUES (?, ?, ?)";
        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, region);
            ps.setString(3, contactInfo);
            ps.executeUpdate();
        }
    }

    // ===============================
    // get by ID (search)
    // ===============================
    public static FarmerRow getByID(int farmerID) throws Exception {
        String sql = "SELECT farmerID, name, region, contactInfo FROM Farmer WHERE farmerID = ?";

        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql)) {

            ps.setInt(1, farmerID);

            try (var rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                return new FarmerRow(
                        rs.getInt("farmerID"),
                        rs.getString("name"),
                        rs.getString("region"),
                        rs.getString("contactInfo")
                );
            }
        }
    }

    // ===============================
    //  update
    // ===============================
    public static void update(int farmerID, String name, String region, String contactInfo) throws Exception {
        String sql = "UPDATE Farmer SET name = ?, region = ?, contactInfo = ? WHERE farmerID = ?";

        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, region);
            ps.setString(3, contactInfo);
            ps.setInt(4, farmerID);

            int rows = ps.executeUpdate();
            if (rows == 0) throw new Exception("Farmer not found");
        }
    }

    // ===============================
    // delete
    // ===============================
    public static void delete(int farmerID) throws Exception {
        String sql = "DELETE FROM Farmer WHERE farmerID = ?";

        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql)) {

            ps.setInt(1, farmerID);

            int rows = ps.executeUpdate();
            if (rows == 0) throw new Exception("Farmer not found");
        }
    }
}
