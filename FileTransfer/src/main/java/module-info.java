module lk.hasitha.filetransfer {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens lk.hasitha.filetransfer to javafx.fxml;
    exports lk.hasitha.filetransfer;
}