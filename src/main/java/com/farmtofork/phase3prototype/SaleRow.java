package com.farmtofork.phase3prototype;

public class SaleRow {
    public int saleID;
    public String saleDate;
    public int quantity;
    public double unitPrice;
    public double totalRevenue;
    public int clientID;
    public int productID;
    public int branchID;

    public SaleRow(int saleID, String saleDate, int quantity, double unitPrice, double totalRevenue,
                   int clientID, int productID, int branchID) {
        this.saleID = saleID;
        this.saleDate = saleDate;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalRevenue = totalRevenue;
        this.clientID = clientID;
        this.productID = productID;
        this.branchID = branchID;
    }
}
