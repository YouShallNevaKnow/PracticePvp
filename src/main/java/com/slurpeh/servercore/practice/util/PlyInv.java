package com.slurpeh.servercore.practice.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class PlyInv {
    private ItemStack[] contents;
    private ItemStack[] armor;

    public PlyInv() {}

    public PlyInv(ItemStack[] contents, ItemStack[] armor) {
        this.contents = contents;
        this.armor = armor;
    }

    public static PlyInv fromPlayerInventory(final PlayerInventory inv) {
        return new PlyInv(inv.getContents(), inv.getArmorContents());
    }

    public ItemStack[] getContents() {
        return this.contents;
    }

    public void setContents(final ItemStack[] contents) {
        this.contents = contents;
    }

    public ItemStack[] getArmorContents() {
        return this.armor;
    }

    public void setArmorContents(final ItemStack[] armorContents) {
        this.armor = armorContents;
    }
}
