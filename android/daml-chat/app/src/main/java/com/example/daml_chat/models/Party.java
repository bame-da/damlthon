package com.example.daml_chat.models;

import androidx.annotation.Nullable;

import java.io.Serializable;

public class Party implements Serializable {
    public String name;

    public Party (String name) {
        this.name = name;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        Party otherParty = (Party)obj;
        return this.name.equals(otherParty.name);
    }
}
