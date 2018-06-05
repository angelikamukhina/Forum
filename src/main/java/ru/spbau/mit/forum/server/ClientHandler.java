package ru.spbau.mit.forum.server;

import org.json.JSONObject;
import ru.spbau.mit.forum.Message;
import ru.spbau.mit.forum.protocol.HTTPRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.List;
import java.util.Set;

class ClientHandler implements Runnable {
    private Socket clientSocket;
    private String clientName;
    private Set<String> clients;
    private List<Message> messages;

    public ClientHandler(Socket clientSocket, Set<String> clients, List<Message> messages) {
        this.clientSocket = clientSocket;
        this.clients = clients;
        this.messages = messages;
    }

    @Override
    public void run() {
        InputStream inputStream;
        try {
            inputStream = clientSocket.getInputStream();
        } catch (IOException e) {
            System.out.println("Unable to set the connection");
            return;
        }
        while (true) {
            HTTPRequest request;
            try {
                request = HTTPRequest.parse(inputStream);
            } catch (IOException e) {
                System.out.println("Unable to parse http request");
                return;
            }
            switch (request.getCommand()) {
                case REGISTER:
                    register(request);
                    break;
                case NEW_MESSAGES:
                    sendNewMessages();
                    break;
                case HIERARCHY:
                    sendHierarchy();
                    break;
                case PUT:
                    putNewMessage(request);
                    break;
                case CLIENTS_ONLINE:
                    sendClientsOnline();
                    break;
                case STOP:
                    stopConnection();
                    break;
            }
        }
    }

    private void register(HTTPRequest request) {
        String name = request.getJSONBody().getString("NAME");
        clients.add(name);
        clientName = name;
    }

    private void putNewMessage(HTTPRequest request) {
        JSONObject body = request.getJSONBody();
        int branch = body.getInt("BRANCH");
        String text = body.getString("TEXT");
        synchronized (messages) {
            int id = messages.size();
            Message message = new Message(id, branch, clientName, text);
            messages.add(message);
        }
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
