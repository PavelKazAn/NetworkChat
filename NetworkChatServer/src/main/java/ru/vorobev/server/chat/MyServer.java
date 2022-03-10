package ru.vorobev.server.chat;

import ru.vorobev.clientserver.Command;
import ru.vorobev.server.chat.auth.IAuthService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MyServer {

    private final List<ClientHandler> clients = new ArrayList<>();
    private IAuthService authService;

    public void start(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server has been started");
            while (true) {
                waitAndProcessClientConnection(serverSocket);
            }

        } catch (IOException e) {
            System.err.println("Failed to bind port " + port);
            e.printStackTrace();
        }
    }

    private void waitAndProcessClientConnection(ServerSocket serverSocket) throws IOException {
        System.out.println("Waiting for new client connection");
        Socket clientSocket = serverSocket.accept();
        System.out.println("Client has been connected");
        ClientHandler clientHandler = new ClientHandler(this, clientSocket);
        clientHandler.handle();
    }

    public synchronized void broadcastMessage(String message, ClientHandler sender) throws IOException {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendCommand(Command.clientMessageCommand(sender.getUserName(), message));
            }
        }
    }

    public synchronized void sendPrivateMessage(ClientHandler sender, String recipient, String privateMessage) throws IOException {
        for (ClientHandler client : clients) {
            if (client != sender && client.getUserName().equals(recipient)) {
                client.sendCommand(Command.clientMessageCommand(sender.getUserName(), privateMessage));
            }
        }
    }

    public synchronized boolean isUsernameBusy(String username) {
        for (ClientHandler client : clients) {
            if (client.getUserName().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void subscribe(ClientHandler clientHandler) throws IOException {
        clients.add(clientHandler);
        notifyClientUserListUpdate();
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) throws IOException {
        clients.remove(clientHandler);
        notifyClientUserListUpdate();
    }

    public IAuthService getAuthService() {
        return authService;
    }

    public void notifyClientUserListUpdate() throws IOException {

        List<String> userListOnline = new ArrayList<>();

        for (ClientHandler client : clients) {
            userListOnline.add(client.getUserName());
        }
        for (ClientHandler client : clients) {
            client.sendCommand(Command.updateUserListCommand(userListOnline));
        }
    }

}
