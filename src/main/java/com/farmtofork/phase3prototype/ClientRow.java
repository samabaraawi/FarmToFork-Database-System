package com.farmtofork.phase3prototype;

public class ClientRow {
    public int clientID;
    public String name;
    public String type;
    public String contactInfo;

    public ClientRow(int clientID, String name, String type, String contactInfo) {
        this.clientID = clientID;
        this.name = name;
        this.type = type;
        this.contactInfo = contactInfo;
    }
}
