package com.slurpeh.servercore.practice;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bradley on 6/8/16.
 */
public class ChatCommand implements CommandExecutor, Listener {
    KohiPractice plugin;
    List<Player> plyChat;
    List<Player> disabledGlobalChat;
    public ChatCommand(KohiPractice plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.plyChat = new ArrayList<>();
        this.disabledGlobalChat = new ArrayList<>();
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player ply = e.getPlayer();
        String formatNoParty = "§a<player>§r: <message>";
        String formatPartyMember = "§7(§9Party§7)<player>§r: <message>";
        String formatPartyLeader = "§c*§7(§9Party§7)<player>: <message>";
        if (plugin.getTeamManager().getTeamByLeader(ply) != null && hasPartyChat(ply)) {
            formatPartyLeader = formatPartyLeader.replace("<player>", ply.getName());
            formatPartyLeader = formatPartyLeader.replace("<message>", e.getMessage());
            e.setFormat(formatPartyLeader);
            for (Player pl : e.getRecipients()) {
                if (!plugin.getTeamManager().getTeam(ply).getTeam().contains(pl)) {
                    e.getRecipients().remove(pl);
                }
            }
            for (Player pl : disabledGlobalChat) {
                e.getRecipients().remove(pl);
            }
        } else if (plugin.getTeamManager().getTeamByPlayer(ply) != null && !plugin.getTeamManager().getTeamByPlayer(ply).getLeader().getUniqueId().toString().equalsIgnoreCase(ply.getUniqueId().toString()) && hasPartyChat(ply)) {
            formatPartyMember = formatPartyMember.replace("<player>", ply.getName());
            formatPartyMember = formatPartyMember.replace("<message>", e.getMessage());
            e.setFormat(formatPartyMember);
            for (Player pl : e.getRecipients()) {
                if (!plugin.getTeamManager().getTeam(ply).getTeam().contains(pl)) {
                    e.getRecipients().remove(pl);
                }
            }
            for (Player pl : disabledGlobalChat) {
                e.getRecipients().remove(pl);
            }
        } else {
            formatNoParty = formatNoParty.replace("<player>", ply.getName());
            formatNoParty = formatNoParty.replace("<message>", e.getMessage());
            e.setFormat(formatNoParty);
            for (Player pl : disabledGlobalChat) {
                e.getRecipients().remove(pl);
            }
        }
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player ply = (Player)sender;
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("p") || args[0].equalsIgnoreCase("party") && plugin.getTeamManager().hasTeam(ply)) {
                    if (!plyChat.contains(ply)) {
                        plyChat.add(ply);
                        ply.sendMessage("You are now talking in " + ChatColor.DARK_PURPLE + "[P] Party" + ChatColor.RESET + ".");
                        return true;
                    } else {
                        ply.sendMessage("You are already talking in " + ChatColor.DARK_PURPLE + "[P] Party" + ChatColor.RESET + ".");
                        return true;
                    }
                } else if (args[0].equalsIgnoreCase("g") || args[0].equalsIgnoreCase("global")) {
                    if (plyChat.contains(ply)) {
                        plyChat.remove(ply);
                        ply.sendMessage("You are now talking in [G] Global.");
                        return true;
                    } else {
                        ply.sendMessage("You are already talking in [G] Global.");
                        return true;
                    }
                } else if (args[0].equalsIgnoreCase("toggle")) {
                    if (disabledGlobalChat.contains(ply)) {
                        disabledGlobalChat.remove(ply);
                        ply.sendMessage(ChatColor.YELLOW + "You have enabled Global Chat.");
                        return true;
                    } else {
                        disabledGlobalChat.add(ply);
                        ply.sendMessage(ChatColor.YELLOW + "You have disabled Global Chat.");
                        return true;
                    }
                } else {
                    ply.sendMessage(ChatColor.RED + "/c <p, g, toggle>");
                    return true;
                }
            } else {
                ply.sendMessage(ChatColor.RED + "/c <p, g, toggle>");
                return true;
            }
        } else {
            sender.sendMessage(ChatColor.RED + "You must be a player to use this command");
            return false;
        }
    }

    public boolean hasPartyChat(Player ply) {
        return plyChat.contains(ply);
    }
}
