package lk.hasitha.filetransfer;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;

public class ClientController {

    @FXML
    private Button btnSend;

    @FXML
    private TextArea txtArea;

    @FXML
    private Label lblSelectedFile;


    DataInputStream dis;
    DataOutputStream dos;
    Socket remoteSocket = null;
    File fileToSend = null;


    public void initialize() {

        new Thread(() -> {
            try {
                remoteSocket = new Socket("127.0.0.1", 12345);
                Platform.runLater(() -> {
                    txtArea.appendText("Connected to server...\n");
                });



                dis = new DataInputStream(remoteSocket.getInputStream());

                while (true) {

                        String fileName = dis.readUTF();
                        long fileSize = dis.readLong();

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
                                    (bytesRead = dis.read(buffer, 0, (int) Math.min(buffer.length, fileSize - totalRead))) != -1) {
                                fos.write(buffer, 0, bytesRead);
                                totalRead += bytesRead;
                            }


                            Platform.runLater(() ->
                                    txtArea.appendText("File received and saved to: " + outputFile.getAbsolutePath() + "\n"));
                            System.out.println("File saved successfully as: " + outputFile.getName());

                        } catch (IOException e) {
                            System.err.println("Error saving file: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }


            } catch (IOException e) {
                throw new RuntimeException(e);
            }finally {
                try {
                    if (dis != null) dis.close();
                    if (dos != null) dos.close();
                    if (remoteSocket != null) remoteSocket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
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


    @FXML
    private void handleSendFile(ActionEvent event) {
        try {
            dos = new DataOutputStream(remoteSocket.getOutputStream());

            // --- 2. SEND FILE ---
            if (fileToSend != null && fileToSend.exists()) {
                String fileName = fileToSend.getName();

                // Send metadata first: command, name, size
                dos.writeUTF(fileName);
                dos.writeLong(fileToSend.length());

                // Read from local file and write to socket stream
                try (FileInputStream fis = new FileInputStream(fileToSend)) {
                    byte[] buffer = new byte[4096]; // 4KB buffer
                    int bytesRead;

                    while ((bytesRead = fis.read(buffer)) != -1) {
                        dos.write(buffer, 0, bytesRead);
                    }
                    System.out.println("File '" + fileName + "' sent successfully!");

                    Platform.runLater(() -> {
                        txtArea.appendText("File sent: " + fileName + "\n");
                    });
                    dos.flush();

                    handleClear(event); // Clear selection after sending
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @FXML
    private void handleClear(ActionEvent event) {
        lblSelectedFile.setText("file not selected");
        fileToSend = null;

    }

}