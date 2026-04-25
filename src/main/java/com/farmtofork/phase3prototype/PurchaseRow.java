package com.farmtofork.phase3prototype;

public class PurchaseRow {
    public int purchaseID;
    public String purchaseDate;
    public int quantity;
    public double unitPrice;
    public double totalCost;
    public String warehouseEntryDate;
    public int farmerID;
    public int productID;
    public int branchID;

    public PurchaseRow(int purchaseID, String purchaseDate, int quantity, double unitPrice, double totalCost,
                       String warehouseEntryDate, int farmerID, int productID, int branchID) {
        this.purchaseID = purchaseID;
        this.purchaseDate = purchaseDate;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalCost = totalCost;
        this.warehouseEntryDate = warehouseEntryDate;
        this.farmerID = farmerID;
        this.productID = productID;
        this.branchID = branchID;
    }
}
