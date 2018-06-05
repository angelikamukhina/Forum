package ru.spbau.mit.forum.protocol;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class HTTPRequest {
    private String type;
    private CommandType command;
    private List<String> body;

    public CommandType getCommand() {
        return command;
    }

    public List<String> getBody() {
        return body;
    }

    public JSONObject getJSONBody() {
        StringBuilder concatenated = new StringBuilder();
        for (String line : body) {
            concatenated.append(line);
        }
        return new JSONObject(concatenated);
    }

    public static HTTPRequest parse(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line = reader.readLine();
        String[] lineParts = line.split(" ");
        String type = lineParts[0];
        String command = lineParts[1].substring(1);
        do {
            line = reader.readLine();
        } while (!line.isEmpty());
        List<String> body = new ArrayList<>();
        line = reader.readLine();
        while (!line.isEmpty()) {
            body.add(line);
            line = reader.readLine();
        }
        return new HTTPRequest(type, command, body);
    }

    private HTTPRequest(String type, String command, List<String> body) {
        this.type = type;
        switch (command) {
            case "REGISTER":
                this.command = CommandType.REGISTER;
                break;
            case "NEW_MESSAGES":
                this.command = CommandType.NEW_MESSAGES;
                break;
            case "HIERARCHY":
                this.command = CommandType.HIERARCHY;
                break;
            case "PUT":
                this.command = CommandType.PUT;
                break;
            case "CLIENTS_ONLINE":
                this.command = CommandType.CLIENTS_ONLINE;
                break;
            case "STOP":
                this.command = CommandType.STOP;
                break;
        }
        this.body = body;
    }

    public enum CommandType {
        REGISTER, NEW_MESSAGES, HIERARCHY, PUT, CLIENTS_ONLINE, STOP
    }
}
