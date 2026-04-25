package com.farmtofork.phase3prototype;

import java.sql.*;
import java.util.*;

public class ClientDAO {

    public static List<ClientRow> getAll() throws Exception {
        String sql = "SELECT clientID, name, type, contactInfo FROM Client ORDER BY clientID DESC";
        List<ClientRow> rows = new ArrayList<>();

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                rows.add(new ClientRow(
                        rs.getInt("clientID"),
                        rs.getString("name"),
                        rs.getString("type"),
                        rs.getString("contactInfo")
                ));
            }
        }
        return rows;
    }

    public static void insert(String name, String type, String contactInfo) throws Exception {
        String sql = "INSERT INTO Client(name, type, contactInfo) VALUES (?, ?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, type);
            ps.setString(3, contactInfo);

            ps.executeUpdate();
        }
    }

    public static int insertAndReturnID(String name, String type, String contactInfo) throws Exception {
        String sql = "INSERT INTO Client(name, type, contactInfo) VALUES (?, ?, ?)";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name);
            ps.setString(2, type);
            ps.setString(3, contactInfo);

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    // ===============================
    // get by ID (search)
    // ===============================
    public static ClientRow getByID(int clientID) throws Exception {
        String sql = "SELECT clientID, name, type, contactInfo FROM Client WHERE clientID = ?";

        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql)) {

            ps.setInt(1, clientID);

            try (var rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                return new ClientRow(
                        rs.getInt("clientID"),
                        rs.getString("name"),
                        rs.getString("type"),
                        rs.getString("contactInfo")
                );
            }
        }
    }

    // ===============================
    // update
    // ===============================
    public static void update(int clientID, String name, String type, String contactInfo) throws Exception {
        String sql = "UPDATE Client SET name = ?, type = ?, contactInfo = ? WHERE clientID = ?";

        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, type);
            ps.setString(3, contactInfo);
            ps.setInt(4, clientID);

            int rows = ps.executeUpdate();
            if (rows == 0) throw new Exception("Client not found");
        }
    }

    // ===============================
    // delete
    // ===============================
    public static void delete(int clientID) throws Exception {
        String sql = "DELETE FROM Client WHERE clientID = ?";

        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql)) {

            ps.setInt(1, clientID);

            int rows = ps.executeUpdate();
            if (rows == 0) throw new Exception("Client not found");
        }
    }
}
