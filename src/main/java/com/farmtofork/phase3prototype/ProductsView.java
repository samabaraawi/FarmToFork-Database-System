package com.farmtofork.phase3prototype;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.Date;

public class ProductsView {

    private final Label status;
    private final TableView<ProductRow> productTable = new TableView<>();

    // UI fields
    private TextField tfSearch;

    private TextField tfName;
    private DatePicker dpExpiry;
    private TextField tfPrice;
    private TextField tfCategory;
    private TextField tfWarehouse;

    private ComboBox<IdName> cbUpdateProd;
    private TextField tfNewPrice;

    public ProductsView(Label status) {
        this.status = status;
    }

    public Node getContent() {
        return buildProductsTab();
    }

    public void loadProductsPublic() {
        loadProducts();
    }

    // =========================
    // Products Tab UI
    // =========================
    private Node buildProductsTab() {
        setupProductTable();

        // ---------- Header ----------
        Label title = new Label("Products");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label subtitle = new Label("Add / Update / Delete products. Stock is managed via Purchases/Sales.");
        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #E9F7EF;");

        VBox titleBox = new VBox(2, title, subtitle);

        Button btnLoad = bigBtn("Load");
        stylePrimary(btnLoad);
        btnLoad.setOnAction(e -> loadProducts());

        Button btnRefreshLists = bigBtn("Refresh Lists");
        styleSecondary(btnRefreshLists);
        btnRefreshLists.setOnAction(e -> refreshProductLists());

        Button btnClearForm = bigBtn("Clear Form");
        styleDanger(btnClearForm);
        btnClearForm.setOnAction(e -> clearForm());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(12, titleBox, spacer, btnLoad, btnRefreshLists, btnClearForm);
        header.setPadding(new Insets(14));
        header.setStyle(
                "-fx-background-color: #1B5E20;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: #124017;" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-width: 1;"
        );

        // ---------- Search ----------
        tfSearch = new TextField();
        tfSearch.setPromptText("Search by Product ID");
        tfSearch.setPrefHeight(36);
        tfSearch.setPrefWidth(520);

        Button btnSearch = bigBtn("Search");
        stylePrimary(btnSearch);
        btnSearch.setOnAction(e -> doSearch());

        Button btnClearSearch = bigBtn("Clear");
        styleSecondary(btnClearSearch);
        btnClearSearch.setOnAction(e -> {
            tfSearch.clear();
            loadProducts();
            status.setText("Status: Search cleared");
        });

        HBox searchBar = new HBox(12, new Label("Search:"), tfSearch, btnSearch, btnClearSearch);
        searchBar.setPadding(new Insets(10));
        searchBar.setStyle("-fx-alignment: center-left;");

        TitledPane searchPane = titled("Search Products", searchBar);

        // ---------- Add Form ----------
        tfName = new TextField(); tfName.setPromptText("Name");
        dpExpiry = new DatePicker(); dpExpiry.setPromptText("Expiry Date");
        tfPrice = new TextField(); tfPrice.setPromptText("Price");
        tfCategory = new TextField(); tfCategory.setPromptText("CategoryID");
        tfWarehouse = new TextField(); tfWarehouse.setPromptText("WarehouseID");

        setWide(tfName);
        setWide(dpExpiry);
        setWide(tfPrice);
        setWide(tfCategory);
        setWide(tfWarehouse);

        Button btnAdd = bigBtn("Add Product");
        stylePrimary(btnAdd);
        btnAdd.setOnAction(e -> insertProduct());

        GridPane addForm = new GridPane();
        addForm.setHgap(12);
        addForm.setVgap(12);
        addForm.setPadding(new Insets(12));

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(140);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        addForm.getColumnConstraints().addAll(col1, col2);

        addForm.add(new Label("Name:"), 0, 0);         addForm.add(tfName, 1, 0);
        addForm.add(new Label("Expiry Date:"), 0, 1);  addForm.add(dpExpiry, 1, 1);
        addForm.add(new Label("Price:"), 0, 2);        addForm.add(tfPrice, 1, 2);
        addForm.add(new Label("Category ID:"), 0, 3);  addForm.add(tfCategory, 1, 3);
        addForm.add(new Label("Warehouse ID:"), 0, 4); addForm.add(tfWarehouse, 1, 4);

        addForm.add(new Separator(), 0, 5, 2, 1);

        HBox addActions = new HBox(12, btnAdd);
        addActions.setStyle("-fx-alignment: center-left;");
        addForm.add(addActions, 1, 6);

        TitledPane addPane = titled("Add New Product", addForm);

        // ---------- Update / Delete (auto-fill on row select) ----------
        cbUpdateProd = new ComboBox<>();
        cbUpdateProd.setPromptText("Select Product");
        setWide(cbUpdateProd);

        tfNewPrice = new TextField(); tfNewPrice.setPromptText("New Price");
        setWide(tfNewPrice);

        Button btnUpdate = bigBtn("Update Price");
        styleSecondary(btnUpdate);
        btnUpdate.setOnAction(e -> updateProduct());

        Button btnDelete = new Button("Delete Product");
        btnDelete.setOnAction(e -> {
            try {
                ProductRow sel = productTable.getSelectionModel().getSelectedItem();
                if (sel == null) { status.setText("Status: Select a product row first"); return; }

                if (!confirmDeleteProduct(sel.productID)) {
                    status.setText("Status: Delete cancelled");
                    return;
                }

                ProductDAO.deleteProduct(sel.productID);
                status.setText("Status: Product deleted");
                loadProducts();
                AppContext.refreshSales.run();
                AppContext.refreshPurchases.run();

            } catch (Exception ex) {
                status.setText("Status: Delete failed  " + ex.getMessage());
            }
        });

        GridPane updateForm = new GridPane();
        updateForm.setHgap(12);
        updateForm.setVgap(12);
        updateForm.setPadding(new Insets(12));
        updateForm.getColumnConstraints().addAll(col1, col2);

        updateForm.add(new Label("Selected Product:"), 0, 0); updateForm.add(cbUpdateProd, 1, 0);
        updateForm.add(new Label("New Price:"), 0, 1);        updateForm.add(tfNewPrice, 1, 1);

        updateForm.add(new Separator(), 0, 2, 2, 1);

        HBox updateActions = new HBox(12, btnUpdate, btnDelete);
        updateActions.setStyle("-fx-alignment: center-left;");
        updateForm.add(updateActions, 1, 3);

        TitledPane updatePane = titled("Update / Delete (Auto-fill on row selection)", updateForm);

        // ---------- Table Card ----------
        Label tableTitle = new Label("Products Table");
        tableTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1B5E20;");

        productTable.setPrefHeight(520);
        productTable.setStyle("-fx-background-radius: 10;");

        VBox tableBox = new VBox(8, tableTitle, productTable);
        tableBox.setPadding(new Insets(12));
        tableBox.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: #C8E6C9;" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-width: 1;"
        );

