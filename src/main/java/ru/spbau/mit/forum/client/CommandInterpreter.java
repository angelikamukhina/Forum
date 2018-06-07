package ru.spbau.mit.forum.client;

import org.json.JSONArray;
import org.json.JSONObject;
import ru.spbau.mit.forum.protocol.HTTPRequest;
import ru.spbau.mit.forum.protocol.HTTPResponse;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;

public class CommandInterpreter {
    private static String usage = ":q --- for closing connection\n" +
            ":register <name> --- for registration\n" +
            ":hierarchy [-m] --- for printing hierarchy [with messages]\n" +
            ":set <branch_name> --- for setting active branch\n" +
            ":put <message> --- for post to active branch\n" +
            ":new --- for getting new message\n" +
            ":online -- for getting online users\n" +
            ":u --- for printing usage";

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

    public void onConnectionSetted(String name) throws IOException {
        System.out.println("> :register " + name);
        parseRegisterResponce(register(name));
        System.out.println("> :hierarchy");
        parseHierarchyResponse(getForumHierarchy(), false);
    }

    public boolean interpret() {
        HTTPResponse response = null;
        String command = scanner.next();
        try {
            switch (command) {
                case ":q":
                    response = closeConnection();
                    parseStop(response);
                    return false;
                case ":hierarchy":
                    response = getForumHierarchy();
                    if (scanner.hasNext("-m")) {
                        scanner.next();
                        parseHierarchyResponse(response, true);
                    } else {
                        parseHierarchyResponse(response, false);
                    }
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
                        String message = scanner.nextLine();
                        response = putNewMessage(message.substring(1));
                        parsePutResponse(response);
                    }
                    break;
                case ":register":
                    String name = scanner.next();
                    response = register(name);
                    parseRegisterResponce(response);
                    break;
                case ":new":
                    response = getNewMessages();
                    parseNewMessages(response);
                    break;
                case ":online":
                    response = getClientsOnline();
                    parseOnline(response);
                    break;
                case ":u":
                    System.out.println(usage);
                    break;
                default:
                    System.out.println("Unknown command");
                    System.out.println(usage);
            }
        } catch (SocketException exception) {
            onConnectionClosedForcibly();
            return false;
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
        int index = branches.indexOf(activeBranch);
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

    private HTTPResponse getForumHierarchy() throws IOException {
        HTTPRequest hierarchy = new HTTPRequest("GET", "HIERARCHY", Arrays.asList());
        hierarchy.writeToStream(out);
        return HTTPResponse.parse(in);
    }

    private void printHierarchy(HTTPResponse response) {
        JSONObject body = response.getJSONBody();
        int count = body.getInt("AMOUNT");
        if (branches == null) {
            branches = new ArrayList<String>();
        }
        for (int i = 0; i < count; i++) {
            String branch = body.getString("BRANCH" + i);
            if (branches.size() < count) {
                branches.add(branch);
            }
            System.out.println(branch);
        }
        System.out.println("------------------------------------");
    }

    private void parseHierarchyResponse(HTTPResponse response, boolean printMessage) {
        if (!checkRegister(response)) return;
        printHierarchy(response);
        if (printMessage) {
            JSONObject body = response.getJSONBody();
            int count = body.getInt("AMOUNT");
            for (int i = 0; i < count; i++) {
                String branch = body.getString("BRANCH" + i);
                JSONArray messages = body.getJSONArray("MESSAGES" + i);
                for (int j = 0; j < messages.length(); j++) {
                    JSONObject object = messages.getJSONObject(j);
                    System.out.println(
                            branch + ": " +
                                    object.getString("AUTHOR") + " in " +
                                    object.getString("DATE") + " posted: " +
                                    object.getString("TEXT")
                    );
                }
            }
        }
    }

    private void parsePutResponse(HTTPResponse response) {
        if (!checkRegister(response)) return;
        if (response.getStatus() == 200) {
            System.out.println("Your message is posted");
        }
    }

    private void parseRegisterResponce(HTTPResponse response) {
        if (response.getStatus() == 200) {
            System.out.println("You are successfully registered");
        }
        if (response.getStatus() == 409) {
            System.out.println("This name already exists");
        }
    }

    private void parseNewMessages(HTTPResponse response) {
        if (!checkRegister(response)) return;
        JSONObject body = response.getJSONBody();
        if (response.getStatus() == 200) {
            JSONArray messages = body.getJSONArray("MESSAGES");
            for (int i = 0; i < messages.length(); i++) {
                JSONObject object = messages.getJSONObject(i);
                if (!activeBranch.equals("ALL")) {
                    System.out.print(object.getString("BRANCH") + ": ");
                }
                System.out.println(
                                object.getString("AUTHOR") + " in " +
                                object.getString("DATE") + " posted: " +
                                object.getString("TEXT")
                );
            }
        }
    }

    private void parseOnline(HTTPResponse response) {
        if (!checkRegister(response)) return;
        if (response.getStatus() == 200) {
            System.out.println("Now is online:");
            JSONObject body = response.getJSONBody();
            int count = body.getInt("AMOUNT");
            for (int i = 0; i < count; i++) {
                System.out.println(body.getString("CLIENT" + i));
            }
        }
    }

    private void parseStop(HTTPResponse response) {
        if (response.getStatus() == 200) {
            System.out.println("Connection is broken");
        } else {
            System.out.println("Connection isn't broken correctly");
        }
    }

    private boolean checkRegister(HTTPResponse response) {
        if (response.getStatus() == 401) {
            System.out.println("You are not registered");
            return false;
        }
        return true;
    }

    private void onConnectionClosedForcibly() {
        System.out.println("Connection was broken by the server");
    }
}
