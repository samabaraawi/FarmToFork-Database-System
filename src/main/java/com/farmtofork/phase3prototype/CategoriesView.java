package com.farmtofork.phase3prototype;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class CategoriesView {

    private final Label status;
    private final TableView<CategoryRow> categoryTable = new TableView<>();

    private TextField tfSearch;

    public CategoriesView(Label status) {
        this.status = status;
    }

    public Node getContent() {
        return buildCategoriesTab();
    }

    // =========================
    // UI
    // =========================
    private Node buildCategoriesTab() {
        setupCategoryTable();

        // -------- Header --------
        Label title = new Label("Categories");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label sub = new Label("Manage product categories (Add / Update / Delete / Search)");
        sub.setStyle("-fx-text-fill: #E9F7EF;");

        VBox titleBox = new VBox(2, title, sub);

        Button btnLoad = bigBtn("Load");
        stylePrimary(btnLoad);
        btnLoad.setOnAction(e -> loadCategories());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(12, titleBox, spacer, btnLoad);
        header.setPadding(new Insets(14));
        header.setStyle("-fx-background-color: #1B5E20; -fx-background-radius: 10;");

        // -------- Search Bar (NEW) --------
        tfSearch = new TextField();
        tfSearch.setPromptText("Search by Category ID");
        tfSearch.setPrefHeight(36);
        tfSearch.setPrefWidth(420);

        Button btnSearch = bigBtn("Search");
        stylePrimary(btnSearch);
        btnSearch.setOnAction(e -> doSearch());

        Button btnClearSearch = bigBtn("Clear");
        styleSoft(btnClearSearch);
        btnClearSearch.setOnAction(e -> {
            tfSearch.clear();
            loadCategories();
            status.setText("Status: Search cleared");
        });

        HBox searchBar = new HBox(12, new Label("Search:"), tfSearch, btnSearch, btnClearSearch);
        searchBar.setPadding(new Insets(10));
        searchBar.setStyle("-fx-alignment: center-left;");

        TitledPane searchPane = titled("Search Categories", searchBar);

        // -------- Form --------
        TextField tfId = new TextField();
        tfId.setPromptText("Category ID (auto)");
        tfId.setDisable(true);

        TextField tfName = new TextField();
        tfName.setPromptText("Category Name");

        TextField tfDesc = new TextField();
        tfDesc.setPromptText("Description");

        setWide(tfId);
        setWide(tfName);
        setWide(tfDesc);

        Button btnAdd = bigBtn("Add");
        stylePrimary(btnAdd);

        Button btnUpdate = bigBtn("Update");
        stylePrimary(btnUpdate);

        Button btnDelete = bigBtn("Delete");
        styleDanger(btnDelete);

        Button btnClear = bigBtn("Clear");
        styleSoft(btnClear);

        btnAdd.setOnAction(e -> {
            try {
                if (tfName.getText().isBlank()) {
                    status.setText("Status: Category name required");
                    return;
                }

                CategoryDAO.insert(tfName.getText().trim(), tfDesc.getText().trim());
                status.setText("Status: Category inserted");
                loadCategories();
                clearForm(tfId, tfName, tfDesc);

            } catch (Exception ex) {
                status.setText("Status: Insert failed " + ex.getMessage());
            }
        });

        btnUpdate.setOnAction(e -> {
            try {
                CategoryRow sel = categoryTable.getSelectionModel().getSelectedItem();
                if (sel == null) { status.setText("Status: Select a category row to update"); return; }

                if (tfName.getText().isBlank()) {
                    status.setText("Status: Category name required");
                    return;
                }

                CategoryDAO.update(sel.categoryID, tfName.getText().trim(), tfDesc.getText().trim());
                status.setText("Status: Category updated");
                loadCategories();
                clearForm(tfId, tfName, tfDesc);
                categoryTable.getSelectionModel().clearSelection();

            } catch (Exception ex) {
                status.setText("Status: Update failed " + ex.getMessage());
            }
        });

        btnDelete.setOnAction(e -> {
            try {
                CategoryRow sel = categoryTable.getSelectionModel().getSelectedItem();
                if (sel == null) { status.setText("Status: Select a category first"); return; }

                if (!confirmDelete(sel.categoryID)) {
                    status.setText("Status: Delete cancelled");
                    return;
                }

                CategoryDAO.delete(sel.categoryID);
                status.setText("Status: Category deleted");
                loadCategories();
                clearForm(tfId, tfName, tfDesc);
                categoryTable.getSelectionModel().clearSelection();

            } catch (Exception ex) {
                status.setText("Status: Delete failed " + ex.getMessage());
            }
        });

        btnClear.setOnAction(e -> {
            clearForm(tfId, tfName, tfDesc);
            categoryTable.getSelectionModel().clearSelection();
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

        form.add(labelChip("Name"), 0, 1);
        form.add(tfName, 1, 1);

        form.add(labelChip("Description"), 0, 2);
        form.add(tfDesc, 1, 2);

        HBox btnRow = new HBox(10, btnAdd, btnUpdate, btnDelete, btnClear);
        btnRow.setPadding(new Insets(0, 12, 12, 12));

        VBox formCard = new VBox(10, form, btnRow);
        formCard.setStyle(cardStyle());

        // -------- Auto-fill on row select --------
        categoryTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, sel) -> {
            if (sel == null) return;
            tfId.setText(String.valueOf(sel.categoryID));
            tfName.setText(sel.name == null ? "" : sel.name);
            tfDesc.setText(sel.description == null ? "" : sel.description);
            status.setText("Status: Selected category loaded (ready to update)");
        });

        // -------- Table Card --------
        VBox tableCard = new VBox(10, categoryTable);
        tableCard.setPadding(new Insets(12));
        tableCard.setStyle(cardStyle());

        loadCategories();

        VBox content = new VBox(12, header, searchPane, formCard, tableCard);
        content.setPadding(new Insets(12));
        content.setStyle("-fx-background-color: #F1F8E9;");

        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return sp;
    }

    // =========================
    // Search
    // =========================
    private void doSearch() {
        try {
            String text = (tfSearch == null) ? "" : tfSearch.getText().trim();

            if (text.isEmpty()) {
                loadCategories();
                status.setText("Status: Showing all categories");
                return;
            }

            int id;
            try {
                id = Integer.parseInt(text);
            } catch (NumberFormatException ex) {
                status.setText("Status: Category ID must be a number");
                return;
            }

            CategoryRow row = CategoryDAO.getByID(id);

            if (row == null) {
                categoryTable.getItems().clear();
                status.setText("Status: No category found with ID " + id);
            } else {
                categoryTable.setItems(FXCollections.observableArrayList(row));
                status.setText("Status: Category " + id + " found");
            }

        } catch (Exception ex) {
            status.setText("Status: Search failed " + ex.getMessage());
        }
    }

    // =========================
    // Actions
    // =========================
    private void loadCategories() {
        try {
            categoryTable.setItems(FXCollections.observableArrayList(CategoryDAO.getAll()));
            status.setText("Status: Categories loaded");
        } catch (Exception ex) {
            status.setText("Status: Load failed " + ex.getMessage());
        }
    }

    // =========================
    // Confirmation Alert
    // =========================
    private boolean confirmDelete(int id) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Confirm Delete");
        a.setHeaderText("Delete Category");
        a.setContentText("Are you sure you want to delete Category ID = " + id + " ?");

        ButtonType del = new ButtonType("Delete");
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        a.getButtonTypes().setAll(del, cancel);

        return a.showAndWait().orElse(cancel) == del;
    }

    // =========================
    // Table
    // =========================
    private void setupCategoryTable() {
        TableColumn<CategoryRow, Number> cId = new TableColumn<>("ID");
        cId.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().categoryID));

        TableColumn<CategoryRow, String> cName = new TableColumn<>("Name");
        cName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().name));

        TableColumn<CategoryRow, String> cDesc = new TableColumn<>("Description");
        cDesc.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().description));

        categoryTable.getColumns().setAll(cId, cName, cDesc);
        categoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        categoryTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        categoryTable.setPrefHeight(420);
        categoryTable.setStyle("-fx-background-radius: 10; -fx-border-color: #C8E6C9; -fx-border-radius: 10;");
    }

    private void clearForm(TextField tfId, TextField tfName, TextField tfDesc) {
        tfId.clear();
        tfName.clear();
        tfDesc.clear();
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
        b.setPrefWidth(150);
        b.setStyle("-fx-font-weight: bold; -fx-background-radius: 12;");
        return b;
    }

    private void stylePrimary(Button b) {
        b.setStyle("-fx-background-color: #2E7D32; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 12;");
    }

    private void styleDanger(Button b) {
        b.setStyle("-fx-background-color: #C62828; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 12;");
    }

    private void styleSoft(Button b) {
        b.setStyle("-fx-background-color: #E8F5E9; -fx-text-fill: #1B5E20; -fx-font-weight: bold; -fx-background-radius: 12; -fx-border-color: #C8E6C9; -fx-border-radius: 12;");
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
