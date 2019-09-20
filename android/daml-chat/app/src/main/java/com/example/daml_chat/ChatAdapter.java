package com.example.daml_chat;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.daml_chat.core.Globals;
import com.example.daml_chat.models.Chat;
import com.example.daml_chat.models.Party;

import java.util.List;
import java.util.StringJoiner;

public class ChatAdapter extends ArrayAdapter<Chat> {
    private final Party ownParty = Globals.getInstance().getOwnParty();
    private final Activity context;
    private final List<Chat> chats;


    public ChatAdapter(Activity context, List<Chat> chats) {
        super(context, R.layout.chat_overview, chats);


        this.context = context;
        this.chats = chats;
    }

    public View getView(int position, View view, ViewGroup parent) {
        Chat chat = chats.get(position);

        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.chat_overview, null, true);

        TextView titleText = rowView.findViewById(R.id.title);
        TextView subTitleText = rowView.findViewById(R.id.subTitle);

        StringJoiner stringJoiner = new StringJoiner(", ");

        for (Party chatMember : chat.members) {
            if (chatMember == ownParty) { continue; }
            stringJoiner.add(chatMember.name);
        }

        titleText.setText(chat.name);
        subTitleText.setText(stringJoiner.toString());

        return rowView;
    }

    public void addAll(List<Chat> chats) {
        this.chats.clear();
        this.chats.addAll(chats);
        context.runOnUiThread(this::notifyDataSetChanged);
    }

    public void add(Chat newChat) {
        this.chats.add(newChat);
        context.runOnUiThread(this::notifyDataSetChanged);
    }

    public void updateChat(Chat chat) {
        for (Chat oldChat: chats) {
            if (!oldChat.GidAsJSON().toString().equals(chat.GidAsJSON().toString())) {
                continue;
            }

            oldChat.postContractId = chat.postContractId;
            oldChat.contractId = chat.contractId;
            oldChat.members = chat.members;
            oldChat.name = chat.name;
        }
    }
}
