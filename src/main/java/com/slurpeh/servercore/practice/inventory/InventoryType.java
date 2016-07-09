package com.slurpeh.servercore.practice.inventory;

import org.bukkit.inventory.Inventory;

/**
 * Created by Bradley on 4/30/16.
 */
public enum InventoryType {
    DEFAULT("default"),
    PARTY_LEADER("party_leader"),
    PARTY_MEMBER("party_member"),
    STAFF("staff");

    private String name;

    InventoryType(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
