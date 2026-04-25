-- FarmToFork - Reports (Q1..Q20)
-- Purpose: Provide the 20 required queries in one SQL file (for submission/report),
-- while the application can still execute them from Java (ReportDAO).
-- Notes:
--   * Q5 and Q6 are parameterized in the Java code (threshold, days). In SQL file
--     we show them with placeholders (replace ? with a number when testing).
--   * Schema assumed: farmtofork (tables: Sale, Purchase, Product, Client, Farmer, Branch, Warehouse, Category)

USE farmtofork;

-- =========================================================
-- Q1) Sales per branch per month
-- =========================================================
SELECT b.name AS branch,
       DATE_FORMAT(s.saleDate, '%Y-%m') AS month,
       SUM(s.totalRevenue) AS total_sales
FROM Sale s
JOIN Branch b ON b.branchID = s.branchID
GROUP BY b.name, DATE_FORMAT(s.saleDate, '%Y-%m')
ORDER BY month, branch;

-- =========================================================
-- Q2) Total revenue per client
-- =========================================================
SELECT c.clientID,
       c.name AS client_name,
       SUM(s.totalRevenue) AS total_revenue
FROM Sale s
JOIN Client c ON c.clientID = s.clientID
GROUP BY c.clientID, c.name
ORDER BY total_revenue DESC;

-- =========================================================
-- Q3) Most in-demand products (by sold quantity)
-- =========================================================
SELECT p.productID,
       p.name AS product_name,
       SUM(s.quantity) AS total_sold
FROM Sale s
JOIN Product p ON p.productID = s.productID
GROUP BY p.productID, p.name
ORDER BY total_sold DESC;

-- =========================================================
-- Q4) Total purchases per month (spending + qty)
-- =========================================================
SELECT DATE_FORMAT(p.purchaseDate, '%Y-%m') AS month,
       SUM(p.totalCost) AS total_spending,
       SUM(p.quantity) AS total_quantity
FROM Purchase p
GROUP BY DATE_FORMAT(p.purchaseDate, '%Y-%m')
ORDER BY month;

-- =========================================================
-- Q5) Low stock products (qty < threshold)  [PARAMETERIZED]
--     Replace ? with a value when testing in MySQL Workbench.
-- =========================================================
SELECT productID, name, quantity
FROM Product
WHERE quantity < ?
ORDER BY quantity ASC;

-- Example test:
-- SELECT productID, name, quantity FROM Product WHERE quantity < 10 ORDER BY quantity ASC;

-- =========================================================
-- Q6) Products nearing expiry (within days) [PARAMETERIZED]
--     Replace ? with a value when testing in MySQL Workbench.
-- =========================================================
SELECT productID, name, expiryDate
FROM Product
WHERE expiryDate IS NOT NULL
  AND expiryDate <= (CURDATE() + INTERVAL ? DAY)
ORDER BY expiryDate ASC;

-- Example test:
-- SELECT productID, name, expiryDate
-- FROM Product
-- WHERE expiryDate IS NOT NULL AND expiryDate <= (CURDATE() + INTERVAL 14 DAY)
-- ORDER BY expiryDate ASC;

-- =========================================================
-- Q7) Total revenue per branch
-- =========================================================
SELECT b.branchID,
       b.name AS branch_name,
       SUM(s.totalRevenue) AS total_revenue
FROM Sale s
JOIN Branch b ON b.branchID = s.branchID
GROUP BY b.branchID, b.name
ORDER BY total_revenue DESC;

-- =========================================================
-- Q8) Total sold quantity per product
-- =========================================================
SELECT p.productID,
       p.name AS product_name,
       SUM(s.quantity) AS total_sold
FROM Sale s
JOIN Product p ON p.productID = s.productID
GROUP BY p.productID, p.name
ORDER BY total_sold DESC;

-- =========================================================
-- Q9) Total revenue per product
-- =========================================================
SELECT p.productID,
       p.name AS product_name,
       SUM(s.totalRevenue) AS revenue
