package com.slurpeh.servercore.practice.match;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.slurpeh.servercore.practice.KohiPractice;
import com.slurpeh.servercore.practice.arena.Arena;
import com.slurpeh.servercore.practice.gametype.GameType;
import com.slurpeh.servercore.practice.util.InventoryBuilder;
import com.slurpeh.servercore.practice.util.ItemBuilder;
import com.slurpeh.servercore.practice.util.JsonBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Random;

/**
 * Created by Bradley on 5/15/16.
 */
public class DuelManager implements Listener {
    //TODO ADD MAP SELECTING
    KohiPractice plugin;
    private HashMap<Player, Player> pickingGameMode;
    private HashMap<Player, Player> awaitingReply;
    private Table<Player, GameType, Arena> waitingForReply;
    private HashMap<Player, GameType> gametypegetter;
    //incomplete

    public DuelManager(KohiPractice plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents((Listener)this, (Plugin)this.plugin);
        plugin.getCommand("duel").setExecutor(new DuelCommand());
        plugin.getCommand("accept").setExecutor(new AcceptCommand());
        this.pickingGameMode = new HashMap<Player, Player>();
        this.awaitingReply = new HashMap<Player, Player>();
        this.waitingForReply = HashBasedTable.create();
        this.gametypegetter = new HashMap<>();
    }

    private void intiateDuel(final Player ply, final Player target, final GameType gt, Arena arena) {
        this.awaitingReply.put(ply, target);
        final JsonBuilder builder = new JsonBuilder(new String[] { "" });
        builder.withText(ply.getName()).withClickEvent(JsonBuilder.ClickAction.RUN_COMMAND, "/accept " + ply.getName()).withHoverEvent(JsonBuilder.HoverAction.SHOW_TEXT, "/accept " + ply.getName() + " (Requests expire in 30 seconds)").withText(" has requested to duel you with ").withColor(ChatColor.YELLOW).withClickEvent(JsonBuilder.ClickAction.RUN_COMMAND, "/accept " + ply.getName()).withHoverEvent(JsonBuilder.HoverAction.SHOW_TEXT, "/accept " + ply.getName() + " (Requests expire in 30 seconds)").withText(gt.getName()).withColor(ChatColor.DARK_GREEN).withClickEvent(JsonBuilder.ClickAction.RUN_COMMAND, "/accept " + ply.getName()).withHoverEvent(JsonBuilder.HoverAction.SHOW_TEXT, "/accept " + ply.getName() + " (Requests expire in 30 seconds)").withText("! Click this message to accept.").withColor(ChatColor.YELLOW).withClickEvent(JsonBuilder.ClickAction.RUN_COMMAND, "/accept " + ply.getName()).withHoverEvent(JsonBuilder.HoverAction.SHOW_TEXT, "/accept " + ply.getName() + " (Requests expire in 30 seconds)");
        builder.sendJson(target);
        new BukkitRunnable() {
            @Override
            public void run() {
                awaitingReply.remove(ply);
                target.sendMessage(ChatColor.YELLOW + "Duel request from " + ChatColor.DARK_GREEN + ply.getName() + ChatColor.YELLOW + " has expired.");
            }
        }.runTaskLater(plugin, 600L);
    }

    private void startDuel(final Player ply, final Player target) {
        ply.sendMessage(ChatColor.YELLOW + "Duel starting with " + ChatColor.GREEN + target.getName());
        target.sendMessage(ChatColor.YELLOW + "Duel starting with " + ChatColor.GREEN + ply.getName());
        this.plugin.getMatchManager().startMatch(ply, target, this.gametypegetter.get(ply), this.waitingForReply.get(ply, gametypegetter.get(ply)), false);
        this.waitingForReply.remove(ply, gametypegetter.get(ply));
        GameType gt = gametypegetter.get(ply);
        this.gametypegetter.remove(ply);
        this.waitingForReply.remove(ply, gt);
    }

    public void openDuelMenu(Player ply) {
        InventoryBuilder inventoryBuilder = new InventoryBuilder(getGameTypes(), "Select PvP Style", false, false);
        int i = 0;
        for (GameType gt : plugin.getGameTypeManager().getGameTypes()) {
            if (gt.isSetup()) {
                inventoryBuilder.withItem(i, new ItemBuilder(gt.getDisplay().getType(), "&b" + gt.getName(), "").getItem());
                i++;
            }
        }
        ply.openInventory(inventoryBuilder.build());
    }

    public void openArenaMenu(Player ply) {
        InventoryBuilder inventoryBuilder = new InventoryBuilder(getArenas(), "Select an arena", false, false);
        inventoryBuilder.withItem(0, new ItemBuilder(Material.EMPTY_MAP, "&aRandom Arena", "&7Unknown map").getItem());
        int i = 1;
        for (Arena arena : plugin.getArenaManager().getArenas()) {
            if (arena.isSetup()) {
                inventoryBuilder.withItem(i, new ItemBuilder(Material.STAINED_CLAY, "&e" + arena.getName(), "", 1, (short)4).getItem());
                i++;
            }
        }
        ply.openInventory(inventoryBuilder.build());
    }

