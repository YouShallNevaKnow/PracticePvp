package com.slurpeh.servercore.practice.team;

import com.slurpeh.servercore.practice.KohiPractice;
import com.slurpeh.servercore.practice.inventory.InventoryType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Bradley on 5/22/16.
 */
public class TeamCommands implements CommandExecutor {
    private KohiPractice plugin;

    public TeamCommands(KohiPractice plugin) {
        this.plugin = plugin;
    }
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (! (sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You must be a player to execute this command!");
            return false;
        }
        Player ply = (Player) sender;
        if (args.length == 0) {
            ply.sendMessage(ChatColor.RED + "/team <kick:leave:create:invite:accept:info>");
        } else if (args.length == 1) {
            switch (args[0]) {
                case "create": {
                    if (!plugin.getTeamManager().hasTeam(ply)) {
                        plugin.getTeamManager().createTeam(ply);
                        ply.sendMessage(ChatColor.BLUE + "Created a new team! Invite players with /team invite");
                        ply.sendMessage(ChatColor.BLUE + "To use party chat, use /c p");
                        return true;
                    } else {
                        ply.sendMessage(ChatColor.RED + "You a in a team already!");
                        return true;
                    }
                }
                case "info": {
                    if (plugin.getTeamManager().hasTeam(ply)) {
                        ply.sendMessage(ChatColor.GREEN + plugin.getTeamManager().getTeam(ply).getLeader().getName() + ChatColor.BLUE + "'s team.");
                        for (Player ply2 : plugin.getTeamManager().getTeam(ply).getTeam()) {
                            ply.sendMessage(ChatColor.GREEN + ply2.getName());
                        }
                    }
                }
                case "leave": {
                    if (plugin.getTeamManager().getTeamByLeader(ply) != null) {
                        ply.sendMessage(ChatColor.RED + "Your team was deleted by the owner.");
                        ply.sendMessage(ChatColor.RED + "Your team was deleted");
                        plugin.getInventorySetter().setupInventory(InventoryType.DEFAULT, ply);
                        for (Player ply2 : plugin.getTeamManager().getTeamByLeader(ply).getTeam()) {
                            if (!ply2.getUniqueId().toString().equalsIgnoreCase(ply.getUniqueId().toString())) {
                                ply2.sendMessage(ChatColor.RED + "Your team was deleted by the owner.");
                                plugin.getInventorySetter().setupInventory(InventoryType.DEFAULT, ply2);
                            }
                        }
                        plugin.getTeamManager().removeTeam(plugin.getTeamManager().getTeamByLeader(ply));
                    } else if (plugin.getTeamManager().getTeamByPlayer(ply) != null) {
                        ply.sendMessage(ChatColor.RED + "You left " + plugin.getTeamManager().getTeamByPlayer(ply).getLeader().getName() + "'s team.");
                        plugin.getTeamManager().getTeamByPlayer(ply).getLeader().sendMessage(ChatColor.RED + ply.getName() + " left your team.");
                        plugin.getTeamManager().removePlayer(ply, plugin.getTeamManager().getTeamByPlayer(ply));
                    } else {
                        ply.sendMessage(ChatColor.RED + "You are not in a team");
                    }
                }
                default: {
                    return true;
                }
            }
        } else if (args.length == 2) {
            switch (args[0]) {
                case "invite": {
                    String pl = args[1];
                    if (plugin.getTeamManager().getTeamByLeader(ply) != null) {
                        if (!pl.equalsIgnoreCase(ply.getName()) && Bukkit.getPlayer(pl) != null && ! plugin.getTeamManager().getTeamByLeader(ply).getInvites().contains(Bukkit.getPlayer(pl)) && ! plugin.getTeamManager().getTeamByLeader(ply).getTeam().contains(Bukkit.getPlayer(pl)) && ! plugin.getTeamManager().hasTeam(Bukkit.getPlayer(pl)) && !plugin.getMatchManager().isInMatch(Bukkit.getPlayer(pl))) {
                            plugin.getTeamManager().addInvite(Bukkit.getPlayer(pl), plugin.getTeamManager().getTeamByLeader(ply));
                            return true;
                        } else {
                            ply.sendMessage(ChatColor.RED + "You can't invite that player!");
                            return true;
                        }
                    } else {
                        ply.sendMessage(ChatColor.RED + "You are not in a team, or you are not the owner!");
                        return true;
                    }
                }
                case "adminremove": {
                    if (ply.hasPermission("kohipractice.commands.adminremove")) {
                        String pLeader = args[1];
                        if (Bukkit.getPlayer(pLeader) != null) {
                            Player pl = Bukkit.getPlayer(pLeader);
                            if (plugin.getTeamManager().getTeamByLeader(pl) != null) {
                                pl.chat("/team leave");
                                ply.sendMessage(ChatColor.GREEN + "Successfully removed team!");
                                return true;
                            } else {
                                ply.sendMessage(ChatColor.RED + "That player does not own a team!");
                                return true;
                            }
                        } else {
                            ply.sendMessage(ChatColor.RED + "Invalid player!");
                            return true;
                        }
                    } else {
                        ply.sendMessage(ChatColor.RED + "You must have permission to execute this command!");
                        return true;
                    }
                }
                case "accept": {
                    String tl = args[1];
                    if (Bukkit.getPlayer(tl) != null) {
                        Player plLeader = Bukkit.getPlayer(tl);
                        if (plugin.getTeamManager().getTeamByLeader(plLeader) != null && ! plugin.getTeamManager().hasTeam(ply) && !plugin.getTeamManager().getTeamByLeader(plLeader).isInMatch()) {
                            if (plugin.getTeamManager().getTeamByLeader(plLeader).getInvites().contains(ply)) {
                                plugin.getTeamManager().addPlayer(ply, plugin.getTeamManager().getTeamByLeader(plLeader));
                                ply.sendMessage(ChatColor.BLUE + "Joined " + ChatColor.DARK_GREEN + plugin.getTeamManager().getTeam(ply).getLeader().getName() + ChatColor.BLUE + "'s team!");
                                ply.sendMessage(ChatColor.BLUE + "To use party chat, type /c p");
                                return true;
                            } else {
                                ply.sendMessage(ChatColor.RED + "You have not been invited to that party!");
                                return true;
                            }
                        } else {
                            ply.sendMessage(ChatColor.RED + "That team may be in a match, or the player doesn't have a team!");
                            return true;
                        }
                    } else {
                        ply.sendMessage(ChatColor.RED + "Player not found.");
                        return true;
                    }
                }
                case "kick": {
                    String p = args[1];
                    if (Bukkit.getPlayer(p) != null) {
                        if (plugin.getTeamManager().getTeamByLeader(ply) != null) {
                            if (plugin.getTeamManager().getTeamByLeader(ply).getTeam().contains(ply) && !p.equalsIgnoreCase(ply.getName())) {
                                plugin.getTeamManager().removePlayer(Bukkit.getPlayer(p), plugin.getTeamManager().getTeamByLeader(ply));
                                ply.sendMessage(ChatColor.RED + "Kicked " + ChatColor.DARK_GREEN + Bukkit.getPlayer(p).getName() + ChatColor.RED + " from your team!");
                                Bukkit.getPlayer(p).sendMessage(ChatColor.RED + "You were kicked from " + ChatColor.DARK_GREEN + ply.getName() + ChatColor.RED + "'s team!");
                                return true;
                            } else {
                                ply.sendMessage(ChatColor.RED + "Player is not in your team!");
                                return true;
                            }
                        } else {
                            ply.sendMessage(ChatColor.RED + "You do not own a team!");
                            return true;
                        }
                    } else {
                        ply.sendMessage(ChatColor.RED + "Player not found");
                        return true;
                    }
                }
            }
        } else {
            ply.sendMessage(ChatColor.RED + "/team <kick:leave:create:invite:accept:info>");
        }
        return false;
    }
}
