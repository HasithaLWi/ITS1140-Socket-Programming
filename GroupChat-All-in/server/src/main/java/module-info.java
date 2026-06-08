module lk.hasitha.server {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens lk.hasitha.server to javafx.fxml;
    exports lk.hasitha.server;
}