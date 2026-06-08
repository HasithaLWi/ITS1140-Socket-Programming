package lk.hasitha.chatapp;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class ClientController {
    @FXML
    private Button btnSend;

    @FXML
    private TextArea txtArea;

    @FXML
    private TextField txtField;

    Socket socket;

    public void initialize() {
        new Thread(() -> {
            try {
                socket = new Socket("localhost", 12345);
                Platform.runLater(() -> {
                    txtArea.appendText("Connected to " + socket.getInetAddress().getHostName() + "\n");
                });
                DataInputStream dis = new DataInputStream(socket.getInputStream());

                while (true) {
                    String message = dis.readUTF();
                    Platform.runLater(() -> {
                        txtArea.appendText(message + "\n");
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void btnSendOnAction(ActionEvent event) {
        try {
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            dos.writeUTF(txtField.getText());

            txtArea.appendText("Me: " + txtField.getText() + "\n");
            dos.flush();

            txtField.setText("");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
