package com.slurpeh.servercore.practice.inventory;

/**
 * Created by Bradley on 5/4/16.
 */
public enum KitEditorInventoryType {
    KIT_LISTER("kit_lister"),
    EDITABLE_GAMETYPES("editable_gametypes");

    private String name;

    private KitEditorInventoryType(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