    public int getArenas() {
        int games = KohiPractice.getInstance().getArenaManager().getArenas().size();
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
    public void onClick(InventoryClickEvent e) {
        Player ply = (Player)e.getWhoClicked();
        if (e.getClickedInventory().getTitle().equalsIgnoreCase("Select PvP Style") && e.getClickedInventory().getSize() == getGameTypes() && pickingGameMode.containsKey(ply)) {
            if (e.getCurrentItem().getItemMeta() != null) {
                String gtName = e.getCurrentItem().getItemMeta().getDisplayName();
                String colorlessName = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', gtName));
                for (GameType gt : plugin.getGameTypeManager().getGameTypes()) {
                    if (gt.getName().equalsIgnoreCase(colorlessName)) {
                        Player target = pickingGameMode.get(ply);
                        this.gametypegetter.put(ply, gt);
                        ply.closeInventory();
                        openArenaMenu(ply);
                        e.setCancelled(true);
                        return;
                    }
                }
            }
        } else if (e.getClickedInventory().getTitle().equalsIgnoreCase("Select an arena") && e.getClickedInventory().getSize() == getArenas() && gametypegetter.containsKey(ply)) {
            if (e.getCurrentItem() != null && e.getCurrentItem().getItemMeta() != null) {
                String arenaName = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());
                if (plugin.getArenaManager().getArena(arenaName) != null) {
                    this.waitingForReply.put(ply, gametypegetter.get(ply), plugin.getArenaManager().getArena(arenaName));
                    ply.sendMessage(ChatColor.YELLOW + "Duel request sent to " + ChatColor.DARK_GREEN + pickingGameMode.get(ply).getName() + ChatColor.YELLOW + " with " + ChatColor.DARK_GREEN + gametypegetter.get(ply).getName() + "" + ChatColor.YELLOW + " on arena " + ChatColor.DARK_GREEN + waitingForReply.get(ply, gametypegetter.get(ply)).getName());
                    intiateDuel(ply, pickingGameMode.get(ply), gametypegetter.get(ply), waitingForReply.get(ply, gametypegetter.get(ply)));
                    pickingGameMode.remove(ply);
                    ply.closeInventory();
                    e.setCancelled(true);
                } else {
                    if (arenaName.equalsIgnoreCase("Random Arena")) {
                        Arena arena = plugin.getArenaManager().getArenas().get(new Random().nextInt(plugin.getArenaManager().getArenas().size()));
                        this.waitingForReply.put(ply, gametypegetter.get(ply), arena);
                        ply.sendMessage(ChatColor.YELLOW + "Duel request sent to " + ChatColor.DARK_GREEN + pickingGameMode.get(ply).getName() + ChatColor.YELLOW + " with " + ChatColor.DARK_GREEN + gametypegetter.get(ply).getName() + "" + ChatColor.YELLOW + " on arena " + ChatColor.DARK_GREEN + waitingForReply.get(ply, gametypegetter.get(ply)).getName());
                        intiateDuel(ply, pickingGameMode.get(ply), gametypegetter.get(ply), waitingForReply.get(ply, gametypegetter.get(ply)));

                        pickingGameMode.remove(ply);
                        ply.closeInventory();
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    public int getGameTypes() {
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

    class DuelCommand implements CommandExecutor {
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "You must be a player to execute this command!");
                return false;
            }
            Player ply = (Player)sender;
            if (args.length == 1) {
                String target = args[0];
                if (Bukkit.getPlayer(target) != null && !ply.getName().equalsIgnoreCase(target)) {
                    openDuelMenu(ply);
                    DuelManager.this.pickingGameMode.put(ply, Bukkit.getPlayer(target));
                    return true;
                } else {
                    ply.sendMessage(ChatColor.RED + "Invalid player!");
                    return true;
                }
            } else {
                ply.sendMessage(ChatColor.RED + "/duel [player-name]");
                return true;
            }
        }
    }

    class AcceptCommand implements CommandExecutor {
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "You must be a player to execute this command!");
                return false;
            }
            Player ply = (Player)sender;
            if (args.length == 1) {
                Player target = Bukkit.getPlayer(args[0]);
                if (target != null) {
                    if (!DuelManager.this.awaitingReply.containsKey(target) || DuelManager.this.awaitingReply.get(target) != ply) {
                        ply.sendMessage(ChatColor.RED + "That player did not send you a duel request!");
                        return true;
                    } else {
                        DuelManager.this.awaitingReply.remove(target);
                        DuelManager.this.startDuel(target, ply);
                        return true;
                    }
                } else {
                    ply.sendMessage(ChatColor.RED + "Invalid player!");
                    return true;
                }
            } else {
                ply.sendMessage(ChatColor.RED + "/accept [player-name]");
                return true;
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().getName().equals("Select Pvp Style") && this.pickingGameMode.containsKey(event.getPlayer())) {
            this.pickingGameMode.remove(event.getPlayer());
        }
    }
}
