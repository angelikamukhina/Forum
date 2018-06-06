package ru.spbau.mit.forum.server;

import org.json.JSONArray;
import org.json.JSONObject;
import ru.spbau.mit.forum.Message;
import ru.spbau.mit.forum.protocol.HTTPRequest;
import ru.spbau.mit.forum.protocol.HTTPResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

class ClientHandler implements Runnable {
    private Socket clientSocket;
    private String clientName;
    private final Set<String> clients;
    private Set<String> branches;
    private final List<Message> messages;
    private Date lastUpdate;

    public ClientHandler(Socket clientSocket, Set<String> clients, Set<String> branches, List<Message> messages) {
        this.clientSocket = clientSocket;
        this.clients = clients;
        this.branches = branches;
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
            try {
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
            } catch (IOException e) {
                System.out.println("Error while requests processing");
                return;
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
        String branch = body.getString("BRANCH");
        String text = body.getString("TEXT");
        synchronized (messages) {
            int id = messages.size();
            Message message = new Message(id, branch, clientName, text);
            messages.add(message);
        }
    }

    private void sendNewMessages() throws IOException {
        List<Message> newMessages = new ArrayList<>();
        synchronized (messages) {
            for (Message message : messages) {
                if (message.getDate().after(lastUpdate)) {
                    newMessages.add(message);
                }
            }
        }
        lastUpdate = new Date();
        JSONArray newMessagesJSON = new JSONArray(newMessages);
        String messagesString = newMessagesJSON.toString();
        List<String> body = new ArrayList<>();
        body.add(messagesString);
        HTTPResponse response = new HTTPResponse(200, "OK", body);
        response.writeToStream(clientSocket.getOutputStream());
    }

    private void sendHierarchy() throws IOException {
        JSONObject body = new JSONObject();
        body.put("AMOUNT", branches.size());
        int counter = 0;
        for (String branch : branches) {
            body.put("BRANCH" + counter, branch);
            counter++;
        }
        String stringBody = body.toString();
        List<String> bodyList = new ArrayList<>();
        bodyList.add(stringBody);
        HTTPResponse response = new HTTPResponse(200, "OK", bodyList);
        response.writeToStream(clientSocket.getOutputStream());
    }

    private void sendClientsOnline() throws IOException {
        JSONObject body = new JSONObject();
        synchronized (clients) {
            body.put("AMOUNT", clients.size());
            int counter = 0;
            for (String client : clients) {
                body.put("CLIENT" + counter, client);
                counter ++;
            }
        }
        String stringBody = body.toString();
        List<String> bodyList = new ArrayList<>();
        bodyList.add(stringBody);
        HTTPResponse response = new HTTPResponse(200, "OK", bodyList);
        response.writeToStream(clientSocket.getOutputStream());
    }

    private void stopConnection() {
    }
}
