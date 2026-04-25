package com.farmtofork.phase3prototype;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class BranchesView {

    private final Label status;
    private final TableView<BranchRow> branchTable = new TableView<>();

    public BranchesView(Label status) {
        this.status = status;
    }

    public Node getContent() {
        return buildBranchesTab();
    }

    private Node buildBranchesTab() {
        setupBranchTable();

        // ===== Header =====
        Label title = new Label("Branches");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label sub = new Label("Manage branch locations & contacts");
        sub.setStyle("-fx-text-fill: #E9F7EF;");

        VBox titleBox = new VBox(2, title, sub);

        Button btnLoad = bigBtn("Load");
        stylePrimary(btnLoad);
        btnLoad.setOnAction(e -> loadBranches());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(12, titleBox, spacer, btnLoad);
        header.setPadding(new Insets(14));
        header.setStyle("-fx-background-color: #1B5E20; -fx-background-radius: 10;");

        // ===== Form (Add/Update) =====
        TextField tfID = new TextField(); tfID.setPromptText("Branch ID (auto)");
        TextField tfName = new TextField(); tfName.setPromptText("Name");
        TextField tfCity = new TextField(); tfCity.setPromptText("City");
        TextField tfAddress = new TextField(); tfAddress.setPromptText("Address");
        TextField tfPhone = new TextField(); tfPhone.setPromptText("Phone (e.g., 0591234567)");

        setWide(tfID); setWide(tfName); setWide(tfCity); setWide(tfAddress); setWide(tfPhone);

        tfID.setDisable(true);

        final int[] selectedBranchID = {-1};

        // auto-fill when selecting a row
        branchTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, sel) -> {
            if (sel == null) return;

            selectedBranchID[0] = sel.branchID;

            tfID.setText(String.valueOf(sel.branchID));
            tfName.setText(sel.name == null ? "" : sel.name);
            tfCity.setText(sel.city == null ? "" : sel.city);
            tfAddress.setText(sel.address == null ? "" : sel.address);
            tfPhone.setText(sel.phone == null ? "" : sel.phone);

            status.setText("Status: Selected branch loaded (ready to update)");
        });

        Button btnAdd = bigBtn("Add");
        stylePrimary(btnAdd);
        btnAdd.setOnAction(e -> {
            try {
                String name = tfName.getText().trim();
                if (name.isEmpty()) { status.setText("Status: Branch name required"); return; }

                String city = tfCity.getText().trim();
                String address = tfAddress.getText().trim();

                String phone = normalizePhone(tfPhone.getText());
                if (!phone.isEmpty() && !isValidPhone(phone)) {
                    status.setText("Status: Invalid phone. Use digits only (9-10 digits), e.g., 0591234567");
                    return;
                }

                // AUTO_INCREMENT insert
                int newID = BranchDAO.insertAndReturnID(name, city, address, phone);

                loadBranches();
                clearForm(tfID, tfName, tfCity, tfAddress, tfPhone);
                selectedBranchID[0] = -1;

                if (newID != -1) status.setText("Status: Branch inserted (ID = " + newID + ")");
                else status.setText("Status: Branch inserted");

            } catch (Exception ex) {
                status.setText("Status: Insert failed " + ex.getMessage());
            }
        });

        Button btnUpdate = bigBtn("Update");
        styleSecondary(btnUpdate);
        btnUpdate.setOnAction(e -> {
            try {
                if (selectedBranchID[0] == -1) {
                    status.setText("Status: Select a branch row first");
                    return;
                }

                String name = tfName.getText().trim();
                if (name.isEmpty()) { status.setText("Status: Branch name required"); return; }

                String city = tfCity.getText().trim();
                String address = tfAddress.getText().trim();

                String phone = normalizePhone(tfPhone.getText());
                if (!phone.isEmpty() && !isValidPhone(phone)) {
                    status.setText("Status: Invalid phone. Use digits only (9-10 digits), e.g., 0591234567");
                    return;
                }

                BranchDAO.update(selectedBranchID[0], name, city, address, phone);

                status.setText("Status: Branch updated");
                loadBranches();

            } catch (Exception ex) {
                status.setText("Status: Update failed " + ex.getMessage());
            }
        });

        Button btnClearForm = bigBtn("Clear");
        styleSoft(btnClearForm);
        btnClearForm.setOnAction(e -> {
            branchTable.getSelectionModel().clearSelection();
            clearForm(tfID, tfName, tfCity, tfAddress, tfPhone);
            selectedBranchID[0] = -1;
            status.setText("Status: Form cleared");
        });

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.setPadding(new Insets(12));

        form.add(new Label("Branch ID:"), 0, 0);
        form.add(tfID, 1, 0);

        form.add(new Label("Name:"), 0, 1);
        form.add(tfName, 1, 1);

        form.add(new Label("City:"), 0, 2);
        form.add(tfCity, 1, 2);

        form.add(new Label("Address:"), 0, 3);
        form.add(tfAddress, 1, 3);

        form.add(new Label("Phone:"), 0, 4);
        form.add(tfPhone, 1, 4);

        HBox actions = new HBox(10, btnAdd, btnUpdate, btnClearForm);
        form.add(actions, 1, 5);

        TitledPane formPane = titled("Add / Update Branch", form);

        // ===== Search by ID =====
        TextField tfSearch = new TextField();
        tfSearch.setPromptText("Search by Branch ID");
        setWide(tfSearch);

        Button btnSearch = bigBtn("Search");
        styleSecondary(btnSearch);
        btnSearch.setOnAction(e -> {
            try {
                if (tfSearch.getText().isBlank()) { status.setText("Status: Enter Branch ID"); return; }

                int id;
                try { id = Integer.parseInt(tfSearch.getText().trim()); }
                catch (NumberFormatException ex) { status.setText("Status: ID must be a number"); return; }

                BranchRow r = BranchDAO.getByID(id);
                if (r == null) {
                    branchTable.setItems(FXCollections.observableArrayList());
                    status.setText("Status: No branch found for ID = " + id);
                    return;
                }

                branchTable.setItems(FXCollections.observableArrayList(r));
                status.setText("Status: Search result loaded");

            } catch (Exception ex) {
                status.setText("Status: Search failed " + ex.getMessage());
            }
        });

        Button btnShowAll = bigBtn("Show All");
        styleSoft(btnShowAll);
        btnShowAll.setOnAction(e -> {
            tfSearch.clear();
            loadBranches();
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
        btnDelete.setOnAction(e -> deleteSelected(tfID, tfName, tfCity, tfAddress, tfPhone, selectedBranchID));

        VBox tableBox = new VBox(10, branchTable, btnDelete);
        tableBox.setPadding(new Insets(12));
        tableBox.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: #C8E6C9;" +
                        "-fx-border-radius: 10;"
        );

        loadBranches();

        VBox content = new VBox(12, header, formPane, searchBox, tableBox);
        content.setPadding(new Insets(12));
        content.setStyle("-fx-background-color: #F1F8E9;");

        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return sp;
    }

    private String normalizePhone(String s) {
        if (s == null) return "";
        // remove spaces and dashes
        return s.trim().replace(" ", "").replace("-", "");
    }

    private boolean isValidPhone(String phone) {
        // digits only
        for (int i = 0; i < phone.length(); i++) {
            if (!Character.isDigit(phone.charAt(i))) return false;
        }

        // length 9..10 (simple rule)
        if (phone.length() < 9 || phone.length() > 10) return false;

        // must start with 0 (common local format)
        if (!phone.startsWith("0")) return false;

        return true;
    }

    private void deleteSelected(TextField tfID, TextField tfName, TextField tfCity, TextField tfAddress, TextField tfPhone, int[] selectedBranchID) {
        try {
            BranchRow sel = branchTable.getSelectionModel().getSelectedItem();
            if (sel == null) { status.setText("Status: Select a branch row first"); return; }

            if (!confirmDelete(sel.branchID)) {
                status.setText("Status: Delete cancelled");
                return;
            }

            BranchDAO.delete(sel.branchID);

            status.setText("Status: Branch deleted");
            loadBranches();

            if (selectedBranchID[0] == sel.branchID) {
                branchTable.getSelectionModel().clearSelection();
                clearForm(tfID, tfName, tfCity, tfAddress, tfPhone);
                selectedBranchID[0] = -1;
            }

        } catch (Exception ex) {
            status.setText("Status: Delete failed " + ex.getMessage());
        }
    }

    private boolean confirmDelete(int id) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Confirm Delete");
        a.setHeaderText("Delete Branch");
        a.setContentText("Are you sure you want to delete Branch ID = " + id + " ?");

        ButtonType del = new ButtonType("Delete");
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        a.getButtonTypes().setAll(del, cancel);

        return a.showAndWait().orElse(cancel) == del;
    }

    private void loadBranches() {
        try {
            branchTable.setItems(FXCollections.observableArrayList(BranchDAO.getAll()));
            status.setText("Status: Branches loaded");
        } catch (Exception ex) {
            status.setText("Status: Load failed " + ex.getMessage());
        }
    }

    private void setupBranchTable() {
        TableColumn<BranchRow, Number> cId = new TableColumn<>("ID");
        cId.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().branchID));

        TableColumn<BranchRow, String> cName = new TableColumn<>("Name");
        cName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().name));

        TableColumn<BranchRow, String> cCity = new TableColumn<>("City");
        cCity.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().city));

        TableColumn<BranchRow, String> cAddress = new TableColumn<>("Address");
        cAddress.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().address));

        TableColumn<BranchRow, String> cPhone = new TableColumn<>("Phone");
        cPhone.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().phone));

        branchTable.getColumns().setAll(cId, cName, cCity, cAddress, cPhone);
        branchTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        branchTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        branchTable.setPrefHeight(420);
    }

    // =========================
    // Helpers (farm theme)
    // =========================
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

    private void clearForm(TextField tfID, TextField tfName, TextField tfCity, TextField tfAddress, TextField tfPhone) {
        tfID.clear();
        tfName.clear();
        tfCity.clear();
        tfAddress.clear();
        tfPhone.clear();
    }
}
