package com.slurpeh.servercore.practice.player;

import com.slurpeh.servercore.practice.KohiPractice;
import com.slurpeh.servercore.practice.inventory.InventoryType;
import com.slurpeh.servercore.practice.match.Match;
import com.slurpeh.servercore.practice.team.Team;
import com.slurpeh.servercore.practice.team.TeamMatch;
import com.slurpeh.servercore.practice.util.ItemBuilder;
import net.minecraft.util.com.mojang.authlib.GameProfile;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.kitteh.tag.AsyncPlayerReceiveNameTagEvent;
import org.kitteh.tag.api.PacketHandlerNetty;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

/**
 * Created by Bradley on 5/14/16.
 */
public class PlayerEvents implements Listener {
    KohiPractice plugin;

    public PlayerEvents(KohiPractice plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, this.plugin);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (plugin.getTeamManager().getTeam(e.getPlayer()) != null && plugin.getTeamManager().getTeam(e.getPlayer()).isInMatch() || plugin.getMatchManager().isInMatch(e.getPlayer()) /*|| plugin.get2v2MatchManager().isInMatch(e.getPlayer()) */ || plugin.getKitEditorManager().isEditing(e.getPlayer())) {
            e.setCancelled(false);
        } else {
            e.getItemDrop().remove();
            e.getItemDrop().eject();
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        new BukkitRunnable() {
            @Override
            public void run() {
                e.getPlayer().teleport(PlayerEvents.this.plugin.getSpawn());
            }
        }.runTaskLater(this.plugin, 5L);
    }

    @EventHandler
    public void onPicker(PlayerPickupItemEvent e) {
        if (plugin.getTeamManager().getTeam(e.getPlayer()) != null && plugin.getTeamManager().getTeam(e.getPlayer()).isInMatch() || plugin.getMatchManager().isInMatch(e.getPlayer())) {
            e.setCancelled(false);
        } else {
            e.setCancelled(true);
            e.getItem().remove();
            e.getItem().eject();
        }
    }

    @EventHandler
    public void onCommandSend(PlayerCommandPreprocessEvent e) {
        boolean inTeam = plugin.getTeamManager().getTeam(e.getPlayer()) != null;
        if (e.getPlayer().isOp() || ! (inTeam && plugin.getTeamManager().getTeam(e.getPlayer()).isInMatch()) || ! plugin.getMatchManager().isInMatch(e.getPlayer()) || e.getMessage().contains("report")) {
            e.setCancelled(false);
        } else {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player ply = e.getPlayer();
        if (ply.getItemInHand() != null && ply.getItemInHand().hasItemMeta()) {
            if (ply.getItemInHand().isSimilar(new ItemBuilder(Material.getMaterial(351), "&cLeave spectator mode", "", 1, (short) 1).getItem())) {
                ply.setFlying(false);
                ply.setGameMode(GameMode.SURVIVAL);
                for (Player ply2 : Bukkit.getOnlinePlayers()) {
                    if (plugin.getConfig().getBoolean("hide-players")) {
                        if (ply2 != ply) {
                            ply2.hidePlayer(ply);
                            ply.hidePlayer(ply2);
                        }
                    }
                }
                for (TeamMatch tm : plugin.getTeamManager().getTeamMatches()) {
                    if (tm.spectators.contains(ply)) {
                        tm.spectators.remove(ply);
                    }
                }
                for (Match m : plugin.getMatchManager().getMatches()) {
                    if (m.spectators.contains(ply)) {
                        m.spectators.remove(ply);
                    }
                }
                plugin.getInventorySetter().setupInventory(InventoryType.DEFAULT, ply);
                ply.sendMessage(ChatColor.RED + "You left spectator mode.");
            }
        }
    }

    @EventHandler
    public void onReceiveNametag(AsyncPlayerReceiveNameTagEvent e) {
        if (plugin.getConfig().getBoolean("use-colored-nametags")) {
            if (plugin.getMatchManager().isInMatch(e.getPlayer()) && (plugin.getMatchManager().getMatch(e.getPlayer()).getPlayers()[0].getName().equalsIgnoreCase(e.getNamedPlayer().getName()) || plugin.getMatchManager().getMatch(e.getPlayer()).getPlayers()[1].getName().equalsIgnoreCase(e.getNamedPlayer().getName()))) {
                //in same match
                e.setTag(ChatColor.RED + e.getNamedPlayer().getName());
            } else {
                if (plugin.getTeamManager().hasTeam(e.getPlayer()) && plugin.getTeamManager().hasTeam(e.getNamedPlayer())) {
                    //have teams
                    if (plugin.getTeamManager().getTeam(e.getPlayer()).getLeader().getUniqueId().toString().equalsIgnoreCase(plugin.getTeamManager().getTeam(e.getNamedPlayer()).getLeader().getUniqueId().toString())) {
                        //same team
                        if (plugin.getTeamManager().getTeam(e.getPlayer()).isInMatch()) {
                            switch (plugin.getTeamManager().getTeam(e.getPlayer()).getTeamMatch().getType()) {
                                case FFA: {
                                    e.setTag(ChatColor.RED + e.getNamedPlayer().getName());
                                    break;
                                }
                                case TEAMSPLIT: {
                                    TeamMatch tm = plugin.getTeamManager().getTeam(e.getPlayer()).getTeamMatch();
                                    List<Player> team1 = new ArrayList<>();
                                    List<Player> team2 = new ArrayList<>();
                                    for (int i = 0; i < tm.t1.getTeam().size(); i++) {
                                        if ((i & 1) == 0) {
                                            team1.add(tm.t1.getTeam().get(i));
                                        } else {
                                            team2.add(tm.t1.getTeam().get(i));
                                        }
                                    }
                                    if (team1.contains(e.getPlayer()) && team1.contains(e.getNamedPlayer())) {
                                        e.setTag(ChatColor.GREEN + e.getNamedPlayer().getName());
                                    } else {
                                        if (team2.contains(e.getPlayer()) && team2.contains(e.getNamedPlayer())) {
                                            e.setTag(ChatColor.GREEN + e.getNamedPlayer().getName());
                                        } else {
                                            e.setTag(ChatColor.RED + e.getNamedPlayer().getName());
                                        }
                                    }
                                    break;
                                }
                                case PARTYvPARTY: {
                                    e.setTag(ChatColor.GREEN + e.getNamedPlayer().getName());
                                    break;
                                }
                            }
                        }
                    } else {
                        Team t1 = plugin.getTeamManager().getTeam(e.getPlayer());
                        Team t2 = plugin.getTeamManager().getTeam(e.getNamedPlayer());
                        if (t1.isInMatch() && t2.isInMatch()) {
                            TeamMatch tm = t1.getTeamMatch();
                            if (tm == t2.getTeamMatch()) {
                                //in same match
                                e.setTag(ChatColor.RED + e.getNamedPlayer().getName());
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if (! plugin.getTeamManager().hasTeam(e.getPlayer()) && ! plugin.getMatchManager().isInMatch(e.getPlayer()) && ! plugin.get2v2MatchManager().isInMatch(e.getPlayer()) && ! e.getPlayer().isOp()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if (! plugin.getTeamManager().hasTeam(e.getPlayer()) && ! plugin.getMatchManager().isInMatch(e.getPlayer()) && ! plugin.get2v2MatchManager().isInMatch(e.getPlayer()) && ! e.getPlayer().isOp()) {
            e.setCancelled(true);
        }
    }
}
