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
            new Branch("C++"),
            new Branch("Java"),
            new Branch("Python"));
    private ServerSocket serverSocket;
    private Map<String, Socket> clients;

    public void start(int port, int threadsNumber) throws IOException {
        ExecutorService threadPool = Executors.newFixedThreadPool(threadsNumber);
        serverSocket = new ServerSocket(port);
        clients = new HashMap<>();

        try {
            System.out.println("Server is running");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                threadPool.submit(new ClientHandler(clientSocket, clients, branches));
            }
        } catch (SocketException ignored) {
        }
    }

    public void stop() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            System.out.println("Error while stopping the server");
        }
    }

    public void closeConnection(String clientName) {
        if (clients.containsKey(clientName)) {
            Socket clientSocket = clients.get(clientName);
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Error while closing connection with " + clientName);
            }
            clients.remove(clientName);
        } else {
            System.out.println("There is no online client with name: " + clientName);
        }
    }
}
