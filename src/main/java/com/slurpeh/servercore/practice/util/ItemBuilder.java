package com.slurpeh.servercore.practice.util;

import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Collections;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.Material;


public class ItemBuilder {
    private Material type;
    private String name;
    private String lore;
    private int amount;
    private MaterialData data;
    private short durability;

    public ItemBuilder(final Material type, final String name, final String lore) {
        this.type = type;
        this.name = name;
        this.lore = lore;
        this.amount = 1;
        this.data = new MaterialData(type);
    }

    public ItemBuilder(final Material type, final String name, final String lore, final int amount) {
        this.type = type;
        this.name = name;
        this.lore = lore;
        this.amount = amount;
        this.data = new MaterialData(type);
    }

    public ItemBuilder(final Material type, final String name, final String lore, final int amount, final MaterialData data) {
        this.type = type;
        this.name = name;
        this.lore = lore;
        this.amount = amount;
        this.data = data;
    }

    public ItemBuilder(Material type, String name, String lore, int amount, short durability) {
        this.type = type;
        this.name = name;
        this.lore = lore;
        this.amount = amount;
        this.durability = durability;
    }

    public ItemBuilder() {

    }

    public ItemStack getItem() {
        final ItemStack item = new ItemStack(this.type);
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', this.name));
        meta.setLore((List)Collections.singletonList(ChatColor.translateAlternateColorCodes('&', this.lore)));
        item.setItemMeta(meta);
        item.setAmount(this.amount);
        item.setData(this.data);
        item.setDurability(this.durability);
        return item;
    }


    public ItemStack getItem(String s) {
        return new ItemStack(Material.ACTIVATOR_RAIL);
    }
}
