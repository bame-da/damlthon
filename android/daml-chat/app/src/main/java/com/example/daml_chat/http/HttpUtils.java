package com.example.daml_chat.http;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.HttpEntity;

public class HttpUtils {
    private static final String BASE_URL = "http://10.0.2.2:7575/";

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.addHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJsZWRnZXJJZCI6ImZvb2JhciIsImFwcGxpY2F0aW9uSWQiOiJmb29iYXIiLCJwYXJ0eSI6IkFsaWNlIn0.aQl0EM6nm9JAJG5MVCSMYh1epEr_LGd-_4R8UmfXCuc");
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(Context context, String url, HttpEntity entity, AsyncHttpResponseHandler responseHandler) {
        client.addHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJsZWRnZXJJZCI6ImZvb2JhciIsImFwcGxpY2F0aW9uSWQiOiJmb29iYXIiLCJwYXJ0eSI6IkFsaWNlIn0.aQl0EM6nm9JAJG5MVCSMYh1epEr_LGd-_4R8UmfXCuc");
        client.post(context, getAbsoluteUrl(url), entity, "application/json", responseHandler);
    }

    public static void getByUrl(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(url, params, responseHandler);
    }

    public static void postByUrl(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(url, params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}
