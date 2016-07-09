package com.slurpeh.servercore.practice.player;

import com.slurpeh.servercore.practice.KohiPractice;
import com.sun.xml.internal.bind.annotation.OverrideAnnotationOf;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Bradley on 5/9/16.
 */
public class EloCommand implements CommandExecutor {
    private KohiPractice plugin;
    public EloCommand(KohiPractice plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("elo")) {
            if (sender instanceof Player) {
                Player ply = (Player)sender;
                if (args.length == 1) {
                    if (args[0].equalsIgnoreCase("averagetop")) {
                        plugin.getRankingManager().sendEloAverageTop(ply);
                        return true;
                    } else {
                        ply.sendMessage(ChatColor.RED + "/elo all [player-name], /elo average [player-name], /elo top [gametype-name], /elo averagetop");
                        return true;
                    }
                } else if (args.length == 2) {
                    if (args[0].equalsIgnoreCase("all")) {
                        plugin.getRankingManager().sendElos(ply, args[1]);
                        return true;
                    } else if (args[0].equalsIgnoreCase("average")) {
                        plugin.getRankingManager().sendAverageElo(ply, args[1]);
                        return true;
                    } else if (args[0].equalsIgnoreCase("top")) {
                        if (plugin.getGameTypeManager().getGameType(args[1]) != null) {
                            plugin.getRankingManager().sendEloTop(plugin.getGameTypeManager().getGameType(args[1]), ply);
                            return true;
                        } else {
                            ply.sendMessage(ChatColor.RED + "GameType '" + args[1] + "' is not a valid gametype!");
                            return true;
                        }
                    } else {
                        ply.sendMessage(ChatColor.RED + "/elo all [player-name], /elo average [player-name], /elo top [gametype-name], /elo averagetop");
                        return true;
                    }
                } else {
                    ply.sendMessage(ChatColor.RED + "/elo all [player-name], /elo average [player-name], /elo top [gametype-name], /elo averagetop");
                    return true;
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You must be a player to use this command!");
                return false;
            }
        } else {
            return false;
        }
    }
}
