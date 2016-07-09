package com.slurpeh.servercore.practice.twovtwos;

import com.slurpeh.servercore.practice.KohiPractice;
import com.slurpeh.servercore.practice.gametype.GameType;
import com.slurpeh.servercore.practice.inventory.UnrankedInventory;
import com.slurpeh.servercore.practice.util.InventoryBuilder;
import com.slurpeh.servercore.practice.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bradley on 6/9/16.
 */
public class UnrankedInventory2v2 implements Listener {
    KohiPractice plugin;
    private InventoryBuilder builder;
    private Inventory inv;
    private List<Player> viewing;

    public UnrankedInventory2v2(KohiPractice plugin) {
        this.plugin = plugin;
        this.viewing = new ArrayList<>();
        this.builder = new InventoryBuilder(getGameTypeSize(), "&9Select a Un-Ranked 2v2 Queue", true, false);
        int i = 0;
        for (GameType gt : plugin.getGameTypeManager().getGameTypes()) {
            if (gt.isSetup()) {
                ItemStack dis = gt.getDisplay().clone();
                ItemMeta dism = dis.getItemMeta();
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.YELLOW + "In queue: " + ChatColor.GREEN + plugin.get2v2MatchManager().getAmountInQueue(gt));
                lore.add(ChatColor.YELLOW + "In fights: " + ChatColor.GREEN + plugin.get2v2MatchManager().getAmountInMatch(gt));
                dism.setLore(lore);
                dism.setDisplayName(ChatColor.BLUE + gt.getName());
                dis.setItemMeta(dism);
                dis.setAmount(plugin.get2v2MatchManager().getAmountInMatch(gt));
                this.builder.withItem(i, dis);
                i++;
            }
        }
        this.inv = builder.build();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public UnrankedInventory2v2(Player ply) {
        this.plugin = KohiPractice.getInstance();
        this.viewing = new ArrayList<>();
        this.builder = new InventoryBuilder(getGameTypeSize(), "&9Select a Un-Ranked 2v2 Queue", true, false);
        int i = 0;
        for (GameType gt : plugin.getGameTypeManager().getGameTypes()) {
            if (gt.isSetup()) {
                ItemStack dis = gt.getDisplay().clone();
                ItemMeta dism = dis.getItemMeta();
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.YELLOW + "In queue: " + ChatColor.GREEN + plugin.get2v2MatchManager().getAmountInQueue(gt));
                lore.add(ChatColor.YELLOW + "In fights: " + ChatColor.GREEN + plugin.get2v2MatchManager().getAmountInMatch(gt));
                dism.setLore(lore);
                dism.setDisplayName(ChatColor.BLUE + gt.getName());
                dis.setItemMeta(dism);
                dis.setAmount(plugin.get2v2MatchManager().getAmountInMatch(gt));
                this.builder.withItem(i, dis);
                i++;
            }
        }
        this.inv = builder.build();
        ply.openInventory(inv);
        ply.updateInventory();
        viewing.add(ply);
    }

    public void openInventory(Player ply) {
        ply.openInventory(this.inv);
        ply.updateInventory();
        viewing.add(ply);
    }

    public List<Player> getViewers() {
        return viewing;
    }

    public void update() {
        this.builder = new InventoryBuilder(getGameTypeSize(), "&9Select a Un-Ranked 2v2 Queue", true, false);
        int i = 0;
        for (GameType gt : plugin.getGameTypeManager().getGameTypes()) {
            if (gt.isSetup()) {
                ItemStack dis = gt.getDisplay().clone();
                ItemMeta dism = dis.getItemMeta();
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.YELLOW + "In queue: " + ChatColor.GREEN + plugin.get2v2MatchManager().getAmountInQueue(gt));
                lore.add(ChatColor.YELLOW + "In fights: " + ChatColor.GREEN + plugin.get2v2MatchManager().getAmountInMatch(gt));
                dism.setLore(lore);
                dism.setDisplayName(ChatColor.BLUE + gt.getName());
                dis.setItemMeta(dism);
                dis.setAmount(plugin.get2v2MatchManager().getAmountInMatch(gt));
                this.builder.withItem(i, dis);
                i++;
            }
        }
        this.inv = builder.build();
        int iz = 0;
        for (ItemStack stack : inv.getContents()) {
            if (stack != null && stack.hasItemMeta()) {
                for (GameType gt : plugin.getGameTypeManager().getGameTypes()) {
                    ItemStack sn = stack.clone();
                    ItemMeta sn2 = stack.getItemMeta();
                    sn2.setDisplayName(ChatColor.BLUE + ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', sn2.getDisplayName())));
                    sn.setItemMeta(sn2);
                    if (gt.getDisplay().isSimilar(sn)) {
                        ItemStack dis = gt.getDisplay().clone();
                        ItemMeta dism = dis.getItemMeta();
                        List<String> lore = new ArrayList<>();
                        lore.add(ChatColor.YELLOW + "In queue: " + ChatColor.GREEN + plugin.get2v2MatchManager().getAmountInQueue(gt));
                        lore.add(ChatColor.YELLOW + "In fights: " + ChatColor.GREEN + plugin.get2v2MatchManager().getAmountInMatch(gt));
                        dism.setLore(lore);
                        dism.setDisplayName(ChatColor.BLUE + gt.getName());
                        dis.setItemMeta(dism);
                        dis.setAmount(plugin.get2v2MatchManager().getAmountInMatch(gt));
                        this.builder.withItem(iz, dis);
                        iz++;
                    }
                }
            } else {
                inv.setItem(iz, new ItemStack(Material.AIR));
                iz++;
            }
        }
        for (Player ply : viewing) {
            ply.updateInventory();
        }
    }

    private int getGameTypeSize() {
        final int games = plugin.getGameTypeManager().getGameTypes().size();
        if (games <= 9 && games >= 1) {
            return 9;
        }
        if (games <= 18 && games >= 10) {
            return 18;
        }
        if (games <= 27 && games >= 19) {
            return 27;
        }
        if (games <= 36 && games >= 28) {
            return 36;
        }
        if (games <= 45 && games >= 37) {
            return 45;
        }
        if (games <= 54 && games >= 46) {
            return 54;
        }
        return 54;
    }

    @EventHandler
    public void onDrag(InventoryMoveItemEvent e) {
        if (e.getDestination().getTitle().equalsIgnoreCase(ChatColor.BLUE + "Select a Un-Ranked 2v2 Queue") || viewing.contains((Player)e.getInitiator().getHolder())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if ((e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) && e.getItem() != null && e.getItem().hasItemMeta() && e.getItem().isSimilar(new ItemBuilder(Material.DIAMOND_SWORD, "&9Join 2v2 unranked queue", "").getItem())) {
            if (plugin.getTeamManager().getTeamByLeader(e.getPlayer()) != null) {
                if (plugin.getTeamManager().getTeamByLeader(e.getPlayer()).getTeam().size() == 2) {
                    new UnrankedInventory2v2(e.getPlayer());
                    e.getPlayer().setMetadata("unranked-2v2", new FixedMetadataValue(plugin, "unranked-2v2"));
                } else {
                    e.getPlayer().sendMessage(ChatColor.RED + "You must have 2 players!");
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Player ply = (Player)e.getWhoClicked();
        if (viewing.contains(ply) && (e.isLeftClick() || e.isShiftClick() || e.isRightClick()) && e.getClickedInventory().getTitle().equalsIgnoreCase(ChatColor.BLUE + "Select a Un-Ranked 2v2 Queue") && e.getCurrentItem() != null && e.getCurrentItem().getItemMeta() != null && plugin.getTeamManager().getTeamByLeader(ply) != null) {
            String colorless = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', e.getCurrentItem().getItemMeta().getDisplayName()));
            for (GameType gt : plugin.getGameTypeManager().getGameTypes()) {
                if (gt.getName().equalsIgnoreCase(colorless)) {
                    plugin.get2v2MatchManager().addToQueue(plugin.getTeamManager().getTeamByLeader(ply), gt);
                    e.setCancelled(true);
                    ply.closeInventory();
                    ply.removeMetadata("unranked-2v2", plugin);
                    viewing.remove(ply);
                }
            }
        }
        if (e.getClickedInventory().getTitle().equalsIgnoreCase(ChatColor.BLUE + "Select a Un-Ranked 2v2 Queue")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (e.getPlayer().hasMetadata("unranked-2v2")) {
            e.getPlayer().removeMetadata("unranked-2v2", plugin);
        }
        if (viewing.contains((Player)e.getPlayer())) {
            viewing.remove((Player)e.getPlayer());
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (viewing.contains((Player)e.getWhoClicked())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent e) {
        if (!viewing.contains((Player)e.getPlayer())) viewing.add((Player)e.getPlayer());
        if (e.getInventory().getSize() == getGameTypeSize() && e.getInventory().getTitle().equalsIgnoreCase(ChatColor.BLUE + "Select a Un-Ranked 2v2 Queue")) {
            update();
        }
    }
}
