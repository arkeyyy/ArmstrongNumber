module org.example.armstrong_number {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.armstrong_number to javafx.fxml;
    exports org.example.armstrong_number;
}