package com.farmtofork.phase3prototype;

public class FarmerRow {
    public int farmerID;
    public String name;
    public String region;
    public String contactInfo;

    public FarmerRow(int farmerID, String name, String region, String contactInfo) {
        this.farmerID = farmerID;
        this.name = name;
        this.region = region;
        this.contactInfo = contactInfo;
    }
}
