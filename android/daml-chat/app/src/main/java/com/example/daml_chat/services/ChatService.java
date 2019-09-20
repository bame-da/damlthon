package com.example.daml_chat.services;

import android.content.Context;

import com.example.daml_chat.ChatAdapter;
import com.example.daml_chat.http.HttpUtils;

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
}
