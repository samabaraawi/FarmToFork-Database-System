-- FarmToFork - Optional Views (8 Views)
-- Purpose: Make the project look more professional by defining some reports as SQL views.
-- Note: Reports that require parameters (like threshold/days) are better kept in Java.

USE farmtofork;

-- Drop views if they exist (safe re-run)
DROP VIEW IF EXISTS v_sales_per_branch_month;
DROP VIEW IF EXISTS v_total_revenue_per_client;
DROP VIEW IF EXISTS v_most_in_demand_products;
DROP VIEW IF EXISTS v_total_purchases_per_month;
DROP VIEW IF EXISTS v_products_never_sold;
DROP VIEW IF EXISTS v_clients_with_no_sales;
DROP VIEW IF EXISTS v_monthly_profit_estimate;
DROP VIEW IF EXISTS v_stock_value_per_warehouse;

-- 1) Q1 as view
CREATE VIEW v_sales_per_branch_month AS
SELECT b.name AS branch,
       DATE_FORMAT(s.saleDate, '%Y-%m') AS month,
       SUM(s.totalRevenue) AS total_sales
FROM Sale s
JOIN Branch b ON b.branchID = s.branchID
GROUP BY b.name, DATE_FORMAT(s.saleDate, '%Y-%m');

-- 2) Q2 as view
CREATE VIEW v_total_revenue_per_client AS
SELECT c.clientID,
       c.name AS client_name,
       SUM(s.totalRevenue) AS total_revenue
FROM Sale s
JOIN Client c ON c.clientID = s.clientID
GROUP BY c.clientID, c.name;

-- 3) Q3 as view
CREATE VIEW v_most_in_demand_products AS
SELECT p.productID,
       p.name AS product_name,
       SUM(s.quantity) AS total_sold
FROM Sale s
JOIN Product p ON p.productID = s.productID
GROUP BY p.productID, p.name;

-- 4) Q4 as view
CREATE VIEW v_total_purchases_per_month AS
SELECT DATE_FORMAT(p.purchaseDate, '%Y-%m') AS month,
       SUM(p.totalCost) AS total_spending,
       SUM(p.quantity) AS total_quantity
FROM Purchase p
GROUP BY DATE_FORMAT(p.purchaseDate, '%Y-%m');

-- 5) Q10 as view
CREATE VIEW v_products_never_sold AS
SELECT p.productID,
       p.name AS product_name,
       p.quantity AS current_qty
FROM Product p
LEFT JOIN Sale s ON s.productID = p.productID
WHERE s.saleID IS NULL;

-- 6) Q11 as view
CREATE VIEW v_clients_with_no_sales AS
SELECT c.clientID,
       c.name AS client_name,
       c.type AS client_type
FROM Client c
LEFT JOIN Sale s ON s.clientID = c.clientID
WHERE s.saleID IS NULL;

-- 7) Q13 as view
CREATE VIEW v_monthly_profit_estimate AS
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
) pu ON pu.month = m.month;

-- 8) Q20 as view
CREATE VIEW v_stock_value_per_warehouse AS
SELECT w.warehouseID,
       w.location AS warehouse,
       SUM(pr.price * pr.quantity) AS stock_value
FROM Product pr
JOIN Warehouse w ON w.warehouseID = pr.warehouseID
GROUP BY w.warehouseID, w.location;

-- Example usage:
-- SELECT * FROM v_sales_per_branch_month ORDER BY month, branch;
-- SELECT * FROM v_total_revenue_per_client ORDER BY total_revenue DESC;
