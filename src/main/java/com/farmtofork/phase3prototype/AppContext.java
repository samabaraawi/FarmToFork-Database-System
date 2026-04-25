package com.farmtofork.phase3prototype;

public class AppContext {
    public static Runnable refreshProducts = () -> {};
    public static Runnable refreshSales = () -> {};
    public static Runnable refreshPurchases = () -> {};

    public static void setRefreshProducts(Runnable r) { refreshProducts = (r == null) ? (() -> {}) : r; }
    public static void setRefreshSales(Runnable r) { refreshSales = (r == null) ? (() -> {}) : r; }
    public static void setRefreshPurchases(Runnable r) { refreshPurchases = (r == null) ? (() -> {}) : r; }
}
