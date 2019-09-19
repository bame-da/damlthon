package com.example.daml_chat;

import android.content.Intent;
import android.os.Bundle;

import com.example.daml_chat.http.HttpUtils;
import com.example.daml_chat.models.Chat;
import com.example.daml_chat.models.Message;
import com.example.daml_chat.services.ChatService;
import com.loopj.android.http.AsyncHttpResponseHandler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class ChatActivity extends AppCompatActivity {

    ListView chatHistory;
    List<Message> messages;
    ChatHistoryAdapter adapter;
    EditText newMessageTextEdit;
    int numMembers;
    Chat parentChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        newMessageTextEdit = findViewById(R.id.new_message);

        Intent i = getIntent();
        parentChat = (Chat)i.getSerializableExtra(getString(R.string.extra_chat));

        numMembers = parentChat.members.length;
        messages = new ArrayList<>();
        messages.add(new Message(parentChat, Calendar.getInstance().getTime(), "Message1", parentChat.members[0 % numMembers]));
        messages.add(new Message(parentChat, Calendar.getInstance().getTime(), "Message2", parentChat.members[1 % numMembers]));
        messages.add(new Message(parentChat, Calendar.getInstance().getTime(), "Message3", parentChat.members[2 % numMembers]));
        messages.add(new Message(parentChat, Calendar.getInstance().getTime(), "Message4", parentChat.members[3 % numMembers]));

        adapter = new ChatHistoryAdapter(this, messages);

        chatHistory = findViewById(R.id.chatHistory);

        chatHistory.setAdapter(adapter);
    }

    public void sendMessage(View view) {
        String message = newMessageTextEdit.getText().toString();
        if (message.length() > 0) {
            try {
                ChatService.GetChatGroups(getApplicationContext(), adapter, parentChat, numMembers);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            //HttpUtils.get("contracts/search", null, new MyResposeHandler(adapter, parentChat, numMembers));
            // TODO SEND MESSAGE
            newMessageTextEdit.getText().clear();
        }
    }
}

