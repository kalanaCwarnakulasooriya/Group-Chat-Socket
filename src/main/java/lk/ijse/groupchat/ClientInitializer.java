package lk.ijse.groupchat;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientInitializer extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/view/Client.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Chat App");
        stage.show();
    }
}
