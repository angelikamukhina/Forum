package ru.spbau.mit.forum.protocol;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

public class HTTPResponse {
    private static final String VERSION = "HTTP/1.1";
    private int statusCode;
    private String reasonPhrase;
    private List<String> body;

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
}
