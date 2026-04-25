package com.farmtofork.phase3prototype;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class UIUtil {

    public static GridPane gridForm(int hgap, int vgap) {
        GridPane g = new GridPane();
        g.setHgap(hgap);
        g.setVgap(vgap);
        g.setPadding(new Insets(10));
        return g;
    }

    public static TitledPane titled(String title, Pane content) {
        TitledPane tp = new TitledPane(title, content);
        tp.setExpanded(true);
        return tp;
    }

    public static HBox headerBar(String title, Button... buttons) {
        Label lbl = new Label(title);
        lbl.setMinWidth(180);

        HBox btnBox = new HBox(8);
        btnBox.getChildren().addAll(buttons);
        btnBox.setAlignment(Pos.CENTER_LEFT);

        HBox h = new HBox(12, lbl, new Separator(), btnBox);
        h.setPadding(new Insets(10));
        return h;
    }

    public static VBox withSearch(String placeholder, TextField tfSearch, TableView<?> table) {
        tfSearch.setPromptText(placeholder);
        VBox v = new VBox(8, tfSearch, table);
        v.setPadding(new Insets(10));
        return v;
    }

    public static boolean confirmDelete(String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Confirm");
        a.setHeaderText("Are you sure?");
        a.setContentText(msg);
        return a.showAndWait().filter(b -> b == ButtonType.OK).isPresent();
    }
}
