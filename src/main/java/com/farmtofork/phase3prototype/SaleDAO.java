package com.farmtofork.phase3prototype;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SaleDAO {

    public static void insertSale(java.sql.Date saleDate, int quantity, double unitPrice,
                                  int clientID, int productID, int branchID) throws Exception {

        if (quantity <= 0) throw new Exception("Quantity must be > 0");
        if (unitPrice <= 0) throw new Exception("Unit price must be > 0");

        String lockSql = "SELECT quantity FROM Product WHERE productID = ? FOR UPDATE";
        String insertSql = """
        INSERT INTO Sale(saleDate, quantity, unitPrice, totalRevenue, clientID, productID, branchID)
        VALUES (?, ?, ?, ?, ?, ?, ?)
    """;
        String updateSql = "UPDATE Product SET quantity = quantity - ? WHERE productID = ?";

        double totalRevenue = quantity * unitPrice;

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // 1) lock product row + check stock
                int currentQty;
                try (PreparedStatement psLock = conn.prepareStatement(lockSql)) {
                    psLock.setInt(1, productID);
                    try (ResultSet rs = psLock.executeQuery()) {
                        if (!rs.next()) throw new Exception("ProductID not found");
                        currentQty = rs.getInt("quantity");
                    }
                }

                if (currentQty < quantity) {
                    throw new Exception("Not enough stock. Current quantity = " + currentQty);
                }

                // 2) insert sale
                try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
                    psInsert.setDate(1, saleDate);
                    psInsert.setInt(2, quantity);
                    psInsert.setDouble(3, unitPrice);
                    psInsert.setDouble(4, totalRevenue);
                    psInsert.setInt(5, clientID);
                    psInsert.setInt(6, productID);
                    psInsert.setInt(7, branchID);
                    psInsert.executeUpdate();
                }

                // 3) decrease stock
                try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                    psUpdate.setInt(1, quantity);
                    psUpdate.setInt(2, productID);
                    psUpdate.executeUpdate();
                }

                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }


    public static List<SaleRow> getAllSales() throws Exception {
        String sql = """
            SELECT saleID, saleDate, quantity, unitPrice, totalRevenue, clientID, productID, branchID
            FROM Sale
            ORDER BY saleDate DESC, saleID DESC
        """;

        List<SaleRow> rows = new ArrayList<>();

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                rows.add(new SaleRow(
                        rs.getInt("saleID"),
                        rs.getDate("saleDate").toString(),
                        rs.getInt("quantity"),
                        rs.getDouble("unitPrice"),
                        rs.getDouble("totalRevenue"),
                        rs.getInt("clientID"),
                        rs.getInt("productID"),
                        rs.getInt("branchID")
                ));
            }
        }
        return rows;
    }

    //  DELETE sale + restore stock (stock++)
    public static void deleteSale(int saleID) throws Exception {

        String selectSql = "SELECT productID, quantity FROM Sale WHERE saleID = ?";
        String lockSql = "SELECT quantity FROM Product WHERE productID = ? FOR UPDATE";
        String deleteSql = "DELETE FROM Sale WHERE saleID = ?";
        String restoreQtySql = "UPDATE Product SET quantity = quantity + ? WHERE productID = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                int productID;
                int oldQty;

                // 1) get sale row
                try (PreparedStatement psSel = conn.prepareStatement(selectSql)) {
                    psSel.setInt(1, saleID);
                    try (ResultSet rs = psSel.executeQuery()) {
                        if (!rs.next()) throw new Exception("SaleID not found");
                        productID = rs.getInt("productID");
                        oldQty = rs.getInt("quantity");
                    }
                }

                // 2) lock product
                try (PreparedStatement psLock = conn.prepareStatement(lockSql)) {
                    psLock.setInt(1, productID);
                    psLock.executeQuery(); // lock row
                }

                // 3) restore stock then delete
                try (PreparedStatement psRest = conn.prepareStatement(restoreQtySql);
                     PreparedStatement psDel = conn.prepareStatement(deleteSql)) {

                    psRest.setInt(1, oldQty);
                    psRest.setInt(2, productID);
                    psRest.executeUpdate();

                    psDel.setInt(1, saleID);
                    int rows = psDel.executeUpdate();
                    if (rows == 0) throw new Exception("SaleID not found");
                }

                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }


    //  UPDATE sale + stock handling + stock check
    public static void updateSale(int saleID,
                                  java.sql.Date saleDate,
                                  int newQty, double unitPrice,
                                  int clientID, int productID, int branchID) throws Exception {

        if (newQty <= 0) throw new Exception("Quantity must be > 0");
        if (unitPrice <= 0) throw new Exception("Unit price must be > 0");

        String selectSaleSql = "SELECT productID, quantity FROM Sale WHERE saleID = ?";

        String lockProductSql = "SELECT quantity FROM Product WHERE productID = ? FOR UPDATE";

        String updateSaleSql = """
        UPDATE Sale
        SET saleDate = ?, quantity = ?, unitPrice = ?, totalRevenue = ?,
            clientID = ?, productID = ?, branchID = ?
        WHERE saleID = ?
    """;

        String restoreOldSql = "UPDATE Product SET quantity = quantity + ? WHERE productID = ?";
        String applyNewSql = "UPDATE Product SET quantity = quantity - ? WHERE productID = ?";

        double totalRevenue = newQty * unitPrice;

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                int oldProductID;
                int oldQty;

                // 1) get old sale values
                try (PreparedStatement psSel = conn.prepareStatement(selectSaleSql)) {
                    psSel.setInt(1, saleID);
                    try (ResultSet rs = psSel.executeQuery()) {
                        if (!rs.next()) throw new Exception("SaleID not found");
                        oldProductID = rs.getInt("productID");
                        oldQty = rs.getInt("quantity");
                    }
                }

                // 2) lock old product, restore old effect
                int oldCurrent;
                try (PreparedStatement psLockOld = conn.prepareStatement(lockProductSql)) {
                    psLockOld.setInt(1, oldProductID);
                    try (ResultSet rs = psLockOld.executeQuery()) {
                        if (!rs.next()) throw new Exception("Old product not found");
                        oldCurrent = rs.getInt("quantity");
                    }
                }
                try (PreparedStatement psRestore = conn.prepareStatement(restoreOldSql)) {
                    psRestore.setInt(1, oldQty);
                    psRestore.setInt(2, oldProductID);
                    psRestore.executeUpdate();
                }

                // 3) lock new product + check stock after restore
                int newCurrent;
                try (PreparedStatement psLockNew = conn.prepareStatement(lockProductSql)) {
                    psLockNew.setInt(1, productID);
                    try (ResultSet rs = psLockNew.executeQuery()) {
                        if (!rs.next()) throw new Exception("ProductID not found");
                        newCurrent = rs.getInt("quantity");
                    }
                }

                if (newCurrent < newQty) {
                    throw new Exception("Not enough stock. Current quantity = " + newCurrent);
                }

                // 4) apply new effect (stock--)
                try (PreparedStatement psApply = conn.prepareStatement(applyNewSql)) {
                    psApply.setInt(1, newQty);
                    psApply.setInt(2, productID);
                    psApply.executeUpdate();
                }

                // 5) update sale row
                try (PreparedStatement psUpd = conn.prepareStatement(updateSaleSql)) {
                    psUpd.setDate(1, saleDate);
                    psUpd.setInt(2, newQty);
                    psUpd.setDouble(3, unitPrice);
                    psUpd.setDouble(4, totalRevenue);
                    psUpd.setInt(5, clientID);
                    psUpd.setInt(6, productID);
                    psUpd.setInt(7, branchID);
                    psUpd.setInt(8, saleID);

                    int rows = psUpd.executeUpdate();
                    if (rows == 0) throw new Exception("SaleID not found");
                }

                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }


    public static SaleRow getSaleByID(int saleID) throws Exception {

        String sql = """
        SELECT saleID, saleDate, quantity, unitPrice, totalRevenue, clientID, productID, branchID
        FROM Sale
        WHERE saleID = ?
    """;

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, saleID);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                return new SaleRow(
                        rs.getInt("saleID"),
                        rs.getDate("saleDate").toString(),
                        rs.getInt("quantity"),
                        rs.getDouble("unitPrice"),
                        rs.getDouble("totalRevenue"),
                        rs.getInt("clientID"),
                        rs.getInt("productID"),
                        rs.getInt("branchID")
                );
            }
        }
    }


}
