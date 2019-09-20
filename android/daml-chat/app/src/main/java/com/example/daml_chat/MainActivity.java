package com.example.daml_chat;

import android.content.Intent;
import android.os.Bundle;

import com.example.daml_chat.core.Globals;
import com.example.daml_chat.models.Chat;
import com.example.daml_chat.models.Party;
import com.example.daml_chat.services.ChatService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ListView listView;
    String[] listItem;
    String[] listItemSubtitles;
    ChatAdapter adapter;
    Globals g = Globals.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        List<Chat> chats = new ArrayList<>();
        adapter = new ChatAdapter(this, chats);

        ChatService.GetChatGroups(getApplicationContext(), adapter);

        final Party alice = new Party("Alice");
        g.setOwnParty(alice);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //new EventAdapter(getApplicationContext(), adapter);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        listItem = getResources().getStringArray(R.array.array_technology);
        listItemSubtitles = getResources().getStringArray(R.array.array_technology_subtitle);


        listView = findViewById(R.id.chatOverviews);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Chat selectedChat = adapter.getItem(position);

                Intent openChatIntent = new Intent(getBaseContext(), ChatActivity.class);
                openChatIntent.putExtra(getString(R.string.extra_chat), selectedChat);
                startActivity(openChatIntent);
            }
        });

        FloatingActionButton addChatButton = findViewById(R.id.fab);
        addChatButton.setOnClickListener(new CrateNewChatListener(this));
    }
}
