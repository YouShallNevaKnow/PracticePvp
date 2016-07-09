package com.slurpeh.servercore.practice.util;

import com.slurpeh.servercore.practice.KohiPractice;
import com.slurpeh.servercore.practice.inventory.InventoryType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.permissions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Bradley on 4/30/16.
 */
public class UtilityManager {
    List<String> awaitingPermissionClear;
    List<UUID> staff;
    List<String> awaitingPermission;
    List<String> awaitingInvClear;
    KohiPractice plugin;

    public UtilityManager(KohiPractice plugin) {
        this.awaitingInvClear = new ArrayList<>();
        this.plugin = plugin;
        this.staff = new ArrayList<>();
        if (plugin.getConfig().getConfigurationSection("staff") != null) {
            plugin.getConfig().getConfigurationSection("staff").getKeys(false).forEach(id -> {
                if (Bukkit.getPlayer(id) != null) {
                    Player ply = Bukkit.getPlayer(id);
                    plugin.getInventorySetter().setupInventory(InventoryType.STAFF, ply);
                    staff.add(UUID.fromString(id));
                } else if (Bukkit.getOfflinePlayer(id) != null) {
                    staff.add(UUID.fromString(id));
                }
            });
        }
        if (plugin.getConfig().getConfigurationSection("awaitingInventoryClear") != null) {
            for (String ign : plugin.getConfig().getConfigurationSection("awaitingInventoryClear").getKeys(false)) {
                awaitingInvClear.add(ign);
            }
        }
        if (plugin.getConfig().getConfigurationSection("awaitingPermission") != null) {
            for (String ign : plugin.getConfig().getConfigurationSection("awaitingPermission").getKeys(false)) {
                awaitingPermission.add(ign);
            }
        }
        if (plugin.getConfig().getConfigurationSection("awaitingPermissionClear") != null) {
            for (String ign: plugin.getConfig().getConfigurationSection("awaitingPermissionClear").getKeys(false)) {
                awaitingPermissionClear.add(ign);
            }
        }
        return;
    }

    public void addStaff(String inGameName, String adder) {
        if (Bukkit.getPlayer(inGameName) != null) {
            Player newStaff = Bukkit.getPlayer(inGameName);
            UUID id = newStaff.getUniqueId();
            staff.add(id);
            plugin.getInventorySetter().setupInventory(InventoryType.STAFF, newStaff);
            saveStaff();
            plugin.getLogger().fine(inGameName + " HAS BEEN ADDED TO THE STAFF LIST BY " + adder);
            PermissionAttachment perm = new PermissibleBase(Bukkit.getConsoleSender()).addAttachment(plugin, plugin.getConfig().getString("staffPermission"), true);
            PermissionAttachmentInfo info = new PermissionAttachmentInfo(perm.getPermissible(), plugin.getConfig().getString("staffPermission"), perm, true);
            newStaff.getEffectivePermissions().add(info);
            return;
        } else if (Bukkit.getOfflinePlayer(inGameName) != null) {
            OfflinePlayer newStaff = Bukkit.getOfflinePlayer(inGameName);
            staff.add(Bukkit.getOfflinePlayer(inGameName).getUniqueId());
            plugin.getLogger().fine(inGameName + " HAS BEEN ADDED TO THE STAFF LIST BY " + adder);
            addToAwaitingPermissionList(newStaff.getName());
            return;
        } else {
            return;
        }
    }

    public void removeStaff(String inGameName, String remover) {
        if (Bukkit.getPlayer(inGameName) != null) {
            staff.remove(Bukkit.getPlayer(inGameName).getUniqueId());
            plugin.getInventorySetter().setupInventory(InventoryType.DEFAULT, Bukkit.getPlayer(inGameName));
            plugin.getLogger().fine(inGameName + " HAS BEEN REMOVED FROM THE STAFF LIST BY " + remover);
            Bukkit.getPlayer(inGameName).getEffectivePermissions().forEach(permissionAttachmentInfo -> {
                if (permissionAttachmentInfo.getPermission().equalsIgnoreCase(plugin.getConfig().getString("staffPermission"))) {
                    Bukkit.getPlayer(inGameName).getEffectivePermissions().remove(permissionAttachmentInfo);
                    return;
                } else {
                    return;
                }
            });
            return;
        } else if (Bukkit.getOfflinePlayer(inGameName) != null) {
            staff.remove(Bukkit.getOfflinePlayer(inGameName).getUniqueId());
            addToAwaitingList(inGameName);
            plugin.getLogger().fine(inGameName + " HAS BEEN REMOVED FROM THE STAFF LIST BY " + remover);
            addToAwaitingPermissionClearList(inGameName);
            return;
        } else {
            return;
        }
    }

