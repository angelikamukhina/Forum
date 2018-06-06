package ru.spbau.mit.forum;

import java.util.Date;

public class Message {
    private String branch;
    private String author;
    private String text;
    private Date date;

    public Message(String branch, String author, String text) {
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

