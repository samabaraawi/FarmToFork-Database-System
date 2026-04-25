package com.farmtofork.phase3prototype;

public class IdName {
    public int id;
    public String name;

    public IdName(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return name + " (ID=" + id + ")";
    }
}

