package com.example.daml_chat;

import android.content.Intent;
import android.os.Bundle;

import com.example.daml_chat.models.Chat;
import com.example.daml_chat.models.Message;
import com.example.daml_chat.services.ChatService;
import com.example.daml_chat.services.MessageService;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    ListView chatHistory;
    List<Message> messages;
    MessageAdapter adapter;
    Chat parentChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        messages = new ArrayList<>();
        adapter = new MessageAdapter(this, messages);

        Intent i = getIntent();
        parentChat = (Chat)i.getSerializableExtra(getString(R.string.extra_chat));

        new EventAdapter(getApplicationContext(), adapter, parentChat);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(parentChat.name);

        chatHistory = findViewById(R.id.chatHistory);
        chatHistory.setAdapter(adapter);
    }

    public void sendMessage(View view) {
        EditText newMessageTextEdit = findViewById(R.id.new_message);

        String message = newMessageTextEdit.getText().toString();
        if (message.length() > 0) {
            MessageService.postMessage(getApplicationContext(), parentChat, message);
            newMessageTextEdit.getText().clear();
        }
    }

    public void leaveChat(View view) {
        ChatService.LeaveChat(getApplicationContext(), parentChat);
    }

    public void addUser(View view) {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
}

