package com.example.daml_chat.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Chat implements Serializable {
    public String contractId;
    public String name;
    public Party[] members;
    public String postContractId;

    public Chat() {}

    public Chat(String contractId, String name, Party[] members) {
        this.contractId = contractId;
        this.name = name;
        this.members = members;
    }

    public JSONObject GidAsJSON() {
        JSONObject gid = new JSONObject();
        JSONObject membersContent = new JSONObject();
        JSONObject textMapContent = new JSONObject();

        try {
            for (Party member : this.members) {
                textMapContent.put(member.name, new JSONObject());
            }

                membersContent.put("textMap", textMapContent);
            gid.put("id", this.name);
            gid.put("members", membersContent);

            return gid;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
