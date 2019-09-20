package com.example.daml_chat.services;

import com.example.daml_chat.models.Chat;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

class PostMessageResponseHandler extends AsyncHttpResponseHandler {
    public Chat parentChat;

    public PostMessageResponseHandler(Chat parentChat) {
        this.parentChat = parentChat;
    }

    @Override
    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
        try {
            JSONObject response = new JSONObject(new String(responseBody));
            JSONArray queryResults = response.getJSONArray("result");

            for (int resCounter = 0; resCounter < queryResults.length(); resCounter++) {
                JSONObject activeContract = queryResults.getJSONObject(resCounter);

                String contractType = activeContract.getJSONObject("templateId").getString("entityName");

                if (contractType.equals("MessageIndex")) {
                    String messageIndexCid = activeContract.getString("contractId");
                    parentChat.postContractId = messageIndexCid;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
    }
}