        // ---------- Auto-fill on selection ----------
        productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, sel) -> {
            if (sel == null) return;
            fillUpdateFromRow(sel);
        });

        // initial load
        refreshProductLists();
        loadProducts();

        VBox content = new VBox(12, header, searchPane, addPane, updatePane, tableBox);
        content.setPadding(new Insets(12));
        content.setStyle("-fx-background-color: #F1F8E9;");

        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setPannable(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        return sp;
    }

    // =========================
    // Actions
    // =========================

    private void doSearch() {
        try {
            String text = (tfSearch == null) ? "" : tfSearch.getText().trim();

            if (text.isEmpty()) {
                loadProducts();
                status.setText("Status: Showing all products");
                return;
            }

            int productID;
            try {
                productID = Integer.parseInt(text);
            } catch (NumberFormatException ex) {
                status.setText("Status: Product ID must be a number");
                return;
            }

            ProductRow row = ProductDAO.getProductByID(productID);

            if (row == null) {
                productTable.getItems().clear();
                status.setText("Status: No product found with ID " + productID);
            } else {
                productTable.setItems(FXCollections.observableArrayList(row));
                status.setText("Status: Product " + productID + " found");
            }

        } catch (Exception ex) {
            status.setText("Status: Search failed " + ex.getMessage());
        }
    }

    private void insertProduct() {
        try {
            if (tfName.getText().isBlank() || dpExpiry.getValue() == null ||
                    tfPrice.getText().isBlank() ||
                    tfCategory.getText().isBlank() || tfWarehouse.getText().isBlank()) {
                status.setText("Status: Fill all product fields");
                return;
            }

            String name = tfName.getText().trim();

            double price;
            int catID, whID;

            try {
                price = Double.parseDouble(tfPrice.getText().trim());
                catID = Integer.parseInt(tfCategory.getText().trim());
                whID = Integer.parseInt(tfWarehouse.getText().trim());
            } catch (NumberFormatException ex) {
                status.setText("Status: Price/IDs must be numbers");
                return;
            }

            if (price <= 0) { status.setText("Status: Price must be > 0"); return; }

            Date expiry = Date.valueOf(dpExpiry.getValue());

            // quantity is managed via Purchases/Sales, not here
            ProductDAO.insertProduct(name, expiry, price, catID, whID);

            status.setText("Status: Product inserted (stock starts at 0; add stock via Purchases)");
            loadProducts();
            refreshProductLists();
            clearForm();

        } catch (Exception ex) {
            status.setText("Status: Insert product failed " + ex.getMessage());
        }
    }

    private void updateProduct() {
        try {
            if (cbUpdateProd.getValue() == null || tfNewPrice.getText().isBlank()) {
                status.setText("Status: Select product + enter new price");
                return;
            }

            int id = cbUpdateProd.getValue().id;

            double newPrice;
            try {
                newPrice = Double.parseDouble(tfNewPrice.getText().trim());
            } catch (NumberFormatException ex) {
                status.setText("Status: Price must be a number");
                return;
            }

            if (newPrice <= 0) { status.setText("Status: Price must be > 0"); return; }

            // quantity is managed via Purchases/Sales, not here
            ProductDAO.updateProduct(id, newPrice);

            status.setText("Status: Product price updated");
            loadProducts();
            refreshProductLists();

            tfNewPrice.clear();

        } catch (Exception ex) {
            status.setText("Status: Update failed " + ex.getMessage());
        }
    }

    private void deleteProduct() {
        try {
            if (cbUpdateProd.getValue() == null) {
                status.setText("Status: Select a product to delete");
                return;
            }

            int id = cbUpdateProd.getValue().id;

            ProductDAO.deleteProduct(id);

            status.setText("Status: Product deleted");
            loadProducts();
            refreshProductLists();

            cbUpdateProd.setValue(null);
            tfNewPrice.clear();

        } catch (Exception ex) {
            status.setText("Status: Delete failed " + ex.getMessage());
        }
    }

    private void fillUpdateFromRow(ProductRow sel) {
        try {
            cbUpdateProd.setValue(null);
            for (IdName x : cbUpdateProd.getItems()) {
                if (x.id == sel.productID) { cbUpdateProd.setValue(x); break; }
            }

            tfNewPrice.setText(String.valueOf(sel.price));

            status.setText("Status: Product selected (ready to update/delete)");
        } catch (Exception ex) {
            status.setText("Status: Auto-fill failed " + ex.getMessage());
        }
    }

    private void refreshProductLists() {
        LookupUtil.loadLookupLists(status, cbUpdateProd, null, null, null);
    }

    private void clearForm() {
        if (tfSearch != null) tfSearch.clear();

        if (tfName != null) tfName.clear();
        if (dpExpiry != null) dpExpiry.setValue(null);
        if (tfPrice != null) tfPrice.clear();
        if (tfCategory != null) tfCategory.clear();
        if (tfWarehouse != null) tfWarehouse.clear();

        if (cbUpdateProd != null) cbUpdateProd.setValue(null);
        if (tfNewPrice != null) tfNewPrice.clear();

        status.setText("Status: Form cleared");
    }


    private void loadProducts() {
        try {
            productTable.setItems(FXCollections.observableArrayList(ProductDAO.getAllProducts()));
            status.setText("Status: Products loaded ");
        } catch (Exception ex) {
            status.setText("Status: Load products failed  " + ex.getMessage());
        }
    }

    private boolean confirmDeleteProduct(int productID) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Confirm Delete");
        a.setHeaderText("Delete Product");
        a.setContentText("Are you sure you want to delete Product ID = " + productID + " ?");

        ButtonType yes = new ButtonType("Delete");
        ButtonType no = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        a.getButtonTypes().setAll(yes, no);

        var res = a.showAndWait();
        return res.isPresent() && res.get() == yes;
    }

    private void setupProductTable() {
        TableColumn<ProductRow, Number> cId = new TableColumn<>("ID");
        cId.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().productID));

        TableColumn<ProductRow, String> cName = new TableColumn<>("Name");
        cName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().name));

        TableColumn<ProductRow, String> cExp = new TableColumn<>("Expiry");
        cExp.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().expiryDate));

        TableColumn<ProductRow, Number> cPrice = new TableColumn<>("Price");
        cPrice.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().price));

        TableColumn<ProductRow, Number> cQty = new TableColumn<>("Qty");
        cQty.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().quantity));

        TableColumn<ProductRow, Number> cCat = new TableColumn<>("CategoryID");
        cCat.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().categoryID));

        TableColumn<ProductRow, Number> cWh = new TableColumn<>("WarehouseID");
        cWh.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().warehouseID));

        productTable.getColumns().setAll(cId, cName, cExp, cPrice, cQty, cCat, cWh);
        productTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        productTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    // =========================
    // Styling helpers
    // =========================
    private Button bigBtn(String text) {
        Button b = new Button(text);
        b.setPrefHeight(38);
        b.setPrefWidth(150);
        b.setStyle("-fx-background-radius: 12; -fx-font-weight: bold;");
        return b;
    }

    private void stylePrimary(Button b) {
        b.setStyle("-fx-background-color: #2E7D32; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 12; -fx-padding: 8 18 8 18;");
    }

    private void styleSecondary(Button b) {
        b.setStyle("-fx-background-color: #E8F5E9; -fx-text-fill: #1B5E20; -fx-font-weight: bold; -fx-background-radius: 12; -fx-padding: 8 18 8 18;");
    }

    private void styleDanger(Button b) {
        b.setStyle("-fx-background-color: #C62828; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 12; -fx-padding: 8 18 8 18;");
    }

    private void setWide(Control c) {
        c.setPrefHeight(36);
        c.setMaxWidth(Double.MAX_VALUE);
    }

    private TitledPane titled(String title, Node content) {
        TitledPane tp = new TitledPane(title, content);
        tp.setCollapsible(false);
        tp.setExpanded(true);
        tp.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: #C8E6C9;" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-width: 1;"
        );
        return tp;
    }
}
