package com.slurpeh.servercore.practice.match;

import com.slurpeh.servercore.practice.KohiPractice;
import com.slurpeh.servercore.practice.gametype.GameType;
import com.slurpeh.servercore.practice.inventory.InventoryType;
import com.slurpeh.servercore.practice.util.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.material.Dye;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

/**
 * Created by Bradley on 5/15/16.
 */
public class Queue implements Listener {
    //incomplete
    private GameType game;
    private boolean ranked;
    private HashMap<Player, Integer> queue;
    private HashMap<Player, Player> awaitingMatch;

    public Queue(final GameType game, final boolean ranked) {
        this.game = game;
        this.ranked = ranked;
        this.queue = new HashMap<Player, Integer>();
        this.awaitingMatch = new HashMap<Player, Player>();
    }

    public void addToQueue(final Player ply, final int rating) {
        this.queue.put(ply, rating);
        ply.getInventory().clear();
        Dye dye = new Dye();
        dye.setColor(DyeColor.ORANGE);
        ply.getInventory().setItem(8, new ItemBuilder(Material.getMaterial(351), ChatColor.RED + "Right click to leave " + ChatColor.YELLOW + game.getName() + " " + ChatColor.RED + "queue", "", 1, (short)14).getItem());
        if (this.ranked) {
            ply.sendMessage(ChatColor.YELLOW + "Added to the " + ChatColor.GREEN + ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', this.game.getName())) + ChatColor.YELLOW  + " queue with " + ChatColor.GREEN + rating + " elo.");
            ply.sendMessage(ChatColor.YELLOW + "Searching in elo range " + ChatColor.GREEN + "[" + (rating - 200) + " -> " + (rating + 200) + "]");
            new BukkitRunnable() {
                int range = 200;
                int i = 0;
                boolean maxRange = false;

                public void run() {
                    if (!Queue.this.queue.containsKey(ply)) {
                        this.cancel();
                        return;
                    }
                    for (final Player qply : Queue.this.queue.keySet()) {
                        if (rating - this.range <= Queue.this.queue.get(ply) && rating + this.range >= Queue.this.queue.get(ply) && qply != ply) {
                            this.cancel();
                            Queue.this.queue.remove(ply);
                            Queue.this.queue.remove(qply);
                            Queue.this.awaitingMatch.put(ply, qply);
                            return;
                        }
                    }
                    ++this.i;
                    if (!this.maxRange) {
                        if (this.i == 5) {
                            this.range += 50;
                            if (rating - this.range <= 0) {
                                this.maxRange = true;
                            }
                            ply.sendMessage(ChatColor.YELLOW + "Searching in elo range " + ChatColor.GREEN + "[" + (rating - this.range) + " -> " + (rating + this.range) + "]");
                            this.i = 0;
                        }
                    }
                    else if (this.i % 5 == 0) {
                        ply.sendMessage(ChatColor.YELLOW + "Removed from the " + ChatColor.GREEN + game.getDisplayName() + "queue.");
                        Queue.this.queue.remove(ply);
                        ((KohiPractice) JavaPlugin.getPlugin((Class)KohiPractice.class)).getInventorySetter().setupInventory(InventoryType.DEFAULT, ply);
                        this.cancel();
                    }
                }
            }.runTaskTimer((Plugin)JavaPlugin.getPlugin((Class)KohiPractice.class), 0L, 20L);
        }
        else {
            ply.sendMessage(ChatColor.YELLOW + "Added to the " + ChatColor.GREEN + ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', this.game.getName())) + ChatColor.YELLOW + " queue, please wait for another player.");
            new BukkitRunnable() {
                public void run() {
                    if (!Queue.this.queue.containsKey(ply)) {
                        this.cancel();
                        return;
                    }
                    for (final Player qply : Queue.this.queue.keySet()) {
                        if (qply != ply) {
                            this.cancel();
                            Queue.this.queue.remove(ply);
                            Queue.this.queue.remove(qply);
                            Queue.this.awaitingMatch.put(ply, qply);
                        }
                    }
                }
            }.runTaskTimer((Plugin)JavaPlugin.getPlugin((Class)KohiPractice.class), 0L, 20L);
        }
    }

    public boolean hasMatch() {
        return this.awaitingMatch.size() > 0;
    }

    public void startMatch(final Player ply) {
        this.awaitingMatch.remove(ply);
    }

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent event) {
        if (this.queue.containsKey(event.getPlayer())) {
            this.queue.remove(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerRightClickLeaveQueue(final PlayerInteractEvent event) {
        if (event.getItem() != null && event.getItem().hasItemMeta() && event.getItem().getItemMeta().getDisplayName() != null && ChatColor.stripColor(event.getItem().getItemMeta().getDisplayName()).equals("Right click to leave " + game.getName() + " queue") && this.queue.containsKey(event.getPlayer())) {
            this.queue.remove(event.getPlayer());
            ((KohiPractice)JavaPlugin.getPlugin((Class)KohiPractice.class)).getInventorySetter().setupInventory(InventoryType.DEFAULT, event.getPlayer());
            event.getPlayer().sendMessage(ChatColor.YELLOW + "Removed from the " + ChatColor.GREEN + game.getName() + ChatColor.YELLOW + " queue.");
            new BukkitRunnable() {
                public void run() {
                    ((KohiPractice)JavaPlugin.getPlugin((Class)KohiPractice.class)).getRankedInventory().updateInventory();
                    KohiPractice.getInstance().getUnrankedInventory().updateInventory();
                }
            }.runTaskLater((Plugin) JavaPlugin.getPlugin((Class)KohiPractice.class), 5L);
        }
    }

    public GameType getGame() {
        return this.game;
    }

    public boolean isRanked() {
        return this.ranked;
    }

    public HashMap<Player, Integer> getQueue() {
        return this.queue;
    }

    public HashMap<Player, Player> getAwaitingMatch() {
        return this.awaitingMatch;
    }
}
