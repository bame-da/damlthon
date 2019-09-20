package com.example.daml_chat.services;

import android.content.Context;

import com.example.daml_chat.MessageAdapter;
import com.example.daml_chat.core.Globals;
import com.example.daml_chat.http.HttpUtils;
import com.example.daml_chat.models.Chat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.UUID;

import cz.msebera.android.httpclient.entity.StringEntity;

public class MessageService {
    static public void getMessagesFromChat(Context context, MessageAdapter adapter, Chat parentChat) {
        try {
            JSONObject jsonQueryTemplateParams = new JSONObject();
            jsonQueryTemplateParams.put("moduleName", "GroupChat");
            jsonQueryTemplateParams.put("entityName", "Message");

            JSONArray jsonArrayQueryTemplateParams = new JSONArray();
            jsonArrayQueryTemplateParams.put(jsonQueryTemplateParams);

            JSONObject jsonQueryParams = new JSONObject();
            jsonQueryParams.put("%templates", jsonArrayQueryTemplateParams);

            StringEntity entity = new StringEntity(jsonQueryParams.toString());
            HttpUtils.post(context,"contracts/search", entity, new MessageQueryResponseHandler(adapter, parentChat));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static public void postMessage(Context context, Chat parentChat, String message) {
        try {
            JSONObject jsonBody = new JSONObject();
            JSONObject jsonTemplateIdBody = new JSONObject();
            JSONObject jsonArgument = new JSONObject();

            jsonTemplateIdBody.put("moduleName", "GroupChat");
            jsonTemplateIdBody.put("entityName", "ChatGroup");

            jsonArgument.put("poster", Globals.getInstance().getOwnParty().name);
            jsonArgument.put("micid", parentChat.postContractId);
            jsonArgument.put("id", UUID.randomUUID().toString());
            jsonArgument.put("text", message);


            jsonBody.put("templateId", jsonTemplateIdBody);
            jsonBody.put("contractId", parentChat.contractId);
            jsonBody.put("choice", "Post_Message");
            jsonBody.put("argument", jsonArgument);

            StringEntity entity = new StringEntity(jsonBody.toString());
            HttpUtils.post(context,"command/exercise", entity, new PostMessageResponseHandler(parentChat));
        }
        catch (Exception e) {

        }
    }
}
