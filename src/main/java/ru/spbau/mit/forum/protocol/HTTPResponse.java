package ru.spbau.mit.forum.protocol;

import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class HTTPResponse {
    private static final String VERSION = "HTTP/1.1";
    private int statusCode;
    private String reasonPhrase;
    private List<String> body;

    public JSONObject getJSONBody() {
        StringBuilder concatenated = new StringBuilder();
        for (String line : body) {
            concatenated.append(line);
        }
        return new JSONObject(concatenated);
    }

    public HTTPResponse(int statusCode, String reasonPhrase, List<String> body) {
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
        this.body = body;
    }

    public void writeToStream(OutputStream outputStream) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
        writer.write(VERSION + " " + Integer.toString(statusCode) + " " + reasonPhrase + "\n");
        writer.write("\n");
        for (String line : body) {
            writer.write(line);
        }
        writer.write("\n");
    }

    public static HTTPResponse parse(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line = reader.readLine();
        String[] lineParts = line.split(" ");
        int statusCode = Integer.parseInt(lineParts[1]);
        String reasonPhrase = lineParts[2];
        do {
            line = reader.readLine();
        } while (!line.isEmpty());
        List<String> body = new ArrayList<>();
        line = reader.readLine();
        while (!line.isEmpty()) {
            body.add(line);
            line = reader.readLine();
        }
        return new HTTPResponse(statusCode, reasonPhrase, body);
    }
}
