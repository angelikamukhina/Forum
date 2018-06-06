package ru.spbau.mit.forum.client;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

public class Client implements AutoCloseable {
    private Socket socket;
    private final String host;
    private final int port;
    private CommandInterpreter interpreter;

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect(Scanner scanner) throws IOException {
        socket = new Socket(host, port);
        interpreter = new CommandInterpreter(socket, scanner);
    }

    public void run() throws IOException {
        boolean isContinue = true;
        while (isContinue) {
            try {
                System.out.print("> ");
                isContinue = interpreter.interpret();
            } catch (SocketException exception) {
                onConnectionClosedForcibly();
                isContinue = false;
            }

        }
        close();
    }

    @Override
    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onConnectionClosedForcibly() {
        System.out.println("Connection was broken by the server");
    }
}
