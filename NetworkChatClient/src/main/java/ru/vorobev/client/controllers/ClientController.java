package ru.vorobev.client.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ru.vorobev.client.ClientChat;
import ru.vorobev.client.model.Network;
import ru.vorobev.client.model.ReadCommandListener;
import ru.vorobev.clientserver.Command;
import ru.vorobev.clientserver.CommandType;
import ru.vorobev.clientserver.commands.ClientMessageCommandData;
import ru.vorobev.clientserver.commands.UpdateUserListCommandData;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class ClientController {

    @FXML
    private TextArea textArea;
    @FXML
    private TextField textField;
    @FXML
    private Button sendButton;
    @FXML
    public ListView<String> userList;

    private ClientChat application;

    public void sendMessage() {
        String message = textField.getText().trim();

        if (message.isEmpty()) {
            textField.clear();
            return;
        }

        String sender = null;
        if (userList.getSelectionModel().isEmpty()) {
            sender = userList.getSelectionModel().getSelectedItem();
        }
        try {
            if(sender != null){
                Network.getInstance().sendPrivateMessage(sender,message);
            }else{
                Network.getInstance().sendMessage(message);
            }
        } catch (IOException e) {
            application.showErrorDialog("Ошибка передачи данных по сети");
        }

        appendMessageToChat("Я", message);
    }

    private void appendMessageToChat(String sender, String message) {
        textArea.appendText(DateFormat.getDateTimeInstance().format(new Date()));
        textArea.appendText(System.lineSeparator());

        if (sender != null) {
            textArea.appendText(sender + ": ");
            textArea.appendText(System.lineSeparator());

        }

        textArea.appendText(message);
        textArea.appendText(System.lineSeparator());
        textArea.appendText(System.lineSeparator());
        textField.setFocusTraversable(true);
        textField.clear();
    }

    public void setApplication(ClientChat application) {
        this.application = application;
    }

    public void initializeMessageHandler() {
        Network.getInstance().addReadMessageListener(new ReadCommandListener() {
            @Override
            public void processReceivedCommand(Command command) {
                if(command.getType() == CommandType.CLIENT_MESSAGE){
                    ClientMessageCommandData data = (ClientMessageCommandData) command.getData();
                    appendMessageToChat(data.getSender(),data.getMessage());
                } else if(command.getType() == CommandType.UPDATE_USER_LIST){
                    UpdateUserListCommandData data = (UpdateUserListCommandData) command.getData();
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            userList.setItems(FXCollections.observableList(data.getUsers()));
                        }
                    });
                }
            }
        });
    }
}