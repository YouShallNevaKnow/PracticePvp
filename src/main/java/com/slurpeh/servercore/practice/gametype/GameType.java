package com.slurpeh.servercore.practice.gametype;

import com.slurpeh.servercore.practice.arena.Arena;
import com.slurpeh.servercore.practice.player.Kit;
import com.slurpeh.servercore.practice.util.PlyInv;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bradley on 4/29/16.
 */
public class GameType {
    private String name;
    private String displayName;
    private Kit startingKit;
    private List<Arena> possibleArenas;
    private ItemStack display;
    private boolean editable;
    private Inventory possibleGear;
    private boolean buildandplace;

    public GameType(final String name) {
        this.name = name;
        this.displayName = name;
        this.startingKit = new Kit(this.displayName + "Default Kit", new PlyInv(new ItemStack[36], new ItemStack[4]));
        this.possibleArenas = new ArrayList<Arena>();
        this.display = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = this.display.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', getDisplayName()));
        this.display.setItemMeta(meta);
        this.editable = false;
        this.buildandplace = false;
        this.possibleGear = Bukkit.createInventory((InventoryHolder) null, 54, ChatColor.stripColor(this.displayName));
    }

    public GameType(final String name, final String displayName, final Kit startingKit, final List<Arena> possibleArenas, final ItemStack display, boolean bp, final boolean editable, final Inventory possibleGear) {
        this.name = name;
        this.displayName = displayName;
        this.startingKit = startingKit;
        this.possibleArenas = possibleArenas;
        this.display = display;
        this.editable = editable;
        this.possibleGear = possibleGear;
        this.buildandplace = bp;
    }

    public boolean isSetup() {
        return this.displayName != null && this.startingKit != null && this.possibleArenas != null && this.display != null;
    }

    public void setPossibleGear(final Inventory inv) {
        (this.possibleGear = Bukkit.createInventory((InventoryHolder)null, 54, ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', this.displayName)))).setContents(inv.getContents());
    }

    public boolean canPlaceAndBreak() {
        return buildandplace;
    }

    public void setPlaceAndBreak(boolean bp) {
        this.buildandplace = bp;
    }

    public Inventory getPossibleGear() {
        final Inventory inv = Bukkit.createInventory((InventoryHolder)null, this.possibleGear.getSize(), this.possibleGear.getName());
        inv.setContents(this.possibleGear.getContents());
        return inv;
    }

    public void setDisplayName(final String string) {
        this.displayName = string;
        this.setPossibleGear(this.getPossibleGear());
    }

    public String getDisplayNameColorless() {
        return ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', this.getDisplayName()));
    }

    public String getName() {
        return this.name;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public Kit getStartingKit() {
        return this.startingKit;
    }

    public void setStartingKit(final Kit startingKit) {
        this.startingKit = startingKit;
    }

    public List<Arena> getPossibleArenas() {
        return this.possibleArenas;
    }

    public void setPossibleArenas(final List<Arena> possibleArenas) {
        this.possibleArenas = possibleArenas;
    }

    public ItemStack getDisplay() {
        return this.display;
    }

    public void setDisplay(final ItemStack display) {
        this.display = display;
    }

    public boolean isEditable() {
        return this.editable;
    }

    public void setEditable(final boolean editable) {
        this.editable = editable;
    }
}