FROM Sale s
JOIN Product p ON p.productID = s.productID
GROUP BY p.productID, p.name
ORDER BY revenue DESC;

-- =========================================================
-- Q10) Products never sold
-- =========================================================
SELECT p.productID,
       p.name AS product_name,
       p.quantity AS current_qty
FROM Product p
LEFT JOIN Sale s ON s.productID = p.productID
WHERE s.saleID IS NULL
ORDER BY p.productID DESC;

-- =========================================================
-- Q11) Clients with no sales
-- =========================================================
SELECT c.clientID,
       c.name AS client_name,
       c.type AS client_type
FROM Client c
LEFT JOIN Sale s ON s.clientID = c.clientID
WHERE s.saleID IS NULL
ORDER BY c.clientID DESC;

-- =========================================================
-- Q12) Monthly sales totals (revenue + qty)
-- =========================================================
SELECT DATE_FORMAT(s.saleDate,'%Y-%m') AS month,
       SUM(s.totalRevenue) AS revenue,
       SUM(s.quantity) AS total_qty
FROM Sale s
GROUP BY DATE_FORMAT(s.saleDate,'%Y-%m')
ORDER BY month;

-- =========================================================
-- Q13) Monthly profit estimate (Sales - Purchases) per month
-- =========================================================
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
ORDER BY m.month;

-- =========================================================
-- Q14) Average selling price per product
-- =========================================================
SELECT p.productID,
       p.name AS product_name,
       AVG(s.unitPrice) AS avg_selling_price
FROM Sale s
JOIN Product p ON p.productID = s.productID
GROUP BY p.productID, p.name
ORDER BY avg_selling_price DESC;

-- =========================================================
-- Q15) Average purchase price per product
-- =========================================================
SELECT pr.productID,
       pr.name AS product_name,
       AVG(p.unitPrice) AS avg_purchase_price
FROM Purchase p
JOIN Product pr ON pr.productID = p.productID
GROUP BY pr.productID, pr.name
ORDER BY avg_purchase_price DESC;

-- =========================================================
-- Q16) Top clients by purchased quantity (sales qty)
-- =========================================================
SELECT c.clientID,
       c.name AS client_name,
       SUM(s.quantity) AS total_qty
FROM Sale s
JOIN Client c ON c.clientID = s.clientID
GROUP BY c.clientID, c.name
ORDER BY total_qty DESC;

-- =========================================================
-- Q17) Top farmers by supplied quantity (purchases qty)
-- =========================================================
SELECT f.farmerID,
       f.name AS farmer_name,
       SUM(p.quantity) AS total_supplied
FROM Purchase p
JOIN Farmer f ON f.farmerID = p.farmerID
GROUP BY f.farmerID, f.name
ORDER BY total_supplied DESC;

-- =========================================================
-- Q18) Purchases per branch per month
-- =========================================================
SELECT b.name AS branch,
       DATE_FORMAT(p.purchaseDate, '%Y-%m') AS month,
       SUM(p.totalCost) AS total_spending
FROM Purchase p
JOIN Branch b ON b.branchID = p.branchID
GROUP BY b.name, DATE_FORMAT(p.purchaseDate, '%Y-%m')
ORDER BY month, branch;

-- =========================================================
-- Q19) Sales per product per branch
-- =========================================================
SELECT b.name AS branch,
       pr.name AS product,
       SUM(s.quantity) AS total_sold
FROM Sale s
JOIN Branch b ON b.branchID = s.branchID
JOIN Product pr ON pr.productID = s.productID
GROUP BY b.name, pr.name
ORDER BY branch, total_sold DESC;

-- =========================================================
-- Q20) Stock value per warehouse (sum(price*qty))
-- =========================================================
SELECT w.warehouseID,
       w.location AS warehouse,
       SUM(pr.price * pr.quantity) AS stock_value
FROM Product pr
JOIN Warehouse w ON w.warehouseID = pr.warehouseID
GROUP BY w.warehouseID, w.location
ORDER BY stock_value DESC;
