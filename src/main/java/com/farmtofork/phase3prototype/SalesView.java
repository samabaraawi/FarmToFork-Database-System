package com.farmtofork.phase3prototype;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.sql.Date;

public class SalesView {

    private final Label status;
    private final TableView<SaleRow> saleTable = new TableView<>();

    // Form controls
    private DatePicker dpSaleDate;
    private TextField tfQty;
    private TextField tfUnit;
    private ComboBox<IdName> cbClient;
    private ComboBox<IdName> cbProduct;
    private ComboBox<IdName> cbBranch;

    // Search
    private TextField tfSearch;

    private final int[] selectedSaleID = {-1};

    public SalesView(Label status) {
        this.status = status;
    }

    public Node getContent() {
        return buildSalesTab();
    }

    public void loadSalesPublic() {
        loadSales();
    }

    private Node buildSalesTab() {
        setupSaleTable();

        // =========================
        // Header / Top Bar (Farm theme)
        // =========================
        Label title = new Label("Sales");
        title.setFont(Font.font(24));
        title.setTextFill(Color.WHITE);

        Label subtitle = new Label("Insert / Update / Delete sales and automatically adjust product stock.");
        subtitle.setFont(Font.font(12));
        subtitle.setTextFill(Color.web("#E9F7EF"));

        VBox titleBox = new VBox(2, title, subtitle);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        Button btnLoad = bigBtn("Load");
        btnLoad.setOnAction(e -> loadSales());

        Button btnRefreshLists = bigBtn("Refresh Lists");
        btnRefreshLists.setOnAction(e -> {
            LookupUtil.loadLookupLists(status, cbProduct, cbClient, null, cbBranch);
            status.setText("Status: Lists refreshed");
        });

        Button btnClearForm = bigBtn("Clear Form");
        btnClearForm.setOnAction(e -> clearForm());

        stylePrimary(btnLoad);        // green
        styleSecondary(btnRefreshLists);
        styleDanger(btnClearForm);    // red

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(12, titleBox, spacer, btnLoad, btnRefreshLists, btnClearForm);
        header.setPadding(new Insets(14));
        header.setAlignment(Pos.CENTER_LEFT);

        header.setBackground(new Background(new BackgroundFill(
                Color.web("#1B5E20"), new CornerRadii(10), Insets.EMPTY   // dark green
        )));
        header.setBorder(new Border(new BorderStroke(
                Color.web("#124017"),
                BorderStrokeStyle.SOLID, new CornerRadii(10), new BorderWidths(1)
        )));

        // =========================
        // Search Section
        // =========================
        tfSearch = new TextField();
        tfSearch.setPromptText("Search by Sale ID");
        tfSearch.setPrefWidth(520);
        tfSearch.setPrefHeight(36);

        Button btnSearch = bigBtn("Search");
        btnSearch.setOnAction(e -> doSearch());

        Button btnClearSearch = bigBtn("Clear");
        btnClearSearch.setOnAction(e -> {
            tfSearch.clear();
            loadSales();
            status.setText("Status: Search cleared");
        });

        stylePrimary(btnSearch);
        styleSecondary(btnClearSearch);

        HBox searchBar = new HBox(12, new Label("Search:"), tfSearch, btnSearch, btnClearSearch);
        searchBar.setPadding(new Insets(10));
        searchBar.setAlignment(Pos.CENTER_LEFT);

        TitledPane searchPane = titled("Search Sales", searchBar, "#F1F8E9"); // light green

        // =========================
        // Form Section
        // =========================
        dpSaleDate = new DatePicker();
        dpSaleDate.setPromptText("Sale Date");

        tfQty = new TextField(); tfQty.setPromptText("Quantity");
        tfUnit = new TextField(); tfUnit.setPromptText("Unit Price");

        cbClient = new ComboBox<>(); cbClient.setPromptText("Client");
        cbProduct = new ComboBox<>(); cbProduct.setPromptText("Product");
        cbBranch = new ComboBox<>(); cbBranch.setPromptText("Branch");

        setWide(dpSaleDate);
        setWide(tfQty);
        setWide(tfUnit);
        setWide(cbClient);
        setWide(cbProduct);
        setWide(cbBranch);

        LookupUtil.loadLookupLists(status, cbProduct, cbClient, null, cbBranch);

        Button btnAdd = bigBtn("Add");
        btnAdd.setOnAction(e -> insertSale());

        Button btnUpdate = bigBtn("Update");
        btnUpdate.setOnAction(e -> updateSale());

        Button btnDelete = bigBtn("Delete");
        btnDelete.setOnAction(e -> deleteSale());

        stylePrimary(btnAdd);
        styleSecondary(btnUpdate);
        styleDanger(btnDelete);

        HBox actions = new HBox(12, btnAdd, btnUpdate, btnDelete);
        actions.setAlignment(Pos.CENTER_LEFT);

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.setPadding(new Insets(12));

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(120);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(col1, col2);

        form.add(new Label("Sale Date:"), 0, 0);   form.add(dpSaleDate, 1, 0);
        form.add(new Label("Quantity:"), 0, 1);    form.add(tfQty, 1, 1);
        form.add(new Label("Unit Price:"), 0, 2);  form.add(tfUnit, 1, 2);
        form.add(new Separator(), 0, 3, 2, 1);
        form.add(new Label("Client:"), 0, 4);      form.add(cbClient, 1, 4);
        form.add(new Label("Product:"), 0, 5);     form.add(cbProduct, 1, 5);
        form.add(new Label("Branch:"), 0, 6);      form.add(cbBranch, 1, 6);
        form.add(new Separator(), 0, 7, 2, 1);
        form.add(actions, 1, 8);

        TitledPane formPane = titled("Sales Form (Auto-fill on row selection)", form, "#FFFFFF");

        // =========================
        // Table Section
        // =========================
        Label tableTitle = new Label("Sales Table");
        tableTitle.setFont(Font.font(16));
        tableTitle.setTextFill(Color.web("#1B5E20"));

        saleTable.setPrefHeight(450);  // fixed, scroll page handles the rest

        VBox tableBox = new VBox(8, tableTitle, saleTable);
        tableBox.setPadding(new Insets(12));
        tableBox.setBackground(new Background(new BackgroundFill(
                Color.WHITE, new CornerRadii(10), Insets.EMPTY
        )));
        tableBox.setBorder(new Border(new BorderStroke(
                Color.web("#C8E6C9"),  // green border
                BorderStrokeStyle.SOLID, new CornerRadii(10), new BorderWidths(1)
        )));

        // Auto-fill on select
        saleTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, sel) -> {
            if (sel == null) return;
            fillFormFromRow(sel);
        });

        // Page content
        VBox content = new VBox(12, header, searchPane, formPane, tableBox);
        content.setPadding(new Insets(12));

        //  Scroll the whole Sales page
        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setPannable(true);

        loadSales();
        return sp;
    }


    // ================= Style Helpers =================

    private Button bigBtn(String text) {
        Button b = new Button(text);
        b.setPrefWidth(160);
        b.setPrefHeight(40);
        b.setFont(Font.font(13));
        return b;
    }

    private void stylePrimary(Button b) {
        b.setStyle("-fx-background-color: #2E7D32; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;");
    }

    private void styleSecondary(Button b) {
        b.setStyle("-fx-background-color: #E8F5E9; -fx-text-fill: #1B5E20; -fx-font-weight: bold; -fx-background-radius: 10;");
    }

    private void styleDanger(Button b) {
        b.setStyle("-fx-background-color: #C62828; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;");
    }

    private TitledPane titled(String title, Node content, String bg) {
        TitledPane tp = new TitledPane(title, content);
        tp.setCollapsible(false);
        tp.setExpanded(true);
        tp.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 10;");
        return tp;
    }


    private void setWide(Control c) {
        c.setPrefHeight(36);
        if (c instanceof TextField tf) tf.setPrefWidth(520);
        if (c instanceof ComboBox<?> cb) cb.setPrefWidth(520);
        if (c instanceof DatePicker dp) dp.setPrefWidth(520);
    }

    // ================= Actions =================

    private void doSearch() {
        try {
            String text = tfSearch.getText().trim();

            if (text.isEmpty()) {
                loadSales();
                status.setText("Status: Showing all sales");
                return;
            }

            int saleID;
            try {
                saleID = Integer.parseInt(text);
            } catch (NumberFormatException ex) {
                status.setText("Status: Sale ID must be a number");
                return;
            }

            SaleRow result = SaleDAO.getSaleByID(saleID);

            if (result == null) {
                saleTable.getItems().clear();
                status.setText("Status: No sale found with ID " + saleID);
            } else {
                saleTable.setItems(FXCollections.observableArrayList(result));
                status.setText("Status: Sale " + saleID + " found");
            }

        } catch (Exception ex) {
            status.setText("Status: Search failed " + ex.getMessage());
        }
    }


    private void insertSale() {
        try {
            if (!validateForm()) return;

            int qty = Integer.parseInt(tfQty.getText().trim());
            double unit = Double.parseDouble(tfUnit.getText().trim());

            SaleDAO.insertSale(
                    Date.valueOf(dpSaleDate.getValue()),
                    qty, unit,
                    cbClient.getValue().id,
                    cbProduct.getValue().id,
                    cbBranch.getValue().id
            );

            status.setText("Status: Sale inserted (Stock updated)");
            loadSales();
            AppContext.refreshProducts.run();
            clearForm();

        } catch (Exception ex) {
            status.setText("Status: Insert failed " + ex.getMessage());
        }
    }

    private void updateSale() {
        try {
            if (selectedSaleID[0] == -1) {
                status.setText("Status: Select a row to update");
                return;
            }
            if (!validateForm()) return;

            int qty = Integer.parseInt(tfQty.getText().trim());
            double unit = Double.parseDouble(tfUnit.getText().trim());

            SaleDAO.updateSale(
                    selectedSaleID[0],
                    Date.valueOf(dpSaleDate.getValue()),
                    qty, unit,
                    cbClient.getValue().id,
                    cbProduct.getValue().id,
                    cbBranch.getValue().id
            );

            status.setText("Status: Sale updated (Stock adjusted)");
            loadSales();
            AppContext.refreshProducts.run();
            clearForm();

        } catch (Exception ex) {
            status.setText("Status: Update failed " + ex.getMessage());
        }
    }

    private void deleteSale() {
        try {
            SaleRow sel = saleTable.getSelectionModel().getSelectedItem();
            if (sel == null) {
                status.setText("Status: Select a row to delete");
                return;
            }

            Alert a = new Alert(Alert.AlertType.CONFIRMATION);
            a.setTitle("Confirm Delete");
            a.setHeaderText("Delete Sale #" + sel.saleID + " ?");
            a.setContentText("This will restore product stock automatically.");
            var res = a.showAndWait();
            if (res.isEmpty() || res.get() != ButtonType.OK) {
                status.setText("Status: Delete cancelled");
                return;
            }

            SaleDAO.deleteSale(sel.saleID);

            status.setText("Status: Sale deleted (Stock restored)");
            loadSales();
            AppContext.refreshProducts.run();
            clearForm();

        } catch (Exception ex) {
            status.setText("Status: Delete failed " + ex.getMessage());
        }
    }

    private boolean validateForm() {
        if (dpSaleDate.getValue() == null) { status.setText("Status: Sale date required"); return false; }
        if (tfQty.getText().isBlank()) { status.setText("Status: Quantity required"); return false; }
        if (tfUnit.getText().isBlank()) { status.setText("Status: Unit price required"); return false; }
        if (cbClient.getValue() == null) { status.setText("Status: Select client"); return false; }
        if (cbProduct.getValue() == null) { status.setText("Status: Select product"); return false; }
        if (cbBranch.getValue() == null) { status.setText("Status: Select branch"); return false; }

        int qty;
        double unit;

        try { qty = Integer.parseInt(tfQty.getText().trim()); }
        catch (NumberFormatException ex) { status.setText("Status: Quantity must be a number"); return false; }

        try { unit = Double.parseDouble(tfUnit.getText().trim()); }
        catch (NumberFormatException ex) { status.setText("Status: Unit price must be a number"); return false; }

        if (qty <= 0) { status.setText("Status: Quantity must be > 0"); return false; }
        if (unit <= 0) { status.setText("Status: Unit price must be > 0"); return false; }

        return true;
    }

    private void fillFormFromRow(SaleRow sel) {
        selectedSaleID[0] = sel.saleID;

        try { dpSaleDate.setValue(java.time.LocalDate.parse(sel.saleDate)); }
        catch (Exception ex) { dpSaleDate.setValue(null); }

        tfQty.setText(String.valueOf(sel.quantity));
        tfUnit.setText(String.valueOf(sel.unitPrice));

        cbClient.setValue(null);
        cbProduct.setValue(null);
        cbBranch.setValue(null);

        for (IdName x : cbClient.getItems()) if (x.id == sel.clientID) { cbClient.setValue(x); break; }
        for (IdName x : cbProduct.getItems()) if (x.id == sel.productID) { cbProduct.setValue(x); break; }
        for (IdName x : cbBranch.getItems()) if (x.id == sel.branchID) { cbBranch.setValue(x); break; }

        status.setText("Status: Loaded Sale #" + sel.saleID + " into form");
    }

    private void clearForm() {
        selectedSaleID[0] = -1;
        dpSaleDate.setValue(null);
        tfQty.clear();
        tfUnit.clear();
        cbClient.setValue(null);
        cbProduct.setValue(null);
        cbBranch.setValue(null);
        saleTable.getSelectionModel().clearSelection();
    }

    // ================= Table =================

    private void loadSales() {
        try {
            saleTable.setItems(FXCollections.observableArrayList(SaleDAO.getAllSales()));
            status.setText("Status: Sales loaded");
        } catch (Exception ex) {
            status.setText("Status: Load failed " + ex.getMessage());
        }
    }

    private void setupSaleTable() {
        TableColumn<SaleRow, Number> cId = new TableColumn<>("SaleID");
        cId.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().saleID));

        TableColumn<SaleRow, String> cDate = new TableColumn<>("Date");
        cDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().saleDate));

        TableColumn<SaleRow, Number> cQty = new TableColumn<>("Qty");
        cQty.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().quantity));

        TableColumn<SaleRow, Number> cUnit = new TableColumn<>("UnitPrice");
        cUnit.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().unitPrice));

        TableColumn<SaleRow, Number> cTot = new TableColumn<>("TotalRevenue");
        cTot.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().totalRevenue));

        TableColumn<SaleRow, Number> cClient = new TableColumn<>("ClientID");
        cClient.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().clientID));

        TableColumn<SaleRow, Number> cProd = new TableColumn<>("ProductID");
        cProd.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().productID));

        TableColumn<SaleRow, Number> cBranch = new TableColumn<>("BranchID");
        cBranch.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().branchID));

        saleTable.getColumns().setAll(cId, cDate, cQty, cUnit, cTot, cClient, cProd, cBranch);
        saleTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        saleTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }
}
