module lk.ijse.groupchat {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens lk.ijse.groupchat to javafx.fxml;
    opens lk.ijse.groupchat.controller to javafx.fxml;
    exports lk.ijse.groupchat;
}