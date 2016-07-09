package com.slurpeh.servercore.practice.twovtwos;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.injector.GamePhase;
import com.slurpeh.servercore.practice.KohiPractice;
import com.slurpeh.servercore.practice.arena.Arena;
import com.slurpeh.servercore.practice.gametype.GameType;
import com.slurpeh.servercore.practice.inventory.InventoryType;
import com.slurpeh.servercore.practice.team.Team;
import com.slurpeh.servercore.practice.team.TeamMatch;
import com.slurpeh.servercore.practice.util.EntityHider;
import com.slurpeh.servercore.practice.util.JsonBuilder;
import net.minecraft.server.v1_7_R4.Blocks;
import org.apache.logging.log4j.core.helpers.FileUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.peer.ListPeer;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Bradley on 6/9/16.
 */
public class Match2v2 implements Listener {
    private Arena arena;
    private GameType gt;
    private Team t1;
    private Team t2;
    private boolean started;
    private KohiPractice plugin;
    private HashMap<Player, PearlCounter> counters;
    private List<Block> blocks;
    List<Player> spectators;
    private HashMap<Player, String> remaining;

    public Match2v2(Arena a, GameType gt, Team t1, Team t2) {
        this.arena = a;
        this.gt = gt;
        this.t1 = t1;
        this.t2 = t2;
        this.started = false;
        this.counters = new HashMap<>();
        this.plugin = JavaPlugin.getPlugin(KohiPractice.class);
        Bukkit.getPluginManager().registerEvents(this, this.plugin);
        this.blocks = new ArrayList<>();
        this.remaining = new HashMap<>();
        this.spectators = new ArrayList<>();
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
        if (gt.canPlaceAndBreak()) {
            while (blocks.iterator().hasNext()) {
                blocks.iterator().next().setType(Material.AIR);
            }
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if (hasPlayer(e.getPlayer())) {
            if (gt.canPlaceAndBreak()) {
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
            if (gt.canPlaceAndBreak()) {
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
            if (gt.canPlaceAndBreak()) {
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
        for (Match2v2 m : plugin.get2v2MatchManager().get2v2Matches()) {
            if (m.hasBlock(e.getBlock())) {
                m.addBlock(e.getToBlock());
            }
        }
    }

    public void startMatch() {
        if (t1.getTeam().size() == 2 && t2.getTeam().size() == 2) {
            for (Match2v2 m : plugin.get2v2MatchManager().get2v2Matches()) {
                if (m.getGametype().canPlaceAndBreak() && m.getArena().getName().equalsIgnoreCase(arena.getName())) {
                    for (Block b : m.getBlocks()) {
                        t1.getTeam().forEach(ply -> ply.sendBlockChange(b.getLocation(), Material.AIR, (byte)0));
                        t2.getTeam().forEach(ply -> ply.sendBlockChange(b.getLocation(), Material.AIR, (byte)0));
                    }
                }
            }
            t1.getTeam().forEach(ply -> remaining.put(ply, "t1"));
            t2.getTeam().forEach(ply -> remaining.put(ply, "t2"));
            t1.getTeam().forEach(ply -> ply.teleport(arena.getSpawn1()));
            t2.getTeam().forEach(ply -> ply.teleport(arena.getSpawn2()));
            for (Player ply : Bukkit.getOnlinePlayers()) {
                if (!t1.getTeam().contains(ply) && !t2.getTeam().contains(ply)) {
                    for (Player ply2 : t1.getTeam()) {
                        ply.hidePlayer(ply2);
                        ply2.hidePlayer(ply);
                    }
                    for (Player ply2 : t2.getTeam()) {
                        ply.hidePlayer(ply2);
                        ply2.hidePlayer(ply);
                    }
                }
            }
            new BukkitRunnable() {
                public void run() {
                    for (Player pl1 : t1.getTeam()) {
                        for (Player pl2 : t2.getTeam()) {
                            pl1.showPlayer(pl2);
                            pl2.showPlayer(pl1);
                        }
                    }
                }
            }.runTaskLater(this.plugin, 5L);
            t1.getTeam().forEach(ply -> ply.sendMessage(ChatColor.YELLOW + "Starting duel against " + ChatColor.GREEN + t2.getLeader().getName()));
            t2.getTeam().forEach(ply -> ply.sendMessage(ChatColor.YELLOW + "Starting duel against " + ChatColor.GREEN + t1.getLeader().getName()));
            t1.getTeam().forEach(ply -> ply.getInventory().clear());
            t2.getTeam().forEach(ply -> ply.getInventory().clear());
            t1.getTeam().forEach(ply -> plugin.getInventoryManager().showKits(ply, getGametype()));
            t2.getTeam().forEach(ply -> plugin.getInventoryManager().showKits(ply, getGametype()));
            new BukkitRunnable() {
                private int i = 5;
                @Override
                public void run() {
                    if (i == 0) {
                        cancel();
                        started = true;
                        if (t1.getTeam().size() == 2 && t1.getTeam().get(0) == null && t1.getTeam().get(1) == null) {
                            cancel();
                            endMatch(t2.getTeam());
                            return;
                        } else {
                            if (t2.getTeam().size() == 2 && t2.getTeam().get(0) == null && t2.getTeam().get(1) == null) {
                                cancel();
                                endMatch(t1.getTeam());
                                return;
                            } else {
                                getAllPlayers().forEach(ply -> ply.sendMessage(ChatColor.GREEN + "Duel starting now!"));
                                getAllPlayers().forEach(ply -> ply.playSound(ply.getLocation(), Sound.NOTE_PIANO, 10.0f, 2.0f));
                            }
                        }
                    } else {
                        if (t1.getTeam().size() == 2 && t1.getTeam().get(0) == null && t1.getTeam().get(1) == null) {
                            cancel();
                            endMatch(t2.getTeam());
                            return;
                        } else {
                            if (t2.getTeam().size() == 2 && t2.getTeam().get(0) == null && t2.getTeam().get(1) == null) {
                                cancel();
                                endMatch(t1.getTeam());
                                return;
                            } else {
                                getAllPlayers().forEach(ply -> ply.sendMessage(ChatColor.GREEN + "Starting in " + ChatColor.YELLOW + i + ChatColor.GREEN + " seconds!"));
                                getAllPlayers().forEach(ply -> ply.playSound(ply.getLocation(), Sound.NOTE_PIANO, 10.0f, 1.0f));
                            }
                            --this.i;
                        }
                    }
                }
            }.runTaskTimer(this.plugin, 0L, 20L);
        }
    }

    public void endMatch(List<Player> winningTeam) {
        if (getGametype().canPlaceAndBreak()) {
            undoBuilds();
        }
        for (Player ply : getAllPlayers()) {
            if (ply == null) {
                ply.spigot().respawn();
            }
        }
        for (Player ply : spectators) {
            removeSpectator(ply);
        }
        if (t1 != null && t2 != null) {
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
        }
        for (Player ply : getAllPlayers()) {
            ply.teleport(plugin.getSpawn());
            ply.setHealth(20);
            ply.setFoodLevel(20);
            ply.setLevel(0);
            for (PotionEffect pe : ply.getActivePotionEffects()) {
                ply.removePotionEffect(pe.getType());
            }
            ply.getActivePotionEffects().clear();
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player ply : Bukkit.getOnlinePlayers()) {
                    if (ply != null) {
                        for (Player ply2 : getAllPlayers()) {
                            if (!ply.getUniqueId().toString().equalsIgnoreCase(ply2.getUniqueId().toString())) {
                                if (plugin.getConfig().getBoolean("hide-players")) {
                                    ply.hidePlayer(ply2);
                                    ply2.hidePlayer(ply);
                                } else {
                                    ply.showPlayer(ply2);
                                    ply2.showPlayer(ply);
                                }
                            }
                        }
                    }
                }
            }
        }.runTaskLater(this.plugin, 5L);
        for (Player ply : getAllPlayers()) {
            String s = ChatColor.YELLOW + "Winners: ";
            if (winningTeam.size() == 2) {
                s += winningTeam.get(0).getName() + ", ";
                s += winningTeam.get(1).getName();
            }
            ply.sendMessage(s);
            if (t1.getTeam().size() == 2 && t2.getTeam().size() == 2) {
                JsonBuilder json = new JsonBuilder("").withText("Inventories (click to view): ").withColor(ChatColor.GOLD).withText(((OfflinePlayer)t1.getTeam().get(0)).getName() + ", ").withColor(ChatColor.YELLOW).withClickEvent(JsonBuilder.ClickAction.RUN_COMMAND, "/inventory " + ((OfflinePlayer)t1.getTeam().get(0)).getName()).withHoverEvent(JsonBuilder.HoverAction.SHOW_TEXT, ChatColor.GREEN + "Click to view inventory").withText("Inventories (click to view): ").withColor(ChatColor.GOLD).withText(((OfflinePlayer)t1.getTeam().get(1)).getName() + ", ").withColor(ChatColor.YELLOW).withClickEvent(JsonBuilder.ClickAction.RUN_COMMAND, "/inventory " + ((OfflinePlayer)t1.getTeam().get(1)).getName()).withHoverEvent(JsonBuilder.HoverAction.SHOW_TEXT, ChatColor.GREEN + "Click to view inventory").withText("Inventories (click to view): ").withColor(ChatColor.GOLD).withText(((OfflinePlayer)t2.getTeam().get(0)).getName() + ", ").withColor(ChatColor.YELLOW).withClickEvent(JsonBuilder.ClickAction.RUN_COMMAND, "/inventory " + ((OfflinePlayer)t2.getTeam().get(0)).getName()).withHoverEvent(JsonBuilder.HoverAction.SHOW_TEXT, ChatColor.GREEN + "Click to view inventory").withText("Inventories (click to view): ").withColor(ChatColor.GOLD).withText(((OfflinePlayer)t2.getTeam().get(1)).getName() + ", ").withColor(ChatColor.YELLOW).withClickEvent(JsonBuilder.ClickAction.RUN_COMMAND, "/inventory " + ((OfflinePlayer)t2.getTeam().get(1)).getName()).withHoverEvent(JsonBuilder.HoverAction.SHOW_TEXT, ChatColor.GREEN + "Click to view inventory");
                json.sendJson(ply);
            }
        }
        this.plugin.get2v2MatchManager().endMatch(this);
        this.arena = null;
        this.gt = null;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if (getAllPlayers().contains(e.getPlayer())) {
            remaining.remove(e.getPlayer());
            if (t1.getTeam().get(0) == null && t1.getTeam().get(1) == null) {
                endMatch(t2.getTeam());
                started = false;
            } else {
                if (t2.getTeam().get(0) == null && t2.getTeam().get(1) == null) {
                    endMatch(t1.getTeam());
                    started = false;
                }
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        if (getAllPlayers().contains(e.getEntity())) {
            e.setDeathMessage(e.getEntity().getName() + " was killed by " + e.getEntity().getKiller().getName());
            remaining.remove(e.getEntity());
            e.getEntity().spigot().respawn();
            setSpectator(e.getEntity());
            if (canEnd()) {
                e.getDrops().clear();
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (t1.getTeam().containsAll(remaining.keySet())) {
                            endMatch(t2.getTeam());
                            started = false;
                        } else {
                            if (t2.getTeam().containsAll(remaining.keySet())) {
                                endMatch(t1.getTeam());
                                started = false;
                            }
                        }
                    }
                }.runTaskLater(this.plugin, 2l);
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player) {
            if (spectators.contains((Player)e.getDamager())) {
                e.setCancelled(true);
            }
        }
        if (e.getEntity() instanceof Player && e.getDamager() instanceof Player && getAllPlayers().contains(e.getEntity()) && getAllPlayers().contains(e.getDamager())) {
            if (!started) {
                e.setCancelled(true);
            } else {
                if (e.getDamage() >= ((Player) e.getEntity()).getHealth()) {
                    plugin.getInventoryManager().storeInv((Player)e.getEntity(), true);
                    plugin.getInventoryManager().storeInv((Player)e.getDamager(), false);
                } else {
                    plugin.getInventoryManager().storeInv((Player)e.getEntity(), false);
                    plugin.getInventoryManager().storeInv((Player)e.getDamager(), false);
                }
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (getAllPlayers().contains(e.getPlayer())) {
            EntityHider hider = plugin.getEntityHider();
            for (Player ply : Bukkit.getOnlinePlayers()) {
                if (!getAllPlayers().contains(ply)) {
                    hider.hideEntity(ply, e.getItemDrop());
                }
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    e.getItemDrop().remove();
                    e.getItemDrop().eject();
                }
            }.runTaskLater(this.plugin, 3l);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getItem() != null && e.getItem().getType() == Material.ENDER_PEARL && getAllPlayers().contains(e.getPlayer())) {
            Player shooter = e.getPlayer();
            if (!started) {
                e.setCancelled(true);
                shooter.sendMessage(ChatColor.RED + "You can't use that before the duel!");
            } else if (counters.containsKey(shooter)) {
                shooter.sendMessage(ChatColor.YELLOW + "Pearl cooldown: " + ChatColor.RED + counters.get(shooter).getCooldown() + " seconds");
                e.setCancelled(true);
            } else {
                PearlCounter pc = new PearlCounter(shooter, this);
                pc.runTaskTimer(this.plugin, 0l, 200l);
                this.counters.put(shooter, pc);
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
    public void onPotionSplash(PotionSplashEvent e) {
        if (getAllPlayers().contains((Player)e.getEntity().getShooter())) {
            e.getAffectedEntities().stream().filter(en -> !getAllPlayers().contains((Player)en)).forEach(en -> e.getAffectedEntities().remove(en));
            e.setCancelled(true);
            e.getAffectedEntities().stream().filter(en -> getAllPlayers().contains((Player)en)).forEach(en -> en.addPotionEffects(e.getPotion().getEffects()));
        }
    }

    public boolean canEnd() {
        if (t1.getTeam().containsAll(remaining.keySet())) {
            return true;
        } else {
            if (t2.getTeam().containsAll(remaining.keySet())) {
                return true;
            } else {
                return false;
            }
        }
    }

    public ArrayList<Player> getAllPlayers() {
        ArrayList<Player> al = new ArrayList<>(t1.getTeam());
        al.addAll(t2.getTeam());
        return al;
    }

    public GameType getGametype() {
        return gt;
    }

    public Arena getArena() {
        return arena;
    }

    public void removeCounter(PearlCounter pl) {
        counters.remove(pl.ply);
    }

    private class PearlCounter extends BukkitRunnable
    {
        private int counter;
        private Player ply;
        private Match2v2 match;

        public PearlCounter(final Player ply, final Match2v2 match) {
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

    public boolean hasPlayer(Player ply) {
        return (t1.getTeam().contains(ply) || t2.getTeam().contains(ply));
    }

    public void setSpectator(Player ply) {
        for(Player ply2 : getAllPlayers()) {
            if (!ply2.getName().equalsIgnoreCase(ply.getName())) ply2.hidePlayer(ply);
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
