package com.slurpeh.servercore.practice.team;

import com.slurpeh.servercore.practice.KohiPractice;
import com.slurpeh.servercore.practice.arena.Arena;
import com.slurpeh.servercore.practice.gametype.GameType;
import com.slurpeh.servercore.practice.inventory.InventoryType;
import com.slurpeh.servercore.practice.player.Kit;
import com.slurpeh.servercore.practice.util.EntityHider;
import com.slurpeh.servercore.practice.util.ItemBuilder;
import com.slurpeh.servercore.practice.util.JsonBuilder;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Dye;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Created by Bradley on 5/11/16.
 */
public class TeamMatch implements Listener {
    boolean started;
    TeamMatchType type;
    List<Player> players;
    GameType gt;
    Arena arena;
    public Team t1;
    public Team t2;
    KohiPractice plugin;
    public HashMap<Player, String> remaining;
    HashMap<Player, PearlCounter> counters;
    public List<Player> spectators;
    private List<Block> blocks;

    public TeamMatch(List<Player> players, TeamMatchType type, GameType gt, Arena arena) { //ffa constructor
        this.players = players;
        this.type = type;
        this.gt = gt;
        this.started = false;
        this.arena = arena;
        this.remaining = new HashMap<>();
        this.plugin = (KohiPractice) JavaPlugin.getPlugin(KohiPractice.class);
        this.counters = new HashMap<>();
        this.spectators = new ArrayList<>();
        Bukkit.getPluginManager().registerEvents(this, this.plugin);
        this.blocks = new ArrayList<>();
    }

    public TeamMatch(Team t1, Team t2, TeamMatchType type, GameType gt, Arena arena) { //partyvparty constructor
        this.type = type;
        this.gt = gt;
        this.arena = arena;
        this.t1 = t1;
        this.t2 = t2;
        this.remaining = new HashMap<>();
        this.started = false;
        this.counters = new HashMap<>();
        this.plugin = (KohiPractice) JavaPlugin.getPlugin(KohiPractice.class);
        this.spectators = new ArrayList<>();
        Bukkit.getPluginManager().registerEvents(this, this.plugin);
        this.blocks = new ArrayList<>();
    }

    public TeamMatch(Team t1, TeamMatchType type, GameType gt, Arena arena) { //teamsplit constructor
        this.type = type;
        this.gt = gt;
        this.arena = arena;
        this.t1 = t1;
        this.remaining = new HashMap<>();
        this.started = false;
        this.counters = new HashMap<>();
        this.plugin = (KohiPractice) JavaPlugin.getPlugin(KohiPractice.class);
        this.spectators = new ArrayList<>();
        Bukkit.getPluginManager().registerEvents(this, this.plugin);
        this.blocks = new ArrayList<>();
    }

