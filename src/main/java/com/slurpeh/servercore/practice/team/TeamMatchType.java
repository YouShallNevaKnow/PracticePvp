package com.slurpeh.servercore.practice.team;

/**
 * Created by Bradley on 5/11/16.
 */
public enum TeamMatchType {
    FFA("ffa"),
    PARTYvPARTY("partyvparty"),
    TEAMSPLIT("teamsplit");

    private String name;

    TeamMatchType(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
