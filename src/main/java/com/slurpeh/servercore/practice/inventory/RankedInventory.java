package com.slurpeh.servercore.practice.inventory;

import com.slurpeh.servercore.practice.KohiPractice;
import com.slurpeh.servercore.practice.gametype.GameType;
import com.slurpeh.servercore.practice.util.InventoryBuilder;
import com.slurpeh.servercore.practice.util.ItemBuilder;
import com.sun.tools.javap.JavapFileManager;
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
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bradley on 4/29/16.
 */
public class RankedInventory implements Listener {
    //incomplete
    private InventoryBuilder builder;
    private Inventory inv;
    private List<Player> viewing;

    public RankedInventory() {
        this.viewing = new ArrayList<>();
        builder = new InventoryBuilder(getGameTypeSize(), "&aSelect a Ranked Queue", true, false);
        int i = 0;
        for (GameType gameType : KohiPractice.getInstance().getGameTypeManager().getGameTypes()) {
            if (gameType.isSetup()) {
                ItemStack display = gameType.getDisplay().clone();
                ItemMeta displayMeta = display.getItemMeta();
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.YELLOW + "In queue: " + ChatColor.GREEN + KohiPractice.getInstance().getMatchManager().getAmountInQueue(gameType, true));
                lore.add(ChatColor.YELLOW + "In fights: " + ChatColor.GREEN + KohiPractice.getInstance().getMatchManager().getAmountInMatch(gameType, true));
                displayMeta.setLore(lore);
                displayMeta.setDisplayName(ChatColor.RESET + "" + ChatColor.BLUE + gameType.getName());
                display.setAmount(KohiPractice.getInstance().getMatchManager().getAmountInMatch(gameType, true) != 0 ? KohiPractice.getInstance().getMatchManager().getAmountInMatch(gameType, true) : 1);
                display.setItemMeta(displayMeta);
                builder.withItem(i, display);
                i++;
            }
        }
        inv = builder.build();

    }

    public RankedInventory(Player ply) {
        this.viewing = new ArrayList<>();
        builder = new InventoryBuilder(getGameTypeSize(), "&aSelect a Ranked Queue", true, false);
        int i = 0;
        for (GameType gameType : KohiPractice.getInstance().getGameTypeManager().getGameTypes()) {
            if (gameType.isSetup()) {
                ItemStack display = gameType.getDisplay().clone();
                ItemMeta displayMeta = display.getItemMeta();
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.YELLOW + "In queue: " + ChatColor.GREEN + KohiPractice.getInstance().getMatchManager().getAmountInQueue(gameType, true));
                lore.add(ChatColor.YELLOW + "In fights: " + ChatColor.GREEN + KohiPractice.getInstance().getMatchManager().getAmountInMatch(gameType, true));
                displayMeta.setLore(lore);
                displayMeta.setDisplayName(ChatColor.RESET + "" + ChatColor.BLUE + gameType.getName());
                display.setAmount(KohiPractice.getInstance().getMatchManager().getAmountInMatch(gameType, true) != 0 ? KohiPractice.getInstance().getMatchManager().getAmountInMatch(gameType, true) : 1);
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
        builder = new InventoryBuilder(getGameTypeSize(), "&aSelect a Ranked Queue", true, false);
        int i = 0;
        for (GameType gameType : KohiPractice.getInstance().getGameTypeManager().getGameTypes()) {
            if (gameType.isSetup()) {
                ItemStack display = gameType.getDisplay().clone();
                ItemMeta displayMeta = display.getItemMeta();
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.YELLOW + "In queue: " + ChatColor.GREEN + KohiPractice.getInstance().getMatchManager().getAmountInQueue(gameType, true));
                lore.add(ChatColor.YELLOW + "In fights: " + ChatColor.GREEN + KohiPractice.getInstance().getMatchManager().getAmountInMatch(gameType, true));
                displayMeta.setLore(lore);
                displayMeta.setDisplayName(ChatColor.RESET + "" + ChatColor.BLUE + gameType.getName());
                display.setAmount(KohiPractice.getInstance().getMatchManager().getAmountInMatch(gameType, true) != 0 ? KohiPractice.getInstance().getMatchManager().getAmountInMatch(gameType, true) : 1);
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
                        ItemStack stack2 = stack.clone();
                        ItemMeta stack2m = stack2.getItemMeta();
                        stack2m.setDisplayName(ChatColor.RESET + "" + ChatColor.BLUE + ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', stack.getItemMeta().getDisplayName())));
                        stack2.setItemMeta(stack2m);
                        if (gtg.getDisplay().isSimilar(stack2)) {
                            ItemMeta meta = stack.getItemMeta();
                            List<String> lore = new ArrayList<String>();
                            lore.add(ChatColor.YELLOW + "In queue: " + ChatColor.GREEN + KohiPractice.getInstance().getMatchManager().getAmountInQueue(gtg, true));
                            lore.add(ChatColor.YELLOW + "In fights: " + ChatColor.GREEN + KohiPractice.getInstance().getMatchManager().getAmountInMatch(gtg, true));
                            meta.setLore(lore);
                            meta.setDisplayName(ChatColor.RESET + "" + ChatColor.BLUE + gtg.getName());
                            stack.setItemMeta(meta);
                            stack.setAmount(KohiPractice.getInstance().getMatchManager().getAmountInMatch(gtg, true) != 0 ? KohiPractice.getInstance().getMatchManager().getAmountInMatch(gtg, true) : 1);
                            inv.setItem(iz, stack);
                            iz++;
                            continue;
                        }
                    }
                }

            } else {
                inv.setItem(iz, new ItemStack(Material.AIR));iz++;
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
        if (e.getDestination().getTitle().equalsIgnoreCase(ChatColor.GREEN + "Select a Ranked Queue")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (e.getPlayer().getItemInHand().hasItemMeta() && e.getPlayer().getItemInHand() != null) {
                if (e.getPlayer().getItemInHand().isSimilar(new ItemBuilder(Material.DIAMOND_SWORD, "&aRanked Queue", "").getItem())) {
                    if (KohiPractice.getInstance().getPlayerDataManager().getPlayerData(e.getPlayer()).getGamesPlayed() >= KohiPractice.getInstance().getConfig().getInt("unranked-games-min")) {
                        new RankedInventory(e.getPlayer());
                        viewing.add(e.getPlayer());
                        e.getPlayer().setMetadata("ranked", new FixedMetadataValue(KohiPractice.getInstance(), "ranked"));
                    } else {
                        e.setCancelled(true);
                        e.getPlayer().sendMessage(ChatColor.RED + "You can't enter a ranked queue until you have won " + KohiPractice.getInstance().getConfig().getInt("unranked-games-min") + " unranked games. " + ChatColor.YELLOW + "You have won " + KohiPractice.getInstance().getPlayerDataManager().getPlayerData(e.getPlayer()).getGamesPlayed());
                    }
                } else {
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Player ply = (Player)e.getWhoClicked();
        if (viewing.contains(ply)) {
            if (e.isLeftClick() || e.isShiftClick() || e.isRightClick()) {
                if (e.getClickedInventory().getTitle().equalsIgnoreCase(ChatColor.GREEN + "Select a Ranked Queue")) {
                    if (e.getCurrentItem().getItemMeta() != null) {
                        String colorlessName = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', e.getCurrentItem().getItemMeta().getDisplayName()));
                        for (GameType gt : KohiPractice.getInstance().getGameTypeManager().getGameTypes()) {
                            if (gt.getName().equalsIgnoreCase(colorlessName)) {
                                KohiPractice.getInstance().getMatchManager().addToQueue(ply, gt, true);
                                e.setCancelled(true);
                                ply.closeInventory();
                                ply.removeMetadata("ranked", KohiPractice.getInstance());
                                viewing.remove(ply);
                            }
                        }
                    }
                }
            }
        }
        if (e.getClickedInventory().getTitle().equalsIgnoreCase(ChatColor.GREEN + "Select a Ranked Queue")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (e.getPlayer().hasMetadata("ranked")) {
            e.getPlayer().removeMetadata("ranked", KohiPractice.getInstance());
        }
        if (viewing.contains(e.getPlayer())) {
            viewing.remove(e.getPlayer());
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        viewing.add((Player)e.getPlayer());
        if (e.getInventory().getSize() == getGameTypeSize() && e.getInventory().getTitle().equalsIgnoreCase(ChatColor.GREEN + "Select a Ranked Queue")) {
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
