package com.example.daml_chat.services;

import android.content.Context;

import com.example.daml_chat.ChatAdapter;
import com.example.daml_chat.core.Globals;
import com.example.daml_chat.http.HttpUtils;
import com.example.daml_chat.models.Chat;

import org.json.JSONArray;
import org.json.JSONObject;


import cz.msebera.android.httpclient.entity.StringEntity;

public class ChatService {
    static public void GetChatGroups(Context context, ChatAdapter adapter) {
        try {
            JSONObject jsonGroupFilter = new JSONObject();
            jsonGroupFilter.put("moduleName", "GroupChat");
            jsonGroupFilter.put("entityName", "ChatGroup");

            JSONObject jsonMessageIdxFilter = new JSONObject();
            jsonMessageIdxFilter.put("moduleName", "GroupChat");
            jsonMessageIdxFilter.put("entityName", "MessageIndex");

            JSONArray jsonFilterArray = new JSONArray();
            jsonFilterArray.put(jsonGroupFilter);
            jsonFilterArray.put(jsonMessageIdxFilter);

            JSONObject jsonQueryParams = new JSONObject();
            jsonQueryParams.put("%templates", jsonFilterArray);

            StringEntity entity = new StringEntity(jsonQueryParams.toString());
            HttpUtils.post(context,"contracts/search", entity, new ChatQueryResponseHandler(adapter));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void CreateNewChat(Context context, ChatAdapter adapter, String chatName) {
        try {
            JSONObject createChat = new JSONObject();
            JSONObject templateId = new JSONObject();
            JSONObject argument = new JSONObject();

            argument.put("id", chatName);
            argument.put("creator", Globals.getInstance().getOwnParty().name);

            templateId.put("moduleName", "GroupChat");
            templateId.put("entityName", "GroupSetup");

            createChat.put("templateId", templateId);
            createChat.put("argument", argument);

            StringEntity entity = new StringEntity(createChat.toString());
            HttpUtils.post(context,"command/create", entity, new ChatSetupResponseHandler(context, adapter));

        } catch (Exception ex) {

        }
    }

    public static void LeaveChat(Context context, Chat parentChat) {
        try {
        }
        catch (Exception e) {

        }
    }
}
