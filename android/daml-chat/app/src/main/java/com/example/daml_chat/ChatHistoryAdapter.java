package com.example.daml_chat;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.daml_chat.core.Globals;
import com.example.daml_chat.models.Message;
import com.example.daml_chat.models.Party;

import java.util.List;

public class ChatHistoryAdapter extends ArrayAdapter<Message> {
    Party ownParty = Globals.getInstance().getOwnParty();

    Activity context;
    List<Message> messages;

    public ChatHistoryAdapter(@NonNull Activity context, List<Message> messages) {
        super(context, R.layout.content_chat, messages);

        this.context = context;
        this.messages = messages;
    }

    public View getView(int position, View view, ViewGroup parent) {
        Message message = messages.get(position);
        boolean isMyMessage = message.poster.equals(ownParty);

        LayoutInflater inflater = context.getLayoutInflater();

        View messageView =
            inflater.inflate(
                    (isMyMessage)? R.layout.my_message : R.layout.their_message,
                    null,
                    true);


        if (!isMyMessage) {
            TextView nameTag = messageView.findViewById(R.id.name);
            nameTag.setText(message.poster.name);
        }

        TextView messageTextView = messageView.findViewById(R.id.message_body);
        messageTextView.setText(message.text);

        return messageView;
    }

    public void add(Message message) {
        this.messages.add(message);
        notifyDataSetChanged();
    }
}
