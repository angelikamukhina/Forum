package ru.spbau.mit.forum.server;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        Server server = new Server();
        try {
            server.start(5300, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
