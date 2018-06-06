package ru.spbau.mit.forum.server;

import org.json.JSONArray;
import org.json.JSONObject;
import ru.spbau.mit.forum.Branch;
import ru.spbau.mit.forum.Message;
import ru.spbau.mit.forum.protocol.HTTPRequest;
import ru.spbau.mit.forum.protocol.HTTPResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.*;

class ClientHandler implements Runnable {
    private Socket clientSocket;
    private String clientName;
    private final Set<String> clients;
    private List<Branch> branches;
    private final List<Message> messages;
    private Date lastUpdate;

    ClientHandler(Socket clientSocket, Set<String> clients, List<Branch> branches, List<Message> messages) {
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

        try {
            HTTPRequest request = HTTPRequest.parse(inputStream);
            while (request.getCommand() != HTTPRequest.CommandType.REGISTER) {
                HTTPResponse notRegisteredResponse = new HTTPResponse(
                        401,
                        "Please, introduce yourself",
                        Collections.emptyList());
                notRegisteredResponse.writeToStream(clientSocket.getOutputStream());
            }

            register(request);
        } catch(IOException e) {
            e.printStackTrace();
        }

        HTTPRequest request;
        while (true) {
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

    private void register(HTTPRequest request) throws IOException {
        String name = request.getJSONBody().getString("NAME");
        if (clients.contains(name)) {
            HTTPResponse nameAlreadyExistsResponse = new HTTPResponse(
                    409,
                    "The name already exists", Collections.emptyList());
            nameAlreadyExistsResponse.writeToStream(clientSocket.getOutputStream());
            return;
        }
        clients.add(name);
        clientName = name;
    }

    private void putNewMessage(HTTPRequest request) {
        JSONObject body = request.getJSONBody();
        int branch = body.getInt("BRANCH");
        String text = body.getString("TEXT");
        branches.get(branch).addToBranch(clientName, text);
    }

    private void sendNewMessages() throws IOException {
        List<Message> newMessages = new ArrayList<>();
        for (Branch branch : branches) {
            newMessages.addAll(branch.getMessageAfter(lastUpdate));
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
        for (Branch branch : branches) {
            body.put("BRANCH" + counter, branch);
            body.put("MESSAGES" + counter, branch.getMessages());
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
