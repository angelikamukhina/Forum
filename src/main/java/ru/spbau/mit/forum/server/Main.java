package ru.spbau.mit.forum.server;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("Enter server port");
        Scanner scanner = new Scanner(System.in);
        int port = Integer.valueOf(scanner.next());
        int threadsNumber = 100000;

        Server server = new Server();
        Thread serverThread = new Thread(() -> {
            try {
                server.start(port, threadsNumber);
            } catch (IOException e) {
                System.out.println("Server error");
            }
        });

        serverThread.start();
        boolean stopped = false;
        while (!stopped) {
            String command = null;
            command = scanner.next();
            switch (command) {
                case "close":
                    String clientName = scanner.next();
                    server.closeConnection(clientName);
                    break;
                case "stop":
                    server.stop();
                    stopped = true;
                    break;
                    default:
                        System.out.println("Wrong command\n " +
                                "usage: \n" +
                                "close client_name -- for closing the connection with the client\n" +
                                "stop -- for stopping the server" );
            }
        }

    }
}
