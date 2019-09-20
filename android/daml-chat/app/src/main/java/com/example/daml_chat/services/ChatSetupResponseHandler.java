package com.example.daml_chat.services;

import android.content.Context;

import com.example.daml_chat.ChatAdapter;
import com.example.daml_chat.core.Globals;
import com.example.daml_chat.http.HttpUtils;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

class ChatSetupResponseHandler extends AsyncHttpResponseHandler {
    ChatAdapter adapter;
    Context context;

    public ChatSetupResponseHandler(Context context, ChatAdapter adapter) {
        this.adapter = adapter;
        this.context = context;
    }

    @Override
    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
        try {
            JSONObject response = new JSONObject(new String(responseBody));
            JSONObject result = response.getJSONObject("result");
            String contractId = result.getString("contractId");

            JSONObject jsonBody = new JSONObject();
            JSONObject jsonTemplateIdBody = new JSONObject();
            JSONObject jsonArgument = new JSONObject();

            jsonTemplateIdBody.put("moduleName", "GroupChat");
            jsonTemplateIdBody.put("entityName", "GroupSetup");

            jsonBody.put("templateId", jsonTemplateIdBody);
            jsonBody.put("contractId", contractId);
            jsonBody.put("choice", "CreateGroup");
            jsonBody.put("argument", new JSONObject());

            StringEntity entity = new StringEntity(jsonBody.toString());
            HttpUtils.post(context,"command/exercise", entity, new NewChatCreatedHandler());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

    }
}
