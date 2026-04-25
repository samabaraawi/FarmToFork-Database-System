package com.farmtofork.phase3prototype;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    private final Label status = new Label("Status: Not connected");

    @Override
    public void start(Stage stage) {

        ProductsView productsView = new ProductsView(status);
        SalesView salesView = new SalesView(status);
        PurchasesView purchasesView = new PurchasesView(status);

        // ===== register refresh callbacks =====
        AppContext.setRefreshProducts(productsView::loadProductsPublic);
        AppContext.setRefreshSales(salesView::loadSalesPublic);
        AppContext.setRefreshPurchases(purchasesView::loadPurchasesPublic);

        // ===== Top bar =====
        Button btnConnect = new Button("Connect");
        btnConnect.setOnAction(e -> {
            try (var c = DBConnection.getConnection()) {
                status.setText("Status: Connected ");
            } catch (Exception ex) {
                status.setText("Status: Connection Failed   " + ex.getMessage());
            }
        });

        // --- top bar style (farm theme) ---
        status.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 13px;"
        );

        btnConnect.setStyle(
                "-fx-background-color: #A5D6A7;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 8 20 8 20;"
        );

        HBox top = new HBox(14, status, btnConnect);
        top.setPadding(new Insets(12));
        top.setStyle(
                "-fx-background-color: #2E7D32;" +     // deep green
                        "-fx-background-radius: 0 0 18 18;"
        );

        // ===== Tabs =====
        TabPane tabs = new TabPane();

        tabs.getTabs().add(new Tab("Products", productsView.getContent()));
        tabs.getTabs().add(new Tab("Categories", new CategoriesView(status).getContent()));
        tabs.getTabs().add(new Tab("Warehouses", new WarehousesView(status).getContent()));
        tabs.getTabs().add(new Tab("Branches", new BranchesView(status).getContent()));
        tabs.getTabs().add(new Tab("Clients", new ClientsView(status).getContent()));
        tabs.getTabs().add(new Tab("Farmers", new FarmersView(status).getContent()));
        tabs.getTabs().add(new Tab("Sales", salesView.getContent()));
        tabs.getTabs().add(new Tab("Purchases", purchasesView.getContent()));
        tabs.getTabs().add(new Tab("Reports", new ReportsView(status).getContent()));

        tabs.getTabs().forEach(t -> t.setClosable(false));

        styleTabs(tabs);

        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setCenter(tabs);
        root.setStyle("-fx-background-color: #F4F7F2;");

        stage.setTitle("FarmToFork | Fresh Supply Chain Dashboard");
        stage.setScene(new Scene(root, 1200, 720));
        stage.setMaximized(true);
        stage.show();

    }

    // =========================================================
    // TabPane styling (farm / agriculture theme)
    // =========================================================
    private void styleTabs(TabPane tabs) {

        tabs.setTabMinHeight(40);
        tabs.setTabMaxHeight(40);
        tabs.setTabMinWidth(130);

        tabs.setStyle(
                "-fx-background-color: #F4F7F2;" +
                        "-fx-padding: 6 10 0 10;"
        );

        for (Tab t : tabs.getTabs()) {
            t.setStyle(
                    "-fx-background-color: #E8F3E5;" +
                            "-fx-border-color: #B6D7A8;" +
                            "-fx-border-radius: 14;" +
                            "-fx-background-radius: 14;" +
                            "-fx-font-size: 14px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 6 18 6 18;"
            );
        }

        // highlight selected tab
        tabs.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            for (Tab t : tabs.getTabs()) {
                t.setStyle(
                        "-fx-background-color: #E8F3E5;" +
                                "-fx-border-color: #B6D7A8;" +
                                "-fx-border-radius: 14;" +
                                "-fx-background-radius: 14;" +
                                "-fx-font-size: 14px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-padding: 6 18 6 18;"
                );
            }
            if (newTab != null) {
                newTab.setStyle(
                        "-fx-background-color: #A5D6A7;" + // darker green
                                "-fx-border-color: #388E3C;" +
                                "-fx-border-radius: 14;" +
                                "-fx-background-radius: 14;" +
                                "-fx-font-size: 14px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-padding: 6 18 6 18;"
                );
            }
        });
    }
}
