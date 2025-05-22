package lk.ijse.groupchat.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerController {

    public TextField txtName;

    public static String userName = "";
    private ServerSocket serverSocket;
    private List<ClientHandler> clients = new ArrayList<>();

    public void initialize() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(5000);

                while (true) {
                    Socket socket = serverSocket.accept();
                    ClientHandler clientHandler = new ClientHandler(socket);
                    clients.add(clientHandler);
                    new Thread(clientHandler).start();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    public void txtEnterOnKeyReleased(KeyEvent keyEvent) throws IOException {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            openClientWindow();
        }
    }

    public void btnEnterClickOnAction(ActionEvent event) throws IOException {
        openClientWindow();
    }

    class ClientHandler implements Runnable{
        private Socket socket;
        private DataInputStream dataInputStream;
        private DataOutputStream dataOutputStream;
        private String clientId;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                dataInputStream = new DataInputStream(socket.getInputStream());
                dataOutputStream = new DataOutputStream(socket.getOutputStream());

                clientId = txtName.getText();
                dataOutputStream.writeUTF(clientId);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void sendText(String text, String sender) {
            try {
                dataOutputStream.writeUTF("text");
                dataOutputStream.writeUTF(sender + ": " + text);
                dataOutputStream.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void sendImage(byte[] image, String sender) throws IOException {
            dataOutputStream.writeUTF("image");
            dataOutputStream.writeUTF(sender);
            dataOutputStream.writeInt(image.length);
            dataOutputStream.write(image);
            dataOutputStream.flush();
        }

        public void sendFile(byte[] file, String sender, String fileName) throws IOException {
            dataOutputStream.writeUTF("file");
            dataOutputStream.writeUTF(sender);
            dataOutputStream.writeUTF(fileName);
            dataOutputStream.writeInt(file.length);
            dataOutputStream.write(file);
            dataOutputStream.flush();
        }

        @Override
        public void run() {
            try{
                while (true) {
                    String type = dataInputStream.readUTF();

                    switch (type){
                        case "text" ->{
                            String text = dataInputStream.readUTF();
                            String msg = clientId + " : " + text;
                            broadcastMessage(text, this);
                        }

                        case "image" ->{
                            int length = dataInputStream.readInt();
                            byte[] bytes = new byte[length];
                            dataInputStream.readFully(bytes);
                            Image image = new Image(new ByteArrayInputStream(bytes));
                            broadcastImage(bytes, this);
                        }

                        case "file" ->{
                            String fileName = dataInputStream.readUTF();
                            int length = dataInputStream.readInt();
                            byte[] bytes = new byte[length];
                            dataInputStream.readFully(bytes);
                            File recived = new File("recived_" + fileName);
                            try (FileOutputStream fileOutputStream = new FileOutputStream(recived)) {
                                fileOutputStream.write(bytes);
                                broadcastFile(bytes, this, fileName);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                clients.remove(this);
            }
        }
    }

    private void broadcastMessage(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendText(message, sender.clientId);
            }
        }
    }

    private void broadcastImage(byte[] image, ClientHandler sender) throws IOException {
        try {
            for (ClientHandler client : clients) {
                if (client != sender) {
                    client.sendImage(image, sender.clientId);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void broadcastFile(byte[] file, ClientHandler sender, String fileName) throws IOException {
        try {
            for (ClientHandler client : clients) {
                if (client != sender) {
                    client.sendFile(file, sender.clientId, fileName);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void openClientWindow() throws IOException {
        userName = txtName.getText();

        if (txtName.getText().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Please Enter Your Name").show();
            return;
        }

        Parent root = FXMLLoader.load(getClass().getResource("/view/Client.fxml"));
        Scene scene = new Scene(root);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Login to Chat");
        stage.show();
    }
}
