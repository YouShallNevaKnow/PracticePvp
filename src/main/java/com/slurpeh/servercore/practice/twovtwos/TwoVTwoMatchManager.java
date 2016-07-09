package com.slurpeh.servercore.practice.twovtwos;

import com.slurpeh.servercore.practice.KohiPractice;
import com.slurpeh.servercore.practice.gametype.GameType;
import com.slurpeh.servercore.practice.match.Queue;
import com.slurpeh.servercore.practice.team.Team;
import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

/**
 * Created by Bradley on 6/9/16.
 */
public class TwoVTwoMatchManager implements Listener {
    List<Match2v2> matches;
    List<TwoVTwoQueue> queues;
    KohiPractice plugin;
    public TwoVTwoMatchManager(KohiPractice plugin) {
        this.plugin = plugin;
        this.matches = new ArrayList<>();
        this.queues = new ArrayList<>();
        for (GameType gt : plugin.getGameTypeManager().getGameTypes()) {
            this.queues.add(new TwoVTwoQueue(gt));
        }
        for (TwoVTwoQueue q : queues) {
            Bukkit.getPluginManager().registerEvents(q, this.plugin);
        }
        Bukkit.getPluginManager().registerEvents(this, this.plugin);
        new BukkitRunnable() {
            @Override
            public void run() {
                for (TwoVTwoQueue q : queues) {
                    for (Team t : q.getAwaiting().keySet()) {
                        GameType gt = q.getGame();
                        Match2v2 m = new Match2v2(gt.getPossibleArenas().isEmpty() ? plugin.getArenaManager().getArenas().get(new Random().nextInt(plugin.getArenaManager().getArenas().size())) : gt.getPossibleArenas().get(new Random().nextInt(gt.getPossibleArenas().size())), gt, t, q.getAwaiting().get(t));
                        m.startMatch();
                        matches.add(m);
                        plugin.getUnranked2v2Inventory().update();
                    }
                }
            }
        }.runTaskTimer(this.plugin, 0l, 5l);
    }

    public TwoVTwoQueue getQueue(Predicate<TwoVTwoQueue> test) {
        for (TwoVTwoQueue tvt : queues) {
            if (test.test(tvt)) {
                return tvt;
            }
        }
        return null;
    }

    public TwoVTwoQueue getQueue(GameType gt) {
        return getQueue(q -> q.getGame() == gt);
    }

    public void addToQueue(Team t, GameType gt) {
        getQueue(gt).addToQueue(t);
    }

    public int getAmountInQueue(GameType gt) {
        return getQueue(gt).getQueue().size();
    }

    public int getAmountInMatch(GameType gt) {
        return getMatches(m -> m.getGametype() == gt).size() * 4;
    }

    public Match2v2 getMatch(Predicate<Match2v2> t) {
        for (Match2v2 m : matches) {
            if (t.test(m)) {
                return m;
            }
        }
        return null;
    }

    public List<Match2v2> getMatches(Predicate<Match2v2> t) {
        List<Match2v2> matches = new ArrayList<>();
        for (Match2v2 m2v2 : this.matches) {
            if (t.test(m2v2)) {
                matches.add(m2v2);
            }
        }
        return matches;
    }

    public GameType getGt(Player ply) {
        return getMatch(ply).getGametype();
    }

    public List<Match2v2> get2v2Matches() {
        return matches;
    }

    public Match2v2 getMatch(Player ply) {
        if (isInMatch(ply)) {
            for (Match2v2 m2v2 : matches) {
                if (m2v2.hasPlayer(ply)) {
                    return m2v2;
                }
            }
        }
        return null;
    }

    public boolean isInMatch(Player ply) {
        for (Match2v2 m2v2 : matches) {
            if (m2v2.hasPlayer(ply)) {
                return true;
            }
        }
        return false;
    }

    public void endMatch(Match2v2 m) {
        plugin.getUnranked2v2Inventory().update();
        matches.remove(m);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (!isInMatch(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent e) {
        if (!isInMatch((Player)e.getEntity())) {
            e.setCancelled(true);
        }
    }
}
