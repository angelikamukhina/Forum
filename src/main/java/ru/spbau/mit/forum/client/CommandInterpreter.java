package ru.spbau.mit.forum.client;

import org.json.JSONObject;
import ru.spbau.mit.forum.Message;
import ru.spbau.mit.forum.protocol.HTTPRequest;
import ru.spbau.mit.forum.protocol.HTTPResponse;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class CommandInterpreter {
    private Scanner scanner;
    private InputStream in;
    private OutputStream out;
    private String activeBranch = "ALL";
    private List<String> branches = null;

    public CommandInterpreter(Socket socket, Scanner scanner) throws IOException {
        this.scanner = scanner;
        in = socket.getInputStream();
        out = socket.getOutputStream();
    }

    public boolean interpret() {
        HTTPResponse response = null;
        String command = scanner.next();
        try {
            switch (command) {
                case ":q":
                    response = closeConnection();
                    // что приходит;
                    return false;
                case "hierarchy":
                    response = getForumHierarchy();
                    break;
                case ":set":
                    if (branches == null) {
                        System.out.println("You must request list of branches");
                    } else {
                        String branch = scanner.next();
                        if (!branches.contains(branch)) {
                            System.out.println("Target branch not exist");
                        } else {
                            activeBranch = branch;
                        }
                    }
                    break;
                case ":put":
                    if (activeBranch.equals("ALL")) {
                        System.out.println("You must set source branch");
                    } else {
                        String message = scanner.next();
                        response = putNewMessage(message);
                    }
                    break;
                case ":register":
                    String name = scanner.next();
                    response = register(name);
                    break;
                case ":new":
                    response = getNewMessages();
                    break;
                case ":online":
                    response = getClientsOnline();
                    break;
                default:
                    System.out.println("Unknown command");
            }
        } catch (IOException exception) {
            System.out.println("Unfortunately server can't to handle command");
            return true;
        }
        return true;
    }

    private HTTPResponse register(String name) throws IOException {
        JSONObject body = new JSONObject();
        body.put("NAME", name);
        HTTPRequest register = new HTTPRequest("POST", "REGISTER", Arrays.asList(body.toString()));
        register.writeToStream(out);
        return HTTPResponse.parse(in);
    }

    private HTTPResponse getNewMessages() throws IOException {
        JSONObject body = new JSONObject();
        if (activeBranch.equals("ALL")) {
            body.put("BRANCH", -1);
        } else {
            int index = branches.indexOf(activeBranch);
            if (index > 0) {
                body.put("BRANCH", index);
            } else {
                body.put("BRANCH", -1);
            }
        }
        HTTPRequest newMessages = new HTTPRequest("GET", "NEW_MESSAGES", Arrays.asList(body.toString()));
        newMessages.writeToStream(out);
        return HTTPResponse.parse(in);
    }

    private HTTPResponse getClientsOnline() throws IOException {
        HTTPRequest online = new HTTPRequest("GET", "CLIENTS_ONLINE", Arrays.asList());
        online.writeToStream(out);
        return HTTPResponse.parse(in);
    }

    private HTTPResponse putNewMessage(String message) throws IOException {
        JSONObject body = new JSONObject();
        int index = branches.indexOf(message);
        body.put("BRANCH", index);
        body.put("TEXT", message);
        HTTPRequest put = new HTTPRequest("POST", "PUT", Arrays.asList(body.toString()));
        put.writeToStream(out);
        return HTTPResponse.parse(in);
    }

    private HTTPResponse closeConnection() throws IOException {
        HTTPRequest stop = new HTTPRequest("POST", "STOP", Arrays.asList());
        stop.writeToStream(out);
        return HTTPResponse.parse(in);
    }

    public HTTPResponse getForumHierarchy() throws IOException {
        HTTPRequest hierarchy = new HTTPRequest("GET", "HIERARCHY", Arrays.asList());
        hierarchy.writeToStream(out);
        return HTTPResponse.parse(in);
    }

    private void parseHierarchyResponse(HTTPResponse response) {
        JSONObject body = response.getJSONBody();

    }
}
