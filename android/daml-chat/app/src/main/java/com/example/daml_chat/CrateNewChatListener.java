package com.example.daml_chat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.example.daml_chat.services.ChatService;

class CrateNewChatListener implements View.OnClickListener {
    Activity activity;
    EditText chatName;
    ChatAdapter adapter;

    public CrateNewChatListener(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onClick(View v) {
        final AlertDialog.Builder chatNameDialog = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View chatNameDialogView = inflater.inflate(R.layout.dialog_chat_name, null);

        chatNameDialog.setView(chatNameDialogView)
            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ChatService.CreateNewChat(activity, adapter, chatName.getText().toString());
                }
            })
            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            })
            .setTitle("New Chat").setMessage("Name: ");


        chatNameDialog.create();
        chatName = chatNameDialogView.findViewById(R.id.chatName);
        chatNameDialog.show();
    }
}
