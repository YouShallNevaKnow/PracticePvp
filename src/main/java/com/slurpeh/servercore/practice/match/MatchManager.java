package com.slurpeh.servercore.practice.match;

import com.slurpeh.servercore.practice.KohiPractice;
import com.slurpeh.servercore.practice.arena.Arena;
import com.slurpeh.servercore.practice.gametype.GameType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

/**
 * Created by Bradley on 5/15/16.
 */
public class MatchManager implements Listener {
    //incomplete
    private KohiPractice plugin;
    private java.util.List<Match> matches;
    private java.util.List<Queue> queues;

    public MatchManager(KohiPractice plugin) {
        this.plugin = plugin;
        this.matches = new ArrayList<>();
        this.queues = new ArrayList<>();
        for (GameType gt : this.plugin.getGameTypeManager().getGameTypes()) {
            this.queues.add(new Queue(gt, true));
            this.queues.add(new Queue(gt, false));
        }
        for (Queue queue : this.queues) {
            Bukkit.getPluginManager().registerEvents(queue, this.plugin);
        }
        Bukkit.getPluginManager().registerEvents(this, this.plugin);
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Queue que : MatchManager.this.queues) {
                    Iterator<Player> iter = que.getAwaitingMatch().keySet().iterator();
                    while (iter.hasNext()) {
                        Player ply = iter.next();
                        GameType gt = que.getGame();
                        Match match = new Match(gt.getPossibleArenas().isEmpty() ? plugin.getArenaManager().getArenas().get(new Random().nextInt(plugin.getArenaManager().getArenas().size())) : gt.getPossibleArenas().get(new Random().nextInt(gt.getPossibleArenas().size())), gt, ply, que.getAwaitingMatch().get(ply), que.isRanked());
                        match.startMatch();
                        matches.add(match);
                        que.startMatch(ply);
                        if (que.isRanked()) {
                            plugin.getRankedInventory().updateInventory();
                        } else {
                            plugin.getUnrankedInventory().updateInventory();
                        }
                    }
                }
            }
        }.runTaskTimer(this.plugin, 0, 5);
    }

    public void startMatch(final Player ply, final Player ply2, final GameType gt, Arena a, final boolean ranked) {
        final Match match = new Match(a, gt, ply, ply2, ranked);
        match.startMatch();
        this.matches.add(match);
    }

    private Queue getQueue(final Predicate<Queue> test) {
        for (final Queue queue : this.queues) {
            if (test.test(queue)) {
                return queue;
            }
        }
        return null;
    }

    private Queue getQueue(final GameType gt, final boolean ranked) {
        return this.getQueue(queue -> queue.isRanked() == ranked && queue.getGame() == gt);
    }

    public void addToQueue(final Player ply, final GameType gt, final boolean ranked) {
        this.getQueue(gt, ranked).addToQueue(ply, this.plugin.getPlayerDataManager().getRating(ply, gt));
    }

    public int getAmountInQueue(final GameType gt, final boolean ranked) {
        return this.getQueue(gt, ranked).getQueue().keySet().size();
    }

    public int getAmountInMatch(final GameType gt, final boolean ranked) {
        return this.getMatches(match -> match.getGameType() == gt && match.isRanked() == ranked).size() * 2;
    }

    public Match getMatch(final Predicate<Match> test) {
        for (final Match match : this.matches) {
            if (test.test(match)) {
                return match;
            }
        }
        return null;
    }

    public List<Match> getMatches(final Predicate<Match> test) {
        final List<Match> matches = new ArrayList<Match>();
        for (final Match match : this.matches) {
            if (test.test(match)) {
                matches.add(match);
            }
        }
        return matches;
    }

    public Match getMatch(final Player ply) {
        return this.getMatch(match -> match.hasPlayer(ply));
    }

    public GameType getGameType(final Player ply) {
        return this.getMatch(ply).getGameType();
    }

    public boolean isInMatch(final Player ply) {
        return this.getMatch(ply) != null;
    }

    @EventHandler
    public void onDropItem(final PlayerDropItemEvent event) {
        if (!this.isInMatch(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDamage(final EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && !this.isInMatch((Player)event.getEntity()) && !plugin.get2v2MatchManager().isInMatch((Player) event.getEntity()) && !this.plugin.getTeamManager().hasTeam((Player)event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFoodChange(final FoodLevelChangeEvent event) {
        if (!this.isInMatch((Player)event.getEntity())) {
            event.setCancelled(true);
        }
    }

    public void endMatch(final Match match) {
        if (match.isRanked()) {
            this.plugin.getRankedInventory().updateInventory();
        } else {
            this.plugin.getUnrankedInventory().updateInventory();
        }
        this.matches.remove(match);
    }

    public List<Match> getMatches() {
        return matches;
    }

    public List<Queue> getQueues() { return queues; }
}
