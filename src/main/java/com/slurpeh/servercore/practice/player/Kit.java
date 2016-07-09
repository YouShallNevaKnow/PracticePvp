package com.slurpeh.servercore.practice.player;

import com.slurpeh.servercore.practice.util.MiscUtil;
import com.slurpeh.servercore.practice.util.PlyInv;

/**
 * Created by Bradley on 4/29/16.
 */
public class Kit {
    private String name;
    private PlyInv inv;

    public Kit(final String name, final PlyInv inv) {
        this.name = name;
        this.inv = inv;
    }

    @Override
    public String toString() {
        return this.name + "|" + MiscUtil.playerInventoryToString(this.inv);
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public PlyInv getInv() {
        return this.inv;
    }

    public void setInv(final PlyInv inv) {
        this.inv = inv;
    }
}
