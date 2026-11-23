package com.example.myapplication;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Application extends javafx.application.Application {

    @Override
    public void start(Stage stage) throws Exception {


        FXMLLoader loader = new FXMLLoader(
                Application.class.getResource("LoginView.fxml")
        );

        Scene scene = new Scene(loader.load(), 900, 600);

        stage.setTitle("JegyGo - Vonatjegy foglalás");
        stage.setScene(scene);
        stage.setMinWidth(700);
        stage.setMinHeight(500);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
