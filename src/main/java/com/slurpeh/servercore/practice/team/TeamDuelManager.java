package com.slurpeh.servercore.practice.team;

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
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Random;

public class TeamDuelManager implements Listener {
    //TODO MAP SELECTING
    public KohiPractice plugin;
    public HashMap<Team, Team> pickingGameMode;
    public HashMap<Team, Team> awaitingReply;
    public HashMap<Team , GameType> data;
    public Table<Team, GameType, Arena> waitingReply;

    public TeamDuelManager(KohiPractice plugin) {
        this.pickingGameMode = new HashMap<>();
        this.plugin = plugin;
        this.awaitingReply = new HashMap<>();
        this.waitingReply = HashBasedTable.create();
        this.data = new HashMap<>();
        Bukkit.getPluginManager().registerEvents(this, this.plugin);
        this.plugin.getCommand("tduel").setExecutor(new DuelCommand());
        this.plugin.getCommand("taccept").setExecutor(new TAcceptCommand());
    }

    public void intiateDuel(final Team plyTeam, final Player target, final GameType gt, Arena a) {
        if (plugin.getTeamManager().getTeamByLeader(target) != null) {
            this.awaitingReply.put(plyTeam, plugin.getTeamManager().getTeamByLeader(target));
            JsonBuilder builder = new JsonBuilder("");
            builder.withText(plyTeam.getLeader().getName()).withClickEvent(JsonBuilder.ClickAction.RUN_COMMAND, "/taccept " + plyTeam.getLeader().getName()).withHoverEvent(JsonBuilder.HoverAction.SHOW_TEXT, "/taccept " + plyTeam.getLeader().getName() + " (Requests expire in 30 seconds)").withText(" has requested to duel your team with ").withColor(ChatColor.YELLOW).withClickEvent(JsonBuilder.ClickAction.RUN_COMMAND, "/taccept " + plyTeam.getLeader().getName()).withHoverEvent(JsonBuilder.HoverAction.SHOW_TEXT, "/taccept " + plyTeam.getLeader().getName() + " (Requests expire in 30 seconds)").withText(gt.getName()).withColor(ChatColor.DARK_GREEN).withClickEvent(JsonBuilder.ClickAction.RUN_COMMAND, "/taccept " + plyTeam.getLeader().getName()).withHoverEvent(JsonBuilder.HoverAction.SHOW_TEXT, "/taccept " + plyTeam.getLeader().getName() + " (Requests expire in 30 seconds)").withText("! Click this message to accept.").withColor(ChatColor.YELLOW).withClickEvent(JsonBuilder.ClickAction.RUN_COMMAND, "/taccept " + plyTeam.getLeader().getName()).withHoverEvent(JsonBuilder.HoverAction.SHOW_TEXT, "/taccept " + plyTeam.getLeader().getName() + " (Requests expire in 30 seconds)");
            builder.sendJson(target);
            for (Player ply : plugin.getTeamManager().getTeamByLeader(target).getTeam()) {
                if (!ply.getName().equalsIgnoreCase(target.getName())) {
                    ply.sendMessage(ChatColor.BLUE + "Team Duel request from " + ChatColor.GREEN + plyTeam.getLeader() + ChatColor.BLUE + "'s team with " + ChatColor.GREEN + gt.getName() + ChatColor.BLUE + " on arena " + ChatColor.GREEN + a.getName());
                }
            }
            for (Player ply : plyTeam.getTeam()) {
                if (!ply.getName().equalsIgnoreCase(plyTeam.getLeader().getName())) {
                    ply.sendMessage(ChatColor.BLUE + "Team Duel request sent to " + ChatColor.GREEN + target.getName() + ChatColor.BLUE + "'s team with " + ChatColor.GREEN + gt.getName() + ChatColor.BLUE + " on arena " + ChatColor.GREEN + a.getName());
                }
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    awaitingReply.remove(plyTeam);
                    target.sendMessage(ChatColor.YELLOW + "Team duel request from " + ChatColor.DARK_GREEN + plyTeam.getLeader().getName() + ChatColor.YELLOW + " has expired.");
                }
            }.runTaskLater(plugin, 600L);
        }
    }

    public void startDuel(final Team ply, final Team target, GameType gt, Arena a) {
        for (Player pyl : ply.getTeam()) {
            pyl.sendMessage(ChatColor.YELLOW + "Duel starting with " + ChatColor.GREEN + target.getLeader().getName() + "'s " + ChatColor.YELLOW + "team");
        }
        for (Player pyl : target.getTeam()) {
            pyl.sendMessage(ChatColor.YELLOW + "Duel starting with " + ChatColor.GREEN + ply.getLeader().getName() + "'s " + ChatColor.YELLOW + "team");
        }
        TeamMatch tm = new TeamMatch(ply, target, TeamMatchType.PARTYvPARTY, gt, a);
        tm.startMatch();
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
                if (Bukkit.getPlayer(target) != null && !ply.getName().equalsIgnoreCase(target) && plugin.getTeamManager().getTeamByLeader(ply) != null && plugin.getTeamManager().getTeamByLeader(Bukkit.getPlayer(target)) != null && !plugin.getTeamManager().getTeamByLeader(ply).isInMatch() && !plugin.getTeamManager().getTeamByLeader(Bukkit.getPlayer(target)).isInMatch()) {
                    openDuelMenu(ply);
                    plugin.getTeamManager().picking.put(plugin.getTeamManager().getTeamByLeader(ply), plugin.getTeamManager().getTeamByLeader(Bukkit.getPlayer(target)));
                    return true;
                } else {
                    ply.sendMessage(ChatColor.RED + "Invalid player!");
                    return true;
                }
            } else {
                ply.sendMessage(ChatColor.RED + "/tduel [player-name]");
                return true;
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

    public void openDuelMenu(Player ply) {
        InventoryBuilder inventoryBuilder = new InventoryBuilder(getGameTypes(), "Select a Pvp Style", false, false);
        int i = 0;
        for (GameType gt : plugin.getGameTypeManager().getGameTypes()) {
            if (gt.isSetup()) {
                inventoryBuilder.withItem(i, new ItemBuilder(gt.getDisplay().getType(), "&b" + gt.getName(), "").getItem());
                i++;
            }
        }
        ply.openInventory(inventoryBuilder.build());
        ply.updateInventory();
    }

    public void openArenaMenu(Player ply) {
        InventoryBuilder inventoryBuilder = new InventoryBuilder(getArenas(), "Select an Arena", false, false);
        inventoryBuilder.withItem(0, new ItemBuilder(Material.EMPTY_MAP, "&aRandom Arena", "&7Unknown map").getItem());
        int i = 1;
        for (Arena arena : plugin.getArenaManager().getArenas()) {
            if (arena.isSetup()) {
                inventoryBuilder.withItem(i, new ItemBuilder(Material.STAINED_CLAY, "&e" + arena.getName(), "", 1, (short)4).getItem());
                i++;
            }
        }
        ply.openInventory(inventoryBuilder.build());
        ply.updateInventory();
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

    class TAcceptCommand implements CommandExecutor {
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "You must be a player to execute this command!");
                return false;
            }
            Player ply = (Player)sender;
            if (args.length == 1) {
                Player target = Bukkit.getPlayer(args[0]);
                if (target != null && plugin.getTeamManager().getTeamByLeader(target) != null && plugin.getTeamManager().getTeamByLeader(ply) != null) {
                    if (!TeamDuelManager.this.awaitingReply.containsKey(plugin.getTeamManager().getTeamByLeader(target)) || TeamDuelManager.this.awaitingReply.get(plugin.getTeamManager().getTeamByLeader(target)) != plugin.getTeamManager().getTeamByLeader(ply) || plugin.getTeamManager().getTeamByLeader(target).isInMatch()) {
                        ply.sendMessage(ChatColor.RED + "That player did not send you a duel request!");
                        return true;
                    } else {
                        TeamDuelManager.this.startDuel(plugin.getTeamManager().getTeam(ply), plugin.getTeamManager().getTeam(target), data.get(plugin.getTeamManager().getTeam(target)), waitingReply.get(plugin.getTeamManager().getTeam(target), data.get(plugin.getTeamManager().getTeam(target))));
                        return true;
                    }
                } else {
                    ply.sendMessage(ChatColor.RED + "Invalid player!");
                    return true;
                }
            } else {
                ply.sendMessage(ChatColor.RED + "/taccept [player-name]");
                return true;
            }
        }
    }
}
