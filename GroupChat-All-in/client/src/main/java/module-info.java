module lk.hasitha.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens lk.hasitha.client to javafx.fxml;
    exports lk.hasitha.client;
}