package com.farmtofork.phase3prototype;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class ClientsView {

    private final Label status;
    private final TableView<ClientRow> clientTable = new TableView<>();

    public ClientsView(Label status) {
        this.status = status;
    }

    public Node getContent() {
        return buildClientsTab();
    }

    private Node buildClientsTab() {
        setupClientTable();

        // ===== Header =====
        Label title = new Label("Clients");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label sub = new Label("Manage client profiles & contact information");
        sub.setStyle("-fx-text-fill: #E9F7EF;");

        VBox titleBox = new VBox(2, title, sub);

        Button btnLoad = bigBtn("Load");
        stylePrimary(btnLoad);
        btnLoad.setOnAction(e -> loadClients());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(12, titleBox, spacer, btnLoad);
        header.setPadding(new Insets(14));
        header.setStyle("-fx-background-color: #1B5E20; -fx-background-radius: 10;");

        // ===== Form (Add/Update) =====
        TextField tfID = new TextField(); tfID.setPromptText("Client ID (auto)");
        TextField tfName = new TextField(); tfName.setPromptText("Name");
        TextField tfType = new TextField(); tfType.setPromptText("Type (Restaurant / Shop / etc.)");
        TextField tfContact = new TextField(); tfContact.setPromptText("Contact (Phone/Email)");

        setWide(tfID); setWide(tfName); setWide(tfType); setWide(tfContact);

        // ID is always auto (never entered)
        tfID.setDisable(true);

        final int[] selectedClientID = {-1};

        // auto-fill when selecting row
        clientTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, sel) -> {
            if (sel == null) return;

            selectedClientID[0] = sel.clientID;

            tfID.setText(String.valueOf(sel.clientID));
            tfName.setText(sel.name == null ? "" : sel.name);
            tfType.setText(sel.type == null ? "" : sel.type);
            tfContact.setText(sel.contactInfo == null ? "" : sel.contactInfo);

            status.setText("Status: Selected client loaded (ready to update)");
        });

        Button btnAdd = bigBtn("Add");
        stylePrimary(btnAdd);
        btnAdd.setOnAction(e -> {
            try {
                String name = tfName.getText().trim();
                if (name.isEmpty()) { status.setText("Status: Client name required"); return; }

                String type = tfType.getText().trim();
                String contact = tfContact.getText().trim();

                int newID = ClientDAO.insertAndReturnID(name, type, contact);

                loadClients();
                clearForm(tfID, tfName, tfType, tfContact);
                clientTable.getSelectionModel().clearSelection();
                selectedClientID[0] = -1;

                if (newID != -1) status.setText("Status: Client inserted (ID = " + newID + ")");
                else status.setText("Status: Client inserted");

            } catch (Exception ex) {
                status.setText("Status: Insert failed " + ex.getMessage());
            }
        });

        Button btnUpdate = bigBtn("Update");
        styleSecondary(btnUpdate);
        btnUpdate.setOnAction(e -> {
            try {
                if (selectedClientID[0] == -1) {
                    status.setText("Status: Select a client row first");
                    return;
                }

                String name = tfName.getText().trim();
                if (name.isEmpty()) { status.setText("Status: Client name required"); return; }

                String type = tfType.getText().trim();
                String contact = tfContact.getText().trim();

                ClientDAO.update(selectedClientID[0], name, type, contact);

                status.setText("Status: Client updated");
                loadClients();

            } catch (Exception ex) {
                status.setText("Status: Update failed " + ex.getMessage());
            }
        });

        Button btnClearForm = bigBtn("Clear");
        styleSoft(btnClearForm);
        btnClearForm.setOnAction(e -> {
            clientTable.getSelectionModel().clearSelection();
            clearForm(tfID, tfName, tfType, tfContact);
            selectedClientID[0] = -1;
            status.setText("Status: Form cleared");
        });

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.setPadding(new Insets(12));

        form.add(new Label("Client ID:"), 0, 0);
        form.add(tfID, 1, 0);

        form.add(new Label("Name:"), 0, 1);
        form.add(tfName, 1, 1);

        form.add(new Label("Type:"), 0, 2);
        form.add(tfType, 1, 2);

        form.add(new Label("Contact:"), 0, 3);
        form.add(tfContact, 1, 3);

        HBox actions = new HBox(10, btnAdd, btnUpdate, btnClearForm);
        form.add(actions, 1, 4);

        TitledPane formPane = titled("Add / Update Client", form);

        // ===== Search by ID =====
        TextField tfSearch = new TextField();
        tfSearch.setPromptText("Search by Client ID");
        setWide(tfSearch);

        Button btnSearch = bigBtn("Search");
        styleSecondary(btnSearch);
        btnSearch.setOnAction(e -> {
            try {
                if (tfSearch.getText().isBlank()) { status.setText("Status: Enter Client ID"); return; }

                int id;
                try { id = Integer.parseInt(tfSearch.getText().trim()); }
                catch (NumberFormatException ex) { status.setText("Status: ID must be a number"); return; }

                ClientRow r = ClientDAO.getByID(id);
                if (r == null) {
                    clientTable.setItems(FXCollections.observableArrayList());
                    status.setText("Status: No client found for ID = " + id);
                    return;
                }

                clientTable.setItems(FXCollections.observableArrayList(r));
                status.setText("Status: Search result loaded");

            } catch (Exception ex) {
                status.setText("Status: Search failed " + ex.getMessage());
            }
        });

        Button btnShowAll = bigBtn("Show All");
        styleSoft(btnShowAll);
        btnShowAll.setOnAction(e -> {
            tfSearch.clear();
            loadClients();
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
        btnDelete.setOnAction(e -> deleteSelected(tfID, tfName, tfType, tfContact, selectedClientID));

        VBox tableBox = new VBox(10, clientTable, btnDelete);
        tableBox.setPadding(new Insets(12));
        tableBox.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: #C8E6C9;" +
                        "-fx-border-radius: 10;"
        );

        loadClients();

        VBox content = new VBox(12, header, formPane, searchBox, tableBox);
        content.setPadding(new Insets(12));
        content.setStyle("-fx-background-color: #F1F8E9;");

        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return sp;
    }

    private void deleteSelected(TextField tfID, TextField tfName, TextField tfType, TextField tfContact, int[] selectedClientID) {
        try {
            ClientRow sel = clientTable.getSelectionModel().getSelectedItem();
            if (sel == null) { status.setText("Status: Select a client row first"); return; }

            if (!confirmDelete(sel.clientID)) {
                status.setText("Status: Delete cancelled");
                return;
            }

            ClientDAO.delete(sel.clientID);

            status.setText("Status: Client deleted");
            loadClients();

            if (selectedClientID[0] == sel.clientID) {
                clientTable.getSelectionModel().clearSelection();
                clearForm(tfID, tfName, tfType, tfContact);
                selectedClientID[0] = -1;
            }

        } catch (Exception ex) {
            status.setText("Status: Delete failed " + ex.getMessage());
        }
    }

    private boolean confirmDelete(int id) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Confirm Delete");
        a.setHeaderText("Delete Client");
        a.setContentText("Are you sure you want to delete Client ID = " + id + " ?");

        ButtonType del = new ButtonType("Delete");
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        a.getButtonTypes().setAll(del, cancel);

        return a.showAndWait().orElse(cancel) == del;
    }

    private void loadClients() {
        try {
            clientTable.setItems(FXCollections.observableArrayList(ClientDAO.getAll()));
            status.setText("Status: Clients loaded");
        } catch (Exception ex) {
            status.setText("Status: Load failed " + ex.getMessage());
        }
    }

    private void setupClientTable() {
        TableColumn<ClientRow, Number> cId = new TableColumn<>("ID");
        cId.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().clientID));

        TableColumn<ClientRow, String> cName = new TableColumn<>("Name");
        cName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().name));

        TableColumn<ClientRow, String> cType = new TableColumn<>("Type");
        cType.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().type));

        TableColumn<ClientRow, String> cContact = new TableColumn<>("Contact");
        cContact.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().contactInfo));

        clientTable.getColumns().setAll(cId, cName, cType, cContact);
        clientTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        clientTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        clientTable.setPrefHeight(420);
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

    private void clearForm(TextField tfID, TextField tfName, TextField tfType, TextField tfContact) {
        tfID.clear();
        tfName.clear();
        tfType.clear();
        tfContact.clear();
    }
}
