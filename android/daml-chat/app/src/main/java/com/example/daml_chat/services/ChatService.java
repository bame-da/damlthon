package com.example.daml_chat.services;

import android.content.Context;

import com.example.daml_chat.ChatHistoryAdapter;
import com.example.daml_chat.MyResposeHandler;
import com.example.daml_chat.http.HttpUtils;
import com.example.daml_chat.models.Chat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.entity.StringEntity;

public class ChatService {
    static public void GetChatGroups(Context context, ChatHistoryAdapter adapter, Chat parentChat, int numMembers) throws JSONException, UnsupportedEncodingException {

        JSONObject jsonQueryTemplateParams = new JSONObject();
        jsonQueryTemplateParams.put("moduleName", "GroupChat");
        jsonQueryTemplateParams.put("entityName", "ChatGroup");

        JSONArray jsonArrayQueryTemplateParams = new JSONArray();
        jsonArrayQueryTemplateParams.put(jsonQueryTemplateParams);

        JSONObject jsonQueryParams = new JSONObject();
        jsonQueryParams.put("%templates", jsonArrayQueryTemplateParams);

        StringEntity entity = new StringEntity(jsonQueryParams.toString());

        HttpUtils.post(context,"contracts/search", entity, new MyResposeHandler(adapter, parentChat, numMembers));
    }
}
