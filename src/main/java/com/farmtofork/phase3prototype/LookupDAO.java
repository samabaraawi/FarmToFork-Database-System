package com.farmtofork.phase3prototype;

import java.sql.*;
import java.util.*;

public class LookupDAO {

    public static List<IdName> getProducts() throws Exception {
        String sql = "SELECT productID, name FROM Product ORDER BY name";
        return fetch(sql, "productID", "name");
    }

    public static List<IdName> getClients() throws Exception {
        String sql = "SELECT clientID, name FROM Client ORDER BY name";
        return fetch(sql, "clientID", "name");
    }

    public static List<IdName> getFarmers() throws Exception {
        String sql = "SELECT farmerID, name FROM Farmer ORDER BY name";
        return fetch(sql, "farmerID", "name");
    }

    public static List<IdName> getBranches() throws Exception {
        String sql = "SELECT branchID, name FROM Branch ORDER BY name";
        return fetch(sql, "branchID", "name");
    }

    private static List<IdName> fetch(String sql, String idCol, String nameCol) throws Exception {
        List<IdName> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new IdName(rs.getInt(idCol), rs.getString(nameCol)));
            }
        }
        return list;
    }
}

