package com.example.daml_chat.services;

import com.example.daml_chat.ChatAdapter;
import com.example.daml_chat.core.Globals;
import com.example.daml_chat.models.Chat;
import com.example.daml_chat.models.Party;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class ChatQueryResponseHandler extends AsyncHttpResponseHandler {

    ChatAdapter adapter;
    Chat parentChat;
    int numMembers;

    public ChatQueryResponseHandler(ChatAdapter adapter) {
        this.adapter = adapter;
        this.parentChat = parentChat;
        this.numMembers = numMembers;
    }

    @Override
    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
        List<Chat> chats = new ArrayList<>();
        Map<String, String> gidToIndexCid = new HashMap<>();
        try {
            JSONObject response = new JSONObject(new String(responseBody));
            JSONArray queryResults = response.getJSONArray("result");

            for (int resCounter = 0; resCounter < queryResults.length(); resCounter++) {
                JSONArray activeContracts = queryResults.getJSONObject(resCounter).getJSONArray("activeContracts");

                for (int acCounter = 0; acCounter < activeContracts.length(); acCounter++) {
                    JSONObject activeContract = activeContracts.getJSONObject(acCounter);
                    String contractType = activeContract.getJSONObject("templateId").getString("entityName");

                    if (contractType.equals("ChatGroup")) {
                        chats.add(processChatGroupJson(activeContract));
                    }
                    else if (contractType.equals("MessageIndex")) {
                        processMessageIdx(gidToIndexCid, activeContract);
                    }
                }
            }

            updateChatWithIndexCid(chats, gidToIndexCid);
            adapter.addAll(chats);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateChatWithIndexCid(List<Chat> chats, Map<String, String> gidToIndexCid) {
        for (Chat chat : chats) {
            chat.postContractId = gidToIndexCid.get(chat.GidAsJSON().toString());
        }
    }

    private void processMessageIdx(Map<String, String> gidToIndexCid, JSONObject activeContract) throws JSONException {
        JSONObject gid = activeContract.getJSONObject("argument").getJSONObject("gid");
        String contractId = activeContract.getString("contractId");

        String poster = activeContract.getJSONObject("argument").getString("poster");
        if (Globals.getInstance().getOwnParty().name.equals(poster)) {
            gidToIndexCid.put(gid.toString(), contractId);
        }
    }

    private Chat processChatGroupJson(JSONObject activeContract) throws JSONException {
        JSONObject gid = activeContract.getJSONObject("argument").getJSONObject("gid");
        String contractId = activeContract.getString("contractId");
        String chatName = gid.getString("id");


        Iterator<String> memberList = gid.getJSONObject("members").getJSONObject("textMap").keys();
        List<Party> members = new ArrayList<Party>();

        while (memberList.hasNext()) {
            members.add(new Party(memberList.next()));
        }

        Chat newChat = new Chat(contractId, chatName, members);
        return newChat;
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
    }
}
