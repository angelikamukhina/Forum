package ru.spbau.mit.forum.server;

import ru.spbau.mit.forum.Message;

import java.net.Socket;

class ClientHandler implements Runnable {
    private Socket clientSocket;
    private String clientName;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {

    }

    private void register(String clientName) {
    }

    private void putNewMessage(String branch, Message message) {
    }

    private void sendNewMessages() {
    }

    private void sendHierarchy() {
    }

    private void sendClientsOnline() {
    }

    private void stopConnection() {
    }
}
