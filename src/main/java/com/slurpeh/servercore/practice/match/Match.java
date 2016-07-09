package com.slurpeh.servercore.practice.match;

import com.slurpeh.servercore.practice.KohiPractice;
import com.slurpeh.servercore.practice.arena.Arena;
import com.slurpeh.servercore.practice.gametype.GameType;
import com.slurpeh.servercore.practice.inventory.InventoryType;
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
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Dye;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Bradley on 5/15/16.
 */
public class Match implements Listener {
    //incomplete
    private Arena arena;
    private GameType gameType;
    private Player player1;
    private Player player2;
    private KohiPractice plugin;
    private boolean started;
    private boolean ranked;
    private HashMap<Player, PearlCounter> counters;
    public List<Player> spectators;
    public List<Block> blocks;

    public Match(final Arena arena, final GameType gameType, final Player p1, final Player p2, final boolean ranked) {
        this.arena = arena;
        this.gameType = gameType;
        this.player1 = p1;
        this.player2 = p2;
        this.started = false;
        this.ranked = ranked;
        this.spectators = new ArrayList<>();
        this.plugin = (KohiPractice) JavaPlugin.getPlugin((Class)KohiPractice.class);
        this.counters = new HashMap<Player, PearlCounter>();
        Bukkit.getPluginManager().registerEvents((Listener)this, (Plugin) this.plugin);
        this.blocks = new ArrayList<>();
    }

    public List<Block> getBlocks() {
        return blocks;
    }

    public void addBlock(Block b) {
        blocks.add(b);
    }

    public void removeBlock(Block b) {
        blocks.remove(b);
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
        for (Match m : plugin.getMatchManager().getMatches()) {
            if (m.hasBlock(e.getBlock())) {
                m.addBlock(e.getToBlock());
            }
        }
    }

    public void startMatch() {
        for (Match m : plugin.getMatchManager().getMatches()) {
            if (m.getGameType().canPlaceAndBreak() && m.arena.getName().equalsIgnoreCase(arena.getName())) {
                for (Block b : m.getBlocks()) {
                    player1.sendBlockChange(b.getLocation(), Material.AIR, (byte)0);
                    player2.sendBlockChange(b.getLocation(), Material.AIR, (byte)0);
                }
            }
        }
        this.player1.teleport(this.arena.getSpawn1());
        this.player2.teleport(this.arena.getSpawn2());
        for (final Player ply : Bukkit.getOnlinePlayers()) {
            if (ply != this.player1 && ply != this.player2) {
                ply.hidePlayer(this.player1);
                ply.hidePlayer(this.player2);
                this.player1.hidePlayer(ply);
                this.player2.hidePlayer(ply);
            }
        }
        new BukkitRunnable() {
            public void run() {
                Match.this.player1.showPlayer(Match.this.player2);
                Match.this.player2.showPlayer(Match.this.player1);
            }
        }.runTaskLater((Plugin)this.plugin, 5L);
        this.player1.sendMessage(ChatColor.YELLOW + "Starting duel against " + ChatColor.GREEN + this.player2.getName());
        this.player2.sendMessage(ChatColor.YELLOW + "Starting duel against " + ChatColor.GREEN + this.player1.getName());
        this.player1.getInventory().clear();
        this.player2.getInventory().clear();
        this.plugin.getInventoryManager().showKits(this.player1, this.gameType);
        this.plugin.getInventoryManager().showKits(this.player2, this.gameType);
        new BukkitRunnable() {
            private int i = 5;

            public void run() {
                if (this.i == 0) {
                    this.cancel();
                    Match.this.started = true;
                    for (final Player ply : new Player[] { Match.this.player1, Match.this.player2 }) {
                        if (ply == null) {
                            this.cancel();
                            return;
                        }
                        ply.sendMessage(ChatColor.GREEN + "Duel starting now!");
                        ply.playSound(ply.getLocation(), Sound.NOTE_PIANO, 10.0f, 2.0f);
                    }
                    return;
                }
                for (final Player ply : new Player[] { Match.this.player1, Match.this.player2 }) {
                    if (ply == null) {
                        this.cancel();
                        return;
                    }
                    ply.sendMessage(ChatColor.GREEN + "Starting in " + ChatColor.YELLOW + this.i + ChatColor.GREEN + " seconds!");
                    ply.playSound(ply.getLocation(), Sound.NOTE_PIANO, 10.0f, 1.0f);
                }
                --this.i;
            }
        }.runTaskTimer((Plugin)this.plugin, 0L, 20L);
    }

