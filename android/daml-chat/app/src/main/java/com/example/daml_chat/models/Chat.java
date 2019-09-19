package com.example.daml_chat.models;

import java.io.Serializable;

public class Chat implements Serializable {
    public String name;
    public Party[] members;

    public Chat() {}

    public Chat(String name, Party[] members) {
        this.name = name;
        this.members = members;
    }
}
