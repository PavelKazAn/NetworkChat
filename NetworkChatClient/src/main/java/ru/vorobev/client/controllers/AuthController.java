package ru.vorobev.client.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ru.vorobev.client.ClientChat;
import ru.vorobev.client.Network;

import java.io.IOException;
import java.util.function.Consumer;

public class AuthController {
    public static final String AUTH_COMMAND = "/auth";
    public static final String AUTH_OK_COMMAND = "/authOk";

    public TextField loginField;
    public PasswordField passwordField;
    public Button authButton;

    private ClientChat clientChat ;

    public void executeAuth(ActionEvent actionEvent) {
        String login = loginField.getText();
        String password = passwordField.getText();
         if(login == null || login.isBlank() || password.isBlank()| password.isBlank()){
            clientChat.showErrorDialog("Логин и пароль должны быть указаны");
            return;
         }

         String authCommandMessage = String.format("%s %s %s", AUTH_COMMAND,login ,password);

        try {
            Network.getInstance().sendMessage(authCommandMessage);
        } catch (IOException e) {
            clientChat.showErrorDialog("Ошибка передачи данных по сети");
            e.printStackTrace();
        }
    }

    public void setClientChat(ClientChat clientChat) {
        this.clientChat = clientChat;
    }

    public void initializeMessageHandler() {
        Network.getInstance().waitMessages(new Consumer<String>() {
            @Override
            public void accept(String message) {
                if(message.startsWith(AUTH_OK_COMMAND)){
                    String[] parts = message.split(" ");
                    String userName = parts[1];
                    Thread.currentThread().interrupt();
                    Platform.runLater(() -> {
                        clientChat.getChatStage().setTitle(userName);
                        clientChat.getAuthStage().close();
                    });
                }else {
                    Platform.runLater(() -> {
                        clientChat.showErrorDialog("Логин или пароль неверный");
                    });
                }
            }
        });
    }
}
