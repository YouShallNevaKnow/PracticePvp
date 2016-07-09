package com.slurpeh.servercore.practice.inventory;

import com.slurpeh.servercore.practice.KohiPractice;
import com.slurpeh.servercore.practice.gametype.GameType;
import com.slurpeh.servercore.practice.util.InventoryBuilder;
import com.slurpeh.servercore.practice.util.ItemBuilder;
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
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bradley on 5/2/16.
 */
public class UnrankedInventory implements Listener {
    //TODO FIXES
    private InventoryBuilder builder;
    private Inventory inv;
    private List<Player> viewing;

    public UnrankedInventory() {
        this.viewing = new ArrayList<>();
        builder = new InventoryBuilder(getGameTypeSize(), "&9Select a Un-Ranked Queue", true, false);
        int i = 0;
        for (GameType gameType : KohiPractice.getInstance().getGameTypeManager().getGameTypes()) {
            if (gameType.isSetup()) {
                ItemStack display = gameType.getDisplay().clone();
                ItemMeta displayMeta = display.getItemMeta();
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.YELLOW + "In queue: " + ChatColor.GREEN + KohiPractice.getInstance().getMatchManager().getAmountInQueue(gameType, false));
                lore.add(ChatColor.YELLOW + "In fights: " + ChatColor.GREEN + KohiPractice.getInstance().getMatchManager().getAmountInMatch(gameType, false));
                displayMeta.setLore(lore);
                displayMeta.setDisplayName(ChatColor.BLUE + gameType.getName());
                display.setAmount(KohiPractice.getInstance().getMatchManager().getAmountInMatch(gameType, false));
                display.setItemMeta(displayMeta);
                builder.withItem(i, display);
                i++;
            }
        }
        inv = builder.build();

    }

    public UnrankedInventory(Player ply) {
        this.viewing = new ArrayList<>();
        builder = new InventoryBuilder(getGameTypeSize(), "&9Select a Un-Ranked Queue", true, false);
        int i = 0;
        for (GameType gameType : KohiPractice.getInstance().getGameTypeManager().getGameTypes()) {
            if (gameType.isSetup()) {
                ItemStack display = gameType.getDisplay().clone();
                ItemMeta displayMeta = display.getItemMeta();
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.YELLOW + "In queue: " + ChatColor.GREEN + KohiPractice.getInstance().getMatchManager().getAmountInQueue(gameType, false));
                lore.add(ChatColor.YELLOW + "In fights: " + ChatColor.GREEN + KohiPractice.getInstance().getMatchManager().getAmountInMatch(gameType, false));
                displayMeta.setLore(lore);
                displayMeta.setDisplayName(ChatColor.BLUE + gameType.getName());
                display.setAmount(KohiPractice.getInstance().getMatchManager().getAmountInMatch(gameType, false));
                display.setItemMeta(displayMeta);
                builder.withItem(i, display);
                i++;
            }
        }
        inv = builder.build();
        ply.openInventory(inv);
        ply.updateInventory();
        viewing.add(ply);
    }

    public void openInventory(Player ply) {
        ply.openInventory(inv);
        ply.updateInventory();
        viewing.add(ply);
    }

    public List<Player> getViewers() {
        return this.viewing;
    }

    public void updateInventory() {
        builder = new InventoryBuilder(getGameTypeSize(), "&9Select a Un-Ranked Queue", true, false);
        int i = 0;
        for (GameType gameType : KohiPractice.getInstance().getGameTypeManager().getGameTypes()) {
            if (gameType.isSetup()) {
                ItemStack display = gameType.getDisplay().clone();
                ItemMeta displayMeta = display.getItemMeta();
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.YELLOW + "In queue: " + ChatColor.GREEN + KohiPractice.getInstance().getMatchManager().getAmountInQueue(gameType, false));
                lore.add(ChatColor.YELLOW + "In fights: " + ChatColor.GREEN + KohiPractice.getInstance().getMatchManager().getAmountInMatch(gameType, false));
                displayMeta.setLore(lore);
                displayMeta.setDisplayName(ChatColor.BLUE + gameType.getName());
                display.setAmount(Math.round(KohiPractice.getInstance().getMatchManager().getAmountInMatch(gameType, false) / 2));
                display.setItemMeta(displayMeta);
                builder.withItem(i, display);
                i++;
                for (Player ply : viewing) {
                    ply.updateInventory();
                }
            }
        }
        inv = builder.build();
        int iz = 0;
        for (ItemStack stack : inv.getContents()) {
            if (stack != null && stack.hasItemMeta() && stack.getItemMeta() != null) {
                if (KohiPractice.getInstance().getGameTypeManager() != null && KohiPractice.getInstance().getGameTypeManager().getGameTypes().size() != 0 && KohiPractice.getInstance().getGameTypeManager().getGameTypes() != null) {
                    for (GameType gtg : JavaPlugin.getPlugin(KohiPractice.class).getGameTypeManager().getGameTypes()) {
                        ItemStack stackn = stack.clone();
                        ItemMeta sn2 = stackn.getItemMeta();
                        sn2.setDisplayName(ChatColor.BLUE + ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', stack.getItemMeta().getDisplayName())));
                        stackn.setItemMeta(sn2);
                        if (gtg.getDisplay().isSimilar(stackn)) {
                            ItemMeta meta = stack.clone().getItemMeta();
                            List<String> lore = new ArrayList<String>();
                            lore.add(ChatColor.YELLOW + "In queue: " + ChatColor.GREEN + KohiPractice.getInstance().getMatchManager().getAmountInQueue(gtg, false));
                            lore.add(ChatColor.YELLOW + "In fights: " + ChatColor.GREEN + KohiPractice.getInstance().getMatchManager().getAmountInMatch(gtg, false));
                            meta.setLore(lore);
                            meta.setDisplayName(ChatColor.BLUE + gtg.getName());
                            stack.setItemMeta(meta);
                            stack.setAmount(KohiPractice.getInstance().getMatchManager().getAmountInMatch(gtg, false));
                            inv.setItem(iz, stack);
                            iz++;
                            continue;
                        }
                    }
                }

            } else {
                inv.setItem(iz, new ItemStack(Material.AIR));
                iz++;
                continue;
            }
            continue;
        }
        for (Player ply : viewing) {
            ply.updateInventory();
        }
    }

    private int getGameTypeSize() {
        int games = KohiPractice.getInstance().getGameTypeManager().getGameTypes().size();
        if (games <= 9 && games >= 1) {
            return 9;
        } else if (games <= 18 && games >= 10) {
            return 18;
        } else if (games <= 27 && games >= 19) {
            return 27;
        } else if (games <= 36 && games >= 28) {
            return 36;
        } else if (games <= 45 && games >= 37) {
            return 45;
        } else if (games <= 54 && games >= 46) {
            return 54;
        } else {
            return 54;
        }
    }

    @EventHandler
    public void onDrag(InventoryMoveItemEvent e) {
        if (e.getDestination().getTitle().equalsIgnoreCase(ChatColor.BLUE + "Select a Un-Ranked Queue")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (e.getItem() != null && e.getItem().isSimilar(new ItemBuilder(Material.IRON_SWORD, "&9Un-Ranked Queue", "").getItem())) {
                new UnrankedInventory(e.getPlayer());
                viewing.add(e.getPlayer());
                e.getPlayer().setMetadata("unranked", new FixedMetadataValue(KohiPractice.getInstance(), "unranked"));
            }
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Player ply = (Player)e.getWhoClicked();
        if (viewing.contains(ply)) {
            if (e.isLeftClick() || e.isShiftClick() || e.isRightClick()) {
                if (e.getClickedInventory().getTitle().equalsIgnoreCase(ChatColor.BLUE + "Select a Un-Ranked Queue")) {
                    if (e.getCurrentItem().getItemMeta() != null) {
                        String colorlessName = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', e.getCurrentItem().getItemMeta().getDisplayName()));
                        for (GameType gt : KohiPractice.getInstance().getGameTypeManager().getGameTypes()) {
                            if (gt.getName().equalsIgnoreCase(colorlessName)) {
                                KohiPractice.getInstance().getMatchManager().addToQueue(ply, gt, false);
                                e.setCancelled(true);
                                ply.closeInventory();
                                ply.removeMetadata("unranked", KohiPractice.getInstance());
                                viewing.remove(ply);
                            }
                        }
                    }
                }
            }
        }
        if (e.getClickedInventory().getTitle().equalsIgnoreCase(ChatColor.BLUE + "Select a Un-Ranked Queue")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (e.getPlayer().hasMetadata("unranked")) {
            e.getPlayer().removeMetadata("unranked", KohiPractice.getInstance());
        }
        if (viewing.contains(e.getPlayer())) {
            viewing.remove(e.getPlayer());
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        viewing.add((Player)e.getPlayer());
        if (e.getInventory().getSize() == getGameTypeSize() && e.getInventory().getTitle().equalsIgnoreCase(ChatColor.BLUE + "Select a Un-Ranked Queue")) {
            updateInventory();
        }
    }

    @EventHandler
    public void onMove(InventoryMoveItemEvent e) {
        if (viewing.contains((Player)e.getInitiator().getHolder())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (viewing.contains((Player)e.getWhoClicked())) {
            e.setCancelled(true);
        }
    }
}
