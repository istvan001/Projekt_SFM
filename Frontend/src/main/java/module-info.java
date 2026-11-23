module com.example.myapplication {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.net.http;



    requires com.fasterxml.jackson.databind;
    opens com.example.myapplication to com.fasterxml.jackson.databind;
    exports com.example.myapplication;
    exports com.example.myapplication.controller;
    opens com.example.myapplication.controller to javafx.fxml;
}
