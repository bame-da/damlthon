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

public class ChatOverviewListAdapter extends ArrayAdapter<Chat> {
    private final Party ownParty = Globals.getInstance().getOwnParty();
    private final Activity context;
    private final List<Chat> chats;


    public ChatOverviewListAdapter(Activity context, List<Chat> chats) {
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
}
