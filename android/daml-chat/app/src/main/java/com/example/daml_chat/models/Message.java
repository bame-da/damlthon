package com.example.daml_chat.models;

import java.time.Instant;

public class Message {
    public Chat parentChat;
    public Instant postedAt;
    public String text;
    public Party poster;

    public Message() {}

    public Message(Chat parentChat, Instant postedAt, String text, Party poster) {
        this.parentChat = parentChat;
        this.postedAt = postedAt;
        this.text = text;
        this.poster = poster;
    }
}
