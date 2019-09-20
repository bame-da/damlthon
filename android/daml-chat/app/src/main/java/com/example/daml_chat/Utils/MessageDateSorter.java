package com.example.daml_chat.Utils;

import com.example.daml_chat.models.Message;

import java.util.Comparator;

public class MessageDateSorter implements Comparator<Message> {
    @Override
    public int compare(Message m1, Message m2) {
        return m1.postedAt.compareTo(m2.postedAt);
    }
}
