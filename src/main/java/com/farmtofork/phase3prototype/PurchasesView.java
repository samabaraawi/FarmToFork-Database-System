package com.farmtofork.phase3prototype;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class PurchasesView {

    private final Label status;
    private final TableView<PurchaseRow> purchaseTable = new TableView<>();

    public PurchasesView(Label status) {
        this.status = status;
    }

    public Node getContent() {
        return buildPurchasesTab();
    }

    public void loadPurchasesPublic() {
        loadPurchases();
    }

    private Node buildPurchasesTab() {

        setupPurchaseTable();

        // ===== Header =====
        Label title = new Label("Purchases");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label sub = new Label("Track farmer supplies and update product stock automatically");
        sub.setStyle("-fx-text-fill: #E9F7EF;");

        VBox titleBox = new VBox(2, title, sub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnLoad = bigBtn("Load Purchases");
        styleSoft(btnLoad);
        btnLoad.setOnAction(e -> loadPurchases());

        Button btnRefreshLists = bigBtn("Refresh Lists");
        styleSoft(btnRefreshLists);

        HBox header = new HBox(12, titleBox, spacer, btnLoad, btnRefreshLists);
        header.setPadding(new Insets(14));
        header.setStyle("-fx-background-color: #1B5E20; -fx-background-radius: 10;");

        // ===== Fields =====
        DatePicker dpPurchaseDate = new DatePicker();
        dpPurchaseDate.setPromptText("Purchase Date");
        dpPurchaseDate.setPrefHeight(38);

        DatePicker dpEntryDate = new DatePicker();
        dpEntryDate.setPromptText("Warehouse Entry Date");
        dpEntryDate.setPrefHeight(38);

        TextField tfQty = new TextField();
        tfQty.setPromptText("Quantity");
        tfQty.setPrefHeight(38);

        TextField tfUnit = new TextField();
        tfUnit.setPromptText("Unit Price");
        tfUnit.setPrefHeight(38);

        ComboBox<IdName> cbFarmer = new ComboBox<>();
        ComboBox<IdName> cbProduct = new ComboBox<>();
        ComboBox<IdName> cbBranch = new ComboBox<>();

        cbFarmer.setPromptText("Farmer");
        cbProduct.setPromptText("Product");
        cbBranch.setPromptText("Branch");

        cbFarmer.setPrefHeight(38);
        cbProduct.setPrefHeight(38);
        cbBranch.setPrefHeight(38);

        cbFarmer.setMaxWidth(Double.MAX_VALUE);
        cbProduct.setMaxWidth(Double.MAX_VALUE);
        cbBranch.setMaxWidth(Double.MAX_VALUE);

        // selected purchaseID for update mode
        final int[] selectedPurchaseID = {-1};

        btnRefreshLists.setOnAction(e -> {
            try {
                LookupUtil.loadLookupLists(status, cbProduct, null, cbFarmer, cbBranch);
                status.setText("Status: Lists refreshed");
            } catch (Exception ex) {
                status.setText("Status: Refresh failed " + ex.getMessage());
            }
        });

        // initial lists
        LookupUtil.loadLookupLists(status, cbProduct, null, cbFarmer, cbBranch);

        // ===== Buttons =====
        Button btnAdd = bigBtn("Add Purchase");
        stylePrimary(btnAdd);

        Button btnUpdate = bigBtn("Update");
        stylePrimary(btnUpdate);

        Button btnDelete = bigBtn("Delete");
        styleDanger(btnDelete);

        Button btnClear = bigBtn("Clear Form");
        styleSoft(btnClear);

        // ===== Actions =====
        btnAdd.setOnAction(e -> {
            try {
                if (!validatePurchaseForm(dpPurchaseDate, dpEntryDate, tfQty, tfUnit, cbFarmer, cbProduct, cbBranch)) return;

                int qty = Integer.parseInt(tfQty.getText().trim());
                double unit = Double.parseDouble(tfUnit.getText().trim());

                PurchaseDAO.insertPurchase(
                        java.sql.Date.valueOf(dpPurchaseDate.getValue()),
                        qty, unit,
                        java.sql.Date.valueOf(dpEntryDate.getValue()),
                        cbFarmer.getValue().id,
                        cbProduct.getValue().id,
                        cbBranch.getValue().id
                );

                status.setText("Status: Purchase inserted (Stock increased)");
                loadPurchases();
                AppContext.refreshProducts.run();

                clearPurchaseForm(selectedPurchaseID, dpPurchaseDate, dpEntryDate, tfQty, tfUnit, cbFarmer, cbProduct, cbBranch);

            } catch (Exception ex) {
                status.setText("Status: Purchase failed " + ex.getMessage());
            }
        });

        btnUpdate.setOnAction(e -> {
            try {
                if (selectedPurchaseID[0] == -1) {
                    status.setText("Status: Select a purchase row to update");
                    return;
                }
                if (!validatePurchaseForm(dpPurchaseDate, dpEntryDate, tfQty, tfUnit, cbFarmer, cbProduct, cbBranch)) return;

                int qty = Integer.parseInt(tfQty.getText().trim());
                double unit = Double.parseDouble(tfUnit.getText().trim());

                PurchaseDAO.updatePurchase(
                        selectedPurchaseID[0],
                        java.sql.Date.valueOf(dpPurchaseDate.getValue()),
                        qty, unit,
                        java.sql.Date.valueOf(dpEntryDate.getValue()),
                        cbFarmer.getValue().id,
                        cbProduct.getValue().id,
                        cbBranch.getValue().id
                );

                status.setText("Status: Purchase updated (Stock adjusted)");
                loadPurchases();
                AppContext.refreshProducts.run();

                clearPurchaseForm(selectedPurchaseID, dpPurchaseDate, dpEntryDate, tfQty, tfUnit, cbFarmer, cbProduct, cbBranch);

            } catch (Exception ex) {
                status.setText("Status: Update failed " + ex.getMessage());
            }
        });

        btnDelete.setOnAction(e -> {
            try {
                PurchaseRow sel = purchaseTable.getSelectionModel().getSelectedItem();
                if (sel == null) { status.setText("Status: Select a purchase row first"); return; }

                if (!confirmDeletePurchase(sel.purchaseID)) {
                    status.setText("Status: Delete cancelled");
                    return;
                }

                PurchaseDAO.deletePurchase(sel.purchaseID);

                status.setText("Status: Purchase deleted (Stock decreased)");
                loadPurchases();
                AppContext.refreshProducts.run();

                if (selectedPurchaseID[0] == sel.purchaseID) {
                    clearPurchaseForm(selectedPurchaseID, dpPurchaseDate, dpEntryDate, tfQty, tfUnit, cbFarmer, cbProduct, cbBranch);
                }

            } catch (Exception ex) {
                status.setText("Status: Delete failed " + ex.getMessage());
            }
        });

        btnClear.setOnAction(e -> {
            clearPurchaseForm(selectedPurchaseID, dpPurchaseDate, dpEntryDate, tfQty, tfUnit, cbFarmer, cbProduct, cbBranch);
            purchaseTable.getSelectionModel().clearSelection();
            status.setText("Status: Form cleared");
        });

        // ===== Auto-fill on row select =====
        purchaseTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, sel) -> {
            if (sel == null) return;

            selectedPurchaseID[0] = sel.purchaseID;

            try { dpPurchaseDate.setValue(java.time.LocalDate.parse(sel.purchaseDate)); }
            catch (Exception ex) { dpPurchaseDate.setValue(null); }

            try {
                if (sel.warehouseEntryDate == null || sel.warehouseEntryDate.isBlank()) dpEntryDate.setValue(null);
                else dpEntryDate.setValue(java.time.LocalDate.parse(sel.warehouseEntryDate));
            } catch (Exception ex) {
                dpEntryDate.setValue(null);
            }

            tfQty.setText(String.valueOf(sel.quantity));
            tfUnit.setText(String.valueOf(sel.unitPrice));

            cbFarmer.setValue(null);
            cbProduct.setValue(null);
            cbBranch.setValue(null);

            for (IdName x : cbFarmer.getItems()) if (x.id == sel.farmerID) { cbFarmer.setValue(x); break; }
            for (IdName x : cbProduct.getItems()) if (x.id == sel.productID) { cbProduct.setValue(x); break; }
            for (IdName x : cbBranch.getItems()) if (x.id == sel.branchID) { cbBranch.setValue(x); break; }

            status.setText("Status: Selected purchase loaded (ready to update)");
        });

        // ===== Layout cards =====
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(12));

        ColumnConstraints col = new ColumnConstraints();
        col.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col, col, col, col);

        grid.add(labelChip("Purchase Date"), 0, 0);
        grid.add(dpPurchaseDate, 0, 1);

        grid.add(labelChip("Quantity"), 1, 0);
        grid.add(tfQty, 1, 1);

        grid.add(labelChip("Unit Price"), 2, 0);
        grid.add(tfUnit, 2, 1);

        grid.add(labelChip("Entry Date"), 3, 0);
        grid.add(dpEntryDate, 3, 1);

        grid.add(labelChip("Farmer"), 0, 2);
        grid.add(cbFarmer, 0, 3);

        grid.add(labelChip("Product"), 1, 2);
        grid.add(cbProduct, 1, 3);

        grid.add(labelChip("Branch"), 2, 2);
        grid.add(cbBranch, 2, 3);

        HBox btnRow = new HBox(10, btnAdd, btnUpdate, btnDelete, btnClear);
        btnRow.setPadding(new Insets(12, 12, 0, 12));

        VBox formCard = new VBox(6, grid, btnRow);
        formCard.setStyle(cardStyle());

        VBox tableCard = new VBox(10, purchaseTable);
        tableCard.setPadding(new Insets(12));
        tableCard.setStyle(cardStyle());

        VBox content = new VBox(12, header, formCard, tableCard);
        content.setPadding(new Insets(12));
        content.setStyle("-fx-background-color: #F1F8E9;");

        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return sp;
    }

    private void loadPurchases() {
        try {
            purchaseTable.setItems(FXCollections.observableArrayList(PurchaseDAO.getAllPurchases()));
            status.setText("Status: Purchases loaded");
        } catch (Exception ex) {
            status.setText("Status: Load purchases failed " + ex.getMessage());
        }
    }

    private void setupPurchaseTable() {
        TableColumn<PurchaseRow, Number> cId = new TableColumn<>("PurchaseID");
        cId.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().purchaseID));

        TableColumn<PurchaseRow, String> cDate = new TableColumn<>("Purchase Date");
        cDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().purchaseDate));

        TableColumn<PurchaseRow, Number> cQty = new TableColumn<>("Qty");
        cQty.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().quantity));

        TableColumn<PurchaseRow, Number> cUnit = new TableColumn<>("Unit Price");
        cUnit.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().unitPrice));

        TableColumn<PurchaseRow, Number> cTot = new TableColumn<>("Total Cost");
        cTot.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().totalCost));

        TableColumn<PurchaseRow, String> cEntry = new TableColumn<>("Entry Date");
        cEntry.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().warehouseEntryDate));

        TableColumn<PurchaseRow, Number> cFarmer = new TableColumn<>("FarmerID");
        cFarmer.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().farmerID));

        TableColumn<PurchaseRow, Number> cProd = new TableColumn<>("ProductID");
        cProd.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().productID));

        TableColumn<PurchaseRow, Number> cBranch = new TableColumn<>("BranchID");
        cBranch.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().branchID));

        purchaseTable.getColumns().setAll(cId, cDate, cQty, cUnit, cTot, cEntry, cFarmer, cProd, cBranch);
        purchaseTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        purchaseTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // make table easier to read
        purchaseTable.setStyle("-fx-background-radius: 10; -fx-border-color: #C8E6C9; -fx-border-radius: 10;");
    }

    // ===== Validation =====
    private boolean validatePurchaseForm(DatePicker dpPurchaseDate,
                                         DatePicker dpEntryDate,
                                         TextField tfQty,
                                         TextField tfUnit,
                                         ComboBox<IdName> cbFarmer,
                                         ComboBox<IdName> cbProduct,
                                         ComboBox<IdName> cbBranch) {

        if (dpPurchaseDate.getValue() == null || dpEntryDate.getValue() == null ||
                tfQty.getText().isBlank() || tfUnit.getText().isBlank() ||
                cbFarmer.getValue() == null || cbProduct.getValue() == null || cbBranch.getValue() == null) {
            status.setText("Status: Fill all purchase fields");
            return false;
        }

        int qty;
        double unit;
        try {
            qty = Integer.parseInt(tfQty.getText().trim());
            unit = Double.parseDouble(tfUnit.getText().trim());
        } catch (NumberFormatException ex) {
            status.setText("Status: Quantity/Price must be numbers");
            return false;
        }

        if (qty <= 0) { status.setText("Status: Quantity must be > 0"); return false; }
        if (unit <= 0) { status.setText("Status: Unit price must be > 0"); return false; }

        return true;
    }

    private void clearPurchaseForm(int[] selectedPurchaseID,
                                   DatePicker dpPurchaseDate,
                                   DatePicker dpEntryDate,
                                   TextField tfQty,
                                   TextField tfUnit,
                                   ComboBox<IdName> cbFarmer,
                                   ComboBox<IdName> cbProduct,
                                   ComboBox<IdName> cbBranch) {

        selectedPurchaseID[0] = -1;
        dpPurchaseDate.setValue(null);
        dpEntryDate.setValue(null);
        tfQty.clear();
        tfUnit.clear();
        cbFarmer.setValue(null);
        cbProduct.setValue(null);
        cbBranch.setValue(null);
    }

    // ===== Confirm delete =====
    private boolean confirmDeletePurchase(int purchaseID) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Confirm Delete");
        a.setHeaderText("Delete Purchase");
        a.setContentText("Are you sure you want to delete Purchase ID = " + purchaseID + " ?");

        ButtonType yes = new ButtonType("Delete");
        ButtonType no = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        a.getButtonTypes().setAll(yes, no);

        var res = a.showAndWait();
        return res.isPresent() && res.get() == yes;
    }

    // ===== UI Helpers =====
    private String cardStyle() {
        return "-fx-background-color: white;" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: #C8E6C9;" +
                "-fx-border-radius: 10;";
    }

    private Label labelChip(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: #1B5E20; -fx-font-weight: bold;");
        return l;
    }

    private Button bigBtn(String text) {
        Button b = new Button(text);
        b.setPrefHeight(38);
        b.setPrefWidth(150);
        b.setStyle("-fx-font-weight: bold; -fx-background-radius: 12;");
        return b;
    }

    private void stylePrimary(Button b) {
        b.setStyle("-fx-background-color: #2E7D32; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 12;");
    }

    private void styleSoft(Button b) {
        b.setStyle("-fx-background-color: #E8F5E9; -fx-text-fill: #1B5E20; -fx-font-weight: bold; -fx-background-radius: 12; -fx-border-color: #C8E6C9; -fx-border-radius: 12;");
    }

    private void styleDanger(Button b) {
        b.setStyle("-fx-background-color: #C62828; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 12;");
    }
}