    public void saveStaff() {
        plugin.getConfig().set("awaitingPermissionClear", null);
        plugin.getConfig().set("awaitingPermissionClear", awaitingPermissionClear);
        plugin.getConfig().set("awaitingPermission", null);
        plugin.getConfig().set("awaitingPermission", awaitingPermission);
        plugin.getConfig().set("staff", null);
        plugin.getConfig().set("staff", staff);
        plugin.getConfig().set("awaitingInventoryClear", null);
        plugin.getConfig().set("awaitingInventoryClear", awaitingInvClear);
        plugin.saveConfig();
    }

    public List<UUID> getStaff() {
        return this.staff;
    }

    public boolean isStaff(UUID id) {
        return staff.contains(id);
    }

    public void addToAwaitingPermissionClearList(String name) {
        this.awaitingPermissionClear.add(name);
        saveStaff();
        return;
    }

    public void addToAwaitingList(String name) {
        this.awaitingInvClear.add(name);
        saveStaff();
        return;
    }

    public void addToAwaitingPermissionList(String name) {
        if (isAwaitingPermission(name)) {
            this.awaitingPermission.add(name);
            saveStaff();
            return;
        }
    }

    public void removeFromAwaitingPermissionList(String name) {
        this.awaitingPermission.remove(name);
        saveStaff();
        return;
    }

    public void removeFromAwaitingList(String name) {
        if (isAwaitingInventoryClear(name)) {
            this.awaitingInvClear.remove(name);
            saveStaff();
            return;
        }
    }

    public void removeFromAwaitingPermissionClearList(String name) {
        if (isAwaitingPermissionClear(name)) {
            this.awaitingPermissionClear.remove(name);
            saveStaff();
            return;
        }
    }

    public List<String> getAwaitingPermissionClear() {
        return this.awaitingPermissionClear;
    }

    public List<String> getAwaitingInventoryClear() {
        return this.awaitingInvClear;
    }

    public List<String> getAwaitingPermission() {
        return this.awaitingPermission;
    }

    public boolean isAwaitingInventoryClear(String ign) {
        return getAwaitingInventoryClear().contains(ign);
    }

    public boolean isAwaitingPermissionClear(String name) {
        return getAwaitingPermissionClear().contains(name);
    }

    public boolean isAwaitingPermission(String name) {
        return getAwaitingPermission().contains(name);
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent e) {
        if (isAwaitingInventoryClear(e.getPlayer().getName())) {
            e.getPlayer().getInventory().clear();
            removeFromAwaitingList(e.getPlayer().getName());
            plugin.getInventorySetter().setupInventory(InventoryType.DEFAULT, e.getPlayer());
        }
        if (isAwaitingPermission(e.getPlayer().getName())) {
            PermissionAttachment perm = new PermissibleBase(Bukkit.getConsoleSender()).addAttachment(plugin, plugin.getConfig().getString("staffPermission"), true);
            PermissionAttachmentInfo info = new PermissionAttachmentInfo(perm.getPermissible(), plugin.getConfig().getString("staffPermission"), perm, true);
            e.getPlayer().getEffectivePermissions().add(info);
            removeFromAwaitingPermissionList(e.getPlayer().getName());
        }
        if (isAwaitingPermissionClear(e.getPlayer().getName())) {
            e.getPlayer().getEffectivePermissions().forEach(perm -> {
                if (perm.getPermission().equalsIgnoreCase(plugin.getConfig().getString("staffPermission"))) {
                    e.getPlayer().getEffectivePermissions().remove(perm);
                    return;
                }
            });
            removeFromAwaitingPermissionClearList(e.getPlayer().getName());
        }
    }
}
