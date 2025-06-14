package lk.ijse.groupchat.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.*;
import java.net.Socket;

import static lk.ijse.groupchat.controller.ServerController.clientName;

public class ClientController {

    public Label lblClient;
    @FXML
    private ScrollPane scrollBasePane;

    @FXML
    private VBox scrollbodyVBox;

    @FXML
    private TextField txtField;

    Socket socket;
    DataOutputStream dos;
    DataInputStream dis;
    File file;
    File recieved;

    public void initialize() {
        new Thread(() -> {
            try{
                socket = new Socket("localhost",8000);
                dos = new DataOutputStream(socket.getOutputStream());
                dis = new DataInputStream(socket.getInputStream());
                this.lblClient.setText(clientName);

                while(true){
                    String type = dis.readUTF();

                    switch(type){
                        case "text" -> {
                            String text = dis.readUTF();
                            Platform.runLater(() -> {displayMessage(text,"left");});
                        }
                        case "image" ->{
                            String sender = dis.readUTF();
                            int length = dis.readInt();
                            byte[] bytes = new byte[length];
                            dis.readFully(bytes);
                            Image image = new Image(new ByteArrayInputStream(bytes));

                            Platform.runLater(()->displayImage(image,"left",sender));
                        }
                        case "file" ->{
                            String sender = dis.readUTF();
                            String fileName = dis.readUTF();
                            int length = dis.readInt();
                            byte[] bytes = new byte[length];
                            dis.readFully(bytes);

                            File downloadDir = new File("downloads");
                            if (!downloadDir.exists()) downloadDir.mkdirs();
                            File receivedFile = new File(downloadDir, fileName);

                            try(FileOutputStream fos = new FileOutputStream(receivedFile)){
                                fos.write(bytes);
                                Platform.runLater(() -> displayFile(sender,receivedFile, "left"));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    private void displayFile(String sender,File file, String alignment) {
        if (file == null || !file.exists())return;

        Label senderLabel = new Label(sender);
        Label fileLabel = new Label(file.getName());
        fileLabel.setStyle("-fx-text-fill: blue; -fx-underline: true; -fx-cursor: hand;");

        fileLabel.setOnMouseClicked(event -> {
            try{
                java.awt.Desktop.getDesktop().open(file);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        HBox hbox = new HBox(senderLabel,fileLabel);
        hbox.setPadding(new Insets(10));
        hbox.setAlignment(alignment.equals("left") ? Pos.CENTER_LEFT : Pos.CENTER_RIGHT);

        scrollbodyVBox.getChildren().add(hbox);
        scrollBasePane.layout();
        scrollBasePane.setVvalue(1.0);
    }

    private void displayImage(Image image, String alignment, String sender) {
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(200);
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        Label label = new Label(sender);
        label.setWrapText(true);

        VBox container = new VBox(imageView,label);
        container.setStyle("-fx-padding: 10;");

        if(alignment.equals("right")){
            container.setStyle("-fx-padding: 10; -fx-alignment: center-right;");
        }else{
            container.setStyle("-fx-padding: 10; -fx-alignment: center-left;");
        }
        scrollbodyVBox.getChildren().add(container);
        scrollBasePane.layout();
        scrollBasePane.setVvalue(1.0);
    }

    private void displayMessage(String text, String alignment) {
        Label messagelabel = new Label(text);
        messagelabel.setWrapText(true);

        HBox box = new HBox(messagelabel);
        box.setPadding(new Insets(5));

        if(alignment.equals("left")){
            box.setAlignment(Pos.CENTER_LEFT);
            messagelabel.setStyle("-fx-padding: 8; -fx-background-color: #dff9fb; -fx-background-radius: 10");
        }else{
            box.setAlignment(Pos.CENTER_RIGHT);
            messagelabel.setStyle("-fx-padding: 8; -fx-background-color: #c7ecee; -fx-background-radius: 10");
        }

        scrollbodyVBox.getChildren().add(box);
        scrollBasePane.layout();
        scrollBasePane.setVvalue(1.0);
    }

    @FXML
    void btnAttachFilesOnAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");

        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("All Files","*.*"));

        file = fileChooser.showOpenDialog(null);
    }

    @FXML
    void btnSendOnAction(ActionEvent event) {
        sendMessage();
    }

    private void sendMessage() {
        if (socket == null || socket.isClosed()) return;

        String message = txtField.getText();
        try{
            if (!message.trim().isEmpty()) {
                dos.writeUTF("text");
                dos.writeUTF(message);
                dos.flush();
                displayMessage(message,"right");
                txtField.clear();
                return;
            }

            if (file != null) {
                String fileName = file.getName();
                byte[] fileBytes = new FileInputStream(file).readAllBytes();

                boolean isImage = fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg");

                if (isImage) {
                    dos.writeUTF("image");
                    dos.writeInt(fileBytes.length);
                    dos.write(fileBytes);
                    dos.flush();

                    Image image = new Image(file.toURI().toString());
                    displayImage(image,"right","Me : ");
                }else{
                    dos.writeUTF("file");
                    dos.writeUTF(fileName);
                    dos.writeInt(fileBytes.length);
                    dos.write(fileBytes);
                    dos.flush();
                    displayFile("Me",file,"right");
                }
                file = null;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void btnEnter(KeyEvent keyEvent) {
        if(keyEvent.getCode()== KeyCode.ENTER){
            sendMessage();
        }
    }
}
