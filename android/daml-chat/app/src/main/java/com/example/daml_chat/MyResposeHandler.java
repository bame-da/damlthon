package com.example.daml_chat;

import com.example.daml_chat.models.Chat;
import com.example.daml_chat.models.Message;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.util.Calendar;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class MyResposeHandler extends AsyncHttpResponseHandler {

    ChatHistoryAdapter adapter;
    Chat parentChat;
    int numMembers;

    public MyResposeHandler(ChatHistoryAdapter adapter, Chat parentChat, int numMembers) {
        this.adapter = adapter;
        this.parentChat = parentChat;
        this.numMembers = numMembers;
    }

    @Override
    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
        adapter.add(new Message(parentChat, Calendar.getInstance().getTime(), "Message4", parentChat.members[3 % numMembers]));
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
        adapter.add(new Message(parentChat, Calendar.getInstance().getTime(), "Message4", parentChat.members[3 % numMembers]));
    }
}
