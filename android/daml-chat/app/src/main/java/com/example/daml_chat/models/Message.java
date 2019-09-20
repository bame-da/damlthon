package com.example.daml_chat.models;

import com.daml.ledger.javaapi.data.LedgerOffset;

import java.time.Instant;
import java.util.Optional;

public class Message {
    public Chat parentChat;
    public Instant postedAt;
    public String text;
    public Party poster;
    public Optional<String> hash;

    public Message() {}

    public Message(Chat parentChat, Instant postedAt, String text, Party poster, Optional<String> hash) {
        this.parentChat = parentChat;
        this.postedAt = postedAt;
        this.text = text;
        this.poster = poster;
        this.hash = hash;
    }
}
