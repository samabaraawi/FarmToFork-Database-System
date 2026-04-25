package com.farmtofork.phase3prototype;

public class ProductRow {
    public int productID;
    public String name;
    public String expiryDate;
    public double price;
    public int quantity;
    public int categoryID;
    public int warehouseID;

    public ProductRow(int productID, String name, String expiryDate, double price,
                      int quantity, int categoryID, int warehouseID) {
        this.productID = productID;
        this.name = name;
        this.expiryDate = expiryDate;
        this.price = price;
        this.quantity = quantity;
        this.categoryID = categoryID;
        this.warehouseID = warehouseID;
    }
}
