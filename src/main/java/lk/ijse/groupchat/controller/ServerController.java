package lk.ijse.groupchat.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
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

    public static String clientName = "";

    private ServerSocket serverSocket;
    private List<ClientHandler> clients = new ArrayList<>();
    private File file;

    public void initialize() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(8000);

                while (true) {
                    Socket socket = serverSocket.accept();
                    ClientHandler clientHandler = new ClientHandler(socket);
                    clients.add(clientHandler);
                    new Thread(clientHandler).start();
                }

            } catch (Exception e) {
                e.printStackTrace();
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

    class ClientHandler implements Runnable {
        private Socket socket;
        private DataInputStream dis;
        private DataOutputStream dos;
        private String clientId;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                dis = new DataInputStream(socket.getInputStream());
                dos = new DataOutputStream(socket.getOutputStream());

                clientId = txtName.getText();
                txtName.clear();
                dos.writeUTF(clientId);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void sendText(String sender, String text) {
            try {
                dos.writeUTF("text");
                dos.writeUTF(sender + " : " + text);
                dos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void sendImage(String sender,byte[] imageBytes) throws IOException {
            dos.writeUTF("image");
            dos.writeUTF(sender);
            dos.writeInt(imageBytes.length);
            dos.write(imageBytes);
            dos.flush();
        }

        public void sendFile(String sender,String fileName, byte[] fileBytes) throws IOException {
            dos.writeUTF("file");
            dos.writeUTF(sender);
            dos.writeUTF(fileName);
            dos.writeInt(fileBytes.length);
            dos.write(fileBytes);
            dos.flush();
        }

        @Override
        public void run() {
            try {
                while (true) {
                    String type = dis.readUTF();

                    switch (type) {
                        case "text" -> {
                            String text = dis.readUTF();
                            broadcastMessage(text, this);
                        }
                        case "image" -> {
                            int length = dis.readInt();
                            byte[] bytes = new byte[length];
                            dis.readFully(bytes);
                            broadcastImage(bytes,this);
                        }
                        case "file" -> {
                            String fileName = dis.readUTF();
                            int length = dis.readInt();
                            byte[] bytes = new byte[length];
                            dis.readFully(bytes);

                            File received = new File("received_" + fileName);
                            try (FileOutputStream fos = new FileOutputStream(received)) {
                                fos.write(bytes);
                                broadcastFile(fileName, bytes, this);
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
                client.sendText(sender.clientId, message);
            }
        }
    }

    private void broadcastImage(byte[] imageBytes, ClientHandler sender) {
        try{
            for (ClientHandler client : clients) {
                if (client != sender) {
                    client.sendImage(sender.clientId,imageBytes);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void broadcastFile(String fileName, byte[] fileBytes, ClientHandler sender) {
        try {
            for (ClientHandler client : clients) {
                if (client != sender) {
                    client.sendFile(sender.clientId,fileName, fileBytes);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openClientWindow() throws IOException {
        clientName = txtName.getText();
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