    public TeamMatchType getType() {
        return type;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public GameType getGameType() {
        return gt;
    }

    public List<Block> getBlocks() {
        return blocks;
    }

    public void addBlock(Block b) {
        blocks.add(b);
    }

    public void removeBlock(Block v) {
        blocks.remove(v);
    }

    public boolean hasBlock(Block b) {
        return blocks.contains(b);
    }

    public void undoBuilds() {
        if (getGameType().canPlaceAndBreak()) {
            while (blocks.iterator().hasNext()) {
                blocks.iterator().next().setType(Material.AIR);
            }
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if (hasPlayer(e.getPlayer())) {
            if (getGameType().canPlaceAndBreak()) {
                addBlock(e.getBlockPlaced());
            } else {
                e.setCancelled(true);
            }
        } else {
            if (!e.getPlayer().isOp()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if (hasPlayer(e.getPlayer())) {
            if (getGameType().canPlaceAndBreak()) {
                if (hasBlock(e.getBlock())) {
                    removeBlock(e.getBlock());
                } else {
                    e.setCancelled(true);
                }
            } else {
                e.setCancelled(true);
            }
        } else {
            if (!e.getPlayer().isOp()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        if (hasPlayer(e.getPlayer())) {
            if (getGameType().canPlaceAndBreak()) {
                Location l = e.getBlockClicked().getLocation();
                BlockFace bf = e.getBlockFace();
                Location l2 = new Location(l.getWorld(), l.getX() + bf.getModX(), l.getY() + bf.getModY(), l.getZ() + bf.getModZ());
                addBlock(l2.getBlock());
            } else {
                e.setCancelled(true);
            }
        } else {
            if (!e.getPlayer().isOp()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockChange(BlockFromToEvent e) {
        for (TeamMatch m : plugin.getTeamManager().getTeamMatches()) {
            if (m.hasBlock(e.getBlock())) {
                m.addBlock(e.getToBlock());
            }
        }
    }

    public Arena getArena() {
        return arena;
    }

    public void startMatch() {
        switch (type) {
            case FFA: {
                if (players != null) {
                    for (TeamMatch m : plugin.getTeamManager().getTeamMatches()) {
                        if (m.getGameType().canPlaceAndBreak() && m.arena.getName().equalsIgnoreCase(arena.getName())) {
                            for (Block b : m.getBlocks()) {
                                for (Player ply : players) {
                                    ply.sendBlockChange(b.getLocation(), Material.AIR, (byte) 0);
                                }
                            }
                        }
                    }
                    for (int i = 0; i < players.size(); i++) {
                        if ((i & 1) == 0) {
                            players.get(i).teleport(arena.getSpawn1());
                        } else {
                            players.get(i).teleport(arena.getSpawn2());
                        }
                    }
                    for (Player ply : players) {
                        remaining.put(ply, "s");
                    }
                    plugin.getTeamManager().getTeam(players.get(0)).setMatch(this);
                    plugin.getTeamManager().getTeam(players.get(0)).setInMatch(true);
                    for (Player ply : Bukkit.getOnlinePlayers()) {
                        if (! players.contains(ply)) {
                            for (Player pl : players) {
                                pl.hidePlayer(ply);
                                ply.hidePlayer(pl);
                            }
                        }
                    }
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            for (Player pl1 : players) {
                                for (Player pl2 : players) {
                                    if (pl1 != pl2) {
                                        pl1.showPlayer(pl2);
                                        pl2.showPlayer(pl1);
                                    }
                                }
                            }
                        }
                    }.runTaskLater(this.plugin, 5);
                    for (Player ply : players) {
                        ply.sendMessage(ChatColor.YELLOW + "Starting ffa against other members of your team.");
                        ply.getInventory().clear();
                        ply.getInventory().setContents(gt.getStartingKit().getInv().getContents());
                        ply.getInventory().setArmorContents(gt.getStartingKit().getInv().getArmorContents());
                    }
                    new BukkitRunnable() {
                        private int i = 5;

                        @Override
                        public void run() {
                            if (i == 0) {
                                cancel();
                                started = true;
                                for (Player ply : players) {
                                    ply.sendMessage(ChatColor.GREEN + "Duel starting now!");
                                    ply.playSound(ply.getLocation(), Sound.NOTE_PIANO, 10.0f, 2.0f);
                                }
                                return;
                            }
                            for (Player ply : players) {
                                ply.sendMessage(ChatColor.GREEN + "Starting in " + ChatColor.YELLOW + this.i + ChatColor.GREEN + " seconds!");
                                ply.playSound(ply.getLocation(), Sound.NOTE_PIANO, 10.0f, 1.0f);
                            }
                            -- this.i;
                        }
                    }.runTaskTimer(this.plugin, 0, 20);
                }
                break;
            }
            case TEAMSPLIT: {
                t1.setMatch(this);
                t1.setInMatch(true);
                for (TeamMatch m : plugin.getTeamManager().getTeamMatches()) {
                    if (m.getGameType().canPlaceAndBreak() && m.arena.getName().equalsIgnoreCase(arena.getName())) {
                        for (Block b : m.getBlocks()) {
                            for (Player ply : t1.getTeam()) {
                                ply.sendBlockChange(b.getLocation(), Material.AIR, (byte)0);
                            }
                        }
                    }
                }
                List<Player> team1 = new ArrayList<>();
                List<Player> team2 = new ArrayList<>();
                for (int i = 0; i < t1.getTeam().size(); i++) {
                    if ((i & 1) == 0) {
                        team1.add(t1.getTeam().get(i));
                        remaining.put(t1.getTeam().get(i), "team1");
                    } else {
                        team2.add(t1.getTeam().get(i));
                        remaining.put(t1.getTeam().get(i), "team2");
                    }
                }
                for (Player ply : team1) {
                    ply.teleport(this.arena.getSpawn1());
                    for (Player on : Bukkit.getOnlinePlayers()) {
                        if (! team1.contains(on) && ! team2.contains(on)) {
                            ply.hidePlayer(on);
                            on.hidePlayer(ply);
                        }
                    }
                }
                for (Player ply : team2) {
                    ply.teleport(this.arena.getSpawn2());
                    for (Player on : Bukkit.getOnlinePlayers()) {
                        if (! team1.contains(on) && ! team2.contains(on)) {
                            ply.hidePlayer(on);
                            on.hidePlayer(ply);
                        }
                    }
                }
                new BukkitRunnable() {
                    public void run() {
                        for (Player ply : team1) {
                            for (Player ply1 : team2) {
                                ply.showPlayer(ply1);
                                ply1.showPlayer(ply);
                            }
                        }
                    }
                }.runTaskLater(this.plugin, 5L);
                for (Player ply : team1) {
                    ply.sendMessage(ChatColor.BLUE + "Starting team fight against the members in your team.");
                    ply.sendMessage(ChatColor.YELLOW + "You are on Team A.");
                    ply.getInventory().clear();
                    ply.getInventory().setContents(gt.getStartingKit().getInv().getContents());
                    ply.getInventory().setArmorContents(gt.getStartingKit().getInv().getArmorContents());
                }
                for (Player ply : team2) {
                    ply.sendMessage(ChatColor.BLUE + "Starting team fight against the members in your team.");
                    ply.sendMessage(ChatColor.YELLOW + "You are on Team B.");
                    ply.getInventory().clear();
                    ply.getInventory().setContents(gt.getStartingKit().getInv().getContents());
                    ply.getInventory().setArmorContents(gt.getStartingKit().getInv().getArmorContents());
                }
                new BukkitRunnable() {
                    private int i = 5;

                    public void run() {
                        if (this.i == 0) {
                            this.cancel();
                            TeamMatch.this.started = true;
                            List<Player> plys = new ArrayList<>();
                            for (Player ply : team1) {
                                plys.add(ply);
                            }
                            for (Player ply : team2) {
                                plys.add(ply);
                            }
                            for (Player ply : plys) {
                                if (ply == null) {
                                    this.cancel();
                                    return;
                                }
                                ply.sendMessage(ChatColor.GREEN + "Duel starting now!");
                                ply.playSound(ply.getLocation(), Sound.NOTE_PIANO, 10.0f, 2.0f);
                            }
                            return;
                        }
                        List<Player> plys = new ArrayList<>();
                        for (Player ply : team1) {
                            plys.add(ply);
                        }
                        for (Player ply : team2) {
                            plys.add(ply);
                        }
                        for (Player ply : plys) {
                            if (ply == null) {
                                this.cancel();
                                return;
                            }
                            ply.sendMessage(ChatColor.GREEN + "Starting in " + ChatColor.YELLOW + this.i + ChatColor.GREEN+ " seconds!");
                            ply.playSound(ply.getLocation(), Sound.NOTE_PIANO, 10.0f, 1.0f);
                        }
                        -- this.i;
                    }
                }.runTaskTimer(this.plugin, 0L, 20L);
                break;
            }
            case PARTYvPARTY: {
                t1.setMatch(this);
                t1.setInMatch(true);
                t2.setMatch(this);
                t2.setInMatch(true);
                for (TeamMatch m : plugin.getTeamManager().getTeamMatches()) {
                    if (m.getGameType().canPlaceAndBreak() && m.arena.getName().equalsIgnoreCase(arena.getName())) {
                        for (Block b : m.getBlocks()) {
                            for (Player ply : t1.getTeam()) {
                                ply.sendBlockChange(b.getLocation(), Material.AIR, (byte) 0);
                            }
                        }
                    }
                }
                for (TeamMatch m : plugin.getTeamManager().getTeamMatches()) {
                    if (m.getGameType().canPlaceAndBreak() && m.arena.getName().equalsIgnoreCase(arena.getName())) {
                        for (Block b : m.getBlocks()) {
                            for (Player ply : t2.getTeam()) {
                                ply.sendBlockChange(b.getLocation(), Material.AIR, (byte) 0);
                            }
                        }
                    }
                }
                List<Player> team1 = t1.getTeam();
                List<Player> team2 = t2.getTeam();
                for (Player ply : t1.getTeam()) {
                    ply.teleport(arena.getSpawn1());
                    for (Player ply2 : Bukkit.getOnlinePlayers()) {
                        if (! team1.contains(ply2) && ! team2.contains(ply2)) {
                            ply.hidePlayer(ply2);
                            ply2.hidePlayer(ply);
                        }
                    }
                    remaining.put(ply, "team1");
                }
                for (Player ply : t2.getTeam()) {
                    ply.teleport(arena.getSpawn2());
                    for (Player ply2 : Bukkit.getOnlinePlayers()) {
                        if (! team1.contains(ply2) && ! team2.contains(ply2)) {
                            ply.hidePlayer(ply2);
                            ply2.hidePlayer(ply);
                        }
                    }
                    remaining.put(ply, "team2");
                }
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        for (Player pl1 : team1) {
                            for (Player pl2 : team2) {
                                pl1.showPlayer(pl2);
                                pl2.showPlayer(pl1);
                            }
                        }
                    }
                }.runTaskLater(this.plugin, 5);
                if (gt != null && gt.getStartingKit() != null && gt.getStartingKit().getInv() != null && gt.getStartingKit().getInv().getContents() != null && gt.getStartingKit().getInv().getArmorContents() != null) {
                    for (Player ply : team1) {
                        ply.sendMessage(ChatColor.YELLOW + "Starting team match against " + ChatColor.GREEN + t2.getLeader().getName() + "'s " + ChatColor.YELLOW + "team.");
                        ply.getInventory().clear();
                        ply.getInventory().setContents(gt.getStartingKit().getInv().getContents());
                        ply.getInventory().setArmorContents(gt.getStartingKit().getInv().getArmorContents());
                    }
                    for (Player ply : team2) {
                        ply.sendMessage(ChatColor.YELLOW + "Starting team match against " + ChatColor.GREEN + t1.getLeader().getName() + "'s " + ChatColor.YELLOW + "team.");
                        ply.getInventory().clear();
                        ply.getInventory().setContents(gt.getStartingKit().getInv().getContents());
                        ply.getInventory().setArmorContents(gt.getStartingKit().getInv().getArmorContents());
                    }
                }
                ArrayList<Player> all = new ArrayList<Player>(team1);
                all.addAll(team2);
                new BukkitRunnable() {
                    private int i = 5;

                    @Override
                    public void run() {
                        if (i == 0) {
                            this.cancel();
                            started = true;
                            for (Player ply : all) {
                                ply.sendMessage(ChatColor.GREEN + "Duel starting now!");
                                ply.playSound(ply.getLocation(), Sound.NOTE_PIANO, 10.0f, 2.0f);
                            }
                            return;
                        }
                        for (Player ply : all) {
                            ply.sendMessage(ChatColor.GREEN + "Starting in " + ChatColor.YELLOW + this.i + ChatColor.GREEN + " seconds!");
                            ply.playSound(ply.getLocation(), Sound.NOTE_PIANO, 10.0f, 1.0f);
                        }
                        -- this.i;
                    }
                }.runTaskTimer(this.plugin, 0, 20);
                break;
            }
        }
    }

    public void endMatch(List<Player> winners) {
        if (getGameType().canPlaceAndBreak()) {
            undoBuilds();
        }
        switch (type) {
            case FFA: {
                if (players != null) {
                    for (Player ply : players) {
                        if (ply == null) {
                            ply.spigot().respawn();
                        }
                    }
                    plugin.getTeamManager().getTeam(players.get(0)).setInMatch(false);
                    plugin.getTeamManager().getTeam(players.get(0)).setMatch(null);
                    for (Player ply : players) {
                        if (!plugin.getTeamManager().hasTeam(ply)) {
                            plugin.getInventorySetter().setupInventory(InventoryType.DEFAULT, ply);
                        } else {
                            if (plugin.getTeamManager().getTeamByLeader(ply) != null) {
                                plugin.getInventorySetter().setupInventory(InventoryType.PARTY_LEADER, ply);
                            } else {
                                if (plugin.getTeamManager().getTeamByPlayer(ply) != null && plugin.getTeamManager().getTeamByPlayer(ply).getLeader() != null) {
                                    plugin.getInventorySetter().setupInventory(InventoryType.PARTY_MEMBER, ply);
                                } else {
                                    plugin.getInventorySetter().setupInventory(InventoryType.DEFAULT, ply);
                                }
                            }
                        }
                        ply.teleport(plugin.getSpawn());
                        ply.setHealth(20);
                        ply.setFoodLevel(20);
                        ply.setLevel(0);
                        for (final PotionEffectType type : PotionEffectType.values()) {
                            if (type != null && ply.hasPotionEffect(type)) {
                                ply.removePotionEffect(type);
                            }
                        }
                        ply.getActivePotionEffects().clear();
                    }
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            for (final Player ply : Bukkit.getOnlinePlayers()) {
                                if (ply != null) {
                                    for (Player pl : players) {
                                        if (pl != null && pl != ply && !plugin.getConfig().getBoolean("hide-players")) {
                                            pl.showPlayer(ply);
                                            ply.showPlayer(pl);
                                        } else {
                                            pl.hidePlayer(ply);
                                            ply.hidePlayer(pl);
                                        }
                                    }
                                }
                            }
                        }
                    }.runTaskLater(this.plugin, 5L);
                    for (Player ply : players) {
                        ply.sendMessage(ChatColor.YELLOW + "Winner: " + winners.get(0).getName());
                        JsonBuilder jb = new JsonBuilder("");
                        jb.withText("Inventories (click to view): ").withColor(ChatColor.GOLD);
                        Iterator<Player> plIterator = players.iterator();
                        while (plIterator.hasNext()) {
                            Player pl = plIterator.next();
                            jb.withText(((OfflinePlayer)pl).getName() + (plIterator.hasNext() ? ", " : ".")).withColor(ChatColor.YELLOW).withClickEvent(JsonBuilder.ClickAction.RUN_COMMAND, "/inventory " + ((OfflinePlayer)pl).getName()).withHoverEvent(JsonBuilder.HoverAction.SHOW_TEXT, ChatColor.GREEN + "CLick to view inventory");
                        }
                        jb.sendJson(ply);
                    }
                    break;
                }
            }
            case PARTYvPARTY: {
                List<Player> all = new ArrayList<>(t1.getTeam());
                all.addAll(t2.getTeam());
                for (Player ply : all) {
                    if (ply == null) {
                        ply.spigot().respawn();
                    }
                }
                t1.setInMatch(false);
                t1.setMatch(null);
                t2.setInMatch(false);
                t2.setMatch(null);
                for (Player ply : t1.getTeam()) {
                    if (!plugin.getTeamManager().hasTeam(ply)) {
                        plugin.getInventorySetter().setupInventory(InventoryType.DEFAULT, ply);
                    } else {
                        if (plugin.getTeamManager().getTeamByLeader(ply) != null) {
                            plugin.getInventorySetter().setupInventory(InventoryType.PARTY_LEADER, ply);
                        } else {
                            if (plugin.getTeamManager().getTeamByPlayer(ply) != null && plugin.getTeamManager().getTeamByPlayer(ply).getLeader() != null) {
                                plugin.getInventorySetter().setupInventory(InventoryType.PARTY_MEMBER, ply);
                            } else {
                                plugin.getInventorySetter().setupInventory(InventoryType.DEFAULT, ply);
                            }
                        }
                    }
                }
                for (Player ply : t2.getTeam()) {
                    if (!plugin.getTeamManager().hasTeam(ply)) {
                        plugin.getInventorySetter().setupInventory(InventoryType.DEFAULT, ply);
                    } else {
                        if (plugin.getTeamManager().getTeamByLeader(ply) != null) {
                            plugin.getInventorySetter().setupInventory(InventoryType.PARTY_LEADER, ply);
                        } else {
                            if (plugin.getTeamManager().getTeamByPlayer(ply) != null && plugin.getTeamManager().getTeamByPlayer(ply).getLeader() != null) {
                                plugin.getInventorySetter().setupInventory(InventoryType.PARTY_MEMBER, ply);
                            } else {
                                plugin.getInventorySetter().setupInventory(InventoryType.DEFAULT, ply);
                            }
                        }
                    }
                }
                for (Player ply : all) {
                    ply.teleport(plugin.getSpawn());
                    ply.setHealth(20);
                    ply.setFoodLevel(20);
                    ply.setLevel(0);
                    for (final PotionEffectType type : PotionEffectType.values()) {
                        if (type != null && ply.hasPotionEffect(type)) {
                            ply.removePotionEffect(type);
                        }
                    }
                    ply.getActivePotionEffects().clear();
                }
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        for (final Player ply : Bukkit.getOnlinePlayers()) {
                            if (ply != null) {
                                for (Player pl : all) {
                                    if (pl != ply && !plugin.getConfig().getBoolean("hide-players")) {
                                        pl.showPlayer(ply);
                                        ply.showPlayer(pl);
                                    } else {
                                        pl.hidePlayer(ply);
                                        ply.hidePlayer(pl);
                                    }
                                }
                            }
                        }
                    }
                }.runTaskLater(this.plugin, 5L);
                for (Player ply : all) {
                    String s = ChatColor.YELLOW + "Winners: ";
                    for (Player pr : winners) {
                        s += pr.getName() + ",";
                    }
                    String s2 = s.substring(0, s.length() - 1);
                    ply.sendMessage(s2);
                    JsonBuilder jb = new JsonBuilder("").withText("Inventories (click to view): ").withColor(ChatColor.GOLD);
                    Iterator<Player> plIterator = all.iterator();
                    while (plIterator.hasNext()) {
                        Player pl = plIterator.next();
                        jb.withText(((OfflinePlayer)pl).getName() + (plIterator.hasNext() ? ", " : ".")).withColor(ChatColor.YELLOW).withClickEvent(JsonBuilder.ClickAction.RUN_COMMAND, "/inventory " + ((OfflinePlayer)pl).getName()).withHoverEvent(JsonBuilder.HoverAction.SHOW_TEXT, ChatColor.GREEN + "Click to view inventory");
                    }
                    jb.sendJson(ply);
                }
                break;
            }
            case TEAMSPLIT: {
                List<Player> all = t1.getTeam();
                for (Player ply : all) {
                    if (ply == null) {
                        ply.spigot().respawn();
                    }
                }
                t1.setInMatch(false);
                t1.setMatch(null);
                for (Player ply : t1.getTeam()) {
                    if (!plugin.getTeamManager().hasTeam(ply)) {
                        plugin.getInventorySetter().setupInventory(InventoryType.DEFAULT, ply);
                    } else {
                        if (plugin.getTeamManager().getTeamByLeader(ply) != null) {
                            plugin.getInventorySetter().setupInventory(InventoryType.PARTY_LEADER, ply);
                        } else {
                            if (plugin.getTeamManager().getTeamByPlayer(ply) != null && plugin.getTeamManager().getTeamByPlayer(ply).getLeader() != null) {
                                plugin.getInventorySetter().setupInventory(InventoryType.PARTY_MEMBER, ply);
                            } else {
                                plugin.getInventorySetter().setupInventory(InventoryType.DEFAULT, ply);
                            }
                        }
                    }
                }
                Collections.reverse(t1.getTeam());
                Collections.rotate(t1.getTeam(), new Random().nextInt(t1.getTeam().size()));
                for (Player ply : all) {
                    ply.teleport(plugin.getSpawn());
                    ply.setHealth(20);
                    ply.setFoodLevel(20);
                    ply.setLevel(0);
                    for (final PotionEffectType type : PotionEffectType.values()) {
                        if (type != null && ply.hasPotionEffect(type)) {
                            ply.removePotionEffect(type);
                        }
                    }
                    ply.getActivePotionEffects().clear();
                }
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        for (final Player ply : Bukkit.getOnlinePlayers()) {
                            if (ply != null) {
                                for (Player pl : all) {
                                    if (pl != ply && plugin.getConfig().getBoolean("hide-players")) {
                                        pl.showPlayer(ply);
                                        ply.showPlayer(pl);
                                    } else {
                                        pl.hidePlayer(ply);
                                        ply.hidePlayer(pl);
                                    }
                                }
                            }
                        }
                    }
                }.runTaskLater(this.plugin, 5L);
                for (Player ply : t1.getTeam()) {
                    if (plugin.getTeamManager().getTeamByLeader(ply) != null) {
                        plugin.getInventorySetter().setupInventory(InventoryType.PARTY_LEADER, ply);
                    } else if (plugin.getTeamManager().getTeamByPlayer(ply) != null) {
                        if (plugin.getTeamManager().getTeamByPlayer(ply).getLeader() == null) {
                            plugin.getInventorySetter().setupInventory(InventoryType.DEFAULT, ply);
                        } else {
                            plugin.getInventorySetter().setupInventory(InventoryType.PARTY_MEMBER, ply);

                        }
                    }
                }
                for (Player ply : all) {
                    String s = ChatColor.YELLOW + "Winners: ";
                    for (Player pr : winners) {
                        s += pr.getName() + ",";
                    }
                    String s2 = s.substring(0, s.length() - 1);
                    ply.sendMessage(s2);
                    JsonBuilder jb = new JsonBuilder("").withText("Inventories (click to view): ").withColor(ChatColor.GOLD);
                    Iterator<Player> iter = all.iterator();
                    while (iter.hasNext()) {
                        Player pl = iter.next();
                        jb.withText(((OfflinePlayer)pl).getName() + (iter.hasNext() ? ", " : ".")).withColor(ChatColor.YELLOW).withClickEvent(JsonBuilder.ClickAction.RUN_COMMAND, "/inventory " + ((OfflinePlayer)pl).getName()).withHoverEvent(JsonBuilder.HoverAction.SHOW_TEXT, ChatColor.GREEN + "Click to view inventory");
                    }
                    jb.sendJson(ply);
                }
                break;
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        switch (type) {
            case FFA: {
                if (remaining.containsKey(e.getPlayer())) {
                    remaining.remove(e.getPlayer());
                }
                if (remaining.size() == 1) {
                    for (Player ply : spectators) {
                        removeSpectator(ply);
                    }
                    e.setQuitMessage(e.getPlayer().getName() + " died");
                    e.getPlayer().getInventory().clear();
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            endMatch(toList(remaining.keySet()));
                            started = false;
                        }
                    }.runTaskLater(this.plugin, 2L);
                }
            }
            case TEAMSPLIT: {
                List<Player> team1 = new ArrayList<>();
                List<Player> team2 = new ArrayList<>();
                for (int i = 0; i < players.size(); i++) {
                    if ((i & 1) == 0) {
                        team1.add(players.get(i));
                    } else {
                        team2.add(players.get(i));
                    }
                }
                if (remaining.containsKey(e.getPlayer())) {
                    remaining.remove(e.getPlayer());
                }
                if (t1.getTeam().containsAll(remaining.keySet())) {
                    for (Player ply : spectators) {
                        removeSpectator(ply);
                    }
                    e.setQuitMessage(e.getPlayer().getName() + " died");
                    e.getPlayer().getInventory().clear();
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            endMatch(toList(remaining.keySet()));
                            started = false;
                        }
                    }.runTaskLater(this.plugin, 2L);
                }
            }
            case PARTYvPARTY: {
                if (remaining.containsKey(e.getPlayer())) {
                    remaining.remove(e.getPlayer());
                }
                if (t1.getTeam().containsAll(remaining.keySet())) {
                    for (Player ply : spectators) {
                        removeSpectator(ply);
                    }
                    e.setQuitMessage(e.getPlayer().getName() + " died");
                    e.getPlayer().getInventory().clear();
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            endMatch(toList(remaining.keySet()));
                            started = false;
                        }
                    }.runTaskLater(this.plugin, 2L);
                } else if (t2.getTeam().containsAll(remaining.keySet())) {
                    for (Player ply : spectators) {
                        removeSpectator(ply);
                    }
                    e.setQuitMessage(e.getPlayer().getName() + " died");
                    e.getPlayer().getInventory().clear();
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            endMatch(toList(remaining.keySet()));
                            started = false;
                        }
                    }.runTaskLater(this.plugin, 2L);
                }
            }
        }
    }

    public ArrayList<Player> getAllPlayers() {
        ArrayList<Player> ar = new ArrayList<Player>();
        switch (type) {
            case FFA: {
                ar.addAll(players);
                break;
            }
            case TEAMSPLIT: {
                ar.addAll(t1.getTeam());
                break;
            }
            case PARTYvPARTY: {
                ar.addAll(t1.getTeam());
                ar.addAll(t2.getTeam());
                break;
            }
        }
        return ar;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player) {
            if (spectators.contains((Player)e.getDamager())) {
                e.setCancelled(true);
            } else {
                if (e.getEntity() instanceof Player) {
                    Player p1 = (Player)e.getDamager();
                    Player p2 = (Player)e.getEntity();
                    switch (type) {
                        case PARTYvPARTY: {
                            if (t1.getTeam().contains(p1) && t1.getTeam().contains(p2)) {
                                e.setCancelled(true);
                            } else if (t2.getTeam().contains(p2) && t2.getTeam().contains(p1)) {
                                e.setCancelled(true);
                            } else {
                                e.setCancelled(false);
                            }
                            break;
                        }
                        case FFA: {
                            e.setCancelled(false);
                            break;
                        }
                        case TEAMSPLIT: {
                            List<Player> team1 = new ArrayList<>();
                            List<Player> team2 = new ArrayList<>();
                            for (int i = 0; i < t1.getTeam().size(); i++) {
                                if ((i & 1) == 0) {
                                    team1.add(t1.getTeam().get(i));
                                } else {
                                    team2.add(t1.getTeam().get(i));
                                }
                            }
                            if (team1.contains(p1) && team1.contains(p2)) {
                                e.setCancelled(true);
                            } else if (team2.contains(p1) && team2.contains(p2)) {
                                e.setCancelled(true);
                            } else {
                                e.setCancelled(false);
                            }
                            break;
                        }
                    }
                }
            }
        }
        if (e.getEntity() instanceof Player) {
            this.plugin.getInventoryManager().storeInv((Player) e.getEntity(), ((Player) e.getEntity()).getHealth() < e.getDamage());
        }
        if (e.getDamager() instanceof Player) {
            this.plugin.getInventoryManager().storeInv((Player) e.getDamager(), false);
        }
        if (e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
            if (!started) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        switch (type) {
            case FFA: {
                if (remaining.containsKey(e.getEntity())) {
                    e.setDeathMessage(e.getEntity().getName() + " was killed by " + e.getEntity().getKiller().getName());
                    remaining.remove(e.getEntity());
                }
                if (remaining.size() == 1) {
                    for (Player ply : spectators) {
                        removeSpectator(ply);
                    }
                    e.getDrops().clear();
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            endMatch(toList(remaining.keySet()));
                            e.getEntity().spigot().respawn();
                            started = false;
                        }
                    }.runTaskLater(this.plugin, 2L);
                } else {
                    setSpectator(e.getEntity());
                }
                break;
            }
            case TEAMSPLIT: {
                List<Player> team1 = new ArrayList<>();
                List<Player> team2 = new ArrayList<>();
                for (int i = 0; i < t1.getTeam().size(); i++) {
                    if ((i & 1) == 0) {
                        team1.add(t1.getTeam().get(i));
                    } else {
                        team2.add(t1.getTeam().get(i));
                    }
                }
                if (remaining.containsKey(e.getEntity())) {
                    remaining.remove(e.getEntity());
                    e.setDeathMessage(e.getEntity().getName() + " was killed by " + e.getEntity().getKiller().getName());
                }
                if (t1 != null && t1.getTeam() != null && t1.getTeam().containsAll(remaining.keySet())) {
                    for (Player ply : spectators) {
                        removeSpectator(ply);
                    }
                    e.getDrops().clear();
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            endMatch(toList(remaining.keySet()));
                            e.getEntity().spigot().respawn();
                            started = false;
                        }
                    }.runTaskLater(this.plugin, 2L);
                } else {
                    setSpectator(e.getEntity());
                }
                break;
            }
            case PARTYvPARTY: {
                if (remaining.containsKey(e.getEntity())) {
                    e.setDeathMessage(e.getEntity().getName() + " was killed by " + e.getEntity().getKiller().getName());
                    remaining.remove(e.getEntity());
                }
                if (t1 != null && t1.getTeam() != null && t1.getTeam().containsAll(remaining.keySet())) {
                    for (Player ply : spectators) {
                        removeSpectator(ply);
                    }
                    e.getDrops().clear();;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            endMatch(toList(remaining.keySet()));
                            e.getEntity().spigot().respawn();
                            started = false;
                        }
                    }.runTaskLater(this.plugin, 2L);
                } else if (t2 != null && t2.getTeam() != null && t2.getTeam().containsAll(remaining.keySet())) {
                    for (Player ply : spectators) {
                        removeSpectator(ply);
                    }
                    e.getDrops().clear();
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            endMatch(toList(remaining.keySet()));
                            e.getEntity().spigot().respawn();
                            started = false;
                        }
                    }.runTaskLater(this.plugin, 2L);
                } else {
                    setSpectator(e.getEntity());
                }
                break;
            }
        }
    }

    @EventHandler
    public void onSplash(PotionSplashEvent e) {
        switch (type) {
            case FFA: {
                if (players.contains(e.getEntity().getShooter())) {
                    for (Player ply : players) {
                        e.getAffectedEntities().stream().filter(entity -> entity != ply).forEach(entity -> e.getAffectedEntities().remove(entity));
                        e.setCancelled(true);
                        e.getAffectedEntities().stream().filter(entity -> entity == ply).forEach(entity -> entity.addPotionEffects(e.getEntity().getEffects()));
                    }
                }
            }
            case TEAMSPLIT: {
                if (t1.getTeam().contains(e.getEntity().getShooter())) {
                    for (Player ply : t1.getTeam()) {
                        e.getAffectedEntities().stream().filter(entity -> entity != ply).forEach(entity -> e.getAffectedEntities().remove(entity));
                        e.setCancelled(true);
                        e.getAffectedEntities().stream().filter(entity -> entity == ply).forEach(entity -> entity.addPotionEffects(e.getEntity().getEffects()));
                    }
                }
            }
            case PARTYvPARTY: {
                if (t1.getTeam().contains(e.getEntity().getShooter())) {
                    for (Player ply : t1.getTeam()) {
                        e.getAffectedEntities().stream().filter(entity -> entity != ply).forEach(entity -> e.getAffectedEntities().remove(entity));
                        e.setCancelled(true);
                        e.getAffectedEntities().stream().filter(entity -> entity == ply).forEach(entity -> entity.addPotionEffects(e.getEntity().getEffects()));
                    }
                } else if (t2.getTeam().contains(e.getEntity().getShooter())) {
                    for (Player ply : t2.getTeam()) {
                        e.getAffectedEntities().stream().filter(entity -> entity != ply).forEach(entity -> e.getAffectedEntities().remove(entity));
                        e.setCancelled(true);
                        e.getAffectedEntities().stream().filter(entity -> entity == ply).forEach(entity -> entity.addPotionEffects(e.getEntity().getEffects()));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onThrowItem(ProjectileLaunchEvent e) {
        EntityHider hider = plugin.getEntityHider();
        if (getAllPlayers().contains((Player)e.getEntity().getShooter())) {
            for (Player ply : Bukkit.getOnlinePlayers()) {
                if (!getAllPlayers().contains(ply)) {
                    hider.hideEntity(ply, e.getEntity());
                }
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        switch (type) {
            case FFA: {
                if (players != null) {
                    for (Player ply : players) {
                        if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) && event.getItem() != null && event.getItem().getType() == Material.ENDER_PEARL && (event.getPlayer() == ply)) {
                            final Player shooter = event.getPlayer();
                            if (! this.started) {
                                event.setCancelled(true);
                                shooter.sendMessage(ChatColor.RED + "You can't use that before the duel!");
                            } else if (this.counters.containsKey(shooter)) {
                                shooter.sendMessage(ChatColor.RED + "You are on cooldown for " + this.counters.get(shooter).getCooldown() + " more seconds.");
                                event.setCancelled(true);
                            } else {
                                final PearlCounter counter = new PearlCounter(shooter, this);
                                counter.runTaskTimer((Plugin) this.plugin, 0L, 20L);
                                this.counters.put(shooter, counter);
                            }
                        } else if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                            if (event.getItem().getType() == Material.ENCHANTED_BOOK) {
                                final String kitName = ChatColor.stripColor(event.getItem().getItemMeta().getDisplayName());
                                if (kitName.startsWith("Default")) {
                                    if (plugin.getTeamManager().getTeam(ply) != null && plugin.getTeamManager().getTeam(ply).isInMatch()) {
                                        Kit startingKit = plugin.getTeamManager().getTeam(ply).getTeamMatch().getGameType().getStartingKit();
                                        if (startingKit.getInv() != null && startingKit.getInv().getArmorContents() != null) {
                                            ply.getInventory().setArmorContents(startingKit.getInv().getArmorContents());
                                        }
                                        if (startingKit.getInv() != null && startingKit.getInv().getContents() != null) {
                                            ply.getInventory().setContents(startingKit.getInv().getContents());
                                        }
                                    }
                                } else {
                                    final Kit kit = this.plugin.getPlayerDataManager().getKit(ply, this.plugin.getTeamManager().getTeam(ply).getTeamMatch().getGameType(), event.getPlayer().getInventory().getHeldItemSlot());
                                    if (kit == null) {
                                        Bukkit.broadcastMessage("uhhh");
                                    }
                                    if (kit.getInv() != null && kit.getInv().getArmorContents() != null) {
                                        ply.getInventory().setArmorContents(kit.getInv().getArmorContents());
                                    }
                                    if (kit.getInv() != null && kit.getInv().getContents() != null) {
                                        ply.getInventory().setContents(kit.getInv().getContents());
                                    }
                                }
                                ply.updateInventory();
                            }
                        }
                    }
                }
            }
            case TEAMSPLIT: {
                if (t1 != null && t1.getTeam() != null) {
                    for (Player ply : t1.getTeam()) {
                        if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) && event.getItem() != null && event.getItem().getType() == Material.ENDER_PEARL && (event.getPlayer() == ply)) {
                            final Player shooter = event.getPlayer();
                            if (! this.started) {
                                event.setCancelled(true);
                                shooter.sendMessage(ChatColor.RED + "You can't use that before the duel!");
                            } else if (this.counters.containsKey(shooter)) {
                                shooter.sendMessage(ChatColor.RED + "You are on cooldown for " + this.counters.get(shooter).getCooldown() + " more seconds.");
                                event.setCancelled(true);
                            } else {
                                final PearlCounter counter = new PearlCounter(shooter, this);
                                counter.runTaskTimer((Plugin) this.plugin, 0L, 20L);
                                this.counters.put(shooter, counter);
                            }
                        } else if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                            if (event.getItem().getType() == Material.ENCHANTED_BOOK) {
                                final String kitName = ChatColor.stripColor(event.getItem().getItemMeta().getDisplayName());
                                if (kitName.startsWith("Default")) {
                                    if (plugin.getTeamManager().getTeam(ply) != null && plugin.getTeamManager().getTeam(ply).isInMatch()) {
                                        Kit startingKit = plugin.getTeamManager().getTeam(ply).getTeamMatch().getGameType().getStartingKit();
                                        if (startingKit.getInv() != null && startingKit.getInv().getArmorContents() != null) {
                                            ply.getInventory().setArmorContents(startingKit.getInv().getArmorContents());
                                        }
                                        if (startingKit.getInv() != null && startingKit.getInv().getContents() != null) {
                                            ply.getInventory().setContents(startingKit.getInv().getContents());
                                        }
                                    }
                                } else {
                                    final Kit kit = this.plugin.getPlayerDataManager().getKit(ply, this.plugin.getTeamManager().getTeam(ply).getTeamMatch().getGameType(), event.getPlayer().getInventory().getHeldItemSlot());
                                    if (kit == null) {
                                        Bukkit.broadcastMessage("uhhh");
                                    }
                                    if (kit.getInv() != null && kit.getInv().getArmorContents() != null) {
                                        ply.getInventory().setArmorContents(kit.getInv().getArmorContents());
                                    }
                                    if (kit.getInv() != null && kit.getInv().getContents() != null) {
                                        ply.getInventory().setContents(kit.getInv().getContents());
                                    }
                                }
                                ply.updateInventory();
                            }
                        }
                    }
                }
            }
            case PARTYvPARTY: {
                if (t1 != null && t1.getTeam() != null && t2 != null && t2.getTeam() != null) {
                    for (Player ply : t1.getTeam()) {
                        if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) && event.getItem() != null && event.getItem().getType() == Material.ENDER_PEARL && (event.getPlayer() == ply)) {
                            final Player shooter = event.getPlayer();
                            if (! this.started) {
                                event.setCancelled(true);
                                shooter.sendMessage(ChatColor.RED + "You can't use that before the duel!");
                            } else if (this.counters.containsKey(shooter)) {
                                shooter.sendMessage(ChatColor.YELLOW + "Pearl cooldown: " + ChatColor.RED + this.counters.get(shooter).getCooldown() + " seconds");
                                event.setCancelled(true);
                            } else {
                                final PearlCounter counter = new PearlCounter(shooter, this);
                                counter.runTaskTimer((Plugin) this.plugin, 0L, 20L);
                                this.counters.put(shooter, counter);
                            }
                        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
                            if (event.getItem().getType() == Material.ENCHANTED_BOOK) {
                                final String kitName = ChatColor.stripColor(event.getItem().getItemMeta().getDisplayName());
                                if (kitName.startsWith("Default")) {
                                    if (plugin.getTeamManager().getTeam(ply) != null && plugin.getTeamManager().getTeam(ply).isInMatch()) {
                                        Kit startingKit = plugin.getTeamManager().getTeam(ply).getTeamMatch().getGameType().getStartingKit();
                                        if (startingKit.getInv() != null && startingKit.getInv().getArmorContents() != null) {
                                            ply.getInventory().setArmorContents(startingKit.getInv().getArmorContents());
                                        }
                                        if (startingKit.getInv() != null && startingKit.getInv().getContents() != null) {
                                            ply.getInventory().setContents(startingKit.getInv().getContents());
                                        }
                                    }
                                } else {
                                    final Kit kit = this.plugin.getPlayerDataManager().getKit(ply, this.plugin.getTeamManager().getTeam(ply).getTeamMatch().getGameType(), event.getPlayer().getInventory().getHeldItemSlot());
                                    if (kit == null) {
                                        Bukkit.broadcastMessage("uhhh");
                                    }
                                    if (kit.getInv() != null && kit.getInv().getArmorContents() != null) {
                                        ply.getInventory().setArmorContents(kit.getInv().getArmorContents());
                                    }
                                    if (kit.getInv() != null && kit.getInv().getContents() != null) {
                                        ply.getInventory().setContents(kit.getInv().getContents());
                                    }
                                }
                                ply.updateInventory();
                            }
                        }
                    }
                    for (Player ply : t2.getTeam()) {
                        if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) && event.getItem() != null && event.getItem().getType() == Material.ENDER_PEARL && (event.getPlayer() == ply)) {
                            final Player shooter = event.getPlayer();
                            if (! this.started) {
                                event.setCancelled(true);
                                shooter.sendMessage(ChatColor.RED + "You can't use that before the duel!");
                            } else if (this.counters.containsKey(shooter)) {
                                shooter.sendMessage(ChatColor.RED + "You are on cooldown for " + this.counters.get(shooter).getCooldown() + " more seconds.");
                                event.setCancelled(true);
                            } else {
                                final PearlCounter counter = new PearlCounter(shooter, this);
                                counter.runTaskTimer((Plugin) this.plugin, 0L, 20L);
                                this.counters.put(shooter, counter);
                            }
                        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
                            if (event.getItem().getType() == Material.ENCHANTED_BOOK) {
                                final String kitName = ChatColor.stripColor(event.getItem().getItemMeta().getDisplayName());
                                if (kitName.startsWith("Default")) {
                                    if (plugin.getTeamManager().getTeam(ply) != null && plugin.getTeamManager().getTeam(ply).isInMatch()) {
                                        Kit startingKit = plugin.getTeamManager().getTeam(ply).getTeamMatch().getGameType().getStartingKit();
                                        if (startingKit.getInv() != null && startingKit.getInv().getArmorContents() != null) {
                                            ply.getInventory().setArmorContents(startingKit.getInv().getArmorContents());
                                        }
                                        if (startingKit.getInv() != null && startingKit.getInv().getContents() != null) {
                                            ply.getInventory().setContents(startingKit.getInv().getContents());
                                        }
                                    }
                                } else {
                                    final Kit kit = this.plugin.getPlayerDataManager().getKit(ply, this.plugin.getTeamManager().getTeam(ply).getTeamMatch().getGameType(), event.getPlayer().getInventory().getHeldItemSlot());
                                    if (kit == null) {
                                        Bukkit.broadcastMessage("uhhh");
                                    }
                                    if (kit.getInv() != null && kit.getInv().getArmorContents() != null) {
                                        ply.getInventory().setArmorContents(kit.getInv().getArmorContents());
                                    }
                                    if (kit.getInv() != null && kit.getInv().getContents() != null) {
                                        ply.getInventory().setContents(kit.getInv().getContents());
                                    }
                                }
                                ply.updateInventory();
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (spectators.contains(event.getPlayer())) {
            event.setCancelled(true);
        }
        switch (type) {
            case FFA: {
                for (Player ply1 : players) {
                    if (event.getPlayer() == ply1) {
                        final EntityHider hider = this.plugin.getEntityHider();
                        for (final Player ply : Bukkit.getOnlinePlayers()) {
                            if (ply != ply1) {
                                hider.hideEntity(ply, (Entity) event.getItemDrop());
                            }
                        }
                        new BukkitRunnable() {
                            public void run() {
                                event.getItemDrop().remove();
                            }
                        }.runTaskLater((Plugin) this.plugin, 60L);
                    }
                }
            }
            case TEAMSPLIT: {
                for (Player ply1 : t1.getTeam()) {
                    if (event.getPlayer() == ply1) {
                        final EntityHider hider = this.plugin.getEntityHider();
                        for (final Player ply : Bukkit.getOnlinePlayers()) {
                            if (ply != ply1) {
                                hider.hideEntity(ply, (Entity) event.getItemDrop());
                            }
                        }
                        new BukkitRunnable() {
                            public void run() {
                                event.getItemDrop().remove();
                            }
                        }.runTaskLater((Plugin) this.plugin, 60L);
                    }
                }
            }
            case PARTYvPARTY: {
                for (Player ply1 : t1.getTeam()) {
                    if (event.getPlayer() == ply1) {
                        final EntityHider hider = this.plugin.getEntityHider();
                        for (final Player ply : Bukkit.getOnlinePlayers()) {
                            if (ply != ply1) {
                                hider.hideEntity(ply, (Entity) event.getItemDrop());
                            }
                        }
                        new BukkitRunnable() {
                            public void run() {
                                event.getItemDrop().remove();
                            }
                        }.runTaskLater((Plugin) this.plugin, 60L);
                    }
                }
                for (Player ply1 : t2.getTeam()) {
                    if (event.getPlayer() == ply1) {
                        final EntityHider hider = this.plugin.getEntityHider();
                        for (final Player ply : Bukkit.getOnlinePlayers()) {
                            if (ply != ply1) {
                                hider.hideEntity(ply, (Entity) event.getItemDrop());
                            }
                        }
                        new BukkitRunnable() {
                            public void run() {
                                event.getItemDrop().remove();
                            }
                        }.runTaskLater((Plugin) this.plugin, 60L);
                    }
                }
            }
        }
    }

    public List<Player> toList(Set<Player> players) {
        ArrayList<Player> plys = new ArrayList<>();
        plys.addAll(players);
        return plys;
    }

    public class PearlCounter extends BukkitRunnable {
        private int counter;
        private Player ply;
        private TeamMatch match;

        public PearlCounter(final Player ply, final TeamMatch match) {
            this.ply = ply;
            this.counter = 16;
            this.match = match;
        }

        public void run() {
            this.ply.setLevel(this.counter);
            -- this.counter;
            if (this.counter < 0) {
                this.cancel();
                this.match.removeCounter(this);
            }
        }

        public int getCooldown() {
            return this.counter + 1;
        }
    }

    public void removeCounter(PearlCounter pc) {
        counters.remove(pc.ply);
    }

    public boolean hasPlayer(Player ply) {
        switch (type) {
            case FFA: {
                return players.contains(ply);
            }
            case TEAMSPLIT: {
                return t1.getTeam().contains(ply);
            }
            case PARTYvPARTY: {
                return (t1.getTeam().contains(ply) || t2.getTeam().contains(ply));
            }
            default: {
                return false;
            }
        }
    }

    public void setSpectator(Player ply) {
        switch (type) {
            case FFA: {
                for (Player pl2 : players) {
                    if (!ply.getName().equalsIgnoreCase(pl2.getName())) pl2.hidePlayer(ply);
                }
            }
            case TEAMSPLIT: {
                for (Player pl2 : t1.getTeam()) {
                    if (!ply.getName().equalsIgnoreCase(pl2.getName())) pl2.hidePlayer(ply);
                }
            }
            case PARTYvPARTY: {
                List<Player> all = new ArrayList<>();
                all.addAll(t1.getTeam());
                all.addAll(t2.getTeam());
                for (Player pl2 : all) {
                    if (!ply.getName().equalsIgnoreCase(pl2.getName())) pl2.hidePlayer(ply);
                }
            }
        }
        ply.setGameMode(GameMode.CREATIVE);
        ply.setFlying(true);
        spectators.add(ply);
        Dye dye = new Dye();
        dye.setColor(DyeColor.RED);
        ItemStack is = dye.toItemStack();
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(ChatColor.RED + "Leave spectator mode");
        is.setItemMeta(im);
        ply.getInventory().setItem(8, is);
    }

    public void removeSpectator(Player ply) {
        ply.getInventory().clear();
        ply.setGameMode(GameMode.SURVIVAL);
        ply.setFlying(false);
        spectators.remove(ply);
    }
}
