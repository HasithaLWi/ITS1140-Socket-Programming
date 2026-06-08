package lk.hasitha.chatapp;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerController {

    @FXML
    private Button btnSend;

    @FXML
    private TextArea txtArea;

    @FXML
    private TextField txtField;

    ServerSocket serverSocket = null;
    List<Socket> clients = new CopyOnWriteArrayList<>();

    @FXML
    public void initialize() {
       new Thread(() -> {
           try{
               serverSocket = new ServerSocket(12345);
               Platform.runLater(() -> txtArea.appendText("Server started...\n"));
               while (true) {
                   Socket socket = serverSocket.accept();
                   clients.add(socket);

                     Platform.runLater(() -> txtArea.appendText("New client connected\n"));

                   // handle each client in separate thread
                   new Thread(() -> handleClient(socket)).start();
               }
           } catch (Exception e){
               e.printStackTrace();
           }
       }).start();

    }


    private void handleClient(Socket socket) {
        try{

            DataInputStream inputStream = new DataInputStream(socket.getInputStream());

            while(true){
                String massage = inputStream.readUTF();
                Platform.runLater(() -> txtArea.appendText("Received: " + massage + "\n"));
                String msg = "Client: " + massage;
                broadcast(msg, socket);
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void broadcast(String message,Socket socket){
        for (Socket client : clients) {

            if(client.equals(socket)){
                continue;
            }

            try {
                DataOutputStream outputStream = new DataOutputStream(client.getOutputStream());
                outputStream.writeUTF(message);
                outputStream.flush();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void btnSendOnAction(ActionEvent actionEvent) {
        String message = "Server: "+txtField.getText();
        if (message.isEmpty()) {
            return;
        }

            broadcast(message, null);

        Platform.runLater(() -> {
            txtArea.appendText("Sent: " + message + "\n");

        });

        txtField.setText("");
    }

}
