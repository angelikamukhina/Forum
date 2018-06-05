package ru.spbau.mit.forum;

import java.util.Date;

public class Message {
    private int id;
    private int branch;
    private String author;
    private String text;
    private Date date;

    public Message(int id, int branch, String author, String text) {
        this.id = id;
        this.branch = branch;
        this.author = author;
        this.text = text;
        date = new Date();
    }

    public String getAuthor() {
        return author;
    }

    public String getText() {
        return text;
    }

    public Date getDate() {
        return date;
    }
}
