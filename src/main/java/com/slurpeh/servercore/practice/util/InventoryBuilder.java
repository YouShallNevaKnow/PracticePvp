package com.slurpeh.servercore.practice.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LightningStrike;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;

/**
 * Created by Bradley on 4/29/16.
 */
public class InventoryBuilder {
    private Inventory inv;

    public InventoryBuilder(int size, String title, boolean useColors, boolean playerInventory) {
        if (!playerInventory) {
            inv = Bukkit.createInventory((InventoryHolder) null, size, useColors ? ChatColor.translateAlternateColorCodes('&', title) : title);
        } else {
            inv = (PlayerInventory)Bukkit.createInventory(null, size, useColors ? ChatColor.translateAlternateColorCodes('&', title) : title);
        }
    }

    public InventoryBuilder withViewer(HumanEntity viewer) {
        inv.getViewers().add(viewer);
        return this;
    }

    public InventoryBuilder withItem(int slot, ItemStack stack) {
        inv.setItem(slot, stack);
        return this;
    }

    public InventoryBuilder withMaxStackSize(int size) {
        inv.setMaxStackSize(size);
        return this;
    }

    public InventoryBuilder withContents(ItemStack[] items) {
        inv.setContents(items);
        return this;
    }

    public InventoryBuilder withContents(List<ItemStack> items) {
        inv.setContents((ItemStack[])items.toArray(new ItemStack[inv.getSize() - 1]));
        return this;
    }

    public InventoryBuilder withArmorContents(ItemStack[] items) {
        if (inv instanceof PlayerInventory) {
            ((PlayerInventory)inv).setArmorContents(items);
        } else {
            throw new IllegalStateException("cant be done");
        }
        return this;
    }

    public InventoryBuilder withHelmet(ItemStack stack) {
        if (inv instanceof PlayerInventory) {
            ((PlayerInventory)inv).setHelmet(stack);
        } else {
            throw new IllegalStateException("cant be done");
        }
        return this;
    }

    public InventoryBuilder withChestplate(ItemStack stack) {
        if (inv instanceof PlayerInventory) {
            ((PlayerInventory)inv).setChestplate(stack);
        } else {
            throw new IllegalStateException("cant be done");
        }
        return this;
    }

    public InventoryBuilder withLeggings(ItemStack stack) {
        if (inv instanceof PlayerInventory) {
            ((PlayerInventory)inv).setLeggings(stack);
        } else {
            throw new IllegalStateException("cant be done");
        }
        return this;
    }

    public InventoryBuilder withBoots(ItemStack stack) {
        if (inv instanceof PlayerInventory) {
            ((PlayerInventory)inv).setBoots(stack);
        } else {
            throw new IllegalStateException("cant be done");
        }
        return this;
    }

    public Inventory build() {
        if (inv instanceof PlayerInventory) {
            return (PlayerInventory)inv;
        } else {
            return inv;
        }
    }
}
