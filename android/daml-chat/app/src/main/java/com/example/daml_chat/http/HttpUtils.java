package com.example.daml_chat.http;

import android.content.Context;
import android.util.Base64;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;


import cz.msebera.android.httpclient.HttpEntity;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultJwtBuilder;

public class HttpUtils {
    private static final String BASE_IP = "10.0.2.2";
    private static final int JSON_PORT = 7575;
    private static final int GRPC_PORT = 6865;
    private static final String LEDGER_ID = "foobar";
    private static final String PARTY = "Alice";
    private static final String JWT = Jwts.builder()
            .setPayload(String.format("{\"ledgerId\": \"%s\",\"applicationId\": \"Chat\",\"party\": \"%s\"}",LEDGER_ID, PARTY))
            .setHeaderParam("typ", "JWT")
            .signWith(SignatureAlgorithm.HS256, Base64.encodeToString("secret".getBytes(),Base64.NO_CLOSE))
            .compact();

    private static final String BASE_URL = String.format("http://%s:%s/", BASE_IP, JSON_PORT);

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.removeAllHeaders();
        client.addHeader("Authorization", String.format("Bearer %s", JWT));
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(Context context, String url, HttpEntity entity, AsyncHttpResponseHandler responseHandler) {
        client.removeAllHeaders();
        client.addHeader("Authorization", String.format("Bearer %s", JWT));
        client.post(context, getAbsoluteUrl(url), entity, "application/json", responseHandler);
    }

    public static void getByUrl(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.removeAllHeaders();
        client.addHeader("Authorization", String.format("Bearer %s", JWT));
        client.get(url, params, responseHandler);
    }

    public static void postByUrl(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.removeAllHeaders();
        client.addHeader("Authorization", String.format("Bearer %s", JWT));
        client.post(url, params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }

    public static String getHostIP() {
        return BASE_IP;
    }
    public static int getGRPCPort() {
        return GRPC_PORT;
    }

}
