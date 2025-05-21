package lk.ijse.groupchat.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {
    public static String userName = "";

    public AnchorPane rootNode;

    @FXML
    private TextField txtName;

    @FXML
    void btnEnterClickOnAction(ActionEvent event) throws IOException {
        userName = txtName.getText();
        if (txtName.getText().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Please Enter Your Name").show();
            return;
        }

        FXMLLoader fxmlLoader = new FXMLLoader(LoginController.class.getResource("../view/Client.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = (Stage)rootNode.getScene().getWindow();
        stage.setTitle("Login to Chat");
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    void txtNameOnAction(ActionEvent event) throws IOException {
        btnEnterClickOnAction(event);
    }

}
