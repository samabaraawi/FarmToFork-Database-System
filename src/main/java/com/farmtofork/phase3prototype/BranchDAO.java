package com.farmtofork.phase3prototype;

import java.sql.*;
import java.util.*;

public class BranchDAO {

    public static List<BranchRow> getAll() throws Exception {
        String sql = "SELECT branchID, name, city, address, phone FROM Branch ORDER BY branchID DESC";
        List<BranchRow> rows = new ArrayList<>();

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                rows.add(new BranchRow(
                        rs.getInt("branchID"),
                        rs.getString("name"),
                        rs.getString("city"),
                        rs.getString("address"),
                        rs.getString("phone")
                ));
            }
        }
        return rows;
    }

    public static void insert(String name, String city, String address, String phone) throws Exception {
        String sql = "INSERT INTO Branch(name, city, address, phone) VALUES (?, ?, ?, ?)";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, city);
            ps.setString(3, address);
            ps.setString(4, phone);

            ps.executeUpdate();
        }
    }

    public static int insertAndReturnID(String name, String city, String address, String phone) throws Exception {
        String sql = "INSERT INTO Branch(name, city, address, phone) VALUES (?, ?, ?, ?)";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name);
            ps.setString(2, city);
            ps.setString(3, address);
            ps.setString(4, phone);

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    public static BranchRow getByID(int branchID) throws Exception {
        String sql = "SELECT branchID, name, city, address, phone FROM Branch WHERE branchID = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, branchID);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                return new BranchRow(
                        rs.getInt("branchID"),
                        rs.getString("name"),
                        rs.getString("city"),
                        rs.getString("address"),
                        rs.getString("phone")
                );
            }
        }
    }

    public static void update(int branchID, String name, String city, String address, String phone) throws Exception {
        String sql = "UPDATE Branch SET name = ?, city = ?, address = ?, phone = ? WHERE branchID = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, city);
            ps.setString(3, address);
            ps.setString(4, phone);
            ps.setInt(5, branchID);

            int rows = ps.executeUpdate();
            if (rows == 0) throw new Exception("Branch not found");
        }
    }

    public static void delete(int branchID) throws Exception {
        String sql = "DELETE FROM Branch WHERE branchID = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, branchID);

            int rows = ps.executeUpdate();
            if (rows == 0) throw new Exception("Branch not found");
        }
    }
}
