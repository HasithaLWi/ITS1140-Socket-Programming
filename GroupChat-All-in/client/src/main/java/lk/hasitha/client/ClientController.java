package lk.hasitha.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.Socket;
import java.util.Optional;

public class ClientController {

    @FXML
    private Button btnSend;

    @FXML
    private TextArea txtArea;

    @FXML
    private TextField txtField;

    @FXML
    private Label lblSelectedFile;


    DataInputStream dis;
    DataOutputStream dos;
    Socket remoteSocket = null;
    String message = "";
    File fileToSend = null;
    String clientName = "anonymous";


    public void initialize() {
        clientName = getClientNamme();

        new Thread(() -> {
            try {
                remoteSocket = new Socket("127.0.0.1", 12345);
                Platform.runLater(() -> {
                    txtArea.appendText("Connected to server...\n");
                });



                dis = new DataInputStream(remoteSocket.getInputStream());

                while (true) {
                    message = dis.readUTF();

                    final String receivedMsg = message;
                    Platform.runLater(() -> {
                        txtArea.appendText(receivedMsg + "\n");
                    });

                    if (message.startsWith("FILE:")) {
                        // --- 2. RECEIVE FILE ---
                        // Read metadata first
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

                            Platform.runLater(() -> txtArea.appendText("File: " + fileName + " (" + fileSize + " bytes)\n"));
                            Platform.runLater(() ->
                                    txtArea.appendText("File received and saved to: " + outputFile.getAbsolutePath() + "\n"));
                            System.out.println("File saved successfully as: " + outputFile.getName());

                        } catch (IOException e) {
                            System.err.println("Error saving file: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }


    private String getClientNamme() {
        while (true) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Enter your name");
            dialog.setHeaderText(null);
            dialog.setGraphic(null);
            dialog.setContentText("Please enter your name:");
            Optional<String> result = dialog.showAndWait();
            if (result.isPresent() && !result.get().trim().isEmpty()) {
                return result.get().trim();
            }else {
                return "AnonymousUser";
            }
        }
    }



    @FXML
    private void handleSendEmoji(ActionEvent event) {
        openWindowsEmojiPicker();
        txtField.requestFocus();
    }

    @FXML
    private void handleSendFile(ActionEvent event) {

        Stage stage = new Stage();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File");
        fileToSend = fileChooser.showOpenDialog(stage);

        if (fileToSend != null) {
            lblSelectedFile.setText("Selected: " + fileToSend.getName());
        }
    }

    private void openWindowsEmojiPicker() {
        try {
            Robot robot = new Robot();

            // Simulate pressing Windows Key + Period (.)
            robot.keyPress(KeyEvent.VK_WINDOWS);
            robot.keyPress(KeyEvent.VK_PERIOD);

            // Release the keys (always release what you press!)
            robot.keyRelease(KeyEvent.VK_PERIOD);
            robot.keyRelease(KeyEvent.VK_WINDOWS);

        } catch (AWTException ex) {
            System.err.println("Failed to open emoji picker: " + ex.getMessage());
        }
    }


    @FXML
    private void handleSendMassage(ActionEvent event) {
        try {
            dos = new DataOutputStream(remoteSocket.getOutputStream());
            String text = txtField.getText();

            // write message to server if not empty
            if (text != null && !text.trim().isEmpty()) {
                dos.writeUTF(clientName+": "+text);
                dos.flush();
            }

            // --- 2. SEND FILE ---
            if (fileToSend != null && fileToSend.exists()) {
                String fileName = fileToSend.getName();

                // Send metadata first: command, name, size
                dos.writeUTF("FILE:" + fileName);
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
                        lblSelectedFile.setText("no file selected");
                    });
                    dos.flush();
                    fileToSend = null; // Reset after sending
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        txtField.clear();
    }

    @FXML
    private void handleClear(ActionEvent event) {
        lblSelectedFile.setText("no file selected");
        fileToSend = null;
        txtField.clear();
    }

}