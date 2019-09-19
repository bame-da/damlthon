package com.example.daml_chat.core;

import com.example.daml_chat.models.Party;

// Singleton Class holding global variables
public class Globals{

    ////////// Global Vars //////////
    private Party ownParty;


    /// Global Vars getter/setter ///
    public void setOwnParty(Party ownParty) {
        this.ownParty = ownParty;
    }
    public Party getOwnParty() {
        return this.ownParty;
    }


    /// Singleton implementation ///
    private static Globals instance;

    private Globals() {}

    public static synchronized Globals getInstance() {
        if (instance == null) {
            instance = new Globals();
        }

        return instance;
    }
}