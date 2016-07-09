package com.slurpeh.servercore.practice.team;

import com.slurpeh.servercore.practice.KohiPractice;
import com.slurpeh.servercore.practice.arena.Arena;
import com.slurpeh.servercore.practice.gametype.GameType;
import com.slurpeh.servercore.practice.inventory.InventoryType;
import com.slurpeh.servercore.practice.twovtwos.UnrankedInventory2v2;
import com.slurpeh.servercore.practice.util.InventoryBuilder;
import com.slurpeh.servercore.practice.util.ItemBuilder;
import com.slurpeh.servercore.practice.util.JsonBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Created by Bradley on 4/29/16.
 */
public class TeamManager implements Listener {
    //TODO /GC, /PC,
    HashMap<Team, TeamMatch> teamMatches;
    public List<Team> teams;
    KohiPractice plugin;
    HashMap<Team, Team> picking;
    HashMap<Team, GameType> gts;
    HashMap<Team, String> pickin;

    public TeamManager(KohiPractice plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
        this.teams = new ArrayList<>();
        this.picking = new HashMap<>();
        this.pickin = new HashMap<>();
        this.teamMatches = new HashMap<>();
        this.gts = new HashMap<>();
    }

    public Collection<TeamMatch> getTeamMatches() {
        return teamMatches.values();
    }

    public void createTeam(Player leader) {
        Team t = new Team(leader, plugin);
        teams.add(t);
        plugin.getInventorySetter().setupInventory(InventoryType.PARTY_LEADER, leader);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Team t = getTeamByLeader(e.getPlayer());
        if (t != null) {
            removeTeam(t);
        } else {
            Team t2 = getTeamByPlayer(e.getPlayer());
            if (t2 != null) {
                t2.getLeader().sendMessage(ChatColor.RED + e.getPlayer().getName() + " left your team.");
                removePlayer(e.getPlayer(), t2);
            } else {
                return;
            }
        }
    }

    public void removeTeam(Team t) {
        this.teams.remove(t);
        t.delete();
    }

    public void removePlayer(Player ply, Team t) {
        t.removePlayer(ply);
        if (ply != null) {
            plugin.getInventorySetter().setupInventory(InventoryType.DEFAULT, ply);
        }
    }

    public void addInvite(Player ply, Team t) {
        t.addInvite(ply);
        t.getLeader().sendMessage(ChatColor.BLUE + "Team request sent to " + ChatColor.DARK_GREEN + ply.getName());
        new JsonBuilder("").withText("Click here to join ").withColor(ChatColor.YELLOW).withClickEvent(JsonBuilder.ClickAction.RUN_COMMAND, "/team accept " + t.getLeader().getName()).withHoverEvent(JsonBuilder.HoverAction.SHOW_TEXT, ChatColor.BLUE + "Click to accept request. Expires in " + ChatColor.GREEN + "30 " + ChatColor.BLUE + "seconds").withText(t.getLeader().getName() + "'s").withColor(ChatColor.GREEN).withClickEvent(JsonBuilder.ClickAction.RUN_COMMAND, "/team accept " + t.getLeader().getName()).withHoverEvent(JsonBuilder.HoverAction.SHOW_TEXT, ChatColor.BLUE + "Click to accept request. Expires in " + ChatColor.GREEN + "30 " + ChatColor.BLUE + "seconds").withText(" team").withColor(ChatColor.YELLOW).withClickEvent(JsonBuilder.ClickAction.RUN_COMMAND, "/team accept " + t.getLeader().getName()).withHoverEvent(JsonBuilder.HoverAction.SHOW_TEXT, ChatColor.BLUE + "Click to accept request. Expires in " + ChatColor.GREEN + "30 " + ChatColor.BLUE + "seconds").sendJson(ply);
        new BukkitRunnable() {
            @Override
            public void run() {
                t.removeInvite(ply);
                ply.sendMessage(ChatColor.BLUE + "Your invite to " + ChatColor.DARK_GREEN + t.getLeader().getName() + ChatColor.BLUE + "'s team has expired.");
            }
        }.runTaskLater(plugin, 600);
    }

    public void addPlayer(Player ply, Team t) {
        t.removeInvite(ply);
        t.addPlayer(ply);
        plugin.getInventorySetter().setupInventory(InventoryType.PARTY_MEMBER, ply);
    }

