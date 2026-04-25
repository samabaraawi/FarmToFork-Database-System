package com.farmtofork.phase3prototype;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO {

    /* =========================
       Report 1: Sales per branch per month
       ========================= */
    public static List<ReportRow> salesPerBranchPerMonth() throws Exception {
        String sql = """
            SELECT b.name AS branch,
                   DATE_FORMAT(s.saleDate, '%Y-%m') AS month,
                   SUM(s.totalRevenue) AS total_sales
            FROM Sale s
            JOIN Branch b ON b.branchID = s.branchID
            GROUP BY b.name, DATE_FORMAT(s.saleDate, '%Y-%m')
            ORDER BY month, branch
        """;
        return run(sql, "branch", "month", "total_sales", "");
    }

    /* =========================
       Report 2: Total revenue per client
       ========================= */
    public static List<ReportRow> totalRevenuePerClient() throws Exception {
        String sql = """
            SELECT c.clientID,
                   c.name AS client_name,
                   SUM(s.totalRevenue) AS total_revenue
            FROM Sale s
            JOIN Client c ON c.clientID = s.clientID
            GROUP BY c.clientID, c.name
            ORDER BY total_revenue DESC
        """;
        return run(sql, "clientID", "client_name", "total_revenue", "");
    }

    /* =========================
       Report 3: Most in-demand products (by sold quantity)
       ========================= */
    public static List<ReportRow> mostInDemandProducts() throws Exception {
        String sql = """
            SELECT p.productID,
                   p.name AS product_name,
                   SUM(s.quantity) AS total_sold
            FROM Sale s
            JOIN Product p ON p.productID = s.productID
            GROUP BY p.productID, p.name
            ORDER BY total_sold DESC
        """;
        return run(sql, "productID", "product_name", "total_sold", "");
    }

    /* =========================
       Report 4: Total purchases per month
       ========================= */
    public static List<ReportRow> totalPurchasesPerMonth() throws Exception {
        String sql = """
            SELECT DATE_FORMAT(p.purchaseDate, '%Y-%m') AS month,
                   SUM(p.totalCost) AS total_spending,
                   SUM(p.quantity) AS total_quantity
            FROM Purchase p
            GROUP BY DATE_FORMAT(p.purchaseDate, '%Y-%m')
            ORDER BY month
        """;
        return run(sql, "month", "total_spending", "total_quantity", "");
    }

    /* =========================
       Report 5: Low stock products (qty < threshold)
       ========================= */
    public static List<ReportRow> lowStockProducts(int threshold) throws Exception {
        String sql = """
            SELECT productID, name, quantity
            FROM Product
            WHERE quantity < ?
            ORDER BY quantity ASC
        """;

        List<ReportRow> rows = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, threshold);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new ReportRow(
                            rs.getString("productID"),
                            rs.getString("name"),
                            rs.getString("quantity"),
                            ""
                    ));
                }
            }
        }
        return rows;
    }

    /* =========================
       Report 6: Products nearing expiry (within days)
       ========================= */
    public static List<ReportRow> nearExpiryProducts(int days) throws Exception {
        String sql = """
            SELECT productID, name, expiryDate
            FROM Product
            WHERE expiryDate IS NOT NULL
              AND expiryDate <= (CURDATE() + INTERVAL ? DAY)
            ORDER BY expiryDate ASC
        """;

        List<ReportRow> rows = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, days);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new ReportRow(
                            rs.getString("productID"),
                            rs.getString("name"),
                            rs.getString("expiryDate"),
                            ""
                    ));
                }
            }
        }
        return rows;
    }

    /* =========================
       Report 7: Total revenue per branch
       ========================= */
    public static List<ReportRow> totalRevenuePerBranch() throws Exception {
        String sql = """
            SELECT b.branchID,
                   b.name AS branch_name,
                   SUM(s.totalRevenue) AS total_revenue
            FROM Sale s
            JOIN Branch b ON b.branchID = s.branchID
            GROUP BY b.branchID, b.name
            ORDER BY total_revenue DESC
        """;
        return run(sql, "branchID", "branch_name", "total_revenue", "");
    }

    /* =========================
       Report 8: Total sold quantity per product
       ========================= */
    public static List<ReportRow> totalSoldQtyPerProduct() throws Exception {
        String sql = """
            SELECT p.productID,
                   p.name AS product_name,
                   SUM(s.quantity) AS total_sold
            FROM Sale s
            JOIN Product p ON p.productID = s.productID
            GROUP BY p.productID, p.name
            ORDER BY total_sold DESC
        """;
        return run(sql, "productID", "product_name", "total_sold", "");
    }

    /* =========================
       Report 9: Total revenue per product
       ========================= */
    public static List<ReportRow> totalRevenuePerProduct() throws Exception {
        String sql = """
            SELECT p.productID,
                   p.name AS product_name,
                   SUM(s.totalRevenue) AS revenue
            FROM Sale s
            JOIN Product p ON p.productID = s.productID
            GROUP BY p.productID, p.name
            ORDER BY revenue DESC
        """;
        return run(sql, "productID", "product_name", "revenue", "");
    }

    /* =========================
       Report 10: Products never sold
       ========================= */
    public static List<ReportRow> productsNeverSold() throws Exception {
        String sql = """
            SELECT p.productID,
                   p.name AS product_name,
                   p.quantity AS current_qty
            FROM Product p
            LEFT JOIN Sale s ON s.productID = p.productID
            WHERE s.saleID IS NULL
            ORDER BY p.productID DESC
        """;
        return run(sql, "productID", "product_name", "current_qty", "");
    }

    /* =========================
       Report 11: Clients with no sales
       ========================= */
    public static List<ReportRow> clientsWithNoSales() throws Exception {
        String sql = """
            SELECT c.clientID,
                   c.name AS client_name,
                   c.type AS client_type
            FROM Client c
            LEFT JOIN Sale s ON s.clientID = c.clientID
            WHERE s.saleID IS NULL
            ORDER BY c.clientID DESC
        """;
        return run(sql, "clientID", "client_name", "client_type", "");
    }

    /* =========================
       Report 12: Monthly sales totals (revenue + qty)
       ========================= */
    public static List<ReportRow> monthlySalesTotals() throws Exception {
        String sql = """
            SELECT DATE_FORMAT(s.saleDate,'%Y-%m') AS month,
                   SUM(s.totalRevenue) AS revenue,
                   SUM(s.quantity) AS total_qty
            FROM Sale s
            GROUP BY DATE_FORMAT(s.saleDate,'%Y-%m')
            ORDER BY month
        """;
        return run(sql, "month", "revenue", "total_qty", "");
    }

    /* =========================
       Report 13: Monthly profit estimate (Sales - Purchases) per month
       ========================= */
    public static List<ReportRow> monthlyProfitEstimate() throws Exception {
        String sql = """
            SELECT m.month AS month,
                   COALESCE(sa.sales_revenue,0) AS sales_revenue,
                   COALESCE(pu.purchase_cost,0) AS purchase_cost,
                   (COALESCE(sa.sales_revenue,0) - COALESCE(pu.purchase_cost,0)) AS profit_estimate
            FROM (
                SELECT DATE_FORMAT(d,'%Y-%m') AS month
                FROM (
                    SELECT saleDate AS d FROM Sale
                    UNION
                    SELECT purchaseDate AS d FROM Purchase
                ) x
            ) m
            LEFT JOIN (
                SELECT DATE_FORMAT(saleDate,'%Y-%m') AS month,
                       SUM(totalRevenue) AS sales_revenue
                FROM Sale
                GROUP BY DATE_FORMAT(saleDate,'%Y-%m')
            ) sa ON sa.month = m.month
            LEFT JOIN (
                SELECT DATE_FORMAT(purchaseDate,'%Y-%m') AS month,
                       SUM(totalCost) AS purchase_cost
                FROM Purchase
                GROUP BY DATE_FORMAT(purchaseDate,'%Y-%m')
            ) pu ON pu.month = m.month
            ORDER BY m.month
        """;
        return run(sql, "month", "sales_revenue", "purchase_cost", "profit_estimate");
    }

    /* =========================
       Report 14: Average selling price per product
       ========================= */
    public static List<ReportRow> avgSellingPricePerProduct() throws Exception {
        String sql = """
            SELECT p.productID,
                   p.name AS product_name,
                   AVG(s.unitPrice) AS avg_selling_price
            FROM Sale s
            JOIN Product p ON p.productID = s.productID
            GROUP BY p.productID, p.name
            ORDER BY avg_selling_price DESC
        """;
        return run(sql, "productID", "product_name", "avg_selling_price", "");
    }

    /* =========================
       Report 15: Average purchase price per product
       ========================= */
    public static List<ReportRow> avgPurchasePricePerProduct() throws Exception {
        String sql = """
            SELECT pr.productID,
                   pr.name AS product_name,
                   AVG(p.unitPrice) AS avg_purchase_price
            FROM Purchase p
            JOIN Product pr ON pr.productID = p.productID
            GROUP BY pr.productID, pr.name
            ORDER BY avg_purchase_price DESC
        """;
        return run(sql, "productID", "product_name", "avg_purchase_price", "");
    }

    /* =========================
       Report 16: Top clients by purchased quantity (sales qty)
       ========================= */
    public static List<ReportRow> topClientsByQuantity() throws Exception {
        String sql = """
            SELECT c.clientID,
                   c.name AS client_name,
                   SUM(s.quantity) AS total_qty
            FROM Sale s
            JOIN Client c ON c.clientID = s.clientID
            GROUP BY c.clientID, c.name
            ORDER BY total_qty DESC
        """;
        return run(sql, "clientID", "client_name", "total_qty", "");
    }

    /* =========================
       Report 17: Top farmers by supplied quantity (purchases qty)
       ========================= */
    public static List<ReportRow> topFarmersBySuppliedQuantity() throws Exception {
        String sql = """
            SELECT f.farmerID,
                   f.name AS farmer_name,
                   SUM(p.quantity) AS total_supplied
            FROM Purchase p
            JOIN Farmer f ON f.farmerID = p.farmerID
            GROUP BY f.farmerID, f.name
            ORDER BY total_supplied DESC
        """;
        return run(sql, "farmerID", "farmer_name", "total_supplied", "");
    }

    /* =========================
       Report 18: Purchases per branch per month
       ========================= */
    public static List<ReportRow> purchasesPerBranchPerMonth() throws Exception {
        String sql = """
            SELECT b.name AS branch,
                   DATE_FORMAT(p.purchaseDate, '%Y-%m') AS month,
                   SUM(p.totalCost) AS total_spending
            FROM Purchase p
            JOIN Branch b ON b.branchID = p.branchID
            GROUP BY b.name, DATE_FORMAT(p.purchaseDate, '%Y-%m')
            ORDER BY month, branch
        """;
        return run(sql, "branch", "month", "total_spending", "");
    }

    /* =========================
       Report 19: Sales per product per branch
       ========================= */
    public static List<ReportRow> salesPerProductPerBranch() throws Exception {
        String sql = """
            SELECT b.name AS branch,
                   pr.name AS product,
                   SUM(s.quantity) AS total_sold
            FROM Sale s
            JOIN Branch b ON b.branchID = s.branchID
            JOIN Product pr ON pr.productID = s.productID
            GROUP BY b.name, pr.name
            ORDER BY branch, total_sold DESC
        """;
        return run(sql, "branch", "product", "total_sold", "");
    }

    /* =========================
       Report 20: Stock value per warehouse (sum(price*qty))
       ========================= */
    public static List<ReportRow> stockValuePerWarehouse() throws Exception {
        String sql = """
            SELECT w.warehouseID,
                   w.location AS warehouse,
                   SUM(pr.price * pr.quantity) AS stock_value
            FROM Product pr
            JOIN Warehouse w ON w.warehouseID = pr.warehouseID
            GROUP BY w.warehouseID, w.location
            ORDER BY stock_value DESC
        """;
        return run(sql, "warehouseID", "warehouse", "stock_value", "");
    }

    /* =========================
       Helper method
       ========================= */
    private static List<ReportRow> run(String sql,
                                       String c1, String c2, String c3, String c4) throws Exception {
        List<ReportRow> rows = new ArrayList<>();

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                rows.add(new ReportRow(
                        get(rs, c1),
                        get(rs, c2),
                        get(rs, c3),
                        (c4 == null || c4.isBlank()) ? "" : get(rs, c4)
                ));
            }
        }
        return rows;
    }

    private static String get(ResultSet rs, String col) throws Exception {
        if (col == null || col.isBlank()) return "";
        Object v = rs.getObject(col);
        return v == null ? "" : v.toString();
    }
}
