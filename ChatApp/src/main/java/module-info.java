module lk.hasitha.chatapp {
    requires javafx.controls;
    requires javafx.fxml;


    opens lk.hasitha.chatapp to javafx.fxml;
    exports lk.hasitha.chatapp;
}