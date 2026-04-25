module com.farmtofork.phase3prototype {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.farmtofork.phase3prototype to javafx.fxml;
    exports com.farmtofork.phase3prototype;
}