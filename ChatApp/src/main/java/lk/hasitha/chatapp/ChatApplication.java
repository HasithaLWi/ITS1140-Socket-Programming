package lk.hasitha.chatapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ChatApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ChatApplication.class.getResource("server-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        stage.setTitle("Chat Server");
        stage.setScene(scene);
        stage.show();

        FXMLLoader fxmlLoader2 = new FXMLLoader(ChatApplication.class.getResource("client-view.fxml"));
        Stage stage2 = new Stage();
        Scene scene2 = new Scene(fxmlLoader2.load(), 600, 400);
        stage2.setTitle("Chat Client");
        stage2.setScene(scene2);
        stage2.show();

        FXMLLoader fxmlLoader3 = new FXMLLoader(ChatApplication.class.getResource("client2-view.fxml"));
        Stage stage3 = new Stage();
        Scene scene3 = new Scene(fxmlLoader3.load(), 600, 400);
        stage3.setTitle("Chat Client 2");
        stage3.setScene(scene3);
        stage3.show();
    }
}
