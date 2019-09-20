package com.example.daml_chat.services;

import com.loopj.android.http.AsyncHttpResponseHandler;

import cz.msebera.android.httpclient.Header;

class PostMessageResponseHandler extends AsyncHttpResponseHandler {
    @Override
    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
        String test = new String(responseBody);
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
        String test = new String(responseBody);
    }
}
