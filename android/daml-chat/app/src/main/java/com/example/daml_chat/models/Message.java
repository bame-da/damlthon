package com.example.daml_chat.models;

import com.daml.ledger.javaapi.data.LedgerOffset;

import java.time.Instant;

public class Message {
    public Chat parentChat;
    public Instant postedAt;
    public String text;
    public Party poster;
    public LedgerOffset offset;

    public Message() {}

    public Message(Chat parentChat, Instant postedAt, String text, Party poster, LedgerOffset offset) {
        this.parentChat = parentChat;
        this.postedAt = postedAt;
        this.text = text;
        this.poster = poster;
        this.offset = offset;
    }
}
