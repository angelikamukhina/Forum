package ru.spbau.mit.forum;

import java.util.*;
import java.util.stream.Collectors;

public class Branch {
    private static int INITIAL_CAPACITY = 1000;

    private String name;
    private PriorityQueue<Message> messages;

    public Branch(String name) {
        this.name = name;
        this.messages = new PriorityQueue<Message>(INITIAL_CAPACITY, new MessageComparator());
    }

    public synchronized void addToBranch(int id, String author, String text) {
        messages.add(new Message(id, name, author, text));
    }

    public List<Message> getMessages() {
        return messages.stream().collect(Collectors.toList());
    }

    public List<Message> getMessageAfter(Date date) {
        List<Message> all = messages.stream().collect(Collectors.toList());
        List<Message> newPost = new ArrayList<>();
        synchronized (this) {
            int position = Collections.binarySearch(all, new Message(name, date), new MessageComparator());
            if (position > 0) {
                newPost = all.subList(position, all.size());
            } else {
                newPost = all.subList((position + 1) * -1, all.size());
            }
        }
        return newPost;
    }
}
