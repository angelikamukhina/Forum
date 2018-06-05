package ru.spbau.mit.forum.client;


import ru.spbau.mit.forum.Message;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Client {
    private Socket socket;
    public void start(String hostname, int port) {
    }

    public List<Message> getNewMessages() {
        return new ArrayList<>();
    }

    public List<String> getForumHierarchy() {
        return new ArrayList<>();
    }

    public void chooseBranch(String branch) {
    }

    public void putNewMessage(String branch, Message message) {
    }

    public List<String> getClientsOnline() {
        return new ArrayList<>();
    }

    public void closeConnection() {
    }

    public void onConnectionClosedForcibly() {
    }
}
