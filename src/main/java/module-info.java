module lk.ijse.groupchat {
    requires javafx.controls;
    requires javafx.fxml;


    opens lk.ijse.groupchat to javafx.fxml;
    exports lk.ijse.groupchat;
}