    public void endMatch(final Player winner) {
        for (Player ply : new Player[]{player1, player2}) {
            if (ply == null) {
                ply.spigot().respawn();
            }
        }
        if (getGameType().canPlaceAndBreak()) {
            undoBuilds();
        }
        for (final Player ply : new Player[] { this.player1, this.player2 }) {
            this.plugin.getInventorySetter().setupInventory(InventoryType.DEFAULT, ply);
            ply.teleport(this.plugin.getSpawn());
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
            public void run() {
                for (final Player ply : Bukkit.getOnlinePlayers()) {
                    if (ply != null && player1 != null && player2 != null) {
                        if (plugin.getConfig().getBoolean("hide-players")) {
                            ply.hidePlayer(player1);
                            ply.hidePlayer(player2);
                            player1.hidePlayer(ply);
                            player1.hidePlayer(player2);
                            player2.hidePlayer(ply);
                            player2.hidePlayer(ply);
                        } else {
                            ply.showPlayer(player1);
                            ply.showPlayer(player2);
                            player1.showPlayer(ply);
                            player1.showPlayer(player2);
                            player2.showPlayer(ply);
                            player2.showPlayer(player1);
                        }
                    }
                }
            }
        }.runTaskLater((Plugin)this.plugin, 5L);
        if (this.ranked) {
            final double p1 = this.plugin.getPlayerDataManager().getRating(this.player1, this.gameType);
            final double p2 = this.plugin.getPlayerDataManager().getRating(this.player2, this.gameType);
            int scoreChange = 0;
            final double expectedp1 = 1.0 / (1.0 + Math.pow(10.0, (p1 - p2) / 400.0));
            final double expectedp2 = 1.0 / (1.0 + Math.pow(10.0, (p2 - p1) / 400.0));
            Player loser;
            if (winner == this.player1) {
                scoreChange = (int)(expectedp1 * 32.0);
                loser = this.player2;
            }
            else {
                scoreChange = (int)(expectedp2 * 32.0);
                loser = this.player1;
            }
            scoreChange = ((scoreChange > 25) ? 25 : scoreChange);
            this.plugin.getPlayerDataManager().updateElo(winner, this.gameType, scoreChange, true);
            this.plugin.getPlayerDataManager().updateElo(loser, this.gameType, scoreChange, false);
            for (final Player ply2 : new Player[] { this.player1, this.player2 }) {
                ply2.sendMessage(ChatColor.YELLOW + "Winner: " + winner.getName());
                final JsonBuilder message = new JsonBuilder("").withText("Inventories (click to view): ").withColor(ChatColor.GOLD).withText(((OfflinePlayer)this.player1).getName() + ", ").withColor(ChatColor.YELLOW).withClickEvent(JsonBuilder.ClickAction.RUN_COMMAND, "/inventory " + ((OfflinePlayer)this.player1).getName()).withText(((OfflinePlayer)this.player2).getName()).withColor(ChatColor.YELLOW).withClickEvent(JsonBuilder.ClickAction.RUN_COMMAND, "/inventory " + ((OfflinePlayer)this.player2).getName());
                message.sendJson(ply2);
                ply2.sendMessage(ChatColor.YELLOW + "Elo Changes: " + ChatColor.GREEN + winner.getName() + " +" + scoreChange + " (" + this.plugin.getPlayerDataManager().getRating(winner, this.gameType) + ") " + ChatColor.RED + loser.getName() + " -" + scoreChange + " (" + this.plugin.getPlayerDataManager().getRating(loser, this.gameType) + ")");
            }
        }
        else {
            for (final Player ply : new Player[] { this.player1, this.player2 }) {
                ply.sendMessage(ChatColor.YELLOW + "Winner: " + winner.getName());
                final JsonBuilder message2 = new JsonBuilder("").withText("Inventories (click to view): ").withColor(ChatColor.GOLD).withText(((OfflinePlayer)this.player1).getName() + ", ").withColor(ChatColor.YELLOW).withClickEvent(JsonBuilder.ClickAction.RUN_COMMAND, "/inventory " + ((OfflinePlayer)this.player1).getName()).withText(((OfflinePlayer)this.player2).getName()).withColor(ChatColor.YELLOW).withClickEvent(JsonBuilder.ClickAction.RUN_COMMAND, "/inventory " + ((OfflinePlayer)this.player2).getName());
                message2.sendJson(ply);
            }
            KohiPractice.getInstance().getPlayerDataManager().getPlayerData(winner).setGamesPlayed(KohiPractice.getInstance().getPlayerDataManager().getPlayerData(winner).getGamesPlayed() + 1, true);
        }
        this.plugin.getMatchManager().endMatch(this);
        this.arena = null;
        this.gameType = null;
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        if (event.getPlayer() == this.player1) {
            this.endMatch(this.player2);
            started = false;
        }
        else if (event.getPlayer() == this.player2) {
            this.endMatch(this.player1);
            started = false;
        }
    }

