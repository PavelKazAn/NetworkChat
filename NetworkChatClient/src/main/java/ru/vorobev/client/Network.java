package ru.vorobev.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.function.Consumer;

public class Network {

    public static final String SERVER_HOST = "localhost";
    public static final int SERVER_PORT = 8189;

    private int port;
    private String host;
    private Socket socket;
    private DataOutputStream socketOutput;
    private DataInputStream socketInput;

    public Network(int port, String host) {
        this.port = port;
        this.host = host;
    }

    public Network(){
        this(SERVER_PORT,SERVER_HOST);
    }

    public boolean connect(){
        try {
            this.socket = new Socket(this.host, this.port );
            this.socketInput = new DataInputStream(socket.getInputStream());
            this.socketOutput = new DataOutputStream(socket.getOutputStream());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Не удалось установить соеденение");
            return false;
        }
    }

    public void sendMessage(String message) throws IOException{
        try {
            socketOutput.writeUTF(message);
        } catch (IOException e) {
            System.err.println("Не удалось отправить сообщение на сервер");
            throw e;
        }
    }

    public void waitMessages(Consumer<String> messageHandler){
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    String message = socketInput.readUTF();
                    messageHandler.accept(message);
                } catch (IOException e) {
                    System.err.println("Не удалось получить сообщение от сервера");
                    break;
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void close(){
        try {
            this.socket.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

}
