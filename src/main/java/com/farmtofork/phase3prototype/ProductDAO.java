package com.farmtofork.phase3prototype;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    public static List<ProductRow> getAllProducts() throws Exception {
        String sql = """
            SELECT productID, name, expiryDate, price, quantity, categoryID, warehouseID
            FROM Product
            ORDER BY productID DESC
        """;

        List<ProductRow> rows = new ArrayList<>();

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                rows.add(new ProductRow(
                        rs.getInt("productID"),
                        rs.getString("name"),
                        rs.getDate("expiryDate") == null ? "" : rs.getDate("expiryDate").toString(),
                        rs.getDouble("price"),
                        rs.getInt("quantity"),
                        rs.getInt("categoryID"),
                        rs.getInt("warehouseID")
                ));
            }
        }
        return rows;
    }

    public static void insertProduct(String name, java.sql.Date expiryDate, double price,
                                     int categoryID, int warehouseID) throws Exception {
        String sql = """
        INSERT INTO Product(name, expiryDate, price, quantity, categoryID, warehouseID)
        VALUES (?, ?, ?, 0, ?, ?)
    """;

        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setDate(2, expiryDate);
            ps.setDouble(3, price);
            ps.setInt(4, categoryID);
            ps.setInt(5, warehouseID);

            ps.executeUpdate();
        }
    }


    public static void updateProduct(int productID, double price) throws Exception {
        String sql = "UPDATE Product SET price = ? WHERE productID = ?";

        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql)) {

            ps.setDouble(1, price);
            ps.setInt(2, productID);

            int rows = ps.executeUpdate();
            if (rows == 0) throw new Exception("Product not found");
        }
    }


    // =========================================================
    //  (needed for ProductsView: search + delete)
    // =========================================================

    public static ProductRow getProductByID(int productID) throws Exception {
        String sql = """
            SELECT productID, name, expiryDate, price, quantity, categoryID, warehouseID
            FROM Product
            WHERE productID = ?
        """;

        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql)) {

            ps.setInt(1, productID);

            try (var rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                return new ProductRow(
                        rs.getInt("productID"),
                        rs.getString("name"),
                        rs.getDate("expiryDate") == null ? "" : rs.getDate("expiryDate").toString(),
                        rs.getDouble("price"),
                        rs.getInt("quantity"),
                        rs.getInt("categoryID"),
                        rs.getInt("warehouseID")
                );
            }
        }
    }

    public static void deleteProduct(int productID) throws Exception {

        String checkSql = """
        SELECT
          (SELECT COUNT(*) FROM Purchase WHERE productID = ?) AS pc,
          (SELECT COUNT(*) FROM Sale WHERE productID = ?) AS sc
    """;

        String delSql = "DELETE FROM Product WHERE productID = ?";

        try (var c = DBConnection.getConnection()) {

            int pc, sc;
            try (var ps = c.prepareStatement(checkSql)) {
                ps.setInt(1, productID);
                ps.setInt(2, productID);
                try (var rs = ps.executeQuery()) {
                    rs.next();
                    pc = rs.getInt("pc");
                    sc = rs.getInt("sc");
                }
            }

            if (pc > 0 || sc > 0) {
                throw new Exception("Cannot delete product. It has Purchases/Sales records.");
            }

            try (var ps = c.prepareStatement(delSql)) {
                ps.setInt(1, productID);
                int rows = ps.executeUpdate();
                if (rows == 0) throw new Exception("Product not found");
            }
        }
    }

}
