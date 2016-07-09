package com.slurpeh.servercore.practice.team;

import com.slurpeh.servercore.practice.KohiPractice;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bradley on 4/29/16.
 */
public class Team {
    //incomplete
    private TeamMatch teamMatch;
    private List<Player> team;
    private boolean inMatch;
    private Player leader;
    private List<Player> invites;
    private KohiPractice plugin;
    private boolean nulled;

    public Team(Player leader, KohiPractice plugin) {
        this.team = new ArrayList<>();
        team.add(leader);
        this.inMatch = false;
        this.leader = leader;
        this.teamMatch = null;
        this.nulled = false;
        this.invites = new ArrayList<>();
        this.plugin = plugin;
    }

    public List<Player> getTeam() {
        return this.team;
    }

    public Player getLeader() {
        return this.leader;
    }

    public void addInvite(Player ply) {
        invites.add(ply);
    }

    public TeamMatch getTeamMatch() {
        return teamMatch;
    }

    public boolean isInMatch() {
        return inMatch;
    }

    public List<Player> getInvites() {
        return invites;
    }

    public void setInMatch(boolean inMatch) {
        this.inMatch = inMatch;
    }

    public void setMatch(TeamMatch match) {
        this.teamMatch = match;
    }

    public void delete() {

    }

    public void setTeam(List<Player> team) {
        this.team = team;
    }

    public void setInvites(List<Player> invites) {
        this.invites = invites;
    }

    public void addPlayer(Player ply) {
        team.add(ply);
    }

    public void removePlayer(Player ply) {
        team.remove(ply);
    }

    public void removeInvite(Player ply) {
        invites.remove(ply);
    }
}
