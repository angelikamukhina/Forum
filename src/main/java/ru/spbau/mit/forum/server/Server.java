package ru.spbau.mit.forum.server;


import ru.spbau.mit.forum.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private ServerSocket serverSocket;
    private List<String> clients;
    private List<Message> messages;

    public void start(int port, int threadsNumber) throws IOException {
        ExecutorService threadPool = Executors.newFixedThreadPool(threadsNumber);
        serverSocket = new ServerSocket(port);
        clients = new CopyOnWriteArrayList<>();
        messages = new CopyOnWriteArrayList<>();

        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                threadPool.submit(new ClientHandler(clientSocket, clients, messages));
            }
        } catch (SocketException ignored) {
        }
    }

    public void stop() {
    }

    public void closeConnection(String clientName) {
    }
}
