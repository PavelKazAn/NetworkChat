package ru.vorobev.server.chat;

import ru.vorobev.clientserver.Command;
import ru.vorobev.clientserver.CommandType;
import ru.vorobev.clientserver.commands.AuthCommandData;
import ru.vorobev.clientserver.commands.PrivateMessageCommandData;
import ru.vorobev.clientserver.commands.PublicMessageCommandData;

import java.io.*;
import java.net.Socket;

public class ClientHandler {

    private MyServer server;
    private final Socket clientSocket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private String username;

    public ClientHandler(MyServer myServer, Socket clientSocket) {
        this.server = myServer;
        this.clientSocket = clientSocket;
    }

    public void handle() throws IOException {
        inputStream = new ObjectInputStream(clientSocket.getInputStream());
        outputStream = new ObjectOutputStream(clientSocket.getOutputStream());

        new Thread(() -> {
            try {
                authenticate();
                readMessages();
            } catch (IOException e) {
                System.err.println("Failed to process message to client");
                e.printStackTrace();
            } finally {
                try {
                    closeConnection();
                } catch (IOException e) {
                    System.err.println("Failed to close connection");
                }
            }
        }).start();

    }

    private void authenticate() throws IOException {
        while (true) {
            Command command = readCommand();
            if (command == null) {
                continue;
            }

            if (command.getType() == CommandType.AUTH) {
                AuthCommandData data = (AuthCommandData) command.getData();
                String login = data.getLogin();
                String password = data.getPassword();

                String userName = server.getAuthService().getUserNameByLoginAndPassword(login, password);

                if (userName == null) {
                    sendCommand(Command.errorCommand("Некорректный логин и пароль"));
                } else if (server.isUsernameBusy(userName)) {
                    sendCommand(Command.errorCommand("Такой пользователь уже существует"));
                } else {
                    this.username = userName;
                    sendCommand(Command.authOkCommand(userName));
                    server.subscribe(this);
                    return;
                }
            }
        }
    }

    public void sendCommand(Command command) throws IOException {
        outputStream.writeObject(command);
    }

    private Command readCommand() throws IOException {
        Command command = null;
        try {
            command = (Command) inputStream.readObject();
        } catch (ClassNotFoundException e) {
            System.err.println("Failed to read Command class");
            e.printStackTrace();
        }
        return command;
    }

    private void readMessages() throws IOException {
        while (true) {
//            String message = inputStream.readUTF().trim();
            Command command = readCommand();
            if (command == null) {
                continue;
            }

            switch (command.getType()) {
                case END:
                    return;
                case PRIVATE_MESSAGE: {
                    PrivateMessageCommandData data = (PrivateMessageCommandData) command.getData();
                    String recipient = data.getReceiver();
                    String privateMessage = data.getMessage();
                    server.sendPrivateMessage(this, recipient, privateMessage);
                    break;
                }
                case PUBLIC_MESSAGE: {
                    PublicMessageCommandData data = (PublicMessageCommandData) command.getData();
                    processMessage(data.getMessage());
                }
            }
        }
    }

    private void processMessage(String message) throws IOException {
        this.server.broadcastMessage(message, this);
    }

    private void closeConnection() throws IOException {
        server.unsubscribe(this);
        clientSocket.close();
    }

    public String getUsername() {
        return username;
    }
}
