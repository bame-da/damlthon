package com.example.daml_chat.services;

import android.net.Uri;

import com.daml.ledger.javaapi.data.CreatedEvent;
import com.daml.ledger.javaapi.data.FiltersByParty;
import com.daml.ledger.javaapi.data.LedgerOffset;
import com.daml.ledger.javaapi.data.NoFilter;
import com.daml.ledger.javaapi.data.Record;
import com.daml.ledger.javaapi.data.TransactionFilter;
import com.daml.ledger.rxjava.DamlLedgerClient;
import com.example.daml_chat.core.Globals;
import com.example.daml_chat.http.HttpUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.netty.handler.ssl.SslContext;
import io.reactivex.disposables.Disposable;

public class FileService {
    private static FileService instance = null;
    private Map<String, Uri> files = new HashMap<>();

    private FileService() {
        final DamlLedgerClient client = DamlLedgerClient.forHostWithLedgerIdDiscovery(HttpUtils.getHostIP(), HttpUtils.getGRPCPort(), Optional.<SslContext>empty());
        client.connect();
        TransactionFilter filter = new FiltersByParty(Collections.singletonMap(Globals.getInstance().getOwnParty().name, NoFilter.instance));
        Disposable f = client.getTransactionsClient().getTransactions(LedgerOffset.LedgerBegin.getInstance(), filter , true).subscribe(tx -> {
            tx.getEvents().forEach(ev -> {
                if(ev.getTemplateId().getEntityName().equals("File") && ev instanceof CreatedEvent) {
                    CreatedEvent ce = (CreatedEvent) ev;
                    String hash = ce.getArguments().getFieldsMap().get("hash").asRecord().get().getFieldsMap().get("unpack").asText().get().getValue();
                    String uriStr = ce.getArguments().getFieldsMap().get("uri").asRecord().get().getFieldsMap().get("unpack").asText().get().getValue();
                    String mime = ce.getArguments().getFieldsMap().get("mimeType").asRecord().get().getFieldsMap().get("unpack").asText().get().getValue();
                    Record enc = ce.getArguments().getFieldsMap().get("encryption").asVariant().get().getValue().asRecord().get();
                    String key = enc.getFieldsMap().get("encKey").asText().get().getValue();
                    String iv = enc.getFieldsMap().get("iv").asText().get().getValue();
                    Uri uri = Uri.parse(String.format("%s%s?key=%s&iv=%s&mimetype=%s",HttpUtils.getAttachmentURL(), hash, key, iv, mime));
                    if(!files.containsKey(hash)) files.put(hash, uri);
                }
            });
        });

    }

    public Optional<Uri> getURI (String hash) {
        return Optional.ofNullable(files.get(hash));
    }

    public static FileService getInstance() {
        if(instance == null) instance = new FileService();
        return instance;
    }
}