    @EventHandler
    public void onDeath(final PlayerDeathEvent event) {
        if (this.player1 == event.getEntity() || this.player2 == event.getEntity()) {
            event.setDeathMessage((String)null);
            event.getDrops().clear();
            new BukkitRunnable() {
                public void run() {
                    event.getEntity().spigot().respawn();
                    if (event.getEntity() == Match.this.player1) {
                        Match.this.endMatch(Match.this.player2);
                    }
                    else {
                        Match.this.endMatch(Match.this.player1);
                    }
                    Match.this.started = false;
                }
            }.runTaskLater((Plugin)this.plugin, 2L);
        }
    }

    @EventHandler
    public void onDamage(final EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            if (spectators.contains((Player)event.getDamager())) {
                event.setCancelled(true);
            }
        }
        if (event.getEntity() instanceof Player && (this.player1 == event.getEntity() || this.player2 == event.getEntity())) {
            if (!this.started) {
                event.setCancelled(true);
            }
            else {
                if (event.getDamage() >= ((Player)event.getEntity()).getHealth()) {
                    this.plugin.getInventoryManager().storeInv(this.player1, event.getEntity() == this.player1);
                    this.plugin.getInventoryManager().storeInv(this.player2, event.getEntity() == this.player2);
                }
            }
        }
    }

    @EventHandler
    public void onItemDrop(final PlayerDropItemEvent event) {
        if (event.getPlayer() == this.player1 || event.getPlayer() == this.player2) {
            final EntityHider hider = this.plugin.getEntityHider();
            for (final Player ply : Bukkit.getOnlinePlayers()) {
                if (ply != this.player1 && ply != this.player2) {
                    hider.hideEntity(ply, (Entity)event.getItemDrop());
                }
            }
            new BukkitRunnable() {
                public void run() {
                    event.getItemDrop().remove();
                }
            }.runTaskLater((Plugin)this.plugin, 60L);
        }
    }

    @EventHandler
    public void onInteract(final PlayerInteractEvent event) {
        if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) && event.getItem() != null && event.getItem().getType() == Material.ENDER_PEARL && (event.getPlayer() == this.player1 || event.getPlayer() == this.player2)) {
            final Player shooter = event.getPlayer();
            if (!this.started) {
                event.setCancelled(true);
                shooter.sendMessage(ChatColor.RED + "You can't use that before the duel!");
            }
            else if (this.counters.containsKey(shooter)) {
                shooter.sendMessage(ChatColor.YELLOW + "Pearl cooldown: " + ChatColor.RED + this.counters.get(shooter).getCooldown() + " seconds");
                event.setCancelled(true);
            }
            else {
                final PearlCounter counter = new PearlCounter(shooter, this);
                counter.runTaskTimer((Plugin)this.plugin, 0L, 20L);
                this.counters.put(shooter, counter);
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
    public void onPotionSplashEvent(final PotionSplashEvent event) {
        if (event.getEntity().getShooter() == this.player1 || event.getEntity().getShooter() == this.player2) {
            event.getAffectedEntities().stream().filter(entity -> entity != this.player1 && entity != this.player2).forEach(entity -> event.getAffectedEntities().remove(entity));
            event.setCancelled(true);
            event.getAffectedEntities().stream().filter(entity -> entity == this.player1 || entity == this.player2).forEach(entity -> entity.addPotionEffects(event.getEntity().getEffects()));
        }
    }

    public ArrayList<Player> getAllPlayers() {
        ArrayList<Player> r = new ArrayList<>();
        r.add(player1);
        r.add(player2);
        return r;
    }

    public void removeCounter(final PearlCounter counter) {
        this.counters.remove(counter.ply);
    }

    public boolean hasPlayer(final Player ply) {
        return this.player1 == ply || this.player2 == ply;
    }

    public GameType getGameType() {
        return this.gameType;
    }

    public Player[] getPlayers() {
        return new Player[] { this.player1, this.player2 };
    }

    public void setRanked(boolean ranked) {
        this.ranked = ranked;
    }

    public boolean isRanked() {
        return this.ranked;
    }

    private class PearlCounter extends BukkitRunnable
    {
        private int counter;
        private Player ply;
        private Match match;

        public PearlCounter(final Player ply, final Match match) {
            this.ply = ply;
            this.counter = 16;
            this.match = match;
        }

        public void run() {
            --this.counter;
            if (this.counter < 0) {
                this.cancel();
                this.match.removeCounter(this);
            }
        }

        public int getCooldown() {
            return this.counter + 1;
        }
    }

    public void setSpectator(Player ply) {
        player1.hidePlayer(ply);
        player2.hidePlayer(ply);
        ply.setGameMode(GameMode.CREATIVE);
        ply.setFlying(true);
        spectators.add(ply);
        ItemStack is = new ItemBuilder(Material.getMaterial(351), "&cLeave spectator mode", "", 1, (short)1).getItem();
        ply.getInventory().setItem(8, is);
    }

    public void removeSpectator(Player ply) {
        ply.getInventory().clear();
        ply.setGameMode(GameMode.SURVIVAL);
        ply.setFlying(false);
        spectators.remove(ply);
    }
}
