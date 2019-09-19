package com.example.daml_chat;

import android.content.Intent;
import android.os.Bundle;

import com.example.daml_chat.core.Globals;
import com.example.daml_chat.models.Chat;
import com.example.daml_chat.models.Party;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.provider.Telephony;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ListView listView;
    String[] listItem;
    String[] listItemSubtitles;
    ChatOverviewListAdapter adapter;
    Globals g = Globals.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Init Data

        final Party bernhard = new Party("Bernhard");
        final Party jussi = new Party("Jussi");
        final Party soren = new Party("Soren");
        final Party darko = new Party("Darko");
        g.setOwnParty(darko);

        List<Chat> chats = new ArrayList<>();
        chats.add(new Chat("Awesome Chat1", new Party[]{darko, jussi}));
        chats.add(new Chat("Uiiii", new Party[]{darko, bernhard}));
        chats.add(new Chat("Serious Business", new Party[]{darko, soren}));
        chats.add(new Chat("Group Chat1", new Party[]{darko, jussi, soren, bernhard}));



        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        listItem = getResources().getStringArray(R.array.array_technology);
        listItemSubtitles = getResources().getStringArray(R.array.array_technology_subtitle);

        adapter = new ChatOverviewListAdapter(this, chats);

        listView = findViewById(R.id.chatOverviews);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Chat selectedChat = adapter.getItem(position);
                Toast.makeText(getApplicationContext(), selectedChat.name, Toast.LENGTH_SHORT).show();

                Intent openChatIntent = new Intent(getBaseContext(), ChatActivity.class);
                openChatIntent.putExtra(getString(R.string.extra_chat), selectedChat);
                startActivity(openChatIntent);
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
