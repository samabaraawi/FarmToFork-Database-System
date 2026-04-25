package com.farmtofork.phase3prototype;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class ReportsView {

    private final Label status;
    private final TableView<ReportRow> reportTable = new TableView<>();

    // keep references so we can change headers dynamically
    private TableColumn<ReportRow, String> col1;
    private TableColumn<ReportRow, String> col2;
    private TableColumn<ReportRow, String> col3;
    private TableColumn<ReportRow, String> col4;

    public ReportsView(Label status) {
        this.status = status;
    }

    public Node getContent() {
        return buildReportsTab();
    }

    private Node buildReportsTab() {
        setupReportTable();

        // ===== Header (farm theme) =====
        Label title = new Label("Reports Center");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label sub = new Label("Analytics & insights for sales, purchases, stock, and partners");
        sub.setStyle("-fx-text-fill: #E9F7EF;");

        VBox titleBox = new VBox(2, title, sub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnRun = bigBtn("Run Report");
        stylePrimary(btnRun);

        HBox header = new HBox(12, titleBox, spacer, btnRun);
        header.setPadding(new Insets(14));
        header.setStyle("-fx-background-color: #1B5E20; -fx-background-radius: 10;");

        // ===== Report Selector =====
        ComboBox<String> cb = new ComboBox<>();
        cb.getItems().setAll(
                "1) Total sales per branch per month",
                "2) Total revenue per client",
                "3) Most in-demand products (by sold quantity)",
                "4) Total purchases per month",
                "5) Low stock products (qty < 10)",
                "6) Products nearing expiry (7 days)",
                "7) Total revenue per branch",
                "8) Total sold quantity per product",
                "9) Total revenue per product",
                "10) Products never sold",
                "11) Clients with no sales",
                "12) Monthly sales totals (revenue + qty)",
                "13) Monthly profit estimate (sales - purchases)",
                "14) Average selling price per product",
                "15) Average purchase price per product",
                "16) Top clients by purchased quantity",
                "17) Top farmers by supplied quantity",
                "18) Purchases per branch per month",
                "19) Sales per product per branch",
                "20) Stock value per warehouse"
        );
        cb.getSelectionModel().selectFirst();
        cb.setMaxWidth(Double.MAX_VALUE);
        cb.setPrefHeight(38);

        Label hint = new Label("");
        hint.setStyle("-fx-text-fill: #33691E; -fx-font-style: italic;");

        // update hint + headers when selection changes
        cb.valueProperty().addListener((obs, oldV, newV) -> {
            hint.setText(hintFor(newV));
            applyHeadersFor(newV);
        });
        hint.setText(hintFor(cb.getValue()));
        applyHeadersFor(cb.getValue());

        btnRun.setOnAction(e -> runReport(cb.getValue()));

        Button btnClear = bigBtn("Clear Table");
        styleSoft(btnClear);
        btnClear.setOnAction(e -> {
            reportTable.setItems(FXCollections.observableArrayList());
            status.setText("Status: Report table cleared");
        });

        HBox controls = new HBox(10, new Label("Choose Report:"), cb, btnClear);
        controls.setPadding(new Insets(12));
        controls.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: #C8E6C9;" +
                        "-fx-border-radius: 10;"
        );
        HBox.setHgrow(cb, Priority.ALWAYS);

        VBox hintBox = new VBox(6, hint);
        hintBox.setPadding(new Insets(0, 12, 0, 12));

        // ===== Table Card =====
        reportTable.setPrefHeight(520);

        VBox tableCard = new VBox(10, reportTable);
        tableCard.setPadding(new Insets(12));
        tableCard.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: #C8E6C9;" +
                        "-fx-border-radius: 10;"
        );

        VBox content = new VBox(12, header, controls, hintBox, tableCard);
        content.setPadding(new Insets(12));
        content.setStyle("-fx-background-color: #F1F8E9;");

        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return sp;
    }

    private void runReport(String name) {
        try {
            // make sure headers match before showing data
            applyHeadersFor(name);

            switch (name) {
                case "1) Total sales per branch per month" -> reportTable.setItems(FXCollections.observableArrayList(ReportDAO.salesPerBranchPerMonth()));
                case "2) Total revenue per client" -> reportTable.setItems(FXCollections.observableArrayList(ReportDAO.totalRevenuePerClient()));
                case "3) Most in-demand products (by sold quantity)" -> reportTable.setItems(FXCollections.observableArrayList(ReportDAO.mostInDemandProducts()));
                case "4) Total purchases per month" -> reportTable.setItems(FXCollections.observableArrayList(ReportDAO.totalPurchasesPerMonth()));
                case "5) Low stock products (qty < 10)" -> reportTable.setItems(FXCollections.observableArrayList(ReportDAO.lowStockProducts(10)));
                case "6) Products nearing expiry (7 days)" -> reportTable.setItems(FXCollections.observableArrayList(ReportDAO.nearExpiryProducts(7)));
                case "7) Total revenue per branch" -> reportTable.setItems(FXCollections.observableArrayList(ReportDAO.totalRevenuePerBranch()));
                case "8) Total sold quantity per product" -> reportTable.setItems(FXCollections.observableArrayList(ReportDAO.totalSoldQtyPerProduct()));
                case "9) Total revenue per product" -> reportTable.setItems(FXCollections.observableArrayList(ReportDAO.totalRevenuePerProduct()));
                case "10) Products never sold" -> reportTable.setItems(FXCollections.observableArrayList(ReportDAO.productsNeverSold()));
                case "11) Clients with no sales" -> reportTable.setItems(FXCollections.observableArrayList(ReportDAO.clientsWithNoSales()));
                case "12) Monthly sales totals (revenue + qty)" -> reportTable.setItems(FXCollections.observableArrayList(ReportDAO.monthlySalesTotals()));
                case "13) Monthly profit estimate (sales - purchases)" -> reportTable.setItems(FXCollections.observableArrayList(ReportDAO.monthlyProfitEstimate()));
                case "14) Average selling price per product" -> reportTable.setItems(FXCollections.observableArrayList(ReportDAO.avgSellingPricePerProduct()));
                case "15) Average purchase price per product" -> reportTable.setItems(FXCollections.observableArrayList(ReportDAO.avgPurchasePricePerProduct()));
                case "16) Top clients by purchased quantity" -> reportTable.setItems(FXCollections.observableArrayList(ReportDAO.topClientsByQuantity()));
                case "17) Top farmers by supplied quantity" -> reportTable.setItems(FXCollections.observableArrayList(ReportDAO.topFarmersBySuppliedQuantity()));
                case "18) Purchases per branch per month" -> reportTable.setItems(FXCollections.observableArrayList(ReportDAO.purchasesPerBranchPerMonth()));
                case "19) Sales per product per branch" -> reportTable.setItems(FXCollections.observableArrayList(ReportDAO.salesPerProductPerBranch()));
                case "20) Stock value per warehouse" -> reportTable.setItems(FXCollections.observableArrayList(ReportDAO.stockValuePerWarehouse()));
            }

            status.setText("Status: Report loaded");

        } catch (Exception ex) {
            status.setText("Status: Report failed " + ex.getMessage());
        }
    }

    private void setupReportTable() {
        col1 = new TableColumn<>("Col1");
        col1.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().col1));

        col2 = new TableColumn<>("Col2");
        col2.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().col2));

        col3 = new TableColumn<>("Col3");
        col3.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().col3));

        col4 = new TableColumn<>("Col4");
        col4.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().col4));

        reportTable.getColumns().setAll(col1, col2, col3, col4);
        reportTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    // ===== Dynamic Headers =====
    private void applyHeadersFor(String reportName) {
        if (reportName == null) return;

        // default
        setHeaders("Col1", "Col2", "Col3", "Col4");

        if (reportName.startsWith("1)")) setHeaders("Branch", "Month", "Total Sales", "");
        else if (reportName.startsWith("2)")) setHeaders("Client ID", "Client Name", "Total Revenue", "");
        else if (reportName.startsWith("3)")) setHeaders("Product ID", "Product Name", "Total Sold", "");
        else if (reportName.startsWith("4)")) setHeaders("Month", "Total Spending", "Total Quantity", "");
        else if (reportName.startsWith("5)")) setHeaders("Product ID", "Product Name", "Quantity", "");
        else if (reportName.startsWith("6)")) setHeaders("Product ID", "Product Name", "Expiry Date", "");
        else if (reportName.startsWith("7)")) setHeaders("Branch ID", "Branch Name", "Total Revenue", "");
        else if (reportName.startsWith("8)")) setHeaders("Product ID", "Product Name", "Total Sold", "");
        else if (reportName.startsWith("9)")) setHeaders("Product ID", "Product Name", "Total Revenue", "");
        else if (reportName.startsWith("10)")) setHeaders("Product ID", "Product Name", "Current Qty", "");
        else if (reportName.startsWith("11)")) setHeaders("Client ID", "Client Name", "Client Type", "");
        else if (reportName.startsWith("12)")) setHeaders("Month", "Revenue", "Total Qty", "");
        else if (reportName.startsWith("13)")) setHeaders("Month", "Sales Revenue", "Purchase Cost", "Profit");
        else if (reportName.startsWith("14)")) setHeaders("Product ID", "Product Name", "Avg Selling Price", "");
        else if (reportName.startsWith("15)")) setHeaders("Product ID", "Product Name", "Avg Purchase Price", "");
        else if (reportName.startsWith("16)")) setHeaders("Client ID", "Client Name", "Total Qty", "");
        else if (reportName.startsWith("17)")) setHeaders("Farmer ID", "Farmer Name", "Total Supplied", "");
        else if (reportName.startsWith("18)")) setHeaders("Branch", "Month", "Total Spending", "");
        else if (reportName.startsWith("19)")) setHeaders("Branch", "Product", "Total Sold", "");
        else if (reportName.startsWith("20)")) setHeaders("Warehouse ID", "Warehouse", "Stock Value", "");
    }

    private void setHeaders(String h1, String h2, String h3, String h4) {
        col1.setText(h1 == null ? "" : h1);
        col2.setText(h2 == null ? "" : h2);
        col3.setText(h3 == null ? "" : h3);
        col4.setText(h4 == null ? "" : h4);

        // hide col4 if not used
        boolean show4 = h4 != null && !h4.isBlank();
        col4.setVisible(show4);
        col4.setPrefWidth(show4 ? 120 : 0);
        col4.setMinWidth(show4 ? 60 : 0);
        col4.setMaxWidth(show4 ? Double.MAX_VALUE : 0);
    }

    // ===== Helpers =====
    private Button bigBtn(String text) {
        Button b = new Button(text);
        b.setPrefHeight(38);
        b.setPrefWidth(170);
        b.setStyle("-fx-font-weight: bold; -fx-background-radius: 12;");
        return b;
    }

    private void stylePrimary(Button b) {
        b.setStyle("-fx-background-color: #2E7D32; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 12;");
    }

    private void styleSoft(Button b) {
        b.setStyle("-fx-background-color: #E8F5E9; -fx-text-fill: #1B5E20; -fx-font-weight: bold; -fx-background-radius: 12; -fx-border-color: #C8E6C9; -fx-border-radius: 12;");
    }

    private String hintFor(String reportName) {
        if (reportName == null) return "";
        if (reportName.startsWith("5)")) return "Hint: This report uses threshold = 10 (products with quantity < 10).";
        if (reportName.startsWith("6)")) return "Hint: This report shows products expiring within 7 days.";
        if (reportName.startsWith("13)")) return "Hint: Profit estimate = Sales revenue - Purchases cost (grouped by month).";
        if (reportName.startsWith("20)")) return "Hint: Stock value = SUM(price × quantity) grouped by warehouse.";
        return reportName;
    }
}
