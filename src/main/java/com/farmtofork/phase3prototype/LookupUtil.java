package com.farmtofork.phase3prototype;

import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

public class LookupUtil {

    public static void loadLookupLists(Label status,
                                       ComboBox<IdName> cbProduct,
                                       ComboBox<IdName> cbClient,
                                       ComboBox<IdName> cbFarmer,
                                       ComboBox<IdName> cbBranch) {
        try {
            if (cbProduct != null) cbProduct.setItems(FXCollections.observableArrayList(LookupDAO.getProducts()));
            if (cbClient != null) cbClient.setItems(FXCollections.observableArrayList(LookupDAO.getClients()));
            if (cbFarmer != null) cbFarmer.setItems(FXCollections.observableArrayList(LookupDAO.getFarmers()));
            if (cbBranch != null) cbBranch.setItems(FXCollections.observableArrayList(LookupDAO.getBranches()));
        } catch (Exception ex) {
            if (status != null) status.setText("Status: Failed to load lists " + ex.getMessage());
        }
    }
}