    public Team getTeamByPlayer(Player ply) {
        for (Team t : teams) {
            if (t.getTeam().contains(ply) && ! ply.getUniqueId().toString().equals(t.getLeader().getUniqueId().toString())) {
                return t;
            }
        }
        return null;
    }

    public boolean hasTeam(Player ply) {
        return (getTeamByLeader(ply) != null || getTeamByPlayer(ply) != null);
    }

    public Team getTeamByLeader(Player ply) {
        for (Team t : teams) {
            if (t.getLeader().getUniqueId().toString().equalsIgnoreCase(ply.getUniqueId().toString())) {
                return t;
            }
        }
        return null;
    }

    public Team getTeam(Player ply) {
        if (getTeamByLeader(ply) != null) {
            return getTeamByLeader(ply);
        } else if (getTeamByPlayer(ply) != null) {
            return getTeamByPlayer(ply);
        } else {
            return null;
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player ply = e.getPlayer();
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (e.getItem() != null && e.getItem().hasItemMeta()) {
                if (e.getItem().isSimilar(new ItemBuilder(Material.SKULL_ITEM, "&9List all members in your Team", "", 1, (short) SkullType.PLAYER.ordinal()).getItem())) {
                    if (getTeam(e.getPlayer()) != null) {
                        ply.sendMessage(ChatColor.GREEN + getTeam(ply).getLeader().getName() + ChatColor.BLUE + "'s Team.");
                        for (Player ply2 : getTeam(ply).getTeam()) {
                            ply.sendMessage(ChatColor.GREEN + ply2.getName());
                        }
                    }
                } else if (e.getItem().isSimilar(new ItemBuilder(Material.FIRE, "&cLeave this team.", "").getItem())) {
                    if (hasTeam(ply)) {
                        ply.chat("/team leave");
                    }
                } else if (e.getItem().isSimilar(new ItemBuilder(Material.EYE_OF_ENDER, "&9Show other teams to duel", "").getItem()) || e.getItem().isSimilar(new ItemBuilder(Material.EYE_OF_ENDER, "&9View other teams to duel", "").getItem())) {
                    e.setCancelled(true);
                    InventoryBuilder builder = new InventoryBuilder(54, "All current teams", false, false);
                    int i = 0;
                    for (Team t : this.teams) {
                        if (t.isInMatch()) {
                            ItemStack is = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.CREEPER.ordinal());
                            ItemMeta im = is.getItemMeta();
                            List<String> lore = new ArrayList<>();
                            im.setDisplayName(ChatColor.GREEN + t.getLeader().getName() + "'s Team " + ChatColor.RED + "(In Duel)");
                            t.getTeam().stream().forEach(pl -> lore.add(ChatColor.GREEN + pl.getName() + (t.getTeamMatch().remaining.containsKey(ply) ? ChatColor.RED + " (In Duel)" : "")));
                            im.setLore(lore);
                            is.setItemMeta(im);
                            builder.withItem(i, is);
                            i++;
                        } else {
                            ItemStack is = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
                            ItemMeta im = is.getItemMeta();
                            List<String> lore = new ArrayList<>();
                            im.setDisplayName(ChatColor.GREEN + t.getLeader().getName() + "'s Team");
                            t.getTeam().stream().forEach(pl -> lore.add(ChatColor.GREEN + pl.getName()));
                            im.setLore(lore);
                            is.setItemMeta(im);
                            builder.withItem(i, is);
                            i++;
                        }
                    }
                    Inventory inv = builder.build();
                    ply.openInventory(inv);
                    ply.updateInventory();
                } else if (e.getItem().isSimilar(new ItemBuilder(Material.GOLD_SWORD, "&eStart a Team Event", "").getItem())) {
                    if (getTeamByLeader(ply) != null) {
                        InventoryBuilder builder = new InventoryBuilder(18, "Select a Team Event", false, false);
                        ItemStack i1 = new ItemBuilder(Material.DIAMOND_SWORD, "&eFFA", "").getItem();
                        ItemMeta im1 = i1.getItemMeta();
                        i1.setItemMeta(im1);
                        builder.withItem(0, i1);
                        ItemStack i2 = new ItemBuilder(Material.DIAMOND_SWORD, "&eTeam fight", "").getItem();
                        ItemMeta im2 = i2.getItemMeta();
                        List<String> lo2 = new ArrayList<>();
                        lo2.add(ChatColor.DARK_PURPLE + "Split your team at");
                        lo2.add(ChatColor.DARK_PURPLE + "random in half");
                        lo2.add(ChatColor.DARK_PURPLE + "and duel each other");
                        im2.setLore(lo2);
                        i2.setItemMeta(im2);
                        builder.withItem(1, i2);
                        ply.openInventory(builder.build());
                        ply.updateInventory();
                    }
                }
            }
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Player ply = (Player) e.getWhoClicked();
        if (getTeamByLeader(ply) != null) {
            Team t = getTeamByLeader(ply);
            if (e.getClickedInventory().getTitle().equalsIgnoreCase("All current teams")) {
                for (Team t2 : teams) {
                    if (! t.getLeader().getName().equalsIgnoreCase(t2.getLeader().getName().toString())) {
                        if (e.getCurrentItem() != null && e.getCurrentItem().hasItemMeta()) {
                            if (t2.isInMatch()) {
                                e.setCancelled(true);
                                ply.closeInventory();
                            }
                            ItemStack is = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
                            ItemMeta im = is.getItemMeta();
                            List<String> lore = new ArrayList<>();
                            im.setDisplayName(ChatColor.GREEN + t2.getLeader().getName() + "'s Team");
                            t2.getTeam().stream().forEach(pl -> lore.add(ChatColor.GREEN + pl.getName()));
                            im.setLore(lore);
                            is.setItemMeta(im);
                            if (e.getCurrentItem().isSimilar(is) && ! t2.isInMatch()) {
                                e.setCancelled(true);
                                InventoryBuilder invbu = new InventoryBuilder(getGameTypeSize(), "Select A Pvp Style", false, false);
                                int i = 0;
                                for (GameType gt : plugin.getGameTypeManager().getGameTypes()) {
                                    if (gt.isSetup()) {
                                        ItemStack is2 = new ItemBuilder(gt.getDisplay().getType(), "&b" + gt.getName(), "").getItem();
                                        invbu.withItem(i, is2);
                                        i++;
                                    }
                                }
                                picking.put(t, t2);
                                ply.openInventory(invbu.build());
                                ply.updateInventory();
                                return;
                            } else {
                                e.setCancelled(true);
                                return;
                            }
                        } else {
                            e.setCancelled(true);
                            return;
                        }
                    } else {
                        e.setCancelled(true);
                        return;
                    }
                }
            } else if (e.getClickedInventory().getTitle().equalsIgnoreCase("Select a Team Event")) {
                if (e.getCurrentItem() != null && e.getCurrentItem().hasItemMeta()) {
                    String itemName = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', e.getCurrentItem().getItemMeta().getDisplayName()));
                    if (itemName.equalsIgnoreCase("FFA")) {
                        InventoryBuilder invbu = new InventoryBuilder(getGameTypeSize(), "Select A Pvp Style", false, false);
                        int i = 0;
                        for (GameType gt : plugin.getGameTypeManager().getGameTypes()) {
                            if (gt.isSetup()) {
                                ItemStack is = new ItemBuilder(gt.getDisplay().getType(), "&b" + gt.getName(), "").getItem();
                                invbu.withItem(i, is);
                                i++;
                            }
                        }
                        e.setCancelled(true);
                        pickin.put(t, "FFA");
                        ply.openInventory(invbu.build());
                        ply.updateInventory();
                        return;
                    } else if (itemName.equalsIgnoreCase("Team fight")) {
                        InventoryBuilder invbu = new InventoryBuilder(getGameTypeSize(), "Select A Pvp Style", false, false);
                        int i = 0;
                        for (GameType gt : plugin.getGameTypeManager().getGameTypes()) {
                            if (gt.isSetup()) {
                                ItemStack is = new ItemBuilder(gt.getDisplay().getType(), "&b" + gt.getName(), "").getItem();
                                invbu.withItem(i, is);
                                i++;
                            }
                        }
                        e.setCancelled(true);
                        pickin.put(t, "Team fight");
                        ply.openInventory(invbu.build());
                        ply.updateInventory();
                        return;
                    }
                }
            } else if (e.getClickedInventory().getTitle().equalsIgnoreCase("Select A Pvp Style")) {
                if (e.getCurrentItem() != null && e.getCurrentItem().hasItemMeta()) {
                    for (GameType gt : plugin.getGameTypeManager().getGameTypes()) {
                        if (e.getCurrentItem().isSimilar(new ItemBuilder(gt.getDisplay().getType(), "&b" + gt.getName(), "").getItem())) {
                            if (picking.containsKey(t)) {
                                plugin.getTeamDuelManager().data.put(t, gt);
                                plugin.getTeamDuelManager().openArenaMenu(ply);
                                e.setCancelled(true);
                                return;
                            } else if (pickin.containsKey(t)) {
                                switch (pickin.get(t)) {
                                    case "FFA": {
                                        gts.put(t, gt);
                                        plugin.getTeamDuelManager().openArenaMenu(ply);
                                        e.setCancelled(true);
                                        break;
                                    }
                                    case "Team fight": {
                                        gts.put(t, gt);
                                        plugin.getTeamDuelManager().openArenaMenu(ply);
                                        e.setCancelled(true);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (e.getInventory().getTitle().equalsIgnoreCase("Select an Arena")) {
                if (plugin.getTeamDuelManager().data.containsKey(t)) {
                    if (picking.containsKey(t)) {
                        if (e.getCurrentItem() != null && e.getCurrentItem().hasItemMeta()) {
                            String arenaName = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());
                            if (plugin.getArenaManager().getArena(arenaName) != null) {
                                Arena arena = plugin.getArenaManager().getArena(arenaName);
                                plugin.getTeamDuelManager().waitingReply.put(t, plugin.getTeamDuelManager().data.get(t), arena);
                                ply.sendMessage(ChatColor.YELLOW + "Team duel request sent to " + ChatColor.DARK_GREEN + picking.get(t).getLeader().getName() + ChatColor.YELLOW + " with " + ChatColor.DARK_GREEN + plugin.getTeamDuelManager().data.get(t).getName() + ChatColor.YELLOW + " on arena " + ChatColor.DARK_GREEN + plugin.getTeamDuelManager().waitingReply.get(t, plugin.getTeamDuelManager().data.get(t)).getName());
                                plugin.getTeamDuelManager().intiateDuel(t, picking.get(t).getLeader(), plugin.getTeamDuelManager().data.get(t), arena);
                                ply.closeInventory();
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        System.out.println(plugin.getTeamDuelManager().waitingReply.contains(t, plugin.getTeamDuelManager().data.get(t)));
                                    }
                                }.runTaskTimer(this.plugin, 0L, 10L);
                                e.setCancelled(true);
                            } else {
                                if (arenaName.equalsIgnoreCase("Random Arena")) {
                                    Arena arena = plugin.getArenaManager().getArenas().get(new Random().nextInt(plugin.getArenaManager().getArenas().size() - 1));
                                    plugin.getTeamDuelManager().waitingReply.put(t, plugin.getTeamDuelManager().data.get(t), arena);
                                    ply.sendMessage(ChatColor.YELLOW + "Team duel request sent to " + ChatColor.DARK_GREEN + picking.get(t).getLeader().getName() + ChatColor.YELLOW + " with " + ChatColor.DARK_GREEN + plugin.getTeamDuelManager().data.get(t).getName() + ChatColor.YELLOW + " on arena " + ChatColor.DARK_GREEN + arena.getName());
                                    plugin.getTeamDuelManager().intiateDuel(t, picking.get(t).getLeader(), plugin.getTeamDuelManager().data.get(t), arena);
                                    ply.closeInventory();
                                    e.setCancelled(true);
                                }
                            }
                        }
                    }
                    e.setCancelled(true);
                    ply.closeInventory();
                } else {
                    if (gts.get(t) != null && pickin.containsKey(t)) {
                        if (e.getCurrentItem() != null && e.getCurrentItem().hasItemMeta()) {
                            String arenaName = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());
                            if (plugin.getArenaManager().getArena(arenaName) != null) {
                                switch (pickin.get(t)) {
                                    case "FFA": {
                                        TeamMatch tm = new TeamMatch(t.getTeam(), TeamMatchType.FFA, gts.get(t), plugin.getArenaManager().getArena(arenaName));
                                        if (t.getTeam().size() > 1) {
                                            ply.closeInventory();
                                            e.setCancelled(true);
                                            tm.startMatch();
                                            gts.remove(t);
                                        } else {
                                            ply.sendMessage(ChatColor.RED + "You do not have enough players!");
                                            return;
                                        }
                                        break;
                                    }
                                    case "Team fight": {
                                        TeamMatch tm = new TeamMatch(t, TeamMatchType.TEAMSPLIT, gts.get(t), plugin.getArenaManager().getArena(arenaName));
                                        if (t.getTeam().size() > 1) {
                                            ply.closeInventory();
                                            e.setCancelled(true);
                                            tm.startMatch();
                                            gts.remove(t);
                                        } else {
                                            ply.sendMessage(ChatColor.RED + "You do not have enough players!");
                                            return;
                                        }
                                        break;
                                    }
                                }
                                ply.closeInventory();
                                e.setCancelled(true);
                            } else {
                                if (arenaName.equalsIgnoreCase("Random Arena")) {
                                    Arena arena = plugin.getArenaManager().getArenas().get(new Random().nextInt(plugin.getArenaManager().getArenas().size()));
                                    switch (pickin.get(t)) {
                                        case "FFA": {
                                            TeamMatch tm = new TeamMatch(t.getTeam(), TeamMatchType.FFA, gts.get(t), arena);
                                            if (t.getTeam().size() > 1) {
                                                ply.closeInventory();
                                                e.setCancelled(true);
                                                tm.startMatch();
                                                gts.remove(t);
                                            } else {
                                                ply.sendMessage(ChatColor.RED + "You do not have enough players!");
                                                return;
                                            }
                                        }
                                        case "Team fight": {
                                            TeamMatch tm = new TeamMatch(t, TeamMatchType.TEAMSPLIT, gts.get(t), arena);
                                            if (t.getTeam().size() > 1) {
                                                ply.closeInventory();
                                                e.setCancelled(true);
                                                tm.startMatch();
                                                gts.remove(t);
                                            } else {
                                                ply.sendMessage(ChatColor.RED + "You do not have enough players!");
                                                return;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    e.setCancelled(true);
                    ply.closeInventory();
                }
            }
        } else {
            if (getTeamByPlayer(ply) != null && (e.getClickedInventory().getTitle().equalsIgnoreCase("Select a Pvp Style") || e.getClickedInventory().getTitle().equalsIgnoreCase("Other Teams to Duel") || e.getClickedInventory().getTitle().equalsIgnoreCase("Select a Team Event"))) {
                e.setCancelled(true);
            }
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
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
            Player ply1 = (Player) e.getDamager();
            Player ply2 = (Player) e.getEntity();
            if (hasTeam(ply1) && hasTeam(ply2) && getTeam(ply1) == getTeam(ply2) && getTeam(ply1).getTeamMatch() != null && getTeam(ply1).getTeamMatch().type == TeamMatchType.PARTYvPARTY) {
                ply1.sendMessage(ChatColor.RED + ply2.getName() + " is on your team!");
                e.setDamage(0);
            } else if (hasTeam(ply1) && hasTeam(ply2) && getTeam(ply1) == getTeam(ply2) && getTeam(ply1).getTeamMatch() != null && getTeam(ply1).getTeamMatch().type == TeamMatchType.TEAMSPLIT) {
                Team t1 = getTeam(ply1);
                List<Player> team1 = new ArrayList<>();
                List<Player> team2 = new ArrayList<>();
                for (int i = 0; i < t1.getTeam().size(); i++) {
                    if ((i & 1) == 0) {
                        team1.add(t1.getTeam().get(i));
                    } else {
                        team2.add(t1.getTeam().get(i));
                    }
                }
                if ((team1.contains(ply1) && team1.contains(ply1)) || (team2.contains(ply1) && team2.contains(ply2))) {
                    e.setDamage(0);
                    ply1.sendMessage(ChatColor.RED + ply2.getName() + " is on your team!");
                }
            } else {

            }
        }
    }
}
