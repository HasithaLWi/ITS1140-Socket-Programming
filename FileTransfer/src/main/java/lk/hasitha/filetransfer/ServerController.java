package lk.hasitha.filetransfer;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
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
    private Label lblSelectedFile;

    ServerSocket serverSocket = null;
    Socket socket = null;
    File fileToSend = null;



    public void initialize() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(12345);
                Platform.runLater(() -> txtArea.appendText("Server started...\n"));

                socket = serverSocket.accept();

                Platform.runLater(() -> txtArea.appendText("client connected\n"));

                DataInputStream in = new DataInputStream(socket.getInputStream());

                String message;

                while (true) {

                    message = in.readUTF();

                    final String msg = message;
                    Platform.runLater(() -> txtArea.appendText(msg + "\n"));

                    receiveFile(in);

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleSelectFile(ActionEvent event) {

        Stage stage = new Stage();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File");
        fileToSend = fileChooser.showOpenDialog(stage);

        if (fileToSend != null) {
            lblSelectedFile.setText("Selected: " + fileToSend.getName());
        }
    }


    private void receiveFile(DataInputStream in) throws IOException {

        String fileName = in.readUTF();
        long fileSize = in.readLong();

        System.out.println("Receiving file: " + fileName + " (" + fileSize + " bytes)");

        // Prepare to write the file to disk
        String saveFilePath = "downloads/" + fileName;
        File outputFile = new File(saveFilePath);
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }


        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[4096]; // 4KB buffer
            int bytesRead;
            long totalRead = 0;

            // Read exactly the amount of bytes specified by fileSize
            while (totalRead < fileSize &&
                    (bytesRead = in.read(buffer, 0, (int) Math.min(buffer.length, fileSize - totalRead))) != -1) {
                fos.write(buffer, 0, bytesRead);
                totalRead += bytesRead;
            }

            Platform.runLater(() -> txtArea.appendText("File: " + fileName + " (" + fileSize + " bytes)\n"));
            Platform.runLater(() ->
                    txtArea.appendText("File received and saved to: " + outputFile.getAbsolutePath() + "\n"));
            System.out.println("File saved successfully as: " + outputFile.getName());

        } catch (IOException e) {
            System.err.println("Error saving file: " + e.getMessage());
        }

    }



    @FXML
    private void handleSendFile(ActionEvent event) {

        if (fileToSend == null) {
            Platform.runLater(() -> txtArea.appendText("No file selected to send.\n"));
            return;
        }


        try {
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

            if (!fileToSend.exists()) {
                System.out.println("File does not exist: " + fileToSend.getName());
            }

            // Send metadata first
            dos.writeUTF(fileToSend.getName());
            dos.writeLong(fileToSend.length());

            // Read from local file and write to socket stream
            try (FileInputStream fis = new FileInputStream(fileToSend)) {
                byte[] buffer = new byte[4096]; // 4KB buffer
                int bytesRead;

                while ((bytesRead = fis.read(buffer)) != -1) {
                    dos.write(buffer, 0, bytesRead);
                }
                System.out.println("File '" + fileToSend.getName() + "' sent successfully!");

                Platform.runLater(() -> txtArea.appendText("File sent successfully!\n"));
            }

            dos.flush(); // Ensure all data is pushed through the socket

            handleClear(event); // Clear selection after sending

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @FXML
    private void handleClear(ActionEvent event) {
        lblSelectedFile.setText("file not selected");
        fileToSend = null;
    }

}