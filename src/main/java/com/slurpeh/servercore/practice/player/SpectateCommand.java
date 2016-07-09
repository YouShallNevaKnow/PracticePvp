package com.slurpeh.servercore.practice.player;

import com.slurpeh.servercore.practice.KohiPractice;
import com.slurpeh.servercore.practice.match.Match;
import com.slurpeh.servercore.practice.team.TeamMatch;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Bradley on 6/8/16.
 */
public class SpectateCommand implements CommandExecutor {
    KohiPractice plugin;

    public SpectateCommand(KohiPractice plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender snd, Command cmd, String s, String[] args) {
        if (!(snd instanceof Player)) {
            snd.sendMessage(ChatColor.RED + "You must be a player to execute this command");
            return false;
        }
        Player ply = (Player)snd;
        if (args.length == 1) {
            String target = args[0];
            if (Bukkit.getPlayer(target) != null) {
                if (plugin.getMatchManager().isInMatch(Bukkit.getPlayer(target))) {
                    Match m = plugin.getMatchManager().getMatch(Bukkit.getPlayer(target));
                    m.setSpectator(ply);
                    ply.sendMessage(ChatColor.YELLOW + "Now spectating " + ChatColor.GOLD + Bukkit.getPlayer(target).getName() + ChatColor.YELLOW + ". Right click the red dye to leave spectator mode.");
                    return true;
                } else {
                    if (plugin.getTeamManager().hasTeam(Bukkit.getPlayer(target)) && plugin.getTeamManager().getTeam(Bukkit.getPlayer(target)).isInMatch()) {
                        TeamMatch tm = plugin.getTeamManager().getTeam(Bukkit.getPlayer(target)).getTeamMatch();
                        tm.setSpectator(ply);
                        ply.sendMessage(ChatColor.YELLOW + "Now spectating " + ChatColor.GOLD + Bukkit.getPlayer(target).getName() + ChatColor.YELLOW + ". Right click the red dye to leave spectator mode.");
                        return true;
                    } else {
                        ply.sendMessage(ChatColor.RED + "That player is not in a match currently.");
                        return true;
                    }
                }
            } else {
                ply.sendMessage(ChatColor.RED + "Player not found.");
                return true;
            }
        } else {
            ply.sendMessage(ChatColor.RED + "Too few arguments.\n/spectate <player>");
            return true;
        }
    }
}
