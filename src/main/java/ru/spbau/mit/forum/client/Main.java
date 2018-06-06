package ru.spbau.mit.forum.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("Enter forum server hostname");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String hostname;
        try {
            hostname = reader.readLine();
        } catch (IOException e) {
            System.out.println("Error while reading hostname");
            return;
        }
        System.out.println("Enter forum server port");
        int port;
        try {
            port = Integer.getInteger(reader.readLine());
        } catch (IOException e) {
            System.out.println("Error while reading port");
            return;
        }

        try (Client client = new Client(hostname, port)) {
            client.connect(new Scanner(System.in));
            client.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
