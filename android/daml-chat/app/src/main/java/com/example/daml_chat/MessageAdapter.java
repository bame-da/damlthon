package com.example.daml_chat;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.daml_chat.core.Globals;
import com.example.daml_chat.models.Message;
import com.example.daml_chat.models.Party;
import com.example.daml_chat.services.FileService;
import com.google.common.collect.Lists;

import java.util.List;

public class MessageAdapter extends ArrayAdapter<Message> {
    Party ownParty = Globals.getInstance().getOwnParty();

    Activity context;
    List<Message> messages;

    public MessageAdapter(@NonNull Activity context, List<Message> messages) {
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

        if(message.hash.isPresent()) {
            ImageView imageView = messageView.findViewById(R.id.imageView);
            if(FileService.getInstance().getURI(message.hash.get()).isPresent()) {
                Glide.with(context).load(FileService.getInstance().getURI(message.hash.get()).get()).into(imageView);
                imageView.setMaxHeight(336);
            }
        }

        TextView messageTextView = messageView.findViewById(R.id.message_body);
        messageTextView.setText(message.text);

        return messageView;
    }

    public void addAll(List<Message> messages) {
        this.messages.clear();
        this.messages.addAll(messages);
        context.runOnUiThread(this::notifyDataSetChanged);
    }

    public void add(Message message) {
        this.messages.add(message);
        context.runOnUiThread(this::notifyDataSetChanged);
    }
}
