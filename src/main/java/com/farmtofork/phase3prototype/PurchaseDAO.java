package com.farmtofork.phase3prototype;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class PurchaseDAO {

    public static void insertPurchase(java.sql.Date purchaseDate,
                                      int quantity, double unitPrice,
                                      java.sql.Date warehouseEntryDate,
                                      int farmerID, int productID, int branchID) throws Exception {

        double totalCost = quantity * unitPrice;

        String insertPurchaseSql = """
            INSERT INTO Purchase(purchaseDate, quantity, unitPrice, totalCost, warehouseEntryDate,
                                 farmerID, productID, branchID)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        String updateQtySql = """
            UPDATE Product
            SET quantity = quantity + ?
            WHERE productID = ?
        """;

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps1 = conn.prepareStatement(insertPurchaseSql);
                 PreparedStatement ps2 = conn.prepareStatement(updateQtySql)) {

                ps1.setDate(1, purchaseDate);
                ps1.setInt(2, quantity);
                ps1.setDouble(3, unitPrice);
                ps1.setDouble(4, totalCost);
                ps1.setDate(5, warehouseEntryDate);
                ps1.setInt(6, farmerID);
                ps1.setInt(7, productID);
                ps1.setInt(8, branchID);
                ps1.executeUpdate();

                ps2.setInt(1, quantity);
                ps2.setInt(2, productID);
                ps2.executeUpdate();

                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public static List<PurchaseRow> getAllPurchases() throws Exception {
        String sql = """
            SELECT purchaseID, purchaseDate, quantity, unitPrice, totalCost, warehouseEntryDate,
                   farmerID, productID, branchID
            FROM Purchase
            ORDER BY purchaseDate DESC, purchaseID DESC
        """;

        List<PurchaseRow> rows = new ArrayList<>();

        try (var c = DBConnection.getConnection();
             var ps = c.prepareStatement(sql);
             var rs = ps.executeQuery()) {

            while (rs.next()) {
                rows.add(new PurchaseRow(
                        rs.getInt("purchaseID"),
                        rs.getDate("purchaseDate").toString(),
                        rs.getInt("quantity"),
                        rs.getDouble("unitPrice"),
                        rs.getDouble("totalCost"),
                        rs.getDate("warehouseEntryDate") == null ? "" : rs.getDate("warehouseEntryDate").toString(),
                        rs.getInt("farmerID"),
                        rs.getInt("productID"),
                        rs.getInt("branchID")
                ));
            }
        }
        return rows;
    }

    // DELETE purchase + reverse stock (stock--)
    public static void deletePurchase(int purchaseID) throws Exception {

        String selectSql = "SELECT productID, quantity FROM Purchase WHERE purchaseID = ?";
        String lockSql = "SELECT quantity FROM Product WHERE productID = ? FOR UPDATE";
        String deleteSql = "DELETE FROM Purchase WHERE purchaseID = ?";
        String updateQtySql = "UPDATE Product SET quantity = quantity - ? WHERE productID = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                int productID;
                int oldQty;

                // 1) get purchase info
                try (PreparedStatement psSel = conn.prepareStatement(selectSql)) {
                    psSel.setInt(1, purchaseID);
                    try (ResultSet rs = psSel.executeQuery()) {
                        if (!rs.next()) throw new Exception("PurchaseID not found");
                        productID = rs.getInt("productID");
                        oldQty = rs.getInt("quantity");
                    }
                }

                // 2) lock product + check we can subtract
                int currentQty;
                try (PreparedStatement psLock = conn.prepareStatement(lockSql)) {
                    psLock.setInt(1, productID);
                    try (ResultSet rs = psLock.executeQuery()) {
                        if (!rs.next()) throw new Exception("ProductID not found");
                        currentQty = rs.getInt("quantity");
                    }
                }

                if (currentQty < oldQty) {
                    throw new Exception("Cannot delete this purchase: stock is already used/sold. Current stock = " + currentQty);
                }

                // 3) subtract stock + delete purchase
                try (PreparedStatement psUpd = conn.prepareStatement(updateQtySql);
                     PreparedStatement psDel = conn.prepareStatement(deleteSql)) {

                    psUpd.setInt(1, oldQty);
                    psUpd.setInt(2, productID);
                    psUpd.executeUpdate();

                    psDel.setInt(1, purchaseID);
                    int rows = psDel.executeUpdate();
                    if (rows == 0) throw new Exception("PurchaseID not found");
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


    // UPDATE purchase + adjust stock correctly (remove old, add new)
    public static void updatePurchase(int purchaseID,
                                      java.sql.Date purchaseDate,
                                      int newQty, double unitPrice,
                                      java.sql.Date warehouseEntryDate,
                                      int farmerID, int productID, int branchID) throws Exception {

        if (newQty <= 0) throw new Exception("Quantity must be > 0");
        if (unitPrice <= 0) throw new Exception("Unit price must be > 0");

        String selectSql = "SELECT productID, quantity FROM Purchase WHERE purchaseID = ?";

        String lockSql = "SELECT quantity FROM Product WHERE productID = ? FOR UPDATE";

        String updatePurchaseSql = """
        UPDATE Purchase
        SET purchaseDate = ?, quantity = ?, unitPrice = ?, totalCost = ?, warehouseEntryDate = ?,
            farmerID = ?, productID = ?, branchID = ?
        WHERE purchaseID = ?
    """;

        String decOldProductSql = "UPDATE Product SET quantity = quantity - ? WHERE productID = ?";
        String incNewProductSql = "UPDATE Product SET quantity = quantity + ? WHERE productID = ?";

        double totalCost = newQty * unitPrice;

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                int oldProductID;
                int oldQty;

                // 1) old values
                try (PreparedStatement psSel = conn.prepareStatement(selectSql)) {
                    psSel.setInt(1, purchaseID);
                    try (ResultSet rs = psSel.executeQuery()) {
                        if (!rs.next()) throw new Exception("PurchaseID not found");
                        oldProductID = rs.getInt("productID");
                        oldQty = rs.getInt("quantity");
                    }
                }

                // 2) lock old product + check we can subtract oldQty
                int oldCurrent;
                try (PreparedStatement psLockOld = conn.prepareStatement(lockSql)) {
                    psLockOld.setInt(1, oldProductID);
                    try (ResultSet rs = psLockOld.executeQuery()) {
                        if (!rs.next()) throw new Exception("Old product not found");
                        oldCurrent = rs.getInt("quantity");
                    }
                }

                if (oldCurrent < oldQty) {
                    throw new Exception("Cannot update this purchase: stock is already used/sold. Current stock = " + oldCurrent);
                }

                // 3) reverse old effect (subtract oldQty)
                try (PreparedStatement psDec = conn.prepareStatement(decOldProductSql)) {
                    psDec.setInt(1, oldQty);
                    psDec.setInt(2, oldProductID);
                    psDec.executeUpdate();
                }

                // 4) lock new product (just to be safe) then apply new effect (add)
                try (PreparedStatement psLockNew = conn.prepareStatement(lockSql)) {
                    psLockNew.setInt(1, productID);
                    psLockNew.executeQuery();
                }

                try (PreparedStatement psInc = conn.prepareStatement(incNewProductSql)) {
                    psInc.setInt(1, newQty);
                    psInc.setInt(2, productID);
                    psInc.executeUpdate();
                }

                // 5) update purchase row
                try (PreparedStatement psUpd = conn.prepareStatement(updatePurchaseSql)) {
                    psUpd.setDate(1, purchaseDate);
                    psUpd.setInt(2, newQty);
                    psUpd.setDouble(3, unitPrice);
                    psUpd.setDouble(4, totalCost);
                    psUpd.setDate(5, warehouseEntryDate);
                    psUpd.setInt(6, farmerID);
                    psUpd.setInt(7, productID);
                    psUpd.setInt(8, branchID);
                    psUpd.setInt(9, purchaseID);

                    int rows = psUpd.executeUpdate();
                    if (rows == 0) throw new Exception("PurchaseID not found");
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

}
