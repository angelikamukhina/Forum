package ru.spbau.mit.forum;

import java.util.Date;

public class Message {
    private String author;
    private String text;
    private Date date;

    public Message(String author, String text) {
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
