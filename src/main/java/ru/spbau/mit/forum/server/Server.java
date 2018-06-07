package ru.spbau.mit.forum.server;


import ru.spbau.mit.forum.Branch;
import ru.spbau.mit.forum.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private List<Branch> branches = Arrays.asList(
            new Branch("Алгоритмы"),
            new Branch("С++"),
            new Branch("Java"),
            new Branch("Python"));
    private ServerSocket serverSocket;
    private Map<String, Socket> clients;

    public void start(int port, int threadsNumber) throws IOException {
        ExecutorService threadPool = Executors.newFixedThreadPool(threadsNumber);
        serverSocket = new ServerSocket(port);
        clients = new HashMap<>();

        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                threadPool.submit(new ClientHandler(clientSocket, clients, branches));
            }
        } catch (SocketException ignored) {
        }
    }

    public void stop() {
    }

    public void closeConnection(String clientName) {

    }
}
