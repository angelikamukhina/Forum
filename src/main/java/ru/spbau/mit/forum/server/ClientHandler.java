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
    private final Map<String, Socket> clients;
    private List<Branch> branches;
    private Date lastUpdate;

    ClientHandler(Socket clientSocket, Map<String, Socket> clients, List<Branch> branches) {
        this.clientSocket = clientSocket;
        this.clients = clients;
        this.branches = branches;
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
                        sendNewMessages(request);
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
        if (clients.containsKey(name)) {
            HTTPResponse nameAlreadyExistsResponse = new HTTPResponse(
                    409,
                    "The name already exists", Collections.emptyList());
            nameAlreadyExistsResponse.writeToStream(clientSocket.getOutputStream());
            return;
        }
        clients.put(name, clientSocket);
        clientName = name;
        HTTPResponse response = new HTTPResponse(200, "OK", Collections.emptyList());
        response.writeToStream(clientSocket.getOutputStream());
    }

    private void putNewMessage(HTTPRequest request) throws IOException {
        JSONObject body = request.getJSONBody();
        int branch = body.getInt("BRANCH");
        String text = body.getString("TEXT");
        branches.get(branch).addToBranch(clientName, text);
        HTTPResponse response = new HTTPResponse(200, "OK", Collections.emptyList());
        response.writeToStream(clientSocket.getOutputStream());
    }

    private void sendNewMessages(HTTPRequest request) throws IOException {
        JSONObject body = request.getJSONBody();
        int branchIdx = body.getInt("BRANCH");
        List<Message> newMessages;
        if (branchIdx != -1) {
            newMessages = branches.get(branchIdx).getMessageAfter(lastUpdate);
        } else {
            newMessages = new ArrayList<>();
            for (Branch branch : branches) {
                newMessages.addAll(branch.getMessageAfter(lastUpdate));
            }
        }
        lastUpdate = new Date();

        JSONArray newMessagesJSON = messagesToJSONArray(newMessages);
        JSONObject responseBody = new JSONObject();
        responseBody.put("MESSAGES", newMessagesJSON);
        List<String> responseString = Collections.singletonList(responseBody.toString());
        HTTPResponse response = new HTTPResponse(200, "OK", responseString);
        response.writeToStream(clientSocket.getOutputStream());
    }

    private void sendHierarchy() throws IOException {
        JSONObject body = new JSONObject();
        body.put("AMOUNT", branches.size());
        int counter = 0;
        for (Branch branch : branches) {
            body.put("BRANCH" + counter, branch.getName());
            List<Message> messages = branch.getMessages();
            JSONArray messagesJSON = messagesToJSONArray(messages);
            body.put("MESSAGES" + counter, messagesJSON);
            counter++;
        }
        String stringBody = body.toString();
        List<String> bodyList = Collections.singletonList(stringBody);
        HTTPResponse response = new HTTPResponse(200, "OK", bodyList);
        response.writeToStream(clientSocket.getOutputStream());
    }

    private JSONArray messagesToJSONArray(List<Message> messages) {
        JSONArray messagesJSON = new JSONArray();
        for (Message message : messages) {
            JSONObject messageJSON = new JSONObject();
            messageJSON.put("AUTHOR", message.getAuthor());
            messageJSON.put("DATE", message.getDate().toString());
            messageJSON.put("TEXT", message.getText());
            messagesJSON.put(messageJSON);
        }
        return messagesJSON;
    }

    private void sendClientsOnline() throws IOException {
        JSONObject body = new JSONObject();
        synchronized (clients) {
            body.put("AMOUNT", clients.size());
            int counter = 0;
            for (Map.Entry client : clients.entrySet()) {
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

    private void stopConnection() throws IOException {
        clients.remove(clientName);
        HTTPResponse response = new HTTPResponse(200, "OK", Collections.emptyList());
        response.writeToStream(clientSocket.getOutputStream());
    }
}
