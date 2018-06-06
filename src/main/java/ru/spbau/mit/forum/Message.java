package ru.spbau.mit.forum;

import java.util.Date;

public class Message {
    private int id;
    private String branch;
    private String author;
    private String text;
    private Date date;

    public Message(int id, String branch, String author, String text) {
        this.id = id;
        this.branch = branch;
        this.author = author;
        this.text = text;
        date = new Date();
    }

    // Подставной message для поиска в списке
    public Message(String branch, Date date) {
        this.branch = branch;
        this.date = date;
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

