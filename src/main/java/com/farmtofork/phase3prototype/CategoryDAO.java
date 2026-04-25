package com.farmtofork.phase3prototype;

import java.sql.*;
import java.util.*;

public class CategoryDAO {

    public static List<CategoryRow> getAll() throws Exception {
        String sql = "SELECT categoryID, name, description FROM Category ORDER BY categoryID DESC";
        List<CategoryRow> rows = new ArrayList<>();

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                rows.add(new CategoryRow(
                        rs.getInt("categoryID"),
                        rs.getString("name"),
                        rs.getString("description")
                ));
            }
        }
        return rows;
    }

    public static void insert(String name, String description) throws Exception {
        String sql = "INSERT INTO Category(name, description) VALUES (?, ?)";
        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, description);
            ps.executeUpdate();
        }
    }

    public static CategoryRow getByID(int categoryID) throws Exception {
        String sql = "SELECT categoryID, name, description FROM Category WHERE categoryID = ?";
        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql)) {

            ps.setInt(1, categoryID);

            try (var rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                return new CategoryRow(
                        rs.getInt("categoryID"),
                        rs.getString("name"),
                        rs.getString("description")
                );
            }
        }
    }

    public static void update(int categoryID, String name, String description) throws Exception {
        String sql = "UPDATE Category SET name = ?, description = ? WHERE categoryID = ?";

        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, description);
            ps.setInt(3, categoryID);

            int rows = ps.executeUpdate();
            if (rows == 0) throw new Exception("Category not found");
        }
    }

    public static void delete(int categoryID) throws Exception {
        String sql = "DELETE FROM Category WHERE categoryID = ?";

        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql)) {

            ps.setInt(1, categoryID);

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new Exception("Category not found");
            }
        }
    }
}
