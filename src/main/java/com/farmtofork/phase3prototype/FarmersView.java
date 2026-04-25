package com.farmtofork.phase3prototype;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class FarmersView {

    private final Label status;
    private final TableView<FarmerRow> farmerTable = new TableView<>();

    public FarmersView(Label status) {
        this.status = status;
    }

    public Node getContent() {
        return buildFarmersTab();
    }

    private Node buildFarmersTab() {
        setupFarmerTable();

        // ===== Header =====
        Label title = new Label("Farmers");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label sub = new Label("Manage farmers who supply products");
        sub.setStyle("-fx-text-fill: #E9F7EF;");

        VBox titleBox = new VBox(2, title, sub);

        Button btnLoad = bigBtn("Load");
        stylePrimary(btnLoad);
        btnLoad.setOnAction(e -> loadFarmers());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(12, titleBox, spacer, btnLoad);
        header.setPadding(new Insets(14));
        header.setStyle("-fx-background-color: #1B5E20; -fx-background-radius: 10;");

        // ===== Form (Add/Update) =====
        TextField tfID = new TextField(); tfID.setPromptText("Farmer ID (auto for update)");
        tfID.setDisable(true); // ID ما بنغيره بالتحديث

        TextField tfName = new TextField(); tfName.setPromptText("Name");
        TextField tfRegion = new TextField(); tfRegion.setPromptText("Region");
        TextField tfContact = new TextField(); tfContact.setPromptText("Contact (Phone/Email)");

        setWide(tfName); setWide(tfRegion); setWide(tfContact);

        final int[] selectedFarmerID = {-1};

        // auto-fill when selecting row
        farmerTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, sel) -> {
            if (sel == null) return;

            selectedFarmerID[0] = sel.farmerID;

            tfID.setText(String.valueOf(sel.farmerID));
            tfName.setText(sel.name == null ? "" : sel.name);
            tfRegion.setText(sel.region == null ? "" : sel.region);
            tfContact.setText(sel.contactInfo == null ? "" : sel.contactInfo);

            status.setText("Status: Selected farmer loaded (ready to update)");
        });

        Button btnAdd = bigBtn("Add Farmer");
        stylePrimary(btnAdd);
        btnAdd.setOnAction(e -> {
            try {
                String name = tfName.getText().trim();
                if (name.isEmpty()) { status.setText("Status: Farmer name required"); return; }

                String region = tfRegion.getText().trim();
                String contact = tfContact.getText().trim();

                FarmerDAO.insert(name, region, contact);

                status.setText("Status: Farmer inserted");
                loadFarmers();

                clearForm(tfID, tfName, tfRegion, tfContact);
                selectedFarmerID[0] = -1;

            } catch (Exception ex) {
                status.setText("Status: Insert failed " + ex.getMessage());
            }
        });

        Button btnUpdate = bigBtn("Update");
        styleSecondary(btnUpdate);
        btnUpdate.setOnAction(e -> {
            try {
                if (selectedFarmerID[0] == -1) { status.setText("Status: Select a farmer row first"); return; }

                String name = tfName.getText().trim();
                if (name.isEmpty()) { status.setText("Status: Farmer name required"); return; }

                String region = tfRegion.getText().trim();
                String contact = tfContact.getText().trim();

                FarmerDAO.update(selectedFarmerID[0], name, region, contact);

                status.setText("Status: Farmer updated");
                loadFarmers();

            } catch (Exception ex) {
                status.setText("Status: Update failed " + ex.getMessage());
            }
        });

        Button btnClear = bigBtn("Clear");
        styleSoft(btnClear);
        btnClear.setOnAction(e -> {
            farmerTable.getSelectionModel().clearSelection();
            clearForm(tfID, tfName, tfRegion, tfContact);
            selectedFarmerID[0] = -1;
            status.setText("Status: Form cleared");
        });

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.setPadding(new Insets(12));

        form.add(new Label("Selected ID:"), 0, 0);
        form.add(tfID, 1, 0);

        form.add(new Label("Name:"), 0, 1);
        form.add(tfName, 1, 1);

        form.add(new Label("Region:"), 0, 2);
        form.add(tfRegion, 1, 2);

        form.add(new Label("Contact:"), 0, 3);
        form.add(tfContact, 1, 3);

        HBox actions = new HBox(10, btnAdd, btnUpdate, btnClear);
        form.add(actions, 1, 4);

        TitledPane formPane = titled("Add / Update Farmer", form);

        // ===== Search by Farmer ID =====
        TextField tfSearch = new TextField();
        tfSearch.setPromptText("Search by Farmer ID");
        setWide(tfSearch);

        Button btnSearch = bigBtn("Search");
        styleSecondary(btnSearch);
        btnSearch.setOnAction(e -> {
            try {
                if (tfSearch.getText().isBlank()) { status.setText("Status: Enter Farmer ID"); return; }

                int id;
                try { id = Integer.parseInt(tfSearch.getText().trim()); }
                catch (NumberFormatException ex) { status.setText("Status: ID must be a number"); return; }

                FarmerRow r = FarmerDAO.getByID(id);
                if (r == null) {
                    farmerTable.setItems(FXCollections.observableArrayList());
                    status.setText("Status: No farmer found for ID = " + id);
                    return;
                }

                farmerTable.setItems(FXCollections.observableArrayList(r));
                status.setText("Status: Search result loaded");

            } catch (Exception ex) {
                status.setText("Status: Search failed " + ex.getMessage());
            }
        });

        Button btnShowAll = bigBtn("Show All");
        styleSoft(btnShowAll);
        btnShowAll.setOnAction(e -> {
            tfSearch.clear();
            loadFarmers();
        });

        HBox searchBox = new HBox(10, tfSearch, btnSearch, btnShowAll);
        searchBox.setPadding(new Insets(12));
        searchBox.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: #C8E6C9;" +
                        "-fx-border-radius: 10;"
        );

        // ===== Table + Delete =====
        Button btnDelete = bigBtn("Delete Selected");
        styleDanger(btnDelete);
        btnDelete.setOnAction(e -> deleteSelected(tfID, tfName, tfRegion, tfContact, selectedFarmerID));

        VBox tableBox = new VBox(10, farmerTable, btnDelete);
        tableBox.setPadding(new Insets(12));
        tableBox.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: #C8E6C9;" +
                        "-fx-border-radius: 10;"
        );

        loadFarmers();

        VBox content = new VBox(12, header, formPane, searchBox, tableBox);
        content.setPadding(new Insets(12));
        content.setStyle("-fx-background-color: #F1F8E9;");

        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return sp;
    }

    private void deleteSelected(TextField tfID, TextField tfName, TextField tfRegion, TextField tfContact, int[] selectedFarmerID) {
        try {
            FarmerRow sel = farmerTable.getSelectionModel().getSelectedItem();
            if (sel == null) { status.setText("Status: Select a farmer row first"); return; }

            if (!confirmDelete(sel.farmerID)) { status.setText("Status: Delete cancelled"); return; }

            FarmerDAO.delete(sel.farmerID);

            status.setText("Status: Farmer deleted");
            loadFarmers();

            if (selectedFarmerID[0] == sel.farmerID) {
                farmerTable.getSelectionModel().clearSelection();
                clearForm(tfID, tfName, tfRegion, tfContact);
                selectedFarmerID[0] = -1;
            }

        } catch (Exception ex) {
            status.setText("Status: Delete failed " + ex.getMessage());
        }
    }

    private boolean confirmDelete(int id) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Confirm Delete");
        a.setHeaderText("Delete Farmer");
        a.setContentText("Are you sure you want to delete Farmer ID = " + id + " ?");

        ButtonType del = new ButtonType("Delete");
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        a.getButtonTypes().setAll(del, cancel);

        return a.showAndWait().orElse(cancel) == del;
    }

    private void loadFarmers() {
        try {
            farmerTable.setItems(FXCollections.observableArrayList(FarmerDAO.getAll()));
            status.setText("Status: Farmers loaded");
        } catch (Exception ex) {
            status.setText("Status: Load failed " + ex.getMessage());
        }
    }

    private void setupFarmerTable() {
        TableColumn<FarmerRow, Number> cId = new TableColumn<>("ID");
        cId.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().farmerID));

        TableColumn<FarmerRow, String> cName = new TableColumn<>("Name");
        cName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().name));

        TableColumn<FarmerRow, String> cRegion = new TableColumn<>("Region");
        cRegion.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().region));

        TableColumn<FarmerRow, String> cContact = new TableColumn<>("Contact");
        cContact.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().contactInfo));

        farmerTable.getColumns().setAll(cId, cName, cRegion, cContact);
        farmerTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        farmerTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        farmerTable.setPrefHeight(420);
    }

    // ===== Helpers (farm theme) =====
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

    private TitledPane titled(String title, Node content) {
        TitledPane tp = new TitledPane(title, content);
        tp.setExpanded(true);
        tp.setCollapsible(false);
        tp.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: #C8E6C9;" +
                        "-fx-border-radius: 10;"
        );
        return tp;
    }

    private void clearForm(TextField tfID, TextField tfName, TextField tfRegion, TextField tfContact) {
        tfID.clear();
        tfName.clear();
        tfRegion.clear();
        tfContact.clear();
    }
}
