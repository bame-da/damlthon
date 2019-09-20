package com.example.daml_chat;

import android.content.Context;

import com.daml.ledger.javaapi.data.FiltersByParty;
import com.daml.ledger.javaapi.data.NoFilter;
import com.daml.ledger.javaapi.data.TransactionFilter;
import com.daml.ledger.rxjava.DamlLedgerClient;
import com.example.daml_chat.core.Globals;
import com.example.daml_chat.http.HttpUtils;
import com.example.daml_chat.models.Chat;
import com.example.daml_chat.services.MessageService;

import java.util.Collections;
import java.util.Optional;

import io.netty.handler.ssl.SslContext;
import io.reactivex.disposables.Disposable;

public class EventAdapter {
    public EventAdapter(Context context, MessageAdapter messageAdapter, final Chat parentChat) {
        final DamlLedgerClient client = DamlLedgerClient.forHostWithLedgerIdDiscovery(HttpUtils.getHostIP(), HttpUtils.getGRPCPort(), Optional.<SslContext>empty());
        client.connect();
        Disposable d = client.getTransactionsClient().getLedgerEnd()
                .subscribe(os -> {
                    TransactionFilter filter = new FiltersByParty(Collections.singletonMap(Globals.getInstance().getOwnParty().name, NoFilter.instance));
                    Disposable f = client.getTransactionsClient().getTransactions(os, filter , false).subscribe(tx -> {
                        MessageService.getMessagesFromChat(context, messageAdapter, parentChat);
                    });
                });
        MessageService.getMessagesFromChat(context, messageAdapter, parentChat);
    }
}
