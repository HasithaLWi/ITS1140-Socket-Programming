package lk.hasitha.filetransfer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ChatApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ChatApplication.class.getResource("server_view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        stage.setTitle("Server");
        stage.setScene(scene);
        stage.show();


        FXMLLoader fxmlLoader2 = new FXMLLoader(ChatApplication.class.getResource("client_view.fxml"));
        Stage stage2 = new Stage();
        Scene scene2 = new Scene(fxmlLoader2.load(), 600, 400);
        stage2.setTitle("Client");
        stage2.setScene(scene2);
        stage2.show();
    }
}
