package com.example.daml_chat.models;

import java.util.Date;

public class Message {
    public Chat parentChat;
    public Date postedAt;
    public String text;
    public Party poster;

    public Message() {}

    public Message(Chat parentChat, Date postedAt, String text, Party poster) {
        this.parentChat = parentChat;
        this.postedAt = postedAt;
        this.text = text;
        this.poster = poster;
    }
}
