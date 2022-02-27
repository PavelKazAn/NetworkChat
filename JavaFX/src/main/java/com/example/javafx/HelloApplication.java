package com.example.javafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("hello-view.fxml"));

        Parent load = fxmlLoader.load();
        Scene scene = new Scene(load);

        stage.setTitle("Онлайн чат GeekBrains");
        stage.setScene(scene);

        HelloController controller = fxmlLoader.getController();
        controller.userList.getItems().addAll("user1", "user2");

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}