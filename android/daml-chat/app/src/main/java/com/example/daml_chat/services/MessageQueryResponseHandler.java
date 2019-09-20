package com.example.daml_chat.services;

import com.example.daml_chat.MessageAdapter;
import com.example.daml_chat.Utils.MessageDateSorter;
import com.example.daml_chat.models.Chat;
import com.example.daml_chat.models.Message;
import com.example.daml_chat.models.Party;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class MessageQueryResponseHandler  extends AsyncHttpResponseHandler {

    MessageAdapter adapter;
    Chat parentChat;

    public MessageQueryResponseHandler(MessageAdapter adapter, Chat parentChat) {
        this.adapter = adapter;
        this.parentChat = parentChat;
        setUsePoolThread(true);
        setUseSynchronousMode(false);
    }

    @Override
    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
        try {
            List<Message> messages = new ArrayList<>();
            JSONObject response = new JSONObject(new String(responseBody));
            JSONArray queryResults = response.getJSONArray("result");

            // Result
            for (int resCounter = 0; resCounter < queryResults.length(); resCounter++) {
                JSONArray activeContracts = queryResults.getJSONObject(resCounter).getJSONArray("activeContracts");

                // Active Contracts
                for (int acCounter = 0; acCounter < activeContracts.length(); acCounter++) {
                    JSONObject argument = activeContracts.getJSONObject(acCounter).getJSONObject("argument");
                    org.json.JSONObject mid = argument.getJSONObject("mid");
                    JSONObject gid = mid.getJSONObject("gid");

                    JSONObject parentGid = parentChat.GidAsJSON();
                    if (!gid.toString().equals(parentGid.toString())) {
                        continue;
                    }

                    String text = argument.getString("text");
                    Instant postedAt = Instant.parse(argument.getString("postedAt"));
                    Party poster = new Party(mid.getString("poster"));

                    messages.add(new Message(parentChat, postedAt, text, poster, null));

                }
            }

            messages.sort(new MessageDateSorter());
            adapter.addAll(messages);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
    }
}
