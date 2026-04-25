package com.farmtofork.phase3prototype;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class WarehousesView {

    private final Label status;
    private final TableView<WarehouseRow> warehouseTable = new TableView<>();

    public WarehousesView(Label status) {
        this.status = status;
    }

    public Node getContent() {
        return buildWarehousesTab();
    }

    // =========================
    // UI
    // =========================
    private Node buildWarehousesTab() {
        setupWarehouseTable();

        // -------- Header --------
        Label title = new Label("Warehouses");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label sub = new Label("Manage storage locations & capacities (Add / Update / Delete)");
        sub.setStyle("-fx-text-fill: #E9F7EF;");

        VBox titleBox = new VBox(2, title, sub);

        Button btnLoad = bigBtn("Load");
        stylePrimary(btnLoad);
        btnLoad.setOnAction(e -> loadWarehouses());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(12, titleBox, spacer, btnLoad);
        header.setPadding(new Insets(14));
        header.setStyle("-fx-background-color: #1B5E20; -fx-background-radius: 10;");

        // -------- Form (auto-fill) --------
        TextField tfId = new TextField();
        tfId.setPromptText("Warehouse ID (auto)");
        tfId.setDisable(true);

        TextField tfLoc = new TextField();
        tfLoc.setPromptText("Location (e.g., Ramallah Central)");

        TextField tfCap = new TextField();
        tfCap.setPromptText("Capacity (number)");

        setWide(tfId);
        setWide(tfLoc);
        setWide(tfCap);

        Button btnAdd = bigBtn("Add");
        stylePrimary(btnAdd);

        Button btnUpdate = bigBtn("Update");
        stylePrimary(btnUpdate);

        Button btnDelete = bigBtn("Delete");
        styleDanger(btnDelete);

        Button btnClearForm = bigBtn("Clear");
        styleSoft(btnClearForm);

        btnAdd.setOnAction(e -> {
            try {
                if (tfLoc.getText().isBlank() || tfCap.getText().isBlank()) {
                    status.setText("Status: Fill warehouse fields");
                    return;
                }

                int cap;
                try { cap = Integer.parseInt(tfCap.getText().trim()); }
                catch (NumberFormatException ex) { status.setText("Status: Capacity must be a number"); return; }

                if (cap <= 0) { status.setText("Status: Capacity must be > 0"); return; }

                WarehouseDAO.insert(tfLoc.getText().trim(), cap);
                status.setText("Status: Warehouse inserted");

                loadWarehouses();
                clearForm(tfId, tfLoc, tfCap);
                warehouseTable.getSelectionModel().clearSelection();

            } catch (Exception ex) {
                status.setText("Status: Insert failed " + ex.getMessage());
            }
        });

        btnUpdate.setOnAction(e -> {
            try {
                WarehouseRow sel = warehouseTable.getSelectionModel().getSelectedItem();
                if (sel == null) { status.setText("Status: Select a warehouse row to update"); return; }

                if (tfLoc.getText().isBlank() || tfCap.getText().isBlank()) {
                    status.setText("Status: Fill warehouse fields");
                    return;
                }

                int cap;
                try { cap = Integer.parseInt(tfCap.getText().trim()); }
                catch (NumberFormatException ex) { status.setText("Status: Capacity must be a number"); return; }

                if (cap <= 0) { status.setText("Status: Capacity must be > 0"); return; }

                WarehouseDAO.update(sel.warehouseID, tfLoc.getText().trim(), cap);
                status.setText("Status: Warehouse updated");

                loadWarehouses();
                clearForm(tfId, tfLoc, tfCap);
                warehouseTable.getSelectionModel().clearSelection();

            } catch (Exception ex) {
                status.setText("Status: Update failed " + ex.getMessage());
            }
        });

        btnDelete.setOnAction(e -> {
            try {
                WarehouseRow sel = warehouseTable.getSelectionModel().getSelectedItem();
                if (sel == null) { status.setText("Status: Select a warehouse first"); return; }

                if (!confirmDelete(sel.warehouseID)) {
                    status.setText("Status: Delete cancelled");
                    return;
                }

                WarehouseDAO.delete(sel.warehouseID);
                status.setText("Status: Warehouse deleted");

                loadWarehouses();
                clearForm(tfId, tfLoc, tfCap);
                warehouseTable.getSelectionModel().clearSelection();

            } catch (Exception ex) {
                status.setText("Status: Delete failed " + ex.getMessage());
            }
        });

        btnClearForm.setOnAction(e -> {
            clearForm(tfId, tfLoc, tfCap);
            warehouseTable.getSelectionModel().clearSelection();
            status.setText("Status: Form cleared");
        });

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.setPadding(new Insets(12));

        ColumnConstraints c0 = new ColumnConstraints();
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(c0, c1);

        form.add(labelChip("Selected ID"), 0, 0);
        form.add(tfId, 1, 0);

        form.add(labelChip("Location"), 0, 1);
        form.add(tfLoc, 1, 1);

        form.add(labelChip("Capacity"), 0, 2);
        form.add(tfCap, 1, 2);

        HBox btnRow = new HBox(10, btnAdd, btnUpdate, btnDelete, btnClearForm);
        btnRow.setPadding(new Insets(0, 12, 12, 12));

        VBox formCard = new VBox(10, form, btnRow);
        formCard.setStyle(cardStyle());

        // -------- Auto-fill on row select --------
        warehouseTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, sel) -> {
            if (sel == null) return;
            tfId.setText(String.valueOf(sel.warehouseID));
            tfLoc.setText(sel.location == null ? "" : sel.location);
            tfCap.setText(String.valueOf(sel.capacity));
            status.setText("Status: Selected warehouse loaded (ready to update)");
        });

        // -------- Search by ID --------
        TextField tfSearch = new TextField();
        tfSearch.setPromptText("Search by Warehouse ID");
        setWide(tfSearch);

        Button btnSearch = bigBtn("Search");
        styleSecondary(btnSearch);
        btnSearch.setOnAction(e -> {
            try {
                if (tfSearch.getText().isBlank()) { status.setText("Status: Enter Warehouse ID"); return; }

                int id;
                try { id = Integer.parseInt(tfSearch.getText().trim()); }
                catch (NumberFormatException ex) { status.setText("Status: ID must be a number"); return; }

                WarehouseRow row = WarehouseDAO.getByID(id);
                if (row == null) {
                    warehouseTable.setItems(FXCollections.observableArrayList());
                    status.setText("Status: No warehouse found for ID = " + id);
                    return;
                }

                warehouseTable.setItems(FXCollections.observableArrayList(row));
                status.setText("Status: Search result loaded");

            } catch (Exception ex) {
                status.setText("Status: Search failed " + ex.getMessage());
            }
        });

        Button btnClearSearch = bigBtn("Clear Search");
        styleSoft(btnClearSearch);
        btnClearSearch.setOnAction(e -> {
            tfSearch.clear();
            loadWarehouses();
        });

        HBox searchBox = new HBox(10, tfSearch, btnSearch, btnClearSearch);
        searchBox.setPadding(new Insets(12));
        searchBox.setStyle(cardStyle());

        // -------- Table Card --------
        VBox tableCard = new VBox(10, warehouseTable);
        tableCard.setPadding(new Insets(12));
        tableCard.setStyle(cardStyle());

        loadWarehouses();

        VBox content = new VBox(12, header, formCard, searchBox, tableCard);
        content.setPadding(new Insets(12));
        content.setStyle("-fx-background-color: #F1F8E9;");

        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        return sp;
    }

    // =========================
    // Actions
    // =========================
    private void loadWarehouses() {
        try {
            warehouseTable.setItems(FXCollections.observableArrayList(WarehouseDAO.getAll()));
            status.setText("Status: Warehouses loaded");
        } catch (Exception ex) {
            status.setText("Status: Load failed " + ex.getMessage());
        }
    }

    private boolean confirmDelete(int id) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Confirm Delete");
        a.setHeaderText("Delete Warehouse");
        a.setContentText("Are you sure you want to delete Warehouse ID = " + id + " ?");

        ButtonType del = new ButtonType("Delete");
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        a.getButtonTypes().setAll(del, cancel);

        return a.showAndWait().orElse(cancel) == del;
    }

    private void setupWarehouseTable() {
        TableColumn<WarehouseRow, Number> cId = new TableColumn<>("ID");
        cId.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().warehouseID));

        TableColumn<WarehouseRow, String> cLoc = new TableColumn<>("Location");
        cLoc.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().location));

        TableColumn<WarehouseRow, Number> cCap = new TableColumn<>("Capacity");
        cCap.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().capacity));

        warehouseTable.getColumns().setAll(cId, cLoc, cCap);
        warehouseTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        warehouseTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        warehouseTable.setPrefHeight(420);
        warehouseTable.setStyle("-fx-background-radius: 10; -fx-border-color: #C8E6C9; -fx-border-radius: 10;");
    }

    private void clearForm(TextField tfId, TextField tfLoc, TextField tfCap) {
        tfId.clear();
        tfLoc.clear();
        tfCap.clear();
    }

    // =========================
    // Styling helpers
    // =========================
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
        b.setPrefWidth(160);
        b.setStyle("-fx-font-weight: bold; -fx-background-radius: 12;");
        return b;
    }

    private void stylePrimary(Button b) {
        b.setStyle("-fx-background-color: #2E7D32; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 12;");
    }

    private void styleSecondary(Button b) {
        b.setStyle("-fx-background-color: #558B2F; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 12;");
    }

    private void styleSoft(Button b) {
        b.setStyle("-fx-background-color: #E8F5E9; -fx-text-fill: #1B5E20; -fx-font-weight: bold; -fx-background-radius: 12; -fx-border-color: #C8E6C9; -fx-border-radius: 12;");
    }

    private void styleDanger(Button b) {
        b.setStyle("-fx-background-color: #C62828; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 12;");
    }

    private void setWide(Control c) {
        c.setPrefHeight(36);
        c.setMaxWidth(Double.MAX_VALUE);
    }
}
